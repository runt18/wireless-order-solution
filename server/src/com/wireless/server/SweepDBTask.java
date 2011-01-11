package com.wireless.server;

import org.tiling.scheduling.SchedulerTask;

import com.wireless.protocol.Restaurant;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

/**
 * This sweep db task is designed to accomplish three goals.
 * 1 - Sweep the paid order record (the "total_price" isn't -1) from "order" table and its corresponding food information 
 *     from "order_food" table, if the order date is expired (exceed the "record_alive" in "restaurant" table).
 * 2 - Sweep the food information from "food" table, if this food does can be deleted.       
 * 	   When the restaurant client deletes a food from food menu, just update the food's enabled attribute
 *     to zero, not be deleted from the database at once,
 *     because there might be still some records associated with this food in "order_food" table.
 *     Since the food id is the foreign key to "order_food" table,
 *     delete the food which the "order_food" table still contains would cause the restraint problem.
 *     So we adopt the schedule task to implement this function. 
 * 3 - Sweep the table information from "table" table, if this table does can be deleted.
 *     When the restaurant client deletes a table, just update the table's enabled attribute to 
 *     zero, not be deleted from database at once,
 *     because there might be still some records associated with this table in "order" table.
 *     Since the table id is the foreign key to "order" table,
 *     delete the table which the "order" table still contains would cause the restraint problem.
 *     So we still adopt the same way as food to sweep the redundant table.
 * We would use this scheduled task to sweep the expired order record and
 * check to see which foods can be deleted every specific time (maybe 30 days)
 */
class SweepDBTask extends SchedulerTask {

	class RecAlive{
		int restaurantID = 0;
		long recordAlive = 0;
		RecAlive(int restaurantID, long recordAlive){
			this.restaurantID = restaurantID;
			this.recordAlive = recordAlive;
		}
	}
	
	/**
	 * Perform two actions.
	 * 1 - Sweep the expired orders
	 * 2 - Sweep the redundant food
	 */
	public void run() {

		//open the database
		Connection dbCon = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sep = System.getProperty("line.separator");
		String taskInfo = "Sweeper task starts on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new java.util.Date()) + sep;
		
		try {   
			
			Class.forName("com.mysql.jdbc.Driver");   
			dbCon = DriverManager.getConnection(WirelessSocketServer.url, 
					WirelessSocketServer.user, 
					WirelessSocketServer.password);
			
			stmt = dbCon.createStatement();						
			
			/* Task1 */
			ArrayList<RecAlive> recAlives = new ArrayList<RecAlive>();
			//get all the restaurant id and record alive 
			//except three reserved restaurant(root, idle and discarded),
			//and the record alive value equals 0
			String sql = "SELECT id, record_alive FROM " + WirelessSocketServer.database + 
						".restaurant WHERE id NOT IN (" + Restaurant.ADMIN + "," +
						Restaurant.IDLE + "," + Restaurant.DISCARD + ") AND record_alive <> 0";
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				recAlives.add(new RecAlive(rs.getInt("id"), rs.getLong("record_alive")));
			}
			
			//get the order id matches the restaurant id and its order record is expired from "order" table
			ArrayList<Long> expiredOrders = new ArrayList<Long>();
			for(int i = 0; i < recAlives.size(); i++){
				sql = "SELECT id FROM " + WirelessSocketServer.database + 
						".order WHERE restaurant_id=" + recAlives.get(i).restaurantID + 
						" AND (UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(order_date) > " + 
						recAlives.get(i).recordAlive + " AND total_price <> -1)";
				rs = stmt.executeQuery(sql);
				while(rs.next()){
					expiredOrders.add(new Long(rs.getLong("id")));
				}
			}
			
			//delete all the food records matches the order which has been expired from "order_food" table
			stmt.clearBatch();
			for(int i = 0; i < expiredOrders.size(); i++){
				sql = "DELETE FROM " + WirelessSocketServer.database + 
					  ".order_food WHERE order_id=" + expiredOrders.get(i).toString();
				stmt.addBatch(sql);
			}
			int count = 0;
			int[] rowsAffected = stmt.executeBatch();
			for(int i = 0; i < rowsAffected.length; i++){
				if(rowsAffected[i] >= 0)
					count += rowsAffected[i];
			}
			taskInfo += "info : " + count + " record(s) are deleted from \"order_food\" table" + sep;
			
			//delete all the expired order record from "order" table
			stmt.clearBatch();
			for(int i = 0; i < expiredOrders.size(); i++){
				sql = "DELETE FROM " + WirelessSocketServer.database + 
					  ".order WHERE id=" + expiredOrders.get(i).toString();
				stmt.addBatch(sql);
			}
			rowsAffected = stmt.executeBatch();		
			count = 0;
			for(int i = 0; i < rowsAffected.length; i++){
				if(rowsAffected[i] >= 0)
					count += rowsAffected[i];
			}
			taskInfo += "info : " + count + " record(s) are deleted from \"order\" table" + sep;
			
			/* Task2 */
			//get the canceled foods from "food" table, whose enabled attribute equals to zero
			ArrayList<Long> cancelFoods = new ArrayList<Long>();
			sql = "SELECT id FROM " + WirelessSocketServer.database + 
				  ".food WHERE enabled=0";
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				cancelFoods.add(new Long(rs.getLong("id")));
			}
			
			//check if the canceled food is contained in "order_food" table
			//if no related record exist, then delete this canceled food from "food" table
			count = 0;
			for(int i = 0; i < cancelFoods.size(); i++){
				sql = "SELECT COUNT(*) FROM " + WirelessSocketServer.database +
				      ".order_food WHERE food_id=" + cancelFoods.get(i).toString();
				rs = stmt.executeQuery(sql);
				if(rs.next()){
					if(rs.getInt(1) == 0){
						sql = "DELETE FROM " + WirelessSocketServer.database + 
							  ".food WHERE id=" + cancelFoods.get(i).toString();
						if(stmt.executeUpdate(sql) != 0){
							count++;
						}
					}
				}
			}
			taskInfo += "info : " + count + " record(s) are deleted from \"food\" table" + sep;
			
			/* Task3 */
			//get the canceled table from "table" table, whose enabled attribute equals to zero
			ArrayList<Long> cancelTables = new ArrayList<Long>();
			sql = "SELECT id FROM " + WirelessSocketServer.database + 
			  	".table WHERE enabled=0";
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				cancelTables.add(new Long(rs.getLong("id")));
			}
			//check if the canceled table is contained in "order" table
			//if no related record exist, then delete this table from "table" table
			count = 0;
			for(int i = 0; i < cancelTables.size(); i++){
				sql = "SELECT COUNT(*) FROM " + WirelessSocketServer.database +
					  ".order WHERE table_id=" + cancelTables.get(i).toString();
				rs = stmt.executeQuery(sql);
				if(rs.next()){
					if(rs.getInt(1) == 0){
						sql = "DELETE FROM " + WirelessSocketServer.database + 
						      ".table WHERE id=" + cancelTables.get(i).toString();
						if(stmt.executeUpdate(sql) != 0){
							count++;
						}
					}
				}
			}
			taskInfo += "info : " + count + " record(s) are deleted from \"table\" table" + sep;
			
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
				File logFile = new File("log/sweeper_db.log");
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


