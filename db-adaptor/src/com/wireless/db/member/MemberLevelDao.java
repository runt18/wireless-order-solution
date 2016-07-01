package com.wireless.db.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.member.MemberLevel;
import com.wireless.pojo.member.MemberLevel.InsertBuilder;
import com.wireless.pojo.member.MemberLevel.UpdateBuilder;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.staffMgr.Staff;

public class MemberLevelDao {
	/**
	 * 获取低一级积分
	 * @param dbCon
	 * @param staff
	 * @param level
	 * @return
	 * @throws SQLException
	 */
	private static int getLowerPointThreshold(DBCon dbCon, Staff staff, int level) throws SQLException{
		int point = -1;
		String sql = "SELECT IFNULL(MAX(point_threshold), -1) FROM " + Params.dbName + ".member_level WHERE restaurant_id = " + staff.getRestaurantId() + " AND level_id < "+ level +" ORDER BY level_id DESC LIMIT 0, 1";
		try{
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				point = dbCon.rs.getInt(1);
			}
			return point;
		}finally{
			dbCon.rs.close();
		}

	}
	/**
	 * 获取高一级积分
	 * @param dbCon
	 * @param staff
	 * @param level
	 * @return
	 * @throws SQLException
	 */
	private static int getHigherPointThreshold(DBCon dbCon, Staff staff, int level) throws SQLException{
		int point = Integer.MAX_VALUE;
		String sql = "SELECT IFNULL(MAX(point_threshold),"+ Integer.MAX_VALUE +") FROM " + Params.dbName + ".member_level WHERE restaurant_id = " + staff.getRestaurantId() + " AND level_id > "+ level +" ORDER BY level_id LIMIT 0, 1";
		try{
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				point = dbCon.rs.getInt(1);
			}
			return point;
		}finally{
			dbCon.rs.close();
		}

	}
	/**
	 * 判断会员类型是否有所属
	 * @param dbCon
	 * @param staff
	 * @param typeId
	 * @throws SQLException
	 * @throws BusinessException
	 */
	private static void isMemberTypeBelong(DBCon dbCon, Staff staff, int id, int typeId) throws SQLException, BusinessException{
		String sql = "SELECT COUNT(*) FROM " + Params.dbName + ".member_level WHERE restaurant_id = " + staff.getRestaurantId() + " AND member_type_id = " + typeId + " AND id <> " + id;
		try{
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				if(dbCon.rs.getInt(1) > 0){
					throw new BusinessException(MemberError.MEMBER_TYPE_BELONG);
				}
			}
		}finally{
			dbCon.rs.close();
		}

	}
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
	public static int insert(Staff staff, InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
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
	public static int insert(DBCon dbCon, Staff staff, InsertBuilder builder) throws SQLException, BusinessException{
		MemberLevel memberLevel = builder.build();
		String sql;
		//判断是否会员类型已有所属
//		isMemberTypeBelong(dbCon, staff, memberLevel.getMemberType().getTypeId());
		//获取等级id
		sql = " SELECT IFNULL(MAX(level_id), 0) + 1 FROM " + Params.dbName + ".member_level WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int levelId = 0;
		if(dbCon.rs.next()){
			levelId = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		int minPoint = getLowerPointThreshold(dbCon, staff, levelId);
		//判断积分是否大于低等级
		if(memberLevel.getPointThreshold() <= minPoint){
			throw new BusinessException(MemberError.MEMBER_LEVEL_HIGHEST);
		}
		
		sql = "INSERT INTO " + Params.dbName + ".member_level(restaurant_id, level_id, point_threshold, member_type_id) VALUES( " +
				memberLevel.getRestaurantId() + "," +
				levelId + ", " +
				memberLevel.getPointThreshold() + "," +
				memberLevel.getMemberType().getId() + ")";
		
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
	 * 			throw if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if 该会员等级不存在或已被删除.
	 * 			throw if 积分小于低等级的.
	 * 			throw if 积分大于高等级的
	 */
	public static void update(Staff staff, UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon,staff, builder);
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
	public static void update(DBCon dbCon, Staff staff, UpdateBuilder builder) throws SQLException, BusinessException{
		MemberLevel memberLevel = builder.build();
		String sql;
		int levelId = 0;
		//判断会员类型是否有所属
		isMemberTypeBelong(dbCon, staff, memberLevel.getId(), memberLevel.getMemberType().getId());
		//获取自身等级id
		sql = "SELECT level_id FROM " + Params.dbName + ".member_level WHERE id = " + memberLevel.getId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			levelId = dbCon.rs.getInt(1);
		}else{
			throw new BusinessException(MemberError.MEMBER_LEVEL_NOT_EXIST);
		}
		dbCon.rs.close();
		
		//获取低一级积分
		int minPoint = getLowerPointThreshold(dbCon, staff, levelId);
		
		//获取高一级积分
		int maxPoint = getHigherPointThreshold(dbCon, staff, levelId);
		
		//判断修改积分是否符合条件
		if(minPoint >= memberLevel.getPointThreshold()){
			throw new BusinessException(MemberError.MEMBER_LEVEL_LESS_POINT);
		}else if(maxPoint <= memberLevel.getPointThreshold()){
			throw new BusinessException(MemberError.MEMBER_LEVEL_MORE_POINT);
		}
		sql = " UPDATE " + Params.dbName + ".member_level SET " +
			  " id = " + memberLevel.getId() +
			  (builder.isPointThresholdChange() ? " ,point_threshold = " + memberLevel.getPointThreshold() : "")+
			  (builder.isMemberTypeIdChange() ? " ,member_type_id = " + memberLevel.getMemberType().getId() : "") +
			  " WHERE id = " + memberLevel.getId();
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Delete the memberLevel.
	 * @param levelId
	 * @throws SQLException
	 * 			throw if failed to execute any SQL statement 
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
	 * @param staff
	 * 			the staff to perform this action
	 * @return the result of member level
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 	 		throws if the any member type in levels does NOT exist
	 */
	public static List<MemberLevel> get(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return get(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the list of memberLevel.
	 * @param dbCon
	 * 			the database connection 
	 * @param staff
	 * 			the staff to perform this action
	 * @return the result of member level
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 	 		throws if the any member type in levels does NOT exist
	 */
	public static List<MemberLevel> get(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		return getByCond(dbCon, staff, null, null);
	}
	
	/**
	 * Get the list of memberLevel by extra condition.
	 * @param dbCon
	 * @param restaurantId
	 * @param extraCond
	 * @param otherClause
	 * @return the result of member level
	 * @throws BusinessException 
	 * 	 		throws if the any member type in levels does NOT exist
	 */
	private static List<MemberLevel> getByCond(DBCon dbCon, Staff staff, String extraCond, String otherClause) throws SQLException, BusinessException{
		
		List<MemberLevel> result = new ArrayList<MemberLevel>();
		
		String sql = "SELECT ML.* FROM " + Params.dbName + ".member_level ML" +
					" WHERE ML.restaurant_id = " + staff.getRestaurantId() +
					(extraCond != null ? extraCond : " ")+
					(otherClause != null ? otherClause : " ORDER BY point_threshold");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			MemberLevel level = new MemberLevel(dbCon.rs.getInt("id"));
			MemberType type = MemberTypeDao.getById(staff, dbCon.rs.getInt("member_type_id"));
			level.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			level.setLevelId(dbCon.rs.getInt("level_id"));
			level.setPointThreshold(dbCon.rs.getInt("point_threshold"));
			level.setMemberType(type);
			result.add(level);
		}
		
		return result;
	}
	
	/**
	 * Get memberLevel by Id.
	 * @param id
	 * @param restaurantId
	 * @return
	 * @throws SQLException
	 * 			throw if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if 该会员等级不存在或已被删除
	 */
	public static MemberLevel getById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			List<MemberLevel> list = getByCond(dbCon, staff, " AND ML.id = " + id, null);
			if(list.isEmpty()){
				throw new BusinessException(MemberError.MEMBER_LEVEL_NOT_EXIST);
			}else{
				return list.get(0);
			}
		}finally{
			dbCon.disconnect();
		}
	}

}
