package com.wireless.db.promotion;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.marker.weixin.api.BaseAPI;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.member.MemberDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PromotionError;
import com.wireless.pojo.billStatistics.DateRange;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponOperation;
import com.wireless.pojo.promotion.CouponOperation.OperateType;
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.StringHtml;

public class CouponDao {

	public static class ExtraCond{
		public static enum Filter{
			AVAILABLE(0, "开始和中间"),
			BETWEENS(1, "中间"),;
			
			private final int val;
			private final String desc;
			
			Filter(int val, String desc){
				this.val = val;
				this.desc = desc;
			}
			
			public static Filter valueOf(int val){
				for(Filter filter : values()){
					if(filter.val == val){
						return filter;
					}
				}
				throw new IllegalArgumentException("The filter(val=" + val + ") is invalid.");
			}
			
			public int getVal(){
				return this.val;
			}
			
			public String getDesc(){
				return this.desc;
			}
			
			@Override
			public String toString(){
				return this.desc;
			}
		}
		
		private boolean isOnlyAmount = false;
		private int id;
		private Coupon.Status status;
		private int memberId;
		private int couponTypeId;
		private List<Integer> promotions = new ArrayList<>();
		private CouponOperation.Operate operation;
		private int associateId;
		private Filter filter;
		private DutyRange range;
		
		private int restaurantId;
		
		ExtraCond setRestaurant(int restaurantId){
			this.restaurantId = restaurantId;
			return this;
		}
		
		
		public ExtraCond setOnlyAmount(boolean onOff){
			this.isOnlyAmount = onOff;
			return this;
		}
		
		public ExtraCond setFilter(Filter filter){
			this.filter = filter;
			return this;
		}
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setStatus(Coupon.Status status){
			this.status = status;
			return this;
		}
		
		public ExtraCond setMember(int memberId){
			this.memberId = memberId;
			return this;
		}
		
		public ExtraCond setMember(Member member){
			this.memberId = member.getId();
			return this;
		}
		
		public ExtraCond setCouponType(int couponTypeId){
			this.couponTypeId = couponTypeId;
			return this;
		}
		
		public ExtraCond setPromotion(int promotionId){
			this.promotions.clear();
			this.promotions.add(promotionId);
			return this;
		}
		
		public ExtraCond setPromotion(Promotion promotion){
			return setPromotion(promotion.getId());
		}
		
		public ExtraCond addPromotions(List<Promotion> promotions){
			for(Promotion promotion : promotions){
				this.promotions.add(promotion.getId());
			}
			return this;
		}
		
		public ExtraCond setOperation(CouponOperation.Operate operation, int associateId){
			this.operation = operation;
			this.associateId = associateId;
			return this;
		}
		
		public ExtraCond setRange(String begin, String end){
			this.range = new DutyRange(begin, end);
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND C.coupon_id = " + id);
			}
			if(status != null){
				extraCond.append(" AND C.status = " + status.getVal());
			}
			if(memberId != 0){
				extraCond.append(" AND C.member_id = " + memberId);
			}
			if(couponTypeId != 0){
				extraCond.append(" AND C.coupon_type_id = " + couponTypeId);
			}
			final StringBuilder promotionCond = new StringBuilder();
			for(Integer promotionId : promotions){
				if(promotionCond.length() > 0){
					promotionCond.append(",");
				}
				promotionCond.append(promotionId);
			}
			if(promotionCond.length() != 0){
				extraCond.append(" AND C.promotion_id IN ( " + promotionCond + " ) ");
			}
			if(this.operation != null || this.associateId != 0 || this.range != null){
				String sql;
				sql = " SELECT coupon_id FROM " + Params.dbName + ".coupon_operation " +
					  " WHERE restaurant_id = " + restaurantId +
					  (this.operation != null ? " AND operate = " + this.operation.getVal() : "") +
					  (this.associateId != 0 ? " AND associate_id = " + this.associateId : "") +
					  (this.range != null ? " AND operate_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" : "");
				extraCond.append(" AND C.coupon_id IN ( " + sql + ")");
			}
			return extraCond.toString();
		}
	}
	
	
	/**
	 * Issue the coupon according to specific builder {@link Coupon.IssueBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to issue coupons
	 * @return the array containing the id to coupon just issued
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the promotion does NOT exist
	 */
	public static int[] issue(Staff staff, Coupon.IssueBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return issue(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Issue the coupon according to specific builder {@link Coupon.IssueBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to issue coupons
	 * @return the array containing the id to coupon just issued
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the promotion does NOT exist
	 * 			<li>发放多张满足【单次消费满】活动的优惠券
	 */
	public static int[] issue(DBCon dbCon, final Staff staff, final Coupon.IssueBuilder builder) throws SQLException, BusinessException{
		
		final List<Integer> issueCoupons = new ArrayList<Integer>();
		for(Entry<Integer, Integer> entry : builder.getPromotions()){
			final Promotion promotion = PromotionDao.getById(dbCon, staff, entry.getKey());
			final int amount = entry.getValue();
			
			if(amount == 0){
				continue;
			}

			//单次满
			if(promotion.getIssueTrigger().getIssueRule().isSingleExceed()){
				if(amount > 1){
					throw new BusinessException(("满足【$(promotion)】活动的优惠券只能发放1张").replace("$(promotion)", promotion.getTitle()), PromotionError.COUPON_ISSUE_NOT_ALLOW);
				}
				List<CouponOperation> issuedCoupons = CouponOperationDao.getByCond(dbCon, staff, new CouponOperationDao.ExtraCond()
																													   .setCouponType(promotion.getCouponType())
																													   .setAssociateId(builder.getAssociateId())
																													   .addOperation(CouponOperation.Operate.ORDER_ISSUE));
				if(!issuedCoupons.isEmpty()){
					throw new BusinessException(("本张账单只能发放1张【$(promotion)】活动的优惠券").replace("$(promotion)", promotion.getTitle()), PromotionError.COUPON_ISSUE_NOT_ALLOW);
				}
			}
			
			if(promotion.getCouponType().hasLimit()){
				List<CouponOperation> issuedCoupons = CouponOperationDao.getByCond(dbCon, staff, new CouponOperationDao.ExtraCond()
																													   .setCouponType(promotion.getCouponType())
																													   .addOperation(OperateType.ISSUE));
				if(issuedCoupons.size() > promotion.getCouponType().getLimitAmount()){
					throw new BusinessException(("发送的张数超过了【$(promotion)】活动的优惠券的张数限制").replace("$(promotion)", promotion.getTitle()), PromotionError.COUPON_ISSUE_NOT_ALLOW);
				}
				
			}
			
			for(int memberId : builder.getMembers()){
				try{
					//Check to see whether the member exist.
					if(MemberDao.getByCond(dbCon, staff, new MemberDao.ExtraCond().setId(memberId), null).isEmpty()){
						continue;
					}
					String sql;
					for(int i = 0; i < amount; i++){
						//Issue a new coupon.
						sql = " INSERT INTO " + Params.dbName + ".coupon " +
							  "(`restaurant_id`, `coupon_type_id`, `promotion_id`, `birth_date`, `member_id`, `status`) VALUES (" +
							  (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) + "," +
							  promotion.getCouponType().getId() + "," +
							  promotion.getId() + "," +
							  " NOW(), " +
							  memberId + "," +
							  Coupon.Status.ISSUED.getVal() + 
							  ")";
						dbCon.stmt.executeUpdate(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
						dbCon.rs = dbCon.stmt.getGeneratedKeys();
						int couponId = 0;
						if(dbCon.rs.next()){
							couponId = dbCon.rs.getInt(1);
							issueCoupons.add(couponId);
						}else{
							throw new SQLException("The id of coupon is not generated successfully.");
						}
						dbCon.rs.close();
						
						//Insert the associated the coupon operation.
						CouponOperationDao.insert(dbCon, staff, new CouponOperation.InsertBuilder(couponId)
																		  .setComment(builder.getComment())
																		  .setOperate(builder.getOperation(), builder.getAssociateId()));
					}
				}catch(BusinessException | SQLException e){
					e.printStackTrace();
				}
			}
		}


		final int[] result = new int[issueCoupons.size()];
		for(int i = 0; i < result.length; i++){
			result[i] = issueCoupons.get(i).intValue();
		}
		
		//Send the weixin template msg.
		if(builder.hasWxServer()){
			final ScheduledThreadPoolExecutor schedule = new ScheduledThreadPoolExecutor(1);
			schedule.schedule(new Runnable(){
				@Override
				public void run() {
					for(int couponId : result.clone()){
						try {
							//System.out.println("http://" + serverName + "/wx-term/WxNotifyMember.do?dataSource=issue&couponId=" + couponId + "&staffId=" + staff.getId());
							BaseAPI.doPost("http://" + builder.getWxServer() + "/wx-term/WxNotifyMember.do?dataSource=issue&couponId=" + couponId + "&staffId=" + staff.getId(), "");
						} catch (Exception ignored) {
							ignored.printStackTrace();
						}
					}
					schedule.shutdown();
				}
				
			}, 1, TimeUnit.SECONDS);
		}
		
		return result;
	}

	/**
	 * Use the coupon to specific builder {@link Coupon.UseBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the coupon use builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>any coupon to use does NOT exist
	 * 			<li>any coupon to use has been expired
	 * 			<li>the coupons to use does NOT belongs to the same member
	 */
	public static void use(Staff staff, Coupon.UseBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			use(dbCon, staff, builder);
			dbCon.conn.commit();
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Use the coupon to specific builder {@link Coupon.UseBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the coupon use builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>any coupon to use does NOT exist
	 * 			<li>any coupon to use has been expired
	 * 			<li>the coupons to use does NOT belongs to the same member
	 */
	public static void use(DBCon dbCon, Staff staff, Coupon.UseBuilder builder) throws SQLException, BusinessException{
		for(int couponId : builder.getCoupons()){
			
			Coupon coupon = getById(dbCon, staff, couponId);
			
			if(coupon.getMember().getId() != builder.getMemberId()){
				throw new BusinessException("使用的优惠券不属于此会员", PromotionError.COUPON_USE_NOT_ALLOW);
			}
			
			if(coupon.isExpired()){
				throw new BusinessException("【已过期】的优惠券不可使用", PromotionError.COUPON_USE_NOT_ALLOW);
			}
			
			if(!coupon.getPromotion().isMatchUseTime()){
				throw new BusinessException("【不在使用时段】的优惠券不可使用", PromotionError.COUPON_USE_NOT_ALLOW);
			}
			
			if(coupon.getStatus() == Coupon.Status.USED){
				throw new BusinessException("优惠券【$(promotionName)】不能再次使用".replace("$(promotionName)", coupon.getName()), PromotionError.COUPON_USE_NOT_ALLOW);
			}
			
			String sql;
			sql = " UPDATE " + Params.dbName + ".coupon " +
				  " SET coupon_id = " + couponId +
				  " ,status = " + Coupon.Status.USED.getVal() +
				  " WHERE coupon_id = " + couponId;
			
			if(dbCon.stmt.executeUpdate(sql) == 0){
				throw new BusinessException(PromotionError.COUPON_NOT_EXIST);
			}
			
			//Insert the use operation to this coupon.
			CouponOperationDao.insert(dbCon, staff, new CouponOperation.InsertBuilder(coupon.getId())
																	   .setComment(builder.getComment())
																	   .setOperate(builder.getOperation(), builder.getAssociateId()));
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
	 * @return the array containing the id to coupon just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if any cases below
	 * 			<li>the associated promotion does NOT exist
	 * 			<li>the associated coupon does NOT exist
	 */
//	private static int[] create(DBCon dbCon, Staff staff, Coupon.CreateBuilder builder) throws SQLException, BusinessException{
//		final Promotion promotion = PromotionDao.getById(dbCon, staff, builder.getPromotionId());
////		if(promotion.getStatus() == Promotion.Status.FINISH){
////			throw new BusinessException("【" + promotion.getTitle() + "】已经结束, 不能创建优惠券", PromotionError.COUPON_CREATE_NOT_ALLOW);
////		}
//		
//		//Check to see whether the coupon type is expired.
//		final CouponType couponType = CouponTypeDao.getById(dbCon, staff, builder.getCouponTypeId());
////		if(couponType.isExpired()){
////			throw new BusinessException(PromotionError.COUPON_EXPIRED);
////		}
//		final List<Integer> coupons = new ArrayList<Integer>();
//		for(Member member : builder.getMembers()){
//			try{
//				//Check to see whether the member exist.
//				if(MemberDao.getByCond(dbCon, staff, new MemberDao.ExtraCond().setId(member.getId()), null).isEmpty()){
//					continue;
//				}
//				//Insert the coupon.
//				int couponId = insert(dbCon, staff, new Coupon.InsertBuilder(couponType, member, promotion));
//				//Draw the coupon if draw type belongs to auto.
//				if(builder.getDrawType() == DrawType.AUTO){
//					String sql;
//					sql = " UPDATE " + Params.dbName + ".coupon SET " +
//						  " status = " + Coupon.Status.DRAWN.getVal() +
//						  " ,draw_date = NOW() " +
//						  " WHERE coupon_id = " + couponId;
//					if(dbCon.stmt.executeUpdate(sql) == 0){
//						throw new BusinessException(PromotionError.COUPON_NOT_EXIST);
//					}
//					//draw(dbCon, staff, couponId, DrawType.AUTO);
//				}
//				coupons.add(couponId);
//			}catch(BusinessException | SQLException e){
//				e.printStackTrace();
//			}
//		}
//
//		int[] result = new int[coupons.size()];
//		for(int i = 0; i < result.length; i++){
//			result[i] = coupons.get(i).intValue();
//		}
//		return result;
//	}
	
	
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
	 * 			<li>throws if the coupon to use does NOT exist
	 * 			<li>throws if the coupon has been expired
	 * 			<li>throws if the coupon has NOT been drawn before
	 */
//	private static void use(DBCon dbCon, Staff staff, int couponId, int orderId) throws SQLException, BusinessException{
//		
//		Coupon coupon = getById(dbCon, staff, couponId);
//		
//		if(coupon.isExpired()){
//			throw new BusinessException("【已过期】的优惠券不可使用", PromotionError.COUPON_DRAW_NOT_ALLOW);
//		}
//		
//		if(!coupon.isDrawn()){
//			throw new BusinessException("只有【已领取】的优惠券才可使用", PromotionError.COUPON_DRAW_NOT_ALLOW);
//		}
//		
//		String sql;
//		sql = " UPDATE " + Params.dbName + ".coupon SET " +
//			  " coupon_id = " + couponId +
//			  " ,order_id = " + orderId +
//			  " ,order_date = NOW() " +
//			  " ,status = " + Coupon.Status.USED.getVal() +
//			  " WHERE coupon_id = " + couponId +
//			  " AND restaurant_id = " + staff.getRestaurantId();
//		if(dbCon.stmt.executeUpdate(sql) == 0){
//			throw new BusinessException(PromotionError.COUPON_NOT_EXIST);
//		}
//	}
	
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
		List<Coupon> result = getByCond(dbCon, staff, new ExtraCond().setId(couponId), null);
		if(result.isEmpty()){
			throw new BusinessException(PromotionError.COUPON_NOT_EXIST);
		}else{
			Coupon coupon = result.get(0);

			coupon.setCouponType(CouponTypeDao.getById(dbCon, staff, coupon.getCouponType().getId()));
			coupon.setMember(MemberDao.getById(dbCon, staff, coupon.getMember().getId()));
			coupon.setPromotion(PromotionDao.getById(dbCon, staff, coupon.getPromotion().getId()));

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
	public static List<Coupon> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
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
	public static List<Coupon> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  (extraCond.isOnlyAmount ? 
			  " COUNT(*) " : 
			  " C.coupon_id, P.entire, C.restaurant_id, C.birth_date, C.status, " +
			  " C.coupon_type_id, CT.name, CT.price, CT.begin_expired, CT.end_expired, CT.limit_amount, CT.expired_type, CT.expired_duration, CT.oss_image_id, " +
			  " C.member_id, M.name AS member_name, M.mobile, M.member_card, M.`consumption_amount`, M.point, M.`base_balance`, M.`extra_balance`, MT.name AS memberTypeName, " +
			  " C.promotion_id, P.title, P.rule, P.start_date, P.finish_date ") +
			  " FROM " + Params.dbName + ".coupon C " +
			  " JOIN " + Params.dbName + ".coupon_type CT ON C.coupon_type_id = CT.coupon_type_id " +
			  " JOIN " + Params.dbName + ".promotion P ON C.promotion_id = P.promotion_id " +
			  " JOIN " + Params.dbName + ".member M ON C.member_id = M.member_id " +
			  " JOIN " + Params.dbName + ".member_type MT ON M.member_type_id = MT.member_type_id " +
			  " WHERE 1 = 1 " +
			  " AND C.restaurant_id = " + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) +
			  (extraCond != null ? extraCond.setRestaurant(staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) : " ") +
			  (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		final List<Coupon> result;
		if(extraCond.isOnlyAmount){
			if(dbCon.rs.next()){
				result = Collections.nCopies(dbCon.rs.getInt(1), null);
			}else{
				result = Collections.emptyList();
			}
		}else{
			result = new ArrayList<Coupon>();
			while(dbCon.rs.next()){
				Coupon coupon = new Coupon(dbCon.rs.getInt("coupon_id"));
				coupon.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				coupon.setBirthDate(dbCon.rs.getTimestamp("birth_date").getTime());
				coupon.setStatus(Coupon.Status.valueOf(dbCon.rs.getInt("status")));
				
				CouponType ct = new CouponType(dbCon.rs.getInt("coupon_type_id")); 
				ct.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				ct.setPrice(dbCon.rs.getFloat("price"));
				ct.setName(dbCon.rs.getString("name"));
				if(dbCon.rs.getTimestamp("begin_Expired") != null){
					ct.setBeginExpired(dbCon.rs.getTimestamp("begin_Expired").getTime());
				}
				
				if(dbCon.rs.getTimestamp("end_expired") !=null){
					ct.setEndExpired(dbCon.rs.getTimestamp("end_expired").getTime());
				}
				if(dbCon.rs.getInt("oss_image_id") != 0){
					ct.setImage(new OssImage(dbCon.rs.getInt("oss_image_id")));
				}
				
				ct.setExpiredType(CouponType.ExpiredType.valueOf(dbCon.rs.getInt("expired_type")));
				ct.setExpiredDuration(dbCon.rs.getInt("expired_duration"));
				
				ct.setLimitAmount(dbCon.rs.getInt("limit_amount"));
				
				coupon.setCouponType(ct);
				
				Member m = new Member(dbCon.rs.getInt("member_id"));
				m.setName(dbCon.rs.getString("member_name"));
				m.setMobile(dbCon.rs.getString("mobile"));
				m.setMemberCard(dbCon.rs.getString("member_card"));
				m.setPoint(dbCon.rs.getInt("point"));
				m.setBaseBalance(dbCon.rs.getFloat("base_balance"));
				m.setExtraBalance(dbCon.rs.getFloat("extra_balance"));
				m.setConsumptionAmount(dbCon.rs.getInt("consumption_amount"));
				MemberType mt = new MemberType(0);
				mt.setName(dbCon.rs.getString("memberTypeName"));
				m.setMemberType(mt);
				coupon.setMember(m);
				
				Promotion promotion = new Promotion(dbCon.rs.getInt("promotion_id"));
				promotion.setTitle(dbCon.rs.getString("title"));
				promotion.setRule(Promotion.Rule.valueOf(dbCon.rs.getInt("rule")));
				if(dbCon.rs.getTimestamp("start_date") != null && dbCon.rs.getTimestamp("finish_date") != null){
					promotion.setDateRange(new DateRange(dbCon.rs.getTimestamp("start_date").getTime(), dbCon.rs.getTimestamp("finish_date").getTime()));
				}
				promotion.setEntire(new StringHtml(dbCon.rs.getString("entire"), StringHtml.ConvertTo.TO_HTML).toString());
				coupon.setPromotion(promotion);
				
				//筛选出符合条件的券
				if(extraCond.filter == ExtraCond.Filter.AVAILABLE){
					if((ct.isBeforeBegin() || ct.isBetween())){
						result.add(coupon);
					}
				}else if(extraCond.filter == ExtraCond.Filter.BETWEENS){
					if(ct.isBetween()){
						result.add(coupon);	
					}
				}else{
					result.add(coupon);
				}
				
			}
		}

		dbCon.rs.close();
		
		
		return result;
	}
	
	
	/**
	 * Delete the coupon to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount to coupon deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(Coupon coupon : getByCond(dbCon, staff, extraCond, null)){
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".coupon WHERE coupon_id = " + coupon.getId(); 
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		return amount;
	}
	
	/**
	 * the result of coupons which is expired soon
	 * @param dbCon
	 * @return
	 * @throws SQLException
	 * @throws BusinessException 
	 */
	public static void getExpiredCoupon(DBCon dbCon, String wxServer) throws SQLException, BusinessException{
		final List<Coupon> coupons = new ArrayList<>();
		for(Restaurant restaurant : RestaurantDao.getByCond(dbCon, null, null)){
			final Staff staff = StaffDao.getAdminByRestaurant(dbCon, restaurant.getId());
			//判定是否存在模板
			if(WxRestaurantDao.get(dbCon, staff).hasExpiredTemplate()){
				coupons.addAll(getByCond(dbCon, staff, null, null));
			}
		}
		final int alarmBeforeDays = -5;
		Iterator<Coupon> iter = coupons.iterator();
		while(iter.hasNext()){
			final Coupon coupon = iter.next();
			final Calendar endDay = Calendar.getInstance();
			final Calendar nowDay = Calendar.getInstance();
			//有效期的coupon
			if(coupon.getCouponType().getExpiredType() == CouponType.ExpiredType.DURANTION){
				Calendar calcDate = Calendar.getInstance();
				calcDate.setTimeInMillis(coupon.getBirthDate());
				calcDate.add(Calendar.DATE, coupon.getCouponType().getExpiredDuration());
				endDay.setTimeInMillis(DateUtil.parseDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date(calcDate.getTimeInMillis()))));
			//使用时段的coupon
			}else{
				endDay.setTimeInMillis(coupon.getCouponType().getEndExpired());
			}
			endDay.add(Calendar.DATE, alarmBeforeDays);
			nowDay.setTimeInMillis(DateUtil.parseDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
			//获取刚好到预警天数的优惠券
			if(endDay.compareTo(nowDay) != 0){
				iter.remove();
			}
		}
		
		final Map<Member, List<Coupon>> tipsMap = new HashMap<>();
		for(Coupon coupon : coupons){
			final Member member = coupon.getMember();
			if(tipsMap.containsKey(member)){
				tipsMap.get(member).add(coupon);
			}else{
				final ArrayList<Coupon> couponList = new ArrayList<>();
				couponList.add(coupon);
				tipsMap.put(member, couponList);
			}
		}
		
		//隔2S发送一次  防止微信服务器因操作过于频发 报错
		if(wxServer != null && !wxServer.isEmpty()){
			final ScheduledThreadPoolExecutor schedule = new ScheduledThreadPoolExecutor(1);
			schedule.schedule(new Runnable(){
				@Override
				public void run() {
					Iterator<Member> iter = tipsMap.keySet().iterator();
					while(iter.hasNext()){
						Member member = iter.next();
						String expireDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(tipsMap.get(member).get(0).getCouponType().getEndExpired()));
					
						try {
							BaseAPI.doPost(("http://wx.e-tones.net/wx-term/WxNotifyMember.do?dataSource=expired&restaurantId=$(restaurantId)"+ 
									   "&memberId=$(memberId)&expiredDate=$(expiredDate)&amount=$(amount)").replace("$(restaurantId)", "" + member.getRestaurantId())
																							 			.replace("$(memberId)", "" + member.getId())
																							 			.replace("$(expiredDate)", expireDate)
																							 			.replace("$(amount)", "" + tipsMap.get(member).size()), "");
							Thread.sleep(2000);
						} catch (Exception ignored) {
							ignored.printStackTrace();
						}
					}
					
					schedule.shutdown();
				}
				
			}, 1, TimeUnit.SECONDS);
		}
	}
	
	/**
	 * the result of coupons which is expired soon
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void getExpiredCoupon(String wxServer) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			getExpiredCoupon(dbCon, wxServer);
		} finally {
			dbCon.disconnect();
		}
	}
	
}
