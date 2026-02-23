package com.naicha.hou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.dto.UserDTO;
import com.naicha.hou.dto.UserProfileUpdateDTO;
import com.naicha.hou.dto.UserQueryDTO;
import com.naicha.hou.dto.UserViolationRequestDTO;

/**
 * 用户服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface UserService {

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件
     * @return 分页数据
     */
    IPage<UserDTO> getUserPage(UserQueryDTO queryDTO);

    /**
     * 根据用户ID获取用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    UserDTO getUserDetail(Long userId);

    /**
     * 处理用户违规
     *
     * @param requestDTO 违规处理请求
     * @param handler 处理人（管理员用户名或ID）
     * @return 是否成功
     */
    boolean handleUserViolation(UserViolationRequestDTO requestDTO, String handler);

    /**
     * 更新用户个人信息
     *
     * @param userId 用户ID
     * @param updateDTO 更新信息
     * @return 是否成功
     */
    boolean updateUserProfile(Long userId, UserProfileUpdateDTO updateDTO);
}

