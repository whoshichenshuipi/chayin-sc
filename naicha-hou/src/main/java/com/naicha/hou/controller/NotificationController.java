package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.NotificationCreateDTO;
import com.naicha.hou.dto.NotificationDTO;
import com.naicha.hou.dto.NotificationQueryDTO;
import com.naicha.hou.dto.NotificationSettingDTO;
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
import java.util.List;

/**
 * 通知管理控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
@Tag(name = "通知管理", description = "通知查询、管理、设置等接口")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 创建通知
     */
    @PostMapping
    @Operation(summary = "创建通知", description = "创建一条新通知")
    public Result<Long> createNotification(@Valid @RequestBody NotificationCreateDTO createDTO) {
        log.info("创建通知: {}", createDTO);
        Long id = notificationService.createNotification(createDTO);
        return Result.success("创建成功", id);
    }

    /**
     * 分页查询通知列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询通知列表", description = "根据条件分页查询通知列表")
    public Result<IPage<NotificationDTO>> getNotificationPage(
            @Parameter(description = "接收者ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Parameter(description = "接收者类型") 
            @RequestHeader(value = "X-User-Type", required = false, defaultValue = "merchant") String userType,
            @Valid @RequestBody NotificationQueryDTO queryDTO) {
        
        log.info("分页查询通知列表，用户ID: {}, 用户类型: {}, 查询条件: {}", userId, userType, queryDTO);
        
        queryDTO.setRecipientId(userId);
        queryDTO.setRecipientType(userType);
        
        IPage<NotificationDTO> result = notificationService.getNotificationPage(queryDTO);
        return Result.success("查询成功", result);
    }

    /**
     * 查询通知详情
     */
    @GetMapping("/{notificationId}")
    @Operation(summary = "查询通知详情", description = "根据通知ID查询通知详细信息")
    public Result<NotificationDTO> getNotificationDetail(
            @Parameter(description = "通知ID", required = true) 
            @PathVariable @NotNull Long notificationId) {
        
        log.info("查询通知详情，通知ID: {}", notificationId);
        
        NotificationDTO dto = notificationService.getNotificationDetail(notificationId);
        if (dto == null) {
            return Result.error(404, "通知不存在");
        }
        
        return Result.success("查询成功", dto);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "标记通知为已读", description = "标记指定通知为已读")
    public Result<Boolean> markAsRead(
            @Parameter(description = "通知ID", required = true) 
            @PathVariable @NotNull Long notificationId,
            @Parameter(description = "接收者ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Parameter(description = "接收者类型") 
            @RequestHeader(value = "X-User-Type", required = false, defaultValue = "merchant") String userType) {
        
        log.info("标记通知为已读，通知ID: {}, 用户ID: {}, 用户类型: {}", notificationId, userId, userType);
        
        boolean result = notificationService.markAsRead(notificationId, userId, userType);
        return result ? Result.success("标记成功") : Result.error(400, "标记失败");
    }

    /**
     * 批量标记为已读
     */
    @PutMapping("/batch-read")
    @Operation(summary = "批量标记为已读", description = "批量标记通知为已读")
    public Result<Boolean> batchMarkAsRead(
            @Parameter(description = "通知ID列表") 
            @RequestBody List<Long> notificationIds,
            @Parameter(description = "接收者ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Parameter(description = "接收者类型") 
            @RequestHeader(value = "X-User-Type", required = false, defaultValue = "merchant") String userType) {
        
        log.info("批量标记为已读，通知ID列表: {}, 用户ID: {}, 用户类型: {}", notificationIds, userId, userType);
        
        boolean result = notificationService.batchMarkAsRead(notificationIds, userId, userType);
        return result ? Result.success("标记成功") : Result.error(400, "标记失败");
    }

    /**
     * 标记所有为已读
     */
    @PutMapping("/mark-all-read")
    @Operation(summary = "标记所有为已读", description = "标记所有通知为已读")
    public Result<Boolean> markAllAsRead(
            @Parameter(description = "接收者ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Parameter(description = "接收者类型") 
            @RequestHeader(value = "X-User-Type", required = false, defaultValue = "merchant") String userType) {
        
        log.info("标记所有为已读，用户ID: {}, 用户类型: {}", userId, userType);
        
        boolean result = notificationService.markAllAsRead(userId, userType);
        return result ? Result.success("标记成功") : Result.error(400, "标记失败");
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "删除通知", description = "删除指定通知")
    public Result<Boolean> deleteNotification(
            @Parameter(description = "通知ID", required = true) 
            @PathVariable @NotNull Long notificationId,
            @Parameter(description = "接收者ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Parameter(description = "接收者类型") 
            @RequestHeader(value = "X-User-Type", required = false, defaultValue = "merchant") String userType) {
        
        log.info("删除通知，通知ID: {}, 用户ID: {}, 用户类型: {}", notificationId, userId, userType);
        
        boolean result = notificationService.deleteNotification(notificationId, userId, userType);
        return result ? Result.success("删除成功") : Result.error(400, "删除失败");
    }

    /**
     * 批量删除通知
     */
    @DeleteMapping("/batch-delete")
    @Operation(summary = "批量删除通知", description = "批量删除通知")
    public Result<Boolean> batchDeleteNotifications(
            @Parameter(description = "通知ID列表") 
            @RequestBody List<Long> notificationIds,
            @Parameter(description = "接收者ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Parameter(description = "接收者类型") 
            @RequestHeader(value = "X-User-Type", required = false, defaultValue = "merchant") String userType) {
        
        log.info("批量删除通知，通知ID列表: {}, 用户ID: {}, 用户类型: {}", notificationIds, userId, userType);
        
        boolean result = notificationService.batchDeleteNotifications(notificationIds, userId, userType);
        return result ? Result.success("删除成功") : Result.error(400, "删除失败");
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数量", description = "获取当前用户的未读通知数量")
    public Result<Integer> getUnreadCount(
            @Parameter(description = "接收者ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Parameter(description = "接收者类型") 
            @RequestHeader(value = "X-User-Type", required = false, defaultValue = "merchant") String userType) {
        
        log.info("获取未读通知数量，用户ID: {}, 用户类型: {}", userId, userType);
        
        Integer count = notificationService.getUnreadCount(userId, userType);
        return Result.success("查询成功", count);
    }

    /**
     * 获取通知统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取通知统计", description = "获取通知统计数据")
    public Result<NotificationStatsDTO> getNotificationStats(
            @Parameter(description = "接收者ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Parameter(description = "接收者类型") 
            @RequestHeader(value = "X-User-Type", required = false, defaultValue = "merchant") String userType) {
        
        log.info("获取通知统计，用户ID: {}, 用户类型: {}", userId, userType);
        
        NotificationStatsDTO stats = notificationService.getNotificationStats(userId, userType);
        return Result.success("查询成功", stats);
    }

    /**
     * 获取通知设置
     */
    @GetMapping("/settings")
    @Operation(summary = "获取通知设置", description = "获取当前用户的通知设置")
    public Result<NotificationSettingDTO> getNotificationSettings(
            @Parameter(description = "用户ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Parameter(description = "用户类型") 
            @RequestHeader(value = "X-User-Type", required = false, defaultValue = "merchant") String userType) {
        
        log.info("获取通知设置，用户ID: {}, 用户类型: {}", userId, userType);
        
        NotificationSettingDTO settings = notificationService.getNotificationSettings(userId, userType);
        return Result.success("查询成功", settings);
    }

    /**
     * 更新通知设置
     */
    @PutMapping("/settings")
    @Operation(summary = "更新通知设置", description = "更新当前用户的通知设置")
    public Result<Boolean> updateNotificationSettings(
            @Parameter(description = "用户ID") 
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Parameter(description = "用户类型") 
            @RequestHeader(value = "X-User-Type", required = false, defaultValue = "merchant") String userType,
            @Valid @RequestBody NotificationSettingDTO settingsDTO) {
        
        log.info("更新通知设置，用户ID: {}, 用户类型: {}, 设置: {}", userId, userType, settingsDTO);
        
        boolean result = notificationService.updateNotificationSettings(userId, userType, settingsDTO);
        return result ? Result.success("更新成功") : Result.error(400, "更新失败");
    }
}

