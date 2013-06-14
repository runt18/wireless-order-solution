SET NAMES utf8;
USE wireless_order_db;

DELETE FROM mysql.user WHERE user='digie';
-- -----------------------------------------------------
-- create an account digie at localhost and set its password
-- the digie account is applied to the digie of wireless order db
-- it has all the privileges on the db.
-- -----------------------------------------------------
GRANT ALL ON wireless_order_db.* to digie@`localhost`;
SET PASSWORD FOR digie@localhost=PASSWORD('HelloZ315');

-- -----------------------------------------------------
-- View`restaurant_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS restaurant_view;

CREATE VIEW `restaurant_view` AS select `r`.`id` AS `id`,`r`.`account` AS `account`,`r`.`restaurant_name` AS `restaurant_name`,`r`.`tele1` AS `tele1`,`r`.`tele2` AS `tele2`,`r`.`address` AS `address`,`r`.`restaurant_info` AS `restaurant_info`,`r`.`record_alive` AS `record_alive`,(select count(`order`.`id`) from `order` where (`order`.`restaurant_id` = `r`.`id`)) AS `order_num`,(select count(`order_history`.`id`) from `order_history` where (`order_history`.`restaurant_id` = `r`.`id`)) AS `order_history_num`,(select count(`terminal`.`pin`) from `terminal` where ((`terminal`.`restaurant_id` = `r`.`id`) and (`terminal`.`model_id` <= 0x7f))) AS `terminal_num`,(select count(`terminal`.`pin`) from `terminal` where ((`terminal`.`restaurant_id` = `r`.`id`) and (`terminal`.`model_id` > 0x7f))) AS `terminal_virtual_num`,(select count(`food`.`food_id`) from `food` where (`food`.`restaurant_id` = `r`.`id`)) AS `food_num`,(select count(`table`.`table_id`) from `table` where (`table`.`restaurant_id` = `r`.`id`)) AS `table_num`,(select count(`order`.`id`) from `order` where ((`order`.`restaurant_id` = `r`.`id`) and (`order`.`total_price` is not null))) AS `order_paid`,(select count(`order_history`.`id`) from `order_history` where ((`order_history`.`restaurant_id` = `r`.`id`) and (`order_history`.`total_price` is not null))) AS `order_history_paid`,(select count(`table`.`table_id`) from `table` where ((`table`.`restaurant_id` = `r`.`id`) and exists(select 1 from `order` where ((`order`.`table_alias` = `table`.`table_alias`) and isnull(`order`.`total_price`) and (`order`.`restaurant_id` = `r`.`id`))))) AS `table_using` from `restaurant` `r`;

-- -----------------------------------------------------
-- View`terminal_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS terminal_view;
CREATE VIEW `terminal_view` AS select `t`.`pin` AS `pin`,`t`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`t`.`model_name` AS `model_name`,`t`.`model_id` AS `model_id`,(case `t`.`model_id` when 0 then 'BlackBerry' when 1 then 'Android' when 2 then 'iPhone' when 3 then 'WindowsMobile' end) AS `model_id_name`,`t`.`entry_date` AS `entry_date`,`t`.`discard_date` AS `discard_date`,format((((`t`.`idle_duration` / 3600) / 24) / 30),1) AS `idle_month`,format((((`t`.`work_duration` / 3600) / 24) / 30),1) AS `work_month`,`t`.`expire_date` AS `expire_date`,(case when (`t`.`restaurant_id` = 2) then '空闲' when (`t`.`restaurant_id` = 3) then '废弃' when ((`t`.`restaurant_id` > 10) and (now() <= `t`.`expire_date`)) then '使用' when ((`t`.`restaurant_id` > 10) and (now() > `t`.`expire_date`)) then '过期' end) AS `status`,format(((`t`.`work_duration` / (`t`.`work_duration` + `t`.`idle_duration`)) * 100),0) AS `use_rate`,`t`.`owner_name` AS `owner_name`,`t`.`idle_duration` AS `idle_duration`,`t`.`work_duration` AS `work_duration` from (`terminal` `t` left join `restaurant` `r` on((`t`.`restaurant_id` = `r`.`id`)));


DROP TABLE IF EXISTS `wireless_order_db`.`material`;
DROP TABLE IF EXISTS `wireless_order_db`.`material_cate`;
DROP TABLE IF EXISTS `wireless_order_db`.`material_dept`;
DROP TABLE IF EXISTS `wireless_order_db`.`material_detail`;
DROP TABLE IF EXISTS `wireless_order_db`.`supplier`;

-- -----------------------------------------------------
-- Add the field 'stock_status' to table 'food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`food` 
ADD COLUMN `stock_status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the stock status is as below.\n1 - 无管理\n2 - 商品管理\n3 - 原料管理'  AFTER `status` ;

-- -----------------------------------------------------
-- Add the field 'current_material_month' to table 'setting'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`setting` 
ADD COLUMN `current_material_month` INT UNSIGNED NULL DEFAULT NULL COMMENT '当前会计月份'  AFTER `erase_quota` ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`material`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`material` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`material` (
  `material_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this material' ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 ,
  `cate_id` INT NOT NULL COMMENT 'the catagory id to this material' ,
  `price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the price to this material' ,
  `stock` FLOAT NOT NULL DEFAULT 0 COMMENT 'the stock to this material' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name to this material' ,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status to this material is as below.\n1 - 正常\n2 - 停用\n3 - 预警\n4 - 删除' ,
  `last_mod_staff` VARCHAR(45) NOT NULL ,
  `last_mod_date` DATETIME NOT NULL ,
  PRIMARY KEY (`material_id`) ,
  INDEX `ix_cate_id` (`cate_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the material information.' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`material_cate`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`material_cate` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`material_cate` (
  `cate_id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `name` VARCHAR(45) NOT NULL ,
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the value to category of this material as below.\n1 - 商品\n2 - 原料' ,
  PRIMARY KEY (`cate_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the category of material' ;

-- -----------------------------------------------------
-- Insert the "商品" category to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.material_cate
(`restaurant_id`, `name`, `type`)
SELECT id, '商品', 1 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`food_material`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_material` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food_material` (
  `food_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the food id' ,
  `material_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the material id' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `consumption` FLOAT NOT NULL DEFAULT 0 COMMENT 'the consumption between the food and the material' ,
  INDEX `id_material_id` (`material_id` ASC) ,
  PRIMARY KEY (`food_id`, `material_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the releation ship between food and material' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`supplier`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`supplier` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`supplier` (
  `supplier_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this supplier' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name to ths supplier' ,
  `tele` VARCHAR(45) NOT NULL COMMENT 'the telephone to this supplier' ,
  `addr` VARCHAR(100) NOT NULL COMMENT 'the address to this supplier' ,
  `contact` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the contact person to this supplier' ,
  `comment` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`supplier_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`stock_take`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`stock_take` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`stock_take` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'the restaurant id that this material detial belongs to' ,
  `dept_id` TINYINT NOT NULL COMMENT 'the supplier id that this material detail record belong to' ,
  `dept_name` VARCHAR(45) NULL DEFAULT NULL ,
  `material_cate_id` INT NOT NULL ,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status to stock taking as below.\n1 - 盘点中\n2 - 盘点完成\n3 - 审核通过' ,
  `parent_id` INT NULL DEFAULT NULL ,
  `operator` VARCHAR(45) NULL ,
  `operator_id` INT NULL ,
  `approver_id` INT NULL ,
  `approver` VARCHAR(45) NULL ,
  `start_date` DATETIME NULL ,
  `finish_date` DATETIME NULL ,
  `comment` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) ,
  INDEX `ix_dept_id` (`dept_id` ASC) ,
  INDEX `ix_material_id` (`material_cate_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the material detail to history' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`material_dept`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`material_dept` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`material_dept` (
  `material_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the id to material ' ,
  `dept_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the id to department' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `stock` FLOAT NOT NULL DEFAULT 0 COMMENT 'the stock to this material in department' ,
  PRIMARY KEY (`material_id`, `dept_id`) ,
  INDEX `ix_dept_id` (`dept_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the stock to each material of department' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`stock_take_detail`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`stock_take_detail` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`stock_take_detail` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `stock_take_id` INT NOT NULL DEFAULT 0 ,
  `material_id` INT NOT NULL ,
  `actual_amount` FLOAT NULL COMMENT '盘点后的实际数量' ,
  `expect_amount` FLOAT NULL COMMENT '盘点前的期望数量' ,
  `delta_amount` FLOAT NULL COMMENT '盘点前后的差额' ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_material_id` (`material_id` ASC) ,
  INDEX `ix_stock_take_id` (`stock_take_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the detail to stock taking' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`stock_action`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`stock_action` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`stock_action` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `cate_type` TINYINT NOT NULL DEFAULT 1 COMMENT '1 - 商品\n2 - 原料' ,
  `birth_date` DATETIME NULL DEFAULT NULL ,
  `ori_stock_id` VARCHAR(45) NULL DEFAULT NULL ,
  `ori_stock_date` DATETIME NULL DEFAULT NULL ,
  `approver_id` INT NULL DEFAULT NULL ,
  `approver` VARCHAR(45) NULL DEFAULT NULL ,
  `approve_date` DATETIME NULL DEFAULT NULL ,
  `dept_in` TINYINT UNSIGNED NOT NULL DEFAULT 0 ,
  `dept_in_name` VARCHAR(45) NULL DEFAULT NULL ,
  `dept_out` TINYINT NOT NULL DEFAULT 0 ,
  `dept_out_name` VARCHAR(45) NULL DEFAULT NULL ,
  `supplier_id` INT NOT NULL DEFAULT 0 ,
  `supplier_name` VARCHAR(45) NULL DEFAULT NULL ,
  `operator_id` INT NOT NULL ,
  `operator` VARCHAR(45) NOT NULL ,
  `amount` FLOAT NOT NULL DEFAULT 0 ,
  `price` FLOAT NOT NULL DEFAULT 0 ,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type to stock in as below.\n1 - 入库单\n2 - 出库单' ,
  `sub_type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the sub type to stock in as below.\n1 - 采购 \n2 - 入库调拨 \n3 - 报溢 \n4 - 退货 \n5 - 出库调拨 \n6 - 报损 \n7 - 盘盈 \n8 - 盘亏 \n9 - 消耗' ,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status to stock in as below.\n1 - 未审核\n2 - 审核通过\n3 - 冲红' ,
  `comment` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) ,
  INDEX `ix_dept_out` (`dept_out` ASC) ,
  INDEX `ix_dept_in` (`dept_in` ASC) ,
  INDEX `ix_supplier_id` (`supplier_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'descirbe the general stock action information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`stock_action_detail`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`stock_action_detail` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`stock_action_detail` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `stock_action_id` INT NOT NULL ,
  `material_id` INT NOT NULL ,
  `name` VARCHAR(45) NOT NULL ,
  `price` FLOAT NOT NULL DEFAULT 0 ,
  `amount` FLOAT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_stock_in_id` (`stock_action_id` ASC) ,
  INDEX `ix_material_id` (`material_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the detail to stock action' ;