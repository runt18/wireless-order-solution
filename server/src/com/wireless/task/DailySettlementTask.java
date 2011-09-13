package com.wireless.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.tiling.scheduling.SchedulerTask;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.protocol.Restaurant;
import com.wireless.server.WirelessSocketServer;

/**
 * 
 * @author Ying.Zhang
 *
 */
public class DailySettlementTask extends SchedulerTask{


	
	public DailySettlementTask(){

	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		DBCon dbCon = new DBCon();
		String sep = System.getProperty("line.separator");
		String taskInfo = "Daily settlement task starts on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new java.util.Date()) + sep;
		
		try {   
			
			//clean up the unprinted records
			synchronized(WirelessSocketServer.printLosses){
				WirelessSocketServer.printLosses.clear();
			}
			
			dbCon.connect();
			
			//get the count to orders which have been paid
			int nOrders = 0;
			String sql = "SELECT count(*) FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				nOrders = dbCon.rs.getInt(1);
			}
			dbCon.rs.close();
			
			//get the count to order details which have been paid 
			int nOrderDetails = 0;
			sql = "SELECT count(*) FROM " + Params.dbName + ".order_food WHERE order_id IN (" +
			  	  "SELECT id FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL)";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				nOrderDetails = dbCon.rs.getInt(1);				
			}
			dbCon.rs.close();
			
			//get the amount of orders to move to "temp_order_food_history"
			final String tempOrder = "FROM " + Params.dbName + ".`order_food` WHERE order_id IN ( " +
			  					"SELECT id FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL)" +
			  					"GROUP BY " + 
			  					Params.dbName + ".order_food.order_id," +
			  					Params.dbName + ".order_food.food_id," + 
			  					Params.dbName + ".order_food.taste_id," +
			  					Params.dbName + ".order_food.taste_id2," + 
			  					Params.dbName + ".order_food.taste_id3 " +
			  					"HAVING (SUM(" + Params.dbName + ".`order_food`.`order_count`) > 0)";
			int nTempOrders = 0;
			sql = "SELECT COUNT(*) FROM(SELECT id " + tempOrder + ") aa";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				nTempOrders = dbCon.rs.getInt(1);
			}
			dbCon.rs.close();
			
			final String orderItem = "`id`, `restaurant_id`,`order_date`, `total_price`, `total_price_2`, `custom_num`," + 
									"`waiter`, `type`, `discount_type`,`category`, `member_id`, `member`,`terminal_pin`, `terminal_model`, `table_id`, " +
									"`table_name`, `table2_id`, `table2_name`, `service_rate`, `comment`";
			
			final String orderFoodItem = "`id`,`order_id`, `food_id`, `order_date`, `order_count`," + 
										"`unit_price`,`name`, `food_status`, `taste`,`taste_price`," +
										"`taste_id`,`taste_id2`,`taste_id3`,`discount`,`kitchen`,`comment`,`waiter`";
			
			dbCon.stmt.clearBatch();
			//move the order have been paid from "order" to "order_history"
			sql = "INSERT INTO " + Params.dbName + ".order_history (" + orderItem + ") " + 
				  "SELECT " + orderItem + " FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL";
			dbCon.stmt.addBatch(sql);
			
			//move the paid order details from "order_food" to "order_food_history" 
			sql = "INSERT INTO " + Params.dbName + ".order_food_history (" + orderFoodItem + ") " +
				  "SELECT " + orderFoodItem + " FROM " + Params.dbName + ".order_food WHERE order_id IN (" +
				  "SELECT id FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL)";
			dbCon.stmt.addBatch(sql);
			
			//move the details from "order_food" to "temp_order_food_history"
			sql = "INSERT INTO " + Params.dbName + ".temp_order_food_history(order_id, food_id, taste_id, taste_id2, taste_id3, " +
				  "`name`, taste, order_count, unit_price, taste_price, discount, food_status, kitchen, waiter) " + 
				  "SELECT " +
				  Params.dbName + ".`order_food`.`order_id` AS `order_id`," +
				  Params.dbName + ".`order_food`.`food_id` AS `food_id`," +
				  Params.dbName + ".`order_food`.`taste_id2` AS `taste_id`," +
				  Params.dbName + ".`order_food`.`taste_id3` AS `taste_id2`," +
				  Params.dbName + ".`order_food`.`taste_id` AS `taste_id3`," +
				  Params.dbName + ".`order_food`.`name` AS `name`," +
				  Params.dbName + ".`order_food`.`taste` AS `taste`," + 
				  "sum(`wireless_order_db`.`order_food`.`order_count`) AS `order_count`," +
				  "max(`wireless_order_db`.`order_food`.`unit_price`) AS `unit_price`," +
				  "max(`wireless_order_db`.`order_food`.`taste_price`) AS `taste_price`," +
				  "max(`wireless_order_db`.`order_food`.`discount`) AS `discount`," +
				  "max(`wireless_order_db`.`order_food`.`food_status`) AS `food_status`," +
				  "max(`wireless_order_db`.`order_food`.`kitchen`) AS `kitchen`," +
				  "max(`wireless_order_db`.`order_food`.`waiter`) AS `waiter` " +
				  tempOrder;
			dbCon.stmt.addBatch(sql);
			
			//delete the order details which have been paid from "order_food"
			sql = "DELETE FROM " + Params.dbName + ".order_food WHERE order_id IN (" +
				  "SELECT id FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL)";
			dbCon.stmt.addBatch(sql);
			
			//delete the order which have been paid from "order"
			sql = "DELETE FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL";
			dbCon.stmt.addBatch(sql);
			
			//delete the order_food record to root
			sql = "DELETE FROM " + Params.dbName + ".order_food WHERE order_id IN (SELECT id FROM " + 
				  Params.dbName + ".order WHERE restaurant_id=" + Restaurant.ADMIN + ")";
			dbCon.stmt.addBatch(sql);
			
			//delete the order record to root
			sql = "DELETE FROM " + Params.dbName + ".order WHERE restaurant_id=" + Restaurant.ADMIN;
			dbCon.stmt.addBatch(sql);			
			
			dbCon.stmt.executeBatch();
			
			//get the max order id from both order and order_history
			int maxOrderID = 0;
			sql = "SELECT MAX(`id`) + 1 FROM (" + "SELECT id FROM " + Params.dbName + 
				  ".order UNION SELECT id FROM " + Params.dbName + 
				  ".order_history) AS all_order";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				maxOrderID = dbCon.rs.getInt(1);
				//insert a order record with the max order id to root
				sql = "INSERT INTO " + Params.dbName + ".order (`id`, `restaurant_id`, `order_date`) VALUES (" + 
					  maxOrderID + ", " +
					  Restaurant.ADMIN + ", " +
					  0 +
					  ")";
				dbCon.stmt.execute(sql);
			}
			dbCon.rs.close();
			
			
			//get the max order_food id from both order_food and order_food_history
			int maxOrderFoodID = 0;
			sql = "SELECT MAX(`id`) + 1 FROM (SELECT id FROM " + Params.dbName +
				  ".order_food UNION SELECT id FROM " + Params.dbName +
				  ".order_food_history) AS all_order";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				maxOrderFoodID = dbCon.rs.getInt(1);
				//insert a order_food record with the max order_food id to root
				sql = "INSERT INTO " + Params.dbName + ".order_food (`id`, `order_id`, `order_date`) VALUES (" +
					  maxOrderFoodID + ", " +
					  maxOrderID + ", " +
					  0 +
					  ")";
				dbCon.stmt.execute(sql);
			}
			dbCon.rs.close();
			
			//update all terminal gift amount to zero
			sql = "UPDATE " + Params.dbName + ".terminal SET gift_amount=0";
			dbCon.stmt.executeUpdate(sql);			
					
			taskInfo += "info : " + nOrders + " record(s) are moved from \"order\" to \"order_history\"" + sep;
			taskInfo += "info : " + nTempOrders + " record(s) are moved from \"order_food\" to \"temp_order_food_history\"" + sep;
			taskInfo += "info : " + nOrderDetails + " record(s) are moved from \"order_food\" to \"order_food_history\"" + sep;
			taskInfo += "info : " + "maxium order id : " + maxOrderID + ", maxium order food id : " + maxOrderFoodID + sep;
			
			
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
			
			dbCon.disconnect();
		}
	}

}
