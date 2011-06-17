SET NAMES utf8;
USE `wireless_order_db` ;

ALTER   TABLE   `table`   RENAME   TO   `old_table`;
ALTER TABLE `old_table` DROP FOREIGN KEY `fk_table_restaurant1`;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`table`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`table` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`table` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'in the form of \"restaurant.id << 32 | table.alias_id\"' ,
  `alias_id` SMALLINT UNSIGNED NULL ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'Indicates the table belongs to which restaurant.' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to this table' ,
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT 'indicates whether the table information is enabled or not' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_table_restaurant1` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_table_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'describe the restaurant\'s table info';

INSERT INTO `table`(`alias_id`,`restaurant_id`,`name`,`enabled`) 
SELECT `alias_id`,`restaurant_id`,`name`,`enabled` FROM `old_table`;

DROP TABLE `old_table`;