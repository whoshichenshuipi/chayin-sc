package com.naicha.hou.controller;

import com.naicha.hou.common.Result;
import com.naicha.hou.dto.ReviewReplyDTO;
import com.naicha.hou.dto.ShopInfoDTO;
import com.naicha.hou.dto.ShopInfoUpdateDTO;
import com.naicha.hou.dto.ShopStatusUpdateDTO;
import com.naicha.hou.entity.ShopReview;
import com.naicha.hou.service.ShopInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 店铺信息管理控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/shop/info")
@RequiredArgsConstructor
@Tag(name = "店铺信息管理", description = "店铺信息管理相关接口")
public class ShopInfoController {

    private final ShopInfoService shopInfoService;

    /**
     * 获取店铺完整信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取店铺信息", description = "获取店铺的完整信息，包括基础信息、营业状态、公告、营业时间等")
    public Result<ShopInfoDTO> getShopInfo(
            @Parameter(description = "商家ID（从token中获取）")
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId) {
        
        try {
            ShopInfoDTO shopInfo = shopInfoService.getShopInfo(merchantId);
            return Result.success(shopInfo);
        } catch (Exception e) {
            log.error("获取店铺信息失败: {}", e.getMessage(), e);
            return Result.error(500, "获取店铺信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新店铺基础信息
     */
    @PostMapping("/update")
    @Operation(summary = "更新店铺基础信息", description = "更新店铺名称、Logo、简介、联系电话、地址、营业时间等")
    public Result<String> updateShopInfo(
            @Parameter(description = "商家ID（从token中获取）")
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Valid @RequestBody ShopInfoUpdateDTO request) {
        
        try {
            shopInfoService.updateShopBasicInfo(
                    merchantId,
                    request.getShopName(),
                    request.getShopLogo(),
                    request.getDescription(),
                    request.getContactPhone(),
                    request.getAddress(),
                    request.getLongitude(),
                    request.getLatitude(),
                    request.getWeekday() != null ? request.getWeekday().getOpenTime() : null,
                    request.getWeekday() != null ? request.getWeekday().getCloseTime() : null,
                    request.getWeekend() != null ? request.getWeekend().getOpenTime() : null,
                    request.getWeekend() != null ? request.getWeekend().getCloseTime() : null
            );
            
            return Result.success("店铺信息更新成功");
        } catch (Exception e) {
            log.error("更新店铺信息失败: {}", e.getMessage(), e);
            return Result.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新店铺营业状态和公告
     */
    @PostMapping("/status/update")
    @Operation(summary = "更新店铺营业状态", description = "更新店铺营业状态和公告")
    public Result<String> updateShopStatus(
            @Parameter(description = "商家ID（从token中获取）")
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Valid @RequestBody ShopStatusUpdateDTO request) {
        
        try {
            shopInfoService.updateShopStatus(
                    merchantId,
                    request.getBusinessStatus(),
                    request.getAnnouncement()
            );
            
            return Result.success("店铺状态更新成功");
        } catch (Exception e) {
            log.error("更新店铺状态失败: {}", e.getMessage(), e);
            return Result.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取店铺评分统计
     */
    @GetMapping("/rating/stats")
    @Operation(summary = "获取店铺评分统计", description = "获取店铺的综合评分、商品质量评分、配送速度评分、服务态度评分等统计信息")
    public Result<Map<String, Object>> getRatingStats(
            @Parameter(description = "商家ID（从token中获取）")
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId) {
        
        try {
            Map<String, Object> stats = shopInfoService.getRatingStats(merchantId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取评分统计失败: {}", e.getMessage(), e);
            return Result.error(500, "获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取评价列表
     */
    @GetMapping("/reviews")
    @Operation(summary = "获取评价列表", description = "获取店铺的评价列表，支持按评分筛选")
    public Result<List<ShopReview>> getReviewList(
            @Parameter(description = "商家ID（从token中获取）")
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Parameter(description = "评分筛选（可选）")
            @RequestParam(required = false) Integer rating) {
        
        try {
            List<ShopReview> reviews = shopInfoService.getReviewList(merchantId, rating);
            return Result.success(reviews);
        } catch (Exception e) {
            log.error("获取评价列表失败: {}", e.getMessage(), e);
            return Result.error(500, "获取失败: " + e.getMessage());
        }
    }

    /**
     * 回复评价
     */
    @PostMapping("/review/reply")
    @Operation(summary = "回复评价", description = "商家回复用户评价")
    public Result<String> replyReview(
            @Parameter(description = "商家ID（从token中获取）")
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Valid @RequestBody ReviewReplyDTO request) {
        
        try {
            shopInfoService.replyReview(merchantId, request.getReviewId(), request.getReply());
            return Result.success("回复成功");
        } catch (Exception e) {
            log.error("回复评价失败: {}", e.getMessage(), e);
            return Result.error(500, "回复失败: " + e.getMessage());
        }
    }
}
