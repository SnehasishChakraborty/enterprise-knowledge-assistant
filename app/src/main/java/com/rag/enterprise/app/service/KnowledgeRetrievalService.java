package com.rag.enterprise.app.service;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KnowledgeRetrievalService {

    private final QdrantClient qdrantClient;
    private final EmbeddingModel embeddingModel;
    private final KeywordSparseEncoderService sparseEncoder;

    // Must match the collection we just rebuilt
    private static final String COLLECTION_NAME = "enterprise_knowledge";

    public KnowledgeRetrievalService(QdrantClient qdrantClient,
                                     EmbeddingModel embeddingModel,
                                     KeywordSparseEncoderService sparseEncoder) {
        this.qdrantClient = qdrantClient;
        this.embeddingModel = embeddingModel;
        this.sparseEncoder = sparseEncoder;
    }

    public List<String> retrieveChunks(String userQuery) {

        // 1. Generate the Dense Vector (Semantic Meaning)
        float[] denseVectorArray = embeddingModel.embed(userQuery);
        List<Float> denseVector = new ArrayList<>();
        for (float v : denseVectorArray) {
            denseVector.add(v);
        }

        // 2. Generate the Sparse Vector (Exact Keyword Matches)
        KeywordSparseEncoderService.SparseVector sparseVector = sparseEncoder.encode(userQuery);

        // 3. Build the Native Hybrid Query using Qdrant's Prefetch & RRF Engine
        Points.QueryPoints hybridQuery = Points.QueryPoints.newBuilder()
                .setCollectionName(COLLECTION_NAME)

                // --- STREAM 1: Semantic Search ---
                .addPrefetch(Points.PrefetchQuery.newBuilder()
                        .setQuery(Points.Query.newBuilder()
                                // Wrap the Vector in a VectorInput so Qdrant knows it's Dense
                                .setNearest(Points.VectorInput.newBuilder()
                                        .setDense(Points.DenseVector.newBuilder()
                                                .addAllData(denseVector)
                                                .build())
                                        .build())
                                .build())
                        .setUsing("text-dense") // Target the Dense index
                        .setLimit(20) // Grab top 20 conceptual matches
                        .build())

                // --- STREAM 2: Keyword Search ---
                .addPrefetch(Points.PrefetchQuery.newBuilder()
                        .setQuery(Points.Query.newBuilder()
                                // Wrap the SparseVector in a VectorInput so Qdrant knows it's Sparse
                                .setNearest(Points.VectorInput.newBuilder()
                                        .setSparse(Points.SparseVector.newBuilder()
                                                .addAllIndices(sparseVector.indices())
                                                .addAllValues(sparseVector.values())
                                                .build())
                                        .build())
                                .build())
                        .setUsing("text-sparse") // Target the Sparse index
                        .setLimit(20) // Grab top 20 exact keyword matches
                        .build())

                // --- THE FUSION ENGINE ---
                .setQuery(Points.Query.newBuilder()
                        // Fusion is a simple enum, no builder required
                        .setFusion(Points.Fusion.RRF)
                        .build())
                .setLimit(10) // Final output size after fusion
                .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                .build();

        // 4. Execute the unified query natively on the database
        try {
            List<Points.ScoredPoint> results = qdrantClient.queryAsync(hybridQuery).get();

            System.out.println("🔥 Hybrid Retrieval successful! Fused " + results.size() + " top chunks.");

            // Extract the raw text content to hand off to the LLM
            return results.stream()
                    .map(point -> point.getPayloadMap().get("content").getStringValue())
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("Failed to execute hybrid search in Qdrant", e);
        }
    }
}