package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.orderMgr.QueryOrderFoodDao;
import com.wireless.pojo.billStatistics.CancelIncomeByDept;
import com.wireless.pojo.billStatistics.CancelIncomeByReason;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.protocol.OrderFood;
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
	public static List<CancelIncomeByDept> getCancelledFoodByDept(DBCon dbCon, Terminal term, DutyRange range, int queryType, int orderBy) throws SQLException{

//		CancelledFood[] result = null;
//		
//		/**
//		 * Get the duty range between on and off duty date
//		 */
//		DutyRange dutyRange = QueryDutyRange.exec(dbCon, term, range.getOnDuty(), range.getOffDuty());
//		
//		if(dutyRange == null){
//			return null;
//		}
//		
//		SingleOrderFood[] orderFoods = SingleOrderFoodReflector.getDetailHistory(dbCon, 
//				"AND B.restaurant_id=" + term.restaurantID + " " + 
//				"AND B.order_date BETWEEN '" + dutyRange.getOnDuty() + "' AND '" + dutyRange.getOffDuty() + "'", 
//				null);		
//		
//		/**
//		 * Get all the basic info to department 
//		 */
//		HashMap<Department, CancelledFood> deptCancelledFoodDetail = new HashMap<Department, CancelledFood>();
//		Department[] departmentDetail = QueryMenu.queryDepartments(dbCon, " AND DEPT.restaurant_id=" + term.restaurantID, null);
//		for(Department dept : departmentDetail){
//			deptCancelledFoodDetail.put(dept, new CancelledFood(dept.name, ""));
//		}
//		
//		/**
//		 * Put the temporary department
//		 */
//		deptCancelledFoodDetail.put(new Department("临时菜", Department.DEPT_TEMP, term.restaurantID, Department.TYPE_RESERVED), new CancelledFood("临时菜", ""));
//		
//		/**
//		 * Calculate the orderCount to each department during this period
//		 */
//		for(SingleOrderFood singleOrderFood : orderFoods){
//			CancelledFood cancelledDetail = deptCancelledFoodDetail.get(singleOrderFood.kitchen.getDept());
//			if(cancelledDetail != null && singleOrderFood.orderCount < 0){
//				cancelledDetail.setDeptID(singleOrderFood.kitchen.getDept().deptID);
//				cancelledDetail.setCount(Math.abs(cancelledDetail.getCount()) + Math.abs(singleOrderFood.orderCount));
////				bfDetail.setPrice(singleOrderFood.unitPrice);
//				cancelledDetail.setTotalPrice(cancelledDetail.getTotalPrice() + Math.abs(singleOrderFood.orderCount) * singleOrderFood.unitPrice);
//				deptCancelledFoodDetail.put(singleOrderFood.kitchen.getDept(), cancelledDetail);
//			}
//		}
//		
//		Iterator<Map.Entry<Department, CancelledFood>> iter = deptCancelledFoodDetail.entrySet().iterator();
//		while(iter.hasNext()){
//			Entry<Department, CancelledFood> entry = iter.next();
//			CancelledFood val = entry.getValue();
//			if(val.getCount() <= 0){
//				iter.remove();
//			}
//		}
//		
//		result = deptCancelledFoodDetail.values().toArray(new CancelledFood[deptCancelledFoodDetail.values().size()]);
		
		List<CancelIncomeByDept> result = CalcBillStatistics.calcCancelIncomeByDept(dbCon, term, range, null, queryType);		

		
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
	public static List<CancelIncomeByDept> getCancelledFoodByDept(Terminal term, DutyRange range, int queryType, int orderBy) throws SQLException{
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();					
			
			return getCancelledFoodByDept(dbCon, term, range, queryType, orderBy);			
			
		}finally{
			dbCon.disconnect();
		}

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
	public static List<CancelIncomeByReason> getCancelledFoodByReason(Terminal term, DutyRange range, int queryType, int orderBy) throws SQLException{
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();					
			
			return getCancelledFoodByReason(dbCon, term, range, queryType, orderBy);			
			
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
	public static List<CancelIncomeByReason> getCancelledFoodByReason(DBCon dbCon, Terminal term, DutyRange range, int queryType, int orderBy) throws SQLException{
		
		List<CancelIncomeByReason> result = CalcBillStatistics.calcCancelIncomeByReason(dbCon, term, range, null, queryType);		
		
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

	public static OrderFood[] getCancelledFoodDetail(Terminal term, DutyRange range, int queryType, int orderBy, String deptID) throws SQLException{
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
	public static OrderFood[] getCancelledFoodDetail(DBCon dbCon, Terminal term, DutyRange range, int queryType, int orderBy, String deptID) throws SQLException{
		
		String dept = "";
		if(deptID != null && deptID.trim().length() > 0){
			String[] tp = deptID.split(",");
			for(int i = 0; i < tp.length; i++){
				dept += (i > 0 ? "," : "");
				dept += tp[i];
			}
		}
		
		if(queryType == QUERY_HISTORY){
			return QueryOrderFoodDao.getSingleDetailHistory(dbCon, " AND OFH.order_count < 0 " +
																   (dept.length() != 0 && dept != "-1" ? " AND OFH.dept_id IN(" + dept + ")" : "") +
																   " AND OFH.restaurant_id = " + term.restaurantID +
																   " AND OFH.order_date BETWEEN '" + range.getOnDuty() + "' AND '" + range.getOffDuty() + "'", 
															null);
		}else if(queryType == QUERY_TODAY){
			return QueryOrderFoodDao.getSingleDetailToday(dbCon, " AND OF.order_count < 0 " +
																 (dept.length() != 0 && dept != "-1" ? " AND OF.dept_id IN(" + dept + ")" : "") +
					   									   	     " AND OF.restaurant_id = " + term.restaurantID +
					   											 " AND OF.order_date BETWEEN '" + range.getOnDuty() + "' AND '" + range.getOffDuty() + "'", 
					   									  null);
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
	}
	
	
}
