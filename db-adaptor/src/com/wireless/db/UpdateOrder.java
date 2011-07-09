package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.Table;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Terminal;

enum STATUS{
	NOT_MATCHED,
	FULL_MATCHED,
	FULL_MATCHED_BUT_COUNT
}

public class UpdateOrder {
	
	public static class Result{
		public Order canceledOrder = null;
		public Order extraOrder = null;
	}
	
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
	 * 							 - Any taste to this order does NOT exist.
	 * @throws SQLException throws if fail to execute any SQL statement.
	 */
	public static Result exec(int pin, short model, Order orderToUpdate) throws BusinessException, SQLException{
	
		DBCon dbCon = new DBCon();	
		
		try{
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			
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
			
			int orderID = 0;
			
			/**
			 * In the case the table is the same as before,
			 * need to assure the table to update remains in busy.
			 */
			if(orderToUpdate.table_id == orderToUpdate.originalTableID){
				
				Table table = QueryTable.exec(dbCon, pin, model, orderToUpdate.table_id);
				if(table.status == Table.TABLE_IDLE){
					throw new BusinessException("The table(alias_id=" + orderToUpdate.table_id + ") to change order is IDLE."
												,ErrorCode.TABLE_IDLE);
				}
				orderToUpdate.table_name = table.name;
				orderID = Util.getUnPaidOrderID(dbCon, table);
				
			/**
			 * In the case that the table is different from before,
			 * need to assure two conditions
			 * 1 - original table remains in busy
			 * 2 - the table to be transferred is idle now
			 */
			}else{			
				
				Table originalTable = QueryTable.exec(dbCon, pin, model, orderToUpdate.originalTableID);
				Table transferredTable = QueryTable.exec(dbCon, pin, model, orderToUpdate.table_id);
				
				if(transferredTable.status == Table.TABLE_BUSY){
					throw new BusinessException("The table(alias_id=" + orderToUpdate.table_id + ") to be transferred is BUSY.",
												ErrorCode.TABLE_BUSY);
					
				}else if(originalTable.status == Table.TABLE_IDLE){
					throw new BusinessException("The original table(alias_id=" + orderToUpdate.originalTableID + ") to be transferred is IDLE.",
												ErrorCode.TABLE_IDLE);
				}
				orderToUpdate.table_name = transferredTable.name;
				orderID = Util.getUnPaidOrderID(dbCon, originalTable);

			}
			
			//query all the food's id ,order count and taste preference of this order
			ArrayList<Food> originalRecords = new ArrayList<Food>();
			String sql = "SELECT food_id, unit_price, name, food_status, discount, SUM(order_count) AS order_sum, taste, taste_price, taste_id, kitchen FROM `" + 
						Params.dbName + "`.`order_food` WHERE order_id=" + orderID + 
						" GROUP BY food_id, taste_id HAVING order_sum > 0";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Food food = new Food();
				food.alias_id = dbCon.rs.getShort("food_id");
				food.setPrice(new Float(dbCon.rs.getFloat("unit_price")));
				food.name = dbCon.rs.getString("name");
				food.status = dbCon.rs.getShort("food_status");
				food.discount = (byte)(dbCon.rs.getFloat("discount") * 100);
				food.setCount(new Float(dbCon.rs.getFloat("order_sum")));
				food.kitchen = dbCon.rs.getShort("kitchen");
				food.taste.alias_id = dbCon.rs.getShort("taste_id");
				food.taste.preference = dbCon.rs.getString("taste");
				food.taste.setPrice(new Float(dbCon.rs.getFloat("taste_price")));
				originalRecords.add(food);
			}
			dbCon.rs.close();
			
			ArrayList<Food> extraFoods = new ArrayList<Food>();
			ArrayList<Food> cancelledFoods = new ArrayList<Food>();
			
			for(int i = 0; i < orderToUpdate.foods.length; i++){
						
				/**
				 * Assume the order record is new, means not matched any original record.
				 * So the difference is equal to the amount of new order food
				 */
				STATUS status = STATUS.NOT_MATCHED;
				float diff = orderToUpdate.foods[i].count2Float().floatValue();
				
				for(int j = 0; j < originalRecords.size(); j++){
					/**
					 * In the case below,
					 * 1 - both food alias id and taste id is matched
					 * 2 - order count is matched
					 * Skip this record since it is totally the same as original.
					 */
					if(orderToUpdate.foods[i].equals(originalRecords.get(j)) &&
						orderToUpdate.foods[i].count == originalRecords.get(j).count){
						diff = 0;
						status = STATUS.FULL_MATCHED;
						break;
						
					/**
					 * In the case below,
					 * 1 - both food alias id and taste id is matched
					 * 2 - order count isn't matched
					 * Calculate the difference between these two records and insert a new record to keep track of this incremental
					 */
					}else if(orderToUpdate.foods[i].equals(originalRecords.get(j)) &&
							orderToUpdate.foods[i].count != originalRecords.get(j).count){

						//calculate the difference between the submitted and original record
						diff = orderToUpdate.foods[i].count2Float().floatValue() - originalRecords.get(j).count2Float().floatValue();					
						status = STATUS.FULL_MATCHED_BUT_COUNT;
						break;					
					}
				}
				
				if(status == STATUS.NOT_MATCHED || status == STATUS.FULL_MATCHED_BUT_COUNT){
					
					/**
					 * firstly, check to see whether the new food submitted by terminal exist in db or is disabled by user.
					 * If the food can't be found in db or has been disabled by user, means the menu in terminal has been expired,
					 * and then sent back an error to tell the terminal to update the menu.
					 * secondly, check to see whether the taste preference submitted by terminal exist in db or not.
					 * If the taste preference can't be found in db, means the taste in terminal has been expired,
					 * and then sent back an error to tell the terminal to update the menu.
					 */
					
					//get the food name and its unit price
					sql = "SELECT name, status, unit_price, kitchen FROM " + Params.dbName + 
							".food WHERE alias_id=" + orderToUpdate.foods[i].alias_id + 
							" AND restaurant_id=" + term.restaurant_id +
							" AND enabled=1";
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					//check if the food to be inserted exist in db or not
					Food food = new Food();
					if(dbCon.rs.next()){
						food.alias_id = orderToUpdate.foods[i].alias_id;
						food.status = dbCon.rs.getShort("status");
						food.name = dbCon.rs.getString("name");
						food.setPrice(new Float(dbCon.rs.getFloat("unit_price")));
						food.setCount(new Float((float)Math.round(Math.abs(diff) * 100) / 100));
						food.kitchen = dbCon.rs.getShort("kitchen");
					}else{
						throw new BusinessException("The food(alias_id=" + orderToUpdate.foods[i].alias_id + ") to query does NOT exist.", ErrorCode.MENU_EXPIRED);
					}
					dbCon.rs.close();
					
					/**
					 * The special food does NOT discount.
					 */
//					if(food.isSpecial()){
//						food.discount = 100;
//					}else{
//						//get the associated foods' discount
//						sql = "SELECT discount FROM " + Params.dbName + ".kitchen WHERE restaurant_id=" + term.restaurant_id +
//							" AND alias_id=" + orderToUpdate.foods[i].kitchen;		
//						dbCon.rs = dbCon.stmt.executeQuery(sql);
//						if(dbCon.rs.next()){
//							food.discount = (byte)(dbCon.rs.getFloat("discount") * 100);
//						}
//						dbCon.rs.close();						
//					}
					
					//get the taste preference only if the food has taste preference
					if(orderToUpdate.foods[i].taste.alias_id != Taste.NO_TASTE){
						sql = "SELECT preference, price FROM " + Params.dbName + ".taste WHERE restaurant_id=" + term.restaurant_id +
							" AND alias_id=" + orderToUpdate.foods[i].taste.alias_id;
						dbCon.rs = dbCon.stmt.executeQuery(sql);
						//check if the taste preference exist in db
						if(dbCon.rs.next()){
							food.taste.alias_id = orderToUpdate.foods[i].taste.alias_id;
							food.taste.preference = dbCon.rs.getString("preference");
							food.taste.setPrice(dbCon.rs.getFloat("price"));
						}else{
							throw new BusinessException("The taste(alias_id=" + orderToUpdate.foods[i].taste.alias_id + ") to query does NOT exist.", ErrorCode.MENU_EXPIRED);
						}
						dbCon.rs.close();
						
					}
					
					if(diff > 0){
						extraFoods.add(food);
					}else if(diff < 0){
						cancelledFoods.add(food);
					}
				}
			}	
			
			//insert the canceled order records
			for(int i = 0; i < originalRecords.size(); i++){
				/**
				 * If the sum to original record's order count is zero,
				 * means the record to this food has been canceled before.
				 * So we should skip to check this record.
				 */
				if(originalRecords.get(i).count > 0){
					boolean isCancelled = true;
					for(int j = 0; j < orderToUpdate.foods.length; j++){
						if(originalRecords.get(i).equals(orderToUpdate.foods[j])){
							isCancelled = false;
							break;
						}
					}
					/**
					 * If the original records are excluded from the submitted, means the food is to be cancel.
					 * So we insert an record whose order count is negative to original record
					 */
					if(isCancelled){
						cancelledFoods.add(originalRecords.get(i));
					}			
				}
			}
			
			dbCon.stmt.clearBatch();
			
			float giftAmount = 0;
			
			//insert the extra order food records
			for(int i = 0; i < extraFoods.size(); i++){

				//add the gift amount if extra foods
				if(extraFoods.get(i).isGift()){
					giftAmount += extraFoods.get(i).getPrice2().floatValue();
				}
				
				sql = "INSERT INTO `" + Params.dbName + "`.`order_food` (`order_id`, `food_id`, `order_count`, `unit_price`, `name`, `food_status`, `discount`, `taste_id`, `taste_price`, `taste`, `kitchen`, `waiter`, `order_date`) VALUES (" +
						orderID + ", " + extraFoods.get(i).alias_id + ", " + 
						extraFoods.get(i).getCount() + ", " + 
						extraFoods.get(i).getPrice() + ", '" + 
						extraFoods.get(i).name + "', " + 
						extraFoods.get(i).status + ", " +
						(float)extraFoods.get(i).discount / 100 + ", " +
						extraFoods.get(i).taste.alias_id + "," +
						extraFoods.get(i).taste.getPrice() + ", '" +
						extraFoods.get(i).taste.preference + "', " + 
						extraFoods.get(i).kitchen + ", '" + 
						term.owner + "', NOW()" + ")";
				dbCon.stmt.addBatch(sql);			
			}
			
			//insert the canceled order food records 
			for(int i = 0; i < cancelledFoods.size(); i++){

				//minus the gift amount if canceled foods
				if(cancelledFoods.get(i).isGift()){
					giftAmount -= cancelledFoods.get(i).getPrice2().floatValue();
				}
				
				sql = "INSERT INTO `" + Params.dbName + "`.`order_food` (`order_id`, `food_id`, `order_count`, `unit_price`, `name`, `food_status`, `discount`, `taste_id`, `taste_price`, `taste`, `kitchen`, `waiter`, `order_date`) VALUES (" +
						orderID + ", " + cancelledFoods.get(i).alias_id + ", " + 
						"-" + cancelledFoods.get(i).getCount() + ", " + 
						cancelledFoods.get(i).getPrice() + ", '" + 
						cancelledFoods.get(i).name + "', " + 
						cancelledFoods.get(i).status + ", " +
						(float)cancelledFoods.get(i).discount / 100 + ", " +
						cancelledFoods.get(i).taste.alias_id + "," +
						cancelledFoods.get(i).taste.getPrice() + ", '" +
						cancelledFoods.get(i).taste.preference + "', " + 
						cancelledFoods.get(i).kitchen + ", '" + 
						term.owner + "', NOW()" + ")";
				dbCon.stmt.addBatch(sql);			
			}
			
			/**
			 * Update the gift amount if not reach the quota.
			 * Otherwise throw a business exception.
			 */
			if(term.getGiftQuota() > 0){
				if((giftAmount + term.getGiftAmount()) > term.getGiftQuota()){
					throw new BusinessException("The gift amount exceeds the quota.", ErrorCode.EXCEED_GIFT_QUOTA);
					
				}else{
					sql = "UPDATE " + Params.dbName + ".terminal SET" +
					  " gift_amount = gift_amount + " + giftAmount +
					  " WHERE pin=" + "0x" + Integer.toHexString(term.pin) +
					  " AND restaurant_id=" + term.restaurant_id;
					dbCon.stmt.executeUpdate(sql);
				}
			}

			/**
			 * Update the related info to this order
			 */
			sql = "UPDATE `" + Params.dbName + "`.`order` SET custom_num=" + orderToUpdate.custom_num +
					", terminal_pin=" + term.pin + 
					", waiter='" + term.owner + 
					"', table_id=" + orderToUpdate.table_id + 
					", table_name='" + orderToUpdate.table_name + "'" +
					" WHERE id=" + orderID;
			dbCon.stmt.addBatch(sql);

			dbCon.stmt.executeBatch();
			
			Result result = new Result();			
			/**
			 * Notify the print handler to print the extra and canceled foods
			 */
			if(!extraFoods.isEmpty() || !cancelledFoods.isEmpty()){			
				
				ArrayList<Food> tmpFoods = new ArrayList<Food>();
				
				/**
				 * Find the extra foods to print
				 */
				tmpFoods.clear();
				for(int i = 0; i < extraFoods.size(); i++){				
					boolean isExtra = true;	
					/**
					 * In the case below, 
					 * 1 - food alias id is matched 
					 * 2 - taste alias id is NOT matched 
					 * 3 - order count is matched 
					 * Means just change the taste preference to this food. 
					 * We don't print this record.
					 */
					for(int j = 0; j < cancelledFoods.size(); j++){
						if(extraFoods.get(i).alias_id == cancelledFoods.get(j).alias_id &&
						   extraFoods.get(i).count == cancelledFoods.get(j).count &&
						   extraFoods.get(i).taste.alias_id != cancelledFoods.get(j).taste.alias_id){
								isExtra = false;
								break;
						}
					}				
					if(isExtra){
						tmpFoods.add(extraFoods.get(i));
					}
				}
				
				if(!tmpFoods.isEmpty()){
					result.extraOrder = new Order();
					result.extraOrder.id = orderID;
					result.extraOrder.table_id = orderToUpdate.table_id;
					result.extraOrder.table_name = orderToUpdate.table_name;
					result.extraOrder.foods = tmpFoods.toArray(new Food[tmpFoods.size()]);
				}
				
				/**
				 * Find the canceled foods to print
				 */
				tmpFoods.clear();
				for(int i = 0; i < cancelledFoods.size(); i++){				
					boolean isCancelled = true;	
					/**
					 * In the case below, 
					 * 1 - food alias id is matched 
					 * 2 - taste alias id is NOT matched 
					 * 3 - order count is matched 
					 * Means just change the taste preference to this food. 
					 * We don't print this record.
					 */
					for(int j = 0; j < extraFoods.size(); j++){
						if(cancelledFoods.get(i).alias_id == extraFoods.get(j).alias_id &&
							cancelledFoods.get(i).count == extraFoods.get(j).count &&
							cancelledFoods.get(i).taste.alias_id != extraFoods.get(j).taste.alias_id){
							
							isCancelled = false;
							break;
						}
					}				
					if(isCancelled){
						tmpFoods.add(cancelledFoods.get(i));
					}
				}
				
				if(!tmpFoods.isEmpty()){
					result.canceledOrder = new Order();
					result.canceledOrder.id = orderID;
					result.canceledOrder.table_id = orderToUpdate.table_id;
					result.canceledOrder.table_name = orderToUpdate.table_name;
					result.canceledOrder.foods = tmpFoods.toArray(new Food[tmpFoods.size()]);
				}
			}
			
			return result;
			
		}finally{
			dbCon.disconnect();
		}
	}	
}


