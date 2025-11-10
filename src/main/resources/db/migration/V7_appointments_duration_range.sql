-- V7: support duration-based appointment holds and prevent overlapping slots

CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE app.appointments
    ADD COLUMN IF NOT EXISTS duration_minutes INTEGER,
    ADD COLUMN IF NOT EXISTS end_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS appt_range tstzrange;

WITH item_minutes AS (
    SELECT ai.appointment_id,
           SUM(si.estimated_minutes * ai.qty)::INTEGER AS total_minutes
    FROM app.appointment_items ai
    JOIN app.services si ON si.id = ai.service_id
    GROUP BY ai.appointment_id
)
UPDATE app.appointments a
SET duration_minutes = item_minutes.total_minutes
FROM item_minutes
WHERE a.id = item_minutes.appointment_id;

UPDATE app.appointments
SET duration_minutes = 60
WHERE duration_minutes IS NULL OR duration_minutes <= 0;

ALTER TABLE app.appointments
    ALTER COLUMN duration_minutes SET DEFAULT 60,
    ALTER COLUMN duration_minutes SET NOT NULL;

UPDATE app.appointments
SET end_at = appointment_at + (duration_minutes * INTERVAL '1 minute')
WHERE end_at IS NULL;

ALTER TABLE app.appointments
    ALTER COLUMN end_at SET NOT NULL;

UPDATE app.appointments
SET appt_range = tstzrange(appointment_at, end_at, '[)')
WHERE appt_range IS NULL;

ALTER TABLE app.appointments
    ALTER COLUMN appt_range SET NOT NULL;

DROP INDEX IF EXISTS app.ux_appt_user_slot;
DROP INDEX IF EXISTS ux_appt_user_slot;

CREATE OR REPLACE FUNCTION app.calc_appointment_range()
RETURNS TRIGGER AS $$
BEGIN
  NEW.end_at := NEW.appointment_at + (NEW.duration_minutes * INTERVAL '1 minute');
  NEW.appt_range := tstzrange(NEW.appointment_at, NEW.end_at, '[)');
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_calc_appt_range ON app.appointments;
CREATE TRIGGER trg_calc_appt_range
  BEFORE INSERT OR UPDATE OF appointment_at, duration_minutes
  ON app.appointments
  FOR EACH ROW
  EXECUTE FUNCTION app.calc_appointment_range();

CREATE INDEX IF NOT EXISTS idx_appt_range_gist
  ON app.appointments USING gist (appt_range);

ALTER TABLE app.appointments
    DROP CONSTRAINT IF EXISTS no_overlapping_appointments;

ALTER TABLE app.appointments
    ADD CONSTRAINT no_overlapping_appointments
    EXCLUDE USING gist (
        user_id WITH =,
        appt_range WITH &&
    )
    WHERE (deleted_at IS NULL AND status NOT IN ('CANCELLED','REFUNDED'));

COMMENT ON COLUMN app.appointments.duration_minutes IS 'Accumulated estimated minutes for all services in the appointment.';
COMMENT ON COLUMN app.appointments.end_at IS 'Calculated end timestamp (appointment_at + duration).';
COMMENT ON COLUMN app.appointments.appt_range IS 'Range column used to detect overlapping appointments.';
