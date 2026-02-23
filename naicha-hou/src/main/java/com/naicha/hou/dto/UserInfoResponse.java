package com.naicha.hou.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 *
 * @author naicha
 * @since 2023-12-01
 */
@Data
@Schema(description = "用户信息响应")
public class UserInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "系统管理员")
    private String nickname;

    /**
     * 头像
     */
    @Schema(description = "头像", example = "https://example.com/avatar.jpg")
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

    /**
     * 状态 0-禁用 1-启用
     */
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
     * 用户类型
     */
    @Schema(description = "用户类型", example = "admin")
    private String userType;

    /**
     * 角色（管理员特有）
     */
    @Schema(description = "角色", example = "admin")
    private String role;

    /**
     * 真实姓名（管理员特有）
     */
    @Schema(description = "真实姓名", example = "系统管理员")
    private String realName;

    /**
     * 店铺名称（商家特有）
     */
    @Schema(description = "店铺名称", example = "测试奶茶店")
    private String shopName;

    /**
     * 店铺Logo（商家特有）
     */
    @Schema(description = "店铺Logo", example = "https://example.com/logo.jpg")
    private String shopLogo;

    /**
     * 联系人姓名（商家特有）
     */
    @Schema(description = "联系人姓名", example = "张三")
    private String contactName;

    /**
     * 联系电话（商家特有）
     */
    @Schema(description = "联系电话", example = "13900139000")
    private String contactPhone;

    /**
     * 联系邮箱（商家特有）
     */
    @Schema(description = "联系邮箱", example = "merchant@example.com")
    private String contactEmail;

    /**
     * 店铺描述（商家特有）
     */
    @Schema(description = "店铺描述", example = "专业的奶茶店")
    private String description;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2023-12-01T10:00:00")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2023-12-01T10:00:00")
    private LocalDateTime updatedAt;

    /**
     * 登录后跳转地址
     * 管理员和商家跳转到后台管理界面，普通用户跳转到前台界面
     */
    @Schema(description = "登录后跳转地址", example = "http://localhost:3001/admin/dashboard")
    private String redirectUrl;
}
