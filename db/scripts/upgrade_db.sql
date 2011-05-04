USE `wireless_order_db` ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`restaurant`
-- -----------------------------------------------------


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
-- Table `wireless_order_db`.`kitchen`
-- -----------------------------------------------------
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


INSERT INTO `kitchen`(`restaurant_id`,`alias_id`,`name`) 
SELECT `restaurant`.`id`,0,'厨房1'
FROM `restaurant`
WHERE `restaurant`.`id` > 10;
INSERT INTO `kitchen`(`restaurant_id`,`alias_id`,`name`) 
SELECT `restaurant`.`id`,1,'厨房2'
FROM `restaurant`
WHERE `restaurant`.`id` > 10;
INSERT INTO `kitchen`(`restaurant_id`,`alias_id`,`name`) 
SELECT `restaurant`.`id`,2,'厨房3'
FROM `restaurant`
WHERE `restaurant`.`id` > 10;
INSERT INTO `kitchen`(`restaurant_id`,`alias_id`,`name`) 
SELECT `restaurant`.`id`,3,'厨房4'
FROM `restaurant`
WHERE `restaurant`.`id` > 10;
INSERT INTO `kitchen`(`restaurant_id`,`alias_id`,`name`) 
SELECT `restaurant`.`id`,4,'厨房5'
FROM `restaurant`
WHERE `restaurant`.`id` > 10;
INSERT INTO `kitchen`(`restaurant_id`,`alias_id`,`name`) 
SELECT `restaurant`.`id`,5,'厨房6'
FROM `restaurant`
WHERE `restaurant`.`id` > 10;
INSERT INTO `kitchen`(`restaurant_id`,`alias_id`,`name`) 
SELECT `restaurant`.`id`,6,'厨房7'
FROM `restaurant`
WHERE `restaurant`.`id` > 10;
INSERT INTO `kitchen`(`restaurant_id`,`alias_id`,`name`) 
SELECT `restaurant`.`id`,7,'厨房8'
FROM `restaurant`
WHERE `restaurant`.`id` > 10;
INSERT INTO `kitchen`(`restaurant_id`,`alias_id`,`name`) 
SELECT `restaurant`.`id`,8,'厨房9'
FROM `restaurant`
WHERE `restaurant`.`id` > 10;
INSERT INTO `kitchen`(`restaurant_id`,`alias_id`,`name`) 
SELECT `restaurant`.`id`,9,'厨房10'
FROM `restaurant`
WHERE `restaurant`.`id` > 10;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`taste`
-- -----------------------------------------------------


-- -----------------------------------------------------
-- Table `wireless_order_db`.`food`
-- -----------------------------------------------------
ALTER TABLE `food` RENAME TO `food_old`;
ALTER TABLE `food_old` DROP FOREIGN KEY `fk_food_restaurant`;
CREATE  TABLE IF NOT EXISTS `food` (
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
    REFERENCES `restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'This table contains the all restaurant\'s food information.';

INSERT INTO `food`(`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `order_count`,`kitchen`,`enabled`) 
SELECT `id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `order_count`,`kitchen`,`enabled` FROM `food_old`;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`table`
-- -----------------------------------------------------


-- -----------------------------------------------------
-- Table `wireless_order_db`.`terminal`
-- -----------------------------------------------------
ALTER TABLE `terminal` RENAME TO `terminal_old`;
ALTER TABLE `terminal_old` DROP FOREIGN KEY `fk_terminal_restaurant1`;

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

INSERT INTO `terminal`(`pin`, `restaurant_id`, `model_name`, `owner_name`,`expire_date`, `entry_date`,`idle_date`,`work_date`,`discard_date`,`idle_duration`,`work_duration`) 
SELECT `pin`, `restaurant_id`, `model_name`, `owner_name`,`expire_date`, `entry_date`,`idle_date`,`work_date`,`discard_date`,`idle_duration`,`work_duration` FROM `terminal_old`;


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
-- Table `wireless_order_db`.`order`
-- -----------------------------------------------------
ALTER TABLE `order` DROP FOREIGN KEY `fk_order_table`;
UPDATE `order`,`table` SET `order`.`table_id` = `table`.`alias_id` 
WHERE `order`.`table_id` = `table`.`id`;
ALTER TABLE `order` DROP FOREIGN KEY `fk_order_restaurant`;
ALTER TABLE `order` RENAME TO `order_old`;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order` (
  `id` INT UNSIGNED NOT NULL COMMENT 'the id to order' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'external key associated with the  restaurant table' ,
  `order_date` DATETIME NOT NULL COMMENT 'the order\'s date and time' ,
 `total_price` DECIMAL(10,2) NULL DEFAULT NULL COMMENT 'The total price to this order.\nIts default value is NULL, means the order not be paid, otherwise means the order has been paid.' ,
  `custom_num` TINYINT UNSIGNED NOT NULL DEFAULT 0 ,
  `waiter` VARCHAR(45) NOT NULL COMMENT 'the waiter who operates on this order' ,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type to pay order, it would be one of the values below.\n现金 : 1\n刷卡 : 2\n会员卡 : 3\n挂账 ：4\n签单：5' ,
  `member_id` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member\'s alias id' ,
  `member` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member name' ,
  `terminal_model` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the terminal model to this order' ,
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

INSERT INTO `order`(`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `custom_num`, `waiter`)
SELECT `id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `custom_num`, `waiter`
FROM `order_old`;

ALTER TABLE `order` MODIFY COLUMN
`id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to order';

-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_food`
-- -----------------------------------------------------
ALTER TABLE `order_food` DROP FOREIGN KEY `fk_order_food_food1`;
UPDATE `order_food`,`food` SET `order_food`.`food_id` = `food`.`alias_id` 
WHERE `order_food`.`food_id` = `food`.`id`;
ALTER TABLE `order_food` DROP FOREIGN KEY `fk_order_food_order`;
ALTER TABLE `order_food` RENAME TO `order_food_old`;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_food` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this order detail record' ,
  `order_id` INT UNSIGNED NOT NULL COMMENT 'external key associated with the order table' ,
  `food_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the alias id to this food' ,
  `order_date` DATETIME NOT NULL DEFAULT 19000101 ,
  `order_count` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT 'the count that the waiter ordered. the count can be positive or negative.' ,
  `unit_price` DECIMAL(7,2) UNSIGNED NOT NULL DEFAULT 0 ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to the ordered food' ,
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


INSERT INTO `order_food`(`order_id`, `food_id`, `order_count`, `unit_price`, `name`,`taste`,`taste_price`,`taste_id`)
SELECT `order_id`, `food_id`, `order_count`, `unit_price`, `name`,`taste`,`taste_price`,`taste_id`
FROM `order_food_old`;

UPDATE `order_food`,`order`
SET `order_food`.`order_date` = `order`.`order_date`
,`order_food`.`waiter` = `order`.`waiter`
WHERE `order_food`.`order_id` = `order`.`id`;

UPDATE `order_food`,`food`
SET `order_food`.`kitchen` = `food`.`kitchen`
WHERE `order_food`.`food_id` = `food`.`alias_id`;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_history` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to order' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'external key associated with the  restaurant table' ,
  `order_date` DATETIME NOT NULL COMMENT 'the order\'s date and time' ,
  `total_price` DECIMAL(10,2) NULL DEFAULT NULL COMMENT 'The total price to this order.\nIts default value is NULL, means the order not be paid, otherwise means the order has been paid.' ,
  `custom_num` TINYINT UNSIGNED NOT NULL DEFAULT 0 ,
  `waiter` VARCHAR(45) NOT NULL COMMENT 'the waiter who operates on this order' ,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type to pay order, it would be one of the values below.\n现金 : 1\n刷卡 : 2\n会员卡 : 3\n挂账 ：4\n签单：5' ,
  `member_id` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member\'s alias id' ,
  `member` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member name' ,
  `terminal_model` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the terminal model to this order' ,
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
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this order detail record' ,
  `order_id` INT UNSIGNED NOT NULL ,
  `food_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the alias id to this food' ,
  `order_count` DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT 'the count that the waiter ordered. the count can be positive or negative.' ,
  `order_date` DATETIME NOT NULL DEFAULT 19000101 ,
  `unit_price` DECIMAL(7,2) UNSIGNED NOT NULL DEFAULT 0 ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to the ordered food' ,
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
-- Drop old tables
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_food_old` ;
DROP TABLE IF EXISTS `wireless_order_db`.`order_old` ;
DROP TABLE IF EXISTS `wireless_order_db`.`terminal_old` ;
DROP TABLE IF EXISTS `wireless_order_db`.`food_old` ;


-- -----------------------------------------------------
-- View`order_food_history_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_history_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_food_history_view` AS select sum(`order_food_history`.`order_count`) AS `order_count`,max(`order_food_history`.`unit_price`) AS `unit_price`,max(`order_food_history`.`taste_price`) AS `taste_price`,max(`order_food_history`.`name`) AS `name`,max(`order_food_history`.`taste`) AS `taste`,max(`order_food_history`.`taste_id`) AS `taste_id`,max(`order_food_history`.`discount`) AS `discount`,`order_food_history`.`order_id` AS `order_id`,`order_food_history`.`food_id` AS `food_id` from `order_food_history` group by `order_food_history`.`order_id`,`order_food_history`.`food_id` having (sum(`order_food_history`.`order_count`) > 0);

-- -----------------------------------------------------
-- View`order_food_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_food_view` AS select sum(`order_food`.`order_count`) AS `order_count`,max(`order_food`.`unit_price`) AS `unit_price`,max(`order_food`.`taste_price`) AS `taste_price`,max(`order_food`.`name`) AS `name`,max(`order_food`.`taste`) AS `taste`,max(`order_food`.`taste_id`) AS `taste_id`,max(`order_food`.`discount`) AS `discount`,`order_food`.`order_id` AS `order_id`,`order_food`.`food_id` AS `food_id` from `order_food` group by `order_food`.`order_id`,`order_food`.`food_id` having (sum(`order_food`.`order_count`) > 0);

-- -----------------------------------------------------
-- View`order_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_view` AS select `a`.`id` AS `id`,`d`.`alias_id` AS `alias_id`,`a`.`order_date` AS `order_date`,format(sum((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`)),2) AS `total_price`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',(case when (`b`.`discount` < 1) then concat('(',format((`b`.`discount` * 10),1),'折',')') else '' end),'|',format((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`),2)) separator ',') AS `foods`,(`a`.`total_price` is not null) AS `is_paid`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`id` AS `table_id`,`a`.`waiter` AS `waiter`,`a`.`type` AS `type_value`,(case `a`.`type` when 1 then '现金' when 2 then '刷卡' when 3 then '会员卡' when 4 then '挂账' when 5 then '签单' end) AS `type_name` from (((((`order` `a` left join `order_food_view` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on((`b`.`food_id` = `c`.`alias_id`))) left join `table` `d` on((`a`.`table_id` = `d`.`alias_id`))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`))) group by `a`.`id`,`d`.`alias_id`,`a`.`order_date`,`a`.`custom_num`,`a`.`total_price`;

-- -----------------------------------------------------
-- View`order_history_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_history_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_history_view` AS select `a`.`id` AS `id`,`d`.`alias_id` AS `alias_id`,`a`.`order_date` AS `order_date`,format(sum((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`)),2) AS `total_price`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',(case when (`b`.`discount` < 1) then concat('(',format((`b`.`discount` * 10),1),'折',')') else '' end),'|',format((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`),2)) separator ',') AS `foods`,(`a`.`total_price` is not null) AS `is_paid`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`id` AS `table_id`,`a`.`waiter` AS `waiter`,`a`.`type` AS `type_value`,(case `a`.`type` when 1 then '现金' when 2 then '刷卡' when 3 then '会员卡' when 4 then '挂账' when 5 then '签单' end) AS `type_name` from (((((`order_history` `a` left join `order_food_history_view` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on((`b`.`food_id` = `c`.`alias_id`))) left join `table` `d` on((`a`.`table_id` = `d`.`alias_id`))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`))) group by `a`.`id`,`d`.`alias_id`,`a`.`order_date`,`a`.`custom_num`,`a`.`total_price`;

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