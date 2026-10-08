package com.tourism.service;

import com.tourism.entity.Destination;

import java.util.List;

/**
 * 目的地服务
 */
public interface DestinationService {

    /** 展示中的目的地列表(游客可看) */
    List<Destination> listShown();

    /** 热门目的地(有上架民宿/资源优先) */
    List<Destination> hotDestinations(int limit);

    /** 详情(含 民宿数/景点数/体验数/餐饮数) */
    Destination detail(Long id);
}
