// src/main/java/com/example/aitutor/llm/OpenAiTtsClient.java
package com.example.aitutor.llm;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.aitutor.storage.AudioStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiTtsClient {

    private final AudioStorage audioStorage;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.tts.url:https://api.openai.com/v1/audio/speech}")
    private String ttsUrl;

    @Value("${openai.tts.model:gpt-4o-mini-tts}")
    private String model;

    @Value("${openai.tts.voice:alloy}")
    private String voice;
    public String synthesize(String transcript) {
        String filename = "listening_" + UUID.randomUUID() + ".mp3";

        try {
            String body = """
            {
              "model": "%s",
              "voice": "%s",
              "input": %s,
              "format": "mp3"
            }
            """.formatted(model, voice, quoteForJson(transcript));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            ResponseEntity<byte[]> resp = restTemplate.exchange(
                    ttsUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(body.getBytes(StandardCharsets.UTF_8), headers),
                    byte[].class
            );

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new RuntimeException("TTS failed: " + resp.getStatusCode());
            }

            byte[] mp3 = resp.getBody();
            String publicUrl = audioStorage.save(filename, mp3);
            log.info("[TTS] Generated mp3 => {}", publicUrl);
            return publicUrl;

        } catch (Exception ex) {
            log.error("[TTS] error", ex);
            // 失敗時也可回傳一個靜音檔（dev/local）或直接拋錯
            throw new RuntimeException("TTS generation failed", ex);
        }
    }

    private static String quoteForJson(String s) {
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n") + "\"";
    }
   
}
