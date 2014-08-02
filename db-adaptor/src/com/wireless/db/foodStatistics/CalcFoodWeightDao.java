package com.wireless.db.foodStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.restaurantMgr.Restaurant;

public class CalcFoodWeightDao {

	public static void exec() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
		}finally{
			dbCon.disconnect();
		}
	}
	
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
		
		sql = " UPDATE " + Params.dbName + ".food " + 
			  " SET restaurant_id = restaurant_id " +
			  " ,weight = 0 " +
			  " ,probability = 0 " +
			  " WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
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
				  " ( " + 
					  " SELECT " +
					  " OFH.order_id, OFH.food_id, SUM(OFH.order_count) AS order_food_amount " + 
					  " FROM " + Params.dbName + ".order_food_history OFH " +
					  " JOIN " + Params.dbName + ".food F ON OFH.food_id = F.food_id " +
					  " WHERE 1 = 1 " + 
					  " AND F.restaurant_id = " + restaurantId +
					  " AND F.name NOT LIKE '送%' " + 
					  " AND F.name NOT LIKE '赠送%' " + 
					  " AND F.name NOT LIKE '配%' " + 
					  " AND F.name NOT LIKE '白饭%' " + 
					  " AND F.name NOT LIKE '%打包%' " + 
					  " AND F.name NOT LIKE '%茶位%' " + 
					  " AND F.name NOT LIKE '%饭盒%' " + 
					  " AND F.name NOT LIKE '%饭合%' " + 
					  " AND F.name NOT LIKE '%纸巾%' " + 
					  " GROUP BY OFH.order_id, OFH.food_id " + 
				  " ) AS A " +
				  " GROUP BY A.food_id ";

			sql = " UPDATE " + Params.dbName + ".food F " +
				  " JOIN ( " + sql + " ) AS TMP ON F.food_id = TMP.food_id " +
			      " SET " + 
				  " F.weight = TMP.weight, " +
			      " F.probability = TMP.probability ";
			dbCon.stmt.executeUpdate(sql);
			
		}
		
	}

}
