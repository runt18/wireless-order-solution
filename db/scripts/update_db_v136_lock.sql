SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`lock`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`lock` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`lock` (
  `operation_id` INT NOT NULL COMMENT '',
  `associated_id` INT NOT NULL COMMENT '',
  `birth_date` DATETIME NULL COMMENT '',
  PRIMARY KEY (`operation_id`, `associated_id`)  COMMENT '')
ENGINE = MEMORY
DEFAULT CHARACTER SET = utf8;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



