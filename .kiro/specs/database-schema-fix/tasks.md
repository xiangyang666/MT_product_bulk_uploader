# Tasks Document

## Overview

This document outlines the implementation tasks for fixing the database schema issue where the `category_id` column is too small (VARCHAR(20)) to accommodate actual Meituan category IDs.

## Task List

### Task 1: Update database-init.sql Schema Definition
**Status**: Not Started  
**Type**: Required  
**Estimated Time**: 5 minutes

**Description**: Update the CREATE TABLE statement in `database-init.sql` to define `category_id` as VARCHAR(255) instead of VARCHAR(20).

**Acceptance Criteria**:
- [ ] Line containing `category_id` VARCHAR(20) is updated to VARCHAR(255)
- [ ] Comment is updated to remove "(10位数字)" reference
- [ ] File syntax is valid SQL
- [ ] No other schema definitions are modified

**Implementation Notes**:
```sql
-- Change from:
`category_id` VARCHAR(20) NOT NULL COMMENT '类目ID（10位数字）',

-- Change to:
`category_id` VARCHAR(255) NOT NULL COMMENT '类目ID',
```

---

### Task 2: Create Migration Script Documentation
**Status**: Not Started  
**Type**: Required  
**Estimated Time**: 10 minutes

**Description**: Create a migration script file that documents the ALTER TABLE command for updating existing databases.

**Acceptance Criteria**:
- [ ] Create file `database-migration-category-id.sql` in project root
- [ ] Include pre-migration verification queries
- [ ] Include the ALTER TABLE statement
- [ ] Include post-migration verification queries
- [ ] Include rollback instructions
- [ ] Document execution instructions

**Implementation Notes**:
- File should be executable as a standalone SQL script
- Include comments explaining each step
- Add safety checks before execution

---

### Task 3: Execute Migration on Development Database
**Status**: Not Started  
**Type**: Required  
**Estimated Time**: 5 minutes

**Description**: Execute the ALTER TABLE migration on the development database and verify success.

**Acceptance Criteria**:
- [ ] Connect to development database
- [ ] Execute pre-migration verification
- [ ] Execute ALTER TABLE statement
- [ ] Verify column definition changed to VARCHAR(255)
- [ ] Verify all existing data preserved
- [ ] Record migration execution time

**Implementation Notes**:
- Use MySQL client or database management tool
- Capture and log any errors
- Take database backup before execution (recommended)

---

### Task 4: Verify Schema Change and Data Integrity
**Status**: Not Started  
**Type**: Required  
**Estimated Time**: 10 minutes

**Description**: Verify that the schema change was successful and all existing data remains intact.

**Acceptance Criteria**:
- [ ] Run `SHOW CREATE TABLE product` and verify VARCHAR(255)
- [ ] Query all products and verify count matches pre-migration count
- [ ] Verify all category_id values are unchanged
- [ ] Verify indexes on category_id remain functional
- [ ] Verify NOT NULL constraint is preserved

**Implementation Notes**:
```sql
-- Verification queries
SHOW CREATE TABLE product;
SELECT COUNT(*) FROM product;
SELECT DISTINCT category_id FROM product ORDER BY category_id;
EXPLAIN SELECT * FROM product WHERE category_id = '2000010001';
```

---

### Task 5: Test Import with Long Category IDs
**Status**: Not Started  
**Type**: Required  
**Estimated Time**: 15 minutes

**Description**: Test the batch import functionality with products that have category_id values longer than 20 characters.

**Acceptance Criteria**:
- [ ] Create test Excel file with category_id values of 25, 50, 100, 200, 255 characters
- [ ] Import test file through the application
- [ ] Verify import succeeds without truncation errors
- [ ] Query imported products and verify complete category_id values stored
- [ ] Verify products can be queried by long category_id values

**Implementation Notes**:
- Use the Meituan format import feature
- Test both standard format and Meituan format
- Verify error handling for category_id > 255 characters

---

### Task 6: Property Test - Column Size Sufficiency
**Status**: Not Started  
**Type**: Optional  
**Estimated Time**: 30 minutes

**Description**: Implement property-based test to verify that category_id values up to 255 characters are stored without truncation.

**Property**: For any category_id string with length between 1 and 255 characters, when inserted into the product table, the stored value SHALL equal the original value without truncation.

**Acceptance Criteria**:
- [ ] Create test class `CategoryIdColumnSizePropertyTest.java`
- [ ] Use jqwik library for property-based testing
- [ ] Generate random category_id strings of length 1-255
- [ ] Insert products with generated category_id values
- [ ] Retrieve products and verify category_id matches original
- [ ] Run minimum 100 iterations
- [ ] All test iterations pass

**Implementation Notes**:
```java
// Feature: database-schema-fix, Property 1: Column size sufficiency
@Property
void categoryIdStoredWithoutTruncation(@ForAll @StringLength(min = 1, max = 255) String categoryId) {
    // Test implementation
}
```

---

### Task 7: Property Test - Parser Backward Compatibility
**Status**: Not Started  
**Type**: Optional  
**Estimated Time**: 30 minutes

**Description**: Implement property-based test to verify that MeituanFormatParser extracts category_id values of any length correctly.

**Property**: For any product data with category_id of any length up to 255 characters, the MeituanFormatParser SHALL extract the complete category_id value without modification.

**Acceptance Criteria**:
- [ ] Create test class `CategoryIdParserPropertyTest.java`
- [ ] Generate test Excel data with varying category_id lengths
- [ ] Parse Excel data using MeituanFormatParser
- [ ] Verify extracted category_id matches original value
- [ ] Run minimum 100 iterations
- [ ] All test iterations pass

**Implementation Notes**:
```java
// Feature: database-schema-fix, Property 2: Parser backward compatibility
@Property
void parserExtractsCompleteCategoryId(@ForAll @StringLength(min = 1, max = 255) String categoryId) {
    // Test implementation
}
```

---

### Task 8: Property Test - Storage and Retrieval Round-Trip
**Status**: Not Started  
**Type**: Optional  
**Estimated Time**: 30 minutes

**Description**: Implement property-based test to verify that category_id values survive a complete round-trip through storage and retrieval.

**Property**: For any product with a category_id value, storing the product to the database and then retrieving it SHALL return the exact same category_id value.

**Acceptance Criteria**:
- [ ] Create test class `CategoryIdRoundTripPropertyTest.java`
- [ ] Generate products with random category_id values (1-255 chars)
- [ ] Save products to database using ProductService
- [ ] Retrieve products from database
- [ ] Verify retrieved category_id equals original
- [ ] Run minimum 100 iterations
- [ ] All test iterations pass

**Implementation Notes**:
```java
// Feature: database-schema-fix, Property 3: Storage and retrieval round-trip
@Property
void categoryIdRoundTripPreservesValue(@ForAll @StringLength(min = 1, max = 255) String categoryId) {
    // Test implementation
}
```

---

### Task 9: Property Test - Index Functionality Preservation
**Status**: Not Started  
**Type**: Optional  
**Estimated Time**: 30 minutes

**Description**: Implement property-based test to verify that the category_id index continues to function correctly after the schema change.

**Property**: For any category_id value, querying products by that category_id SHALL return all and only products with that exact category_id value.

**Acceptance Criteria**:
- [ ] Create test class `CategoryIdIndexPropertyTest.java`
- [ ] Generate multiple products with same category_id
- [ ] Generate products with different category_id values
- [ ] Query by specific category_id
- [ ] Verify query returns correct products (no false positives/negatives)
- [ ] Verify query uses index (check EXPLAIN plan)
- [ ] Run minimum 100 iterations
- [ ] All test iterations pass

**Implementation Notes**:
```java
// Feature: database-schema-fix, Property 4: Index functionality preservation
@Property
void categoryIdIndexReturnsCorrectProducts(@ForAll @StringLength(min = 1, max = 255) String categoryId) {
    // Test implementation
}
```

---

### Task 10: Execute Migration on Staging Database
**Status**: Not Started  
**Type**: Optional  
**Estimated Time**: 10 minutes

**Description**: Execute the migration on staging environment to validate the process before production deployment.

**Acceptance Criteria**:
- [ ] Connect to staging database
- [ ] Execute migration script
- [ ] Verify schema change successful
- [ ] Run integration tests on staging
- [ ] Verify application functions correctly
- [ ] Document any issues encountered

**Implementation Notes**:
- Follow same process as development migration
- Coordinate with QA team for testing
- Monitor application logs for errors

---

### Task 11: Execute Migration on Production Database
**Status**: Not Started  
**Type**: Optional  
**Estimated Time**: 15 minutes

**Description**: Execute the migration on production database with proper safety measures.

**Acceptance Criteria**:
- [ ] Schedule maintenance window (if required)
- [ ] Create full database backup
- [ ] Verify backup is restorable
- [ ] Execute migration script
- [ ] Verify schema change successful
- [ ] Monitor application for errors
- [ ] Verify no performance degradation
- [ ] Document migration completion

**Implementation Notes**:
- Execute during low-traffic period
- Have rollback plan ready
- Monitor database performance metrics
- Keep backup for at least 7 days

---

### Task 12: Update Documentation
**Status**: Not Started  
**Type**: Required  
**Estimated Time**: 20 minutes

**Description**: Update project documentation to reflect the schema change and provide guidance for future deployments.

**Acceptance Criteria**:
- [ ] Update README.md with schema change notes
- [ ] Update TROUBLESHOOTING.md with solution for truncation errors
- [ ] Create migration log documenting execution details
- [ ] Update database schema documentation
- [ ] Document the change in version control commit message

**Implementation Notes**:
- Include before/after schema definitions
- Document the root cause and solution
- Provide instructions for new deployments
- Add to FAQ if applicable

---

## Checkpoint Tasks

### Checkpoint 1: Schema Update Complete
**After Tasks**: 1, 2  
**Verification**:
- [ ] database-init.sql contains VARCHAR(255) definition
- [ ] Migration script file exists and is documented
- [ ] Code review completed

### Checkpoint 2: Development Migration Complete
**After Tasks**: 3, 4, 5  
**Verification**:
- [ ] Development database schema updated
- [ ] All existing data verified intact
- [ ] Import test with long category_id successful
- [ ] No errors in application logs

### Checkpoint 3: Property Tests Complete (Optional)
**After Tasks**: 6, 7, 8, 9  
**Verification**:
- [ ] All property tests implemented
- [ ] All property tests passing
- [ ] Test coverage meets requirements
- [ ] Tests integrated into CI/CD pipeline

### Checkpoint 4: Production Deployment Complete (Optional)
**After Tasks**: 10, 11  
**Verification**:
- [ ] Staging migration successful
- [ ] Production migration successful
- [ ] Application functioning normally
- [ ] No customer-reported issues

### Checkpoint 5: Documentation Complete
**After Tasks**: 12  
**Verification**:
- [ ] All documentation updated
- [ ] Migration logged
- [ ] Knowledge base updated
- [ ] Team notified of changes

---

## Task Dependencies

```
Task 1 (Update schema) → Task 2 (Create migration script)
Task 2 → Task 3 (Execute dev migration)
Task 3 → Task 4 (Verify migration)
Task 4 → Task 5 (Test import)
Task 5 → Task 6, 7, 8, 9 (Property tests - optional)
Task 5 → Task 10 (Staging migration - optional)
Task 10 → Task 11 (Production migration - optional)
Task 11 → Task 12 (Documentation)
```

---

## Immediate Action Required

**URGENT**: The user is currently experiencing the truncation error in their environment. Before proceeding with the full task list, execute this immediate fix:

### Quick Fix SQL Command

```sql
-- Execute this on your current database to fix the issue immediately
ALTER TABLE `product` 
MODIFY COLUMN `category_id` VARCHAR(255) NOT NULL COMMENT '类目ID';
```

**Execution Steps**:
1. Connect to your MySQL database
2. Select the `meituan_product` database
3. Execute the ALTER TABLE command above
4. Verify with: `SHOW CREATE TABLE product;`
5. Test your import again

This will resolve the immediate error while you work through the complete task list for a comprehensive solution.

---

## Notes

- Tasks marked as "Required" must be completed for the fix to be considered complete
- Tasks marked as "Optional" provide additional validation and can be completed based on project priorities
- Property-based tests (Tasks 6-9) provide comprehensive validation but require additional setup time
- Production deployment (Tasks 10-11) should only be done after thorough testing in development and staging
- Estimated times are approximate and may vary based on environment and experience

