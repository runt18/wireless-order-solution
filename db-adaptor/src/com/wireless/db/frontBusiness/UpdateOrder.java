package com.wireless.db.frontBusiness;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.TasteGroup;
import com.wireless.protocol.Terminal;

public class UpdateOrder {
	
	/**
	 * The difference result between two orders, which consists of several things below.
	 * 1 - The extra foods more than the original.
	 * 2 - The cancelled foods less than the original.
	 * 3 - The hurried foods in the new.
	 */
	public static class DiffResult{
		public Order oriOrder;
		public Order newOrder;
		public List<OrderFood> extraFoods;
		public List<OrderFood> cancelledFoods;
		public List<OrderFood> hurriedFoods;
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
	 *             Throws if one of the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The order to this id does NOT exist.<br>
	 *             - The order to this id is expired<br>.
	 *             - Any food to this order does NOT exist.<br>
	 *             - Any taste to this order does NOT exist.<br>
	 *             - Exceed the gift quota.
	 * @throws SQLException
	 *             Throws if fail to execute any SQL statement.
	 */
	public static DiffResult execByID(Terminal term, Order orderToUpdate) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();	
		
		try{
			dbCon.connect();
			return execByID(dbCon, term, orderToUpdate);

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
	 * @return The update result containing two orders below.<br>
	 *         - The extra order.<br>
	 *         - The canceled order.
	 * @throws BusinessException
	 *             Throws if one of the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The order to this id does NOT exist.<br>
	 *             - The order to this id is expired<br>.
	 *             - Any food to this order does NOT exist.<br>
	 *             - Any taste to this order does NOT exist.<br>
	 *             - Exceed the gift quota.
	 * @throws SQLException
	 *             Throws if fail to execute any SQL statement.
	 */
	public static DiffResult execByID(DBCon dbCon, Terminal term, Order newOrder) throws BusinessException, SQLException{
		
		boolean isAutoCommit = dbCon.conn.getAutoCommit();
		
		try{
			dbCon.conn.setAutoCommit(false);
			
			DiffResult diffResult = doUpdate(dbCon, term, doPrepare(dbCon, term, newOrder));
			
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
	 * Note that the method does NOT run in db transition.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param newOrder
	 * 			The order to update, at least along with order id & table.
	 * @return the difference between original order and the new
 	 * @throws BusinessException 
 	 * 			Throws if one of the cases below.<br>
 	 * 			- The order to this id does NOT exist.<br>
	 * 	        - The order to this id is expired.<br>
	 * 			- The table of new order to update is BUSY.<br>
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 * 
	 * @see DiffResult
	 */
	public static DiffResult execByIdAsync(DBCon dbCon, Terminal term, Order newOrder) throws BusinessException, SQLException{
		return doUpdate(dbCon, term, doPrepare(dbCon, term, newOrder));
	}
	
	/**
	 * Prepare to calculate the difference between new order and the original, which is used in {@link doUpdate}
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param newOrder
	 * @return the difference between original order and the new
 	 * @throws BusinessException 
 	 * 			Throws if one of the cases below.<br>
 	 * 			- The order to this id does NOT exist.<br>
	 * 	        - The order to this id is expired.<br>
	 * 			- The table of new order to update is BUSY.<br>
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 * 
	 * @see DiffResult
	 */
	private static DiffResult doPrepare(DBCon dbCon, Terminal term, Order newOrder) throws BusinessException, SQLException{
		
		Order oriOrder = QueryOrderDao.execByID(dbCon, newOrder.getId(), QueryOrderDao.QUERY_TODAY);
		
		newOrder.setDestTbl(TableDao.getTableByAlias(dbCon, term, newOrder.getDestTbl().getAliasId()));
		
		/*
		 * If the order to update is unpaid and the table to original order is different from the new.
		 * Assure the table of new order is idle since need to switch the unpaid order to this new table. 
		 */
		if(oriOrder.isUnpaid() && !oriOrder.getDestTbl().equals(newOrder.getDestTbl())){
			if(!newOrder.getDestTbl().isIdle()){
				throw new BusinessException("The new " + newOrder.getDestTbl() + " of order(id = " + newOrder.getId() + ") to update should be Idle.", ProtocolError.TABLE_BUSY);
			}
		}
		
		//Check to see whether the new order is expired.
		if(newOrder.getOrderDate() != 0 && newOrder.getOrderDate() < oriOrder.getOrderDate()){
			throw new BusinessException("The order(order_id=" + newOrder.getId() + ",restaurant_id=" + term.restaurantID + ") has expired.", ProtocolError.ORDER_EXPIRED);
		}
		
		//Fill the detail to each new order food
		OrderFood[] newFoods = newOrder.getOrderFoods(); 
		for(int i = 0; i < newFoods.length; i++){
			fillFoodDetail(dbCon, term, newFoods[i]);
		}
		
		//Get the region detail associated with the new order.
		//newOrder.setRegion(QueryRegion.execByTbl(dbCon, term, newOrder.getDestTbl().getAliasId()));
		
		//Calculate the difference between the original and new order.
		return diff(oriOrder, newOrder);
	}
	
	/**
	 * Prepare to update an order.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param newOrder
	 * @return the difference between original order and the new
 	 * @throws BusinessException 
 	 * 			Throws if one of the cases below.<br>
 	 * 			- The order to this id does NOT exist.<br>
	 * 	        - The order to this id is expired.<br>
	 * 			- The table of new order to update is BUSY.<br>
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 * 
	 * @see DiffResult
	 */
	private static DiffResult doUpdate(DBCon dbCon, Terminal term, DiffResult diffResult) throws SQLException{
		
		String sql;
		
		//insert the extra order food records
		for(OrderFood extraFood : diffResult.extraFoods){

			/**
			 * Insert the taste group info if containing taste and the extra taste group is new
			 */
			if(extraFood.hasTaste() && extraFood.getTasteGroup().getGroupId() == TasteGroup.NEW_TASTE_GROUP_ID){
				
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
					  (tg.hasTmpTaste() ? tg.getTmpTaste().getAliasId() : "NULL") + ", " +
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
				  " `restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_count`, `unit_price`, `name`, `food_status`, " +
				  " `discount`, `taste_group_id`, " +
				  " `dept_id`, `kitchen_id`, `kitchen_alias`, `waiter`, `order_date`, `is_temporary`, `is_paid` " +
				  " ) " +
				  " VALUES " +
				  "(" +
				  term.restaurantID + ", " +
				  diffResult.newOrder.getId() + ", " +
				  (extraFood.getFoodId() == 0 ? "NULL" : extraFood.getFoodId()) + ", " +
				  extraFood.getAliasId() + ", " + 
				  extraFood.getCount() + ", " + 
				  extraFood.getPrice() + ", '" + 
				  extraFood.getName() + "', " + 
				  extraFood.getStatus() + ", " +
				  extraFood.getDiscount() + ", " +
				  (extraFood.hasTaste() ? extraFood.getTasteGroup().getGroupId() : TasteGroup.EMPTY_TASTE_GROUP_ID) + ", " +
				  extraFood.getKitchen().getDept().getId() + ", " +
				  extraFood.getKitchen().getId() + ", " +
				  extraFood.getKitchen().getAliasId() + ", '" + 
				  term.owner + "', " +
				  "NOW(), " + 
				  (extraFood.isTemp() ? 1 : 0) + ", " +
				  (diffResult.oriOrder.isUnpaid() ? 0 : 1) +
				  " ) ";
			dbCon.stmt.executeUpdate(sql);			
		}
		
		//insert the canceled order food records 
		for(OrderFood cancelledFood : diffResult.cancelledFoods){

			sql = " INSERT INTO `" + Params.dbName + "`.`order_food` " +
				  " ( " +
				  " `restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_count`, `unit_price`, `name`, `food_status`, " +
				  " `discount`, `taste_group_id`, `cancel_reason_id`, `cancel_reason`, " +
				  " `dept_id`, `kitchen_id`, `kitchen_alias`, " +
				  " `waiter`, `order_date`, `is_temporary`, `is_paid`) VALUES (" +
				  term.restaurantID + ", " +
				  diffResult.newOrder.getId() + ", " +
				  (cancelledFood.getFoodId() == 0 ? "NULL" : cancelledFood.getFoodId()) + ", " +
				  cancelledFood.getAliasId() + ", " + 
				  "-" + cancelledFood.getCount() + ", " + 
				  cancelledFood.getPrice() + ", " + 
				  "'" + cancelledFood.getName() + "', " + 
				  cancelledFood.getStatus() + ", " +
				  cancelledFood.getDiscount() + ", " +
				  (cancelledFood.hasTaste() ? cancelledFood.getTasteGroup().getGroupId() : TasteGroup.EMPTY_TASTE_GROUP_ID) + ", " +
				  (cancelledFood.hasCancelReason() ? cancelledFood.getCancelReason().getId() : CancelReason.NO_REASON) + ", " +
				  (cancelledFood.hasCancelReason() ? "'" + cancelledFood.getCancelReason().getReason() + "'" : "NULL") + ", " +
				  cancelledFood.getKitchen().getDept().getId() + ", " +
				  cancelledFood.getKitchen().getId() + ", " +
				  cancelledFood.getKitchen().getAliasId() + ", " + 
				  "'" + term.owner + "', " +
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
			  " category = " + diffResult.newOrder.getCategory() + ", " +
			  " order_date = NOW(), " +
//			  " discount_id = " + diffResult.newOrder.getDiscount().getId() + ", " +
//			  (diffResult.oriOrder.isUnpaid() ? "" : "region_id = " + newOrder.getRegion().getRegionId() + ", ") +
//			  (diffResult.oriOrder.isUnpaid() ? "" : "region_name = '" + newOrder.getRegion().getName() + "', ") +
//			  (diffResult.oriOrder.isUnpaid() ? "" : "table_id = " + newOrder.getDestTbl().getTableId() + ", ") +
//			  (diffResult.oriOrder.isUnpaid() ? "" : "table_alias = " + newOrder.getDestTbl().getAliasId() + ", ") +
//			  (diffResult.oriOrder.isUnpaid() ? "" : "table_name = '" + newOrder.getDestTbl().getName() + "', ") +
			  " terminal_pin = " + term.pin + ", " +
			  " waiter = " + "'" + term.owner + "' " +
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
				  " category = " + diffResult.newOrder.getCategory() + "," +
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
	 * 
	 * @param dbCon
	 * @param term
	 * @param oriOrder
	 * @param newOrder
	 * @param isPaidAgain
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
//	private static DiffResult updateOrder(DBCon dbCon, Terminal term, Order oriOrder, Order newOrder, boolean isPaidAgain) throws BusinessException, SQLException{		
//		
//		//Throws exception if the new order is expired.
//		if(newOrder.getOrderDate() != 0 && newOrder.getOrderDate() < oriOrder.getOrderDate()){
//			throw new BusinessException("The order(order_id=" + newOrder.getId() + ",restaurant_id=" + term.restaurantID + ") has expired.", ErrorCode.ORDER_EXPIRED);
//		}
//		
//		List<OrderFood> extraFoods;
//		List<OrderFood> cancelledFoods;
//
//		//Get the detail to each order foods of new order.
//		List<OrderFood> newFoods = new ArrayList<OrderFood>(newOrder.getOrderFoods().length);
//		for(OrderFood newFood : newOrder.getOrderFoods()){
//			//Skip the food whose count is less than zero.
//			//if(newFood.getCount() > 0){
//				fillFoodDetail(dbCon, term, newFood);
//				newFoods.add(newFood);
//			//}
//		}
//		newOrder.setOrderFoods(newFoods.toArray(new OrderFood[newFoods.size()]));
//		
//		//Get the difference between the original and new order.
//		OrderDiff.DiffResult diffResult = OrderDiff.diff(oriOrder, newOrder);
//		extraFoods = diffResult.extraFoods;
//		cancelledFoods = diffResult.cancelledFoods;
//		//hurriedFoods = diffResult.hurriedFoods;
//		
//		/**
//		 * Get the region to this table if the order has NOT been paid before
//		 */
//		if(!isPaidAgain){
//			newOrder.setRegion(QueryRegion.execByTbl(dbCon, term, newOrder.getDestTbl().getAliasId()));
//		}
//		
//		try{
//		
//			String sql;
//			float giftAmount = 0;
//			
//			dbCon.conn.setAutoCommit(false);
//			
//			//insert the extra order food records
//			for(OrderFood extraFood : extraFoods){
//
//				//add the gift amount if extra foods
//				if(extraFood.isGift()){
//					giftAmount += extraFood.getUnitPriceWithTaste() * extraFood.getCount();
//				}
//				
//				/**
//				 * Insert the taste group info if containing taste and the extra taste group is new
//				 */
//				if(extraFood.hasTaste() && extraFood.getTasteGroup().getGroupId() == TasteGroup.NEW_TASTE_GROUP_ID){
//					
//					TasteGroup tg = extraFood.getTasteGroup();					
//					/**
//					 * Insert the taste group if containing taste.
//					 */
//					sql = " INSERT INTO " + Params.dbName + ".taste_group " +
//						  " ( " +
//						  " `normal_taste_group_id`, `normal_taste_pref`, `normal_taste_price`, " +
//						  " `tmp_taste_id`, `tmp_taste_pref`, `tmp_taste_price` " +
//						  " ) " +
//						  " SELECT " +
//						  (tg.hasNormalTaste() ? "MAX(normal_taste_group_id) + 1" : TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID) + ", " +
//						  (tg.hasNormalTaste() ? ("'" + tg.getNormalTastePref() + "'") : "NULL") + ", " +
//						  (tg.hasNormalTaste() ? tg.getNormalTastePrice() : "NULL") + ", " +
//						  (tg.hasTmpTaste() ? tg.getTmpTaste().getAliasId() : "NULL") + ", " +
//						  (tg.hasTmpTaste() ? ("'" + tg.getTmpTastePref() + "'") : "NULL") + ", " +
//						  (tg.hasTmpTaste() ? tg.getTmpTastePrice() : "NULL") +
//						  " FROM " + 
//						  Params.dbName + ".taste_group" +
//						  " LIMIT 1 ";
//					dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
//					//get the generated id to taste group 
//					dbCon.rs = dbCon.stmt.getGeneratedKeys();
//					if(dbCon.rs.next()){
//						tg.setGroupId(dbCon.rs.getInt(1));
//					}else{
//						throw new SQLException("The id of taste group is not generated successfully.");
//					}
//					
//					/**
//					 * Insert the normal taste group if containing normal tastes.
//					 */
//					if(tg.hasNormalTaste()){
//						for(Taste normalTaste : tg.getNormalTastes()){
//							sql = " INSERT INTO " + Params.dbName + ".normal_taste_group " +
//								  " ( " +
//								  " `normal_taste_group_id`, `taste_id` " +
//								  " ) " +
//								  " VALUES " +
//								  " ( " +
//								  " (SELECT normal_taste_group_id FROM " + Params.dbName + ".taste_group " + 
//								  " WHERE " +
//								  " taste_group_id = " + tg.getGroupId() + ")" + " , " +
//								  normalTaste.getTasteId() + 
//								  " ) ";
//							dbCon.stmt.executeUpdate(sql);
//						}
//					}
//				}
//				
//				sql = " INSERT INTO " + Params.dbName + ".order_food " +
//					  " ( " + 
//					  " `restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_count`, `unit_price`, `name`, `food_status`, " +
//					  " `discount`, `taste_group_id`, " +
//					  " `dept_id`, `kitchen_id`, `kitchen_alias`, `waiter`, `order_date`, `is_temporary`, `is_paid` " +
//					  " ) " +
//					  " VALUES " +
//					  "(" +
//					  term.restaurantID + ", " +
//					  newOrder.getId() + ", " +
//					  (extraFood.getFoodId() == 0 ? "NULL" : extraFood.getFoodId()) + ", " +
//					  extraFood.getAliasId() + ", " + 
//					  extraFood.getCount() + ", " + 
//					  extraFood.getPrice() + ", '" + 
//					  extraFood.getName() + "', " + 
//					  extraFood.getStatus() + ", " +
//					  extraFood.getDiscount() + ", " +
//					  (extraFood.hasTaste() ? extraFood.getTasteGroup().getGroupId() : TasteGroup.EMPTY_TASTE_GROUP_ID) + ", " +
//					  extraFood.getKitchen().getDept().getId() + ", " +
//					  extraFood.getKitchen().getId() + ", " +
//					  extraFood.getKitchen().getAliasId() + ", '" + 
//					  term.owner + "', " +
//					  "NOW(), " + 
//					  (extraFood.isTemp() ? 1 : 0) + ", " +
//					  (isPaidAgain ? 1 : 0) +
//					  " ) ";
//				dbCon.stmt.executeUpdate(sql);			
//			}
//			
//			//insert the canceled order food records 
//			for(OrderFood cancelledFood : cancelledFoods){
//
//				//minus the gift amount if canceled foods
//				if(cancelledFood.isGift()){
//					giftAmount -= cancelledFood.getUnitPriceWithTaste() * cancelledFood.getCount();
//				}				
//				
//				sql = " INSERT INTO `" + Params.dbName + "`.`order_food` " +
//					  " ( " +
//					  " `restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_count`, `unit_price`, `name`, `food_status`, " +
//					  " `discount`, `taste_group_id`, `cancel_reason_id`, `cancel_reason`, " +
//					  " `dept_id`, `kitchen_id`, `kitchen_alias`, " +
//					  " `waiter`, `order_date`, `is_temporary`, `is_paid`) VALUES (" +
//					  term.restaurantID + ", " +
//					  newOrder.getId() + ", " +
//					  (cancelledFood.getFoodId() == 0 ? "NULL" : cancelledFood.getFoodId()) + ", " +
//					  cancelledFood.getAliasId() + ", " + 
//					  "-" + cancelledFood.getCount() + ", " + 
//					  cancelledFood.getPrice() + ", '" + 
//					  cancelledFood.getName() + "', " + 
//					  cancelledFood.getStatus() + ", " +
//					  cancelledFood.getDiscount() + ", " +
//					  (cancelledFood.hasTaste() ? cancelledFood.getTasteGroup().getGroupId() : TasteGroup.EMPTY_TASTE_GROUP_ID) + ", " +
//					  (cancelledFood.hasCancelReason() ? cancelledFood.getCancelReason().getId() : CancelReason.NO_REASON) + ", " +
//					  (cancelledFood.hasCancelReason() ? "'" + cancelledFood.getCancelReason().getReason() + "'" : "NULL") + ", " +
//					  cancelledFood.getKitchen().getDept().getId() + ", " +
//					  cancelledFood.getKitchen().getId() + ", " +
//					  cancelledFood.getKitchen().getAliasId() + ", '" + 
//					  term.owner + "', " +
//					  "NOW(), " + 
//					  (cancelledFood.isTemp() ? 1 : 0) + ", " +
//					  (isPaidAgain ? 1 : 0) +
//					  " ) ";
//				dbCon.stmt.executeUpdate(sql);			
//			}
//			
//			/**
//			 * Update the gift amount if not reach the quota.
//			 * Otherwise throw a business exception.
//			 */
//			if(term.getGiftQuota() >= 0 && !isPaidAgain){
//				if((giftAmount + term.getGiftAmount()) > term.getGiftQuota()){
//					throw new BusinessException("The gift amount exceeds the quota.", ErrorCode.EXCEED_GIFT_QUOTA);
//					
//				}else{
//					sql = " UPDATE " + 
//						  Params.dbName + ".terminal SET" +
//						  " gift_amount = gift_amount + " + giftAmount +
//						  " WHERE " +
//						  " pin = " + "0x" + Long.toHexString(term.pin) +
//						  " AND " +
//						  " restaurant_id = " + term.restaurantID;
//					dbCon.stmt.executeUpdate(sql);
//				}
//			}
//			
//			/**
//			 * Update the related info to this order.
//			 * Don't update the region and table status if the order has been paid before.
//			 */
//			sql = " UPDATE " + 
//				  Params.dbName + ".order SET " +
//				  " custom_num = " + newOrder.getCustomNum() +	", " +
//				  " terminal_pin = " + term.pin + ", " +
//				  " discount_id = " + newOrder.getDiscount().getId() + ", " +
//				  " order_date = NOW(), " +
//				  (isPaidAgain ? "" : "region_id = " + newOrder.getRegion().getRegionId() + ", ") +
//				  (isPaidAgain ? "" : "region_name = '" + newOrder.getRegion().getName() + "', ") +
//				  (isPaidAgain ? "" : "table_id = " + newOrder.getDestTbl().getTableId() + ", ") +
//				  (isPaidAgain ? "" : "table_alias = " + newOrder.getDestTbl().getAliasId() + ", ") +
//				  (isPaidAgain ? "" : "table_name = '" + newOrder.getDestTbl().getName() + "', ") +
//				  " waiter = " + "'" + term.owner + "' " +
//				  " WHERE " +
//				  " id = " + newOrder.getId();
//			dbCon.stmt.executeUpdate(sql);
//			
//			/**
//			 * Update the custom number to the merger table if the order has NOT been paid before.
//			 */
//			if(!isPaidAgain){
//				/**
//				 * Update the table status in tow cases.
//				 * 1 - Transfer table
//				 * 2 - Not transfer table
//				 */
//				if(newOrder.getDestTbl().getAliasId() != newOrder.getSrcTbl().getAliasId()){
//					// update the original table status to idle
//					sql = " UPDATE " + 
//						  Params.dbName + ".table SET " +
//						  " status = " + Table.TABLE_IDLE + "," + 
//						  " custom_num = NULL, " +
//						  " category = NULL " + 
//						  " WHERE " +
//						  " restaurant_id = " + newOrder.getSrcTbl().getRestaurantId() + 
//						  " AND " +
//						  " table_alias = "	+ newOrder.getSrcTbl().getAliasId();
//					dbCon.stmt.executeUpdate(sql);				
//					
//					// update the new table status to busy
//					sql = " UPDATE " + 
//						  Params.dbName + ".table SET " +
//						  " status = " + Table.TABLE_BUSY + "," +
//						  " category = " + newOrder.getSrcTbl().getCategory() + "," +
//						  " custom_num = " + newOrder.getCustomNum() + 
//						  " WHERE " +
//						  " restaurant_id = " + newOrder.getDestTbl().getRestaurantId() + 
//						  " AND " +
//						  " table_alias = " + newOrder.getDestTbl().getAliasId();
//					dbCon.stmt.executeUpdate(sql);				
//					
//				}else{
//
//					sql = " UPDATE " + 
//						  Params.dbName + ".table SET " +
//					      " status = " + Table.TABLE_BUSY + "," +
//						  " category = " + newOrder.getCategory() + "," +
//						  " custom_num = " + newOrder.getCustomNum() +
//						  " WHERE " +
//						  " restaurant_id = " + term.restaurantID + 
//						  " AND " +
//						  " table_alias = " + newOrder.getDestTbl().getAliasId();
//					dbCon.stmt.executeUpdate(sql);				
//				}				
//			}
//			
//			dbCon.conn.commit();
//			
//			/**
//			 * Find the extra and canceled foods and put them to result.
//			 */
////			if(!extraFoods.isEmpty() || !cancelledFoods.isEmpty()){			
////				
////				ArrayList<OrderFood> tmpFoods = new ArrayList<OrderFood>();				
////				
////				/**
////				 * Find the canceled foods to print
////				 */
////				tmpFoods.clear();
////				
////				Iterator<OrderFood> iterCancelled = cancelledFoods.iterator();
////				
////				while(iterCancelled.hasNext()){
////					
////					OrderFood canceledFood = iterCancelled.next();
////					
////					for(OrderFood extraFood : extraFoods){						
////						/**
////						 * If the food to cancel is hang up before.
////						 * Check to see whether the same extra food is exist
////						 * and the amount is equal or greater than the canceled.
////						 * If so, means the extra food is immediate
////						 * and remove this food from cancelled list so that NOT print this canceled food.
////						 */
////						if(canceledFood.hangStatus == OrderFood.FOOD_HANG_UP){
////							if(extraFood.equalsIgnoreHangStauts(canceledFood) && 
////							   extraFood.getCount().floatValue() >= canceledFood.getCount().floatValue()){
////								
////								iterCancelled.remove();
////								extraFood.hangStatus = OrderFood.FOOD_IMMEDIATE;
////								break;
////							}
////							
////						/**
////						 * In the case below, 
////						 * 1 - both of foods is the same except tastes
////						 * 2 - order count is matched 
////						 * Means just change the taste preference to this cancelled food. 
////						 * Remove this food from cancelled list so that NOT print this canceled food.
////						 */
////						}else if(canceledFood.equalsIgnoreTaste(extraFood) &&
////								 canceledFood.getCount().equals(extraFood.getCount())) {
////
////							iterCancelled.remove();
////							break;
////						}
////					}					
////				}
////				
////				
////				/**
////				 * Find the extra foods to print
////				 */
////				Iterator<OrderFood> iterExtra = extraFoods.iterator();
////				while(iterExtra.hasNext()){
////					
////					OrderFood extraFood = iterExtra.next();
////					
////					/**
////					 * In the case below, 
////					 * 1 - both of foods is the same except tastes
////					 * 2 - order count is matched 
////					 * Means just change the taste preference to this extra food.
////					 * We don't print this record.
////					 */
////					for(OrderFood canceledFood : cancelledFoods){
////						if(extraFood.equalsIgnoreTaste(canceledFood) &&
////						   extraFood.getCount().equals(canceledFood.getCount())){
////								iterExtra.remove();
////								break;
////						}
////					}					
////				}	
////				
////			}	
//			
//			return diffResult;
//			
//		}catch(SQLException e){
//			dbCon.conn.rollback();
//			throw e;
//			
//		}catch(BusinessException e){
//			dbCon.conn.rollback();
//			throw e;
//			
//		}catch(Exception e){
//			dbCon.conn.rollback();
//			throw new BusinessException(e.getMessage());
//			
//		}finally{
//			dbCon.conn.setAutoCommit(true);
//		}
//
//	}	
	
	/**
	 * Fill the detail to food.
	 * The basic information consists of alias id, discount, taste id, hang status and so on.
	 * @param dbCon
	 * 			The db connection
	 * @param term
	 * 			The terminal associated with this request
	 * @param foodToFill
	 * 			The food instance with the basic information
	 * @return 
	 * 			The food instance with the detail information
	 * @throws BusinessException
	 * 			Throws with "MENU_EXPIRED" if the food can NOT be found in db
	 * @throws SQLException
	 * 			Throws if fail to execute any SQL statement
	 */
	private static void fillFoodDetail(DBCon dbCon, Terminal term, OrderFood foodToFill) throws BusinessException, SQLException{
		
		//Get the details to cancel reason if contained.
		if(foodToFill.hasCancelReason()){
			foodToFill.setCancelReason(CancelReasonDao.getReasonById(dbCon, term, foodToFill.getCancelReason().getId()));
		}
		
		if(foodToFill.isTemp()){
			// Get the associated kitchen detail in case of temporary.
			foodToFill.setKitchen(KitchenDao.getKitchenByAlias(dbCon, term, foodToFill.getKitchen().getAliasId()));
			
		}else{
			//Get the details to each order food			
			Food[] detailFood = QueryMenu.queryFoods(dbCon, " AND FOOD.food_alias=" + foodToFill.getAliasId() + " AND FOOD.restaurant_id=" + term.restaurantID, null);
			
			if(detailFood.length > 0){
				foodToFill.setFoodId(detailFood[0].getFoodId());
				foodToFill.setAliasId(detailFood[0].getAliasId());
				foodToFill.setRestaurantId(detailFood[0].getRestaurantId());
				foodToFill.setStatus(detailFood[0].getStatus());
				foodToFill.setName(detailFood[0].getName());
				foodToFill.setPrice(detailFood[0].getPrice());
				foodToFill.setKitchen(detailFood[0].getKitchen());
				foodToFill.setChildFoods(detailFood[0].getChildFoods());
			}else{
				throw new BusinessException("The food(alias_id=" + foodToFill.getAliasId() + ", restaurant_id=" + term.restaurantID + ") to query does NOT exist.", ProtocolError.MENU_EXPIRED);
			}			

			//Get the details to each normal tastes
			if(foodToFill.hasNormalTaste()){
				Taste[] tastes; 
				//Get the detail to tastes.
				tastes = foodToFill.getTasteGroup().getTastes();
				for(int j = 0; j < tastes.length; j++){
					Taste[] detailTaste = QueryMenu.queryTastes(dbCon, 
																Taste.CATE_ALL, 
																" AND restaurant_id=" + term.restaurantID + " AND taste_alias =" + tastes[j].getAliasId(), 
																null);

					if(detailTaste.length > 0){
						tastes[j] = detailTaste[0];
					}							
				}
				//Get the detail to specs.
				tastes = foodToFill.getTasteGroup().getSpecs();
				for(int j = 0; j < tastes.length; j++){
					Taste[] detailTaste = QueryMenu.queryTastes(dbCon, 
																Taste.CATE_ALL, 
																" AND restaurant_id=" + term.restaurantID + " AND taste_alias =" + tastes[j].getAliasId(), 
																null);

					if(detailTaste.length > 0){
						tastes[j] = detailTaste[0];
					}							
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
		DiffResult result = new DiffResult();

		List<OrderFood> oriFoods = new ArrayList<OrderFood>(Arrays.asList(oriOrder.getOrderFoods()));
		List<OrderFood> newFoods = new ArrayList<OrderFood>(Arrays.asList(newOrder.getOrderFoods()));
		
		result.oriOrder = oriOrder;
		result.newOrder = newOrder;		
		result.extraFoods = new ArrayList<OrderFood>();
		result.cancelledFoods = new ArrayList<OrderFood>();
		result.hurriedFoods = new ArrayList<OrderFood>();
		
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
			if(newExtraFood.hasTaste()){
				newExtraFood.getTasteGroup().setGroupId(TasteGroup.NEW_TASTE_GROUP_ID);
			}
			result.extraFoods.add(newExtraFood);
		}
		//result.extraFoods.addAll(newFoods);		
		result.cancelledFoods.addAll(oriFoods);
		
		return result;
	}
}


