package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.DBTbl;
import com.wireless.db.Params;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.TasteError;
import com.wireless.pojo.dishesOrder.TasteGroup;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.DateType;

public class TasteGroupDao {

	public static class ExtraCond{
		private final DBTbl dbTbl;
		private int tgId;
		
		public ExtraCond(DateType dateType){
			this.dbTbl = new DBTbl(dateType);
		}
		
		public ExtraCond setTasteGroupId(TasteGroup tg){
			this.tgId = tg.getGroupId();
			return this;
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
	 * Archive the taste group.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param tgId4ArchiveFrom
	 * 			the id to taste group archived from
	 * @param archiveFrom
	 * 			the date type to archive from
	 * @param archiveTo
	 * 			the date type to archive to
	 * @return the id to taste group archived to
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 */
	public static int archive(DBCon dbCon, Staff staff, int tgId4ArchiveFrom, DateType archiveFrom, DateType archiveTo) throws SQLException{
		try{
			TasteGroup tg = getById(dbCon, staff, tgId4ArchiveFrom, archiveFrom);
			
			DBTbl toTbl = new DBTbl(archiveTo);
			
			String sql;
			sql = " INSERT INTO " + Params.dbName + "." + toTbl.tgTbl +
				  " ( " +
				  " `normal_taste_group_id`, `normal_taste_pref`, `normal_taste_price`, " +
				  " `tmp_taste_id`, `tmp_taste_pref`, `tmp_taste_price` " +
				  " ) VALUES (" +
				  TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID + ", " +
				  (tg.hasNormalTaste() ? ("'" + tg.getNormalTastePref() + "'") : "NULL") + ", " +
				  (tg.hasNormalTaste() ? tg.getNormalTastePrice() : "NULL") + ", " +
				  (tg.hasTmpTaste() ? tg.getTmpTaste().getTasteId() : "NULL") + ", " +
				  (tg.hasTmpTaste() ? "'" + tg.getTmpTastePref() + "'" : "NULL") + ", " +
				  (tg.hasTmpTaste() ? tg.getTmpTastePrice() : "NULL") +
				  ")";
			
			dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			dbCon.rs = dbCon.stmt.getGeneratedKeys();
			int tgId;
			if(dbCon.rs.next()){
				tgId = dbCon.rs.getInt(1);
			}else{
				throw new SQLException("The id of taste group is not generated successfully.");
			}
			
			//Update the normal taste group id.
			if(tg.hasNormalTaste()){
				sql = " UPDATE " + Params.dbName + "." + toTbl.tgTbl + " SET normal_taste_group_id = " + tgId + " WHERE taste_group_id = " + tgId;
				dbCon.stmt.executeUpdate(sql);
			}
			
			for(Taste normalTaste : tg.getNormalTastes()){
				sql = " INSERT INTO " + Params.dbName + "." + toTbl.ntgTbl +
					  " ( `normal_taste_group_id`, `taste_id` ) " +
					  " VALUES ( " +
					  " (SELECT normal_taste_group_id FROM " + Params.dbName + "." + toTbl.tgTbl + " WHERE taste_group_id = " + tgId + "), " +
					  normalTaste.getTasteId() + 
					  " ) ";
				dbCon.stmt.executeUpdate(sql);
			}
			
			return tgId;
			
		}catch(BusinessException e){
			e.printStackTrace();
			return TasteGroup.EMPTY_TASTE_GROUP_ID;
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
		ExtraCond extraCond = new ExtraCond(DateType.TODAY);
		
		String sql;
		sql = " INSERT INTO " + Params.dbName + "." + extraCond.dbTbl.tgTbl +
			  " ( " +
			  " `restaurant_id`, " +
			  " `normal_taste_group_id`, `normal_taste_pref`, `normal_taste_price`, " +
			  " `tmp_taste_id`, `tmp_taste_pref`, `tmp_taste_price` " +
			  " ) VALUES ( " +
			  staff.getRestaurantId() + ", " +
			  TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID + ", " +
			  (tg.hasNormalTaste() ? ("'" + tg.getNormalTastePref() + "'") : "NULL") + ", " +
			  (tg.hasNormalTaste() ? tg.getNormalTastePrice() : "NULL") + ", " +
			  (tg.hasTmpTaste() ? tg.getTmpTaste().getTasteId() : "NULL") + ", " +
			  (tg.hasTmpTaste() ? "'" + tg.getTmpTastePref() + "'" : "NULL") + ", " +
			  (tg.hasTmpTaste() ? tg.getTmpTastePrice() : "NULL") +
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		//get the generated id to taste group 
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int tgId;
		if(dbCon.rs.next()){
			tgId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of taste group is not generated successfully.");
		}
		
		//Update the normal taste group id.
		if(tg.hasNormalTaste()){
			sql = " UPDATE " + Params.dbName + "." + extraCond.dbTbl.tgTbl + " SET normal_taste_group_id = " + tgId + " WHERE taste_group_id = " + tgId;
			dbCon.stmt.executeUpdate(sql);
		}
		
		for(Taste normalTaste : tg.getNormalTastes()){
			sql = " INSERT INTO " + Params.dbName + "." + extraCond.dbTbl.ntgTbl +
				  " ( `normal_taste_group_id`, `taste_id` ) " +
				  " VALUES ( " +
				  " (SELECT normal_taste_group_id FROM " + Params.dbName + "." + extraCond.dbTbl.tgTbl + " WHERE taste_group_id = " + tgId + "), " +
				  normalTaste.getTasteId() + 
				  " ) ";
			dbCon.stmt.executeUpdate(sql);
		}
		
		return tgId;
	}
	
	public static List<TasteGroup> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		String sql;
		sql = " SELECT " +
			  " TG.taste_group_id, " +
			  " TG.normal_taste_group_id, TG.normal_taste_pref, TG.normal_taste_price, " +
			  " CASE WHEN (TG.tmp_taste_id IS NULL) THEN 0 ELSE 1 END AS has_tmp_taste, " +
			  " TG.tmp_taste_id, TG.tmp_taste_pref, TG.tmp_taste_price " +
			  " FROM " +  Params.dbName + "." + extraCond.dbTbl.tgTbl + " TG " + 
			  " WHERE 1 = 1 " + extraCond;

		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		final List<TasteGroup> result = new ArrayList<TasteGroup>();
		while(dbCon.rs.next()){
			int tgId = dbCon.rs.getInt("taste_group_id");
			
			int normalTasteGroupId;
			Taste normal = null;
			Taste tmp = null;
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
			
			//Get detail of each normal taste to this group in case of today
			List<Taste> normalTastes = null;
			if(normalTasteGroupId != TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID){
				sql = " SELECT taste_id FROM " + Params.dbName + "." + extraCond.dbTbl.ntgTbl + " WHERE normal_taste_group_id = " + normalTasteGroupId;
				normalTastes = TasteDao.getTastes(staff, " AND TASTE.taste_id IN (" + sql + ")", null);
			}
			
			result.add(new TasteGroup(tgId, normal, normalTastes, tmp));
		}
		dbCon.rs.close();

		return result;
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
		List<TasteGroup> result = getByCond(dbCon, staff, new ExtraCond(dateType).setTasteGroupId(tgId));
		if(result.isEmpty()){
			throw new BusinessException(TasteError.TASTE_GROUP_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Delete the taste group according to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to taste group deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		int amount = 0;
		for(TasteGroup tg : getByCond(dbCon, staff, extraCond)){
			String sql;
			//Delete the associated normal taste.
			sql = " DELETE NTG " + 
				  " FROM " + Params.dbName + "." + extraCond.dbTbl.ntgTbl + " NTG " +
				  " JOIN " + Params.dbName + "." + extraCond.dbTbl.tgTbl + " TG ON NTG.normal_taste_group_id = TG.normal_taste_group_id " +
				  " WHERE TG.taste_group_id = " + tg.getGroupId() +
				  " AND TG.normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID;
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the taste group.
			sql = " DELETE FROM " + Params.dbName + "." + extraCond.dbTbl.tgTbl + " WHERE taste_group_id = " + tg.getGroupId() + " AND taste_group_id <> " + TasteGroup.EMPTY_TASTE_GROUP_ID;
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		return amount;
	}
	
	public static class ArchiveResult{
		private final int tgAmount;
		private final int ntgAmount;
		private final int tgMaxId;
		private final int ntgMaxId;
		
		public ArchiveResult(int tgAmount, int ntgAmount, int tgMaxId, int ntgMaxId){
			this.tgAmount = tgAmount;
			this.ntgAmount = ntgAmount;
			this.tgMaxId = tgMaxId;
			this.ntgMaxId = ntgMaxId;
		}
		public int getTgAmount(){
			return this.tgAmount;
		}
		
		public int getNTgAmount(){
			return this.ntgAmount;
		}
		
		public int getTgMaxId(){
			return this.tgMaxId;
		}
		
		public int getNTgMaxId(){
			return this.ntgMaxId;
		}
		
		@Override
		public String toString(){
			return tgAmount + " taste group record(s) are moved to history, maxium id : " + tgMaxId + System.getProperty("line.separator") +
				   ntgAmount + " normal taste group record(s) are moved to history, maxium id : " + ntgMaxId;
		}
	}
	
//	static ArchiveResult archive(DBCon dbCon, Staff staff, String paidOrder) throws SQLException{
//		
//		String sql;
//		
//		final String tgItem = "`taste_group_id`, `normal_taste_group_id`, `normal_taste_pref`, `normal_taste_price`, " +
//				  			  "`tmp_taste_id`, `tmp_taste_pref`, `tmp_taste_price`";
//		
//		//Move the taste group to history
//		sql = " INSERT INTO " + Params.dbName + ".taste_group_history (" + tgItem + " ) " +
//			  " SELECT " + tgItem + " FROM " + Params.dbName + ".taste_group" +
//			  " WHERE " +
//			  " taste_group_id <> " + TasteGroup.EMPTY_TASTE_GROUP_ID +
//			  " AND taste_group_id IN (" +
//			  " SELECT taste_group_id FROM " + Params.dbName + ".order_food WHERE order_id IN (" + paidOrder + " ) " +
//			  " ) ";
//		
//		int tgAmount = dbCon.stmt.executeUpdate(sql);
//		
//		final String ntgItem = "`normal_taste_group_id`, `taste_id`";
//		//Move the normal taste group to history.
//		sql = " INSERT INTO " + Params.dbName + ".normal_taste_group_history (" + ntgItem + ")" +
//			  " SELECT " + ntgItem + " FROM " + Params.dbName + ".normal_taste_group" +
//			  " WHERE " +
//			  " normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID +
//			  " AND " +
//			  " normal_taste_group_id IN (" +
//				  " SELECT normal_taste_group_id " +
//				  " FROM " + Params.dbName + ".order_food OF " + 
//				  " JOIN " + Params.dbName + ".taste_group TG ON OF.taste_group_id = TG.taste_group_id " +
//				  " WHERE OF.order_id IN (" + paidOrder + ")" +
//			  " ) ";
//		
//		int ntgAmount = dbCon.stmt.executeUpdate(sql);
//		
//		//Calculate the max taste group id from both today and history.
//		int maxTgId = 0;
//		sql = " SELECT MAX(taste_group_id) + 1 " +
//			  " FROM " +
//			  " (SELECT MAX(taste_group_id) AS taste_group_id FROM " + Params.dbName + ".taste_group" +
//			  " UNION " +
//			  " SELECT MAX(taste_group_id) AS taste_group_id FROM " + Params.dbName + ".taste_group_history) AS all_taste_group";
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		if(dbCon.rs.next()){
//			maxTgId = dbCon.rs.getInt(1);
//		}
//		dbCon.rs.close();
//		
//		//Calculate the max normal taste group id from both today and history
//		int maxNormalTgId = 0;
//		sql = " SELECT MAX(normal_taste_group_id) + 1 " +
//			  " FROM " +
//			  " (SELECT MAX(normal_taste_group_id) AS normal_taste_group_id FROM " + Params.dbName + ".normal_taste_group" +
//			  " UNION " +
//			  " SELECT MAX(normal_taste_group_id) AS normal_taste_group_id FROM " + Params.dbName + ".normal_taste_group_history) AS all_normal_taste_group";
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		if(dbCon.rs.next()){
//			maxNormalTgId = dbCon.rs.getInt(1);
//		}
//		dbCon.rs.close();
//		
//		//Update the max taste group id
//		sql = " DELETE FROM " + Params.dbName + ".taste_group WHERE restaurant_id = " + Restaurant.ADMIN;
//		dbCon.stmt.executeUpdate(sql);
//		
//		//Insert a record with the max taste group id and max normal taste group id.
//		sql = " INSERT INTO " + Params.dbName + ".taste_group" +
//			  " (`taste_group_id`, `normal_taste_group_id`, `restaurant_id`) " +
//			  " VALUES " +
//			  " ( " +
//			  maxTgId + ", " +
//			  maxNormalTgId + ", " +
//			  Restaurant.ADMIN +
//			  " ) ";
//		dbCon.stmt.executeUpdate(sql);
//		
//		//Delete the paid order normal taste group except the empty normal taste group
//		sql = " DELETE FROM " + Params.dbName + ".normal_taste_group " +
//			  " WHERE " +
//			  " normal_taste_group_id IN (" +
//			  " SELECT normal_taste_group_id " +
//			  " FROM " + Params.dbName + ".order_food OF " + " JOIN " + Params.dbName + ".taste_group TG" +
//			  " ON OF.taste_group_id = TG.taste_group_id " +
//			  " WHERE " +
//			  " OF.order_id IN (" + paidOrder + ")" +
//			  " ) " + 
//			  " AND " +
//			  " normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID;
//		dbCon.stmt.executeUpdate(sql);
//		
//		//Delete the paid order taste group except the empty taste group
//		sql = " DELETE FROM " + Params.dbName + ".taste_group" +
//			  " WHERE " +
//			  " taste_group_id IN (" +
//			  " SELECT taste_group_id FROM " + Params.dbName + ".order_food" +
//			  " WHERE " + 
//			  " order_id IN (" + paidOrder + ")" +
//			  " ) " + 
//			  " AND " +
//			  " taste_group_id <> " + TasteGroup.EMPTY_TASTE_GROUP_ID;
//		dbCon.stmt.executeUpdate(sql);	
//		
//		return new ArchiveResult(tgAmount, ntgAmount, maxTgId, maxNormalTgId);
//	}
	
	public static class CleanupResult{
		private final long elapsedTime;
		private final int nTasteGroup;
		private final int nNormalTasteGroup;
		
		public CleanupResult(long elapsedTime, int nTasteGroup, int nNormalTasteGroup) {
			this.elapsedTime = elapsedTime;
			this.nTasteGroup = nTasteGroup;
			this.nNormalTasteGroup = nNormalTasteGroup;
		}
		
	
		@Override
		public String toString(){
			return "The clean up to " + nTasteGroup + " taste group(s), " + nNormalTasteGroup + " normal taste group(s) takes " + elapsedTime + " sec.";
		}
	}
	
	public static CleanupResult cleanup() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			long beginTime = System.currentTimeMillis();
			dbCon.connect();
			String sql;
			sql = " DELETE TG FROM " + Params.dbName + ".taste_group TG " +
				  " LEFT JOIN " + Params.dbName + ".order_food OF ON TG.taste_group_id = OF.taste_group_id " +
				  " WHERE 1 = 1 " +
				  " AND TG.taste_group_id <> 1 " +
				  " AND TG.restaurant_id <> " + Restaurant.ADMIN + 
				  " AND OF.id IS NULL ";
			int nTasteGroup = dbCon.stmt.executeUpdate(sql);
			
			sql = " DELETE NTG FROM " + Params.dbName + ".normal_taste_group NTG " +
				  " LEFT JOIN " + Params.dbName + ".taste_group TG ON NTG.normal_taste_group_id = TG.normal_taste_group_id " +
				  " WHERE 1 = 1 " +
				  " AND NTG.normal_taste_group_id <> 1 " +
				  " AND TG.normal_taste_group_id IS NULL ";
			int nNormalTasteGroup = dbCon.stmt.executeUpdate(sql);
			
			long elapsedTime = (System.currentTimeMillis() - beginTime) / 1000;
			
			return new CleanupResult(elapsedTime, nTasteGroup, nNormalTasteGroup);
			
		}finally{
			dbCon.disconnect();
		}
	}
}
