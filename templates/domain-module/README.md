# COLA domain module template

This directory is a copyable bounded-context skeleton. It is intentionally not listed in the root `pom.xml` reactor.

## Add a domain

1. Copy this directory to the project root as `<domain>` using a lowercase domain name such as `identity`.
2. Replace `template` with the domain name in `pom.xml`, Java package paths, package declarations, and test annotations.
3. Change the artifact ID and module name to `tolink-<domain>`.
4. Add a module entry such as `<module>identity</module>` to the root `pom.xml`, replacing `identity` with the domain name.
5. Add `tolink-<domain>` as a dependency of `../../tolink-bootstrap/pom.xml` so Spring can assemble the context.
6. Add domain-specific dependencies only to the layer that owns them. Keep the domain package framework-independent.

## Package contract

- `adapter`: HTTP controllers, message consumers, and input mapping.
- `application`: use cases, `port.in`, `port.out`, and narrow `api` facades.
- `domain`: entities, value objects, domain services, and domain events.
- `infrastructure`: repositories, MyBatis mappers/POs, gateways, and vendor adapters.

Do not depend on another domain's `domain` or `infrastructure` package. Synchronous cross-domain calls must use a reviewed `application.api`; asynchronous collaboration should use events or a caller-owned port.

## Flyway migrations

Put this domain's SQL under `src/main/resources/db/migration`. Flyway is initialized once by `bootstrap` and scans the combined application classpath. Use the repository-wide monotonic version sequence, for example `V2__identity_baseline.sql`; never restart at `V1` inside a domain.
