package com.meituan.product.service;

import com.meituan.product.enums.FormatType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel文件格式检测器
 * 用于识别文件是美团格式还是标准格式
 */
@Slf4j
@Component
public class FormatDetector {
    
    // 美团格式的必需列
    private static final String[] MEITUAN_REQUIRED_COLUMNS = {
        "商品类目ID",
        "商品名称",
        "价格"
    };
    
    // 标准格式的必需列
    private static final String[] STANDARD_REQUIRED_COLUMNS = {
        "商品名称*",
        "类目ID*",
        "价格(元)*"
    };
    
    // 美团格式的列映射（美团列名 -> 系统字段名）
    private static final Map<String, String> MEITUAN_COLUMN_MAPPING = new HashMap<>();
    
    static {
        // ============================================
        // 基础信息字段
        // ============================================
        MEITUAN_COLUMN_MAPPING.put("sku_id", "skuId");
        MEITUAN_COLUMN_MAPPING.put("SKU ID", "skuId");
        MEITUAN_COLUMN_MAPPING.put("SKUID", "skuId");
        
        MEITUAN_COLUMN_MAPPING.put("条形码(upc/ean等)", "upcEan");
        MEITUAN_COLUMN_MAPPING.put("条形码", "upcEan");
        MEITUAN_COLUMN_MAPPING.put("UPC/EAN", "upcEan");
        
        MEITUAN_COLUMN_MAPPING.put("商品类目名称", "categoryName");
        MEITUAN_COLUMN_MAPPING.put("类目名称", "categoryName");
        
        MEITUAN_COLUMN_MAPPING.put("商品类目ID", "categoryId");
        MEITUAN_COLUMN_MAPPING.put("类目ID", "categoryId");
        
        MEITUAN_COLUMN_MAPPING.put("app_spu_code", "appSpuCode");
        MEITUAN_COLUMN_MAPPING.put("APP SPU编码", "appSpuCode");
        MEITUAN_COLUMN_MAPPING.put("APPSPU编码", "appSpuCode");
        
        MEITUAN_COLUMN_MAPPING.put("商品名称", "productName");
        
        // ============================================
        // 图片视频字段
        // ============================================
        MEITUAN_COLUMN_MAPPING.put("商品图片", "productImage");
        MEITUAN_COLUMN_MAPPING.put("商品图片URL", "productImage");
        MEITUAN_COLUMN_MAPPING.put("图片", "imageUrl");
        MEITUAN_COLUMN_MAPPING.put("图片URL", "imageUrl");
        
        MEITUAN_COLUMN_MAPPING.put("封面视频", "coverVideo");
        MEITUAN_COLUMN_MAPPING.put("封面视频URL", "coverVideo");
        
        MEITUAN_COLUMN_MAPPING.put("规格图", "specImage");
        MEITUAN_COLUMN_MAPPING.put("规格图URL", "specImageUrl");
        
        // ============================================
        // 分类库存字段
        // ============================================
        MEITUAN_COLUMN_MAPPING.put("店内分类", "storeCategory");
        MEITUAN_COLUMN_MAPPING.put("所处店内分类数量", "storeCategoryCount");
        MEITUAN_COLUMN_MAPPING.put("规格名称", "specName");
        MEITUAN_COLUMN_MAPPING.put("店内码/货号", "storeCode");
        MEITUAN_COLUMN_MAPPING.put("店内码", "storeCode");
        MEITUAN_COLUMN_MAPPING.put("货号", "storeCode");
        
        MEITUAN_COLUMN_MAPPING.put("价格", "price");
        MEITUAN_COLUMN_MAPPING.put("库存", "stock");
        
        MEITUAN_COLUMN_MAPPING.put("售卖状态", "saleStatus");
        MEITUAN_COLUMN_MAPPING.put("月售", "monthlySales");
        MEITUAN_COLUMN_MAPPING.put("月售数量", "monthlySales");
        
        MEITUAN_COLUMN_MAPPING.put("重量", "weight");
        MEITUAN_COLUMN_MAPPING.put("重量单位", "weightUnit");
        MEITUAN_COLUMN_MAPPING.put("起购数", "minPurchase");
        MEITUAN_COLUMN_MAPPING.put("货架码/位置码", "shelfCode");
        MEITUAN_COLUMN_MAPPING.put("货架码", "shelfCode");
        MEITUAN_COLUMN_MAPPING.put("位置码", "shelfCode");
        
        // ============================================
        // 详情描述字段
        // ============================================
        MEITUAN_COLUMN_MAPPING.put("商品描述", "description");
        MEITUAN_COLUMN_MAPPING.put("描述", "description");
        
        MEITUAN_COLUMN_MAPPING.put("商品卖点", "sellingPoint");
        MEITUAN_COLUMN_MAPPING.put("卖点", "sellingPoint");
        MEITUAN_COLUMN_MAPPING.put("卖点展示期", "sellingPointPeriod");
        
        MEITUAN_COLUMN_MAPPING.put("文字详情", "textDetail");
        MEITUAN_COLUMN_MAPPING.put("图片详情", "imageDetail");
        MEITUAN_COLUMN_MAPPING.put("品牌商图片详情", "brandImageDetail");
        
        // ============================================
        // 日期相关字段
        // ============================================
        MEITUAN_COLUMN_MAPPING.put("生产日期", "productionDate");
        MEITUAN_COLUMN_MAPPING.put("到期日期", "expiryDate");
        MEITUAN_COLUMN_MAPPING.put("是否临期", "isNearExpiry");
        MEITUAN_COLUMN_MAPPING.put("是否过期", "isExpired");
        
        // ============================================
        // 配送时间字段
        // ============================================
        MEITUAN_COLUMN_MAPPING.put("发货模式", "deliveryMode");
        MEITUAN_COLUMN_MAPPING.put("预售的可配送时间", "presaleDeliveryTime");
        MEITUAN_COLUMN_MAPPING.put("预售配送时间", "presaleDeliveryTime");
        MEITUAN_COLUMN_MAPPING.put("可售时间", "availableTime");
        
        // ============================================
        // 商品属性字段
        // ============================================
        MEITUAN_COLUMN_MAPPING.put("商品属性", "productAttributes");
        MEITUAN_COLUMN_MAPPING.put("力荐", "isRecommended");
        MEITUAN_COLUMN_MAPPING.put("无理由退货", "noReasonReturn");
        MEITUAN_COLUMN_MAPPING.put("组合商品", "isCombo");
        MEITUAN_COLUMN_MAPPING.put("参与的组合商品", "comboProducts");
        MEITUAN_COLUMN_MAPPING.put("是否四轮配送", "isFourWheelDelivery");
        MEITUAN_COLUMN_MAPPING.put("四轮配送", "isFourWheelDelivery");
        
        // ============================================
        // 合规审核字段
        // ============================================
        MEITUAN_COLUMN_MAPPING.put("合规状态", "complianceStatus");
        MEITUAN_COLUMN_MAPPING.put("违规下架", "violationOffline");
        MEITUAN_COLUMN_MAPPING.put("必填信息缺失", "missingRequiredInfo");
        MEITUAN_COLUMN_MAPPING.put("审核状态", "auditStatus");
    }
    
    /**
     * 检测Excel文件格式
     * 
     * @param headers Excel文件的表头行
     * @return 格式类型
     */
    public FormatType detectFormat(List<String> headers) {
        if (headers == null || headers.isEmpty()) {
            log.warn("表头为空，无法识别格式");
            return FormatType.UNKNOWN;
        }
        
        // 打印所有表头，帮助调试
        log.info("检测到的表头列数：{}", headers.size());
        for (int i = 0; i < Math.min(headers.size(), 10); i++) {
            log.info("表头[{}]: '{}'", i, headers.get(i));
        }
        
        // 检查是否为美团格式
        if (isMeituanFormat(headers)) {
            log.info("识别为美团格式");
            return FormatType.MEITUAN;
        }
        
        // 检查是否为标准格式
        if (isStandardFormat(headers)) {
            log.info("识别为标准格式");
            return FormatType.STANDARD;
        }
        
        // 无法识别，默认尝试标准格式
        log.warn("无法识别文件格式，将尝试使用标准格式处理");
        log.warn("美团格式需要的列：{}", String.join(", ", MEITUAN_REQUIRED_COLUMNS));
        log.warn("标准格式需要的列：{}", String.join(", ", STANDARD_REQUIRED_COLUMNS));
        return FormatType.UNKNOWN;
    }
    
    /**
     * 检查是否为美团格式
     * 
     * @param headers 表头列表
     * @return 是否为美团格式
     */
    private boolean isMeituanFormat(List<String> headers) {
        // 检查是否包含所有必需列
        for (String requiredColumn : MEITUAN_REQUIRED_COLUMNS) {
            if (!containsColumn(headers, requiredColumn)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 检查是否为标准格式
     * 
     * @param headers 表头列表
     * @return 是否为标准格式
     */
    private boolean isStandardFormat(List<String> headers) {
        // 检查是否包含所有必需列
        for (String requiredColumn : STANDARD_REQUIRED_COLUMNS) {
            if (!containsColumn(headers, requiredColumn)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 检查表头是否包含指定列（宽松匹配：忽略前后空格）
     * 
     * @param headers 表头列表
     * @param columnName 列名
     * @return 是否包含
     */
    private boolean containsColumn(List<String> headers, String columnName) {
        String normalizedTarget = normalizeColumnName(columnName);
        for (String header : headers) {
            if (header != null) {
                String normalizedHeader = normalizeColumnName(header);
                if (normalizedHeader.equals(normalizedTarget)) {
                    log.debug("匹配到列：'{}' == '{}'", header, columnName);
                    return true;
                }
            }
        }
        log.debug("未找到列：'{}'", columnName);
        return false;
    }
    
    /**
     * 标准化列名（去除前后空格，转换为统一格式）
     * 
     * @param columnName 原始列名
     * @return 标准化后的列名
     */
    private String normalizeColumnName(String columnName) {
        if (columnName == null) {
            return "";
        }
        // 去除前后空格，并去除所有空白字符
        return columnName.trim().replaceAll("\\s+", "");
    }
    
    /**
     * 获取美团格式的列索引映射
     * 
     * @param headers Excel文件的表头行
     * @return 列名到索引的映射（系统字段名 -> 列索引）
     */
    public Map<String, Integer> getMeituanColumnMapping(List<String> headers) {
        Map<String, Integer> columnIndexMap = new HashMap<>();
        
        // 遍历表头，找到美团列名对应的索引
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if (header != null) {
                String normalizedHeader = normalizeColumnName(header);
                // 检查每个美团列名映射
                for (Map.Entry<String, String> entry : MEITUAN_COLUMN_MAPPING.entrySet()) {
                    String meituanColumnName = entry.getKey();
                    String systemFieldName = entry.getValue();
                    String normalizedMeituanColumn = normalizeColumnName(meituanColumnName);
                    
                    if (normalizedHeader.equals(normalizedMeituanColumn)) {
                        columnIndexMap.put(systemFieldName, i);
                        log.info("映射列：'{}' -> {} (索引: {})", header, systemFieldName, i);
                        break;
                    }
                }
            }
        }
        
        log.info("美团格式列映射完成，共映射{}个字段", columnIndexMap.size());
        return columnIndexMap;
    }
    
    /**
     * 获取缺失的必需列
     * 
     * @param headers 表头列表
     * @param formatType 格式类型
     * @return 缺失的列名列表
     */
    public List<String> getMissingRequiredColumns(List<String> headers, FormatType formatType) {
        String[] requiredColumns;
        
        if (formatType == FormatType.MEITUAN) {
            requiredColumns = MEITUAN_REQUIRED_COLUMNS;
        } else if (formatType == FormatType.STANDARD) {
            requiredColumns = STANDARD_REQUIRED_COLUMNS;
        } else {
            return List.of();
        }
        
        List<String> missingColumns = new java.util.ArrayList<>();
        for (String requiredColumn : requiredColumns) {
            if (!containsColumn(headers, requiredColumn)) {
                missingColumns.add(requiredColumn);
            }
        }
        
        return missingColumns;
    }
}
