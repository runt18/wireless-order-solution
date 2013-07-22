package com.wireless.db.frontBusiness;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.AuditBuilder;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.tasteMgr.TasteGroup;
import com.wireless.protocol.Terminal;

public class DailySettleDao {
	
	public static enum SettleType{
		MANUAL, 
		AUTO_MATION
	}
	
	/**
	 * The result to daily settle is as below.
	 * 1 - the amount to the order 
	 * 2 - the amount to the order detail
	 * 3 - the maximum order id 
	 */
	public static final class Result{
		
		private int totalOrder;				//当日已结帐的账单数
		private int totalOrderDetail;		//当日已结帐的账单明细数
		private int totalShift;				//当日交班的记录数
		private int maxOrderId;				//order和order_history表的最大id
		private int maxOrderFoodId;			//order_food和order_food_history表的最大id
		private int maxShiftId;				//shift和shift_history表的最大id
		private int maxTasteGroupId;		//taste_group和taste_group_history表的最大id
		private int maxNormalTasteGroupId;	//normal_taste_group和normal_taste_group_history表的最大id
		private int maxMemberOperationId;	//member_operation和member_operation_history表的最大id
		
		Result(){
			
		}
		
		public int getTotalOrder() {
			return totalOrder;
		}
		
		void setTotalOrder(int totalOrder) {
			this.totalOrder = totalOrder;
		}
		
		public int getTotalOrderDetail() {
			return totalOrderDetail;
		}
		
		void setTotalOrderDetail(int totalOrderDetail) {
			this.totalOrderDetail = totalOrderDetail;
		}
		
		public int getTotalShift() {
			return totalShift;
		}
		
		void setTotalShift(int totalShift) {
			this.totalShift = totalShift;
		}
		
		public int getMaxOrderId() {
			return maxOrderId;
		}
		
		void setMaxOrderId(int maxOrderId) {
			this.maxOrderId = maxOrderId;
		}
		
		public int getMaxOrderFoodId() {
			return maxOrderFoodId;
		}
		
		void setMaxOrderFoodId(int maxOrderFoodId) {
			this.maxOrderFoodId = maxOrderFoodId;
		}
		
		public int getMaxShiftId() {
			return maxShiftId;
		}
		
		void setMaxShiftId(int maxShiftId) {
			this.maxShiftId = maxShiftId;
		}
		
		public int getMaxTasteGroupId() {
			return maxTasteGroupId;
		}
		
		void setMaxTasteGroupId(int maxTasteGroupId) {
			this.maxTasteGroupId = maxTasteGroupId;
		}
		
		public int getMaxNormalTasteGroupId() {
			return maxNormalTasteGroupId;
		}
		
		void setMaxNormalTasteGroupId(int maxNormalTasteGroupId) {
			this.maxNormalTasteGroupId = maxNormalTasteGroupId;
		}
		
		public int getMaxMemberOperationId() {
			return maxMemberOperationId;
		}
		
		void setMaxMemberOperationId(int maxMemberOperationId) {
			this.maxMemberOperationId = maxMemberOperationId;
		}
		
	}

	/**
	 * Perform the daily settlement to all the restaurant.
	 * 
	 * @return the result to daily settlement
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static Result exec() throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon);
			
		}finally{
			dbCon.disconnect();
		}
	}	
	
	/**
	 * Perform the daily settlement to the restaurant whose order record exceeds 1 day.
	 * Note that the database should be connected before invoking this method.
	 * @return the result to daily settlement
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static Result exec(DBCon dbCon) throws SQLException, BusinessException{
		
		String sql;
		
		List<Terminal> terms = new ArrayList<Terminal>();
		
		//Filter the restaurant whose order record exceed 1 day.
		sql = " SELECT restaurant_id " +
			  " FROM " + Params.dbName + ".order " +
			  " WHERE " +
			  " restaurant_id > " + Restaurant.RESERVED_7 +
			  " AND " +
			  " (status = " + Order.Status.PAID.getVal() + " OR " + " status = " + Order.Status.REPAID.getVal() + ")" +
			  " GROUP BY restaurant_id " +
			  " HAVING TO_DAYS(NOW()) - TO_DAYS(MIN(order_date)) > 1 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Terminal term = new Terminal();
			term.restaurantID = dbCon.rs.getInt("restaurant_id");
			term.owner = "system";
			terms.add(term);
		}
		dbCon.rs.close();		
		
		Result result = new Result();
		for(Terminal term : terms){			
			Result eachResult = exec(dbCon, term, SettleType.AUTO_MATION);
			
			result.setTotalOrder(result.getTotalOrder() + eachResult.getTotalOrder());
			result.setTotalOrderDetail(result.getTotalOrderDetail()	+ eachResult.getTotalOrderDetail());
			result.setTotalShift(result.getTotalShift() + eachResult.getTotalShift());
			result.setMaxOrderFoodId(eachResult.getMaxOrderFoodId());
			result.setMaxOrderId(eachResult.getMaxOrderId());
			result.setMaxShiftId(eachResult.getMaxShiftId());
		}
		
		return result;
	}
	
	/**
	 * Perform the daily settlement according to both pin and model.	
	 * 
	 * @param pin
	 *            the pin to this terminal
	 * @param model
	 *            the model to this terminal
	 * @return the result to daily settlement
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The member to query does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Result exec(long pin, short model) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, VerifyPin.exec(dbCon, pin, model), SettleType.MANUAL);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform the daily settlement according to both pin and model.	
	 * Note that the database should be connected before invoking this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param pin
	 *            the pin to this terminal
	 * @param model
	 *            the model to this terminal
	 * @return the result to daily settlement
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The member to query does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Result exec(DBCon dbCon, long pin, short model) throws BusinessException, SQLException{
		return exec(dbCon, VerifyPin.exec(dbCon, pin, model), SettleType.MANUAL);
	}
	
	/**
	 * Perform to daily settle according to a terminal.
	 * @param term
	 * 			  the terminal with both user name and restaurant id
	 * @return the result to daily settle
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @throws BusinessException
	 */
	public static Result exec(Terminal term) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term, SettleType.MANUAL);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to daily settle according to a terminal.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 *            the database connection
	 * @param term
	 * 			  the terminal with both user name and restaurant id
	 * @param type
	 * 			  indicates whether the daily settle is manual or automation
	 * @return the result to daily settle
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @throws BusinessException
	 */
	public static Result exec(DBCon dbCon, Terminal term, SettleType type) throws SQLException, BusinessException{
		Result result = new Result();
		
		//Perform food material consumption
		List<StockAction.AuditBuilder> auditBuilders = new ArrayList<StockAction.AuditBuilder>();
		
		//生成每个部门的商品和原料出库消耗单
		for(Department dept : DepartmentDao.getDepartments(term, null, null)){
			
			for(MaterialCate.Type cateType : MaterialCate.Type.values()){
				
				StockAction.InsertBuilder builder = StockAction.InsertBuilder.newUseUp(term.restaurantID, dept, cateType);
				
				String sql;
				
				sql = " SELECT " +
				      " MAX(M.material_id) AS material_id, MAX(M.name) AS material, " +
					  " SUM(OF.order_count * FM.consumption) AS consume_amount, " +
					  " M.price AS consume_price " +
				      " FROM " + Params.dbName + ".order O " +
					  " JOIN " + Params.dbName + ".order_food OF ON O.id = OF.order_id" +
					  " JOIN " + Params.dbName + ".food_material FM ON OF.food_id = FM.food_id " +
				      " JOIN " + Params.dbName + ".material M ON FM.material_id = M.material_id " +
					  " JOIN " + Params.dbName + ".material_cate MC ON M.cate_id = MC.cate_id " +
					  " WHERE 1 = 1 " +
					  " AND O.restaurant_id = " + term.restaurantID +
					  " AND O.status <> " + Order.Status.UNPAID.getVal() +
					  " AND OF.dept_id = " + dept.getId() +
					  " AND MC.type = " + cateType.getValue() +
					  " GROUP BY OF.food_id " +
					  " HAVING SUM(OF.order_count) > 0 ";
				
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				while(dbCon.rs.next()){
					StockActionDetail detail = new StockActionDetail();
					detail.setMaterialId(dbCon.rs.getInt("material_id"));
					detail.setName(dbCon.rs.getString("material"));
					detail.setPrice(dbCon.rs.getFloat("consume_price"));
					detail.setAmount(dbCon.rs.getFloat("consume_amount"));
					builder.addDetail(detail);
				}
				dbCon.rs.close();
				
				//如果存在出库消耗单则插入
				if(!builder.getStockActionDetails().isEmpty()){
					auditBuilders.add(StockAction.AuditBuilder.newStockActionAudit(StockActionDao.insertStockAction(term, builder)));
				}
			}
			
		}
		
		//如果有盘点任务正在进行，则不审核出入库消耗单
		if(!StockActionDao.isStockTakeChecking(dbCon, term)){
			for(AuditBuilder auditBuilder : auditBuilders){
				StockActionDao.auditStockAction(term, auditBuilder);
			}
		}
		
		String sql;
		String onDuty = null;
		
		/**
		 * Get the date to last daily settlement.
		 * Make the 00:00 of today as on duty if no daily settle record exist before. 
		 */
		sql = " SELECT MAX(off_duty) FROM " + Params.dbName + ".daily_settle_history " +
			  " WHERE " +
			  " restaurant_id = " + term.restaurantID;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			Timestamp offDuty = dbCon.rs.getTimestamp(1);
			if(offDuty != null){
				onDuty = "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(offDuty) + "'";
			}else{
				onDuty = "date_format(NOW(), '%Y-%m-%d')";
			}
		}else{
			onDuty = "date_format(NOW(), '%Y-%m-%d')";
		}
		
		StringBuilder paidOrderCond = new StringBuilder();
		StringBuilder paidMergedOrderCond = new StringBuilder();
		
		//Get the amount and id to paid orders
		sql = " SELECT id, category FROM " + Params.dbName + ".order " +
			  " WHERE " +
			  " (status = " + Order.Status.PAID.getVal() + " OR " + " status = " + Order.Status.REPAID.getVal() + ")" +
			 (term.restaurantID < 0 ? "" : "AND restaurant_id=" + term.restaurantID);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			
			int orderId = dbCon.rs.getInt("id");
			
			paidOrderCond.append(orderId).append(",");
			result.setTotalOrder(result.getTotalOrder() + 1);
			
			if(dbCon.rs.getInt("category") == Order.Category.MERGER_TBL.getVal()){
				paidMergedOrderCond.append(orderId).append(",");
			}
			
		}
		dbCon.rs.close();		
		
		if(paidMergedOrderCond.length() > 0){
			paidMergedOrderCond.deleteCharAt(paidMergedOrderCond.length() - 1);
		}
		
		if(paidOrderCond.length() > 0){
			paidOrderCond.deleteCharAt(paidOrderCond.length() - 1);
		}
		
		if(paidOrderCond.length() > 0){			
			
			//get the amount to order detail 
			sql = " SELECT COUNT(*) FROM " + Params.dbName + ".order_food WHERE order_id IN (" + paidOrderCond + ")";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				result.setTotalOrderDetail(dbCon.rs.getInt(1));				
			}
			dbCon.rs.close();
		}
		
		//get the amount to shift record
		sql = " SELECT COUNT(*) FROM " + Params.dbName + ".shift " +
			  " WHERE 1=1 " +
			  (term.restaurantID < 0 ? "AND restaurant_id <> " + Restaurant.ADMIN : "AND restaurant_id=" + term.restaurantID);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.setTotalShift(dbCon.rs.getInt(1));
		}
		dbCon.rs.close();
		
		//Calculate the max order id from both today and history.
		sql = " SELECT MAX(id) + 1 FROM (" + 
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order " +
			  " UNION " +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order_history) AS all_order";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.setMaxOrderId(dbCon.rs.getInt(1));
		}
		dbCon.rs.close();
		
		//Calculate the max order food id from both today and history.
		sql = " SELECT MAX(id) + 1 FROM (" +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order_food " +
			  " UNION " +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order_food_history) AS all_order_food";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.setMaxOrderFoodId(dbCon.rs.getInt(1));
		}
		dbCon.rs.close();
		
		//Calculate the max shift id from both today and history.
		sql = " SELECT MAX(id) + 1 FROM (" +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".shift " +
			  " UNION " +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".shift_history) AS all_shift";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.setMaxShiftId(dbCon.rs.getInt(1));
		}
		dbCon.rs.close();
		
		//Calculate the max taste group id from both today and history.
		sql = " SELECT MAX(taste_group_id) + 1 " +
			  " FROM " +
			  " (SELECT MAX(taste_group_id) AS taste_group_id FROM " + Params.dbName + ".taste_group" +
			  " UNION " +
			  " SELECT MAX(taste_group_id) AS taste_group_id FROM " + Params.dbName + ".taste_group_history) AS all_taste_group";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.setMaxTasteGroupId(dbCon.rs.getInt(1));
		}
		dbCon.rs.close();
		
		//Calculate the max normal taste group id from both today and history
		sql = " SELECT MAX(normal_taste_group_id) + 1 " +
			  " FROM " +
			  " (SELECT MAX(normal_taste_group_id) AS normal_taste_group_id FROM " + Params.dbName + ".normal_taste_group" +
			  " UNION " +
			  " SELECT MAX(normal_taste_group_id) AS normal_taste_group_id FROM " + Params.dbName + ".normal_taste_group_history) AS all_normal_taste_group";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.setMaxNormalTasteGroupId(dbCon.rs.getInt(1));
		}
		dbCon.rs.close();
		
		//Calculate the max member operation id from both today and history
		sql = " SELECT MAX(id) + 1 " +
			  " FROM " +
			  " (SELECT MAX(id) AS id FROM " + Params.dbName + ".member_operation" +
			  " UNION " +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".member_operation_history) AS all_mo_id ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.setMaxMemberOperationId(dbCon.rs.getInt(1));
		}
		dbCon.rs.close();
		
		int orderIdToAdmin = 0;
		//Get the order id attached to admin.
		sql = "SELECT id FROM " + Params.dbName + ".order WHERE restaurant_id=" + Restaurant.ADMIN;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			orderIdToAdmin = dbCon.rs.getInt("id");
		}
		dbCon.rs.close();
		
		int ofIdToAdmin = 0;
		int tgIdToAdmin = 0;
		//Get the order food id and taste group id attached to admin.
		sql = "SELECT id, taste_group_id FROM " + Params.dbName + ".order_food WHERE order_id=" + orderIdToAdmin;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			ofIdToAdmin = dbCon.rs.getInt("id");
			tgIdToAdmin = dbCon.rs.getInt("taste_group_id");
		}
		dbCon.rs.close();
		
		final String subOrderItem = "`order_id`, `table_id`, `table_name`, `cancel_price`, " +
									"`gift_price`, `discount_price`, `erase_price`, `total_price`, `actual_price`";
		
		final String orderGroupItem = "`order_id`, `sub_order_id`, `restaurant_id`";
		
		final String orderItem = "`id`, `seq_id`, `restaurant_id`, `birth_date`, `order_date`, `status`, " +
				"`cancel_price`, `discount_price`, `gift_price`, `repaid_price`, `erase_price`, `total_price`, `actual_price`, `custom_num`," + 
				"`waiter`, `settle_type`, `pay_type`, `category`, `member_id`, `member_operation_id`, `terminal_pin`, `terminal_model`, " +
				"`region_id`, `region_name`, `table_alias`, `table_name`, `service_rate`, `comment`";

//		final String memberOperationItem = "`id`, `restaurant_id`, `staff_id`, `staff_name`, " +
//										   "`member_id`, `member_card_id`, `member_card_alias`," +
//										   "`operate_seq`, `operate_date`, `operate_type`, " +
//										   "`pay_type`, `pay_money`, `charge_type`, `charge_money`," +
//										   "`delta_base_money`, `delta_extra_money`, `delta_point`," +
//										   "`remaining_base_money`, `remaining_extra_money`, `remaining_point`";
		
		final String shiftItem = "`id`, `restaurant_id`, `name`, `on_duty`, `off_duty`";
		
		try{
			dbCon.conn.setAutoCommit(false);
			
			if(result.getTotalOrder() > 0){
			
				if(paidMergedOrderCond.length() > 0){
					
					String querySubId = " SELECT " + " sub_order_id " + " FROM " + Params.dbName + ".order_group WHERE order_id IN (" + paidMergedOrderCond + ")";
					
					//Move the paid child order details from "order_food" to "order_food_history"
					moveOrderFood(dbCon, querySubId);

					//Move the paid child order taste group from 'taste_group' to 'taste_group_history' except the empty taste group.
					moveTasteGroup(dbCon, querySubId);
					
					//Move the paid child order normal taste group from 'normal_taste_group' to 'normal_taste_group_history' except the empty normal taste group.
					moveNormalTasteGroup(dbCon, querySubId);
					
					//Move the paid child orders from "sub_order" to "sub_order_history"
					sql = " INSERT INTO " + Params.dbName + ".sub_order_history (" + subOrderItem + " ) " +
						  " SELECT " + subOrderItem + " FROM " + Params.dbName + ".sub_order WHERE order_id IN (" +
						  " SELECT " + " sub_order_id " + " FROM " + Params.dbName + ".order_group WHERE order_id IN (" + paidMergedOrderCond + ")" + ")";
					dbCon.stmt.executeUpdate(sql);
					
					//Move the paid order group from "order_group" to "order_group_history"
					sql = " INSERT INTO " + Params.dbName + ".order_group_history (" + orderGroupItem + ")" +
						  " SELECT " + orderGroupItem + " FROM " + Params.dbName + ".order_group WHERE order_id IN (" + paidMergedOrderCond + ")";
					dbCon.stmt.executeUpdate(sql);					
					
				}				
				//Move the paid order from "order" to "order_history".
				sql = " INSERT INTO " + Params.dbName + ".order_history (" + orderItem + ") " + 
					  " SELECT " + orderItem + " FROM " + Params.dbName + ".order WHERE id IN " + "(" + paidOrderCond + ")";
				dbCon.stmt.executeUpdate(sql);
			
				//Move the paid order details from "order_food" to "order_food_history".
				moveOrderFood(dbCon, paidOrderCond.toString());
			
				//Move the paid order taste group from 'taste_group' to 'taste_group_history' except the empty taste group.
				moveTasteGroup(dbCon, paidOrderCond.toString());
				
				//Move the paid order normal taste group from 'normal_taste_group' to 'normal_taste_group_history' except the empty normal taste group.
				moveNormalTasteGroup(dbCon, paidOrderCond.toString());
			}
			
			//Move the member operation record from 'member_operation' to 'member_operation_history'
//			sql = " INSERT INTO " + Params.dbName + ".member_operation_history (" + memberOperationItem + ")" +
//				  " SELECT " + memberOperationItem + " FROM " + Params.dbName + ".member_operation";
			sql = " INSERT INTO " + Params.dbName + ".member_operation_history" +
				  " SELECT * FROM " + Params.dbName + ".member_operation" + 
				  " WHERE restaurant_id <> " + Restaurant.ADMIN;
			dbCon.stmt.executeUpdate(sql);
			
			//Move the shift record from 'shift' to 'shift_history'.
			sql = " INSERT INTO " + Params.dbName + ".shift_history (" + shiftItem + ") " +
				  " SELECT " + shiftItem + " FROM " + Params.dbName + ".shift " +
				  " WHERE " + (term.restaurantID < 0 ? "" : "restaurant_id=" + term.restaurantID);
			dbCon.stmt.executeUpdate(sql);
			
			//Create the daily settle record
			sql = " INSERT INTO " + Params.dbName + ".daily_settle_history (`restaurant_id`, `name`, `on_duty`, `off_duty`) VALUES (" +
				  term.restaurantID + ", " +
				  "'" + (term.owner == null ? "" : term.owner) + "', " +
				  onDuty + ", " +
				  " NOW() " +
				  " ) ";

			//Insert the daily settle record in case of manual.
			if(type == SettleType.MANUAL){
				dbCon.stmt.executeUpdate(sql);				
			}else{
				//Insert the record if the amount of rest orders is greater than zero in case of automation.
				if(result.getTotalOrder() > 0){
					dbCon.stmt.executeUpdate(sql);
				}
			}
			
			if(paidMergedOrderCond.length() > 0){
				//Delete the paid child order details from "order_food" table
				sql = " DELETE FROM " + Params.dbName + ".order_food WHERE order_id IN(" +
					  " SELECT " + " sub_order_id " + " FROM " + Params.dbName + ".order_group WHERE order_id IN (" + paidMergedOrderCond + ")" + ")";
				dbCon.stmt.executeUpdate(sql);
				
				//Delete the paid child order from "sub_order" table.
				sql = " DELETE FROM " + Params.dbName + ".sub_order WHERE order_id IN(" +
					  " SELECT " + " sub_order_id " + " FROM " + Params.dbName + ".order_group WHERE order_id IN (" + paidMergedOrderCond + ")" + ")";
				dbCon.stmt.executeUpdate(sql);
				
				//Delete the paid order group from "order_group" table.
				sql = " DELETE FROM " + Params.dbName + ".order_group WHERE order_id IN (" + paidMergedOrderCond + ")" ;
				dbCon.stmt.executeUpdate(sql);			
			}
			
			if(paidOrderCond.length() > 0){
			
				//Delete the paid order normal taste group except the empty normal taste group
				sql = " DELETE FROM " + Params.dbName + ".normal_taste_group " +
					  " WHERE " +
					  " normal_taste_group_id IN (" +
					  " SELECT normal_taste_group_id " +
					  " FROM " + Params.dbName + ".order_food OF " + " JOIN " + Params.dbName + ".taste_group TG" +
					  " ON OF.taste_group_id = TG.taste_group_id " +
					  " WHERE " +
					  " OF.order_id IN (" + paidOrderCond + ")" +
					  " ) " + 
					  " AND " +
					  " normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID;
				dbCon.stmt.executeUpdate(sql);
				
				//Delete the paid order taste group except the empty taste group
				sql = " DELETE FROM " + Params.dbName + ".taste_group" +
					  " WHERE " +
					  " taste_group_id IN (" +
					  " SELECT taste_group_id FROM " + Params.dbName + ".order_food" +
					  " WHERE " + 
					  " order_id IN (" + paidOrderCond + ")" +
					  " ) " + 
					  " AND " +
					  " taste_group_id <> " + TasteGroup.EMPTY_TASTE_GROUP_ID;
				dbCon.stmt.executeUpdate(sql);				
			
				
				//Delete the paid order food from 'order_food' table.
				sql = " DELETE FROM " + Params.dbName + ".order_food WHERE order_id IN (" + paidOrderCond + ")";
				dbCon.stmt.executeUpdate(sql);
				
				//Delete the paid order from "order" table.
				sql = " DELETE FROM " + Params.dbName + ".order WHERE id IN ( " + paidOrderCond + ")";
				dbCon.stmt.executeUpdate(sql);
			
			}
			
			//Delete the shift record from "shift".
			sql = " DELETE FROM " + Params.dbName + ".shift WHERE " + (term.restaurantID < 0 ? "" : "restaurant_id=" + term.restaurantID);
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the member operation 
			sql = " DELETE FROM " + Params.dbName + ".member_operation";
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the taste group record attached to admin.
			sql = " DELETE FROM " + Params.dbName + ".taste_group WHERE taste_group_id = " + tgIdToAdmin;
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the order_food record attached to admin.
			sql = " DELETE FROM " + Params.dbName + ".order_food WHERE id = " + ofIdToAdmin;
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the order record with max id.
			sql = " DELETE FROM " + Params.dbName + ".order WHERE restaurant_id=" + Restaurant.ADMIN;
			dbCon.stmt.executeUpdate(sql);
			
			//delete the shift record to root
			sql = " DELETE FROM " + Params.dbName + ".shift WHERE restaurant_id=" + Restaurant.ADMIN;
			dbCon.stmt.executeUpdate(sql);
			
			//Insert a order record with the max order id to root.
			sql = "INSERT INTO " + Params.dbName + ".order (`id`, `restaurant_id`, `order_date`) VALUES (" + 
				  result.getMaxOrderId() + ", " +
				  Restaurant.ADMIN + ", " +
				  0 +
				  ")";
			dbCon.stmt.executeUpdate(sql);
			
			//Insert a order_food record with the max order food id, max taste group id to root.
			sql = "INSERT INTO " + Params.dbName + ".order_food (`id`, `order_id`, `taste_group_id`, `order_date`) VALUES (" +
				  result.getMaxOrderFoodId() + ", " +
				  result.getMaxOrderId() + ", " +
				  result.getMaxTasteGroupId() + ", " +
				  0 +
				  ")";
			dbCon.stmt.executeUpdate(sql);
			
			//Insert a record with the max taste group id and max normal taste group id.
			sql = " INSERT INTO " + Params.dbName + ".taste_group" +
				  " (`taste_group_id`, `normal_taste_group_id`) " +
				  " VALUES " +
				  " ( " +
				  result.getMaxTasteGroupId() + ", " +
				  result.getMaxNormalTasteGroupId() +
				  " ) ";
			dbCon.stmt.executeUpdate(sql);
			
			//Insert a record with max member operation id 
			sql = " INSERT INTO " + Params.dbName + ".member_operation" +
				  " (`id`, `restaurant_id`, `staff_id`, `member_id`, `member_name`, `member_mobile`, `operate_seq`, `operate_date`, `operate_type`) " +
				  " VALUES " +
				  " ( " +
				  result.getMaxMemberOperationId() + "," +
				  Restaurant.ADMIN + "," +
				  " 0, 0, '', '', '', 0, 0 " +
				  " ) ";
			dbCon.stmt.executeUpdate(sql);
			
			//Insert a shift record with the max shift id to root.
			sql = "INSERT INTO " + Params.dbName + ".shift (`id`, `restaurant_id`) VALUES (" +
				  result.getMaxShiftId() + ", " +
				  Restaurant.ADMIN +
				  ")";
			dbCon.stmt.executeUpdate(sql);
			
			dbCon.conn.commit();
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}catch(Exception e){
			dbCon.conn.rollback();
			throw new BusinessException(e.getMessage());
			
		}finally{
			dbCon.conn.setAutoCommit(true);
		}
		
		return result;
	}
	
	private static void moveOrderFood(DBCon dbCon, String orderIdCond) throws SQLException{
		
		final String orderFoodItem = "`id`,`restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_date`, `order_count`," + 
				"`unit_price`,`name`, `food_status`, `taste_group_id`, `cancel_reason_id`, `cancel_reason`," +
				"`discount`, `dept_id`, `kitchen_id`, `kitchen_alias`," +
				"`waiter`, `is_temporary`, `is_paid`";

		
		String sql;
		
		sql = " INSERT INTO " + Params.dbName + ".order_food_history (" + orderFoodItem + ") " +
			  " SELECT " + orderFoodItem + " FROM " + Params.dbName + ".order_food " +
			  " WHERE " +
			  " order_id IN ( " + orderIdCond + " ) ";
		
		dbCon.stmt.executeUpdate(sql);
	}
	
	private static void moveTasteGroup(DBCon dbCon, String orderIdCond) throws SQLException{
		
		final String tasteGroupItem = "`taste_group_id`, " +
				  "`normal_taste_group_id`, `normal_taste_pref`, `normal_taste_price`, " +
				  "`tmp_taste_id`, `tmp_taste_pref`, `tmp_taste_price`";
		
		String sql;
		
		sql = " INSERT INTO " + Params.dbName + ".taste_group_history (" + tasteGroupItem + " ) " +
			  " SELECT " + tasteGroupItem + " FROM " + Params.dbName + ".taste_group" +
			  " WHERE " +
			  " taste_group_id <> " + TasteGroup.EMPTY_TASTE_GROUP_ID +
			  " AND taste_group_id IN (" +
			  " SELECT taste_group_id FROM " + Params.dbName + ".order_food WHERE order_id IN (" + orderIdCond + " ) " +
			  " ) ";
		
		dbCon.stmt.executeUpdate(sql);
	}
	
	private static void moveNormalTasteGroup(DBCon dbCon, String orderIdCond) throws SQLException{
		
		final String normalTasteGroupItem = "`normal_taste_group_id`, `taste_id`";
		
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".normal_taste_group_history (" + normalTasteGroupItem + ")" +
			  " SELECT " + normalTasteGroupItem + " FROM " + Params.dbName + ".normal_taste_group" +
			  " WHERE " +
			  " normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID +
			  " AND " +
			  " normal_taste_group_id IN(" +
			  " SELECT normal_taste_group_id " +
			  " FROM " + Params.dbName + ".order_food OF " + " JOIN " + Params.dbName + ".taste_group TG" +
			  " ON OF.taste_group_id = TG.taste_group_id " +
			  " WHERE " +
			  " OF.order_id IN (" + orderIdCond + ")" +
			  " ) ";
		
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Check to see whether the paid order is exist before daily settlement to a specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal attached with the restaurant id
	 * @return
	 * 			an integer array holding the paid order id, 
	 * 			return int[0] if no paid order exist. 
	 * @throws SQLException
	 */
	public static int[] check(DBCon dbCon, Terminal term) throws SQLException{
		String sql;
		int[] restOrderID = new int[0];
		
		/**
		 * Get the last off duty date from both shift and shift_history,
		 */
		String lastOffDuty;
		sql = "SELECT MAX(off_duty) FROM (" +
		 	  "SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id=" + term.restaurantID + " UNION " +
			  "SELECT off_duty FROM " + Params.dbName + ".shift_history WHERE restaurant_id=" + term.restaurantID + ") AS all_off_duty";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			Timestamp offDuty = dbCon.rs.getTimestamp(1);
			if(offDuty == null){
				lastOffDuty = "2011-07-30 00:00:00";
			}else{
				lastOffDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(offDuty);
			}
		}else{
			lastOffDuty = "2011-07-30 00:00:00";
		}
		dbCon.rs.close();

		
		/**
		 * In the case perform the daily settle to a specific restaurant,
		 * get the paid orders which has NOT been shifted between the last off duty and now,
		 */
		sql = " SELECT id FROM " + Params.dbName + ".order WHERE " +
			  " restaurant_id = " + term.restaurantID + " AND " +
			  " (status = " + Order.Status.PAID.getVal() + " OR " + " status = " + Order.Status.REPAID.getVal() + ")" + " AND " +
			  " order_date BETWEEN " +
			  "'" + lastOffDuty + "'" + " AND " + "NOW()";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<Integer> orderIDs = new ArrayList<Integer>();
		while(dbCon.rs.next()){
			orderIDs.add(dbCon.rs.getInt("id"));
		}
		dbCon.rs.close();
		
		restOrderID = new int[orderIDs.size()];
		for(int i = 0; i < restOrderID.length; i++){
			restOrderID[i] = orderIDs.get(i).intValue();
		}
		
		return restOrderID;
	}
	
}
