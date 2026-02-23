package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单退款附件实体
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("order_refund_attachment")
public class OrderRefundAttachment {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 退款申请ID
     */
    @TableField("refund_id")
    private Long refundId;

    /**
     * 附件名称
     */
    @TableField("name")
    private String name;

    /**
     * 附件URL
     */
    @TableField("url")
    private String url;

    /**
     * 附件类型：image-图片，file-文件
     */
    @TableField("type")
    private String type;

    /**
     * 文件大小（字节）
     */
    @TableField("size")
    private Long size;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}
