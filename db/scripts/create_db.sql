SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `wireless_order_db` DEFAULT CHARACTER SET utf8 ;
USE `wireless_order_db` ;

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
-- Table `wireless_order_db`.`food`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to this food' ,
  `alias_id` SMALLINT UNSIGNED NOT NULL COMMENT 'the waiter use this alias id to select food in terminal' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name of the food' ,
  `pinyin` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the pinyin to this food' ,
  `unit_price` DECIMAL(7,2) UNSIGNED NOT NULL DEFAULT 0.0 COMMENT 'the unit price of the food' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'indicates the food belong to which restaurant' ,
  `kitchen` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the kitchen number which the food belong to. the maximum value (255) means the food does not belong to any kitchen.' ,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates the status to this food, the value is the combination of values below.\n特价菜 ：0x01\n推荐菜 ：0x02\n停售　 ：0x04\n赠送     ：0x08' ,
  `img1` BINARY NULL DEFAULT NULL ,
  `img2` BINARY NULL DEFAULT NULL ,
  `img3` BINARY NULL DEFAULT NULL ,
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT 'indicates whether the food information is enabled or not' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_food_restaurant` (`restaurant_id` ASC) ,
  INDEX `ix_food_alias_id` (`alias_id` ASC) ,
  CONSTRAINT `fk_food_restaurant`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'This table contains the all restaurant\'s food information.' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'external key associated with the  restaurant table' ,
  `order_date` DATETIME NOT NULL COMMENT 'the order\'s date and time' ,
  `gift_price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT 'the gift price to this order' ,
  `total_price` DECIMAL(10,2) NULL DEFAULT NULL COMMENT 'The total price to this order.\nIts default value is NULL, means the order not be paid, otherwise means the order has been paid.' ,
  `total_price_2` DECIMAL(10,2) NULL DEFAULT NULL COMMENT 'the actual total price to this order' ,
  `custom_num` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the amount of custom to this order' ,
  `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the waiter who operates on this order' ,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type to pay order, it would be one of the values below.\n现金 : 1\n刷卡 : 2\n会员卡 : 3\n签单：4\n挂账 ：5\n' ,
  `category` TINYINT NOT NULL DEFAULT 1 COMMENT 'the category to this order, it should be one the values below.\n一般 : 1\n外卖 : 2\n并台 : 3\n拼台 : 4' ,
  `discount_type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the discount type to this order' ,
  `member_id` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member\'s alias id' ,
  `member` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member name' ,
  `terminal_model` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the terminal model to this order' ,
  `terminal_pin` INT NOT NULL DEFAULT 0 COMMENT 'the terminal pin to this order' ,
  `table_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the table alias id to this order' ,
  `table_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the table to this order' ,
  `table2_id` SMALLINT NULL DEFAULT NULL COMMENT 'the 2nd table alias id to this order(used for table merger)' ,
  `table2_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the 2nd table name to this order(used for table merger)' ,
  `service_rate` DECIMAL(3,2) NOT NULL DEFAULT 0 COMMENT 'the service rate to this order' ,
  `comment` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the comment to this order' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_order_restaurant` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_order_restaurant`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'This table describe the all restaurant\'s order information.' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_food`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_food` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_food` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this order detail record' ,
  `order_id` INT UNSIGNED NOT NULL COMMENT 'external key associated with the order table' ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this order detail' ,
  `food_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this food' ,
  `order_date` DATETIME NOT NULL DEFAULT 19000101 ,
  `order_count` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT 'the count that the waiter ordered. the count can be positive or negative.' ,
  `unit_price` DECIMAL(7,2) UNSIGNED NOT NULL DEFAULT 0 ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to the ordered food' ,
  `food_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates the status to this food, the value is the combination of values below.\n特价菜 ：0x01\n推荐菜 ：0x02\n停售　 ：0x04\n赠送     ：0x08' ,
  `hang_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates hang up status.\n0 - normal\n1 - hang_up\n2 - immediate' ,
  `taste_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the taste alias id' ,
  `taste_id2` SMALLINT UNSIGNED NOT NULL DEFAULT 0 ,
  `taste_id3` SMALLINT UNSIGNED NOT NULL DEFAULT 0 ,
  `taste` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the taste preference to the ordered food' ,
  `taste_price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to taste preference' ,
  `discount` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the discount to this food' ,
  `kitchen` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the kitchen number which the order food of this record belong to. the maximum value (255) means the food does not belong to any kitchen.' ,
  `comment` VARCHAR(100) NULL DEFAULT NULL COMMENT 'the comment to this record, such as the reason to cancel food' ,
  `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of waiter who deal with this record' ,
  `is_temporary` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates whether the food to this record is temporary' ,
  INDEX `fk_order_food_order` (`order_id` ASC) ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_order_food_order`
    FOREIGN KEY (`order_id` )
    REFERENCES `wireless_order_db`.`order` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'descirbe the relationship between the order and food' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`terminal`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`terminal` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`terminal` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this terminal' ,
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
  PRIMARY KEY (`id`) ,
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
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this table' ,
  `alias_id` SMALLINT UNSIGNED NULL ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'Indicates the table belongs to which restaurant.' ,
  `region` TINYINT UNSIGNED NOT NULL DEFAULT 255 COMMENT 'the region alias id to this table. 255 means the table does NOT belong to any region.' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to this table' ,
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT 'indicates whether the table information is enabled or not' ,
  `minimum_cost` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the minimum cost to this table' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_table_restaurant` (`restaurant_id` ASC) ,
  INDEX `ix_table_alias_id` (`alias_id` ASC) ,
  CONSTRAINT `fk_table_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the restaurant\'s table info' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`taste`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`taste` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`taste` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to taste table' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'indicates the taste preference belong to which restaurant' ,
  `alias_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this taste preference, the lower the alias id , the more commonly this taste preference used' ,
  `preference` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the description to this taste preference' ,
  `price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to this taste preference, used for the calc type is 按价格' ,
  `category` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the category to this taste, one of the values below.\n0 - 口味\n1 - 做法\n2 - 规格' ,
  `rate` DECIMAL(3,2) NOT NULL DEFAULT 0 COMMENT 'the rate to this taste, used for the calc type is 按比例' ,
  `calc` TINYINT NOT NULL DEFAULT 0 COMMENT 'the calculate type to this taste, one of the values below.\n0 - 按价格\n1 - 按比例' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_taste_restaurant_id` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_taste_restaurant_id`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the taste info' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`kitchen`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`kitchen` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`kitchen` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this kitchen' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `alias_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the alias id to this kitchen' ,
  `dept_id` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the department alias id that this kitchen belong to. ' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of this kitchen' ,
  `discount` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the discount to the food belong to this kitchen, range from 0.00 to 1.00' ,
  `discount_2` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 2nd discount to the food belong to this kitchen, range from 0.00 to 1.00' ,
  `discount_3` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 3rd discount to the food belong to this kitchen, range from 0.00 to 1.00' ,
  `member_discount_1` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 1st member discount to the food belong to this kitchen, range from 0.00 to 1.00' ,
  `member_discount_2` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 2nd member discount to the food belong to this kitchen, range from 0.00 to 1.00' ,
  `member_discount_3` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 3rd member discount to the food belong to this kitchen, range from 0.00 to 1.00' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_kitchen_restaurant` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_kitchen_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the kitchen information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this memeber' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `alias_id` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the alias id to this member, it could be any useful value determined by user' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to this member' ,
  `tele` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the telephone to this member' ,
  `birth` DATE NULL DEFAULT NULL COMMENT 'the birthday to this member' ,
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT 'the balance to this member' ,
  `discount_type` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the discount type to this member, it\'s one of the values below.\n0 - using 1st member discount\n1 - using 2nd member discount' ,
  `exchange_rate` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT 'the rate between the balance and the amount of order' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_member_restaurant1` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_member_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_history` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'external key associated with the  restaurant table' ,
  `order_date` DATETIME NOT NULL COMMENT 'the order\'s date and time' ,
  `gift_price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT 'the gift price to this order' ,
  `total_price` DECIMAL(10,2) NULL DEFAULT NULL COMMENT 'The total price to this order.\nIts default value is NULL, means the order not be paid, otherwise means the order has been paid.' ,
  `total_price_2` DECIMAL(10,2) NULL DEFAULT NULL COMMENT 'the actual total price to this order' ,
  `custom_num` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the amount of custom to this order' ,
  `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the waiter who operates on this order' ,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type to pay order, it would be one of the values below.\n现金 : 1\n刷卡 : 2\n会员卡 : 3\n签单：4\n挂账 ：5\n' ,
  `category` TINYINT NOT NULL DEFAULT 1 COMMENT 'the category to this order, it should be one the values below.\n一般 : 1\n外卖 : 2\n并台 : 3\n拼台 : 4' ,
  `discount_type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the discount type to this order' ,
  `member_id` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member\'s alias id' ,
  `member` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member name' ,
  `terminal_model` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the terminal model to this order' ,
  `terminal_pin` INT NOT NULL DEFAULT 0 COMMENT 'the terminal pin to this order' ,
  `table_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the table alias id to this order' ,
  `table_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the table name to this order' ,
  `table2_id` SMALLINT NULL DEFAULT NULL COMMENT 'the 2nd table alias id to this order(used for table merger)' ,
  `table2_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the 2nd table name to this order(used for table merger)' ,
  `service_rate` DECIMAL(3,2) NOT NULL DEFAULT 0 COMMENT 'the service rate to this order' ,
  `comment` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the comment to this order' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_order_restaurant` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_order_restaurant0`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
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
-- Table `wireless_order_db`.`member_charge`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_charge` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_charge` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this record' ,
  `member_id` INT NOT NULL ,
  `date` DATETIME NOT NULL DEFAULT 19000101 COMMENT 'the date to recharge' ,
  `money` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT 'the money to recharge' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_member_charge_member1` (`member_id` ASC) ,
  CONSTRAINT `fk_member_charge_member1`
    FOREIGN KEY (`member_id` )
    REFERENCES `wireless_order_db`.`member` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member charge records ' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_food_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_food_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_food_history` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this order detail record' ,
  `order_id` INT UNSIGNED NOT NULL ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this order history detail' ,
  `food_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this food' ,
  `order_count` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT 'the count that the waiter ordered. the count can be positive or negative.' ,
  `order_date` DATETIME NOT NULL DEFAULT 19000101 ,
  `unit_price` DECIMAL(7,2) UNSIGNED NOT NULL DEFAULT 0 ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to the ordered food' ,
  `food_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates the status to this food, the value is the combination of values below.\n特价菜 ：0x01\n推荐菜 ：0x02\n停售　 ：0x04\n赠送     ：0x08' ,
  `taste` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the taste preference to the ordered food' ,
  `taste_price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to taste preference' ,
  `taste_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the taste alias id' ,
  `taste_id2` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the 2nd taste to this record' ,
  `taste_id3` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the 3rd taste to this record' ,
  `discount` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the discount to this food' ,
  `kitchen` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the kitchen number which the order food of this record belong to. the maximum value (255) means the food does not belong to any kitchen.' ,
  `comment` VARCHAR(100) NULL DEFAULT NULL COMMENT 'the comment to this record, such as the reason to cancel food' ,
  `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of waiter who deal with this record' ,
  `is_temporary` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates whether the food to this record is temporary' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_order_food_history_order_history1` (`order_id` ASC) ,
  CONSTRAINT `fk_order_food_history_order_history1`
    FOREIGN KEY (`order_id` )
    REFERENCES `wireless_order_db`.`order_history` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'descirbe the relationship between the order and food' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`staff`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`staff` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`staff` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `terminal_id` INT NOT NULL ,
  `alias_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the alias id to this stuff' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to this stuff' ,
  `pwd` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the password to this staff whose format is MD5' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_staff_restaurant1` (`restaurant_id` ASC) ,
  INDEX `fk_staff_terminal1` (`terminal_id` ASC) ,
  CONSTRAINT `fk_staff_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_staff_terminal1`
    FOREIGN KEY (`terminal_id` )
    REFERENCES `wireless_order_db`.`terminal` (`id` )
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
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to setting' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'the restaurant id to this setting' ,
  `price_tail` TINYINT UNSIGNED NOT NULL DEFAULT 2 COMMENT 'indicates how to deal with the tail of price:\n不处理 : 0\n小数抹零 : 1\n小数四舍五入 : 2' ,
  `auto_reprint` TINYINT NOT NULL DEFAULT 1 COMMENT 'indicates whether to auto re-print' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_setting_restaurant` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_setting_restaurant`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
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
  INDEX `fk_shift_restaurant1` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_shift_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'the shift history to each restaurant' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`region`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`region` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`region` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to this record' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `alias_id` TINYINT UNSIGNED NULL COMMENT 'the alias id to this table region' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to this table region' ,
  PRIMARY KEY (`id`, `restaurant_id`) ,
  INDEX `fk_region_restaurant1` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_region_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the region information to the tables' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`department`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`department` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`department` (
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `dept_id` TINYINT UNSIGNED NOT NULL DEFAULT 0 ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' ,
  INDEX `fk_super_kitchen_restaurant` (`restaurant_id` ASC) ,
  PRIMARY KEY (`dept_id`, `restaurant_id`) ,
  CONSTRAINT `fk_super_kitchen_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the department information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`temp_order_food_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`temp_order_food_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`temp_order_food_history` (
  `order_id` INT UNSIGNED NOT NULL COMMENT 'the order id to this order detail record' ,
  `food_id` SMALLINT UNSIGNED NOT NULL COMMENT 'the food id to this order detail record' ,
  `taste_id` SMALLINT UNSIGNED NOT NULL COMMENT 'the taste id to this order detail record' ,
  `taste_id2` SMALLINT UNSIGNED NOT NULL COMMENT 'the 2nd taste id to this order detail record' ,
  `taste_id3` SMALLINT UNSIGNED NOT NULL COMMENT 'the 3rd taste id to this order detail record' ,
  `is_temporary` TINYINT NOT NULL DEFAULT 0 COMMENT 'the flag indicates whether the food to this record is temporary' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the food name to this order detail record' ,
  `taste` VARCHAR(45) NOT NULL COMMENT 'the taste preference to this order detail record' ,
  `order_count` DECIMAL(5,2) NOT NULL COMMENT 'the sum of order count to this order detail record' ,
  `unit_price` DECIMAL(7,2) UNSIGNED NOT NULL COMMENT 'the unit price to this order detail record' ,
  `taste_price` DECIMAL(7,2) UNSIGNED NOT NULL COMMENT 'the taste price to this order detail record' ,
  `discount` DECIMAL(3,2) NOT NULL COMMENT 'the discount to this order detail record' ,
  `food_status` TINYINT NOT NULL COMMENT 'the food status to this order detail record' ,
  `kitchen` TINYINT UNSIGNED NOT NULL COMMENT 'the kitchen to this order detail record' ,
  `waiter` VARCHAR(45) NOT NULL COMMENT 'the waiter name to this order detail record' ,
  PRIMARY KEY (`order_id`, `food_id`, `taste_id`, `taste_id2`, `taste_id3`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'temporary order food history table for performance problem' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`temp_order_food_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`temp_order_food_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`temp_order_food_history` (
  `order_id` INT UNSIGNED NOT NULL COMMENT 'the order id to this order detail record' ,
  `food_id` SMALLINT UNSIGNED NOT NULL COMMENT 'the food id to this order detail record' ,
  `taste_id` SMALLINT UNSIGNED NOT NULL COMMENT 'the taste id to this order detail record' ,
  `taste_id2` SMALLINT UNSIGNED NOT NULL COMMENT 'the 2nd taste id to this order detail record' ,
  `taste_id3` SMALLINT UNSIGNED NOT NULL COMMENT 'the 3rd taste id to this order detail record' ,
  `is_temporary` TINYINT NOT NULL DEFAULT 0 COMMENT 'the flag indicates whether the food to this record is temporary' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the food name to this order detail record' ,
  `taste` VARCHAR(45) NOT NULL COMMENT 'the taste preference to this order detail record' ,
  `order_count` DECIMAL(5,2) NOT NULL COMMENT 'the sum of order count to this order detail record' ,
  `unit_price` DECIMAL(7,2) UNSIGNED NOT NULL COMMENT 'the unit price to this order detail record' ,
  `taste_price` DECIMAL(7,2) UNSIGNED NOT NULL COMMENT 'the taste price to this order detail record' ,
  `discount` DECIMAL(3,2) NOT NULL COMMENT 'the discount to this order detail record' ,
  `food_status` TINYINT NOT NULL COMMENT 'the food status to this order detail record' ,
  `kitchen` TINYINT UNSIGNED NOT NULL COMMENT 'the kitchen to this order detail record' ,
  `waiter` VARCHAR(45) NOT NULL COMMENT 'the waiter name to this order detail record' ,
  PRIMARY KEY (`order_id`, `food_id`, `taste_id`, `taste_id2`, `taste_id3`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'temporary order food history table for performance problem' ;


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
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `supplier_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the supplier alias id that this material detail record belong to' ,
  `material_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the material alias id that this material detail record belong to' ,
  `price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to this material record' ,
  `price_prev` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the previous price to this material detail record' ,
  `date` DATETIME NULL DEFAULT NULL COMMENT 'the date to this material detail record' ,
  `staff` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the staff name to this material detail record' ,
  `dept_id` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the super kitchen id to this material detail record' ,
  `dept2_id` TINYINT UNSIGNED NULL DEFAULT NULL COMMENT 'indicates the 调入部门 in case of the type is “调出”' ,
  `amount` FLOAT NOT NULL DEFAULT 0 COMMENT 'the amount to this material detail record' ,
  `amount_prev` FLOAT NOT NULL DEFAULT 0 COMMENT 'the previous amount to this material detail record' ,
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the type is as below.\n0 : 消耗\n1 : 报损 \n2 : 销售\n3 : 退货\n4 : 出仓\n5 : 入库\n6 : 调出\n7 : 调入\n8 : 盘点' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_material_detail_restaurant` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_material_detail_restaurant`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


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



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;



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
-- View`order_food_history_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_history_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_food_history_view` AS select sum(`order_food_history`.`order_count`) AS `order_count`,max(`order_food_history`.`unit_price`) AS `unit_price`,max(`order_food_history`.`taste_price`) AS `taste_price`,max(`order_food_history`.`name`) AS `name`,max(`order_food_history`.`taste`) AS `taste`,max(`order_food_history`.`taste_id`) AS `taste_id`,max(`order_food_history`.`discount`) AS `discount`,max(`order_food_history`.`food_status`) AS `food_status`,max(`order_food_history`.`kitchen`) AS `kitchen`,max(`order_food_history`.`waiter`) AS `waiter`,`order_food_history`.`order_id` AS `order_id`,`order_food_history`.`food_id` AS `food_id` from `order_food_history` group by `order_food_history`.`order_id`,`order_food_history`.`food_id`,`order_food_history`.`taste_id`,`order_food_history`.`taste_id2`,`order_food_history`.`taste_id3`,`order_food_history`.`is_temporary` having (sum(`order_food_history`.`order_count`) > 0);

-- -----------------------------------------------------
-- View`order_food_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_food_view` AS select sum(`order_food`.`order_count`) AS `order_count`,max(`order_food`.`unit_price`) AS `unit_price`,max(`order_food`.`taste_price`) AS `taste_price`,max(`order_food`.`name`) AS `name`,max(`order_food`.`taste`) AS `taste`,max(`order_food`.`taste_id`) AS `taste_id`,max(`order_food`.`discount`) AS `discount`,max(`order_food`.`food_status`) AS `food_status`,`order_food`.`order_id` AS `order_id`,`order_food`.`food_id` AS `food_id` from `order_food` group by `order_food`.`order_id`,`order_food`.`food_id`,`order_food`.`taste_id`,`order_food`.`taste_id2`,`order_food`.`taste_id3`,`order_food`.`is_temporary` having (sum(`order_food`.`order_count`) > 0);

-- -----------------------------------------------------
-- View`order_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_view` AS select `a`.`id` AS `id`,`a`.`table_id` AS `alias_id`,`a`.`table_name` AS `table_name`,`a`.`order_date` AS `order_date`,`a`.`category` AS `category`,(case `a`.`category` when 1 then '一般' when 2 then '外卖' when 3 then '并台' when 4 then '拼台' end) AS `category_name`,`a`.`comment` AS `comment`,`a`.`service_rate` AS `service_rate`,format((sum((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price`,format((sum(((`b`.`unit_price` * (1 - `b`.`discount`)) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_discount`,format((sum(((`b`.`unit_price` * `b`.`discount`) * (case when ((`b`.`food_status` & 8) = 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_present`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',(case `b`.`food_status` when 1 then '(特)' when 2 then '(荐)' when 3 then '(特,荐)' when 8 then '(赠)' when 9 then '(特,赠)' when 10 then '(荐,赠)' when 11 then '(特,荐,赠)' else '' end),'|',(case when (`b`.`discount` < 1) then concat('(',format((`b`.`discount` * 10),1),'折',')') else '' end),'|',format((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`),2)) separator ';') AS `foods`,(`a`.`total_price` is not null) AS `is_paid`,`a`.`total_price_2` AS `total_price_2`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`id` AS `table_id`,`a`.`waiter` AS `waiter`,`a`.`type` AS `type_value`,(case `a`.`type` when 1 then '现金' when 2 then '刷卡' when 3 then '会员卡' when 4 then '签单' when 5 then '挂账' end) AS `type_name` from (((((`order` `a` left join `order_food_view` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on(((`b`.`food_id` = `c`.`alias_id`) and (`c`.`restaurant_id` = `a`.`restaurant_id`)))) left join `table` `d` on(((`a`.`table_id` = `d`.`alias_id`) and (`d`.`restaurant_id` = `a`.`restaurant_id`)))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`) and (`t`.model_id = `a`.terminal_model))) group by `a`.`id`;

-- -----------------------------------------------------
-- View`order_history_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_history_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_history_view` AS select `a`.`id` AS `id`,`a`.`table_id` AS `alias_id`,`a`.`table_name` AS `table_name`,`a`.`order_date` AS `order_date`,`a`.`category` AS `category`,(case `a`.`category` when 1 then '一般' when 2 then '外卖' when 3 then '并台' when 4 then '拼台' end) AS `category_name`,`a`.`comment` AS `comment`,`a`.`service_rate` AS `service_rate`,format((sum((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price`,format((sum(((`b`.`unit_price` * (1 - `b`.`discount`)) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_discount`,format((sum(((`b`.`unit_price` * `b`.`discount`) * (case when ((`b`.`food_status` & 8) = 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_present`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',(case `b`.`food_status` when 1 then '(特)' when 2 then '(荐)' when 3 then '(特,荐)' when 8 then '(赠)' when 9 then '(特,赠)' when 10 then '(荐,赠)' when 11 then '(特,荐,赠)' else '' end),'|',(case when (`b`.`discount` < 1) then concat('(',format((`b`.`discount` * 10),1),'折',')') else '' end),'|',format((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`),2)) separator ';') AS `foods`,(`a`.`total_price` is not null) AS `is_paid`,`a`.`total_price_2` AS `total_price_2`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`id` AS `table_id`,`a`.`waiter` AS `waiter`,`a`.`type` AS `type_value`,(case `a`.`type` when 1 then '现金' when 2 then '刷卡' when 3 then '会员卡' when 4 then '签单' when 5 then '挂账' end) AS `type_name` from (((((`order_history` `a` left join `temp_order_food_history` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on(((`b`.`food_id` = `c`.`alias_id`) and (`c`.`restaurant_id` = `a`.`restaurant_id`)))) left join `table` `d` on(((`a`.`table_id` = `d`.`alias_id`) and (`d`.`restaurant_id` = `a`.`restaurant_id`)))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`) and (`t`.model_id = `a`.terminal_model))) group by `a`.`id`;

-- -----------------------------------------------------
-- View`restaurant_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS restaurant_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `restaurant_view` AS select `r`.`id` AS `id`,`r`.`account` AS `account`,`r`.`restaurant_name` AS `restaurant_name`,`r`.`tele1` AS `tele1`,`r`.`tele2` AS `tele2`,`r`.`address` AS `address`,`r`.`restaurant_info` AS `restaurant_info`,`r`.`record_alive` AS `record_alive`,(select count(`order`.`id`) from `order` where (`order`.`restaurant_id` = `r`.`id`)) AS `order_num`,(select count(`order_history`.`id`) from `order_history` where (`order_history`.`restaurant_id` = `r`.`id`)) AS `order_history_num`,(select count(`terminal`.`pin`) from `terminal` where ((`terminal`.`restaurant_id` = `r`.`id`) and (`terminal`.`model_id` <= 0x7f))) AS `terminal_num`,(select count(`terminal`.`pin`) from `terminal` where ((`terminal`.`restaurant_id` = `r`.`id`) and (`terminal`.`model_id` > 0x7f))) AS `terminal_virtual_num`,(select count(`food`.`id`) from `food` where (`food`.`restaurant_id` = `r`.`id`)) AS `food_num`,(select count(`table`.`id`) from `table` where (`table`.`restaurant_id` = `r`.`id`)) AS `table_num`,(select count(`order`.`id`) from `order` where ((`order`.`restaurant_id` = `r`.`id`) and (`order`.`total_price` is not null))) AS `order_paid`,(select count(`order_history`.`id`) from `order_history` where ((`order_history`.`restaurant_id` = `r`.`id`) and (`order_history`.`total_price` is not null))) AS `order_history_paid`,(select count(`table`.`id`) from `table` where ((`table`.`restaurant_id` = `r`.`id`) and exists(select 1 from `order` where ((`order`.`table_id` = `table`.`alias_id`) and isnull(`order`.`total_price`) and (`order`.`restaurant_id` = `r`.`id`))))) AS `table_using` from `restaurant` `r`;

-- -----------------------------------------------------
-- View`terminal_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS terminal_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `terminal_view` AS select `t`.`pin` AS `pin`,`t`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`t`.`model_name` AS `model_name`,`t`.`model_id` AS `model_id`,(case `t`.`model_id` when 0 then 'BlackBerry' when 1 then 'Android' when 2 then 'iPhone' when 3 then 'WindowsMobile' end) AS `model_id_name`,`t`.`entry_date` AS `entry_date`,`t`.`discard_date` AS `discard_date`,format((((`t`.`idle_duration` / 3600) / 24) / 30),1) AS `idle_month`,format((((`t`.`work_duration` / 3600) / 24) / 30),1) AS `work_month`,`t`.`expire_date` AS `expire_date`,(case when (`t`.`restaurant_id` = 2) then '空闲' when (`t`.`restaurant_id` = 3) then '废弃' when ((`t`.`restaurant_id` > 10) and (now() <= `t`.`expire_date`)) then '使用' when ((`t`.`restaurant_id` > 10) and (now() > `t`.`expire_date`)) then '过期' end) AS `status`,format(((`t`.`work_duration` / (`t`.`work_duration` + `t`.`idle_duration`)) * 100),0) AS `use_rate`,`t`.`owner_name` AS `owner_name`,`t`.`idle_duration` AS `idle_duration`,`t`.`work_duration` AS `work_duration` from (`terminal` `t` left join `restaurant` `r` on((`t`.`restaurant_id` = `r`.`id`)));

