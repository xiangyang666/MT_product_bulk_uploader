package com.meituan.product.dto;

import com.meituan.product.enums.FormatType;
import lombok.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 映射结果
 * 包含完整的字段映射信息、警告和错误
 */
@Data
public class MappingResult {
    /**
     * 格式类型
     */
    private FormatType formatType;
    
    /**
     * 字段映射（字段名 -> 列映射）
     */
    private Map<String, ColumnMapping> mappings;
    
    /**
     * 警告列表
     */
    private List<MappingWarning> warnings;
    
    /**
     * 错误列表
     */
    private List<MappingError> errors;
    
    /**
     * 映射是否有效（所有必需字段都已映射）
     */
    private boolean valid;
    
    /**
     * 构建耗时（毫秒）
     */
    private long buildTime;
    
    /**
     * 构造函数
     */
    public MappingResult() {
        this.mappings = new HashMap<>();
        this.warnings = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.valid = false;
    }
    
    /**
     * 构造函数
     */
    public MappingResult(FormatType formatType) {
        this();
        this.formatType = formatType;
    }
    
    /**
     * 添加映射
     */
    public void addMapping(ColumnMapping mapping) {
        this.mappings.put(mapping.getFieldName(), mapping);
    }
    
    /**
     * 添加警告
     */
    public void addWarning(MappingWarning warning) {
        this.warnings.add(warning);
    }
    
    /**
     * 添加错误
     */
    public void addError(MappingError error) {
        this.errors.add(error);
    }
    
    /**
     * 获取列索引映射（用于解析Excel）
     * @return 字段名 -> 列索引
     */
    public Map<String, Integer> getColumnIndexMap() {
        Map<String, Integer> indexMap = new HashMap<>();
        for (Map.Entry<String, ColumnMapping> entry : mappings.entrySet()) {
            indexMap.put(entry.getKey(), entry.getValue().getColumnIndex());
        }
        return indexMap;
    }
    
    /**
     * 检查是否有警告
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    /**
     * 检查是否有错误
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
