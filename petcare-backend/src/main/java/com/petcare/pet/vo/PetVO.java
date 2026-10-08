package com.petcare.pet.vo;

import com.petcare.pet.entity.Pet;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class PetVO extends Pet {
    private Integer age;            // 岁（按生日计算，不足一年按 0）
    private List<String> tags;      // 性格标签名
    private List<MapVaccine> vaccines;

    @Data
    public static class MapVaccine {
        private Long id;
        private String vaccineName;
        private String inoculationDate;
        private String expireDate;
        private String org;
        private String remark;
        private String status;      // 有效 / 即将到期 / 已过期
    }
}
