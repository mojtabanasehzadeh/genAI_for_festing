package com.moji.genaitestingdemo.controller;

import com.moji.genaitestingdemo.dto.ContentRequest;
import com.moji.genaitestingdemo.dto.ContentResponse;
import com.moji.genaitestingdemo.service.AIContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final AIContentService aiContentService;

    @PostMapping("/generate")
    public ResponseEntity<ContentResponse> generateContent(@RequestBody ContentRequest request) {
        String generatedContent = aiContentService.generateContent(request.getPrompt());

        ContentResponse response = new ContentResponse();
        response.setContent(generatedContent);
        response.setPromptUsed(request.getPrompt());

        return ResponseEntity.ok(response);
    }
}