package com.rag.enterprise.app.controller;

import com.rag.enterprise.app.service.KnowledgeRetrievalService;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/diagnostic")
public class DiagnosticRetrievalController {

    private final KnowledgeRetrievalService retrievalService;

    public DiagnosticRetrievalController(KnowledgeRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> testSearch(@RequestBody DiagnosticQueryRequest request) {

        // 1. request.query() safely pulls ONLY the unescaped inner string value
        String cleanQuery = request.query();

        // 2. Execute vector search with clean text
        List<Document> matchedDocuments = retrievalService.retrieveContext(cleanQuery);

        List<Map<String, Object>> serializedResults = matchedDocuments.stream()
                .map(doc -> Map.of(
                        "id", doc.getId(),
                        "text_chunk", doc.getText(),
                        "metadata", doc.getMetadata(),
                        "score", doc.getMetadata().getOrDefault("distance", "N/A")
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "search_query", cleanQuery, // Verify this is now unescaped prose
                "chunks_found", serializedResults.size(),
                "results", serializedResults,
                "timestamp", System.currentTimeMillis()
        ));
    }
}

/**
 * Enterprise DTO mapping record.
 * Jackson maps the JSON key "query" straight to this field.
 */
record DiagnosticQueryRequest(String query) {}