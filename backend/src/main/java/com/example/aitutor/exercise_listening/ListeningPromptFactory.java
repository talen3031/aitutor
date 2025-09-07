package com.example.aitutor.exercise_listening;

import org.springframework.stereotype.Component;

@Component
public class ListeningPromptFactory {

    public String buildPrompt(String difficulty, int numQuestions, String topic, String genre) {
        // topic 可為 null/空字串時退回 general
        String t = (topic == null || topic.isBlank()) ? "general" : topic;
        // genre 為 dialogue | short
        String g = (genre == null || genre.isBlank()) ? "dialogue" : genre; 
        
        return String.format(
            "You are an English teacher. Generate a listening exercise.\n" +
            "Requirements:\n" +
            "- difficulty: %s\n" +
            "- topic: %s (ensure the transcript and questions reflect this theme)\n" +
            "- genre: %s (dialogue=conversation; short=passage)\n" +
            "- Include a short transcript (dialogue or passage) and %d multiple-choice questions.\n" +
            " Each question must include:\n" +
            "- question: the question text\n" +
            "- options: an array of 4 answer choices (strings)\n" +
            "- answer: the index (0–3) of the correct option\n" +
            "- explanation: a short explanation of why that answer is correct\n\n" +
            "Return JSON with fields: transcript, questions[].",
            difficulty, t,g, numQuestions
        );
    }
}
