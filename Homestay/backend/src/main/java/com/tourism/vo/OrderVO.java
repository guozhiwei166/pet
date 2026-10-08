package com.tourism.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单 VO(展示给前端,含快照字段与状态)
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long userId;
    /** 下单用户昵称/头像(商家/管理员查看客户信息用) */
    private String userNickname;
    private String userAvatar;
    private Long homestayId;
    private Long roomTypeId;
    private String homestayName;
    private String homestayCover;
    private String roomTypeName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer nights;
    private Integer roomCount;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private String contactName;
    private String contactPhone;
    private String guestNames;
    private String status;
    private String statusText;
    private String payStatus;
    private String payType;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOutTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
