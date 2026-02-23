package com.naicha.hou.controller;

import com.naicha.hou.common.Result;
import com.naicha.hou.dto.UserDTO;
import com.naicha.hou.dto.UserProfileUpdateDTO;
import com.naicha.hou.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户端接口", description = "用户端个人信息管理相关接口")
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息", description = "根据用户ID获取当前登录用户的详细信息")
    public Result<UserDTO> getCurrentUserProfile(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {
        try {
            log.info("获取当前用户信息，用户ID: {}", userId);
            UserDTO user = userService.getUserDetail(userId);
            return Result.success("查询成功", user);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新当前用户个人信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新当前用户个人信息", description = "更新当前登录用户的昵称、邮箱、手机号、头像、性别、生日、地址等信息")
    public Result<Boolean> updateCurrentUserProfile(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        try {
            log.info("更新用户个人信息，用户ID: {}, 更新信息: {}", userId, updateDTO);
            boolean success = userService.updateUserProfile(userId, updateDTO);
            if (success) {
                return Result.success("更新成功", true);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新当前用户个人信息（POST方法，兼容前端）
     */
    @PostMapping("/profile")
    @Operation(summary = "更新当前用户个人信息（POST）", description = "更新当前登录用户的个人信息，使用POST方法")
    public Result<Boolean> updateCurrentUserProfilePost(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        return updateCurrentUserProfile(userId, updateDTO);
    }
}

