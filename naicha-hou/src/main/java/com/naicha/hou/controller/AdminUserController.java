package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.UserDTO;
import com.naicha.hou.dto.UserQueryDTO;
import com.naicha.hou.dto.UserViolationRequestDTO;
import com.naicha.hou.service.UserService;
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
 * 管理员用户管理控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
@Validated
@Tag(name = "管理员用户管理", description = "管理员用户管理相关接口")
public class AdminUserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表", description = "根据条件分页查询用户列表")
    public Result<IPage<UserDTO>> getUserList(@Valid UserQueryDTO queryDTO) {
        try {
            log.info("分页查询用户列表，查询条件: {}", queryDTO);
            
            // 处理前端传入的状态字符串，转换为整数
            if (queryDTO.getStatus() == null && queryDTO.getKeyword() == null) {
                // 如果没有传入status，但前端可能传了字符串形式的status，需要通过type来判断
            }
            
            IPage<UserDTO> page = userService.getUserPage(queryDTO);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息，包括违规记录")
    public Result<UserDTO> getUserDetail(
            @Parameter(description = "用户ID", required = true) 
            @PathVariable @NotNull Long userId) {
        try {
            log.info("获取用户详情，用户ID: {}", userId);
            UserDTO user = userService.getUserDetail(userId);
            return Result.success("查询成功", user);
        } catch (Exception e) {
            log.error("查询用户详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 处理用户违规
     */
    @PostMapping("/violation")
    @Operation(summary = "处理用户违规", description = "对用户进行违规处理（警告、限制下单、冻结账号）")
    public Result<Boolean> handleUserViolation(
            @Parameter(description = "处理人", required = false) 
            @RequestHeader(value = "X-Admin-Username", required = false, defaultValue = "管理员") String handler,
            @Valid @RequestBody UserViolationRequestDTO requestDTO) {
        try {
            log.info("处理用户违规，用户ID: {}, 处理类型: {}, 处理人: {}", requestDTO.getUserId(), requestDTO.getType(), handler);
            boolean success = userService.handleUserViolation(requestDTO, handler);
            if (success) {
                return Result.success("处理成功", true);
            } else {
                return Result.error("处理失败");
            }
        } catch (Exception e) {
            log.error("处理用户违规失败", e);
            return Result.error("处理失败: " + e.getMessage());
        }
    }
}

