package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.common.ResultCode;
import com.naicha.hou.dto.UserDTO;
import com.naicha.hou.dto.UserProfileUpdateDTO;
import com.naicha.hou.dto.UserQueryDTO;
import com.naicha.hou.dto.UserViolationDTO;
import com.naicha.hou.dto.UserViolationRequestDTO;
import com.naicha.hou.entity.User;
import com.naicha.hou.entity.UserViolation;
import com.naicha.hou.exception.BusinessException;
import com.naicha.hou.mapper.OrderMapper;
import com.naicha.hou.mapper.UserMapper;
import com.naicha.hou.mapper.UserViolationMapper;
import com.naicha.hou.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserViolationMapper userViolationMapper;
    private final OrderMapper orderMapper;

    @Override
    public IPage<UserDTO> getUserPage(UserQueryDTO queryDTO) {
        Page<User> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 根据搜索类型和关键词过滤
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword().trim();
            switch (queryDTO.getType()) {
                case "username":
                    wrapper.like(User::getUsername, keyword);
                    break;
                case "phone":
                    wrapper.like(User::getPhone, keyword);
                    break;
                case "id":
                    try {
                        Long userId = Long.parseLong(keyword);
                        wrapper.eq(User::getId, userId);
                    } catch (NumberFormatException e) {
                        // 如果无法转换为Long，则不匹配任何结果
                        wrapper.eq(User::getId, -1L);
                    }
                    break;
            }
        }
        
        // 根据状态过滤
        // 前端传入：normal对应1, limited对应2, frozen对应3
        if (queryDTO.getStatus() != null) {
            wrapper.eq(User::getStatus, queryDTO.getStatus());
        }
        
        // 排序：按创建时间倒序
        wrapper.orderByDesc(User::getCreatedAt);
        
        IPage<User> userPage = userMapper.selectPage(page, wrapper);
        
        // 转换为DTO并填充统计信息
        Page<UserDTO> dtoPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserDTO> dtoList = userPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    @Override
    public UserDTO getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        
        UserDTO dto = convertToDTO(user);
        
        // 查询违规记录
        List<UserViolation> violations = userViolationMapper.selectByUserId(userId);
        List<UserViolationDTO> violationDTOs = violations.stream()
                .map(this::convertViolationToDTO)
                .collect(Collectors.toList());
        dto.setViolations(violationDTOs);
        
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleUserViolation(UserViolationRequestDTO requestDTO, String handler) {
        User user = userMapper.selectById(requestDTO.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        
        // 创建违规记录
        UserViolation violation = new UserViolation();
        violation.setUserId(requestDTO.getUserId());
        violation.setType(requestDTO.getType());
        violation.setReason(requestDTO.getReason());
        if (requestDTO.getLimitDays() != null) {
            violation.setLimitDays(requestDTO.getLimitDays());
        }
        violation.setHandler(handler);
        violation.setCreatedAt(LocalDateTime.now());
        
        userViolationMapper.insert(violation);
        
        // 更新用户状态
        Integer newStatus;
        switch (requestDTO.getType()) {
            case "warning":
                // 警告不改变状态，如果当前状态是正常，保持正常
                newStatus = (user.getStatus() != null && user.getStatus() == 1) ? 1 : user.getStatus();
                break;
            case "limit":
                newStatus = 2; // 限制下单
                break;
            case "freeze":
                newStatus = 3; // 冻结
                break;
            default:
                throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "无效的处理类型");
        }
        
        user.setStatus(newStatus);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("处理用户违规成功: userId={}, type={}, handler={}", requestDTO.getUserId(), requestDTO.getType(), handler);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserProfile(Long userId, UserProfileUpdateDTO updateDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }

        // 更新允许修改的字段
        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getEmail() != null) {
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getAvatar() != null) {
            // 验证头像 URL 长度（虽然数据库已改为 TEXT，但添加验证以防万一）
            String avatar = updateDTO.getAvatar();
            if (avatar.length() > 65535) { // TEXT 类型最大长度
                log.warn("头像 URL 过长，已截断: userId={}, length={}", userId, avatar.length());
                throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "头像 URL 过长，请重新上传");
            }
            user.setAvatar(avatar);
        }
        if (updateDTO.getGender() != null) {
            user.setGender(updateDTO.getGender());
        }
        if (updateDTO.getBirthday() != null) {
            user.setBirthday(updateDTO.getBirthday());
        }
        if (updateDTO.getAddress() != null) {
            user.setAddress(updateDTO.getAddress());
        }

        user.setUpdatedAt(LocalDateTime.now());
        int result = userMapper.updateById(user);

        log.info("更新用户个人信息成功: userId={}", userId);
        return result > 0;
    }

    /**
     * 将User实体转换为UserDTO，并填充统计信息
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        dto.setRegisterTime(user.getCreatedAt());
        dto.setStatus(user.getStatus());
        
        // 查询订单统计信息
        try {
            Map<String, Object> stats = orderMapper.selectUserOrderStats(user.getId());
            if (stats != null) {
                Object totalAmountObj = stats.get("totalAmount");
                Object orderCountObj = stats.get("orderCount");
                Object lastLoginTimeObj = stats.get("lastLoginTime");
                
                if (totalAmountObj != null) {
                    try {
                        dto.setTotalAmount(new BigDecimal(totalAmountObj.toString()));
                    } catch (Exception e) {
                        log.warn("转换消费总额失败: {}", e.getMessage());
                        dto.setTotalAmount(BigDecimal.ZERO);
                    }
                } else {
                    dto.setTotalAmount(BigDecimal.ZERO);
                }
                
                if (orderCountObj != null) {
                    try {
                        dto.setOrderCount(((Number) orderCountObj).intValue());
                    } catch (Exception e) {
                        log.warn("转换订单数量失败: {}", e.getMessage());
                        dto.setOrderCount(0);
                    }
                } else {
                    dto.setOrderCount(0);
                }
                
                if (lastLoginTimeObj != null && lastLoginTimeObj instanceof LocalDateTime) {
                    dto.setLastLoginTime((LocalDateTime) lastLoginTimeObj);
                }
            } else {
                dto.setTotalAmount(BigDecimal.ZERO);
                dto.setOrderCount(0);
            }
        } catch (Exception e) {
            log.warn("查询用户订单统计失败: userId={}, error={}", user.getId(), e.getMessage());
            dto.setTotalAmount(BigDecimal.ZERO);
            dto.setOrderCount(0);
        }
        
        return dto;
    }

    /**
     * 将UserViolation实体转换为UserViolationDTO
     */
    private UserViolationDTO convertViolationToDTO(UserViolation violation) {
        UserViolationDTO dto = new UserViolationDTO();
        dto.setId(violation.getId());
        dto.setType(violation.getType());
        
        // 设置类型文本
        String typeText;
        switch (violation.getType()) {
            case "warning":
                typeText = "警告";
                break;
            case "limit":
                typeText = "限制下单";
                break;
            case "freeze":
                typeText = "冻结账号";
                break;
            default:
                typeText = violation.getType();
        }
        dto.setTypeText(typeText);
        
        dto.setReason(violation.getReason());
        dto.setLimitDays(violation.getLimitDays());
        dto.setHandler(violation.getHandler());
        dto.setTime(violation.getCreatedAt());
        
        return dto;
    }
}

