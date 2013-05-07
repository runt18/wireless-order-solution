package com.wireless.db.frontBusiness;

import java.sql.SQLException;
import java.sql.Statement;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
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
		
		doPrepare(dbCon, term, orderToInsert);
		
		/**
		 * Put all the INSERT statements into a database transition so as to assure 
		 * the status to both table and order is consistent. 
		 */
		boolean isAutoCommit = dbCon.conn.getAutoCommit();
		try{				
			
			dbCon.conn.setAutoCommit(false);
			
			doInsert(dbCon, term, orderToInsert);
			
			dbCon.conn.commit();
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}catch(Exception e){
			dbCon.conn.rollback();
			throw new SQLException(e);
			
		}finally{
			dbCon.conn.setAutoCommit(isAutoCommit);
		}
		
		return orderToInsert;		

	}
	
	/**
	 * Insert a new order without database transaction.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param orderToInsert
	 * 			the order along with basic insert parameters
	 * @return the completed order details to insert
	 * @throws BusinessException
	 *          Throws if one of cases below.<br>
	 *          - The terminal is NOT attached to any restaurant.<br>
	 *          - The terminal is expired.<br>
	 *          - The table associated with this order does NOT exist.<br>
	 *          - The table associated with this order is BUSY.<br>
	 *          - Any food query to insert does NOT exist.<br>
	 *          - Any food to this order does NOT exist.<br>
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statements.
	 */
	public static Order execAsync(DBCon dbCon, Terminal term, Order orderToInsert) throws BusinessException, SQLException{
		doPrepare(dbCon, term, orderToInsert);
		doInsert(dbCon, term, orderToInsert);
		return orderToInsert;
	}
	
	/**
	 * Prepare to fill the details to order inserted.
	 * The SQL statements should only be the SELECT type. 
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param orderToInsert
	 * 			the order along with basic insert parameters
	 * @return the completed order details to insert
	 * @throws BusinessException
	 *          Throws if one of cases below.<br>
	 *          - The terminal is NOT attached to any restaurant.<br>
	 *          - The terminal is expired.<br>
	 *          - The table associated with this order does NOT exist.<br>
	 *          - The table associated with this order is NOT idle.<br>
	 *          - Any food query to insert does NOT exist.<br>
	 *          - Any food to this order does NOT exist.<br>
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statements.
	 */
	private static void doPrepare(DBCon dbCon, Terminal term, Order orderToInsert) throws BusinessException, SQLException{
		
		orderToInsert.setDestTbl(TableDao.getTableByAlias(dbCon, term, orderToInsert.getDestTbl().getAliasId()));
		
		if(orderToInsert.getDestTbl().isIdle()){
			
			OrderFood[] foodsToInsert = orderToInsert.getOrderFoods();
			for(int i = 0; i < foodsToInsert.length; i++){
				
				//Skip the food whose order count is less than zero.
				if(foodsToInsert[i].getCount() > 0){				
					/**
					 * Get all the food's detail info submitted by terminal.
					 * If the food does NOT exist, tell the terminal that the food menu has been expired.
					 */
					if(foodsToInsert[i].isTemp()){
						foodsToInsert[i].setKitchen(KitchenDao.getKitchenByAlias(dbCon, term, foodsToInsert[i].getKitchen().getAliasId()));
						
					}else{					
						//get the associated foods' unit price and name
						Food[] detailFood = QueryMenu.queryFoods(dbCon, "AND FOOD.food_alias=" + foodsToInsert[i].getAliasId() + " AND FOOD.restaurant_id=" + term.restaurantID, null);
						if(detailFood.length > 0){
							foodsToInsert[i].setFoodId(detailFood[0].getFoodId());
							foodsToInsert[i].setAliasId(detailFood[0].getAliasId());
							foodsToInsert[i].setRestaurantId(detailFood[0].getRestaurantId());
							foodsToInsert[i].setName(detailFood[0].getName());
							foodsToInsert[i].setStatus(detailFood[0].getStatus());
							foodsToInsert[i].setPrice(detailFood[0].getPrice());
							foodsToInsert[i].setKitchen(detailFood[0].getKitchen());
							foodsToInsert[i].setChildFoods(detailFood[0].getChildFoods());
						}else{
							throw new BusinessException("The food(alias_id=" + foodsToInsert[i].getAliasId() + ", restaurant_id=" + term.restaurantID + ") to query does NOT exit.", ProtocolError.MENU_EXPIRED);
						}
						
						//Get the details to normal tastes
						if(foodsToInsert[i].hasNormalTaste()){
							Taste[] tastes; 
							//Get the detail to tastes.
							tastes = foodsToInsert[i].getTasteGroup().getTastes();
							for(int j = 0; j < tastes.length; j++){
								Taste[] detailTaste = QueryMenu.queryTastes(dbCon, 
																			Taste.CATE_ALL, 
																			" AND restaurant_id=" + term.restaurantID + " AND taste_alias =" + tastes[j].getAliasId(), 
																			null);

								if(detailTaste.length > 0){
									tastes[j] = detailTaste[0];
								}else{							
									throw new BusinessException("The taste(alias_id=" + tastes[j].getAliasId() + ", restaurant_id=" + term.restaurantID + ") to query does NOT exit.", ProtocolError.MENU_EXPIRED);
								}
									
							}
							//Get the detail to specs.
							tastes = foodsToInsert[i].getTasteGroup().getSpecs();
							for(int j = 0; j < tastes.length; j++){
								Taste[] detailTaste = QueryMenu.queryTastes(dbCon, 
																			Taste.CATE_ALL, 
																			" AND restaurant_id=" + term.restaurantID + " AND taste_alias =" + tastes[j].getAliasId(), 
																			null);

								if(detailTaste.length > 0){
									tastes[j] = detailTaste[0];
								}else{
									throw new BusinessException("The taste(alias_id=" + tastes[j].getAliasId() + ", restaurant_id=" + term.restaurantID + ") to query does NOT exit.", ProtocolError.MENU_EXPIRED);
								}
							}

						}
					}					
				}
			}

			//Get the price plan which is active to this restaurant
			orderToInsert.setPricePlan(PricePlanDao.getActivePricePlan(dbCon, term));
			
		}else if(orderToInsert.getDestTbl().isBusy()){
			throw new BusinessException("The " + orderToInsert.getDestTbl() + " to insert order is BUSY.", ProtocolError.TABLE_BUSY);
			
		}else{
			throw new BusinessException("Unknown error occourred while inserting order.", ProtocolError.UNKNOWN);
		}
		
	}
	

	/**
	 * Prepare to insert the order to database.
	 * The SQL statements should only be the INSERT, UPDATE or DELETE type. 
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param orderToInsert
	 * 			the order along with basic insert parameters
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statements.
	 */
	private static void doInsert(DBCon dbCon, Terminal term, Order orderToInsert) throws SQLException{
		
		String sql; 

		/**
		 * Insert to 'order' table.
		 */
		sql = " INSERT INTO `" + Params.dbName + "`.`order` (" +
			  " `id`, `restaurant_id`, `category`, `region_id`, `region_name`, " +
			  " `table_id`, `table_alias`, `table_name`, " +
			  " `terminal_model`, `terminal_pin`, `birth_date`, `order_date`, `custom_num`, `waiter`, `price_plan_id`) VALUES (" +
			  " NULL, " + 
			  orderToInsert.getDestTbl().getRestaurantId() + ", " + 
			  orderToInsert.getCategory() + ", " +
			  orderToInsert.getRegion().getRegionId() + ", '" +
			  orderToInsert.getRegion().getName() + "', " +
			  orderToInsert.getDestTbl().getTableId() + ", " +
			  orderToInsert.getDestTbl().getAliasId() + ", " +
			  "'" + orderToInsert.getDestTbl().getName() + "'" + ", " +
			  term.modelID + ", " + 
			  term.pin + ", " +
			  " NOW() " + ", " + 
			  " NOW() " + ", " +
			  orderToInsert.getCustomNum() + ", " +
			  "'" + term.owner + "'" + ", " +
			  orderToInsert.getPricePlan().getId() + ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		//get the generated id to order 
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			orderToInsert.setId(dbCon.rs.getInt(1));
		}else{
			throw new SQLException("The id of order is not generated successfully.");
		}				

		/**
		 * Update the table status to busy.
		 */
		sql = " UPDATE " + Params.dbName + ".table SET " +
			  " status = " + Table.Status.BUSY.getVal() + ", " +
			  " category = " + orderToInsert.getCategory() + ", " +
			  " custom_num = " + orderToInsert.getCustomNum() +
			  " WHERE restaurant_id = " + term.restaurantID + 
			  " AND table_alias = " + orderToInsert.getDestTbl().getAliasId();
		dbCon.stmt.executeUpdate(sql);
		
		//Otherwise update the gift amount if the gift quota is set if gift amount dose NOT reach the quota.
		float giftAmount = orderToInsert.calcGiftPrice().floatValue();
		if(term.getGiftQuota() >= 0){
			if((giftAmount + term.getGiftAmount()) <= term.getGiftQuota()){
				sql = " UPDATE " + Params.dbName + ".terminal SET " +
					  " gift_amount = gift_amount + " + giftAmount +
					  " WHERE pin = " + "0x" + Long.toHexString(term.pin) +
					  " AND restaurant_id = " + term.restaurantID;
				dbCon.stmt.executeUpdate(sql);
			}
		}
		
		/**
		 * Insert the detail records to 'order_food' table
		 */
		for(OrderFood foodToInsert : orderToInsert.getOrderFoods()){
			

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
					  (tg.hasTmpTaste() ? tg.getTmpTaste().getAliasId() : "NULL") + ", " +
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
							  normalTaste.getTasteId() + 
							  " ) ";
						dbCon.stmt.executeUpdate(sql);
					}
				}
				
			}
				
			//insert the record to table "order_food"
			sql = " INSERT INTO `" + Params.dbName + "`.`order_food` " +
				  " ( " +
				  " `restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_count`, `unit_price`, `name`, " +
				  " `food_status`, `discount`, `taste_group_id`, " +
				  " `dept_id`, `kitchen_id`, `kitchen_alias`, " +
				  " `waiter`, `order_date`, `is_temporary` " +
				  " ) " +
				  " VALUES " +
				  " ( " +	
				  term.restaurantID + ", " +
				  orderToInsert.getId() + ", " +
				  (foodToInsert.getFoodId() == 0 ? "NULL" : foodToInsert.getFoodId()) + ", " +
				  foodToInsert.getAliasId() + ", " + 
				  foodToInsert.getCount() + ", " + 
				  foodToInsert.getPrice() + ", '" + 
				  foodToInsert.getName() + "', " +
				  foodToInsert.getStatus() + ", " +
				  foodToInsert.getDiscount() + ", " +
				  (foodToInsert.hasTaste() ? foodToInsert.getTasteGroup().getGroupId() : TasteGroup.EMPTY_TASTE_GROUP_ID) + ", " +
				  foodToInsert.getKitchen().getDept().getId() + ", " +
				  foodToInsert.getKitchen().getId() + ", " +
				  foodToInsert.getKitchen().getAliasId() + ", '" + 
				  term.owner + "', NOW(), " + 
				  (foodToInsert.isTemp() ? "1" : "0") + 
				  " ) ";
				
			dbCon.stmt.executeUpdate(sql);
		}
	}
}
