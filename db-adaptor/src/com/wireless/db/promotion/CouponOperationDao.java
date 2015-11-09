package com.wireless.db.promotion;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.member.MemberDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponOperation;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class CouponOperationDao {

	public static class ExtraCond{
		private final List<CouponOperation.Operate> operations = new ArrayList<CouponOperation.Operate>();
		private int associateId;
		private int couponId;
		private DutyRange range;
		private HourRange hourRange;
		private CouponOperation.Operate operate;
		private CouponOperation.OperateType operateType;
		private int staffId;
		private int memberId;
		private String memberFuzzy;
		private Staff internalStaff;
		
		private ExtraCond setInternalStaff(Staff staff){
			this.internalStaff = staff;
			return this;
		}
		
		public ExtraCond setMemberFuzzy(String fuzzy){
			this.memberFuzzy = fuzzy;
			return this;
		}
		
		public ExtraCond setMember(int memberId){
			this.memberId = memberId;
			return this;
		}
		
		public ExtraCond setOperate(CouponOperation.Operate operate){
			this.operate = operate;
			return this;
		}
		
		public ExtraCond setOperateType(CouponOperation.OperateType operateType){
			this.operateType = operateType;
			return this;
		}
		
		public ExtraCond setStaff(Staff staff){
			this.staffId = staff.getId();
			return this;
		}
		
		public ExtraCond setStaff(int staffId){
			this.staffId = staffId;
			return this;
		}

		public ExtraCond setHourRange(String begin, String end) throws ParseException{
			this.hourRange = new HourRange(begin, end, DateUtil.Pattern.HOUR);
			return this;
		}
		
		public ExtraCond setRange(DutyRange range){
			this.range = range;
			return this;
		}
		
		public ExtraCond setRange(String begin, String end){
			this.range = new DutyRange(begin, end);
			return this;
		}
		
		public ExtraCond setCoupon(Coupon coupon){
			this.couponId = coupon.getId();
			return this;
		}
		
		public ExtraCond setCoupon(int couponId){
			this.couponId = couponId;
			return this;
		}
		
		public ExtraCond addOperation(CouponOperation.OperateType type){
			for(CouponOperation.Operate operation : type.operationOf()){
				addOperation(operation);
			}
			return this;
		}
		
		public ExtraCond addOperation(CouponOperation.Operate operation){
			if(!this.operations.contains(operation)){
				this.operations.add(operation);
			}
			return this;
		}
		
		public ExtraCond setAssociateId(int associateId){
			this.associateId = associateId;
			return this;
		}
		
		@Override
		public String toString(){
			final StringBuilder extraCond = new StringBuilder();
			final StringBuilder operateCond = new StringBuilder();
			for(CouponOperation.Operate operation : operations){
				if(operateCond.length() > 0){
					operateCond.append(",");
				}
				operateCond.append(operation.getVal());
			}
			if(operateCond.length() > 0){
				extraCond.append(" AND operate IN ( " + operateCond + ")");
			}
			if(associateId != 0){
				extraCond.append(" AND associate_id = " + associateId);
			}
			if(couponId != 0){
				extraCond.append(" AND coupon_id = " + couponId);
			}
			if(range != null){
				extraCond.append(" AND operate_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'");
			}
			if(hourRange != null){
				extraCond.append(" AND operate_date BETWEEN '" + hourRange.getOpeningFormat() + "' AND '" + hourRange.getEndingFormat() + "'");
			}
			if(staffId != 0){
				extraCond.append(" AND operate_staff_id = " + staffId);
			}
			if(operate != null){
				extraCond.append(" AND operate = " + operate.getVal());
			}
			if(operateType != null){
				StringBuilder operateTypeCond = new StringBuilder();
				for(CouponOperation.Operate operate : operateType.operationOf()){
					if(operateTypeCond.length() > 0){
						operateTypeCond.append(",");
					}
					operateTypeCond.append(operate.getVal());
				}
				extraCond.append(" AND operate IN (" + operateTypeCond + ")");
			}
			if(memberId != 0){
				extraCond.append(" AND member_id = " + memberId);
			}
			if(memberFuzzy != null){
				final StringBuilder fuzzyCond = new StringBuilder();
				try {
					for(Member member : MemberDao.getByCond(internalStaff, new MemberDao.ExtraCond().setFuzzyName(memberFuzzy), null)){
						if(fuzzyCond.length() > 0){
							fuzzyCond.append(",");
						}
						fuzzyCond.append(member.getId());
					}
					if(fuzzyCond.length() > 0){
						extraCond.append(" AND member_id IN ( " + fuzzyCond + ")");
					}else{
						extraCond.append(" AND member_id = -1");
					}
				} catch (SQLException | BusinessException ignored) {
					ignored.printStackTrace();
				}
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert the coupon operation according to specific builder {@link CouponOperation#InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a coupon operation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the coupon to operate does NOT exist
	 */
	static void insert(DBCon dbCon, Staff staff, CouponOperation.InsertBuilder builder) throws SQLException, BusinessException{
		
		CouponOperation detail = builder.build();
		
		deleteByCond(dbCon, staff, new ExtraCond().setCoupon(detail.getCouponId())
												  .addOperation(detail.getOperate())
												  .setAssociateId(detail.getAssociateId()));
		
		Coupon coupon = CouponDao.getById(dbCon, staff, detail.getCouponId());
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".coupon_operation " +
			  " (restaurant_id, coupon_id, coupon_name, coupon_price, operate, associate_id, operate_date, operate_staff, operate_staff_id, member_id, member_name, comment) VALUES( " +
			  staff.getRestaurantId() + "," +
			  coupon.getId() + "," +
			  "'" + coupon.getName() + "'," +
			  coupon.getPrice() + "," +
			  detail.getOperate().getVal() + "," +
			  detail.getAssociateId() + "," +
			  " NOW(), " +
			  "'" + staff.getName() + "'," +
			  staff.getId() + "," +
			  coupon.getMember().getId() + "," +
			  "'" + coupon.getMember().getName() + "'," +
			  "'" + detail.getComment() + "'" +
			  ")";
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Get the coupon operation to specific extra condition{@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list to coupon operation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<CouponOperation> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the coupon operation to specific extra condition{@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list to coupon operation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<CouponOperation> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".coupon_operation WHERE restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.setInternalStaff(staff) : "") +
			  " ORDER BY operate_date DESC ";
		
		final List<CouponOperation> result = new ArrayList<>();
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			CouponOperation operation = new CouponOperation(dbCon.rs.getInt("id"));
			operation.setCouponId(dbCon.rs.getInt("coupon_id"));
			operation.setCouponName(dbCon.rs.getString("coupon_name"));
			operation.setCouponPrice(dbCon.rs.getFloat("coupon_price"));
			operation.setAssociateId(dbCon.rs.getInt("associate_id"));
			operation.setOperate(CouponOperation.Operate.valueOf(dbCon.rs.getInt("operate")));
			operation.setOperateDate(dbCon.rs.getTimestamp("operate_date").getTime());
			operation.setOperateStaff(dbCon.rs.getString("operate_staff"));
			operation.setComment(dbCon.rs.getString("comment"));
			operation.setMemberId(dbCon.rs.getInt("member_id"));
			operation.setMemberName(dbCon.rs.getString("member_name"));
			result.add(operation);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(CouponOperation operation : getByCond(dbCon, staff, extraCond)){
			String sql = " DELETE FROM " + Params.dbName + ".coupon_operation WHERE id = " + operation.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		return amount;
	}
	
}
