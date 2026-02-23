package com.naicha.hou.service;

import com.naicha.hou.dto.AddressCreateDTO;
import com.naicha.hou.dto.AddressDTO;

import java.util.List;

/**
 * 地址管理服务接口
 *
 * @author naicha
 * @since 2024-01-15
 */
public interface AddressService {

    /**
     * 获取用户地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<AddressDTO> getAddressList(Long userId);

    /**
     * 根据ID获取地址详情
     *
     * @param addressId 地址ID
     * @param userId 用户ID（用于权限验证）
     * @return 地址详情
     */
    AddressDTO getAddressDetail(Long addressId, Long userId);

    /**
     * 创建地址
     *
     * @param userId 用户ID
     * @param createDTO 地址创建DTO
     * @return 创建的地址ID
     */
    Long createAddress(Long userId, AddressCreateDTO createDTO);

    /**
     * 更新地址
     *
     * @param addressId 地址ID
     * @param userId 用户ID（用于权限验证）
     * @param createDTO 地址更新DTO
     * @return 是否成功
     */
    boolean updateAddress(Long addressId, Long userId, AddressCreateDTO createDTO);

    /**
     * 删除地址
     *
     * @param addressId 地址ID
     * @param userId 用户ID（用于权限验证）
     * @return 是否成功
     */
    boolean deleteAddress(Long addressId, Long userId);

    /**
     * 设置默认地址
     *
     * @param addressId 地址ID
     * @param userId 用户ID（用于权限验证）
     * @return 是否成功
     */
    boolean setDefaultAddress(Long addressId, Long userId);
}

