package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 配送进度DTO
 *
 * @author naicha
 * @since 2024-01-15
 */
@Data
public class ShippingProgressDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 时间
     */
    private LocalDateTime time;

    /**
     * 时间文本
     */
    private String timeText;

    /**
     * 状态
     */
    private String status;

    /**
     * 描述
     */
    private String description;

    /**
     * 操作人
     */
    private String operator;
}

