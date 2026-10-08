package com.petcare.care.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.care.entity.FeedingRecord;
import com.petcare.care.entity.PetDailyReport;
import com.petcare.care.mapper.FeedingRecordMapper;
import com.petcare.care.mapper.PetDailyReportMapper;
import com.petcare.care.mapper.ActivityRecordMapper;
import com.petcare.care.entity.ActivityRecord;
import com.petcare.pet.entity.PetHealth;
import com.petcare.pet.mapper.PetHealthMapper;
import com.petcare.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 宠物每日照护日报（核心创新）：由当天喂食/活动/健康观察记录自动汇总生成
 */
@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final PetDailyReportMapper reportMapper;
    private final FeedingRecordMapper feedingMapper;
    private final ActivityRecordMapper activityMapper;
    private final PetHealthMapper healthMapper;
    private final NotificationService notificationService;
    private final com.petcare.boarding.mapper.BoardingOrderMapper orderMapper;

    /** 任务完成时刷新当天日报（存在则更新，不存在则创建） */
    public void refresh(Long orderId, LocalDate date, Long petId) {
        List<FeedingRecord> meals = feedingMapper.selectList(new LambdaQueryWrapper<FeedingRecord>()
                .eq(FeedingRecord::getOrderId, orderId)
                .apply("DATE(record_time) = {0}", date));

        List<ActivityRecord> activities = activityMapper.selectList(new LambdaQueryWrapper<ActivityRecord>()
                .eq(ActivityRecord::getOrderId, orderId)
                .apply("DATE(record_time) = {0}", date));

        PetHealth health = healthMapper.selectOne(new LambdaQueryWrapper<PetHealth>()
                .eq(PetHealth::getOrderId, orderId)
                .eq(PetHealth::getRecordDate, date)
                .orderByDesc(PetHealth::getId)
                .last("LIMIT 1"));

        String mealsSummary = meals.stream()
                .map(r -> r.getFood() + (r.getAmount() == null ? "" : " " + r.getAmount()))
                .collect(Collectors.joining("、"));
        int activityMinutes = activities.stream()
                .mapToInt(a -> a.getDurationMinutes() == null ? 0 : a.getDurationMinutes()).sum();

        PetDailyReport report = reportMapper.selectOne(new LambdaQueryWrapper<PetDailyReport>()
                .eq(PetDailyReport::getOrderId, orderId)
                .eq(PetDailyReport::getReportDate, date));
        boolean created = report == null;
        if (created) {
            report = new PetDailyReport();
            report.setOrderId(orderId);
            report.setPetId(petId);
            report.setReportDate(date);
        }
        if (!meals.isEmpty()) report.setMealsSummary(mealsSummary);
        report.setActivityMinutes(activityMinutes);
        if (health != null) {
            report.setMentalState(health.getMentalState());
            report.setAppetite(health.getAppetite());
            report.setWater(health.getWater());
            report.setPoop(health.getPoop());
            report.setIsAbnormal(health.getIsAbnormal());
        }
        if (created) {
            reportMapper.insert(report);
        } else {
            reportMapper.updateById(report);
        }

        // 日报生成即通知主人（每天最多打扰一次：仅首次创建时发送）
        if (created) {
            var order = orderMapper.selectById(orderId);
            Long ownerUserId = order == null ? null : order.getUserId();
            // refType=REPORT 但 refId 指向订单（前端据此跳转订单详情查看日报）
            notificationService.send(ownerUserId, "🐾 今日日报已生成",
                    "毛孩子 " + date + " 的照护日报已更新，快去看看它今天的状态吧。", "DAILY", "ORDER", orderId);
        }
    }

    /** 订单维度日报列表 */
    public List<PetDailyReport> listByOrder(Long orderId) {
        return reportMapper.selectList(new LambdaQueryWrapper<PetDailyReport>()
                .eq(PetDailyReport::getOrderId, orderId)
                .orderByDesc(PetDailyReport::getReportDate));
    }

    /** 宠物维度日报（成长档案） */
    public List<PetDailyReport> listByPet(Long petId, int days) {
        return reportMapper.selectList(new LambdaQueryWrapper<PetDailyReport>()
                .eq(PetDailyReport::getPetId, petId)
                .ge(PetDailyReport::getReportDate, LocalDate.now().minusDays(days))
                .orderByDesc(PetDailyReport::getReportDate));
    }

    /** 员工备注写入日报 */
    public void saveNote(Long orderId, LocalDate date, String note, String photos, Long staffId) {
        PetDailyReport report = reportMapper.selectOne(new LambdaQueryWrapper<PetDailyReport>()
                .eq(PetDailyReport::getOrderId, orderId).eq(PetDailyReport::getReportDate, date));
        if (report == null) {
            var order = orderMapper.selectById(orderId);
            if (order == null) throw new com.petcare.common.BusinessException("订单不存在");
            report = new PetDailyReport();
            report.setOrderId(orderId);
            report.setPetId(order.getPetId());
            report.setReportDate(date);
        }
        report.setStaffNote(note);
        if (photos != null) report.setPhotos(photos);
        report.setStaffId(staffId);
        if (report.getId() == null) reportMapper.insert(report);
        else reportMapper.updateById(report);
    }
}
