package com.example.aitutor.article;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "article") // ⚠️ 對齊你的既有資料表「article」（單數）
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Article {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 對應 TEXT
  @Column(columnDefinition = "TEXT")
  private String title;

  @Column(columnDefinition = "TEXT")
  private String source;

  // 對應 source_url TEXT
  @Column(name = "source_url", columnDefinition = "TEXT")
  private String sourceUrl;

  // 對應 license TEXT
  @Column(columnDefinition = "TEXT")
  private String license;

  // 對應 lang TEXT
  @Column(columnDefinition = "TEXT")
  private String lang;

  // 對應 cleaned_text TEXT NOT NULL
  @Lob
  @Column(name = "cleaned_text", columnDefinition = "TEXT", nullable = false)
  private String cleanedText;

  // 對應 content_hash TEXT UNIQUE NOT NULL
  @Column(name = "content_hash", columnDefinition = "TEXT", nullable = false, unique = true)
  private String contentHash;

  // 對應 fetched_at TIMESTAMP NOT NULL DEFAULT NOW()
  @Column(name = "fetched_at", nullable = false)
  private Instant fetchedAt;
  
  @PrePersist
  void prePersist() {
    if (fetchedAt == null) fetchedAt = Instant.now();
}
}


