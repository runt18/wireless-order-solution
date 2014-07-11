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

UPDATE wireless_order_db.member
SET mobile_crc = CRC32(mobile),
member_card_crc = CRC32(member_card);

SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



