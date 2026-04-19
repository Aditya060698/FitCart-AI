# Architecture Notes

This folder documents FitCart AI from a system-design perspective.

The goal of these notes is not just to list components. The goal is to explain:

- why the system is split the way it is
- which service owns what
- where AI fits and where it should not fit
- how data and requests move through the platform
- which trade-offs make this a strong SDE-2 style project

## Reading Order

If you want the fastest understanding path, read in this order:

1. [System Overview](system-overview.md)
2. [AI Pipeline Notes](ai-pipeline.md)

## What You Should Learn From These Docs

- How to split React, Spring Boot, and FastAPI into clean service boundaries
- How to think about orchestration versus computation ownership
- How to combine structured filters with semantic retrieval
- Why ranking should stay explainable
- Why resilience and fallback design matter for AI products
- How caching, personalization, and review intelligence fit into a commerce system

## Architecture Principles Used In FitCart AI

1. Structured commerce remains the backbone.
   AI should improve understanding and discovery, not replace the catalog.

2. Spring Boot owns business truth.
   Product data, persistence, ranking, and orchestration belong in the backend service layer.

3. FastAPI owns AI-specialized logic.
   Prompted explanation, summarization, retrieval, and vector workflows belong in the AI service.

4. Frontend should orchestrate user experience, not backend logic.
   The UI should display state cleanly and degrade gracefully.

5. AI features must be grounded.
   The system should prefer constrained, evidence-backed outputs over more fluent but risky ones.

6. Resume-ready systems need trade-offs, not just features.
   A good project explains why a decision was made and what was intentionally deferred.
