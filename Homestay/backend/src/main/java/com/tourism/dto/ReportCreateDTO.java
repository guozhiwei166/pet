package com.tourism.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交举报请求(用户)
 */
@Data
public class ReportCreateDTO {

    /** 1=民宿 2=评价 3=体验 4=餐饮 */
    @NotNull(message = "缺少举报类型")
    private Integer reportType;

    @NotNull(message = "缺少举报对象")
    private Long targetId;

    /** 虚假信息/违规内容/价格问题/服务问题/其他 */
    @NotBlank(message = "请选择举报原因")
    private String reason;

    @Size(max = 500, message = "举报说明不能超过 500 字")
    private String description;
}
