package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class MerchantDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商家ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺Logo
     */
    private String shopLogo;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 营业执照
     */
    private String businessLicense;

    /**
     * 店铺地址
     */
    private String address;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 店铺描述
     */
    private String description;

    /**
     * 状态 0-待审核 1-已审核 2-已拒绝 3-已禁用
     */
    private Integer status;

    /**
     * 营业状态 1-营业中 2-休息中 3-暂停营业
     */
    private Integer businessStatus;

    /**
     * 店铺公告
     */
    private String announcement;

    /**
     * 评分（需要从评价表计算）
     */
    private BigDecimal rating;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后登录时间（需要从登录日志获取）
     */
    private LocalDateTime lastLogin;
}

