# ShareWay Monolith

Modular monolith of **ShareWay**, the university platform for scheduled shared rides with verified
drivers (UPC, SI657 Fundamentos de Arquitectura de Software). It joins four services that were
built separately into one Spring Boot application, and it is the "before" of a Strangler Fig
refactoring: first the monolith, then the [API gateway](https://github.com/FundaTechPeru/shareway-api-gateway)
in front of it, then each bounded context is extracted back to its own service.

## Bounded contexts

| Context | Package | Schema | OpenAPI group | Paths | Imported from |
|---|---|---|---|---|---|
| Trip Planning & Matching | `tripplanning` | `tpm` | `tripplanning` | `/api/v1/trip-requests`, `/api/v1/driver-availabilities`, `/api/v1/info` | `shareway-trip-planning-matching-service` |
| Booking & Pricing | `booking` (with `booking.pricing`) | `bp` | `booking` | `/api/v1/bookings`, `/api/v1/fares`, `/api/v1/fare-rules` | `shareway-booking-pricing` |
| Payments & Settlement | `payments` | `ps` | `payments` | `/api/v1/payments`, `/api/v1/payment-methods`, `/api/v1/settlements`, `/api/v1/admin/settlements`, `/api/v1/internal/**` | `shareway-payments-settlement-service` |

Trip Execution (`shareway-trip-execution-service`, C#) is **not** part of the monolith: the gateway
routes `/api/v1/trips` straight to it.

### Extracted contexts

| Context | Service | Paths now served by the service | Monolith side |
|---|---|---|---|
| Driver Management | [`shareway-driver-management-service`](https://github.com/FundaTechPeru/shareway-driver-management-service) | `/api/v1/auth`, `/api/v1/users`, `/api/v1/drivers`, `/api/v1/driver-documents` | Validates its tokens with `JWT_SECRET`; asks it the current role of each user when `DRIVER_MANAGEMENT_URL` is set |

Its tables (`dm` schema) stay in the database, untouched, until the service owns its own database.

## Architecture

```text
com.fundatechperu.shareway
├── shared                 shared kernel
│   ├── security           the only SecurityFilterChain, JWT HS256, internal API key
│   ├── web                uniform error body and global exception handler
│   ├── exception          not found (404), conflict (409), domain validation (400)
│   ├── openapi            global info, bearer scheme and one group per context
│   └── integration        events that cross a context boundary (published language)
├── tripplanning           domain · application · infrastructure · interfaces
├── booking                domain · application · infrastructure · interfaces · pricing
└── payments               domain · application · infrastructure · interfaces
```

- **Layers.** Every context keeps the layers it had as a service: `domain` (model, events and the
  ports it needs), `application` (use cases), `infrastructure` (adapters: JPA, storage, gateway,
  messaging) and `interfaces` (REST controllers, in-process listeners).
- **One database, one schema per context.** PostgreSQL with the `tpm`, `bp` and `ps` schemas (plus
  the `dm` schema left behind by Driver Management).
  Flyway is the only owner of the schema (`ddl-auto=validate`): `V1` creates the schemas and each
  context adds its own scripts. Tables never reference another schema.
- **Identifiers.** Every id is a UUID, including the references between contexts (`driverId`,
  `passengerId`, `bookingId`, `tripId`…) and the JWT subject.
- **Security.** One filter chain in `shared.security`. The extracted Driver Management service
  issues the tokens at login; every request is authenticated by the same filter. Each context contributes its role rules with
  a `SecurityRules` bean. Service-to-service endpoints under `/api/v1/internal/**` take the
  `X-Internal-Api-Key` header instead of a token.
- **Integration.** Contexts never call each other's classes. They use ports implemented with Spring
  events delivered after commit, ready to be replaced by HTTP clients when a context is extracted:
  see [docs/integration-map.md](docs/integration-map.md).

## Tech stack

Java 25 · Spring Boot 4.1 (Spring Framework 7, Spring Security 7, Hibernate 7, Jackson 3) ·
PostgreSQL 17 · Flyway · springdoc-openapi 3.1.1 · jjwt 0.12.6 · JUnit 5, Mockito, Cucumber 7.34.6
and H2 for the tests.

## Running locally

Prerequisites: JDK 25 and Docker.

```bash
cp .env.example .env            # then set DB_PASSWORD, JWT_SECRET and INTERNAL_API_KEY
docker compose up --build       # PostgreSQL + the monolith on http://localhost:8080
```

To run the application from the IDE or the wrapper, start only the database and pass the
variables of `.env` to the process:

```bash
docker compose up -d postgres
DB_URL=jdbc:postgresql://localhost:5432/shareway DB_PASSWORD=... JWT_SECRET=... \
INTERNAL_API_KEY=... ./mvnw spring-boot:run
```

The `dev` profile (`SPRING_PROFILES_ACTIVE=dev`) adds `POST /dev/events`, which simulates the
events that Booking & Pricing consumes from contexts outside the monolith.

- Swagger UI: http://localhost:8080/swagger-ui.html (choose the context in the selector, log in
  with `POST /api/v1/auth/login` of Driver Management, through the gateway, and paste the
  `accessToken` in **Authorize**)
- OpenAPI documents: `/v3/api-docs/tripplanning`, `/v3/api-docs/booking`, `/v3/api-docs/payments`
- Health: http://localhost:8080/actuator/health

### Configuration

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8080` | HTTP port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/shareway` | JDBC URL |
| `DB_USER` / `DB_PASSWORD` | `shareway` / `shareway` | Database credentials (development defaults) |
| `JWT_SECRET` | none, required | HS256 secret of at least 32 bytes, shared with the gateway |
| `JWT_EXPIRATION_MINUTES` | `60` | Lifetime of the access tokens |
| `INTERNAL_API_KEY` | none, required | Key of `/api/v1/internal/**` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200,http://localhost:5173,http://localhost:3000` | Allowed browser origins |
| `COMMISSION_RATE` | `0.15` | Platform commission over the gross amount of a trip |
| `PENALTY_RATE` / `PENALTY_WINDOW_MINUTES` | `0.50` / `30` | Penalty charged by Payments & Settlement |
| `BOOKING_*` | see `application.yaml` | Booking saga timeouts, retries, simulated departure and Booking & Pricing penalty |
| `DRIVER_MANAGEMENT_URL` | empty | Base URL of the extracted Driver Management service; when set, the current role of each user is asked to it, otherwise the token claims are used as they are |
| `PAYMENTS_SETTLEMENT_URL` | empty | Base URL of the extracted Payments & Settlement service; when set, Booking & Pricing asks it for payment authorizations and reports cancellations over HTTP |
| `PAYMENTS_SETTLEMENT_API_KEY` | `INTERNAL_API_KEY` | `X-Internal-Api-Key` of the internal endpoints of Payments & Settlement |
| `SERVICES_TIMEOUT_MS` | `3000` | Connect and read timeout of every call to an extracted service |

## Tests

```bash
./mvnw verify
```

The tests need no database server: they use H2 in PostgreSQL mode, and Flyway creates the four
schemas as in production. They include the Cucumber acceptance suite of Payments & Settlement
(`features/payments`), the unit and
integration tests of Booking & Pricing (including its concurrency checks) and Trip Planning,
the in-process integration between booking and payments, and a check that every OpenAPI
operation is documented with its responses and examples.

## Decisions taken while merging

Where the plan left room, the simplest option was taken:

- **Booking passenger.** Booking & Pricing trusted an `X-User-Id` header set by a gateway. The
  passenger is now the subject of the JWT, like in the other contexts.
- **Fare rule ids** became UUIDs as well, so no entity keeps a numeric id.
- **Role changes.** The monolith asks Driver Management for the current user on every request
  (`DriverManagementUserResolver`), so a passenger who registers as a driver does not need to log
  in again. Without `DRIVER_MANAGEMENT_URL`, or when the service does not answer, the claims of the
  token are used.
- **Errors.** All contexts answer with the Driver Management error body (`timestamp`, `status`,
  `error`, `message`, `path`, `fieldErrors`); Booking & Pricing used a smaller one.
- **Trip Planning** had no authentication: its endpoints now require a token, but the
  `passengerId` and `driverId` still come in the body as in the original API. Its service
  description moved from `/` and `/api` to `/api/v1/info`. Its application service takes commands
  instead of the web DTOs.
- **In-process payments.** The authorization request did not name a card: Payments & Settlement
  uses the first active card of the passenger. `BookingCancelled` now carries its reason.
- **Imports.** The domain entities of Driver Management, Payments & Settlement and Trip Planning
  are also their JPA entities, so the "domain model" commits bring the repository adapters and the
  Flyway scripts along to keep the application bootable.
- **Configuration keys.** Booking & Pricing keeps its `booking.*` keys; the others live under
  `app.*`.
- **Actuator.** Only `/actuator/health` (with its probes) is public; `/actuator/info` needs a token.
- **Comments.** Payments & Settlement keeps its Spanish Javadoc as imported; identifiers are in
  English everywhere.

## Known issues

- **DriverAvailability exists twice (US-19).** Driver Management keeps the weekly schedule of a
  driver (its own service, `/api/v1/drivers/me/availability`) and Trip Planning keeps
  dated availability for matching (`tpm.driver_availabilities`, `/api/v1/driver-availabilities`).
  Both are kept on purpose: the schemas avoid the table collision and the Trip Planning entity has
  its own JPA name (`TripPlanningDriverAvailability`). Matching does not read the weekly schedule
  yet.
- **Two penalty policies.** Booking & Pricing records a late-cancellation penalty of 20 % of the
  estimated fare (`bp.penalties`, minimum 1.00) and Payments & Settlement charges 50 % of the
  authorized amount (`ps.penalties`). The schemas separate the tables, but only the second one is
  actually charged; the policies still have to be reconciled.
- **Events without transport.** The trip group events (`TripGroupOpened`, `GroupCompleted`,
  `ProposalAccepted`, `RouteUpdated`) and the Trip Execution events (`BoardingValidated`,
  `TripStarted`, `TripCompleted`) that move the booking saga have no producer in the monolith; they
  can only be simulated with `POST /dev/events` under the `dev` profile.
- **Simulated data.** Booking & Pricing quotes every group with a fake 12 km distance and assumes
  the departure 120 minutes ahead (`BOOKING_DEPARTURE_SIMULATED_OFFSET_MINUTES`).
- Any authenticated user can read a booking by id; only its passenger can cancel it.
- `/api/v1/info` is not in the gateway route table, so it is only reachable on the monolith.
- The Docker image is built by the CI workflow; it was not built on the development machine.

## Contributing

GitFlow: `main`, `develop`, `feature/*` merged into `develop` with `--no-ff`, `release/*` and
SemVer tags. Commits follow [Conventional Commits](https://www.conventionalcommits.org/) in English
(`type(scope): subject`, at most 72 characters). Java code follows the Google Java Style.
