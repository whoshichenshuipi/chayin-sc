package com.naicha.hou.controller;

import com.naicha.hou.common.Result;
import com.naicha.hou.dto.SystemConfigDTO;
import com.naicha.hou.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 系统配置控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system-config")
@RequiredArgsConstructor
@Validated
@Tag(name = "系统配置接口", description = "系统配置管理相关接口")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * 获取基础参数配置
     */
    @GetMapping("/basic")
    @Operation(summary = "获取基础参数配置", description = "获取系统基础参数配置")
    public Result<SystemConfigDTO.BasicConfig> getBasicConfig() {
        try {
            SystemConfigDTO.BasicConfig config = systemConfigService.getBasicConfig();
            return Result.success("获取配置成功", config);
        } catch (Exception e) {
            log.error("获取基础配置失败", e);
            return Result.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存基础参数配置
     */
    @PostMapping("/basic")
    @Operation(summary = "保存基础参数配置", description = "保存系统基础参数配置")
    public Result<Boolean> saveBasicConfig(@Valid @RequestBody SystemConfigDTO.BasicConfig config) {
        try {
            boolean success = systemConfigService.saveBasicConfig(config);
            if (success) {
                return Result.success("基础配置保存成功", true);
            } else {
                return Result.error("基础配置保存失败");
            }
        } catch (Exception e) {
            log.error("保存基础配置失败", e);
            return Result.error("保存配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取支付方式配置
     */
    @GetMapping("/payment")
    @Operation(summary = "获取支付方式配置", description = "获取支付方式配置")
    public Result<SystemConfigDTO.PaymentConfig> getPaymentConfig() {
        try {
            SystemConfigDTO.PaymentConfig config = systemConfigService.getPaymentConfig();
            return Result.success("获取配置成功", config);
        } catch (Exception e) {
            log.error("获取支付配置失败", e);
            return Result.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存支付方式配置
     */
    @PostMapping("/payment")
    @Operation(summary = "保存支付方式配置", description = "保存支付方式配置")
    public Result<Boolean> savePaymentConfig(@Valid @RequestBody SystemConfigDTO.PaymentConfig config) {
        try {
            boolean success = systemConfigService.savePaymentConfig(config);
            if (success) {
                return Result.success("支付配置保存成功", true);
            } else {
                return Result.error("支付配置保存失败");
            }
        } catch (Exception e) {
            log.error("保存支付配置失败", e);
            return Result.error("保存配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取配送规则配置
     */
    @GetMapping("/delivery")
    @Operation(summary = "获取配送规则配置", description = "获取配送规则配置")
    public Result<SystemConfigDTO.DeliveryConfig> getDeliveryConfig() {
        try {
            SystemConfigDTO.DeliveryConfig config = systemConfigService.getDeliveryConfig();
            return Result.success("获取配置成功", config);
        } catch (Exception e) {
            log.error("获取配送配置失败", e);
            return Result.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存配送规则配置
     */
    @PostMapping("/delivery")
    @Operation(summary = "保存配送规则配置", description = "保存配送规则配置")
    public Result<Boolean> saveDeliveryConfig(@Valid @RequestBody SystemConfigDTO.DeliveryConfig config) {
        try {
            boolean success = systemConfigService.saveDeliveryConfig(config);
            if (success) {
                return Result.success("配送配置保存成功", true);
            } else {
                return Result.error("配送配置保存失败");
            }
        } catch (Exception e) {
            log.error("保存配送配置失败", e);
            return Result.error("保存配置失败: " + e.getMessage());
        }
    }

    /**
     * 测试支付连接
     */
    @PostMapping("/payment/test")
    @Operation(summary = "测试支付连接", description = "测试支付连接是否正常")
    public Result<Boolean> testPaymentConnection() {
        try {
            boolean success = systemConfigService.testPaymentConnection();
            if (success) {
                return Result.success("支付连接测试成功", true);
            } else {
                return Result.error("支付连接测试失败");
            }
        } catch (Exception e) {
            log.error("测试支付连接失败", e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }
}

