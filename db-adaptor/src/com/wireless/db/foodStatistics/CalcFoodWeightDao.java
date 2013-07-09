package com.wireless.db.foodStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		
		//Get the order amount to this restaurant.
		sql = " SET @restaurant_id = " + restaurantId + ";";
		dbCon.stmt.execute(sql);
		
		sql = " SET @order_amount = " + " (SELECT COUNT(*) " + " FROM " + Params.dbName + ".order_history " + " WHERE " + " restaurant_id = @restaurant_id); ";
		dbCon.stmt.execute(sql);
		
		//Calculate the food weight to each food to this restaurant.
		sql = " SELECT " +
			  " A.food_id, " +
			  " (COUNT(A.order_id) / @order_amount * LOG10(@order_amount / COUNT(A.order_id))) AS weight " +
			  " FROM " + 
			  " ((SELECT " +
			  " order_id, food_id " + " FROM " + Params.dbName + ".order_food_history " +
			  " WHERE " + 
			  " food_id IN ( " +
			  " SELECT food_id FROM " + Params.dbName + ".food" + " WHERE " +
			  " restaurant_id = @restaurant_id " + " AND " + 
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

		List<Map.Entry<Integer, Float>> foodWeights = new ArrayList<Map.Entry<Integer, Float>>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			final int foodId = dbCon.rs.getInt("food_id");
			final float weight = dbCon.rs.getFloat("weight");
			
			foodWeights.add(new Map.Entry<Integer, Float>(){

				@Override
				public Integer getKey() {
					return foodId;
				}

				@Override
				public Float getValue() {
					return weight;
				}

				@Override
				public Float setValue(Float value) {
					return null;
				}
			});
		}
		dbCon.rs.close();
		
		//Insert the weight of each food to this restaurant.
		for(Map.Entry<Integer, Float> entry : foodWeights){
			sql = " UPDATE " + Params.dbName + ".food_statistics " +
				  " SET weight = " + entry.getValue() + 
				  " WHERE " +
				  " food_id = " + entry.getKey();
			dbCon.stmt.executeUpdate(sql);
		}
		
		
	}

}
