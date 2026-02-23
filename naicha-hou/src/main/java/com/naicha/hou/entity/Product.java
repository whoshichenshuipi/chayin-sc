package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商家ID
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 商品名称
     */
    @TableField("name")
    private String name;

    /**
     * 商品描述
     */
    @TableField("description")
    private String description;

    /**
     * 价格（当前售价）
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 原价
     */
    @TableField("original_price")
    private BigDecimal originalPrice;

    /**
     * 会员价
     */
    @TableField("member_price")
    private BigDecimal memberPrice;

    /**
     * 库存
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 库存预警值
     */
    @TableField("stock_alert")
    private Integer stockAlert;

    /**
     * 销量
     */
    @TableField("sales")
    private Integer sales;

    /**
     * 商品图片（JSON格式存储轮播图）
     */
    @TableField("images")
    private String images;

    /**
     * 商品状态 0-下架 1-上架 2-预售 3-审核中 4-审核不通过
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否参与促销 0-否 1-是
     */
    @TableField("is_promotion")
    private Integer isPromotion;

    /**
     * 促销活动类型（JSON格式）
     */
    @TableField("promotion_types")
    private String promotionTypes;

    /**
     * 是否需审核 0-否 1-是
     */
    @TableField("need_audit")
    private Integer needAudit;

    /**
     * 审核状态 0-未审核 1-审核通过 2-审核不通过
     */
    @TableField("audit_status")
    private Integer auditStatus;

    /**
     * 审核意见
     */
    @TableField("audit_remark")
    private String auditRemark;

    /**
     * 审核人ID
     */
    @TableField("auditor_id")
    private Long auditorId;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    private LocalDateTime auditTime;

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

