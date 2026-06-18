package com.rag.enterprise.app.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RAGGenerationService {

    private final KnowledgeRetrievalService retrievalService;
    private final ChatModel chatModel;
    private final QueryRewriterService queryRewriterService;

    // Spring AI automatically autoconfigures and injects your OpenAI ChatModel bean here
    public RAGGenerationService(KnowledgeRetrievalService retrievalService, ChatModel chatModel, QueryRewriterService queryRewriterService) {
        this.retrievalService = retrievalService;
        this.chatModel = chatModel;
        this.queryRewriterService = queryRewriterService;
    }

    public String generateAnswer(String userQuery) {

        // 1. Intercept the raw query and rewrite it for optimal vector matching!
        String optimizedSearchQuery = queryRewriterService.rewriteQuery(userQuery);

        // Optional: Print to console so you can see the magic happening in your logs
        System.out.println("Original Query: " + userQuery);
        System.out.println("Rewritten Search Query: " + optimizedSearchQuery);

        // 2. Pass the optimized string to the database instead of the raw input
        List<Document> contexts = retrievalService.retrieveContext(optimizedSearchQuery);

        // 3. Flatten the retrieved chunk documents into a single continuous context block
        String structuralContext = contexts.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        // 4. Define an enterprise-grade prompt layout forcing grounding guardrails
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

        // 5. Inject variables cleanly using Spring AI's native PromptTemplate engine
        PromptTemplate template = new PromptTemplate(promptInstructions);
        Prompt compiledPrompt = template.create(Map.of(
                "context", structuralContext.isBlank() ? "No corporate context found for this query." : structuralContext,
                "question", userQuery
        ));

        // 6. Fire the call to OpenAI and return the clean generative response block
        return chatModel.call(compiledPrompt).getResult().getOutput().getText();
    }
}
