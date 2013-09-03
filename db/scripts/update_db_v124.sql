SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`printer`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`printer` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`printer` (
  `printer_id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT NOT NULL ,
  `name` VARCHAR(45) NOT NULL ,
  `alias` VARCHAR(45) NULL DEFAULT NULL ,
  `style` TINYINT NOT NULL DEFAULT 1 COMMENT 'the style as below.\n1 - 58mm\n2 - 80mm' ,
  `enabled` TINYINT NOT NULL DEFAULT 1 ,
  PRIMARY KEY (`printer_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the printer information to each restaruant' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`print_func`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`print_func` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`print_func` (
  `func_id` INT NOT NULL AUTO_INCREMENT ,
  `printer_id` INT NOT NULL ,
  `repeat` INT NOT NULL DEFAULT 1 ,
  `type` TINYINT NOT NULL COMMENT 'the type as below.\n1 - 下单\n2 - 下单详细\n3 - 退菜\n4 - 退菜详细\n5 - 暂结\n6 - 结帐\n7 - 转台\n8 - 催菜' ,
  PRIMARY KEY (`func_id`) ,
  INDEX `ix_printer_id` (`printer_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the print function ' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`func_dept`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`func_dept` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`func_dept` (
  `func_id` INT NOT NULL ,
  `dept_id` INT NOT NULL ,
  `restaurant_id` INT NOT NULL ,
  PRIMARY KEY (`func_id`, `dept_id`, `restaurant_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the relation between function and department' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`func_kitchen`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`func_kitchen` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`func_kitchen` (
  `func_id` INT NOT NULL ,
  `kitchen_alias` INT NOT NULL ,
  `restaurant_id` INT NOT NULL ,
  PRIMARY KEY (`func_id`, `kitchen_alias`, `restaurant_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the relation between function and kitchen' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`func_region`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`func_region` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`func_region` (
  `func_id` INT NOT NULL ,
  `region_id` INT NOT NULL ,
  `restaurant_id` INT NOT NULL ,
  PRIMARY KEY (`func_id`, `region_id`, `restaurant_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the relationship between function and region' ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`role`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`role` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`role` (
  `role_id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT NOT NULL DEFAULT 0 ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' ,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type to role as below.\n1 - 普通\n2 - 系统保留' ,
  `cate` TINYINT NOT NULL DEFAULT 6 COMMENT 'the category to role as below.\n1 - 管理员\n2 - 老板\n3 - 财务\n4 - 店长\n5 - 收银员\n6 - 服务员\n7 - 其他\n' ,
  PRIMARY KEY (`role_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the role information' ;

-- -----------------------------------------------------
-- Insert a '管理员' role to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role
(`restaurant_id`, `name`, `type`, `cate`)
SELECT id, '管理员', 2, 1 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Insert a '老板' role to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role
(`restaurant_id`, `name`, `type`, `cate`)
SELECT id, '老板', 2, 2 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Insert a '财务' role to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role
(`restaurant_id`, `name`, `type`, `cate`)
SELECT id, '财务', 1, 3 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Insert a '店长' role to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role
(`restaurant_id`, `name`, `type`, `cate`)
SELECT id, '店长', 1, 4 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Insert a '收银员' role to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role
(`restaurant_id`, `name`, `type`, `cate`)
SELECT id, '收银员', 1, 5 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Insert a '服务员' role to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role
(`restaurant_id`, `name`, `type`, `cate`)
SELECT id, '服务员', 1, 6 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`staff`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`staff2` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`staff2` (
  `staff_id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT NOT NULL DEFAULT 0 ,
  `role_id` INT NOT NULL DEFAULT 0 ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' ,
  `tele` VARCHAR(45) NULL DEFAULT NULL ,
  `pwd` VARCHAR(45) NOT NULL DEFAULT '' ,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type to staff as below.\n1 - 普通\n2 - 系统保留' ,
  PRIMARY KEY (`staff_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) ,
  INDEX `ix_role_id` (`role_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the staff information' ;

-- -----------------------------------------------------
-- Insert a '管理员' staff to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.staff2
(`restaurant_id`, `role_id`, `name`, `pwd`, type)
SELECT REST.id, ROLE.role_id, '管理员', REST.pwd, 2 
FROM wireless_order_db.restaurant REST 
JOIN wireless_order_db.role ROLE ON REST.id = ROLE.restaurant_id AND ROLE.cate = 1
WHERE REST.id > 10;

-- -----------------------------------------------------
-- Move the staff information and have them assigned a '店长' role
-- -----------------------------------------------------
INSERT INTO wireless_order_db.staff2
(`restaurant_id`, `role_id`, `name`, `pwd`, type)
SELECT STAFF.restaurant_id, ROLE.role_id, STAFF.name, STAFF.pwd, 1
FROM wireless_order_db.staff STAFF
LEFT JOIN wireless_order_db.role ROLE ON STAFF.restaurant_id = ROLE.restaurant_id AND ROLE.cate = 4;

-- -----------------------------------------------------
-- Add a field 'expire_date' to table 'restaurant'
-- Update expire date to each restaurant
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`restaurant` ADD COLUMN `expire_date` DATETIME NULL DEFAULT NULL  AFTER `pwd5` ;

UPDATE wireless_order_db.restaurant REST,
(SELECT restaurant_id, MAX(expire_date) AS expire_date FROM `wireless_order_db`.`terminal` GROUP BY restaurant_id) AS TMP
SET REST.expire_date = TMP.expire_date
WHERE REST.id = TMP.restaurant_id;

-- -----------------------------------------------------
-- Drop the table 'staff' and 'terminal'
-- Rename the 'staff2' to 'staff'
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`staff` ;
-- DROP TABLE IF EXISTS `wireless_order_db`.`terminal` ;
ALTER TABLE `wireless_order_db`.`staff2` RENAME TO  `wireless_order_db`.`staff` ;

-- -----------------------------------------------------
-- Drop the field 'terminal_pin' and 'terminal_model' in table 'order'
-- Add the field 'staff_id' to table 'order'
-- Add the field 'staff_id' to table 'order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` DROP COLUMN `terminal_pin` , DROP COLUMN `terminal_model` ;
ALTER TABLE `wireless_order_db`.`order` ADD COLUMN `staff_id` INT NOT NULL DEFAULT 0  AFTER `waiter` ;
ALTER TABLE `wireless_order_db`.`order_food` ADD COLUMN `staff_id` INT NOT NULL DEFAULT 0  AFTER `waiter` ;

-- -----------------------------------------------------
-- Drop the field 'terminal_pin' and 'terminal_model' in table 'order_history'
-- Add the field 'staff_id' to table 'order_history'
-- Add the field 'staff_id' to table 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` DROP COLUMN `terminal_pin` , DROP COLUMN `terminal_model` ;
ALTER TABLE `wireless_order_db`.`order_history` ADD COLUMN `staff_id` INT NOT NULL DEFAULT 0  AFTER `waiter` ;
ALTER TABLE `wireless_order_db`.`order_food_history` ADD COLUMN `staff_id` INT NOT NULL DEFAULT 0  AFTER `waiter` ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`device`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`device` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`device` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT NOT NULL ,
  `device_id` VARCHAR(45) NOT NULL ,
  `device_id_crc` INT UNSIGNED NOT NULL ,
  `model_id` TINYINT NOT NULL DEFAULT 1 COMMENT 'the model id as below.\n1 - Android\n2 - iOS\n3 - WP' ,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status as below.\n1 - 停用\n2 - 启用' ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) ,
  INDEX `ix_device_id_crc` (`device_id_crc` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`privilege`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`privilege` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`privilege` (
  `pri_id` INT NOT NULL AUTO_INCREMENT ,
  `pri_code` INT NOT NULL COMMENT 'the privilege code as below:\n1000 - 点菜\n1001 - 退菜\n1002 - 打折\n1003 - 赠送\n1004 - 反结帐\n1005 - 结帐\n1006 - 账单\n2000 - 后台\n3000 - 库存\n4000 - 历史\n5000 - 会员\n6000 - 系统' ,
  `cate` TINYINT NOT NULL COMMENT 'the category to privilege as below.\n1 - 前台\n2 - 后台\n3 - 库存\n4 - 历史\n5 - 会员\n6 - 系统' ,
  PRIMARY KEY (`pri_id`) ,
  INDEX `ix_privilege_code` (`pri_code` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the privilege information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`role_privilege`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`role_privilege` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`role_privilege` (
  `role_id` INT NOT NULL ,
  `pri_id` INT NOT NULL ,
  `restaurant_id` INT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`role_id`, `pri_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`role_discount`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`role_discount` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`role_discount` (
  `role_id` INT NOT NULL ,
  `discount_id` INT NOT NULL ,
  PRIMARY KEY (`role_id`, `discount_id`) )
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Insert a '退菜' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege (`pri_code`, `cate`) VALUES (1000, 1);

-- -----------------------------------------------------
-- Insert a '退菜' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege (`pri_code`, `cate`) VALUES (1001, 1);

-- -----------------------------------------------------
-- Insert a '打折' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege (`pri_code`, `cate`) VALUES (1002, 1);

-- -----------------------------------------------------
-- Insert a '赠送' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege (`pri_code`, `cate`) VALUES (1003, 1);

-- -----------------------------------------------------
-- Insert a '反结帐' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege (`pri_code`, `cate`) VALUES (1004, 1);

-- -----------------------------------------------------
-- Insert a '结帐' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege (`pri_code`, `cate`) VALUES (1005, 1);

-- -----------------------------------------------------
-- Insert a '账单' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege (`pri_code`, `cate`) VALUES (1006, 1);


-- -----------------------------------------------------
-- Insert a '后台' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege (`pri_code`, `cate`) VALUES (2000, 2);

-- -----------------------------------------------------
-- Insert a '库存' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege (`pri_code`, `cate`) VALUES (3000, 3);

-- -----------------------------------------------------
-- Insert a '历史' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege (`pri_code`, `cate`) VALUES (4000, 4);

-- -----------------------------------------------------
-- Insert a '会员' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege (`pri_code`, `cate`) VALUES (5000, 5);

-- -----------------------------------------------------
-- Insert a '系统' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege (`pri_code`, `cate`) VALUES (6000, 6);

-- -----------------------------------------------------
-- All all privileges to '管理员'
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role_privilege 
(`role_id`, `pri_id`, `restaurant_id`)
SELECT role_id, pri_id, restaurant_id
FROM wireless_order_db.role R JOIN wireless_order_db.privilege P ON R.cate = 1;

-- -----------------------------------------------------
-- Add all privileges to '老板'
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role_privilege 
(`role_id`, `pri_id`, `restaurant_id`)
SELECT role_id, pri_id, restaurant_id
FROM wireless_order_db.role R JOIN wireless_order_db.privilege P ON R.cate = 2;

-- -----------------------------------------------------
-- Add '前台'、'后台'、'库存'、'历史'、'会员' privileges to '财务'
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role_privilege 
(`role_id`, `pri_id`, `restaurant_id`)
SELECT role_id, pri_id, restaurant_id
FROM wireless_order_db.role R JOIN wireless_order_db.privilege P ON R.cate = 3 AND pri_code IN(1000, 1001, 1002, 1003, 1004, 1005, 1006, 2000, 3000, 4000, 5000);

-- -----------------------------------------------------
-- Add '前台'、'后台' privileges to '店长'
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role_privilege 
(`role_id`, `pri_id`, `restaurant_id`)
SELECT role_id, pri_id, restaurant_id
FROM wireless_order_db.role R JOIN wireless_order_db.privilege P ON R.cate = 4 AND pri_code IN(1000, 1001, 1002, 1003, 1004, 1005, 1006, 2000);

-- -----------------------------------------------------
-- Add '前台'privileges to '收银员'
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role_privilege 
(`role_id`, `pri_id`, `restaurant_id`)
SELECT role_id, pri_id, restaurant_id
FROM wireless_order_db.role R JOIN wireless_order_db.privilege P ON R.cate = 4 AND pri_code IN(1000, 1001, 1002, 1003, 1004, 1005, 1006);

-- -----------------------------------------------------
-- Add '前台' privileges to '服务员'
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role_privilege 
(`role_id`, `pri_id`, `restaurant_id`)
SELECT role_id, pri_id, restaurant_id
FROM wireless_order_db.role R JOIN wireless_order_db.privilege P ON R.cate = 5 AND pri_code IN(1000, 1001);

-- -----------------------------------------------------
-- Drop the field 'pwd', 'pwd2', 'pwd3', 'pwd4', 'pwd5' to table 'restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`restaurant` DROP COLUMN `pwd5` , DROP COLUMN `pwd4` , DROP COLUMN `pwd3` , DROP COLUMN `pwd2` , DROP COLUMN `pwd` ;

-- -----------------------------------------------------
-- Drop the field 'taste_alias' and index 'ix_taste_alias_id'
-- Add the index 'ix_restaurant_id'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`taste` DROP COLUMN `taste_alias` 
, DROP INDEX `ix_taste_alias_id` 
, ADD INDEX `ix_restaurant_id` (`restaurant_id` ASC) ;

-- -----------------------------------------------------
-- Add the field 'birth_date' and 'liveness' to table 'restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`restaurant` 
ADD COLUMN `birth_date` DATE NULL DEFAULT NULL  AFTER `account` , 
ADD COLUMN `liveness` FLOAT NOT NULL DEFAULT 0  AFTER `expire_date` ;

UPDATE `wireless_order_db`.`restaurant` SET birth_date = '2013-01-01 00:00:00';
