package com.petcare.statistics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.boarding.entity.BoardingOrder;
import com.petcare.boarding.mapper.BoardingOrderMapper;
import com.petcare.care.entity.CareTask;
import com.petcare.care.mapper.CareTaskMapper;
import com.petcare.room.entity.Room;
import com.petcare.room.mapper.RoomMapper;
import com.petcare.user.entity.SysUser;
import com.petcare.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 运营统计聚合：驾驶舱指标、趋势、热门服务、员工完成率
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final BoardingOrderMapper orderMapper;
    private final RoomMapper roomMapper;
    private final SysUserMapper userMapper;
    private final com.petcare.user.mapper.SysUserRoleMapper userRoleMapper;
    private final com.petcare.user.mapper.SysRoleMapper roleMapper;
    private final com.petcare.boarding.mapper.BoardingOrderServiceItemMapper orderServiceMapper;
    private final com.petcare.care.mapper.CareTaskMapper careTaskMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    private static final String DASH_CACHE_KEY = "admin:dashboard";
    private static final java.time.Duration DASH_CACHE_TTL = java.time.Duration.ofSeconds(60);

    /** 营业额统计口径：已支付（payTime 落在统计区间）且未取消/未退款 */
    private boolean revenueEligible(BoardingOrder o) {
        return o.getPayTime() != null
                && !"CANCELLED".equals(o.getStatus())
                && !"REFUNDING".equals(o.getStatus())
                && !"REFUNDED".equals(o.getStatus());
    }

    /** 驾驶舱总览（Redis 缓存 60 秒，miss 时回源聚合后回填） */
    @SuppressWarnings("unchecked")
    public Map<String, Object> dashboard() {
        try {
            Object cached = redisTemplate.opsForValue().get(DASH_CACHE_KEY);
            if (cached instanceof Map<?, ?> map) return (Map<String, Object>) map;
        } catch (Exception ignored) {
            // Redis 不可用降级直算
        }
        Map<String, Object> fresh = computeDashboard();
        try {
            redisTemplate.opsForValue().set(DASH_CACHE_KEY, fresh, DASH_CACHE_TTL);
        } catch (Exception ignored) {
        }
        return fresh;
    }

    private Map<String, Object> computeDashboard() {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        // 订单维度指标全部下推数据库聚合，避免全表加载内存求和
        BigDecimal todayRevenue = orderMapper.sumRevenueBetween(dayStart, dayEnd);
        long todayCheckin = orderMapper.countCheckinBetween(dayStart, dayEnd);
        long todayCheckout = orderMapper.countCheckoutBetween(dayStart, dayEnd);

        List<Room> rooms = roomMapper.selectList(null);
        long occupied = rooms.stream().filter(r -> "OCCUPIED".equals(r.getStatus())).count();

        // 宠物状态分布（当日日报）
        var reportMapper = com.petcare.util.SpringContextHolder.getBean(com.petcare.care.mapper.PetDailyReportMapper.class);
        var todayReports = reportMapper.selectList(new LambdaQueryWrapper<com.petcare.care.entity.PetDailyReport>()
                .eq(com.petcare.care.entity.PetDailyReport::getReportDate, LocalDate.now()));
        long abnormalToday = 0, normalToday = 0;
        for (var r : todayReports) {
            if (r.getIsAbnormal() != null && r.getIsAbnormal() == 1) abnormalToday++;
            else normalToday++;
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("todayRevenue", todayRevenue);
        res.put("occupying", occupied);
        res.put("todayCheckin", todayCheckin);
        res.put("todayCheckout", todayCheckout);
        res.put("roomTotal", rooms.size());
        res.put("roomUsage", rooms.isEmpty() ? 0 : Math.round(occupied * 100.0 / rooms.size()));
        res.put("petNormal", normalToday);
        res.put("petAbnormal", abnormalToday);
        return res;
    }

    /** 近 N 日营业额与订单量趋势（口径与驾驶舱一致：按 payTime 统计，排除取消/退款） */
    public List<Map<String, Object>> trend(int days) {
        LocalDate from = LocalDate.now().minusDays(days - 1);
        List<Map<String, Object>> agg = orderMapper.sumDailyRevenue(from.atStartOfDay());
        Map<String, Map<String, Object>> byDay = new LinkedHashMap<>();
        for (Map<String, Object> row : agg) {
            String day = String.valueOf(row.get("day"));
            byDay.put(day, row);
        }
        List<Map<String, Object>> res = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            String day = from.plusDays(i).toString().substring(5); // MM-dd
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", day);
            Map<String, Object> row = byDay.get(day);
            item.put("orders", row == null ? 0 : row.get("orders"));
            item.put("revenue", row == null ? BigDecimal.ZERO : row.get("revenue"));
            res.add(item);
        }
        return res;
    }

    /** 热门服务 Top（数据库聚合） */
    public List<Map<String, Object>> hotServices() {
        return orderServiceMapper.hotServices(6);
    }

    /** 员工任务完成率（数据库聚合） */
    public List<Map<String, Object>> staffStats() {
        List<Map<String, Object>> agg = careTaskMapper.countGroupByStaff();
        List<Map<String, Object>> res = new ArrayList<>();
        for (Map<String, Object> row : agg) {
            Long staffId = row.get("staffId") == null ? null : Long.valueOf(row.get("staffId").toString());
            if (staffId == null) continue;
            long total = row.get("total") == null ? 0 : Long.parseLong(row.get("total").toString());
            long done = row.get("done") == null ? 0 : Long.parseLong(row.get("done").toString());
            SysUser u = userMapper.selectById(staffId);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", u == null ? "员工" : u.getNickname());
            m.put("total", total);
            m.put("done", done);
            m.put("rate", total == 0 ? 0 : Math.round(done * 100.0 / total));
            res.add(m);
        }
        return res;
    }

    /** 用户列表（含角色名） */
    public List<Map<String, Object>> users(String keyword) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<SysUser>().orderByDesc(SysUser::getId);
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(SysUser::getNickname, keyword).or().like(SysUser::getUsername, keyword));
        }
        List<SysUser> users = userMapper.selectList(w);
        List<com.petcare.user.entity.SysUserRole> urs = userRoleMapper.selectList(null);
        List<com.petcare.user.entity.SysRole> roles = roleMapper.selectList(null);
        return users.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname());
            m.put("phone", u.getPhone());
            m.put("status", u.getStatus());
            m.put("createTime", u.getCreateTime());
            var ur = urs.stream().filter(x -> x.getUserId().equals(u.getId())).findFirst();
            String code = ur.flatMap(x -> roles.stream().filter(r -> r.getId().equals(x.getRoleId())).findFirst())
                    .map(com.petcare.user.entity.SysRole::getCode).orElse("ROLE_USER");
            String roleName = switch (code) {
                case "ROLE_STAFF" -> "工作人员";
                case "ROLE_ADMIN" -> "管理员";
                default -> "宠物主人";
            };
            m.put("role", code);
            m.put("roleName", roleName);
            return m;
        }).collect(Collectors.toList());
    }

    /** 管理员重置任意用户密码（忘记密码兜底） */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new com.petcare.common.BusinessException("用户不存在");
        if (newPassword == null || newPassword.length() < 6) {
            throw new com.petcare.common.BusinessException("新密码至少6位");
        }
        u.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(u);
    }

    @Transactional
    public void toggleUserStatus(Long id) {
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new com.petcare.common.BusinessException("用户不存在");
        if ("admin".equals(u.getUsername())) throw new com.petcare.common.BusinessException("不能禁用管理员账号");
        u.setStatus(u.getStatus() != null && u.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(u);
    }
}
