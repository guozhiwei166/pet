package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.entity.Banner;
import com.tourism.entity.SysSetting;
import com.tourism.mapper.BannerMapper;
import com.tourism.mapper.SysSettingMapper;
import com.tourism.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 站点配置服务实现
 */
@Service
@RequiredArgsConstructor
public class SiteConfigServiceImpl implements SiteConfigService {

    private static final String KEY_NOTICE = "platform_notice";

    private final BannerMapper bannerMapper;
    private final SysSettingMapper sysSettingMapper;

    @Override
    public List<Banner> banners() {
        return bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSortOrder));
    }

    @Override
    public String notice() {
        SysSetting s = sysSettingMapper.selectOne(new LambdaQueryWrapper<SysSetting>()
                .eq(SysSetting::getSettingKey, KEY_NOTICE)
                .last("limit 1"));
        return s == null ? null : s.getSettingValue();
    }
}
