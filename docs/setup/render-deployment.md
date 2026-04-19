# Render Deployment

This project can be deployed to Render with a single Blueprint file at the repository root: [`render.yaml`](/E:/Courses-and-trainings/Git/FitCart-AI/render.yaml).

## Deployment Goal

The current Blueprint is optimized for an all-free Render deployment:

- `fitcart-web`: public static React frontend
- `fitcart-api`: public Spring Boot API
- `fitcart-ai`: public FastAPI AI service
- `fitcart-postgres`: managed PostgreSQL
- `fitcart-redis`: managed Redis-compatible Key Value

This is not the cleanest production security posture, but it is the only way to keep every Render resource on a free tier under Render's current service rules.

## Why this shape works well on Render

Render Blueprints let one repository create multiple linked resources in one deploy. FitCart AI still benefits from that on free tiers because:

- the React app is best served as a static site
- the Spring Boot API owns public business APIs, persistence, and orchestration
- the FastAPI service stays focused on AI workflows, but it must be a public web service instead of a private service
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
- receives the public AI service URL from the FastAPI service
- uses ephemeral local storage only, which is acceptable for free-tier demos but not persistent uploads

### `fitcart-ai`

- built from `apps/ai/Dockerfile`
- runs as a public web service because private services do not support free instances
- receives the Spring Boot API public URL for internal callbacks and data fetches
- receives `REDIS_URL` for cache access
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

### Ephemeral filesystem

Free Render web services do not support persistent disks. That means:

- uploaded files stored on the API filesystem are lost on restart, redeploy, or spin-down
- the AI service's local file-backed vector store is also lost on restart, redeploy, or spin-down

For a free demo deployment, that is acceptable as long as you treat uploads and AI-ingested local files as temporary.

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

## Free-tier constraints

This all-free deployment works because Render currently allows free:

- static sites
- web services
- one Postgres database per workspace
- one Key Value instance per workspace

Important limitations from Render's current docs:

- private services do not support free instances
- free web services spin down after inactivity
- free web services do not support persistent disks
- free Postgres expires 30 days after creation
- free Key Value is in-memory only and loses data on restart

## Known production follow-ups

This Blueprint is strong for a portfolio-grade full-stack deployment, but production hardening usually continues with:

- a custom frontend domain and matching CORS origin update
- Flyway or Liquibase instead of `ddl-auto: update`
- service-to-service auth between Spring Boot and FastAPI
- external object storage for documents instead of local disk
- pgvector-backed embeddings instead of file-backed vectors
- moving the AI service back to a private service on a paid plan
