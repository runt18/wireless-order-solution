package com.wireless.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.tiling.scheduling.SchedulerTask;

import com.wireless.db.SweepDB;


/**
 * This sweep db task is designed to accomplish the goals below.
 * 1 - Sweep the paid order record (the "total_price" is NOT NULL) from "order_history" table and its corresponding food information 
 *     from "order_food_history" table, if the order date is expired (exceed the "record_alive" in "restaurant" table).
 * We would use this scheduled task to sweep the expired order record and
 * check to see which foods can be deleted every specific time (maybe 30 days)
 */
public class SweepDBTask extends SchedulerTask {
	
	public void run() {		

		String sep = System.getProperty("line.separator");
		String taskInfo = "Sweeper task starts on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new java.util.Date()) + sep;
			
		try {   
			
			SweepDB.Result result = SweepDB.exec();
			
			taskInfo += "info : " + result.totalExpiredOrderDetail + " record(s) are deleted from \"order_food_history\" table" + sep;
			taskInfo += "info : " + result.totalExpiredOrder + " record(s) are deleted from \"order_history\" table" + sep;
			taskInfo += "info : " + result.totalExpiredShift + " record(s) are deleted from \"shift_history\" table" + sep;
			taskInfo += "info : " + result.totalExpiredDailySettle + " record(s) are deleted from \"daily_settle_history\" table" + sep;

			
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

		}

	}
}


