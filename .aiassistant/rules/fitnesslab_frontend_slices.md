---
apply: always
---

# FitnessLab – Frontend Vertical Slices (Angular 19/20 + Signals)

This document describes frontend vertical slices using Angular 19/20,
aligned with the config‑driven product model and unified ProductContract backend.

Each slice maps to backend functionality and aims to be shippable on its own.

---

## General Frontend Architecture

- **Framework:** Angular 19+/20 (standalone components)
- **State:** Angular Signals for feature/local state
- **Styling:** TailwindCSS (or similar)
- **Structure:**

```text
/app/features/memberships/...
/app/features/billing/...
/app/features/running-school/...
/app/features/metabolic/...
/app/features/customers/...
/app/features/dunning/...
/app/shared/...
```

Each feature usually contains:

- `routes.ts`
- components (UI)
- signal‑based state files
- API services


---

## Slice F1 – Membership Sign-up

### UX Flow

1. Trainer opens “New Membership” screen.
2. Enters customer data and health questionnaire.
3. Selects membership tariff (from `ProductVariant` list for memberships).
4. Chooses payment mode (on‑site vs invoice).
5. Captures signature.
6. On submit:
   - Calls `POST /api/memberships/sign-up`.
   - Shows confirmation with contractId, bookingId, invoiceId.

### Structure

```text
/app/features/memberships/sign-up/
  - sign-up.component.ts / .html
  - sign-up.state.ts
  - memberships.api.service.ts
  - routes.ts
```

**Signals**

```ts
const customerForm = signal<CustomerForm>({...});
const membershipVariants = signal<ProductVariantDto[]>([]);
const selectedVariant = signal<ProductVariantDto | null>(null);
const paymentMode = signal<'PAY_ON_SITE' | 'INVOICE_EMAIL'>('INVOICE_EMAIL');
const isSubmitting = signal(false);
const error = signal<string | null>(null);
const result = signal<SignUpResultDto | null>(null);
```

**API**

- `GET /api/products/memberships`
- `POST /api/memberships/sign-up`


---

## Slice F2 – Invoice Overview & Payment

### UX Flow

1. Staff opens “Invoices” overview.
2. Filters by status (OPEN, OVERDUE, PAID).
3. Selects invoice to see details.
4. For on‑site payments, clicks “Mark as paid”.
5. List updates after response.

### Structure

```text
/app/features/billing/invoices/
  - invoice-list.component
  - invoice-detail.component
  - invoices.state.ts
  - billing.api.service.ts
  - routes.ts
```

**Signals**

```ts
const invoices = signal<InvoiceDto[]>([]);
const selectedInvoice = signal<InvoiceDto | null>(null);
const filterStatus = signal<'OPEN' | 'OVERDUE' | 'PAID' | 'ALL'>('OPEN');
const isLoading = signal(false);
const payInProgress = signal(false);
const error = signal<string | null>(null);
```

**API**

- `GET /api/invoices?status=...`
- `GET /api/invoices/{id}`
- `POST /api/invoices/{id}/pay`


---

## Slice F3 – Membership Details & Pause

### UX Flow

1. Staff opens membership detail page (ProductContract with membership behavior).
2. Sees status (ACTIVE, PAUSED, CANCELLED, EXPIRED, BLOCKED), start/end dates, pause history.
3. If pause allowed, shows “Pause” button.
4. Trainer enters pause dates + reason type (medical/travel/military).
5. On submit, UI calls pause API and updates view.

### Structure

```text
/app/features/memberships/detail/
  - membership-detail.component
  - pause-dialog.component
  - membership-detail.state.ts
  - routes.ts
```

**Signals**

```ts
const membership = signal<ProductContractDetailDto | null>(null);
const canPause = computed(() =>
  membership()?.status === 'ACTIVE' &&
  membership()?.behavior.canBePaused === true &&
  membership()?.paymentStatus === 'PAID_UP'
);
const isPauseDialogOpen = signal(false);
const pauseForm = signal<PauseForm | null>(null);
const isSubmittingPause = signal(false);
const error = signal<string | null>(null);
```

**API**

- `GET /api/product-contracts/{id}`
- `POST /api/product-contracts/{id}/pause`
- `POST /api/product-contracts/{id}/resume`


---

## Slice F4 – Cancel & Reactivate Membership

### UX Flow

1. On membership detail page, user can see cancellation information (latest allowed date, rules).
2. Can initiate cancellation via dialog.
3. After cancellation, “Reactivate” button is visible until expiry.
4. On first reactivation → simple confirmation.
5. On second reactivation → justification text area (min 100 chars).

### Additions

Components:

- `cancel-membership-dialog.component`
- `reactivate-membership-dialog.component`

**Signals**

```ts
const cancellationInfo = signal<CancellationInfoDto | null>(null);
const reactivationCount = signal<number>(0);
const justification = signal<string>('');
const isCancelDialogOpen = signal(false);
const isReactivateDialogOpen = signal(false);
```

**API**

- `GET /api/product-contracts/{id}/cancellation-info`
- `POST /api/product-contracts/{id}/cancel`
- `POST /api/product-contracts/{id}/reactivate`


---

## Slice F5 – Dunning & Blacklist Dashboard

### UX Flow

1. Backoffice opens “Dunning Dashboard”.
2. Sees overview tiles (overdue invoices, second reminder, blacklisted customers).
3. Can drill down to a customer:
   - open invoices
   - contract statuses
   - dunning history
4. Can manually unblacklist a customer once all payments are resolved.

### Structure

```text
/app/features/dunning/dashboard/
  - dunning-dashboard.component
  - dunning.state.ts
  - dunning.api.service.ts
  - routes.ts
```

**Signals**

```ts
const overview = signal<DunningOverviewDto | null>(null);
const selectedCustomer = signal<CustomerDunningDetailDto | null>(null);
const isLoading = signal(false);
const error = signal<string | null>(null);
```

**API**

- `GET /api/dunning/overview`
- `POST /api/customers/{id}/unblacklist`


---

## Slice F6 – Running School Booking & Sessions

### UX Flow

**Booking**

1. Trainer opens “Running School booking” screen.
2. Selects a session‑based `ProductVariant` for Running School.
3. Adds 1–3 participants.
4. UI computes total price based on variant + participant count.
5. Chooses payment mode.
6. Submits → backend booking + invoice.

**Session tracking**

1. Coach opens “Running School contracts” list.
2. Selects a contract.
3. Clicks “Consume session” after each unit.
4. UI shows remaining sessions and contract status (ACTIVE/COMPLETED/EXPIRED).

### Structure

```text
/app/features/running-school/booking/
  - running-booking.component
  - running-booking.state.ts
  - running-school.api.service.ts

/app/features/running-school/contracts/
  - running-contract-list.component
  - running-contract-detail.component
  - running-contract.state.ts
```

**Signals (booking)**

```ts
const variants = signal<ProductVariantDto[]>([]);
const selectedVariant = signal<ProductVariantDto | null>(null);
const participants = signal<RunningParticipantForm[]>([]);
const calculatedPrice = computed(() => calculateRunningPrice(selectedVariant(), participants()));
const paymentMode = signal<'PAY_ON_SITE' | 'INVOICE_EMAIL'>('INVOICE_EMAIL');
const isSubmitting = signal(false);
const error = signal<string | null>(null);
```

**API**

- `GET /api/products/running-school`
- `POST /api/running-school/book`
- `GET /api/product-contracts?type=RUNNING_SCHOOL`
- `POST /api/product-contracts/{id}/consume-session`


---

## Slice F7 – Metabolic / Nutrition Course Enrolment

### UX Flow

1. Staff opens “Metabolic/Nutrition enrolment” screen.
2. Selects internal or external course variant.
3. Selects or creates customer.
4. Submits enrolment → booking + invoice.
5. After payment, course contract appears in “My Programs”.

### Structure

```text
/app/features/metabolic/enrolment/
  - metabolic-enrolment.component
  - metabolic-enrolment.state.ts

/app/features/metabolic/contracts/
  - metabolic-contract-list.component
  - metabolic-contract-detail.component
```

**Signals**

```ts
const metabolicVariants = signal<ProductVariantDto[]>([]);
const selectedVariant = signal<ProductVariantDto | null>(null);
const customer = signal<CustomerDto | null>(null);
const isSubmitting = signal(false);
const error = signal<string | null>(null);

const contracts = signal<ProductContractDto[]>([]);
const selectedContract = signal<ProductContractDto | null>(null);
```

**API**

- `GET /api/products/metabolic`
- `POST /api/metabolic/enroll`
- `GET /api/product-contracts?type=METABOLIC_HEALTH`
- `POST /api/product-contracts/{id}/complete`


---

## Slice F8 – Customer Profile & History

### UX Flow

1. Staff searches for a customer by name, email, or member number.
2. Selects customer from results.
3. Profile view shows:
   - Contact data
   - MembershipStatus + blacklist flag
   - Product contracts (membership, running school, metabolic, etc.)
   - Invoices and open payments
   - Bookings history

### Structure

```text
/app/features/customers/profile/
  - customer-search.component
  - customer-profile.component
  - customer-profile.state.ts
  - customers.api.service.ts
```

**Signals**

```ts
const searchQuery = signal<string>('');
const searchResults = signal<CustomerSummaryDto[]>([]);
const selectedCustomerId = signal<string | null>(null);
const profile = signal<CustomerProfileDto | null>(null);
const isLoading = signal(false);
const error = signal<string | null>(null);
```

**API**

- `GET /api/customers?query=...`
- `GET /api/customers/{id}/profile`


---

End of Frontend Vertical Slices.
