package com.naicha.hou.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户营销活动参与记录创建DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class PromotionUserCartCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 营销活动ID
     */
    @NotNull(message = "营销活动ID不能为空")
    private Long promotionId;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 购物车项ID（前端生成的唯一标识）
     */
    @NotNull(message = "购物车项ID不能为空")
    private String cartItemId;

    /**
     * 商品数量
     */
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量至少为1")
    private Integer quantity;

    /**
     * 商品原价
     */
    @NotNull(message = "商品原价不能为空")
    @Min(value = 0, message = "商品原价不能小于0")
    private BigDecimal originalPrice;

    /**
     * 促销价格（可选）
     */
    private BigDecimal promotionPrice;

    /**
     * 优惠金额（可选）
     */
    private BigDecimal discountAmount;
}

