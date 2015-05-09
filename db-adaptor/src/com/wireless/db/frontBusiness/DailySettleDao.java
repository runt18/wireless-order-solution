package com.wireless.db.frontBusiness;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.orderMgr.MixedPaymentDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.OrderFoodDao;
import com.wireless.db.orderMgr.TasteGroupDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.shift.PaymentDao;
import com.wireless.db.shift.ShiftDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.AuditBuilder;
import com.wireless.pojo.stockMgr.StockActionDetail;

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
		
		private int elapsedTime;			//日结消耗的时间
		private int totalShift;				//当日交班的记录数
		private DutyRange range;
		
		private OrderDao.ArchiveResult orderArchive;
		private OrderFoodDao.ArchiveResult orderFoodArchive;
		private TasteGroupDao.ArchiveResult tgArchive;
		private MixedPaymentDao.ArchiveResult mixedArchive;
		
		Result(){
			
		}
		
		public int getElapsedTime(){
			return this.elapsedTime;
		}
		
		void setElapsedTime(int elapsedTime){
			this.elapsedTime = elapsedTime;
		}
		
		public int getTotalShift() {
			return totalShift;
		}
		
		void setTotalShift(int totalShift) {
			this.totalShift = totalShift;
		}
		
		void setRange(DutyRange range){
			this.range = range;
		}
		
		public DutyRange getRange(){
			return this.range;
		}
		
		@Override
		public String toString(){
			
			final String sep = System.getProperty("line.separator");

			StringBuilder resultInfo = new StringBuilder();
			resultInfo.append("info : " + orderArchive.getAmount() + " record(s) are moved from \"order\" to \"order_history\"").append(sep);
			resultInfo.append("info : " + mixedArchive.getAmount() + " record(s) are moved from \"mixed_payment\" to \"mixed_payment_history\"").append(sep);
			resultInfo.append("info : " + orderFoodArchive.getAmount() + " record(s) are moved from \"order_food\" to \"order_food_history\"").append(sep);
			resultInfo.append("info : " + getTotalShift() + " record(s) are moved from \"shift\" to \"shift_history\"").append(sep);
			resultInfo.append("info : " + 
							  "maxium order id : " + orderArchive.getMaxId() + ", " +
							  "maxium order food id : " + orderFoodArchive.getMaxId()).append(sep);
			resultInfo.append("info : " +
							  "maxium taste group id : " + tgArchive.getTgMaxId()).append(sep);
			resultInfo.append("info : The record movement takes " + getElapsedTime() + " sec.");
			
			return resultInfo.toString();
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
		
		long beginTime = System.currentTimeMillis();
		
		String sql;
		
		List<Staff> staffs = new ArrayList<Staff>();
		
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
			Staff s = new Staff();
			s.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			s.setName("system");
			staffs.add(s);
		}
		dbCon.rs.close();		
		
		int totalOrder = 0, totalMixedPayment = 0, totalOrderFood = 0, totalShift = 0, maxOrderId = 0, maxOrderFoodId = 0;
		int maxTgId = 0, maxNTgId = 0;
		for(Staff staff : staffs){			
			Result eachResult = exec(dbCon, staff, SettleType.AUTO_MATION);
			
			totalOrder += eachResult.orderArchive != null ? eachResult.orderArchive.getAmount() : 0;
			totalMixedPayment += eachResult.mixedArchive.getAmount();
			maxOrderId = eachResult.orderArchive != null ? eachResult.orderArchive.getMaxId() : 0; 
			totalOrderFood += eachResult.orderFoodArchive != null ? eachResult.orderFoodArchive.getAmount() : 0;
			maxOrderFoodId = eachResult.orderFoodArchive != null ? eachResult.orderFoodArchive.getMaxId() : 0;
			maxTgId = eachResult.tgArchive != null ? eachResult.tgArchive.getTgMaxId() : 0;
			maxNTgId = eachResult.tgArchive != null ? eachResult.tgArchive.getNTgMaxId() : 0;
			totalShift += eachResult.getTotalShift();
		}
		
		Result result = new Result();
		result.orderArchive = new OrderDao.ArchiveResult(maxOrderId, totalOrder);
		result.orderFoodArchive = new OrderFoodDao.ArchiveResult(maxOrderFoodId, totalOrderFood);
		result.tgArchive = new TasteGroupDao.ArchiveResult(0, 0, maxTgId, maxNTgId);
		result.mixedArchive = new MixedPaymentDao.ArchiveResult(totalMixedPayment);
		result.setTotalShift(totalShift);
		result.setElapsedTime((int)(System.currentTimeMillis() - beginTime) / 1000);
		
		return result;
	}
	
	/**
	 * Perform to daily settle according to a terminal.
	 * @param staff
	 * 			  the staff to perform this action
	 * @return the result to daily settle
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @throws BusinessException
	 */
	public static Result exec(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, staff, SettleType.MANUAL);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to daily settle according to a terminal.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff with both user name and restaurant id
	 * @param type
	 * 			  indicates whether the daily settle is manual or automation
	 * @return the result to daily settle
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @throws BusinessException
	 */
	public static Result exec(DBCon dbCon, Staff staff, SettleType type) throws SQLException, BusinessException{
		Result result = new Result();
		
		//Perform food material consumption
		List<StockAction.AuditBuilder> auditBuilders = new ArrayList<StockAction.AuditBuilder>();
		
		//生成每个部门的商品和原料出库消耗单
		for(Department dept : DepartmentDao.getByType(dbCon, staff, Department.Type.NORMAL)){
			
			for(MaterialCate.Type cateType : MaterialCate.Type.values()){
				
				StockAction.InsertBuilder builder = StockAction.InsertBuilder.newUseUp(staff.getRestaurantId(), dept, cateType);
				
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
					  " AND O.restaurant_id = " + staff.getRestaurantId() +
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
					auditBuilders.add(StockAction.AuditBuilder.newStockActionAudit(StockActionDao.insertStockAction(dbCon, staff, builder)));
				}
			}
			
		}
		
		//如果有盘点任务正在进行，则不审核出入库消耗单
		if(!StockActionDao.isStockTakeChecking(dbCon, staff)){
			for(AuditBuilder auditBuilder : auditBuilders){
				StockActionDao.auditStockAction(dbCon, staff, auditBuilder);
			}
		}
		
		String sql;
		
		//Get the date to last daily settlement.
		//Make the 00:00 of today as on duty if no daily settle record exist before. 
		sql = " SELECT MAX(off_duty) FROM " + Params.dbName + ".daily_settle_history " +
			  " WHERE " +
			  " restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		long onDuty = 0;
		if(dbCon.rs.next()){
			if(dbCon.rs.getTimestamp(1) != null){
				onDuty = dbCon.rs.getTimestamp(1).getTime();
			}else{
				onDuty = RestaurantDao.getById(dbCon, staff.getRestaurantId()).getBirthDate();
			}
		}else{
			onDuty = RestaurantDao.getById(dbCon, staff.getRestaurantId()).getBirthDate();
		}
		
		DutyRange range = new DutyRange(onDuty, System.currentTimeMillis());

		result.setRange(range);
		
		//get the amount to shift record
		sql = " SELECT COUNT(*) FROM " + Params.dbName + ".shift " +
			  " WHERE 1=1 " +
			  (staff.getRestaurantId() < 0 ? "AND restaurant_id <> " + Restaurant.ADMIN : "AND restaurant_id=" + staff.getRestaurantId());
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.setTotalShift(dbCon.rs.getInt(1));
		}
		dbCon.rs.close();
		
		
		try{
			dbCon.conn.setAutoCommit(false);
			
			//Archive the order and associated order food, taste group records.
			OrderDao.Result orderResult = OrderDao.archive(dbCon, staff);
			result.orderArchive = orderResult.getOrderArchive();
			result.orderFoodArchive = orderResult.getOrderFoodArchive();
			result.tgArchive = orderResult.getTgArchive();
			result.mixedArchive = orderResult.getMixedArchive();
			
			//Archive the shift records.
			ShiftDao.archive(dbCon, staff);
			
			//Archive the payment records.
			PaymentDao.archive(dbCon, staff);
			
			//Archive the member operation records.
			MemberOperationDao.archive(dbCon, staff);
			
			//Create the daily settle record
			sql = " INSERT INTO " + Params.dbName + ".daily_settle_history (`restaurant_id`, `name`, `on_duty`, `off_duty`) VALUES (" +
				  staff.getRestaurantId() + ", " +
				  "'" + staff.getName() + "', " +
				  " DATE_FORMAT('" + range.getOnDutyFormat() + "', '%Y%m%d%H%i%s')" + "," +
				  " DATE_FORMAT('" + range.getOffDutyFormat() + "', '%Y%m%d%H%i%s')" + 
				  " ) ";

			//Insert the daily settle record in case of manual.
			if(type == SettleType.MANUAL){
				dbCon.stmt.executeUpdate(sql);				
			}else{
				//Insert the record if the amount of rest orders is greater than zero in case of automation.
				if(result.orderArchive.getAmount() > 0){
					dbCon.stmt.executeUpdate(sql);
				}
			}
			
			//Perform restore food limit.
			FoodDao.restoreLimit(dbCon, staff);
			
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
	
	/**
	 * Check to see whether the paid order is exist before daily settlement to a specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal attached with the restaurant id
	 * @return
	 * 			an integer array holding the paid order id, 
	 * 			return int[0] if no paid order exist. 
	 * @throws SQLException
	 */
	public static int[] check(DBCon dbCon, Staff staff) throws SQLException{
		String sql;
		int[] restOrderId = new int[0];
		
		/**
		 * Get the last off duty date from both shift and shift_history,
		 */
		String lastOffDuty;
		sql = "SELECT MAX(off_duty) FROM (" +
		 	  "SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id=" + staff.getRestaurantId() + " UNION " +
			  "SELECT off_duty FROM " + Params.dbName + ".shift_history WHERE restaurant_id=" + staff.getRestaurantId() + ") AS all_off_duty";
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
			  " restaurant_id = " + staff.getRestaurantId() + " AND " +
			  " (status = " + Order.Status.PAID.getVal() + " OR " + " status = " + Order.Status.REPAID.getVal() + ")" + " AND " +
			  " order_date BETWEEN " +
			  "'" + lastOffDuty + "'" + " AND " + "NOW()";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<Integer> orderIds = new ArrayList<Integer>();
		while(dbCon.rs.next()){
			orderIds.add(dbCon.rs.getInt("id"));
		}
		dbCon.rs.close();
		
		restOrderId = new int[orderIds.size()];
		for(int i = 0; i < restOrderId.length; i++){
			restOrderId[i] = orderIds.get(i).intValue();
		}
		
		return restOrderId;
	}
	
}
