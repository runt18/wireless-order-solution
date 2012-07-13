SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Update the dept_id to 253 for the temporary record
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food SET dept_id = 253 WHERE is_temporary = 1;
UPDATE wireless_order_db.order_food_history SET dept_id = 253 WHERE is_temporary = 1;

-- -----------------------------------------------------
-- Update the dept_id to 253 for the temporary record
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`material_cate` DROP FOREIGN KEY `fk_material_cate_restaurant1` ;
ALTER TABLE `wireless_order_db`.`material_cate` 
DROP INDEX `fk_material_cate_restaurant1` ;

-- -----------------------------------------------------
-- Append the material 11 - 50 
-- -----------------------------------------------------
INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 10, '种类11' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 11, '种类12' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 12, '种类13' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 13, '种类14' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 14, '种类15' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 15, '种类16' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 16, '种类17' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 17, '种类18' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 18, '种类19' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 19, '种类20' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 20, '种类21' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 21, '种类22' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 22, '种类23' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 23, '种类24' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 24, '种类25' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 25, '种类26' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 26, '种类27' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 27, '种类28' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 28, '种类29' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 29, '种类30' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 30, '种类31' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 31, '种类32' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 32, '种类33' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 33, '种类34' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 34, '种类35' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 35, '种类36' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 36, '种类37' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 37, '种类38' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 38, '种类39' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 39, '种类40' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 40, '种类41' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 41, '种类42' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 42, '种类43' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 43, '种类44' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 44, '种类45' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 45, '种类46' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 46, '种类47' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 47, '种类48' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 48, '种类49' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.material_cate (`restaurant_id`, `cate_id`, `name`)
SELECT id, 49, '种类50' FROM wireless_order_db.restaurant WHERE id > 10;