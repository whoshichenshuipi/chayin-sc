package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.dto.NotificationDTO;
import com.naicha.hou.dto.NotificationQueryDTO;
import com.naicha.hou.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 通知Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 分页查询通知列表
     */
    IPage<NotificationDTO> selectNotificationPage(Page<NotificationDTO> page, @Param("query") NotificationQueryDTO query);

    /**
     * 查询未读通知数量
     */
    Integer countUnread(@Param("recipientId") Long recipientId, @Param("recipientType") String recipientType);

    /**
     * 统计通知数量（按类型）
     */
    List<Map<String, Object>> countByType(@Param("recipientId") Long recipientId, @Param("recipientType") String recipientType);

    /**
     * 统计通知数量（按优先级）
     */
    List<Map<String, Object>> countByPriority(@Param("recipientId") Long recipientId, @Param("recipientType") String recipientType);

    /**
     * 批量标记为已读
     */
    int batchMarkAsRead(@Param("recipientId") Long recipientId, @Param("recipientType") String recipientType, @Param("ids") List<Long> ids);

    /**
     * 标记所有为已读
     */
    int markAllAsRead(@Param("recipientId") Long recipientId, @Param("recipientType") String recipientType);
}

