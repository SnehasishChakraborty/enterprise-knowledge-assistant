# Enterprise Knowledge Assistant (Modern RAG 2.0)

**Developer:** Snehasish Chakraborty
**Goal:** Build a production-grade AI application specializing in AI-powered backend systems using Java and Spring Boot.

---

## 📍 Current State tracker
**Active Sprint:** Sprint 3 - Embeddings & Vector Storage
**Current Task:** Configuring Spring AI EmbeddingModel, mapping DocumentChunks to Qdrant points, and establishing the database storage collection.

---

## Architecture Evolution
**Phase 1 (Target): Naive RAG**
User Query -> Embedding -> Vector Search -> Top-K Chunks -> LLM Prompt -> Answer

**Phase 2 (Target): Modern RAG 2.0**
User Query -> Query Rewriting -> Hybrid Retrieval -> Reranking -> Context Compression -> LLM Generation -> Answer Validation -> Response with Citations

## Technology Stack
* **Language:** Java 25
* **Framework:** Spring Boot 4.1.0
* **AI Framework:** Spring AI
* **Tokenization Engine:** JTokkit (CL100K_BASE / Tiktoken)
* **Vector Database:** Qdrant
* **Cache:** Redis
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

### Sprint 3: Embeddings & Vector Storage 1.0 ⬜ [IN PROGRESS]
- [ ] Inject Spring AI `EmbeddingModel` into service layer
- [ ] Map `DocumentChunk` objects into vector embeddings
- [ ] Connect ingestion output to Qdrant payload collections
- [ ] Verify contextual retrieval manually or via integration tests

### Sprint 4: Basic RAG Pipeline ⬜ [PLANNED]
- [ ] Build Ask endpoint
- [ ] Generate query embeddings
- [ ] Retrieve Top-K chunks
- [ ] Construct prompts
- [ ] Generate grounded responses

### Sprint 5: Query Rewriting ⬜ [PLANNED]
- [ ] Introduce query rewriting (Expand ambiguous questions)
- [ ] Generate optimized search queries

### Sprint 6: Hybrid Retrieval ⬜ [PLANNED]
- [ ] Implement vector search
- [ ] Implement keyword search
- [ ] Merge retrieval results & tune weights

### Sprint 7: Reranking ⬜ [PLANNED]
- [ ] Evaluate reranker options
- [ ] Integrate reranking layer
- [ ] Reorder retrieved results

### Sprint 8: Context Compression ⬜ [PLANNED]
- [ ] Summarize retrieved context
- [ ] Remove irrelevant sections / optimize token usage

### Sprint 9: Grounding & Citations ⬜ [PLANNED]
- [ ] Force grounded prompting
- [ ] Generate source references
- [ ] Return citation metadata in JSON response

### Sprint 10: Answer Validation ⬜ [PLANNED]
- [ ] Implement self-check mechanism
- [ ] Compare answers against context
- [ ] Reject unsupported claims (Mitigate hallucinations)

### Sprint 11: Security ⬜ [PLANNED]
- [ ] Configure Spring Security
- [ ] Implement JWT authentication
- [ ] Define user roles (Admin vs. User)

### Sprint 12: Redis Integration ⬜ [PLANNED]
- [ ] Cache repeated responses
- [ ] Define cache invalidation rules
- [ ] Track cache hit ratios

### Sprint 13: Audit Logging ⬜ [PLANNED]
- [ ] Track user activity & log retrieval results
- [ ] Capture response latency & token usage

### Sprint 14: Production Readiness & Containerization ⬜ [PLANNED]
- [ ] Dockerize application (Write multi-stage Dockerfile)
- [ ] Docker Compose setup for local multi-service testing
- [ ] Health checks & Spring Boot Actuator integration
- [ ] Externalized environment profiles (`application-prod.properties`)

### Sprint 15: Angular UI Integration ⬜ [PLANNED]
- [ ] Bootstrap Angular application (with standalone components)
- [ ] Build drag-and-drop document upload interface
- [ ] Implement streaming chat component using Server-Sent Events (SSE)
- [ ] Add an "Informed Source Inspector" side-panel to view cited Qdrant chunks

### Sprint 16: Cloud Deployment & CI/CD ⬜ [PLANNED]
- [ ] Provision managed cluster on Qdrant Cloud
- [ ] Set up live PostgreSQL and Redis instances on Render/Railway
- [ ] Configure automatic GitHub Deployment webhooks
- [ ] Secure production API keys/secrets using platform environment variables
