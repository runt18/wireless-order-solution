package com.wireless.db.member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.DBTbl;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.member.MOSummary;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberOperation.ChargeType;
import com.wireless.pojo.member.MemberOperation.OperationCate;
import com.wireless.pojo.member.MemberOperation.OperationType;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.DateUtil.Pattern;
import com.wireless.util.SQLUtil;

public class MemberOperationDao {
	
	public static class ExtraCond4Consume extends ExtraCond{

		public ExtraCond4Consume(DateType dateType) {
			super(dateType);
		}
		
		@Override
		public String toString(){
			final StringBuilder extraCond = new StringBuilder();
			
			final StringBuilder operationCond = new StringBuilder();
			for(OperationType type : OperationType.typeOf(OperationCate.CONSUME_TYPE)){
				if(operationCond.length() != 0){
					operationCond.append(",");
				}
				operationCond.append(type.getValue());
			}
			extraCond.append(" AND MO.id IN (" +
							 " SELECT MAX(id) FROM wireless_order_db." + super.dbTbl.moTbl + 
							 " WHERE 1 = 1 " + 
							 " AND " + (super.branchId != 0 ? " branch_id = " + super.branchId : " restaurant_id = " + super.restaurantId) +
							 " AND operate_type IN (" + operationCond.toString() + ") GROUP BY order_id )");
			
			return super.toString() + extraCond.toString();
		}
		
	}
	
	public static class ExtraCond{
		protected final DBTbl dbTbl;
		private int id;
		private final List<Integer> members = new ArrayList<Integer>();
		private int memberTypeId;
		private int orderId;
		private String mobile;
		private String card;
		private String name;
		private final List<MemberOperation.OperationType> operationTypes = new ArrayList<MemberOperation.OperationType>();
		private DutyRange operationDate;
		private int payTypeId;
		private int chargeTypeId;
		private boolean containsCoupon;
		private int branchId;
		private int restaurantId;
		private Float minChargeAmount;
		private Float maxChargeAmount;
		private String comment;
		
		public ExtraCond(DateType dateType){
			this.dbTbl = new DBTbl(dateType);
		}
		
		public ExtraCond setComment(String comment){
			this.comment = comment;
			return this;
		}
		
		public ExtraCond setMinChargeAmount(Float min){
			this.minChargeAmount = min;
			return this;
		}
		
		public ExtraCond setMaxChargeAmount(Float max){
			this.maxChargeAmount = max;
			return this;
		}
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		ExtraCond setRestaurant(int restaurantId){
			this.restaurantId = restaurantId;
			return this;
		}
		
		public ExtraCond setBranch(int branchId){
			this.branchId = branchId;
			return this;
		}
		
		public ExtraCond setBranch(Restaurant branch){
			return setBranch(branch.getId());
		}
		
		public ExtraCond addMember(int memberId){
			this.members.add(memberId);
			return this;
		}
		
		public ExtraCond addMember(Member member){
			this.members.add(member.getId());
			return this;
		}
		
		public ExtraCond setMemberType(MemberType memberType){
			this.memberTypeId = memberType.getId();
			return this;
		}
		
		public ExtraCond setMemberType(int memberTypeId){
			this.memberTypeId = memberTypeId;
			return this;
		}
		
		public ExtraCond setOrder(int orderId){
			this.orderId = orderId;
			return this;
		}
		
		public ExtraCond setOrder(Order order){
			this.orderId = order.getId();
			return this;
		}
		
		public ExtraCond setMobile(String mobile){
			this.mobile = mobile;
			return this;
		}
		
		public ExtraCond setCard(String card){
			this.card = card;
			return this;
		}
		
		public ExtraCond setName(String name){
			this.name = name;
			return this;
		}
		
		public ExtraCond addOperationType(OperationType type){
			if(type != null){
				operationTypes.add(type);
			}
			return this;
		}

		public ExtraCond addOperationType(List<OperationType> types){
			if(types != null){
				operationTypes.addAll(types);
			}
			return this;
		}
		
		public ExtraCond setOperateDate(DutyRange range){
			this.operationDate = range;
			return this;
		}
		
		public ExtraCond setContainsCoupon(boolean onOff){
			this.containsCoupon = onOff;
			return this;
		}

		public ExtraCond clearOperationType(){
			this.operationTypes.clear();
			return this;
		}
		
		public ExtraCond setPayType(PayType payType){
			this.payTypeId = payType.getId();
			return this;
		}
		
		public ExtraCond setPayType(int payTypeId){
			this.payTypeId = payTypeId;
			return this;
		}
		
		public ExtraCond setChargeType(ChargeType chargeType){
			this.chargeTypeId = chargeType.getValue();
			return this;
		}
		
		public ExtraCond setChargeType(int chargeTypeId){
			this.chargeTypeId = chargeTypeId;
			return this;
		}
		
		@Override
		public String toString(){
			final StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND MO.id = " + id);
			}
			if(payTypeId != 0){
				extraCond.append(" AND PT.pay_type_id = " + payTypeId);
			}
			if(chargeTypeId != 0){
				extraCond.append(" AND MO.charge_type = " + chargeTypeId);
			}
			StringBuilder memberCond = new StringBuilder();
			for(Integer memberId : this.members){
				if(memberCond.length() != 0){
					memberCond.append(",").append(memberId.intValue());
				}else{
					memberCond.append(memberId.intValue());
				}
			}
			if(memberCond.length() != 0){
				extraCond.append(" AND MO.member_id IN (" + memberCond + ")");
			}
			if(memberTypeId != 0){
				extraCond.append(" AND M.member_type_id = " + memberTypeId);
			}
			if(orderId != 0){
				extraCond.append(" AND MO.order_id = " + orderId);
			}
			StringBuilder operateTypeCond = new StringBuilder();
			for(MemberOperation.OperationType type : operationTypes){
				if(operateTypeCond.length() == 0){
					operateTypeCond.append(type.getValue());
				}else{
					operateTypeCond.append("," + type.getValue());
				}
			}
			if(operateTypeCond.length() != 0){
				extraCond.append(" AND MO.operate_type IN (" + operateTypeCond.toString() + ")");
			}
			if(operationDate != null){
				extraCond.append(" AND MO.operate_date BETWEEN '" + operationDate.getOnDutyFormat() + "' AND '" + operationDate.getOffDutyFormat() + "'");
			}
			if(mobile != null){
				extraCond.append(" AND MO.member_mobile LIKE '%" + mobile.trim() + "%'");
			}
			if(card != null){
				extraCond.append(" AND MO.member_card LIKE '%" + card.trim() + "%'");
			}
			if(name != null){
				extraCond.append(" AND MO.member_name LIKE '%" + name.trim() + "%'");
			}
			if(containsCoupon){
				extraCond.append(" AND MO.coupon_id > 0 AND MO.operate_type = " + OperationType.CONSUME.getValue());
			}
			if(branchId != 0){
				extraCond.append(" AND MO.branch_id = " + branchId);
			}
			if(minChargeAmount != null && maxChargeAmount != null){
				extraCond.append(" AND delta_extra_money BETWEEN " + minChargeAmount + " AND " + maxChargeAmount);
			}else if(minChargeAmount != null && maxChargeAmount == null){
				extraCond.append(" AND delta_extra_money > " + minChargeAmount);
			}else if(minChargeAmount == null && maxChargeAmount != null){
				extraCond.append(" AND delta_extra_money < " + maxChargeAmount);
			}
			if(comment != null){
				extraCond.append(" AND MO.comment LIKE '%" + comment + "%'");
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert a new member operation.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action 
	 * @param mo
	 *            the member operation to insert
	 * @return the row count for the SQL statements 
	 * @throws SQLException
	 *             throws if failed to execute any SQL statements
	 */
	public static int insert(DBCon dbCon, Staff staff, MemberOperation mo) throws SQLException {
		
		//Build the operate date and sequence.
		long now = System.currentTimeMillis();
		mo.setOperateDate(now);
		mo.setOperateSeq(mo.getOperationType().getPrefix().concat(DateUtil.format(now, Pattern.MO_SEQ)));

		if(staff.isBranch()){
			mo.setRestaurantId(staff.getGroupId());
			mo.setBranchId(staff.getRestaurantId());
		}else{
			mo.setRestaurantId(staff.getRestaurantId());
		}
		mo.setStaffId(staff.getId());
		mo.setStaffName(staff.getName());
		
		String sql;
		sql = " INSERT INTO " +
			  Params.dbName + ".member_operation " +
			  "(" +
			  " restaurant_id, branch_id, staff_id, staff_name, member_id, member_card, member_name, member_mobile," +
			  " operate_seq, operate_date, operate_type, pay_type_id, pay_money, order_id, charge_type, charge_money, " +
			  " coupon_id, coupon_money, coupon_name, " +
			  " delta_base_money, delta_extra_money, delta_point, "	+
			  " remaining_base_money, remaining_extra_money, remaining_point, comment "	+
			  ")" +
			  " VALUES( " +
		      (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) + "," +
			  staff.getRestaurantId() + "," +
		      mo.getStaffId() + "," +
		      "'" + mo.getStaffName() + "'," + 
		      mo.getMemberId() + "," +
		      "'" + mo.getMemberCard() + "'," + 
		      "'" + mo.getMemberName() + "'," +
		      "'" + mo.getMemberMobile() + "'," +
		      "'" + mo.getOperateSeq() + "'," +
		      "'" + DateUtil.format(mo.getOperateDate()) + "'," + 
		      mo.getOperationType().getValue() + "," + 
		      (mo.getOperationType().isConsume() ? mo.getPayType().getId() : "NULL") + "," +
		      (mo.getOperationType().isConsume() ? mo.getPayMoney() : "NULL") + "," + 
		      (mo.getOperationType().isConsume() ? mo.getOrderId() : "NULL") + "," +
		      (mo.getOperationType() == OperationType.CHARGE ? mo.getChargeType().getValue() : (mo.getOperationType() == OperationType.REFUND ? mo.getChargeType().getValue() : "NULL")) + "," + 
		      (mo.getOperationType() == OperationType.CHARGE ? mo.getChargeMoney() : (mo.getOperationType() == OperationType.REFUND ? mo.getChargeMoney() : "NULL")) + "," +
		      mo.getCouponId() + "," +
		      mo.getCouponMoney() + "," +
		      "'" + mo.getCouponName() + "'," +
		      mo.getDeltaBaseMoney() + "," + 
		      mo.getDeltaExtraMoney() + "," + 
		      mo.getDeltaPoint() + ","	+ 
		      mo.getRemainingBaseMoney() + "," + 
		      mo.getRemainingExtraMoney() + "," + 
		      mo.getRemainingPoint() + "," +
		      "'" + mo.getComment() + "'" + 
		      ") ";
		
		int count = dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		//Get the generated id to this member operation. 
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			mo.setId(dbCon.rs.getInt(1));
			return count;
		}else{
			throw new SQLException("The id of member operation is not generated successfully.");
		}
	}
	
	/**
	 * Insert a new member operation.
	 * 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param mo
	 *          the member operation to insert
	 * @return the row count for the SQL statements 
	 * @throws SQLException
	 *             Throws if failed to execute any SQL statements.
	 */
	public static int insert(Staff staff, MemberOperation mo) throws SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.insert(dbCon, staff, mo);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member operation according to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result to member operation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<MemberOperation> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member operation according to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result to member operation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<MemberOperation> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " MO.id, MO.restaurant_id, MO.branch_id, R.restaurant_name AS branch_name, MO.staff_id, MO.staff_name, " +
			  " MO.member_id, MO.member_card, MO.member_name, MO.member_mobile, " +
			  " MO.pay_type_id, IFNULL(PT.name, '其他') AS pay_type_name, MO.pay_money, " +
			  " MO.operate_seq, MO.operate_date, MO.operate_type, MO.order_id, MO.charge_type, MO.charge_money,"	+
			  " MO.delta_base_money, MO.delta_extra_money, MO.delta_point, " +
			  " MO.remaining_base_money, MO.remaining_extra_money, MO.remaining_point, MO.comment, " +
			  " MO.coupon_id, MO.coupon_money, MO.coupon_name " +
			  " FROM " + Params.dbName + "." + extraCond.dbTbl.moTbl + " MO " +
			  " LEFT JOIN " + Params.dbName + ".member M ON MO.member_id = M.member_id "	+
			  " LEFT JOIN " + Params.dbName + ".pay_type PT ON PT.pay_type_id = MO.pay_type_id " +
			  " LEFT JOIN " + Params.dbName + ".restaurant R ON R.id = MO.branch_id " +
			  " WHERE 1 = 1 " +
			  " AND MO.restaurant_id = " + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) +
			  extraCond.setRestaurant(staff.getRestaurantId()).toString() +
			  (orderClause != null ? orderClause : " ORDER BY MO.id DESC ");
		
		List<MemberOperation> result = new ArrayList<MemberOperation>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			MemberOperation mo = MemberOperation.newMO(dbCon.rs.getInt("member_id"), 
													   dbCon.rs.getString("member_name"), 
													   dbCon.rs.getString("member_mobile"), 
													   dbCon.rs.getString("member_card"));
			mo.setId(dbCon.rs.getInt("id"));
			mo.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			mo.setBranchId(dbCon.rs.getInt("branch_id"));
			mo.setBranchName(dbCon.rs.getString("branch_name"));
			mo.setStaffId(dbCon.rs.getInt("staff_id"));
			mo.setStaffName(dbCon.rs.getString("staff_name"));
			mo.setOperateSeq(dbCon.rs.getString("operate_seq"));
			mo.setOperateDate(dbCon.rs.getTimestamp("operate_date").getTime());
			mo.setOperationType(dbCon.rs.getShort("operate_type"));
			if(mo.getOperationType().isConsume()){
				PayType payType = new PayType(dbCon.rs.getInt("pay_type_id"));
				payType.setName(dbCon.rs.getString("pay_type_name"));
				mo.setPayType(payType);
				mo.setPayMoney(dbCon.rs.getFloat("pay_money"));
				mo.setOrderId(dbCon.rs.getInt("order_id"));
			}
			if(mo.getOperationType() == OperationType.CHARGE || mo.getOperationType() == OperationType.REFUND){
				mo.setChargeType(dbCon.rs.getShort("charge_type"));
				mo.setChargeMoney(dbCon.rs.getFloat("charge_money"));
			}
			mo.setDeltaBaseMoney(dbCon.rs.getFloat("delta_base_money"));
			mo.setDeltaExtraMoney(dbCon.rs.getFloat("delta_extra_money"));
			mo.setDeltaPoint(dbCon.rs.getInt("delta_point"));
			mo.setRemainingBaseMoney(dbCon.rs.getFloat("remaining_base_money"));
			mo.setRemainingExtraMoney(dbCon.rs.getFloat("remaining_extra_money"));
			mo.setRemainingPoint(dbCon.rs.getInt("remaining_point"));
			mo.setComment(dbCon.rs.getString("comment"));
			
			mo.setCouponId(dbCon.rs.getInt("coupon_id"));
			mo.setCouponMoney(dbCon.rs.getFloat("coupon_money"));
			mo.setCoupnName(dbCon.rs.getString("coupon_name"));
			
			result.add(mo);
		}
		dbCon.rs.close();
		return result;
	}
	
	/**
	 * Get the last member operation to specific order id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param orderId
	 * 			the order id 
	 * @return the member operation to this order id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member operation to this order id does NOT exist
	 */
	public static MemberOperation getLastConsumptionByOrder(Staff staff, Order order) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getLastConsumptionByOrder(dbCon, staff, order);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member operation to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param dateType
	 * 			the date type {@link DateType}
	 * @param id
	 * 			the id
	 * @return the member operation to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member operation to this id does NOT exist
	 */
	public static MemberOperation getById(Staff staff, DateType dateType, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, dateType, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member operation to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param dateType
	 * 			the date type {@link DateType}
	 * @param id
	 * 			the id
	 * @return the member operation to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member operation to this id does NOT exist
	 */
	public static MemberOperation getById(DBCon dbCon, Staff staff, DateType dateType, int id) throws SQLException, BusinessException{
		List<MemberOperation> result = getByCond(dbCon, staff, new ExtraCond(dateType).setId(id), null);
		if(result.isEmpty()){
			throw new BusinessException(MemberError.OPERATION_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the last consumption member operation to specific order id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param orderId
	 * 			the order id 
	 * @return the member operation to this order id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member operation to this order id does NOT exist
	 */
	public static MemberOperation getLastConsumptionByOrder(DBCon dbCon, Staff staff, Order order) throws SQLException, BusinessException{
		List<MemberOperation> result = getByCond(dbCon, staff, new MemberOperationDao.ExtraCond(DateType.TODAY).setOrder(order)
																				.addOperationType(OperationType.typeOf(OperationCate.CONSUME_TYPE)),
																				" ORDER BY id DESC LIMIT 1 ");
		if(result.isEmpty()){
			throw new BusinessException(MemberError.OPERATION_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get amount to member operation according to extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount to member operation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int getAmountByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getAmountByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get amount to member operation according to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount to member operation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int getAmountByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT COUNT(*) " +
			  " FROM " + Params.dbName + "." + extraCond.dbTbl.moTbl + " MO " +
			  " JOIN " + Params.dbName + ".member M ON MO.member_id = M.member_id "	+
			  " LEFT JOIN " + Params.dbName + ".pay_type PT ON PT.pay_type_id = MO.pay_type_id " +
			  " WHERE 1 = 1 " +
			  " AND MO.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int amount = 0;
		if(dbCon.rs.next()){
			amount = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		return amount;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	private static int getSummaryByTodayCount(DBCon dbCon, Map<Object, Object> params) throws BusinessException, SQLException{
		int count = 0;
		String querySQL = "SELECT COUNT(MO.member_id) FROM member_operation_history MO LEFT JOIN member M ON MO.member_id = M.member_id WHERE MO.member_id > 0 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		querySQL = "SELECT COUNT(*) FROM (" + querySQL + ") TT";
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next()){
			count = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		return count;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int getSummaryByTodayCount(Map<Object, Object> params) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getSummaryByTodayCount(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static List<MOSummary> getSummaryByToday(DBCon dbCon, Staff staff, Map<Object, Object> params) throws BusinessException, SQLException{
		List<MOSummary> list = new ArrayList<MOSummary>();
		MOSummary item = null;
		String querySQL = "SELECT"
			+ " MO.member_id, IFNULL(SUM(MO.charge_money), 0) charge_money, IFNULL(SUM(MO.pay_money), 0) pay_money,"
			+ " SUM(CASE WHEN MO.operate_type = 2 THEN 1 ELSE 0 END) consume_amount,"
			+ " SUM(CASE WHEN MO.operate_type = 2 THEN MO.delta_point ELSE 0 END) consume_point,"
			+ " SUM(CASE WHEN MO.operate_type = 3 THEN MO.delta_point ELSE 0 END) point_consume,"
			+ " SUM(CASE WHEN MO.operate_type = 4 THEN MO.delta_point ELSE 0 END) point_adjust,"
			+ " SUM(CASE WHEN MO.operate_type = 5 THEN MO.delta_base_money + MO.delta_extra_money ELSE 0 END) money_adjust"
			+ " FROM member_operation MO LEFT JOIN member M ON MO.member_id = M.member_id "
			+ " WHERE MO.member_id > 0 ";
		params.put(SQLUtil.SQL_PARAMS_GROUPBY, " GROUP BY MO.member_id ");
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		Statement stmt = dbCon.conn.createStatement();
		ResultSet rs = stmt.executeQuery(querySQL);
		while(rs != null && rs.next()){
			item = new MOSummary();
			item.setMember(MemberDao.getById(dbCon, staff, rs.getInt("member_id")));
			item.setChargeMoney(rs.getFloat("charge_money"));
			item.setConsumeAmount(rs.getInt("consume_amount"));
			item.setPayMoney(rs.getFloat("pay_money"));
			item.setConsumePoint(rs.getFloat("consume_point"));
			item.setPointConsume(rs.getFloat("point_consume"));
			item.setPointAdjust(rs.getFloat("point_adjust"));
			item.setMoneyAdjust(rs.getFloat("money_adjust"));
			
			list.add(item);
		}
		item = null;
		rs.close();
		rs = null;
		stmt.close();
		stmt = null;
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static List<MOSummary> getSummaryByToday(Staff staff, Map<Object, Object> params) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getSummaryByToday(dbCon, staff, params);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int getSummaryByHistoryCount(DBCon dbCon, Map<Object, Object> params) throws BusinessException, SQLException{
		int count = 0;
		String querySQL = "SELECT COUNT(MO.member_id) FROM member_operation_history MO LEFT JOIN member M ON MO.member_id = M.member_id WHERE MO.member_id > 0 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		querySQL = "SELECT COUNT(*) FROM (" + querySQL + ") TT";
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next()){
			count = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		return count;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int getSummaryByHistoryCount(Map<Object, Object> params) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getSummaryByHistoryCount(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static List<MOSummary> getSummaryByHistory(DBCon dbCon, Staff staff, Map<Object, Object> params) throws BusinessException, SQLException{
		List<MOSummary> list = new ArrayList<MOSummary>();
		MOSummary item = null;
		String querySQL = "SELECT"
			+ " MO.member_id, IFNULL(SUM(MO.charge_money), 0) charge_money, IFNULL(SUM(MO.pay_money), 0) pay_money,"
			+ " SUM(CASE WHEN MO.operate_type = 2 THEN 1 ELSE 0 END) consume_amount,"
			+ " SUM(CASE WHEN MO.operate_type = 2 THEN MO.delta_point ELSE 0 END) consume_point,"
			+ " SUM(CASE WHEN MO.operate_type = 3 THEN MO.delta_point ELSE 0 END) point_consume,"
			+ " SUM(CASE WHEN MO.operate_type = 4 THEN MO.delta_point ELSE 0 END) point_adjust,"
			+ " SUM(CASE WHEN MO.operate_type = 5 THEN MO.delta_base_money + MO.delta_extra_money ELSE 0 END) money_adjust"
			+ " FROM member_operation_history MO LEFT JOIN member M ON MO.member_id = M.member_id "
			+ " WHERE MO.member_id > 0 ";
		params.put(SQLUtil.SQL_PARAMS_GROUPBY, " GROUP BY MO.member_id ");
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		Statement stmt = dbCon.conn.createStatement();
		ResultSet rs = stmt.executeQuery(querySQL);
		while(rs != null && rs.next()){
			item = new MOSummary();
			item.setMember(MemberDao.getById(dbCon, staff, rs.getInt("member_id")));
			item.setChargeMoney(rs.getFloat("charge_money"));
			item.setConsumeAmount(rs.getInt("consume_amount"));
			item.setPayMoney(rs.getFloat("pay_money"));
			item.setConsumePoint(rs.getFloat("consume_point"));
			item.setPointConsume(rs.getFloat("point_consume"));
			item.setPointAdjust(rs.getFloat("point_adjust"));
			item.setMoneyAdjust(rs.getFloat("money_adjust"));
			
			list.add(item);
		}
		item = null;
		rs.close();
		rs = null;
		stmt.close();
		stmt = null;
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static List<MOSummary> getSummaryByHistory(Staff staff, Map<Object, Object> params) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getSummaryByHistory(dbCon, staff, params);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static class ArchiveResult{
		private final int maxId;
		private final int moAmount;
		public ArchiveResult(int maxId, int moAmount){
			this.maxId = maxId;
			this.moAmount = moAmount;
		}
		
		public int getMaxId(){
			return this.maxId;
		}
		
		public int getAmount(){
			return this.moAmount;
		}
		
		@Override
		public String toString(){
			return moAmount + " record(s) are moved to history, maxium id : " + maxId;
		}
	}

	/**
	 * Archive the daily member operation records from today to history.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the amount to records archived
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static ArchiveResult archive4Daily(DBCon dbCon, Staff staff) throws SQLException{
		return archive(dbCon, staff, null, DateType.TODAY, DateType.HISTORY);
	}

	/**
	 * Archive the expired member operation records from history to archive.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the amount to records archived
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static ArchiveResult archive4Expired(DBCon dbCon, final Staff staff) throws SQLException{
		ExtraCond extraCond4Expired = new ExtraCond(DateType.HISTORY){
			@Override
			public String toString(){
				return " AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(MO.operate_date) > REST.record_alive ";
			}
		};
		return archive(dbCon, staff, extraCond4Expired, DateType.HISTORY, DateType.ARCHIVE);
	}
	
	private static ArchiveResult archive(DBCon dbCon, Staff staff, ExtraCond extraCond, DateType archiveFrom, DateType archiveTo) throws SQLException{
		String sql;
		
		DBTbl fromTbl = new DBTbl(archiveFrom);
		DBTbl toTbl = new DBTbl(archiveTo);
		
		sql = " SELECT COUNT(*) FROM " + Params.dbName + "." + fromTbl.moTbl + 
			  " WHERE 1 = 1 " + 
			  " AND restaurant_id = " + (staff.isBranch() ? staff.getGroupId() :  staff.getRestaurantId()) +
			  " AND branch_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int moAmount = 0;
		if(dbCon.rs.next()){
			moAmount = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		if(moAmount == 0){
			return new ArchiveResult(0, 0);
		}
		
		final String item = "`restaurant_id`, `branch_id`, `staff_id`, `staff_name`, `member_id`, `member_name`, `member_mobile`, `member_card`, `operate_seq`, `operate_date`," +
							"`operate_type`, `pay_type_id`, `pay_money`, `coupon_id`, `coupon_money`, `coupon_name`, `order_id`, `charge_type`, `charge_money`, `delta_base_money`," +
							"`delta_extra_money`, `delta_point`, `remaining_base_money`, `remaining_extra_money`, `remaining_point`, `comment`";
		
		//Move the member operation record from 'member_operation' to 'member_operation_history'
		sql = " INSERT INTO " + Params.dbName + "." + toTbl.moTbl + "(" + item + ")" +
			  " SELECT " + item + " FROM " + Params.dbName + "." + fromTbl.moTbl + " MO " +
			  " JOIN " + Params.dbName + ".restaurant REST ON REST.id = IFNULL(MO.branch_id, MO.restaurant_id) " +
			  " WHERE 1 = 1 " + 
			  " AND restaurant_id = " + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) +
			  " AND branch_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		moAmount = dbCon.stmt.executeUpdate(sql);
		
		//Delete the member operation to this restaurant
		sql = " DELETE MO FROM " + Params.dbName + "." + fromTbl.moTbl + " MO " +
			  " JOIN " + Params.dbName + ".restaurant REST ON REST.id = MO.restaurant_id " +
			  " WHERE 1 = 1 " + 
			  " AND restaurant_id = " + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) +
			  " AND branch_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		dbCon.stmt.executeUpdate(sql);
		
		return new ArchiveResult(0, moAmount);
	}
}
