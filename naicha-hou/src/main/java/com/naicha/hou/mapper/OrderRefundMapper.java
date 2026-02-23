package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.dto.OrderRefundDTO;
import com.naicha.hou.dto.OrderRefundQueryDTO;
import com.naicha.hou.entity.OrderRefund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单退款申请Mapper接口
 *
 * @author naicha
 * @since 2024-01-15
 */
@Mapper
public interface OrderRefundMapper extends BaseMapper<OrderRefund> {

    /**
     * 分页查询订单退款申请列表
     *
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 订单退款申请列表
     */
    IPage<OrderRefundDTO> selectRefundPage(IPage<OrderRefundDTO> page, @Param("query") OrderRefundQueryDTO queryDTO);

    /**
     * 根据ID查询订单退款申请详情
     *
     * @param id 退款申请ID
     * @return 订单退款申请详情
     */
    OrderRefundDTO selectRefundDetailById(@Param("id") Long id);

    /**
     * 根据订单ID查询订单退款申请详情
     *
     * @param orderId 订单ID
     * @return 订单退款申请详情
     */
    OrderRefundDTO selectRefundDetailByOrderId(@Param("orderId") Long orderId);

    /**
     * 统计待处理取消申请数量
     *
     * @param merchantId 商家ID
     * @return 待处理取消申请数量
     */
    Integer countPendingCancel(@Param("merchantId") Long merchantId);

    /**
     * 统计待处理退款申请数量
     *
     * @param merchantId 商家ID
     * @return 待处理退款申请数量
     */
    Integer countPendingRefund(@Param("merchantId") Long merchantId);

    /**
     * 统计已完成退款数量
     *
     * @param merchantId 商家ID
     * @return 已完成退款数量
     */
    Integer countCompletedRefund(@Param("merchantId") Long merchantId);

    /**
     * 统计本月退款总额
     *
     * @param merchantId 商家ID
     * @return 本月退款总额
     */
    Double sumMonthlyRefundAmount(@Param("merchantId") Long merchantId);
}
