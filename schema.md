# Database Schema

PostgreSQL 16 — 10 tables, auto-created by Hibernate (`ddl-auto: update`)

---

## Entity Relationship Overview

```
membership_tiers
      │
      ├──< tier_benefits          (1 tier → many benefits)
      ├──< tier_criteria          (1 tier → many criteria)
      └──< user                   (1 tier → many users)
                │
                └──< user_membership ──── membership_plans
                          │
                          └──< membership_payment_transactions

user ──< order_aggregate          (1 user → many monthly aggregates)

audit_log  (records plan + tier changes; nullable FKs to plans and tiers)
processed_orders  (idempotency guard for Kafka order events)
```

---

## Tables

### membership_tiers

| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, IDENTITY |
| name | VARCHAR | NOT NULL |
| level | INTEGER | NOT NULL |
| description | VARCHAR | |
| is_active | BOOLEAN | NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

---

### membership_plans

| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, IDENTITY |
| plan_name | VARCHAR | NOT NULL |
| duration_type | VARCHAR | NOT NULL — `MONTHLY` \| `QUARTERLY` \| `ANNUALLY` |
| duration_in_days | INTEGER | NOT NULL |
| price | NUMERIC(19,2) | NOT NULL |
| status | VARCHAR | NOT NULL — `active` \| `inactive` |

---

### user

| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, IDENTITY |
| membership_tier_id | BIGINT | FK → membership_tiers(id) |
| name | VARCHAR | NOT NULL |
| email | VARCHAR | UNIQUE |
| phone_number | VARCHAR | NOT NULL, UNIQUE |
| cohort_type | VARCHAR | NOT NULL — `GOOD` \| `BAD` \| `MEDIUM` |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

**Foreign keys:** `fk_user_membership_tier` → `membership_tiers(id)`

---

### user_membership

| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, IDENTITY |
| user_id | BIGINT | NOT NULL, FK → user(id) |
| membership_plan_id | BIGINT | NOT NULL, FK → membership_plans(id) |
| status | VARCHAR | NOT NULL — `active` \| `cancelled` \| `expired` \| `pending` |
| start_date | TIMESTAMPTZ | NOT NULL |
| end_date | TIMESTAMPTZ | NOT NULL |
| version | BIGINT | NOT NULL — optimistic lock |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

**Foreign keys:** `fk_user_membership_user` → `user(id)`, `fk_user_membership_plan` → `membership_plans(id)`
**Index:** `idx_user_membership_user_status` on `(user_id, status)`

---

### tier_benefits

| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, IDENTITY |
| membership_tier_id | BIGINT | NOT NULL, FK → membership_tiers(id) |
| benefit_type | VARCHAR | NOT NULL — `FREE_DELIVERY` \| `DISCOUNTS` \| `EARLY_ACCESS` |
| config | JSONB | NOT NULL — benefit-specific parameters |
| is_active | BOOLEAN | NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

**Foreign keys:** `fk_tier_benefit_membership_tier` → `membership_tiers(id)`

**Sample config values:**
```json
FREE_DELIVERY : { "min_order_value": 299 }
DISCOUNTS     : { "percent": 10, "categories": ["fashion","home"] }
EARLY_ACCESS  : { "hours_before_sale": 24 }
```

---

### tier_criteria

| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, IDENTITY |
| membership_tier_id | BIGINT | NOT NULL, FK → membership_tiers(id) |
| criteria_type | VARCHAR | NOT NULL — `ORDER_COUNT` \| `ORDER_VALUE` \| `USER_COHORT` |
| threshold_value | DOUBLE PRECISION | |
| period_in_days | INTEGER | |
| operator | VARCHAR | NOT NULL — `EQUALS` \| `GREATER_THAN` \| `GREATER_THAN_OR_EQUALS` \| `LESS_THAN` \| `IN` … |
| additional_parameters | JSONB | NOT NULL — e.g. `{"cohortType": "GOOD"}` |
| priority | INTEGER | NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL |
| updated_at | TIMESTAMPTZ | NOT NULL |

**Foreign keys:** `fk_tier_criteria_membership_tier` → `membership_tiers(id)`

---

### order_aggregate

| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, IDENTITY |
| user_id | BIGINT | NOT NULL, FK → user(id) |
| order_month_year | VARCHAR | NOT NULL — format `YYYY-MM` |
| total_amount | NUMERIC | NOT NULL |
| order_count | INTEGER | NOT NULL |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

**Foreign keys:** `fk_order_aggregate_user` → `user(id)`
**Unique constraint:** `uk_order_aggregate_user_month` on `(user_id, order_month_year)`

---

### audit_log

| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, IDENTITY |
| phone_number | VARCHAR | NOT NULL |
| from_plan | BIGINT | FK → membership_plans(id), nullable |
| to_plan | BIGINT | FK → membership_plans(id), nullable |
| from_tier | BIGINT | FK → membership_tiers(id), nullable |
| to_tier | BIGINT | FK → membership_tiers(id), nullable |
| config | JSONB | nullable |
| created_at | TIMESTAMPTZ | |

**Foreign keys:** `fk_audit_log_from_plan`, `fk_audit_log_to_plan`, `fk_audit_log_from_tier`, `fk_audit_log_to_tier`

> Nullable FK columns record what changed: a plan-change event populates `from_plan`/`to_plan`; a tier-change event populates `from_tier`/`to_tier`.

---

### processed_orders

| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, IDENTITY |
| order_id | VARCHAR | NOT NULL, UNIQUE |
| processed_at | TIMESTAMPTZ | NOT NULL |

**Unique constraint:** `uk_processed_order_id` on `order_id`

> Idempotency guard — if an `order_id` already exists here, the Kafka consumer skips reprocessing.

---

### membership_payment_transactions

| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, IDENTITY |
| user_membership_id | BIGINT | NOT NULL, FK → user_membership(id) |
| amount | NUMERIC(19,2) | NOT NULL |
| payment_status | VARCHAR | NOT NULL |
| external_payment_id | VARCHAR | NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL |

**Foreign keys:** `fk_user_membership` → `user_membership(id)`

---

## Full Relationship Diagram

```
┌──────────────────┐        ┌─────────────────────┐
│ membership_tiers │        │   membership_plans   │
├──────────────────┤        ├─────────────────────┤
│ PK id            │        │ PK id               │
│    name          │        │    plan_name        │
│    level         │        │    duration_type    │
│    description   │        │    duration_in_days │
│    is_active     │        │    price            │
│    created_at    │        │    status           │
│    updated_at    │        └──────────┬──────────┘
└──────┬───────────┘                   │
       │                               │
       │ 1                             │ 1
       ├──────────────────────┐        │
       │                      │        │
       ▼ N                    ▼ N      ▼ N
┌─────────────┐   ┌───────────────────────────┐
│tier_benefits│   │       user_membership      │
├─────────────┤   ├───────────────────────────┤
│ PK id       │   │ PK id                     │
│ FK tier_id  │   │ FK user_id ───────────────┼──────────────────────┐
│ benefit_type│   │ FK membership_plan_id     │                      │
│ config(JSONB│   │    status                 │                      │
│ is_active   │   │    start_date             │                      ▼
│ created_at  │   │    end_date               │            ┌──────────────────┐
│ updated_at  │   │    version                │            │      user        │
└─────────────┘   │    created_at             │            ├──────────────────┤
                  │    updated_at             │            │ PK id            │
┌─────────────┐   └─────────────┬─────────────┘            │ FK tier_id       │
│tier_criteria│                 │                           │    name          │
├─────────────┤                 │ 1                         │    email         │
│ PK id       │                 ▼ N                         │    phone_number  │
│ FK tier_id  │   ┌────────────────────────────────┐        │    cohort_type   │
│ criteria_type   │membership_payment_transactions │        │    created_at    │
│ threshold   │   ├────────────────────────────────┤        │    updated_at    │
│ period_days │   │ PK id                          │        └────────┬─────────┘
│ operator    │   │ FK user_membership_id          │                 │
│ params(JSONB│   │    amount                      │                 │ 1
│ priority    │   │    payment_status              │                 ▼ N
│ created_at  │   │    external_payment_id         │   ┌──────────────────────┐
│ updated_at  │   │    created_at                  │   │   order_aggregate    │
└─────────────┘   └────────────────────────────────┘   ├──────────────────────┤
                                                        │ PK id               │
┌───────────────────────────────────────────────┐       │ FK user_id          │
│                  audit_log                    │       │    order_month_year │
├───────────────────────────────────────────────┤       │    total_amount     │
│ PK id                                         │       │    order_count      │
│    phone_number                               │       │    created_at       │
│ FK from_plan  ──► membership_plans            │       │    updated_at       │
│ FK to_plan    ──► membership_plans            │       └──────────────────────┘
│ FK from_tier  ──► membership_tiers            │
│ FK to_tier    ──► membership_tiers            │   ┌─────────────────────┐
│    config (JSONB)                             │   │   processed_orders  │
│    created_at                                 │   ├─────────────────────┤
└───────────────────────────────────────────────┘   │ PK id               │
                                                    │    order_id  UNIQUE │
                                                    │    processed_at     │
                                                    └─────────────────────┘
```
