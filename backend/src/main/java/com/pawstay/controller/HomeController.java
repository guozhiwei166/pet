package com.pawstay.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pawstay.common.Result;
import com.pawstay.entity.Merchant;
import com.pawstay.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 用户端首页：商家浏览与搜索（公开接口） */
@RestController
@RequestMapping("/api/home")
public class HomeController {

    @Autowired
    private MerchantService merchantService;

    @GetMapping
    public Result<IPage<Merchant>> page(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "10") long size,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String species) {
        return Result.ok(merchantService.pageHome(page, size, keyword, species));
    }
}
