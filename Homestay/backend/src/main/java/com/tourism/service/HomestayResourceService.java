package com.tourism.service;

import com.tourism.vo.HomestayLinksVO;

import java.util.List;

/**
 * 民宿-旅游资源关联维护(商家/管理员在民宿编辑页维护)
 */
public interface HomestayResourceService {

    /** 保存民宿-景点关联(全量替换) */
    void saveScenic(Long homestayId, Long merchantId, List<Long> scenicIds);

    /** 保存民宿-体验关联 */
    void saveExperience(Long homestayId, Long merchantId, List<Long> experienceIds);

    /** 保存民宿-餐饮关联 */
    void saveRestaurant(Long homestayId, Long merchantId, List<Long> restaurantIds);

    /** 查询民宿已关联的各资源 id 列表(编辑回显) */
    HomestayLinksVO linkIds(Long homestayId);
}
