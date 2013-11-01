package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.IncomeByEachDay;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DateType;

public class QueryIncomeStatisticsDao {
	
	/**
	 * Get income to each day during on & off duty.
	 * @param staff
	 * 			the staff to perform this action
	 * @param onDuty
	 * 			the on duty
	 * @param offDuty
	 * 			the off duty
	 * @return the income by each during on & off duty
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the on or off duty string
	 */
	public static List<IncomeByEachDay> getIncomeByEachDay(Staff staff, String onDuty, String offDuty) throws SQLException, ParseException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getIncomeByEachDay(dbCon, staff, onDuty, offDuty);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get income to each day during on & off duty.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param onDuty
	 * 			the on duty
	 * @param offDuty
	 * 			the off duty
	 * @return the income by each during on & off duty
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the on or off duty string
	 */
	public static List<IncomeByEachDay> getIncomeByEachDay(DBCon dbCon, Staff staff, String onDuty, String offDuty) throws SQLException, ParseException{
		
		List<IncomeByEachDay> result = new ArrayList<IncomeByEachDay>();
		
		Calendar c = Calendar.getInstance();
		Date dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(onDuty);
		Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(offDuty);
		c.setTime(dateBegin);
		while (dateBegin.compareTo(dateEnd) <= 0) {
			c.add(Calendar.DATE, 1);
			
			DutyRange range = QueryDutyRange.exec(dbCon, staff, 
												  DateUtil.format(dateBegin, DateUtil.Pattern.DATE_TIME), 
												  DateUtil.format(c.getTime(), DateUtil.Pattern.DATE_TIME));
			
			IncomeByEachDay income = new IncomeByEachDay(DateUtil.format(c.getTime(), DateUtil.Pattern.DATE));
			
			if(range != null){
				
				//Calculate the general income
				income.setIncomeByPay(CalcBillStatisticsDao.calcIncomeByPayType(dbCon, staff, range, DateType.HISTORY));
				
				//Calculate the total & amount to erase price
				income.setIncomeByErase(CalcBillStatisticsDao.calcErasePrice(dbCon, staff, range, DateType.HISTORY));
				
				//Get the total & amount to discount price
				income.setIncomeByDiscount(CalcBillStatisticsDao.calcDiscountPrice(dbCon, staff, range, DateType.HISTORY));
	
				//Get the total & amount to gift price
				income.setIncomeByGift(CalcBillStatisticsDao.calcGiftPrice(dbCon, staff, range, DateType.HISTORY));
				
				//Get the total & amount to cancel price
				income.setIncomeByCancel(CalcBillStatisticsDao.calcCancelPrice(dbCon, staff, range, DateType.HISTORY));
				
				//Get the total & amount to repaid order
				income.setIncomeByRepaid(CalcBillStatisticsDao.calcRepaidPrice(dbCon, staff, range, DateType.HISTORY));
				
				//Get the total & amount to order with service
				income.setIncomeByService(CalcBillStatisticsDao.calcServicePrice(dbCon, staff, range, DateType.HISTORY));
				
				//Get the charge income by both cash and credit card
				income.setIncomeByCharge(CalcBillStatisticsDao.calcIncomeByCharge(dbCon, staff, range, DateType.HISTORY));
				
			}
			result.add(income);
			
			dateBegin = c.getTime();
		}
		
		return Collections.unmodifiableList(result);
	}
	
}
