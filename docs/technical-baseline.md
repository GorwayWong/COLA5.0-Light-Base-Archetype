# Technical Baseline

Verification date: 2026-09-14

| Component | Version / Decision | Support | Source / Note |
| --- | --- | --- | --- |
| COLA Light | 5.0.0 | Officially Supported | Generated from `com.alibaba.cola:cola-archetype-light:5.0.0` |
| Java | 21 LTS | Officially Supported | Build, test, and runtime target |
| Spring Boot | 3.5.16 | Officially Supported | Boot parent and dependency management |
| MyBatis-Plus | 3.5.17 | Officially Supported | `mybatis-plus-spring-boot3-starter` |
| PostgreSQL | 18.6 | Verified in Project | Compose integration test passed |
| Flyway | 11.20.3 + `flyway-database-postgresql` | Verified in Project | Latest Flyway 11 GA; overrides Boot BOM 11.7.2; PostgreSQL 18.6 validation passed without compatibility warning |
| Redis | 8.2.9 Extended image | Verified in Project | Real Redis Compose integration passed; cache-only responsibility |
| AgentScope | 2.0.3 `agentscope-core` only | Verified in Project | Boot 3.5.16 context and minimal mock-model Agent construction passed |
| OSS SDK | Deferred | Blocked | Waiting for official stable V2 GA |
| springdoc | 2.8.17 | Officially Supported | Boot 3.5.x compatibility line |
| Maven | 3.9.16 | Officially Supported | Maven Wrapper and Docker build |

## Official generation

The initial project was generated from the official Maven Central Archetype:

```bash
mvn -B archetype:generate \
  -DarchetypeGroupId=com.alibaba.cola \
  -DarchetypeArtifactId=cola-archetype-light \
  -DarchetypeVersion=5.0.0 \
  -DgroupId=org.xaspire \
  -DartifactId=tolink \
  -Dversion=0.1.0-SNAPSHOT \
  -Dpackage=org.xaspire.tolink \
  -DinteractiveMode=false
```

The generated Charge/Account examples and their old technical dependencies were removed. The reactor now contains a `shared` jar, an `agent` bounded-context jar, and an executable `bootstrap` jar. Future bounded contexts are one jar each and keep the four COLA Light package layers inside the context; `templates/domain-module` is a copyable skeleton and is not part of the reactor.

## Modular-monolith rules

- `bootstrap` owns the composition root and global runtime configuration; it assembles the enabled domain jars.
- `shared` remains framework-independent and contains only stable, domain-neutral contracts.
- A domain module owns its adapter, application, domain, and infrastructure packages.
- `agent` is the first empty domain skeleton; its AgentScope configuration and properties live in its infrastructure package.
- Domain modules do not depend on another domain's `domain` or `infrastructure` packages. Reviewed synchronous calls target only a narrow `application.api`; asynchronous collaboration uses events or caller-owned ports.
- Flyway runs once from `bootstrap`. Domain migrations live in the owning domain jar and use one repository-wide monotonic version sequence (`V1__baseline.sql`, `V2__identity_baseline.sql`, ...).

## AgentScope compatibility policy

Only `agentscope-core:2.0.3` is included. The AgentScope BOM is deliberately not imported because it manages a Spring Boot 4/Spring 7 dependency line. Compatibility is accepted only after `dependency:tree`, `help:effective-pom`, Boot context startup, and minimal `ReActAgent` construction pass under the project baseline.

## Deferred decisions

- OSS Java SDK V2 remains deferred until an official stable GA is available.
- LLM provider selection belongs to a later agent capability decision.
- Identity authentication and authorization belong to the next business phase.
- Flyway 11.20.3/PostgreSQL 18.6 passed the full integration result; no Flyway 12/13 dependency is allowed in this baseline.
