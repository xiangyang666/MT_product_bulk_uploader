# 设计文档

## 概述

批量上传全部商品功能是对现有批量上传页面的重大改进。通过简化操作流程、优化用户界面和提升性能，使商家能够更高效地完成商品批量上传准备工作。

### 核心改进
- 一键生成全部商品模板，无需手动选择
- 简化页面设计，突出主要功能
- 实时商品数据统计和预览
- 操作历史记录追踪
- 优化大数据量处理性能

### 技术栈
**前端：**
- Vue 3：组件化开发
- Element Plus：UI组件库
- Axios：HTTP客户端

**后端：**
- Spring Boot 3：应用框架
- MyBatis Plus：数据访问
- Apache POI：Excel生成
- MySQL 8：数据存储

## 架构

### 组件架构

```
┌─────────────────────────────────────────────────────────┐
│              批量上传页面 (Upload.vue)                    │
│  ┌──────────────────────────────────────────────────┐   │
│  │         统计卡片 (StatsCard)                      │   │
│  │  - 商品总数                                       │   │
│  │  - 待上传数量                                     │   │
│  │  - 已上传数量                                     │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │      主操作区 (ActionCard)                        │   │
│  │  - 生成全部商品模板按钮                           │   │
│  │  - 商品预览列表                                   │   │
│  │  - 查看全部商品链接                               │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │      操作历史 (HistoryCard)                       │   │
│  │  - 最近3次操作记录                                │   │
│  │  - 操作时间、商品数量、状态                       │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          │ HTTP API
                          ▼
┌─────────────────────────────────────────────────────────┐
│              后端服务 (ProductController)                │
│  ┌──────────────────────────────────────────────────┐   │
│  │  GET /api/products/stats                         │   │
│  │  - 返回商品统计信息                               │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │  POST /api/products/generate-all-template        │   │
│  │  - 生成全部商品模板                               │   │
│  │  - 流式处理大数据量                               │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │  GET /api/products/recent                        │   │
│  │  - 获取最近导入的商品预览                         │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │  GET /api/operation-logs/recent                  │   │
│  │  - 获取最近操作历史                               │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
                   ┌──────────────┐
                   │  MySQL数据库  │
                   └──────────────┘
```

### 数据流

```
用户点击"生成全部商品模板"
         │
         ▼
前端发送 POST /api/products/generate-all-template
         │
         ▼
后端查询所有商品数据（分批查询）
         │
         ▼
使用Apache POI流式生成Excel
         │
         ▼
返回Excel文件流给前端
         │
         ▼
前端触发文件下载
         │
         ▼
记录操作日志到数据库
         │
         ▼
刷新统计信息和操作历史
```

## 组件和接口

### 前端组件

#### 1. Upload.vue（主页面）
- **职责：** 批量上传页面的主容器
- **子组件：**
  - StatsCard：统计信息卡片
  - ActionCard：主操作区域
  - HistoryCard：操作历史卡片

#### 2. StatsCard（统计卡片）
- **职责：** 显示商品统计信息
- **接口：**
  ```typescript
  interface StatsCardProps {
    totalCount: number;
    pendingCount: number;
    uploadedCount: number;
    loading: boolean;
  }
  ```

#### 3. ActionCard（主操作区）
- **职责：** 提供生成模板功能和商品预览
- **接口：**
  ```typescript
  interface ActionCardProps {
    recentProducts: Product[];
    loading: boolean;
    onGenerateTemplate: () => Promise<void>;
  }
  ```

#### 4. HistoryCard（操作历史）
- **职责：** 显示最近的操作记录
- **接口：**
  ```typescript
  interface HistoryCardProps {
    operations: OperationLog[];
    loading: boolean;
  }
  ```

### 后端接口

#### 1. 获取商品统计信息
```
GET /api/products/stats

Response:
{
  "code": 200,
  "message": "成功",
  "data": {
    "totalCount": 1000,
    "pendingCount": 800,
    "uploadedCount": 200,
    "failedCount": 0
  }
}
```

#### 2. 生成全部商品模板
```
POST /api/products/generate-all-template
Content-Type: application/json

Request:
{
  "merchantId": 1
}

Response:
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="meituan_all_products_20260209.xlsx"

[Excel文件二进制流]
```

#### 3. 获取最近商品预览
```
GET /api/products/recent?limit=10

Response:
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "productName": "商品1",
      "categoryId": "1234567890",
      "price": 99.99,
      "status": 0,
      "createdTime": "2026-02-09 10:00:00"
    }
  ]
}
```

#### 4. 获取最近操作历史
```
GET /api/operation-logs/recent?limit=3&type=GENERATE_ALL

Response:
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "operationType": "GENERATE_ALL",
      "productCount": 1000,
      "status": 1,
      "createdTime": "2026-02-09 09:30:00",
      "duration": 8500
    }
  ]
}
```

## 数据模型

### 前端数据模型

```typescript
// 商品统计信息
interface ProductStats {
  totalCount: number;
  pendingCount: number;
  uploadedCount: number;
  failedCount: number;
}

// 商品预览
interface ProductPreview {
  id: number;
  productName: string;
  categoryId: string;
  price: number;
  status: number;
  createdTime: string;
}

// 操作日志
interface OperationLog {
  id: number;
  operationType: string;
  productCount: number;
  status: number;
  createdTime: string;
  duration: number;
  errorMessage?: string;
}

// 页面状态
interface PageState {
  stats: ProductStats;
  recentProducts: ProductPreview[];
  recentOperations: OperationLog[];
  loading: {
    stats: boolean;
    products: boolean;
    operations: boolean;
    generating: boolean;
  };
}
```

### 后端数据模型

现有的Product和OperationLog实体类保持不变，新增查询方法：

```java
// ProductMapper新增方法
public interface ProductMapper extends BaseMapper<Product> {
    
    // 获取商品统计信息
    @Select("SELECT " +
            "COUNT(*) as totalCount, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as pendingCount, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as uploadedCount, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as failedCount " +
            "FROM t_product WHERE merchant_id = #{merchantId}")
    ProductStats getStats(@Param("merchantId") Long merchantId);
    
    // 获取最近导入的商品
    @Select("SELECT * FROM t_product " +
            "WHERE merchant_id = #{merchantId} " +
            "ORDER BY created_time DESC " +
            "LIMIT #{limit}")
    List<Product> selectRecent(@Param("merchantId") Long merchantId, 
                               @Param("limit") int limit);
    
    // 流式查询所有商品（用于生成模板）
    @Select("SELECT * FROM t_product WHERE merchant_id = #{merchantId}")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 1000)
    void streamAll(@Param("merchantId") Long merchantId, 
                   ResultHandler<Product> handler);
}

// OperationLogMapper新增方法
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    
    // 获取最近操作记录
    @Select("SELECT * FROM t_operation_log " +
            "WHERE merchant_id = #{merchantId} " +
            "AND operation_type = #{operationType} " +
            "ORDER BY created_time DESC " +
            "LIMIT #{limit}")
    List<OperationLog> selectRecent(@Param("merchantId") Long merchantId,
                                    @Param("operationType") String operationType,
                                    @Param("limit") int limit);
}
```

## 正确性属性

*属性是指在系统所有有效执行中都应该成立的特征或行为——本质上是关于系统应该做什么的正式声明。属性是人类可读规范和机器可验证正确性保证之间的桥梁。*

### 属性 1：全部商品查询完整性
*对于任何*商品数据集，当点击生成全部商品模板按钮时，系统应该查询并包含数据库中的所有商品
**验证需求：1.1, 1.2**

### 属性 2：模板下载触发
*对于任何*成功的模板生成操作，系统应该自动触发文件下载
**验证需求：1.3**

### 属性 3：加载状态显示
*对于任何*模板生成操作，在生成过程中系统应该显示加载状态
**验证需求：1.5**

### 属性 4：统计信息准确性
*对于任何*商品数据集，页面显示的商品总数、待上传数量、已上传数量应该与数据库中的实际数据一致
**验证需求：2.2, 3.1**

### 属性 5：商品预览数量限制
*对于任何*商品数据集，商品预览列表显示的商品数量应该不超过10条，且应该是最近导入的商品
**验证需求：3.2**

### 属性 6：商品预览数据完整性
*对于任何*显示在预览列表中的商品，应该包含商品名称、类目ID、价格、状态等关键字段
**验证需求：3.4**

### 属性 7：操作日志记录
*对于任何*成功的模板生成操作，系统应该在数据库中记录操作日志，包含操作时间、商品数量
**验证需求：4.1**

### 属性 8：操作历史显示限制
*对于任何*操作历史查询，页面显示的记录数量应该不超过3条
**验证需求：4.2**

### 属性 9：操作历史数据完整性
*对于任何*显示的操作历史记录，应该包含操作时间、商品数量、操作状态字段
**验证需求：4.4**

### 属性 10：成功消息完整性
*对于任何*成功的模板生成操作，成功提示消息应该包含商品数量和文件名
**验证需求：5.4**

### 属性 11：响应式布局功能性
*对于任何*窗口大小，批量上传页面的核心功能应该保持可用
**验证需求：7.3**

## 错误处理

### 错误场景

#### 1. 无商品数据
```typescript
// 前端处理
if (stats.totalCount === 0) {
  ElMessage.warning('暂无商品数据，请先导入商品');
  return;
}
```

#### 2. 模板生成失败
```java
// 后端处理
@ExceptionHandler(TemplateGenerationException.class)
public ResponseEntity<ApiResponse> handleTemplateGenerationException(
    TemplateGenerationException e) {
    log.error("模板生成失败", e);
    return ResponseEntity.status(500)
        .body(ApiResponse.error(500, 
            "模板生成失败：" + e.getMessage() + "。请检查数据完整性后重试"));
}
```

#### 3. 网络错误
```typescript
// 前端Axios拦截器
axios.interceptors.response.use(
  response => response,
  error => {
    if (!error.response) {
      ElMessage.error('网络连接失败，请检查网络设置后重试');
    }
    return Promise.reject(error);
  }
);
```

#### 4. 数据库超时
```java
// 后端配置
@Configuration
public class DatabaseConfig {
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setConnectionTimeout(30000); // 30秒
        config.setValidationTimeout(5000);  // 5秒
        return new HikariDataSource(config);
    }
}
```

### 用户反馈设计

```typescript
// 消息类型配置
const MessageConfig = {
  success: {
    type: 'success',
    icon: 'CircleCheck',
    duration: 3000
  },
  warning: {
    type: 'warning',
    icon: 'Warning',
    duration: 5000
  },
  error: {
    type: 'error',
    icon: 'CircleClose',
    duration: 5000
  }
};

// 使用示例
ElMessage({
  ...MessageConfig.success,
  message: `成功生成包含 ${count} 个商品的模板：${filename}`
});
```

## 测试策略

### 测试框架

**前端测试：**
- Vitest：单元测试框架
- @vue/test-utils：Vue组件测试
- fast-check：属性测试框架

**后端测试：**
- JUnit 5：单元测试框架
- Mockito：Mock框架
- jqwik：属性测试框架

### 单元测试

单元测试覆盖具体示例和边界情况：

**前端单元测试：**
```typescript
describe('Upload.vue', () => {
  it('should show warning when no products exist', async () => {
    const wrapper = mount(Upload, {
      global: {
        mocks: {
          $api: {
            getStats: () => Promise.resolve({ totalCount: 0 })
          }
        }
      }
    });
    
    await wrapper.vm.handleGenerateTemplate();
    
    // 验证警告消息
    expect(ElMessage.warning).toHaveBeenCalledWith(
      expect.stringContaining('暂无商品数据')
    );
  });
  
  it('should navigate to products page when clicking view all', async () => {
    const wrapper = mount(Upload);
    const router = useRouter();
    
    await wrapper.find('.view-all-link').trigger('click');
    
    expect(router.push).toHaveBeenCalledWith('/products');
  });
  
  it('should show error message on network failure', async () => {
    const wrapper = mount(Upload, {
      global: {
        mocks: {
          $api: {
            generateAllTemplate: () => Promise.reject(new Error('Network Error'))
          }
        }
      }
    });
    
    await wrapper.vm.handleGenerateTemplate();
    
    expect(ElMessage.error).toHaveBeenCalled();
  });
});
```

**后端单元测试：**
```java
@Test
void testGenerateAllTemplate_WithNoProducts_ShouldThrowException() {
    // Given
    when(productMapper.selectCount(any())).thenReturn(0L);
    
    // When & Then
    assertThatThrownBy(() -> productService.generateAllTemplate(1L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("暂无商品数据");
}

@Test
void testGenerateAllTemplate_WithProducts_ShouldReturnExcelFile() {
    // Given
    List<Product> products = createTestProducts(100);
    when(productMapper.selectList(any())).thenReturn(products);
    
    // When
    File result = productService.generateAllTemplate(1L);
    
    // Then
    assertThat(result).exists();
    assertThat(result.getName()).endsWith(".xlsx");
}
```

### 属性测试

属性测试验证跨所有输入的通用属性，每个测试运行至少100次迭代：

**前端属性测试：**
```typescript
import fc from 'fast-check';

describe('Property Tests', () => {
  it('Property 4: 统计信息准确性', () => {
    /**
     * Feature: batch-upload-all-products, Property 4: 统计信息准确性
     */
    fc.assert(
      fc.property(
        fc.array(productArbitrary(), { minLength: 0, maxLength: 1000 }),
        (products) => {
          // Given: 任意商品数据集
          const stats = calculateStats(products);
          
          // Then: 统计信息应该准确
          const pending = products.filter(p => p.status === 0).length;
          const uploaded = products.filter(p => p.status === 1).length;
          
          expect(stats.totalCount).toBe(products.length);
          expect(stats.pendingCount).toBe(pending);
          expect(stats.uploadedCount).toBe(uploaded);
        }
      ),
      { numRuns: 100 }
    );
  });
  
  it('Property 5: 商品预览数量限制', () => {
    /**
     * Feature: batch-upload-all-products, Property 5: 商品预览数量限制
     */
    fc.assert(
      fc.property(
        fc.array(productArbitrary(), { minLength: 0, maxLength: 100 }),
        (products) => {
          // Given: 任意商品数据集
          const preview = getRecentProducts(products, 10);
          
          // Then: 预览数量不超过10条
          expect(preview.length).toBeLessThanOrEqual(10);
          expect(preview.length).toBeLessThanOrEqual(products.length);
          
          // 且应该是最近的商品
          if (products.length > 0) {
            const sortedProducts = [...products].sort(
              (a, b) => new Date(b.createdTime) - new Date(a.createdTime)
            );
            expect(preview).toEqual(sortedProducts.slice(0, 10));
          }
        }
      ),
      { numRuns: 100 }
    );
  });
  
  it('Property 6: 商品预览数据完整性', () => {
    /**
     * Feature: batch-upload-all-products, Property 6: 商品预览数据完整性
     */
    fc.assert(
      fc.property(
        fc.array(productArbitrary(), { minLength: 1, maxLength: 20 }),
        (products) => {
          // Given: 任意商品数据集
          const wrapper = mount(ProductPreview, {
            props: { products }
          });
          
          // Then: 每个商品应该显示所有关键字段
          products.forEach((product, index) => {
            const row = wrapper.findAll('.product-row')[index];
            expect(row.text()).toContain(product.productName);
            expect(row.text()).toContain(product.categoryId);
            expect(row.text()).toContain(product.price.toString());
          });
        }
      ),
      { numRuns: 100 }
    );
  });
  
  it('Property 10: 成功消息完整性', () => {
    /**
     * Feature: batch-upload-all-products, Property 10: 成功消息完整性
     */
    fc.assert(
      fc.property(
        fc.integer({ min: 1, max: 10000 }),
        fc.string({ minLength: 5, maxLength: 50 }),
        (count, filename) => {
          // Given: 任意商品数量和文件名
          const message = formatSuccessMessage(count, filename);
          
          // Then: 消息应该包含商品数量和文件名
          expect(message).toContain(count.toString());
          expect(message).toContain(filename);
        }
      ),
      { numRuns: 100 }
    );
  });
});

// 数据生成器
function productArbitrary() {
  return fc.record({
    id: fc.integer({ min: 1 }),
    productName: fc.string({ minLength: 1, maxLength: 100 }),
    categoryId: fc.string({ minLength: 10, maxLength: 10 }),
    price: fc.double({ min: 0.01, max: 99999.99 }),
    status: fc.integer({ min: 0, max: 2 }),
    createdTime: fc.date().map(d => d.toISOString())
  });
}
```

**后端属性测试：**
```java
@Property(tries = 100)
void allProductsQueryCompleteness(@ForAll List<@From("validProducts") Product> products) {
    /**
     * Feature: batch-upload-all-products, Property 1: 全部商品查询完整性
     */
    // Given: 任意商品数据集
    productRepository.batchInsert(products);
    
    // When: 生成全部商品模板
    File template = productService.generateAllTemplate(1L);
    List<Product> parsedProducts = excelService.parseExcel(
        new FileInputStream(template), "xlsx"
    );
    
    // Then: 模板应该包含所有商品
    assertThat(parsedProducts).hasSize(products.size());
    assertThat(parsedProducts).usingRecursiveComparison()
        .ignoringFields("id", "createdTime", "updatedTime")
        .isEqualTo(products);
}

@Property(tries = 100)
void operationLogRecording(
    @ForAll @IntRange(min = 1, max = 10000) int productCount
) {
    /**
     * Feature: batch-upload-all-products, Property 7: 操作日志记录
     */
    // Given: 任意数量的商品
    List<Product> products = createTestProducts(productCount);
    productRepository.batchInsert(products);
    
    // When: 生成模板
    productService.generateAllTemplate(1L);
    
    // Then: 应该记录操作日志
    List<OperationLog> logs = operationLogMapper.selectRecent(1L, "GENERATE_ALL", 1);
    assertThat(logs).hasSize(1);
    
    OperationLog log = logs.get(0);
    assertThat(log.getOperationType()).isEqualTo("GENERATE_ALL");
    assertThat(log.getProductCount()).isEqualTo(productCount);
    assertThat(log.getCreatedTime()).isNotNull();
}

@Property(tries = 100)
void operationHistoryDisplayLimit(
    @ForAll @IntRange(min = 0, max = 20) int historyCount
) {
    /**
     * Feature: batch-upload-all-products, Property 8: 操作历史显示限制
     */
    // Given: 任意数量的历史记录
    for (int i = 0; i < historyCount; i++) {
        createOperationLog(1L, "GENERATE_ALL", 100);
    }
    
    // When: 查询最近操作历史
    List<OperationLog> recent = operationLogMapper.selectRecent(1L, "GENERATE_ALL", 3);
    
    // Then: 返回的记录数不超过3条
    assertThat(recent.size()).isLessThanOrEqual(3);
    assertThat(recent.size()).isLessThanOrEqual(historyCount);
}

@Property(tries = 100)
void statsAccuracy(@ForAll List<@From("validProducts") Product> products) {
    /**
     * Feature: batch-upload-all-products, Property 4: 统计信息准确性
     */
    // Given: 任意商品数据集
    productRepository.batchInsert(products);
    
    // When: 查询统计信息
    ProductStats stats = productMapper.getStats(1L);
    
    // Then: 统计信息应该准确
    long pending = products.stream().filter(p -> p.getStatus() == 0).count();
    long uploaded = products.stream().filter(p -> p.getStatus() == 1).count();
    
    assertThat(stats.getTotalCount()).isEqualTo(products.size());
    assertThat(stats.getPendingCount()).isEqualTo(pending);
    assertThat(stats.getUploadedCount()).isEqualTo(uploaded);
}

// 数据生成器
@Provide
Arbitrary<Product> validProducts() {
    return Combinators.combine(
        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100),
        Arbitraries.strings().numeric().ofLength(10),
        Arbitraries.bigDecimals().between(
            BigDecimal.valueOf(0.01), 
            BigDecimal.valueOf(99999.99)
        ),
        Arbitraries.integers().between(0, 2)
    ).as((name, categoryId, price, status) -> {
        Product product = new Product();
        product.setProductName(name);
        product.setCategoryId(categoryId);
        product.setPrice(price);
        product.setStatus(status);
        product.setMerchantId(1L);
        return product;
    });
}
```

## 性能优化

### 流式处理大数据量

```java
@Service
public class ProductService {
    
    public File generateAllTemplate(Long merchantId) {
        // 使用流式查询避免一次性加载所有数据到内存
        File tempFile = File.createTempFile("meituan_template_", ".xlsx");
        
        try (FileOutputStream fos = new FileOutputStream(tempFile);
             SXSSFWorkbook workbook = new SXSSFWorkbook(100)) { // 内存中保留100行
            
            Sheet sheet = workbook.createSheet("商品数据");
            AtomicInteger rowNum = new AtomicInteger(0);
            
            // 写入表头
            createHeaderRow(sheet, rowNum.getAndIncrement());
            
            // 流式查询并写入数据
            productMapper.streamAll(merchantId, resultContext -> {
                Product product = resultContext.getResultObject();
                createDataRow(sheet, rowNum.getAndIncrement(), product);
            });
            
            workbook.write(fos);
        }
        
        return tempFile;
    }
}
```

### 前端异步处理

```typescript
const handleGenerateTemplate = async () => {
  // 显示loading
  generating.value = true;
  
  try {
    // 使用异步下载，不阻塞UI
    const response = await api.generateAllTemplate({
      merchantId: 1
    });
    
    // 创建下载链接
    const blob = new Blob([response], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `meituan_all_products_${Date.now()}.xlsx`;
    
    // 触发下载
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    // 刷新统计和历史
    await Promise.all([
      fetchStats(),
      fetchOperationHistory()
    ]);
    
    ElMessage.success({
      message: `成功生成包含 ${stats.value.totalCount} 个商品的模板`,
      duration: 3000
    });
  } catch (error) {
    handleError(error);
  } finally {
    generating.value = false;
  }
};
```

### 性能目标

- 1000条商品：≤ 10秒
- 10000条商品：≤ 60秒
- 内存使用：≤ 500MB（无论商品数量）
- UI响应时间：≤ 200ms

## UI设计

### 页面布局

```
┌─────────────────────────────────────────────────────────┐
│                     批量上传页面                          │
├─────────────────────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │ 商品总数  │  │ 待上传   │  │ 已上传   │              │
│  │  1,234   │  │   800    │  │   434    │              │
│  └──────────┘  └──────────┘  └──────────┘              │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────┐   │
│  │           生成全部商品模板                       │   │
│  │                                                  │   │
│  │  [生成全部商品模板] 按钮（大号、突出）           │   │
│  │                                                  │   │
│  │  最近导入的商品（预览）                          │   │
│  │  ┌────────────────────────────────────────┐    │   │
│  │  │ 商品1 | 类目ID | ¥99.99 | 待上传       │    │   │
│  │  │ 商品2 | 类目ID | ¥88.88 | 待上传       │    │   │
│  │  │ ...                                     │    │   │
│  │  └────────────────────────────────────────┘    │   │
│  │  [查看全部商品 →]                               │   │
│  └─────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────┐   │
│  │           操作历史                               │   │
│  │  ┌────────────────────────────────────────┐    │   │
│  │  │ 2026-02-09 10:30 | 1000个商品 | 成功   │    │   │
│  │  │ 2026-02-09 09:15 | 800个商品  | 成功   │    │   │
│  │  │ 2026-02-08 16:45 | 1200个商品 | 成功   │    │   │
│  │  └────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 响应式设计

```css
/* 桌面端（>1200px） */
.upload-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

/* 平板端（768px - 1200px） */
@media (max-width: 1200px) {
  .upload-container {
    padding: 16px;
  }
  
  .stats-row {
    grid-template-columns: repeat(3, 1fr);
  }
}

/* 移动端（<768px） */
@media (max-width: 768px) {
  .upload-container {
    padding: 12px;
  }
  
  .stats-row {
    grid-template-columns: 1fr;
  }
  
  .action-card {
    padding: 16px;
  }
}
```

## 部署和维护

### 前端部署

无需额外部署，直接替换现有的Upload.vue组件。

### 后端部署

1. 添加新的API端点
2. 更新ProductMapper添加新的查询方法
3. 重启后端服务

### 数据库迁移

无需数据库结构变更，使用现有表结构。

### 向后兼容性

- 保留原有的选择性生成模板API
- 新增全部商品生成API作为补充
- 前端可以根据需要切换使用

## 安全考虑

### 访问控制

```java
@PreAuthorize("hasRole('MERCHANT')")
@PostMapping("/generate-all-template")
public ResponseEntity<Resource> generateAllTemplate(
    @RequestParam Long merchantId,
    Authentication authentication
) {
    // 验证用户只能生成自己的商品模板
    if (!authService.canAccessMerchant(authentication, merchantId)) {
        throw new AccessDeniedException("无权访问该商家数据");
    }
    
    File template = productService.generateAllTemplate(merchantId);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=" + template.getName())
        .body(new FileSystemResource(template));
}
```

### 资源限制

```java
// 限制单次生成的最大商品数量
private static final int MAX_PRODUCTS_PER_GENERATION = 50000;

public File generateAllTemplate(Long merchantId) {
    long count = productMapper.selectCount(
        new QueryWrapper<Product>().eq("merchant_id", merchantId)
    );
    
    if (count > MAX_PRODUCTS_PER_GENERATION) {
        throw new BusinessException(
            "商品数量超过限制（" + MAX_PRODUCTS_PER_GENERATION + "），" +
            "请联系管理员或分批处理"
        );
    }
    
    // 继续处理...
}
```

### 临时文件清理

```java
@Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
public void cleanupTempFiles() {
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    File[] tempFiles = tempDir.listFiles(
        (dir, name) -> name.startsWith("meituan_template_")
    );
    
    if (tempFiles != null) {
        for (File file : tempFiles) {
            // 删除超过24小时的临时文件
            if (System.currentTimeMillis() - file.lastModified() > 86400000) {
                file.delete();
            }
        }
    }
}
```
