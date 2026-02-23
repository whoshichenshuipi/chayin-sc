package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.ShopReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 店铺评价Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface ShopReviewMapper extends BaseMapper<ShopReview> {

    /**
     * 根据店铺ID查询评价列表
     *
     * @param shopId 店铺ID
     * @return 评价列表
     */
    List<ShopReview> selectByShopId(@Param("shopId") Long shopId);

    /**
     * 根据店铺ID统计评价数量
     *
     * @param shopId 店铺ID
     * @return 评价数量
     */
    Long countByShopId(@Param("shopId") Long shopId);

    /**
     * 根据店铺ID查询平均评分
     *
     * @param shopId 店铺ID
     * @return 平均评分
     */
    Double selectAverageRating(@Param("shopId") Long shopId);
}
