# Implementation Plan

- [x] 1. Update MerchantConfig entity with JSON sanitization


  - Add custom setter for templateConfig that sanitizes null/empty values to "{}"
  - Add custom getter that ensures non-null return value
  - Add constructor that initializes templateConfig to "{}"
  - Add private sanitizeJson() helper method
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 2.4_

- [ ]* 1.1 Write property test for JSON sanitization
  - **Property 1: JSON Non-Null Invariant**
  - **Property 2: Empty JSON Normalization**
  - **Property 3: Valid JSON Preservation**
  - **Validates: Requirements 1.2, 1.3, 1.4, 2.2, 2.3, 2.4**

- [ ]* 1.2 Write unit tests for MerchantConfig
  - Test constructor initializes templateConfig to "{}"
  - Test setter converts null to "{}"
  - Test setter converts empty string to "{}"
  - Test setter converts whitespace to "{}"
  - Test setter preserves valid JSON
  - Test getter never returns null
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4_



- [ ] 2. Update SettingsService with JSON validation
  - Add sanitizeConfigJson() private method
  - Call sanitization in updateMerchantConfig() before database operations
  - Update createDefaultConfig() to explicitly set templateConfig to "{}"
  - Add logging when JSON sanitization occurs
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ]* 2.1 Write property test for service layer sanitization
  - **Property 5: Service Layer Sanitization**
  - **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

- [ ]* 2.2 Write unit tests for SettingsService
  - Test updateMerchantConfig sanitizes null JSON
  - Test updateMerchantConfig sanitizes empty JSON
  - Test createDefaultConfig sets empty JSON
  - Test getMerchantConfig handles missing config


  - Test resetToDefaults sets empty JSON
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 3. Verify database schema compatibility
  - Check template_config column type (should be JSON or TEXT)
  - Test inserting record with templateConfig = "{}"
  - Test updating record with templateConfig = "{}"
  - Document any schema changes needed
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [ ]* 3.1 Write integration test for database operations
  - **Property 4: Database Round-Trip Consistency**


  - Test insert with empty JSON succeeds
  - Test update with empty JSON succeeds
  - Test query returns valid JSON
  - **Validates: Requirements 4.1, 4.2, 4.3**

- [ ] 4. Add error recovery mechanism
  - Implement try-catch in updateMerchantConfig for JSON errors
  - Add retry logic with sanitized JSON on failure
  - Add user-friendly error messages
  - Add detailed error logging
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_



- [ ]* 4.1 Write unit tests for error recovery
  - Test recovery from null JSON
  - Test recovery from invalid JSON
  - Test error messages are user-friendly
  - Test logging captures error details
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ] 5. Test backward compatibility
  - Test handling of existing null values in database
  - Test handling of existing empty strings
  - Test preservation of existing valid JSON
  - Verify no data migration required


  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ]* 5.1 Write integration tests for backward compatibility
  - Test reading legacy null values
  - Test reading legacy empty strings
  - Test reading legacy valid JSON



  - Test updating legacy records
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 6. Manual testing and verification
  - Test system settings page loads without errors
  - Test saving settings with empty configuration
  - Test saving settings with valid configuration
  - Test reset to defaults functionality
  - Verify no JSON errors in application logs
  - _Requirements: All_

- [ ] 7. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

