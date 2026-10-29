-- Booking & Pricing, booking side: seat inventory per trip group, bookings and their saga.
-- Taken from V1 of shareway-booking-pricing, now in the bp schema. group_id and passenger_id are
-- UUIDs owned by other bounded contexts.

CREATE TABLE bp.seat_inventories (
    group_id        UUID PRIMARY KEY,
    capacity        INT       NOT NULL,
    available_seats INT       NOT NULL,
    version         BIGINT    NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT seat_inventories_available_seats_check CHECK (available_seats BETWEEN 0 AND capacity)
);

CREATE TABLE bp.bookings (
    booking_id      UUID PRIMARY KEY,
    group_id        UUID         NOT NULL REFERENCES bp.seat_inventories (group_id),
    request_id      UUID         NOT NULL UNIQUE,
    passenger_id    UUID         NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    pin_hash        VARCHAR(255),
    qr_token_hash   VARCHAR(255),
    idempotency_key VARCHAR(100) UNIQUE,
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT bookings_status_check CHECK (
        status IN ('requested', 'grouped', 'confirmed', 'boarded', 'in_progress', 'completed', 'cancelled')
    )
);

CREATE INDEX idx_bookings_group_id ON bp.bookings (group_id);

CREATE TABLE bp.booking_sagas (
    saga_id      UUID PRIMARY KEY,
    booking_id   UUID        NOT NULL UNIQUE REFERENCES bp.bookings (booking_id),
    current_step VARCHAR(60) NOT NULL,
    status       VARCHAR(20) NOT NULL,
    deadline_at  TIMESTAMP,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT booking_sagas_status_check CHECK (status IN ('running', 'completed', 'compensated'))
);
