-- ============================================================
-- PawStay 演示数据（MySQL 与 H2 通用）
-- 日期基于当前日期动态生成，任何时间启动都自带"寄养中"的进行时数据
-- 日期表达式采用两库通用的标准语法：NOW() - INTERVAL 'n' DAY
-- 所有演示账号密码均为 123456（SHA-256(盐+原文)）
-- ============================================================

-- 用户：2 主人 / 3 商家 / 1 管理员
INSERT INTO pet_user (id, phone, password, nickname, role, status, create_time) VALUES
(1, '13800000001', 'd6ef976ad206c915752396b9e9ec4d2c1dcf1def7322d06853f6d788c0c8d088', '小汪', 'OWNER', 1, NOW() - INTERVAL '60' DAY),
(2, '13800000002', 'd6ef976ad206c915752396b9e9ec4d2c1dcf1def7322d06853f6d788c0c8d088', '桃桃妈', 'OWNER', 1, NOW() - INTERVAL '45' DAY),
(3, '13900000001', 'd6ef976ad206c915752396b9e9ec4d2c1dcf1def7322d06853f6d788c0c8d088', '汪汪之家店长', 'MERCHANT', 1, NOW() - INTERVAL '90' DAY),
(4, '13900000002', 'd6ef976ad206c915752396b9e9ec4d2c1dcf1def7322d06853f6d788c0c8d088', '猫屿主理人', 'MERCHANT', 1, NOW() - INTERVAL '80' DAY),
(5, '13900000003', 'd6ef976ad206c915752396b9e9ec4d2c1dcf1def7322d06853f6d788c0c8d088', '汪喵星球店长', 'MERCHANT', 1, NOW() - INTERVAL '2' DAY),
(6, '10000000000', 'd6ef976ad206c915752396b9e9ec4d2c1dcf1def7322d06853f6d788c0c8d088', '平台管理员', 'ADMIN', 1, NOW() - INTERVAL '120' DAY);

-- 商家：2 家已通过，1 家待审核（供管理端审核演示）
INSERT INTO pet_merchant (id, user_id, shop_name, address, phone, license, description, tags, service_species, rating, reply_rate, audit_status, audit_reason, boarding_count, open_status, create_time) VALUES
(1, 3, '汪汪之家宠物寄养', '阳光花园小区南门沿街12号', '13900000001', '动物防疫条件合格证 编号A2023-115', '专注犬类寄养8年，独立犬舍24小时监控，每日两次户外遛弯，持证驯养师驻店，接送服务覆盖全城。', '持证经营,监控直看,每日遛弯', 'DOG', 4.9, 98, 'APPROVED', NULL, 12, 1, NOW() - INTERVAL '90' DAY),
(2, 4, '猫屿·猫咪专属寄养', '梧桐路38号创意园B栋2层', '13900000002', '营业执照 91330106MA2CAT2024', '猫咪独栋无犬环境，三层猫公寓独立通风，每日投喂直播，主理人持有宠物护理师证书。', '无犬环境,投喂直播,猫咪专属', 'CAT', 4.8, 100, 'APPROVED', NULL, 8, 1, NOW() - INTERVAL '80' DAY),
(3, 5, '汪喵星球寄养中心', '滨江路99号临街商铺', '13900000003', '营业执照 91330106MA2PET2026', '新店开业，猫犬分区寄养，首单价五折。', '新店开业,猫犬分区', 'CAT,DOG', 5.0, 100, 'PENDING', NULL, 0, 1, NOW() - INTERVAL '2' DAY);

-- 宠物档案
INSERT INTO pet_pet (id, user_id, name, species, breed, gender, birth_date, weight, sterilized, temperament, aggressive, vaccinated, vaccine_note, notes, create_time) VALUES
(1, 1, '汪汪', 'DOG', '金毛寻回犬', 'M', CAST(CURRENT_DATE - INTERVAL '1100' DAY AS DATE), 28.5, 1, '温顺', 0, 1, '2026-05-12 犬联苗加强', '怕打雷，需要安抚。', NOW() - INTERVAL '60' DAY),
(2, 1, '咪咪', 'CAT', '布偶猫', 'F', CAST(CURRENT_DATE - INTERVAL '700' DAY AS DATE), 4.2, 1, '温顺', 0, 1, '2026-03-08 猫三联年度', '有点挑食，需自带主粮。', NOW() - INTERVAL '60' DAY),
(3, 2, '豆豆', 'DOG', '柯基', 'M', CAST(CURRENT_DATE - INTERVAL '900' DAY AS DATE), 11.8, 1, '活泼', 0, 1, '2026-06-20 犬联苗', '精力旺盛，喜欢追球。', NOW() - INTERVAL '45' DAY);

-- 房型
INSERT INTO pet_room (id, merchant_id, name, species, max_weight, need_sterilized, allow_aggressive, total_count, price_per_day, photo, tags, description, status, create_time) VALUES
(1, 1, '标准犬舍', 'DOG', 40.00, 0, 1, 6, 68.00, NULL, '24h监控,独立通风', '2.5㎡独立犬舍，恒温通风，每日两次消毒。', 1, NOW()),
(2, 1, '豪华犬房', 'DOG', 60.00, 0, 1, 3, 128.00, NULL, '独立空调,每日视频', '4㎡带落地窗犬房，附赠每日玩耍视频。', 1, NOW()),
(3, 1, '小型犬温馨间', 'DOG', 15.00, 0, 1, 4, 58.00, NULL, '小型犬专属,软垫窝位', '适合15kg以内小型犬，防滑软垫。', 1, NOW()),
(4, 2, '猫咪标准间', 'CAT', 10.00, 1, 0, 8, 78.00, NULL, '无犬环境,每日直播', '三层猫别墅，独立通风，需已绝育。', 1, NOW()),
(5, 2, '猫咪大床房', 'CAT', 10.00, 1, 0, 4, 138.00, NULL, '全景窗景,专属管家', '2㎡全景猫房，管家一对一陪玩。', 1, NOW());

-- 订单：覆盖全部状态（寄养中/已接单/已完成/待确认/已取消）
INSERT INTO pet_order (id, order_no, user_id, pet_id, merchant_id, room_id, start_date, end_date, days, price_per_day, total_amount, status, contact_name, contact_phone, remark, agreement_signed, pay_time, checkin_time, checkout_time, weight_in, weight_out, reject_reason, create_time, update_time) VALUES
(1, 'PS20260901001', 1, 1, 1, 1, CAST(CURRENT_DATE - INTERVAL '3' DAY AS DATE), CAST(CURRENT_DATE + INTERVAL '4' DAY AS DATE), 7, 68.00, 476.00, 'BOARDING', '小汪', '13800000001', '怕打雷，麻烦多安抚', 1, NOW() - INTERVAL '3' DAY, NOW() - INTERVAL '3' DAY, NULL, 28.5, NULL, NULL, NOW() - INTERVAL '3' DAY, NOW()),
(2, 'PS20260902002', 1, 2, 2, 4, CAST(CURRENT_DATE + INTERVAL '3' DAY AS DATE), CAST(CURRENT_DATE + INTERVAL '6' DAY AS DATE), 3, 78.00, 234.00, 'CONFIRMED', '小汪', '13800000001', '自带主粮和猫砂', 1, NOW() - INTERVAL '1' DAY, NULL, NULL, NULL, NULL, NULL, NOW() - INTERVAL '1' DAY, NOW()),
(3, 'PS20260815003', 2, 3, 1, 2, CAST(CURRENT_DATE - INTERVAL '20' DAY AS DATE), CAST(CURRENT_DATE - INTERVAL '15' DAY AS DATE), 5, 128.00, 640.00, 'COMPLETED', '桃桃妈', '13800000002', '第一次寄养，请多关照', 1, NOW() - INTERVAL '20' DAY, NOW() - INTERVAL '20' DAY, NOW() - INTERVAL '15' DAY, 11.5, 11.6, NULL, NOW() - INTERVAL '21' DAY, NOW() - INTERVAL '15' DAY),
(4, 'PS20260720004', 1, 1, 1, 2, CAST(CURRENT_DATE - INTERVAL '40' DAY AS DATE), CAST(CURRENT_DATE - INTERVAL '35' DAY AS DATE), 5, 128.00, 640.00, 'COMPLETED', '小汪', '13800000001', '', 1, NOW() - INTERVAL '40' DAY, NOW() - INTERVAL '40' DAY, NOW() - INTERVAL '35' DAY, 28.8, 28.4, NULL, NOW() - INTERVAL '41' DAY, NOW() - INTERVAL '35' DAY),
(5, 'PS20260904005', 2, 3, 1, 1, CAST(CURRENT_DATE + INTERVAL '1' DAY AS DATE), CAST(CURRENT_DATE + INTERVAL '2' DAY AS DATE), 1, 68.00, 68.00, 'PENDING_CONFIRM', '桃桃妈', '13800000002', '第一次寄养，请多关照', 1, NULL, NULL, NULL, NULL, NULL, NULL, NOW() - INTERVAL '4' HOUR, NOW()),
(6, 'PS20260828006', 1, 2, 2, 5, CAST(CURRENT_DATE + INTERVAL '10' DAY AS DATE), CAST(CURRENT_DATE + INTERVAL '12' DAY AS DATE), 2, 138.00, 276.00, 'CANCELLED', '小汪', '13800000001', '', 1, NOW() - INTERVAL '8' DAY, NULL, NULL, NULL, NULL, NULL, NOW() - INTERVAL '8' DAY, NOW() - INTERVAL '6' DAY);

-- 寄养日记（订单1寄养中，连续3天打卡，含每日体重供健康曲线展示）
INSERT INTO pet_care_record (order_id, merchant_id, record_date, meal_done, walk_done, mood, content, weight, create_time) VALUES
(1, 1, CAST(CURRENT_DATE - INTERVAL '2' DAY AS DATE), 1, 1, '开心', '汪汪今天食欲很好，早晚各遛弯一次，下午和小伙伴玩了飞盘，玩得很尽兴。', 28.5, NOW() - INTERVAL '2' DAY),
(1, 1, CAST(CURRENT_DATE - INTERVAL '1' DAY AS DATE), 1, 1, '开心', '今天天气好，户外活动加时半小时，晚上已经视频反馈给主人啦。', 28.4, NOW() - INTERVAL '1' DAY),
(1, 1, CURRENT_DATE, 1, 0, '平静', '上午喂食正常，中午在窝里晒太阳午休，下午安排遛弯。', NULL, NOW());

-- 评价（两笔已完成订单）
INSERT INTO pet_review (id, order_id, user_id, merchant_id, rating, content, photos, reply, create_time) VALUES
(1, 3, 2, 1, 5, '豆豆回家后一直很开心，每天都能收到照片反馈，特别放心！', NULL, '谢谢信任，欢迎豆豆随时回来玩～', NOW() - INTERVAL '14' DAY),
(2, 4, 1, 1, 4, '环境不错，服务也细心，就是旺季有点难订。', NULL, '感谢建议，我们已扩充豪华房数量啦。', NOW() - INTERVAL '34' DAY);

-- 站内消息
INSERT INTO pet_message (id, user_id, title, content, type, is_read, ref_id, create_time) VALUES
(1, 1, '下单成功', '您在「汪汪之家宠物寄养」的寄养订单已提交，支付后等待商家确认。', 'ORDER', 1, 1, NOW() - INTERVAL '3' DAY),
(2, 1, '商家已接单', '「汪汪之家宠物寄养」已确认您的订单，记得按时入住哦。', 'ORDER', 1, 1, NOW() - INTERVAL '3' DAY),
(3, 1, '寄养日记更新', '毛孩子今天（昨天）的寄养日记已更新，快去看看它吧～', 'DIARY', 0, 1, NOW() - INTERVAL '1' DAY),
(4, 1, '订单已确认', '猫屿·猫咪专属寄养已确认您的订单。', 'ORDER', 0, 2, NOW() - INTERVAL '1' DAY),
(5, 3, '收到新的寄养订单', '「豆豆」的主人已下单寄养，请及时确认。', 'ORDER', 0, 5, NOW() - INTERVAL '4' HOUR);
