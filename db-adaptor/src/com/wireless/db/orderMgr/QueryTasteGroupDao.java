package com.wireless.db.orderMgr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteGroup;

public class QueryTasteGroupDao {
	
	/**
	 * Get the taste group from today according to the specific condition.
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the array holding the {@link TasteGroup}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static TasteGroup[] execByToday(String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execByToday(dbCon, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the taste group from history according to the specific condition.
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the array holding the {@link TasteGroup}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static TasteGroup[] execByHistory(String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execByHistory(dbCon, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the taste group from history according to the specific condition.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon  
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the array holding the {@link TasteGroup}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static TasteGroup[] execByHistory(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " TG.taste_group_id, " +
			  " TG.normal_taste_group_id, TG.normal_taste_pref, TG.normal_taste_price, " +
			  " CASE WHEN (TG.tmp_taste_id IS NULL) THEN 0 ELSE 1 END AS has_tmp_taste, " +			  
			  " TG.tmp_taste_id, TG.tmp_taste_pref, TG.tmp_taste_price, " +
			  " NTG.taste_id " +
			  " FROM " +
			  Params.dbName + ".taste_group_history TG " + 
			  " JOIN " +
			  Params.dbName + ".normal_taste_group_history NTG " +
			  " ON " +
			  " TG.normal_taste_group_id = NTG.normal_taste_group_id " +
			  " WHERE 1 = 1 " +
			  (extraCond == null ? "" : extraCond) +
			  (orderClause == null ? "" : orderClause);
		
		return getTasteGroupGeneral(dbCon.stmt.executeQuery(sql));
		
	}
	
	/**
	 * Get the taste group from today according to the specific condition.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon  
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the array holding the {@link TasteGroup}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static TasteGroup[] execByToday(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " TG.taste_group_id, " +
			  " TG.normal_taste_group_id, TG.normal_taste_pref, TG.normal_taste_price, " +
			  " CASE WHEN (TG.tmp_taste_id IS NULL) THEN 0 ELSE 1 END AS has_tmp_taste, " +
			  " TG.tmp_taste_id, TG.tmp_taste_pref, TG.tmp_taste_price, " +
			  " NTG.taste_id, TASTE.taste_alias, TASTE.restaurant_id, TASTE.category " +
			  " FROM " +
			  Params.dbName + ".taste_group TG " + 
			  " JOIN " +
			  Params.dbName + ".normal_taste_group NTG " +
			  " ON " +
			  " TG.normal_taste_group_id = NTG.normal_taste_group_id " +
			  " LEFT JOIN " +
			  Params.dbName + ".taste TASTE " +
			  " ON " +
			  " NTG.taste_id = TASTE.taste_id " +
			  " WHERE 1 = 1 " +
			  (extraCond == null ? "" : extraCond) +
			  (orderClause == null ? "" : orderClause);		

		return getTasteGroupDetail(dbCon.stmt.executeQuery(sql));
		
	}
	
	/**
	 * Get taste group with details
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private static TasteGroup[] getTasteGroupDetail(ResultSet rs) throws SQLException{
		LinkedHashMap<Integer, TasteGroup> tgs = new LinkedHashMap<Integer, TasteGroup>();
		while(rs.next()){
			int tasteGroupId = rs.getInt("taste_group_id");
			TasteGroup tg = tgs.get(tasteGroupId);
			if(tg != null){
				if(rs.getInt("normal_taste_group_id") != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
					Taste normalDetail = new Taste();
					normalDetail.setTasteId(rs.getInt("taste_id"));
					normalDetail.setAliasId(rs.getInt("taste_alias"));
					normalDetail.setCategory(rs.getShort("category"));
					normalDetail.setRestaurantId(rs.getInt("restaurant_id"));
					tg.addTaste(normalDetail);
				}
				
				tgs.put(tasteGroupId, tg);
				
			}else{
				Taste normal = null;
				if(rs.getInt("normal_taste_group_id") != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
					normal = new Taste();
					normal.setPreference(rs.getString("normal_taste_pref"));
					normal.setPrice(rs.getFloat("normal_taste_price"));
				}
				
				Taste tmp = null;
				if(rs.getBoolean("has_tmp_taste")){
					tmp = new Taste();
					tmp.setAliasId(rs.getInt("tmp_taste_id"));
					tmp.setTasteId(rs.getInt("tmp_taste_id"));
					tmp.setPreference(rs.getString("tmp_taste_pref"));
					tmp.setPrice(rs.getFloat("tmp_taste_price"));
				}				
				
				tg = new TasteGroup(tasteGroupId, normal, tmp);
				
				if(rs.getInt("normal_taste_group_id") != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
					Taste normalDetail = new Taste();
					normalDetail.setTasteId(rs.getInt("taste_id"));
					normalDetail.setAliasId(rs.getInt("taste_alias"));
					normalDetail.setCategory(rs.getShort("category"));
					normalDetail.setRestaurantId(rs.getInt("restaurant_id"));
					tg.addTaste(normalDetail);
				}
				
				tgs.put(tasteGroupId, tg);
			}
		}
		rs.close();
		
		return tgs.values().toArray(new TasteGroup[tgs.values().size()]);
	}
	
	private static TasteGroup[] getTasteGroupGeneral(ResultSet rs) throws SQLException{
		
		List<TasteGroup> tgs = new ArrayList<TasteGroup>();
		
		while(rs.next()){			
			Taste normal = null;
			if(rs.getInt("normal_taste_group_id") != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
				normal = new Taste();
				normal.setPreference(rs.getString("normal_taste_pref"));
				normal.setPrice(rs.getFloat("normal_taste_price"));
			}
			
			Taste tmp = null;
			if(rs.getBoolean("has_tmp_taste")){
				tmp = new Taste();
				tmp.setAliasId(rs.getInt("tmp_taste_id"));
				tmp.setTasteId(rs.getInt("tmp_taste_id"));
				tmp.setPreference(rs.getString("tmp_taste_pref"));
				tmp.setPrice(rs.getFloat("tmp_taste_price"));
			}				
			
			tgs.add(new TasteGroup(rs.getInt("taste_group_id"), normal, tmp));
		}
		rs.close();
		
		return tgs.toArray(new TasteGroup[tgs.size()]);
	}
	
}
