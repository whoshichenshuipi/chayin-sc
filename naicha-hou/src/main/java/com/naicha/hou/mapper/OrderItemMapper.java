package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单商品Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
