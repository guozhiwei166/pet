package com.tourism.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 旅行计划详情 VO(含按天分组的行程)
 */
@Data
public class TravelPlanDetailVO {

    private Long id;
    private String planName;
    private Long destinationId;
    private String destinationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String intro;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 按天分组的行程:day -> items */
    private List<DayGroup> days;

    @Data
    public static class DayGroup {
        private Integer day;
        private List<ItemVO> items;
    }

    @Data
    public static class ItemVO {
        private Long id;
        private Integer dayNumber;
        private String itemType;
        private String itemTypeText;
        private Long itemId;
        private String itemName;
        private String coverImage;
        private String timeText;    // 09:00-11:00 或空
        private String remark;
        private Integer sortOrder;
        private String address;
        /** 民宿:价格;其它:人均/门票 */
        private String priceText;
    }
}
