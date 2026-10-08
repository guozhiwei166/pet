package com.pawstay.controller;

import com.pawstay.common.Result;
import com.pawstay.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 商家公开信息：详情含在售房型与评价 */
@RestController
public class MerchantPublicController {

    @Autowired
    private MerchantService merchantService;

    @GetMapping("/api/merchants/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.ok(merchantService.detail(id));
    }
}
