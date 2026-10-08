-- =====================================================================
-- Tourism Homestay - Round 4 scale-up data (run after data_extra2.sql)
-- =====================================================================
USE tourism_homestay;

-- 1. Extra users to 40 (id 26-40)
INSERT INTO `user` (id, username, password, nickname, phone, avatar, gender, email, role, status, create_time, update_time) VALUES
(26, 'user18', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '滇池边的风', '13800000026', 'https://randomuser.me/api/portraits/men/19.jpg', 1, 'u18@tourism.com', 'USER', 0, '2026-04-12 09:00:00', '2026-04-12 09:00:00'),
(27, 'user19', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '火车迷小陈', '13800000027', 'https://randomuser.me/api/portraits/men/20.jpg', 1, 'u19@tourism.com', 'USER', 0, '2026-04-15 09:00:00', '2026-04-15 09:00:00'),
(28, 'user20', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '民宿试睡员', '13800000028', 'https://randomuser.me/api/portraits/men/21.jpg', 2, 'u20@tourism.com', 'USER', 0, '2026-04-18 09:00:00', '2026-04-18 09:00:00'),
(29, 'user21', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '周末去哪玩', '13800000029', 'https://randomuser.me/api/portraits/men/22.jpg', 2, 'u21@tourism.com', 'USER', 0, '2026-04-20 09:00:00', '2026-04-20 09:00:00'),
(30, 'user22', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '老饕逛云南', '13800000030', 'https://randomuser.me/api/portraits/men/23.jpg', 1, 'u22@tourism.com', 'USER', 0, '2026-04-22 09:00:00', '2026-04-22 09:00:00'),
(31, 'user23', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '云朵收藏家', '13800000031', 'https://randomuser.me/api/portraits/men/24.jpg', 2, 'u23@tourism.com', 'USER', 0, '2026-04-25 09:00:00', '2026-04-25 09:00:00'),
(32, 'user24', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '环湖青年', '13800000032', 'https://randomuser.me/api/portraits/men/25.jpg', 1, 'u24@tourism.com', 'USER', 0, '2026-04-28 09:00:00', '2026-04-28 09:00:00'),
(33, 'user25', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '边城旅人', '13800000033', 'https://randomuser.me/api/portraits/men/26.jpg', 1, 'u25@tourism.com', 'USER', 0, '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
(34, 'user26', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '摄影发烧友', '13800000034', 'https://randomuser.me/api/portraits/men/27.jpg', 1, 'u26@tourism.com', 'USER', 0, '2026-05-03 09:00:00', '2026-05-03 09:00:00'),
(35, 'user27', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '亲子假期', '13800000035', 'https://randomuser.me/api/portraits/men/28.jpg', 2, 'u27@tourism.com', 'USER', 0, '2026-05-05 09:00:00', '2026-05-05 09:00:00'),
(36, 'user28', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '一个人的旅行', '13800000036', 'https://randomuser.me/api/portraits/men/29.jpg', 2, 'u28@tourism.com', 'USER', 0, '2026-05-08 09:00:00', '2026-05-08 09:00:00'),
(37, 'user29', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '自然观察员', '13800000037', 'https://randomuser.me/api/portraits/men/30.jpg', 1, 'u29@tourism.com', 'USER', 0, '2026-05-10 09:00:00', '2026-05-10 09:00:00'),
(38, 'user30', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '温泉爱好者', '13800000038', 'https://randomuser.me/api/portraits/men/31.jpg', 2, 'u30@tourism.com', 'USER', 0, '2026-05-12 09:00:00', '2026-05-12 09:00:00'),
(39, 'user31', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '荷花季常客', '13800000039', 'https://randomuser.me/api/portraits/men/32.jpg', 1, 'u31@tourism.com', 'USER', 0, '2026-05-15 09:00:00', '2026-05-15 09:00:00'),
(40, 'user32', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '星空守望者', '13800000040', 'https://randomuser.me/api/portraits/men/33.jpg', 1, 'u32@tourism.com', 'USER', 0, '2026-05-18 09:00:00', '2026-05-18 09:00:00');

-- 2. Bulk orders: user x homestay deterministic buckets (past 120 days)
INSERT INTO orders (order_no, user_id, homestay_id, room_type_id, homestay_name, homestay_cover, room_type_name, check_in_date, check_out_date, nights, room_count, unit_price, total_amount, discount_amount, pay_amount, contact_name, contact_phone, status, pay_status, pay_type, create_time, pay_time, check_in_time, check_out_time, complete_time, cancel_time)
SELECT
  CONCAT('HSB', u.id * 1000 + h.id) AS order_no,
  u.id,
  h.id,
  rt.id,
  h.name, h.cover_image, rt.name,
  DATE_ADD(CURDATE(), INTERVAL -((u.id * 7 + h.id * 13) % 100 + 3) DAY) AS ckin,
  DATE_ADD(DATE_ADD(CURDATE(), INTERVAL -((u.id * 7 + h.id * 13) % 100 + 3) DAY), INTERVAL (1 + (u.id + h.id) % 3) DAY) AS ckout,
  1 + (u.id + h.id) % 3 AS nights,
  1 + (u.id + h.id * 2) % 2 AS room_count,
  rt.price,
  rt.price * (1 + (u.id + h.id) % 3) * (1 + (u.id + h.id * 2) % 2) AS total,
  0,
  rt.price * (1 + (u.id + h.id) % 3) * (1 + (u.id + h.id * 2) % 2) AS pay,
  u.nickname, u.phone,
  CASE (u.id + h.id) % 7
    WHEN 0 THEN 'COMPLETED' WHEN 1 THEN 'COMPLETED' WHEN 2 THEN 'COMPLETED'
    WHEN 3 THEN 'COMPLETED' WHEN 4 THEN 'CHECKED_IN' WHEN 5 THEN 'PAID'
    ELSE 'CANCELLED' END AS status,
  CASE WHEN (u.id + h.id) % 7 = 6 THEN 'UNPAID' ELSE 'PAID' END AS pay_status,
  ELT(1 + (u.id + h.id) % 3, 'WECHAT', 'ALIPAY', 'BANK') AS pay_type,
  DATE_SUB(NOW(), INTERVAL ((u.id * 5 + h.id * 11) % 200 + 5) HOUR) AS created,
  DATE_SUB(NOW(), INTERVAL ((u.id * 5 + h.id * 11) % 200 + 4) HOUR) AS paid,
  NULL, NULL, NULL,
  CASE WHEN (u.id + h.id) % 7 = 6 THEN DATE_SUB(NOW(), INTERVAL ((u.id * 5 + h.id * 11) % 100 + 1) HOUR) ELSE NULL END AS cancelled
FROM user u
JOIN homestay h ON h.audit_status = 'APPROVED' AND h.deleted = 0
JOIN room_type rt ON rt.homestay_id = h.id AND rt.status = 'ON'
WHERE u.role = 'USER' AND u.id BETWEEN 6 AND 40
  AND (u.id + h.id) % 7 IN (0,1,2)
  AND rt.id = (SELECT MIN(rt2.id) FROM room_type rt2 WHERE rt2.homestay_id = h.id AND rt2.status='ON')
  AND NOT EXISTS (SELECT 1 FROM orders o2 WHERE o2.order_no = CONCAT('HSB', u.id * 1000 + h.id));

-- 3. Bulk reviews for half of HSB completed orders
INSERT INTO review (order_id, user_id, homestay_id, rating, hygiene_score, service_score, facility_score, location_score, content, images, merchant_reply, reply_time, status, create_time, update_time)
SELECT
  o.id, o.user_id, o.homestay_id,
  4 + (o.id % 2) AS rating,
  4 + ((o.id + 1) % 2) AS hygiene,
  4 + ((o.id + 2) % 2) AS service,
  4 + ((o.id + 3) % 2) AS facility,
  4 + ((o.id + 4) % 2) AS location,
  ELT(1 + (o.id % 10),
    '非常满意的体验,房间干净舒适,位置也好找,下次还会来!',
    '民宿很有特色,老板人超好,还给升级了房型,推荐!',
    '整体不错,就是隔音稍微一般,其他都很满意。',
    '性价比很高,这个价格住到这样的民宿很值,早餐也好吃。',
    '风景绝了!早上起来看到这样的景色就觉得这趟值了。',
    '带孩子来的,民宿对小朋友很友好,有儿童设施,好评。',
    '位置方便,离景区很近,步行就能到,房间也干净。',
    '住了两晚,服务很贴心,还帮忙规划了游玩路线,感谢!',
    '装修很有格调,拍照很出片,公共区域也舒服。',
    '老板很热情,推荐了本地人才知道的美食,体验很棒!') AS content,
  CASE WHEN o.id % 3 = 0 THEN CONCAT('[\"https://picsum.photos/seed/grv', o.id, '/600/400\"]') ELSE NULL END AS images,
  CASE WHEN o.id % 2 = 0 THEN ELT(1 + (o.id % 5),
    '感谢您的认可,欢迎下次再来!',
    '谢谢支持,我们会继续努力做得更好!',
    '感谢您的建议,我们会持续改进,期待再会!',
    '很开心您喜欢,欢迎不同季节再来体验!',
    '谢谢惠顾,祝您旅途愉快,期待再见!') ELSE NULL END AS reply,
  DATE_ADD(o.complete_time, INTERVAL 1 DAY) AS rtime,
  0,
  DATE_ADD(o.complete_time, INTERVAL 1 DAY),
  DATE_ADD(o.complete_time, INTERVAL 2 DAY)
FROM orders o
WHERE o.order_no LIKE 'HSB%' AND o.status = 'COMPLETED'
  AND o.id % 2 = 0
  AND NOT EXISTS (SELECT 1 FROM review r WHERE r.order_id = o.id);

-- 4. Bulk favorites
INSERT INTO favorite (user_id, target_type, target_id, create_time)
SELECT u.id, 1, h.id, DATE_SUB(NOW(), INTERVAL ((u.id + h.id) % 90 + 1) DAY)
FROM user u
JOIN homestay h ON h.audit_status = 'APPROVED'
WHERE u.role = 'USER' AND u.id BETWEEN 6 AND 40
  AND (u.id + h.id * 2) % 5 = 0
  AND NOT EXISTS (SELECT 1 FROM favorite f2 WHERE f2.user_id = u.id AND f2.target_type = 1 AND f2.target_id = h.id);

-- 5. Recompute homestay rating by reviews
UPDATE homestay h
SET h.rating = (SELECT ROUND(AVG(r.rating), 1) FROM review r WHERE r.homestay_id = h.id AND r.status = 0 AND r.deleted = 0),
    h.rating_count = (SELECT COUNT(*) FROM review r WHERE r.homestay_id = h.id AND r.status = 0 AND r.deleted = 0)
WHERE h.deleted = 0;
UPDATE homestay SET rating = 5.0, rating_count = 0 WHERE rating_count IS NULL OR rating_count = 0;

-- 6. Bulk travel plans for odd users 9-39
INSERT INTO travel_plan (user_id, plan_name, destination_id, destination_name, start_date, end_date, intro, status, create_time, update_time)
SELECT u.id,
  CONCAT((SELECT name FROM destination WHERE id = 1 + (u.id * 3) % 9), '之旅') AS pname,
  1 + (u.id * 3) % 9 AS did,
  (SELECT name FROM destination WHERE id = 1 + (u.id * 3) % 9) AS dname,
  DATE_ADD(CURDATE(), INTERVAL (u.id % 20 + 5) DAY) AS sdate,
  DATE_ADD(CURDATE(), INTERVAL (u.id % 20 + 8) DAY) AS edate,
  ELT(1 + u.id % 4,
    '想去看看不同的风景,体验当地生活。',
    '计划了很久的旅行,把想去的都排上。',
    '和家人一起的放松之旅,以吃为主。',
    '摄影之旅,追日出日落和好风景。') AS intro,
  'ONGOING',
  DATE_SUB(NOW(), INTERVAL u.id DAY),
  DATE_SUB(NOW(), INTERVAL u.id DAY)
FROM user u
WHERE u.role = 'USER' AND u.id BETWEEN 9 AND 39
  AND u.id % 2 = 1
  AND NOT EXISTS (SELECT 1 FROM travel_plan tp WHERE tp.user_id = u.id);

-- 7. Plan items for new plans (2 days x 3 items)
INSERT INTO travel_plan_item (plan_id, day_number, item_type, item_id, item_name, start_time, end_time, remark, sort_order)
SELECT tp.id,
  dseq.n AS dayn,
  ELT(1 + (tp.id + dseq.n) % 4, 'SCENIC', 'EXPERIENCE', 'RESTAURANT', 'HOMESTAY') AS itype,
  CASE (tp.id + dseq.n) % 4
    WHEN 0 THEN (SELECT id FROM scenic_spot WHERE destination_id = tp.destination_id ORDER BY id LIMIT 1)
    WHEN 1 THEN (SELECT id FROM tourism_experience WHERE destination_id = tp.destination_id ORDER BY id LIMIT 1)
    WHEN 2 THEN (SELECT id FROM restaurant WHERE destination_id = tp.destination_id ORDER BY id LIMIT 1)
    ELSE (SELECT id FROM homestay WHERE destination_id = tp.destination_id AND audit_status='APPROVED' ORDER BY id LIMIT 1)
  END AS iid,
  CASE (tp.id + dseq.n) % 4
    WHEN 0 THEN (SELECT name FROM scenic_spot WHERE destination_id = tp.destination_id ORDER BY id LIMIT 1)
    WHEN 1 THEN (SELECT name FROM tourism_experience WHERE destination_id = tp.destination_id ORDER BY id LIMIT 1)
    WHEN 2 THEN (SELECT name FROM restaurant WHERE destination_id = tp.destination_id ORDER BY id LIMIT 1)
    ELSE (SELECT name FROM homestay WHERE destination_id = tp.destination_id AND audit_status='APPROVED' ORDER BY id LIMIT 1)
  END AS iname,
  CONCAT(LPAD(9 + (tp.id + dseq.n) % 6, 2, '0'), ':00') AS st,
  CONCAT(LPAD(11 + (tp.id + dseq.n) % 6, 2, '0'), ':00') AS et,
  NULL,
  dseq.n
FROM travel_plan tp
JOIN (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3) dseq
WHERE tp.id > 2 AND tp.user_id % 2 = 1;
