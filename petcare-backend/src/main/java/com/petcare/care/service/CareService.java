package com.petcare.care.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.boarding.entity.BoardingOrder;
import com.petcare.boarding.mapper.BoardingOrderMapper;
import com.petcare.care.entity.*;
import com.petcare.care.mapper.*;
import com.petcare.common.BusinessException;
import com.petcare.common.Params;
import com.petcare.pet.entity.Pet;
import com.petcare.pet.mapper.PetMapper;
import com.petcare.pet.mapper.PetHealthMapper;
import com.petcare.room.entity.Room;
import com.petcare.room.mapper.RoomMapper;
import com.petcare.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 照护服务：任务生成、员工今日工作台、完成任务落五类记录
 */
@Service
@RequiredArgsConstructor
public class CareService {

    private final CareTaskMapper taskMapper;
    private final FeedingRecordMapper feedingMapper;
    private final ActivityRecordMapper activityMapper;
    private final MedicationRecordMapper medicationMapper;
    private final CleaningRecordMapper cleaningMapper;
    private final PetHealthMapper petHealthMapper;
    private final PetMapper petMapper;
    private final RoomMapper roomMapper;
    private final BoardingOrderMapper orderMapper;
    private final DailyReportService dailyReportService;

    /** 入住确认时生成整段寄养期的照护任务：每日早餐/遛宠/健康观察，入住与离店日加检查任务 */
    @Transactional
    public void generateTasks(BoardingOrder order) {
        LocalDate start = order.getStartDate();
        LocalDate end = order.getEndDate();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            boolean first = d.equals(start);
            boolean last = d.equals(end);
            if (first) {
                addTask(order, d, "09:00", "CHECKIN_CHECK", "入住检查", "核对疫苗、体重登记、习惯交接");
            }
            addTask(order, d, "08:30", "FEEDING", "早餐", "狗粮 150g");
            addTask(order, d, "11:00", "CLEANING", "房间清洁", "清扫消毒、更换垫料");
            if (!last) addTask(order, d, "15:30", "WALK", "遛宠活动", "户外活动 30 分钟");
            addTask(order, d, "16:00", "HEALTH_CHECK", "健康观察", "精神/食欲/饮水/排便");
            // 备注中提到喂药时生成喂药任务（演示规则；后续可扩展为独立喂药医嘱字段）
            if (order.getRemark() != null && order.getRemark().contains("药")) {
                addTask(order, d, "09:30", "MEDICATION", "喂药", "按医嘱定时喂药并记录");
            }
            if (last) {
                addTask(order, d, "09:00", "CHECKOUT_CHECK", "离店检查", "物品清点、体重登记、交接说明");
            }
        }
    }

    private void addTask(BoardingOrder order, LocalDate date, String time, String type, String title, String content) {
        CareTask t = new CareTask();
        t.setOrderId(order.getId());
        t.setPetId(order.getPetId());
        t.setRoomId(order.getRoomId());
        t.setTaskType(type);
        t.setTaskDate(date);
        t.setPlanTime(time);
        t.setTitle(title);
        t.setContent(content);
        t.setStatus("PENDING");
        taskMapper.insert(t);
    }

    /** 员工今日工作台：统计 + 今日任务（含宠物与房间信息） */
    /** 员工工作台：统计 + 任务（按 date，缺省今天） */
    public Map<String, Object> workbench(LocalDate date) {
        LocalDate target = date == null ? LocalDate.now() : date;
        List<Map<String, Object>> tasks = tasksOfDay(target);
        long done = tasks.stream().filter(t -> "DONE".equals(t.get("status"))).count();
        long pending = tasks.stream().filter(t -> "PENDING".equals(t.get("status"))).count();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("date", target.toString());
        res.put("total", tasks.size());
        res.put("done", done);
        res.put("pending", pending);
        res.put("tasks", tasks);
        return res;
    }

    /** 某日任务列表（带宠物名/主人/房间号，供卡片展示） */
    public List<Map<String, Object>> tasksOfDay(LocalDate date) {
        List<CareTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<CareTask>()
                .eq(CareTask::getTaskDate, date)
                .orderByAsc(CareTask::getPlanTime));
        return tasks.stream().map(this::toMap).collect(java.util.stream.Collectors.toList());
    }

    public List<Map<String, Object>> tasks(LocalDate date, String type, String status) {
        LocalDate date0 = date == null ? LocalDate.now() : date;
        List<Map<String, Object>> list = tasksOfDay(date0);
        return list.stream()
                .filter(t -> type == null || type.isBlank() || "ALL".equals(type) || type.equals(t.get("taskType")))
                .filter(t -> status == null || status.isBlank() || "ALL".equals(status) || status.equals(t.get("status")))
                .collect(java.util.stream.Collectors.toList());
    }

    private Map<String, Object> toMap(CareTask t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("orderId", t.getOrderId());
        m.put("petId", t.getPetId());
        m.put("roomId", t.getRoomId());
        m.put("taskType", t.getTaskType());
        m.put("taskDate", t.getTaskDate().toString());
        m.put("planTime", t.getPlanTime());
        m.put("title", t.getTitle());
        m.put("content", t.getContent());
        m.put("status", t.getStatus());
        m.put("remark", t.getRemark());
        Pet pet = petMapper.selectById(t.getPetId());
        if (pet != null) {
            m.put("petName", pet.getName());
            m.put("petNotes", pet.getNotes());
            m.put("petPhoto", pet.getPhoto());
        }
        Room room = t.getRoomId() == null ? null : roomMapper.selectById(t.getRoomId());
        m.put("roomNo", room == null ? "待分配" : room.getRoomNo());
        return m;
    }

    /** 完成任务：按类型落对应记录表；健康观察同时写 pet_health；每次完成刷新当日日报 */
    @Transactional
    public void complete(Long taskId, Map<String, Object> body) {
        CareTask task = taskMapper.selectById(taskId);
        if (task == null) throw new BusinessException("任务不存在");
        if ("DONE".equals(task.getStatus())) throw new BusinessException("该任务已完成");
        Long staffId = SecurityUtils.currentId();
        LocalDateTime now = LocalDateTime.now();

        task.setStatus("DONE");
        task.setStaffId(staffId);
        task.setDoneTime(now);
        task.setRemark(Params.str(body, "remark"));
        taskMapper.updateById(task);

        switch (task.getTaskType()) {
            case "FEEDING" -> {
                FeedingRecord r = new FeedingRecord();
                r.setTaskId(taskId); r.setOrderId(task.getOrderId()); r.setPetId(task.getPetId());
                r.setFood(bodyStr(body, "food") != null ? bodyStr(body, "food") : task.getContent());
                r.setAmount(bodyStr(body, "amount"));
                r.setAppetite(bodyStr(body, "appetite") != null ? bodyStr(body, "appetite") : "正常");
                r.setStaffId(staffId); r.setRecordTime(now); r.setRemark(task.getRemark());
                feedingMapper.insert(r);
            }
            case "WALK" -> {
                ActivityRecord r = new ActivityRecord();
                r.setTaskId(taskId); r.setOrderId(task.getOrderId()); r.setPetId(task.getPetId());
                Integer duration = Params.intOrNull(body, "durationMinutes", "活动时长");
                r.setDurationMinutes(duration == null || duration < 1 ? 30 : duration);
                r.setMood(bodyStr(body, "mood") != null ? bodyStr(body, "mood") : "活跃");
                r.setPhotos(bodyStr(body, "photos"));
                r.setStaffId(staffId); r.setRecordTime(now); r.setRemark(task.getRemark());
                activityMapper.insert(r);
            }
            case "MEDICATION" -> {
                MedicationRecord r = new MedicationRecord();
                r.setTaskId(taskId); r.setOrderId(task.getOrderId()); r.setPetId(task.getPetId());
                r.setMedicine(bodyStr(body, "medicine")); r.setDosage(bodyStr(body, "dosage"));
                r.setStaffId(staffId); r.setRecordTime(now);
                medicationMapper.insert(r);
            }
            case "HEALTH_CHECK", "CHECKIN_CHECK", "CHECKOUT_CHECK" -> {
                com.petcare.pet.entity.PetHealth h = new com.petcare.pet.entity.PetHealth();
                h.setPetId(task.getPetId());
                h.setOrderId(task.getOrderId());
                h.setRecordDate(task.getTaskDate());
                h.setWeight(Params.decimalOrNull(body, "weight", "体重"));
                h.setTemperature(Params.decimalOrNull(body, "temperature", "体温"));
                h.setMentalState(bodyStr(body, "mentalState") != null ? bodyStr(body, "mentalState") : "正常");
                h.setAppetite(bodyStr(body, "appetite") != null ? bodyStr(body, "appetite") : "正常");
                h.setWater(bodyStr(body, "water") != null ? bodyStr(body, "water") : "正常");
                h.setPoop(bodyStr(body, "poop") != null ? bodyStr(body, "poop") : "正常");
                h.setIsAbnormal(body != null && "1".equals(String.valueOf(body.getOrDefault("isAbnormal", 0))) ? 1 : 0);
                h.setRemark(task.getRemark());
                h.setStaffId(staffId);
                petHealthMapper.insert(h);
                // 离店检查：回写宠物体重
                if ("CHECKOUT_CHECK".equals(task.getTaskType()) && h.getWeight() != null) {
                    Pet pet = petMapper.selectById(task.getPetId());
                    pet.setWeight(h.getWeight());
                    petMapper.updateById(pet);
                }
            }
            case "CLEANING" -> {
                CleaningRecord r = new CleaningRecord();
                r.setTaskId(taskId); r.setRoomId(task.getRoomId()); r.setOrderId(task.getOrderId());
                r.setStaffId(staffId); r.setRecordTime(now);
                cleaningMapper.insert(r);
            }
            default -> { /* 其它类型仅标记完成 */ }
        }

        // 记录落库后再刷新当日日报
        dailyReportService.refresh(task.getOrderId(), task.getTaskDate(), task.getPetId());
    }

    public void skip(Long taskId, String remark) {
        CareTask task = taskMapper.selectById(taskId);
        if (task == null) throw new BusinessException("任务不存在");
        if ("DONE".equals(task.getStatus())) throw new BusinessException("该任务已完成");
        task.setStatus("SKIPPED");
        task.setStaffId(SecurityUtils.currentId());
        task.setRemark(remark == null || remark.isBlank() ? "员工跳过" : remark);
        task.setDoneTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    /** 订单维度照护记录（时间线数据源，Phase 6 日报复用） */
    public List<Map<String, Object>> orderTimeline(Long orderId) {
        List<Map<String, Object>> timeline = new java.util.ArrayList<>();
        for (FeedingRecord r : feedingMapper.selectList(new LambdaQueryWrapper<FeedingRecord>()
                .eq(FeedingRecord::getOrderId, orderId).orderByDesc(FeedingRecord::getRecordTime))) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", "FEEDING"); m.put("icon", "喂食");
            m.put("time", r.getRecordTime());
            m.put("title", r.getFood() + " " + (r.getAmount() == null ? "" : r.getAmount()));
            m.put("detail", "食欲：" + (r.getAppetite() == null ? "正常" : r.getAppetite()));
            m.put("photos", List.of());
            timeline.add(m);
        }
        for (ActivityRecord r : activityMapper.selectList(new LambdaQueryWrapper<ActivityRecord>()
                .eq(ActivityRecord::getOrderId, orderId).orderByDesc(ActivityRecord::getRecordTime))) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", "WALK"); m.put("icon", "活动");
            m.put("time", r.getRecordTime());
            m.put("title", "户外活动 " + (r.getDurationMinutes() == null ? "" : r.getDurationMinutes() + " 分钟"));
            m.put("detail", "状态：" + (r.getMood() == null ? "良好" : r.getMood()));
            m.put("photos", r.getPhotos() == null || r.getPhotos().isBlank()
                    ? List.of() : List.of(r.getPhotos().split(",")));
            timeline.add(m);
        }
        for (com.petcare.pet.entity.PetHealth h : petHealthMapper.selectList(new LambdaQueryWrapper<com.petcare.pet.entity.PetHealth>()
                .eq(com.petcare.pet.entity.PetHealth::getOrderId, orderId)
                .orderByDesc(com.petcare.pet.entity.PetHealth::getRecordDate))) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", "HEALTH"); m.put("icon", "健康观察");
            m.put("time", h.getRecordDate().atStartOfDay());
            m.put("title", "健康观察" + (h.getIsAbnormal() == 1 ? "（异常）" : ""));
            m.put("detail", "精神 " + h.getMentalState() + " · 食欲 " + h.getAppetite()
                    + " · 饮水 " + h.getWater() + " · 排便 " + h.getPoop()
                    + (h.getWeight() == null ? "" : " · 体重 " + h.getWeight() + "kg"));
            m.put("photos", List.of());
            timeline.add(m);
        }
        timeline.sort((a, b) -> ((LocalDateTime) b.get("time")).compareTo((LocalDateTime) a.get("time")));
        return timeline;
    }

    private String bodyStr(Map<String, Object> body, String key) {
        return Params.str(body, key);
    }
}
