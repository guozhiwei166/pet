package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.dto.LoginDTO;
import com.tourism.dto.RegisterDTO;
import com.tourism.dto.RegisterMerchantDTO;
import com.tourism.security.Public;
import com.tourism.service.AuthService;
import com.tourism.vo.FindUsernameVO;
import com.tourism.vo.LoginVO;
import com.tourism.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证与账号接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 注册普通用户 */
    @Public
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    /** 商家入驻:注册商家账号并提交资质,等待平台审核 */
    @Public
    @PostMapping("/register-merchant")
    public Result<Void> registerMerchant(@Valid @RequestBody RegisterMerchantDTO dto) {
        authService.registerMerchant(dto.getUser(), dto.getMerchantName(),
                dto.getContactName(), dto.getContactPhone(), dto.getDescription());
        return Result.success();
    }

    /** 登录 */
    @Public
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    /** 忘记账号:按注册手机号找回用户名(未找到返回空列表) */
    @Public
    @GetMapping("/find-username")
    public Result<List<FindUsernameVO>> findUsername(@RequestParam String phone) {
        return Result.success(authService.findByPhone(phone));
    }

    /** 当前登录用户信息 */
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.success(authService.currentUser());
    }
}
