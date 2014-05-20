package com.wireless.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

import org.tiling.scheduling.SchedulerTask;

import com.wireless.db.misc.SweepDB;


/**
 * This sweep db task is designed to accomplish the goals below.
 * 1 - Sweep the paid order record (the "total_price" is NOT NULL) from "order_history" table and its corresponding food information 
 *     from "order_food_history" table, if the order date is expired (exceed the "record_alive" in "restaurant" table).
 * We would use this scheduled task to sweep the expired order record and
 * check to see which foods can be deleted every specific time (maybe 30 days)
 */
public class SweepDBTask extends SchedulerTask {

	private final static String SEP = System.getProperty("line.separator");

	
	public void run() {		

		StringBuilder taskInfo = new StringBuilder();
			
		try {   
			
			taskInfo.append(SweepDB.exec());
			
		}catch(SQLException e){				
			taskInfo.append("error : ").append(e.getMessage()).append(SEP);
			e.printStackTrace();
			
		}finally{
			
			//append to the log file
			taskInfo.append("***************************************************************").append(SEP);
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
				logWriter.write(taskInfo.toString());
				logWriter.close();
			}catch(IOException e){}

		}

	}
}


