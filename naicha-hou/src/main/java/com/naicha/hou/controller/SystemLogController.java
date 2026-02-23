package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.SystemLogBackupConfigDTO;
import com.naicha.hou.dto.SystemLogDTO;
import com.naicha.hou.dto.SystemLogQueryDTO;
import com.naicha.hou.service.SystemLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 系统日志控制器
 *
 * @author naicha
 * @since 2024-01-25
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system-log")
@RequiredArgsConstructor
@Validated
@Tag(name = "系统日志接口", description = "系统日志管理相关接口")
public class SystemLogController {

    private final SystemLogService systemLogService;

    /**
     * 分页查询日志列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询日志列表", description = "根据条件分页查询系统日志")
    public Result<IPage<SystemLogDTO>> getLogList(@Valid SystemLogQueryDTO queryDTO) {
        try {
            IPage<SystemLogDTO> page = systemLogService.getLogList(queryDTO);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            log.error("查询日志列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取日志详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取日志详情", description = "根据日志ID获取日志详细信息")
    public Result<SystemLogDTO> getLogById(
            @Parameter(description = "日志ID", required = true) @PathVariable @NotNull Long id) {
        try {
            SystemLogDTO log = systemLogService.getLogById(id);
            if (log == null) {
                return Result.error("日志不存在");
            }
            return Result.success("查询成功", log);
        } catch (Exception e) {
            log.error("查询日志详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 手动备份日志
     */
    @PostMapping("/backup")
    @Operation(summary = "手动备份日志", description = "手动触发日志备份操作")
    public Result<Boolean> backupLogs() {
        try {
            boolean success = systemLogService.backupLogs();
            if (success) {
                return Result.success("备份成功", true);
            } else {
                return Result.error("备份失败");
            }
        } catch (Exception e) {
            log.error("备份日志失败", e);
            return Result.error("备份失败: " + e.getMessage());
        }
    }

    /**
     * 导出日志
     */
    @PostMapping("/export")
    @Operation(summary = "导出日志", description = "根据条件导出日志")
    public Result<String> exportLogs(@Valid @RequestBody SystemLogQueryDTO queryDTO) {
        try {
            String filePath = systemLogService.exportLogs(queryDTO);
            return Result.success("导出成功", filePath);
        } catch (Exception e) {
            log.error("导出日志失败", e);
            return Result.error("导出失败: " + e.getMessage());
        }
    }

    /**
     * 获取自动备份配置
     */
    @GetMapping("/backup/config")
    @Operation(summary = "获取自动备份配置", description = "获取日志自动备份配置信息")
    public Result<SystemLogBackupConfigDTO> getBackupConfig() {
        try {
            SystemLogBackupConfigDTO config = systemLogService.getBackupConfig();
            return Result.success("获取配置成功", config);
        } catch (Exception e) {
            log.error("获取备份配置失败", e);
            return Result.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存自动备份配置
     */
    @PostMapping("/backup/config")
    @Operation(summary = "保存自动备份配置", description = "保存日志自动备份配置")
    public Result<Boolean> saveBackupConfig(@Valid @RequestBody SystemLogBackupConfigDTO configDTO) {
        try {
            boolean success = systemLogService.saveBackupConfig(configDTO);
            if (success) {
                return Result.success("保存配置成功", true);
            } else {
                return Result.error("保存配置失败");
            }
        } catch (Exception e) {
            log.error("保存备份配置失败", e);
            return Result.error("保存配置失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期日志
     */
    @PostMapping("/clean")
    @Operation(summary = "清理过期日志", description = "清理指定天数之前的日志")
    public Result<Integer> cleanLogs(
            @Parameter(description = "保留天数", required = true) @RequestParam @NotNull Integer retentionDays) {
        try {
            int count = systemLogService.cleanLogs(retentionDays);
            return Result.success("清理成功，共清理 " + count + " 条日志", count);
        } catch (Exception e) {
            log.error("清理日志失败", e);
            return Result.error("清理失败: " + e.getMessage());
        }
    }
}

