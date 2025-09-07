package com.example.aitutor.exercise_reading;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aitutor.article.Article;
import com.example.aitutor.article.ArticleService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExerciseReadingService {

  private final ArticleService articleService;
  private final ExerciseSetReadingRepository repo;
  private final ReadingQuestionGenService questionGenService;
  private final ObjectMapper objectMapper; // Spring Boot 會自動提供

  /**
   * 產生題組；若已有相同 (articleId, difficulty, spec) 的題組，就直接回傳既有 id。
   * 與 Controller 對齊的簽名：
   *   generateIfAbsent(Long articleId, String difficulty, List<String> types, Map<String,Integer> count)
   */
  @Transactional
  public Long generateIfAbsent(Long articleId,
                               String difficulty,
                               List<String> types,
                               Map<String, Integer> count) {
    // 組 spec（必須與 DB 存的結構一致，避免比對失敗）
    Map<String, Object> spec = Map.of(
        "types", types,
        "count", count,
        "difficulty", difficulty
    );

    // 先查是否已存在：比對 (articleId, difficulty, spec::jsonb)
    String specJson = toJson(spec);
    Long existingId = repo.findExistingId(articleId, difficulty, specJson);
    if (existingId != null) {
      return existingId; // 已存在，回傳舊的 id
    }

    // 沒有才真正生成
    Article article = loadArticleOrThrow(articleId);

    List<Question> questions = questionGenService.generate(
        article.getCleanedText(),
        difficulty,
        types,
        count
    );

    ExerciseSetReading set = ExerciseSetReading.builder()
        .article(article)
        .difficulty(difficulty)
        .spec(spec)       // jsonb：Map<String,Object>
        .items(questions) // List<Question>
        .build();

    repo.save(set);
    return set.getId();
  }

  /**
   * 依 id 取得題組，無則拋錯（配合 Controller 的 get(Long) 呼叫）
   */
  @Transactional(readOnly = true)
  public ExerciseSetReading get(Long id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("ExerciseSet %d not found".formatted(id)));
        
  }

  // ====== 私有工具方法 ======

  private Article loadArticleOrThrow(Long articleId) {
    return articleService.findById(articleId);
  }

  private String toJson(Map<String, Object> spec) {
    try {
      return objectMapper.writeValueAsString(spec);
    } catch (Exception e) {
      throw new IllegalStateException("Serialize spec failed", e);
    }
  }
}
