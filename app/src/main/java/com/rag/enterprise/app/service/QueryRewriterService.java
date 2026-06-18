package com.rag.enterprise.app.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class QueryRewriterService {

    private final ChatModel chatModel;

    public QueryRewriterService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Inspects a raw user query and refactors it into an optimized search string
     * that stripping away conversational noise and resolving missing context.
     */
    public String rewriteQuery(String rawUserQuery) {
        if (rawUserQuery == null || rawUserQuery.isBlank()) {
            return rawUserQuery;
        }

        // System prompt instructing the model to behave purely as an orchestration engine
        String rewriteInstructions = """
                You are an advanced AI Query Refiner for an Enterprise Search Engine.
                Your task is to take a user's conversational query and rewrite it into an optimized, independent search query designed for a vector database.
                
                CRITICAL INSTRUCTIONS:
                1. Strip out conversational filler words (e.g., "hey can you show me", "please tell me about", "i want to know").
                2. If the phrase contains vague pronouns ("it", "they", "that data", "this template"), expand them into concrete search terms based on typical technical contexts.
                3. Maintain all critical technical codes, error identifiers, and filenames exactly as written (e.g., ORA-12541, .IFD files).
                4. Output ONLY the raw rewritten search query string. Do not include any conversational preamble, notes, or quotes.
                
                RAW USER QUERY:
                {rawQuery}
                
                OPTIMIZED SEARCH QUERY:
                """;

        PromptTemplate template = new PromptTemplate(rewriteInstructions);
        Prompt compiledPrompt = template.create(Map.of("rawQuery", rawUserQuery));

        // Execute a fast, low-token pass to clean up the input text
        String rewritten = chatModel.call(compiledPrompt).getResult().getOutput().getText();

        return rewritten != null ? rewritten.trim() : rawUserQuery;
    }
}