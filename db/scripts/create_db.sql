﻿SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
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
  `tele1` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'One of the telephones to this restaurant.' ,
  `tele2` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'One of the telephones to this restaurant.' ,
  `address` VARCHAR(70) NOT NULL DEFAULT '' COMMENT 'The address to this restaurant.' ,
  `total_income` DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT 'the total income of the restaurant' ,
  `record_alive` BIGINT NOT NULL DEFAULT 0 COMMENT 'Indicates how long the order record of this restaurant can be persisted. It\'s represented in second. Value 0 means the records never expire.' ,
  `token` VARCHAR(45) NOT NULL DEFAULT 'b60061d439af3d4cb937a0a3ddd36b34' COMMENT 'The token used for login verification in web service.' ,
  PRIMARY KEY (`id`, `account`) ,
  UNIQUE INDEX `account_UNIQUE` (`account` ASC) ,
  INDEX `token_index` (`token` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'describe the restaurnat\'s information';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`food`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT 'in the form of \"restaurant.id << 32 | food.alias_id\"' ,
  `alias_id` SMALLINT UNSIGNED NOT NULL COMMENT 'the waiter use this alias id to select food in terminal' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name of the food' ,
  `unit_price` DECIMAL(7,2) UNSIGNED NOT NULL DEFAULT 0.0 COMMENT 'the unit price of the food' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'indicates the food belong to which restaurant' ,
  `order_count` INT NOT NULL DEFAULT 0 COMMENT 'the food\'s total order count' ,
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
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to order' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'external key associated with the  restaurant table' ,
  `order_date` DATETIME NOT NULL COMMENT 'the order\'s date and time' ,
  `total_price` DECIMAL(10,2) NOT NULL DEFAULT -1 COMMENT 'The total price to this order.\nIts default value is -1, means the order not be paid, in the case the total price is greater than 0, means the order has been paid.' ,
  `custom_num` TINYINT UNSIGNED NOT NULL DEFAULT 0 ,
  `waiter` VARCHAR(45) NOT NULL COMMENT 'the waiter who operates on this order' ,
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the type to pay order, it would be one of the values below.\n现金 : 1\n刷卡 : 2\n会员卡 : 3\n挂账 ：4\n签单：5' ,
  `member_id` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member\'s alias id' ,
  `member` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member name' ,
  `terminal_model` TINYINT NOT NULL DEFAULT 0 COMMENT 'the terminal model to this order' ,
  `terminal_pin` INT NOT NULL DEFAULT 0 COMMENT 'the terminal pin to this order' ,
  `table_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the table alias id to this order' ,
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
  `order_id` INT UNSIGNED NOT NULL COMMENT 'external key associated with the order table' ,
  `food_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the alias id to this food' ,
  `order_count` DECIMAL(5,2) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the amount of this food is ordered' ,
  `unit_price` DECIMAL(7,2) UNSIGNED NOT NULL DEFAULT 0 ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to the ordered food' ,
  `taste` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the taste preference to the ordered food' ,
  `taste_price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to taste preference' ,
  `taste_id` TINYINT NOT NULL DEFAULT 0 COMMENT 'the taste alias id' ,
  `discount` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the discount to this food' ,
  `kitchen` TINYINT NOT NULL DEFAULT 0 COMMENT 'the kitchen number which the order food of this record belong to. the maximum value (255) means the food does not belong to any kitchen.' ,
  INDEX `fk_order_food_order` (`order_id` ASC) ,
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
  `model_id` TINYINT NOT NULL DEFAULT 0 COMMENT 'the model to this terminal.\nBlackBerry : 0x00\nAndroid : 0x01\nBrowser : 0xFF' ,
  `model_name` VARCHAR(45) NULL COMMENT 'the model name to the phone' ,
  `owner_name` VARCHAR(45) NOT NULL COMMENT 'the owner name of this terminal' ,
  `expire_date` DATE NULL DEFAULT 10000101 COMMENT 'the expired date to the phone' ,
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
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'in the form of \"restaurant.id << 32 | table.alias_id\"' ,
  `alias_id` SMALLINT UNSIGNED NULL ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'Indicates the table belongs to which restaurant.' ,
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT 'indicates whether the table information is enabled or not' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_table_restaurant1` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_table_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB;


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
  `member_discount_1` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 1st member discount to the food belong to this kitchen, range from 0.00 to 1.00' ,
  `member_discount_2` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 2nd member discount to the food belong to this kitchen, range from 0.00 to 1.00' ,
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
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to order' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'external key associated with the  restaurant table' ,
  `order_date` DATETIME NOT NULL COMMENT 'the order\'s date and time' ,
  `total_price` DECIMAL(10,2) NOT NULL DEFAULT -1 COMMENT 'The total price to this order.\nIts default value is -1, means the order not be paid, in the case the total price is greater than 0, means the order has been paid.' ,
  `custom_num` TINYINT UNSIGNED NOT NULL DEFAULT 0 ,
  `waiter` VARCHAR(45) NOT NULL COMMENT 'the waiter who operates on this order' ,
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the type to pay order, it would be one of the values below.\nCash : 1\nCredit Card : 2\nMember Card : 3' ,
  `member_id` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member\'s alias id' ,
  `member` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member name' ,
  `terminal_model` TINYINT NOT NULL DEFAULT 0 COMMENT 'the terminal model to this order' ,
  `terminal_pin` INT NOT NULL DEFAULT 0 COMMENT 'the terminal pin to this order' ,
  `table_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the table alias id to this order' ,
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
-- Table `wireless_order_db`.`order_food_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_food_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_food_history` (
  `order_id` INT UNSIGNED NOT NULL ,
  `food_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the alias id to this food' ,
  `order_count` DECIMAL(5,2) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the amount of this food is ordered' ,
  `unit_price` DECIMAL(7,2) UNSIGNED NOT NULL DEFAULT 0 ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to the ordered food' ,
  `taste` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the taste preference to the ordered food' ,
  `taste_price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to taste preference' ,
  `taste_id` TINYINT NOT NULL DEFAULT 0 COMMENT 'the taste alias id' ,
  `discount` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the discount to this food' ,
  `kitchen` TINYINT NOT NULL DEFAULT 0 COMMENT 'the kitchen number which the order food of this record belong to. the maximum value (255) means the food does not belong to any kitchen.' ,
  INDEX `fk_order_food_history_order_history1` (`order_id` ASC) ,
  CONSTRAINT `fk_order_food_history_order_history1`
    FOREIGN KEY (`order_id` )
    REFERENCES `wireless_order_db`.`order_history` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'descirbe the relationship between the order and food';


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
  `food_id` BIGINT UNSIGNED NOT NULL ,
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
-- View`order_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_view` AS select `a`.`id` AS `id`,`d`.`alias_id` AS `alias_id`,`a`.`order_date` AS `order_date`,format(sum(((`b`.`unit_price` + `b`.`taste_price`) * `b`.`order_count`)),2) AS `total_price`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',format(((`b`.`unit_price` + `b`.`taste_price`) * `b`.`order_count`),2)) separator ',') AS `foods`,(`a`.`total_price` > 0) AS `is_paid`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`id` AS `table_id`,`a`.`waiter` AS `waiter` from (((((`order` `a` left join `order_food` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on((`b`.`food_id` = `c`.`id`))) left join `table` `d` on((`a`.`table_id` = `d`.`id`))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`))) group by `a`.`id`,`d`.`alias_id`,`a`.`order_date`,`a`.`custom_num`,`a`.`total_price`;

-- -----------------------------------------------------
-- View`restaurant_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS restaurant_view;

CREATE VIEW `restaurant_view` AS select 
`r`.`id` AS `id`,`r`.`account` AS `account`,
`r`.`restaurant_name` AS `restaurant_name`,
`r`.`restaurant_info` AS `restaurant_info`,
`r`.`record_alive` AS `record_alive`,
(select count(`order`.`id`) from `order` where (`order`.`restaurant_id` = `r`.`id`)) AS `order_num`,
(select count(`terminal`.`pin`) from `terminal` where (`terminal`.`restaurant_id` = `r`.`id`)) AS `terminal_num`,
(select count(`food`.`id`) from `food` where (`food`.`restaurant_id` = `r`.`id`)) AS `food_num`,
(select count(`table`.`id`) from `table` where (`table`.`restaurant_id` = `r`.`id`)) AS `table_num`,
(select count(`order`.`id`) from `order` where (`order`.`restaurant_id` = `r`.`id` AND `order`.total_price>0)) AS `order_paid`,
(select count(`table`.`id`) from `table` where (`table`.`restaurant_id` = `r`.`id` AND EXISTS (SELECT * FROM `order` WHERE `order`.table_id = `table`.id AND NOT `order`.total_price>0))) AS `table_using`
from `restaurant` `r`;

-- -----------------------------------------------------
-- View`terminal_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS terminal_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `terminal_view` AS select `t`.`pin` AS
`pin`,`t`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,
`t`.`model_name` AS `model_name`,`t`.`entry_date` AS `entry_date`,`t`.`discard_date` AS `discard_date`,
format((((`t`.`idle_duration` / 3600) / 24) / 30),1) AS `idle_month`,
format((((`t`.`work_duration` / 3600) / 24) / 30),1) AS `work_month`,
`t`.`expire_date` AS `expire_date`,
(case when (`t`.`restaurant_id` = 2) then '空闲' when (`t`.`restaurant_id` = 3) then '废弃' when ((`t`.`restaurant_id` > 10) and (now() <= `t`.`expire_date`)) then '使用' when ((`t`.`restaurant_id` > 10) and (now() > `t`.`expire_date`)) then '过期' end) AS `status`,
format(((`t`.`work_duration` / (`t`.`work_duration` + `t`.`idle_duration`)) * 100),0) AS `use_rate`,
`t`.`owner_name`,
`t`.idle_duration,
`t`.work_duration
from (`terminal` `t` left join `restaurant` `r` on((`t`.`restaurant_id` = `r`.`id`)))

