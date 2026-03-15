-- 添加品牌字段到商品表
ALTER TABLE t_product ADD COLUMN brand VARCHAR(255) COMMENT '品牌' AFTER weight_unit;
