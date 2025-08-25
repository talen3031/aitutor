package com.example.aitutor.article;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ArticleRepository extends JpaRepository<Article, Long> {

  /** 依來源網址查找（去重用；不變更 DB 結構的最小方案） */
  Optional<Article> findBySourceUrl(String sourceUrl);

  /** 若未來要用內容雜湊去重，可保留此方法並在 Entity 加上 contentHash 欄位 */
  Optional<Article> findByContentHash(String contentHash);
  Page<Article> findByTitleContainingIgnoreCaseOrSourceContainingIgnoreCase(
      String title, String source, Pageable pageable);
}
