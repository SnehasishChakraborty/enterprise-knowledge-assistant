package com.rag.enterprise.app.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RAGGenerationService {

    private final KnowledgeRetrievalService retrievalService;
    private final ChatModel chatModel;
    private final QueryRewriterService queryRewriterService;
    private final CohereRerankerService rerankerService;

    // Spring AI automatically autoconfigures and injects your OpenAI ChatModel bean here
    public RAGGenerationService(KnowledgeRetrievalService retrievalService, ChatModel chatModel, QueryRewriterService queryRewriterService, CohereRerankerService rerankerService) {
        this.retrievalService = retrievalService;
        this.chatModel = chatModel;
        this.queryRewriterService = queryRewriterService;
        this.rerankerService = rerankerService;
    }

    public String generateAnswer(String userQuery) {

        // 1. Intercept the raw query and rewrite it for optimal vector matching!
        String optimizedSearchQuery = queryRewriterService.rewriteQuery(userQuery);

        // Optional: Print to console so you can see the magic happening in your logs
        System.out.println("Original Query: " + userQuery);
        System.out.println("Rewritten Search Query: " + optimizedSearchQuery);

        // 2. Pass the optimized string to our new Hybrid Retrieval Engine
        // (This now returns a clean List<String> instead of Spring AI Documents)
        List<String> rawChunks = retrievalService.retrieveChunks(optimizedSearchQuery);

        // 3. 🧠 SPRINT 7 ADDITION: Cross-Encoder Reranking
        // Filter those 10 chunks down to the 3 most contextually perfect chunks
        List<String> perfectChunks = rerankerService.rerankContext(userQuery, rawChunks);

        // 4. Flatten the retrieved strings into a single continuous context block
        String structuralContext = String.join("\n\n---\n\n", perfectChunks);

        // 5. Define an enterprise-grade prompt layout forcing grounding guardrails
        String promptInstructions = """
                You are a secure Enterprise Knowledge Assistant. 
                Answer the user's question accurately using ONLY the provided supporting documentation context below.
                If the answer cannot be found or reasonably inferred from the context, state clearly that the information is not available in the company repository. 
                Do not use outside knowledge or hallucinate missing facts.
                
                SUPPORTING CONTEXT:
                {context}
                
                USER QUESTION:
                {question}
                
                GENERATE CONTEXT-GROUNDED ANSWER:
                """;

        // 6. Inject variables cleanly using Spring AI's native PromptTemplate engine
        PromptTemplate template = new PromptTemplate(promptInstructions);
        Prompt compiledPrompt = template.create(Map.of(
                "context", structuralContext.isBlank() ? "No corporate context found for this query." : structuralContext,
                "question", userQuery
        ));

        // 7. Fire the call to OpenAI and return the clean generative response block
        return chatModel.call(compiledPrompt).getResult().getOutput().getText();
    }
}