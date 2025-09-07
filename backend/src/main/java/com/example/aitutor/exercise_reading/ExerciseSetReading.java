package com.example.aitutor.exercise_reading;

import java.time.Instant;
import java.util.List;              // ✅ add
import java.util.Map;               // ✅ add

import org.hibernate.annotations.Type;

import com.example.aitutor.article.Article;
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;   // ✅ add
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "exercise_set_reading")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExerciseSetReading {

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
