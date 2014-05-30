package com.wireless.db.frontBusiness;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.OrderFoodDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.exception.ProtocolError;
import com.wireless.exception.StaffError;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteGroup;
import com.wireless.util.DateType;

public class UpdateOrder {
	
	/**
	 * The difference result between two orders, which consists of several things below.
	 * 1 - The extra foods more than the original.
	 * 2 - The cancelled foods less than the original.
	 * 3 - The hurried foods in the new.
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
			OrderFood of = OrderFoodDao.getSingleDetailToday(dbCon, staff, " AND OF.order_id = " + newOrder.getId(), " ORDER BY OF.id DESC LIMIT 1 ").get(0);
			long deltaSeconds = (System.currentTimeMillis() - of.getOrderDate()) / 1000;
			throw new BusinessException("\"" + of.getWaiter() + "\"" + (deltaSeconds >= 60 ? ((deltaSeconds / 60) + "分钟") : (deltaSeconds + "秒")) + "前修改了账单, 请重新确认", FrontBusinessError.ORDER_EXPIRED);
		}
		
		//Fill the detail to each new order food
		List<OrderFood> newFoods = newOrder.getOrderFoods(); 
		for(OrderFood of : newFoods){
			fillFoodDetail(dbCon, staff, of);
		}
		
		//Set the default discount to new order if original order is unpaid
		if(oriOrder.isUnpaid()){
			newOrder.setDiscount(DiscountDao.getDefault(dbCon, staff));
		}
		
		//Calculate the difference between the original and new order.
		DiffResult diffResult = diff(oriOrder, newOrder);
		
		//Check to see whether the staff has privilege to cancel the food
		if(!diffResult.cancelledFoods.isEmpty() && !staff.getRole().hasPrivilege(Privilege.Code.CANCEL_FOOD)){
			throw new BusinessException(StaffError.CANCEL_FOOD_NOT_ALLOW);
			
		}else if(!diffResult.extraFoods.isEmpty()){
			//Check to see whether the staff has privilege to present the food
			for(OrderFood extraFood : diffResult.extraFoods){
				if(extraFood.asFood().isGift() && !staff.getRole().hasPrivilege(Privilege.Code.GIFT)){
					throw new BusinessException(StaffError.GIFT_NOT_ALLOW);
				}
			}
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

			/**
			 * Insert the taste group info if containing taste and the extra taste group is new
			 */
			if(extraFood.hasTasteGroup() && extraFood.getTasteGroup().getGroupId() == TasteGroup.NEW_TASTE_GROUP_ID){
				
				TasteGroup tg = extraFood.getTasteGroup();					
				/**
				 * Insert the taste group if containing taste.
				 */
				sql = " INSERT INTO " + Params.dbName + ".taste_group " +
					  " ( " +
					  " `normal_taste_group_id`, `normal_taste_pref`, `normal_taste_price`, " +
					  " `tmp_taste_id`, `tmp_taste_pref`, `tmp_taste_price` " +
					  " ) " +
					  " SELECT " +
					  (tg.hasNormalTaste() ? "MAX(normal_taste_group_id) + 1" : TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID) + ", " +
					  (tg.hasNormalTaste() ? ("'" + tg.getNormalTastePref() + "'") : "NULL") + ", " +
					  (tg.hasNormalTaste() ? tg.getNormalTastePrice() : "NULL") + ", " +
					  (tg.hasTmpTaste() ? tg.getTmpTaste().getTasteId() : "NULL") + ", " +
					  (tg.hasTmpTaste() ? ("'" + tg.getTmpTastePref() + "'") : "NULL") + ", " +
					  (tg.hasTmpTaste() ? tg.getTmpTastePrice() : "NULL") +
					  " FROM " + 
					  Params.dbName + ".taste_group" +
					  " LIMIT 1 ";
				dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
				//get the generated id to taste group 
				dbCon.rs = dbCon.stmt.getGeneratedKeys();
				if(dbCon.rs.next()){
					tg.setGroupId(dbCon.rs.getInt(1));
				}else{
					throw new SQLException("The id of taste group is not generated successfully.");
				}
				
				/**
				 * Insert the normal taste group if containing normal tastes.
				 */
				if(tg.hasNormalTaste()){
					for(Taste normalTaste : tg.getNormalTastes()){
						sql = " INSERT INTO " + Params.dbName + ".normal_taste_group " +
							  " ( " +
							  " `normal_taste_group_id`, `taste_id` " +
							  " ) " +
							  " VALUES " +
							  " ( " +
							  " (SELECT normal_taste_group_id FROM " + Params.dbName + ".taste_group " + 
							  " WHERE " +
							  " taste_group_id = " + tg.getGroupId() + ")" + " , " +
							  normalTaste.getTasteId() + 
							  " ) ";
						dbCon.stmt.executeUpdate(sql);
					}
				}
			}
			
			sql = " INSERT INTO " + Params.dbName + ".order_food " +
				  " ( " + 
				  " `restaurant_id`, `order_id`, `food_id`, `order_count`, `unit_price`, `commission`, `name`, `food_status`, " +
				  " `discount`, `taste_group_id`, " +
				  " `dept_id`, `kitchen_id`, " +
				  " `staff_id`, `waiter`, `order_date`, `is_temporary`, `is_paid` " +
				  " ) " +
				  " VALUES " +
				  "(" +
				  staff.getRestaurantId() + ", " +
				  diffResult.newOrder.getId() + ", " +
				  extraFood.getFoodId() + ", " +
				  extraFood.getCount() + ", " + 
				  extraFood.getPrice() + ", " + 
				  extraFood.asFood().getCommission() + ", " +
				  "'" + extraFood.getName() + "', " + 
				  extraFood.asFood().getStatus() + ", " +
				  extraFood.getDiscount() + ", " +
				  (extraFood.hasTasteGroup() ? extraFood.getTasteGroup().getGroupId() : TasteGroup.EMPTY_TASTE_GROUP_ID) + ", " +
				  extraFood.getKitchen().getDept().getId() + ", " +
				  extraFood.getKitchen().getId() + ", " +
				  staff.getId() + ", " +
				  "'" + staff.getName() + "', " +
				  "NOW(), " + 
				  (extraFood.isTemp() ? 1 : 0) + ", " +
				  (diffResult.oriOrder.isUnpaid() ? 0 : 1) +
				  " ) ";
			dbCon.stmt.executeUpdate(sql);		
			
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
				  " `staff_id`, `waiter`, `order_date`, `is_temporary`, `is_paid`) VALUES (" +
				  staff.getRestaurantId() + ", " +
				  diffResult.newOrder.getId() + ", " +
				  cancelledFood.getFoodId() + ", " +
				  "-" + cancelledFood.getCount() + ", " + 
				  cancelledFood.getPrice() + ", " + 
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
				  (diffResult.oriOrder.isUnpaid() ? 0 : 1) +
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
				  " region_id = " + diffResult.newOrder.getRegion().getRegionId() + ", " +
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
	 * Fill the detail to food.
	 * The basic information consists of alias id, discount, taste id, hang status and so on.
	 * @param dbCon
	 * 			The db connection
	 * @param staff
	 * 			The terminal associated with this request
	 * @param foodToFill
	 * 			The food instance with the basic information
	 * @return 
	 * 			The food instance with the detail information
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the associated cancel reason does NOT exist
	 * 			<li>the associated kitchen does NOT exist
	 * 			<li>the associated food does NOT exist
	 * 			<li>the associated taste does NOT exist
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	private static void fillFoodDetail(DBCon dbCon, Staff staff, OrderFood foodToFill) throws BusinessException, SQLException{
		
		//Get the details to cancel reason if contained.
		if(foodToFill.hasCancelReason()){
			foodToFill.setCancelReason(CancelReasonDao.getReasonById(dbCon, staff, foodToFill.getCancelReason().getId()));
		}
		
		if(foodToFill.isTemp()){
			// Get the associated kitchen detail in case of temporary.
			foodToFill.asFood().setKitchen(KitchenDao.getById(dbCon, staff, foodToFill.getKitchen().getId()));
			
		}else{
			//Get the details to each order food			
			foodToFill.asFood().copyFrom(FoodDao.getById(dbCon, staff, foodToFill.getFoodId()));
			
			//Get the details to each normal tastes.
			if(foodToFill.hasNormalTaste()){
				//Get the detail to each tastes.
				for(Taste taste : foodToFill.getTasteGroup().getTastes()){
					taste.copyFrom(TasteDao.getTasteById(dbCon, staff, taste.getTasteId()));
				}
				
				//Get the detail to each spec.
				if(foodToFill.getTasteGroup().hasSpec()){
					foodToFill.getTasteGroup().getSpec().copyFrom(TasteDao.getTasteById(dbCon, staff, foodToFill.getTasteGroup().getSpec().getTasteId()));
				}
			}			
	
		}		
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
						oriFood.setCount((float)Math.round(Math.abs(diff) * 100) / 100);
						result.extraFoods.add(oriFood);
						
					}else if(diff < 0){
						oriFood.setCount((float)Math.round(Math.abs(diff) * 100) / 100);
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
			if(newExtraFood.hasTasteGroup()){
				newExtraFood.getTasteGroup().setGroupId(TasteGroup.NEW_TASTE_GROUP_ID);
			}
			result.extraFoods.add(newExtraFood);
		}
		//result.extraFoods.addAll(newFoods);		
		result.cancelledFoods.addAll(oriFoods);
		
		return result;
	}
}


