package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.PromotionProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 促销活动商品关联Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface PromotionProductMapper extends BaseMapper<PromotionProduct> {

    /**
     * 根据促销活动ID查询商品ID列表
     *
     * @param promotionId 促销活动ID
     * @return 商品ID列表
     */
    List<Long> selectProductIdsByPromotionId(@Param("promotionId") Long promotionId);

    /**
     * 根据促销活动ID查询赠送商品ID列表
     *
     * @param promotionId 促销活动ID
     * @return 赠送商品ID列表
     */
    List<Long> selectGiftProductIdsByPromotionId(@Param("promotionId") Long promotionId);

    /**
     * 批量插入促销活动商品关联
     *
     * @param promotionId 促销活动ID
     * @param productIds 商品ID列表
     */
    void batchInsert(@Param("promotionId") Long promotionId, @Param("productIds") List<Long> productIds);

    /**
     * 批量插入赠送商品关联
     *
     * @param promotionId 促销活动ID
     * @param giftProductIds 赠送商品ID列表
     */
    void batchInsertGiftProducts(@Param("promotionId") Long promotionId, @Param("giftProductIds") List<Long> giftProductIds);

    /**
     * 根据促销活动ID删除关联关系
     *
     * @param promotionId 促销活动ID
     */
    void deleteByPromotionId(@Param("promotionId") Long promotionId);
}

