package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户违规记录实体类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_violation")
public class UserViolation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 违规记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 违规类型：warning-警告, limit-限制下单, freeze-冻结账号
     */
    @TableField("type")
    private String type;

    /**
     * 违规原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 限制天数（仅当type为limit时有效）
     */
    @TableField("limit_days")
    private Integer limitDays;

    /**
     * 处理人（管理员用户名或ID）
     */
    @TableField("handler")
    private String handler;

    /**
     * 处理时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 删除标志 0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}

