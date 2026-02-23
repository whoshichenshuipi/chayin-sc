package com.naicha.hou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.dto.*;

/**
 * 订单配送服务接口
 *
 * @author naicha
 * @since 2024-01-15
 */
public interface OrderShipmentService {

    /**
     * 分页查询订单配送列表
     */
    IPage<OrderShipmentDTO> getShipmentPage(OrderShipmentQueryDTO queryDTO);

    /**
     * 根据订单ID查询配送信息
     */
    OrderShipmentDTO getShipmentByOrderId(Long orderId);

    /**
     * 根据订单号查询配送信息
     */
    OrderShipmentDTO getShipmentByOrderNo(String orderNo);

    /**
     * 订单发货
     */
    boolean shipOrder(OrderShipRequest request);

    /**
     * 批量发货
     */
    boolean batchShipOrder(java.util.List<Long> orderIds, OrderShipRequest request);

    /**
     * 更新配送进度
     */
    boolean updateShippingProgress(ShippingProgressUpdateRequest request);

    /**
     * 完成配送
     */
    boolean completeDelivery(Long orderId);
}

