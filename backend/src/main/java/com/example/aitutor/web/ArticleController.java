package com.example.aitutor.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aitutor.article.Article;
import com.example.aitutor.article.ArticleService;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

  private final ArticleService articleService;

  public record FetchReq(@NotBlank String url) {}

  @PostMapping("/fetch")
  public Map<String, Object> fetch(@RequestBody FetchReq req) {
    Article a = articleService.fetchAndSave(req.url());
    String text = a.getCleanedText() == null ? "" : a.getCleanedText();
    String[] paragraphs = text.split("\\n\\n+");

    return Map.of(
        "id", a.getId(),
        "source", a.getSource(),
        "url", a.getSourceUrl(),
        "title", a.getTitle(),
        "paragraphs", paragraphs
    );
  }

  @GetMapping("/{id}")
  public Map<String, Object> get(@PathVariable Long id) {
    Article a = articleService.get(id);
    String text = a.getCleanedText() == null ? "" : a.getCleanedText();
    String[] paragraphs = text.split("\\n\\n+");

    return Map.of(
        "id", a.getId(),
        "source", a.getSource(),
        "url", a.getSourceUrl(),
        "title", a.getTitle(),
        "paragraphs", paragraphs
    );
  }
}
