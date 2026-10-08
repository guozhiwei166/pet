package com.tourism.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家入驻注册请求(账号信息 + 商家资料)
 */
@Data
public class RegisterMerchantDTO {

    @Valid
    private RegisterDTO user;

    @NotBlank(message = "商家名称不能为空")
    @Size(max = 100, message = "商家名称过长")
    private String merchantName;

    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    @Size(max = 1000, message = "商家简介过长")
    private String description;
}
