package com.tourism.service;

import com.tourism.common.PageVO;
import com.tourism.dto.ReviewCreateDTO;
import com.tourism.vo.ReviewVO;

/**
 * 评价服务
 */
public interface ReviewService {

    /** 发表评价:校验订单归属 + 已完成/入住中 + 未重复评价 */
    ReviewVO create(ReviewCreateDTO dto, Long userId);

    /** 民宿评价分页(游客/用户可见,含评分分布) */
    PageVO<ReviewVO> pageByHomestay(Long homestayId, int current, int size);

    /** 我的评价分页 */
    PageVO<ReviewVO> pageByUser(Long userId, int current, int size);

    /** 商家回复(校验商家拥有该民宿) */
    void reply(Long reviewId, Long merchantId, String content);

    /** 管理员隐藏违规评价(联动重算民宿评分) */
    void hideByAdmin(Long reviewId);

    /** 管理员恢复展示(联动重算民宿评分) */
    void restoreByAdmin(Long reviewId);

    /** 按民宿可见评价重算平均分与人数(隐藏/恢复/删除评价后调用) */
    void recalcRating(Long homestayId);

    /** 商家查看本民宿全部评价 */
    PageVO<ReviewVO> pageByMerchant(Long merchantId, int current, int size, Long homestayId);

    /** 管理员查看全平台评价(可按状态过滤) */
    PageVO<ReviewVO> pageAllForAdmin(int current, int size, Integer status);
}
