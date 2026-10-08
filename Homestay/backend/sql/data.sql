-- =====================================================================
-- 旅游民宿综合服务平台 - 初始化数据
-- 适用 schema: schema.sql(先执行建库建表,再执行本脚本)
-- 统一密码: 123456 (BCrypt)
-- 图片: 使用 picsum.photos 稳定 seed 占位图,演示无需本地资源
-- =====================================================================
USE tourism_homestay;

-- ---------------------------------------------------------------------
-- 1. 用户 user(含管理员/商家/普通用户)
-- 密码均为 123456
-- ---------------------------------------------------------------------
INSERT INTO `user` (id, username, password, nickname, phone, avatar, gender, email, role, status, create_time, update_time) VALUES
(1,  'admin',         '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '平台管理员', '13800000001', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', 0, 'admin@tourism.com',     'ADMIN',    0, '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
(2,  'merchant_dali', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '洱海边的云', '13800000002', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60',   0, 'mer1@tourism.com',      'MERCHANT', 0, '2026-01-02 09:00:00', '2026-01-02 09:00:00'),
(3,  'merchant_lijiang', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '古城慢生活', '13800000003', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', 0, 'mer2@tourism.com',      'MERCHANT', 0, '2026-01-03 09:00:00', '2026-01-03 09:00:00'),
(4,  'merchant_kunming', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '春城小院', '13800000004', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', 0, 'mer3@tourism.com',      'MERCHANT', 0, '2026-01-04 09:00:00', '2026-01-04 09:00:00'),
(5,  'merchant_pending', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '待审核新商家', '13800000005', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', 0, 'mer4@tourism.com',    'MERCHANT', 0, '2026-02-01 09:00:00', '2026-02-01 09:00:00'),
(6,  'user01',        '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '旅行的小鹿', '13800000006', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60',  1, 'u1@tourism.com',        'USER',     0, '2026-01-05 09:00:00', '2026-01-05 09:00:00'),
(7,  'user02',        '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '背包客阿凯', '13800000007', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60',  1, 'u2@tourism.com',        'USER',     0, '2026-01-06 09:00:00', '2026-01-06 09:00:00'),
(8,  'user03',        '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '摄影小满', '13800000008',   'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60',  2, 'u3@tourism.com',        'USER',     0, '2026-01-07 09:00:00', '2026-01-07 09:00:00'),
(9,  'user04',        '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '亲子游妈妈', '13800000009', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60',  2, 'u4@tourism.com',        'USER',     0, '2026-01-10 09:00:00', '2026-01-10 09:00:00'),
(10, 'user05',        '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '自由行老周', '13800000010', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60',  1, 'u5@tourism.com',        'USER',     0, '2026-02-05 09:00:00', '2026-02-05 09:00:00'),
(11, 'user06',        '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '旅拍少女', '13800000011',   'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60',  2, 'u6@tourism.com',        'USER',     0, '2026-02-10 09:00:00', '2026-02-10 09:00:00'),
(12, 'user07',        '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '洱海骑行侠', '13800000012', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60',  1, 'u7@tourism.com',        'USER',     0, '2026-03-01 09:00:00', '2026-03-01 09:00:00');

-- ---------------------------------------------------------------------
-- 2. 目的地 destination
-- ---------------------------------------------------------------------
INSERT INTO destination (id, name, province, city, level, cover_image, description, scenic_tags, status, sort_order, create_time, update_time) VALUES
(1, '大理', '云南省', '大理白族自治州', 2, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60',  '风花雪月,苍山洱海,白族文化的故乡。', '苍山,洱海,古城,白族', 1, 1, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
(2, '丽江', '云南省', '丽江市', 2, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60',  '雪山脚下的古城,纳西文化与慢生活。', '古城,雪山,纳西,束河', 1, 2, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
(3, '昆明', '云南省', '昆明市', 2, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60',  '四季如春的春城,滇池与石林。', '滇池,石林,温泉,花市', 1, 3, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
(4, '西双版纳', '云南省', '西双版纳傣族自治州', 2, 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '热带雨林与傣族风情,东南亚风情。', '雨林,傣族,植物园,泼水节', 1, 4, '2026-01-01 10:00:00', '2026-01-01 10:00:00');

-- ---------------------------------------------------------------------
-- 3. 商家 merchant(关联用户)
-- 说明:民宿发布前需平台审核;这里 Dali/Lijiang/Kunming 商家已通过,merchant_pending 为待审核演示
-- ---------------------------------------------------------------------
INSERT INTO merchant (id, user_id, merchant_name, contact_name, contact_phone, license_no, description, status, audit_time, audit_remark, create_time, update_time) VALUES
(1, 2, '大理洱海民宿管理有限公司', '张老板', '13800000002', '91532900MA6DLI', '专注洱海一线海景民宿十年,提供环海骑行、旅拍等服务。', 'APPROVED', '2026-01-03 10:00:00', '审核通过', '2026-01-02 09:00:00', '2026-01-03 10:00:00'),
(2, 3, '丽江古城客栈集团', '和掌柜', '13800000003', '91530702MA6LJ',   '丽江古城内多家特色纳西小院,闹中取静。', 'APPROVED', '2026-01-04 10:00:00', '审核通过', '2026-01-03 09:00:00', '2026-01-04 10:00:00'),
(3, 4, '昆明春城小院民宿', '李经理', '13800000004', '91530100MA6KM',   '昆明市区花园民宿,近滇池与翠湖。',   'APPROVED', '2026-01-05 10:00:00', '审核通过', '2026-01-04 09:00:00', '2026-01-05 10:00:00'),
(4, 5, '待审核商家示例', '王申请', '13800000005', '91530100MA6XX',    '新申请入驻的商家,用于演示管理员审核流程。', 'PENDING', NULL, NULL, '2026-02-01 09:00:00', '2026-02-01 09:00:00');

-- ---------------------------------------------------------------------
-- 4. 民宿 homestay(12 家;1-6 大理,7-10 丽江,11-12 昆明;含 1 家待审核/下架用于演示)
-- ---------------------------------------------------------------------
INSERT INTO homestay (id, merchant_id, destination_id, name, cover_image, address, longitude, latitude, intro, description, features, facilities, phone, business_status, audit_status, rating, rating_count, favorite_count, view_count, min_price, sort_order, audit_time, audit_remark, create_time, update_time) VALUES
(1, 1, 1, '洱海之恋海景度假民宿', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '大理市海东镇环海东路 168 号', 100.2676, 25.6065, '推窗见洱海,躺看苍山雪', '位于洱海东岸的一线海景民宿,全屋落地窗正对洱海。清晨在露台看日出,傍晚沿环海路骑行。提供免费自行车、旅拍预约与白族早餐。', '海景,网红露台,骑行,旅拍', 'WiFi,停车场,接送机,自行车,早餐', '0872-8888001', 'OPEN', 'APPROVED', 4.8, 86, 132, 3200, 468.00, 1, '2026-01-05 10:00:00', NULL, '2026-01-03 09:00:00', '2026-01-05 10:00:00'),
(2, 1, 1, '大理古城·云见小院', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '大理市大理古城人民路 88 号', 100.1673, 25.6923, '古城里的白族三合院,闹中取静', '坐落在大理古城人民路上的白族传统三合院民宿,青瓦白墙,院内种满多肉与绣球花。距洋人街、五华楼步行 5 分钟。', '庭院,古城,安静,白族', 'WiFi,早餐,洗衣房,茶室', '0872-8888002', 'OPEN', 'APPROVED', 4.6, 54, 78, 2100, 268.00, 2, '2026-01-05 10:00:00', NULL, '2026-01-03 10:00:00', '2026-01-05 10:00:00'),
(3, 1, 1, '才村码头·湖景轻奢度假屋', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', '大理市大理镇才村码头 12 号', 100.2156, 25.6721, '洱海边的轻奢一居,适合情侣', '紧邻才村码头,步行到洱海边 2 分钟。房间配备全屋智能与高端床品,顶楼有 360 度湖景露台。', '湖景,轻奢,情侣,智能家居', 'WiFi,停车场,早餐,投影,浴缸', '0872-8888003', 'OPEN', 'APPROVED', 4.9, 42, 66, 1800, 588.00, 3, '2026-01-05 10:00:00', NULL, '2026-01-03 11:00:00', '2026-01-05 10:00:00'),
(4, 1, 1, '苍山脚下·半山听风别院', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '大理市银桥镇苍山半山腰 1 号', 100.1300, 25.7200, '半山上的森林木屋,负氧离子爆表', '隐于苍山半山的森林民宿,每间房都有山景阳台,清晨可听到鸟鸣。可预约徒步苍山、采茶体验。', '山景,森林,安静,徒步', 'WiFi,停车场,篝火,茶室,接机', '0872-8888004', 'OPEN', 'APPROVED', 4.7, 33, 51, 1500, 398.00, 4, '2026-01-05 10:00:00', NULL, '2026-01-03 12:00:00', '2026-01-05 10:00:00'),
(5, 1, 1, '喜洲古镇·稻香田园民宿', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '大理市喜洲镇古镇内 22 号', 100.1150, 25.8550, '推开窗就是金黄稻田', '喜洲古镇边的田园民宿,一楼带小院,二楼可观稻田与苍山。可步行到喜洲粑粑、转角楼打卡。', '田园,古镇,亲子,稻田', 'WiFi,停车场,厨房,儿童设施', '0872-8888005', 'OPEN', 'APPROVED', 4.5, 28, 40, 980, 328.00, 5, '2026-01-05 10:00:00', NULL, '2026-01-03 13:00:00', '2026-01-05 10:00:00'),
(6, 1, 1, '双廊·海地生活精品客栈', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', '大理市双廊镇海街 6 号', 100.2700, 25.8700, '双廊古镇的海景白房子', '位于双廊镇核心区的海景客栈,白色地中海风格建筑,楼下就是网红打卡点。提供旅拍与环海包车服务。', '海景,网红,双廊,白房子', 'WiFi,餐厅,咖啡,旅拍', '0872-8888006', 'OPEN', 'APPROVED', 4.4, 60, 90, 2600, 458.00, 6, '2026-01-05 10:00:00', NULL, '2026-01-03 14:00:00', '2026-01-05 10:00:00'),
(7, 2, 2, '丽江古城·纳西四合院客栈', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '丽江市古城区七一街 56 号', 100.2346, 26.8721, '古城深处的百年纳西老院', '丽江古城内保存完好的纳西族四合五天井院落,木质结构,冬暖夏凉。夜晚安静,距四方街步行 8 分钟。', '古城,纳西,老宅,安静', 'WiFi,茶室,接机,早餐', '0888-6666001', 'OPEN', 'APPROVED', 4.7, 75, 110, 2900, 388.00, 7, '2026-01-06 10:00:00', NULL, '2026-01-04 09:00:00', '2026-01-06 10:00:00'),
(8, 2, 2, '束河·雪山脚下花园民宿', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '丽江市束河古镇中和路 9 号', 100.2100, 26.9000, '抬头就能看见玉龙雪山', '束河古镇的花园民宿,院子里种满月季与薰衣草,晴天可远眺玉龙雪山。免费提供雪山一日游规划。', '雪山,花园,束河,浪漫', 'WiFi,停车场,花园,烧烤', '0888-6666002', 'OPEN', 'APPROVED', 4.8, 49, 72, 2000, 468.00, 8, '2026-01-06 10:00:00', NULL, '2026-01-04 10:00:00', '2026-01-06 10:00:00'),
(9, 2, 2, '玉龙雪山·山语民宿', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '丽江市白沙古镇旁 3 公里', 100.1900, 26.9900, '白沙古镇旁看雪山日落', '位于白沙古镇与玉龙雪山之间的乡村民宿,星空极美,配有电热毯与地暖,适合看星星与雪山。', '雪山,星空,白沙,乡村', 'WiFi,地暖,停车场,厨房', '0888-6666003', 'OPEN', 'APPROVED', 4.6, 22, 30, 800, 288.00, 9, '2026-01-06 10:00:00', NULL, '2026-01-04 11:00:00', '2026-01-06 10:00:00'),
(10, 2, 2, '古城狮子山·观景云端客栈', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '丽江市古城区狮子山万古楼旁', 100.2300, 26.8800, '古城最高处的观景客栈', '建在狮子山上的观景客栈,能俯瞰整个丽江古城与远处的雪山。日出日落都是大片机位。', '观景,古城全景,日出', 'WiFi,咖啡,观景台', '0888-6666004', 'OPEN', 'APPROVED', 4.5, 38, 55, 1700, 428.00, 10, '2026-01-06 10:00:00', NULL, '2026-01-04 12:00:00', '2026-01-06 10:00:00'),
(11, 3, 3, '昆明翠湖·小满花园民宿', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '昆明市五华区翠湖南路 66 号', 102.7050, 25.0450, '春城翠湖边,推窗见红嘴鸥', '翠湖公园旁的花园洋房民宿,冬季推窗可见红嘴鸥,步行可达讲武堂、文林街咖啡馆。', '城市,花园,翠湖,鸥鸟', 'WiFi,厨房,洗衣机,停车', '0871-3333001', 'OPEN', 'APPROVED', 4.6, 30, 44, 1200, 358.00, 11, '2026-01-07 10:00:00', NULL, '2026-01-05 09:00:00', '2026-01-07 10:00:00'),
(12, 3, 3, '滇池海埂·暖冬度假民宿', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '昆明市西山区海埂大坝 20 号', 102.6500, 24.9700, '滇池边看海鸥与落日', '紧邻海埂大坝的度假民宿,可骑行环滇池,冬季海鸥成群。部分房间带独立阳台面向滇池。', '滇池,海鸥,骑行,暖冬', 'WiFi,停车场,早餐,洗衣机', '0871-3333002', 'OPEN', 'APPROVED', 4.5, 26, 33, 1100, 418.00, 12, '2026-01-07 10:00:00', NULL, '2026-01-05 10:00:00', '2026-01-07 10:00:00');

-- ---------------------------------------------------------------------
-- 5. 民宿多图 homestay_image(每家 3-4 张)
-- ---------------------------------------------------------------------
INSERT INTO homestay_image (homestay_id, url, sort_order) VALUES
(1, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', 0), (1, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', 1), (1, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', 2),
(2, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', 0), (2, 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', 1), (2, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', 2),
(3, 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', 0), (3, 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', 1), (3, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', 2),
(4, 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', 0), (4, 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', 1), (4, 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', 2),
(5, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', 0), (5, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', 1), (5, 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', 2),
(6, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', 0), (6, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', 1), (6, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', 2),
(7, 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', 0), (7, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', 1), (7, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', 2),
(8, 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', 0), (8, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', 1), (8, 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', 2),
(9, 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', 0), (9, 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', 1),
(10, 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', 0), (10, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', 1),
(11, 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', 0), (11, 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', 1),
(12, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', 0), (12, 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', 1);

-- ---------------------------------------------------------------------
-- 6. 房型 room_type(25 个,覆盖大床/双床/家庭/观景;stock 与房间数一致)
-- ---------------------------------------------------------------------
INSERT INTO room_type (id, homestay_id, name, cover_image, bed_type, max_guests, area, has_breakfast, price, room_count, stock, intro, facilities, status) VALUES
(1,  1, '180°海景大床房', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60',  '1.8米大床',   2, 35.0, 1, 588.00, 3, 3, '正对洱海,落地窗,可看日出', '海景阳台,浴缸,智能马桶', 'ON'),
(2,  1, '海景双床房',   'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60',  '1.2米双床',   2, 30.0, 1, 468.00, 4, 4, '侧海景,适合闺蜜双人', '独立卫浴,mini吧', 'ON'),
(3,  1, '家庭海景套房', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60',  '1.8米大床+1.5米床', 4, 60.0, 1, 888.00, 2, 2, '两室一厅,适合亲子家庭', '客厅,厨房,双卫', 'ON'),
(4,  2, '白族庭院大床房', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '1.8米大床',   2, 25.0, 1, 268.00, 4, 4, '庭院景观,安静舒适', '独立卫浴,茶具', 'ON'),
(5,  2, '古城双床房',   'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60',  '1.2米双床',   2, 22.0, 1, 228.00, 3, 3, '性价比之选', '独立卫浴', 'ON'),
(6,  3, '湖景轻奢大床房', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '1.8米大床',   2, 40.0, 1, 588.00, 3, 3, '全屋智能,湖景浴缸', '投影,智能家居,浴缸', 'ON'),
(7,  3, '湖景露台套房', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60',  '1.8米大床',   2, 55.0, 1, 888.00, 2, 2, '带 360° 湖景露台', '露台,浴缸,音响', 'ON'),
(8,  4, '山景森林木屋', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60',  '1.8米大床',   2, 28.0, 0, 398.00, 4, 4, '木质结构,山景阳台', '阳台,地暖', 'ON'),
(9,  4, '森林家庭木屋', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60',  '1.5米双床',   3, 40.0, 0, 558.00, 2, 2, '可加床,适合亲子', '小厨房,阳台', 'ON'),
(10, 5, '田园景观大床房', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '1.8米大床',   2, 26.0, 1, 328.00, 5, 5, '推窗见稻田', '独立卫浴,衣架', 'ON'),
(11, 5, '亲子田园家庭房', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '1.5米双床',   3, 38.0, 1, 458.00, 3, 3, '儿童友好,带玩具角', '儿童设施,小院', 'ON'),
(12, 6, '双廊海景大床房', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '1.8米大床',   2, 30.0, 1, 458.00, 6, 6, '楼下即网红打卡点', '海景窗,咖啡机', 'ON'),
(13, 6, '海景复式房',   'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60',  '1.8米大床',   3, 50.0, 1, 688.00, 2, 2, 'loft 结构,视野开阔', '露台,沙发床', 'ON'),
(14, 7, '纳西庭院大床房', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '1.8米大床',   2, 24.0, 1, 388.00, 5, 5, '木质老宅,冬暖夏凉', '茶室,庭院', 'ON'),
(15, 7, '古城区双床房', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60',  '1.2米双床',   2, 20.0, 1, 328.00, 3, 3, '古城核心位置', '独立卫浴', 'ON'),
(16, 8, '雪山景观大床房', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '1.8米大床',   2, 30.0, 1, 468.00, 4, 4, '庭院正对雪山', '花园,地暖', 'ON'),
(17, 8, '束河花园家庭房', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', '1.5米双床',   3, 45.0, 1, 668.00, 2, 2, '带花园小院', '花园,烧烤架', 'ON'),
(18, 9, '雪山观景大床房', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '1.8米大床',   2, 22.0, 1, 288.00, 4, 4, '看雪山日落与星空', '地暖,电热毯', 'ON'),
(19, 9, '星空玻璃房',   'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60',  '1.8米大床',   2, 25.0, 0, 388.00, 2, 2, '屋顶可开,躺看星空', '玻璃天窗,地暖', 'ON'),
(20, 10, '古城全景大床房', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '1.8米大床',  2, 32.0, 1, 428.00, 5, 5, '俯瞰整个古城', '观景窗,咖啡', 'ON'),
(21, 10, '观景双床房',  'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60',  '1.2米双床',   2, 28.0, 1, 368.00, 3, 3, '古城与雪山双景', '观景窗', 'ON'),
(22, 11, '翠湖洋房大床房', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '1.8米大床',   2, 30.0, 1, 358.00, 5, 5, '老洋房,红嘴鸥景观', '厨房,洗衣机', 'ON'),
(23, 11, '翠湖双床房',  'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60',  '1.2米双床',   2, 26.0, 1, 298.00, 4, 4, '近文林街咖啡馆', '独立卫浴', 'ON'),
(24, 12, '滇池观景大床房', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '1.8米大床',   2, 35.0, 1, 418.00, 4, 4, '正对海埂大坝', '阳台,洗衣机', 'ON'),
(25, 12, '滇池家庭房',  'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60',  '1.5米双床',   3, 50.0, 1, 588.00, 2, 2, '适合带老人小孩', '双床,客厅', 'ON');

-- ---------------------------------------------------------------------
-- 7. 物理房间 hotel_room(演示房间管理;部分民宿 2 个房型各 2-3 间)
-- ---------------------------------------------------------------------
INSERT INTO hotel_room (homestay_id, room_type_id, room_number, floor, remark) VALUES
(1, 1, '301', '3F', '海景正房'), (1, 1, '302', '3F', NULL), (1, 2, '201', '2F', NULL), (1, 2, '202', '2F', NULL),
(2, 4, '101', '1F', '庭院侧'), (2, 4, '102', '1F', NULL), (2, 5, '201', '2F', NULL),
(7, 14, '201', '2F', NULL), (7, 14, '202', '2F', NULL), (7, 15, '101', '1F', NULL),
(8, 16, '301', '3F', NULL), (8, 16, '302', '3F', NULL), (8, 17, '101', '1F', '带花园'),
(11, 22, '501', '5F', NULL), (11, 22, '502', '5F', NULL), (11, 23, '401', '4F', NULL);

-- ---------------------------------------------------------------------
-- 8. 景点 scenic_spot
-- ---------------------------------------------------------------------
INSERT INTO scenic_spot (id, destination_id, name, cover_image, intro, description, address, longitude, latitude, price, rating, tags, favorite_count, status, create_time, update_time) VALUES
(1, 1, '洱海', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '云南第二大高原淡水湖,环湖 120 公里', '洱海是大理的灵魂。可环海骑行、乘船游湖,沿途经过才村、喜洲、双廊等打卡点,日出日落美不胜收。', '大理市洱海环湖路', 100.2676, 25.6065, 0.00, 4.9, '湖景,骑行,日出,免费', 260, 1, '2026-01-01 11:00:00', '2026-01-01 11:00:00'),
(2, 1, '大理古城', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '南诏国故都,白族风情老街', '大理古城始建于明洪武十五年,城内五华楼、洋人街、复兴路是主要逛点,夜晚的人民路文艺气息浓厚。', '大理市古城区', 100.1673, 25.6923, 0.00, 4.7, '古城,白族,免费,夜生活', 320, 1, '2026-01-01 11:00:00', '2026-01-01 11:00:00'),
(3, 1, '苍山', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '十九峰十八溪,可乘索道观云海', '苍山十九峰连绵 50 公里,可乘洗马潭索道至 3966 米观云海,或徒步玉带路感受苍山洱海同框。', '大理市苍山景区', 100.1300, 25.7200, 80.00, 4.8, '雪山,索道,徒步,云海', 210, 1, '2026-01-01 11:00:00', '2026-01-01 11:00:00'),
(4, 1, '崇圣寺三塔', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '大理标志性古塔群,千年佛寺', '崇圣寺三塔始建于南诏时期,是大理最著名的历史地标。三塔倒影池是最佳拍摄机位。', '大理市三塔路', 100.1460, 25.7060, 75.00, 4.6, '古建筑,佛寺,摄影', 150, 1, '2026-01-01 11:00:00', '2026-01-01 11:00:00'),
(5, 1, '喜洲古镇', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '白族民居建筑博物馆,稻田风光', '喜洲以保存完好的白族民居和金黄稻田闻名,转角楼、题名坊是经典机位,喜洲粑粑必吃。', '大理市喜洲镇', 100.1150, 25.8550, 0.00, 4.7, '古镇,白族民居,稻田,免费', 180, 1, '2026-01-01 11:00:00', '2026-01-01 11:00:00'),
(6, 1, '双廊古镇', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '洱海边最文艺的小镇', '双廊紧邻洱海,玉几岛、南诏风情岛在此,海地生活白房子是网红拍照点。', '大理市双廊镇', 100.2700, 25.8700, 0.00, 4.5, '海景,文艺,网红', 165, 1, '2026-01-01 11:00:00', '2026-01-01 11:00:00'),
(7, 2, '丽江古城', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '世界文化遗产,纳西族古城', '丽江古城依山傍水,四方街、大水车、木府是必打卡点,夜晚酒吧街热闹非凡。', '丽江市古城区', 100.2346, 26.8721, 0.00, 4.7, '古城,纳西,世界遗产', 300, 1, '2026-01-01 11:00:00', '2026-01-01 11:00:00'),
(8, 2, '玉龙雪山', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', '纳西神山,终年积雪', '玉龙雪山 13 座雪峰连绵,可乘大索道至 4506 米,蓝月谷水色湛蓝,是丽江必去。', '丽江市玉龙县', 100.1900, 26.9900, 100.00, 4.8, '雪山,索道,蓝月谷', 280, 1, '2026-01-01 11:00:00', '2026-01-01 11:00:00'),
(9, 2, '束河古镇', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '比大研更安静的茶马古道古镇', '束河是茶马古道上的重镇,比大研古城更安静,青龙桥、四方听音广场值得慢逛。', '丽江市束河古镇', 100.2100, 26.9000, 0.00, 4.6, '古镇,茶马古道,安静', 130, 1, '2026-01-01 11:00:00', '2026-01-01 11:00:00'),
(10, 3, '滇池', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '云南最大淡水湖,冬季红嘴鸥', '滇池又称昆明湖,冬季成千上万红嘴鸥来此过冬,海埂大坝是喂鸥与看落日的最佳地。', '昆明市西山区海埂大坝', 102.6500, 24.9700, 0.00, 4.5, '湖景,海鸥,免费', 190, 1, '2026-01-01 11:00:00', '2026-01-01 11:00:00'),
(11, 3, '石林风景区', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '世界自然遗产,喀斯特地貌奇观', '石林是世界最典型的喀斯特地貌之一,奇石林立,阿诗玛传说流传于此。', '昆明市石林县', 103.2700, 24.8200, 130.00, 4.6, '喀斯特,世界遗产', 120, 1, '2026-01-01 11:00:00', '2026-01-01 11:00:00'),
(12, 3, '翠湖公园', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '昆明市中心的城市绿肺', '翠湖四季皆有景,冬季红嘴鸥聚集,周边是昆明老街与文林街咖啡馆带。', '昆明市五华区翠湖南路', 102.7050, 25.0450, 0.00, 4.6, '公园,海鸥,免费,城市', 110, 1, '2026-01-01 11:00:00', '2026-01-01 11:00:00');

-- ---------------------------------------------------------------------
-- 9. 旅游体验 tourism_experience
-- ---------------------------------------------------------------------
INSERT INTO tourism_experience (id, destination_id, name, cover_image, intro, description, address, longitude, latitude, price, rating, tags, favorite_count, status, create_time, update_time) VALUES
(1, 1, '白族扎染手工体验', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '跟着非遗传承人学扎染,做一块属于自己的布料', '在大理周城体验国家级非遗白族扎染,从扎花、浸染到拆线全程手作,作品可带走。', '大理市周城村扎染坊', 100.1600, 25.8800, 88.00, 4.9, '非遗,手工,亲子', 95, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(2, 1, '洱海环湖骑行', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '沿 120 公里环海路骑行,看遍洱海风光', '提供专业山地车与补给,推荐 40 公里精华段:才村-喜洲-双廊,一路海景与稻田。', '大理市才村码头', 100.2156, 25.6721, 60.00, 4.8, '骑行,海景,运动', 120, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(3, 1, '白族民族服饰旅拍', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', '穿上白族盛装,在古城留下最美瞬间', '含白族传统服饰、妆造与 1 小时跟拍,古城/洱海多个场景可选,精修 9 张。', '大理古城人民路摄影工作室', 100.1673, 25.6923, 299.00, 4.7, '旅拍,民族服饰,闺蜜', 88, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(4, 1, '苍山徒步看日出', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '清晨徒步苍山玉带路,看洱海日出', '本地领队带队,含早餐简餐,行走苍山半山玉带路,俯瞰整个洱海日出。', '大理市苍山感通索道口', 100.1400, 25.7000, 168.00, 4.8, '徒步,日出,自然', 45, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(5, 1, '茶马古道马帮下午茶', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '在百年马厩改造的茶室里听马帮故事', '于喜洲马帮老宅体验云南烤茶与茶点,听掌柜讲茶马古道往事。', '大理市喜洲镇马帮茶室', 100.1150, 25.8550, 68.00, 4.6, '茶文化,历史,文艺', 60, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(6, 2, '纳西东巴纸制作', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '亲手制作千年不腐的东巴纸', '东巴纸是纳西族古老造纸技艺,全程体验抄纸、晾晒与东巴文书写。', '丽江古城东巴纸坊', 100.2346, 26.8721, 98.00, 4.7, '非遗,手工,文化', 55, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(7, 2, '玉龙雪山一日游(含索道)', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '大索道登顶 4506 米,蓝月谷拍大片', '含接送、大索道票、氧气瓶与午餐,专业向导讲解纳西神山文化。', '丽江市玉龙雪山景区', 100.1900, 26.9900, 380.00, 4.8, '雪山,一日游,省心', 150, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(8, 2, '丽江古城旅拍(纳西+雪山)', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '古城夜景与雪山同框的定制旅拍', '含纳西/藏式服饰与妆造,拍摄机位覆盖大水车、万古楼、狮子山。', '丽江市古城区摄影馆', 100.2300, 26.8800, 328.00, 4.6, '旅拍,古城,夜景', 70, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(9, 3, '滇池海鸥喂食+环湖骑行', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '冬天去滇池喂红嘴鸥,春天环湖骑行', '海埂大坝喂鸥 + 环滇池骑行,含自行车与鸥粮。', '昆明市海埂大坝', 102.6500, 24.9700, 45.00, 4.5, '海鸥,骑行,亲子', 40, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(10, 3, '斗南花市鲜花DIY', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '亚洲最大鲜花市场,亲手包一束花', '逛斗南花市,了解鲜花拍卖,亲手扎一束花带走,含花材。', '昆明市斗南花市', 102.7800, 24.8900, 58.00, 4.6, '鲜花,DIY,浪漫', 35, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00');

-- ---------------------------------------------------------------------
-- 10. 餐饮 restaurant
-- ---------------------------------------------------------------------
-- ---------------------------------------------------------------------
-- 10. 餐饮 restaurant(续)
-- ---------------------------------------------------------------------
INSERT INTO restaurant (id, destination_id, name, cover_image, intro, description, address, longitude, latitude, price, rating, tags, favorite_count, status, create_time, update_time) VALUES
(1, 1, '人民路白族风味餐厅', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '酸辣鱼、乳扇、砂锅豆腐', '大理古城人民路上的老字号白族菜馆,主打洱海酸辣鱼与乳扇,本地人也常去。', '大理古城人民路 210 号', 100.1673, 25.6923, 68.00, 4.8, '白族菜,酸辣鱼,乳扇', 160, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(2, 1, '喜洲破酥粑粑老店', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', '喜洲古镇必吃的破酥粑粑', '祖传三代老店,甜咸两种破酥粑粑现烤现卖,外酥里软。', '大理市喜洲镇四方街', 100.1150, 25.8550, 15.00, 4.7, '小吃,粑粑', 130, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(3, 1, '洱海边的湖景咖啡馆', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '面朝洱海发呆一下午', '才村码头的湖景咖啡馆,手冲咖啡配大理蓝,坐拥一线海景。', '大理市才村码头旁', 100.2156, 25.6721, 45.00, 4.8, '咖啡,海景', 110, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(4, 1, '菌子火锅野生菌王', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '云南野生菌火锅,鲜掉眉毛', '选用当季牛肝菌、见手青等野生菌熬汤,搭配土鸡锅底,菌香浓郁。', '大理市下关洱河南路', 100.2500, 25.5800, 128.00, 4.9, '火锅,野生菌', 90, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(5, 1, '双廊巷子口米线', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', '一碗地道的小锅米线', '双廊古镇巷子里的小店,小锅米线酸辣开胃,加卤蛋和酥肉是本地吃法。', '大理市双廊镇', 100.2700, 25.8700, 20.00, 4.5, '米线,小吃', 75, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(6, 2, '纳西火塘腊排骨火锅', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '围着火塘吃腊排骨', '纳西人家最爱的腊排骨火锅,配水性杨花与蘸水,冬天围炉超治愈。', '丽江市古城区象山市场旁', 100.2340, 26.8800, 75.00, 4.8, '腊排骨,火锅,纳西', 95, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(7, 2, '束河雪山景观餐厅', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '边吃纳西菜边看玉龙雪山', '束河古镇内的观景餐厅,二楼露台正对雪山,主打纳西烤肉与鸡豆凉粉。', '丽江市束河古镇中和路', 100.2100, 26.9000, 85.00, 4.6, '观景,纳西菜', 60, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(8, 2, '丽江古城一米阳光酒吧', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '夜晚古城里听民谣喝一杯', '四方街边的老牌民谣酒吧,驻唱歌手嗓音干净,适合旅行尾声放松。', '丽江市古城四方街旁', 100.2346, 26.8721, 90.00, 4.4, '酒吧,民谣,夜生活', 85, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(9, 3, '昆明桥香园过桥米线', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '云南过桥米线老字号', '过桥米线是云南名片,鸡汤滚烫配料丰富,仪式感满满。', '昆明市五华区南屏街', 102.7200, 25.0300, 40.00, 4.6, '米线,老字号', 120, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(10, 3, '翠湖小院滇菜馆', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '翠湖边的滇菜小院,汽锅鸡必点', '藏在小巷里的滇菜馆,招牌汽锅鸡与炸洋芋,坐在小院里吃饭很昆明。', '昆明市翠湖北路小巷', 102.7050, 25.0450, 70.00, 4.7, '滇菜,汽锅鸡', 70, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00');

-- ---------------------------------------------------------------------
-- 11. 民宿-资源关联(创新点①:住这里,玩什么/体验什么/吃什么)
-- ---------------------------------------------------------------------
INSERT INTO homestay_scenic (homestay_id, scenic_id, sort_order) VALUES
(1,1,0),(1,2,1),(1,5,2),(1,6,3),(2,2,0),(2,1,1),(2,4,2),(3,1,0),(3,3,1),(4,3,0),(4,1,1),(5,5,0),(5,2,1),(5,1,2),(6,6,0),(6,1,1),(7,7,0),(7,8,1),(8,9,0),(8,8,1),(9,8,0),(10,7,0),(10,8,1),(11,12,0),(11,10,1),(12,10,0);
INSERT INTO homestay_experience (homestay_id, experience_id, sort_order) VALUES
(1,2,0),(1,3,1),(1,4,2),(2,1,0),(2,3,1),(3,2,0),(4,4,0),(5,5,0),(5,1,1),(6,3,0),(6,2,1),(7,8,0),(7,6,1),(8,7,0),(9,7,0),(10,8,0),(11,10,0),(11,9,1),(12,9,0);
INSERT INTO homestay_restaurant (homestay_id, restaurant_id, sort_order) VALUES
(1,3,0),(1,1,1),(1,4,2),(2,1,0),(2,2,1),(3,3,0),(4,4,0),(5,2,0),(6,5,0),(6,3,1),(7,6,0),(7,8,1),(8,7,0),(8,6,1),(9,6,0),(10,8,0),(11,10,0),(11,9,1),(12,9,0);

-- ---------------------------------------------------------------------
-- 12. 首页轮播 banner
-- ---------------------------------------------------------------------
INSERT INTO banner (id, title, image_url, link_type, link_id, sort_order, status, create_time, update_time) VALUES
(1, '住进大理的风景里', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', 'HOMESTAY', 1, 1, 1, '2026-01-01 12:00:00', '2026-01-01 12:00:00'),
(2, '环洱海,遇见风花雪月', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', 'SCENIC', 1, 2, 1, '2026-01-01 12:00:00', '2026-01-01 12:00:00'),
(3, '丽江古城,慢下来的时光', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', 'HOMESTAY', 7, 3, 1, '2026-01-01 12:00:00', '2026-01-01 12:00:00'),
(4, '滇池海鸥季,春城暖冬', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', 'SCENIC', 10, 4, 1, '2026-01-01 12:00:00', '2026-01-01 12:00:00');

-- ---------------------------------------------------------------------
-- 13. 收藏 favorite(user01/user02 收藏若干,用于推荐偏好与收藏页演示)
-- ---------------------------------------------------------------------
INSERT INTO favorite (user_id, target_type, target_id, create_time) VALUES
(6, 1, 1, '2026-03-02 10:00:00'), (6, 1, 3, '2026-03-02 10:05:00'), (6, 2, 1, '2026-03-03 10:00:00'), (6, 3, 2, '2026-03-03 11:00:00'), (6, 4, 3, '2026-03-03 12:00:00'),
(7, 1, 7, '2026-03-05 10:00:00'), (7, 2, 8, '2026-03-05 11:00:00'), (7, 3, 7, '2026-03-06 10:00:00'),
(8, 1, 5, '2026-03-06 10:00:00'), (8, 2, 5, '2026-03-06 11:00:00');
-- 同步民宿/景点收藏数(上面收藏过的)
UPDATE homestay SET favorite_count = favorite_count + 1 WHERE id IN (1,3,7,5);
UPDATE scenic_spot SET favorite_count = favorite_count + 1 WHERE id IN (1,8,5);
UPDATE tourism_experience SET favorite_count = favorite_count + 1 WHERE id IN (2,7);
UPDATE restaurant SET favorite_count = favorite_count + 1 WHERE id IN (3);

-- ---------------------------------------------------------------------
-- 14. 订单 orders(覆盖 待支付/已支付/入住中/已完成/已取消;日期相对今天生成)
-- 注意:待支付订单 PENDING 超过 15 分钟会被定时任务自动取消,因此真正演示用的
--       "待支付" 由用户在前台新下,这里保留少量已支付/历史完成订单支撑统计与评价。
-- 金额: 晚数x单价x间数
-- ---------------------------------------------------------------------
-- user01(id6) 历史已完成订单(支撑评价 + 商家统计)
INSERT INTO orders (order_no, user_id, homestay_id, room_type_id, homestay_name, homestay_cover, room_type_name, check_in_date, check_out_date, nights, room_count, unit_price, total_amount, discount_amount, pay_amount, contact_name, contact_phone, status, pay_status, pay_type, create_time, pay_time, check_in_time, check_out_time, complete_time) VALUES
('HS202603010001', 6, 1, 1, '洱海之恋海景度假民宿', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '180°海景大床房', DATE_ADD(CURDATE(), INTERVAL -10 DAY), DATE_ADD(CURDATE(), INTERVAL -8 DAY), 2, 1, 588.00, 1176.00, 0.00, 1176.00, '小鹿', '13800000006', 'COMPLETED', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -12 DAY), DATE_ADD(NOW(), INTERVAL -12 DAY), DATE_ADD(NOW(), INTERVAL -10 DAY), DATE_ADD(NOW(), INTERVAL -8 DAY), DATE_ADD(NOW(), INTERVAL -8 DAY));
INSERT INTO orders (order_no, user_id, homestay_id, room_type_id, homestay_name, homestay_cover, room_type_name, check_in_date, check_out_date, nights, room_count, unit_price, total_amount, discount_amount, pay_amount, contact_name, contact_phone, status, pay_status, pay_type, create_time, pay_time, check_in_time, check_out_time, complete_time) VALUES
('HS202602200001', 7, 7, 14, '丽江古城纳西四合院客栈', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '纳西庭院大床房', DATE_ADD(CURDATE(), INTERVAL -20 DAY), DATE_ADD(CURDATE(), INTERVAL -18 DAY), 2, 1, 388.00, 776.00, 0.00, 776.00, '阿凯', '13800000007', 'COMPLETED', 'PAID', 'ALIPAY', DATE_ADD(NOW(), INTERVAL -22 DAY), DATE_ADD(NOW(), INTERVAL -22 DAY), DATE_ADD(NOW(), INTERVAL -20 DAY), DATE_ADD(NOW(), INTERVAL -18 DAY), DATE_ADD(NOW(), INTERVAL -18 DAY));
INSERT INTO orders (order_no, user_id, homestay_id, room_type_id, homestay_name, homestay_cover, room_type_name, check_in_date, check_out_date, nights, room_count, unit_price, total_amount, discount_amount, pay_amount, contact_name, contact_phone, status, pay_status, pay_type, create_time, pay_time, check_in_time, check_out_time, complete_time) VALUES
('HS202603150001', 8, 3, 6, '才村码头湖景轻奢度假屋', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', '湖景轻奢大床房', DATE_ADD(CURDATE(), INTERVAL -3 DAY), DATE_ADD(CURDATE(), INTERVAL -1 DAY), 2, 1, 588.00, 1176.00, 0.00, 1176.00, '小满', '13800000008', 'CHECKED_IN', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -4 DAY), DATE_ADD(NOW(), INTERVAL -4 DAY), DATE_ADD(NOW(), INTERVAL -3 DAY), NULL, NULL);
INSERT INTO orders (order_no, user_id, homestay_id, room_type_id, homestay_name, homestay_cover, room_type_name, check_in_date, check_out_date, nights, room_count, unit_price, total_amount, discount_amount, pay_amount, contact_name, contact_phone, status, pay_status, pay_type, create_time, pay_time, remark) VALUES
('HS202609010001', 6, 1, 1, '洱海之恋海景度假民宿', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '180°海景大床房', DATE_ADD(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 2, 1, 588.00, 1176.00, 0.00, 1176.00, '小鹿', '13800000006', 'PAID', 'PAID', 'WECHAT', NOW(), NOW(), '期待入住,希望安排高层海景房');
-- 已取消订单(演示取消/库存回补)
INSERT INTO orders (order_no, user_id, homestay_id, room_type_id, homestay_name, homestay_cover, room_type_name, check_in_date, check_out_date, nights, room_count, unit_price, total_amount, discount_amount, pay_amount, contact_name, contact_phone, status, pay_status, create_time, cancel_time) VALUES
('HS202607010001', 9, 2, 4, '大理古城云见小院', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '白族庭院大床房', DATE_ADD(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 4 DAY), 1, 1, 268.00, 268.00, 0.00, 268.00, '亲子游妈妈', '13800000009', 'CANCELLED', 'UNPAID', DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY));
-- 近 6 个月更多已完成订单(为统计曲线提供数据,分散到各家民宿)
INSERT INTO orders (order_no, user_id, homestay_id, room_type_id, homestay_name, homestay_cover, room_type_name, check_in_date, check_out_date, nights, room_count, unit_price, total_amount, discount_amount, pay_amount, contact_name, contact_phone, status, pay_status, pay_type, create_time, pay_time, complete_time) VALUES
('HS202604010001', 10, 2, 4, '大理古城云见小院', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '白族庭院大床房', DATE_ADD(CURDATE(), INTERVAL -30 DAY), DATE_ADD(CURDATE(), INTERVAL -28 DAY), 2, 1, 268.00, 536.00, 0.00, 536.00, '老周', '13800000010', 'COMPLETED', 'PAID', 'BANK', DATE_ADD(NOW(), INTERVAL -32 DAY), DATE_ADD(NOW(), INTERVAL -32 DAY), DATE_ADD(NOW(), INTERVAL -28 DAY)),
('HS202604050001', 11, 6, 12, '双廊海地生活精品客栈', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', '双廊海景大床房', DATE_ADD(CURDATE(), INTERVAL -28 DAY), DATE_ADD(CURDATE(), INTERVAL -27 DAY), 1, 2, 458.00, 916.00, 0.00, 916.00, '旅拍少女', '13800000011', 'COMPLETED', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -29 DAY), DATE_ADD(NOW(), INTERVAL -29 DAY), DATE_ADD(NOW(), INTERVAL -27 DAY)),
('HS202605010001', 12, 4, 8, '苍山脚下半山听风别院', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '山景森林木屋', DATE_ADD(CURDATE(), INTERVAL -25 DAY), DATE_ADD(CURDATE(), INTERVAL -24 DAY), 1, 1, 398.00, 398.00, 0.00, 398.00, '骑行侠', '13800000012', 'COMPLETED', 'PAID', 'ALIPAY', DATE_ADD(NOW(), INTERVAL -26 DAY), DATE_ADD(NOW(), INTERVAL -26 DAY), DATE_ADD(NOW(), INTERVAL -24 DAY)),
('HS202605150001', 10, 8, 16, '束河雪山脚下花园民宿', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '雪山景观大床房', DATE_ADD(CURDATE(), INTERVAL -20 DAY), DATE_ADD(CURDATE(), INTERVAL -18 DAY), 2, 1, 468.00, 936.00, 0.00, 936.00, '老周', '13800000010', 'COMPLETED', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -21 DAY), DATE_ADD(NOW(), INTERVAL -21 DAY), DATE_ADD(NOW(), INTERVAL -18 DAY)),
('HS202606010001', 7, 10, 20, '古城狮子山观景云端客栈', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '古城全景大床房', DATE_ADD(CURDATE(), INTERVAL -16 DAY), DATE_ADD(CURDATE(), INTERVAL -15 DAY), 1, 1, 428.00, 428.00, 0.00, 428.00, '阿凯', '13800000007', 'COMPLETED', 'PAID', 'BANK', DATE_ADD(NOW(), INTERVAL -17 DAY), DATE_ADD(NOW(), INTERVAL -17 DAY), DATE_ADD(NOW(), INTERVAL -15 DAY)),
('HS202606200001', 11, 11, 22, '昆明翠湖小满花园民宿', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '翠湖洋房大床房', DATE_ADD(CURDATE(), INTERVAL -12 DAY), DATE_ADD(CURDATE(), INTERVAL -11 DAY), 1, 1, 358.00, 358.00, 0.00, 358.00, '旅拍少女', '13800000011', 'COMPLETED', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -13 DAY), DATE_ADD(NOW(), INTERVAL -13 DAY), DATE_ADD(NOW(), INTERVAL -11 DAY)),
('HS202607100001', 12, 12, 24, '滇池海埂暖冬度假民宿', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '滇池观景大床房', DATE_ADD(CURDATE(), INTERVAL -8 DAY), DATE_ADD(CURDATE(), INTERVAL -6 DAY), 2, 1, 418.00, 836.00, 0.00, 836.00, '骑行侠', '13800000012', 'COMPLETED', 'PAID', 'ALIPAY', DATE_ADD(NOW(), INTERVAL -9 DAY), DATE_ADD(NOW(), INTERVAL -9 DAY), DATE_ADD(NOW(), INTERVAL -6 DAY));

-- ---------------------------------------------------------------------
-- 15. 评价 review(对应已完成订单 order_id=1,2,6..11)
-- ---------------------------------------------------------------------
INSERT INTO review (order_id, user_id, homestay_id, rating, hygiene_score, service_score, facility_score, location_score, content, images, merchant_reply, reply_time, status, create_time, update_time) VALUES
(1, 6, 1, 5, 5, 5, 5, 5, '太喜欢了!房间正对洱海,早上在阳台看日出,老板娘还帮我们规划了环海骑行路线,早餐的米线也超好吃。下次还来!', '["https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60"]', '感谢您的认可!欢迎下次再来,淡季来可以安排更好的海景房~', DATE_ADD(NOW(), INTERVAL -7 DAY), 0, DATE_ADD(NOW(), INTERVAL -8 DAY), DATE_ADD(NOW(), INTERVAL -7 DAY)),
(2, 7, 7, 5, 5, 4, 5, 4, '纳西老宅很有味道,院子安静,晚上在茶室喝茶很惬意。位置稍难找但掌柜会来接。', NULL, '谢谢阿凯,古城里巷子多,下次提前打电话我们到路口接您!', DATE_ADD(NOW(), INTERVAL -17 DAY), 0, DATE_ADD(NOW(), INTERVAL -18 DAY), DATE_ADD(NOW(), INTERVAL -17 DAY)),
(3, 8, 3, 4, 5, 4, 5, 4, '湖景一流,智能家居很方便。扣一分因为周末入住的人多,早餐稍等了一会儿。', '["https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60"]', '感谢反馈,周末早餐我们已增加人手,期待您再来体验露台房~', DATE_ADD(NOW(), INTERVAL -2 DAY), 0, DATE_ADD(NOW(), INTERVAL -3 DAY), DATE_ADD(NOW(), INTERVAL -2 DAY)),
(4, 10, 2, 5, 4, 5, 4, 5, '古城里闹中取静,院子里的绣球花开得很好,老板人很实在。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -28 DAY), DATE_ADD(NOW(), INTERVAL -28 DAY)),
(5, 11, 6, 4, 4, 5, 4, 5, '位置就在双廊核心,下楼就是网红拍照点,房间稍微小了点但干净。', '["https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60"]', NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -27 DAY), DATE_ADD(NOW(), INTERVAL -27 DAY)),
(6, 12, 4, 5, 5, 5, 5, 4, '半山森林民宿太治愈了,早上被鸟叫醒,徒步苍山的领队很专业。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -24 DAY), DATE_ADD(NOW(), INTERVAL -24 DAY)),
(7, 7, 10, 4, 5, 4, 4, 5, '在狮子山顶看古城全景,日出超美,价格略高但值得。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -15 DAY), DATE_ADD(NOW(), INTERVAL -15 DAY)),
(8, 11, 11, 5, 5, 5, 4, 5, '翠湖边的小洋房,冬天阳台就能看到红嘴鸥,楼下就是咖啡馆,太惬意了。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -11 DAY), DATE_ADD(NOW(), INTERVAL -11 DAY)),
(9, 12, 12, 4, 4, 5, 4, 5, '海埂大坝附近,傍晚看滇池落日很浪漫,房间干净暖和。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -6 DAY), DATE_ADD(NOW(), INTERVAL -6 DAY));
-- 民宿评分与评分人数按评价同步(近似)
UPDATE homestay SET rating = 4.8, rating_count = 86 WHERE id = 1;
UPDATE homestay SET rating = 4.6, rating_count = 54 WHERE id = 2;
UPDATE homestay SET rating = 4.9, rating_count = 42 WHERE id = 3;
UPDATE homestay SET rating = 4.7, rating_count = 33 WHERE id = 4;
UPDATE homestay SET rating = 4.7, rating_count = 75 WHERE id = 7;
UPDATE homestay SET rating = 4.5, rating_count = 38 WHERE id = 10;
UPDATE homestay SET rating = 4.6, rating_count = 30 WHERE id = 11;
UPDATE homestay SET rating = 4.5, rating_count = 26 WHERE id = 12;
UPDATE homestay SET rating = 4.5, rating_count = 28 WHERE id = 5;
UPDATE homestay SET rating = 4.4, rating_count = 60 WHERE id = 6;
UPDATE homestay SET rating = 4.8, rating_count = 49 WHERE id = 8;
UPDATE homestay SET rating = 4.6, rating_count = 22 WHERE id = 9;

-- ---------------------------------------------------------------------
-- 16. 旅行计划(创新点②:user01 的"大理三日游",含 民宿+景点+体验+餐饮)
-- ---------------------------------------------------------------------
INSERT INTO travel_plan (id, user_id, plan_name, destination_id, destination_name, start_date, end_date, intro, status, create_time, update_time) VALUES
(1, 6, '大理三日游·风花雪月', 1, '大理', DATE_ADD(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), '第一次去大理,想住海景民宿,环洱海骑行,体验扎染和民族服饰旅拍,吃遍大理美食。', 'ONGOING', DATE_ADD(NOW(), INTERVAL -3 DAY), DATE_ADD(NOW(), INTERVAL -3 DAY)),
(2, 7, '丽江雪山周末', 2, '丽江', DATE_ADD(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY), '周末去丽江看雪山逛古城。', 'ONGOING', DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY));

-- 计划1: 大理三日游 行程项目
INSERT INTO travel_plan_item (plan_id, day_number, item_type, item_id, item_name, start_time, end_time, remark, sort_order) VALUES
(1, 1, 'HOMESTAY', 1, '洱海之恋海景度假民宿', '14:00:00', NULL, '办理入住,放行李', 1),
(1, 1, 'SCENIC', 1, '洱海', '16:00:00', '18:30:00', '傍晚环海骑行看日落', 2),
(1, 1, 'RESTAURANT', 3, '洱海边的湖景咖啡馆', '19:00:00', '21:00:00', '湖景晚餐+咖啡', 3),
(1, 2, 'SCENIC', 2, '大理古城', '09:30:00', '12:00:00', '逛人民路、五华楼', 1),
(1, 2, 'EXPERIENCE', 3, '白族民族服饰旅拍', '13:00:00', '15:00:00', '古城民族服饰写真', 2),
(1, 2, 'RESTAURANT', 1, '人民路白族风味餐厅', '18:00:00', '19:30:00', '晚餐:酸辣鱼+乳扇', 3),
(1, 3, 'EXPERIENCE', 1, '白族扎染手工体验', '09:00:00', '11:00:00', '周城扎染,作品带走', 1),
(1, 3, 'SCENIC', 3, '苍山', '13:00:00', '17:00:00', '乘索道上苍山看洱海全景', 2);
-- 计划2: 丽江周末 行程项目
INSERT INTO travel_plan_item (plan_id, day_number, item_type, item_id, item_name, start_time, end_time, remark, sort_order) VALUES
(2, 1, 'HOMESTAY', 7, '丽江古城纳西四合院客栈', '15:00:00', NULL, '入住,晚上逛四方街', 1),
(2, 2, 'SCENIC', 8, '玉龙雪山', '08:00:00', '16:00:00', '大索道+蓝月谷', 1),
(2, 2, 'EXPERIENCE', 7, '玉龙雪山一日游(含索道)', '08:00:00', '16:00:00', '跟团更省心(备选)', 2),
(2, 2, 'RESTAURANT', 6, '纳西火塘腊排骨火锅', '18:30:00', '20:00:00', '晚餐:腊排骨火锅', 3);

-- ---------------------------------------------------------------------
-- 17. 举报 report(user01 举报一家民宿虚假信息,用于管理员演示处理流)
-- ---------------------------------------------------------------------
INSERT INTO report (id, report_type, target_id, reason, description, reporter_id, status, handle_time, handle_remark, create_time, update_time) VALUES
(1, 1, 6, '虚假信息', '页面宣传说"网红白房子"但实际照片与房间不符,海景被遮挡,存在误导。', 6, 'PENDING', NULL, NULL, DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY)),
(2, 2, 5, '服务问题', '该评价存在明显刷分嫌疑,内容与入住体验不符。', 7, 'HANDLING', DATE_ADD(NOW(), INTERVAL -2 HOUR), '正在核实评价真实性', DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -2 HOUR));

-- ---------------------------------------------------------------------
-- 18. 房型日期库存(为所有上架房型生成未来 61 天库存,数量=房间数)
--     商家新增房型时后端会自动生成;此处在初始化数据中一并预置
-- ---------------------------------------------------------------------
INSERT INTO room_inventory (room_type_id, stay_date, available_count, create_time, update_time)
SELECT rt.id, d.dt, rt.room_count, NOW(), NOW()
FROM room_type rt
JOIN (
  WITH RECURSIVE seq AS (
    SELECT CURDATE() AS dt
    UNION ALL
    SELECT DATE_ADD(dt, INTERVAL 1 DAY) FROM seq WHERE dt < DATE_ADD(CURDATE(), INTERVAL 60 DAY)
  )
  SELECT dt FROM seq
) d
WHERE rt.status = 'ON'
  AND rt.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM room_inventory inv WHERE inv.room_type_id = rt.id AND inv.stay_date = d.dt);
