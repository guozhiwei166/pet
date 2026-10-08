package com.petcare.marketing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.petcare.boarding.entity.BoardingOrder;
import com.petcare.boarding.mapper.BoardingOrderMapper;
import com.petcare.common.BusinessException;
import com.petcare.marketing.entity.Announcement;
import com.petcare.marketing.entity.Coupon;
import com.petcare.marketing.entity.UserCoupon;
import com.petcare.marketing.mapper.AnnouncementMapper;
import com.petcare.marketing.mapper.CouponMapper;
import com.petcare.marketing.mapper.UserCouponMapper;
import com.petcare.marketing.vo.MyCouponVO;
import com.petcare.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** 营销：优惠券领取/核销 + 平台公告 */
@Service
@RequiredArgsConstructor
public class MarketingService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final AnnouncementMapper announcementMapper;
    private final BoardingOrderMapper orderMapper;

    // ---------- 优惠券 ----------

    /** 可领券列表（进行中 + 未领完，排除 percent>=100 的无效券） */
    public List<Coupon> availableCoupons() {
        return couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getStatus, 1)
                .and(w -> w.isNull(Coupon::getEndDate).or().ge(Coupon::getEndDate, LocalDate.now())))
                .stream()
                .filter(c -> !("PERCENT".equals(c.getType()) && c.getPercent() != null && c.getPercent() >= 100))
                .toList();
    }

    /** 我的券：未使用/已使用/已过期（过期状态惰性标记），拼装券模板信息并过滤无效券（10折=无优惠，不展示） */
    public List<MyCouponVO> myCoupons() {
        Long uid = SecurityUtils.currentId();
        LocalDate today = LocalDate.now();
        List<UserCoupon> mine = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, uid).orderByDesc(UserCoupon::getId));
        List<MyCouponVO> vos = new java.util.ArrayList<>();
        for (UserCoupon uc : mine) {
            Coupon c = couponMapper.selectById(uc.getCouponId());
            if (c == null) continue;
            // 10 折（percent>=100）属于无优惠的异常券：不展示、不做任何业务
            if ("PERCENT".equals(c.getType()) && c.getPercent() != null && c.getPercent() >= 100) continue;
            // 惰性标过期（仅对未使用券）
            if ("UNUSED".equals(uc.getStatus()) && c.getEndDate() != null && c.getEndDate().isBefore(today)) {
                uc.setStatus("EXPIRED");
                userCouponMapper.updateById(uc);
            }
            MyCouponVO vo = new MyCouponVO();
            vo.setId(uc.getId());
            vo.setCouponId(uc.getCouponId());
            vo.setUserId(uc.getUserId());
            vo.setStatus(uc.getStatus());
            vo.setUsedOrderId(uc.getUsedOrderId());
            vo.setGetTime(uc.getGetTime());
            vo.setUseTime(uc.getUseTime());
            vo.setName(c.getName());
            vo.setType(c.getType());
            vo.setThreshold(c.getThreshold());
            vo.setAmount(c.getAmount());
            vo.setPercent(c.getPercent());
            vo.setStartDate(c.getStartDate());
            vo.setEndDate(c.getEndDate());
            vos.add(vo);
        }
        return vos;
    }

    @Transactional
    public void claim(Long couponId) {
        Long uid = SecurityUtils.currentId();
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() != 1) throw new BusinessException("优惠券不存在或已下线");
        if (coupon.getEndDate() != null && coupon.getEndDate().isBefore(LocalDate.now())) throw new BusinessException("优惠券已过期");
        Long claimed = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, uid).eq(UserCoupon::getCouponId, couponId));
        if (claimed > 0) throw new BusinessException("您已领取过该优惠券");
        if (coupon.getTotalCount() != null && coupon.getIssuedCount() != null
                && coupon.getIssuedCount() >= coupon.getTotalCount()) throw new BusinessException("优惠券已领完");

        UserCoupon uc = new UserCoupon();
        uc.setCouponId(couponId);
        uc.setUserId(uid);
        uc.setStatus("UNUSED");
        userCouponMapper.insert(uc);
        coupon.setIssuedCount((coupon.getIssuedCount() == null ? 0 : coupon.getIssuedCount()) + 1);
        couponMapper.updateById(coupon);
    }

    /**
     * 为指定订单应用最优券：仅限本人未支付订单，金额取订单自身（不接受客户端传入）。
     * 核销券的同时回写订单 discount_amount / total_amount，避免"券扣了、钱没减"。
     */
    @Transactional
    public BigDecimal applyCouponToOrder(Long orderId) {
        BoardingOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(SecurityUtils.currentId())) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("仅待支付订单可使用优惠券");
        }
        if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("该订单已使用过优惠券");
        }
        BigDecimal amount = nz(order.getRoomAmount()).add(nz(order.getServiceAmount()));
        BigDecimal discount = markBestCouponUsed(orderId, amount);
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            if (discount.compareTo(amount) > 0) discount = amount;
            order.setDiscountAmount(discount);
            order.setTotalAmount(amount.subtract(discount));
            orderMapper.updateById(order);
        }
        return discount;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /** 优惠计算：订单金额 × 应用最优券（返回减免额），并占用 */
    private BigDecimal markBestCouponUsed(Long orderId, BigDecimal orderAmount) {
        Long uid = SecurityUtils.currentId();
        List<UserCoupon> unused = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, uid).eq(UserCoupon::getStatus, "UNUSED"));
        BigDecimal best = BigDecimal.ZERO;
        UserCoupon bestUc = null;
        for (UserCoupon uc : unused) {
            Coupon c = couponMapper.selectById(uc.getCouponId());
            if (c == null || c.getStatus() != 1) continue;
            if (c.getEndDate() != null && c.getEndDate().isBefore(LocalDate.now())) continue;
            if ("DISCOUNT".equals(c.getType())) {
                if (orderAmount.compareTo(c.getThreshold() == null ? BigDecimal.ZERO : c.getThreshold()) < 0) continue;
                BigDecimal save = c.getAmount() == null ? BigDecimal.ZERO : c.getAmount();
                if (save.compareTo(best) > 0) { best = save; bestUc = uc; }
            } else if ("PERCENT".equals(c.getType())) {
                BigDecimal save = orderAmount.multiply(BigDecimal.valueOf(100 - (c.getPercent() == null ? 100 : c.getPercent())))
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                if (save.compareTo(best) > 0) { best = save; bestUc = uc; }
            }
        }
        if (bestUc != null) {
            bestUc.setStatus("USED");
            bestUc.setUsedOrderId(orderId);
            bestUc.setUseTime(java.time.LocalDateTime.now());
            userCouponMapper.updateById(bestUc);
        }
        return best;
    }

    // 管理端券模板管理
    public void saveCoupon(Coupon coupon) {
        SecurityUtils.requireRole("ROLE_ADMIN");
        if (coupon.getId() == null) {
            coupon.setIssuedCount(0);
            couponMapper.insert(coupon);
        } else couponMapper.updateById(coupon);
    }

    public void deleteCoupon(Long id) {
        SecurityUtils.requireRole("ROLE_ADMIN");
        couponMapper.deleteById(id);
    }

    public List<Coupon> allCoupons() {
        return couponMapper.selectList(new LambdaQueryWrapper<Coupon>().orderByDesc(Coupon::getId));
    }

    // ---------- 公告 ----------

    public List<Announcement> listAnnouncements(boolean onlyActive) {
        LambdaQueryWrapper<Announcement> w = new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getPinned)
                .orderByDesc(Announcement::getId);
        if (onlyActive) w.eq(Announcement::getStatus, 1);
        return announcementMapper.selectList(w);
    }

    public void saveAnnouncement(Announcement a) {
        SecurityUtils.requireRole("ROLE_ADMIN");
        if (a.getId() == null) announcementMapper.insert(a);
        else announcementMapper.updateById(a);
    }

    public void deleteAnnouncement(Long id) {
        SecurityUtils.requireRole("ROLE_ADMIN");
        announcementMapper.deleteById(id);
    }
}
