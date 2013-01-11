package com.wireless.db.shift;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.db.billStatistics.CalcBillStatistics;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.IncomeByCancel;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.IncomeByDiscount;
import com.wireless.pojo.billStatistics.IncomeByErase;
import com.wireless.pojo.billStatistics.IncomeByGift;
import com.wireless.pojo.billStatistics.IncomeByPay;
import com.wireless.pojo.billStatistics.IncomeByRepaid;
import com.wireless.pojo.billStatistics.IncomeByService;
import com.wireless.protocol.Terminal;

public class QueryShiftDao {
	
	public final static int QUERY_TODAY = CalcBillStatistics.QUERY_TODAY;
	public final static int QUERY_HISTORY = CalcBillStatistics.QUERY_HISTORY;
	
	public static class Result{
		public String onDuty;			//开始时间
		public String offDuty;			//结束时间
		
		public int orderAmount;			//总账单数
		
		public int cashAmount;			//现金账单数
		public float cashIncome;		//现金金额
		public float cashIncome2;		//现金实收
		
		public int creditCardAmount;	//刷卡账单数
		public float creditCardIncome;	//刷卡金额
		public float creditCardIncome2;	//刷卡实收
		
		public int memeberCardAmount;	//会员卡账单数
		public float memberCardIncome;	//会员卡金额
		public float memberCardIncome2;	//会员卡实收
		
		public int signAmount;			//签单账单数
		public float signIncome;		//签单金额
		public float signIncome2;		//签单实收
		
		public int hangAmount;			//挂账账单数
		public float hangIncome;		//挂账金额
		public float hangIncome2;		//挂账实收
		
		public float totalActual;		//合计实收金额
		
		public int discountAmount;		//折扣账单数
		public float discountIncome;	//合计折扣金额
		
		public int giftAmount;			//赠送账单数
		public float giftIncome;		//合计赠送金额
		
		public int cancelAmount;		//退菜账单数
		public float cancelIncome;		//合计退菜金额
		
		public int serviceAmount;		//服务费账单数
		public float serviceIncome;		//服务费金额
		
		public int paidAmount;			//反结帐账单数
		public float paidIncome;		//反结帐金额
		
		public int eraseAmount;			//抹数账单数
		public float eraseIncome;		//抹数金额
		
		public List<IncomeByDept> deptIncome;	//所有部门营业额
	}
	
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
	public static Result execDailybyNow(Terminal term) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execDailyByNow(dbCon, term);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to query the shift information through now to last daily settlement.
	 * Note that the database base should be connected before invoking this method.
	 * @param dbCon
	 * 			The database connection
	 * @param term
	 * 			The terminal
	 * @return The daily detail record
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Result execDailyByNow(DBCon dbCon, Terminal term) throws BusinessException, SQLException{
		/**
		 * Get the latest off duty date from daily settle history 
		 * and make it as the on duty date to this daily shift
		 */
		String onDuty;
		String sql = " SELECT MAX(off_duty) FROM " +
					 Params.dbName + ".daily_settle_history " +
					 " WHERE " +
					 " restaurant_id=" + term.restaurantID;
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
		
		return exec(dbCon, term, onDuty, offDuty, CalcBillStatistics.QUERY_TODAY);
	}
	
	/**
	 * Perform to query the shift information through now to last shift.
	 * 
	 * @param pin
	 *            the pin to this terminal
	 * @param model
	 *            the model to this terminal
	 * @return the shift detail information
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Result execByNow(long pin, short model) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execByNow(dbCon, pin, model);
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
	 * @param pin
	 *            the pin to this terminal
	 * @param model
	 *            the model to this terminal
	 * @return the shift detail information
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Result execByNow(DBCon dbCon, long pin, short model) throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(dbCon, pin, model);
		
		/**
		 * Get the latest off duty date from the tables below.
		 * 1 - shift 
		 * 2 - shift_history
		 * 3 - daily_settle_history
		 * and make it as the on duty date to this duty shift
		 */
		String onDuty;
		String sql = "SELECT MAX(off_duty) FROM (" +
					 "SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id=" + term.restaurantID + " UNION " +
					 "SELECT off_duty FROM " + Params.dbName + ".shift_history WHERE restaurant_id=" + term.restaurantID + " UNION " +
					 "SELECT off_duty FROM " + Params.dbName + ".daily_settle_history WHERE restaurant_id=" + term.restaurantID +
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
		
		return exec(dbCon, term, onDuty, offDuty, CalcBillStatistics.QUERY_TODAY);

	}
	
	public static Result execDailySettleByNow(long pin) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		dbCon.connect();
		
		Terminal term = VerifyPin.exec(dbCon, pin, Terminal.MODEL_STAFF);
		
		String onDuty;
		String sql = "SELECT MAX(off_duty) FROM (" +
					 "SELECT off_duty FROM " + Params.dbName + ".daily_settle_history WHERE restaurant_id=" + term.restaurantID +
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
		
		return exec(dbCon, term, onDuty, offDuty, CalcBillStatistics.QUERY_TODAY);

	}
	
	
	/**
	 * Generate the details to shift within the on & off duty date.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to request
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
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attached with any restaurant.<br>
	 * 							 - The terminal is expired.
	 */
	public static Result exec(long pin, short model, String onDuty, String offDuty, int queryType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, pin, model, onDuty, offDuty, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Generate the details to shift within the on & off duty date.
	 * Note that database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to request
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
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attached with any restaurant.<br>
	 * 							 - The terminal is expired.
	 */
	public static Result exec(DBCon dbCon, long pin, short model, String onDuty, String offDuty, int queryType) throws SQLException, BusinessException{
		return exec(dbCon, VerifyPin.exec(dbCon, pin, model), onDuty, offDuty, queryType);
	}
	
	/**
	 * Generate the details to shift within the on & off duty date.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to request
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
	public static Result exec(Terminal term, String onDuty, String offDuty, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term, onDuty, offDuty, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Generate the details to shift within the on & off duty date.
	 * Note that database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to request
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
	public static Result exec(DBCon dbCon, Terminal term, String onDuty, String offDuty, int queryType) throws SQLException{
		
		Result result = new Result();
		result.onDuty = onDuty;
		result.offDuty = offDuty;
		
		//Calculate the general income
		IncomeByPay incomeByPay = CalcBillStatistics.calcIncomeByPayType(dbCon, term, new DutyRange(onDuty, offDuty), queryType);
		
		//FIXME
		result.cashAmount = incomeByPay.getCashAmount();
		result.cashIncome = incomeByPay.getCashIncome();
		result.cashIncome2 = incomeByPay.getCashActual();
				
		result.creditCardAmount = incomeByPay.getCreditCardAmount();
		result.creditCardIncome = incomeByPay.getCreditCardIncome();
		result.creditCardIncome2 = incomeByPay.getCreditCardActual();
				
		result.memeberCardAmount = incomeByPay.getMemeberCardAmount();
		result.memberCardIncome = incomeByPay.getMemberCardIncome();
		result.memberCardIncome2 = incomeByPay.getMemberCardActual();
				
		result.hangAmount = incomeByPay.getHangAmount();
		result.hangIncome = incomeByPay.getHangIncome();
		result.hangIncome2 = incomeByPay.getHangActual();
				
		result.signAmount = incomeByPay.getSignAmount();
		result.signIncome = incomeByPay.getSignIncome();
		result.signIncome2 = incomeByPay.getSignActual();
		
		result.orderAmount = incomeByPay.getOrderAmount();
		result.totalActual = incomeByPay.getTotalActual();
		
		//Calculate the total & amount to erase price
		IncomeByErase incomeByErase = CalcBillStatistics.calcErasePrice(dbCon, term, new DutyRange(onDuty, offDuty), queryType);
		//FIXME
		result.eraseAmount = incomeByErase.getEraseAmount();
		result.eraseIncome = incomeByErase.getTotalErase();
		//-----------------------------
		
		//Get the total & amount to discount price
		IncomeByDiscount incomeByDiscount = CalcBillStatistics.calcDiscountPrice(dbCon, term, new DutyRange(onDuty, offDuty), queryType);
		//FIXME
		result.discountAmount = incomeByDiscount.getDiscountAmount();
		result.discountIncome = incomeByDiscount.getTotalDiscount();	

		
		//Get the total & amount to gift price
		IncomeByGift incomeByGift = CalcBillStatistics.calcGiftPrice(dbCon, term, new DutyRange(onDuty, offDuty),  queryType);
		//FIXME
		result.giftAmount = incomeByGift.getGiftAmount();
		result.giftIncome = incomeByGift.getTotalGift();
		
		//Get the total & amount to cancel price
		IncomeByCancel incomeByCancel = CalcBillStatistics.calcCancelPrice(dbCon, term, new DutyRange(onDuty, offDuty), queryType);
		//FIXME
		result.cancelAmount = incomeByCancel.getCancelAmount();
		result.cancelIncome = incomeByCancel.getTotalCancel();
		
		//Get the total & amount to repaid order
		IncomeByRepaid incomeByRepaid = CalcBillStatistics.calcRepaidPrice(dbCon, term, new DutyRange(onDuty, offDuty), queryType);
		//FIXME
		result.paidAmount = incomeByRepaid.getRepaidAmount();
		result.paidIncome = incomeByRepaid.getTotalRepaid();
		
		//Get the total & amount to order with service
		IncomeByService incomeByService = CalcBillStatistics.calcServicePrice(dbCon, term, new DutyRange(onDuty, offDuty), queryType);
		//FIXME
		result.serviceAmount = incomeByService.getServiceAmount();
		result.serviceIncome = incomeByService.getTotalService();
		
		
		//Get the gift, discount & total to each department during this period.
		List<IncomeByDept> incomeByDept = CalcBillStatistics.calcIncomeByDept(dbCon, term, new DutyRange(onDuty, offDuty), null, queryType);
		result.deptIncome = incomeByDept;
		
		return result;
	}
	
}
