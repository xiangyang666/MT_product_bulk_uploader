package com.meituan.product.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

/**
 * 模板配置类
 */
@Slf4j
@Data
@Component
public class TemplateConfig {
    
    private String templateName;
    private String version;
    private List<ColumnConfig> columns;
    
    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("template-config.json");
            ObjectMapper mapper = new ObjectMapper();
            TemplateConfig config = mapper.readValue(resource.getInputStream(), TemplateConfig.class);
            
            this.templateName = config.getTemplateName();
            this.version = config.getVersion();
            this.columns = config.getColumns();
            
            log.info("成功加载模板配置：{}", templateName);
        } catch (IOException e) {
            log.error("加载模板配置失败", e);
            throw new RuntimeException("加载模板配置失败", e);
        }
    }
    
    /**
     * 列配置
     */
    @Data
    public static class ColumnConfig {
        private String field;
        private String headerName;
        private Boolean required;
        private String type;
        private Integer maxLength;
        private String pattern;
        private Double min;
        private Double max;
    }
}
