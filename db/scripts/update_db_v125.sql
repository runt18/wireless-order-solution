SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Drop the field 'discount_type' & 'discount_id' from table 'member_type'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_type` DROP COLUMN `discount_type` , DROP COLUMN `discount_id` ;

-- -----------------------------------------------------
-- Add the field 'liveness' & 'member_card'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member` ADD COLUMN `liveness` FLOAT NULL DEFAULT 0  AFTER `member_card` , ADD COLUMN `last_consumption` DATETIME NULL DEFAULT NULL  AFTER `liveness` ;

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