package com.tourism.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量新增房间请求(商家)
 */
@Data
public class HotelRoomCreateDTO {

    @NotNull(message = "缺少民宿")
    private Long homestayId;

    @NotNull(message = "缺少房型")
    private Long roomTypeId;

    /** 房间号列表,如 ["201","202"] */
    @NotEmpty(message = "请填写房间号")
    private List<String> roomNumbers;

    private String floor;
    private String remark;
}
