package com.rag.enterprise.app.controller;

import com.rag.enterprise.app.service.KnowledgeRetrievalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/diagnostic")
public class DiagnosticRetrievalController {

    private final KnowledgeRetrievalService retrievalService;

    // Injecting the new Hybrid Engine!
    public DiagnosticRetrievalController(KnowledgeRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @PostMapping("/retrieve")
    public ResponseEntity<Map<String, Object>> retrieveKnowledge(@RequestBody DiagnosticQueryRequest request) {

        // Fire the dual-vector search (Dense + Sparse) natively into Qdrant
        List<String> fusedChunks = retrievalService.retrieveChunks(request.query());

        // Return a clean JSON receipt
        return ResponseEntity.ok(Map.of(
                "query_executed", request.query(),
                "fused_chunks_returned", fusedChunks.size(),
                "results", fusedChunks
        ));
    }
}

/**
 * Enterprise DTO mapping record.
 * Jackson maps the JSON key "query" straight to this field.
 */
record DiagnosticQueryRequest(String query) {}