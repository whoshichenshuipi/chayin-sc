package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统配置Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据配置类型和配置键查询配置
     *
     * @param configType 配置类型
     * @param configKey 配置键
     * @return 配置信息
     */
    SystemConfig selectByTypeAndKey(@Param("configType") String configType, @Param("configKey") String configKey);
}

