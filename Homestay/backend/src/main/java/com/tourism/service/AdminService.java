package com.tourism.service;

import com.tourism.common.PageVO;
import com.tourism.entity.Merchant;
import com.tourism.entity.Orders;
import com.tourism.vo.HomestayVO;
import com.tourism.vo.UserAdminVO;

/**
 * 管理员服务:用户/商家/民宿/订单 监管
 */
public interface AdminService {

    // ===== 用户管理 =====
    PageVO<UserAdminVO> pageUsers(int current, int size, String keyword, String role);

    void changeUserStatus(Long userId, Integer status);

    /** 管理员重置用户密码为默认 123456 */
    void resetPassword(Long userId);

    // ===== 商家审核 =====
    PageVO<Merchant> pageMerchants(int current, int size, String status, String keyword);

    void auditMerchant(Long merchantId, String status, String remark);

    // ===== 民宿审核/监管 =====
    PageVO<HomestayVO> pageHomestaysForAdmin(int current, int size, String auditStatus,
                                             String keyword, Long destinationId);

    void auditHomestay(Long homestayId, String auditStatus, String remark);

    void offlineHomestay(Long homestayId);

    // ===== 订单监管 =====
    PageVO<Orders> pageOrders(int current, int size, String status, String orderNo, String keyword);
}
