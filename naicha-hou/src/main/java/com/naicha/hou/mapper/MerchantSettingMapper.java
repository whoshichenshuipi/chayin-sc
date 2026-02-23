package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.MerchantSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商家设置Mapper接口
 *
 * @author naicha
 * @since 2023-12-01
 */
@Mapper
public interface MerchantSettingMapper extends BaseMapper<MerchantSetting> {

    /**
     * 根据商家ID查询设置
     *
     * @param merchantId 商家ID
     * @return 商家设置
     */
    MerchantSetting selectByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 根据商家ID更新设置
     *
     * @param setting 商家设置
     * @return 更新记录数
     */
    int updateByMerchantId(@Param("setting") MerchantSetting setting);
}

