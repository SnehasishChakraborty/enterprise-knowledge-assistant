# Enterprise Knowledge Assistant (Modern RAG 2.0)

**Developer:** Snehasish Chakraborty  
**Goal:** Build a production-grade AI application specializing in AI-powered backend systems using Java and Spring Boot.

---

## 📍 Current State Tracker
**Active Sprint:** Sprint 9 - Grounding & Citations  
**Current Task:** Implementing structured metadata reflection to return exact document filenames and page numbers within the JSON response payload.

---

## Architecture Evolution
**Phase 1 (Complete): Naive RAG** User Query -> Embedding -> Vector Search -> Top-K Chunks -> LLM Prompt -> Answer

**Phase 2 (Active): Modern RAG 2.0** User Query -> Query Rewriting -> Hybrid Retrieval -> Reranking -> Context Compression -> LLM Generation -> Answer Validation -> Response with Citations

## Technology Stack
* **Language:** Java 25
* **Framework:** Spring Boot 4.1.0
* **AI Framework:** Spring AI
* **Tokenization Engine:** JTokkit (CL100K_BASE / Tiktoken)
* **Vector Database:** Qdrant (Native Hybrid / RRF Enabled)
* **Reranking Engine:** Cohere Rerank API (`rerank-english-v3.0`)
* **Cache & Memory:** Redis
* **Security:** Spring Security + JWT
* **Database:** PostgreSQL
* **Containerization:** Docker & Docker Compose

---

## 🏃‍♂️ Sprint Tracker

### Sprint 1: Foundation Setup ✅ [COMPLETE]
- [x] Create Spring Boot project (Java 25, Maven)
- [x] Install/Configure Docker
- [x] Run Qdrant locally via `docker-compose.yml`
- [x] Configure Spring AI and OpenAI provider in `application.properties`
- [x] Verify embedding generation (Integration Test)
- [x] Verify vector store connectivity (Integration Test)

### Sprint 2: Document Ingestion Pipeline ✅ [COMPLETE]
- [x] Create PDF upload endpoint (`POST /api/v1/documents/upload`)
- [x] Parse PDFs page-by-page using Apache PDFBox to preserve source metadata
- [x] Implement structural magic-byte screening (`%PDF`) to prevent MIME-spoofing attacks
- [x] Enforce execution guardrails (15MB file-size limit, encryption checks) to prevent OOM errors
- [x] Integrate JTokkit for real model-aware BPE token counting (`CL100K_BASE`)
- [x] Design token-based sliding window chunking with an explicit 100-token overlap to prevent word-slicing
- [x] Configure centralized `@RestControllerAdvice` exception mappings for uniform REST error responses

### Sprint 3: Embeddings & Vector Storage ✅ [COMPLETE]
- [x] Provision a decoupled `VectorStorageService` adhering to Domain-Driven Design boundaries
- [x] Implement database-agnostic portability using the industry-standard Spring AI `VectorStore` abstraction
- [x] Automate runtime vector schema generation (`spring.ai.vectorstore.qdrant.initialize-schema=true`)
- [x] Connect the document ingestion pipeline to a local Qdrant collection via reactive gRPC network channels
- [x] Suppress internal non-fatal library logging anomalies (`org.apache.fontbox`) to ensure production log cleanliness
- [x] Manually verify vector payload structure and lineage fields inside the Qdrant local dashboard

### Sprint 4: Basic RAG Pipeline ✅ [COMPLETE]
- [x] Expose diagnostic endpoints utilizing a clean JSON request body payload DTO (`DiagnosticQueryRequest`)
- [x] Execute programmatic similarity searches bypassing framework abstract conversions using a calibrated similarity threshold
- [x] Map extracted database payloads directly into uniform Spring AI `Document` data objects
- [x] Inject Spring AI’s `ChatModel` to orchestrate context-isolated prompting templates
- [x] Confirm end-to-end grounded generation via Postman without model hallucination or leakage

### Sprint 5: Query Rewriting ✅ [COMPLETE]
- [x] Create a standalone `QueryRewriterService` using a specialized system prompt for orchestration preprocessing
- [x] Clean up conversational noise and conversational filler text out of incoming queries via low-token fast LLM evaluation passes
- [x] Resolve missing pronouns and vague references dynamically before passing inputs to the vector layer
- [x] Maintain precise original query text configurations when targeting the generation stage to preserve client formatting intents

### Sprint 6: Hybrid Retrieval ✅ [COMPLETE]
- [x] Implement a custom BM25 `KeywordSparseEncoderService` using 32-bit integer token hashing
- [x] Refactor `VectorStorageService` to inject dual-vectors (Dense + Sparse) natively via gRPC
- [x] Configure Qdrant database schema to accept `text-dense` and `text-sparse` named vectors
- [x] Execute Reciprocal Rank Fusion (RRF) natively on the database using `Points.FusionAlgorithm.RRF`

### Sprint 7: Reranking ✅ [COMPLETE]
- [x] Evaluate reranker options (Cross-Encoders vs. LLM-as-a-Judge)
- [x] Integrate cloud-based Cohere Rerank API utilizing Spring Boot's modern `RestClient`
- [x] Intercept top 10 Qdrant hybrid results and filter down to the top 3 contextually perfect chunks based on direct query-to-chunk scoring

### Sprint 8: Context Compression ⬜ [DEFERRED/PLANNED]
- [ ] Summarize retrieved context
- [ ] Remove irrelevant sections / optimize token usage

### Sprint 9: Grounding & Citations ⬜ [IN PROGRESS]
- [ ] Force grounded prompting configurations
- [ ] Refactor generation signatures to capture and propagate original document metadata fields
- [ ] Return citation metadata arrays explicitly in JSON responses for UI consumption

### Sprint 10: Conversational Memory & Session Management ⬜ [PLANNED]
- [ ] Implement Spring AI's `ChatMemory` abstraction (starting in-memory)
- [ ] Inject `MessageChatMemoryAdvisor` into the generation layer to maintain contextual history
- [ ] Update the `QueryRewriterService` signature to accept a Conversation ID for stateful pronoun resolution

### Sprint 11: Answer Validation ⬜ [PLANNED]
- [ ] Implement self-check mechanism
- [ ] Compare answers against context
- [ ] Reject unsupported claims (Mitigate hallucinations)

### Sprint 12: Security ⬜ [PLANNED]
- [ ] Configure Spring Security
- [ ] Implement JWT authentication
- [ ] Define user roles (Admin vs. User)

### Sprint 13: Redis Integration ⬜ [PLANNED]
- [ ] Cache repeated responses & define cache invalidation rules
- [ ] Migrate `InMemoryChatMemory` to distributed Redis session store for scalable chat history
- [ ] Track cache hit ratios

### Sprint 14: Audit Logging ⬜ [PLANNED]
- [ ] Track user activity & log retrieval results
- [ ] Capture response latency & token usage

### Sprint 15: Production Readiness & Containerization ⬜ [PLANNED]
- [ ] Dockerize application (Write multi-stage Dockerfile)
- [ ] Docker Compose setup for local multi-service testing
- [ ] Health checks & Spring Boot Actuator integration
- [ ] Externalized environment profiles (`application-prod.properties`)

### Sprint 16: Angular UI Integration ⬜ [PLANNED]
- [ ] Bootstrap Angular application (with standalone components)
- [ ] Build drag-and-drop document upload interface
- [ ] Implement streaming chat component using Server-Sent Events (SSE)
- [ ] Add an "Informed Source Inspector" side-panel to view cited Qdrant chunks

### Sprint 17: Cloud Deployment & CI/CD ⬜ [PLANNED]
- [ ] Provision managed cluster on Qdrant Cloud
- [ ] Set up live PostgreSQL and Redis instances on Render/Railway
- [ ] Configure automatic GitHub Deployment webhooks
- [ ] Secure production API keys/secrets using platform environment variables