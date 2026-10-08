package com.tourism.controller;

import com.tourism.common.PageVO;
import com.tourism.common.Result;
import com.tourism.dto.TravelPlanDTO;
import com.tourism.dto.TravelPlanItemDTO;
import com.tourism.entity.TravelPlan;
import com.tourism.security.LoginUser;
import com.tourism.service.TravelPlanService;
import com.tourism.vo.TravelPlanDetailVO;
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
 * 个人旅行计划接口(仅登录用户)
 */
@RestController
@RequestMapping("/api/travel-plan")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    /** 我的计划列表 */
    @GetMapping("/my")
    public Result<PageVO<TravelPlan>> my(@RequestParam(defaultValue = "1") int current,
                                         @RequestParam(defaultValue = "6") int size) {
        return Result.success(travelPlanService.pageByUser(LoginUser.userId(), current, size));
    }

    /** 新建计划 */
    @PostMapping("/create")
    public Result<TravelPlan> create(@Valid @RequestBody TravelPlanDTO dto) {
        return Result.success(travelPlanService.create(LoginUser.userId(), dto));
    }

    /** 编辑计划 */
    @PutMapping("/update")
    public Result<TravelPlan> update(@Valid @RequestBody TravelPlanDTO dto) {
        return Result.success(travelPlanService.update(LoginUser.userId(), dto));
    }

    /** 计划详情(按天) */
    @GetMapping("/{id}")
    public Result<TravelPlanDetailVO> detail(@PathVariable Long id) {
        return Result.success(travelPlanService.detail(LoginUser.userId(), id));
    }

    /** 删除计划 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        travelPlanService.delete(LoginUser.userId(), id);
        return Result.success();
    }

    /** 添加行程项目 */
    @PostMapping("/item")
    public Result<TravelPlanDetailVO> addItem(@Valid @RequestBody TravelPlanItemDTO dto) {
        return Result.success(travelPlanService.addItem(LoginUser.userId(), dto));
    }

    /** 删除行程项目 */
    @DeleteMapping("/item/{itemId}")
    public Result<TravelPlanDetailVO> removeItem(@PathVariable Long itemId) {
        return Result.success(travelPlanService.removeItem(LoginUser.userId(), itemId));
    }

    /** 调整项目(天数/顺序) */
    @PutMapping("/item/{itemId}/move")
    public Result<TravelPlanDetailVO> move(@PathVariable Long itemId,
                                           @RequestParam(required = false) Integer day,
                                           @RequestParam(required = false) Integer sort) {
        return Result.success(travelPlanService.moveItem(LoginUser.userId(), itemId, day, sort));
    }

    /** 更新计划状态(进行中/完成/取消) */
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam String status) {
        travelPlanService.changeStatus(LoginUser.userId(), id, status);
        return Result.success();
    }
}
