package com.example.aitutor.exercise.llm;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class DummyLlmClient implements LlmClient {
  @Override
  public String completeJson(String prompt) {
    return """
      {
        "items": [
          {
            "type": "mcq",
            "prompt": "What is the main idea?",
            "options": ["A","B","C","D"],
            "answer": "B",
            "explanation": "..."
          }
        ]
      }
      """;
  }
}
