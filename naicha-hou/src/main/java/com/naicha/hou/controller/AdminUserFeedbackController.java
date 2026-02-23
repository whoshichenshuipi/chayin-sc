package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.UserFeedbackAssignDTO;
import com.naicha.hou.dto.UserFeedbackCompleteDTO;
import com.naicha.hou.dto.UserFeedbackDTO;
import com.naicha.hou.dto.UserFeedbackQueryDTO;
import com.naicha.hou.service.UserFeedbackService;
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
 * 管理员用户反馈管理控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/user-feedback")
@RequiredArgsConstructor
@Validated
@Tag(name = "管理员用户反馈管理", description = "管理员用户反馈管理相关接口")
public class AdminUserFeedbackController {

    private final UserFeedbackService userFeedbackService;

    /**
     * 分页查询用户反馈列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询用户反馈列表", description = "根据条件分页查询用户反馈列表")
    public Result<IPage<UserFeedbackDTO>> getFeedbackList(@Valid UserFeedbackQueryDTO queryDTO) {
        try {
            log.info("分页查询用户反馈列表，查询条件: {}", queryDTO);
            IPage<UserFeedbackDTO> page = userFeedbackService.getFeedbackPage(queryDTO);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            log.error("查询用户反馈列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取反馈详情
     */
    @GetMapping("/{feedbackId}")
    @Operation(summary = "获取反馈详情", description = "根据反馈ID获取反馈详细信息")
    public Result<UserFeedbackDTO> getFeedbackDetail(
            @Parameter(description = "反馈ID", required = true) 
            @PathVariable @NotNull Long feedbackId) {
        try {
            log.info("获取反馈详情，反馈ID: {}", feedbackId);
            UserFeedbackDTO feedback = userFeedbackService.getFeedbackDetail(feedbackId);
            return Result.success("查询成功", feedback);
        } catch (Exception e) {
            log.error("查询反馈详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 分配反馈处理
     */
    @PostMapping("/assign")
    @Operation(summary = "分配反馈处理", description = "将待处理的反馈分配给负责人")
    public Result<Boolean> assignFeedback(@Valid @RequestBody UserFeedbackAssignDTO assignDTO) {
        try {
            log.info("分配反馈处理，反馈ID: {}, 负责人: {}", assignDTO.getFeedbackId(), assignDTO.getAssignee());
            boolean success = userFeedbackService.assignFeedback(assignDTO);
            if (success) {
                return Result.success("分配成功", true);
            } else {
                return Result.error("分配失败");
            }
        } catch (Exception e) {
            log.error("分配反馈处理失败", e);
            return Result.error("分配失败: " + e.getMessage());
        }
    }

    /**
     * 完成反馈处理
     */
    @PostMapping("/complete")
    @Operation(summary = "完成反馈处理", description = "完成反馈处理并可选是否通知用户")
    public Result<Boolean> completeFeedback(@Valid @RequestBody UserFeedbackCompleteDTO completeDTO) {
        try {
            log.info("完成反馈处理，反馈ID: {}, 通知用户: {}", completeDTO.getFeedbackId(), completeDTO.getNotifyUser());
            boolean success = userFeedbackService.completeFeedback(completeDTO);
            if (success) {
                return Result.success("处理完成", true);
            } else {
                return Result.error("处理失败");
            }
        } catch (Exception e) {
            log.error("完成反馈处理失败", e);
            return Result.error("处理失败: " + e.getMessage());
        }
    }
}

