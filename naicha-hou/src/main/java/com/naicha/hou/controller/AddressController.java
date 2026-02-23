package com.naicha.hou.controller;

import com.naicha.hou.common.Result;
import com.naicha.hou.dto.AddressCreateDTO;
import com.naicha.hou.dto.AddressDTO;
import com.naicha.hou.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址管理控制器
 *
 * @author naicha
 * @since 2024-01-15
 */
@Slf4j
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
@Validated
@Tag(name = "地址管理", description = "用户收货地址管理接口")
public class AddressController {

    private final AddressService addressService;

    /**
     * 获取用户地址列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户地址列表", description = "获取当前用户的所有收货地址")
    public Result<List<AddressDTO>> getAddressList(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {
        
        log.info("获取用户地址列表，用户ID: {}", userId);
        
        List<AddressDTO> addresses = addressService.getAddressList(userId);
        return Result.success("查询成功", addresses);
    }

    /**
     * 获取地址详情
     */
    @GetMapping("/detail/{addressId}")
    @Operation(summary = "获取地址详情", description = "根据地址ID获取地址详细信息")
    public Result<AddressDTO> getAddressDetail(
            @Parameter(description = "地址ID", required = true)
            @PathVariable @NotNull Long addressId,
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {
        
        log.info("获取地址详情，地址ID: {}, 用户ID: {}", addressId, userId);
        
        AddressDTO address = addressService.getAddressDetail(addressId, userId);
        return Result.success("查询成功", address);
    }

    /**
     * 创建地址
     */
    @PostMapping("/create")
    @Operation(summary = "创建地址", description = "创建新的收货地址")
    public Result<Long> createAddress(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Valid @RequestBody AddressCreateDTO createDTO) {
        
        log.info("创建地址，用户ID: {}, 地址信息: {}", userId, createDTO);
        
        Long addressId = addressService.createAddress(userId, createDTO);
        return Result.success("地址创建成功", addressId);
    }

    /**
     * 更新地址
     */
    @PutMapping("/update/{addressId}")
    @Operation(summary = "更新地址", description = "更新已有的收货地址")
    public Result<Boolean> updateAddress(
            @Parameter(description = "地址ID", required = true)
            @PathVariable @NotNull Long addressId,
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Valid @RequestBody AddressCreateDTO createDTO) {
        
        log.info("更新地址，地址ID: {}, 用户ID: {}, 地址信息: {}", addressId, userId, createDTO);
        
        boolean success = addressService.updateAddress(addressId, userId, createDTO);
        if (success) {
            return Result.success("地址更新成功", true);
        } else {
            return Result.error("地址更新失败");
        }
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/delete/{addressId}")
    @Operation(summary = "删除地址", description = "删除指定的收货地址")
    public Result<Boolean> deleteAddress(
            @Parameter(description = "地址ID", required = true)
            @PathVariable @NotNull Long addressId,
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {
        
        log.info("删除地址，地址ID: {}, 用户ID: {}", addressId, userId);
        
        boolean success = addressService.deleteAddress(addressId, userId);
        if (success) {
            return Result.success("地址删除成功", true);
        } else {
            return Result.error("地址删除失败");
        }
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/set-default/{addressId}")
    @Operation(summary = "设置默认地址", description = "将指定地址设置为默认收货地址")
    public Result<Boolean> setDefaultAddress(
            @Parameter(description = "地址ID", required = true)
            @PathVariable @NotNull Long addressId,
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId) {
        
        log.info("设置默认地址，地址ID: {}, 用户ID: {}", addressId, userId);
        
        boolean success = addressService.setDefaultAddress(addressId, userId);
        if (success) {
            return Result.success("默认地址设置成功", true);
        } else {
            return Result.error("默认地址设置失败");
        }
    }
}

