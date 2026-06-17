package com.rag.enterprise.app.controller;
import com.rag.enterprise.app.domain.DocumentChunk;
import com.rag.enterprise.app.service.DocumentIngestionService;
import com.rag.enterprise.app.service.VectorStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentIngestionController {

    private final DocumentIngestionService ingestionService;
    private final VectorStorageService vectorStorageService;

    @Autowired
    public DocumentIngestionController(DocumentIngestionService ingestionService,
                                       VectorStorageService vectorStorageService) {
        this.ingestionService = ingestionService;
        this.vectorStorageService = vectorStorageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadDocument(@RequestBody MultipartFile file) {
        // 1. Ingest, validate, and parse the PDF into token-aware chunks
        List<DocumentChunk> chunks = ingestionService.processPdf(file);

        // 2. Persist those chunks into Qdrant using Spring AI's VectorStore
        vectorStorageService.persistChunks(chunks);

        // 3. Return a clean, production-ready receipt payload
        return ResponseEntity.ok(Map.of(
                "message", "Document successfully processed and indexed into vector storage.",
                "filename", file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown",
                "chunks_generated", chunks.size(),
                "timestamp", System.currentTimeMillis()
        ));
    }
}