package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.security.Public;
import com.tourism.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页站点配置(游客公开)
 */
@RestController
@RequestMapping("/api/site")
@RequiredArgsConstructor
public class SiteConfigController {

    private final SiteConfigService siteConfigService;

    @Public
    @GetMapping("/home")
    public Result<Map<String, Object>> home() {
        Map<String, Object> data = new HashMap<>();
        data.put("banners", siteConfigService.banners());
        data.put("notice", siteConfigService.notice());
        return Result.success(data);
    }
}
