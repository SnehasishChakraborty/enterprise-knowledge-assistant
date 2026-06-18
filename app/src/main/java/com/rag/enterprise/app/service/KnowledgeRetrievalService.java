package com.rag.enterprise.app.service;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeRetrievalService {

    private final VectorStore vectorStore;

    // Inject the same autoconfigured Qdrant VectorStore from Sprint 3
    public KnowledgeRetrievalService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Executes a semantic vector search against Qdrant to find chunks
     * that conceptually match the user's question.
     */
    public List<Document> retrieveContext(String userQuery) {
        if (userQuery == null || userQuery.isBlank()) {
            return List.of();
        }

        /*
         * SearchRequest is the industry-standard builder for defining RAG boundaries.
         * It allows us to control exactly how strict and how wide our search is.
         */
        SearchRequest request = SearchRequest.builder()
                .query(userQuery)
                .topK(4)                       // Retrieve the 4 most relevant chunks
                .similarityThreshold(0.30)     // Discard anything with a semantic match score below 75%
                // .filterExpression("page_number == 1") <-- This is where we will add Arkadipta's metadata filters later!
                .build();

        /*
         * The vectorStore automatically:
         * 1. Calls the OpenAI Embedding API to vectorize the 'userQuery'.
         * 2. Executes a Cosine Similarity search in Qdrant.
         * 3. Maps the raw JSON payloads back into Java Document objects.
         */
        return vectorStore.similaritySearch(request);
    }
}
