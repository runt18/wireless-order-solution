package com.wireless.db.coupon;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.client.member.MemberDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.coupon.Coupon;
import com.wireless.pojo.coupon.CouponType;
import com.wireless.pojo.staffMgr.Staff;

public class CouponDao {

	/**
	 * Create a new coupon and assign to specific member.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * @return the id to coupon just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if any cases below
	 * 			<li>the coupon type does NOT exist
	 * 			<li>the coupon type has expired
	 * 			<li>the member does NOT exist
	 */
	public static int insert(Staff staff, Coupon.InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Create a new coupon and assign to specific member.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * @return the id to coupon just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if any cases below
	 * 			<li>the coupon type does NOT exist
	 * 			<li>the coupon type has expired
	 * 			<li>the member does NOT exist
	 */
	public static int insert(DBCon dbCon, Staff staff, Coupon.InsertBuilder builder) throws SQLException, BusinessException{
		String sql;

		Coupon coupon = builder.build();

		//Check to see whether the coupon type exist or expired.
		coupon.getCouponType().copyFrom(CouponTypeDao.getById(dbCon, staff, coupon.getCouponType().getId()));
		if(coupon.getCouponType().isExpired()){
			throw new BusinessException(MemberError.COUPON_EXPIRED);
		}
		
		//Check to see whether the member exist.
		sql = " SELECT COUNT(*) FROM " + Params.dbName + ".member WHERE member_id = " + coupon.getMember().getId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			if(dbCon.rs.getInt(1) == 0){
				throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
			}
		}
		dbCon.rs.close();
		
		//Insert a new coupon.
		sql = " INSERT INTO " + Params.dbName + ".coupon " +
			  "(`restaurant_id`, `coupon_type_id`, `birth_date`, `member_id`) VALUES(" +
			  staff.getRestaurantId() + "," +
			  coupon.getCouponType().getId() + "," +
			  " NOW(), " +
			  coupon.getMember().getId() +
			  ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of coupon is not generated successfully.");
		}
	}
	
	/**
	 * Insert a new coupon and assign to specific members.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert all builder
	 * @return the amount to coupon assigned to member
	 * @throws SQLException 
	 * 			throws if database can NOT be connected
	 */
	public static int insertAll(Staff staff, Coupon.InsertAllBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insertAll(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new coupon and assign to specific members.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert all builder
	 * @return the amount to coupon assigned to member
	 */
	public static int insertAll(DBCon dbCon, Staff staff, Coupon.InsertAllBuilder builder){
		int assigned = 0;
		for(Coupon.InsertBuilder eachBuilder : builder.build()){
			try{
				insert(dbCon, staff, eachBuilder);
				assigned++;
			}catch(Exception ignored){}
		}
		return assigned;
	}
	
	/**
	 * Use the coupon.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param couponId
	 * 			the coupon id to use
	 * @param orderId
	 * 			the order id used in coupon
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the coupon to use does NOT exist
	 */
	public static void use(DBCon dbCon, Staff staff, int couponId, int orderId) throws SQLException, BusinessException{
		String sql;
		sql = " UPDATE " + Params.dbName + ".coupon SET " +
			  " coupon_id = " + couponId +
			  " ,order_id = " + orderId +
			  " ,order_date = NOW() " +
			  " ,status = " + Coupon.Status.USED.getVal() +
			  " WHERE coupon_id = " + couponId +
			  " AND restaurant_id = " + staff.getRestaurantId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(MemberError.COUPON_NOT_EXIST);
		}
	}
	
	/**
	 * Get the coupons to specific restaurant associated with the staff
	 * @param staff
	 * 			the staff to perform this action
	 * @return the coupons to specific restaurant 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Coupon> getAll(Staff staff) throws SQLException{ 
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getAll(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the coupons to specific restaurant associated with the staff
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the coupons to specific restaurant 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Coupon> getAll(DBCon dbCon, Staff staff) throws SQLException{ 
		return getByCond(dbCon, staff, null, null);
	}
	
	/**
	 * Get coupons to specific member.
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id
	 * @return the coupons to this member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Coupon> getByMember(Staff staff, int memberId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByMember(dbCon, staff, memberId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get coupons to specific member.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id
	 * @return the coupons to this member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Coupon> getByMember(DBCon dbCon, Staff staff, int memberId) throws SQLException{
		return getByCond(dbCon, staff, " AND C.member_id = " + memberId, null);
	}
	
	/**
	 * Get used coupons to specific member.
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id
	 * @return the used coupons to this member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Coupon> getUsedByMember(Staff staff, int memberId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getUsedByMember(dbCon, staff, memberId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get used coupons to specific member.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id
	 * @return the used coupons to this member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Coupon> getUsedByMember(DBCon dbCon, Staff staff, int memberId) throws SQLException{
		return getByCond(dbCon, staff, " AND C.member_id = " + memberId + " AND C.status = " + Coupon.Status.USED.getVal(), null);
	}
	
	/**
	 * Get expired coupon to specific member.
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id
	 * @return the expired coupon to this member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Coupon> getExpiredByMember(Staff staff, int memberId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getExpiredByMember(dbCon, staff, memberId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get expired coupon to specific member.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id
	 * @return the expired coupon to this member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Coupon> getExpiredByMember(DBCon dbCon, Staff staff, int memberId) throws SQLException{
		return getByCond(dbCon, staff, " AND C.member_id = " + memberId + " AND C.status = " + Coupon.Status.EXPIRED.getVal(), null);
	}
	
	/**
	 * Get available coupon to specific member.
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id
	 * @return the available coupon to this member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Coupon> getAvailByMember(Staff staff, int memberId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getAvailByMember(dbCon, staff, memberId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get available coupon to specific member.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id
	 * @return the available coupon to this member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Coupon> getAvailByMember(DBCon dbCon, Staff staff, int memberId) throws SQLException{
		return getByCond(dbCon, staff, " AND C.member_id = " + memberId + " AND C.status = " + Coupon.Status.CREATED.getVal(), null);
	}
	
	/**
	 * Get the coupon to specific type.
	 * @param staff
	 * 			the staff to perform this action
	 * @param couponTypeId
	 * 			the coupon type id
	 * @return the coupons to specific type
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Coupon> getByType(Staff staff, int couponTypeId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByType(dbCon, staff, couponTypeId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the coupon to specific type.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param couponTypeId
	 * 			the coupon type id
	 * @return the coupons to specific type
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Coupon> getByType(DBCon dbCon, Staff staff, int couponTypeId) throws SQLException{
		return getByCond(dbCon, staff, " AND C.coupon_type_id = " + couponTypeId, null);
	}
	
	/**
	 * Get coupon to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param couponId
	 * 			the coupon id
	 * @return the coupon to this specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the coupon to this id does NOT exist
	 */
	public static Coupon getById(Staff staff, int couponId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, couponId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get coupon to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param couponId
	 * 			the coupon id
	 * @return the coupon to this specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the coupon to this id does NOT exist
	 */
	public static Coupon getById(DBCon dbCon, Staff staff, int couponId) throws SQLException, BusinessException{
		List<Coupon> result = getByCond(dbCon, staff, " AND C.coupon_id = " + couponId, null);
		if(result.isEmpty()){
			throw new BusinessException(MemberError.COUPON_NOT_EXIST);
		}else{
			Coupon coupon = result.get(0);
			coupon.setCouponType(CouponTypeDao.getById(dbCon, staff, coupon.getCouponType().getId()));
			coupon.setMember(MemberDao.getMemberById(dbCon, staff, coupon.getMember().getId()));
			return coupon;
		}
	}
	
	/**
	 * Get the coupon to specific extra condition. 
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the coupons to specific extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Coupon> getByCond(Staff staff, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the coupon to specific extra condition. 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the coupons to specific extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Coupon> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		List<Coupon> result = new ArrayList<Coupon>();
		String sql;
		sql = " SELECT " +
			  " C.coupon_id, C.restaurant_id, C.coupon_type_id, C.birth_date, C.member_id, C.order_id, C.order_date, C.status, " +
			  " CT.name, CT.price, CT.expired, " +
			  " M.name AS member_name, M.mobile, M.member_card " +
			  " FROM " + Params.dbName + ".coupon C " +
			  " JOIN " + Params.dbName + ".coupon_type CT ON C.coupon_type_id = CT.coupon_type_id " +
			  " JOIN " + Params.dbName + ".member M ON C.member_id = M.member_id " +
			  " WHERE 1 = 1 " +
			  " AND C.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Coupon coupon = new Coupon(dbCon.rs.getInt("coupon_id"));
			coupon.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			CouponType ct = new CouponType(dbCon.rs.getInt("coupon_type_id")); 
			ct.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			ct.setPrice(dbCon.rs.getFloat("price"));
			ct.setName(dbCon.rs.getString("name"));
			ct.setExpired(dbCon.rs.getTimestamp("expired").getTime());
			coupon.setCouponType(ct);
			coupon.setBirthDate(dbCon.rs.getTimestamp("birth_date").getTime());
			Member m = new Member(dbCon.rs.getInt("member_id"));
			m.setName(dbCon.rs.getString("member_name"));
			m.setMobile(dbCon.rs.getString("mobile"));
			m.setMemberCard(dbCon.rs.getString("member_card"));
			coupon.setMember(m);
			coupon.setOrderId(dbCon.rs.getInt("order_id"));
			Timestamp ts = dbCon.rs.getTimestamp("order_date");
			if(ts != null){
				coupon.setOrderDate(ts.getTime());
			}
			coupon.setStatus(Coupon.Status.valueOf(dbCon.rs.getInt("status")));
			result.add(coupon);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Delete the coupon to specific member.
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id
	 * @return the amount to coupon deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByMember(Staff staff, int memberId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return deleteByMember(dbCon, staff, memberId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the coupon to specific member.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id
	 * @return the amount to coupon deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByMember(DBCon dbCon, Staff staff, int memberId) throws SQLException{
		return delete(dbCon, staff, " AND member_id = " + memberId);
	}
	
	/**
	 * Delete the coupon to specific type.
	 * @param staff
	 * 			the staff to perform this action 
	 * @param couponTypeId
	 * 			the coupon type to delete
	 * @return the amount to coupon deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByType(Staff staff, int couponTypeId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return deleteByType(dbCon, staff, couponTypeId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the coupon to specific type.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action 
	 * @param couponTypeId
	 * 			the coupon type to delete
	 * @return the amount to coupon deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByType(DBCon dbCon, Staff staff, int couponTypeId) throws SQLException{
		return delete(dbCon, staff, " AND coupon_type_id = " + couponTypeId);
	}
	
	private static int delete(DBCon dbCon, Staff staff, String extraCond) throws SQLException{
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".coupon WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : "");
		return dbCon.stmt.executeUpdate(sql);
	}
	
}
