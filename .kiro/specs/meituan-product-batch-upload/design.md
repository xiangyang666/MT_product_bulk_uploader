# 设计文档

## 概述

美团商品批量上传管理工具是一个基于前后端分离架构的PC端应用，旨在帮助商家高效地批量管理和上传商品到美团平台。系统由Electron桌面客户端和Spring Boot后端服务组成，通过RESTful API进行通信。

### 核心功能
- Excel文件解析和数据导入
- 美团上传模板自动生成
- 批量商品上传到美团平台
- 一键清空美团后台商品
- 操作历史记录和数据持久化

### 技术栈
**前端：**
- Electron：跨平台桌面应用框架
- Vue 3：渐进式JavaScript框架
- Vite：现代化前端构建工具
- Element Plus：UI组件库
- Axios：HTTP客户端

**后端：**
- Spring Boot 3：Java应用框架
- MyBatis Plus：持久层框架
- MySQL 8：关系型数据库
- Apache POI：Excel文件处理
- Spring Security：安全框架

## 架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      Electron 客户端                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Vue 3 UI    │  │  状态管理     │  │  IPC通信     │      │
│  │  Components  │  │  (Pinia)     │  │  模块        │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │ HTTPS/REST API
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot 后端服务                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Controller  │  │   Service    │  │  Repository  │      │
│  │    层        │  │     层       │  │     层       │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                 │                  │              │
│         └─────────────────┼──────────────────┘              │
│                           ▼                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Excel处理   │  │  美团API     │  │  MySQL       │      │
│  │  模块        │  │  集成模块    │  │  数据库      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │ HTTPS
                            ▼
                   ┌──────────────────┐
                   │   美团开放平台    │
                   └──────────────────┘
```

### 分层架构

**前端架构（Electron + Vue 3）：**
1. **表现层（Presentation Layer）**
   - Vue组件：负责UI渲染和用户交互
   - 路由管理：页面导航和状态管理
   
2. **业务逻辑层（Business Logic Layer）**
   - Pinia Store：全局状态管理
   - API Service：封装HTTP请求
   
3. **通信层（Communication Layer）**
   - Electron IPC：主进程与渲染进程通信
   - Axios拦截器：请求/响应处理

**后端架构（Spring Boot）：**
1. **控制层（Controller Layer）**
   - REST API端点
   - 请求验证和响应封装
   
2. **服务层（Service Layer）**
   - 业务逻辑处理
   - 事务管理
   
3. **持久层（Repository Layer）**
   - MyBatis Plus数据访问
   - 数据库操作
   
4. **集成层（Integration Layer）**
   - 美团API客户端
   - Excel处理工具

## 组件和接口

### 前端组件

#### 1. 主窗口组件（MainWindow）
- **职责：** 应用主框架，包含导航和内容区域
- **子组件：** 
  - NavigationBar：导航栏
  - ContentArea：内容展示区

#### 2. 文件导入组件（FileImport）
- **职责：** 处理Excel文件选择和上传
- **接口：**
  ```typescript
  interface FileImportProps {
    onFileSelected: (file: File) => void;
    acceptedFormats: string[];
  }
  ```

#### 3. 数据表格组件（ProductTable）
- **职责：** 展示导入的商品数据
- **接口：**
  ```typescript
  interface ProductTableProps {
    products: Product[];
    loading: boolean;
    onEdit: (product: Product) => void;
  }
  ```

#### 4. 模板生成组件（TemplateGenerator）
- **职责：** 触发模板生成和下载
- **接口：**
  ```typescript
  interface TemplateGeneratorProps {
    productCount: number;
    onGenerate: () => Promise<void>;
  }
  ```

#### 5. 批量上传组件（BatchUpload）
- **职责：** 执行批量上传操作
- **接口：**
  ```typescript
  interface BatchUploadProps {
    products: Product[];
    onUploadComplete: (result: UploadResult) => void;
  }
  ```

### 后端组件

#### 1. ProductController
- **职责：** 处理商品相关的HTTP请求
- **端点：**
  ```java
  POST /api/products/import - 导入商品数据
  GET /api/products - 获取商品列表
  POST /api/products/generate-template - 生成美团模板
  POST /api/products/upload - 批量上传到美团
  DELETE /api/products/clear - 清空商品
  ```

#### 2. ProductService
- **职责：** 商品业务逻辑处理
- **方法：**
  ```java
  List<Product> importFromExcel(MultipartFile file)
  File generateMeituanTemplate(List<Long> productIds)
  UploadResult uploadToMeituan(List<Long> productIds)
  void clearAllProducts(Long merchantId)
  ```

#### 3. ExcelService
- **职责：** Excel文件解析和生成
- **方法：**
  ```java
  List<Product> parseExcel(InputStream inputStream, String fileType)
  File generateExcel(List<Product> products, TemplateConfig config)
  boolean validateExcelFormat(MultipartFile file)
  ```

#### 4. MeituanApiClient
- **职责：** 与美团开放平台API交互
- **方法：**
  ```java
  ApiResponse uploadProducts(List<ProductDTO> products, String accessToken)
  ApiResponse deleteAllProducts(String merchantId, String accessToken)
  boolean validateAccessToken(String accessToken)
  ```

#### 5. ProductRepository
- **职责：** 数据库访问层
- **方法：**
  ```java
  int batchInsert(List<Product> products)
  List<Product> selectByMerchantId(Long merchantId)
  int deleteByMerchantId(Long merchantId)
  ```

### API接口定义

#### 1. 导入商品数据
```
POST /api/products/import
Content-Type: multipart/form-data

Request:
- file: Excel文件
- merchantId: 商家ID

Response:
{
  "code": 200,
  "message": "导入成功",
  "data": {
    "totalCount": 100,
    "successCount": 98,
    "failedCount": 2,
    "products": [...],
    "errors": [...]
  }
}
```

#### 2. 生成美团模板
```
POST /api/products/generate-template
Content-Type: application/json

Request:
{
  "productIds": [1, 2, 3],
  "merchantId": 123
}

Response:
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "fileUrl": "/downloads/template_20240209.xlsx",
    "fileName": "meituan_template_20240209.xlsx"
  }
}
```

#### 3. 批量上传到美团
```
POST /api/products/upload
Content-Type: application/json

Request:
{
  "productIds": [1, 2, 3],
  "merchantId": 123,
  "accessToken": "xxx"
}

Response:
{
  "code": 200,
  "message": "上传完成",
  "data": {
    "totalCount": 100,
    "successCount": 95,
    "failedCount": 5,
    "failedProducts": [...],
    "duration": 120000
  }
}
```

#### 4. 清空商品
```
DELETE /api/products/clear
Content-Type: application/json

Request:
{
  "merchantId": 123,
  "accessToken": "xxx"
}

Response:
{
  "code": 200,
  "message": "清空成功",
  "data": {
    "deletedCount": 500
  }
}
```

## 数据模型

### 数据库表设计

#### 1. 商品表（t_product）
```sql
CREATE TABLE t_product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  merchant_id BIGINT NOT NULL COMMENT '商家ID',
  product_name VARCHAR(255) NOT NULL COMMENT '商品名称',
  category_id VARCHAR(50) NOT NULL COMMENT '类目ID',
  price DECIMAL(10, 2) NOT NULL COMMENT '价格',
  stock INT DEFAULT 0 COMMENT '库存',
  description TEXT COMMENT '商品描述',
  image_url VARCHAR(500) COMMENT '商品图片URL',
  status TINYINT DEFAULT 0 COMMENT '状态：0-待上传，1-已上传，2-上传失败',
  meituan_product_id VARCHAR(100) COMMENT '美团商品ID',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_merchant_id (merchant_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 2. 操作日志表（t_operation_log）
```sql
CREATE TABLE t_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  merchant_id BIGINT NOT NULL COMMENT '商家ID',
  operation_type VARCHAR(50) NOT NULL COMMENT '操作类型：IMPORT/GENERATE/UPLOAD/CLEAR',
  operation_detail TEXT COMMENT '操作详情',
  success_count INT DEFAULT 0 COMMENT '成功数量',
  failed_count INT DEFAULT 0 COMMENT '失败数量',
  duration BIGINT COMMENT '耗时（毫秒）',
  status TINYINT DEFAULT 0 COMMENT '状态：0-进行中，1-成功，2-失败',
  error_message TEXT COMMENT '错误信息',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_merchant_id (merchant_id),
  INDEX idx_operation_type (operation_type),
  INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 3. 商家配置表（t_merchant_config）
```sql
CREATE TABLE t_merchant_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  merchant_id BIGINT NOT NULL UNIQUE COMMENT '商家ID',
  merchant_name VARCHAR(255) NOT NULL COMMENT '商家名称',
  meituan_app_key VARCHAR(255) COMMENT '美团AppKey',
  meituan_app_secret VARCHAR(255) COMMENT '美团AppSecret',
  access_token VARCHAR(500) COMMENT '访问令牌',
  token_expire_time DATETIME COMMENT '令牌过期时间',
  template_config JSON COMMENT '模板配置',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 实体类定义

#### Product实体
```java
@Data
@TableName("t_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long merchantId;
    private String productName;
    private String categoryId;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String imageUrl;
    private Integer status;
    private String meituanProductId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
```

#### OperationLog实体
```java
@Data
@TableName("t_operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long merchantId;
    private String operationType;
    private String operationDetail;
    private Integer successCount;
    private Integer failedCount;
    private Long duration;
    private Integer status;
    private String errorMessage;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
```

### 前端数据模型

```typescript
// 商品接口
interface Product {
  id: number;
  merchantId: number;
  productName: string;
  categoryId: string;
  price: number;
  stock: number;
  description?: string;
  imageUrl?: string;
  status: ProductStatus;
  meituanProductId?: string;
  createdTime: string;
  updatedTime: string;
}

// 商品状态枚举
enum ProductStatus {
  PENDING = 0,    // 待上传
  UPLOADED = 1,   // 已上传
  FAILED = 2      // 上传失败
}

// 上传结果接口
interface UploadResult {
  totalCount: number;
  successCount: number;
  failedCount: number;
  failedProducts: Product[];
  duration: number;
}

// API响应接口
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}
```


## 正确性属性

*属性是指在系统所有有效执行中都应该成立的特征或行为——本质上是关于系统应该做什么的正式声明。属性是人类可读规范和机器可验证正确性保证之间的桥梁。*

### 属性 1：Excel解析往返一致性
*对于任何*有效的商品数据集合，如果将其写入Excel文件然后再解析回来，应该得到等价的商品数据
**验证需求：1.1, 1.2**

### 属性 2：无效文件格式拒绝
*对于任何*非Excel格式的文件（如txt、pdf、jpg等），系统应该拒绝该文件并返回错误信息
**验证需求：1.4**

### 属性 3：无效数据行标记
*对于任何*包含空白行或缺失必填字段的Excel文件，系统应该标记所有无效数据行并返回验证错误列表
**验证需求：1.5**

### 属性 4：模板生成数据完整性
*对于任何*商品数据集合，生成的美团上传模板应该包含所有商品的名称、类目ID、价格等必填字段
**验证需求：2.1, 2.2**

### 属性 5：UI数据展示完整性
*对于任何*商品数据列表，前端表格渲染应该包含所有商品的名称、类目ID、价格字段
**验证需求：1.3, 5.2**

### 属性 6：数据验证一致性
*对于任何*上传模板文件，如果数据验证通过，则文件中所有商品记录都应该满足美团平台的数据格式要求
**验证需求：3.1**

### 属性 7：部分失败继续处理
*对于任何*商品批量上传操作，如果部分商品上传失败，系统应该继续处理剩余商品并记录所有失败的商品信息
**验证需求：3.6**

### 属性 8：上传结果反馈完整性
*对于任何*上传操作结果，前端应该显示总数量、成功数量、失败数量和失败商品列表
**验证需求：3.4, 3.5**

### 属性 9：取消操作数据不变性
*对于任何*清空商品操作，如果商家在确认对话框中取消，则数据库中的商品数据应该保持不变
**验证需求：4.5**

### 属性 10：HTTPS通信加密
*对于任何*前端与后端的通信请求，应该使用HTTPS协议进行加密传输
**验证需求：6.1, 6.2**

### 属性 11：配置文件驱动模板格式
*对于任何*模板格式配置的修改，系统应该能够在不修改代码的情况下生成新格式的模板
**验证需求：7.2**

### 属性 12：操作日志记录
*对于任何*导入、上传或清空操作，系统应该在数据库中记录操作日志，包含操作类型、成功数量、失败数量和时间戳
**验证需求：7.4, 8.2**

### 属性 13：数据持久化往返
*对于任何*导入的商品数据，存储到数据库后再查询出来应该得到等价的商品信息
**验证需求：8.1, 8.3**

## 错误处理

### 错误分类

#### 1. 客户端错误（4xx）
- **400 Bad Request**：请求参数错误或数据格式不正确
- **401 Unauthorized**：未授权访问，缺少或无效的访问令牌
- **404 Not Found**：请求的资源不存在
- **413 Payload Too Large**：上传文件超过大小限制

#### 2. 服务器错误（5xx）
- **500 Internal Server Error**：服务器内部错误
- **502 Bad Gateway**：美团API调用失败
- **503 Service Unavailable**：服务暂时不可用

### 错误处理策略

#### 前端错误处理
```typescript
// 全局错误拦截器
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 400:
          ElMessage.error('请求参数错误，请检查输入');
          break;
        case 401:
          ElMessage.error('未授权访问，请重新登录');
          router.push('/login');
          break;
        case 413:
          ElMessage.error('文件过大，请选择小于10MB的文件');
          break;
        case 500:
          ElMessage.error('服务器错误，请稍后重试');
          break;
        case 502:
          ElMessage.error('美团平台连接失败，请检查网络');
          break;
        default:
          ElMessage.error('操作失败，请重试');
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络设置');
    }
    return Promise.reject(error);
  }
);
```

#### 后端错误处理
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(FileFormatException.class)
    public ResponseEntity<ApiResponse> handleFileFormatException(FileFormatException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(400, "文件格式错误：" + e.getMessage()));
    }
    
    @ExceptionHandler(DataValidationException.class)
    public ResponseEntity<ApiResponse> handleDataValidationException(DataValidationException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(400, "数据验证失败：" + e.getMessage()));
    }
    
    @ExceptionHandler(MeituanApiException.class)
    public ResponseEntity<ApiResponse> handleMeituanApiException(MeituanApiException e) {
        return ResponseEntity.status(502)
            .body(ApiResponse.error(502, "美团API调用失败：" + e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception e) {
        log.error("未处理的异常", e);
        return ResponseEntity.status(500)
            .body(ApiResponse.error(500, "系统错误，请联系管理员"));
    }
}
```

### 业务异常定义

```java
// 文件格式异常
public class FileFormatException extends RuntimeException {
    public FileFormatException(String message) {
        super(message);
    }
}

// 数据验证异常
public class DataValidationException extends RuntimeException {
    private List<ValidationError> errors;
    
    public DataValidationException(String message, List<ValidationError> errors) {
        super(message);
        this.errors = errors;
    }
}

// 美团API异常
public class MeituanApiException extends RuntimeException {
    private String apiErrorCode;
    
    public MeituanApiException(String message, String apiErrorCode) {
        super(message);
        this.apiErrorCode = apiErrorCode;
    }
}
```

### 重试机制

对于可能因网络波动导致的临时性失败，实现指数退避重试策略：

```java
@Service
public class MeituanApiClient {
    
    @Retryable(
        value = {MeituanApiException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public ApiResponse uploadProducts(List<ProductDTO> products, String accessToken) {
        // 调用美团API
    }
}
```

### 事务管理

确保数据一致性，使用Spring事务管理：

```java
@Service
public class ProductService {
    
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importFromExcel(MultipartFile file) {
        // 1. 解析Excel
        List<Product> products = excelService.parseExcel(file);
        
        // 2. 批量插入数据库
        productRepository.batchInsert(products);
        
        // 3. 记录操作日志
        operationLogService.log(OperationType.IMPORT, products.size());
        
        return ImportResult.success(products);
    }
}
```

## 测试策略

### 测试框架选择

**后端测试：**
- JUnit 5：单元测试框架
- Mockito：Mock框架
- jqwik：属性测试框架（Java的Property-Based Testing库）
- Spring Boot Test：集成测试支持
- H2 Database：内存数据库用于测试

**前端测试：**
- Vitest：单元测试框架
- @vue/test-utils：Vue组件测试工具
- fast-check：属性测试框架（JavaScript/TypeScript的Property-Based Testing库）
- Playwright：端到端测试框架

### 单元测试

单元测试覆盖具体示例、边界情况和错误条件：

**后端单元测试示例：**
```java
@Test
void testParseExcel_WithValidXlsxFile_ShouldReturnProducts() {
    // Given
    MultipartFile file = createMockXlsxFile();
    
    // When
    List<Product> products = excelService.parseExcel(file);
    
    // Then
    assertThat(products).hasSize(10);
    assertThat(products.get(0).getProductName()).isEqualTo("测试商品");
}

@Test
void testImportFromExcel_WithEmptyRows_ShouldMarkInvalidRows() {
    // Given
    MultipartFile file = createFileWithEmptyRows();
    
    // When
    ImportResult result = productService.importFromExcel(file);
    
    // Then
    assertThat(result.getFailedCount()).isEqualTo(2);
    assertThat(result.getErrors()).hasSize(2);
}
```

**前端单元测试示例：**
```typescript
describe('ProductTable', () => {
  it('should display all products', () => {
    const products = [
      { id: 1, productName: '商品1', price: 100 },
      { id: 2, productName: '商品2', price: 200 }
    ];
    
    const wrapper = mount(ProductTable, {
      props: { products }
    });
    
    expect(wrapper.findAll('tr')).toHaveLength(2);
  });
  
  it('should show error message for invalid file format', async () => {
    const wrapper = mount(FileImport);
    const file = new File(['content'], 'test.txt', { type: 'text/plain' });
    
    await wrapper.vm.handleFileUpload(file);
    
    expect(wrapper.find('.error-message').text()).toContain('文件格式错误');
  });
});
```

### 属性测试

属性测试验证跨所有输入的通用属性。每个属性测试应该运行至少100次迭代。

**后端属性测试配置：**
```java
// 使用jqwik进行属性测试
@Property(tries = 100)
void excelRoundTripConsistency(@ForAll List<@From("validProducts") Product> products) {
    /**
     * Feature: meituan-product-batch-upload, Property 1: Excel解析往返一致性
     */
    // Given: 任意有效的商品数据集合
    
    // When: 写入Excel然后解析回来
    File excelFile = excelService.generateExcel(products, templateConfig);
    List<Product> parsedProducts = excelService.parseExcel(
        new FileInputStream(excelFile), "xlsx"
    );
    
    // Then: 应该得到等价的商品数据
    assertThat(parsedProducts).usingRecursiveComparison()
        .ignoringFields("id", "createdTime", "updatedTime")
        .isEqualTo(products);
}

@Property(tries = 100)
void invalidFileFormatRejection(@ForAll @From("nonExcelFiles") File file) {
    /**
     * Feature: meituan-product-batch-upload, Property 2: 无效文件格式拒绝
     */
    // Given: 任意非Excel格式的文件
    
    // When & Then: 系统应该拒绝该文件
    assertThatThrownBy(() -> excelService.parseExcel(file))
        .isInstanceOf(FileFormatException.class)
        .hasMessageContaining("不支持的文件格式");
}

@Property(tries = 100)
void templateDataCompleteness(@ForAll List<@From("validProducts") Product> products) {
    /**
     * Feature: meituan-product-batch-upload, Property 4: 模板生成数据完整性
     */
    // Given: 任意商品数据集合
    
    // When: 生成美团上传模板
    File template = excelService.generateMeituanTemplate(products);
    List<Product> parsedProducts = excelService.parseExcel(
        new FileInputStream(template), "xlsx"
    );
    
    // Then: 模板应该包含所有必填字段
    assertThat(parsedProducts).allSatisfy(product -> {
        assertThat(product.getProductName()).isNotBlank();
        assertThat(product.getCategoryId()).isNotBlank();
        assertThat(product.getPrice()).isPositive();
    });
}

@Property(tries = 100)
void partialFailureContinuesProcessing(
    @ForAll @Size(min = 10, max = 100) List<@From("validProducts") Product> products,
    @ForAll @IntRange(min = 1, max = 5) int failureCount
) {
    /**
     * Feature: meituan-product-batch-upload, Property 7: 部分失败继续处理
     */
    // Given: 任意商品列表，模拟部分失败
    when(meituanApiClient.uploadProduct(any()))
        .thenReturn(ApiResponse.success())
        .thenThrow(new MeituanApiException("上传失败", "E001"))
        .thenReturn(ApiResponse.success());
    
    // When: 执行批量上传
    UploadResult result = productService.uploadToMeituan(products);
    
    // Then: 应该继续处理剩余商品并记录失败信息
    assertThat(result.getSuccessCount() + result.getFailedCount())
        .isEqualTo(products.size());
    assertThat(result.getFailedProducts()).isNotEmpty();
}

@Property(tries = 100)
void dataPersistenceRoundTrip(@ForAll List<@From("validProducts") Product> products) {
    /**
     * Feature: meituan-product-batch-upload, Property 13: 数据持久化往返
     */
    // Given: 任意商品数据
    
    // When: 存储到数据库后再查询
    productRepository.batchInsert(products);
    List<Product> retrievedProducts = productRepository.selectByMerchantId(
        products.get(0).getMerchantId()
    );
    
    // Then: 应该得到等价的商品信息
    assertThat(retrievedProducts).usingRecursiveComparison()
        .ignoringFields("id", "createdTime", "updatedTime")
        .isEqualTo(products);
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
        )
    ).as((name, categoryId, price) -> {
        Product product = new Product();
        product.setProductName(name);
        product.setCategoryId(categoryId);
        product.setPrice(price);
        product.setMerchantId(1L);
        return product;
    });
}

@Provide
Arbitrary<File> nonExcelFiles() {
    return Arbitraries.of("txt", "pdf", "jpg", "png", "doc")
        .map(ext -> createTempFile("test." + ext));
}
```

**前端属性测试配置：**
```typescript
import fc from 'fast-check';

describe('Property Tests', () => {
  it('Property 5: UI数据展示完整性', () => {
    /**
     * Feature: meituan-product-batch-upload, Property 5: UI数据展示完整性
     */
    fc.assert(
      fc.property(
        fc.array(productArbitrary(), { minLength: 1, maxLength: 100 }),
        (products) => {
          // Given: 任意商品数据列表
          const wrapper = mount(ProductTable, {
            props: { products }
          });
          
          // Then: 表格应该包含所有商品的必填字段
          const rows = wrapper.findAll('tbody tr');
          expect(rows).toHaveLength(products.length);
          
          products.forEach((product, index) => {
            const row = rows[index];
            expect(row.text()).toContain(product.productName);
            expect(row.text()).toContain(product.categoryId);
            expect(row.text()).toContain(product.price.toString());
          });
        }
      ),
      { numRuns: 100 }
    );
  });
  
  it('Property 8: 上传结果反馈完整性', () => {
    /**
     * Feature: meituan-product-batch-upload, Property 8: 上传结果反馈完整性
     */
    fc.assert(
      fc.property(
        uploadResultArbitrary(),
        (result) => {
          // Given: 任意上传结果
          const wrapper = mount(UploadResult, {
            props: { result }
          });
          
          // Then: 应该显示完整的结果信息
          expect(wrapper.text()).toContain(`总数：${result.totalCount}`);
          expect(wrapper.text()).toContain(`成功：${result.successCount}`);
          expect(wrapper.text()).toContain(`失败：${result.failedCount}`);
          
          if (result.failedCount > 0) {
            expect(wrapper.find('.failed-products-list').exists()).toBe(true);
          }
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
    merchantId: fc.constant(1)
  });
}

function uploadResultArbitrary() {
  return fc.record({
    totalCount: fc.integer({ min: 1, max: 1000 }),
    successCount: fc.integer({ min: 0, max: 1000 }),
    failedCount: fc.integer({ min: 0, max: 1000 })
  }).chain(base => {
    const total = base.totalCount;
    return fc.record({
      totalCount: fc.constant(total),
      successCount: fc.integer({ min: 0, max: total }),
      failedCount: fc.integer({ min: 0, max: total })
    }).filter(r => r.successCount + r.failedCount === total);
  });
}
```

### 集成测试

集成测试验证组件之间的交互：

```java
@SpringBootTest
@AutoConfigureMockMvc
class ProductIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCompleteWorkflow() throws Exception {
        // 1. 导入Excel文件
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "products.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            createTestExcelContent()
        );
        
        mockMvc.perform(multipart("/api/products/import")
                .file(file)
                .param("merchantId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.successCount").value(10));
        
        // 2. 生成模板
        mockMvc.perform(post("/api/products/generate-template")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productIds\":[1,2,3],\"merchantId\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.fileUrl").exists());
        
        // 3. 批量上传
        mockMvc.perform(post("/api/products/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productIds\":[1,2,3],\"merchantId\":1,\"accessToken\":\"xxx\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.successCount").exists());
    }
}
```

### 测试覆盖率目标

- 单元测试代码覆盖率：≥ 80%
- 属性测试覆盖所有核心业务逻辑
- 集成测试覆盖主要业务流程
- 端到端测试覆盖关键用户场景

## 性能考虑

### 性能目标

1. **应用启动时间**：≤ 5秒
2. **Excel解析性能**：1000条商品数据解析时间 ≤ 2秒
3. **批量上传性能**：1000条商品上传时间 ≤ 3分钟
4. **数据库查询性能**：单次查询响应时间 ≤ 100ms
5. **UI响应时间**：用户操作反馈时间 ≤ 200ms

### 性能优化策略

#### 1. 批量处理优化
```java
@Service
public class ProductService {
    
    private static final int BATCH_SIZE = 500;
    
    public UploadResult uploadToMeituan(List<Long> productIds) {
        List<Product> products = productRepository.selectBatchIds(productIds);
        
        // 分批上传，避免单次请求过大
        List<List<Product>> batches = Lists.partition(products, BATCH_SIZE);
        
        return batches.parallelStream()
            .map(batch -> uploadBatch(batch))
            .reduce(UploadResult::merge)
            .orElse(UploadResult.empty());
    }
}
```

#### 2. 数据库优化
- 使用批量插入减少数据库往返次数
- 添加适当的索引提升查询性能
- 使用连接池管理数据库连接

```java
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    @Insert("<script>" +
            "INSERT INTO t_product (merchant_id, product_name, category_id, price) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.merchantId}, #{item.productName}, #{item.categoryId}, #{item.price})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<Product> products);
}
```

#### 3. 前端性能优化
- 使用虚拟滚动处理大量数据展示
- 实现分页加载减少初始加载时间
- 使用Web Worker处理Excel解析避免阻塞UI

```typescript
// 虚拟滚动示例
<template>
  <el-table-v2
    :columns="columns"
    :data="products"
    :width="800"
    :height="600"
    fixed
  />
</template>
```

#### 4. 缓存策略
```java
@Service
public class MerchantConfigService {
    
    @Cacheable(value = "merchantConfig", key = "#merchantId")
    public MerchantConfig getConfig(Long merchantId) {
        return merchantConfigRepository.selectById(merchantId);
    }
}
```

## 安全考虑

### 数据传输安全
- 前后端通信强制使用HTTPS
- API请求使用JWT令牌认证
- 敏感数据加密存储

### 访问控制
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### 输入验证
```java
@PostMapping("/import")
public ResponseEntity<ApiResponse> importProducts(
    @RequestParam("file") @Valid @FileSize(max = 10 * 1024 * 1024) MultipartFile file,
    @RequestParam("merchantId") @NotNull Long merchantId
) {
    // 验证文件类型
    if (!isValidExcelFile(file)) {
        throw new FileFormatException("仅支持xlsx和xls格式");
    }
    
    // 处理导入
    ImportResult result = productService.importFromExcel(file, merchantId);
    return ResponseEntity.ok(ApiResponse.success(result));
}
```

## 部署架构

### 开发环境
- 前端：Electron开发模式 + Vite热更新
- 后端：Spring Boot内嵌Tomcat
- 数据库：本地MySQL 8

### 生产环境
```
┌─────────────────┐
│  Electron App   │
│  (客户端安装包)  │
└────────┬────────┘
         │ HTTPS
         ▼
┌─────────────────┐
│  Nginx反向代理   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Spring Boot应用  │
│  (JAR部署)      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  MySQL数据库     │
└─────────────────┘
```

### 打包部署

**前端打包：**
```bash
# 构建Electron应用
npm run build
npm run electron:build

# 生成安装包
# Windows: .exe
# macOS: .dmg
# Linux: .AppImage
```

**后端打包：**
```bash
# Maven打包
mvn clean package -DskipTests

# 生成可执行JAR
java -jar meituan-backend-1.0.0.jar
```

## 可维护性设计

### 配置外部化
```yaml
# application.yml
meituan:
  api:
    base-url: https://api.meituan.com
    timeout: 30000
  template:
    config-file: classpath:template-config.json
  upload:
    batch-size: 500
    max-retry: 3
```

### 日志管理
```java
@Slf4j
@Service
public class ProductService {
    
    public ImportResult importFromExcel(MultipartFile file) {
        log.info("开始导入Excel文件，文件名：{}", file.getOriginalFilename());
        
        try {
            List<Product> products = excelService.parseExcel(file);
            log.info("成功解析{}条商品数据", products.size());
            
            productRepository.batchInsert(products);
            log.info("成功导入{}条商品到数据库", products.size());
            
            return ImportResult.success(products);
        } catch (Exception e) {
            log.error("导入失败", e);
            throw new ImportException("导入失败：" + e.getMessage());
        }
    }
}
```

### 版本管理
- 使用语义化版本号：MAJOR.MINOR.PATCH
- 数据库迁移使用Flyway管理
- API版本化：/api/v1/products

```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    // API实现
}
```
