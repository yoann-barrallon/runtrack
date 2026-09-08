# Copilot instructions for Runtrack

## Project overview

Runtrack is a Java 21 Spring Boot application built with the Maven wrapper. The
application entry point is
`src/main/java/fr/yoannbarrallon/runtrack/RuntrackApplication.java`.
The codebase is currently a starter application; domain, web, persistence, and
service layers have not yet been added, so preserve the existing package root
`fr.yoannbarrallon.runtrack` when introducing them.

The runtime is configured around:

- Spring MVC for HTTP endpoints.
- Spring Data JPA with PostgreSQL for relational persistence.
- Flyway for database migrations.
- Redis as the Spring Cache backend and as a Spring Data Redis dependency.
- Spring Boot Docker Compose support for local infrastructure discovery.
- Springdoc OpenAPI UI at `/swagger-ui.html`.

`compose.yaml` provides the local PostgreSQL 16 and Redis 7 services. The
application uses `spring.jpa.hibernate.ddl-auto: validate`, so entity mappings
must match an existing database schema; schema changes should be represented by
Flyway migrations rather than Hibernate DDL generation. Keep
`spring.jpa.open-in-view: false` in mind when designing web-to-persistence
flows.

## Build, test, and run

Use the Maven wrapper so the project uses its pinned Maven configuration:

```sh
./mvnw test
./mvnw -Dtest=RuntrackApplicationTests test
./mvnw package
./mvnw spring-boot:run
```

The single-test form accepts a test class name or a method selector, for
example:

```sh
./mvnw -Dtest=RuntrackApplicationTests#contextLoads test
```

There is no lint or formatter plugin configured in `pom.xml`; do not assume a
separate lint command exists. The current test suite contains a Spring context
smoke test, so tests that start the application may require the PostgreSQL and
Redis services from `compose.yaml`.

## Configuration and implementation conventions

- Put application configuration in `src/main/resources/application.yaml`.
- Keep Java packages below `fr.yoannbarrallon.runtrack`.
- Use constructor-based dependency injection and Spring's existing repository,
  validation, caching, and transaction facilities rather than introducing
  parallel mechanisms.
- Add Flyway SQL migrations under
  `src/main/resources/db/migration/` when persistent schema changes are needed.
- Treat PostgreSQL and Redis as local runtime dependencies; start them with
  `docker compose up -d` when Docker Compose auto-start is not available.
- Preserve the configured Redis cache type and the disabled Open Session in
  View setting unless a deliberate architectural change requires otherwise.
- The Maven compiler is configured to process Lombok annotations for both main
  and test sources. If Lombok is used, keep it limited to the existing Maven
  setup rather than adding another annotation-processing configuration.

## API documentation

Springdoc serves the Swagger UI at `/swagger-ui.html`. New MVC controllers
should follow the existing Spring Boot application configuration and expose
their API contract through the generated OpenAPI documentation.
