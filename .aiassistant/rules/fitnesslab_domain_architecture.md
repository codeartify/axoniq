---
apply: always
---

# FitnessLab – Domain & Architecture Overview (Config-Driven Products)

This document describes the domain model, bounded contexts, and high-level architecture
for the FitnessLab system, using configurable products and a single generic
**ProductContract** aggregate to represent all “owned” services (fitness membership,
Running School, Metabolic Health, future offerings).

---

## 1. Bounded Contexts

### 1.1 Customer Context

**Responsibility**  
Manage customer identity, contact data, membership activity, and blacklist status.

**Key concepts**

- `Customer` (aggregate root)
- `CustomerType`: `MEMBER` or `EXTERNAL`
- `MembershipStatus`: `NONE | ACTIVE | EXPIRED`  
  - A customer is `ACTIVE` while they have at least one membership‑contributing
    ProductContract that is not expired. Cancelled‑but‑running still counts as ACTIVE.
- `isBlacklisted`: Boolean

**Integration**

- Consumes events from Product Contract and Billing contexts to update:
  - membership status (based on active membership‑contributing contracts)
  - blacklist status (based on dunning).


### 1.2 Product & Tariff Context

**Responsibility**  
Define *what* can be sold and *how it behaves* without changing code:
fitness memberships, Running School, Metabolic programs, future offerings.

**Key concepts**

- `Product`
- `ProductVariant` – concrete sellable tariff

```text
ProductVariant
  id: ProductVariantId
  code: String              // e.g. "FITNESS_12M", "RS_1TO1_6", "METABOLIC_10W"
  productType: String       // free text, e.g. "MEMBERSHIP", "SESSION_PACKAGE", "COURSE"
  audience: INTERNAL | EXTERNAL | BOTH
  requiresMembership: Boolean
  behavior: ProductBehaviorConfig
```

```text
ProductBehaviorConfig
  isTimeBased: Boolean
  isSessionBased: Boolean
  canBePaused: Boolean
  autoRenew: Boolean
  renewalLeadTimeDays: Int?        // e.g. 120 for 4 months
  contributesToMembershipStatus: Boolean
  maxActivePerCustomer: Int?       // e.g. 1 for fitness
  exclusivityGroup: String?        // e.g. "FITNESS", "RUNNING_SCHOOL"
```

Examples (informal):
- Fitness 12M: time‑based, can pause, auto renew, membership‑contributing, maxActivePerCustomer=1, group="FITNESS".
- Running School 1:1: session‑based, no auto renew, group="RUNNING_SCHOOL", maxActivePerCustomer=1.
- Metabolic 10‑weeks: time‑based course, no auto renew, not membership‑contributing.

Persistence: classic relational/config service; no event sourcing required.


### 1.3 Booking Context

**Responsibility**  
Capture what a customer orders before any invoice or product contract is created.

**Aggregate root – `BookingAggregate`**

- `bookingId`
- `payerCustomerId`
- `status`: `PENDING | CONFIRMED | CANCELLED`
- `purchasedProducts: List<PurchasedProduct>`
- `invoiceIds: List<InvoiceId>`

`PurchasedProduct`:

- `productVariantId`
- `participants: List<Participant>`
- `totalPrice` (pre‑calculated)

`Participant`:

- optional `customerId`
- `displayName`
- `email`

**Events**

- `BookingPlaced`
- `BookingConfirmed`
- `BookingCancelled`

**Integration**

- `BookingPlaced` → Billing context creates invoice(s).
- For memberships, cancellation via booking is limited (wrong contracts fixed via trainer correction rules).


### 1.4 Billing & Payment Context

**Responsibility**  
Manage invoices, instalments, payment tracking, overdue detection, and dunning.

**Aggregate root – `InvoiceAggregate`**

- `invoiceId`
- `bookingId`
- (optional) purchased product reference
- `amount`
- `dueDate`
- `status`: `OPEN | PAID | OVERDUE | CANCELLED`
- `isInstallment: Boolean`
- `installmentNumber: Int?` (1..3)

**Events**

- `InvoiceCreated`
- `InvoicePaid`
- `InvoiceOverdueMarked`
- `InvoiceCancelled`

**DunningSaga**

- Drives:
  - first reminder (email, +10 days)
  - second reminder (email + letter, +5 days, fee invoice)
  - final escalation: blacklist customer + block contracts.

**Integration**

- Receives `PaymentReceived` from PSP / bank (e.g. camt.054), mapped to `MarkInvoicePaid`.
- Emits `InvoicePaid` to Product Contract context.


### 1.5 Product Contract Context (Unified)

**Responsibility**  
Manage lifecycle of **all contract‑based products**:

- Fitness memberships
- Running School packages
- Metabolic / nutrition programs
- Future products with similar patterns

**Aggregate root – `ProductContractAggregate`**

- `contractId`
- `customerId`
- `productVariantId`
- `status`:
  - `PENDING_ACTIVATION`
  - `ACTIVE`
  - `PAUSED`
  - `COMPLETED`      // session/course finished
  - `EXPIRED`
  - `BLOCKED`
  - `CANCELLED`
- `validity: DateRange?` (for time‑based behavior)
- `usage: UsageState?`   (for session‑based behavior)

`UsageState`:

- `sessionsTotal: Int`
- `sessionsUsed: Int`

**Events**

- `ProductContractCreated`
- `ProductContractActivated`
- `ProductContractPaused`
- `ProductContractResumed`
- `ProductContractCancelled`
- `ProductContractCompleted`
- `ProductContractExpired`
- `ProductContractBlocked`
- `ProductContractUnblocked`
- `ProductContractSessionsConsumed`

**Behaviour (driven by ProductBehaviorConfig)**

The aggregate never switches on hard‑coded enums like `MEMBERSHIP`; it uses behavior flags:

- Activation on `InvoicePaid` or on signing (for membership, as per rules).
- Pausing only if `behavior.canBePaused = true` and external preconditions are satisfied.
- Auto‑renew via `autoRenew` + `renewalLeadTimeDays` (scheduler issues renewal invoices).
- Session consumption if `isSessionBased = true` (complete when sessionsUsed == sessionsTotal).
- Expiry if `isTimeBased = true` and `validity.end` passed.
- Blocking on dunning escalation; unblocking when all related invoices are paid.

**Concurrency / exclusivity enforcement**

Before creating a new contract:

- Load all non‑expired contracts for that customer.
- Read `maxActivePerCustomer` + `exclusivityGroup` from ProductVariant.
- Enforce:
  - at most one active fitness membership (group "FITNESS", maxActivePerCustomer=1)
  - at most one Running School contract in group "RUNNING_SCHOOL"
  - similar constraints for future product groups.

**Integration**

- Consumes `InvoicePaid`, dunning events, scheduler commands.
- Emits lifecycle events for:
  - Customer context (to recompute MembershipStatus)
  - Read models (dashboards, overviews).


---

## 2. Integration Overview

### 2.1 Payment Integration

External sources:

- PSP webhooks (card, TWINT, etc.)
- Bank transfers (ISO 20022 camt.054)

Mapped to:

- `MarkInvoicePaid` commands → `InvoiceAggregate` → `InvoicePaid` events.


### 2.2 Email & Letter Delivery

Notification subsystem:

- Sends invoices, reminders, renewal notices, expiry notifications.
- Prints second reminder letters.

Driven by events:

- `InvoiceCreated`
- Dunning events (e.g. `FirstReminderDue`, `SecondReminderDue`)
- `ProductContractExpired` for membership contracts.


### 2.3 Scheduler / Time-based Triggers

Used for:

- Marking invoices overdue (`MarkInvoiceOverdue`)
- Advancing dunning steps
- Expiring contracts (`ExpireProductContract`)
- Issuing renewal invoices (`IssueRenewalInvoice`)
- Expiring session‑based contracts when validity window ends


---

## 3. Technology & Architectural Style

### Backend

- Kotlin, Spring Boot
- Axon Framework (CQRS + Event Sourcing)
- Event store for:
  - `BookingAggregate`
  - `InvoiceAggregate`
  - `ProductContractAggregate`
- Relational read models for queries, dashboards, and integration.


### Frontend

- Angular 19+/20
- Angular Signals for feature state
- Vertical feature modules per business area (memberships, billing, running school, metabolic, dunning, customer profile).


### Communication

- REST / JSON for synchronous APIs
- Asynchronous domain events for cross‑context integration and projections.


---

## 4. Aggregate Responsibilities (Summary)

### BookingAggregate

- Capture and validate orders (`BookingPlaced`).
- Link bookings to invoices.
- Allow cancellation within business rules (esp. for add‑ons).

### InvoiceAggregate

- Manage single invoice state across `OPEN → PAID / OVERDUE / CANCELLED`.
- Drive dunning and contract activation via events.

### ProductContractAggregate

- Represent a customer’s entitlement to any configured `ProductVariant`.
- Govern lifecycle for memberships, running school packages, metabolic programs, future products.
- Enforce behavior via `ProductBehaviorConfig`, not hard‑coded enums.


---

## 5. Read Models

Typical projections:

- `CustomerOverview`
- `MembershipOverview` (contracts where `contributesToMembershipStatus = true`)
- `ProductContractOverview` (all contracts)
- `InvoiceList` / `OpenInvoices`
- `DunningDashboard`
- `RunningSchoolOverview`
- `MetabolicProgramOverview`
- `BookingHistory`
- `RenewalDashboard` (contracts approaching expiry)

Each read model subscribes to events from Booking, Invoice, and ProductContract aggregates.

---

End of Domain & Architecture Overview.
