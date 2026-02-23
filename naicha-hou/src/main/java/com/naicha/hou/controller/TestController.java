package com.naicha.hou.controller;

import com.naicha.hou.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 *
 * @author naicha
 * @since 2023-12-01
 */
@Slf4j
@RestController
@RequestMapping("/test")
@Tag(name = "测试接口", description = "用于测试系统是否正常运行")
public class TestController {

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查系统是否正常运行")
    public Result<Map<String, Object>> health() {
        log.info("健康检查接口被调用");
        
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("service", "naicha-hou");
        data.put("version", "1.0.0");
        
        return Result.success("系统运行正常", data);
    }

    /**
     * 获取系统信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取系统信息", description = "获取系统基本信息")
    public Result<Map<String, Object>> info() {
        log.info("获取系统信息接口被调用");
        
        Map<String, Object> data = new HashMap<>();
        data.put("application", "naicha-hou");
        data.put("version", "1.0.0");
        data.put("description", "奶茶小程序后端服务");
        data.put("author", "naicha");
        data.put("javaVersion", System.getProperty("java.version"));
        data.put("osName", System.getProperty("os.name"));
        data.put("osVersion", System.getProperty("os.version"));
        
        return Result.success(data);
    }
}
