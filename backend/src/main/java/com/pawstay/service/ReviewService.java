package com.pawstay.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pawstay.common.BusinessException;
import com.pawstay.dto.ReviewDTO;
import com.pawstay.entity.BoardingOrder;
import com.pawstay.entity.Merchant;
import com.pawstay.entity.Review;
import com.pawstay.entity.User;
import com.pawstay.mapper.MerchantMapper;
import com.pawstay.mapper.OrderMapper;
import com.pawstay.mapper.ReviewMapper;
import com.pawstay.mapper.UserMapper;
import com.pawstay.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private UserMapper userMapper;

    /** 订单完成后主人评价；评价后回算商家评分 */
    public void create(Long orderId, ReviewDTO dto) {
        Long uid = UserContext.requireLogin().getId();
        BoardingOrder o = orderMapper.selectById(orderId);
        if (o == null || !o.getUserId().equals(uid)) throw new BusinessException("订单不存在");
        if (!"COMPLETED".equals(o.getStatus())) throw new BusinessException("寄养完成后才能评价");
        Long exists = reviewMapper.selectCount(new LambdaQueryWrapper<Review>().eq(Review::getOrderId, orderId));
        if (exists > 0) throw new BusinessException("该订单已评价过啦");
        if (dto.rating() == null || dto.rating() < 1 || dto.rating() > 5) throw new BusinessException("评分需在1-5星之间");

        Review r = new Review();
        r.setOrderId(orderId);
        r.setUserId(uid);
        r.setMerchantId(o.getMerchantId());
        r.setRating(dto.rating());
        r.setContent(dto.content());
        r.setPhotos(dto.photos());
        r.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(r);

        refreshMerchantRating(o.getMerchantId());
    }

    public void reply(Long reviewId, String reply) {
        Review r = reviewMapper.selectById(reviewId);
        if (r == null) throw new BusinessException("评价不存在");
        Merchant m = merchantMapper.selectById(r.getMerchantId());
        if (m == null || !m.getUserId().equals(UserContext.requireLogin().getId())) throw new BusinessException(403, "无权回复该评价");
        r.setReply(reply);
        reviewMapper.updateById(r);
    }

    public List<Map<String, Object>> listByMerchant(Long merchantId, int limit) {
        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getMerchantId, merchantId).orderByDesc(Review::getId).last("LIMIT " + Math.min(limit, 50)));
        List<Map<String, Object>> vos = new ArrayList<>();
        for (Review r : reviews) {
            Map<String, Object> vo = new LinkedHashMap<>();
            vo.put("id", r.getId());
            vo.put("rating", r.getRating());
            vo.put("content", r.getContent());
            vo.put("photos", r.getPhotos());
            vo.put("reply", r.getReply());
            vo.put("createTime", r.getCreateTime());
            User u = userMapper.selectById(r.getUserId());
            vo.put("nickname", u == null ? "匿名用户" : u.getNickname());
            vos.add(vo);
        }
        return vos;
    }

    private void refreshMerchantRating(Long merchantId) {
        List<Review> all = reviewMapper.selectList(new LambdaQueryWrapper<Review>().eq(Review::getMerchantId, merchantId));
        double avg = all.stream().mapToInt(Review::getRating).average().orElse(5.0);
        Merchant m = merchantMapper.selectById(merchantId);
        if (m != null) {
            m.setRating(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
            merchantMapper.updateById(m);
        }
    }
}
