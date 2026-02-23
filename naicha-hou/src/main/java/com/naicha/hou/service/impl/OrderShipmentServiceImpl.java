package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.common.ResultCode;
import com.naicha.hou.dto.*;
import com.naicha.hou.entity.Order;
import com.naicha.hou.entity.OrderShipment;
import com.naicha.hou.entity.ShippingProgress;
import com.naicha.hou.enums.OrderStatus;
import com.naicha.hou.enums.ShippingProgressStatus;
import com.naicha.hou.enums.ShippingStatus;
import com.naicha.hou.exception.BusinessException;
import com.naicha.hou.mapper.OrderMapper;
import com.naicha.hou.mapper.OrderShipmentMapper;
import com.naicha.hou.mapper.ShippingProgressMapper;
import com.naicha.hou.service.OrderShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 订单配送服务实现类
 *
 * @author naicha
 * @since 2024-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderShipmentServiceImpl implements OrderShipmentService {

    private final OrderShipmentMapper orderShipmentMapper;
    private final ShippingProgressMapper shippingProgressMapper;
    private final OrderMapper orderMapper;

    @Override
    public IPage<OrderShipmentDTO> getShipmentPage(OrderShipmentQueryDTO queryDTO) {
        Page<OrderShipmentDTO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<OrderShipmentDTO> result = orderShipmentMapper.selectShipmentPage(page, queryDTO);
        
        // 为每个配送信息设置格式化的时间和文本
        result.getRecords().forEach(this::enrichShipmentData);
        
        return result;
    }

    @Override
    public OrderShipmentDTO getShipmentByOrderId(Long orderId) {
        OrderShipmentDTO shipmentDTO = orderShipmentMapper.selectShipmentByOrderId(orderId);
        if (shipmentDTO != null) {
            enrichShipmentData(shipmentDTO);
        }
        return shipmentDTO;
    }

    @Override
    public OrderShipmentDTO getShipmentByOrderNo(String orderNo) {
        OrderShipmentDTO shipmentDTO = orderShipmentMapper.selectShipmentByOrderNo(orderNo);
        if (shipmentDTO != null) {
            enrichShipmentData(shipmentDTO);
        }
        return shipmentDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(OrderShipRequest request) {
        log.info("订单发货，订单ID: {}, 配送方式: {}", request.getOrderId(), request.getDeliveryType());
        
        // 查询订单信息
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }

        // 验证订单状态是否可以发货
        if (!OrderStatus.canShip(order.getStatus())) {
            String statusText = OrderStatus.getDescriptionByCode(order.getStatus());
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, 
                "订单状态不允许发货。当前状态: " + statusText + "，只有已支付、已接单、制作中的订单可以发货");
        }

        // 验证配送方式必填信息
        if ("self".equals(request.getDeliveryType())) {
            // 自配送需要配送员信息
            if (request.getDeliveryPerson() == null || request.getDeliveryPerson().trim().isEmpty()) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "自配送必须填写配送员姓名");
            }
            if (request.getDeliveryPhone() == null || request.getDeliveryPhone().trim().isEmpty()) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "自配送必须填写配送员电话");
            }
        } else if ("third_party".equals(request.getDeliveryType())) {
            // 第三方配送需要配送公司和单号
            if (request.getDeliveryCompany() == null || request.getDeliveryCompany().trim().isEmpty()) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "第三方配送必须选择配送公司");
            }
            if (request.getThirdPartyOrderNo() == null || request.getThirdPartyOrderNo().trim().isEmpty()) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "第三方配送必须填写配送单号");
            }
        }

        // 创建配送记录
        OrderShipment shipment = new OrderShipment();
        shipment.setOrderId(request.getOrderId());
        shipment.setOrderNo(order.getOrderNo());
        shipment.setDeliveryType(request.getDeliveryType());
        shipment.setDeliveryPerson(request.getDeliveryPerson());
        shipment.setDeliveryPhone(request.getDeliveryPhone());
        shipment.setDeliveryCompany(request.getDeliveryCompany());
        shipment.setThirdPartyOrderNo(request.getThirdPartyOrderNo());
        shipment.setShippingStatus(ShippingStatus.SHIPPING.getCode());
        shipment.setShippingTime(LocalDateTime.now());
        shipment.setEstimatedTime(request.getEstimatedTime());
        shipment.setRemark(request.getRemark());
        
        int insertResult = orderShipmentMapper.insert(shipment);
        if (insertResult <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "创建配送记录失败");
        }

        // 添加配送进度记录
        ShippingProgress progress = new ShippingProgress();
        progress.setShipmentId(shipment.getId());
        progress.setOrderId(request.getOrderId());
        progress.setStatus(ShippingProgressStatus.SHIPPED.getCode());
        progress.setDescription(request.getRemark() != null && !request.getRemark().trim().isEmpty() 
            ? request.getRemark() : ShippingProgressStatus.SHIPPED.getDescription());
        progress.setOperator("商家");
        progress.setCreatedAt(LocalDateTime.now());
        
        shippingProgressMapper.insert(progress);

        // 更新订单状态为已发货（状态4）
        order.setStatus(OrderStatus.SHIPPED.getCode()); // 4 - 已发货
        order.setDeliveryTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        int updateResult = orderMapper.updateById(order);
        if (updateResult <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "更新订单状态失败");
        }

        log.info("订单发货成功，订单ID: {}, 配送记录ID: {}", request.getOrderId(), shipment.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchShipOrder(List<Long> orderIds, OrderShipRequest request) {
        log.info("批量发货，订单数量: {}, 配送方式: {}", orderIds.size(), request.getDeliveryType());
        
        if (orderIds == null || orderIds.isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单列表不能为空");
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (Long orderId : orderIds) {
            try {
                OrderShipRequest shipRequest = new OrderShipRequest();
                shipRequest.setOrderId(orderId);
                shipRequest.setDeliveryType(request.getDeliveryType());
                shipRequest.setDeliveryPerson(request.getDeliveryPerson());
                shipRequest.setDeliveryPhone(request.getDeliveryPhone());
                shipRequest.setDeliveryCompany(request.getDeliveryCompany());
                shipRequest.setThirdPartyOrderNo(request.getThirdPartyOrderNo());
                shipRequest.setEstimatedTime(request.getEstimatedTime());
                shipRequest.setRemark(request.getRemark());
                
                shipOrder(shipRequest);
                successCount++;
            } catch (Exception e) {
                log.error("批量发货中订单 {} 发货失败: {}", orderId, e.getMessage());
                failCount++;
                // 继续处理下一个订单
            }
        }
        
        if (failCount > 0) {
            log.warn("批量发货完成，成功: {}, 失败: {}", successCount, failCount);
            if (successCount == 0) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, 
                    String.format("批量发货全部失败，共 %d 个订单", failCount));
            }
            // 部分成功，返回成功但记录警告
        }
        
        log.info("批量发货完成，成功: {}, 失败: {}", successCount, failCount);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateShippingProgress(ShippingProgressUpdateRequest request) {
        log.info("更新配送进度，订单ID: {}, 状态: {}", request.getOrderId(), request.getStatus());
        
        // 查询配送信息
        OrderShipment shipment = orderShipmentMapper.selectOne(
            new LambdaQueryWrapper<OrderShipment>()
                .eq(OrderShipment::getOrderId, request.getOrderId())
                .eq(OrderShipment::getDeleted, 0)
                .orderByDesc(OrderShipment::getCreatedAt)
                .last("LIMIT 1")
        );

        if (shipment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "配送信息不存在");
        }

        // 验证配送状态是否可以更新进度
        if (!ShippingStatus.canUpdateProgress(shipment.getShippingStatus())) {
            String statusText = ShippingStatus.getDescriptionByCode(shipment.getShippingStatus());
            throw new BusinessException(ResultCode.VALIDATE_FAILED, 
                "配送状态不允许更新进度。当前状态: " + statusText + "，只有配送中的订单可以更新进度");
        }

        // 添加配送进度记录
        ShippingProgress progress = new ShippingProgress();
        progress.setShipmentId(shipment.getId());
        progress.setOrderId(request.getOrderId());
        progress.setStatus(getProgressStatusText(request.getStatus()));
        progress.setDescription(request.getDescription() != null && !request.getDescription().trim().isEmpty() 
            ? request.getDescription() : getProgressStatusText(request.getStatus()));
        progress.setOperator("商家");
        progress.setCreatedAt(LocalDateTime.now());
        
        int insertResult = shippingProgressMapper.insert(progress);
        if (insertResult <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "添加配送进度记录失败");
        }

        // 更新预计送达时间
        if (request.getEstimatedTime() != null) {
            shipment.setEstimatedTime(request.getEstimatedTime());
            orderShipmentMapper.updateById(shipment);
        }

        log.info("配送进度更新成功，订单ID: {}", request.getOrderId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeDelivery(Long orderId) {
        log.info("完成配送，订单ID: {}", orderId);
        
        // 查询配送信息
        OrderShipment shipment = orderShipmentMapper.selectOne(
            new LambdaQueryWrapper<OrderShipment>()
                .eq(OrderShipment::getOrderId, orderId)
                .eq(OrderShipment::getDeleted, 0)
                .orderByDesc(OrderShipment::getCreatedAt)
                .last("LIMIT 1")
        );

        if (shipment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "配送信息不存在");
        }

        // 验证配送状态是否可以完成
        if (!ShippingStatus.canComplete(shipment.getShippingStatus())) {
            String statusText = ShippingStatus.getDescriptionByCode(shipment.getShippingStatus());
            throw new BusinessException(ResultCode.VALIDATE_FAILED, 
                "配送状态不允许完成。当前状态: " + statusText + "，只有配送中的订单可以完成");
        }

        // 更新配送状态
        shipment.setShippingStatus(ShippingStatus.DELIVERED.getCode());
        shipment.setActualDeliveryTime(LocalDateTime.now());
        shipment.setUpdatedAt(LocalDateTime.now());
        int updateShipmentResult = orderShipmentMapper.updateById(shipment);
        
        if (updateShipmentResult <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "更新配送状态失败");
        }

        // 添加配送完成进度记录
        ShippingProgress progress = new ShippingProgress();
        progress.setShipmentId(shipment.getId());
        progress.setOrderId(orderId);
        progress.setStatus(ShippingProgressStatus.DELIVERED.getCode());
        progress.setDescription(ShippingProgressStatus.DELIVERED.getDescription());
        progress.setOperator("商家");
        progress.setCreatedAt(LocalDateTime.now());
        
        shippingProgressMapper.insert(progress);

        // 更新订单状态为已完成（状态5）
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        
        order.setStatus(OrderStatus.COMPLETED.getCode()); // 5 - 已完成
        order.setUpdatedAt(LocalDateTime.now());
        int updateOrderResult = orderMapper.updateById(order);
        
        if (updateOrderResult <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "更新订单状态失败");
        }

        log.info("配送完成成功，订单ID: {}, 订单状态已更新为已完成", orderId);
        return true;
    }

    /**
     * 丰富配送数据
     */
    private void enrichShipmentData(OrderShipmentDTO shipmentDTO) {
        // 设置配送方式文本
        shipmentDTO.setDeliveryTypeText(getDeliveryTypeText(shipmentDTO.getDeliveryType()));
        
        // 设置配送状态文本
        shipmentDTO.setShippingStatusText(getShippingStatusText(shipmentDTO.getShippingStatus()));
        
        // 格式化时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        if (shipmentDTO.getShippingTime() != null) {
            shipmentDTO.setShippingTimeText(shipmentDTO.getShippingTime().format(formatter));
        }
        if (shipmentDTO.getEstimatedTime() != null) {
            shipmentDTO.setEstimatedTimeText(shipmentDTO.getEstimatedTime().format(formatter));
        }
        
        // 查询配送进度历史
        if (shipmentDTO.getProgressHistory() != null) {
            shipmentDTO.getProgressHistory().forEach(progress -> {
                if (progress.getTime() != null) {
                    progress.setTimeText(progress.getTime().format(formatter));
                }
            });
        }
    }

    /**
     * 获取配送方式文本
     */
    private String getDeliveryTypeText(String type) {
        if (type == null) return "未知";
        
        return switch (type) {
            case "self" -> "商家自配送";
            case "third_party" -> "第三方配送";
            default -> "未知";
        };
    }

    /**
     * 获取配送状态文本
     */
    private String getShippingStatusText(String status) {
        return ShippingStatus.getDescriptionByCode(status);
    }

    /**
     * 获取配送进度状态文本
     */
    private String getProgressStatusText(String status) {
        return ShippingProgressStatus.getDescriptionByCode(status);
    }
}

