package com.wireless.db.shift;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.IncomeByCancel;
import com.wireless.pojo.billStatistics.IncomeByCharge;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.IncomeByDiscount;
import com.wireless.pojo.billStatistics.IncomeByErase;
import com.wireless.pojo.billStatistics.IncomeByGift;
import com.wireless.pojo.billStatistics.IncomeByPay;
import com.wireless.pojo.billStatistics.IncomeByRepaid;
import com.wireless.pojo.billStatistics.IncomeByService;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DateType;

public class QueryShiftDao {
	
	/**
	 * Perform to query the shift information through now to last daily settlement.
	 * @param dbCon
	 * 			The database connection
	 * @return The daily detail record
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static ShiftDetail execDailybyNow(Staff staff) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execDailyByNow(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to query the shift information through now to last daily settlement.
	 * Note that the database base should be connected before invoking this method.
	 * @param dbCon
	 * 			The database connection
	 * @param staff
	 * 			The terminal
	 * @return The daily detail record
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static ShiftDetail execDailyByNow(DBCon dbCon, Staff staff) throws BusinessException, SQLException{
		/**
		 * Get the latest off duty date from daily settle history 
		 * and make it as the on duty date to this daily shift
		 */
		String onDuty;
		String sql = " SELECT MAX(off_duty) FROM " +
					 Params.dbName + ".daily_settle_history " +
					 " WHERE " +
					 " restaurant_id=" + staff.getRestaurantId();
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
		
		return exec(dbCon, staff, onDuty, offDuty, DateType.TODAY);
	}
	
	/**
	 * Perform to query the shift information through now to last shift.
	 * 
	 * @param staff
	 *            the staff 
	 * @return the shift detail information
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static ShiftDetail execByNow(Staff staff) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execByNow(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to query the shift information through now to last shift.
	 * Note that the database should be connected before invoking this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 *            the staff 
	 * @return the shift detail information
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static ShiftDetail execByNow(DBCon dbCon, Staff staff) throws BusinessException, SQLException{
		
		/**
		 * Get the latest off duty date from the tables below.
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
		
		return exec(dbCon, staff, onDuty, offDuty, DateType.TODAY);

	}
	
	public static ShiftDetail execDailySettleByNow(Staff staff) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		dbCon.connect();
		
		String onDuty;
		String sql = "SELECT MAX(off_duty) FROM (" +
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
		
		return exec(dbCon, staff, onDuty, offDuty, DateType.TODAY);

	}
	
	/**
	 * Generate the details to shift within the on & off duty date.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to request
	 * @param onDuty
	 * 			the date to be on duty
	 * @param offDuty
	 * 			the date to be off duty
	 * @param queryType
	 * 			indicate which query type should use
	 * 			it is one of values below.
	 * 			- QUERY_TODAY
	 * 		    - QUERY_HISTORY
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static ShiftDetail exec(Staff staff, String onDuty, String offDuty, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, staff, onDuty, offDuty, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Generate the details to shift within the on & off duty date.
	 * Note that database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to request
	 * @param onDuty
	 * 			the date to be on duty
	 * @param offDuty
	 * 			the date to be off duty
	 * @param queryType
	 * 			indicate which query type should use
	 * 			it is one of values below.
	 * 			- QUERY_TODAY
	 * 		    - QUERY_HISTORY
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static ShiftDetail exec(DBCon dbCon, Staff staff, String onDuty, String offDuty, DateType queryType) throws SQLException{
		
		ShiftDetail result = new ShiftDetail();
		result.setOnDuty(onDuty);
		result.setOffDuty(offDuty);
		
		//Calculate the general income
		IncomeByPay incomeByPay = CalcBillStatisticsDao.calcIncomeByPayType(dbCon, staff, new DutyRange(onDuty, offDuty), queryType);
		
		result.setCashAmount(incomeByPay.getCashAmount());
		result.setCashTotalIncome(incomeByPay.getCashIncome());
		result.setCashActualIncome(incomeByPay.getCashActual());
				
		result.setCreditCardAmount(incomeByPay.getCreditCardAmount());
		result.setCreditTotalIncome(incomeByPay.getCreditCardIncome());
		result.setCreditActualIncome(incomeByPay.getCreditCardActual());
				
		result.setMemberCardAmount(incomeByPay.getMemberCardAmount());
		result.setMemberTotalIncome(incomeByPay.getMemberCardIncome());
		result.setMemberActualIncome(incomeByPay.getMemberCardActual());
				
		result.setHangAmount(incomeByPay.getHangAmount());
		result.setHangTotalIncome(incomeByPay.getHangIncome());
		result.setHangActualIncome(incomeByPay.getHangActual());
				
		result.setSignAmount(incomeByPay.getSignAmount());
		result.setSignTotalIncome(incomeByPay.getSignIncome());
		result.setSignActualIncome(incomeByPay.getSignActual());
		
		result.setOrderAmount(incomeByPay.getOrderAmount());
		result.setTotalActual(incomeByPay.getTotalActual());
		
		//Calculate the total & amount to erase price
		IncomeByErase incomeByErase = CalcBillStatisticsDao.calcErasePrice(dbCon, staff, new DutyRange(onDuty, offDuty), queryType);
		result.setEraseAmount(incomeByErase.getEraseAmount());
		result.setEraseIncome(incomeByErase.getTotalErase());
		//-----------------------------
		
		//Get the total & amount to discount price
		IncomeByDiscount incomeByDiscount = CalcBillStatisticsDao.calcDiscountPrice(dbCon, staff, new DutyRange(onDuty, offDuty), queryType);
		result.setDiscountAmount(incomeByDiscount.getDiscountAmount());
		result.setDiscountIncome(incomeByDiscount.getTotalDiscount());	

		
		//Get the total & amount to gift price
		IncomeByGift incomeByGift = CalcBillStatisticsDao.calcGiftPrice(dbCon, staff, new DutyRange(onDuty, offDuty),  queryType);
		result.setGiftAmount(incomeByGift.getGiftAmount());
		result.setGiftIncome(incomeByGift.getTotalGift());
		
		//Get the total & amount to cancel price
		IncomeByCancel incomeByCancel = CalcBillStatisticsDao.calcCancelPrice(dbCon, staff, new DutyRange(onDuty, offDuty), queryType);
		result.setCancelAmount(incomeByCancel.getCancelAmount());
		result.setCancelIncome(incomeByCancel.getTotalCancel());
		
		//Get the total & amount to repaid order
		IncomeByRepaid incomeByRepaid = CalcBillStatisticsDao.calcRepaidPrice(dbCon, staff, new DutyRange(onDuty, offDuty), queryType);
		result.setPaidAmount(incomeByRepaid.getRepaidAmount());
		result.setPaidIncome(incomeByRepaid.getTotalRepaid());
		
		//Get the total & amount to order with service
		IncomeByService incomeByService = CalcBillStatisticsDao.calcServicePrice(dbCon, staff, new DutyRange(onDuty, offDuty), queryType);
		result.setServiceAmount(incomeByService.getServiceAmount());
		result.setServiceIncome(incomeByService.getTotalService());
		
		//Get the charge income by both cash and credit card
		IncomeByCharge incomeByCharge = CalcBillStatisticsDao.calcIncomeByCharge(dbCon, staff, new DutyRange(onDuty, offDuty), queryType);
		result.setChargeByCash(incomeByCharge.getCash());
		result.setChargeByCreditCard(incomeByCharge.getCreditCard());
		
		//Get the gift, discount & total to each department during this period.
		List<IncomeByDept> incomeByDept = CalcBillStatisticsDao.calcIncomeByDept(dbCon, staff, new DutyRange(onDuty, offDuty), null, queryType);
		result.setDeptIncome(incomeByDept);
		
		return result;
	}
	
}
