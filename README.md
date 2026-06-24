# UOL Compass — Desafio Backend

API REST de um banco digital com suporte a carteiras **Individual** (CPF) e **Business** (CNPJ), transferências assíncronas via **SAGA Coreografada** com RabbitMQ, cache com Redis, e consistência garantida por **Pessimistic Locking** + **Outbox Pattern**.

---

## Stack Tecnológica

| Componente | Tecnologia |
|------------|-----------|
| Linguagem | Java 25 |
| Framework | Spring Boot 4.1.0 |
| Build | Maven |
| Banco de dados | MySQL 8.0 (Flyway migrations) |
| Cache | Redis 7 |
| Mensageria | RabbitMQ 3.12 |
| Testes | JUnit 5 + Mockito |
| Documentação | SpringDoc OpenAPI (Swagger UI) |
| Containerização | Docker + Docker Compose |

---

## Pré-requisitos

- Docker Desktop 4.x+ (com Docker Compose v2)
- Java 25 (JDK)
- Maven 3.9+ (ou usar `mvnw` incluso)

---

## Setup Rápido

```bash
# 1. Clone o repositório
git clone <repo-url>
cd uolcompass-desafio-backend

# 2. Suba a infraestrutura (MySQL, Redis, RabbitMQ)
docker compose up -d

# 3. Execute a aplicação
./mvnw clean spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Serviços e URLs

| Serviço | URL |
|---------|-----|
| Aplicação | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| RabbitMQ Management | `http://localhost:15672` (usuário: `admin`, senha: `admin`) |

---

## Variáveis de Ambiente

| Variável | Valor Default | Descrição |
|----------|--------------|-----------|
| `MYSQL_ROOT_PASSWORD` | `rootadmin123` | Senha do root MySQL |
| `MYSQL_USER` | `admin` | Usuário da aplicação MySQL |
| `MYSQL_PASSWORD` | `admin123` | Senha da aplicação MySQL |
| `RABBITMQ_HOST` | `localhost` | Host RabbitMQ |
| `RABBITMQ_PORT` | `5672` | Porta RabbitMQ |
| `RABBITMQ_USERNAME` | `admin` | Usuário RabbitMQ |
| `RABBITMQ_PASSWORD` | `admin` | Senha RabbitMQ |
| `REDIS_HOST` | `localhost` | Host Redis |
| `REDIS_PORT` | `6379` | Porta Redis |

---

## Fluxo de Transferência (SAGA Coreografada)

```
 POST /api/v1/transferences
         │
         ▼
 InitiateTransferenceUseCase
         │  Valida regras → cria PENDING → salva outbox
         ▼
 [RabbitMQ] transference.initiated
         │
    ┌────┴────┐
    ▼         │
 SagaConsumer │ (Pessimistic Lock no payer)
    │         ▼
    │    [RabbitMQ] transference.debited
    │         │
    ▼         ▼
 CreditConsumer → atualiza payee → COMPLETED
    │
    ▼
 [RabbitMQ] transference.completed
    │
    ▼
 NotificationConsumer → POST API externa + Redis

 Em caso de falha:
 [RabbitMQ] transference.failed → CompensationConsumer → estorno → COMPENSATED
```

---

## Execução de Testes

```bash
./mvnw clean test
```

Cobertura:
- Use cases (criação de carteira, transferência, extrato)
- Consumers da SAGA (débito, crédito, compensação)
- Cache adapter (hit, miss, invalidação, corrupção)
- Outbox gateway (persistência de evento)
- Exception handler (todas as exceções de negócio)

---

## Estrutura do Projeto

```
src/main/java/br/com/uolcompass/
├── core/                    # Domínio puro (sem frameworks)
│   ├── domain/              # WalletDomain, TransferenceDomain, NotificationDomain
│   ├── enums/               # WalletType, TransferenceStatus
│   ├── gateway/             # Interfaces de saída (WalletGateway, TransferenceGateway)
│   └── usecase/             # Casos de uso + implementações + exceções
├── dataprovider/            # Adaptadores de saída
│   ├── cache/               # Redis (WalletCacheAdapter, IdempotencyService)
│   ├── database/            # JPA entities, gateways impl, mappers MapStruct
│   ├── messaging/           # RabbitMQ consumers, eventos, outbox scheduler
│   └── repository/          # Spring Data JPA repositories
└── entrypoints/             # Adaptadores de entrada (REST)
    ├── controller/          # WalletController, TransferenceController
    ├── dto/                 # Records de request/response
    ├── handler/             # GlobalExceptionHandler (RFC 9457 ProblemDetail)
    ├── mapper/              # MapStruct DTO mappers + StatementDtoMapper
    └── config/              # Security, RabbitMQ, Redis, Swagger, DataSeeder configs
```
