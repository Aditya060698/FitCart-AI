# FitCart AI System Overview

## 1. What Kind Of System Is This?

FitCart AI is an AI-assisted commerce platform in the nutrition and supplement domain.

That means it is not just:

- an ecommerce catalog
- a chatbot
- a recommendation engine
- a document summarizer

It is a combination of all of them, with a design that keeps the product grounded in structured product data.

The most important architectural idea is this:

> the catalog is the source of truth, and AI is a supporting intelligence layer on top of it

That distinction matters because many weak AI product designs collapse everything into one prompt. FitCart AI does not do that.

## 2. High-Level Component View

```text
Browser / React Frontend
        |
        v
Spring Boot API
        | \
        |  \---- Redis
        | 
        +------ PostgreSQL
        |
        +------ FastAPI AI Service
                    |
                    +---- Vector Store (pgvector / Chroma style)
```

### Frontend

The React frontend owns:

- page structure
- route state
- comparison state
- request / response rendering
- error and loading states
- graceful degradation messaging

It should not own recommendation logic, ranking, or persistence decisions.

### Spring Boot API

The Spring Boot backend is the application backbone.

It owns:

- product catalog APIs
- filtering and pagination
- comparison data retrieval
- review analytics aggregation
- personalization state
- ranking logic
- advisor orchestration
- document metadata and upload lifecycle
- caching for backend read paths
- resilience and fallback behavior

This is where most product logic belongs because it is:

- closer to the database
- easier to version and govern
- easier to test as business logic

### FastAPI AI Service

The FastAPI service owns AI-specialized operations:

- nutrient explanations
- review summarization
- semantic retrieval
- vector ingestion
- document explanation logic

This split is useful because AI workflows evolve differently from traditional backend code. Prompting, schema validation, retrieval logic, and vector operations fit naturally in a Python service.

### PostgreSQL

PostgreSQL stores the structured source of truth:

- products
- brands
- categories
- reviews
- goals
- dietary flags
- user preferences
- saved products
- search history
- uploaded document metadata

### Redis

Redis is used for:

- response caching
- low-latency repeated reads
- AI and backend cache acceleration

### Vector Store

The vector layer stores embeddings for:

- product descriptions
- ingredient explanations
- review summaries
- educational notes

This enables semantic search and retrieval-augmented AI flows.

## 3. Why The Service Boundaries Matter

The most important service-boundary rule in this project is:

- Spring Boot owns business workflows
- FastAPI owns AI-specific computation

Why Spring Boot should own these responsibilities:

- product truth lives in PostgreSQL
- ranking mixes structured business signals with AI-style signals
- personalization depends on user data and product data
- orchestration across retrieval, ranking, and review intelligence is business logic
- resilience and fallback behavior should be controlled centrally

Why FastAPI should own these responsibilities:

- prompt logic changes faster than entity logic
- schema-constrained explanation and summarization fit Python well
- vector ingestion and retrieval pipelines are easier to iterate on in the AI service
- AI code often has different dependencies and release cadence

This separation makes the project easier to reason about and more realistic in interviews.

## 4. Core User Flows

### Catalog Browse

1. User opens listing page
2. React calls Spring Boot
3. Spring Boot applies filters, sorting, and pagination against PostgreSQL
4. Results return as structured product summaries

This flow does not depend on AI. That is intentional.

### Product Comparison

1. User selects 2 to 4 products
2. React fetches structured product detail records
3. The comparison table renders exact fields side by side

This should stay available even if the AI layer fails.

### Advisor Query

1. User enters a goal and budget-oriented query
2. Spring Boot parses or normalizes the intent
3. Candidate products are retrieved
4. Ranking combines:
   - semantic match
   - budget fit
   - nutrition signals
   - review sentiment
   - personalization
5. Review intelligence is attached
6. A grounded answer is returned
7. If the higher-level recommendation path degrades, the backend falls back to structured catalog results

This is one of the strongest architectural flows in the project because it shows orchestration, ranking, resilience, and explainability together.

### Nutrient Explanation

1. Spring Boot or another caller assembles structured nutrient metadata
2. FastAPI receives the grounded payload
3. Prompt logic generates an educational response using only supplied metadata
4. The response is validated and cached

### Document Understanding

1. User uploads a PDF or image
2. Spring Boot stores file metadata and processing state
3. Text extraction / analysis can happen asynchronously
4. FastAPI turns extracted text into:
   - simplified summary
   - important term explanations
   - nutrition / supplement relevance observations
5. Output remains educational, not diagnostic

## 5. Ranking As A System Design Topic

Ranking is where this project becomes stronger than a CRUD backend.

FitCart AI ranking uses multiple signals:

- semantic match
- budget fit
- protein quality
- sugar penalty
- rating
- review sentiment
- popularity
- user preference match

This is important because real recommendation systems rarely depend on one signal. They combine multiple imperfect signals into an explainable score.

From a design perspective, ranking belongs in Spring Boot because:

- it combines structured product data
- it depends on business-owned constraints
- it should be testable and explainable
- it should not disappear into a black-box prompt

## 6. Where DSA Shows Up

This project uses DSA concepts in practical ways.

### Trie

Used for autocomplete across:

- product names
- brands
- goals
- ingredients

The key lesson:
tries are useful when prefix search is a first-class product behavior.

### Heap / Priority Queue

Used for top-k ranking selection.

The key lesson:
when you only need the best `k` items, a heap is more efficient and more interview-worthy than sorting everything blindly.

### Vector Similarity

Used for semantic retrieval.

The key lesson:
some information should be embedded for semantic meaning, while other information should remain structured and exact.

## 7. Failure Handling And Resilience

AI systems fail differently from normal APIs:

- they can time out
- they can degrade in quality
- they can return partial value
- they often sit behind another service boundary

FitCart AI now applies this resilience principle:

> core commerce must survive AI degradation

Examples:

- product search still works without AI
- comparison still works without AI
- advisor can fall back to structured catalog matches
- the frontend shows explicit degraded-state messaging instead of a blank failure

This is a strong system-design choice because it makes the product robust and more realistic.

## 8. Caching Strategy

Caching is used where repeated reads are expensive or common:

- top products for common advisor queries
- autocomplete results
- nutrient explanations
- review summaries

Important architectural rule:

- cache pure reads
- do not cache flows in a way that skips important side effects

That is why advisor caching is separated from search-history recording.

## 9. Why This Project Is Interview-Ready

This project is strong for interviews because it lets you discuss:

- monorepo organization
- service boundaries
- Spring Boot layered architecture
- React query-driven frontend design
- AI service decomposition
- ranking systems
- semantic retrieval
- caching
- resilience and graceful degradation
- document workflows
- personalization

A weaker project would stop at “I used an LLM.” A stronger project explains how the entire system is organized around reliable product behavior.
