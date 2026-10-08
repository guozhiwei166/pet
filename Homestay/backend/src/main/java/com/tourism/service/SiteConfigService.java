package com.tourism.service;

import com.tourism.entity.Banner;

import java.util.List;

/**
 * 站点配置服务(首页轮播/公告)
 */
public interface SiteConfigService {

    List<Banner> banners();

    /** 站点公告文案 */
    String notice();
}
