-- ============================================
-- QUICK FIX: category_id Column Size
-- ============================================
-- This is a simplified version for immediate execution
-- For full verification, use database-migration-category-id.sql
-- ============================================

USE `meituan_product`;

-- Execute the fix
ALTER TABLE `product` 
MODIFY COLUMN `category_id` VARCHAR(255) NOT NULL COMMENT '类目ID';

-- Verify the change
SHOW CREATE TABLE product;

-- Done!
SELECT 'category_id column updated to VARCHAR(255)' AS result;
