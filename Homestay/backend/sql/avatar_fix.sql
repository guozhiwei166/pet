-- =====================================================================
-- 用户头像修正:统一为 randomuser 真人头像(最后执行;重导后跑一次即可)
-- 用法: mysql ... tourism_homestay < avatar_fix.sql
-- =====================================================================
USE tourism_homestay;
UPDATE user SET avatar = CONCAT('https://randomuser.me/api/portraits/men/', (id % 40) + 1, '.jpg') WHERE gender = 1;
UPDATE user SET avatar = CONCAT('https://randomuser.me/api/portraits/women/', (id % 40) + 1, '.jpg') WHERE gender = 2;
UPDATE user SET avatar = CONCAT('https://randomuser.me/api/portraits/', IF(id % 2 = 0, 'women', 'men'), '/', (id % 40) + 1, '.jpg') WHERE gender = 0;
