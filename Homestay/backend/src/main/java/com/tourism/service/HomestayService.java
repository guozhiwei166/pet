package com.tourism.service;

import com.tourism.common.PageVO;
import com.tourism.dto.HomestayQueryDTO;
import com.tourism.dto.HomestaySaveDTO;
import com.tourism.entity.Homestay;
import com.tourism.vo.HomestayVO;

import java.util.List;

/**
 * 民宿服务
 */
public interface HomestayService {

    /** 游客分页浏览(仅已通过+营业中) */
    PageVO<HomestayVO> page(HomestayQueryDTO query);

    /** 首页推荐/精选(游客) */
    List<HomestayVO> recommendList(int limit, Long destinationId);

    /** 详情(游客可看已上架;商家/管理员可看自有/全部) */
    HomestayVO detail(Long id);

    /** 浏览量自增(详情页调用) */
    void incrView(Long id);

    /** 关联民宿下的所有民宿(目的地维度,供旅行计划/推荐) */
    List<Homestay> listByDestination(Long destinationId);

    // ============ 商家侧 ============

    /** 商家查询自己的民宿列表 */
    List<HomestayVO> listByMerchant(Long merchantId);

    /** 商家新增民宿(进入待审核) */
    HomestayVO create(HomestaySaveDTO dto, Long merchantId);

    /** 商家编辑民宿(仅 OWN 且 APPROVED/PENDING/REJECTED 可编辑;OFFLINE 亦应可改后重新提交) */
    HomestayVO update(HomestaySaveDTO dto, Long merchantId);

    /** 商家下架/上架(仅 OWN) */
    void changeStatus(Long id, Long merchantId, String auditStatus);

    /** 删除民宿(仅 OWN,若已产生订单则禁止) */
    void delete(Long id, Long merchantId);

    /** 实体转 VO(含图集/目的地名/商家名),供其它服务复用 */
    HomestayVO toVO(Homestay homestay);
}
