# Architecture

## COLA Light layers

This is a single Maven module using package-based COLA Light layering:

```text
org.xaspire.tolink
├── adapter
├── application
├── domain
└── infrastructure
```

`adapter` receives external requests. `application` coordinates use cases. `domain` owns business rules and must remain technology-independent. `infrastructure` implements technical integrations and outbound boundaries.

## Dependency direction

The domain does not depend on adapter or infrastructure. MyBatis-Plus, Redis, OSS, and AgentScope stay in infrastructure. Future repositories and external services should be represented by ports owned by the appropriate inner layer and implemented in infrastructure.

## Technical responsibilities

- PostgreSQL stores durable business facts and is migrated only through Flyway.
- Redis stores cache and short-lived state; losing Redis must not lose business truth.
- OSS is reserved for object storage, but its SDK integration is deferred in this baseline.
- AgentScope is an optional infrastructure capability inside the backend JVM, not a domain model or separate service.

There are no business APIs, business tables, agent workflows, or identity rules in this foundation.
