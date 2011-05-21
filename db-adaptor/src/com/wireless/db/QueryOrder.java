package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.Table;

public class QueryOrder {

	/**
	 * Get the order detail information to the specific table alias id.
	 * 
	 * @param pin
	 *            the pin to the terminal
	 * @param model
	 *            the model id to the terminal
	 * @param tableID
	 *            the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException
	 *             throws if one of cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The table to query does NOT exist.<br>
	 *             - The table associated with this order is idle.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static Order exec(int pin, short model, short tableID)
			throws BusinessException, SQLException {

		Table table = QueryTable.exec(pin, model, tableID);
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();

			int orderID = Util.getUnPaidOrderID(dbCon, table);
			int nCustom = 0;
			// query the custom number from "order" table according to the order id
			String sql = "SELECT custom_num FROM `" + Params.dbName
					+ "`.`order` WHERE id=" + orderID;

			dbCon.rs.close();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if (dbCon.rs.next()) {
				nCustom = dbCon.rs.getByte(1);
			}
			// query the food's id and order count associate with the order id for "order_food" table
			sql = "SELECT name, food_id, SUM(order_count) AS order_sum, unit_price, discount, taste, taste_price, taste_id, kitchen FROM `"
					+ Params.dbName
					+ "`.`order_food` WHERE order_id="
					+ orderID
					+ " GROUP BY food_id, taste_id HAVING order_sum > 0";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			ArrayList<Food> foods = new ArrayList<Food>();
			while (dbCon.rs.next()) {
				Food food = new Food();
				food.name = dbCon.rs.getString("name");
				food.alias_id = dbCon.rs.getInt("food_id");
				food.setCount(new Float(dbCon.rs.getFloat("order_sum")));
				food.setPrice(new Float(dbCon.rs.getFloat("unit_price")));
				food.kitchen = dbCon.rs.getByte("kitchen");
				food.discount = (byte) (dbCon.rs.getFloat("discount") * 100);
				food.taste.preference = dbCon.rs.getString("taste");
				food.taste.setPrice(dbCon.rs.getFloat("taste_price"));
				food.taste.alias_id = dbCon.rs.getShort("taste_id");
				foods.add(food);
			}

			Order orderInfo = new Order();
			orderInfo.id = orderID;
			orderInfo.restaurant_id = table.restaurant_id;
			orderInfo.table_id = tableID;
			orderInfo.custom_num = nCustom;
			orderInfo.foods = foods.toArray(new Food[foods.size()]);

			return orderInfo;

		} finally {
			dbCon.disconnect();
		}
	}
	

}
