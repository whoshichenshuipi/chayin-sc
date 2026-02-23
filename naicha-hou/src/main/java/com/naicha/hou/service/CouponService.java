package com.naicha.hou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.dto.CouponCreateDTO;
import com.naicha.hou.dto.CouponDTO;
import com.naicha.hou.dto.CouponQueryDTO;
import com.naicha.hou.dto.UserCouponDTO;
import com.naicha.hou.dto.UserCouponQueryDTO;

/**
 * 优惠券服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface CouponService {

    /**
     * 创建优惠券
     *
     * @param merchantId 商家ID
     * @param createDTO 优惠券创建DTO
     * @return 优惠券ID
     */
    Long createCoupon(Long merchantId, CouponCreateDTO createDTO);

    /**
     * 更新优惠券
     *
     * @param couponId 优惠券ID
     * @param createDTO 优惠券创建DTO
     * @return 是否成功
     */
    boolean updateCoupon(Long couponId, CouponCreateDTO createDTO);

    /**
     * 删除优惠券
     *
     * @param couponId 优惠券ID
     * @return 是否成功
     */
    boolean deleteCoupon(Long couponId);

    /**
     * 根据ID查询优惠券详情
     *
     * @param couponId 优惠券ID
     * @return 优惠券详情DTO
     */
    CouponDTO getCouponById(Long couponId);

    /**
     * 分页查询优惠券列表
     *
     * @param queryDTO 查询条件
     * @return 分页数据
     */
    IPage<CouponDTO> getCouponList(CouponQueryDTO queryDTO);

    /**
     * 暂停/恢复优惠券
     *
     * @param couponId 优惠券ID
     * @return 是否成功
     */
    boolean togglePause(Long couponId);

    /**
     * 更新优惠券状态（自动更新状态，基于时间）
     *
     * @param couponId 优惠券ID
     * @return 是否成功
     */
    boolean updateCouponStatus(Long couponId);

    /**
     * 分页查询用户优惠券列表
     *
     * @param queryDTO 查询条件
     * @return 分页数据
     */
    IPage<UserCouponDTO> getUserCoupons(UserCouponQueryDTO queryDTO);

    /**
     * 用户领取优惠券
     *
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 是否成功
     */
    boolean receiveCoupon(Long userId, Long couponId);
}

