package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import com.wireless.dbReflect.OrderFoodReflector;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
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
		public Order hurriedOrder = null;
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
	 * 							 - Any taste to this order does NOT exist.<br>
	 * 							 - Exceed the gift quota.
	 * @throws SQLException throws if fail to execute any SQL statement.
	 */
	public static Result exec(long pin, short model, Order orderToUpdate) throws BusinessException, SQLException{
	
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
	public static Result exec(Terminal term, Order orderToUpdate) throws BusinessException, SQLException{
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
	 * @param orderToUpdate 
	 * 			the updated detail information
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
	public static Result exec(DBCon dbCon, Terminal term, Order orderToUpdate) throws BusinessException, SQLException{
		
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
		if(orderToUpdate.table.aliasID == orderToUpdate.oriTbl.aliasID){
			
			orderToUpdate.table = QueryTable.exec(dbCon, term, orderToUpdate.table.aliasID);
			if(orderToUpdate.table.status == Table.TABLE_IDLE){
				throw new BusinessException("The table(alias_id=" + orderToUpdate.table.aliasID + ", restaurant_id=" + term.restaurantID + ") to change order is IDLE."
											,ErrorCode.TABLE_IDLE);
			}
			//orderToUpdate.table_name = oriTbl.name;
			//orderToUpdate.category = oriTbl.category;
			orderToUpdate.id = Util.getUnPaidOrderID(dbCon, orderToUpdate.table);
			
		/**
		 * In the case that the table is different from before,
		 * need to assure two conditions
		 * 1 - original table remains in busy
		 * 2 - the table to be transferred is idle now
		 */
		}else{			
			
			orderToUpdate.oriTbl = QueryTable.exec(dbCon, term, orderToUpdate.oriTbl.aliasID);
			orderToUpdate.table = QueryTable.exec(dbCon, term, orderToUpdate.table.aliasID);
			
			if(orderToUpdate.table.status == Table.TABLE_BUSY){
				throw new BusinessException("The table(alias_id=" + orderToUpdate.table.aliasID + ", restaurant_id=" + orderToUpdate.table.restaurantID + ") to be transferred is BUSY.",
											ErrorCode.TABLE_BUSY);
				
			}else if(orderToUpdate.oriTbl.status == Table.TABLE_IDLE){
				throw new BusinessException("The original table(alias_id=" + orderToUpdate.oriTbl.aliasID + ", restaurant_id=" + orderToUpdate.oriTbl.restaurantID + ") to be transferred is IDLE.",
											ErrorCode.TABLE_IDLE);
			}
			//orderToUpdate.table_name = newTbl.name;
			orderToUpdate.id = Util.getUnPaidOrderID(dbCon, orderToUpdate.oriTbl);
		}
		
		
		return updateOrder(dbCon, term, orderToUpdate, false);
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
	 *             - Any food to this order does NOT exist.<br>
	 *             - Any taste to this order does NOT exist.<br>
	 *             - Exceed the gift quota.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static Result execByID(long pin, short model, Order orderToUpdate, boolean isPaidAgain) throws BusinessException, SQLException{
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
	 * @param orderToUpdate
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
	 *             - Any food to this order does NOT exist.<br>
	 *             - Any taste to this order does NOT exist.<br>
	 *             - Exceed the gift quota.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static Result execByID(DBCon dbCon, long pin, short model, Order orderToUpdate, boolean isPaidAgain) throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(dbCon, pin, model);
		String sql = "SELECT table_id, table_alias, table_name, category FROM " + Params.dbName + ".order WHERE id=" + orderToUpdate.id;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			orderToUpdate.table.tableID = dbCon.rs.getInt("table_id");
			orderToUpdate.table.aliasID = dbCon.rs.getInt("table_alias");
			String tblName = dbCon.rs.getString("table_name");
			orderToUpdate.table.name = (tblName != null ? tblName : "");
			orderToUpdate.oriTbl.aliasID = orderToUpdate.table.aliasID;
			orderToUpdate.category = dbCon.rs.getShort("category");
			dbCon.rs.close();
			
			return updateOrder(dbCon, term, orderToUpdate, isPaidAgain);
			
		}else{
			throw new BusinessException("Order(id=" + orderToUpdate.id + ") to query does NOT exist.", ErrorCode.ORDER_NOT_EXIST);
		}
	
	}
	
	private static Result updateOrder(DBCon dbCon, Terminal term, Order orderToUpdate, boolean isPaidAgain) throws BusinessException, SQLException{		
		
		String extraCond = " AND B.id=" + orderToUpdate.id;
		OrderFood[] oriFoods = OrderFoodReflector.getDetailToday(dbCon, extraCond, null);
		
		ArrayList<OrderFood> extraFoods = new ArrayList<OrderFood>();
		ArrayList<OrderFood> canceledFoods = new ArrayList<OrderFood>();
		ArrayList<OrderFood> hurriedFoods = new ArrayList<OrderFood>();
		
		for(int i = 0; i < orderToUpdate.foods.length; i++){
					
			/**
			 * Check to see whether the food to update is hurried.
			 */
			if(orderToUpdate.foods[i].isHurried){
				OrderFood food = genFoodDetail(dbCon, term, orderToUpdate.foods[i]);
				food.setCount(orderToUpdate.foods[i].getCount());
				hurriedFoods.add(food);
			}
			
			/**
			 * Assume the order record is new, means not matched any original record.
			 * So the difference is equal to the amount of new order food
			 */
			STATUS status = STATUS.NOT_MATCHED;
			float diff = orderToUpdate.foods[i].getCount().floatValue();
			
			for(int j = 0; j < oriFoods.length; j++){
				/**
				 * In the case below,
				 * 1 - both food alias id and taste id is matched
				 * 2 - order count is matched
				 * Skip this record since it is totally the same as original.
				 */
				if(orderToUpdate.foods[i].equals(oriFoods[j]) &&
					orderToUpdate.foods[i].getCount().equals(oriFoods[j].getCount())){
					diff = 0;
					status = STATUS.FULL_MATCHED;
					break;
					
				/**
				 * In the case below,
				 * 1 - both food alias id and taste id is matched
				 * 2 - order count isn't matched
				 * Calculate the difference between these two records and insert a new record to keep track of this incremental
				 */
				}else if(orderToUpdate.foods[i].equals(oriFoods[j]) &&
						!orderToUpdate.foods[i].getCount().equals(oriFoods[j].getCount())){

					//calculate the difference between the submitted and original record
					diff = orderToUpdate.foods[i].getCount().floatValue() - oriFoods[j].getCount().floatValue();					
					status = STATUS.FULL_MATCHED_BUT_COUNT;
					break;					
				}
			}
			
			if(status == STATUS.NOT_MATCHED || status == STATUS.FULL_MATCHED_BUT_COUNT){
				
				OrderFood food = genFoodDetail(dbCon, term, orderToUpdate.foods[i]);
				
				food.setCount(new Float((float)Math.round(Math.abs(diff) * 100) / 100));
				
				if(diff > 0){
					extraFoods.add(food);
				}else if(diff < 0){
					canceledFoods.add(food);
				}
			}
		}	
		
		//insert the canceled order records
		for(int i = 0; i < oriFoods.length; i++){
			/**
			 * If the sum to original record's order count is zero,
			 * means the record to this food has been canceled before.
			 * So we should skip to check this record.
			 */
			if(oriFoods[i].getCount() > 0){
				boolean isCancelled = true;
				for(int j = 0; j < orderToUpdate.foods.length; j++){
					if(oriFoods[i].equals(orderToUpdate.foods[j])){
						isCancelled = false;
						break;
					}
				}
				/**
				 * If the original records are excluded from the submitted, means the food is to be cancel.
				 * So we insert an record whose order count is negative to original record
				 */
				if(isCancelled){
					OrderFood food = genFoodDetail(dbCon, term, oriFoods[i]);
					food.setCount(oriFoods[i].getCount());
					canceledFoods.add(food);
				}			
			}
		}
		
		/**
		 * Get the region to this table if the order has NOT been paid before
		 */
		if(!isPaidAgain){
			orderToUpdate.region = QueryRegion.exec(dbCon, term, orderToUpdate.table.aliasID);
		}
		
		try{
		
			String sql;
			float giftAmount = 0;
			
			dbCon.conn.setAutoCommit(false);
			
			//insert the extra order food records
			for(int i = 0; i < extraFoods.size(); i++){

				//add the gift amount if extra foods
				if(extraFoods.get(i).isGift()){
					giftAmount += extraFoods.get(i).getPriceWithTaste() * extraFoods.get(i).getCount();
				}
				
				sql = "INSERT INTO `" + Params.dbName + "`.`order_food` " +
						"(`restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_count`, `unit_price`, `name`, `food_status`, `hang_status`, " +
						"`taste_id`, `taste2_id`, `taste3_id`, " + 
						"`discount`, `taste_alias`, `taste2_alias`, `taste3_alias`, `taste_price`, `taste`, " +
						"`taste_tmp_alias`, `taste_tmp`, `taste_tmp_price`, " +
						"`dept_id`, `kitchen_id`, `kitchen_alias`, `waiter`, `order_date`, `is_temporary`, `is_paid`) VALUES (" +
						term.restaurantID + ", " +
						orderToUpdate.id + ", " +
						(extraFoods.get(i).foodID == 0 ? "NULL" : extraFoods.get(i).foodID) + ", " +
						extraFoods.get(i).aliasID + ", " + 
						extraFoods.get(i).getCount() + ", " + 
						extraFoods.get(i).getPrice() + ", '" + 
						extraFoods.get(i).name + "', " + 
						extraFoods.get(i).status + ", " +
						(extraFoods.get(i).hangStatus == OrderFood.FOOD_HANG_UP ? OrderFood.FOOD_HANG_UP : OrderFood.FOOD_NORMAL) + ", " +
						(extraFoods.get(i).tastes[0].tasteID == 0 ? "NULL" : extraFoods.get(i).tastes[0].tasteID) + ", " +
						(extraFoods.get(i).tastes[1].tasteID == 0 ? "NULL" : extraFoods.get(i).tastes[1].tasteID) + ", " +
						(extraFoods.get(i).tastes[2].tasteID == 0 ? "NULL" : extraFoods.get(i).tastes[2].tasteID) + ", " +
						extraFoods.get(i).getDiscount() + ", " +
						extraFoods.get(i).tastes[0].aliasID + "," +
						extraFoods.get(i).tastes[1].aliasID + "," +
						extraFoods.get(i).tastes[2].aliasID + "," +
						extraFoods.get(i).getTasteNormalPrice() + ", '" +
						extraFoods.get(i).tasteNormalPref + "', " + 
						(extraFoods.get(i).tmpTaste == null ? "NULL" : extraFoods.get(i).tmpTaste.aliasID) + ", " +
						(extraFoods.get(i).tmpTaste == null ? "NULL" : ("'" + extraFoods.get(i).tmpTaste.preference + "'")) + ", " +
						(extraFoods.get(i).tmpTaste == null ? "NULL" : extraFoods.get(i).tmpTaste.getPrice()) + ", " +
						extraFoods.get(i).kitchen.dept.deptID + ", " + 
						"(SELECT kitchen_id FROM " + Params.dbName + ".kitchen WHERE restaurant_id=" + term.restaurantID + " AND kitchen_alias=" + extraFoods.get(i).kitchen.aliasID + "), " + 
						extraFoods.get(i).kitchen.aliasID + ", '" + 
						term.owner + "', " +
						"NOW(), " + 
						(extraFoods.get(i).isTemporary ? 1 : 0) + ", " +
						(isPaidAgain ? 1 : 0) +
						")";
				dbCon.stmt.executeUpdate(sql);			
			}
			
			//insert the canceled order food records 
			for(int i = 0; i < canceledFoods.size(); i++){

				//minus the gift amount if canceled foods
				if(canceledFoods.get(i).isGift()){
					giftAmount -= canceledFoods.get(i).getPriceWithTaste() * canceledFoods.get(i).getCount();
				}
				
				sql = "INSERT INTO `" + Params.dbName + "`.`order_food` " +
						"(`restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_count`, `unit_price`, `name`, `food_status`, `hang_status`, " +
						"`taste_id`, `taste2_id`, `taste3_id`, " +
						"`discount`, `taste_alias`, `taste2_alias`, `taste3_alias`, `taste_price`, `taste`, " +
						"`taste_tmp_alias`, `taste_tmp`, `taste_tmp_price`, " +
						"`dept_id`, `kitchen_id`, `kitchen_alias`, " +
						"`waiter`, `order_date`, `is_temporary`, `is_paid`) VALUES (" +
						term.restaurantID + ", " +
						orderToUpdate.id + ", " +
						(canceledFoods.get(i).foodID == 0 ? "NULL" : canceledFoods.get(i).foodID) + ", " +
						canceledFoods.get(i).aliasID + ", " + 
						"-" + canceledFoods.get(i).getCount() + ", " + 
						canceledFoods.get(i).getPrice() + ", '" + 
						canceledFoods.get(i).name + "', " + 
						canceledFoods.get(i).status + ", " +
						(canceledFoods.get(i).hangStatus == OrderFood.FOOD_HANG_UP ? OrderFood.FOOD_HANG_UP : OrderFood.FOOD_NORMAL) + ", " +
						(canceledFoods.get(i).tastes[0].tasteID == 0 ? "NULL" : canceledFoods.get(i).tastes[0].tasteID) + ", " +
						(canceledFoods.get(i).tastes[1].tasteID == 0 ? "NULL" : canceledFoods.get(i).tastes[1].tasteID)+ ", " +
						(canceledFoods.get(i).tastes[2].tasteID == 0 ? "NULL" : canceledFoods.get(i).tastes[2].tasteID)+ ", " +
						canceledFoods.get(i).getDiscount() + ", " +
						canceledFoods.get(i).tastes[0].aliasID + "," +
						canceledFoods.get(i).tastes[1].aliasID + "," +
						canceledFoods.get(i).tastes[2].aliasID + "," +
						canceledFoods.get(i).getTasteNormalPrice() + ", '" +
						canceledFoods.get(i).tasteNormalPref + "', " + 
						(canceledFoods.get(i).tmpTaste == null ? "NULL" : canceledFoods.get(i).tmpTaste.aliasID) + ", " +
						(canceledFoods.get(i).tmpTaste == null ? "NULL" : ("'" + canceledFoods.get(i).tmpTaste.preference + "'")) + ", " +
						(canceledFoods.get(i).tmpTaste == null ? "NULL" : canceledFoods.get(i).tmpTaste.getPrice()) + ", " +
						canceledFoods.get(i).kitchen.dept.deptID + ", " + 
						"(SELECT kitchen_id FROM " + Params.dbName + ".kitchen WHERE restaurant_id=" + term.restaurantID + " AND kitchen_alias=" + canceledFoods.get(i).kitchen.aliasID + "), " + 
						canceledFoods.get(i).kitchen.aliasID + ", '" + 
						term.owner + "', " +
						"NOW(), " + 
						(canceledFoods.get(i).isTemporary ? 1 : 0) + ", " +
						(isPaidAgain ? 1 : 0) +
						")";
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
					sql = "UPDATE " + Params.dbName + ".terminal SET" +
					  " gift_amount = gift_amount + " + giftAmount +
					  " WHERE pin=" + "0x" + Long.toHexString(term.pin) +
					  " AND restaurant_id=" + term.restaurantID;
					dbCon.stmt.executeUpdate(sql);
				}
			}
			
			/**
			 * Update the related info to this order.
			 * Don't update the region and table status if the order has been paid before.
			 */
			sql = "UPDATE `" + Params.dbName + "`.`order` SET " +
					"custom_num=" + orderToUpdate.custom_num +	", " +
					"terminal_pin=" + term.pin + ", " +
					"is_paid=" + (isPaidAgain ? 1 : 0) + ", " +
					(isPaidAgain ? "" : "region_id=" + orderToUpdate.region.regionID + ", ") +
					(isPaidAgain ? "" : "region_name='" + orderToUpdate.region.name + "', ") +
					(isPaidAgain ? "" : "table_id=" + orderToUpdate.table.tableID + ", ") +
					(isPaidAgain ? "" : "table_alias=" + orderToUpdate.table.aliasID + ", ") +
					(isPaidAgain ? "" : "table_name='" + orderToUpdate.table.name + "', ") +
					"waiter='" + term.owner + "' " +
					"WHERE id=" + orderToUpdate.id;
			dbCon.stmt.executeUpdate(sql);
			
			/**
			 * Update the custom number to the merger table if the order has NOT been paid before.
			 */
			if(!isPaidAgain){
				if(orderToUpdate.category == Order.CATE_MERGER_TABLE){
					sql = "UPDATE " + Params.dbName + ".table SET " +
						  "status=" + Table.TABLE_BUSY + ", " +
						  "category=" + Order.CATE_MERGER_TABLE + ", " +
						  "custom_num=" + orderToUpdate.custom_num +
					  	  " WHERE restaurant_id=" + term.restaurantID + 
					  	  " AND table_alias=" + orderToUpdate.table2.aliasID;
					dbCon.stmt.executeUpdate(sql);				
				}
				
				/**
				 * Update the table status in tow cases.
				 * 1 - Transfer table
				 * 2 - Not transfer table
				 */
				if(orderToUpdate.table.aliasID != orderToUpdate.oriTbl.aliasID){
					// update the original table status to idle
					sql = "UPDATE " + Params.dbName + ".table SET status="
							+ Table.TABLE_IDLE + "," + "custom_num=NULL,"
							+ "category=NULL" + " WHERE restaurant_id="
							+ orderToUpdate.oriTbl.restaurantID + " AND table_alias="
							+ orderToUpdate.oriTbl.aliasID;
					dbCon.stmt.executeUpdate(sql);				
					
					// update the new table status to busy
					sql = "UPDATE " + Params.dbName + ".table SET " +
							  "status=" + Table.TABLE_BUSY + ", " +
							  "category=" + orderToUpdate.oriTbl.category + ", " +
							  "custom_num=" + orderToUpdate.custom_num + 
							  " WHERE restaurant_id=" + orderToUpdate.table.restaurantID + 
							  " AND table_alias=" + orderToUpdate.table.aliasID;
					dbCon.stmt.executeUpdate(sql);				
					
				}else{

					sql = "UPDATE " + Params.dbName + ".table SET " +
					      "status=" + Table.TABLE_BUSY + ", " +
						  "category=" + orderToUpdate.category + ", " +
						  "custom_num=" + orderToUpdate.custom_num +
						  " WHERE restaurant_id=" + term.restaurantID + 
						  " AND table_alias=" + orderToUpdate.table.aliasID;
					dbCon.stmt.executeUpdate(sql);				
				}				
			}
			
			dbCon.conn.commit();
			
			Result result = new Result();			
			/**
			 * Find the extra and canceled foods and put them to result.
			 */
			if(!extraFoods.isEmpty() || !canceledFoods.isEmpty()){			
				
				ArrayList<OrderFood> tmpFoods = new ArrayList<OrderFood>();
				
				Iterator<OrderFood> iterExtra;
				Iterator<OrderFood> iterCancel;
				
				/**
				 * Find the canceled foods to print
				 */
				tmpFoods.clear();
				iterCancel = canceledFoods.iterator();
				while(iterCancel.hasNext()){
					OrderFood canceledFood = iterCancel.next();
					
					boolean isCancelled = true;	
					iterExtra = extraFoods.iterator();
					while(iterExtra.hasNext()){
						OrderFood extraFood = iterExtra.next();
						/**
						 * If the food to cancel is hang up before.
						 * Check to see whether the same extra food is exist
						 * and the amount is equal or greater than the canceled.
						 * If so, means the extra food is immediate
						 * and NOT need to print this canceled food.
						 */
						if(canceledFood.hangStatus == OrderFood.FOOD_HANG_UP){
							if(extraFood.equals2(canceledFood) && 
							   extraFood.getCount().floatValue() >= canceledFood.getCount().floatValue()){
								
								isCancelled = false;
								extraFood.hangStatus = OrderFood.FOOD_IMMEDIATE;
								break;
							}
							
						/**
						 * In the case below, 
						 * 1 - food alias id is matched 
						 * 2 - order count is matched 
						 * 3 - the hang status is the same
						 * Means just change the taste preference to this food. 
						 * We don't print this record.
						 */
						}else if(canceledFood.aliasID == extraFood.aliasID &&
								 canceledFood.getCount().equals(extraFood.getCount()) &&
								 canceledFood.hangStatus == extraFood.hangStatus) {

							isCancelled = false;
							break;
						}
					}
					
					if(isCancelled){
						tmpFoods.add(canceledFood);
					}				
				}
				
				if(!tmpFoods.isEmpty()){
					result.canceledOrder = new Order();
					result.canceledOrder.id = orderToUpdate.id;
					result.canceledOrder.table = orderToUpdate.table;
					result.canceledOrder.custom_num = orderToUpdate.custom_num;
					result.canceledOrder.region = orderToUpdate.region;
					result.canceledOrder.foods = tmpFoods.toArray(new OrderFood[tmpFoods.size()]);
				}
				
				/**
				 * Find the extra foods to print
				 */
				tmpFoods.clear();
				iterExtra = extraFoods.listIterator();
				while(iterExtra.hasNext()){
					
					OrderFood extraFood = iterExtra.next();
					
					boolean isExtra = true;	
					/**
					 * In the case below, 
					 * 1 - food alias id is matched 
					 * 2 - order count is matched 
					 * 3 - the hang status is the same
					 * Means just change the taste preference to this food. 
					 * We don't print this record.
					 */
					iterCancel = canceledFoods.iterator();
					while(iterCancel.hasNext()){
						OrderFood canceledFood = iterCancel.next();
						if(extraFood.aliasID == canceledFood.aliasID &&
						   extraFood.getCount().equals(canceledFood.getCount()) &&
						   extraFood.hangStatus == canceledFood.hangStatus){
								isExtra = false;
								break;
						}
					}
					
					if(isExtra){
						tmpFoods.add(extraFood);
					}
				}
				
				if(!tmpFoods.isEmpty()){
					result.extraOrder = new Order();
					result.extraOrder.id = orderToUpdate.id;
					result.extraOrder.table = orderToUpdate.table;
					result.extraOrder.custom_num = orderToUpdate.custom_num;
					result.extraOrder.region = orderToUpdate.region;
					result.extraOrder.foods = tmpFoods.toArray(new OrderFood[tmpFoods.size()]);
				}
			}
			
			/**
			 * Find the hurried foods and put them to result.
			 */
			if(!hurriedFoods.isEmpty()){
				result.hurriedOrder = new Order();
				result.hurriedOrder.id = orderToUpdate.id;
				result.hurriedOrder.table = orderToUpdate.table;
				result.hurriedOrder.custom_num = orderToUpdate.custom_num;
				result.hurriedOrder.region = orderToUpdate.region;
				result.hurriedOrder.foods = hurriedFoods.toArray(new OrderFood[hurriedFoods.size()]);
			}		
			
			return result;
			
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
	 * Generate the food detail from the basic information defined in protocol.
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
	private static OrderFood genFoodDetail(DBCon dbCon, Terminal term, OrderFood foodBasic) throws BusinessException, SQLException{
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
		
		OrderFood food = new OrderFood();
		
		if(foodBasic.isTemporary){
			food.name = foodBasic.name;
			food.setPrice(foodBasic.getPrice());
			food.kitchen = foodBasic.kitchen;
			//food.setCount(foodBasic.getCount());
			
		}else{
			//get the food name and its unit price
			String sql = " SELECT " +
						 " FOOD.food_id AS food_id, FOOD.name AS name, FOOD.status AS status, " +
						 " FOOD.unit_price AS unit_price, FOOD.kitchen_id AS kitchen_id, FOOD.kitchen_alias AS kitchen_alias, " +
						 " DEPT.dept_id AS dept_id" +
						 " FROM " + Params.dbName + ".food FOOD " +
						 " LEFT JOIN " + Params.dbName + ".department DEPT ON " +
						 " DEPT.restaurant_id = FOOD.restaurant_id " +
						 " AND " +
						 " DEPT.dept_id = " + "(SELECT dept_id FROM " + Params.dbName + ".kitchen WHERE kitchen_id = FOOD.kitchen_id)" +
						 " WHERE " +
						 " FOOD.restaurant_id = " + term.restaurantID +
						 " AND " +
						 " FOOD.food_alias = " + foodBasic.aliasID;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			//check if the food to be inserted exist in db or not

			if(dbCon.rs.next()){
				food.foodID = dbCon.rs.getInt("food_id");
				food.status = dbCon.rs.getShort("status");
				food.name = dbCon.rs.getString("name");
				food.setPrice(new Float(dbCon.rs.getFloat("unit_price")));
				food.kitchen.kitchenID = dbCon.rs.getLong("kitchen_id");
				food.kitchen.aliasID = dbCon.rs.getShort("kitchen_alias");
				food.kitchen.dept.restaurantID = term.restaurantID;
				food.kitchen.dept.deptID = dbCon.rs.getShort("dept_id");
			}else{
				throw new BusinessException("The food(alias_id=" + foodBasic.aliasID + ", restaurant_id=" + term.restaurantID + ") to query does NOT exist.", ErrorCode.MENU_EXPIRED);
			}
			dbCon.rs.close();
			
			//get the each taste information to this food only if the food has taste preference
			for(int j = 0; j < foodBasic.tastes.length; j++){
				if(foodBasic.tastes[j].aliasID != Taste.NO_TASTE){
					sql = "SELECT taste_id, preference, price, category, rate, calc FROM " + Params.dbName + ".taste WHERE restaurant_id=" + term.restaurantID +
						" AND taste_alias=" + foodBasic.tastes[j].aliasID;
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					//check if the taste preference exist in db
					if(dbCon.rs.next()){
						food.tastes[j].tasteID = dbCon.rs.getInt("taste_id");
						food.tastes[j].aliasID = foodBasic.tastes[j].aliasID;
						food.tastes[j].preference = dbCon.rs.getString("preference");
						food.tastes[j].category = dbCon.rs.getShort("category");
						food.tastes[j].calc = dbCon.rs.getShort("calc");
						food.tastes[j].setRate(dbCon.rs.getFloat("rate"));
						food.tastes[j].setPrice(dbCon.rs.getFloat("price"));
					}else{
						throw new BusinessException("The taste(alias_id=" + foodBasic.tastes[j].aliasID + ", restaurant_id=" + term.restaurantID +") to query does NOT exist.", ErrorCode.MENU_EXPIRED);
					}
					dbCon.rs.close();
					
				}
			}
		}
		
		//set the alias id
		food.aliasID = foodBasic.aliasID;
		//set the discount
		food.setDiscount(foodBasic.getDiscount());
		//set the hang status
		food.hangStatus = foodBasic.hangStatus;
		//set the temporary flag
		food.isTemporary = foodBasic.isTemporary;
		//set the temporary taste
		food.tmpTaste = foodBasic.tmpTaste;
		
		//set the taste preference to this food
		food.tasteNormalPref = com.wireless.protocol.Util.genTastePref(food.tastes);
		//set the total taste price to this food
		food.setTasteNormalPrice(com.wireless.protocol.Util.genTastePrice(food.tastes, food.getPrice()));
		
		return food;
	}
	
}


