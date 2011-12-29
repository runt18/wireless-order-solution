SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Modify the `region` table
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`region` 
DROP COLUMN `id` ,
CHANGE COLUMN `alias_id` `region_id` TINYINT(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this table region'  , 
DROP PRIMARY KEY , 
ADD PRIMARY KEY (`restaurant_id`, `region_id`) , 
DROP INDEX `fk_region_restaurant1` , 
ADD INDEX `fk_region_restaurant` (`restaurant_id` ASC) ;

-- -----------------------------------------------------
-- Modify the `table` table
-- 1、rename the field "region" to "region_id"
-- 2、add the field "custom_num"
-- 3、add the field "category"
-- 4、add the field "status"
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`table` 
ADD COLUMN `custom_num` TINYINT UNSIGNED DEFAULT NULL COMMENT 'the amount of customer to this table if the status is not idle'  AFTER `enabled` , 
ADD COLUMN `category` TINYINT DEFAULT NULL COMMENT 'the category to this table, it should be one the values below.\n一般 : 1\n外卖 : 2\n并台 : 3\n拼台 : 4'  AFTER `custom_num` , 
ADD COLUMN `status` TINYINT COMMENT 'the status to this table, one of the values below.\n空闲 : 0\n就餐 : 1\n预定 : 2'  AFTER `category` , 
CHANGE COLUMN `minimum_cost` `minimum_cost` DECIMAL(7,2) NOT NULL DEFAULT '0.00' COMMENT 'the minimum cost to this table'  AFTER `name` , 
CHANGE COLUMN `region` `region_id` TINYINT(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the region alias id to this table.'  ;

-- -----------------------------------------------------
-- Update the field 'region_id' to 0
-- -----------------------------------------------------
UPDATE wireless_order_db.table SET region_id = 0;

-- -----------------------------------------------------
-- Update the custom_num, category, status of table.
-- Note that removing the NOT NULL restriction of field status before invoking the update.
-- -----------------------------------------------------
UPDATE wireless_order_db.table a
SET a.custom_num = (SELECT custom_num FROM wireless_order_db.order b
		    WHERE b.id = (SELECT max(id) FROM wireless_order_db.order b1
                                  WHERE a.restaurant_id = b1.restaurant_id
                                    AND a.alias_id = b1.table_id)
                      AND b.total_price IS NULL),
a.category = (SELECT category FROM wireless_order_db.order c
	       WHERE c.id = (SELECT max(id) FROM wireless_order_db.order c1
                              WHERE a.restaurant_id = c1.restaurant_id
                                AND a.alias_id = c1.table_id)
	              AND c.total_price is null),
a.status = (SELECT (CASE WHEN d.total_price IS NULL THEN 1 ELSE 0 END)
              FROM wireless_order_db.order d
	     WHERE d.id = (SELECT max(id) FROM wireless_order_db.order d1
                            WHERE a.restaurant_id = d1.restaurant_id
                              AND a.alias_id = d1.table_id));

UPDATE wireless_order_db.table
SET status = 0
WHERE status IS NULL;

-- -----------------------------------------------------
-- Restore field 'status' to NOT NULL 
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`table`
CHANGE COLUMN `status` `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'the status to this table, one of the values below.\n空闲 : 0\n就餐 : 1\n预定 : 2'  AFTER `category`;