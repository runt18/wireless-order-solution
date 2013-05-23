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
			exec(dbCon, f.getFoodId());
		}

		
//		List<Food> foodsToAssociate = null;
//		for(int i = 0; i < foods.length; i++){
//			if(i % MAX_FOOD_AMOUNT_PER_CONNECTION == 0){
//				foodsToAssociate = new ArrayList<Food>();
//				foodsToAssociate.add(foods[i]);
//				
//			}else if(i % MAX_FOOD_AMOUNT_PER_CONNECTION < MAX_FOOD_AMOUNT_PER_CONNECTION - 1 && i != foods.length - 1){
//				foodsToAssociate.add(foods[i]);
//				
//			}else if(i % MAX_FOOD_AMOUNT_PER_CONNECTION == MAX_FOOD_AMOUNT_PER_CONNECTION - 1 || i == foods.length - 1){
//				
//				foodsToAssociate.add(foods[i]);
//				
//				final List<Food> foodsToCalc = foodsToAssociate; 
//				
//				new Thread(){
//					@Override
//					public void run(){
//						//long beginTime = System.currentTimeMillis();
//						DBCon dbCon = new DBCon();
//						try{
//							dbCon.connect();
//							for(Food f : foodsToCalc){
//								exec(dbCon, f.foodID);
//							}
//						}catch(SQLException e){
//							e.printStackTrace();
//						}finally{
//							dbCon.disconnect();
//						}
//						//long elapsedTime = System.currentTimeMillis() - beginTime;
//						//System.err.println(this.toString() + " takes " + elapsedTime / 1000 + " sec.");
//					}
//				}.start();
//				
//			}
//		}
	}
	
	public static void exec(long foodId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			exec(dbCon, foodId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static void exec(DBCon dbCon, long foodId) throws SQLException{
		
		String sql;
		
		//Get the id to orders contained the food
		sql = " SELECT order_id FROM " + Params.dbName + ".order_food_history " +
			  " WHERE " + " food_id = " + foodId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		String orderIdCond = null;
		while(dbCon.rs.next()){
			if(orderIdCond != null){
				orderIdCond += "," + dbCon.rs.getInt("order_id");
			}else{
				orderIdCond = "" + dbCon.rs.getInt("order_id");
			}
		}
		dbCon.rs.close();
		
		if(orderIdCond != null){
			//Delete the original food association records.
//			sql = " DELETE FROM " + Params.dbName + ".food_association" +
//				  " WHERE " + 
//				  " food_id = " + foodId;
//			dbCon.stmt.executeUpdate(sql);
			
			//Calculate the top 10 most associated food.
			sql = " INSERT INTO " + Params.dbName + ".food_association" +
				  " (`food_id`, `associated_food_id`, `associated_amount`) " +
				  " SELECT " +
				  foodId + "," +
				  " food_id AS associated_food_id, COUNT(food_id) AS associated_amount " +
				  " FROM " +
				  " (( SELECT " + " order_id, food_id " + " FROM " + Params.dbName + ".order_food_history" +
				  " WHERE " + " order_id " + " IN ( " + orderIdCond + " ) " +
				  " GROUP BY " + " order_id, food_id ) " + " AS A ) " +
				  " WHERE food_id <> " + foodId +
				  " GROUP BY " + " food_id " +
				  " ORDER BY associated_amount DESC " +
				  " LIMIT 10 ";
			dbCon.stmt.executeUpdate(sql);
		}
		
		//System.out.println(Thread.currentThread() + "  " + foodId);
	}
}
