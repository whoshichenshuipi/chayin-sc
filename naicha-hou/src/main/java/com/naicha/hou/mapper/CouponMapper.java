package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.dto.CouponDTO;
import com.naicha.hou.dto.CouponQueryDTO;
import com.naicha.hou.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 优惠券Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    /**
     * 分页查询优惠券列表
     *
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 优惠券列表
     */
    IPage<CouponDTO> selectCouponPage(Page<CouponDTO> page, @Param("query") CouponQueryDTO queryDTO);

    /**
     * 根据ID查询优惠券详情
     *
     * @param couponId 优惠券ID
     * @return 优惠券详情
     */
    CouponDTO selectCouponDetail(@Param("couponId") Long couponId);

    /**
     * 查询优惠券的领取人数
     *
     * @param couponId 优惠券ID
     * @return 领取人数
     */
    Integer selectReceivedCount(@Param("couponId") Long couponId);

    /**
     * 查询优惠券的使用人数
     *
     * @param couponId 优惠券ID
     * @return 使用人数
     */
    Integer selectUsedCount(@Param("couponId") Long couponId);
}

