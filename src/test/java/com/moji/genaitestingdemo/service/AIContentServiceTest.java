package com.moji.genaitestingdemo.service;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIContentServiceTest {

    @Mock
    private OpenAiService openAiService;

    @InjectMocks
    private AIContentService aiContentService;

    @BeforeEach
    void setUp() {
        // Set properties that would normally come from application.yml
        ReflectionTestUtils.setField(aiContentService, "model", "gpt-3.5-turbo");
        ReflectionTestUtils.setField(aiContentService, "temperature", 0.7);
        ReflectionTestUtils.setField(aiContentService, "maxTokens", 500);
    }

    @Test
    void shouldGenerateContentSuccessfully() {
        // Arrange
        String expectedContent = "This is a generated response";

        ChatMessage responseMessage = new ChatMessage("assistant", expectedContent);

        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setMessage(responseMessage);

        ChatCompletionResult mockResult = new ChatCompletionResult();
        List<ChatCompletionChoice> choices = new ArrayList<>();
        choices.add(choice);
        mockResult.setChoices(choices);

        when(openAiService.createChatCompletion(any(ChatCompletionRequest.class)))
                .thenReturn(mockResult);

        // Act
        String result = aiContentService.generateContent("Test prompt");

        // Assert
        assertThat(result).isEqualTo(expectedContent);
    }

    @Test
    void shouldThrowExceptionWhenApiCallFails() {
        // Arrange
        when(openAiService.createChatCompletion(any(ChatCompletionRequest.class)))
                .thenThrow(new RuntimeException("API Error"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () ->
                aiContentService.generateContent("Test prompt")
        );

        assertThat(exception.getMessage()).contains("Failed to generate content");
    }
}