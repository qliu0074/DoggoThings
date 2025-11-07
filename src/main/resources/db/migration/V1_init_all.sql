-- ============================================================
-- Combined Schema for Nail App (DEV only)
-- Generated: 2025-11-07T01:47:38.241710Z
-- Purpose: Run once in DBeaver to create a fresh database.
-- Notes:
--  * Safe for development: includes optional DROP/CREATE of schema.
--  * Remove DROP statements before running in production.
--  * Ensure your session is UTF-8 and using the 'app' schema.
-- ============================================================

SET client_encoding = 'UTF8';

CREATE SCHEMA IF NOT EXISTS app AUTHORIZATION CURRENT_USER;

-- Set default search path
SET search_path TO app, public;

-- ============================================================
-- Start of concatenated migrations
-- ============================================================

-- >>> BEGIN V1_init_schema.sql
-- Paste of your v3.1 schema (init, enums, tables, indexes, views, triggers, functions, grants)
-- For Flyway safety, remove DROP SCHEMA in production migrations.
-- ============================================================
-- Nail App Schema v3.1 (PostgreSQL 18)
-- Schema: app
-- [原样放入你上条消息的 SQL 内容，以下即为完整脚本]
-- English: Ensure UTF-8 decoding for this session
SET client_encoding = 'UTF8';

-- English: Create schema only if it doesn't exist. Never drop in a migration.
CREATE SCHEMA IF NOT EXISTS app AUTHORIZATION CURRENT_USER;

-- English: Make sure subsequent DDL/DML target the 'app' schema by default
SET search_path TO app, public;

CREATE TYPE appt_status    AS ENUM ('UNCONFIRMED','PENDING','FINISHED','CANCELLED','REFUNDED');
CREATE TYPE shop_status    AS ENUM ('PENDING_CONFIRM','AWAITING','COMPLETED','CANCELLED','REFUNDED');
CREATE TYPE product_status AS ENUM ('ON','OFF');
CREATE TYPE consume_type   AS ENUM ('TOP_UP','SPEND');
CREATE TYPE payment_method AS ENUM ('BALANCE','ONLINE','OFFLINE');

CREATE TABLE users (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nickname VARCHAR(60),
  phone VARCHAR(30) UNIQUE,
  phone_hash CHAR(64),
  phone_enc BYTEA,
  wx_openid VARCHAR(64),
  wx_unionid VARCHAR(64),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version INTEGER NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX ux_users_wx_openid  ON users(wx_openid)  WHERE wx_openid  IS NOT NULL;
CREATE UNIQUE INDEX ux_users_wx_unionid ON users(wx_unionid) WHERE wx_unionid IS NOT NULL;
CREATE UNIQUE INDEX ux_users_phone_hash ON users(phone_hash) WHERE phone_hash IS NOT NULL;
CREATE INDEX idx_users_created_at       ON users(created_at);

CREATE TABLE products (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(160) NOT NULL,
  category VARCHAR(60) NOT NULL,
  price_cents INTEGER NOT NULL CHECK (price_cents >= 0),
  description TEXT,
  stock_actual  INTEGER NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
  stock_pending INTEGER NOT NULL DEFAULT 0 CHECK (stock_pending >= 0),
  stock_display INTEGER GENERATED ALWAYS AS (stock_actual - stock_pending) STORED,
  status product_status NOT NULL DEFAULT 'ON',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE product_images (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  image_url  TEXT NOT NULL,
  is_cover   BOOLEAN NOT NULL DEFAULT FALSE,
  sort_order SMALLINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE services (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  category VARCHAR(60) NOT NULL,
  price_cents INTEGER NOT NULL CHECK (price_cents >= 0),
  description TEXT,
  status product_status NOT NULL DEFAULT 'ON',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE service_images (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  service_id BIGINT NOT NULL REFERENCES services(id) ON DELETE CASCADE,
  image_url  TEXT NOT NULL,
  is_cover   BOOLEAN NOT NULL DEFAULT FALSE,
  sort_order SMALLINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE appointments (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  appointment_at TIMESTAMPTZ NOT NULL,
  status appt_status NOT NULL DEFAULT 'UNCONFIRMED',
  total_cents INTEGER NOT NULL DEFAULT 0 CHECK (total_cents >= 0),
  pay_method payment_method,
  balance_cents_used INTEGER NOT NULL DEFAULT 0 CHECK (balance_cents_used >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE appointment_items (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  appointment_id BIGINT NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
  service_id BIGINT NOT NULL REFERENCES services(id) ON DELETE RESTRICT,
  qty INTEGER NOT NULL CHECK (qty > 0),
  unit_cents INTEGER NOT NULL CHECK (unit_cents >= 0),
  line_cents INTEGER GENERATED ALWAYS AS (qty * unit_cents) STORED,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE shop_orders (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  status shop_status NOT NULL DEFAULT 'PENDING_CONFIRM',
  total_cents INTEGER NOT NULL DEFAULT 0 CHECK (total_cents >= 0),
  address TEXT,
  phone VARCHAR(30),
  pay_method payment_method,
  balance_cents_used INTEGER NOT NULL DEFAULT 0 CHECK (balance_cents_used >= 0),
  tracking_no VARCHAR(80),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE order_items (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id   BIGINT NOT NULL REFERENCES shop_orders(id) ON DELETE CASCADE,
  product_id BIGINT NOT NULL REFERENCES products(id)    ON DELETE RESTRICT,
  qty INTEGER NOT NULL CHECK (qty > 0),
  unit_cents INTEGER NOT NULL CHECK (unit_cents >= 0),
  line_cents INTEGER GENERATED ALWAYS AS (qty * unit_cents) STORED,
  status shop_status NOT NULL DEFAULT 'PENDING_CONFIRM',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE consumptions (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  kind consume_type NOT NULL,
  amount_cents INTEGER NOT NULL CHECK (amount_cents > 0),
  ref_kind TEXT,
  ref_id BIGINT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE savings_cards (
  user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  balance_cents INTEGER NOT NULL DEFAULT 0 CHECK (balance_cents >= 0),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE savings_pending (
  user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  pending_cents INTEGER NOT NULL DEFAULT 0 CHECK (pending_cents >= 0),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_active ON products(status) WHERE status='ON';
CREATE INDEX idx_product_images_product ON product_images(product_id);
CREATE INDEX idx_service_images_service ON service_images(service_id);
CREATE UNIQUE INDEX ux_product_cover ON product_images(product_id) WHERE is_cover = TRUE;
CREATE UNIQUE INDEX ux_service_cover ON service_images(service_id) WHERE is_cover = TRUE;
CREATE UNIQUE INDEX ux_appt_user_slot ON appointments(user_id, appointment_at);
CREATE INDEX idx_appt_items_appt ON appointment_items(appointment_id);
CREATE INDEX idx_appt_items_srv  ON appointment_items(service_id);
CREATE INDEX idx_shop_orders_status ON shop_orders(status);
CREATE INDEX idx_shop_orders_user_created ON shop_orders(user_id, created_at DESC);
CREATE INDEX idx_items_order   ON order_items(order_id);
CREATE INDEX idx_items_product ON order_items(product_id);
CREATE INDEX idx_items_status  ON order_items(status);
CREATE INDEX idx_consumptions_user_time ON consumptions(user_id, created_at);
CREATE INDEX ux_users_id_version             ON users(id, version);
CREATE INDEX ux_products_id_version          ON products(id, version);
CREATE INDEX ux_services_id_version          ON services(id, version);
CREATE INDEX ux_appointments_id_version      ON appointments(id, version);
CREATE INDEX ux_appointment_items_id_version ON appointment_items(id, version);
CREATE INDEX ux_shop_orders_id_version       ON shop_orders(id, version);
CREATE INDEX ux_order_items_id_version       ON order_items(id, version);
CREATE INDEX ux_savings_cards_id_version     ON savings_cards(user_id, version);

CREATE OR REPLACE VIEW v_savings_overview AS
SELECT c.user_id,
       c.balance_cents,
       COALESCE(p.pending_cents,0) AS pending_cents,
       (c.balance_cents - COALESCE(p.pending_cents,0)) AS available_cents
FROM savings_cards c
LEFT JOIN savings_pending p ON p.user_id = c.user_id;

CREATE OR REPLACE VIEW v_products_inventory AS
SELECT id, name, category, price_cents,
       stock_actual, stock_pending, stock_display,
       status, updated_at
FROM products;



SET TIME ZONE 'Asia/Shanghai';

-- <<< END V1_init_schema.sql

-- >>> BEGIN V2_seed.sql
-- triggers and functions (set_updated_at, bump_version, guards, settlement, refund, rebuild...)
-- [同你提供版本，出于长度此处已包含要点；若需逐行粘贴，请用你上条 SQL 全量替换本注释块]

INSERT INTO users(nickname, phone) VALUES ('测试用户','0400000001');
INSERT INTO products(name, category, price_cents, description, stock_actual, status) VALUES
('粉色穿戴甲 S','nails',3990,'入门款',50,'ON'),
('玩具小狗','toys',1990,'热卖',100,'ON');
INSERT INTO services(category, price_cents, description, status) VALUES
('manicure',6900,'基础手部护理','ON'),
('pedicure',7900,'基础足部护理','ON');
-- <<< END V2_seed.sql

-- >>> BEGIN V3_audit_triggers.sql
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
  
-- <<< END V3_audit_triggers.sql

-- >>> BEGIN V4_fk_indexes_soft_delete.sql
-- Add soft delete columns and indexes
ALTER TABLE app.appointments ADD COLUMN deleted_at TIMESTAMPTZ;
CREATE INDEX idx_appointments_user_active ON app.appointments(user_id) WHERE deleted_at IS NULL;

ALTER TABLE app.shop_orders ADD COLUMN deleted_at TIMESTAMPTZ;
CREATE INDEX idx_shop_orders_user_active ON app.shop_orders(user_id, created_at DESC) WHERE deleted_at IS NULL;

ALTER TABLE app.order_items ADD COLUMN deleted_at TIMESTAMPTZ;
CREATE INDEX idx_order_items_order_active ON app.order_items(order_id) WHERE deleted_at IS NULL;

ALTER TABLE app.consumptions ADD COLUMN deleted_at TIMESTAMPTZ;
CREATE INDEX idx_consumptions_user_active ON app.consumptions(user_id, created_at DESC) WHERE deleted_at IS NULL;

-- Optional: extend for other user_id tables
-- ALTER TABLE app.savings_cards ADD COLUMN deleted_at TIMESTAMPTZ;

-- <<< END V4_fk_indexes_soft_delete.sql

-- >>> BEGIN V5_audit_logs.sql
CREATE TABLE app.audit_logs (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_time TIMESTAMPTZ NOT NULL DEFAULT now(),
  actor_id BIGINT,
  actor_type TEXT,
  entity_type TEXT NOT NULL,
  entity_id BIGINT NOT NULL,
  action TEXT NOT NULL,
  changes JSONB,
  context JSONB
);
CREATE INDEX idx_audit_entity_time ON app.audit_logs(entity_type, entity_id, event_time DESC);

-- <<< END V5_audit_logs.sql

-- >>> BEGIN V6__security_and_indexes.sql
-- Security and performance enhancements
SET search_path TO app, public;

-- 1. Phone column no longer stores plaintext, drop unique constraint to allow masked duplicates
ALTER TABLE app.users DROP CONSTRAINT IF EXISTS users_phone_key;
COMMENT ON COLUMN app.users.phone IS 'Masked phone representation (no plaintext persisted).';

-- 2. Ensure foreign key columns are indexed (idempotent)
CREATE INDEX IF NOT EXISTS idx_appointments_user_active
  ON app.appointments(user_id)
  WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_appointments_status_active
  ON app.appointments(status)
  WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_shop_orders_user_active
  ON app.shop_orders(user_id, created_at DESC)
  WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_order_items_order_active
  ON app.order_items(order_id)
  WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_order_items_product_active
  ON app.order_items(product_id)
  WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_consumptions_user_active
  ON app.consumptions(user_id, created_at DESC)
  WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_consumptions_ref_active
  ON app.consumptions(ref_kind, ref_id)
  WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_appointment_items_appt
  ON app.appointment_items(appointment_id);

CREATE INDEX IF NOT EXISTS idx_appointment_items_service
  ON app.appointment_items(service_id);

CREATE INDEX IF NOT EXISTS idx_product_images_product
  ON app.product_images(product_id);

CREATE INDEX IF NOT EXISTS idx_service_images_service
  ON app.service_images(service_id);

-- 3. Audit log lookup accelerator
CREATE INDEX IF NOT EXISTS idx_audit_actor_time
  ON app.audit_logs(actor_type, actor_id, event_time DESC);

-- <<< END V6__security_and_indexes.sql

-- >>> BEGIN V7__refund_and_payment_updates.sql
-- Extend consume_type enum and add payment reference columns
ALTER TYPE consume_type ADD VALUE IF NOT EXISTS 'REFUND';

ALTER TABLE app.shop_orders
    ADD COLUMN IF NOT EXISTS payment_ref VARCHAR(120);

ALTER TABLE app.appointments
    ADD COLUMN IF NOT EXISTS payment_ref VARCHAR(120);

-- <<< END V7__refund_and_payment_updates.sql

-- ============================================================
-- End of concatenated migrations
-- ============================================================
-- Verify:
--   SELECT nspname FROM pg_namespace WHERE nspname = 'app';
--   -- \dn+ app
--   -- \dt app.*
-- ============================================================
