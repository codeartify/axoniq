---
apply: always
---

# FitnessLab – Backend Vertical Slices (Axon + REST, Config-Driven)

This document describes backend vertical slices using Axon + Spring Boot,
aligned with the config‑driven product model and unified `ProductContractAggregate`.

Each slice includes:
- User stories
- Acceptance criteria
- Commands & Events
- REST endpoints
- Involved aggregates

---

## Slice B1 – Membership Sign-up (Create Fitness Contract)

### User Story

As a *prospective member*  
I want to sign a membership and start training immediately  
So that I can use the gym from day one, even if payment is later.

### Acceptance Criteria

- A membership `ProductVariant` is selected (behavior marks it as membership‑contributing).
- `ProductContract` is created and set to `ACTIVE` with correct validity.
- A `Booking` is created referencing the membership variant.
- An `Invoice` (or instalments) is created.
- On-site payment immediately marks invoice `PAID`.


### Backend Flow

1. `POST /api/memberships/sign-up`
2. App service:
   - Validates customer & guardian.
   - Validates membership variant and exclusivity (no second active membership).
   - Sends:
     - `CreateProductContract` (membership)
     - `PlaceBooking` (membership line)
3. `ProductContractAggregate` → `ProductContractCreated` (status = ACTIVE, validity set).
4. `BookingAggregate` → `BookingPlaced`.
5. Booking policy → `CreateInvoice`.
6. `InvoiceAggregate` → `InvoiceCreated`.
7. On PAY_ON_SITE: `MarkInvoicePaid` → `InvoicePaid`.

**Commands**

- `CreateProductContract`
- `PlaceBooking`
- `CreateInvoice`
- `MarkInvoicePaid`

**Endpoints**

- `GET /api/products/memberships`
- `POST /api/memberships/sign-up`


---

## Slice B2 – Pay Invoice (On-site + Bank Integration)

### User Stories

- Mark invoice as `PAID` for on-site payments.
- Automatically mark as `PAID` when bank/PSP reports payment.

### Acceptance Criteria

- `OPEN` invoices can be marked `PAID` once; after that they are immutable for payment.
- Late payment does **not** reactivate expired contracts.
- When all overdue and fee invoices are `PAID`, dunning policies may unblock contracts and unblacklist the customer.


### Backend Flow

1. On-site:
   - `POST /api/invoices/{invoiceId}/pay` → `MarkInvoicePaid` → `InvoicePaid`.
2. Bank import:
   - camt.054 / PSP webhook → parse transaction → `MarkInvoicePaid` → `InvoicePaid`.
3. Payment‑to‑Contract policy:
   - On `InvoicePaid`, check if this closes all overdue/fee invoices.
   - If yes: `UnblacklistCustomer`, `UnblockProductContract` as per rules.

**Command**

- `MarkInvoicePaid`

**Endpoints**

- `GET /api/invoices?status=...`
- `POST /api/invoices/{invoiceId}/pay`


---

## Slice B3 – Dunning & Blacklisting

### User Stories

- First reminder 30 days after due date.
- Second reminder + fee after further 10 days.
- Blacklist + block contracts after final non‑payment.

### Acceptance Criteria

- Day 30 overdue: first reminder email, dueDate += 10 days.
- After +10 days overdue: second reminder email + letter, fee invoice created, dueDate += 5 days.
- After +5 days overdue: customer blacklisted, relevant contracts blocked.
- Paying all overdue + fee invoices allows de‑blacklisting and unblocking.


### Backend Flow

- Scheduler:
  - Sends `MarkInvoiceOverdue` for invoices with `dueDate < today` and status `OPEN`.
- `DunningSaga`:
  - Reacts to overdue and reminder deadlines.
  - Commands:
    - `SendFirstReminder`
    - `SendSecondReminder`
    - `CreateInvoice` for the 10 CHF processing fee
    - `BlacklistCustomer`
    - `BlockProductContract` (usually membership contracts)
- Payment policy:
  - On `InvoicePaid`, checks whether all dunning‑relevant invoices are paid.
  - If yes: `UnblacklistCustomer`, `UnblockProductContract`.

**Endpoints**

- Mainly scheduler + saga + notification integration; optional:
  - `GET /api/dunning/overview` for dashboards.


---

## Slice B4 – Pause Membership (ProductContract)

### User Story

As a *member*  
I want to pause my membership for valid reasons  
So that I don’t lose contract time while I cannot train.

### Acceptance Criteria

- Contract is a membership‑contributing ProductContract with `behavior.canBePaused = true`.
- Contract status is `ACTIVE`.
- Membership invoices are fully paid.
- Pause duration 3–8 weeks; max 2 months per membership year.
- End date extended by pause duration.
- Status `PAUSED` during pause; returns to `ACTIVE` when resumed.


### Backend Flow

1. `POST /api/product-contracts/{contractId}/pause`
2. App service:
   - Validates business rules and `ProductBehaviorConfig`.
   - Sends `PauseProductContract`.
3. `ProductContractAggregate`:
   - Stores pause range, sets `status = PAUSED`.
   - Emits `ProductContractPaused`.
4. Resumption (scheduler or manual endpoint `.../resume`):
   - `ResumeProductContract` → extend `validity.end` → `ProductContractResumed`.


---

## Slice B5 – Cancel & Reactivate Membership

### User Stories

- Cancel membership with proper notice period.
- Reactivate once; second time requires justification; after expiry no reactivation.

### Backend Flow

- `GET /api/product-contracts/{id}/cancellation-info`
- `POST /api/product-contracts/{id}/cancel` → `CancelProductContract` → `ProductContractCancelled`
- `POST /api/product-contracts/{id}/reactivate` → `ReactivateProductContract` → `ProductContractActivated`

The aggregate tracks reactivation count and requires justification ≥ 100 chars on second attempt.


---

## Slice B6 – Membership Renewal

### User Story

As *FitnessLab*  
I want automatic renewal flows for certain membership products  
So that contracts can be extended smoothly when customers pay.

### Acceptance Criteria

- For variants with `behavior.autoRenew = true`:
  - At `validity.end - renewalLeadTimeDays`:
    - Renewal invoice is created and emailed.
- If renewal invoice is `PAID` before `validity.end`:
  - Contract validity extended (e.g. +12 months).
- If renewal invoice is `PAID` after `EXPIRED`:
  - Invoice is settled; contract is not automatically reactivated.


### Backend Flow

- Scheduler finds membership contracts:
  - `behavior.autoRenew = true`
  - `today = validity.end - renewalLeadTimeDays`
- For each: `IssueRenewalInvoice` (or `PlaceBooking` + `CreateInvoice`).
- On `InvoicePaid` for renewal invoices:
  - Policy sends `ExtendProductContract` to extend `validity.end`.


---

## Slice B7 – Running School Booking (Session-Based Product)

### User Story

As *trainer/frontdesk*  
I want to book Running School packages for members or external customers  
So that they can attend coached running sessions.

### Backend Flow

- Running School variants:
  - `behavior.isSessionBased = true`
  - `maxActivePerCustomer = 1`, `exclusivityGroup = "RUNNING_SCHOOL"`
  - `requiresMembership` may be false for external variants.

1. `POST /api/running-school/book` → app service sends `PlaceBooking`.
2. `BookingPlaced` → policy `CreateInvoice`.
3. `InvoicePaid` → policy `CreateProductContract` with `usage.sessionsTotal` set.
4. Session consumption:
   - `POST /api/product-contracts/{id}/consume-session` → `ConsumeProductSession` → `ProductContractSessionsConsumed`.
   - When sessionsUsed == sessionsTotal → `ProductContractCompleted`.


---

## Slice B8 – Metabolic / Nutrition Course Enrolment

### User Story

As a *customer*  
I want to enroll in a metabolic/nutrition program  
So that I can follow a structured health plan.

### Backend Flow

- Metabolic variants:
  - `behavior.isTimeBased = true`
  - `behavior.autoRenew = false`
  - `requiresMembership` true/false depending on variant.

1. `POST /api/metabolic/enroll` → `PlaceBooking`.
2. `BookingPlaced` → `CreateInvoice`.
3. `InvoicePaid` → `CreateProductContract` (time‑based course).
4. Completion:
   - `POST /api/product-contracts/{id}/complete` → `CompleteProductContract` → `ProductContractCompleted`.
5. Expiry:
   - Scheduler `ExpireProductContract` at `validity.end` → `ProductContractExpired`.


---

## Slice B9 – Customer Profile & History (Backend)

Provides aggregated projections for frontend slice FS‑8:

- `GET /api/customers?query=...`
- `GET /api/customers/{id}/profile`

Profile includes:

- Customer identity
- MembershipStatus + isBlacklisted
- Product contracts summary
- Invoices summary
- Bookings history

---

End of Backend Vertical Slices.
