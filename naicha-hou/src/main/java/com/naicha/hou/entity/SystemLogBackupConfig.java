package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 日志备份配置实体类
 *
 * @author naicha
 * @since 2024-01-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("system_log_backup_config")
public class SystemLogBackupConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 是否启用自动备份 0-否 1-是
     */
    @TableField("enabled")
    private Integer enabled;

    /**
     * 备份频率：daily-每日，weekly-每周，monthly-每月
     */
    @TableField("frequency")
    private String frequency;

    /**
     * 备份时间
     */
    @TableField("backup_time")
    private LocalTime backupTime;

    /**
     * 保留天数
     */
    @TableField("retention_days")
    private Integer retentionDays;

    /**
     * 备份路径
     */
    @TableField("backup_path")
    private String backupPath;

    /**
     * 最后备份时间
     */
    @TableField("last_backup_time")
    private LocalDateTime lastBackupTime;

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
}

