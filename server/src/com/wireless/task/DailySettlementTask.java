package com.wireless.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.tiling.scheduling.SchedulerTask;

import com.wireless.db.foodAssociation.CalcFoodAssociationDao;
import com.wireless.db.foodStatistics.CalcFoodStatisticsDao;
import com.wireless.db.frontBusiness.DailySettleDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.orderMgr.TasteGroupDao;
import com.wireless.db.oss.OssImageDao;
import com.wireless.db.printScheme.PrintLossDao;
import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.sms.VerifySMSDao;
import com.wireless.db.system.BillBoardDao;
import com.wireless.db.tasteRef.TasteRefDao;
import com.wireless.db.token.TokenDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.exception.BusinessException;

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
			
			//Clean up the unused taste records
			taskInfo.append("info : " + TasteGroupDao.cleanup()).append(sep);
			
			//Clean up the unprinted records
			taskInfo.append("info : " + PrintLossDao.cleanup() + " print loss(es) are removed.").append(sep);

			//Clean up all the verification SMS record
			taskInfo.append("info : " + VerifySMSDao.deleteAll() + " verification SMS record(s) are removed.").append(sep);
			
			//Perform daily settlement.
			taskInfo.append(DailySettleDao.auto()).append(sep);
			
			//Perform to smart taste calculation.
			taskInfo.append("info : " + TasteRefDao.exec()).append(sep);
			
			//Perform to food association.
			taskInfo.append("info : " + CalcFoodAssociationDao.exec()).append(sep);
			
			//Perform to calculate food statistics.
			taskInfo.append("info : " + CalcFoodStatisticsDao.exec()).append(sep);
			
			//Perform to calculate restaurant liveness.
			taskInfo.append("info : " + RestaurantDao.calcLiveness()).append(sep);
			
			//Perform to calculate restaurant expired.
			taskInfo.append("info : " + RestaurantDao.calcExpired()).append(sep);
			
			//Perform to calculate member favor foods.
			taskInfo.append("info : " + MemberDao.calcFavorFoods()).append(sep);
			
			//Perform to calculate member recommended foods.
			taskInfo.append("info : " + MemberDao.calcRecommendFoods()).append(sep);
			
			//Perform to calculate the promotion and coupon status.
			taskInfo.append("info : " + PromotionDao.calcStatus()).append(sep);
			
			//Perform to cleanup the single oss images.
			taskInfo.append("info : " + OssImageDao.cleanup()).append(sep);
			
			//Perform to cleanup the wx orders.
			taskInfo.append("info : " + WxOrderDao.cleanup()).append(sep);
			
			//Perform to cleanup the tokens.
			taskInfo.append("info : " + TokenDao.cleanup()).append(sep);
			
			//Perform to cleanup the expired bill boards.
			taskInfo.append("info : " + BillBoardDao.cleanup()).append(sep);
			
		}catch(SQLException | BusinessException e){
			taskInfo.append("error : " + e.getMessage()).append(sep);
			e.printStackTrace();
			
		}catch(Exception e){
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

