-- =====================================================================
-- 旅游民宿综合服务平台 - 目的地与数据全面扩充(在 data.sql + data_extra.sql 之后执行)
-- 新增 5 个目的地:香格里拉/腾冲/玉溪抚仙湖/文山普者黑/红河元阳梯田
-- =====================================================================
USE tourism_homestay;

-- ---------------------------------------------------------------------
-- 1. 新增目的地(id 5-9)
-- ---------------------------------------------------------------------
INSERT INTO destination (id, name, province, city, level, cover_image, description, scenic_tags, status, sort_order, create_time, update_time) VALUES
(5, '香格里拉', '云南省', '迪庆藏族自治州', 2, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '消失的地平线,高原雪山与藏地文化。', '雪山,藏式,松赞林寺,普达措', 1, 5, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
(6, '腾冲', '云南省', '保山市', 2, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '火山热海与侨乡古镇,温泉之乡。', '温泉,和顺古镇,火山,银杏', 1, 6, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
(7, '玉溪', '云南省', '玉溪市', 2, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', '抚仙湖畔的湖光山色。', '抚仙湖,帆船,铜锅鱼', 1, 7, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
(8, '普者黑', '云南省', '文山壮族苗族自治州', 2, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '喀斯特山水田园,三生三世取景地。', '荷花,山水,田园,打水仗', 1, 8, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
(9, '元阳', '云南省', '红河哈尼族彝族自治州', 2, 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '哈尼梯田世界遗产,云海日出。', '梯田,云海,哈尼族,日出', 1, 9, '2026-01-01 10:00:00', '2026-01-01 10:00:00');

-- ---------------------------------------------------------------------
-- 2. 新增民宿(id 19-27)
-- ---------------------------------------------------------------------
INSERT INTO homestay (id, merchant_id, destination_id, name, cover_image, address, longitude, latitude, intro, description, features, facilities, phone, business_status, audit_status, rating, rating_count, favorite_count, view_count, min_price, sort_order, audit_time, create_time, update_time) VALUES
(19, 2, 5, '香格里拉·独克宗藏式客栈', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '香格里拉独克宗古城金龙街 12 号', 99.7000, 27.8250, '古城里的藏式老宅,抬头见大佛寺转经筒', '独克宗古城内的藏式夯土民居,木质梁柱雕花精美。天台可看龟山公园大转经筒,夜晚古城灯火尽收眼底。', '藏式,古城,转经筒,温暖', 'WiFi,地暖,茶室,停车场', '0887-8888019', 'OPEN', 'APPROVED', 4.8, 26, 45, 1400, 358.00, 19, '2026-04-02 10:00:00', '2026-04-01 10:00:00', '2026-04-02 10:00:00'),
(20, 2, 5, '香格里拉·松赞林卡雪山庄园', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '香格里拉建塘镇松赞林寺旁 2 公里', 99.7100, 27.8600, '面朝松赞林寺的高原庄园', '藏式庄园风格民宿,房间落地窗正对松赞林寺金顶,清晨可看日照金寺。含藏式早餐与酥油茶体验。', '雪山,庄园,松赞林寺,藏式', 'WiFi,地暖,餐厅,接机', '0887-8888020', 'OPEN', 'APPROVED', 4.9, 18, 40, 1100, 588.00, 20, '2026-04-03 10:00:00', '2026-04-02 11:00:00', '2026-04-03 10:00:00'),
(21, 3, 6, '腾冲·和顺古镇温泉客栈', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '腾冲和顺古镇水碓村 6 号', 98.4600, 25.0100, '百年侨乡古镇里的私汤小院', '和顺古镇内的温泉客栈,每间房带独立私汤池,温泉水引自热海。院子里的百年玉兰春天开花。', '温泉,古镇,私汤,侨乡', 'WiFi,私汤,早餐,茶室', '0875-8888021', 'OPEN', 'APPROVED', 4.8, 32, 55, 1800, 468.00, 21, '2026-04-04 10:00:00', '2026-04-03 10:00:00', '2026-04-04 10:00:00'),
(22, 3, 6, '腾冲·银杏村金色小院', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', '腾冲固东镇银杏村内 18 号', 98.5200, 25.3300, '秋天住进金色银杏童话里', '银杏村内的农家小院,11 月银杏金黄铺满屋顶。院里养了两只鹅,老板娘做的银杏炖鸡一绝。', '银杏,农家,秋景,田园', 'WiFi,厨房,停车场', '0875-8888022', 'OPEN', 'APPROVED', 4.6, 22, 30, 950, 268.00, 22, '2026-04-05 10:00:00', '2026-04-04 10:00:00', '2026-04-05 10:00:00'),
(23, 3, 7, '玉溪·抚仙湖琉璃海景民宿', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '玉溪澄江市抚仙湖东岸 3 号', 102.9100, 24.5500, '躺在房间看抚仙湖的琉璃色', '抚仙湖东岸一线湖景民宿,湖水清澈见底如琉璃。提供帆船体验预约与湖边日落晚餐。', '湖景,帆船,日落,清澈', 'WiFi,停车场,餐厅,咖啡', '0877-8888023', 'OPEN', 'APPROVED', 4.7, 20, 35, 1200, 428.00, 23, '2026-04-06 10:00:00', '2026-04-05 10:00:00', '2026-04-06 10:00:00'),
(24, 3, 8, '普者黑·荷塘月色客栈', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '文山丘北县普者黑村仙人洞 20 号', 104.0600, 24.1300, '开窗就是荷花池与喀斯特峰林', '普者黑核心区的湖景客栈,夏季推窗见万亩荷花。老板可代订游船打水仗,天台看青龙山日出。', '荷花,湖景,喀斯特,田园', 'WiFi,天台,停车场', '0876-8888024', 'OPEN', 'APPROVED', 4.7, 25, 38, 1500, 328.00, 24, '2026-04-07 10:00:00', '2026-04-06 10:00:00', '2026-04-07 10:00:00'),
(25, 3, 8, '普者黑·三生三世湖畔民宿', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', '文山丘北县普者黑菜花箐 3 号', 104.0800, 24.1100, '取景地旁的田园小院', '紧邻三生三世取景地的白墙小院,院里有菜地可摘菜。老板娘做的荷花宴是隐藏菜单。', '田园,取景地,荷花宴', 'WiFi,厨房,停车场', '0876-8888025', 'OPEN', 'APPROVED', 4.5, 15, 22, 700, 238.00, 25, '2026-04-08 10:00:00', '2026-04-07 10:00:00', '2026-04-08 10:00:00'),
(26, 3, 9, '元阳·云上梯田观景民宿', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '红河元阳县多依树景区内 5 号', 102.7700, 23.1100, '房间正对多依树日出观景台', '建在梯田边的观景民宿,清晨躺在床上看云海日出。老板是哈尼族,可约梯田徒步讲解。', '梯田,云海,日出,哈尼族', 'WiFi,观景台,电热毯', '0873-8888026', 'OPEN', 'APPROVED', 4.8, 28, 50, 2000, 398.00, 26, '2026-04-09 10:00:00', '2026-04-08 10:00:00', '2026-04-09 10:00:00'),
(27, 3, 9, '元阳·哈尼蘑菇房民宿', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '红河元阳县箐口村哈尼族村落', 102.7900, 23.0900, '住进世界遗产地的哈尼蘑菇房', '传统哈尼族蘑菇房改造民宿,保留夯土墙与蘑菇顶。夜晚围火塘听哈尼族老人唱古歌。', '哈尼族,蘑菇房,火塘,世界遗产', 'WiFi,火塘,简餐', '0873-8888027', 'OPEN', 'APPROVED', 4.7, 19, 28, 1100, 288.00, 27, '2026-04-10 10:00:00', '2026-04-09 10:00:00', '2026-04-10 10:00:00');

-- ---------------------------------------------------------------------
-- 3. 新增商家(6 滇西文旅 / 7 滇南山水)
-- ---------------------------------------------------------------------
INSERT INTO `user` (id, username, password, nickname, phone, avatar, gender, email, role, status, create_time, update_time) VALUES
(24, 'merchant_dianxi', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '滇西文旅民宿', '13800000024', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', 0, 'mer6@tourism.com', 'MERCHANT', 0, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(25, 'merchant_diannan', '$2a$10$f4d1VYnSGEzpo7NiN33UiOsirAw2/7OFBVNNIZEDbE2UW167mMRyu', '滇南山水民宿', '13800000025', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', 0, 'mer7@tourism.com', 'MERCHANT', 0, '2026-04-01 09:00:00', '2026-04-01 09:00:00');
INSERT INTO merchant (id, user_id, merchant_name, contact_name, contact_phone, license_no, description, status, audit_time, audit_remark, create_time, update_time) VALUES
(6, 24, '滇西文旅民宿管理', '扎西', '13800000024', '91533400MA6DX', '香格里拉与腾冲两地藏式/温泉特色民宿。', 'APPROVED', '2026-04-02 10:00:00', '审核通过', '2026-04-01 09:00:00', '2026-04-02 10:00:00'),
(7, 25, '滇南山水民宿管理', '阿依', '13800000025', '91532600MA6DN', '玉溪抚仙湖、普者黑、元阳梯田山水民宿。', 'APPROVED', '2026-04-02 10:00:00', '审核通过', '2026-04-01 09:00:00', '2026-04-02 10:00:00');

-- 民宿归属修正:19-22 归商家 6(滇西);23-27 归商家 7(滇南)
UPDATE homestay SET merchant_id = 6 WHERE id IN (19, 20, 21, 22);
UPDATE homestay SET merchant_id = 7 WHERE id IN (23, 24, 25, 26, 27);

-- ---------------------------------------------------------------------
-- 4. 民宿多图(19-27 每家 3 张)
-- ---------------------------------------------------------------------
INSERT INTO homestay_image (homestay_id, url, sort_order) VALUES
(19, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', 0), (19, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', 1), (19, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', 2),
(20, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', 0), (20, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', 1), (20, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', 2),
(21, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', 0), (21, 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', 1), (21, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', 2),
(22, 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', 0), (22, 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', 1), (22, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', 2),
(23, 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', 0), (23, 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', 1), (23, 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', 2),
(24, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', 0), (24, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', 1), (24, 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', 2),
(25, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', 0), (25, 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', 1), (25, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', 2),
(26, 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', 0), (26, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', 1), (26, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', 2),
(27, 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', 0), (27, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', 1), (27, 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', 2);

-- ---------------------------------------------------------------------
-- 5. 新房型(id 41-58)
-- ---------------------------------------------------------------------
INSERT INTO room_type (id, homestay_id, name, cover_image, bed_type, max_guests, area, has_breakfast, price, room_count, stock, intro, facilities, status) VALUES
(41, 19, '藏式雕花大床房', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '1.8米大床', 2, 24.0, 1, 358.00, 4, 4, '藏式木雕装饰,带飘窗', '地暖,茶具', 'ON'),
(42, 19, '古城景观双床房', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '1.2米双床', 2, 22.0, 1, 298.00, 3, 3, '窗外是古城屋顶与转经筒', '地暖', 'ON'),
(43, 20, '松赞林寺景观房', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '1.8米大床', 2, 32.0, 1, 588.00, 5, 5, '正对松赞林寺金顶', '地暖,落地窗', 'ON'),
(44, 20, '高原庄园家庭房', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '1.8米床+1.5米床', 4, 48.0, 1, 888.00, 2, 2, '适合家庭,带客厅', '客厅,地暖', 'ON'),
(45, 21, '私汤大床房', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '1.8米大床', 2, 30.0, 1, 468.00, 5, 5, '房内独立温泉私汤池', '私汤,茶具', 'ON'),
(46, 21, '古镇庭院标间', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '1.2米双床', 2, 26.0, 1, 358.00, 4, 4, '庭院景观,百年玉兰旁', '庭院', 'ON'),
(47, 22, '银杏景观大床房', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', '1.8米大床', 2, 22.0, 1, 268.00, 4, 4, '秋天窗外就是金色银杏', '农家', 'ON'),
(48, 22, '农家小院家庭房', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '1.5米双床', 3, 32.0, 1, 328.00, 2, 2, '带独立小院', '小院,厨房', 'ON'),
(49, 23, '琉璃海景大床房', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '1.8米大床', 2, 32.0, 1, 428.00, 5, 5, '一线湖景,看日落', '阳台', 'ON'),
(50, 23, '湖景家庭套房', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '1.8米床+沙发床', 3, 45.0, 1, 628.00, 3, 3, '含客厅,适合亲子', '客厅,阳台', 'ON'),
(51, 24, '荷塘景观大床房', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', '1.8米大床', 2, 25.0, 1, 328.00, 5, 5, '推窗见荷花池', '阳台', 'ON'),
(52, 24, '青龙山景观双床房', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '1.2米双床', 2, 24.0, 1, 288.00, 4, 4, '天台看青龙山日出', '天台', 'ON'),
(53, 25, '田园大床房', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '1.8米大床', 2, 20.0, 1, 238.00, 3, 3, '窗外是菜地与峰林', '厨房', 'ON'),
(54, 25, '取景地景观房', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '1.5米双床', 2, 24.0, 0, 268.00, 2, 2, '远眺三生三世取景地', '天台', 'ON'),
(55, 26, '云海日出观景房', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '1.8米大床', 2, 26.0, 1, 398.00, 5, 5, '清晨床上看云海日出', '观景窗,电热毯', 'ON'),
(56, 26, '梯田景观双床房', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '1.2米双床', 2, 24.0, 1, 328.00, 4, 4, '正对万亩梯田', '观景窗', 'ON'),
(57, 27, '蘑菇房大床房', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '1.8米大床', 2, 18.0, 0, 288.00, 3, 3, '哈尼族传统蘑菇房', '火塘', 'ON'),
(58, 27, '蘑菇房家庭间', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '1.5米双床', 3, 26.0, 0, 358.00, 2, 2, '家庭体验房', '火塘,简餐', 'ON');

-- ---------------------------------------------------------------------
-- 6. 新城市景点(id 19-28)
-- ---------------------------------------------------------------------
INSERT INTO scenic_spot (id, destination_id, name, cover_image, intro, description, address, longitude, latitude, price, rating, tags, favorite_count, status, create_time, update_time) VALUES
(19, 5, '松赞林寺', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '云南最大藏传佛教寺院,小布达拉宫', '噶丹松赞林寺依山而建,金顶在阳光下熠熠生辉,晨雾中的寺庙与拉姆央措湖相映成画。', '香格里拉建塘镇', 99.7000, 27.8600, 90.00, 4.8, '藏传佛教,金顶,晨雾', 130, 1, '2026-04-01 10:00:00', '2026-04-01 10:00:00'),
(20, 5, '普达措国家公园', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '中国大陆第一个国家公园', '属都湖与碧塔海的原始森林和高原湖泊,栈道徒步可遇小松鼠,夏秋最美。', '香格里拉建塘镇东 22 公里', 99.9500, 27.8900, 138.00, 4.9, '国家公园,湖泊,森林', 155, 1, '2026-04-01 10:00:00', '2026-04-01 10:00:00'),
(21, 6, '和顺古镇', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '中国十大魅力名镇之首', '侨乡和顺,图书馆、洗衣亭、古树群,油菜花田环绕,适合慢逛一整天。', '腾冲和顺镇', 98.4600, 25.0100, 55.00, 4.8, '侨乡,古镇,图书馆,油菜花', 145, 1, '2026-04-01 10:00:00', '2026-04-01 10:00:00'),
(22, 6, '腾冲火山热海', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '99 座火山与 88 处温泉', '热海景区大滚锅水温 97 度,可煮温泉蛋;火山公园可乘热气球俯瞰火山口。', '腾冲市马站乡', 98.4700, 25.1300, 50.00, 4.6, '火山,温泉,热气球', 110, 1, '2026-04-01 10:00:00', '2026-04-01 10:00:00'),
(23, 6, '银杏村', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '三千棵古银杏的金色童话', '每年 11 月,3000 余棵银杏树金黄一片,屋顶、路面铺满落叶,摄影师的天堂。', '腾冲固东镇', 98.5200, 25.3300, 30.00, 4.7, '银杏,秋景,摄影', 90, 1, '2026-04-01 10:00:00', '2026-04-01 10:00:00'),
(24, 7, '抚仙湖', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '中国最深的高原淡水湖之一', '湖水清澈见底呈琉璃色,环湖骑行、帆船、潜水皆可,禄充风景区看笔架山日落。', '玉溪澄江市', 102.9100, 24.5500, 0.00, 4.8, '湖泊,帆船,骑行,清澈', 170, 1, '2026-04-01 10:00:00', '2026-04-01 10:00:00'),
(25, 7, '禄充风景区', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', '抚仙湖西岸的传统渔村', '笔架山上看湖,村内保留车水捕鱼古法,铜锅鱼一条街在此。', '玉溪澄江市禄充村', 102.8700, 24.5100, 15.00, 4.5, '渔村,笔架山,铜锅鱼', 60, 1, '2026-04-01 10:00:00', '2026-04-01 10:00:00'),
(26, 8, '普者黑国家湿地公园', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '喀斯特湖泊峰林与万亩荷花', '夏季万亩荷花盛开,乘柳叶小舟打水仗是经典玩法,青龙山观景台看日落。', '文山丘北县普者黑', 104.0600, 24.1300, 0.00, 4.8, '荷花,湿地,打水仗,峰林', 160, 1, '2026-04-01 10:00:00', '2026-04-01 10:00:00'),
(27, 9, '元阳哈尼梯田', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '世界文化遗产,大地的雕刻', '多依树看日出、老虎嘴看日落、坝达看云海,灌水季梯田如天空之镜。', '红河元阳县', 102.7700, 23.1100, 70.00, 4.9, '梯田,世界遗产,云海,日出', 185, 1, '2026-04-01 10:00:00', '2026-04-01 10:00:00'),
(28, 9, '箐口哈尼民俗村', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', '活的哈尼族博物馆', '蘑菇房村落,可看哈尼族织布、舂粑粑,村口梯田观景台视野开阔。', '红河元阳县箐口村', 102.7900, 23.0900, 30.00, 4.6, '哈尼族,蘑菇房,民俗', 70, 1, '2026-04-01 10:00:00', '2026-04-01 10:00:00');

-- ---------------------------------------------------------------------
-- 7. 新城市体验(id 16-24)
-- ---------------------------------------------------------------------
INSERT INTO tourism_experience (id, destination_id, name, cover_image, intro, description, address, longitude, latitude, price, rating, tags, favorite_count, status, create_time, update_time) VALUES
(16, 5, '藏家晚宴+锅庄舞会', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '在藏家吃糌粑喝酥油茶跳锅庄', '傍晚到藏族人家做客,学打酥油茶、捏糌粑,晚餐后围着篝火跳锅庄舞。', '香格里拉独克宗古城', 99.7000, 27.8250, 168.00, 4.8, '藏家,锅庄,酥油茶', 55, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(17, 5, '普达措骑行徒步', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '属都湖畔轻徒步,偶遇小松鼠', '含门票与向导,走属都湖栈道精华段,向导讲高原植物与藏地生态。', '香格里拉普达措', 99.9500, 27.8900, 298.00, 4.7, '徒步,高原,国家公园', 40, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(18, 6, '和顺古镇旅拍', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '侨乡古镇旗袍/民国风写真', '含服饰与妆造,拍摄点覆盖图书馆、洗衣亭、古巷,赠精修 9 张。', '腾冲和顺古镇', 98.4600, 25.0100, 299.00, 4.7, '旅拍,民国风,侨乡', 45, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(19, 6, '热海温泉泡汤+煮蛋', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '大滚锅温泉蛋,硫磺泉泡汤', '含热海门票与温泉票,体验用 97 度温泉水煮蛋,硫磺泉泡去一身疲惫。', '腾冲热海景区', 98.4700, 25.1300, 198.00, 4.8, '温泉,煮蛋,养生', 60, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(20, 7, '抚仙湖帆船体验', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '在琉璃色湖面扬帆', '专业教练带教的双体帆船体验,含救生衣与基础教学,约 1 小时。', '玉溪澄江市抚仙湖', 102.9200, 24.5400, 258.00, 4.7, '帆船,湖上运动', 38, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(21, 7, '环抚仙湖骑行', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '沿湖骑行看琉璃水色', '提供专业单车,推荐东岸 30 公里精华段,一路湖景与村落。', '玉溪澄江市', 102.9000, 24.5600, 60.00, 4.6, '骑行,湖景', 30, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(22, 8, '普者黑柳叶舟打水仗', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '夏日限定水上狂欢', '乘柳叶小舟穿行荷花丛,与邻船打水仗,含雨衣与水枪,湿身预警!', '文山丘北县普者黑', 104.0600, 24.1300, 188.00, 4.9, '打水仗,荷花,水上', 85, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(23, 9, '多依树梯田日出摄影', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '世界遗产地最经典机位', '凌晨出发占位,向导讲解最佳机位与拍摄参数,含热姜茶。', '红河元阳多依树', 102.7700, 23.1100, 128.00, 4.9, '摄影,日出,梯田', 70, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(24, 9, '哈尼梯田徒步讲解', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', '走进田埂,听哈尼族讲梯田故事', '哈尼族向导带队走田埂小道,讲千年梯田的开垦智慧与稻鱼鸭共生。', '红河元阳县坝达', 102.7800, 23.1200, 98.00, 4.8, '徒步,哈尼族,农耕', 35, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00');

-- ---------------------------------------------------------------------
-- 8. 新城市餐饮(id 16-22)
-- ---------------------------------------------------------------------
INSERT INTO restaurant (id, destination_id, name, cover_image, intro, description, address, longitude, latitude, price, rating, tags, favorite_count, status, create_time, update_time) VALUES
(16, 5, '独克宗牦牛火锅', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '高原牦牛肉火锅,暖到心里', '牦牛肉切薄片涮菌汤锅底,配青稞饼与酥油茶,高原夜晚的最佳慰藉。', '香格里拉独克宗古城', 99.7000, 27.8250, 88.00, 4.7, '牦牛火锅,藏餐,暖身', 60, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(17, 5, '藏家青稞酒馆', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&q=60', '喝青稞酒听藏歌', '古城里的小酒馆,自酿青稞酒配烤羊排,驻场歌手唱藏族民歌。', '香格里拉独克宗古城', 99.6990, 27.8240, 60.00, 4.5, '青稞酒,藏歌,小酒馆', 35, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(18, 6, '和顺古镇·侨乡私房菜', 'https://images.unsplash.com/photo-1512918728675-ed5a9ecdebfd?w=800&q=60', '大救驾与稀豆粉的故乡味', '腾冲名菜大救驾(饵块炒蛋)、稀豆粉油条,还有侨乡风格的南洋咖喱。', '腾冲和顺古镇', 98.4600, 25.0100, 55.00, 4.7, '大救驾,稀豆粉,侨乡', 55, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(19, 6, '银杏村农家乐', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '银杏炖鸡配农家菜', '银杏树下吃银杏炖土鸡,配时令野菜,秋季一桌金黄。', '腾冲固东镇银杏村', 98.5200, 25.3300, 65.00, 4.6, '农家菜,银杏鸡', 40, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(20, 7, '抚仙湖铜锅鱼一条街', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '铜锅煮鱼,鲜掉眉毛', '用抚仙湖抗浪鱼或青鱼,铜锅清汤煮,配糊辣蘸水,湖边必吃。', '玉溪澄江市禄充村', 102.8700, 24.5100, 70.00, 4.8, '铜锅鱼,湖鲜,必吃', 85, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(21, 8, '普者黑荷花宴', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '荷花的 N 种吃法', '炸荷花、荷叶鸡、莲子羹、藕带炒肉,夏季限定全荷宴。', '文山丘北县普者黑', 104.0600, 24.1300, 80.00, 4.7, '荷花宴,时令', 50, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(22, 9, '元阳哈尼蘸水鸡', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '梯田鸡配哈尼蘸水', '放养梯田边的土鸡白切,灵魂是哈尼蘸水(十几种香料),配梯田红米饭。', '红河元阳新街镇', 102.7300, 23.1200, 75.00, 4.7, '蘸水鸡,哈尼族,红米饭', 45, 1, '2026-04-02 10:00:00', '2026-04-02 10:00:00');

-- ---------------------------------------------------------------------
-- 9. 民宿-资源关联(新城市民宿 ↔ 本地资源)
-- ---------------------------------------------------------------------
INSERT INTO homestay_scenic (homestay_id, scenic_id, sort_order) VALUES
(19,19,0),(19,20,1),(20,19,0),(20,20,1),(21,21,0),(21,22,1),(22,23,0),(22,21,1),(23,24,0),(23,25,1),(24,26,0),(25,26,0),(26,27,0),(26,28,1),(27,27,0),(27,28,1);
INSERT INTO homestay_experience (homestay_id, experience_id, sort_order) VALUES
(19,16,0),(20,17,0),(20,16,1),(21,18,0),(21,19,1),(22,19,0),(23,20,0),(23,21,1),(24,22,0),(25,22,0),(26,23,0),(26,24,1),(27,24,0),(27,23,1);
INSERT INTO homestay_restaurant (homestay_id, restaurant_id, sort_order) VALUES
(19,16,0),(19,17,1),(20,16,0),(21,18,0),(22,19,0),(22,18,1),(23,20,0),(24,21,0),(25,21,0),(26,22,0),(27,22,0);

-- ---------------------------------------------------------------------
-- 10. 新房型(id 41-58)生成未来 61 天库存
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
WHERE rt.id BETWEEN 41 AND 58
  AND rt.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM room_inventory inv WHERE inv.room_type_id = rt.id AND inv.stay_date = d.dt);

-- ---------------------------------------------------------------------
-- 11. 新城市订单(id 27-36;对应新民宿 19-27)
-- ---------------------------------------------------------------------
INSERT INTO orders (order_no, user_id, homestay_id, room_type_id, homestay_name, homestay_cover, room_type_name, check_in_date, check_out_date, nights, room_count, unit_price, total_amount, discount_amount, pay_amount, contact_name, contact_phone, status, pay_status, pay_type, create_time, pay_time, check_in_time, check_out_time, complete_time) VALUES
('HSY20260401001', 8, 19, 41, '香格里拉·独克宗藏式客栈', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '藏式雕花大床房', DATE_ADD(CURDATE(), INTERVAL -45 DAY), DATE_ADD(CURDATE(), INTERVAL -44 DAY), 1, 1, 358.00, 358.00, 0.00, 358.00, '小满', '13800000008', 'COMPLETED', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -46 DAY), DATE_ADD(NOW(), INTERVAL -46 DAY), DATE_ADD(NOW(), INTERVAL -45 DAY), DATE_ADD(NOW(), INTERVAL -44 DAY), DATE_ADD(NOW(), INTERVAL -44 DAY)),
('HSY20260402001', 9, 21, 45, '腾冲·和顺古镇温泉客栈', 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800&q=60', '私汤大床房', DATE_ADD(CURDATE(), INTERVAL -40 DAY), DATE_ADD(CURDATE(), INTERVAL -38 DAY), 2, 1, 468.00, 936.00, 0.00, 936.00, '亲子游妈妈', '13800000009', 'COMPLETED', 'PAID', 'ALIPAY', DATE_ADD(NOW(), INTERVAL -41 DAY), DATE_ADD(NOW(), INTERVAL -41 DAY), DATE_ADD(NOW(), INTERVAL -40 DAY), DATE_ADD(NOW(), INTERVAL -38 DAY), DATE_ADD(NOW(), INTERVAL -38 DAY)),
('HSY20260403001', 10, 23, 49, '玉溪·抚仙湖琉璃海景民宿', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '琉璃海景大床房', DATE_ADD(CURDATE(), INTERVAL -33 DAY), DATE_ADD(CURDATE(), INTERVAL -32 DAY), 1, 1, 428.00, 428.00, 0.00, 428.00, '自由行老周', '13800000010', 'COMPLETED', 'PAID', 'BANK', DATE_ADD(NOW(), INTERVAL -34 DAY), DATE_ADD(NOW(), INTERVAL -34 DAY), DATE_ADD(NOW(), INTERVAL -33 DAY), DATE_ADD(NOW(), INTERVAL -32 DAY), DATE_ADD(NOW(), INTERVAL -32 DAY)),
('HSY20260404001', 11, 24, 51, '普者黑·荷塘月色客栈', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '荷塘景观大床房', DATE_ADD(CURDATE(), INTERVAL -28 DAY), DATE_ADD(CURDATE(), INTERVAL -27 DAY), 1, 1, 328.00, 328.00, 0.00, 328.00, '旅拍少女', '13800000011', 'COMPLETED', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -29 DAY), DATE_ADD(NOW(), INTERVAL -29 DAY), DATE_ADD(NOW(), INTERVAL -28 DAY), DATE_ADD(NOW(), INTERVAL -27 DAY), DATE_ADD(NOW(), INTERVAL -27 DAY)),
('HSY20260405001', 12, 26, 55, '元阳·云上梯田观景民宿', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '云海日出观景房', DATE_ADD(CURDATE(), INTERVAL -22 DAY), DATE_ADD(CURDATE(), INTERVAL -21 DAY), 1, 1, 398.00, 398.00, 0.00, 398.00, '洱海骑行侠', '13800000012', 'COMPLETED', 'PAID', 'ALIPAY', DATE_ADD(NOW(), INTERVAL -23 DAY), DATE_ADD(NOW(), INTERVAL -23 DAY), DATE_ADD(NOW(), INTERVAL -22 DAY), DATE_ADD(NOW(), INTERVAL -21 DAY), DATE_ADD(NOW(), INTERVAL -21 DAY)),
('HSY20260406001', 13, 20, 43, '香格里拉·松赞林卡雪山庄园', 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60', '松赞林寺景观房', DATE_ADD(CURDATE(), INTERVAL -18 DAY), DATE_ADD(CURDATE(), INTERVAL -16 DAY), 2, 1, 588.00, 1176.00, 0.00, 1176.00, '古城漫步者', '13800000013', 'COMPLETED', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -19 DAY), DATE_ADD(NOW(), INTERVAL -19 DAY), DATE_ADD(NOW(), INTERVAL -18 DAY), DATE_ADD(NOW(), INTERVAL -16 DAY), DATE_ADD(NOW(), INTERVAL -16 DAY)),
('HSY20260407001', 14, 22, 47, '腾冲·银杏村金色小院', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60', '银杏景观大床房', DATE_ADD(CURDATE(), INTERVAL -14 DAY), DATE_ADD(CURDATE(), INTERVAL -13 DAY), 1, 1, 268.00, 268.00, 0.00, 268.00, '米线爱好者', '13800000014', 'COMPLETED', 'PAID', 'BANK', DATE_ADD(NOW(), INTERVAL -15 DAY), DATE_ADD(NOW(), INTERVAL -15 DAY), DATE_ADD(NOW(), INTERVAL -14 DAY), DATE_ADD(NOW(), INTERVAL -13 DAY), DATE_ADD(NOW(), INTERVAL -13 DAY)),
('HSY20260408001', 15, 25, 54, '普者黑·三生三世湖畔民宿', 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&q=60', '取景地景观房', DATE_ADD(CURDATE(), INTERVAL -10 DAY), DATE_ADD(CURDATE(), INTERVAL -9 DAY), 1, 1, 268.00, 268.00, 0.00, 268.00, '山野徒步客', '13800000015', 'COMPLETED', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -11 DAY), DATE_ADD(NOW(), INTERVAL -11 DAY), DATE_ADD(NOW(), INTERVAL -10 DAY), DATE_ADD(NOW(), INTERVAL -9 DAY), DATE_ADD(NOW(), INTERVAL -9 DAY)),
('HSY20260409001', 16, 27, 57, '元阳·哈尼蘑菇房民宿', 'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=800&q=60', '蘑菇房大床房', DATE_ADD(CURDATE(), INTERVAL -7 DAY), DATE_ADD(CURDATE(), INTERVAL -6 DAY), 1, 1, 288.00, 288.00, 0.00, 288.00, '奶茶不加冰', '13800000016', 'COMPLETED', 'PAID', 'ALIPAY', DATE_ADD(NOW(), INTERVAL -8 DAY), DATE_ADD(NOW(), INTERVAL -8 DAY), DATE_ADD(NOW(), INTERVAL -7 DAY), DATE_ADD(NOW(), INTERVAL -6 DAY), DATE_ADD(NOW(), INTERVAL -6 DAY)),
('HSY20260410001', 17, 19, 42, '香格里拉·独克宗藏式客栈', 'https://images.unsplash.com/photo-1544984243-ec57ea16fe25?w=800&q=60', '古城景观双床房', DATE_ADD(CURDATE(), INTERVAL -4 DAY), DATE_ADD(CURDATE(), INTERVAL -2 DAY), 2, 1, 298.00, 596.00, 0.00, 596.00, '毕业旅行团', '13800000017', 'CHECKED_IN', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -5 DAY), DATE_ADD(NOW(), INTERVAL -5 DAY), DATE_ADD(NOW(), INTERVAL -4 DAY), NULL, NULL),
('HSY20260411001', 18, 26, 56, '元阳·云上梯田观景民宿', 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&q=60', '梯田景观双床房', DATE_ADD(CURDATE(), INTERVAL -2 DAY), DATE_ADD(CURDATE(), INTERVAL -1 DAY), 1, 1, 328.00, 328.00, 0.00, 328.00, '候鸟旅居客', '13800000018', 'CHECKED_IN', 'PAID', 'ALIPAY', DATE_ADD(NOW(), INTERVAL -3 DAY), DATE_ADD(NOW(), INTERVAL -3 DAY), DATE_ADD(NOW(), INTERVAL -2 DAY), NULL, NULL),
('HSY20260412001', 19, 23, 50, '玉溪·抚仙湖琉璃海景民宿', 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&q=60', '湖景家庭套房', DATE_ADD(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 4 DAY), 2, 1, 628.00, 1256.00, 0.00, 1256.00, '周末出逃记', '13800000019', 'PAID', 'PAID', 'WECHAT', DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY), NULL, NULL, NULL),
('HSY20260413001', 20, 24, 52, '普者黑·荷塘月色客栈', 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60', '青龙山景观双床房', DATE_ADD(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 2, 1, 288.00, 576.00, 0.00, 576.00, '咖啡续命人', '13800000020', 'PAID', 'PAID', 'BANK', DATE_ADD(NOW(), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL -1 DAY), NULL, NULL, NULL);

-- ---------------------------------------------------------------------
-- 12. 新城市评价(order 27-35 已完成单)
-- ---------------------------------------------------------------------
INSERT INTO review (order_id, user_id, homestay_id, rating, hygiene_score, service_score, facility_score, location_score, content, images, merchant_reply, reply_time, status, create_time, update_time) VALUES
(27, 8, 19, 5, 5, 5, 4, 5, '藏式老宅太有味道了,天台看转经筒夜景绝了。老板还教我们转经的正确方式。', '["https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=60"]', '扎西德勒!欢迎冬天来看雪后的独克宗,更美。', DATE_ADD(NOW(), INTERVAL -43 DAY), 0, DATE_ADD(NOW(), INTERVAL -44 DAY), DATE_ADD(NOW(), INTERVAL -43 DAY)),
(28, 9, 21, 5, 5, 5, 5, 5, '私汤太舒服了!和顺古镇安静又有味道,图书馆很惊艳。温泉客栈体验满分。', NULL, '谢谢,和顺四季都美,秋天银杏黄了更值得来!', DATE_ADD(NOW(), INTERVAL -37 DAY), 0, DATE_ADD(NOW(), INTERVAL -38 DAY), DATE_ADD(NOW(), INTERVAL -37 DAY)),
(29, 10, 23, 5, 5, 5, 5, 4, '抚仙湖的水真的像琉璃!房间躺看湖景,帆船体验很刺激。铜锅鱼好吃到跺脚。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -31 DAY), DATE_ADD(NOW(), INTERVAL -31 DAY)),
(30, 11, 24, 5, 4, 5, 5, 5, '荷花季来的,推窗就是荷花池,打水仗全身湿透但超快乐!青龙山日出值得早起。', '["https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60"]', '夏天来普者黑就对了,欢迎秋天来看候鸟!', DATE_ADD(NOW(), INTERVAL -26 DAY), 0, DATE_ADD(NOW(), INTERVAL -27 DAY), DATE_ADD(NOW(), INTERVAL -26 DAY)),
(31, 12, 26, 5, 5, 5, 5, 5, '多依树日出看到云海了!房间直接能看到梯田,老板推荐的摄影机位超赞。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -20 DAY), DATE_ADD(NOW(), INTERVAL -20 DAY)),
(32, 13, 20, 5, 5, 5, 5, 5, '松赞林寺景观房名不虚传,清晨日照金寺太震撼。庄园的藏式早餐很丰盛。', '["https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=60"]', '感谢!高原早晚凉,记得多穿点,欢迎再回香格里拉。', DATE_ADD(NOW(), INTERVAL -15 DAY), 0, DATE_ADD(NOW(), INTERVAL -16 DAY), DATE_ADD(NOW(), INTERVAL -15 DAY)),
(33, 14, 22, 4, 4, 5, 4, 5, '银杏村太出片了,小院干净。秋天来正好,老板娘炖的银杏鸡很鲜。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -12 DAY), DATE_ADD(NOW(), INTERVAL -12 DAY)),
(34, 15, 25, 5, 4, 5, 4, 5, '取景地就在旁边,田园小院很治愈,荷花宴是惊喜!', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -8 DAY), DATE_ADD(NOW(), INTERVAL -8 DAY)),
(35, 16, 27, 5, 4, 5, 5, 4, '蘑菇房体验新奇,晚上围火塘听老人唱哈尼古歌,很特别的经历。', NULL, '谢谢体验我们的文化,欢迎参加十月年!', DATE_ADD(NOW(), INTERVAL -5 DAY), 0, DATE_ADD(NOW(), INTERVAL -6 DAY), DATE_ADD(NOW(), INTERVAL -5 DAY)),
(36, 17, 19, 5, 5, 4, 4, 5, '毕业旅行最后一站选了香格里拉,客栈氛围超好,认识了同路的驴友。', NULL, NULL, NULL, 0, DATE_ADD(NOW(), INTERVAL -3 DAY), DATE_ADD(NOW(), INTERVAL -3 DAY));

-- ---------------------------------------------------------------------
-- 13. 新城市收藏
-- ---------------------------------------------------------------------
INSERT INTO favorite (user_id, target_type, target_id, create_time) VALUES
(8, 1, 19, '2026-04-20 10:00:00'), (9, 1, 21, '2026-04-21 10:00:00'), (9, 2, 21, '2026-04-21 11:00:00'),
(10, 1, 23, '2026-04-22 10:00:00'), (10, 2, 24, '2026-04-22 11:00:00'), (11, 1, 24, '2026-04-23 10:00:00'),
(12, 1, 26, '2026-04-24 10:00:00'), (13, 1, 20, '2026-04-25 10:00:00'), (14, 1, 22, '2026-04-26 10:00:00'),
(15, 1, 25, '2026-04-27 10:00:00'), (16, 1, 27, '2026-04-28 10:00:00'), (17, 2, 19, '2026-04-29 10:00:00'),
(18, 3, 22, '2026-04-30 10:00:00'), (19, 1, 23, '2026-05-01 10:00:00'), (20, 4, 20, '2026-05-02 10:00:00'),
(21, 1, 24, '2026-05-03 10:00:00'), (22, 2, 27, '2026-05-04 10:00:00');
UPDATE homestay SET favorite_count = favorite_count + 1 WHERE id IN (19,20,21,22,23,24,25,26,27);
UPDATE scenic_spot SET favorite_count = favorite_count + 1 WHERE id IN (19,21,24,27);
UPDATE tourism_experience SET favorite_count = favorite_count + 1 WHERE id IN (22);
UPDATE restaurant SET favorite_count = favorite_count + 1 WHERE id IN (20);

-- ---------------------------------------------------------------------
-- 14. 评分同步(按新评价)
-- ---------------------------------------------------------------------
UPDATE homestay SET rating = 4.8, rating_count = 29 WHERE id = 19;
UPDATE homestay SET rating = 4.9, rating_count = 21 WHERE id = 20;
UPDATE homestay SET rating = 4.8, rating_count = 35 WHERE id = 21;
UPDATE homestay SET rating = 4.6, rating_count = 25 WHERE id = 22;
UPDATE homestay SET rating = 4.7, rating_count = 23 WHERE id = 23;
UPDATE homestay SET rating = 4.7, rating_count = 28 WHERE id = 24;
UPDATE homestay SET rating = 4.6, rating_count = 17 WHERE id = 25;
UPDATE homestay SET rating = 4.8, rating_count = 31 WHERE id = 26;
UPDATE homestay SET rating = 4.7, rating_count = 22 WHERE id = 27;
