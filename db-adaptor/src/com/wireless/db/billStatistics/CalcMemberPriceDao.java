package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.DBTbl;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.member.MemberPriceByEachDay;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;

public class CalcMemberPriceDao {
	
	public static class ExtraCond{
		private final DBTbl dbTbl;
		private HourRange hourRange;
		private int staffId;
		
		public ExtraCond setHourRange(HourRange hourRange){
			this.hourRange = hourRange;
			return this;
		}
		
		public ExtraCond(DateType dateType){
			dbTbl = new DBTbl(dateType);
		}
		
		public ExtraCond setStaff(int staffId){
			this.staffId = staffId;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(hourRange != null){
				extraCond.append(" AND TIME(O.order_date) BETWEEN '" + hourRange.getOpeningFormat() + "' AND '" + hourRange.getEndingFormat() + "'");
			}
			if(staffId != 0){
				extraCond.append(" AND O.staff_id = " + staffId);
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Get the member price detail to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the order to member price matched the extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Order> getMemberPriceDetail(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getMemberPriceDetail(dbCon, staff, range, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member price detail to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the order to member price matched the extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Order> getMemberPriceDetail(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException, BusinessException{
		String sql = makeSql4MemberPrice(staff, range, extraCond);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		final List<Order> result = new ArrayList<Order>();
		while(dbCon.rs.next()){
			Order o = new Order();
			o.setId(dbCon.rs.getInt("id"));
			o.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			o.setPurePrice(dbCon.rs.getInt("pure_price"));
			o.setActualPrice(dbCon.rs.getFloat("actual_price"));
			o.setWaiter(dbCon.rs.getString("waiter"));
			o.setComment(dbCon.rs.getString("comment"));
			Table t = new Table(dbCon.rs.getInt("table_id"));
			t.setTableAlias(dbCon.rs.getInt("table_alias"));
			t.setTableName(dbCon.rs.getString("table_name"));
			o.setDestTbl(t);
			result.add(o);
		}
		dbCon.rs.close();
		
		return result;
		
	}

	/**
	 * Calculate the member price by each day.
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 */
	public static List<MemberPriceByEachDay> calcMemberPriceByEachDay(Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException, ParseException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcMemberPriceByEachDay(dbCon, staff, dutyRange, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the member price by each day.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 */
	public static List<MemberPriceByEachDay> calcMemberPriceByEachDay(DBCon dbCon, Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException, ParseException{
		List<MemberPriceByEachDay> result = new ArrayList<MemberPriceByEachDay>();
		
		Calendar c = Calendar.getInstance();
		Date dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(dutyRange.getOnDutyFormat());
		Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(dutyRange.getOffDutyFormat());
		c.setTime(dateBegin);
		while (dateBegin.compareTo(dateEnd) <= 0) {
			c.add(Calendar.DATE, 1);
			
			DutyRange range = DutyRangeDao.exec(dbCon, staff, 
					DateUtil.format(dateBegin, DateUtil.Pattern.DATE_TIME), 
					DateUtil.format(c.getTime(), DateUtil.Pattern.DATE_TIME));
			
			if(range != null){
				String sql;
				sql = " SELECT " +
					  " COUNT(*) AS amount, " +
					  " ROUND(SUM(TMP.actual_price), 2) AS price " +
				      " FROM (" +
				      makeSql4MemberPrice(staff, range, extraCond) + 
					  " ) AS TMP ";
			    dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					result.add(new MemberPriceByEachDay(new DutyRange(dateBegin.getTime(), c.getTimeInMillis()),
														 dbCon.rs.getFloat("amount"),
														 dbCon.rs.getFloat("price")));
				}
				dbCon.rs.close();

			}else{
				result.add(new MemberPriceByEachDay(new DutyRange(dateBegin.getTime(), c.getTimeInMillis()), 0, 0)); 
			}
			
			dateBegin = c.getTime();
		}
		
		return result;
	}
	
	private static String makeSql4MemberPrice(Staff staff, DutyRange range, ExtraCond extraCond){
		String sql;
		sql = " SELECT " +
			  " O.id, O.order_date, O.waiter, O.staff_id, O.pure_price, O.actual_price, O.comment, O.table_alias, O.table_name, O.table_id " +
			  " FROM " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
		      " JOIN " + Params.dbName + "." + extraCond.dbTbl.moTbl + " MO ON O.id = MO.order_id AND MO.operate_type = " + MemberOperation.OperationType.CONSUME.getValue() +
		      " WHERE 1 = 1 " +
		      " AND O.restaurant_id = " + staff.getRestaurantId() +
		      " AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
		      (extraCond != null ? extraCond.toString() : "");
		return sql;
	}
}
