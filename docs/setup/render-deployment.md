# Render Deployment

This project can be deployed to Render with a single Blueprint file at the repository root: [`render.yaml`](/E:/Courses-and-trainings/Git/FitCart-AI/render.yaml).

## Deployment Goal

The Render setup uses a production-style split:

- `fitcart-web`: public static React frontend
- `fitcart-api`: public Spring Boot API
- `fitcart-ai`: private FastAPI AI service
- `fitcart-postgres`: managed PostgreSQL
- `fitcart-redis`: managed Redis-compatible Key Value

This keeps the browser-facing surface small while preserving internal service-to-service networking for AI orchestration.

## Why this shape works well on Render

Render Blueprints let one repository create multiple linked resources in one deploy. FitCart AI benefits from that because:

- the React app is best served as a static site
- the Spring Boot API owns public business APIs, persistence, and orchestration
- the FastAPI service stays focused on AI workflows and can remain private
- Postgres and Redis are provisioned as managed infrastructure instead of self-hosted containers

## Resource Design

### `fitcart-web`

- built from `apps/web`
- deployed as a static site
- rewrites all routes to `index.html` so React Router works on refresh
- calls the public Spring Boot API URL

### `fitcart-api`

- built from `apps/api/Dockerfile`
- exposed publicly for frontend access
- receives database credentials from Render Postgres using discrete env vars
- receives `REDIS_URL` from Render Key Value
- receives the AI private network address from the FastAPI service
- mounts a persistent disk for uploaded document files

### `fitcart-ai`

- built from `apps/ai/Dockerfile`
- runs as a private service, not directly exposed to browsers
- receives the Spring Boot API private address for internal callbacks and data fetches
- receives `REDIS_URL` for cache access
- mounts a persistent disk for vector-store file persistence
- expects `OPENAI_API_KEY` to be provided during Blueprint creation

### `fitcart-postgres`

- managed Postgres instance
- internal networking only via `ipAllowList: []`
- referenced by the API through `host`, `port`, `database`, `user`, and `password`

### `fitcart-redis`

- managed Render Key Value instance
- internal networking only via `ipAllowList: []`
- shared by the API and AI service

## Important Config Choices

### Database configuration

Spring Boot expects a JDBC-style datasource. Instead of trying to transform Render's full Postgres URL, the Blueprint passes:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`

The API assembles those into a JDBC URL through `application.yml`.

### Redis configuration

Render Key Value exposes a single internal connection string. The API now supports `spring.data.redis.url`, which makes Render integration simpler than passing host and port separately.

### CORS

The frontend and API run on different Render domains, so the API needs explicit CORS configuration. The Spring Boot app now reads allowed origins from `FITCART_WEB_ALLOWED_ORIGINS`.

### Persistent disks

Render services are otherwise ephemeral. FitCart AI currently writes:

- uploaded document files on the API side
- file-backed vector data on the AI side

Those paths are mounted to persistent disks in the Blueprint.

## One-Click Deployment Steps

1. Push this repository to GitHub.
2. In Render, choose `New` -> `Blueprint`.
3. Connect the repository that contains `render.yaml`.
4. Review the resources Render plans to create.
5. Provide a value for `OPENAI_API_KEY` when prompted.
6. Deploy the Blueprint.

After the initial deploy:

- open the `fitcart-web` URL for the frontend
- confirm `fitcart-api` health at `/actuator/health`
- confirm `fitcart-ai` health from the Render dashboard private-service health status

## Cost and plan note

The static frontend can stay free. The private AI service cannot use Render's free web-service plan, so the current Blueprint uses `starter` for the API, AI service, and Key Value instance.

If you want the lowest-cost deployment instead of the cleaner architecture, the main compromise is to convert `fitcart-ai` from `type: pserv` to a public `type: web` service.

## Known production follow-ups

This Blueprint is strong for a portfolio-grade full-stack deployment, but production hardening usually continues with:

- a custom frontend domain and matching CORS origin update
- Flyway or Liquibase instead of `ddl-auto: update`
- service-to-service auth between Spring Boot and FastAPI
- external object storage for documents instead of local disk
- pgvector-backed embeddings instead of file-backed vectors
