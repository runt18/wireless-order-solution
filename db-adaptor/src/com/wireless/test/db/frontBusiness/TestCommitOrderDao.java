package com.wireless.test.db.frontBusiness;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.member.MemberTypeDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.orderMgr.InsertOrder;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.PayOrder;
import com.wireless.db.orderMgr.UpdateOrder;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.exception.TableError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.Order.PayBuilder;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberOperation.ChargeType;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.regionMgr.Table.Category;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.SortedList;
import com.wireless.test.db.TestInit;

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
	public void testCommit4Feast() throws BusinessException, SQLException{
		int orderId = 0;
		try{
			final List<Department> depts = DepartmentDao.getByType(mStaff, Department.Type.NORMAL);
			orderId = OrderDao.feast(mStaff, new Order.FeastBuilder().add(depts.get(0).getId(), 1000).add(depts.get(1), 2000));
			
			Order actual = OrderDao.getById(mStaff, orderId, DateType.TODAY);
			
			Assert.assertEquals("feast : total price", 3000, actual.getActualPrice(), 0.01);
			Assert.assertEquals("feast : order category", Order.Category.FEAST, actual.getCategory());
			
			for(OrderFood of : actual.getOrderFoods()){
				if(of.asFood().getKitchen().getDept().equals(depts.get(0))){
					Assert.assertEquals(of.getName() + "#name", depts.get(0).getName() + "酒席费", of.getName());
					Assert.assertEquals(of.getName() + "#price", 1000, of.getPrice(), 0.01);
					Assert.assertEquals(of.getName() + "#count", 1, of.getCount(), 0.01);
					
				}else if(of.asFood().getKitchen().getDept().equals(depts.get(1))){
					Assert.assertEquals(of.getName() + "#name", depts.get(1).getName() + "酒席费", of.getName());
					Assert.assertEquals(of.getName() + "#price", 2000, of.getPrice(), 0.01);
					Assert.assertEquals(of.getName() + "#count", 1, of.getCount(), 0.01);
				}
			}
			
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
	
	@Test
	public void testCommit4Fast() throws BusinessException, SQLException{
		int orderId = 0;
		try{
			
			List<Food> foods = FoodDao.getPureByCond(mStaff, null, null);
			
			Order expectedOrder = new Order(0);
			expectedOrder.setCustomNum(10);
			expectedOrder.setCategory(Order.Category.FAST);
			
			OrderFood of;
			of = new OrderFood(foods.get(0));
			of.setCount(1.35f);
			expectedOrder.addFood(of, mStaff);
			
			of = new OrderFood(foods.get(1));
			of.setCount(2.35f);
			expectedOrder.addFood(of, mStaff);
			
			//-----------Test to insert a new fast order---------------------------
			final int fastNo = 1;
			orderId = InsertOrder.exec(mStaff, Order.InsertBuilder.newInstance4Fast(fastNo)
																  .addAll(expectedOrder.getOrderFoods(), mStaff).setCustomNum(expectedOrder.getCustomNum())).getId();
			
			Order actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
			
			compare4FastCommit(expectedOrder, actualOrder, fastNo);
			
			//-----------Test to pay the order---------------------------
			Order.PayBuilder payBuilder = Order.PayBuilder.build4Normal(orderId, PayType.CASH);
			PayOrder.pay(mStaff, payBuilder);
			
			actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
			compare4Payment(payBuilder, expectedOrder, actualOrder);
			
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
	
	@Test
	public void testCommit4Join() throws BusinessException, SQLException{
		int orderId = 0;
		try{
			final Table table = TableDao.getByCond(mStaff, null, null).get(0);
			
			List<Food> foods = FoodDao.getPureByCond(mStaff, null, null);
			
			Order expectedOrder = new Order(0);
			expectedOrder.setDestTbl(table);
			expectedOrder.setCustomNum(10);
			expectedOrder.setCategory(Order.Category.JOIN);
			
			OrderFood of;
			of = new OrderFood(foods.get(0));
			of.setCount(1.35f);
			expectedOrder.addFood(of, mStaff);
			
			of = new OrderFood(foods.get(1));
			of.setCount(2.35f);
			expectedOrder.addFood(of, mStaff);
			
			//-----------Test to insert a new joined order---------------------------
			orderId = InsertOrder.exec(mStaff, Order.InsertBuilder.newInstance4Join(new Table.Builder(table.getId()), Table.InsertBuilder4Join.Suffix.A)
																  .addAll(expectedOrder.getOrderFoods(), mStaff).setCustomNum(expectedOrder.getCustomNum())).getId();
			
			Order actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
			
			compare4JoinCommit(expectedOrder, actualOrder, Table.InsertBuilder4Join.Suffix.A);
			
			//-----------Test to pay the order---------------------------
			Order.PayBuilder payBuilder = Order.PayBuilder.build4Normal(orderId, PayType.CASH);
			PayOrder.pay(mStaff, payBuilder);
			
			actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
			compare4Payment(payBuilder, expectedOrder, actualOrder);
			
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
	
	@Test
	public void testCommit() throws BusinessException, SQLException{
		
		int orderId = 0;
		final List<Table> idleTables = TableDao.getByCond(mStaff, new TableDao.ExtraCond().setStatus(Table.Status.IDLE).addCategory(Category.NORMAL), null);
		//final List<Table> busyTables = TableDao.getByCond(mStaff, new TableDao.ExtraCond().setStatus(Table.Status.BUSY), null);
		
		try{
			Table tblToInsert = idleTables.get(0);
			OrderDao.deleteByCond(mStaff, new OrderDao.ExtraCond(DateType.TODAY).setTableAlias(tblToInsert.getAliasId()));
			
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
			orderId = InsertOrder.exec(mStaff, new Order.InsertBuilder(new Table.Builder(tblToInsert.getId())).addAll(expectedOrder.getOrderFoods(), mStaff).setCustomNum(expectedOrder.getCustomNum())).getId();
			
			Order actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
			
			compare4Commit(expectedOrder, actualOrder);
			
			//-----------Test to update the order---------------------------
			expectedOrder.removeAll(mStaff);
			
			of = new OrderFood(foods.get(1));
			of.setCount(1.35f);
			expectedOrder.addFood(of, mStaff);
			
			of = new OrderFood(foods.get(2));
			of.setCount(2.35f);
			expectedOrder.addFood(of, mStaff);
			
			UpdateOrder.exec(mStaff, new Order.UpdateBuilder(actualOrder).addNew(expectedOrder.getOrderFoods(), mStaff));
			
			actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
			
			compare4Commit(expectedOrder, actualOrder);
			
			//-----------Test to update the order---------------------------
			expectedOrder.removeAll(mStaff);
			
			of = new OrderFood(foods.get(0));
			of.setCount(1.35f);
			expectedOrder.addFood(of, mStaff);
			
			of = new OrderFood(foods.get(1));
			of.setCount(2.35f);
			expectedOrder.addFood(of, mStaff);
			
			UpdateOrder.exec(mStaff, new Order.UpdateBuilder(actualOrder).addNew(expectedOrder.getOrderFoods(), mStaff));
			
			actualOrder = OrderDao.getById(mStaff, actualOrder.getId(), DateType.TODAY);
			
			compare4Commit(expectedOrder, actualOrder);

			//-----------Test to transfer food---------------------------
//			OrderFood transferFood1 = actualOrder.getOrderFoods().get(0);
//			OrderFood transferFood2 = actualOrder.getOrderFoods().get(1);
//			
//			if(!busyTables.isEmpty()){
//				Table tblToTransfer = busyTables.get(0);
//				Order expectedTransferOrder = OrderDao.getByTableId(mStaff, tblToTransfer.getId());
//				
//				expectedTransferOrder.addFood(transferFood1, mStaff);
//				
//				OrderDao.transfer(mStaff, new Order.TransferBuilder(orderId, new Table.AliasBuilder(tblToTransfer.getAliasId())).add(transferFood1));
//
//				expectedOrder.remove(transferFood1, mStaff);
//				
//				actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
//
//				compare4Commit(expectedOrder, actualOrder);
//
//				Order actualTransferOrder = OrderDao.getByTableId(mStaff, tblToTransfer.getId());
//				compare4Commit(expectedTransferOrder, actualTransferOrder);
//				
//			}
//			
//			if(!idleTables.isEmpty()){
//				Table tblToTransfer = idleTables.get(1);
//				Order expectedTransferOrder = new Order(0);
//				expectedTransferOrder.setCustomNum(1);
//				expectedTransferOrder.setDestTbl(tblToTransfer);
//				
//				expectedTransferOrder.addFood(transferFood2, mStaff);
//				
//				OrderDao.transfer(mStaff, new Order.TransferBuilder(orderId, new Table.AliasBuilder(tblToTransfer.getAliasId())).add(transferFood2));
//
//				expectedOrder.remove(transferFood2, mStaff);
//				
//				actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
//
//				compare4Commit(expectedOrder, actualOrder);
//
//				Order actualTransferOrder = OrderDao.getByTableId(mStaff, tblToTransfer.getId());
//				compare4Commit(expectedTransferOrder, actualTransferOrder);
//				
//				OrderDao.deleteByCond(mStaff, new OrderDao.ExtraCond(DateType.TODAY).setOrderId(actualTransferOrder.getId()));
//			}
			

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
			
			//-----------Test to re-pay the order in case of normal -> member---------------------------
			final MemberType chargeType = MemberTypeDao.getByCond(mStaff, new MemberTypeDao.ExtraCond().setAttribute(MemberType.Attribute.CHARGE), null).get(0);
			Member expectedMember = MemberDao.getByCond(mStaff, new MemberDao.ExtraCond().setMemberType(chargeType), null).get(0);
			MemberDao.charge(mStaff, expectedMember.getId(), actualOrder.getActualPrice(), actualOrder.getActualPrice(), ChargeType.CASH);
			expectedMember = MemberDao.getById(mStaff, expectedMember.getId());
			
			Order.DiscountBuilder discountBuilder = Order.DiscountBuilder.build4Member(orderId, expectedMember);
			Order.UpdateBuilder updateBuilder = new Order.UpdateBuilder(actualOrder).addOri(actualOrder.getOrderFoods());
			payBuilder = PayBuilder.build4Member(orderId, PayType.MEMBER).setSms(false);
			OrderDao.repaid(mStaff, new Order.RepaidBuilder(updateBuilder, payBuilder).setDiscountBuilder(discountBuilder));
			
			Member actualMember = MemberDao.getById(mStaff, expectedMember.getId());
			actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
			
			expectedOrder.setSettleType(Order.SettleType.MEMBER);
			compare4RePayment(payBuilder, expectedOrder, actualOrder);
			
			MemberOperation expectedMo = expectedMember.consume(actualOrder.getActualPrice(), PayType.MEMBER);
			compare4RePayment(expectedMember, actualMember);
			compare4RePayment(expectedMo, MemberOperationDao.getLastConsumptionByOrder(mStaff, actualOrder));
			
			//-----------Test to re-pay the order in case of the same member -> member---------------------------
			expectedMember = MemberDao.getById(mStaff, expectedMember.getId());
			
			discountBuilder = Order.DiscountBuilder.build4Member(orderId, expectedMember);
			updateBuilder = new Order.UpdateBuilder(actualOrder).addOri(actualOrder.getOrderFoods());
			payBuilder = PayBuilder.build4Member(orderId, PayType.MEMBER).setSms(false);
			
			expectedMember.restore(MemberOperationDao.getLastConsumptionByOrder(mStaff, actualOrder));
			expectedMo = expectedMember.reConsume(actualOrder.getActualPrice(), PayType.MEMBER);
			
			OrderDao.repaid(mStaff, new Order.RepaidBuilder(updateBuilder, payBuilder).setDiscountBuilder(discountBuilder));
			
			actualMember = MemberDao.getById(mStaff, expectedMember.getId());
			actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
			
			expectedOrder.setSettleType(Order.SettleType.MEMBER);
			compare4RePayment(payBuilder, expectedOrder, actualOrder);
			
			compare4RePayment(expectedMember, actualMember);
			compare4RePayment(expectedMo, MemberOperationDao.getLastConsumptionByOrder(mStaff, actualOrder));
			
			//-----------Test to re-pay the order in case of the different member -> member---------------------------
			Member expectedNewMember = MemberDao.getByCond(mStaff, new MemberDao.ExtraCond().setMemberType(chargeType), null).get(1);
			MemberDao.charge(mStaff, expectedNewMember.getId(), actualOrder.getActualPrice(), actualOrder.getActualPrice(), ChargeType.CASH);
			expectedNewMember = MemberDao.getById(mStaff, expectedNewMember.getId());
			Member expectedOriMember = expectedMember = MemberDao.getById(mStaff, expectedMember.getId());
			
			discountBuilder = Order.DiscountBuilder.build4Member(orderId, expectedNewMember);
			updateBuilder = new Order.UpdateBuilder(actualOrder).addOri(actualOrder.getOrderFoods());
			payBuilder = PayBuilder.build4Member(orderId, PayType.MEMBER).setSms(false);
			
			MemberOperation expectedOriMo = expectedOriMember.restore(MemberOperationDao.getLastConsumptionByOrder(mStaff, actualOrder));
			
			OrderDao.repaid(mStaff, new Order.RepaidBuilder(updateBuilder, payBuilder).setDiscountBuilder(discountBuilder));
			
			actualOrder = OrderDao.getById(mStaff, orderId, DateType.TODAY);
			
			MemberOperation expectedNewMo = expectedNewMember.reConsume(actualOrder.getActualPrice(), PayType.MEMBER);
			
			Member actualNewMember = MemberDao.getById(mStaff, expectedNewMember.getId());
			Member actualOriMember = MemberDao.getById(mStaff, expectedOriMember.getId());
			
			expectedOrder.setSettleType(Order.SettleType.MEMBER);
			compare4RePayment(payBuilder, expectedOrder, actualOrder);
			
			compare4RePayment(expectedOriMember, actualOriMember);
			compare4RePayment(expectedOriMo, MemberOperationDao.getByCond(mStaff, new MemberOperationDao.ExtraCond(DateType.TODAY).addMember(actualOriMember), " ORDER BY id DESC LIMIT 1 ").get(0));
			
			compare4RePayment(expectedNewMember, actualNewMember);
			compare4RePayment(expectedNewMo, MemberOperationDao.getLastConsumptionByOrder(mStaff, actualOrder));
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
		Assert.assertEquals("the settle to order", Order.SettleType.NORMAL, actual.getSettleType());
		//Check the total price
		Assert.assertEquals("the total price to order", expected.calcTotalPrice(), actual.getTotalPrice(), 0.01);
		//Check the order status
		Assert.assertEquals("the status to order", Order.Status.REPAID, actual.getStatus());
		//Check the mixed payment
		Assert.assertEquals("the mixed payment to order", payBuilder.getMixedPayment(), actual.getMixedPayment());
	}
	
	private void compare4RePayment(MemberOperation expected, MemberOperation actual){
		assertEquals("mo - associated restaurant id", mStaff.getRestaurantId(), actual.getRestaurantId());
//		assertEquals("mo - staff id", mStaff.getId(), actual.getStaffId());
//		assertEquals("mo - member name", expected.getMemberName(), actual.getMemberName());
//		assertEquals("mo - member mobile", expected.getMemberMobile(), actual.getMemberMobile());
//		assertEquals("mo - member id", expected.getMemberId(), actual.getMemberId());
//		assertEquals("mo - member card", expected.getMemberCard(), actual.getMemberCard());
//		assertEquals("mo - operation seq", expected.getOperateSeq(), actual.getOperateSeq());
//		assertEquals("mo - operation date", expected.getOperateDate(), actual.getOperateDate());
		assertEquals("mo - operation type", expected.getOperationType(), expected.getOperationType());
		assertEquals("mo - consume money", expected.getPayMoney(), actual.getPayMoney(), 0.01);
		assertEquals("mo - charge type", expected.getChargeType(), actual.getChargeType());
		assertEquals("mo - charge balance", expected.getChargeMoney(), actual.getChargeMoney(), 0.01);
		assertEquals("mo - delta base balance", expected.getDeltaBaseMoney(), actual.getDeltaBaseMoney(), 0.01);
		assertEquals("mo - delta extra balance", expected.getDeltaExtraMoney(), actual.getDeltaExtraMoney(), 0.01);
		assertEquals("mo - delta point", expected.getDeltaPoint(), actual.getDeltaPoint());
		assertEquals("mo - remaining base balance", expected.getRemainingBaseMoney(), actual.getRemainingBaseMoney(), 0.01);
		assertEquals("mo - remaining extra balance", expected.getRemainingExtraMoney(), actual.getRemainingExtraMoney(), 0.01);
		assertEquals("mo - remaining point", expected.getRemainingPoint(), actual.getRemainingPoint());
		assertEquals("mo - coupon id", expected.getCouponId(), actual.getCouponId());
		assertEquals("mo - coupon money", expected.getCouponMoney(), actual.getCouponMoney(), 0.01);
		assertEquals("mo - coupon name", expected.getCouponName(), actual.getCouponName());
	}
	
	private void compare4RePayment(Member expected, Member actual){
		assertEquals("member id", expected.getId(), actual.getId());
		assertEquals("member card", expected.getMemberCard(), actual.getMemberCard());
		assertEquals("member_name", expected.getName(), actual.getName());
		assertEquals("member mobile", expected.getMobile(), actual.getMobile());
		assertEquals("member type", expected.getMemberType(), actual.getMemberType());
		assertEquals("associated restaurant id", expected.getRestaurantId(), actual.getRestaurantId());
		assertEquals("member consumption amount", expected.getConsumptionAmount(),  actual.getConsumptionAmount());
		assertEquals("member used balance", expected.getUsedBalance(), actual.getUsedBalance(), 0.01);
		assertEquals("member base balance", expected.getBaseBalance(), actual.getBaseBalance(), 0.01);
		assertEquals("member extra balance", expected.getExtraBalance(), actual.getExtraBalance(), 0.01);
		assertEquals("member point", expected.getPoint(), actual.getPoint());
		assertEquals("member used point", expected.getUsedPoint(), actual.getUsedPoint());
		assertEquals("member total consumption", expected.getTotalConsumption(), actual.getTotalConsumption(), 0.01);
		assertEquals("member total point", expected.getTotalPoint(), actual.getTotalPoint(), 0.01);
	}
	
	private void compare4RePayment(Order.PayBuilder payBuilder, Order expected, Order actual) throws SQLException{
		//Check the associated table
		Assert.assertEquals("the payment to order", payBuilder.getPaymentType(), actual.getPaymentType());
		//Check the custom number
		Assert.assertEquals("the custom number to order", payBuilder.getCustomNum(), actual.getCustomNum());
		//Check the settle type
		Assert.assertEquals("the settle to order", expected.getSettleType(), actual.getSettleType());
		//Check the total price
		expected.setDiscount(DiscountDao.getDefault(mStaff));
		Assert.assertEquals("the total price to order", expected.calcTotalPrice(), actual.getTotalPrice(), 0.01);
		//Check the order status
		Assert.assertEquals("the status to order", Order.Status.REPAID, actual.getStatus());
	}
	
	private void compare4Payment(Order.PayBuilder payBuilder, Order expected, Order actual) throws SQLException{
		if(expected.getCategory().isNormal()){
			try{
				TableDao.getById(mStaff, expected.getDestTbl().getId());
			}catch(BusinessException e){
				Assert.assertTrue("the table is removed after payment", false);
			}
			
		}else if(expected.getCategory().isJoin() || expected.getCategory().isTakeout() || expected.getCategory().isFast()){
			try{
				TableDao.getById(mStaff, expected.getDestTbl().getId());
				Assert.assertTrue("the temporary 【" + expected.getCategory().getDesc() + "】 table NOT be removed after payment", false);
			}catch(BusinessException e){
				Assert.assertEquals("the temporary 【" + expected.getCategory().getDesc() + "】 table NOT be removed after payment", TableError.TABLE_NOT_EXIST, e.getErrCode());
			}
		}
		//Check the associated table
		Assert.assertEquals("the payment to order", payBuilder.getPaymentType(), actual.getPaymentType());
		//Check the custom number
		Assert.assertEquals("the custom number to order", payBuilder.getCustomNum(), actual.getCustomNum());
		//Check the settle type
		Assert.assertEquals("the settle to order", Order.SettleType.NORMAL, actual.getSettleType());
		//Check the total price
		expected.setDiscount(DiscountDao.getDefault(mStaff));
		Assert.assertEquals("the total price to order", expected.calcTotalPrice(), actual.getTotalPrice(), 0.01);
		//Check the order status
		Assert.assertEquals("the status to order", Order.Status.PAID, actual.getStatus());
	}
	
	private void compare4Commit(Order expected, Order actual) throws BusinessException, SQLException{
		
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
		Table tbl = TableDao.getById(mStaff, actual.getDestTbl().getId());
		//Check the status to associated table
		Assert.assertEquals("the status to associated table", tbl.getStatus().getVal(), Table.Status.BUSY.getVal());
		//Check the custom number to associated table
		Assert.assertEquals("the custom number to associated table", tbl.getCustomNum(), actual.getCustomNum());
		//Check the category to associated table
		Assert.assertEquals("the category to associated table", tbl.getCategory().getVal(), actual.getCategory().getVal());
	}
	
	private void compare4JoinCommit(Order expected, Order actual, Table.InsertBuilder4Join.Suffix suffix) throws BusinessException, SQLException{
		Table tbl = TableDao.getById(mStaff, actual.getDestTbl().getId());
		expected.getDestTbl().setId(tbl.getId());
		expected.getDestTbl().setTableName(expected.getDestTbl().getAliasId() + suffix.getVal() + "(搭" + expected.getDestTbl().getName() + ")");
		expected.getDestTbl().setTableAlias(tbl.getAliasId());
		expected.getDestTbl().setCategory(Table.Category.JOIN);
		//Check the name to joined table
		Assert.assertEquals("the name to joined table", expected.getDestTbl().getName(), tbl.getName());
		//Check the category to joined table
		Assert.assertEquals("the category to joined table", expected.getDestTbl().getCategory(), actual.getDestTbl().getCategory());
		
		compare4Commit(expected, actual);
	}
	
	private void compare4FastCommit(Order expected, Order actual, int fastNo) throws BusinessException, SQLException{
		Table tbl = TableDao.getById(mStaff, actual.getDestTbl().getId());
		expected.getDestTbl().setId(tbl.getId());
		expected.getDestTbl().setTableName("快餐#" + fastNo);
		expected.getDestTbl().setTableAlias(tbl.getAliasId());
		expected.getDestTbl().setCategory(Table.Category.FAST);
		expected.getDestTbl().setRestaurantId(tbl.getRestaurantId());
		//Check the name to joined table
		Assert.assertEquals("the name to fast table", expected.getDestTbl().getName(), tbl.getName());
		//Check the category to joined table
		Assert.assertEquals("the category to fast table", expected.getDestTbl().getCategory(), actual.getDestTbl().getCategory());
		
		compare4Commit(expected, actual);
	}
}
