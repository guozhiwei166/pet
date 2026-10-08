
-- ============================================================
-- Round 4b: balance order statuses (PAID future / CHECKED_IN recent)
-- ============================================================
USE tourism_homestay;
INSERT INTO orders (order_no, user_id, homestay_id, room_type_id, homestay_name, homestay_cover, room_type_name, check_in_date, check_out_date, nights, room_count, unit_price, total_amount, discount_amount, pay_amount, contact_name, contact_phone, status, pay_status, pay_type, create_time, pay_time)
SELECT
  CONCAT('HSP', u.id * 1000 + h.id) AS order_no,
  u.id, h.id, rt.id, h.name, h.cover_image, rt.name,
  DATE_ADD(CURDATE(), INTERVAL ((u.id + h.id) % 14 + 2) DAY) AS ckin,
  DATE_ADD(DATE_ADD(CURDATE(), INTERVAL ((u.id + h.id) % 14 + 2) DAY), INTERVAL (1 + (u.id + h.id) % 2) DAY) AS ckout,
  1 + (u.id + h.id) % 2 AS nights, 1 AS room_count,
  rt.price, rt.price * (1 + (u.id + h.id) % 2), 0,
  rt.price * (1 + (u.id + h.id) % 2) AS pay,
  u.nickname, u.phone, 'PAID', 'PAID',
  ELT(1 + (u.id + h.id) % 3, 'WECHAT', 'ALIPAY', 'BANK'),
  DATE_SUB(NOW(), INTERVAL (1 + (u.id + h.id) % 5) DAY),
  DATE_SUB(NOW(), INTERVAL (1 + (u.id + h.id) % 5) DAY)
FROM user u
JOIN homestay h ON h.audit_status = 'APPROVED' AND h.deleted = 0
JOIN room_type rt ON rt.homestay_id = h.id AND rt.status = 'ON'
WHERE u.role = 'USER' AND u.id BETWEEN 6 AND 40
  AND (u.id + h.id) % 9 IN (0,1,2,3)
  AND rt.id = (SELECT MIN(rt2.id) FROM room_type rt2 WHERE rt2.homestay_id = h.id AND rt2.status='ON')
  AND NOT EXISTS (SELECT 1 FROM orders o2 WHERE o2.order_no = CONCAT('HSP', u.id * 1000 + h.id));

-- recent CHECKED_IN (past 1-3 days, checked in)
INSERT INTO orders (order_no, user_id, homestay_id, room_type_id, homestay_name, homestay_cover, room_type_name, check_in_date, check_out_date, nights, room_count, unit_price, total_amount, discount_amount, pay_amount, contact_name, contact_phone, status, pay_status, pay_type, create_time, pay_time, check_in_time)
SELECT
  CONCAT('HSC', u.id * 1000 + h.id) AS order_no,
  u.id, h.id, rt.id, h.name, h.cover_image, rt.name,
  DATE_ADD(CURDATE(), INTERVAL -((u.id + h.id) % 3 + 1) DAY) AS ckin,
  DATE_ADD(DATE_ADD(CURDATE(), INTERVAL -((u.id + h.id) % 3 + 1) DAY), INTERVAL (1 + (u.id + h.id) % 3) DAY) AS ckout,
  1 + (u.id + h.id) % 3 AS nights, 1 AS room_count,
  rt.price, rt.price * (1 + (u.id + h.id) % 3), 0,
  rt.price * (1 + (u.id + h.id) % 3) AS pay,
  u.nickname, u.phone, 'CHECKED_IN', 'PAID',
  ELT(1 + (u.id + h.id) % 3, 'WECHAT', 'ALIPAY', 'BANK'),
  DATE_SUB(NOW(), INTERVAL ((u.id + h.id) % 6 + 2) DAY),
  DATE_SUB(NOW(), INTERVAL ((u.id + h.id) % 6 + 2) DAY),
  DATE_SUB(NOW(), INTERVAL ((u.id + h.id) % 2 + 1) DAY)
FROM user u
JOIN homestay h ON h.audit_status = 'APPROVED' AND h.deleted = 0
JOIN room_type rt ON rt.homestay_id = h.id AND rt.status = 'ON'
WHERE u.role = 'USER' AND u.id BETWEEN 6 AND 40
  AND (u.id + h.id) % 9 IN (5,6,7)
  AND rt.id = (SELECT MIN(rt2.id) FROM room_type rt2 WHERE rt2.homestay_id = h.id AND rt2.status='ON')
  AND NOT EXISTS (SELECT 1 FROM orders o2 WHERE o2.order_no = CONCAT('HSC', u.id * 1000 + h.id));

-- recent CANCELLED (past 10-30 days, unpaid)
INSERT INTO orders (order_no, user_id, homestay_id, room_type_id, homestay_name, homestay_cover, room_type_name, check_in_date, check_out_date, nights, room_count, unit_price, total_amount, discount_amount, pay_amount, contact_name, contact_phone, status, pay_status, pay_type, create_time, cancel_time)
SELECT
  CONCAT('HSX2', u.id * 1000 + h.id) AS order_no,
  u.id, h.id, rt.id, h.name, h.cover_image, rt.name,
  DATE_ADD(CURDATE(), INTERVAL ((u.id + h.id) % 20 + 5) DAY) AS ckin,
  DATE_ADD(DATE_ADD(CURDATE(), INTERVAL ((u.id + h.id) % 20 + 5) DAY), INTERVAL 2 DAY) AS ckout,
  2, 1 AS room_count,
  rt.price, rt.price * 2, 0, rt.price * 2 AS pay,
  u.nickname, u.phone, 'CANCELLED', 'UNPAID',
  ELT(1 + (u.id + h.id) % 3, 'WECHAT', 'ALIPAY', 'BANK'),
  DATE_SUB(NOW(), INTERVAL ((u.id + h.id) % 15 + 5) DAY),
  DATE_SUB(NOW(), INTERVAL ((u.id + h.id) % 14 + 4) DAY)
FROM user u
JOIN homestay h ON h.audit_status = 'APPROVED' AND h.deleted = 0
JOIN room_type rt ON rt.homestay_id = h.id AND rt.status = 'ON'
WHERE u.role = 'USER' AND u.id BETWEEN 6 AND 40
  AND (u.id + h.id) % 11 = 5
  AND rt.id = (SELECT MIN(rt2.id) FROM room_type rt2 WHERE rt2.homestay_id = h.id AND rt2.status='ON')
  AND NOT EXISTS (SELECT 1 FROM orders o2 WHERE o2.order_no = CONCAT('HSX2', u.id * 1000 + h.id));

-- Converge order volume (keep status balance)
DELETE FROM orders WHERE order_no LIKE 'HSP%' AND id % 7 NOT IN (0,1);
DELETE FROM orders WHERE order_no LIKE 'HSC%' AND id % 5 <> 0;
DELETE FROM orders WHERE order_no LIKE 'HSX2%' AND id % 3 <> 0;
