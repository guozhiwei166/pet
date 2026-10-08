package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.BizException;
import com.tourism.common.Constants;
import com.tourism.common.PageVO;
import com.tourism.common.ResultCode;
import com.tourism.dto.OrderCreateDTO;
import com.tourism.dto.OrderPreviewDTO;
import com.tourism.entity.Homestay;
import com.tourism.entity.Merchant;
import com.tourism.entity.Orders;
import com.tourism.entity.RoomInventory;
import com.tourism.entity.RoomType;
import com.tourism.entity.User;
import com.tourism.mapper.HomestayMapper;
import com.tourism.mapper.OrdersMapper;
import com.tourism.mapper.RoomInventoryMapper;
import com.tourism.mapper.RoomTypeMapper;
import com.tourism.mapper.UserMapper;
import com.tourism.service.OrderService;
import com.tourism.util.StatusTextUtil;
import com.tourism.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 订单服务实现
 * 防超卖:创建订单事务内按 房型+日期 逐日 SELECT ... FOR UPDATE 行锁,
 * 并校验扣减库存;取消/拒单回补。
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrdersMapper ordersMapper;
    private final HomestayMapper homestayMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final RoomInventoryMapper roomInventoryMapper;
    private final UserMapper userMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== 试算与下单 ====================

    @Override
    public OrderVO preview(OrderPreviewDTO dto) {
        // 仅计算价格并校验合法性,不落库(联系人等下单信息不参与试算)
        OrderCreateDTO full = new OrderCreateDTO();
        full.setHomestayId(dto.getHomestayId());
        full.setRoomTypeId(dto.getRoomTypeId());
        full.setCheckInDate(dto.getCheckInDate());
        full.setCheckOutDate(dto.getCheckOutDate());
        full.setRoomCount(dto.getRoomCount());
        Calc calc = calc(full);
        OrderVO vo = new OrderVO();
        vo.setHomestayId(calc.homestay().getId());
        vo.setHomestayName(calc.homestay().getName());
        vo.setHomestayCover(calc.homestay().getCoverImage());
        vo.setRoomTypeId(calc.roomType().getId());
        vo.setRoomTypeName(calc.roomType().getName());
        vo.setCheckInDate(calc.checkIn());
        vo.setCheckOutDate(calc.checkOut());
        vo.setNights(calc.nights());
        vo.setRoomCount(calc.roomCount());
        vo.setUnitPrice(calc.roomType().getPrice());
        vo.setTotalAmount(calc.total());
        vo.setDiscountAmount(BigDecimal.ZERO);
        vo.setPayAmount(calc.total());
        return vo;
    }

    @Override
    @Transactional
    public OrderVO create(OrderCreateDTO dto, Long userId) {
        Calc calc = calc(dto);
        Homestay homestay = calc.homestay();
        RoomType roomType = calc.roomType();
        LocalDate checkIn = calc.checkIn();
        LocalDate checkOut = calc.checkOut();
        int nights = calc.nights();
        int roomCount = calc.roomCount();

        // 防超卖:逐晚行锁 + 校验可售,再扣减
        for (int i = 0; i < nights; i++) {
            LocalDate date = checkIn.plusDays(i);
            RoomInventory inv = roomInventoryMapper.selectForUpdate(roomType.getId(), date);
            if (inv == null) {
                throw new BizException("所选日期暂无可售房间");
            }
            if (inv.getAvailableCount() < roomCount) {
                throw new BizException("所选日期房间库存不足,请调整日期或房型");
            }
        }
        // 校验通过后统一扣减(再次用原子 SQL 保证并发安全)
        for (int i = 0; i < nights; i++) {
            LocalDate date = checkIn.plusDays(i);
            int rows = roomInventoryMapper.decrement(roomType.getId(), date, roomCount);
            if (rows == 0) {
                throw new BizException("库存已变化,请重新下单");
            }
        }
        // 同步房型冗余库存 stock(原子条件扣减,与日期库存保持同向,供前端房态展示)
        int stockRows = roomTypeMapper.decrementStock(roomType.getId(), roomCount);
        if (stockRows == 0) {
            throw new BizException("库存已变化,请重新下单");
        }

        Orders order = new Orders();
        order.setOrderNo(genOrderNo());
        order.setUserId(userId);
        order.setHomestayId(homestay.getId());
        order.setHomestayName(homestay.getName());
        order.setHomestayCover(homestay.getCoverImage());
        order.setRoomTypeId(roomType.getId());
        order.setRoomTypeName(roomType.getName());
        order.setCheckInDate(checkIn);
        order.setCheckOutDate(checkOut);
        order.setNights(nights);
        order.setRoomCount(roomCount);
        order.setUnitPrice(roomType.getPrice());
        order.setTotalAmount(calc.total());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(calc.total());
        order.setContactName(dto.getContactName());
        order.setContactPhone(dto.getContactPhone());
        order.setGuestNames(dto.getGuestNames());
        order.setRemark(dto.getRemark());
        order.setStatus(Constants.ORDER_PENDING);
        order.setPayStatus(Constants.PAY_UNPAID);
        ordersMapper.insert(order);
        return toVO(order);
    }

    // ==================== 支付 ====================

    @Override
    @Transactional
    public OrderVO pay(Long orderId, Long userId, String payType) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!Objects.equals(order.getStatus(), Constants.ORDER_PENDING)) {
            throw new BizException("订单状态不允许支付(可能已取消或已支付)");
        }
        // 超时未支付订单不可再支付(15 分钟内有效),需重新下单
        if (order.getCreateTime() != null
                && order.getCreateTime().isBefore(LocalDateTime.now().minusMinutes(Constants.ORDER_EXPIRE_MINUTES))) {
            throw new BizException("订单已超时未支付,已自动失效,请重新下单");
        }
        // 幂等:仅当仍为 PENDING 时更新为 PAID,并发重复支付只有一个成功
        Orders patch = new Orders();
        patch.setId(order.getId());
        patch.setStatus(Constants.ORDER_PAID);
        patch.setPayStatus(Constants.PAY_PAID);
        patch.setPayType(payType == null ? Constants.PAY_TYPE_WECHAT : payType);
        patch.setPayTime(LocalDateTime.now());
        int rows = ordersMapper.update(patch, new LambdaQueryWrapper<Orders>()
                .eq(Orders::getId, order.getId())
                .eq(Orders::getStatus, Constants.ORDER_PENDING));
        if (rows == 0) {
            throw new BizException("订单状态已变化,请刷新后重试");
        }
        return toVO(ordersMapper.selectById(order.getId()));
    }

    // ==================== 取消与恢复库存 ====================

    @Override
    @Transactional
    public OrderVO cancelByUser(Long orderId, Long userId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        // 待支付 / 已支付(待入住) / 已到店未接待 均可取消;商家已接待(入住中)或已完成则不可取消
        boolean pending = Objects.equals(order.getStatus(), Constants.ORDER_PENDING);
        boolean paid = Objects.equals(order.getStatus(), Constants.ORDER_PAID);
        boolean arrived = Objects.equals(order.getStatus(), Constants.ORDER_ARRIVED);
        if (!pending && !paid && !arrived) {
            throw new BizException("订单已接待入住或已完成,无法取消");
        }
        restoreStock(order);
        order.setStatus(Constants.ORDER_CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        if (paid || arrived) {
            // 已支付订单取消后标记退款(模拟退款,原路退回)
            order.setPayStatus(Constants.PAY_REFUNDED);
        }
        ordersMapper.updateById(order);
        return toVO(order);
    }

    @Override
    @Transactional
    public OrderVO arriveByUser(Long orderId, Long userId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!Objects.equals(order.getStatus(), Constants.ORDER_PAID)) {
            throw new BizException("仅待入住(已支付)订单可确认到达");
        }
        // 已到店:只能从入住日期当天起(防止提前多天误操作)
        if (order.getCheckInDate() == null || order.getCheckInDate().isAfter(LocalDate.now())) {
            throw new BizException("入住日期当天方可确认到达,请入住当日再操作");
        }
        order.setStatus(Constants.ORDER_ARRIVED);
        ordersMapper.updateById(order);
        return toVO(order);
    }

    @Override
    @Transactional
    public OrderVO earlyCheckoutByUser(Long orderId, Long userId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!Objects.equals(order.getStatus(), Constants.ORDER_CHECKED_IN)) {
            throw new BizException("仅入住中的订单可办理提前退房");
        }
        // 立即退房:提前结束入住,订单置为已完成;已入住不退款、不回补库存
        order.setStatus(Constants.ORDER_COMPLETED);
        order.setCheckOutTime(LocalDateTime.now());
        order.setCompleteTime(LocalDateTime.now());
        ordersMapper.updateById(order);
        return toVO(order);
    }

    @Override
    @Transactional
    public OrderVO cancelByMerchant(Long orderId, Long merchantId, String remark) {
        Orders order = requireMerchantOrder(orderId, merchantId);
        // 已支付(待入住,客人尚未到店)商家可取消/拒单;到店/入住后不可取消
        if (!Objects.equals(order.getStatus(), Constants.ORDER_PAID)) {
            throw new BizException("仅客人未到店的待入住订单可取消");
        }
        restoreStock(order);
        order.setStatus(Constants.ORDER_CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setRemark((order.getRemark() == null ? "" : order.getRemark() + " ") + "商家取消:" + (remark == null ? "" : remark));
        ordersMapper.updateById(order);
        return toVO(order);
    }

    // ==================== 履约 ====================

    @Override
    @Transactional
    public OrderVO checkIn(Long orderId, Long merchantId) {
        Orders order = requireMerchantOrder(orderId, merchantId);
        // 只有客人已确认到达(已到店)后,商家才能接待办理入住
        if (!Objects.equals(order.getStatus(), Constants.ORDER_ARRIVED)) {
            throw new BizException("客人尚未确认到店,暂不能接待入住");
        }
        order.setStatus(Constants.ORDER_CHECKED_IN);
        order.setCheckInTime(LocalDateTime.now());
        ordersMapper.updateById(order);
        return toVO(order);
    }

    @Override
    @Transactional
    public OrderVO checkOut(Long orderId, Long merchantId) {
        Orders order = requireMerchantOrder(orderId, merchantId);
        if (!Objects.equals(order.getStatus(), Constants.ORDER_CHECKED_IN)) {
            throw new BizException("仅入住中订单可办理退房");
        }
        order.setStatus(Constants.ORDER_COMPLETED);
        order.setCompleteTime(LocalDateTime.now());
        order.setCheckOutTime(LocalDateTime.now());
        ordersMapper.updateById(order);
        return toVO(order);
    }

    // ==================== 查询 ====================

    @Override
    public PageVO<OrderVO> pageByUser(Long userId, int current, int size, String status) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId);
        if (status != null && !status.isBlank()) {
            wrapper.eq(Orders::getStatus, status);
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        return toPageVO(ordersMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @Override
    public PageVO<OrderVO> pageByMerchant(Long merchantId, int current, int size, String status, Long homestayId) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        // 商家只能看到自己民宿的订单
        List<Homestay> mine = homestayMapper.selectList(new LambdaQueryWrapper<Homestay>()
                .eq(Homestay::getMerchantId, merchantId));
        List<Long> ids = mine.stream().map(Homestay::getId).toList();
        if (ids.isEmpty()) {
            return new PageVO<>();
        }
        wrapper.in(Orders::getHomestayId, ids);
        if (homestayId != null) {
            wrapper.eq(Orders::getHomestayId, homestayId);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(Orders::getStatus, status);
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        return toPageVO(ordersMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @Override
    public OrderVO detail(Long orderId, Long userId, String role, Long merchantId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        // 权限:用户本人 / 民宿所属商家 / 管理员
        boolean admin = Constants.ROLE_ADMIN.equals(role);
        boolean ownerUser = Objects.equals(order.getUserId(), userId);
        Homestay h = homestayMapper.selectById(order.getHomestayId());
        boolean ownerMerchant = h != null && Objects.equals(h.getMerchantId(), merchantId);
        if (!admin && !ownerUser && !ownerMerchant) {
            throw new BizException(ResultCode.FORBIDDEN, "无权查看该订单");
        }
        return toVO(order);
    }

    @Override
    public PageVO<OrderVO> pageForAdmin(int current, int size, String status, String orderNo, String keyword) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(Orders::getStatus, status);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.eq(Orders::getOrderNo, orderNo.trim());
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Orders::getHomestayName, kw)
                    .or().like(Orders::getContactName, kw)
                    .or().like(Orders::getContactPhone, kw)
                    .or().like(Orders::getOrderNo, kw));
        }
        wrapper.orderByDesc(Orders::getCreateTime);
        Page<Orders> page = ordersMapper.selectPage(new Page<>(current, size), wrapper);
        return toPageVO(page);
    }

    @Override
    public Orders getById(Long id) {
        return ordersMapper.selectById(id);
    }

    @Override
    @Transactional
    public int autoCancelExpired() {
        LocalDateTime expireBefore = LocalDateTime.now().minusMinutes(Constants.ORDER_EXPIRE_MINUTES);
        List<Orders> expired = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, Constants.ORDER_PENDING)
                .lt(Orders::getCreateTime, expireBefore)
                .last("limit 200"));
        int count = 0;
        for (Orders o : expired) {
            restoreStock(o);
            o.setStatus(Constants.ORDER_CANCELLED);
            o.setCancelTime(LocalDateTime.now());
            ordersMapper.updateById(o);
            count++;
        }
        return count;
    }

    @Override
    public boolean canReview(Long userId, Long homestayId) {
        Long cnt = ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .eq(Orders::getHomestayId, homestayId)
                .eq(Orders::getStatus, Constants.ORDER_COMPLETED));
        return cnt > 0;
    }

    // ==================== 内部工具 ====================

    private record Calc(Homestay homestay, RoomType roomType, LocalDate checkIn,
                        LocalDate checkOut, int nights, int roomCount, BigDecimal total) {
    }

    private Calc calc(OrderCreateDTO dto) {
        Homestay homestay = homestayMapper.selectById(dto.getHomestayId());
        if (homestay == null || !Constants.AUDIT_APPROVED.equals(homestay.getAuditStatus())
                || !Constants.BIZ_OPEN.equals(homestay.getBusinessStatus())) {
            throw new BizException("民宿不存在或未上架");
        }
        RoomType roomType = roomTypeMapper.selectById(dto.getRoomTypeId());
        if (roomType == null || !Objects.equals(roomType.getHomestayId(), homestay.getId())) {
            throw new BizException("房型不存在或不属于该民宿");
        }
        LocalDate checkIn;
        LocalDate checkOut;
        try {
            checkIn = LocalDate.parse(dto.getCheckInDate(), FMT);
            checkOut = LocalDate.parse(dto.getCheckOutDate(), FMT);
        } catch (DateTimeParseException e) {
            throw new BizException("日期格式不正确");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new BizException("退房日期必须晚于入住日期");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BizException("入住日期不能早于今天");
        }
        int nights = (int) Duration.between(checkIn.atStartOfDay(), checkOut.atStartOfDay()).toDays();
        int roomCount = dto.getRoomCount() == null ? 1 : dto.getRoomCount();
        BigDecimal total = roomType.getPrice().multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(roomCount));
        return new Calc(homestay, roomType, checkIn, checkOut, nights, roomCount, total);
    }

    /** 回补库存(取消/拒单时) */
    private void restoreStock(Orders order) {
        if (order.getCheckInDate() == null || order.getCheckOutDate() == null) {
            return;
        }
        // 仅回补仍属于未来入住时间的晚数;已过去的晚数不回补
        LocalDate today = LocalDate.now();
        long nights = java.time.temporal.ChronoUnit.DAYS.between(order.getCheckInDate(), order.getCheckOutDate());
        for (int i = 0; i < nights; i++) {
            LocalDate date = order.getCheckInDate().plusDays(i);
            if (!date.isBefore(today)) {
                roomInventoryMapper.increment(order.getRoomTypeId(), date, order.getRoomCount());
            }
        }
        // 同步房型冗余库存(与日期库存同口径:仅整单仍在未来时才回补)
        RoomType rt = roomTypeMapper.selectById(order.getRoomTypeId());
        if (rt != null && !order.getCheckInDate().isBefore(today)) {
            roomTypeMapper.incrementStock(order.getRoomTypeId(), order.getRoomCount());
        }
    }

    private Orders requireMerchantOrder(Long orderId, Long merchantId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        Homestay h = homestayMapper.selectById(order.getHomestayId());
        if (h == null || !Objects.equals(h.getMerchantId(), merchantId)) {
            throw new BizException(ResultCode.FORBIDDEN, "无权操作该订单");
        }
        return order;
    }

    private String genOrderNo() {
        // 时间戳 + 12 位随机,降低并发重复概率;order_no 唯一索引仍为最终兜底
        return "HS" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private PageVO<OrderVO> toPageVO(Page<Orders> page) {
        PageVO<OrderVO> vo = new PageVO<>();
        vo.setRecords(page.getRecords().stream().map(this::toVO).toList());
        vo.setTotal(page.getTotal());
        vo.setCurrent(page.getCurrent());
        vo.setSize(page.getSize());
        vo.setPages(page.getPages());
        return vo;
    }

    private OrderVO toVO(Orders order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setStatusText(StatusTextUtil.order(order.getStatus()));
        // 商家/管理员查看客户信息:补下单用户昵称与头像
        User buyer = userMapper.selectById(order.getUserId());
        if (buyer != null) {
            vo.setUserNickname(buyer.getNickname());
            vo.setUserAvatar(buyer.getAvatar());
        }
        return vo;
    }
}
