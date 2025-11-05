-- English: Trigger to auto-update 'updated_at' on UPDATE for multiple tables.
SET search_path TO app, public;

CREATE OR REPLACE FUNCTION app.touch_updated_at() RETURNS trigger AS $$
BEGIN
  NEW.updated_at := now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- English: attach trigger to tables that have updated_at
DO $$
DECLARE
  t TEXT;
BEGIN
  FOR t IN SELECT tablename
           FROM pg_tables
           WHERE schemaname = 'app'
             AND tablename IN ('users','products','product_images','services','service_images',
                               'shop_orders','shop_order_items','appointments','appointment_items',
                               'savings_cards','consumptions')
  LOOP
    EXECUTE format('
      DO $do$
      BEGIN
        IF NOT EXISTS (
          SELECT 1 FROM pg_trigger
          WHERE tgname = %L AND tgrelid = %L::regclass
        ) THEN
          CREATE TRIGGER %I
          BEFORE UPDATE ON %I.%I
          FOR EACH ROW
          EXECUTE FUNCTION app.touch_updated_at();
        END IF;
      END
      $do$;', 'trg_touch_'||t, 'app.'||t, 'trg_touch_'||t, 'app', t);
  END LOOP;
END;
$$;
  