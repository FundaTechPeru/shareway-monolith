-- Payments & Settlement. Derived from the JPA entities: the standalone service relied on
-- ddl-auto=update. passenger_id, driver_id, booking_id and trip_id are UUIDs owned by other
-- bounded contexts, so they have no foreign keys.

CREATE TABLE ps.payment_methods (
    id           UUID PRIMARY KEY,
    passenger_id UUID         NOT NULL,
    card_token   VARCHAR(100) NOT NULL,
    brand        VARCHAR(20)  NOT NULL,
    last4        VARCHAR(4)   NOT NULL,
    exp_month    INTEGER      NOT NULL,
    exp_year     INTEGER      NOT NULL,
    active       BOOLEAN      NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_payment_methods_passenger_id ON ps.payment_methods (passenger_id);

CREATE TABLE ps.payments (
    id                  UUID PRIMARY KEY,
    booking_id          UUID           NOT NULL,
    passenger_id        UUID           NOT NULL,
    payment_method_id   UUID           NOT NULL,
    authorized_amount   NUMERIC(12, 2) NOT NULL,
    currency            VARCHAR(3)     NOT NULL,
    captured_amount     NUMERIC(12, 2),
    captured_currency   VARCHAR(3),
    status              VARCHAR(20)    NOT NULL,
    provider_ref        VARCHAR(100),
    cancellation_reason VARCHAR(30),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    authorized_at       TIMESTAMP WITH TIME ZONE,
    captured_at         TIMESTAMP WITH TIME ZONE,
    voided_at           TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_payments_booking_id UNIQUE (booking_id)
);

CREATE INDEX idx_payments_passenger_id ON ps.payments (passenger_id);

-- Penalty actually charged to the passenger. Booking & Pricing keeps its own record of late
-- cancellations in bp.penalties; see "Known issues" in the README.
CREATE TABLE ps.penalties (
    id           UUID PRIMARY KEY,
    payment_id   UUID           NOT NULL,
    booking_id   UUID           NOT NULL,
    passenger_id UUID           NOT NULL,
    reason       VARCHAR(30)    NOT NULL,
    amount       NUMERIC(12, 2) NOT NULL,
    currency     VARCHAR(3)     NOT NULL,
    applied_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_penalties_booking_id UNIQUE (booking_id)
);

CREATE TABLE ps.driver_settlements (
    id                  UUID PRIMARY KEY,
    trip_id             UUID           NOT NULL,
    driver_id           UUID           NOT NULL,
    gross_amount        NUMERIC(12, 2) NOT NULL,
    currency            VARCHAR(3)     NOT NULL,
    commission_amount   NUMERIC(12, 2) NOT NULL,
    commission_currency VARCHAR(3)     NOT NULL,
    net_amount          NUMERIC(12, 2) NOT NULL,
    net_currency        VARCHAR(3)     NOT NULL,
    status              VARCHAR(20)    NOT NULL,
    trip_completed_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    paid_at             TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_driver_settlements_trip_id UNIQUE (trip_id)
);

CREATE INDEX idx_driver_settlements_driver_id ON ps.driver_settlements (driver_id);
