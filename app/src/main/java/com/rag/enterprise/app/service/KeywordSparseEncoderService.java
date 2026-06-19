package com.rag.enterprise.app.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeywordSparseEncoderService {

    // A standard list of "noise" words that carry zero search value.
    // We filter these out so they don't bloat our database index.
    private static final List<String> STOP_WORDS = List.of(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "with", "about", "against", "between", "into", "through", "during",
            "before", "after", "above", "below", "from", "up", "down",
            "out", "off", "over", "under", "again", "further", "then",
            "once", "here", "there", "when", "where", "why", "how", "all", "any",
            "both", "each", "few", "more", "most", "other", "some", "such", "no",
            "nor", "not", "only", "own", "same", "so", "than", "too", "very", "is",
            "are", "was", "were", "be", "been", "being", "have", "has", "had", "do",
            "does", "did", "will", "would", "shall", "should", "can", "could", "may",
            "might", "must", "ought", "i", "you", "he", "she", "it", "we", "they"
    );

    /**
     * Converts raw text into a Qdrant-compatible Sparse Vector format.
     */
    public SparseVector encode(String text) {
        if (text == null || text.isBlank()) {
            return new SparseVector(new ArrayList<>(), new ArrayList<>());
        }

        // 1. Clean the text: lowercase it, and remove punctuation
        // We KEEP hyphens and dots because of technical terms like ORA-12541 or ap_bookmark.IFD
        String cleanText = text.toLowerCase().replaceAll("[^a-z0-9\\-. ]", "");
        String[] tokens = cleanText.split("\\s+");

        // 2. Count term frequencies (How many times does each word appear?)
        Map<Integer, Float> frequencyMap = new HashMap<>();

        Arrays.stream(tokens)
                .filter(token -> !token.isBlank())
                .filter(token -> !STOP_WORDS.contains(token)) // Drop the noise words
                .forEach(token -> {
                    // Hash the string token to a positive 32-bit integer.
                    // Qdrant uses numbers for its index, not text strings!
                    // Bitwise AND forces a positive 32-bit integer safely
                    int hashedIndex = token.hashCode() & 0x7fffffff;
                    frequencyMap.put(hashedIndex, frequencyMap.getOrDefault(hashedIndex, 0f) + 1.0f);
                });

        // 3. Unpack the map into the parallel array structure required by Qdrant
        List<Integer> indices = frequencyMap.keySet().stream().toList();
        List<Float> values = indices.stream().map(frequencyMap::get).toList();

        return new SparseVector(indices, values);
    }

    /**
     * A simple Record to hold the two arrays Qdrant needs:
     * - indices: The hashed IDs of the words
     * - values: How many times that word appeared
     */
    public record SparseVector(List<Integer> indices, List<Float> values) {}
}
