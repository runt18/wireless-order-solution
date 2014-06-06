package com.wireless.db.shift;

import java.sql.SQLException;
import java.sql.Timestamp;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.billStatistics.CalcBillStatisticsDao.ExtraCond;
import com.wireless.db.billStatistics.CalcBillStatisticsDao.ExtraCond4Charge;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DateType;

public class ShiftDao {
	
	/**
	 * Do shift by now.
	 * @param staff
	 * 			the staff to perform this action
	 * @return
	 * 			the duty range to this shift
	 * @throws SQLException
	 * 				throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 	 			throws if the restaurant does NOT exist 
	 */
	public static DutyRange doShift(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return doShift(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Do shift by now.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return
	 * 			the duty range to this shift
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 	 			throws if the restaurant does NOT exist 
	 */
	public static DutyRange doShift(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		
		DutyRange range = getCurrentShiftRange(dbCon, staff);
		
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".shift (restaurant_id, name, on_duty, off_duty) VALUES(" +
			  staff.getRestaurantId() + "," +
			  "'" + staff.getName() + "'," +
			  " DATE_FORMAT('" + range.getOnDutyFormat() + "', '%Y%m%d%H%i%s')" + "," +
			  " DATE_FORMAT('" + range.getOffDutyFormat() + "', '%Y%m%d%H%i%s')" + 
			  " ) ";
	
		dbCon.stmt.execute(sql);
		
		return range;
	}
	
	/**
	 * Get current daily shift detail to today.
	 * @param dbCon
	 * 			The database connection
	 * @param staff
	 * 			The staff to perform this action
	 * @return the shift detail to today
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @throws BusinessException 
	 * 				throws if the restaurant does NOT exist
	 */
	public static ShiftDetail getTodayDaily(Staff staff) throws SQLException, BusinessException{
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
	 * @throws BusinessException 
	 * 				throws if the restaurant does NOT exist
	 */
	public static ShiftDetail getTodayDaily(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		
		//Get the latest off duty date from daily settle history, and make it as the on duty date to this daily shift
		String sql = " SELECT MAX(off_duty) FROM " +
					 Params.dbName + ".daily_settle_history " +
					 " WHERE " +
					 " restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		long onDuty;
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
		
		return getByRange(dbCon, staff, new DutyRange(onDuty, System.currentTimeMillis()), DateType.TODAY);
	}
	
	/**
	 * Get the current shift detail. 
	 * @param staff
	 *            the staff to perform this action
	 * @return the current shift detail 
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @throws BusinessException 
	 * 	 			throws if the restaurant does NOT exist 
	 */
	public static ShiftDetail getCurrentShift(Staff staff) throws SQLException, BusinessException{
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
	 * @throws BusinessException
	 * 	 			throws if the restaurant does NOT exist 
	 */
	private static ShiftDetail getCurrentShift(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		return getByRange(dbCon, staff, getCurrentShiftRange(dbCon, staff), DateType.TODAY);
	}
	
	/**
	 * Get the range to current shift.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the duty range to current shift
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the restaurant does NOT exist
	 */
	private static DutyRange getCurrentShiftRange(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		/**
		 * Get the latest off duty date from tables below.
		 * 1 - shift 
		 * 2 - shift_history
		 * 3 - daily_settle_history
		 * And make it as the on duty date to this duty shift.
		 * Use the birth date to restaurant if no latest off duty exist.
		 */
		long onDuty;
		String sql = "SELECT MAX(off_duty) FROM (" +
					 "SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id=" + staff.getRestaurantId() + " UNION " +
					 "SELECT off_duty FROM " + Params.dbName + ".shift_history WHERE restaurant_id=" + staff.getRestaurantId() + " UNION " +
					 "SELECT off_duty FROM " + Params.dbName + ".daily_settle_history WHERE restaurant_id=" + staff.getRestaurantId() +
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
	 * Get the details to shift within the duty range
	 * @param staff
	 * 			the staff to request
	 * @param range
	 * 			the duty range
	 * @param dateType
	 * 			indicate the date type {@link DateType}
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static ShiftDetail getByRange(Staff staff, DutyRange range, DateType dateType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByRange(dbCon, staff, range, dateType);
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
	 * @param dateType
	 * 			indicate the date type {@link DateType}
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static ShiftDetail getByRange(DBCon dbCon, Staff staff, DutyRange range, DateType dateType) throws SQLException{
		
		ShiftDetail result = new ShiftDetail();
		result.setOnDuty(range.getOnDutyFormat());
		result.setOffDuty(range.getOffDutyFormat());
		
		ExtraCond extraCond = new ExtraCond(dateType);
		
		//Calculate the general income
		result.setIncomeByPay(CalcBillStatisticsDao.calcIncomeByPayType(dbCon, staff, range, extraCond));
		
		//Calculate the total & amount to erase price
		result.setEraseIncome(CalcBillStatisticsDao.calcErasePrice(dbCon, staff, range, extraCond));
		//-----------------------------
		
		//Get the total & amount to discount price
		result.setDiscountIncome(CalcBillStatisticsDao.calcDiscountPrice(dbCon, staff, range, extraCond));
		
		//Get the total & amount to gift price
		result.setGiftIncome(CalcBillStatisticsDao.calcGiftPrice(dbCon, staff, range, extraCond));
		
		//Get the total & amount to cancel price
		result.setCancelIncome(CalcBillStatisticsDao.calcCancelPrice(dbCon, staff, range, extraCond));
		
		//Get the total & amount to coupon price
		result.setCouponIncome(CalcBillStatisticsDao.calcCouponPrice(dbCon, staff, range, extraCond));
		
		//Get the total & amount to repaid order
		result.setRepaidIncome(CalcBillStatisticsDao.calcRepaidPrice(dbCon, staff, range, extraCond));
		
		//Get the total & amount to order with service
		result.setServiceIncome(CalcBillStatisticsDao.calcServicePrice(dbCon, staff, range, extraCond));
		
		//Get the income by charge
		result.setIncomeByCharge(CalcBillStatisticsDao.calcIncomeByCharge(dbCon, staff, range, new ExtraCond4Charge(dateType)));
		
		//Get the gift, discount & total to each department during this period.
		result.setDeptIncome(CalcBillStatisticsDao.calcIncomeByDept(dbCon, staff, range, extraCond));
		
		return result;
	}

	/**
	 * Archive the shift records.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the amount of shift records archived
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int archive(DBCon dbCon, Staff staff) throws SQLException{
		String sql;
		
		final String shiftItem = "`id`, `restaurant_id`, `name`, `on_duty`, `off_duty`";
		
		//Move the shift record from 'shift' to 'shift_history'.
		sql = " INSERT INTO " + Params.dbName + ".shift_history (" + shiftItem + ") " +
			  " SELECT " + shiftItem + " FROM " + Params.dbName + ".shift " +
			  " WHERE restaurant_id = " + staff.getRestaurantId();
		int amount = dbCon.stmt.executeUpdate(sql);
		
		//Delete the today shift records belong to this restaurant.
		sql = " DELETE FROM " + Params.dbName + ".shift WHERE " + (staff.getRestaurantId() < 0 ? "" : "restaurant_id=" + staff.getRestaurantId());
		dbCon.stmt.executeUpdate(sql);
		
		return amount;
	}
	
	/**
	 * Sweep the history shift records which have been expired.
	 * @param dbCon
	 * 			the database connection
	 * @return the amount of history shift records to sweep
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int sweep(DBCon dbCon) throws SQLException{
		// Delete the history shift which has been expired.
		String sql;
		sql = " DELETE SH FROM " + 
			  Params.dbName + ".shift_history AS SH, " +
			  Params.dbName + ".restaurant AS REST " +
			  " WHERE 1 = 1 " +
			  " AND REST.id > " + Restaurant.RESERVED_7 +
			  " AND SH.restaurant_id = REST.id " +
			  " AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(SH.off_duty) > REST.record_alive ";
		return dbCon.stmt.executeUpdate(sql);
	}
	
}
