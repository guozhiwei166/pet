package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.dto.ResourceLinkDTO;
import com.tourism.security.LoginUser;
import com.tourism.security.Public;
import com.tourism.service.HomestayResourceService;
import com.tourism.service.MerchantService;
import com.tourism.vo.HomestayLinksVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 民宿周边资源关联(创新点①):商家维护 + 游客查询
 */
@RestController
@RequestMapping("/api/homestay-resource")
@RequiredArgsConstructor
public class HomestayResourceController {

    private final HomestayResourceService homestayResourceService;
    private final MerchantService merchantService;

    // ==================== 商家维护 ====================

    @PutMapping("/scenic")
    public Result<Void> saveScenic(@Valid @RequestBody ResourceLinkDTO dto) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        homestayResourceService.saveScenic(dto.getHomestayId(), merchantService.currentMerchant().getId(), dto.getTargetIds());
        return Result.success();
    }

    @PutMapping("/experience")
    public Result<Void> saveExperience(@Valid @RequestBody ResourceLinkDTO dto) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        homestayResourceService.saveExperience(dto.getHomestayId(), merchantService.currentMerchant().getId(), dto.getTargetIds());
        return Result.success();
    }

    @PutMapping("/restaurant")
    public Result<Void> saveRestaurant(@Valid @RequestBody ResourceLinkDTO dto) {
        LoginUser.requireRole("MERCHANT");
        merchantService.requireApproved();
        homestayResourceService.saveRestaurant(dto.getHomestayId(), merchantService.currentMerchant().getId(), dto.getTargetIds());
        return Result.success();
    }

    /** 已关联资源 id(民宿编辑页回显,游客无需) */
    @Public
    @GetMapping("/{homestayId}")
    public Result<HomestayLinksVO> links(@PathVariable Long homestayId) {
        return Result.success(homestayResourceService.linkIds(homestayId));
    }
}
