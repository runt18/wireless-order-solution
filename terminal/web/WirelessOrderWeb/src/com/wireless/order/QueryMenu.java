package com.wireless.order;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Taste;


public class QueryMenu {
	
	public static FoodMenu exec(String pin) throws Exception{
		int restaurantID = Verify.exec(pin);
		
		//open the database
		Connection dbCon = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {   		
				
			Class.forName("com.mysql.jdbc.Driver");   
			dbCon = DriverManager.getConnection(Params.dbUrl, Params.dbUser, Params.dbPwd);   
			stmt = dbCon.createStatement();   	
			
			ArrayList<Food> foods = new ArrayList<Food>();
	        //get all the food information to this restaurant
			String sql = "SELECT alias_id, name, unit_price, kitchen FROM " + Params.dbName + ".food WHERE restaurant_id=" + restaurantID +
						 " AND enabled=1";
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				Food food = new Food(rs.getShort("alias_id"),
									 rs.getString("name"),
									 new Float(rs.getFloat("unit_price")),
									 rs.getShort("kitchen"));
				foods.add(food);
			}
		
			rs.close();
			//get all the kitchen information to this restaurant,
			ArrayList<Kitchen> kitchens = new ArrayList<Kitchen>();
			sql = "SELECT alias_id, name, discount, member_discount_1, member_discount_2 FROM " + Params.dbName + ".kitchen WHERE restaurant_id=" + restaurantID;
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				kitchens.add(new Kitchen(rs.getString("name"),
										 rs.getShort("alias_id"),
										 (byte)(rs.getFloat("discount") * 100),
										 (byte)(rs.getFloat("member_discount_1") * 100),
										 (byte)(rs.getFloat("member_discount_2") * 100)));
			}
			
			//Get the taste preferences to this restaurant sort by alias id in ascend order.
			//The lower alias id, the more commonly this preference used.
			//Put the most commonly used taste preference in first position 
			sql = "SELECT alias_id, preference, price FROM " + Params.dbName + ".taste WHERE restaurant_id=" + restaurantID + 
					" ORDER BY alias_id";
			rs = stmt.executeQuery(sql);
			ArrayList<Taste> tastes = new ArrayList<Taste>();
			while(rs.next()){
				Taste taste = new Taste(rs.getShort("alias_id"), 
										rs.getString("preference"),
										new Float(rs.getFloat("price")));
				tastes.add(taste);
			}
			
			return new FoodMenu(foods.toArray(new Food[foods.size()]), 
							    tastes.toArray(new Taste[tastes.size()]),
							    kitchens.toArray(new Kitchen[kitchens.size()]));
			
		}catch(ClassNotFoundException e){
			throw new Exception("菜谱信息请求失败");
			
		}catch(SQLException e){
			throw new Exception("菜谱信息请求失败");
			
		}finally{
			try{
				if(rs != null){
					rs.close();
					rs = null;
				}
				if(stmt != null){
					stmt.close();
					stmt = null;
				}
				if(dbCon != null){
					dbCon.close();
					dbCon = null;
				}
			}catch(SQLException e){
				System.err.println(e.toString());
			}
		}
	}
}
