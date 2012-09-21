SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'type' to table 'kitchen'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`kitchen` ADD COLUMN `type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the type to this taste as below.\n0 - normal\n1 - reserved'  AFTER `member_discount_3` ;

-- -----------------------------------------------------
-- Drop the foreign key between table 'department' and 'restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`department` DROP FOREIGN KEY `fk_super_kitchen_restaurant1` ;

-- -----------------------------------------------------
-- Add the field 'type' to table 'department'
-- Create a compound index on both field 'restaurant_id' and 'dept_id'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`department` ADD COLUMN `type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the type to this taste as below.\n0 - normal\n1 - reserved'  AFTER `name` , CHANGE COLUMN `dept_id` `dept_id` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0'  FIRST 

, DROP PRIMARY KEY 

, ADD PRIMARY KEY (`restaurant_id`, `dept_id`) 

, DROP INDEX `fk_super_kitchen_restaurant` ;

-- -----------------------------------------------------
-- Add the '临时部门' to each restaurant's department
-- -----------------------------------------------------
INSERT INTO wireless_order_db.department (`restaurant_id`, `dept_id`, `name`, `type`)
SELECT id, 253, '临时部门', 1 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Add the '空' to each restaurant's department
-- -----------------------------------------------------
INSERT INTO wireless_order_db.department (`restaurant_id`, `dept_id`, `name`, `type`)
SELECT id, 255, '空', 1 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Add the '临时厨房' to each restaurant's kitchen
-- -----------------------------------------------------
INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `dept_id`, `name`, `type`)
SELECT id, 253, 253, '临时厨房', 1 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Add the '空' to each restaurant's kitchen
-- -----------------------------------------------------
INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `dept_id`, `name`, `type`)
SELECT id, 255, 255, '空', 1 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Update the kitchen_id of the food records whose kitchen_alias equals 253(temporary kitchen) or 255(null kitchen)
-- -----------------------------------------------------
UPDATE wireless_order_db.food FOOD
SET 
kitchen_id = 
(
    SELECT kitchen_id FROM wireless_order_db.kitchen KITCHEN 
    WHERE KITCHEN.kitchen_alias = FOOD.kitchen_alias AND KITCHEN.restaurant_id = FOOD.restaurant_id
)
WHERE FOOD.kitchen_alias = 255;

-- -----------------------------------------------------
-- Update the kitchen_id of the order_food records whose kitchen_alias equals 253(temporary kitchen) or 255(null kitchen)
-- Update the dept_id of the order_food records whose kitchen_alias equals 253(temporary kitchen) or 255(null kitchen)
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food OF 
SET 
kitchen_id = 
(
    SELECT kitchen_id FROM wireless_order_db.kitchen KITCHEN 
    WHERE KITCHEN.kitchen_alias = OF.kitchen_alias AND KITCHEN.restaurant_id = OF.restaurant_id
),
dept_id = 
(
    SELECT dept_id FROM wireless_order_db.kitchen KITCHEN 
    WHERE KITCHEN.kitchen_alias = OF.kitchen_alias AND KITCHEN.restaurant_id = OF.restaurant_id
)
WHERE OF.kitchen_alias = 253 OR OF.kitchen_alias = 255;

-- -----------------------------------------------------
-- Update the kitchen_id of the order_food_history records whose kitchen_alias equals 253(temporary kitchen) or 255(null kitchen)
-- Update the dept_id of the order_food_history records whose kitchen_alias equals 253(temporary kitchen) or 255(null kitchen)
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food_history OFH 
SET 
kitchen_id = 
(
    SELECT kitchen_id FROM wireless_order_db.kitchen KITCHEN 
    WHERE KITCHEN.kitchen_alias = OFH.kitchen_alias AND KITCHEN.restaurant_id = OFH.restaurant_id
),
dept_id = 
(
    SELECT dept_id FROM wireless_order_db.kitchen KITCHEN 
    WHERE KITCHEN.kitchen_alias = OFH.kitchen_alias AND KITCHEN.restaurant_id = OFH.restaurant_id
)
WHERE OFH.kitchen_alias = 253 OR OFH.kitchen_alias = 255;