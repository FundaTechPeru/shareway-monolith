# Integration map

How the bounded contexts of the monolith talk to each other, and what changes when one of them
is extracted to its own service (Strangler Fig, phase C).

## Rules

- A context never reads the tables of another one: each owns its schema (`dm`, `tpm`, `bp`, `ps`)
  and references the others only by UUID, without foreign keys.
- A context never calls a class of another one. It calls an **outbound port** (an interface in its
  own package) and receives through an **inbound adapter**.
- Inside the monolith the ports are implemented with Spring application events
  (`ApplicationEventPublisher` + `@TransactionalEventListener`). The listener runs once the
  publishing transaction commits, in a new transaction of its own (`REQUIRES_NEW`), so a failure
  on one side never rolls back the other.
- The events that cross a boundary live in `shared.integration` (the published language). When a
  context is extracted, the adapter behind the same port becomes an HTTP client and nothing else
  in the caller changes.

## Interactions

| # | From | To | Port (owner) | In-process adapter | Message | After extraction |
|---|---|---|---|---|---|---|
| 1 | Booking & Pricing | Payments & Settlement | `booking.domain.service.PaymentsPort#requestAuthorization` | `booking.infrastructure.integration.InProcessPaymentsAdapter` | `PaymentAuthorizationRequestedEvent` | `POST /api/v1/internal/payments/authorizations` (201/200 authorized, 402/404/503 failed) |
| 2 | Payments & Settlement | Booking & Pricing | `payments.application.PaymentResultsPort` | `payments.infrastructure.integration.InProcessPaymentResultsAdapter` | `PaymentAuthorizationSucceededEvent`, `PaymentAuthorizationFailedEvent` | The REST adapter of #1 turns the HTTP answer into the same saga events; the port is no longer needed |
| 3 | Booking & Pricing | Payments & Settlement | `booking.domain.service.PaymentsPort#notifyCancellation` | `booking.infrastructure.integration.InProcessPaymentsAdapter` | `BookingCancelledEvent` | `POST /api/v1/internal/events/booking-cancelled` |
| 4 | Trip Execution (C#, external) | Payments & Settlement | — | — | HTTP | Unchanged: `POST /api/v1/internal/events/trip-completed` with `X-Internal-Api-Key` |
| 5 | Driver Management | every context | — | `shared.security` | JWT (subject and `userId` = user UUID, `role` and `email` claims) | Every service validates the same HS256 token with `JWT_SECRET`; Driver Management keeps issuing it |
| 6 | Driver Management | every context | `shared.security.AuthenticatedUserResolver` | `drivermanagement.infrastructure.security.CurrentUserResolver` | In-process call | `shared.security.DriverManagementUserResolver`: `GET /api/v1/users/me` with the caller's token (`DRIVER_MANAGEMENT_URL`); the token claims when the service does not answer |

### 1-3. Booking saga and payments

```text
POST /api/v1/bookings
  BookingTransactionalService.createOnce            (bp transaction)
    -> SeatHeld, PaymentAuthorizationRequested       (Booking in-memory bus)
    -> PaymentsOutboundHandler -> PaymentsPort.requestAuthorization(estimated fare)
  commit bp
    -> BookingEventsListener (payments)              (new ps transaction)
         first active card of the passenger -> PaymentAuthorizationService.authorize
         -> PaymentResultsPort.authorized | rejected
       commit ps
         -> PaymentResultsListener (booking)          (new bp transaction)
              PaymentAuthorized          -> saga WAIT_GROUP
              PaymentAuthorizationFailed -> booking cancelled, seat released, saga compensated
                                            -> BookingCancelled(PAYMENT_FAILED) -> #3

POST /api/v1/bookings/{id}/cancel, saga timeout or payment failure
  BookingCancelled(reason) -> PaymentsOutboundHandler -> PaymentsPort.notifyCancellation
  commit bp
    -> BookingEventsListener.onBookingCancelled      (new ps transaction)
         PASSENGER_CANCELLED inside 30 min or NO_SHOW -> penalty charged (CAPTURED)
         anything else                                 -> authorization VOIDED
         no authorized payment                         -> ignored and logged
```

Decisions:

- The standalone services had no message broker: Booking & Pricing published
  `PaymentAuthorizationRequested` on its in-memory bus and nobody consumed it, while Payments &
  Settlement exposed the internal endpoints for a future caller. The monolith wires them together.
- `PaymentAuthorizationRequested` did not carry the amount nor the card. The outbound handler adds
  the estimated fare recorded for the booking, and Payments & Settlement charges the first active
  card of the passenger (the simplest choice; a "default card" does not exist yet).
- `BookingCancelled` gained a `reason` so Payments & Settlement can apply its policy: only
  `PASSENGER_CANCELLED` and `NO_SHOW` can be penalized; platform reasons (`NO_DRIVER_AVAILABLE`,
  `PAYMENT_TIMEOUT`, `PAYMENT_FAILED`) void the authorization.

### 4. Trip Execution

Trip Execution is a separate C# service and is not part of the monolith. It reports completed trips
to Payments & Settlement through the internal endpoint, which the monolith keeps with the same
contract. The API gateway routes `/api/v1/trips` straight to it (`/api/viajes`).

Its events for Booking & Pricing (`BoardingValidated`, `TripStarted`, `TripCompleted`) and the trip
group events of Driver Operations & Routing (`TripGroupOpened`, `GroupCompleted`,
`ProposalAccepted`, `RouteUpdated`) still have no transport: as in the standalone service, they
can only be simulated with `POST /dev/events` under the `dev` profile.

### Events that stay inside one context

| Context | Events | Consumers today |
|---|---|---|
| Driver Management | `DriverVerified`, `DocumentRejected` | Logged only |
| Payments & Settlement | `PaymentAuthorized`, `PaymentRejected`, `PaymentCaptured`, `PaymentVoided`, `PenaltyApplied`, `DriverSettlementCreated` | Logged only |
| Booking & Pricing | `SeatHeld`, `BookingConfirmed`, `FinalFareCalculated` | None (BookingConfirmed carries the boarding codes for a future notification service) |

## Extraction checklist (phase C)

1. Add a REST client adapter behind the outbound port of the caller (for Payments & Settlement:
   `PaymentsPort` calling the internal endpoints with `X-Internal-Api-Key`).
2. Remove the module from the monolith. Its tables stay in the shared database until the new
   service owns its own schema.
3. Point the gateway prefixes of the context to the new service with `UPSTREAM_<CONTEXT>`.
