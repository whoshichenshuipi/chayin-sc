package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 店铺评价实体类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("shop_review")
public class ShopReview implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 店铺ID
     */
    @TableField("shop_id")
    private Long shopId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 用户头像
     */
    @TableField("user_avatar")
    private String userAvatar;

    /**
     * 总体评分
     */
    @TableField("overall_rating")
    private BigDecimal overallRating;

    /**
     * 商品质量评分
     */
    @TableField("quality_rating")
    private BigDecimal qualityRating;

    /**
     * 配送速度评分
     */
    @TableField("delivery_rating")
    private BigDecimal deliveryRating;

    /**
     * 服务态度评分
     */
    @TableField("service_rating")
    private BigDecimal serviceRating;

    /**
     * 评价内容
     */
    @TableField("comment")
    private String comment;

    /**
     * 评价图片（JSON格式）
     */
    @TableField("images")
    private String images;

    /**
     * 商家回复
     */
    @TableField("merchant_reply")
    private String merchantReply;

    /**
     * 回复时间
     */
    @TableField("reply_time")
    private LocalDateTime replyTime;

    /**
     * 有用数
     */
    @TableField("helpful_count")
    private Integer helpfulCount;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 删除标志 0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
