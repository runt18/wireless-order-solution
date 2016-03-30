package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.shift.PaymentDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.PaymentGeneral;
import com.wireless.pojo.billStatistics.ShiftGeneral;
import com.wireless.pojo.billStatistics.ShiftGeneral.StaffPayment;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;

public class ShiftGeneralDao {

	public static class ExtraCond{
		
		private final DateType dateType;
		private final String shiftTbl;
		private DutyRange range; 
		
		public ExtraCond(DateType dateType){
			this.dateType = dateType;
			if(this.dateType == DateType.HISTORY){
				shiftTbl = "shift_history";
			}else{
				shiftTbl = "shift";
			}
		}
		
		public ExtraCond setRange(DutyRange range){
			this.range = range;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(range != null){
				extraCond.append(" AND ").append("off_duty BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'");
			}
			return extraCond.toString();
		}
		
	}
	
	/**
	 * Get the today shift general.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the shift general to today
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ShiftGeneral> getTodayShift(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getTodayShift(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the today shift general.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the shift general to today
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ShiftGeneral> getTodayShift(DBCon dbCon, Staff staff) throws SQLException{
		String sql;
		sql = " SELECT name, on_duty, off_duty " +
			  " FROM " +
			  " ( "	+
			  " (SELECT '全天' AS name, (SELECT IFNULL(MAX(off_duty), '1970-01-01 00:00:00') FROM " + Params.dbName + ".daily_settle_history WHERE restaurant_id = " + staff.getRestaurantId() + ") AS on_duty, NOW() AS off_duty) " +
			  " UNION ALL" +
			  " (SELECT name, on_duty, off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id = " + staff.getRestaurantId() + " ORDER BY off_duty)" +
			  " UNION ALL " +
			  " (SELECT * FROM (SELECT '本班次' AS name, (SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id = " + staff.getRestaurantId() + " ORDER BY off_duty DESC LIMIT 0,1) AS on_duty, NOW() AS off_duty) TT WHERE on_duty IS NOT NULL)) T ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<ShiftGeneral> result = new ArrayList<ShiftGeneral>();
		while(dbCon.rs.next()){
			ShiftGeneral item = new ShiftGeneral(0);
			item.setStaffName(dbCon.rs.getString("name"));
			item.setOnDuty(dbCon.rs.getTimestamp("on_duty").getTime());
			item.setOffDuty(dbCon.rs.getTimestamp("off_duty").getTime());
			result.add(item);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Get the shift and associated payment info to today.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the list result {@link ShiftGeneral}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<ShiftGeneral> getToday(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getToday(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the shift and associated payment info to today.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the list result {@link ShiftGeneral}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<ShiftGeneral> getToday(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		String sql;
		
		List<ShiftGeneral> result = getByCond(dbCon, staff, new ExtraCond(DateType.TODAY), null);
		
		for(ShiftGeneral eachShift : result){
			//计算每个员工的应交款项
			sql = " SELECT SUM(actual_price) AS total_payment, MAX(waiter) AS waiter, staff_id " +
				  " FROM " + Params.dbName + ".order " +
				  " WHERE 1 = 1 " +
				  " AND restaurant_id = " + staff.getRestaurantId() +
				  " AND status <> " + Order.Status.UNPAID.getVal() +
				  " AND order_date BETWEEN '" + DateUtil.format(eachShift.getOnDuty()) + "' AND '" + DateUtil.format(eachShift.getOffDuty()) + "'" +
				  " GROUP BY staff_id ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				StaffPayment payment = new StaffPayment();
				payment.setStaffName(dbCon.rs.getString("waiter"));
				payment.setStaffId(dbCon.rs.getInt("staff_id"));
				payment.setTotalPrice(dbCon.rs.getFloat("total_payment"));
				eachShift.addPayment(payment);
			}
			dbCon.rs.close();

			for(PaymentGeneral eachPayment : PaymentDao.getByCond(dbCon, staff, new DutyRange(eachShift.getOnDuty(), eachShift.getOffDuty()), new PaymentDao.ExtraCond(DateType.TODAY))){
				StaffPayment payment = new StaffPayment();
				payment.setStaffName(eachPayment.getStaffName());
				payment.setStaffId(eachPayment.getStaffId());
				eachShift.addPayment(payment);
			}
			
			//统计交班时间内每个员工的应交款项
			for(StaffPayment eachPayment : eachShift.getPayments()){
				for(PaymentGeneral eachPayGeneral : PaymentDao.getByCond(dbCon, staff, new DutyRange(eachShift.getOnDuty(), eachShift.getOffDuty()), new PaymentDao.ExtraCond(DateType.TODAY).setStaffId(eachPayment.getStaffId()))){
					eachPayment.addPaymentGeneral(eachPayGeneral);
					eachPayment.setActualPrice(eachPayment.getActualPrice() + 
											   PaymentDao.getDetail(dbCon, staff, 
													   			    new DutyRange(eachPayGeneral.getOnDuty(), eachPayGeneral.getOffDuty()), 
													   			    new PaymentDao.ExtraCond(DateType.TODAY).setStaffId(eachPayment.getStaffId())).getTotalActual());
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Get the history shift general according to date range. 
	 * @param staff
	 * 			the staff to perform this action
	 * @return the shift general among the range.
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ShiftGeneral> getByRange(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByRange(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the history shift general according to date range. 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the shift general among the range.
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ShiftGeneral> getByRange(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		return getByCond(dbCon, staff, extraCond, null);
	}
	
	private static List<ShiftGeneral> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " + 
			  " restaurant_id, id, name, on_duty, off_duty " + 
			  " FROM " + Params.dbName + "." + extraCond.shiftTbl +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : " ORDER BY off_duty ");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<ShiftGeneral> result = new ArrayList<ShiftGeneral>();
		while(dbCon.rs.next()){
			ShiftGeneral shiftGeneral = new ShiftGeneral(dbCon.rs.getInt("id"));
			shiftGeneral.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			shiftGeneral.setStaffName(dbCon.rs.getString("name"));
			shiftGeneral.setOnDuty(dbCon.rs.getTimestamp("on_duty").getTime());
			shiftGeneral.setOffDuty(dbCon.rs.getTimestamp("off_duty").getTime());
			result.add(shiftGeneral);
		}
		dbCon.rs.close();
		
		return result;
	}
}
