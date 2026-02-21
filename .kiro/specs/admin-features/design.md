# Design Document: Admin Features

## Overview

This design document outlines the implementation of three administrative modules for the Meituan product management system: Template Management, Operation Logs, and System Settings. These features provide essential administrative capabilities while maintaining consistency with the existing architecture and iPhone-inspired UI theme.

The implementation follows the existing patterns in the codebase:
- Backend: Spring Boot with MyBatis-Plus for data access
- Frontend: Vue 3 with Element Plus components
- Storage: MinIO for file storage, MySQL for metadata
- Architecture: RESTful API with service layer separation

## Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend (Vue 3)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Template    │  │   Logs       │  │  Settings    │      │
│  │  Management  │  │   Viewer     │  │   Panel      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                    REST API (HTTP/JSON)
                            │
┌─────────────────────────────────────────────────────────────┐
│                    Backend (Spring Boot)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Template    │  │   Log        │  │  Settings    │      │
│  │  Controller  │  │   Controller │  │  Controller  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│          │                 │                 │              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Template    │  │   Log        │  │  Settings    │      │
│  │  Service     │  │   Service    │  │  Service     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│          │                 │                 │              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Template    │  │  Operation   │  │  Merchant    │      │
│  │  Mapper      │  │  Log Mapper  │  │  Config      │      │
│  │              │  │              │  │  Mapper      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┴───────────────────┐
        │                                       │
┌───────────────┐                      ┌────────────────┐
│  MinIO        │                      │  MySQL         │
│  (File        │                      │  (Metadata)    │
│   Storage)    │                      │                │
└───────────────┘                      └────────────────┘
```

### Data Flow

1. **Template Upload Flow**:
   - User selects file → Frontend validates → POST to /api/templates/upload
   - Backend validates format → Store file in MinIO → Save metadata to DB
   - Return template info → Update UI

2. **Log Query Flow**:
   - User applies filters → GET to /api/logs with query params
   - Backend queries DB with filters → Return paginated results
   - Frontend displays in table

3. **Settings Update Flow**:
   - User modifies settings → PUT to /api/settings
   - Backend validates → Update DB → Create operation log
   - Return updated config → Update UI

## Components and Interfaces

### Backend Components

#### 1. Template Entity
```java
@Data
@TableName("t_template")
public class Template {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private String templateName;
    private String templateType;  // IMPORT, EXPORT, MEITUAN
    private String filePath;      // MinIO object name
    private String fileUrl;       // MinIO presigned URL
    private Long fileSize;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    @TableLogic
    private Integer deleted;
}
```

#### 2. Template Controller
```java
@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    
    // GET /api/templates - List all templates
    ApiResponse<List<Template>> listTemplates(@RequestParam Long merchantId);
    
    // POST /api/templates/upload - Upload new template
    ApiResponse<Template> uploadTemplate(
        @RequestParam MultipartFile file,
        @RequestParam String templateType,
        @RequestParam Long merchantId
    );
    
    // GET /api/templates/{id}/download - Download template
    ResponseEntity<byte[]> downloadTemplate(@PathVariable Long id);
    
    // DELETE /api/templates/{id} - Delete template
    ApiResponse<Void> deleteTemplate(@PathVariable Long id);
    
    // GET /api/templates/{id}/preview - Preview template structure
    ApiResponse<TemplatePreview> previewTemplate(@PathVariable Long id);
}
```

#### 3. Log Controller
```java
@RestController
@RequestMapping("/api/logs")
public class LogController {
    
    // GET /api/logs - Query logs with filters
    ApiResponse<PageResult<OperationLog>> queryLogs(
        @RequestParam Long merchantId,
        @RequestParam(required = false) String operationType,
        @RequestParam(required = false) LocalDateTime startTime,
        @RequestParam(required = false) LocalDateTime endTime,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "20") Integer size
    );
    
    // GET /api/logs/{id} - Get log detail
    ApiResponse<OperationLog> getLogDetail(@PathVariable Long id);
}
```

#### 4. Settings Controller
```java
@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    
    // GET /api/settings - Get merchant config
    ApiResponse<MerchantConfig> getSettings(@RequestParam Long merchantId);
    
    // PUT /api/settings - Update merchant config
    ApiResponse<MerchantConfig> updateSettings(
        @RequestBody MerchantConfigDTO config
    );
    
    // POST /api/settings/reset - Reset to defaults
    ApiResponse<MerchantConfig> resetSettings(@RequestParam Long merchantId);
}
```

### Frontend Components

#### 1. Template.vue
- Template list table with upload/download/delete actions
- File upload dialog with format validation
- Template preview modal
- Uses Element Plus table and upload components

#### 2. Logs.vue
- Log table with filtering and search
- Date range picker for time filtering
- Operation type filter dropdown
- Pagination controls
- Log detail modal

#### 3. Settings.vue
- Form with merchant configuration fields
- Input validation
- Save/Reset buttons
- Success/error notifications

## Data Models

### Database Schema

#### t_template Table
```sql
CREATE TABLE t_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id BIGINT NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    template_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_url VARCHAR(1000),
    file_size BIGINT,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_template_type (template_type),
    INDEX idx_created_time (created_time)
);
```

#### t_operation_log Table (existing)
- Already defined in the system
- Add index on created_time for efficient date-range queries

#### t_merchant_config Table (existing)
- Already defined in the system
- No schema changes needed

### DTOs

#### TemplateDTO
```java
@Data
public class TemplateDTO {
    private Long id;
    private String templateName;
    private String templateType;
    private String fileUrl;
    private Long fileSize;
    private LocalDateTime createdTime;
}
```

#### LogQueryDTO
```java
@Data
public class LogQueryDTO {
    private Long merchantId;
    private String operationType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String keyword;
    private Integer page;
    private Integer size;
}
```

#### MerchantConfigDTO
```java
@Data
public class MerchantConfigDTO {
    private Long merchantId;
    private String merchantName;
    private String meituanAppKey;
    private String meituanAppSecret;
    private String templateConfig;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Template list contains required fields
*For any* template query response, all returned templates should contain name, type, and creation date fields.
**Validates: Requirements 1.1**

### Property 2: Valid template files are stored successfully
*For any* valid Excel template file, uploading it should result in the file being stored in MinIO and metadata being saved to the database.
**Validates: Requirements 1.2**

### Property 3: Template upload-download round trip
*For any* uploaded template file, downloading it should return file content that matches the original upload.
**Validates: Requirements 1.3**

### Property 4: Template deletion is consistent
*For any* template, after deletion, both the MinIO file and the database record should be removed (file should not exist in MinIO, database record should be marked as deleted).
**Validates: Requirements 1.4, 4.5**

### Property 5: Template preview returns structure
*For any* template file, the preview response should contain column headers and structure information.
**Validates: Requirements 1.5**

### Property 6: Logs are ordered chronologically
*For any* set of operation logs, querying them should return results in reverse chronological order (newest first).
**Validates: Requirements 2.1**

### Property 7: Log entries contain required fields
*For any* operation log entry, it should contain username, operation type, operation details, timestamp, and status fields.
**Validates: Requirements 2.2**

### Property 8: Log filtering returns matching entries
*For any* log query with filters (date range or operation type), all returned results should match the filter criteria.
**Validates: Requirements 2.3**

### Property 9: Pagination respects page size
*For any* paginated log query, the number of returned results should not exceed the requested page size.
**Validates: Requirements 2.4**

### Property 10: Search filters by keyword
*For any* log search with a keyword, all returned results should contain the keyword in the operation details field.
**Validates: Requirements 2.5**

### Property 11: Settings validation rejects invalid inputs
*For any* invalid setting value (based on type constraints), the system should reject the update and return a validation error.
**Validates: Requirements 3.2, 6.4**

### Property 12: Settings update round trip
*For any* valid settings update, saving then retrieving the settings should return the same values that were saved.
**Validates: Requirements 3.3**

### Property 13: Settings updates create log entries
*For any* settings update operation, a corresponding operation log entry should be created with type "SETTINGS_UPDATE".
**Validates: Requirements 3.4**

### Property 14: Settings reset restores defaults
*For any* modified setting, resetting it should restore the default value.
**Validates: Requirements 3.5**

### Property 15: Template metadata is complete
*For any* stored template, the database record should contain file path, name, type, and upload timestamp.
**Validates: Requirements 4.1**

### Property 16: Log creation is atomic
*For any* operation log creation, either all required fields (user ID, operation type, details, timestamp) are stored or none are.
**Validates: Requirements 4.2**

### Property 17: Templates are merchant-isolated
*For any* merchant, querying templates should return only templates belonging to that merchant, not templates from other merchants.
**Validates: Requirements 4.3**

### Property 18: Operations trigger notifications
*For any* completed operation (success or failure), a notification message should be generated.
**Validates: Requirements 5.4**

### Property 19: Invalid template format returns specific error
*For any* invalid template file upload, the system should return an error message that specifically indicates the format issue.
**Validates: Requirements 6.1**

## Error Handling

### Error Categories

1. **Validation Errors** (400 Bad Request)
   - Invalid file format
   - Missing required fields
   - Invalid setting values
   - File size exceeds limit

2. **Not Found Errors** (404 Not Found)
   - Template not found
   - Log entry not found
   - Merchant config not found

3. **Storage Errors** (500 Internal Server Error)
   - MinIO connection failure
   - File upload/download failure
   - Database operation failure

4. **Authorization Errors** (403 Forbidden)
   - Accessing templates from different merchant
   - Unauthorized settings modification

### Error Response Format
```json
{
  "code": 400,
  "message": "Invalid template format: Expected .xlsx file",
  "data": null
}
```

### Retry Strategy
- MinIO operations: Retry up to 3 times with exponential backoff
- Database operations: No automatic retry, log error and return to user
- Network errors: Frontend retries once, then displays error

## Testing Strategy

### Unit Testing

Unit tests will cover specific examples and integration points:

1. **Controller Tests**
   - Test request validation
   - Test response formatting
   - Test error handling for specific cases

2. **Service Tests**
   - Test business logic with mock dependencies
   - Test specific edge cases (empty files, null values)
   - Test error scenarios (MinIO unavailable, DB failure)

3. **Mapper Tests**
   - Test SQL queries with test database
   - Test specific query conditions

### Property-Based Testing

Property-based tests will verify universal properties across all inputs using **jqwik** (Java property-based testing library):

**Configuration**: Each property test will run a minimum of 100 iterations.

**Test Tagging**: Each property-based test will be tagged with a comment in this format:
```java
/**
 * Feature: admin-features, Property 1: Template list contains required fields
 */
```

**Property Test Coverage**:
- Each correctness property listed above will be implemented as a single property-based test
- Tests will generate random valid inputs (templates, logs, settings)
- Tests will verify the property holds across all generated inputs

**Example Property Test Structure**:
```java
@Property
void templateUploadDownloadRoundTrip(@ForAll("validTemplateFiles") File template) {
    // Upload template
    Template uploaded = templateService.upload(template, merchantId);
    
    // Download template
    byte[] downloaded = templateService.download(uploaded.getId());
    
    // Verify content matches
    assertThat(downloaded).isEqualTo(readFileBytes(template));
}
```

### Integration Testing

Integration tests will verify end-to-end flows:
- Template upload → storage → retrieval → deletion
- Settings update → log creation → settings retrieval
- Log filtering → pagination → search

### Testing Tools
- JUnit 5 for unit tests
- jqwik for property-based tests
- Mockito for mocking dependencies
- TestContainers for database integration tests
- MockMvc for controller tests

## Implementation Notes

### MinIO Integration
- Reuse existing MinioService
- Store templates in "templates/" folder
- Generate presigned URLs with 7-day expiry
- Handle file cleanup on deletion

### Database Considerations
- Add index on t_operation_log.created_time for efficient date queries
- Use MyBatis-Plus for CRUD operations
- Implement soft delete for templates

### Frontend Considerations
- Reuse existing iPhone-inspired theme styles
- Use Element Plus components for consistency
- Implement loading states for async operations
- Add form validation before API calls

### Security Considerations
- Validate merchant ID matches authenticated user
- Sanitize file names to prevent path traversal
- Limit file upload size (max 10MB)
- Validate file extensions (only .xlsx, .xls, .csv)
- Use parameterized queries to prevent SQL injection

## Performance Considerations

- **Template List**: Add pagination if template count exceeds 100
- **Log Queries**: Use database indexes for date-range filtering
- **File Downloads**: Stream large files instead of loading into memory
- **Settings Updates**: Cache merchant config to reduce DB queries

## Future Enhancements

- Template versioning
- Bulk template operations
- Log export functionality
- Advanced log analytics
- Settings validation rules engine
- Template sharing between merchants
