package com.example.aitutor.exercise.llm;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("prod")
public class OpenAiLlmClient implements LlmClient {

  private final OpenAiService service;
  private final String model;

  public OpenAiLlmClient(@Value("${openai.api-key}") String apiKey,
                       @Value("${openai.model:gpt-4o-mini}") String model) {
  this.service = new OpenAiService(apiKey, Duration.ofSeconds(60)); // ← 加長 timeout
  this.model = model;
}
  @Override
  public String completeJson(String prompt) {
    ChatMessage system = new ChatMessage("system", "You are an assistant that outputs only JSON.");
    ChatMessage user = new ChatMessage("user", prompt);

    ChatCompletionRequest req = ChatCompletionRequest.builder()
        .model(model)
        .messages(List.of(system, user))
        .temperature(0.7)
        .build();

    var result = service.createChatCompletion(req);
    String content = result.getChoices().get(0).getMessage().getContent();

    log.info("=== RAW LLM prompt === \n{}", prompt);

    log.info("=== RAW LLM RESPONSE === \n{}", content);


    return content;
  }
}
