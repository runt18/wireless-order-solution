SET NAMES utf8;
USE wireless_order_db;

- -----------------------------------------------------
-- Table `wireless_order_db`.`printer`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`printer` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`printer` (
  `printer_id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT NOT NULL ,
  `name` VARCHAR(45) NOT NULL ,
  `alias` VARCHAR(45) NULL DEFAULT NULL ,
  `style` TINYINT NOT NULL DEFAULT 1 COMMENT 'the style as below.\n1 - 58mm\n2 - 80mm' ,
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