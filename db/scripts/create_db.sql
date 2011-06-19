SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `wireless_order_db` ;
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
  `pwd2` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the 2nd password to this restaurant, used to grant permission to change the order' ,
  `tele1` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'One of the telephones to this restaurant.' ,
  `tele2` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'One of the telephones to this restaurant.' ,
  `address` VARCHAR(70) NOT NULL DEFAULT '' COMMENT 'The address to this restaurant.' ,
  `record_alive` BIGINT NOT NULL DEFAULT 0 COMMENT 'Indicates how long the order record of this restaurant can be persisted. It\'s represented in second. Value 0 means the records never expire.' ,
  PRIMARY KEY (`id`, `account`) ,
  UNIQUE INDEX `account_UNIQUE` (`account` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'describe the restaurnat\'s information';


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
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates the status to this food, the value is the combination of values below.\n特价菜 ：0x01\n推荐菜 ：0x02\n停售　 ：0x04' ,
  `img1` BINARY NULL DEFAULT NULL ,
  `img2` BINARY NULL DEFAULT NULL ,
  `img3` BINARY NULL DEFAULT NULL ,
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT 'indicates whether the food information is enabled or not' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_food_restaurant` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_food_restaurant`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'This table contains the all restaurant\'s food information.';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'external key associated with the  restaurant table' ,
  `order_date` DATETIME NOT NULL COMMENT 'the order\'s date and time' ,
  `total_price` DECIMAL(10,2) NULL DEFAULT NULL COMMENT 'The total price to this order.\nIts default value is NULL, means the order not be paid, otherwise means the order has been paid.' ,
  `total_price_2` DECIMAL(10,2) NULL DEFAULT NULL COMMENT 'the actual total price to this order' ,
  `custom_num` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the amount of custom to this order' ,
  `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the waiter who operates on this order' ,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type to pay order, it would be one of the values below.\n现金 : 1\n刷卡 : 2\n会员卡 : 3\n挂账 ：4\n签单：5' ,
  `category` TINYINT NOT NULL DEFAULT 1 COMMENT 'the category to this order, it should be one the values below.\n一般 : 1\n外卖 : 2\n并台 : 3\n拼台 : 4' ,
  `member_id` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member\'s alias id' ,
  `member` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member name' ,
  `terminal_model` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the terminal model to this order' ,
  `terminal_pin` INT NOT NULL DEFAULT 0 COMMENT 'the terminal pin to this order' ,
  `table_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the table alias id to this order' ,
  `table_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the table to this order' ,
  `table2_id` SMALLINT NULL DEFAULT NULL COMMENT 'the 2nd table alias id to this order(used for table merger)' ,
  `table2_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the 2nd table name to this order(used for table merger)' ,
  `comment` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the comment to this order' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_order_restaurant` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_order_restaurant`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'This table describe the all restaurant\'s order information.';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_food`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_food` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_food` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this order detail record' ,
  `order_id` INT UNSIGNED NOT NULL COMMENT 'external key associated with the order table' ,
  `food_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the alias id to this food' ,
  `order_date` DATETIME NOT NULL DEFAULT 19000101 ,
  `order_count` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT 'the count that the waiter ordered. the count can be positive or negative.' ,
  `unit_price` DECIMAL(7,2) UNSIGNED NOT NULL DEFAULT 0 ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to the ordered food' ,
  `food_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates the status to this food, the value is the combination of values below.\n特价菜 ：0x01\n推荐菜 ：0x02\n停售　 ：0x04' ,
  `taste` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the taste preference to the ordered food' ,
  `taste_price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to taste preference' ,
  `taste_id` TINYINT NOT NULL DEFAULT 0 COMMENT 'the taste alias id' ,
  `discount` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the discount to this food' ,
  `kitchen` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the kitchen number which the order food of this record belong to. the maximum value (255) means the food does not belong to any kitchen.' ,
  `comment` VARCHAR(100) NULL DEFAULT NULL COMMENT 'the comment to this record, such as the reason to cancel food' ,
  `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of waiter who deal with this record' ,
  INDEX `fk_order_food_order` (`order_id` ASC) ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_order_food_order`
    FOREIGN KEY (`order_id` )
    REFERENCES `wireless_order_db`.`order` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'descirbe the relationship between the order and food';


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
  `entry_date` DATETIME NULL COMMENT 'the date to add the terminal' ,
  `idle_date` DATETIME NULL COMMENT 'the date to make the phone idle' ,
  `work_date` DATETIME NULL COMMENT 'the date to make the phone in use' ,
  `discard_date` DATETIME NULL COMMENT 'the date make the phone discarded' ,
  `idle_duration` BIGINT NOT NULL DEFAULT 0 COMMENT 'the phone\'s idle duration (expressed by second)' ,
  `work_duration` BIGINT NOT NULL DEFAULT 0 COMMENT 'the phone\'s work duration(expressed by second)' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_terminal_restaurant1` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_terminal_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'describe the terminal info';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`table`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`table` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`table` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this table' ,
  `alias_id` SMALLINT UNSIGNED NULL ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'Indicates the table belongs to which restaurant.' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to this table' ,
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT 'indicates whether the table information is enabled or not' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_table_restaurant1` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_table_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'describe the restaurant\'s table info';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`taste`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`taste` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`taste` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to taste table' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'indicates the taste preference belong to which restaurant' ,
  `alias_id` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this taste preference, the lower the alias id , the more commonly this taste preference used' ,
  `preference` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the description to this taste preference' ,
  `price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to this taste preference' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_taste_restaurant_id` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_taste_restaurant_id`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'describe the taste info';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`kitchen`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`kitchen` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`kitchen` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this kitchen' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `alias_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the alias id to this kitchen' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of this kitchen' ,
  `discount` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the discount to the food belong to this kitchen, range from 0.00 to 1.00' ,
  `discount_2` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 2nd discount to the food belong to this kitchen, range from 0.00 to 1.00' ,
  `discount_3` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 3rd discount to the food belong to this kitchen, range from 0.00 to 1.00' ,
  `member_discount_1` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 1st member discount to the food belong to this kitchen, range from 0.00 to 1.00' ,
  `member_discount_2` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 2nd member discount to the food belong to this kitchen, range from 0.00 to 1.00' ,
  `member_discount_3` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 3rd member discount to the food belong to this kitchen, range from 0.00 to 1.00' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_kitchen_restaurant1` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_kitchen_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'describe the kitchen information';


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
DEFAULT CHARACTER SET = utf8
COMMENT = 'describe the member information';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_history` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'external key associated with the  restaurant table' ,
  `order_date` DATETIME NOT NULL COMMENT 'the order\'s date and time' ,
  `total_price` DECIMAL(10,2) NULL DEFAULT NULL COMMENT 'The total price to this order.\nIts default value is NULL, means the order not be paid, otherwise means the order has been paid.' ,
  `total_price_2` DECIMAL(10,2) NULL DEFAULT NULL COMMENT 'the actual total price to this order' ,
  `custom_num` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the amount of custom to this order' ,
  `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the waiter who operates on this order' ,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type to pay order, it would be one of the values below.\n现金 : 1\n刷卡 : 2\n会员卡 : 3\n挂账 ：4\n签单：5' ,
  `category` TINYINT NOT NULL DEFAULT 1 COMMENT 'the category to this order, it should be one the values below.\n一般 : 1\n外卖 : 2\n并台 : 3\n拼台 : 4' ,
  `member_id` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member\'s alias id' ,
  `member` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member name' ,
  `terminal_model` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the terminal model to this order' ,
  `terminal_pin` INT NOT NULL DEFAULT 0 COMMENT 'the terminal pin to this order' ,
  `table_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the table alias id to this order' ,
  `table_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the table name to this order' ,
  `table2_id` SMALLINT NULL DEFAULT NULL COMMENT 'the 2nd table alias id to this order(used for table merger)' ,
  `table2_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the 2nd table name to this order(used for table merger)' ,
  `comment` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the comment to this order' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_order_restaurant` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_order_restaurant0`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'This table preserves all the order records.';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`material`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`material` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`material` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this material' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'the id to related restaurant' ,
  `alias_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the alias id to this material' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to this material' ,
  `stock` FLOAT NOT NULL DEFAULT 0 COMMENT 'the remaining amount to this material' ,
  `price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the unit price to this material' ,
  `warning_threshold` FLOAT NOT NULL DEFAULT 0 COMMENT 'the warning threshold to this material' ,
  `danger_threshold` FLOAT NOT NULL DEFAULT 0 COMMENT 'the danger threshold to this material' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_material_restaurant1` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_material_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'describe the material information.';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`food_material`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_material` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food_material` (
  `food_id` INT UNSIGNED NOT NULL ,
  `material_id` INT NOT NULL ,
  `consumption` FLOAT NOT NULL DEFAULT 0 COMMENT 'the consumption between the food and the material' ,
  INDEX `fk_food_material_food1` (`food_id` ASC) ,
  INDEX `fk_food_material_material1` (`material_id` ASC) ,
  CONSTRAINT `fk_food_material_food1`
    FOREIGN KEY (`food_id` )
    REFERENCES `wireless_order_db`.`food` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_food_material_material1`
    FOREIGN KEY (`material_id` )
    REFERENCES `wireless_order_db`.`material` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'describe the releation ship between food and material';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`material_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`material_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`material_history` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this material history record' ,
  `material_id` INT NOT NULL ,
  `date` DATE NOT NULL DEFAULT 19000101 ,
  `price` DECIMAL(7,2) NOT NULL DEFAULT 0 ,
  `amount` FLOAT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_material_history_material1` (`material_id` ASC) ,
  CONSTRAINT `fk_material_history_material1`
    FOREIGN KEY (`material_id` )
    REFERENCES `wireless_order_db`.`material` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'preserved the material  storage history records';


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
DEFAULT CHARACTER SET = utf8
COMMENT = 'describe the member charge records ';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_food_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_food_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_food_history` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this order detail record' ,
  `order_id` INT UNSIGNED NOT NULL ,
  `food_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the alias id to this food' ,
  `order_count` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT 'the count that the waiter ordered. the count can be positive or negative.' ,
  `order_date` DATETIME NOT NULL DEFAULT 19000101 ,
  `unit_price` DECIMAL(7,2) UNSIGNED NOT NULL DEFAULT 0 ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to the ordered food' ,
  `food_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates the status to this food, the value is the combination of values below.\n特价菜 ：0x01\n推荐菜 ：0x02\n停售　 ：0x04' ,
  `taste` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the taste preference to the ordered food' ,
  `taste_price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to taste preference' ,
  `taste_id` TINYINT NOT NULL DEFAULT 0 COMMENT 'the taste alias id' ,
  `discount` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the discount to this food' ,
  `kitchen` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the kitchen number which the order food of this record belong to. the maximum value (255) means the food does not belong to any kitchen.' ,
  `comment` VARCHAR(100) NULL DEFAULT NULL COMMENT 'the comment to this record, such as the reason to cancel food' ,
  `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of waiter who deal with this record' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_order_food_history_order_history1` (`order_id` ASC) ,
  CONSTRAINT `fk_order_food_history_order_history1`
    FOREIGN KEY (`order_id` )
    REFERENCES `wireless_order_db`.`order_history` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'descirbe the relationship between the order and food';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_food_material`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_food_material` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_food_material` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this record' ,
  `order_food_id` INT NOT NULL ,
  `material_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the alias id to the material' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to the material' ,
  `price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to this material' ,
  `consumption` FLOAT NOT NULL DEFAULT 0 COMMENT 'the consumption to this material' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_order_food_material_order_food1` (`order_food_id` ASC) ,
  CONSTRAINT `fk_order_food_material_order_food1`
    FOREIGN KEY (`order_food_id` )
    REFERENCES `wireless_order_db`.`order_food` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_food_material_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_food_material_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_food_material_history` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this record' ,
  `order_food_id` INT NOT NULL ,
  `material_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the alias id to the material' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to the material' ,
  `price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to this material' ,
  `consumption` FLOAT NOT NULL DEFAULT 0 COMMENT 'the consumption to this material' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_order_food_material_history_order_food_history1` (`order_food_id` ASC) ,
  CONSTRAINT `fk_order_food_material_history_order_food_history1`
    FOREIGN KEY (`order_food_id` )
    REFERENCES `wireless_order_db`.`order_food_history` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


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
DEFAULT CHARACTER SET = utf8
COMMENT = 'the staff information ';



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

-- -----------------------------------------------------
-- View`order_food_history_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_history_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_food_history_view` AS select sum(`order_food_history`.`order_count`) AS `order_count`,max(`order_food_history`.`unit_price`) AS `unit_price`,max(`order_food_history`.`taste_price`) AS `taste_price`,max(`order_food_history`.`name`) AS `name`,max(`order_food_history`.`taste`) AS `taste`,max(`order_food_history`.`taste_id`) AS `taste_id`,max(`order_food_history`.`discount`) AS `discount`,max(`order_food_history`.`food_status`) AS `food_status`,`order_food_history`.`order_id` AS `order_id`,`order_food_history`.`food_id` AS `food_id` from `order_food_history` group by `order_food_history`.`order_id`,`order_food_history`.`food_id`,`order_food_history`.`taste_id` having (sum(`order_food_history`.`order_count`) > 0);

-- -----------------------------------------------------
-- View`order_food_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_food_view` AS select sum(`order_food`.`order_count`) AS `order_count`,max(`order_food`.`unit_price`) AS `unit_price`,max(`order_food`.`taste_price`) AS `taste_price`,max(`order_food`.`name`) AS `name`,max(`order_food`.`taste`) AS `taste`,max(`order_food`.`taste_id`) AS `taste_id`,max(`order_food`.`discount`) AS `discount`,max(`order_food`.`food_status`) AS `food_status`,`order_food`.`order_id` AS `order_id`,`order_food`.`food_id` AS `food_id` from `order_food` group by `order_food`.`order_id`,`order_food`.`food_id`,`order_food`.`taste_id` having (sum(`order_food`.`order_count`) > 0);

-- -----------------------------------------------------
-- View`order_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_view` AS select `a`.`id` AS `id`,`d`.`alias_id` AS `alias_id`,`a`.`order_date` AS `order_date`,`a`.`category` AS `category`,(case `a`.`category` when 1 then '一般' when 2 then '外卖' when 3 then '拼台' end) AS `category_name`,`a`.`comment` AS `comment`,format(sum((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`)),2) AS `total_price`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',(case `b`.`food_status` when 1 then '(特)' when 2 then '(荐)' when 3 then '(特,荐)' when 4 then '(停)' when 5 then '(特,停)' when 6 then '(荐,停)' when 7 then '(特,荐,停)' else '' end),'|',(case when (`b`.`discount` < 1) then concat('(',format((`b`.`discount` * 10),1),'折',')') else '' end),'|',format((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`),2)) separator ';') AS `foods`,(`a`.`total_price` is not null) AS `is_paid`,`a`.`total_price_2` AS `total_price_2`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`id` AS `table_id`,`a`.`waiter` AS `waiter`,`a`.`type` AS `type_value`,(case `a`.`type` when 1 then '现金' when 2 then '刷卡' when 3 then '会员卡' when 4 then '挂账' when 5 then '签单' end) AS `type_name` from (((((`order` `a` left join `order_food_view` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on(((`b`.`food_id` = `c`.`alias_id`) and (`c`.`restaurant_id` = `a`.`restaurant_id`)))) left join `table` `d` on(((`a`.`table_id` = `d`.`alias_id`) and (`d`.`restaurant_id` = `a`.`restaurant_id`)))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`))) group by `a`.`id`,`d`.`alias_id`,`a`.`order_date`,`a`.`custom_num`,`a`.`total_price`,`a`.`category`,`a`.`comment`;

-- -----------------------------------------------------
-- View`order_history_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_history_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_history_view` AS select `a`.`id` AS `id`,`d`.`alias_id` AS `alias_id`,`a`.`order_date` AS `order_date`,`a`.`category` AS `category`,(case `a`.`category` when 1 then '一般' when 2 then '外卖' when 3 then '拼台' end) AS `category_name`,`a`.`comment` AS `comment`,format(sum((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`)),2) AS `total_price`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',(case `b`.`food_status` when 1 then '(特)' when 2 then '(荐)' when 3 then '(特,荐)' when 4 then '(停)' when 5 then '(特,停)' when 6 then '(荐,停)' when 7 then '(特,荐,停)' else '' end),'|',(case when (`b`.`discount` < 1) then concat('(',format((`b`.`discount` * 10),1),'折',')') else '' end),'|',format((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`),2)) separator ';') AS `foods`,(`a`.`total_price` is not null) AS `is_paid`,`a`.`total_price_2` AS `total_price_2`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`id` AS `table_id`,`a`.`waiter` AS `waiter`,`a`.`type` AS `type_value`,(case `a`.`type` when 1 then '现金' when 2 then '刷卡' when 3 then '会员卡' when 4 then '挂账' when 5 then '签单' end) AS `type_name` from (((((`order_history` `a` left join `order_food_history_view` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on(((`b`.`food_id` = `c`.`alias_id`) and (`c`.`restaurant_id` = `a`.`restaurant_id`)))) left join `table` `d` on(((`a`.`table_id` = `d`.`alias_id`) and (`d`.`restaurant_id` = `a`.`restaurant_id`)))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`))) group by `a`.`id`,`d`.`alias_id`,`a`.`order_date`,`a`.`custom_num`,`a`.`total_price`,`a`.`category`,`a`.`comment`;

-- -----------------------------------------------------
-- View`restaurant_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS restaurant_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `wireless_order_db`.`restaurant_view` AS select `r`.`id` AS `id`,`r`.`account` AS `account`,`r`.`restaurant_name` AS `restaurant_name`,`r`.`tele1` AS `tele1`,`r`.`tele2` AS `tele2`,`r`.`address` AS `address`,`r`.`restaurant_info` AS `restaurant_info`,`r`.`record_alive` AS `record_alive`,(select count(`wireless_order_db`.`order`.`id`) from `wireless_order_db`.`order` where (`wireless_order_db`.`order`.`restaurant_id` = `r`.`id`)) AS `order_num`,(select count(`wireless_order_db`.`terminal`.`pin`) from `wireless_order_db`.`terminal` where (`wireless_order_db`.`terminal`.`restaurant_id` = `r`.`id`)) AS `terminal_num`,(select count(`wireless_order_db`.`food`.`id`) from `wireless_order_db`.`food` where (`wireless_order_db`.`food`.`restaurant_id` = `r`.`id`)) AS `food_num`,(select count(`wireless_order_db`.`table`.`id`) from `wireless_order_db`.`table` where (`wireless_order_db`.`table`.`restaurant_id` = `r`.`id`)) AS `table_num`,(select count(`wireless_order_db`.`order`.`id`) from `wireless_order_db`.`order` where ((`wireless_order_db`.`order`.`restaurant_id` = `r`.`id`) and (`wireless_order_db`.`order`.`total_price` > 0))) AS `order_paid`,(select count(`wireless_order_db`.`table`.`id`) from `wireless_order_db`.`table` where ((`wireless_order_db`.`table`.`restaurant_id` = `r`.`id`) and exists(select 1 from `wireless_order_db`.`order` where ((`wireless_order_db`.`order`.`table_id` = `wireless_order_db`.`table`.`id`) and (`wireless_order_db`.`order`.`total_price` <= 0))))) AS `table_using` from `wireless_order_db`.`restaurant` `r`;

-- -----------------------------------------------------
-- View`terminal_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS terminal_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `terminal_view` AS select `t`.`pin` AS `pin`,`t`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`t`.`model_name` AS `model_name`,`t`.`model_id` AS `model_id`,`t`.`entry_date` AS `entry_date`,`t`.`discard_date` AS `discard_date`,format((((`t`.`idle_duration` / 3600) / 24) / 30),1) AS `idle_month`,format((((`t`.`work_duration` / 3600) / 24) / 30),1) AS `work_month`,`t`.`expire_date` AS `expire_date`,(case when (`t`.`restaurant_id` = 2) then '空闲' when (`t`.`restaurant_id` = 3) then '废弃' when ((`t`.`restaurant_id` > 10) and (now() <= `t`.`expire_date`)) then '使用' when ((`t`.`restaurant_id` > 10) and (now() > `t`.`expire_date`)) then '过期' end) AS `status`,format(((`t`.`work_duration` / (`t`.`work_duration` + `t`.`idle_duration`)) * 100),0) AS `use_rate`,`t`.`owner_name` AS `owner_name`,`t`.`idle_duration` AS `idle_duration`,`t`.`work_duration` AS `work_duration` from (`terminal` `t` left join `restaurant` `r` on((`t`.`restaurant_id` = `r`.`id`)));

