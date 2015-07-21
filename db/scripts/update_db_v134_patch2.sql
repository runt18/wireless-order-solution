SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`book`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`book` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`book` (
  `book_id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NULL,
  `book_date` DATETIME NULL DEFAULT NULL,
  `book_reserved` INT NULL DEFAULT NULL,
  `book_region` VARCHAR(45) NULL DEFAULT NULL,
  `book_member` VARCHAR(45) NULL DEFAULT NULL,
  `book_member_id` INT NULL DEFAULT NULL,
  `book_tele` VARCHAR(45) NULL DEFAULT NULL,
  `book_amount` INT NULL DEFAULT 0,
  `book_confirm_date` DATETIME NULL DEFAULT NULL,
  `book_staff` VARCHAR(45) NULL DEFAULT NULL,
  `book_staff_id` INT NULL DEFAULT NULL,
  `book_cate` VARCHAR(45) NULL DEFAULT NULL,
  `book_source` TINYINT NOT NULL DEFAULT 1 COMMENT 'the book source as below.\n1 - weixin\n2 - manual',
  `book_status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the book status as below\n1 - 未确认\n2 - 已确认\n3 - 已完成\n4 - 已失效',
  `book_money` FLOAT NULL DEFAULT 0,
  `comment` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`book_id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC),
  INDEX `ix_book_member_id` (`book_member_id` ASC),
  INDEX `ix_book_staff_id` (`book_staff_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`book_table`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`book_table` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`book_table` (
  `book_id` INT NOT NULL,
  `table_id` INT NOT NULL,
  `table_name` VARCHAR(45) NULL,
  PRIMARY KEY (`book_id`, `table_id`),
  INDEX `ix_table_id` (`table_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`book_order`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`book_order` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`book_order` (
  `book_id` INT NOT NULL,
  `food_id` INT NOT NULL,
  `food_unit_id` INT NULL DEFAULT NULL,
  `amount` FLOAT NULL DEFAULT 0,
  `tmp_taste` VARCHAR(45) NULL DEFAULT NULL,
  `tmp_taste_price` FLOAT NULL DEFAULT NULL,
  PRIMARY KEY (`book_id`, `food_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`book_order_taste`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`book_order_taste` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`book_order_taste` (
  `book_id` INT NOT NULL,
  `food_id` INT NOT NULL,
  `taste_id` INT NOT NULL,
  PRIMARY KEY (`book_id`, `food_id`, `taste_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`weixin_menu_action`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`weixin_menu_action` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`weixin_menu_action` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NULL,
  `type` TINYINT NULL,
  `action` TEXT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`stock_init`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`stock_init` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`stock_init` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NULL,
  `init_date` DATETIME NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



