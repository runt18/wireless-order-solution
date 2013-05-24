SET NAMES utf8;
USE wireless_order_db;

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
-- Add the field 'stock_take_status' to table 'setting'
-- Add the field 'last_stock_take' to table 'setting'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`setting` 
ADD COLUMN `stock_take_status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status to stock take is as below.\n1 - 盘点完成\n2 - 盘点中'  AFTER `erase_quota` , 
ADD COLUMN `current_stock_take` INT DEFAULT NULL COMMENT 'last date to stock take'  AFTER `stock_take_status` ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`material`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`material` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`material` (
  `material_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this material' ,
  `cate_id` INT NOT NULL COMMENT 'the catagory id to this material' ,
  `price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the price to this material' ,
  `stock` FLOAT NOT NULL DEFAULT 0 COMMENT 'the stock to this material' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name to this material' ,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status to this material is as below.\n1 - 正常\n2 - 停用\n3 - 预警\n4 - 删除' ,
  `last_mod_staff` VARCHAR(45) NOT NULL ,
  `last_mod_date` DATETIME NOT NULL ,
  PRIMARY KEY (`material_id`) ,
  INDEX `ix_cate_id` (`cate_id` ASC) )
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
  `material_cate_id` INT NOT NULL ,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status to stock taking as below.\n1 - 盘点中\n2 - 盘点完成\n3 - 审核通过' ,
  `parent_id` INT NULL DEFAULT NULL ,
  `operator` VARCHAR(45) NULL ,
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
-- Table `wireless_order_db`.`stock_in`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`stock_in` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`stock_in` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `ori_stock_id` VARCHAR(45) NOT NULL ,
  `approver_id` INT NULL DEFAULT NULL ,
  `approver` VARCHAR(45) NULL DEFAULT NULL ,
  `approve_date` DATETIME NULL DEFAULT NULL ,
  `dept_in` TINYINT NOT NULL DEFAULT 0 ,
  `dept_out` TINYINT NOT NULL DEFAULT 0 ,
  `operator_id` INT NOT NULL ,
  `operator` VARCHAR(45) NOT NULL ,
  `operate_date` DATETIME NOT NULL ,
  `amount` FLOAT NOT NULL DEFAULT 0 ,
  `price` FLOAT NOT NULL DEFAULT 0 ,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type to stock in as below.\n1 - 商品入库\n2 - 商品调拨\n3 - 商品报溢\n4 - 原料入库\n5 - 原料调拨\n6 - 原料报溢' ,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status to stock in as below.\n1 - 未审核\n2 - 审核通过\n3 - 冲红' ,
  `comment` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) ,
  INDEX `ix_dept_out` (`dept_out` ASC) ,
  INDEX `ix_dept_in` (`dept_in` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'descirbe the general stock in information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`stock_in_detail`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`stock_in_detail` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`stock_in_detail` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `stock_in_id` INT NOT NULL ,
  `material_id` INT NOT NULL ,
  `price` FLOAT NOT NULL DEFAULT 0 ,
  `amount` FLOAT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_stock_in_id` (`stock_in_id` ASC) ,
  INDEX `ix_material_id` (`material_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the detail to stock in' ;