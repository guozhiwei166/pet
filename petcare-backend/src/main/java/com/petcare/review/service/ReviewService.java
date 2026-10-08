package com.petcare.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.boarding.entity.BoardingOrder;
import com.petcare.boarding.mapper.BoardingOrderMapper;
import com.petcare.common.BusinessException;
import com.petcare.notification.NotificationService;
import com.petcare.pet.entity.Pet;
import com.petcare.pet.mapper.PetMapper;
import com.petcare.review.entity.Review;
import com.petcare.review.mapper.ReviewMapper;
import com.petcare.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价：寄养完成后主人五维评价 + 标签；管理员回复
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final BoardingOrderMapper orderMapper;
    private final PetMapper petMapper;
    private final NotificationService notificationService;

    @Transactional
    public void create(Long orderId, Map<String, Object> body) {
        Long uid = SecurityUtils.currentId();
        BoardingOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(uid)) throw new BusinessException("订单不存在");
        if (!"COMPLETED".equals(order.getStatus())) throw new BusinessException("寄养完成后才能评价");
        Long exists = reviewMapper.selectCount(new LambdaQueryWrapper<Review>().eq(Review::getOrderId, orderId));
        if (exists > 0) throw new BusinessException("该订单已评价过啦");

        Review r = new Review();
        r.setOrderId(orderId);
        r.setUserId(uid);
        r.setPetId(order.getPetId());
        r.setRatingOverall(requireRating(body.get("ratingOverall"), "整体体验"));
        r.setRatingEnv(requireRating(body.get("ratingEnv"), "环境"));
        r.setRatingService(requireRating(body.get("ratingService"), "服务"));
        r.setRatingStaff(requireRating(body.get("ratingStaff"), "工作人员"));
        r.setRatingPetStatus(requireRating(body.get("ratingPetStatus"), "宠物状态"));
        r.setContent((String) body.get("content"));
        // tags 可能是 JSON 数组（["环境干净","反馈及时"]），统一存成逗号分隔字符串
        Object tags = body.get("tags");
        if (tags instanceof List<?> list) {
            r.setTags(list.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")));
        } else if (tags != null) {
            r.setTags(String.valueOf(tags));
        }
        reviewMapper.insert(r);

        // 通知管理员有新评价
        notificationService.sendToRole("ROLE_ADMIN", "收到新评价",
                "订单 " + order.getOrderNo() + " 已收到评价，整体 " + r.getRatingOverall() + " 星。", "SYSTEM", "REVIEW", r.getId());
    }

    private int requireRating(Object v, String label) {
        if (v == null) throw new BusinessException("请为「" + label + "」评分");
        int n;
        try {
            n = Integer.parseInt(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(label + "评分格式不正确");
        }
        if (n < 1 || n > 5) throw new BusinessException(label + "评分需在1-5星之间");
        return n;
    }

    public void reply(Long reviewId, String reply) {
        // 商家回复：员工/管理员（与 @RequirePermission("review:reply") 权限码一致）
        String role = SecurityUtils.currentRole();
        if (!"ROLE_STAFF".equals(role) && !"ROLE_ADMIN".equals(role)) {
            throw new BusinessException(403, "仅工作人员可回复评价");
        }
        Review r = reviewMapper.selectById(reviewId);
        if (r == null) throw new BusinessException("评价不存在");
        r.setReply(reply);
        r.setReplyTime(LocalDateTime.now());
        reviewMapper.updateById(r);
    }

    /** 宠物维度的评价（成长档案）：属主本人可见；STAFF/ADMIN 全量可见 */
    public List<Review> listByPet(Long petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) throw new BusinessException("宠物不存在");
        String role = SecurityUtils.currentRole();
        if ("ROLE_USER".equals(role) && !pet.getUserId().equals(SecurityUtils.currentId())) {
            throw new BusinessException(403, "无权查看该宠物的评价");
        }
        return reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getPetId, petId).orderByDesc(Review::getId));
    }

    /** 管理端评价列表（带主人昵称摘要） */
    public List<Map<String, Object>> listAll() {
        return reviewMapper.selectList(new LambdaQueryWrapper<Review>().orderByDesc(Review::getId))
                .stream().map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", r.getId());
                    m.put("orderId", r.getOrderId());
                    m.put("petId", r.getPetId());
                    m.put("ratingOverall", r.getRatingOverall());
                    m.put("content", r.getContent());
                    m.put("tags", r.getTags());
                    m.put("reply", r.getReply());
                    m.put("createTime", r.getCreateTime());
                    return m;
                }).toList();
    }
}
