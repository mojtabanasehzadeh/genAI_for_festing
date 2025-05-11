package com.moji.genaitestingdemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moji.genaitestingdemo.dto.ContentRequest;
import com.moji.genaitestingdemo.service.AIContentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContentController.class)
class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AIContentService aiContentService;

    @Test
    void shouldGenerateContentSuccessfully() throws Exception {
        // Arrange
        String prompt = "Write a haiku about testing AI";
        String generatedContent = "Testing AI systems\nLogic flows through silicon\nBugs disappear now";

        ContentRequest request = new ContentRequest();
        request.setPrompt(prompt);

        when(aiContentService.generateContent(anyString())).thenReturn(generatedContent);

        // Act & Assert
        mockMvc.perform(post("/api/content/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(generatedContent))
                .andExpect(jsonPath("$.promptUsed").value(prompt));
    }

    @Test
    void shouldReturn500WhenServiceFails() throws Exception {
        // Arrange
        ContentRequest request = new ContentRequest();
        request.setPrompt("Test prompt");

        when(aiContentService.generateContent(anyString()))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/api/content/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}