package com.petcare.exception.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.BusinessException;
import com.petcare.exception.entity.PetException;
import com.petcare.exception.mapper.PetExceptionMapper;
import com.petcare.notification.NotificationService;
import com.petcare.pet.entity.Pet;
import com.petcare.pet.mapper.PetMapper;
import com.petcare.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 异常事件：上报 → 待处理 → 处理中 → 观察中 → 已解决 → 关闭
 * 通知规则：一般→通知管理员；严重→通知管理员 + 宠物主人
 */
@Service
@RequiredArgsConstructor
public class PetExceptionService {

    /** 合法流转：当前状态 → 允许的下一状态 */
    private static final Map<String, List<String>> FLOW = Map.of(
            "PENDING", List.of("PROCESSING"),
            "PROCESSING", List.of("OBSERVING", "RESOLVED"),
            "OBSERVING", List.of("RESOLVED"),
            "RESOLVED", List.of("CLOSED"));

    private final PetExceptionMapper exceptionMapper;
    private final PetMapper petMapper;
    private final NotificationService notificationService;

    /** 员工上报异常（仅 STAFF/ADMIN） */
    @Transactional
    public void report(Long petId, Long orderId, String type, String level, String description, String photos) {
        requireStaff();
        if (type == null || type.isBlank()) throw new BusinessException("请选择异常类型");
        if (level == null || !List.of("MILD", "NORMAL", "SEVERE").contains(level)) {
            throw new BusinessException("异常级别不合法");
        }
        if (description == null || description.isBlank()) throw new BusinessException("请填写情况描述");
        Pet pet = petMapper.selectById(petId);
        if (pet == null) throw new BusinessException("宠物不存在");

        PetException e = new PetException();
        e.setPetId(petId);
        e.setOrderId(orderId);
        e.setType(type);
        e.setLevel(level);
        e.setDescription(description);
        e.setPhotos(photos);
        e.setStatus("PENDING");
        e.setReportStaffId(SecurityUtils.currentId());
        exceptionMapper.insert(e);

        String levelText = "SEVERE".equals(level) ? "严重" : "NORMAL".equals(level) ? "一般" : "轻微";
        String title = "⚠ 毛孩子出现" + levelText + "异常";
        String content = pet.getName() + " 被工作人员上报异常（" + levelText + "）：" + description;

        if (!"MILD".equals(level)) {
            notificationService.sendToRole("ROLE_ADMIN", title, content, "EXCEPTION", "EXCEPTION", e.getId());
        }
        if ("SEVERE".equals(level)) {
            notificationService.send(pet.getUserId(), title, content + "，工作人员已第一时间处理，请留意后续通知。", "EXCEPTION", "EXCEPTION", e.getId());
        }
    }

    public List<Map<String, Object>> list(String status) {
        LambdaQueryWrapper<PetException> w = new LambdaQueryWrapper<PetException>().orderByDesc(PetException::getId);
        if (status != null && !status.isBlank() && !"ALL".equals(status)) w.eq(PetException::getStatus, status);
        return exceptionMapper.selectList(w).stream().map(this::toMap).collect(java.util.stream.Collectors.toList());
    }

    private Map<String, Object> toMap(PetException e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("petId", e.getPetId());
        m.put("orderId", e.getOrderId());
        m.put("type", e.getType());
        m.put("level", e.getLevel());
        m.put("description", e.getDescription());
        m.put("status", e.getStatus());
        m.put("createTime", e.getCreateTime());
        m.put("handleNote", e.getHandleNote());
        Pet pet = petMapper.selectById(e.getPetId());
        m.put("petName", pet == null ? "—" : pet.getName());
        return m;
    }

    private void requireStaff() {
        String role = SecurityUtils.currentRole();
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_STAFF".equals(role)) {
            throw new BusinessException(403, "仅工作人员可操作");
        }
    }

    /** 生命周期流转（后端裁决，禁止乱跳；仅 STAFF/ADMIN） */
    @Transactional
    public void transition(Long id, String next, String handleNote) {
        requireStaff();
        PetException e = exceptionMapper.selectById(id);
        if (e == null) throw new BusinessException("异常事件不存在");
        List<String> allowed = FLOW.get(e.getStatus());
        if (allowed == null || !allowed.contains(next)) {
            throw new BusinessException("不允许从 " + e.getStatus() + " 流转到 " + next);
        }
        e.setStatus(next);
        e.setHandlerId(SecurityUtils.currentId());
        e.setHandleNote(handleNote == null || handleNote.isBlank() ? e.getHandleNote() : handleNote);
        e.setHandleTime(LocalDateTime.now());
        exceptionMapper.updateById(e);

        // 关闭时通知主人（若曾通知过）
        if ("CLOSED".equals(next) && "SEVERE".equals(e.getLevel())) {
            Pet pet = petMapper.selectById(e.getPetId());
            if (pet != null) {
                notificationService.send(pet.getUserId(), "✓ 异常事件已关闭",
                        pet.getName() + " 的异常事件已处理完毕，状态恢复正常。", "EXCEPTION", "EXCEPTION", e.getId());
            }
        }
    }
}
