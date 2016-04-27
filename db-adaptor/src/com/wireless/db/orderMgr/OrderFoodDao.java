package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.DBTbl;
import com.wireless.db.Params;
import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.menuMgr.FoodDao.ExtraCond4Combo;
import com.wireless.db.menuMgr.FoodDao.ExtraCond4Price;
import com.wireless.db.menuMgr.FoodUnitDao;
import com.wireless.db.menuMgr.PricePlanDao;
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
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodUnit;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;

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
		private Integer kitchenId;
		private DutyRange dutyRange;
		private HourRange hourRange;
		private Region.RegionId regionId;
		private int staffId;
		private String foodName;
		private int foodId;
		private Boolean isGift;
		
		public ExtraCond(DateType dateType){
			this.dateType = dateType;
			DBTbl dbTbl = new DBTbl(dateType);
			orderTbl = dbTbl.orderTbl;
			orderFoodTbl = dbTbl.orderFoodTbl;
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
		
		public ExtraCond setDutyRange(String beginDate, String endDate){
			this.dutyRange = new DutyRange(beginDate, endDate);
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
		
		
		
		public ExtraCond setKitchen(Kitchen kitchen){
			this.kitchenId = Integer.valueOf(kitchen.getId());
			return this;
		}
		
		public ExtraCond setKitchen(int kitchenId){
			this.kitchenId = Integer.valueOf(kitchenId);
			return this;
		}
		
		public ExtraCond setFood(Food food){
			this.foodId = food.getFoodId();
			return this;
		}
		
		public ExtraCond setFood(int foodId){
			this.foodId = foodId;
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
			if(kitchenId != null){
				extraCond.append(" AND " + orderFoodTblAlias + ".kitchen_id = " + kitchenId.intValue());
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
			if(foodId > 0){
				extraCond.append(" AND " + orderFoodTblAlias + ".food_id = " + foodId);
			}
			if(isGift != null){
				extraCond.append(" AND " + orderFoodTblAlias + ".is_gift = " + (isGift.booleanValue() ? "1" : "0"));
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
	 */
	public static List<OrderFood> getSingleDetail(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException {
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
	 */
	public static List<OrderFood> getSingleDetail(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException {
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
	 */
	private static List<OrderFood> getSingleDetail(DBCon dbCon, Staff staff, String extraCond, String orderClause, DateType dateType) throws SQLException {
		String sql;

		if(dateType.isHistory()){
			sql = "SELECT OF.order_id, OF.taste_group_id, OF.is_temporary, OF.is_gift, OF.operation, " +
					" OF.restaurant_id, OF.food_id, OF.name, OF.food_status, OF.commission, OF.is_paid, " +
					" OF.unit_price, OF.order_count, OF.staff_id, OF.waiter, OF.order_date, OF.discount, OF.order_date, " +
					" OF.food_unit_id, OF.food_unit, OF.food_unit_price, " +
					" IFNULL(OF.plan_price, -1) AS plan_price, " +
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
					" OF.restaurant_id, OF.food_id, OF.name, OF.food_status, OF.commission, OF.is_paid, " +
					" OF.unit_price, OF.order_count, OF.staff_id, OF.waiter, OF.order_date, OF.discount, OF.order_date, " +
					" OF.food_unit_id, OF.food_unit, OF.food_unit_price, " +
					" IFNULL(OF.plan_price, -1) AS plan_price, " +
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
			of.asFood().setCommission(dbCon.rs.getFloat("commission"));
			of.setRepaid(dbCon.rs.getBoolean("is_paid"));
			of.setGift(dbCon.rs.getBoolean("is_gift"));
			of.setOperation(OrderFood.Operation.valueOf(dbCon.rs.getInt("operation")));
			
			if(dbCon.rs.getInt("food_unit_id") != 0){
				FoodUnit foodUnit = new FoodUnit(dbCon.rs.getInt("food_unit_id"));
				foodUnit.setFoodId(dbCon.rs.getInt("food_id"));
				foodUnit.setPrice(dbCon.rs.getFloat("food_unit_price"));
				foodUnit.setUnit(dbCon.rs.getString("food_unit"));
				of.setFoodUnit(foodUnit);
			}
			
			int tasteGroupId = dbCon.rs.getInt("taste_group_id");
			//Get the detail to taste group.
			if(tasteGroupId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				try{
					of.setTasteGroup(TasteGroupDao.getById(staff, tasteGroupId, dateType));
				}catch(BusinessException ignored){
					ignored.printStackTrace();
				}
			}
			
			of.setCount(dbCon.rs.getFloat("order_count"));
			of.setPlanPrice(dbCon.rs.getFloat("plan_price"));
			of.asFood().setPrice(dbCon.rs.getFloat("unit_price"));
			of.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			of.setStaffId(dbCon.rs.getInt("staff_id"));
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

		sql = " SELECT OF.order_id, OF.food_id, OF.taste_group_id, OF.food_unit_id, OF.is_temporary, OF.is_gift, " +
			  (extraCond.dateType.isToday() ? " combo_id, " : "") +
			  " MIN(OF.id) AS id, MAX(OF.restaurant_id) AS restaurant_id, MAX(OF.kitchen_id) AS kitchen_id, " + 
			  " MAX(OF.name) AS name, MAX(OF.food_status) AS food_status, " +
			  " MAX(OF.food_unit) AS food_unit, MAX(OF.food_unit_price) AS food_unit_price, " +
			  " IFNULL(MAX(OF.plan_price), -1) AS plan_price, " +
			  " MAX(OF.unit_price) AS unit_price, MAX(OF.commission) AS commission, MAX(OF.waiter) AS waiter, MAX(OF.order_date) AS order_date, MAX(OF.discount) AS discount, " +
			  " MAX(OF.dept_id) AS dept_id, MAX(OF.id) AS id, MAX(OF.order_date) AS pay_datetime, SUM(OF.order_count) AS order_sum, " +
			  " MAX(F.print_kitchen_id) AS print_kitchen_id " +
			  " FROM " + Params.dbName + "." + extraCond.orderFoodTbl + " " + extraCond.orderFoodTblAlias +
			  " JOIN " + Params.dbName + "." + extraCond.orderTbl + " " + extraCond.orderTblAlias + " ON OF.order_id = O.id " +
			  " LEFT JOIN " + Params.dbName + ".food F ON OF.food_id = F.food_id " +
			  " WHERE 1 = 1 " +
			  (extraCond == null ? "" : extraCond) +
			  " GROUP BY OF.food_id, OF.taste_group_id, OF.food_unit_id, OF.is_temporary, OF.is_gift " + (extraCond.dateType.isToday() ? ", OF.combo_id" : "") +
			  " HAVING order_sum > 0 " +
			  " ORDER BY id ASC ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		final List<OrderFood> result = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood of = new OrderFood(dbCon.rs.getLong("id"));
			of.setOrderId(dbCon.rs.getInt("order_id"));
			of.setPlanPrice(dbCon.rs.getFloat("plan_price"));
			of.asFood().setFoodId(dbCon.rs.getInt("food_id"));
			of.asFood().setName(dbCon.rs.getString("name"));
			of.asFood().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			of.asFood().setStatus(dbCon.rs.getShort("food_status"));
			of.asFood().setPrintKitchenId(dbCon.rs.getInt("print_kitchen_id"));
			if(extraCond.dateType.isToday()){
				int comboId = dbCon.rs.getInt("combo_id");
				of.setComboId(comboId);
			}
			int tgId = dbCon.rs.getInt("taste_group_id");
			if(tgId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				of.setTasteGroup(new TasteGroup(tgId, null, null, null));
			}
			if(dbCon.rs.getInt("food_unit_id") != 0){
				FoodUnit foodUnit = new FoodUnit(dbCon.rs.getInt("food_unit_id"));
				foodUnit.setFoodId(dbCon.rs.getInt("food_id"));
				foodUnit.setUnit(dbCon.rs.getString("food_unit"));
				foodUnit.setPrice(dbCon.rs.getFloat("food_unit_price"));
				of.setFoodUnit(foodUnit);
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
				//Get the detail to combo order food.
				if(of.getComboId() > 0){
					of.setCombo(ComboDao.getByComboId(dbCon, staff, of.getComboId(), extraCond.dateType));
				}
				//Get the details to the combo of the food.
				if(of.asFood().isCombo()){
					of.asFood().setChildFoods(FoodDao.getComboByCond(dbCon, staff, new ExtraCond4Combo(of.asFood())));
				}
				for(ComboFood comboFood : of.asFood().getChildFoods()){
					ComboOrderFood cof = null;
					for(ComboOrderFood each : of.getCombo()){
						if(each.asComboFood().equals(comboFood)){
							cof = each;
							break;
						}
					}
					if(cof == null){
						of.addCombo(new ComboOrderFood(comboFood));
					}
				}
				//Get the detail to associated price plan.
				of.asFood().setPricePlan(FoodDao.getPricePlan(dbCon, staff, new ExtraCond4Price(of.asFood())));
				//使用下单时的价格作为方案价钱
				if(of.getPlanPrice() >= 0){
					of.asFood().addPricePlan(PricePlanDao.getByCond(dbCon, staff, new PricePlanDao.ExtraCond().setOrder(of.getOrderId())).get(0), of.getPlanPrice());
				}
			}
			//Get the detail to taste group.
			if(of.getTasteGroup() != null){
				try{
					of.setTasteGroup(TasteGroupDao.getById(dbCon, staff, of.getTasteGroup().getGroupId(), extraCond.dateType));
				}catch(BusinessException e){
					of.clearTasetGroup();
				}
			}
		}
		
		return result;
	}
	
	static class ExtraBuilder{
		private final Order order;
		private final OrderFood extra;
		private boolean isPaid = false;
		
		public ExtraBuilder(Order order, OrderFood extra){
			this.order = order;
			this.extra = extra;
			this.extra.setOperation(Operation.ADD);
		}
		
		public ExtraBuilder setPaid(boolean isPaid){
			this.isPaid = isPaid;
			return this;
		}
	}
	
	/**
	 * Insert the extra order food according to builder {@link ExtraBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the extra builder {@link ExtraBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the remaining to limit food is insufficient 
	 */
	static void insertExtra(DBCon dbCon, Staff staff, ExtraBuilder builder) throws SQLException, BusinessException{
		/**
		 * Insert the taste group info if containing taste.
		 */
		if(builder.extra.hasTasteGroup()){
			
			TasteGroup tg = builder.extra.getTasteGroup();		
			
			if(tg.getGroupId() == TasteGroup.EMPTY_TASTE_GROUP_ID){
				int tgId = TasteGroupDao.insert(dbCon, staff, new TasteGroup.InsertBuilder(builder.extra)
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
			  " `food_unit_id`, `food_unit`, `food_unit_price`, " +
			  " `plan_price`, " +
			  " `discount`, `taste_group_id`, " +
			  " `dept_id`, `kitchen_id`, " +
			  " `staff_id`, `waiter`, `order_date`, `is_temporary`, `is_paid`, `is_gift`, " +
			  " `combo_id` " +
			  " ) " +
			  " VALUES " +
			  "(" +
			  staff.getRestaurantId() + ", " +
			  builder.order.getId() + ", " +
			  builder.extra.getOperation().getVal() + "," +
			  builder.extra.getFoodId() + ", " +
			  Math.abs(builder.extra.getDelta()) + ", " + 
			  builder.extra.asFood().getPrice() + ", " + 
			  builder.extra.asFood().getCommission() + ", " +
			  "'" + builder.extra.asFood().getName() + "', " + 
			  builder.extra.asFood().getStatus() + ", " +
			  (builder.extra.hasFoodUnit() ? builder.extra.getFoodUnit().getId() : " NULL ") + "," +
			  (builder.extra.hasFoodUnit() ? "'" + builder.extra.getFoodUnit().getUnit() + "'" : " NULL ") + "," +
			  (builder.extra.hasFoodUnit() ? builder.extra.getFoodUnit().getPrice() : " NULL ") + "," +
			  (builder.order.hasPricePlan() ? builder.extra.asFood().getPrice(builder.order.getPricePlan()) : " NULL") + "," +
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
		
		//Calculate the limit remaining.
		if(builder.extra.asFood().isLimit()){
			builder.extra.asFood().copyFrom(FoodDao.getPureById(dbCon, staff, builder.extra.getFoodId()));
			int limitRemaining = Math.round(builder.extra.asFood().getLimitRemaing() - Math.abs(builder.extra.getDelta()));
			if(limitRemaining < 0){
				throw new BusinessException("【" + builder.extra.asFood().getName() + "】的点菜数量超过设定的限量数量");
			}
			try{
				FoodDao.update(dbCon, staff, new Food.LimitRemainingBuilder(builder.extra.asFood(), limitRemaining));
			} catch (BusinessException ignored) {
				ignored.printStackTrace();
			}
		}
		
		//FIXME Insert the temporary food to menu.
//		if(builder.extra.isTemp()){
//			try{
//				FoodDao.insert(dbCon, staff, new Food.InsertBuilder(builder.extra.getName(), builder.extra.getPrice(), builder.extra.getKitchen()).setTemp(true));
//			}catch(BusinessException ingored){}
//		}
	}
	
	static class TransferBuilder{
		
		private final CancelBuilder cancelBuilder;;
		
		public TransferBuilder(Order order, OrderFood foodOut){
			cancelBuilder = new CancelBuilder(order, foodOut);
			cancelBuilder.cancel.setOperation(Operation.TRANSFER);
		}
		
		public CancelBuilder asCancel(){
			return this.cancelBuilder;
		}
	}
	
	static class CancelBuilder{
		private final Order order;
		private final OrderFood cancel;
		private boolean isPaid = false;
		
		public CancelBuilder(Order order, OrderFood cancel){
			this.order = order;
			this.cancel = cancel;
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
			  " `food_unit_id`, `food_unit`, `food_unit_price`, " +
			  " `plan_price`, " +
			  " `discount`, `taste_group_id`, `cancel_reason_id`, `cancel_reason`, " +
			  " `dept_id`, `kitchen_id`, " +
			  " `staff_id`, `waiter`, `order_date`, `is_temporary`, `is_paid`, `is_gift`, `combo_id`) VALUES (" +
			  staff.getRestaurantId() + ", " +
			  builder.order.getId() + ", " +
			  builder.cancel.getOperation().getVal() + "," +
			  builder.cancel.getFoodId() + ", " +
			  "-" + Math.abs(builder.cancel.getDelta()) + ", " + 
			  builder.cancel.asFood().getPrice() + ", " + 
			  builder.cancel.asFood().getCommission() + "," +
			  "'" + builder.cancel.asFood().getName() + "', " + 
			  builder.cancel.asFood().getStatus() + ", " +
			  (builder.cancel.hasFoodUnit() ? builder.cancel.getFoodUnit().getId() : " NULL ") + "," +
			  (builder.cancel.hasFoodUnit() ? "'" + builder.cancel.getFoodUnit().getUnit() + "'" : " NULL ") + "," +
			  (builder.cancel.hasFoodUnit() ? builder.cancel.getFoodUnit().getPrice() : " NULL ") + "," +
			  (builder.order.hasPricePlan() ? builder.cancel.asFood().getPrice(builder.order.getPricePlan()) : " NULL") + "," +
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
			  (builder.cancel.isGift() ? 1 : 0 ) + ", " +
			  (builder.cancel.getComboId() != 0 ? builder.cancel.getComboId() : "NULL") +
			  " ) ";
		dbCon.stmt.executeUpdate(sql);	
		
		//Add and update the food to be on sale.
		if(builder.cancel.asFood().isLimit()){
			try {
				builder.cancel.asFood().copyFrom(FoodDao.getById(dbCon, staff, builder.cancel.getFoodId()));
				int limitRemaining = Math.round(builder.cancel.asFood().getLimitRemaing() + Math.abs(builder.cancel.getDelta()));
				limitRemaining = limitRemaining > builder.cancel.asFood().getLimitAmount() ? builder.cancel.asFood().getLimitAmount() : limitRemaining;
				FoodDao.update(dbCon, staff, new Food.LimitRemainingBuilder(builder.cancel.asFood(), limitRemaining));
			} catch (BusinessException ignored) {
				ignored.printStackTrace();
			}
		}
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
			//Get the detail to associated food.
			src.asFood().copyFrom(FoodDao.getById(dbCon, staff, src.getFoodId()));
			
			//Get the detail to food unit
			if(src.hasFoodUnit() && !src.asFood().isCurPrice()){
				try{
					src.setFoodUnit(FoodUnitDao.getById(dbCon, staff, src.getFoodUnit().getId()));
				}catch(BusinessException ignored){}
			}
			
			//Get the detail to normal tastes.
			if(src.hasNormalTaste()){
				//Get the detail to each taste.
				for(Taste t : src.getTasteGroup().getTastes()){
					t.copyFrom(TasteDao.getById(dbCon, staff, t.getTasteId()));
				}
				//Get the detail to each spec.
				if(src.getTasteGroup().hasSpec()){
					src.getTasteGroup().getSpec().copyFrom(TasteDao.getById(dbCon, staff, src.getTasteGroup().getSpec().getTasteId()));
				}
				src.getTasteGroup().refresh();
			}
			
			//Get the detail to combo
			if(src.hasCombo()){
				for(ComboOrderFood cof : src.getCombo()){
					//Get the details to combo food.
					cof.asComboFood().copyFrom(FoodDao.getComboByCond(dbCon, staff, new ExtraCond4Combo(src.asFood().getFoodId()).setChildId(cof.asComboFood().getFoodId())).get(0));
					//Get the details to food unit.
					if(cof.hasFoodUnit()){
						cof.setFoodUnit(FoodUnitDao.getById(dbCon, staff, cof.getFoodUnit().getId()));
					}
					//Get the details to normal tastes of combo
					if(cof.hasTasteGroup() && cof.getTasteGroup().hasNormalTaste()){
						//Get the details to each taste of combo.
						for(Taste t : cof.getTasteGroup().getTastes()){
							t.copyFrom(TasteDao.getById(dbCon, staff, t.getTasteId()));
						}
						//Get the detail to each spec of combo
						if(cof.getTasteGroup().hasSpec()){
							cof.getTasteGroup().getSpec().copyFrom(TasteDao.getById(dbCon, staff, cof.getTasteGroup().getSpec().getTasteId()));
						}
						cof.getTasteGroup().refresh();
					}
				}
			}
		}
	}
	
	public static class ArchiveResult{
		public final int ofAmount;
		public final int tgAmount;
		private final DBTbl fromTbl;
		private final DBTbl toTbl;
		
		public ArchiveResult(DateType archiveFrom, DateType archiveTo, int ofAmount, int tgAmount){
			this.fromTbl = new DBTbl(archiveFrom);
			this.toTbl = new DBTbl(archiveTo);
			this.ofAmount = ofAmount;
			this.tgAmount = tgAmount;
		}
		
		public int getAmount(){
			return this.ofAmount;
		}
		
		@Override
		public String toString(){
			final String sep = System.getProperty("line.separator");
			return ofAmount + " record(s) are moved from '" + fromTbl.orderFoodTbl + "' to '" + toTbl.orderFoodTbl + "'" + sep +
				   tgAmount + " record(s) are moved from '" + fromTbl.tgTbl + "' to '" + toTbl.tgTbl + "'";
		}
	}
	
	static ArchiveResult archive(DBCon dbCon, Staff staff, Order order, DateType archiveFrom, DateType archiveTo) throws SQLException{
		int ofAmount = 0, tgAmount = 0;
		DBTbl toTbl = new DBTbl(archiveTo);
		Map<Integer, Integer> tgMap = new HashMap<>();
		for(OrderFood of : getSingleDetail(dbCon, staff, new ExtraCond(archiveFrom).setOrder(order), null)){
			//Archive the taste group.
			int tgId = TasteGroup.EMPTY_TASTE_GROUP_ID;
			if(of.hasTasteGroup()){
				if(!tgMap.containsKey(of.getTasteGroup().getGroupId())){
					int tgId4Archive = TasteGroupDao.archive(dbCon, staff, of.getTasteGroup().getGroupId(), archiveFrom, archiveTo);
					tgMap.put(of.getTasteGroup().getGroupId(), tgId4Archive);
					tgAmount++;
				}
				tgId = tgMap.get(of.getTasteGroup().getGroupId()).intValue();
			}
			
			String sql;
			//Archive the order food records.
			sql = " INSERT INTO " + Params.dbName + "." + toTbl.orderFoodTbl +
				  " ( " + 
				  " `restaurant_id`, `order_id`, `operation`, `food_id`, `order_count`, `unit_price`, `commission`, `name`, `food_status`, " +
				  " `food_unit_id`, `food_unit`, `food_unit_price`, " +
				  " `plan_price`, " +
				  " `discount`, `taste_group_id`, " +
				  " `dept_id`, `kitchen_id`, " +
				  " `staff_id`, `waiter`, `order_date`, `is_temporary`, `is_paid`, `is_gift`, " +
				  " `cancel_reason_id`, `cancel_reason` " +
				  " ) " +
				  " VALUES " +
				  "(" +
				  staff.getRestaurantId() + ", " +
				  of.getOrderId() + ", " +
				  of.getOperation().getVal() + "," +
				  of.getFoodId() + ", " +
				  of.getCount() + ", " + 
				  of.getFoodPrice() + ", " + 
				  of.asFood().getCommission() + ", " +
				  "'" + of.getName().trim() + "', " + 
				  of.asFood().getStatus() + ", " +
				  (of.hasFoodUnit() ? of.getFoodUnit().getId() : " NULL ") + "," +
				  (of.hasFoodUnit() ? "'" + of.getFoodUnit().getUnit() + "'" : " NULL ") + "," +
				  (of.hasFoodUnit() ? of.getFoodUnit().getPrice() : " NULL ") + "," +
				  (of.getPlanPrice() >= 0 ? of.getPlanPrice() : " NULL ") + "," +
				  of.getDiscount() + ", " +
				  tgId + "," +
				  of.getKitchen().getDept().getId() + ", " +
				  of.getKitchen().getId() + ", " +
				  of.getStaffId() + ", " +
				  "'" + of.getWaiter() + "', " +
				  "'" +  DateUtil.format(of.getOrderDate(), DateUtil.Pattern.DATE_TIME) + "'," +
				  (of.isTemp() ? 1 : 0) + ", " +
				  (of.isRepaid() ? 1 : 0) + "," +
				  (of.isGift() ? 1 : 0) + "," +
				  (of.hasCancelReason() ? of.getCancelReason().getId() : "NULL") + "," +
				  (of.hasCancelReason() ? "'" + of.getCancelReason().getReason() + "'" : "NULL") +
				  " ) ";
			System.err.println(staff.getRestaurantId() + "..." + of.getName());
			if(dbCon.stmt.executeUpdate(sql) != 0){
				ofAmount++;
			}	
		}
		
		return new ArchiveResult(archiveFrom, archiveTo, ofAmount, tgAmount);
	}
	
	
}
