package com.wireless.db.promotion;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PromotionError;
import com.wireless.pojo.billStatistics.DateRange;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class PromotionDao {

	public static class ExtraCond{
		private int promotionId;
		private Promotion.Status status;
		private Promotion.Type type;
		
		public ExtraCond setPromotionId(int id){
			this.promotionId = id;
			return this;
		}
		
		public ExtraCond setStatus(Promotion.Status status){
			this.status = status;
			return this;
		}
		
		public ExtraCond setType(Promotion.Type type){
			this.type = type;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(promotionId != 0){
				extraCond.append(" AND P.promotion_id = " + promotionId);
			}
			if(status != null){
				extraCond.append(" AND P.status = " + status.getVal());
			}
			if(type != null){
				extraCond.append(" AND P.type = " + type.getVal());
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert a new promotion according to builder {@link Promotion#CreateBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder
	 * @return the id to promotion just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the created date of promotion exceed current time
	 */
	public static int create(Staff staff, Promotion.CreateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			dbCon.conn.setAutoCommit(false);
			int promotionId = create(dbCon, staff, builder);
			dbCon.conn.commit();
			
			return promotionId;
			
		}catch(SQLException | BusinessException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new promotion according to builder {@link Promotion#CreateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder
	 * @return the id to promotion just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the created date of promotion exceed current time
	 */
	public static int create(DBCon dbCon, Staff staff, Promotion.CreateBuilder builder) throws SQLException, BusinessException{
		
		Promotion promotion = builder.build();
		
		//Check to see whether the start date exceed now.
		if(promotion.getDateRange().getOpeningTime() < System.currentTimeMillis()){
			throw new BusinessException(PromotionError.PROMOTION_START_DATE_EXCEED_NOW);
		}
		
		//Insert the associated coupon type
		int couponTypeId = CouponTypeDao.insert(dbCon, staff, builder.getTypeBuilder());
		
		//Insert the promotion.
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".promotion " +
			  " (restaurant_id, create_date, start_date, finish_date, title, body, coupon_type_id, type, point, status) " +
			  " VALUES ( " +
			  staff.getRestaurantId() + "," +
			  "'" + DateUtil.format(promotion.getCreateDate(), DateUtil.Pattern.DATE) + "'," +
			  "'" + promotion.getDateRange().getOpeningFormat() + "'," +
			  "'" + promotion.getDateRange().getEndingFormat() + "'," +
			  "'" + promotion.getTitle() + "'," +
			  "'" + promotion.getBody() + "'," +
			  couponTypeId + "," +
			  promotion.getType().getVal() + "," +
			  promotion.getPoint() + "," +
			  promotion.getStatus().getVal() +
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int promotionId;
		if(dbCon.rs.next()){
			promotionId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("Failed to generated the promotion id.");
		}
		
		//Create the coupon with members if the promotion type NOT belong to 'DISPLAY_ONLY'.
		if(promotion.getType() != Promotion.Type.DISPLAY_ONLY){
			CouponDao.create(dbCon, staff, new Coupon.CreateBuilder(couponTypeId, promotionId).setMembers(builder.getMembers()));
		}
		return promotionId;
	}
	
	/**
	 * Publish the promotion.
	 * @param staff
	 * 			the staff to perform this action
	 * @param promotionId
	 * 			the id of promotion to publish
	 * @return the amount of associated coupon to publish
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the promotion does NOT exist
	 * 			<li>throws if the promotion does NOT belong to created status
	 * 			<li>throws if the promotion opening time exceed now
	 */
	public static int publish(Staff staff, int promotionId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int amount = publish(dbCon, staff, promotionId);
			dbCon.conn.commit();
			return amount;
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Publish the promotion.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param promotionId
	 * 			the id of promotion to publish
	 * @return the amount of associated coupon to publish
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the promotion does NOT exist
	 * 			<li>throws if the promotion does NOT belong to created status
	 * 			<li>throws if the promotion opening time exceed now
	 */
	public static int publish(DBCon dbCon, Staff staff, int promotionId) throws SQLException, BusinessException{
		
		Promotion promotion = getById(dbCon, staff, promotionId);
		
		//Assure the status to this promotion should be created.
		if(promotion.getStatus() != Promotion.Status.CREATED){
			throw new BusinessException("只有【已创建】状态的活动才能发布", PromotionError.PROMOTION_PUBLISH_NOT_ALLOW);
		}
		
		//Assure the opening time to this promotion before now
		if(promotion.getDateRange().getOpeningTime() < System.currentTimeMillis()){
			throw new BusinessException("活动的开始日期应该在当前日期之后", PromotionError.PROMOTION_PUBLISH_NOT_ALLOW);
		}
		
		String sql;
		//Update the promotion status to be published. 
		sql = " UPDATE " + Params.dbName + ".promotion SET " +
			  " status = " + Promotion.Status.PUBLISH.getVal() +
			  " WHERE promotion_id = " + promotionId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PromotionError.PROMOTION_NOT_EXIST);
		}
		
		//Update the coupon status to be published.
		sql = " UPDATE " + Params.dbName + ".coupon SET " +
			  " status = " + Coupon.Status.PUBLISHED.getVal() +
			  " WHERE promotion_id = " + promotionId;
		
		return dbCon.stmt.executeUpdate(sql);
		
	}
	
	/**
	 * cancel the promotion published.
	 * @param staff
	 * 			the staff to perform this action
	 * @param promotionId
	 * 			the id of promotion to cancel publish
	 * @return the amount of associated coupon to cancel publish
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the promotion does NOT exist
	 * 			<li>throws if the promotion does NOT belong to publish status
	 */
	public static int cancelPublish(Staff staff, int promotionId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int amount = cancelPublish(dbCon, staff, promotionId);
			dbCon.conn.commit();
			return amount;
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * cancel the promotion published.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param promotionId
	 * 			the id of promotion to cancel publish
	 * @return the amount of associated coupon to cancel publish
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the promotion does NOT exist
	 * 			<li>throws if the promotion does NOT belong to publish status
	 */
	public static int cancelPublish(DBCon dbCon, Staff staff, int promotionId) throws SQLException, BusinessException{
		
		Promotion promotion = getById(dbCon, staff, promotionId);
		
		//Assure the status to this promotion should be created.
		if(promotion.getStatus() != Promotion.Status.PUBLISH){
			throw new BusinessException("只有【已发布】状态的活动才能撤销", PromotionError.PROMOTION_PUBLISH_NOT_ALLOW);
		}
		
		String sql;
		//Update the promotion status to be published. 
		sql = " UPDATE " + Params.dbName + ".promotion SET " +
			  " status = " + Promotion.Status.CREATED.getVal() +
			  " WHERE promotion_id = " + promotionId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PromotionError.PROMOTION_NOT_EXIST);
		}
		
		//Update the coupon status to be published.
		sql = " UPDATE " + Params.dbName + ".coupon SET " +
			  " status = " + Coupon.Status.CREATED.getVal() +
			  " WHERE promotion_id = " + promotionId;
		
		return dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Get the promotion to specific id
	 * @param staff
	 * 			the staff to perform this action
	 * @param promotionId
	 * 			the promotion id
	 * @return the promotion to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the promotion to this id does NOT exist
	 */
	public static Promotion getById(Staff staff, int promotionId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, promotionId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Get the promotion to specific id
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param promotionId
	 * 			the promotion id
	 * @return the promotion to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the promotion to this id does NOT exist
	 */
	public static Promotion getById(DBCon dbCon, Staff staff, int promotionId) throws SQLException, BusinessException{
		List<Promotion> result = getByCond(dbCon, staff, new ExtraCond().setPromotionId(promotionId));
		if(result.isEmpty()){
			throw new BusinessException(PromotionError.PROMOTION_NOT_EXIST);
		}else{
			Promotion promotion = result.get(0);
			promotion.setCouponType(CouponTypeDao.getById(dbCon, staff, promotion.getCouponType().getId()));
			return promotion;
		}
	}
	
	/**
	 * Get the promotion to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the promotions to this extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Promotion> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the promotion to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the promotions to this extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Promotion> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		List<Promotion> result = new ArrayList<Promotion>();
		
		String sql;
		sql = " SELECT P.promotion_id, P.restaurant_id, P.create_date, P.start_date, P.finish_date, P.title, P.body, P.type, P.point, P.status, " +
			  " P.coupon_type_id, CT.name " +
			  " FROM " + Params.dbName + ".promotion P " +
			  " JOIN " + Params.dbName + ".coupon_type CT ON P.coupon_type_id = CT.coupon_type_id " +
			  " WHERE 1 = 1 " +
			  " AND P.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : "") +
			  " ORDER BY P.create_date ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Promotion promotion = new Promotion(dbCon.rs.getInt("promotion_id"));
			promotion.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			promotion.setCreateDate(dbCon.rs.getTimestamp("create_date").getTime());
			promotion.setDateRange(new DateRange(dbCon.rs.getTimestamp("start_date").getTime(), dbCon.rs.getTimestamp("finish_date").getTime()));
			promotion.setTitle(dbCon.rs.getString("title"));
			promotion.setBody(dbCon.rs.getString("body"));
			promotion.setType(Promotion.Type.valueOf(dbCon.rs.getInt("type")));
			promotion.setPoint(dbCon.rs.getInt("point"));
			promotion.setStatus(Promotion.Status.valueOf(dbCon.rs.getInt("status")));
			
			CouponType type = new CouponType(dbCon.rs.getInt("coupon_type_id"));
			type.setName(dbCon.rs.getString("name"));
			promotion.setCouponType(type);
			
			result.add(promotion);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	
	/**
	 * Delete the promotion to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param promotionId
	 * 			the promotion id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the promotion to delete does NOT exist
	 * 			<li>throws if the promotion to delete NOT belong to 'CREATED' status
	 */
	public static void delete(Staff staff, int promotionId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			delete(dbCon, staff, promotionId);
			dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the promotion to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param promotionId
	 * 			the promotion id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the promotion to delete does NOT exist
	 * 			<li>throws if the promotion to delete NOT belong to 'CREATED' status
	 */
	public static void delete(DBCon dbCon, Staff staff, int promotionId) throws SQLException, BusinessException{
		Promotion promotion = getById(dbCon, staff, promotionId);
		if(promotion.getStatus() == Promotion.Status.CREATED){
			CouponTypeDao.delete(dbCon, staff, promotion.getCouponType().getId());
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".promotion WHERE promotion_id = " + promotionId;
			if(dbCon.stmt.executeUpdate(sql) == 0){
				throw new BusinessException(PromotionError.PROMOTION_NOT_EXIST);
			}
		}else{
			throw new BusinessException("只有【已创建】状态的活动才能删除", PromotionError.PROMOTION_DELETE_NOT_ALLOW);
		}
	}
	
	/**
	 * Update the promotion status after published in daily settlement.
	 * @param dbCon
	 * 			the database connection
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void updateStatus(DBCon dbCon) throws SQLException{
		String sql;
		//Update the promotion has been published to 'PROGRESS' if the start date exceed now. 
		sql = " UPDATE " + Params.dbName + ".promotion SET " +
			  " status = " + Promotion.Status.PROGRESS.getVal() + 
			  " WHERE 1 = 1 " +
			  " AND start_date > NOW() " +
			  " AND status = " + Promotion.Status.PUBLISH.getVal();
		dbCon.stmt.executeUpdate(sql);
		
		//Update the promotion has been progressed to 'FINISH' if the finish date exceed now. 
		sql = " UPDATE " + Params.dbName + ".promotion SET " +
			  " status = " + Promotion.Status.FINISH.getVal() +
			  " WHERE 1 = 1 " +
			  " AND finish_date > NOW() " +
			  " AND status = " + Promotion.Status.PROGRESS.getVal();
		dbCon.stmt.executeUpdate(sql);
	}
	
}
