package com.rag.enterprise.app.service;

import com.rag.enterprise.app.domain.DocumentChunk;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VectorStorageService {

    private final VectorStore vectorStore;

    // Spring AI automatically injects the autoconfigured QdrantVectorStore bean here
    @Autowired
    public VectorStorageService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Accepts chunks from Sprint 2 Ingestion, converts them into Spring AI documents,
     * generates embeddings behind the scenes, and pushes them to Qdrant.
     */
    public void persistChunks(List<DocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }

        // Map internal domain records into Spring AI Documents
        List<Document> springAiDocuments = chunks.stream()
                .map(chunk -> new Document(
                        chunk.id(),          // Retain our generated UUID
                        chunk.content(),     // The text block to embed
                        chunk.metadata()     // Source page, lineage, and timestamps
                ))
                .collect(Collectors.toList());

        /*
         * .accept() is the industry-standard bulk write call.
         * Behind the scenes, Spring AI automatically calls the EmbeddingModel,
         * generates the mathematical arrays, attaches them to the payload,
         * and posts them to your running Qdrant instance.
         */
        vectorStore.accept(springAiDocuments);
    }
}
