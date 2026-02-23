package com.naicha.hou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.dto.NotificationCreateDTO;
import com.naicha.hou.dto.NotificationDTO;
import com.naicha.hou.dto.NotificationQueryDTO;
import com.naicha.hou.dto.NotificationSettingDTO;
import com.naicha.hou.dto.NotificationStatsDTO;

import java.util.List;

/**
 * 通知服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface NotificationService {

    /**
     * 创建通知
     */
    Long createNotification(NotificationCreateDTO createDTO);

    /**
     * 分页查询通知列表
     */
    IPage<NotificationDTO> getNotificationPage(NotificationQueryDTO queryDTO);

    /**
     * 获取通知详情
     */
    NotificationDTO getNotificationDetail(Long notificationId);

    /**
     * 标记通知为已读
     */
    boolean markAsRead(Long notificationId, Long recipientId, String recipientType);

    /**
     * 批量标记为已读
     */
    boolean batchMarkAsRead(List<Long> notificationIds, Long recipientId, String recipientType);

    /**
     * 标记所有为已读
     */
    boolean markAllAsRead(Long recipientId, String recipientType);

    /**
     * 删除通知
     */
    boolean deleteNotification(Long notificationId, Long recipientId, String recipientType);

    /**
     * 批量删除通知
     */
    boolean batchDeleteNotifications(List<Long> notificationIds, Long recipientId, String recipientType);

    /**
     * 获取未读通知数量
     */
    Integer getUnreadCount(Long recipientId, String recipientType);

    /**
     * 获取通知统计
     */
    NotificationStatsDTO getNotificationStats(Long recipientId, String recipientType);

    /**
     * 获取通知设置
     */
    NotificationSettingDTO getNotificationSettings(Long userId, String userType);

    /**
     * 更新通知设置
     */
    boolean updateNotificationSettings(Long userId, String userType, NotificationSettingDTO settingsDTO);

    /**
     * 发送管理员通知（商家入驻申请、消费者申诉、系统故障、合规检查报告）
     */
    void sendAdminNotification(String type, String title, String content, Long relatedId, String relatedType, Integer priority);

    /**
     * 发送商家通知（新订单、取消订单申请、售后申请、库存预警、结算单）
     */
    void sendMerchantNotification(Long merchantId, String type, String title, String content, Long relatedId, String relatedType, Integer priority);
}

