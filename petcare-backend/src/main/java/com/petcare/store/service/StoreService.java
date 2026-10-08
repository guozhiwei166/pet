package com.petcare.store.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.BusinessException;
import com.petcare.common.Params;
import com.petcare.pet.entity.Pet;
import com.petcare.pet.mapper.PetMapper;
import com.petcare.room.entity.ServiceItem;
import com.petcare.room.mapper.ServiceMapper;
import com.petcare.security.SecurityUtils;
import com.petcare.store.entity.ServiceBooking;
import com.petcare.store.entity.StoreProfile;
import com.petcare.store.mapper.ServiceBookingMapper;
import com.petcare.store.mapper.StoreProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** 门店信息 + 服务预约（洗澡/美容/接送等，独立于寄养主订单） */
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreProfileMapper profileMapper;
    private final ServiceBookingMapper bookingMapper;
    private final ServiceMapper serviceMapper;
    private final PetMapper petMapper;

    // ---------- 门店信息 ----------

    public StoreProfile profile() {
        return profileMapper.selectList(null).stream().findFirst().orElse(null);
    }

    public void saveProfile(StoreProfile profile) {
        SecurityUtils.requireRole("ROLE_ADMIN");
        StoreProfile existing = profileMapper.selectList(null).stream().findFirst().orElse(null);
        if (existing == null) {
            profileMapper.insert(profile);
        } else {
            profile.setId(existing.getId());
            profile.setUpdateTime(java.time.LocalDateTime.now());
            profileMapper.updateById(profile);
        }
    }

    // ---------- 服务预约 ----------

    /** 我的预约列表 */
    public List<ServiceBooking> myBookings() {
        return bookingMapper.selectList(new LambdaQueryWrapper<ServiceBooking>()
                .eq(ServiceBooking::getUserId, SecurityUtils.currentId())
                .orderByDesc(ServiceBooking::getBookDate));
    }

    /** 当日预约（员工端看板；全店数据，控制器已限 STAFF/ADMIN，此处再兜一层） */
    public List<ServiceBooking> bookingsByDate(LocalDate date) {
        requireStaff();
        LocalDate target = date == null ? LocalDate.now() : date;
        return bookingMapper.selectList(new LambdaQueryWrapper<ServiceBooking>()
                .eq(ServiceBooking::getBookDate, target).orderByAsc(ServiceBooking::getBookTime));
    }

    @Transactional
    public ServiceBooking create(Map<String, Object> body) {
        Long uid = SecurityUtils.currentId();
        Long petId = Params.requireLong(body, "petId", "宠物");
        Long serviceId = Params.requireLong(body, "serviceId", "服务");
        Pet pet = petMapper.selectById(petId);
        if (pet == null || !pet.getUserId().equals(uid)) throw new BusinessException("宠物不存在");
        ServiceItem svc = serviceMapper.selectById(serviceId);
        if (svc == null || svc.getStatus() != 1) throw new BusinessException("服务不可预约");
        LocalDate bookDate = Params.requireDate(body, "bookDate", "预约日期");
        if (bookDate.isBefore(LocalDate.now())) throw new BusinessException("不能预约过去的日期");

        ServiceBooking b = new ServiceBooking();
        b.setUserId(uid);
        b.setPetId(petId);
        b.setServiceId(serviceId);
        b.setServiceName(svc.getName());
        b.setPrice(svc.getPrice());
        b.setBookDate(bookDate);
        b.setBookTime(Params.str(body, "bookTime"));
        b.setStatus("PENDING");
        b.setRemark(Params.str(body, "remark"));
        bookingMapper.insert(b);
        return b;
    }

    /** 员工确认完成 / 用户取消 */
    @Transactional
    public void transition(Long id, String action) {
        ServiceBooking b = bookingMapper.selectById(id);
        if (b == null) throw new BusinessException("预约不存在");
        String role = SecurityUtils.currentRole();
        if ("CANCEL".equals(action)) {
            if (!b.getUserId().equals(SecurityUtils.currentId())) throw new BusinessException(403, "无权操作");
            if (!"PENDING".equals(b.getStatus())) throw new BusinessException("仅待确认的预约可取消");
            b.setStatus("CANCELLED");
        } else if ("CONFIRM".equals(action) || "DONE".equals(action)) {
            if (!"ROLE_STAFF".equals(role) && !"ROLE_ADMIN".equals(role)) throw new BusinessException(403, "仅工作人员可操作");
            b.setStatus("CONFIRM".equals(action) ? "CONFIRMED" : "DONE");
        } else {
            throw new BusinessException("未知操作");
        }
        bookingMapper.updateById(b);
    }

    private void requireStaff() {
        String role = SecurityUtils.currentRole();
        if (!"ROLE_STAFF".equals(role) && !"ROLE_ADMIN".equals(role)) {
            throw new BusinessException(403, "仅工作人员可操作");
        }
    }
}
