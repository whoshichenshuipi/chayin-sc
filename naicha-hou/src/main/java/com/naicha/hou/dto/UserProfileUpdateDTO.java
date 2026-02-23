package com.naicha.hou.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户个人信息更新DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户个人信息更新请求")
public class UserProfileUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 头像
     */
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    /**
     * 性别 0-未知 1-男 2-女
     */
    @Schema(description = "性别", example = "1")
    private Integer gender;

    /**
     * 生日
     */
    @Schema(description = "生日", example = "1990-01-01")
    private LocalDate birthday;

    /**
     * 地址
     */
    @Schema(description = "地址", example = "北京市朝阳区")
    private String address;
}

