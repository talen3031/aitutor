package com.example.aitutor.submission_listening;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 儲存一次聽力作答提交結果（對應一個 ExerciseSetListening）
 * - answers: 使用者送出的原始答案（原封不動存 jsonb）
 * - results: 系統比對後的逐題結果（含是否正確、正解、使用者答案、解析）
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "submissions_listening")
public class ListeningSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long exerciseSetId;

    /** 使用者提交的原始答案（jsonb） */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> answers;

    /** 系統比對後的逐題結果（jsonb） */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> results;

    private Integer total;
    private Integer correct;

    /** 例如 3/4 = 0.75 */
    private Double score;

    @CreationTimestamp
    private OffsetDateTime createdAt;
}
