package com.wireless.order;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;



public class QueryOrder {
	
	public static Order exec(String pin, short tableID) throws Exception{
		
		int restaurantID = Verify.exec(pin);
		
		//open the database
		Connection dbCon = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {   		
			
			Class.forName("com.mysql.jdbc.Driver");   
		
			dbCon = DriverManager.getConnection(Params.dbUrl, Params.dbUser, Params.dbPwd);   
			stmt = dbCon.createStatement();   	
			
			//query the order id associated with the this table
			String sql = "SELECT id FROM `" + Params.dbName + 
						"`.`order` WHERE table_id = " + tableID +
						" AND restaurant_id = " + restaurantID +
						" AND total_price IS NULL";
			rs = stmt.executeQuery(sql);
			if(rs.next()){
				int orderID = rs.getInt("id");
				int nCustom = 0;
				//query the custom number from "order" table according to the order id
				sql = "SELECT custom_num FROM `" + Params.dbName + "`.`order` WHERE id=" + orderID;
				
				rs.close();
				rs = stmt.executeQuery(sql);
				if(rs.next()){
					nCustom = rs.getByte(1);
				}
				//query the food's id and order count associate with the order id for "order_food" table
				sql = "SELECT name, food_id, SUM(order_count) AS order_sum, unit_price, discount, taste, taste_price, taste_id FROM `" + 
						Params.dbName + 
						"`.`order_food` WHERE order_id=" + orderID +
						" GROUP BY food_id, taste_id HAVING order_sum > 0";
				rs = stmt.executeQuery(sql);
				ArrayList<Food> foods = new ArrayList<Food>();
				while(rs.next()){
					Food food = new Food();
					food.name = rs.getString("name");
					food.alias_id = rs.getInt("food_id");
					food.setCount(new Float(rs.getFloat("order_sum")));
					food.setPrice(new Float(rs.getFloat("unit_price")));
					food.discount = (byte)(rs.getFloat("discount") * 100);
					food.taste.preference = rs.getString("taste");
					food.taste.setPrice(rs.getFloat("taste_price"));
					food.taste.alias_id = rs.getShort("taste_id");
					foods.add(food);			
				}
				
				Order orderInfo = new Order();
				orderInfo.id = orderID;
				orderInfo.tableID = tableID;
				orderInfo.customNum = nCustom;
				orderInfo.foods = foods.toArray(new Food[foods.size()]);
				
				return orderInfo;
				
			}else{
				throw new Exception(tableID + "号餐台信息不存在");
			}
			
		}catch(ClassNotFoundException e){
			throw new Exception("餐台信息请求失败");
			
		}catch(SQLException e){
			throw new Exception("餐台信息请求失败");
			
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
