# 批量导入预览 Bug 修复说明

## 问题描述

**严重 Bug**: 用户在批量导入页面点击"预览数据"按钮时，商品数量会增加，说明预览操作错误地将数据写入了数据库。

## 问题原因

### 根本原因

预览接口 `/products/preview` 错误地调用了 `productService.importFromExcel()` 方法，该方法会执行以下操作：

1. 解析 Excel 文件
2. 设置商家 ID
3. **批量插入数据库** ← 这是问题所在！

```java
// 错误的实现
@PostMapping("/preview")
public ApiResponse<Map<String, Object>> previewProducts(...) {
    // ❌ 这会将数据插入数据库！
    ImportResult result = productService.importFromExcel(file, merchantId);
    ...
}
```

### 影响

- 每次点击"预览"都会导入一次数据
- 如果用户多次预览同一个文件，会产生大量重复数据
- 用户点击"预览" → 点击"确认导入"，数据会被导入两次

## 修复方案

### 1. 创建专门的预览方法

在 `ProductService` 中新增 `previewExcel()` 方法，只解析文件，不插入数据库：

```java
/**
 * 预览Excel文件（只解析不导入）
 * 
 * @param file Excel文件
 * @param merchantId 商家ID
 * @return 预览结果
 */
public ImportResult previewExcel(MultipartFile file, Long merchantId) {
    log.info("开始预览Excel文件，商家ID：{}，文件名：{}", merchantId, file.getOriginalFilename());
    
    try {
        // 只解析Excel文件，不插入数据库
        List<Product> products = excelService.parseExcel(file);
        log.info("成功解析{}条商品数据（预览模式，未导入数据库）", products.size());
        
        // 设置商家ID（仅用于预览显示）
        products.forEach(product -> product.setMerchantId(merchantId));
        
        return ImportResult.success(products);
        
    } catch (DataValidationException e) {
        log.error("数据验证失败", e);
        return ImportResult.failure(e.getErrors());
    } catch (Exception e) {
        log.error("预览失败", e);
        throw e;
    }
}
```

### 2. 更新预览接口

修改 `ProductController` 的预览接口，调用新的 `previewExcel()` 方法：

```java
@PostMapping("/preview")
public ApiResponse<Map<String, Object>> previewProducts(...) {
    try {
        // ✅ 只预览，不导入数据库
        ImportResult result = productService.previewExcel(file, merchantId);
        ...
    }
}
```

### 3. 保持导入接口不变

导入接口 `/products/import` 继续使用 `importFromExcel()` 方法，正常导入数据：

```java
@PostMapping("/import")
public ApiResponse<ImportResult> importProducts(...) {
    // ✅ 真正导入数据到数据库
    ImportResult result = productService.importFromExcel(file, merchantId);
    ...
}
```

## 修复对比

### 修复前的流程

```
用户操作流程：
1. 选择文件
2. 点击"预览" → ❌ 数据被导入数据库（第1次）
3. 查看预览数据
4. 点击"确认导入" → ❌ 数据再次被导入（第2次）

结果：数据重复导入2次！
```

### 修复后的流程

```
用户操作流程：
1. 选择文件
2. 点击"预览" → ✅ 只解析文件，不导入数据库
3. 查看预览数据
4. 点击"确认导入" → ✅ 数据被导入数据库（仅1次）

结果：数据只导入1次，正确！
```

## 技术细节

### 方法对比

| 方法 | 用途 | 是否插入数据库 | 事务控制 |
|------|------|---------------|---------|
| `previewExcel()` | 预览 | ❌ 否 | 无需事务 |
| `importFromExcel()` | 导入 | ✅ 是 | `@Transactional` |

### 日志区分

修复后，日志会明确标识操作类型：

```
预览操作：
[INFO] 开始预览Excel文件，商家ID：1，文件名：products.xlsx
[INFO] 成功解析3750条商品数据（预览模式，未导入数据库）

导入操作：
[INFO] 开始导入Excel文件，商家ID：1，文件名：products.xlsx
[INFO] 成功解析3750条商品数据
[INFO] 成功导入3750条商品到数据库
```

## 验证修复

### 测试步骤

1. **清空现有数据**
   ```sql
   DELETE FROM products WHERE merchant_id = 1;
   ```

2. **测试预览功能**
   - 选择一个包含 100 条数据的文件
   - 点击"预览数据"
   - 检查数据库：`SELECT COUNT(*) FROM products WHERE merchant_id = 1;`
   - **预期结果**: 应该是 0 条（预览不应插入数据）

3. **测试导入功能**
   - 继续点击"确认导入"
   - 再次检查数据库：`SELECT COUNT(*) FROM products WHERE merchant_id = 1;`
   - **预期结果**: 应该是 100 条（导入成功）

4. **测试重复预览**
   - 清空数据
   - 选择同一个文件
   - 连续点击"预览"3次
   - 检查数据库
   - **预期结果**: 应该是 0 条（多次预览不应插入数据）

### 验证清单

- [ ] 预览操作不会增加商品数量
- [ ] 预览后点击导入，数据只导入一次
- [ ] 多次预览同一文件不会产生重复数据
- [ ] 导入功能正常工作
- [ ] 日志正确标识预览和导入操作

## 相关文件

- `meituan-backend/src/main/java/com/meituan/product/service/ProductService.java` - 新增 `previewExcel()` 方法
- `meituan-backend/src/main/java/com/meituan/product/controller/ProductController.java` - 更新预览接口

## 清理重复数据

如果之前因为这个 bug 产生了重复数据，使用以下 SQL 清理：

```sql
-- 删除重复数据（保留最早的记录）
DELETE p1 FROM products p1
INNER JOIN products p2 ON 
    p1.product_name = p2.product_name 
    AND p1.category_id = p2.category_id 
    AND p1.sku_id = p2.sku_id
    AND p1.merchant_id = p2.merchant_id
    AND p1.id > p2.id
WHERE p1.merchant_id = 1;
```

或者使用前端的"清空商品"功能重新开始。

## 总结

这是一个严重的 bug，会导致：
- ❌ 预览时错误地插入数据
- ❌ 数据重复导入
- ❌ 用户困惑（为什么预览会增加数据？）

修复后：
- ✅ 预览只读取文件，不写入数据库
- ✅ 导入才会写入数据库
- ✅ 符合用户预期的行为
- ✅ 避免数据重复

**建议**: 立即部署此修复，并清理现有的重复数据。
