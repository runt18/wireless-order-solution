SET @OLD_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET NAMES utf8;
SET SQL_SAFE_UPDATES = 0;
USE wireless_order_db;

-- -----------------------------------------------------
-- Drop the field 'discount_type' & 'discount_id' from table 'member_type'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_type` DROP COLUMN `discount_type` , DROP COLUMN `discount_id` ;

-- -----------------------------------------------------
-- Add the field 'liveness' & 'member_card'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member` ADD COLUMN `liveness` FLOAT NULL DEFAULT 0  AFTER `member_card` , 
ADD COLUMN `last_consumption` DATETIME NULL DEFAULT NULL  AFTER `liveness` ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_type_discount`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_type_discount` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_type_discount` (
  `member_type_id` INT NOT NULL ,
  `discount_id` INT NOT NULL ,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type to this member discount as below.\n1 - 普通\n2 - 默认' ,
  PRIMARY KEY (`member_type_id`, `discount_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Set the default discount to each member type
-- -----------------------------------------------------
INSERT INTO wireless_order_db.member_type_discount
(`member_type_id`, `discount_id`, `type`)
SELECT member_type_id, discount_id, 2 FROM
wireless_order_db.member_type MT
LEFT JOIN wireless_order_db.discount DIST
ON MT.restaurant_id = DIST.restaurant_id AND (DIST.status = 2 OR DIST.status = 3);

-- -----------------------------------------------------
-- Update the restaurant which does NOT has default & reserved discount
-- to has default discount
-- -----------------------------------------------------
UPDATE wireless_order_db.discount D,
(
SELECT DIST.restaurant_id FROM wireless_order_db.discount DIST
WHERE DIST.restaurant_id NOT IN( SELECT restaurant_id FROM wireless_order_db.discount WHERE (status = 2 OR status = 3) )
GROUP BY DIST.restaurant_id) AS TMP
SET status = 3
WHERE 1 = 1
AND D.restaurant_id = TMP.restaurant_id
AND D.level = 201;

-- -----------------------------------------------------
-- Delete the discount whose status is 4(means member)
-- -----------------------------------------------------
DELETE FROM wireless_order_db.discount WHERE status = 4;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_favor_food`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_favor_food` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`member_favor_food` (
  `member_id` INT NOT NULL,
  `food_id` INT NOT NULL,
  `point` FLOAT NOT NULL DEFAULT 0,
  PRIMARY KEY (`member_id`, `food_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_recommend_food`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_recommend_food` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`member_recommend_food` (
  `member_id` INT NOT NULL,
  `food_id` INT NOT NULL,
  `point` FLOAT NOT NULL DEFAULT 0,
  PRIMARY KEY (`member_id`, `food_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`interested_member`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`interested_member` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`interested_member` (
  `staff_id` INT NOT NULL,
  `member_id` INT NOT NULL,
  `start_date` DATETIME NULL,
  PRIMARY KEY (`staff_id`, `member_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_comment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_comment` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`member_comment` (
  `member_id` INT NOT NULL,
  `staff_id` INT NOT NULL,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type to member comment as below.\n1 - public\n2 - private',
  `comment` VARCHAR(500) NULL DEFAULT NULL,
  `last_modified` DATETIME NOT NULL,
  INDEX `ix_member_id` (`member_id` ASC),
  INDEX `ix_staff_id` (`staff_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`weixin_restaurant`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`weixin_restaurant` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`weixin_restaurant` (
  `weixin_serial` VARCHAR(45) NOT NULL,
  `restaurant_id` INT NOT NULL,
  `weixin_serial_crc` INT NOT NULL,
  INDEX `ix_finance_crc` (`weixin_serial_crc` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Add the field 'joint_probability' & 'mutual_info' to table 'food_association'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`food_association` 
ADD COLUMN `joint_probability` FLOAT NOT NULL DEFAULT 0 AFTER `associated_amount`,
ADD COLUMN `similarity` FLOAT NOT NULL DEFAULT 0 AFTER `joint_probability`;

-- -----------------------------------------------------
-- Add the field 'probability' to table 'food_statistics'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`food_statistics` 
ADD COLUMN `probability` FLOAT NOT NULL DEFAULT 0 AFTER `order_cnt`;


SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;