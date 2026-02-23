package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商家评分DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class MerchantRatingDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 商家名称
     */
    private String merchantName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 总体评分
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
     * 评价内容
     */
    private String comment;

    /**
     * 评价图片列表
     */
    private List<String> images;

    /**
     * 商家回复
     */
    private String merchantReply;

    /**
     * 回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 有用数
     */
    private Integer helpfulCount;

    /**
     * 创建时间（评价时间）
     */
    private LocalDateTime createTime;
}

