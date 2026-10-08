package com.petcare.user.controller;

import com.petcare.common.OperationLog;
import com.petcare.common.Result;
import com.petcare.security.LoginUser;
import com.petcare.security.SecurityUtils;
import com.petcare.user.dto.LoginDTO;
import com.petcare.user.dto.PasswordDTO;
import com.petcare.user.dto.ProfileDTO;
import com.petcare.user.dto.RegisterDTO;
import com.petcare.user.dto.SecurityQuestionDTO;
import com.petcare.user.service.AuthService;
import com.petcare.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/captcha")
    public Result<Map<String, Object>> captcha() {
        return Result.ok(authService.captcha());
    }

    /** 仅校验验证码（供注册分步/找回密码在提交前即时校验） */
    @PostMapping("/captcha/verify")
    public Result<Void> verifyCaptcha(@RequestBody Map<String, String> body) {
        authService.verifyCaptchaPublic(body.get("captchaId"), body.get("captchaCode"));
        return Result.ok();
    }

    @PostMapping("/register")
    @OperationLog(module = "用户", action = "注册")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok(authService.register(dto));
    }

    @PostMapping("/login")
    @OperationLog(module = "用户", action = "登录")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PostMapping("/logout")
    @OperationLog(module = "用户", action = "退出")
    public Result<Void> logout(@RequestHeader("Authorization") String auth) {
        authService.logout(auth.substring(7));
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(authService.me(SecurityUtils.currentId()));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileDTO dto) {
        return Result.ok(authService.updateProfile(SecurityUtils.currentId(), dto));
    }

    @PostMapping("/reset-password")
    public Result<Void> resetByPhone(@RequestBody Map<String, String> body) {
        authService.resetByPhone(body.get("phone"), body.get("captchaId"),
                body.get("captchaCode"), body.get("newPassword"), body.get("securityAnswer"));
        return Result.ok();
    }

    /** 设置/修改密保问题与答案 */
    @PostMapping("/security-question")
    public Result<Void> saveSecurityQuestion(@Valid @RequestBody SecurityQuestionDTO dto) {
        authService.saveSecurityQuestion(SecurityUtils.currentId(), dto.question(), dto.answer());
        return Result.ok();
    }

    @PutMapping("/password")
    @OperationLog(module = "用户", action = "修改密码")
    public Result<Void> changePassword(@Valid @RequestBody PasswordDTO dto) {
        authService.changePassword(SecurityUtils.currentId(), dto);
        return Result.ok();
    }
}
