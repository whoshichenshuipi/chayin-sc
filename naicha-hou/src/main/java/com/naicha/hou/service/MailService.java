package com.naicha.hou.service;

/**
 * 邮件服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface MailService {

    /**
     * 发送简单文本邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    void sendSimpleMail(String to, String subject, String content);

    /**
     * 发送HTML邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param content HTML内容
     */
    void sendHtmlMail(String to, String subject, String content);

    /**
     * 发送通知邮件
     *
     * @param to      收件人邮箱
     * @param title   通知标题
     * @param content 通知内容
     * @param priority 优先级
     */
    void sendNotificationMail(String to, String title, String content, Integer priority);
}

