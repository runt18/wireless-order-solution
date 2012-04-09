package com.wireless.dbReflect;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.dbObject.SingleOrderFood;

public class SingleOrderFoodReflector {
	
	/**
	 * Generate the history single order foods according an extra condition. 
	 * Note that the database should be connected before invoking this method.
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
	public static SingleOrderFood[] getDetailToday(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		return getResult(dbCon, extraCond, orderClause, false);
	}
	
	/**
	 * Generate the today single order foods according an extra condition. 
	 * Note that the database should be connected before invoking this method.
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
	public static SingleOrderFood[] getDetailHistory(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		return getResult(dbCon, extraCond, orderClause, true);
	}
	
	/**
	 * Generate the single order foods according an extra condition. 
	 * Note that the database should be connected before invoking this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @param isHistory
	 * 			  indicates whether to search history record 
	 * @return an array of foods
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 */
	private static SingleOrderFood[] getResult(DBCon dbCon, String extraCond, String orderClause, boolean isHistory) throws SQLException{
		
		String orderFoodTbl = isHistory ? "order_food_history" : "order_food";
		String orderTbl = isHistory ? "order_history" : "order";
		
		String sql;
		sql = " SELECT " +
			  " A.order_id, " +
			  " A.food_id, A.name, A.food_alias, A.food_status, " +
			  " A.order_count, A.unit_price, A.discount, " + 
			  " A.kitchen_id, A.kitchen_alias, C.name AS kitchen_name, A.dept_id, " +
			  " A.taste, A.taste_price, A.taste_id, A.taste2_id, A.taste3_id, A.taste_alias, A.taste2_alias, A.taste3_alias, " +
			  " A.order_date, A.is_temporary, A.is_paid, A.waiter, " +
			  " B.type, B.service_rate " +
			  " FROM " + 
			  Params.dbName + "." + orderFoodTbl + " A, " +
			  Params.dbName + "." + orderTbl + " B, " +
			  Params.dbName + ".kitchen C " +
			  " WHERE " +
			  " A.order_id = B.id " + " AND " +
			  " A.kitchen_id = C.kitchen_id " + 
			  (extraCond == null ? "" : extraCond) + " " +
			  (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		ArrayList<SingleOrderFood> singleOrderFoods = new ArrayList<SingleOrderFood>();
		while(dbCon.rs.next()){
			SingleOrderFood orderFood = new SingleOrderFood();

			orderFood.orderID = dbCon.rs.getLong("order_id");
			
			orderFood.food.foodID = dbCon.rs.getInt("food_id");
			orderFood.food.name = dbCon.rs.getString("name");
			orderFood.food.aliasID = dbCon.rs.getInt("food_alias");
			orderFood.food.status = dbCon.rs.getShort("food_status");
			
			orderFood.orderCount = dbCon.rs.getFloat("order_count");
			orderFood.unitPrice = dbCon.rs.getFloat("unit_price");
			orderFood.discount = dbCon.rs.getFloat("discount");

			orderFood.kitchen.kitchenID = dbCon.rs.getInt("kitchen_id");			
			orderFood.kitchen.aliasID = dbCon.rs.getShort("kitchen_alias");
			orderFood.kitchen.name = dbCon.rs.getString("kitchen_name");
			orderFood.kitchen.deptID = dbCon.rs.getShort("dept_id");
			
			orderFood.taste.preference = dbCon.rs.getString("taste");			
			orderFood.taste.setPrice(dbCon.rs.getFloat("taste_price"));
			
			orderFood.orderDate = dbCon.rs.getTimestamp("order_date").getTime();
			orderFood.isTemporary = dbCon.rs.getBoolean("is_temporary");
			orderFood.isPaid = dbCon.rs.getBoolean("is_paid");
			orderFood.staff.name = dbCon.rs.getString("waiter");
			
			orderFood.payManner = dbCon.rs.getInt("type");
			orderFood.serviceRate = dbCon.rs.getFloat("service_rate");
			
			singleOrderFoods.add(orderFood);
		}
		dbCon.rs.close();
		
		return singleOrderFoods.toArray(new SingleOrderFood[singleOrderFoods.size()]);
	}
}
