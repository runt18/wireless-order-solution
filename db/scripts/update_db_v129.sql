SET @OLD_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET NAMES utf8;
SET SQL_SAFE_UPDATES = 0;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'is_gift' to table 'order_food' & 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` 
CHANGE COLUMN `hang_status` `is_gift` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'indicates the order food is gift' ;

ALTER TABLE `wireless_order_db`.`order_food_history` 
ADD COLUMN `is_gift` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates the order food is gift' AFTER `is_paid`;

-- -----------------------------------------------------
-- Update the the gift status whose record belongs to gift
-- -----------------------------------------------------
UPDATE `wireless_order_db`.`order_food`
SET is_gift = 1
WHERE (food_status & (1 << 3)) <> 0;

UPDATE `wireless_order_db`.`order_food_history`
SET is_gift = 1
WHERE (food_status & (1 << 3)) <> 0;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`combo_order_food`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`combo_order_food` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`combo_order_food` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `combo_id` INT UNSIGNED NOT NULL,
  `food_id` INT NOT NULL,
  `food_name` VARCHAR(45) NULL,
  `food_amount` INT NOT NULL DEFAULT 0,
  `taste_group_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_combo_id` (`combo_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Add the 'combo_id' to table 'order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` 
ADD COLUMN `combo_id` INT UNSIGNED NULL DEFAULT NULL AFTER `taste_group_id`;

UPDATE wireless_order_db.order_food
SET restaurant_id = 1, order_id = 0
WHERE restaurant_id = 0;

-- -----------------------------------------------------
-- Add the field 'image' to table 'coupon_type'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`coupon_type` 
ADD COLUMN `image` VARCHAR(50) NULL DEFAULT NULL AFTER `comment`;

-- -----------------------------------------------------
-- Add the field 'weixin_card' to table 'weixin_member' as primary key
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_member` 
AUTO_INCREMENT = 10000000 ,
ADD COLUMN `weixin_card` INT UNSIGNED NOT NULL AUTO_INCREMENT FIRST,
ADD PRIMARY KEY (`weixin_card`);


-- -----------------------------------------------------
-- Modify the field 'weixin_serial_crc' to unsigned int
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_member` 
CHANGE COLUMN `weixin_serial_crc` `weixin_serial_crc` INT UNSIGNED NOT NULL ;

-- -----------------------------------------------------
-- Add the field 'mobile_crc' & 'member_card_crc' to table 'member'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member` 
ADD COLUMN `mobile_crc` INT UNSIGNED NULL AFTER `mobile`,
ADD COLUMN `member_card_crc` INT UNSIGNED NULL AFTER `member_card`,
ADD INDEX `ix_mobile_crc` (`mobile_crc` ASC),
ADD INDEX `ix_member_card_crc` (`member_card_crc` ASC);

-- -----------------------------------------------------
-- Update the crc to mobile and member card
-- -----------------------------------------------------
UPDATE wireless_order_db.member
SET mobile_crc = CRC32(mobile),
member_card_crc = CRC32(member_card);

-- -----------------------------------------------------
-- Add the field 'restaurant_id' to table 'taste_group'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`taste_group` 
ADD COLUMN `restaurant_id` INT NOT NULL AFTER `taste_group_id`;

UPDATE `wireless_order_db`.`taste_group` TG
JOIN wireless_order_db.order_food OF ON TG.taste_group_id = OF.taste_group_id
SET TG.restaurant_id = OF.restaurant_id;

UPDATE wireless_order_db.taste_group
SET restaurant_id = 0
WHERE taste_group_id = 1;

-- -----------------------------------------------------
-- Add the region 11 - 20
-- -----------------------------------------------------
INSERT INTO wireless_order_db.region
(restaurant_id, region_id, name)
SELECT id, 10, '区域11'
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.region
(restaurant_id, region_id, name)
SELECT id, 11, '区域12'
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.region
(restaurant_id, region_id, name)
SELECT id, 12, '区域13'
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.region
(restaurant_id, region_id, name)
SELECT id, 13, '区域14'
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.region
(restaurant_id, region_id, name)
SELECT id, 14, '区域15'
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.region
(restaurant_id, region_id, name)
SELECT id, 15, '区域16'
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.region
(restaurant_id, region_id, name)
SELECT id, 16, '区域17'
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.region
(restaurant_id, region_id, name)
SELECT id, 17, '区域18'
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.region
(restaurant_id, region_id, name)
SELECT id, 18, '区域19'
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.region
(restaurant_id, region_id, name)
SELECT id, 19, '区域20'
FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Add the field 'status' & 'display_id' to table 'region'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`region` 
ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status as below\n 1 - busy\n 2 - idle' AFTER `name`,
ADD COLUMN `display_id` TINYINT NOT NULL DEFAULT 0 AFTER `status`;

-- -----------------------------------------------------
-- Add the index 'ix_region_id'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`table` 
ADD INDEX `ix_region_id` (`region_id` ASC);

-- -----------------------------------------------------
-- Update the display_id to each region
-- -----------------------------------------------------
UPDATE wireless_order_db.region
SET display_id = region_id + 1;

-- -----------------------------------------------------
-- Update the region which has no tables to be idle.
-- -----------------------------------------------------
UPDATE 
wireless_order_db.region R,
(SELECT region_id, restaurant_id FROM wireless_order_db.region R
WHERE 1 = 1
AND NOT EXISTS( SELECT * FROM wireless_order_db.table T WHERE T.region_id = R.region_id AND T.restaurant_id = R.restaurant_id)
AND R.name LIKE '区域%'
) AS R_UNUSED
SET 
R.status = 2
WHERE R.region_id = R_UNUSED.region_id AND R.restaurant_id = R_UNUSED.restaurant_id;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`service_rate`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`service_rate` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`service_rate` (
  `rate_id` INT NOT NULL AUTO_INCREMENT,
  `plan_id` INT NOT NULL,
  `restaurant_id` INT NOT NULL,
  `region_id` INT NOT NULL,
  `rate` FLOAT NOT NULL DEFAULT 0,
  PRIMARY KEY (`rate_id`),
  INDEX `ix_region_id` (`restaurant_id` ASC, `region_id` ASC),
  INDEX `ix_plan_id` (`plan_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`service_plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`service_plan` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`service_plan` (
  `plan_id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type as below.\n1 - normal\n2 - reserved',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status as below\n1 - normal\n2 - reserved',
  PRIMARY KEY (`plan_id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Insert '免服务费' plan to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.service_plan
(restaurant_id, name, type, status)
SELECT id, '免服务费', 2, 2
FROM wireless_order_db.restaurant WHERE id > 10;

SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



