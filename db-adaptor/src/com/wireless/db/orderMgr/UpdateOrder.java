package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.exception.ProtocolError;
import com.wireless.exception.StaffError;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.TasteGroup;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.DateType;

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
	 * Update the order in a db transition according to the specific order id. 
	 * 
	 * @param terminal 
	 * 			the terminal
	 * @param newOrder
	 *            the order along with the order id and other detail information
	 * 
	 * @return The update result containing two orders below.<br>
	 *         - The extra order.<br>
	 *         - The canceled order.
	 * @throws BusinessException
	 *             throws if one of the cases below
	 *             <li>the terminal is NOT attached to any restaurant
	 *             <li>the terminal is expired
	 *             <li>the order to this id does NOT exist
	 *             <li>the order to this id is expired
	 *             <li>any food to this order does NOT exist
	 *             <li>any taste to this order does NOT exist
	 *             <li>exceed the gift quota
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static DiffResult execById(Staff staff, Order orderToUpdate) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();	
		
		try{
			dbCon.connect();
			return execById(dbCon, staff, orderToUpdate);

		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the order according to the specific order id. Note that the method
	 * should be invoked before database connected.
	 * 
	 * @param terminal 
	 * 			the terminal
	 * @param newOrder
	 *            the order along with the order id and other detail information
	 * 
	 * @return the update result containing two orders below
	 *         <li>the extra order
	 *         <li>the canceled order
	 * @throws BusinessException
	 *             throws if one of the cases below<br>
	 *             <li>the terminal is NOT attached to any restaurant
	 *             <li>the terminal is expired
	 *             <li>the order to this id does NOT exist
	 *             <li>the order to this id is expired
	 *             <li>any food to this order does NOT exist
	 *             <li>any taste to this order does NOT exist
	 *             <li>exceed the gift quota
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static DiffResult execById(DBCon dbCon, Staff staff, Order newOrder) throws BusinessException, SQLException{
		
		boolean isAutoCommit = dbCon.conn.getAutoCommit();
		
		try{
			dbCon.conn.setAutoCommit(false);
			
			DiffResult diffResult = doUpdate(dbCon, staff, doPrepare(dbCon, staff, newOrder));
			
			dbCon.conn.commit();
			
			return diffResult;
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.conn.setAutoCommit(isAutoCommit);
		}
		
	}
	
	/**
	 * Update an order according to a specific order id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param newOrder
	 * 			The order to update, at least along with order id & table.
	 * @return the difference between original order and the new
 	 * @throws BusinessException 
 	 * 			throws if one of the cases below
 	 * 			<li>the order to this id does NOT exist
	 * 	        <li>the order to this id is expired
	 * 			<li>the table of new order to update is BUSY
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * 
	 * @see DiffResult
	 */
	public static DiffResult execByIdAsync(DBCon dbCon, Staff staff, Order newOrder) throws BusinessException, SQLException{
		return doUpdate(dbCon, staff, doPrepare(dbCon, staff, newOrder));
	}
	
	/**
	 * Prepare to calculate the difference between new order and the original, which is used in {@link doUpdate}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param newOrder
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
	private static DiffResult doPrepare(DBCon dbCon, Staff staff, Order newOrder) throws BusinessException, SQLException{
		
		//Check to see whether the staff has the privilege to add the food 
		if(!staff.getRole().hasPrivilege(Privilege.Code.ADD_FOOD)){
			throw new BusinessException(StaffError.ORDER_NOT_ALLOW);
		}
		
		Order oriOrder = OrderDao.getById(dbCon, staff, newOrder.getId(), DateType.TODAY);
		
		newOrder.setDestTbl(TableDao.getTableByAlias(dbCon, staff, newOrder.getDestTbl().getAliasId()));
		
		/*
		 * If the order to update is unpaid and the table to original order is different from the new.
		 * Assure the table of new order is idle since need to switch the unpaid order to this new table. 
		 */
		if(oriOrder.isUnpaid() && !oriOrder.getDestTbl().equals(newOrder.getDestTbl())){
			if(!newOrder.getDestTbl().isIdle()){
				throw new BusinessException(newOrder.getDestTbl().getAliasId() + "号台是就餐状态，不能转台", ProtocolError.TABLE_BUSY);
			}
		}
		
		//Check to see whether the new order is expired.
		if(newOrder.getOrderDate() != 0 && newOrder.getOrderDate() < oriOrder.getOrderDate()){
			OrderFood of = OrderFoodDao.getSingleDetail(dbCon, staff, new OrderFoodDao.ExtraCond(DateType.TODAY).setOrderId(newOrder.getId()), " ORDER BY OF.id DESC LIMIT 1 ").get(0);
			long deltaSeconds = (System.currentTimeMillis() - of.getOrderDate()) / 1000;
			throw new BusinessException("\"" + of.getWaiter() + "\"" + (deltaSeconds >= 60 ? ((deltaSeconds / 60) + "分钟") : (deltaSeconds + "秒")) + "前修改了账单, 请重新确认", FrontBusinessError.ORDER_EXPIRED);
		}
		
		//Fill the detail to each new order food
		List<OrderFood> newFoods = newOrder.getOrderFoods(); 
		for(OrderFood of : newFoods){
			OrderFoodDao.fill(dbCon, staff, of);
			//fillFoodDetail(dbCon, staff, of);
		}
		
		//Set the default discount to new order if original order is unpaid
//		if(oriOrder.isUnpaid()){
//			newOrder.setDiscount(DiscountDao.getDefault(dbCon, staff));
//		}
		
		//Calculate the difference between the original and new order.
		DiffResult diffResult = diff(oriOrder, newOrder);
		
		if(!diffResult.cancelledFoods.isEmpty() && !staff.getRole().hasPrivilege(Privilege.Code.CANCEL_FOOD)){
			//Check to see whether the staff has privilege to cancel the food
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
	 * @return the difference between original order and the new
 	 * @throws BusinessException 
 	 * 			throws if one of the cases below
 	 * 			<li>the order to this id does NOT exist
	 * 	        <li>the order to this id is expired
	 * 			<li>the table of new order to update is BUSY
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * 
	 * @see DiffResult
	 */
	private static DiffResult doUpdate(DBCon dbCon, Staff staff, DiffResult diffResult) throws SQLException{
		
		String sql;
		
		//insert the extra order food records
		for(OrderFood extraFood : diffResult.extraFoods){

			OrderFoodDao.insertExtra(dbCon, staff, new OrderFoodDao.ExtraBuilder(diffResult.newOrder.getId(), extraFood).setPaid(diffResult.oriOrder.isUnpaid()));
			
			//FIXME Insert the temporary food to menu.
//			if(extraFood.isTemp()){
//				try{
//					FoodDao.insert(dbCon, staff, new Food.InsertBuilder(extraFood.getName(), extraFood.getPrice(), extraFood.getKitchen()).setTemp(true));
//				}catch(BusinessException ingored){}
//			}
		}
		
		//insert the canceled order food records 
		for(OrderFood cancelledFood : diffResult.cancelledFoods){

			sql = " INSERT INTO `" + Params.dbName + "`.`order_food` " +
				  " ( " +
				  " `restaurant_id`, `order_id`, `food_id`, `order_count`, `unit_price`, `commission`, `name`, `food_status`, " +
				  " `discount`, `taste_group_id`, `cancel_reason_id`, `cancel_reason`, " +
				  " `dept_id`, `kitchen_id`, " +
				  " `staff_id`, `waiter`, `order_date`, `is_temporary`, `is_paid`, `is_gift`) VALUES (" +
				  staff.getRestaurantId() + ", " +
				  diffResult.newOrder.getId() + ", " +
				  cancelledFood.getFoodId() + ", " +
				  "-" + cancelledFood.getCount() + ", " + 
				  cancelledFood.asFood().getPrice() + ", " + 
				  cancelledFood.asFood().getCommission() + "," +
				  "'" + cancelledFood.getName() + "', " + 
				  cancelledFood.asFood().getStatus() + ", " +
				  cancelledFood.getDiscount() + ", " +
				  (cancelledFood.hasTasteGroup() ? cancelledFood.getTasteGroup().getGroupId() : TasteGroup.EMPTY_TASTE_GROUP_ID) + ", " +
				  (cancelledFood.hasCancelReason() ? cancelledFood.getCancelReason().getId() : CancelReason.NO_REASON) + ", " +
				  (cancelledFood.hasCancelReason() ? "'" + cancelledFood.getCancelReason().getReason() + "'" : "NULL") + ", " +
				  cancelledFood.getKitchen().getDept().getId() + ", " +
				  cancelledFood.getKitchen().getId() + ", " +
				  staff.getId() + ", " +
				  "'" + staff.getName() + "', " +
				  "NOW(), " + 
				  (cancelledFood.isTemp() ? 1 : 0) + ", " +
				  (diffResult.oriOrder.isUnpaid() ? 0 : 1) + ", " +
				  (cancelledFood.isGift()? 1 : 0 ) +
				  " ) ";
			dbCon.stmt.executeUpdate(sql);			
		}
		
		/*
		 * Update the related info to this order.
		 * Note that update the region and table status only if the order is unpaid.
		 */
		sql = " UPDATE " + 
			  Params.dbName + ".order SET " +
			  " custom_num = " + diffResult.newOrder.getCustomNum() + ", " +
			  " category = " + diffResult.newOrder.getCategory().getVal() + ", " +
			  " order_date = NOW(), " +
			  " staff_id = " + staff.getId() + ", " +
			  " waiter = " + "'" + staff.getName() + "' " +
			  " WHERE " +
			  " id = " + diffResult.newOrder.getId();
		dbCon.stmt.executeUpdate(sql);
		
		
		if(diffResult.oriOrder.isUnpaid()){
			
			//Update the region and table status only if the order is unpaid.
			sql = " UPDATE " + 
				  Params.dbName + ".order SET " +
				  " region_id = " + diffResult.newOrder.getRegion().getId() + ", " +
				  " region_name = '" + diffResult.newOrder.getRegion().getName() + "', " +
				  " table_id = " + diffResult.newOrder.getDestTbl().getTableId() + ", " +
				  " table_alias = " + diffResult.newOrder.getDestTbl().getAliasId() + ", " +
				  " table_name = '" + diffResult.newOrder.getDestTbl().getName() + "' " +
				  " WHERE id = " + diffResult.newOrder.getId();
			dbCon.stmt.executeUpdate(sql);
			
			// Update the new table status only if the order is unpaid.
			sql = " UPDATE " + 
				  Params.dbName + ".table SET " +
			      " status = " + Table.Status.BUSY.getVal() + "," +
				  " category = " + diffResult.newOrder.getCategory().getVal() + "," +
				  " custom_num = " + diffResult.newOrder.getCustomNum() +
				  " WHERE " +
				  " table_id = " + diffResult.newOrder.getDestTbl().getTableId();
			dbCon.stmt.executeUpdate(sql);	
			
			// Update the original table status to idle if new table is different from the original.
			if(!diffResult.newOrder.getDestTbl().equals(diffResult.oriOrder.getDestTbl())){

				sql = " UPDATE " + 
					  Params.dbName + ".table SET " +
					  " status = " + Table.Status.IDLE.getVal() + "," + 
					  " custom_num = NULL, " +
					  " category = NULL " + 
					  " WHERE " +
					  " table_id = " + diffResult.oriOrder.getDestTbl().getTableId();
				dbCon.stmt.executeUpdate(sql);				
				
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
	 */
	private static DiffResult diff(Order oriOrder, Order newOrder){
		DiffResult result = new DiffResult(oriOrder, newOrder);

		List<OrderFood> oriFoods = new ArrayList<OrderFood>(oriOrder.getOrderFoods());
		List<OrderFood> newFoods = new ArrayList<OrderFood>(newOrder.getOrderFoods());
		
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
						oriFood.setCount(NumericUtil.roundFloat(Math.abs(diff)));
						result.extraFoods.add(oriFood);
						
					}else if(diff < 0){
						oriFood.setCount(NumericUtil.roundFloat(Math.abs(diff)));
						oriFood.setCancelReason(newFood.getCancelReason());
						result.cancelledFoods.add(oriFood);
					}
					
					iterOri.remove();
					iterNew.remove();
					break;
				}
			}
			
		}
		
		for(OrderFood newExtraFood : newFoods){
			result.extraFoods.add(newExtraFood);
		}
		//result.extraFoods.addAll(newFoods);		
		result.cancelledFoods.addAll(oriFoods);
		
		return result;
	}
}


