package com.wireless.db.system;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.stockMgr.MonthlyBalanceDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StockError;
import com.wireless.exception.SystemError;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.system.DailySettle;
import com.wireless.pojo.system.Setting;
import com.wireless.pojo.system.SystemSetting;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.SQLUtil;

public class SystemDao {
	
	public static final String RID_LIST = "RESTAURANT_ID_LIST";
	public static final short MIN_DAILY_SETTLE = 0x0;
	public static final short MAX_DAILY_SETTLE = 0x1;
	
	/**
	 * 
	 * @param set
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int updatePriceTail(SystemSetting ss) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = updatePriceTail(dbCon, ss);
		} finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param set
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int updatePriceTail(DBCon dbCon, SystemSetting ss) throws BusinessException, SQLException{
		int count = 0;
		if(ss == null){
			// TODO
			throw new BusinessException("修改失败, 获取修改信息失败.");
		}
		String updateSQL = "UPDATE " + Params.dbName + ".setting SET "
						   + " price_tail = " + ss.getSetting().getPriceTail().getValue()
						   + " ,erase_quota = " + ss.getSetting().getEraseQuota()
						   + " WHERE restaurant_id = " + ss.getRestaurantID();
		count = dbCon.stmt.executeUpdate(updateSQL);
		return count;
	}
	
	/**
	 * Update the current_material_month. 
	 * @param s the Setting
	 * @throws SQLException 
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the setting is not exist
	 */
	public static void updateCurrentMonth(Staff term) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			updateCurrentMonth(dbCon, term);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Update the current_material_month.
	 * @param dbCon
	 * 			the database connection
	 * @param s	the Setting
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the setting is not exist
	 */
	public static void updateCurrentMonth(DBCon dbCon, Staff term) throws SQLException, BusinessException{
		//判断是否有未审核的盘点单
		if(StockActionDao.isStockTakeChecking(dbCon, term)){
			throw new BusinessException(StockError.STOCKTAKE_CURRENTMONTH_UPDATE);
		}
		//判断是否有未审核的库单
		List<StockAction> list = StockActionDao.getStockActions(term, " AND status = " + StockAction.Status.UNAUDIT.getVal(), null);
		if(!list.isEmpty()){
			throw new BusinessException(StockError.STOCKACTION_CURRENTMONTH_UPDATE);
		}
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND B.restaurant_id = " + term.getRestaurantId());
		
		//Setting setting = SystemDao.getSystemSetting(params).get(0).getSetting();
		Calendar c = Calendar.getInstance();
		//c.setTime(new Date(setting.getLongCurrentMonth()));
		//给日期增加一个工作月
		c.add(Calendar.MONTH, +1);
		String sql = "UPDATE " + Params.dbName + ".setting SET " +
					" current_material_month = '" + DateUtil.formatToDate(c.getTime().getTime()) + "' " + 
					" WHERE restaurant_id = " + term.getRestaurantId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(SystemError.NOT_FIND_RESTAURANTID);
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<SystemSetting> getSystemSetting(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		List<SystemSetting> list = new ArrayList<SystemSetting>();
		SystemSetting item = null;
		String querySQL = "SELECT A.id restaurant_id, A.restaurant_name, A.restaurant_info, A.record_alive, " 
				   + " B.setting_id, B.price_tail, B.receipt_style, B.erase_quota "
				   + " FROM " + Params.dbName + ".restaurant A, " + Params.dbName + ".setting B "
				   + " WHERE A.id = B.restaurant_id ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new SystemSetting();
			Restaurant restaurant = item.getRestaurant();
			Setting setting = item.getSetting();
			
			restaurant.setId(dbCon.rs.getInt("restaurant_id"));
			restaurant.setName(dbCon.rs.getString("restaurant_name"));
			restaurant.setInfo(dbCon.rs.getString("restaurant_info"));
			restaurant.setRecordAlive((int)dbCon.rs.getLong("record_alive"));
			
			setting.setId(dbCon.rs.getInt("setting_id"));
			setting.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			setting.setPriceTail(dbCon.rs.getInt("price_tail"));
			setting.setReceiptStyle((int)dbCon.rs.getLong("receipt_style"));
			setting.setEraseQuota(dbCon.rs.getInt("erase_quota"));
			
			list.add(item);
			item = null;
		}
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<SystemSetting> getSystemSetting(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return SystemDao.getSystemSetting(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param rid
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static SystemSetting getSystemSettingById(int rid) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getSystemSettingById(dbCon, rid);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param rid
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static SystemSetting getSystemSettingById(DBCon dbCon, int rid) throws BusinessException, SQLException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.id = " + rid);
		List<SystemSetting> list = SystemDao.getSystemSetting(dbCon, params);
		if(!list.isEmpty())
			return list.get(0);
		else
			return null;
	}
	
	/**
	 * 
	 * @param rid
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static Restaurant getRestaurant(int rid) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getRestaurant(dbCon, rid);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param rid
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static Restaurant getRestaurant(DBCon dbCon, int rid) throws BusinessException, SQLException{
		SystemSetting ss = SystemDao.getSystemSettingById(dbCon, rid);
		if(ss != null)
			return ss.getRestaurant();
		else
			return null;
	}
	
	/**
	 * 
	 * @param rid
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static Setting getSetting(int rid) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getSetting(dbCon, rid);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param rid
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static Setting getSetting(DBCon dbCon, int rid) throws BusinessException, SQLException{
		SystemSetting ss = SystemDao.getSystemSettingById(dbCon, rid);
		if(ss != null)
			return ss.getSetting();
		else
			return null;
	}
	
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	private static List<DailySettle> getDailySettle(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		List<DailySettle> list = new ArrayList<DailySettle>();
		DailySettle item = null;
		StringBuffer ridContent = new StringBuffer();
		if(params != null){
			List<?> ridList = params.get(SystemDao.RID_LIST) == null ? null : (List<?>)params.get(SystemDao.RID_LIST);
			if(ridList != null && ridList.size() > 0){
				for(int i = 0; i < ridList.size(); i++){
					ridContent.append(i > 0 ? "," : "");
					ridContent.append(ridList.get(i).toString());
				}
			}
		}
		String querySQL = "SELECT A.id, A.restaurant_id, A.name, A.on_duty, A.off_duty "
		 		+ " FROM "
		 		+ Params.dbName + ".daily_settle_history A "
		 		+ " WHERE 1 = 1 "
		 		+ (ridContent.length() > 0 ? " AND A.restaurant_id IN (" + ridContent.toString() +")" : "");
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new DailySettle();
			item.setId(dbCon.rs.getInt("id"));
			item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			item.setName(dbCon.rs.getString("name"));
			item.setOnDuty(dbCon.rs.getTimestamp("on_duty").getTime());
			item.setOffDuty(dbCon.rs.getTimestamp("off_duty").getTime());
			
			list.add(item);
			item = null;
		}
		return list;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param ridList
	 * @return
	 * @throws SQLException
	 */
	public static List<DailySettle> getDailySettle(DBCon dbCon, List<Integer> ridList) throws SQLException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SystemDao.RID_LIST, ridList);
		params.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY A.off_duty ASC ");
		return SystemDao.getDailySettle(dbCon, params);
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param rid
	 * @return
	 * @throws SQLException
	 */
	public static List<DailySettle> getDailySettle(DBCon dbCon, int rid) throws SQLException{
		List<Integer> ridList = new ArrayList<Integer>();
		ridList.add(rid);
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SystemDao.RID_LIST, ridList);
		params.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY A.off_duty ASC ");
		return SystemDao.getDailySettle(dbCon, params);
	}
	
	/**
	 * 
	 * @param ridList
	 * @return
	 * @throws SQLException
	 */
	public static List<DailySettle> getDailySettle(List<Integer> ridList) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return SystemDao.getDailySettle(dbCon, ridList);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param rid
	 * @return
	 * @throws SQLException
	 */
	public static List<DailySettle> getDailySettle(int rid) throws SQLException{
		List<Integer> ridList = new ArrayList<Integer>();
		ridList.add(rid);
		return SystemDao.getDailySettle(ridList);
	}
	
	
	/**
	 * 
	 * @param dbCon
	 * @param rid
	 * @param type
	 * @return
	 * @throws SQLException
	 */
	public static DailySettle getDailySettle(DBCon dbCon, int rid, short type) throws SQLException{
		List<DailySettle> list = null;
		DailySettle item = null;
		List<Integer> ridList = new ArrayList<Integer>();
		ridList.add(rid);
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SystemDao.RID_LIST, ridList);
		if(type == SystemDao.MAX_DAILY_SETTLE){
			params.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY A.off_duty DESC ");
		}else{
			params.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY A.off_duty ASC ");
		}
		list = SystemDao.getDailySettle(dbCon, params);
		
		if(list != null && !list.isEmpty()){
			item = list.get(0);
		}
		return item;
	}
	
	/**
	 * 
	 * @param rid
	 * @param type
	 * @return
	 * @throws SQLException
	 */
	public static DailySettle getDailySettle(int rid, short type) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return SystemDao.getDailySettle(dbCon, rid, type);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<SystemSetting> getSystemSetting() throws SQLException {
		List<SystemSetting> list = new ArrayList<SystemSetting>();
		
		return list;
	}
	/**
	 * Get the CurrentMonth.
	 * @param term
	 * 			The Terminal
	 * @return
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static long getCurrentMonth(Staff term) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getCurrentMonth(dbCon, term);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Get the CurrentMonth.
	 * @param dbCon
	 * @param term
	 * 			The Terminal
	 * @return
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static long getCurrentMonth(DBCon dbCon, Staff term) throws SQLException, BusinessException{
		long currentDate = MonthlyBalanceDao.getCurrentMonthTimeByRestaurant(term.getRestaurantId());
		return currentDate;
		
		
	}
		
		
	
	
	
}
