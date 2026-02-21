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
            // 商品属性字段
            // ============================================
            product.setProductAttributes(extractField(row, "productAttributes", columnMapping));
            product.setIsRecommended(parseBoolean(extractField(row, "isRecommended", columnMapping)));
            product.setNoReasonReturn(parseBoolean(extractField(row, "noReasonReturn", columnMapping)));
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
}
