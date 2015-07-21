package com.wireless.task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.tiling.scheduling.SchedulerTask;

import com.wireless.db.foodAssociation.CalcFoodAssociationDao;
import com.wireless.db.foodStatistics.CalcFoodStatisticsDao;
import com.wireless.db.frontBusiness.DailySettleDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.misc.DbArchiveDao;
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
	
	private String doPost(String url, String param) throws ClientProtocolException, IOException{
		
	    DefaultHttpClient client = new DefaultHttpClient();
	    
        HttpPost request = new HttpPost(url);
        if(param != null && !param.isEmpty()){
		    request.setEntity(new StringEntity(param, HTTP.UTF_8));
        }
        
        ByteArrayOutputStream bos = null;
        InputStream bis = null;
        byte[] buf = new byte[10240];

        String content = null;
        
	    try{

	        HttpResponse response = client.execute(request);
	
	        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	            bis = response.getEntity().getContent();
	
	            bos = new ByteArrayOutputStream();
	            int count;
	            while ((count = bis.read(buf)) != -1) {
	                bos.write(buf, 0, count);
	            }
	            bis.close();
	            content = bos.toString();
	            
	        } else {
	            throw new IOException("error code is " + response.getStatusLine().getStatusCode());
	        }
			
			return content;
	    }finally{
            if (bis != null) {
                try {
                    bis.close();// 最后要关闭BufferedReader
                } catch (Exception ignored) { }
            }
	    }
	}
	
	@Override
	public void run() {
		final String sep = System.getProperty("line.separator");
		final StringBuilder taskInfo = new StringBuilder(); 
		taskInfo.append("Daily settlement task starts on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new java.util.Date())).append(sep);
		
		try {   
			//Perform to db archive.
			taskInfo.append(DbArchiveDao.archive()).append(sep);
			
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
			
			//Notify wx-term to send the liveness & expired. 
			doPost("http://wx.e-tones.net/wx-term/WxRemind.do?dataSource=expired", "");
			doPost("http://wx.e-tones.net/wx-term/WxRemind.do?dataSource=liveness", "");
			
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
	
	public static void main(String[] args) throws Exception{
		new DailySettlementTask().doPost("http://wx.e-tones.net/wx-term/WxRemind.do?dataSource=liveness", "");
		new DailySettlementTask().doPost("http://wx.e-tones.net/wx-term/WxRemind.do?dataSource=expired", "");
	}
}

