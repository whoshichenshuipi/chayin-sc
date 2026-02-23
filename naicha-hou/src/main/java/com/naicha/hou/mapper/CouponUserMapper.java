package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.dto.CouponUsageDetailDTO;
import com.naicha.hou.dto.UserCouponDTO;
import com.naicha.hou.dto.UserCouponQueryDTO;
import com.naicha.hou.entity.CouponUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户优惠券关联Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface CouponUserMapper extends BaseMapper<CouponUser> {

    /**
     * 查询优惠券使用明细
     *
     * @param couponId 优惠券ID
     * @return 使用明细列表
     */
    List<CouponUsageDetailDTO> selectUsageDetails(@Param("couponId") Long couponId);

    /**
     * 分页查询用户优惠券列表
     *
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<UserCouponDTO> selectUserCouponPage(Page<UserCouponDTO> page, @Param("query") UserCouponQueryDTO query);
}

