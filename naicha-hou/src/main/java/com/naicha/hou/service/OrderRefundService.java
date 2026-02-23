package com.naicha.hou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.dto.*;

import java.util.List;

/**
 * 订单退款申请服务接口
 *
 * @author naicha
 * @since 2024-01-15
 */
public interface OrderRefundService {

    /**
     * 分页查询订单退款申请列表
     *
     * @param queryDTO 查询条件
     * @return 订单退款申请列表
     */
    IPage<OrderRefundDTO> getRefundPage(OrderRefundQueryDTO queryDTO);

    /**
     * 根据ID查询订单退款申请详情
     *
     * @param id 退款申请ID
     * @return 订单退款申请详情
     */
    OrderRefundDTO getRefundDetailById(Long id);

    /**
     * 根据订单ID查询订单退款申请详情
     *
     * @param orderId 订单ID
     * @return 订单退款申请详情
     */
    OrderRefundDTO getRefundDetailByOrderId(Long orderId);

    /**
     * 提交订单退款申请
     *
     * @param requestDTO 退款申请请求
     * @return 是否成功
     */
    boolean submitRefundRequest(OrderRefundRequestDTO requestDTO);

    /**
     * 处理订单退款申请
     *
     * @param requestDTO 处理请求
     * @return 是否成功
     */
    boolean processRefundRequest(OrderRefundProcessRequestDTO requestDTO);

    /**
     * 批量处理订单退款申请
     *
     * @param requestDTO 批量处理请求
     * @return 是否成功
     */
    boolean batchProcessRefundRequest(BatchOrderRefundProcessRequestDTO requestDTO);

    /**
     * 获取统计数据
     *
     * @param merchantId 商家ID
     * @return 统计数据
     */
    OrderRefundStatsDTO getRefundStats(Long merchantId);

    /**
     * 完成退款
     *
     * @param refundId 退款申请ID
     * @return 是否成功
     */
    boolean completeRefund(Long refundId);
}
