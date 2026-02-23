package com.naicha.hou.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 最小化测试控制器
 * 不依赖任何复杂配置，用于验证基本功能
 *
 * @author naicha
 * @since 2023-12-01
 */
@RestController
@RequestMapping("/minimal")
public class MinimalTestController {

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "服务正常运行");
        return response;
    }

    /**
     * 简单登录测试
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        Map<String, Object> response = new HashMap<>();
        
        String username = loginData.get("username");
        String password = loginData.get("password");
        
        if ("admin".equals(username) && "123123123".equals(password)) {
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("user", Map.of(
                "id", 1,
                "username", "admin",
                "role", "admin",
                "name", "系统管理员"
            ));
        } else {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
        }
        
        return response;
    }

    /**
     * 简单注册测试
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> registerData) {
        Map<String, Object> response = new HashMap<>();
        
        String username = registerData.get("username");
        String password = registerData.get("password");
        
        if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("user", Map.of(
                "id", System.currentTimeMillis(),
                "username", username,
                "role", "user",
                "name", "新用户"
            ));
        } else {
            response.put("success", false);
            response.put("message", "用户名和密码不能为空");
        }
        
        return response;
    }

    /**
     * 获取系统信息
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "naicha-hou");
        response.put("version", "1.0.0");
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("osName", System.getProperty("os.name"));
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
