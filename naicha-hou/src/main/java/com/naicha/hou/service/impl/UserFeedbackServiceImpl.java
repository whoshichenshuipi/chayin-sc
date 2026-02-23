package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.common.ResultCode;
import com.naicha.hou.dto.NotificationCreateDTO;
import com.naicha.hou.dto.UserFeedbackAssignDTO;
import com.naicha.hou.dto.UserFeedbackCompleteDTO;
import com.naicha.hou.dto.UserFeedbackDTO;
import com.naicha.hou.dto.UserFeedbackQueryDTO;
import com.naicha.hou.entity.UserFeedback;
import com.naicha.hou.exception.BusinessException;
import com.naicha.hou.mapper.UserFeedbackMapper;
import com.naicha.hou.service.NotificationService;
import com.naicha.hou.service.UserFeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户反馈服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFeedbackServiceImpl implements UserFeedbackService {

    private final UserFeedbackMapper userFeedbackMapper;
    private final NotificationService notificationService;

    @Override
    public IPage<UserFeedbackDTO> getFeedbackPage(UserFeedbackQueryDTO queryDTO) {
        Page<UserFeedbackDTO> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        return userFeedbackMapper.selectFeedbackPage(page, queryDTO);
    }

    @Override
    public UserFeedbackDTO getFeedbackDetail(Long feedbackId) {
        UserFeedbackDTO feedback = userFeedbackMapper.selectFeedbackDetail(feedbackId);
        if (feedback == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "反馈不存在");
        }
        return feedback;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignFeedback(UserFeedbackAssignDTO assignDTO) {
        UserFeedback feedback = userFeedbackMapper.selectById(assignDTO.getFeedbackId());
        if (feedback == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "反馈不存在");
        }

        if (!"pending".equals(feedback.getStatus())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "只能分配待处理的反馈");
        }

        // 更新反馈信息
        feedback.setStatus("processing");
        feedback.setAssignee(assignDTO.getAssignee());
        feedback.setAssignNote(assignDTO.getNote());
        feedback.setAssignTime(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());

        userFeedbackMapper.updateById(feedback);

        log.info("分配反馈处理成功: feedbackId={}, assignee={}", assignDTO.getFeedbackId(), assignDTO.getAssignee());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeFeedback(UserFeedbackCompleteDTO completeDTO) {
        UserFeedback feedback = userFeedbackMapper.selectById(completeDTO.getFeedbackId());
        if (feedback == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "反馈不存在");
        }

        if (!"processing".equals(feedback.getStatus())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "只能完成处理中的反馈");
        }

        // 更新反馈信息
        feedback.setStatus("completed");
        feedback.setResult(completeDTO.getResult());
        feedback.setCompleteTime(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());

        userFeedbackMapper.updateById(feedback);

        // 如果设置了通知用户，发送通知
        if (Boolean.TRUE.equals(completeDTO.getNotifyUser()) && feedback.getUserId() != null) {
            try {
                NotificationCreateDTO notificationDTO = new NotificationCreateDTO();
                notificationDTO.setRecipientId(feedback.getUserId());
                notificationDTO.setRecipientType("user");
                notificationDTO.setType("feedback_completed");
                notificationDTO.setTitle("反馈处理完成");
                notificationDTO.setContent(String.format("您提交的反馈「%s」已处理完成。处理结果：%s", 
                    feedback.getTitle(), completeDTO.getResult()));
                notificationDTO.setPriority(2);
                notificationDTO.setRelatedId(feedback.getId());
                notificationDTO.setRelatedType("feedback");

                notificationService.createNotification(notificationDTO);
                log.info("已向用户发送反馈处理完成通知: userId={}, feedbackId={}", feedback.getUserId(), feedback.getId());
            } catch (Exception e) {
                log.warn("发送反馈处理完成通知失败: {}", e.getMessage());
                // 通知发送失败不影响反馈完成的处理
            }
        }

        log.info("完成反馈处理成功: feedbackId={}, notifyUser={}", completeDTO.getFeedbackId(), completeDTO.getNotifyUser());
        return true;
    }
}

