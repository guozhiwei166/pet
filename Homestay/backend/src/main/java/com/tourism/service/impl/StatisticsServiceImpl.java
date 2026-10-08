package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.common.Constants;
import com.tourism.entity.Destination;
import com.tourism.entity.Homestay;
import com.tourism.entity.Merchant;
import com.tourism.entity.Orders;
import com.tourism.entity.Report;
import com.tourism.entity.RoomType;
import com.tourism.entity.User;
import com.tourism.mapper.DestinationMapper;
import com.tourism.mapper.HomestayMapper;
import com.tourism.mapper.MerchantMapper;
import com.tourism.mapper.OrdersMapper;
import com.tourism.mapper.ReportMapper;
import com.tourism.mapper.RoomTypeMapper;
import com.tourism.mapper.UserMapper;
import com.tourism.service.StatisticsService;
import com.tourism.vo.MerchantStatsVO;
import com.tourism.vo.PlatformStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据统计服务实现(演示级数据量,查询后在内存聚合,简单可靠)
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final OrdersMapper ordersMapper;
    private final HomestayMapper homestayMapper;
    private final RoomTypeMapper roomTypeMapper;
    private final DestinationMapper destinationMapper;
    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final ReportMapper reportMapper;

    // ==================== 商家统计 ====================

    @Override
    public MerchantStatsVO merchantStats(Long merchantId) {
        // 该商家民宿
        List<Homestay> mine = homestayMapper.selectList(new LambdaQueryWrapper<Homestay>()
                .eq(Homestay::getMerchantId, merchantId));
        List<Long> hid = mine.stream().map(Homestay::getId).toList();
        MerchantStatsVO vo = new MerchantStatsVO();
        if (hid.isEmpty()) {
            vo.setTodayOrders(0L);
            vo.setMonthOrders(0L);
            vo.setMonthIncome(BigDecimal.ZERO);
            vo.setOccupancyRate(BigDecimal.ZERO);
            vo.setAvgRating(BigDecimal.ZERO);
            vo.setTotalRooms(0L);
            vo.setLast7DayOrders(new ArrayList<>());
            vo.setMonthIncomeTrend(new ArrayList<>());
            vo.setRoomTypeSales(new ArrayList<>());
            vo.setOrderStatusDist(new HashMap<>());
            return vo;
        }
        List<Orders> all = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .in(Orders::getHomestayId, hid));
        LocalDate today = LocalDate.now();

        // 概览
        vo.setTodayOrders(all.stream().filter(o -> o.getCreateTime() != null
                && o.getCreateTime().toLocalDate().equals(today)).count());
        vo.setMonthOrders(all.stream().filter(o -> o.getCreateTime() != null
                && YearMonth.from(o.getCreateTime()).equals(YearMonth.now())).count());
        vo.setMonthIncome(all.stream()
                .filter(o -> Constants.ORDER_PAID.equals(o.getStatus())
                        || Constants.ORDER_CHECKED_IN.equals(o.getStatus())
                        || Constants.ORDER_ARRIVED.equals(o.getStatus())
                        || Constants.ORDER_COMPLETED.equals(o.getStatus()))
                .filter(o -> o.getPayTime() != null && YearMonth.from(o.getPayTime()).equals(YearMonth.now()))
                .map(Orders::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setAvgRating(mine.stream().filter(h -> h.getRatingCount() != null && h.getRatingCount() > 0)
                .map(Homestay::getRating)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, (int) mine.stream()
                        .filter(h -> h.getRatingCount() != null && h.getRatingCount() > 0).count())), 1, java.math.RoundingMode.HALF_UP));
        // 在售房型房间总数(与入住率分母同口径)
        long totalRoomsCapacity = roomTypeMapper.selectList(new LambdaQueryWrapper<RoomType>()
                        .in(RoomType::getHomestayId, hid))
                .stream().mapToLong(RoomType::getRoomCount).sum();
        vo.setTotalRooms(totalRoomsCapacity);

        // 入住率 = 近30天(今天往前)实际占用晚数 / (房间总数 × 30)
        // 仅统计 check_in 到 check_out 中落在 [today-30, today] 窗口内的晚数,排除未来订单
        long occupiedNights = 0;
        for (Orders o : all) {
            if (!(Constants.ORDER_PAID.equals(o.getStatus())
                    || Constants.ORDER_CHECKED_IN.equals(o.getStatus())
                    || Constants.ORDER_ARRIVED.equals(o.getStatus())
                    || Constants.ORDER_COMPLETED.equals(o.getStatus()))) {
                continue;
            }
            if (o.getCheckInDate() == null || o.getCheckOutDate() == null) {
                continue;
            }
            LocalDate windowStart = today.minusDays(30);
            LocalDate overlapStart = o.getCheckInDate().isAfter(windowStart) ? o.getCheckInDate() : windowStart;
            LocalDate overlapEnd = o.getCheckOutDate().isBefore(today) ? o.getCheckOutDate() : today;
            if (overlapEnd.isAfter(overlapStart)) {
                long nights = java.time.temporal.ChronoUnit.DAYS.between(overlapStart, overlapEnd);
                occupiedNights += nights * o.getRoomCount();
            }
        }
        vo.setOccupancyRate(totalRoomsCapacity == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(occupiedNights * 100.0 / (totalRoomsCapacity * 30)).setScale(1, java.math.RoundingMode.HALF_UP));

        // 近7天订单趋势
        List<MerchantStatsVO.DateValue> week = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            MerchantStatsVO.DateValue dv = new MerchantStatsVO.DateValue();
            dv.setDate(d);
            dv.setValue(all.stream().filter(o -> o.getCreateTime() != null
                    && o.getCreateTime().toLocalDate().equals(d)).count());
            week.add(dv);
        }
        vo.setLast7DayOrders(week);

        // 近6月收入趋势
        List<MerchantStatsVO.MonthValue> months = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            MerchantStatsVO.MonthValue mv = new MerchantStatsVO.MonthValue();
            mv.setMonth(ym.toString());
            List<Orders> inMonth = all.stream()
                    .filter(o -> o.getPayTime() != null && YearMonth.from(o.getPayTime()).equals(ym)
                            && (Constants.ORDER_PAID.equals(o.getStatus())
                            || Constants.ORDER_CHECKED_IN.equals(o.getStatus())
                            || Constants.ORDER_ARRIVED.equals(o.getStatus())
                            || Constants.ORDER_COMPLETED.equals(o.getStatus())))
                    .toList();
            mv.setIncome(inMonth.stream().map(Orders::getPayAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            mv.setOrders((long) inMonth.size());
            months.add(mv);
        }
        vo.setMonthIncomeTrend(months);

        // 房型销量排行(按订单中房间晚数)
        Map<Long, Long> salesByRoomType = new HashMap<>();
        Map<Long, BigDecimal> incomeByRoomType = new HashMap<>();
        for (Orders o : all) {
            if (!Constants.ORDER_CANCELLED.equals(o.getStatus())) {
                salesByRoomType.merge(o.getRoomTypeId(), (long) o.getNights() * o.getRoomCount(), Long::sum);
                if (Constants.ORDER_PAID.equals(o.getStatus()) || Constants.ORDER_CHECKED_IN.equals(o.getStatus())
                        || Constants.ORDER_ARRIVED.equals(o.getStatus())
                        || Constants.ORDER_COMPLETED.equals(o.getStatus())) {
                    incomeByRoomType.merge(o.getRoomTypeId(), o.getPayAmount(), BigDecimal::add);
                }
            }
        }
        List<MerchantStatsVO.RankItem> sales = new ArrayList<>();
        salesByRoomType.forEach((rtId, cnt) -> {
            RoomType rt = roomTypeMapper.selectById(rtId);
            MerchantStatsVO.RankItem item = new MerchantStatsVO.RankItem();
            item.setName(rt == null ? "房型#" + rtId : rt.getName());
            item.setSales(cnt);
            item.setIncome(incomeByRoomType.getOrDefault(rtId, BigDecimal.ZERO));
            sales.add(item);
        });
        sales.sort((a, b) -> Long.compare(b.getSales(), a.getSales()));
        vo.setRoomTypeSales(sales);

        // 订单状态分布
        vo.setOrderStatusDist(all.stream().collect(Collectors.groupingBy(Orders::getStatus, Collectors.counting())));
        return vo;
    }

    // ==================== 平台统计 ====================

    @Override
    public PlatformStatsVO platformStats() {
        PlatformStatsVO vo = new PlatformStatsVO();
        LocalDate today = LocalDate.now();

        vo.setUserCount(userMapper.selectCount(null));
        vo.setMerchantCount(merchantMapper.selectCount(null));
        vo.setHomestayCount(homestayMapper.selectCount(null));
        vo.setOrderCount(ordersMapper.selectCount(null));

        List<Orders> allOrders = ordersMapper.selectList(null);
        vo.setTodayOrderCount(allOrders.stream().filter(o -> o.getCreateTime() != null
                && o.getCreateTime().toLocalDate().equals(today)).count());
        vo.setMonthIncome(allOrders.stream()
                .filter(o -> (Constants.ORDER_PAID.equals(o.getStatus())
                        || Constants.ORDER_CHECKED_IN.equals(o.getStatus())
                        || Constants.ORDER_ARRIVED.equals(o.getStatus())
                        || Constants.ORDER_COMPLETED.equals(o.getStatus())))
                .filter(o -> o.getPayTime() != null && YearMonth.from(o.getPayTime()).equals(YearMonth.now()))
                .map(Orders::getPayAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setPendingHomestay(homestayMapper.selectCount(new LambdaQueryWrapper<Homestay>()
                .eq(Homestay::getAuditStatus, Constants.AUDIT_PENDING)));
        vo.setPendingMerchant(merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getStatus, Constants.AUDIT_PENDING)));
        vo.setPendingReport(reportMapper.selectCount(new LambdaQueryWrapper<Report>()
                .eq(Report::getStatus, Constants.REPORT_PENDING)));

        // 近7日新增用户(按天统计,避免逐日 N+1,聚合后填充)
        LocalDate weekAgo = today.minusDays(6);
        List<User> recentUsers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, weekAgo.atStartOfDay()));
        Map<LocalDate, Long> byDay = recentUsers.stream()
                .filter(u -> u.getCreateTime() != null)
                .collect(Collectors.groupingBy(u -> u.getCreateTime().toLocalDate(), Collectors.counting()));
        List<PlatformStatsVO.UserGrowth> growth = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            PlatformStatsVO.UserGrowth g = new PlatformStatsVO.UserGrowth();
            g.setDate(d);
            g.setCount(byDay.getOrDefault(d, 0L));
            growth.add(g);
        }
        vo.setUserGrowth(growth);

        // 近7日订单
        List<PlatformStatsVO.OrderTrend> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            PlatformStatsVO.OrderTrend t = new PlatformStatsVO.OrderTrend();
            t.setDate(d);
            t.setCount(allOrders.stream().filter(o -> o.getCreateTime() != null
                    && o.getCreateTime().toLocalDate().equals(d)).count());
            trend.add(t);
        }
        vo.setOrderTrend(trend);

        // 近6月收入
        List<PlatformStatsVO.MonthIncome> months = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            PlatformStatsVO.MonthIncome mi = new PlatformStatsVO.MonthIncome();
            mi.setMonth(ym.toString());
            mi.setIncome(allOrders.stream()
                    .filter(o -> o.getPayTime() != null && YearMonth.from(o.getPayTime()).equals(ym)
                            && (Constants.ORDER_PAID.equals(o.getStatus())
                            || Constants.ORDER_CHECKED_IN.equals(o.getStatus())
                            || Constants.ORDER_ARRIVED.equals(o.getStatus())
                            || Constants.ORDER_COMPLETED.equals(o.getStatus())))
                    .map(Orders::getPayAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            months.add(mi);
        }
        vo.setMonthIncomeTrend(months);

        // 热门目的地(按民宿数)
        List<PlatformStatsVO.RankItem> hotDest = new ArrayList<>();
        Map<Long, Long> destCount = homestayMapper.selectList(new LambdaQueryWrapper<Homestay>()
                        .eq(Homestay::getAuditStatus, Constants.AUDIT_APPROVED))
                .stream().collect(Collectors.groupingBy(Homestay::getDestinationId, Collectors.counting()));
        destCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .forEach(e -> {
                    Destination d = destinationMapper.selectById(e.getKey());
                    PlatformStatsVO.RankItem item = new PlatformStatsVO.RankItem();
                    item.setName(d == null ? "目的地#" + e.getKey() : d.getName());
                    item.setValue(e.getValue());
                    hotDest.add(item);
                });
        vo.setHotDestinations(hotDest);

        // 热门民宿(按订单数)
        Map<Long, Long> orderByHomestay = allOrders.stream()
                .filter(o -> !Constants.ORDER_CANCELLED.equals(o.getStatus()))
                .collect(Collectors.groupingBy(Orders::getHomestayId, Collectors.counting()));
        List<PlatformStatsVO.RankItem> hotHs = new ArrayList<>();
        orderByHomestay.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .forEach(e -> {
                    Homestay h = homestayMapper.selectById(e.getKey());
                    PlatformStatsVO.RankItem item = new PlatformStatsVO.RankItem();
                    item.setName(h == null ? "民宿#" + e.getKey() : h.getName());
                    item.setValue(e.getValue());
                    hotHs.add(item);
                });
        vo.setHotHomestays(hotHs);

        vo.setOrderStatusDist(allOrders.stream()
                .collect(Collectors.groupingBy(Orders::getStatus, Collectors.counting())));
        return vo;
    }
}
