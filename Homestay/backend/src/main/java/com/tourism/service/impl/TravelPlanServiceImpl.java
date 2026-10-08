package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.BizException;
import com.tourism.common.Constants;
import com.tourism.common.PageVO;
import com.tourism.common.ResultCode;
import com.tourism.dto.TravelPlanDTO;
import com.tourism.dto.TravelPlanItemDTO;
import com.tourism.entity.Destination;
import com.tourism.entity.Homestay;
import com.tourism.entity.Restaurant;
import com.tourism.entity.RoomType;
import com.tourism.entity.ScenicSpot;
import com.tourism.entity.TourismExperience;
import com.tourism.entity.TravelPlan;
import com.tourism.entity.TravelPlanItem;
import com.tourism.mapper.DestinationMapper;
import com.tourism.mapper.HomestayMapper;
import com.tourism.mapper.RestaurantMapper;
import com.tourism.mapper.RoomTypeMapper;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.mapper.TourismExperienceMapper;
import com.tourism.mapper.TravelPlanItemMapper;
import com.tourism.mapper.TravelPlanMapper;
import com.tourism.service.TravelPlanService;
import com.tourism.vo.TravelPlanDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 个人旅行计划服务实现
 */
@Service
@RequiredArgsConstructor
public class TravelPlanServiceImpl implements TravelPlanService {

    private static final Map<String, String> TYPE_TEXT = Map.of(
            Constants.ITEM_HOMESTAY, "民宿",
            Constants.ITEM_SCENIC, "景点",
            Constants.ITEM_EXPERIENCE, "体验",
            Constants.ITEM_RESTAURANT, "餐饮"
    );

    private final TravelPlanMapper travelPlanMapper;
    private final TravelPlanItemMapper travelPlanItemMapper;
    private final DestinationMapper destinationMapper;
    private final HomestayMapper homestayMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final ScenicSpotMapper scenicSpotMapper;
    private final TourismExperienceMapper experienceMapper;
    private final RestaurantMapper restaurantMapper;

    @Override
    public PageVO<TravelPlan> pageByUser(Long userId, int current, int size) {
        Page<TravelPlan> page = travelPlanMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<TravelPlan>()
                        .eq(TravelPlan::getUserId, userId)
                        .orderByDesc(TravelPlan::getCreateTime));
        return PageVO.of(page);
    }

    @Override
    public TravelPlan create(Long userId, TravelPlanDTO dto) {
        Destination d = destinationMapper.selectById(dto.getDestinationId());
        if (d == null) {
            throw new BizException("目的地不存在");
        }
        TravelPlan plan = new TravelPlan();
        plan.setUserId(userId);
        plan.setPlanName(dto.getPlanName());
        plan.setDestinationId(d.getId());
        plan.setDestinationName(d.getName());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setIntro(dto.getIntro());
        validateDateRange(dto.getStartDate(), dto.getEndDate());
        plan.setStatus(Constants.PLAN_ONGOING);
        travelPlanMapper.insert(plan);
        return plan;
    }

    @Override
    public TravelPlan update(Long userId, TravelPlanDTO dto) {
        TravelPlan plan = requireOwned(userId, dto.getId());
        if (dto.getDestinationId() != null) {
            Destination d = destinationMapper.selectById(dto.getDestinationId());
            if (d == null) {
                throw new BizException("目的地不存在");
            }
            plan.setDestinationId(d.getId());
            plan.setDestinationName(d.getName());
        }
        plan.setPlanName(dto.getPlanName());
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setIntro(dto.getIntro());
        validateDateRange(plan.getStartDate(), plan.getEndDate());
        travelPlanMapper.updateById(plan);
        return plan;
    }

    @Override
    @Transactional
    public void delete(Long userId, Long planId) {
        TravelPlan plan = requireOwned(userId, planId);
        travelPlanItemMapper.delete(new LambdaQueryWrapper<TravelPlanItem>()
                .eq(TravelPlanItem::getPlanId, planId));
        travelPlanMapper.deleteById(plan.getId());
    }

    @Override
    public TravelPlanDetailVO detail(Long userId, Long planId) {
        TravelPlan plan = requireOwned(userId, planId);
        TravelPlanDetailVO vo = new TravelPlanDetailVO();
        vo.setId(plan.getId());
        vo.setPlanName(plan.getPlanName());
        vo.setDestinationId(plan.getDestinationId());
        vo.setDestinationName(plan.getDestinationName());
        vo.setStartDate(plan.getStartDate());
        vo.setEndDate(plan.getEndDate());
        vo.setIntro(plan.getIntro());
        vo.setStatus(plan.getStatus());
        vo.setCreateTime(plan.getCreateTime());

        List<TravelPlanItem> items = travelPlanItemMapper.selectList(new LambdaQueryWrapper<TravelPlanItem>()
                .eq(TravelPlanItem::getPlanId, planId)
                .orderByAsc(TravelPlanItem::getDayNumber)
                .orderByAsc(TravelPlanItem::getSortOrder));
        // 按天分组
        Map<Integer, List<TravelPlanItem>> grouped = items.stream()
                .collect(Collectors.groupingBy(TravelPlanItem::getDayNumber));
        List<TravelPlanDetailVO.DayGroup> days = new ArrayList<>();
        grouped.keySet().stream().sorted().forEach(day -> {
            TravelPlanDetailVO.DayGroup g = new TravelPlanDetailVO.DayGroup();
            g.setDay(day);
            g.setItems(grouped.get(day).stream().map(this::toItemVO).toList());
            days.add(g);
        });
        vo.setDays(days);
        return vo;
    }

    @Override
    @Transactional
    public TravelPlanDetailVO addItem(Long userId, TravelPlanItemDTO dto) {
        TravelPlan plan = requireOwned(userId, dto.getPlanId());
        // 校验资源存在并取名称/图
        ResourceSnapshot res = resolveItem(dto.getItemType(), dto.getItemId());
        if (res == null) {
            throw new BizException("所选项目不存在或已下线");
        }
        // 排序号 = 同天最大值 + 1
        int maxSort = travelPlanItemMapper.selectList(new LambdaQueryWrapper<TravelPlanItem>()
                        .eq(TravelPlanItem::getPlanId, plan.getId())
                        .eq(TravelPlanItem::getDayNumber, dto.getDayNumber()))
                .stream().mapToInt(TravelPlanItem::getSortOrder).max().orElse(0);

        TravelPlanItem item = new TravelPlanItem();
        item.setPlanId(plan.getId());
        item.setDayNumber(dto.getDayNumber());
        item.setItemType(dto.getItemType());
        item.setItemId(dto.getItemId());
        item.setItemName(res.name);
        item.setStartTime(dto.getStartTime());
        item.setEndTime(dto.getEndTime());
        item.setRemark(dto.getRemark());
        item.setSortOrder(maxSort + 1);
        travelPlanItemMapper.insert(item);
        return detail(userId, plan.getId());
    }

    @Override
    @Transactional
    public TravelPlanDetailVO removeItem(Long userId, Long itemId) {
        TravelPlanItem item = travelPlanItemMapper.selectById(itemId);
        if (item == null) {
            throw new BizException(ResultCode.NOT_FOUND, "行程项目不存在");
        }
        requireOwned(userId, item.getPlanId());
        travelPlanItemMapper.deleteById(item.getId());
        return detail(userId, item.getPlanId());
    }

    @Override
    @Transactional
    public TravelPlanDetailVO moveItem(Long userId, Long itemId, Integer dayNumber, Integer sortOrder) {
        TravelPlanItem item = travelPlanItemMapper.selectById(itemId);
        if (item == null) {
            throw new BizException(ResultCode.NOT_FOUND, "行程项目不存在");
        }
        requireOwned(userId, item.getPlanId());
        if (dayNumber != null) {
            item.setDayNumber(dayNumber);
        }
        if (sortOrder != null) {
            item.setSortOrder(sortOrder);
        }
        travelPlanItemMapper.updateById(item);
        return detail(userId, item.getPlanId());
    }

    @Override
    public void changeStatus(Long userId, Long planId, String status) {
        TravelPlan plan = requireOwned(userId, planId);
        if (!List.of(Constants.PLAN_ONGOING, Constants.PLAN_FINISHED, Constants.PLAN_CANCELLED).contains(status)) {
            throw new BizException("计划状态不正确");
        }
        plan.setStatus(status);
        travelPlanMapper.updateById(plan);
    }

    // ==================== 内部 ====================

    private void validateDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BizException("结束日期不能早于开始日期");
        }
    }

    private TravelPlan requireOwned(Long userId, Long planId) {
        TravelPlan plan = travelPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BizException(ResultCode.NOT_FOUND, "计划不存在");
        }
        if (!Objects.equals(plan.getUserId(), userId)) {
            throw new BizException(ResultCode.FORBIDDEN, "无权操作该计划");
        }
        return plan;
    }

    /** 校验并获取资源名称(快照) */
    private ResourceSnapshot resolveItem(String type, Long itemId) {
        switch (type) {
            case Constants.ITEM_HOMESTAY -> {
                Homestay h = homestayMapper.selectById(itemId);
                if (h == null || !Constants.AUDIT_APPROVED.equals(h.getAuditStatus())) {
                    return null;
                }
                BigDecimal min = h.getMinPrice();
                return new ResourceSnapshot(h.getName(), h.getCoverImage(), h.getAddress(),
                        min == null ? "" : "¥" + min + "起");
            }
            case Constants.ITEM_SCENIC -> {
                ScenicSpot s = scenicSpotMapper.selectById(itemId);
                if (s == null || s.getStatus() != 1) {
                    return null;
                }
                return new ResourceSnapshot(s.getName(), s.getCoverImage(), s.getAddress(),
                        s.getPrice() == null || s.getPrice().signum() == 0 ? "免费" : "¥" + s.getPrice());
            }
            case Constants.ITEM_EXPERIENCE -> {
                TourismExperience e = experienceMapper.selectById(itemId);
                if (e == null || e.getStatus() != 1) {
                    return null;
                }
                return new ResourceSnapshot(e.getName(), e.getCoverImage(), e.getAddress(),
                        e.getPrice() == null ? "" : "¥" + e.getPrice());
            }
            case Constants.ITEM_RESTAURANT -> {
                Restaurant r = restaurantMapper.selectById(itemId);
                if (r == null || r.getStatus() != 1) {
                    return null;
                }
                return new ResourceSnapshot(r.getName(), r.getCoverImage(), r.getAddress(),
                        r.getPrice() == null ? "" : "¥" + r.getPrice() + "/人");
            }
            default -> {
                return null;
            }
        }
    }

    private record ResourceSnapshot(String name, String coverImage, String address, String priceText) {
    }

    private TravelPlanDetailVO.ItemVO toItemVO(TravelPlanItem item) {
        TravelPlanDetailVO.ItemVO vo = new TravelPlanDetailVO.ItemVO();
        vo.setId(item.getId());
        vo.setDayNumber(item.getDayNumber());
        vo.setItemType(item.getItemType());
        vo.setItemTypeText(TYPE_TEXT.getOrDefault(item.getItemType(), item.getItemType()));
        vo.setItemId(item.getItemId());
        vo.setItemName(item.getItemName());
        vo.setRemark(item.getRemark());
        vo.setSortOrder(item.getSortOrder());
        if (item.getStartTime() != null && item.getEndTime() != null) {
            vo.setTimeText(item.getStartTime() + "-" + item.getEndTime());
        } else if (item.getStartTime() != null) {
            vo.setTimeText(String.valueOf(item.getStartTime()));
        }
        // 补全封面/地址/价格(最新)
        ResourceSnapshot res = resolveItem(item.getItemType(), item.getItemId());
        if (res != null) {
            vo.setCoverImage(res.coverImage());
            vo.setAddress(res.address());
            vo.setPriceText(res.priceText());
        }
        return vo;
    }
}
