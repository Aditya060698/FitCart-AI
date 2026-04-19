# FitCart AI

FitCart AI is a production-style AI commerce platform for nutrition and supplement discovery. The project is designed to feel like a real product system rather than a demo chatbot: users can browse a product catalog, apply structured filters, compare products side by side, view review intelligence, get grounded recommendation support, and upload fitness or health-related documents for educational AI-assisted interpretation.

This repository is built as a monorepo so the frontend, backend, AI service, and architecture notes evolve together.

## Problem Statement

Nutrition and supplement shopping is noisy. Users often struggle to answer practical questions such as:

- Which product fits my budget and goal?
- What do these ingredients or nutrients actually mean?
- How do I compare similar products quickly?
- Are reviews positive for the right reasons, or just generic praise?
- Can a health or fitness document help me narrow relevant product categories without turning the app into a diagnostic tool?

FitCart AI addresses that gap by combining structured commerce flows with AI-assisted explanation, summarization, semantic retrieval, and recommendation orchestration.

## Core Features

- Product catalog with filters, search, sorting, and pagination
- Product detail pages with spec tables, ingredient highlights, and review snapshots
- Comparison workflow for 2 to 4 products
- AI advisor for goal and budget-aware recommendations
- Review aggregation and AI-ready review summarization
- Nutrient and supplement explanation flows
- Semantic retrieval / RAG foundation for product and educational content
- Document upload pipeline with metadata storage and safe AI-assisted explanation boundaries
- Lightweight personalization using saved products, search history, and user preferences
- Redis-backed caching for common read paths
- Resilience and graceful degradation for AI-adjacent flows

## Architecture

High-level system shape:

```text
React Frontend
    |
    v
Spring Boot API
    |-- PostgreSQL
    |-- Redis
    |-- Business logic, orchestration, ranking, personalization
    |
    +--> FastAPI AI Service
            |-- Prompted explanation and summarization
            |-- Vector ingestion and semantic search
            |-- Redis-backed response caching
            +-- pgvector / Chroma-style vector layer
```

Service responsibilities:

- `apps/web`: React UI, routing, TanStack Query, API integration, compare flow, advisor UI
- `apps/api`: Spring Boot backend, catalog APIs, filtering, ranking, caching, orchestration, document metadata, personalization
- `apps/ai`: FastAPI AI service, nutrient explanation, review summarization, semantic retrieval, vector ingestion
- `docs`: architecture, learning notes, and project reasoning

Detailed notes:

- [Architecture Index](docs/architecture/README.md)
- [System Overview](docs/architecture/system-overview.md)
- [AI Pipeline Notes](docs/architecture/ai-pipeline.md)
- [Setup Docs](docs/setup/README.md)

## Tech Stack

Frontend:

- React
- React Router
- Tailwind CSS
- Axios
- TanStack Query

Backend:

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Redis
- Spring validation
- Actuator
- OpenAPI / Swagger

AI Service:

- Python 3.11+
- FastAPI
- Pydantic
- Uvicorn

AI / Retrieval:

- Prompt-based grounded summarization and explanation
- Vector ingestion pipeline
- Semantic retrieval / RAG-lite patterns
- pgvector or Chroma-compatible design

Infra / Local Dev:

- Docker / Docker Compose

## AI Pipeline

FitCart AI is intentionally designed so AI is a subsystem, not the entire product.

Representative recommendation flow:

1. User enters a query such as `Best whey under 2500 for muscle gain with low sugar`
2. Spring Boot parses or normalizes the request context
3. Candidate products are retrieved using structured search and semantic signals
4. Ranking logic scores candidates using:
   - semantic match
   - budget fit
   - protein quality
   - sugar penalty
   - rating
   - review sentiment
   - popularity
   - personalization signals
5. Review analytics are attached
6. A grounded answer is generated
7. If the AI-style recommendation path degrades, the system falls back to catalog-backed structured matches

Representative explanation / summarization flow:

1. Backend gathers structured facts
2. AI service receives a constrained payload
3. Prompt logic is grounded only in supplied evidence
4. Response is validated against the schema
5. Response is cached where appropriate

This architecture keeps the system explainable and safer than a thin “LLM wrapper.”

## DSA Elements

This project intentionally includes DSA concepts that are relevant to production engineering:

- Trie-based autocomplete for product names, brands, ingredients, and goals
- Heap / priority queue for top-k ranking selection
- Weighted scoring and normalization for ranking
- Vector similarity retrieval for semantic search
- Hash-based cache keying for deterministic caching
- Pagination and filtering patterns for scalable catalog browse flows

These choices are not academic add-ons. They are used where they map naturally to system requirements.

## Repository Layout

```text
apps/
  web/   React frontend
  api/   Spring Boot backend
  ai/    FastAPI AI service
docs/
  architecture/
  learning/
  product/
  setup/
infra/
```

## Getting Started

Suggested local startup order:

1. Start infrastructure dependencies: PostgreSQL and Redis
2. Start the Spring Boot API
3. Start the FastAPI AI service
4. Start the React frontend

Useful docs:

- [Local Development](docs/setup/local-development.md)
- [Docker Local Development](docs/setup/docker-local-development.md)

Typical app ports:

- Frontend: `5173` or local Vite default
- Spring Boot API: `8080`
- FastAPI AI service: `8000`
- PostgreSQL: `5432`
- Redis: `6379`

## Running Tests

Backend tests:

```bash
cd apps/api
mvn test
```

AI service validation:

```bash
cd apps/ai
python -m compileall app
```

## Screenshots

Add real screenshots or GIFs here as the UI matures:

- `docs/assets/home-page.png`
- `docs/assets/catalog-page.png`
- `docs/assets/product-detail.png`
- `docs/assets/compare-page.png`
- `docs/assets/advisor-page.png`
- `docs/assets/document-upload.png`

Suggested README screenshot section format:

```text
[Home Page Screenshot Placeholder]
[Catalog Screenshot Placeholder]
[Comparison Screenshot Placeholder]
[AI Advisor Screenshot Placeholder]
```

## What Makes This Project Strong

- It combines commerce architecture with AI workflows instead of replacing one with the other.
- It separates service boundaries clearly between React, Spring Boot, and FastAPI.
- It includes ranking, caching, retrieval, resilience, and personalization rather than only CRUD screens.
- It documents trade-offs and architecture decisions in a learning-friendly way.
- It is suitable for discussing backend design, AI integration, and full-stack architecture in interviews.

## How To Present This In Interviews

Use this framing:

- FitCart AI is a production-style AI commerce system, not just a chatbot.
- The backend owns business logic, ranking, persistence, caching, and orchestration.
- The AI service owns prompt-driven explanation, summarization, and semantic retrieval.
- The frontend is built around real catalog, comparison, and recommendation user journeys.
- The system includes graceful degradation so core shopping flows still work when AI-adjacent features degrade.

A strong summary line:

> “I built FitCart AI as a modular AI-assisted commerce platform that combines traditional backend system design with grounded AI features like summarization, semantic retrieval, and explainable recommendation ranking.”

## Future Scope

- Full semantic retrieval integration into recommendation flows
- Better observability for AI latency, fallback rate, and response quality
- More sophisticated document extraction and OCR pipeline
- Auth and multi-user profiles
- Better benchmark datasets and evaluation harnesses for AI features
- Async job orchestration for heavy ingestion or document workflows
- Stronger recommendation explainability UI
- Stale-result fallback and circuit breakers for AI service resilience

## Disclaimer

FitCart AI is designed for educational and shopping decision-support use cases. It is not intended to diagnose conditions, prescribe treatment, or replace professional medical advice.
