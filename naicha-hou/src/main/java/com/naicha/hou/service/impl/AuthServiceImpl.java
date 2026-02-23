package com.naicha.hou.service.impl;

import com.naicha.hou.common.ResultCode;
import com.naicha.hou.dto.LoginRequest;
import com.naicha.hou.dto.RegisterRequest;
import com.naicha.hou.dto.UserInfoResponse;
import com.naicha.hou.entity.Admin;
import com.naicha.hou.entity.Merchant;
import com.naicha.hou.entity.User;
import com.naicha.hou.exception.BusinessException;
import com.naicha.hou.mapper.AdminMapper;
import com.naicha.hou.mapper.MerchantMapper;
import com.naicha.hou.mapper.UserMapper;
import com.naicha.hou.service.AuthService;
import com.naicha.hou.utils.PasswordUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 认证服务实现类
 *
 * @author naicha
 * @since 2023-12-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final MerchantMapper merchantMapper;

    @Override
    public UserInfoResponse login(LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String userType = loginRequest.getUserType();

        UserInfoResponse userInfo = null;

        switch (userType) {
            case "admin":
                userInfo = loginAdmin(username, password);
                break;
            case "merchant":
                userInfo = loginMerchant(username, password);
                break;
            case "user":
                userInfo = loginUser(username, password);
                break;
            default:
                throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "不支持的用户类型");
        }

        if (userInfo == null) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR.getCode(), "用户名或密码错误");
        }

        log.info("用户登录成功: {}", username);
        return userInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse register(RegisterRequest registerRequest) {
        log.info("用户注册请求: {}", registerRequest.getUsername());

        // 验证密码一致性
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "两次输入的密码不一致");
        }

        String userType = registerRequest.getUserType();
        UserInfoResponse userInfo = null;

        switch (userType) {
            case "user":
                userInfo = registerUser(registerRequest);
                break;
            case "merchant":
                userInfo = registerMerchant(registerRequest);
                break;
            case "admin":
                userInfo = registerAdmin(registerRequest);
                break;
            default:
                throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "不支持的用户类型");
        }

        log.info("用户注册成功: {}", registerRequest.getUsername());
        return userInfo;
    }

    @Override
    public boolean checkUsernameExists(String username, String userType) {
        switch (userType) {
            case "admin":
                return adminMapper.existsByUsername(username);
            case "merchant":
                return merchantMapper.existsByUsername(username);
            case "user":
                return userMapper.existsByUsername(username);
            default:
                return false;
        }
    }

    @Override
    public boolean checkEmailExists(String email) {
        return userMapper.existsByEmail(email);
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        return userMapper.existsByPhone(phone);
    }

    @Override
    public boolean checkShopNameExists(String shopName) {
        return merchantMapper.existsByShopName(shopName);
    }

    /**
     * 管理员登录
     */
    private UserInfoResponse loginAdmin(String username, String password) {
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) {
            return null;
        }

        if (admin.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_DISABLED.getCode(), "账号已被禁用");
        }

        if (!PasswordUtils.matches(password, admin.getPassword())) {
            return null;
        }

        return convertAdminToUserInfo(admin);
    }

    /**
     * 商家登录
     */
    private UserInfoResponse loginMerchant(String username, String password) {
        Merchant merchant = merchantMapper.selectByUsername(username);
        if (merchant == null) {
            return null;
        }

        if (merchant.getStatus() == 3) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_DISABLED.getCode(), "账号已被禁用");
        }

        if (!PasswordUtils.matches(password, merchant.getPassword())) {
            return null;
        }

        return convertMerchantToUserInfo(merchant);
    }

    /**
     * 普通用户登录
     */
    private UserInfoResponse loginUser(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return null;
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_DISABLED.getCode(), "账号已被禁用");
        }

        if (!PasswordUtils.matches(password, user.getPassword())) {
            return null;
        }

        return convertUserToUserInfo(user);
    }

    /**
     * 普通用户注册
     */
    private UserInfoResponse registerUser(RegisterRequest registerRequest) {
        // 检查用户名是否存在
        if (userMapper.existsByUsername(registerRequest.getUsername())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), "用户名已存在");
        }

        // 检查邮箱是否存在
        if (StringUtils.hasText(registerRequest.getEmail()) && userMapper.existsByEmail(registerRequest.getEmail())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), "邮箱已存在");
        }

        // 检查手机号是否存在
        if (StringUtils.hasText(registerRequest.getPhone()) && userMapper.existsByPhone(registerRequest.getPhone())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), "手机号已存在");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(PasswordUtils.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setNickname(registerRequest.getNickname());
        user.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        userMapper.insert(user);

        return convertUserToUserInfo(user);
    }

    /**
     * 商家注册
     */
    private UserInfoResponse registerMerchant(RegisterRequest registerRequest) {
        // 检查用户名是否存在
        if (merchantMapper.existsByUsername(registerRequest.getUsername())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), "用户名已存在");
        }

        // 检查店铺名称是否存在
        if (StringUtils.hasText(registerRequest.getShopName()) && merchantMapper.existsByShopName(registerRequest.getShopName())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), "店铺名称已存在");
        }

        Merchant merchant = new Merchant();
        merchant.setUsername(registerRequest.getUsername());
        merchant.setPassword(PasswordUtils.encode(registerRequest.getPassword()));
        merchant.setShopName(registerRequest.getShopName());
        merchant.setContactName(registerRequest.getContactName());
        merchant.setContactPhone(registerRequest.getContactPhone());
        merchant.setContactEmail(registerRequest.getEmail());
        merchant.setAddress(registerRequest.getAddress());
        merchant.setStatus(0); // 待审核
        LocalDateTime now = LocalDateTime.now();
        merchant.setCreatedAt(now);
        merchant.setUpdatedAt(now);

        merchantMapper.insert(merchant);

        return convertMerchantToUserInfo(merchant);
    }

    /**
     * 管理员注册
     */
    private UserInfoResponse registerAdmin(RegisterRequest registerRequest) {
        // 检查用户名是否存在
        if (adminMapper.existsByUsername(registerRequest.getUsername())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), "用户名已存在");
        }

        // 检查邮箱是否存在
        if (StringUtils.hasText(registerRequest.getEmail()) && adminMapper.existsByEmail(registerRequest.getEmail())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), "邮箱已存在");
        }

        // 检查手机号是否存在
        if (StringUtils.hasText(registerRequest.getPhone()) && adminMapper.existsByPhone(registerRequest.getPhone())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), "手机号已存在");
        }

        Admin admin = new Admin();
        admin.setUsername(registerRequest.getUsername());
        admin.setPassword(PasswordUtils.encode(registerRequest.getPassword()));
        admin.setRealName(registerRequest.getRealName());
        admin.setEmail(registerRequest.getEmail());
        admin.setPhone(registerRequest.getPhone());
        admin.setRole("admin");
        admin.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);

        adminMapper.insert(admin);

        return convertAdminToUserInfo(admin);
    }

    /**
     * 转换管理员为用户信息响应
     */
    private UserInfoResponse convertAdminToUserInfo(Admin admin) {
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(admin.getId());
        userInfo.setUsername(admin.getUsername());
        userInfo.setEmail(admin.getEmail());
        userInfo.setPhone(admin.getPhone());
        userInfo.setRealName(admin.getRealName());
        userInfo.setRole(admin.getRole());
        userInfo.setStatus(admin.getStatus());
        userInfo.setUserType("admin");
        userInfo.setCreatedAt(admin.getCreatedAt());
        userInfo.setUpdatedAt(admin.getUpdatedAt());
        return userInfo;
    }

    /**
     * 转换商家为用户信息响应
     */
    private UserInfoResponse convertMerchantToUserInfo(Merchant merchant) {
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(merchant.getId());
        userInfo.setUsername(merchant.getUsername());
        userInfo.setShopName(merchant.getShopName());
        userInfo.setShopLogo(merchant.getShopLogo());
        userInfo.setContactName(merchant.getContactName());
        userInfo.setContactPhone(merchant.getContactPhone());
        userInfo.setContactEmail(merchant.getContactEmail());
        userInfo.setAddress(merchant.getAddress());
        userInfo.setDescription(merchant.getDescription());
        userInfo.setStatus(merchant.getStatus());
        userInfo.setUserType("merchant");
        userInfo.setCreatedAt(merchant.getCreatedAt());
        userInfo.setUpdatedAt(merchant.getUpdatedAt());
        return userInfo;
    }

    /**
     * 转换普通用户为用户信息响应
     */
    private UserInfoResponse convertUserToUserInfo(User user) {
        UserInfoResponse userInfo = new UserInfoResponse();
        BeanUtils.copyProperties(user, userInfo);
        userInfo.setUserType("user");
        return userInfo;
    }
}
