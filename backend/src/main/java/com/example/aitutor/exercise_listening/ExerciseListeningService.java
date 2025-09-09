package com.example.aitutor.exercise_listening;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.aitutor.llm.OpenAiTtsClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ExerciseListeningService {

    private final ExerciseSetListeningRepository repository;
    private final ListeningQuestionGenService questionGenService;
    private final ObjectMapper objectMapper;
    private final OpenAiTtsClient ttsClient;  
    
    public ExerciseListeningService(
            ExerciseSetListeningRepository repository,
            ListeningQuestionGenService questionGenService,
            ObjectMapper objectMapper,
            OpenAiTtsClient ttsClient
    ) {
        this.repository = repository;
        this.questionGenService = questionGenService;
        this.objectMapper = objectMapper;
        this.ttsClient = ttsClient;
    }

    public List<ExerciseSetListening> findAll() {
        Map<String, Integer> difficultyOrder = Map.of(
            "easy", 1,
            "medium", 2,
            "hard", 3
        );
        return repository.findAll().stream()
                .sorted(Comparator
                .comparing(ExerciseSetListening::getCreatedAt).reversed() // created_at DESC
                .thenComparing(e -> difficultyOrder.getOrDefault(e.getDifficulty(), 99)) // 照固定順序
                )
                .toList();
    }

    public ExerciseSetListening findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listening set not found: " + id));
    }

    public ExerciseSetListening generateExercise(String difficulty, int numQuestions,String topic,String genre) {
        try {
            // 呼叫 LLM 產生 JSON
            String responseJson = questionGenService.generateQuestions(difficulty, numQuestions,topic,genre);
            
            // 解析 JSON
            JsonNode root = objectMapper.readTree(responseJson);
            String transcript = root.get("transcript").asText();

            List<ListeningQuestion> questions = objectMapper.readValue(
                    root.get("questions").toString(),
                    new TypeReference<List<ListeningQuestion>>() {}
            );
            String topicSafe = (topic == null || topic.isBlank()) ? "general" : topic;
            String genreSafe = (genre == null || genre.isBlank()) ? "dialogue" : genre;
            Map<String, Object> spec = Map.of(
                    "difficulty", difficulty,
                    "numQuestions", numQuestions,
                    "topic", topicSafe,
                    "genre",genreSafe
            );
            
            // 3) 呼叫 TTS
            String audioUrl;
            try {
                log.info("[Listening] calling TTS...");
                audioUrl = ttsClient.synthesize(transcript); // 可能拋例外
                log.info("[Listening] TTS synthesized. url={}", audioUrl);
            } catch (Exception ex) {
                throw new RuntimeException("TTS generation failed for text=" + transcript, ex);

            }
            
            // === 存 DB ===
            ExerciseSetListening set = new ExerciseSetListening();
            set.setDifficulty(difficulty);
            set.setTranscript(transcript);
            set.setAudioUrl(audioUrl);
            set.setSpec(spec);
            set.setItems(questions);
            //log.info("[Listening] saved. id={}, audioUrl={}", set.getId(), set.getAudioUrl());

            return repository.save(set);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate listening exercise", e);
        }
    }
    
}
