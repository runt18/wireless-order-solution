package com.wireless.db.foodStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.restaurantMgr.Restaurant;

public class CalcFoodWeightDao {

	public static void exec(DBCon dbCon) throws SQLException{
		String sql;
		sql = " SELECT id " + " FROM " + Params.dbName + ".restaurant" + " WHERE " + " id > " + Restaurant.RESERVED_7;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<Integer> result = new ArrayList<Integer>();
		while(dbCon.rs.next()){
			result.add(dbCon.rs.getInt("id"));
		}
		dbCon.rs.close();
		
		for(Integer restaurantId : result){
			exec(dbCon, restaurantId);
		}
	}
	
	/**
	 * Calculate the weight to food
	 * @param dbCon
	 * @param restaurantId
	 * @throws SQLException
	 */
	public static void exec(DBCon dbCon, int restaurantId) throws SQLException{
		String sql;
		
		//Get the total order amount to this restaurant.
		int totalOrderAmount = 0;
		
		sql = " SELECT COUNT(*) " + " FROM " + Params.dbName + ".order_history " + " WHERE " + " restaurant_id = " + restaurantId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			totalOrderAmount = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		if(totalOrderAmount > 0){
			sql = " SET @total_order_amount = " + totalOrderAmount + ";";
			dbCon.stmt.execute(sql);
			
			//Calculate the total order food amount to this restaurant.
			sql = " SELECT SUM(order_count) FROM " + Params.dbName + ".order_food_history WHERE restaurant_id = " + restaurantId;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			int totalOrderFoodAmount = 0;
			if(dbCon.rs.next()){
				totalOrderFoodAmount = dbCon.rs.getInt(1);
			}
			dbCon.rs.close();
			
			sql = " SET @total_order_food_amount = " + totalOrderFoodAmount;
			dbCon.stmt.execute(sql);
			
			//Calculate the food weight to each food to this restaurant using TF-IDF.
			sql = " SELECT " +
				  " A.food_id, " +
				  " COUNT(A.order_id) / @total_order_amount AS probability, " +
				  " (SUM(A.order_food_amount) / @total_order_food_amount * LOG(@total_order_amount / COUNT(A.order_id))) AS weight " +
				  " FROM " + 
				  " ((SELECT " +
				  " order_id, food_id, SUM(order_count) AS order_food_amount " + " FROM " + Params.dbName + ".order_food_history " +
				  " WHERE " + 
				  " food_id IN ( " +
				  " SELECT food_id FROM " + Params.dbName + ".food" + " WHERE " +
				  " restaurant_id = " + restaurantId + " AND " + 
				  " name NOT LIKE '送%' " + " AND " +
				  " name NOT LIKE '赠送%' " + " AND " + 
				  " name NOT LIKE '配%' " + " AND " +
				  " name NOT LIKE '白饭%' " + " AND " +
				  " name NOT LIKE '%打包%' " + " AND " +
				  " name NOT LIKE '%茶位%' " + " AND " +
				  " name NOT LIKE '%饭盒%' " + " AND " +
				  " name NOT LIKE '%饭合%' " + " AND " +
				  " name NOT LIKE '%纸巾%' " + 
				  " ) " + 
				  " GROUP BY " + 
				  " order_id, food_id ) AS A) " +
				  " GROUP BY " +
				  " A.food_id " + ";";

			dbCon.stmt.clearBatch();
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				final int foodId = dbCon.rs.getInt("food_id");
				final float probability = dbCon.rs.getFloat("probability");
				final float weight = dbCon.rs.getFloat("weight");
				
				//Insert the probability & weight of each food to this restaurant.
				sql = " UPDATE " + Params.dbName + ".food_statistics SET " +
					  " weight = " + weight +
					  " ,probability = " + probability +
					  " WHERE " +
					  " food_id = " + foodId;
				dbCon.stmt.addBatch(sql);
				
			}
			dbCon.rs.close();

			dbCon.stmt.executeBatch();
		}
		
	}

}
