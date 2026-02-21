# Design Document

## Overview

This design addresses the critical bug where the System Settings page fails to update merchant configuration due to invalid JSON handling in the `template_config` field. The solution implements a multi-layered approach with JSON validation and sanitization at the entity, service, and database levels to ensure robust handling of JSON data.

## Architecture

### Component Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend Layer                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Settings.vue - System Settings Page              │  │
│  └────────────────────┬─────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────┘
                        │ HTTP PUT /api/settings
                        ↓
┌─────────────────────────────────────────────────────────┐
│                   Controller Layer                       │
│  ┌──────────────────────────────────────────────────┐  │
│  │  SettingsController.updateSettings()              │  │
│  └────────────────────┬─────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────┐
│                    Service Layer                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │  SettingsService.updateMerchantConfig()           │  │
│  │  - Validate and sanitize JSON                     │  │
│  │  - Ensure template_config is never null/empty     │  │
│  └────────────────────┬─────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────┐
│                    Entity Layer                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │  MerchantConfig                                   │  │
│  │  - Custom setter for template_config              │  │
│  │  - Automatic JSON sanitization                    │  │
│  └────────────────────┬─────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────┐
│                   Persistence Layer                      │
│  ┌──────────────────────────────────────────────────┐  │
│  │  MerchantConfigMapper                             │  │
│  │  - MyBatis Plus operations                        │  │
│  └────────────────────┬─────────────────────────────┘  │
└───────────────────────┼─────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────┐
│                    Database Layer                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │  t_merchant_config table                          │  │
│  │  - template_config JSON column                    │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. MerchantConfig Entity Enhancement

**Purpose:** Provide automatic JSON sanitization at the entity level

**Key Methods:**
```java
public class MerchantConfig {
    private String templateConfig;
    
    // Custom setter with JSON sanitization
    public void setTemplateConfig(String templateConfig) {
        this.templateConfig = sanitizeJson(templateConfig);
    }
    
    // Custom getter ensuring non-null
    public String getTemplateConfig() {
        return templateConfig != null ? templateConfig : "{}";
    }
    
    // Private helper for JSON sanitization
    private String sanitizeJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "{}";
        }
        return json;
    }
    
    // Constructor initialization
    public MerchantConfig() {
        this.templateConfig = "{}";
    }
}
```

**Responsibilities:**
- Initialize `templateConfig` to "{}" in constructor
- Sanitize JSON in setter to prevent null/empty values
- Ensure getter never returns null

### 2. SettingsService Enhancement

**Purpose:** Add service-level validation before database operations

**Key Methods:**
```java
@Service
public class SettingsService {
    
    public MerchantConfig updateMerchantConfig(MerchantConfig config) {
        // Sanitize JSON before database operation
        sanitizeConfigJson(config);
        
        // Existing update logic...
        merchantConfigMapper.updateById(config);
        
        return config;
    }
    
    private void sanitizeConfigJson(MerchantConfig config) {
        if (config.getTemplateConfig() == null || 
            config.getTemplateConfig().trim().isEmpty()) {
            config.setTemplateConfig("{}");
        }
    }
    
    private MerchantConfig createDefaultConfig(Long merchantId) {
        MerchantConfig config = new MerchantConfig();
        config.setMerchantId(merchantId);
        config.setMerchantName("默认商家");
        config.setTemplateConfig("{}"); // Explicit default
        
        merchantConfigMapper.insert(config);
        return config;
    }
}
```

**Responsibilities:**
- Validate and sanitize JSON before all database operations
- Ensure default values are properly set
- Log warnings when JSON sanitization occurs

### 3. Database Schema Verification

**Purpose:** Ensure database schema supports empty JSON objects

**SQL Verification:**
```sql
-- Verify column type
SHOW COLUMNS FROM t_merchant_config WHERE Field = 'template_config';

-- Test empty JSON insertion
INSERT INTO t_merchant_config (merchant_id, merchant_name, template_config) 
VALUES (999, 'Test', '{}');

-- Test empty JSON update
UPDATE t_merchant_config 
SET template_config = '{}' 
WHERE merchant_id = 999;
```

**Expected Behavior:**
- Column type should be JSON or TEXT
- Empty JSON object "{}" should be accepted
- NULL values should be handled gracefully

## Data Models

### MerchantConfig Entity

```java
@Data
@TableName("t_merchant_config")
public class MerchantConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long merchantId;
    private String merchantName;
    private String meituanAppKey;
    private String meituanAppSecret;
    private String accessToken;
    private LocalDateTime tokenExpireTime;
    
    // Enhanced field with custom getter/setter
    private String templateConfig;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    @TableLogic
    private Integer deleted;
    
    // Custom methods for JSON handling
    public void setTemplateConfig(String templateConfig) {
        this.templateConfig = sanitizeJson(templateConfig);
    }
    
    public String getTemplateConfig() {
        return templateConfig != null ? templateConfig : "{}";
    }
    
    private String sanitizeJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "{}";
        }
        return json;
    }
    
    public MerchantConfig() {
        this.templateConfig = "{}";
    }
}
```

### Database Table Schema

```sql
CREATE TABLE t_merchant_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    merchant_name VARCHAR(100),
    meituan_app_key VARCHAR(100),
    meituan_app_secret VARCHAR(200),
    access_token VARCHAR(500),
    token_expire_time DATETIME,
    template_config JSON DEFAULT ('{}'),  -- JSON type with default
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_merchant_id (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: JSON Non-Null Invariant

*For any* MerchantConfig instance, after setting template_config to any value (including null or empty string), getting template_config should always return a valid non-null JSON string.

**Validates: Requirements 1.2, 1.3, 2.4**

### Property 2: Empty JSON Normalization

*For any* input value that is null, empty string, or whitespace-only, setting template_config should result in the value "{}" being stored.

**Validates: Requirements 1.1, 1.2, 1.3, 2.2, 2.3**

### Property 3: Valid JSON Preservation

*For any* valid JSON string input, setting template_config should preserve the exact JSON value without modification.

**Validates: Requirements 1.4, 2.5**

### Property 4: Database Round-Trip Consistency

*For any* MerchantConfig with template_config = "{}", saving to database and then retrieving should return a MerchantConfig with template_config = "{}".

**Validates: Requirements 4.1, 4.2, 4.3**

### Property 5: Service Layer Sanitization

*For any* MerchantConfig passed to updateMerchantConfig() or createDefaultConfig(), the template_config field should be sanitized to valid JSON before database operations.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

### Property 6: Constructor Initialization

*For any* newly created MerchantConfig instance (using default constructor), the template_config field should be initialized to "{}".

**Validates: Requirements 2.1**

### Property 7: Null-to-Default Conversion

*For any* MerchantConfig where template_config is set to null, subsequent getter calls should return "{}" instead of null.

**Validates: Requirements 5.1, 5.3, 6.1, 6.3**

## Error Handling

### Error Scenarios and Responses

| Scenario | Detection | Response | User Message |
|----------|-----------|----------|--------------|
| Null template_config | Entity setter | Convert to "{}" | None (transparent) |
| Empty string template_config | Entity setter | Convert to "{}" | None (transparent) |
| Invalid JSON format | Service validation | Log warning, use "{}" | "配置已重置为默认值" |
| Database constraint violation | Catch SQLException | Retry with "{}" | "配置保存失败，已使用默认值" |
| JSON parsing error | Try-catch in service | Use default "{}" | "配置格式错误，已恢复默认值" |

### Error Recovery Flow

```
User saves settings
    ↓
Service receives MerchantConfig
    ↓
Sanitize template_config
    ↓
Attempt database update
    ↓
    ├─ Success → Return updated config
    │
    └─ Failure (JSON error)
        ↓
        Log error details
        ↓
        Set template_config = "{}"
        ↓
        Retry database update
        ↓
        ├─ Success → Return config with warning
        │
        └─ Failure → Throw exception with user-friendly message
```

## Testing Strategy

### Unit Tests

**Test Class:** `MerchantConfigTest`

```java
@Test
void testConstructorInitializesTemplateConfig() {
    MerchantConfig config = new MerchantConfig();
    assertEquals("{}", config.getTemplateConfig());
}

@Test
void testSetNullTemplateConfigConvertsToEmptyJson() {
    MerchantConfig config = new MerchantConfig();
    config.setTemplateConfig(null);
    assertEquals("{}", config.getTemplateConfig());
}

@Test
void testSetEmptyStringTemplateConfigConvertsToEmptyJson() {
    MerchantConfig config = new MerchantConfig();
    config.setTemplateConfig("");
    assertEquals("{}", config.getTemplateConfig());
}

@Test
void testSetWhitespaceTemplateConfigConvertsToEmptyJson() {
    MerchantConfig config = new MerchantConfig();
    config.setTemplateConfig("   ");
    assertEquals("{}", config.getTemplateConfig());
}

@Test
void testSetValidJsonPreservesValue() {
    MerchantConfig config = new MerchantConfig();
    String validJson = "{\"key\":\"value\"}";
    config.setTemplateConfig(validJson);
    assertEquals(validJson, config.getTemplateConfig());
}
```

**Test Class:** `SettingsServiceTest`

```java
@Test
void testUpdateMerchantConfigSanitizesNullJson() {
    MerchantConfig config = new MerchantConfig();
    config.setMerchantId(1L);
    config.setTemplateConfig(null);
    
    MerchantConfig result = settingsService.updateMerchantConfig(config);
    
    assertEquals("{}", result.getTemplateConfig());
}

@Test
void testCreateDefaultConfigSetsEmptyJson() {
    MerchantConfig config = settingsService.getMerchantConfig(999L);
    
    assertNotNull(config);
    assertEquals("{}", config.getTemplateConfig());
}
```

### Integration Tests

**Test Class:** `MerchantConfigIntegrationTest`

```java
@Test
@Transactional
void testInsertWithEmptyJsonSucceeds() {
    MerchantConfig config = new MerchantConfig();
    config.setMerchantId(1L);
    config.setMerchantName("Test");
    config.setTemplateConfig("{}");
    
    merchantConfigMapper.insert(config);
    
    MerchantConfig retrieved = merchantConfigMapper.selectById(config.getId());
    assertEquals("{}", retrieved.getTemplateConfig());
}

@Test
@Transactional
void testUpdateWithEmptyJsonSucceeds() {
    // Create initial record
    MerchantConfig config = new MerchantConfig();
    config.setMerchantId(1L);
    config.setTemplateConfig("{\"old\":\"value\"}");
    merchantConfigMapper.insert(config);
    
    // Update to empty JSON
    config.setTemplateConfig("{}");
    merchantConfigMapper.updateById(config);
    
    // Verify
    MerchantConfig retrieved = merchantConfigMapper.selectById(config.getId());
    assertEquals("{}", retrieved.getTemplateConfig());
}
```

### Property-Based Tests

Using jqwik for property-based testing:

```java
@Property
void templateConfigNeverReturnsNull(@ForAll String input) {
    MerchantConfig config = new MerchantConfig();
    config.setTemplateConfig(input);
    
    assertNotNull(config.getTemplateConfig());
}

@Property
void nullOrEmptyInputAlwaysBecomesEmptyJson(
    @ForAll @StringLength(max = 10) String input) {
    
    Assume.that(input == null || input.trim().isEmpty());
    
    MerchantConfig config = new MerchantConfig();
    config.setTemplateConfig(input);
    
    assertEquals("{}", config.getTemplateConfig());
}
```

## Implementation Notes

### Key Changes Required

1. **MerchantConfig.java**
   - Add custom setter for `templateConfig`
   - Add custom getter for `templateConfig`
   - Add constructor initialization
   - Add private `sanitizeJson()` helper method

2. **SettingsService.java**
   - Add `sanitizeConfigJson()` method
   - Call sanitization before all database operations
   - Update `createDefaultConfig()` to explicitly set "{}"
   - Add logging for JSON sanitization events

3. **Database Migration (if needed)**
   - Verify column type supports JSON
   - Add default value constraint if possible
   - Update existing NULL values to "{}"

### Backward Compatibility

The solution maintains full backward compatibility:
- Existing valid JSON values are preserved
- NULL values are transparently converted to "{}"
- Empty strings are transparently converted to "{}"
- No database migration required (handled at application level)

### Performance Considerations

- JSON sanitization is O(1) operation (simple null/empty check)
- No additional database queries required
- Minimal memory overhead (single string comparison)
- No impact on read performance

## Deployment Plan

1. **Phase 1: Code Changes**
   - Update MerchantConfig entity
   - Update SettingsService
   - Add unit tests

2. **Phase 2: Testing**
   - Run unit tests
   - Run integration tests
   - Manual testing on dev environment

3. **Phase 3: Deployment**
   - Deploy to staging
   - Verify system settings page works
   - Deploy to production
   - Monitor logs for JSON sanitization events

4. **Phase 4: Verification**
   - Test create/update/reset operations
   - Verify no JSON errors in logs
   - Confirm user can save settings successfully

