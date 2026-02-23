package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户营销活动参与记录实体类
 * 用于绑定用户ID、营销活动ID和购物车商品ID
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("promotion_user_cart")
public class PromotionUserCart implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 营销活动ID
     */
    @TableField("promotion_id")
    private Long promotionId;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 购物车项ID（前端生成的唯一标识）
     */
    @TableField("cart_item_id")
    private String cartItemId;

    /**
     * 商品数量
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 商品原价
     */
    @TableField("original_price")
    private BigDecimal originalPrice;

    /**
     * 促销价格
     */
    @TableField("promotion_price")
    private BigDecimal promotionPrice;

    /**
     * 优惠金额
     */
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 状态: active-有效, used-已使用, expired-已过期
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

