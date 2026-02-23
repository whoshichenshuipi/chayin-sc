package com.naicha.hou.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson配置类
 * 配置LocalDateTime的序列化和反序列化格式
 *
 * @author naicha
 * @since 2024-01-01
 */
@Configuration
public class JacksonConfig {

    /**
     * 自定义LocalDateTime反序列化器
     * 支持多种日期格式：
     * - yyyy-MM-ddTHH:mm:ss (ISO格式)
     * - yyyy-MM-dd HH:mm:ss (空格格式)
     */
    private static class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
        private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        private static final DateTimeFormatter SPACE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        protected LocalDateTimeDeserializer() {
            super(LocalDateTime.class);
        }

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String dateTimeStr = p.getText().trim();
            
            // 尝试 ISO 格式（带 T）
            if (dateTimeStr.contains("T")) {
                try {
                    return LocalDateTime.parse(dateTimeStr, ISO_FORMATTER);
                } catch (Exception e) {
                    // 如果解析失败，尝试其他格式
                }
            }
            
            // 尝试空格格式
            try {
                return LocalDateTime.parse(dateTimeStr, SPACE_FORMATTER);
            } catch (Exception e) {
                // 如果都失败，抛出异常
                throw new IOException("无法解析日期时间格式: " + dateTimeStr, e);
            }
        }
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 配置LocalDateTime序列化器（输出格式）
        javaTimeModule.addSerializer(LocalDateTime.class, 
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // 配置LocalDateTime反序列化器（支持多种输入格式）
        javaTimeModule.addDeserializer(LocalDateTime.class, 
                new LocalDateTimeDeserializer());
        
        return builder
                .modules(javaTimeModule)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }
}

