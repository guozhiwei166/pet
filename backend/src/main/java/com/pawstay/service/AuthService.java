package com.pawstay.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pawstay.common.BusinessException;
import com.pawstay.dto.LoginDTO;
import com.pawstay.dto.RegisterDTO;
import com.pawstay.entity.Merchant;
import com.pawstay.entity.User;
import com.pawstay.mapper.MerchantMapper;
import com.pawstay.mapper.UserMapper;
import com.pawstay.util.JwtUtil;
import com.pawstay.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private MessageService messageService;

    public Map<String, Object> register(RegisterDTO dto) {
        if (!"OWNER".equals(dto.role()) && !"MERCHANT".equals(dto.role())) {
            throw new BusinessException("注册角色不合法");
        }
        if ("MERCHANT".equals(dto.role())
                && (isBlank(dto.shopName()) || isBlank(dto.address()))) {
            throw new BusinessException("商家入驻请填写店铺名称与地址");
        }
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.phone()));
        if (exists > 0) throw new BusinessException("该手机号已注册，请直接登录");

        User u = new User();
        u.setPhone(dto.phone());
        u.setPassword(PasswordUtil.hash(dto.password()));
        u.setNickname(dto.nickname());
        u.setRole(dto.role());
        u.setStatus(1);
        u.setCreateTime(LocalDateTime.now());
        userMapper.insert(u);

        if ("MERCHANT".equals(dto.role())) {
            Merchant m = new Merchant();
            m.setUserId(u.getId());
            m.setShopName(dto.shopName());
            m.setAddress(dto.address());
            m.setPhone(dto.phone());
            m.setRating(new java.math.BigDecimal("5.0"));
            m.setReplyRate(98);
            m.setServiceSpecies("CAT,DOG");
            m.setAuditStatus("PENDING");
            m.setBoardingCount(0);
            m.setOpenStatus(1);
            m.setCreateTime(LocalDateTime.now());
            merchantMapper.insert(m);
        }
        messageService.send(u.getId(), "欢迎加入毛孩驿站",
                "MERCHANT".equals(dto.role()) ? "入驻申请已提交，等待平台审核，审核通过后即可接单。" : "愿每一只毛孩子都被温柔以待～",
                "SYSTEM", null);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("id", u.getId());
        res.put("nickname", u.getNickname());
        res.put("role", u.getRole());
        return res;
    }

    public Map<String, Object> login(LoginDTO dto) {
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.phone()));
        if (u == null || !PasswordUtil.match(dto.password(), u.getPassword())) {
            throw new BusinessException("手机号或密码错误");
        }
        if (u.getStatus() == null || u.getStatus() != 1) {
            throw new BusinessException("账号已被停用，请联系平台客服");
        }
        Long merchantId = null;
        if ("MERCHANT".equals(u.getRole())) {
            Merchant m = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, u.getId()));
            if (m != null) merchantId = m.getId();
        }
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", u.getId());
        user.put("nickname", u.getNickname());
        user.put("phone", u.getPhone());
        user.put("avatar", u.getAvatar());
        user.put("role", u.getRole());
        user.put("merchantId", merchantId);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("token", JwtUtil.create(u.getId(), u.getRole()));
        res.put("user", user);
        return res;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
