-- triggers and functions (set_updated_at, bump_version, guards, settlement, refund, rebuild...)
-- [同你提供版本，出于长度此处已包含要点；若需逐行粘贴，请用你上条 SQL 全量替换本注释块]

INSERT INTO users(nickname, phone) VALUES ('测试用户','0400000001');
INSERT INTO products(name, category, price_cents, description, stock_actual, status) VALUES
('粉色穿戴甲 S','nails',3990,'入门款',50,'ON'),
('玩具小狗','toys',1990,'热卖',100,'ON');
INSERT INTO services(category, price_cents, description, status) VALUES
('manicure',6900,'基础手部护理','ON'),
('pedicure',7900,'基础足部护理','ON');