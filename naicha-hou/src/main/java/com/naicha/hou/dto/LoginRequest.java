package com.naicha.hou.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 登录请求DTO
 *
 * @author naicha
 * @since 2023-12-01
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Schema(description = "密码", example = "123456")
    private String password;

    /**
     * 用户类型：admin-管理员，merchant-商家，user-普通用户
     */
    @NotBlank(message = "用户类型不能为空")
    @Schema(description = "用户类型", example = "admin", allowableValues = {"admin", "merchant", "user"})
    private String userType;
}
