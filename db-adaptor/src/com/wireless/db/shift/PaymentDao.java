package com.wireless.db.shift;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.PaymentGeneral;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DateType;

public class PaymentDao {

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
	private static DutyRange getCurrentPaymentRange(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		/**
		 * Get the latest payment date from tables below.
		 * 1 - payment 
		 * 2 - payment_history
		 * 3 - daily_settle_history
		 * And make it as the on duty date to this duty shift.
		 * Use the birth date to restaurant if no latest payment exist.
		 */
		long onDuty;
		String sql = "SELECT MAX(off_duty) FROM (" +
					 "SELECT off_duty FROM " + Params.dbName + ".payment WHERE restaurant_id = " + staff.getRestaurantId() + " UNION " +
					 "SELECT off_duty FROM " + Params.dbName + ".payment_history WHERE restaurant_id = " + staff.getRestaurantId() + " UNION " +
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
		return get(dbCon, staff, null, DateType.TODAY);
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
		return get(dbCon, staff, range, DateType.HISTORY);
	}
	
	private static List<PaymentGeneral> get(DBCon dbCon, Staff staff, DutyRange range, DateType dateType) throws SQLException{
		final String paymentTbl;
		if(dateType == DateType.TODAY){
			paymentTbl = "payment";
		}else{
			paymentTbl = "payment_history";
		}
		
		String sql;
		sql = " SELECT restaurant_id, staff_id, staff_name, on_duty, off_duty FROM " +
			  Params.dbName + "." + paymentTbl +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  range != null ? (" AND off_duty BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'") : "";
		
		List<PaymentGeneral> result = new ArrayList<PaymentGeneral>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			PaymentGeneral detail = new PaymentGeneral();
			detail.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			detail.setStaffName(dbCon.rs.getString("staff_name"));
			detail.setOnDuty(DateUtil.format(dbCon.rs.getTimestamp("on_duty").getTime()));
			detail.setOffDuty(DateUtil.format(dbCon.rs.getTimestamp("off_duty").getTime()));
			result.add(detail);
		}
		dbCon.rs.close();
		
		return result;
	}
}
