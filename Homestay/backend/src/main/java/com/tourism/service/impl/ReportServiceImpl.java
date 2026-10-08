package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.BizException;
import com.tourism.common.Constants;
import com.tourism.common.PageVO;
import com.tourism.common.ResultCode;
import com.tourism.dto.ReportCreateDTO;
import com.tourism.entity.Homestay;
import com.tourism.entity.Report;
import com.tourism.entity.Restaurant;
import com.tourism.entity.Review;
import com.tourism.entity.TourismExperience;
import com.tourism.mapper.HomestayMapper;
import com.tourism.mapper.ReportMapper;
import com.tourism.mapper.RestaurantMapper;
import com.tourism.mapper.ReviewMapper;
import com.tourism.mapper.TourismExperienceMapper;
import com.tourism.service.ReportService;
import com.tourism.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 举报服务实现
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final HomestayMapper homestayMapper;
    private final TourismExperienceMapper experienceMapper;
    private final RestaurantMapper restaurantMapper;
    private final ReviewMapper reviewMapper;
    private final ReviewService reviewService;

    @Override
    @Transactional
    public void create(ReportCreateDTO dto, Long reporterId) {
        // 校验目标存在
        requireTarget(dto.getReportType(), dto.getTargetId());

        // 同一举报人对同一目标,存在未处理记录则不可重复举报
        Long pending = reportMapper.selectCount(new LambdaQueryWrapper<Report>()
                .eq(Report::getReporterId, reporterId)
                .eq(Report::getReportType, dto.getReportType())
                .eq(Report::getTargetId, dto.getTargetId())
                .eq(Report::getStatus, Constants.REPORT_PENDING));
        if (pending > 0) {
            throw new BizException(ResultCode.CONFLICT, "您已举报过该内容,平台正在处理中");
        }

        Report report = new Report();
        report.setReportType(dto.getReportType());
        report.setTargetId(dto.getTargetId());
        report.setReason(dto.getReason());
        report.setDescription(dto.getDescription());
        report.setReporterId(reporterId);
        report.setStatus(Constants.REPORT_PENDING);
        reportMapper.insert(report);
    }

    @Override
    public PageVO<Report> pageByUser(Long userId, int current, int size) {
        Page<Report> page = reportMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Report>()
                        .eq(Report::getReporterId, userId)
                        .orderByDesc(Report::getCreateTime));
        return PageVO.of(page);
    }

    @Override
    public PageVO<Report> pageForAdmin(int current, int size, String status, Integer reportType) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            wrapper.eq(Report::getStatus, status);
        }
        if (reportType != null) {
            wrapper.eq(Report::getReportType, reportType);
        }
        wrapper.orderByDesc(Report::getCreateTime);
        return PageVO.of(reportMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @Override
    @Transactional
    public void handle(Long reportId, String status, String handleRemark) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BizException(ResultCode.NOT_FOUND, "举报不存在");
        }
        if (!List.of(Constants.REPORT_HANDLING, Constants.REPORT_HANDLED, Constants.REPORT_REJECTED).contains(status)) {
            throw new BizException("处理状态不正确");
        }
        report.setStatus(status);
        report.setHandleRemark(handleRemark);
        report.setHandleTime(LocalDateTime.now());
        reportMapper.updateById(report);

        // 若被举报对象是评价且处理为"已处理",联动隐藏该评价并重算民宿评分
        if (Constants.REPORT_HANDLED.equals(status) && report.getReportType() == Constants.REPORT_REVIEW) {
            reviewService.hideByAdmin(report.getTargetId());
        }
    }

    private void requireTarget(int type, Long targetId) {
        switch (type) {
            case Constants.REPORT_HOMESTAY -> {
                Homestay h = homestayMapper.selectById(targetId);
                if (h == null) {
                    throw new BizException("被举报民宿不存在");
                }
            }
            case Constants.REPORT_REVIEW -> {
                if (reviewMapper.selectById(targetId) == null) {
                    throw new BizException("被举报评价不存在");
                }
            }
            case Constants.TARGET_EXPERIENCE -> {
                if (experienceMapper.selectById(targetId) == null) {
                    throw new BizException("被举报体验不存在");
                }
            }
            case Constants.TARGET_RESTAURANT -> {
                if (restaurantMapper.selectById(targetId) == null) {
                    throw new BizException("被举报餐饮不存在");
                }
            }
            default -> throw new BizException("举报类型不正确");
        }
    }
}
