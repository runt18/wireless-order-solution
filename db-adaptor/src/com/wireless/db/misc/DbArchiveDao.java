package com.wireless.db.misc;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.DBTbl;
import com.wireless.db.frontBusiness.DailySettleDao;
import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.shift.PaymentDao;
import com.wireless.db.shift.ShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;

public class DbArchiveDao {

	public static class ArchiveResult{
		
		private int elapsedTime;			//归档处理消耗的秒数
		private int shiftAmount;			//归档处理交班的记录数
		private int orderAmount;			//归档处理的账单数
		private int ofAmount;				//归档处理的order food数量
		private int tgAmount;				//归档处理的taste group数量
		private int mixedAmount;			//归档处理的混合结账账单数
		private int dailyAmount;			//归档处理的日结数量
		private int moAmount;				//归档处理member operation
		private int paymentAmount;			//归档处理的交款数量
		
		@Override
		public String toString(){
			
			final String sep = System.getProperty("line.separator");

			DBTbl dbTbl = new DBTbl(DateType.ARCHIVE);
			
			StringBuilder resultInfo = new StringBuilder();
			resultInfo.append("info : " + orderAmount + " record(s) are moved to \"").append(dbTbl.orderTbl).append("\"").append(sep);
			resultInfo.append("info : " + mixedAmount + " record(s) are moved to \"").append(dbTbl.mixedTbl).append("\"").append(sep);
			resultInfo.append("info : " + ofAmount + " record(s) are moved to \"").append(dbTbl.orderFoodTbl).append("\"").append(sep);
			resultInfo.append("info : " + tgAmount + " record(s) are moved to \"").append(dbTbl.tgTbl).append("\"").append(sep);
			resultInfo.append("info : " + paymentAmount + " record(s) are moved to \"").append(dbTbl.paymentTbl).append("\"").append(sep);
			resultInfo.append("info : " + shiftAmount + " record(s) are moved to \"").append(dbTbl.shiftTbl).append("\"").append(sep);
			resultInfo.append("info : " + dailyAmount + " record(s) are moved to \"").append(dbTbl.dailyTbl).append("\"").append(sep);
			resultInfo.append("info : " + moAmount + " record(s) are moved to \"").append(dbTbl.moTbl).append("\"").append(sep);
			resultInfo.append("info : The record movement takes " + elapsedTime + " sec.");
			
			return resultInfo.toString();
		}
	}
	
	public static ArchiveResult archive() throws SQLException, BusinessException{
		long beginTime = System.currentTimeMillis();
		
		ArchiveResult result = new ArchiveResult();
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			//dbCon.conn.setAutoCommit(false);
			for(Restaurant restaurant : RestaurantDao.getByCond(dbCon, null, null)){
				Staff staff = StaffDao.getAdminByRestaurant(dbCon, restaurant.getId());
				//Archive the expired order.
				OrderDao.ArchiveResult orderResult = OrderDao.archive4Expired(dbCon, staff);
				result.orderAmount += orderResult.orderAmount;
				result.ofAmount += orderResult.ofAmount;
				result.tgAmount += orderResult.tgAmount;
				result.mixedAmount += orderResult.mixedAmount;
				
				//Archive the expired payment. 
				result.paymentAmount += PaymentDao.archive4Expired(dbCon, staff);
				
				//Archive the expired shift.
				result.shiftAmount += ShiftDao.archive4Expired(dbCon, staff);
				
				//Archive the expired daily.
				result.dailyAmount += DailySettleDao.archive4Expired(dbCon, staff);
				
				//Archive the expired member operation.
				result.moAmount += MemberOperationDao.archive4Expired(dbCon, staff).getAmount();
			}
			//dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
			//dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		
		result.elapsedTime = ((int)(System.currentTimeMillis() - beginTime) / 1000);
		return result;
	}
	
}
