package com.wireless.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderDiff;
import com.wireless.protocol.OrderDiff.DiffResult;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Table;
import com.wireless.protocol.Taste;
import com.wireless.protocol.TasteGroup;
import com.wireless.protocol.Terminal;

public class UpdateOrder {
	
//	public static class Result{
//		public Order canceledOrder = null;
//		public Order extraOrder = null;
//		public Order hurriedOrder = null;
//	}
	
	/**
	 * Update the order according to the specific table id.
	 * @param pin the pin to terminal
	 * @param model the model to terminal
	 * @param orderToUpdate the updated detail information
	 * @return the update result containing two orders below.<br>
	 * 		   - The extra order.<br>
	 * 		   - The canceled order.
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The table associated with this order does NOT exist.<br>
	 * 							 - The table of this order to update is IDLE<br>
	 * 							 - Any table to be transferred of this order is BUSY.<br>
	 * 							 - Any food to this order does NOT exist.<br>
	 * 							 - Any taste to this order does NOT exist.<br>
	 * 							 - Exceed the gift quota.
	 * @throws SQLException throws if fail to execute any SQL statement.
	 */
	public static DiffResult exec(long pin, short model, Order orderToUpdate) throws BusinessException, SQLException{
	
		DBCon dbCon = new DBCon();	
		
		try{
			dbCon.connect();
			
			return exec(dbCon, VerifyPin.exec(dbCon, pin, model), orderToUpdate);
			
		}finally{
			dbCon.disconnect();
		}
	}	
	
	/**
	 * Update the order according to the specific table id.
	 * @param term
	 * 			the terminal to query
	 * @param orderToUpdate 
	 * 			the updated detail information
	 * @return the update result containing order results below.<br>
	 * 		   - The extra order, return null if no extra order.<br>
	 * 		   - The canceled order, return null if no cancel order.<br>
	 * 		   - The hurried order, return null if no hurried order.
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The table associated with this order does NOT exist.<br>
	 * 							 - The table of this order to update is IDLE<br>
	 * 							 - Any table to be transferred of this order is BUSY.<br>
	 * 							 - Any food to this order does NOT exist.<br>
	 * 							 - Any taste to this order does NOT exist.<br>
	 * 							 - Exceed the gift quota.
	 * @throws SQLException throws if fail to execute any SQL statement.
	 */
	public static DiffResult exec(Terminal term, Order orderToUpdate) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();	
		
		try{
			dbCon.connect();
			
			return exec(dbCon, term, orderToUpdate);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the order according to the specific table id.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to query
	 * @param newOrder 
	 * 			the updated detail information
	 * @return the update result containing two orders below.<br>
	 * 		   - The extra order.<br>
	 * 		   - The canceled order.
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The table associated with this order does NOT exist.<br>
	 * 	             			 - The order associated with this table is expired<br>.
	 * 							 - The table of this order to update is IDLE<br>
	 * 							 - Any table to be transferred of this order is BUSY.<br>
	 * 							 - Any food to this order does NOT exist.<br>
	 * 							 - Any taste to this order does NOT exist.<br>
	 * 							 - Exceed the gift quota.
	 * @throws SQLException throws if fail to execute any SQL statement.
	 */
	public static DiffResult exec(DBCon dbCon, Terminal term, Order newOrder) throws BusinessException, SQLException{
		
		//Table oriTbl = null, newTbl = null;
		
		/**
		 * There are two update order condition to deal with.
		 * 1 - The table is the same
		 * 2 - The table is different
		 * 
		 * In the 1st case, need to assure the table to update remains in busy.
		 * 
		 * In the 2nd case, need to assure two conditions
		 * 1 - original table remains in busy
		 * 2 - the table to be transferred is idle now
		 */
		

		/**
		 * In the case the table is the same as before,
		 * need to assure the table to update remains in busy.
		 */
		if(newOrder.destTbl.aliasID == newOrder.srcTbl.aliasID){
			
			newOrder.destTbl = QueryTable.exec(dbCon, term, newOrder.destTbl.aliasID);
			if(newOrder.destTbl.status == Table.TABLE_IDLE){
				throw new BusinessException("The destination table(alias_id=" + newOrder.destTbl.aliasID + ", restaurant_id=" + term.restaurantID + ") to update order is IDLE."
											,ErrorCode.TABLE_IDLE);
			}
			newOrder.id = Util.getUnPaidOrderID(dbCon, newOrder.destTbl);
			
		/**
		 * In the case that the table is different from before,
		 * need to assure two conditions
		 * 1 - original table remains in busy
		 * 2 - the table to be transferred is idle now
		 */
		}else{			
			
			newOrder.srcTbl = QueryTable.exec(dbCon, term, newOrder.srcTbl.aliasID);
			newOrder.destTbl = QueryTable.exec(dbCon, term, newOrder.destTbl.aliasID);
			
			if(newOrder.destTbl.status == Table.TABLE_BUSY){
				throw new BusinessException("The destination table(alias_id=" + newOrder.destTbl.aliasID + ", restaurant_id=" + newOrder.destTbl.restaurantID + ") is BUSY.",
											ErrorCode.TABLE_BUSY);
				
			}else if(newOrder.srcTbl.status == Table.TABLE_IDLE){
				throw new BusinessException("The source table(alias_id=" + newOrder.srcTbl.aliasID + ", restaurant_id=" + newOrder.srcTbl.restaurantID + ") is IDLE.",
											ErrorCode.TABLE_IDLE);
			}
			newOrder.id = Util.getUnPaidOrderID(dbCon, newOrder.srcTbl);
		}
		
		Order oriOrder = QueryOrder.execByID(newOrder.id, QueryOrder.QUERY_TODAY);
		
		return updateOrder(dbCon, term, oriOrder, newOrder, false);
	}
	
	/**
	 * Update the order according to the specific order id.
	 * 
	 * @param pin
	 *            the pin to terminal
	 * @param model
	 *            the model to terminal
	 * @param orderToUpdate
	 *            the order along with the order id and other detail information
	 * @param isPaidAgain
	 * 			  indicating whether the order has been paid before
	 * @return the update result containing two orders below.<br>
	 *         - The extra order.<br>
	 *         - The canceled order.
	 * @throws BusinessException
	 *             throws if one of the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The order to this id does NOT exist.<br>
	 *             - The order to this id is expired<br>.
	 *             - Any food to this order does NOT exist.<br>
	 *             - Any taste to this order does NOT exist.<br>
	 *             - Exceed the gift quota.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static DiffResult execByID(long pin, short model, Order orderToUpdate, boolean isPaidAgain) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();	
		
		try{
			dbCon.connect();
			return execByID(dbCon, pin, model, orderToUpdate, isPaidAgain);

		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the order according to the specific order id. Note that the method
	 * should be invoked before database connected.
	 * 
	 * @param pin
	 *            the pin to terminal
	 * @param model
	 *            the model to terminal
	 * @param newOrder
	 *            the order along with the order id and other detail information
	 * @param isPaidAgain
	 *            indicating whether the order has been paid before
	 * 
	 * @return the update result containing two orders below.<br>
	 *         - The extra order.<br>
	 *         - The canceled order.
	 * @throws BusinessException
	 *             throws if one of the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The order to this id does NOT exist.<br>
	 *             - The order to this id is expired<br>.
	 *             - Any food to this order does NOT exist.<br>
	 *             - Any taste to this order does NOT exist.<br>
	 *             - Exceed the gift quota.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static DiffResult execByID(DBCon dbCon, long pin, short model, Order newOrder, boolean isPaidAgain) throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(dbCon, pin, model);
		Order oriOrder = QueryOrder.execByID(dbCon, newOrder.id, QueryOrder.QUERY_TODAY);

		newOrder.destTbl = oriOrder.destTbl;
		newOrder.srcTbl = newOrder.destTbl;
		newOrder.category = newOrder.category;
		
		return updateOrder(dbCon, term, oriOrder, newOrder, isPaidAgain);
		
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
	private static DiffResult updateOrder(DBCon dbCon, Terminal term, Order oriOrder, Order newOrder, boolean isPaidAgain) throws BusinessException, SQLException{		
		
		//Throws exception if the new order is expired.
		if(newOrder.orderDate != 0 && newOrder.orderDate < oriOrder.orderDate){
			throw new BusinessException("The order(order_id=" + newOrder.id + ",restaurant_id=" + term.restaurantID + ") has expired.", ErrorCode.ORDER_EXPIRED);
		}
		
		List<OrderFood> extraFoods;
		List<OrderFood> canceledFoods;

		//Get the detail to each order foods of new order.
		List<OrderFood> newFoods = new ArrayList<OrderFood>(newOrder.foods.length);
		for(OrderFood newFood : newOrder.foods){
			//Skip the food whose count is less than zero.
			if(newFood.getCount() > 0){
				fillFoodDetail(dbCon, term, newFood);
				newFoods.add(newFood);
			}
		}
		newOrder.foods = newFoods.toArray(new OrderFood[newFoods.size()]);
		
		//Get the difference between the original and new order.
		OrderDiff.DiffResult diffResult = OrderDiff.diff(oriOrder, newOrder);
		extraFoods = diffResult.extraFoods;
		canceledFoods = diffResult.cancelledFoods;
		//hurriedFoods = diffResult.hurriedFoods;
		
		/**
		 * Get the region to this table if the order has NOT been paid before
		 */
		if(!isPaidAgain){
			newOrder.region = QueryRegion.exec(dbCon, term, newOrder.destTbl.aliasID);
		}
		
		try{
		
			String sql;
			float giftAmount = 0;
			
			dbCon.conn.setAutoCommit(false);
			
			//insert the extra order food records
			for(OrderFood extraFood : extraFoods){

				//add the gift amount if extra foods
				if(extraFood.isGift()){
					giftAmount += extraFood.getPriceWithTaste() * extraFood.getCount();
				}
				
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
						  (tg.hasTmpTaste() ? tg.getTmpTaste().aliasID : "NULL") + ", " +
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
								  normalTaste.tasteID + 
								  " ) ";
							dbCon.stmt.executeUpdate(sql);
						}
					}
				}
				
				sql = " INSERT INTO " + Params.dbName + ".order_food " +
					  " ( " + 
					  " `restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_count`, `unit_price`, `name`, `food_status`, `hang_status`, " +
					  " `discount`, `taste_group_id`, " +
					  " `dept_id`, `kitchen_id`, `kitchen_alias`, `waiter`, `order_date`, `is_temporary`, `is_paid` " +
					  " ) " +
					  " VALUES " +
					  "(" +
					  term.restaurantID + ", " +
					  newOrder.id + ", " +
					  (extraFood.foodID == 0 ? "NULL" : extraFood.foodID) + ", " +
					  extraFood.getAliasId() + ", " + 
					  extraFood.getCount() + ", " + 
					  extraFood.getPrice() + ", '" + 
					  extraFood.name + "', " + 
					  extraFood.getStatus() + ", " +
					  (extraFood.hangStatus == OrderFood.FOOD_HANG_UP ? OrderFood.FOOD_HANG_UP : OrderFood.FOOD_NORMAL) + ", " +
					  extraFood.getDiscount() + ", " +
					  (extraFood.hasTaste() ? extraFood.getTasteGroup().getGroupId() : TasteGroup.EMPTY_TASTE_GROUP_ID) + ", " +
					  extraFood.kitchen.dept.deptID + ", " +
					  extraFood.kitchen.kitchenID + ", " +
					  extraFood.kitchen.aliasID + ", '" + 
					  term.owner + "', " +
					  "NOW(), " + 
					  (extraFood.isTemporary ? 1 : 0) + ", " +
					  (isPaidAgain ? 1 : 0) +
					  " ) ";
				dbCon.stmt.executeUpdate(sql);			
			}
			
			//insert the canceled order food records 
			for(OrderFood canceledFood : canceledFoods){

				//minus the gift amount if canceled foods
				if(canceledFood.isGift()){
					giftAmount -= canceledFood.getPriceWithTaste() * canceledFood.getCount();
				}
				
				sql = " INSERT INTO `" + Params.dbName + "`.`order_food` " +
					  " ( " +
					  " `restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_count`, `unit_price`, `name`, `food_status`, `hang_status`, " +
					  " `discount`, `taste_group_id`, " +
					  " `dept_id`, `kitchen_id`, `kitchen_alias`, " +
					  " `waiter`, `order_date`, `is_temporary`, `is_paid`) VALUES (" +
					  term.restaurantID + ", " +
					  newOrder.id + ", " +
					  (canceledFood.foodID == 0 ? "NULL" : canceledFood.foodID) + ", " +
					  canceledFood.getAliasId() + ", " + 
					  "-" + canceledFood.getCount() + ", " + 
					  canceledFood.getPrice() + ", '" + 
					  canceledFood.name + "', " + 
					  canceledFood.getStatus() + ", " +
					  (canceledFood.hangStatus == OrderFood.FOOD_HANG_UP ? OrderFood.FOOD_HANG_UP : OrderFood.FOOD_NORMAL) + ", " +
					  canceledFood.getDiscount() + ", " +
					  (canceledFood.hasTaste() ? canceledFood.getTasteGroup().getGroupId() : TasteGroup.EMPTY_TASTE_GROUP_ID) + ", " +
					  canceledFood.kitchen.dept.deptID + ", " +
					  canceledFood.kitchen.kitchenID + ", " +
					  canceledFood.kitchen.aliasID + ", '" + 
					  term.owner + "', " +
					  "NOW(), " + 
					  (canceledFood.isTemporary ? 1 : 0) + ", " +
					  (isPaidAgain ? 1 : 0) +
					  " ) ";
				dbCon.stmt.executeUpdate(sql);			
			}
			
			/**
			 * Update the gift amount if not reach the quota.
			 * Otherwise throw a business exception.
			 */
			if(term.getGiftQuota() >= 0 && !isPaidAgain){
				if((giftAmount + term.getGiftAmount()) > term.getGiftQuota()){
					throw new BusinessException("The gift amount exceeds the quota.", ErrorCode.EXCEED_GIFT_QUOTA);
					
				}else{
					sql = " UPDATE " + 
						  Params.dbName + ".terminal SET" +
						  " gift_amount = gift_amount + " + giftAmount +
						  " WHERE " +
						  " pin = " + "0x" + Long.toHexString(term.pin) +
						  " AND " +
						  " restaurant_id = " + term.restaurantID;
					dbCon.stmt.executeUpdate(sql);
				}
			}
			
			/**
			 * Update the related info to this order.
			 * Don't update the region and table status if the order has been paid before.
			 */
			sql = " UPDATE " + 
				  Params.dbName + ".order SET " +
				  " custom_num = " + newOrder.customNum +	", " +
				  " terminal_pin = " + term.pin + ", " +
				  " is_paid = " + (isPaidAgain ? 1 : 0) + ", " +
				  " discount_id = " + newOrder.getDiscount().discountID + ", " +
				  " order_date = NOW(), " +
				  (isPaidAgain ? "" : "region_id=" + newOrder.region.regionID + ", ") +
				  (isPaidAgain ? "" : "region_name='" + newOrder.region.name + "', ") +
				  (isPaidAgain ? "" : "table_id=" + newOrder.destTbl.tableID + ", ") +
				  (isPaidAgain ? "" : "table_alias=" + newOrder.destTbl.aliasID + ", ") +
				  (isPaidAgain ? "" : "table_name='" + newOrder.destTbl.name + "', ") +
				  " waiter = " + "'" + term.owner + "' " +
				  " WHERE " +
				  " id = " + newOrder.id;
			dbCon.stmt.executeUpdate(sql);
			
			/**
			 * Update the custom number to the merger table if the order has NOT been paid before.
			 */
			if(!isPaidAgain){
				if(newOrder.category == Order.CATE_MERGER_TABLE){
					sql = " UPDATE " + 
						  Params.dbName + ".table SET " +
						  " status= " + Table.TABLE_BUSY + ", " +
						  " category= " + Order.CATE_MERGER_TABLE + ", " +
						  " custom_num= " + newOrder.customNum +
					  	  " WHERE " +
					  	  " restaurant_id= " + term.restaurantID + 
					  	  " AND " +
					  	  " table_alias= " + newOrder.destTbl2.aliasID;
					dbCon.stmt.executeUpdate(sql);				
				}
				
				/**
				 * Update the table status in tow cases.
				 * 1 - Transfer table
				 * 2 - Not transfer table
				 */
				if(newOrder.destTbl.aliasID != newOrder.srcTbl.aliasID){
					// update the original table status to idle
					sql = " UPDATE " + 
						  Params.dbName + ".table SET " +
						  " status = " + Table.TABLE_IDLE + "," + 
						  " custom_num = NULL, " +
						  " category = NULL " + 
						  " WHERE " +
						  " restaurant_id = " + newOrder.srcTbl.restaurantID + 
						  " AND " +
						  " table_alias = "	+ newOrder.srcTbl.aliasID;
					dbCon.stmt.executeUpdate(sql);				
					
					// update the new table status to busy
					sql = " UPDATE " + 
						  Params.dbName + ".table SET " +
						  " status = " + Table.TABLE_BUSY + "," +
						  " category = " + newOrder.srcTbl.category + "," +
						  " custom_num = " + newOrder.customNum + 
						  " WHERE " +
						  " restaurant_id = " + newOrder.destTbl.restaurantID + 
						  " AND " +
						  " table_alias = " + newOrder.destTbl.aliasID;
					dbCon.stmt.executeUpdate(sql);				
					
				}else{

					sql = " UPDATE " + 
						  Params.dbName + ".table SET " +
					      " status = " + Table.TABLE_BUSY + "," +
						  " category = " + newOrder.category + "," +
						  " custom_num = " + newOrder.customNum +
						  " WHERE " +
						  " restaurant_id = " + term.restaurantID + 
						  " AND " +
						  " table_alias = " + newOrder.destTbl.aliasID;
					dbCon.stmt.executeUpdate(sql);				
				}				
			}
			
			dbCon.conn.commit();
			
			/**
			 * Find the extra and canceled foods and put them to result.
			 */
			if(!extraFoods.isEmpty() || !canceledFoods.isEmpty()){			
				
				ArrayList<OrderFood> tmpFoods = new ArrayList<OrderFood>();				
				
				/**
				 * Find the canceled foods to print
				 */
				tmpFoods.clear();
				
				Iterator<OrderFood> iterCancelled = canceledFoods.iterator();
				
				while(iterCancelled.hasNext()){
					
					OrderFood canceledFood = iterCancelled.next();
					
					for(OrderFood extraFood : extraFoods){						
						/**
						 * If the food to cancel is hang up before.
						 * Check to see whether the same extra food is exist
						 * and the amount is equal or greater than the canceled.
						 * If so, means the extra food is immediate
						 * and remove this food from cancelled list so that NOT print this canceled food.
						 */
						if(canceledFood.hangStatus == OrderFood.FOOD_HANG_UP){
							if(extraFood.equalsIgnoreHangStauts(canceledFood) && 
							   extraFood.getCount().floatValue() >= canceledFood.getCount().floatValue()){
								
								iterCancelled.remove();
								extraFood.hangStatus = OrderFood.FOOD_IMMEDIATE;
								break;
							}
							
						/**
						 * In the case below, 
						 * 1 - both of foods is the same except tastes
						 * 2 - order count is matched 
						 * Means just change the taste preference to this cancelled food. 
						 * Remove this food from cancelled list so that NOT print this canceled food.
						 */
						}else if(canceledFood.equalsIgnoreTaste(extraFood) &&
								 canceledFood.getCount().equals(extraFood.getCount())) {

							iterCancelled.remove();
							break;
						}
					}					
				}
				
				
				/**
				 * Find the extra foods to print
				 */
				Iterator<OrderFood> iterExtra = extraFoods.iterator();
				while(iterExtra.hasNext()){
					
					OrderFood extraFood = iterExtra.next();
					
					/**
					 * In the case below, 
					 * 1 - both of foods is the same except tastes
					 * 2 - order count is matched 
					 * Means just change the taste preference to this extra food.
					 * We don't print this record.
					 */
					for(OrderFood canceledFood : canceledFoods){
						if(extraFood.equalsIgnoreTaste(canceledFood) &&
						   extraFood.getCount().equals(canceledFood.getCount())){
								iterExtra.remove();
								break;
						}
					}					
				}	
				
			}	
			
			return diffResult;
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
			
		}catch(Exception e){
			dbCon.conn.rollback();
			throw new BusinessException(e.getMessage());
			
		}finally{
			dbCon.conn.setAutoCommit(true);
		}

	}	
	
	/**
	 * Fill the detail to basic food.
	 * The basic information consists of alias id, discount, taste id, hang status and so on.
	 * @param dbCon
	 * 			The db connection
	 * @param term
	 * 			The terminal associated with this request
	 * @param foodBasic
	 * 			The food instance with the basic information
	 * @return 
	 * 			The food instance with the detail information
	 * @throws BusinessException
	 * 			Throws with "MENU_EXPIRED" if the food can NOT be found in db
	 * @throws SQLException
	 * 			Throws if fail to execute any SQL statement
	 */
	private static void fillFoodDetail(DBCon dbCon, Terminal term, OrderFood foodBasic) throws BusinessException, SQLException{
		
		/**
		 * Firstly, check to see whether the submitted food is temporary.
		 * If temporary, assign food basic's the name and price directly.
		 * If not, check to see whether the submitted food sent by terminal exist in db.
		 * If the food can NOT be found in db, means the menu in terminal has been expired,
		 * and then sent back an error to tell the terminal to update the menu.
		 * secondly, check to see whether the taste preference submitted by terminal exist in db or not.
		 * If the taste preference can't be found in db, means the taste in terminal has been expired,
		 * and then sent back an error to tell the terminal to update the menu.
		 */			
		if(foodBasic.isTemporary){
			Kitchen[] kitchens = QueryMenu.queryKitchens(dbCon, "AND KITCHEN.kitchen_alias=" + foodBasic.kitchen.aliasID + " AND KITCHEN.restaurant_id=" + term.restaurantID, null);
			if(kitchens.length > 0){
				foodBasic.kitchen = kitchens[0];
			}
			
		}else{
			//Get the details to each order food			
			Food[] detailFood = QueryMenu.queryFoods(dbCon, " AND FOOD.food_alias=" + foodBasic.getAliasId() + " AND FOOD.restaurant_id=" + term.restaurantID, null);
			
			if(detailFood.length > 0){
				foodBasic.foodID = detailFood[0].foodID;
				foodBasic.setAliasId(detailFood[0].getAliasId());
				foodBasic.restaurantID = detailFood[0].restaurantID;
				foodBasic.setStatus(detailFood[0].getStatus());
				foodBasic.name = detailFood[0].name;
				foodBasic.setPrice(detailFood[0].getPrice());
				foodBasic.kitchen = detailFood[0].kitchen;
				foodBasic.childFoods = detailFood[0].childFoods;
			}else{
				throw new BusinessException("The food(alias_id=" + foodBasic.getAliasId() + ", restaurant_id=" + term.restaurantID + ") to query does NOT exist.", ErrorCode.MENU_EXPIRED);
			}			

			//Get the details to each normal tastes
			if(foodBasic.hasNormalTaste()){
				Taste[] tastes; 
				//Get the detail to tastes.
				tastes = foodBasic.getTasteGroup().getTastes();
				for(int j = 0; j < tastes.length; j++){
					Taste[] detailTaste = QueryMenu.queryTastes(dbCon, 
																Taste.CATE_ALL, 
																" AND restaurant_id=" + term.restaurantID + " AND taste_alias =" + tastes[j].aliasID, 
																null);

					if(detailTaste.length > 0){
						tastes[j] = detailTaste[0];
					}							
				}
				//Get the detail to specs.
				tastes = foodBasic.getTasteGroup().getSpecs();
				for(int j = 0; j < tastes.length; j++){
					Taste[] detailTaste = QueryMenu.queryTastes(dbCon, 
																Taste.CATE_ALL, 
																" AND restaurant_id=" + term.restaurantID + " AND taste_alias =" + tastes[j].aliasID, 
																null);

					if(detailTaste.length > 0){
						tastes[j] = detailTaste[0];
					}							
				}
			}
		}		
	}
	
}


