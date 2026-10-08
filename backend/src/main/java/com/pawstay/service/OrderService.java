package com.pawstay.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pawstay.common.BusinessException;
import com.pawstay.common.OrderStatus;
import com.pawstay.dto.OrderCreateDTO;
import com.pawstay.entity.BoardingOrder;
import com.pawstay.entity.Merchant;
import com.pawstay.entity.Pet;
import com.pawstay.entity.Room;
import com.pawstay.mapper.MerchantMapper;
import com.pawstay.mapper.OrderMapper;
import com.pawstay.mapper.PetMapper;
import com.pawstay.mapper.RoomMapper;
import com.pawstay.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private PetMapper petMapper;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private MessageService messageService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private com.pawstay.mapper.CareRecordMapper careRecordMapper;
    @Autowired
    private com.pawstay.mapper.ReviewMapper reviewMapper;

    public BoardingOrder create(OrderCreateDTO dto) {
        Long uid = UserContext.requireLogin().getId();
        Pet pet = petMapper.selectById(dto.petId());
        if (pet == null || !pet.getUserId().equals(uid)) throw new BusinessException("宠物不存在");
        Room room = roomMapper.selectById(dto.roomId());
        if (room == null || room.getStatus() == null || room.getStatus() != 1) throw new BusinessException("该房型已下架");
        if (dto.endDate() == null || dto.startDate() == null || !dto.endDate().isAfter(dto.startDate())) {
            throw new BusinessException("离店日期需晚于入住日期");
        }
        if (!Boolean.TRUE.equals(dto.agreementSigned())) {
            throw new BusinessException("请阅读并同意《宠物寄养服务协议》");
        }
        int overlap = orderMapper.countOverlap(room.getId(), dto.startDate(), dto.endDate());
        if (overlap >= room.getTotalCount()) throw new BusinessException("所选日期该房型已订满，换个房型或日期试试");

        long days = ChronoUnit.DAYS.between(dto.startDate(), dto.endDate());
        Merchant merchant = merchantMapper.selectById(room.getMerchantId());

        BoardingOrder o = new BoardingOrder();
        o.setOrderNo("PS" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(10, 100));
        o.setUserId(uid);
        o.setPetId(pet.getId());
        o.setMerchantId(merchant.getId());
        o.setRoomId(room.getId());
        o.setStartDate(dto.startDate());
        o.setEndDate(dto.endDate());
        o.setDays((int) days);
        o.setPricePerDay(room.getPricePerDay());
        o.setTotalAmount(room.getPricePerDay().multiply(BigDecimal.valueOf(days)));
        o.setStatus(OrderStatus.PENDING_CONFIRM);
        o.setContactName(dto.contactName());
        o.setContactPhone(dto.contactPhone());
        o.setRemark(dto.remark());
        o.setAgreementSigned(1);
        o.setCreateTime(LocalDateTime.now());
        o.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(o);

        messageService.send(uid, "下单成功", "您在「" + merchant.getShopName() + "」的寄养订单已提交，支付后等待商家确认。", "ORDER", o.getId());
        messageService.send(merchant.getUserId(), "收到新的寄养订单", "「" + pet.getName() + "」的主人已下单寄养，请及时确认。", "ORDER", o.getId());
        return o;
    }

    /** 模拟支付（答辩演示用；接入支付宝沙箱后替换此方法实现） */
    public void pay(Long id) {
        BoardingOrder o = getMineAsOwner(id);
        mustStatus(o, OrderStatus.PENDING_CONFIRM);
        o.setPayTime(LocalDateTime.now());
        o.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(o);
        messageService.send(o.getUserId(), "支付成功", "订单 " + o.getOrderNo() + " 已支付，等待商家确认接单。", "ORDER", o.getId());
    }

    public void cancel(Long id) {
        BoardingOrder o = getMineAsOwner(id);
        mustStatus(o, OrderStatus.PENDING_CONFIRM);
        o.setStatus(OrderStatus.CANCELLED);
        o.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(o);
        Merchant m = merchantMapper.selectById(o.getMerchantId());
        if (m != null) {
            messageService.send(m.getUserId(), "订单已取消",
                    "订单 " + o.getOrderNo() + " 已被主人取消" + (o.getPayTime() != null ? "，款项将原路退回。" : "。"), "ORDER", o.getId());
        }
    }

    public void confirm(Long id) {
        BoardingOrder o = getMineAsMerchant(id);
        mustStatus(o, OrderStatus.PENDING_CONFIRM);
        o.setStatus(OrderStatus.CONFIRMED);
        o.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(o);
        Merchant m = merchantMapper.selectById(o.getMerchantId());
        messageService.send(o.getUserId(), "商家已接单",
                "「" + (m == null ? "商家" : m.getShopName()) + "」已确认您的寄养订单，记得 " + o.getStartDate() + " 按时入住哦。", "ORDER", o.getId());
    }

    public void reject(Long id, String reason) {
        BoardingOrder o = getMineAsMerchant(id);
        mustStatus(o, OrderStatus.PENDING_CONFIRM);
        o.setStatus(OrderStatus.REJECTED);
        o.setRejectReason(reason == null || reason.isBlank() ? "商家无法接待" : reason);
        o.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(o);
        messageService.send(o.getUserId(), "订单被拒单",
                "很抱歉，商家拒绝了订单 " + o.getOrderNo() + "：" + o.getRejectReason() + "。已支付款项将原路退回。", "ORDER", o.getId());
    }

    public void checkin(Long id, java.math.BigDecimal weightIn) {
        BoardingOrder o = getMineAsMerchant(id);
        mustStatus(o, OrderStatus.CONFIRMED);
        o.setStatus(OrderStatus.BOARDING);
        o.setCheckinTime(LocalDateTime.now());
        o.setWeightIn(weightIn);
        o.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(o);
        messageService.send(o.getUserId(), "寄养开始", "您的毛孩子已办理入住，商家将每日更新寄养日记，记得回来看它～", "ORDER", o.getId());
    }

    public void checkout(Long id, java.math.BigDecimal weightOut) {
        BoardingOrder o = getMineAsMerchant(id);
        mustStatus(o, OrderStatus.BOARDING);
        o.setStatus(OrderStatus.COMPLETED);
        o.setCheckoutTime(LocalDateTime.now());
        o.setWeightOut(weightOut);
        o.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(o);
        Merchant m = merchantMapper.selectById(o.getMerchantId());
        if (m != null) {
            m.setBoardingCount(m.getBoardingCount() == null ? 1 : m.getBoardingCount() + 1);
            merchantMapper.updateById(m);
        }
        messageService.send(o.getUserId(), "寄养结束", "毛孩子已安全回家！对本次服务满意的话，记得给商家一个好评～", "ORDER", o.getId());
    }

    /** 商家端今日看板：待接单/今日入住/寄养中/待离店 + 最近订单 */
    public Map<String, Object> merchantDashboard() {
        Long mid = merchantService.myShop().getId();
        LocalDate today = LocalDate.now();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("pendingConfirm", orderMapper.selectCount(new LambdaQueryWrapper<BoardingOrder>()
                .eq(BoardingOrder::getMerchantId, mid).eq(BoardingOrder::getStatus, OrderStatus.PENDING_CONFIRM)));
        res.put("todayCheckin", orderMapper.selectCount(new LambdaQueryWrapper<BoardingOrder>()
                .eq(BoardingOrder::getMerchantId, mid).eq(BoardingOrder::getStatus, OrderStatus.CONFIRMED)
                .eq(BoardingOrder::getStartDate, today)));
        res.put("boarding", orderMapper.selectCount(new LambdaQueryWrapper<BoardingOrder>()
                .eq(BoardingOrder::getMerchantId, mid).eq(BoardingOrder::getStatus, OrderStatus.BOARDING)));
        res.put("todayCheckout", orderMapper.selectCount(new LambdaQueryWrapper<BoardingOrder>()
                .eq(BoardingOrder::getMerchantId, mid).eq(BoardingOrder::getStatus, OrderStatus.BOARDING)
                .le(BoardingOrder::getEndDate, today)));
        res.put("recentOrders", page(1, 10, "ALL").getRecords());
        return res;
    }

    /** 角色自适应分页：主人看自己的单，商家看店铺的单，管理员看全部 */
    public Page<com.pawstay.vo.OrderVO> page(long page, long size, String status) {
        var lu = UserContext.requireLogin();
        LambdaQueryWrapper<BoardingOrder> w = new LambdaQueryWrapper<>();
        if ("OWNER".equals(lu.getRole())) {
            w.eq(BoardingOrder::getUserId, lu.getId());
        } else if ("MERCHANT".equals(lu.getRole())) {
            w.eq(BoardingOrder::getMerchantId, merchantService.myShop().getId());
        }
        if (status != null && !status.isBlank() && !"ALL".equals(status)) w.eq(BoardingOrder::getStatus, status);
        w.orderByDesc(BoardingOrder::getId);

        Page<BoardingOrder> p = orderMapper.selectPage(new Page<>(page, size), w);
        Page<com.pawstay.vo.OrderVO> voPage = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    public com.pawstay.vo.OrderDetailVO detail(Long id) {
        var lu = UserContext.requireLogin();
        BoardingOrder o = orderMapper.selectById(id);
        if (o == null) throw new BusinessException("订单不存在");
        if ("OWNER".equals(lu.getRole()) && !o.getUserId().equals(lu.getId())) throw new BusinessException(403, "无权查看该订单");
        if ("MERCHANT".equals(lu.getRole()) && !o.getMerchantId().equals(merchantService.myShop().getId())) {
            throw new BusinessException(403, "无权查看该订单");
        }

        com.pawstay.vo.OrderDetailVO vo = new com.pawstay.vo.OrderDetailVO();
        vo.setOrder(o);
        vo.setPet(petMapper.selectById(o.getPetId()));
        vo.setMerchant(merchantMapper.selectById(o.getMerchantId()));
        vo.setRoom(roomMapper.selectById(o.getRoomId()));
        vo.setDiaries(orderDiaries(id));
        vo.setReview(reviewOfOrder(id));
        return vo;
    }

    private java.util.List<com.pawstay.entity.CareRecord> orderDiaries(Long orderId) {
        return careRecordMapper.selectList(new LambdaQueryWrapper<com.pawstay.entity.CareRecord>()
                .eq(com.pawstay.entity.CareRecord::getOrderId, orderId)
                .orderByDesc(com.pawstay.entity.CareRecord::getRecordDate));
    }

    private com.pawstay.entity.Review reviewOfOrder(Long orderId) {
        return reviewMapper.selectOne(new LambdaQueryWrapper<com.pawstay.entity.Review>()
                .eq(com.pawstay.entity.Review::getOrderId, orderId));
    }

    private com.pawstay.vo.OrderVO toVO(BoardingOrder o) {
        com.pawstay.vo.OrderVO v = new com.pawstay.vo.OrderVO();
        v.setId(o.getId());
        v.setOrderNo(o.getOrderNo());
        v.setStatus(o.getStatus());
        v.setStartDate(o.getStartDate());
        v.setEndDate(o.getEndDate());
        v.setDays(o.getDays());
        v.setTotalAmount(o.getTotalAmount());
        v.setPricePerDay(o.getPricePerDay());
        v.setContactName(o.getContactName());
        v.setContactPhone(o.getContactPhone());
        v.setPayTime(o.getPayTime());
        v.setCreateTime(o.getCreateTime());
        Pet pet = petMapper.selectById(o.getPetId());
        if (pet != null) {
            v.setPetId(pet.getId());
            v.setPetName(pet.getName());
            v.setPetSpecies(pet.getSpecies());
            v.setPetPhoto(pet.getPhoto());
        }
        Room room = roomMapper.selectById(o.getRoomId());
        if (room != null) {
            v.setRoomId(room.getId());
            v.setRoomName(room.getName());
        }
        Merchant m = merchantMapper.selectById(o.getMerchantId());
        if (m != null) {
            v.setMerchantId(m.getId());
            v.setShopName(m.getShopName());
            v.setMerchantPhone(m.getPhone());
        }
        return v;
    }

    private BoardingOrder getMineAsOwner(Long id) {
        BoardingOrder o = orderMapper.selectById(id);
        if (o == null || !o.getUserId().equals(UserContext.requireLogin().getId())) throw new BusinessException("订单不存在");
        return o;
    }

    private BoardingOrder getMineAsMerchant(Long id) {
        BoardingOrder o = orderMapper.selectById(id);
        if (o == null || !o.getMerchantId().equals(merchantService.myShop().getId())) throw new BusinessException("订单不存在");
        return o;
    }

    private void mustStatus(BoardingOrder o, String expected) {
        if (!expected.equals(o.getStatus())) throw new BusinessException("当前订单状态不支持此操作");
    }
}
