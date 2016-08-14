package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.member.MemberDao;
import com.wireless.db.member.MemberOperationDao;
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
import com.wireless.pojo.member.SummaryByEachMember;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.SortedList;

public class CalcMemberStatisticsDao {

//	public static class ExtraCond{
//		private final DBTbl dbTbl;
//		private int staffId;
//		private int branchId;
//		private MemberOperation.OperationType operateType;
//		
//		public ExtraCond(DateType dateType){
//			this.dbTbl = new DBTbl(dateType);
//		}
//		
//		public ExtraCond setStaff(int staffId){
//			this.staffId = staffId;
//			return this;
//		}
//		
//		public ExtraCond setBranch(int branchId){
//			this.branchId = branchId;
//			return this;
//		}
//		
//		public ExtraCond setOperateType(MemberOperation.OperationType operateType){
//			this.operateType = operateType;
//			return this;
//		}
//		
//		@Override
//		public String toString(){
//			StringBuilder extraCond = new StringBuilder();
//			if(staffId > 0){
//				extraCond.append(" AND staff_id = " + staffId);
//			}
//			if(branchId != 0){
//				extraCond.append(" AND branch_id = " + branchId);
//			}
//			if(operateType != null){
//				extraCond.append(" AND operate_type = " + operateType.getValue());
//			}
//			return extraCond.toString();
//		}
//	}

	public static MemberStatistics calcStatisticsByEachDay(Staff staff, DutyRange dutyRange, MemberOperationDao.ExtraCond extraCond) throws SQLException, ParseException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcStatisticsByEachDay(dbCon, staff, dutyRange, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static MemberStatistics calcStatisticsByEachDay(DBCon dbCon, Staff staff, DutyRange dutyRange, MemberOperationDao.ExtraCond extraCond) throws SQLException, ParseException{
		
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
				consumption = calcIncomeByConsume(dbCon, staff, extraCond.setOperateDate(range));
				
				//Get the charge income by both cash and credit card
				charge = calcIncomeByCharge(dbCon, staff, extraCond.setOperateDate(range));
				
			}
			
			List<Member> creates = null;
			try {
				creates = MemberDao.getByCond(dbCon, staff, new MemberDao.ExtraCond().setCreateRange(new DutyRange(dateBegin.getTime(), c.getTime().getTime()))
																				     .setBranch(staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()), null);
			} catch (BusinessException e) {
				creates = Collections.emptyList();
				e.printStackTrace();
			}
			
			result.add(new StatisticsByEachDay(DateUtil.format(dateBegin, DateUtil.Pattern.DATE), creates, charge, consumption));
			
			dateBegin = c.getTime();
		}
		
		return result;
	}
	
	public static IncomeByConsume calcIncomeByConsume(DBCon dbCon, Staff staff, MemberOperationDao.ExtraCond extraCond) throws SQLException{
		String sql;
		
		sql = " SELECT IFNULL(PT.name, '其他') AS pay_type, SUM(TMP.actual_price) AS total_consume, COUNT(*) AS consume_amount FROM ( " +
				" SELECT OH.actual_price, OH.pay_type_id FROM " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " OH " +
				" JOIN ( " +  MemberOperationDao.makeSql(staff, extraCond.setOperationType(OperationType.CONSUME), null) + " ) AS TMP ON TMP.order_id = OH.id " +
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
	  * @param extraCond
	  * 		the extra condition
	  * @return the income by charge refer to {@link IncomeByCharge}
	  * @throws SQLException
	  * 			if failed to execute any SQL statement
	  */
	 public static IncomeByCharge calcIncomeByCharge(Staff staff, MemberOperationDao.ExtraCond extraCond) throws SQLException{
		 DBCon dbCon = new DBCon();
		 try{
			 dbCon.connect();
			 return calcIncomeByCharge(dbCon, staff, extraCond);
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
	  * @param extraCond
	  * 		the extra condition
	  * @return the income by charge refer to {@link IncomeByCharge}
	  * @throws SQLException
	  * 			if failed to execute any SQL statement
	  */
	 public static IncomeByCharge calcIncomeByCharge(DBCon dbCon, Staff staff, MemberOperationDao.ExtraCond extraCond) throws SQLException{
		 String sql;
		 
		 // Calculate the charge money. 
		 sql = " SELECT " +
			   " COUNT(*) AS charge_amount, " +
			   " SUM(delta_base_money + delta_extra_money) AS total_account_charge, " +
			   " SUM(IF(charge_type = " + ChargeType.CASH.getValue() + ", charge_money, 0)) AS total_actual_charge_by_cash, " +
			   " SUM(IF(charge_type = " + ChargeType.CREDIT_CARD.getValue() + ", charge_money, 0)) AS total_actual_charge_by_card " +
			   " FROM (" + MemberOperationDao.makeSql(staff, extraCond.setOperationType(OperationType.CHARGE), null) + ") AS TMP";
		 
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
			   " FROM (" + MemberOperationDao.makeSql(staff, extraCond.setOperationType(OperationType.REFUND), null) + ") AS TMP";
		 dbCon.rs = dbCon.stmt.executeQuery(sql);
		 
		 if(dbCon.rs.next()){
			 incomeByCharge.setRefundAmount(dbCon.rs.getInt("refund_amount"));
			 incomeByCharge.setTotalActualRefund(Math.abs(dbCon.rs.getFloat("total_actual_refund")));
			 incomeByCharge.setTotalAccountRefund(Math.abs(dbCon.rs.getFloat("total_account_refund")));
		 }
		 
		 dbCon.rs.close();
		 
		 return incomeByCharge;
		 
	 }	
	 
//	 private static String makeSql(Staff staff, DutyRange range, ExtraCond extraCond){
//		 String sql;
//		 sql = " SELECT * FROM " + Params.dbName + "." + extraCond.dbTbl.moTbl +
//			   " WHERE 1 = 1 " +
//			   " AND restaurant_id = " + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) +
//			   " AND operate_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
//		 	   (extraCond != null ? extraCond.toString() : "");
//		 return sql;
//	 }
	 
	/**
	 * Calculate the summary to each member.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to summary of each member
	 * @throws BusinessException
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<SummaryByEachMember> calcByEachMember(Staff staff, MemberOperationDao.ExtraCond extraCond) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcByEachMember(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
		
	/**
	 * Calculate the summary to each member.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to summary of each member
	 * @throws BusinessException
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<SummaryByEachMember> calcByEachMember(DBCon dbCon, Staff staff, MemberOperationDao.ExtraCond extraCond) throws BusinessException, SQLException{
		String sql;
		sql = " SELECT TMP.member_id, MAX(TMP.member_name) AS member_name, MAX(TMP.member_mobile) AS member_mobile, MAX(TMP.member_card) AS member_card " +
			  " ,SUM(IF(TMP.operate_type = " + MemberOperation.OperationType.CHARGE.getValue() + " ,TMP.delta_base_money, 0)) AS charge_actual " +
			  " ,SUM(IF(TMP.operate_type = " + MemberOperation.OperationType.CHARGE.getValue() + " ,TMP.delta_base_money + TMP.delta_extra_money, 0)) AS charge_money " +
			  " ,SUM(IF(TMP.operate_type = " + MemberOperation.OperationType.REFUND.getValue() + " ,TMP.delta_base_money, 0)) AS refund_actual " +
			  " ,SUM(IF(TMP.operate_type = " + MemberOperation.OperationType.REFUND.getValue() + " ,TMP.delta_extra_money, 0)) AS refund_money " +
			  " ,SUM(IF(TMP.operate_type = " + MemberOperation.OperationType.CONSUME.getValue() + " OR TMP.operate_type = " + MemberOperation.OperationType.RE_CONSUME.getValue() + " ,TMP.delta_base_money, 0)) AS consume_base " +
			  " ,SUM(IF(TMP.operate_type = " + MemberOperation.OperationType.CONSUME.getValue() + " OR TMP.operate_type = " + MemberOperation.OperationType.RE_CONSUME.getValue() + " ,TMP.delta_extra_money, 0)) AS consume_extra " +
			  " ,SUM(IF(TMP.operate_type = " + MemberOperation.OperationType.CONSUME.getValue() + " OR TMP.operate_type = " + MemberOperation.OperationType.RE_CONSUME.getValue() + " ,TMP.pay_money, 0)) AS consume_total " +
			  " ,SUM(IF(TMP.delta_point <> 0 " + " ,TMP.delta_point, 0)) AS changed_point " +
			  " FROM ( " + MemberOperationDao.makeSql(staff, extraCond, null) + " ) AS TMP " +
			  " GROUP BY TMP.member_id "; 
		
		final List<SummaryByEachMember> result = SortedList.newInstance(new Comparator<SummaryByEachMember>() {
			@Override
			public int compare(SummaryByEachMember o1, SummaryByEachMember o2) {
				return o1.getMember().compareTo(o2.getMember());
			}
		});
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			SummaryByEachMember summary = new SummaryByEachMember();
			Member member = new Member(dbCon.rs.getInt("member_id"));
			member.setName(dbCon.rs.getString("member_name"));
			member.setMobile(dbCon.rs.getString("member_mobile"));
			member.setMemberCard(dbCon.rs.getString("member_card"));
			summary.setMember(member);
			//充值实收
			summary.setChargeActual(dbCon.rs.getFloat("charge_actual"));
			//充值实充
			summary.setChargeMoney(dbCon.rs.getFloat("charge_money"));
			//取款实退
			summary.setRefundActual(dbCon.rs.getFloat("refund_actual"));
			//取款实扣
			summary.setRefundMoney(dbCon.rs.getFloat("refund_money"));
			//基础消费扣额
			summary.setConsumeBase(dbCon.rs.getFloat("consume_base"));
			//赠送消费扣额
			summary.setConsumeExtra(dbCon.rs.getFloat("consume_extra"));
			//消费金额
			summary.setConsumeTotal(dbCon.rs.getFloat("consume_total"));
			//变动积分
			summary.setChangedPoint(dbCon.rs.getInt("changed_point"));
			result.add(summary);
		}
		
		sql = " SELECT * FROM " + Params.dbName + "." + extraCond.dbTbl.moTbl + " WHERE id IN ( " +
				" SELECT MAX(TMP.id) AS last_member_operation " +
				" FROM ( " + MemberOperationDao.makeSql(staff, extraCond, null) + " ) AS TMP " +
				" GROUP BY TMP.member_id " +
			  " ) ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			SummaryByEachMember summary = new SummaryByEachMember();
			summary.setMember(new Member(dbCon.rs.getInt("member_id")));
			int index = result.indexOf(summary);
			if(index >= 0){
				//剩余金额
				result.get(index).setRemainingBalance(dbCon.rs.getFloat("remaining_base_money") + dbCon.rs.getFloat("remaining_extra_money"));
				//基础剩余余额
				result.get(index).setDeltaBase(dbCon.rs.getFloat("remaining_base_money"));
				//赠送剩余余额
				result.get(index).setDeltaExtra(dbCon.rs.getFloat("remaining_extra_money"));
				//剩余积分
				result.get(index).setRemainingPoint(dbCon.rs.getInt("remaining_point"));
			}
		}
		dbCon.rs.close();
		
//		for(SummaryByEachMember summary : result){
//			sql = MemberOperationDao.makeSql(staff, ((MemberOperationDao.ExtraCond)extraCond.clone()).addMember(summary.getMember()), " ORDER BY MO.id DESC LIMIT 1 ");
//			dbCon.rs = dbCon.stmt.executeQuery(sql);
//			if(dbCon.rs.next()){
//				//剩余金额
//				summary.setRemainingBalance(dbCon.rs.getFloat("remaining_base_money") + dbCon.rs.getFloat("remaining_extra_money"));
//				//基础剩余余额
//				summary.setDeltaBase(dbCon.rs.getFloat("remaining_base_money"));
//				//赠送剩余余额
//				summary.setDeltaExtra(dbCon.rs.getFloat("remaining_extra_money"));
//				//剩余积分
//				summary.setRemainingPoint(dbCon.rs.getInt("remaining_point"));
//			}
//			dbCon.rs.close();
//		}
		
		return result;
	}
}
