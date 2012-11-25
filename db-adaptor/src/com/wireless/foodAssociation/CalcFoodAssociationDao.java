package com.wireless.foodAssociation;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryMenu;
import com.wireless.protocol.Food;

public class CalcFoodAssociationDao {
	
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
		Food[] foods = QueryMenu.queryPureFoods(dbCon, null, null);
		final List<Food> f1 = new LinkedList<Food>();
		final List<Food> f2 = new LinkedList<Food>();
		final List<Food> f3 = new LinkedList<Food>();
		int nCnt = 0;
		for(Food f : foods){
			if(nCnt++ < 3000){
				f1.add(f);
			}else if(nCnt++ < 6000){
				f2.add(f);
			}else{
				f3.add(f);
			}
		}
		
		new Thread(){
			@Override
			public void run(){
				DBCon dbCon1 = new DBCon();
				try{
					dbCon1.connect();
					for(Food f : f1){
						exec(dbCon1, f.foodID);
					}
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					dbCon1.disconnect();
				}
			}
		}.start();
		
		new Thread(){
			@Override
			public void run(){
				DBCon dbCon2 = new DBCon();
				try{
					dbCon2.connect();
					for(Food f : f2){
						exec(dbCon2, f.foodID);
					}
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					dbCon2.disconnect();
				}
			}
		}.start();
		
		new Thread(){
			@Override
			public void run(){
				DBCon dbCon3 = new DBCon();
				try{
					dbCon3.connect();
					for(Food f : f3){
						exec(dbCon3, f.foodID);
					}
				}catch(SQLException e){
					e.printStackTrace();
				}finally{
					dbCon3.disconnect();
				}
			}
		}.start();

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
			sql = " DELETE FROM " + Params.dbName + ".food_association" +
				  " WHERE " + 
				  " food_id = " + foodId;
			dbCon.stmt.executeUpdate(sql);
			
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
		
		System.out.println(Thread.currentThread() + "  " + foodId);
	}
}
