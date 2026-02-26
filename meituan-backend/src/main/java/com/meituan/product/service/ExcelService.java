package com.meituan.product.service;

import com.meituan.product.config.TemplateConfig;
import com.meituan.product.dto.ErrorDetail;
import com.meituan.product.entity.Product;
import com.meituan.product.enums.FormatType;
import com.meituan.product.exception.DataValidationException;
import com.meituan.product.exception.FileFormatException;
import com.meituan.product.exception.TemplateFileException;
import com.meituan.product.exception.TemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel文件处理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelService {
    
    private final TemplateConfig templateConfig;
    private final FormatDetector formatDetector;
    private final MeituanFormatParser meituanFormatParser;
    private final com.meituan.product.mapper.TemplateMapper templateMapper;
    private final MinioService minioService;
    
    /**
     * 解析Excel文件
     * 
     * @param file Excel文件
     * @return 商品列表
     */
    public List<Product> parseExcel(MultipartFile file) {
        // 验证文件格式
        if (!isValidExcelFile(file)) {
            throw new FileFormatException("不支持的文件格式，仅支持xlsx和xls格式");
        }
        
        try (InputStream inputStream = file.getInputStream()) {
            String fileName = file.getOriginalFilename();
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            
            return parseExcel(inputStream, fileExtension);
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new FileFormatException("读取Excel文件失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 解析Excel输入流（支持格式自动识别）
     * 
     * @param inputStream 输入流
     * @param fileType 文件类型（xlsx或xls）
     * @return 商品列表
     */
    public List<Product> parseExcel(InputStream inputStream, String fileType) {
        try {
            Workbook workbook = createWorkbook(inputStream, fileType);
            Sheet sheet = workbook.getSheetAt(0);
            
            // 读取表头
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new FileFormatException("Excel文件表头为空");
            }
            
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                String headerValue = getCellValueAsString(cell);
                headers.add(headerValue);
            }
            
            // 检测文件格式
            FormatType formatType = formatDetector.detectFormat(headers);
            log.info("检测到文件格式：{}", formatType.getDescription());
            
            List<Product> products = new ArrayList<>();
            List<ErrorDetail> errorDetails = new ArrayList<>();
            
            // 根据格式类型选择解析方式
            if (formatType == FormatType.MEITUAN) {
                // 美团格式解析
                Map<String, Integer> columnMapping = formatDetector.getMeituanColumnMapping(headers);
                products = parseMeituanFormat(sheet, columnMapping, errorDetails);
            } else {
                // 标准格式解析（保持原有逻辑）
                products = parseStandardFormat(sheet, errorDetails);
            }
            
            workbook.close();
            
            // 如果有验证错误，抛出异常
            if (!errorDetails.isEmpty()) {
                List<DataValidationException.ValidationError> validationErrors = new ArrayList<>();
                for (ErrorDetail errorDetail : errorDetails) {
                    validationErrors.add(new DataValidationException.ValidationError(
                        errorDetail.getRowNum(),
                        errorDetail.getFieldName(),
                        errorDetail.getErrorMessage()
                    ));
                }
                throw new DataValidationException("Excel数据验证失败", validationErrors);
            }
            
            log.info("成功解析Excel文件，格式：{}，共{}条商品数据", formatType.getDescription(), products.size());
            return products;
            
        } catch (IOException e) {
            log.error("解析Excel文件失败", e);
            throw new FileFormatException("解析Excel文件失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 解析美团格式
     */
    private List<Product> parseMeituanFormat(Sheet sheet, Map<String, Integer> columnMapping, 
                                             List<ErrorDetail> errorDetails) {
        List<Product> products = new ArrayList<>();
        int rowCount = sheet.getPhysicalNumberOfRows();
        
        // 从第3行开始读取（第1行是表头，第2行是描述）
        for (int i = 2; i < rowCount; i++) {
            Row row = sheet.getRow(i);
            
            // 跳过空行
            if (isEmptyRow(row)) {
                continue;
            }
            
            try {
                Product product = meituanFormatParser.parseRow(row, columnMapping, i + 1);
                products.add(product);
            } catch (Exception e) {
                // 记录错误但继续处理其他行
                errorDetails.add(new ErrorDetail(i + 1, "整行", e.getMessage()));
                log.debug("第{}行解析失败：{}", i + 1, e.getMessage());
            }
        }
        
        return products;
    }
    
    /**
     * 解析标准格式（改进版 - 支持灵活列顺序）
     */
    private List<Product> parseStandardFormat(Sheet sheet, List<ErrorDetail> errorDetails) {
        List<Product> products = new ArrayList<>();
        int rowCount = sheet.getPhysicalNumberOfRows();
        
        // 读取表头，建立列映射
        Row headerRow = sheet.getRow(0);
        Map<String, Integer> columnMapping = buildStandardColumnMapping(headerRow);
        
        log.info("标准格式列映射：{}", columnMapping);
        
        // 从第2行开始读取（第1行是表头）
        for (int i = 1; i < rowCount; i++) {
            Row row = sheet.getRow(i);
            
            // 跳过空行
            if (isEmptyRow(row)) {
                continue;
            }
            
            try {
                Product product = parseStandardRow(row, columnMapping, i + 1);
                products.add(product);
            } catch (Exception e) {
                // 记录错误但继续处理其他行
                errorDetails.add(new ErrorDetail(i + 1, "整行", e.getMessage()));
                log.debug("第{}行解析失败：{}", i + 1, e.getMessage());
            }
        }
        
        return products;
    }
    
    /**
     * 建立标准格式的列映射
     * 
     * @param headerRow 表头行
     * @return 列映射（字段名 -> 列索引）
     */
    private Map<String, Integer> buildStandardColumnMapping(Row headerRow) {
        Map<String, Integer> mapping = new HashMap<>();
        
        if (headerRow == null) {
            // 如果没有表头，使用默认顺序
            mapping.put("productName", 0);
            mapping.put("categoryId", 1);
            mapping.put("price", 2);
            mapping.put("stock", 3);
            mapping.put("description", 4);
            mapping.put("imageUrl", 5);
            return mapping;
        }
        
        // 遍历表头，建立映射
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String header = cell.getStringCellValue().trim();
                String normalizedHeader = header.replaceAll("\\s+", "").toLowerCase();
                
                // 匹配商品名称
                if (normalizedHeader.contains("商品名称") || normalizedHeader.contains("名称") || 
                    normalizedHeader.contains("productname") || normalizedHeader.contains("product_name")) {
                    mapping.put("productName", i);
                    log.debug("映射：商品名称 -> 列{}", i);
                }
                // 匹配类目ID
                else if (normalizedHeader.contains("类目") || normalizedHeader.contains("分类") || 
                         normalizedHeader.contains("category") || normalizedHeader.contains("categoryid")) {
                    mapping.put("categoryId", i);
                    log.debug("映射：类目ID -> 列{}", i);
                }
                // 匹配价格
                else if (normalizedHeader.contains("价格") || normalizedHeader.contains("price")) {
                    mapping.put("price", i);
                    log.debug("映射：价格 -> 列{}", i);
                }
                // 匹配库存
                else if (normalizedHeader.contains("库存") || normalizedHeader.contains("stock")) {
                    mapping.put("stock", i);
                    log.debug("映射：库存 -> 列{}", i);
                }
                // 匹配描述
                else if (normalizedHeader.contains("描述") || normalizedHeader.contains("说明") || 
                         normalizedHeader.contains("description")) {
                    mapping.put("description", i);
                    log.debug("映射：描述 -> 列{}", i);
                }
                // 匹配图片URL
                else if (normalizedHeader.contains("图片") || normalizedHeader.contains("url") || 
                         normalizedHeader.contains("image")) {
                    mapping.put("imageUrl", i);
                    log.debug("映射：图片URL -> 列{}", i);
                }
            }
        }
        
        // 如果某些字段没有映射到，使用默认位置
        if (!mapping.containsKey("productName")) mapping.put("productName", 0);
        if (!mapping.containsKey("categoryId")) mapping.put("categoryId", 1);
        if (!mapping.containsKey("price")) mapping.put("price", 2);
        if (!mapping.containsKey("stock")) mapping.put("stock", 3);
        if (!mapping.containsKey("description")) mapping.put("description", 4);
        if (!mapping.containsKey("imageUrl")) mapping.put("imageUrl", 5);
        
        return mapping;
    }
    
    /**
     * 解析标准格式的单行数据（使用列映射）
     * 
     * @param row 行对象
     * @param columnMapping 列映射
     * @param rowNum 行号
     * @return 商品对象
     */
    private Product parseStandardRow(Row row, Map<String, Integer> columnMapping, int rowNum) {
        Product product = new Product();
        
        // 商品名称
        String productName = getCellValueAsString(row.getCell(columnMapping.get("productName")));
        product.setProductName(productName != null ? productName.trim() : "");
        
        // 类目ID
        String categoryId = getCellValueAsString(row.getCell(columnMapping.get("categoryId")));
        product.setCategoryId(categoryId != null ? categoryId.trim() : "");
        
        // 价格
        Double priceValue = getCellValueAsDouble(row.getCell(columnMapping.get("price")));
        product.setPrice(priceValue != null ? BigDecimal.valueOf(priceValue) : BigDecimal.ZERO);
        
        // 库存
        Double stockValue = getCellValueAsDouble(row.getCell(columnMapping.get("stock")));
        product.setStock(stockValue != null ? stockValue.intValue() : 0);
        
        // 商品描述
        String description = getCellValueAsString(row.getCell(columnMapping.get("description")));
        product.setDescription(description);
        
        // 商品图片URL
        String imageUrl = getCellValueAsString(row.getCell(columnMapping.get("imageUrl")));
        product.setImageUrl(imageUrl);
        
        // 设置默认状态为待上传
        product.setStatus(Product.Status.PENDING.getCode());
        
        return product;
    }
    
    /**
     * 解析单行数据（无验证版本 - 直接导入原始数据）
     * 
     * @param row 行对象
     * @param rowNum 行号
     * @return 商品对象
     */
    private Product parseRow(Row row, int rowNum) {
        Product product = new Product();
        
        // 商品名称（不验证）
        String productName = getCellValueAsString(row.getCell(0));
        product.setProductName(productName != null ? productName.trim() : "");
        
        // 类目ID（不验证格式）
        String categoryId = getCellValueAsString(row.getCell(1));
        product.setCategoryId(categoryId != null ? categoryId.trim() : "");
        
        // 价格（不验证范围）
        Double priceValue = getCellValueAsDouble(row.getCell(2));
        product.setPrice(priceValue != null ? BigDecimal.valueOf(priceValue) : BigDecimal.ZERO);
        
        // 库存（默认0）
        Double stockValue = getCellValueAsDouble(row.getCell(3));
        product.setStock(stockValue != null ? stockValue.intValue() : 0);
        
        // 商品描述（不限制长度）
        String description = getCellValueAsString(row.getCell(4));
        product.setDescription(description);
        
        // 商品图片URL（不限制长度）
        String imageUrl = getCellValueAsString(row.getCell(5));
        product.setImageUrl(imageUrl);
        
        // 设置默认状态为待上传
        product.setStatus(Product.Status.PENDING.getCode());
        
        return product;
    }
    
    /**
     * 创建Workbook对象
     */
    private Workbook createWorkbook(InputStream inputStream, String fileType) throws IOException {
        if ("xlsx".equalsIgnoreCase(fileType)) {
            return new XSSFWorkbook(inputStream);
        } else if ("xls".equalsIgnoreCase(fileType)) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new FileFormatException("不支持的文件类型：" + fileType);
        }
    }
    
    /**
     * 验证是否为有效的Excel文件
     */
    public boolean isValidExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return "xlsx".equals(extension) || "xls".equals(extension);
    }
    
    /**
     * 判断是否为空行
     */
    private boolean isEmptyRow(Row row) {
        if (row == null) {
            return true;
        }
        
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * 获取单元格的字符串值
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
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return null;
            default:
                return null;
        }
    }
    
    /**
     * 获取单元格的数值
     */
    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    return value.isEmpty() ? null : Double.parseDouble(value);
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 生成导入模板
     * 
     * @param sampleProducts 示例商品数据
     * @return Excel文件字节数组
     */
    public byte[] generateImportTemplate(List<Product> sampleProducts) {
        log.info("开始生成导入模板");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("商品导入");
            
            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // 1. 创建表头行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"商品名称*", "类目ID*", "价格(元)*", "库存", "商品描述", "图片URL"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                
                // 设置列宽
                if (i == 0 || i == 4) {
                    sheet.setColumnWidth(i, 30 * 256); // 商品名称和描述列宽
                } else if (i == 5) {
                    sheet.setColumnWidth(i, 40 * 256); // 图片URL列宽
                } else {
                    sheet.setColumnWidth(i, 15 * 256);
                }
            }
            
            // 2. 填充示例数据
            for (int i = 0; i < sampleProducts.size(); i++) {
                Row dataRow = sheet.createRow(i + 1);
                Product product = sampleProducts.get(i);
                
                // 商品名称
                Cell cell0 = dataRow.createCell(0);
                cell0.setCellValue(product.getProductName());
                cell0.setCellStyle(dataStyle);
                
                // 类目ID
                Cell cell1 = dataRow.createCell(1);
                cell1.setCellValue(product.getCategoryId());
                cell1.setCellStyle(dataStyle);
                
                // 价格
                Cell cell2 = dataRow.createCell(2);
                if (product.getPrice() != null) {
                    cell2.setCellValue(product.getPrice().doubleValue());
                }
                cell2.setCellStyle(dataStyle);
                
                // 库存
                Cell cell3 = dataRow.createCell(3);
                if (product.getStock() != null) {
                    cell3.setCellValue(product.getStock());
                }
                cell3.setCellStyle(dataStyle);
                
                // 商品描述
                Cell cell4 = dataRow.createCell(4);
                if (product.getDescription() != null) {
                    cell4.setCellValue(product.getDescription());
                }
                cell4.setCellStyle(dataStyle);
                
                // 图片URL
                Cell cell5 = dataRow.createCell(5);
                if (product.getImageUrl() != null) {
                    cell5.setCellValue(product.getImageUrl());
                }
                cell5.setCellStyle(dataStyle);
            }
            
            // 3. 添加说明行
            Row noteRow = sheet.createRow(sampleProducts.size() + 2);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("说明：带*号的字段为必填项，类目ID必须是10位数字");
            
            CellStyle noteStyle = workbook.createCellStyle();
            Font noteFont = workbook.createFont();
            noteFont.setColor(IndexedColors.RED.getIndex());
            noteFont.setItalic(true);
            noteStyle.setFont(noteFont);
            noteCell.setCellStyle(noteStyle);
            
            // 合并说明单元格
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                sampleProducts.size() + 2, sampleProducts.size() + 2, 0, 5));
            
            // 4. 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            log.info("成功生成导入模板");
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("生成模板失败", e);
            throw new FileFormatException("生成模板失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 生成美团上传模板
     * 
     * @param products 商品列表
     * @return Excel文件字节数组
     */
    public byte[] generateMeituanTemplate(List<Product> products) {
        log.info("开始生成美团上传模板，商品数量：{}", products.size());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(templateConfig.getTemplateName());
            
            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // 创建数据样式
            CellStyle dataStyle = createDataStyle(workbook);
            
            // 1. 创建表头行
            Row headerRow = sheet.createRow(0);
            List<TemplateConfig.ColumnConfig> columns = templateConfig.getColumns();
            
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i).getHeaderName());
                cell.setCellStyle(headerStyle);
                
                // 设置列宽
                sheet.setColumnWidth(i, 20 * 256);
            }
            
            // 2. 填充商品数据
            for (int i = 0; i < products.size(); i++) {
                Row dataRow = sheet.createRow(i + 1);
                Product product = products.get(i);
                
                for (int j = 0; j < columns.size(); j++) {
                    Cell cell = dataRow.createCell(j);
                    cell.setCellStyle(dataStyle);
                    
                    String field = columns.get(j).getField();
                    setCellValue(cell, product, field);
                }
            }
            
            // 3. 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            log.info("成功生成美团上传模板");
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("生成模板失败", e);
            throw new FileFormatException("生成模板失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        
        return style;
    }
    
    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
    
    /**
     * 设置单元格值
     */
    private void setCellValue(Cell cell, Product product, String field) {
        switch (field) {
            case "productName":
                cell.setCellValue(product.getProductName());
                break;
            case "categoryId":
                cell.setCellValue(product.getCategoryId());
                break;
            case "price":
                if (product.getPrice() != null) {
                    cell.setCellValue(product.getPrice().doubleValue());
                }
                break;
            case "stock":
                if (product.getStock() != null) {
                    cell.setCellValue(product.getStock());
                }
                break;
            case "description":
                if (product.getDescription() != null) {
                    cell.setCellValue(product.getDescription());
                }
                break;
            case "imageUrl":
                if (product.getImageUrl() != null) {
                    cell.setCellValue(product.getImageUrl());
                }
                break;
            default:
                break;
        }
    }
    
    /**
     * 使用用户上传的模板生成美团上传文件
     * 
     * @param products 商品列表
     * @param merchantId 商家ID
     * @return Excel文件字节数组
     */
    public byte[] generateMeituanTemplateFromUserTemplate(List<Product> products, Long merchantId) {
        log.info("使用用户模板生成美团上传文件，商家ID：{}，商品数量：{}", merchantId, products.size());
        
        // 1. 查找商家的美团模板
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.meituan.product.entity.Template> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(com.meituan.product.entity.Template::getMerchantId, merchantId)
                   .eq(com.meituan.product.entity.Template::getTemplateType, "MEITUAN")
                   .orderByDesc(com.meituan.product.entity.Template::getCreatedTime)
                   .last("LIMIT 1");
        
        com.meituan.product.entity.Template template = templateMapper.selectOne(queryWrapper);
        
        // 2. 如果没有找到用户模板，抛出异常（移除默认模板回退）
        if (template == null) {
            log.error("未找到商家的美团模板，商家ID：{}", merchantId);
            throw new TemplateNotFoundException(merchantId);
        }
        
        log.info("找到用户模板：{}，模板ID：{}", template.getTemplateName(), template.getId());
        
        // 3. 从MinIO下载模板文件
        String objectName = getObjectNameFromTemplate(template);
        InputStream templateStream;
        try {
            templateStream = minioService.downloadFile(objectName);
        } catch (Exception e) {
            log.error("模板文件下载失败，商家ID：{}，文件路径：{}，错误：{}", 
                    merchantId, objectName, e.getMessage(), e);
            throw new TemplateFileException("模板文件丢失，请重新上传");
        }
        
        // 4. 读取模板文件
        Workbook templateWorkbook;
        try {
            templateWorkbook = new XSSFWorkbook(templateStream);
        } catch (Exception e) {
            log.error("模板文件读取失败，商家ID：{}，文件大小：{}，错误：{}", 
                    merchantId, template.getFileSize(), e.getMessage(), e);
            try {
                templateStream.close();
            } catch (IOException ignored) {
            }
            throw new TemplateFileException("模板文件损坏，请重新上传");
        }
        
        try {
            Sheet templateSheet = templateWorkbook.getSheetAt(0);
            
            // 5. 美团模板前7行是说明和示例，第1行是表头，从第8行开始是数据
            // 获取表头行（第1行，索引0）
            Row headerRow = templateSheet.getRow(0);
            if (headerRow == null) {
                log.error("模板文件没有表头，商家ID：{}", merchantId);
                templateWorkbook.close();
                templateStream.close();
                throw new TemplateFileException("模板文件格式错误：缺少表头");
            }
            
            // 6. 读取表头列名
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    headers.add(cell.getStringCellValue());
                } else {
                    headers.add("");
                }
            }
            
            log.info("模板表头：{}", headers);
            
            // 7. 删除模板中第8行之后的所有旧数据行（如果有的话）
            int lastRowNum = templateSheet.getLastRowNum();
            for (int i = lastRowNum; i >= 7; i--) {
                Row row = templateSheet.getRow(i);
                if (row != null) {
                    templateSheet.removeRow(row);
                }
            }
            
            // 8. 从第8行开始填充商品数据（索引7）
            int startRowIndex = 7;
            for (int i = 0; i < products.size(); i++) {
                Row dataRow = templateSheet.createRow(startRowIndex + i);
                Product product = products.get(i);
                
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = dataRow.createCell(j);
                    String header = headers.get(j);
                    
                    // 根据表头名称填充对应的商品数据
                    fillCellByHeaderName(cell, product, header);
                }
            }
            
            // 9. 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            templateWorkbook.write(outputStream);
            
            // 10. 关闭资源
            templateWorkbook.close();
            templateStream.close();
            
            log.info("成功使用用户模板生成文件，保留前7行模板说明，从第8行开始填充{}条商品数据", products.size());
            return outputStream.toByteArray();
            
        } catch (TemplateFileException e) {
            // 重新抛出模板文件异常
            throw e;
        } catch (Exception e) {
            log.error("使用用户模板生成文件失败，商家ID：{}", merchantId, e);
            try {
                templateWorkbook.close();
                templateStream.close();
            } catch (Exception ignored) {
            }
            throw new TemplateFileException("模板文件处理失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据表头名称填充单元格数据
     * 
     * @param cell 单元格
     * @param product 商品对象
     * @param headerName 表头名称
     */
    private void fillCellByHeaderName(Cell cell, Product product, String headerName) {
        if (headerName == null || headerName.trim().isEmpty()) {
            return;
        }
        
        String header = headerName.trim();
        
        // 基础信息
        if (header.contains("SKU") || header.contains("sku")) {
            setStringValue(cell, product.getSkuId());
        } else if (header.contains("条形码") || header.contains("UPC") || header.contains("EAN")) {
            setStringValue(cell, product.getUpcEan());
        } else if (header.contains("商品类目名称") || header.contains("类目名称")) {
            setStringValue(cell, product.getCategoryName());
        } else if (header.contains("商品类目ID") || header.contains("类目ID") || header.contains("类目") || header.contains("分类") || header.equalsIgnoreCase("category_id") || header.equalsIgnoreCase("categoryId")) {
            setStringValue(cell, product.getCategoryId());
        } else if (header.contains("APP") && header.contains("SPU")) {
            setStringValue(cell, product.getAppSpuCode());
        } else if (header.contains("商品名称") || (header.contains("名称") && !header.contains("类目") && !header.contains("规格")) || header.equalsIgnoreCase("product_name") || header.equalsIgnoreCase("productName")) {
            setStringValue(cell, product.getProductName());
        }
        // 图片视频
        else if (header.contains("商品图片") || (header.contains("图片") && !header.contains("规格"))) {
            setStringValue(cell, product.getProductImage());
        } else if (header.contains("封面视频")) {
            setStringValue(cell, product.getCoverVideo());
        } else if (header.contains("规格图")) {
            setStringValue(cell, product.getSpecImage());
        }
        // 分类库存
        else if (header.contains("店内分类") && !header.contains("数量")) {
            setStringValue(cell, product.getStoreCategory());
        } else if (header.contains("店内分类数量")) {
            setIntegerValue(cell, product.getStoreCategoryCount());
        } else if (header.contains("规格名称")) {
            setStringValue(cell, product.getSpecName());
        } else if (header.contains("店内码") || header.contains("货号")) {
            setStringValue(cell, product.getStoreCode());
        } else if (header.contains("价格") || header.equalsIgnoreCase("price")) {
            setBigDecimalValue(cell, product.getPrice());
        } else if (header.contains("库存") || header.equalsIgnoreCase("stock")) {
            setIntegerValue(cell, product.getStock());
        } else if (header.contains("售卖状态") || header.contains("销售状态")) {
            setStringValue(cell, product.getSaleStatus());
        } else if (header.contains("月售")) {
            setIntegerValue(cell, product.getMonthlySales());
        } else if (header.contains("重量") && !header.contains("单位")) {
            setBigDecimalValue(cell, product.getWeight());
        } else if (header.contains("重量单位")) {
            setStringValue(cell, product.getWeightUnit());
        } else if (header.contains("起购数")) {
            setIntegerValue(cell, product.getMinPurchase());
        } else if (header.contains("货架码") || header.contains("位置码")) {
            setStringValue(cell, product.getShelfCode());
        }
        // 详情描述
        else if (header.contains("商品卖点") || header.contains("卖点") && !header.contains("展示期")) {
            setStringValue(cell, product.getSellingPoint());
        } else if (header.contains("卖点展示期")) {
            setStringValue(cell, product.getSellingPointPeriod());
        } else if (header.contains("文字详情") || (header.contains("详情") && !header.contains("图片"))) {
            setStringValue(cell, product.getTextDetail());
        }
        // 日期相关
        else if (header.contains("生产日期")) {
            setLocalDateValue(cell, product.getProductionDate());
        } else if (header.contains("到期日期")) {
            setLocalDateValue(cell, product.getExpiryDate());
        } else if (header.contains("是否临期")) {
            setIntegerBooleanValue(cell, product.getIsNearExpiry());
        } else if (header.contains("是否过期")) {
            setIntegerBooleanValue(cell, product.getIsExpired());
        }
        // 配送时间
        else if (header.contains("发货模式")) {
            setStringValue(cell, product.getDeliveryMode());
        } else if (header.contains("预售配送时间")) {
            setStringValue(cell, product.getPresaleDeliveryTime());
        } else if (header.contains("可售时间")) {
            setStringValue(cell, product.getAvailableTime());
        }
        // 商品属性
        else if (header.contains("力荐")) {
            setIntegerBooleanValue(cell, product.getIsRecommended());
        } else if (header.contains("无理由退货")) {
            setIntegerBooleanValue(cell, product.getNoReasonReturn());
        } else if (header.contains("组合商品")) {
            setIntegerBooleanValue(cell, product.getIsCombo());
        } else if (header.contains("四轮配送")) {
            setIntegerBooleanValue(cell, product.getIsFourWheelDelivery());
        }
        // 合规审核
        else if (header.contains("合规状态")) {
            setStringValue(cell, product.getComplianceStatus());
        } else if (header.contains("违规下架")) {
            setIntegerBooleanValue(cell, product.getViolationOffline());
        } else if (header.contains("必填信息缺失")) {
            setIntegerBooleanValue(cell, product.getMissingRequiredInfo());
        } else if (header.contains("审核状态")) {
            setStringValue(cell, product.getAuditStatus());
        }
        // 兼容旧字段
        else if (header.contains("描述") || header.contains("说明") || header.equalsIgnoreCase("description")) {
            setStringValue(cell, product.getDescription());
        } else if (header.contains("URL") || header.equalsIgnoreCase("image_url") || header.equalsIgnoreCase("imageUrl")) {
            setStringValue(cell, product.getImageUrl());
        } else {
            // 未匹配的列，留空
            cell.setCellValue("");
        }
    }
    
    /**
     * 设置字符串值
     */
    private void setStringValue(Cell cell, String value) {
        if (value != null && !value.isEmpty()) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
        }
    }
    
    /**
     * 设置整数值
     */
    private void setIntegerValue(Cell cell, Integer value) {
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
        }
    }
    
    /**
     * 设置BigDecimal值
     */
    private void setBigDecimalValue(Cell cell, BigDecimal value) {
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue("");
        }
    }
    
    /**
     * 设置布尔值
     */
    private void setBooleanValue(Cell cell, Boolean value) {
        if (value != null) {
            cell.setCellValue(value ? "是" : "否");
        } else {
            cell.setCellValue("");
        }
    }
    
    /**
     * 设置LocalDate值
     */
    private void setLocalDateValue(Cell cell, java.time.LocalDate value) {
        if (value != null) {
            cell.setCellValue(value.toString());
        } else {
            cell.setCellValue("");
        }
    }
    
    /**
     * 设置Integer类型的布尔值（0-否，1-是）
     */
    private void setIntegerBooleanValue(Cell cell, Integer value) {
        if (value != null) {
            cell.setCellValue(value == 1 ? "是" : "否");
        } else {
            cell.setCellValue("");
        }
    }
    
    /**
     * 从模板对象获取MinIO对象名称
     * 
     * @param template 模板对象
     * @return MinIO对象名称
     */
    private String getObjectNameFromTemplate(com.meituan.product.entity.Template template) {
        String filePath = template.getFilePath();
        String fileUrl = template.getFileUrl();
        
        // 如果 filePath 不为空且不是完整URL，直接使用
        if (filePath != null && !filePath.isEmpty() && !filePath.startsWith("http")) {
            return filePath;
        }
        
        // 如果 filePath 为空或是完整URL，尝试从 fileUrl 提取
        if (fileUrl != null && !fileUrl.isEmpty()) {
            return extractFilePathFromUrl(fileUrl);
        }
        
        throw new IllegalStateException("模板文件路径为空，模板ID：" + template.getId());
    }
    
    /**
     * 从URL中提取文件路径
     * 
     * @param url MinIO预签名URL
     * @return 文件路径
     */
    private String extractFilePathFromUrl(String url) {
        try {
            String urlWithoutParams = url.split("\\?")[0];
            String[] parts = urlWithoutParams.split("/");
            StringBuilder path = new StringBuilder();
            boolean foundBucket = false;
            
            for (String part : parts) {
                if (foundBucket && !part.isEmpty()) {
                    if (path.length() > 0) {
                        path.append("/");
                    }
                    path.append(part);
                }
                if (part.equals("meituan-files")) {
                    foundBucket = true;
                }
            }
            
            String extractedPath = path.toString();
            
            if (extractedPath.isEmpty() && parts.length >= 2) {
                extractedPath = parts[parts.length - 2] + "/" + parts[parts.length - 1];
            }
            
            if (extractedPath.isEmpty()) {
                throw new IllegalStateException("无法从URL提取有效的文件路径：" + url);
            }
            
            return extractedPath;
            
        } catch (Exception e) {
            throw new RuntimeException("无法提取文件路径：" + e.getMessage(), e);
        }
    }
}

