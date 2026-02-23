package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统日志实体类
 *
 * @author naicha
 * @since 2024-01-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("system_log")
public class SystemLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 日志类型：operation-操作日志，system-系统日志
     */
    @TableField("type")
    private String type;

    /**
     * 日志级别：info, warn, error, debug
     */
    @TableField("level")
    private String level;

    /**
     * 操作用户（管理员用户名或system）
     */
    @TableField("user")
    private String user;

    /**
     * 操作模块：user, merchant, order, system, statistics, database, payment, security
     */
    @TableField("module")
    private String module;

    /**
     * 操作内容
     */
    @TableField("action")
    private String action;

    /**
     * 操作结果：success, failed
     */
    @TableField("result")
    private String result;

    /**
     * IP地址
     */
    @TableField("ip")
    private String ip;

    /**
     * 详细信息
     */
    @TableField("detail")
    private String detail;

    /**
     * 请求参数（JSON格式）
     */
    @TableField("params")
    private String params;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 删除标志 0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}

