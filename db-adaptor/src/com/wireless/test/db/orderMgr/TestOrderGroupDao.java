package com.wireless.test.db.orderMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.CancelOrder;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.OrderGroupDao;
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

public class TestOrderGroupDao {
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
	}
	
	@Test 
	public void testInsertByTbl() throws BusinessException, SQLException{
		
		Staff staff = StaffDao.getStaffs(37).get(0);
		
		List<Table> tbls = TableDao.getTables(staff, null, null);
		
		Table[] tblToInsert = new Table[]{
			tbls.get(0),
			tbls.get(1)
		};
		
		//Cancel the record before performing insertion.
		try{
			OrderGroupDao.cancel(staff, tblToInsert[0]);
		}catch(BusinessException e){
			
		}	
		
		for(Table tbl : tblToInsert){
			try{
				CancelOrder.exec(staff, tbl.getAliasId());
			}catch(BusinessException e){
				
			}
		}
			
		
		Order orderAfterInsert = OrderDao.getById(staff, OrderGroupDao.insert(staff, tblToInsert), DateType.TODAY);
		//Check if parent order is merged.
		Assert.assertTrue(orderAfterInsert.isMerged());
		
		if(orderAfterInsert.hasChildOrder()){
			Assert.assertEquals(orderAfterInsert.getChildOrder().size(), tblToInsert.length);
			for(Order childOrder : orderAfterInsert.getChildOrder()){
				Order orderToChild = OrderDao.getById(staff, childOrder.getId(), DateType.TODAY);
				//Check if each child order is merged.
				Assert.assertTrue(orderToChild.isMergedChild());
				//Check if the table associated with each child order is merged.
				Assert.assertTrue(orderToChild.getDestTbl().isMerged());
			}
		}else{
			Assert.assertTrue("The order does NOT contain any child order.", false);
		}
		
		//Cancel the record after performing insertion.
		OrderGroupDao.cancel(staff, tblToInsert[0]);
	}
	
	@Test
	public void testUpdateByTbl() throws BusinessException, SQLException{		

		Staff staff = StaffDao.getStaffs(37).get(0);
	
		List<Table> tbls = TableDao.getTables(staff, null, null);
		
		Table[] tblToInsert = new Table[]{
			tbls.get(0),
			tbls.get(1)
		};
		
		//Cancel the record before performing insertion.
		try{
			OrderGroupDao.cancel(staff, tblToInsert[0]);
		}catch(BusinessException e){
			
		}
		
		for(Table tbl : tblToInsert){
			try{
				CancelOrder.exec(staff, tbl.getAliasId());
			}catch(BusinessException e){
				
			}
		}
		
		int parentOrderId = OrderGroupDao.insert(staff, tblToInsert);
		
		Table[] tblToUpdate = new Table[]{
			tbls.get(0),
			tbls.get(1)
		};
		OrderGroupDao.update(staff, parentOrderId, tblToUpdate);		
		Order parentOrder = OrderDao.getById(staff, parentOrderId, DateType.TODAY);
		checkByTbl(staff, parentOrder, tblToUpdate);

		tblToUpdate = new Table[]{
			tbls.get(0),
		};
		OrderGroupDao.update(staff, parentOrderId, tblToUpdate);		
		parentOrder = OrderDao.getById(staff, parentOrderId, DateType.TODAY);
		checkByTbl(staff, parentOrder, tblToUpdate);
		
		tblToUpdate = new Table[]{
			tbls.get(1),
			tbls.get(0)
		};
		OrderGroupDao.update(staff, parentOrderId, tblToUpdate);		
		parentOrder = OrderDao.getById(staff, parentOrderId, DateType.TODAY);
		checkByTbl(staff, parentOrder, tblToUpdate);
		
		tblToUpdate = new Table[]{
			tbls.get(1),
			tbls.get(2)
		};
		OrderGroupDao.update(staff, parentOrderId, tblToUpdate);		
		parentOrder = OrderDao.getById(staff, parentOrderId, DateType.TODAY);
		checkByTbl(staff, parentOrder, tblToUpdate);
		
		OrderGroupDao.cancel(staff, tblToUpdate[0]);
	}
	
	private void checkByTbl(Staff term, Order orderToCheck, Table[] expectedTbls) throws BusinessException, SQLException{
		//Check if parent order is merged.
		Assert.assertTrue(orderToCheck.isMerged());
		
		if(orderToCheck.hasChildOrder()){
			Assert.assertEquals(orderToCheck.getChildOrder().size(), expectedTbls.length);
			for(Order childOrder : orderToCheck.getChildOrder()){
				Order orderToChild = OrderDao.getById(term, childOrder.getId(), DateType.TODAY);
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
	public void testUpdateByOrder() throws BusinessException, SQLException{
		Staff staff = StaffDao.getStaffs(37).get(0);
		
		List<Table> tbls = TableDao.getTables(staff, null, null);

		List<Food> foods = FoodDao.getPureFoods(staff, null, null);

		Params4Order[] params = new Params4Order[]{
			new Params4Order(tbls.get(0), 
					new OrderFood[]{
						buildOrderFood(foods.get(0), 1.53f),
						buildOrderFood(foods.get(1), 1.53f)
					}),
					
			new Params4Order(tbls.get(1), 
					new OrderFood[]{
						buildOrderFood(foods.get(0), 1.53f),
						buildOrderFood(foods.get(1), 1.53f)
					})	
		};
		
		//Cancel the record before performing insertion.
		try{
			OrderGroupDao.cancel(staff, params[0].tbl);
		}catch(BusinessException e){
			
		}
		
		for(Params4Order param : params){
			try{
				CancelOrder.exec(staff, param.tbl.getAliasId());
			}catch(BusinessException e){
				
			}
		}
		
		
		Order expectOrderGroup = new Order();
		
		List<Order> childOrdersToInsert = new ArrayList<Order>(params.length);
		for(int i = 0; i < params.length; i++){
			Order childOrderToInsert = new Order();
			childOrderToInsert.setDestTbl(params[i].tbl);
			childOrderToInsert.setOrderFoods(params[i].orderFoods);
			childOrdersToInsert.add(childOrderToInsert);
		}
		
		expectOrderGroup.setChildOrder(childOrdersToInsert);
		
		//---------------------------------------------------------------
		//Insert a new order group
		int actualOrderId = OrderGroupDao.insert(staff, expectOrderGroup);

		Order actualOrderGroup = OrderDao.getById(staff, actualOrderId, DateType.TODAY);
		for(int i = 0; i < actualOrderGroup.getChildOrder().size(); i++){
			actualOrderGroup.getChildOrder().set(i, OrderDao.getById(staff, actualOrderGroup.getChildOrder().get(i).getId(), DateType.TODAY));
			actualOrderGroup.getChildOrder().get(i).setDestTbl(TableDao.getTableByAlias(staff, actualOrderGroup.getChildOrder().get(i).getDestTbl().getAliasId()));
			expectOrderGroup.getChildOrder().get(i).setId(actualOrderGroup.getChildOrder().get(i).getId());
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
				new Params4Order(expectOrderGroup.getChildOrder().get(1).getDestTbl(), 
						new OrderFood[]{
							buildOrderFood(foods.get(1), 0.53f),
							buildOrderFood(foods.get(2), 2.53f)
						}, 
						expectOrderGroup.getChildOrder().get(1).getId()),
						
				new Params4Order(tbls.get(2), 
						new OrderFood[]{
							buildOrderFood(foods.get(0), 1.53f),
							buildOrderFood(foods.get(1), 1.53f)
						})	
			};

		Order expectedLeavedOrder = expectOrderGroup.getChildOrder().get(0);
		
		List<Order> childOrdersToUpdate = new ArrayList<Order>(params.length);
		for(int i = 0; i < params.length; i++){
			Order childOrderToUpdate = new Order();
			childOrderToUpdate.setDestTbl(params[i].tbl);
			childOrderToUpdate.setOrderFoods(params[i].orderFoods);
			childOrderToUpdate.setId(params[i].orderId);
			childOrdersToUpdate.add(childOrderToUpdate);
		}
		expectOrderGroup.setChildOrder(childOrdersToUpdate);
		
		OrderGroupDao.update(staff, expectOrderGroup);
		
		actualOrderGroup = OrderDao.getById(staff, actualOrderId, DateType.TODAY);
		for(int i = 0; i < actualOrderGroup.getChildOrder().size(); i++){
			actualOrderGroup.getChildOrder().set(i, OrderDao.getById(staff, actualOrderGroup.getChildOrder().get(i).getId(), DateType.TODAY));
			actualOrderGroup.getChildOrder().get(i).setDestTbl(TableDao.getTableByAlias(staff, actualOrderGroup.getChildOrder().get(i).getDestTbl().getAliasId()));
			expectOrderGroup.getChildOrder().get(i).setId(actualOrderGroup.getChildOrder().get(i).getId());

		}
		compareOrderGroup(expectOrderGroup, actualOrderGroup);

		//Check the status to leaved order
		Order actualLeavedOrder = OrderDao.getById(staff, expectedLeavedOrder.getId(), DateType.TODAY);
		actualLeavedOrder.setDestTbl(TableDao.getTableByAlias(staff, actualLeavedOrder.getDestTbl().getAliasId()));
		//Check the category to table associated with leaved order
		Assert.assertEquals("cateogry to table associated with leaved order", actualLeavedOrder.getDestTbl().isNormal(), true);
		//Check the category to leaved order
		Assert.assertEquals("category to leaved order", actualLeavedOrder.getCategory().getVal(), Order.Category.NORMAL.getVal());
		//Check the order foods to leaved order
		List<OrderFood> expectedFoods = expectedLeavedOrder.getOrderFoods();
		List<OrderFood> actualFoods = actualLeavedOrder.getOrderFoods();
		
		Collections.sort(expectedFoods);
		Collections.sort(actualFoods);
		
		Assert.assertEquals(expectedFoods.size(), actualFoods.size());
		for(int j = 0; j < expectedFoods.size(); j++){
			Assert.assertEquals("basic info to food[" + j + "]" + " in leaved order", expectedFoods.get(j), actualFoods.get(j));
			Assert.assertEquals("order count to food[" + j + "]" + " in leaved order", expectedFoods.get(j).getCount(), actualFoods.get(j).getCount(), 0.01);
			Assert.assertEquals("unit price to food[" + j + "]" + " in leaved order", expectedFoods.get(j).getPrice(), actualFoods.get(j).getPrice(), 0.01);
		}
		
		//-----------------------------------------------------------------
		// Cancel the order group
		OrderGroupDao.cancel(staff, actualOrderGroup);
		CancelOrder.exec(staff, actualLeavedOrder.getDestTbl().getAliasId());
		for(Order childOrder : actualOrderGroup.getChildOrder()){
			CancelOrder.exec(staff, childOrder.getDestTbl().getAliasId());
		}
	}
	
	private void compareOrderGroup(Order expected, Order actual){
		// Check the category to parent
		Assert.assertEquals("category to parent order group", actual.getCategory().getVal(), Order.Category.MERGER_TBL.getVal());
		
		// Check each child order
		List<Order> expectedChildOrders = expected.getChildOrder();
		List<Order> actualChildOrders = actual.getChildOrder();
		Assert.assertEquals(expectedChildOrders.size(), actualChildOrders.size());
		
		for(int i = 0; i < expectedChildOrders.size(); i++){
			//Check category to table associated with each child order
			Assert.assertEquals("category to table associated with child order[" + i + "]", actualChildOrders.get(i).getDestTbl().isMerged(), true);
			
			// Check the category to each child order
			Assert.assertEquals("category to child order[" + i + "]", expectedChildOrders.get(i).getCategory().getVal(), Order.Category.MERGER_CHILD.getVal());
			
			// Check the table to each child order
			Assert.assertEquals("table alias to child order[" + i + "]", expectedChildOrders.get(i).getDestTbl().getAliasId(), actualChildOrders.get(i).getDestTbl().getAliasId());
			Assert.assertEquals("table id to child order[" + i + "]", expectedChildOrders.get(i).getDestTbl().getTableId(), actualChildOrders.get(i).getDestTbl().getTableId());
			
			// Check the order foods to each child order
			List<OrderFood> expectedFoods = expectedChildOrders.get(i).getOrderFoods();
			List<OrderFood> actualFoods = actualChildOrders.get(i).getOrderFoods();
			
			Collections.sort(expectedFoods);
			Collections.sort(actualFoods);
			
			Assert.assertEquals(expectedFoods.size(), actualFoods.size());
			for(int j = 0; j < expectedFoods.size(); j++){
				Assert.assertEquals("basic info to food[" + j + "]" + " in child order[" + i + "]", expectedFoods.get(j), actualFoods.get(j));
				Assert.assertEquals("order count to food[" + j + "]" + " in child order[" + i + "]", expectedFoods.get(j).getCount(), actualFoods.get(j).getCount(), 0.01);
				Assert.assertEquals("unit price to food[" + j + "]" + " in child order[" + i + "]", expectedFoods.get(j).getPrice(), actualFoods.get(j).getPrice(), 0.01);
			}
		}
	}
	
	private static OrderFood buildOrderFood(Food food, float orderCnt){
		OrderFood of = new OrderFood(food);
		of.setCount(orderCnt);
		return of;
	}
	
	static class Params4Order{
		
		Params4Order(Table tbl, OrderFood[] orderFoods){
			this.tbl = tbl;
			this.orderFoods = Arrays.asList(orderFoods);
		}
		
		Params4Order(Table tbl, OrderFood[] orderFoods, int orderId){
			this.tbl = tbl;
			this.orderFoods = Arrays.asList(orderFoods);
			this.orderId = orderId;
		}
		
		Table tbl;
		int orderId;
		List<OrderFood> orderFoods;
	}
}
