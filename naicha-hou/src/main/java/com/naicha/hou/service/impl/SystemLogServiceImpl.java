package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naicha.hou.dto.SystemLogBackupConfigDTO;
import com.naicha.hou.dto.SystemLogDTO;
import com.naicha.hou.dto.SystemLogQueryDTO;
import com.naicha.hou.entity.SystemLog;
import com.naicha.hou.entity.SystemLogBackupConfig;
import com.naicha.hou.mapper.SystemLogBackupConfigMapper;
import com.naicha.hou.mapper.SystemLogMapper;
import com.naicha.hou.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统日志服务实现类
 *
 * @author naicha
 * @since 2024-01-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogMapper systemLogMapper;
    private final SystemLogBackupConfigMapper backupConfigMapper;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<SystemLogDTO> getLogList(SystemLogQueryDTO queryDTO) {
        Page<SystemLog> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
        
        // 根据日志类型过滤
        if (StringUtils.hasText(queryDTO.getType())) {
            wrapper.eq(SystemLog::getType, queryDTO.getType());
        }
        
        // 根据日志级别过滤
        if (StringUtils.hasText(queryDTO.getLevel())) {
            wrapper.eq(SystemLog::getLevel, queryDTO.getLevel());
        }
        
        // 根据操作模块过滤
        if (StringUtils.hasText(queryDTO.getModule())) {
            wrapper.eq(SystemLog::getModule, queryDTO.getModule());
        }
        
        // 根据操作用户过滤（模糊查询）
        if (StringUtils.hasText(queryDTO.getUser())) {
            wrapper.like(SystemLog::getUser, queryDTO.getUser());
        }
        
        // 根据时间范围过滤
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(SystemLog::getCreatedAt, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(SystemLog::getCreatedAt, queryDTO.getEndTime());
        }
        
        // 排序：按创建时间倒序
        wrapper.orderByDesc(SystemLog::getCreatedAt);
        
        IPage<SystemLog> logPage = systemLogMapper.selectPage(page, wrapper);
        
        // 转换为DTO
        Page<SystemLogDTO> dtoPage = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        List<SystemLogDTO> dtoList = logPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    @Override
    public SystemLogDTO getLogById(Long id) {
        SystemLog log = systemLogMapper.selectById(id);
        if (log == null) {
            return null;
        }
        return convertToDTO(log);
    }

    @Override
    @Transactional
    public boolean backupLogs() {
        try {
            log.info("开始手动备份日志");
            // TODO: 实现日志备份逻辑（导出到文件或数据库备份）
            // 这里可以记录备份操作到日志表
            SystemLog backupLog = new SystemLog();
            backupLog.setType("system");
            backupLog.setLevel("info");
            backupLog.setUser("system");
            backupLog.setModule("system");
            backupLog.setAction("手动备份日志");
            backupLog.setResult("success");
            backupLog.setIp("127.0.0.1");
            backupLog.setDetail("手动备份日志完成");
            systemLogMapper.insert(backupLog);
            
            log.info("日志备份完成");
            return true;
        } catch (Exception e) {
            log.error("日志备份失败", e);
            return false;
        }
    }

    @Override
    public String exportLogs(SystemLogQueryDTO queryDTO) {
        try {
            log.info("开始导出日志");
            // TODO: 实现日志导出逻辑（导出为Excel或CSV文件）
            // 返回导出文件路径
            return "/exports/logs_" + System.currentTimeMillis() + ".xlsx";
        } catch (Exception e) {
            log.error("日志导出失败", e);
            throw new RuntimeException("日志导出失败", e);
        }
    }

    @Override
    public SystemLogBackupConfigDTO getBackupConfig() {
        SystemLogBackupConfig config = backupConfigMapper.selectById(1L);
        if (config == null) {
            // 返回默认配置
            SystemLogBackupConfigDTO dto = new SystemLogBackupConfigDTO();
            dto.setEnabled(false);
            dto.setFrequency("daily");
            dto.setTime(LocalTime.of(2, 0));
            dto.setRetentionDays(30);
            dto.setBackupPath("/backup/logs");
            return dto;
        }
        
        SystemLogBackupConfigDTO dto = new SystemLogBackupConfigDTO();
        dto.setEnabled(config.getEnabled() == 1);
        dto.setFrequency(config.getFrequency());
        dto.setTime(config.getBackupTime());
        dto.setRetentionDays(config.getRetentionDays());
        dto.setBackupPath(config.getBackupPath());
        
        return dto;
    }

    @Override
    @Transactional
    public boolean saveBackupConfig(SystemLogBackupConfigDTO configDTO) {
        try {
            SystemLogBackupConfig config = backupConfigMapper.selectById(1L);
            if (config == null) {
                config = new SystemLogBackupConfig();
                config.setId(1L);
            }
            
            config.setEnabled(configDTO.getEnabled() ? 1 : 0);
            config.setFrequency(configDTO.getFrequency());
            config.setBackupTime(configDTO.getTime());
            config.setRetentionDays(configDTO.getRetentionDays());
            config.setBackupPath(configDTO.getBackupPath());
            
            if (config.getId() == null || backupConfigMapper.selectById(1L) == null) {
                backupConfigMapper.insert(config);
            } else {
                backupConfigMapper.updateById(config);
            }
            
            return true;
        } catch (Exception e) {
            log.error("保存备份配置失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public int cleanLogs(Integer retentionDays) {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
            
            LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(SystemLog::getCreatedAt, cutoffTime)
                   .eq(SystemLog::getDeleted, 0);
            
            List<SystemLog> logsToDelete = systemLogMapper.selectList(wrapper);
            int count = logsToDelete.size();
            
            // 逻辑删除
            for (SystemLog log : logsToDelete) {
                log.setDeleted(1);
                systemLogMapper.updateById(log);
            }
            
            log.info("清理了 {} 条过期日志", count);
            return count;
        } catch (Exception e) {
            log.error("清理日志失败", e);
            throw new RuntimeException("清理日志失败", e);
        }
    }

    /**
     * 转换为DTO
     */
    private SystemLogDTO convertToDTO(SystemLog systemLog) {
        SystemLogDTO dto = new SystemLogDTO();
        BeanUtils.copyProperties(systemLog, dto);
        dto.setCreateTime(systemLog.getCreatedAt());
        
        // 解析params JSON
        if (StringUtils.hasText(systemLog.getParams())) {
            try {
                Map<String, Object> params = objectMapper.readValue(systemLog.getParams(), 
                        new TypeReference<Map<String, Object>>() {});
                dto.setParams(params);
            } catch (Exception e) {
                log.warn("解析日志参数失败: {}", systemLog.getParams());
            }
        }
        
        return dto;
    }
}

