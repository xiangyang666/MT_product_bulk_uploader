# Implementation Plan

- [x] 1. Create database schema and migration


  - Create `app_version` table with all required fields
  - Add unique constraint on (version, platform)
  - Add indexes for performance optimization
  - Create migration SQL script
  - _Requirements: 1.5, 2.2_



- [ ] 2. Implement backend entity and mapper
  - Create `AppVersion` entity class with MyBatis Plus annotations
  - Create `AppVersionMapper` interface extending BaseMapper
  - Create `AppVersionDTO` for API responses


  - Create `AppVersionUploadRequest` with validation annotations
  - _Requirements: 1.3, 1.4, 6.3_

- [ ] 3. Implement AppVersionService
  - Implement `uploadVersion` method with file validation and MinIO integration
  - Implement `listVersions` method with pagination
  - Implement `getLatestVersion` method
  - Implement `setLatest` method with transaction to update previous latest
  - Implement `deleteVersion` method with MinIO file deletion
  - Implement `generateDownloadUrl` method using MinIO presigned URLs


  - Implement `incrementDownloadCount` method
  - Add platform detection logic based on file extension
  - Add file size formatting utility
  - _Requirements: 1.1, 1.2, 1.5, 2.1, 3.1, 3.2, 4.1, 4.3, 5.1, 5.2, 6.1, 6.2_

- [ ] 4. Implement AppVersionController
  - Create POST `/api/app-versions/upload` endpoint
  - Create GET `/api/app-versions` endpoint with pagination
  - Create GET `/api/app-versions/latest/{platform}` endpoint


  - Create PUT `/api/app-versions/{id}/set-latest` endpoint
  - Create DELETE `/api/app-versions/{id}` endpoint
  - Create GET `/api/app-versions/{id}/download` endpoint
  - Add proper error handling and response formatting
  - Add authentication checks for admin-only operations
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1_

- [x] 5. Create frontend API service


  - Add version management API methods to `src/api/index.js`
  - Implement `uploadVersion(file, data)` method
  - Implement `getVersionList(platform, page, size)` method
  - Implement `getLatestVersion(platform)` method
  - Implement `setLatestVersion(id)` method
  - Implement `deleteVersion(id)` method
  - Implement `downloadVersion(id)` method
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1_



- [ ] 6. Create VersionUploadDialog component
  - Create file upload component with .exe and .dmg filter
  - Add version number input with format validation
  - Add platform display (auto-detected from file extension)
  - Add release notes textarea
  - Add upload progress indicator
  - Implement file size validation (max 500MB)
  - Implement form submission and error handling



  - _Requirements: 1.1, 1.2, 1.3, 1.4, 6.1, 6.2, 6.3, 6.4, 7.1_

- [ ] 7. Create VersionList component
  - Display version list in table format
  - Show version number, platform badge, file size, download count
  - Add "Latest" badge for current latest version
  - Add action buttons (Set Latest, Delete, Download)
  - Implement delete confirmation dialog
  - Handle empty state with helpful message
  - Add pagination controls
  - Display release notes with "show more" for long text
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 4.1, 4.2, 7.3, 7.4_

- [ ] 8. Enhance Settings page with version management
  - Add version management card to Settings.vue
  - Add tabs for Windows and macOS platforms
  - Add "Upload New Version" button
  - Integrate VersionUploadDialog component
  - Integrate VersionList component for each platform
  - Add loading states and error handling
  - _Requirements: 1.1, 2.1_

- [ ] 9. Implement automatic latest version promotion
  - When uploading first version for a platform, automatically mark as latest
  - When deleting the latest version, promote next most recent version
  - Add logic in service layer to handle these scenarios
  - _Requirements: 3.4, 4.4_

- [ ] 10. Add operation logging
  - Log version upload operations
  - Log version deletion operations
  - Log set latest operations
  - Include version number and platform in log descriptions
  - _Requirements: 1.1, 3.1, 4.1_

- [ ] 11. Test upload and download flow
  - Upload a Windows .exe file with version 1.0.0
  - Upload a macOS .dmg file with version 1.0.0
  - Verify files are stored in MinIO
  - Verify database records are created
  - Download files and verify they are correct
  - Verify download count increments
  - _Requirements: 1.1, 1.2, 1.5, 5.1, 5.2_

- [ ] 12. Test version management operations
  - Upload multiple versions for same platform
  - Set different versions as latest
  - Verify only one version is marked as latest
  - Delete non-latest version
  - Delete latest version and verify promotion
  - Test duplicate version upload (should fail)
  - _Requirements: 3.1, 3.2, 3.4, 4.1, 4.2, 4.3, 4.4_

- [ ] 13. Test error handling
  - Test uploading invalid file types
  - Test uploading files exceeding size limit
  - Test invalid version number formats
  - Test duplicate version uploads
  - Verify appropriate error messages are displayed
  - _Requirements: 1.2, 1.4, 6.4_

- [ ] 14. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
