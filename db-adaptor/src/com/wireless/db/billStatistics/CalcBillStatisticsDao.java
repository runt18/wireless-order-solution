package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.DBTbl;
import com.wireless.db.Params;
import com.wireless.db.book.BookDao;
import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.orderMgr.PayTypeDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.shift.ShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.IncomeByBook;
import com.wireless.pojo.billStatistics.IncomeByCancel;
import com.wireless.pojo.billStatistics.IncomeByCoupon;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.IncomeByDiscount;
import com.wireless.pojo.billStatistics.IncomeByEachDay;
import com.wireless.pojo.billStatistics.IncomeByErase;
import com.wireless.pojo.billStatistics.IncomeByFood;
import com.wireless.pojo.billStatistics.IncomeByGift;
import com.wireless.pojo.billStatistics.IncomeByKitchen;
import com.wireless.pojo.billStatistics.IncomeByMemberPrice;
import com.wireless.pojo.billStatistics.IncomeByPay;
import com.wireless.pojo.billStatistics.IncomeByPay.PaymentIncome;
import com.wireless.pojo.billStatistics.IncomeByRepaid;
import com.wireless.pojo.billStatistics.IncomeByRound;
import com.wireless.pojo.billStatistics.IncomeByService;
import com.wireless.pojo.billStatistics.IncomeTrendByDept;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.billStatistics.commission.CommissionStatistics;
import com.wireless.pojo.book.Book;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.promotion.CouponOperation;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.SortedList;

public class CalcBillStatisticsDao {

	public static class ExtraCond implements Cloneable{
		public final DateType dateType;
		private final DBTbl dbTbl;

		private boolean isChain = false;		//是否连锁
		private boolean calcByDuty;				//是否按日结区间计算
		private Region.RegionId regionId;
		private Department.DeptId deptId;
		private int kitchenId;
		private String foodName;
		private DutyRange dutyRange;
		private HourRange hourRange;
		private int staffId;
		private int staffId4OrderFood;
		
		private Staff staff;
		
		public ExtraCond(DateType dateType){
			this.dateType = dateType;
			this.dbTbl = new DBTbl(dateType);
		}
		
		private ExtraCond setStaff(Staff staff){
			this.staff = staff;
			return this;
		}
		
		public ExtraCond setChain(boolean onOff){
			this.isChain = onOff;
			return this;
		}
		
		public boolean isChain(){
			return this.isChain;
		}
		
		public ExtraCond setCalcByDuty(boolean onOff){
			this.calcByDuty = onOff;
			return this;
		}
		
		public ExtraCond setRegion(Region.RegionId regionId){
			this.regionId = regionId;
			return this;
		}
		
		public ExtraCond setDept(Department.DeptId deptId){
			this.deptId = deptId;
			return this;
		}
		
		public ExtraCond setKitchen(Kitchen kitchen){
			this.kitchenId = kitchen.getId();
			return this;
		}
		
		public ExtraCond setKitchen(int kitchenId){
			this.kitchenId = kitchenId;
			return this;
		}
		
		public ExtraCond setFoodName(String foodName){
			this.foodName = foodName;
			return this;
		}
		
		public ExtraCond setDutyRange(DutyRange range){
			this.dutyRange = range;
			return this;
		}
		
		public DutyRange getDutyRange(){
			return this.dutyRange;
		}
		
		public ExtraCond setHourRange(HourRange range){
			this.hourRange = range;
			return this;
		}
		
		public ExtraCond setStaffId(int staffId){
			this.staffId = staffId;
			return this;
		}
		
		public ExtraCond setStaffId4OrderFood(int staffId){
			this.staffId4OrderFood = staffId;
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
			StringBuilder extraCond = new StringBuilder();
			if(regionId != null){
				extraCond.append(" AND O.region_id = " + regionId.getId());
			}
			if(deptId != null){
				extraCond.append(" AND OF.dept_id = " + deptId.getVal());
			}
			if(foodName != null){
				extraCond.append(" AND OF.name LIKE '%" + foodName + "%'");
			}
			if(dutyRange != null){
				DutyRange range;
				if(calcByDuty){
					try {
						range = DutyRangeDao.exec(staff, dutyRange);
					} catch (SQLException e) {
						e.printStackTrace();
						range = dutyRange;
					}
				}else{
					range = dutyRange;
				}
				if(range != null){
					extraCond.append(" AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'");
				}else{
					extraCond.append(" AND 0 ");
				}
			}
			if(hourRange != null){
				extraCond.append(" AND TIME(O.order_date) BETWEEN '" + hourRange.getOpeningFormat() + "' AND '" + hourRange.getEndingFormat() + "'");
			}
			if(staffId > 0){
				extraCond.append(" AND O.staff_id = " + staffId);
			}
			if(staffId4OrderFood > 0){
				extraCond.append(" AND OF.staff_id = " + staffId4OrderFood);
			}
			if(kitchenId > 0){
				extraCond.append(" AND OF.kitchen_id = " + kitchenId);
			}
			return extraCond.toString();
		}
	}
	
	
	/**
	 * Calculate the income by pay type.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the income by pay {@link IncomeByPayType}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static IncomeByPay calcIncomeByPayType(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{	
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByPayType(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the income by pay type.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the income by pay {@link IncomeByPayType}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static IncomeByPay calcIncomeByPayType(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{		
		
		IncomeByPay incomeByPay;
		
		if(extraCond.isChain()){
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the pay type income to group.
			incomeByPay = calcIncomeByPayType(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false));

			//Append the pay type income to each branch.
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				IncomeByPay branchIncome = calcIncomeByPayType(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false));
				for(IncomeByPay.PaymentIncome incomeByPayType : branchIncome.getPaymentIncomes()){
					incomeByPay.addIncome4Chain(incomeByPayType);
				}
			}
			
		}else{
			
			extraCond.setStaff(staff);
			
			String sql;
			//Calculate the order amount.
			sql = " SELECT COUNT(*) FROM " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " + 
				  " WHERE 1 = 1 " +
				  (extraCond != null ? extraCond.toString() : "") +
				  " AND O.restaurant_id = " + staff.getRestaurantId() +
				  //" AND O.order_date BETWEEN '" + extraCond.dutyRange.getOnDutyFormat() + "' AND '" + extraCond.dutyRange.getOffDutyFormat() + "'" +
				  " AND (O.status = " + Order.Status.PAID.getVal() + " OR " + " status = " + Order.Status.REPAID.getVal() + ")";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				incomeByPay = new IncomeByPay(dbCon.rs.getInt(1));
			}else{
				incomeByPay = new IncomeByPay(0);
			}
			dbCon.rs.close();
			
			//Calculate the single payment to each pay type.
			sql = " SELECT " +
				  " O.pay_type_id, IFNULL(PT.name, '其他') AS pay_type_name " +
				  " ,COUNT(*) AS amount " +
				  " ,ROUND(SUM(O.total_price), 2) AS total_income" + 
				  " ,ROUND(SUM(IF(O.pay_type_id <> " + PayType.MIXED.getId() + ",O.actual_price, 0)), 2) AS actual_income " +
				  " FROM " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
				  " LEFT JOIN " + Params.dbName + ".pay_type PT ON O.pay_type_id = PT.pay_type_id " +
				  " WHERE 1 = 1 " +
				  (extraCond != null ? extraCond.toString() : "") +
				  " AND O.restaurant_id = " + staff.getRestaurantId() + 
				  //" AND O.pay_type_id <> " + PayType.MIXED.getId() +
				  //" AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
				  " AND (O.status = " + Order.Status.PAID.getVal() + " OR " + " status = " + Order.Status.REPAID.getVal() + ")"  +
				  " GROUP BY O.pay_type_id ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				PayType payType = new PayType(dbCon.rs.getInt("pay_type_id"));
				payType.setName(dbCon.rs.getString("pay_type_name"));
				int amount = dbCon.rs.getInt("amount");
				float total = dbCon.rs.getFloat("total_income");
				float actual = dbCon.rs.getFloat("actual_income");
				incomeByPay.addPaymentIncome(new IncomeByPay.PaymentIncome(payType, amount, total, actual));
			}
			dbCon.rs.close();

			//Calculate the mixed payment income to each pay type.
			sql = " SELECT " +
				  " MP.pay_type_id, IFNULL(MAX(PT.name), '其他') AS pay_type_name, " +
				  " COUNT(*) AS amount, ROUND(SUM(MP.price), 2) AS total_income, ROUND(SUM(MP.price), 2) AS actual_income " +
				  " FROM " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
				  " JOIN " + Params.dbName + "." + extraCond.dbTbl.mixedTbl + " MP ON O.id = MP.order_id " +
				  " LEFT JOIN " + Params.dbName + ".pay_type PT ON MP.pay_type_id = PT.pay_type_id " +
				  " WHERE 1 = 1 " +
				  (extraCond != null ? extraCond.toString() : "") +
				  " AND O.restaurant_id = " + staff.getRestaurantId() + 
				  " AND O.pay_type_id = " + PayType.MIXED.getId() +
				  //" AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
				  " AND (O.status = " + Order.Status.PAID.getVal() + " OR " + " status = " + Order.Status.REPAID.getVal() + ")"  +
				  " GROUP BY MP.pay_type_id ";
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				PayType payType = new PayType(dbCon.rs.getInt("pay_type_id"));
				payType.setName(dbCon.rs.getString("pay_type_name"));
				//int amount = dbCon.rs.getInt("amount");
				float actual = dbCon.rs.getFloat("actual_income");
				incomeByPay.addPaymentIncome(new IncomeByPay.PaymentIncome(payType, 0, 0, actual));
			}
			dbCon.rs.close();
			
			//Append the designed and member payment type if NOT contained within the result.
			for(PayType payType : PayTypeDao.getByCond(dbCon, staff, new PayTypeDao.ExtraCond().addType(PayType.Type.DESIGNED).addType(PayType.Type.MEMBER))){
				incomeByPay.addPaymentIncome(new PaymentIncome(payType, 0, 0, 0));
			}
		}
		
		return incomeByPay;
	}
	
	/**
	 * Calculate the erase price according to extra condition
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to income by erase {@link IncomeByErase}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByErase calcErasePrice(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcErasePrice(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Calculate the customer amount according to extra condition
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the customer amount
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int calcCustomerAmount(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		
		String sql;
		
		//Get the total & amount to erase price
		sql = " SELECT " +
			  " SUM(custom_num) AS custom_amount " +
			  " FROM " +
			  Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
			  " WHERE 1 = 1 " +
			  (extraCond != null ? extraCond.setStaff(staff).toString() : "") +
			  " AND restaurant_id = " + staff.getRestaurantId();
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int customAmount = 0;
		if(dbCon.rs.next()){
			customAmount = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		return customAmount;
	}
	
	/**
	 * Calculate the erase price according to extra condition
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to income by erase {@link IncomeByErase}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByErase calcErasePrice(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		
		String sql;
		
		//Get the total & amount to erase price
		sql = " SELECT " +
			  " COUNT(*) AS amount, ROUND(SUM(erase_price), 2) AS total_erase " +
			  " FROM " +
			  Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
			  " WHERE 1 = 1 " +
			  (extraCond != null ? extraCond.setStaff(staff).toString() : "") +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  //" AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND erase_price > 0 ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByErase eraseIncome = new IncomeByErase();
		if(dbCon.rs.next()){
			eraseIncome.setEraseAmount(dbCon.rs.getInt("amount"));
			eraseIncome.setErasePrice(dbCon.rs.getFloat("total_erase"));
		}
		dbCon.rs.close();
		
		return eraseIncome;
	}
	
	/**
	 * Calculate the discount price according to extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extra condition
	 * @return the result to income by discount {@link IncomeByDiscount}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByDiscount calcDiscountPrice(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcDiscountPrice(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the discount price according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extra condition
	 * @return the result to income by discount {@link IncomeByDiscount}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByDiscount calcDiscountPrice(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		
		String sql;
		
		sql = " SELECT " +
			  " COUNT(*) AS amount, ROUND(SUM(discount_price), 2) AS total_discount " +
			  " FROM " +
			  Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
			  " WHERE 1 = 1 " +
			  (extraCond != null ? extraCond.toString() : "") +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  //" AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND discount_price > 0 ";
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByDiscount discountIncome = new IncomeByDiscount();
		if(dbCon.rs.next()){
			discountIncome.setDiscountAmount(dbCon.rs.getInt("amount"));
			discountIncome.setTotalDiscount(dbCon.rs.getFloat("total_discount"));
		}
		dbCon.rs.close();
		
		return discountIncome;
	}
	
	/**
	 * Calculate the gift price according to extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to income by gift {@link IncomeByGift}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByGift calcGiftPrice(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcGiftPrice(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the gift price according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to income by gift {@link IncomeByGift}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByGift calcGiftPrice(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		
		String sql;
		sql = " SELECT " +
		      " COUNT(*) AS amount, ROUND(SUM(gift_price), 2) AS total_gift " +
		      " FROM " +
		      Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
		      " WHERE 1 = 1 " +
		      (extraCond != null ? extraCond.setStaff(staff).toString() : "") +
		      " AND restaurant_id = " + staff.getRestaurantId() +
		      //" AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND gift_price > 0 ";
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByGift giftIncome = new IncomeByGift();
		if(dbCon.rs.next()){
			giftIncome.setGiftAmount(dbCon.rs.getInt("amount"));
			giftIncome.setTotalGift(dbCon.rs.getFloat("total_gift"));
		}
		dbCon.rs.close();
		
		return giftIncome;
	}
	
	/**
	 * Calculate the cancel price to specific duty range.
	 * @param staff
	 * 			the staff to perform this action
	 * @param queryType
	 * 			the query type
	 * @return the income by cancel pricee
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByCancel calcCancelPrice(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCancelPrice(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}	
	
	/**
	 * Calculate the cancel price to specific duty range.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param queryType
	 * 			the query type
	 * @return the income by cancel price
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByCancel calcCancelPrice(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		
		String sql;
		
		sql = " SELECT " +
		      " COUNT(*) AS amount, ROUND(SUM(cancel_price), 2) AS total_cancel " +
		      " FROM " +
		      Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
		      " WHERE 1 = 1 " +
		      (extraCond != null ? extraCond.setStaff(staff).toString() : "") +
		      " AND restaurant_id = " + staff.getRestaurantId() +
		      //" AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND cancel_price > 0 ";
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByCancel cancelIncome = new IncomeByCancel();
		if(dbCon.rs.next()){
			cancelIncome.setCancelAmount(dbCon.rs.getInt("amount"));
			cancelIncome.setTotalCancel(dbCon.rs.getFloat("total_cancel"));
		}
		dbCon.rs.close();
		
		return cancelIncome;
	}

	/**
	 * Calculate the coupon price to specific duty range.
	 * @param staff
	 * 			the staff to perform this action
	 * @param queryType 
	 * 			the query type
	 * @return the income by coupon price
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByCoupon calcCouponPrice(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCouponPrice(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the coupon price to specific duty range.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param queryType 
	 * 			the query type
	 * @return the income by coupon price
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByCoupon calcCouponPrice(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		
		String sql;
		
		sql = " SELECT COUNT(*) AS amount, SUM(TMP.coupon_price) AS total_coupon FROM " +
			  " ( " +
				  " SELECT O.coupon_price " +
			      " FROM " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
			      " JOIN " + Params.dbName + ".coupon_operation CO ON O.id = CO.associate_id AND CO.operate = " + CouponOperation.Operate.ORDER_USE.getVal() + 
			      " WHERE 1 = 1 " +
			      (extraCond != null ? extraCond.setStaff(staff).toString() : "") +
			      " AND O.restaurant_id = " + staff.getRestaurantId() +
			      //" AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			      " GROUP BY O.id " +
			   " ) AS TMP ";
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByCoupon couponIncome = new IncomeByCoupon();
		if(dbCon.rs.next()){
			couponIncome.setCouponAmount(dbCon.rs.getInt("amount"));
			couponIncome.setTotalCoupon(dbCon.rs.getFloat("total_coupon"));
		}
		dbCon.rs.close();
		
		return couponIncome;
	}
	
	/**
	 * Calculate the repaid price according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to income by repaid {@link IncomeByRepaid}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByRepaid calcRepaidPrice(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcRepaidPrice(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the repaid price according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to income by repaid {@link IncomeByRepaid}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByRepaid calcRepaidPrice(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		
		String sql;
		sql = " SELECT " +
		      " COUNT(*) AS amount, ROUND(SUM(repaid_price), 2) AS total_repaid " +
		      " FROM " +
		      Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
		      " WHERE 1 = 1 " +
		      " AND restaurant_id = " + staff.getRestaurantId() +
		      //" AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND status = " + Order.Status.REPAID.getVal() +
			  (extraCond != null ? extraCond.setStaff(staff).toString() : "");
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByRepaid repaidIncome = new IncomeByRepaid();
		if(dbCon.rs.next()){
			repaidIncome.setRepaidAmount(dbCon.rs.getInt("amount"));
			repaidIncome.setTotalRepaid(dbCon.rs.getFloat("total_repaid"));
		}
		dbCon.rs.close();
		
		return repaidIncome;
	}
	
	/**
	 * Calculate the rould price according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to income by repaid {@link IncomeByRound}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByRound calcRoundPrice(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		
		String sql;
		sql = " SELECT " +
		      " ROUND(SUM(IFNULL(O.actual_price, 0) + IFNULL(O.erase_price, 0) + IFNULL(O.coupon_price, 0) - IFNULL(O.total_price, 0)), 2) AS round_price " +
		      " FROM " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
		      " WHERE 1 = 1 " +
		      " AND O.restaurant_id = " + staff.getRestaurantId() +
		      //" AND O.order_date BETWEEN '" + extraCond.dutyRange.getOnDutyFormat() + "' AND '" + extraCond.dutyRange.getOffDutyFormat() + "'" +
		      (extraCond != null ? extraCond.setStaff(staff).toString() : "") +
		      " GROUP BY O.id " +
		      " HAVING round_price <> 0 ";
		
		sql = " SELECT COUNT(*) AS amount, SUM(round_price) AS price FROM ( " + sql + " ) AS TMP ";
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByRound roundIncome = new IncomeByRound();
		if(dbCon.rs.next()){
			roundIncome.setAmount(dbCon.rs.getInt("amount"));
			roundIncome.setTotal(dbCon.rs.getFloat("price"));
		}
		dbCon.rs.close();
		
		return roundIncome;
	}
	
	/**
	 * Calculate the repaid price according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to income by repaid {@link IncomeByRepaid}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByMemberPrice calcMemberPrice(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		
		String sql;
		sql = " SELECT " +
		      " COUNT(*) AS amount, ROUND(SUM(pure_price) - SUM(actual_price), 2) AS price " +
		      " FROM " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
		      " JOIN " + Params.dbName + "." + extraCond.dbTbl.moTbl + " MO ON O.id = MO.order_id" +
		      " WHERE 1 = 1 " +
		      " AND O.restaurant_id = " + staff.getRestaurantId() +
		      " AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'";
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByMemberPrice memberPriceIncome = new IncomeByMemberPrice();
		if(dbCon.rs.next()){
			memberPriceIncome.setMemberPriceAmount(dbCon.rs.getInt("amount"));
			memberPriceIncome.setMemberPrice(dbCon.rs.getFloat("price"));
		}
		dbCon.rs.close();
		
		return memberPriceIncome;
	}
	
	/**
	 * Calculate the service income.
	 * @param staff
	 * 			the staff to perform this action
	 * @param queryType
	 * 			the date type
	 * @return the service income {@link IncomeByService}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByService calcServicePrice(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcServicePrice(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the service income.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param queryType
	 * 			the date type
	 * @return the service income {@link IncomeByService}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByService calcServicePrice(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		
		String sql;
		
		sql = " SELECT " +
			  " COUNT(*) AS amount, ROUND(SUM(total_price * service_rate), 2) AS total_service " +
			  " FROM " +
			  Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
			  " WHERE 1 = 1 " +
			  (extraCond != null ? extraCond.setStaff(staff).toString() : "") +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  //" AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND service_rate > 0 ";
				
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByService serviceIncome = new IncomeByService();
		if(dbCon.rs.next()){
			serviceIncome.setServiceAmount(dbCon.rs.getInt("amount"));
			serviceIncome.setTotalService(dbCon.rs.getFloat("total_service"));
		}
		dbCon.rs.close();
		
		return serviceIncome;
	}
	
	static String makeSql4CalcFood(Staff staff, ExtraCond extraCond){
		
		return (
			   " SELECT " +
			   " OF.order_id, OF.food_id, " +
			   " MAX(O.restaurant_id) AS restaurant_id, " +
 			   " MAX(OF.food_status) AS food_status, MAX(OF.name) AS food_name,	MAX(OF.dept_id) AS dept_id, MAX(OF.kitchen_id) AS kitchen_id, " +
			   " SUM(order_count) AS food_amount, " +
			   " CASE WHEN OF.is_gift = 1 THEN ($(unit_price) + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * SUM(OF.order_count) ELSE 0 END AS food_gift," +
			   " CASE WHEN OF.is_gift = 1 THEN SUM(OF.order_count) ELSE 0 END AS food_gift_amount," +
			   " ($(unit_price) + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * (1 - discount) * SUM(OF.order_count) AS food_discount, " +
			   " CASE WHEN ((OF.is_gift = 0) AND (OF.food_status & " + Food.WEIGHT + ") = 0) THEN ($(unit_price) + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * SUM(OF.order_count) " +
					" WHEN ((OF.is_gift = 0) AND (OF.food_status & " + Food.WEIGHT + ") <> 0) THEN ($(unit_price) * SUM(OF.order_count) + (IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0))) * discount " +
				  	" ELSE 0 " +
				  	" END AS food_income, " +
			   " CASE WHEN ((OF.is_gift = 0) AND (OF.food_status & " + Food.WEIGHT + ") = 0) THEN (IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * SUM(OF.order_count) " +
					" WHEN ((OF.is_gift = 0) AND (OF.food_status & " + Food.WEIGHT + ") <> 0) THEN (IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount " +
				  	" ELSE 0 " +
				  	" END AS taste_income " +
			   " FROM " + Params.dbName + "." + extraCond.dbTbl.orderFoodTbl + " OF " + 
			   " JOIN " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " O ON 1 = 1 " + 
			   " AND OF.order_id = O.id " + 
			   " AND O.restaurant_id = " + staff.getRestaurantId() + 
			   " AND O.status <> " + Order.Status.UNPAID.getVal() +
			   " JOIN " + Params.dbName + "." + extraCond.dbTbl.tgTbl + " TG " + " ON OF.taste_group_id = TG.taste_group_id " +
			   " WHERE 1 = 1 " +
			   (extraCond == null ? "" : extraCond.setStaff(staff).toString()) +
			   //" AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			   " GROUP BY OF.order_id, OF.food_id, OF.taste_group_id, OF.food_unit_id, OF.is_gift " +
			   " HAVING food_amount > 0 "
			   ).replace("$(unit_price)", "IFNULL(OF.plan_price, IFNULL(OF.food_unit_price, OF.unit_price))");
	}
	
	
	/**
	 * Calculate the income to each department according to extra condition.
	 * @param dbCon	
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result {@link IncomeByDept} list
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<IncomeByDept> calcIncomeByDept(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByDept(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the income to each department according to extra condition.
	 * @param dbCon	
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result {@link IncomeByDept} list
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<IncomeByDept> calcIncomeByDept(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		
		String sql;
		
		//Get the gift, discount & total to each department during this period.
		sql = " SELECT " +
			  " D.dept_id, D.restaurant_id, D.type, D.name AS dept_name, D.display_id, " +
			  " ROUND(SUM(food_gift), 2) AS dept_gift, " +
			  " ROUND(SUM(food_discount), 2) AS dept_discount, " +
			  " ROUND(SUM(food_income), 2) AS dept_income " +
			  " FROM (" + 
			  	makeSql4CalcFood(staff, extraCond) +
			  " ) AS TMP " +
		      " JOIN " + Params.dbName + ".department D " + " ON TMP.dept_id = D.dept_id " + " AND D.restaurant_id = " + staff.getRestaurantId() +
			  " GROUP BY TMP.dept_id " +
			  " ORDER BY D.display_id ASC ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<IncomeByDept> deptIncomes = new ArrayList<IncomeByDept>();
		while(dbCon.rs.next()){
			Department dept = new Department(dbCon.rs.getString("dept_name"),
				    						 dbCon.rs.getShort("dept_id"),
				    						 dbCon.rs.getInt("restaurant_id"),
				    						 Department.Type.valueOf(dbCon.rs.getShort("type")),
				    						 dbCon.rs.getInt("display_id"));
			
			if(dept.isIdle()){
				dept.setName(dept.getName().isEmpty() ? "已删除部门" : dept.getName() + "(已删除)");
			}
			deptIncomes.add(new IncomeByDept(dept,
										     dbCon.rs.getFloat("dept_gift"),
										     dbCon.rs.getFloat("dept_discount"),
										     dbCon.rs.getFloat("dept_income")));
		}
		dbCon.rs.close();
	
		return deptIncomes;
	}
	
	/**
	 * Calculate the income to each kitchen according to extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link IncomeByKitchen}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<IncomeByKitchen> calcIncomeByKitchen(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByKitchen(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}

	
	/**
	 * Calculate the income to each kitchen according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link IncomeByKitchen}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<IncomeByKitchen> calcIncomeByKitchen(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		
		String sql;
		
		sql = " SELECT " +
			  " K.kitchen_id, K.display_id AS kitchen_display_id, K.name AS kitchen_name, K.type AS kitchen_type, " +
			  " D.dept_id, D.type AS dept_type, D.name AS dept_name, D.display_id AS dept_display_id, " +
			  " ROUND(SUM(food_gift), 2) AS kitchen_gift, ROUND(SUM(food_discount), 2) AS kitchen_discount, ROUND(SUM(food_income), 2) AS kitchen_income " +
			  " FROM ( " +
			  makeSql4CalcFood(staff, extraCond) +
			  " ) AS TMP " +
			  " JOIN " + Params.dbName + ".kitchen K " + " ON TMP.kitchen_id = K.kitchen_id " + 
			  " JOIN " + Params.dbName + ".department D " + " ON TMP.dept_id = D.dept_id AND D.restaurant_id = " + staff.getRestaurantId() +
			  " GROUP BY TMP.kitchen_id " +
			  " ORDER BY K.display_id ASC ";

		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<IncomeByKitchen> kitchenIncomes = new ArrayList<IncomeByKitchen>();
		while(dbCon.rs.next()){
			Kitchen k = new Kitchen(dbCon.rs.getInt("kitchen_id"));
			k.setName(dbCon.rs.getString("kitchen_name"));
			k.setRestaurantId(staff.getRestaurantId());
			k.setAllowTemp(false);
			k.setType(dbCon.rs.getShort("kitchen_type"));
			k.setDept(new Department(dbCon.rs.getString("dept_name"),
									 dbCon.rs.getShort("dept_id"),
									 staff.getRestaurantId(),
									 Department.Type.valueOf(dbCon.rs.getShort("dept_type")),
									 dbCon.rs.getInt("dept_display_id")));
			
			if(k.getType() == Kitchen.Type.IDLE){
				k.setName(k.getName().isEmpty() ? "已删除厨房" : k.getName() + "(已删除)");
			}
			
			kitchenIncomes.add(new IncomeByKitchen(k, 
												   dbCon.rs.getFloat("kitchen_gift"),
												   dbCon.rs.getFloat("kitchen_discount"),
												   dbCon.rs.getFloat("kitchen_income")));
		}
		
		dbCon.rs.close();
		
		return kitchenIncomes;
	}
	
	/**
	 * Calculate income by each food according to extra condition {@link ExtraCond}
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link IncomeByFood}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<IncomeByFood> calcIncomeByFood(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByFood(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate income by each food according to extra condition {@link ExtraCond}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link IncomeByFood}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<IncomeByFood> calcIncomeByFood(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		
		String sql;
		
		//Get the gift, discount & total to each food during this period.
		sql = " SELECT " +
			  " TMP.food_id, " + 
			  " MAX(R.restaurant_name) AS restaurant_name, " +
			  " MAX(IFNULL(TMP2.unit_cost, 0)) AS unit_cost, " + 
			  " ROUND(SUM(TMP.food_amount * IFNULL(TMP2.unit_cost, 0)), 2) AS food_cost, " +
			  " MAX(TMP.food_name) AS food_name, MAX(TMP.food_status) AS food_status, " + 
			  " MAX(TMP.dept_id) AS dept_id, MAX(TMP.kitchen_id) AS kitchen_id, " +
			  " SUM(TMP.food_amount) AS food_amount, " +
			  " ROUND(SUM(TMP.food_gift), 2) AS food_gift, " +
			  " ROUND(SUM(TMP.food_gift_amount), 2) AS food_gift_amount, " +
			  " ROUND(SUM(TMP.food_discount), 2) AS food_discount, " +
			  " ROUND(SUM(TMP.food_income), 2) AS food_income, " +
			  " ROUND(SUM(TMP.taste_income), 2) AS taste_income, " +
			  " ROUND(SUM(TMP.taste_income), 2) AS taste_income " +
			  " FROM (" +
			  makeSql4CalcFood(staff, extraCond) +
			  " ) AS TMP " +
			  " JOIN " + Params.dbName + ".restaurant R ON TMP.restaurant_id = R.id " +
			  " LEFT JOIN " +
			  	  " ( SELECT FM.food_id, M.price AS unit_cost" + 
			  	   	 " FROM " + Params.dbName + ".food_material FM " +
			  	     " JOIN " + Params.dbName + ".material M ON FM.material_id = M.material_id " +
			  	     " JOIN " + Params.dbName + ".material_cate MC ON M.cate_id = MC.cate_id AND MC.type = " + MaterialCate.Type.GOOD.getValue() +
			  	     " WHERE FM.restaurant_id = " + staff.getRestaurantId() +
			  	  " ) AS TMP2 ON TMP.food_id = TMP2.food_id " +
			  " GROUP BY TMP.food_id " +
			  " ORDER BY TMP.food_id ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<IncomeByFood> foodIncomes = new ArrayList<IncomeByFood>();
		while(dbCon.rs.next()){

			Department dept = new Department(dbCon.rs.getShort("dept_id"));
			dept.setRestaurantId(staff.getRestaurantId());

			
			Kitchen kitchen = new Kitchen(dbCon.rs.getInt("kitchen_id"));
			kitchen.setRestaurantId(staff.getRestaurantId());
			kitchen.setDept(dept);

			Food food = new Food(dbCon.rs.getInt("food_id"));
			food.setName(dbCon.rs.getString("food_name"));
			food.setRestaurantId(staff.getRestaurantId());
			food.setStatus(dbCon.rs.getShort("food_status"));
			food.setKitchen(kitchen);
			
			foodIncomes.add(new IncomeByFood(food,
											 dbCon.rs.getString("restaurant_name"),
											 dbCon.rs.getFloat("food_gift"),
											 dbCon.rs.getFloat("food_gift_amount"),
											 dbCon.rs.getFloat("food_discount"),
											 dbCon.rs.getFloat("food_income"),
											 dbCon.rs.getFloat("taste_income"),
											 dbCon.rs.getFloat("food_amount"),
											 dbCon.rs.getFloat("unit_cost"),
											 dbCon.rs.getFloat("food_cost")
											 ));
		}
		
		dbCon.rs.close();
		
		return foodIncomes;
	}

	 
	/**
	 * Calculate the income by book.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the range to calculate
	 * @return the income by book 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByBook calcIncomeByBook(DBCon dbCon, Staff staff, DutyRange range) throws SQLException{
		float income = 0;
		int amount = 0;
		for(Book book : BookDao.getByCond(dbCon, staff, new BookDao.ExtraCond().setConfirmRange(range).addStatus(Book.Status.CONFIRMED).addStatus(Book.Status.SEAT))){
			if(book.getMoney() != 0){
				income += book.getMoney();
				amount++;
			}
		}
		return new IncomeByBook(income, amount);
	}
	
	public static List<CommissionStatistics> calcCommissionTotal(Staff staff, DutyRange range, DateType queryType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCommissionTotal(dbCon, staff, range, null, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get 
	 * @param staff
	 * @param range
	 * @param deptId
	 * @param queryType
	 * @return
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			
	 */
	public static List<CommissionStatistics> calcCommissionTotalByDept(Staff staff, DutyRange range, int deptId, DateType queryType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCommissionTotal(dbCon, staff, range, " AND OFH.dept_id = " + deptId, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get commission total by extra condition.
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param extraCond
	 * 			the extra condition
	 * @param dateType
	 * @return
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if the query type is invalid
	 */
	public static List<CommissionStatistics> calcCommissionTotal(DBCon dbCon, Staff staff, DutyRange range, String extraCond, DateType dateType) throws SQLException, BusinessException{
		DBTbl dbTbl = new DBTbl(dateType);
		String sql;
		sql = " SELECT ROUND(SUM(totalPrice), 2) AS totalPrice, ROUND(SUM(commission), 2) AS commission, SF.name AS waiter,  MAX(TOTAL.staff_id) AS staff_id  FROM ( SELECT " +
			  " ROUND(SUM(OFH.unit_price * OFH.order_count), 2) AS totalPrice, " +
			  " ROUND(SUM(OFH.commission * OFH.order_count), 2) AS commission, MAX(OFH.waiter) AS waiter, " +
			  " OFH.staff_id " +
			  " FROM " + Params.dbName + "." + dbTbl.orderFoodTbl + " OFH " +
			  " JOIN " + Params.dbName + "." + dbTbl.orderTbl + " OH ON 1 = 1 " +
			  " AND OH.id = OFH.order_id " +
			  " AND OH.restaurant_id = " + staff.getRestaurantId() + 
			  " AND OH.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " WHERE (OFH.food_status & " + Food.COMMISSION + ") <> 0 " +
			  " AND OFH.commission <> 0 " +
			  (extraCond != null ? extraCond : "") +
			  " GROUP BY OFH.order_id) AS TOTAL" +
			  " JOIN " + Params.dbName + ".staff SF ON SF.staff_id = TOTAL.staff_id " +
			  " GROUP BY TOTAL.staff_id";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<CommissionStatistics> result = new ArrayList<CommissionStatistics>();
		while(dbCon.rs.next()){
			CommissionStatistics c = new CommissionStatistics();
			c.setWaiter(dbCon.rs.getString("waiter"));
			c.setTotalPrice(dbCon.rs.getFloat("totalPrice"));
			c.setCommission(dbCon.rs.getFloat("commission"));
			result.add(c);
		}
		dbCon.rs.close();
		
		return Collections.unmodifiableList(result);
	}

	/**
	 * Calculate the income trend according to specific duty range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition 
	 * @return the result to income trend {@link IncomeTrendByDept}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 * @throws BusinessException 
	 */
	public static List<IncomeTrendByDept> calcIncomeTrendByDept(Staff staff, ExtraCond extraCond) throws SQLException, ParseException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeTrendByDept(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Calculate the income trend according to specific duty range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition 
	 * @return the result to income trend {@link IncomeTrendByDept}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 * @throws BusinessException 
	 */
	public static List<IncomeTrendByDept> calcIncomeTrendByDept(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, ParseException, BusinessException{
		
		if(extraCond.isChain){
			
			final Map<DutyRange, IncomeTrendByDept> chainResult = new HashMap<>();
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the department trend income to the group.
			for(IncomeTrendByDept groupIncome : calcIncomeTrendByDept(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false))){
				chainResult.put(groupIncome.getRange(), groupIncome);
			}
			
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				//Append the department trend income to each branch.
				for(IncomeTrendByDept branchIncome : calcIncomeTrendByDept(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false))){
					if(chainResult.containsKey(branchIncome.getRange())){
						IncomeTrendByDept deptTrendIncome = chainResult.get(branchIncome.getRange());
						deptTrendIncome.getDeptIncome().setDiscount(deptTrendIncome.getDeptIncome().getDiscount() + branchIncome.getDeptIncome().getDiscount());
						deptTrendIncome.getDeptIncome().setGift(deptTrendIncome.getDeptIncome().getGift() + branchIncome.getDeptIncome().getGift());
						deptTrendIncome.getDeptIncome().setIncome(deptTrendIncome.getDeptIncome().getIncome() + branchIncome.getDeptIncome().getIncome());

					}else{
						chainResult.put(branchIncome.getRange(), branchIncome);
					}
				}
			}
			
			return SortedList.newInstance(chainResult.values());
			
		}else{
			List<IncomeTrendByDept> result = new ArrayList<IncomeTrendByDept>();
			
			Calendar c = Calendar.getInstance();
			Date dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(extraCond.dutyRange.getOnDutyFormat());
			Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(extraCond.dutyRange.getOffDutyFormat());
			c.setTime(dateBegin);
			while (dateBegin.compareTo(dateEnd) <= 0) {
				c.add(Calendar.DATE, 1);
				
				DutyRange rangeByEachDay = new DutyRange(dateBegin.getTime(), c.getTime().getTime());
				List<IncomeByDept> deptIncomes = calcIncomeByDept(dbCon, staff, ((ExtraCond)extraCond.clone()).setDutyRange(rangeByEachDay).setCalcByDuty(true));
				if(deptIncomes.isEmpty()){
					result.add(new IncomeTrendByDept(rangeByEachDay, new IncomeByDept()));
				}else{
					result.add(new IncomeTrendByDept(rangeByEachDay, deptIncomes.get(0)));
				}
				
				dateBegin = c.getTime();
			}
			
			return result;
			
		}
		
	}
	
	/**
	 * Get income to each day during on & off duty.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the income by each during on & off duty
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the on or off duty string
	 * @throws BusinessException 
	 */
	public static List<IncomeByEachDay> calcIncomeByEachDay(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, ParseException, BusinessException{
		
		
		if(extraCond.isChain()){
			//Group by the date in case of chain.
			//key : value - date : incomeByEachDay
			final Map<String, IncomeByEachDay> chainResult = new HashMap<>();
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			for(IncomeByEachDay groupIncome : calcIncomeByEachDay(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false))){
				chainResult.put(groupIncome.getDate(), groupIncome);
			}
			
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				for(IncomeByEachDay branchIncome : calcIncomeByEachDay(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false))){
					if(chainResult.containsKey(branchIncome.getDate())){
						IncomeByEachDay incomeByEachDay = chainResult.get(branchIncome.getDate());
						//Append the branch income.
						incomeByEachDay.append(branchIncome);
					}else{
						chainResult.put(branchIncome.getDate(), branchIncome);
					}
				}
			}
			
			return SortedList.newInstance(chainResult.values());
		}else{
			final List<IncomeByEachDay> result = new ArrayList<IncomeByEachDay>();

			Calendar c = Calendar.getInstance();
			Date dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(extraCond.dutyRange.getOnDutyFormat());
			Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(extraCond.dutyRange.getOffDutyFormat());
			c.setTime(dateBegin);
			while (dateBegin.compareTo(dateEnd) <= 0) {
				c.add(Calendar.DATE, 1);
				
				DutyRange range = DutyRangeDao.exec(dbCon, staff, 
													DateUtil.format(dateBegin, DateUtil.Pattern.DATE_TIME), 
													DateUtil.format(c.getTime(), DateUtil.Pattern.DATE_TIME));
				
				IncomeByEachDay income = new IncomeByEachDay(DateUtil.format(dateBegin, DateUtil.Pattern.DATE));
				if(range != null){
					
					extraCond = ((ExtraCond)extraCond.clone()).setDutyRange(range).setCalcByDuty(false);
					
					//Calculate the customer amount
					income.setCustomerAmount(calcCustomerAmount(dbCon, staff, extraCond));
					
					//Calculate the income by pay
					income.setIncomeByPay(calcIncomeByPayType(dbCon, staff, extraCond));
					
					//Calculate the total & amount to erase price
					income.setIncomeByErase(calcErasePrice(dbCon, staff, extraCond));
					
					//Get the total & amount to discount price
					income.setIncomeByDiscount(calcDiscountPrice(dbCon, staff, extraCond));
		
					//Get the total & amount to gift price
					income.setIncomeByGift(calcGiftPrice(dbCon, staff, extraCond));
					
					//Get the total & amount to cancel price
					income.setIncomeByCancel(calcCancelPrice(dbCon, staff, extraCond));
					
					//Get the total & amount to coupon price
					income.setIncomeByCoupon(calcCouponPrice(dbCon, staff, extraCond));
					
					//Get the total & amount to repaid order
					income.setIncomeByRepaid(calcRepaidPrice(dbCon, staff, extraCond));
					
					//Get the total & amount to order with service
					income.setIncomeByService(calcServicePrice(dbCon, staff, extraCond));
					
					//Get the charge income by both cash and credit card
					income.setIncomeByCharge(CalcMemberStatisticsDao.calcIncomeByCharge(dbCon, staff, new MemberOperationDao.ExtraCond(DateType.HISTORY).setOperateDate(range).setStaff(extraCond.staffId)));
					
				}
				result.add(income);
				
				dateBegin = c.getTime();
			}
			
			return result;

		}
		
	}

	/**
	 * Get income to each day during on & off duty.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the income by each during on & off duty
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the on or off duty string
	 * @throws BusinessException 
	 */
	public static List<IncomeByEachDay> calcIncomeByEachDay(Staff staff, ExtraCond extraCond) throws SQLException, ParseException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByEachDay(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * calculate the sales income (营业统计) according to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to sales income {@link ShiftDetail}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the branches does NOT exist in case of chain
	 */
	public static ShiftDetail calcSalesIncome(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcSalesIncome(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * calculate the sales income (营业统计) according to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to sales income {@link ShiftDetail}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the branches does NOT exist in case of chain
	 */
	public static ShiftDetail calcSalesIncome(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		final ShiftDetail result;
		
		if(extraCond.isChain()){
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the sales income to the group.
			result = calcSalesIncome(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false));
			//Append the sales income to each branch.
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				result.append(calcSalesIncome(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false)));
			}
			
		}else{
			final DutyRange dutyRange = DutyRangeDao.exec(dbCon, staff, extraCond.dutyRange);
			if(dutyRange == null){
				//result = ShiftDao.getByRange(dbCon, staff, extraCond);
				return new ShiftDetail(extraCond.dutyRange);
			}else{
				result = ShiftDao.getByRange(dbCon, staff, ((ExtraCond)extraCond.clone()).setDutyRange(dutyRange).setCalcByDuty(false));
			}
		}
		
		return result;

	}
	
}
