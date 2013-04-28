package com.wireless.test.db.frontBusiness;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.CancelOrder;
import com.wireless.db.frontBusiness.InsertOrder;
import com.wireless.db.frontBusiness.QueryMenu;
import com.wireless.db.frontBusiness.QueryTable;
import com.wireless.db.frontBusiness.UpdateOrder;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestCommitOrderDao {
	
	private static Terminal mTerminal;
	
	@BeforeClass
	public static void initDbParam(){
		TestInit.init();
		try {
			mTerminal = VerifyPin.exec(229, Terminal.MODEL_STAFF);
		} catch (BusinessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateOrder() throws BusinessException, SQLException{
		
		Table tblToInsert = QueryTable.exec(mTerminal)[0];
		Food[] foods = QueryMenu.queryPureFoods("AND FOOD.restaurant_id = " + mTerminal.restaurantID, null);
		
		//Cancel the order associated with table inserted if it exist before.
		try{
			CancelOrder.exec(mTerminal, tblToInsert.getAliasId());
		}catch(BusinessException e){
			
		}
		
		Order expectedOrder = new Order();
		expectedOrder.setDestTbl(tblToInsert);
		expectedOrder.setCustomNum(10);
		expectedOrder.setCategory(Order.CATE_NORMAL);
		expectedOrder.setOrderFoods(new OrderFood[]{
				new OrderFood(foods[0]),
				new OrderFood(foods[1])
		});
		for(int i = 0; i < expectedOrder.getOrderFoods().length; i++){
			expectedOrder.getOrderFoods()[i].setCount(1.35f + i);
		}
		
		//---------------------------------------------------------------
		//Insert a new order
		Order actualOrder = InsertOrder.exec(mTerminal, expectedOrder);
		
		actualOrder = QueryOrderDao.execByID(actualOrder.getId(), QueryOrderDao.QUERY_TODAY);
		
		compareOrder(expectedOrder, actualOrder);
		
		//---------------------------------------------------------------
		//Update
		expectedOrder.setOrderFoods(new OrderFood[]{
				new OrderFood(foods[1]),
				new OrderFood(foods[2])
		});
		for(int i = 0; i < expectedOrder.getOrderFoods().length; i++){
			expectedOrder.getOrderFoods()[i].setCount(1.35f + i);
		}

		UpdateOrder.execByID(mTerminal, expectedOrder);
		
		actualOrder = QueryOrderDao.execByID(actualOrder.getId(), QueryOrderDao.QUERY_TODAY);
		
		compareOrder(expectedOrder, actualOrder);
		
		//---------------------------------------------------------------
		//Update
		expectedOrder.setOrderFoods(new OrderFood[]{
				new OrderFood(foods[0]),
				new OrderFood(foods[1])
		});
		for(int i = 0; i < expectedOrder.getOrderFoods().length; i++){
			expectedOrder.getOrderFoods()[i].setCount(1.35f + i);
		}

		UpdateOrder.execByID(mTerminal, expectedOrder);
		
		actualOrder = QueryOrderDao.execByID(actualOrder.getId(), QueryOrderDao.QUERY_TODAY);
		
		compareOrder(expectedOrder, actualOrder);
		
		//------------------------------------------------------------------
		//Cancel the order associated with table inserted after test.
		try{
			CancelOrder.exec(mTerminal, actualOrder.getDestTbl().getAliasId());
		}catch(BusinessException e){
			
		}
	}
	
	private void compareOrder(Order expected, Order actual) throws BusinessException, SQLException{
		
		Comparator<Food> foodComp = new Comparator<Food>(){

			@Override
			public int compare(Food o1, Food o2) {
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
		Assert.assertEquals("the table to order", expected.getDestTbl(), actual.getDestTbl());
		//Check the custom number
		Assert.assertEquals("the custom number to order", expected.getCustomNum(), actual.getCustomNum());
		//Check the category
		Assert.assertEquals("the category to order", expected.getCategory(), actual.getCategory());
		//Check the order foods
		OrderFood[] expectedFoods = expected.getOrderFoods();
		OrderFood[] actualFoods = actual.getOrderFoods();
		Arrays.sort(expectedFoods, foodComp);
		Arrays.sort(actualFoods, foodComp);
		
		Assert.assertEquals(expectedFoods.length, actualFoods.length);
		for(int i = 0; i < expectedFoods.length; i++){
			Assert.assertEquals("basic info to food[" + i + "]", expectedFoods[i], actualFoods[i]);
			Assert.assertEquals("order count to food[" + i + "]", expectedFoods[i].getCount(), actualFoods[i].getCount());
		}
		
		//Check the associated table detail
		Table tbl = QueryTable.exec(mTerminal, actual.getDestTbl().getAliasId());
		//Check the status to associated table
		Assert.assertEquals("the status to associated table", tbl.getStatus().getVal(), Table.Status.BUSY.getVal());
		//Check the custom number to associated table
		Assert.assertEquals("the custom number to associated table", tbl.getCustomNum(), actual.getCustomNum());
		//Check the category to associated table
		Assert.assertEquals("the category to associated table", tbl.getCategory(), actual.getCategory());
	}
}
