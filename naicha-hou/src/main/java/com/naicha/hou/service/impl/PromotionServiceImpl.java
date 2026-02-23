package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naicha.hou.common.ResultCode;
import com.naicha.hou.dto.*;
import com.naicha.hou.entity.Promotion;
import com.naicha.hou.exception.BusinessException;
import com.naicha.hou.mapper.PromotionMapper;
import com.naicha.hou.mapper.PromotionProductMapper;
import com.naicha.hou.mapper.ProductMapper;
import com.naicha.hou.service.PromotionService;
import com.naicha.hou.service.PromotionUserCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 促销活动服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionMapper promotionMapper;
    private final PromotionProductMapper promotionProductMapper;
    private final ProductMapper productMapper;
    private final PromotionUserCartService promotionUserCartService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPromotion(Long merchantId, PromotionCreateDTO createDTO) {
        log.info("创建促销活动，商家ID: {}, 活动名称: {}", merchantId, createDTO.getName());

        // 验证时间范围
        validateTimeRange(createDTO.getTimeRange());

        // 验证活动类型相关字段
        validatePromotionFields(createDTO);

        // 构建促销活动实体
        Promotion promotion = new Promotion();
        promotion.setMerchantId(merchantId);
        promotion.setName(createDTO.getName());
        promotion.setType(createDTO.getType());
        promotion.setDescription(createDTO.getDescription());
        promotion.setStartTime(createDTO.getTimeRange().get(0));
        promotion.setEndTime(createDTO.getTimeRange().get(1));
        promotion.setStatus(determineInitialStatus(promotion.getStartTime()));
        promotion.setParticipantCount(0);
        promotion.setOrderCount(0);
        promotion.setSalesAmount(BigDecimal.ZERO);
        promotion.setDiscountAmount(BigDecimal.ZERO);
        promotion.setCreatedAt(LocalDateTime.now());
        promotion.setUpdatedAt(LocalDateTime.now());

        // 根据活动类型设置相关字段
        setPromotionTypeFields(promotion, createDTO);

        // 保存促销活动
        promotionMapper.insert(promotion);

        // 保存促销活动商品关联
        savePromotionProducts(promotion.getId(), createDTO);

        log.info("促销活动创建成功，活动ID: {}", promotion.getId());
        return promotion.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePromotion(Long promotionId, PromotionCreateDTO createDTO) {
        log.info("更新促销活动，活动ID: {}", promotionId);

        Promotion promotion = promotionMapper.selectById(promotionId);
        if (promotion == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "促销活动不存在");
        }

        // 验证时间范围
        validateTimeRange(createDTO.getTimeRange());

        // 验证活动类型相关字段
        validatePromotionFields(createDTO);

        // 更新促销活动信息
        promotion.setName(createDTO.getName());
        promotion.setType(createDTO.getType());
        promotion.setDescription(createDTO.getDescription());
        promotion.setStartTime(createDTO.getTimeRange().get(0));
        promotion.setEndTime(createDTO.getTimeRange().get(1));
        promotion.setUpdatedAt(LocalDateTime.now());

        // 根据活动类型设置相关字段
        setPromotionTypeFields(promotion, createDTO);

        // 更新状态
        updatePromotionStatus(promotion);

        promotionMapper.updateById(promotion);

        // 更新促销活动商品关联（先删除旧的，再插入新的）
        promotionProductMapper.deleteByPromotionId(promotionId);
        savePromotionProducts(promotionId, createDTO);

        log.info("促销活动更新成功，活动ID: {}", promotionId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePromotion(Long promotionId) {
        log.info("删除促销活动，活动ID: {}", promotionId);

        Promotion promotion = promotionMapper.selectById(promotionId);
        if (promotion == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "促销活动不存在");
        }

        // 只有未开始或已结束的促销活动可以删除
        if (!"pending".equals(promotion.getStatus()) && !"ended".equals(promotion.getStatus())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "只能删除未开始或已结束的促销活动");
        }

        // 逻辑删除
        promotionMapper.deleteById(promotionId);

        log.info("促销活动删除成功，活动ID: {}", promotionId);
        return true;
    }

    @Override
    public PromotionDTO getPromotionById(Long promotionId) {
        PromotionDTO promotionDTO = promotionMapper.selectPromotionDetail(promotionId);
        if (promotionDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "促销活动不存在");
        }

        // 填充详细信息
        enrichPromotionDTO(promotionDTO);

        return promotionDTO;
    }

    @Override
    public IPage<PromotionDTO> getPromotionList(PromotionQueryDTO queryDTO) {
        Page<PromotionDTO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<PromotionDTO> result = promotionMapper.selectPromotionPage(page, queryDTO);

        // 为每个促销活动填充详细信息
        result.getRecords().forEach(this::enrichPromotionDTO);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean togglePause(Long promotionId) {
        log.info("暂停/恢复促销活动，活动ID: {}", promotionId);

        Promotion promotion = promotionMapper.selectById(promotionId);
        if (promotion == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "促销活动不存在");
        }

        if ("paused".equals(promotion.getStatus())) {
            // 恢复：根据时间重新判断状态
            updatePromotionStatus(promotion);
        } else if ("active".equals(promotion.getStatus())) {
            // 暂停
            promotion.setStatus("paused");
        } else {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "只能暂停或恢复进行中的促销活动");
        }

        promotion.setUpdatedAt(LocalDateTime.now());
        promotionMapper.updateById(promotion);

        log.info("促销活动状态更新成功，活动ID: {}, 新状态: {}", promotionId, promotion.getStatus());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean endPromotion(Long promotionId) {
        log.info("结束促销活动，活动ID: {}", promotionId);

        Promotion promotion = promotionMapper.selectById(promotionId);
        if (promotion == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "促销活动不存在");
        }

        if ("ended".equals(promotion.getStatus())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "促销活动已结束");
        }

        promotion.setStatus("ended");
        promotion.setUpdatedAt(LocalDateTime.now());
        promotionMapper.updateById(promotion);

        log.info("促销活动已结束，活动ID: {}", promotionId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePromotionStatus(Long promotionId) {
        Promotion promotion = promotionMapper.selectById(promotionId);
        if (promotion == null) {
            return false;
        }

        String oldStatus = promotion.getStatus();
        updatePromotionStatus(promotion);

        if (!oldStatus.equals(promotion.getStatus())) {
            promotion.setUpdatedAt(LocalDateTime.now());
            promotionMapper.updateById(promotion);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean participatePromotion(Long userId, Long promotionId) {
        log.info("用户参与促销活动，用户ID: {}, 活动ID: {}", userId, promotionId);

        // 1. 查询活动信息
        Promotion promotion = promotionMapper.selectById(promotionId);
        if (promotion == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "促销活动不存在");
        }

        // 2. 检查活动状态
        updatePromotionStatus(promotion);
        if (!"active".equals(promotion.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                "活动状态不可参与，当前状态: " + promotion.getStatus());
        }

        // 3. 检查活动时间
        LocalDateTime now = LocalDateTime.now();
        if (promotion.getStartTime() != null && now.isBefore(promotion.getStartTime())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "活动尚未开始");
        }
        if (promotion.getEndTime() != null && now.isAfter(promotion.getEndTime())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "活动已结束");
        }

        // 4. 检查用户是否已参与该活动（每个用户对同一活动只能参与一次）
        // 必须在更新参与人数之前检查，如果已参与则直接返回，不更新参与人数
        boolean hasParticipated = false;
        try {
            hasParticipated = promotionUserCartService.hasUserParticipatedPromotion(userId, promotionId);
        } catch (Exception e) {
            // 如果检查失败（可能是表不存在），记录警告
            log.warn("检查用户营销活动参与状态失败，用户ID: {}, 活动ID: {}, 错误: {}", 
                userId, promotionId, e.getMessage());
            // 检查失败时，假设用户未参与，继续执行（但实际应该确保表存在）
        }
        
        if (hasParticipated) {
            log.warn("用户已参与该营销活动，不允许重复参与，用户ID: {}, 活动ID: {}", userId, promotionId);
            throw new BusinessException(ResultCode.BUSINESS_ERROR, 
                "你已参加活动，不要重复参加");
        }

        // 5. 更新参与人数（只有在用户未参与时才更新）
        // 注意：这里简单实现，实际可以考虑使用 Redis 或数据库乐观锁
        Integer currentCount = promotion.getParticipantCount() != null ? promotion.getParticipantCount() : 0;
        promotion.setParticipantCount(currentCount + 1);
        promotion.setUpdatedAt(now);
        promotionMapper.updateById(promotion);

        // 6. 创建用户营销活动参与记录（用于后续检查参与状态）
        // 由于此时还没有添加到购物车，使用特殊的 cartItemId 标识
        try {
            // 获取活动的第一个商品ID（如果活动有商品）
            List<Long> productIds = promotionProductMapper.selectProductIdsByPromotionId(promotionId);
            Long productId = (productIds != null && !productIds.isEmpty()) ? productIds.get(0) : 0L;
            
            // 使用特殊的 cartItemId 标识"仅参与活动，未添加到购物车"
            String specialCartItemId = "promotion_participate_" + promotionId + "_" + userId;
            
            // 创建参与记录（价格等信息暂时为0，等添加到购物车时再更新）
            promotionUserCartService.saveOrUpdatePromotionUserCart(
                userId,
                promotionId,
                productId,
                specialCartItemId,
                1, // 数量为1
                java.math.BigDecimal.ZERO, // 原价暂时为0
                java.math.BigDecimal.ZERO, // 促销价格暂时为0
                java.math.BigDecimal.ZERO  // 优惠金额暂时为0
            );
            log.info("创建用户营销活动参与记录成功，用户ID: {}, 活动ID: {}, 购物车项ID: {}", 
                userId, promotionId, specialCartItemId);
        } catch (Exception e) {
            // 如果创建参与记录失败，记录警告但不影响参与流程
            log.warn("创建用户营销活动参与记录失败，用户ID: {}, 活动ID: {}, 错误: {}", 
                userId, promotionId, e.getMessage());
        }

        log.info("用户参与促销活动成功，用户ID: {}, 活动ID: {}, 当前参与人数: {}", 
            userId, promotionId, promotion.getParticipantCount());
        return true;
    }

    /**
     * 验证时间范围
     */
    private void validateTimeRange(List<LocalDateTime> timeRange) {
        if (timeRange == null || timeRange.size() != 2) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "活动时间范围格式错误");
        }
        if (timeRange.get(0).isAfter(timeRange.get(1))) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "活动开始时间不能晚于结束时间");
        }
    }

    /**
     * 验证活动类型相关字段
     */
    private void validatePromotionFields(PromotionCreateDTO createDTO) {
        String type = createDTO.getType();
        
        if ("discount".equals(type)) {
            // 限时折扣：需要商品ID和折扣比例
            if (createDTO.getProductIds() == null || createDTO.getProductIds().isEmpty()) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "限时折扣必须选择活动商品");
            }
            if (createDTO.getDiscountRate() == null) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "限时折扣必须设置折扣比例");
            }
        } else if ("full_reduce".equals(type)) {
            // 满减活动：需要满减规则
            // 注意：满减活动可以绑定商品（用于显示和筛选），但不强制要求
            if (createDTO.getFullReduceRules() == null || createDTO.getFullReduceRules().isEmpty()) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "满减活动必须设置满减规则");
            }
        } else if ("buy_one_get_one".equals(type)) {
            // 买一送一：需要商品ID和赠送方式
            if (createDTO.getProductIds() == null || createDTO.getProductIds().isEmpty()) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "买一送一必须选择活动商品");
            }
            if (createDTO.getGiftType() == null) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "买一送一必须选择赠送方式");
            }
            if ("different".equals(createDTO.getGiftType())) {
                if (createDTO.getGiftProductIds() == null || createDTO.getGiftProductIds().isEmpty()) {
                    throw new BusinessException(ResultCode.VALIDATE_FAILED, "买A送B必须选择赠送商品");
                }
            }
        } else if ("second_half_price".equals(type)) {
            // 第二件半价：需要商品ID
            if (createDTO.getProductIds() == null || createDTO.getProductIds().isEmpty()) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "第二件半价必须选择活动商品");
            }
        }
    }

    /**
     * 设置活动类型相关字段
     */
    private void setPromotionTypeFields(Promotion promotion, PromotionCreateDTO createDTO) {
        String type = createDTO.getType();
        
        promotion.setDiscountRate(null);
        promotion.setFullReduceRules(null);
        promotion.setAllowCoupon(null);
        promotion.setGiftType(null);
        promotion.setParticipationLimit(null);

        if ("discount".equals(type)) {
            promotion.setDiscountRate(createDTO.getDiscountRate());
        } else if ("full_reduce".equals(type)) {
            try {
                String fullReduceRulesJson = objectMapper.writeValueAsString(createDTO.getFullReduceRules());
                promotion.setFullReduceRules(fullReduceRulesJson);
            } catch (Exception e) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "满减规则格式错误");
            }
            promotion.setAllowCoupon(createDTO.getAllowCoupon() != null && createDTO.getAllowCoupon() ? 1 : 0);
        } else if ("buy_one_get_one".equals(type)) {
            promotion.setGiftType(createDTO.getGiftType());
        } else if ("second_half_price".equals(type)) {
            promotion.setParticipationLimit(createDTO.getParticipationLimit() != null ? createDTO.getParticipationLimit() : 0);
        }
    }

    /**
     * 保存促销活动商品关联
     */
    private void savePromotionProducts(Long promotionId, PromotionCreateDTO createDTO) {
        // 保存活动商品（所有类型的促销活动都可以绑定商品）
        // 满减活动虽然不直接影响单个商品价格，但可以绑定商品用于显示和筛选
        if (createDTO.getProductIds() != null && !createDTO.getProductIds().isEmpty()) {
            promotionProductMapper.batchInsert(promotionId, createDTO.getProductIds());
            log.info("促销活动 {} 绑定商品数量: {}", promotionId, createDTO.getProductIds().size());
        }

        // 保存赠送商品（买A送B）
        if ("buy_one_get_one".equals(createDTO.getType()) 
            && "different".equals(createDTO.getGiftType())
            && createDTO.getGiftProductIds() != null && !createDTO.getGiftProductIds().isEmpty()) {
            promotionProductMapper.batchInsertGiftProducts(promotionId, createDTO.getGiftProductIds());
        }
    }

    /**
     * 确定初始状态
     */
    private String determineInitialStatus(LocalDateTime startTime) {
        LocalDateTime now = LocalDateTime.now();
        if (startTime.isAfter(now)) {
            return "pending";
        } else {
            return "active";
        }
    }

    /**
     * 更新促销活动状态（基于时间）
     */
    private void updatePromotionStatus(Promotion promotion) {
        if ("paused".equals(promotion.getStatus())) {
            // 暂停状态的促销活动需要手动恢复
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promotion.getStartTime())) {
            promotion.setStatus("pending");
        } else if (now.isAfter(promotion.getEndTime())) {
            promotion.setStatus("ended");
        } else {
            promotion.setStatus("active");
        }
    }

    /**
     * 填充促销活动DTO的详细信息
     */
    private void enrichPromotionDTO(PromotionDTO promotionDTO) {
        Long promotionId = promotionDTO.getId();
        Long merchantId = promotionDTO.getMerchantId();
        String promotionType = promotionDTO.getType();

        // 查询商品ID列表（从promotion_product表）
        List<Long> productIds = promotionProductMapper.selectProductIdsByPromotionId(promotionId);
        
        // 如果productIds为空，根据商品的promotionTypes标签补充商品ID
        // 注意：满减活动也可以通过标签补充商品ID（虽然不直接影响价格，但可以用于显示）
        if ((productIds == null || productIds.isEmpty()) && merchantId != null) {
            List<String> promotionTags = getPromotionTagsByType(promotionType);
            if (promotionTags != null && !promotionTags.isEmpty()) {
                List<Long> tagBasedProductIds = productMapper.selectProductIdsByPromotionTags(merchantId, promotionTags);
                if (tagBasedProductIds != null && !tagBasedProductIds.isEmpty()) {
                    productIds = tagBasedProductIds;
                    log.info("促销活动 {} 通过商品标签补充商品ID，类型: {}, 标签: {}, 商品数量: {}", 
                        promotionId, promotionType, promotionTags, tagBasedProductIds.size());
                }
            }
        }
        
        promotionDTO.setProductIds(productIds != null ? productIds : new ArrayList<>());

        // 查询赠送商品ID列表
        if ("buy_one_get_one".equals(promotionDTO.getType()) && "different".equals(promotionDTO.getGiftType())) {
            List<Long> giftProductIds = promotionProductMapper.selectGiftProductIdsByPromotionId(promotionId);
            promotionDTO.setGiftProductIds(giftProductIds != null ? giftProductIds : new ArrayList<>());
        }

        // 解析满减规则
        Promotion promotion = promotionMapper.selectById(promotionId);
        if (promotion != null && promotion.getFullReduceRules() != null) {
            try {
                List<PromotionCreateDTO.FullReduceRuleDTO> rules = objectMapper.readValue(
                    promotion.getFullReduceRules(),
                    new TypeReference<List<PromotionCreateDTO.FullReduceRuleDTO>>() {}
                );
                promotionDTO.setFullReduceRules(
                    rules.stream().map(rule -> {
                        PromotionDTO.FullReduceRuleDTO dto = new PromotionDTO.FullReduceRuleDTO();
                        dto.setFullAmount(rule.getFullAmount());
                        dto.setReduceAmount(rule.getReduceAmount());
                        return dto;
                    }).collect(Collectors.toList())
                );
            } catch (Exception e) {
                log.warn("解析满减规则失败，活动ID: {}", promotionId, e);
            }
        }

        // 设置允许叠加优惠券
        if (promotion != null && promotion.getAllowCoupon() != null) {
            promotionDTO.setAllowCoupon(promotion.getAllowCoupon() == 1);
        }

        // 查询活动订单明细（这里简化处理，实际应该从订单表查询）
        List<PromotionOrderDetailDTO> orderDetails = new ArrayList<>();
        promotionDTO.setOrderDetails(orderDetails);
    }

    /**
     * 根据促销活动类型获取对应的商品标签列表
     * 
     * @param promotionType 促销活动类型
     * @return 商品标签列表
     */
    private List<String> getPromotionTagsByType(String promotionType) {
        if (promotionType == null) {
            return null;
        }
        
        switch (promotionType) {
            case "discount":
                // 限时折扣对应 discount 和 flash-sale 标签
                return List.of("discount", "flash-sale");
            case "buy_one_get_one":
                // 买一送一对应 combo 标签
                return List.of("combo");
            case "second_half_price":
                // 第二件半价暂时没有对应标签，返回null
                return null;
            case "full_reduce":
                // 满减活动不针对单个商品，返回null
                return null;
            default:
                return null;
        }
    }
}

