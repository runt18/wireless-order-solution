package com.wireless.test.db.orderMgr;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.CancelOrder;
import com.wireless.db.QueryMenu;
import com.wireless.db.VerifyPin;
import com.wireless.db.orderMgr.OrderGroupDao;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestOrderGroupDao {
	@BeforeClass
	public static void initDbParam(){
		TestInit.init();
	}
	
	@Test 
	public void testInsertByTbl() throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(229, Terminal.MODEL_STAFF);
		
		Table[] tblToInsert = new Table[]{
			new Table(0, 1, 37),
			new Table(0, 2, 37)
		};
		
		//Cancel the record before performing insertion.
		try{
			OrderGroupDao.cancel(term, tblToInsert[0]);
		}catch(BusinessException e){
			
		}	
		
		for(Table tbl : tblToInsert){
			try{
				CancelOrder.exec(term, tbl.getAliasId());
			}catch(BusinessException e){
				
			}
		}
			
		
		Order orderAfterInsert = QueryOrderDao.execByID(OrderGroupDao.insert(term, tblToInsert), QueryOrderDao.QUERY_TODAY);
		//Check if parent order is merged.
		Assert.assertTrue(orderAfterInsert.isMerged());
		
		if(orderAfterInsert.hasChildOrder()){
			Assert.assertEquals(orderAfterInsert.getChildOrder().length, tblToInsert.length);
			for(Order childOrder : orderAfterInsert.getChildOrder()){
				Order orderToChild = QueryOrderDao.execByID(childOrder.getId(), QueryOrderDao.QUERY_TODAY);
				//Check if each child order is merged.
				Assert.assertTrue(orderToChild.isMergedChild());
				//Check if the table associated with each child order is merged.
				Assert.assertTrue(orderToChild.getDestTbl().isMerged());
			}
		}else{
			Assert.assertTrue("The order does NOT contain any child order.", false);
		}
		
		//Cancel the record after performing insertion.
		OrderGroupDao.cancel(term, tblToInsert[0]);
	}
	
	@Test
	public void testUpdateByTbl() throws BusinessException, SQLException{		

		Terminal term = VerifyPin.exec(229, Terminal.MODEL_STAFF);
	
		Table[] tblToInsert = new Table[]{
			new Table(0, 1, 37),
			new Table(0, 2, 37)
		};
		//Cancel the record before performing insertion.
		try{
			OrderGroupDao.cancel(term, tblToInsert[0]);
		}catch(BusinessException e){
			
		}
		
		for(Table tbl : tblToInsert){
			try{
				CancelOrder.exec(term, tbl.getAliasId());
			}catch(BusinessException e){
				
			}
		}
		
		int parentOrderId = OrderGroupDao.insert(term, tblToInsert);
		
		Table[] tblToUpdate = new Table[]{
			new Table(0, 1, 37),
			new Table(0, 2, 37)
		};
		OrderGroupDao.update(term, parentOrderId, tblToUpdate);		
		Order parentOrder = QueryOrderDao.execByID(parentOrderId, QueryOrderDao.QUERY_TODAY);
		checkByTbl(parentOrder, tblToUpdate);

		tblToUpdate = new Table[]{
			new Table(0, 1, 37),
		};
		OrderGroupDao.update(term, parentOrderId, tblToUpdate);		
		parentOrder = QueryOrderDao.execByID(parentOrderId, QueryOrderDao.QUERY_TODAY);
		checkByTbl(parentOrder, tblToUpdate);
		
		tblToUpdate = new Table[]{
			new Table(0, 2, 37),
			new Table(0, 1, 37)
		};
		OrderGroupDao.update(term, parentOrderId, tblToUpdate);		
		parentOrder = QueryOrderDao.execByID(parentOrderId, QueryOrderDao.QUERY_TODAY);
		checkByTbl(parentOrder, tblToUpdate);
		
		tblToUpdate = new Table[]{
			new Table(0, 2, 37),
			new Table(0, 3, 37)
		};
		OrderGroupDao.update(term, parentOrderId, tblToUpdate);		
		parentOrder = QueryOrderDao.execByID(parentOrderId, QueryOrderDao.QUERY_TODAY);
		checkByTbl(parentOrder, tblToUpdate);
		
		OrderGroupDao.cancel(term, tblToUpdate[0]);
	}
	
	private void checkByTbl(Order orderToCheck, Table[] expectedTbls) throws BusinessException, SQLException{
		//Check if parent order is merged.
		Assert.assertTrue(orderToCheck.isMerged());
		
		if(orderToCheck.hasChildOrder()){
			Assert.assertEquals(orderToCheck.getChildOrder().length, expectedTbls.length);
			for(Order childOrder : orderToCheck.getChildOrder()){
				Order orderToChild = QueryOrderDao.execByID(childOrder.getId(), QueryOrderDao.QUERY_TODAY);
				//Check if the table to each child order is contained in expected tables.
				boolean isContained = false;
				for(Table tbl : expectedTbls){
					if(orderToChild.getDestTbl().equals(tbl)){
						isContained = true;
						break;
					}
				}
				Assert.assertTrue(isContained);
				
				//Check if each child order is merged.
				Assert.assertEquals("the category to child order(id=" + childOrder.getId() + ")", orderToChild.isMergedChild(), true);
				//Check if the table associated with each child order is merged.
				Assert.assertEquals("the table to child order(id=" + childOrder.getId() + ")", orderToChild.getDestTbl().isMerged(), true);
			}
		}else{
			Assert.assertTrue("The order does NOT contain any child order.", false);
		}
	}
	
	@Test
	public void testInsertByOrder() throws BusinessException, SQLException{
		Terminal term = VerifyPin.exec(229, Terminal.MODEL_STAFF);
		
		Table[] tblToInsert = new Table[]{
			new Table(0, 1, 37),
			new Table(0, 2, 37)
		};
		
		//Cancel the record before performing insertion.
		try{
			OrderGroupDao.cancel(term, tblToInsert[0]);
		}catch(BusinessException e){
			
		}
		
		for(Table tbl : tblToInsert){
			try{
				CancelOrder.exec(term, tbl.getAliasId());
			}catch(BusinessException e){
				
			}
		}
		
		Food[] foods = QueryMenu.queryPureFoods("AND FOOD.restaurant_id = " + term.restaurantID, null);
		
		Order orderGroupToInsert = new Order();
		
		Order[] childOrders = new Order[tblToInsert.length];
		for(int i = 0; i < childOrders.length; i++){
			childOrders[i] = new Order();
			childOrders[i].setDestTbl(tblToInsert[i]);
			childOrders[i].setOrderFoods(new OrderFood[]{
					new OrderFood(foods[i]),
					new OrderFood(foods[i + 1])
			});
			OrderFood[] foodsToChildOrder = childOrders[i].getOrderFoods();
			for(int j = 0; j < foodsToChildOrder.length; j++){
				foodsToChildOrder[j].setCount(1.53f);
			}
		}
		
		orderGroupToInsert.setChildOrder(childOrders);
		
		int parentOrderId = OrderGroupDao.insert(term, orderGroupToInsert);
		
		Order orderGroupAfterInsert = QueryOrderDao.execByID(parentOrderId, QueryOrderDao.QUERY_TODAY);
		
		// Check to see the category to order group
		Assert.assertEquals("category to order group", orderGroupAfterInsert.getCategory(), Order.CATE_MERGER_TABLE);
		
		// Check each child order
		Order[] expectedChildOrders = orderGroupAfterInsert.getChildOrder();
		Order[] actualChildOrders = orderGroupAfterInsert.getChildOrder();
		Assert.assertEquals(expectedChildOrders.length, actualChildOrders.length);
		
		// Check the order foods to each order
		for(int i = 0; i < expectedChildOrders.length; i++){
			OrderFood[] expectedFoods = expectedChildOrders[i].getOrderFoods();
			OrderFood[] actualFoods = actualChildOrders[i].getOrderFoods();
			Assert.assertEquals(expectedFoods.length, actualFoods.length);
			for(int j = 0; j < expectedFoods.length; j++){
				Assert.assertEquals("basic info to food[" + j + "]" + " in child order[" + i + "]", expectedFoods[j], actualFoods[j]);
				Assert.assertEquals("order count to food[" + j + "]" + " in child order[" + i + "]", expectedFoods[j].getCount(), actualFoods[j].getCount());
			}
		}
		
		// Cancel the order group
		OrderGroupDao.cancel(term, orderGroupAfterInsert);
		for(Order childOrder : actualChildOrders){
			CancelOrder.exec(term, childOrder.getDestTbl().getAliasId());
		}
	}
}
