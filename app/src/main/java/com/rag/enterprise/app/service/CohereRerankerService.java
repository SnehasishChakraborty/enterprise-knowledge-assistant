package com.rag.enterprise.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CohereRerankerService {

    private final RestClient restClient;

    public CohereRerankerService(@Value("${cohere.api.key}") String apiKey) {
        // Initialize modern Spring RestClient with the Cohere Authorization header
        this.restClient = RestClient.builder()
                .baseUrl("https://api.cohere.com/v1/rerank")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public List<String> rerankContext(String userQuery, List<String> retrievedChunks) {
        if (retrievedChunks == null || retrievedChunks.isEmpty()) {
            return List.of();
        }

        // 1. Build the Cohere Request Payload
        Map<String, Object> requestPayload = Map.of(
                "model", "rerank-english-v3.0",
                "query", userQuery,
                "documents", retrievedChunks,
                "top_n", 3 // Only keep the absolute best 3 chunks for the LLM
        );

        // 2. Fire the network call to Cohere's Reranker
        Map<String, Object> response = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestPayload)
                .retrieve()
                .body(Map.class);

        // 3. Parse the results and reorder the original chunks
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

        System.out.println("🧠 Cohere Reranking complete! Reduced " + retrievedChunks.size() + " chunks down to top 3.");

        // Extract the index of the winning chunks and map them back to your strings
        return results.stream()
                .map(result -> (Integer) result.get("index"))
                .map(retrievedChunks::get)
                .collect(Collectors.toList());
    }
}