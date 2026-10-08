package com.tourism.controller;

import com.tourism.common.PageVO;
import com.tourism.common.Result;
import com.tourism.dto.ReportCreateDTO;
import com.tourism.entity.Report;
import com.tourism.security.LoginUser;
import com.tourism.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 举报接口(登录用户)
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** 提交举报 */
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody ReportCreateDTO dto) {
        reportService.create(dto, LoginUser.userId());
        return Result.success();
    }

    /** 我的举报 */
    @GetMapping("/my")
    public Result<PageVO<Report>> my(@RequestParam(defaultValue = "1") int current,
                                     @RequestParam(defaultValue = "10") int size) {
        return Result.success(reportService.pageByUser(LoginUser.userId(), current, size));
    }
}
