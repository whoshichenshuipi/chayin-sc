package com.naicha.hou.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单退款附件DTO
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
public class OrderRefundAttachmentDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 退款申请ID
     */
    private Long refundId;

    /**
     * 附件名称
     */
    private String name;

    /**
     * 附件URL
     */
    private String url;

    /**
     * 附件类型：image-图片，file-文件
     */
    private String type;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
