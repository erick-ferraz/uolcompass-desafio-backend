# Project Overview

Spring Boot 4.1.0 + Java 25 + MySQL 8.0 + RabbitMQ + Redis + Flyway + MapStruct.

## Architecture

Clean Architecture adopted. Package structure under `br.com.uolcompass`:

### Layers

| Layer | Package | Responsibility |
|---|---|---|
| **Domain** | `core.domain` | Pure domain objects (POJOs with Lombok, no framework annotations) |
| **Use Cases** | `core.usecase` (interface), `core.usecase.impl` (implementation) | Business logic, validation rules |
| **Gateways** | `core.gateway` (interface) | Contracts for data access / external services |
| **Data Provider** | `dataprovider.*` | JPA entities, repositories, gateway impls, messaging, cache, Flyway migrations |
| **Entrypoints** | `entrypoints.*` | REST controllers, DTOs, mappers, exception handlers, RabbitMQ config |

### Flow for a request

```
HTTP Request
  → Controller (entrypoints.controller)
  → DTO → Entrypoint Mapper (entrypoints.mapper) → Domain
  → Use Case (core.usecase.impl)
  → Gateway Interface (core.gateway)
  → Gateway Impl (dataprovider.database.gateway)
  → Repository (dataprovider.repository)
  → Entity Mapper (dataprovider.database.mapper) ↔ Domain
  → Response DTO → HTTP Response
```

### Saga / Messaging Flow for Transferences

```
POST /api/v1/transferences
  → Use Case saves Transference (status=PENDING)
  → OutboxTransferenceEventGateway saves event to outbox_events table
  → OutboxScheduler (@Scheduled fixedDelay=2000) polls & publishes to RabbitMQ
  → TransferenceSagaConsumer (DEBIT from payer, status=DEBITED)
  → TransferenceCreditConsumer (CREDIT to payee, status=COMPLETED)
  → NotificationConsumer (external API call)
  → On failure: CompensationConsumer (reverses debit, status=COMPENSATED)
```

Each consumer checks Redis-backed idempotency before processing.

---

## Build

```bash
./mvnw clean install         # full build
./mvnw spring-boot:run       # run application
./mvnw clean test            # run tests only
```

## Run Dependencies

```bash
docker compose -f docker/docker-compose.yml up -d
```

Environment variables are loaded from `.env` at the project root.

Services: MySQL 8.0, Redis 7-alpine, RabbitMQ 3.12 (management UI on port 15672).

## Application Properties

Located at `src/main/resources/application.properties`. Key settings:

- `spring.jpa.hibernate.ddl-auto=none` — schema managed by Flyway only
- `spring.flyway.enabled=true`
- `spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`
- `spring.jackson.time-zone=UTC`

---

## Coding Conventions

### General Java

- Use `var` for local variable type inference where the type is obvious from the right-hand side.
- Never write comments unless strictly necessary (Javadoc on public APIs, complex algorithm explanation).
- Package names: lowercase, no underscores (`core.usecase.impl`, `dataprovider.messaging.dto`).

### Lombok

- `@Getter` / `@Setter` on entity classes — never write manual getters/setters.
- `@RequiredArgsConstructor` for constructor injection — never write explicit constructors for dependencies.
- `@NoArgsConstructor` / `@AllArgsConstructor` on entities and DTOs where needed.
- Domain classes use `@AllArgsConstructor` with mix of `final` (immutable) and non-final (mutable) fields.

### Domain Classes (`core.domain`)

- Plain POJOs with Lombok annotations only.
- No JPA, Jackson, or Spring annotations.
- Use `Long id` (nullable for new entities), primitive wrappers for other fields.

### Enums (`core.enums`)

- Simple enums with constants in UPPER_SNAKE_CASE.
- Business enums: `TransferenceStatus` (PENDING, DEBITED, COMPLETED, FAILED, COMPENSATED), `WalletType` (INDIVIDUAL, BUSINESS).
- Infrastructure enums (like `TransferenceQueue` inside config classes) can have fields and constructors.

### JPA Entities (`dataprovider.database.entity`)

- `BaseEntity` is a `@MappedSuperclass` with `@Id`, `@GeneratedValue(strategy = IDENTITY)`, `createdAt`, `updatedAt`, and `@PrePersist`/`@PreUpdate` lifecycle callbacks (UTC-3 timezone).
- All regular entities extend `BaseEntity` (except `OutboxEventEntity` which is standalone with its own `@Id` and `createdAt`).
- Use `@Table(name = "tb_<plural>")` naming convention (e.g., `tb_wallets`, `tb_transferences`).
- Enum fields use `@Enumerated(EnumType.STRING)`.
- Relationship fields use `@ManyToOne` with `@JoinColumn(name = "fk_id", nullable = false)`.
- Use `@Version` for optimistic locking when needed.
- Always use Jakarta Persistence (`jakarta.persistence.*`), never `javax.persistence`.

### Repositories (`dataprovider.repository`)

- All extend `JpaRepository<Entity, Long>`.
- Only `@Repository` annotation when needed (Spring Data auto-detects).
- Derived query methods for simple lookups (e.g., `existsByCpfCnpj`, `findByPublishedFalseOrderByCreatedAtAsc`).
- Return `Optional<Entity>` for single-entity lookups.

### Gateway Interfaces (`core.gateway`)

- Pure Java interfaces with no framework annotations.
- Methods return domain objects or `Optional<Domain>`.
- One gateway per aggregate.

### Gateway Implementations (`dataprovider.database.gateway`)

- Annotated `@Component`, use `@RequiredArgsConstructor`.
- Use `@Transactional` on write methods, `@Transactional(readOnly = true)` on read methods.
- Pattern: `entityMapper.toEntity(domain)` → `repository.save(entity)` → `entityMapper.toDomain(saved)`.
- For updates: `repository.findById(id).ifPresent(entity -> { entity.setField(value); repository.save(entity); })`.

### Use Case Interfaces (`core.usecase`)

- Single method `execute(...)` returning domain objects.
- No framework annotations.

### Use Case Implementations (`core.usecase.impl`)

- Annotated `@Service`, use `@RequiredArgsConstructor`.
- Depend on gateway interfaces only (dependency inversion).
- Business rule validation happens here.
- Throw custom exceptions (extending `ApplicationBaseException`) for business rule violations.

### Controllers (`entrypoints.controller`)

- Annotated `@RestController`, `@RequestMapping("/api/v1/<resource>")`, `@RequiredArgsConstructor`.
- Use `@Tag` (Swagger grouping) and `@Operation`/`@ApiResponse` per endpoint.
- Pattern: `mapper.toDomain(request)` → `useCase.execute(domain)` → `mapper.toResponse(domain)`.
- Return `ResponseEntity<ResponseDTO>` with appropriate status codes (201, 202, 200, 404).

### DTOs (`entrypoints.dto`)

- All are Java `record` types (immutable, compact constructors).
- Use `@Schema` for OpenAPI documentation (with `description` and `example`).
- Request records use `jakarta.validation.constraints` (`@NotBlank`, `@Email`, `@NotNull`, `@Positive`).
- Suffix: `*Request` for input, `*Response` for output.

### Mappers (MapStruct)

Two layers of mappers:

1. **Entrypoint Mappers** (`entrypoints.mapper`): DTO ↔ Domain
   - Annotated `@Mapper(componentModel = "spring")`.
   - Use `expression = "java(...)"` for default field values (e.g., `balance = ZERO`, `status = PENDING`).
   - Use `ignore = true` for fields not mapped from request (`id`).
   - Default methods for complex logic (e.g., `buildMessage(status, id)` with switch expression).

2. **Entity Mappers** (`dataprovider.database.mapper`): Domain ↔ JPA Entity
   - Annotated `@Mapper(componentModel = "spring")`.
   - Use `uses = ResolverClass.class` for resolving entity references (e.g., `WalletEntityResolver` maps `Long walletId` ↔ `WalletEntity` via `getReferenceById`).
   - Explicit `@Mapping(target = "entityField", source = "domainField")` when field names differ.

### Exception Handling

- Abstract `ApplicationBaseException` extends `RuntimeException`, carries `HttpStatus`.
- Concrete exceptions (e.g., `WalletNotFoundException`, `InsufficientBalanceException`) extend it.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) returns `ProblemDetail` (RFC 7807).
- Handles `ApplicationBaseException` and `MethodArgumentNotValidException`.
- All exception classes live in `core.usecase.impl` (close to where they're thrown) or in a dedicated package if many.

### Messaging / RabbitMQ

- **Outbox Pattern**: Events go to `outbox_events` table first; `OutboxScheduler` polls every 2s and publishes to RabbitMQ.
- **Exchange**: `transference.events` (Topic).
- **Queues** (each with DLQ): `transference.initiated`, `transference.debited`, `transference.completed`, `transference.failed`, `transference.compensated`, `notification.send`.
- Queue naming constant: `TransferenceRabbitMQConfig.TransferenceQueue` enum with `queueName` and `dlqName` fields.
- **JSON**: `JacksonJsonMessageConverter` set globally in `RabbitMQInfraConfig`.
- **Retry**: `StatelessRetryOperationsInterceptor` (1s initial, 2x multiplier, 5s max) + `RejectAndDontRequeueRecoverer`.
- **Consumers**: annotated `@RabbitListener(queues = "...")`, use `@Transactional`.
- **Idempotency**: Each consumer checks `IdempotencyService` (Redis) before processing.
- Event DTOs are records in `dataprovider.messaging.dto`.

### Cache / Redis (`dataprovider.cache`)

- `IdempotencyService`: stores processed message IDs in Redis with configurable TTL.
- `WalletCacheAdapter`: caching layer for wallet lookups (if applicable).

### Flyway Migrations (`src/main/resources/db/migration`)

- Naming: `V<number>__<description>.sql` (double underscore before description).
- All DDL is explicit — no `spring.jpa.hibernate.ddl-auto`.
- Table naming: `tb_<plural>` (e.g., `tb_wallets`, `tb_transferences`), except `outbox_events` (no prefix).
- Always include `created_at` / `updated_at` timestamps for entities extending `BaseEntity`.
- Use `DECIMAL(15, 2)` for monetary values.
- Foreign keys with explicit `CONSTRAINT fk_*` naming.

### Validation

- Jakarta Bean Validation annotations on request records only.
- No custom validators in the codebase.

### Testing

- Test class at `src/test/java/br/com/uolcompass/UolcompassApplicationTests.java` (default Spring Boot smoke test).
- Add tests using `@SpringBootTest` or sliced tests (`@WebMvcTest`, `@DataJpaTest`) as appropriate.

---

## Commit Pattern

```
type: Brief description in English

Types: feat, fix, chore, refactor, test, docs.
```
