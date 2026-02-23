package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家实体类
 *
 * @author naicha
 * @since 2023-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("merchant")
public class Merchant implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商家ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 店铺名称
     */
    @TableField("shop_name")
    private String shopName;

    /**
     * 店铺Logo
     */
    @TableField("shop_logo")
    private String shopLogo;

    /**
     * 联系人姓名
     */
    @TableField("contact_name")
    private String contactName;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @TableField("contact_email")
    private String contactEmail;

    /**
     * 营业执照
     */
    @TableField("business_license")
    private String businessLicense;

    /**
     * 店铺地址
     */
    @TableField("address")
    private String address;

    /**
     * 经度
     */
    @TableField("longitude")
    private java.math.BigDecimal longitude;

    /**
     * 纬度
     */
    @TableField("latitude")
    private java.math.BigDecimal latitude;

    /**
     * 店铺描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态 0-待审核 1-已审核 2-已拒绝 3-已禁用
     */
    @TableField("status")
    private Integer status;

    /**
     * 营业状态 1-营业中 2-休息中 3-暂停营业
     */
    @TableField("business_status")
    private Integer businessStatus;

    /**
     * 店铺公告
     */
    @TableField("announcement")
    private String announcement;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 删除标志 0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
