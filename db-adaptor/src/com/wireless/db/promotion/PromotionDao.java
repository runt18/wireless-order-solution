package com.wireless.db.promotion;

import gui.ava.html.image.generator.HtmlImageGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.imageio.ImageIO;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.member.MemberDao;
import com.wireless.db.oss.CompressImage;
import com.wireless.db.oss.OssImageDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PromotionError;
import com.wireless.pojo.billStatistics.DateRange;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.promotion.Promotion.Status;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.StringHtml;

public class PromotionDao {

	public static class ExtraCond{
		private int promotionId;
		private List<Promotion.Status> statusList = new ArrayList<Promotion.Status>();
		private Promotion.Rule rule;
		private Promotion.Type type;
		private Promotion.Oriented oriented;
		
		public ExtraCond setPromotionId(int id){
			this.promotionId = id;
			return this;
		}
		
		public ExtraCond setStatus(Promotion.Status status){
			this.statusList.clear();
			this.statusList.add(status);
			return this;
		}
		
		public ExtraCond addStatus(Promotion.Status status){
			this.statusList.add(status);
			return this;
		}
		
		public ExtraCond setRule(Promotion.Rule rule){
			this.rule = rule;
			return this;
		}
		
		public ExtraCond setType(Promotion.Type type){
			this.type = type;
			return this;
		}
		
		public ExtraCond setOriented(Promotion.Oriented oriented){
			this.oriented = oriented;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(promotionId != 0){
				extraCond.append(" AND P.promotion_id = " + promotionId);
			}
			
			StringBuilder psCond = new StringBuilder();
			for(Promotion.Status status : statusList){
				String cond = null;
				long now = System.currentTimeMillis();
				if(status == Status.CREATED){
					cond = " (P.start_date > '" + DateUtil.format(now, DateUtil.Pattern.DATE) + "')";
				}else if(status == Status.PROGRESS){
					cond = " (P.start_date <= '" + DateUtil.format(now, DateUtil.Pattern.DATE) + "' AND P.finish_date >= '" + DateUtil.format(now, DateUtil.Pattern.DATE) + "')";
				}else if(status == Status.FINISH){
					cond = " (P.finish_date < '" + DateUtil.format(now, DateUtil.Pattern.DATE) + "')";
				}
				if(cond != null){
					if(psCond.length() == 0){
						psCond.append(cond);
					}else{
						psCond.append(" OR " + cond);
					}
				}
			}
			if(psCond.length() != 0){
				extraCond.append(" AND (" + psCond.toString() + ")");
			}
			
			if(rule != null){
				extraCond.append(" AND P.rule = " + rule.getVal());
			}
			if(type != null){
				extraCond.append(" AND P.type = " + type.getVal());
			}
			if(oriented != null){
				extraCond.append(" AND P.oriented = " + oriented.getVal());
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
		
		//Assure the duplication welcome promotion.
//		if(promotion.getType() == Promotion.Type.WELCOME){
//			if(!getByCond(dbCon, staff, new ExtraCond().setType(Promotion.Type.WELCOME).addStatus(Promotion.Status.CREATED).addStatus(Promotion.Status.PROGRESS)).isEmpty()){
//				throw new BusinessException("【" + Promotion.Type.WELCOME.toString() + "】属性的活动只能创建一个", PromotionError.PROMOTION_CREATE_NOT_ALLOW);
//			}
//		}
		
		//Check to see whether the start date exceed now.
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, -1);
		if(promotion.getDateRange().getOpeningTime() < c.getTimeInMillis()){
			throw new BusinessException(PromotionError.PROMOTION_START_DATE_EXCEED_NOW);
		}
		
		//Insert the associated coupon type.
		int couponTypeId = CouponTypeDao.insert(dbCon, staff, builder.getTypeBuilder());
		
		//Insert the promotion.
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".promotion " +
			  " (restaurant_id, create_date, start_date, finish_date, title, body, entire, coupon_type_id, oriented, rule, type, point, status) " +
			  " VALUES ( " +
			  staff.getRestaurantId() + "," +
			  "'" + DateUtil.format(promotion.getCreateDate(), DateUtil.Pattern.DATE) + "'," +
			  (promotion.hasDateRange() ? "'" + promotion.getDateRange().getOpeningFormat() + "'," : " NULL ") +
			  (promotion.hasDateRange() ? "'" + promotion.getDateRange().getEndingFormat() + "'," : " NULL " ) +
			  "'" + promotion.getTitle() + "'," +
			  "'" + new StringHtml(promotion.getBody(), StringHtml.ConvertTo.TO_NORMAL) + "'," +
			  "'" + new StringHtml(promotion.getEntire(), StringHtml.ConvertTo.TO_NORMAL) + "'," +
			  couponTypeId + "," +
			  promotion.getOriented().getVal() + "," +
			  promotion.getRule().getVal() + "," +
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
		
		if(!promotion.getBody().isEmpty()){
			
			//Update the associated oss image to this promotion's body.
			OssImageDao.update(dbCon, staff, new OssImage.UpdateBuilder4Html(OssImage.Type.WX_PROMOTION, promotionId).setHtml(promotion.getBody()));
			
			//Generate the image to this promotion's body.
			try{
				HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
				imageGenerator.loadHtml(promotion.getBody());
				ByteArrayOutputStream bosJpg = new ByteArrayOutputStream();
				ImageIO.write(new CompressImage().imageZoomOut(imageGenerator.getBufferedImage(), 360, 280), OssImage.ImageType.PNG.getSuffix(), bosJpg);
				bosJpg.flush();
			
				int ossImageId = OssImageDao.insert(dbCon, staff, 
								   					new OssImage.InsertBuilder(OssImage.Type.PROMOTION, promotionId)
									 		   				    .setImgResource(OssImage.ImageType.PNG, new ByteArrayInputStream(bosJpg.toByteArray())));
				
				sql = " UPDATE " + Params.dbName + ".promotion SET oss_image_id = " + ossImageId + " WHERE promotion_id = " + promotionId;
				dbCon.stmt.executeUpdate(sql);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
		if(promotion.getOriented() == Promotion.Oriented.ALL){
			//Create the coupon to all members.
			CouponDao.create(dbCon, staff, new Coupon.CreateBuilder(couponTypeId, promotionId).setMembers(MemberDao.getByCond(dbCon, staff, null, null)));
		}else{
			//Create the coupon to specific members.
			CouponDao.create(dbCon, staff, new Coupon.CreateBuilder(couponTypeId, promotionId).setMembers(builder.getMembers()));
		}
		return promotionId;
	}
	
	/**
	 * Update the promotion according to specific builder {@link Promotion#UpdateBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder {@link Promotion#UpdateBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the promotion status does NOT belong to 'CREATED'
	 * 			<li>throws if the promotion to update does NOT exist
	 * 			<li>throws if the promotion's start exceed now if date range changed
	 * @throws IOException 
	 * 			throws if failed to put the image of coupon type to oss storage  
	 */
	public static void update(Staff staff, Promotion.UpdateBuilder builder) throws SQLException, BusinessException, IOException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the promotion according to specific builder {@link Promotion#UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder {@link Promotion#UpdateBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the promotion to update does NOT exist
	 * 			<li>throws if the promotion's start exceed now if date range changed
	 * @throws IOException 
	 * 			throws if failed to put the image of coupon type to oss storage 
	 */
	public static void update(DBCon dbCon, Staff staff, Promotion.UpdateBuilder builder) throws SQLException, BusinessException, IOException{

		Promotion promotion = builder.build();
		
		Promotion original = getById(dbCon, staff, promotion.getId());
//		if(original.getStatus() != Promotion.Status.CREATED){
//			throw new BusinessException("只有【已创建】状态的活动才能修改", PromotionError.PROMOTION_UPDATE_NOT_ALLOW);
//		}

		//Update the date range to promotion.
//		if(builder.isRangeChanged()){
//			//Check to see whether the start date exceed now.
//			Calendar c = Calendar.getInstance();
//			c.add(Calendar.DAY_OF_YEAR, -1);
//			if(promotion.getDateRange().getOpeningTime() < c.getTimeInMillis()){
//				throw new BusinessException(PromotionError.PROMOTION_START_DATE_EXCEED_NOW);
//			}
//		}
		
		//Update the coupon type.
		if(builder.isCouponTypeChanged()){
			CouponTypeDao.update(dbCon, staff, builder.getCouponTypeBuilder());
		}
		
		//Update the promotion.
		String sql;
		sql = " UPDATE " + Params.dbName + ".promotion SET " +
			  " promotion_id = " + promotion.getId() +
			  (builder.isBodyChanged() ? " ,body = '" + new StringHtml(promotion.getBody(), StringHtml.ConvertTo.TO_NORMAL) + "', entire = '" + new StringHtml(promotion.getEntire(), StringHtml.ConvertTo.TO_NORMAL) + "'" : "") +
			  (builder.isPointChanged() ? ",point = " + promotion.getPoint() : "") +
			  (builder.isRangeChanged() && promotion.hasDateRange() ? ",start_date = '" + promotion.getDateRange().getOpeningFormat() + "',finish_date = '" + promotion.getDateRange().getEndingFormat() + "'" : "") +
			  (builder.isRangeChanged() && !promotion.hasDateRange() ? ",start_date = NULL, finish_date = NULL " : "") +
			  (builder.isTitleChanged() ? ",title = '" + promotion.getTitle() + "'" : "") +
			  (builder.isMemberChanged() ? ",oriented = " + promotion.getOriented().getVal() : "") +
			  " WHERE promotion_id = " + promotion.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PromotionError.PROMOTION_NOT_EXIST);
		}
		
		if(builder.isBodyChanged() && !promotion.getBody().isEmpty()){
			
			//Update the associated oss image to this promotion's body.
			OssImageDao.update(dbCon, staff, new OssImage.UpdateBuilder4Html(OssImage.Type.WX_PROMOTION, promotion.getId()).setHtml(promotion.getBody()));
			
			//Re-Generate the image to promotion's body. 
			try{
				HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
				imageGenerator.loadHtml(promotion.getBody());
				ByteArrayOutputStream bosJpg = new ByteArrayOutputStream();
				ImageIO.write(new CompressImage().imageZoomOut(imageGenerator.getBufferedImage(), 360, 280), OssImage.ImageType.PNG.getSuffix(), bosJpg);
				bosJpg.flush();

				if(original.hasImage()){
					OssImageDao.update(dbCon, staff, new OssImage.UpdateBuilder(original.getImage().getId()).setImgResource(OssImage.ImageType.PNG, new ByteArrayInputStream(bosJpg.toByteArray())));
				}else{
					int ossImageId = OssImageDao.insert(dbCon, staff, 
		   											    new OssImage.InsertBuilder(OssImage.Type.PROMOTION, promotion.getId())
			 		   				    						    .setImgResource(OssImage.ImageType.PNG, new ByteArrayInputStream(bosJpg.toByteArray())));

					sql = " UPDATE " + Params.dbName + ".promotion SET oss_image_id = " + ossImageId + " WHERE promotion_id = " + promotion.getId();
					dbCon.stmt.executeUpdate(sql);
				}
				
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
		//Create the associated coupons if the member changed.
		if(builder.isMemberChanged()){
//			if(original.getType() == Promotion.Type.WELCOME && promotion.getOriented() != Promotion.Oriented.ALL){
//				throw new BusinessException("【" + Promotion.Type.WELCOME.toString() + "】属性的优惠活动只能面向所有会员", PromotionError.PROMOTION_UPDATE_NOT_ALLOW);
//			}
			CouponDao.deleteByCond(dbCon, staff, new CouponDao.ExtraCond().setPromotion(promotion.getId()));
			if(promotion.getOriented() == Promotion.Oriented.ALL){
				//Create the coupon to all members.
				CouponDao.create(dbCon, staff, new Coupon.CreateBuilder(original.getCouponType().getId(), promotion.getId()).setMembers(MemberDao.getByCond(dbCon, staff, null, null)));
			}else{
				//Create the coupon to specific members.
				CouponDao.create(dbCon, staff, new Coupon.CreateBuilder(original.getCouponType().getId(), promotion.getId()).setMembers(builder.getMembers()));
			}
		}
		
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
//	public static int publish(Staff staff, int promotionId) throws SQLException, BusinessException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			dbCon.conn.setAutoCommit(false);
//			int amount = publish(dbCon, staff, promotionId);
//			dbCon.conn.commit();
//			return amount;
//		}catch(BusinessException | SQLException e){
//			dbCon.conn.rollback();
//			throw e;
//		}finally{
//			dbCon.disconnect();
//		}
//	}
	
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
//	public static int publish(DBCon dbCon, Staff staff, int promotionId) throws SQLException, BusinessException{
//		
//		Promotion promotion = getById(dbCon, staff, promotionId);
//		
//		//Assure the status to this promotion should be created.
//		if(promotion.getStatus() != Promotion.Status.CREATED){
//			throw new BusinessException("只有【已创建】状态的活动才能发布", PromotionError.PROMOTION_PUBLISH_NOT_ALLOW);
//		}
//		
//		//Assure the opening time to this promotion before now
//		//Check to see whether the start date exceed now.
//		Calendar c = Calendar.getInstance();
//		c.add(Calendar.DAY_OF_YEAR, -1);
//		if(promotion.getDateRange().getOpeningTime() < c.getTimeInMillis()){
//			throw new BusinessException("活动的开始日期应该在当前日期之后", PromotionError.PROMOTION_START_DATE_EXCEED_NOW);
//		}
//		
//		String sql;
//		//Update the promotion status to be published. 
//		sql = " UPDATE " + Params.dbName + ".promotion SET " +
//			  " status = " + Promotion.Status.PUBLISH.getVal() +
//			  " WHERE promotion_id = " + promotionId;
//		if(dbCon.stmt.executeUpdate(sql) == 0){
//			throw new BusinessException(PromotionError.PROMOTION_NOT_EXIST);
//		}
//		
//		//Update the coupon status to be published.
//		sql = " UPDATE " + Params.dbName + ".coupon SET " +
//			  " status = " + Coupon.Status.PUBLISHED.getVal() +
//			  " WHERE promotion_id = " + promotionId;
//		
//		return dbCon.stmt.executeUpdate(sql);
//		
//	}
	
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
//	public static int cancelPublish(Staff staff, int promotionId) throws SQLException, BusinessException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			dbCon.conn.setAutoCommit(false);
//			int amount = cancelPublish(dbCon, staff, promotionId);
//			dbCon.conn.commit();
//			return amount;
//		}catch(BusinessException | SQLException e){
//			dbCon.conn.rollback();
//			throw e;
//		}finally{
//			dbCon.disconnect();
//		}
//	}
	
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
//	public static int cancelPublish(DBCon dbCon, Staff staff, int promotionId) throws SQLException, BusinessException{
//		
//		Promotion promotion = getById(dbCon, staff, promotionId);
//		
//		//Assure the status to this promotion should be created.
//		if(promotion.getStatus() != Promotion.Status.PUBLISH){
//			throw new BusinessException("只有【已发布】状态的活动才能撤销", PromotionError.PROMOTION_PUBLISH_NOT_ALLOW);
//		}
//		
//		String sql;
//		//Update the promotion status to be published. 
//		sql = " UPDATE " + Params.dbName + ".promotion SET " +
//			  " status = " + Promotion.Status.CREATED.getVal() +
//			  " WHERE promotion_id = " + promotionId;
//		if(dbCon.stmt.executeUpdate(sql) == 0){
//			throw new BusinessException(PromotionError.PROMOTION_NOT_EXIST);
//		}
//		
//		//Update the coupon status to be published.
//		sql = " UPDATE " + Params.dbName + ".coupon SET " +
//			  " status = " + Coupon.Status.CREATED.getVal() +
//			  " WHERE promotion_id = " + promotionId;
//		
//		return dbCon.stmt.executeUpdate(sql);
//	}
	
	/**
	 * finish the promotion in progressed.
	 * @param staff
	 * 			the staff to perform this action
	 * @param promotionId
	 * 			the id of promotion to finish
	 * @return the amount of associated coupon to finish publish
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the promotion does NOT exist
	 * 			<li>throws if the promotion does NOT belong to progressed status
	 */
//	public static int finish(Staff staff, int promotionId) throws SQLException, BusinessException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return finish(dbCon, staff, promotionId);
//		}finally{
//			dbCon.disconnect();
//		}
//	}
	
	/**
	 * finish the promotion in progressed.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param promotionId
	 * 			the id of promotion to finish
	 * @return the amount of associated coupon to finish publish
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the promotion does NOT exist
	 * 			<li>throws if the promotion does NOT belong to progressed status
	 */
//	public static int finish(DBCon dbCon, Staff staff, int promotionId) throws SQLException, BusinessException{
//		Promotion promotion = getById(dbCon, staff, promotionId);
//		
//		//Assure the status to this promotion should be progressed.
//		if(promotion.getStatus() != Promotion.Status.PROGRESS){
//			throw new BusinessException("只有【进行中】状态的活动才能结束", PromotionError.PROMOTION_FINISH_NOT_ALLOW);
//		}
//		
//		String sql;
//		//Update the promotion status to be finish. 
//		sql = " UPDATE " + Params.dbName + ".promotion SET " +
//			  " status = " + Promotion.Status.FINISH.getVal() +
//			  " WHERE promotion_id = " + promotionId;
//		if(dbCon.stmt.executeUpdate(sql) == 0){
//			throw new BusinessException(PromotionError.PROMOTION_NOT_EXIST);
//		}
//		
//		//Update the coupon status to be finish except the drawn.
//		sql = " UPDATE " + Params.dbName + ".coupon SET " +
//			  " status = " + Coupon.Status.FINISH.getVal() +
//			  " WHERE promotion_id = " + promotionId + 
// 			  " AND status <> " + Coupon.Status.DRAWN.getVal();		
//		return dbCon.stmt.executeUpdate(sql);
//	}
	
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
//			if(promotion.getType() != Promotion.Type.DISPLAY_ONLY){
				promotion.setCouponType(CouponTypeDao.getById(dbCon, staff, promotion.getCouponType().getId()));
//			}
			if(promotion.hasImage()){
				promotion.setImage(OssImageDao.getById(dbCon, staff, promotion.getImage().getId()));
			}
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
		sql = " SELECT P.promotion_id, P.restaurant_id, P.create_date, P.start_date, P.finish_date, P.title, P.body, P.entire, P.oriented, P.type, P.rule, P.point, " +
			  " P.coupon_type_id, CT.name, " +
			  " OI.oss_image_id, OI.image, OI.type AS oss_type " +	
			  " FROM " + Params.dbName + ".promotion P " +
			  " LEFT JOIN " + Params.dbName + ".coupon_type CT ON P.coupon_type_id = CT.coupon_type_id " +
			  " LEFT JOIN " + Params.dbName + ".oss_image OI ON P.oss_image_id = OI.oss_image_id " +
			  " WHERE 1 = 1 " +
			  " AND P.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : "") +
			  " ORDER BY P.create_date ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Promotion promotion = new Promotion(dbCon.rs.getInt("promotion_id"));
			promotion.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			promotion.setCreateDate(dbCon.rs.getTimestamp("create_date").getTime());
			
			if(dbCon.rs.getTimestamp("start_date") != null && dbCon.rs.getTimestamp("finish_date") != null){
				promotion.setDateRange(new DateRange(dbCon.rs.getTimestamp("start_date").getTime(), dbCon.rs.getTimestamp("finish_date").getTime()));
			}
			promotion.setTitle(dbCon.rs.getString("title"));
			String body = dbCon.rs.getString("body");
			if(!body.isEmpty()){
				promotion.setBody(new StringHtml(body, StringHtml.ConvertTo.TO_HTML).toString());
				
			}	
			String entire = dbCon.rs.getString("entire");
			if(!entire.isEmpty()){
				promotion.setEntire(new StringHtml(entire, StringHtml.ConvertTo.TO_HTML).toString());
			}
			
			promotion.setOriented(Promotion.Oriented.valueOf(dbCon.rs.getInt("oriented")));
			promotion.setType(Promotion.Type.valueOf(dbCon.rs.getInt("type")));
			promotion.setRule(Promotion.Rule.valueOf(dbCon.rs.getInt("rule")));
			promotion.setPoint(dbCon.rs.getInt("point"));
			
//			if(promotion.getType() != Promotion.Type.DISPLAY_ONLY){
				CouponType type = new CouponType(dbCon.rs.getInt("coupon_type_id"));
				type.setName(dbCon.rs.getString("name"));
				promotion.setCouponType(type);
//			}
			
			if(dbCon.rs.getInt("oss_image_id") != 0){
				OssImage image = new OssImage(dbCon.rs.getInt("oss_image_id"));
				image.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				image.setImage(dbCon.rs.getString("image"));
				image.setType(OssImage.Type.valueOf(dbCon.rs.getInt("oss_type")));
				promotion.setImage(image);
			}
			
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
		//if(promotion.getStatus() == Promotion.Status.CREATED || promotion.getStatus() == Promotion.Status.FINISH){
			
//			if(!CouponDao.getByCond(dbCon, staff, new CouponDao.ExtraCond().setPromotion(promotion).setStatus(Coupon.Status.DRAWN), null).isEmpty()){
//				throw new BusinessException("还有【" + promotion.getTitle() + "】活动的优惠券在使用，请在此活动发放的优惠券全部使用或过期后，再删除活动", PromotionError.PROMOTION_DELETE_NOT_ALLOW);
//			}
			
			//Delete the associated oss image to this promotion
			OssImageDao.delete(dbCon, staff, new OssImageDao.ExtraCond().setAssociated(OssImage.Type.WX_PROMOTION, promotion.getId()));
			
			//Delete the associated full oss image to this promotion.
			if(promotion.hasImage()){
				OssImageDao.delete(dbCon, staff, new OssImageDao.ExtraCond().setId(promotion.getImage().getId()));
			}
			
			//Delete the associated coupon type.
			CouponTypeDao.delete(dbCon, staff, promotion.getCouponType().getId());
			
			//Delete the associated coupons.
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".coupon WHERE promotion_id = " + promotionId;
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the promotion.
			sql = " DELETE FROM " + Params.dbName + ".promotion WHERE promotion_id = " + promotionId;
			if(dbCon.stmt.executeUpdate(sql) == 0){
				throw new BusinessException(PromotionError.PROMOTION_NOT_EXIST);
			}
		//}else{
		//	throw new BusinessException("只有【已创建】或【已结束】状态的活动才能删除", PromotionError.PROMOTION_DELETE_NOT_ALLOW);
		//}
	}
	
	public static class Result{
		private final int nPromotionProgressed;
		private final int nPromotionFinished;
		private final int nCouponExpired;
		
		public Result(int nPromotionPublished, int nPromotionFinished, int nCouponExpired){
			this.nPromotionProgressed = nPromotionPublished;
			this.nPromotionFinished = nPromotionFinished;
			this.nCouponExpired = nCouponExpired;
		}
		
		public int getPromotionPublished(){
			return this.nPromotionProgressed;
		}
		
		public int getPromotionFinished(){
			return this.nPromotionFinished;
		}
		
		public int getCouponExpired(){
			return this.nCouponExpired;
		}
		
		@Override
		public String toString(){
			return "promotion progressed : " + nPromotionProgressed + 
				   ", promotion finished : " + nPromotionFinished +
				   ", coupon expired : " + nCouponExpired;
		}
	}
	
	/**
	 * Update the promotion and coupon status after published in daily settlement.
	 * @param dbCon
	 * 			the database connection
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
//	public static Result calcStatus() throws SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			String sql;
//			//Update the promotion has been published to 'PROGRESS' if the start date exceed now. 
//			sql = " UPDATE " + Params.dbName + ".promotion SET " +
//				  " status = " + Promotion.Status.PROGRESS.getVal() + 
//				  " WHERE 1 = 1 " +
//				  " AND NOW() > start_date " +
//				  " AND status = " + Promotion.Status.PUBLISH.getVal();
//			int nPromotionProgress = dbCon.stmt.executeUpdate(sql);
//			
//			//Update the promotion has been progressed to 'FINISH' if the finish date exceed now. 
//			sql = " UPDATE " + Params.dbName + ".promotion SET " +
//				  " status = " + Promotion.Status.FINISH.getVal() +
//				  " WHERE 1 = 1 " +
//				  " AND NOW() > finish_date " +
//				  " AND status = " + Promotion.Status.PROGRESS.getVal();
//			int nPromotionFinished = dbCon.stmt.executeUpdate(sql);
//			
//			//Update the coupon to be expired if the coupon has been drawn and exceeded now.
//			sql = " SELECT coupon_type_id FROM " + Params.dbName + ".coupon_type WHERE NOW() > expired ";
//			sql = " UPDATE " + Params.dbName + ".coupon SET " +
//				  " status = " + Coupon.Status.EXPIRED.getVal() +
//				  " WHERE status = " + Coupon.Status.DRAWN.getVal() +
//				  " AND coupon_type_id IN (" + sql + ")";
//			int nCouponExpired = dbCon.stmt.executeUpdate(sql);
//			
//			return new Result(nPromotionProgress, nPromotionFinished, nCouponExpired);
//			
//		}finally{
//			dbCon.disconnect();
//		}
//	}
	
}
