package com.example.aitutor.exercise_listening;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "exercise_set_listening")
@Getter
@Setter
public class ExerciseSetListening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String difficulty;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String transcript;

    @Column(nullable = false)
    private String audioUrl;

    // spec：Map -> 存成 jsonb
    @Type(JsonType.class)   // ✅ Hibernate Types
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> spec;

    // items：List<ListeningQuestion> -> 存成 jsonb
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<ListeningQuestion> items;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
