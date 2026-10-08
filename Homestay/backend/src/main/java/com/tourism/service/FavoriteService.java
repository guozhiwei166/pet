package com.tourism.service;

import com.tourism.common.PageVO;
import com.tourism.vo.FavoriteVO;

/**
 * 收藏服务(民宿/景点/体验/餐饮)
 */
public interface FavoriteService {

    /** 收藏或取消收藏(切换),返回是否已收藏 */
    boolean toggle(Long userId, int targetType, Long targetId);

    /** 取消收藏(不存在时静默成功,与 DELETE 语义一致) */
    void remove(Long userId, int targetType, Long targetId);

    /** 是否已收藏 */
    boolean isFavorited(Long userId, int targetType, Long targetId);

    /** 收藏列表(分页,按类型) */
    PageVO<FavoriteVO> page(Long userId, int current, int size, Integer targetType);
}
