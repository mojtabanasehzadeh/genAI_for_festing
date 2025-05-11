package com.moji.genaitestingdemo.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIContentService {

    private final OpenAiService openAiService;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.temperature}")
    private Double temperature;

    @Value("${openai.max-tokens}")
    private Integer maxTokens;

    /**
     * Generates text content based on a given prompt using OpenAI
     */
    public String generateContent(String prompt) {
        log.info("Generating content for prompt: {}", prompt);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .messages(List.of(new ChatMessage("user", prompt)))
                .build();

        try {
            String response = openAiService.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();
            log.info("Generated content successfully");
            return response;
        } catch (Exception e) {
            log.error("Error generating content: {}", e.getMessage());
            throw new RuntimeException("Failed to generate content", e);
        }
    }
}