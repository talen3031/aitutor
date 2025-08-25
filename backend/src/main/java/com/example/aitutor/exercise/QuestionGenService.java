package com.example.aitutor.exercise;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.aitutor.exercise.llm.LlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionGenService {
  private final PromptFactory promptFactory;
  private final ObjectMapper om;
  private final LlmClient llm;

  public List<Question> generate(String passage, String diff, List<String> types, Map<String, Integer> count) {
    String prompt = promptFactory.build(passage, diff, types, count);
    String json = llm.completeJson(prompt);

    List<Question> res = new ArrayList<>();
    try {
      var root = om.readTree(json);
      var items = root.path("items");
      if (items.isArray()) {
        for (var n : items) {
          Question q = new Question();
          q.setType(n.path("type").asText(null));
          q.setPrompt(n.path("prompt").asText(null));

          // options
          if (n.has("options") && n.get("options").isArray()) {
            List<String> opts = new ArrayList<>();
            for (var opt : n.get("options")) {
              opts.add(opt.asText());
            }
            q.setOptions(opts);
          }

          // answer (可能是 int / boolean / string)
          if (n.has("answer")) {
            if (n.get("answer").isInt()) {
              q.setAnswer(n.get("answer").asInt());
            } else if (n.get("answer").isBoolean()) {
              q.setAnswer(n.get("answer").asBoolean());
            } else if (n.get("answer").isTextual()) {
              q.setAnswer(n.get("answer").asText());
            }
          }

          // explanation
          if (n.has("explanation")) {
            q.setExplanation(n.get("explanation").asText());
          }

          res.add(q);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse LLM JSON: " + e.getMessage(), e);
    }

    return res;
  }
}
