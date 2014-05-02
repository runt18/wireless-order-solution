package com.wireless.test.db.billStatistics;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.CancelIncomeByDept;
import com.wireless.pojo.billStatistics.CancelIncomeByReason;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.IncomeByCancel;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.IncomeByFood;
import com.wireless.pojo.billStatistics.IncomeByKitchen;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;
import com.wireless.util.DateType;

public class TestCalcBillStatisticsDao {
	
	private static Staff mStaff;
	private static DutyRange mDutyRange;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, SQLException, BusinessException{
		TestInit.init();
		 mStaff = StaffDao.getAdminByRestaurant(37);
		 mDutyRange = new DutyRange("2014-1-10 23:40:04", "2014-1-26 23:49:36"); 
	}
	
	@Test 
	public void testCalcIncomeByKitchenAndDept() throws BusinessException, SQLException{
		
		List<IncomeByKitchen> kitchenIncomes = CalcBillStatisticsDao.calcIncomeByKitchen(mStaff, mDutyRange, null, DateType.HISTORY);
		
		Map<Department, IncomeByDept> deptIncomeByKitchen = new HashMap<Department, IncomeByDept>();
		for(IncomeByKitchen kitchenIncome : kitchenIncomes){
			IncomeByDept income = deptIncomeByKitchen.get(kitchenIncome.getKitchen().getDept());
			if(income != null){
				income.setGift(income.getGift() + kitchenIncome.getGift());
				income.setDiscount(income.getDiscount() + kitchenIncome.getDiscount());
				income.setIncome(income.getIncome() + kitchenIncome.getIncome());
			}else{
				income = new IncomeByDept(kitchenIncome.getKitchen().getDept(),
										  kitchenIncome.getGift(),
										  kitchenIncome.getDiscount(),
										  kitchenIncome.getIncome());
				deptIncomeByKitchen.put(kitchenIncome.getKitchen().getDept(), income);
			}
		}
		
		List<IncomeByDept> deptIncomes = CalcBillStatisticsDao.calcIncomeByDept(mStaff, mDutyRange, null, DateType.HISTORY);
		
		if(deptIncomeByKitchen.size() != deptIncomes.size()){
			//Check if the amount of department income is the same as before.
			Assert.assertTrue(false);
		}else{
			for(IncomeByDept deptIncome : deptIncomeByKitchen.values()){
				for(IncomeByDept deptIncomeToComp : deptIncomes){
					if(deptIncome.getDept().equals(deptIncomeToComp.getDept())){
						Assert.assertTrue("The discount to " + deptIncome.getDept() + " is different.", 
										  Float.valueOf(deptIncome.getDiscount()).intValue() == Float.valueOf(deptIncomeToComp.getDiscount()).intValue());
						Assert.assertTrue("The gift to " + deptIncome.getDept() + " is different.", 
										  Float.valueOf(deptIncome.getGift()).intValue() == Float.valueOf(deptIncomeToComp.getGift()).intValue());
						Assert.assertTrue("The income to " + deptIncome.getDept() + " is different.", 
										  Float.valueOf(deptIncome.getIncome()).intValue() == Float.valueOf(deptIncomeToComp.getIncome()).intValue());
					}
				}
			}
		}
		
	}
	
	@Test 
	public void testCalcIncomeByFood() throws BusinessException, SQLException{
		
		List<IncomeByFood> foodIncomes = CalcBillStatisticsDao.calcIncomeByFood(mStaff, mDutyRange, null, DateType.HISTORY);
		
		HashMap<Department, IncomeByDept> deptIncomeByFood = new HashMap<Department, IncomeByDept>();
		for(IncomeByFood foodIncome : foodIncomes){
			IncomeByDept income = deptIncomeByFood.get(foodIncome.getFood().getKitchen().getDept());
			if(income != null){
				income.setGift(income.getGift() + foodIncome.getGift());
				income.setDiscount(income.getDiscount() + foodIncome.getDiscount());
				income.setIncome(income.getIncome() + foodIncome.getIncome());
			}else{
				income = new IncomeByDept(foodIncome.getFood().getKitchen().getDept(),
										  foodIncome.getGift(),
										  foodIncome.getDiscount(),
										  foodIncome.getIncome());
				deptIncomeByFood.put(foodIncome.getFood().getKitchen().getDept(), income);
			}
		}
		
		List<IncomeByDept> deptIncomes = CalcBillStatisticsDao.calcIncomeByDept(mStaff, mDutyRange, null, DateType.HISTORY);
		
		if(deptIncomeByFood.size() != deptIncomes.size()){
			//Check if the amount of department income is the same as before.
			Assert.assertTrue(false);
		}else{
			for(IncomeByDept deptIncome : deptIncomeByFood.values()){
				for(IncomeByDept deptIncomeToComp : deptIncomes){
					if(deptIncome.getDept().equals(deptIncomeToComp.getDept())){
						Assert.assertTrue("The discount to " + deptIncome.getDept() + " is different.", 
										  Float.valueOf(deptIncome.getDiscount()).intValue() == Float.valueOf(deptIncomeToComp.getDiscount()).intValue());
						Assert.assertTrue("The gift to " + deptIncome.getDept() + " is different.", 
										  Float.valueOf(deptIncome.getGift()).intValue() == Float.valueOf(deptIncomeToComp.getGift()).intValue());
						Assert.assertTrue("The income to " + deptIncome.getDept() + " is different.", 
										  Float.valueOf(deptIncome.getIncome()).intValue() == Float.valueOf(deptIncomeToComp.getIncome()).intValue());
					}
				}
			}
		}
	}
	
	@Test
	public void testCalcCancelIncomeByReason() throws SQLException, BusinessException{
		
		List<CancelIncomeByReason> cancelByReason = CalcBillStatisticsDao.calcCancelIncomeByReason(mStaff, mDutyRange, null, DateType.HISTORY);
		
		IncomeByCancel cancelIncome = CalcBillStatisticsDao.calcCancelPrice(mStaff, mDutyRange, DateType.HISTORY);
		
		float totalCancel = 0;
		for(CancelIncomeByReason cancelByEachReason : cancelByReason){
			totalCancel += cancelByEachReason.getTotalCancelPrice();
		}
		
		Assert.assertEquals("", Float.valueOf(cancelIncome.getTotalCancel()).intValue(), Float.valueOf(totalCancel).intValue());
	}
	
	@Test
	public void testCalcCancelIncomeByDept() throws SQLException, BusinessException{
		Staff staff = StaffDao.getStaffs(37).get(0);
		
		DutyRange range = new DutyRange("2013-12-10 23:40:04", "2013-12-26 23:49:36"); 
		
		List<CancelIncomeByDept> cancelByDept = CalcBillStatisticsDao.calcCancelIncomeByDept(staff, range, null, DateType.HISTORY);
		
		IncomeByCancel cancelIncome = CalcBillStatisticsDao.calcCancelPrice(staff, range, DateType.HISTORY);
		
		float totalCancel = 0;
		for(CancelIncomeByDept cancelByEachDept : cancelByDept){
			totalCancel += cancelByEachDept.getTotalCancelPrice();
		}
		
		Assert.assertEquals(Float.valueOf(cancelIncome.getTotalCancel()).intValue(), Float.valueOf(totalCancel).intValue());
	}
}
