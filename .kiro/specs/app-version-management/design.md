# Design Document

## Overview

This design implements an application version management system that allows administrators to upload, manage, and distribute desktop application installers through the system settings page. The solution integrates with the existing MinIO storage service and provides a complete version control workflow.

## Architecture

### System Components

```
Frontend (Vue 3 + Element Plus)
    ↓
Backend API (Spring Boot)
    ↓
├─ MySQL Database (version metadata)
└─ MinIO (installer file storage)
```

### Data Flow

1. **Upload Flow**: Admin uploads file → Backend validates → Store in MinIO → Save metadata to DB
2. **List Flow**: Admin views versions → Backend queries DB → Return version list with metadata
3. **Download Flow**: User requests download → Backend generates signed URL → MinIO serves file
4. **Delete Flow**: Admin deletes version → Backend removes from DB → Delete file from MinIO

## Components and Interfaces

### 1. Database Schema

**New Table: `app_version`**

```sql
CREATE TABLE `app_version` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `version` VARCHAR(20) NOT NULL COMMENT '版本号 (e.g., 1.0.0)',
  `platform` VARCHAR(20) NOT NULL COMMENT '平台: Windows, macOS',
  `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
  `file_path` VARCHAR(500) NOT NULL COMMENT 'MinIO存储路径',
  `is_latest` TINYINT DEFAULT 0 COMMENT '是否最新版本: 0-否, 1-是',
  `download_count` INT DEFAULT 0 COMMENT '下载次数',
  `release_notes` TEXT COMMENT '版本发布说明',
  `uploaded_by` BIGINT COMMENT '上传者用户ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_version_platform` (`version`, `platform`),
  INDEX `idx_platform_latest` (`platform`, `is_latest`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用版本管理表';
```

### 2. Backend Components

#### Entity: `AppVersion.java`

```java
@Data
@TableName("app_version")
public class AppVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String version;
    private String platform;
    private String fileName;
    private Long fileSize;
    private String filePath;
    private Integer isLatest;
    private Integer downloadCount;
    private String releaseNotes;
    private Long uploadedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### DTO: `AppVersionDTO.java`

```java
@Data
public class AppVersionDTO {
    private Long id;
    private String version;
    private String platform;
    private String fileName;
    private Long fileSize;
    private String fileSizeFormatted; // e.g., "103.8 MB"
    private Boolean isLatest;
    private Integer downloadCount;
    private String releaseNotes;
    private String uploadedBy;
    private LocalDateTime createdAt;
}
```

#### Upload Request: `AppVersionUploadRequest.java`

```java
@Data
public class AppVersionUploadRequest {
    @NotBlank(message = "版本号不能为空")
    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "版本号格式错误，应为 X.Y.Z")
    private String version;
    
    private String releaseNotes;
}
```

#### Controller: `AppVersionController.java`

**Endpoints:**
- `POST /api/app-versions/upload` - Upload new version
- `GET /api/app-versions` - List all versions
- `GET /api/app-versions/latest/{platform}` - Get latest version for platform
- `PUT /api/app-versions/{id}/set-latest` - Mark version as latest
- `DELETE /api/app-versions/{id}` - Delete version
- `GET /api/app-versions/{id}/download` - Generate download URL

#### Service: `AppVersionService.java`

**Key Methods:**
- `uploadVersion(MultipartFile file, AppVersionUploadRequest request, Long userId)` - Handle file upload
- `listVersions(String platform, Integer page, Integer size)` - Get paginated version list
- `getLatestVersion(String platform)` - Get latest version for platform
- `setLatest(Long id)` - Mark version as latest
- `deleteVersion(Long id)` - Delete version and file
- `generateDownloadUrl(Long id)` - Generate temporary download URL
- `incrementDownloadCount(Long id)` - Increment download counter

### 3. Frontend Components

#### Page: `Settings.vue` (Enhanced)

Add new section for version management:

```vue
<el-card class="version-management">
  <template #header>
    <div class="card-header">
      <span>应用版本管理</span>
      <el-button type="primary" @click="showUploadDialog">上传新版本</el-button>
    </div>
  </template>
  
  <el-tabs v-model="activeTab">
    <el-tab-pane label="Windows" name="Windows">
      <version-list :platform="'Windows'" />
    </el-tab-pane>
    <el-tab-pane label="macOS" name="macOS">
      <version-list :platform="'macOS'" />
    </el-tab-pane>
  </el-tabs>
</el-card>
```

#### Component: `VersionList.vue`

Display version list with actions:
- Version number and platform badge
- File size and download count
- Latest version indicator
- Action buttons (Set Latest, Delete, Download)
- Release notes display

#### Component: `VersionUploadDialog.vue`

Upload form with:
- File upload (accept .exe, .dmg)
- Version number input with validation
- Platform auto-detection display
- Release notes textarea
- Upload progress indicator

### 4. MinIO Integration

**Storage Structure:**
```
bucket: meituan-products
path: app-versions/{platform}/{version}/{filename}
example: app-versions/Windows/1.0.0/美团商品上传工具-1.0.0-x64.exe
```

**File Operations:**
- Upload: Use existing MinIO service
- Download: Generate presigned URL with 1-hour expiration
- Delete: Remove file when version is deleted

## Data Models

### Version Number Format

Semantic versioning: `MAJOR.MINOR.PATCH`
- MAJOR: Breaking changes
- MINOR: New features (backward compatible)
- PATCH: Bug fixes

### Platform Types

- `Windows`: .exe files
- `macOS`: .dmg files

### File Size Limits

- Maximum file size: 500 MB
- Enforced at both frontend and backend

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Unique Version Per Platform
*For any* platform and version number combination, there should exist at most one version record in the database.
**Validates: Requirements 1.4**

### Property 2: Single Latest Version Per Platform
*For any* platform, there should be exactly zero or one version marked as latest at any given time.
**Validates: Requirements 3.2**

### Property 3: File Storage Consistency
*For any* version record in the database, the corresponding file should exist in MinIO storage, and vice versa.
**Validates: Requirements 1.5, 4.3**

### Property 4: Platform Detection Accuracy
*For any* uploaded file, if the extension is .exe then platform should be Windows, if .dmg then platform should be macOS.
**Validates: Requirements 6.1, 6.2**

### Property 5: Download Count Monotonicity
*For any* version record, the download count should never decrease over time.
**Validates: Requirements 5.2**

### Property 6: Version Format Validation
*For any* version string, it should match the pattern `\d+\.\d+\.\d+` (semantic versioning).
**Validates: Requirements 1.4**

## Error Handling

### Upload Errors

1. **Invalid file type**: Return 400 with message "仅支持 .exe 和 .dmg 文件"
2. **File too large**: Return 413 with message "文件大小超过限制（最大 500MB）"
3. **Duplicate version**: Return 409 with message "该版本号已存在"
4. **MinIO upload failure**: Return 500 with message "文件上传失败，请重试"
5. **Invalid version format**: Return 400 with message "版本号格式错误，应为 X.Y.Z"

### Download Errors

1. **Version not found**: Return 404 with message "版本不存在"
2. **File not found in MinIO**: Return 404 with message "安装包文件不存在"
3. **URL generation failure**: Return 500 with message "下载链接生成失败"

### Delete Errors

1. **Version not found**: Return 404 with message "版本不存在"
2. **MinIO delete failure**: Log error but continue with DB deletion
3. **Database constraint violation**: Return 500 with message "删除失败"

## Testing Strategy

### Unit Tests

1. **Version Format Validation Test**: Test semantic version regex pattern
2. **Platform Detection Test**: Test file extension to platform mapping
3. **File Size Formatting Test**: Test bytes to human-readable format conversion
4. **Latest Version Logic Test**: Test setting latest version updates previous latest

### Integration Tests

1. **Upload Flow Test**: Upload file → Verify DB record → Verify MinIO file
2. **Download Flow Test**: Request download → Verify URL generation → Verify count increment
3. **Delete Flow Test**: Delete version → Verify DB removal → Verify MinIO removal
4. **Set Latest Test**: Set version as latest → Verify previous latest is updated

### Manual Testing

1. **UI Upload Test**: Upload .exe and .dmg files through UI
2. **Version List Test**: Verify version list displays correctly
3. **Download Test**: Download file and verify it's correct
4. **Delete Test**: Delete version and verify it's removed
5. **Latest Version Test**: Mark different versions as latest and verify indicator

## Implementation Notes

### Security Considerations

1. **Authentication**: Only authenticated administrators can upload/delete versions
2. **File Validation**: Validate file extension and MIME type
3. **SQL Injection**: Use parameterized queries (MyBatis Plus handles this)
4. **XSS Prevention**: Sanitize release notes before display

### Performance Considerations

1. **Large File Upload**: Use streaming upload to MinIO
2. **Download URLs**: Use presigned URLs to avoid proxying large files
3. **Version List**: Implement pagination for large version histories
4. **File Size Display**: Format file sizes on backend to reduce frontend processing

### User Experience

1. **Upload Progress**: Show progress bar during file upload
2. **Confirmation Dialogs**: Confirm before deleting versions
3. **Success Messages**: Show success notifications after operations
4. **Error Messages**: Display clear, actionable error messages
5. **Loading States**: Show loading indicators during async operations
