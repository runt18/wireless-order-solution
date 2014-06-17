package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.TasteError;
import com.wireless.pojo.dishesOrder.TasteGroup;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.util.DateType;

public class TasteGroupDao {

	public static class ExtraCond{
		private final DateType dateType;
		private final String tgTbl;
		private int tgId;
		
		public ExtraCond(DateType dateType){
			this.dateType = dateType;
			if(this.dateType.isHistory()){
				tgTbl = "taste_group_history";
			}else{
				tgTbl = "taste_group";
			}
		}
		
		public ExtraCond setTasteGroupId(int tgId){
			this.tgId = tgId;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(tgId > 0){
				extraCond.append(" AND TG.taste_group_id = " + tgId);
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert the taste group according to builder {@link TasteGroup#InsertBuilder}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder to taste group
	 * @return the id to taste group just insert
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, TasteGroup.InsertBuilder builder) throws SQLException{
		TasteGroup tg = builder.build();
		
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".taste_group " +
			  " ( " +
			  " `normal_taste_group_id`, `normal_taste_pref`, `normal_taste_price`, " +
			  " `tmp_taste_id`, `tmp_taste_pref`, `tmp_taste_price` " +
			  " ) " +
			  " SELECT " +
			  (tg.hasNormalTaste() ? "MAX(normal_taste_group_id) + 1" : TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID) + ", " +
			  (tg.hasNormalTaste() ? ("'" + tg.getNormalTastePref() + "'") : "NULL") + ", " +
			  (tg.hasNormalTaste() ? tg.getNormalTastePrice() : "NULL") + ", " +
			  (tg.hasTmpTaste() ? tg.getTmpTaste().getTasteId() : "NULL") + ", " +
			  (tg.hasTmpTaste() ? "'" + tg.getTmpTastePref() + "'" : "NULL") + ", " +
			  (tg.hasTmpTaste() ? tg.getTmpTastePrice() : "NULL") +
			  " FROM " + Params.dbName + ".taste_group LIMIT 1";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		//get the generated id to taste group 
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int tgId;
		if(dbCon.rs.next()){
			tgId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of taste group is not generated successfully.");
		}
		
		for(Taste normalTaste : tg.getNormalTastes()){
			sql = " INSERT INTO " + Params.dbName + ".normal_taste_group " +
				  " ( " +
				  " `normal_taste_group_id`, `taste_id` " +
				  " ) " +
				  " VALUES " +
				  " ( " +
				  " (SELECT normal_taste_group_id FROM " + Params.dbName + ".taste_group WHERE taste_group_id = " + tgId + "), " +
				  normalTaste.getTasteId() + 
				  " ) ";
			dbCon.stmt.executeUpdate(sql);
		}
		
		return tgId;
	}
	
	/**
	 * Get the taste group from according to the specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param tasteGroupId
	 * 			the taste group id
	 * @param DateType 
	 * 			indicates which date type use{@link DateType}
	 * @return the {@link TasteGroup} to specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the taste group to this specific id does NOT exist
	 */
	public static TasteGroup getById(Staff staff, int tgId, DateType dateType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, tgId, dateType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the taste group from according to the specific id.
	 * @param dbCon  
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param tasteGroupId
	 * 			the taste group id
	 * @param DateType 
	 * 			indicates which date type use{@link DateType}
	 * @return the {@link TasteGroup} to specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the taste group to this specific id does NOT exist
	 */
	public static TasteGroup getById(DBCon dbCon, Staff staff, int tgId, DateType dateType) throws SQLException, BusinessException{
		
		ExtraCond extraCond = new ExtraCond(dateType).setTasteGroupId(tgId);
		
		String sql;
		
		sql = " SELECT " +
			  " TG.taste_group_id, " +
			  " TG.normal_taste_group_id, TG.normal_taste_pref, TG.normal_taste_price, " +
			  " CASE WHEN (TG.tmp_taste_id IS NULL) THEN 0 ELSE 1 END AS has_tmp_taste, " +
			  " TG.tmp_taste_id, TG.tmp_taste_pref, TG.tmp_taste_price " +
			  " FROM " +  Params.dbName + "." + extraCond.tgTbl + " TG " + 
			  " WHERE 1 = 1 " + extraCond;

		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		int normalTasteGroupId;
		Taste normal = null;
		Taste tmp = null;

		if(dbCon.rs.next()){
			//Get the normal taste in case of exist.
			normalTasteGroupId = dbCon.rs.getInt("normal_taste_group_id");
			if(normalTasteGroupId != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
				normal = new Taste(0);
				normal.setPreference(dbCon.rs.getString("normal_taste_pref"));
				normal.setPrice(dbCon.rs.getFloat("normal_taste_price"));
			}
			
			//Get the temporary taste in case of exist.
			if(dbCon.rs.getBoolean("has_tmp_taste")){
				tmp = new Taste(0);
				tmp.setTasteId(dbCon.rs.getInt("tmp_taste_id"));
				tmp.setPreference(dbCon.rs.getString("tmp_taste_pref"));
				tmp.setPrice(dbCon.rs.getFloat("tmp_taste_price"));
			}				
			
		}else{
			throw new BusinessException(TasteError.TASTE_GROUP_NOT_EXIST);
		}
		dbCon.rs.close();

		//Get detail of each taste to this group.
		List<Taste> normalTastes = null;
		if(normalTasteGroupId != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
			sql = " SELECT taste_id FROM " + Params.dbName + ".normal_taste_group WHERE normal_taste_group_id = " + normalTasteGroupId;
			normalTastes = TasteDao.getTastes(dbCon, staff, " AND TASTE.taste_id IN (" + sql + ")", null);
		}
		
		return new TasteGroup(tgId, normal, normalTastes, tmp);
	}
	
}
