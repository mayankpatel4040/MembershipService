# FirstClub Membership Service

A Spring Boot service that manages membership tiers, subscription plans, and benefit entitlements for an e-commerce platform. Tier assignment is event-driven — every order processed via Kafka re-evaluates which tier a user qualifies for.

---

## Table of Contents

- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Domain Model](#domain-model)
- [API Reference](#api-reference)
- [Kafka Integration](#kafka-integration)
- [Design Patterns](#design-patterns)
- [Observability](#observability)
- [Prerequisites](#prerequisites)
- [Running Locally](#running-locally)
- [Configuration Reference](#configuration-reference)

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      REST API (8080)                     │
│   /api/users  │  /api/v1/membership  │  /api/v1/kafka   │
└───────────────────────────┬─────────────────────────────┘
                            │
            ┌───────────────▼───────────────┐
            │        Business Logic          │
            │  MembershipService             │
            │  UserService                   │
            │  OrderService (idempotent)     │
            │  TierRuleEvaluatorService      │
            └───────┬───────────────┬────────┘
                    │               │
          ┌─────────▼──┐     ┌──────▼───────────────────┐
          │ PostgreSQL  │     │     Apache Kafka           │
          │  membership │     │                           │
          │             │     │  order-topic              │
          │  Users      │     │  order-topic.DLT          │
          │  Tiers      │     │  payment-topic            │
          │  Plans      │     │  payment-topic.DLT        │
          │  Memberships│     └──────┬────────────────────┘
          │  Orders     │            │
          │  AuditLogs  │   ┌────────▼──────────────────┐
          └─────────────┘   │  Kafka Consumers           │
                            │  OrderConsumer (concurrent)│
                            │  PaymentConsumer (serial)  │
                            │  → retry x3 → DLT on fail │
                            └───────────────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.6, Java 21 |
| Database | PostgreSQL 16 via Hibernate (dialect: PostgreSQLDialect) |
| Messaging | Apache Kafka 7.5.0 (Confluent via Docker) |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| API Docs | SpringDoc OpenAPI 2.6.0 (Swagger UI) |
| Observability | Spring Actuator + Micrometer + Prometheus |
| Utilities | Lombok |

---

## Domain Model

### Tier System

Users are automatically assigned a tier based on their order history and cohort. Tiers are re-evaluated after every order event.

| Tier | Level | How Earned |
|---|---|---|
| Silver | 1 | Default on signup |
| Gold | 2 | 10+ orders **or** ₹10,000 spend this month **or** GOOD cohort |
| Platinum | 3 | 30+ orders **or** ₹50,000 spend this month |

### Membership Plans

| Plan | Duration | Price |
|---|---|---|
| Monthly | 30 days | ₹199 |
| Quarterly | 90 days | ₹499 |
| Yearly | 365 days | ₹1,499 |

### Subscription Lifecycle

```
(none) ──subscribe──► ACTIVE ──cancel──► CANCELLED
                         │
                      changePlan
                         │
                    CANCELLED + new ACTIVE
```

---

## API Reference

All request params use `snake_case`. Error responses return `{ "message": "...", "status": 4xx/5xx, "timestamp": "..." }`.

### User

| Method | Endpoint | Body | Description |
|---|---|---|---|
| `POST` | `/api/users/create` | `{ "name", "phone_number", "cohort_type" }` | Register a new user. `cohort_type`: `GOOD`, `BAD`, `MEDIUM` |

### Membership

| Method | Endpoint | Params | Description |
|---|---|---|---|
| `GET` | `/api/v1/membership/plans` | `phone_number` | Active plans + user's tier and benefits |
| `POST` | `/api/v1/membership/subscribe` | `phone_number`, `plan_name` | Subscribe to a plan |
| `PATCH` | `/api/v1/membership/changePlan` | `phone_number`, `new_plan_name` | Upgrade or downgrade (direction inferred from price) |
| `PATCH` | `/api/v1/membership/unsubscribe` | `phone_number` | Cancel active subscription |
| `GET` | `/api/v1/membership/userMembershipDetails` | `phone_number` | Current subscription details |

### Kafka Test

| Method | Endpoint | Body | Description |
|---|---|---|---|
| `POST` | `/api/v1/kafka/sendOrderEvent` | `{ "phone_number", "order_id", "order_value" }` | Simulate an order event. Triggers aggregate update + tier re-evaluation |



---

## Kafka Integration

### Topics

| Topic | Consumer | Concurrency | Purpose |
|---|---|---|---|
| `order-topic` | `OrderConsumer` | Configurable (default 1) | Drives order aggregation and tier re-evaluation |
| `order-topic.DLT` | — | — | Dead-letter: orders that failed after all retries |
| `payment-topic` | `PaymentConsumer` | 1 (strict ordering) | Payment event tracking (stub — pending implementation) |
| `payment-topic.DLT` | — | — | Dead-letter: failed payment events |

### Reliability Features

**Manual Acknowledgement** — offsets are committed only after `processOrder()` succeeds. A crash mid-processing causes Kafka to redeliver the message rather than silently losing it.

**Retry + Dead Letter Topic** — on failure, the message is retried 3 times with a 1-second interval. After all retries are exhausted, it is published to `<topic>.DLT` so processing of the live partition continues unblocked.

**Idempotent Consumer** — each `order_id` is recorded in a `processed_orders` table. Kafka redeliveries are detected and skipped before any business logic runs, preventing double-counting.

### Event Flow

```
POST /api/v1/kafka/sendOrderEvent
        │
        ▼
KafkaProducerService ──► order-topic
                                │
                    ┌───────────▼────────────────┐
                    │  OrderConsumer              │
                    │  1. idempotency check       │
                    │  2. processOrder()  ──► ack │
                    │  3. evaluateRules()         │
                    └──────────┬─────────────────┘
                     failure? ─┤
                               ▼
                    retry x3 ──► order-topic.DLT
```

---

## Design Patterns

| Pattern | Where | Why |
|---|---|---|
| **Strategy** | `TierRuleEvaluator` interface + 3 implementations | Each evaluator (`ORDER_COUNT`, `ORDER_VALUE`, `USER_COHORT`) is a pluggable strategy selected by the factory |
| **Template Method** | `AbstractTierRuleEvaluator` | Fetch-criteria → guard-empty → delegate is defined once; subclasses only implement `doEvaluate()` |
| **Factory** | `TierRuleEvaluatorFactory` | Resolves the correct evaluator by `CriteriaType` at runtime |
| **Chain of Responsibility** | `GlobalExceptionHandler` | Exception types are handled in order from most-specific (`UserNotFoundException`) to generic fallback |
| **Idempotent Consumer** | `ProcessedOrder` entity | Prevents duplicate processing on Kafka redelivery |
| **State** | `SubscriptionStatus`, `PlanStatus` enums + `AttributeConverter` | DB stores lowercase strings (`"active"`); Java uses uppercase enum constants; converter bridges both |

---

## Observability

| Endpoint | Description |
|---|---|
| `GET /actuator/health` | Application health status |
| `GET /actuator/info` | Build and version info |
| `GET /actuator/prometheus` | Prometheus metrics scrape endpoint |

Kafka consumer metrics exposed automatically (requires Micrometer):

- `kafka.consumer.fetch.manager.records.lag` — consumer lag per partition
- `spring.kafka.listener.seconds` — listener processing latency

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 21+ |
| Maven | 3.9+ |
| Docker Desktop | Latest |

---

## Running Locally

### 1. Start infrastructure (PostgreSQL + Kafka)

```bash
docker compose up -d
```

This starts PostgreSQL on port `5432`, Zookeeper, and Kafka on port `9092`. Hibernate auto-creates the schema on first run (`ddl-auto: update`).

Verify containers are running:

```bash
docker ps
```

Stop when done:

```bash
docker compose down
```

### 2. Start the Application

```bash
./mvnw spring-boot:run
```

The application starts on **http://localhost:8080**.

### 3. Explore the API

Open Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```
http://localhost:8080/v3/api-docs
```

### Quick Start Walkthrough

```bash
# 1. Create a user
curl -X POST http://localhost:8080/api/users/create \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","phone_number":"9876543210","cohort_type":"GOOD"}'

# 2. View available plans (user starts on Silver tier)
curl "http://localhost:8080/api/v1/membership/plans?phone_number=9876543210"

# 3. Subscribe
curl -X POST "http://localhost:8080/api/v1/membership/subscribe?phone_number=9876543210&plan_name=Monthly"

# 4. Send an order event (triggers tier re-evaluation)
curl -X POST http://localhost:8080/api/v1/kafka/sendOrderEvent \
  -H "Content-Type: application/json" \
  -d '{"phone_number":"9876543210","order_id":"ORD-001","order_value":5000}'

# 5. Check membership details
curl "http://localhost:8080/api/v1/membership/userMembershipDetails?phone_number=9876543210"
```

---

## Configuration Reference

```yaml
# Database
spring.datasource.url: jdbc:postgresql://localhost:5432/membership
spring.datasource.username: postgres
spring.datasource.password: postgres

# Kafka broker
spring.kafka.bootstrap-servers: localhost:9092

# Consumer group and offset behaviour
spring.kafka.consumer.group-id: ecommerce-group
spring.kafka.consumer.auto-offset-reset: earliest
spring.kafka.consumer.enable-auto-commit: false   # manual ack
spring.kafka.consumer.concurrency: 1              # listener threads (order consumer)

# Topic names
app.kafka.topics.order: order-topic
app.kafka.topics.payment: payment-topic
app.kafka.topics.order-dlt: order-topic.DLT
app.kafka.topics.payment-dlt: payment-topic.DLT

# Retry policy (applied to both consumers)
app.kafka.retry.max-attempts: 3
app.kafka.retry.interval-ms: 1000
```
