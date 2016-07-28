SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`promotion_trigger`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`promotion_trigger` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`promotion_trigger` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `restaurant_id` INT NULL COMMENT '',
  `promotion_id` INT NULL COMMENT '',
  `type` INT NULL COMMENT 'the trigger type as below\n1 - 发券\n2 - 用券',
  `rule` INT NULL COMMENT '',
  `extra` INT NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '',
  INDEX `ix_promotion_id` (`promotion_id` ASC)  COMMENT '',
  INDEX `ix_restaurant_id` (`restaurant_id` ASC)  COMMENT '')
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



