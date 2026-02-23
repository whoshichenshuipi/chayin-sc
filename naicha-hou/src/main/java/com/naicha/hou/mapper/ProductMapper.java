package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 根据商家ID查询商品列表
     *
     * @param merchantId 商家ID
     * @return 商品列表
     */
    List<Product> selectByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 根据分类ID查询商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<Product> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据状态查询商品列表
     *
     * @param merchantId 商家ID
     * @param status 商品状态
     * @return 商品列表
     */
    List<Product> selectByStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status);

    /**
     * 查询库存预警商品
     *
     * @param merchantId 商家ID
     * @return 商品列表
     */
    List<Product> selectLowStockProducts(@Param("merchantId") Long merchantId);

    /**
     * 根据商户ID和促销标签查询商品ID列表
     * 用于补充促销活动的商品ID（当promotion_product表中没有记录时）
     *
     * @param merchantId 商户ID
     * @param promotionTags 促销标签列表（如 ["discount", "flash-sale"]）
     * @return 商品ID列表
     */
    List<Long> selectProductIdsByPromotionTags(@Param("merchantId") Long merchantId, 
                                                 @Param("promotionTags") List<String> promotionTags);
}

