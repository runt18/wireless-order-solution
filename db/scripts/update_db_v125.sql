SET @OLD_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET NAMES utf8;
SET SQL_SAFE_UPDATES = 0;
USE wireless_order_db;

-- -----------------------------------------------------
-- Drop the field 'discount_type' & 'discount_id' from table 'member_type'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_type` DROP COLUMN `discount_type` , DROP COLUMN `discount_id` ;

-- -----------------------------------------------------
-- Add the field 'last_consumption' to table 'member'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member` ADD COLUMN `last_consumption` DATETIME NULL DEFAULT NULL  AFTER `member_card` ;

-- -----------------------------------------------------
-- Update the field 'last_consumption' according to the records of 'member_operation_history'
-- -----------------------------------------------------
UPDATE 
wireless_order_db.member M,
(SELECT member_id, MAX(operate_date) AS last_consumption FROM wireless_order_db.member_operation_history WHERE operate_type = 2 GROUP BY member_id) AS A
SET M.last_consumption = A.last_consumption
WHERE M.member_id = A.member_id;

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

-- -----------------------------------------------------
-- Add the field 'type' to table 'member_type'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_type` 
ADD COLUMN `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type as below\n1 - normal\n2 - reserved' AFTER `initial_point`;

-- -----------------------------------------------------
-- Insert the reserved weixin member type to each restaurant
-- -----------------------------------------------------
INSERT INTO `wireless_order_db`.`member_type`
(`restaurant_id`, `name`, `type`)
SELECT id, '微信会员', 2 FROM `wireless_order_db`.`restaurant` WHERE id > 10;

-- -----------------------------------------------------
-- Insert the discount to each weixin member type
-- -----------------------------------------------------
INSERT INTO wireless_order_db.member_type_discount
(`member_type_id`, `discount_id`, `type`)
SELECT MT.member_type_id, D.discount_id, 2 FROM wireless_order_db.member_type MT
JOIN wireless_order_db.discount D ON MT.restaurant_id = D.restaurant_id AND (D.status = 2 OR D.status = 3)
WHERE MT.type = 2;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`weixin_finance`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`weixin_finance` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`weixin_finance` (
  `weixin_serial` VARCHAR(45) NOT NULL,
  `restaurant_id` INT NOT NULL,
  `weixin_serial_crc` BIGINT NOT NULL,
  `bind_date` DATETIME NOT NULL,
  INDEX `ix_finance_crc` (`weixin_serial_crc` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`weixin_member`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`weixin_member` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`weixin_member` (
  `weixin_serial` VARCHAR(45) NOT NULL,
  `weixin_serial_crc` BIGINT NOT NULL,
  `restaurant_id` INT NOT NULL,
  `member_id` INT NOT NULL DEFAULT 0,
  `interest_date` DATETIME NULL,
  `bind_date` DATETIME NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status to weixin member as below.\n1 - 已关注\n2 - 已绑定\n',
  INDEX `ix_weixin_serial_crc` (`weixin_serial_crc` ASC),
  INDEX `ix_member_id` (`member_id` ASC),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`weixin_restaurant`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`weixin_restaurant` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`weixin_restaurant` (
  `weixin_serial` VARCHAR(45) NULL DEFAULT NULL,
  `weixin_serial_crc` BIGINT NULL DEFAULT NULL,
  `restaurant_id` INT NOT NULL,
  `bind_date` DATETIME NULL DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status as below.\n1 - 已验证\n2 - 已绑定',
  INDEX `ix_weixin_serial_crc` (`weixin_serial_crc` ASC),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`verify_sms`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`verify_sms` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`verify_sms` (
  `sms_id` INT NOT NULL AUTO_INCREMENT,
  `code` INT NOT NULL,
  `created` DATETIME NOT NULL,
  `expired` DATETIME NOT NULL,
  PRIMARY KEY (`sms_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`monthly_balance`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`monthly_balance` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`monthly_balance` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `staff` VARCHAR(45) NOT NULL,
  `month` DATE NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`monthly_balance_detail`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`monthly_balance_detail` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`monthly_balance_detail` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `monthly_balance_id` INT NOT NULL,
  `restaurant_id` INT NOT NULL,
  `dept_id` INT NOT NULL,
  `dept_name` VARCHAR(45) NOT NULL,
  `opening_balance` FLOAT NOT NULL DEFAULT 0,
  `ending_balance` FLOAT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `ix_monthly_balance_id` (`monthly_balance_id` ASC),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC),
  INDEX `ix_dept_id` (`dept_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Add the field 'delta' to table 'material'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`material` 
ADD COLUMN `delta` FLOAT NOT NULL DEFAULT 0 AFTER `price`;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`billboard`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`billboard` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`billboard` (
  `billboard_id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `title` VARCHAR(45) NOT NULL,
  `desc` VARCHAR(500) NULL DEFAULT NULL,
  `created` DATETIME NOT NULL,
  `expired` DATETIME NOT NULL,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type as below.\n1 - system\n2 - restaurant',
  PRIMARY KEY (`billboard_id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Drop the field 'current_material_month' in table 'setting'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`setting` DROP COLUMN `current_material_month`;

-- -----------------------------------------------------
-- Update the role to staff whose type is normal and role is admin to boss
-- -----------------------------------------------------
UPDATE wireless_order_db.staff S 
JOIN wireless_order_db.role R ON S.role_id = R.role_id 
SET S.role_id = (SELECT role_id FROM wireless_order_db.role WHERE restaurant_id = S.restaurant_id AND cate = 2)
WHERE S.type <> 2 AND R.cate = 1;

SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;