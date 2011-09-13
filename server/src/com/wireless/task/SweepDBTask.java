package com.wireless.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.tiling.scheduling.SchedulerTask;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.protocol.Restaurant;


/**
 * This sweep db task is designed to accomplish the goals below.
 * 1 - Sweep the paid order record (the "total_price" is NOT NULL) from "order_history" table and its corresponding food information 
 *     from "order_food_history" table, if the order date is expired (exceed the "record_alive" in "restaurant" table).
 * We would use this scheduled task to sweep the expired order record and
 * check to see which foods can be deleted every specific time (maybe 30 days)
 */
public class SweepDBTask extends SchedulerTask {


	class RecAlive{
		int restaurantID = 0;
		long recordAlive = 0;
		RecAlive(int restaurantID, long recordAlive){
			this.restaurantID = restaurantID;
			this.recordAlive = recordAlive;
		}
	}
	
	public SweepDBTask(){

	}
	
	public void run() {		

		String sep = System.getProperty("line.separator");
		String taskInfo = "Sweeper task starts on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new java.util.Date()) + sep;
		
		//open the database
		DBCon dbCon = new DBCon();
		
		try {   
			
			dbCon.connect();
			
			/* Task1 */
			ArrayList<RecAlive> recAlives = new ArrayList<RecAlive>();

			/**
			 * Get all the restaurant id and record alive except the reserved restaurants.
			 * Note that the record alive is zero means never expired.
			 */
			String sql = "SELECT id, record_alive FROM " + Params.dbName + 
						".restaurant WHERE id NOT IN (" + Restaurant.ADMIN + "," + Restaurant.IDLE + "," + Restaurant.DISCARD + "," + 
						Restaurant.RESERVED_1 + "," + Restaurant.RESERVED_2 + "," + Restaurant.RESERVED_3 + "," + Restaurant.RESERVED_4 + "," + 
						Restaurant.RESERVED_5 + "," + Restaurant.RESERVED_6 + "," + Restaurant.RESERVED_7 + 
						") AND record_alive <> 0";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				recAlives.add(new RecAlive(dbCon.rs.getInt("id"), dbCon.rs.getLong("record_alive")));
			}
			
			/**
			 * Get the order id matches the restaurant id and its order record is expired from "order_histroy" table
			 */
			ArrayList<Long> expiredOrders = new ArrayList<Long>();
			for(int i = 0; i < recAlives.size(); i++){
				sql = "SELECT id FROM " + Params.dbName + 
						".order_history WHERE restaurant_id=" + recAlives.get(i).restaurantID + 
						" AND (UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(order_date) > " + recAlives.get(i).recordAlive + 
						" AND total_price IS NOT NULL)";
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				while(dbCon.rs.next()){
					expiredOrders.add(new Long(dbCon.rs.getLong("id")));
				}
			}
			
			/**
			 * Delete all the records matches the order which has been expired from "temp_order_food_history" table.
			 */
			dbCon.stmt.clearBatch();
			for(int i = 0; i < expiredOrders.size(); i++){
				sql = "DELETE FROM " + Params.dbName + 
				  ".temp_order_food_history WHERE order_id=" + expiredOrders.get(i).toString();
				dbCon.stmt.addBatch(sql);				
			}
			int count = 0;
			int[] rowsAffected = dbCon.stmt.executeBatch();
			for(int i = 0; i < rowsAffected.length; i++){
				if(rowsAffected[i] >= 0)
					count += rowsAffected[i];
			}
			taskInfo += "info : " + count + " record(s) are deleted from \"temp_order_food_history\" table" + sep;
			
			/**
			 * Delete all the records matches the order which has been expired from "order_food_material_hisotry" table.
			 */
			dbCon.stmt.clearBatch();
			for(int i = 0; i < expiredOrders.size(); i++){
				sql = "DELETE FROM " + Params.dbName + 
					  ".order_food_material_history WHERE order_food_id IN(" + 
					  "SELECT id FROM " + Params.dbName + ".order_food_history WHERE order_id IN(" + 
					  "SELECT id FROM " + Params.dbName + ".order_history WHERE id=" + expiredOrders.get(i).toString() + "))";
				dbCon.stmt.addBatch(sql);			
			}
			count = 0;
			rowsAffected = dbCon.stmt.executeBatch();
			for(int i = 0; i < rowsAffected.length; i++){
				if(rowsAffected[i] >= 0)
					count += rowsAffected[i];
			}
			taskInfo += "info : " + count + " record(s) are deleted from \"order_food_material_history\" table" + sep;
			
			/**
			 * Delete all the food records matches the order which has been expired from "order_food_hisotry" table.
			 */
			dbCon.stmt.clearBatch();
			for(int i = 0; i < expiredOrders.size(); i++){
				sql = "DELETE FROM " + Params.dbName + 
					  ".order_food_history WHERE order_id=" + expiredOrders.get(i).toString();
				dbCon.stmt.addBatch(sql);			
			}
			count = 0;
			rowsAffected = dbCon.stmt.executeBatch();
			for(int i = 0; i < rowsAffected.length; i++){
				if(rowsAffected[i] >= 0)
					count += rowsAffected[i];
			}
			taskInfo += "info : " + count + " record(s) are deleted from \"order_food_history\" table" + sep;
			
			/**
			 * Delete all the expired order record from "order_history" table
			 */
			dbCon.stmt.clearBatch();
			for(int i = 0; i < expiredOrders.size(); i++){
				sql = "DELETE FROM " + Params.dbName + 
					  ".order_history WHERE id=" + expiredOrders.get(i).toString();
				dbCon.stmt.addBatch(sql);
			}
			rowsAffected = dbCon.stmt.executeBatch();		
			count = 0;
			for(int i = 0; i < rowsAffected.length; i++){
				if(rowsAffected[i] >= 0)
					count += rowsAffected[i];
			}
			taskInfo += "info : " + count + " record(s) are deleted from \"order_history\" table" + sep;
			
		}catch(SQLException e){
			taskInfo += "error : " + e.getMessage() + sep;
			e.printStackTrace();
			
		}finally{
			
			//disconnect the database
			dbCon.disconnect();
			
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

		}

	}
}


