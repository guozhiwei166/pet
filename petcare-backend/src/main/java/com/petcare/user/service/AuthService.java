package com.petcare.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.BusinessException;
import com.petcare.common.CaptchaUtil;
import com.petcare.security.JwtUtil;
import com.petcare.user.dto.LoginDTO;
import com.petcare.user.dto.PasswordDTO;
import com.petcare.user.dto.ProfileDTO;
import com.petcare.user.dto.RegisterDTO;
import com.petcare.user.entity.SysRole;
import com.petcare.user.entity.SysUser;
import com.petcare.user.entity.SysUserRole;
import com.petcare.user.mapper.SysRoleMapper;
import com.petcare.user.mapper.SysUserMapper;
import com.petcare.user.mapper.SysUserRoleMapper;
import com.petcare.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final String FAIL_PREFIX = "login:fail:";
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String CAPTCHA_UNAVAILABLE = "验证码服务暂不可用，请稍后重试";
    private static final int MAX_FAIL = 5;

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 图形验证码：图片 dataURL + Redis 存 5 分钟 */
    public Map<String, Object> captcha() {
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String code = CaptchaUtil.randomCode();
        try {
            redisTemplate.opsForValue().set(CAPTCHA_PREFIX + captchaId, code, Duration.ofMinutes(5));
        } catch (Exception e) {
            // 验证码强依赖 Redis，挂掉时给出可读提示而不是 500"系统开小差"
            throw new BusinessException(503, CAPTCHA_UNAVAILABLE);
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("captchaId", captchaId);
        res.put("image", CaptchaUtil.toDataUrl(code));
        return res;
    }

    public Map<String, Object> register(RegisterDTO dto) {
        verifyCaptcha(dto.captchaId(), dto.captchaCode());
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.username())) > 0) {
            throw new BusinessException("用户名已被占用");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, dto.phone())) > 0) {
            throw new BusinessException("该手机号已注册");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.username());
        user.setPhone(dto.phone());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setNickname(dto.nickname());
        user.setStatus(1);
        try {
            userMapper.insert(user);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 并发注册兜底：唯一索引（uk_username / uk_phone）裁决，先查后插只是快速路径
            throw new BusinessException("用户名或手机号已被占用");
        }
        bindRole(user.getId(), "ROLE_USER");

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("id", user.getId());
        res.put("username", user.getUsername());
        return res;
    }

    public Map<String, Object> login(LoginDTO dto) {
        // 登录同样需要图形验证码（防撞库/暴力破解）
        verifyCaptcha(dto.captchaId(), dto.captchaCode());
        String failKey = FAIL_PREFIX + dto.username();
        // Redis 不可用时安全功能降级（不锁定），不影响登录主流程
        long failCount = 0;
        try {
            Object v = redisTemplate.opsForValue().get(failKey);
            failCount = v == null ? 0 : Long.parseLong(v.toString());
        } catch (Exception ignored) { }
        if (failCount >= MAX_FAIL) {
            throw new BusinessException(429, "失败次数过多，账号已锁定 10 分钟");
        }
        // 账号可为用户名或手机号：分两次查询而非 OR —— 用户名允许纯数字，
        // 若某人的用户名恰为他人手机号，OR 条件会命中两行导致 selectOne 抛 TooManyResultsException
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.username()));
        if (user == null) {
            user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, dto.username()));
        }
        if (user == null || !passwordEncoder.matches(dto.password(), user.getPassword())) {
            try {
                redisTemplate.opsForValue().increment(failKey);
                redisTemplate.expire(failKey, Duration.ofMinutes(10));
            } catch (Exception ignored) { }
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        try { redisTemplate.delete(failKey); } catch (Exception ignored) { }
        String role = roleOf(user.getId());
        String token = jwtUtil.create(user.getId(), user.getUsername(), role);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("token", token);
        res.put("user", toVO(user, role));
        return res;
    }

    public void logout(String token) {
        try {
            long remaining = jwtUtil.remainingMs(token);
            if (remaining > 0) {
                redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, 1, Duration.ofMillis(remaining));
            }
        } catch (Exception ignored) { }
    }

    public UserVO me(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        return toVO(user, roleOf(userId));
    }

    public UserVO updateProfile(Long userId, ProfileDTO dto) {
        SysUser user = userMapper.selectById(userId);
        user.setNickname(dto.nickname());
        if (dto.avatar() != null) user.setAvatar(dto.avatar());
        userMapper.updateById(user);
        return toVO(user, roleOf(userId));
    }

    public void changePassword(Long userId, PasswordDTO dto) {
        SysUser user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(dto.oldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        // 已设置密保问题的用户，改密必须同时校验密保答案（防账号被盗后直接改密）
        verifySecurityAnswer(user, dto.securityAnswer());
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userMapper.updateById(user);
    }

    /** 设置/修改密保问题与答案 */
    public void saveSecurityQuestion(Long userId, String question, String answer) {
        if (question == null || question.isBlank() || answer == null || answer.isBlank()) {
            throw new BusinessException("请填写密保问题与答案");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setSecurityQuestion(question.trim());
        user.setSecurityAnswer(passwordEncoder.encode(answer.trim()));
        userMapper.updateById(user);
    }

    /** 忘记密码：手机号 + 图形验证码 + （已设置则需）密保答案 校验后重置 */
    public void resetByPhone(String phone, String captchaId, String captchaCode, String newPassword, String securityAnswer) {
        verifyCaptcha(captchaId, captchaCode);
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码至少6位");
        }
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
        if (user == null) throw new BusinessException("该手机号未注册");
        // 已设置密保则必须回答正确（纯手机号找回风险太高）
        verifySecurityAnswer(user, securityAnswer);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private void verifySecurityAnswer(SysUser user, String answer) {
        String q = user.getSecurityQuestion();
        String a = user.getSecurityAnswer();
        if (q == null || q.isBlank() || a == null || a.isBlank()) return; // 未设置密保：不做额外要求
        if (answer == null || answer.isBlank() || !passwordEncoder.matches(answer.trim(), a)) {
            throw new BusinessException("密保答案不正确");
        }
    }

    // ---------- 内部方法 ----------

    /** 仅比对验证码是否正确，不销毁（供注册/找回密码分步表单"下一步"即时校验，最后提交时再消费） */
    public void verifyCaptchaPublic(String captchaId, String captchaCode) {
        checkCaptcha(captchaId, captchaCode, false);
    }

    private void verifyCaptcha(String captchaId, String captchaCode) {
        checkCaptcha(captchaId, captchaCode, true);
    }

    private void checkCaptcha(String captchaId, String captchaCode, boolean consume) {
        Object saved;
        try {
            saved = redisTemplate.opsForValue().get(CAPTCHA_PREFIX + captchaId);
        } catch (Exception e) {
            throw new BusinessException(503, CAPTCHA_UNAVAILABLE);
        }
        if (saved == null) throw new BusinessException("验证码已过期，请刷新重试");
        // Redis 用 GenericJackson2Json 序列化字符串会带引号（如 "YRHN"），需去引号再比对
        String real = saved.toString().trim();
        if (real.length() >= 2 && real.startsWith("\"") && real.endsWith("\"")) {
            real = real.substring(1, real.length() - 1);
        }
        if (!real.equalsIgnoreCase(captchaCode)) {
            throw new BusinessException("验证码不正确");
        }
        if (consume) redisTemplate.delete(CAPTCHA_PREFIX + captchaId);
    }

    private void bindRole(Long userId, String roleCode) {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, roleCode));
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId);
        ur.setRoleId(role.getId());
        userRoleMapper.insert(ur);
    }

    private String roleOf(Long userId) {
        SysUserRole ur = userRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (ur == null) return "ROLE_USER";
        SysRole role = roleMapper.selectById(ur.getRoleId());
        return role == null ? "ROLE_USER" : role.getCode();
    }

    private UserVO toVO(SysUser user, String role) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRole(role);
        vo.setSecurityQuestion(user.getSecurityQuestion());
        SysRole r = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, role));
        vo.setRoleName(r == null ? role : r.getName());
        return vo;
    }
}
