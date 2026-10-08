package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourism.common.BizException;
import com.tourism.common.Constants;
import com.tourism.common.PageVO;
import com.tourism.common.ResultCode;
import com.tourism.dto.ReviewCreateDTO;
import com.tourism.entity.Homestay;
import com.tourism.entity.Orders;
import com.tourism.entity.Review;
import com.tourism.entity.RoomType;
import com.tourism.entity.User;
import com.tourism.mapper.HomestayMapper;
import com.tourism.mapper.OrdersMapper;
import com.tourism.mapper.ReviewMapper;
import com.tourism.mapper.RoomTypeMapper;
import com.tourism.mapper.UserMapper;
import com.tourism.service.ReviewService;
import com.tourism.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 评价服务实现
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrdersMapper ordersMapper;
    private final HomestayMapper homestayMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ReviewVO create(ReviewCreateDTO dto, Long userId) {
        Orders order = ordersMapper.selectById(dto.getOrderId());
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        // 仅已完成(或入住中)订单可评价;待支付/已取消不可
        if (!Constants.ORDER_COMPLETED.equals(order.getStatus())
                && !Constants.ORDER_CHECKED_IN.equals(order.getStatus())) {
            throw new BizException("订单完成后才能评价");
        }
        // 一个订单只能评价一次(order_id 唯一 + 业务判断)
        Long exist = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, order.getId()));
        if (exist > 0) {
            throw new BizException(ResultCode.CONFLICT, "该订单已评价,不能重复评价");
        }

        Review review = new Review();
        review.setOrderId(order.getId());
        review.setUserId(userId);
        review.setHomestayId(order.getHomestayId());
        review.setRating(dto.getRating());
        review.setHygieneScore(dto.getHygieneScore());
        review.setServiceScore(dto.getServiceScore());
        review.setFacilityScore(dto.getFacilityScore());
        review.setLocationScore(dto.getLocationScore());
        review.setContent(dto.getContent());
        review.setImages(writeImages(dto.getImages()));
        review.setStatus(Constants.REVIEW_VISIBLE);
        reviewMapper.insert(review);

        // 更新民宿总评分(加权平均)与评分人数
        refreshHomestayRating(order.getHomestayId());
        return toVO(reviewMapper.selectById(review.getId()));
    }

    @Override
    public PageVO<ReviewVO> pageByHomestay(Long homestayId, int current, int size) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .eq(Review::getHomestayId, homestayId)
                .eq(Review::getStatus, Constants.REVIEW_VISIBLE)
                .orderByDesc(Review::getCreateTime);
        Page<Review> page = reviewMapper.selectPage(new Page<>(current, size), wrapper);
        return toPageVO(page);
    }

    @Override
    public PageVO<ReviewVO> pageByUser(Long userId, int current, int size) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreateTime);
        Page<Review> page = reviewMapper.selectPage(new Page<>(current, size), wrapper);
        return toPageVO(page);
    }

    @Override
    public PageVO<ReviewVO> pageByMerchant(Long merchantId, int current, int size, Long homestayId) {
        List<Homestay> mine = homestayMapper.selectList(new LambdaQueryWrapper<Homestay>()
                .eq(Homestay::getMerchantId, merchantId));
        List<Long> ids = mine.stream().map(Homestay::getId).toList();
        if (ids.isEmpty()) {
            return new PageVO<>();
        }
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .in(Review::getHomestayId, ids);
        if (homestayId != null) {
            wrapper.eq(Review::getHomestayId, homestayId);
        }
        wrapper.orderByDesc(Review::getCreateTime);
        return toPageVO(reviewMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @Override
    public PageVO<ReviewVO> pageAllForAdmin(int current, int size, Integer status) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Review::getStatus, status);
        }
        wrapper.orderByDesc(Review::getCreateTime);
        return toPageVO(reviewMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @Override
    public void reply(Long reviewId, Long merchantId, String content) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BizException(ResultCode.NOT_FOUND, "评价不存在");
        }
        Homestay h = homestayMapper.selectById(review.getHomestayId());
        if (h == null || !Objects.equals(h.getMerchantId(), merchantId)) {
            throw new BizException(ResultCode.FORBIDDEN, "无权回复该评价");
        }
        review.setMerchantReply(content);
        review.setReplyTime(LocalDateTime.now());
        reviewMapper.updateById(review);
    }

    @Override
    public void hideByAdmin(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review != null) {
            review.setStatus(Constants.REVIEW_HIDDEN);
            reviewMapper.updateById(review);
            recalcRating(review.getHomestayId());
        }
    }

    @Override
    public void recalcRating(Long homestayId) {
        refreshHomestayRating(homestayId);
    }

    @Override
    public void restoreByAdmin(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review != null) {
            review.setStatus(Constants.REVIEW_VISIBLE);
            reviewMapper.updateById(review);
            recalcRating(review.getHomestayId());
        }
    }

    // ==================== 内部 ====================

    private void refreshHomestayRating(Long homestayId) {
        List<Review> list = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getHomestayId, homestayId)
                .eq(Review::getStatus, Constants.REVIEW_VISIBLE));
        Homestay h = homestayMapper.selectById(homestayId);
        if (h == null) {
            return;
        }
        if (list.isEmpty()) {
            // 可见评价为空(如全部被隐藏):恢复默认值,避免残留旧评分
            h.setRating(new BigDecimal("5.0"));
            h.setRatingCount(0);
            homestayMapper.updateById(h);
            return;
        }
        double sum = list.stream().mapToInt(Review::getRating).sum();
        BigDecimal rating = BigDecimal.valueOf(sum / list.size()).setScale(1, RoundingMode.HALF_UP);
        h.setRating(rating);
        h.setRatingCount(list.size());
        homestayMapper.updateById(h);
    }

    private String writeImages(List<String> images) {
        try {
            return images == null || images.isEmpty() ? null : objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> readImages(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private PageVO<ReviewVO> toPageVO(Page<Review> page) {
        PageVO<ReviewVO> result = new PageVO<>();
        result.setRecords(page.getRecords().stream().map(this::toVO).toList());
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }

    private ReviewVO toVO(Review review) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setOrderId(review.getOrderId());
        vo.setUserId(review.getUserId());
        vo.setHomestayId(review.getHomestayId());
        vo.setRating(review.getRating());
        vo.setHygieneScore(review.getHygieneScore());
        vo.setServiceScore(review.getServiceScore());
        vo.setFacilityScore(review.getFacilityScore());
        vo.setLocationScore(review.getLocationScore());
        vo.setContent(review.getContent());
        vo.setImages(readImages(review.getImages()));
        vo.setMerchantReply(review.getMerchantReply());
        vo.setReplyTime(review.getReplyTime());
        vo.setStatus(review.getStatus());
        vo.setCreateTime(review.getCreateTime());

        User user = userMapper.selectById(review.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }
        Homestay h = homestayMapper.selectById(review.getHomestayId());
        if (h != null) {
            vo.setHomestayRating(h.getRating());
        }
        Orders order = ordersMapper.selectById(review.getOrderId());
        if (order != null) {
            RoomType rt = roomTypeMapper.selectById(order.getRoomTypeId());
            if (rt != null) {
                vo.setRoomTypeName(rt.getName());
            }
        }
        return vo;
    }
}
