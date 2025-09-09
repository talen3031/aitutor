package com.example.aitutor.exercise_reading;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExerciseSetReadingRepository extends JpaRepository<ExerciseSetReading, Long> {

  @Query(
    value = """
      SELECT id
      FROM exercise_set_reading
      WHERE article_id = :articleId
        AND difficulty = :difficulty
        AND spec = CAST(:specJson AS jsonb)
      LIMIT 1
    """,
    nativeQuery = true
  )
  Long findExistingId(Long articleId, String difficulty, String specJson);
}
