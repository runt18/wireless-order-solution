package com.wireless.db;

import java.sql.SQLException;
import java.sql.Statement;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Table;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Terminal;

public class InsertOrder {
	/**
	 * Insert a new order according to the specific order detail information.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param orderToInsert the order information submitted by terminal, 
	 * 						refer to class "ReqInsertOrder" for more detail about what information the order contains 
	 * @throws BusinessException throws if one of cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The table associated with this order does NOT exist.<br>
	 * 							 - The table associated with this order is BUSY.<br>
	 * 							 - Any food query to insert does NOT exist.<br>
	 * 							 - Any food to this order does NOT exist.<br>
	 * @throws SQLException throws if fail to execute any SQL statement
	 * @return Order completed information to inserted order
	 */
	public static Order exec(long pin, short model, Order orderToInsert) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
			
		try{
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			
			return exec(dbCon, term, orderToInsert);
				
		}finally{
			dbCon.disconnect();
		}			
	}
	
	/**
	 * Insert a new order according to the specific order detail information.
	 * 
	 * @param term
	 *            the terminal to query
	 * @param orderToInsert
	 *            the order information submitted by terminal, refer to class
	 *            "ReqInsertOrder" for more detail about what information the
	 *            order contains
	 * @throws BusinessException
	 *             throws if one of cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The table associated with this order does NOT exist.<br>
	 *             - The table associated with this order is BUSY.<br>
	 *             - Any food query to insert does NOT exist.<br>
	 *             - Any food to this order does NOT exist.<br>
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @return Order completed information to inserted order
	 */
	public static Order exec(Terminal term, Order orderToInsert) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			return exec(dbCon, term, orderToInsert);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new order according to the specific order detail information.
	 * Note that the database should be connected before invoking this method
	 * 
	 * @param dbCon
	 * 			  the database connection
	 * @param term
	 *            the terminal to query
	 * @param orderToInsert
	 *            the order information submitted by terminal, refer to class
	 *            "ReqInsertOrder" for more detail about what information the
	 *            order contains
	 * @throws BusinessException
	 *             throws if one of cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The table associated with this order does NOT exist.<br>
	 *             - The table associated with this order is BUSY.<br>
	 *             - Any food query to insert does NOT exist.<br>
	 *             - Any food to this order does NOT exist.<br>
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @return Order completed information to inserted order
	 */
	public static Order exec(DBCon dbCon, Terminal term, Order orderToInsert) throws BusinessException, SQLException{
		/**
		 * Create a temporary table in the case of "并台" and "外卖". 
		 * The order would be attached with this new table.
		 */
		if(orderToInsert.category == Order.CATE_JOIN_TABLE){
			Table newTable = new Table();
			newTable.name = "并" + Integer.toString(orderToInsert.table.aliasID);
			orderToInsert.table = InsertTable.exec(dbCon, term, newTable, true);
			
		}else if(orderToInsert.category == Order.CATE_TAKE_OUT){
			Table newTable = new Table();
			orderToInsert.table = InsertTable.exec(dbCon, term, newTable, true);		
			
		}else{
			orderToInsert.table = QueryTable.exec(dbCon, term, orderToInsert.table.aliasID);
		}		
			
		if(orderToInsert.table.status == Table.TABLE_IDLE){
			
			/**
			 * In the case of table merger,
			 * check to see if the table to be merger is idle or busy.
			 * Assure both tables to be merger remains in idle. 
			 */
			if(orderToInsert.category == Order.CATE_MERGER_TABLE){
				orderToInsert.table2 = QueryTable.exec(dbCon, term, orderToInsert.table2.aliasID);
				if(orderToInsert.table2.status == Table.TABLE_BUSY){
					throw new BusinessException("The tabe(alias_id=" + orderToInsert.table2.aliasID + ") to be mergerd is BUSY.", ErrorCode.TABLE_BUSY);
				}
			}
			
			//orderToInsert.table_name = table.name;
			
			String sql = null;
			/**
			 * Get all the food's detail info submitted by terminal, 
			 * and then check whether the food exist in db.
			 * If the food doesn't exist in db or is disabled by user,
			 * then notify the terminal that the food menu is expired.
			 */
			for(int i = 0; i < orderToInsert.foods.length; i++){
				
				/**
				 * Not to get the detail if the submitted food is temporary,
				 * since the submitted string has contained the details, like name, price and amount. 
				 */
				if(!orderToInsert.foods[i].isTemporary){
					
					//get the associated foods' unit price and name
					Food[] detailFood = QueryMenu.queryFoods(dbCon, " AND FOOD.food_alias=" + orderToInsert.foods[i].aliasID + " AND FOOD.restaurant_id=" + term.restaurantID, null);
					if(detailFood.length > 0){
						orderToInsert.foods[i].foodID = detailFood[0].foodID;
						orderToInsert.foods[i].aliasID = detailFood[0].aliasID;
						orderToInsert.foods[i].restaurantID = detailFood[0].restaurantID;
						orderToInsert.foods[i].name = detailFood[0].name;
						orderToInsert.foods[i].status = detailFood[0].status;
						orderToInsert.foods[i].setPrice(detailFood[0].getPrice());
						orderToInsert.foods[i].kitchen = detailFood[0].kitchen;
						orderToInsert.foods[i].subFoods = detailFood[0].subFoods;
					}else{
						throw new BusinessException("The food(alias_id=" + orderToInsert.foods[i].aliasID + ", restaurant_id=" + orderToInsert.table.restaurantID+ ") to query doesn't exit.", ErrorCode.MENU_EXPIRED);
					}
					
					//get three taste information for each food
					for(int j = 0; j < orderToInsert.foods[i].tastes.length; j++){
						if(orderToInsert.foods[i].tastes[j].aliasID != Taste.NO_TASTE){
							
							Taste[] detailTaste = QueryMenu.queryTastes(dbCon, 
									Taste.CATE_ALL, 
									" AND restaurant_id=" + term.restaurantID + " AND taste_alias =" + orderToInsert.foods[i].tastes[j].aliasID, 
									null);
							
							if(detailTaste.length > 0){
								orderToInsert.foods[i].tastes[j] = detailTaste[0];
							}
						}
					}
					
					//set the taste preference
					orderToInsert.foods[i].tasteNormalPref = com.wireless.protocol.Util.genTastePref(orderToInsert.foods[i].tastes);
					//set the total taste price
					orderToInsert.foods[i].setTasteNormalPrice(com.wireless.protocol.Util.genTastePrice(orderToInsert.foods[i].tastes, 
																							 orderToInsert.foods[i].getPrice()));
				}					
			}
			
			/**
			 * Throw a business exception if gift amount reach the quota.
			 */
			float giftAmount = orderToInsert.calcGiftPrice().floatValue();
			if(term.getGiftQuota() >= 0){
				if((giftAmount + term.getGiftAmount()) > term.getGiftQuota()){
					throw new BusinessException("The gift amount exceeds the quota.", ErrorCode.EXCEED_GIFT_QUOTA);						
				}
			}

			/**
			 * Get the region to this table
			 */
			orderToInsert.region = QueryRegion.exec(dbCon, term, orderToInsert.table.aliasID);

			/**
			 * Put all the INSERT statements into a database transition so as to assure 
			 * the status to both table and order is consistent. 
			 */
			try{
				
				dbCon.conn.setAutoCommit(false);
				
				/**
				 * Insert to 'order' table.
				 */
				sql = "INSERT INTO `" + Params.dbName + "`.`order` (" +
						"`id`, `restaurant_id`, `category`, `region_id`, `region_name`, " +
						"`table_id`, `table_alias`, `table_name`, `table2_id`, `table2_alias`, `table2_name`, " +
						"`terminal_model`, `terminal_pin`, `order_date`, `custom_num`, `waiter`) VALUES (" +
						"NULL, " + 
						orderToInsert.table.restaurantID + ", " + 
						orderToInsert.category + ", " +
						orderToInsert.region.regionID + ", '" +
						orderToInsert.region.name + "', " +
						orderToInsert.table.tableID + ", " +
						orderToInsert.table.aliasID + ", '" + 
						orderToInsert.table.name + "', " +
						(orderToInsert.category == Order.CATE_MERGER_TABLE ? orderToInsert.table2.tableID : "NULL") + ", " +
						(orderToInsert.category == Order.CATE_MERGER_TABLE ? orderToInsert.table2.aliasID : "NULL") + ", " +
						(orderToInsert.category == Order.CATE_MERGER_TABLE ? "'" + orderToInsert.table2.name + "'" : "NULL") + ", " +
						term.modelID + ", " + 
						term.pin + 
						", NOW(), " + 
						orderToInsert.custom_num + ", '" + 
						term.owner + "')";
				dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
				//get the generated id to order 
				dbCon.rs = dbCon.stmt.getGeneratedKeys();
				if(dbCon.rs.next()){
					orderToInsert.id = dbCon.rs.getInt(1);
				}else{
					throw new SQLException("The id of order is not generated successfully.");
				}				
				
				/**
				 * Update the 2nd table to busy if the category is for merger
				 */
				if(orderToInsert.category == Order.CATE_MERGER_TABLE){
					sql = "UPDATE " + Params.dbName + ".table SET " +
						  "status=" + Table.TABLE_BUSY + ", " +
						  "category=" + orderToInsert.category + ", " +
						  "custom_num=" + orderToInsert.custom_num +
						  " WHERE restaurant_id=" + term.restaurantID +
						  " AND table_alias=" + orderToInsert.table2.aliasID;
					dbCon.stmt.executeUpdate(sql);
				}
				/**
				 * Update the table status to busy.
				 */
				sql = "UPDATE " + Params.dbName + ".table SET " +
					  "status=" + Table.TABLE_BUSY + ", " +
					  "category=" + orderToInsert.category + ", " +
					  "custom_num=" + orderToInsert.custom_num +
					  " WHERE restaurant_id=" + term.restaurantID + 
					  " AND table_alias=" + orderToInsert.table.aliasID;
				dbCon.stmt.executeUpdate(sql);
				
				/**
				 * Update the gift amount if the gift quota is set.
				 */
				if(term.getGiftQuota() >= 0){
					sql = "UPDATE " + Params.dbName + ".terminal SET" +
							  " gift_amount = gift_amount + " + giftAmount +
							  " WHERE pin=" + "0x" + Long.toHexString(term.pin) +
							  " AND restaurant_id=" + term.restaurantID;
					dbCon.stmt.executeUpdate(sql);
				}
				
				/**
				 * Insert the detail records to 'order_food' table
				 */
				for(int i = 0; i < orderToInsert.foods.length; i++){
						
					//insert the record to table "order_food"
					sql = "INSERT INTO `" + Params.dbName + "`.`order_food` (" +
							"`restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_count`, `unit_price`, `name`, " +
							"`food_status`, `hang_status`, `discount`, `taste`, `taste_price`, " +
							"`taste_id`, `taste2_id`, `taste3_id` ," +
							"`taste_alias`, `taste2_alias`, `taste3_alias`, " +
							"`taste_tmp_alias`, `taste_tmp`, `taste_tmp_price`, " +
							"`dept_id`, `kitchen_id`, `kitchen_alias`, " +
							"`waiter`, `order_date`, `is_temporary`) VALUES (" +	
							term.restaurantID + ", " +
							orderToInsert.id + ", " +
							(orderToInsert.foods[i].foodID == 0 ? "NULL" : orderToInsert.foods[i].foodID) + ", " +
							orderToInsert.foods[i].aliasID + ", " + 
							orderToInsert.foods[i].getCount() + ", " + 
							orderToInsert.foods[i].getPrice() + ", '" + 
							orderToInsert.foods[i].name + "', " +
							orderToInsert.foods[i].status + ", " +
							(orderToInsert.foods[i].hangStatus == OrderFood.FOOD_HANG_UP ? OrderFood.FOOD_HANG_UP : OrderFood.FOOD_NORMAL) + ", " +
							orderToInsert.foods[i].getDiscount() + ", '" +
							orderToInsert.foods[i].tasteNormalPref + "', " + 
							orderToInsert.foods[i].getTasteNormalPrice() + ", " +
							(orderToInsert.foods[i].tastes[0].tasteID == 0 ? "NULL" : orderToInsert.foods[i].tastes[0].tasteID) + ", " +
							(orderToInsert.foods[i].tastes[1].tasteID == 0 ? "NULL" : orderToInsert.foods[i].tastes[1].tasteID) + ", " +
							(orderToInsert.foods[i].tastes[2].tasteID == 0 ? "NULL" : orderToInsert.foods[i].tastes[2].tasteID) + ", " +
							orderToInsert.foods[i].tastes[0].aliasID + ", " + 
							orderToInsert.foods[i].tastes[1].aliasID + ", " + 
							orderToInsert.foods[i].tastes[2].aliasID + ", " +
							(orderToInsert.foods[i].tmpTaste == null ? "NULL" : orderToInsert.foods[i].tmpTaste.aliasID) + ", " +
							(orderToInsert.foods[i].tmpTaste == null ? "NULL" : ("'" + orderToInsert.foods[i].tmpTaste.preference + "'")) + ", " +
							(orderToInsert.foods[i].tmpTaste == null ? "NULL" : orderToInsert.foods[i].tmpTaste.getPrice()) + ", " +
							orderToInsert.foods[i].kitchen.dept.deptID + ", " +
							"(SELECT kitchen_id FROM " + Params.dbName + ".kitchen WHERE restaurant_id=" + term.restaurantID + " AND kitchen_alias=" + orderToInsert.foods[i].kitchen.aliasID + "), " + 
							orderToInsert.foods[i].kitchen.aliasID + ", '" + 
							term.owner + "', NOW(), " + 
							(orderToInsert.foods[i].isTemporary ? "1" : "0") + ")";
						
					dbCon.stmt.executeUpdate(sql);
				}
				
				dbCon.conn.commit();
				
			}catch(SQLException e){
				dbCon.conn.rollback();
				throw e;
				
			}catch(Exception e){
				dbCon.conn.rollback();
				throw new SQLException(e);
				
			}finally{
				dbCon.conn.setAutoCommit(true);
			}
			
			return orderToInsert;
			
		}else if(orderToInsert.table.status == Table.TABLE_BUSY){
			throw new BusinessException("The table(alias_id=" + orderToInsert.table.aliasID + ", restaurant_id=" + term.restaurantID + ") to insert order is BUSY.", ErrorCode.TABLE_BUSY);
			
		}else{
			throw new BusinessException("Unknown error occourred while inserting order.", ErrorCode.UNKNOWN);
		}
	}
}
