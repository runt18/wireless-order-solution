package com.wireless.db.client.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Staff;

public class MemberTypeDao {

	/**
	 * Insert a new member type.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new member type
	 * @return the id to member type just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, MemberType.InsertBuilder builder) throws SQLException{
		MemberType mt = builder.build();
		// 插入新数据
		String sql = " INSERT INTO " + Params.dbName + ".member_type " 
				   + " ( restaurant_id, name, type, exchange_rate, charge_rate, attribute, initial_point )"
				   + " VALUES("
				   + mt.getRestaurantId() + ","	
				   + "'" + mt.getName() + "',"
				   + mt.getType().getVal() + "," +
				   + mt.getExchangeRate() + ","	
				   + mt.getChargeRate() + "," 
				   + mt.getAttribute().getVal() + "," 
				   + mt.getInitialPoint()
				   + ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			mt.setId(dbCon.rs.getInt(1));
		}else{
			throw new SQLException("Failed to generated the discount id.");
		}
		
		//Insert the discounts associated with this member type.
		for(Discount discount : mt.getDiscounts()){
			sql = " INSERT INTO " + Params.dbName + ".member_type_discount" +
				  " (`member_type_id`, `discount_id`, `type`) " +
				  " VALUES (" +
				  mt.getId() + "," +
				  discount.getId() + "," +
				  MemberType.DiscountType.NORMAL.getVal() + 
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}

		//Update the default discount.
		sql = " UPDATE " + Params.dbName + ".member_type_discount" +
			  " SET type = " + MemberType.DiscountType.DEFAULT.getVal() +
			  " WHERE member_type_id = " + mt.getId() +
			  " AND discount_id = " + mt.getDefaultDiscount().getId();
		dbCon.stmt.executeUpdate(sql);

		return mt.getId();
	}
	
	/**
	 * Insert a new member type.
	 * @param builder
	 * 			the builder to insert a new member type
	 * @param staff
	 * 			the staff to perform this action
	 * @return the id to member type just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, MemberType.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = insert(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * Delete a member type according to specific id
	 * @param dbCon
	 * 			the database connection
	 * @param memberTypeId
	 * 			the member type id to delete
	 * @param staff
	 * 			the staff to perform this action
	 * @throws BusinessException
	 * 			throws if the member type does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int memberTypeId) throws BusinessException, SQLException{
		String sql;
		sql = "SELECT * FROM " + Params.dbName + ".member WHERE restaurant_id = " + staff.getRestaurantId() + " AND member_type_id = " + memberTypeId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(MemberError.TYPE_DELETE_ISNOT_EMPTY);
		}
		//Delete the discounts associated with this member type.
		sql = " DELETE FROM " + Params.dbName + ".member_type_discount WHERE member_type_id = " + memberTypeId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the member type.
		sql = " DELETE FROM " + Params.dbName + ".member_type WHERE member_type_id = " + memberTypeId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(MemberError.TYPE_DELETE_ISNOT_EMPTY);
		}
	}
	
	/**
	 * Delete a member type according to specific id
	 * @param memberTypeId
	 * 			the member type id to delete
	 * @param staff
	 * 			the staff to perform this action
	 * @throws BusinessException
	 * 			throws if the member type does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void deleteById(Staff staff, int memberTypeId) throws BusinessException, SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			deleteById(dbCon, staff, memberTypeId);
			dbCon.conn.commit();
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Update the member type.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to update a member type
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member type to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, MemberType.UpdateBuilder builder) throws SQLException, BusinessException{
		String sql;
		// 更新数据
		sql = " UPDATE " + Params.dbName + ".member_type SET " +
			  (builder.isNameChanged() ? " name = '" + builder.getName() + "', " : "") +
			  (builder.isExchangRateChanged() ? " exchange_rate = " + builder.getExchangeRate() + ", " : "") +
			  (builder.isChargeRateChanged() ? " charge_rate = " + builder.getChargeRate() + ", " : "") + 
			  (builder.isAttributeChanged() ? " attribute = " + builder.getAttribute().getVal() + "," : "") + 
			  (builder.isInitialPointChanged() ? " initial_point = " + builder.getInitialPoint() : "") +
			  " WHERE restaurant_id = " + staff.getRestaurantId() +
			  " AND member_type_id = " + builder.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(MemberError.MEMBER_TYPE_NOT_EXIST);
		}
		
		if(builder.isDiscountChanged() || builder.isDefaultDiscountChanged()){
			sql = " DELETE FROM " + Params.dbName + ".member_type_discount WHERE member_type_id = " + builder.getId();
			dbCon.stmt.executeUpdate(sql); 
			
			//Insert the discounts associated with this member type.
			for(Discount discount : builder.getDiscounts()){
				sql = " INSERT INTO " + Params.dbName + ".member_type_discount" +
					  " (`member_type_id`, `discount_id`, `type`) " +
					  " VALUES (" +
					  builder.getId() + "," +
					  discount.getId() + "," +
					  MemberType.DiscountType.NORMAL.getVal() +
					  ")";
				dbCon.stmt.executeUpdate(sql);
			}

			//Update the default discount.
			sql = " UPDATE " + Params.dbName + ".member_type_discount" +
				  " SET type = " + MemberType.DiscountType.DEFAULT.getVal() +
				  " WHERE member_type_id = " + builder.getId() +
				  " AND discount_id = " + builder.getDefaultDiscount().getId();
			dbCon.stmt.executeUpdate(sql);
		}
	}
	
	/**
	 * Update the member type.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to update a member type
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member type to update does NOT exist
	 */
	public static void update(Staff staff, MemberType.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberTypeDao.update(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member type according to specific extra condition and order clause.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause	
	 * 			the order clause
	 * @return the result list
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if any discount associated with the member type is NOT found
	 */
	public static List<MemberType> getMemberType(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException, BusinessException{
		List<MemberType> result = new ArrayList<MemberType>();
		String sql;
		sql = " SELECT " +
			  " member_type_id, restaurant_id, exchange_rate, charge_rate, name, attribute, initial_point, type " +
			  " FROM " + Params.dbName + ".member_type MT " +
			  " WHERE 1 = 1 " +
			  " AND MT.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			MemberType mt = new MemberType(dbCon.rs.getInt("member_type_id"));
			mt.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			mt.setName(dbCon.rs.getString("name"));
			mt.setType(MemberType.Type.valueOf(dbCon.rs.getInt("type")));
			mt.setChargeRate(dbCon.rs.getFloat("charge_rate"));
			mt.setExchangeRate(dbCon.rs.getFloat("exchange_rate"));
			mt.setAttribute(dbCon.rs.getInt("attribute"));
			mt.setInitialPoint(dbCon.rs.getInt("initial_point"));
			
			result.add(mt);
		}
		dbCon.rs.close();
		
		for(MemberType eachType : result){
			sql = " SELECT discount_id, type FROM " + Params.dbName + ".member_type_discount WHERE member_type_id = " + eachType.getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Discount distToMemberType = DiscountDao.getDiscountById(staff, dbCon.rs.getInt("discount_id"));
				eachType.addDiscount(distToMemberType);
				if(MemberType.DiscountType.valueOf(dbCon.rs.getInt("type")) == MemberType.DiscountType.DEFAULT){
					eachType.setDefaultDiscount(distToMemberType);
				}
			}
			dbCon.rs.close();
		}
		return result;
	}
	
	/**
	 * Get the member type according to specific extra condition and order clause.
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause	
	 * 			the order clause
	 * @return the result list
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if any discount associated with the member type is NOT found
	 */
	public static List<MemberType> getMemberType(Staff staff, String extraCond, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberTypeDao.getMemberType(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member type according to a specified id
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the member type id
	 * @return	the member type to a specified id
	 * @throws SQLException
	 * 			throws if fails to execute any SQL statements
	 * @throws BusinessException
	 * 			throws if the member type to this specified id is NOT found
	 */
	public static MemberType getMemberTypeById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		List<MemberType> list = MemberTypeDao.getMemberType(dbCon, staff, " AND MT.member_type_id = " + id, null);
		if(list.isEmpty()){
			throw new BusinessException(MemberError.MEMBER_TYPE_NOT_EXIST);
		}else{
			return list.get(0);
		}
	}
	
	/**
	 * Get the member type according to a specified id
	 * @param id
	 * 			the member type id
	 * @param staff
	 * 			the staff to perform this action
	 * @return	the member type to a specified id
	 * @throws SQLException
	 * 			throws if fails to execute any SQL statements
	 * @throws BusinessException
	 * 			throws if the member type to this specified id is NOT found
	 */
	public static MemberType getMemberTypeById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberTypeDao.getMemberTypeById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member type belongs to weixin.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the member type to weixin
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member type does NOT exist
	 */
	public static MemberType getWeixinMemberType(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getWeixinMemberType(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member type belongs to weixin.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the member type to weixin
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member type does NOT exist
	 */
	public static MemberType getWeixinMemberType(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		List<MemberType> list = MemberTypeDao.getMemberType(dbCon, staff, " AND MT.type = " + MemberType.Type.WEIXIN.getVal(), null);
		if(list.isEmpty()){
			throw new BusinessException(MemberError.MEMBER_TYPE_NOT_EXIST);
		}else{
			return list.get(0);
		}
	}
	
	public static List<MemberType> getNotBelongMemberType(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getNotBelongMemberType(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<MemberType> getNotBelongMemberType(DBCon dbCon, Staff staff) throws SQLException{
		List<MemberType> list = new ArrayList<MemberType>();
		String sql = "SELECT member_type_id, name, attribute FROM " + Params.dbName + ".member_type WHERE restaurant_id = "+staff.getRestaurantId() +
					" AND member_type_id NOT IN (SELECT member_type_id FROM " + Params.dbName + ".member_level WHERE restaurant_id = " + staff.getRestaurantId() + ")";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			MemberType mType = new MemberType(-1);
			mType.setId(dbCon.rs.getInt("member_type_id"));
			mType.setName(dbCon.rs.getString("name"));
			mType.setAttribute(dbCon.rs.getInt("attribute"));
			list.add(mType);
		}
		return list;
	}
}
