# FitCart AI AI Service

FastAPI-based AI service for FitCart AI.

## Current starter scope

- environment-based configuration
- health check endpoint
- nutrient explanation placeholder endpoint
- recommendation analysis placeholder endpoint
- modular route, schema, and service layout

## Design principle

This service owns AI-oriented computation and response shaping.
Spring Boot remains the product orchestrator and system of record.

## Starter endpoints

- `GET /health`
- `GET /api/v1/health`
- `POST /api/v1/nutrients/explain`
- `POST /api/v1/recommendations/analyze`
