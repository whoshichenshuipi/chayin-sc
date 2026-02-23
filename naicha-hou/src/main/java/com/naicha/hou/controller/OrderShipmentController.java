package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.*;
import com.naicha.hou.entity.Order;
import com.naicha.hou.mapper.OrderMapper;
import com.naicha.hou.service.OrderShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 订单配送管理控制器
 *
 * @author naicha
 * @since 2024-01-15
 */
@Slf4j
@RestController
@RequestMapping("/api/order/shipment")
@RequiredArgsConstructor
@Validated
@Tag(name = "订单配送管理", description = "订单发货、配送跟踪等接口")
public class OrderShipmentController {

    private final OrderShipmentService orderShipmentService;
    private final OrderMapper orderMapper;

    /**
     * 分页查询订单配送列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询订单配送列表", description = "根据条件分页查询订单配送列表，支持商家筛选")
    public Result<IPage<OrderShipmentDTO>> getShipmentPage(
            @Parameter(description = "商家ID", required = false) 
            @RequestHeader(value = "X-Merchant-Id", required = false) Long merchantId,
            @Valid @RequestBody OrderShipmentQueryDTO queryDTO) {
        
        log.info("分页查询订单配送列表，商家ID: {}, 查询条件: {}", merchantId, queryDTO);
        
        // 如果请求头中有商家ID，则设置商家ID（商家端）
        if (merchantId != null) {
            queryDTO.setMerchantId(merchantId);
        }
        
        IPage<OrderShipmentDTO> result = orderShipmentService.getShipmentPage(queryDTO);
        log.info("查询结果总数: {}, 当前页记录数: {}", result.getTotal(), result.getRecords().size());
        return Result.success("查询成功", result);
    }

    /**
     * 根据订单ID查询配送信息
     */
    @GetMapping("/detail/{orderId}")
    @Operation(summary = "查询订单配送信息", description = "根据订单ID查询配送详细信息")
    public Result<OrderShipmentDTO> getShipmentDetail(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull Long orderId) {
        
        log.info("查询订单配送信息，订单ID: {}", orderId);
        
        OrderShipmentDTO shipmentDTO = orderShipmentService.getShipmentByOrderId(orderId);
        if (shipmentDTO == null) {
            return Result.error(404, "配送信息不存在");
        }
        
        return Result.success("查询成功", shipmentDTO);
    }

    /**
     * 根据订单号查询配送信息
     */
    @GetMapping("/orderNo/{orderNo}")
    @Operation(summary = "根据订单号查询配送信息", description = "根据订单号查询配送详细信息")
    public Result<OrderShipmentDTO> getShipmentByOrderNo(
            @Parameter(description = "订单号", required = true) 
            @PathVariable @NotNull String orderNo) {
        
        log.info("根据订单号查询配送信息，订单号: {}", orderNo);
        
        OrderShipmentDTO shipmentDTO = orderShipmentService.getShipmentByOrderNo(orderNo);
        if (shipmentDTO == null) {
            return Result.error(404, "配送信息不存在");
        }
        
        return Result.success("查询成功", shipmentDTO);
    }

    /**
     * 订单发货
     */
    @PostMapping("/ship")
    @Operation(summary = "订单发货", description = "订单发货，更新订单状态为已发货")
    public Result<Boolean> shipOrder(
            @Parameter(description = "商家ID", required = false) 
            @RequestHeader(value = "X-Merchant-Id", required = false) Long merchantId,
            @Valid @RequestBody OrderShipRequest request) {
        
        log.info("订单发货，商家ID: {}, 订单ID: {}, 配送方式: {}", merchantId, request.getOrderId(), request.getDeliveryType());
        
        // 验证商家权限
        if (merchantId != null) {
            Order order = orderMapper.selectById(request.getOrderId());
            if (order == null) {
                return Result.error(404, "订单不存在");
            }
            if (!merchantId.equals(order.getMerchantId())) {
                log.warn("商家ID不匹配，请求商家ID: {}, 订单商家ID: {}", merchantId, order.getMerchantId());
                return Result.error(403, "无权操作此订单");
            }
        }
        
        boolean success = orderShipmentService.shipOrder(request);
        if (success) {
            return Result.success("发货成功", true);
        } else {
            return Result.error(500, "发货失败");
        }
    }

    /**
     * 批量发货
     */
    @PostMapping("/batch-ship")
    @Operation(summary = "批量发货", description = "批量发货")
    public Result<Boolean> batchShipOrder(@Valid @RequestBody BatchShipRequest request) {
        
        log.info("批量发货，订单数量: {}", request.getOrderIds().size());
        
        boolean success = orderShipmentService.batchShipOrder(request.getOrderIds(), request);
        if (success) {
            return Result.success("批量发货成功", true);
        } else {
            return Result.error(500, "批量发货失败");
        }
    }

    /**
     * 更新配送进度
     */
    @PostMapping("/progress")
    @Operation(summary = "更新配送进度", description = "更新配送进度")
    public Result<Boolean> updateShippingProgress(@Valid @RequestBody ShippingProgressUpdateRequest request) {
        
        log.info("更新配送进度，订单ID: {}", request.getOrderId());
        
        boolean success = orderShipmentService.updateShippingProgress(request);
        if (success) {
            return Result.success("更新成功", true);
        } else {
            return Result.error(500, "更新失败");
        }
    }

    /**
     * 完成配送
     */
    @PostMapping("/complete/{orderId}")
    @Operation(summary = "完成配送", description = "完成配送")
    public Result<Boolean> completeDelivery(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull Long orderId) {
        
        log.info("完成配送，订单ID: {}", orderId);
        
        boolean success = orderShipmentService.completeDelivery(orderId);
        if (success) {
            return Result.success("配送完成", true);
        } else {
            return Result.error(500, "完成配送失败");
        }
    }
}

/**
 * 批量发货请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = false)
class BatchShipRequest extends OrderShipRequest {
    private List<Long> orderIds;
}

