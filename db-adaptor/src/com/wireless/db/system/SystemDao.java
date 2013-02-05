package com.wireless.db.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.system.DailySettle;
import com.wireless.pojo.system.Restaurant;
import com.wireless.pojo.system.Setting;
import com.wireless.pojo.system.SystemSetting;
import com.wireless.util.SQLUtil;

@SuppressWarnings("unchecked")
public class SystemDao {
	
	public static final String RID_LIST = "RESTAURANT_ID_LIST";
	public static final short MIN_DAILY_SETTLE = 0x0;
	public static final short MAX_DAILY_SETTLE = 0x1;
	
	/**
	 * 
	 * @param set
	 * @throws Exception
	 */
	public static int updatePriceTail(SystemSetting set) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			if(set == null){
				throw new Exception("修改失败,获取修改信息失败.");
			}
			
			dbCon.connect();
			
			String updateSQL = "UPDATE " + Params.dbName + ".setting SET "
							   + " price_tail = " + set.getSetting().getPriceTail() 
							   + " ,erase_quota = " + set.getSetting().getEraseQuota()
							   + " WHERE restaurant_id = " + set.getRestaurantID();
			
			count = dbCon.stmt.executeUpdate(updateSQL);
			if(count != 1){
				throw new Exception("修改失败, 未修改收款金额设置,未知错误.");
			}
		} catch(Exception e){
			throw e;
		} finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param set
	 * @return
	 * @throws Exception
	 */
	public static SystemSetting getSystemSetting(SystemSetting set) throws Exception{
		SystemSetting sysSet = null;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String selectSQL = "SELECT A.id restaurant_id, A.restaurant_name, A.restaurant_info, A.record_alive, " 
							   + " B.setting_id, B.price_tail, B.auto_reprint, B.receipt_style, B.erase_quota "
							   + " FROM restaurant A, setting B "
							   + " WHERE" 
							   + " A.id = B.restaurant_id"
							   + " AND A.id = " + set.getRestaurantID();
			
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			
			while(dbCon.rs != null && dbCon.rs.next()){
				sysSet = new SystemSetting();
				Restaurant restaurant = sysSet.getRestaurant();
				Setting setting = sysSet.getSetting();
				
				restaurant.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
				restaurant.setName(dbCon.rs.getString("restaurant_name"));
				restaurant.setInfo(dbCon.rs.getString("restaurant_info"));
				restaurant.setRecordAlive(dbCon.rs.getLong("record_alive"));
				
				setting.setSettingID(dbCon.rs.getInt("setting_id"));
				setting.setPriceTail(dbCon.rs.getInt("price_tail"));
				setting.setAutoReprint(dbCon.rs.getInt("auto_reprint"));
				setting.setReceiptStyle(dbCon.rs.getLong("receipt_style"));
				setting.setEraseQuota(dbCon.rs.getInt("erase_quota"));
				
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return sysSet;
	}
	
	/**
	 * 
	 * @param restaurant
	 * @return
	 * @throws Exception
	 */
	public static Restaurant getRestaurant(Restaurant restaurant) throws Exception{
		SystemSetting s = new SystemSetting(restaurant, null, null);
		return SystemDao.getSystemSetting(s).getRestaurant();
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<DailySettle> getDailySettle(DBCon dbCon, Map<String, Object> params) throws Exception{
		List<DailySettle> list = new ArrayList<DailySettle>();
		DailySettle item = null;
		StringBuffer ridContent = new StringBuffer();
		if(params != null){
			List<Integer> ridList = params.get(SystemDao.RID_LIST) == null ? null : (List<Integer>)params.get(SystemDao.RID_LIST);
			if(ridList != null && ridList.size() > 0){
				for(int i = 0; i < ridList.size(); i++){
					ridContent.append(i > 0 ? "," : "");
					ridContent.append(ridList.get(i));
				}
			}
		}
		String querySQL = "SELECT A.id, A.restaurant_id, A.name, A.on_duty, A.off_duty "
		 		+ " FROM "
		 		+ " daily_settle_history A "
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
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<DailySettle> getDailySettle(Map<String, Object> params) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return SystemDao.getDailySettle(dbCon, params);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param rid
	 * @return
	 * @throws Exception
	 */
	public static List<DailySettle> getDailySettle(DBCon dbCon, List<Integer> ridList) throws Exception{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(SystemDao.RID_LIST, ridList);
		params.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY A.off_duty ASC ");
		return SystemDao.getDailySettle(dbCon, params);
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param rid
	 * @return
	 * @throws Exception
	 */
	public static List<DailySettle> getDailySettle(DBCon dbCon, int rid) throws Exception{
		List<Integer> ridList = new ArrayList<Integer>();
		ridList.add(rid);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(SystemDao.RID_LIST, ridList);
		params.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY A.off_duty ASC ");
		return SystemDao.getDailySettle(dbCon, params);
	}
	
	/**
	 * 
	 * @param rid
	 * @return
	 * @throws Exception
	 */
	public static List<DailySettle> getDailySettle(List<Integer> ridList) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return SystemDao.getDailySettle(dbCon, ridList);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param rid
	 * @return
	 * @throws Exception
	 */
	public static List<DailySettle> getDailySettle(int rid) throws Exception{
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
	 * @throws Exception
	 */
	public static DailySettle getDailySettle(DBCon dbCon, int rid, short type) throws Exception{
		List<DailySettle> list = null;
		DailySettle item = null;
		List<Integer> ridList = new ArrayList<Integer>();
		
		ridList.add(rid);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(SystemDao.RID_LIST, ridList);
		if(type == SystemDao.MAX_DAILY_SETTLE){
			params.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY A.off_duty DESC ");
		}else{
			params.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY A.off_duty ASC ");
		}
		list = SystemDao.getDailySettle(dbCon, params);
		
		if(list != null && list.size() > 0){
			item = list.get(0);
		}
		return item;
	}
	
	/**
	 * 
	 * @param rid
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public static DailySettle getDailySettle(int rid, short type) throws Exception{
		DailySettle item = null;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			item = SystemDao.getDailySettle(dbCon, rid, type);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return item;
	}
	
	
}
