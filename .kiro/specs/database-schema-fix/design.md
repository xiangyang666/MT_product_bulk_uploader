# Design Document

## Overview

This design addresses the database schema limitation where the `category_id` column in the `product` table is defined as `VARCHAR(20)`, causing data truncation errors when importing products with longer category IDs. The solution involves executing a SQL ALTER TABLE statement to increase the column size to `VARCHAR(255)`, which provides sufficient capacity for all known Meituan category ID formats while maintaining backward compatibility with existing code.

## Architecture

The fix is a database schema modification that requires no changes to application code. The architecture remains unchanged:

- **Database Layer**: MySQL 8.0+ with utf8mb4 character set
- **Application Layer**: Spring Boot backend with MyBatis Plus ORM
- **Data Access**: Existing mappers and services continue to function without modification

### Migration Strategy

1. **Direct SQL Execution**: Execute ALTER TABLE statement directly on the database
2. **Zero Downtime**: The ALTER TABLE operation on VARCHAR columns is typically fast and non-blocking in MySQL 8.0+
3. **No Code Changes**: The application code already handles category_id as a String without length restrictions

## Components and Interfaces

### Database Schema

**Current Schema:**
```sql
`category_id` VARCHAR(20) NOT NULL COMMENT '类目ID（10位数字）'
```

**Updated Schema:**
```sql
`category_id` VARCHAR(255) NOT NULL COMMENT '类目ID'
```

### Migration Script

The migration will be executed using a SQL ALTER TABLE statement:

```sql
ALTER TABLE `product` 
MODIFY COLUMN `category_id` VARCHAR(255) NOT NULL COMMENT '类目ID';
```

### Affected Components

1. **database-init.sql**: Update the schema definition for future deployments
2. **Product Table**: The target of the schema modification
3. **MeituanFormatParser**: No changes required - already extracts category_id as String
4. **ProductService**: No changes required - already handles String values
5. **ProductMapper**: No changes required - MyBatis Plus handles the column automatically

## Data Models

### Product Entity

The Product entity already defines category_id as a String with no length restriction:

```java
@TableName("product")
public class Product {
    private String categoryId;  // No @Length annotation - accepts any length
    // ... other fields
}
```

This design is already compatible with the schema change.

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Column size sufficiency
*For any* category_id string with length between 1 and 255 characters, when inserted into the product table, the stored value SHALL equal the original value without truncation
**Validates: Requirements 1.1, 1.2, 4.1, 4.2, 4.4**

### Property 2: Parser backward compatibility
*For any* product data with category_id of any length up to 255 characters, the MeituanFormatParser SHALL extract the complete category_id value without modification
**Validates: Requirements 3.1**

### Property 3: Storage and retrieval round-trip
*For any* product with a category_id value, storing the product to the database and then retrieving it SHALL return the exact same category_id value
**Validates: Requirements 3.2, 3.3**

### Property 4: Index functionality preservation
*For any* category_id value, querying products by that category_id SHALL return all and only products with that exact category_id value
**Validates: Requirements 1.5, 3.4, 4.3**

## Error Handling

### Migration Errors

1. **Connection Failure**: If database connection fails, provide clear error message with connection details
2. **Permission Denied**: If user lacks ALTER TABLE privileges, provide error message indicating required permissions
3. **Table Lock Timeout**: If table is locked by other operations, retry with exponential backoff
4. **Syntax Error**: If SQL syntax is invalid, provide the exact SQL statement and error message

### Runtime Errors

After migration, the following error scenarios should be handled:

1. **Oversized Category ID**: If category_id exceeds 255 characters (edge case), log warning and truncate with notification
2. **Invalid Characters**: Existing validation in MeituanFormatParser handles invalid characters
3. **NULL Values**: Database constraint prevents NULL values

## Testing Strategy

### Manual Testing

1. **Pre-Migration Verification**:
   - Query existing products and record category_id values
   - Verify current column definition using `SHOW CREATE TABLE product`
   - Test import with category_id > 20 characters (should fail with truncation error)

2. **Migration Execution**:
   - Execute ALTER TABLE statement
   - Verify no errors during execution
   - Check execution time (should be < 1 second for typical table sizes)

3. **Post-Migration Verification**:
   - Verify column definition changed to VARCHAR(255)
   - Query all products and verify category_id values unchanged
   - Test import with category_id > 20 characters (should succeed)
   - Test import with category_id > 255 characters (should fail gracefully)

### Integration Testing

1. **Import Test**: Import a batch of products with varying category_id lengths (10, 20, 50, 100, 200 characters)
2. **Query Test**: Query products by category_id and verify correct results
3. **Update Test**: Update existing products with new category_id values of various lengths
4. **Performance Test**: Verify query performance on category_id index remains acceptable

### Property-Based Testing

Property-based testing will use **JUnit 5** with **jqwik** library for Java.

Each property test will:
- Run a minimum of 100 iterations
- Generate random test data within valid ranges
- Verify the correctness property holds for all generated inputs

Property tests will be tagged with comments referencing the design document properties:
- Format: `// Feature: database-schema-fix, Property {number}: {property_text}`

## Implementation Plan

### Phase 1: Schema Update

1. **Update database-init.sql**: Modify the CREATE TABLE statement to use VARCHAR(255)
2. **Create migration script**: Document the ALTER TABLE statement for existing databases
3. **Execute migration**: Run ALTER TABLE on development, staging, and production databases

### Phase 2: Verification

1. **Verify schema change**: Confirm column definition updated
2. **Verify data integrity**: Confirm all existing data preserved
3. **Test import functionality**: Import products with long category_id values
4. **Monitor performance**: Verify no performance degradation

### Phase 3: Documentation

1. **Update README**: Document the schema change and migration process
2. **Update troubleshooting guide**: Add solution for category_id truncation errors
3. **Create migration log**: Record migration execution details and results

## Rollback Plan

If issues arise after migration:

1. **Immediate Rollback**: Execute reverse ALTER TABLE to restore VARCHAR(20)
   ```sql
   ALTER TABLE `product` 
   MODIFY COLUMN `category_id` VARCHAR(20) NOT NULL COMMENT '类目ID（10位数字）';
   ```
   
2. **Data Validation**: Verify no data loss during rollback (products with category_id > 20 chars will fail)
3. **Application Restart**: Restart backend service to clear any cached schema information

Note: Rollback will cause the original truncation issue to return. Only rollback if critical issues are discovered.

## Performance Considerations

### Storage Impact

- **Before**: VARCHAR(20) = max 20 bytes per row (utf8mb4)
- **After**: VARCHAR(255) = max 255 bytes per row (utf8mb4)
- **Actual Impact**: Minimal, as VARCHAR only uses space for actual data + 1-2 bytes for length

### Index Impact

- The index on category_id will be rebuilt automatically during ALTER TABLE
- Index size will increase proportionally to actual data length, not max length
- Query performance should remain unchanged for existing data

### Migration Time

- ALTER TABLE on VARCHAR columns in MySQL 8.0+ uses ALGORITHM=INSTANT when possible
- Expected migration time: < 1 second for tables with < 100,000 rows
- No table locking required for this operation in MySQL 8.0.12+

## Security Considerations

1. **SQL Injection**: The ALTER TABLE statement is static and not vulnerable to injection
2. **Data Exposure**: No sensitive data is exposed during migration
3. **Access Control**: Requires ALTER TABLE privilege on the database
4. **Audit Trail**: Log the migration execution in operation_log table

## Dependencies

- MySQL 8.0+ (for ALGORITHM=INSTANT support)
- Database user with ALTER TABLE privilege
- No application code dependencies (backward compatible)

## Future Considerations

1. **Dynamic Column Sizing**: Consider implementing a configuration-based column size management system
2. **Schema Versioning**: Implement a schema version tracking mechanism for future migrations
3. **Automated Migration**: Create a migration framework for handling schema changes programmatically
4. **Monitoring**: Add monitoring for column size utilization to detect future sizing issues early
