package com.wireless.test.db.frontBusiness;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.CancelOrder;
import com.wireless.db.frontBusiness.InsertOrder;
import com.wireless.db.frontBusiness.UpdateOrder;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;
import com.wireless.util.DateType;

public class TestCommitOrderDao {
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getStaffs(37).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateOrder() throws BusinessException, BusinessException, SQLException{
		
		Table tblToInsert = TableDao.getTables(mStaff, null, null).get(0);
		List<Food> foods = FoodDao.getPureFoods(mStaff, null, null);
		
		//Cancel the order associated with table inserted if it exist before.
		try{
			CancelOrder.exec(mStaff, tblToInsert.getAliasId());
		}catch(BusinessException e){
			
		}
		
		Order expectedOrder = new Order();
		expectedOrder.setDestTbl(tblToInsert);
		expectedOrder.setCustomNum(10);
		expectedOrder.setCategory(Order.Category.NORMAL);
		
		OrderFood of;
		of = new OrderFood(foods.get(0));
		of.setCount(1.35f);
		expectedOrder.addFood(of);
		
		of = new OrderFood(foods.get(1));
		of.setCount(2.35f);
		expectedOrder.addFood(of);
		
		//---------------------------------------------------------------
		//Insert a new order
		Order actualOrder = InsertOrder.exec(mStaff, expectedOrder);
		
		actualOrder = OrderDao.getById(mStaff, actualOrder.getId(), DateType.TODAY);
		
		compareOrder(expectedOrder, actualOrder);
		
		//---------------------------------------------------------------
		//Update
		expectedOrder.removeAll(mStaff);
		
		of = new OrderFood(foods.get(1));
		of.setCount(1.35f);
		expectedOrder.addFood(of);
		
		of = new OrderFood(foods.get(2));
		of.setCount(2.35f);
		expectedOrder.addFood(of);
		
		UpdateOrder.execByID(mStaff, expectedOrder);
		
		actualOrder = OrderDao.getById(mStaff, actualOrder.getId(), DateType.TODAY);
		
		compareOrder(expectedOrder, actualOrder);
		
		//---------------------------------------------------------------
		//Update
		expectedOrder.removeAll(mStaff);
		
		of = new OrderFood(foods.get(0));
		of.setCount(1.35f);
		expectedOrder.addFood(of);
		
		of = new OrderFood(foods.get(1));
		of.setCount(2.35f);
		expectedOrder.addFood(of);
		
		UpdateOrder.execByID(mStaff, expectedOrder);
		
		actualOrder = OrderDao.getById(mStaff, actualOrder.getId(), DateType.TODAY);
		
		compareOrder(expectedOrder, actualOrder);
		
		//------------------------------------------------------------------
		//Cancel the order associated with table inserted after test.
		try{
			CancelOrder.exec(mStaff, actualOrder.getDestTbl().getAliasId());
		}catch(BusinessException e){
			
		}
	}
	
	private void compareOrder(Order expected, Order actual) throws BusinessException, SQLException{
		
		Comparator<OrderFood> foodComp = new Comparator<OrderFood>(){

			@Override
			public int compare(OrderFood o1, OrderFood o2) {
				if(o1.getAliasId() > o2.getAliasId()){
					return 1;
				}else if(o1.getAliasId() < o2.getAliasId()){
					return -1;
				}else{
					return 0;
				}
			}
		};
		
		//Check the associated table
		assertEquals("the table to order", expected.getDestTbl(), actual.getDestTbl());
		//Check the custom number
		assertEquals("the custom number to order", expected.getCustomNum(), actual.getCustomNum());
		//Check the category
		assertEquals("the category to order", expected.getCategory(), actual.getCategory());
		//Check the order foods
		List<OrderFood> expectedFoods = expected.getOrderFoods();
		List<OrderFood> actualFoods = actual.getOrderFoods();
		Collections.sort(expectedFoods, foodComp);
		Collections.sort(actualFoods, foodComp);
		
		assertEquals(expectedFoods.size(), actualFoods.size());
		for(int i = 0; i < expectedFoods.size(); i++){
			assertEquals("basic info to food[" + i + "]", expectedFoods.get(i), actualFoods.get(i));
			assertEquals("order count to food[" + i + "]", expectedFoods.get(i).getCount(), actualFoods.get(i).getCount(), 0.01);
		}
		
		//Check the associated table detail
		Table tbl = TableDao.getTableByAlias(mStaff, actual.getDestTbl().getAliasId());
		//Check the status to associated table
		assertEquals("the status to associated table", tbl.getStatus().getVal(), Table.Status.BUSY.getVal());
		//Check the custom number to associated table
		assertEquals("the custom number to associated table", tbl.getCustomNum(), actual.getCustomNum());
		//Check the category to associated table
		assertEquals("the category to associated table", tbl.getCategory().getVal(), actual.getCategory().getVal());
	}
}
