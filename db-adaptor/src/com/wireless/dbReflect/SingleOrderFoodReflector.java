package com.wireless.dbReflect;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.dbObject.SingleOrderFood;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Taste;

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
			  " A.restaurant_id, " +
			  " A.order_id, " +
			  " A.food_id, A.name, A.food_alias, A.food_status, " +
			  " A.order_count, A.unit_price, A.discount, " + 
			  " A.kitchen_id, A.kitchen_alias, A.dept_id, " +
			  "(CASE WHEN A.kitchen_alias = " + Kitchen.KITCHEN_TEMP + " THEN '临时' " +
			  " WHEN A.kitchen_alias = " + Kitchen.KITCHEN_NULL + " THEN '空' " +
			  " WHEN A.kitchen_id IS NULL OR C.kitchen_id IS NULL THEN '已删除厨房' " +
			  " ELSE C.name END) AS kitchen_name, " +
			  " A.taste, A.taste_price, A.taste_id, A.taste2_id, A.taste3_id, A.taste_alias, A.taste2_alias, A.taste3_alias, " +
			  " A.taste_tmp_alias, A.taste_tmp, A.taste_tmp_price, " + 
			  " A.order_date, A.is_temporary, A.is_paid, A.waiter, A.comment, " +
			  " B.type, B.service_rate, D.name as dept_name" +
			  " FROM " + 
			  Params.dbName + "." + orderFoodTbl + " A LEFT OUTER JOIN " +
			  Params.dbName + ".kitchen C " + " ON A.kitchen_id = C.kitchen_id, " +
			  Params.dbName + "." + orderTbl + " B, " +
			  Params.dbName + ".department D " +
			  " WHERE " +
			  " A.order_id = B.id AND A.dept_id = D.dept_id AND B.restaurant_id = D.restaurant_id " +
			  (extraCond == null ? "" : extraCond) + " " +
			  (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		ArrayList<SingleOrderFood> singleOrderFoods = new ArrayList<SingleOrderFood>();
		while(dbCon.rs.next()){
			SingleOrderFood singleOrderFood = new SingleOrderFood();

			singleOrderFood.orderID = dbCon.rs.getLong("order_id");
			
			int restaurantID = dbCon.rs.getInt("restaurant_id");
			
			singleOrderFood.food.restaurantID = restaurantID;
			singleOrderFood.food.foodID = dbCon.rs.getInt("food_id");
			singleOrderFood.food.name = dbCon.rs.getString("name");
			singleOrderFood.food.aliasID = dbCon.rs.getInt("food_alias");
			singleOrderFood.food.status = dbCon.rs.getShort("food_status");
			
			singleOrderFood.orderCount = dbCon.rs.getFloat("order_count");
			singleOrderFood.unitPrice = dbCon.rs.getFloat("unit_price");
			singleOrderFood.discount = dbCon.rs.getFloat("discount");

			singleOrderFood.kitchen.kitchenID = dbCon.rs.getInt("kitchen_id");			
			singleOrderFood.kitchen.aliasID = dbCon.rs.getShort("kitchen_alias");
			singleOrderFood.kitchen.name = dbCon.rs.getString("kitchen_name");
			singleOrderFood.kitchen.dept.restaurantID = restaurantID;
			singleOrderFood.kitchen.dept.deptID = dbCon.rs.getShort("dept_id");
			singleOrderFood.kitchen.dept.name = dbCon.rs.getString("dept_name");
			
			String normalTastePref = dbCon.rs.getString("taste");
			String tmpTastePref = dbCon.rs.getString("taste_tmp");
			if(tmpTastePref != null){
				singleOrderFood.taste.preference = (normalTastePref.equals(Taste.NO_PREFERENCE) ? "" : (normalTastePref + ",")) + tmpTastePref;				
			}else{
				singleOrderFood.taste.preference = normalTastePref;
			}
			
			float normalTastePrice = dbCon.rs.getFloat("taste_price");
			float tmpTastePrice = dbCon.rs.getFloat("taste_tmp_price");
			singleOrderFood.taste.setPrice(normalTastePrice + tmpTastePrice);
			
			singleOrderFood.orderDate = dbCon.rs.getTimestamp("order_date").getTime();
			singleOrderFood.isTemporary = dbCon.rs.getBoolean("is_temporary");
			singleOrderFood.isPaid = dbCon.rs.getBoolean("is_paid");
			singleOrderFood.staff.name = dbCon.rs.getString("waiter");
			
			singleOrderFood.payManner = dbCon.rs.getInt("type");
			singleOrderFood.serviceRate = dbCon.rs.getFloat("service_rate");
			
			singleOrderFood.comment = dbCon.rs.getString("comment");
			
			singleOrderFoods.add(singleOrderFood);
		}
		dbCon.rs.close();
		
		return singleOrderFoods.toArray(new SingleOrderFood[singleOrderFoods.size()]);
	}
}
