package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.common.BizException;
import com.tourism.common.Constants;
import com.tourism.common.ResultCode;
import com.tourism.dto.LoginDTO;
import com.tourism.dto.RegisterDTO;
import com.tourism.entity.Merchant;
import com.tourism.entity.User;
import com.tourism.mapper.MerchantMapper;
import com.tourism.mapper.UserMapper;
import com.tourism.security.JwtUtil;
import com.tourism.security.LoginUser;
import com.tourism.service.AuthService;
import com.tourism.vo.FindUsernameVO;
import com.tourism.vo.LoginVO;
import com.tourism.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 认证服务实现:注册 / 登录 / 个人信息
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // ==================== 注册 ====================

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        registerUser(dto, Constants.ROLE_USER);
    }

    @Override
    @Transactional
    public void registerMerchant(RegisterDTO userDto, String merchantName, String contactName,
                                 String contactPhone, String description) {
        User user = registerUser(userDto, Constants.ROLE_MERCHANT);
        if (merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUserId, user.getId())) > 0) {
            throw new BizException("该账号已申请过商家");
        }
        Merchant merchant = new Merchant();
        merchant.setUserId(user.getId());
        merchant.setMerchantName(merchantName);
        merchant.setContactName(contactName);
        merchant.setContactPhone(contactPhone);
        merchant.setDescription(description);
        merchant.setStatus(Constants.AUDIT_PENDING);
        merchantMapper.insert(merchant);
    }

    private User registerUser(RegisterDTO dto, String role) {
        checkRegister(dto);
        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname().trim() : dto.getUsername().trim());
        user.setPhone(dto.getPhone());
        user.setRole(role);
        user.setStatus(Constants.USER_STATUS_NORMAL);
        userMapper.insert(user);
        return user;
    }

    private void checkRegister(RegisterDTO dto) {
        if (!Objects.equals(dto.getPassword(), dto.getConfirmPassword())) {
            throw new BizException("两次输入的密码不一致");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername().trim())) > 0) {
            throw new BizException(ResultCode.CONFLICT, "用户名已被注册");
        }
    }

    // ==================== 登录 ====================

    @Override
    public List<FindUsernameVO> findByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return List.of();
        }
        String p = phone.trim();
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, p)
                        .orderByAsc(User::getCreateTime))
                .stream().map(u -> {
                    FindUsernameVO vo = new FindUsernameVO();
                    vo.setUsername(u.getUsername());
                    vo.setNickname(u.getNickname());
                    vo.setAvatar(u.getAvatar());
                    vo.setRole(u.getRole());
                    vo.setRoleText(switch (u.getRole()) {
                        case Constants.ROLE_USER -> "普通用户";
                        case Constants.ROLE_MERCHANT -> "商家";
                        case Constants.ROLE_ADMIN -> "管理员";
                        default -> u.getRole();
                    });
                    return vo;
                }).collect(Collectors.toList());
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername().trim())
                .last("limit 1"));
        if (user == null || !encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.BAD_REQUEST, "用户名或密码错误");
        }
        if (Objects.equals(user.getStatus(), Constants.USER_STATUS_DISABLED)) {
            throw new BizException(ResultCode.FORBIDDEN, "账号已被禁用,请联系管理员");
        }
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setLoginTime(LocalDateTime.now());

        // 商家角色额外返回审核状态,便于前端判断能否进入经营后台
        if (Constants.ROLE_MERCHANT.equals(user.getRole())) {
            Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                    .eq(Merchant::getUserId, user.getId()).last("limit 1"));
            if (merchant != null) {
                vo.setMerchantStatus(merchant.getStatus());
                vo.setMerchantApproved(Constants.AUDIT_APPROVED.equals(merchant.getStatus()));
            }
        }
        return vo;
    }

    // ==================== 个人信息 ====================

    @Override
    public UserVO currentUser() {
        Long userId = LoginUser.userId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "账号不存在");
        }
        return toVO(user);
    }

    @Override
    public UserVO updateProfile(UserVO vo) {
        Long userId = LoginUser.userId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("账号不存在");
        }
        user.setNickname(vo.getNickname());
        user.setPhone(vo.getPhone());
        user.setEmail(vo.getEmail());
        user.setAvatar(vo.getAvatar());
        user.setGender(vo.getGender());
        userMapper.updateById(user);
        return toVO(user);
    }

    @Override
    public void updatePassword(String oldPassword, String newPassword) {
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6) {
            throw new BizException("新密码长度需不少于 6 位");
        }
        User user = userMapper.selectById(LoginUser.userId());
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        user.setPassword(encoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        if (Constants.ROLE_MERCHANT.equals(user.getRole())) {
            Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                    .eq(Merchant::getUserId, user.getId()).last("limit 1"));
            if (merchant != null) {
                vo.setMerchantId(merchant.getId());
                vo.setMerchantStatus(merchant.getStatus());
            }
        }
        return vo;
    }
}
