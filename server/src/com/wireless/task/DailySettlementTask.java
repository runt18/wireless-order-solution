package com.wireless.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import org.tiling.scheduling.SchedulerTask;

/**
 * 
 * @author Ying.Zhang
 *
 */
public class DailySettlementTask extends SchedulerTask{

	private String _dbUrl = null;
	private String _dbName = null;
	private String _dbUser = null;
	private String _dbPwd = null;
	
	public DailySettlementTask(String url, String db, String user, String pwd){
		_dbUrl = url;
		_dbName = db;
		_dbUser = user;
		_dbPwd = pwd;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Connection dbCon = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sep = System.getProperty("line.separator");
		String taskInfo = "Daily settlement task starts on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new java.util.Date()) + sep;
		
		try {   
			
			Class.forName("com.mysql.jdbc.Driver");   
			dbCon = DriverManager.getConnection(_dbUrl, _dbUser, _dbPwd);
			
			stmt = dbCon.createStatement();		
			
			//get the count to orders which have been paid
			int nOrders = 0;
			String sql = "SELECT count(*) FROM " + _dbName + ".order WHERE total_price IS NOT NULL";
			rs = stmt.executeQuery(sql);
			if(rs.next()){
				nOrders = rs.getInt(1);
			}
			rs.close();
			
			//get the count to order details which have been paid 
			int nOrderDetails = 0;
			sql = "SELECT count(*) FROM " + _dbName + ".order_food WHERE order_id IN (" +
			  	  "SELECT id FROM " + _dbName + ".order WHERE total_price IS NOT NULL)";
			rs = stmt.executeQuery(sql);
			if(rs.next()){
				nOrderDetails = rs.getInt(1);				
			}
			rs.close();
			
			final String orderItem = "`id`, `restaurant_id`,`order_date`, `total_price`, `total_price_2`, `custom_num`," + 
									"`waiter`,`type`, `member_id`, `member`,`terminal_pin`, `terminal_model`, `table_id`";
			
			final String orderFoodItem = "`id`,`order_id`, `food_id`, `order_date`, `order_count`," + 
										"`unit_price`,`name`, `food_status`, `taste`,`taste_price`,`taste_id`,`discount`,`kitchen`,`comment`,`waiter`";
			
			stmt.clearBatch();
			//move the order have been paid from "order" to "order_history"
			sql = "INSERT INTO " + _dbName + ".order_history (" + orderItem + ") " + 
				  "SELECT " + orderItem + " FROM " + _dbName + ".order WHERE total_price IS NOT NULL";
			stmt.addBatch(sql);
			
			//move the paid order details from "order_food" to "order_food_history" 
			sql = "INSERT INTO " + _dbName + ".order_food_history (" + orderFoodItem + ") " +
				  "SELECT " + orderFoodItem + " FROM " + _dbName + ".order_food WHERE order_id IN (" +
				  "SELECT id FROM " + _dbName + ".order WHERE total_price IS NOT NULL)";
			stmt.addBatch(sql);
			
			//delete the order details which have been paid from "order_food"
			sql = "DELETE FROM " + _dbName + ".order_food WHERE order_id IN (" +
				  "SELECT id FROM " + _dbName + ".order WHERE total_price IS NOT NULL)";
			stmt.addBatch(sql);
			
			//delete the order which have been paid from "order"
			sql = "DELETE FROM " + _dbName + ".order WHERE total_price IS NOT NULL";
			stmt.addBatch(sql);
			stmt.executeBatch();
			
			taskInfo += "info : " + nOrders + " record(s) are moved from \"order\" to \"order_history\"" + sep;
			taskInfo += "info : " + nOrderDetails + " record(s) are moved from \"order_food\" to \"order_food_history\"" + sep;
			
		}catch(ClassNotFoundException e) { 
			taskInfo += "error : " + e.getMessage() + sep;
			e.printStackTrace();   
			
		}catch(SQLException e){
			taskInfo += "error : " + e.getMessage() + sep;
			e.printStackTrace();
			
		}finally{
			
			//append to the log file
			taskInfo += "***************************************************************" + sep;
			try{
				File parent = new File("log/");
				if(!parent.exists()){
					parent.mkdir();
				}
				File logFile = new File("log/daily_settlement.log");
				if(!logFile.exists()){
					logFile.createNewFile();
				}
				FileWriter logWriter = new FileWriter(logFile, true);
				logWriter.write(taskInfo);
				logWriter.close();
			}catch(IOException e){}
			
			try{
				if(rs != null)
					rs.close();
				if(stmt != null)
					stmt.close();
				if(dbCon != null)
					dbCon.close();
			}catch(SQLException e){}
		}
	}

}
