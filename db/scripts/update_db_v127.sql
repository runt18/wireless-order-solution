SET @OLD_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET NAMES utf8;
SET SQL_SAFE_UPDATES = 0;
USE wireless_order_db;

-- -----------------------------------------------------
-- Rename the field 'kitchen_alias' to 'kitchen_id' to table 'func_kitchen'
-- Add index 'ix_kitchen_id' to table 'func_kitchen'
-- Add index 'ix_restaurant_id' to table 'func_kitchen'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`func_kitchen` 
CHANGE COLUMN `kitchen_alias` `kitchen_id` INT(11) NOT NULL ,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`func_id`, `kitchen_id`),
ADD INDEX `ix_kitchen_id` (`kitchen_id` ASC),
ADD INDEX `ix_restaurant_id` (`restaurant_id` ASC);

-- -----------------------------------------------------
-- Replace kitchen alias with kitchen id to table 'func_kitchen'
-- -----------------------------------------------------
UPDATE wireless_order_db.func_kitchen FK
JOIN wireless_order_db.kitchen K ON FK.kitchen_id = K.kitchen_alias AND FK.restaurant_id = K.restaurant_id
SET FK.kitchen_id = K.kitchen_id;

-- -----------------------------------------------------
-- Add the field 'display_id' to table `kitchen`
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`kitchen` 
ADD COLUMN `display_id` SMALLINT NOT NULL DEFAULT 0 AFTER `type`,
CHANGE COLUMN `type` `type` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'the type to this taste as below.\n0 - normal\n1 - idle\n2 - temp\n3 - null' ;

-- -----------------------------------------------------
-- Update the display id to kitchen alias.
-- -----------------------------------------------------
UPDATE wireless_order_db.kitchen 
SET display_id = kitchen_alias + 1;

-- -----------------------------------------------------
-- Update the type to temporary kitchen whose alias is 253
-- -----------------------------------------------------
UPDATE wireless_order_db.kitchen
SET type = 2, display_id = 0
WHERE kitchen_alias = 253;

-- -----------------------------------------------------
-- Update the type to null kitchen whose alias is 254
-- -----------------------------------------------------
UPDATE wireless_order_db.kitchen
SET type = 3, display_id = 0
WHERE kitchen_alias = 255;

-- -----------------------------------------------------
-- Update the kitchens which has no foods to be idle.
-- -----------------------------------------------------
UPDATE 
wireless_order_db.kitchen K,
(SELECT kitchen_id FROM wireless_order_db.kitchen K
WHERE 1 = 1
AND NOT EXISTS( SELECT * FROM wireless_order_db.food F WHERE F.kitchen_id = K.kitchen_id)
AND K.type = 0
AND K.name LIKE '厨房%'
) AS K_UNUSED
SET 
K.type = 1,
K.dept_id = 255,
K.is_allow_temp = 0
WHERE K.kitchen_id = K_UNUSED.kitchen_id AND K.type = 0;

-- -----------------------------------------------------
-- Drop the field 'kitchen_alias' to table 'kitchen'
-- Replace the index 'ix_kitchen_alias' with 'ix_restaurant_id'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`kitchen` 
DROP COLUMN `kitchen_alias`,
DROP INDEX `ix_kitchen_alias_id` ,
ADD INDEX `ix_restaurant_id` USING BTREE (`restaurant_id` ASC);

-- -----------------------------------------------------
-- Change the comment of field 'type' to table 'department'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`department` 
ADD COLUMN `display_id` SMALLINT NOT NULL DEFAULT 0 AFTER `type`,
CHANGE COLUMN `type` `type` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'the type to this department as below.\n0 - normal (普通)\n1 - idle (空闲)\n2 - ware_house (总仓)\n3 - temp (临时)\n4 - null (空)' ;

-- -----------------------------------------------------
-- Update the display_id to dept_id
-- -----------------------------------------------------
UPDATE wireless_order_db.department
SET display_id = dept_id + 1;

-- -----------------------------------------------------
-- Update the type to warehouse department whose dept_id is 252
-- -----------------------------------------------------
UPDATE wireless_order_db.department
SET type = 2, display_id = 0
WHERE dept_id = 252;

-- -----------------------------------------------------
-- Update the type to temporary department whose dept_id is 253
-- -----------------------------------------------------
UPDATE wireless_order_db.department
SET type = 3, display_id = 0
WHERE dept_id = 253;

-- -----------------------------------------------------
-- Update the type to null department whose dept_id is 255
-- -----------------------------------------------------
UPDATE wireless_order_db.department
SET type = 4, display_id = 0
WHERE dept_id = 255;

-- -----------------------------------------------------
-- Update the department which has no kitchens to be idle.
-- -----------------------------------------------------
UPDATE 
wireless_order_db.department D,
(SELECT dept_id, restaurant_id FROM wireless_order_db.department D
WHERE 1 = 1
AND NOT EXISTS( SELECT * FROM wireless_order_db.kitchen K WHERE K.dept_id = D.dept_id AND K.restaurant_id = D.restaurant_id)
AND D.type = 0
AND D.name LIKE '部门%'
) AS D_UNUSED
SET 
D.type = 1
WHERE D.dept_id = D_UNUSED.dept_id AND D.restaurant_id = D_UNUSED.restaurant_id AND D.type = 0;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`weixin_order`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`weixin_order` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`weixin_order` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `weixin_serial` VARCHAR(45) NOT NULL,
  `weixin_serial_crc` BIGINT NOT NULL,
  `birth_date` DATETIME NOT NULL,
  `order_id` INT NULL DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status as below\n1 - 未使用\n2 - 已使用',
  `code` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC),
  INDEX `ix_weixin_serial_crc` (`weixin_serial_crc` ASC),
  INDEX `ix_order_id` (`order_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`weixin_order_food`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`weixin_order_food` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`weixin_order_food` (
  `weixin_order_id` INT NOT NULL,
  `food_id` INT NOT NULL,
  `food_count` FLOAT NOT NULL DEFAULT 0,
  PRIMARY KEY (`weixin_order_id`, `food_id`),
  INDEX `ix_food_id` (`food_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Add the index 'ix_discount_id' to table 'member_type_discount'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_type_discount` 
ADD INDEX `ix_discount_id` (`discount_id` ASC);

-- -----------------------------------------------------
-- Add the index 'ix_food_id' to table 'member_favor_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_favor_food` 
ADD INDEX `ix_food_id` (`food_id` ASC);

-- -----------------------------------------------------
-- Add the index 'ix_food_id' to table 'member_recommend_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_recommend_food` 
ADD INDEX `ix_food_id` (`food_id` ASC);

-- -----------------------------------------------------
-- Add the index 'ix_kitchen_id' to table `food`
-- Drop the field 'kitchen_alias' to table 'food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`food` 
DROP COLUMN `kitchen_alias`,
CHANGE COLUMN `kitchen_id` `kitchen_id` INT(11) NOT NULL COMMENT 'the kitchen id the food belong to' ,
ADD INDEX `ix_kitchen_id` (`kitchen_id` ASC);

-- -----------------------------------------------------
-- Drop the field 'price_plan_id' to table 'order' 
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
DROP COLUMN `price_plan_id`;

-- -----------------------------------------------------
-- Drop the field 'kitchen_alias' & 'food_alias' to table 'order_food' and 'order_food_history'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food
SET food_id = CRC32(name) % 1000000000
WHERE is_temporary = 1;

UPDATE wireless_order_db.order_food_history
SET food_id = CRC32(name) % 1000000000
WHERE is_temporary = 1;

ALTER TABLE `wireless_order_db`.`order_food` 
DROP COLUMN `kitchen_alias`,
DROP COLUMN `food_alias`;
ALTER TABLE `wireless_order_db`.`order_food_history` 
DROP COLUMN `kitchen_alias`,
DROP COLUMN `food_alias`;

-- -----------------------------------------------------
-- Add the field 'price' & 'commission' to table 'food'
-- Drop the field 'pinyin' to table 'food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`food` 
DROP COLUMN `pinyin`,
ADD COLUMN `price` FLOAT NOT NULL DEFAULT 0 AFTER `name`,
ADD COLUMN `commission` FLOAT NULL DEFAULT NULL AFTER `price`,
DROP INDEX `ix_food_alias_id` ,
ADD INDEX `ix_restaurant_id` (`restaurant_id` ASC);

-- -----------------------------------------------------
-- Update the price and commission to each food
-- -----------------------------------------------------
UPDATE wireless_order_db.food F
JOIN wireless_order_db.food_price_plan FPP ON F.food_id = FPP.food_id
JOIN wireless_order_db.price_plan PP ON FPP.price_plan_id = PP.price_plan_id AND PP.status = 1
SET F.price = FPP.unit_price, F.commission = FPP.commission;

SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;