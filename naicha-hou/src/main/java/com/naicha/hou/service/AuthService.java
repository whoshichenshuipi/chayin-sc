package com.naicha.hou.service;

import com.naicha.hou.dto.LoginRequest;
import com.naicha.hou.dto.RegisterRequest;
import com.naicha.hou.dto.UserInfoResponse;

/**
 * 认证服务接口
 *
 * @author naicha
 * @since 2023-12-01
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 用户信息
     */
    UserInfoResponse login(LoginRequest loginRequest);

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 用户信息
     */
    UserInfoResponse register(RegisterRequest registerRequest);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @param userType 用户类型
     * @return 是否存在
     */
    boolean checkUsernameExists(String username, String userType);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean checkEmailExists(String email);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return 是否存在
     */
    boolean checkPhoneExists(String phone);

    /**
     * 检查店铺名称是否存在
     *
     * @param shopName 店铺名称
     * @return 是否存在
     */
    boolean checkShopNameExists(String shopName);
}
