package com.wireless.dbReflect;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.protocol.OrderFood;

/**
 * The DB reflector is designed to the bridge between the Food instance of
 * protocol and database.
 * 
 * @author Ying.Zhang
 * 
 */
public class OrderFoodReflector {

	private final static byte DETAIL_TODAY = 1;
	private final static byte DETAIL_HISTORY = 2;
	
	/**
	 * Create the foods from database table 'order_food' according an extra
	 * condition. Note that the database should be connected before invoking
	 * this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return an array of foods
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 */
	public static OrderFood[] getDetailToday(DBCon dbCon, String extraCond, String orderClause)
			throws SQLException {
		return getDetails(dbCon, extraCond, orderClause, DETAIL_TODAY);
	}

	/**
	 * Create the foods from database table 'order_food_history' according an extra
	 * condition. Note that the database should be connected before invoking
	 * this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return an array of foods
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 */
	public static OrderFood[] getDetailHistory(DBCon dbCon, String extraCond, String orderClause) throws SQLException {
		return getDetails(dbCon, extraCond, orderClause, DETAIL_HISTORY);
	}
	
	/**
	 * Create the foods from database table 'order_food' according an extra
	 * condition. Note that the database should be connected before invoking
	 * this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 * 
	 * @param detailType
	 * 			  one of the detail types
	 * @return an array of foods
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 */
	private static OrderFood[] getDetails(DBCon dbCon, String extraCond, String orderClause, byte detailType) throws SQLException{
		String sql;
		sql = "SELECT " 
				+ "A.name, A.food_id, A.food_status, SUM(A.order_count) AS order_sum, A.unit_price, A.order_date, "
				+ "A.discount, A.taste, A.taste_price, A.taste_id, A.taste_id2, A.taste_id3, " 
				+ "A.hang_status, A.kitchen, A.is_temporary, B.type FROM `"
				+ Params.dbName
				+ (detailType == DETAIL_TODAY ? "`.`order_food` " : "`.`order_food_history`") 
				+ "A LEFT OUTER JOIN " + Params.dbName + ".order B ON (A.order_id = B.id) "
				+ (extraCond == null ? "" : extraCond)
				+ " GROUP BY food_id, taste_id, taste_id2, taste_id3, hang_status, is_temporary HAVING order_sum > 0 "
				+ orderClause;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<OrderFood> orderFoods = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood food = new OrderFood();
			food.name = dbCon.rs.getString("name");
			food.alias_id = dbCon.rs.getInt("food_id");
			food.status = dbCon.rs.getShort("food_status");
			food.setCount(dbCon.rs.getFloat("order_sum"));
			food.setPrice(dbCon.rs.getFloat("unit_price"));
			food.orderDate = dbCon.rs.getTimestamp("order_date").getTime();
			food.kitchen = dbCon.rs.getShort("kitchen");
			food.setDiscount(dbCon.rs.getFloat("discount"));
			food.tastePref = dbCon.rs.getString("taste");
			food.setTastePrice(dbCon.rs.getFloat("taste_price"));
			food.tastes[0].alias_id = dbCon.rs.getInt("taste_id");
			food.tastes[1].alias_id = dbCon.rs.getInt("taste_id2");
			food.tastes[2].alias_id = dbCon.rs.getInt("taste_id3");
			food.hangStatus = dbCon.rs.getShort("hang_status");
			food.isTemporary = dbCon.rs.getBoolean("is_temporary");
			food.payManner = dbCon.rs.getShort("type");
			orderFoods.add(food);
		}
		dbCon.rs.close();
		return orderFoods.toArray(new OrderFood[orderFoods.size()]);
	}
}
