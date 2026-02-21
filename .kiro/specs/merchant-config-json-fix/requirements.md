# Requirements Document

## Introduction

This feature fixes a critical bug in the System Settings page where updating merchant configuration fails due to invalid JSON handling for the `template_config` column. The error occurs when the field is null or empty, causing MySQL to reject the update with "Invalid JSON text: The document is empty" error.

## Glossary

- **System**: The Meituan product management application
- **MerchantConfig**: Entity representing merchant-specific configuration stored in `t_merchant_config` table
- **template_config**: A JSON column in the database that stores template-related configuration
- **JSON Handler**: MyBatis type handler that converts between Java objects and JSON strings
- **Default Value**: A valid empty JSON object "{}" used when no configuration exists

## Requirements

### Requirement 1: JSON Field Validation

**User Story:** As a system, I want to ensure template_config always contains valid JSON, so that database operations never fail due to empty or null JSON values.

#### Acceptance Criteria

1. WHEN a MerchantConfig is created without template_config THEN the System SHALL set template_config to an empty JSON object "{}"
2. WHEN a MerchantConfig is updated with null template_config THEN the System SHALL convert null to an empty JSON object "{}"
3. WHEN a MerchantConfig is updated with empty string template_config THEN the System SHALL convert empty string to an empty JSON object "{}"
4. WHEN template_config is set to a valid JSON string THEN the System SHALL preserve the JSON value unchanged
5. WHEN retrieving MerchantConfig from database THEN the System SHALL return template_config as a valid JSON string

### Requirement 2: Entity-Level Default Handling

**User Story:** As a developer, I want MerchantConfig entity to handle JSON defaults automatically, so that I don't need to manually set default values in every service method.

#### Acceptance Criteria

1. WHEN a new MerchantConfig instance is created THEN the System SHALL initialize template_config to "{}"
2. WHEN template_config setter receives null THEN the System SHALL store "{}" instead
3. WHEN template_config setter receives empty string THEN the System SHALL store "{}" instead
4. WHEN template_config getter returns value THEN the System SHALL never return null
5. WHEN serializing MerchantConfig to JSON THEN the System SHALL include template_config as a valid JSON object

### Requirement 3: Service Layer Validation

**User Story:** As a service layer, I want to validate and sanitize template_config before database operations, so that invalid JSON never reaches the database.

#### Acceptance Criteria

1. WHEN SettingsService creates a MerchantConfig THEN the System SHALL ensure template_config is valid JSON
2. WHEN SettingsService updates a MerchantConfig THEN the System SHALL validate template_config before calling mapper
3. WHEN validation detects invalid JSON THEN the System SHALL replace it with "{}"
4. WHEN resetting to defaults THEN the System SHALL set template_config to "{}"
5. WHEN creating default config THEN the System SHALL initialize template_config to "{}"

### Requirement 4: Database Schema Compatibility

**User Story:** As a database administrator, I want the template_config column to accept valid JSON including empty objects, so that the system can store minimal configurations.

#### Acceptance Criteria

1. WHEN the database column is JSON type THEN the System SHALL accept "{}" as valid input
2. WHEN inserting a record with template_config = "{}" THEN the System SHALL complete successfully
3. WHEN updating a record with template_config = "{}" THEN the System SHALL complete successfully
4. WHEN querying records THEN the System SHALL return template_config as valid JSON string
5. WHEN template_config is null in database THEN the System SHALL treat it as "{}" when reading

### Requirement 5: Error Recovery

**User Story:** As a user, I want the system to recover gracefully from JSON errors, so that I can continue using the settings page even if data is corrupted.

#### Acceptance Criteria

1. WHEN loading settings with invalid template_config THEN the System SHALL log a warning and use "{}" as default
2. WHEN saving settings fails due to JSON error THEN the System SHALL display a clear error message to the user
3. WHEN JSON parsing fails THEN the System SHALL not crash but return a safe default value
4. WHEN database constraint violation occurs THEN the System SHALL retry with sanitized JSON
5. IF retry fails THEN the System SHALL log the error and inform the user with actionable guidance

### Requirement 6: Backward Compatibility

**User Story:** As a system maintainer, I want the fix to work with existing data, so that no manual data migration is required.

#### Acceptance Criteria

1. WHEN existing records have null template_config THEN the System SHALL handle them as "{}" without migration
2. WHEN existing records have valid JSON THEN the System SHALL preserve the existing values
3. WHEN existing records have empty string THEN the System SHALL treat them as "{}"
4. WHEN reading legacy data THEN the System SHALL apply JSON sanitization automatically
5. WHEN updating legacy records THEN the System SHALL convert invalid JSON to valid JSON transparently

