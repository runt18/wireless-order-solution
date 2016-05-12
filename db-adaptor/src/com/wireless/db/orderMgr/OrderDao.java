package com.wireless.db.orderMgr;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.DBTbl;
import com.wireless.db.Params;
import com.wireless.db.billStatistics.DutyRangeDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.promotion.CouponOperationDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.serviceRate.ServicePlanDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.exception.FrontBusinessError;
import com.wireless.exception.PromotionError;
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
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.dishesOrder.TasteGroup;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponOperation;
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

	public static class ExtraCond implements Cloneable{
		public final String orderTblAlias;
		private final DBTbl dbTbl;
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
		private int restaurantId;
		private boolean isOnlyAmount;	//只获取数量
		
		private boolean isRepaid;		//是否有反结帐
		private boolean isDiscount;		//是否有折扣
		private boolean isGift;			//是否有赠送
		private boolean isCancelled;	//是否有退菜
		private boolean isErased;		//是否有抹数
		private boolean isCoupon;		//是否有使用优惠券
		private boolean isTransfer;		//是否有转菜
		private boolean isMemberPrice;	//是否有会员价
		
		private boolean isChain;		//是否连锁
		private boolean calcByDuty;		//是否按日结区间计算
		
		public ExtraCond(DateType dateType){
			this.dbTbl = new DBTbl(dateType); 
			if(dateType == DateType.TODAY){
				orderTblAlias = "O";
			}else{
				orderTblAlias = "OH";
			}
		}
		
		public ExtraCond setCalcByDuty(boolean onOff){
			this.calcByDuty = onOff;
			return this;
		}
		
		public ExtraCond setChain(boolean onOff){
			this.isChain = onOff;
			return this;
		}
		
		public ExtraCond setOnlyAmount(boolean onOff){
			this.isOnlyAmount = onOff;
			return this;
		}
		
		public ExtraCond setRestaurant(int restaurantId){
			this.restaurantId = restaurantId;
			return this;
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
		
		public ExtraCond isMemberPrice(boolean onOff){
			this.isMemberPrice = onOff;
			return this;
		}
		
		@Override
	    public Object clone() {   
	        try {   
	            return super.clone();   
	        } catch (CloneNotSupportedException e) {   
	            return null;   
	        }   
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
				DutyRange range;
				if(this.calcByDuty){
					try {
						range = DutyRangeDao.exec(StaffDao.getAdminByRestaurant(restaurantId), orderRange);
					} catch (SQLException | BusinessException e) {
						e.printStackTrace();
						range = orderRange;
					}
				}else{
					range = orderRange;
				}
				
				if(range != null){
					filterCond.append(" AND " + orderTblAlias + ".order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'");
				}else{
					filterCond.append(" AND 0 ");
				}
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
				String sql;
				sql = " SELECT O.id " + 
					  " FROM " + Params.dbName + "." + dbTbl.orderTbl + " O " +
					  " JOIN " + Params.dbName + ".coupon_operation CO ON CO.associate_id = O.id AND O.restaurant_id = " + restaurantId;
				filterCond.append(" AND " + orderTblAlias + ".id IN(" + sql + ")");
			}
			if(isTransfer){
				final String sql = " SELECT order_id FROM " + Params.dbName + "." + dbTbl.orderFoodTbl + " WHERE operation = " + OrderFood.Operation.TRANSFER.getVal();;
				filterCond.append(" AND " + orderTblAlias + ".id IN( " + sql + ")");
			}
			if(isMemberPrice){
				final String sql = " SELECT OH.id FROM " + Params.dbName + "." + dbTbl.orderTbl + " OH " +
								   " JOIN " + Params.dbName + "." + dbTbl.moTbl + " MOH ON OH.id = MOH.order_id AND MOH.operate_type = " + MemberOperation.OperationType.CONSUME.getValue() +
								   " WHERE MOH.restaurant_id = " + restaurantId;
				filterCond.append(" AND " + orderTblAlias + ".id IN (" + sql + ")");
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
		//Get the detail to used coupon.
		if(order.hasUsedCoupon()){
			order.setUsedCoupons(CouponOperationDao.getByCond(dbCon, staff, new CouponOperationDao.ExtraCond().addOperation(CouponOperation.Operate.ORDER_USE).setAssociateId(order.getId())));
		}
		//Get the detail to issued coupons.
		order.setIssuedCoupons(CouponOperationDao.getByCond(dbCon, staff, new CouponOperationDao.ExtraCond().addOperation(CouponOperation.Operate.ORDER_ISSUE).setAssociateId(order.getId())));
		//Get the detail to mixed payment.
		if(order.getPaymentType().isMixed()){
			order.setMixedPayment(MixedPaymentDao.getByCond(dbCon, staff, new MixedPaymentDao.ExtraCond(dateType, order)));
		}
		//Get the detail to associated wx orders.
		for(WxOrder wxOrder : WxOrderDao.getByCond(dbCon, staff, new WxOrderDao.ExtraCond().setOrder(order), null)){
			try{
				order.addWxOrder(WxOrderDao.getById(dbCon, staff, wxOrder.getId()));
			}catch(BusinessException | SQLException ignored){
				ignored.printStackTrace();
			}
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
	 * @throws BusinessException 
	 * 			throws if the staff does NOT exist in case of chain
	 */
	public static List<Order> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException{
		
		List<Order> result = new ArrayList<Order>();
		if(extraCond.isChain){
			
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the orders to the group.
			result.addAll(getByCond(dbCon, staff, ((ExtraCond)extraCond.clone()).setChain(false), orderClause));
			
			//Append orders to each branch.
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				result.addAll(getByCond(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false), orderClause));
			}
			
		}else{
			
			String sql;
			if(extraCond.dbTbl.dateType == DateType.TODAY){
				sql = " SELECT " +
					  (extraCond.isOnlyAmount ? 
					  " COUNT(*) " : 
					  " O.id, O.birth_date, O.order_date, O.seq_id, O.custom_num, O.table_id, O.table_alias, O.table_name, " +
					  " O.temp_staff, O.temp_date, " +
					  " R.restaurant_name, " +
					  " T.minimum_cost, IFNULL(T.category, 1) AS tbl_category, " +
					  " O.waiter, O.staff_id, " +
					  " O.region_id, O.region_name, O.restaurant_id, " +
					  " O.settle_type, O.pay_type_id, IFNULL(PT.name, '其他') AS pay_type_name, O.category, O.status, O.service_plan_id, O.service_rate, O.comment, " +
					  " O.discount_id, O.discount_staff_id, O.discount_staff, O.discount_date, " +
					  " O.price_plan_id, O.member_id, " +
					  " IF(O.coupon_price IS NULL, 0, 1) AS has_coupon, O.coupon_price, " +
					  " O.gift_price, O.cancel_price, O.discount_price, O.repaid_price, O.erase_price, O.pure_price, O.total_price, O.actual_price ") +
					  " FROM " + 
					  Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
					  " LEFT JOIN " + Params.dbName + ".restaurant R ON O.restaurant_id = R.id " +
					  " LEFT JOIN " + Params.dbName + ".table T ON O.table_id = T.table_id " +
					  " LEFT JOIN " + Params.dbName + ".pay_type PT ON O.pay_type_id = PT.pay_type_id " +
					  " WHERE 1 = 1 " + 
					  " AND O.restaurant_id = " + staff.getRestaurantId() + " " +
					  (extraCond != null ? extraCond.setRestaurant(staff.getRestaurantId()).toString() : "") + " " +
					  (orderClause != null ? orderClause : "");
				
			}else if(extraCond.dbTbl.dateType == DateType.HISTORY){
				sql = " SELECT " +
					  (extraCond.isOnlyAmount ? 
					  " COUNT(*) " : 
					  " OH.id, OH.birth_date, OH.order_date, OH.seq_id, OH.custom_num, OH.table_id, OH.table_alias, OH.table_name, " +
					  " OH.waiter, OH.staff_id, OH.discount_staff_id, OH.discount_staff, OH.discount_date, " +
					  " OH.region_id, OH.region_name, OH.restaurant_id, " +
					  " R.restaurant_name, " +
					  " IF(OH.coupon_price IS NULL, 0, 1) AS has_coupon, OH.coupon_price, " +
					  " OH.settle_type, OH.pay_type_id, IFNULL(PT.name, '其他') AS pay_type_name, OH.category, OH.status, OH.service_rate, OH.comment, " +
					  " OH.gift_price, OH.cancel_price, OH.discount_price, OH.repaid_price, OH.erase_price, OH.pure_price, OH.total_price, OH.actual_price ") +
					  " FROM " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " OH " + 
					  " LEFT JOIN " + Params.dbName + ".restaurant R ON OH.restaurant_id = R.id " + 
					  " LEFT JOIN " + Params.dbName + ".pay_type PT ON PT.pay_type_id = OH.pay_type_id " +
					  " WHERE 1 = 1 " + 
					  " AND OH.restaurant_id = " + staff.getRestaurantId() + " " +
					  (extraCond != null ? extraCond.setRestaurant(staff.getRestaurantId()).toString() : "") + " " +
					  (orderClause != null ? orderClause : "");
			}else{
				throw new IllegalArgumentException("The query type passed to query order is NOT valid.");
			}
			
			//Get the details to each order.
			dbCon.rs = dbCon.stmt.executeQuery(sql);		

			
			if(extraCond.isOnlyAmount){
				if(dbCon.rs.next()){
					result = Collections.nCopies(dbCon.rs.getInt(1), null);
				}else{
					result = Collections.emptyList();
				}
			}else{
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
					order.setRestaurantName(dbCon.rs.getString("restaurant_name"));
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
					if(extraCond.dbTbl.dateType == DateType.TODAY){
						table.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
						table.setCategory(Table.Category.valueOf(dbCon.rs.getShort("tbl_category")));
					}
					order.setDestTbl(table);
					order.getRegion().setRegionId(dbCon.rs.getShort("region_id"));
					order.getRegion().setName(dbCon.rs.getString("region_name"));

					order.setCustomNum(dbCon.rs.getShort("custom_num"));
					order.setCategory(Category.valueOf(dbCon.rs.getShort("category")));
					
					if(extraCond.dbTbl.dateType == DateType.TODAY){
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
					if(dbCon.rs.getBoolean("has_coupon")){
						order.setCouponPrice(dbCon.rs.getFloat("coupon_price"));
					}
					order.setPurePrice(dbCon.rs.getFloat("pure_price"));
					order.setTotalPrice(dbCon.rs.getFloat("total_price"));
					order.setActualPrice(dbCon.rs.getFloat("actual_price"));
					
					result.add(order);
				}
			}

			dbCon.rs.close();
		}
		
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
	 * @throws BusinessException 
	 */
	public static List<Order> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException{
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
	 * @return the difference result {@link UpdateOrder#DiffResult}
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the staff has no privilege for re-payment
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static UpdateOrder.DiffResult repaid(Staff staff, Order.RepaidBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			UpdateOrder.DiffResult diffResult = repaid(dbCon, staff, builder);
			dbCon.conn.commit();
			return diffResult;
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
	 * 			the builder to repaid order {@link UpdateOrder#RepaidBuilder}
	 * @return the difference result {@link UpdateOrder#DiffResult}
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the staff has no privilege for re-payment
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static UpdateOrder.DiffResult repaid(DBCon dbCon, Staff staff, Order.RepaidBuilder builder) throws BusinessException, SQLException{
		if(staff.getRole().hasPrivilege(Privilege.Code.RE_PAYMENT)){
			Order oriOrder = OrderDao.getById(staff, builder.getPayBuilder().getOrderId(), DateType.TODAY);
			//Restore the account if perform member consumption before.
			if(oriOrder.getMemberId() != 0){
				MemberDao.restore(dbCon, staff, oriOrder.getMemberId(), oriOrder);
			}
			//Restore the coupon if using coupon before & new coupons are used.
			if(oriOrder.hasUsedCoupon() && builder.hasCouponBuilder()){
				coupon(dbCon, staff, new Order.CouponBuilder(oriOrder.getId()));
			}
			if(builder.hasDiscountBuilder()){
				discount(dbCon, staff, builder.getDiscountBuilder());
			}
			if(builder.hasServiceBuilder()){
				service(dbCon, staff, builder.getServiceBuilder());
			}
			if(builder.hasCouponBuilder()){
				coupon(dbCon, staff, builder.getCouponBuilder());
			}
			UpdateOrder.DiffResult diff = UpdateOrder.exec(dbCon, staff, builder.getUpdateBuilder());
			PayOrder.pay(dbCon, staff, builder.getPayBuilder());
			
			return diff;
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
	public static TransResult transfer(Staff staff, Order.TransferBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			TransResult result = transfer(dbCon, staff, builder);
			dbCon.conn.commit();
			return result;
			
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static class TransResult{
		public final static TransResult EMPTY = new TransResult(null, null, null);
		public final Table srcTbl;
		public final Table destTbl;
		public final List<OrderFood> transFoods = new ArrayList<OrderFood>();
		
		public TransResult(Table source, Table dest, List<OrderFood> transFoods){
			this.destTbl = dest;
			this.srcTbl = source;
			if(transFoods != null){
				this.transFoods.addAll(transFoods);
			}
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
	public static TransResult transfer(DBCon dbCon, Staff staff, Order.TransferBuilder builder) throws BusinessException, SQLException{
		
		if(builder.getTransferFoods().isEmpty()){
			return TransResult.EMPTY;
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
				foodOut.asFood().setName(foodToOut.asFood().getName());
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
		
		return new TransResult(source.getDestTbl(), destTbl, builder.getTransferFoods());
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
		
		final Order order = OrderDao.getById(dbCon, staff, builder.getOrderId(), DateType.TODAY);

		if(builder.hasMember()){
			Member member = MemberDao.getById(dbCon, staff, builder.getMemberId());
			//Set the price plan.
			if(builder.hasPricePlan()){
				boolean isExist = false;
				//Check to see whether the price plan is allowed by this member.
				for(PricePlan pp : member.getMemberType().getPrices()){
					if(pp.getId() == builder.getPricePlanId()){
						order.setPricePlan(pp);
						isExist = true;
						break;
					}
				}
				if(!isExist){
					throw new BusinessException(StaffError.PRICE_PLAN_NOT_ALLOW);
				}
			}else{
				order.setPricePlan(member.getMemberType().getDefaultPrice());
			}
			
			//Set the discount.
			if(builder.hasDiscountId()){
				boolean isExist = false;
				//Check to see whether the discount is allowed by this member.
				for(Discount discount : member.getMemberType().getDiscounts()){
					if(discount.getId() == builder.getDiscountId()){
						order.setDiscount(discount);
						isExist = true;
						break;
					}
				}
				if(!isExist){
					throw new BusinessException(StaffError.DISCOUNT_NOT_ALLOW);
				}
			}else{
				order.setDiscount(member.getMemberType().getDefaultDiscount());
			}
		}else{
			final List<Discount> discounts = DiscountDao.getByCond(dbCon, staff, new DiscountDao.ExtraCond().setRole(staff.getRole()).setDiscountId(builder.getDiscountId()), DiscountDao.ShowType.BY_PLAN);
			if(discounts.isEmpty()){
				throw new BusinessException(StaffError.DISCOUNT_NOT_ALLOW);
			}else{
				order.setDiscount(discounts.get(0));
			}
			//Set the price plan to null.
			order.setPricePlan(null);
		}
		
		//Clear the coupons used before. 
		if(order.hasUsedCoupon() && order.getMemberId() != builder.getMemberId()){
			coupon(dbCon, staff, new Order.CouponBuilder(order.getId()));
		}
		
		String sql;
		
		//Update the order discount.
		sql = " UPDATE " + Params.dbName + ".order SET id = " + order.getId() +
			  " ,discount_staff_id = " + staff.getId() +
			  " ,discount_staff = '" + staff.getName() + "'" +
  			  " ,discount_date = NOW() " +
			  " ,discount_id = " + order.getDiscount().getId() +
			  " ,price_plan_id = " + (order.hasPricePlan() ? order.getPricePlan().getId() : " NULL ") +
			  " ,member_id = " + builder.getMemberId() +
			  " WHERE id = " + order.getId();
		dbCon.stmt.executeUpdate(sql);
		
		//Update each food's discount & unit price.
		for(OrderFood of : order.getOrderFoods()){
			sql = " UPDATE " + Params.dbName + ".order_food " +
				  " SET food_id = " + of.getFoodId() +
				  " ,discount = " + of.getDiscount() + 
				  " ,plan_price = " + (order.hasPricePlan() && of.asFood().hasPricePlan() ?  of.asFood().getPrice(order.getPricePlan()) : " NULL ") +
				  " WHERE order_id = " + order.getId() + 
				  " AND food_id = " + of.getFoodId() +
				  (of.hasFoodUnit() ? " AND food_unit_id = " + of.getFoodUnit().getId() : "");
			dbCon.stmt.executeUpdate(sql);				
		}	
		
	}
	
	/**
	 * Perform the coupon action to specific builder {@link Order.CouponBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the coupon builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the order to coupon does NOT exist
	 * 			<li>the order has NOT set member
	 */
	public static void coupon(Staff staff, Order.CouponBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			coupon(dbCon, staff, builder);
			dbCon.conn.commit();
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform the coupon action to specific builder {@link Order.CouponBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the coupon builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the order to coupon does NOT exist
	 * 			<li>the order has NOT set member
	 */
	public static void coupon(DBCon dbCon, Staff staff, Order.CouponBuilder builder) throws SQLException, BusinessException{
		
		Order order = getById(dbCon, staff, builder.getOrderId(), DateType.TODAY);
		
		if(!order.hasMember()){
			throw new BusinessException("此账单无会员信息，不能使用优惠券", PromotionError.COUPON_USE_NOT_ALLOW);
		}
		
		String sql;
		//Restore the coupons used in this order before.
		for(CouponOperation operation : order.getUsedCoupons()){
			
			sql = " UPDATE " + Params.dbName + ".coupon SET status = " + Coupon.Status.ISSUED.getVal() + " WHERE coupon_id = " + operation.getCouponId();
			dbCon.stmt.executeUpdate(sql);
			
			CouponOperationDao.deleteByCond(dbCon, staff, new CouponOperationDao.ExtraCond().setCoupon(operation.getCouponId()).addOperation(CouponOperation.Operate.ORDER_USE));
		}

		if(builder.getCoupons().isEmpty()){
			sql = " UPDATE " + Params.dbName + ".order SET coupon_price = null WHERE id = " + builder.getOrderId();
			dbCon.stmt.executeUpdate(sql);
			
		}else{
			//Use the coupons.
			CouponDao.use(dbCon, staff, Coupon.UseBuilder.newInstance4Order(order.getMemberId(), builder.getOrderId()).setCoupons(builder.getCoupons()));
			
			//Calculate the coupon price.
			final StringBuilder couponDetail = new StringBuilder();
			float couponPrice = 0;
			for(CouponOperation operation : CouponOperationDao.getByCond(dbCon, staff, new CouponOperationDao.ExtraCond().addOperation(CouponOperation.Operate.ORDER_USE).setAssociateId(builder.getOrderId()))){
				if(couponDetail.length() > 0){
					couponDetail.append(",");
				}
				couponDetail.append(operation.getCouponName());
				couponPrice += operation.getCouponPrice();
			}
			
			sql = " UPDATE " + Params.dbName + ".order SET coupon_price = " + couponPrice + " WHERE id = " + builder.getOrderId();
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
	 * Delete the order according to extra condition
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the order amount to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static int deleteByCond(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
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
	 * @throws BusinessException 
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		int amount = 0;
		for(Order order : getByCond(dbCon, staff, extraCond, null)){
			String sql;

			//Delete the records to normal taste group. 
			sql = " DELETE NTG FROM " + Params.dbName + "." + extraCond.dbTbl.ntgTbl + " NTG " +
				  " JOIN " + Params.dbName + "." + extraCond.dbTbl.tgTbl + " TG ON NTG.normal_taste_group_id = TG.normal_taste_group_id " +
				  " JOIN " + Params.dbName + "." + extraCond.dbTbl.orderFoodTbl + " OF ON OF.taste_group_id = TG.taste_group_id " +
				  " WHERE 1 = 1 " + 
				  " AND OF.order_id = " + order.getId() +
				  " AND TG.normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID;
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the records to taste group.
			sql = " DELETE TG FROM " + Params.dbName + "." + extraCond.dbTbl.tgTbl + " TG " +
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
			MixedPaymentDao.deleteByCond(dbCon, staff, new MixedPaymentDao.ExtraCond(extraCond.dbTbl.dateType, order.getId()));
			
			//Delete the temporary table
			if(extraCond.dbTbl.dateType.isToday() && !order.getDestTbl().getCategory().isNormal()){
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
	 * @throws BusinessException 
	 */
	public static ArchiveResult archive4Daily(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
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
	 * @throws BusinessException 
	 */
	public static ArchiveResult archive4Expired(DBCon dbCon, final Staff staff) throws SQLException, BusinessException{
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
	
	private static ArchiveResult archive(DBCon dbCon, Staff staff, ExtraCond extraCond, DateType archiveFrom, DateType archiveTo) throws SQLException, BusinessException{
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
