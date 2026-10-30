-- One schema per bounded context. Each context owns the tables of its schema and only reaches
-- the data of another context through that context's API, never through a join or a foreign key.
CREATE SCHEMA IF NOT EXISTS dm;  -- Driver Management
CREATE SCHEMA IF NOT EXISTS tpm; -- Trip Planning & Matching
CREATE SCHEMA IF NOT EXISTS bp;  -- Booking & Pricing
CREATE SCHEMA IF NOT EXISTS ps;  -- Payments & Settlement
