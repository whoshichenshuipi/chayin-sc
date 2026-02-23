package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 系统日志DTO
 *
 * @author naicha
 * @since 2024-01-25
 */
@Data
public class SystemLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 日志类型：operation-操作日志，system-系统日志
     */
    private String type;

    /**
     * 日志级别：info, warn, error, debug
     */
    private String level;

    /**
     * 操作用户
     */
    private String user;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作内容
     */
    private String action;

    /**
     * 操作结果：success, failed
     */
    private String result;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 详细信息
     */
    private String detail;

    /**
     * 请求参数
     */
    private Map<String, Object> params;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

