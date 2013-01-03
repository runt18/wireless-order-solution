package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryTable;
import com.wireless.db.VerifyPin;
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
	 * Get the unpaid order detail information to the specific table alias id.
	 * 
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
	 *             - The unpaid order to this table does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static Order execByTable(long pin, short model, int tableAlias) throws BusinessException, SQLException {
		
		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();

			return execByTable(dbCon, pin, model, tableAlias);

		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the unpaid order detail information to the specific table alias id.
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
	 *             - The unpaid order to this table does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static Order execByTable(DBCon dbCon, long pin, short model, int tableAlias) throws BusinessException, SQLException {		
		return execByTable(VerifyPin.exec(dbCon, pin, model), tableAlias);
	}
	
	/**
	 * Get the unpaid order detail information to the specific restaurant and table.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param term
	 *            the terminal
	 * @param tableAlias
	 *            the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException
	 *             Throws if one of cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The table to query does NOT exist.<br>
	 *             - The unpaid order to this table does NOT exist.
	 * @throws SQLException
	 *             Throws if fail to execute any SQL statement.
	 */
	public static Order execByTable(DBCon dbCon, Terminal term, int tableAlias) throws BusinessException, SQLException {		
		return execByID(dbCon, QueryOrderDao.getOrderIdByUnPaidTable(dbCon, QueryTable.exec(dbCon, term, tableAlias))[0], QUERY_TODAY);
	}
	
	/**
	 * Get the unpaid order detail information to the specific restaurant and
	 * table. If the table is merged, get its parent order, otherwise get the
	 * order of its own.
	 * @param tableAlias
	 *            the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException
	 *             Throws if one of cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The table to query does NOT exist.<br>
	 *             - The unpaid order to this table does NOT exist.
	 * @throws SQLException
	 *             Throws if fail to execute any SQL statement.
	 */
	public static Order execByTableDync(Terminal term, int tableAlias) throws BusinessException, SQLException {		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execByTableDync(dbCon, term, tableAlias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the unpaid order detail information to the specific restaurant and
	 * table. If the table is merged, get its parent order, otherwise get the
	 * order of its own.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param term
	 *            the terminal
	 * @param tableAlias
	 *            the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException
	 *             Throws if one of cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The table to query does NOT exist.<br>
	 *             - The unpaid order to this table does NOT exist.
	 * @throws SQLException
	 *             Throws if fail to execute any SQL statement.
	 */
	public static Order execByTableDync(DBCon dbCon, Terminal term, int tableAlias) throws BusinessException, SQLException {
		
		//If the table is merged, get its parent order.
		//Otherwise get the order of its own.
		int[] unpaidId = QueryOrderDao.getOrderIdByUnPaidTable(dbCon, QueryTable.exec(dbCon, term, tableAlias));
		if(unpaidId.length > 1){
			return execByID(dbCon, unpaidId[1], QUERY_TODAY);
		}else{
			return execByID(dbCon, unpaidId[0], QUERY_TODAY);
		}
	}
	
	/**
	 * Get the unpaid order detail information to the specific restaurant and table.
	 * @param terminal
	 *            the terminal to query
	 * @param tableAlias
	 *            the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException
	 *             throws if one of cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The table to query does NOT exist.<br>
	 *             - The unpaid order to this table does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static Order execByTable(Terminal term, int tableAlias) throws BusinessException, SQLException {		

		DBCon dbCon = new DBCon();
		try{
			
			dbCon.connect();			
			return execByTable(dbCon, term, tableAlias);
			
		}finally{
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
	 * Get the order detail information according to the specific order id. Note
	 * that the database should be connected before invoking this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param orderId
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
	public static Order execByID(DBCon dbCon, int orderId, int queryType) throws BusinessException, SQLException{
		
		String extraCond = null;
		if(queryType == QUERY_TODAY){
			extraCond = "AND O.id = " + orderId;
		}else if(queryType == QUERY_HISTORY){
			extraCond = " AND OH.id = " + orderId;
		}
		
		Order[] results = exec(dbCon, extraCond, null, queryType);
		if(results.length > 0){
			return results[0];
		}else{
			throw new BusinessException("The order(id=" + orderId + ") does NOT exist.", ErrorCode.ORDER_NOT_EXIST);
		}
	}
	
	/**
	 * Get the details to both order and its children according to the specific order id. 
	 * 
	 * @param orderId
	 *            the order id to query
	 * @param queryType
	 * 			  indicates which query type should use.
	 * 		  	  it is one of values below.
	 * 			  - QUERY_TODAY
	 *            - QUERY_HISTORY
	 * @return the order detail information
	 * @throws BusinessException
	 * 			   Throws if the order to this id does NOT exist.
	 * @throws SQLException
	 *             Throws if fail to execute any SQL statement
	 */
	public static Order execWithChildDetailByID(int orderId, int queryType) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execWithChildDetailByID(dbCon, orderId, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the details to both order and its children according to the specific order id. 
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param orderId
	 *            the order id to query
	 * @param queryType
	 * 			  indicates which query type should use.
	 * 		  	  it is one of values below.
	 * 			  - QUERY_TODAY
	 *            - QUERY_HISTORY
	 * @return the order detail information
	 * @throws BusinessException
	 * 			   Throws if the order to this id does NOT exist.
	 * @throws SQLException
	 *             Throws if fail to execute any SQL statement
	 */
	public static Order execWithChildDetailByID(DBCon dbCon, int orderId, int queryType) throws BusinessException, SQLException{
		Order result = execByID(dbCon, orderId, queryType);
		if(result.isMerged() && result.hasChildOrder()){
			Order[] childOrders = result.getChildOrder();
			for(int i = 0; i < childOrders.length; i++){
				if(queryType == QUERY_TODAY){
					childOrders[i].foods = QueryOrderFoodDao.getDetailToday(dbCon, "AND O.id = " + childOrders[i].getId(), null);
				}else if(queryType == QUERY_HISTORY){
					childOrders[i].foods = QueryOrderFoodDao.getDetailHistory(dbCon, "AND OH.id = " + childOrders[i].getId(), null);
				}

			}
		}
		return result;
	}
	
	/**
	 * Get the order detail information according to the specific extra condition and order clause. 
	 * @param extraCond
	 *            the extra condition to query
	 * @param orderClause
	 * 			  the order clause to query
	 * @param queryType
	 * 			  indicates which query type should use.
	 * 		  	  it is one of values below.
	 * 			  - QUERY_TODAY
	 *            - QUERY_HISTORY
	 * @return the array holding the result to each order detail information
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order[] exec(String extraCond, String orderClause, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, extraCond, orderClause, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the order detail information according to the specific extra condition and order clause. 
	 * Note that the database should be connected before invoking this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param extraCond
	 *            the extra condition to query
	 * @param orderClause
	 * 			  the order clause to query
	 * @param queryType
	 * 			  indicates which query type should use.
	 * 		  	  it is one of values below.
	 * 			  - QUERY_TODAY
	 *            - QUERY_HISTORY
	 * @return the array holding the result to each order detail information
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order[] exec(DBCon dbCon, String extraCond, String orderClause, int queryType) throws SQLException{
		String sql;
		if(queryType == QUERY_TODAY){
			sql = " SELECT " +
				  " O.id, O.order_date, O.seq_id, O.custom_num, O.table_id, O.table_alias, O.table_name, " +
				  " T.minimum_cost, " +
				  " O.region_id, O.region_name, O.restaurant_id, O.type, O.category, O.status, O.discount_id, O.service_rate, " +
				  " O.gift_price, O.cancel_price, O.discount_price, O.repaid_price, O.erase_price, O.total_price, O.total_price_2, " +
				  " PP.price_plan_id, PP.name AS price_plan_name, PP.status AS price_plan_status " +
				  " FROM " + 
				  Params.dbName + ".order O " +
				  " LEFT JOIN " + Params.dbName + ".table T " +
				  " ON O.table_id = T.table_id " +
				  " LEFT JOIN " + Params.dbName + ".price_plan PP " +
				  " ON O.price_plan_id = PP.price_plan_id " +
				  " WHERE 1 = 1 " + 
				  (extraCond != null ? extraCond : "") + " " +
				  (orderClause != null ? orderClause : "");
			
		}else if(queryType == QUERY_HISTORY){
			sql = " SELECT " +
				  " OH.id, OH.order_date, OH.seq_id, OH.custom_num, OH.table_id, OH.table_alias, OH.table_name, 0 AS minimum_cost, " +
				  " OH.region_id, OH.region_name, OH.restaurant_id, OH.type, OH.category, OH.status, 0 AS discount_id, OH.service_rate, " +
				  " OH.gift_price, OH.cancel_price, OH.discount_price, OH.repaid_price, OH.erase_price, OH.total_price, OH.total_price_2 " +
				  " FROM " + Params.dbName + ".order_history OH " + 
				  " WHERE 1 = 1 " + 
				  (extraCond != null ? extraCond : "") + " " +
				  (orderClause != null ? orderClause : "");
		}else{
			throw new IllegalArgumentException("The query type passed to query order is NOT valid.");
		}
		
		//Get the details to each order.
		dbCon.rs = dbCon.stmt.executeQuery(sql);		

		List<Order> results = new ArrayList<Order>();
		
		while(dbCon.rs.next()) {
			Order orderInfo = new Order();
			orderInfo.setId(dbCon.rs.getInt("id"));
			orderInfo.seqID = dbCon.rs.getInt("seq_id");
			orderInfo.orderDate = dbCon.rs.getTimestamp("order_date").getTime();
			orderInfo.restaurantID = dbCon.rs.getInt("restaurant_id");
			orderInfo.setStatus(dbCon.rs.getInt("status"));
			Table table = new Table();
			table.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			table.setCategory(dbCon.rs.getShort("category"));
			table.setTableId(dbCon.rs.getInt("table_id"));
			if(orderInfo.isUnpaid()){
				table.setStatus(Table.TABLE_IDLE);
			}else{
				table.setStatus(Table.TABLE_BUSY);
			}
			table.setAliasId(dbCon.rs.getInt("table_alias"));
			table.setName(dbCon.rs.getString("table_name"));
			table.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
			orderInfo.setDestTbl(table);
			orderInfo.setSrcTbl(table);
			orderInfo.region.regionID = dbCon.rs.getShort("region_id");
			orderInfo.region.name = dbCon.rs.getString("region_name");
			orderInfo.setCustomNum(dbCon.rs.getShort("custom_num"));
			orderInfo.setCategory(dbCon.rs.getShort("category"));
			orderInfo.setDiscount(new Discount(dbCon.rs.getInt("discount_id")));
			orderInfo.payManner = dbCon.rs.getShort("type");
			orderInfo.setStatus(dbCon.rs.getInt("status"));
			orderInfo.setServiceRate(dbCon.rs.getFloat("service_rate"));
			orderInfo.setGiftPrice(dbCon.rs.getFloat("gift_price"));
			orderInfo.setCancelPrice(dbCon.rs.getFloat("cancel_price"));
			orderInfo.setRepaidPrice(dbCon.rs.getFloat("repaid_price"));
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
			
			results.add(orderInfo);
		}

		dbCon.rs.close();
		
		for(Order orderInfo : results){
			//Get the details to child orders and generate the condition string if the order belongs to merged.
			StringBuffer childOrderIds = new StringBuffer();		
			if(orderInfo.isMerged()){
				if(queryType == QUERY_TODAY){
					sql = " SELECT " + 
						  " OG.sub_order_id, SO.table_id, SO.table_name," +
						  " SO.cancel_price, SO.gift_price, SO.discount_price, SO.erase_price, SO.total_price, SO.actual_price, " +
						  " T.table_alias, T.restaurant_id " +
						  " FROM " + 
						  Params.dbName + ".order_group OG " +
						  " JOIN " + Params.dbName + ".sub_order SO " +
						  " ON " + " OG.sub_order_id = SO.order_id " +
						  " LEFT JOIN " + Params.dbName + ".table T " + 
						  " ON " + " SO.table_id = T.table_id " +
						  " WHERE " + " OG.order_id = " + orderInfo.getId();
					
				}else if(queryType == QUERY_HISTORY){
					sql = " SELECT " + 
						  " OGH.sub_order_id, SOH.table_id, SOH.table_name," +
						  " SOH.cancel_price, SOH.gift_price, SOH.discount_price, SOH.erase_price, SOH.total_price, SOH.actual_price, " +
						  " T.table_alias, T.restaurant_id " +
						  " FROM " + 
						  Params.dbName + ".order_group_history OGH " +
						  " JOIN " + Params.dbName + ".sub_order SOH " +
						  " ON " + " OGH.sub_order_id = SOH.order_id " +
						  " LEFT JOIN " + Params.dbName + ".table T " + 
						  " ON " + " SOH.table_id = T.table_id " +
						  " WHERE " + " OGH.order_id = " + orderInfo.getId();
				}
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				List<Order> subOrders = new ArrayList<Order>();
				while(dbCon.rs.next()){
					Order subOrder = new Order();
					subOrder.setId(dbCon.rs.getInt("sub_order_id"));
					subOrder.setCancelPrice(dbCon.rs.getFloat("cancel_price"));
					subOrder.setGiftPrice(dbCon.rs.getFloat("gift_price"));
					subOrder.setDiscountPrice(dbCon.rs.getFloat("discount_price"));
					subOrder.setErasePrice(dbCon.rs.getInt("erase_price"));
					subOrder.setTotalPrice(dbCon.rs.getFloat("total_price"));
					subOrder.setActualPrice(dbCon.rs.getFloat("actual_price"));
					
					subOrder.getDestTbl().setTableId(dbCon.rs.getInt("table_id"));
					subOrder.getDestTbl().setAliasId(dbCon.rs.getInt("table_alias"));
					subOrder.getDestTbl().restaurantID = dbCon.rs.getInt("restaurant_id");
					subOrder.getDestTbl().name = dbCon.rs.getString("table_name");
					
					subOrders.add(subOrder);
					childOrderIds.append(dbCon.rs.getInt("sub_order_id") + ",");
				}
				if(childOrderIds.length() != 0){
					childOrderIds.deleteCharAt(childOrderIds.length() - 1);
				}
				orderInfo.setChildOrder(subOrders.toArray(new Order[subOrders.size()]));
				dbCon.rs.close();
				
			}else{
				childOrderIds.append(orderInfo.getId());
			}
			
			//Get the food's id and order count associate with the order id for "order_food" table		
			if(childOrderIds.length() != 0){
				if(queryType == QUERY_TODAY){
					orderInfo.foods = QueryOrderFoodDao.getDetailToday(dbCon, " AND OF.order_id IN(" + childOrderIds + ")", "ORDER BY pay_datetime");					
				}else if(queryType == QUERY_HISTORY){
					orderInfo.foods = QueryOrderFoodDao.getDetailHistory(dbCon, " AND OFH.order_id IN(" + childOrderIds + ")", "ORDER BY pay_datetime");
				} 
			}
		}
		
		return results.toArray(new Order[results.size()]);
	}
	


	/**
	 * Get the order id according to the specific unpaid table.
	 * @param dbCon 
	 * 			the database connection
	 * @param table 
	 * 			the table information containing the alias id and associated restaurant id
	 * @return An array holding the unpaid child and parent order id to this table.
	 * 		   If the table is merged, the array contains two elements,
	 * 		   the 1st element is the order id of its own, the 2nd is the order id of its parent.
	 * 		   Otherwise, the array only has one element which is the order id of its own. <br>
	 * 		   As a result, we can make use of the length of array to check if the table to query is merged or not, looks like below
	 * 		   <p>
	 * 		   if(QueryOrderDao.getOrderIdByUnPaidTable(dbCon, tbl).length > 1){<br>
	 * 				//means the table with an unpaid merged order<br>
	 *				//to do something ... <br>
	 * 		   }<br>
	 * 		   </p>
	 * @throws BusinessException 
	 * 			Throws if either of cases below.<br>
	 * 			1 - The table to query is IDLE.<br>
	 * 			2 - The unpaid order to this table does NOT exist.<br>
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static int[] getOrderIdByUnPaidTable(DBCon dbCon, Table table) throws BusinessException, SQLException{
		
		String sql;
		
		int[] result;		
		
		//Get the order id & category associated with this table.
		int childOrderId;
		int category = 0;

		sql = " SELECT " +
			  " id, category " +
			  " FROM " + Params.dbName + ".order " +
			  " WHERE " +
			  " table_alias = " + table.getAliasId() +
			  " AND restaurant_id = " + table.restaurantID +
			  " AND status = " + Order.STATUS_UNPAID;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			childOrderId = dbCon.rs.getInt("id");
			category = dbCon.rs.getShort("category");
			result = new int[1];
			result[0] = childOrderId;
		}else{
			throw new BusinessException("The un-paid order id to table(alias_id=" + table.getAliasId() + ", restaurant_id=" + table.restaurantID + ") does NOT exist.", ErrorCode.ORDER_NOT_EXIST);
		}
		dbCon.rs.close();
		
		//If the table is child merged, get the id to its parent order.
		if(category == Order.CATE_MERGER_CHILD){
			sql = " SELECT " +
				  " O.id " +
				  " FROM " +
				  Params.dbName + ".order O " +
				  " JOIN " + Params.dbName + ".order_group OG " + " ON " + " O.id = OG.order_id " +
				  " WHERE 1 = 1" +
				  " AND O.status = " + Order.STATUS_UNPAID +
				  " AND " + " OG.sub_order_id = " + childOrderId;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				result = new int[2];
				result[0] = childOrderId;
				result[1] = dbCon.rs.getInt("id");
			}else{
				throw new BusinessException("The un-paid merged order (sub order id = " + childOrderId + ") does NOT exist.", ErrorCode.ORDER_NOT_EXIST);
			}
			dbCon.rs.close();
		}
		
		return result;
		
	}
	
	
}
