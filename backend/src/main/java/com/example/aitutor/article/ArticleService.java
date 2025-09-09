package com.example.aitutor.article;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleService {

  private final ArticleRepository repo;
  private final JsoupFetcher fetcher;

  /**
   * 抓取→清洗(段落)→抽標題→以 content_hash 去重→保存
   * 注意：資料表對 content_hash 設了 UNIQUE NOT NULL，因此這裡以內容雜湊去重。
   */
  @Transactional
  public Article fetchAndSave(String url) {
    // 先以 sourceUrl 去重（若已存在則直接回傳）
    var byUrl = repo.findBySourceUrl(url);
    if (byUrl.isPresent()) return byUrl.get();

    // 取得原始 HTML
    String html = fetcher.fetchHtml(url);
    // 轉為純文字段落（\n\n 分段）
    String cleaned = fetcher.cleanToParagraphs(html);
    // 擷取標題（若取不到讓它為 null 亦可）
    String title = fetcher.extractTitle(html);

    // 以 cleaned 內容計算 hash 做去重
    String contentHash = fetcher.sha256(cleaned);

    var dup = repo.findByContentHash(contentHash);
    if (dup.isPresent()) return dup.get();

    // 建立並保存
    Article a = Article.builder()
        .title(title)
        .source(extractHost(url))
        .sourceUrl(url)
        .license(null)       // 若未解析授權/語言，先保留 null
        .lang(null)
        .cleanedText(cleaned)
        .contentHash(contentHash)
        .fetchedAt(Instant.now())
        .build();

    return repo.save(a);
  }

  @Transactional(readOnly = true)
  public Article get(Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Article not found: " + id));
  }
  
  public Map<String,Object> findAllPaged(int page, int size, String q) {
    Pageable pageable = PageRequest.of(
        page,
        size
        // ,Sort.by(Sort.Direction.DESC, "fetchedAt")  // 依 fetchedAt DESC 排序
    );
    Page<Article> result;

    if (q != null && !q.isBlank()) {
      result = repo.findByTitleContainingIgnoreCaseOrSourceContainingIgnoreCase(q, q, pageable);
    } else {
      result = repo.findAll(pageable);
    }

  return Map.of(
    "content", result.getContent().stream().map(a -> Map.of(
        "id", a.getId(),
        "title", a.getTitle(),
        "source", a.getSource(),
        "url", a.getSourceUrl(),
        "fetchedAt", a.getFetchedAt()
    )).toList(),
    "page", result.getNumber(),
    "size", result.getSize(),
    "totalElements", result.getTotalElements(),
    "totalPages", result.getTotalPages()
  );
}
  // 若其他服務需要，可用這個別名；或直接呼叫 get(id) 也行
  @Transactional(readOnly = true)
  public Article findById(Long id) {
    return get(id);
  }

  private String extractHost(String url) {
    try {
      return URI.create(url).getHost();
    } catch (Exception e) {
      return null;
    }
  }
}