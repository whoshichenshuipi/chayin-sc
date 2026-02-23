package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.CouponProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 优惠券商品关联Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface CouponProductMapper extends BaseMapper<CouponProduct> {

    /**
     * 根据优惠券ID查询商品ID列表
     *
     * @param couponId 优惠券ID
     * @return 商品ID列表
     */
    List<Long> selectProductIdsByCouponId(@Param("couponId") Long couponId);

    /**
     * 批量插入优惠券商品关联
     *
     * @param couponId 优惠券ID
     * @param productIds 商品ID列表
     */
    void batchInsert(@Param("couponId") Long couponId, @Param("productIds") List<Long> productIds);

    /**
     * 根据优惠券ID删除关联关系
     *
     * @param couponId 优惠券ID
     */
    void deleteByCouponId(@Param("couponId") Long couponId);
}

