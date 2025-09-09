package com.example.aitutor.exercise_listening;

import org.springframework.stereotype.Service;

import com.example.aitutor.llm.LlmClient;

@Service
public class ListeningQuestionGenService {

    private final LlmClient llmClient;
    private final ListeningPromptFactory promptFactory;

    public ListeningQuestionGenService(LlmClient llmClient, ListeningPromptFactory promptFactory) {
        this.llmClient = llmClient;
        this.promptFactory = promptFactory;
    }

    public String generateQuestions(String difficulty, int numQuestions, String topic, String genre) {
        String prompt = promptFactory.buildPrompt(difficulty, numQuestions, topic, genre);
        return llmClient.completeJson(prompt);
    }
}