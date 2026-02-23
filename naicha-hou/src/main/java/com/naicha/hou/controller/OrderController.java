package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.OrderCreateDTO;
import com.naicha.hou.dto.OrderDTO;
import com.naicha.hou.dto.OrderPayDTO;
import com.naicha.hou.dto.OrderQueryDTO;
import com.naicha.hou.dto.OrderStatusUpdateDTO;
import com.naicha.hou.service.OrderService;
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
 * 订单管理控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Validated
@Tag(name = "订单管理", description = "订单查询、状态更新等接口")
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    @Operation(summary = "创建订单", description = "用户创建新订单")
    public Result<OrderDTO> createOrder(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Valid @RequestBody OrderCreateDTO createDTO) {
        
        log.info("创建订单，用户ID: {}, 订单数据: {}", userId, createDTO);
        
        Long orderId = orderService.createOrder(userId, createDTO);
        
        // 返回订单详情
        OrderDTO orderDTO = orderService.getOrderDetail(orderId);
        return Result.success("订单创建成功", orderDTO);
    }

    /**
     * 分页查询订单列表
     * 支持用户端和商家端两种查询方式：
     * - 用户端：通过 X-User-Id 查询该用户的所有订单
     * - 商家端：通过 X-Merchant-Id 查询该商家的所有订单
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询订单列表", description = "根据条件分页查询订单列表，支持用户端和商家端查询")
    public Result<IPage<OrderDTO>> getOrderPage(
            @Parameter(description = "商家ID", required = false) 
            @RequestHeader(value = "X-Merchant-Id", required = false) Long merchantId,
            @Parameter(description = "用户ID", required = false)
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody OrderQueryDTO queryDTO) {
        
        log.info("分页查询订单列表，请求头-商家ID: {}, 请求头-用户ID: {}, 查询条件: {}", merchantId, userId, queryDTO);
        
        // 如果请求头中有商家ID，则按商家查询（商家端）- 优先级最高
        if (merchantId != null) {
            queryDTO.setMerchantId(merchantId);
            queryDTO.setUserId(null); // 商家查询时，清除用户ID条件
            log.info("设置商家查询条件，merchantId: {}", merchantId);
        } else if (userId != null) {
            // 如果请求头中有用户ID，则按用户查询（用户端）
            queryDTO.setUserId(userId);
            queryDTO.setMerchantId(null); // 用户查询时，清除商家ID条件
            log.info("设置用户查询条件，userId: {}", userId);
        }
        
        log.info("最终查询条件 - merchantId: {}, userId: {}, orderNo: {}, status: {}", 
            queryDTO.getMerchantId(), queryDTO.getUserId(), queryDTO.getOrderNo(), queryDTO.getStatus());
        
        IPage<OrderDTO> result = orderService.getOrderPage(queryDTO);
        log.info("查询结果总数: {}, 当前页记录数: {}", result.getTotal(), result.getRecords().size());
        return Result.success("查询成功", result);
    }

    /**
     * 根据订单ID查询订单详情
     * 支持用户端和商家端两种查询方式：
     * - 用户端：通过 X-User-Id 验证订单是否属于该用户
     * - 商家端：通过 X-Merchant-Id 验证订单是否属于该商家
     */
    @GetMapping("/detail/{orderId}")
    @Operation(summary = "查询订单详情", description = "根据订单ID查询订单详细信息")
    public Result<OrderDTO> getOrderDetail(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull Long orderId,
            @Parameter(description = "商家ID", required = false) 
            @RequestHeader(value = "X-Merchant-Id", required = false) Long merchantId,
            @Parameter(description = "用户ID", required = false)
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("查询订单详情，订单ID: {}, 商家ID: {}, 用户ID: {}", orderId, merchantId, userId);
        
        OrderDTO orderDTO = orderService.getOrderDetail(orderId);
        if (orderDTO == null) {
            return Result.error(404, "订单不存在");
        }
        
        // 权限验证：如果请求头中有商家ID，验证订单是否属于该商家
        if (merchantId != null) {
            if (!merchantId.equals(orderDTO.getMerchantId())) {
                log.warn("商家ID不匹配，请求商家ID: {}, 订单商家ID: {}", merchantId, orderDTO.getMerchantId());
                return Result.error(403, "无权访问此订单");
            }
        }
        
        // 权限验证：如果请求头中有用户ID，验证订单是否属于该用户
        if (userId != null) {
            if (!userId.equals(orderDTO.getUserId())) {
                log.warn("用户ID不匹配，请求用户ID: {}, 订单用户ID: {}", userId, orderDTO.getUserId());
                return Result.error(403, "无权访问此订单");
            }
        }
        
        return Result.success("查询成功", orderDTO);
    }

    /**
     * 根据订单号查询订单
     */
    @GetMapping("/search/{orderNo}")
    @Operation(summary = "根据订单号查询订单", description = "根据订单号查询订单信息")
    public Result<OrderDTO> getOrderByOrderNo(
            @Parameter(description = "订单号", required = true) 
            @PathVariable @NotNull String orderNo) {
        
        log.info("根据订单号查询订单，订单号: {}", orderNo);
        
        OrderDTO orderDTO = orderService.getOrderByOrderNo(orderNo);
        if (orderDTO == null) {
            return Result.error(404, "订单不存在");
        }
        
        return Result.success("查询成功", orderDTO);
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay/{orderId}")
    @Operation(summary = "支付订单", description = "模拟支付订单，将订单状态从待支付更新为已支付")
    public Result<OrderDTO> payOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId,
            @Valid @RequestBody OrderPayDTO payDTO) {
        
        log.info("支付订单，订单ID: {}, 支付方式: {}", orderId, payDTO.getPayMethod());
        
        boolean success = orderService.payOrder(orderId, payDTO.getPayMethod());
        if (success) {
            // 返回更新后的订单详情
            OrderDTO orderDTO = orderService.getOrderDetail(orderId);
            return Result.success("支付成功", orderDTO);
        } else {
            return Result.error(500, "支付失败");
        }
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/status")
    @Operation(summary = "更新订单状态", description = "更新订单状态")
    public Result<Boolean> updateOrderStatus(
            @Valid @RequestBody OrderStatusUpdateDTO updateDTO) {
        
        log.info("更新订单状态，订单ID: {}, 新状态: {}", updateDTO.getOrderId(), updateDTO.getStatus());
        
        boolean success = orderService.updateOrderStatus(updateDTO);
        if (success) {
            return Result.success("状态更新成功", true);
        } else {
            return Result.error(500, "状态更新失败");
        }
    }

    /**
     * 处理订单（接单）
     */
    @PutMapping("/process/{orderId}")
    @Operation(summary = "处理订单", description = "将已支付订单标记为已接单")
    public Result<Boolean> processOrder(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull Long orderId,
            @Parameter(description = "商家ID", required = false) 
            @RequestHeader(value = "X-Merchant-Id", required = false) Long merchantId) {
        
        log.info("处理订单，订单ID: {}, 商家ID: {}", orderId, merchantId);
        
        // 验证商家权限
        if (merchantId != null) {
            Result<OrderDTO> checkResult = verifyMerchantOrder(orderId, merchantId);
            if (checkResult.getCode() != 200) {
                return Result.error(checkResult.getCode(), checkResult.getMessage());
            }
        }
        
        boolean success = orderService.processOrder(orderId);
        if (success) {
            return Result.success("订单处理成功", true);
        } else {
            return Result.error(500, "订单处理失败");
        }
    }

    /**
     * 发货订单
     */
    @PutMapping("/ship/{orderId}")
    @Operation(summary = "发货订单", description = "将订单标记为已发货")
    public Result<Boolean> shipOrder(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull Long orderId,
            @Parameter(description = "商家ID", required = false) 
            @RequestHeader(value = "X-Merchant-Id", required = false) Long merchantId) {
        
        log.info("发货订单，订单ID: {}, 商家ID: {}", orderId, merchantId);
        
        // 验证商家权限
        if (merchantId != null) {
            Result<OrderDTO> checkResult = verifyMerchantOrder(orderId, merchantId);
            if (checkResult.getCode() != 200) {
                return Result.error(checkResult.getCode(), checkResult.getMessage());
            }
        }
        
        boolean success = orderService.shipOrder(orderId);
        if (success) {
            return Result.success("订单发货成功", true);
        } else {
            return Result.error(500, "订单发货失败");
        }
    }

    /**
     * 完成订单
     */
    @PutMapping("/complete/{orderId}")
    @Operation(summary = "完成订单", description = "将已发货订单标记为已完成")
    public Result<Boolean> completeOrder(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull Long orderId,
            @Parameter(description = "商家ID", required = false) 
            @RequestHeader(value = "X-Merchant-Id", required = false) Long merchantId) {
        
        log.info("完成订单，订单ID: {}, 商家ID: {}", orderId, merchantId);
        
        // 验证商家权限
        if (merchantId != null) {
            Result<OrderDTO> checkResult = verifyMerchantOrder(orderId, merchantId);
            if (checkResult.getCode() != 200) {
                return Result.error(checkResult.getCode(), checkResult.getMessage());
            }
        }
        
        boolean success = orderService.completeOrder(orderId);
        if (success) {
            return Result.success("订单完成", true);
        } else {
            return Result.error(500, "订单完成失败");
        }
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel/{orderId}")
    @Operation(summary = "取消订单", description = "取消订单（商家或用户都可以取消）")
    public Result<Boolean> cancelOrder(
            @Parameter(description = "订单ID", required = true) 
            @PathVariable @NotNull Long orderId,
            @Parameter(description = "商家ID", required = false) 
            @RequestHeader(value = "X-Merchant-Id", required = false) Long merchantId,
            @Parameter(description = "用户ID", required = false)
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        log.info("取消订单，订单ID: {}, 商家ID: {}, 用户ID: {}", orderId, merchantId, userId);
        
        // 验证权限：商家只能取消自己的订单，用户只能取消自己的订单
        OrderDTO orderDTO = orderService.getOrderDetail(orderId);
        if (orderDTO == null) {
            return Result.error(404, "订单不存在");
        }
        
        if (merchantId != null) {
            if (!merchantId.equals(orderDTO.getMerchantId())) {
                log.warn("商家ID不匹配，请求商家ID: {}, 订单商家ID: {}", merchantId, orderDTO.getMerchantId());
                return Result.error(403, "无权操作此订单");
            }
        }
        
        if (userId != null) {
            if (!userId.equals(orderDTO.getUserId())) {
                log.warn("用户ID不匹配，请求用户ID: {}, 订单用户ID: {}", userId, orderDTO.getUserId());
                return Result.error(403, "无权操作此订单");
            }
        }
        
        boolean success = orderService.cancelOrder(orderId);
        if (success) {
            return Result.success("订单取消成功", true);
        } else {
            return Result.error(500, "订单取消失败");
        }
    }

    /**
     * 获取订单统计信息
     * 支持用户端和商家端两种查询方式：
     * - 用户端：通过 X-User-Id 获取该用户的订单统计
     * - 商家端：通过 X-Merchant-Id 获取该商家的订单统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取订单统计", description = "获取订单统计信息，支持用户端和商家端")
    public Result<Object> getOrderStatistics(
            @Parameter(description = "商家ID", required = false) 
            @RequestHeader(value = "X-Merchant-Id", required = false) Long merchantId,
            @Parameter(description = "用户ID", required = false)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {
        
        log.info("获取订单统计信息，商家ID: {}, 用户ID: {}", merchantId, userId);
        
        // 优先使用商家ID（商家端），否则使用用户ID（用户端）
        Long queryId = merchantId != null ? merchantId : userId;
        boolean isMerchant = merchantId != null;
        
        Object statistics = orderService.getOrderStatistics(queryId, isMerchant);
        return Result.success("查询成功", statistics);
    }

    /**
     * 验证商家订单权限的辅助方法
     */
    private Result<OrderDTO> verifyMerchantOrder(Long orderId, Long merchantId) {
        OrderDTO orderDTO = orderService.getOrderDetail(orderId);
        if (orderDTO == null) {
            return Result.error(404, "订单不存在");
        }
        
        if (!merchantId.equals(orderDTO.getMerchantId())) {
            log.warn("商家ID不匹配，请求商家ID: {}, 订单商家ID: {}", merchantId, orderDTO.getMerchantId());
            return Result.error(403, "无权操作此订单");
        }
        
        return Result.success("验证通过", orderDTO);
    }
}
