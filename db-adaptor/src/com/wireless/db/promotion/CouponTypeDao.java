package com.wireless.db.promotion;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.oss.OssImageDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PromotionError;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class CouponTypeDao {
	
	/**
	 * Insert a coupon type according to specific builder {@link CouponType#InsertBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder {@link CouponType#InsertBuilder}
	 * @return the id to coupon type just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the image resource to coupon type does NOT exist
	 */
	public static int insert(Staff staff, CouponType.InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int id = insert(dbCon, staff, builder);
			dbCon.conn.commit();
			return id;
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a coupon type according to specific builder {@link CouponType#InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder {@link CouponType#InsertBuilder}
	 * @return the id to coupon type just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the image resource to coupon type does NOT exist
	 */
	public static int insert(DBCon dbCon, Staff staff, CouponType.InsertBuilder builder) throws SQLException, BusinessException{
		CouponType type = builder.build();
		
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".coupon_type " +
			  " (`restaurant_id`, `name`, `price`, `begin_expired`, `end_expired`, `oss_image_id`, `comment`, `limit_amount`) VALUES(" +
			  staff.getRestaurantId() + "," +
			  "'" + type.getName() + "'," +
			  type.getPrice() + "," +
			  "'" + DateUtil.format(type.getBeginExpired(), DateUtil.Pattern.DATE_TIME) + "'," +
			  "'" + DateUtil.format(type.getEndExpired(), DateUtil.Pattern.DATE_TIME) + "'," +
			  (type.hasImage() ? type.getImage().getId() : "NULL") + "," +
			  "'" + type.getComment() + "'," +
			  type.getLimitAmount() + 
			  ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int id = 0;
		if(dbCon.rs.next()){
			id = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of coupon type is not generated successfully.");
		}
		dbCon.rs.close();
		
		//Married the oss image with this coupon type.
		if(builder.hasImage()){
			try{
				OssImageDao.update(dbCon, staff, new OssImage.UpdateBuilder(type.getImage().getId()).setAssociated(OssImage.Type.WX_COUPON_TYPE, id));
			}catch(IOException ignored){}
		}
		
		return id;
		
	}
	
	/**
	 * Update the coupon type according to specific builder {@link CouponType#UpdateBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder {@link CouponType#UpdateBuilder}
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
	 * Update the coupon type according to specific builder {@link CouponType#UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder {@link CouponType#UpdateBuilder}
	 * @throws BusinessException
	 * 			throws if the coupon type to update does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void update(DBCon dbCon, Staff staff, CouponType.UpdateBuilder builder) throws BusinessException, SQLException{
		CouponType type = builder.build();
		String sql;
		
//		if(builder.isExpiredChanged()){
//			if(type.isExpired()){
//				//Update the associated coupon from 'CREATED' to 'EXPIRED' 
//				sql = " UPDATE " + Params.dbName + ".coupon SET " +
//					  " status = " + Coupon.Status.EXPIRED.getVal() +
//					  " WHERE 1 = 1 " + 
//					  " AND coupon_type_id = " + type.getId() +
//					  " AND status = " + Coupon.Status.CREATED.getVal();
//				dbCon.stmt.executeUpdate(sql);
//			}else{
//				//Update the associated coupon from 'EXPIRED' to 'CREATED' 
//				sql = " UPDATE " + Params.dbName + ".coupon SET " +
//					  " status = " + Coupon.Status.CREATED.getVal() +
//					  " WHERE 1 = 1 " + 
//					  " AND coupon_type_id = " + type.getId() +
//					  " AND status = " + Coupon.Status.EXPIRED.getVal();
//				dbCon.stmt.executeUpdate(sql);
//			}
//		}
		
		if(builder.isImageChanged()){
			if(type.hasImage()){
				//Married the oss image with this coupon type.
				try{
					OssImageDao.update(dbCon, staff, new OssImage.UpdateBuilder(type.getImage().getId()).setSingleAssociated(OssImage.Type.WX_COUPON_TYPE, type.getId()));
				}catch(IOException ignored){
					ignored.printStackTrace();
				}
				
				//Update the oss image id.
				sql = " UPDATE " + Params.dbName + ".coupon_type SET " +
					  " oss_image_id = " + type.getImage().getId() +
					  " WHERE coupon_type_id = " + type.getId();
				dbCon.stmt.executeUpdate(sql);
			}else{
				//Delete the associated oss image.
				OssImageDao.delete(dbCon, staff, new OssImageDao.ExtraCond().setAssociated(OssImage.Type.WX_COUPON_TYPE, type.getId()));
				//Update the oss image id.
				sql = " UPDATE " + Params.dbName + ".coupon_type SET " +
					  " oss_image_id = 0 " +
					  " WHERE coupon_type_id = " + type.getId();
				dbCon.stmt.executeUpdate(sql);
			}

		}
		
		sql = " UPDATE " + Params.dbName + ".coupon_type SET " +
			  " coupon_type_id = " + type.getId() +
			  (builder.isNameChanged() ? " ,name = '" + type.getName() + "'" : "") +
			  (builder.isPriceChanged() ? " ,price = " + type.getPrice() : "") +
			  (builder.isBeginExpiredChanged() ? " ,begin_expired = '" + DateUtil.format(type.getBeginExpired()) + "'" : "") +
			  (builder.isEndExpiredChanged() ? " ,end_expired = '" + DateUtil.format(type.getEndExpired()) + "'" : "") +
			  (builder.isCommentChanged() ? " ,comment = '" + type.getComment() + "'" : "") +
			  (builder.isLimitAmountChanged() ? " ,limit_amount = " + type.getLimitAmount() : "") + 
			  " WHERE coupon_type_id = " + type.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PromotionError.COUPON_TYPE_NOT_EXIST);
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
	 * Delete the coupon type to specific id.
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
		
		//Delete the associated coupon type image.
		OssImageDao.delete(dbCon, staff, new OssImageDao.ExtraCond().setAssociated(OssImage.Type.WX_COUPON_TYPE, couponTypeId));
		
		String sql;
		//Delete the coupon type.
		sql = " DELETE FROM " + Params.dbName + ".coupon_type WHERE coupon_type_id = " + couponTypeId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PromotionError.COUPON_TYPE_NOT_EXIST);
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
			throw new BusinessException(PromotionError.COUPON_TYPE_NOT_EXIST);
		}else{
			CouponType type = result.get(0);
			try{
				if(type.hasImage()){
					type.setImage(OssImageDao.getById(dbCon, staff.isBranch() ? StaffDao.getAdminByRestaurant(dbCon, staff.getGroupId()) : staff, type.getImage().getId()));
				}
			}catch(BusinessException ignored){
				ignored.printStackTrace();
				type.setImage(null);
			}
			return type;
		}
	}
	
	private static List<CouponType> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " coupon_type_id, restaurant_id, name, price, begin_expired, end_expired, comment, limit_amount, oss_image_id " +
			  " FROM " + Params.dbName + ".coupon_type " +
			  " WHERE restaurant_id = " + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause: "");

		final List<CouponType> result = new ArrayList<CouponType>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			CouponType type = new CouponType(dbCon.rs.getInt("coupon_type_id"));
			type.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			type.setName(dbCon.rs.getString("name"));
			type.setPrice(dbCon.rs.getFloat("price"));
			if(dbCon.rs.getTimestamp("begin_expired") != null){
				type.setBeginExpired(dbCon.rs.getTimestamp("begin_expired").getTime());
			}
			if(dbCon.rs.getTimestamp("end_Expired") != null){
				type.setEndExpired(dbCon.rs.getTimestamp("end_Expired").getTime());
			}
			type.setComment(dbCon.rs.getString("comment"));
			if(dbCon.rs.getInt("oss_image_id") != 0){
				type.setImage(new OssImage(dbCon.rs.getInt("oss_image_id")));
			}
			type.setLimitAmount(dbCon.rs.getInt("limit_amount"));
			result.add(type);
		}
		dbCon.rs.close();
		
		return result;
	}
}
