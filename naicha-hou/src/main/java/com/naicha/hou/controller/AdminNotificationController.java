package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.NotificationDTO;
import com.naicha.hou.dto.NotificationQueryDTO;
import com.naicha.hou.dto.NotificationStatsDTO;
import com.naicha.hou.service.NotificationService;
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
 * 管理员通知控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@Validated
@Tag(name = "管理员通知管理", description = "管理员通知相关接口")
public class AdminNotificationController {

    private final NotificationService notificationService;

    /**
     * 分页查询管理员通知列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询管理员通知列表", description = "根据条件分页查询管理员通知列表")
    public Result<IPage<NotificationDTO>> getNotificationPage(
            @Parameter(description = "管理员ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long adminId,
            @Valid @RequestBody NotificationQueryDTO queryDTO) {
        
        log.info("分页查询管理员通知列表，管理员ID: {}, 查询条件: {}", adminId, queryDTO);
        
        queryDTO.setRecipientId(adminId);
        queryDTO.setRecipientType("admin");
        
        IPage<NotificationDTO> result = notificationService.getNotificationPage(queryDTO);
        return Result.success("查询成功", result);
    }

    /**
     * 获取管理员通知统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取管理员通知统计", description = "获取管理员通知统计数据")
    public Result<NotificationStatsDTO> getStats(
            @Parameter(description = "管理员ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long adminId) {
        
        log.info("获取管理员通知统计，管理员ID: {}", adminId);
        
        NotificationStatsDTO stats = notificationService.getNotificationStats(adminId, "admin");
        return Result.success("查询成功", stats);
    }

    /**
     * 处理通知操作
     */
    @PostMapping("/{notificationId}/action/{actionId}")
    @Operation(summary = "处理通知操作", description = "处理通知中的操作按钮")
    public Result<Boolean> handleNotificationAction(
            @Parameter(description = "通知ID", required = true) 
            @PathVariable @NotNull Long notificationId,
            @Parameter(description = "操作ID", required = true) 
            @PathVariable @NotNull Long actionId) {
        
        log.info("处理通知操作，通知ID: {}, 操作ID: {}", notificationId, actionId);
        
        // TODO: 根据actionId执行相应操作（如审核商家入驻申请、处理申诉等）
        return Result.success("操作成功");
    }
}

