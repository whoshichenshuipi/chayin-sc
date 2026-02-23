package com.naicha.hou.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 注册请求DTO
 *
 * @author naicha
 * @since 2023-12-01
 */
@Data
@Schema(description = "注册请求")
public class RegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    @Schema(description = "用户名", example = "testuser")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Schema(description = "密码", example = "123456")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "123456")
    private String confirmPassword;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "test@example.com")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    @Schema(description = "昵称", example = "测试用户")
    private String nickname;

    /**
     * 用户类型：merchant-商家，user-普通用户
     */
    @NotBlank(message = "用户类型不能为空")
    @Schema(description = "用户类型", example = "user", allowableValues = {"merchant", "user"})
    private String userType;

    // 商家特有字段
    /**
     * 店铺名称（商家注册时必填）
     */
    @Schema(description = "店铺名称", example = "测试奶茶店")
    private String shopName;

    /**
     * 联系人姓名（商家注册时必填）
     */
    @Schema(description = "联系人姓名", example = "张三")
    private String contactName;

    /**
     * 联系电话（商家注册时必填）
     */
    @Schema(description = "联系电话", example = "13900139000")
    private String contactPhone;

    /**
     * 店铺地址（商家注册时必填）
     */
    @Schema(description = "店铺地址", example = "北京市朝阳区测试街道123号")
    private String address;

    /**
     * 真实姓名（管理员注册时必填）
     */
    @Schema(description = "真实姓名", example = "系统管理员")
    private String realName;
}
