package com.wireless.dbReflect;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.protocol.Food;

/**
 * The DB reflector is designed to the bridge between the Food instance of
 * protocol and database.
 * 
 * @author Ying.Zhang
 * 
 */
public class FoodReflector {

	/**
	 * Create the foods from database table 'order_food' according an extra
	 * condition. Note that the database should be connected before invoking
	 * this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @return an array of foods
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 */
	public static Food[] getDetailToday(DBCon dbCon, String extraCond)
			throws SQLException {
		String sql;
		sql = "SELECT name, food_id, food_status, SUM(order_count) AS order_sum, unit_price, "
				+ "discount, taste, taste_price, taste_id, taste_id2, taste_id3, hang_status, kitchen, is_temporary FROM `"
				+ Params.dbName
				+ "`.`order_food` "
				+ (extraCond == null ? "" : extraCond)
				+ " GROUP BY food_id, taste_id, taste_id2, taste_id3, hang_status, is_temporary HAVING order_sum > 0 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<Food> foods = new ArrayList<Food>();
		while (dbCon.rs.next()) {
			Food food = new Food();
			food.name = dbCon.rs.getString("name");
			food.alias_id = dbCon.rs.getInt("food_id");
			food.status = dbCon.rs.getShort("food_status");
			food.setCount(dbCon.rs.getFloat("order_sum"));
			food.setPrice(dbCon.rs.getFloat("unit_price"));
			food.kitchen = dbCon.rs.getShort("kitchen");
			food.setDiscount(dbCon.rs.getFloat("discount"));
			food.tastePref = dbCon.rs.getString("taste");
			food.setTastePrice(dbCon.rs.getFloat("taste_price"));
			food.tastes[0].alias_id = dbCon.rs.getInt("taste_id");
			food.tastes[1].alias_id = dbCon.rs.getInt("taste_id2");
			food.tastes[2].alias_id = dbCon.rs.getInt("taste_id3");
			food.hangStatus = dbCon.rs.getShort("hang_status");
			food.isTemporary = dbCon.rs.getBoolean("is_temporary");
			foods.add(food);
		}
		dbCon.rs.close();
		return foods.toArray(new Food[foods.size()]);
	}

	public static Food[] getDetailHistory(DBCon dbCon, String extraCond)
			throws SQLException {
		String sql;
		sql = "SELECT name, food_id, food_status, SUM(order_count) AS order_sum, unit_price, "
				+ "discount, taste, taste_price, taste_id, taste_id2, taste_id3, hang_status, kitchen, is_temporary FROM `"
				+ Params.dbName
				+ "`.`order_food_history` "
				+ (extraCond == null ? "" : extraCond)
				+ " GROUP BY food_id, taste_id, taste_id2, taste_id3, hang_status, is_temporary HAVING order_sum > 0 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<Food> foods = new ArrayList<Food>();
		while (dbCon.rs.next()) {
			Food food = new Food();
			food.name = dbCon.rs.getString("name");
			food.alias_id = dbCon.rs.getInt("food_id");
			food.status = dbCon.rs.getShort("food_status");
			food.setCount(dbCon.rs.getFloat("order_sum"));
			food.setPrice(dbCon.rs.getFloat("unit_price"));
			food.kitchen = dbCon.rs.getShort("kitchen");
			food.setDiscount(dbCon.rs.getFloat("discount"));
			food.tastePref = dbCon.rs.getString("taste");
			food.setTastePrice(dbCon.rs.getFloat("taste_price"));
			food.tastes[0].alias_id = dbCon.rs.getInt("taste_id");
			food.tastes[1].alias_id = dbCon.rs.getInt("taste_id2");
			food.tastes[2].alias_id = dbCon.rs.getInt("taste_id3");
			food.hangStatus = dbCon.rs.getShort("hang_status");
			food.isTemporary = dbCon.rs.getBoolean("is_temporary");
			foods.add(food);
		}
		dbCon.rs.close();
		return foods.toArray(new Food[foods.size()]);
	}
}
