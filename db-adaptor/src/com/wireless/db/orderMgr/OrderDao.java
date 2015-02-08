package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.exception.StaffError;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.Order.Category;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.OrderSummary;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.dishesOrder.TasteGroup;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.serviceRate.ServicePlan;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.util.DateType;

public class OrderDao {

	public static class ExtraCond{
		public final String orderTblAlias;
		private final String orderFoodTbl;
		
		private final DateType dateType;
		private int orderId = -1;		//按账单号
		private int seqId = -1;			//按流水号
		private int tableAlias = -1;	//按餐台号
		private int tableId = -1;		//按餐台号
		private String tableName;		//按餐台名称
		private Region.RegionId regionId;	//按区域
		private DutyRange orderRange;	//按结账日期
		private HourRange hourRange;	//按时间区间
		private PayType payType;		//按付款类型
		private String comment;			//按备注
		private int staffId;			//按员工
		private final List<Order.Status> statusList = new ArrayList<Order.Status>();	//按状态状态
		
		private boolean isRepaid;		//是否有反结帐
		private boolean isDiscount;		//是否有折扣
		private boolean isGift;			//是否有赠送
		private boolean isCancelled;	//是否有退菜
		private boolean isErased;		//是否有抹数
		private boolean isCoupon;		//是否有使用优惠券
		private boolean isTransfer;		//是否有转菜
		
		public ExtraCond(DateType dateType){
			this.dateType = dateType;
			if(dateType == DateType.TODAY){
				orderTblAlias = "O";
				orderFoodTbl = "order_food";
			}else{
				orderTblAlias = "OH";
				orderFoodTbl = "order_food_history";
			}
		}
		
		public ExtraCond setOrderId(int orderId){
			this.orderId = orderId;
			return this;
		}
		
		public ExtraCond setSeqId(int seqId){
			this.seqId = seqId;
			return this;
		}
		
		public ExtraCond setTableId(int tableId){
			this.tableId = tableId;
			return this;
		}
		
		public ExtraCond setTableAlias(int tableAlias){
			this.tableAlias = tableAlias;
			return this;
		}
		
		public ExtraCond setStaff(int staffId){
			this.staffId = staffId;
			return this;
		}
		
		public ExtraCond setStaff(Staff staff){
			this.staffId = staff.getId();
			return this;
		}
		
		public ExtraCond setTableName(String tableName){
			this.tableName = tableName;
			return this;
		}
		
		public ExtraCond setRegionId(Region.RegionId regionId){
			this.regionId = regionId;
			return this;
		}
		
		public ExtraCond setOrderRange(DutyRange orderRange){
			this.orderRange = orderRange;
			return this;
		}
		
		public ExtraCond setHourRange(HourRange hourRange){
			this.hourRange = hourRange;
			return this;
		}
		
		public ExtraCond setPayType(PayType payType){
			this.payType = payType;
			return this;
		}
		
		public ExtraCond setComment(String comment){
			this.comment = comment;
			return this;
		}
		
		public ExtraCond addStatus(Order.Status status){
			this.statusList.add(status);
			return this;
		}
		
		public ExtraCond isRepaid(boolean onOff){
			this.isRepaid = onOff;
			return this;
		}
		
		public ExtraCond isDiscount(boolean onOff){
			this.isDiscount = onOff;
			return this;
		}
		
		public ExtraCond isGift(boolean onOff){
			this.isGift = onOff;
			return this;
		}
		
		public ExtraCond isCancelled(boolean onOff){
			this.isCancelled = onOff;
			return this;
		}

		public ExtraCond isErased(boolean onOff){
			this.isErased = onOff;
			return this;
		}
		
		public ExtraCond isCoupon(boolean onOff){
			this.isCoupon = onOff;
			return this;
		}
		
		public ExtraCond isTransfer(boolean onOff){
			this.isTransfer = onOff;
			return this;
		}
		
		@Override
		public String toString(){

			StringBuilder filterCond = new StringBuilder();
			
			if(orderId >= 0){
				filterCond.append(" AND " + orderTblAlias + ".id = " + orderId);
			}
			if(seqId > 0){
				filterCond.append(" AND " + orderTblAlias + ".seq_id = " + seqId);
			}
			if(tableAlias > 0){
				filterCond.append(" AND " + orderTblAlias + ".table_alias = " + tableAlias);
			}
			if(tableId > 0){
				filterCond.append(" AND " + orderTblAlias + ".table_id = " + tableId);
			}
			if(staffId > 0){
				filterCond.append(" AND " + orderTblAlias + ".staff_id = " + staffId);
			}
			if(tableName != null){
				filterCond.append(" AND " + orderTblAlias + ".table_name LIKE '%" + tableName + "%'");
			}
			if(regionId != null){
				filterCond.append(" AND " + orderTblAlias + ".region_id = " + regionId.getId());
			}
			if(orderRange != null){
				filterCond.append(" AND " + orderTblAlias + ".order_date BETWEEN '" + orderRange.getOnDutyFormat() + "' AND '" + orderRange.getOffDutyFormat() + "'");
			}
			if(hourRange != null){
				filterCond.append(" AND TIME(" + orderTblAlias + ".order_date) BETWEEN '" + hourRange.getOpeningFormat() + "' AND '" + hourRange.getEndingFormat() + "'");
			}
			if(payType != null){
				filterCond.append(" AND " + orderTblAlias + ".pay_type_id = " + payType.getId());
			}
			if(comment != null){
				filterCond.append(" AND " + orderTblAlias + ".comment LIKE '%" + comment + "%'");
			}
			if(!statusList.isEmpty()){
				filterCond.append(" AND " + orderTblAlias + ".status IN(");
				for(Order.Status status : statusList){
					filterCond.append(status.getVal() + ",");
				}
				filterCond.deleteCharAt(filterCond.length() - 1);
				filterCond.append(")");
			}
			if(isRepaid){
				filterCond.append(" AND " + orderTblAlias + ".status = " + Order.Status.REPAID.getVal());
			}
			if(isDiscount){
				filterCond.append(" AND " + orderTblAlias + ".discount_price > 0");
			}
			if(isGift){
				filterCond.append(" AND " + orderTblAlias + ".gift_price > 0");
			}
			if(isCancelled){
				filterCond.append(" AND " + orderTblAlias + ".cancel_price > 0");
			}
			if(isErased){
				filterCond.append(" AND " + orderTblAlias + ".erase_price > 0");
			}
			if(isCoupon){
				filterCond.append(" AND " + orderTblAlias + ".coupon_price > 0");
			}
			if(isTransfer){
				final String sql = " SELECT order_id FROM " + Params.dbName + "." + orderFoodTbl + " WHERE operation = " + OrderFood.Operation.TRANSFER.getVal();;
				filterCond.append(" AND " + orderTblAlias + ".id IN( " + sql + ")");
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
	 * Get the unpaid order detail information to the specific restaurant and table. 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 *            the staff to perform this action
	 * @param tableAlias
	 *            the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException
	 *             throws if the un-paid order to this table does NOT exist 
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static Order getByTableId(DBCon dbCon, Staff staff, int tableId) throws BusinessException, SQLException {		
		List<Order> result = getByCond(dbCon, staff, new OrderDao.ExtraCond(DateType.TODAY).setTableId(tableId).addStatus(Order.Status.UNPAID), null);
		if(result.isEmpty()){
			throw new BusinessException(FrontBusinessError.ORDER_NOT_EXIST);
		}else{
			Order order = result.get(0);
			fillDetail(dbCon, staff, order, DateType.TODAY);
			return order;
		}
	}
	
	/**
	 * Get the unpaid order detail information to the specific restaurant and table. 
	 * @param staff
	 *            the staff to perform this action
	 * @param tableAlias
	 *            the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException
	 *             throws if the un-paid order to this table does NOT exist 
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static Order getByTableId(Staff staff, int tableId) throws BusinessException, SQLException {		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();			
			return getByTableId(dbCon, staff, tableId);
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
		
		List<Order> results = getByCond(dbCon, staff, new ExtraCond(dateType).setOrderId(orderId), null);
		if(results.isEmpty()){
			throw new BusinessException("The order(id = " + orderId + ") does NOT exist.", FrontBusinessError.ORDER_NOT_EXIST);
		}else{
			Order order = results.get(0);
			fillDetail(dbCon, staff, order, dateType);
			return order;
		}
	}
	
	private static void fillDetail(DBCon dbCon, Staff staff, Order order, DateType dateType) throws SQLException, BusinessException{
		//Get the order foods to each order.
		order.setOrderFoods(OrderFoodDao.getDetail(dbCon, staff, new OrderFoodDao.ExtraCond(dateType).setOrder(order)));	
		//Get the mixed payment detail.
		if(order.getPaymentType().isMixed()){
			order.setMixedPayment(MixedPaymentDao.get(dbCon, staff, new MixedPaymentDao.ExtraCond(dateType, order)));
		}
		//Get the associated wx orders.
		for(WxOrder wxOrder : WxOrderDao.getByCond(dbCon, staff, new WxOrderDao.ExtraCond().setOrder(order), null)){
			order.addWxOrder(wxOrder);
		}
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
	public static List<Order> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		String sql;
		if(extraCond.dateType == DateType.TODAY){
			sql = " SELECT " +
				  " O.id, O.order_date, O.seq_id, O.custom_num, O.table_id, O.table_alias, O.table_name, O.staff_id, " +
				  " T.minimum_cost, IFNULL(T.category, 1) AS tbl_category, " +
				  " O.waiter, O.discount_staff, O.discount_date, " +
				  " O.region_id, O.region_name, O.restaurant_id, " +
				  " O.settle_type, O.pay_type_id, IFNULL(PT.name, '其他') AS pay_type_name, O.category, O.status, O.service_plan_id, O.service_rate, O.comment, " +
				  " O.discount_id, DIST.name AS discount_name, " +
				  " O.price_plan_id, O.member_id, " +
				  " O.gift_price, O.cancel_price, O.discount_price, O.repaid_price, O.erase_price, O.coupon_price, O.total_price, O.actual_price " +
				  " FROM " + 
				  Params.dbName + ".order O " +
				  " LEFT JOIN " + Params.dbName + ".table T ON O.table_id = T.table_id " +
				  " LEFT JOIN " + Params.dbName + ".discount DIST ON O.discount_id = DIST.discount_id " +
				  " LEFT JOIN " + Params.dbName + ".pay_type PT ON O.pay_type_id = PT.pay_type_id " +
				  " WHERE 1 = 1 " + 
				  " AND O.restaurant_id = " + staff.getRestaurantId() + " " +
				  (extraCond != null ? extraCond.toString() : "") + " " +
				  (orderClause != null ? orderClause : "");
			
		}else if(extraCond.dateType == DateType.HISTORY){
			sql = " SELECT " +
				  " OH.id, OH.order_date, OH.seq_id, OH.custom_num, OH.table_id, OH.table_alias, OH.table_name, " +
				  " OH.waiter, OH.discount_staff, OH.discount_date, " +
				  " OH.region_id, OH.region_name, OH.restaurant_id, " +
				  " OH.coupon_price, " +
				  " OH.settle_type, OH.pay_type_id, IFNULL(PT.name, '其他') AS pay_type_name, OH.category, OH.status, OH.service_rate, OH.comment, " +
				  " OH.gift_price, OH.cancel_price, OH.discount_price, OH.repaid_price, OH.erase_price, OH.total_price, OH.actual_price " +
				  " FROM " + Params.dbName + ".order_history OH " + 
				  " LEFT JOIN " + Params.dbName + ".pay_type PT ON PT.pay_type_id = OH.pay_type_id " +
				  " WHERE 1 = 1 " + 
				  " AND OH.restaurant_id = " + staff.getRestaurantId() + " " +
				  (extraCond != null ? extraCond.toString() : "") + " " +
				  (orderClause != null ? orderClause : "");
		}else{
			throw new IllegalArgumentException("The query type passed to query order is NOT valid.");
		}
		
		//Get the details to each order.
		dbCon.rs = dbCon.stmt.executeQuery(sql);		

		List<Order> result = new ArrayList<Order>();
		
		while(dbCon.rs.next()) {
			Order order = new Order();
			order.setId(dbCon.rs.getInt("id"));
			order.setSeqId(dbCon.rs.getInt("seq_id"));
			order.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			order.setWaiter(dbCon.rs.getString("waiter"));
			order.setDiscounter(dbCon.rs.getString("discount_staff"));
			order.setDiscountDate(dbCon.rs.getTimestamp("discount_date") != null ? dbCon.rs.getTimestamp("discount_date").getTime() : 0);
			
			order.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			order.setStatus(Order.Status.valueOf(dbCon.rs.getInt("status")));
			Table table = new Table(dbCon.rs.getInt("table_id"));
			table.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			if(order.isUnpaid()){
				table.setStatus(Table.Status.IDLE);
			}else{
				table.setStatus(Table.Status.BUSY);
			}
			table.setTableAlias(dbCon.rs.getInt("table_alias"));
			table.setTableName(dbCon.rs.getString("table_name"));
			if(extraCond.dateType == DateType.TODAY){
				table.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
				table.setCategory(Table.Category.valueOf(dbCon.rs.getShort("tbl_category")));
			}
			order.setDestTbl(table);
			order.getRegion().setRegionId(dbCon.rs.getShort("region_id"));
			order.getRegion().setName(dbCon.rs.getString("region_name"));

			order.setCustomNum(dbCon.rs.getShort("custom_num"));
			order.setCategory(Category.valueOf(dbCon.rs.getShort("category")));
			
			if(extraCond.dateType == DateType.TODAY){
				if(dbCon.rs.getInt("discount_id") != 0){
					Discount discount = new Discount(dbCon.rs.getInt("discount_id"));
					discount.setName(dbCon.rs.getString("discount_name"));
					order.setDiscount(discount);
				}
				if(dbCon.rs.getInt("service_plan_id") != 0){
					order.setServicePlan(new ServicePlan(dbCon.rs.getInt("service_plan_id")));
				}
				if(dbCon.rs.getInt("price_plan_id") != 0){
					order.setPricePlan(new PricePlan(dbCon.rs.getInt("price_plan_id")));
				}
				order.setMemberId(dbCon.rs.getInt("member_id"));
			}
			
			PayType payType = new PayType(dbCon.rs.getInt("pay_type_id"));
			payType.setName(dbCon.rs.getString("pay_type_name"));
			order.setPaymentType(payType);
			order.setSettleType(dbCon.rs.getShort("settle_type"));
			order.setStatus(Order.Status.valueOf(dbCon.rs.getInt("status")));
			order.setServiceRate(dbCon.rs.getFloat("service_rate"));
			order.setComment(dbCon.rs.getString("comment"));
			order.setGiftPrice(dbCon.rs.getFloat("gift_price"));
			order.setCancelPrice(dbCon.rs.getFloat("cancel_price"));
			order.setRepaidPrice(dbCon.rs.getFloat("repaid_price"));
			order.setDiscountPrice(dbCon.rs.getFloat("discount_price"));
			order.setErasePrice(dbCon.rs.getInt("erase_price"));
			order.setCouponPrice(dbCon.rs.getFloat("coupon_price"));
			order.setTotalPrice(dbCon.rs.getFloat("total_price"));
			order.setActualPrice(dbCon.rs.getFloat("actual_price"));
			
			result.add(order);
		}

		dbCon.rs.close();
		
		return result;
	}

	/**
	 * Get the pure order according to extra condition {@link ExtraCond} and order clause.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond {@link ExtraCond}
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the pure order
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Order> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Repaid the order according to specific builder {@link Order#RepaidBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to repaid order {@link Order#RepaidBuilder}
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the staff has no privilege for re-payment
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void repaid(Staff staff, Order.RepaidBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			repaid(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(SQLException | BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Repaid the order according to specific builder {@link Order#RepaidBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to repaid order {@link Order#RepaidBuilder}
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the staff has no privilege for re-payment
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void repaid(DBCon dbCon, Staff staff, Order.RepaidBuilder builder) throws BusinessException, SQLException{
		if(staff.getRole().hasPrivilege(Privilege.Code.RE_PAYMENT)){
			UpdateOrder.exec(dbCon, staff, builder.getUpdateBuilder());
			PayOrder.pay(dbCon, staff, builder.getPayBuilder());
		}else{
			throw new BusinessException(StaffError.PAYMENT_NOT_ALLOW);
		}
	}
	
	/**
	 * Transfer the order food according to specific builder {@link Order#TransferBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the transfer builder
	 * @throws BusinessException
	 * 			throws if NOT permitted to transfer food
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void transfer(Staff staff, Order.TransferBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			transfer(dbCon, staff, builder);
			dbCon.conn.commit();
			
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Transfer the order food according to specific builder {@link Order#TransferBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the transfer builder
	 * @throws BusinessException
	 * 			throws if NOT permitted to transfer food
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void transfer(DBCon dbCon, Staff staff, Order.TransferBuilder builder) throws BusinessException, SQLException{
		
		if(builder.getTransferFoods().isEmpty()){
			return;
		}
		
		if(!staff.getRole().hasPrivilege(Privilege.Code.TRANSFER_FOOD)){
			throw new BusinessException(StaffError.TRANSFER_FOOD_NOT_ALLOW);
		}
		
		Order source = OrderDao.getById(dbCon, staff, builder.getSourceOrderId(), DateType.TODAY);
		
		if(source.getDestTbl().getAliasId() == builder.getDestTbl().getAliasId()){
			throw new BusinessException("菜品不能转到相同餐台");
		}
		
		//Assert the food to transfer out does exist and the amount is less than the original.
		for(OrderFood foodOut : builder.getTransferFoods()){
			int index = source.getOrderFoods().indexOf(foodOut);
			if(index < 0){
				throw new BusinessException("菜品【" + FoodDao.getPureById(dbCon, staff, foodOut.getFoodId()).getName() + "】不在账单中");
			}else{
				if(source.getOrderFoods().get(index).getCount() < foodOut.getCount()){
					throw new BusinessException("菜品【" + source.getOrderFoods().get(index).getName() + "】的转菜数量大过已有数量");
				}
			}
		}
		
		Table destTbl = TableDao.getByAlias(dbCon, staff, builder.getDestTbl().getAliasId());
		
		//Transfer out the foods from source order.
		for(OrderFood foodOut : builder.getTransferFoods()){
			OrderFoodDao.fill(dbCon, staff, foodOut);
			foodOut.setCancelReason(CancelReason.newTemporary("【" + staff.getName() + "】从【" + source.getDestTbl().getName() + "】转至【" + destTbl.getName() + "】"));
			OrderFoodDao.insertCancelled(dbCon, staff, new OrderFoodDao.TransferBuilder(source.getId(), foodOut).asCancel());
		}

		//Transfer the foods out to destination table.
		if(destTbl.isIdle()){
			//Insert a new if the destination table is idle.
			InsertOrder.exec(dbCon, staff, new Order.InsertBuilder(new Table.Builder(destTbl.getId())).addAll(builder.getTransferFoods(), staff));
			
		}else if(destTbl.isBusy()){
			//Update the order if the destination table is busy.
			Order destOrder = getByTableId(dbCon, staff, destTbl.getId());
			UpdateOrder.exec(dbCon, staff, new Order.UpdateBuilder(destOrder).addOri(destOrder.getOrderFoods()).addNew(builder.getTransferFoods(), staff));
		}
	}

	/**
	 * Discount the order to specific builder {@link Order#DiscountBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to discount order
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below.
	 * 			<li>the discount is NOT permitted by this staff
	 * 			<li>the order does NOT exist
	 */
	public static void discount(Staff staff, Order.DiscountBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			discount(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Discount the order to specific builder {@link Order#DiscountBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to discount order
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below.
	 * 			<li>the discount is NOT permitted by this staff
	 * 			<li>the order does NOT exist
	 */
	public static void discount(DBCon dbCon, Staff staff, Order.DiscountBuilder builder) throws SQLException, BusinessException{

		final List<Discount> discounts;
		final List<PricePlan> prices;
		if(builder.getMemberId() != 0){
			Member m = MemberDao.getById(dbCon, staff, builder.getMemberId());
			prices = PricePlanDao.getByCond(dbCon, staff, new PricePlanDao.ExtraCond().setId(builder.getPricePlanId()).setMemberType(m.getMemberType()));
			discounts = DiscountDao.getByCond(dbCon, staff, new DiscountDao.ExtraCond().setMemberType(m.getMemberType()).setDiscountId(builder.getDiscountId()), DiscountDao.ShowType.BY_PLAN);
		}else{
			discounts = DiscountDao.getByCond(dbCon, staff, new DiscountDao.ExtraCond().setRole(staff.getRole()).setDiscountId(builder.getDiscountId()), DiscountDao.ShowType.BY_PLAN);
			prices = null;
		}
		
		if(discounts.isEmpty()){
			throw new BusinessException(StaffError.DISCOUNT_NOT_ALLOW);
		}
		
		Order order = OrderDao.getById(dbCon, staff, builder.getOrderId(), DateType.TODAY);
		order.setDiscount(discounts.get(0));
		
		if(prices != null){
			if(prices.isEmpty()){
				throw new BusinessException(StaffError.PRICE_PLAN_NOT_ALLOW);
			}else{
				order.setPricePlan(prices.get(0));
			}
		}
		
		String sql;
		
		//Update the order discount.
		sql = " UPDATE " + Params.dbName + ".order SET id = " + order.getId() +
			  " ,discount_staff_id = " + staff.getId() +
			  " ,discount_staff = '" + staff.getName() + "'" +
  			  " ,discount_date = NOW() " +
			  " ,discount_id = " + order.getDiscount().getId() +
			  (order.hasPricePlan() ? " ,price_plan_id = " + order.getPricePlan().getId() : "") +
			  " ,member_id = " + builder.getMemberId() +
			  " WHERE id = " + order.getId();
		dbCon.stmt.executeUpdate(sql);
		
		//Update each food's discount & unit price.
		for(OrderFood of : order.getOrderFoods()){
			sql = " UPDATE " + Params.dbName + ".order_food " +
				  " SET " +
				  " food_id = " + of.getFoodId() +
				  " ,discount = " + of.getDiscount() + 
				  " ,unit_price = " + of.getFoodPrice() +
				  " WHERE order_id = " + order.getId() + 
				  " AND food_id = " + of.getFoodId();
			dbCon.stmt.executeUpdate(sql);				
		}		
	}
	
	/**
	 * Get the summary to orders to specified restaurant defined in {@link Staff} and other condition.
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
	public static OrderSummary getOrderSummary(Staff staff, ExtraCond extraCond, DateType dateType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getOrderSummary(dbCon, staff, extraCond, dateType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the summary to orders to specified restaurant defined in {@link Staff} and other condition.
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
	public static OrderSummary getOrderSummary(DBCon dbCon, Staff staff, ExtraCond extraCond, DateType dateType) throws SQLException{
		
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
	 * Delete the order according to extra condition
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the order amount to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int amount = deleteByCond(dbCon, staff, extraCond);
			dbCon.conn.commit();
			return amount;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the order according to extra condition
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the order amount to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(Order order : getByCond(dbCon, staff, extraCond, null)){
			String sql;

			//Delete the records to normal taste group. 
			sql = " DELETE FROM " + Params.dbName + ".normal_taste_group" +
			      " WHERE " +
				  " normal_taste_group_id IN (" +
					  " SELECT normal_taste_group_id " +
					  " FROM " + Params.dbName + ".order_food OF " +
					  " JOIN " + Params.dbName + ".taste_group TG ON OF.taste_group_id = TG.taste_group_id " +
					  " WHERE 1 = 1 " + 
					  " AND OF.order_id = " + order.getId() +
					  " AND TG.normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID +
				  " ) ";
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the records to taste group.
			sql = " DELETE FROM " + Params.dbName + ".taste_group " +
			      " WHERE taste_group_id IN (" +
					  " SELECT taste_group_id FROM " + Params.dbName + ".order_food " +
				      " WHERE " + 
					  " order_id = " + order.getId() +
					  " AND " +
					  " taste_group_id <> " + TasteGroup.EMPTY_TASTE_GROUP_ID +
				  " ) ";
			dbCon.stmt.executeUpdate(sql);
			
			//delete the records related to the order id and food id in "order_food" table
			sql = " DELETE FROM `" + Params.dbName + "`.`order_food` WHERE order_id = " + order.getId();
			dbCon.stmt.executeUpdate(sql);
			
			//delete the corresponding order record in "order" table
			sql = " DELETE FROM `" + Params.dbName + "`.`order` WHERE id = " + order.getId();
			dbCon.stmt.executeUpdate(sql);

			//Delete the associated mixed payment.
			MixedPaymentDao.delete(dbCon, staff, new MixedPaymentDao.ExtraCond(DateType.TODAY, order.getId()));
			
			//Delete the temporary table in case of joined or take out
			if(order.getCategory().isJoin() || order.getCategory().isTakeout()){
				try {
					TableDao.deleteById(dbCon, staff, order.getDestTbl().getId());
				} catch (BusinessException ignored) {
					ignored.printStackTrace();
				}
			}
			
			amount++;
		}
		return amount;
	}

	public static class Result{
		private final OrderDao.ArchiveResult orderArchive;
		private final OrderFoodDao.ArchiveResult ofArchive;
		private final TasteGroupDao.ArchiveResult tgArchive;
		private final MixedPaymentDao.ArchiveResult mixedArchive;
		
		Result(OrderDao.ArchiveResult orderArchive, MixedPaymentDao.ArchiveResult mixedArchive, OrderFoodDao.ArchiveResult orderFoodArchive, TasteGroupDao.ArchiveResult tgArchive){
			this.orderArchive = orderArchive;
			this.mixedArchive = mixedArchive;
			this.ofArchive = orderFoodArchive;
			this.tgArchive = tgArchive;
		}
		
		Result(){
			orderArchive = new OrderDao.ArchiveResult(0, 0);
			mixedArchive = new MixedPaymentDao.ArchiveResult(0);
			ofArchive = new OrderFoodDao.ArchiveResult(0, 0);
			tgArchive = new TasteGroupDao.ArchiveResult(0, 0, 0, 0);
		}
		
		public OrderDao.ArchiveResult getOrderArchive(){
			return this.orderArchive;
		}

		public MixedPaymentDao.ArchiveResult getMixedArchive(){
			return this.mixedArchive;
		}
		
		public OrderFoodDao.ArchiveResult getOrderFoodArchive(){
			return this.ofArchive;
		}
		
		public TasteGroupDao.ArchiveResult getTgArchive(){
			return this.tgArchive;
		}
	}
	
	public static Result archive(DBCon dbCon, Staff staff) throws SQLException{
		StringBuilder paidOrderCond = new StringBuilder();
		
		String sql;
		//Get the amount and id to paid orders
		sql = " SELECT id FROM " + Params.dbName + ".order " +
			  " WHERE " +
			  " (status = " + Order.Status.PAID.getVal() + " OR status = " + Order.Status.REPAID.getVal() + ")" +
			 (staff.getRestaurantId() < 0 ? "" : "AND restaurant_id=" + staff.getRestaurantId());
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			paidOrderCond.append(dbCon.rs.getInt("id")).append(",");
		}
		dbCon.rs.close();
		
		if(paidOrderCond.length() > 0){
			paidOrderCond.deleteCharAt(paidOrderCond.length() - 1);

			//Archive the taste group.
			TasteGroupDao.ArchiveResult tgArchive = TasteGroupDao.archive(dbCon, staff, paidOrderCond.toString());
			
			//Archive the order food.
			OrderFoodDao.ArchiveResult ofArchive = OrderFoodDao.archive(dbCon, staff, paidOrderCond.toString());

			//Archive the mixed payment.
			MixedPaymentDao.ArchiveResult mixedArchive = MixedPaymentDao.archive(dbCon, paidOrderCond.toString());

			//Archive the order.
			OrderDao.ArchiveResult orderArchive = OrderDao.archive(dbCon, staff, paidOrderCond.toString());
			
			return new Result(orderArchive, mixedArchive, ofArchive, tgArchive);
			
		}else{
			return new Result();
		}
		
	}
	
	public static class ArchiveResult{
		private final int maxId;
		private final int orderAmount;
		public ArchiveResult(int maxId, int orderAmount){
			this.maxId = maxId;
			this.orderAmount = orderAmount;
		}
		
		public int getMaxId(){
			return this.maxId;
		}
		
		public int getAmount(){
			return this.orderAmount;
		}
		
		@Override
		public String toString(){
			return orderAmount + " record(s) are moved to history, maxium id : " + maxId;
		}
	}
	
	private static ArchiveResult archive(DBCon dbCon, Staff staff, String paidOrder) throws SQLException{
		final String orderItem = "`id`, `seq_id`, `restaurant_id`, `birth_date`, `order_date`, `status`, " +
				"`discount_staff`, `discount_staff_id`, `discount_date`," +
				"`cancel_price`, `discount_price`, `gift_price`, `coupon_price`, `repaid_price`, `erase_price`, `total_price`, `actual_price`, `custom_num`," + 
				"`waiter`, `settle_type`, `pay_type_id`, `category`, `staff_id`, " +
				"`region_id`, `region_name`, `table_id`, `table_alias`, `table_name`, `service_rate`, `comment`";
		
		String sql;
		//Move the paid order from "order" to "order_history".
		sql = " INSERT INTO " + Params.dbName + ".order_history (" + orderItem + ") " + 
			  " SELECT " + orderItem + " FROM " + Params.dbName + ".order WHERE id IN " + "(" + paidOrder + ")";
		int amount = dbCon.stmt.executeUpdate(sql);
		
		//Calculate the max order id from both today and history.
		int maxId = 0;
		sql = " SELECT MAX(id) + 1 FROM (" + 
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order " +
			  " UNION " +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order_history) AS all_order";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			maxId = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		sql = " DELETE FROM " + Params.dbName + ".order WHERE restaurant_id = " + Restaurant.ADMIN;
		dbCon.stmt.executeUpdate(sql);
		
		sql = " INSERT INTO " + Params.dbName + ".order " +
			  " ( id, restaurant_id ) VALUES " +
			 "(" + maxId + "," + Restaurant.ADMIN + ")";
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the paid order from "order" table.
		sql = " DELETE FROM " + Params.dbName + ".order WHERE id IN ( " + paidOrder + ")";
		dbCon.stmt.executeUpdate(sql);
		
		return new ArchiveResult(maxId, amount);
	}
	
}
