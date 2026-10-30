-- Trip Planning & Matching. Taken from V1 of shareway-trip-planning-matching-service, now in the
-- tpm schema and with UUID ids. passenger_id and driver_id are UUIDs owned by Driver Management.

CREATE TABLE tpm.trip_requests (
    id                 UUID PRIMARY KEY,
    passenger_id       UUID          NOT NULL,
    origin_label       VARCHAR(160)  NOT NULL,
    destination_label  VARCHAR(160)  NOT NULL,
    operation_zone     VARCHAR(120)  NOT NULL,
    origin_lat         NUMERIC(9, 6) NOT NULL,
    origin_lng         NUMERIC(9, 6) NOT NULL,
    destination_lat    NUMERIC(9, 6) NOT NULL,
    destination_lng    NUMERIC(9, 6) NOT NULL,
    travel_date        DATE          NOT NULL,
    earliest_departure TIME          NOT NULL,
    latest_departure   TIME          NOT NULL,
    seats_requested    INTEGER       NOT NULL,
    status             VARCHAR(20)   NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_trip_requests_zone_date ON tpm.trip_requests (operation_zone, travel_date);

-- Dated availability used for matching. Driver Management keeps a weekly schedule in
-- dm.driver_availabilities; see "Known issues" in the README.
CREATE TABLE tpm.driver_availabilities (
    id              UUID PRIMARY KEY,
    driver_id       UUID         NOT NULL,
    operation_zone  VARCHAR(120) NOT NULL,
    available_date  DATE         NOT NULL,
    start_time      TIME         NOT NULL,
    end_time        TIME         NOT NULL,
    available_seats INTEGER      NOT NULL
);

CREATE INDEX idx_driver_availabilities_matching
    ON tpm.driver_availabilities (operation_zone, available_date, start_time, end_time, available_seats);

CREATE TABLE tpm.match_proposals (
    id                         UUID PRIMARY KEY,
    trip_request_id            UUID          NOT NULL REFERENCES tpm.trip_requests (id),
    driver_id                  UUID          NOT NULL,
    estimated_fare             NUMERIC(8, 2) NOT NULL,
    estimated_duration_minutes INTEGER       NOT NULL,
    status                     VARCHAR(20)   NOT NULL,
    created_at                 TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_match_proposals_trip_request
    ON tpm.match_proposals (trip_request_id, created_at DESC);
