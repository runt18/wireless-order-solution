package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.DBTbl;
import com.wireless.db.Params;
import com.wireless.db.member.MemberDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.member.IncomeByCharge;
import com.wireless.pojo.billStatistics.member.IncomeByConsume;
import com.wireless.pojo.billStatistics.member.MemberStatistics;
import com.wireless.pojo.billStatistics.member.StatisticsByEachDay;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberOperation.ChargeType;
import com.wireless.pojo.member.MemberOperation.OperationType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;

public class CalcMemberStatisticsDao {

	public static class ExtraCond{
		private final DBTbl dbTbl;
		private int staffId;
		
		public ExtraCond(DateType dateType){
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

	public static MemberStatistics calcStatisticsByEachDay(Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException, ParseException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcStatisticsByEachDay(dbCon, staff, dutyRange, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static MemberStatistics calcStatisticsByEachDay(DBCon dbCon, Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException, ParseException{
		
		final MemberStatistics result = new MemberStatistics();
		
		Calendar c = Calendar.getInstance();
		Date dateBegin = new SimpleDateFormat(DateUtil.Pattern.DATE_TIME.getPattern()).parse(dutyRange.getOnDutyFormat());
		Date dateEnd = new SimpleDateFormat(DateUtil.Pattern.DATE_TIME.getPattern()).parse(dutyRange.getOffDutyFormat());
		c.setTime(dateBegin);
		while (dateBegin.compareTo(dateEnd) <= 0) {
			c.add(Calendar.DATE, 1);
			
//			final DutyRange range = DutyRangeDao.exec(dbCon, staff, 
//													  DateUtil.format(dateBegin, DateUtil.Pattern.DATE_TIME), 
//													  DateUtil.format(c.getTime(), DateUtil.Pattern.DATE_TIME));
			
			final DutyRange range;
			if(c.getTime().getTime() < dateEnd.getTime()){
				range = new DutyRange(DateUtil.format(dateBegin, DateUtil.Pattern.DATE_TIME), DateUtil.format(c.getTime(), DateUtil.Pattern.DATE_TIME));
			}else{
				range = new DutyRange(DateUtil.format(dateBegin, DateUtil.Pattern.DATE_TIME), DateUtil.format(dateEnd.getTime(), DateUtil.Pattern.DATE_TIME));
			}
			
			IncomeByConsume consumption = null;
			IncomeByCharge charge = null;
			if(range != null){
				//Calculate the consumption income
				consumption = calcIncomeByConsume(dbCon, staff, range, extraCond);
				
				//Get the charge income by both cash and credit card
				charge = calcIncomeByCharge(dbCon, staff, range, extraCond);
				
			}
			
			List<Member> creates = null;
			try {
				creates = MemberDao.getByCond(dbCon, staff, new MemberDao.ExtraCond().setCreateRange(new DutyRange(dateBegin.getTime(), c.getTime().getTime())), null);
			} catch (BusinessException e) {
				creates = Collections.emptyList();
				e.printStackTrace();
			}
			
			result.add(new StatisticsByEachDay(DateUtil.format(dateBegin, DateUtil.Pattern.DATE), creates, charge, consumption));
			
			dateBegin = c.getTime();
		}
		
		return result;
	}
	
	public static IncomeByConsume calcIncomeByConsume(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT IFNULL(PT.name, '其他') AS pay_type, SUM(TMP.actual_price) AS total_consume, COUNT(*) AS consume_amount FROM ( " +
				" SELECT OH.actual_price, OH.pay_type_id FROM " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " OH " +
				" JOIN " + Params.dbName + "." + extraCond.dbTbl.moTbl + " MOH ON MOH.order_id = OH.id " +
				" WHERE OH.restaurant_id = " + staff.getRestaurantId() +
			    " AND MOH.operate_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
				" AND MOH.operate_type = " + MemberOperation.OperationType.CONSUME.getValue() +
				" GROUP BY OH.id " +
			  " ) AS TMP " +
			  " LEFT JOIN " + Params.dbName + ".pay_type PT ON PT.pay_type_id = TMP.pay_type_id " +
			  " GROUP BY TMP.pay_type_id ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		IncomeByConsume result = new IncomeByConsume();
		
		while(dbCon.rs.next()){
			result.add(dbCon.rs.getString(1), dbCon.rs.getFloat(2), dbCon.rs.getInt(3));
		}
		dbCon.rs.close();
		
		return result;
	}
	
	 /**
	  * Calculate the member charge income according to duty range and extra condition.
	  * @param staff
	  * 		the staff to perform this action
	  * @param range
	  * 		the duty range
	  * @param extraCond
	  * 		the extra condition
	  * @return the income by charge refer to {@link IncomeByCharge}
	  * @throws SQLException
	  * 			if failed to execute any SQL statement
	  */
	 public static IncomeByCharge calcIncomeByCharge(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		 DBCon dbCon = new DBCon();
		 try{
			 dbCon.connect();
			 return calcIncomeByCharge(dbCon, staff, range, extraCond);
		 }finally{
			 dbCon.disconnect();
		 }
	 }
	 
	 /**
	  * Calculate the member charge income according to duty range and extra condition.
	  * @param dbCon
	  * 		the data base connection
	  * @param staff
	  * 		the staff to perform this action
	  * @param range
	  * 		the duty range
	  * @param extraCond
	  * 		the extra condition
	  * @return the income by charge refer to {@link IncomeByCharge}
	  * @throws SQLException
	  * 			if failed to execute any SQL statement
	  */
	 public static IncomeByCharge calcIncomeByCharge(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		 String sql;
		 
		 // Calculate the charge money. 
		 sql = " SELECT " +
			   " COUNT(*) AS charge_amount, " +
			   " SUM(delta_base_money + delta_extra_money) AS total_account_charge, " +
		 	   " SUM(IF(charge_type = " + ChargeType.CASH.getValue() + ", charge_money, 0)) AS total_actual_charge_by_cash, " +
		 	   " SUM(IF(charge_type = " + ChargeType.CREDIT_CARD.getValue() + ", charge_money, 0)) AS total_actual_charge_by_card " +
			   " FROM " + Params.dbName + "." + extraCond.dbTbl.moTbl +
			   " WHERE 1 = 1 " +
			   (extraCond != null ? extraCond.toString() : "") +
			   " AND restaurant_id = " + staff.getRestaurantId() +
			   " AND operate_type = " + OperationType.CHARGE.getValue() +
			   " AND operate_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'";
		 
		 dbCon.rs = dbCon.stmt.executeQuery(sql);
		 
		 IncomeByCharge incomeByCharge = new IncomeByCharge();
		 
		 if(dbCon.rs.next()){
			 incomeByCharge.setChargeAmount(dbCon.rs.getInt("charge_amount"));
			 incomeByCharge.setActualCashCharge(dbCon.rs.getFloat("total_actual_charge_by_cash"));
			 incomeByCharge.setActualCreditCardCharge(dbCon.rs.getFloat("total_actual_charge_by_card"));
			 incomeByCharge.setTotalAccountCharge(dbCon.rs.getFloat("total_account_charge"));
		 }
		 
		 dbCon.rs.close();
		 
		 // Calculate the refund. 
		 sql = " SELECT " +
			   " COUNT(*) AS refund_amount, " +
			   " SUM(delta_base_money + delta_extra_money) AS total_account_refund, " +
		 	   " SUM(charge_money) AS total_actual_refund " +
			   " FROM " + Params.dbName + "." + extraCond.dbTbl.moTbl +
			   " WHERE 1 = 1 " +
			   (extraCond != null ? extraCond.toString() : "") +
			   " AND restaurant_id = " + staff.getRestaurantId() +
			   " AND operate_type = " + OperationType.REFUND.getValue() +
			   " AND operate_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'";
		 
		 dbCon.rs = dbCon.stmt.executeQuery(sql);
		 
		 if(dbCon.rs.next()){
			 incomeByCharge.setRefundAmount(dbCon.rs.getInt("refund_amount"));
			 incomeByCharge.setTotalActualRefund(Math.abs(dbCon.rs.getFloat("total_actual_refund")));
			 incomeByCharge.setTotalAccountRefund(Math.abs(dbCon.rs.getFloat("total_account_refund")));
		 }
		 
		 dbCon.rs.close();
		 
		 return incomeByCharge;
		 
	 }	
}
