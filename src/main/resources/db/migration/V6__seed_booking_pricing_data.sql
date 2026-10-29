-- Development data taken from V2 of shareway-booking-pricing.

-- A trip group with capacity 4 and 4 free seats.
INSERT INTO bp.seat_inventories (group_id, capacity, available_seats, version, updated_at)
VALUES ('11111111-1111-1111-1111-111111111111', 4, 4, 0, CURRENT_TIMESTAMP);

-- The fare rule in force: version 1, base 5.00, 1.20 per km, 10 % platform fee.
INSERT INTO bp.fare_rules
    (rule_id, version, base_amount, amount_per_km, platform_fee_pct, valid_from, valid_to)
VALUES ('0b6a7f3e-1c2d-4e5f-8a9b-0c1d2e3f4a5b', 1, 5.00, 1.20, 10.00, DATE '2020-01-01',
        DATE '2099-12-31');
