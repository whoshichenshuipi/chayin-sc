package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户地址Mapper接口
 *
 * @author naicha
 * @since 2024-01-15
 */
@Mapper
public interface AddressMapper extends BaseMapper<UserAddress> {

    /**
     * 根据用户ID查询地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<UserAddress> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    UserAddress selectDefaultByUserId(@Param("userId") Long userId);

    /**
     * 取消用户的所有默认地址
     *
     * @param userId 用户ID
     * @return 更新的记录数
     */
    int cancelDefaultByUserId(@Param("userId") Long userId);
}

