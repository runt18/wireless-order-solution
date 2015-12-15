package com.wireless.db.foodAssociation;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.pojo.menuMgr.Food;

public class CalcFoodAssociationDao {
	
	//private static final int MAX_FOOD_AMOUNT_PER_CONNECTION = 3000;

	private final static String rangeCond = " AND OH.order_date BETWEEN DATE_SUB(NOW(), INTERVAL 90 DAY) AND NOW() ";
	
	public final static class Result{
		private final int elapsed;
		Result(int elapsed){
			this.elapsed = elapsed;
		}
		public int getElapsed(){
			return this.elapsed;
		}
		@Override
		public String toString(){
			return "The calculation to food association takes " + elapsed + " sec.";
		}
	}
	
	public static Result exec() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static Result exec(DBCon dbCon) throws SQLException{
		
		long beginTime = System.currentTimeMillis();
		
		String sql;
		//Delete the original food association records.
		sql = " DELETE FROM " + Params.dbName + ".food_association";
		dbCon.stmt.executeUpdate(sql);
		
		Map<Integer, Integer> orderAmounts = new HashMap<Integer, Integer>();
		for(Food f : FoodDao.getPureByCond(dbCon, null, null)){
			//Get the total order amount to this restaurant.
			Integer amount = orderAmounts.get(f.getRestaurantId());
			if(amount != null){
				exec(dbCon, f, amount.intValue());
			}else{
				sql = " SELECT COUNT(*) FROM wireless_order_db.order_history OH WHERE OH.restaurant_id = " + f.getRestaurantId() + rangeCond + "; ";
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				int orderAmount = 0;
				if(dbCon.rs.next()){
					orderAmount = dbCon.rs.getInt(1);
					orderAmounts.put(f.getRestaurantId(), orderAmount);
				}
				dbCon.rs.close();
				
				exec(dbCon, f, orderAmount);
			}
		}
		
		return new Result((int)(System.currentTimeMillis() - beginTime) / 1000);
	}
	
	private static void exec(DBCon dbCon, Food food, int orderAmount) throws SQLException{
		
		String sql;
		
		//Get the id to orders contained the food
		sql = " SELECT OH.id FROM " + Params.dbName + ".order_history OH " +
			  " JOIN " + Params.dbName + ".order_food_history OFH ON OFH.order_id = OH.id " + 
			  " WHERE 1 = 1 " +
			  " AND OFH.food_id = " + food.getFoodId() + 
			  rangeCond ;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		StringBuilder orderIdCond = new StringBuilder();
		while(dbCon.rs.next()){
			if(orderIdCond.length() != 0){
				orderIdCond.append(",").append(dbCon.rs.getInt("id"));
			}else{
				orderIdCond.append(dbCon.rs.getInt("id"));
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
			sql = " SET @total_order_amount = " + orderAmount + ";";
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
