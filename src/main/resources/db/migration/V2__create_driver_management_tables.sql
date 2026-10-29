-- Driver Management. Derived from the JPA entities: the standalone service relied on
-- ddl-auto=update, here Flyway owns the schema and Hibernate only validates it.
-- driver_id columns keep the user UUID without a foreign key, as the entities map them.

CREATE TABLE dm.emergency_contacts (
    id           UUID PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    phone        VARCHAR(20)  NOT NULL,
    relationship VARCHAR(50)  NOT NULL
);

CREATE TABLE dm.users (
    id                   UUID PRIMARY KEY,
    email                VARCHAR(120) NOT NULL,
    password_hash        VARCHAR(255) NOT NULL,
    full_name            VARCHAR(100) NOT NULL,
    phone                VARCHAR(20)  NOT NULL,
    role                 VARCHAR(20)  NOT NULL,
    license_number       VARCHAR(12),
    driver_status        VARCHAR(30),
    emergency_contact_id UUID,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_license_number UNIQUE (license_number),
    CONSTRAINT uk_users_emergency_contact UNIQUE (emergency_contact_id),
    CONSTRAINT fk_users_emergency_contact
        FOREIGN KEY (emergency_contact_id) REFERENCES dm.emergency_contacts (id)
);

CREATE TABLE dm.driver_documents (
    id                UUID PRIMARY KEY,
    driver_id         UUID         NOT NULL,
    document_type     VARCHAR(30)  NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type      VARCHAR(50)  NOT NULL,
    size_bytes        BIGINT       NOT NULL,
    storage_key       VARCHAR(100) NOT NULL,
    rejection_reason  VARCHAR(500),
    reviewed_by       UUID,
    uploaded_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    reviewed_at       TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_driver_documents_storage_key UNIQUE (storage_key)
);

CREATE INDEX idx_driver_documents_driver_id ON dm.driver_documents (driver_id);

CREATE TABLE dm.vehicles (
    id               UUID PRIMARY KEY,
    driver_id        UUID        NOT NULL,
    plate            VARCHAR(7)  NOT NULL,
    brand            VARCHAR(50) NOT NULL,
    model            VARCHAR(50) NOT NULL,
    manufacture_year INTEGER     NOT NULL,
    color            VARCHAR(30) NOT NULL,
    seat_count       INTEGER     NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_vehicles_plate UNIQUE (plate)
);

CREATE INDEX idx_vehicles_driver_id ON dm.vehicles (driver_id);

-- Weekly availability of a driver (US-19). Trip Planning keeps its own dated availability in
-- tpm.driver_availabilities; see "Known issues" in the README.
CREATE TABLE dm.driver_availabilities (
    id          UUID PRIMARY KEY,
    driver_id   UUID        NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    start_time  TIME        NOT NULL,
    end_time    TIME        NOT NULL
);

CREATE INDEX idx_driver_availabilities_driver_id ON dm.driver_availabilities (driver_id);
