package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * 日志备份配置DTO
 *
 * @author naicha
 * @since 2024-01-25
 */
@Data
public class SystemLogBackupConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否启用自动备份
     */
    private Boolean enabled;

    /**
     * 备份频率：daily-每日，weekly-每周，monthly-每月
     */
    private String frequency;

    /**
     * 备份时间
     */
    private LocalTime time;

    /**
     * 保留天数
     */
    private Integer retentionDays;

    /**
     * 备份路径
     */
    private String backupPath;
}

