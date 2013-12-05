package com.wireless.db.foodAssociation;

import java.sql.SQLException;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.pojo.menuMgr.Food;

public class CalcFoodAssociationDao {
	
	//private static final int MAX_FOOD_AMOUNT_PER_CONNECTION = 3000;
	
	public static void exec() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			exec(dbCon);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static void exec(DBCon dbCon) throws SQLException{
		
		String sql;
		//Delete the original food association records.
		sql = " DELETE FROM " + Params.dbName + ".food_association";
		dbCon.stmt.executeUpdate(sql);
		
		List<Food> foods = FoodDao.getPureFoods(dbCon, null, null);		
		
		for(Food f : foods){
			exec(dbCon, f);
		}
	}
	
	public static void exec(Food food) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			exec(dbCon, food);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static void exec(DBCon dbCon, Food food) throws SQLException{
		
		String sql;
		
		//Get the id to orders contained the food
		sql = " SELECT order_id FROM " + Params.dbName + ".order_food_history " +
			  " WHERE " + " food_id = " + food.getFoodId() + " GROUP BY order_id ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		StringBuilder orderIdCond = new StringBuilder();
		while(dbCon.rs.next()){
			if(orderIdCond.length() != 0){
				orderIdCond.append(",").append(dbCon.rs.getInt("order_id"));
			}else{
				orderIdCond.append(dbCon.rs.getInt("order_id"));
			}
		}
		dbCon.rs.close();
		
		if(orderIdCond.length() != 0){
			//Delete the original food association records.
//			sql = " DELETE FROM " + Params.dbName + ".food_association" +
//				  " WHERE " + 
//				  " food_id = " + foodId;
//			dbCon.stmt.executeUpdate(sql);
			
			//Get the total order amount to this restaurant.
//			int totalOrderAmount = 0;
//			
//			sql = " SELECT COUNT(*) " + " FROM " + Params.dbName + ".order_history " + " WHERE " + " restaurant_id = " + food.getRestaurantId();
//			dbCon.rs = dbCon.stmt.executeQuery(sql);
//			if(dbCon.rs.next()){
//				totalOrderAmount = dbCon.rs.getInt(1);
//			}
//			dbCon.rs.close();
//			
//			sql = " SET @total_order_amount = " + totalOrderAmount + ";";
//			dbCon.stmt.execute(sql);
//			
//			//Calculate the top 10 most associated food.
//			sql = " INSERT INTO " + Params.dbName + ".food_association" +
//				  " (`food_id`, `associated_food_id`, `associated_amount`, `joint_probability`) " +
//				  " SELECT " +
//				  food.getFoodId() + "," +
//				  " food_id AS associated_food_id, COUNT(food_id) AS associated_amount, " +
//				  " COUNT(food_id) / @total_order_amount AS joint_probability " +
//				  " FROM " +
//				  " (( SELECT " + " order_id, food_id " + " FROM " + Params.dbName + ".order_food_history" +
//				  " WHERE " + " order_id " + " IN ( " + orderIdCond + " ) " +
//				  " GROUP BY " + " order_id, food_id ) " + " AS A ) " +
//				  " WHERE food_id <> " + food.getFoodId() +
//				  " GROUP BY " + " food_id " +
//				  " ORDER BY associated_amount DESC " +
//				  " LIMIT 10 ";
//			dbCon.stmt.executeUpdate(sql);
			
			sql = " SET @food_id_to_calc = " + food.getFoodId() + ";";
			dbCon.stmt.execute(sql);
			
			//Get the total order amount to this restaurant.
			sql = " SELECT @total_order_amount := COUNT(*) FROM wireless_order_db.order_history WHERE restaurant_id IN (" + food.getRestaurantId() + "); ";
			dbCon.stmt.execute(sql);
			
			//Get the probability to food.
			sql = " SELECT @food_probability := probability FROM wireless_order_db.food WHERE food_id = @food_id_to_calc; ";
			dbCon.stmt.execute(sql);
			
			//Calculate the similarity between food and its associated one.
			sql = " INSERT INTO " + Params.dbName + ".food_association" +
				   " (`food_id`, `associated_food_id`, `associated_amount`, `joint_probability`, `similarity`) " +
				   " SELECT " +
				   " @food_id_to_calc, associated_food_id, COUNT(associated_food_id) AS associated_amount, " + 
				   " COUNT(associated_food_id) / @total_order_amount AS joint_probability, " +
				   " (COUNT(associated_food_id) / @total_order_amount) / SQRT(@food_probability * F.probability) AS similarity " +
				   " FROM (( " +
				   " SELECT order_id, food_id AS associated_food_id FROM wireless_order_db.order_food_history WHERE order_id IN (" + orderIdCond + ") GROUP BY order_id, associated_food_id " +
				   " ) AS A) " +
				   " JOIN wireless_order_db.food F ON A.associated_food_id = F.food_id " +
				   " WHERE associated_food_id <> @food_id_to_calc " +
				   " GROUP BY A.associated_food_id " +
				   " HAVING similarity IS NOT NULL " +
				   " ORDER BY similarity DESC " +
				   " LIMIT 20; ";
			
			dbCon.stmt.executeUpdate(sql);

			//Get the max similarity to the food.
			sql = " SELECT @max_similarity := MAX(similarity) FROM " + Params.dbName + ".food_association WHERE food_id = @food_id_to_calc";
			dbCon.stmt.execute(sql);
			
			//Normalize the similarity.
			sql = " UPDATE " + Params.dbName + ".food_association SET " +
				  " similarity = similarity / @max_similarity " +
				  " WHERE food_id = @food_id_to_calc";
			dbCon.stmt.executeUpdate(sql);
		}
		
	}
}
