package com.wireless.db.client.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.client.MemberLevel;
import com.wireless.pojo.client.MemberLevel.InsertBuilder;
import com.wireless.pojo.client.MemberLevel.UpdateBuilder;

public class MemberLevelDao {

	/**
	 * Insert a new memberLevel.
	 * @param builder
	 * 			the detail of memberLevel
	 * @param restaurantId
	 * 			the restaurantId
	 * @return	the id of memberLevel just create
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			
	 */
	public static int insert(InsertBuilder builder, int restaurantId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, builder, restaurantId);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * 
	 * @param dbCon
	 * @param builder
	 * @param restaurantId
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int insert(DBCon dbCon, InsertBuilder builder, int restaurantId) throws SQLException, BusinessException{
		MemberLevel memberLevel = builder.build();
		String sql;
		//判断是否会员类型已有所属
		sql = "SELECT * FROM " + Params.dbName + ".member_level WHERE restaurant_id = " + restaurantId + " AND member_type_id = " + memberLevel.getMemberTypeId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(MemberError.MEMBER_TYPE_BELONG);
		}
		dbCon.rs.close();
		//获取等级id
		sql = " SELECT IFNULL(MAX(level_id), 0) + 1 FROM " + Params.dbName + ".member_level WHERE restaurant_id = " + restaurantId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int levelId = 0;
		if(dbCon.rs.next()){
			levelId = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		//判断积分是否大于低等级
		sql = "SELECT IF(" + memberLevel.getPointThreshold() + " > (SELECT IFNULL(MAX(point_threshold), -1) FROM " + Params.dbName + ".member_level WHERE restaurant_id = " + restaurantId + " AND level_id < "+ levelId +" LIMIT 0, 1), " + memberLevel.getPointThreshold() + ", -1)" +
				" FROM " + Params.dbName + ".member_level WHERE restaurant_id = " + restaurantId ;
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(!dbCon.rs.next() || dbCon.rs.getInt(1) < 0){
			throw new BusinessException(MemberError.MEMBER_LEVEL_LESS_POINT);
		}
		dbCon.rs.close();
		
		sql = "INSERT INTO " + Params.dbName + ".member_level(restaurant_id, level_id, point_threshold, member_type_id) VALUES( " +
				memberLevel.getRestaurantId() + "," +
				levelId + ", " +
				memberLevel.getPointThreshold() + "," +
				memberLevel.getMemberTypeId() + ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int memberLevelId = 0;
		if(dbCon.rs.next()){
			memberLevelId = dbCon.rs.getInt(1);
		}else{
			throw new BusinessException("The id of member is not generated successfully.");
		}
		return memberLevelId;
	}
	
	/**
	 * Update the memberLevel.
	 * @param builder
	 * 			the detail of memberLevel
	 * @param restaurantId
	 * 			the restaurantId
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void update(UpdateBuilder builder, int restaurantId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, builder, restaurantId);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * 
	 * @param dbCon
	 * @param builder
	 * @param restaurantId
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void update(DBCon dbCon, UpdateBuilder builder, int restaurantId) throws SQLException, BusinessException{
		MemberLevel memberLevel = builder.build();
		String sql;
		int levelId = 0;
		//判断会员类型是否有所属
		sql = "SELECT * FROM " + Params.dbName + ".member_level WHERE restaurant_id = " + restaurantId + " AND member_type_id = " + memberLevel.getMemberTypeId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(MemberError.MEMBER_TYPE_BELONG);
		}
		dbCon.rs.close();
		//获取等级id
		sql = "SELECT level_id FROM " + Params.dbName + ".member_level WHERE id = " + memberLevel.getId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			levelId = dbCon.rs.getInt(1);
		}else{
			throw new BusinessException(MemberError.MEMBER_LEVEL_NOT_EXIST);
		}
		dbCon.rs.close();
		//获取低一级积分
		int minPoint = -1, maxPoint = 2147483640;
		sql = "SELECT IFNULL(MAX(point_threshold), -1) FROM " + Params.dbName + ".member_level WHERE restaurant_id = " + restaurantId + " AND level_id < "+ levelId +" LIMIT 0, 1";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			minPoint = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		//获取高一级积分
		sql = "SELECT IFNULL(MAX(point_threshold), 2147483640) FROM " + Params.dbName + ".member_level WHERE restaurant_id = " + restaurantId + " AND level_id > "+ levelId +" LIMIT 0, 1";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			maxPoint = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		//判断修改积分是否符合条件
		if(minPoint > memberLevel.getPointThreshold()){
			throw new BusinessException(MemberError.MEMBER_LEVEL_LESS_POINT);
		}else if(maxPoint < memberLevel.getPointThreshold()){
			throw new BusinessException(MemberError.MEMBER_LEVEL_MORE_POINT);
		}
		sql = "UPDATE " + Params.dbName + ".member_level SET " +
						" id = " + memberLevel.getId() +
						(builder.isPointThresholdChange()?" ,point_threshold = " + memberLevel.getPointThreshold() : "")+
						(builder.isMemberTypeIdChange()?" ,member_type_id = " + memberLevel.getMemberTypeId() : "") +
					  " WHERE id = " + memberLevel.getId();
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Delete the memberLevel.
	 * @param levelId
	 * @throws SQLException
	 */
	public static void delete(int levelId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			delete(dbCon, levelId);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * 
	 * @param dbCon
	 * @param memberLevelId
	 * @throws SQLException
	 */
	public static void delete(DBCon dbCon, int memberLevelId) throws SQLException{
		String sql = "DELETE FROM " + Params.dbName + ".member_level WHERE id = " + memberLevelId;
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Get the list of memberLevel.
	 * @param restaurantId
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberLevel> getMemberLevels(int restaurantId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getMemberLevels(dbCon,restaurantId,null, null);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Get the list of memberLevel by extra condition.
	 * @param dbCon
	 * @param restaurantId
	 * @param extraCond
	 * @param otherClause
	 * @return
	 * @throws SQLException
	 */
	private static List<MemberLevel> getMemberLevels(DBCon dbCon, int restaurantId, String extraCond, String otherClause) throws SQLException{
		List<MemberLevel> list = new ArrayList<MemberLevel>();
		String sql = "SELECT ML.*, MT.name FROM " + Params.dbName + ".member_level ML" +
					" JOIN " + Params.dbName + ".member_type MT ON MT.member_type_id = ML.member_type_id " +
					" WHERE ML.restaurant_id = " + restaurantId +
					(extraCond != null ? extraCond : "")+
					(otherClause != null ? otherClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			MemberLevel mLevel = new MemberLevel();
			mLevel.setId(dbCon.rs.getInt("id"));
			mLevel.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			mLevel.setLevelId(dbCon.rs.getInt("level_id"));
			mLevel.setPointThreshold(dbCon.rs.getInt("point_threshold"));
			mLevel.setMemberTypeId(dbCon.rs.getInt("member_type_id"));
			mLevel.setMemberTypeName(dbCon.rs.getString("name"));
			list.add(mLevel);
		}
		return list;
	}
	
	/**
	 * Get memberLevel by Id.
	 * @param id
	 * @param restaurantId
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static MemberLevel getMemberLevelById(int id, int restaurantId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			List<MemberLevel> list = getMemberLevels(dbCon, restaurantId, " AND ML.id = " + id, null);
			if(!list.isEmpty()){
				return list.get(0);
			}else{
				throw new BusinessException(MemberError.MEMBER_LEVEL_NOT_EXIST);
			}
		}finally{
			dbCon.disconnect();
		}
	}

}
