package com.pawstay.controller;

import com.pawstay.common.Result;
import com.pawstay.dto.DiaryDTO;
import com.pawstay.entity.CareRecord;
import com.pawstay.service.DiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 寄养日记：商家每日打卡，主人查看 */
@RestController
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @PostMapping("/api/orders/{id}/diary")
    public Result<Void> save(@PathVariable Long id, @RequestBody DiaryDTO dto) {
        diaryService.save(id, dto);
        return Result.ok();
    }

    @GetMapping("/api/orders/{id}/diary")
    public Result<List<CareRecord>> list(@PathVariable Long id) {
        return Result.ok(diaryService.listByOrder(id));
    }
}
