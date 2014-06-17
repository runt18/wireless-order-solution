package com.wireless.db.frontBusiness;

import java.sql.SQLException;
import java.sql.Statement;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.OrderFoodDao;
import com.wireless.db.orderMgr.TasteGroupDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.exception.ProtocolError;
import com.wireless.exception.StaffError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.TasteGroup;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.util.DateType;

public class InsertOrder {
	
	/**
	 * Insert a new order according to the specific order detail information.
	 * 
	 * @param staff
	 *            the staff to perform this action
	 * @param orderToInsert
	 *            the order information submitted by terminal, refer to class
	 *            "ReqInsertOrder" for more detail about what information the
	 *            order contains
	 * @throws BusinessException
	 *             throws if one of cases below
	 *             <li>the table associated with this order does NOT exist
	 *             <li>the table associated with this order is BUSY
	 *             <li>any food query to insert does NOT exist
	 *             <li>any food to this order does NOT exist
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @return Order completed information to inserted order
	 */
	public static Order exec(Staff staff, Order orderToInsert) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			return exec(dbCon, staff, orderToInsert);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new order according to the specific order detail information.
	 * 
	 * @param dbCon
	 * 			  the database connection
	 * @param staff
	 *            the staff to perform this action
	 * @param orderToInsert
	 *            the order information submitted by terminal, refer to class
	 *            "ReqInsertOrder" for more detail about what information the
	 *            order contains
	 * @throws BusinessException
	 *             throws if one of cases below
	 *             <li>the table associated with this order does NOT exist
	 *             <li>the table associated with this order is BUSY
	 *             <li>any food query to insert does NOT exist
	 *             <li>any food to this order does NOT exist
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @return Order completed information to inserted order
	 */
	public static Order exec(DBCon dbCon, Staff staff, Order orderToInsert) throws BusinessException, SQLException{
		
		doPrepare(dbCon, staff, orderToInsert);
		
		/**
		 * Put all the INSERT statements into a database transition so as to assure 
		 * the status to both table and order is consistent. 
		 */
		boolean isAutoCommit = dbCon.conn.getAutoCommit();
		try{				
			
			dbCon.conn.setAutoCommit(false);
			
			doInsert(dbCon, staff, orderToInsert);
			
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
	 * Prepare to fill the details to order inserted.
	 * The SQL statements should only be the SELECT type. 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param orderToInsert
	 * 			the order along with basic insert parameters
	 * @return the completed order details to insert
	 * @throws BusinessException
	 *          throws if one of cases below
	 *          <li>the table associated with this order does NOT exist
	 *          <li>the table associated with this order is NOT idle
	 *          <li>any food query to insert does NOT exist
	 *          <li>any food to this order does NOT exist
	 *          <li>the staff has no privilege to add the food
	 *          <li>the staff has no privilege to present the food
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	private static void doPrepare(DBCon dbCon, Staff staff, Order orderToInsert) throws BusinessException, SQLException{
		
		//Check to see whether the staff has the privilege to add the food 
		if(!staff.getRole().hasPrivilege(Privilege.Code.ADD_FOOD)){
			throw new BusinessException(StaffError.ORDER_NOT_ALLOW);
		}
		
		orderToInsert.setDestTbl(TableDao.getTableByAlias(dbCon, staff, orderToInsert.getDestTbl().getAliasId()));
		
		if(orderToInsert.getDestTbl().isIdle()){
			
			for(OrderFood of : orderToInsert.getOrderFoods()){
				//Skip the food whose order count is less than zero.
				if(of.getCount() > 0){				
					if(of.isTemp()){
						of.asFood().setKitchen(KitchenDao.getById(dbCon, staff, of.getKitchen().getId()));
						
					}else{		
						//Get the details to each order food.
						of.asFood().copyFrom(FoodDao.getById(dbCon, staff, of.getFoodId()));
						
						//Check to see whether the staff has the privilege to present the food.
						if(of.isGift() && !staff.getRole().hasPrivilege(Privilege.Code.GIFT)){
							throw new BusinessException(StaffError.GIFT_NOT_ALLOW);
						}
						
						//Get the details to normal tastes.
						if(of.hasNormalTaste()){
							//Get the detail to each taste.
							for(Taste t : of.getTasteGroup().getTastes()){
								t.copyFrom(TasteDao.getTasteById(dbCon, staff, t.getTasteId()));
							}
							//Get the detail to each spec.
							if(of.getTasteGroup().hasSpec()){
								of.getTasteGroup().getSpec().copyFrom(TasteDao.getTasteById(dbCon, staff, of.getTasteGroup().getSpec().getTasteId()));
							}
							of.getTasteGroup().refresh();
						}
					}					
				}
			}

			//Set the default discount.
			orderToInsert.setDiscount(DiscountDao.getDefault(dbCon, staff));
			
		}else if(orderToInsert.getDestTbl().isBusy()){
			int orderId = OrderDao.getOrderIdByUnPaidTable(dbCon, staff, orderToInsert.getDestTbl());
			OrderFood of = OrderFoodDao.getSingleDetail(dbCon, staff, new OrderFoodDao.ExtraCond(DateType.TODAY).setOrderId(orderId), " ORDER BY OF.id DESC LIMIT 1 ").get(0);
			long deltaSeconds = (System.currentTimeMillis() - of.getOrderDate()) / 1000;
			throw new BusinessException("\"" + of.getWaiter() + "\"" + (deltaSeconds >= 60 ? ((deltaSeconds / 60) + "分钟") : (deltaSeconds + "秒")) + "前修改了账单, 是否继续提交?", FrontBusinessError.ORDER_EXPIRED);
			
		}else{
			throw new BusinessException("Unknown error occourred while inserting order.", ProtocolError.UNKNOWN);
		}
		
	}
	

	/**
	 * Prepare to insert the order to database.
	 * The SQL statements should only be the INSERT, UPDATE or DELETE type. 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param orderToInsert
	 * 			the order along with basic insert parameters
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	private static void doInsert(DBCon dbCon, Staff staff, Order orderToInsert) throws SQLException{

		String sql; 

		/**
		 * Insert to 'order' table.
		 */
		sql = " INSERT INTO `" + Params.dbName + "`.`order` (" +
			  " `restaurant_id`, `category`, `region_id`, `region_name`, " +
			  " `table_id`, `table_alias`, `table_name`, " +
			  " `birth_date`, `order_date`, `custom_num`, `staff_id`, `waiter`, `discount_id`) VALUES (" +
			  orderToInsert.getDestTbl().getRestaurantId() + ", " + 
			  orderToInsert.getCategory().getVal() + ", " +
			  orderToInsert.getRegion().getRegionId() + ", '" +
			  orderToInsert.getRegion().getName() + "', " +
			  orderToInsert.getDestTbl().getTableId() + ", " +
			  orderToInsert.getDestTbl().getAliasId() + ", " +
			  "'" + orderToInsert.getDestTbl().getName() + "'" + ", " +
			  " NOW() " + ", " + 
			  " NOW() " + ", " +
			  orderToInsert.getCustomNum() + ", " +
			  staff.getId() + ", " +
			  "'" + staff.getName() + "'" + ", " +
			  orderToInsert.getDiscount().getId() + 
			  ")";
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
			  " category = " + orderToInsert.getCategory().getVal() + ", " +
			  " custom_num = " + orderToInsert.getCustomNum() +
			  " WHERE restaurant_id = " + staff.getRestaurantId() + 
			  " AND table_alias = " + orderToInsert.getDestTbl().getAliasId();
		dbCon.stmt.executeUpdate(sql);
		
		/**
		 * Insert the detail records to 'order_food' table
		 */
		for(OrderFood foodToInsert : orderToInsert.getOrderFoods()){
			

			if(foodToInsert.hasTasteGroup()){
			
				TasteGroup tg = foodToInsert.getTasteGroup();
				
				int tgId = TasteGroupDao.insert(dbCon, staff, new TasteGroup.InsertBuilder(foodToInsert)
															     .addAllTastes(tg.getNormalTastes())
															     .setTmpTaste(tg.getTmpTaste()));
				tg.setGroupId(tgId);
//				TasteGroup tg = foodToInsert.getTasteGroup();
//				
//				/**
//				 * Insert the taste group if containing taste.
//				 */
//				sql = " INSERT INTO " + Params.dbName + ".taste_group " +
//					  " ( " +
//					  " `normal_taste_group_id`, `normal_taste_pref`, `normal_taste_price`, " +
//					  " `tmp_taste_id`, `tmp_taste_pref`, `tmp_taste_price` " +
//					  " ) " +
//					  " SELECT " +
//					  (tg.hasNormalTaste() ? "MAX(normal_taste_group_id) + 1" : TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID) + ", " +
//					  (tg.hasNormalTaste() ? ("'" + tg.getNormalTastePref() + "'") : "NULL") + ", " +
//					  (tg.hasNormalTaste() ? tg.getNormalTastePrice() : "NULL") + ", " +
//					  (tg.hasTmpTaste() ? tg.getTmpTaste().getTasteId() : "NULL") + ", " +
//					  (tg.hasTmpTaste() ? "'" + tg.getTmpTastePref() + "'" : "NULL") + ", " +
//					  (tg.hasTmpTaste() ? tg.getTmpTastePrice() : "NULL") +
//					  " FROM " +
//					  Params.dbName + ".taste_group" + 
//					  " LIMIT 1 ";
//				dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
//				//get the generated id to taste group 
//				dbCon.rs = dbCon.stmt.getGeneratedKeys();
//				if(dbCon.rs.next()){
//					tg.setGroupId(dbCon.rs.getInt(1));
//				}else{
//					throw new SQLException("The id of taste group is not generated successfully.");
//				}
//				
//				/**
//				 * Insert the normal taste group if containing normal tastes.
//				 */
//				if(tg.hasNormalTaste()){
//					for(Taste normalTaste : tg.getNormalTastes()){
//						sql = " INSERT INTO " + Params.dbName + ".normal_taste_group " +
//							  " ( " +
//							  " `normal_taste_group_id`, `taste_id` " +
//							  " ) " +
//							  " VALUES " +
//							  " ( " +
//							  " (SELECT normal_taste_group_id FROM " + Params.dbName + ".taste_group " + 
//							  " WHERE " +
//							  " taste_group_id = " + tg.getGroupId() + "), " +
//							  normalTaste.getTasteId() + 
//							  " ) ";
//						dbCon.stmt.executeUpdate(sql);
//					}
//				}
//				
			}
				
			//insert the record to table "order_food"
			sql = " INSERT INTO `" + Params.dbName + "`.`order_food` " +
				  " ( " +
				  " `restaurant_id`, `order_id`, `food_id`, `order_count`, `unit_price`, `commission`, `name`, " +
				  " `food_status`, `discount`, `taste_group_id`, " +
				  " `dept_id`, `kitchen_id`, " +
				  " `staff_id`, `waiter`, `order_date`, " +
				  " `is_temporary`, `is_gift` " +
				  " ) " +
				  " VALUES " +
				  " ( " +	
				  staff.getRestaurantId() + ", " +
				  orderToInsert.getId() + ", " +
				  foodToInsert.getFoodId() + ", " +
				  foodToInsert.getCount() + ", " + 
				  foodToInsert.asFood().getPrice() + ", " + 
				  foodToInsert.asFood().getCommission() + ",'" +
				  foodToInsert.getName() + "', " +
				  foodToInsert.asFood().getStatus() + ", " +
				  foodToInsert.getDiscount() + ", " +
				  foodToInsert.getTasteGroup().getGroupId() + ", " +
				  foodToInsert.getKitchen().getDept().getId() + ", " +
				  foodToInsert.getKitchen().getId() + ", " +
				  staff.getId() + "," +
				  "'" + staff.getName() + "', NOW(), " + 
				  (foodToInsert.isTemp() ? "1" : "0") + ", " +
				  (foodToInsert.isGift() ? "1" : "0") +
				  " ) ";
				
			dbCon.stmt.executeUpdate(sql);

			//FIXME Insert the temporary food to menu.
//			if(foodToInsert.isTemp()){
//				try{
//					FoodDao.insert(dbCon, staff, new Food.InsertBuilder(foodToInsert.getName(), foodToInsert.getPrice(), foodToInsert.getKitchen()).setTemp(true));
//				}catch(BusinessException ingored){}
//			}
		}
	}
}
