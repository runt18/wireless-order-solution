package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryTable;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Discount;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.PricePlan;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class QueryOrderDao {

	public final static int QUERY_TODAY = 0;
	public final static int QUERY_HISTORY = 1;
	
	/**
	 * Get the order detail information to the specific table alias id.
	 * 
	 * @param pin
	 *            the pin to the terminal
	 * @param model
	 *            the model id to the terminal
	 * @param tableID
	 *            the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException
	 *             throws if one of cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The table to query does NOT exist.<br>
	 *             - The table associated with this order is idle.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static Order exec(long pin, short model, int tableID) throws BusinessException, SQLException {
		
		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();

			return exec(dbCon, pin, model, tableID);

		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the order detail information according to the specific order id. Note
	 * @param orderID
	 *            the order id to query
	 * @param queryType
	 * 			  indicates which query type should use.
	 * 		  	  it is one of values below.
	 * 			  - QUERY_TODAY
	 *            - QUERY_HISTORY
	 * @return the order detail information
	 * @throws BusinessException
	 * 			   throws if the order to this id does NOT exist
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order execByID(int orderID, int queryType) throws BusinessException, SQLException {
		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();

			return execByID(dbCon, orderID, queryType);

		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the order detail information to the specific table alias id.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			  the database connection
	 * @param pin
	 *            the pin to the terminal
	 * @param model
	 *            the model id to the terminal
	 * @param tableAlias
	 *            the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException
	 *             throws if one of cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The table to query does NOT exist.<br>
	 *             - The table associated with this order is idle.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static Order exec(DBCon dbCon, long pin, short model, int tableAlias) throws BusinessException, SQLException {		

		Table table = QueryTable.exec(dbCon, pin, model, tableAlias);
			
		return execByID(dbCon, QueryOrderDao.getOrderIdByUnPaidTable(dbCon, table), QUERY_TODAY);

	}

	/**
	 * Get the order detail information according to the specific order id. Note
	 * that the database should be connected before invoking this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param orderID
	 *            the order id to query
	 * @param queryType
	 * 			  indicates which query type should use.
	 * 		  	  it is one of values below.
	 * 			  - QUERY_TODAY
	 *            - QUERY_HISTORY
	 * @return the order detail information
	 * @throws BusinessException
	 * 			   throws if the order to this id does NOT exist
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order execByID(DBCon dbCon, int orderID, int queryType) throws BusinessException, SQLException{

		String sql;
		if(queryType == QUERY_TODAY){
			sql = " SELECT " +
				  " O.order_date, O.seq_id, O.custom_num, O.table_id, O.table_alias, O.table_name, " +
				  " O.region_id, O.region_name, O.restaurant_id, O.type, O.category, O.status, O.discount_id, O.service_rate, " +
				  " O.gift_price, O.cancel_price, O.discount_price, O.erase_price, O.total_price, O.total_price_2, " +
				  " PP.price_plan_id, PP.name AS price_plan_name, PP.status AS price_plan_status " +
				  " FROM " + 
				  Params.dbName + ".order O " +
				  " LEFT JOIN " + Params.dbName + ".price_plan PP" +
				  " ON O.price_plan_id = PP.price_plan_id " +
				  " WHERE id = " + orderID;
		}else if(queryType == QUERY_HISTORY){
			sql = " SELECT " +
				  " order_date, seq_id, custom_num, table_id, table_alias, table_name, " +
				  " region_id, region_name, restaurant_id, type, category, status, 0 AS discount_id, service_rate, " +
				  " gift_price, cancel_price, discount_price, erase_price, total_price, total_price_2 " +
				  " FROM " + Params.dbName + ".order_history" + 
				  " WHERE id = " + orderID;
		}else{
			throw new IllegalArgumentException("The query type passed to query order is NOT valid.");
		}
		
		//Get the details to this order.
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		Order orderInfo = new Order();

		if(dbCon.rs.next()) {
			orderInfo.seqID = dbCon.rs.getInt("seq_id");
			orderInfo.orderDate = dbCon.rs.getTimestamp("order_date").getTime();
			orderInfo.restaurantID = dbCon.rs.getInt("restaurant_id");
			orderInfo.destTbl.tableID = dbCon.rs.getInt("table_id");
			orderInfo.destTbl.aliasID = dbCon.rs.getInt("table_alias");
			orderInfo.destTbl.name = dbCon.rs.getString("table_name");
			orderInfo.region.regionID = dbCon.rs.getShort("region_id");
			orderInfo.region.name = dbCon.rs.getString("region_name");
			orderInfo.customNum = dbCon.rs.getShort("custom_num");
			orderInfo.setCategory(dbCon.rs.getShort("category"));
			orderInfo.setDiscount(new Discount(dbCon.rs.getInt("discount_id")));
			orderInfo.payManner = dbCon.rs.getShort("type");
			orderInfo.setStatus(dbCon.rs.getInt("status"));
			orderInfo.setServiceRate(dbCon.rs.getFloat("service_rate"));
			orderInfo.setGiftPrice(dbCon.rs.getFloat("gift_price"));
			orderInfo.setCancelPrice(dbCon.rs.getFloat("cancel_price"));
			orderInfo.setDiscountPrice(dbCon.rs.getFloat("discount_price"));
			orderInfo.setErasePrice(dbCon.rs.getInt("erase_price"));
			orderInfo.setTotalPrice(dbCon.rs.getFloat("total_price"));
			orderInfo.setActualPrice(dbCon.rs.getFloat("total_price_2"));
			if(queryType == QUERY_TODAY){
				orderInfo.setPricePlan(new PricePlan(dbCon.rs.getInt("price_plan_id"),
													 dbCon.rs.getString("price_plan_name"),
													 dbCon.rs.getInt("price_plan_status"),
													 dbCon.rs.getInt("restaurant_id")));
			}
		}else{
			throw new BusinessException("The order(id=" + orderID + ") does NOT exist.", ErrorCode.ORDER_NOT_EXIST);
		}
		dbCon.rs.close();
		
		//Get the details to sub orders and generate the condition string if the order belongs to merged.
		StringBuffer subOrderIds = new StringBuffer();		
		if(orderInfo.isMerged()){
			sql = " SELECT " + 
				  " sub_order_id, table_id, table_name," +
				  " cancel_price, gift_price, discount_price, erase_price, total_price, actual_price " + 
				  " FROM " + Params.dbName + ".order_group" +
				  " WHERE " + " order_id = " + orderID;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			List<Order> subOrders = new ArrayList<Order>();
			while(dbCon.rs.next()){
				Order subOrder = new Order();
				subOrder.id = dbCon.rs.getInt("sub_order_id");
				subOrder.setCancelPrice(dbCon.rs.getFloat("cancel_price"));
				subOrder.setGiftPrice(dbCon.rs.getFloat("gift_price"));
				subOrder.setDiscountPrice(dbCon.rs.getFloat("discount_price"));
				subOrder.setErasePrice(dbCon.rs.getInt("erase_price"));
				subOrder.setTotalPrice(dbCon.rs.getFloat("total_price"));
				subOrder.setActualPrice(dbCon.rs.getFloat("actual_price"));
				
				subOrder.destTbl.tableID = dbCon.rs.getInt("table_id");
				subOrder.destTbl.name = dbCon.rs.getString("table_name");
				
				subOrders.add(subOrder);
				subOrderIds.append(dbCon.rs.getInt("sub_order_id") + ",");
			}
			subOrderIds.deleteCharAt(subOrderIds.length() - 1);
			orderInfo.setSubOrder(subOrders.toArray(new Order[subOrders.size()]));
			dbCon.rs.close();
			
		}else{
			subOrderIds.append(orderID);
		}
		
		//Get the minimum cost to the table associated with this order.
		sql = " SELECT " +
			  " minimum_cost " +
			  " FROM " + Params.dbName + ".table " +
			  " WHERE " +
			  " restaurant_id= " + orderInfo.restaurantID +
			  " AND " +
			  " table_alias= " + orderInfo.destTbl.aliasID;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			//orderInfo.minimum_cost = new Float(dbCon.rs.getFloat("minimum_cost") * 100).intValue();
			orderInfo.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
		}
		dbCon.rs.close();
		
		//Get the food's id and order count associate with the order id for "order_food" table		
		if(queryType == QUERY_HISTORY){
			orderInfo.foods = QueryOrderFoodDao.getDetailHistory(dbCon, " AND OH.id IN(" + subOrderIds + ")", "ORDER BY pay_datetime");
		}else if(queryType == QUERY_TODAY){
			orderInfo.foods = QueryOrderFoodDao.getDetailToday(dbCon, " AND O.id IN(" + subOrderIds + ")", "ORDER BY pay_datetime");
		}
		orderInfo.id = orderID;
		
		return orderInfo;
	}
	
	/**
	 * Get the order detail information to the specific restaurant and table id.
	 * @param terminal
	 *            the terminal to query
	 * @param tableID
	 *            the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException
	 *             throws if one of cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The table to query does NOT exist.<br>
	 *             - The table associated with this order is idle.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static Order exec(Terminal term, int tableID) throws BusinessException, SQLException {		

		DBCon dbCon = new DBCon();
		try{
			
			dbCon.connect();
			
			Table table = QueryTable.exec(dbCon, term, tableID);
			
			return execByID(dbCon, QueryOrderDao.getOrderIdByUnPaidTable(dbCon, table), QUERY_TODAY);
			
		}finally{
			dbCon.disconnect();
		}

	}
	
	/**
	 * Get the order detail information to the specific restaurant and table id.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			  the database connection
	 * @param terminal
	 *            the terminal to query
	 * @param tableAlias
	 *            the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException
	 *             throws if one of cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The table to query does NOT exist.<br>
	 *             - The table associated with this order is idle.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static Order getUnPaidOrderByTable(DBCon dbCon, Terminal term, int tableAlias) throws BusinessException, SQLException {		

		Table table = QueryTable.exec(dbCon, term, tableAlias);		
		
		return execByID(dbCon, QueryOrderDao.getOrderIdByUnPaidTable(dbCon, table), QUERY_TODAY);

	}

	/**
	 * Get the order id according to the specific unpaid table.
	 * @param dbCon 
	 * 			the database connection
	 * @param table 
	 * 			the table information containing the alias id and associated restaurant id
	 * @return the unpaid order id to this table
	 * @throws BusinessException 
	 * 			Throws if either of cases below.<br>
	 * 			1 - The table to query is IDLE.<br>
	 * 			2 - The order to the this table does NOT exist.<br>
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static int getOrderIdByUnPaidTable(DBCon dbCon, Table table) throws BusinessException, SQLException{
		
		String sql;
		int orderId;
		if(table.isIdle()){
			throw new BusinessException("The table(alias_id=" + table.aliasID + ", restaurant_id=" + table.restaurantID + ") to query is IDLE.", ErrorCode.TABLE_IDLE);			
		}else{
			int category = 0;
			//Get the order id & category associated with this table.
			sql = " SELECT " +
				  " id, category " +
				  " FROM " + Params.dbName + ".order " +
				  " WHERE " +
				  " table_id = " + table.tableID + 
				  " AND status = " + Order.STATUS_UNPAID;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				orderId = dbCon.rs.getInt("id");
				category = dbCon.rs.getShort("category");
			}else{
				throw new BusinessException("The un-paid order id to table(alias_id=" + table.aliasID + ", restaurant_id=" + table.restaurantID + ") does NOT exist.", ErrorCode.TABLE_IDLE);
			}
			dbCon.rs.close();
			
			//If the table is merged, get the id to its parent order.
			if(category == Order.CATE_MERGER_TABLE){
				sql = " SELECT " +
					  " id " +
					  " FROM " +
					  Params.dbName + ".order O " +
					  " JOIN " + Params.dbName + ".order_group OG " + " ON " + " O.id = OG.order_id " +
					  " WHERE 1 = 1" +
					  " AND O.status = " + Order.STATUS_UNPAID +
					  " AND " + " OG.sub_order_id = " + orderId;
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					orderId = dbCon.rs.getInt("id");
				}else{
					throw new BusinessException("The un-paid merged order (sub order id = " + orderId + ") does NOT exist.", ErrorCode.ORDER_NOT_EXIST);
				}
				dbCon.rs.close();
			}
			
			return orderId;
			
		}
	}
	
}
