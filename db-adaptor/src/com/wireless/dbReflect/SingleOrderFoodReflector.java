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
		
		String tblName = isHistory ? "order_food_history" : "order_food";
		
		String sql;
		sql = " SELECT A.*, B.name AS kitchen_name " +
			  " FROM " + 
			  Params.dbName + "." + tblName + " A, " +
			  Params.dbName + ".kitchen B " +
			  " WHERE " +
			  " A.kitchen_id = B.kitchen_id " + 
			  (extraCond == null ? "" : extraCond) + " " +
			  (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		ArrayList<SingleOrderFood> singleOrderFoods = new ArrayList<SingleOrderFood>();
		while(dbCon.rs.next()){
			SingleOrderFood singleOrderFood = new SingleOrderFood();
			singleOrderFood.discount = dbCon.rs.getFloat("discount");
			singleOrderFood.name = dbCon.rs.getString("name");
			singleOrderFood.orderCount = dbCon.rs.getFloat("order_count");
			singleOrderFood.orderDate = dbCon.rs.getTimestamp("order_date");
			singleOrderFood.orderID = dbCon.rs.getLong("order_id");
			singleOrderFood.unitPrice = dbCon.rs.getFloat("unit_price");
			singleOrderFood.waiter = dbCon.rs.getString("waiter");
			singleOrderFood.taste.preference = dbCon.rs.getString("taste");
			singleOrderFood.taste.setPrice(dbCon.rs.getFloat("taste_price"));
			singleOrderFood.kitchen.kitchenID = dbCon.rs.getInt("kitchen_id");
			singleOrderFood.kitchen.aliasID = dbCon.rs.getShort("kitchen_alias");
			singleOrderFood.kitchen.deptID = dbCon.rs.getShort("dept_id");
			singleOrderFood.kitchen.name = dbCon.rs.getString("kitchen_name");
			singleOrderFoods.add(singleOrderFood);
		}
		dbCon.rs.close();
		
		return singleOrderFoods.toArray(new SingleOrderFood[singleOrderFoods.size()]);
	}
}
