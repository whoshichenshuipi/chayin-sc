package com.naicha.hou.service;

import com.naicha.hou.dto.NotificationDTO;
import com.naicha.hou.entity.Notification;
import com.naicha.hou.enums.NotificationPriority;
import com.naicha.hou.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket通知服务
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 发送通知到指定用户
     */
    public void sendNotification(Notification notification) {
        try {
            NotificationDTO dto = convertToDTO(notification);
            
            // 根据接收者类型和ID构建目的地
            String destination = String.format("/topic/notifications/%s/%d", 
                notification.getRecipientType(), notification.getRecipientId());
            
            messagingTemplate.convertAndSend(destination, dto);
            
            log.info("WebSocket通知发送成功: {} -> {}", destination, notification.getTitle());
        } catch (Exception e) {
            log.error("WebSocket通知发送失败", e);
        }
    }

    /**
     * 发送通知到指定用户（使用用户目的地）
     */
    public void sendNotificationToUser(Long userId, String userType, NotificationDTO notification) {
        try {
            String destination = String.format("/user/%s/%d/notifications", userType, userId);
            messagingTemplate.convertAndSendToUser(
                String.format("%s-%d", userType, userId),
                "/notifications",
                notification
            );
            
            log.info("WebSocket通知发送成功: {} -> {}", destination, notification.getTitle());
        } catch (Exception e) {
            log.error("WebSocket通知发送失败", e);
        }
    }

    /**
     * 发送未读数量更新
     */
    public void sendUnreadCount(Long userId, String userType, Integer count) {
        try {
            String destination = String.format("/topic/notifications/%s/%d/unread-count", 
                userType, userId);
            
            messagingTemplate.convertAndSend(destination, count);
            
            log.debug("未读数量更新发送成功: {} -> {}", destination, count);
        } catch (Exception e) {
            log.error("未读数量更新发送失败", e);
        }
    }

    /**
     * 转换为DTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        BeanUtils.copyProperties(notification, dto);
        
        // 设置类型描述
        NotificationType type = NotificationType.getByCode(notification.getType());
        if (type != null) {
            dto.setTypeDescription(type.getDescription());
        }
        
        // 设置优先级描述
        NotificationPriority priority = NotificationPriority.getByCode(notification.getPriority());
        if (priority != null) {
            dto.setPriorityDescription(priority.getDescription());
        }
        
        return dto;
    }
}

