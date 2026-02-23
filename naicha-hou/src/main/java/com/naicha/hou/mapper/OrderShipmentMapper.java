package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.dto.OrderShipmentDTO;
import com.naicha.hou.dto.OrderShipmentQueryDTO;
import com.naicha.hou.entity.OrderShipment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单配送Mapper接口
 *
 * @author naicha
 * @since 2024-01-15
 */
@Mapper
public interface OrderShipmentMapper extends BaseMapper<OrderShipment> {

    /**
     * 分页查询订单配送列表
     */
    IPage<OrderShipmentDTO> selectShipmentPage(Page<OrderShipmentDTO> page, @Param("query") OrderShipmentQueryDTO query);

    /**
     * 根据订单ID查询配送信息
     */
    OrderShipmentDTO selectShipmentByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单号查询配送信息
     */
    OrderShipmentDTO selectShipmentByOrderNo(@Param("orderNo") String orderNo);
}

