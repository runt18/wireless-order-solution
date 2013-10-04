package com.wireless.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.tiling.scheduling.SchedulerTask;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.foodAssociation.CalcFoodAssociationDao;
import com.wireless.db.foodStatistics.CalcFoodStatisticsDao;
import com.wireless.db.frontBusiness.DailySettleDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.tasteRef.TasteRefDao;
import com.wireless.exception.BusinessException;
import com.wireless.server.PrinterLosses;

/**
 * 
 * @author Ying.Zhang
 *
 */
public class DailySettlementTask extends SchedulerTask{
	
	@Override
	public void run() {
		final String sep = System.getProperty("line.separator");
		final StringBuilder taskInfo = new StringBuilder(); 
		taskInfo.append("Daily settlement task starts on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new java.util.Date())).append(sep);
		
		try {   
			
			//Clean up the unprinted records
			PrinterLosses.instance().clear();
			
			//Perform daily settlement.
			DailySettleDao.Result result = DailySettleDao.exec();		
					
			taskInfo.append("info : " + result.getTotalOrder() + " record(s) are moved from \"order\" to \"order_history\"").append(sep);
			taskInfo.append("info : " + result.getTotalOrderDetail() + " record(s) are moved from \"order_food\" to \"order_food_history\"").append(sep);
			taskInfo.append("info : " + result.getTotalShift() + " record(s) are moved from \"shift\" to \"shift_history\"").append(sep);
			taskInfo.append("info : " + 
							"maxium order id : " + result.getMaxOrderId() + ", " +
							"maxium order food id : " + result.getMaxOrderFoodId() + ", " +
							"maxium shift id : " + result.getMaxShiftId()).append(sep);
			
			//Perform to smart taste calculation.
			long beginTime = System.currentTimeMillis();
			TasteRefDao.exec();
			long elapsedTime = System.currentTimeMillis() - beginTime;
			
			taskInfo.append("info : The calculation to smart taste reference takes " + elapsedTime / 1000 + " sec.").append(sep);
			
			//Perform to food association.
			beginTime = System.currentTimeMillis();
			CalcFoodAssociationDao.exec();
			elapsedTime = System.currentTimeMillis() - beginTime;
			
			taskInfo.append("info : The calculation to food association takes " + elapsedTime / 1000 + " sec.").append(sep);
			
			//Perform to calculate food statistics.
			beginTime = System.currentTimeMillis();
			int nRows = CalcFoodStatisticsDao.exec();
			elapsedTime = System.currentTimeMillis() - beginTime;
			
			taskInfo.append("info : The calculation to " + nRows + " food's statistics takes " + elapsedTime / 1000 + " sec.").append(sep);
			
			//Perform to calculate restaurant liveness.
			beginTime = System.currentTimeMillis();
			RestaurantDao.calcLiveness();
			elapsedTime = System.currentTimeMillis() - beginTime;
			
			taskInfo.append("info : The calculation to restaurant liveness takes " + elapsedTime / 1000 + " sec.").append(sep);
			
			//Perform to calculate member favor foods.
			beginTime = System.currentTimeMillis();
			MemberDao.calcFavorFoods();
			elapsedTime = System.currentTimeMillis() - beginTime;
			
			taskInfo.append("info : The calculation to member favor foods takes " + elapsedTime / 1000 + " sec.").append(sep);
			
			//Perform to calculate member recommended foods.
			beginTime = System.currentTimeMillis();
			MemberDao.calcRecommendFoods();
			elapsedTime = System.currentTimeMillis() - beginTime;
			
			taskInfo.append("info : The calculation to member recommended foods takes " + elapsedTime / 1000 + " sec.").append(sep);
			
		}catch(SQLException e){
			taskInfo.append("error : " + e.getMessage()).append(sep);
			e.printStackTrace();
			
		}catch(BusinessException e){
			taskInfo.append("error : " + e.getMessage()).append(sep);
			e.printStackTrace();
			
		}finally{
			
			//append to the log file
			taskInfo.append("***************************************************************").append(sep);
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
				logWriter.write(taskInfo.toString());
				logWriter.close();
			}catch(IOException e){}
			
		}
	}
}

