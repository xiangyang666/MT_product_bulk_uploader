package com.meituan.product.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Jackson 配置 - 解决时区问题
 * 
 * 问题：MySQL 存储的是 CST 时间，但 Jackson 序列化时使用 UTC，导致时间相差8小时
 * 解决：配置 Jackson 使用 Asia/Shanghai 时区
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置 Jackson ObjectMapper
     * 设置默认时区为 Asia/Shanghai（CST / UTC+8）
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // 设置时区为中国标准时间
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        
        // 配置 JavaTimeModule 处理 LocalDateTime
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // LocalDateTime 序列化格式
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        
        objectMapper.registerModule(javaTimeModule);
        
        return objectMapper;
    }
}
