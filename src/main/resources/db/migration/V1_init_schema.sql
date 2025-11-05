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
