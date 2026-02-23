package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 商家评分统计DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class MerchantRatingStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 商家名称
     */
    private String merchantName;

    /**
     * 综合评分
     */
    private BigDecimal overallRating;

    /**
     * 商品质量评分
     */
    private BigDecimal qualityRating;

    /**
     * 配送速度评分
     */
    private BigDecimal deliveryRating;

    /**
     * 服务态度评分
     */
    private BigDecimal serviceRating;

    /**
     * 评价总数
     */
    private Long reviewCount;

    /**
     * 各评分数量统计（key: 评分值，value: 数量）
     */
    private Map<Integer, Long> ratingDistribution;

    /**
     * 好评率（4星及以上占比）
     */
    private BigDecimal positiveRate;
}

