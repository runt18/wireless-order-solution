package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderSummary;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DateType;

public class OrderDao {

	/**
	 * Get the status to a specific order.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param orderId
	 * 			the order id 
	 * @return the status to this order
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the order to check does NOT exist
	 */
	public static Order.Status getStatusById(DBCon dbCon, Staff staff, int orderId) throws SQLException, BusinessException{
		String sql;
		sql = " SELECT status FROM " + Params.dbName + ".order WHERE id = " + orderId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			int status = dbCon.rs.getInt("status");
			dbCon.rs.close();
			return Order.Status.valueOf(status);
		}else{
			throw new BusinessException(ProtocolError.ORDER_NOT_EXIST);
		}
	}
	
	/**
	 * Get the unpaid order detail information to the specific restaurant and table 
	 * regardless of the merged status.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 *            the staff to perform this action
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
	public static Order getByTableAlias(DBCon dbCon, Staff staff, int tableAlias) throws BusinessException, SQLException {		
		return getById(dbCon, staff, OrderDao.getOrderIdByUnPaidTable(dbCon, TableDao.getTableByAlias(dbCon, staff, tableAlias))[0], DateType.TODAY);
	}
	
	/**
	 * Get the unpaid order detail information to the specific restaurant and
	 * table. If the table is merged, get its parent order, otherwise get the
	 * order of its own.
	 * @param staff
	 * 			the staff to perform this action
	 * @param tableAlias
	 *            the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException
	 *             throws if one of cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The table to query does NOT exist.<br>
	 *             - The unpaid order to this table does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order getByTableAliasDync(Staff staff, int tableAlias) throws BusinessException, SQLException {		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByTableAliasDync(dbCon, staff, tableAlias);
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
	 * @param staff
	 *            the staff to perform this action
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
	public static Order getByTableAliasDync(DBCon dbCon, Staff staff, int tableAlias) throws BusinessException, SQLException {
		
		//If the table is merged, get its parent order.
		//Otherwise get the order of its own.
		int[] unpaidId = OrderDao.getOrderIdByUnPaidTable(dbCon, TableDao.getTableByAlias(dbCon, staff, tableAlias));
		if(unpaidId.length > 1){
			return getById(dbCon, staff, unpaidId[1], DateType.TODAY);
		}else{
			return getById(dbCon, staff, unpaidId[0], DateType.TODAY);
		}
	}
	
	/**
	 * Get the unpaid order detail information to the specific restaurant and table regardless of the merged status.
	 * @param staff
	 *            the staff to perform this action
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
	public static Order getByTableAlias(Staff staff, int tableAlias) throws BusinessException, SQLException {		

		DBCon dbCon = new DBCon();
		try{
			
			dbCon.connect();			
			return getByTableAlias(dbCon, staff, tableAlias);
			
		}finally{
			dbCon.disconnect();
		}

	}
	
	/**
	 * Get the order detail information according to the specific order id. Note
	 * @param staff
	 * 			  the staff to perform this action
	 * @param orderId
	 *            the order id to query
	 * @param dateType
	 * 			  indicates which date type {@link DateType} should use
	 * @return the order detail information
	 * @throws BusinessException
	 * 			   throws if the order to this id does NOT exist
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order getById(Staff staff, int orderId, DateType dateType) throws BusinessException, SQLException {
		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();

			return getById(dbCon, staff, orderId, dateType);

		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the order detail information according to the specific order id. 
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param orderId
	 *            the order id to query
	 * @param dateType
	 * 			  indicates which date type {@link DateType} should use
	 * @return the order detail information
	 * @throws BusinessException
	 * 			   throws if the order to this id does NOT exist
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order getById(DBCon dbCon, Staff staff, int orderId, DateType dateType) throws BusinessException, SQLException{
		
		String extraCond = null;
		if(dateType == DateType.TODAY){
			extraCond = "AND O.id = " + orderId;
		}else if(dateType == DateType.HISTORY){
			extraCond = " AND OH.id = " + orderId;
		}
		
		List<Order> results = getByCond(dbCon, staff, extraCond, null, dateType);
		if(results.isEmpty()){
			throw new BusinessException("The order(id = " + orderId + ") does NOT exist.", ProtocolError.ORDER_NOT_EXIST);
		}else{
			return results.get(0);
		}
	}
	
	/**
	 * Get the details to both order and its children according to the specific order id. 
	 * @param term
	 * 			  the terminal
	 * @param orderId
	 *            the order id to query
	 * @param dateType
	 * 			  indicates which date type {@link DateType} should use
	 * @return the order detail information
	 * @throws BusinessException
	 * 			   Throws if the order to this id does NOT exist.
	 * @throws SQLException
	 *             Throws if fail to execute any SQL statement
	 */
	public static Order execWithChildDetailByID(Staff term, int orderId, DateType queryType) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getWithChildDetailById(dbCon, term, orderId, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the details to both order and its children according to the specific order id. 
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param orderId
	 *            the order id to query
	 * @param dateType
	 * 			  indicates which date type {@link DateType} should use
	 * @return the order detail information
	 * @throws BusinessException
	 * 			   Throws if the order to this id does NOT exist.
	 * @throws SQLException
	 *             Throws if fail to execute any SQL statement
	 */
	public static Order getWithChildDetailById(DBCon dbCon, Staff staff, int orderId, DateType dateType) throws BusinessException, SQLException{
		Order result = getById(dbCon, staff, orderId, dateType);
		if(result.isMerged() && result.hasChildOrder()){
			for(Order childOrder : result.getChildOrder()){
				if(dateType == DateType.TODAY){
					childOrder.setOrderFoods(OrderFoodDao.getDetailToday(dbCon, staff, "AND O.id = " + childOrder.getId(), null));
				}else if(dateType == DateType.HISTORY){
					childOrder.setOrderFoods(OrderFoodDao.getDetailHistory(dbCon, staff, "AND OH.id = " + childOrder.getId(), null));
				}

			}
		}
		return result;
	}
	
	/**
	 * Get the order detail information according to the specific extra condition and order clause. 
	 * @param term
	 * 			  the terminal
	 * @param extraCond
	 *            the extra condition to query
	 * @param orderClause
	 * 			  the order clause to query
	 * @param dateType
	 * 			  indicates which date type {@link DateType} should use
	 * @return the list holding the result to each order detail information
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @throws BusinessException 
	 * 			   throws if any associated taste group is NOT found
	 */
	public static List<Order> getByCond(Staff term, String extraCond, String orderClause, DateType dateType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, term, extraCond, orderClause, dateType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the order detail information according to the specific extra condition and order clause. 
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to query
	 * @param orderClause
	 * 			  the order clause to query
	 * @param dateType
	 * 			  indicates which date type {@link DateType} should use
	 * @return the list holding the result to each order detail information
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @throws BusinessException 
	 * 			   throws if any associated taste group is NOT found
	 */
	public static List<Order> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause, DateType dateType) throws SQLException, BusinessException{

		List<Order> result = getPureOrder(dbCon, staff, extraCond, orderClause, dateType);
		
		for(Order eachOrder : result){
			
			String sql;
			
			StringBuilder childOrderIds = new StringBuilder();		
			/*
			 * If the order status is merged (means containing any child order), 
			 * then get the basic detail (such as child order id) to each child order. 
			 */
			if(eachOrder.isMerged()){
				if(dateType == DateType.TODAY){
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
					
				}else if(dateType == DateType.HISTORY){
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
				}else{
					throw new IllegalArgumentException("The query type passed to query order is NOT valid.");
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
				eachOrder.setChildOrder(childOrders);
				dbCon.rs.close();
				
			}else{
				//Just assign the parent order id.
				childOrderIds.append(eachOrder.getId());
			}
			
			/*
			 * Note that get the order foods of all its child order to parent order in case of merged.
			 */
			if(childOrderIds.length() != 0){
				if(dateType == DateType.TODAY){
					eachOrder.setOrderFoods(OrderFoodDao.getDetailToday(dbCon, staff, " AND OF.order_id IN(" + childOrderIds + ")", "ORDER BY pay_datetime"));					
				}else if(dateType == DateType.HISTORY){
					eachOrder.setOrderFoods(OrderFoodDao.getDetailHistory(dbCon, staff, " AND OFH.order_id IN(" + childOrderIds + ")", "ORDER BY pay_datetime"));
				} 
			}
		}
		
		return result;
	}
	
	/**
	 * Get the pure order according to specified restaurant and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @param dateType
	 * 			  indicates which date type {@link DateType} should use
	 * @return the list holding the pure order
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Order> getPureOrder(DBCon dbCon, Staff staff, String extraCond, String orderClause, DateType dateType) throws SQLException{
		String sql;
		if(dateType == DateType.TODAY){
			sql = " SELECT " +
				  " O.id, O.order_date, O.seq_id, O.custom_num, O.table_id, O.table_alias, O.table_name, " +
				  " T.minimum_cost, T.service_rate AS tbl_service_rate, T.status AS table_status, " +
				  " O.waiter, " +
				  " O.region_id, O.region_name, O.restaurant_id, " +
				  " O.member_id, O.member_operation_id, " +
				  " O.settle_type, O.pay_type, O.category, O.status, O.service_rate, O.comment, " +
				  " O.discount_id, DIST.name AS discount_name, " +
				  " O.gift_price, O.cancel_price, O.discount_price, O.repaid_price, O.erase_price, O.total_price, O.actual_price " +
				  " FROM " + 
				  Params.dbName + ".order O " +
				  " LEFT JOIN " + Params.dbName + ".table T " +
				  " ON O.table_id = T.table_id " +
				  " LEFT JOIN " + Params.dbName + ".discount DIST " +
				  " ON O.discount_id = DIST.discount_id " +
				  " WHERE 1 = 1 " + 
				  " AND O.restaurant_id = " + staff.getRestaurantId() + " " +
				  (extraCond != null ? extraCond : "") + " " +
				  (orderClause != null ? orderClause : "");
			
		}else if(dateType == DateType.HISTORY){
			sql = " SELECT " +
				  " OH.id, OH.order_date, OH.seq_id, OH.custom_num, OH.table_id, OH.table_alias, OH.table_name, " +
				  " OH.waiter, " +
				  " OH.region_id, OH.region_name, OH.restaurant_id, " +
				  " OH.member_id, OH.member_operation_id, " +
				  " OH.settle_type, OH.pay_type, OH.category, OH.status, 0 AS discount_id, OH.service_rate, OH.comment, " +
				  " OH.gift_price, OH.cancel_price, OH.discount_price, OH.repaid_price, OH.erase_price, OH.total_price, OH.actual_price " +
				  " FROM " + Params.dbName + ".order_history OH " + 
				  " WHERE 1 = 1 " + 
				  " AND OH.restaurant_id = " + staff.getRestaurantId() + " " +
				  (extraCond != null ? extraCond : "") + " " +
				  (orderClause != null ? orderClause : "");
		}else{
			throw new IllegalArgumentException("The query type passed to query order is NOT valid.");
		}
		
		//Get the details to each order.
		dbCon.rs = dbCon.stmt.executeQuery(sql);		

		List<Order> result = new ArrayList<Order>();
		
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
			if(dateType == DateType.TODAY){
				table.setStatus(dbCon.rs.getShort("table_status"));
				table.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
				table.setServiceRate(dbCon.rs.getFloat("tbl_service_rate"));
			}
			orderInfo.setDestTbl(table);
			orderInfo.getRegion().setRegionId(dbCon.rs.getShort("region_id"));
			orderInfo.getRegion().setName(dbCon.rs.getString("region_name"));

			orderInfo.setCustomNum(dbCon.rs.getShort("custom_num"));
			orderInfo.setCategory(dbCon.rs.getShort("category"));
			
			Discount discount = new Discount(dbCon.rs.getInt("discount_id"));
			if(dateType == DateType.TODAY){
				discount.setName(dbCon.rs.getString("discount_name"));
			}
			orderInfo.setDiscount(discount);
			
			orderInfo.setPaymentType(dbCon.rs.getShort("pay_type"));
			orderInfo.setSettleType(dbCon.rs.getShort("settle_type"));
			if(orderInfo.isSettledByMember()){
				orderInfo.setMember(new Member(dbCon.rs.getInt("member_id")));
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
			
			result.add(orderInfo);
		}

		dbCon.rs.close();
		
		return result;
	}

	/**
	 * Get the pure order according to extra condition and order clause.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @param dateType
	 * 			  indicates which date type {@link DateType} should use
	 * @return the list holding the pure order
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Order> getPureOrder(Staff staff, String extraCond, String orderClause, DateType dateType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureOrder(dbCon, staff, extraCond, orderClause, dateType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the summary to orders to specified restaurant defined in {@link terminal} and other condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param dateType
	 * 			  indicates which date type {@link DateType} should use
	 * @return the order summary
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static OrderSummary getOrderSummary(Staff staff, String extraCond, DateType dateType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getOrderSummary(dbCon, staff, extraCond, dateType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the summary to orders to specified restaurant defined in {@link terminal} and other condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param dateType
	 * 			  indicates which date type {@link DateType} should use
	 * @return the order summary
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static OrderSummary getOrderSummary(DBCon dbCon, Staff staff, String extraCond, DateType dateType) throws SQLException{
		
		String sql;
		
		if(dateType == DateType.TODAY){
			sql = " SELECT " +
				  " COUNT(*) AS total_amount, SUM(total_price) AS total_price, SUM(actual_price) AS total_actual_price, " +
				  " SUM(gift_price) AS total_gift_price, SUM(cancel_price) AS total_cancel_price, SUM(discount_price) AS total_discount_price, " +
				  " SUM(repaid_price) AS total_repaid_price " +
				  " FROM " + Params.dbName + ".order O " +
				  " WHERE 1 = 1 " +
				  " AND O.restaurant_id = " + staff.getRestaurantId() + " " +
				  (extraCond != null ? extraCond : "");
			
		}else if(dateType == DateType.HISTORY){
			sql = " SELECT " +
				  " COUNT(*) AS total_amount, SUM(total_price) AS total_price, SUM(actual_price) AS total_actual_price, " +
				  " SUM(gift_price) AS total_gift_price, SUM(cancel_price) AS total_cancel_price, SUM(discount_price) AS total_discount_price, " +
				  " SUM(repaid_price) AS total_repaid_price, " +
				  " SUM(total_price) AS price " +
				  " FROM " + Params.dbName + ".order_history OH " +
				  " WHERE 1 = 1 " +
				  " AND OH.restaurant_id = " + staff.getRestaurantId() + " " +
				  (extraCond != null ? extraCond : "");
			
		}else{
			throw new IllegalArgumentException("The query type passed to query order is NOT valid.");
		}
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			return new OrderSummary.Builder().setTotalAmount(dbCon.rs.getInt("total_amount"))
					  			             .setTotalPrice(dbCon.rs.getFloat("total_price"))
											 .setTotalActualPrice(dbCon.rs.getFloat("total_actual_price"))
											 .setTotalCancelPrice(dbCon.rs.getFloat("total_cancel_price"))
											 .setTotalDiscountPrice(dbCon.rs.getFloat("total_discount_price"))
											 .setTotalCancelPrice(dbCon.rs.getFloat("total_cancel_price"))
											 .setTotalRepaidPrice(dbCon.rs.getFloat("total_repaid_price")).build();	
		}else{
			return OrderSummary.DUMMY;
		}
		
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
			  " AND status = " + Order.Status.UNPAID.getVal();
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
		if(category == Order.Category.MERGER_CHILD.getVal()){
			sql = " SELECT " +
				  " O.id " +
				  " FROM " +
				  Params.dbName + ".order O " +
				  " JOIN " + Params.dbName + ".order_group OG " + " ON " + " O.id = OG.order_id " +
				  " WHERE 1 = 1" +
				  " AND O.status = " + Order.Status.UNPAID.getVal() +
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
	public static List<Order> getOrderByChild(Staff staff, String extraCond, String orderClause, DateType dateType, Table childTable) throws Exception{
		DBCon dbCon = new DBCon();
		List<Order> order = null;
		try{
			if(childTable != null){
				dbCon.connect();
				int[] oid =  getOrderIdByUnPaidTable(dbCon, childTable);
				if(dateType == DateType.HISTORY){
					extraCond += (" AND OH.id = " + oid[1]);
				}else{
					extraCond += (" AND O.id = " + oid[1]);
				}
				if(oid.length == 2){
					order = OrderDao.getByCond(dbCon, staff, extraCond, orderClause, dateType);
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
