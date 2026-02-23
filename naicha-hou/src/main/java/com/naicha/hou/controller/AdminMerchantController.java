package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.MerchantDTO;
import com.naicha.hou.dto.MerchantQueryDTO;
import com.naicha.hou.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 管理员商家管理控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/merchant")
@RequiredArgsConstructor
@Validated
@Tag(name = "管理员商家管理", description = "管理员商家管理相关接口")
public class AdminMerchantController {

    private final MerchantService merchantService;

    /**
     * 分页查询商家列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询商家列表", description = "根据条件分页查询商家列表")
    public Result<IPage<MerchantDTO>> getMerchantList(@Valid MerchantQueryDTO queryDTO) {
        try {
            log.info("分页查询商家列表，查询条件: {}", queryDTO);
            IPage<MerchantDTO> page = merchantService.getMerchantPage(queryDTO);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            log.error("查询商家列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取商家详情
     */
    @GetMapping("/{merchantId}")
    @Operation(summary = "获取商家详情", description = "根据商家ID获取商家详细信息")
    public Result<MerchantDTO> getMerchantDetail(
            @Parameter(description = "商家ID", required = true) 
            @PathVariable @NotNull Long merchantId) {
        try {
            log.info("获取商家详情，商家ID: {}", merchantId);
            MerchantDTO merchant = merchantService.getMerchantDetail(merchantId);
            return Result.success("查询成功", merchant);
        } catch (Exception e) {
            log.error("查询商家详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新商家状态
     */
    @PutMapping("/{merchantId}/status")
    @Operation(summary = "更新商家状态", description = "更新商家状态（0-待审核 1-已审核 2-已拒绝 3-已禁用）")
    public Result<Boolean> updateMerchantStatus(
            @Parameter(description = "商家ID", required = true) 
            @PathVariable @NotNull Long merchantId,
            @Parameter(description = "状态", required = true) 
            @RequestParam @NotNull Integer status) {
        try {
            log.info("更新商家状态，商家ID: {}, 状态: {}", merchantId, status);
            boolean success = merchantService.updateMerchantStatus(merchantId, status);
            if (success) {
                return Result.success("更新成功", true);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新商家状态失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }
}

