package com.wireless.db.orderMgr;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.DBTbl;
import com.wireless.db.Params;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.serviceRate.ServicePlanDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.exception.FrontBusinessError;
import com.wireless.exception.StaffError;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.Order.Category;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.OrderSummary;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.dishesOrder.TasteGroup;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.serviceRate.ServicePlan;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.sccon.ServerConnector;

public class OrderDao {

	public static class ExtraCond{
		public final String orderTblAlias;
		private final DBTbl dbTbl;
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
			this.dbTbl = new DBTbl(dateType); 
			if(dateType == DateType.TODAY){
				orderTblAlias = "O";
			}else{
				orderTblAlias = "OH";
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
				final String sql = " SELECT order_id FROM " + Params.dbName + "." + dbTbl.orderFoodTbl + " WHERE operation = " + OrderFood.Operation.TRANSFER.getVal();;
				filterCond.append(" AND " + orderTblAlias + ".id IN( " + sql + ")");
			}
			return filterCond.toString();
		}
	}
	
	/**
	 * Get the status to a specific order.
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
	public static Order.Status getStatusById(Staff staff, int orderId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStatusById(dbCon, staff, orderId);
		}finally{
			dbCon.disconnect();
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
		//Get the detail to discount.
		if(order.hasDiscount()){
			order.setDiscount(DiscountDao.getById(dbCon, staff, order.getDiscount().getId()));
		}
		//Get the detail to service plan.
		if(order.hasServicePlan()){
			order.setServicePlan(ServicePlanDao.getById(dbCon, staff, order.getServicePlan().getPlanId(), ServicePlanDao.ShowType.BY_PLAN));
		}
		//Get the detail to price plan
		if(order.hasPricePlan()){
			order.setPricePlan(PricePlanDao.getById(dbCon, staff, order.getPricePlan().getId()));
		}
		//Get the detail to coupon.
		if(order.hasCoupon()){
			order.setCoupon(CouponDao.getById(dbCon, staff, order.getCoupon().getId()));
		}
		//Get the detail to mixed payment.
		if(order.getPaymentType().isMixed()){
			order.setMixedPayment(MixedPaymentDao.getByCond(dbCon, staff, new MixedPaymentDao.ExtraCond(dateType, order)));
		}
		//Get the detail to associated wx orders.
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
				  " O.id, O.birth_date, O.order_date, O.seq_id, O.custom_num, O.table_id, O.table_alias, O.table_name, " +
				  " O.temp_staff, O.temp_date, " +
				  " T.minimum_cost, IFNULL(T.category, 1) AS tbl_category, " +
				  " O.waiter, O.staff_id, " +
				  " O.region_id, O.region_name, O.restaurant_id, " +
				  " O.settle_type, O.pay_type_id, IFNULL(PT.name, '其他') AS pay_type_name, O.category, O.status, O.service_plan_id, O.service_rate, O.comment, " +
				  " O.discount_id, O.discount_staff_id, O.discount_staff, O.discount_date, " +
				  " O.coupon_id, " +
				  " O.price_plan_id, O.member_id, " +
				  " O.gift_price, O.cancel_price, O.discount_price, O.repaid_price, O.erase_price, O.coupon_price, O.pure_price, O.total_price, O.actual_price " +
				  " FROM " + 
				  Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
				  " LEFT JOIN " + Params.dbName + ".table T ON O.table_id = T.table_id " +
				  " LEFT JOIN " + Params.dbName + ".pay_type PT ON O.pay_type_id = PT.pay_type_id " +
				  " WHERE 1 = 1 " + 
				  " AND O.restaurant_id = " + staff.getRestaurantId() + " " +
				  (extraCond != null ? extraCond.toString() : "") + " " +
				  (orderClause != null ? orderClause : "");
			
		}else if(extraCond.dateType == DateType.HISTORY){
			sql = " SELECT " +
				  " OH.id, OH.birth_date, OH.order_date, OH.seq_id, OH.custom_num, OH.table_id, OH.table_alias, OH.table_name, " +
				  " OH.waiter, OH.staff_id, OH.discount_staff_id, OH.discount_staff, OH.discount_date, " +
				  " OH.region_id, OH.region_name, OH.restaurant_id, " +
				  " OH.coupon_price, " +
				  " OH.settle_type, OH.pay_type_id, IFNULL(PT.name, '其他') AS pay_type_name, OH.category, OH.status, OH.service_rate, OH.comment, " +
				  " OH.gift_price, OH.cancel_price, OH.discount_price, OH.repaid_price, OH.erase_price, OH.pure_price, OH.total_price, OH.actual_price " +
				  " FROM " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " OH " + 
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

		final List<Order> result = new ArrayList<Order>();
		
		while(dbCon.rs.next()) {
			Order order = new Order();
			order.setId(dbCon.rs.getInt("id"));
			order.setSeqId(dbCon.rs.getInt("seq_id"));
			order.setBirthDate(dbCon.rs.getTimestamp("birth_date").getTime());
			order.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			order.setWaiter(dbCon.rs.getString("waiter"));
			order.setStaffId(dbCon.rs.getInt("staff_id"));
			if(dbCon.rs.getTimestamp("discount_date") != null){
				order.setDiscounterId(dbCon.rs.getInt("discount_staff_id"));
				order.setDiscounter(dbCon.rs.getString("discount_staff"));
				order.setDiscountDate(dbCon.rs.getTimestamp("discount_date").getTime());
			}
			
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
				if(dbCon.rs.getTimestamp("temp_date") != null){
					order.setTempDate(dbCon.rs.getTimestamp("temp_date").getTime());
					order.setTempStaff(dbCon.rs.getString("temp_staff"));
				}
				if(dbCon.rs.getInt("discount_id") != 0){
					order.setDiscount(new Discount(dbCon.rs.getInt("discount_id")));
				}
				if(dbCon.rs.getInt("service_plan_id") != 0){
					order.setServicePlan(new ServicePlan(dbCon.rs.getInt("service_plan_id")));
				}
				if(dbCon.rs.getInt("price_plan_id") != 0){
					order.setPricePlan(new PricePlan(dbCon.rs.getInt("price_plan_id")));
				}
				if(dbCon.rs.getInt("coupon_id") != 0){
					order.setCoupon(new Coupon(dbCon.rs.getInt("coupon_id")));
				}
				order.setMemberId(dbCon.rs.getInt("member_id"));
			}
			
			PayType payType = new PayType(dbCon.rs.getInt("pay_type_id"));
			payType.setName(dbCon.rs.getString("pay_type_name"));
			order.setPaymentType(payType);
			order.setSettleType(Order.SettleType.valueOf(dbCon.rs.getShort("settle_type")));
			order.setStatus(Order.Status.valueOf(dbCon.rs.getInt("status")));
			order.setServiceRate(dbCon.rs.getFloat("service_rate"));
			order.setComment(dbCon.rs.getString("comment"));
			order.setGiftPrice(dbCon.rs.getFloat("gift_price"));
			order.setCancelPrice(dbCon.rs.getFloat("cancel_price"));
			order.setRepaidPrice(dbCon.rs.getFloat("repaid_price"));
			order.setDiscountPrice(dbCon.rs.getFloat("discount_price"));
			order.setErasePrice(dbCon.rs.getInt("erase_price"));
			order.setCouponPrice(dbCon.rs.getFloat("coupon_price"));
			order.setPurePrice(dbCon.rs.getFloat("pure_price"));
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
	 * Perform the service rate specific order.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the service builder {@link Order#ServiceBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the order does NOT exist
	 */
	public static void service(Staff staff, Order.ServiceBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			service(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform the service rate specific order.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the service builder {@link Order#ServiceBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the order does NOT exist
	 */
	public static void service(DBCon dbCon, Staff staff, Order.ServiceBuilder builder) throws SQLException, BusinessException{
		Order order = getById(dbCon, staff, builder.getOrderId(), DateType.TODAY);
		ServicePlan sp = ServicePlanDao.getByRegion(dbCon, staff, builder.getServicePlanId(), order.getRegion());
		String sql;
		sql = " UPDATE " + Params.dbName + ".order SET " +
			  " service_plan_id = " + sp.getPlanId() +
			  " ,service_rate = " + (sp.hasRates() ? sp.getRates().get(0).getRate() : 0) +
			  " WHERE id = " + order.getId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(FrontBusinessError.ORDER_NOT_EXIST);
		}
	}
	
	/**
	 * Comment the order to specific builder {@link Order#CommentBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the comment builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the order to comment does NOT exist
	 */
	public static void comment(Staff staff, Order.CommentBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			comment(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Comment the order to specific builder {@link Order#CommentBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the comment builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the order to comment does NOT exist
	 */
	public static void comment(DBCon dbCon, Staff staff, Order.CommentBuilder builder) throws SQLException, BusinessException{
		String sql;
		sql = " UPDATE " + Params.dbName + ".order SET comment = '" + builder.getComment() + "' WHERE id = " + builder.getOrderId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(FrontBusinessError.ORDER_NOT_EXIST);
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
			//Restore the account if perform member consumption before.
			Order oriOrder = OrderDao.getById(staff, builder.getPayBuilder().getOrderId(), DateType.TODAY);
			if(oriOrder.getMemberId() != 0){
				MemberDao.restore(dbCon, staff, oriOrder.getMemberId(), oriOrder);
			}
			if(builder.hasDiscountBuilder()){
				discount(dbCon, staff, builder.getDiscountBuilder());
			}
			if(builder.hasServiceBuilder()){
				service(dbCon, staff, builder.getServiceBuilder());
			}
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

		final Table destTbl; 
		
		if(builder.getDestTbl().getId() != 0){
			destTbl = TableDao.getById(dbCon, staff, builder.getDestTbl().getId());
		}else{
			destTbl = TableDao.getByAlias(dbCon, staff, builder.getDestTbl().getAliasId());
		}
		
		if(source.getDestTbl().equals(destTbl)){
			throw new BusinessException("菜品不能转到相同餐台");
		}

		//Assert the food to transfer out does exist and the amount is less than the original.
		for(OrderFood foodOut : builder.getTransferFoods()){
			int index = source.getOrderFoods().indexOf(foodOut);
			if(index < 0){
				throw new BusinessException("菜品【" + FoodDao.getPureById(dbCon, staff, foodOut.getFoodId()).getName() + "】不在账单中");
			}else{
				final OrderFood foodToOut = source.getOrderFoods().get(index);
				if(foodToOut.getCount() < foodOut.getCount()){
					throw new BusinessException("菜品【" + source.getOrderFoods().get(index).getName() + "】的转菜数量大过已有数量");
				}else{
					foodToOut.setCancelReason(CancelReason.newTemporary("【" + staff.getName() + "】从【" + source.getDestTbl().getName() + "】转至【" + destTbl.getName() + "】"));
					foodToOut.setOperation(OrderFood.Operation.TRANSFER);
					foodToOut.removeCount(foodOut.getCount(), staff);
				}
			}
		}
		//Transfer out the foods from source order.
		UpdateOrder.exec(dbCon, staff, new Order.UpdateBuilder(source).addOri(source.getOrderFoods()));

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
	 * Gifted the food according to specific builder {@link Order#GiftBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the gift builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the order does NOT exist
	 * 			<li>the amount to gift exceed the one in the order
	 */
	public static void gift(Staff staff, Order.GiftBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			gift(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(SQLException | BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Gifted the food according to specific builder {@link Order#GiftBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the gift builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the order does NOT exist
	 * 			<li>the order food can NOT be gifted
	 * 			<li>the amount to gift exceed the one in the order
	 */
	public static void gift(DBCon dbCon, Staff staff, Order.GiftBuilder builder) throws SQLException, BusinessException{
		
		if(!staff.getRole().hasPrivilege(Privilege.Code.GIFT)){
			throw new BusinessException(StaffError.GIFT_NOT_ALLOW);
		}
		
		OrderFood giftedFood = builder.getGiftedFood();
		OrderFoodDao.fill(dbCon, staff, giftedFood);
		
		if(!giftedFood.asFood().isGift()){
			throw new BusinessException("菜品【" + giftedFood.getName() + "】不是可赠送菜品");
		}
		
		Order order = getById(dbCon, staff, builder.getOrderId(), DateType.TODAY);
		
		int index = order.getOrderFoods().indexOf(giftedFood);
		if(index < 0){
			throw new BusinessException("菜品【" + giftedFood.getName() + "】不在账单中");
		}else{
			OrderFood foodToGifted = order.getOrderFoods().get(index);
			if(foodToGifted.getCount() < giftedFood.getCount()){
				throw new BusinessException("菜品【" + foodToGifted.getName() + "】的赠送数量大过已有数量");
			}else{
				//Cancel the order food as gift operation.
				foodToGifted.setCancelReason(CancelReason.newTemporary("【" + staff.getName() + "】赠送【" + giftedFood.asFood().getName() + "】"));
				foodToGifted.removeCount(builder.getGiftedFood().getCount(), staff);
				foodToGifted.setOperation(OrderFood.Operation.GIFT);
				UpdateOrder.exec(dbCon, staff, new Order.UpdateBuilder(order).addOri(order.getOrderFoods()));
			}
		}
		
		//Gift the order food.
		order = getById(dbCon, staff, builder.getOrderId(), DateType.TODAY);
		giftedFood.setGift(true);
		UpdateOrder.exec(dbCon, staff, new Order.UpdateBuilder(order).addOri(order.getOrderFoods()).addNew(giftedFood, staff));
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
	 * 			<li>the coupon does NOT exist
	 */
	public static void discount(DBCon dbCon, Staff staff, Order.DiscountBuilder builder) throws SQLException, BusinessException{

		final Discount discount;
		final List<PricePlan> prices;
		if(builder.hasMember()){
			Member m = MemberDao.getById(dbCon, staff, builder.getMemberId());
			if(builder.hasPricePlan()){
				prices = PricePlanDao.getByCond(dbCon, staff, new PricePlanDao.ExtraCond().setId(builder.getPricePlanId()).setMemberType(m.getMemberType()));
			}else{
				prices = null;
			}
			if(builder.hasDiscountId()){
				List<Discount> discounts = DiscountDao.getByCond(dbCon, staff, new DiscountDao.ExtraCond().setMemberType(m.getMemberType()).setDiscountId(builder.getDiscountId()), DiscountDao.ShowType.BY_PLAN);
				if(discounts.isEmpty()){
					throw new BusinessException(StaffError.DISCOUNT_NOT_ALLOW);
				}else{
					discount = discounts.get(0);
				}
			}else{
				discount = m.getMemberType().getDefaultDiscount();
			}
		}else{
			List<Discount> discounts = DiscountDao.getByCond(dbCon, staff, new DiscountDao.ExtraCond().setRole(staff.getRole()).setDiscountId(builder.getDiscountId()), DiscountDao.ShowType.BY_PLAN);
			if(discounts.isEmpty()){
				throw new BusinessException(StaffError.DISCOUNT_NOT_ALLOW);
			}else{
				discount = discounts.get(0);
			}
			prices = null;
		}
		
		final Order order = OrderDao.getById(dbCon, staff, builder.getOrderId(), DateType.TODAY);
		order.setDiscount(discount);
		
		if(prices != null){
			if(prices.isEmpty()){
				throw new BusinessException(StaffError.PRICE_PLAN_NOT_ALLOW);
			}else{
				order.setPricePlan(prices.get(0));
			}
		}
		
		//Set the coupon if exist.
		if(builder.hasCoupon()){
			order.setCoupon(CouponDao.getById(dbCon, staff, builder.getCouponId()));
		}else{
			order.setCoupon(null);
		}
		
		String sql;
		
		//Update the order discount.
		sql = " UPDATE " + Params.dbName + ".order SET id = " + order.getId() +
			  " ,discount_staff_id = " + staff.getId() +
			  " ,discount_staff = '" + staff.getName() + "'" +
  			  " ,discount_date = NOW() " +
			  " ,discount_id = " + order.getDiscount().getId() +
			  " ,price_plan_id = " + (builder.hasPricePlan() ? order.getPricePlan().getId() : " NULL ") +
			  " ,coupon_id = " + (order.hasCoupon() ? order.getCoupon().getId() : " NULL ") +
			  " ,coupon_price = " + (order.hasCoupon() ? order.getCoupon().getPrice() : "0") +
			  " ,member_id = " + builder.getMemberId() +
			  " WHERE id = " + order.getId();
		dbCon.stmt.executeUpdate(sql);
		
		//Update each food's discount & unit price.
		for(OrderFood of : order.getOrderFoods()){
			sql = " UPDATE " + Params.dbName + ".order_food " +
				  " SET food_id = " + of.getFoodId() +
				  " ,discount = " + of.getDiscount() + 
				  " ,unit_price = " + of.asFood().getPrice() +
				  " WHERE order_id = " + order.getId() + 
				  " AND food_id = " + of.getFoodId() +
				  (of.hasFoodUnit() ? " AND food_unit_id = " + of.getFoodUnit().getId() : "");
			dbCon.stmt.executeUpdate(sql);				
		}		
	}
	
	/**
	 * Perform this feast order according to specific builder {@link Order#FeastBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to feast
	 * @return the id to feast order
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement 
	 * @throws BusinessException
	 */
	public static int feast(Staff staff, Order.FeastBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int orderId = feast(dbCon, staff, builder);
			dbCon.conn.commit();
			return orderId;
		}catch(SQLException | BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform this feast order according to specific builder {@link Order#FeastBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to feast
	 * @return the id to feast order
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement 
	 * @throws BusinessException
	 */
	public static int feast(DBCon dbCon, Staff staff, Order.FeastBuilder builder) throws SQLException, BusinessException{
		Order.InsertBuilder insertBuilder = Order.InsertBuilder.newInstance4Feast();
		for(Map.Entry<Department, Float> entry : builder.getIncomeByDept()){
			Kitchen feastKitchen = KitchenDao.getByCond(dbCon, staff, new KitchenDao.ExtraCond().setDeptId(entry.getKey().getId()).setType(Kitchen.Type.FEAST), null).get(0);
			OrderFood of = new OrderFood(0);
			of.setTemp(true);
			of.setCount(1);
			of.asFood().setName(feastKitchen.getName());
			of.asFood().setKitchen(feastKitchen);
			of.asFood().setPrice(entry.getValue());
			insertBuilder.add(of, staff);
		}
		int orderId = InsertOrder.exec(dbCon, staff, insertBuilder).getId();
		
		//return PayOrder.pay(dbCon, staff, Order.PayBuilder.build4Normal(orderId)).getId();
		return orderId;
	}
	
	/**
	 * Cancel the order to specific table.
	 * @param staff
	 * 			the staff to perform this action
	 * @param tblBuilder
	 * 			the order to this table
	 * @return the id to order cancelled 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the order to this table does NOT exist
	 * 			<li>the order has performed any action before
	 */
	public static int cancel(Staff staff, Table.Builder tblBuilder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int orderId = cancel(dbCon, staff, tblBuilder);
			dbCon.conn.commit();
			return orderId;
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Cancel the order to specific table.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param tblBuilder
	 * 			the order to this table
	 * @return the id to order cancelled 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the order to this table does NOT exist
	 * 			<li>the order has performed any action before
	 */
	public static int cancel(DBCon dbCon, Staff staff, Table.Builder tblBuilder) throws SQLException, BusinessException{
		Order order = OrderDao.getByTableId(dbCon, staff, tblBuilder.build().getId());
		if(OrderFoodDao.getSingleDetail(dbCon, staff, new OrderFoodDao.ExtraCond(DateType.TODAY).setOrder(order), null).isEmpty()){
			deleteByCond(dbCon, staff, new ExtraCond(DateType.TODAY).setOrderId(order.getId()));
			return order.getId();
		}else{
			throw new BusinessException("【" + order.getDestTbl().getName() + "】的账单已有操作，不能撤台", FrontBusinessError.ORDER_CANCEL_FAIL);
		}
	}
	
	/**
	 * Perform the order multi insert action to specific builder {@link Order#InsertMultiBuilder}. 
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the multi insert builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>any table is busy
	 * 			<li>lack of order
	 */
	public static void insertMulti(Staff staff, Order.InsertMultiBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			insertMulti(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform the order multi insert action to specific builder {@link Order#InsertMultiBuilder}. 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the multi insert builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>any table is busy
	 * 			<li>lack of order
	 */
	public static void insertMulti(DBCon dbCon, Staff staff, Order.InsertMultiBuilder builder) throws SQLException, BusinessException{
		for(Order.InsertBuilder orderBuilder : builder.getBuilders()){
			Table tbl = TableDao.getById(dbCon, staff, orderBuilder.build().getDestTbl().getId());
			if(tbl.isBusy()){
				throw new BusinessException("餐台【" + tbl.getName() + "】是就餐状态，不能入席", FrontBusinessError.INSERT_MULTI_FAIL);
			}
		}
		
		final int nOrder = builder.getBuilders().size();
		if(nOrder == 0){
			throw new BusinessException("缺少账单信息，不能多台入席");
		}
		for(Order.InsertBuilder orderBuilder : builder.getBuilders()){
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, orderBuilder.setForce(true), PrintOption.DO_PRINT));
				if(resp.header.type == Type.NAK){
					throw new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
				}
			}catch(IOException e){
				throw new BusinessException(e.getMessage());
			}
		}
	}
	
	/**
	 * Merge multi orders to the single.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the merge builder {@link Order.MergeBuilder}
	 * @return the id to destination table
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the order to destination table does NOT exist
	 * 			<li>any order to merged table does NOT exist
	 */
	public static int merge(Staff staff, Order.MergeBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int tableId = merge(dbCon, staff, builder);
			dbCon.conn.commit();
			return tableId;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Merge multi orders to the single.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the merge builder {@link Order.MergeBuilder}
	 * @return the id to destination table
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the order to destination table does NOT exist
	 * 			<li>any order to merged table does NOT exist
	 */
	public static int merge(DBCon dbCon, Staff staff, Order.MergeBuilder builder) throws SQLException, BusinessException{
		Order dest = getByTableId(dbCon, staff, builder.getDest().getId());
		List<Order> mergeOrders = new ArrayList<Order>();
		for(Table tlbToMerge : builder.getBuilders()){
			mergeOrders.add(getByTableId(dbCon, staff, tlbToMerge.getId()));
		}
		
		for(Order eachMergeOrder : mergeOrders){
			String sql;
			sql = " UPDATE " + Params.dbName + ".order_food SET " +
				  " order_id = " + dest.getId() +
				  " WHERE order_id = " + eachMergeOrder.getId();
			dbCon.stmt.executeUpdate(sql);
			
			deleteByCond(dbCon, staff, new ExtraCond(DateType.TODAY).setOrderId(eachMergeOrder.getId()));
		}
		
		return dest.getDestTbl().getId();
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

			DBTbl dbTbl = new DBTbl(extraCond.dateType);
			//Delete the records to normal taste group. 
			sql = " DELETE NTG FROM " + Params.dbName + "." + dbTbl.ntgTbl + " NTG " +
				  " JOIN " + Params.dbName + "." + dbTbl.tgTbl + " TG ON NTG.normal_taste_group_id = TG.normal_taste_group_id " +
				  " JOIN " + Params.dbName + "." + extraCond.dbTbl.orderFoodTbl + " OF ON OF.taste_group_id = TG.taste_group_id " +
				  " WHERE 1 = 1 " + 
				  " AND OF.order_id = " + order.getId() +
				  " AND TG.normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID;
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the records to taste group.
			sql = " DELETE TG FROM " + Params.dbName + "." + dbTbl.tgTbl + " TG " +
				  " JOIN " + Params.dbName + "." + extraCond.dbTbl.orderFoodTbl + " OF ON TG.taste_group_id = OF.taste_group_id " +
			      " WHERE 1 = 1 " + 
				  " AND OF.order_id = " + order.getId() +
				  " AND TG.taste_group_id <> " + TasteGroup.EMPTY_TASTE_GROUP_ID +
			dbCon.stmt.executeUpdate(sql);
			
			//delete the records related to the order id and food id in "order_food" table
			sql = " DELETE FROM " + Params.dbName + "." + extraCond.dbTbl.orderFoodTbl + " WHERE order_id = " + order.getId();
			dbCon.stmt.executeUpdate(sql);
			
			//delete the corresponding order record in "order" table
			sql = " DELETE FROM " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " WHERE id = " + order.getId();
			dbCon.stmt.executeUpdate(sql);

			//Delete the associated mixed payment.
			MixedPaymentDao.deleteByCond(dbCon, staff, new MixedPaymentDao.ExtraCond(extraCond.dateType, order.getId()));
			
			//Delete the temporary table
			if(extraCond.dateType.isToday() && !order.getDestTbl().getCategory().isNormal()){
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

	public static class ArchiveResult{
		private final DBTbl fromTbl;
		private final DBTbl toTbl;
		public final int orderAmount;
		public final int ofAmount;
		public final int tgAmount;
		public final int mixedAmount;
		
		public ArchiveResult(DateType archiveFrom, DateType archiveTo, int orderAmount, int ofAmount, int tgAmount, int mixedAmount){
			this.fromTbl = new DBTbl(archiveFrom);
			this.toTbl = new DBTbl(archiveTo);
			this.orderAmount = orderAmount;
			this.ofAmount = ofAmount;
			this.tgAmount = tgAmount;
			this.mixedAmount = mixedAmount;
		}
		
		@Override
		public String toString(){
			final String sep = System.getProperty("line.separator");
			return orderAmount + " record(s) are moved from '" + fromTbl.orderTbl + "'to '" + toTbl.orderTbl + "'" + sep +
				   ofAmount + " record(s) are moved from '" + fromTbl.orderFoodTbl + "' to '" + toTbl.orderFoodTbl + "'" + sep +
				   tgAmount + " record(s) are moved from '" + fromTbl.tgTbl + "' to '" + toTbl.tgTbl + "'" + sep +
				   mixedAmount + " record(s) are moved from '" + fromTbl.mixedTbl + "' to '" + toTbl.mixedTbl + "'";
		}
	}
	
	/**
	 * Archive the daily record from today to history.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the archive result
	 * @throws SQLException
	 * 			throws if failed to execute SQL statement
	 */
	public static ArchiveResult archive4Daily(DBCon dbCon, Staff staff) throws SQLException{
		String sql;
		DBTbl fromTbl = new DBTbl(DateType.TODAY);
		DBTbl toTbl = new DBTbl(DateType.HISTORY);
		
		//Calculate the max order id from both today and history.
		int maxId = 0;
		sql = " SELECT MAX(id) + 1 FROM (" + 
			  " SELECT MAX(id) AS id FROM " + Params.dbName + "." + fromTbl.orderTbl +
			  " UNION " +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + "." + toTbl.orderTbl + ") AS all_order";
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
		
		return archive(dbCon, staff, new ExtraCond(DateType.TODAY).addStatus(Order.Status.PAID).addStatus(Order.Status.REPAID), DateType.TODAY, DateType.HISTORY);
		
	}
	
	/**
	 * Archive the expired records from history to archive.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the archive result {@link ArchiveResult}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static ArchiveResult archive4Expired(DBCon dbCon, final Staff staff) throws SQLException{
		ExtraCond extraCond4Expired = new ExtraCond(DateType.HISTORY){
			@Override
			public String toString(){
				String sql;
				sql = " SELECT OH.id FROM " + Params.dbName + ".order_history OH " +
					  " JOIN " + Params.dbName + ".restaurant REST ON REST.id = OH.restaurant_id " +
					  " WHERE 1 = 1 " +
					  " AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(OH.order_date) > REST.record_alive " +
					  " AND REST.id = " + staff.getRestaurantId();
				return " AND " + orderTblAlias + ".id IN ( " + sql + ")";
			}
		};
		
		return archive(dbCon, staff, extraCond4Expired, DateType.HISTORY, DateType.ARCHIVE);
	}
	
	private static ArchiveResult archive(DBCon dbCon, Staff staff, ExtraCond extraCond, DateType archiveFrom, DateType archiveTo) throws SQLException{
		DBTbl toTbl = new DBTbl(archiveTo);
		
		String sql;
		
		int amount = 0;
		int ofAmount = 0;
		int tgAmount = 0;
		int mixedAmount = 0;
		for(Order order : getByCond(dbCon, staff, extraCond, null)){
			sql = " INSERT INTO " + Params.dbName + "." + toTbl.orderTbl + "(" +
				  "`id`, `seq_id`, `restaurant_id`, `birth_date`, `order_date`, `status`, " +
				  "`discount_staff`, `discount_staff_id`, `discount_date`," +
				  "`cancel_price`, `discount_price`, `gift_price`, `coupon_price`, `repaid_price`, `erase_price`, `pure_price`, `total_price`, `actual_price`, `custom_num`," + 
				  "`waiter`, `settle_type`, `pay_type_id`, `category`, `staff_id`, " +
				  "`region_id`, `region_name`, `table_id`, `table_alias`, `table_name`, `service_rate`, `comment`) VALUES ( " +
				  order.getId() + "," +
				  order.getSeqId() + "," +
				  order.getRestaurantId() + "," +
				  "'" + DateUtil.format(order.getBirthDate(), DateUtil.Pattern.DATE_TIME) + "'," +
				  "'" + DateUtil.format(order.getOrderDate(), DateUtil.Pattern.DATE_TIME) + "'," +
				  order.getStatus().getVal() + "," +
				  (order.getDiscounterId() != 0 ? "'" + order.getDiscounter() + "'" : "NULL") + "," +
				  (order.getDiscounterId() != 0 ? order.getDiscounterId() : "NULL") + "," +
				  (order.getDiscountDate() != 0 ? "'" + DateUtil.format(order.getDiscountDate(), DateUtil.Pattern.DATE_TIME) + "'" : "NULL") + "," +
				  order.getCancelPrice() + "," +
				  order.getDiscountPrice() + "," +
				  order.getGiftPrice() + "," +
				  order.getCouponPrice() + "," +
				  order.getRepaidPrice() + "," +
				  order.getErasePrice() + "," +
				  order.getPurePrice() + "," +
				  order.getTotalPrice() + "," +
				  order.getActualPrice() + "," +
				  order.getCustomNum() + "," +
				  "'" + order.getWaiter() + "'," +
				  order.getSettleType().getVal() + "," +
				  order.getPaymentType().getId() + "," + 
				  order.getCategory().getVal() + "," +
				  order.getStaffId() + "," +
				  order.getRegion().getId() + "," +
				  "'" + order.getRegion().getName() + "'," +
				  order.getDestTbl().getId() + "," +
				  order.getDestTbl().getAliasId() + "," +
				  "'" + order.getDestTbl().getName() + "'," +
				  order.getServiceRate() + "," +
				  "'" + order.getComment() + "'" +
 				  ")";
			
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
			
			//Archive the order food records.
			OrderFoodDao.ArchiveResult ofResult = OrderFoodDao.archive(dbCon, staff, order, archiveFrom, archiveTo);
			ofAmount += ofResult.ofAmount;
			tgAmount += ofResult.tgAmount;
			
			//Archive the mixed payment records.
			mixedAmount += MixedPaymentDao.archive(dbCon, staff, order, archiveFrom, archiveTo).amount;
			
			//Delete the order and associated records. 
			deleteByCond(dbCon, staff, new ExtraCond(archiveFrom).setOrderId(order.getId()));
		}
		
		return new ArchiveResult(archiveFrom, archiveTo, amount, ofAmount, tgAmount, mixedAmount);
	}
	
}
