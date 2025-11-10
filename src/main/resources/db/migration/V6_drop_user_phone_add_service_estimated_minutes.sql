-- V6: drop plain phone column and add estimated duration for services

ALTER TABLE app.users
    DROP COLUMN IF EXISTS phone;

ALTER TABLE app.services
    ADD COLUMN IF NOT EXISTS estimated_minutes INTEGER NOT NULL DEFAULT 60;

UPDATE app.services SET estimated_minutes = 60 WHERE estimated_minutes IS NULL;

ALTER TABLE app.services
    ALTER COLUMN estimated_minutes DROP DEFAULT;

COMMENT ON COLUMN app.services.estimated_minutes IS 'Estimated duration in minutes for completing the service.';
