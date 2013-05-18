package com.wireless.test.db.frontBusiness;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.CancelOrder;
import com.wireless.db.frontBusiness.InsertOrder;
import com.wireless.db.frontBusiness.UpdateOrder;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.excep.ProtocolException;
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
	public void testUpdateOrder() throws ProtocolException, BusinessException, SQLException{
		
		Table tblToInsert = TableDao.getTables(mTerminal, null, null).get(0);
		List<Food> foods = FoodDao.getPureFoods(mTerminal, null, null);
		
		//Cancel the order associated with table inserted if it exist before.
		try{
			CancelOrder.exec(mTerminal, tblToInsert.getAliasId());
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
		Order actualOrder = InsertOrder.exec(mTerminal, expectedOrder);
		
		actualOrder = QueryOrderDao.execByID(actualOrder.getId(), QueryOrderDao.QUERY_TODAY);
		
		compareOrder(expectedOrder, actualOrder);
		
		//---------------------------------------------------------------
		//Update
		expectedOrder.removeAll();
		
		of = new OrderFood(foods.get(1));
		of.setCount(1.35f);
		expectedOrder.addFood(of);
		
		of = new OrderFood(foods.get(2));
		of.setCount(2.35f);
		expectedOrder.addFood(of);
		
		UpdateOrder.execByID(mTerminal, expectedOrder);
		
		actualOrder = QueryOrderDao.execByID(actualOrder.getId(), QueryOrderDao.QUERY_TODAY);
		
		compareOrder(expectedOrder, actualOrder);
		
		//---------------------------------------------------------------
		//Update
		expectedOrder.removeAll();
		
		of = new OrderFood(foods.get(0));
		of.setCount(1.35f);
		expectedOrder.addFood(of);
		
		of = new OrderFood(foods.get(1));
		of.setCount(2.35f);
		expectedOrder.addFood(of);
		
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
		Assert.assertEquals("the table to order", expected.getDestTbl(), actual.getDestTbl());
		//Check the custom number
		Assert.assertEquals("the custom number to order", expected.getCustomNum(), actual.getCustomNum());
		//Check the category
		Assert.assertEquals("the category to order", expected.getCategory(), actual.getCategory());
		//Check the order foods
		List<OrderFood> expectedFoods = expected.getOrderFoods();
		List<OrderFood> actualFoods = actual.getOrderFoods();
		Collections.sort(expectedFoods, foodComp);
		Collections.sort(actualFoods, foodComp);
		
		Assert.assertEquals(expectedFoods.size(), actualFoods.size());
		for(int i = 0; i < expectedFoods.size(); i++){
			Assert.assertEquals("basic info to food[" + i + "]", expectedFoods.get(i), actualFoods.get(i));
			Assert.assertEquals("order count to food[" + i + "]", expectedFoods.get(i).getCount(), actualFoods.get(i).getCount());
		}
		
		//Check the associated table detail
		Table tbl = TableDao.getTableByAlias(mTerminal, actual.getDestTbl().getAliasId());
		//Check the status to associated table
		Assert.assertEquals("the status to associated table", tbl.getStatus().getVal(), Table.Status.BUSY.getVal());
		//Check the custom number to associated table
		Assert.assertEquals("the custom number to associated table", tbl.getCustomNum(), actual.getCustomNum());
		//Check the category to associated table
		Assert.assertEquals("the category to associated table", tbl.getCategory().getVal(), actual.getCategory().getVal());
	}
}
