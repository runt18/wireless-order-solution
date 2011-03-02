package com.wireless.order;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import com.wireless.protocol.*;

public class Print {
	public static void exec(String token, int orderID, int[] printFunc) throws VerifyFault, PrintFault{
		Verify.exec(token);
		Connection dbCon = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {   
			Class.forName("com.mysql.jdbc.Driver");   
		
			dbCon = DriverManager.getConnection(Params.dbUrl, Params.dbUser, Params.dbPwd);   
			stmt = dbCon.createStatement();   		
			//set names to UTF-8
			stmt.execute("SET NAMES utf8");

			//get the table id and amount of customer
			String sql = "SELECT table_id, custom_num FROM " + Params.dbName + ".order WHERE id=" + orderID;
			rs = stmt.executeQuery(sql);
			short tableID = 0;
			int customNum = 0;
			if(rs.next()){
				tableID = (short)(rs.getLong("table_id") & 0x000000000000FFFF);
				customNum = rs.getInt("custom_num");
			}else{
				throw new PrintFault();
			}
			
			//get all the food's detail to this order
			sql = "SELECT food_id, order_count FROM " + Params.dbName + ".order_food WHERE order_id=" + orderID;
			rs = stmt.executeQuery(sql);
			ArrayList<Food> foods = new ArrayList<Food>();
			while(rs.next()){
				Food food = new Food();
				food.alias_id = (short)(rs.getLong("food_id") & 0x000000000000FFFF);
				food.setCount(new Float(rs.getFloat("order_count")));
				foods.add(food);
			}
			
			//generate the order using the food's info above
			Order reqOrder = new Order();
			reqOrder.tableID = tableID;
			reqOrder.customNum = customNum;
			reqOrder.foods = foods.toArray(new Food[foods.size()]);
			
		}catch(ClassNotFoundException e) { 
			throw new PrintFault();
			
		}catch(SQLException e){
			throw new PrintFault();
			
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
