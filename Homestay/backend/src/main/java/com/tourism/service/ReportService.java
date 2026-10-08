package com.tourism.service;

import com.tourism.common.PageVO;
import com.tourism.dto.ReportCreateDTO;
import com.tourism.entity.Report;

/**
 * 举报服务
 */
public interface ReportService {

    /** 提交举报(校验目标存在;同一人对同一目标未处理前不可重复举报) */
    void create(ReportCreateDTO dto, Long reporterId);

    /** 我的举报(用户) */
    PageVO<Report> pageByUser(Long userId, int current, int size);

    /** 管理员分页(按状态/类型) */
    PageVO<Report> pageForAdmin(int current, int size, String status, Integer reportType);

    /** 管理员处理:状态 + 备注(可联动隐藏被举报评价) */
    void handle(Long reportId, String status, String handleRemark);
}
