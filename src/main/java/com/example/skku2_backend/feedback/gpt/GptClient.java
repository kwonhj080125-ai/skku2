package com.example.skku2_backend.feedback.gpt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * 파이프라인 4단계: 실제 GPT API 호출.
 * 별도 SDK 없이 Spring 6.1+ 내장 RestClient로 OpenAI Chat Completions 엔드포인트를 직접 호출한다.
 */
@Slf4j
@Component
public class GptClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public GptClient(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model:gpt-4.1}") String model,
            ObjectMapper objectMapper
    ) {
        this.model = model;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public GptFeedbackResult chat(String systemPrompt, String userPrompt) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "temperature", 0.4,                              // 창작성보다 일관성이 중요
                "response_format", Map.of("type", "json_object"), // JSON 강제
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        JsonNode response = restClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        String rawContent = response
                .path("choices").get(0)
                .path("message").path("content")
                .asText();

        try {
            return objectMapper.readValue(rawContent, GptFeedbackResult.class);
        } catch (Exception e) {
            log.error("GPT 응답을 JSON으로 파싱하지 못함: {}", rawContent, e);
            throw new IllegalStateException("GPT 응답 파싱 실패", e);
        }
    }
}