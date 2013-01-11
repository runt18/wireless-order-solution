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
import com.wireless.db.Params;
import com.wireless.db.QueryMenu;
import com.wireless.dbObject.MaterialDetail;
import com.wireless.dbObject.SingleOrderFood;
import com.wireless.dbReflect.MaterialDetailReflector;
import com.wireless.dbReflect.SingleOrderFoodReflector;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.IncomeByKitchen;
import com.wireless.pojo.billStatistics.SalesDetail;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Terminal;

public class QuerySaleDetails {
	
	public final static int QUERY_TODAY = CalcBillStatistics.QUERY_TODAY;			//查询当日账单
	public final static int QUERY_HISTORY = CalcBillStatistics.QUERY_HISTORY;		//查询历史账单
	
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
	public static SalesDetail[] execByDept(Terminal term, String onDuty, String offDuty, int queryType) throws SQLException{
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
	public static SalesDetail[] execByDept(DBCon dbCon, Terminal term, String onDuty, String offDuty, int queryType) throws SQLException{
		
		//SingleOrderFood[] orderFoods = new SingleOrderFood[0];
		List<IncomeByDept> deptIncomes;
		MaterialDetail[] materialDetails = new MaterialDetail[0];

		if(queryType == QUERY_HISTORY){
			
			/**
			 * Get the duty range between on and off duty date
			 */
			DutyRange dutyRange = QueryDutyRange.exec(dbCon, term, onDuty, offDuty);
			
			if(dutyRange == null){
				return new SalesDetail[0];
			}
			
//			/**
//			 * Get the single order food information to history.
//			 */
//			orderFoods = SingleOrderFoodReflector.getDetailHistory(dbCon, 
//								"AND B.restaurant_id=" + term.restaurantID + " " + 
//								"AND B.order_date BETWEEN '" + dutyRange.getOnDuty() + "' AND '" + dutyRange.getOffDuty() + "'", 
//								null);
			
			//Calculate the incomes to each department.
			deptIncomes = CalcBillStatistics.calcIncomeByDept(dbCon, term, dutyRange, null, queryType);
			
			/**
			 * Get the material detail information.
			 */
			materialDetails = MaterialDetailReflector.getMaterialDetail(dbCon, 
								" AND MATE_DETAIL.restaurant_id=" + term.restaurantID + " " +
								" AND MATE_DETAIL.type=" + MaterialDetail.TYPE_CONSUME +
								" AND MATE_DETAIL.date BETWEEN '" + dutyRange.getOnDuty() + "' AND '" + dutyRange.getOffDuty() + "'", 
								"");
			
		}else{
//			/**
//			 * Get the single order food information to today.
//			 */
//			orderFoods = SingleOrderFoodReflector.getDetailToday(dbCon, 
//								"AND B.restaurant_id=" + term.restaurantID + " " + 
//								"AND B.order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'", 
//								null);
			
			//Calculate the incomes to each department.
			deptIncomes = CalcBillStatistics.calcIncomeByDept(dbCon, term, new DutyRange(onDuty, offDuty), null, queryType);
			/**
			 * Get the material detail information.
			 */
			materialDetails = MaterialDetailReflector.getMaterialDetail(dbCon, 
								" AND MATE_DETAIL.restaurant_id=" + term.restaurantID + " " +
								" AND MATE_DETAIL.type=" + MaterialDetail.TYPE_CONSUME +
								" AND MATE_DETAIL.date BETWEEN '" + onDuty + "' AND '" + offDuty + "'", 
								"");
		}
		
		HashMap<Department, SalesDetail> deptSalesDetail = new HashMap<Department, SalesDetail>();
		for(IncomeByDept deptIncome : deptIncomes){
			SalesDetail salesDetail = new SalesDetail(deptIncome.getDept());
			salesDetail.setGifted(deptIncome.getGift());
			salesDetail.setDiscount(deptIncome.getDiscount());
			salesDetail.setIncome(deptIncome.getIncome());
			deptSalesDetail.put(deptIncome.getDept(), salesDetail);
		}
		
//		/**
//		 * Get all the basic info to department 
//		 */
//		
//		for(Department dept : QueryMenu.queryDepartments(dbCon, "AND restaurant_id=" + term.restaurantID, null)){
//			deptSalesDetail.put(dept, new SalesDetail(dept.name));
//		}
//		/**
//		 * Put the temporary department
//		 */
//		deptSalesDetail.put(new Department("临时菜", Department.DEPT_TEMP, term.restaurantID, Department.TYPE_RESERVED), new SalesDetail("临时菜"));
//			
//		/**
//		 * Calculate the gift, discount, income to each department during this period
//		 */
//		for(SingleOrderFood singleOrderFood : orderFoods){
//			SalesDetail salesDetail = deptSalesDetail.get(singleOrderFood.kitchen.dept);
//	
//			if(salesDetail != null){
//				if(singleOrderFood.food.isGift()){
//					salesDetail.setGifted(salesDetail.getGifted() + singleOrderFood.calcPriceWithTaste());
//				}else{
//					salesDetail.setIncome(salesDetail.getIncome() + singleOrderFood.calcPriceWithTaste());
//				}
//					
//				if(singleOrderFood.discount < 1){
//					salesDetail.setDiscount(salesDetail.getDiscount() + singleOrderFood.calcDiscountPrice());
//				}
//					
//				deptSalesDetail.put(singleOrderFood.kitchen.dept, salesDetail);
//			}
//		}
			
		/**
		 * Calculate the cost to each department during this period
		 */
		for(MaterialDetail materialDetail : materialDetails){
			SalesDetail salesDetail = deptSalesDetail.get(materialDetail.dept);
			if(salesDetail != null){
				salesDetail.setCost(salesDetail.getCost() + Math.abs(materialDetail.calcPrice()));
			}
		}

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
//		for(Department dept : deptSalesDetail.keySet()){
//			SalesDetail salesDetail = deptSalesDetail.get(dept);
//				
//			salesDetail.setGifted((float)Math.round(salesDetail.getGifted() * 100) / 100);
//			salesDetail.setDiscount((float)Math.round(salesDetail.getDiscount() * 100) / 100);
//			salesDetail.setIncome((float)Math.round(salesDetail.getIncome() * 100) / 100);
//			salesDetail.setCost((float)Math.round(salesDetail.getCost() * 100) / 100);
//				
//			salesDetail.setProfit(salesDetail.getIncome() - salesDetail.getCost());
//			if(salesDetail.getIncome() != 0.00){
//				salesDetail.setProfitRate(salesDetail.getProfit() / salesDetail.getIncome());
//				salesDetail.setCostRate(salesDetail.getCost() / salesDetail.getIncome());
//			}
//			deptSalesDetail.put(dept, salesDetail);
//		}
			
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
	public static SalesDetail[] execByKitchen(Terminal term, String onDuty, String offDuty, int queryType) throws SQLException{
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
	public static SalesDetail[] execByKitchen(DBCon dbCon, Terminal term, String onDuty, String offDuty, int queryType) throws SQLException{
		
		List<IncomeByKitchen> kitchenIncomes;

		if(queryType == QUERY_HISTORY){
			
			/**
			 * Get the duty range between on and off duty date
			 */
			DutyRange dutyRange = QueryDutyRange.exec(dbCon, term, onDuty, offDuty);
			
			if(dutyRange == null){
				return new SalesDetail[0];
			}
			//Calculate the incomes to each department.
			kitchenIncomes = CalcBillStatistics.calcIncomeByKitchen(dbCon, term, dutyRange, null, queryType);
			
		}else{
			
			//Calculate the incomes to each department.
			kitchenIncomes = CalcBillStatistics.calcIncomeByKitchen(dbCon, term, new DutyRange(onDuty, offDuty), null, queryType);
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
		 * Remove the invalid department sales detail record
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
		 * Calculate the profit, cost rate, profit rate to each department
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
	 * @param deptID
	 * @param orderType
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static SalesDetail[] execByFood(Terminal term, String onDuty, String offDuty, int[] deptID, int orderType, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execByFood(dbCon, term, onDuty, offDuty, deptID, orderType, queryType);
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
	public static SalesDetail[] execByFood(DBCon dbCon, Terminal term, String onDuty, String offDuty, int[] deptID, int orderType, int queryType) throws SQLException{
		
		String deptCond = "";
		if(deptID.length != 0){
			for(int i = 0; i < deptID.length; i++){
				if(i == 0){
					deptCond = Integer.toString(deptID[0]);
				}else{
					deptCond += "," + deptID[i];
				}
			}
		}
		
		SingleOrderFood[] orderFoods = new SingleOrderFood[0];
		MaterialDetail[] materialDetails = new MaterialDetail[0];

		if(queryType == QUERY_HISTORY){
			
			/**
			 * Get the duty range between on and off duty date
			 */
			DutyRange dutyRange = QueryDutyRange.exec(dbCon, term, onDuty, offDuty);
			
			if(dutyRange == null){
				return new SalesDetail[0];
			}
			/**
			 * Get the single order food information to history.
			 */
			orderFoods = SingleOrderFoodReflector.getDetailHistory(dbCon, 
								" AND B.restaurant_id=" + term.restaurantID + " " + 
								" AND B.order_date BETWEEN '" + dutyRange.getOnDuty() + "' AND '" + dutyRange.getOffDuty() + "'" +  
								(deptID.length != 0 ? " AND A.dept_id IN(" + deptCond + ")" : ""),
								null);
			/**
			 * Get the material detail information to history.
			 */
			materialDetails = MaterialDetailReflector.getMaterialDetail(dbCon, 
								" AND MATE_DETAIL.restaurant_id=" + term.restaurantID + " " +
								" AND MATE_DETAIL.type=" + MaterialDetail.TYPE_CONSUME +
								" AND MATE_DETAIL.date BETWEEN '" + dutyRange.getOnDuty() + "' AND '" + dutyRange.getOffDuty() + "'" +
								(deptID.length != 0 ? " AND MATE_DETAIL.dept_id IN(" + deptCond + ")" : ""),
								"");
		}else{
			orderFoods = SingleOrderFoodReflector.getDetailToday(dbCon, 
								" AND B.restaurant_id=" + term.restaurantID + " " + 
								" AND B.order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'" +  
								(deptID.length != 0 ? " AND A.dept_id IN(" + deptCond + ")" : ""),
								null);	
			
			materialDetails = MaterialDetailReflector.getMaterialDetail(dbCon, 
								" AND MATE_DETAIL.restaurant_id=" + term.restaurantID + " " +
								" AND MATE_DETAIL.type=" + MaterialDetail.TYPE_CONSUME +
								" AND MATE_DETAIL.date BETWEEN '" + onDuty + "' AND '" + offDuty + "'" +
								(deptID.length != 0 ? " AND MATE_DETAIL.dept_id IN(" + deptCond + ")" : ""),
								"");
		}
		
		String queryFoodExtraCond;
		queryFoodExtraCond = " AND FOOD.restaurant_id=" + term.restaurantID +
							 " AND FOOD.kitchen_alias IN " +
							 " (SELECT kitchen_alias FROM " + 
							 Params.dbName + ".kitchen" +
							 " WHERE 1=1 " +
							 " AND restaurant_id=" + term.restaurantID + ")" +
							 (deptID.length != 0 ? " AND DEPT.dept_id IN(" + deptCond + ")" : "");
		
		Food[] foodList = QueryMenu.queryPureFoods(dbCon, queryFoodExtraCond, null);
		HashMap<Food, SalesDetail> foodSalesDetail = new HashMap<Food, SalesDetail>();
		for(Food food : foodList){
			foodSalesDetail.put(food, new SalesDetail(food));
		}
		
		/**
		 * Calculate the gift, discount, income to each food during this period
		 */
		for(SingleOrderFood singleOrderFood : orderFoods){
			SalesDetail salesDetail = foodSalesDetail.get(singleOrderFood.food);
			
			if(salesDetail != null){
				if(singleOrderFood.food.isGift()){
					salesDetail.setGifted(salesDetail.getGifted() + singleOrderFood.calcPriceWithTaste());
				}else{
					salesDetail.setIncome(salesDetail.getIncome() + singleOrderFood.calcPriceWithTaste());
				}
					
				if(singleOrderFood.discount < 1){
					salesDetail.setDiscount(salesDetail.getDiscount() + singleOrderFood.calcDiscountPrice());
				}
				
				salesDetail.setSalesAmount(salesDetail.getSalesAmount() + singleOrderFood.orderCount);
				
				foodSalesDetail.put(singleOrderFood.food, salesDetail);			
			}
		}
		
		/**
		 * Calculate the cost to each food during this period
		 */
		for(MaterialDetail materialDetail : materialDetails){
			SalesDetail salesDetail = foodSalesDetail.get(materialDetail.food);
			if(salesDetail != null){
				salesDetail.setCost(salesDetail.getCost() + Math.abs(materialDetail.calcPrice()));
			}
		}
		
		/**
		 * Calculate the profit, cost rate, profit rate, average price, average cost to each food
		 */
		for(Food food : foodSalesDetail.keySet()){
			SalesDetail salesDetail = foodSalesDetail.get(food);
				
			salesDetail.setGifted((float)Math.round(salesDetail.getGifted() * 100) / 100);
			salesDetail.setDiscount((float)Math.round(salesDetail.getDiscount() * 100) / 100);
			salesDetail.setIncome((float)Math.round(salesDetail.getIncome() * 100) / 100);
			salesDetail.setCost((float)Math.round(salesDetail.getCost() * 100) / 100);
				
			salesDetail.setProfit(salesDetail.getIncome() - salesDetail.getCost());
			if(salesDetail.getIncome() != 0.00){
				salesDetail.setProfitRate(salesDetail.getProfit() / salesDetail.getIncome());
				salesDetail.setCostRate(salesDetail.getCost() / salesDetail.getIncome());
			}
			
			if(salesDetail.getSalesAmount() != 0.00){
				salesDetail.setAvgPrice((float)Math.round(salesDetail.getIncome() / salesDetail.getSalesAmount() * 100) / 100);
				salesDetail.setAvgCost((float)Math.round(salesDetail.getCost() / salesDetail.getSalesAmount() * 100) /100);
			}
			
			foodSalesDetail.put(food, salesDetail);
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
