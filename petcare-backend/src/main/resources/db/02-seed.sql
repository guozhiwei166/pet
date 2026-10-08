-- ============================================================
-- 猫狗宠物寄养系统 基础种子数据（角色/标签/房型/房间/服务）
-- 用户与员工由后端 DataInitializer 在首次启动时创建（密码需 BCrypt 运行时生成）
-- ============================================================
SET NAMES utf8mb4;

-- 角色（permissions 由 @RequirePermission 切面实际校验；支持逗号分隔与 *: 通配）
INSERT INTO sys_role (id, code, name, permissions) VALUES
(1, 'ROLE_USER',   '宠物主人', 'pet:*,boarding:*,review:create,notification:*,file:upload'),
(2, 'ROLE_STAFF',  '工作人员', 'care:*,pet:read,room:view,room:status,review:reply,exception:*,notification:*,file:upload'),
(3, 'ROLE_ADMIN',  '管理员',   '*');

-- 性格标签字典
INSERT INTO pet_tag (id, name) VALUES
(1,'亲人'),(2,'活泼'),(3,'安静'),(4,'怕打雷'),(5,'喜欢玩球'),
(6,'贪吃'),(7,'挑食'),(8,'粘人'),(9,'独立'),(10,'好奇心强'),
(11,'不喜欢陌生狗'),(12,'喜欢鸡胸肉');

-- 房型（犬区 4 档 + 猫区 4 档；species 为显式分区字段，下单时按宠物物种匹配）
-- 猫区档位与犬区同价对应，便于主人按预算跨区对比
INSERT INTO room_type (id, name, species, price, area, max_pets, facilities, description, photo, status, sort) VALUES
(1, '标准房',        'DOG', 68.00,  6.00,  1, '独立空间,每日清洁,通风系统',       '标准独立空间，适合短途寄养，每日两次清扫消毒。', '/img/rooms/standard.svg', 1, 1),
(2, '舒适房',        'DOG', 88.00,  8.00,  1, '独立空间,空调,每日清洁,监控系统',   '带空调舒适空间，全天监控系统，性价比之选。', '/img/rooms/comfort.svg', 1, 2),
(3, '豪华房',        'DOG', 128.00, 12.00, 1, '独立空间,空调,摄像监控,每日清洁,独立活动时间', '宽敞豪华单间，落地窗采光，附赠每日独立活动时间与视频反馈。', '/img/rooms/luxury.svg', 1, 3),
(4, 'VIP宠物套房',   'DOG', 198.00, 20.00, 2, '独立空间,空调,摄像监控,每日清洁,独立活动时间,专属管家', '顶级套房配专属管家，一对一照护，实时视频可看。', '/img/rooms/vip-suite.svg', 1, 4),
(5, '猫·标准房',     'CAT', 68.00,  6.00,  1, '猫爬架,独立猫砂盆,每日清洁,安静隔音', '猫咪标准小间：猫爬架+独立猫砂盆，安静舒适。', '/img/rooms/cat-standard.svg', 1, 5),
(6, '猫·舒适房',     'CAT', 88.00,  8.00,  1, '猫爬架,猫抓板,独立猫砂盆,空调,每日清洁', '猫咪舒适房：猫爬架与玩具齐备，空调恒温。', '/img/rooms/cat-comfort.svg', 1, 6),
(7, '猫·豪华房',     'CAT', 128.00, 12.00, 1, '猫爬架,猫抓板,独立猫砂盆,空调,摄像监控,每日清洁', '猫咪豪华房：攀爬乐园+落地窗观景，带监控可随时看猫。', '/img/rooms/cat-luxury.svg', 1, 7),
(8, '猫·VIP套房',    'CAT', 198.00, 20.00, 2, '猫爬架,猫抓板,独立猫砂盆,空调,摄像监控,每日清洁,专属管家', '猫咪顶级套房：专属管家陪玩梳毛，实时视频可看。', '/img/rooms/cat-vip.svg', 1, 8);

-- 房间（三层，每层 20 间：犬区 12 间[标准3+舒适3+豪华3+VIP3] + 猫区 8 间[各档2间]）
INSERT INTO room (room_no, floor, room_type_id, status) VALUES
('101',1,1,'AVAILABLE'),('102',1,1,'AVAILABLE'),('103',1,1,'AVAILABLE'),
('104',1,2,'AVAILABLE'),('105',1,2,'AVAILABLE'),('106',1,2,'AVAILABLE'),
('107',1,3,'AVAILABLE'),('108',1,3,'AVAILABLE'),('109',1,3,'AVAILABLE'),
('110',1,4,'AVAILABLE'),('111',1,4,'AVAILABLE'),('112',1,4,'AVAILABLE'),
('113',1,5,'AVAILABLE'),('114',1,5,'AVAILABLE'),
('115',1,6,'AVAILABLE'),('116',1,6,'AVAILABLE'),
('117',1,7,'AVAILABLE'),('118',1,7,'AVAILABLE'),
('119',1,8,'AVAILABLE'),('120',1,8,'AVAILABLE'),
('201',2,1,'AVAILABLE'),('202',2,1,'AVAILABLE'),('203',2,1,'AVAILABLE'),
('204',2,2,'AVAILABLE'),('205',2,2,'AVAILABLE'),('206',2,2,'AVAILABLE'),
('207',2,3,'AVAILABLE'),('208',2,3,'AVAILABLE'),('209',2,3,'AVAILABLE'),
('210',2,4,'AVAILABLE'),('211',2,4,'AVAILABLE'),('212',2,4,'AVAILABLE'),
('213',2,5,'AVAILABLE'),('214',2,5,'AVAILABLE'),
('215',2,6,'AVAILABLE'),('216',2,6,'AVAILABLE'),
('217',2,7,'AVAILABLE'),('218',2,7,'AVAILABLE'),
('219',2,8,'AVAILABLE'),('220',2,8,'AVAILABLE'),
('301',3,1,'AVAILABLE'),('302',3,1,'AVAILABLE'),('303',3,1,'AVAILABLE'),
('304',3,2,'AVAILABLE'),('305',3,2,'AVAILABLE'),('306',3,2,'AVAILABLE'),
('307',3,3,'AVAILABLE'),('308',3,3,'AVAILABLE'),('309',3,3,'AVAILABLE'),
('310',3,4,'AVAILABLE'),('311',3,4,'AVAILABLE'),('312',3,4,'AVAILABLE'),
('313',3,5,'AVAILABLE'),('314',3,5,'AVAILABLE'),
('315',3,6,'AVAILABLE'),('316',3,6,'AVAILABLE'),
('317',3,7,'AVAILABLE'),('318',3,7,'AVAILABLE'),
('319',3,8,'AVAILABLE'),('320',3,8,'AVAILABLE');

-- 增值服务
INSERT INTO service (id, name, price, unit, description, status, sort) VALUES
(1, '洗澡',     35.00, '次',     '专业宠物香波，吹干造型', 1, 1),
(2, '美容',     60.00, '次',     '精细修剪造型，含洗澡',   1, 2),
(3, '单独遛宠', 20.00, '30分钟', '一对一户外遛弯活动',     1, 3),
(4, '宠物摄影', 30.00, '次',     '专业设备拍摄精美照片',   1, 4),
(5, '接送服务', 50.00, '单程',   '上门接送，同城范围',     1, 5),
(6, '喂药服务', 10.00, '次',     '按时按量喂药并记录',     1, 6),
(7, '特殊饮食', 15.00, '天',     '按主人要求定制餐食',     1, 7);
