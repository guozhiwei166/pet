package com.tourism.service;

import com.tourism.dto.LoginDTO;
import com.tourism.dto.RegisterDTO;
import com.tourism.vo.FindUsernameVO;
import com.tourism.vo.LoginVO;
import com.tourism.vo.UserVO;

import java.util.List;

/**
 * 认证服务
 */
public interface AuthService {

    /** 注册普通用户 */
    void register(RegisterDTO dto);

    /** 注册商家(账号+商家资料,商家需管理员审核后方可经营) */
    void registerMerchant(RegisterDTO userDto, String merchantName, String contactName,
                          String contactPhone, String description);

    /** 登录,签发 token */
    LoginVO login(LoginDTO dto);

    /** 按手机号找回用户名(忘记账号场景;返回空列表表示未找到) */
    List<FindUsernameVO> findByPhone(String phone);

    /** 当前登录用户信息 */
    UserVO currentUser();

    /** 修改个人资料 */
    UserVO updateProfile(UserVO vo);

    /** 修改密码 */
    void updatePassword(String oldPassword, String newPassword);
}
