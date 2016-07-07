package com.wireless.db.shift;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.DBTbl;
import com.wireless.db.Params;
import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.billStatistics.CalcMemberStatisticsDao;
import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.PaymentGeneral;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;

public class PaymentDao {

	public static class ExtraCond{
		private final DateType dateType;
		protected final DBTbl dbTbl;
		private int staffId;
		
		public ExtraCond(DateType dateType){
			this.dateType = dateType;
			this.dbTbl = new DBTbl(dateType);
		}
		
		public ExtraCond setStaffId(int staffId){
			this.staffId = staffId;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(staffId > 0){
				extraCond.append(" AND staff_id = " + staffId);
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Do the payment to specific staff.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the duty range to this payment
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * 			
	 */
	public static DutyRange doPayment(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return doPayment(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Do the payment to specific staff.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the duty range to this payment
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * 			
	 */
	public static DutyRange doPayment(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		DutyRange range = getCurrentPaymentRange(dbCon, staff);
		
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".payment" +
			  " (restaurant_id, staff_id, staff_name, on_duty, off_duty) VALUES(" +
			  staff.getRestaurantId() + "," +
			  staff.getId() + "," +
			  "'" + staff.getName() + "'," +
			  "'" + range.getOnDutyFormat() + "'," +
			  "'" + range.getOffDutyFormat() + "'" +
			  ")";
	
		dbCon.stmt.execute(sql);
		
		return range;
	}
	
	/**
	 * Get current payment detail to the staff performed this action.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the details to this payment
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the restaurant does NOT exist
	 */
	public static ShiftDetail getCurrentPayment(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getCurrentPayment(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get current payment detail to the staff performed this action.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the details to this payment
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the restaurant does NOT exist
	 */
	public static ShiftDetail getCurrentPayment(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		return getDetail(dbCon, staff, getCurrentPaymentRange(dbCon, staff), new ExtraCond(DateType.TODAY).setStaffId(staff.getId()));
	}
	

	
	/**
	 * Get the range to current payment.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the duty range to current payment
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the restaurant does NOT exist
	 */
	public static DutyRange getCurrentPaymentRange(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getCurrentPaymentRange(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	private static DutyRange getCurrentPaymentRange(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		/**
		 * Get the latest payment date to this staff from tables below.
		 * 1 - payment 
		 * 2 - payment_history
		 * 3 - daily_settle_history
		 * And make it as the on duty date to this duty shift.
		 * Use the birth date to restaurant if no latest payment exist.
		 */
		long onDuty;
		String sql = "SELECT MAX(off_duty) FROM (" +
					 "SELECT off_duty FROM " + Params.dbName + ".payment WHERE restaurant_id = " + staff.getRestaurantId() + " AND " + " staff_id = " + staff.getId() + 
					 " UNION " +
					 "SELECT off_duty FROM " + Params.dbName + ".payment_history WHERE restaurant_id = " + staff.getRestaurantId() + " AND " + " staff_id = " + staff.getId() + 
					 " UNION " +
					 "SELECT off_duty FROM " + Params.dbName + ".daily_settle_history WHERE restaurant_id = " + staff.getRestaurantId() +
					 ") AS all_off_duty";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			Timestamp offDuty = dbCon.rs.getTimestamp(1);
			if(offDuty == null){
				onDuty = RestaurantDao.getById(dbCon, staff.getRestaurantId()).getBirthDate();
			}else{
				onDuty = offDuty.getTime();
			}
		}else{
			onDuty = RestaurantDao.getById(dbCon, staff.getRestaurantId()).getBirthDate();
		}
		dbCon.rs.close();
		
		return new DutyRange(onDuty, System.currentTimeMillis());
	}
	
	/**
	 * Get today payments. 
	 * @param staff
	 * 			the staff to perform this action
	 * @return the payment general
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PaymentGeneral> getToday(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getToday(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get today payments. 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the payment general
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PaymentGeneral> getToday(DBCon dbCon, Staff staff) throws SQLException{
		return getByCond(dbCon, staff, null, new ExtraCond(DateType.TODAY));
	}
	
	/**
	 * Get history payments. 
	 * @param staff
	 * 			the staff to perform this action
	 * @return the payment general
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PaymentGeneral> getHistory(Staff staff, DutyRange range) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getHistory(dbCon, staff, range);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get history payments. 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the payment general
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PaymentGeneral> getHistory(DBCon dbCon, Staff staff, DutyRange range) throws SQLException{
		return getByCond(dbCon, staff, range, new ExtraCond(DateType.HISTORY));
	}
	
	/**
	 * Get the payment general according to specific duty range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to payment general
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PaymentGeneral> getByCond(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		
		String sql;
		sql = " SELECT restaurant_id, staff_id, staff_name, on_duty, off_duty FROM " +
			  Params.dbName + "." + extraCond.dbTbl.paymentTbl +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "") +
			  (range != null ? " AND off_duty BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" : "") +
			  " ORDER BY off_duty ";
		
		List<PaymentGeneral> result = new ArrayList<PaymentGeneral>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			PaymentGeneral detail = new PaymentGeneral();
			detail.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			detail.setStaffName(dbCon.rs.getString("staff_name"));
			detail.setStaffId(dbCon.rs.getInt("staff_id"));
			detail.setOnDuty(DateUtil.format(dbCon.rs.getTimestamp("on_duty").getTime()));
			detail.setOffDuty(DateUtil.format(dbCon.rs.getTimestamp("off_duty").getTime()));
			result.add(detail);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Get the detail to payment to specific range and staff.
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the detail to this payment
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static ShiftDetail getDetail(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getDetail(dbCon, staff, range, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the detail to payment to specific range and staff.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the detail to this payment
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static ShiftDetail getDetail(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException, BusinessException{
		ShiftDetail result = new ShiftDetail(range);
		
		CalcBillStatisticsDao.ExtraCond extraCond4CalcBill = new CalcBillStatisticsDao.ExtraCond(extraCond.dateType).setStaffId(extraCond.staffId).setDutyRange(range);
		//Calculate the general income
		result.setIncomeByPay(CalcBillStatisticsDao.calcIncomeByPayType(dbCon, staff, extraCond4CalcBill));
		
		//Calculate the total & amount to erase price
		result.setEraseIncome(CalcBillStatisticsDao.calcErasePrice(dbCon, staff, extraCond4CalcBill));
		//-----------------------------
		
		//Get the total & amount to discount price
		result.setDiscountIncome(CalcBillStatisticsDao.calcDiscountPrice(dbCon, staff, extraCond4CalcBill));
		
		//Get the total & amount to gift price
		result.setGiftIncome(CalcBillStatisticsDao.calcGiftPrice(dbCon, staff, extraCond4CalcBill));
		
		//Get the total & amount to cancel price
		result.setCancelIncome(CalcBillStatisticsDao.calcCancelPrice(dbCon, staff, extraCond4CalcBill));
		
		//Get the total & amount to coupon price
		result.setCouponIncome(CalcBillStatisticsDao.calcCouponPrice(dbCon, staff, extraCond4CalcBill));
		
		//Get the total & amount to repaid order
		result.setRepaidIncome(CalcBillStatisticsDao.calcRepaidPrice(dbCon, staff, extraCond4CalcBill));
		
		//Get the total & amount to order with service
		result.setServiceIncome(CalcBillStatisticsDao.calcServicePrice(dbCon, staff, extraCond4CalcBill));
		
		//Get the income by charge
		result.setIncomeByCharge(CalcMemberStatisticsDao.calcIncomeByCharge(dbCon, staff, new MemberOperationDao.ExtraCond(extraCond.dateType).setOperateDate(range).setStaff(extraCond.staffId)));
		
		return result;
	}
	
	/**
	 * Archived the daily payment records from today to history.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the amount to payment records archived
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int archive4Daily(DBCon dbCon, Staff staff) throws SQLException{
		return archive(dbCon, staff, null, DateType.TODAY, DateType.HISTORY);
	}
	
	/**
	 * Archive the expired payment records from history to archive.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the amount to payment records archived
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statment
	 */
	public static int archive4Expired(DBCon dbCon, final Staff staff) throws SQLException{
		ExtraCond extraCond4Expired = new ExtraCond(DateType.HISTORY){
			@Override
			public String toString(){
				return " AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(P.off_duty) > REST.record_alive ";
			}
		};
		return archive(dbCon, staff, extraCond4Expired, DateType.HISTORY, DateType.ARCHIVE);
	}
	
	private static int archive(DBCon dbCon, Staff staff, ExtraCond extraCond, DateType archiveFrom, DateType archiveTo) throws SQLException{
		DBTbl fromTbl = new DBTbl(archiveFrom);
		DBTbl toTbl = new DBTbl(archiveTo);
		
		final String paymentItem = "`restaurant_id`, `staff_id`, `staff_name`, `on_duty`, `off_duty`";
		
		String sql;
		sql = " INSERT INTO " + Params.dbName + "." + toTbl.paymentTbl + 
			  "(" + paymentItem + ")" +
			  " SELECT " + paymentItem + " FROM " + Params.dbName + "." + fromTbl.paymentTbl + " P " +
			  " JOIN " + Params.dbName + ".restaurant REST ON REST.id = P.restaurant_id " +
			  " WHERE restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		
		int amount = dbCon.stmt.executeUpdate(sql);
		
		sql = " DELETE P FROM " + Params.dbName + "." + fromTbl.paymentTbl + " P " +
			  " JOIN " + Params.dbName + ".restaurant REST ON REST.id = P.restaurant_id " +
			  " WHERE restaurant_id = " + staff.getRestaurantId() + (extraCond != null ? extraCond.toString() : "");
		dbCon.stmt.executeUpdate(sql);
		
		return amount;
	}
	
	/**
	 * Sweep the history payment records which have been expired.
	 * @param dbCon
	 * 			the database connection
	 * @return the amount of payment records to sweep
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int sweep(DBCon dbCon) throws SQLException{
		// Delete the SMS details which has been expired.
		String sql;
		sql = " DELETE P FROM " + 
			  Params.dbName + ".payment_history AS P, " +
			  Params.dbName + ".restaurant AS REST " +
			  " WHERE 1 = 1 " +
			  " AND REST.id > " + Restaurant.RESERVED_7 +
			  " AND P.restaurant_id = REST.id " +
			  " AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(P.off_duty) > REST.record_alive ";
		return dbCon.stmt.executeUpdate(sql);
	}
}
