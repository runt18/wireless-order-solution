package com.wireless.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.menuMgr.QueryPricePlanDao;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.PricePlan;
import com.wireless.protocol.Table;
import com.wireless.protocol.Taste;
import com.wireless.protocol.TasteGroup;
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
		if(orderToInsert.isJoined()){
			Table newTable = new Table();
			newTable.name = "并" + Integer.toString(orderToInsert.destTbl.aliasID);
			orderToInsert.destTbl = InsertTable.exec(dbCon, term, newTable, true);
			
		}else if(orderToInsert.isTakeout()){
			Table newTable = new Table();
			orderToInsert.destTbl = InsertTable.exec(dbCon, term, newTable, true);		
			
		}else{
			orderToInsert.destTbl = QueryTable.exec(dbCon, term, orderToInsert.destTbl.aliasID);
		}
		
		if(orderToInsert.destTbl.isIdle()){
			
			List<OrderFood> newFoods = new ArrayList<OrderFood>(orderToInsert.foods.length);
			for(OrderFood newFood : orderToInsert.foods){
				
				//Skip the food whose order count is less than zero.
				if(newFood.getCount() > 0){				
					/**
					 * Get all the food's detail info submitted by terminal.
					 * If the food does NOT exist, tell the terminal that the food menu has been expired.
					 */
					if(newFood.isTemporary){
						Kitchen[] kitchens = QueryMenu.queryKitchens(dbCon, "AND KITCHEN.kitchen_alias=" + newFood.kitchen.aliasID + " AND KITCHEN.restaurant_id=" + term.restaurantID, null);
						if(kitchens.length > 0){
							newFood.kitchen = kitchens[0];
						}
						
					}else{					
						//get the associated foods' unit price and name
						Food[] detailFood = QueryMenu.queryFoods(dbCon, "AND FOOD.food_alias=" + newFood.getAliasId() + " AND FOOD.restaurant_id=" + term.restaurantID, null);
						if(detailFood.length > 0){
							newFood.foodID = detailFood[0].foodID;
							newFood.setAliasId(detailFood[0].getAliasId());
							newFood.restaurantID = detailFood[0].restaurantID;
							newFood.name = detailFood[0].name;
							newFood.setStatus(detailFood[0].getStatus());
							newFood.setPrice(detailFood[0].getPrice());
							newFood.kitchen = detailFood[0].kitchen;
							newFood.childFoods = detailFood[0].childFoods;
						}else{
							throw new BusinessException("The food(alias_id=" + newFood.getAliasId() + ", restaurant_id=" + term.restaurantID + ") to query does NOT exit.", ErrorCode.MENU_EXPIRED);
						}
						
						//Get the details to normal tastes
						if(newFood.hasNormalTaste()){
							Taste[] tastes; 
							//Get the detail to tastes.
							tastes = newFood.getTasteGroup().getTastes();
							for(int j = 0; j < tastes.length; j++){
								Taste[] detailTaste = QueryMenu.queryTastes(dbCon, 
																			Taste.CATE_ALL, 
																			" AND restaurant_id=" + term.restaurantID + " AND taste_alias =" + tastes[j].aliasID, 
																			null);

								if(detailTaste.length > 0){
									tastes[j] = detailTaste[0];
								}else{							
									throw new BusinessException("The taste(alias_id=" + tastes[j].aliasID + ", restaurant_id=" + term.restaurantID + ") to query does NOT exit.", ErrorCode.MENU_EXPIRED);
								}
									
							}
							//Get the detail to specs.
							tastes = newFood.getTasteGroup().getSpecs();
							for(int j = 0; j < tastes.length; j++){
								Taste[] detailTaste = QueryMenu.queryTastes(dbCon, 
																			Taste.CATE_ALL, 
																			" AND restaurant_id=" + term.restaurantID + " AND taste_alias =" + tastes[j].aliasID, 
																			null);

								if(detailTaste.length > 0){
									tastes[j] = detailTaste[0];
								}else{
									throw new BusinessException("The taste(alias_id=" + tastes[j].aliasID + ", restaurant_id=" + term.restaurantID + ") to query does NOT exit.", ErrorCode.MENU_EXPIRED);
								}
							}

						}
					}	
					
					newFoods.add(newFood);
				}
			}
			
			String sql = null;
			
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
			orderToInsert.region = QueryRegion.exec(dbCon, term, orderToInsert.destTbl.aliasID);

			/**
			 * Get the price plan which is in use to this restaurant
			 */
			PricePlan[] pricePlans = QueryPricePlanDao.exec(dbCon, " AND status = " + PricePlan.IN_USE + " AND restaurant_id = " + term.restaurantID, null);
			if(pricePlans.length > 0){
				orderToInsert.setPricePlan(pricePlans[0]);
			}
			
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
						"`table_id`, `table_alias`, `table_name`, " +
						"`terminal_model`, `terminal_pin`, `birth_date`, `order_date`, `custom_num`, `waiter`, `price_plan_id`) VALUES (" +
						"NULL, " + 
						orderToInsert.destTbl.restaurantID + ", " + 
						orderToInsert.getCategory() + ", " +
						orderToInsert.region.regionID + ", '" +
						orderToInsert.region.name + "', " +
						orderToInsert.destTbl.tableID + ", " +
						orderToInsert.destTbl.aliasID + ", '" + 
						orderToInsert.destTbl.name + "', " +
						term.modelID + ", " + 
						term.pin + ", " +
						" NOW() " + ", " + 
						" NOW() " + ", " +
						orderToInsert.customNum + ", " +
						"'" + term.owner + "'" + ", " +
						orderToInsert.getPricePlan().getId() + ")";
				dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
				//get the generated id to order 
				dbCon.rs = dbCon.stmt.getGeneratedKeys();
				if(dbCon.rs.next()){
					orderToInsert.id = dbCon.rs.getInt(1);
				}else{
					throw new SQLException("The id of order is not generated successfully.");
				}				

				/**
				 * Update the table status to busy.
				 */
				sql = " UPDATE " + Params.dbName + ".table SET " +
					  " status = " + Table.TABLE_BUSY + ", " +
					  " category = " + orderToInsert.getCategory() + ", " +
					  " custom_num = " + orderToInsert.customNum +
					  " WHERE restaurant_id = " + term.restaurantID + 
					  " AND table_alias = " + orderToInsert.destTbl.aliasID;
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
				for(OrderFood foodToInsert : newFoods){
					

					if(foodToInsert.hasTaste()){
						
						TasteGroup tg = foodToInsert.getTasteGroup();
						
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
							  (tg.hasTmpTaste() ? "'" + tg.getTmpTastePref() + "'" : "NULL") + ", " +
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
									  " taste_group_id = " + tg.getGroupId() + "), " +
									  normalTaste.tasteID + 
									  " ) ";
								dbCon.stmt.executeUpdate(sql);
							}
						}
						
					}
						
					//insert the record to table "order_food"
					sql = " INSERT INTO `" + Params.dbName + "`.`order_food` " +
						  " ( " +
						  " `restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_count`, `unit_price`, `name`, " +
						  " `food_status`, `hang_status`, `discount`, `taste_group_id`, " +
						  " `dept_id`, `kitchen_id`, `kitchen_alias`, " +
						  " `waiter`, `order_date`, `is_temporary` " +
						  " ) " +
						  " VALUES " +
						  " ( " +	
						  term.restaurantID + ", " +
						  orderToInsert.id + ", " +
						  (foodToInsert.foodID == 0 ? "NULL" : foodToInsert.foodID) + ", " +
						  foodToInsert.getAliasId() + ", " + 
						  foodToInsert.getCount() + ", " + 
						  foodToInsert.getPrice() + ", '" + 
						  foodToInsert.name + "', " +
						  foodToInsert.getStatus() + ", " +
						  (foodToInsert.hangStatus == OrderFood.FOOD_HANG_UP ? OrderFood.FOOD_HANG_UP : OrderFood.FOOD_NORMAL) + ", " +
						  foodToInsert.getDiscount() + ", " +
						  (foodToInsert.hasTaste() ? foodToInsert.getTasteGroup().getGroupId() : TasteGroup.EMPTY_TASTE_GROUP_ID) + ", " +
						  foodToInsert.kitchen.dept.deptID + ", " +
						  foodToInsert.kitchen.kitchenID + ", " +
						  foodToInsert.kitchen.aliasID + ", '" + 
						  term.owner + "', NOW(), " + 
						  (foodToInsert.isTemporary ? "1" : "0") + 
						  " ) ";
						
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
			
		}else if(orderToInsert.destTbl.isBusy()){
			throw new BusinessException("The table(alias_id=" + orderToInsert.destTbl.aliasID + ", restaurant_id=" + term.restaurantID + ") to insert order is BUSY.", ErrorCode.TABLE_BUSY);
			
		}else{
			throw new BusinessException("Unknown error occourred while inserting order.", ErrorCode.UNKNOWN);
		}
	}
}
