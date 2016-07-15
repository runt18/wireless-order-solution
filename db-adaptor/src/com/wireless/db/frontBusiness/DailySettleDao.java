package com.wireless.db.frontBusiness;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.DBTbl;
import com.wireless.db.Params;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.orderMgr.OrderDao;
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
import com.wireless.pojo.util.DateType;

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
	public static final class AutoResult{
		
		int elapsedTime;			//自动日结消耗的秒数
		int shiftAmount;			//当日交班的记录数
		int orderAmount;			//自动日结处理的账单数
		int ofAmount;				//自动日结处理的order food数量
		int tgAmount;				//自动日结处理的taste group数量
		int mixedAmount;			//自动日结处理的混合结账账单数
		int maxOrderId;				//自动日结后最大的order id
		int maxOrderFoodId;			//自动日结后最大的order food id
		
		AutoResult(){
			
		}
		
		@Override
		public String toString(){
			
			final String sep = System.getProperty("line.separator");
			DBTbl dbTbl = new DBTbl(DateType.HISTORY);
			StringBuilder resultInfo = new StringBuilder();
			resultInfo.append("info : " + orderAmount + " record(s) are moved to \"").append(dbTbl.orderTbl).append("\"").append(sep);
			resultInfo.append("info : " + mixedAmount + " record(s) are moved to \"").append(dbTbl.mixedTbl).append("\"").append(sep);
			resultInfo.append("info : " + ofAmount + " record(s) are moved to \"").append(dbTbl.orderFoodTbl).append("\"").append(sep);
			resultInfo.append("info : " + tgAmount + " record(s) are moved to \"").append(dbTbl.tgTbl).append("\"").append(sep);
			resultInfo.append("info : " + shiftAmount + " record(s) are moved to \"").append(dbTbl.shiftTbl).append("\"").append(sep);
			resultInfo.append("info : " + 
							  "maxium order id : " + maxOrderId + ", " +
							  "maxium order food id : " + maxOrderFoodId).append(sep);
			resultInfo.append("info : The record movement takes " + elapsedTime + " sec.");
			
			return resultInfo.toString();
		}
		
	}
	
	/**
	 * The result to daily settle is as below.
	 * 1 - the amount to the order 
	 * 2 - the amount to the order detail
	 * 3 - the maximum order id 
	 */
	public static final class ManualResult{
		
		int elapsedTime;				//日结消耗的时间
		int shiftAmount;				//当日交班的记录数
		DutyRange range;
		OrderDao.ArchiveResult orderArchive;
		
		public DutyRange getRange(){
			return this.range;
		}
		
		ManualResult(){
			
		}
		
		@Override
		public String toString(){
			return orderArchive.toString();
		}
		
	}

	/**
	 * Perform the daily settlement to all the restaurant.
	 * 
	 * @return the result to daily settlement
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static AutoResult auto() throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return auto(dbCon);
			
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
	public static AutoResult auto(DBCon dbCon) throws SQLException, BusinessException{
		
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
		
		AutoResult result = new AutoResult();
		
		//Get the max id to order
		sql = " SELECT MAX(id) FROM " + Params.dbName + ".order";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.maxOrderId = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();

		//Get the max id to order food
		sql = " SELECT MAX(id) FROM " + Params.dbName + ".order_food";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.maxOrderFoodId = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		int totalOrder = 0, totalOrderFood = 0, totalShift = 0;
		for(Staff staff : staffs){			
			ManualResult eachResult = auto(dbCon, staff);
			totalOrder += eachResult.orderArchive != null ? eachResult.orderArchive.orderAmount : 0;
			totalOrderFood += eachResult.orderArchive != null ? eachResult.orderArchive.ofAmount : 0;
			totalShift += eachResult.shiftAmount;
		}
		
		result.orderAmount = totalOrder;
		result.ofAmount = totalOrderFood;
		result.shiftAmount = totalShift;
		result.elapsedTime = ((int)(System.currentTimeMillis() - beginTime) / 1000);
		
		return result;
	}
	
	public static ManualResult auto(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		return exec(dbCon, staff, SettleType.AUTO_MATION);
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
	public static synchronized ManualResult manual(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return manual(dbCon, staff);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	private static void materialConsumption(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
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
					  " MAX(M.price) AS consume_price " +
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
					  " GROUP BY FM.food_id, FM.material_id " +
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
				
				StockAction stockAction = builder.build();
				
				//如果存在出库消耗单则插入
				if(!stockAction.getStockDetails().isEmpty()){
					auditBuilders.add(StockAction.AuditBuilder.newStockActionAudit(StockActionDao.insert(dbCon, staff, builder)));
				}
			}
			
		}
		
		//如果有盘点任务正在进行，则不审核出入库消耗单
		if(!StockActionDao.isStockTakeChecking(dbCon, staff)){
			for(AuditBuilder auditBuilder : auditBuilders){
				StockActionDao.audit(dbCon, staff, auditBuilder);
			}
		}
	}
	
	/**
	 * Perform to daily settle manually.
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param type
	 * 			  indicates whether the daily settle is manual or automation
	 * @return the result to daily settle
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @throws BusinessException
	 */
	private static ManualResult manual(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		return exec(dbCon, staff, SettleType.MANUAL);
	}
	
	/**
	 * Perform to daily settle.
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param type
	 * 			  indicates whether the daily settle is manual or automation
	 * @return the result to daily settle
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @throws BusinessException
	 */
	private static ManualResult exec(DBCon dbCon, Staff staff, SettleType type) throws SQLException, BusinessException{
		
		materialConsumption(dbCon, staff);
		
		ManualResult result = new ManualResult();
				
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

		result.range = range;

		//get the amount to shift record
		sql = " SELECT COUNT(*) FROM " + Params.dbName + ".shift WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.shiftAmount = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		try{
			dbCon.conn.setAutoCommit(false);
			
			//Archive the order and associated order food, taste group records from today to history.
			result.orderArchive = OrderDao.archive4Daily(dbCon, staff);
			
			//Archive the shift records.
			ShiftDao.archive4Daily(dbCon, staff);
			
			//Archive the payment records.
			PaymentDao.archive4Daily(dbCon, staff);
			
			//Archive the member operation records.
			MemberOperationDao.archive4Daily(dbCon, staff);
			
			//Create the daily settle record
			sql = " INSERT INTO " + Params.dbName + ".daily_settle_history (`restaurant_id`, `name`, `on_duty`, `off_duty`) VALUES (" +
				  staff.getRestaurantId() + ", " +
				  "'" + staff.getName() + "', " +
				  "'" + range.getOnDutyFormat() + "'," +
				  "'" + range.getOffDutyFormat() + "'" + 
				  " ) ";

			if(type == SettleType.MANUAL){
				//Insert the daily settle record in case of manual.
				dbCon.stmt.executeUpdate(sql);				
			}else if(type == SettleType.AUTO_MATION && result.orderArchive.orderAmount > 0){
				//Insert the record if the amount of rest orders is greater than zero in case of automation.
				dbCon.stmt.executeUpdate(sql);
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
	 * Archive the expired daily records from history to archive.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the amount to records archive
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int archive4Expired(DBCon dbCon, Staff staff) throws SQLException{
		DBTbl fromTbl = new DBTbl(DateType.HISTORY);
		DBTbl toTbl = new DBTbl(DateType.ARCHIVE);
		
		String sql;
		
		String extraCond = " AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(DAILY.off_duty) > REST.record_alive ";
		
		final String dailyItem = "`restaurant_id`, `name`, `on_duty`, `off_duty`";
		//Move the shift record from 'shift' to 'shift_history'.
		sql = " INSERT INTO " + Params.dbName + "." + toTbl.dailyTbl + "(" + dailyItem + ") " +
			  " SELECT " + dailyItem + " FROM " + Params.dbName + "." + fromTbl.dailyTbl + " DAILY " +
			  " JOIN " + Params.dbName + ".restaurant REST ON REST.id = DAILY.restaurant_id " +
			  " WHERE restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		int amount = dbCon.stmt.executeUpdate(sql);
		
		//Delete the today shift records belong to this restaurant.
		sql = " DELETE DAILY FROM " + Params.dbName + "." + fromTbl.dailyTbl + " DAILY " +
			  " JOIN " + Params.dbName + ".restaurant REST ON REST.id = DAILY.restaurant_id " +
			  " WHERE restaurant_id = " + staff.getRestaurantId() + (extraCond != null ? extraCond.toString() : "");
		dbCon.stmt.executeUpdate(sql);
		
		return amount;
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
