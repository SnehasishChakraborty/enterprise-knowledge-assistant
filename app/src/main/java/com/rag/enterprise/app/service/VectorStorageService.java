package com.rag.enterprise.app.service;

import io.qdrant.client.PointIdFactory;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.ValueFactory;
import io.qdrant.client.grpc.Points;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VectorStorageService {

    private final QdrantClient qdrantClient;
    private final EmbeddingModel embeddingModel;
    private final KeywordSparseEncoderService sparseEncoder;

    private static final String COLLECTION_NAME = "enterprise_knowledge";

    public VectorStorageService(QdrantClient qdrantClient,
                                EmbeddingModel embeddingModel,
                                KeywordSparseEncoderService sparseEncoder) {
        this.qdrantClient = qdrantClient;
        this.embeddingModel = embeddingModel;
        this.sparseEncoder = sparseEncoder;
    }

    public void storeDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        int batchSize = 100;
        for (int i = 0; i < documents.size(); i += batchSize) {
            int end = Math.min(i + batchSize, documents.size());
            List<Document> batch = documents.subList(i, end);
            processBatch(batch);
        }
    }

    private void processBatch(List<Document> batch) {
        List<String> contents = batch.stream().map(Document::getText).toList();
        List<float[]> denseVectors = embeddingModel.embed(contents);

        List<Points.PointStruct> points = new ArrayList<>();

        for (int i = 0; i < batch.size(); i++) {
            Document doc = batch.get(i);
            String text = doc.getText();

            // 1. Get the 32-bit Integer Sparse Vector
            KeywordSparseEncoderService.SparseVector sparseVector = sparseEncoder.encode(text);

            // 2. Build the Dual-Vector Map explicitly using NamedVectors
            Points.Vectors dualVectors = Points.Vectors.newBuilder()
                    .setVectors(Points.NamedVectors.newBuilder()
                            .putVectors("text-dense", Points.Vector.newBuilder()
                                    .addAllData(toBoxedList(denseVectors.get(i)))
                                    .build())
                            .putVectors("text-sparse", Points.Vector.newBuilder()
                                    .setSparse(Points.SparseVector.newBuilder()
                                            .addAllIndices(sparseVector.indices())
                                            .addAllValues(sparseVector.values())
                                            .build())
                                    .build())
                            .build())
                    .build();

            // 3. Assemble the point (Parse the String ID into a strict UUID)
            Points.PointStruct point = Points.PointStruct.newBuilder()
                    .setId(PointIdFactory.id(java.util.UUID.fromString(doc.getId())))
                    .setVectors(dualVectors)
                    .putAllPayload(buildPayload(doc))
                    .build();

            points.add(point);
        }

        try {
            qdrantClient.upsertAsync(COLLECTION_NAME, points).get();
            System.out.println("✅ Hybrid Batch Ingested successfully. Count: " + batch.size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to push hybrid vectors to Qdrant", e);
        }
    }

    private List<Float> toBoxedList(float[] primitiveArray) {
        List<Float> list = new ArrayList<>(primitiveArray.length);
        for (float v : primitiveArray) {
            list.add(v);
        }
        return list;
    }

    // Notice how ValueFactory completely eliminates the messy protobuf value casting
    private Map<String, io.qdrant.client.grpc.JsonWithInt.Value> buildPayload(Document doc) {
        Map<String, io.qdrant.client.grpc.JsonWithInt.Value> payload = new HashMap<>();

        payload.put("content", ValueFactory.value(doc.getText()));

        for (Map.Entry<String, Object> entry : doc.getMetadata().entrySet()) {
            if (entry.getValue() instanceof String str) {
                payload.put(entry.getKey(), ValueFactory.value(str));
            } else if (entry.getValue() instanceof Integer num) {
                payload.put(entry.getKey(), ValueFactory.value(num));
            } else if (entry.getValue() instanceof Long num) {
                payload.put(entry.getKey(), ValueFactory.value(num));
            } else if (entry.getValue() instanceof Float num) {
                payload.put(entry.getKey(), ValueFactory.value(num));
            } else if (entry.getValue() instanceof Double num) {
                payload.put(entry.getKey(), ValueFactory.value(num));
            } else if (entry.getValue() instanceof Boolean bool) {
                payload.put(entry.getKey(), ValueFactory.value(bool));
            }
        }
        return payload;
    }
}