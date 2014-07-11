package com.wireless.db.coupon;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.coupon.Coupon;
import com.wireless.pojo.coupon.CouponType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class CouponTypeDao {
	
	/**
	 * Insert a coupon type according to specific builder.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder
	 * @return the id to coupon type just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, CouponType.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a coupon type according to specific builder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder
	 * @return the id to coupon type just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, CouponType.InsertBuilder builder) throws SQLException{
		CouponType type = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".coupon_type " +
			  " (`restaurant_id`, `name`, `price`, `expired`, `comment`, `image`) VALUES(" +
			  staff.getRestaurantId() + "," +
			  "'" + type.getName() + "'," +
			  type.getPrice() + "," +
			  "'" + DateUtil.format(type.getExpired(), DateUtil.Pattern.DATE_TIME) + "'," +
			  "'" + type.getComment() + "'," +
			  "'" + type.getImage() + "'" +
			  ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of coupon type is not generated successfully.");
		}
	}
	
	/**
	 * Update the coupon type according to specific builder.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws BusinessException
	 * 			throws if the coupon type to update does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void update(Staff staff, CouponType.UpdateBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			update(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the coupon type according to specific builder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws BusinessException
	 * 			throws if the coupon type to update does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void update(DBCon dbCon, Staff staff, CouponType.UpdateBuilder builder) throws BusinessException, SQLException{
		CouponType type = builder.build();
		String sql;
		
		if(builder.isExpiredChanged()){
			if(type.isExpired()){
				//Update the associated coupon from 'CREATED' to 'EXPIRED' 
				sql = " UPDATE " + Params.dbName + ".coupon SET " +
					  " status = " + Coupon.Status.EXPIRED.getVal() +
					  " WHERE 1 = 1 " + 
					  " AND coupon_type_id = " + type.getId() +
					  " AND status = " + Coupon.Status.CREATED.getVal();
				dbCon.stmt.executeUpdate(sql);
			}else{
				//Update the associated coupon from 'EXPIRED' to 'CREATED' 
				sql = " UPDATE " + Params.dbName + ".coupon SET " +
					  " status = " + Coupon.Status.CREATED.getVal() +
					  " WHERE 1 = 1 " + 
					  " AND coupon_type_id = " + type.getId() +
					  " AND status = " + Coupon.Status.EXPIRED.getVal();
				dbCon.stmt.executeUpdate(sql);
			}
		}
		
		sql = " UPDATE " + Params.dbName + ".coupon_type SET " +
			  " coupon_type_id = " + type.getId() +
			  (builder.isNameChanged() ? " ,name = '" + type.getName() + "'" : "") +
			  (builder.isExpiredChanged() ? " ,expired = '" + DateUtil.format(type.getExpired()) + "'" : "") +
			  (builder.isCommentChanged() ? " ,comment = '" + type.getComment() + "'" : "") +
			  (builder.isImageChanged() ? " ,image = '" + type.getImage() + "'" : "") +
			  " WHERE coupon_type_id = " + type.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(MemberError.COUPON_TYPE_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the coupon type and associated coupon to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param couponTypeId
	 * 			the coupon type id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the coupon type to delete does NOT exist
	 */
	public static void delete(Staff staff, int couponTypeId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			delete(dbCon, staff, couponTypeId);
			dbCon.conn.commit();
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Delete the coupon type and associated coupon to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param couponTypeId
	 * 			the coupon type id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the coupon type to delete does NOT exist
	 */
	public static void delete(DBCon dbCon, Staff staff, int couponTypeId) throws SQLException, BusinessException{
		String sql;
		
		//Delete the associated coupon.
		CouponDao.deleteByType(dbCon, staff, couponTypeId);
		
		//Delete the coupon type.
		sql = " DELETE FROM " + Params.dbName + ".coupon_type WHERE coupon_type_id = " + couponTypeId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(MemberError.COUPON_TYPE_NOT_EXIST);
		}
	}
	
	/**
	 * Get the coupon type to specific restaurant defined in staff {@link Staff}.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the coupon type to specific restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<CouponType> get(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return get(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the coupon type to specific restaurant defined in staff {@link Staff}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the coupon type to specific restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<CouponType> get(DBCon dbCon, Staff staff) throws SQLException{
		return getByCond(dbCon, staff, null, " ORDER BY expired DESC");
	}
	
	/**
	 * Get the coupon type according to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param couponTypeId
	 * 			the coupon type id to get
	 * @return the coupon type to this specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the coupon type to this id does NOT exist
	 */
	public static CouponType getById(Staff staff, int couponTypeId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, couponTypeId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the coupon type according to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param couponTypeId
	 * 			the coupon type id to get
	 * @return the coupon type to this specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the coupon type to this id does NOT exist
	 */
	public static CouponType getById(DBCon dbCon, Staff staff, int couponTypeId) throws SQLException, BusinessException{
		List<CouponType> result = getByCond(dbCon, staff, " AND coupon_type_id = " + couponTypeId, null);
		if(result.isEmpty()){
			throw new BusinessException(MemberError.COUPON_TYPE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	private static List<CouponType> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " coupon_type_id, restaurant_id, name, price, expired, comment, image " +
			  " FROM " + Params.dbName + ".coupon_type " +
			  " WHERE restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause: "");

		List<CouponType> result = new ArrayList<CouponType>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			CouponType type = new CouponType(dbCon.rs.getInt("coupon_type_id"));
			type.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			type.setName(dbCon.rs.getString("name"));
			type.setPrice(dbCon.rs.getFloat("price"));
			type.setExpired(dbCon.rs.getTimestamp("expired").getTime());
			type.setComment(dbCon.rs.getString("comment"));
			type.setImage(dbCon.rs.getString("image"));
			result.add(type);
		}
		dbCon.rs.close();
		
		return Collections.unmodifiableList(result);
	}
}
