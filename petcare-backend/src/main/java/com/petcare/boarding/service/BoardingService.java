package com.petcare.boarding.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.boarding.entity.BoardingOrder;
import com.petcare.boarding.entity.BoardingOrderServiceItem;
import com.petcare.boarding.dto.OrderCreateDTO;
import com.petcare.boarding.mapper.BoardingOrderMapper;
import com.petcare.boarding.mapper.BoardingOrderServiceItemMapper;
import com.petcare.boarding.vo.OrderDetailVO;
import com.petcare.common.BusinessException;
import com.petcare.notification.NotificationService;
import com.petcare.payment.entity.Payment;
import com.petcare.payment.mapper.PaymentMapper;
import com.petcare.pet.entity.Pet;
import com.petcare.pet.mapper.PetMapper;
import com.petcare.room.entity.Room;
import com.petcare.room.entity.RoomType;
import com.petcare.room.mapper.RoomMapper;
import com.petcare.room.mapper.RoomTypeMapper;
import com.petcare.room.service.RoomService;
import com.petcare.security.LoginUser;
import com.petcare.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 寄养订单服务：availability 计算、订单状态机、模拟支付、房间分配
 *
 * 状态机：PENDING_PAYMENT → PAID → CONFIRMED → CHECKED_IN → BOARDING → COMPLETED
 * 旁路：PENDING_PAYMENT/PAID → CANCELLED；PAID/CONFIRMED → REFUNDING → REFUNDED
 * 并发控制：订单创建只校验库存；入住前确认（confirm）时以数据库条件 UPDATE 原子占房
 */
@Service
@RequiredArgsConstructor
public class BoardingService {

    /**
     * 占用中状态：PENDING_PAYMENT 也计入库存占用（下单即锁库存，防止超卖；取消后自动释放）。
     * 支付超时未支付的订单由门店在管理端人工取消释放。
     */
    private static final List<String> OCCUPYING = List.of("PENDING_PAYMENT", "PAID", "CONFIRMED", "CHECKED_IN", "BOARDING");

    private final BoardingOrderMapper orderMapper;
    private final BoardingOrderServiceItemMapper orderServiceMapper;
    private final PaymentMapper paymentMapper;
    private final PetMapper petMapper;
    private final com.petcare.room.mapper.RoomMapper roomMapper;
    private final com.petcare.room.mapper.RoomTypeMapper roomTypeMapper;
    private final com.petcare.room.mapper.ServiceMapper serviceMapper;
    private final NotificationService notificationService;
    private final com.petcare.care.service.CareService careService;
    private final com.petcare.review.mapper.ReviewMapper reviewMapper;
    private final com.petcare.marketing.mapper.UserCouponMapper userCouponMapper;
    private final com.petcare.marketing.mapper.CouponMapper couponMapper;
    private final com.petcare.room.service.RoomService roomService;
    private final com.petcare.care.mapper.CareTaskMapper careTaskMapper;
    private final com.petcare.log.mapper.OperationLogMapper operationLogMapper;

    // ---------- 可订计算 ----------

    /**
     * 日期区间内各房型的库存情况：总房间数、每天可用数（供日历标色 🟢🟡🔴）
     * 下架房型(status=0)也一并返回，供前端展示"已下架"而非空白（不可下单）。
     * 容量口径：非维修中的房间都可作为容量（清洁中只是暂时状态，未来日期仍可预订）。
     */
    public List<Map<String, Object>> availability(LocalDate start, LocalDate end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new BusinessException("离店日期需晚于入住日期");
        }
        List<RoomType> types = roomTypeMapper.selectList(
                new LambdaQueryWrapper<RoomType>().orderByAsc(RoomType::getSort));
        List<Room> rooms = roomMapper.selectList(new LambdaQueryWrapper<Room>()
                .ne(Room::getStatus, "MAINTENANCE"));
        List<BoardingOrder> occupying = orderMapper.selectList(new LambdaQueryWrapper<BoardingOrder>()
                .in(BoardingOrder::getStatus, OCCUPYING)
                .lt(BoardingOrder::getStartDate, end)
                .gt(BoardingOrder::getEndDate, start));

        Map<Long, List<Room>> roomsByType = rooms.stream()
                .collect(Collectors.groupingBy(Room::getRoomTypeId));

        List<Map<String, Object>> res = new ArrayList<>();
        for (RoomType type : types) {
            List<Room> typeRooms = roomsByType.getOrDefault(type.getId(), List.of());
            Map<String, Integer> perDay = new LinkedHashMap<>();
            int minAvailable = Integer.MAX_VALUE;
            for (LocalDate d = start; d.isBefore(end); d = d.plusDays(1)) {
                final LocalDate day = d;
                long busy = occupying.stream()
                        .filter(o -> type.getId().equals(o.getRoomTypeId()))
                        .filter(o -> o.getRoomId() == null || typeRooms.stream().anyMatch(r -> r.getId().equals(o.getRoomId())))
                        .filter(o -> o.getStartDate().isBefore(day.plusDays(1)) && o.getEndDate().isAfter(day))
                        .count();
                int available = Math.max(0, typeRooms.size() - (int) busy);
                perDay.put(day.toString(), available);
                minAvailable = Math.min(minAvailable, available);
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("roomType", type);
            item.put("totalRooms", typeRooms.size());
            item.put("perDay", perDay);
            item.put("minAvailable", minAvailable);
            // 下架房型或已无可用房 → 不可订
            boolean onSale = type.getStatus() != null && type.getStatus() == 1;
            item.put("onSale", onSale);
            item.put("bookable", onSale && minAvailable > 0);
            res.add(item);
        }
        return res;
    }

    // ---------- 创建订单 ----------

    @Transactional
    public BoardingOrder create(OrderCreateDTO dto) {
        Long uid = SecurityUtils.currentId();
        Pet pet = petMapper.selectById(dto.petId());
        if (pet == null || !pet.getUserId().equals(uid)) throw new BusinessException("宠物不存在");
        if (dto.endDate() == null || dto.startDate() == null || !dto.endDate().isAfter(dto.startDate())) {
            throw new BusinessException("离店日期需晚于入住日期");
        }
        if (dto.startDate().isBefore(LocalDate.now())) throw new BusinessException("不能预约过去的日期");

        // 行级写锁锁定房型：同房型并发下单在此串行，后到者在事务内重查库存时能看到前者的占用，
        // 消除"先查库存再插入"的并发超卖窗口（跨房型下单不受影响）
        RoomType type = roomTypeMapper.selectByIdForUpdate(dto.roomTypeId());
        if (type == null || type.getStatus() != 1) throw new BusinessException("房型已下架");
        // 分区校验：猫宠只能订猫区房型，狗宠只能订犬区房型（分区为房型显式字段，不依赖名称）
        requireSpeciesMatch(type, pet);

        // 防重复预订：同一宠物不允许存在时间重叠的占用中订单
        Long petOverlap = orderMapper.selectCount(new LambdaQueryWrapper<BoardingOrder>()
                .eq(BoardingOrder::getPetId, dto.petId())
                .in(BoardingOrder::getStatus, OCCUPYING)
                .lt(BoardingOrder::getStartDate, dto.endDate())
                .gt(BoardingOrder::getEndDate, dto.startDate()));
        if (petOverlap != null && petOverlap > 0) {
            throw new BusinessException("该宠物在所选日期已有寄养安排，请更换时间");
        }

        // 库存校验：区间内每一天该房型可用房间数必须 > 0
        for (Map<String, Object> item : availability(dto.startDate(), dto.endDate())) {
            if (item.get("roomType").equals(type) && (int) item.get("minAvailable") <= 0) {
                throw new BusinessException("所选日期该房型已订满，请更换日期或房型");
            }
        }

        long days = ChronoUnit.DAYS.between(dto.startDate(), dto.endDate());
        BigDecimal roomAmount = type.getPrice().multiply(BigDecimal.valueOf(days));

        // 服务明细与金额
        List<BoardingOrderServiceItem> items = new ArrayList<>();
        BigDecimal serviceAmount = BigDecimal.ZERO;
        if (dto.services() != null) {
            for (OrderCreateDTO.ServiceChoice sc : dto.services()) {
                if (sc.serviceId() == null) continue;
                var svc = serviceMapper.selectById(sc.serviceId());
                if (svc == null || svc.getStatus() != 1) throw new BusinessException("存在已下架的服务，请重新选择");
                // 按"天"计费的服务：未指定数量时默认按整段寄养晚数计价（如特殊饮食）
                int qty = sc.quantity() == null || sc.quantity() < 1
                        ? ("天".equals(svc.getUnit()) ? (int) days : 1)
                        : sc.quantity();
                BigDecimal amount = svc.getPrice().multiply(BigDecimal.valueOf(qty));
                BoardingOrderServiceItem item = new BoardingOrderServiceItem();
                item.setServiceId(svc.getId());
                item.setServiceName(svc.getName());
                item.setPrice(svc.getPrice());
                item.setUnit(svc.getUnit());
                item.setQuantity(qty);
                item.setAmount(amount);
                items.add(item);
                serviceAmount = serviceAmount.add(amount);
            }
        }

        BoardingOrder order = new BoardingOrder();
        order.setOrderNo("PC" + bizNo());
        order.setUserId(uid);
        order.setPetId(pet.getId());
        order.setRoomTypeId(type.getId());
        order.setStartDate(dto.startDate());
        order.setEndDate(dto.endDate());
        order.setDays((int) days);
        order.setRoomAmount(roomAmount);
        order.setServiceAmount(serviceAmount);
        BigDecimal total = roomAmount.add(serviceAmount);
        BigDecimal discount = BigDecimal.ZERO;
        // 优惠券抵扣：校验券归属/未用/有效期，计算最优或指定券
        if (dto.userCouponId() != null) {
            com.petcare.marketing.entity.UserCoupon uc = userCouponMapper.selectById(dto.userCouponId());
            if (uc == null || !uc.getUserId().equals(uid) || !"UNUSED".equals(uc.getStatus())) {
                throw new BusinessException("优惠券不可用");
            }
            com.petcare.marketing.entity.Coupon c = couponMapper.selectById(uc.getCouponId());
            if (c == null || c.getStatus() != 1) throw new BusinessException("优惠券已失效");
            if (c.getEndDate() != null && c.getEndDate().isBefore(LocalDate.now())) {
                throw new BusinessException("优惠券已过期");
            }
            if ("DISCOUNT".equals(c.getType())) {
                if (total.compareTo(c.getThreshold() == null ? BigDecimal.ZERO : c.getThreshold()) >= 0) {
                    discount = c.getAmount() == null ? BigDecimal.ZERO : c.getAmount();
                } else throw new BusinessException("未达满减门槛 ￥" + c.getThreshold());
            } else if ("PERCENT".equals(c.getType())) {
                discount = total.multiply(BigDecimal.valueOf(100 - (c.getPercent() == null ? 100 : c.getPercent())))
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            }
            if (discount.compareTo(total) > 0) discount = total;
            // 券在订单落库后正式占用（见下方 insert 之后）
        }
        order.setDiscountAmount(discount);
        order.setTotalAmount(total.subtract(discount));
        order.setStatus("PENDING_PAYMENT");
        order.setContactName(dto.contactName());
        order.setContactPhone(dto.contactPhone());
        order.setRemark(dto.remark());
        orderMapper.insert(order);

        // 优惠券正式占用：绑定到本订单
        if (dto.userCouponId() != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            com.petcare.marketing.entity.UserCoupon uc = userCouponMapper.selectById(dto.userCouponId());
            if (uc != null) {
                uc.setStatus("USED");
                uc.setUsedOrderId(order.getId());
                uc.setUseTime(java.time.LocalDateTime.now());
                userCouponMapper.updateById(uc);
            }
        }

        for (BoardingOrderServiceItem item : items) {
            item.setOrderId(order.getId());
            orderServiceMapper.insert(item);
        }

        notificationService.send(uid, "下单成功",
                "订单 " + order.getOrderNo() + " 已创建，支付后门店将尽快确认。", "REMINDER", "ORDER", order.getId());
        return order;
    }

    // ---------- 状态机 ----------

    /** 模拟支付（答辩演示；保留 payment 流水模型，可替换为支付宝沙箱） */
    @Transactional
    public void pay(Long orderId) {
        BoardingOrder order = getMineAsOwner(orderId);
        mustStatus(order, "PENDING_PAYMENT");
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        Payment p = new Payment();
        p.setPaymentNo("PAY" + bizNo());
        p.setOrderId(order.getId());
        p.setUserId(order.getUserId());
        p.setAmount(order.getTotalAmount());
        p.setStatus("SUCCESS");
        p.setChannel("SIMULATED");
        p.setPayTime(LocalDateTime.now());
        paymentMapper.insert(p);

        notificationService.sendToRole("ROLE_ADMIN", "新订单待确认",
                "订单 " + order.getOrderNo() + " 已支付，请确认并分配房间。", "REMINDER", "ORDER", order.getId());
    }

    @Transactional
    public void cancel(Long orderId, String reason) {
        BoardingOrder order = getMineAsOwner(orderId);
        if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"PAID".equals(order.getStatus())) {
            throw new BusinessException("当前状态不支持取消（已入住订单请联系门店）");
        }
        order.setStatus("CANCELLED");
        order.setCancelReason(reason == null || reason.isBlank() ? "用户主动取消" : reason);
        // 若订单占用了优惠券，取消后释放为未使用
        if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            com.petcare.marketing.entity.UserCoupon uc = userCouponMapper.selectOne(
                    new LambdaQueryWrapper<com.petcare.marketing.entity.UserCoupon>()
                            .eq(com.petcare.marketing.entity.UserCoupon::getUsedOrderId, order.getId()));
            if (uc != null) {
                uc.setStatus("UNUSED");
                uc.setUsedOrderId(null);
                userCouponMapper.updateById(uc);
            }
        }
        orderMapper.updateById(order);
        if (order.getPayTime() != null) {
            paymentMapper.insert(snapshot(order, "CANCELLED"));
        }
        // 通知主人（消息中心可查、点击回订单详情）与管理员
        notificationService.send(order.getUserId(), "订单已取消",
                "订单 " + order.getOrderNo() + "（" + order.getStartDate() + " 入住）已取消，原因："
                        + order.getCancelReason() + "。如需继续寄养请重新下单。",
                "REMINDER", "ORDER", order.getId());
        notificationService.sendToRole("ROLE_ADMIN", "订单已取消",
                "订单 " + order.getOrderNo() + " 已被主人取消。", "REMINDER", "ORDER", order.getId());
    }

    /**
     * 可分配房间候选（手动分房用）：返回该订单房型下的全部房间，并标注
     * 当前占用（在住/超期未离店）、未来预约、以及本订单是否可直接分配/需确认/不可分配。
     * 管理员 = 全部房间；员工 = 仅空闲且无当前冲突的房间可选（其余仅展示并给出原因）。
     */
    public Map<String, Object> roomCandidates(Long orderId) {
        BoardingOrder order = requireStaffOrder(orderId);
        if (!List.of("PAID", "CONFIRMED").contains(order.getStatus())) {
            throw new BusinessException("当前订单状态不支持分配房间（当前：" + order.getStatus() + "）");
        }
        boolean admin = "ROLE_ADMIN".equals(SecurityUtils.currentRole());
        LocalDateTime now = LocalDateTime.now();
        List<Room> rooms = roomMapper.selectList(new LambdaQueryWrapper<Room>()
                .eq(Room::getRoomTypeId, order.getRoomTypeId())
                .orderByAsc(Room::getRoomNo));
        Map<Long, String> petNames = petNamesOfRooms(rooms);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Room room : rooms) {
            List<BoardingOrder> holds = roomService.holdingOrders(room.getId()).stream()
                    .filter(o -> !o.getId().equals(order.getId()))
                    .toList();
            BoardingOrder inHouse = holds.stream()
                    .filter(o -> ("CHECKED_IN".equals(o.getStatus()) || "BOARDING".equals(o.getStatus())))
                    .findFirst().orElse(null);
            boolean overdue = inHouse != null && !now.isBefore(RoomService.windowEnd(inHouse));
            BoardingOrder reservedNow = holds.stream()
                    .filter(o -> "CONFIRMED".equals(o.getStatus())
                            && !now.isBefore(RoomService.windowStart(o)) && now.isBefore(RoomService.windowEnd(o)))
                    .findFirst().orElse(null);
            // 与本订单时段重叠的未来预约
            List<BoardingOrder> overlapping = holds.stream()
                    .filter(o -> RoomService.windowStart(o).isBefore(RoomService.windowEnd(order))
                            && RoomService.windowEnd(o).isAfter(RoomService.windowStart(order)))
                    .toList();
            List<BoardingOrder> future = holds.stream()
                    .filter(o -> "CONFIRMED".equals(o.getStatus()) && RoomService.windowStart(o).isAfter(now))
                    .toList();

            String physical = room.getStatus() == null ? "AVAILABLE" : room.getStatus();
            String effective;
            if ("MAINTENANCE".equals(physical)) effective = "MAINTENANCE";
            else if (inHouse != null || reservedNow != null) effective = "OCCUPIED";
            else if ("CLEANING".equals(physical)) effective = "CLEANING";
            else if (!future.isEmpty()) effective = "RESERVED";
            else effective = "AVAILABLE";

            // 可分配性：管理员可确认后分配（软冲突），员工仅可分配完全空闲的房间
            String blockReason = null;
            boolean needConfirm = false;
            if ("MAINTENANCE".equals(physical)) {
                blockReason = "维修中";
            } else if (inHouse != null) {
                blockReason = overdue ? "有宠物入住且已超期未离店" : "当前有宠物入住";
            } else if (reservedNow != null) {
                blockReason = "当前时段已有预约未办理入住";
            } else if ("CLEANING".equals(physical)) {
                if (admin) needConfirm = true;
                else blockReason = "清洁中";
            } else if (!overlapping.isEmpty()) {
                if (admin) needConfirm = true;
                else blockReason = "与已有预约时段重叠";
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", room.getId());
            m.put("roomNo", room.getRoomNo());
            m.put("floor", room.getFloor());
            m.put("physicalStatus", physical);
            m.put("status", effective);
            m.put("currentOrderId", inHouse != null ? inHouse.getId() : (reservedNow != null ? reservedNow.getId() : null));
            m.put("currentPetName", inHouse != null ? petNames.get(inHouse.getPetId())
                    : (reservedNow != null ? petNames.get(reservedNow.getPetId()) : null));
            m.put("currentRangeText", inHouse != null ? RoomService.rangeText(inHouse)
                    : (reservedNow != null ? RoomService.rangeText(reservedNow) : null));
            m.put("overdue", overdue);
            m.put("reservations", future.stream().map(o -> reservationBrief(o, petNames)).toList());
            m.put("conflicts", overlapping.stream().map(o -> reservationBrief(o, petNames)).toList());
            m.put("assignable", blockReason == null);
            m.put("needConfirm", blockReason == null && needConfirm);
            m.put("blockReason", blockReason);
            list.add(m);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        Pet orderPet = petMapper.selectById(order.getPetId());
        res.put("orderId", order.getId());
        res.put("orderNo", order.getOrderNo());
        res.put("roomTypeId", order.getRoomTypeId());
        RoomType orderType = roomTypeMapper.selectById(order.getRoomTypeId());
        res.put("roomTypeName", orderType == null ? "" : orderType.getName());
        res.put("startTime", RoomService.rangeText(order).split(" ~ ")[0]);
        res.put("endTime", RoomService.rangeText(order).split(" ~ ")[1]);
        res.put("roomRangeText", RoomService.rangeText(order));
        res.put("petName", orderPet == null ? null : orderPet.getName());
        res.put("contactName", order.getContactName());
        res.put("role", admin ? "ADMIN" : "STAFF");
        res.put("rooms", list);
        return res;
    }

    private Map<Long, String> petNamesOfRooms(List<Room> rooms) {
        List<Long> ids = rooms.stream().map(Room::getId).toList();
        if (ids.isEmpty()) return Map.of();
        List<BoardingOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<BoardingOrder>()
                .in(BoardingOrder::getRoomId, ids)
                .in(BoardingOrder::getStatus, RoomService.HOLDING_STATUSES));
        List<Long> petIds = orders.stream().map(BoardingOrder::getPetId).filter(java.util.Objects::nonNull).distinct().toList();
        if (petIds.isEmpty()) return Map.of();
        return petMapper.selectBatchIds(petIds).stream()
                .collect(Collectors.toMap(Pet::getId, Pet::getName));
    }

    private Map<String, Object> reservationBrief(BoardingOrder o, Map<Long, String> petNames) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("orderId", o.getId());
        m.put("orderNo", o.getOrderNo());
        m.put("petName", petNames.get(o.getPetId()));
        m.put("contactName", o.getContactName());
        m.put("status", o.getStatus());
        m.put("rangeText", RoomService.rangeText(o));
        return m;
    }

    /**
     * 分配房间并确认订单（PAID → CONFIRMED）：由员工/管理员手动指定房间，不再自动挑房。
     * 硬性冲突（维修中 / 当前有宠物入住）直接拒绝；软冲突（清洁中 / 与新入住时段重叠的未来预约）
     * 需带 force=true 二次确认后放行。分配动作写入操作日志（操作人 / 时间 / 入住人 / 房间）。
     */
    @Transactional
    public void assignRoom(Long orderId, Long roomId, boolean force) {
        requireStaffOrAdmin();
        if (roomId == null) throw new BusinessException("请选择要分配的房间");
        BoardingOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!List.of("PAID", "CONFIRMED").contains(order.getStatus())) {
            throw new BusinessException("当前订单状态不支持分配房间（当前：" + order.getStatus() + "）");
        }
        Room room = roomMapper.selectById(roomId);
        if (room == null) throw new BusinessException("房间不存在");
        if (!room.getRoomTypeId().equals(order.getRoomTypeId())) {
            throw new BusinessException("房间「" + room.getRoomNo() + "」与订单房型不一致，请选择同房型房间");
        }
        if (roomId.equals(order.getRoomId())) throw new BusinessException("该订单已分配此房间");

        LocalDateTime now = LocalDateTime.now();
        List<BoardingOrder> holds = roomService.holdingOrders(roomId).stream()
                .filter(o -> !o.getId().equals(order.getId())).toList();
        BoardingOrder inHouse = holds.stream()
                .filter(o -> "CHECKED_IN".equals(o.getStatus()) || "BOARDING".equals(o.getStatus()))
                .findFirst().orElse(null);
        if (inHouse != null) {
            throw new BusinessException("该房间当前有宠物入住（" + inHouse.getStartDate() + " ~ " + inHouse.getEndDate()
                    + "），请先办理离店或另选房间");
        }
        if ("MAINTENANCE".equals(room.getStatus())) {
            throw new BusinessException("该房间维修中，不能分配");
        }
        BoardingOrder reservedNow = holds.stream()
                .filter(o -> "CONFIRMED".equals(o.getStatus())
                        && !now.isBefore(RoomService.windowStart(o)) && now.isBefore(RoomService.windowEnd(o)))
                .findFirst().orElse(null);
        if (reservedNow != null) {
            throw new BusinessException("该房间当前时段已被预约（" + RoomService.rangeText(reservedNow) + "），请先处理该预约");
        }
        // 软冲突：叠了未来预约 / 房间清洁中 → 管理员二次确认后可放行；员工不可越权覆盖
        boolean admin = "ROLE_ADMIN".equals(SecurityUtils.currentRole());
        List<BoardingOrder> overlapping = holds.stream()
                .filter(o -> RoomService.windowStart(o).isBefore(RoomService.windowEnd(order))
                        && RoomService.windowEnd(o).isAfter(RoomService.windowStart(order)))
                .toList();
        if ("CLEANING".equals(room.getStatus())) {
            if (!admin) throw new BusinessException("该房间清洁中，员工不能分配，请联系管理员");
            if (!force) throw new BusinessException("该房间清洁中，确认仍然分配吗？");
        }
        if (!overlapping.isEmpty()) {
            if (!admin) throw new BusinessException("该房间与已有预约时段重叠，员工不能覆盖，请联系管理员");
            if (!force) throw new BusinessException("该房间已有预约【" + RoomService.rangeText(overlapping.get(0)) + "】");
        }

        // 原子分配：未确认(force=false)时连重叠预约一起挡掉，避免并发把同一间房重复预订
        int updated = roomMapper.tryAssign(roomId, order.getId(),
                order.getStartDate(), order.getEndDate(), force ? 1 : 0);
        if (updated != 1) {
            throw new BusinessException("该房间刚刚被占用或已有重叠预约，请刷新后重新选择");
        }

        boolean firstConfirm = !"CONFIRMED".equals(order.getStatus());
        Long oldRoomId = order.getRoomId();
        order.setRoomId(roomId);
        order.setStatus("CONFIRMED");
        orderMapper.updateById(order);

        // 升级/改房场景：旧房间仅解除绑定，物理状态不动
        if (oldRoomId != null) {
            roomMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Room>()
                    .eq(Room::getId, oldRoomId)
                    .eq(Room::getCurrentOrderId, order.getId())
                    .set(Room::getCurrentOrderId, null));
        }
        // 换房时同步已生成照护任务的房间号
        careTaskMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.petcare.care.entity.CareTask>()
                .eq(com.petcare.care.entity.CareTask::getOrderId, order.getId())
                .set(com.petcare.care.entity.CareTask::getRoomId, roomId));

        if (firstConfirm) {
            // 首次确认：整段寄养期生成照护任务
            careService.generateTasks(order);
        }

        // 操作日志：操作人 / 时间 / 入住人 / 房间
        writeAssignLog(order, room);

        notificationService.send(order.getUserId(), "房间已预约",
                "您的订单 " + order.getOrderNo() + " 已预约房间 " + room.getRoomNo()
                        + "，入住时间 " + RoomService.rangeText(order) + "，入住当天请带好宠物用品。",
                "REMINDER", "ORDER", order.getId());
    }

    private void writeAssignLog(BoardingOrder order, Room room) {
        try {
            Pet pet = petMapper.selectById(order.getPetId());
            com.petcare.log.entity.OperationLogEntity log = new com.petcare.log.entity.OperationLogEntity();
            log.setModule("订单");
            log.setAction("分配房间");
            log.setUserId(SecurityUtils.currentId());
            log.setUsername(SecurityUtils.current().getUsername());
            log.setDetail("订单 " + order.getOrderNo()
                    + " | 房间 " + room.getRoomNo() + "（" + room.getFloor() + " 层）"
                    + " | 入住人 " + (order.getContactName() == null ? "-" : order.getContactName())
                    + " " + (order.getContactPhone() == null ? "" : order.getContactPhone())
                    + " | 宠物 " + (pet == null ? "-" : pet.getName())
                    + " | 寄养时段 " + RoomService.rangeText(order));
            log.setCreateTime(LocalDateTime.now());
            operationLogMapper.insert(log);
        } catch (Exception ignored) {
            // 日志失败不影响主流程
        }
    }

    /** 办理入住：CONFIRMED → CHECKED_IN →（入住检查通过）→ BOARDING 由员工端触发 */
    @Transactional
    public void checkIn(Long orderId) {
        BoardingOrder order = requireStaffOrder(orderId);
        mustStatus(order, "CONFIRMED");
        if (order.getRoomId() == null) throw new BusinessException("该订单还没有分配房间，请先选房分配");
        // 未到入住日期不能提前办理入住：避免"未来日期的订单"被走完流程变成已完成
        if (LocalDate.now().isBefore(order.getStartDate())) {
            throw new BusinessException("未到入住日期（" + order.getStartDate() + "），不能提前办理入住");
        }
        // 房间的"入住中/已预约"由订单时段派生，入住时无需改动 room.status（只改物理状态）
        order.setStatus("CHECKED_IN");
        order.setCheckinTime(LocalDateTime.now());
        orderMapper.updateById(order);
        notificationService.send(order.getUserId(), "毛孩子已入住",
                "您的宠物已办理入住，工作人员将每日更新照护动态。", "DAILY", "ORDER", order.getId());
    }

    @Transactional
    public void startBoarding(Long orderId) {
        BoardingOrder order = requireStaffOrder(orderId);
        mustStatus(order, "CHECKED_IN");
        order.setStatus("BOARDING");
        orderMapper.updateById(order);
    }

    /** 办理离店：BOARDING → COMPLETED，回写宠物体重，房间转清洁中 */
    @Transactional
    public void checkOut(Long orderId, BigDecimal weightOut) {
        BoardingOrder order = requireStaffOrder(orderId);
        mustStatus(order, "BOARDING");
        order.setStatus("COMPLETED");
        order.setCheckoutTime(LocalDateTime.now());
        orderMapper.updateById(order);

        Pet pet = petMapper.selectById(order.getPetId());
        if (pet != null && weightOut != null) {
            pet.setWeight(weightOut);
            petMapper.updateById(pet);
        }
        if (order.getRoomId() != null) {
            roomMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Room>()
                    .eq(Room::getId, order.getRoomId())
                    .set(Room::getStatus, "CLEANING")
                    .set(Room::getCurrentOrderId, null));
        }
        notificationService.send(order.getUserId(), "寄养结束",
                "毛孩子已安全回家！对本次服务满意的话，记得评价哦～", "REMINDER", "ORDER", order.getId());
    }

    @Transactional
    public void refundRequest(Long orderId) {
        BoardingOrder order = getMineAsOwner(orderId);
        if (!"PAID".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new BusinessException("当前状态不支持退款申请");
        }
        order.setStatus("REFUNDING");
        orderMapper.updateById(order);
        paymentMapper.insert(snapshot(order, "REFUNDING"));
        notificationService.sendToRole("ROLE_ADMIN", "退款申请",
                "订单 " + order.getOrderNo() + " 申请退款，请及时处理。", "REMINDER", "ORDER", order.getId());
    }

    @Transactional
    public void refundApprove(Long orderId, boolean approve) {
        SecurityUtils.requireRole("ROLE_ADMIN");
        BoardingOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        mustStatus(order, "REFUNDING");
        if (approve) {
            // 退款通过：仅解除房间绑定，房间"已预约/入住中"状态由订单派生，会自动消失
            order.setStatus("REFUNDED");
            if (order.getRoomId() != null) {
                roomMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Room>()
                        .eq(Room::getId, order.getRoomId())
                        .eq(Room::getCurrentOrderId, order.getId())
                        .set(Room::getCurrentOrderId, null));
            }
        } else {
            // 驳回退款：恢复到申请前状态（已分房为 CONFIRMED，未分房为 PAID），订单继续履行
            order.setStatus(order.getRoomId() != null ? "CONFIRMED" : "PAID");
        }
        orderMapper.updateById(order);
        paymentMapper.insert(snapshot(order, approve ? "REFUNDED" : "FAILED"));
        notificationService.send(order.getUserId(), approve ? "退款成功" : "退款申请被驳回",
                "订单 " + order.getOrderNo() + (approve
                        ? " 退款已原路退回至您的支付账户（模拟渠道），请注意查收。"
                        : " 退款申请未通过，请联系门店。"), "REMINDER", "ORDER", order.getId());
    }

    /**
     * 升级房型（主人操作）：PENDING_PAYMENT / PAID / CONFIRMED（未入住）可升级到更高价房型。
     * 自动计算补差价并生成一笔 SUCCESS 模拟支付流水（演示环境；可接真实支付）。
     */
    @Transactional
    public Map<String, Object> upgradeRoom(Long orderId, Long newRoomTypeId) {
        BoardingOrder order = getMineAsOwner(orderId);
        if (!"PENDING_PAYMENT".equals(order.getStatus())
                && !"PAID".equals(order.getStatus())
                && !"CONFIRMED".equals(order.getStatus())) {
            throw new BusinessException("当前状态不支持升级房型（已入住/已完成订单不可升级）");
        }
        RoomType newType = roomTypeMapper.selectById(newRoomTypeId);
        if (newType == null || newType.getStatus() != 1) throw new BusinessException("目标房型不存在或已下架");
        // 升级只允许在同分区内进行（猫区订单不能升成犬区房型）
        requireSpeciesMatch(newType, petMapper.selectById(order.getPetId()));
        RoomType oldType = roomTypeMapper.selectById(order.getRoomTypeId());
        if (oldType != null && oldType.getPrice().compareTo(newType.getPrice()) >= 0) {
            throw new BusinessException("升级需选择更高价房型（当前 " + oldType.getName() + " ￥" + oldType.getPrice() + "/天）");
        }
        // 目标房型在订单日期区间内必须可订
        long days = order.getDays();
        boolean hasStock = availability(order.getStartDate(), order.getEndDate()).stream()
                .anyMatch(item -> ((RoomType) item.get("roomType")).getId().equals(newType.getId())
                        && Boolean.TRUE.equals(item.get("bookable")));
        if (!hasStock) throw new BusinessException("目标房型在所选日期已订满，无法升级");

        // 新住宿费与补差价
        BigDecimal newRoomAmount = newType.getPrice().multiply(BigDecimal.valueOf(days));
        BigDecimal oldRoomAmount = order.getRoomAmount() == null ? BigDecimal.ZERO : order.getRoomAmount();
        BigDecimal diff = newRoomAmount.subtract(oldRoomAmount);
        if (diff.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("无需补差价（房型价格未提升）");

        // 已确认订单原已占房：解除旧房间绑定；新房间由员工/管理员在房间地图手动选择分配
        if (order.getRoomId() != null) {
            roomMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Room>()
                    .eq(Room::getId, order.getRoomId())
                    .set(Room::getCurrentOrderId, null));
            careTaskMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.petcare.care.entity.CareTask>()
                    .eq(com.petcare.care.entity.CareTask::getOrderId, order.getId())
                    .set(com.petcare.care.entity.CareTask::getRoomId, null));
            order.setRoomId(null);
        }

        order.setRoomTypeId(newType.getId());
        order.setRoomAmount(newRoomAmount);
        order.setTotalAmount(order.getTotalAmount().add(diff));
        orderMapper.updateById(order);

        // 补差价流水（演示：直接 SUCCESS；真实环境应生成待支付单并引导支付）
        Payment p = new Payment();
        p.setPaymentNo("PAY" + bizNo());
        p.setOrderId(order.getId());
        p.setUserId(order.getUserId());
        p.setAmount(diff);
        p.setStatus("SUCCESS");
        p.setChannel("SIMULATED");
        p.setPayTime(LocalDateTime.now());
        paymentMapper.insert(p);

        notificationService.send(order.getUserId(), "房型升级成功",
                "订单 " + order.getOrderNo() + " 已升级为 " + newType.getName() + "，补差价 ￥" + diff + "（模拟渠道已支付）。",
                "REMINDER", "ORDER", order.getId());
        notificationService.sendToRole("ROLE_ADMIN", "订单升级房型",
                "订单 " + order.getOrderNo() + " 已由主人升级为 " + newType.getName() + "，请留意房间安排。",
                "REMINDER", "ORDER", order.getId());

        Map<String, Object> res = new java.util.LinkedHashMap<>();
        res.put("orderId", order.getId());
        res.put("roomTypeId", newType.getId());
        res.put("roomTypeName", newType.getName());
        res.put("diffAmount", diff);
        res.put("totalAmount", order.getTotalAmount());
        return res;
    }

    // ---------- 查询 ----------

    /** 角色自适应分页：主人看自己的，员工/管理员看全部 */
    public Page<BoardingOrder> page(long page, long size, String status) {
        LoginUser lu = SecurityUtils.current();
        LambdaQueryWrapper<BoardingOrder> w = new LambdaQueryWrapper<>();
        if ("ROLE_USER".equals(lu.getRole())) w.eq(BoardingOrder::getUserId, lu.getId());
        if (status != null && !status.isBlank() && !"ALL".equals(status)) w.eq(BoardingOrder::getStatus, status);
        w.orderByDesc(BoardingOrder::getId);
        return orderMapper.selectPage(new Page<>(page, size), w);
    }

    public OrderDetailVO detail(Long orderId) {
        BoardingOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        LoginUser lu = SecurityUtils.current();
        if ("ROLE_USER".equals(lu.getRole()) && !order.getUserId().equals(lu.getId())) {
            throw new BusinessException(403, "无权查看该订单");
        }
        OrderDetailVO vo = new OrderDetailVO();
        vo.setOrder(order);
        vo.setPet(petMapper.selectById(order.getPetId()));
        vo.setRoomType(roomTypeMapper.selectById(order.getRoomTypeId()));
        vo.setRoom(order.getRoomId() == null ? null : roomMapper.selectById(order.getRoomId()));
        vo.setServices(orderServiceMapper.selectList(new LambdaQueryWrapper<BoardingOrderServiceItem>()
                .eq(BoardingOrderServiceItem::getOrderId, orderId)));
        vo.setPayments(paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderId, orderId).orderByDesc(Payment::getId)));
        vo.setReview(reviewMapper.selectOne(new LambdaQueryWrapper<com.petcare.review.entity.Review>()
                .eq(com.petcare.review.entity.Review::getOrderId, orderId)));
        return vo;
    }

    // ---------- 内部 ----------

    private BoardingOrder getMineAsOwner(Long orderId) {
        BoardingOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(SecurityUtils.currentId())) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private BoardingOrder requireStaffOrder(Long orderId) {
        BoardingOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        requireStaffOrAdmin();
        return order;
    }

    /** 校验当前用户为 STAFF 或 ADMIN（与 Controller 层 @PreAuthorize 口径一致） */
    private void requireStaffOrAdmin() {
        String role = SecurityUtils.currentRole();
        if (!"ROLE_STAFF".equals(role) && !"ROLE_ADMIN".equals(role)) {
            throw new BusinessException(403, "仅工作人员可执行此操作");
        }
    }

    /** 业务单号：毫秒时间戳 + 6 位随机后缀，降低并发碰撞概率（唯一索引兜底） */
    private String bizNo() {
        return System.currentTimeMillis() + String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
    }

    /** 校验房型分区与宠物物种一致（猫宠→猫区、狗宠→犬区） */
    private void requireSpeciesMatch(RoomType type, Pet pet) {
        String typeSpecies = type.getSpecies() == null || type.getSpecies().isBlank() ? "DOG" : type.getSpecies();
        String petSpecies = pet == null || pet.getSpecies() == null ? "DOG" : pet.getSpecies();
        if (!typeSpecies.equals(petSpecies)) {
            throw new BusinessException("该宠物为" + ("CAT".equals(petSpecies) ? "猫咪" : "狗狗")
                    + "，只能预订" + ("CAT".equals(typeSpecies) ? "猫区" : "犬区") + "房型");
        }
    }

    private void mustStatus(BoardingOrder order, String expected) {
        if (!expected.equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不支持此操作（当前：" + order.getStatus() + "）");
        }
    }

    private Payment snapshot(BoardingOrder order, String status) {
        Payment p = new Payment();
        p.setPaymentNo("PAY" + bizNo());
        p.setOrderId(order.getId());
        p.setUserId(order.getUserId());
        p.setAmount(order.getTotalAmount());
        p.setStatus(status);
        p.setChannel("SIMULATED");
        if ("SUCCESS".equals(status)) p.setPayTime(LocalDateTime.now());
        return p;
    }
}
