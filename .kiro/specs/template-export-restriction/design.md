# 设计文档 - 模板导出限制

## 概述

本设计文档描述了如何实现模板导出限制功能，确保商家删除美团模板后无法继续导出，必须重新上传模板。该功能涉及后端API修改、前端UI状态管理和用户体验优化。

### 设计目标

1. 移除默认模板回退机制，强制要求用户上传模板
2. 提供模板状态查询API，支持前端动态UI控制
3. 优化页面加载体验，避免误操作
4. 提供清晰的错误提示和操作指引

## 架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Vue.js)                        │
├─────────────────────────────────────────────────────────────┤
│  Upload.vue                                                  │
│  ├─ 页面加载时调用模板状态API                                 │
│  ├─ 根据模板状态控制按钮启用/禁用                              │
│  ├─ 显示loading状态和错误提示                                 │
│  └─ 点击导出按钮调用生成模板API                               │
├─────────────────────────────────────────────────────────────┤
│  Template.vue                                                │
│  ├─ 上传/删除模板后触发状态更新                               │
│  └─ 通过事件总线通知Upload.vue                                │
└─────────────────────────────────────────────────────────────┘
                              ↓ HTTP
┌─────────────────────────────────────────────────────────────┐
│                      后端层 (Spring Boot)                     │
├─────────────────────────────────────────────────────────────┤
│  TemplateController                                          │
│  ├─ GET /api/template/status - 查询模板状态                  │
│  └─ 返回 TemplateStatusDTO                                   │
├─────────────────────────────────────────────────────────────┤
│  ProductController                                           │
│  ├─ POST /api/product/generate-template                     │
│  └─ 调用模板检查 → 生成导出文件                              │
├─────────────────────────────────────────────────────────────┤
│  ExcelService                                                │
│  ├─ generateMeituanTemplateFromUserTemplate()               │
│  ├─ 移除默认模板回退逻辑                                      │
│  └─ 抛出 TemplateNotFoundException                           │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      数据层 (MySQL + MinIO)                   │
├─────────────────────────────────────────────────────────────┤
│  template 表 - 存储模板元数据                                 │
│  MinIO - 存储模板文件                                         │
└─────────────────────────────────────────────────────────────┘
```

## 组件和接口

### 1. 后端组件

#### 1.1 TemplateStatusDTO (新增)

模板状态数据传输对象

```java
public class TemplateStatusDTO {
    private Boolean hasTemplate;        // 是否有可用模板
    private String templateName;        // 模板名称
    private LocalDateTime uploadTime;   // 上传时间
    private Long fileSize;              // 文件大小（字节）
    private String templateType;        // 模板类型（MEITUAN）
}
```

#### 1.2 TemplateNotFoundException (新增)

模板未找到异常

```java
public class TemplateNotFoundException extends RuntimeException {
    private Long merchantId;
    
    public TemplateNotFoundException(Long merchantId) {
        super("未找到商家的美团模板，商家ID: " + merchantId);
        this.merchantId = merchantId;
    }
}
```

#### 1.3 TemplateController (修改)

新增模板状态查询接口

```java
@GetMapping("/status")
public ApiResponse<TemplateStatusDTO> getTemplateStatus(
    @RequestParam(value = "merchantId", defaultValue = "1") Long merchantId
) {
    TemplateStatusDTO status = templateService.getTemplateStatus(merchantId);
    return ApiResponse.success(status);
}
```

#### 1.4 TemplateService (修改)

新增模板状态查询方法

```java
public TemplateStatusDTO getTemplateStatus(Long merchantId) {
    // 查询最新的美团模板
    Template template = findLatestMeituanTemplate(merchantId);
    
    if (template == null) {
        return TemplateStatusDTO.builder()
            .hasTemplate(false)
            .build();
    }
    
    return TemplateStatusDTO.builder()
        .hasTemplate(true)
        .templateName(template.getTemplateName())
        .uploadTime(template.getCreatedTime())
        .fileSize(template.getFileSize())
        .templateType(template.getTemplateType())
        .build();
}
```


#### 1.5 ExcelService (修改)

移除默认模板回退逻辑

```java
public byte[] generateMeituanTemplateFromUserTemplate(List<Product> products, Long merchantId) {
    log.info("使用用户模板生成美团上传文件，商家ID：{}，商品数量：{}", merchantId, products.size());
    
    // 1. 查找商家的美团模板
    Template template = findLatestMeituanTemplate(merchantId);
    
    // 2. 如果没有找到用户模板，抛出异常（移除默认模板回退）
    if (template == null) {
        log.error("未找到商家的美团模板，商家ID：{}", merchantId);
        throw new TemplateNotFoundException(merchantId);
    }
    
    // 3. 从MinIO下载模板文件
    String objectName = getObjectNameFromTemplate(template);
    InputStream templateStream;
    try {
        templateStream = minioService.downloadFile(objectName);
    } catch (Exception e) {
        log.error("模板文件下载失败，商家ID：{}，文件：{}", merchantId, objectName, e);
        throw new TemplateFileException("模板文件丢失，请重新上传");
    }
    
    // 4. 读取和处理模板文件
    try {
        Workbook templateWorkbook = new XSSFWorkbook(templateStream);
        // ... 原有的模板处理逻辑 ...
        return outputStream.toByteArray();
    } catch (Exception e) {
        log.error("模板文件处理失败，商家ID：{}", merchantId, e);
        throw new TemplateFileException("模板文件损坏，请重新上传");
    }
}
```

#### 1.6 GlobalExceptionHandler (修改)

添加模板异常处理

```java
@ExceptionHandler(TemplateNotFoundException.class)
public ResponseEntity<ApiResponse<String>> handleTemplateNotFound(TemplateNotFoundException e) {
    log.error("模板未找到异常: {}", e.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("请先上传美团模板"));
}

@ExceptionHandler(TemplateFileException.class)
public ResponseEntity<ApiResponse<String>> handleTemplateFileError(TemplateFileException e) {
    log.error("模板文件异常: {}", e.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(e.getMessage()));
}
```

### 2. 前端组件

#### 2.1 Upload.vue (修改)

状态管理和UI控制

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import api from '@/api'

// 状态变量
const loading = ref(true)              // 页面加载状态
const templateStatus = ref({           // 模板状态
  hasTemplate: false,
  templateName: '',
  uploadTime: null,
  fileSize: 0
})
const generating = ref(false)          // 生成中状态
const stats = ref({                    // 商品统计
  totalCount: 0
})

const router = useRouter()

// 页面加载时获取模板状态和商品统计
onMounted(async () => {
  await Promise.all([
    fetchTemplateStatus(),
    fetchProductStats()
  ])
  loading.value = false
})

// 获取模板状态
const fetchTemplateStatus = async () => {
  try {
    const response = await api.get('/api/template/status', {
      params: { merchantId: 1 }
    })
    templateStatus.value = response.data
  } catch (error) {
    console.error('获取模板状态失败:', error)
    ElMessage.error('获取模板状态失败，请刷新页面重试')
  }
}

// 获取商品统计
const fetchProductStats = async () => {
  try {
    const response = await api.get('/api/product/stats', {
      params: { merchantId: 1 }
    })
    stats.value = response.data
  } catch (error) {
    console.error('获取商品统计失败:', error)
  }
}

// 生成模板按钮点击处理
const handleGenerateTemplate = async () => {
  // 检查是否有模板
  if (!templateStatus.value.hasTemplate) {
    ElMessageBox.confirm(
      '您还没有上传美团模板，无法生成导出文件。是否前往模板管理上传？',
      '提示',
      {
        confirmButtonText: '前往模板管理',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      router.push('/template')
    }).catch(() => {})
    return
  }
  
  // 检查是否有商品
  if (stats.value.totalCount === 0) {
    ElMessage.warning('暂无商品数据，无法生成模板')
    return
  }
  
  // 执行生成逻辑
  generating.value = true
  try {
    // ... 原有的生成逻辑 ...
  } catch (error) {
    if (error.response?.data?.message === '请先上传美团模板') {
      // 模板被删除了，刷新状态
      await fetchTemplateStatus()
      ElMessageBox.confirm(
        '模板文件不存在或已被删除，请重新上传。是否前往模板管理？',
        '错误',
        {
          confirmButtonText: '前往模板管理',
          cancelButtonText: '取消',
          type: 'error'
        }
      ).then(() => {
        router.push('/template')
      }).catch(() => {})
    } else {
      ElMessage.error('生成模板失败，请重试')
    }
  } finally {
    generating.value = false
  }
}

// 计算按钮是否禁用
const isButtonDisabled = computed(() => {
  return loading.value || 
         !templateStatus.value.hasTemplate || 
         stats.value.totalCount === 0 || 
         generating.value
})

// 计算提示文本
const tipText = computed(() => {
  if (loading.value) {
    return '正在加载...'
  }
  if (!templateStatus.value.hasTemplate) {
    return '请先在模板管理中上传美团模板'
  }
  if (stats.value.totalCount === 0) {
    return '暂无商品数据，请先导入商品'
  }
  return `将使用您在模板管理中上传的美团模板：${templateStatus.value.templateName}`
})
</script>

<template>
  <div class="upload-page">
    <div class="action-card">
      <div class="action-header">
        <h3>批量上传全部商品</h3>
        <p class="action-desc">一键生成包含所有商品的美团上传模板</p>
      </div>

      <div class="action-body">
        <!-- Loading 骨架屏 -->
        <div v-if="loading" class="skeleton-button">
          <div class="skeleton-shimmer"></div>
        </div>
        
        <!-- 生成按钮 -->
        <button
          v-else
          class="generate-btn"
          :class="{ 
            'is-loading': generating, 
            'is-disabled': isButtonDisabled 
          }"
          :disabled="isButtonDisabled"
          @click="handleGenerateTemplate"
          :title="!templateStatus.hasTemplate ? '请先上传美团模板' : ''"
        >
          <span v-if="generating" class="btn-loading">
            <span class="loading-spinner"></span>
            生成中...
          </span>
          <span v-else class="btn-content">
            <el-icon><Download /></el-icon>
            生成全部商品模板
          </span>
        </button>

        <!-- 提示信息 -->
        <div v-if="!loading" class="tip-box" 
             :class="{ 
               'empty': stats.totalCount === 0,
               'warning': !templateStatus.hasTemplate,
               'success': templateStatus.hasTemplate && stats.totalCount > 0
             }">
          <el-icon><InfoFilled /></el-icon>
          <span>{{ tipText }}</span>
          <a v-if="!templateStatus.hasTemplate" 
             @click="router.push('/template')" 
             class="tip-link">
            前往上传 →
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.skeleton-button {
  width: 100%;
  height: 48px;
  background: #f0f0f0;
  border-radius: 8px;
  position: relative;
  overflow: hidden;
}

.skeleton-shimmer {
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.5),
    transparent
  );
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.tip-box.warning {
  background: #fff7e6;
  border-color: #ffa940;
  color: #d46b08;
}

.tip-link {
  margin-left: 8px;
  color: #1890ff;
  cursor: pointer;
  text-decoration: none;
}

.tip-link:hover {
  text-decoration: underline;
}
</style>
```



## 数据模型

### 1. TemplateStatusDTO

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateStatusDTO {
    /**
     * 是否有可用的美团模板
     */
    private Boolean hasTemplate;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 模板类型
     */
    private String templateType;
}
```

### 2. 异常类

```java
/**
 * 模板未找到异常
 */
public class TemplateNotFoundException extends RuntimeException {
    private Long merchantId;
    
    public TemplateNotFoundException(Long merchantId) {
        super("未找到商家的美团模板，商家ID: " + merchantId);
        this.merchantId = merchantId;
    }
    
    public Long getMerchantId() {
        return merchantId;
    }
}

/**
 * 模板文件异常
 */
public class TemplateFileException extends RuntimeException {
    public TemplateFileException(String message) {
        super(message);
    }
    
    public TemplateFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 3. API响应格式

#### 3.1 模板状态查询响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "hasTemplate": true,
    "templateName": "美团模板_v1.3.7.xlsx",
    "uploadTime": "2026-02-25 14:30:00",
    "fileSize": 15360,
    "templateType": "MEITUAN"
  }
}
```

#### 3.2 无模板时的响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "hasTemplate": false,
    "templateName": null,
    "uploadTime": null,
    "fileSize": null,
    "templateType": null
  }
}
```

#### 3.3 导出失败响应

```json
{
  "code": 400,
  "message": "请先上传美团模板",
  "data": null
}
```

## 正确性属性

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### 属性分析

基于需求文档的验收标准，我们识别出以下可测试的正确性属性：

**Property 1: 无模板时导出被拒绝**
*For any* 没有美团模板的商家，尝试导出商品数据时，系统应该拒绝请求并抛出TemplateNotFoundException
**Validates: Requirements 1.2, 2.2**

**Property 2: 导出拒绝时返回明确错误**
*For any* 因缺少模板而被拒绝的导出请求，系统返回的错误消息应该包含"请先上传美团模板"
**Validates: Requirements 1.3**

**Property 3: 导出前必须检查模板**
*For any* 导出请求，系统在执行导出逻辑之前，必须先调用模板检查方法
**Validates: Requirements 2.1**

**Property 4: 有模板时导出成功**
*For any* 有有效美团模板的商家，导出请求应该成功执行并返回Excel文件字节数组
**Validates: Requirements 2.5**

**Property 5: 移除默认模板回退**
*For any* 查找用户模板失败的情况，系统应该抛出TemplateNotFoundException，而不是使用默认模板
**Validates: Requirements 5.1**

**Property 6: 文件读取失败抛出异常**
*For any* 模板文件读取失败的情况，系统应该抛出TemplateFileException，而不是使用默认模板
**Validates: Requirements 5.2**

**Property 7: 异常包含详细信息**
*For any* 模板相关异常，异常消息应该包含商家ID或文件路径等详细信息
**Validates: Requirements 5.4**

**Property 8: 异常返回HTTP 400**
*For any* 模板相关异常被捕获时，HTTP响应状态码应该是400，且响应体包含错误详情
**Validates: Requirements 5.5**

**Property 9: 模板状态API返回正确格式**
*For any* 商家ID，调用模板状态API应该返回包含hasTemplate字段的TemplateStatusDTO对象
**Validates: Requirements 6.1**

**Property 10: 有模板时返回完整信息**
*For any* 有美团模板的商家，模板状态API应该返回hasTemplate=true，以及templateName、uploadTime、fileSize字段
**Validates: Requirements 6.2**

**Property 11: 无模板时返回false**
*For any* 没有美团模板的商家，模板状态API应该返回hasTemplate=false
**Validates: Requirements 6.3**

## 错误处理

### 1. 异常类型和处理策略

| 异常类型 | 触发条件 | HTTP状态码 | 错误消息 | 处理策略 |
|---------|---------|-----------|---------|---------|
| TemplateNotFoundException | 商家没有美团模板 | 400 | 请先上传美团模板 | 提示用户上传模板 |
| TemplateFileException | 模板文件损坏 | 400 | 模板文件损坏，请重新上传 | 提示用户重新上传 |
| TemplateFileException | 模板文件丢失 | 400 | 模板文件丢失，请重新上传 | 提示用户重新上传 |
| MinioException | MinIO服务异常 | 500 | 文件存储服务异常 | 记录日志，提示稍后重试 |

### 2. 错误处理流程

```
导出请求
    ↓
检查商家是否有模板
    ↓
    ├─ 无模板 → 抛出 TemplateNotFoundException → 返回 400 "请先上传美团模板"
    ↓
    └─ 有模板 → 继续
         ↓
    下载模板文件
         ↓
         ├─ 文件不存在 → 抛出 TemplateFileException → 返回 400 "模板文件丢失"
         ↓
         └─ 文件存在 → 继续
              ↓
         读取模板文件
              ↓
              ├─ 文件损坏 → 抛出 TemplateFileException → 返回 400 "模板文件损坏"
              ↓
              └─ 读取成功 → 继续
                   ↓
              生成导出文件
                   ↓
              返回 Excel 字节数组
```

### 3. 日志记录

所有错误场景都应该记录详细日志：

```java
// 模板未找到
log.error("未找到商家的美团模板，商家ID：{}，请求来源：{}", merchantId, requestSource);

// 文件下载失败
log.error("模板文件下载失败，商家ID：{}，文件路径：{}，错误：{}", 
    merchantId, objectName, e.getMessage(), e);

// 文件读取失败
log.error("模板文件读取失败，商家ID：{}，文件大小：{}，错误：{}", 
    merchantId, fileSize, e.getMessage(), e);
```

## 测试策略

### 1. 单元测试

#### 1.1 TemplateService测试

```java
@Test
void testGetTemplateStatus_WithTemplate() {
    // 有模板的情况
    Template template = createMockTemplate();
    when(templateMapper.selectOne(any())).thenReturn(template);
    
    TemplateStatusDTO status = templateService.getTemplateStatus(1L);
    
    assertTrue(status.getHasTemplate());
    assertEquals("美团模板.xlsx", status.getTemplateName());
    assertNotNull(status.getUploadTime());
}

@Test
void testGetTemplateStatus_WithoutTemplate() {
    // 无模板的情况
    when(templateMapper.selectOne(any())).thenReturn(null);
    
    TemplateStatusDTO status = templateService.getTemplateStatus(1L);
    
    assertFalse(status.getHasTemplate());
    assertNull(status.getTemplateName());
}
```

#### 1.2 ExcelService测试

```java
@Test
void testGenerateTemplate_ThrowsExceptionWhenNoTemplate() {
    // 测试无模板时抛出异常
    when(templateMapper.selectOne(any())).thenReturn(null);
    
    assertThrows(TemplateNotFoundException.class, () -> {
        excelService.generateMeituanTemplateFromUserTemplate(products, 1L);
    });
    
    // 验证没有调用默认模板方法
    verify(excelService, never()).generateMeituanTemplate(any());
}

@Test
void testGenerateTemplate_ThrowsExceptionWhenFileNotFound() {
    // 测试文件不存在时抛出异常
    Template template = createMockTemplate();
    when(templateMapper.selectOne(any())).thenReturn(template);
    when(minioService.downloadFile(any())).thenThrow(new MinioException("File not found"));
    
    TemplateFileException exception = assertThrows(TemplateFileException.class, () -> {
        excelService.generateMeituanTemplateFromUserTemplate(products, 1L);
    });
    
    assertTrue(exception.getMessage().contains("模板文件丢失"));
}
```

### 2. 集成测试

#### 2.1 API集成测试

```java
@Test
void testGenerateTemplate_ReturnsErrorWhenNoTemplate() {
    // 删除所有模板
    templateMapper.delete(new LambdaQueryWrapper<Template>()
        .eq(Template::getMerchantId, 1L));
    
    // 尝试导出
    ResponseEntity<byte[]> response = restTemplate.postForEntity(
        "/api/product/generate-template",
        generateRequest,
        byte[].class
    );
    
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
}

@Test
void testTemplateStatusAPI() {
    // 测试模板状态API
    ResponseEntity<ApiResponse<TemplateStatusDTO>> response = restTemplate.getForEntity(
        "/api/template/status?merchantId=1",
        new ParameterizedTypeReference<ApiResponse<TemplateStatusDTO>>() {}
    );
    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody().getData());
}
```

### 3. 前端测试

#### 3.1 组件测试

```javascript
describe('Upload.vue', () => {
  it('should disable button when no template', async () => {
    // Mock API返回无模板
    mockApi.get.mockResolvedValue({ data: { hasTemplate: false } })
    
    const wrapper = mount(Upload)
    await flushPromises()
    
    const button = wrapper.find('.generate-btn')
    expect(button.attributes('disabled')).toBe('true')
    expect(wrapper.text()).toContain('请先在模板管理中上传美团模板')
  })
  
  it('should enable button when template exists', async () => {
    // Mock API返回有模板
    mockApi.get.mockResolvedValue({ 
      data: { 
        hasTemplate: true,
        templateName: '美团模板.xlsx'
      } 
    })
    
    const wrapper = mount(Upload)
    await flushPromises()
    
    const button = wrapper.find('.generate-btn')
    expect(button.attributes('disabled')).toBeFalsy()
  })
  
  it('should show loading state on mount', () => {
    const wrapper = mount(Upload)
    
    expect(wrapper.find('.skeleton-button').exists()).toBe(true)
  })
})
```

## 性能考虑

### 1. 模板状态查询优化

- 使用数据库索引：在`merchant_id`和`template_type`字段上建立复合索引
- 查询限制：使用`LIMIT 1`只查询最新的一条记录
- 缓存策略：考虑在Redis中缓存模板状态，TTL设置为5分钟

```sql
CREATE INDEX idx_merchant_template ON template(merchant_id, template_type, created_time DESC);
```

### 2. 前端性能优化

- 页面加载时并行请求模板状态和商品统计
- 使用骨架屏提升加载体验
- 避免频繁调用模板状态API，只在必要时刷新

### 3. 性能指标

| 操作 | 目标时间 | 监控方式 |
|-----|---------|---------|
| 模板状态查询 | < 100ms | 日志记录查询时间 |
| 导出文件生成 | < 30s (3000商品) | 进度条反馈 |
| 页面加载 | < 2s | 前端性能监控 |



## 实现细节

### 1. 后端实现步骤

#### 步骤1：创建异常类

在`com.meituan.product.exception`包中创建：
- `TemplateNotFoundException.java`
- `TemplateFileException.java`

#### 步骤2：修改ExcelService

在`ExcelService.generateMeituanTemplateFromUserTemplate()`方法中：
1. 移除所有`return generateMeituanTemplate(products)`的回退调用
2. 替换为抛出相应的异常
3. 添加详细的错误日志

#### 步骤3：添加TemplateService方法

在`TemplateService`中添加：
```java
public TemplateStatusDTO getTemplateStatus(Long merchantId)
private Template findLatestMeituanTemplate(Long merchantId)
```

#### 步骤4：添加Controller接口

在`TemplateController`中添加：
```java
@GetMapping("/status")
public ApiResponse<TemplateStatusDTO> getTemplateStatus(@RequestParam Long merchantId)
```

#### 步骤5：更新GlobalExceptionHandler

添加异常处理方法：
```java
@ExceptionHandler(TemplateNotFoundException.class)
@ExceptionHandler(TemplateFileException.class)
```

### 2. 前端实现步骤

#### 步骤1：添加API方法

在`src/api/template.js`中添加：
```javascript
export const getTemplateStatus = (merchantId) => {
  return request.get('/api/template/status', { params: { merchantId } })
}
```

#### 步骤2：修改Upload.vue

1. 添加状态变量：`loading`, `templateStatus`
2. 在`onMounted`中调用`fetchTemplateStatus()`
3. 添加按钮禁用逻辑
4. 添加loading骨架屏
5. 更新错误处理逻辑

#### 步骤3：添加样式

添加骨架屏动画和警告提示样式

### 3. 数据库优化

```sql
-- 添加复合索引提升查询性能
CREATE INDEX idx_merchant_template_type_time 
ON template(merchant_id, template_type, created_time DESC);

-- 验证索引
EXPLAIN SELECT * FROM template 
WHERE merchant_id = 1 
  AND template_type = 'MEITUAN' 
ORDER BY created_time DESC 
LIMIT 1;
```

## 部署和回滚

### 1. 部署步骤

1. **数据库准备**
   ```sql
   -- 添加索引
   CREATE INDEX idx_merchant_template_type_time 
   ON template(merchant_id, template_type, created_time DESC);
   ```

2. **后端部署**
   ```bash
   # 编译
   mvn clean package -DskipTests
   
   # 停止服务
   ./stop-backend.bat
   
   # 备份旧版本
   cp meituan-backend.jar meituan-backend.jar.backup
   
   # 部署新版本
   cp target/meituan-backend.jar ./
   
   # 启动服务
   ./start-backend.bat
   
   # 验证日志
   tail -f logs/application.log
   ```

3. **前端部署**
   ```bash
   # 编译
   npm run build
   
   # 备份旧版本
   cp -r dist dist.backup
   
   # 部署新版本
   cp -r dist/* /path/to/nginx/html/
   
   # 重启Nginx
   nginx -s reload
   ```

4. **验证**
   - 访问Upload页面，检查loading状态
   - 删除模板，验证按钮禁用
   - 上传模板，验证按钮启用
   - 尝试导出，验证功能正常

### 2. 回滚方案

如果部署后发现问题：

```bash
# 后端回滚
./stop-backend.bat
cp meituan-backend.jar.backup meituan-backend.jar
./start-backend.bat

# 前端回滚
cp -r dist.backup/* /path/to/nginx/html/
nginx -s reload

# 数据库回滚（如果需要）
DROP INDEX idx_merchant_template_type_time ON template;
```

### 3. 监控指标

部署后需要监控：
- 模板状态API的响应时间
- 导出失败率
- TemplateNotFoundException异常数量
- 用户上传模板的频率

## 风险和缓解

### 1. 风险识别

| 风险 | 影响 | 概率 | 缓解措施 |
|-----|------|------|---------|
| 用户不知道需要上传模板 | 用户无法导出 | 中 | 清晰的错误提示和操作指引 |
| 模板文件在MinIO中丢失 | 导出失败 | 低 | 定期备份，提供重新上传入口 |
| 性能下降 | 用户体验差 | 低 | 添加数据库索引，使用缓存 |
| 前端加载慢 | 用户等待时间长 | 中 | 使用骨架屏，并行请求 |

### 2. 兼容性考虑

- **向后兼容**：新功能不影响已有的导入功能
- **数据兼容**：不修改现有数据结构
- **API兼容**：新增API，不修改现有API

### 3. 降级方案

如果新功能出现严重问题，可以通过配置开关临时恢复默认模板回退：

```yaml
# application.yml
meituan:
  template:
    strict-mode: false  # 设置为false恢复默认模板回退
```

```java
@Value("${meituan.template.strict-mode:true}")
private boolean strictMode;

public byte[] generateMeituanTemplateFromUserTemplate(...) {
    Template template = findLatestMeituanTemplate(merchantId);
    
    if (template == null) {
        if (strictMode) {
            throw new TemplateNotFoundException(merchantId);
        } else {
            // 降级：使用默认模板
            log.warn("严格模式已禁用，使用默认模板");
            return generateMeituanTemplate(products);
        }
    }
    // ...
}
```

## 总结

本设计实现了模板导出限制功能，主要改进包括：

1. **移除默认模板回退**：强制用户上传模板，避免混淆
2. **新增模板状态API**：支持前端动态UI控制
3. **优化用户体验**：loading状态、清晰提示、操作指引
4. **完善错误处理**：详细的异常信息和日志记录
5. **性能优化**：数据库索引、并行请求、骨架屏

该设计确保了系统的正确性、可用性和可维护性，同时提供了良好的用户体验。
