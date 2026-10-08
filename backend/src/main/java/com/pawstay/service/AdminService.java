package com.pawstay.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pawstay.entity.Merchant;
import com.pawstay.entity.Pet;
import com.pawstay.entity.User;
import com.pawstay.mapper.MerchantMapper;
import com.pawstay.mapper.OrderMapper;
import com.pawstay.mapper.PetMapper;
import com.pawstay.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private PetMapper petMapper;
    @Autowired
    private OrderMapper orderMapper;

    /** 管理端数据大屏：总览 + 近7日趋势 + 商家排行 + 物种分布 */
    public Map<String, Object> stats() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("users", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, "OWNER")));
        overview.put("merchants", merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>().eq(Merchant::getAuditStatus, "APPROVED")));
        overview.put("pets", petMapper.selectCount(new LambdaQueryWrapper<Pet>()));
        overview.put("orders", orderMapper.selectCount(new LambdaQueryWrapper<com.pawstay.entity.BoardingOrder>()));
        overview.put("revenue", orderMapper.sumRevenue());

        // 近7日订单与营收趋势（缺失日期补0，保证图表连续）
        LocalDateTime from = LocalDate.now().minusDays(6).atStartOfDay();
        Map<String, Map<String, Object>> byDay = new HashMap<>();
        for (Map<String, Object> row : orderMapper.selectDailyStats(from)) {
            String d = String.valueOf(row.getOrDefault("d", row.get("D"))).substring(0, 10);
            byDay.put(d, row);
        }
        List<Map<String, Object>> daily = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            String key = day.format(DateTimeFormatter.ISO_LOCAL_DATE);
            Map<String, Object> row = byDay.get(key);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", key.substring(5));
            item.put("cnt", row == null ? 0 : ((Number) first(row, "cnt", "CNT")).intValue());
            item.put("amount", row == null ? 0 : ((Number) first(row, "amount", "AMOUNT")).doubleValue());
            daily.add(item);
        }

        List<Map<String, Object>> top = new ArrayList<>();
        for (Map<String, Object> row : orderMapper.selectTopMerchants()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", first(row, "name", "NAME"));
            item.put("cnt", ((Number) first(row, "cnt", "CNT")).intValue());
            top.add(item);
        }

        List<Map<String, Object>> species = new ArrayList<>();
        for (Map<String, Object> row : petMapper.selectSpeciesStats()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", "CAT".equals(first(row, "name", "NAME")) ? "猫咪" : "狗狗");
            item.put("value", ((Number) first(row, "cnt", "CNT")).intValue());
            species.add(item);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("overview", overview);
        res.put("daily", daily);
        res.put("topMerchants", top);
        res.put("species", species);
        return res;
    }

    /** 用户管理分页（脱敏：不返回密码散列） */
    public Page<User> pageUsers(long page, long size, String keyword) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(User::getNickname, keyword).or().like(User::getPhone, keyword));
        }
        w.orderByDesc(User::getId);
        Page<User> p = userMapper.selectPage(new Page<>(page, size), w);
        p.getRecords().forEach(u -> u.setPassword(null));
        return p;
    }

    public Object first(Map<String, Object> row, String... keys) {
        for (String k : keys) {
            for (Map.Entry<String, Object> e : row.entrySet()) {
                if (e.getKey().equalsIgnoreCase(k)) return e.getValue();
            }
        }
        return 0;
    }
}
