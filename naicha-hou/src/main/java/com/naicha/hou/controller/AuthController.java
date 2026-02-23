package com.naicha.hou.controller;

import com.naicha.hou.common.Result;
import com.naicha.hou.dto.LoginRequest;
import com.naicha.hou.dto.RegisterRequest;
import com.naicha.hou.dto.UserInfoResponse;
import com.naicha.hou.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * 认证控制器
 *
 * @author naicha
 * @since 2023-12-01
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "认证接口", description = "用户登录、注册等认证相关接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * 根据用户类型设置跳转地址：
     * - 管理员和商家：跳转到后台管理界面（3001端口）
     * - 普通用户：跳转到前台界面（3000端口）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "支持管理员、商家、普通用户登录")
    public Result<UserInfoResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求: {}, 用户类型: {}", loginRequest.getUsername(), loginRequest.getUserType());
        
        UserInfoResponse userInfo = authService.login(loginRequest);
        
        // 根据用户类型设置跳转地址
        String redirectUrl = determineRedirectUrl(userInfo.getUserType());
        userInfo.setRedirectUrl(redirectUrl);
        
        log.info("用户 {} 登录成功，跳转地址: {}", loginRequest.getUsername(), redirectUrl);
        
        return Result.success("登录成功", userInfo);
    }

    /**
     * 根据用户类型确定跳转地址
     *
     * @param userType 用户类型：admin-管理员，merchant-商家，user-普通用户
     * @return 跳转地址
     */
    private String determineRedirectUrl(String userType) {
        if (userType == null) {
            return "http://localhost:3000/home";
        }
        
        return switch (userType) {
            case "admin" -> "http://localhost:3001/admin/dashboard";
            case "merchant" -> "http://localhost:3001/merchant/dashboard";
            case "user" -> "http://localhost:3000/home";
            default -> "http://localhost:3000/home";
        };
    }

    /**
     * 用户注册
     * 根据用户类型设置跳转地址：
     * - 管理员和商家：跳转到后台管理界面（3001端口）
     * - 普通用户：跳转到前台界面（3000端口）
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "支持普通用户和商家注册")
    public Result<UserInfoResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("用户注册请求: {}, 用户类型: {}", registerRequest.getUsername(), registerRequest.getUserType());
        
        UserInfoResponse userInfo = authService.register(registerRequest);
        
        // 根据用户类型设置跳转地址
        String redirectUrl = determineRedirectUrl(userInfo.getUserType());
        userInfo.setRedirectUrl(redirectUrl);
        
        log.info("用户 {} 注册成功，跳转地址: {}", registerRequest.getUsername(), redirectUrl);
        
        return Result.success("注册成功", userInfo);
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名是否存在", description = "检查指定用户类型的用户名是否已存在")
    public Result<Boolean> checkUsername(
            @Parameter(description = "用户名", required = true) @RequestParam @NotBlank String username,
            @Parameter(description = "用户类型", required = true) @RequestParam @NotBlank String userType) {
        
        boolean exists = authService.checkUsernameExists(username, userType);
        
        return Result.success(exists ? "用户名已存在" : "用户名可用", exists);
    }

    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱是否存在", description = "检查邮箱是否已被注册")
    public Result<Boolean> checkEmail(
            @Parameter(description = "邮箱", required = true) @RequestParam @NotBlank String email) {
        
        boolean exists = authService.checkEmailExists(email);
        
        return Result.success(exists ? "邮箱已存在" : "邮箱可用", exists);
    }

    /**
     * 检查手机号是否存在
     */
    @GetMapping("/check-phone")
    @Operation(summary = "检查手机号是否存在", description = "检查手机号是否已被注册")
    public Result<Boolean> checkPhone(
            @Parameter(description = "手机号", required = true) @RequestParam @NotBlank String phone) {
        
        boolean exists = authService.checkPhoneExists(phone);
        
        return Result.success(exists ? "手机号已存在" : "手机号可用", exists);
    }

    /**
     * 检查店铺名称是否存在
     */
    @GetMapping("/check-shop-name")
    @Operation(summary = "检查店铺名称是否存在", description = "检查店铺名称是否已被使用")
    public Result<Boolean> checkShopName(
            @Parameter(description = "店铺名称", required = true) @RequestParam @NotBlank String shopName) {
        
        boolean exists = authService.checkShopNameExists(shopName);
        
        return Result.success(exists ? "店铺名称已存在" : "店铺名称可用", exists);
    }
}
