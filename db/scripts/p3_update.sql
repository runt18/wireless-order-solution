SET NAMES utf8;
USE `wireless_order_db` ;

-- -----------------------------------------------------
-- change the id of food
-- -----------------------------------------------------
ALTER   TABLE   `food`   RENAME   TO   `old_food`;
ALTER TABLE `old_food` DROP FOREIGN KEY `fk_food_restaurant`;

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

INSERT INTO `food`(`alias_id`,`restaurant_id`, `name`, `unit_price`, `kitchen`, `status`, `img1`, `img2`, `img3`, `enabled`) 
SELECT `alias_id`,`restaurant_id`, `name`, `unit_price`, `kitchen`, `status`, `img1`, `img2`, `img3`, `enabled` FROM `old_food`;

DROP TABLE `old_food`;
-- ----------------------------------------------------------------------------------------------

-- -----------------------------------------------------
-- change the id of table
-- -----------------------------------------------------
ALTER   TABLE   `table`   RENAME   TO   `old_table`;
ALTER TABLE `old_table` DROP FOREIGN KEY `fk_table_restaurant1`;

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

INSERT INTO `table`(`alias_id`,`restaurant_id`, `enabled`) 
SELECT `alias_id`,`restaurant_id`, `enabled` FROM `old_table`;

DROP TABLE `old_table`;
-- ----------------------------------------------------------------------------------------------

-- Delete all the order record of root
DELETE FROM `order_food` WHERE order_id IN (SELECT id FROM `order` WHERE restaurant_id=1);
DELETE FROM `order` WHERE restaurant_id=1;
-- Insert a maxium order and order_food id record
SELECT @max_order_id:=(MAX(`id`) + 1) FROM (SELECT id FROM `order` UNION SELECT id FROM `order_history`) AS all_order;
INSERT INTO `order` (`id`, `restaurant_id`) VALUES(@max_order_id, 1);
SELECT @max_order_food_id:=(MAX(`id`) + 1) FROM (SELECT id FROM `order_food` UNION SELECT id FROM `order_food_history`) AS all_order;
INSERT INTO `order_food` (`id`, `order_id`) VALUES(@max_order_food_id, @max_order_id);

DELETE FROM `food` WHERE enabled=0;
DELETE FROM `table` WHERE enabled=0;

ALTER TABLE `restaurant`
ADD `tele1` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'One of the telephones to this restaurant.'; 
ALTER TABLE `restaurant`
ADD `tele2` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'One of the telephones to this restaurant.';
ALTER TABLE `restaurant`
ADD `address` VARCHAR(70) NOT NULL DEFAULT '' COMMENT 'The address to this restaurant.';
ALTER TABLE `restaurant`
ADD `pwd2` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the 2nd password to this restaurant, used to grant permission to change the order';

ALTER TABLE `order`
ADD `category` TINYINT NOT NULL DEFAULT 1 COMMENT 'the category to this order, it should be one the values below.\n一般 : 1\n外卖 : 2\n并台 : 3\n拼台 : 4' ;
ALTER TABLE `order`
ADD `comment` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the comment to this order';
ALTER TABLE `order`
ADD `table_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the table name to this order';
ALTER TABLE `order`
ADD `table2_id` SMALLINT NULL DEFAULT NULL COMMENT 'the 2nd table alias id to this order(used for table merger)' ;
ALTER TABLE `order`
ADD `table2_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the 2nd table name to this order(used for table merger)' ;
ALTER TABLE `order`
MODIFY `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the waiter who operates on this order';
ALTER TABLE `order`
MODIFY `custom_num` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the amount of custom to this order';

ALTER TABLE `order_history`
ADD `category` TINYINT NOT NULL DEFAULT 1 COMMENT 'the category to this order, it should be one the values below.\n一般 : 1\n外卖 : 2\n并台 : 3\n拼台 : 4' ;
ALTER TABLE `order_history`
ADD `comment` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the comment to this order';
ALTER TABLE `order_history`
ADD `table_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the table name to this order';
ALTER TABLE `order_history`
ADD `table2_id` SMALLINT NULL DEFAULT NULL COMMENT 'the 2nd table alias id to this order(used for table merger)' ;
ALTER TABLE `order_history`
ADD `table2_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the 2nd table name to this order(used for table merger)' ;
ALTER TABLE `order`
MODIFY `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the waiter who operates on this order';
ALTER TABLE `order`
MODIFY `custom_num` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the amount of custom to this order';

ALTER TABLE `kitchen`
ADD `discount_3` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 3rd discount to the food belong to this kitchen, range from 0.00 to 1.00';
ALTER TABLE `kitchen`
ADD `member_discount_3` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 3rd member discount to the food belong to this kitchen, range from 0.00 to 1.00';

ALTER TABLE `terminal`
MODIFY COLUMN `model_id` SMALLINT NOT NULL DEFAULT 0 COMMENT 'the model to this terminal.\nBlackBerry : 0x00\nAndroid : 0x01\nStaff : 0xFF';

ALTER TABLE `terminal`
MODIFY COLUMN `expire_date` DATE NULL DEFAULT NULL COMMENT 'the expired date to the terminal,\nNULL means never expired,';

ALTER TABLE `restaurant`
DROP COLUMN `total_income`;

ALTER TABLE `order_food`
ADD `food_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates the status to this food, the value is the combination of values below.\n特价菜 ：0x01\n推荐菜 ：0x02\n停售　 ：0x04';

ALTER TABLE `order_food_history`
ADD `food_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates the status to this food, the value is the combination of values below.\n特价菜 ：0x01\n推荐菜 ：0x02\n停售　 ：0x04';

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


