package com.wireless.db.system;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.system.Setting;
import com.wireless.pojo.system.SystemSetting;

public class SystemDao {
	
	public static class ExtraCond{
		private int id;
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND B.setting_id = " + id);
			}
			return extraCond.toString();
		}
	}
	
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
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws BusinessException 
	 */
	public static List<SystemSetting> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		final List<SystemSetting> result = new ArrayList<SystemSetting>();
		String sql;
		sql = " SELECT A.id AS restaurant_id, B.setting_id, B.price_tail, B.receipt_style, B.erase_quota " +
			  " FROM " + Params.dbName + ".restaurant A " +
			  " JOIN " + Params.dbName + ".setting B ON A.id = B.restaurant_id " +
			  " WHERE 1 = 1 " +
			  " AND B.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			SystemSetting item = new SystemSetting();
			Setting setting = item.getSetting();
			setting.setId(dbCon.rs.getInt("setting_id"));
			setting.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			setting.setPriceTail(Setting.Tail.valueOf(dbCon.rs.getInt("price_tail")));
			setting.setReceiptStyle((int)dbCon.rs.getLong("receipt_style"));
			setting.setEraseQuota(dbCon.rs.getInt("erase_quota"));
			result.add(item);
		}
		dbCon.rs.close();
		
		for(SystemSetting setting : result){
			setting.setRestaurant(RestaurantDao.getById(dbCon, setting.getSetting().getRestaurantId()));
		}
		return result;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws BusinessException 
	 */
	public static List<SystemSetting> getByCond(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return SystemDao.getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
}
