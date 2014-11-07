package com.wireless.test.db.frontBusiness;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.orderMgr.InsertOrder;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.PayOrder;
import com.wireless.db.orderMgr.UpdateOrder;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.SortedList;
import com.wireless.test.db.TestInit;
import com.wireless.util.DateType;

public class TestCommitOrderDao {
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getAdminByRestaurant(40);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCommitOrder() throws BusinessException, BusinessException, SQLException{
		
		int orderId = 0;
		final List<Table> idleTables = TableDao.getByCond(mStaff, new TableDao.ExtraCond().setStatus(Table.Status.IDLE), null);
		final List<Table> busyTables = TableDao.getByCond(mStaff, new TableDao.ExtraCond().setStatus(Table.Status.BUSY), null);
		
		try{
			Table tblToInsert = idleTables.get(0);
			List<Food> foods = FoodDao.getPureByCond(mStaff, null, null);
			
			Order expectedOrder = new Order(0);
			expectedOrder.setDestTbl(tblToInsert);
			expectedOrder.setCustomNum(10);
			expectedOrder.setCategory(Order.Category.NORMAL);
			
			OrderFood of;
			of = new OrderFood(foods.get(0));
			of.setCount(1.35f);
			expectedOrder.addFood(of, mStaff);
			
			of = new OrderFood(foods.get(1));
			of.setCount(2.35f);
			expectedOrder.addFood(of, mStaff);
			
			//-----------Test to insert a new order---------------------------
			orderId = InsertOrder.exec(mStaff, new Order.InsertBuilder(new Table.AliasBuilder(tblToInsert.getAliasId())).addAll(expectedOrder.getOrderFoods(), mStaff).setCustomNum(expectedOrder.getCustomNum())).getId();
			
			Order actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
			
			compareOrder4Commit(expectedOrder, actualOrder);
			
			//-----------Test to update the order---------------------------
			expectedOrder.removeAll(mStaff);
			
			of = new OrderFood(foods.get(1));
			of.setCount(1.35f);
			expectedOrder.addFood(of, mStaff);
			
			of = new OrderFood(foods.get(2));
			of.setCount(2.35f);
			expectedOrder.addFood(of, mStaff);
			
			UpdateOrder.exec(mStaff, new Order.UpdateBuilder(orderId, actualOrder.getOrderDate()).addAll(expectedOrder.getOrderFoods(), mStaff));
			
			actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
			
			compareOrder4Commit(expectedOrder, actualOrder);
			
			//-----------Test to update the order---------------------------
			expectedOrder.removeAll(mStaff);
			
			of = new OrderFood(foods.get(0));
			of.setCount(1.35f);
			expectedOrder.addFood(of, mStaff);
			
			of = new OrderFood(foods.get(1));
			of.setCount(2.35f);
			expectedOrder.addFood(of, mStaff);
			
			UpdateOrder.exec(mStaff, new Order.UpdateBuilder(orderId, actualOrder.getOrderDate()).addAll(expectedOrder.getOrderFoods(), mStaff));
			
			actualOrder = OrderDao.getById(mStaff, actualOrder.getId(), DateType.TODAY);
			
			compareOrder4Commit(expectedOrder, actualOrder);

			//-----------Test to transfer food---------------------------
			OrderFood transferFood1 = actualOrder.getOrderFoods().get(0);
			OrderFood transferFood2 = actualOrder.getOrderFoods().get(1);
			
			if(!busyTables.isEmpty()){
				Table tblToTransfer = busyTables.get(0);
				Order expectedTransferOrder = OrderDao.getByTableAlias(mStaff, tblToTransfer.getAliasId());
				
				expectedTransferOrder.addFood(transferFood1, mStaff);
				
				OrderDao.transfer(mStaff, new Order.TransferBuilder(orderId, new Table.AliasBuilder(tblToTransfer.getAliasId())).add(transferFood1));

				expectedOrder.remove(transferFood1, mStaff);
				
				actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);

				compareOrder4Commit(expectedOrder, actualOrder);

				Order actualTransferOrder = OrderDao.getByTableAlias(mStaff, tblToTransfer.getAliasId());
				compareOrder4Commit(expectedTransferOrder, actualTransferOrder);
				
			}
			
			if(!idleTables.isEmpty()){
				Table tblToTransfer = idleTables.get(1);
				Order expectedTransferOrder = new Order(0);
				expectedTransferOrder.setDestTbl(tblToTransfer);
				
				expectedTransferOrder.addFood(transferFood2, mStaff);
				
				OrderDao.transfer(mStaff, new Order.TransferBuilder(orderId, new Table.AliasBuilder(tblToTransfer.getAliasId())).add(transferFood2));

				expectedOrder.remove(transferFood2, mStaff);
				
				actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);

				compareOrder4Commit(expectedOrder, actualOrder);

				Order actualTransferOrder = OrderDao.getByTableAlias(mStaff, tblToTransfer.getAliasId());
				compareOrder4Commit(expectedTransferOrder, actualTransferOrder);
				
				OrderDao.deleteByCond(mStaff, new OrderDao.ExtraCond(DateType.TODAY).setOrderId(actualTransferOrder.getId()));
			}
			

			//-----------Test to pay the order---------------------------
			Order.PayBuilder payBuilder = Order.PayBuilder.build4Normal(orderId, PayType.CASH);
			PayOrder.pay(mStaff, payBuilder);
			
			actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
			compare4Payment(payBuilder, expectedOrder, actualOrder);

			//-----------Test to re-pay the order using mixed payment---------------------------
			float cash = actualOrder.getActualPrice() / 2;
			float creditCard = actualOrder.getActualPrice() - cash;
			payBuilder = Order.PayBuilder.build4Normal(orderId, PayType.MIXED).addPayment(PayType.CASH, cash).addPayment(PayType.CREDIT_CARD, creditCard);
			PayOrder.pay(mStaff, payBuilder);
			
			actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
			compare4MixedPayment(payBuilder, expectedOrder, actualOrder);
			
		}finally{
			if(orderId != 0){
				OrderDao.deleteByCond(mStaff, new OrderDao.ExtraCond(DateType.TODAY).setOrderId(orderId));
				try{
					//Check to see whether the order is deleted.
					OrderDao.getById(mStaff, orderId, DateType.TODAY);
					Assert.assertTrue("failed to delete order", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the order", FrontBusinessError.ORDER_NOT_EXIST, e.getErrCode());
				}
			}
		}
		
	}
	
	private void compare4MixedPayment(Order.PayBuilder payBuilder, Order expected, Order actual){
		//Check the associated table
		Assert.assertEquals("the payment to order", payBuilder.getPaymentType(), actual.getPaymentType());
		//Check the custom number
		Assert.assertEquals("the custom number to order", payBuilder.getCustomNum(), actual.getCustomNum());
		//Check the settle type
		Assert.assertEquals("the settle to order", payBuilder.getSettleType(), actual.getSettleType());
		//Check the total price
		Assert.assertEquals("the total price to order", expected.calcTotalPrice(), actual.getTotalPrice(), 0.01);
		//Check the order status
		Assert.assertEquals("the status to order", Order.Status.REPAID, actual.getStatus());
		//Check the mixed payment
		Assert.assertEquals("the mixed payment to order", payBuilder.getMixedPayment(), actual.getMixedPayment());
	}
	
	private void compare4Payment(Order.PayBuilder payBuilder, Order expected, Order actual){
		//Check the associated table
		Assert.assertEquals("the payment to order", payBuilder.getPaymentType(), actual.getPaymentType());
		//Check the custom number
		Assert.assertEquals("the custom number to order", payBuilder.getCustomNum(), actual.getCustomNum());
		//Check the settle type
		Assert.assertEquals("the settle to order", payBuilder.getSettleType(), actual.getSettleType());
		//Check the total price
		Assert.assertEquals("the total price to order", expected.calcTotalPrice(), actual.getTotalPrice(), 0.01);
		//Check the order status
		Assert.assertEquals("the status to order", Order.Status.PAID, actual.getStatus());
	}
	
	private void compareOrder4Commit(Order expected, Order actual) throws BusinessException, SQLException{
		
		//Check the associated table
		Assert.assertEquals("the table to order", expected.getDestTbl(), actual.getDestTbl());
		//Check the custom number
		Assert.assertEquals("the custom number to order", expected.getCustomNum(), actual.getCustomNum());
		//Check the category
		Assert.assertEquals("the category to order", expected.getCategory(), actual.getCategory());
		//Check the order foods
		Comparator<OrderFood> comp = new Comparator<OrderFood>(){
			@Override
			public int compare(OrderFood arg0, OrderFood arg1) {
				return arg0.asFood().compareTo(arg1.asFood());
			}
		};
		List<OrderFood> expectedFoods = SortedList.newInstance(expected.getOrderFoods(), comp);
		List<OrderFood> actualFoods = SortedList.newInstance(actual.getOrderFoods(), comp);
		
		Assert.assertEquals(expectedFoods.size(), actualFoods.size());
		for(int i = 0; i < expectedFoods.size(); i++){
			Assert.assertEquals("basic info to food[" + i + "]", expectedFoods.get(i), actualFoods.get(i));
			Assert.assertEquals("order count to food[" + i + "]", expectedFoods.get(i).getCount(), actualFoods.get(i).getCount(), 0.01);
		}
		
		//Check the associated table detail
		Table tbl = TableDao.getByAlias(mStaff, actual.getDestTbl().getAliasId());
		//Check the status to associated table
		Assert.assertEquals("the status to associated table", tbl.getStatus().getVal(), Table.Status.BUSY.getVal());
		//Check the custom number to associated table
		Assert.assertEquals("the custom number to associated table", tbl.getCustomNum(), actual.getCustomNum());
		//Check the category to associated table
		Assert.assertEquals("the category to associated table", tbl.getCategory().getVal(), actual.getCategory().getVal());
	}
}
