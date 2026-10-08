package com.tourism.controller;

import com.tourism.common.PageVO;
import com.tourism.common.Result;
import com.tourism.dto.ResourceQueryDTO;
import com.tourism.dto.ResourceSaveDTO;
import com.tourism.entity.Destination;
import com.tourism.entity.Restaurant;
import com.tourism.entity.ScenicSpot;
import com.tourism.entity.TourismExperience;
import com.tourism.security.RequireRole;
import com.tourism.service.AdminResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员-旅游资源与目的地管理接口
 */
@RestController
@RequestMapping("/api/admin/resource")
@RequireRole("ADMIN")
@RequiredArgsConstructor
public class AdminResourceController {

    private final AdminResourceService adminResourceService;

    // ===== 目的地 =====
    @GetMapping("/destination/page")
    public Result<PageVO<Destination>> destinationPage(@RequestParam(defaultValue = "1") int current,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(required = false) String keyword) {
        return Result.success(adminResourceService.pageDestinations(current, size, keyword));
    }

    @PostMapping("/destination/save")
    public Result<Destination> saveDestination(@Valid @RequestBody ResourceSaveDTO dto) {
        dto.setType(4);
        return Result.success(adminResourceService.saveDestination(dto));
    }

    @DeleteMapping("/destination/{id}")
    public Result<Void> deleteDestination(@PathVariable Long id) {
        adminResourceService.deleteDestination(id);
        return Result.success();
    }

    // ===== 景点 =====
    @GetMapping("/scenic/page")
    public Result<PageVO<ScenicSpot>> scenicPage(ResourceQueryDTO query) {
        return Result.success(adminResourceService.pageScenic(query, true));
    }

    @PostMapping("/scenic/save")
    public Result<ScenicSpot> saveScenic(@Valid @RequestBody ResourceSaveDTO dto) {
        dto.setType(1);
        return Result.success(adminResourceService.saveScenic(dto));
    }

    @PutMapping("/scenic/{id}/status")
    public Result<Void> changeScenicStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminResourceService.changeScenicStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/scenic/{id}")
    public Result<Void> deleteScenic(@PathVariable Long id) {
        adminResourceService.deleteScenic(id);
        return Result.success();
    }

    // ===== 体验 =====
    @GetMapping("/experience/page")
    public Result<PageVO<TourismExperience>> experiencePage(ResourceQueryDTO query) {
        return Result.success(adminResourceService.pageExperience(query, true));
    }

    @PostMapping("/experience/save")
    public Result<TourismExperience> saveExperience(@Valid @RequestBody ResourceSaveDTO dto) {
        dto.setType(2);
        return Result.success(adminResourceService.saveExperience(dto));
    }

    @PutMapping("/experience/{id}/status")
    public Result<Void> changeExperienceStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminResourceService.changeExperienceStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/experience/{id}")
    public Result<Void> deleteExperience(@PathVariable Long id) {
        adminResourceService.deleteExperience(id);
        return Result.success();
    }

    // ===== 餐饮 =====
    @GetMapping("/restaurant/page")
    public Result<PageVO<Restaurant>> restaurantPage(ResourceQueryDTO query) {
        return Result.success(adminResourceService.pageRestaurant(query, true));
    }

    @PostMapping("/restaurant/save")
    public Result<Restaurant> saveRestaurant(@Valid @RequestBody ResourceSaveDTO dto) {
        dto.setType(3);
        return Result.success(adminResourceService.saveRestaurant(dto));
    }

    @PutMapping("/restaurant/{id}/status")
    public Result<Void> changeRestaurantStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminResourceService.changeRestaurantStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/restaurant/{id}")
    public Result<Void> deleteRestaurant(@PathVariable Long id) {
        adminResourceService.deleteRestaurant(id);
        return Result.success();
    }
}
