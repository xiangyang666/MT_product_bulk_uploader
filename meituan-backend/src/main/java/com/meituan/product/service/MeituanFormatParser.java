package com.meituan.product.service;

import com.meituan.product.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * 美团格式Excel解析器
 * 用于解析美团后台导出的商品数据（50+列）
 */
@Slf4j
@Component
public class MeituanFormatParser {
    
    /**
     * 解析美团格式的Excel行（支持50+字段）
     * 
     * @param row Excel行数据
     * @param columnMapping 列映射（系统字段名 -> 列索引）
     * @param rowNum 行号
     * @return 商品对象
     */
    public Product parseRow(Row row, Map<String, Integer> columnMapping, int rowNum) {
        Product product = new Product();
        
        try {
            // ============================================
            // 基础信息字段
            // ============================================
            product.setSkuId(extractField(row, "skuId", columnMapping));
            product.setUpcEan(extractField(row, "upcEan", columnMapping));
            product.setCategoryName(extractField(row, "categoryName", columnMapping));
            product.setCategoryId(extractField(row, "categoryId", columnMapping));
            product.setAppSpuCode(extractField(row, "appSpuCode", columnMapping));
            product.setProductName(extractField(row, "productName", columnMapping));
            
            // ============================================
            // 图片视频字段
            // ============================================
            product.setProductImage(extractField(row, "productImage", columnMapping));
            product.setCoverVideo(extractField(row, "coverVideo", columnMapping));
            product.setSpecImage(extractField(row, "specImage", columnMapping));
            product.setSpecImageUrl(extractField(row, "specImageUrl", columnMapping));
            product.setImageUrl(extractField(row, "imageUrl", columnMapping));
            
            // ============================================
            // 分类库存字段
            // ============================================
            product.setStoreCategory(extractField(row, "storeCategory", columnMapping));
            product.setStoreCategoryCount(parseInteger(extractField(row, "storeCategoryCount", columnMapping)));
            product.setSpecName(extractField(row, "specName", columnMapping));
            product.setStoreCode(extractField(row, "storeCode", columnMapping));
            
            // 价格字段 - 添加详细日志
            String priceStr = extractField(row, "price", columnMapping);
            log.debug("第{}行：价格字符串 = '{}'", rowNum, priceStr);
            BigDecimal price = parsePrice(priceStr);
            log.debug("第{}行：解析后价格 = {}", rowNum, price);
            product.setPrice(price != null ? price : BigDecimal.ZERO);
            
            product.setStock(parseStock(extractField(row, "stock", columnMapping)));
            product.setSaleStatus(extractField(row, "saleStatus", columnMapping));
            product.setMonthlySales(parseInteger(extractField(row, "monthlySales", columnMapping)));
            product.setWeight(parseDecimal(extractField(row, "weight", columnMapping)));
            product.setWeightUnit(extractField(row, "weightUnit", columnMapping));
            product.setBrand(extractField(row, "brand", columnMapping));
            product.setMinPurchase(parseInteger(extractField(row, "minPurchase", columnMapping)));
            product.setShelfCode(extractField(row, "shelfCode", columnMapping));
            
            // ============================================
            // 详情描述字段
            // ============================================
            product.setDescription(extractField(row, "description", columnMapping));
            product.setSellingPoint(extractField(row, "sellingPoint", columnMapping));
            product.setSellingPointPeriod(extractField(row, "sellingPointPeriod", columnMapping));
            product.setTextDetail(extractField(row, "textDetail", columnMapping));
            product.setImageDetail(extractField(row, "imageDetail", columnMapping));
            product.setBrandImageDetail(extractField(row, "brandImageDetail", columnMapping));
            
            // ============================================
            // 日期相关字段
            // ============================================
            product.setProductionDate(parseDate(extractField(row, "productionDate", columnMapping)));
            product.setExpiryDate(parseDate(extractField(row, "expiryDate", columnMapping)));
            product.setIsNearExpiry(parseBoolean(extractField(row, "isNearExpiry", columnMapping)));
            product.setIsExpired(parseBoolean(extractField(row, "isExpired", columnMapping)));
            
            // ============================================
            // 配送时间字段
            // ============================================
            product.setDeliveryMode(extractField(row, "deliveryMode", columnMapping));
            product.setPresaleDeliveryTime(extractField(row, "presaleDeliveryTime", columnMapping));
            product.setAvailableTime(extractField(row, "availableTime", columnMapping));

            // ============================================
            // 商品属性字段 - 完全依赖列映射
            // ============================================
            // 优先从列映射读取
            String productAttrs = extractField(row, "productAttributes", columnMapping);
            // 如果映射失败，尝试从常见索引读取（作为最后的备选）
            if (productAttrs == null || productAttrs.trim().isEmpty()) {
                productAttrs = extractFieldByIndex(row, 50);
            }
            product.setProductAttributes(productAttrs);
            log.info("第{}行：类目属性 = {}", rowNum, productAttrs);
            
            // 添加诊断日志
            if (productAttrs == null || productAttrs.trim().isEmpty()) {
                log.warn("第{}行：类目属性为空！类目名称：{}，类目ID：{}", 
                    rowNum, product.getCategoryName(), product.getCategoryId());
            } else if (productAttrs.length() < 50) {
                log.warn("第{}行：类目属性过短（{}字符），可能不完整：{}", 
                    rowNum, productAttrs.length(), productAttrs);
            }

            // 从productAttributes中提取品牌（优先）
            String extractedBrand = extractBrandFromAttributes(productAttrs);
            if (extractedBrand != null && !extractedBrand.isEmpty()) {
                product.setBrand(extractedBrand);
            } else {
                // 如果没有从productAttributes中提取到品牌，检查brand列
                String brandColumn = extractField(row, "brand", columnMapping);
                if (brandColumn != null && !brandColumn.trim().isEmpty() && !"无".equals(brandColumn)) {
                    product.setBrand(brandColumn);
                }
            }
            log.info("第{}行：提取的品牌 = {}", rowNum, product.getBrand());

            product.setIsRecommended(parseBoolean(extractField(row, "isRecommended", columnMapping)));
            
            // 解析无理由退货标签ID
            String noReasonReturnValue = extractField(row, "noReasonReturnTagId", columnMapping);
            String tagId = parseNoReasonReturnTagId(noReasonReturnValue);
            product.setNoReasonReturnTagId(tagId);
            // 同时设置旧的布尔字段（向后兼容）
            product.setNoReasonReturn("1300030901".equals(tagId) ? 1 : 0);
            
            product.setIsCombo(parseBoolean(extractField(row, "isCombo", columnMapping)));
            product.setComboProducts(extractField(row, "comboProducts", columnMapping));
            product.setIsFourWheelDelivery(parseBoolean(extractField(row, "isFourWheelDelivery", columnMapping)));
            
            // ============================================
            // 合规审核字段
            // ============================================
            product.setComplianceStatus(extractField(row, "complianceStatus", columnMapping));
            product.setViolationOffline(parseBoolean(extractField(row, "violationOffline", columnMapping)));
            product.setMissingRequiredInfo(parseBoolean(extractField(row, "missingRequiredInfo", columnMapping)));
            product.setAuditStatus(extractField(row, "auditStatus", columnMapping));
            
            // ============================================
            // 系统字段
            // ============================================
            product.setStatus(Product.Status.PENDING.getCode());
            
            log.debug("第{}行：成功解析美团格式数据 - 商品名称: {}, 类目ID: {}, SKU: {}", 
                rowNum, product.getProductName(), product.getCategoryId(), product.getSkuId());
            return product;
            
        } catch (Exception e) {
            log.error("第{}行：解析美团格式数据失败 - {}", rowNum, e.getMessage());
            throw e;
        }
    }
    
    /**
     * 从行中提取指定字段的值
     * 
     * @param row Excel行数据
     * @param fieldName 系统字段名
     * @param columnMapping 列映射
     * @return 字段值
     */
    private String extractField(Row row, String fieldName, Map<String, Integer> columnMapping) {
        Integer columnIndex = columnMapping.get(fieldName);
        if (columnIndex == null) {
            log.debug("字段 {} 在列映射中不存在", fieldName);
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        return getCellValueAsString(cell);
    }
    
    /**
     * 从指定列索引提取值（不依赖映射）
     */
    private String extractFieldByIndex(Row row, int columnIndex) {
        if (columnIndex < 0) {
            return null;
        }
        Cell cell = row.getCell(columnIndex);
        return getCellValueAsString(cell);
    }

    /**
     * 获取单元格的字符串值
     *
     * @param cell 单元格
     * @return 字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 避免科学计数法
                    double numericValue = cell.getNumericCellValue();
                    // 如果是整数，返回整数字符串
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                // 对于公式单元格，尝试获取计算后的值
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        double numericValue = cell.getNumericCellValue();
                        if (numericValue == (long) numericValue) {
                            return String.valueOf((long) numericValue);
                        } else {
                            return String.valueOf(numericValue);
                        }
                    } catch (Exception ex) {
                        return null;
                    }
                }
            case BLANK:
                return null;
            default:
                return null;
        }
    }
    
    /**
     * 解析价格字符串
     * 
     * @param priceStr 价格字符串
     * @return 价格（如果解析失败返回0）
     */
    private BigDecimal parsePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            log.warn("价格字符串为空，使用默认值0");
            return BigDecimal.ZERO;
        }
        
        try {
            return new BigDecimal(priceStr.trim());
        } catch (NumberFormatException e) {
            log.warn("无法解析价格：{}，使用默认值0", priceStr);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 解析库存字符串
     * 
     * @param stockStr 库存字符串
     * @return 库存
     */
    private Integer parseStock(String stockStr) {
        if (stockStr == null || stockStr.trim().isEmpty()) {
            return 0; // 默认为0
        }
        
        try {
            double stockValue = Double.parseDouble(stockStr.trim());
            return (int) stockValue;
        } catch (NumberFormatException e) {
            log.warn("无法解析库存：{}，使用默认值0", stockStr);
            return 0;
        }
    }
    
    /**
     * 解析整数字符串
     * 
     * @param str 整数字符串
     * @return 整数
     */
    private Integer parseInteger(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        
        try {
            double value = Double.parseDouble(str.trim());
            return (int) value;
        } catch (NumberFormatException e) {
            log.warn("无法解析整数：{}", str);
            return null;
        }
    }
    
    /**
     * 解析小数字符串
     * 
     * @param str 小数字符串
     * @return 小数
     */
    private BigDecimal parseDecimal(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        
        try {
            return new BigDecimal(str.trim());
        } catch (NumberFormatException e) {
            log.warn("无法解析小数：{}", str);
            return null;
        }
    }
    
    /**
     * 解析布尔值字符串
     * 
     * @param str 布尔值字符串
     * @return 布尔值（0或1）
     */
    private Integer parseBoolean(String str) {
        if (str == null || str.trim().isEmpty()) {
            return 0;
        }
        
        String value = str.trim().toLowerCase();
        if ("是".equals(value) || "true".equals(value) || "1".equals(value) || "yes".equals(value)) {
            return 1;
        }
        return 0;
    }
    
    /**
     * 解析日期字符串
     * 
     * @param str 日期字符串
     * @return 日期
     */
    private LocalDate parseDate(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 尝试多种日期格式
            String[] patterns = {
                "yyyy-MM-dd",
                "yyyy/MM/dd",
                "yyyy年MM月dd日",
                "yyyyMMdd"
            };
            
            for (String pattern : patterns) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                    return LocalDate.parse(str.trim(), formatter);
                } catch (Exception e) {
                    // 继续尝试下一个格式
                }
            }
            
            log.warn("无法解析日期：{}", str);
            return null;
        } catch (Exception e) {
            log.warn("无法解析日期：{}", str);
            return null;
        }
    }

    /**
     * 从类目属性中提取第一个品牌
     * 格式如："品牌：小宁电器。类型：茶杯消毒柜。能效等级：一级能效..."
     * 返回第一个品牌，如："小宁电器"
     */
    private String extractBrandFromAttributes(String productAttributes) {
        if (productAttributes == null || productAttributes.trim().isEmpty()) {
            return null;
        }

        try {
            // 使用正则表达式匹配第一个"品牌："或"品牌:"后面的内容（直到句号）
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("品牌[：:]([^。]+)");
            java.util.regex.Matcher matcher = pattern.matcher(productAttributes);

            // 只取第一个匹配
            if (matcher.find()) {
                return matcher.group(1).trim();
            }

        } catch (Exception e) {
            log.debug("从类目属性提取品牌失败: {}", e.getMessage());
        }

        return null;
    }
    
    /**
     * 解析无理由退货标签ID
     * 支持以下格式：
     * 1. 标签ID（如：1300030895）
     * 2. 标签名称（如：不支持7天无理由退货）
     * 3. 布尔值（0/1）
     *
     * 标签ID映射表：
     * 1300030895 - 不支持7天无理由退货
     * 1300030902 - 7天无理由退货（一次性包装破损不支持）
     * 1300030903 - 7天无理由退货（激活后不支持）
     * 1300030904 - 7天无理由退货（使用后不支持）
     * 1300030905 - 7天无理由退货（安装后不支持）
     * 1300030906 - 7天无理由退货（定制类不支持）
     * 1300030901 - 7天无理由退货
     *
     * @param value 输入值
     * @return 标签ID
     */
    private String parseNoReasonReturnTagId(String value) {
        if (value == null || value.trim().isEmpty()) {
            log.debug("无理由退货字段为空，使用默认值: 1300030895");
            return "1300030895"; // 默认：不支持7天无理由退货
        }

        String trimmedValue = value.trim();

        // 如果已经是标签ID格式（13开头的10位数字），直接返回
        if (trimmedValue.matches("^13\\d{8}$")) {
            log.debug("无理由退货字段已是标签ID格式: {}", trimmedValue);
            return trimmedValue;
        }

        log.debug("开始匹配无理由退货标签，输入值: [{}], 长度: {}", trimmedValue, trimmedValue.length());

        // 优先匹配更具体的标签（必须先匹配，否则会被通用标签覆盖）

        // 1300030902 - 7天无理由退货（一次性包装破损不支持）
        if (trimmedValue.contains("一次性包装破损") && trimmedValue.contains("不支持")) {
            log.info("匹配到: 7天无理由退货（一次性包装破损不支持）-> 1300030902");
            return "1300030902";
        }

        // 1300030903 - 7天无理由退货（激活后不支持）
        if (trimmedValue.contains("激活后") && trimmedValue.contains("不支持")) {
            log.info("匹配到: 7天无理由退货（激活后不支持）-> 1300030903");
            return "1300030903";
        }

        // 1300030904 - 7天无理由退货（使用后不支持）
        if (trimmedValue.contains("使用后") && trimmedValue.contains("不支持")) {
            log.info("匹配到: 7天无理由退货（使用后不支持）-> 1300030904");
            return "1300030904";
        }

        // 1300030905 - 7天无理由退货（安装后不支持）
        if (trimmedValue.contains("安装后") && trimmedValue.contains("不支持")) {
            log.info("匹配到: 7天无理由退货（安装后不支持）-> 1300030905");
            return "1300030905";
        }

        // 1300030906 - 7天无理由退货（定制类不支持）
        if (trimmedValue.contains("定制类") && trimmedValue.contains("不支持")) {
            log.info("匹配到: 7天无理由退货（定制类不支持）-> 1300030906");
            return "1300030906";
        }

        // 1300030901 - 7天无理由退货（通用）
        if (trimmedValue.contains("7天无理由退货") || trimmedValue.contains("七天无理由退货")) {
            log.info("匹配到: 7天无理由退货 -> 1300030901");
            return "1300030901";
        }

        // 1300030895 - 不支持7天无理由退货
        if (trimmedValue.equals("不支持7天无理由退货") ||
            trimmedValue.equals("0") ||
            trimmedValue.equalsIgnoreCase("false") ||
            trimmedValue.equals("否")) {
            log.info("匹配到: 不支持7天无理由退货 -> 1300030895");
            return "1300030895";
        }

        // 布尔值转换为通用标签
        if (trimmedValue.equals("1") ||
            trimmedValue.equalsIgnoreCase("true") ||
            trimmedValue.equals("是")) {
            log.info("匹配到: 布尔值true -> 1300030901");
            return "1300030901";
        }

        // 默认：不支持7天无理由退货
        log.warn("无理由退货字段未匹配到任何标签，使用默认值。输入值: [{}], 长度: {}", trimmedValue, trimmedValue.length());
        return "1300030895";
    }
}
