package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderSummary;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DateType;

public class OrderDao {

	public static class Filter{
		private final DateType dateType;
		private int orderId = -1;
		private int tableAlias = -1;
		private String tableName;
		private short regionId = -1;
		private DutyRange range;
		private Order.PayType payType;
		
		public Filter(DateType dateType){
			this.dateType = dateType;
		}
		
		public void setOrderId(int orderId){
			this.orderId = orderId;
		}
		
		@Override
		public String toString(){
			StringBuilder filterCond = new StringBuilder();
			filterCond.append("AND 1 = 1");
			if(orderId > 0){
				filterCond.append("AND O.id = " + orderId);
			}
			return filterCond.toString();
		}
	}
	
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
			throw new BusinessException(FrontBusinessError.ORDER_NOT_EXIST);
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
		return getById(dbCon, staff, OrderDao.getOrderIdByUnPaidTable(dbCon, staff, TableDao.getTableByAlias(dbCon, staff, tableAlias)), DateType.TODAY);
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
		return getById(dbCon, staff, OrderDao.getOrderIdByUnPaidTable(dbCon, staff, TableDao.getTableByAlias(dbCon, staff, tableAlias)), DateType.TODAY);
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
			throw new BusinessException("The order(id = " + orderId + ") does NOT exist.", FrontBusinessError.ORDER_NOT_EXIST);
		}else{
			return results.get(0);
		}
	}
	
	/**
	 * Get the order detail information according to the specific extra condition and order clause. 
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
	public static List<Order> getByCond(Staff staff, String extraCond, String orderClause, DateType dateType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause, dateType);
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
			//Get the order foods to each order.
			if(dateType == DateType.TODAY){
				eachOrder.setOrderFoods(OrderFoodDao.getDetailToday(dbCon, staff, " AND OF.order_id = " + eachOrder.getId(), null));					
			}else if(dateType == DateType.HISTORY){
				eachOrder.setOrderFoods(OrderFoodDao.getDetailHistory(dbCon, staff, " AND OFH.order_id = " + eachOrder.getId(), null));
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
				  " O.member_operation_id, " +
				  " O.settle_type, O.pay_type, O.category, O.status, O.service_rate, O.comment, " +
				  " O.discount_id, DIST.name AS discount_name, " +
				  " O.gift_price, O.cancel_price, O.discount_price, O.repaid_price, O.erase_price, O.coupon_price, O.total_price, O.actual_price " +
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
				  " OH.member_operation_id, OH.coupon_price, " +
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
			orderInfo.setWaiter(dbCon.rs.getString("waiter"));
			
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
			orderInfo.setCouponPrice(dbCon.rs.getFloat("coupon_price"));
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
	 * @param staff
	 * 			the staff to perform this action
	 * @param table 
	 * 			the table information containing the alias id and associated restaurant id
	 * @return the unpaid order id to this table if exist
	 * @throws BusinessException 
	 * 			throws if either of cases below<br>
	 * 			<li>the table to query is IDLE<br>
	 * 			<li>the unpaid order to this table does NOT exist<br>
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement
	 */
	public static int getOrderIdByUnPaidTable(DBCon dbCon, Staff staff, Table table) throws BusinessException, SQLException{
		
		String sql;
		
		//Get the order id associated with this table.
		int orderId;
		
		sql = " SELECT " +
			  " id " +
			  " FROM " + Params.dbName + ".order " +
			  " WHERE " +
			  " table_alias = " + table.getAliasId() +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  " AND status = " + Order.Status.UNPAID.getVal();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			orderId = dbCon.rs.getInt("id");
		}else{
			throw new BusinessException("The un-paid order id to table(alias_id = " + table.getAliasId() + ", restaurant_id = " + table.getRestaurantId() + ") does NOT exist.", FrontBusinessError.ORDER_NOT_EXIST);
		}
		dbCon.rs.close();
		
		return orderId;
	}
	
}
