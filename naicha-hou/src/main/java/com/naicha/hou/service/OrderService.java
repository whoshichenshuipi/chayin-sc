package com.naicha.hou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.dto.OrderCreateDTO;
import com.naicha.hou.dto.OrderDTO;
import com.naicha.hou.dto.OrderProcessDTO;
import com.naicha.hou.dto.OrderProcessQueryDTO;
import com.naicha.hou.dto.OrderQueryDTO;
import com.naicha.hou.dto.OrderStatusUpdateDTO;

import java.util.List;

/**
 * 订单服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface OrderService {

    /**
     * 创建订单
     *
     * @param userId 用户ID
     * @param createDTO 订单创建DTO
     * @return 订单ID
     */
    Long createOrder(Long userId, OrderCreateDTO createDTO);

    /**
     * 分页查询订单列表
     */
    IPage<OrderDTO> getOrderPage(OrderQueryDTO queryDTO);

    /**
     * 根据订单ID查询订单详情
     */
    OrderDTO getOrderDetail(Long orderId);

    /**
     * 根据订单号查询订单
     */
    OrderDTO getOrderByOrderNo(String orderNo);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(OrderStatusUpdateDTO updateDTO);

    /**
     * 处理订单（待支付 -> 已支付）
     */
    boolean processOrder(Long orderId);

    /**
     * 发货订单（已支付 -> 已发货）
     */
    boolean shipOrder(Long orderId);

    /**
     * 完成订单（已发货 -> 已完成）
     */
    boolean completeOrder(Long orderId);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId);

    /**
     * 支付订单（待支付 -> 已支付）
     *
     * @param orderId 订单ID
     * @param payMethod 支付方式：wechat/alipay/wallet
     * @return 是否成功
     */
    boolean payOrder(Long orderId, String payMethod);

    /**
     * 获取订单统计信息
     *
     * @param id 商家ID或用户ID
     * @param isMerchant 是否为商家端查询（true-商家端，false-用户端）
     */
    Object getOrderStatistics(Long id, boolean isMerchant);

    /**
     * 分页查询待处理订单列表
     */
    IPage<OrderDTO> getPendingOrderPage(OrderProcessQueryDTO queryDTO);

    /**
     * 接单处理
     */
    boolean acceptOrder(OrderProcessDTO processDTO);

    /**
     * 开始制作
     */
    boolean startMaking(OrderProcessDTO processDTO);

    /**
     * 制作完成
     */
    boolean finishMaking(OrderProcessDTO processDTO);

    /**
     * 批量处理订单
     */
    boolean batchProcessOrders(List<Long> orderIds, Integer processType);

    /**
     * 获取订单处理统计
     */
    Object getOrderProcessStatistics(Long merchantId);
}
