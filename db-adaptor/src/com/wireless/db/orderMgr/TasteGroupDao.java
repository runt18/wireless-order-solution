package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.TasteError;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteGroup;

public class TasteGroupDao {
	
	/**
	 * Get the taste group from today according to the specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param tasteGroupId
	 * 			the taste group id
	 * @return the {@link TasteGroup} to specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the taste group to this specific id does NOT exist
	 */
	public static TasteGroup getTodayById(Staff staff, int tasteGroupId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getTodayById(dbCon, staff, tasteGroupId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the taste group from history according to the specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param tasteGroupId
	 * 			the taste group id 
	 * @return the {@link TasteGroup} to this specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the taste group to this specific id does NOT exist
	 */
	public static TasteGroup getHistory(Staff staff, int tasteGroupId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getHistoryById(dbCon, staff, tasteGroupId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the taste group from history according to the specific id.
	 * @param dbCon  
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param tasteGroupId
	 * 			the taste group id 
	 * @return the {@link TasteGroup} to this specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the taste group to this specific id does NOT exist
	 */
	public static TasteGroup getHistoryById(DBCon dbCon, Staff staff, int tasteGroupId) throws SQLException, BusinessException{
		String sql;
		
		sql = " SELECT " +
			  " TG.taste_group_id, " +
			  " TG.normal_taste_group_id, TG.normal_taste_pref, TG.normal_taste_price, " +
			  " CASE WHEN (TG.tmp_taste_id IS NULL) THEN 0 ELSE 1 END AS has_tmp_taste, " +
			  " TG.tmp_taste_id, TG.tmp_taste_pref, TG.tmp_taste_price " +
			  " FROM " +  Params.dbName + ".taste_group_history TG " + 
			  " WHERE TG.taste_group_id = " + tasteGroupId;

		TasteGroup tg;

		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			//Get the normal taste in case of exist.
			Taste normal = null;
			int normalTasteGroupId = dbCon.rs.getInt("normal_taste_group_id");
			if(normalTasteGroupId != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
				normal = new Taste();
				normal.setPreference(dbCon.rs.getString("normal_taste_pref"));
				normal.setPrice(dbCon.rs.getFloat("normal_taste_price"));
			}
			
			//Get the temporary taste in case of exist.
			Taste tmp = null;
			if(dbCon.rs.getBoolean("has_tmp_taste")){
				tmp = new Taste();
				tmp.setTasteId(dbCon.rs.getInt("tmp_taste_id"));
				tmp.setPreference(dbCon.rs.getString("tmp_taste_pref"));
				tmp.setPrice(dbCon.rs.getFloat("tmp_taste_price"));
			}				
			
			tg = new TasteGroup(tasteGroupId, normal, tmp);
		}else{
			throw new BusinessException(TasteError.TASTE_GROUP_NOT_EXIST);
		}
		dbCon.rs.close();
		
		return tg;
	}
	
	/**
	 * Get the taste group from today according to the specific id.
	 * @param dbCon  
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param tasteGroupId
	 * 			the taste group id
	 * @return the {@link TasteGroup} to specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the taste group to this specific id does NOT exist
	 */
	public static TasteGroup getTodayById(DBCon dbCon, Staff staff, int tasteGroupId) throws SQLException, BusinessException{
		String sql;
		
		sql = " SELECT " +
			  " TG.taste_group_id, " +
			  " TG.normal_taste_group_id, TG.normal_taste_pref, TG.normal_taste_price, " +
			  " CASE WHEN (TG.tmp_taste_id IS NULL) THEN 0 ELSE 1 END AS has_tmp_taste, " +
			  " TG.tmp_taste_id, TG.tmp_taste_pref, TG.tmp_taste_price " +
			  " FROM " +  Params.dbName + ".taste_group TG " + 
			  " WHERE TG.taste_group_id = " + tasteGroupId;

		TasteGroup tg;
		int normalTasteGroupId;

		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			//Get the normal taste in case of exist.
			Taste normal = null;
			normalTasteGroupId = dbCon.rs.getInt("normal_taste_group_id");
			if(normalTasteGroupId != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
				normal = new Taste();
				normal.setPreference(dbCon.rs.getString("normal_taste_pref"));
				normal.setPrice(dbCon.rs.getFloat("normal_taste_price"));
			}
			
			//Get the temporary taste in case of exist.
			Taste tmp = null;
			if(dbCon.rs.getBoolean("has_tmp_taste")){
				tmp = new Taste();
				tmp.setTasteId(dbCon.rs.getInt("tmp_taste_id"));
				tmp.setPreference(dbCon.rs.getString("tmp_taste_pref"));
				tmp.setPrice(dbCon.rs.getFloat("tmp_taste_price"));
			}				
			
			tg = new TasteGroup(tasteGroupId, normal, tmp);
		}else{
			throw new BusinessException(TasteError.TASTE_GROUP_NOT_EXIST);
		}
		dbCon.rs.close();

		//Get detail of each taste to this group.
		List<Taste> tastes = new ArrayList<Taste>();
		if(normalTasteGroupId != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
			sql = " SELECT taste_id FROM " + Params.dbName + ".normal_taste_group WHERE normal_taste_group_id = " + normalTasteGroupId;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				tastes.add(new Taste(dbCon.rs.getInt("taste_id")));
			}
			dbCon.rs.close();
		}
		
		for(Taste t : tastes){
			try{
				t.copyFrom(TasteDao.getTasteById(dbCon, staff, t.getTasteId()));
				tg.addTaste(t);
			}catch(BusinessException ignored){}
		}
		return tg;
		
	}
	
	/**
	 * Get taste group with details
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
//	private static List<TasteGroup> getTasteGroupDetail(ResultSet rs) throws SQLException{
//		Map<Integer, TasteGroup> tgs = new LinkedHashMap<Integer, TasteGroup>();
//		while(rs.next()){
//			int tasteGroupId = rs.getInt("taste_group_id");
//			TasteGroup tg = tgs.get(tasteGroupId);
//			if(tg != null){
//				if(rs.getInt("normal_taste_group_id") != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
//					Taste normalDetail = new Taste();
//					normalDetail.setTasteId(rs.getInt("taste_id"));
//					TasteCategory category = new TasteCategory(rs.getInt("category_id"));
//					category.setName(rs.getString("category_name"));
//					category.setType(TasteCategory.Type.valueOf(rs.getInt("category_type")));
//					category.setStatus(TasteCategory.Status.valueOf(rs.getInt("category_status")));
//					normalDetail.setCategory(category);
//					normalDetail.setRestaurantId(rs.getInt("restaurant_id"));
//					normalDetail.setPrice(rs.getFloat("price"));
//					normalDetail.setPreference(rs.getString("preference"));
//					tg.addTaste(normalDetail);
//				}
//				
//				tgs.put(tasteGroupId, tg);
//				
//			}else{
//				Taste normal = null;
//				if(rs.getInt("normal_taste_group_id") != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
//					normal = new Taste();
//					normal.setPreference(rs.getString("normal_taste_pref"));
//					normal.setPrice(rs.getFloat("normal_taste_price"));
//				}
//				
//				Taste tmp = null;
//				if(rs.getBoolean("has_tmp_taste")){
//					tmp = new Taste();
//					tmp.setTasteId(rs.getInt("tmp_taste_id"));
//					tmp.setPreference(rs.getString("tmp_taste_pref"));
//					tmp.setPrice(rs.getFloat("tmp_taste_price"));
//				}				
//				
//				tg = new TasteGroup(tasteGroupId, normal, tmp);
//				
//				if(rs.getInt("normal_taste_group_id") != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
//					Taste normalDetail = new Taste();
//					normalDetail.setTasteId(rs.getInt("taste_id"));
//					normalDetail.setCategory(new TasteCategory(rs.getShort("category_id")));
//					normalDetail.setRestaurantId(rs.getInt("restaurant_id"));
//					normalDetail.setPrice(rs.getFloat("price"));
//					normalDetail.setPreference(rs.getString("preference"));
//					tg.addTaste(normalDetail);
//				}
//				
//				tgs.put(tasteGroupId, tg);
//			}
//		}
//		rs.close();
//		
//		return new ArrayList<TasteGroup>(tgs.values());
//	}
	
//	private static List<TasteGroup> getTasteGroupGeneral(ResultSet rs) throws SQLException{
//		
//		List<TasteGroup> tgs = new ArrayList<TasteGroup>();
//		
//		while(rs.next()){			
//			Taste normal = null;
//			if(rs.getInt("normal_taste_group_id") != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
//				normal = new Taste();
//				normal.setPreference(rs.getString("normal_taste_pref"));
//				normal.setPrice(rs.getFloat("normal_taste_price"));
//			}
//			
//			Taste tmp = null;
//			if(rs.getBoolean("has_tmp_taste")){
//				tmp = new Taste();
//				tmp.setTasteId(rs.getInt("tmp_taste_id"));
//				tmp.setPreference(rs.getString("tmp_taste_pref"));
//				tmp.setPrice(rs.getFloat("tmp_taste_price"));
//			}				
//			
//			tgs.add(new TasteGroup(rs.getInt("taste_group_id"), normal, tmp));
//		}
//		rs.close();
//		
//		return tgs;
//	}
	
}
