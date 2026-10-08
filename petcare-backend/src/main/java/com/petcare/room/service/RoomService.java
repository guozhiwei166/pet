package com.petcare.room.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.boarding.entity.BoardingOrder;
import com.petcare.boarding.mapper.BoardingOrderMapper;
import com.petcare.common.BusinessException;
import com.petcare.room.entity.Room;
import com.petcare.room.entity.RoomType;
import com.petcare.room.entity.ServiceItem;
import com.petcare.room.mapper.RoomMapper;
import com.petcare.room.mapper.RoomTypeMapper;
import com.petcare.room.mapper.ServiceMapper;
import com.petcare.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomMapper roomMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final ServiceMapper serviceMapper;
    private final BoardingOrderMapper boardingOrderMapper;
    private final com.petcare.pet.mapper.PetMapper petMapper;

    // ---------- 房型 ----------

    public List<RoomType> roomTypes(boolean onlyOnSale) {
        LambdaQueryWrapper<RoomType> w = new LambdaQueryWrapper<RoomType>().orderByAsc(RoomType::getSort);
        if (onlyOnSale) w.eq(RoomType::getStatus, 1);
        return roomTypeMapper.selectList(w);
    }

    public void saveRoomType(RoomType in) {
        requireAdmin();
        RoomType type = new RoomType();
        type.setId(in.getId());
        type.setName(in.getName());
        // 分区为显式字段（犬区/猫区），缺省按犬区处理，非法值直接拒绝
        String species = in.getSpecies() == null || in.getSpecies().isBlank()
                ? "DOG" : in.getSpecies().trim().toUpperCase();
        if (!SPECIES.contains(species)) throw new BusinessException("分区只能是犬区或猫区");
        type.setSpecies(species);
        type.setPrice(in.getPrice());
        type.setArea(in.getArea());
        type.setMaxPets(in.getMaxPets() == null ? 1 : in.getMaxPets());
        type.setFacilities(in.getFacilities());
        type.setDescription(in.getDescription());
        type.setPhoto(in.getPhoto());
        type.setStatus(in.getStatus() == null ? 1 : in.getStatus());
        type.setSort(in.getSort() == null ? 0 : in.getSort());
        if (type.getId() == null) {
            roomTypeMapper.insert(type);
            // 注意：不再自动建房——新房型由管理员在「房型管理 → 配置房间」中自行选择楼层/房间号创建
        } else {
            RoomType existing = roomTypeMapper.selectById(type.getId());
            if (existing == null) throw new BusinessException("房型不存在");
            type.setCreateTime(existing.getCreateTime());
            roomTypeMapper.updateById(type);
        }
    }

    @Transactional
    public void deleteRoomType(Long id) {
        requireAdmin();
        Long rooms = roomMapper.selectCount(new LambdaQueryWrapper<Room>().eq(Room::getRoomTypeId, id));
        if (rooms > 0) throw new BusinessException("该房型下仍有 " + rooms + " 间房间，请先移除房间");
        roomTypeMapper.deleteById(id);
    }

    // ---------- 房间 ----------

    /**
     * 房间时间约定：入住日 14:00 起算、离店日 12:00 结束。
     * 房间"当前是否被占用/已预约"只由订单时段与当前时间比较得出，不落地到 room.status，
     * 因此未来预约不会影响房间当前状态，到点自动切换（详见 TASK/需求：预约与占用分离）。
     */
    public static final LocalTime CHECK_IN_TIME = LocalTime.of(14, 0);
    public static final LocalTime CHECK_OUT_TIME = LocalTime.of(12, 0);
    private static final DateTimeFormatter RANGE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    /** 占用房间的订单状态（含未来预约） */
    public static final List<String> HOLDING_STATUSES = List.of("CONFIRMED", "CHECKED_IN", "BOARDING");
    /** 物理状态白名单：逻辑删除/占用状态由订单派生，房间只存物理可用性 */
    private static final List<String> PHYSICAL_STATUSES = List.of("AVAILABLE", "CLEANING", "MAINTENANCE");

    public static LocalDateTime windowStart(BoardingOrder o) {
        return o.getStartDate().atTime(CHECK_IN_TIME);
    }

    public static LocalDateTime windowEnd(BoardingOrder o) {
        return o.getEndDate().atTime(CHECK_OUT_TIME);
    }

    /** 时段文案：2026-09-20 14:00 ~ 2026-09-22 12:00 */
    public static String rangeText(BoardingOrder o) {
        return RANGE_FMT.format(windowStart(o)) + " ~ " + RANGE_FMT.format(windowEnd(o));
    }

    /** 该房间的全部占用中订单（CONFIRMED/CHECKED_IN/BOARDING），按入住时间升序 */
    public List<BoardingOrder> holdingOrders(Long roomId) {
        if (roomId == null) return List.of();
        return boardingOrderMapper.selectList(new LambdaQueryWrapper<BoardingOrder>()
                        .eq(BoardingOrder::getRoomId, roomId)
                        .in(BoardingOrder::getStatus, HOLDING_STATUSES)
                        .orderByAsc(BoardingOrder::getStartDate))
                .stream().sorted(Comparator.comparing(RoomService::windowStart)).collect(Collectors.toList());
    }

    /**
     * 房间地图：按楼层分组。每间房附带
     * status（当前有效状态，由订单派生）、physicalStatus（物理状态）、
     * current（当前在住/超期未离店）、reservations（全部未来预约，含起止时间文案）、deletable。
     */
    public List<Map<String, Object>> roomMap() {
        LocalDateTime now = LocalDateTime.now();
        List<Room> rooms = roomMapper.selectList(new LambdaQueryWrapper<Room>().orderByAsc(Room::getRoomNo));
        Map<Long, RoomType> types = roomTypeMapper.selectList(null).stream()
                .collect(Collectors.toMap(RoomType::getId, t -> t));
        // 一次性取出所有占用中订单，按房间分组，避免逐间查库
        Map<Long, List<BoardingOrder>> byRoom = boardingOrderMapper.selectList(new LambdaQueryWrapper<BoardingOrder>()
                        .in(BoardingOrder::getStatus, HOLDING_STATUSES)
                        .isNotNull(BoardingOrder::getRoomId))
                .stream().collect(Collectors.groupingBy(BoardingOrder::getRoomId));
        Map<Long, String> petNames = petNames(byRoom.values().stream().flatMap(List::stream).toList());

        return rooms.stream().collect(Collectors.groupingBy(Room::getFloor))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Map<String, Object> floor = new LinkedHashMap<>();
                    floor.put("floor", e.getKey());
                    floor.put("rooms", e.getValue().stream()
                            .map(r -> toMap(r, types, byRoom.getOrDefault(r.getId(), List.of()), petNames, now)).toList());
                    return floor;
                }).collect(Collectors.toList());
    }

    private Map<Long, String> petNames(List<BoardingOrder> orders) {
        List<Long> ids = orders.stream().map(BoardingOrder::getPetId).filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return Map.of();
        return petMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(com.petcare.pet.entity.Pet::getId, com.petcare.pet.entity.Pet::getName));
    }

    private Map<String, Object> toMap(Room room, Map<Long, RoomType> types, List<BoardingOrder> holds,
                                      Map<Long, String> petNames, LocalDateTime now) {
        List<BoardingOrder> sorted = holds.stream().sorted(Comparator.comparing(RoomService::windowStart)).toList();
        // 在住：办了入住就一直算占用（不看预约时段，避免提前入住被显示成"已预约"）；离店时间已过即为超期
        BoardingOrder stay = sorted.stream()
                .filter(o -> "CHECKED_IN".equals(o.getStatus()) || "BOARDING".equals(o.getStatus()))
                .findFirst().orElse(null);
        boolean overdue = stay != null && !now.isBefore(windowEnd(stay));
        // 已预约：订单已确认且当前时间处于预约时段内（到点自动切换，无需定时任务）
        BoardingOrder reserved = sorted.stream()
                .filter(o -> "CONFIRMED".equals(o.getStatus())
                        && !now.isBefore(windowStart(o)) && now.isBefore(windowEnd(o)))
                .findFirst().orElse(null);
        // 未来预约：尚未开始的已确认订单
        List<BoardingOrder> future = sorted.stream()
                .filter(o -> "CONFIRMED".equals(o.getStatus()) && windowStart(o).isAfter(now))
                .toList();

        String physical = room.getStatus() == null ? "AVAILABLE" : room.getStatus();
        String effective;
        if ("MAINTENANCE".equals(physical)) effective = "MAINTENANCE";
        else if (stay != null) effective = "OCCUPIED";
        else if (reserved != null) effective = "RESERVED";
        else if ("CLEANING".equals(physical)) effective = "CLEANING";
        else effective = "AVAILABLE";

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", room.getId());
        m.put("roomNo", room.getRoomNo());
        m.put("floor", room.getFloor());
        m.put("status", effective);              // 当前有效状态（派生）
        m.put("physicalStatus", physical);       // 物理状态（空闲/清洁中/维修中）
        m.put("note", room.getNote());
        RoomType type = types.get(room.getRoomTypeId());
        m.put("roomTypeId", room.getRoomTypeId());
        m.put("roomTypeName", type == null ? "—" : type.getName());
        m.put("species", type == null ? null : type.getSpecies());
        // 当前占用/预约（用于卡片与抽屉展示："预约【起 ~ 止】"）
        BoardingOrder shown = stay != null ? stay : reserved;
        if (shown != null) {
            m.put("orderId", shown.getId());
            m.put("petId", shown.getPetId());
            m.put("petName", petNames.get(shown.getPetId()));
            m.put("contactName", shown.getContactName());
            m.put("startDate", shown.getStartDate());
            m.put("endDate", shown.getEndDate());
            m.put("rangeText", rangeText(shown));
            m.put("occupied", stay != null);
            m.put("overdue", overdue);
        }
        m.put("reservations", future.stream().map(o -> reservationMap(o, petNames)).toList());
        m.put("reservationCount", future.size());
        // 有在住/预约的房间不允许删除
        m.put("deletable", holds.isEmpty());
        return m;
    }

    private Map<String, Object> reservationMap(BoardingOrder o, Map<Long, String> petNames) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("orderId", o.getId());
        r.put("orderNo", o.getOrderNo());
        r.put("petId", o.getPetId());
        r.put("petName", petNames.get(o.getPetId()));
        r.put("contactName", o.getContactName());
        r.put("contactPhone", o.getContactPhone());
        r.put("status", o.getStatus());
        r.put("startDate", o.getStartDate());
        r.put("endDate", o.getEndDate());
        r.put("startTime", RANGE_FMT.format(windowStart(o)));
        r.put("endTime", RANGE_FMT.format(windowEnd(o)));
        r.put("rangeText", rangeText(o));
        return r;
    }

    public List<Room> rooms(String status) {
        LambdaQueryWrapper<Room> w = new LambdaQueryWrapper<Room>().orderByAsc(Room::getRoomNo);
        if (status != null && !status.isBlank() && !"ALL".equals(status)) w.eq(Room::getStatus, status);
        return roomMapper.selectList(w);
    }

    public void saveRoom(Room room) {
        requireAdmin();
        if (room.getRoomNo() == null || room.getRoomNo().isBlank()) throw new BusinessException("请填写房间号");
        if (room.getFloor() == null) throw new BusinessException("请填写楼层");
        if (room.getRoomTypeId() == null) throw new BusinessException("请选择房型");
        // 状态白名单：只接受物理状态（预约/入住状态由订单派生）
        String targetStatus = room.getStatus() == null ? "AVAILABLE" : room.getStatus();
        if (!PHYSICAL_STATUSES.contains(targetStatus)) {
            throw new BusinessException("房间状态只能是：空闲 / 清洁中 / 维修中");
        }

        if (room.getId() == null) {
            // 房号查重需包含逻辑删除记录：唯一索引不区分 deleted，重复会直接抛数据库异常
            if (roomMapper.selectAllRoomNos().contains(room.getRoomNo())) {
                throw new BusinessException("房间号 " + room.getRoomNo() + " 已被占用（可能是已删除房间保留的房号），请更换");
            }
            Room insert = new Room();
            insert.setRoomNo(room.getRoomNo());
            insert.setFloor(room.getFloor());
            insert.setRoomTypeId(room.getRoomTypeId());
            insert.setStatus(targetStatus);
            insert.setNote(room.getNote());
            roomMapper.insert(insert);
        } else {
            Room old = roomMapper.selectById(room.getId());
            if (old == null) throw new BusinessException("房间不存在");
            if (hasInHouse(orderIdsOf(old.getId()))) {
                throw new BusinessException("房间有宠物入住中，不能直接修改");
            }
            old.setRoomNo(room.getRoomNo());
            old.setFloor(room.getFloor());
            old.setRoomTypeId(room.getRoomTypeId());
            old.setStatus(targetStatus);
            old.setNote(room.getNote());
            // currentOrderId 保持原值（由订单流程控制），禁止客户端篡改
            roomMapper.updateById(old);
        }
    }

    /**
     * 批量创建房间（管理员在房间地图为房型选楼层/房号建房）：
     * 指定楼层 + 起始房号 + 数量，逐个校验房号唯一后插入为 AVAILABLE。
     * 全部成功或整体失败（事务）；返回实际创建的房间号列表。
     */
    @Transactional
    public List<String> batchCreateRooms(Long roomTypeId, Integer floor, String startNo, Integer count) {
        requireAdmin();
        RoomType type = roomTypeMapper.selectById(roomTypeId);
        if (type == null) throw new BusinessException("房型不存在");
        if (floor == null || floor < 1) throw new BusinessException("请选择有效的楼层");
        if (startNo == null || startNo.isBlank()) throw new BusinessException("请填写起始房间号");
        if (count == null || count < 1 || count > 50) throw new BusinessException("单次创建数量需在 1-50 间");
        if (!startNo.matches("^[A-Za-z0-9]{1,10}$")) throw new BusinessException("房间号格式不正确（字母/数字，≤10位）");

        // 已占用房号集合（含已逻辑删除的记录，保证与 uk_room_no 唯一索引口径一致）
        java.util.Set<String> used = new java.util.HashSet<>(roomMapper.selectAllRoomNos());

        List<String> created = new java.util.ArrayList<>();
        int seq = 0;
        while (created.size() < count) {
            String no = nextRoomNo(startNo, seq++);
            if (no == null) throw new BusinessException("起始房间号格式需以数字结尾以便递增");
            if (used.contains(no)) continue; // 跳过已占用房号（自动顺延）
            Room r = new Room();
            r.setRoomNo(no);
            r.setFloor(floor);
            r.setRoomTypeId(roomTypeId);
            r.setStatus("AVAILABLE");
            r.setNote("房型「" + type.getName() + "」房间");
            roomMapper.insert(r);
            used.add(no);
            created.add(no);
            if (seq > 500) throw new BusinessException("可用房号耗尽，请更换起始房号");
        }
        return created;
    }

    /** 房号递增：数字后缀 +1；无数字后缀返回 null */
    private String nextRoomNo(String start, int step) {
        int i = start.length();
        while (i > 0 && Character.isDigit(start.charAt(i - 1))) i--;
        if (i == start.length()) return null;
        String prefix = start.substring(0, i);
        long num = Long.parseLong(start.substring(i)) + step;
        return prefix + num;
    }

    /** 房型分区白名单：犬区 / 猫区 */
    private static final List<String> SPECIES = List.of("DOG", "CAT");

    /**
     * 手工调整房间状态：只接受物理状态（空闲/清洁中/维修中）。
     * 「已预约/入住中」由订单时段派生，不允许手工设置；房间当前有宠物在住时也不能改物理状态。
     */
    @Transactional
    public void changeRoomStatus(Long id, String status) {
        requireStaff();
        Room room = roomMapper.selectById(id);
        if (room == null) throw new BusinessException("房间不存在");
        if (status == null || !PHYSICAL_STATUSES.contains(status)) {
            throw new BusinessException("只能设置为：空闲 / 清洁中 / 维修中（预约与入住状态由订单自动维护）");
        }
        BoardingOrder active = holdingOrders(id).stream()
                .filter(o -> "CHECKED_IN".equals(o.getStatus()) || "BOARDING".equals(o.getStatus()))
                .findFirst().orElse(null);
        if (active != null) {
            throw new BusinessException("该房间有宠物入住中，请先办理离店");
        }
        room.setStatus(status);
        roomMapper.updateById(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        requireAdmin();
        Room room = roomMapper.selectById(id);
        if (room == null) throw new BusinessException("房间不存在");
        if (!holdingOrders(id).isEmpty()) {
            throw new BusinessException("该房间存在预约或在住订单，不能删除");
        }
        removeRoom(room);
    }

    /**
     * 批量删除房间（管理端房间地图多选删除）：
     * 只删没有预约/在住订单的房间，其余跳过并在结果里返回房号，便于前端提示"哪几间没删掉"。
     */
    @Transactional
    public Map<String, Object> deleteRooms(List<Long> ids) {
        requireAdmin();
        if (ids == null || ids.isEmpty()) throw new BusinessException("请选择要删除的房间");
        if (ids.size() > 100) throw new BusinessException("单次最多删除 100 间房间");
        List<String> deleted = new java.util.ArrayList<>();
        List<String> skipped = new java.util.ArrayList<>();
        for (Long id : ids) {
            Room room = roomMapper.selectById(id);
            if (room == null) continue;
            if (!holdingOrders(id).isEmpty()) {
                skipped.add(room.getRoomNo());
                continue;
            }
            removeRoom(room);
            deleted.add(room.getRoomNo());
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("deleted", deleted);
        res.put("skipped", skipped);
        return res;
    }

    private List<Long> orderIdsOf(Long roomId) {
        return holdingOrders(roomId).stream().map(BoardingOrder::getId).toList();
    }

    private boolean hasInHouse(List<Long> orderIds) {
        if (orderIds.isEmpty()) return false;
        return boardingOrderMapper.selectCount(new LambdaQueryWrapper<BoardingOrder>()
                .in(BoardingOrder::getId, orderIds)
                .in(BoardingOrder::getStatus, List.of("CHECKED_IN", "BOARDING"))) > 0;
    }

    /**
     * 删除单个空闲房间：逻辑删除前先把房号改成占位符「#id」。
     * uk_room_no 唯一索引不区分 deleted，不改房号的话该房号会被已删除记录永久占用，后续无法再用。
     */
    private void removeRoom(Room room) {
        roomMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Room>()
                .eq(Room::getId, room.getId())
                .set(Room::getRoomNo, "#" + room.getId()));
        roomMapper.deleteById(room.getId());
    }

    // ---------- 服务 ----------

    public List<ServiceItem> services(boolean onlyOnSale) {
        LambdaQueryWrapper<ServiceItem> w = new LambdaQueryWrapper<ServiceItem>().orderByAsc(ServiceItem::getSort);
        if (onlyOnSale) w.eq(ServiceItem::getStatus, 1);
        return serviceMapper.selectList(w);
    }

    public void saveService(ServiceItem in) {
        requireAdmin();
        if (in.getName() == null || in.getName().isBlank()) throw new BusinessException("请填写服务名称");
        if (in.getPrice() == null || in.getPrice().signum() < 0) throw new BusinessException("请填写正确的价格");
        ServiceItem svc = new ServiceItem();
        svc.setId(in.getId());
        svc.setName(in.getName());
        svc.setPhoto(in.getPhoto());
        svc.setPrice(in.getPrice());
        svc.setUnit(in.getUnit() == null ? "次" : in.getUnit());
        svc.setDescription(in.getDescription());
        svc.setStatus(in.getStatus() == null ? 1 : in.getStatus());
        svc.setSort(in.getSort() == null ? 0 : in.getSort());
        if (svc.getId() == null) serviceMapper.insert(svc);
        else {
            ServiceItem existing = serviceMapper.selectById(svc.getId());
            if (existing == null) throw new BusinessException("服务不存在");
            svc.setCreateTime(existing.getCreateTime());
            serviceMapper.updateById(svc);
        }
    }

    @Transactional
    public void deleteService(Long id) {
        requireAdmin();
        serviceMapper.deleteById(id);
    }

    /** 指定日期区间内各房型的可订房间数（预订流程使用） */
    public Map<Long, Integer> availableCountByType(LocalDate start, LocalDate end) {
        List<Room> rooms = roomMapper.selectList(new LambdaQueryWrapper<Room>()
                .in(Room::getStatus, "AVAILABLE", "RESERVED"));
        Map<Long, List<Room>> byType = rooms.stream().collect(Collectors.groupingBy(Room::getRoomTypeId));
        Map<Long, Integer> res = new LinkedHashMap<>();
        for (Map.Entry<Long, List<Room>> e : byType.entrySet()) {
            int available = 0;
            for (Room room : e.getValue()) {
                Long overlap = boardingOrderMapper.selectCount(new LambdaQueryWrapper<BoardingOrder>()
                        .eq(BoardingOrder::getRoomId, room.getId())
                        .in(BoardingOrder::getStatus, "PAID", "CONFIRMED", "CHECKED_IN", "BOARDING")
                        .lt(BoardingOrder::getStartDate, end)
                        .gt(BoardingOrder::getEndDate, start));
                if (overlap == 0) available++;
            }
            res.put(e.getKey(), available);
        }
        return res;
    }

    private void requireAdmin() {
        if (!"ROLE_ADMIN".equals(SecurityUtils.currentRole())) throw new BusinessException(403, "仅管理员可操作");
    }

    private void requireStaff() {
        String role = SecurityUtils.currentRole();
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_STAFF".equals(role)) throw new BusinessException(403, "仅工作人员可操作");
    }
}
