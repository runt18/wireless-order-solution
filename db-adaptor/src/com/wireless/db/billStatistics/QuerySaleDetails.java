package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.wireless.db.DBCon;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.IncomeByFood;
import com.wireless.pojo.billStatistics.IncomeByKitchen;
import com.wireless.pojo.billStatistics.SalesDetail;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;

public class QuerySaleDetails {
	
	public final static int QUERY_TODAY = CalcBillStatisticsDao.QUERY_TODAY;			//查询当日账单
	public final static int QUERY_HISTORY = CalcBillStatisticsDao.QUERY_HISTORY;		//查询历史账单
	
	public final static int QUERY_BY_DEPT = 0;		//按部门显示
	public final static int QUERY_BY_FOOD = 1;		//按菜品显示
	public final static int QUERY_BY_KITCHEN = 2;	//按分厨显示
	
	public final static int ORDER_BY_PROFIT = 0;	//按毛利排序
	public final static int ORDER_BY_SALES = 1;		//按销量排序
	
	/**
	 * 
	 * @param term
	 * @param onDuty
	 * @param offDuty
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static SalesDetail[] execByDept(Staff term, String onDuty, String offDuty, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execByDept(dbCon, term, onDuty, offDuty, queryType);
		}finally{
			dbCon.disconnect();
		}
	}	
	
	/**
	 * Get the sales details to each department.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to query
	 * @param onDuty
	 * 			the on duty to query
	 * @param offDuty
	 * 			the off duty to query
	 * @param queryType
	 * 			The query type
	 * @return
	 * 			an array containing department sales details which is sorted by profit in descending order
	 * @throws SQLException
	 * 			throws if any error occurred while execute any SQL statements.
	 */
	public static SalesDetail[] execByDept(DBCon dbCon, Staff term, String onDuty, String offDuty, int queryType) throws SQLException{
		List<IncomeByDept> deptIncomes;

		if(queryType == QUERY_HISTORY){
			
			/**
			 * Get the duty range between on and off duty date
			 */
			DutyRange dutyRange = QueryDutyRange.exec(dbCon, term, onDuty, offDuty);
			
			if(dutyRange == null){
				return new SalesDetail[0];
			}
			
			//Calculate the incomes to each department.
			deptIncomes = CalcBillStatisticsDao.calcIncomeByDept(dbCon, term, dutyRange, null, queryType);
		}else{
			//Calculate the incomes to each department.
			deptIncomes = CalcBillStatisticsDao.calcIncomeByDept(dbCon, term, new DutyRange(onDuty, offDuty), null, queryType);
		}
		
		HashMap<Department, SalesDetail> deptSalesDetail = new HashMap<Department, SalesDetail>();
		for(IncomeByDept deptIncome : deptIncomes){
			SalesDetail salesDetail = new SalesDetail(deptIncome.getDept());
			salesDetail.setGifted(deptIncome.getGift());
			salesDetail.setDiscount(deptIncome.getDiscount());
			salesDetail.setIncome(deptIncome.getIncome());
			deptSalesDetail.put(deptIncome.getDept(), salesDetail);
		}
		
		/**
		 * Calculate the cost to each department during this period
		 */
		/*for(MaterialDetail materialDetail : materialDetails){
			SalesDetail salesDetail = deptSalesDetail.get(materialDetail.dept);
			if(salesDetail != null){
				salesDetail.setCost(salesDetail.getCost() + Math.abs(materialDetail.calcPrice()));
			}
		}*/

		/**
		 * Remove the invalid department sales detail record
		 */
		Iterator<Map.Entry<Department, SalesDetail>> iter = deptSalesDetail.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<Department, SalesDetail> entry = iter.next();
			SalesDetail saleDetail = entry.getValue();
			if(saleDetail.getGifted() == 0 && saleDetail.getIncome() == 0 &&
			   saleDetail.getDiscount() == 0 && saleDetail.getCost() == 0){
				iter.remove();
			}
		}
			
		/**
		 * Calculate the profit, cost rate, profit rate to each department
		 */
		for(Entry<Department, SalesDetail> entry : deptSalesDetail.entrySet()){
			SalesDetail detail = entry.getValue();
			detail.setGifted((float)Math.round(detail.getGifted() * 100) / 100);
			detail.setDiscount((float)Math.round(detail.getDiscount() * 100) / 100);
			detail.setIncome((float)Math.round(detail.getIncome() * 100) / 100);
			detail.setCost((float)Math.round(detail.getCost() * 100) / 100);
				
			detail.setProfit(detail.getIncome() - detail.getCost());
			if(detail.getIncome() != 0.00){
				detail.setProfitRate(detail.getProfit() / detail.getIncome());
				detail.setCostRate(detail.getCost() / detail.getIncome());
			}
			
			entry.setValue(detail);
		}
			
		SalesDetail[] result = deptSalesDetail.values().toArray(new SalesDetail[deptSalesDetail.values().size()]);
		/**
		 * Sort the department sales detail in descending order by profit
		 */
		Arrays.sort(result, new Comparator<SalesDetail>(){

			@Override
			public int compare(SalesDetail result1, SalesDetail result2) {
				if(result1.getProfit() == result2.getProfit()){
					return 0;
				}else if(result1.getProfit() > result2.getProfit()){
					return -1;
				}else{
					return 1;
				}
			}
				
		});
			
		return result;
	}

	/**
	 * 
	 * @param term
	 * @param onDuty
	 * @param offDuty
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static SalesDetail[] execByKitchen(Staff term, String onDuty, String offDuty, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execByKitchen(dbCon, term, onDuty, offDuty, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param onDuty
	 * @param offDuty
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static SalesDetail[] execByKitchen(DBCon dbCon, Staff term, String onDuty, String offDuty, int queryType) throws SQLException{
		
		List<IncomeByKitchen> kitchenIncomes;

		if(queryType == QUERY_HISTORY){
			
			/**
			 * Get the duty range between on and off duty date
			 */
			DutyRange dutyRange = QueryDutyRange.exec(dbCon, term, onDuty, offDuty);
			
			if(dutyRange == null){
				return new SalesDetail[0];
			}
			//Calculate the incomes to each kitchen.
			kitchenIncomes = CalcBillStatisticsDao.calcIncomeByKitchen(dbCon, term, dutyRange, null, queryType);
			
		}else{
			
			//Calculate the incomes to each kitchen.
			kitchenIncomes = CalcBillStatisticsDao.calcIncomeByKitchen(dbCon, term, new DutyRange(onDuty, offDuty), null, queryType);
		}
		
		HashMap<Kitchen, SalesDetail> kitchenSalesDetail = new HashMap<Kitchen, SalesDetail>();
		for(IncomeByKitchen kitchenIncome : kitchenIncomes){
			SalesDetail salesDetail = new SalesDetail(kitchenIncome.getKitchen());
			salesDetail.setGifted(kitchenIncome.getGift());
			salesDetail.setDiscount(kitchenIncome.getDiscount());
			salesDetail.setIncome(kitchenIncome.getIncome());
			kitchenSalesDetail.put(kitchenIncome.getKitchen(), salesDetail);
		}

		/**
		 * Remove the invalid kitchen sales detail record
		 */
		Iterator<Map.Entry<Kitchen, SalesDetail>> iter = kitchenSalesDetail.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<Kitchen, SalesDetail> entry = iter.next();
			SalesDetail saleDetail = entry.getValue();
			if(saleDetail.getGifted() == 0 && saleDetail.getIncome() == 0 &&
			   saleDetail.getDiscount() == 0 && saleDetail.getCost() == 0){
				iter.remove();
			}
		}
			
		/**
		 * Calculate the profit, cost rate, profit rate to each kitchen
		 */
		for(Entry<Kitchen, SalesDetail> entry : kitchenSalesDetail.entrySet()){
			SalesDetail detail = entry.getValue();
			detail.setGifted((float)Math.round(detail.getGifted() * 100) / 100);
			detail.setDiscount((float)Math.round(detail.getDiscount() * 100) / 100);
			detail.setIncome((float)Math.round(detail.getIncome() * 100) / 100);
			detail.setCost((float)Math.round(detail.getCost() * 100) / 100);
				
			detail.setProfit(detail.getIncome() - detail.getCost());
			if(detail.getIncome() != 0.00){
				detail.setProfitRate(detail.getProfit() / detail.getIncome());
				detail.setCostRate(detail.getCost() / detail.getIncome());
			}
			
			entry.setValue(detail);
		}
			
		SalesDetail[] result = kitchenSalesDetail.values().toArray(new SalesDetail[kitchenSalesDetail.values().size()]);
		/**
		 * Sort the kitchen sales detail in descending order by profit
		 */
		Arrays.sort(result, new Comparator<SalesDetail>(){

			@Override
			public int compare(SalesDetail result1, SalesDetail result2) {
				if(result1.getProfit() == result2.getProfit()){
					return 0;
				}else if(result1.getProfit() > result2.getProfit()){
					return -1;
				}else{
					return 1;
				}
			}
				
		});
			
		return result;
	}
	

	/**
	 * 
	 * @param term
	 * @param onDuty
	 * @param offDuty
	 * @param deptID
	 * @param orderType
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static SalesDetail[] execByFood(Staff term, String onDuty, String offDuty, int[] deptID, int orderType, int queryType, String foodName) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execByFood(dbCon, term, onDuty, offDuty, deptID, orderType, queryType, foodName);
		}finally{
			dbCon.disconnect();
		}
	}	
	
	/**
	 * Get the sales details to each food of one or more departments.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to query
	 * @param onDuty
	 * 			the on duty to query
	 * @param offDuty
	 * 			the off duty to query
	 * @param deptID
	 * @param orderType
	 * 			The order type.
	 * @param queryType
	 * 			The query type.
	 * @return
	 * 			an array containing department sales details which is sorted by profit in descending order
	 * @throws SQLException
	 * 			throws if any error occurred while execute any SQL statements.
	 */
	public static SalesDetail[] execByFood(DBCon dbCon, Staff term, String onDuty, String offDuty, int[] deptID, int orderType, int queryType, String foodName) throws SQLException{
		
		StringBuffer deptCond = new StringBuffer();
		if(deptID.length != 0){
			for(int i = 0; i < deptID.length; i++){
				if(i == 0){
					deptCond.append(Integer.toString(deptID[0]));
				}else{
					deptCond.append(",").append(deptID[i]);
				}
			}
		}
		
		List<IncomeByFood> foodIncomes;
		
		if(queryType == QUERY_HISTORY){
			
			/**
			 * Get the duty range between on and off duty date
			 */
			DutyRange dutyRange = QueryDutyRange.exec(dbCon, term, onDuty, offDuty);
			
			if(dutyRange == null){
				return new SalesDetail[0];
			}
			
			foodIncomes = CalcBillStatisticsDao.calcIncomeByFood(dbCon, 
				term, 
				dutyRange,
				(foodName != null && !foodName.trim().isEmpty() ? " AND OF.name LIKE '%" + foodName + "%'" : "") +
				(deptID.length != 0 ? " AND OF.dept_id IN(" + deptCond + ")" : ""), 
				queryType
			);
		}else{
			foodIncomes = CalcBillStatisticsDao.calcIncomeByFood(dbCon, 
				term,
				new DutyRange(onDuty, offDuty),
				(foodName != null && !foodName.trim().isEmpty() ? " AND OF.name LIKE '%" + foodName + "%'" : "") +
				(deptID.length != 0 ? " AND OF.dept_id IN(" + deptCond + ")" : ""), 
				queryType
			);
		}
		
		HashMap<Food, SalesDetail> foodSalesDetail = new HashMap<Food, SalesDetail>();
		
		for(IncomeByFood foodIncome : foodIncomes){
			SalesDetail detail = new SalesDetail(foodIncome.getFood());
			detail.setDiscount(foodIncome.getDiscount());
			detail.setGifted(foodIncome.getGift());
			detail.setIncome(foodIncome.getIncome());
			detail.setSalesAmount(foodIncome.getSaleAmount());
			foodSalesDetail.put(foodIncome.getFood(), detail);
		}
		
		/**
		 * Calculate the profit, cost rate, profit rate, average price, average cost to each food
		 */
		for(Map.Entry<Food, SalesDetail> entry : foodSalesDetail.entrySet()){
			
			SalesDetail detail = entry.getValue();			
				
			detail.setGifted((float)Math.round(detail.getGifted() * 100) / 100);
			detail.setDiscount((float)Math.round(detail.getDiscount() * 100) / 100);
			detail.setIncome((float)Math.round(detail.getIncome() * 100) / 100);
			detail.setCost((float)Math.round(detail.getCost() * 100) / 100);
				
			detail.setProfit(detail.getIncome() - detail.getCost());
			if(detail.getIncome() != 0.00){
				detail.setProfitRate(detail.getProfit() / detail.getIncome());
				detail.setCostRate(detail.getCost() / detail.getIncome());
			}
			
			if(detail.getSalesAmount() != 0.00){
				detail.setAvgPrice((float)Math.round(detail.getIncome() / detail.getSalesAmount() * 100) / 100);
				detail.setAvgCost((float)Math.round(detail.getCost() / detail.getSalesAmount() * 100) /100);
			}
			
			entry.setValue(detail)	;
		}
		
		SalesDetail[] result = foodSalesDetail.values().toArray(new SalesDetail[foodSalesDetail.values().size()]);
		
		if(orderType == QuerySaleDetails.ORDER_BY_PROFIT){
			Arrays.sort(result, new Comparator<SalesDetail>(){
				@Override
				public int compare(SalesDetail o1, SalesDetail o2) {
					if(o1.getProfit() == o2.getProfit()){
						return 0;
					}else if(o1.getProfit() > o2.getProfit()){
						return -1;
					}else{
						return 1;
					}
				}				
			});
		}else if(orderType == QuerySaleDetails.ORDER_BY_SALES){
			Arrays.sort(result, new Comparator<SalesDetail>(){
				@Override
				public int compare(SalesDetail o1, SalesDetail o2) {
					if(o1.getSalesAmount() == o2.getSalesAmount()){
						return 0;
					}else if(o1.getSalesAmount() > o2.getSalesAmount()){
						return -1;
					}else{
						return 1;
					}
				}				
			});
		}
			
		return result;
	}
	
}
