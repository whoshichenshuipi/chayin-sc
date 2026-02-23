package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.common.ResultCode;
import com.naicha.hou.dto.CouponCreateDTO;
import com.naicha.hou.dto.CouponDTO;
import com.naicha.hou.dto.CouponQueryDTO;
import com.naicha.hou.dto.CouponUsageDetailDTO;
import com.naicha.hou.dto.UserCouponDTO;
import com.naicha.hou.dto.UserCouponQueryDTO;
import com.naicha.hou.entity.Coupon;
import com.naicha.hou.entity.CouponUser;
import com.naicha.hou.exception.BusinessException;
import com.naicha.hou.mapper.CouponMapper;
import com.naicha.hou.mapper.CouponProductMapper;
import com.naicha.hou.mapper.CouponUserMapper;
import com.naicha.hou.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final CouponProductMapper couponProductMapper;
    private final CouponUserMapper couponUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCoupon(Long merchantId, CouponCreateDTO createDTO) {
        log.info("创建优惠券，商家ID: {}, 优惠券名称: {}", merchantId, createDTO.getName());

        // 验证时间范围
        validateTimeRange(createDTO.getReceiveTime(), createDTO.getValidTime());

        // 验证指定商品
        if ("specific".equals(createDTO.getProductScope())) {
            if (createDTO.getProductIds() == null || createDTO.getProductIds().isEmpty()) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "指定商品范围时必须选择商品");
            }
        }

        // 构建优惠券实体
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(createDTO, coupon, "receiveTime", "validTime", "productIds");
        coupon.setMerchantId(merchantId);
        coupon.setReceiveStartTime(createDTO.getReceiveTime().get(0));
        coupon.setReceiveEndTime(createDTO.getReceiveTime().get(1));
        coupon.setValidStartTime(createDTO.getValidTime().get(0));
        coupon.setValidEndTime(createDTO.getValidTime().get(1));
        coupon.setRemainingQuantity(createDTO.getTotalQuantity());
        coupon.setStatus(determineInitialStatus(coupon.getReceiveStartTime()));
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());

        // 保存优惠券
        couponMapper.insert(coupon);

        // 保存优惠券商品关联
        if ("specific".equals(createDTO.getProductScope()) && createDTO.getProductIds() != null) {
            couponProductMapper.batchInsert(coupon.getId(), createDTO.getProductIds());
        }

        log.info("优惠券创建成功，优惠券ID: {}", coupon.getId());
        return coupon.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCoupon(Long couponId, CouponCreateDTO createDTO) {
        log.info("更新优惠券，优惠券ID: {}", couponId);

        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "优惠券不存在");
        }

        // 验证时间范围
        validateTimeRange(createDTO.getReceiveTime(), createDTO.getValidTime());

        // 更新优惠券信息
        BeanUtils.copyProperties(createDTO, coupon, "id", "merchantId", "remainingQuantity", "createdAt", "receiveTime", "validTime", "productIds");
        coupon.setReceiveStartTime(createDTO.getReceiveTime().get(0));
        coupon.setReceiveEndTime(createDTO.getReceiveTime().get(1));
        coupon.setValidStartTime(createDTO.getValidTime().get(0));
        coupon.setValidEndTime(createDTO.getValidTime().get(1));
        coupon.setUpdatedAt(LocalDateTime.now());

        // 更新状态
        updateCouponStatus(coupon);

        couponMapper.updateById(coupon);

        // 更新优惠券商品关联（先删除旧的，再插入新的）
        couponProductMapper.deleteByCouponId(couponId);
        if ("specific".equals(createDTO.getProductScope()) && createDTO.getProductIds() != null) {
            couponProductMapper.batchInsert(couponId, createDTO.getProductIds());
        }

        log.info("优惠券更新成功，优惠券ID: {}", couponId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCoupon(Long couponId) {
        log.info("删除优惠券，优惠券ID: {}", couponId);

        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "优惠券不存在");
        }

        // 只有未开始或已结束的优惠券可以删除
        if (!"pending".equals(coupon.getStatus()) && !"ended".equals(coupon.getStatus())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "只能删除未开始或已结束的优惠券");
        }

        // 逻辑删除
        couponMapper.deleteById(couponId);

        log.info("优惠券删除成功，优惠券ID: {}", couponId);
        return true;
    }

    @Override
    public CouponDTO getCouponById(Long couponId) {
        CouponDTO couponDTO = couponMapper.selectCouponDetail(couponId);
        if (couponDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "优惠券不存在");
        }

        // 填充统计信息
        enrichCouponDTO(couponDTO);

        return couponDTO;
    }

    @Override
    public IPage<CouponDTO> getCouponList(CouponQueryDTO queryDTO) {
        Page<CouponDTO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<CouponDTO> result = couponMapper.selectCouponPage(page, queryDTO);

        // 为每个优惠券填充统计信息
        result.getRecords().forEach(this::enrichCouponDTO);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean togglePause(Long couponId) {
        log.info("暂停/恢复优惠券，优惠券ID: {}", couponId);

        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "优惠券不存在");
        }

        if ("paused".equals(coupon.getStatus())) {
            // 恢复：根据时间重新判断状态
            updateCouponStatus(coupon);
        } else if ("active".equals(coupon.getStatus())) {
            // 暂停
            coupon.setStatus("paused");
        } else {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "只能暂停或恢复进行中的优惠券");
        }

        coupon.setUpdatedAt(LocalDateTime.now());
        couponMapper.updateById(coupon);

        log.info("优惠券状态更新成功，优惠券ID: {}, 新状态: {}", couponId, coupon.getStatus());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCouponStatus(Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            return false;
        }

        String oldStatus = coupon.getStatus();
        updateCouponStatus(coupon);

        if (!oldStatus.equals(coupon.getStatus())) {
            coupon.setUpdatedAt(LocalDateTime.now());
            couponMapper.updateById(coupon);
        }

        return true;
    }

    /**
     * 验证时间范围
     */
    private void validateTimeRange(List<LocalDateTime> receiveTime, List<LocalDateTime> validTime) {
        if (receiveTime == null || receiveTime.size() != 2) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "领取时间范围格式错误");
        }
        if (validTime == null || validTime.size() != 2) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "使用有效期范围格式错误");
        }
        if (receiveTime.get(0).isAfter(receiveTime.get(1))) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "领取开始时间不能晚于结束时间");
        }
        if (validTime.get(0).isAfter(validTime.get(1))) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "使用有效期开始时间不能晚于结束时间");
        }
    }

    /**
     * 确定初始状态
     */
    private String determineInitialStatus(LocalDateTime receiveStartTime) {
        LocalDateTime now = LocalDateTime.now();
        if (receiveStartTime.isAfter(now)) {
            return "pending";
        } else {
            return "active";
        }
    }

    /**
     * 更新优惠券状态（基于时间）
     */
    private void updateCouponStatus(Coupon coupon) {
        if ("paused".equals(coupon.getStatus())) {
            // 暂停状态的优惠券需要手动恢复
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getReceiveStartTime())) {
            coupon.setStatus("pending");
        } else if (now.isAfter(coupon.getReceiveEndTime())) {
            coupon.setStatus("ended");
        } else {
            coupon.setStatus("active");
        }
    }

    /**
     * 填充优惠券DTO的统计信息
     */
    private void enrichCouponDTO(CouponDTO couponDTO) {
        // 查询领取人数和使用人数
        Integer receivedCount = couponMapper.selectReceivedCount(couponDTO.getId());
        Integer usedCount = couponMapper.selectUsedCount(couponDTO.getId());
        couponDTO.setReceivedCount(receivedCount != null ? receivedCount : 0);
        couponDTO.setUsedCount(usedCount != null ? usedCount : 0);

        // 查询商品ID列表
        if ("specific".equals(couponDTO.getProductScope())) {
            List<Long> productIds = couponProductMapper.selectProductIdsByCouponId(couponDTO.getId());
            couponDTO.setProductIds(productIds);
        }

        // 查询使用明细
        List<CouponUsageDetailDTO> usageDetails = couponUserMapper.selectUsageDetails(couponDTO.getId());
        couponDTO.setUsageDetails(usageDetails);
    }

    @Override
    public IPage<UserCouponDTO> getUserCoupons(UserCouponQueryDTO queryDTO) {
        log.info("查询用户优惠券列表，用户ID: {}, 状态: {}", queryDTO.getUserId(), queryDTO.getStatus());

        // 先更新过期状态的优惠券
        updateExpiredUserCoupons(queryDTO.getUserId());

        // 分页查询用户优惠券
        Page<UserCouponDTO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<UserCouponDTO> result = couponUserMapper.selectUserCouponPage(page, queryDTO);

        // 处理状态筛选（如果status不为空，进一步过滤）
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            List<UserCouponDTO> filteredRecords = result.getRecords().stream()
                    .filter(item -> {
                        // 如果已使用，状态必须是used
                        if ("used".equals(queryDTO.getStatus()) && !"used".equals(item.getStatus())) {
                            return false;
                        }
                        // 如果查询未使用，排除已使用和已过期的
                        if ("unused".equals(queryDTO.getStatus())) {
                            if ("used".equals(item.getStatus())) {
                                return false;
                            }
                            // 检查是否过期
                            if (item.getValidEndTime() != null && now.isAfter(item.getValidEndTime())) {
                                return false;
                            }
                            return true;
                        }
                        // 如果查询已过期
                        if ("expired".equals(queryDTO.getStatus())) {
                            if (item.getValidEndTime() != null && now.isAfter(item.getValidEndTime()) 
                                    && !"used".equals(item.getStatus())) {
                                return true;
                            }
                            return "expired".equals(item.getStatus());
                        }
                        return queryDTO.getStatus().equals(item.getStatus());
                    })
                    .collect(Collectors.toList());

            // 更新分页结果
            result.setRecords(filteredRecords);
            result.setTotal(filteredRecords.size());
        }

        return result;
    }

    /**
     * 更新过期的用户优惠券状态
     */
    private void updateExpiredUserCoupons(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        
        // 查询用户所有未使用的优惠券
        LambdaQueryWrapper<CouponUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponUser::getUserId, userId)
                .eq(CouponUser::getStatus, "unused");
        
        List<CouponUser> userCoupons = couponUserMapper.selectList(queryWrapper);
        
        // 遍历检查并更新过期状态
        for (CouponUser couponUser : userCoupons) {
            Coupon coupon = couponMapper.selectById(couponUser.getCouponId());
            if (coupon != null && coupon.getValidEndTime() != null 
                    && now.isAfter(coupon.getValidEndTime())) {
                // 已过期，更新状态
                couponUser.setStatus("expired");
                couponUser.setUpdatedAt(now);
                couponUserMapper.updateById(couponUser);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveCoupon(Long userId, Long couponId) {
        log.info("用户领取优惠券，用户ID: {}, 优惠券ID: {}", userId, couponId);

        // 1. 查询优惠券信息
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "优惠券不存在");
        }

        // 2. 检查优惠券状态
        updateCouponStatus(coupon);
        if (!"active".equals(coupon.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, 
                "优惠券状态不可领取，当前状态: " + coupon.getStatus());
        }

        // 3. 检查领取时间
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getReceiveStartTime() != null && now.isBefore(coupon.getReceiveStartTime())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "优惠券尚未开始领取");
        }
        if (coupon.getReceiveEndTime() != null && now.isAfter(coupon.getReceiveEndTime())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "优惠券领取已结束");
        }

        // 4. 检查剩余数量
        if (coupon.getRemainingQuantity() != null && coupon.getRemainingQuantity() <= 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "优惠券已被领完");
        }

        // 5. 检查用户是否已领取（防止重复领取）
        LambdaQueryWrapper<CouponUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponUser::getUserId, userId)
                .eq(CouponUser::getCouponId, couponId);
        long existingCount = couponUserMapper.selectCount(queryWrapper);
        if (existingCount > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "您已领取过此优惠券");
        }

        // 6. 创建用户优惠券记录
        CouponUser couponUser = new CouponUser();
        couponUser.setUserId(userId);
        couponUser.setCouponId(couponId);
        couponUser.setReceivedTime(now);
        couponUser.setStatus("unused");
        couponUser.setCreatedAt(now);
        couponUser.setUpdatedAt(now);
        couponUserMapper.insert(couponUser);

        // 7. 更新优惠券剩余数量
        if (coupon.getRemainingQuantity() != null) {
            coupon.setRemainingQuantity(coupon.getRemainingQuantity() - 1);
            coupon.setUpdatedAt(now);
            couponMapper.updateById(coupon);
        }

        log.info("用户领取优惠券成功，用户ID: {}, 优惠券ID: {}", userId, couponId);
        return true;
    }
}

