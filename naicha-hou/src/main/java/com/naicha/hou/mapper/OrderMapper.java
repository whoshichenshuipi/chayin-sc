package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.dto.OrderDTO;
import com.naicha.hou.dto.OrderQueryDTO;
import com.naicha.hou.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 分页查询订单列表
     */
    IPage<OrderDTO> selectOrderPage(Page<OrderDTO> page, @Param("query") OrderQueryDTO query);

    /**
     * 根据订单ID查询订单详情
     */
    OrderDTO selectOrderDetail(@Param("orderId") Long orderId);

    /**
     * 根据订单号查询订单
     */
    OrderDTO selectOrderByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询用户订单统计信息（消费总额、订单数量、最后登录时间）
     *
     * @param userId 用户ID
     * @return 统计信息Map，包含totalAmount、orderCount、lastLoginTime
     */
    java.util.Map<String, Object> selectUserOrderStats(@Param("userId") Long userId);
}
