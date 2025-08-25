package com.example.aitutor.exercise;

import java.time.Instant;
import java.util.List;              // ✅ add
import java.util.Map;               // ✅ add

import com.example.aitutor.article.Article;
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.Type;   // ✅ add

@Entity
@Table(name = "exercise_set")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExerciseSet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "article_id")          // ✅ 建議加上
  private Article article;

  @Column(nullable = false)
  private String difficulty;

  // spec：Map -> 存成 jsonb
  @Type(JsonType.class)                     // ✅ 需要 org.hibernate.annotations.Type 的 import
  @Column(columnDefinition = "jsonb", nullable = false)
  private Map<String, Object> spec;

  // items：List<Question> -> 存成 jsonb
  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb", nullable = false)
  private List<Question> items;

  @Builder.Default
  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
