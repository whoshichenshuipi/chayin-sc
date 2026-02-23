package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 滞销商品DTO
 *
 * @author naicha
 * @since 2024-01-25
 */
@Data
public class SlowProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 销量
     */
    private Long salesCount;

    /**
     * 销售额
     */
    private BigDecimal salesAmount;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 库存预警值
     */
    private Integer stockAlert;
}

