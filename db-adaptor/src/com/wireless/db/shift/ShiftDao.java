package com.wireless.db.shift;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DateType;

public class ShiftDao {
	
	/**
	 * Get current daily shift detail to today.
	 * @param dbCon
	 * 			The database connection
	 * @param staff
	 * 			The staff to perform this action
	 * @return the shift detail to today
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static ShiftDetail getTodayDaily(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getTodayDaily(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get current daily shift detail to today.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the shift detail to today
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static ShiftDetail getTodayDaily(DBCon dbCon, Staff staff) throws SQLException{
		
		//Get the latest off duty date from daily settle history, and make it as the on duty date to this daily shift
		String sql = " SELECT MAX(off_duty) FROM " +
					 Params.dbName + ".daily_settle_history " +
					 " WHERE " +
					 " restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		String onDuty;
		if(dbCon.rs.next()){
			Timestamp offDuty = dbCon.rs.getTimestamp(1);
			if(offDuty == null){
				onDuty = "2011-07-30 00:00:00";
			}else{
				onDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(offDuty);
			}
		}else{
			onDuty = "2011-07-30 00:00:00";
		}
		dbCon.rs.close();
		
		//Make the current date as the off duty date.
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		String offDuty = sdf.format(System.currentTimeMillis());
		
		return getByRange(dbCon, staff, new DutyRange(onDuty, offDuty), DateType.TODAY);
	}
	
	/**
	 * Get the current shift detail. 
	 * @param staff
	 *            the staff to perform this action
	 * @return the current shift detail 
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static ShiftDetail getCurrentShift(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getCurrentShift(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the current shift detail. 
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 *            the staff to perform this action
	 * @return the current shift detail 
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	private static ShiftDetail getCurrentShift(DBCon dbCon, Staff staff) throws SQLException{
		
		/**
		 * Get the latest off duty date from below.
		 * 1 - shift 
		 * 2 - shift_history
		 * 3 - daily_settle_history
		 * and make it as the on duty date to this duty shift
		 */
		String onDuty;
		String sql = "SELECT MAX(off_duty) FROM (" +
					 "SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id=" + staff.getRestaurantId() + " UNION " +
					 "SELECT off_duty FROM " + Params.dbName + ".shift_history WHERE restaurant_id=" + staff.getRestaurantId() + " UNION " +
					 "SELECT off_duty FROM " + Params.dbName + ".daily_settle_history WHERE restaurant_id=" + staff.getRestaurantId() +
					 ") AS all_off_duty";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			Timestamp offDuty = dbCon.rs.getTimestamp(1);
			if(offDuty == null){
				onDuty = "2011-07-30 00:00:00";
			}else{
				onDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(offDuty);
			}
		}else{
			onDuty = "2011-07-30 00:00:00";
		}
		dbCon.rs.close();
		
		/**
		 * Make the current date as the off duty date
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		String offDuty = sdf.format(System.currentTimeMillis());
		
		return getByRange(dbCon, staff, new DutyRange(onDuty, offDuty), DateType.TODAY);

	}
	
	/**
	 * Get the details to shift within the duty range
	 * @param staff
	 * 			the staff to request
	 * @param range
	 * 			the duty range
	 * @param queryType
	 * 			indicate which query type should use
	 * 			it is one of values below.
	 * 			- QUERY_TODAY
	 * 		    - QUERY_HISTORY
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static ShiftDetail getByRange(Staff staff, DutyRange range, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByRange(dbCon, staff, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the details to shift within the duty range.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to request
	 * @param range
	 * 			the duty range
	 * @param queryType
	 * 			indicate which query type should use
	 * 			it is one of values below.
	 * 			- QUERY_TODAY
	 * 		    - QUERY_HISTORY
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static ShiftDetail getByRange(DBCon dbCon, Staff staff, DutyRange range, DateType queryType) throws SQLException{
		
		ShiftDetail result = new ShiftDetail();
		result.setOnDuty(range.getOnDutyFormat());
		result.setOffDuty(range.getOffDutyFormat());
		
		//Calculate the general income
		result.setIncomeByPay(CalcBillStatisticsDao.calcIncomeByPayType(dbCon, staff, range, queryType));
		
		//Calculate the total & amount to erase price
		result.setEraseIncome(CalcBillStatisticsDao.calcErasePrice(dbCon, staff, range, queryType));
		//-----------------------------
		
		//Get the total & amount to discount price
		result.setDiscountIncome(CalcBillStatisticsDao.calcDiscountPrice(dbCon, staff, range, queryType));
		
		//Get the total & amount to gift price
		result.setGiftIncome(CalcBillStatisticsDao.calcGiftPrice(dbCon, staff, range, queryType));
		
		//Get the total & amount to cancel price
		result.setCancelIncome(CalcBillStatisticsDao.calcCancelPrice(dbCon, staff, range, queryType));
		
		//Get the total & amount to coupon price
		result.setCouponIncome(CalcBillStatisticsDao.calcCouponPrice(dbCon, staff, range, queryType));
		
		//Get the total & amount to repaid order
		result.setRepaidIncome(CalcBillStatisticsDao.calcRepaidPrice(dbCon, staff, range, queryType));
		
		//Get the total & amount to order with service
		result.setServiceIncome(CalcBillStatisticsDao.calcServicePrice(dbCon, staff, range, queryType));
		
		//Get the income by charge
		result.setIncomeByCharge(CalcBillStatisticsDao.calcIncomeByCharge(dbCon, staff, range, queryType));
		
		//Get the gift, discount & total to each department during this period.
		result.setDeptIncome(CalcBillStatisticsDao.calcIncomeByDept(dbCon, staff, range, null, queryType));
		
		return result;
	}
	
}
