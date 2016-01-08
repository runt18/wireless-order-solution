SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`weixin_keyword`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`weixin_keyword` ;

CREATE TABLE `wireless_order_db`.`weixin_keyword` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `restaurant_id` INT NULL COMMENT '',
  `keyword` VARCHAR(45) NULL COMMENT '',
  `action_id` INT NULL COMMENT '',
  `type` TINYINT NULL DEFAULT 1 COMMENT 'the type as below\n1 - normal\n2 - exception',
  PRIMARY KEY (`id`)  COMMENT '',
  INDEX `ix_restaurant_id` (`restaurant_id` ASC)  COMMENT '')
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Insert the '例外回复' to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.weixin_keyword
(restaurant_id, type)
SELECT id, 2 FROM wireless_order_db.restaurant WHERE id > 10

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



