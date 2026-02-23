package com.naicha.hou.controller;

import com.naicha.hou.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 简化认证控制器（用于测试）
 *
 * @author naicha
 * @since 2023-12-01
 */
@Slf4j
@RestController
@RequestMapping("/simple-auth")
@Tag(name = "简化认证接口", description = "用于测试的简化认证接口")
public class SimpleAuthController {

    /**
     * 简单登录测试
     */
    @PostMapping("/login")
    @Operation(summary = "简单登录测试", description = "用于测试登录功能")
    public Result<Map<String, Object>> simpleLogin(@RequestBody Map<String, String> loginData) {
        log.info("简单登录请求: {}", loginData);
        
        String username = loginData.get("username");
        String password = loginData.get("password");
        String userType = loginData.get("userType");
        
        // 简单的硬编码验证
        boolean isValid = false;
        if ("admin".equals(username) && "123123123".equals(password) && "admin".equals(userType)) {
            isValid = true;
        } else if ("merchant".equals(username) && "123123123".equals(password) && "merchant".equals(userType)) {
            isValid = true;
        }
        
        if (isValid) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", 1L);
            userInfo.put("username", username);
            userInfo.put("userType", userType);
            userInfo.put("name", "admin".equals(userType) ? "系统管理员" : "商家用户");
            
            return Result.success("登录成功", userInfo);
        } else {
            return Result.failed("用户名或密码错误");
        }
    }

    /**
     * 简单注册测试
     */
    @PostMapping("/register")
    @Operation(summary = "简单注册测试", description = "用于测试注册功能")
    public Result<Map<String, Object>> simpleRegister(@RequestBody Map<String, String> registerData) {
        log.info("简单注册请求: {}", registerData);
        
        String username = registerData.get("username");
        String password = registerData.get("password");
        String userType = registerData.get("userType");
        
        // 简单的注册逻辑
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", System.currentTimeMillis());
        userInfo.put("username", username);
        userInfo.put("userType", userType);
        userInfo.put("name", "user".equals(userType) ? "普通用户" : "商家用户");
        
        return Result.success("注册成功", userInfo);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务是否正常")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "naicha-hou");
        data.put("timestamp", System.currentTimeMillis());
        
        return Result.success("服务正常", data);
    }
}
