package com.naicha.hou.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品详情DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class ProductDTO {

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 当前售价
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 会员价
     */
    private BigDecimal memberPrice;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 库存预警值
     */
    private Integer stockAlert;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 商品图片列表
     */
    private List<String> images;

    /**
     * 商品状态 0-下架 1-上架 2-预售 3-审核中 4-审核不通过
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 是否参与促销
     */
    private Boolean isPromotion;

    /**
     * 促销活动类型
     */
    private List<String> promotionTypes;

    /**
     * 甜度选项
     */
    private List<String> sweetnessOptions;

    /**
     * 温度选项
     */
    private List<String> temperatureOptions;

    /**
     * 加料选项
     */
    private List<AddonDTO> addons;

    /**
     * 杯型规格
     */
    private List<SizeDTO> sizes;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    @Data
    public static class AddonDTO {
        private String name;
        private BigDecimal price;
        private Integer stock;
    }

    @Data
    public static class SizeDTO {
        private String name;
        private BigDecimal price;
        private String capacity;
    }
}

