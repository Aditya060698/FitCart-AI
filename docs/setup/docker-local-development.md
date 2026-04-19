# Docker Local Development

Use Docker Compose to start the full local FitCart AI stack:

- `web` on `http://localhost:5173`
- `api` on `http://localhost:8080`
- `ai` on `http://localhost:8000`
- `postgres` on `localhost:5432`
- `redis` on `localhost:6379`

## Networking model

- Containers talk to each other with service names such as `postgres`, `redis`, `api`, and `ai`.
- Your browser does not live inside the Docker network, so browser-facing URLs must use published host ports such as `localhost:8080`.

## Startup

```bash
docker compose -f infra/docker-compose.yml up --build
```

## Shutdown

```bash
docker compose -f infra/docker-compose.yml down
```

## Reset local data

```bash
docker compose -f infra/docker-compose.yml down -v
```
