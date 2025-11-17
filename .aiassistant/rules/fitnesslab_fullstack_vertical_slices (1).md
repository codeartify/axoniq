---
apply: always
---

# FitnessLab – Full-Stack Vertical Slices  
(Axon Backend + Angular 19/20 + Signals, Config-Driven Products)

This document unifies backend and frontend slices into full-stack flows,
aligned with the config-driven product model and unified `ProductContractAggregate`.

Each slice delivers end-to-end value: API + domain logic + UI.

---

## FS-1 – Membership Sign-Up (Initial Fitness Contract)

- Backend:
  - `POST /api/memberships/sign-up`
  - Commands: `CreateProductContract`, `PlaceBooking`, `CreateInvoice`, `MarkInvoicePaid`
  - Aggregates: `ProductContractAggregate`, `BookingAggregate`, `InvoiceAggregate`
- Frontend:
  - Route: `/memberships/sign-up`
  - Components: `MembershipSignUpComponent`, `sign-up.state.ts`
  - Uses: `GET /api/products/memberships`, `POST /api/memberships/sign-up`
- Value: trainers can fully sign up a member and see confirmation with contract + invoice IDs.


## FS-2 – Invoice Overview & Payment

- Backend:
  - `GET /api/invoices?status=...`, `GET /api/invoices/{id}`, `POST /api/invoices/{id}/pay`
  - Command: `MarkInvoicePaid`
  - Event: `InvoicePaid`
- Frontend:
  - Route: `/billing/invoices`
  - Components: `InvoiceListComponent`, `InvoiceDetailComponent`, `invoices.state.ts`
- Value: staff can see invoices, filter them, and mark them paid in real time.


## FS-3 – Membership Details & Pause (ProductContract)

- Backend:
  - `GET /api/product-contracts/{id}`
  - `POST /api/product-contracts/{id}/pause`
  - `POST /api/product-contracts/{id}/resume`
  - Commands: `PauseProductContract`, `ResumeProductContract`
  - Events: `ProductContractPaused`, `ProductContractResumed`
- Frontend:
  - Route: `/memberships/:id`
  - Components: `MembershipDetailComponent`, `PauseDialogComponent`
  - Signals: `membership`, `canPause`, `isPauseDialogOpen`, `pauseForm`
- Value: membership can be paused and resumed with all rules enforced.


## FS-4 – Cancel & Reactivate Membership

- Backend:
  - `GET /api/product-contracts/{id}/cancellation-info`
  - `POST /api/product-contracts/{id}/cancel`
  - `POST /api/product-contracts/{id}/reactivate`
  - Commands: `CancelProductContract`, `ReactivateProductContract`
- Frontend:
  - Components: `CancelMembershipDialog`, `ReactivateMembershipDialog`
  - Uses cancellation info to show allowed dates, requires justification on second reactivation.
- Value: cancellation and reactivation flow fully implemented, respecting domain rules.


## FS-5 – Dunning & Blacklist Dashboard

- Backend:
  - DunningSaga + scheduler drive reminders and blacklisting.
  - `GET /api/dunning/overview`
  - `POST /api/customers/{id}/unblacklist`
- Frontend:
  - Route: `/dunning`
  - Component: `DunningDashboardComponent`, `dunning.state.ts`
- Value: backoffice sees overdue invoices, second reminders, blacklisted customers, and can unblacklist.


## FS-6 – Running School Booking & Sessions

- Backend:
  - `GET /api/products/running-school`
  - `POST /api/running-school/book`
  - `GET /api/product-contracts?type=RUNNING_SCHOOL`
  - `POST /api/product-contracts/{id}/consume-session`
  - Domain:
    - `PlaceBooking` → `BookingPlaced` → `CreateInvoice` → `InvoiceCreated`
    - `InvoicePaid` → `CreateProductContract` (session-based)
    - `ConsumeProductSession` → `ProductContractSessionsConsumed`, `ProductContractCompleted`
- Frontend:
  - Routes: `/running-school/book`, `/running-school/contracts`
  - Components: `RunningBookingComponent`, `RunningContractListComponent`, `RunningContractDetailComponent`
- Value: Running School can be sold and tracked end-to-end for members and externals.


## FS-7 – Metabolic / Nutrition Program Enrolment

- Backend:
  - `GET /api/products/metabolic`
  - `POST /api/metabolic/enroll`
  - `GET /api/product-contracts?type=METABOLIC_HEALTH`
  - `POST /api/product-contracts/{id}/complete`
- Frontend:
  - Routes: `/metabolic/enrol`, `/metabolic/contracts`
  - Components: `MetabolicEnrolmentComponent`, `MetabolicContractListComponent`, `MetabolicContractDetailComponent`
- Value: metabolic programs are enrolable, billable, and trackable without adding new aggregate types.


## FS-8 – Customer Profile & History

- Backend:
  - `GET /api/customers?query=...`
  - `GET /api/customers/{id}/profile`
  - Profile combines:
    - Customer info
    - MembershipStatus + blacklist
    - Contracts (ProductContracts)
    - Invoices + open items
    - Bookings history
- Frontend:
  - Route: `/customers`
  - Components: `CustomerSearchComponent`, `CustomerProfileComponent`
  - State: `customer-profile.state.ts`
- Value: staff get a 360° view of each customer across membership, add-ons, invoices, and dunning.


---

End of Full-Stack Vertical Slices.
