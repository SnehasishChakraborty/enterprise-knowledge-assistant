package com.rag.enterprise.app;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class VectorStoreIT {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    // Enforced constructor injection
    @Autowired
    public VectorStoreIT(VectorStore vectorStore, EmbeddingModel embeddingModel) {
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
    }

    @Test
    void verifyEmbeddingAndVectorStoreConnection() {
        assertThat(embeddingModel).isNotNull();

        Document document = new Document(
                "Spring AI makes enterprise RAG development incredibly seamless.",
                Map.of("category", "test", "author", "Snehasish")
        );

        vectorStore.add(List.of(document));

        List<Document> results = vectorStore.similaritySearch("RAG development");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getText()).contains("Spring AI");
        assertThat(results.get(0).getMetadata().get("author")).isEqualTo("Snehasish");

        System.out.println("🚀 Sprint 1 Verification Successful! Retrieved content: " + results.get(0).getText());
    }
}