package com.naicha.hou.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 订单创建DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class OrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 地址ID
     */
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    /**
     * 支付方式：wechat/alipay/wallet
     */
    private String payMethod;

    /**
     * 优惠券ID（可选）
     */
    private Long couponId;

    /**
     * 订单商品列表
     */
    @NotNull(message = "订单商品不能为空")
    @Valid
    private List<OrderItemCreateDTO> items;

    /**
     * 订单备注（可选）
     */
    private String remark;

    /**
     * 订单商品创建DTO
     */
    @Data
    public static class OrderItemCreateDTO implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 商品ID
         */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /**
         * 商品名称
         */
        private String productName;

        /**
         * 商品图片
         */
        private String productImage;

        /**
         * 商品价格
         */
        @NotNull(message = "商品价格不能为空")
        @Min(value = 0, message = "商品价格不能小于0")
        private java.math.BigDecimal price;

        /**
         * 购买数量
         */
        @NotNull(message = "购买数量不能为空")
        @Min(value = 1, message = "购买数量至少为1")
        private Integer quantity;

        /**
         * 购物车项ID（用于关联营销活动参与记录）
         */
        private String cartItemId;
    }
}

