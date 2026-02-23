package com.naicha.hou.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 纯测试控制器
 * 完全不依赖任何Service、Mapper或数据库
 *
 * @author naicha
 * @since 2023-12-01
 */
@RestController
@RequestMapping("/pure")
public class PureTestController {

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "纯测试服务正常运行");
        response.put("version", "1.0.0");
        return response;
    }

    /**
     * 系统信息
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "naicha-hou");
        response.put("version", "1.0.0");
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("osName", System.getProperty("os.name"));
        response.put("osVersion", System.getProperty("os.version"));
        response.put("userDir", System.getProperty("user.dir"));
        response.put("timestamp", LocalDateTime.now());
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
        String userType = loginData.get("userType");
        
        // 简单的硬编码验证
        boolean isValid = false;
        String message = "";
        
        if ("admin".equals(username) && "123123123".equals(password) && "admin".equals(userType)) {
            isValid = true;
            message = "管理员登录成功";
        } else if ("merchant".equals(username) && "123123123".equals(password) && "merchant".equals(userType)) {
            isValid = true;
            message = "商家登录成功";
        } else if ("user".equals(username) && "123456".equals(password) && "user".equals(userType)) {
            isValid = true;
            message = "用户登录成功";
        } else {
            message = "用户名或密码错误";
        }
        
        response.put("success", isValid);
        response.put("message", message);
        
        if (isValid) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", 1L);
            userInfo.put("username", username);
            userInfo.put("userType", userType);
            userInfo.put("name", getDisplayName(userType));
            userInfo.put("token", "test_token_" + System.currentTimeMillis());
            response.put("user", userInfo);
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
        String userType = registerData.get("userType");
        
        // 简单的注册逻辑
        if (username != null && password != null && userType != null && 
            !username.isEmpty() && !password.isEmpty() && !userType.isEmpty()) {
            
            response.put("success", true);
            response.put("message", "注册成功");
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", System.currentTimeMillis());
            userInfo.put("username", username);
            userInfo.put("userType", userType);
            userInfo.put("name", getDisplayName(userType));
            userInfo.put("token", "test_token_" + System.currentTimeMillis());
            response.put("user", userInfo);
        } else {
            response.put("success", false);
            response.put("message", "用户名、密码和用户类型不能为空");
        }
        
        return response;
    }

    /**
     * 测试数据接口
     */
    @GetMapping("/test-data")
    public Map<String, Object> testData() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "测试数据接口");
        response.put("data", Map.of(
            "users", 100,
            "orders", 500,
            "products", 50
        ));
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * 测试用户列表
     */
    @GetMapping("/users")
    public Map<String, Object> getUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "用户列表");
        response.put("users", java.util.List.of(
            Map.of("id", 1, "username", "admin", "name", "系统管理员", "role", "admin"),
            Map.of("id", 2, "username", "merchant", "name", "商家用户", "role", "merchant"),
            Map.of("id", 3, "username", "user", "name", "普通用户", "role", "user")
        ));
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * 测试商品列表
     */
    @GetMapping("/products")
    public Map<String, Object> getProducts() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "商品列表");
        response.put("products", java.util.List.of(
            Map.of("id", 1, "name", "珍珠奶茶", "price", 15.0, "category", "奶茶"),
            Map.of("id", 2, "name", "柠檬茶", "price", 12.0, "category", "果茶"),
            Map.of("id", 3, "name", "咖啡", "price", 18.0, "category", "咖啡")
        ));
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * 获取显示名称
     */
    private String getDisplayName(String userType) {
        return switch (userType) {
            case "admin" -> "系统管理员";
            case "merchant" -> "商家用户";
            case "user" -> "普通用户";
            default -> "未知用户";
        };
    }
}
