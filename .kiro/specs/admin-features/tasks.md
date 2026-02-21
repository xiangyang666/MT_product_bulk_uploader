# Implementation Plan: Admin Features

- [x] 1. Set up database schema and entities



  - Create t_template table migration script
  - Add index on t_operation_log.created_time for efficient queries
  - Create Template entity class with MyBatis-Plus annotations
  - _Requirements: 4.1, 4.4_


- [ ] 2. Implement Template Management backend
- [x] 2.1 Create TemplateMapper and TemplateService



  - Implement TemplateMapper interface extending BaseMapper
  - Create TemplateService with CRUD operations
  - Integrate MinioService for file storage
  - _Requirements: 1.2, 1.4, 4.1_

- [ ]* 2.2 Write property test for template storage
  - **Property 2: Valid template files are stored successfully**
  - **Validates: Requirements 1.2**

- [ ]* 2.3 Write property test for template deletion consistency
  - **Property 4: Template deletion is consistent**
  - **Validates: Requirements 1.4, 4.5**

- [ ]* 2.4 Write property test for merchant isolation
  - **Property 17: Templates are merchant-isolated**
  - **Validates: Requirements 4.3**





- [ ] 2.5 Create TemplateController with REST endpoints
  - Implement POST /api/templates/upload endpoint
  - Implement GET /api/templates endpoint for listing
  - Implement GET /api/templates/{id}/download endpoint
  - Implement DELETE /api/templates/{id} endpoint
  - Implement GET /api/templates/{id}/preview endpoint
  - Add request validation and error handling
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ]* 2.6 Write property test for template round trip
  - **Property 3: Template upload-download round trip**
  - **Validates: Requirements 1.3**

- [ ]* 2.7 Write property test for template list fields
  - **Property 1: Template list contains required fields**
  - **Validates: Requirements 1.1**

- [ ]* 2.8 Write property test for template metadata completeness
  - **Property 15: Template metadata is complete**
  - **Validates: Requirements 4.1**

- [ ]* 2.9 Write property test for invalid format error handling
  - **Property 19: Invalid template format returns specific error**
  - **Validates: Requirements 6.1**

- [ ]* 2.10 Write unit tests for TemplateService
  - Test file upload with valid Excel files
  - Test file deletion edge cases
  - Test error handling for MinIO failures
  - _Requirements: 1.2, 1.4, 6.2_





- [ ] 3. Implement Operation Logs backend
- [ ] 3.1 Create LogService with query methods
  - Implement log query with filtering (date range, operation type)
  - Implement keyword search functionality
  - Implement pagination logic
  - Add sorting by created_time DESC
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [ ]* 3.2 Write property test for log chronological ordering
  - **Property 6: Logs are ordered chronologically**
  - **Validates: Requirements 2.1**

- [ ]* 3.3 Write property test for log required fields
  - **Property 7: Log entries contain required fields**
  - **Validates: Requirements 2.2**

- [ ]* 3.4 Write property test for log filtering
  - **Property 8: Log filtering returns matching entries**
  - **Validates: Requirements 2.3**

- [ ]* 3.5 Write property test for pagination
  - **Property 9: Pagination respects page size**
  - **Validates: Requirements 2.4**






- [ ]* 3.6 Write property test for keyword search
  - **Property 10: Search filters by keyword**
  - **Validates: Requirements 2.5**

- [ ] 3.7 Create LogController with REST endpoints
  - Implement GET /api/logs endpoint with query parameters
  - Implement GET /api/logs/{id} endpoint for detail view
  - Add request validation and error handling
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [ ]* 3.8 Write property test for log creation atomicity
  - **Property 16: Log creation is atomic**
  - **Validates: Requirements 4.2**



- [-]* 3.9 Write unit tests for LogService

  - Test date range filtering with specific dates
  - Test operation type filtering
  - Test pagination edge cases (empty results, last page)
  - _Requirements: 2.3, 2.4_

- [ ] 4. Implement System Settings backend
- [ ] 4.1 Create SettingsService with CRUD operations
  - Implement getMerchantConfig method
  - Implement updateMerchantConfig with validation
  - Implement resetToDefaults method
  - Add operation logging for settings changes
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ]* 4.2 Write property test for settings validation
  - **Property 11: Settings validation rejects invalid inputs**
  - **Validates: Requirements 3.2, 6.4**

- [ ]* 4.3 Write property test for settings round trip
  - **Property 12: Settings update round trip**


  - **Validates: Requirements 3.3**

- [ ]* 4.4 Write property test for settings update logging
  - **Property 13: Settings updates create log entries**
  - **Validates: Requirements 3.4**

- [ ]* 4.5 Write property test for settings reset
  - **Property 14: Settings reset restores defaults**
  - **Validates: Requirements 3.5**

- [ ] 4.6 Create SettingsController with REST endpoints
  - Implement GET /api/settings endpoint
  - Implement PUT /api/settings endpoint
  - Implement POST /api/settings/reset endpoint
  - Add request validation and error handling


  - _Requirements: 3.1, 3.2, 3.3, 3.5_

- [ ]* 4.7 Write unit tests for SettingsService
  - Test validation for different setting types
  - Test default value restoration
  - Test error handling for invalid inputs
  - _Requirements: 3.2, 3.5, 6.4_

- [ ] 5. Checkpoint - Ensure all backend tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Implement Template Management frontend
- [ ] 6.1 Create Template.vue component
  - Create template list table with columns (name, type, size, date)
  - Add upload button with file selection dialog
  - Add download button for each template
  - Add delete button with confirmation dialog
  - Add preview button with modal display

  - Implement loading states for async operations
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 5.5_

- [ ] 6.2 Implement template API integration
  - Create API methods in api/index.js for template operations
  - Implement file upload with FormData
  - Implement file download with blob handling


  - Add error handling and notifications
  - _Requirements: 1.2, 1.3, 5.4, 6.1_

- [ ]* 6.3 Write property test for notification generation
  - **Property 18: Operations trigger notifications**
  - **Validates: Requirements 5.4**

- [ ] 6.3 Add template management to navigation
  - Add "模板管理" link to navigation menu
  - Add route configuration in router
  - Apply iPhone-inspired theme styling
  - _Requirements: 5.1, 5.3_

- [ ] 7. Implement Operation Logs frontend
- [ ] 7.1 Create Logs.vue component
  - Create log table with columns (username, type, details, time, status)
  - Add date range picker for filtering
  - Add operation type dropdown filter

  - Add keyword search input
  - Add pagination controls
  - Implement loading states
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 5.5_

- [ ] 7.2 Implement log API integration
  - Create API methods for log queries
  - Implement filter parameter handling


  - Add error handling and notifications
  - _Requirements: 2.1, 2.3, 2.5, 5.4_

- [ ] 7.3 Add log detail modal
  - Create modal component for detailed log view
  - Display full operation details and error messages
  - Add close button
  - _Requirements: 2.2_

- [ ] 7.4 Add logs page to navigation
  - Add "操作日志" link to navigation menu
  - Add route configuration in router
  - Apply iPhone-inspired theme styling
  - _Requirements: 5.1, 5.3_



- [ ] 8. Implement System Settings frontend
- [ ] 8.1 Create Settings.vue component
  - Create settings form with input fields
  - Add merchant name input
  - Add Meituan AppKey input
  - Add Meituan AppSecret input (password field)
  - Add template config textarea
  - Add Save and Reset buttons
  - Implement form validation
  - Implement loading states
  - _Requirements: 3.1, 3.2, 3.5, 5.5_

- [ ] 8.2 Implement settings API integration
  - Create API methods for settings operations
  - Implement form data submission
  - Add validation error handling
  - Add success/error notifications
  - _Requirements: 3.2, 3.3, 5.4, 6.4_

- [ ] 8.3 Add settings page to navigation
  - Add "系统设置" link to navigation menu
  - Add route configuration in router
  - Apply iPhone-inspired theme styling
  - _Requirements: 5.1, 5.3_

- [ ] 9. Final integration and testing
- [ ] 9.1 Test complete workflows
  - Test template upload → download → delete flow
  - Test settings update → log creation → settings retrieval flow
  - Test log filtering and search functionality
  - Verify all notifications display correctly
  - _Requirements: 1.2, 1.3, 1.4, 3.3, 3.4, 5.4_

- [ ]* 9.2 Write integration tests
  - Test template management end-to-end flow
  - Test settings update with log verification
  - Test log query with various filter combinations
  - _Requirements: 1.2, 1.3, 1.4, 3.3, 3.4_

- [ ] 9.3 Verify error handling
  - Test invalid file upload scenarios
  - Test MinIO unavailable scenario
  - Test invalid settings input scenarios
  - Verify user-friendly error messages
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [ ] 10. Final Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
