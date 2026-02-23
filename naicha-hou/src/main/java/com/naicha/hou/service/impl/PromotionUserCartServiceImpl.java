package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.naicha.hou.entity.PromotionUserCart;
import com.naicha.hou.mapper.PromotionUserCartMapper;
import com.naicha.hou.service.PromotionUserCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户营销活动参与记录服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionUserCartServiceImpl implements PromotionUserCartService {

    private final PromotionUserCartMapper promotionUserCartMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdatePromotionUserCart(Long userId, Long promotionId, Long productId,
                                               String cartItemId, Integer quantity,
                                               java.math.BigDecimal originalPrice, 
                                               java.math.BigDecimal promotionPrice,
                                               java.math.BigDecimal discountAmount) {
        log.info("保存或更新用户营销活动参与记录，用户ID: {}, 活动ID: {}, 商品ID: {}, 购物车项ID: {}", 
            userId, promotionId, productId, cartItemId);

        // 检查用户是否已参与该活动（每个用户对同一活动只能参与一次）
        List<PromotionUserCart> existingPromotions = promotionUserCartMapper.selectByUserIdAndPromotionId(userId, promotionId);
        if (!existingPromotions.isEmpty()) {
            // 用户已参与该活动，检查是否是同一个购物车项
            PromotionUserCart existingPromotion = existingPromotions.stream()
                .filter(p -> cartItemId.equals(p.getCartItemId()))
                .findFirst()
                .orElse(null);
            
            if (existingPromotion != null) {
                // 更新同一购物车项的记录
                existingPromotion.setProductId(productId);
                existingPromotion.setQuantity(quantity);
                existingPromotion.setOriginalPrice(originalPrice);
                existingPromotion.setPromotionPrice(promotionPrice);
                existingPromotion.setDiscountAmount(discountAmount);
                existingPromotion.setUpdatedAt(LocalDateTime.now());
                promotionUserCartMapper.updateById(existingPromotion);
                log.info("更新用户营销活动参与记录成功，记录ID: {}", existingPromotion.getId());
                return existingPromotion.getId();
            } else {
                // 用户已参与该活动，但购物车项不同，不允许重复参与
                log.warn("用户已参与该营销活动，不允许重复参与，用户ID: {}, 活动ID: {}", userId, promotionId);
                throw new com.naicha.hou.exception.BusinessException(
                    com.naicha.hou.common.ResultCode.BUSINESS_ERROR, 
                    "您已参与过该营销活动，每个用户对同一活动只能参与一次"
                );
            }
        }

        // 查询是否已存在相同购物车项ID的记录
        LambdaQueryWrapper<PromotionUserCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromotionUserCart::getUserId, userId)
               .eq(PromotionUserCart::getPromotionId, promotionId)
               .eq(PromotionUserCart::getCartItemId, cartItemId)
               .eq(PromotionUserCart::getStatus, "active");

        PromotionUserCart existing = promotionUserCartMapper.selectOne(wrapper);

        if (existing != null) {
            // 更新现有记录
            existing.setProductId(productId);
            existing.setQuantity(quantity);
            existing.setOriginalPrice(originalPrice);
            existing.setPromotionPrice(promotionPrice);
            existing.setDiscountAmount(discountAmount);
            existing.setUpdatedAt(LocalDateTime.now());
            promotionUserCartMapper.updateById(existing);
            log.info("更新用户营销活动参与记录成功，记录ID: {}", existing.getId());
            return existing.getId();
        } else {
            // 创建新记录
            PromotionUserCart record = new PromotionUserCart();
            record.setUserId(userId);
            record.setPromotionId(promotionId);
            record.setProductId(productId);
            record.setCartItemId(cartItemId);
            record.setQuantity(quantity);
            record.setOriginalPrice(originalPrice);
            record.setPromotionPrice(promotionPrice);
            record.setDiscountAmount(discountAmount);
            record.setStatus("active");
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            promotionUserCartMapper.insert(record);
            log.info("创建用户营销活动参与记录成功，记录ID: {}", record.getId());
            return record.getId();
        }
    }

    @Override
    public List<PromotionUserCart> getByUserIdAndCartItemId(Long userId, String cartItemId) {
        return promotionUserCartMapper.selectByUserIdAndCartItemId(userId, cartItemId);
    }

    @Override
    public List<PromotionUserCart> getByUserIdAndCartItemIds(Long userId, List<String> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return List.of();
        }
        return promotionUserCartMapper.selectByUserIdAndCartItemIds(userId, cartItemIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatusToUsed(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        promotionUserCartMapper.batchUpdateStatusToUsed(ids);
        log.info("批量更新营销活动参与记录状态为已使用，记录数量: {}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByUserIdAndCartItemId(Long userId, String cartItemId) {
        LambdaQueryWrapper<PromotionUserCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromotionUserCart::getUserId, userId)
               .eq(PromotionUserCart::getCartItemId, cartItemId);
        promotionUserCartMapper.delete(wrapper);
        log.info("删除用户营销活动参与记录，用户ID: {}, 购物车项ID: {}", userId, cartItemId);
    }

    @Override
    public boolean hasUserParticipatedPromotion(Long userId, Long promotionId) {
        List<PromotionUserCart> records = promotionUserCartMapper.selectByUserIdAndPromotionId(userId, promotionId);
        return records != null && !records.isEmpty();
    }

    @Override
    public List<PromotionUserCart> getByUserIdAndPromotionId(Long userId, Long promotionId) {
        return promotionUserCartMapper.selectByUserIdAndPromotionId(userId, promotionId);
    }
}

