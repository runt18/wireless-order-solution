package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.billStatistics.CalcBillStatisticsDao.ExtraCond;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.IncomeByFood;
import com.wireless.pojo.billStatistics.IncomeByKitchen;
import com.wireless.pojo.billStatistics.SalesDetail;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;

public class SaleDetailsDao {
	
	public final static int QUERY_BY_DEPT = 0;		//按部门显示
	public final static int QUERY_BY_FOOD = 1;		//按菜品显示
	public final static int QUERY_BY_KITCHEN = 2;	//按分厨显示
	
	public final static int ORDER_BY_PROFIT = 0;	//按毛利排序
	public final static int ORDER_BY_SALES = 1;		//按销量排序
	
	/**
	 * Get the sales details to each department.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link CalcBillStatisticsDao.ExtraCond}
	 * @return the result list {@link SalesDetail}
	 * @throws SQLException
	 * 			throws if any error occurred while execute any SQL statements
	 * @throws BusinessException 
	 */
	public static List<SalesDetail> getByDept(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByDept(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}	
	
	/**
	 * Get the sales details to each department.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link CalcBillStatisticsDao.ExtraCond}
	 * @return the result list {@link SalesDetail}
	 * @throws SQLException
	 * 			throws if any error occurred while execute any SQL statements
	 * @throws BusinessException 
	 */
	public static List<SalesDetail> getByDept(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		
		final List<SalesDetail> result = new ArrayList<SalesDetail>();
		if(extraCond.isChain()){
			//Group by the department name in case of chain.
			//key : value - deptName : salesDetail
			final Map<String, SalesDetail> chainResult = new HashMap<>();
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			for(SalesDetail groupDetail : getByDept(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false))){
				chainResult.put(groupDetail.getDept().getName(), groupDetail);
			}
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				for(SalesDetail branchDetail : getByDept(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false))){
					if(chainResult.containsKey(branchDetail.getDept().getName())){
						SalesDetail salesDetail = chainResult.get(branchDetail.getDept().getName());
						salesDetail.setGifted(branchDetail.getGifted() + salesDetail.getGifted());
						salesDetail.setDiscount(branchDetail.getDiscount() + salesDetail.getDiscount());
						salesDetail.setIncome(branchDetail.getIncome() + salesDetail.getIncome());
						salesDetail.setProfit(branchDetail.getProfit() + salesDetail.getProfit());
						if(salesDetail.getIncome() != 0.00){
							salesDetail.setProfitRate(salesDetail.getProfit() / salesDetail.getIncome());
							salesDetail.setCostRate(salesDetail.getCost() / salesDetail.getIncome());						}
					}else{
						chainResult.put(branchDetail.getDept().getName(), branchDetail);
					}
				}
			}
			
			result.addAll(chainResult.values());
			
		}else{
			final List<IncomeByDept> deptIncomes;

//			if(extraCond.dateType.isHistory()){
//				
//				//Get the duty range between on and off duty date
//				DutyRange dutyRange = DutyRangeDao.exec(dbCon, staff, range.getOnDutyFormat(), range.getOffDutyFormat());
//				
//				//Calculate the incomes to each department.
//				if(dutyRange != null){
//					deptIncomes = CalcBillStatisticsDao.calcIncomeByDept(dbCon, staff, dutyRange, extraCond);
//				}else{
//					deptIncomes = CalcBillStatisticsDao.calcIncomeByDept(dbCon, staff, range, extraCond);
//				}
//			}else{
//				//Calculate the incomes to each department.
//				deptIncomes = CalcBillStatisticsDao.calcIncomeByDept(dbCon, staff, range, extraCond);
//			}
			
			//Calculate the incomes to each department.
			deptIncomes = CalcBillStatisticsDao.calcIncomeByDept(dbCon, staff, extraCond);
			
			for(IncomeByDept deptIncome : deptIncomes){
				SalesDetail salesDetail = new SalesDetail(deptIncome.getDept());
				salesDetail.setGifted(deptIncome.getGift());
				salesDetail.setDiscount(deptIncome.getDiscount());
				salesDetail.setIncome(deptIncome.getIncome());
				salesDetail.setProfit(salesDetail.getIncome() - salesDetail.getCost());
				if(salesDetail.getIncome() != 0.00){
					salesDetail.setProfitRate(salesDetail.getProfit() / salesDetail.getIncome());
					salesDetail.setCostRate(salesDetail.getCost() / salesDetail.getIncome());
				}
				result.add(salesDetail);
			}
			
		}

		/**
		 * Sort the department sales detail in descending order by profit
		 */
		Collections.sort(result, new Comparator<SalesDetail>(){

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
	 * Get the sales detail to each kitchen according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link SalesDetail}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<SalesDetail> getByKitchen(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByKitchen(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Get the sales detail to each kitchen according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link SalesDetail}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<SalesDetail> getByKitchen(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		
		final List<SalesDetail> result = new ArrayList<SalesDetail>();
		if(extraCond.isChain()){
			//Group by the kitchen name in case of chain.
			//key : value - deptName + kitchenName : salesDetail
			final Map<String, SalesDetail> chainResult = new HashMap<>();
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			for(SalesDetail groupDetail : getByKitchen(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false))){
				chainResult.put(groupDetail.getKitchen().getDept().getName() + groupDetail.getKitchen().getName(), groupDetail);
			}
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				for(SalesDetail branchDetail : getByKitchen(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false))){
					if(chainResult.containsKey(branchDetail.getKitchen().getDept().getName() + branchDetail.getKitchen().getName())){
						SalesDetail salesDetail = chainResult.get(branchDetail.getKitchen().getDept().getName() + branchDetail.getKitchen().getName());
						salesDetail.setGifted(branchDetail.getGifted() + salesDetail.getGifted());
						salesDetail.setDiscount(branchDetail.getDiscount() + salesDetail.getDiscount());
						salesDetail.setIncome(branchDetail.getIncome() + salesDetail.getIncome());
						salesDetail.setProfit(branchDetail.getProfit() + salesDetail.getProfit());
						if(salesDetail.getIncome() != 0.00){
							salesDetail.setProfitRate(salesDetail.getProfit() / salesDetail.getIncome());
							salesDetail.setCostRate(salesDetail.getCost() / salesDetail.getIncome());						
						}
					}else{
						chainResult.put(branchDetail.getKitchen().getDept().getName() + branchDetail.getKitchen().getName(), branchDetail);
					}
				}
			}
			
			result.addAll(chainResult.values());
			
			//Restore the chain status to extra condition.
			extraCond.setChain(true);
		}else{
			List<IncomeByKitchen> kitchenIncomes;

//			if(extraCond.dateType.isHistory()){
//				
//				//Get the duty range between on and off duty date
//				DutyRange dutyRange = DutyRangeDao.exec(dbCon, staff, range.getOnDutyFormat(), range.getOffDutyFormat());
//				
//				//Calculate the incomes to each kitchen.
//				if(dutyRange != null){
//					kitchenIncomes = CalcBillStatisticsDao.calcIncomeByKitchen(dbCon, staff, dutyRange, extraCond);
//				}else{
//					kitchenIncomes = CalcBillStatisticsDao.calcIncomeByKitchen(dbCon, staff, range, extraCond);
//				}
//				
//			}else{
//				//Calculate the incomes to each kitchen.
//				kitchenIncomes = CalcBillStatisticsDao.calcIncomeByKitchen(dbCon, staff, range, extraCond);
//			}
			
			kitchenIncomes = CalcBillStatisticsDao.calcIncomeByKitchen(dbCon, staff, extraCond);
			
			for(IncomeByKitchen kitchenIncome : kitchenIncomes){
				SalesDetail salesDetail = new SalesDetail(kitchenIncome.getKitchen());
				salesDetail.setGifted(kitchenIncome.getGift());
				salesDetail.setDiscount(kitchenIncome.getDiscount());
				salesDetail.setIncome(kitchenIncome.getIncome());
				salesDetail.setProfit(salesDetail.getIncome() - salesDetail.getCost());
				if(salesDetail.getIncome() != 0.00){
					salesDetail.setProfitRate(salesDetail.getProfit() / salesDetail.getIncome());
					salesDetail.setCostRate(salesDetail.getCost() / salesDetail.getIncome());
				}
				result.add(salesDetail);
			}

		}
		
		return result;

	}
	

	/**
	 * Get the sales detail to each food according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link CalcBillStatisticsDao.ExtraCond}
	 * @param orderType
	 * @return the result list {@link SalesDetail}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<SalesDetail> getByFood(Staff staff, ExtraCond extraCond, int orderType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByFood(dbCon, staff, extraCond, orderType);
		}finally{
			dbCon.disconnect();
		}
	}	
	
	/**
	 * Get the sales detail to each food according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link CalcBillStatisticsDao.ExtraCond}
	 * @param orderType
	 * @return the result list {@link SalesDetail}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<SalesDetail> getByFood(DBCon dbCon, Staff staff, ExtraCond extraCond, int orderType) throws SQLException, BusinessException{
		
		final List<SalesDetail> result = new ArrayList<SalesDetail>();

		if(extraCond.isChain()){
			//append the food sales detail in case of chain.
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			result.addAll(getByFood(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false), orderType));
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				result.addAll(getByFood(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false), orderType));
			}
			
		}else{
			final List<IncomeByFood> foodIncomes;
			
//			if(extraCond.dateType.isHistory()){
//				
//				//Get the duty range between on and off duty date
//				DutyRange dutyRange = DutyRangeDao.exec(dbCon, staff, range.getOnDutyFormat(), range.getOffDutyFormat());
//				
//				if(dutyRange != null){
//					foodIncomes = CalcBillStatisticsDao.calcIncomeByFood(dbCon,	staff, dutyRange, extraCond);
//				}else{
//					foodIncomes = CalcBillStatisticsDao.calcIncomeByFood(dbCon,	staff, range, extraCond);
//				}
//			}else{
//				foodIncomes = CalcBillStatisticsDao.calcIncomeByFood(dbCon, staff, range, extraCond);
//			}
			
			foodIncomes = CalcBillStatisticsDao.calcIncomeByFood(dbCon, staff, extraCond);
			for(IncomeByFood foodIncome : foodIncomes){
				SalesDetail detail = new SalesDetail(foodIncome.getFood());
				detail.setResturant(foodIncome.getRestaurant());
				detail.setDiscount(foodIncome.getDiscount());
				detail.setGifted(foodIncome.getGift());
				detail.setGiftAmount(foodIncome.getGiftAmount());
				detail.setIncome(foodIncome.getIncome());
				detail.setTasteIncome(foodIncome.getTasteIncome());
				detail.setSalesAmount(foodIncome.getSaleAmount());

				//成本
				detail.setCost(foodIncome.getCost());
				//单位成本
				detail.setAvgCost(foodIncome.getUnitCost());

				detail.setProfit(detail.getIncome() - detail.getCost());
				if(detail.getIncome() != 0.00){
					detail.setProfitRate(detail.getProfit() / detail.getIncome());
					detail.setCostRate(detail.getCost() / detail.getIncome());
				}
				
//				if(detail.getSalesAmount() != 0.00){
//					detail.setAvgPrice((float)Math.round(detail.getIncome() / detail.getSalesAmount() * 100) / 100);
//				}
				
				result.add(detail);
			}
			
		}
		
		if(orderType == SaleDetailsDao.ORDER_BY_PROFIT){
			Collections.sort(result, new Comparator<SalesDetail>(){
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
		}else if(orderType == SaleDetailsDao.ORDER_BY_SALES){
			Collections.sort(result, new Comparator<SalesDetail>(){
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
