package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.OrderDTO;
import com.naicha.hou.dto.OrderProcessDTO;
import com.naicha.hou.dto.OrderProcessQueryDTO;
import com.naicha.hou.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 订单处理控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/order/process")
@RequiredArgsConstructor
@Validated
@Tag(name = "订单处理", description = "订单处理相关接口")
public class OrderProcessController {

    private final OrderService orderService;

    /**
     * 分页查询待处理订单列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询待处理订单列表", description = "查询需要处理的订单列表")
    public Result<IPage<OrderDTO>> getPendingOrderPage(
            @Parameter(description = "商家ID", required = true) 
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Valid @RequestBody OrderProcessQueryDTO queryDTO) {
        
        log.info("分页查询待处理订单列表，商家ID: {}, 查询条件: {}", merchantId, queryDTO);
        
        // 设置商家ID
        queryDTO.setMerchantId(merchantId);
        
        IPage<OrderDTO> result = orderService.getPendingOrderPage(queryDTO);
        return Result.success("查询成功", result);
    }

    /**
     * 接单处理
     */
    @PostMapping("/accept")
    @Operation(summary = "接单处理", description = "将已支付订单标记为已接单")
    public Result<Boolean> acceptOrder(
            @Valid @RequestBody OrderProcessDTO processDTO) {
        
        log.info("接单处理，订单ID: {}", processDTO.getOrderId());
        
        boolean success = orderService.acceptOrder(processDTO);
        if (success) {
            return Result.success("接单成功", true);
        } else {
            return Result.error(500, "接单失败");
        }
    }

    /**
     * 开始制作
     */
    @PostMapping("/start-making")
    @Operation(summary = "开始制作", description = "将已接单订单标记为制作中")
    public Result<Boolean> startMaking(
            @Valid @RequestBody OrderProcessDTO processDTO) {
        
        log.info("开始制作，订单ID: {}", processDTO.getOrderId());
        
        boolean success = orderService.startMaking(processDTO);
        if (success) {
            return Result.success("开始制作", true);
        } else {
            return Result.error(500, "开始制作失败");
        }
    }

    /**
     * 制作完成
     */
    @PostMapping("/finish-making")
    @Operation(summary = "制作完成", description = "将制作中订单标记为已发货")
    public Result<Boolean> finishMaking(
            @Valid @RequestBody OrderProcessDTO processDTO) {
        
        log.info("制作完成，订单ID: {}", processDTO.getOrderId());
        
        boolean success = orderService.finishMaking(processDTO);
        if (success) {
            return Result.success("制作完成", true);
        } else {
            return Result.error(500, "制作完成失败");
        }
    }

    /**
     * 批量处理订单
     */
    @PostMapping("/batch")
    @Operation(summary = "批量处理订单", description = "批量处理多个订单")
    public Result<Boolean> batchProcessOrders(
            @Parameter(description = "订单ID列表", required = true) 
            @RequestBody @NotEmpty List<Long> orderIds,
            @Parameter(description = "处理类型 1-接单 2-开始制作 3-制作完成", required = true) 
            @RequestParam @NotNull Integer processType) {
        
        log.info("批量处理订单，订单数量: {}, 处理类型: {}", orderIds.size(), processType);
        
        boolean success = orderService.batchProcessOrders(orderIds, processType);
        if (success) {
            return Result.success("批量处理成功", true);
        } else {
            return Result.error(500, "批量处理失败");
        }
    }

    /**
     * 获取订单处理统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取订单处理统计", description = "获取订单处理统计信息")
    public Result<Object> getOrderProcessStatistics(
            @Parameter(description = "商家ID", required = true) 
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId) {
        
        log.info("获取订单处理统计信息，商家ID: {}", merchantId);
        
        Object statistics = orderService.getOrderProcessStatistics(merchantId);
        return Result.success("查询成功", statistics);
    }
}
