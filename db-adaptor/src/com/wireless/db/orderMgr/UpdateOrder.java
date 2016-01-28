package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.exception.StaffError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.weixin.order.WxOrder;

public class UpdateOrder {
	
	/**
	 * The difference result between two orders, which consists of several things below.
	 * <li>The extra foods more than the original.
	 * <li>The cancelled foods less than the original.
	 * <li>The hurried foods in the new.
	 * <li>The gift foods in the new. 
	 */
	public static class DiffResult{
		public final Order oriOrder;
		public final Order newOrder;
		public final List<OrderFood> extraFoods = new ArrayList<OrderFood>();
		public final List<OrderFood> cancelledFoods = new ArrayList<OrderFood>();
		public final List<OrderFood> hurriedFoods = new ArrayList<OrderFood>();
		
		public DiffResult(Order oriOrder, Order newOrder) {
			this.oriOrder = oriOrder;
			this.newOrder = newOrder;
		}
	}
	
	/**
	 * Update the order according to specific builder {@link Order#UpdateBuilder}.
	 * @param staff 
	 * 			the staff to perform this action
	 * @param builder
	 *          the builder to update order {@link Order#UpdateBuilder}
	 * @return the difference result {@link DiffResult} between the original and new
	 * @throws BusinessException
	 *             throws if one of the cases below<br>
	 *             <li>the order to this id does NOT exist
	 *             <li>the order to this id is expired
	 *             <li>any food to this order does NOT exist
	 *             <li>any taste to this order does NOT exist
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static DiffResult exec(Staff staff, Order.UpdateBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();	
		
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			DiffResult result = exec(dbCon, staff, builder);
			dbCon.conn.commit();
			return result;
			
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the order according to specific builder {@link Order#UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff 
	 * 			the staff to perform this action
	 * @param builder
	 *          the builder to update order {@link Order#UpdateBuilder}
	 * @return the difference result {@link DiffResult} between the original and new
	 * @throws BusinessException
	 *             throws if one of the cases below<br>
	 *             <li>the order to this id does NOT exist
	 *             <li>the order to this id is expired
	 *             <li>any food to this order does NOT exist
	 *             <li>any taste to this order does NOT exist
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static DiffResult exec(DBCon dbCon, Staff staff, Order.UpdateBuilder builder) throws BusinessException, SQLException{
		return doUpdate(dbCon, staff, doPrepare(dbCon, staff, builder), builder);
	}
	
	/**
	 * Prepare to calculate the difference between new order and the original, which is used in {@link doUpdate}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param builder
	 * 			the builder to update order {@link Order#UpdateBuilder}
	 * @return the difference between original order and the new
 	 * @throws BusinessException 
 	 * 			throws if one of the cases below
 	 * 			<li>the order to this id does NOT exist
	 * 	        <li>the order to this id is expired
	 * 			<li>the table of new order to update is BUSY
	 * 			<li>the staff has no privilege to add the food
	 * 			<li>the staff has no privilege to cancel the food
	 * 			<li>the staff has no privilege to present the food
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * 
	 * @see DiffResult
	 */
	private static DiffResult doPrepare(DBCon dbCon, Staff staff, Order.UpdateBuilder builder) throws BusinessException, SQLException{
		
		//Check to see whether the staff has the privilege to add the food 
		if(!staff.getRole().hasPrivilege(Privilege.Code.ADD_FOOD)){
			throw new BusinessException(StaffError.ORDER_NOT_ALLOW);
		}
		
		Order newOrder = builder.build();
		
		Order oriOrder = OrderDao.getById(dbCon, staff, newOrder.getId(), DateType.TODAY);
		
		newOrder.setDestTbl(oriOrder.getDestTbl());
		
		//Check to see whether the new order is expired.
		if(newOrder.getOrderDate() != 0 && newOrder.getOrderDate() < oriOrder.getOrderDate()){
			OrderFood of = OrderFoodDao.getSingleDetail(dbCon, staff, new OrderFoodDao.ExtraCond(DateType.TODAY).setOrder(newOrder.getId()), " ORDER BY OF.id DESC LIMIT 1 ").get(0);
			long deltaSeconds = (System.currentTimeMillis() - of.getOrderDate()) / 1000;
			throw new BusinessException("\"" + of.getWaiter() + "\"" + (deltaSeconds >= 60 ? ((deltaSeconds / 60) + "分钟") : (deltaSeconds + "秒")) + "前修改了账单, 请重新确认", FrontBusinessError.ORDER_EXPIRED);
		}
		
		//Fill the detail to each new order food
		List<OrderFood> newFoods = newOrder.getOrderFoods(); 
		for(OrderFood of : newFoods){
			OrderFoodDao.fill(dbCon, staff, of);
		}
		
		//Calculate the difference between the original and new order.
		DiffResult diffResult = diff(oriOrder, newOrder, staff);
		
		//Check to see whether the staff has privilege to cancel the food
		if(!diffResult.cancelledFoods.isEmpty() && !staff.getRole().hasPrivilege(Privilege.Code.CANCEL_FOOD)){
			throw new BusinessException(StaffError.CANCEL_FOOD_NOT_ALLOW);
		}
		
		return diffResult;
	}
	
	/**
	 * Prepare to update an order.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param newOrder
	 * @return the difference {@link DiffResult} between original order and the new
 	 * @throws BusinessException 
 	 * 			throws if the remaining to any limit food is insufficient 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static DiffResult doUpdate(DBCon dbCon, Staff staff, DiffResult diffResult, Order.UpdateBuilder builder) throws SQLException, BusinessException{
		
		String sql;
		
		//insert the extra order food records
		for(OrderFood extraFood : diffResult.extraFoods){
			OrderFoodDao.insertExtra(dbCon, staff, new OrderFoodDao.ExtraBuilder(diffResult.newOrder, extraFood).setPaid(!diffResult.oriOrder.isUnpaid()));
		}
		
		//insert the canceled order food records 
		for(OrderFood cancelledFood : diffResult.cancelledFoods){
			OrderFoodDao.insertCancelled(dbCon, staff, new OrderFoodDao.CancelBuilder(diffResult.newOrder, cancelledFood).setPaid(!diffResult.oriOrder.isUnpaid()));
		}
		
		/*
		 * Update the related info to this order.
		 * Note that update the region and table status only if the order is unpaid.
		 */
		sql = " UPDATE " + 
			  Params.dbName + ".order SET " +
			  " id = " + diffResult.newOrder.getId() + 
			  (builder.isCustomChanged() ? " ,custom_num = " + diffResult.newOrder.getCustomNum() : "") +
			  " ,category = " + diffResult.newOrder.getCategory().getVal() +
			  " ,order_date = NOW() " +
			  //" ,staff_id = " + staff.getId() + 
			  //" ,waiter = " + "'" + staff.getName() + "'" +
			  " ,temp_staff = NULL " +
			  " ,temp_date = NULL " +
			  " WHERE id = " + diffResult.newOrder.getId();
		dbCon.stmt.executeUpdate(sql);
		
		//Attach the associated weixin orders.
		for(WxOrder wxOrder: diffResult.newOrder.getWxOrders()){
			try{
				WxOrderDao.update(dbCon, staff, new WxOrder.AttachBuilder(wxOrder, diffResult.newOrder).asBuilder());
			}catch(BusinessException ignored){
				ignored.printStackTrace();
			}
		}
		return diffResult;
	}
	
	
	/**
	 * Compare the order foods of new order with the ones of original order,
	 * and the compared results would be handled according to the three cases below. 
	 * <1> Ori = New
	 * Means the order food is contained in both original and new order,
	 * Check to see if the difference count is greater or less than zero.
	 * Add to the extra if the difference is greater than zero, add to cancelled if the difference is less than zero.
	 * <2> Ori - New
	 * Means the order foods are only contained in the original order.
	 * Add these order foods to the cancelled.
	 * <3> New - Ori
	 * Means the order foods are only contained in the new order
	 * Add these order foods to the extra. 
	 * @param oriOrder	the original order
	 * @param newOrder	the new order
	 * @return the difference result
	 * @throws BusinessException 
	 */
	private static DiffResult diff(Order oriOrder, Order newOrder, Staff staff) throws BusinessException{
		final DiffResult result = new DiffResult(oriOrder, newOrder);

		final List<OrderFood> oriFoods = new ArrayList<OrderFood>(oriOrder.getOrderFoods());
		final List<OrderFood> newFoods = new ArrayList<OrderFood>(newOrder.getOrderFoods());
		
		/**
		 * Compare the order foods of new order with the ones of original order,
		 * and the compared results would be handled according to the three cases below. 
		 * <1> Ori = New
		 * Means the order food is contained in both original and new order,
		 * Check to see if the difference count is greater or less than zero.
		 * Add to the extra if the difference is greater than zero, add to be cancelled if the difference is less than zero.
		 * <2> Ori - New
		 * Means the order foods are only contained in the original order.
		 * Add these order foods would the cancelled.
		 * <3> New - Ori
		 * Means the order foods are only contained in the new order
		 * Add these order foods would the extra.
		 */
		Iterator<OrderFood> iterNew = newFoods.iterator();
		while(iterNew.hasNext()){
			OrderFood newFood = iterNew.next();
			
			if(newFood.isHurried()){
				result.hurriedFoods.add(newFood);
			}
			
			Iterator<OrderFood> iterOri = oriFoods.iterator();
			while(iterOri.hasNext()){
				OrderFood oriFood = iterOri.next();
				if(newFood.equals(oriFood)){
					float diff = newFood.getCount() - oriFood.getCount();
					if(diff > 0){
						newFood.setCount(oriFood.getCount());
						newFood.addCount(NumericUtil.roundFloat(diff));
						result.extraFoods.add(newFood);
						
					}else if(diff < 0){
						oriFood.setCount(oriFood.getCount());
						oriFood.removeCount(NumericUtil.roundFloat(Math.abs(diff)), staff);
						oriFood.setCancelReason(newFood.getCancelReason());
						if(newFood.getOperation() == OrderFood.Operation.GIFT || newFood.getOperation() == OrderFood.Operation.TRANSFER){
							oriFood.setOperation(newFood.getOperation());
						}else{
							oriFood.setOperation(OrderFood.Operation.CANCEL);
						}
						result.cancelledFoods.add(oriFood);
					}
					
					iterOri.remove();
					iterNew.remove();
					break;
				}
			}
			
		}
		
		for(OrderFood newExtraFood : newFoods){
			float count = newExtraFood.getCount();
			newExtraFood.setCount(0);
			newExtraFood.addCount(count);
			result.extraFoods.add(newExtraFood);
		}
		
		for(OrderFood cancelFood : oriFoods){
			cancelFood.removeCount(cancelFood.getCount(), staff);
			cancelFood.setOperation(OrderFood.Operation.CANCEL);
			result.cancelledFoods.add(cancelFood);
		}
		
		return result;
	}
}


