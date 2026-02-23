package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户违规记录DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class UserViolationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 违规记录ID
     */
    private Long id;

    /**
     * 违规类型：warning-警告, limit-限制下单, freeze-冻结账号
     */
    private String type;

    /**
     * 违规类型文本（用于前端显示）
     */
    private String typeText;

    /**
     * 违规原因
     */
    private String reason;

    /**
     * 限制天数（仅当type为limit时有效）
     */
    private Integer limitDays;

    /**
     * 处理人
     */
    private String handler;

    /**
     * 处理时间
     */
    private LocalDateTime time;
}

