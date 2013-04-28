package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.orderMgr.QueryOrderFoodDao;
import com.wireless.pojo.billStatistics.CancelIncomeByDept;
import com.wireless.pojo.billStatistics.CancelIncomeByDept.IncomeByEachReason;
import com.wireless.pojo.billStatistics.CancelIncomeByReason;
import com.wireless.pojo.billStatistics.CancelIncomeByReason.IncomeByEachDept;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.CancelledFood;
import com.wireless.protocol.Terminal;

public class QueryCancelledFood {
	
	public final static int QUERY_BY_DEPT = 0;		//按部门显示
	public final static int QUERY_BY_REASON = 1;	//按原因显示
	public final static int QUERY_BY_FOOD = 2;		//按菜品显示	
	
	public final static int ORDER_BY_COUNT = 0;		//按数量排序
	public final static int ORDER_BY_PRICE = 1;		//按金额排序
	
	public final static int QUERY_TODAY = CalcBillStatisticsDao.QUERY_TODAY;		//查找当日
	public final static int QUERY_HISTORY = CalcBillStatisticsDao.QUERY_HISTORY;	//查找历史
	
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
	public static CancelIncomeByDept getCancelledFoodByDept(Terminal term, DutyRange range, Integer deptId, int queryType, int orderBy) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getCancelledFoodByDept(dbCon, term, range, deptId, queryType, orderBy);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Summary data
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param deptId
	 * @param queryType
	 * @param orderBy
	 * @return
	 * @throws SQLException
	 */
	public static CancelIncomeByDept getCancelledFoodByDept(DBCon dbCon, Terminal term, DutyRange range, Integer deptId, int queryType, int orderBy) throws SQLException{
		List<CancelIncomeByDept> result = getCancelledFoodByDept(dbCon, term, range, deptId != null && deptId >= 0 ?  " AND OF.dept_id = " + deptId : "", queryType, orderBy);
		CancelIncomeByDept dept = null;
		if(result != null && result.size() > 0){
			if(deptId != null && deptId >= 0){
				dept = result.get(0);
			}else{
				// 部门汇总
				dept = new CancelIncomeByDept();
				List<IncomeByEachReason> rl = dept.getIncomeByEachReason();
				for(CancelIncomeByDept tempDept : result){
					for(IncomeByEachReason tempReason : tempDept.getIncomeByEachReason()){
						if(rl.size() == 0){
							rl.add(tempReason);
						}else{
							boolean cs = true;
							for(IncomeByEachReason rlItem : rl){
								if(rlItem.getReason().getId() == tempReason.getReason().getId()){
									rlItem.setAmount(rlItem.getAmount() + tempReason.getAmount());
									rlItem.setPrice(rlItem.getPrice() + tempReason.getPrice());
									cs = false;
									break;
								}
							}
							if(cs){
								rl.add(tempReason);
							}
						}
					}
				}
				dept.setIncomeByEachReason(rl);
			}
		}
		return dept;
	}
	
	/**
	 * Get the cancelled foods to each departments.
	 * Detailed information.
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
		List<CancelIncomeByDept> result = CalcBillStatisticsDao.calcCancelIncomeByDept(dbCon, term, range, extraCond, queryType);		
		if(result != null && result.size() > 0){
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
	public static CancelIncomeByReason getCancelledFoodByReason(Terminal term, DutyRange range, Integer reasonID, int queryType, int orderBy) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getCancelledFoodByReason(dbCon, term, range, reasonID, queryType, orderBy);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Summary data
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param deptId
	 * @param queryType
	 * @param orderBy
	 * @return
	 * @throws SQLException
	 */
	public static CancelIncomeByReason getCancelledFoodByReason(DBCon dbCon, Terminal term, DutyRange range, Integer reasonID, int queryType, int orderBy) throws SQLException{
		List<CancelIncomeByReason> result = getCancelledFoodByReason(dbCon, term, range, reasonID != null && reasonID > 0 ? " AND OF.cancel_reason_id = " + reasonID : "", queryType, orderBy);
		CancelIncomeByReason reason = null;
		if(result != null && result.size() > 0){
			// 
			if(reasonID != null && reasonID > 0){
				reason = result.get(0);
			}else{
				reason = new CancelIncomeByReason();
				List<IncomeByEachDept> dl = reason.getIncomeByEachDept();
				for(CancelIncomeByReason tempReson : result){
					for(IncomeByEachDept tempDept : tempReson.getIncomeByEachDept()){
						if(dl.size() == 0){
							dl.add(tempDept);
						}else{
							boolean cs = true;
							for(IncomeByEachDept dlItem : dl){
								if(tempDept.getDept().getId() == dlItem.getDept().getId()){
									dlItem.setAmount(dlItem.getAmount() + tempDept.getAmount());
									dlItem.setPrice(dlItem.getPrice() + tempDept.getPrice());
									cs = false;
									break;
								}
							}
							if(cs){
								dl.add(tempDept);
							}
						}
					}
				}
				reason.setIncomeByEachDept(dl);
			}
		}
		return reason;
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
		List<CancelIncomeByReason> result = CalcBillStatisticsDao.calcCancelIncomeByReason(dbCon, term, range, extraCond, queryType);		
		if(result != null && result.size() > 0){
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
	public static List<CancelledFood> getCancelledFoodDetail(Terminal term, DutyRange range, int queryType, int orderBy, Integer deptID, Integer reasonID) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getCancelledFoodDetail(dbCon, term, range, queryType, orderBy, deptID, reasonID);
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
	public static List<CancelledFood> getCancelledFoodDetail(DBCon dbCon, Terminal term, DutyRange range, int queryType, int orderBy, Integer deptID, Integer reasonID) throws SQLException{
		List<CancelledFood> list = new ArrayList<CancelledFood>();
		CancelledFood item = null;
		com.wireless.protocol.OrderFood[] of = {};
		
		if(queryType == QUERY_HISTORY){
			of = QueryOrderFoodDao.getSingleDetailHistory(dbCon, " AND OFH.order_count < 0 " +
																   (deptID != null && deptID >= 0 ? " AND OFH.dept_id = " + deptID : "") +
																   (reasonID != null && reasonID > 0 ? " AND OFH.cancel_reason_id = " + reasonID : "") +
																   " AND OFH.restaurant_id = " + term.restaurantID +
																   " AND OFH.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'", 
																   " ORDER BY OFH.order_date ASC ");
		}else if(queryType == QUERY_TODAY){
			of = QueryOrderFoodDao.getSingleDetailToday(dbCon, " AND OF.order_count < 0 " +
																 (deptID != null && deptID >= 0 ? " AND OFH.dept_id = " + deptID : "") +
					   									   	      (reasonID != null && reasonID > 0 ? " AND OFH.cancel_reason_id = " + reasonID : "") +
																 " AND OF.restaurant_id = " + term.restaurantID +
					   											 " AND OF.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'", 
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
