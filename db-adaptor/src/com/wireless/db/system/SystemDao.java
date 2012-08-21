package com.wireless.db.system;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.system.SystemSetting;

public class SystemDao {
	
	/**
	 * 
	 * @param set
	 * @throws Exception
	 */
	public static void updatePriceTail(SystemSetting set) throws Exception{
		
		DBCon dbCon = new DBCon();
		
		try{
			if(set == null){
				throw new Exception("修改失败,获取修改信息失败.");
			}
			
			dbCon.connect();
			
			String updateSQL = "update " + Params.dbName + ".setting set price_tail = " + set.getPriceTail() + " where restaurant_id = " + set.getId();
			
			int count = dbCon.stmt.executeUpdate(updateSQL);
			
			if(count != 1){
				throw new Exception("修改失败,未修改金额尾数处理方式.");
			}
			
		} catch(Exception e){
			throw e;
		} finally{
			dbCon.disconnect();
		}
		
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
			String selectSQL = "select A.id, A.restaurant_name, A.restaurant_info, A.record_alive, " 
							   + " B.setting_id, B.price_tail, B.auto_reprint, B.receipt_style"
							   + " from restaurant A, setting B "
							   + " where" 
							   + " A.id = B.restaurant_id" 
							   + " and A.id = " + set.getId();
			
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			
			while(dbCon.rs != null && dbCon.rs.next()){
				sysSet = new SystemSetting();
				sysSet.setId(set.getId());
				sysSet.setName(dbCon.rs.getString("restaurant_name"));
				sysSet.setInfo(dbCon.rs.getString("restaurant_info"));
				sysSet.setRecordAlive(dbCon.rs.getLong("record_alive"));
				sysSet.setSettingID(dbCon.rs.getInt("setting_id"));
				sysSet.setPriceTail(dbCon.rs.getInt("price_tail"));
				sysSet.setAutoReprint(dbCon.rs.getInt("auto_reprint"));
				sysSet.setReceiptStyle(dbCon.rs.getLong("receipt_style"));
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return sysSet;
	}
	
}
