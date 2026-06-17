package com.rag.enterprise.app.domain;
import java.util.Map;

/**
 * Represents a single, semantically cohesive chunk of text
 * prepared for embedding and vector storage.
 */
public record DocumentChunk(
        String id,
        String content,
        Map<String, Object> metadata
) {}
