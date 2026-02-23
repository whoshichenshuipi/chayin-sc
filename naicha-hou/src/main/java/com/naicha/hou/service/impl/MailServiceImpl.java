package com.naicha.hou.service.impl;

import com.naicha.hou.enums.NotificationPriority;
import com.naicha.hou.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * 邮件服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("发送简单邮件成功: {} -> {}", from, to);
        } catch (Exception e) {
            log.error("发送简单邮件失败: {} -> {}", from, to, e);
            throw new RuntimeException("发送邮件失败: " + e.getMessage());
        }
    }

    @Override
    public void sendHtmlMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            log.info("发送HTML邮件成功: {} -> {}", from, to);
        } catch (MessagingException e) {
            log.error("发送HTML邮件失败: {} -> {}", from, to, e);
            throw new RuntimeException("发送邮件失败: " + e.getMessage());
        }
    }

    @Override
    public void sendNotificationMail(String to, String title, String content, Integer priority) {
        String priorityText = NotificationPriority.getDescriptionByCode(priority);
        String priorityColor = getPriorityColor(priority);
        
        String htmlContent = String.format(
            "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset='UTF-8'>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            ".header { background-color: %s; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
            ".content { background-color: #f9f9f9; padding: 20px; border-radius: 0 0 5px 5px; }" +
            ".priority { display: inline-block; padding: 5px 10px; background-color: %s; color: white; border-radius: 3px; font-size: 12px; }" +
            ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='header'>" +
            "<h2>%s</h2>" +
            "</div>" +
            "<div class='content'>" +
            "<p><span class='priority'>%s</span></p>" +
            "<div style='margin-top: 20px;'>%s</div>" +
            "</div>" +
            "<div class='footer'>" +
            "<p>此邮件由系统自动发送，请勿回复。</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>",
            priorityColor, priorityColor, title, priorityText, content.replace("\n", "<br>")
        );

        sendHtmlMail(to, title, htmlContent);
    }

    /**
     * 根据优先级获取颜色
     */
    private String getPriorityColor(Integer priority) {
        if (priority == null) {
            return "#3498db";
        }
        return switch (priority) {
            case 1 -> "#95a5a6"; // 低 - 灰色
            case 2 -> "#3498db"; // 普通 - 蓝色
            case 3 -> "#f39c12"; // 重要 - 橙色
            case 4 -> "#e74c3c"; // 紧急 - 红色
            default -> "#3498db";
        };
    }
}

