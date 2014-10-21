SET @OLD_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET NAMES utf8;
SET SQL_SAFE_UPDATES = 0;
USE wireless_order_db;

-- -----------------------------------------------------
-- Rename the field 'type' to 'rule' in table 'promotion'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`promotion` 
CHANGE COLUMN `type` `rule` TINYINT(4) NOT NULL COMMENT 'the rule as below\n1 - 免费领取\n2 - 单次消费满X积分\n3 - 累计消费满X积分' ;

-- -----------------------------------------------------
-- Add the field 'type' to table 'promotion'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`promotion` 
ADD COLUMN `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type as below\n1 - normal\n2 - welcome' AFTER `oriented`;

-- -----------------------------------------------------
-- Drop the field 'status' ,'category' & 'custom_num' to table 'table'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`table` 
DROP COLUMN `status`,
DROP COLUMN `category`,
DROP COLUMN `custom_num`;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`food_price_plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_price_plan` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`food_price_plan` (
  `food_id` INT NOT NULL,
  `price_plan_id` INT NOT NULL,
  `price` FLOAT NOT NULL DEFAULT 0,
  PRIMARY KEY (`food_id`, `price_plan_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`price_plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`price_plan` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`price_plan` (
  `price_plan_id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NULL,
  `name` VARCHAR(45) NULL,
  `type` TINYINT NULL COMMENT 'the type as below.\n1 - normal\n2 - reserved',
  PRIMARY KEY (`price_plan_id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_type_price`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_type_price` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`member_type_price` (
  `member_type_id` INT NOT NULL,
  `price_plan_id` INT NOT NULL,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type to this member plan as below.\n1 - 普通\n2 - 默认',
  PRIMARY KEY (`member_type_id`, `price_plan_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Insert a '会员价' to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.price_plan
(`restaurant_id`, `name`, `type`)
SELECT id, '会员价', 1 FROM wireless_order_db.restaurant WHERE id > 10;

SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



