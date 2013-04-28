package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.protocol.Order;
import com.wireless.protocol.PDiscount;
import com.wireless.protocol.PMember;
import com.wireless.protocol.PricePlan;
import com.wireless.protocol.Terminal;
import com.wireless.util.DataType;

public class QueryOrderDao {

	public final static int QUERY_TODAY = 0;
	public final static int QUERY_HISTORY = 1;
	
	/**
	 * Get the unpaid order detail information to the specific table alias id regardless of the merged status.
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
	 * Get the unpaid order detail information to the specific table alias id regardless of the merged status.
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
	 * Get the unpaid order detail information to the specific restaurant and table 
	 * regardless of the merged status.
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
		return execByID(dbCon, QueryOrderDao.getOrderIdByUnPaidTable(dbCon, TableDao.getTableByAlias(dbCon, term, tableAlias))[0], QUERY_TODAY);
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
		int[] unpaidId = QueryOrderDao.getOrderIdByUnPaidTable(dbCon, TableDao.getTableByAlias(dbCon, term, tableAlias));
		if(unpaidId.length > 1){
			return execByID(dbCon, unpaidId[1], QUERY_TODAY);
		}else{
			return execByID(dbCon, unpaidId[0], QUERY_TODAY);
		}
	}
	
	/**
	 * Get the unpaid order detail information to the specific restaurant and table regardless of the merged status.
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
	public static Order execByID(int orderId, int queryType) throws BusinessException, SQLException {
		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();

			return execByID(dbCon, orderId, queryType);

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
			throw new BusinessException("The order(id = " + orderId + ") does NOT exist.", ProtocolError.ORDER_NOT_EXIST);
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
					childOrders[i].setOrderFoods(QueryOrderFoodDao.getDetailToday(dbCon, "AND O.id = " + childOrders[i].getId(), null));
				}else if(queryType == QUERY_HISTORY){
					childOrders[i].setOrderFoods(QueryOrderFoodDao.getDetailHistory(dbCon, "AND OH.id = " + childOrders[i].getId(), null));
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
	 * Note that if the order is merged(means contains any child order), the order foods to each child order would be combined to parent. 
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
				  " T.minimum_cost, T.service_rate AS tbl_service_rate, T.status AS table_status, " +
				  " O.region_id, O.region_name, O.restaurant_id, " +
				  " O.member_id, O.member_operation_id, " +
				  " O.settle_type, O.pay_type, O.category, O.status, O.discount_id, O.service_rate, O.comment, " +
				  " O.gift_price, O.cancel_price, O.discount_price, O.repaid_price, O.erase_price, O.total_price, O.actual_price, " +
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
				  " OH.id, OH.order_date, OH.seq_id, OH.custom_num, OH.table_id, OH.table_alias, OH.table_name, " +
				  " OH.region_id, OH.region_name, OH.restaurant_id, " +
				  " OH.member_id, OH.member_operation_id, " +
				  " OH.settle_type, OH.pay_type, OH.category, OH.status, 0 AS discount_id, OH.service_rate, OH.comment, " +
				  " OH.gift_price, OH.cancel_price, OH.discount_price, OH.repaid_price, OH.erase_price, OH.total_price, OH.actual_price " +
				  " FROM " + Params.dbName + ".order_history OH " + 
				  " WHERE 1 = 1 " + 
				  (extraCond != null ? extraCond : "") + " " +
				  (orderClause != null ? orderClause : "");
		}else{
			throw new IllegalArgumentException("The query type passed to query order is NOT valid.");
		}
		
		//Get the details to each order.
		dbCon.rs = dbCon.stmt.executeQuery(sql);		

		List<Order> orderResultset = new ArrayList<Order>();
		
		while(dbCon.rs.next()) {
			Order orderInfo = new Order();
			orderInfo.setId(dbCon.rs.getInt("id"));
			orderInfo.setSeqId(dbCon.rs.getInt("seq_id"));
			orderInfo.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			orderInfo.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			orderInfo.setStatus(dbCon.rs.getInt("status"));
			Table table = new Table();
			table.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			table.setCategory(dbCon.rs.getShort("category"));
			table.setTableId(dbCon.rs.getInt("table_id"));
			if(orderInfo.isUnpaid()){
				table.setStatus(Table.Status.IDLE);
			}else{
				table.setStatus(Table.Status.BUSY);
			}
			table.setTableAlias(dbCon.rs.getInt("table_alias"));
			table.setTableName(dbCon.rs.getString("table_name"));
			if(queryType == QUERY_TODAY){
				table.setStatus(dbCon.rs.getShort("table_status"));
				table.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
				table.setServiceRate(dbCon.rs.getFloat("tbl_service_rate"));
			}
			orderInfo.setDestTbl(table);
			orderInfo.getRegion().setRegionId(dbCon.rs.getShort("region_id"));
			orderInfo.getRegion().setName(dbCon.rs.getString("region_name"));

			orderInfo.setCustomNum(dbCon.rs.getShort("custom_num"));
			orderInfo.setCategory(dbCon.rs.getShort("category"));
			orderInfo.setDiscount(new PDiscount(dbCon.rs.getInt("discount_id")));
			orderInfo.setPaymentType(dbCon.rs.getShort("pay_type"));
			orderInfo.setSettleType(dbCon.rs.getShort("settle_type"));
			if(orderInfo.isSettledByMember()){
				orderInfo.setMember(new PMember(dbCon.rs.getInt("member_id")));
				orderInfo.setMemberOperationId(dbCon.rs.getInt("member_operation_id"));
			}
			orderInfo.setStatus(dbCon.rs.getInt("status"));
			orderInfo.setServiceRate(dbCon.rs.getFloat("service_rate"));
			orderInfo.setComment(dbCon.rs.getString("comment"));
			orderInfo.setGiftPrice(dbCon.rs.getFloat("gift_price"));
			orderInfo.setCancelPrice(dbCon.rs.getFloat("cancel_price"));
			orderInfo.setRepaidPrice(dbCon.rs.getFloat("repaid_price"));
			orderInfo.setDiscountPrice(dbCon.rs.getFloat("discount_price"));
			orderInfo.setErasePrice(dbCon.rs.getInt("erase_price"));
			orderInfo.setTotalPrice(dbCon.rs.getFloat("total_price"));
			orderInfo.setActualPrice(dbCon.rs.getFloat("actual_price"));
			if(queryType == QUERY_TODAY){
				orderInfo.setPricePlan(new PricePlan(dbCon.rs.getInt("price_plan_id"),
													 dbCon.rs.getString("price_plan_name"),
													 dbCon.rs.getInt("price_plan_status"),
													 dbCon.rs.getInt("restaurant_id")));
			}
			
			orderResultset.add(orderInfo);
		}

		dbCon.rs.close();
		
		for(Order eachOrder : orderResultset){
			
			StringBuffer childOrderIds = new StringBuffer();		
			/*
			 * If the order status is merged (means containing any child order), 
			 * then get the basic detail (such as child order id) to each child order. 
			 */
			if(eachOrder.isMerged()){
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
						  " WHERE " + " OG.order_id = " + eachOrder.getId();
					
				}else if(queryType == QUERY_HISTORY){
					sql = " SELECT " + 
						  " OGH.sub_order_id, SOH.table_id, SOH.table_name," +
						  " SOH.cancel_price, SOH.gift_price, SOH.discount_price, SOH.erase_price, SOH.total_price, SOH.actual_price, " +
						  " T.table_alias, T.restaurant_id " +
						  " FROM " + 
						  Params.dbName + ".order_group_history OGH " +
						  " JOIN " + Params.dbName + ".sub_order_history SOH " +
						  " ON " + " OGH.sub_order_id = SOH.order_id " +
						  " LEFT JOIN " + Params.dbName + ".table T " + 
						  " ON " + " SOH.table_id = T.table_id " +
						  " WHERE " + " OGH.order_id = " + eachOrder.getId();
				}
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				List<Order> childOrders = new ArrayList<Order>();
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
					subOrder.getDestTbl().setTableAlias(dbCon.rs.getInt("table_alias"));
					subOrder.getDestTbl().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
					subOrder.getDestTbl().setTableName(dbCon.rs.getString("table_name"));
					
					childOrders.add(subOrder);
					//Combine each child order
					childOrderIds.append(dbCon.rs.getInt("sub_order_id") + ",");
				}
				if(childOrderIds.length() != 0){
					childOrderIds.deleteCharAt(childOrderIds.length() - 1);
				}
				eachOrder.setChildOrder(childOrders.toArray(new Order[childOrders.size()]));
				dbCon.rs.close();
				
			}else{
				//Just assign the parent order id.
				childOrderIds.append(eachOrder.getId());
			}
			
			/*
			 * Note that get the order foods of all its child order to parent order in case of merged.
			 */
			if(childOrderIds.length() != 0){
				if(queryType == QUERY_TODAY){
					eachOrder.setOrderFoods(QueryOrderFoodDao.getDetailToday(dbCon, " AND OF.order_id IN(" + childOrderIds + ")", "ORDER BY pay_datetime"));					
				}else if(queryType == QUERY_HISTORY){
					eachOrder.setOrderFoods(QueryOrderFoodDao.getDetailHistory(dbCon, " AND OFH.order_id IN(" + childOrderIds + ")", "ORDER BY pay_datetime"));
				} 
			}
		}
		
		return orderResultset.toArray(new Order[orderResultset.size()]);
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
	 * 		   As a result, we can make use of the length of array to check if the table to query is merged or not, looks like below.<br>
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
			  " AND restaurant_id = " + table.getRestaurantId() +
			  " AND status = " + Order.STATUS_UNPAID;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			childOrderId = dbCon.rs.getInt("id");
			category = dbCon.rs.getShort("category");
			result = new int[1];
			result[0] = childOrderId;
		}else{
			throw new BusinessException("The un-paid order id to table(alias_id = " + table.getAliasId() + ", restaurant_id = " + table.getRestaurantId() + ") does NOT exist.", ProtocolError.ORDER_NOT_EXIST);
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
				throw new BusinessException("The un-paid merged order (sub order id = " + childOrderId + ") does NOT exist.", ProtocolError.ORDER_NOT_EXIST);
			}
			dbCon.rs.close();
		}
		
		return result;
		
	}
	
	/**
	 * 
	 * @param child
	 * @param queryType
	 * @return
	 * @throws Exception
	 */
	public static Order[] getOrderByChild(String extraCond, String orderClause, int queryType, Table childTable) throws Exception{
		DBCon dbCon = new DBCon();
		Order[] order = null;
		try{
			if(childTable != null){
				dbCon.connect();
				int[] oid =  getOrderIdByUnPaidTable(dbCon, childTable);
				if(queryType == DataType.HISTORY.getValue()){
					extraCond += (" AND OH.id = " + oid[1]);
				}else{
					extraCond += (" AND O.id = " + oid[1]);
				}
				if(oid.length == 2){
					order = QueryOrderDao.exec(dbCon, 
							extraCond,
							orderClause,
							queryType
					);
				}
			}
			return order;
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
}
