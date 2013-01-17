package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.orderMgr.QueryOrderFoodDao;
import com.wireless.pojo.billStatistics.CancelIncomeByDept;
import com.wireless.pojo.billStatistics.CancelIncomeByReason;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.CancelledFood;
import com.wireless.protocol.Terminal;

public class QueryCancelledFood {
	
	public final static int QUERY_BY_DEPT = 0;		//按部门显示
	public final static int QUERY_BY_REASON = 1;	//按原因显示
	public final static int QUERY_BY_FOOD = 2;		//按菜品显示	
	
	public final static int ORDER_BY_COUNT = 0;		//按数量排序
	public final static int ORDER_BY_PRICE = 1;		//按金额排序
	
	public final static int QUERY_TODAY = CalcBillStatistics.QUERY_TODAY;		//查找当日
	public final static int QUERY_HISTORY = CalcBillStatistics.QUERY_HISTORY;	//查找历史
	
	/**
	 * 
	 * @param term
	 * @param range
	 * @param deptId
	 * @param queryType
	 * @param orderBy
	 * @return
	 * @throws SQLException
	 */
	public static CancelIncomeByDept getCancelledFoodByDept(Terminal term, DutyRange range, int deptId, int queryType, int orderBy) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getCancelledFoodByDept(dbCon, term, range, deptId, queryType, orderBy);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param deptId
	 * @param queryType
	 * @param orderBy
	 * @return
	 * @throws SQLException
	 */
	public static CancelIncomeByDept getCancelledFoodByDept(DBCon dbCon, Terminal term, DutyRange range, int deptId, int queryType, int orderBy) throws SQLException{
		return getCancelledFoodByDept(dbCon, term, range, " AND OF.dept_id = " + deptId, queryType, orderBy).get(0);
	}
	
	/**
	 * Get the cancelled foods to each departments.
	 * @param DBCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to be queried
	 * @param range
	 * 			the date range to query
	 * @param orderBy
	 * 			the category in which the list sort
	 * @return
	 */
	public static List<CancelIncomeByDept> getCancelledFoodByDept(DBCon dbCon, Terminal term, DutyRange range, String extraCond, int queryType, int orderBy) throws SQLException{

		List<CancelIncomeByDept> result = CalcBillStatistics.calcCancelIncomeByDept(dbCon, term, range, extraCond, queryType);		

		
		if(orderBy == QueryCancelledFood.ORDER_BY_COUNT){
			Collections.sort(result, new Comparator<CancelIncomeByDept>(){
				@Override
				public int compare(CancelIncomeByDept r1, CancelIncomeByDept r2) {
					if(r1.getTotalCancelAmount() > r2.getTotalCancelAmount()){
						return -1;
					}else if(r1.getTotalCancelAmount() < r2.getTotalCancelAmount()){
						return 1;
					}else{
						return 0;
					}
				}					
			});
		}else if(orderBy == QueryCancelledFood.ORDER_BY_PRICE){
			Collections.sort(result, new Comparator<CancelIncomeByDept>(){
				@Override
				public int compare(CancelIncomeByDept r1, CancelIncomeByDept r2) {
					if(r1.getTotalCancelAmount() > r2.getTotalCancelAmount()){
						return -1;
					}else if(r1.getTotalCancelAmount() < r2.getTotalCancelAmount()){
						return 1;
					}else{
						return 0;
					}
				}					
			});
		}
		
		return result;
	}
	
	/**
	 * Get the cancelled foods to each departments.
	 * @param pin
	 * 			the pin to query
	 * @param queryDate
	 * 			the date range to query
	 * @param orderBy
	 * 			the category in which the list sort
	 * @return
	 */
	public static List<CancelIncomeByDept> getCancelledFoodByDept(Terminal term, DutyRange range, String extraCond, int queryType, int orderBy) throws SQLException{
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();					
			
			return getCancelledFoodByDept(dbCon, term, range, extraCond, queryType, orderBy);			
			
		}finally{
			dbCon.disconnect();
		}

	}

	/**
	 * 
	 * @param term
	 * @param range
	 * @param deptId
	 * @param queryType
	 * @param orderBy
	 * @return
	 * @throws SQLException
	 */
	public static CancelIncomeByReason getCancelledFoodByReason(Terminal term, DutyRange range, int deptId, int queryType, int orderBy) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getCancelledFoodByReason(dbCon, term, range, deptId, queryType, orderBy);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param deptId
	 * @param queryType
	 * @param orderBy
	 * @return
	 * @throws SQLException
	 */
	public static CancelIncomeByReason getCancelledFoodByReason(DBCon dbCon, Terminal term, DutyRange range, int deptId, int queryType, int orderBy) throws SQLException{
		List<CancelIncomeByReason> result = getCancelledFoodByReason(dbCon, term, range, " AND OF.dept_id = " + deptId, queryType, orderBy);
		return result.get(0);
	}
	
	/**
	 * 
	 * @param term
	 * @param range
	 * @param queryType
	 * @param orderBy
	 * @return
	 * @throws SQLException
	 */
	public static List<CancelIncomeByReason> getCancelledFoodByReason(Terminal term, DutyRange range, String extraCond, int queryType, int orderBy) throws SQLException{
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();					
			
			return getCancelledFoodByReason(dbCon, term, range, extraCond, queryType, orderBy);			
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param queryType
	 * @param orderBy
	 * @return
	 * @throws SQLException
	 */
	public static List<CancelIncomeByReason> getCancelledFoodByReason(DBCon dbCon, Terminal term, DutyRange range, String extraCond, int queryType, int orderBy) throws SQLException{
		
		List<CancelIncomeByReason> result = CalcBillStatistics.calcCancelIncomeByReason(dbCon, term, range, extraCond, queryType);		
		
		if(orderBy == QueryCancelledFood.ORDER_BY_COUNT){
			Collections.sort(result, new Comparator<CancelIncomeByReason>(){
				@Override
				public int compare(CancelIncomeByReason r1, CancelIncomeByReason r2) {
					if(r1.getTotalCancelAmount() > r2.getTotalCancelAmount()){
						return -1;
					}else if(r1.getTotalCancelAmount() < r2.getTotalCancelAmount()){
						return 1;
					}else{
						return 0;
					}
				}					
			});
		}else if(orderBy == QueryCancelledFood.ORDER_BY_PRICE){
			Collections.sort(result, new Comparator<CancelIncomeByReason>(){
				@Override
				public int compare(CancelIncomeByReason r1, CancelIncomeByReason r2) {
					if(r1.getTotalCancelAmount() > r2.getTotalCancelAmount()){
						return -1;
					}else if(r1.getTotalCancelAmount() < r2.getTotalCancelAmount()){
						return 1;
					}else{
						return 0;
					}
				}					
			});
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param term
	 * @param range
	 * @param queryType
	 * @param orderBy
	 * @param deptID
	 * @return
	 * @throws SQLException
	 */
	public static List<CancelledFood> getCancelledFoodDetail(Terminal term, DutyRange range, int queryType, int orderBy, String deptID) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getCancelledFoodDetail(dbCon, term, range, queryType, orderBy, deptID);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param pin
	 * @param duty
	 * @param orderBy
	 * @param deptID
	 * @return
	 */
	public static List<CancelledFood> getCancelledFoodDetail(DBCon dbCon, Terminal term, DutyRange range, int queryType, int orderBy, String deptID) throws SQLException{
		List<CancelledFood> list = new ArrayList<CancelledFood>();
		CancelledFood item = null;
		com.wireless.protocol.OrderFood[] of = {};
		String dept = "";
		if(deptID != null && deptID.trim().length() > 0){
			String[] tp = deptID.split(",");
			for(int i = 0; i < tp.length; i++){
				dept += (i > 0 ? "," : "");
				dept += tp[i];
			}
		}
		
		if(queryType == QUERY_HISTORY){
			of = QueryOrderFoodDao.getSingleDetailHistory(dbCon, " AND OFH.order_count < 0 " +
																   (dept.length() != 0 && !dept.equals("-1") ? " AND OFH.dept_id IN(" + dept + ")" : "") +
																   " AND OFH.restaurant_id = " + term.restaurantID +
																   " AND OFH.order_date BETWEEN '" + range.getOnDuty() + "' AND '" + range.getOffDuty() + "'", 
																   " ORDER BY OFH.order_date ASC ");
		}else if(queryType == QUERY_TODAY){
			of = QueryOrderFoodDao.getSingleDetailToday(dbCon, " AND OF.order_count < 0 " +
																 (dept.length() != 0 && !dept.equals("-1") ? " AND OF.dept_id IN(" + dept + ")" : "") +
					   									   	     " AND OF.restaurant_id = " + term.restaurantID +
					   											 " AND OF.order_date BETWEEN '" + range.getOnDuty() + "' AND '" + range.getOffDuty() + "'", 
					   									  		 " ORDER BY OF.order_date ASC ");
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		for(int i = 0; i < of.length; i++){
			item = new com.wireless.pojo.dishesOrder.CancelledFood(of[i]);
			list.add(item);
			item = null;
		}
		return list;
	}
	
	
}
