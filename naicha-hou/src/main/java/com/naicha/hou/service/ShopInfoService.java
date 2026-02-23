package com.naicha.hou.service;

import com.naicha.hou.dto.ShopInfoDTO;
import com.naicha.hou.entity.ShopReview;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 店铺信息服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface ShopInfoService {

    /**
     * 获取店铺完整信息
     *
     * @param merchantId 商家ID
     * @return 店铺信息DTO
     */
    ShopInfoDTO getShopInfo(Long merchantId);

    /**
     * 更新店铺基础信息
     *
     * @param merchantId 商家ID
     * @param shopName 店铺名称
     * @param shopLogo 店铺Logo
     * @param description 店铺简介
     * @param contactPhone 联系电话
     * @param address 店铺地址
     * @param longitude 经度
     * @param latitude 纬度
     * @param weekdayOpenTime 工作日开始时间
     * @param weekdayCloseTime 工作日结束时间
     * @param weekendOpenTime 周末开始时间
     * @param weekendCloseTime 周末结束时间
     */
    void updateShopBasicInfo(Long merchantId, String shopName, String shopLogo, String description,
                              String contactPhone, String address, BigDecimal longitude, BigDecimal latitude,
                              String weekdayOpenTime, String weekdayCloseTime,
                              String weekendOpenTime, String weekendCloseTime);

    /**
     * 更新店铺营业状态和公告
     *
     * @param merchantId 商家ID
     * @param businessStatus 营业状态
     * @param announcement 店铺公告
     */
    void updateShopStatus(Long merchantId, Integer businessStatus, String announcement);

    /**
     * 获取店铺评分统计
     *
     * @param merchantId 商家ID
     * @return 评分统计信息
     */
    Map<String, Object> getRatingStats(Long merchantId);

    /**
     * 获取评价列表
     *
     * @param merchantId 商家ID
     * @param rating 评分筛选
     * @return 评价列表
     */
    List<ShopReview> getReviewList(Long merchantId, Integer rating);

    /**
     * 回复评价
     *
     * @param merchantId 商家ID
     * @param reviewId 评价ID
     * @param reply 回复内容
     */
    void replyReview(Long merchantId, Long reviewId, String reply);
}
