package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naicha.hou.dto.NotificationCreateDTO;
import com.naicha.hou.dto.NotificationDTO;
import com.naicha.hou.dto.NotificationQueryDTO;
import com.naicha.hou.dto.NotificationSettingDTO;
import com.naicha.hou.dto.NotificationStatsDTO;
import com.naicha.hou.entity.Notification;
import com.naicha.hou.entity.NotificationSetting;
import com.naicha.hou.enums.NotificationPriority;
import com.naicha.hou.enums.NotificationType;
import com.naicha.hou.mapper.NotificationMapper;
import com.naicha.hou.mapper.NotificationSettingMapper;
import com.naicha.hou.service.MailService;
import com.naicha.hou.service.NotificationService;
import com.naicha.hou.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationSettingMapper notificationSettingMapper;
    private final MailService mailService;
    private final WebSocketNotificationService webSocketNotificationService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Long createNotification(NotificationCreateDTO createDTO) {
        Notification notification = new Notification();
        BeanUtils.copyProperties(createDTO, notification);
        
        // 处理操作按钮JSON
        if (createDTO.getActions() != null && !createDTO.getActions().isEmpty()) {
            try {
                notification.setActions(objectMapper.writeValueAsString(createDTO.getActions()));
            } catch (Exception e) {
                log.error("转换操作按钮失败", e);
            }
        }
        
        // 处理扩展数据JSON
        if (createDTO.getExtraData() != null && !createDTO.getExtraData().isEmpty()) {
            try {
                notification.setExtraData(objectMapper.writeValueAsString(createDTO.getExtraData()));
            } catch (Exception e) {
                log.error("转换扩展数据失败", e);
            }
        }
        
        notification.setIsRead(0);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        
        save(notification);
        
        // 发送WebSocket通知
        webSocketNotificationService.sendNotification(notification);
        
        // 根据设置发送邮件和短信
        sendNotificationBySettings(notification);
        
        return notification.getId();
    }

    @Override
    public IPage<NotificationDTO> getNotificationPage(NotificationQueryDTO queryDTO) {
        Page<NotificationDTO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<NotificationDTO> result = notificationMapper.selectNotificationPage(page, queryDTO);
        
        // 处理JSON字段
        result.getRecords().forEach(this::enrichNotificationData);
        
        return result;
    }

    @Override
    public NotificationDTO getNotificationDetail(Long notificationId) {
        Notification notification = getById(notificationId);
        if (notification == null) {
            return null;
        }
        
        NotificationDTO dto = new NotificationDTO();
        BeanUtils.copyProperties(notification, dto);
        
        // 设置类型和优先级描述
        NotificationType type = NotificationType.getByCode(notification.getType());
        if (type != null) {
            dto.setTypeDescription(type.getDescription());
        }
        
        NotificationPriority priority = NotificationPriority.getByCode(notification.getPriority());
        if (priority != null) {
            dto.setPriorityDescription(priority.getDescription());
        }
        
        enrichNotificationData(dto);
        
        return dto;
    }

    @Override
    @Transactional
    public boolean markAsRead(Long notificationId, Long recipientId, String recipientType) {
        Notification notification = getById(notificationId);
        if (notification == null || !notification.getRecipientId().equals(recipientId) 
            || !notification.getRecipientType().equals(recipientType)) {
            return false;
        }
        
        notification.setIsRead(1);
        notification.setUpdatedAt(LocalDateTime.now());
        return updateById(notification);
    }

    @Override
    @Transactional
    public boolean batchMarkAsRead(List<Long> notificationIds, Long recipientId, String recipientType) {
        return notificationMapper.batchMarkAsRead(recipientId, recipientType, notificationIds) > 0;
    }

    @Override
    @Transactional
    public boolean markAllAsRead(Long recipientId, String recipientType) {
        return notificationMapper.markAllAsRead(recipientId, recipientType) > 0;
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long notificationId, Long recipientId, String recipientType) {
        Notification notification = getById(notificationId);
        if (notification == null || !notification.getRecipientId().equals(recipientId) 
            || !notification.getRecipientType().equals(recipientType)) {
            return false;
        }
        
        return removeById(notificationId);
    }

    @Override
    @Transactional
    public boolean batchDeleteNotifications(List<Long> notificationIds, Long recipientId, String recipientType) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getRecipientId, recipientId)
               .eq(Notification::getRecipientType, recipientType)
               .in(Notification::getId, notificationIds);
        
        return remove(wrapper);
    }

    @Override
    public Integer getUnreadCount(Long recipientId, String recipientType) {
        return notificationMapper.countUnread(recipientId, recipientType);
    }

    @Override
    public NotificationStatsDTO getNotificationStats(Long recipientId, String recipientType) {
        NotificationStatsDTO stats = new NotificationStatsDTO();
        
        // 查询未读数量
        Integer unreadCount = notificationMapper.countUnread(recipientId, recipientType);
        stats.setUnreadCount(unreadCount);
        
        // 查询总数和已读数量
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getRecipientId, recipientId)
               .eq(Notification::getRecipientType, recipientType);
        
        long totalCount = count(wrapper);
        stats.setTotalCount((int) totalCount);
        stats.setReadCount((int) (totalCount - unreadCount));
        
        // 按类型统计
        List<Map<String, Object>> typeStats = notificationMapper.countByType(recipientId, recipientType);
        Map<String, Integer> countByType = typeStats.stream()
            .collect(Collectors.toMap(
                m -> (String) m.get("type"),
                m -> ((Number) m.get("count")).intValue()
            ));
        stats.setCountByType(countByType);
        
        // 按优先级统计
        List<Map<String, Object>> priorityStats = notificationMapper.countByPriority(recipientId, recipientType);
        Map<String, Integer> countByPriority = priorityStats.stream()
            .collect(Collectors.toMap(
                m -> String.valueOf(m.get("priority")),
                m -> ((Number) m.get("count")).intValue()
            ));
        stats.setCountByPriority(countByPriority);
        
        return stats;
    }

    @Override
    public NotificationSettingDTO getNotificationSettings(Long userId, String userType) {
        LambdaQueryWrapper<NotificationSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationSetting::getUserId, userId)
               .eq(NotificationSetting::getUserType, userType);
        
        List<NotificationSetting> settings = notificationSettingMapper.selectList(wrapper);
        
        NotificationSettingDTO dto = new NotificationSettingDTO();
        dto.setUserId(userId);
        dto.setUserType(userType);
        
        if (!settings.isEmpty()) {
            NotificationSetting first = settings.get(0);
            dto.setNotificationMethods(new ArrayList<>());
            
            if (first.getEnablePopup() == 1) {
                dto.getNotificationMethods().add("popup");
            }
            if (first.getEnableSound() == 1) {
                dto.getNotificationMethods().add("sound");
            }
            if (first.getEnableEmail() == 1) {
                dto.getNotificationMethods().add("email");
            }
            if (first.getEnableSms() == 1) {
                dto.getNotificationMethods().add("sms");
            }
            
            dto.setQuietStartTime(first.getQuietStartTime());
            dto.setQuietEndTime(first.getQuietEndTime());
            dto.setSoundEnabled(first.getEnableSound() == 1);
        }
        
        return dto;
    }

    @Override
    @Transactional
    public boolean updateNotificationSettings(Long userId, String userType, NotificationSettingDTO settingsDTO) {
        // 这里简化处理，实际应该为每个通知类型保存设置
        // 为了示例，只保存一个通用设置
        
        LambdaQueryWrapper<NotificationSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationSetting::getUserId, userId)
               .eq(NotificationSetting::getUserType, userType)
               .isNull(NotificationSetting::getNotificationType);
        
        NotificationSetting setting = notificationSettingMapper.selectOne(wrapper);
        
        if (setting == null) {
            setting = new NotificationSetting();
            setting.setUserId(userId);
            setting.setUserType(userType);
            setting.setCreatedAt(LocalDateTime.now());
        }
        
        List<String> methods = settingsDTO.getNotificationMethods();
        if (methods == null) {
            methods = new ArrayList<>();
        }
        
        setting.setEnablePopup(methods.contains("popup") ? 1 : 0);
        setting.setEnableSound(methods.contains("sound") ? 1 : 0);
        setting.setEnableEmail(methods.contains("email") ? 1 : 0);
        setting.setEnableSms(methods.contains("sms") ? 1 : 0);
        setting.setQuietStartTime(settingsDTO.getQuietStartTime());
        setting.setQuietEndTime(settingsDTO.getQuietEndTime());
        setting.setUpdatedAt(LocalDateTime.now());
        
        if (setting.getId() == null) {
            return notificationSettingMapper.insert(setting) > 0;
        } else {
            return notificationSettingMapper.updateById(setting) > 0;
        }
    }

    @Override
    public void sendAdminNotification(String type, String title, String content, Long relatedId, String relatedType, Integer priority) {
        // 查询所有管理员ID（这里简化处理，实际应该从数据库查询）
        List<Long> adminIds = getAdminIds();
        
        for (Long adminId : adminIds) {
            NotificationCreateDTO createDTO = new NotificationCreateDTO();
            createDTO.setRecipientId(adminId);
            createDTO.setRecipientType("admin");
            createDTO.setType(type);
            createDTO.setTitle(title);
            createDTO.setContent(content);
            createDTO.setRelatedId(relatedId);
            createDTO.setRelatedType(relatedType);
            createDTO.setPriority(priority != null ? priority : NotificationPriority.NORMAL.getCode());
            
            createNotification(createDTO);
        }
    }

    @Override
    public void sendMerchantNotification(Long merchantId, String type, String title, String content, Long relatedId, String relatedType, Integer priority) {
        NotificationCreateDTO createDTO = new NotificationCreateDTO();
        createDTO.setRecipientId(merchantId);
        createDTO.setRecipientType("merchant");
        createDTO.setType(type);
        createDTO.setTitle(title);
        createDTO.setContent(content);
        createDTO.setRelatedId(relatedId);
        createDTO.setRelatedType(relatedType);
        createDTO.setPriority(priority != null ? priority : NotificationPriority.NORMAL.getCode());
        
        createNotification(createDTO);
    }

    /**
     * 根据设置发送通知（邮件、短信等）
     */
    private void sendNotificationBySettings(Notification notification) {
        // 查询接收者的通知设置
        LambdaQueryWrapper<NotificationSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationSetting::getUserId, notification.getRecipientId())
               .eq(NotificationSetting::getUserType, notification.getRecipientType())
               .or(w -> w.isNull(NotificationSetting::getNotificationType)
                   .eq(NotificationSetting::getNotificationType, notification.getType()));
        
        List<NotificationSetting> settings = notificationSettingMapper.selectList(wrapper);
        
        // 检查是否在免打扰时间
        boolean isQuietTime = isQuietTime(settings);
        
        // 检查是否应该发送邮件
        boolean shouldSendEmail = settings.stream()
            .anyMatch(s -> s.getEnableEmail() == 1 && (!isQuietTime || notification.getPriority() >= NotificationPriority.URGENT.getCode()));
        
        if (shouldSendEmail) {
            String email = getRecipientEmail(notification.getRecipientId(), notification.getRecipientType());
            if (email != null) {
                try {
                    mailService.sendNotificationMail(email, notification.getTitle(), notification.getContent(), notification.getPriority());
                } catch (Exception e) {
                    log.error("发送通知邮件失败", e);
                }
            }
        }
        
        // 检查是否应该发送短信（关键通知如系统故障）
        boolean shouldSendSms = notification.getType().equals(NotificationType.SYSTEM_ERROR.getCode())
            || (notification.getPriority() >= NotificationPriority.URGENT.getCode() 
                && settings.stream().anyMatch(s -> s.getEnableSms() == 1));
        
        if (shouldSendSms) {
            String phone = getRecipientPhone(notification.getRecipientId(), notification.getRecipientType());
            if (phone != null) {
                // TODO: 实现短信发送逻辑
                log.info("需要发送短信通知到: {}", phone);
            }
        }
    }

    /**
     * 判断是否在免打扰时间
     */
    private boolean isQuietTime(List<NotificationSetting> settings) {
        if (settings.isEmpty()) {
            return false;
        }
        
        NotificationSetting setting = settings.get(0);
        if (setting.getQuietStartTime() == null || setting.getQuietEndTime() == null) {
            return false;
        }
        
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.parse(setting.getQuietStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime end = LocalTime.parse(setting.getQuietEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
        
        if (start.isBefore(end)) {
            return now.isAfter(start) && now.isBefore(end);
        } else {
            // 跨天的情况
            return now.isAfter(start) || now.isBefore(end);
        }
    }

    /**
     * 获取接收者邮箱
     */
    private String getRecipientEmail(Long recipientId, String recipientType) {
        // TODO: 从数据库查询管理员或商家的邮箱
        if ("merchant".equals(recipientType)) {
            // 查询商家邮箱
            return null; // 需要实现
        } else {
            // 查询管理员邮箱
            return null; // 需要实现
        }
    }

    /**
     * 获取接收者手机号
     */
    private String getRecipientPhone(Long recipientId, String recipientType) {
        // TODO: 从数据库查询管理员或商家的手机号
        return null;
    }

    /**
     * 获取所有管理员ID
     */
    private List<Long> getAdminIds() {
        // TODO: 从数据库查询所有管理员ID
        return Collections.emptyList();
    }

    /**
     * 丰富通知数据
     */
    private void enrichNotificationData(NotificationDTO dto) {
        // 解析操作按钮JSON
        if (dto.getActions() == null && dto.getId() != null) {
            Notification notification = getById(dto.getId());
            if (notification != null && notification.getActions() != null) {
                try {
                    List<NotificationDTO.NotificationAction> actions = objectMapper.readValue(
                        notification.getActions(),
                        new TypeReference<List<NotificationDTO.NotificationAction>>() {}
                    );
                    dto.setActions(actions);
                } catch (Exception e) {
                    log.error("解析操作按钮失败", e);
                }
            }
        }
        
        // 解析扩展数据JSON
        if (dto.getExtraData() == null && dto.getId() != null) {
            Notification notification = getById(dto.getId());
            if (notification != null && notification.getExtraData() != null) {
                try {
                    Map<String, Object> extraData = objectMapper.readValue(
                        notification.getExtraData(),
                        new TypeReference<Map<String, Object>>() {}
                    );
                    dto.setExtraData(extraData);
                } catch (Exception e) {
                    log.error("解析扩展数据失败", e);
                }
            }
        }
    }
}

