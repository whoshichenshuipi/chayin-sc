package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.PromotionUserCart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户营销活动参与记录Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface PromotionUserCartMapper extends BaseMapper<PromotionUserCart> {

    /**
     * 根据用户ID和购物车项ID查询营销活动参与记录
     *
     * @param userId 用户ID
     * @param cartItemId 购物车项ID
     * @return 营销活动参与记录列表
     */
    List<PromotionUserCart> selectByUserIdAndCartItemId(@Param("userId") Long userId, @Param("cartItemId") String cartItemId);

    /**
     * 根据用户ID和商品ID查询营销活动参与记录
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 营销活动参与记录列表
     */
    List<PromotionUserCart> selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 根据用户ID查询所有有效的营销活动参与记录
     *
     * @param userId 用户ID
     * @return 营销活动参与记录列表
     */
    List<PromotionUserCart> selectActiveByUserId(@Param("userId") Long userId);

    /**
     * 批量更新状态为已使用
     *
     * @param ids 记录ID列表
     */
    void batchUpdateStatusToUsed(@Param("ids") List<Long> ids);

    /**
     * 根据购物车项ID列表查询营销活动参与记录
     *
     * @param userId 用户ID
     * @param cartItemIds 购物车项ID列表
     * @return 营销活动参与记录列表
     */
    List<PromotionUserCart> selectByUserIdAndCartItemIds(@Param("userId") Long userId, @Param("cartItemIds") List<String> cartItemIds);

    /**
     * 根据用户ID和活动ID查询营销活动参与记录（检查用户是否已参与活动）
     *
     * @param userId 用户ID
     * @param promotionId 营销活动ID
     * @return 营销活动参与记录列表
     */
    List<PromotionUserCart> selectByUserIdAndPromotionId(@Param("userId") Long userId, @Param("promotionId") Long promotionId);
}

