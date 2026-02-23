package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.MerchantAuditHistoryDTO;
import com.naicha.hou.dto.MerchantAuditQueryDTO;
import com.naicha.hou.dto.MerchantAuditRequestDTO;
import com.naicha.hou.dto.MerchantDTO;
import com.naicha.hou.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 管理员商家审核控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/merchant-audit")
@RequiredArgsConstructor
@Validated
@Tag(name = "管理员商家审核", description = "管理员商家审核相关接口")
public class AdminMerchantAuditController {

    private final MerchantService merchantService;

    /**
     * 分页查询待审核商家列表
     */
    @GetMapping("/pending/list")
    @Operation(summary = "分页查询待审核商家列表", description = "根据条件分页查询待审核的商家申请")
    public Result<IPage<MerchantDTO>> getPendingAuditMerchants(@Valid MerchantAuditQueryDTO queryDTO) {
        try {
            log.info("分页查询待审核商家列表，查询条件: {}", queryDTO);
            IPage<MerchantDTO> page = merchantService.getPendingAuditMerchants(queryDTO);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            log.error("查询待审核商家列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询审核历史
     */
    @GetMapping("/history/list")
    @Operation(summary = "分页查询审核历史", description = "根据条件分页查询已审核的商家记录")
    public Result<IPage<MerchantAuditHistoryDTO>> getAuditHistory(@Valid MerchantAuditQueryDTO queryDTO) {
        try {
            log.info("分页查询审核历史，查询条件: {}", queryDTO);
            IPage<MerchantAuditHistoryDTO> page = merchantService.getAuditHistory(queryDTO);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            log.error("查询审核历史失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 审核商家
     */
    @PostMapping("/audit")
    @Operation(summary = "审核商家", description = "审核商家入驻申请（通过或拒绝）")
    public Result<Boolean> auditMerchant(
            @Parameter(description = "管理员ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long adminId,
            @Valid @RequestBody MerchantAuditRequestDTO request) {
        try {
            log.info("审核商家，管理员ID: {}, 请求: {}", adminId, request);
            boolean success = merchantService.auditMerchant(request, adminId);
            if (success) {
                return Result.success("审核成功", true);
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            log.error("审核商家失败", e);
            return Result.error("审核失败: " + e.getMessage());
        }
    }

    /**
     * 快速审核通过
     */
    @PostMapping("/approve/{merchantId}")
    @Operation(summary = "快速审核通过", description = "快速审核通过商家入驻申请")
    public Result<Boolean> approveMerchant(
            @Parameter(description = "管理员ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long adminId,
            @Parameter(description = "商家ID", required = true) 
            @PathVariable Long merchantId) {
        try {
            log.info("快速审核通过，管理员ID: {}, 商家ID: {}", adminId, merchantId);
            MerchantAuditRequestDTO request = new MerchantAuditRequestDTO();
            request.setMerchantId(merchantId);
            request.setResult("approved");
            request.setComment("审核通过");
            request.setAutoGenerateAccount(true);
            boolean success = merchantService.auditMerchant(request, adminId);
            if (success) {
                return Result.success("审核通过成功", true);
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            log.error("快速审核通过失败", e);
            return Result.error("审核失败: " + e.getMessage());
        }
    }

    /**
     * 快速审核拒绝
     */
    @PostMapping("/reject/{merchantId}")
    @Operation(summary = "快速审核拒绝", description = "快速审核拒绝商家入驻申请")
    public Result<Boolean> rejectMerchant(
            @Parameter(description = "管理员ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long adminId,
            @Parameter(description = "商家ID", required = true) 
            @PathVariable Long merchantId,
            @Parameter(description = "拒绝理由", required = true) 
            @RequestParam String reason) {
        try {
            log.info("快速审核拒绝，管理员ID: {}, 商家ID: {}, 理由: {}", adminId, merchantId, reason);
            MerchantAuditRequestDTO request = new MerchantAuditRequestDTO();
            request.setMerchantId(merchantId);
            request.setResult("rejected");
            request.setComment(reason);
            request.setRejectReason(reason);
            boolean success = merchantService.auditMerchant(request, adminId);
            if (success) {
                return Result.success("审核拒绝成功", true);
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            log.error("快速审核拒绝失败", e);
            return Result.error("审核失败: " + e.getMessage());
        }
    }
}

