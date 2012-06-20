package com.wireless.db;

import java.sql.SQLException;

import com.wireless.dbReflect.OrderFoodReflector;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class QueryOrder {

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
	public static Order exec(DBCon dbCon, long pin, short model, int tableID) throws BusinessException, SQLException {		

		Table table = QueryTable.exec(dbCon, pin, model, tableID);
			
		return execByID(dbCon, Util.getUnPaidOrderID(dbCon, table), QUERY_TODAY);

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
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order execByID(DBCon dbCon, int orderID, int queryType) throws BusinessException, SQLException{

		String orderTbl;
		if(queryType == QUERY_TODAY){
			orderTbl = "order";
		}else if(queryType == QUERY_HISTORY){
			orderTbl = "order_history";
		}else{
			orderTbl = "order";			
		}
		
		/**
		 * Get the related info to this order.
		 */
		String sql = "SELECT seq_id, custom_num, table_id, table_alias, table_name, table2_alias, table2_name, " +
					 "region_id, region_name, restaurant_id, type, discount_type, category, is_paid FROM " + Params.dbName	+ 
					 "." + orderTbl + " " +
					 "WHERE id=" + orderID;

		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		Order orderInfo = new Order();

		if(dbCon.rs.next()) {
			orderInfo.seqID = dbCon.rs.getInt("seq_id");
			orderInfo.restaurantID = dbCon.rs.getInt("restaurant_id");
			orderInfo.table.tableID = dbCon.rs.getInt("table_id");
			orderInfo.table.aliasID = dbCon.rs.getInt("table_alias");
			orderInfo.table.name = dbCon.rs.getString("table_name");
			orderInfo.table2.aliasID = dbCon.rs.getShort("table2_alias");
			orderInfo.table2.name = dbCon.rs.getString("table2_name");
			orderInfo.region.regionID = dbCon.rs.getShort("region_id");
			orderInfo.region.name = dbCon.rs.getString("region_name");
			orderInfo.custom_num = dbCon.rs.getShort("custom_num");
			orderInfo.category = dbCon.rs.getShort("category");
			orderInfo.pay_manner = dbCon.rs.getShort("type");
			orderInfo.discount_type = dbCon.rs.getShort("discount_type");
			orderInfo.isPaid = dbCon.rs.getBoolean("is_paid");
		}else{
			throw new BusinessException("The order(id=" + orderID + ") does NOT exist.", ErrorCode.ORDER_NOT_EXIST);
		}
		dbCon.rs.close();
		
		/**
		 * Get the total and actual price if the order has been paid
		 */
		sql = "SELECT total_price, total_price_2 FROM " + Params.dbName +
			   "." + orderTbl + 
			   " WHERE id=" + orderID +
			   " AND total_price IS NOT NULL";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			orderInfo.setTotalPrice(dbCon.rs.getFloat("total_price"));
			orderInfo.setActualPrice(dbCon.rs.getFloat("total_price_2"));
		}
		dbCon.rs.close();
		
		/**
		 * Get the minimum cost
		 */
		sql = "SELECT minimum_cost FROM " + Params.dbName +	
			  ".table WHERE restaurant_id=" + orderInfo.restaurantID +
			  " AND table_alias=" + orderInfo.table.aliasID;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			//orderInfo.minimum_cost = new Float(dbCon.rs.getFloat("minimum_cost") * 100).intValue();
			orderInfo.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
		}
		dbCon.rs.close();
		
		// query the food's id and order count associate with the order id for "order_food" table		

		if(queryType == QUERY_HISTORY){
			orderInfo.foods = OrderFoodReflector.getDetailHistory(dbCon, " AND A.order_id=" + orderID, "");
		}else if(queryType == QUERY_TODAY){
			orderInfo.foods = OrderFoodReflector.getDetailToday(dbCon, " AND B.id=" + orderID, "");
		}else{
			orderInfo.foods = OrderFoodReflector.getDetailToday(dbCon, " AND B.id=" + orderID, "");
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
			
			return execByID(dbCon, Util.getUnPaidOrderID(dbCon, table), QUERY_TODAY);
			
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
	public static Order exec(DBCon dbCon, Terminal term, int tableID) throws BusinessException, SQLException {		

		Table table = QueryTable.exec(dbCon, term, tableID);
			
		return execByID(dbCon, Util.getUnPaidOrderID(dbCon, table), QUERY_TODAY);

	}
	
}
