package com.wireless.order;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import com.wireless.protocol.*;

public class Print {
	/**
	 * Connect to the socket server to print the receipt
	 * @param token the token retrieved from login action
	 * @param orderID the id of the order to print
	 * @param conf the configuration parameter to the print, the meaning to each bit is as below.<br>
	 *                [0] - PRINT_SYNC<br>
	 *                [1] - PRINT_ORDER_2<br>
	 *                [2] - PRINT_ORDER_DETAIL_2<br>
	 *                [3] - PRINT_RECEIPT_2<br>
	 *                [4..7] - Not Used	
	 * @throws VerifyFault throws if the token is invalid
	 * @throws PrintFault throws if any logic error occurred while performing print
	 */
	public static void exec(String token, int orderID, byte conf) throws VerifyFault, PrintFault{
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

			//get the total price, table id and amount of customer
			String sql = "SELECT total_price, table_id, custom_num FROM " + Params.dbName + ".order WHERE id=" + orderID;
			rs = stmt.executeQuery(sql);
			float totalPrice = 0;
			short tableID = 0;
			int customNum = 0;
			if(rs.next()){
				totalPrice = rs.getFloat("total_price");
				tableID = (short)(rs.getLong("table_id") & 0x000000000000FFFF);
				customNum = rs.getInt("custom_num");
			}else{
				throw new PrintFault(PrintFault.ORDER_NOT_EXIST);
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
			reqOrder.setTotalPrice(new Float(totalPrice));
			reqOrder.tableID = tableID;
			reqOrder.customNum = customNum;
			reqOrder.foods = foods.toArray(new Food[foods.size()]);
			
			//generate the request for print order
			ReqPrintOrder2 req = new ReqPrintOrder2(reqOrder, conf);
			
			ProtocolPackage resp = ServerConnector.instance().ask(req);
			if(resp.header.type != Type.ACK){
				throw new PrintFault(PrintFault.SOCKET_ERROR);
			}
			
		}catch(ClassNotFoundException e) { 
			throw new PrintFault(PrintFault.UNKNOWN);
			
		}catch(SQLException e){
			throw new PrintFault(PrintFault.DB_ERROR);
			
		}catch(IOException e){
			throw new PrintFault(PrintFault.SOCKET_ERROR);
			
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
