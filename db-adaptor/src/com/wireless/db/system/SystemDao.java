package com.wireless.db.system;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.system.Restaurant;
import com.wireless.pojo.system.Setting;
import com.wireless.pojo.system.SystemSetting;

public class SystemDao {
	
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
	
}
