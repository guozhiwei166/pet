package com.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家资料更新请求
 */
@Data
public class MerchantProfileDTO {

    @NotBlank(message = "商家名称不能为空")
    @Size(max = 100)
    private String merchantName;

    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    @Size(max = 1000)
    private String description;
}
