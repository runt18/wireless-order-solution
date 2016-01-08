package com.wireless.db.weixin.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.action.WxKeyword;
import com.wireless.pojo.weixin.action.WxMenuAction;

public class WxKeywordDao {

	public static class ExtraCond{
		
		private int id;
		private int actionId;
		private String keyword;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setKeyword(String keyword){
			this.keyword = keyword;
			return this;
		}
		
		public ExtraCond setAction(int actionId){
			this.actionId = actionId;
			return this;
		}
		
		public ExtraCond setAction(WxMenuAction action){
			this.actionId = action.getId();
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND id = " + id);
			}
			if(keyword != null && !keyword.isEmpty()){
				extraCond.append(" AND keyword = %" + keyword + "%");
			}
			if(actionId != 0){
				extraCond.append(" AND action_id = " + actionId);
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert the wx keyword to specific builder {@link WxKeyword#InsertBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to wx keyword {@link WxKeyword#InsertBuilder}
	 * @return the id to keyword just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, WxKeyword.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the wx keyword to specific builder {@link WxKeyword#InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to wx keyword {@link WxKeyword#InsertBuilder}
	 * @return the id to keyword just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, WxKeyword.InsertBuilder builder) throws SQLException{
		WxKeyword keyword = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".weixin_keyword " +
			  " (restaurant_id, keyword, type) VALUES ( " +
			  staff.getRestaurantId() + "," +
			  "'" + keyword.getKeyword() + "'," +
			  keyword.getType().getVal() +
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int id = 0;
		if(dbCon.rs.next()){
			id = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The weixin keyword id is NOT generated successfully.");
		}
		dbCon.rs.close();
		
		return id;
	}

	/**
	 * Update the weixin keyword to update builder {@link WxKeyword#UpdateBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the keyword to update does NOT exist
	 */
	public static void update(Staff staff, WxKeyword.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the weixin keyword to update builder {@link WxKeyword#UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the keyword to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, WxKeyword.UpdateBuilder builder) throws SQLException, BusinessException{
		WxKeyword keyword = builder.build();
		String sql;
		sql = " UPDATE " + Params.dbName + ".weixin_keyword SET " +
			  " id = " + keyword.getId() +
			  (builder.isKeywordChanged() ? " ,keyword = '" + keyword.getKeyword() + "'" : "") +
			  (builder.isActionChanged() ? " ,action_id = " + keyword.getActionId() : "") +
			  " WHERE id = " + keyword.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException("关键字信息不存在");
		}
	}
	
	/**
	 * Get the wx keywords to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the weixin keyword result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<WxKeyword> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the wx keywords to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the weixin keyword result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<WxKeyword> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".weixin_keyword WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		final List<WxKeyword> result = new ArrayList<WxKeyword>();
		while(dbCon.rs.next()){
			WxKeyword keyword = new WxKeyword(dbCon.rs.getInt("id"));
			keyword.setKeyword(dbCon.rs.getString("keyword"));
			keyword.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			keyword.setType(WxKeyword.Type.valueOf(dbCon.rs.getInt("type")));
			keyword.setActionId(dbCon.rs.getInt("action_id"));
			result.add(keyword);
		}
		dbCon.rs.close();
		return result;
	}

	/**
	 * Delete the weixin keyword to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to keyword deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin keyword does NOT exist
	 */
	public static void deleteById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the weixin keyword to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to keyword deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin keyword does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(id)) == 0){
			throw new BusinessException("关键字信息不存在");
		}
	}
	
	/**
	 * Delete the wx keywords to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount to weixin keywords deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return deleteByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the wx keywords to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount to weixin keywords deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(WxKeyword keyword : getByCond(dbCon, staff, extraCond)){
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".weixin_keyword WHERE id = " + keyword.getId();
			if(dbCon.stmt.executeUpdate(sql) == 1){
				amount++;
			}
		}
		return amount;
	}
}
