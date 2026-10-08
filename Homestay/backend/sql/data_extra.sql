-- =====================================================================
-- 旅游民宿综合服务平台 - 补充演示数据(在 data.sql 之后执行)
-- 说明:追加 INSERT,不修改 data.sql 已有数据;统一密码 123456
-- 图片:不同 seed 的 picsum 占位图,避免与基础数据重复
-- =====================================================================
USE tourism_homestay;

-- ---------------------------------------------------------------------
-- 1. 补充用户(user09~user18 共 10 个)
-- ---------------------------------------------------------------------
INSERT INTO `user` (id, username, password, nickname, phone, avatar, gender, email, role, status, create_time, update_time) VALUES
(13, 'user08', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '古城漫步者', '13800000013', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', 1, 'u8@tourism.com', 'USER', 0, '2026-03-10 09:00:00', '2026-03-10 09:00:00'),
(14, 'user09', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '米线爱好者', '13800000014', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', 2, 'u9@tourism.com', 'USER', 0, '2026-03-12 09:00:00', '2026-03-12 09:00:00'),
(15, 'user10', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '山野徒步客', '13800000015', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', 1, 'u10@tourism.com', 'USER', 0, '2026-03-15 09:00:00', '2026-03-15 09:00:00'),
(16, 'user11', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '奶茶不加冰', '13800000016', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', 2, 'u11@tourism.com', 'USER', 0, '2026-03-18 09:00:00', '2026-03-18 09:00:00'),
(17, 'user12', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '毕业旅行团', '13800000017', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', 1, 'u12@tourism.com', 'USER', 0, '2026-03-20 09:00:00', '2026-03-20 09:00:00'),
(18, 'user13', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '候鸟旅居客', '13800000018', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', 2, 'u13@tourism.com', 'USER', 0, '2026-03-22 09:00:00', '2026-03-22 09:00:00'),
(19, 'user14', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '周末出逃记', '13800000019', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', 1, 'u14@tourism.com', 'USER', 0, '2026-03-25 09:00:00', '2026-03-25 09:00:00'),
(20, 'user15', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '咖啡续命人', '13800000020', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', 2, 'u15@tourism.com', 'USER', 0, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(21, 'user16', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '自驾环滇', '13800000021', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', 1, 'u16@tourism.com', 'USER', 0, '2026-04-05 09:00:00', '2026-04-05 09:00:00'),
(22, 'user17', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '手账女孩', '13800000022', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', 2, 'u17@tourism.com', 'USER', 0, '2026-04-08 09:00:00', '2026-04-08 09:00:00');

-- ---------------------------------------------------------------------
-- 2. 补充商家:西双版纳商家(已通过)
-- ---------------------------------------------------------------------
INSERT INTO merchant (id, user_id, merchant_name, contact_name, contact_phone, license_no, description, status, audit_time, audit_remark, create_time, update_time) VALUES
(5, 23, '版纳雨林度假民宿', '玉叫', '13800000023', '91532800MA6BN', '西双版纳热带雨林风格民宿,近告庄西双景与植物园。', 'APPROVED', '2026-03-06 10:00:00', '审核通过', '2026-03-05 09:00:00', '2026-03-06 10:00:00');
INSERT INTO `user` (id, username, password, nickname, phone, avatar, gender, email, role, status, create_time, update_time) VALUES
(23, 'merchant_banna', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '版纳雨林', '13800000023', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', 0, 'mer5@tourism.com', 'MERCHANT', 0, '2026-03-05 09:00:00', '2026-03-05 09:00:00');

-- ---------------------------------------------------------------------
-- 3. 补充民宿 6 家(13-14 大理 / 15 丽江 / 16 昆明 / 17-18 版纳)
-- ---------------------------------------------------------------------
INSERT INTO homestay (id, merchant_id, destination_id, name, cover_image, address, longitude, latitude, intro, description, features, facilities, phone, business_status, audit_status, rating, rating_count, favorite_count, view_count, min_price, sort_order, audit_time, create_time, update_time) VALUES
(13, 1, 1, '大理·风花雪月海景客栈', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '大理市银梭岛码头旁 8 号', 100.2850, 25.6100, '洱海最安静一隅的海景客栈', '藏在银梭岛附近的海景客栈,远离古城喧嚣。庭院种满三角梅,每间房都有洱海view,含免费环海旅拍服务。', '海景,安静,三角梅,旅拍', 'WiFi,停车场,旅拍,早餐', '0872-8888013', 'OPEN', 'APPROVED', 4.7, 38, 55, 1600, 398.00, 13, '2026-03-08 10:00:00', '2026-03-07 10:00:00', '2026-03-08 10:00:00'),
(14, 1, 1, '大理古城·南诏小筑', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '大理古城南门文献路 33 号', 100.1620, 25.6880, '南门附近的文艺小院,步行即达古城', '距古城南门 200 米的精致小院,ins 风装修,顶楼露台可看苍山。公共区有书吧与猫。', '文艺,猫咖,露台,古城', 'WiFi,书吧,洗衣机,早餐', '0872-8888014', 'OPEN', 'APPROVED', 4.6, 52, 70, 1900, 298.00, 14, '2026-03-08 10:00:00', '2026-03-07 11:00:00', '2026-03-08 10:00:00'),
(15, 2, 2, '丽江·雪山营地野奢民宿', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', '丽江市玉龙县白沙镇雪山脚下', 100.1850, 27.0200, '雪山脚下的野奢帐篷营地', '以野奢帐篷为特色的营地民宿,直面玉龙雪山,夜晚可观银河。含营地晚餐与篝火晚会。', '野奢,帐篷,星空,篝火', 'WiFi,营地餐,篝火,电暖', '0888-6666015', 'OPEN', 'APPROVED', 4.8, 30, 60, 2200, 588.00, 15, '2026-03-09 10:00:00', '2026-03-08 10:00:00', '2026-03-09 10:00:00'),
(16, 3, 3, '昆明·西山茶马花街民宿', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '昆明市西山区茶马花街 6 号', 102.6300, 24.9800, '西山脚下的茶文化民宿', '位于西山茶马花街的茶主题民宿,每间房配茶席,可预约茶艺课。步行可达滇池索道。', '茶文化,西山,花街,安静', 'WiFi,茶室,停车场', '0871-3333016', 'OPEN', 'APPROVED', 4.5, 22, 32, 900, 328.00, 16, '2026-03-10 10:00:00', '2026-03-09 10:00:00', '2026-03-10 10:00:00'),
(17, 5, 4, '版纳·雨林秘境度假民宿', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '西双版纳景洪市告庄西双景', 100.7900, 22.0100, '热带雨林风,出门即星光夜市', '告庄西双景内的雨林风民宿,傣式吊脚楼设计,泳池环绕热带植物。夜晚步行到星光夜市。', '雨林,泳池,傣式,夜市', 'WiFi,泳池,接机,餐厅', '0691-2223017', 'OPEN', 'APPROVED', 4.8, 45, 88, 2800, 468.00, 17, '2026-03-11 10:00:00', '2026-03-10 10:00:00', '2026-03-11 10:00:00'),
(18, 5, 4, '版纳·傣家竹楼体验民宿', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '西双版纳勐罕镇傣族园内', 100.8500, 21.9300, '住进真正的傣家竹楼', '傣族园内的传统竹楼民宿,主人是傣族大姐,可体验傣味烧烤与织锦。楼下就是橄榄坝集市。', '傣族,竹楼,田园,烟火气', 'WiFi,厨房,集市', '0691-2223018', 'OPEN', 'APPROVED', 4.6, 28, 40, 1200, 238.00, 18, '2026-03-11 10:00:00', '2026-03-10 11:00:00', '2026-03-11 10:00:00');

-- ---------------------------------------------------------------------
-- 4. 民宿多图(每家 3 张)
-- ---------------------------------------------------------------------
INSERT INTO homestay_image (homestay_id, url, sort_order) VALUES
(13, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', 0), (13, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', 1), (13, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', 2),
(14, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', 0), (14, 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', 1), (14, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', 2),
(15, 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', 0), (15, 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', 1), (15, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', 2),
(16, 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', 0), (16, 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', 1), (16, 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', 2),
(17, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', 0), (17, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', 1), (17, 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', 2),
(18, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', 0), (18, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', 1), (18, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', 2);

-- ---------------------------------------------------------------------
-- 5. 补充房型(每家 2-3 个,共 15 个,id 26-40)
-- ---------------------------------------------------------------------
INSERT INTO room_type (id, homestay_id, name, cover_image, bed_type, max_guests, area, has_breakfast, price, room_count, stock, intro, facilities, status) VALUES
(26, 13, '洱海景观大床房', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '1.8米大床', 2, 32.0, 1, 398.00, 4, 4, '正面洱海,带小阳台', '阳台,浴缸', 'ON'),
(27, 13, '三角梅庭院房', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '1.5米双床', 2, 28.0, 1, 358.00, 3, 3, '庭院景观,三角梅环绕', '庭院,茶具', 'ON'),
(28, 14, '苍山景观大床房', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '1.8米大床', 2, 25.0, 1, 298.00, 5, 5, '顶楼看苍山日落', '露台,书吧', 'ON'),
(29, 14, 'ins风双床房', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', '1.2米双床', 2, 22.0, 0, 258.00, 4, 4, '网红装修风格', '投影', 'ON'),
(30, 15, '雪山观景帐篷房', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', '2米大圆床', 2, 30.0, 1, 588.00, 6, 6, '正对雪山,夜里看银河', '营地餐,电暖,星空天窗', 'ON'),
(31, 15, '野奢家庭帐篷', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '1.8米床+儿童床', 4, 45.0, 1, 888.00, 3, 3, '可住一家四口', '营地餐,篝火', 'ON'),
(32, 16, '茶香大床房', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '1.8米大床', 2, 26.0, 1, 328.00, 4, 4, '房内配茶席与茶具', '茶室,茶席', 'ON'),
(33, 16, '山景标间', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', '1.2米双床', 2, 24.0, 1, 268.00, 3, 3, '面朝西山', '阳台', 'ON'),
(34, 17, '雨林泳池大床房', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '1.8米大床', 2, 35.0, 1, 468.00, 6, 6, '推窗见泳池与雨林', '泳池,阳台', 'ON'),
(35, 17, '傣式家庭套房', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '1.8米床+榻榻米', 4, 55.0, 1, 688.00, 3, 3, '吊脚楼设计,含客厅', '客厅,浴缸', 'ON'),
(36, 18, '传统竹楼大床房', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '1.8米大床', 2, 20.0, 0, 238.00, 4, 4, '原汁原味傣家竹楼', '阳台,蚊帐', 'ON'),
(37, 18, '田园家庭竹楼', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '1.5米双床', 3, 30.0, 0, 358.00, 2, 2, '楼下即橄榄坝集市', '小厨房', 'ON'),
(38, 15, '雪山星空泡泡房', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '1.8米大床', 2, 26.0, 1, 688.00, 2, 2, '全透明泡泡屋,观星绝佳', '星空天窗,电暖', 'ON'),
(39, 17, '告庄景观双床房', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '1.2米双床', 2, 30.0, 1, 398.00, 5, 5, '俯瞰告庄西双景夜景', '阳台,咖啡机', 'ON'),
(40, 14, '古城全景家庭房', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '1.8米床+1.2米床', 3, 42.0, 1, 458.00, 2, 2, '适合亲子,视野开阔', '露台,儿童玩具', 'ON');

-- ---------------------------------------------------------------------
-- 6. 补充景点(13-18)
-- ---------------------------------------------------------------------
INSERT INTO scenic_spot (id, destination_id, name, cover_image, intro, description, address, longitude, latitude, price, rating, tags, favorite_count, status, create_time, update_time) VALUES
(13, 1, '寂照庵', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '苍山上的最美尼姑庵,多肉与素斋', '寂照庵以满院多肉与精致素斋闻名,从感通索道步行可达,适合半日清修。', '大理市苍山感通寺旁', 100.1500, 25.6950, 0.00, 4.8, '多肉,素斋,清幽', 85, 1, '2026-03-01 10:00:00', '2026-03-01 10:00:00'),
(14, 1, '天龙八部影视城', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '武侠迷打卡地', '以《天龙八部》为蓝本的影视城,可看段王府与珍珑棋局,常有实景演出。', '大理市苍山脚下', 100.1550, 25.7000, 45.00, 4.3, '影视城,武侠,演出', 40, 1, '2026-03-01 10:00:00', '2026-03-01 10:00:00'),
(15, 2, '蓝月谷', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', '玉龙雪山下的蓝色宝石', '蓝月谷水质湛蓝如宝石,是雪山脚下最出片的免费景点(含在大索道景区内)。', '丽江市玉龙雪山景区内', 100.2000, 27.0100, 0.00, 4.9, '蓝月谷,雪山,拍照', 175, 1, '2026-03-01 10:00:00', '2026-03-01 10:00:00'),
(16, 4, '中科院西双版纳热带植物园', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '中国面积最大的植物园', '上千种热带植物,王莲池与百花园是必看,建议留一整天。', '西双版纳勐仑镇', 101.2500, 21.9300, 80.00, 4.8, '植物园,热带,王莲', 120, 1, '2026-03-01 10:00:00', '2026-03-01 10:00:00'),
(17, 4, '告庄西双景·星光夜市', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '东南亚风情的夜间集市', '大金塔下的星光夜市,傣味小吃、手作、旅拍云集,夜景极美,免费入场。', '西双版纳景洪市告庄', 100.7950, 22.0150, 0.00, 4.7, '夜市,东南亚,夜景', 200, 1, '2026-03-01 10:00:00', '2026-03-01 10:00:00'),
(18, 4, '野象谷', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', '与亚洲象近距离接触', '可看野象表演、雨林牧象,栈道观象需运气,建议上午去。', '西双版纳勐养镇', 100.8900, 22.1600, 60.00, 4.5, '野象,雨林,亲子', 95, 1, '2026-03-01 10:00:00', '2026-03-01 10:00:00');

-- ---------------------------------------------------------------------
-- 7. 补充体验(11-15)
-- ---------------------------------------------------------------------
INSERT INTO tourism_experience (id, destination_id, name, cover_image, intro, description, address, longitude, latitude, price, rating, tags, favorite_count, status, create_time, update_time) VALUES
(11, 1, '洱海旅拍·网红机位跟拍', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '环海 4 大网红机位,含无人机航拍', '专业摄影师带队环洱海拍摄,含天空之镜、玻璃球、白桌子等机位,赠航拍小视频。', '大理市环海东路集合', 100.2700, 25.6200, 399.00, 4.7, '旅拍,航拍,网红机位', 66, 1, '2026-03-02 10:00:00', '2026-03-02 10:00:00'),
(12, 1, '白族三道茶体验', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '一苦二甜三回味,白族待客之道', '在白族人家体验三道茶仪式,配喜洲粑粑与乳扇,听白族大妈讲茶俗。', '大理市喜洲镇', 100.1150, 25.8550, 48.00, 4.6, '茶文化,白族,仪式感', 44, 1, '2026-03-02 10:00:00', '2026-03-02 10:00:00'),
(13, 4, '傣族泼水节体验', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '天天都是泼水节', '在傣族园体验小型泼水狂欢,含傣装租赁与水枪,结束后看傣族歌舞表演。', '西双版纳傣族园', 100.8500, 21.9300, 128.00, 4.8, '泼水节,傣族,狂欢', 80, 1, '2026-03-02 10:00:00', '2026-03-02 10:00:00'),
(14, 4, '热带雨林徒步+树降', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', '穿越雨林,玩树降与荡秋千', '向导带你深入基诺山雨林,识别草药、玩高空树降,含雨林大餐。', '西双版纳基诺山', 101.0200, 22.0300, 268.00, 4.9, '雨林,徒步,树降', 58, 1, '2026-03-02 10:00:00', '2026-03-02 10:00:00'),
(15, 4, '傣味烧烤晚宴+傣装写真', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '香茅草烤鱼配星光夜市夜景', '傍晚换傣装逛夜市,专业摄影跟拍,晚餐吃正宗傣味烧烤。', '西双版纳告庄星光夜市', 100.7950, 22.0150, 328.00, 4.6, '傣味,写真,夜市', 52, 1, '2026-03-02 10:00:00', '2026-03-02 10:00:00');

-- ---------------------------------------------------------------------
-- 8. 补充餐饮(11-15)
-- ---------------------------------------------------------------------
INSERT INTO restaurant (id, destination_id, name, cover_image, intro, description, address, longitude, latitude, price, rating, tags, favorite_count, status, create_time, update_time) VALUES
(11, 1, '寂照庵素斋', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '苍山上的清雅素斋', '寂照庵素斋按位供应,菜品随季节变化,需提前预约,配茶歇更佳。', '大理市苍山寂照庵', 100.1500, 25.6950, 30.00, 4.8, '素斋,清幽,预约', 65, 1, '2026-03-02 10:00:00', '2026-03-02 10:00:00'),
(12, 1, '大理段公子主题餐厅', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '武侠主题的创意滇菜', '以天龙八部为主题的餐厅,招牌"段氏香茅草烤鱼"与"六脉神剑"创意菜,适合年轻人。', '大理古城复兴路 88 号', 100.1670, 25.6910, 95.00, 4.6, '主题餐厅,创意菜,武侠', 88, 1, '2026-03-02 10:00:00', '2026-03-02 10:00:00'),
(13, 4, '曼飞龙烤鸡(告庄店)', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '版纳必吃烤鸡,外焦里嫩', '炭火烤鸡配秘制蘸水,人手一只的网红店,饭点排队建议早到。', '西双版纳告庄西双景', 100.7950, 22.0150, 55.00, 4.7, '烤鸡,网红,傣味', 120, 1, '2026-03-02 10:00:00', '2026-03-02 10:00:00'),
(14, 4, '星光夜市·傣味烧烤摊', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '夜市烟火气,香茅草烤鱼必点', '告庄夜市里的老牌烧烤摊,烤鱼、烤五花、包烧,配老挝冰咖啡,人均超实惠。', '西双版纳告庄星光夜市', 100.7950, 22.0160, 45.00, 4.5, '烧烤,夜市,实惠', 90, 1, '2026-03-02 10:00:00', '2026-03-02 10:00:00'),
(15, 3, '篆新农贸市场小吃街', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '昆明人的菜市场美食天堂', '篆新市场藏着昆明最地道的小吃:豆花米线、烧饵块、甜白酒,人均 20 吃到撑。', '昆明市五华区篆新市场', 102.7100, 25.0400, 25.00, 4.8, '小吃,市井,便宜', 100, 1, '2026-03-02 10:00:00', '2026-03-02 10:00:00');

-- ---------------------------------------------------------------------
-- 9. 民宿-资源关联(新民宿关联新老资源)
-- ---------------------------------------------------------------------
INSERT INTO homestay_scenic (homestay_id, scenic_id, sort_order) VALUES
(13,1,0),(13,2,1),(14,2,0),(14,13,1),(15,8,0),(15,15,1),(16,10,0),(17,17,0),(17,16,1),(17,18,2),(18,17,0),(18,16,1);
INSERT INTO homestay_experience (homestay_id, experience_id, sort_order) VALUES
(13,11,0),(13,3,1),(14,3,0),(14,12,1),(15,7,0),(16,9,0),(17,13,0),(17,14,1),(17,15,2),(18,13,0),(18,14,1);
INSERT INTO homestay_restaurant (homestay_id, restaurant_id, sort_order) VALUES
(13,3,0),(13,1,1),(14,1,0),(14,12,1),(15,6,0),(16,15,0),(17,13,0),(17,14,1),(18,14,0);

-- ---------------------------------------------------------------------
-- 10. 补充收藏
-- ---------------------------------------------------------------------
INSERT INTO favorite (user_id, target_type, target_id, create_time) VALUES
(8, 1, 13, '2026-04-02 10:00:00'), (9, 1, 17, '2026-04-03 10:00:00'), (9, 2, 17, '2026-04-03 11:00:00'),
(10, 1, 15, '2026-04-05 10:00:00'), (10, 3, 14, '2026-04-05 11:00:00'), (11, 4, 13, '2026-04-06 10:00:00'),
(12, 1, 14, '2026-04-07 10:00:00'), (13, 1, 18, '2026-04-08 10:00:00'), (14, 2, 13, '2026-04-09 10:00:00'),
(15, 1, 16, '2026-04-10 10:00:00'), (16, 2, 15, '2026-04-11 10:00:00'), (17, 3, 15, '2026-04-12 10:00:00'),
(18, 1, 13, '2026-04-13 10:00:00'), (19, 4, 14, '2026-04-14 10:00:00'), (20, 1, 17, '2026-04-15 10:00:00');
UPDATE homestay SET favorite_count = favorite_count + 1 WHERE id IN (13,14,15,16,17,18);
UPDATE scenic_spot SET favorite_count = favorite_count + 1 WHERE id IN (13,15,17);
UPDATE tourism_experience SET favorite_count = favorite_count + 1 WHERE id IN (14,15);
UPDATE restaurant SET favorite_count = favorite_count + 1 WHERE id IN (13,14);

-- ---------------------------------------------------------------------
-- 11. 补充订单(新增 8 个已完成/入住中订单,关联新旧民宿)
--     order_no 用固定前缀 + 序号,避免与时间戳冲突;日期相对今天生成
-- ---------------------------------------------------------------------
INSERT INTO orders (order_no, user_id, homestay_id, room_type_id, homestay_name, homestay_cover, room_type_name, check_in_date, check_out_date, nights, room_count, unit_price, total_amount, discount_amount, pay_amount, contact_name, contact_phone, status, pay_status, pay_type, create_time, pay_time, check_in_time, check_out_time, complete_time) VALUES
('HSX20260401001', 8, 13, 26, '大理·风花雪月海景客栈', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '洱海景观大床房', DATE_ADD(CURDATE(), INTERVAL -40 DAY), DATE_ADD(CURDATE(), INTERVAL -38 DAY), 2, 1, 398.00, 796.00, 0.00, 796.00, '小满', '13800000008', 'COMPLETED', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -42 DAY), DATE_ADD(NOW(), INTERVAL -42 DAY), DATE_ADD(NOW(), INTERVAL -40 DAY), DATE_ADD(NOW(), INTERVAL -38 DAY), DATE_ADD(NOW(), INTERVAL -38 DAY)),
('HSX20260402001', 9, 17, 34, '版纳·雨林秘境度假民宿', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '雨林泳池大床房', DATE_ADD(CURDATE(), INTERVAL -35 DAY), DATE_ADD(CURDATE(), INTERVAL -33 DAY), 2, 1, 468.00, 936.00, 0.00, 936.00, '亲子游妈妈', '13800000009', 'COMPLETED', 'PAID', 'ALIPAY', DATE_ADD(NOW(), INTERVAL -36 DAY), DATE_ADD(NOW(), INTERVAL -36 DAY), DATE_ADD(NOW(), INTERVAL -35 DAY), DATE_ADD(NOW(), INTERVAL -33 DAY), DATE_ADD(NOW(), INTERVAL -33 DAY)),
('HSX20260403001', 10, 15, 30, '丽江·雪山营地野奢民宿', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', '雪山观景帐篷房', DATE_ADD(CURDATE(), INTERVAL -30 DAY), DATE_ADD(CURDATE(), INTERVAL -29 DAY), 1, 1, 588.00, 588.00, 0.00, 588.00, '自由行老周', '13800000010', 'COMPLETED', 'PAID', 'BANK', DATE_ADD(NOW(), INTERVAL -31 DAY), DATE_ADD(NOW(), INTERVAL -31 DAY), DATE_ADD(NOW(), INTERVAL -30 DAY), DATE_ADD(NOW(), INTERVAL -29 DAY), DATE_ADD(NOW(), INTERVAL -29 DAY)),
('HSX20260404001', 11, 14, 28, '大理古城·南诏小筑', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '苍山景观大床房', DATE_ADD(CURDATE(), INTERVAL -26 DAY), DATE_ADD(CURDATE(), INTERVAL -25 DAY), 1, 2, 298.00, 596.00, 0.00, 596.00, '旅拍少女', '13800000011', 'COMPLETED', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -27 DAY), DATE_ADD(NOW(), INTERVAL -27 DAY), DATE_ADD(NOW(), INTERVAL -26 DAY), DATE_ADD(NOW(), INTERVAL -25 DAY), DATE_ADD(NOW(), INTERVAL -25 DAY)),
('HSX20260405001', 12, 16, 32, '昆明·西山茶马花街民宿', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '茶香大床房', DATE_ADD(CURDATE(), INTERVAL -22 DAY), DATE_ADD(CURDATE(), INTERVAL -21 DAY), 1, 1, 328.00, 328.00, 0.00, 328.00, '洱海骑行侠', '13800000012', 'COMPLETED', 'PAID', 'ALIPAY', DATE_ADD(NOW(), INTERVAL -23 DAY), DATE_ADD(NOW(), INTERVAL -23 DAY), DATE_ADD(NOW(), INTERVAL -22 DAY), DATE_ADD(NOW(), INTERVAL -21 DAY), DATE_ADD(NOW(), INTERVAL -21 DAY)),
('HSX20260406001', 13, 18, 36, '版纳·傣家竹楼体验民宿', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '传统竹楼大床房', DATE_ADD(CURDATE(), INTERVAL -18 DAY), DATE_ADD(CURDATE(), INTERVAL -17 DAY), 1, 1, 238.00, 238.00, 0.00, 238.00, '古城漫步者', '13800000013', 'COMPLETED', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -19 DAY), DATE_ADD(NOW(), INTERVAL -19 DAY), DATE_ADD(NOW(), INTERVAL -18 DAY), DATE_ADD(NOW(), INTERVAL -17 DAY), DATE_ADD(NOW(), INTERVAL -17 DAY)),
('HSX20260407001', 14, 13, 27, '大理·风花雪月海景客栈', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '三角梅庭院房', DATE_ADD(CURDATE(), INTERVAL -15 DAY), DATE_ADD(CURDATE(), INTERVAL -14 DAY), 1, 1, 358.00, 358.00, 0.00, 358.00, '米线爱好者', '13800000014', 'CHECKED_IN', 'PAID', 'ALIPAY', DATE_ADD(NOW(), INTERVAL -16 DAY), DATE_ADD(NOW(), INTERVAL -16 DAY), DATE_ADD(NOW(), INTERVAL -15 DAY), NULL, NULL),
('HSX20260408001', 15, 17, 39, '版纳·雨林秘境度假民宿', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '告庄景观双床房', DATE_ADD(CURDATE(), INTERVAL -12 DAY), DATE_ADD(CURDATE(), INTERVAL -11 DAY), 1, 1, 398.00, 398.00, 0.00, 398.00, '山野徒步客', '13800000015', 'CHECKED_IN', 'PAID', 'BANK', DATE_ADD(NOW(), INTERVAL -13 DAY), DATE_ADD(NOW(), INTERVAL -13 DAY), DATE_ADD(NOW(), INTERVAL -12 DAY), NULL, NULL),
('HSX20260409001', 16, 15, 38, '丽江·雪山营地野奢民宿', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', '雪山星空泡泡房', DATE_ADD(CURDATE(), INTERVAL -8 DAY), DATE_ADD(CURDATE(), INTERVAL -7 DAY), 1, 1, 688.00, 688.00, 0.00, 688.00, '奶茶不加冰', '13800000016', 'COMPLETED', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -9 DAY), DATE_ADD(NOW(), INTERVAL -9 DAY), DATE_ADD(NOW(), INTERVAL -8 DAY), DATE_ADD(NOW(), INTERVAL -7 DAY), DATE_ADD(NOW(), INTERVAL -7 DAY)),
('HSX20260410001', 17, 14, 40, '大理古城·南诏小筑', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '古城全景家庭房', DATE_ADD(CURDATE(), INTERVAL -6 DAY), DATE_ADD(CURDATE(), INTERVAL -4 DAY), 2, 1, 458.00, 916.00, 0.00, 916.00, '毕业旅行团', '13800000017', 'COMPLETED', 'PAID', 'ALIPAY', DATE_ADD(NOW(), INTERVAL -7 DAY), DATE_ADD(NOW(), INTERVAL -7 DAY), DATE_ADD(NOW(), INTERVAL -6 DAY), DATE_ADD(NOW(), INTERVAL -4 DAY), DATE_ADD(NOW(), INTERVAL -4 DAY)),
('HSX20260411001', 18, 16, 33, '昆明·西山茶马花街民宿', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '山景标间', DATE_ADD(CURDATE(), INTERVAL -5 DAY), DATE_ADD(CURDATE(), INTERVAL -3 DAY), 2, 1, 268.00, 536.00, 0.00, 536.00, '候鸟旅居客', '13800000018', 'CHECKED_IN', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -6 DAY), DATE_ADD(NOW(), INTERVAL -6 DAY), DATE_ADD(NOW(), INTERVAL -5 DAY), NULL, NULL),
('HSX20260412001', 19, 18, 37, '版纳·傣家竹楼体验民宿', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '田园家庭竹楼', DATE_ADD(CURDATE(), INTERVAL -3 DAY), DATE_ADD(CURDATE(), INTERVAL -2 DAY), 1, 1, 358.00, 358.00, 0.00, 358.00, '周末出逃记', '13800000019', 'COMPLETED', 'PAID', 'BANK', DATE_ADD(NOW(), INTERVAL -4 DAY), DATE_ADD(NOW(), INTERVAL -4 DAY), DATE_ADD(NOW(), INTERVAL -3 DAY), DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL -2 DAY)),
('HSX20260413001', 20, 13, 26, '大理·风花雪月海景客栈', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '洱海景观大床房', DATE_ADD(CURDATE(), INTERVAL -1 DAY), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 2, 1, 398.00, 796.00, 0.00, 796.00, '咖啡续命人', '13800000020', 'PAID', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL -2 DAY), NULL, NULL, NULL),
('HSX20260414001', 21, 17, 35, '版纳·雨林秘境度假民宿', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '傣式家庭套房', DATE_ADD(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 2, 1, 688.00, 1376.00, 0.00, 1376.00, '自驾环滇', '13800000021', 'PAID', 'PAID', 'ALIPAY', DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY), NULL, NULL, NULL);

-- ---------------------------------------------------------------------
-- 12. 补充评价(一):新民宿订单评价
-- ---------------------------------------------------------------------
INSERT INTO review (order_id, user_id, homestay_id, rating, hygiene_score, service_score, facility_score, location_score, content, images, merchant_reply, reply_time, status, create_time, update_time) VALUES
(13, 8, 13, 5, 5, 5, 5, 4, '银梭岛这边太安静了,早上被海浪声叫醒。老板帮忙约的旅拍很专业,出片率超高!', '["https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60","https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60"]', '谢谢小满!旅拍是咱家特色,欢迎淡季再来~', DATE_ADD(NOW(), INTERVAL -37 DAY), 0, DATE_ADD(NOW(), INTERVAL -38 DAY), DATE_ADD(NOW(), INTERVAL -37 DAY)),
(14, 9, 17, 5, 5, 5, 5, 5, '带娃来的,泳池孩子玩疯了!推窗就是雨林,晚上步行去星光夜市,强烈推荐家庭出游。', '["https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60"]', '感谢选择雨林秘境,暑期还有雨林夜探活动!', DATE_ADD(NOW(), INTERVAL -32 DAY), 0, DATE_ADD(NOW(), INTERVAL -33 DAY), DATE_ADD(NOW(), INTERVAL -32 DAY)),
(15, 10, 15, 5, 4, 5, 5, 4, '帐篷营地太酷了,晚上真的看到银河!篝火晚会认识几个驴友,第二天一起去了蓝月谷。', NULL, '老周玩得开心就好,欢迎雪季来拍日照金山!', DATE_ADD(NOW(), INTERVAL -28 DAY), 0, DATE_ADD(NOW(), INTERVAL -29 DAY), DATE_ADD(NOW(), INTERVAL -28 DAY)),
(16, 11, 14, 4, 5, 4, 5, 4, '小院很文艺,书吧和猫都很治愈。离古城南门近,房间略小但干净温馨。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -25 DAY), DATE_ADD(NOW(), INTERVAL -25 DAY)),
(17, 12, 16, 5, 5, 5, 5, 4, '茶主题很特别,房内茶席配了三种滇红,老板还送茶艺体验券,昆明中转首选。', NULL, '欢迎茶友再来,春季有绿茶季限定!', DATE_ADD(NOW(), INTERVAL -20 DAY), 0, DATE_ADD(NOW(), INTERVAL -21 DAY), DATE_ADD(NOW(), INTERVAL -20 DAY)),
(18, 13, 18, 5, 4, 5, 5, 4, '住进真正的傣家竹楼,主人超好,教我们做菠萝饭,早上楼下集市水果便宜到哭!', '["https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60"]', '玉叫大姐让我谢谢您,泼水节欢迎再来!', DATE_ADD(NOW(), INTERVAL -16 DAY), 0, DATE_ADD(NOW(), INTERVAL -17 DAY), DATE_ADD(NOW(), INTERVAL -16 DAY)),
(19, 14, 13, 5, 5, 4, 5, 4, '为了拍照来的,果然没失望,庭院三角梅开得正好。早餐米线比古城里很多店都好吃。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -14 DAY), DATE_ADD(NOW(), INTERVAL -14 DAY));

-- ---------------------------------------------------------------------
-- 13. 补充评价(二)
-- ---------------------------------------------------------------------
INSERT INTO review (order_id, user_id, homestay_id, rating, hygiene_score, service_score, facility_score, location_score, content, images, merchant_reply, reply_time, status, create_time, update_time) VALUES
(22, 16, 15, 5, 5, 5, 5, 5, '星空泡泡房此生必住!躺床上看星星,早上雪山就在眼前,值回票价。', '["https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60"]', '泡泡房全营地就两间,能订到就是缘分!', DATE_ADD(NOW(), INTERVAL -6 DAY), 0, DATE_ADD(NOW(), INTERVAL -7 DAY), DATE_ADD(NOW(), INTERVAL -6 DAY)),
(23, 17, 14, 5, 4, 5, 4, 5, '毕业旅行住这,露台看苍山日落绝了,家庭房够我们仨闹腾。', NULL, '祝前程似锦!毕业季对学生有优惠。', DATE_ADD(NOW(), INTERVAL -3 DAY), 0, DATE_ADD(NOW(), INTERVAL -4 DAY), DATE_ADD(NOW(), INTERVAL -3 DAY)),
(25, 19, 18, 5, 4, 4, 5, 5, '周末来版纳放空,竹楼体验很新奇,楼下集市烟火气十足。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY)),
(20, 15, 17, 5, 5, 5, 5, 4, '告庄景观房夜景太美,大金塔亮灯后像在东南亚。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -11 DAY), DATE_ADD(NOW(), INTERVAL -11 DAY)),
(21, 16, 15, 4, 4, 5, 4, 5, '雪山营地体验独特,帐篷隔音一般,景色没得说。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -8 DAY), DATE_ADD(NOW(), INTERVAL -8 DAY)),
(24, 18, 16, 5, 5, 5, 4, 5, '昆明中转住了两晚,茶艺课有意思,买了普洱茶回去。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -2 DAY), DATE_ADD(NOW(), INTERVAL -2 DAY));


-- ---------------------------------------------------------------------
-- 14. 新房型(id 26-40)生成未来 61 天日期库存
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
WHERE rt.id BETWEEN 26 AND 40
  AND rt.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM room_inventory inv WHERE inv.room_type_id = rt.id AND inv.stay_date = d.dt);

-- ---------------------------------------------------------------------
-- 15. 按新评价同步民宿评分与评分人数
-- ---------------------------------------------------------------------
UPDATE homestay SET rating = 4.8, rating_count = 46 WHERE id = 13;
UPDATE homestay SET rating = 4.6, rating_count = 60 WHERE id = 14;
UPDATE homestay SET rating = 4.8, rating_count = 35 WHERE id = 15;
UPDATE homestay SET rating = 4.6, rating_count = 26 WHERE id = 16;
UPDATE homestay SET rating = 4.8, rating_count = 50 WHERE id = 17;
UPDATE homestay SET rating = 4.6, rating_count = 32 WHERE id = 18;
UPDATE homestay SET rating = 4.5, rating_count = 61 WHERE id = 6;
UPDATE homestay SET rating = 4.5, rating_count = 39 WHERE id = 10;
UPDATE homestay SET rating = 4.6, rating_count = 31 WHERE id = 11;
UPDATE homestay SET rating = 4.5, rating_count = 27 WHERE id = 12;
UPDATE homestay SET rating = 4.6, rating_count = 55 WHERE id = 2;
UPDATE homestay SET rating = 4.7, rating_count = 34 WHERE id = 4;
