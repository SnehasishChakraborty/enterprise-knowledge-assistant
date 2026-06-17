package com.rag.enterprise.app.controller;
import com.rag.enterprise.app.domain.DocumentChunk;
import com.rag.enterprise.app.service.DocumentIngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentIngestionController {

    private final DocumentIngestionService ingestionService;

    @Autowired
    public DocumentIngestionController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<DocumentChunk>> uploadDocument(@RequestParam("file") MultipartFile file) {
        List<DocumentChunk> chunks = ingestionService.processPdf(file);
        return ResponseEntity.ok(chunks);
    }
}