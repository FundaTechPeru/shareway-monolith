-- Booking & Pricing, pricing side: fare rules, the fares of each booking and the penalties of
-- late cancellations. Taken from V1 of shareway-booking-pricing, now in the bp schema; the fare
-- rule id becomes a UUID like every other id of the monolith.

CREATE TABLE bp.fare_rules (
    rule_id          UUID PRIMARY KEY,
    version          INT            NOT NULL UNIQUE,
    base_amount      NUMERIC(10, 2) NOT NULL,
    amount_per_km    NUMERIC(10, 2) NOT NULL,
    platform_fee_pct NUMERIC(5, 2)  NOT NULL,
    valid_from       DATE           NOT NULL,
    valid_to         DATE           NOT NULL,
    CONSTRAINT fare_rules_amounts_check CHECK (base_amount >= 0 AND amount_per_km >= 0),
    CONSTRAINT fare_rules_platform_fee_pct_check CHECK (platform_fee_pct BETWEEN 0 AND 100)
);

CREATE TABLE bp.fares (
    fare_id            UUID PRIMARY KEY,
    booking_id         UUID           NOT NULL REFERENCES bp.bookings (booking_id),
    rule_id            UUID           NOT NULL REFERENCES bp.fare_rules (rule_id),
    fare_type          VARCHAR(20)    NOT NULL,
    distance_km        NUMERIC(10, 2) NOT NULL,
    grouped_passengers INT            NOT NULL,
    amount             NUMERIC(10, 2) NOT NULL,
    currency           VARCHAR(3)     NOT NULL,
    CONSTRAINT fares_booking_fare_type_unique UNIQUE (booking_id, fare_type),
    CONSTRAINT fares_fare_type_check CHECK (fare_type IN ('estimated', 'final')),
    CONSTRAINT fares_amount_check CHECK (amount >= 0)
);

-- Penalty recorded by Booking & Pricing for a late cancellation. Payments & Settlement charges
-- its own penalty in ps.penalties; see "Known issues" in the README.
CREATE TABLE bp.penalties (
    penalty_id UUID PRIMARY KEY,
    booking_id UUID           NOT NULL UNIQUE REFERENCES bp.bookings (booking_id),
    amount     NUMERIC(10, 2) NOT NULL,
    reason     VARCHAR(50)    NOT NULL,
    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT penalties_amount_check CHECK (amount > 0)
);
