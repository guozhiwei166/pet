package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.BizException;
import com.tourism.common.Constants;
import com.tourism.common.PageVO;
import com.tourism.common.ResultCode;
import com.tourism.entity.Homestay;
import com.tourism.entity.Merchant;
import com.tourism.entity.Orders;
import com.tourism.entity.User;
import com.tourism.mapper.HomestayMapper;
import com.tourism.mapper.MerchantMapper;
import com.tourism.mapper.OrdersMapper;
import com.tourism.mapper.UserMapper;
import com.tourism.service.AdminService;
import com.tourism.service.HomestayService;
import com.tourism.util.StatusTextUtil;
import com.tourism.vo.HomestayVO;
import com.tourism.vo.UserAdminVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final HomestayMapper homestayMapper;
    private final OrdersMapper ordersMapper;
    private final HomestayService homestayService;

    // ===== 用户管理 =====

    @Override
    public PageVO<UserAdminVO> pageUsers(int current, int size, String keyword, String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(User::getUsername, kw).or().like(User::getNickname, kw)
                    .or().like(User::getPhone, kw));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> page = userMapper.selectPage(new Page<>(current, size), wrapper);

        PageVO<UserAdminVO> vo = new PageVO<>();
        vo.setRecords(page.getRecords().stream().map(this::toUserAdminVO).toList());
        vo.setTotal(page.getTotal());
        vo.setCurrent(page.getCurrent());
        vo.setSize(page.getSize());
        vo.setPages(page.getPages());
        return vo;
    }

    @Override
    public void changeUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (Constants.ROLE_ADMIN.equals(user.getRole())) {
            throw new BizException("不能禁用管理员账号");
        }
        user.setStatus(status == null ? Constants.USER_STATUS_NORMAL : status);
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (Constants.ROLE_ADMIN.equals(user.getRole())) {
            throw new BizException("不能重置管理员密码");
        }
        user.setPassword(encoder.encode("123456"));
        userMapper.updateById(user);
    }

    // ===== 商家审核 =====

    @Override
    public PageVO<Merchant> pageMerchants(int current, int size, String status, String keyword) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(Merchant::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Merchant::getMerchantName, kw)
                    .or().like(Merchant::getContactName, kw)
                    .or().like(Merchant::getContactPhone, kw));
        }
        wrapper.orderByAsc(Merchant::getStatus).orderByDesc(Merchant::getCreateTime);
        return PageVO.of(merchantMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void auditMerchant(Long merchantId, String status, String remark) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BizException(ResultCode.NOT_FOUND, "商家不存在");
        }
        if (!List.of(Constants.AUDIT_APPROVED, Constants.AUDIT_REJECTED, "DISABLED").contains(status)) {
            throw new BizException("审核状态不正确");
        }
        merchant.setStatus(status);
        merchant.setAuditRemark(remark);
        merchant.setAuditTime(LocalDateTime.now());
        merchantMapper.updateById(merchant);

        // 商家被驳回/禁用时,联动下架其名下所有已上架民宿,避免无资质商家继续经营;
        // 重新通过后民宿保持下架,由商家逐条重新提交审核(民宿审核独立)
        if (Constants.AUDIT_REJECTED.equals(status) || "DISABLED".equals(status)) {
            List<Homestay> mine = homestayMapper.selectList(new LambdaQueryWrapper<Homestay>()
                    .eq(Homestay::getMerchantId, merchantId)
                    .eq(Homestay::getAuditStatus, Constants.AUDIT_APPROVED));
            for (Homestay h : mine) {
                h.setAuditStatus(Constants.AUDIT_OFFLINE);
                h.setAuditTime(LocalDateTime.now());
                h.setAuditRemark("商家资质被" + ("DISABLED".equals(status) ? "禁用" : "驳回") + ",自动下架");
                homestayMapper.updateById(h);
            }
        }
    }

    // ===== 民宿审核/监管 =====

    @Override
    public PageVO<HomestayVO> pageHomestaysForAdmin(int current, int size, String auditStatus,
                                                    String keyword, Long destinationId) {
        LambdaQueryWrapper<Homestay> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(auditStatus)) {
            wrapper.eq(Homestay::getAuditStatus, auditStatus);
        }
        if (destinationId != null) {
            wrapper.eq(Homestay::getDestinationId, destinationId);
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Homestay::getName, kw).or().like(Homestay::getAddress, kw));
        }
        wrapper.orderByAsc(Homestay::getAuditStatus).orderByDesc(Homestay::getCreateTime);
        Page<Homestay> page = homestayMapper.selectPage(new Page<>(current, size), wrapper);

        PageVO<HomestayVO> vo = new PageVO<>();
        vo.setRecords(page.getRecords().stream().map(homestayService::toVO).toList());
        vo.setTotal(page.getTotal());
        vo.setCurrent(page.getCurrent());
        vo.setSize(page.getSize());
        vo.setPages(page.getPages());
        return vo;
    }

    @Override
    public void auditHomestay(Long homestayId, String auditStatus, String remark) {
        Homestay h = homestayMapper.selectById(homestayId);
        if (h == null) {
            throw new BizException(ResultCode.NOT_FOUND, "民宿不存在");
        }
        if (!List.of(Constants.AUDIT_APPROVED, Constants.AUDIT_REJECTED).contains(auditStatus)) {
            throw new BizException("审核状态不正确");
        }
        h.setAuditStatus(auditStatus);
        h.setAuditRemark(remark);
        h.setAuditTime(LocalDateTime.now());
        homestayMapper.updateById(h);
    }

    @Override
    public void offlineHomestay(Long homestayId) {
        Homestay h = homestayMapper.selectById(homestayId);
        if (h == null) {
            throw new BizException(ResultCode.NOT_FOUND, "民宿不存在");
        }
        h.setAuditStatus(Constants.AUDIT_OFFLINE);
        h.setAuditTime(LocalDateTime.now());
        h.setAuditRemark("平台管理员下架");
        homestayMapper.updateById(h);
    }

    // ===== 订单监管 =====

    @Override
    public PageVO<Orders> pageOrders(int current, int size, String status, String orderNo, String keyword) {
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
        return PageVO.of(ordersMapper.selectPage(new Page<>(current, size), wrapper));
    }

    // ===== 转换 =====

    private UserAdminVO toUserAdminVO(User user) {
        UserAdminVO vo = new UserAdminVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setRoleText(switch (user.getRole()) {
            case Constants.ROLE_USER -> "普通用户";
            case Constants.ROLE_MERCHANT -> "商家";
            case Constants.ROLE_ADMIN -> "管理员";
            default -> user.getRole();
        });
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        if (Constants.ROLE_MERCHANT.equals(user.getRole())) {
            Merchant m = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                    .eq(Merchant::getUserId, user.getId()).last("limit 1"));
            if (m != null) {
                vo.setMerchantId(m.getId());
                vo.setMerchantName(m.getMerchantName());
                vo.setMerchantStatus(m.getStatus());
                vo.setMerchantStatusText(StatusTextUtil.merchant(m.getStatus()));
            }
        }
        return vo;
    }
}
