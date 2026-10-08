package com.petcare.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.boarding.entity.BoardingOrder;
import com.petcare.boarding.mapper.BoardingOrderMapper;
import com.petcare.care.entity.CareTask;
import com.petcare.care.entity.PetDailyReport;
import com.petcare.care.mapper.CareTaskMapper;
import com.petcare.care.mapper.PetDailyReportMapper;
import com.petcare.marketing.entity.Announcement;
import com.petcare.marketing.entity.Coupon;
import com.petcare.marketing.mapper.AnnouncementMapper;
import com.petcare.marketing.mapper.CouponMapper;
import com.petcare.notification.entity.Notification;
import com.petcare.notification.mapper.NotificationMapper;
import com.petcare.pet.entity.Pet;
import com.petcare.pet.entity.PetHealth;
import com.petcare.pet.entity.PetTagRelation;
import com.petcare.pet.entity.PetVaccine;
import com.petcare.pet.mapper.PetHealthMapper;
import com.petcare.pet.mapper.PetMapper;
import com.petcare.pet.mapper.PetTagRelationMapper;
import com.petcare.pet.mapper.PetVaccineMapper;
import com.petcare.review.entity.Review;
import com.petcare.review.mapper.ReviewMapper;
import com.petcare.room.entity.Room;
import com.petcare.room.entity.RoomType;
import com.petcare.room.mapper.RoomMapper;
import com.petcare.story.entity.PetStory;
import com.petcare.story.mapper.PetStoryMapper;
import com.petcare.store.entity.ServiceBooking;
import com.petcare.store.entity.StoreProfile;
import com.petcare.store.mapper.ServiceBookingMapper;
import com.petcare.store.mapper.StoreProfileMapper;
import com.petcare.user.entity.SysRole;
import com.petcare.user.entity.SysUser;
import com.petcare.user.entity.SysUserRole;
import com.petcare.user.mapper.SysRoleMapper;
import com.petcare.user.mapper.SysUserMapper;
import com.petcare.user.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 演示数据扩充器（在 DataInitializer 之后运行）。
 * 幂等：检测到已有业务数据（宠物 > 2 只）即跳过。
 * 生成：多主人多宠物、跨 60 天订单、完整照护链与日报、评价、通知、门店信息、
 * 服务预约、优惠券、公告、故事墙。
 */
@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final PetMapper petMapper;
    private final PetVaccineMapper vaccineMapper;
    private final PetTagRelationMapper tagRelationMapper;
    private final BoardingOrderMapper orderMapper;
    private final CareTaskMapper taskMapper;
    private final PetDailyReportMapper reportMapper;
    private final PetHealthMapper healthMapper;
    private final NotificationMapper notificationMapper;
    private final ReviewMapper reviewMapper;
    private final RoomMapper roomMapper;
    private final com.petcare.room.mapper.RoomTypeMapper roomTypeMapper;
    private final ServiceBookingMapper serviceBookingMapper;
    private final StoreProfileMapper storeProfileMapper;
    private final CouponMapper couponMapper;
    private final AnnouncementMapper announcementMapper;
    private final PetStoryMapper storyMapper;

    private final Random rnd = new Random();

    @Override
    public void run(String... args) {
        Long petCount = petMapper.selectCount(null);
        if (petCount != null && petCount > 2) {
            log.info("DataSeeder: 已有演示数据，跳过");
            return;
        }
        try {
            seedStore();
            seedUsersAndPets();
            seedOrders();
            seedExtras();
            log.info("DataSeeder: 演示数据扩充完成");
        } catch (Exception e) {
            log.error("DataSeeder 失败", e);
        }
    }

    // ---------- 门店 ----------

    private void seedStore() {
        if (storeProfileMapper.selectCount(null) > 0) return;
        StoreProfile sp = new StoreProfile();
        sp.setStoreName("猫狗宠物寄养生活馆");
        sp.setSlogan("把毛孩子托付给真正懂它的人");
        sp.setIntro("成立于 2020 年，集寄养、洗护、美容、宠物摄影于一体的宠物生活空间。"
                + "持证护理师团队、独立猫犬分区、24 小时新风与监控系统。");
        sp.setAddress("阳光城 · 云栖路 188 号（宠物主题街区 2F）");
        sp.setPhone("400-880-9527");
        sp.setOpenHours("08:00 - 20:00（寄养区 24 小时值守）");
        sp.setFeatures("持证护理师,独立猫犬分区,24h监控,新风系统,接送服务,宠物摄影");
        sp.setPhotos("/img/rooms/dogroom.jpg,/img/rooms/catroom.jpg,/img/hero/golden.jpg");
        sp.setUpdateTime(LocalDateTime.now());
        storeProfileMapper.insert(sp);
    }

    // ---------- 多主人 / 多宠物 ----------

    private void seedUsersAndPets() {
        SysUser u1 = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "user"));
        if (u1 == null) return;
        SysUser u2 = userOrCreate("user2", "桃桃", "13800000002");
        SysUser u3 = userOrCreate("user3", "石头爸", "13800000003");

        ensurePet(u1.getId(), "豆豆", "DOG", "金毛寻回犬", "M", 26.5, 1, "活泼粘人", "喜欢接飞盘，怕打雷", 1);
        ensurePet(u1.getId(), "咪咪", "CAT", "英国短毛猫", "F", 4.8, 1, "安静高冷", "挑食，需温水泡粮", 2);
        ensurePet(u2.getId(), "桃桃", "DOG", "柯基", "F", 11.2, 1, "活泼可爱", "拆家小能手，需充足运动", 4);
        ensurePet(u2.getId(), "团子", "CAT", "布偶猫", "M", 5.6, 1, "亲人撒娇", "喜欢被摸肚皮", 6);
        ensurePet(u2.getId(), "奶糖", "DOG", "比熊", "F", 6.5, 1, "温顺黏人", "需每日梳毛", 2);
        ensurePet(u3.getId(), "大圣", "DOG", "边境牧羊犬", "M", 21.0, 1, "聪明机警", "精力极旺盛", 1);
        ensurePet(u3.getId(), "雪球", "CAT", "中华田园猫", "F", 3.9, 0, "活泼好动", "好奇心强", 5);
        ensurePet(u3.getId(), "黑米", "DOG", "拉布拉多", "M", 29.8, 1, "温和憨厚", "食量大", 3);
    }

    private SysUser userOrCreate(String username, String nickname, String phone) {
        SysUser exists = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (exists != null) return exists;
        SysUser u = new SysUser();
        u.setUsername(username);
        u.setPhone(phone);
        u.setPassword(passwordEncoder.encode("user123"));
        u.setNickname(nickname);
        u.setStatus(1);
        userMapper.insert(u);
        bindRole(u.getId(), "ROLE_USER");
        return u;
    }

    private void bindRole(Long userId, String roleCode) {
        Long cnt = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (cnt != null && cnt > 0) return;
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, roleCode));
        if (role == null) return;
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(role.getId());
        userRoleMapper.insert(ur);
    }

    /** 按品种给演示宠物分配真实照片（复用本地已下载图片，避免 emoji 占位） */
    private String photoOf(String breed, String species) {
        String b = breed == null ? "" : breed;
        if (b.contains("金毛") || b.contains("拉布拉多")) return "/img/hero/golden.jpg";
        if (b.contains("柯基")) return "/img/hero/corgi.jpg";
        if (b.contains("边牧") || b.contains("比熊")) return "/img/hero/golden.jpg";
        return species.equals("CAT") ? "/img/hero/cat.jpg" : "/img/hero/corgi.jpg";
    }

    private void ensurePet(Long ownerId, String name, String species, String breed, String gender,
                           double weight, int sterilized, String temperament, String notes, long tagId) {
        Long cnt = petMapper.selectCount(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getUserId, ownerId).eq(Pet::getName, name));
        if (cnt != null && cnt > 0) return;
        Pet p = new Pet();
        p.setUserId(ownerId);
        p.setName(name);
        p.setSpecies(species);
        p.setBreed(breed);
        p.setGender(gender);
        p.setBirthDate(LocalDate.now().minusMonths(6 + rnd.nextInt(60)));
        p.setWeight(BigDecimal.valueOf(weight));
        p.setSterilized(sterilized);
        p.setTemperament(temperament);
        p.setNotes(notes);
        p.setPhoto(photoOf(breed, species));
        p.setCreateTime(LocalDateTime.now().minusDays(rnd.nextInt(30) + 10));
        petMapper.insert(p);

        PetVaccine v = new PetVaccine();
        v.setPetId(p.getId());
        v.setVaccineName("DOG".equals(species) ? "犬四联" : "猫三联");
        v.setInoculationDate(LocalDate.now().minusMonths(6));
        v.setExpireDate(LocalDate.now().plusMonths(6));
        v.setOrg("安安宠物医院");
        v.setCreateTime(LocalDateTime.now().minusMonths(6));
        vaccineMapper.insert(v);

        PetTagRelation rel = new PetTagRelation();
        rel.setPetId(p.getId());
        rel.setTagId(tagId);
        tagRelationMapper.insert(rel);
    }

    // ---------- 订单 + 照护链 ----------

    private void seedOrders() {
        Long taskCnt = taskMapper.selectCount(null);
        if (taskCnt != null && taskCnt > 100) return;
        Long staffId = findUserId("staff");
        List<Pet> pets = petMapper.selectList(null);
        if (pets.isEmpty()) return;
        List<Room> rooms = roomMapper.selectList(null);
        if (rooms.isEmpty()) return;
        List<RoomType> types = roomTypeMapper.selectList(null);
        // 犬区/猫区房型划分：按 species 显式字段（DOG/CAT），与下单页的匹配口径一致
        Set<Long> catTypeIds = types.stream()
                .filter(t -> "CAT".equals(t.getSpecies()))
                .map(RoomType::getId).collect(Collectors.toSet());
        Set<Long> dogTypeIds = types.stream()
                .filter(t -> !"CAT".equals(t.getSpecies()))
                .map(RoomType::getId).collect(Collectors.toSet());
        // 已分配给"在住/已确认"订单的房间占用区间，避免演示数据出现同房撞期
        List<RoomHold> holds = new ArrayList<>();

        int serial = 0;
        for (int dayOffset = -50; dayOffset <= 12; dayOffset++) {
            if (rnd.nextInt(4) != 0) continue;
            Pet pet = pets.get(rnd.nextInt(pets.size()));
            SysUser owner = userMapper.selectById(pet.getUserId());
            if (owner == null) continue;

            int days = 2 + rnd.nextInt(5);
            LocalDate start = LocalDate.now().plusDays(dayOffset);
            LocalDate end = start.plusDays(days);
            // 与下单页一致：按宠物物种只使用对应分区房型（犬区 1-4 / 猫区 5-8）
            Set<Long> wantedTypeIds = "CAT".equals(pet.getSpecies()) ? catTypeIds : dogTypeIds;

            String status;
            LocalDateTime payTime = null, checkin = null, checkout = null;
            if (end.isBefore(LocalDate.now())) {
                status = "COMPLETED";
                payTime = start.atTime(9, 30).minusDays(1);
                checkin = start.atTime(9, 0);
                checkout = end.atTime(18, 0);
            } else if (start.isBefore(LocalDate.now())) {
                status = "BOARDING";
                payTime = start.atTime(9, 30).minusDays(1);
                checkin = start.atTime(9, 0);
            } else if (start.isBefore(LocalDate.now().plusDays(3))) {
                status = "CONFIRMED";
                payTime = start.atTime(9, 30).minusDays(1);
            } else {
                status = "PENDING_PAYMENT";
            }

            // 具体房间：仅"在住/已确认"订单占用；待支付、已支付订单不占房（与线上流程一致，下单只占房型库存）
            boolean holdRoom = "BOARDING".equals(status) || "CONFIRMED".equals(status);
            Room room = null;
            if (holdRoom || "COMPLETED".equals(status)) {
                room = holdRoom
                        ? pickFreeRoom(rooms, wantedTypeIds, start, end, holds)
                        : pickRandomRoom(rooms, wantedTypeIds);
                if (room == null) continue; // 无可用房间则本次不生成，避免演示数据同房撞期
            }
            RoomType type = room != null ? roomTypeMapper.selectById(room.getRoomTypeId()) : pickRandomType(types, wantedTypeIds);
            if (type == null) continue;

            BoardingOrder o = new BoardingOrder();
            o.setOrderNo("PC" + (System.currentTimeMillis() / 1000) + String.format("%04d", serial++));
            o.setUserId(owner.getId());
            o.setPetId(pet.getId());
            o.setRoomTypeId(type.getId());
            o.setRoomId(room == null ? null : room.getId());
            o.setStartDate(start);
            o.setEndDate(end);
            o.setDays(days);
            BigDecimal roomRate = type.getPrice() == null ? new BigDecimal("88") : type.getPrice();
            BigDecimal svcAmount = BigDecimal.valueOf(rnd.nextInt(4) * 35L);
            o.setRoomAmount(roomRate.multiply(BigDecimal.valueOf(days)));
            o.setServiceAmount(svcAmount);
            o.setTotalAmount(o.getRoomAmount().add(svcAmount));
            o.setStatus(status);
            o.setContactName(owner.getNickname());
            o.setContactPhone(owner.getPhone());
            o.setRemark(rnd.nextBoolean() ? "第一次来，请多关照" : "请按档案习惯喂食");
            o.setPayTime(payTime);
            o.setCheckinTime(checkin);
            o.setCheckoutTime(checkout);
            o.setCreateTime((payTime != null ? payTime : LocalDateTime.now()).minusDays(1));
            orderMapper.insert(o);

            if (room != null && holdRoom) {
                holds.add(new RoomHold(room.getId(), o.getId(), start, end, status));
            }

            if ("COMPLETED".equals(status) || "BOARDING".equals(status)) {
                for (int d = 0; d < days; d++) {
                    LocalDate day = start.plusDays(d);
                    if (day.isAfter(LocalDate.now())) break;
                    generateDayCare(o, day, staffId);
                }
                if ("COMPLETED".equals(status) && rnd.nextBoolean()) {
                    seedReview(o, owner);
                }
            }
        }
        // 房间状态与订单对齐：不置 RESERVED 会让员工端"办理入住"失败，房间地图/使用率也会失真
        syncRoomStatus(holds);
    }

    /** 演示数据内部持有的房间占用（用于区间冲突判断与事后同步房间状态） */
    private record RoomHold(Long roomId, Long orderId, LocalDate start, LocalDate end, String orderStatus) {
        boolean overlaps(Long otherRoomId, LocalDate otherStart, LocalDate otherEnd) {
            return roomId.equals(otherRoomId) && start.isBefore(otherEnd) && end.isAfter(otherStart);
        }
    }

    /** 取一间该分区内、区间不与已占用分配冲突的房间；无则返回 null */
    private Room pickFreeRoom(List<Room> rooms, Set<Long> typeIds, LocalDate start, LocalDate end, List<RoomHold> holds) {
        for (Room r : shuffled(rooms, typeIds)) {
            boolean busy = holds.stream().anyMatch(h -> h.overlaps(r.getId(), start, end));
            if (!busy) return r;
        }
        return null;
    }

    private Room pickRandomRoom(List<Room> rooms, Set<Long> typeIds) {
        List<Room> candidates = shuffled(rooms, typeIds);
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private RoomType pickRandomType(List<RoomType> types, Set<Long> typeIds) {
        List<RoomType> candidates = new ArrayList<>(types.stream()
                .filter(t -> typeIds.contains(t.getId())).toList());
        if (candidates.isEmpty()) return null;
        Collections.shuffle(candidates, rnd);
        return candidates.get(0);
    }

    private List<Room> shuffled(List<Room> rooms, Set<Long> typeIds) {
        List<Room> candidates = new ArrayList<>(rooms.stream()
                .filter(r -> typeIds.contains(r.getRoomTypeId())).toList());
        Collections.shuffle(candidates, rnd);
        return candidates;
    }

    /** 房间状态落库：在住优先于已预订（同房先住后订时，当前状态应是在住） */
    private void syncRoomStatus(List<RoomHold> holds) {
        Map<Long, RoomHold> byRoom = new LinkedHashMap<>();
        for (RoomHold h : holds) {
            RoomHold cur = byRoom.get(h.roomId());
            if (cur == null || rank(h) > rank(cur)) byRoom.put(h.roomId(), h);
        }
        for (RoomHold h : byRoom.values()) {
            Room r = new Room();
            r.setId(h.roomId());
            r.setStatus("BOARDING".equals(h.orderStatus()) ? "OCCUPIED" : "RESERVED");
            r.setCurrentOrderId(h.orderId());
            roomMapper.updateById(r);
        }
    }

    private int rank(RoomHold h) {
        return "BOARDING".equals(h.orderStatus()) ? 2 : 1;
    }

    private void generateDayCare(BoardingOrder order, LocalDate day, Long staffId) {
        taskMapper.insert(taskOf(order, day, "08:30", "FEEDING", "早餐", "狗粮 150g", staffId));
        taskMapper.insert(taskOf(order, day, "11:00", "CLEANING", "房间清洁", "清扫消毒", staffId));
        taskMapper.insert(taskOf(order, day, "15:30", "WALK", "遛宠活动", "户外活动 30 分钟", staffId));
        taskMapper.insert(taskOf(order, day, "16:00", "HEALTH_CHECK", "健康观察", "精神/食欲/饮水/排便", staffId));

        PetHealth h = new PetHealth();
        h.setPetId(order.getPetId());
        h.setOrderId(order.getId());
        h.setRecordDate(day);
        h.setWeight(BigDecimal.valueOf(10 + rnd.nextInt(20)));
        h.setTemperature(new BigDecimal("38.5"));
        h.setMentalState("正常");
        h.setAppetite("正常");
        h.setWater("正常");
        h.setPoop("正常");
        h.setIsAbnormal(0);
        h.setStaffId(staffId);
        h.setCreateTime(day.atTime(17, 0));
        healthMapper.insert(h);

        PetDailyReport report = new PetDailyReport();
        report.setOrderId(order.getId());
        report.setPetId(order.getPetId());
        report.setReportDate(day);
        report.setMentalState("正常");
        report.setAppetite("正常");
        report.setWater("正常");
        report.setPoop("正常");
        report.setIsAbnormal(0);
        report.setActivityMinutes(30 + rnd.nextInt(30));
        report.setMealsSummary("狗粮 150g、清水充足");
        report.setStaffId(staffId);
        report.setCreateTime(day.atTime(17, 30));
        reportMapper.insert(report);
    }

    private CareTask taskOf(BoardingOrder order, LocalDate day, String time, String type,
                            String title, String content, Long staffId) {
        CareTask t = new CareTask();
        t.setOrderId(order.getId());
        t.setPetId(order.getPetId());
        t.setRoomId(order.getRoomId());
        t.setTaskType(type);
        t.setTaskDate(day);
        t.setPlanTime(time);
        t.setTitle(title);
        t.setContent(content);
        t.setStatus("DONE");
        t.setStaffId(staffId);
        t.setDoneTime(day.atTime(Integer.parseInt(time.split(":")[0]), Integer.parseInt(time.split(":")[1])));
        t.setCreateTime(day.atTime(0, 0));
        return t;
    }

    private void seedReview(BoardingOrder order, SysUser owner) {
        Review r = new Review();
        r.setOrderId(order.getId());
        r.setUserId(owner.getId());
        r.setPetId(order.getPetId());
        int star = 4 + rnd.nextInt(2);
        r.setRatingOverall(star);
        r.setRatingEnv(star);
        r.setRatingService(star);
        r.setRatingStaff(star);
        r.setRatingPetStatus(star);
        r.setContent(pick(
                "毛孩子玩得很开心，每天都能收到照片，很放心！",
                "环境干净，员工很负责，会再来。",
                "第一次寄养有些担心，但日报很及时，好评。",
                "整体不错，就是旺季房间有点紧张。"));
        r.setTags("环境干净,服务很好,工作人员负责");
        r.setCreateTime(LocalDateTime.now().minusDays(rnd.nextInt(30)));
        reviewMapper.insert(r);
    }

    // ---------- 其它增值数据 ----------

    private void seedExtras() {
        seedServiceBookings();
        seedCoupons();
        seedAnnouncements();
        seedStories();
    }

    private void seedServiceBookings() {
        if (serviceBookingMapper.selectCount(null) > 0) return;
        List<Pet> pets = petMapper.selectList(null);
        String[] names = {"洗澡", "美容", "单独遛宠", "宠物摄影"};
        BigDecimal[] prices = {new BigDecimal("35"), new BigDecimal("60"), new BigDecimal("20"), new BigDecimal("30")};
        String[] times = {"09:00", "10:30", "14:00", "16:00"};
        for (int i = 0; i < 8 && !pets.isEmpty(); i++) {
            Pet pet = pets.get(rnd.nextInt(pets.size()));
            int si = rnd.nextInt(names.length);
            ServiceBooking b = new ServiceBooking();
            b.setUserId(pet.getUserId());
            b.setPetId(pet.getId());
            b.setServiceId((long) (si + 1));
            b.setServiceName(names[si]);
            b.setPrice(prices[si]);
            b.setBookDate(LocalDate.now().plusDays(rnd.nextInt(7)));
            b.setBookTime(times[rnd.nextInt(times.length)]);
            b.setStatus(rnd.nextBoolean() ? "CONFIRMED" : "PENDING");
            b.setCreateTime(LocalDateTime.now().minusDays(rnd.nextInt(5)));
            serviceBookingMapper.insert(b);
        }
    }

    private void seedCoupons() {
        if (couponMapper.selectCount(null) > 0) return;
        Coupon c1 = new Coupon();
        c1.setName("新人寄养立减券");
        c1.setType("DISCOUNT");
        c1.setThreshold(new BigDecimal("100"));
        c1.setAmount(new BigDecimal("30"));
        c1.setTotalCount(100);
        c1.setIssuedCount(0);
        c1.setStartDate(LocalDate.now().minusDays(10));
        c1.setEndDate(LocalDate.now().plusDays(60));
        c1.setStatus(1);
        couponMapper.insert(c1);

        Coupon c2 = new Coupon();
        c2.setName("洗护 8 折券");
        c2.setType("PERCENT");
        c2.setPercent(80);
        c2.setTotalCount(50);
        c2.setIssuedCount(0);
        c2.setStartDate(LocalDate.now().minusDays(5));
        c2.setEndDate(LocalDate.now().plusDays(30));
        c2.setStatus(1);
        couponMapper.insert(c2);
    }

    private void seedAnnouncements() {
        if (announcementMapper.selectCount(null) > 0) return;
        insertAnn("🌿 春季寄养优惠季", "即日起预订 5 晚以上寄养可享 9 折，先到先得！", "ACTIVITY", 1);
        insertAnn("宠物寄养安全须知", "入住请携带疫苗本；有过敏史或用药的宠物请提前告知前台。", "SAFETY", 0);
        insertAnn("节假日床位预约提醒", "假期为寄养高峰期，建议提前 2 周预约房间。", "NOTICE", 0);
    }

    private void insertAnn(String title, String content, String type, int pinned) {
        Announcement a = new Announcement();
        a.setTitle(title);
        a.setContent(content);
        a.setType(type);
        a.setPinned(pinned);
        a.setStatus(1);
        a.setCreateTime(LocalDateTime.now().minusDays(rnd.nextInt(20)));
        announcementMapper.insert(a);
    }

    private void seedStories() {
        if (storyMapper.selectCount(null) > 0) return;
        String[][] rows = {
                {"豆豆的快乐寄养周记", "第一次送豆豆寄养很担心，结果每天都能看到它的照片，回家还胖了一圈！"},
                {"高冷咪咪居然主动蹭人", "咪咪一向高冷，这次寄养回来居然会主动蹭护理师，看来是真舒服～"},
                {"大圣在宠物乐园的一天", "边牧的精力终于有地方释放，店员说它玩到累趴才肯回房。"},
                {"寄养小贴士", "记得带上毛孩子熟悉的垫子，它会更有安全感。"},
        };
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));
        if (users.isEmpty()) return;
        for (String[] row : rows) {
            PetStory s = new PetStory();
            s.setUserId(users.get(rnd.nextInt(users.size())).getId());
            s.setTitle(row[0]);
            s.setContent(row[1]);
            s.setLikes(rnd.nextInt(60));
            s.setStatus(1);
            s.setCreateTime(LocalDateTime.now().minusDays(rnd.nextInt(30) + 1));
            storyMapper.insert(s);
        }
    }

    private Long findUserId(String username) {
        SysUser u = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        return u == null ? null : u.getId();
    }

    private String pick(String... arr) {
        return arr[rnd.nextInt(arr.length)];
    }
}
