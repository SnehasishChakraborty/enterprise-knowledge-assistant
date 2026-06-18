package com.rag.enterprise.app.controller;

import com.rag.enterprise.app.service.RAGGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
public class DocumentChatController {

    private final RAGGenerationService generationService;

    public DocumentChatController(RAGGenerationService generationService) {
        this.generationService = generationService;
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askQuestion(@RequestBody ChatRequest request) {
        String aiAnswer = generationService.generateAnswer(request.query());

        return ResponseEntity.ok(Map.of(
                "query", request.query(),
                "answer", aiAnswer,
                "timestamp", System.currentTimeMillis()
        ));
    }
}

record ChatRequest(String query) {}
