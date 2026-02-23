package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.CouponCreateDTO;
import com.naicha.hou.dto.CouponDTO;
import com.naicha.hou.dto.CouponQueryDTO;
import com.naicha.hou.dto.UserCouponDTO;
import com.naicha.hou.dto.UserCouponQueryDTO;
import com.naicha.hou.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 优惠券管理控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
@Validated
@Tag(name = "优惠券管理", description = "优惠券增删改查、暂停恢复等接口")
public class CouponController {

    private final CouponService couponService;

    /**
     * 创建优惠券
     */
    @PostMapping("/create")
    @Operation(summary = "创建优惠券", description = "商家创建新优惠券")
    public Result<Long> createCoupon(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Valid @RequestBody CouponCreateDTO createDTO) {
        log.info("创建优惠券请求，商家ID: {}", merchantId);

        Long couponId = couponService.createCoupon(merchantId, createDTO);
        return Result.success("优惠券创建成功", couponId);
    }

    /**
     * 更新优惠券
     */
    @PutMapping("/update/{couponId}")
    @Operation(summary = "更新优惠券", description = "更新优惠券信息")
    public Result<Boolean> updateCoupon(
            @Parameter(description = "优惠券ID", required = true)
            @PathVariable @NotNull Long couponId,
            @Valid @RequestBody CouponCreateDTO createDTO) {
        log.info("更新优惠券请求，优惠券ID: {}", couponId);

        boolean success = couponService.updateCoupon(couponId, createDTO);
        return Result.success("优惠券更新成功", success);
    }

    /**
     * 删除优惠券
     */
    @DeleteMapping("/delete/{couponId}")
    @Operation(summary = "删除优惠券", description = "删除优惠券（只能删除未开始或已结束的优惠券）")
    public Result<Boolean> deleteCoupon(
            @Parameter(description = "优惠券ID", required = true)
            @PathVariable @NotNull Long couponId) {
        log.info("删除优惠券请求，优惠券ID: {}", couponId);

        boolean success = couponService.deleteCoupon(couponId);
        return Result.success("优惠券删除成功", success);
    }

    /**
     * 查询优惠券详情
     */
    @GetMapping("/detail/{couponId}")
    @Operation(summary = "查询优惠券详情", description = "根据优惠券ID查询优惠券详细信息")
    public Result<CouponDTO> getCouponDetail(
            @Parameter(description = "优惠券ID", required = true)
            @PathVariable @NotNull Long couponId) {
        log.info("查询优惠券详情，优惠券ID: {}", couponId);

        CouponDTO couponDTO = couponService.getCouponById(couponId);
        return Result.success("查询成功", couponDTO);
    }

    /**
     * 分页查询优惠券列表（商家端和用户端通用）
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询优惠券列表", description = "根据条件分页查询优惠券列表，商家端需要商户ID，用户端可选")
    public Result<IPage<CouponDTO>> getCouponPage(
            @Parameter(description = "商家ID（商家端必填，用户端可选）")
            @RequestHeader(value = "X-Merchant-Id", required = false) Long merchantId,
            @Valid @RequestBody CouponQueryDTO queryDTO) {
        log.info("分页查询优惠券列表，商家ID: {}, 查询条件: {}", merchantId, queryDTO);

        // 如果请求头中有商户ID，设置到查询条件中；否则允许查询所有商户的优惠券
        if (merchantId != null) {
            queryDTO.setMerchantId(merchantId);
        }
        // 如果查询条件中没有设置状态，默认查询活跃状态的优惠券（用户端）
        if (queryDTO.getStatus() == null && merchantId == null) {
            queryDTO.setStatus("active");
        }

        IPage<CouponDTO> result = couponService.getCouponList(queryDTO);
        return Result.success("查询成功", result);
    }

    /**
     * 暂停/恢复优惠券
     */
    @PutMapping("/toggle-pause/{couponId}")
    @Operation(summary = "暂停/恢复优惠券", description = "暂停或恢复进行中的优惠券")
    public Result<Boolean> togglePause(
            @Parameter(description = "优惠券ID", required = true)
            @PathVariable @NotNull Long couponId) {
        log.info("暂停/恢复优惠券请求，优惠券ID: {}", couponId);

        boolean success = couponService.togglePause(couponId);
        return Result.success("操作成功", success);
    }

    /**
     * 获取我的优惠券列表（用户端）
     */
    @PostMapping("/my")
    @Operation(summary = "获取我的优惠券列表", description = "查询当前用户的优惠券列表，支持按状态筛选")
    public Result<IPage<UserCouponDTO>> getMyCoupons(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Valid @RequestBody UserCouponQueryDTO queryDTO) {
        log.info("查询我的优惠券列表，用户ID: {}, 查询条件: {}", userId, queryDTO);

        // 设置用户ID
        queryDTO.setUserId(userId);

        IPage<UserCouponDTO> result = couponService.getUserCoupons(queryDTO);
        return Result.success("查询成功", result);
    }

    /**
     * 用户领取优惠券
     */
    @PostMapping("/receive/{couponId}")
    @Operation(summary = "领取优惠券", description = "用户领取优惠券")
    public Result<Boolean> receiveCoupon(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Parameter(description = "优惠券ID", required = true)
            @PathVariable @NotNull Long couponId) {
        log.info("用户领取优惠券，用户ID: {}, 优惠券ID: {}", userId, couponId);

        boolean success = couponService.receiveCoupon(userId, couponId);
        return Result.success("优惠券领取成功", success);
    }
}

