package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统日志查询DTO
 *
 * @author naicha
 * @since 2024-01-25
 */
@Data
public class SystemLogQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志类型：operation-操作日志，system-系统日志
     */
    private String type;

    /**
     * 日志级别：info, warn, error, debug
     */
    private String level;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作用户
     */
    private String user;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 20;
}

