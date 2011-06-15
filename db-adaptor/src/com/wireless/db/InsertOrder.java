package com.wireless.db;

import java.sql.SQLException;
import java.sql.Statement;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.Table;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Util;

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
	public static Order exec(int pin, short model, Order orderToInsert) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
			
		try{
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, pin, model);
				
			Table table = QueryTable.exec(dbCon, pin, model, orderToInsert.table_id);
				
			if(table.status == Table.TABLE_IDLE){
				
				/**
				 * In the case of table merger,
				 * check to see if the table to be merger is idle or busy.
				 * Assure both tables to be merger remains in idle. 
				 */
				if(orderToInsert.category == Order.CATE_MERGER_TABLE){
					Table mergerTable = QueryTable.exec(dbCon, pin, model, orderToInsert.table2_id);
					if(mergerTable.status == Table.TABLE_BUSY){
						throw new BusinessException("The tabe(alias_id=" + orderToInsert.table2_id + ") to be mergerd is BUSY.", ErrorCode.TABLE_BUSY);
					}else{
						orderToInsert.table2_name = mergerTable.name;
					}
				}
				
				orderToInsert.table_name = table.name;
				
				String sql = null;
				/**
				 * Get all the food's detail info submitted by terminal, 
				 * and then check whether the food exist in db or is disabled by user.
				 * If the food doesn't exist in db or is disabled by user,
				 * then notify the terminal that the food menu is expired.
				 */
				for(int i = 0; i < orderToInsert.foods.length; i++){
	
					//get the associated foods' unit price and name
					sql = "SELECT unit_price, name, status FROM " +  Params.dbName + 
					 	  ".food WHERE alias_id=" + orderToInsert.foods[i].alias_id + 
					 	  " AND restaurant_id=" + table.restaurant_id + " AND enabled=1";
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					//check if the food exist in db 
					if(dbCon.rs.next()){
						orderToInsert.foods[i].name = dbCon.rs.getString("name");
						orderToInsert.foods[i].status = dbCon.rs.getShort("status");
						int val = (int)(dbCon.rs.getFloat("unit_price") * 100);
						int unitPrice = ((val / 100) << 8) | (val % 100);
						orderToInsert.foods[i].price = unitPrice;
					}else{
						throw new BusinessException("The food(alias_id=" + orderToInsert.foods[i].alias_id + ") to query doesn't exit.", ErrorCode.MENU_EXPIRED);
					}
					dbCon.rs.close();
						
					/**
					 * The special food does NOT discount
					 */
//					if(orderToInsert.foods[i].isSpecial()){
//						orderToInsert.foods[i].discount = 100;
//					}else{
//						//get the associated foods' discount
//						sql = "SELECT discount FROM " + Params.dbName + ".kitchen WHERE restaurant_id=" + 
//							  table.restaurant_id +
//							  " AND alias_id=" + orderToInsert.foods[i].kitchen;		
//						dbCon.rs = dbCon.stmt.executeQuery(sql);
//						if(dbCon.rs.next()){
//							orderToInsert.foods[i].discount = (byte)(dbCon.rs.getFloat("discount") * 100);
//						}						
//					}
						
					//get the taste preference according to the taste id,
					//only if the food has the taste preference
					if(orderToInsert.foods[i].taste.alias_id != Taste.NO_TASTE){
						sql = "SELECT preference, price FROM " + Params.dbName + 
							".taste WHERE restaurant_id=" + table.restaurant_id + 
							" AND alias_id=" + orderToInsert.foods[i].taste.alias_id;
						dbCon.rs = dbCon.stmt.executeQuery(sql);
						if(dbCon.rs.next()){
							orderToInsert.foods[i].taste.preference = dbCon.rs.getString("preference");
							orderToInsert.foods[i].taste.setPrice(dbCon.rs.getFloat("price"));
						}				
					}
				}
				//insert to order table
				sql = "INSERT INTO `" + Params.dbName + 
						"`.`order` (`id`, `restaurant_id`, `table_id`, `table_name`, `table2_id`, `table2_name`, `terminal_model`, `terminal_pin`, `order_date`, `custom_num`, `waiter`) VALUES (NULL, " + 
						table.restaurant_id + ", " + 
						orderToInsert.table_id + ", '" + 
						orderToInsert.table_name + "', " +
						(orderToInsert.category == Order.CATE_MERGER_TABLE ? orderToInsert.table2_id : "NULL") + ", " +
						(orderToInsert.category == Order.CATE_MERGER_TABLE ? "'" + orderToInsert.table2_name + "'" : "NULL") + ", " +
						term.modelID + ", " + 
						term.pin + 
						", NOW(), " + 
						orderToInsert.custom_num + ", '" + 
						term.owner + "')";
				dbCon.stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);
				//get the generated id to order 
				dbCon.rs = dbCon.stmt.getGeneratedKeys();
				if(dbCon.rs.next()){
					orderToInsert.id = dbCon.rs.getInt(1);
				}else{
					throw new SQLException("The id of order is not generated successfully.");
				}
					
				dbCon.stmt.clearBatch();
				//insert each ordered food
				for(int i = 0; i < orderToInsert.foods.length; i++){
						
					//insert the record to table "order_food"
					sql = "INSERT INTO `" + Params.dbName +
						"`.`order_food` (`order_id`, `food_id`, `order_count`, `unit_price`, `name`, `food_status`, `discount`, `taste`, `taste_price`, `taste_id`, `kitchen`, `waiter`, `order_date`) VALUES (" +	
						orderToInsert.id + ", " + 
						orderToInsert.foods[i].alias_id + ", " + 
						orderToInsert.foods[i].count2Float().toString() + ", " + 
						Util.price2Float(orderToInsert.foods[i].price, Util.INT_MASK_2).toString() + ", '" + 
						orderToInsert.foods[i].name + "', " +
						orderToInsert.foods[i].status + ", " +
						(float)orderToInsert.foods[i].discount / 100 + ", '" +
						orderToInsert.foods[i].taste.preference + "', " + 
						Util.price2Float(orderToInsert.foods[i].taste.price, Util.INT_MASK_2).toString() + ", " +
						orderToInsert.foods[i].taste.alias_id + ", " + 
						orderToInsert.foods[i].kitchen + ", '" + 
						term.owner + "', NOW()" + ")";
						
					dbCon.stmt.addBatch(sql);
				}		
				dbCon.stmt.executeBatch();
				
				return orderToInsert;
				
			}else if(table.status == Table.TABLE_BUSY){
				throw new BusinessException("The tabe(alias_id=" + orderToInsert.table_id + ") to be inserted order is BUSY.", ErrorCode.TABLE_BUSY);
				
			}else{
				throw new BusinessException("Unknown error occourred while inserting order.", ErrorCode.UNKNOWN);
			}
				
		}finally{
			dbCon.disconnect();
		}			
	}
}
