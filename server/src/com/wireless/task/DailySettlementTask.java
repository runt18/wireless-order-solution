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
			taskInfo.append(DailySettleDao.exec()).append(sep);
			
			//Perform to smart taste calculation.
			taskInfo.append("info : " + TasteRefDao.exec()).append(sep);
			
			//Perform to food association.
			taskInfo.append("info : " + CalcFoodAssociationDao.exec()).append(sep);
			
			//Perform to calculate food statistics.
			taskInfo.append("info : " + CalcFoodStatisticsDao.exec()).append(sep);
			
			//Perform to calculate restaurant liveness.
			taskInfo.append("info : " + RestaurantDao.calcLiveness()).append(sep);
			
			//Perform to calculate member favor foods.
			taskInfo.append("info : " + MemberDao.calcFavorFoods()).append(sep);
			
			//Perform to calculate member recommended foods.
			taskInfo.append("info : " + MemberDao.calcRecommendFoods()).append(sep);
			
			//Perform to upgrade member level
			taskInfo.append("info : " + MemberDao.upgrade()).append(sep);
			
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

