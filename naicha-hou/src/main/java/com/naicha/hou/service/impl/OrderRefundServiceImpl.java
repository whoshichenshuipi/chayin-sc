package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.dto.*;
import com.naicha.hou.common.ResultCode;
import com.naicha.hou.entity.OrderRefund;
import com.naicha.hou.entity.OrderRefundAttachment;
import com.naicha.hou.exception.BusinessException;
import com.naicha.hou.mapper.OrderRefundAttachmentMapper;
import com.naicha.hou.mapper.OrderRefundMapper;
import com.naicha.hou.mapper.OrderMapper;
import com.naicha.hou.entity.Order;
import com.naicha.hou.service.OrderRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单退款申请服务实现
 *
 * @author naicha
 * @since 2024-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderRefundServiceImpl implements OrderRefundService {

    private final OrderRefundMapper orderRefundMapper;
    private final OrderRefundAttachmentMapper orderRefundAttachmentMapper;
    private final OrderMapper orderMapper;

    @Override
    public IPage<OrderRefundDTO> getRefundPage(OrderRefundQueryDTO queryDTO) {
        Page<OrderRefundDTO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        return orderRefundMapper.selectRefundPage(page, queryDTO);
    }

    @Override
    public OrderRefundDTO getRefundDetailById(Long id) {
        OrderRefundDTO refundDTO = orderRefundMapper.selectRefundDetailById(id);
        if (refundDTO != null) {
            // 查询附件信息
            List<OrderRefundAttachmentDTO> attachments = getRefundAttachments(id);
            refundDTO.setAttachments(attachments);
            
            // 查询商品信息（这里需要根据实际订单表结构来实现）
            // List<OrderItemDTO> items = getOrderItems(refundDTO.getOrderId());
            // refundDTO.setItems(items);
        }
        return refundDTO;
    }

    @Override
    public OrderRefundDTO getRefundDetailByOrderId(Long orderId) {
        OrderRefundDTO refundDTO = orderRefundMapper.selectRefundDetailByOrderId(orderId);
        if (refundDTO != null) {
            // 查询附件信息
            List<OrderRefundAttachmentDTO> attachments = getRefundAttachments(refundDTO.getId());
            refundDTO.setAttachments(attachments);
        }
        return refundDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitRefundRequest(OrderRefundRequestDTO requestDTO) {
        log.info("提交退款申请，订单ID: {}, 处理类型: {}, 退款金额: {}", 
            requestDTO.getOrderId(), requestDTO.getProcessType(), requestDTO.getRefundAmount());
        
        // 根据订单ID查询订单信息
        Order order = orderMapper.selectById(requestDTO.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND, "订单不存在");
        }
        
        // 检查订单状态是否允许退款（已支付、已接单、制作中、已发货的订单可以退款）
        if (order.getStatus() == null || order.getStatus() < 1 || order.getStatus() > 4) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, 
                "订单状态不允许退款，只有已支付、已接单、制作中、已发货的订单可以申请退款");
        }
        
        // 检查是否已经存在退款申请
        OrderRefundDTO existingRefund = orderRefundMapper.selectRefundDetailByOrderId(requestDTO.getOrderId());
        if (existingRefund != null && ("pending".equals(existingRefund.getStatus()) || "approved".equals(existingRefund.getStatus()))) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "该订单已有退款申请正在处理中");
        }
        
        // 创建退款申请记录
        OrderRefund orderRefund = new OrderRefund();
        orderRefund.setOrderId(requestDTO.getOrderId());
        orderRefund.setOrderNo(order.getOrderNo());
        orderRefund.setMerchantId(order.getMerchantId());
        orderRefund.setUserId(order.getUserId());
        orderRefund.setTotalAmount(order.getTotalAmount());
        orderRefund.setProcessType(requestDTO.getProcessType());
        orderRefund.setReason(requestDTO.getReason());
        orderRefund.setDetailReason(requestDTO.getDetailReason());
        orderRefund.setRefundType(requestDTO.getRefundType());
        orderRefund.setRefundReason(requestDTO.getRefundReason());
        orderRefund.setStatus("pending");
        orderRefund.setApplyTime(LocalDateTime.now());
        
        // 设置退款金额：如果是全额退款，使用订单实付金额；如果是部分退款，使用传入的金额
        if ("full".equals(requestDTO.getRefundType())) {
            // 全额退款：使用订单实付金额
            orderRefund.setRefundAmount(order.getPayAmount() != null ? order.getPayAmount() : order.getTotalAmount());
        } else {
            // 部分退款：使用传入的金额，但不超过订单实付金额
            BigDecimal refundAmount = requestDTO.getRefundAmount();
            if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "部分退款金额必须大于0");
            }
            BigDecimal maxRefundAmount = order.getPayAmount() != null ? order.getPayAmount() : order.getTotalAmount();
            if (refundAmount.compareTo(maxRefundAmount) > 0) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, 
                    "退款金额不能超过订单实付金额: " + maxRefundAmount);
            }
            orderRefund.setRefundAmount(refundAmount);
        }
        
        // 插入退款申请
        int result = orderRefundMapper.insert(orderRefund);
        
        if (result <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "创建退款申请失败");
        }
        
        // 保存附件信息（如果有）
        if (requestDTO.getAttachments() != null && !requestDTO.getAttachments().isEmpty()) {
            for (OrderRefundAttachmentDTO attachmentDTO : requestDTO.getAttachments()) {
                OrderRefundAttachment attachment = new OrderRefundAttachment();
                attachment.setRefundId(orderRefund.getId());
                attachment.setName(attachmentDTO.getName());
                attachment.setUrl(attachmentDTO.getUrl());
                attachment.setType(attachmentDTO.getType());
                attachment.setSize(attachmentDTO.getSize());
                orderRefundAttachmentMapper.insert(attachment);
            }
        }
        
        log.info("退款申请提交成功，退款申请ID: {}", orderRefund.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processRefundRequest(OrderRefundProcessRequestDTO requestDTO) {
        log.info("处理退款申请，退款申请ID: {}, 操作: {}", requestDTO.getRefundId(), requestDTO.getAction());
        
        OrderRefund orderRefund = orderRefundMapper.selectById(requestDTO.getRefundId());
        if (orderRefund == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "退款申请不存在");
        }
        
        if (!"pending".equals(orderRefund.getStatus())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, 
                "该申请已处理，无法重复处理。当前状态: " + orderRefund.getStatus());
        }
        
        // 更新处理状态
        orderRefund.setStatus("approved".equals(requestDTO.getAction()) ? "approved" : "rejected");
        orderRefund.setProcessRemark(requestDTO.getRemark());
        orderRefund.setProcessTime(LocalDateTime.now());
        
        if ("rejected".equals(requestDTO.getAction())) {
            orderRefund.setRejectReason(requestDTO.getRejectReason());
            orderRefund.setRejectDetail(requestDTO.getRejectDetail());
        }
        
        int result = orderRefundMapper.updateById(orderRefund);
        
        if (result <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "更新退款申请状态失败");
        }
        
        // 如果是同意退款，模拟退款处理（实际应该调用支付接口进行退款）
        if ("approved".equals(requestDTO.getAction())) {
            // 模拟退款：这里可以调用实际的退款接口
            log.info("退款申请已同意，订单ID: {}, 退款金额: {}", 
                orderRefund.getOrderId(), orderRefund.getRefundAmount());
            // 实际项目中应该：
            // 1. 调用支付接口进行退款
            // 2. 更新订单状态为已退款
            // 3. 发送通知给消费者
            sendRefundNotification(orderRefund);
        }
        
        log.info("退款申请处理成功，退款申请ID: {}, 新状态: {}", orderRefund.getId(), orderRefund.getStatus());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchProcessRefundRequest(BatchOrderRefundProcessRequestDTO requestDTO) {
        try {
            int successCount = 0;
            for (Long refundId : requestDTO.getRefundIds()) {
                OrderRefundProcessRequestDTO processRequest = new OrderRefundProcessRequestDTO();
                processRequest.setRefundId(refundId);
                processRequest.setAction(requestDTO.getAction().replace("batch_", ""));
                processRequest.setRemark(requestDTO.getRemark());
                
                if (processRefundRequest(processRequest)) {
                    successCount++;
                }
            }
            
            log.info("批量处理退款申请完成，成功处理 {} 个申请", successCount);
            return successCount > 0;
        } catch (Exception e) {
            log.error("批量处理退款申请失败", e);
            throw new RuntimeException("批量处理退款申请失败: " + e.getMessage());
        }
    }

    @Override
    public OrderRefundStatsDTO getRefundStats(Long merchantId) {
        OrderRefundStatsDTO stats = new OrderRefundStatsDTO();
        stats.setPendingCancel(orderRefundMapper.countPendingCancel(merchantId));
        stats.setPendingRefund(orderRefundMapper.countPendingRefund(merchantId));
        stats.setCompletedRefund(orderRefundMapper.countCompletedRefund(merchantId));
        stats.setTotalRefundAmount(orderRefundMapper.sumMonthlyRefundAmount(merchantId));
        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeRefund(Long refundId) {
        try {
            OrderRefund orderRefund = orderRefundMapper.selectById(refundId);
            if (orderRefund == null) {
                throw new RuntimeException("退款申请不存在");
            }
            
            if (!"approved".equals(orderRefund.getStatus())) {
                throw new RuntimeException("只有已同意的退款申请才能完成退款");
            }
            
            // 更新完成状态
            orderRefund.setStatus("completed");
            orderRefund.setCompleteTime(LocalDateTime.now());
            
            int result = orderRefundMapper.updateById(orderRefund);
            
            if (result > 0) {
                // 发送退款完成通知
                sendRefundCompleteNotification(orderRefund);
            }
            
            return result > 0;
        } catch (Exception e) {
            log.error("完成退款失败", e);
            throw new RuntimeException("完成退款失败: " + e.getMessage());
        }
    }

    /**
     * 获取退款申请附件
     */
    private List<OrderRefundAttachmentDTO> getRefundAttachments(Long refundId) {
        LambdaQueryWrapper<OrderRefundAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderRefundAttachment::getRefundId, refundId)
               .eq(OrderRefundAttachment::getIsDeleted, 0);
        
        List<OrderRefundAttachment> attachments = orderRefundAttachmentMapper.selectList(wrapper);
        return attachments.stream().map(attachment -> {
            OrderRefundAttachmentDTO dto = new OrderRefundAttachmentDTO();
            BeanUtils.copyProperties(attachment, dto);
            return dto;
        }).toList();
    }

    /**
     * 发送退款通知
     */
    private void sendRefundNotification(OrderRefund orderRefund) {
        // 这里实现发送通知的逻辑
        // 可以调用消息服务、短信服务等
        log.info("发送退款通知：订单号={}, 退款金额={}", orderRefund.getOrderNo(), orderRefund.getRefundAmount());
        
        // 模拟发送通知给消费者前台
        String message = String.format("退款成功，退款金额¥%.2f", orderRefund.getRefundAmount().doubleValue());
        log.info("通知消费者：{}", message);
    }

    /**
     * 发送退款完成通知
     */
    private void sendRefundCompleteNotification(OrderRefund orderRefund) {
        // 这里实现发送退款完成通知的逻辑
        log.info("发送退款完成通知：订单号={}, 退款金额={}", orderRefund.getOrderNo(), orderRefund.getRefundAmount());
        
        // 模拟发送通知给消费者前台
        String message = String.format("退款成功，退款金额¥%.2f", orderRefund.getRefundAmount().doubleValue());
        log.info("通知消费者：{}", message);
    }
}
