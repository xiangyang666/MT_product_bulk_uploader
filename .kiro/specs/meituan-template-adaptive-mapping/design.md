# 设计文档 - 美团模板自适应字段映射

## 概述

本功能实现智能识别不同版本美团模板的列名变化，通过语义匹配和模糊匹配自动建立字段映射关系，无需为每个新版本手动更新代码。系统将支持配置化的映射规则，并提供映射结果可视化和学习优化功能。

### 核心问题

当前系统使用固定的列名映射（如"商品类目ID" → categoryId），但美团模板会更新：
- v1.3.6: "重量" → weight
- v1.3.7: "毛重" → weight（列名变化）

固定映射无法处理这种变化，需要自适应映射机制。

## 架构

### 整体架构

```
┌─────────────────────────────────────────┐
│         AdaptiveMappingEngine           │
│  (自适应映射引擎 - 核心协调器)          │
└────────┬────────────────────────────────┘
         │
         ├─→ ┌──────────────────────────┐
         │   │  MappingRuleRepository   │
         │   │  (映射规则仓库)          │
         │   │  - 加载配置文件          │
         │   │  - 管理规则版本          │
         │   │  - 热加载支持            │
         │   └──────────────────────────┘
         │
         ├─→ ┌──────────────────────────┐
         │   │  SemanticMatcher         │
         │   │  (语义匹配器)            │
         │   │  - 关键词匹配            │
         │   │  - 同义词匹配            │
         │   │  - 字符串相似度          │
         │   └──────────────────────────┘
         │
         ├─→ ┌──────────────────────────┐
         │   │  ConfidenceCalculator    │
         │   │  (置信度计算器)          │
         │   │  - 精确匹配: 1.0         │
         │   │  - 关键词匹配: 0.8-0.95  │
         │   │  - 模糊匹配: 0.5-0.8     │
         │   └──────────────────────────┘
         │
         ├─→ ┌──────────────────────────┐
         │   │  MappingResultBuilder    │
         │   │  (映射结果构建器)        │
         │   │  - 构建映射关系          │
         │   │  - 标记警告和错误        │
         │   │  - 生成可视化数据        │
         │   └──────────────────────────┘
         │
         └─→ ┌──────────────────────────┐
             │  MappingHistoryService   │
             │  (映射历史服务)          │
             │  - 记录成功映射          │
             │  - 统计列名频率          │
             │  - 学习优化建议          │
             └──────────────────────────┘
```

### 处理流程

```
上传文件
   │
   ▼
读取表头 → AdaptiveMappingEngine.buildMapping()
   │
   ├─→ 加载映射规则（MappingRuleRepository）
   │
   ├─→ 对每个表头列进行匹配
   │   │
   │   ├─→ 1. 精确匹配（完全相同）
   │   │      置信度 = 1.0
   │   │
   │   ├─→ 2. 标准化匹配（去空格、标点）
   │   │      置信度 = 0.95
   │   │
   │   ├─→ 3. 关键词匹配（包含关键词）
   │   │      置信度 = 0.8-0.9
   │   │
   │   └─→ 4. 模糊匹配（字符串相似度）
   │          置信度 = 0.5-0.8
   │
   ├─→ 选择最佳匹配（置信度最高）
   │
   ├─→ 检查必需字段是否都已映射
   │
   ├─→ 构建映射结果
   │   - 成功映射列表
   │   - 警告列表（置信度<0.8）
   │   - 错误列表（必需字段未映射）
   │
   └─→ 返回映射结果
```

## 组件和接口

### 1. AdaptiveMappingEngine（自适应映射引擎）

**职责**：协调整个自适应映射过程

**接口**：
```java
public class AdaptiveMappingEngine {
    /**
     * 构建字段映射
     * @param headers Excel表头列表
     * @param formatType 格式类型（MEITUAN / STANDARD）
     * @return 映射结果
     */
    public MappingResult buildMapping(List<String> headers, FormatType formatType);
    
    /**
     * 验证映射结果
     * @param mappingResult 映射结果
     * @return 验证结果（是否包含所有必需字段）
     */
    public ValidationResult validateMapping(MappingResult mappingResult);
}
```

### 2. MappingRuleRepository（映射规则仓库）

**职责**：管理和加载映射规则配置

**接口**：
```java
public class MappingRuleRepository {
    /**
     * 加载映射规则
     * @return 映射规则集合
     */
    public Map<String, FieldMappingRule> loadRules();
    
    /**
     * 重新加载规则（热加载）
     */
    public void reloadRules();
    
    /**
     * 获取字段的映射规则
     * @param fieldName 系统字段名
     * @return 映射规则
     */
    public FieldMappingRule getRule(String fieldName);
}
```

**配置文件格式**（mapping-rules.json）：
```json
{
  "version": "1.0",
  "rules": {
    "productName": {
      "fieldName": "productName",
      "displayName": "商品名称",
      "required": true,
      "exactMatches": ["商品名称"],
      "keywords": ["商品", "名称", "品名", "product", "name"],
      "synonyms": ["商品名", "产品名称", "货品名称"],
      "priority": 100
    },
    "categoryId": {
      "fieldName": "categoryId",
      "displayName": "类目ID",
      "required": true,
      "exactMatches": ["商品类目ID", "类目ID", "商品三级类目ID"],
      "keywords": ["类目", "分类", "category"],
      "keywordCombinations": [
        ["类目", "ID"],
        ["类目", "编号"],
        ["分类", "ID"]
      ],
      "synonyms": ["品类ID", "类别ID"],
      "priority": 100
    },
    "price": {
      "fieldName": "price",
      "displayName": "价格",
      "required": true,
      "exactMatches": ["价格", "价格（元）", "单价"],
      "keywords": ["价格", "单价", "price"],
      "synonyms": ["售价", "定价"],
      "priority": 100
    },
    "stock": {
      "fieldName": "stock",
      "displayName": "库存",
      "required": false,
      "exactMatches": ["库存"],
      "keywords": ["库存", "数量", "stock"],
      "synonyms": ["存货", "库存数量"],
      "priority": 80
    },
    "weight": {
      "fieldName": "weight",
      "displayName": "重量",
      "required": false,
      "exactMatches": ["重量", "毛重", "净重"],
      "keywords": ["重量", "毛重", "净重", "weight"],
      "synonyms": ["重", "克重"],
      "priority": 70
    },
    "description": {
      "fieldName": "description",
      "displayName": "商品描述",
      "required": false,
      "exactMatches": ["商品描述", "文字详情", "描述"],
      "keywords": ["描述", "详情", "说明", "description"],
      "synonyms": ["商品说明", "产品描述"],
      "priority": 60
    }
  }
}
```

### 3. SemanticMatcher（语义匹配器）

**职责**：执行各种匹配算法

**接口**：
```java
public class SemanticMatcher {
    /**
     * 精确匹配
     * @param columnName 列名
     * @param rule 映射规则
     * @return 是否匹配
     */
    public boolean exactMatch(String columnName, FieldMappingRule rule);
    
    /**
     * 标准化匹配（去除空格、标点等）
     * @param columnName 列名
     * @param rule 映射规则
     * @return 是否匹配
     */
    public boolean normalizedMatch(String columnName, FieldMappingRule rule);
    
    /**
     * 关键词匹配
     * @param columnName 列名
     * @param rule 映射规则
     * @return 匹配得分（0-1）
     */
    public double keywordMatch(String columnName, FieldMappingRule rule);
    
    /**
     * 模糊匹配（字符串相似度）
     * @param columnName 列名
     * @param rule 映射规则
     * @return 相似度得分（0-1）
     */
    public double fuzzyMatch(String columnName, FieldMappingRule rule);
}
```

**匹配算法**：

1. **精确匹配**：
   - 列名完全等于规则中的exactMatches
   - 置信度 = 1.0

2. **标准化匹配**：
   - 去除空格、标点、括号等
   - "价格（元）" → "价格元" → 匹配 "价格"
   - 置信度 = 0.95

3. **关键词匹配**：
   - 检查列名是否包含所有关键词
   - "商品类目ID" 包含 ["类目", "ID"] → 匹配
   - 置信度 = 0.8 + (匹配关键词数 / 总关键词数) * 0.1

4. **模糊匹配**（Levenshtein距离）：
   - 计算字符串编辑距离
   - 相似度 = 1 - (编辑距离 / 最大长度)
   - 置信度 = 相似度 * 0.8（最高0.8）

### 4. ConfidenceCalculator（置信度计算器）

**职责**：计算映射的置信度

**接口**：
```java
public class ConfidenceCalculator {
    /**
     * 计算映射置信度
     * @param columnName 列名
     * @param rule 映射规则
     * @param matchType 匹配类型
     * @return 置信度（0-1）
     */
    public double calculateConfidence(
        String columnName, 
        FieldMappingRule rule, 
        MatchType matchType
    );
    
    /**
     * 根据历史数据调整置信度
     * @param baseConfidence 基础置信度
     * @param columnName 列名
     * @param fieldName 字段名
     * @return 调整后的置信度
     */
    public double adjustByHistory(
        double baseConfidence, 
        String columnName, 
        String fieldName
    );
}
```

**置信度规则**：
```
精确匹配:        1.0
标准化匹配:      0.95
关键词匹配:      0.8 - 0.9（根据匹配关键词数量）
模糊匹配:        0.5 - 0.8（根据相似度）
历史加成:        +0.05（如果该列名历史上成功映射过）
```

### 5. MappingResult（映射结果）

**数据结构**：
```java
public class MappingResult {
    private FormatType formatType;                    // 格式类型
    private Map<String, ColumnMapping> mappings;      // 字段映射
    private List<MappingWarning> warnings;            // 警告列表
    private List<MappingError> errors;                // 错误列表
    private boolean isValid;                          // 是否有效
    private long buildTime;                           // 构建耗时（毫秒）
}

public class ColumnMapping {
    private String fieldName;          // 系统字段名
    private String columnName;         // Excel列名
    private int columnIndex;           // 列索引
    private double confidence;         // 置信度
    private MatchType matchType;       // 匹配类型
    private boolean isRequired;        // 是否必需
}

public class MappingWarning {
    private String fieldName;          // 字段名
    private String columnName;         // 列名
    private double confidence;         // 置信度
    private String message;            // 警告信息
}

public class MappingError {
    private String fieldName;          // 字段名
    private String message;            // 错误信息
    private List<String> suggestions;  // 建议的列名
}

public enum MatchType {
    EXACT,          // 精确匹配
    NORMALIZED,     // 标准化匹配
    KEYWORD,        // 关键词匹配
    FUZZY,          // 模糊匹配
    MANUAL          // 手动指定
}
```

### 6. MappingHistoryService（映射历史服务）

**职责**：记录和学习映射历史

**接口**：
```java
public class MappingHistoryService {
    /**
     * 记录成功的映射
     * @param mappingResult 映射结果
     * @param importSuccess 导入是否成功
     */
    public void recordMapping(MappingResult mappingResult, boolean importSuccess);
    
    /**
     * 获取列名的历史映射
     * @param columnName 列名
     * @return 历史映射的字段名列表（按频率排序）
     */
    public List<String> getHistoricalMappings(String columnName);
    
    /**
     * 获取映射统计
     * @return 统计信息
     */
    public MappingStatistics getStatistics();
    
    /**
     * 生成优化建议
     * @return 建议列表
     */
    public List<OptimizationSuggestion> generateSuggestions();
}
```

**数据存储**（mapping_history表）：
```sql
CREATE TABLE mapping_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    column_name VARCHAR(255) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    confidence DECIMAL(3,2),
    match_type VARCHAR(20),
    import_success TINYINT(1),
    success_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_column_name (column_name),
    INDEX idx_field_name (field_name)
);
```

## 数据模型

### FieldMappingRule（字段映射规则）

```java
public class FieldMappingRule {
    private String fieldName;              // 系统字段名
    private String displayName;            // 显示名称
    private boolean required;              // 是否必需
    private List<String> exactMatches;     // 精确匹配列表
    private List<String> keywords;         // 关键词列表
    private List<List<String>> keywordCombinations;  // 关键词组合
    private List<String> synonyms;         // 同义词列表
    private int priority;                  // 优先级（用于冲突解决）
}
```

### MappingStatistics（映射统计）

```java
public class MappingStatistics {
    private int totalMappings;             // 总映射次数
    private int successfulMappings;        // 成功映射次数
    private double averageConfidence;      // 平均置信度
    private Map<String, Integer> columnFrequency;  // 列名频率
    private Map<String, Double> fieldAccuracy;     // 字段准确率
}
```

## 正确性属性

*属性是一个特征或行为，应该在系统的所有有效执行中保持为真——本质上是关于系统应该做什么的正式陈述。属性作为人类可读规范和机器可验证正确性保证之间的桥梁。*

### 属性 1：精确匹配优先性
*对于任何* 列名，如果存在精确匹配，系统应该选择精确匹配而不是其他匹配类型
**验证：需求 6.1**

### 属性 2：必需字段完整性
*对于任何* 映射结果，如果所有必需字段都已映射，isValid应该为true
**验证：需求 1.2, 1.3, 1.4, 1.5, 1.6**

### 属性 3：置信度单调性
*对于任何* 两个匹配，精确匹配的置信度应该高于标准化匹配，标准化匹配应该高于关键词匹配，关键词匹配应该高于模糊匹配
**验证：需求 4.3**

### 属性 4：唯一映射性
*对于任何* 映射结果，每个系统字段最多只能映射到一个Excel列
**验证：需求 2.4**

### 属性 5：关键词组合完整性
*对于任何* 关键词组合匹配，列名应该包含组合中的所有关键词
**验证：需求 1.2, 1.3, 1.4**

### 属性 6：置信度范围正确性
*对于任何* 映射，置信度应该在[0, 1]范围内
**验证：需求 7.4**

### 属性 7：警告阈值一致性
*对于任何* 映射，如果置信度小于0.8，应该生成警告
**验证：需求 4.3**

### 属性 8：错误检测完整性
*对于任何* 必需字段，如果未能映射，应该生成错误
**验证：需求 4.4**

### 属性 9：配置回退正确性
*对于任何* 配置加载失败的情况，系统应该使用内置的默认规则
**验证：需求 3.3, 3.5**

### 属性 10：历史记录一致性
*对于任何* 成功的导入，系统应该记录所有使用的映射关系
**验证：需求 5.1, 5.2**

### 属性 11：映射性能边界
*对于任何* 包含N列的文件（N≤100），字段映射时间应该≤1秒
**验证：需求 7.1**

### 属性 12：向后兼容性
*对于任何* 使用旧版本固定映射的文件，新系统应该产生相同的映射结果
**验证：需求 6.2, 6.3, 6.5**

## 错误处理

### 错误类型

1. **配置加载错误**
   - 配置文件不存在
   - 配置文件格式错误
   - 处理方式：使用内置默认规则，记录警告日志

2. **必需字段缺失**
   - 无法映射必需字段
   - 处理方式：返回错误，提供建议的列名

3. **映射冲突**
   - 多个列都可能映射到同一字段
   - 处理方式：选择置信度最高的，记录警告

4. **低置信度映射**
   - 映射置信度<0.8
   - 处理方式：允许映射，但生成警告

### 错误响应格式

```json
{
  "code": 200,
  "message": "映射完成，但存在警告",
  "data": {
    "formatType": "MEITUAN",
    "isValid": true,
    "mappings": {
      "productName": {
        "fieldName": "productName",
        "columnName": "商品名称",
        "columnIndex": 1,
        "confidence": 1.0,
        "matchType": "EXACT",
        "isRequired": true
      },
      "weight": {
        "fieldName": "weight",
        "columnName": "毛重",
        "columnIndex": 12,
        "confidence": 0.95,
        "matchType": "NORMALIZED",
        "isRequired": false
      }
    },
    "warnings": [
      {
        "fieldName": "weight",
        "columnName": "毛重",
        "confidence": 0.95,
        "message": "列名'毛重'与字段'重量'的匹配置信度较低(0.95)，请确认是否正确"
      }
    ],
    "errors": [],
    "buildTime": 45
  }
}
```

## 测试策略

### 单元测试

1. **SemanticMatcher测试**
   - 测试精确匹配
   - 测试标准化匹配
   - 测试关键词匹配
   - 测试模糊匹配
   - 测试边界情况

2. **ConfidenceCalculator测试**
   - 测试置信度计算
   - 测试历史调整
   - 测试置信度范围

3. **MappingRuleRepository测试**
   - 测试规则加载
   - 测试热加载
   - 测试配置错误处理

### 属性测试

使用 JUnit 5 + QuickTheories 进行属性测试：

1. **精确匹配优先性测试**
   - 生成随机列名和规则
   - 如果存在精确匹配，验证选择精确匹配

2. **置信度单调性测试**
   - 生成随机匹配结果
   - 验证置信度顺序：精确 > 标准化 > 关键词 > 模糊

3. **唯一映射性测试**
   - 生成随机映射结果
   - 验证每个字段最多映射一次

4. **置信度范围测试**
   - 生成随机映射
   - 验证所有置信度在[0, 1]范围内

### 集成测试

1. **多版本模板测试**
   - 测试v1.3.6模板
   - 测试v1.3.7模板
   - 验证都能正确映射

2. **向后兼容性测试**
   - 使用旧的测试数据
   - 验证映射结果与之前一致

3. **性能测试**
   - 测试100列文件的映射时间
   - 验证<1秒完成

## 实现注意事项

### 1. 性能优化

- 缓存映射规则，避免重复加载
- 使用高效的字符串匹配算法
- 对于大文件，只在第一次读取时进行映射
- 使用并行流处理多列匹配

### 2. 内存管理

- 映射规则使用单例模式
- 及时清理临时匹配结果
- 限制历史记录的大小

### 3. 线程安全

- MappingRuleRepository使用读写锁
- 映射历史记录使用线程安全的数据结构
- 避免共享可变状态

### 4. 可扩展性

- 映射规则配置化
- 匹配算法可插拔
- 易于添加新的字段类型

### 5. 日志记录

- 记录映射过程的详细信息
- 记录置信度计算过程
- 记录警告和错误
- 记录性能指标

## 配置管理

### 映射规则配置文件

位置：`src/main/resources/mapping-rules.json`

### 应用配置

```yaml
meituan:
  adaptive-mapping:
    enabled: true
    config-file: classpath:mapping-rules.json
    confidence-threshold: 0.8
    enable-history: true
    enable-learning: true
    cache-rules: true
```

## 部署考虑

### 1. 向后兼容性

- 保持FormatDetector和MeituanFormatParser的接口不变
- 新增AdaptiveMappingEngine作为可选功能
- 通过配置开关控制是否启用自适应映射

### 2. 数据迁移

- 新增mapping_history表
- 不影响现有数据

### 3. 监控指标

- 映射成功率
- 平均置信度
- 警告和错误数量
- 映射耗时

### 4. 回滚计划

- 通过配置禁用自适应映射
- 回退到固定映射逻辑
- 不影响现有功能

## UI设计

### 映射结果可视化

```
┌─────────────────────────────────────────────────────┐
│ 字段映射结果                                        │
├─────────────────────────────────────────────────────┤
│ ✓ 商品名称      →  商品名称        [精确匹配 100%] │
│ ✓ 类目ID        →  商品三级类目ID  [关键词 85%]    │
│ ✓ 价格          →  价格（元）      [标准化 95%]    │
│ ⚠ 重量          →  毛重            [标准化 95%]    │
│ ✓ 库存          →  库存            [精确匹配 100%] │
│ ✓ 商品描述      →  文字详情        [精确匹配 100%] │
├─────────────────────────────────────────────────────┤
│ ⚠ 1个警告  ✓ 6个字段已映射  ✗ 0个错误             │
└─────────────────────────────────────────────────────┘

警告详情：
⚠ 重量字段：列名'毛重'与字段'重量'的匹配置信度为95%
  建议：如果'毛重'不是重量字段，请手动调整映射
```

### 手动调整界面

```
┌─────────────────────────────────────────────────────┐
│ 手动调整映射                                        │
├─────────────────────────────────────────────────────┤
│ 字段：重量                                          │
│ 当前映射：毛重 (置信度: 95%)                       │
│                                                     │
│ 选择正确的列：                                      │
│ ○ 毛重                                              │
│ ○ 净重                                              │
│ ○ 重量                                              │
│ ○ 其他: [_____________]                            │
│                                                     │
│ [取消]  [确认]                                      │
└─────────────────────────────────────────────────────┘
```

## 实现计划

### 阶段1：核心映射引擎（必需）
- AdaptiveMappingEngine
- SemanticMatcher
- ConfidenceCalculator
- MappingResult

### 阶段2：配置化支持（必需）
- MappingRuleRepository
- 配置文件加载
- 默认规则

### 阶段3：UI集成（必需）
- 映射结果可视化
- 警告和错误显示

### 阶段4：历史学习（可选）
- MappingHistoryService
- 数据库表
- 学习优化

### 阶段5：手动调整（可选）
- 手动调整界面
- 映射覆盖功能
