package com.tourism.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商家公开主页 VO(访客可见,含在营民宿)
 */
@Data
public class MerchantPublicVO {

    private Long id;
    /** 商家主体名称 */
    private String merchantName;
    /** 掌柜昵称/头像(取自绑定用户) */
    private String nickname;
    private String avatar;
    private String contactName;
    private String contactPhone;
    private String licenseNo;
    private String description;
    /** PENDING / APPROVED / REJECTED / DISABLED */
    private String status;
    /** 名下在营民宿数 */
    private Long homestayCount;
    /** 名下在营民宿(简要) */
    private List<HomestayItem> homestays;

    @Data
    public static class HomestayItem {
        private Long id;
        private String name;
        private String coverImage;
        private String destinationName;
        private String address;
        private String intro;
        private String features;
        private BigDecimal rating;
        private Integer ratingCount;
        private BigDecimal minPrice;
    }
}
