package com.pawstay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pawstay.entity.Merchant;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface MerchantMapper extends BaseMapper<Merchant> {

    /**
     * 首页商家列表：仅已审核通过且营业中的商家，联查最低在售价，按评分与接单量排序
     */
    @Select("<script>" +
            "SELECT m.*, IFNULL((SELECT MIN(r.price_per_day) FROM pet_room r WHERE r.merchant_id = m.id AND r.status = 1), 0) AS min_price " +
            "FROM pet_merchant m WHERE m.audit_status = 'APPROVED' AND m.open_status = 1" +
            "<if test='kw != null and kw != \"\"'> AND m.shop_name LIKE CONCAT('%', #{kw}, '%')</if>" +
            "<if test='sp != null and sp != \"\"'> AND m.service_species LIKE CONCAT('%', #{sp}, '%')</if>" +
            " ORDER BY m.rating DESC, m.boarding_count DESC" +
            "</script>")
    IPage<Merchant> pageHome(Page<Merchant> page, @Param("kw") String kw, @Param("sp") String sp);
}
