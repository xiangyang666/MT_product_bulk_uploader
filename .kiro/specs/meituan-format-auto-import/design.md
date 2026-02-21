# 设计文档 - 美团格式自动识别导入

## 概述

本功能实现智能识别美团后台导出的商品数据格式（50+列），自动提取所需字段并导入系统，无需用户手动转换数据格式。系统将保持对现有标准格式的兼容性，同时提供更好的用户体验。

## 架构

### 整体架构

```
┌─────────────────┐
│   前端上传      │
│  (Import.vue)   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Controller     │
│ (预览/导入API)  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ ProductService  │
│  (业务逻辑)     │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────┐
│      ExcelService               │
│  ┌──────────────────────────┐  │
│  │  FormatDetector          │  │
│  │  (格式识别器)            │  │
│  └──────────┬───────────────┘  │
│             │                   │
│    ┌────────┴────────┐         │
│    │                 │         │
│    ▼                 ▼         │
│ ┌──────┐      ┌──────────┐    │
│ │标准格式│      │美团格式  │    │
│ │解析器 │      │解析器    │    │
│ └──────┘      └──────────┘    │
│                                 │
│  ┌──────────────────────────┐  │
│  │  DataValidator           │  │
│  │  (数据验证器)            │  │
│  └──────────────────────────┘  │
└─────────────────────────────────┘
```

### 处理流程

```
上传文件
   │
   ▼
读取Excel → 格式识别
   │           │
   │           ├─→ 美团格式 → 字段映射 → 数据提取
   │           │                          │
   │           └─→ 标准格式 → 直接读取   │
   │                                      │
   └──────────────────────────────────────┘
                     │
                     ▼
                 数据验证
                     │
                     ├─→ 验证通过 → 添加到结果列表
                     │
                     └─→ 验证失败 → 记录错误信息
                                      │
                                      ▼
                              返回结果（成功+失败）
```

## 组件和接口

### 1. FormatDetector（格式识别器）

**职责**：检测Excel文件是否为美团格式

**接口**：
```java
public class FormatDetector {
    /**
     * 检测文件格式
     * @param headers Excel文件的表头行
     * @return 格式类型（MEITUAN / STANDARD / UNKNOWN）
     */
    public FormatType detectFormat(List<String> headers);
    
    /**
     * 获取美团格式的列索引映射
     * @param headers Excel文件的表头行
     * @return 列名到索引的映射
     */
    public Map<String, Integer> getMeituanColumnMapping(List<String> headers);
}
```

**识别规则**：
- 如果表头包含"商品类目ID"、"商品名称"、"价格"这三列，识别为美团格式
- 如果表头包含"商品名称*"、"类目ID*"、"价格(元)*"，识别为标准格式
- 其他情况识别为未知格式，尝试标准格式处理

### 2. MeituanFormatParser（美团格式解析器）

**职责**：解析美团格式的Excel文件

**接口**：
```java
public class MeituanFormatParser {
    /**
     * 解析美团格式的Excel行
     * @param row Excel行数据
     * @param columnMapping 列映射
     * @param rowNum 行号
     * @return 商品对象
     */
    public Product parseRow(Row row, Map<String, Integer> columnMapping, int rowNum);
    
    /**
     * 提取字段值
     * @param row Excel行数据
     * @param columnName 列名
     * @param columnMapping 列映射
     * @return 字段值
     */
    private String extractField(Row row, String columnName, Map<String, Integer> columnMapping);
}
```

**字段映射**：
```
美团格式列名          →  系统字段
─────────────────────────────────
商品名称             →  productName
商品类目ID           →  categoryId
价格                 →  price
库存                 →  stock
文字详情             →  description
（图片URL暂不支持）  →  imageUrl (null)
```

### 3. DataValidator（数据验证器）

**职责**：验证提取的数据是否符合系统要求

**接口**：
```java
public class DataValidator {
    /**
     * 验证商品数据
     * @param product 商品对象
     * @param rowNum 行号
     * @return 验证结果（包含错误列表）
     */
    public ValidationResult validate(Product product, int rowNum);
    
    /**
     * 验证类目ID
     * @param categoryId 类目ID
     * @return 是否有效
     */
    private boolean isValidCategoryId(String categoryId);
    
    /**
     * 验证价格
     * @param price 价格
     * @return 是否有效
     */
    private boolean isValidPrice(BigDecimal price);
    
    /**
     * 验证商品名称
     * @param productName 商品名称
     * @return 是否有效
     */
    private boolean isValidProductName(String productName);
}
```

### 4. ImportResult（导入结果）

**数据结构**：
```java
public class ImportResult {
    private int totalRows;           // 总行数
    private int successCount;        // 成功行数
    private int failedCount;         // 失败行数
    private List<Product> products;  // 成功的商品列表
    private List<ErrorDetail> errors; // 错误详情列表
    private String formatType;       // 识别的格式类型
    private long duration;           // 处理耗时（毫秒）
}

public class ErrorDetail {
    private int rowNum;              // 行号
    private String fieldName;        // 字段名
    private String errorMessage;     // 错误信息
    private String originalValue;    // 原始值
}
```

## 数据模型

### Product（商品实体）

保持现有的Product实体不变，新增一个字段用于标记数据来源：

```java
public class Product {
    private Long id;
    private String productName;      // 商品名称
    private String categoryId;       // 类目ID（10位数字）
    private BigDecimal price;        // 价格
    private Integer stock;           // 库存
    private String description;      // 商品描述
    private String imageUrl;         // 图片URL
    private Integer status;          // 状态
    private Long merchantId;         // 商家ID
    private String dataSource;       // 数据来源（MEITUAN / STANDARD）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

## 正确性属性

*属性是一个特征或行为，应该在系统的所有有效执行中保持为真——本质上是关于系统应该做什么的正式陈述。属性作为人类可读规范和机器可验证正确性保证之间的桥梁。*

### 属性 1：美团格式识别准确性
*对于任何* 包含"商品类目ID"、"商品名称"、"价格"列的Excel文件，系统应该识别为美团格式
**验证：需求 1.1**

### 属性 2：字段提取完整性
*对于任何* 被识别为美团格式的文件，提取的数据应该包含所有映射的字段（商品名称、类目ID、价格、库存、描述）
**验证：需求 1.2**

### 属性 3：格式回退正确性
*对于任何* 不符合美团格式的文件，系统应该尝试使用标准格式处理
**验证：需求 1.3, 5.3**

### 属性 4：类目ID验证一致性
*对于任何* 提取的类目ID，如果不是10位数字，系统应该记录错误并继续处理其他行
**验证：需求 2.1, 2.2**

### 属性 5：价格验证边界正确性
*对于任何* 提取的价格，如果不在(0, 99999.99]范围内，系统应该记录错误
**验证：需求 2.3, 2.4**

### 属性 6：商品名称验证完整性
*对于任何* 提取的商品名称，如果为空或长度超过255字符，系统应该记录错误
**验证：需求 2.5, 2.6**

### 属性 7：库存转换默认值正确性
*对于任何* 库存字段，如果为空或无效，系统应该将其设置为0
**验证：需求 2.7**

### 属性 8：描述截取长度正确性
*对于任何* 商品描述，如果长度超过1000字符，系统应该截取前1000个字符
**验证：需求 2.8**

### 属性 9：错误记录完整性
*对于任何* 验证失败的行，错误记录应该包含行号、字段名和错误原因
**验证：需求 3.1**

### 属性 10：统计信息一致性
*对于任何* 导入操作，总行数应该等于成功行数加失败行数
**验证：需求 3.2**

### 属性 11：错误详情数量限制
*对于任何* 导入结果，如果错误超过100条，应该只返回前100条详情并提示剩余错误数量
**验证：需求 3.3, 3.4**

### 属性 12：预览数据结构正确性
*对于任何* 美团格式文件的预览，返回的数据应该包含6个字段（商品名称、类目ID、价格、库存、描述、图片URL）
**验证：需求 4.2**

### 属性 13：预览数量限制正确性
*对于任何* 预览操作，返回的数据行数应该不超过20行
**验证：需求 4.3**

### 属性 14：预览统计准确性
*对于任何* 预览操作，预计成功行数加错误行数应该等于总行数
**验证：需求 4.5**

### 属性 15：标准格式兼容性
*对于任何* 标准格式文件，系统应该按照原有逻辑处理，结果应该与之前一致
**验证：需求 5.1**

## 错误处理

### 错误类型

1. **格式识别错误**
   - 无法识别文件格式
   - 缺少必需列
   - 处理方式：返回明确的错误信息，指出缺少哪些列

2. **数据验证错误**
   - 类目ID格式错误
   - 价格无效
   - 商品名称无效
   - 处理方式：记录错误详情，继续处理其他行

3. **文件读取错误**
   - 文件损坏
   - 格式不支持
   - 处理方式：立即返回错误，不继续处理

4. **系统错误**
   - 内存不足
   - 数据库连接失败
   - 处理方式：记录日志，返回通用错误信息

### 错误响应格式

```json
{
  "code": 400,
  "message": "数据验证失败",
  "data": {
    "formatType": "MEITUAN",
    "totalRows": 3752,
    "successCount": 3500,
    "failedCount": 252,
    "errors": [
      {
        "rowNum": 2,
        "fieldName": "类目ID",
        "errorMessage": "类目ID必须是10位数字",
        "originalValue": "123"
      },
      // ... 最多100条错误详情
    ],
    "hasMoreErrors": true,
    "remainingErrorCount": 152
  }
}
```

## 测试策略

### 单元测试

1. **FormatDetector测试**
   - 测试美团格式识别
   - 测试标准格式识别
   - 测试未知格式处理
   - 测试列映射生成

2. **MeituanFormatParser测试**
   - 测试字段提取
   - 测试空值处理
   - 测试特殊字符处理
   - 测试长文本截取

3. **DataValidator测试**
   - 测试类目ID验证
   - 测试价格验证
   - 测试商品名称验证
   - 测试边界条件

### 属性测试

使用 JUnit 5 + QuickTheories 进行属性测试：

1. **格式识别属性测试**
   - 生成包含美团必需列的随机表头，验证识别为美团格式
   - 生成不包含美团必需列的随机表头，验证不识别为美团格式

2. **字段提取属性测试**
   - 生成随机的美团格式数据，验证字段提取正确性
   - 生成包含空值的数据，验证默认值处理

3. **数据验证属性测试**
   - 生成随机的类目ID，验证10位数字规则
   - 生成随机的价格，验证范围规则
   - 生成随机的商品名称，验证长度规则

4. **统计信息属性测试**
   - 生成随机的导入数据，验证总行数 = 成功行数 + 失败行数
   - 验证错误详情数量不超过100条

### 集成测试

1. **完整导入流程测试**
   - 使用真实的美团导出文件测试
   - 使用标准格式文件测试
   - 使用混合格式文件测试

2. **性能测试**
   - 测试3000行数据的处理时间
   - 测试内存使用情况
   - 测试并发导入

3. **兼容性测试**
   - 测试与现有功能的兼容性
   - 测试不同Excel版本的兼容性

## 实现注意事项

### 1. 性能优化

- 使用流式读取处理大文件，避免一次性加载所有数据到内存
- 对于预览功能，只读取前20行数据
- 使用批量插入优化数据库操作

### 2. 内存管理

- 及时关闭Excel文件流
- 对于大文件，分批处理数据
- 使用弱引用缓存列映射信息

### 3. 线程安全

- FormatDetector设计为无状态，可以安全地并发使用
- 每次导入操作使用独立的Parser实例
- 避免共享可变状态

### 4. 可扩展性

- 格式识别规则配置化，便于添加新的平台格式
- 字段映射配置化，便于调整映射关系
- 验证规则独立，便于添加新的验证逻辑

### 5. 日志记录

- 记录格式识别结果
- 记录字段提取过程
- 记录验证错误详情
- 记录性能指标（处理时间、行数等）

## 配置管理

### 美团格式配置

```yaml
meituan:
  format:
    # 必需列（用于格式识别）
    required-columns:
      - 商品类目ID
      - 商品名称
      - 价格
    
    # 字段映射
    column-mapping:
      商品名称: productName
      商品类目ID: categoryId
      价格: price
      库存: stock
      文字详情: description
    
    # 验证规则
    validation:
      category-id-pattern: "^[0-9]{10}$"
      price-min: 0.01
      price-max: 99999.99
      product-name-max-length: 255
      description-max-length: 1000
```

## 部署考虑

### 1. 向后兼容性

- 保持现有API接口不变
- 新增格式类型字段为可选
- 确保现有客户端不受影响

### 2. 数据迁移

- 不需要数据迁移
- 新增的dataSource字段可以为null（表示旧数据）

### 3. 监控指标

- 格式识别成功率
- 美团格式导入成功率
- 平均处理时间
- 错误率

### 4. 回滚计划

- 如果出现问题，可以通过配置禁用美团格式识别
- 系统会自动回退到标准格式处理
- 不影响现有功能
