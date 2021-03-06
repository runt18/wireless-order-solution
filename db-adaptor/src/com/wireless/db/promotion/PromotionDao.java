package com.wireless.db.promotion;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.member.MemberDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.oss.OssImageDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PromotionError;
import com.wireless.pojo.billStatistics.DateRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.promotion.Promotion.Status;
import com.wireless.pojo.promotion.PromotionTrigger;
import com.wireless.pojo.promotion.PromotionUseTime.InsertBuilder;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.StringHtml;

public class PromotionDao {

	private static class IssueCond{
		final PromotionTrigger.IssueRule rule;
		final Object extra;
		IssueCond(PromotionTrigger.IssueRule rule, Object extra){
			this.rule = rule;
			this.extra = extra;
		}
	}

	private static class UseCond{
		final PromotionTrigger.UseRule rule;
		final Object extra;
		UseCond(PromotionTrigger.UseRule rule, Object extra){
			this.rule = rule;
			this.extra = extra;
		}
	}
	
	public static class ExtraCond{
		private int promotionId;
		private List<Promotion.Status> statusList = new ArrayList<Promotion.Status>();
		private Promotion.Rule rule;
		private Promotion.Type type;
		private final List<IssueCond> issueRules = new ArrayList<>();
		private final List<UseCond> useRules = new ArrayList<>();

		public ExtraCond addIssueRule(PromotionTrigger.IssueRule rule){
			issueRules.add(new IssueCond(rule, null));
			return this;
		}
		
		public ExtraCond addIssueRule(PromotionTrigger.IssueRule rule, Object extra){
			issueRules.add(new IssueCond(rule, extra));
			return this;
		}

		public ExtraCond addUseRule(PromotionTrigger.UseRule rule, Object extra){
			useRules.add(new UseCond(rule, extra));
			return this;
		}

		public ExtraCond addUseRule(PromotionTrigger.UseRule rule){
			useRules.add(new UseCond(rule, null));
			return this;
		}
		
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
					cond = " IFNULL(P.finish_date, 0) = 0 ";
				}else if(status == Status.PROGRESS){
					cond = " (IFNULL(P.finish_date, 0) <> 0 AND P.finish_date >= '" + DateUtil.format(now, DateUtil.Pattern.DATE) + "')";
				}else if(status == Status.FINISH){
					cond = " (IFNULL(P.finish_date, 0) <> 0 AND P.finish_date < '" + DateUtil.format(now, DateUtil.Pattern.DATE) + "')";
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
		
		//Insert the associated coupon type.
		int couponTypeId = CouponTypeDao.insert(dbCon, staff, builder.getTypeBuilder());
		
		//Insert the promotion.
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".promotion " +
			  " (restaurant_id, create_date, start_date, finish_date, title, body, entire, coupon_type_id, rule, type, status) " +
			  " VALUES ( " +
			  staff.getRestaurantId() + "," +
			  "'" + DateUtil.format(promotion.getCreateDate(), DateUtil.Pattern.DATE) + "'," +
			  " NULL, " +
			  " NULL, " +
			  "'" + promotion.getTitle() + "'," +
			  "'" + new StringHtml(promotion.getBody(), StringHtml.ConvertTo.TO_NORMAL) + "'," +
			  "'" + new StringHtml(promotion.getEntire(), StringHtml.ConvertTo.TO_NORMAL) + "'," +
			  couponTypeId + "," +
			  promotion.getRule().getVal() + "," +
			  promotion.getType().getVal() + "," +
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
		
		//Insert the issue promotion trigger.
		if(builder.hasIssueTrigger()){
			PromotionTriggerDao.insert(dbCon, staff, builder.getIssueTriggerBuilder().setPromotion(promotionId));
		}

		//Insert the use promotion trigger.
		if(builder.hasUseTrigger()){
			PromotionTriggerDao.insert(dbCon, staff, builder.getUseTriggerBuilder().setPromotion(promotionId));
		}
		
		//Insert the use promotion time
		for(InsertBuilder promotionUseTime : builder.getUseTimeBuilder()){
			PromotionUseTimeDao.insert(dbCon, staff, promotionUseTime.setPromotionId(promotionId));
		}
		
		
		if(!promotion.getBody().isEmpty()){
			
			//Update the associated oss image to this promotion's body.
			OssImageDao.update(dbCon, staff, new OssImage.UpdateBuilder4Html(OssImage.Type.WX_PROMOTION, promotionId).setHtml(promotion.getBody()));
			
			//Generate the image to this promotion's body.
//			try{
//				HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
//				imageGenerator.loadHtml(promotion.getBody());
//				ByteArrayOutputStream bosJpg = new ByteArrayOutputStream();
//				ImageIO.write(new CompressImage().imageZoomOut(imageGenerator.getBufferedImage(), 360, 280), OssImage.ImageType.PNG.getSuffix(), bosJpg);
//				bosJpg.flush();
//			
//				int ossImageId = OssImageDao.insert(dbCon, staff, 
//								   					new OssImage.InsertBuilder(OssImage.Type.PROMOTION, promotionId)
//									 		   				    .setImgResource(OssImage.ImageType.PNG, new ByteArrayInputStream(bosJpg.toByteArray())));
//				
//				sql = " UPDATE " + Params.dbName + ".promotion SET oss_image_id = " + ossImageId + " WHERE promotion_id = " + promotionId;
//				dbCon.stmt.executeUpdate(sql);
//			}catch(IOException e){
//				e.printStackTrace();
//			}
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
		
		//Promotion original = getById(dbCon, staff, promotion.getId());
		
		//Update the coupon type.
		if(builder.isCouponTypeChanged()){
			CouponTypeDao.update(dbCon, staff, builder.getCouponTypeBuilder());
		}
		
		//Update the promotion.
		String sql;
		sql = " UPDATE " + Params.dbName + ".promotion SET " +
			  " promotion_id = " + promotion.getId() +
			  (builder.isBodyChanged() ? " ,body = '" + new StringHtml(promotion.getBody(), StringHtml.ConvertTo.TO_NORMAL) + "', entire = '" + new StringHtml(promotion.getEntire(), StringHtml.ConvertTo.TO_NORMAL) + "'" : "") +
			  (builder.isRangeChanged() && promotion.hasDateRange() ? ",start_date = '" + promotion.getDateRange().getOpeningFormat() + "',finish_date = '" + promotion.getDateRange().getEndingFormat() + "'" : "") +
			  (builder.isRangeChanged() && !promotion.hasDateRange() ? ",start_date = NULL, finish_date = NULL " : "") +
			  (builder.isTitleChanged() ? ",title = '" + promotion.getTitle() + "'" : "") +
			  " WHERE promotion_id = " + promotion.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PromotionError.PROMOTION_NOT_EXIST);
		}
		
		//Update the associated promotion issue trigger.
		if(builder.isIssueTriggerChanged()){
			PromotionTriggerDao.deleteByCond(dbCon, staff, new PromotionTriggerDao.ExtraCond().setPromotion(promotion).setType(PromotionTrigger.Type.ISSUE));
			if(builder.getIssueTriggerBuilder() != null){
				PromotionTriggerDao.insert(dbCon, staff, builder.getIssueTriggerBuilder().setPromotion(promotion));
			}
		}

		//Update the associated promotion use trigger.
		if(builder.isUseTriggerChanged()){
			PromotionTriggerDao.deleteByCond(dbCon, staff, new PromotionTriggerDao.ExtraCond().setPromotion(promotion).setType(PromotionTrigger.Type.USE));
			if(builder.getUseTriggerBuilder() != null){
				PromotionTriggerDao.insert(dbCon, staff, builder.getUseTriggerBuilder().setPromotion(promotion));
			}
		}
		
		//update the use promotion time 
		if(builder.isUseTimeChange()){
			//delete 
			PromotionUseTimeDao.delById(dbCon, staff, promotion.getId());
			
			if(builder.getUseTimeBuilder() != null){
				for(InsertBuilder promotionUseTime : builder.getUseTimeBuilder()){
					PromotionUseTimeDao.insert(dbCon, staff, promotionUseTime.setPromotionId(promotion));
				}
			}
		}
		
		if(builder.isBodyChanged() && !promotion.getBody().isEmpty()){
			
			//Update the associated oss image to this promotion's body.
			OssImageDao.update(dbCon, staff, new OssImage.UpdateBuilder4Html(OssImage.Type.WX_PROMOTION, promotion.getId()).setHtml(promotion.getBody()));
			
			//Re-Generate the image to promotion's body. 
//			try{
//				HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
//				imageGenerator.loadHtml(promotion.getBody());
//				ByteArrayOutputStream bosJpg = new ByteArrayOutputStream();
//				ImageIO.write(new CompressImage().imageZoomOut(imageGenerator.getBufferedImage(), 360, 280), OssImage.ImageType.PNG.getSuffix(), bosJpg);
//				bosJpg.flush();
//
//				if(original.hasImage()){
//					OssImageDao.update(dbCon, staff, new OssImage.UpdateBuilder(original.getImage().getId()).setImgResource(OssImage.ImageType.PNG, new ByteArrayInputStream(bosJpg.toByteArray())));
//				}else{
//					int ossImageId = OssImageDao.insert(dbCon, staff, 
//		   											    new OssImage.InsertBuilder(OssImage.Type.PROMOTION, promotion.getId())
//			 		   				    						    .setImgResource(OssImage.ImageType.PNG, new ByteArrayInputStream(bosJpg.toByteArray())));
//
//					sql = " UPDATE " + Params.dbName + ".promotion SET oss_image_id = " + ossImageId + " WHERE promotion_id = " + promotion.getId();
//					dbCon.stmt.executeUpdate(sql);
//				}
				
//			}catch(IOException e){
//				e.printStackTrace();
//			}
		}
		
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
	 * @throws BusinessException 
	 */
	public static List<Promotion> getByCond(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
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
	 * @throws BusinessException 
	 */
	public static List<Promotion> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		List<Promotion> result = new ArrayList<Promotion>();
		
		String sql;
		sql = " SELECT P.promotion_id, P.restaurant_id, P.create_date, P.start_date, P.finish_date, P.title, P.body, P.entire, P.type, P.rule, " +
			  " P.coupon_type_id, " +
			  " OI.oss_image_id, OI.image, OI.type AS oss_type " +	
			  " FROM " + Params.dbName + ".promotion P " +
			  " LEFT JOIN " + Params.dbName + ".oss_image OI ON P.oss_image_id = OI.oss_image_id " +
			  " WHERE 1 = 1 " +
			  " AND P.restaurant_id = " + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) +
			  (extraCond != null ? extraCond : "") +
			  " ORDER BY P.create_date ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Promotion promotion = new Promotion(dbCon.rs.getInt("promotion_id"));
			promotion.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			promotion.setCreateDate(dbCon.rs.getTimestamp("create_date").getTime());
			
			if(dbCon.rs.getTimestamp("start_date") != null && dbCon.rs.getTimestamp("finish_date") != null){
				promotion.setDateRange(new DateRange(dbCon.rs.getTimestamp("start_date").getTime(), dbCon.rs.getTimestamp("finish_date").getTime()));
				
			}else if(dbCon.rs.getTimestamp("start_date") != null && dbCon.rs.getTimestamp("finish_date") == null){
				promotion.setDateRange(new DateRange(dbCon.rs.getTimestamp("start_date").getTime(), 0));
				
			}else if(dbCon.rs.getTimestamp("start_date") == null && dbCon.rs.getTimestamp("finish_date") != null){
				promotion.setDateRange(new DateRange(0, dbCon.rs.getTimestamp("finish_date").getTime()));
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
			
			promotion.setType(Promotion.Type.valueOf(dbCon.rs.getInt("type")));
			promotion.setRule(Promotion.Rule.valueOf(dbCon.rs.getInt("rule")));
			
			promotion.setCouponType(CouponTypeDao.getById(staff, dbCon.rs.getInt("coupon_type_id")));
			
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
		
		//Get the associated issue & use trigger
		for(Promotion promotion : result){
			List<PromotionTrigger> triggers = PromotionTriggerDao.getByCond(dbCon, staff, new PromotionTriggerDao.ExtraCond().setType(PromotionTrigger.Type.ISSUE).setPromotion(promotion));
			if(triggers.isEmpty()){
				promotion.setIssueTrigger(PromotionTrigger.InsertBuilder.newIssue4Free().setPromotion(promotion).build());
			}else{
				promotion.setIssueTrigger(triggers.get(0));
			}
			triggers = PromotionTriggerDao.getByCond(dbCon, staff, new PromotionTriggerDao.ExtraCond().setType(PromotionTrigger.Type.USE).setPromotion(promotion));
			if(triggers.isEmpty()){
				promotion.setUseTrigger(PromotionTrigger.InsertBuilder.newUse4Free().setPromotion(promotion).build());
			}else{
				promotion.setUseTrigger(triggers.get(0));
			}
		}

		//GET the use promtion time
		for(Promotion promotion : result){
			promotion.setUseTimes(PromotionUseTimeDao.getByCond(dbCon, staff, new PromotionUseTimeDao.ExtraCond().setPromotionId(promotion))); 
		}


		if(!extraCond.issueRules.isEmpty() || !extraCond.useRules.isEmpty()){
			final List<Promotion> promotions = new ArrayList<>(result);
			result.clear();
			
			//Filter the promotions matched the issue rule.
			for(IssueCond issueRule : extraCond.issueRules){
				for(Promotion promotion : promotions){
					if(issueRule.rule == promotion.getIssueTrigger().getIssueRule()){
						if(issueRule.rule.isSingleExceed()){
							//单次消费满足
							Order order = OrderDao.getById(dbCon, staff, ((Integer)issueRule.extra).intValue(), DateType.TODAY);
							if(order.calcTotalPrice() > promotion.getIssueTrigger().getExtra()){
								result.add(promotion);
							}
						}else{
							result.add(promotion);
						}
					}
				}
			}
			
			//Filter the promotions matched the use rule.
			for(UseCond useRule : extraCond.useRules){
				for(Promotion promotion : promotions){
					if(useRule.rule == promotion.getUseTrigger().getUseRule()){
						if(useRule.rule.isSingleExceed()){
							Order order = OrderDao.getById(dbCon, staff, ((Integer)useRule.extra).intValue(), DateType.TODAY);
							if(order.calcTotalPrice() > promotion.getUseTrigger().getExtra()){
								result.add(promotion);
							}
						}else{
							result.add(promotion);
						}
					}
				}
			}
		}
		
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
	public static void deleteById(Staff staff, int promotionId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			deleteById(dbCon, staff, promotionId);
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
	public static void deleteById(DBCon dbCon, Staff staff, int promotionId) throws SQLException, BusinessException{
		Promotion promotion = getById(dbCon, staff, promotionId);
		
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
		//Delete the use promotion time 
		PromotionUseTimeDao.delById(dbCon, staff, promotionId);
		//Delete the associated triggers.
		PromotionTriggerDao.deleteByCond(dbCon, staff, new PromotionTriggerDao.ExtraCond().setPromotion(promotionId));
		//Delete the promotion.
		sql = " DELETE FROM " + Params.dbName + ".promotion WHERE promotion_id = " + promotionId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PromotionError.PROMOTION_NOT_EXIST);
		}
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
