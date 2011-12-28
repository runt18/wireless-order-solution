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
ADD COLUMN `custom_num` TINYINT UNSIGNED COMMENT 'the amount of customer to this table if the status is not idle'  AFTER `enabled` , 
ADD COLUMN `category` TINYINT NOT NULL DEFAULT 1 COMMENT 'the category to this table, it should be one the values below.\n一般 : 1\n外卖 : 2\n并台 : 3\n拼台 : 4'  AFTER `custom_num` , 
ADD COLUMN `status` TINYINT COMMENT 'the status to this table, one of the values below.\n空闲 : 0\n就餐 : 1\n预定 : 2'  AFTER `category` , 
CHANGE COLUMN `minimum_cost` `minimum_cost` DECIMAL(7,2) NOT NULL DEFAULT '0.00' COMMENT 'the minimum cost to this table'  AFTER `name` , 
CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT  , CHANGE COLUMN `region` `region_id` TINYINT(3) UNSIGNED NOT NULL DEFAULT '255' COMMENT 'the region alias id to this table.'  ;


update wireless_order_db.table a
set a.custom_num = (select custom_num from wireless_order_db.order b
		    where b.id = (select max(id) from wireless_order_db.order b1
                                  where a.restaurant_id = b1.restaurant_id
                                    and a.alias_id = b1.table_id)
                      and b.total_price is null),
a.category = (select category from wireless_order_db.order c
	       where c.id = (select max(id) from wireless_order_db.order c1
                              where a.restaurant_id = c1.restaurant_id
                                and a.alias_id = c1.table_id)
	              and c.total_price is null),
a.status = (select (case when d.total_price is null then 1 else 0 end) 
	    from wireless_order_db.order d
	    where d.id = (select max(id) from wireless_order_db.order d1
                                  where a.restaurant_id = d1.restaurant_id
                                    and a.alias_id = d1.table_id));