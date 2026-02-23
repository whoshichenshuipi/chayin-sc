package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.*;
import com.naicha.hou.service.OrderRefundService;
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
 * 订单退款管理控制器
 *
 * @author naicha
 * @since 2024-01-15
 */
@Slf4j
@RestController
@RequestMapping("/api/order/refund")
@RequiredArgsConstructor
@Validated
@Tag(name = "订单退款管理", description = "订单取消、退款申请等接口")
public class OrderRefundController {

    private final OrderRefundService orderRefundService;

    /**
     * 分页查询订单退款申请列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询订单退款申请列表", description = "根据条件分页查询订单退款申请列表")
    public Result<IPage<OrderRefundDTO>> getRefundPage(
            @Parameter(description = "商家ID", required = true) 
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Valid @RequestBody OrderRefundQueryDTO queryDTO) {
        
        log.info("分页查询订单退款申请列表，商家ID: {}, 查询条件: {}", merchantId, queryDTO);
        
        // 设置商家ID
        queryDTO.setMerchantId(merchantId);
        
        IPage<OrderRefundDTO> result = orderRefundService.getRefundPage(queryDTO);
        return Result.success("查询成功", result);
    }

    /**
     * 根据ID查询订单退款申请详情
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "查询订单退款申请详情", description = "根据ID查询订单退款申请详细信息")
    public Result<OrderRefundDTO> getRefundDetail(
            @Parameter(description = "退款申请ID", required = true) 
            @PathVariable @NotNull Long id) {
        
        log.info("查询订单退款申请详情，退款申请ID: {}", id);
        
        OrderRefundDTO refundDTO = orderRefundService.getRefundDetailById(id);
        if (refundDTO == null) {
            return Result.error(404, "退款申请不存在");
        }
        
        return Result.success("查询成功", refundDTO);
    }

    /**
     * 根据订单ID查询订单退款申请详情
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "根据订单ID查询退款申请", description = "根据订单ID查询订单退款申请详细信息")
    public Result<OrderRefundDTO> getRefundByOrderId(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull Long orderId) {
        
        log.info("根据订单ID查询退款申请，订单ID: {}", orderId);
        
        OrderRefundDTO refundDTO = orderRefundService.getRefundDetailByOrderId(orderId);
        if (refundDTO == null) {
            return Result.error(404, "退款申请不存在");
        }
        
        return Result.success("查询成功", refundDTO);
    }

    /**
     * 提交订单退款申请
     */
    @PostMapping("/submit")
    @Operation(summary = "提交订单退款申请", description = "提交订单退款申请")
    public Result<Boolean> submitRefundRequest(@Valid @RequestBody OrderRefundRequestDTO requestDTO) {
        
        log.info("提交订单退款申请，订单ID: {}", requestDTO.getOrderId());
        
        boolean success = orderRefundService.submitRefundRequest(requestDTO);
        if (success) {
            return Result.success("提交成功", true);
        } else {
            return Result.error(500, "提交失败");
        }
    }

    /**
     * 处理订单退款申请
     */
    @PostMapping("/process")
    @Operation(summary = "处理订单退款申请", description = "同意或拒绝订单退款申请")
    public Result<Boolean> processRefundRequest(@Valid @RequestBody OrderRefundProcessRequestDTO requestDTO) {
        
        log.info("处理订单退款申请，退款申请ID: {}, 处理动作: {}", requestDTO.getRefundId(), requestDTO.getAction());
        
        boolean success = orderRefundService.processRefundRequest(requestDTO);
        if (success) {
            String message = "approve".equals(requestDTO.getAction()) ? "处理成功" : "拒绝成功";
            return Result.success(message, true);
        } else {
            return Result.error(500, "处理失败");
        }
    }

    /**
     * 批量处理订单退款申请
     */
    @PostMapping("/batch-process")
    @Operation(summary = "批量处理订单退款申请", description = "批量同意或拒绝订单退款申请")
    public Result<Boolean> batchProcessRefundRequest(@Valid @RequestBody BatchOrderRefundProcessRequestDTO requestDTO) {
        
        log.info("批量处理订单退款申请，申请数量: {}, 处理动作: {}", requestDTO.getRefundIds().size(), requestDTO.getAction());
        
        boolean success = orderRefundService.batchProcessRefundRequest(requestDTO);
        if (success) {
            return Result.success("批量处理成功", true);
        } else {
            return Result.error(500, "批量处理失败");
        }
    }

    /**
     * 获取退款统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取退款统计数据", description = "获取退款相关的统计数据")
    public Result<OrderRefundStatsDTO> getRefundStats(
            @Parameter(description = "商家ID", required = true) 
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId) {
        
        log.info("获取退款统计数据，商家ID: {}", merchantId);
        
        OrderRefundStatsDTO stats = orderRefundService.getRefundStats(merchantId);
        return Result.success("查询成功", stats);
    }

    /**
     * 完成退款
     */
    @PostMapping("/complete/{refundId}")
    @Operation(summary = "完成退款", description = "完成退款操作")
    public Result<Boolean> completeRefund(
            @Parameter(description = "退款申请ID", required = true) 
            @PathVariable @NotNull Long refundId) {
        
        log.info("完成退款，退款申请ID: {}", refundId);
        
        boolean success = orderRefundService.completeRefund(refundId);
        if (success) {
            return Result.success("退款完成", true);
        } else {
            return Result.error(500, "完成退款失败");
        }
    }
}
