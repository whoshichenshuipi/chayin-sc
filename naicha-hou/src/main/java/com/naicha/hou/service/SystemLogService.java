package com.naicha.hou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.dto.SystemLogBackupConfigDTO;
import com.naicha.hou.dto.SystemLogDTO;
import com.naicha.hou.dto.SystemLogQueryDTO;

/**
 * 系统日志服务接口
 *
 * @author naicha
 * @since 2024-01-25
 */
public interface SystemLogService {

    /**
     * 分页查询日志列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<SystemLogDTO> getLogList(SystemLogQueryDTO queryDTO);

    /**
     * 根据ID获取日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    SystemLogDTO getLogById(Long id);

    /**
     * 手动备份日志
     *
     * @return 是否成功
     */
    boolean backupLogs();

    /**
     * 导出日志
     *
     * @param queryDTO 查询条件
     * @return 导出文件路径
     */
    String exportLogs(SystemLogQueryDTO queryDTO);

    /**
     * 获取自动备份配置
     *
     * @return 备份配置
     */
    SystemLogBackupConfigDTO getBackupConfig();

    /**
     * 保存自动备份配置
     *
     * @param configDTO 备份配置
     * @return 是否成功
     */
    boolean saveBackupConfig(SystemLogBackupConfigDTO configDTO);

    /**
     * 清理过期日志
     *
     * @param retentionDays 保留天数
     * @return 清理的日志数量
     */
    int cleanLogs(Integer retentionDays);
}

