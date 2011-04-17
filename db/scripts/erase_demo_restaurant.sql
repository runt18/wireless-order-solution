DELETE FROM wireless_order_db.order_food_history WHERE order_id IN (SELECT id FROM wireless_order_db.order_history WHERE restaurant_id=11);
DELETE FROM wireless_order_db.order_history WHERE restaurant_id=11;
DELETE FROM wireless_order_db.order_food WHERE order_id IN (SELECT id FROM wireless_order_db.order WHERE restaurant_id=11);
DELETE FROM wireless_order_db.order WHERE restaurant_id=11;
DELETE FROM wireless_order_db.food_material WHERE food_id IN (SELECT id FROM wireless_order_db.food WHERE restaurant_id=11);
DELETE FROM wireless_order_db.material_history WHERE material_id IN (SELECT id FROM wireless_order_db.material WHERE restaurant_id=11);
DELETE FROM wireless_order_db.material WHERE restaurant_id=11;
DELETE FROM wireless_order_db.terminal WHERE restaurant_id=11;
DELETE FROM wireless_order_db.table WHERE restaurant_id=11;
DELETE FROM wireless_order_db.food WHERE restaurant_id=11;
DELETE FROM wireless_order_db.taste WHERE restaurant_id=11;
DELETE FROM wireless_order_db.kitchen WHERE restaurant_id=11;
DELETE FROM wireless_order_db.member WHERE restaurant_id=11;

DELETE FROM wireless_order_db.restaurant WHERE id=11;


