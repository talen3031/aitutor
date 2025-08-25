package com.example.aitutor.article;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 以 Jsoup 抓取與清洗文章的工具類。
 * 提供：
 *   - fetchHtml(url): 取回原始 HTML（帶 UA、redirect、Accept-Language）
 *   - cleanToParagraphs(html): 抽取正文並清洗為 \n\n 分段的純文字
 *   - extractTitle(html): 擷取標題（og:title → <title> → h1）
 *   - sha256(str): 內容雜湊（去重）
 *
 * 特化支援 BBC 新版頁面（data-component="text-block" 與 JSON-LD articleBody），
 * 並排除「More on this story / Related」等連結模組，避免混入正文。
 */
@Component
public class JsoupFetcher {

  private static final Duration TIMEOUT = Duration.ofSeconds(15);
  private static final String USER_AGENT =
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
          + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

  private static final Pattern MULTI_SPACE = Pattern.compile("[ \\t\\x0B\\f\\r]+");
  private static final Pattern MULTI_NL = Pattern.compile("\\n{2,}");

  private final ObjectMapper mapper = new ObjectMapper();

  /** 抓取原始 HTML 字串（含 redirect、UA、Accept-Language）。 */
  public String fetchHtml(String url) {
    try {
      Document doc = connect(url).get();
      return doc.outerHtml(); // 直接回傳 HTML 字串；後續以內容特徵判斷是否為 BBC
    } catch (IOException e) {
      throw new RuntimeException("Fetch HTML failed: " + url, e);
    }
  }

  /** 將 HTML 解析為純文字段落（\n\n 分段），帶站台特化（BBC）與多層 fallback。 */
  public String cleanToParagraphs(String html) {
    if (isBlank(html)) return "";
    Document doc = Jsoup.parse(html);

    // 判斷是否為 BBC 頁面（不依賴 baseUri；以內容特徵判斷）
    boolean isBBC = isBBC(doc);

    if (isBBC) {
      String bbc = extractFromBBC(doc);
      if (!isBlank(bbc)) return cleanText(bbc);
    }

    // 一般抽取（article / 常見容器）
    String generic = extractGeneric(doc);
    if (!isBlank(generic)) return cleanText(generic);

    // JSON-LD fallback（articleBody / text）
    String jsonld = extractFromJsonLd(doc);
    if (!isBlank(jsonld)) return cleanText(jsonld);

    // 全頁 fallback
    String fallback = extractWholePageFallback(doc);
    return cleanText(fallback);
  }

  /** 從 HTML 擷取標題（優先 og:title → <title> → 第一個 h1）。 */
  public String extractTitle(String html) {
    if (isBlank(html)) return null;
    Document doc = Jsoup.parse(html);

    // og:title
    Element og = doc.selectFirst("meta[property=og:title], meta[name=og:title]");
    if (og != null && og.hasAttr("content")) {
      String v = og.attr("content");
      if (!isBlank(v)) return v.trim();
    }
    // <title>
    String t = doc.title();
    if (!isBlank(t)) return t.trim();

    // 第一個 h1
    Element h1 = doc.selectFirst("h1");
    if (h1 != null) {
      String v = h1.text();
      if (!isBlank(v)) return v.trim();
    }
    return null;
  }

  /** SHA‑256 雜湊（用於內容去重）。 */
  public String sha256(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder();
      for (byte b : hash) hex.append(String.format("%02x", b));
      return hex.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // ====== 內部實作 ======

  private Connection connect(String url) {
    return Jsoup.connect(url)
        .userAgent(USER_AGENT)
        .timeout((int) TIMEOUT.toMillis())
        .followRedirects(true)
        .header("Accept-Language", "en-US,en;q=0.9,zh-TW;q=0.8,zh;q=0.7");
  }

  /** 以內容特徵判斷是否為 BBC 頁面（避免依賴 baseUri）。 */
  private boolean isBBC(Document doc) {
    // og:site_name=BBC 或 canonical 指向 bbc.com/news
    if (doc.selectFirst("meta[property=og:site_name][content*=BBC]") != null) return true;
    if (doc.selectFirst("link[rel=canonical][href*=bbc.com]") != null) return true;
    // 常見 BBC 正文容器特徵
    if (!doc.select("[data-component=text-block]").isEmpty()) return true;
    return false;
  }

  /**
   * BBC 新版正文抽取：
   *  - 只取 [data-component=text-block] 裡的 <p>
   *  - 可選取 [data-component=quote-block] 的 quotes
   *  - 排除相關連結模組（links-list/topic-list/related 等）、單一 <a> 段落
   */
  private String extractFromBBC(Document doc) {
    List<String> lines = new ArrayList<>();

    // 1) 正文段落
    Elements paras = doc.select("[data-component=text-block] p");
    for (Element p : paras) {
      if (isNoise(p)) continue;

      // 排除只有單一 <a> 的段落（多半是「相關閱讀」）
      if (p.children().size() == 1 && "a".equals(p.child(0).tagName())
          && p.text().equals(p.child(0).text())) {
        continue;
      }

      String t = p.text();
      if (!isBlank(t)) lines.add(t);
    }

    // 2) 引言（可選）
    Elements quotes = doc.select("[data-component=quote-block] blockquote, [data-component=quote-block] q");
    for (Element q : quotes) {
      if (isNoise(q)) continue;
      String t = q.text();
      if (!isBlank(t)) lines.add(t);
    }

    String joined = joinLines(lines);
    if (!isBlank(joined)) return joined;

    // 3) JSON‑LD fallback
    String jsonld = extractFromJsonLd(doc);
    if (!isBlank(jsonld)) return jsonld;

    // 4) 一般抽取
    return extractGeneric(doc);
  }

  /**
   * 一般站點抽取：優先 <article>，再嘗試常見容器，最後全頁段落。
   */
  private String extractGeneric(Document doc) {
    // 先嘗試 <article>
    Element article = doc.selectFirst("article");
    if (article != null) {
      String fromArticle = collectParagraphLike(article);
      if (!isBlank(fromArticle)) return fromArticle;
    }

    // 常見容器
    Elements containers = doc.select(
        "main, .content, .post, .article, .entry-content, #content, .rich-text, .story-body");
    for (Element c : containers) {
      String t = collectParagraphLike(c);
      if (!isBlank(t) && t.length() >= 200) return t; // 避免抓到過短區塊
    }

    // 退回：全頁 p/li/h* 等
    return collectParagraphLike(doc.body());
  }

  /**
   * 解析所有 <script type="application/ld+json">，擷取 articleBody/text。
   */
  private String extractFromJsonLd(Document doc) {
    Elements scripts = doc.select("script[type=application/ld+json]");
    List<String> bodies = new ArrayList<>();

    for (Element s : scripts) {
      // DataNode
      for (DataNode dn : s.dataNodes()) {
        parseJsonLd(dn.getWholeData(), bodies);
      }
      // innerHTML
      parseJsonLd(s.html(), bodies);
    }
    String joined = joinLines(bodies);
    return isBlank(joined) ? null : joined;
  }

  private void parseJsonLd(String raw, List<String> out) {
    if (isBlank(raw)) return;
    try {
      JsonNode node = mapper.readTree(raw);
      collectArticleBodyFromJsonLd(node, out);
    } catch (Exception ignored) {}
  }

  private void collectArticleBodyFromJsonLd(JsonNode node, List<String> out) {
    if (node == null) return;

    if (node.isObject()) {
      JsonNode body = node.get("articleBody");
      if (body != null && body.isTextual()) {
        String t = body.asText();
        if (!isBlank(t)) out.add(t);
      }
      JsonNode text = node.get("text");
      if (text != null && text.isTextual()) {
        String t = text.asText();
        if (!isBlank(t)) out.add(t);
      }
      JsonNode graph = node.get("@graph");
      if (graph != null && graph.isArray()) {
        for (JsonNode n : graph) collectArticleBodyFromJsonLd(n, out);
      }
      node.fieldNames().forEachRemaining(fn -> {
        JsonNode child = node.get(fn);
        if (child != null && (child.isObject() || child.isArray())) {
          collectArticleBodyFromJsonLd(child, out);
        }
      });
    } else if (node.isArray()) {
      for (JsonNode n : node) collectArticleBodyFromJsonLd(n, out);
    }
  }

  /**
   * 防呆「全頁 fallback」：抓 body 內 p/h1-h6/li/blockquote，濾掉常見噪音。
   */
  private String extractWholePageFallback(Document doc) {
    if (doc.body() == null) return "";
    // 移除常見噪音
    doc.select("nav, footer, header, aside, script, style, noscript, form, button, input, textarea, figure, figcaption, svg, video, audio")
        .remove();
    return collectParagraphLike(doc.body());
  }

  /**
   * 從容器收集「類段落」節點：p, h1-h6, li, blockquote；去重保序。
   */
  private String collectParagraphLike(Element root) {
    if (root == null) return "";
    Elements nodes = root.select("p, h1, h2, h3, h4, h5, h6, li, blockquote");
    List<String> lines = new ArrayList<>();
    for (Element el : nodes) {
      if (isNoise(el)) continue;
      String t = el.text();
      if (!isBlank(t)) lines.add(t);
    }
    Set<String> uniq = new LinkedHashSet<>(lines); // 去重保序
    return joinLines(new ArrayList<>(uniq));
  }

  /**
   * 噪音判斷：分享/導覽/廣告/推薦模組等；含 BBC 的 links-list/topic-list/related 等排除。
   */
  private boolean isNoise(Element el) {
    String cls = String.join(" ", el.classNames()).toLowerCase(Locale.ROOT);
    String id = el.id() == null ? "" : el.id().toLowerCase(Locale.ROOT);
    String blob = cls + " " + id;

    // 通用雜訊
    if (blob.contains("share") || blob.contains("social") || blob.contains("breadcrumb")
        || blob.contains("comment") || blob.contains("subscribe")
        || blob.contains("advert") || blob.contains(" ad-") || blob.contains("-ad-")
        || blob.contains("sponsor") || blob.contains("promo")
        || blob.contains("nav") || blob.contains("footer") || blob.contains("header")
        || blob.contains("btn") || blob.contains("button")) {
      return true;
    }

    // BBC 常見「相關/推薦/主題列表」容器：用 closest(...) 判斷祖先（注意：closest 回傳單一 Element 或 null）
    if (el.closest("[data-component=links-list], [data-component=topic-list], [data-component=tag-list], " +
                   "[data-component=unordered-list-block], [data-component=ordered-list-block], " +
                   "[data-component=related-content], [data-component=story-package]") != null) {
      return true;
    }

    // 祖先語意判斷
    Element sec = el.closest("section, aside, nav");
    if (sec != null) {
      String aria = (sec.hasAttr("aria-label") ? sec.attr("aria-label") : "") + " " +
                    (sec.hasAttr("aria-labelledby") ? sec.attr("aria-labelledby") : "");
      String ariaLc = aria.toLowerCase(Locale.ROOT);
      if (ariaLc.contains("related") || ariaLc.contains("more on this story")
          || ariaLc.contains("more") || ariaLc.contains("you may also like")) {
        return true;
      }
    }

    // 只有單一 <a> 的段落：視為噪音（多半是「相關閱讀」）
    if ("p".equals(el.tagName()) && el.children().size() == 1 && "a".equals(el.child(0).tagName())) {
      return true;
    }

    return false;
  }

  /** 統一清洗：去多空白、合併連續空行、修剪首尾。 */
  private String cleanText(String raw) {
    if (raw == null) return "";
    String s = raw.replace("\r\n", "\n").replace('\r', '\n');
    s = s.replace("\u00A0", " ").replace("\u200B", "");
    s = MULTI_SPACE.matcher(s).replaceAll(" ");
    s = MULTI_NL.matcher(s).replaceAll("\n\n");

    List<String> lines = s.lines()
        .map(String::trim)
        .filter(line -> !line.isEmpty())
        .collect(Collectors.toList());

    return String.join("\n\n", lines).trim();
  }

  private static String joinLines(List<String> lines) {
    if (lines == null || lines.isEmpty()) return "";
    List<String> filtered = lines.stream()
        .map(String::trim)
        .filter(t -> t.length() >= 2)
        .collect(Collectors.toList());
    return String.join("\n\n", filtered);
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  @SuppressWarnings("unused")
  private String hostOf(String url) {
    if (isBlank(url)) return null;
    try { return URI.create(url).getHost(); } catch (Exception e) { return null; }
  }
}
