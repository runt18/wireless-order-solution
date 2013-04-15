package com.wireless.test.db.orderMgr;

import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.CancelOrder;
import com.wireless.db.frontBusiness.QueryMenu;
import com.wireless.db.frontBusiness.QueryTable;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.orderMgr.OrderGroupDao;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.PTable;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.comp.FoodComp;
import com.wireless.test.db.TestInit;

public class TestOrderGroupDao {
	@BeforeClass
	public static void initDbParam(){
		TestInit.init();
	}
	
	@Test 
	public void testInsertByTbl() throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(229, Terminal.MODEL_STAFF);
		
		PTable[] tbls = QueryTable.exec(term);
		
		PTable[] tblToInsert = new PTable[]{
			tbls[0],
			tbls[1]
		};
		
		//Cancel the record before performing insertion.
		try{
			OrderGroupDao.cancel(term, tblToInsert[0]);
		}catch(BusinessException e){
			
		}	
		
		for(PTable tbl : tblToInsert){
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
	
		PTable[] tbls = QueryTable.exec(term);
		
		PTable[] tblToInsert = new PTable[]{
			tbls[0],
			tbls[1]
		};
		
		//Cancel the record before performing insertion.
		try{
			OrderGroupDao.cancel(term, tblToInsert[0]);
		}catch(BusinessException e){
			
		}
		
		for(PTable tbl : tblToInsert){
			try{
				CancelOrder.exec(term, tbl.getAliasId());
			}catch(BusinessException e){
				
			}
		}
		
		int parentOrderId = OrderGroupDao.insert(term, tblToInsert);
		
		PTable[] tblToUpdate = new PTable[]{
			tbls[0],
			tbls[1]
		};
		OrderGroupDao.update(term, parentOrderId, tblToUpdate);		
		Order parentOrder = QueryOrderDao.execByID(parentOrderId, QueryOrderDao.QUERY_TODAY);
		checkByTbl(parentOrder, tblToUpdate);

		tblToUpdate = new PTable[]{
			tbls[0],
		};
		OrderGroupDao.update(term, parentOrderId, tblToUpdate);		
		parentOrder = QueryOrderDao.execByID(parentOrderId, QueryOrderDao.QUERY_TODAY);
		checkByTbl(parentOrder, tblToUpdate);
		
		tblToUpdate = new PTable[]{
			tbls[1],
			tbls[0]
		};
		OrderGroupDao.update(term, parentOrderId, tblToUpdate);		
		parentOrder = QueryOrderDao.execByID(parentOrderId, QueryOrderDao.QUERY_TODAY);
		checkByTbl(parentOrder, tblToUpdate);
		
		tblToUpdate = new PTable[]{
			tbls[1],
			tbls[2]
		};
		OrderGroupDao.update(term, parentOrderId, tblToUpdate);		
		parentOrder = QueryOrderDao.execByID(parentOrderId, QueryOrderDao.QUERY_TODAY);
		checkByTbl(parentOrder, tblToUpdate);
		
		OrderGroupDao.cancel(term, tblToUpdate[0]);
	}
	
	private void checkByTbl(Order orderToCheck, PTable[] expectedTbls) throws BusinessException, SQLException{
		//Check if parent order is merged.
		Assert.assertTrue(orderToCheck.isMerged());
		
		if(orderToCheck.hasChildOrder()){
			Assert.assertEquals(orderToCheck.getChildOrder().length, expectedTbls.length);
			for(Order childOrder : orderToCheck.getChildOrder()){
				Order orderToChild = QueryOrderDao.execByID(childOrder.getId(), QueryOrderDao.QUERY_TODAY);
				//Check if the table to each child order is contained in expected tables.
				boolean isContained = false;
				for(PTable tbl : expectedTbls){
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
	public void testUpdateByOrder() throws BusinessException, SQLException{
		Terminal term = VerifyPin.exec(229, Terminal.MODEL_STAFF);
		
		PTable[] tbls = QueryTable.exec(term);

		Food[] foods = QueryMenu.queryPureFoods("AND FOOD.restaurant_id = " + term.restaurantID, null);

		Params4Order[] params = new Params4Order[]{
			new Params4Order(tbls[0], 
					new OrderFood[]{
						buildOrderFood(foods[0], 1.53f),
						buildOrderFood(foods[1], 1.53f)
					}),
					
			new Params4Order(tbls[1], 
					new OrderFood[]{
						buildOrderFood(foods[0], 1.53f),
						buildOrderFood(foods[1], 1.53f)
					})	
		};
		
		//Cancel the record before performing insertion.
		try{
			OrderGroupDao.cancel(term, params[0].tbl);
		}catch(BusinessException e){
			
		}
		
		for(Params4Order param : params){
			try{
				CancelOrder.exec(term, param.tbl.getAliasId());
			}catch(BusinessException e){
				
			}
		}
		
		
		Order expectOrderGroup = new Order();
		
		Order[] childOrdersToInsert = new Order[params.length];
		for(int i = 0; i < params.length; i++){
			childOrdersToInsert[i] = new Order();
			childOrdersToInsert[i].setDestTbl(params[i].tbl);
			childOrdersToInsert[i].setOrderFoods(params[i].orderFoods);
		}
		
		expectOrderGroup.setChildOrder(childOrdersToInsert);
		
		//---------------------------------------------------------------
		//Insert a new order group
		int actualOrderId = OrderGroupDao.insert(term, expectOrderGroup);

		Order actualOrderGroup = QueryOrderDao.execByID(actualOrderId, QueryOrderDao.QUERY_TODAY);
		for(int i = 0; i < actualOrderGroup.getChildOrder().length; i++){
			actualOrderGroup.getChildOrder()[i] = QueryOrderDao.execByID(actualOrderGroup.getChildOrder()[i].getId(), QueryOrderDao.QUERY_TODAY);
			actualOrderGroup.getChildOrder()[i].setDestTbl(QueryTable.exec(term, actualOrderGroup.getChildOrder()[i].getDestTbl().getAliasId()));
			expectOrderGroup.getChildOrder()[i].setId(actualOrderGroup.getChildOrder()[i].getId());
		}
		compareOrderGroup(expectOrderGroup, actualOrderGroup);
		
		//---------------------------------------------------------------
		//Update the order group, 
		
		/*
		 * The rule to test case is below.
		 * 1 - Remove the order associated with table[0].
		 * 2 - Add an order associated with table[2].
		 * 3 - Remove a food from order associated with table[1]
		 * 4 - Add a food to order associated with table[1].
		 */
		params = new Params4Order[]{
				new Params4Order(expectOrderGroup.getChildOrder()[1].getDestTbl(), 
						new OrderFood[]{
							buildOrderFood(foods[1], 0.53f),
							buildOrderFood(foods[2], 2.53f)
						}, 
						expectOrderGroup.getChildOrder()[1].getId()),
						
				new Params4Order(tbls[2], 
						new OrderFood[]{
							buildOrderFood(foods[0], 1.53f),
							buildOrderFood(foods[1], 1.53f)
						})	
			};

		Order expectedLeavedOrder = expectOrderGroup.getChildOrder()[0];
		
		Order[] childOrdersToUpdate = new Order[params.length];
		for(int i = 0; i < params.length; i++){
			childOrdersToUpdate[i] = new Order();
			childOrdersToUpdate[i].setDestTbl(params[i].tbl);
			childOrdersToUpdate[i].setOrderFoods(params[i].orderFoods);
			childOrdersToUpdate[i].setId(params[i].orderId);
		}
		expectOrderGroup.setChildOrder(childOrdersToUpdate);
		
		OrderGroupDao.update(term, expectOrderGroup);
		
		actualOrderGroup = QueryOrderDao.execByID(actualOrderId, QueryOrderDao.QUERY_TODAY);
		for(int i = 0; i < actualOrderGroup.getChildOrder().length; i++){
			actualOrderGroup.getChildOrder()[i] = QueryOrderDao.execByID(actualOrderGroup.getChildOrder()[i].getId(), QueryOrderDao.QUERY_TODAY);
			actualOrderGroup.getChildOrder()[i].setDestTbl(QueryTable.exec(term, actualOrderGroup.getChildOrder()[i].getDestTbl().getAliasId()));
			expectOrderGroup.getChildOrder()[i].setId(actualOrderGroup.getChildOrder()[i].getId());

		}
		compareOrderGroup(expectOrderGroup, actualOrderGroup);

		//Check the status to leaved order
		Order actualLeavedOrder = QueryOrderDao.execByID(expectedLeavedOrder.getId(), QueryOrderDao.QUERY_TODAY);
		actualLeavedOrder.setDestTbl(QueryTable.exec(term, actualLeavedOrder.getDestTbl().getAliasId()));
		//Check the category to table associated with leaved order
		Assert.assertEquals("cateogry to table associated with leaved order", actualLeavedOrder.getDestTbl().isNormal(), true);
		//Check the category to leaved order
		Assert.assertEquals("category to leaved order", actualLeavedOrder.getCategory(), Order.CATE_NORMAL);
		//Check the order foods to leaved order
		OrderFood[] expectedFoods = expectedLeavedOrder.getOrderFoods();
		OrderFood[] actualFoods = actualLeavedOrder.getOrderFoods();
		
		Arrays.sort(expectedFoods, FoodComp.DEFAULT);
		Arrays.sort(actualFoods, FoodComp.DEFAULT);
		
		Assert.assertEquals(expectedFoods.length, actualFoods.length);
		for(int j = 0; j < expectedFoods.length; j++){
			Assert.assertEquals("basic info to food[" + j + "]" + " in leaved order", expectedFoods[j], actualFoods[j]);
			Assert.assertEquals("order count to food[" + j + "]" + " in leaved order", expectedFoods[j].getCount(), actualFoods[j].getCount());
			Assert.assertEquals("unit price to food[" + j + "]" + " in leaved order", expectedFoods[j].getPrice(), actualFoods[j].getPrice());
		}
		
		//-----------------------------------------------------------------
		// Cancel the order group
		OrderGroupDao.cancel(term, actualOrderGroup);
		CancelOrder.exec(term, actualLeavedOrder.getDestTbl().getAliasId());
		for(Order childOrder : actualOrderGroup.getChildOrder()){
			CancelOrder.exec(term, childOrder.getDestTbl().getAliasId());
		}
	}
	
	private void compareOrderGroup(Order expected, Order actual){
		// Check the category to parent
		Assert.assertEquals("category to parent order group", actual.getCategory(), Order.CATE_MERGER_TABLE);
		
		// Check each child order
		Order[] expectedChildOrders = expected.getChildOrder();
		Order[] actualChildOrders = actual.getChildOrder();
		Assert.assertEquals(expectedChildOrders.length, actualChildOrders.length);
		
		for(int i = 0; i < expectedChildOrders.length; i++){
			//Check category to table associated with each child order
			Assert.assertEquals("category to table associated with child order[" + i + "]", actualChildOrders[i].getDestTbl().isMerged(), true);
			
			// Check the category to each child order
			Assert.assertEquals("category to child order[" + i + "]", expectedChildOrders[i].getCategory(), Order.CATE_MERGER_CHILD);
			
			// Check the table to each child order
			Assert.assertEquals("table alias to child order[" + i + "]", expectedChildOrders[i].getDestTbl().getAliasId(), actualChildOrders[i].getDestTbl().getAliasId());
			Assert.assertEquals("table id to child order[" + i + "]", expectedChildOrders[i].getDestTbl().getTableId(), actualChildOrders[i].getDestTbl().getTableId());
			
			// Check the order foods to each child order
			OrderFood[] expectedFoods = expectedChildOrders[i].getOrderFoods();
			OrderFood[] actualFoods = actualChildOrders[i].getOrderFoods();
			
			Arrays.sort(expectedFoods, FoodComp.DEFAULT);
			Arrays.sort(actualFoods, FoodComp.DEFAULT);
			
			Assert.assertEquals(expectedFoods.length, actualFoods.length);
			for(int j = 0; j < expectedFoods.length; j++){
				Assert.assertEquals("basic info to food[" + j + "]" + " in child order[" + i + "]", expectedFoods[j], actualFoods[j]);
				Assert.assertEquals("order count to food[" + j + "]" + " in child order[" + i + "]", expectedFoods[j].getCount(), actualFoods[j].getCount());
				Assert.assertEquals("unit price to food[" + j + "]" + " in child order[" + i + "]", expectedFoods[j].getPrice(), actualFoods[j].getPrice());
			}
		}
	}
	
	private static OrderFood buildOrderFood(Food food, float orderCnt){
		OrderFood of = new OrderFood(food);
		of.setCount(orderCnt);
		return of;
	}
	
	static class Params4Order{
		
		Params4Order(PTable tbl, OrderFood[] orderFoods){
			this.tbl = tbl;
			this.orderFoods = orderFoods;
		}
		
		Params4Order(PTable tbl, OrderFood[] orderFoods, int orderId){
			this.tbl = tbl;
			this.orderFoods = orderFoods;
			this.orderId = orderId;
		}
		
		PTable tbl;
		int orderId;
		OrderFood[] orderFoods;
	}
}
