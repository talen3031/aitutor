package com.example.aitutor.exercise_reading;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ReadingPromptFactory {

  public String build(String passage, String diff, List<String> types, Map<String,Integer> count) {
    StringBuilder sb = new StringBuilder();

    sb.append("You are an English pedagogy assistant.\n");
    sb.append("Target CEFR level: ").append(diff).append("\n");
    sb.append("Generate questions from the passage below.\n\n");

    sb.append("Passage:\n");
    sb.append(passage).append("\n\n");

    sb.append("Question types and counts requested: ").append(count).append("\n\n");

    sb.append("Return ONLY valid JSON with this schema:\n");
    sb.append("{\n");
    sb.append("  \"items\": [\n");
    sb.append("    {\n");
    sb.append("      \"type\": \"mcq\" | \"tf\", \n");
    sb.append("      \"prompt\": string,\n");
    sb.append("      \"options\": [string,...],   // for mcq only\n");
    sb.append("      \"answer\": number | boolean, // 0-based index for mcq, true/false for tf\n");
    sb.append("      \"explanation\": string\n");
    sb.append("    }\n");
    sb.append("  ]\n");
    sb.append("}\n\n");

    sb.append("Rules:\n");
    sb.append("- Output ONLY JSON. No markdown, no text before or after.\n");
    sb.append("- For 'mcq', `answer` must be the 0-based index of the correct option.\n");
    sb.append("- For 'tf', `answer` must be a boolean true/false.\n");
    sb.append("- Respect the requested number of each question type.\n");
    sb.append("- Keep explanations short and clear.\n");

    return sb.toString();
  }
}
