package com.naicha.hou.service.impl;

import com.naicha.hou.common.ResultCode;
import com.naicha.hou.dto.ShopInfoDTO;
import com.naicha.hou.entity.Merchant;
import com.naicha.hou.entity.MerchantSetting;
import com.naicha.hou.entity.ShopReview;
import com.naicha.hou.exception.BusinessException;
import com.naicha.hou.mapper.MerchantMapper;
import com.naicha.hou.mapper.MerchantSettingMapper;
import com.naicha.hou.mapper.ShopReviewMapper;
import com.naicha.hou.service.ShopInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 店铺信息服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopInfoServiceImpl implements ShopInfoService {

    private final MerchantMapper merchantMapper;
    private final MerchantSettingMapper merchantSettingMapper;
    private final ShopReviewMapper shopReviewMapper;

    @Override
    public ShopInfoDTO getShopInfo(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(400, "商家不存在");
        }

        MerchantSetting setting = merchantSettingMapper.selectByMerchantId(merchantId);

        ShopInfoDTO dto = new ShopInfoDTO();
        dto.setMerchantId(merchantId);
        dto.setShopName(merchant.getShopName());
        dto.setShopLogo(merchant.getShopLogo());
        dto.setDescription(merchant.getDescription());
        dto.setContactPhone(merchant.getContactPhone());
        dto.setAddress(merchant.getAddress());
        dto.setLongitude(merchant.getLongitude());
        dto.setLatitude(merchant.getLatitude());
        dto.setBusinessStatus(merchant.getBusinessStatus());
        dto.setAnnouncement(merchant.getAnnouncement());

        // 营业时间
        if (setting != null) {
            dto.setWeekdayHours(Arrays.asList(setting.getWeekdayOpenTime(), setting.getWeekdayCloseTime()));
            dto.setWeekendHours(Arrays.asList(setting.getWeekendOpenTime(), setting.getWeekendCloseTime()));
        } else {
            dto.setWeekdayHours(Arrays.asList("09:00", "22:00"));
            dto.setWeekendHours(Arrays.asList("10:00", "23:00"));
        }

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShopBasicInfo(Long merchantId, String shopName, String shopLogo, String description,
                                     String contactPhone, String address, BigDecimal longitude, BigDecimal latitude,
                                     String weekdayOpenTime, String weekdayCloseTime,
                                     String weekendOpenTime, String weekendCloseTime) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "商家不存在");
        }

        // 更新店铺信息
        if (shopName != null) {
            merchant.setShopName(shopName);
        }
        if (shopLogo != null) {
            merchant.setShopLogo(shopLogo);
        }
        if (description != null) {
            merchant.setDescription(description);
        }
        if (contactPhone != null) {
            merchant.setContactPhone(contactPhone);
        }
        if (address != null) {
            merchant.setAddress(address);
        }
        if (longitude != null) {
            merchant.setLongitude(longitude);
        }
        if (latitude != null) {
            merchant.setLatitude(latitude);
        }
        
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);

        // 更新营业时间设置
        MerchantSetting setting = merchantSettingMapper.selectByMerchantId(merchantId);
        if (setting == null) {
            setting = new MerchantSetting();
            setting.setMerchantId(merchantId);
            setting.setCreatedAt(LocalDateTime.now());
        }

        if (weekdayOpenTime != null) {
            setting.setWeekdayOpenTime(weekdayOpenTime);
        }
        if (weekdayCloseTime != null) {
            setting.setWeekdayCloseTime(weekdayCloseTime);
        }
        if (weekendOpenTime != null) {
            setting.setWeekendOpenTime(weekendOpenTime);
        }
        if (weekendCloseTime != null) {
            setting.setWeekendCloseTime(weekendCloseTime);
        }

        setting.setUpdatedAt(LocalDateTime.now());

        if (setting.getId() == null) {
            merchantSettingMapper.insert(setting);
        } else {
            merchantSettingMapper.updateById(setting);
        }

        log.info("更新店铺基础信息成功: merchantId={}", merchantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShopStatus(Long merchantId, Integer businessStatus, String announcement) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "商家不存在");
        }

        if (businessStatus != null) {
            merchant.setBusinessStatus(businessStatus);
        }
        if (announcement != null) {
            merchant.setAnnouncement(announcement);
        }

        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);

        log.info("更新店铺状态成功: merchantId={}, businessStatus={}", merchantId, businessStatus);
    }

    @Override
    public Map<String, Object> getRatingStats(Long merchantId) {
        List<ShopReview> reviews = shopReviewMapper.selectByShopId(merchantId);
        
        if (reviews.isEmpty()) {
            return getEmptyRatingStats();
        }

        double overallRating = reviews.stream()
                .mapToDouble(r -> r.getOverallRating().doubleValue())
                .average()
                .orElse(0.0);

        double qualityRating = reviews.stream()
                .mapToDouble(r -> r.getQualityRating().doubleValue())
                .average()
                .orElse(0.0);

        double deliveryRating = reviews.stream()
                .mapToDouble(r -> r.getDeliveryRating().doubleValue())
                .average()
                .orElse(0.0);

        double serviceRating = reviews.stream()
                .mapToDouble(r -> r.getServiceRating().doubleValue())
                .average()
                .orElse(0.0);

        Map<String, Object> stats = new HashMap<>();
        stats.put("overall", BigDecimal.valueOf(overallRating).setScale(1, java.math.RoundingMode.HALF_UP));
        stats.put("quality", BigDecimal.valueOf(qualityRating).setScale(1, java.math.RoundingMode.HALF_UP));
        stats.put("delivery", BigDecimal.valueOf(deliveryRating).setScale(1, java.math.RoundingMode.HALF_UP));
        stats.put("service", BigDecimal.valueOf(serviceRating).setScale(1, java.math.RoundingMode.HALF_UP));
        stats.put("reviewCount", reviews.size());

        return stats;
    }

    @Override
    public List<ShopReview> getReviewList(Long merchantId, Integer rating) {
        List<ShopReview> reviews = shopReviewMapper.selectByShopId(merchantId);
        
        if (rating != null && rating > 0) {
            reviews = reviews.stream()
                    .filter(r -> r.getOverallRating().intValue() == rating)
                    .toList();
        }
        
        return reviews;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyReview(Long merchantId, Long reviewId, String reply) {
        ShopReview review = shopReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(400, "评价不存在");
        }

        if (!review.getShopId().equals(merchantId)) {
            throw new BusinessException(403, "无权限操作此评价");
        }

        review.setMerchantReply(reply);
        review.setReplyTime(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        shopReviewMapper.updateById(review);

        log.info("回复评价成功: reviewId={}, merchantId={}", reviewId, merchantId);
    }

    private Map<String, Object> getEmptyRatingStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("overall", BigDecimal.ZERO);
        stats.put("quality", BigDecimal.ZERO);
        stats.put("delivery", BigDecimal.ZERO);
        stats.put("service", BigDecimal.ZERO);
        stats.put("reviewCount", 0);
        return stats;
    }
}
