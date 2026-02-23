package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.NotificationSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知设置Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface NotificationSettingMapper extends BaseMapper<NotificationSetting> {
}

