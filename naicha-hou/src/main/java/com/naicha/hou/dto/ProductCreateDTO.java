package com.naicha.hou.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品创建DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class ProductCreateDTO {

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String name;

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    @Positive(message = "分类ID必须大于0")
    private Long categoryId;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 原价
     */
    @NotNull(message = "原价不能为空")
    @Positive(message = "原价必须大于0")
    private BigDecimal originalPrice;

    /**
     * 会员价
     */
    @PositiveOrZero(message = "会员价不能为负数")
    private BigDecimal memberPrice;

    /**
     * 初始库存
     */
    @NotNull(message = "库存不能为空")
    @PositiveOrZero(message = "库存不能为负数")
    private Integer stock;

    /**
     * 库存预警值
     */
    @NotNull(message = "库存预警值不能为空")
    @PositiveOrZero(message = "库存预警值不能为负数")
    private Integer stockAlert;

    /**
     * 商品图片（轮播图URL列表）
     */
    private List<String> images;

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
     * 是否参与促销
     */
    private Boolean isPromotion;

    /**
     * 促销活动类型
     */
    private List<String> promotionTypes;

    /**
     * 加料DTO
     */
    @Data
    public static class AddonDTO {
        private String name;
        private BigDecimal price;
        private Integer stock;
    }

    /**
     * 杯型DTO
     */
    @Data
    public static class SizeDTO {
        private String name;
        private BigDecimal price;
        private String capacity;
    }
}

