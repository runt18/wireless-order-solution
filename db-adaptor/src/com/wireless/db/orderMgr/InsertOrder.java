package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.exception.ProtocolError;
import com.wireless.exception.StaffError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.weixin.order.WxOrder;

public class InsertOrder {
	
	/**
	 * Insert a new order according to the specific builder {@link Order#InsertBuilder}.
	 * 
	 * @param staff
	 *            the staff to perform this action
	 * @param builder
	 *			  the builder to insert order
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
	public static Order exec(Staff staff, Order.InsertBuilder builder) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			Order order = exec(dbCon, staff, builder);
			dbCon.conn.commit();
			return order;
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new order according to the specific builder {@link Order#InsertBuilder}.
	 * 
	 * @param dbCon
	 * 			  the database connection
	 * @param staff
	 *            the staff to perform this action
	 * @param builder
	 * 			  the builder to insert order
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
	public static Order exec(DBCon dbCon, Staff staff, Order.InsertBuilder builder) throws BusinessException, SQLException{
		
		Order orderToInsert = builder.build();
		
		doPrepare(dbCon, staff, orderToInsert, builder.getSuffix(), builder.getFastNo());
			
		doInsert(dbCon, staff, orderToInsert);
		
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
	private static void doPrepare(DBCon dbCon, Staff staff, Order orderToInsert, Table.InsertBuilder4Join.Suffix suffix, int fastNo) throws BusinessException, SQLException{
		
		//Check to see whether the staff has the privilege to add the food 
		if(!staff.getRole().hasPrivilege(Privilege.Code.ADD_FOOD)){
			throw new BusinessException(StaffError.ORDER_NOT_ALLOW);
		}
		
		if(orderToInsert.getCategory().isNormal()){
			orderToInsert.setDestTbl(TableDao.getById(dbCon, staff, orderToInsert.getDestTbl().getId()));
			
		}else if(orderToInsert.getCategory().isJoin()){
			//Create the temporary table with the suffix for joined
			int joinedTblId = TableDao.insert(dbCon, staff, new Table.InsertBuilder4Join(TableDao.getById(dbCon, staff, orderToInsert.getDestTbl().getId()), suffix));
			orderToInsert.setDestTbl(TableDao.getById(dbCon, staff, joinedTblId));
			
		}else if(orderToInsert.getCategory().isTakeout()){
			//Create the temporary table for take out
			int takeoutTblId = TableDao.insert(dbCon, staff, new Table.InsertBuilder4Takeout());
			orderToInsert.setDestTbl(TableDao.getById(dbCon, staff, takeoutTblId));
			
		}else if(orderToInsert.getCategory().isFeast()){
			//Create the temporary table for feast
			int feastTblId = TableDao.insert(dbCon, staff, new Table.InsertBuilder4Feast());
			orderToInsert.setDestTbl(TableDao.getById(dbCon, staff, feastTblId));
			
		}else if(orderToInsert.getCategory().isFast()){
			//Create the temporary table for fast
			int fastTblId = TableDao.insert(dbCon, staff, new Table.InsertBuilder4Fast(fastNo));
			orderToInsert.setDestTbl(TableDao.getById(dbCon, staff, fastTblId));
		}
		
		if(orderToInsert.getDestTbl().isIdle()){
			
			for(OrderFood of : orderToInsert.getOrderFoods()){
				//Skip the food whose order count is less than zero.
				if(of.getCount() >= 0){		
					float count = of.getCount();
					of.setCount(0);
					of.addCount(count);
					//Check to see whether the staff has the privilege to present the food.
					if(of.isGift() && !staff.getRole().hasPrivilege(Privilege.Code.GIFT)){
						throw new BusinessException(StaffError.GIFT_NOT_ALLOW);
					}
					//Fill the detail to each order food.
					OrderFoodDao.fill(dbCon, staff, of);
				}
			}

		}else if(orderToInsert.getDestTbl().isBusy()){
			List<OrderFood> orderFoods = OrderFoodDao.getSingleDetail(dbCon, staff, new OrderFoodDao.ExtraCond(DateType.TODAY).setOrder(orderToInsert.getDestTbl().getOrderId()), " ORDER BY OF.id DESC LIMIT 1 ");
			if(!orderFoods.isEmpty()){
				OrderFood of = orderFoods.get(0);
				long deltaSeconds = (System.currentTimeMillis() - of.getOrderDate()) / 1000;
				throw new BusinessException("\"" + of.getWaiter() + "\"" + (deltaSeconds >= 60 ? ((deltaSeconds / 60) + "分钟") : (deltaSeconds + "秒")) + "前修改了账单, 是否继续提交?", FrontBusinessError.ORDER_EXPIRED);
			}			
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
	 * @throws BusinessException 
	 * 			throws if the remaining to any limit food is insufficient 
	 */
	private static void doInsert(DBCon dbCon, Staff staff, Order orderToInsert) throws SQLException, BusinessException{

		String sql; 

		/**
		 * Insert to 'order' table.
		 */
		sql = " INSERT INTO `" + Params.dbName + "`.`order` (" +
			  " `restaurant_id`, `comment`, `category`, `region_id`, `region_name`, " +
			  " `table_id`, `table_alias`, `table_name`, " +
			  " `birth_date`, `order_date`, `custom_num`, `staff_id`, `waiter`, `discount_id`) VALUES (" +
			  staff.getRestaurantId() + ", " + 
			  "'" + orderToInsert.getComment() + "'," +
			  orderToInsert.getCategory().getVal() + ", " +
			  orderToInsert.getRegion().getId() + ", " +
			  "'" + orderToInsert.getRegion().getName() + "', " +
			  orderToInsert.getDestTbl().getId() + ", " +
			  orderToInsert.getDestTbl().getAliasId() + ", " +
			  "'" + orderToInsert.getDestTbl().getName() + "', " +
			  " NOW(), " + 
			  " NOW(), " +
			  orderToInsert.getCustomNum() + ", " +
			  staff.getId() + ", " +
			  "'" + staff.getName() + "', " +
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

		//Insert the detail records to 'order_food' table.
		for(OrderFood foodToInsert : orderToInsert.getOrderFoods()){
			OrderFoodDao.insertExtra(dbCon, staff, new OrderFoodDao.ExtraBuilder(orderToInsert, foodToInsert));
		}
		
		//Associated the weixin order.
		for(WxOrder wxOrder : orderToInsert.getWxOrders()){
			try{
				WxOrderDao.update(dbCon, staff, new WxOrder.AttachBuilder(WxOrderDao.getByCode(dbCon, staff, wxOrder.getCode()), orderToInsert).asBuilder());
			}catch(BusinessException ignored){
				ignored.printStackTrace();
			}
		}
	}
}
