package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.common.ResultCode;
import com.naicha.hou.dto.BusinessHoursUpdateRequest;
import com.naicha.hou.dto.MerchantAuditHistoryDTO;
import com.naicha.hou.dto.MerchantAuditQueryDTO;
import com.naicha.hou.dto.MerchantAuditRequestDTO;
import com.naicha.hou.dto.MerchantDTO;
import com.naicha.hou.dto.MerchantQueryDTO;
import com.naicha.hou.dto.MerchantRatingDTO;
import com.naicha.hou.dto.MerchantRatingQueryDTO;
import com.naicha.hou.dto.MerchantRatingStatsDTO;
import com.naicha.hou.dto.ShopInfoUpdateRequest;
import com.naicha.hou.entity.Merchant;
import com.naicha.hou.entity.MerchantSetting;
import com.naicha.hou.entity.ShopReview;
import com.naicha.hou.exception.BusinessException;
import com.naicha.hou.mapper.MerchantMapper;
import com.naicha.hou.mapper.MerchantSettingMapper;
import com.naicha.hou.mapper.ShopReviewMapper;
import com.naicha.hou.service.MerchantService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naicha.hou.utils.AmapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商家服务实现类
 *
 * @author naicha
 * @since 2023-12-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;
    private final MerchantSettingMapper merchantSettingMapper;
    private final ShopReviewMapper shopReviewMapper;
    private final AmapUtil amapUtil;
    private final ObjectMapper objectMapper;

    @Override
    public Merchant getMerchantInfo(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "商家不存在");
        }
        return merchant;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Merchant updateShopInfo(Long merchantId, ShopInfoUpdateRequest request) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "商家不存在");
        }

        // 如果地址发生变化，重新获取经纬度
        if (request.getAddress() != null && !request.getAddress().equals(merchant.getAddress())) {
            BigDecimal[] location = amapUtil.geocode(request.getAddress());
            if (location != null) {
                merchant.setLongitude(location[0]);
                merchant.setLatitude(location[1]);
            }
        }

        // 更新店铺信息
        if (request.getShopName() != null) {
            merchant.setShopName(request.getShopName());
        }
        if (request.getShopLogo() != null) {
            merchant.setShopLogo(request.getShopLogo());
        }
        if (request.getContactPhone() != null) {
            merchant.setContactPhone(request.getContactPhone());
        }
        if (request.getAddress() != null) {
            merchant.setAddress(request.getAddress());
        }
        if (request.getDescription() != null) {
            merchant.setDescription(request.getDescription());
        }
        if (request.getLongitude() != null) {
            merchant.setLongitude(request.getLongitude());
        }
        if (request.getLatitude() != null) {
            merchant.setLatitude(request.getLatitude());
        }

        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);

        log.info("更新店铺信息成功: merchantId={}", merchantId);
        return merchant;
    }

    @Override
    public MerchantSetting getBusinessHours(Long merchantId) {
        MerchantSetting setting = merchantSettingMapper.selectByMerchantId(merchantId);
        if (setting == null) {
            // 如果没有设置，返回默认值
            setting = new MerchantSetting();
            setting.setMerchantId(merchantId);
            setting.setWeekdayOpenTime("09:00");
            setting.setWeekdayCloseTime("22:00");
            setting.setWeekendOpenTime("10:00");
            setting.setWeekendCloseTime("23:00");
            setting.setIs24Hours(0);
        }
        return setting;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantSetting updateBusinessHours(Long merchantId, BusinessHoursUpdateRequest request) {
        MerchantSetting setting = merchantSettingMapper.selectByMerchantId(merchantId);
        
        if (setting == null) {
            // 创建新的设置
            setting = new MerchantSetting();
            setting.setMerchantId(merchantId);
            setting.setCreatedAt(LocalDateTime.now());
        }

        setting.setWeekdayOpenTime(request.getWeekdayOpenTime());
        setting.setWeekdayCloseTime(request.getWeekdayCloseTime());
        setting.setWeekendOpenTime(request.getWeekendOpenTime());
        setting.setWeekendCloseTime(request.getWeekendCloseTime());
        setting.setIs24Hours(request.getIs24Hours());
        setting.setUpdatedAt(LocalDateTime.now());

        if (setting.getId() == null) {
            merchantSettingMapper.insert(setting);
        } else {
            merchantSettingMapper.updateById(setting);
        }

        log.info("更新营业时间设置成功: merchantId={}", merchantId);
        return setting;
    }

    @Override
    public Double[] getLocationByAddress(String address) {
        BigDecimal[] location = amapUtil.geocode(address);
        if (location != null) {
            return new Double[]{location[0].doubleValue(), location[1].doubleValue()};
        }
        return null;
    }

    @Override
    public IPage<MerchantDTO> getMerchantPage(MerchantQueryDTO queryDTO) {
        Page<Merchant> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        
        // 根据店铺名称过滤（模糊查询）
        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(Merchant::getShopName, queryDTO.getName());
        }
        
        // 根据状态过滤
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Merchant::getStatus, queryDTO.getStatus());
        }
        
        // 根据联系人姓名过滤（模糊查询）
        if (StringUtils.hasText(queryDTO.getContactName())) {
            wrapper.like(Merchant::getContactName, queryDTO.getContactName());
        }
        
        // 根据联系电话过滤（模糊查询）
        if (StringUtils.hasText(queryDTO.getContactPhone())) {
            wrapper.like(Merchant::getContactPhone, queryDTO.getContactPhone());
        }
        
        // 根据注册时间范围过滤
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(Merchant::getCreatedAt, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(Merchant::getCreatedAt, queryDTO.getEndTime());
        }
        
        // 排序：按创建时间倒序
        wrapper.orderByDesc(Merchant::getCreatedAt);
        
        IPage<Merchant> merchantPage = merchantMapper.selectPage(page, wrapper);
        
        // 转换为DTO
        Page<MerchantDTO> dtoPage = new Page<>(merchantPage.getCurrent(), merchantPage.getSize(), merchantPage.getTotal());
        List<MerchantDTO> dtoList = merchantPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    @Override
    public MerchantDTO getMerchantDetail(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "商家不存在");
        }
        return convertToDTO(merchant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMerchantStatus(Long merchantId, Integer status) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "商家不存在");
        }
        
        merchant.setStatus(status);
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);
        
        log.info("更新商家状态成功: merchantId={}, status={}", merchantId, status);
        return true;
    }

    /**
     * 将Merchant实体转换为MerchantDTO
     */
    private MerchantDTO convertToDTO(Merchant merchant) {
        MerchantDTO dto = new MerchantDTO();
        dto.setId(merchant.getId());
        dto.setUsername(merchant.getUsername());
        dto.setShopName(merchant.getShopName());
        dto.setShopLogo(merchant.getShopLogo());
        dto.setContactName(merchant.getContactName());
        dto.setContactPhone(merchant.getContactPhone());
        dto.setContactEmail(merchant.getContactEmail());
        dto.setBusinessLicense(merchant.getBusinessLicense());
        dto.setAddress(merchant.getAddress());
        dto.setLongitude(merchant.getLongitude());
        dto.setLatitude(merchant.getLatitude());
        dto.setDescription(merchant.getDescription());
        dto.setStatus(merchant.getStatus());
        dto.setBusinessStatus(merchant.getBusinessStatus());
        dto.setAnnouncement(merchant.getAnnouncement());
        dto.setCreateTime(merchant.getCreatedAt());
        // 评分和最后登录时间需要从其他表查询，这里暂时设为null
        dto.setRating(null);
        dto.setLastLogin(null);
        return dto;
    }

    @Override
    public IPage<MerchantDTO> getPendingAuditMerchants(MerchantAuditQueryDTO queryDTO) {
        Page<Merchant> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询待审核状态的商家
        wrapper.eq(Merchant::getStatus, 0);
        
        // 根据店铺名称过滤（模糊查询）
        if (StringUtils.hasText(queryDTO.getShopName())) {
            wrapper.like(Merchant::getShopName, queryDTO.getShopName());
        }
        
        // 根据注册时间范围过滤
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(Merchant::getCreatedAt, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(Merchant::getCreatedAt, queryDTO.getEndTime());
        }
        
        // 排序：按创建时间倒序
        wrapper.orderByDesc(Merchant::getCreatedAt);
        
        IPage<Merchant> merchantPage = merchantMapper.selectPage(page, wrapper);
        
        // 转换为DTO
        Page<MerchantDTO> dtoPage = new Page<>(merchantPage.getCurrent(), merchantPage.getSize(), merchantPage.getTotal());
        List<MerchantDTO> dtoList = merchantPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    @Override
    public IPage<MerchantAuditHistoryDTO> getAuditHistory(MerchantAuditQueryDTO queryDTO) {
        Page<Merchant> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        
        // 查询已审核或已拒绝的商家
        wrapper.in(Merchant::getStatus, 1, 2);
        
        // 根据店铺名称过滤（模糊查询）
        if (StringUtils.hasText(queryDTO.getShopName())) {
            wrapper.like(Merchant::getShopName, queryDTO.getShopName());
        }
        
        // 根据审核状态过滤
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Merchant::getStatus, queryDTO.getStatus());
        }
        
        // 根据更新时间范围过滤（作为审核时间）
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(Merchant::getUpdatedAt, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(Merchant::getUpdatedAt, queryDTO.getEndTime());
        }
        
        // 排序：按更新时间倒序
        wrapper.orderByDesc(Merchant::getUpdatedAt);
        
        IPage<Merchant> merchantPage = merchantMapper.selectPage(page, wrapper);
        
        // 转换为审核历史DTO
        Page<MerchantAuditHistoryDTO> dtoPage = new Page<>(merchantPage.getCurrent(), merchantPage.getSize(), merchantPage.getTotal());
        List<MerchantAuditHistoryDTO> dtoList = merchantPage.getRecords().stream()
                .map(merchant -> {
                    MerchantAuditHistoryDTO dto = new MerchantAuditHistoryDTO();
                    dto.setId(merchant.getId());
                    dto.setShopName(merchant.getShopName());
                    dto.setContactName(merchant.getContactName());
                    dto.setContactPhone(merchant.getContactPhone());
                    dto.setApplyTime(merchant.getCreatedAt());
                    dto.setAuditTime(merchant.getUpdatedAt());
                    // 状态转换为审核结果
                    if (merchant.getStatus() == 1) {
                        dto.setResult("approved");
                    } else if (merchant.getStatus() == 2) {
                        dto.setResult("rejected");
                    }
                    // 审核人和审核意见等信息可以从其他表获取，这里暂时设为null
                    dto.setAuditorId(null);
                    dto.setAuditorName(null);
                    dto.setComment(null);
                    dto.setRejectReason(null);
                    return dto;
                })
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditMerchant(MerchantAuditRequestDTO request, Long auditorId) {
        Merchant merchant = merchantMapper.selectById(request.getMerchantId());
        if (merchant == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "商家不存在");
        }
        
        if (merchant.getStatus() != 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "该商家不是待审核状态，无法审核");
        }
        
        // 根据审核结果设置状态
        if ("approved".equals(request.getResult())) {
            merchant.setStatus(1); // 已审核
        } else if ("rejected".equals(request.getResult())) {
            merchant.setStatus(2); // 已拒绝
        } else {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "审核结果无效");
        }
        
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);
        
        // TODO: 如果审核通过且autoGenerateAccount为true，可以自动生成账号并发送通知
        // TODO: 可以创建审核日志记录审核人、审核时间、审核意见等信息
        
        log.info("审核商家成功: merchantId={}, result={}, auditorId={}", 
                request.getMerchantId(), request.getResult(), auditorId);
        return true;
    }

    @Override
    public IPage<MerchantRatingDTO> getMerchantRatingPage(MerchantRatingQueryDTO queryDTO) {
        Page<ShopReview> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        LambdaQueryWrapper<ShopReview> wrapper = new LambdaQueryWrapper<>();
        
        // 根据商家ID过滤
        if (queryDTO.getMerchantId() != null) {
            wrapper.eq(ShopReview::getShopId, queryDTO.getMerchantId());
        }
        
        // 根据评分筛选
        if (queryDTO.getRating() != null && queryDTO.getRating() > 0) {
            wrapper.eq(ShopReview::getOverallRating, queryDTO.getRating());
        }
        
        // 根据评价时间范围过滤
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(ShopReview::getCreatedAt, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(ShopReview::getCreatedAt, queryDTO.getEndTime());
        }
        
        // 排序：按创建时间倒序
        wrapper.orderByDesc(ShopReview::getCreatedAt);
        
        IPage<ShopReview> reviewPage = shopReviewMapper.selectPage(page, wrapper);
        
        // 转换为DTO
        Page<MerchantRatingDTO> dtoPage = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        List<MerchantRatingDTO> dtoList = reviewPage.getRecords().stream()
                .map(review -> {
                    MerchantRatingDTO dto = convertReviewToDTO(review);
                    // 如果指定了商家名称查询，需要过滤
                    if (StringUtils.hasText(queryDTO.getMerchantName())) {
                        Merchant merchant = merchantMapper.selectById(review.getShopId());
                        if (merchant != null && merchant.getShopName().contains(queryDTO.getMerchantName())) {
                            dto.setMerchantName(merchant.getShopName());
                            return dto;
                        }
                        return null; // 不匹配则过滤掉
                    } else {
                        // 设置商家名称
                        Merchant merchant = merchantMapper.selectById(review.getShopId());
                        if (merchant != null) {
                            dto.setMerchantName(merchant.getShopName());
                        }
                        return dto;
                    }
                })
                .filter(dto -> dto != null) // 过滤掉null值
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    @Override
    public List<MerchantRatingStatsDTO> getMerchantRatingStats(List<Long> merchantIds) {
        // 如果指定了商家ID列表，则查询这些商家；否则查询所有已审核的商家
        LambdaQueryWrapper<Merchant> merchantWrapper = new LambdaQueryWrapper<>();
        merchantWrapper.eq(Merchant::getStatus, 1); // 只查询已审核的商家
        if (merchantIds != null && !merchantIds.isEmpty()) {
            merchantWrapper.in(Merchant::getId, merchantIds);
        }
        
        List<Merchant> merchants = merchantMapper.selectList(merchantWrapper);
        
        return merchants.stream()
                .map(this::buildMerchantRatingStats)
                .collect(Collectors.toList());
    }

    @Override
    public MerchantRatingStatsDTO getMerchantRatingStatsById(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "商家不存在");
        }
        return buildMerchantRatingStats(merchant);
    }

    /**
     * 构建商家评分统计
     */
    private MerchantRatingStatsDTO buildMerchantRatingStats(Merchant merchant) {
        List<ShopReview> reviews = shopReviewMapper.selectByShopId(merchant.getId());
        
        MerchantRatingStatsDTO stats = new MerchantRatingStatsDTO();
        stats.setMerchantId(merchant.getId());
        stats.setMerchantName(merchant.getShopName());
        
        if (reviews.isEmpty()) {
            stats.setOverallRating(BigDecimal.ZERO);
            stats.setQualityRating(BigDecimal.ZERO);
            stats.setDeliveryRating(BigDecimal.ZERO);
            stats.setServiceRating(BigDecimal.ZERO);
            stats.setReviewCount(0L);
            stats.setRatingDistribution(java.util.Map.of(1, 0L, 2, 0L, 3, 0L, 4, 0L, 5, 0L));
            stats.setPositiveRate(BigDecimal.ZERO);
            return stats;
        }
        
        // 计算平均分
        double overallAvg = reviews.stream()
                .mapToDouble(r -> r.getOverallRating().doubleValue())
                .average()
                .orElse(0.0);
        double qualityAvg = reviews.stream()
                .mapToDouble(r -> r.getQualityRating().doubleValue())
                .average()
                .orElse(0.0);
        double deliveryAvg = reviews.stream()
                .mapToDouble(r -> r.getDeliveryRating().doubleValue())
                .average()
                .orElse(0.0);
        double serviceAvg = reviews.stream()
                .mapToDouble(r -> r.getServiceRating().doubleValue())
                .average()
                .orElse(0.0);
        
        stats.setOverallRating(BigDecimal.valueOf(overallAvg).setScale(1, java.math.RoundingMode.HALF_UP));
        stats.setQualityRating(BigDecimal.valueOf(qualityAvg).setScale(1, java.math.RoundingMode.HALF_UP));
        stats.setDeliveryRating(BigDecimal.valueOf(deliveryAvg).setScale(1, java.math.RoundingMode.HALF_UP));
        stats.setServiceRating(BigDecimal.valueOf(serviceAvg).setScale(1, java.math.RoundingMode.HALF_UP));
        stats.setReviewCount((long) reviews.size());
        
        // 统计各评分数量
        java.util.Map<Integer, Long> distribution = new java.util.HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            long count = reviews.stream()
                    .filter(r -> r.getOverallRating().intValue() == rating)
                    .count();
            distribution.put(rating, count);
        }
        stats.setRatingDistribution(distribution);
        
        // 计算好评率（4星及以上占比）
        long positiveCount = reviews.stream()
                .filter(r -> r.getOverallRating().intValue() >= 4)
                .count();
        double positiveRate = reviews.size() > 0 ? (double) positiveCount / reviews.size() * 100 : 0.0;
        stats.setPositiveRate(BigDecimal.valueOf(positiveRate).setScale(2, java.math.RoundingMode.HALF_UP));
        
        return stats;
    }

    /**
     * 将ShopReview实体转换为MerchantRatingDTO
     */
    private MerchantRatingDTO convertReviewToDTO(ShopReview review) {
        MerchantRatingDTO dto = new MerchantRatingDTO();
        dto.setId(review.getId());
        dto.setMerchantId(review.getShopId());
        dto.setUserId(review.getUserId());
        dto.setUserName(review.getUserName());
        dto.setUserAvatar(review.getUserAvatar());
        dto.setOverallRating(review.getOverallRating());
        dto.setQualityRating(review.getQualityRating());
        dto.setDeliveryRating(review.getDeliveryRating());
        dto.setServiceRating(review.getServiceRating());
        dto.setComment(review.getComment());
        
        // 解析图片JSON
        if (StringUtils.hasText(review.getImages())) {
            try {
                List<String> images = objectMapper.readValue(review.getImages(), new TypeReference<List<String>>() {});
                dto.setImages(images);
            } catch (Exception e) {
                log.warn("解析评价图片JSON失败: {}", review.getImages(), e);
                dto.setImages(java.util.Collections.emptyList());
            }
        } else {
            dto.setImages(java.util.Collections.emptyList());
        }
        
        dto.setMerchantReply(review.getMerchantReply());
        dto.setReplyTime(review.getReplyTime());
        dto.setHelpfulCount(review.getHelpfulCount());
        dto.setCreateTime(review.getCreatedAt());
        return dto;
    }
}

