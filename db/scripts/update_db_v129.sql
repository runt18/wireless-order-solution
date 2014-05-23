SET @OLD_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET NAMES utf8;
SET SQL_SAFE_UPDATES = 0;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`payment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`payment` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`payment` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `staff_id` INT NOT NULL,
  `staff_name` VARCHAR(45) NULL DEFAULT NULL,
  `on_duty` DATETIME NOT NULL,
  `off_duty` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC),
  INDEX `ix_staff_id` (`staff_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`payment_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`payment_history` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`payment_history` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `staff_id` INT NOT NULL,
  `staff_name` VARCHAR(45) NULL DEFAULT NULL,
  `on_duty` DATETIME NOT NULL,
  `off_duty` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC),
  INDEX `ix_staff_id` (`staff_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



