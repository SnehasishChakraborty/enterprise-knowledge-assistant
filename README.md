# Enterprise Knowledge Assistant (Modern RAG 2.0)

**Developer:** Snehasish Chakraborty
**Goal:** Build a production-grade AI application specializing in AI-powered backend systems using Java and Spring Boot.

---

## 📍 Current State tracker
**Active Sprint:** Sprint 1 - Foundation Setup
**Current Task:** Bootstrapping the Spring Boot app, configuring Docker for Qdrant, and verifying API connections.

---

## Architecture Evolution
**Phase 1 (Target): Naive RAG**
User Query -> Embedding -> Vector Search -> Top-K Chunks -> LLM Prompt -> Answer

**Phase 2 (Target): Modern RAG 2.0**
User Query -> Query Rewriting -> Hybrid Retrieval -> Reranking -> Context Compression -> LLM Generation -> Answer Validation -> Response with Citations

## Technology Stack
* **Language:** Java 25
* **Framework:** Spring Boot 3.x
* **AI Framework:** Spring AI
* **Vector Database:** Qdrant
* **Cache:** Redis
* **Security:** Spring Security + JWT
* **Database:** PostgreSQL
* **Containerization:** Docker & Docker Compose

---

## 🏃‍♂️ Sprint Tracker

### Sprint 1: Foundation Setup 🔄 [IN PROGRESS]
- [ ] Create Spring Boot project (Java 25, Maven)
- [ ] Install/Configure Docker
- [ ] Run Qdrant locally via `docker-compose.yml`
- [ ] Configure Spring AI and OpenAI provider in `application.properties`
- [ ] Verify embedding generation (Integration Test)
- [ ] Verify vector store connectivity (Integration Test)

### Sprint 2: Document Ingestion Pipeline ⬜ [PLANNED]
- [ ] Create PDF upload endpoint
- [ ] Parse PDFs using PDFBox
- [ ] Extract raw text
- [ ] Design chunking strategy with overlap
- [ ] Add metadata extraction (Name, Date, Page, Type)

### Sprint 3: Embeddings & Vector Storage ⬜ [PLANNED]
- [ ] Generate embeddings
- [ ] Store vectors in Qdrant
- [ ] Associate metadata with vectors
- [ ] Verify retrieval manually

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

### Sprint 14: Production Readiness ⬜ [PLANNED]
- [ ] Dockerize application
- [ ] Docker Compose setup for all services
- [ ] Health checks & Actuator integration
- [ ] Externalized configuration (Environment profiles)
