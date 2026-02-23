package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 店铺信息DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class ShopInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺Logo
     */
    private String shopLogo;

    /**
     * 店铺描述
     */
    private String description;

    /**
     * 联系电话
     */
    private String contactPhone;

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
     * 营业状态 1-营业中 2-休息中 3-暂停营业
     */
    private Integer businessStatus;

    /**
     * 店铺公告
     */
    private String announcement;

    /**
     * 工作日营业时间
     */
    private List<String> weekdayHours;

    /**
     * 周末营业时间
     */
    private List<String> weekendHours;
}
