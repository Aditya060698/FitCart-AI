# FitCart AI API

Spring Boot backend for FitCart AI.

## Current starter scope

- Spring Web
- Spring Data JPA
- PostgreSQL integration
- Validation
- Lombok
- Spring Boot Actuator
- OpenAPI / Swagger UI
- Redis placeholder configuration
- Clean layered sample modules for products, brands, categories, and reviews
- Global exception handling
- Paginated product listing with filter skeleton

## Package philosophy

The backend is organized by feature, with layered internals inside each feature. That keeps business domains visible while preserving standard Spring separation:

- `controller`: HTTP entry points
- `service`: use-case and orchestration logic
- `repository`: persistence abstraction
- `domain/entity`: JPA entities
- `dto`: request/response contracts

## Starter endpoints

- `GET /api/v1/health`
- `GET /api/v1/products`
- `GET /api/v1/products/{id}`
- `POST /api/v1/products`
- `GET /api/v1/categories`
- `POST /api/v1/categories`
- `GET /api/v1/categories/{id}`
- `GET /api/v1/brands`
- `POST /api/v1/brands`
- `GET /api/v1/brands/{id}`
- `GET /api/v1/reviews/products/{productId}`
- `POST /api/v1/reviews`
- `GET /actuator/health`
- `GET /swagger-ui.html`
