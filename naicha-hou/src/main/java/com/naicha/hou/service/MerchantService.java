package com.naicha.hou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.dto.BusinessHoursUpdateRequest;
import com.naicha.hou.dto.MerchantAuditHistoryDTO;
import com.naicha.hou.dto.MerchantAuditQueryDTO;
import com.naicha.hou.dto.MerchantAuditRequestDTO;
import com.naicha.hou.dto.MerchantDTO;
import com.naicha.hou.dto.MerchantQueryDTO;
import com.naicha.hou.dto.MerchantRatingDTO;
import com.naicha.hou.dto.MerchantRatingQueryDTO;
import com.naicha.hou.dto.MerchantRatingStatsDTO;
import com.naicha.hou.dto.ShopInfoUpdateRequest;
import com.naicha.hou.entity.Merchant;
import com.naicha.hou.entity.MerchantSetting;

import java.util.List;

/**
 * 商家服务接口
 *
 * @author naicha
 * @since 2023-12-01
 */
public interface MerchantService {

    /**
     * 获取当前商家信息
     *
     * @param merchantId 商家ID
     * @return 商家信息
     */
    Merchant getMerchantInfo(Long merchantId);

    /**
     * 更新店铺信息
     *
     * @param merchantId 商家ID
     * @param request 更新请求
     * @return 更新后的商家信息
     */
    Merchant updateShopInfo(Long merchantId, ShopInfoUpdateRequest request);

    /**
     * 获取商家营业时间设置
     *
     * @param merchantId 商家ID
     * @return 营业时间设置
     */
    MerchantSetting getBusinessHours(Long merchantId);

    /**
     * 更新营业时间设置
     *
     * @param merchantId 商家ID
     * @param request 更新请求
     * @return 更新后的设置
     */
    MerchantSetting updateBusinessHours(Long merchantId, BusinessHoursUpdateRequest request);

    /**
     * 根据地址获取经纬度
     *
     * @param address 地址
     * @return 经纬度 [经度, 纬度]
     */
    Double[] getLocationByAddress(String address);

    /**
     * 分页查询商家列表（管理员用）
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<MerchantDTO> getMerchantPage(MerchantQueryDTO queryDTO);

    /**
     * 根据ID获取商家详情（管理员用）
     *
     * @param merchantId 商家ID
     * @return 商家详情
     */
    MerchantDTO getMerchantDetail(Long merchantId);

    /**
     * 更新商家状态
     *
     * @param merchantId 商家ID
     * @param status 状态 0-待审核 1-已审核 2-已拒绝 3-已禁用
     * @return 是否成功
     */
    boolean updateMerchantStatus(Long merchantId, Integer status);

    /**
     * 分页查询待审核商家列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<MerchantDTO> getPendingAuditMerchants(MerchantAuditQueryDTO queryDTO);

    /**
     * 分页查询审核历史
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<MerchantAuditHistoryDTO> getAuditHistory(MerchantAuditQueryDTO queryDTO);

    /**
     * 审核商家
     *
     * @param request 审核请求
     * @param auditorId 审核人ID
     * @return 是否成功
     */
    boolean auditMerchant(MerchantAuditRequestDTO request, Long auditorId);

    /**
     * 分页查询商家评分列表（管理员用）
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<MerchantRatingDTO> getMerchantRatingPage(MerchantRatingQueryDTO queryDTO);

    /**
     * 获取商家评分统计列表（管理员用）
     *
     * @param merchantIds 商家ID列表（可选，为空则查询所有商家）
     * @return 评分统计列表
     */
    List<MerchantRatingStatsDTO> getMerchantRatingStats(List<Long> merchantIds);

    /**
     * 根据商家ID获取评分统计（管理员用）
     *
     * @param merchantId 商家ID
     * @return 评分统计
     */
    MerchantRatingStatsDTO getMerchantRatingStatsById(Long merchantId);
}

