package com.naicha.hou.dto;

import lombok.Data;

/**
 * 商品查询DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class ProductQueryDTO {

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品名称（模糊查询）
     */
    private String name;

    /**
     * 商品状态
     */
    private Integer status;

    /**
     * 库存状态（sufficient-库存充足, low-库存不足, out-缺货）
     */
    private String stockStatus;

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 20;
}

