package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商家Mapper接口
 *
 * @author naicha
 * @since 2023-12-01
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {

    /**
     * 根据用户名查询商家
     *
     * @param username 用户名
     * @return 商家信息
     */
    Merchant selectByUsername(@Param("username") String username);

    /**
     * 根据店铺名称查询商家
     *
     * @param shopName 店铺名称
     * @return 商家信息
     */
    Merchant selectByShopName(@Param("shopName") String shopName);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 存在返回true，否则返回false
     */
    boolean existsByUsername(@Param("username") String username);

    /**
     * 检查店铺名称是否存在
     *
     * @param shopName 店铺名称
     * @return 存在返回true，否则返回false
     */
    boolean existsByShopName(@Param("shopName") String shopName);
}
