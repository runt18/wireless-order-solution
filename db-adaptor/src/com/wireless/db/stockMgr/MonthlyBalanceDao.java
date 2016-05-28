package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StockError;
import com.wireless.pojo.billStatistics.DateRange;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.MonthlyBalance;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.Type;
import com.wireless.pojo.util.DateUtil;

public class MonthlyBalanceDao {
	
	
	public static class ExtraCond{
		private DateRange range; 
		private int id;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setRange(String yyyymm) throws ParseException{
			Calendar c = Calendar.getInstance();
			c.setTime(new SimpleDateFormat("yyyy-MM").parse(yyyymm));
			int day = c.getActualMaximum(Calendar.DAY_OF_MONTH);
			this.range = new DateRange(yyyymm + "-01", yyyymm + "-" + day);
			return this;
		}
		
		public String toString() {
			final StringBuilder extraCond = new StringBuilder();
			
			if(this.range != null){
				extraCond.append(" AND MB.month BETWEEN '" + range.getOpeningFormat() + "' AND '" + range.getEndingFormat() + " 23:59:59'");
			}
			
			if(this.id != 0){
				extraCond.append(" AND MB.Id = " + this.id);
			}
			
			
			return extraCond.toString();
		}
	}

	
	/**
	 * Insert a new monthlyBalance.
	 * @param build
	 * @return
	 * 			the id of monthlyBalance
	 * @throws SQLException
	 * @throws BusinessException 
	 * @throws ParseException 
	 */
	public static int insert(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int monthlyBalanceId = insert(dbCon, staff);
			dbCon.conn.commit();
			return monthlyBalanceId;
		}catch(SQLException | BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}

	
	
	/**
	 * 初始化操作
	 * @param dbCon
	 * @param staff
	 * @return
	 * @throws SQLException
	 */
	public static int init(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		MonthlyBalanceDao.deleteByCond(dbCon ,staff, null);
		return insert(dbCon, staff, true);
	}
	
	
	
	/**
	 * insert monthlyBalance
	 * @param dbCon
	 * @param staff
	 * @return
	 * @throws SQLException
	 */
	public static int insert(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		return insert(dbCon, staff, false);
	}
	
	
	
	/**
	 * insert monthlyBalance
	 * @param dbCon
	 * @param staff
	 * @return
	 * @throws SQLException
	 */
	private static int insert(DBCon dbCon, Staff staff, boolean init) throws SQLException, BusinessException{
		Date insertDate;
		if(init){
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			insertDate = new Date(DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)) + "-" + "01"));
		}else{
			insertDate = new Date(getCurrentMonthTime(staff));
			isAvailable(dbCon, staff);
		}
		
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".monthly_balance (restaurant_id, staff, month) VALUES (" + 
			 staff.getRestaurantId() +  
			 " ,'" + staff.getName() + "'" + 
			 " ,'" + new SimpleDateFormat("yyyy-MM-dd").format(insertDate) + "'" +
			 ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int monthlyBalanceId = 0;
		if(dbCon.rs.next()){
			monthlyBalanceId = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		if(monthlyBalanceId != 0){
			sql = " INSERT INTO " + Params.dbName + ".monthly_cost (material_id, monthly_balance_id, cost) " +
				  " SELECT material_id,"+ monthlyBalanceId + ", price FROM " + Params.dbName + ".material WHERE restaurant_id = " + staff.getRestaurantId();
			dbCon.stmt.executeUpdate(sql);
		}else{
			throw new BusinessException(" the id of monthlyBalance is not defined");
		}
		
		dbCon.rs.close();
		
		return monthlyBalanceId;
	}
	
	/**
	 * 
	 * @param staff
	 * @param extraCond
	 * @param orderCause
	 * @return
	 * @throws SQLException
	 */
	public static List<MonthlyBalance> getByCond(Staff staff, ExtraCond extraCond, String orderCause) throws SQLException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderCause);
		
		} finally {
			dbCon.disconnect();
		}
	}
	
	
	
	/**
	 * get monthlyBalance by extraCond
	 * @param dbCon
	 * @param staff
	 * @param extraCond
	 * @param OrderCause
	 * @return
	 * @throws SQLException
	 */
	public static List<MonthlyBalance> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String OrderCause) throws SQLException{
		String sql = " SELECT MB.id, MB.restaurant_id, MB.staff, MB.month FROM " + Params.dbName + ".monthly_balance MB" +
				 	 " WHERE 1 = 1" +
				 	 " AND MB.restaurant_id = " + staff.getRestaurantId() +
				 	 (extraCond != null ? extraCond.toString() : "") + 
				 	 (OrderCause != null ? OrderCause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		final List<MonthlyBalance> result = new ArrayList<MonthlyBalance>();
		while(dbCon.rs.next()){
			MonthlyBalance monthlyBalance = new MonthlyBalance();
			monthlyBalance.setId(dbCon.rs.getInt("id"));
			monthlyBalance.setStaffName(dbCon.rs.getString("staff"));
			monthlyBalance.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			monthlyBalance.setMonth(dbCon.rs.getDate("month").getTime());
			result.add(monthlyBalance);
		}
		dbCon.rs.close();
		return result;
	}
	
	/**
	 * delete monthlyBalance by extraCond
	 * @param staff
	 * @param extraCond
	 * @return
	 * @throws SQLException
	 */
	public static int deleteByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			return deleteByCond(staff, extraCond);
		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * delete monthlyBalance by extraCond
	 * @param dbCon
	 * @param staff
	 * @param extraCond
	 * @throws SQLException
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		int amount = 0;
		for(MonthlyBalance monthlyBalance : getByCond(dbCon, staff, extraCond, "")){
			
			sql = " DELETE FROM " + Params.dbName + ".monthly_cost WHERE monthly_balance_id = " + monthlyBalance.getId();
			dbCon.stmt.executeUpdate(sql);
			
			sql = " DELETE FROM " + Params.dbName + ".monthly_balance WHERE id = " + monthlyBalance.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
			
		}
		dbCon.rs.close();
		return amount;
	}
	
	
	/**
	 * Get MonthlyBalance according to id.
	 * @param id
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static MonthlyBalance getMonthlyBalanceById(Staff staff, int id) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			List<MonthlyBalance> list = getByCond(dbCon, staff, new ExtraCond().setId(id), "");
			if(!list.isEmpty()){
				return list.get(0);
			}else{
				throw new BusinessException(StockError.MONTHLY_BALANCE_NOT_EXIST);
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	
	/**
	 * getCurrentMonthTime by restaurantId
	 * @param restaurantId
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static long getCurrentMonthTime(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			return getCurrentMonthTime(dbCon, staff);
		} finally {
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * getCurrentMonthTime by restaurantId
	 * @param restaurant
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static long getCurrentMonthTime(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		
		String sql = " SELECT id, MAX(date_add(month, interval 1 MONTH)) month FROM " + Params.dbName + ".monthly_balance " + 
				 	 " WHERE restaurant_id = " + staff.getRestaurantId() ;
	
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		MonthlyBalance m = new MonthlyBalance();
		if(dbCon.rs.next()){
			if(dbCon.rs.getTimestamp("month") != null){
				m.setId(dbCon.rs.getInt("id"));
				m.setMonth(dbCon.rs.getTimestamp("month").getTime());
			}else{
				m.setId(-1);
			};
		}else{
			throw new BusinessException("还没进行月结操作");
		}
	
		if(m.getId() > 0){
			return m.getMonth();
		}else{
			List<StockAction> list = StockActionDao.getByCond(dbCon, staff, new StockActionDao.ExtraCond().setType(Type.STOCK_IN), " ORDER BY S.birth_date ASC LIMIT 1");
			Calendar c = Calendar.getInstance();
			if(list.isEmpty()){
				c.setTime(new Date());
				return DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-01");
			}else{
				c.setTime(new Date(list.get(0).getOriStockDate()));
				return DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 2) + "-01");
			}
		}
	}
	
	
	
	/**
	 * 判断能否进行月结
	 * @param staff
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	private static void isAvailable(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		long current = getCurrentMonthTime(dbCon, staff);
		Calendar currentDate = Calendar.getInstance();
		currentDate.setTime(new Date(current));
		/**
		 * 会计月末最后一天
		 */
		currentDate.setTime(new Date(DateUtil.parseDate(currentDate.get(Calendar.YEAR) + "-" + (currentDate.get(Calendar.MONTH) + 1) + "-" + currentDate.getMaximum(Calendar.DAY_OF_MONTH))));
		Calendar presentDate = Calendar.getInstance();
		presentDate.setTime(new Date());
		if(presentDate.after(currentDate) || presentDate.equals(currentDate)){
			String sql = " SELECT COUNT(id) FROM " + Params.dbName + ".stock_action " + 
					 	 " WHERE 1 = 1 " + 
					 	 " AND restaurant_id = " + staff.getRestaurantId() + 
					 	 " AND status = " + StockAction.Status.UNAUDIT.getVal();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			int count = 0;
			if(dbCon.rs.next()){
				count = dbCon.rs.getInt(1);
			}
			dbCon.rs.close();
			if(count > 0){
				throw new BusinessException("还存在没有审核的库单或盘点单");
			}
		}else{
			throw new BusinessException("还没到月结时间");
		}
		
	}
	
}
