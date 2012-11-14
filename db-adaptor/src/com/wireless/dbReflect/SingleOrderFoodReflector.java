package com.wireless.dbReflect;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.dbObject.SingleOrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.TasteGroup;

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
		String tgTbl = isHistory ? "taste_group_history" : "taste_group";
		
		String sql;
		sql = " SELECT " +
			  " A.restaurant_id, " +
			  " A.order_id, " +
			  " A.food_id, A.name, A.food_alias, A.food_status, " +
			  " A.order_count, A.unit_price, A.discount, " + 
			  " A.kitchen_id, A.kitchen_alias, " +
			  "(CASE WHEN C.kitchen_id IS NULL THEN '已删除厨房' ELSE C.name END) AS kitchen_name, " +
			  " TG.taste_group_id, " +
			  " TG.normal_taste_group_id, TG.normal_taste_pref, TG.normal_taste_price, " +
			  " (CASE WHEN TG.tmp_taste_id IS NULL THEN 0 ELSE 1 END) AS has_tmp_taste, " +
			  " TG.tmp_taste_pref, TG.tmp_taste_price, " +
			  " A.order_date, A.is_temporary, A.is_paid, A.waiter, A.comment, " +
			  " B.type, B.service_rate, B.erase_price, " +
			  " A.dept_id, (CASE WHEN D.dept_id IS NULL THEN '已删除部门' ELSE D.name END) as dept_name " +
			  " FROM " + 
			  Params.dbName + "." + orderFoodTbl + " A LEFT JOIN " +
			  Params.dbName + ".kitchen C " + 
			  " ON A.kitchen_id = C.kitchen_id " + 
			  " LEFT JOIN " +
			  Params.dbName + ".department D " + 
			  " ON C.restaurant_id = D.restaurant_id AND C.dept_id = D.dept_id " +
			  " JOIN " +
			  Params.dbName + "." + tgTbl + " TG " +
			  " ON " +
			  " A.taste_group_id = TG.taste_group_id " +
			  " JOIN " +
			  Params.dbName + "." + orderTbl + " B " +
			  " ON  A.order_id = B.id " +
			  " WHERE 1=1 " +
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
			singleOrderFood.food.setAliasId(dbCon.rs.getInt("food_alias"));
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
			
			int tasteGroupId = dbCon.rs.getInt("taste_group_id");
			if(tasteGroupId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				Taste normal = null;
				if(dbCon.rs.getInt("normal_taste_group_id") != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
					normal = new Taste();
					normal.setPreference(dbCon.rs.getString("normal_taste_pref"));
					normal.setPrice(dbCon.rs.getFloat("normal_taste_price"));
				}
				
				Taste temp = null;
				if(dbCon.rs.getBoolean("has_tmp_taste")){
					temp = new Taste();
					temp.setPreference(dbCon.rs.getString("tmp_taste_pref"));
					temp.setPrice(dbCon.rs.getFloat("tmp_taste_price"));
				}
				
				singleOrderFood.tasteGroup = new TasteGroup(tasteGroupId, normal, temp);
				
			}
			
			singleOrderFood.orderDate = dbCon.rs.getTimestamp("order_date").getTime();
			singleOrderFood.isTemporary = dbCon.rs.getBoolean("is_temporary");
			singleOrderFood.isPaid = dbCon.rs.getBoolean("is_paid");
			singleOrderFood.staff.name = dbCon.rs.getString("waiter");
			
			singleOrderFood.payManner = dbCon.rs.getInt("type");
			singleOrderFood.serviceRate = dbCon.rs.getFloat("service_rate");
			
			singleOrderFood.comment = dbCon.rs.getString("comment");
			
			singleOrderFood.erasePrice = dbCon.rs.getInt("erase_price");
			
			singleOrderFoods.add(singleOrderFood);
		}
		dbCon.rs.close();
		
		return singleOrderFoods.toArray(new SingleOrderFood[singleOrderFoods.size()]);
	}
}
