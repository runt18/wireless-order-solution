DELETE FROM wireless_order_db.order_food_material_history WHERE order_food_id IN (SELECT id FROM wireless_order_db.order_food_history WHERE order_id IN (SELECT id FROM wireless_order_db.order_history WHERE restaurant_id=11) );
DELETE FROM wireless_order_db.order_food_history WHERE order_id IN (SELECT id FROM wireless_order_db.order_history WHERE restaurant_id=11);
DELETE FROM wireless_order_db.temp_order_food_history WHERE order_id IN (SELECT id FROM wireless_order_db.order_history WHERE restaurant_id=11);
DELETE FROM wireless_order_db.order_history WHERE restaurant_id=11;

DELETE FROM wireless_order_db.order_food_material WHERE order_food_id IN (SELECT id FROM wireless_order_db.order_food WHERE order_id IN (SELECT id FROM wireless_order_db.order WHERE restaurant_id=11) );
DELETE FROM wireless_order_db.order_food WHERE order_id IN (SELECT id FROM wireless_order_db.order WHERE restaurant_id=11);
DELETE FROM wireless_order_db.order WHERE restaurant_id=11;

DELETE FROM wireless_order_db.staff WHERE restaurant_id=11;

DELETE FROM wireless_order_db.food_material WHERE food_id IN (SELECT id FROM wireless_order_db.food WHERE restaurant_id=11);
DELETE FROM wireless_order_db.material_history WHERE material_id IN (SELECT id FROM wireless_order_db.material WHERE restaurant_id=11);
DELETE FROM wireless_order_db.material WHERE restaurant_id=11;
DELETE FROM wireless_order_db.terminal WHERE restaurant_id=11;
DELETE FROM wireless_order_db.table WHERE restaurant_id=11;
DELETE FROM wireless_order_db.region WHERE restaurant_id=11;
DELETE FROM wireless_order_db.food WHERE restaurant_id=11;
DELETE FROM wireless_order_db.taste WHERE restaurant_id=11;
DELETE FROM wireless_order_db.kitchen WHERE restaurant_id=11;
DELETE FROM wireless_order_db.super_kitchen WHERE restaurant_id=11;

DELETE FROM wireless_order_db.member_charge WHERE member_id IN (SELECT id FROM wireless_order_db.member WHERE restaurant_id=11);
DELETE FROM wireless_order_db.member WHERE restaurant_id=11;

DELETE FROM wireless_order_db.setting WHERE restaurant_id=11;

DELETE FROM wireless_order_db.shift WHERE restaurant_id=11;

DELETE FROM wireless_order_db.restaurant WHERE id=11;


