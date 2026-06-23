# Project Overview

Spring Boot 4.1.0 + Java 25 + MySQL 8.0.

## Architecture

Clean Architecture adopted. The package structure under `br.com.uolcompass`:

- **`core.domain`** — Pure domain entities, no framework annotations.
- **`core.usecase`** — Business logic / use cases.
- **`dataprovider.entity`** — JPA `@Entity` classes (infrastructure detail).
- **`dataprovider.repository`** — Spring Data JPA repositories.

## Build

```bash
./mvnw clean install   # full build
./mvnw spring-boot:run # run application
```

## Run Dependencies

```bash
docker compose -f docker/docker-compose.yml up -d
```

Environment variables are loaded from `.env` at the project root.

## Application Properties

Located at `src/main/resources/application.properties`. Current datasource config reads `MYSQL_USER` and `MYSQL_PASSWORD` from environment variables.

## Coding Conventions

- Use Lombok (`@Getter`, `@Setter`, `@RequiredArgsConstructor`, etc.) — no manual getters/setters.
- JPA entities go in `dataprovider.entity` with annotations (`@Entity`, `@Table`, `@Id`, etc.).
- Use Jakarta Persistence (`jakarta.persistence.*`), not `javax.persistence`.
- No comments in generated/implemented code unless strictly necessary (e.g., Javadoc on public APIs, complex algorithm explanation). Avoid inline noise comments.

## Commit Pattern

```
type: Brief description in English

Types: feat, fix, chore, refactor, test, docs.
```
