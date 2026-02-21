-- ============================================
-- Database Migration Script
-- ============================================
-- Purpose: Fix category_id column size issue
-- Issue: VARCHAR(20) is too small for actual Meituan category IDs
-- Solution: Increase to VARCHAR(255)
-- Date: 2026-02-09
-- Database: meituan_product
-- Table: product
-- ============================================

-- IMPORTANT: Take a backup before running this script!
-- mysqldump -u root -p meituan_product > backup_before_migration_$(date +%Y%m%d_%H%M%S).sql

USE `meituan_product`;

-- ============================================
-- PRE-MIGRATION VERIFICATION
-- ============================================

-- 1. Check current table structure
SELECT 'Step 1: Current table structure' AS info;
SHOW CREATE TABLE product;

-- 2. Count existing products
SELECT 'Step 2: Total products before migration' AS info, COUNT(*) AS product_count FROM product;

-- 3. Check current category_id values
SELECT 'Step 3: Sample category_id values' AS info;
SELECT DISTINCT category_id, LENGTH(category_id) AS length 
FROM product 
ORDER BY LENGTH(category_id) DESC 
LIMIT 10;

-- 4. Check for category_id values longer than 20 characters
SELECT 'Step 4: Category IDs longer than 20 characters' AS info, COUNT(*) AS count
FROM product 
WHERE LENGTH(category_id) > 20;

-- 5. Verify indexes
SELECT 'Step 5: Current indexes on product table' AS info;
SHOW INDEX FROM product WHERE Column_name = 'category_id';

-- ============================================
-- MIGRATION EXECUTION
-- ============================================

SELECT 'EXECUTING MIGRATION...' AS status;

-- Execute the ALTER TABLE statement
-- This operation is typically fast in MySQL 8.0+ (uses ALGORITHM=INSTANT when possible)
ALTER TABLE `product` 
MODIFY COLUMN `category_id` VARCHAR(255) NOT NULL COMMENT '类目ID';

SELECT 'Migration completed successfully!' AS status;

-- ============================================
-- POST-MIGRATION VERIFICATION
-- ============================================

-- 1. Verify new table structure
SELECT 'Step 6: New table structure' AS info;
SHOW CREATE TABLE product;

-- 2. Verify product count unchanged
SELECT 'Step 7: Total products after migration' AS info, COUNT(*) AS product_count FROM product;

-- 3. Verify category_id values unchanged
SELECT 'Step 8: Sample category_id values after migration' AS info;
SELECT DISTINCT category_id, LENGTH(category_id) AS length 
FROM product 
ORDER BY LENGTH(category_id) DESC 
LIMIT 10;

-- 4. Verify indexes still exist
SELECT 'Step 9: Indexes after migration' AS info;
SHOW INDEX FROM product WHERE Column_name = 'category_id';

-- 5. Test query performance (should use index)
SELECT 'Step 10: Query execution plan' AS info;
EXPLAIN SELECT * FROM product WHERE category_id = '2000010001';

-- 6. Verify NOT NULL constraint
SELECT 'Step 11: Checking for NULL category_id values' AS info, COUNT(*) AS null_count 
FROM product WHERE category_id IS NULL;

-- ============================================
-- VALIDATION QUERIES
-- ============================================

-- Test inserting a product with long category_id (should succeed)
SELECT 'Step 12: Testing insert with long category_id' AS info;

INSERT INTO `product` 
(`merchant_id`, `product_name`, `category_id`, `price`, `stock`, `description`, `status`) 
VALUES 
(1, 'Test Product - Long Category ID', 
 '12345678901234567890123456789012345678901234567890',
 99.99, 10, 'Test product for migration verification', 0);

-- Verify the insert
SELECT 'Step 13: Verifying test product insert' AS info;
SELECT id, product_name, category_id, LENGTH(category_id) AS length 
FROM product 
WHERE product_name = 'Test Product - Long Category ID';

-- Clean up test data
DELETE FROM product WHERE product_name = 'Test Product - Long Category ID';
SELECT 'Step 14: Test data cleaned up' AS info;

-- ============================================
-- MIGRATION SUMMARY
-- ============================================

SELECT 'MIGRATION SUMMARY' AS title;
SELECT 'Migration Status: SUCCESS' AS result;
SELECT 'Column Definition: category_id VARCHAR(255) NOT NULL' AS new_definition;
SELECT NOW() AS migration_completed_at;
SELECT VERSION() AS mysql_version;

-- ============================================
-- ROLLBACK INSTRUCTIONS
-- ============================================

/*
IF YOU NEED TO ROLLBACK THIS MIGRATION:

WARNING: Rolling back will cause the original truncation issue to return!
Only rollback if critical issues are discovered.

ROLLBACK SQL:
--------------
USE `meituan_product`;

ALTER TABLE `product` 
MODIFY COLUMN `category_id` VARCHAR(20) NOT NULL COMMENT '类目ID（10位数字）';

-- Verify rollback
SHOW CREATE TABLE product;

NOTE: If any products have category_id > 20 characters, they will cause
errors after rollback. You may need to update or delete those records first.

RESTORE FROM BACKUP:
--------------------
If you took a backup before migration, you can restore it:

mysql -u root -p meituan_product < backup_before_migration_YYYYMMDD_HHMMSS.sql

*/

-- ============================================
-- EXECUTION INSTRUCTIONS
-- ============================================

/*
HOW TO RUN THIS SCRIPT:

Method 1: MySQL Command Line
-----------------------------
mysql -u root -p < database-migration-category-id.sql

Method 2: MySQL Client
----------------------
mysql -u root -p
USE meituan_product;
source database-migration-category-id.sql;

Method 3: MySQL Workbench or Other GUI Tool
--------------------------------------------
1. Open the tool
2. Connect to your database
3. Open this file
4. Execute the script

BEFORE RUNNING:
---------------
1. Take a full database backup
2. Verify backup is restorable
3. Test on development environment first
4. Schedule maintenance window if needed (usually not required)
5. Notify team members

AFTER RUNNING:
--------------
1. Verify all verification queries show expected results
2. Test application import functionality
3. Monitor application logs for errors
4. Keep backup for at least 7 days

ESTIMATED EXECUTION TIME:
-------------------------
- Small tables (< 10,000 rows): < 1 second
- Medium tables (10,000 - 100,000 rows): 1-5 seconds
- Large tables (> 100,000 rows): 5-30 seconds

MySQL 8.0.12+ uses ALGORITHM=INSTANT for this type of change,
so it should be very fast regardless of table size.

*/

-- ============================================
-- END OF MIGRATION SCRIPT
-- ============================================
