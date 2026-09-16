# ToLink vNext Java Backend

## Overview

ToLink vNext is a clean, modular-monolith foundation built from the official Alibaba COLA Light 5.0.0 Archetype. This iteration contains infrastructure only; identity, FAR, membership, persona, recommendation, workflow, and agent business are intentionally absent.

## Architecture

For the current implementation map, runtime structure, security boundaries, and test coverage, see [架构总览](ARCHITECTURE.md).

The project uses one Maven module with four package layers:

- `adapter`: inbound transport and boundary configuration.
- `application`: use-case orchestration.
- `domain`: business model, currently empty by design.
- `infrastructure`: PostgreSQL, Redis, AgentScope, and other technical integrations.

The domain layer must not depend on infrastructure, adapter, Redis, MyBatis-Plus, OSS, or AgentScope.

## Requirements

Only Git, Docker, and Docker Compose v2 are required for the container workflow. Java and Maven are supplied by the Maven Wrapper and Docker build stage.

The pinned baseline is Java 21, Spring Boot 3.5.16, PostgreSQL 18.6, Redis 8.2 Extended, MyBatis-Plus 3.5.17, AgentScope Core 2.0.3, springdoc 2.8.17, and Maven 3.9.16.

OSS SDK integration is intentionally deferred because the official OSS Java SDK V2 line is not yet treated as stable GA in this baseline.

## Quick Start

```bash
cp .env.example .env
docker compose up --build -d
docker compose ps
```

Backend health endpoints:

```bash
curl http://127.0.0.1:8080/actuator/health/liveness
curl http://127.0.0.1:8080/actuator/health/readiness
```

The development profile exposes Swagger UI at `/swagger-ui/index.html`; there are no business endpoints yet.

## Docker

```bash
docker compose build
docker compose up -d
docker compose logs -f backend
docker compose down
```

PostgreSQL and Redis are reachable only from the Compose network. The PostgreSQL data volume is retained by `docker compose down` and removed only by an explicit `docker compose down -v`.

## Configuration

Copy `.env.example` to `.env` for local-only values. `.env` is ignored by Git. AgentScope and OSS are disabled by default and no API keys or cloud credentials are committed.

Production should set `SPRING_PROFILES_ACTIVE=prod`, provide credentials through the runtime secret mechanism, and keep the default deny-all security boundary until Identity is implemented.

## Database and Migration

Flyway is the only schema migration entry point: `src/main/resources/db/migration`. The initial migration is technical only and creates no business table. PostgreSQL is the system of record; Redis is an ephemeral cache and short-lived state store.

Spring Boot 3.5.16 supplies Flyway 11.7.2, but this project explicitly overrides that property to Flyway 11.20.3, the latest Flyway 11 GA, for PostgreSQL 18.6 validation. The project does not use Flyway 12 or 13.

## Testing

Fast unit and architecture checks:

```bash
./mvnw verify
```

Full PostgreSQL/Redis integration tests run in isolated Compose services and use no H2 or Testcontainers:

```bash
docker compose \
  -f compose.yaml \
  -f compose.integration.yaml \
  -p tolink-integration \
  up --build --abort-on-container-exit --exit-code-from backend
docker compose -f compose.yaml -f compose.integration.yaml -p tolink-integration down -v
```

The integration suite verifies Flyway, MyBatis-Plus CRUD, PostgreSQL JSONB and transactions, Redis SET/GET/TTL/delete, application startup, and disabled AgentScope behavior.

## Operations

Only Actuator health is exposed. Liveness does not include PostgreSQL or Redis; readiness includes PostgreSQL and intentionally excludes Redis because Redis is not a source of business truth.

Create a PostgreSQL custom-format backup outside the Docker volume:

```bash
mkdir -p backups
docker compose exec -T postgres pg_dump -U tolink -d tolink -Fc > backups/tolink.dump
```

Restore a backup:

```bash
cat backups/tolink.dump | docker compose exec -T postgres pg_restore -U tolink -d tolink --clean --if-exists
```

`Docker Volume persistence != backup`.
