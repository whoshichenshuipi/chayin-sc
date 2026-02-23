package com.naicha.hou.service.impl;

import com.naicha.hou.common.ResultCode;
import com.naicha.hou.dto.AddressCreateDTO;
import com.naicha.hou.dto.AddressDTO;
import com.naicha.hou.entity.UserAddress;
import com.naicha.hou.exception.BusinessException;
import com.naicha.hou.mapper.AddressMapper;
import com.naicha.hou.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 地址管理服务实现类
 *
 * @author naicha
 * @since 2024-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    @Override
    public List<AddressDTO> getAddressList(Long userId) {
        List<UserAddress> addresses = addressMapper.selectByUserId(userId);
        return addresses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO getAddressDetail(Long addressId, Long userId) {
        UserAddress address = addressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "地址不存在");
        }
        // 验证地址是否属于当前用户
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权访问该地址");
        }
        return convertToDTO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAddress(Long userId, AddressCreateDTO createDTO) {
        // 如果设置为默认地址，先取消其他默认地址
        if (Boolean.TRUE.equals(createDTO.getIsDefault())) {
            addressMapper.cancelDefaultByUserId(userId);
        }

        UserAddress address = new UserAddress();
        BeanUtils.copyProperties(createDTO, address);
        address.setUserId(userId);
        address.setIsDefault(Boolean.TRUE.equals(createDTO.getIsDefault()) ? 1 : 0);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());

        addressMapper.insert(address);
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(Long addressId, Long userId, AddressCreateDTO createDTO) {
        UserAddress address = addressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "地址不存在");
        }
        // 验证地址是否属于当前用户
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权修改该地址");
        }

        // 如果设置为默认地址，先取消其他默认地址
        if (Boolean.TRUE.equals(createDTO.getIsDefault()) && address.getIsDefault() != 1) {
            addressMapper.cancelDefaultByUserId(userId);
        }

        BeanUtils.copyProperties(createDTO, address);
        address.setIsDefault(Boolean.TRUE.equals(createDTO.getIsDefault()) ? 1 : 0);
        address.setUpdatedAt(LocalDateTime.now());

        int result = addressMapper.updateById(address);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long addressId, Long userId) {
        UserAddress address = addressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "地址不存在");
        }
        // 验证地址是否属于当前用户
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权删除该地址");
        }

        int result = addressMapper.deleteById(addressId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(Long addressId, Long userId) {
        UserAddress address = addressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "地址不存在");
        }
        // 验证地址是否属于当前用户
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作该地址");
        }

        // 先取消所有默认地址
        addressMapper.cancelDefaultByUserId(userId);

        // 设置当前地址为默认
        address.setIsDefault(1);
        address.setUpdatedAt(LocalDateTime.now());
        int result = addressMapper.updateById(address);
        return result > 0;
    }

    /**
     * 转换实体为DTO
     */
    private AddressDTO convertToDTO(UserAddress address) {
        AddressDTO dto = new AddressDTO();
        BeanUtils.copyProperties(address, dto);
        dto.setIsDefault(address.getIsDefault() == 1);
        return dto;
    }
}

