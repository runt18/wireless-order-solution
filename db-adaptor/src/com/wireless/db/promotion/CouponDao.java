package com.wireless.db.promotion;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.client.member.MemberDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.exception.PromotionError;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.staffMgr.Staff;

public class CouponDao {

	public static class ExtraCond{
		private int id;
		private Coupon.Status status;
		private int memberId;
		private int couponTypeId;
		private int promotionId;
		private final List<Promotion.Status> promotionStatus = new ArrayList<Promotion.Status>();
		
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
			this.promotionId = promotionId;
			return this;
		}
		
		public ExtraCond setPromotion(Promotion promotion){
			this.promotionId = promotion.getId();
			return this;
		}
		
		public ExtraCond addPromotionStatus(Promotion.Status status){
			this.promotionStatus.add(status);
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
			if(promotionId != 0){
				extraCond.append(" AND C.promotion_id = " + promotionId);
			}
			StringBuilder psCond = new StringBuilder();
			for(Promotion.Status status : promotionStatus){
				if(psCond.length() == 0){
					psCond.append(" P.status = " + status.getVal());
				}else{
					psCond.append(" OR P.status = " + status.getVal());
				}
			}
			if(psCond.length() != 0){
				extraCond.append(" AND (" + psCond.toString() + ")");
			}
			return extraCond.toString();
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
	private static int insert(DBCon dbCon, Staff staff, Coupon.InsertBuilder builder) throws SQLException, BusinessException{
		String sql;

		Coupon coupon = builder.build();

		//Check to see whether the coupon type exist or expired.
		coupon.getCouponType().copyFrom(CouponTypeDao.getById(dbCon, staff, coupon.getCouponType().getId()));
		if(coupon.getCouponType().isExpired()){
			throw new BusinessException(PromotionError.COUPON_EXPIRED);
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
			  "(`restaurant_id`, `coupon_type_id`, `promotion_id`, `birth_date`, `member_id`, `status`) VALUES(" +
			  staff.getRestaurantId() + "," +
			  coupon.getCouponType().getId() + "," +
			  coupon.getPromotion().getId() + "," +
			  " NOW(), " +
			  coupon.getMember().getId() + "," +
			  coupon.getStatus().getVal() +
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
	 * create a new coupon and assign to specific members.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert all builder
	 * @return the amount to coupon assigned to member
	 * @throws SQLException 
	 * 			throws if database can NOT be connected
	 */
	static int create(Staff staff, Coupon.CreateBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return create(dbCon, staff, builder);
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
	static int create(DBCon dbCon, Staff staff, Coupon.CreateBuilder builder){
		int amount = 0;
		for(Coupon.InsertBuilder eachBuilder : builder.build()){
			try{
				insert(dbCon, staff, eachBuilder);
				amount++;
			}catch(BusinessException | SQLException e){
				e.printStackTrace();
			}
		}
		return amount;
	}
	
	/**
	 * Draw a coupon to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param couponId
	 * 			the coupon to draw
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the coupon to draw does NOT exist
	 * 			<li>throws if coupon is NOT in 'PUBLISH' status
	 * 			<li>throws if the promotion associated with this coupon is NOT in 'PROGRESS' status
	 * 			<li>throws if the coupon is NOT qualified to draw
	 */
	public static void draw(Staff staff, int couponId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			draw(dbCon, staff, couponId);
			dbCon.conn.commit();
		}catch(SQLException | BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Draw a coupon to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param couponId
	 * 			the coupon to draw
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the coupon to draw does NOT exist
	 * 			<li>throws if coupon is NOT in 'PUBLISH' status
	 * 			<li>throws if the promotion associated with this coupon is NOT in 'PROGRESS' status
	 * 			<li>throws if the coupon is NOT qualified to draw
	 */
	public static void draw(DBCon dbCon, Staff staff, int couponId) throws SQLException, BusinessException{
		
		Coupon coupon = getById(dbCon, staff, couponId);
		
		if(coupon.getStatus() != Coupon.Status.PUBLISHED){
			throw new BusinessException("只有【已发布】的优惠券才可领取", PromotionError.COUPON_DRAW_NOT_ALLOW);
		}
		
		if(coupon.getPromotion().getType() == Promotion.Type.DISPLAY_ONLY){
			throw new BusinessException("【纯展示】的优惠活动没有优惠券", PromotionError.COUPON_DRAW_NOT_ALLOW);
		}
		
		if(coupon.getPromotion().getStatus() != Promotion.Status.PROGRESS){
			throw new BusinessException("只有【进行中】的优惠活动才可领取优惠券", PromotionError.COUPON_DRAW_NOT_ALLOW);
		}
		
		if(coupon.getDrawProgress().isOk()){
			String sql;
			sql = " UPDATE " + Params.dbName + ".coupon SET " +
				  " status = " + Coupon.Status.DRAWN.getVal() +
				  " ,draw_date = NOW() " +
				  " WHERE coupon_id = " + couponId;
			if(dbCon.stmt.executeUpdate(sql) == 0){
				throw new BusinessException(PromotionError.COUPON_NOT_EXIST);
			}
		}else{
			throw new BusinessException("优惠券还没符合领取条件", PromotionError.COUPON_DRAW_NOT_ALLOW);
		}
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
	 * 			<li>throws if the coupon to use does NOT exist
	 * 			<li>throws if the coupon has NOT been drawn before
	 */
	public static void use(DBCon dbCon, Staff staff, int couponId, int orderId) throws SQLException, BusinessException{
		
		Coupon coupon = getById(dbCon, staff, couponId);
		
		if(coupon.getStatus() != Coupon.Status.DRAWN){
			throw new BusinessException("只有【已领取】的优惠券才可使用", PromotionError.COUPON_DRAW_NOT_ALLOW);
		}
		
		String sql;
		sql = " UPDATE " + Params.dbName + ".coupon SET " +
			  " coupon_id = " + couponId +
			  " ,order_id = " + orderId +
			  " ,order_date = NOW() " +
			  " ,status = " + Coupon.Status.USED.getVal() +
			  " WHERE coupon_id = " + couponId +
			  " AND restaurant_id = " + staff.getRestaurantId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PromotionError.COUPON_NOT_EXIST);
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
			Promotion promotion = PromotionDao.getById(dbCon, staff, coupon.getPromotion().getId());
			coupon.setPromotion(promotion);

			if(coupon.getStatus() == Coupon.Status.PUBLISHED && promotion.getStatus() == Promotion.Status.PROGRESS){
				if(promotion.getType() == Promotion.Type.TOTAL || promotion.getType() == Promotion.Type.ONCE){
					String sql;
					sql = " SELECT delta_point, operate_date FROM " + Params.dbName + ".member_operation " +
						  " WHERE 1 = 1 " +
						  " AND member_id = " + coupon.getMember().getId() +
						  " AND operate_type = " + MemberOperation.OperationType.CONSUME.getValue() + 
						  " AND operate_date BETWEEN '" + promotion.getDateRange().getOpeningFormat() + "' AND '" + promotion.getDateRange().getEndingFormat() + "'" +
						  " UNION " +
						  " SELECT delta_point, operate_date FROM " + Params.dbName + ".member_operation_history " +
						  " WHERE 1 = 1 " +
						  " AND member_id = " + coupon.getMember().getId() +
						  " AND operate_type = " + MemberOperation.OperationType.CONSUME.getValue() + 
						  " AND operate_date BETWEEN '" + promotion.getDateRange().getOpeningFormat() + "' AND '" + promotion.getDateRange().getEndingFormat() + "'";
					
					if(promotion.getType() == Promotion.Type.ONCE){
						//String sql4LastestDraw = " SELECT draw_date FROM " + Params.dbName + ".coupon WHERE promotion_id = " + promotion.getId() + " ORDER BY draw_date DESC LIMIT 1 ";
						//sql = " SELECT MAX(delta_point) AS max_point FROM ( " + sql + " ) AS TMP WHERE TMP.operate_date >= ( " + sql4LastestDraw + ")";
						sql = " SELECT MAX(delta_point) AS max_point FROM ( " + sql + " ) AS TMP ";
					}else if(promotion.getType() == Promotion.Type.TOTAL){
						sql = " SELECT SUM(delta_point) AS total_point FROM ( " + sql + " ) AS TMP ";
					}
					
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					if(dbCon.rs.next()){
						coupon.setDrawProgress(dbCon.rs.getInt(1));
					}
					dbCon.rs.close();				
				}

			}
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
		List<Coupon> result = new ArrayList<Coupon>();
		String sql;
		sql = " SELECT " +
			  " C.coupon_id, C.restaurant_id, C.birth_date, C.draw_date, C.order_id, C.order_date, C.status, " +
			  " C.coupon_type_id, CT.name, CT.price, CT.expired, CT.oss_image_id, " +
			  " C.member_id, M.name AS member_name, M.mobile, M.member_card, M.`consumption_amount`, M.point, M.`base_balance`, M.`extra_balance`, MT.name AS memberTypeName, " +
			  " C.promotion_id, P.title " +
			  " FROM " + Params.dbName + ".coupon C " +
			  " JOIN " + Params.dbName + ".coupon_type CT ON C.coupon_type_id = CT.coupon_type_id " +
			  " JOIN " + Params.dbName + ".promotion P ON C.promotion_id = P.promotion_id " +
			  " JOIN " + Params.dbName + ".member M ON C.member_id = M.member_id " +
			  " JOIN " + Params.dbName + ".member_type MT ON M.member_type_id = MT.member_type_id " +
			  " WHERE 1 = 1 " +
			  " AND C.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Coupon coupon = new Coupon(dbCon.rs.getInt("coupon_id"));
			coupon.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			coupon.setBirthDate(dbCon.rs.getTimestamp("birth_date").getTime());
			if(dbCon.rs.getTimestamp("draw_date") != null){
				coupon.setDrawDate(dbCon.rs.getTimestamp("draw_date").getTime());
			}
			coupon.setOrderId(dbCon.rs.getInt("order_id"));
			if(dbCon.rs.getTimestamp("order_date") != null){
				coupon.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			}
			coupon.setStatus(Coupon.Status.valueOf(dbCon.rs.getInt("status")));
			
			CouponType ct = new CouponType(dbCon.rs.getInt("coupon_type_id")); 
			ct.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			ct.setPrice(dbCon.rs.getFloat("price"));
			ct.setName(dbCon.rs.getString("name"));
			ct.setExpired(dbCon.rs.getTimestamp("expired").getTime());
			ct.setImage(new OssImage(dbCon.rs.getInt("oss_image_id")));
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
			coupon.setPromotion(promotion);
			
			result.add(coupon);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	
	public static int delete(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " DELETE C FROM " + Params.dbName + ".coupon AS C " + 
			  " WHERE 1 = 1 " +
			  " AND C.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : "");
		
		return dbCon.stmt.executeUpdate(sql);
	}
	
}
