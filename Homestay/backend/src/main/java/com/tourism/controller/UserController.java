package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.dto.ChangePasswordDTO;
import com.tourism.service.AuthService;
import com.tourism.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户中心(个人资料、密码)—— 仅登录用户
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    /** 查看个人信息 */
    @GetMapping("/info")
    public Result<UserVO> info() {
        return Result.success(authService.currentUser());
    }

    /** 修改个人资料(昵称/手机/邮箱/头像/性别) */
    @PutMapping("/info")
    public Result<UserVO> updateInfo(@RequestBody UserVO vo) {
        return Result.success(authService.updateProfile(vo));
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        authService.updatePassword(dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }
}
