SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `wireless_order_db` DEFAULT CHARACTER SET utf8 ;
USE `wireless_order_db` ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`food`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food` (
  `food_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this food' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'indicates the food belong to which restaurant' ,
  `food_alias` SMALLINT UNSIGNED NOT NULL COMMENT 'the waiter use this alias id to select food in terminal' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name of the food' ,
  `pinyin` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the pinyin to this food' ,
  `kitchen_id` INT NULL DEFAULT NULL COMMENT 'the kitchen id the food belong to' ,
  `kitchen_alias` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the kitchen number which the food belong to. the maximum value (255) means the food does not belong to any kitchen.' ,
  `status` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'indicates the status to this food, the value is the combination of values below.\n特价菜 ：0x01\n推荐菜 ：0x02\n停售　 ：0x04\n赠送     ：0x08\n时价     ：0x10\n套菜     ：0x20\n热销     ：0x40\n称重     ：0x80' ,
  `taste_ref_type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the taste reference type is below.\n1 - smart reference\n2 - manual reference' ,
  `desc` VARCHAR(500) NULL DEFAULT NULL COMMENT 'the description to this food' ,
  `img` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the image to this food' ,
  PRIMARY KEY (`food_id`) ,
  INDEX `ix_food_alias_id` (`restaurant_id` ASC, `food_alias` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'This table contains the all restaurant\'s food information.' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `seq_no` INT NULL DEFAULT NULL COMMENT 'the sequence no to this order' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'external key associated with the  restaurant table' ,
  `birth_date` DATETIME NOT NULL DEFAULT 0 COMMENT 'the birth date to this order' ,
  `order_date` DATETIME NOT NULL DEFAULT 0 COMMENT 'the end date to this order' ,
  `gift_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the gift price to this order' ,
  `cancel_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the cancelled price to this order' ,
  `discount_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the discount price to this order' ,
  `repaid_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the repaid price to this order' ,
  `erase_price` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the erase price to this order' ,
  `total_price` FLOAT NULL DEFAULT NULL COMMENT 'The total price to this order.' ,
  `actual_price` FLOAT NULL DEFAULT NULL COMMENT 'the actual total price to this order' ,
  `custom_num` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the amount of custom to this order' ,
  `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the waiter who operates on this order' ,
  `settle_type` TINYINT NOT NULL DEFAULT 1 COMMENT '结帐方式：\n一般：1\n会员卡：2\n' ,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '付款方式：\n现金 : 1\n刷卡 : 2\n会员 : 3\n签单：4\n挂账 ：5\n' ,
  `category` TINYINT NOT NULL DEFAULT 1 COMMENT 'the category to this order, it should be one the values below.\n一般 : 1\n外卖 : 2\n并台 : 3\n拼台 : 4' ,
  `price_plan_id` INT NULL DEFAULT 0 COMMENT 'the price plan id this order uses' ,
  `member_id` INT NULL DEFAULT NULL COMMENT 'the member id to this order' ,
  `member_operation_id` INT NULL DEFAULT NULL COMMENT 'the member operation id' ,
  `terminal_model` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the terminal model to this order' ,
  `terminal_pin` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the terminal pin to this order' ,
  `region_id` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the region id to this order' ,
  `region_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the region name to this order' ,
  `table_id` INT NOT NULL DEFAULT 0 COMMENT 'the table id to this order' ,
  `table_alias` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the table alias id to this order' ,
  `table_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the table to this order' ,
  `discount_id` INT NOT NULL DEFAULT 0 COMMENT 'the discount id to this order' ,
  `service_rate` DECIMAL(3,2) NOT NULL DEFAULT 0 COMMENT 'the service rate to this order' ,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'the status to this order is as below.\n0 - unpaid\n1 - paid\n2 - repaid' ,
  `comment` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the comment to this order' ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_order_restaurant` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'This table describe the all restaurant\'s order information.' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_food`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_food` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_food` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this order detail record' ,
  `order_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the order id this order food belongs to' ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this order detail' ,
  `order_date` DATETIME NOT NULL DEFAULT 19000101 ,
  `order_count` FLOAT NOT NULL DEFAULT 0 COMMENT 'the count that the waiter ordered. the count can be positive or negative.' ,
  `unit_price` FLOAT UNSIGNED NOT NULL DEFAULT 0 ,
  `food_id` INT NULL DEFAULT NULL COMMENT 'the id to this food' ,
  `food_alias` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this food' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to the ordered food' ,
  `food_status` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'indicates the status to this food, the value is the combination of values below.\n特价菜 ：0x01\n推荐菜 ：0x02\n停售　 ：0x04\n赠送     ：0x08\n时价     ：0x10\n套菜     ：0x20\n热销     ：0x40\n称重     ：0x80' ,
  `hang_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates hang up status.\n0 - normal\n1 - hang_up\n2 - immediate' ,
  `taste_group_id` INT NOT NULL DEFAULT 1 COMMENT 'the taste group id to this order food, the default value(1) means empty taste group' ,
  `cancel_reason_id` INT NULL DEFAULT 0 COMMENT 'the cancel reason id to this order food' ,
  `cancel_reason` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the cancel reason description to this order food' ,
  `kitchen_id` INT NULL DEFAULT NULL COMMENT 'the kitchen id which the order food of this record belong to.' ,
  `kitchen_alias` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the kitchen alias id which the order food of this record belong to.' ,
  `dept_id` TINYINT UNSIGNED NULL DEFAULT NULL COMMENT 'the department alias id to this record' ,
  `discount` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the discount to this food' ,
  `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of waiter who deal with this record' ,
  `is_temporary` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates whether the food to this record is temporary' ,
  `is_paid` TINYINT NULL DEFAULT 0 COMMENT 'indicates whether this record is occurred before order has been paid or not' ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_taste_group_id` (`taste_group_id` ASC) ,
  INDEX `ix_cancel_reason_id` (`cancel_reason_id` ASC) ,
  INDEX `ix_order_id` (`order_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'descirbe the relationship between the order and food' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`restaurant`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`restaurant` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`restaurant` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to this restaurant, id 1 indicates the root user, id 2 indicates idle-repository, id 3 indicates discarded-repository' ,
  `pwd` VARCHAR(45) NOT NULL COMMENT 'the password for the restaurant to log in' ,
  `account` VARCHAR(45) NOT NULL COMMENT 'the account for the restaurant to log in' ,
  `restaurant_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT 'the restaurant name ' ,
  `restaurant_info` VARCHAR(300) NOT NULL DEFAULT '' COMMENT 'the restaurant info' ,
  `pwd2` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the 2nd password to this restaurant, whose permission priority is lower than pwd' ,
  `pwd3` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the 3rd password to this restaurant, whose permission priority is lower than pwd2' ,
  `tele1` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'One of the telephones to this restaurant.' ,
  `tele2` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'One of the telephones to this restaurant.' ,
  `address` VARCHAR(70) NOT NULL DEFAULT '' COMMENT 'The address to this restaurant.' ,
  `record_alive` BIGINT NOT NULL DEFAULT 0 COMMENT 'Indicates how long the order record of this restaurant can be persisted. It\'s represented in second. Value 0 means the records never expire.' ,
  PRIMARY KEY (`id`, `account`) ,
  UNIQUE INDEX `account_UNIQUE` (`account` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the restaurnat\'s information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`terminal`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`terminal` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`terminal` (
  `terminal_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this terminal' ,
  `pin` INT UNSIGNED NOT NULL COMMENT 'the pin to identify the phone' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'indicates the terminal belong to which restaurant' ,
  `model_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the model to this terminal.\nBlackBerry : 0x00\nAndroid : 0x01\nAdmin : 0xFE\nStaff : 0xFF' ,
  `model_name` VARCHAR(45) NULL COMMENT 'the model name to the phone' ,
  `owner_name` VARCHAR(45) NOT NULL COMMENT 'the owner name of this terminal' ,
  `expire_date` DATE NULL DEFAULT NULL COMMENT 'the expired date to the terminal,\nNULL means never expired,' ,
  `gift_amount` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the gift amount to this terminal' ,
  `gift_quota` DECIMAL(7,2) NULL DEFAULT -1 COMMENT 'the gift quota to this terminal, \"-1\" means no limit' ,
  `entry_date` DATETIME NULL COMMENT 'the date to add the terminal' ,
  `idle_date` DATETIME NULL COMMENT 'the date to make the phone idle' ,
  `work_date` DATETIME NULL COMMENT 'the date to make the phone in use' ,
  `discard_date` DATETIME NULL COMMENT 'the date make the phone discarded' ,
  `idle_duration` BIGINT NOT NULL DEFAULT 0 COMMENT 'the phone\'s idle duration (expressed by second)' ,
  `work_duration` BIGINT NOT NULL DEFAULT 0 COMMENT 'the phone\'s work duration(expressed by second)' ,
  PRIMARY KEY (`terminal_id`) ,
  INDEX `fk_terminal_restaurant1` (`restaurant_id` ASC) ,
  INDEX `ix_terminal_pm` (`pin` ASC, `model_id` ASC) ,
  CONSTRAINT `fk_terminal_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the terminal info' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`table`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`table` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`table` (
  `table_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this table' ,
  `table_alias` SMALLINT UNSIGNED NULL ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'Indicates the table belongs to which restaurant.' ,
  `region_id` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the region alias id to this table. 255 means the table does NOT belong to any region.' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to this table' ,
  `minimum_cost` FLOAT NOT NULL DEFAULT 0 COMMENT 'the minimum cost to this table' ,
  `custom_num` TINYINT UNSIGNED NULL DEFAULT NULL COMMENT 'the amount of customer to this table if the status is not idle' ,
  `category` TINYINT NULL DEFAULT NULL COMMENT 'the category to this table, it should be one the values below.\n一般 : 1\n外卖 : 2\n并台 : 3\n拼台 : 4' ,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'the status to this table, one of the values below.\n空闲 : 0\n就餐 : 1\n预定 : 2' ,
  `service_rate` DECIMAL(3,2) NOT NULL DEFAULT 0 COMMENT 'the service rate to this table' ,
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT 'indicates whether the table information is enabled or not' ,
  PRIMARY KEY (`table_id`) ,
  INDEX `ix_table_alias_id` (`restaurant_id` ASC, `table_alias` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the restaurant\'s table info' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`taste`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`taste` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`taste` (
  `taste_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to taste table' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'indicates the taste preference belong to which restaurant' ,
  `taste_alias` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this taste preference, the lower the alias id , the more commonly this taste preference used' ,
  `preference` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the description to this taste preference' ,
  `price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to this taste preference, used for the calc type is 按价格' ,
  `category` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the category to this taste, one of the values below.\n0 - 口味\n1 - 做法\n2 - 规格' ,
  `rate` DECIMAL(3,2) NOT NULL DEFAULT 0 COMMENT 'the rate to this taste, used for the calc type is 按比例' ,
  `calc` TINYINT NOT NULL DEFAULT 0 COMMENT 'the calculate type to this taste, one of the values below.\n0 - 按价格\n1 - 按比例' ,
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the type to this taste as below.\n0 - normal\n1 - reserved' ,
  PRIMARY KEY (`taste_id`) ,
  INDEX `ix_taste_alias_id` (`restaurant_id` ASC, `taste_alias` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the taste info' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`kitchen`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`kitchen` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`kitchen` (
  `kitchen_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this kitchen' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `kitchen_alias` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this kitchen' ,
  `dept_id` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the department alias id that this kitchen belong to. ' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of this kitchen' ,
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the type to this taste as below.\n0 - normal\n1 - reserved' ,
  `is_allow_temp` TINYINT NOT NULL DEFAULT 0 COMMENT 'the flag to indicate whether allow temporary food' ,
  PRIMARY KEY (`kitchen_id`) ,
  INDEX `ix_kitchen_alias_id` (`restaurant_id` ASC, `kitchen_alias` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the kitchen information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_type`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_type` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_type` (
  `member_type_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to member type' ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this member type' ,
  `discount_id` INT UNSIGNED NOT NULL COMMENT 'the discount id this member type uses' ,
  `discount_type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the discount type is as below.\n0 - discount plan\n1 - entire ' ,
  `exchange_rate` FLOAT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the exchange rate used to transfer the price to point' ,
  `charge_rate` FLOAT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the charge rate used to transfer money to balance' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name to this member type' ,
  `attribute` TINYINT NOT NULL DEFAULT 0 COMMENT 'the attribute to this member tye as below.\n0 - 充值属性\n1 - 积分属性\n2 - 优惠属性\n\n' ,
  PRIMARY KEY (`member_type_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member type information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_history` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `seq_no` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the sequence no to this order' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'external key associated with the  restaurant table' ,
  `birth_date` DATETIME NOT NULL DEFAULT 0 COMMENT 'the birth date to this order' ,
  `order_date` DATETIME NOT NULL DEFAULT 0 COMMENT 'the end date to this order' ,
  `gift_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the gift price to this order' ,
  `cancel_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the cancel price to this order' ,
  `discount_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the discount price to this order' ,
  `repaid_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the repaid price to this order' ,
  `total_price` FLOAT NULL DEFAULT NULL COMMENT 'The total price to this order.' ,
  `actual_price` FLOAT NULL DEFAULT NULL COMMENT 'the actual total price to this order' ,
  `erase_price` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the erase price to this order' ,
  `custom_num` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the amount of custom to this order' ,
  `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the waiter who operates on this order' ,
  `settle_type` TINYINT NOT NULL DEFAULT 1 COMMENT '付款方式\n一般：1\n会员：2' ,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '付款方式\n现金 : 1\n刷卡 : 2\n会员卡 : 3\n签单：4\n挂账 ：5\n' ,
  `category` TINYINT NOT NULL DEFAULT 1 COMMENT 'the category to this order, it should be one the values below.\n一般 : 1\n外卖 : 2\n并台 : 3\n拼台 : 4' ,
  `member_id` INT NULL DEFAULT NULL COMMENT 'the member id to this order' ,
  `member_operation_id` INT NULL DEFAULT NULL COMMENT 'the member operation id' ,
  `terminal_model` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the terminal model to this order' ,
  `terminal_pin` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the terminal pin to this order' ,
  `region_id` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the region id to this order' ,
  `region_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the region name to this order' ,
  `table_id` INT NOT NULL DEFAULT 0 COMMENT 'the table id to this order' ,
  `table_alias` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the table alias id to this order' ,
  `table_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the table name to this order' ,
  `service_rate` DECIMAL(3,2) NOT NULL DEFAULT 0 COMMENT 'the service rate to this order' ,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'the status to this order is as below.\n0 - unpaid\n1 - paid\n2 - repaid' ,
  `comment` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the comment to this order' ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_order_history_restaurant` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'This table preserves all the order records.' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`material`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`material` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`material` (
  `material_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to this material' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'the id to related restaurant' ,
  `cate_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the catagory id to this material' ,
  `material_alias` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this material' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to this material' ,
  `warning_threshold` FLOAT NOT NULL DEFAULT 0 COMMENT 'the warning threshold to this material' ,
  `danger_threshold` FLOAT NOT NULL DEFAULT 0 COMMENT 'the danger threshold to this material' ,
  INDEX `fk_material_restaurant` (`restaurant_id` ASC) ,
  PRIMARY KEY (`material_id`) ,
  CONSTRAINT `fk_material_restaurant`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the material information.' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`food_material`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_material` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food_material` (
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `material_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the material id' ,
  `food_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the food id' ,
  `consumption` FLOAT NOT NULL DEFAULT 0 COMMENT 'the consumption between the food and the material' ,
  INDEX `fk_food_material_restaurant` (`restaurant_id` ASC) ,
  PRIMARY KEY (`restaurant_id`, `material_id`, `food_id`) ,
  CONSTRAINT `fk_food_material_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the releation ship between food and material' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_food_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_food_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_food_history` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this order detail record' ,
  `order_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the order id this order food belongs to' ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this order history detail' ,
  `order_count` FLOAT NOT NULL DEFAULT 0 COMMENT 'the count that the waiter ordered. the count can be positive or negative.' ,
  `order_date` DATETIME NOT NULL DEFAULT 19000101 ,
  `unit_price` FLOAT UNSIGNED NOT NULL DEFAULT 0 ,
  `food_id` INT NULL DEFAULT NULL COMMENT 'the id to this food' ,
  `food_alias` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this food' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to the ordered food' ,
  `food_status` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'indicates the status to this food, the value is the combination of values below.\n特价菜 ：0x01\n推荐菜 ：0x02\n停售　 ：0x04\n赠送     ：0x08\n时价     ：0x10\n套菜     ：0x20\n热销     ：0x40\n称重     ：0x80' ,
  `taste_group_id` INT NOT NULL DEFAULT 1 COMMENT 'the taste group id to this order food, the default value(1) is empty taste group' ,
  `cancel_reason_id` INT NULL DEFAULT 0 COMMENT 'the cancel reason id to this order food' ,
  `cancel_reason` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the cancel reason description to this order food' ,
  `dept_id` TINYINT UNSIGNED NULL DEFAULT NULL COMMENT 'the department alias id to this record' ,
  `kitchen_id` INT NULL DEFAULT NULL COMMENT 'the kitchen id which the order food of this record belong to.' ,
  `kitchen_alias` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the kitchen number which the order food of this record belong to. the maximum value (255) means the food does not belong to any kitchen.' ,
  `discount` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the discount to this food' ,
  `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of waiter who deal with this record' ,
  `is_temporary` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates whether the food to this record is temporary' ,
  `is_paid` TINYINT NULL DEFAULT 0 COMMENT 'indicates whether this record is occurred before order has been paid or not' ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_taste_group_id` (`taste_group_id` ASC) ,
  INDEX `ix_cancel_reason_id` (`cancel_reason_id` ASC) ,
  INDEX `ix_order_id` (`order_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'descirbe the relationship between the order and food' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`staff`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`staff` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`staff` (
  `staff_id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `terminal_id` INT NOT NULL ,
  `staff_alias` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the alias id to this stuff' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to this staff' ,
  `pwd` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the password to this staff whose format is MD5' ,
  PRIMARY KEY (`staff_id`) ,
  INDEX `fk_staff_restaurant` (`restaurant_id` ASC) ,
  INDEX `fk_staff_terminal` (`terminal_id` ASC) ,
  CONSTRAINT `fk_staff_restaurant`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_staff_terminal`
    FOREIGN KEY (`terminal_id` )
    REFERENCES `wireless_order_db`.`terminal` (`terminal_id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'the staff information ' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`setting`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`setting` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`setting` (
  `setting_id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this setting' ,
  `price_tail` TINYINT UNSIGNED NOT NULL DEFAULT 2 COMMENT 'indicates how to deal with the tail of price:\n不处理 : 0\n小数抹零 : 1\n小数四舍五入 : 2' ,
  `auto_reprint` TINYINT NOT NULL DEFAULT 1 COMMENT 'indicates whether to auto re-print' ,
  `receipt_style` INT UNSIGNED NOT NULL DEFAULT 4294967295 COMMENT 'the receipt style is as below.\n0x01 : 结帐单是否显示折扣\n0x02 : 结帐单是否显示数量\n0x04 : 结帐单是否显示状态\n0x08 : 结帐单是否显示折扣额' ,
  `erase_quota` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the erase quota, 0 means no limit' ,
  PRIMARY KEY (`setting_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'the setting to restaurant' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`shift`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`shift` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`shift` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to each shift record' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of the operator to shift' ,
  `on_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be on duty' ,
  `off_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be off duty' ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'the shift history to each restaurant' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`region`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`region` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`region` (
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this region' ,
  `region_id` TINYINT UNSIGNED NOT NULL COMMENT 'the alias id to this table region' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to this table region' ,
  PRIMARY KEY (`restaurant_id`, `region_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the region information to the tables' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`department`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`department` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`department` (
  `dept_id` TINYINT UNSIGNED NOT NULL DEFAULT 0 ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' ,
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the type to this taste as below.\n0 - normal\n1 - reserved' ,
  PRIMARY KEY (`restaurant_id`, `dept_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the department information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`supplier`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`supplier` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`supplier` (
  `supplier_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to this supplier' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `supplier_alias` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this supplier' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to ths supplier' ,
  `tele` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the telephone to this supplier' ,
  `addr` VARCHAR(100) NOT NULL DEFAULT '' COMMENT 'the address to this supplier' ,
  `contact` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the contact person to this supplier' ,
  INDEX `fk_supplier_restaurant` (`restaurant_id` ASC) ,
  PRIMARY KEY (`supplier_id`) ,
  CONSTRAINT `fk_supplier_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`material_detail`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`material_detail` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`material_detail` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id that this material detial belongs to' ,
  `supplier_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the supplier id that this material detail record belong to' ,
  `material_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the material id that this material detail record belong to' ,
  `food_id` INT NOT NULL DEFAULT 0 COMMENT 'the food id that this material detail record belong to' ,
  `price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to this material record' ,
  `price_prev` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the previous price to this material detail record' ,
  `date` DATETIME NULL DEFAULT NULL COMMENT 'the date to this material detail record' ,
  `staff` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the staff name to this material detail record' ,
  `dept_id` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the super kitchen id to this material detail record' ,
  `dept2_id` TINYINT UNSIGNED NULL DEFAULT NULL COMMENT 'indicates the 调入部门 in case of the type is “调出”' ,
  `amount` FLOAT NOT NULL DEFAULT 0 COMMENT 'the amount to this material detail record' ,
  `amount_prev` FLOAT NOT NULL DEFAULT 0 COMMENT 'the previous amount to this material detail record' ,
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the type is as below.\n0 : 消耗\n1 : 报损 \n2 : 销售\n3 : 退货\n4 : 出仓\n5 : 入库\n6 : 调出\n7 : 调入\n8 : 盘点' ,
  `comment` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the comment to this material detail' ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the material detail to history' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`material_dept`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`material_dept` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`material_dept` (
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `material_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the id to material ' ,
  `dept_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the id to department' ,
  `dept_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to department' ,
  `material_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to material' ,
  `price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to this material' ,
  `stock` FLOAT NOT NULL DEFAULT 0 COMMENT 'the stock to this material' ,
  INDEX `fk_material_dept_restaurant1` (`restaurant_id` ASC) ,
  PRIMARY KEY (`restaurant_id`, `material_id`, `dept_id`) ,
  CONSTRAINT `fk_material_dept_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the stock to each material of department' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`material_cate`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`material_cate` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`material_cate` (
  `cate_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0 ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' ,
  PRIMARY KEY (`cate_id`, `restaurant_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the category of material' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`shift_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`shift_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`shift_history` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to each shift record' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of the operator to shift' ,
  `on_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be on duty' ,
  `off_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be off duty' ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'the shift history to each restaurant' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`daily_settle_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`daily_settle_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`daily_settle_history` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to each shift record' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of the operator to perform daily settle' ,
  `on_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be on duty' ,
  `off_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be off duty' ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'the daily settle history to each restaurant' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`food_taste`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_taste` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food_taste` (
  `food_id` INT NULL DEFAULT NULL COMMENT 'the food id' ,
  `taste_id` INT NULL DEFAULT NULL COMMENT 'the taste id' ,
  `restaurant_id` INT NULL DEFAULT NULL COMMENT 'the restaurant id' ,
  `ref_cnt` INT UNSIGNED NULL DEFAULT 0 COMMENT 'the reference count of taste to this food' ,
  INDEX `ix_food_id` (`food_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the rank of taste to each food' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`kitchen_taste`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`kitchen_taste` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`kitchen_taste` (
  `kitchen_id` INT NULL DEFAULT NULL COMMENT 'the kitchen id' ,
  `taste_id` INT NULL DEFAULT NULL COMMENT 'the taste id' ,
  `restaurant_id` INT NULL DEFAULT NULL COMMENT 'the restaurant id' ,
  `ref_cnt` INT UNSIGNED NULL DEFAULT 0 COMMENT 'the reference count of taste to this kitchen' ,
  INDEX `ix_kitchen_id` (`kitchen_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the taste reference information to each kitchen' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`dept_taste`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`dept_taste` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`dept_taste` (
  `dept_id` INT NULL DEFAULT NULL COMMENT 'the department id' ,
  `taste_id` INT NULL DEFAULT NULL COMMENT 'the taste id' ,
  `restaurant_id` INT NULL DEFAULT NULL COMMENT 'the restaurant id' ,
  `ref_cnt` INT UNSIGNED NULL DEFAULT 0 COMMENT 'the reference count of taste to this department' ,
  INDEX `ix_dept_id` (`dept_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the taste reference information to each department' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`food_taste_rank`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_taste_rank` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food_taste_rank` (
  `food_id` INT NULL DEFAULT NULL COMMENT 'the food id' ,
  `taste_id` INT NULL DEFAULT NULL COMMENT 'the taste id' ,
  `restaurant_id` INT NULL DEFAULT NULL COMMENT 'the restaurant id' ,
  `rank` SMALLINT UNSIGNED NULL DEFAULT 0 COMMENT 'the rank of taste reference to this food. ' ,
  INDEX `ix_food_id` (`food_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the rank of taste reference to each food' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`combo`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`combo` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`combo` (
  `food_id` INT NULL DEFAULT NULL COMMENT 'the main food id to this combo' ,
  `sub_food_id` INT NULL DEFAULT NULL COMMENT 'the sub food id to this combo' ,
  `restaurant_id` INT NULL DEFAULT NULL COMMENT 'the restaurant id' ,
  `amount` SMALLINT UNSIGNED NULL DEFAULT 1 COMMENT 'the amount of sub food to this combo' ,
  INDEX `ix_food_id` (`food_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the combo information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`discount`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`discount` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`discount` (
  `discount_id` INT UNSIGNED NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NULL COMMENT 'the restaurant id this discount belongs to' ,
  `name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the name to this discount' ,
  `level` SMALLINT NULL DEFAULT 0 ,
  `status` TINYINT NULL DEFAULT 0 COMMENT 'the status is as below.\n0 - normal\n1 - default\n2 - reserved\n3 - default_reserved\n4 - member' ,
  PRIMARY KEY (`discount_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the discount plan' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`discount_plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`discount_plan` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`discount_plan` (
  `dist_plan_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this discount plan' ,
  `discount_id` INT UNSIGNED NOT NULL DEFAULT 0 ,
  `kitchen_id` INT NOT NULL DEFAULT 0 ,
  `rate` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the discount rate which ranges from 0.00 to 1.00' ,
  PRIMARY KEY (`dist_plan_id`) ,
  INDEX `ix_discount_id` (`discount_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the plan to each discount' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`client`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`client` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`client` (
  `client_id` INT NOT NULL AUTO_INCREMENT COMMENT 'thie id to this client' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'the restaurant id to this client' ,
  `client_type_id` INT NULL DEFAULT NULL COMMENT 'the type this client belongs to' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name to this client' ,
  `sex` TINYINT NOT NULL DEFAULT 0 COMMENT 'the sex to this client' ,
  `birth_date` DATETIME NULL DEFAULT NULL COMMENT 'the birth date to client' ,
  `tele` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the telephone to this client' ,
  `mobile` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the mobile to this client' ,
  `birthday` DATE NULL DEFAULT NULL COMMENT 'the birthday to this client' ,
  `id_card` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the id card to this client' ,
  `company` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the company to this client' ,
  `taste_pref` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the taste preference to client' ,
  `taboo` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the taboo to client' ,
  `contact_addr` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the contact address to client' ,
  `comment` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the comment to client' ,
  `level` TINYINT NULL DEFAULT 0 COMMENT 'the status to client.\n0 - normal\n1 - anonymous' ,
  `last_staff_id` INT NULL DEFAULT NULL COMMENT 'the id to last modifed staff' ,
  PRIMARY KEY (`client_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) ,
  INDEX `ix_client_type_id` (`client_type_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the client' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`client_type`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`client_type` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`client_type` (
  `client_type_id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT NOT NULL ,
  `parent_id` INT NULL COMMENT 'the parent id to this clent type' ,
  `name` VARCHAR(45) NULL ,
  PRIMARY KEY (`client_type_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the clent type' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_card`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_card` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_card` (
  `member_card_id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `member_card_alias` VARCHAR(45) NULL DEFAULT NULL ,
  `status` TINYINT NULL DEFAULT 0 COMMENT 'the status is as below.\n0 - idle\n1 - lost\n2 - disable\n3 - in used' ,
  `comment` VARCHAR(500) NULL DEFAULT NULL COMMENT 'the comment to this member card' ,
  `last_staff_id` INT NULL DEFAULT NULL COMMENT 'the id to last modified staff' ,
  `last_mod_date` DATETIME NULL DEFAULT NULL COMMENT 'the last modified date' ,
  PRIMARY KEY (`member_card_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member card' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member` (
  `member_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this member' ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this member' ,
  `member_type_id` INT NOT NULL COMMENT 'the type this member belongs to' ,
  `member_card_id` INT NULL DEFAULT NULL COMMENT 'the card this member owns' ,
  `base_balance` FLOAT NOT NULL DEFAULT 0 COMMENT 'the base balance to this member' ,
  `extra_balance` FLOAT NOT NULL DEFAULT 0 COMMENT 'the extra balance to this member' ,
  `point` INT NOT NULL DEFAULT 0 COMMENT 'the remaining point to this member' ,
  `birth_date` DATETIME NULL DEFAULT NULL ,
  `comment` VARCHAR(500) NULL DEFAULT NULL ,
  `status` TINYINT NULL DEFAULT 0 COMMENT 'the status to this member\n0 - normal\n1 - disabled' ,
  `last_mod_date` DATETIME NULL DEFAULT NULL COMMENT 'the last modified date' ,
  `last_staff_id` INT NULL DEFAULT NULL COMMENT 'the id to last modified staff' ,
  PRIMARY KEY (`member_id`) ,
  INDEX `ix_member_type_id` (`member_type_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) ,
  INDEX `ix_member_card_id` (`member_card_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`food_statistics`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_statistics` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food_statistics` (
  `food_id` INT NOT NULL ,
  `order_cnt` INT UNSIGNED NOT NULL DEFAULT 0 ,
  `weight` FLOAT NOT NULL DEFAULT 0 COMMENT 'the weight to this food' ,
  PRIMARY KEY (`food_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the food statistics' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`client_member`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`client_member` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`client_member` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `client_id` INT NOT NULL COMMENT 'the id to client' ,
  `member_id` INT NOT NULL COMMENT 'the id to member' ,
  `restaurant_id` INT NOT NULL COMMENT 'the id to restaurant' ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_client_id` (`client_id` ASC) ,
  INDEX `ix_memeber_id` (`member_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the relationship between member and client' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`taste_group`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`taste_group` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`taste_group` (
  `taste_group_id` INT NOT NULL AUTO_INCREMENT ,
  `normal_taste_group_id` INT NOT NULL DEFAULT 1 ,
  `normal_taste_pref` VARCHAR(45) NULL DEFAULT NULL ,
  `normal_taste_price` DECIMAL(7,2) UNSIGNED NULL DEFAULT NULL ,
  `tmp_taste_id` INT NULL DEFAULT NULL ,
  `tmp_taste_pref` VARCHAR(45) NULL DEFAULT NULL ,
  `tmp_taste_price` DECIMAL(7,2) UNSIGNED NULL DEFAULT NULL ,
  PRIMARY KEY (`taste_group_id`) ,
  INDEX `ix_normal_taste_group_id` (`normal_taste_group_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the taste group' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`normal_taste_group`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`normal_taste_group` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`normal_taste_group` (
  `normal_taste_group_id` INT NOT NULL ,
  `taste_id` INT NOT NULL ,
  PRIMARY KEY (`normal_taste_group_id`, `taste_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the relationship between taste group and its normal' /* comment truncated */ ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`taste_group_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`taste_group_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`taste_group_history` (
  `taste_group_id` INT NOT NULL AUTO_INCREMENT ,
  `normal_taste_group_id` INT NOT NULL DEFAULT 1 ,
  `normal_taste_pref` VARCHAR(45) NULL DEFAULT NULL ,
  `normal_taste_price` DECIMAL(7,2) UNSIGNED NULL DEFAULT NULL ,
  `tmp_taste_id` INT NULL DEFAULT NULL ,
  `tmp_taste_pref` VARCHAR(45) NULL DEFAULT NULL ,
  `tmp_taste_price` DECIMAL(7,2) UNSIGNED NULL DEFAULT NULL ,
  PRIMARY KEY (`taste_group_id`) ,
  INDEX `ix_normal_taste_group_id` (`normal_taste_group_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the taste group' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`normal_taste_group_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`normal_taste_group_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`normal_taste_group_history` (
  `normal_taste_group_id` INT NOT NULL ,
  `taste_id` INT NOT NULL ,
  PRIMARY KEY (`normal_taste_group_id`, `taste_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the relationship between taste group and its normal' /* comment truncated */ ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`food_association`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_association` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food_association` (
  `food_id` INT NULL ,
  `associated_food_id` INT NULL ,
  `associated_amount` INT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`food_id`, `associated_food_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the association between the foods' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`cancel_reason`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`cancel_reason` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`cancel_reason` (
  `cancel_reason_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this cancel reason' ,
  `reason` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the description to this cancel reason' ,
  `restaurant_id` INT UNSIGNED NULL DEFAULT 0 COMMENT 'the restaurant id this cancel reason belongs to' ,
  PRIMARY KEY (`cancel_reason_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the cancel reason' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`price_plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`price_plan` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`price_plan` (
  `price_plan_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this food price plan' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'the restaurant id this food price plan belongs to' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name to this food price plan' ,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'the status to price plan is as below.\n0 - normal\n1 - in use' ,
  PRIMARY KEY (`price_plan_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the general information to food price plan' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`food_price_plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_price_plan` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food_price_plan` (
  `price_plan_id` INT NOT NULL ,
  `food_id` INT NOT NULL ,
  `unit_price` FLOAT NOT NULL DEFAULT 0 ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this food price plan' ,
  PRIMARY KEY (`price_plan_id`, `food_id`) ,
  INDEX `ix_food_id` (`food_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the food unit price to a specific plan' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_group`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_group` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_group` (
  `order_id` INT UNSIGNED NULL ,
  `sub_order_id` INT UNSIGNED NULL ,
  `restaurant_id` INT UNSIGNED NULL ,
  PRIMARY KEY (`order_id`, `sub_order_id`) ,
  INDEX `ix_sub_order_id` (`sub_order_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the general order group information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`sub_order`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`sub_order` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`sub_order` (
  `order_id` INT NOT NULL COMMENT 'the order id to this sub order' ,
  `table_id` INT NOT NULL COMMENT 'the table id to this sub order' ,
  `table_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the table name to this sub order' ,
  `cancel_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the cancel price to this sub order' ,
  `gift_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the gift price to this sub order' ,
  `discount_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the discount price to this sub order' ,
  `erase_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the erase price to this sub order' ,
  `total_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the total price to this sub order' ,
  `actual_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the actual price to this sub order' ,
  PRIMARY KEY (`order_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the information to sub order' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_group_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_group_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_group_history` (
  `order_id` INT UNSIGNED NULL ,
  `sub_order_id` INT UNSIGNED NULL ,
  `restaurant_id` INT UNSIGNED NULL ,
  PRIMARY KEY (`order_id`, `sub_order_id`) ,
  INDEX `ix_sub_order_id` (`sub_order_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the general order group history information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`sub_order_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`sub_order_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`sub_order_history` (
  `order_id` INT NOT NULL COMMENT 'the order id to this sub order' ,
  `table_id` INT NOT NULL COMMENT 'the table id to this sub order' ,
  `table_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the table name to this sub order' ,
  `cancel_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the cancel price to this sub order' ,
  `gift_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the gift price to this sub order' ,
  `discount_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the discount price to this sub order' ,
  `erase_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the erase price to this sub order' ,
  `total_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the total price to this sub order' ,
  `actual_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the actual price to this sub order' ,
  PRIMARY KEY (`order_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the information to sub order history' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_operation_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_operation_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_operation_history` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `staff_id` INT NOT NULL COMMENT 'the staff id ' ,
  `staff_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the staff name' ,
  `member_id` INT NOT NULL COMMENT 'the member id' ,
  `member_card_id` INT NOT NULL COMMENT 'the member card id' ,
  `member_card_alias` VARCHAR(45) NOT NULL ,
  `operate_seq` VARCHAR(45) NOT NULL COMMENT 'the format to operate seq is defined below.\n挂失YYYYMMDDHHIISS: GS20130101230000' ,
  `operate_date` DATETIME NOT NULL ,
  `operate_type` TINYINT NOT NULL COMMENT 'the operation type:\n1 - 充值\n2 - 消费\n3 - 冻结\n4 - 解冻\n5 - 换卡\n6 - 反结帐退款\n7 - 反结帐消费' ,
  `pay_type` TINYINT NULL DEFAULT NULL COMMENT '付款方式：\n现金 : 1\n刷卡 : 2\n会员 : 3\n签单：4\n挂账 ：5' ,
  `pay_money` FLOAT NULL DEFAULT NULL COMMENT 'the memory to pay' ,
  `order_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'the order id this member operation, only available in case of either consume or repaid' ,
  `charge_type` TINYINT NULL DEFAULT NULL COMMENT '充值类型：\n1 - 现金\n2 - 刷卡' ,
  `charge_money` FLOAT NULL DEFAULT NULL COMMENT 'the memory to charge' ,
  `delta_base_money` FLOAT NOT NULL DEFAULT 0 ,
  `delta_extra_money` FLOAT NOT NULL DEFAULT 0 ,
  `delta_point` INT NOT NULL DEFAULT 0 ,
  `remaining_base_money` FLOAT NOT NULL DEFAULT 0 ,
  `remaining_extra_money` FLOAT NOT NULL DEFAULT 0 ,
  `remaining_point` INT NOT NULL DEFAULT 0 ,
  `comment` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_staff_id` (`staff_id` ASC) ,
  INDEX `ix_member_id` (`member_id` ASC) ,
  INDEX `ix_member_card_id` (`member_card_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member operation to history' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_operation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_operation` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_operation` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `staff_id` INT NOT NULL COMMENT 'the staff id ' ,
  `staff_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the staff name' ,
  `member_id` INT NOT NULL COMMENT 'the member id' ,
  `member_card_id` INT NOT NULL COMMENT 'the member card id' ,
  `member_card_alias` VARCHAR(45) NOT NULL ,
  `operate_seq` VARCHAR(45) NOT NULL COMMENT 'the format to operate seq is defined below.\n挂失YYYYMMDDHHIISS: GS20130101230000' ,
  `operate_date` DATETIME NOT NULL ,
  `operate_type` TINYINT NOT NULL COMMENT 'the operation type:\n1 - 充值\n2 - 消费\n3 - 冻结\n4 - 解冻\n5 - 换卡\n6 - 反结帐退款\n7 - 反结帐消费' ,
  `pay_type` TINYINT NULL DEFAULT NULL COMMENT '付款方式：\n现金 : 1\n刷卡 : 2\n会员 : 3\n签单：4\n挂账 ：5' ,
  `pay_money` FLOAT NULL DEFAULT NULL COMMENT 'the memory to pay' ,
  `order_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'the order id this member operation, only available in case of either consume or repaid' ,
  `charge_type` TINYINT NULL DEFAULT NULL COMMENT '充值类型：\n1 - 现金\n2 - 刷卡' ,
  `charge_money` FLOAT NULL DEFAULT NULL COMMENT 'the memory to charge' ,
  `delta_base_money` FLOAT NOT NULL DEFAULT 0 ,
  `delta_extra_money` FLOAT NOT NULL DEFAULT 0 ,
  `delta_point` INT NOT NULL DEFAULT 0 ,
  `remaining_base_money` FLOAT NOT NULL DEFAULT 0 ,
  `remaining_extra_money` FLOAT NOT NULL DEFAULT 0 ,
  `remaining_point` INT NOT NULL DEFAULT 0 ,
  `comment` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_staff_id` (`staff_id` ASC) ,
  INDEX `ix_member_id` (`member_id` ASC) ,
  INDEX `ix_member_card_id` (`member_card_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member operation to today' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`user` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`user` (
  `user_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this user' ,
  `tele` VARCHAR(45) NOT NULL COMMENT 'the telephone to this user' ,
  PRIMARY KEY (`user_id`) ,
  UNIQUE INDEX `tele_UNIQUE` (`tele` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the user information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_user` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_user` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `user_id` INT NOT NULL ,
  `member_id` VARCHAR(45) NOT NULL ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_user_id` (`user_id` ASC) ,
  INDEX `ix_member_id` (`member_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the relationship between the member and user' ;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;



-- -----------------------------------------------------
-- Data for table `wireless_order_db`.`restaurant`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `wireless_order_db`;
SET NAMES utf8;
INSERT INTO `wireless_order_db`.`restaurant` (`id`, `pwd`, `account`, `restaurant_name`, `restaurant_info`,`record_alive`) VALUES ('1', MD5('root@123'), 'root', 'root', '欢迎使用e点通手持终端。\n智易科技，智慧，简易，引领新生活。', '0');
INSERT INTO `wireless_order_db`.`restaurant` (`id`, `pwd`, `account`, `restaurant_name`, `record_alive`) VALUES ('2', MD5('idle@123'), 'idle', 'idle', '0');
INSERT INTO `wireless_order_db`.`restaurant` (`id`, `pwd`, `account`, `restaurant_name`, `record_alive`) VALUES ('3', MD5('discard@123'), 'discarded', 'discarded', '0');
INSERT INTO `wireless_order_db`.`restaurant` (`id`, `pwd`, `account`, `restaurant_name`, `record_alive`) VALUES ('4', MD5('reserved@123'), 'reserved1', 'reserved1', '0');
INSERT INTO `wireless_order_db`.`restaurant` (`id`, `pwd`, `account`, `restaurant_name`, `record_alive`) VALUES ('5', MD5('reserved@123'), 'reserved2', 'reserved2', '0');
INSERT INTO `wireless_order_db`.`restaurant` (`id`, `pwd`, `account`, `restaurant_name`, `record_alive`) VALUES ('6', MD5('reserved@123'), 'reserved3', 'reserved3', '0');
INSERT INTO `wireless_order_db`.`restaurant` (`id`, `pwd`, `account`, `restaurant_name`, `record_alive`) VALUES ('7', MD5('reserved@123'), 'reserved4', 'reserved4', '0');
INSERT INTO `wireless_order_db`.`restaurant` (`id`, `pwd`, `account`, `restaurant_name`, `record_alive`) VALUES ('8', MD5('reserved@123'), 'reserved5', 'reserved5', '0');
INSERT INTO `wireless_order_db`.`restaurant` (`id`, `pwd`, `account`, `restaurant_name`, `record_alive`) VALUES ('9', MD5('reserved@123'), 'reserved6', 'reserved6', '0');
INSERT INTO `wireless_order_db`.`restaurant` (`id`, `pwd`, `account`, `restaurant_name`, `record_alive`) VALUES ('10', MD5('reserved@123'), 'reserved7', 'reserved7', '0');
COMMIT;
SET AUTOCOMMIT=1;

-- -----------------------------------------------------
-- View`restaurant_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS restaurant_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `restaurant_view` AS select `r`.`id` AS `id`,`r`.`account` AS `account`,`r`.`restaurant_name` AS `restaurant_name`,`r`.`tele1` AS `tele1`,`r`.`tele2` AS `tele2`,`r`.`address` AS `address`,`r`.`restaurant_info` AS `restaurant_info`,`r`.`record_alive` AS `record_alive`,(select count(`order`.`id`) from `order` where (`order`.`restaurant_id` = `r`.`id`)) AS `order_num`,(select count(`order_history`.`id`) from `order_history` where (`order_history`.`restaurant_id` = `r`.`id`)) AS `order_history_num`,(select count(`terminal`.`pin`) from `terminal` where ((`terminal`.`restaurant_id` = `r`.`id`) and (`terminal`.`model_id` <= 0x7f))) AS `terminal_num`,(select count(`terminal`.`pin`) from `terminal` where ((`terminal`.`restaurant_id` = `r`.`id`) and (`terminal`.`model_id` > 0x7f))) AS `terminal_virtual_num`,(select count(`food`.`food_id`) from `food` where (`food`.`restaurant_id` = `r`.`id`)) AS `food_num`,(select count(`table`.`table_id`) from `table` where (`table`.`restaurant_id` = `r`.`id`)) AS `table_num`,(select count(`order`.`id`) from `order` where ((`order`.`restaurant_id` = `r`.`id`) and (`order`.`total_price` is not null))) AS `order_paid`,(select count(`order_history`.`id`) from `order_history` where ((`order_history`.`restaurant_id` = `r`.`id`) and (`order_history`.`total_price` is not null))) AS `order_history_paid`,(select count(`table`.`table_id`) from `table` where ((`table`.`restaurant_id` = `r`.`id`) and exists(select 1 from `order` where ((`order`.`table_alias` = `table`.`table_alias`) and isnull(`order`.`total_price`) and (`order`.`restaurant_id` = `r`.`id`))))) AS `table_using` from `restaurant` `r`;

-- -----------------------------------------------------
-- View`terminal_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS terminal_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `terminal_view` AS select `t`.`pin` AS `pin`,`t`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`t`.`model_name` AS `model_name`,`t`.`model_id` AS `model_id`,(case `t`.`model_id` when 0 then 'BlackBerry' when 1 then 'Android' when 2 then 'iPhone' when 3 then 'WindowsMobile' end) AS `model_id_name`,`t`.`entry_date` AS `entry_date`,`t`.`discard_date` AS `discard_date`,format((((`t`.`idle_duration` / 3600) / 24) / 30),1) AS `idle_month`,format((((`t`.`work_duration` / 3600) / 24) / 30),1) AS `work_month`,`t`.`expire_date` AS `expire_date`,(case when (`t`.`restaurant_id` = 2) then '空闲' when (`t`.`restaurant_id` = 3) then '废弃' when ((`t`.`restaurant_id` > 10) and (now() <= `t`.`expire_date`)) then '使用' when ((`t`.`restaurant_id` > 10) and (now() > `t`.`expire_date`)) then '过期' end) AS `status`,format(((`t`.`work_duration` / (`t`.`work_duration` + `t`.`idle_duration`)) * 100),0) AS `use_rate`,`t`.`owner_name` AS `owner_name`,`t`.`idle_duration` AS `idle_duration`,`t`.`work_duration` AS `work_duration` from (`terminal` `t` left join `restaurant` `r` on((`t`.`restaurant_id` = `r`.`id`)));



