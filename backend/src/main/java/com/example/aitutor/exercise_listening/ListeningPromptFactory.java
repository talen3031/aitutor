package com.example.aitutor.exercise_listening;

import org.springframework.stereotype.Component;

@Component
public class ListeningPromptFactory {

    public String buildPrompt(String difficulty, int numQuestions, String topics, String genre) {

        String t = (topics == null || topics.isBlank()) ? "general" : topics;
        String g = (genre == null || genre.isBlank()) ? "dialogue" : genre;
        
        String genreRules = "dialogue".equalsIgnoreCase(g)
            ? "- Transcript must be dialogue with speaker labels (A:, B: or names).\n- Keep 6–10 short turns."
            : "- Transcript must be a short narrative passage (1–2 paragraphs).\n- NO speaker labels or colon-prefixed lines.";

        return String.format("""
            You are an English teacher. Generate a English listening exercise in JSON.

            Requirements:
            - difficulty: %s
            - topic: %s
            - genre: %s
            - Transcript rules:
              %s
            - Include %d multiple-choice questions.
            - transcript and all questions/answers MUST be written in English.

            Each question:
            - question (string)
            - options (4 strings)
            - answer (index 0–3)
            - explanation (string)

            Output JSON only:
            {
              "transcript": "...",
              "questions": [
                {"question":"...","options":["...","...","...","..."],"answer":0,"explanation":"..."}
              ]
            }
            """, difficulty, t, g, genreRules, numQuestions);
    }
}
