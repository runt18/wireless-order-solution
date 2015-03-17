package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.menuMgr.FoodDao.ExtraCond4Combo;
import com.wireless.db.menuMgr.FoodDao.ExtraCond4Price;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.dishesOrder.ComboOrderFood;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.OrderFood.Operation;
import com.wireless.pojo.dishesOrder.TasteGroup;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.util.DateType;

/**
 * The DB reflector is designed to the bridge between the OrderFood instance of
 * protocol and database.
 * 
 * @author Ying.Zhang
 * 
 */
public class OrderFoodDao {
	
	public static class ExtraCond4CancelFood extends ExtraCond{
		
		private int reasonId = -1;
		
		public ExtraCond4CancelFood(DateType dateType){
			super(dateType);
		}
		
		public ExtraCond4CancelFood setReasonId(int reasonId){
			this.reasonId = reasonId;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(reasonId > 0){
				extraCond.append(" AND " + orderFoodTblAlias + ".cancel_reason_id = " + reasonId);
			}
			extraCond.append(" AND " + orderTblAlias + ".cancel_price <> 0");
			extraCond.append(" AND " + orderFoodTblAlias + ".order_count < 0");
			extraCond.append(" AND " + orderFoodTblAlias + ".operation = " + OrderFood.Operation.CANCEL.getVal());
			extraCond.append(super.toString());
			
			return extraCond.toString();
		}
	}
	
	public static class ExtraCond{
		private final DateType dateType;
		final String orderFoodTblAlias = "OF";
		final String orderTblAlias = "O";
		final String orderFoodTbl;
		final String orderTbl;
		
		private int orderId;
		private Department.DeptId deptId;
		private DutyRange dutyRange;
		private HourRange hourRange;
		private Region.RegionId regionId;
		private int staffId;
		private String foodName;
		private boolean isGift;
		
		public ExtraCond(DateType dateType){
			this.dateType = dateType;
			if(dateType.isToday()){
				orderTbl = "order";
				orderFoodTbl = "order_food";
			}else{
				orderTbl = "order_history";
				orderFoodTbl = "order_food_history";
			}
		}
		
		public ExtraCond setOrder(Order order){
			this.orderId = order.getId();
			return this;
		}
		
		public ExtraCond setOrder(int orderId){
			this.orderId = orderId;
			return this;
		}
		
		public ExtraCond setDeptId(Department.DeptId deptId){
			this.deptId = deptId;
			return this;
		}
		
		public ExtraCond setDutyRange(DutyRange range){
			this.dutyRange = range;
			return this;
		}
		
		public ExtraCond setHourRange(HourRange range){
			this.hourRange = range;
			return this;
		}
		
		public ExtraCond setRegionId(Region.RegionId regionId){
			this.regionId = regionId;
			return this;
		}
		
		public ExtraCond setFoodName(String foodName){
			this.foodName = foodName;
			return this;
		}
		
		public ExtraCond setStaffId(int staffId){
			this.staffId = staffId;
			return this;
		}
		
		public ExtraCond setGift(boolean onOff){
			this.isGift = onOff;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(orderId > 0){
				extraCond.append(" AND " + orderTblAlias + ".id = " + orderId);
			}
			if(deptId != null){
				extraCond.append(" AND " + orderFoodTblAlias + ".dept_id = " + deptId.getVal());
			}
			if(dutyRange != null){
				extraCond.append(" AND " + orderTblAlias + ".order_date BETWEEN '" + dutyRange.getOnDutyFormat() + "' AND '" + dutyRange.getOffDutyFormat() + "'");
			}
			if(hourRange != null){
				extraCond.append(" AND TIME(" + orderFoodTblAlias + ".order_date) BETWEEN '" + hourRange.getOpeningFormat() + "' AND '" + hourRange.getEndingFormat() + "'");
			}
			if(regionId != null){
				extraCond.append(" AND " + orderTblAlias + ".region_id = " + regionId.getId());
			}
			if(staffId > 0){
				extraCond.append(" AND " + orderFoodTblAlias + ".staff_id = " + staffId);
			}
			if(foodName != null){
				extraCond.append(" AND " + orderFoodTblAlias + ".name LIKE '%" + foodName + "%'");
			}
			if(isGift){
				extraCond.append(" AND " + orderFoodTblAlias + ".is_gift = 1");
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Get each single detail from order to today according to the specific table. 
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return the list of order holding each single detail
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 */
	public static List<OrderFood> getSingleDetailByTableId(Staff staff, int tblId) throws BusinessException, SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getSingleDetailByTableId(dbCon, staff, tblId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get each single detail from order to today according to the specific table. 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return the list of order holding each single detail
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 */
	public static List<OrderFood> getSingleDetailByTableId(DBCon dbCon, Staff staff, int tblId) throws BusinessException, SQLException {
		int orderId = OrderDao.getByTableId(dbCon, staff, tblId).getId();
		return getSingleDetail(dbCon, staff, new ExtraCond(DateType.TODAY).setOrder(orderId), null);
	}
	
	/**
	 * Get each single detail of order according to extra condition. 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return the list of order holding each single detail
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement.
	 * @throws BusinessException 
	 * 			   throws if any associated taste group does NOT exist
	 */
	public static List<OrderFood> getSingleDetail(Staff staff, ExtraCond extraCond, String orderClause) throws Exception {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return OrderFoodDao.getSingleDetail(dbCon, staff, extraCond, orderClause);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get each single detail of order according to extra condition. 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return the list of order holding each single detail
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement.
	 * @throws BusinessException 
	 * 			   throws if any associated taste group does NOT exist
	 */
	public static List<OrderFood> getSingleDetail(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException {
		return getSingleDetail(dbCon, staff, extraCond.toString(), orderClause, extraCond.dateType);
	}
	
	/**
	 * Get each single detail from order to history. 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @param dateType the date type {@link DateType}
	 * @return the list of order holding each single detail
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement.
	 * @throws BusinessException 
	 * 			   throws if any associated taste group does NOT exist
	 */
	private static List<OrderFood> getSingleDetail(DBCon dbCon, Staff staff, String extraCond, String orderClause, DateType dateType) throws SQLException, BusinessException {
		String sql;

		if(dateType.isHistory()){
		sql = "SELECT OF.order_id, OF.taste_group_id, OF.is_temporary, OF.is_gift, OF.operation, " +
				" OF.restaurant_id, OF.food_id, OF.name, OF.food_status, OF.is_paid, " +
				" OF.unit_price, OF.order_count, OF.waiter, OF.order_date, OF.discount, OF.order_date, " +
				" OF.cancel_reason_id, IF(OF.cancel_reason_id = 1, '无原因', OF.cancel_reason) cancel_reason, " +
				" OF.kitchen_id, (CASE WHEN K.kitchen_id IS NULL THEN '已删除厨房' ELSE K.name END) AS kitchen_name, " +
				" OF.dept_id, (CASE WHEN D.dept_id IS NULL THEN '已删除部门' ELSE D.name END) as dept_name " +
				" FROM " + Params.dbName + ".order_food_history OF " +
				" JOIN " + Params.dbName + ".order_history O ON O.id = OF.order_id " +
				" LEFT JOIN " + Params.dbName + ".kitchen K ON OF.kitchen_id = K.kitchen_id " +
				" LEFT JOIN " + Params.dbName + ".department D ON D.dept_id = K.dept_id AND D.restaurant_id = K.restaurant_id " +
				" WHERE 1 = 1 " +
				" AND O.restaurant_id = " + staff.getRestaurantId() +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : " " + orderClause);
		
		}else if(dateType.isToday()){
			sql = " SELECT OF.id, OF.order_id, OF.taste_group_id, OF.is_temporary, OF.is_gift, OF.operation, " +
					" OF.restaurant_id, OF.food_id, OF.name, OF.food_status, OF.is_paid, " +
					" OF.unit_price, OF.order_count, OF.waiter, OF.order_date, OF.discount, OF.order_date, " +
					" OF.cancel_reason_id, OF.cancel_reason, " +
					" OF.kitchen_id, (CASE WHEN K.kitchen_id IS NULL THEN '已删除厨房' ELSE K.name END) AS kitchen_name, " +
					" OF.dept_id, (CASE WHEN D.dept_id IS NULL THEN '已删除部门' ELSE D.name END) as dept_name " +
					" FROM " + Params.dbName + ".order_food OF " +
					" JOIN " + Params.dbName + ".order O ON OF.order_id = O.id " +
					" LEFT JOIN " + Params.dbName + ".kitchen K " + " ON OF.kitchen_id = K.kitchen_id " +
					" LEFT JOIN " + Params.dbName + ".department D " + " ON D.dept_id = K.dept_id AND D.restaurant_id = K.restaurant_id " +
					" WHERE 1 = 1 " +
					" AND O.restaurant_id = " + staff.getRestaurantId() +
					(extraCond == null ? "" : extraCond) +
					(orderClause == null ? "" : " " + orderClause);
		}else{
			throw new IllegalArgumentException("The date type is invalid.");
		}
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<OrderFood> orderFoods = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood of = new OrderFood();
			of.setOrderId(dbCon.rs.getInt("order_id"));
			of.asFood().setFoodId(dbCon.rs.getInt("food_id"));
			of.asFood().setName(dbCon.rs.getString("name"));
			of.asFood().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			of.asFood().setStatus(dbCon.rs.getShort("food_status"));
			of.setRepaid(dbCon.rs.getBoolean("is_paid"));
			of.setGift(dbCon.rs.getBoolean("is_gift"));
			of.setOperation(OrderFood.Operation.valueOf(dbCon.rs.getInt("operation")));
			
			int tasteGroupId = dbCon.rs.getInt("taste_group_id");
			//Get the detail to taste group.
			if(tasteGroupId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				of.setTasteGroup(TasteGroupDao.getById(staff, tasteGroupId, dateType));
			}
			
			of.setCount(dbCon.rs.getFloat("order_count"));
			of.asFood().setPrice(dbCon.rs.getFloat("unit_price"));
			of.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			of.setWaiter(dbCon.rs.getString("waiter"));
			
			Kitchen kitchen = new Kitchen(dbCon.rs.getInt("kitchen_id"));
			kitchen.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			kitchen.setName(dbCon.rs.getString("kitchen_name"));
			
			Department dept = new Department(dbCon.rs.getShort("dept_id"));
			dept.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			dept.setName(dbCon.rs.getString("dept_name"));
			
			kitchen.setDept(dept);
			of.asFood().setKitchen(kitchen);
			
			of.setDiscount(dbCon.rs.getFloat("discount"));
			of.setTemp(dbCon.rs.getBoolean("is_temporary"));
			
			CancelReason cr = new CancelReason(dbCon.rs.getInt("cancel_reason_id"),
											   dbCon.rs.getString("cancel_reason"),
											   dbCon.rs.getInt("restaurant_id"));
			of.setCancelReason(cr);
			
			orderFoods.add(of);
		}
		dbCon.rs.close();
		
		return orderFoods;
	}

	/**
	 * Get the detail according to extra condition
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the order food
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any taste group does NOT exist 
	 */
	public static List<OrderFood> getDetail(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		String sql;

		sql = " SELECT OF.order_id, OF.food_id, OF.taste_group_id, OF.is_temporary, OF.is_gift, " +
			  " MIN(OF.id) AS id, MAX(OF.restaurant_id) AS restaurant_id, MAX(OF.kitchen_id) AS kitchen_id, " + 
			  (extraCond.dateType.isToday() ? " MAX(OF.combo_id) AS combo_id, " : "") +
			  " MAX(OF.name) AS name, MAX(OF.food_status) AS food_status, " +
			  " MAX(OF.unit_price) AS unit_price, MAX(OF.commission) AS commission, MAX(OF.waiter) AS waiter, MAX(OF.order_date) AS order_date, MAX(OF.discount) AS discount, " +
			  " MAX(OF.dept_id) AS dept_id, MAX(OF.id) AS id, MAX(OF.order_date) AS pay_datetime, SUM(OF.order_count) AS order_sum " +
			  " FROM " + Params.dbName + "." + extraCond.orderFoodTbl + " " + extraCond.orderFoodTblAlias +
			  " JOIN " + Params.dbName + "." + extraCond.orderTbl + " " + extraCond.orderTblAlias +
			  " ON OF.order_id = O.id " +
			  " WHERE 1 = 1 " +
			  (extraCond == null ? "" : extraCond) +
			  " GROUP BY OF.food_id, OF.taste_group_id, OF.is_temporary, OF.is_gift " + 
			  " HAVING order_sum > 0 " +
			  " ORDER BY id ASC ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<OrderFood> result = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood of = new OrderFood(dbCon.rs.getLong("id"));
			of.asFood().setFoodId(dbCon.rs.getInt("food_id"));
			of.asFood().setName(dbCon.rs.getString("name"));
			of.asFood().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			of.asFood().setStatus(dbCon.rs.getShort("food_status"));
			if(extraCond.dateType.isToday()){
				int comboId = dbCon.rs.getInt("combo_id");
				if(comboId > 0){
					for(ComboOrderFood cof : ComboDao.getByComboId(staff, comboId, extraCond.dateType)){
						of.addCombo(cof);
					}
				}
			}
			int tgId = dbCon.rs.getInt("taste_group_id");
			if(tgId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				of.setTasteGroup(new TasteGroup(tgId, null, null, null));
			}
			of.setCount(dbCon.rs.getFloat("order_sum"));
			of.asFood().setPrice(dbCon.rs.getFloat("unit_price"));
			of.asFood().setCommission(dbCon.rs.getFloat("commission"));
			of.setOrderDate(dbCon.rs.getTimestamp("pay_datetime").getTime());
			of.setWaiter(dbCon.rs.getString("waiter"));
			of.getKitchen().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			of.getKitchen().setId(dbCon.rs.getInt("kitchen_id"));
			of.getKitchen().getDept().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			of.getKitchen().getDept().setId(dbCon.rs.getShort("dept_id"));
			of.setDiscount(dbCon.rs.getFloat("discount"));
			of.setTemp(dbCon.rs.getBoolean("is_temporary"));
			of.setGift(dbCon.rs.getBoolean("is_gift"));
			result.add(of);
		}
		dbCon.rs.close();
		
		for(OrderFood of : result){
			if(extraCond.dateType.isToday()){
				//Get the details to the combo.
				if(of.asFood().isCombo()){
					of.asFood().setChildFoods(FoodDao.getComboByCond(dbCon, staff, new ExtraCond4Combo(of.asFood().getFoodId())));
				}
				//Get the detail to associated price plan.
				of.asFood().setPricePlan(FoodDao.getPricePlan(dbCon, staff, new ExtraCond4Price(of.asFood())));
			}
			if(of.getTasteGroup() != null){
				of.setTasteGroup(TasteGroupDao.getById(staff, of.getTasteGroup().getGroupId(), extraCond.dateType));
			}
		}
		
		return result;
	}
	
	static class ExtraBuilder{
		private final int orderId;
		private final OrderFood extra;
		private boolean isPaid = false;
		
		public ExtraBuilder(int orderId, OrderFood extra){
			this.orderId = orderId;
			this.extra = extra;
			this.extra.setOperation(Operation.ADD);
		}
		
		public ExtraBuilder setPaid(boolean isPaid){
			this.isPaid = isPaid;
			return this;
		}
	}
	
	static void insertExtra(DBCon dbCon, Staff staff, ExtraBuilder builder) throws SQLException{
		/**
		 * Insert the taste group info if containing taste.
		 */
		if(builder.extra.hasTasteGroup()){
			
			TasteGroup tg = builder.extra.getTasteGroup();		
			
			if(tg.getGroupId() == TasteGroup.EMPTY_TASTE_GROUP_ID){
				int tgId = TasteGroupDao.insert(dbCon, staff, new TasteGroup.InsertBuilder(builder.extra.asFood())
			     															.addTastes(tg.getNormalTastes())
			     															.setTmpTaste(tg.getTmpTaste()));
				tg.setGroupId(tgId);
			}			
		}
		
		//Insert the combo order food
		int comboId = 0;
		if(builder.extra.hasCombo()){
			comboId = ComboDao.insert(dbCon, staff, builder.extra.getCombo());
		}

		String sql;
		sql = " INSERT INTO " + Params.dbName + ".order_food " +
			  " ( " + 
			  " `restaurant_id`, `order_id`, `operation`, `food_id`, `order_count`, `unit_price`, `commission`, `name`, `food_status`, " +
			  " `discount`, `taste_group_id`, " +
			  " `dept_id`, `kitchen_id`, " +
			  " `staff_id`, `waiter`, `order_date`, `is_temporary`, `is_paid`, `is_gift`, " +
			  " `combo_id` " +
			  " ) " +
			  " VALUES " +
			  "(" +
			  staff.getRestaurantId() + ", " +
			  builder.orderId + ", " +
			  builder.extra.getOperation().getVal() + "," +
			  builder.extra.getFoodId() + ", " +
			  builder.extra.getCount() + ", " + 
			  builder.extra.asFood().getPrice() + ", " + 
			  builder.extra.asFood().getCommission() + ", " +
			  "'" + builder.extra.getName() + "', " + 
			  builder.extra.asFood().getStatus() + ", " +
			  builder.extra.getDiscount() + ", " +
			  (builder.extra.hasTasteGroup() ? builder.extra.getTasteGroup().getGroupId() : TasteGroup.EMPTY_TASTE_GROUP_ID) + ", " +
			  builder.extra.getKitchen().getDept().getId() + ", " +
			  builder.extra.getKitchen().getId() + ", " +
			  staff.getId() + ", " +
			  "'" + staff.getName() + "', " +
			  "NOW(), " + 
			  (builder.extra.isTemp() ? 1 : 0) + ", " +
			  (builder.isPaid ? 1 : 0) + "," +
			  (builder.extra.isGift() ? 1 : 0) + "," +
			  (comboId != 0 ? comboId : "NULL") +
			  " ) ";
		dbCon.stmt.executeUpdate(sql);	
		
		//FIXME Insert the temporary food to menu.
//		if(builder.extra.isTemp()){
//			try{
//				FoodDao.insert(dbCon, staff, new Food.InsertBuilder(builder.extra.getName(), builder.extra.getPrice(), builder.extra.getKitchen()).setTemp(true));
//			}catch(BusinessException ingored){}
//		}
	}
	
	static class GiftBuilder{
		private final CancelBuilder giftBuilder;;
		
		public GiftBuilder(int orderId, OrderFood giftFood){
			giftBuilder = new CancelBuilder(orderId, giftFood);
			giftBuilder.cancel.setOperation(Operation.GIFT);
		}
		
		public CancelBuilder asCancel(){
			return this.giftBuilder;
		}
	}
	
	static class TransferBuilder{
		
		private final CancelBuilder cancelBuilder;;
		
		public TransferBuilder(int orderId, OrderFood foodOut){
			cancelBuilder = new CancelBuilder(orderId, foodOut);
			cancelBuilder.cancel.setOperation(Operation.TRANSFER);
		}
		
		public CancelBuilder asCancel(){
			return this.cancelBuilder;
		}
	}
	
	static class CancelBuilder{
		private final int orderId;
		private final OrderFood cancel;
		private boolean isPaid = false;
		
		public CancelBuilder(int orderId, OrderFood cancel){
			this.orderId = orderId;
			this.cancel = cancel;
			this.cancel.setOperation(Operation.CANCEL);
		}
		
		public CancelBuilder setPaid(boolean isPaid){
			this.isPaid = isPaid;
			return this;
		}
		
	}
	
	static void insertCancelled(DBCon dbCon, Staff staff, CancelBuilder builder) throws SQLException{
		String sql;
		sql = " INSERT INTO `" + Params.dbName + "`.`order_food` " +
			  " ( " +
			  " `restaurant_id`, `order_id`, `operation`, `food_id`, `order_count`, `unit_price`, `commission`, `name`, `food_status`, " +
			  " `discount`, `taste_group_id`, `cancel_reason_id`, `cancel_reason`, " +
			  " `dept_id`, `kitchen_id`, " +
			  " `staff_id`, `waiter`, `order_date`, `is_temporary`, `is_paid`, `is_gift`) VALUES (" +
			  staff.getRestaurantId() + ", " +
			  builder.orderId + ", " +
			  builder.cancel.getOperation().getVal() + "," +
			  builder.cancel.getFoodId() + ", " +
			  "-" + builder.cancel.getCount() + ", " + 
			  builder.cancel.asFood().getPrice() + ", " + 
			  builder.cancel.asFood().getCommission() + "," +
			  "'" + builder.cancel.getName() + "', " + 
			  builder.cancel.asFood().getStatus() + ", " +
			  builder.cancel.getDiscount() + ", " +
			  (builder.cancel.hasTasteGroup() ? builder.cancel.getTasteGroup().getGroupId() : TasteGroup.EMPTY_TASTE_GROUP_ID) + ", " +
			  (builder.cancel.hasCancelReason() ? builder.cancel.getCancelReason().getId() : CancelReason.NO_REASON) + ", " +
			  (builder.cancel.hasCancelReason() ? "'" + builder.cancel.getCancelReason().getReason() + "'" : "NULL") + ", " +
			  builder.cancel.getKitchen().getDept().getId() + ", " +
			  builder.cancel.getKitchen().getId() + ", " +
			  staff.getId() + ", " +
			  "'" + staff.getName() + "', " +
			  " NOW(), " + 
			  (builder.cancel.isTemp() ? 1 : 0) + ", " +
			  (builder.isPaid ? 1 : 0) + ", " +
			  (builder.cancel.isGift() ? 1 : 0 ) +
			 " ) ";
		dbCon.stmt.executeUpdate(sql);	
	}
	
	/**
	 * Fill the detail to this order food.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param src
	 * 			the source to order food
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below<br>
	 * 			<li>any cancel reason does NOT exist
	 * 			<li>the associated food does NOT exist
	 * 			<li>and taste does NOT exist
	 */
	static void fill(DBCon dbCon, Staff staff, OrderFood src) throws SQLException, BusinessException{
		//Get the details to cancel reason if contained and not temporary.
		if(src.hasCancelReason() && !src.getCancelReason().isTemp()){
			src.setCancelReason(CancelReasonDao.getById(dbCon, staff, src.getCancelReason().getId()));
		}
		
		if(src.isTemp()){
			src.asFood().setKitchen(KitchenDao.getById(dbCon, staff, src.getKitchen().getId()));
			
		}else{		
			//Get the details to each order food.
			src.asFood().copyFrom(FoodDao.getById(dbCon, staff, src.getFoodId()));
			
			//Get the details to normal tastes.
			if(src.hasNormalTaste()){
				//Get the detail to each taste.
				for(Taste t : src.getTasteGroup().getTastes()){
					t.copyFrom(TasteDao.getTasteById(dbCon, staff, t.getTasteId()));
				}
				//Get the detail to each spec.
				if(src.getTasteGroup().hasSpec()){
					src.getTasteGroup().getSpec().copyFrom(TasteDao.getTasteById(dbCon, staff, src.getTasteGroup().getSpec().getTasteId()));
				}
				src.getTasteGroup().refresh();
			}
			
			//Get the detail to combo
			if(src.hasCombo()){
				for(ComboOrderFood cof : src.getCombo()){
					//Get the details to combo food.
					cof.asComboFood().copyFrom(FoodDao.getComboByCond(dbCon, staff, new ExtraCond4Combo(src.asFood().getFoodId()).setChildId(cof.asComboFood().getFoodId())).get(0));
					//Get the details to normal tastes of combo
					if(cof.hasTasteGroup() && cof.getTasteGroup().hasNormalTaste()){
						//Get the details to each taste of combo.
						for(Taste t : cof.getTasteGroup().getTastes()){
							t.copyFrom(TasteDao.getTasteById(dbCon, staff, t.getTasteId()));
						}
						//Get the detail to each spec of combo
						if(cof.getTasteGroup().hasSpec()){
							cof.getTasteGroup().getSpec().copyFrom(TasteDao.getTasteById(dbCon, staff, cof.getTasteGroup().getSpec().getTasteId()));
						}
						cof.getTasteGroup().refresh();
					}
				}
			}
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
	
	static ArchiveResult archive(DBCon dbCon, Staff staff, String paidOrder) throws SQLException{
		
		final String orderFoodItem = "`id`,`restaurant_id`, `order_id`, `operation`, `food_id`, `order_date`, `order_count`," + 
				"`unit_price`, `commission`, `name`, `food_status`, `taste_group_id`, `cancel_reason_id`, `cancel_reason`," +
				"`discount`, `dept_id`, `kitchen_id`, " +
				"`staff_id`, `waiter`, `is_temporary`, `is_paid`, `is_gift`";

		String sql;
		
		//Move the records from today to history.
		sql = " INSERT INTO " + Params.dbName + ".order_food_history (" + orderFoodItem + ") " +
			  " SELECT " + orderFoodItem + " FROM " + Params.dbName + ".order_food " +
			  " WHERE " +
			  " order_id IN ( " + paidOrder + " ) ";
		
		int orderAmount = dbCon.stmt.executeUpdate(sql);
		
		int maxId = 0;
		//Calculate the max order food id from both today and history.
		sql = " SELECT MAX(id) + 1 FROM (" +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order_food " +
			  " UNION " +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order_food_history) AS all_order_food";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			maxId = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		sql = " DELETE FROM " + Params.dbName + ".order_food WHERE restaurant_id = " + Restaurant.ADMIN;
		dbCon.stmt.executeUpdate(sql);
		
		//Insert the max id.
		sql = " INSERT INTO " + Params.dbName + ".order_food " +
			  " (id, restaurant_id) VALUES(" +
			  maxId + "," +
			  Restaurant.ADMIN + 
			  ")";
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the paid order food from 'order_food' table.
		sql = " DELETE FROM " + Params.dbName + ".order_food WHERE order_id IN (" + paidOrder + ")";
		dbCon.stmt.executeUpdate(sql);
		
		return new ArchiveResult(maxId, orderAmount);
	}
	
}
