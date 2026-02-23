package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 配送进度实体类
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("shipping_progress")
public class ShippingProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 进度ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配送ID
     */
    @TableField("shipment_id")
    private Long shipmentId;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 配送状态
     */
    @TableField("status")
    private String status;

    /**
     * 进度描述
     */
    @TableField("description")
    private String description;

    /**
     * 操作人
     */
    @TableField("operator")
    private String operator;

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

    /**
     * 删除标志 0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}

