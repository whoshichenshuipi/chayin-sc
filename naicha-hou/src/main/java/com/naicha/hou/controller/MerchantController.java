package com.naicha.hou.controller;

import com.naicha.hou.common.Result;
import com.naicha.hou.dto.BusinessHoursUpdateRequest;
import com.naicha.hou.dto.ShopInfoUpdateRequest;
import com.naicha.hou.entity.Merchant;
import com.naicha.hou.entity.MerchantSetting;
import com.naicha.hou.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * 商家管理控制器
 *
 * @author naicha
 * @since 2023-12-01
 */
@Slf4j
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
@Validated
@Tag(name = "商家管理接口", description = "商家店铺信息、营业时间等管理接口")
public class MerchantController {

    private final MerchantService merchantService;

    /**
     * 获取商家信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取商家信息", description = "获取当前登录商家的详细信息")
    public Result<Merchant> getMerchantInfo(
            @Parameter(description = "商家ID", required = true) @RequestParam Long merchantId) {
        
        Merchant merchant = merchantService.getMerchantInfo(merchantId);
        return Result.success("获取成功", merchant);
    }

    /**
     * 更新店铺信息
     */
    @PutMapping("/shop/info")
    @Operation(summary = "更新店铺信息", description = "更新店铺名称、Logo、电话、地址、简介等信息")
    public Result<Merchant> updateShopInfo(
            @Parameter(description = "商家ID", required = true) @RequestParam Long merchantId,
            @Valid @RequestBody ShopInfoUpdateRequest request) {
        
        Merchant merchant = merchantService.updateShopInfo(merchantId, request);
        return Result.success("更新成功", merchant);
    }

    /**
     * 获取营业时间设置
     */
    @GetMapping("/business-hours")
    @Operation(summary = "获取营业时间", description = "获取商家营业时间设置")
    public Result<MerchantSetting> getBusinessHours(
            @Parameter(description = "商家ID", required = true) @RequestParam Long merchantId) {
        
        MerchantSetting setting = merchantService.getBusinessHours(merchantId);
        return Result.success("获取成功", setting);
    }

    /**
     * 更新营业时间设置
     */
    @PutMapping("/business-hours")
    @Operation(summary = "更新营业时间", description = "更新商家工作日和周末的营业时间")
    public Result<MerchantSetting> updateBusinessHours(
            @Parameter(description = "商家ID", required = true) @RequestParam Long merchantId,
            @Valid @RequestBody BusinessHoursUpdateRequest request) {
        
        MerchantSetting setting = merchantService.updateBusinessHours(merchantId, request);
        return Result.success("更新成功", setting);
    }

    /**
     * 根据地址获取经纬度
     */
    @GetMapping("/location")
    @Operation(summary = "地址转坐标", description = "根据地址获取经纬度坐标")
    public Result<Map<String, Double>> getLocationByAddress(
            @Parameter(description = "地址", required = true) @RequestParam @NotBlank String address) {
        
        Double[] location = merchantService.getLocationByAddress(address);
        
        if (location == null) {
            return Result.failed("未找到该地址的坐标");
        }
        
        Map<String, Double> result = new HashMap<>();
        result.put("longitude", location[0]);
        result.put("latitude", location[1]);
        
        return Result.success("获取成功", result);
    }
}

