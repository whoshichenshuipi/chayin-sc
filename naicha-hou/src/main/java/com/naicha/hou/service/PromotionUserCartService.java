package com.naicha.hou.service;

import com.naicha.hou.entity.PromotionUserCart;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户营销活动参与记录服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface PromotionUserCartService {

    /**
     * 创建或更新用户营销活动参与记录
     *
     * @param userId 用户ID
     * @param promotionId 营销活动ID
     * @param productId 商品ID
     * @param cartItemId 购物车项ID
     * @param quantity 商品数量
     * @param originalPrice 商品原价
     * @param promotionPrice 促销价格
     * @param discountAmount 优惠金额
     * @return 记录ID
     */
    Long saveOrUpdatePromotionUserCart(Long userId, Long promotionId, Long productId, 
                                       String cartItemId, Integer quantity, 
                                       BigDecimal originalPrice, BigDecimal promotionPrice, 
                                       BigDecimal discountAmount);

    /**
     * 根据用户ID和购物车项ID查询营销活动参与记录
     *
     * @param userId 用户ID
     * @param cartItemId 购物车项ID
     * @return 营销活动参与记录列表
     */
    List<PromotionUserCart> getByUserIdAndCartItemId(Long userId, String cartItemId);

    /**
     * 根据用户ID和购物车项ID列表查询营销活动参与记录
     *
     * @param userId 用户ID
     * @param cartItemIds 购物车项ID列表
     * @return 营销活动参与记录列表
     */
    List<PromotionUserCart> getByUserIdAndCartItemIds(Long userId, List<String> cartItemIds);

    /**
     * 批量更新状态为已使用
     *
     * @param ids 记录ID列表
     */
    void batchUpdateStatusToUsed(List<Long> ids);

    /**
     * 删除用户营销活动参与记录
     *
     * @param userId 用户ID
     * @param cartItemId 购物车项ID
     */
    void deleteByUserIdAndCartItemId(Long userId, String cartItemId);

    /**
     * 检查用户是否已参与指定活动（每个用户对同一活动只能参与一次）
     *
     * @param userId 用户ID
     * @param promotionId 营销活动ID
     * @return 如果已参与返回true，否则返回false
     */
    boolean hasUserParticipatedPromotion(Long userId, Long promotionId);

    /**
     * 根据用户ID和活动ID查询营销活动参与记录
     *
     * @param userId 用户ID
     * @param promotionId 营销活动ID
     * @return 营销活动参与记录列表
     */
    List<PromotionUserCart> getByUserIdAndPromotionId(Long userId, Long promotionId);
}

