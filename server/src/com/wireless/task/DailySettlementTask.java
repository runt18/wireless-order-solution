package com.wireless.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.tiling.scheduling.SchedulerTask;

import com.wireless.db.DailySettle;
import com.wireless.db.tasteRef.TasteRef;
import com.wireless.exception.BusinessException;
import com.wireless.server.WirelessSocketServer;

/**
 * 
 * @author Ying.Zhang
 *
 */
public class DailySettlementTask extends SchedulerTask{
	
	@Override
	public void run() {
		String sep = System.getProperty("line.separator");
		String taskInfo = "Daily settlement task starts on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new java.util.Date()) + sep;
		
		try {   
			
			//clean up the unprinted records
			synchronized(WirelessSocketServer.printLosses){
				WirelessSocketServer.printLosses.clear();
			}
			
			DailySettle.Result result = DailySettle.exec();		
					
			taskInfo += "info : " + result.totalOrder + " record(s) are moved from \"order\" to \"order_history\"" + sep;
			taskInfo += "info : " + result.totalOrderDetail + " record(s) are moved from \"order_food\" to \"order_food_history\"" + sep;
			taskInfo += "info : " + result.totalShift + " record(s) are moved from \"shift\" to \"shift_history\"" + sep;
			taskInfo += "info : " + 
						"maxium order id : " + result.maxOrderID + ", " +
						"maxium order food id : " + result.maxOrderFoodID + ", " +
						"maxium shift id : " + result.maxShiftID + sep;
			
			long beginTime = System.currentTimeMillis();
			TasteRef.exec();
			long elapsedTime = System.currentTimeMillis() - beginTime;
			
			taskInfo += "info : The calcation to smart taste reference takes up " + elapsedTime / 1000 + "sec." + sep; 
				
		}catch(SQLException e){
			taskInfo += "error : " + e.getMessage() + sep;
			e.printStackTrace();
			
		}catch(BusinessException e){
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
			
		}
	}

}
