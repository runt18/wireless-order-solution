SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`represent`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`represent` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`represent` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `restaurant_id` INT NULL COMMENT '',
  `finish_date` DATETIME NULL COMMENT '',
  `title` VARCHAR(45) NULL DEFAULT NULL COMMENT '',
  `slogon` VARCHAR(45) NULL DEFAULT NULL COMMENT '',
  `body` VARCHAR(500) NULL COMMENT '',
  `recommend_point` INT NULL DEFAULT NULL COMMENT '',
  `recommend_money` FLOAT NULL DEFAULT NULL COMMENT '',
  `subscribe_point` INT NULL DEFAULT NULL COMMENT '',
  `subscribe_money` FLOAT NULL DEFAULT NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '',
  INDEX `ix_restaurant_id` (`restaurant_id` ASC)  COMMENT '')
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`represent_chain`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`represent_chain` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`represent_chain` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `restaurant_id` INT NULL COMMENT '',
  `subscribe_date` DATETIME NULL COMMENT '',
  `recommend_member_id` INT NULL COMMENT '',
  `recommend_member` VARCHAR(45) NULL COMMENT '',
  `recommend_point` INT NULL COMMENT '',
  `recommend_money` FLOAT NULL COMMENT '',
  `subscribe_member_id` INT NULL COMMENT '',
  `subscribe_member` VARCHAR(45) NULL COMMENT '',
  `subscribe_point` INT NULL COMMENT '',
  `subscribe_money` FLOAT NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '',
  INDEX `ix_restaurant_id` (`restaurant_id` ASC)  COMMENT '',
  INDEX `ix_recommend_member_id` (`recommend_member_id` ASC) COMMENT '',
  INDEX `ix_subscribe_member_id` (`subscribe_member_id` ASC) COMMENT ''
  )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Insert the represent to each restaurant
-- -----------------------------------------------------
INSERT INTO `wireless_order_db`.`represent`
(`restaurant_id`)
SELECT id FROM `wireless_order_db`.`restaurant` WHERE id > 10;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



