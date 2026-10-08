package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.BizException;
import com.tourism.common.Constants;
import com.tourism.common.PageVO;
import com.tourism.common.ResultCode;
import com.tourism.dto.HomestayQueryDTO;
import com.tourism.dto.HomestaySaveDTO;
import com.tourism.entity.Destination;
import com.tourism.entity.Homestay;
import com.tourism.entity.HomestayImage;
import com.tourism.entity.Merchant;
import com.tourism.entity.Orders;
import com.tourism.entity.RoomType;
import com.tourism.mapper.DestinationMapper;
import com.tourism.mapper.HomestayImageMapper;
import com.tourism.mapper.HomestayMapper;
import com.tourism.mapper.MerchantMapper;
import com.tourism.mapper.OrdersMapper;
import com.tourism.mapper.RoomTypeMapper;
import com.tourism.service.HomestayService;
import com.tourism.vo.HomestayVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 民宿服务实现
 */
@Service
@RequiredArgsConstructor
public class HomestayServiceImpl implements HomestayService {

    private final HomestayMapper homestayMapper;
    private final HomestayImageMapper homestayImageMapper;
    private final DestinationMapper destinationMapper;
    private final MerchantMapper merchantMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final OrdersMapper ordersMapper;

    // ==================== 游客浏览 ====================

    @Override
    public PageVO<HomestayVO> page(HomestayQueryDTO query) {
        LambdaQueryWrapper<Homestay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Homestay::getAuditStatus, Constants.AUDIT_APPROVED)
                .eq(Homestay::getBusinessStatus, Constants.BIZ_OPEN)
                .eq(Homestay::getDeleted, 0);

        if (query.getDestinationId() != null) {
            wrapper.eq(Homestay::getDestinationId, query.getDestinationId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Homestay::getName, kw)
                    .or().like(Homestay::getIntro, kw)
                    .or().like(Homestay::getAddress, kw)
                    .or().like(Homestay::getFeatures, kw));
        }
        if (query.getMinPrice() != null) {
            wrapper.ge(Homestay::getMinPrice, query.getMinPrice());
        }
        if (query.getMaxPrice() != null) {
            wrapper.le(Homestay::getMinPrice, query.getMaxPrice());
        }
        if (StringUtils.hasText(query.getFeatures())) {
            wrapper.like(Homestay::getFeatures, query.getFeatures().trim());
        }

        // 排序:默认推荐 -> 交给 RecommendService 综合打分,此处先按评分/浏览兜底;sort=recommend 时由调用方统一推荐
        Page<Homestay> page = new Page<>(query.getCurrent(), query.getSize());
        switch (query.getSort() == null ? "recommend" : query.getSort()) {
            case "rating" -> wrapper.orderByDesc(Homestay::getRating);
            case "price_asc" -> wrapper.orderByAsc(Homestay::getMinPrice);
            case "price_desc" -> wrapper.orderByDesc(Homestay::getMinPrice);
            case "newest" -> wrapper.orderByDesc(Homestay::getCreateTime);
            default -> wrapper.orderByDesc(Homestay::getRating, Homestay::getViewCount);
        }
        Page<Homestay> result = homestayMapper.selectPage(page, wrapper);
        return PageVO.of(result.convert(this::toVOWithoutFavorite));
    }

    @Override
    public List<HomestayVO> recommendList(int limit, Long destinationId) {
        LambdaQueryWrapper<Homestay> wrapper = new LambdaQueryWrapper<Homestay>()
                .eq(Homestay::getAuditStatus, Constants.AUDIT_APPROVED)
                .eq(Homestay::getBusinessStatus, Constants.BIZ_OPEN)
                .eq(Homestay::getDeleted, 0);
        if (destinationId != null) {
            wrapper.eq(Homestay::getDestinationId, destinationId);
        }
        wrapper.orderByDesc(Homestay::getRating)
                .orderByDesc(Homestay::getViewCount)
                .orderByDesc(Homestay::getFavoriteCount)
                .last("limit " + limit);
        return homestayMapper.selectList(wrapper).stream().map(this::toVOWithoutFavorite).toList();
    }

    @Override
    public HomestayVO detail(Long id) {
        Homestay h = homestayMapper.selectById(id);
        if (h == null) {
            throw new BizException(ResultCode.NOT_FOUND, "民宿不存在");
        }
        // 访问控制(未上架的民宿仅商家本人/管理员可看)由 controller 依据角色判断后再调用
        return toVOWithoutFavorite(h);
    }

    @Override
    public void incrView(Long id) {
        homestayMapper.incrementView(id);
    }

    @Override
    public List<Homestay> listByDestination(Long destinationId) {
        return homestayMapper.selectList(new LambdaQueryWrapper<Homestay>()
                .eq(Homestay::getDestinationId, destinationId)
                .eq(Homestay::getAuditStatus, Constants.AUDIT_APPROVED)
                .eq(Homestay::getBusinessStatus, Constants.BIZ_OPEN)
                .eq(Homestay::getDeleted, 0));
    }

    // ==================== 商家侧 ====================

    @Override
    public List<HomestayVO> listByMerchant(Long merchantId) {
        List<Homestay> list = homestayMapper.selectList(new LambdaQueryWrapper<Homestay>()
                .eq(Homestay::getMerchantId, merchantId)
                .eq(Homestay::getDeleted, 0)
                .orderByDesc(Homestay::getCreateTime));
        return list.stream().map(h -> {
            HomestayVO vo = toVOWithoutFavorite(h);
            // 商家视角摘要:房型数 + 近30天有效订单数
            vo.setRoomTypeCount(Math.toIntExact(roomTypeMapper.selectCount(new LambdaQueryWrapper<RoomType>()
                    .eq(RoomType::getHomestayId, h.getId()))));
            vo.setMonthOrderCount(ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                    .eq(Orders::getHomestayId, h.getId())
                    .ge(Orders::getCreateTime, java.time.LocalDateTime.now().minusDays(30))
                    .ne(Orders::getStatus, Constants.ORDER_CANCELLED)));
            return vo;
        }).toList();
    }

    @Override
    @Transactional
    public HomestayVO create(HomestaySaveDTO dto, Long merchantId) {
        // 校验商家资质(merchantId 为 merchant 表主键)
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null || !Constants.AUDIT_APPROVED.equals(merchant.getStatus())) {
            throw new BizException(ResultCode.FORBIDDEN, "商家尚未通过审核,无法发布民宿");
        }
        if (destinationMapper.selectById(dto.getDestinationId()) == null) {
            throw new BizException("所选目的地不存在");
        }

        Homestay h = new Homestay();
        copyToEntity(dto, h);
        h.setMerchantId(merchant.getId());
        h.setAuditStatus(Constants.AUDIT_PENDING);
        h.setRating(new BigDecimal("5.0"));
        h.setRatingCount(0);
        h.setFavoriteCount(0);
        h.setViewCount(0);
        homestayMapper.insert(h);

        refreshImages(h.getId(), dto.getImages());
        refreshMinPrice(h.getId());
        return toVOWithoutFavorite(homestayMapper.selectById(h.getId()));
    }

    @Override
    @Transactional
    public HomestayVO update(HomestaySaveDTO dto, Long merchantId) {
        Homestay h = homestayMapper.selectById(dto.getId());
        if (h == null) {
            throw new BizException(ResultCode.NOT_FOUND, "民宿不存在");
        }
        requireOwner(h, merchantId);

        copyToEntity(dto, h);
        // 修改后回到待审核(已上架民宿改资料需重新审核;下架/驳回/待审核可直接存)
        if (Constants.AUDIT_APPROVED.equals(h.getAuditStatus())) {
            h.setAuditStatus(Constants.AUDIT_PENDING);
            h.setAuditTime(null);
            h.setAuditRemark(null);
        }
        homestayMapper.updateById(h);

        if (dto.getImages() != null) {
            refreshImages(h.getId(), dto.getImages());
        }
        refreshMinPrice(h.getId());
        return toVOWithoutFavorite(homestayMapper.selectById(h.getId()));
    }

    @Override
    public void changeStatus(Long id, Long merchantId, String auditStatus) {
        Homestay h = homestayMapper.selectById(id);
        requireOwner(h, merchantId);
        if (Constants.AUDIT_OFFLINE.equals(auditStatus)) {
            h.setAuditStatus(Constants.AUDIT_OFFLINE);
        } else if (Constants.AUDIT_PENDING.equals(auditStatus)) {
            // 下架后重新上架提交审核
            h.setAuditStatus(Constants.AUDIT_PENDING);
            h.setAuditTime(null);
            h.setAuditRemark(null);
        } else {
            throw new BizException("仅支持下架或重新提交审核");
        }
        homestayMapper.updateById(h);
    }

    @Override
    public void delete(Long id, Long merchantId) {
        Homestay h = homestayMapper.selectById(id);
        requireOwner(h, merchantId);
        // 存在订单则禁止删除(业务边界)
        Long orderCount = ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getHomestayId, id)
                .in(Orders::getStatus, Constants.ORDER_PENDING, Constants.ORDER_PAID,
                        Constants.ORDER_ARRIVED, Constants.ORDER_CHECKED_IN, Constants.ORDER_COMPLETED));
        if (orderCount > 0) {
            throw new BizException("该民宿已有订单记录,无法删除,可改为下架");
        }
        // 级联软删房型与关联
        homestayMapper.deleteById(id);
        roomTypeMapper.delete(new LambdaQueryWrapper<RoomType>().eq(RoomType::getHomestayId, id));
    }

    private void requireOwner(Homestay h, Long merchantId) {
        if (h.getMerchantId() == null || !Objects.equals(h.getMerchantId(), merchantId)) {
            throw new BizException(ResultCode.FORBIDDEN, "无权操作该民宿");
        }
    }

    private void copyToEntity(HomestaySaveDTO dto, Homestay h) {
        // id/merchantId 由调用方控制;images 列表走关联表;其余基础字段(含封面)拷贝
        BeanUtils.copyProperties(dto, h, "id", "merchantId", "images");
    }

    private void refreshImages(Long homestayId, List<String> urls) {
        homestayImageMapper.delete(new LambdaQueryWrapper<HomestayImage>()
                .eq(HomestayImage::getHomestayId, homestayId));
        if (urls == null || urls.isEmpty()) {
            return;
        }
        int order = 0;
        for (String url : urls) {
            if (!StringUtils.hasText(url)) {
                continue;
            }
            HomestayImage img = new HomestayImage();
            img.setHomestayId(homestayId);
            img.setUrl(url.trim());
            img.setSortOrder(order++);
            homestayImageMapper.insert(img);
        }
    }

    private void refreshMinPrice(Long homestayId) {
        List<RoomType> roomTypes = roomTypeMapper.selectList(new LambdaQueryWrapper<RoomType>()
                .eq(RoomType::getHomestayId, homestayId)
                .eq(RoomType::getStatus, Constants.ROOM_ON));
        BigDecimal min = roomTypes.stream().map(RoomType::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        Homestay h = new Homestay();
        h.setId(homestayId);
        h.setMinPrice(min);
        homestayMapper.updateById(h);
    }

    // ==================== VO 转换 ====================

    private HomestayVO toVOWithoutFavorite(Homestay h) {
        HomestayVO vo = new HomestayVO();
        BeanUtils.copyProperties(h, vo);
        List<HomestayImage> images = homestayImageMapper.selectList(new LambdaQueryWrapper<HomestayImage>()
                .eq(HomestayImage::getHomestayId, h.getId())
                .orderByAsc(HomestayImage::getSortOrder));
        vo.setImages(images.stream().map(HomestayImage::getUrl).toList());
        Destination d = destinationMapper.selectById(h.getDestinationId());
        if (d != null) {
            vo.setDestinationName(d.getName());
        }
        Merchant m = merchantMapper.selectById(h.getMerchantId());
        if (m != null) {
            vo.setMerchantName(m.getMerchantName());
        }
        return vo;
    }

    // 供订单/收藏等其它服务使用的内部访问(非接口方法,依赖注入本类时可用)
    public Homestay requireApproved(Long id) {
        Homestay h = homestayMapper.selectById(id);
        if (h == null || !Constants.AUDIT_APPROVED.equals(h.getAuditStatus())) {
            throw new BizException("民宿不存在或未上架");
        }
        return h;
    }

    @Override
    public HomestayVO toVO(Homestay h) {
        return toVOWithoutFavorite(h);
    }
}
