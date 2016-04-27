package com.wireless.db.promotion;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.promotion.CouponEffect;
import com.wireless.pojo.promotion.CouponOperation;
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.staffMgr.Staff;

public class CouponEffectDao {
	
	public static class ExtraCond{
		
		private String beginDate;
		private String endDate;
		private int couponTypeId;
		private int branchId;
		
		public ExtraCond setRange(String beginDate, String endDate){
			this.beginDate = beginDate;
			this.endDate = endDate;
			return this;
		}
		
		public ExtraCond setCouponType(CouponType couponType){
			this.couponTypeId = couponType.getId();
			return this;
		}
		
		public ExtraCond setCouponType(int couponId){
			this.couponTypeId = couponId;
			return this;
		}
		
		public ExtraCond setBranchId(int branchId){
			this.branchId = branchId;
			return this;
		}
		
		@Override
		public String toString(){
			final StringBuilder extraCond = new StringBuilder();
			if(this.beginDate != null && this.endDate != null){
				extraCond.append("  AND CO.operate_date BETWEEN '" + this.beginDate + "' AND '" + this.endDate + "'");
			}
			
			if(this.couponTypeId != 0){
				extraCond.append(" AND C.coupon_type_id =" + this.couponTypeId);
			}
			
			if(this.branchId != 0){
				extraCond.append(" AND CO.branch_id =" + this.branchId);
			}
			
			return extraCond.toString();
		}
		
		
	}
	
	/**
	 * Get the couponEffect to specific extra condition {@link ExtraCond}
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return	the result to couponEffect
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<CouponEffect> calcByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcByCond(dbCon, staff, extraCond);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the couponEffect to specific extra condition {@link ExtraCond}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return	the result to represent
	 * @throws SQLException
	 *			throws if failed to execute any SQL statement
	 */
	public static List<CouponEffect> calcByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		
		StringBuilder issueCond = new StringBuilder();
		for(CouponOperation.Operate issueOperation : CouponOperation.OperateType.ISSUE.operationOf()){
			if(issueCond.length() != 0){
				issueCond.append(",");
			}
			issueCond.append(issueOperation.getVal());
		}
		
		StringBuilder useCond = new StringBuilder();
		for(CouponOperation.Operate useOperation : CouponOperation.OperateType.USE.operationOf()){
			if(useCond.length() != 0){
				useCond.append(",");
			}
			useCond.append(useOperation.getVal());
		}
		
		String sql;
		sql = " SELECT MAX(CT.coupon_type_id) AS coupon_type_id, IFNULL(MAX(CT.name), '已删除的优惠活动') AS coupon_name, MAX(CT.price) AS coupon_price " + 
			  " ,SUM(IF(CO.operate IN (" + issueCond.toString() + "), 1, 0)) AS issued_amount " +
			  " ,SUM(IF(CO.operate IN (" + issueCond.toString() + "), CO.coupon_price, 0)) AS issued_price " +
			  " ,SUM(IF(CO.operate IN (" + useCond.toString() + "), 1, 0)) AS used_amount " +
			  " ,SUM(IF(CO.operate IN (" + useCond.toString() + "), CO.coupon_price, 0)) AS used_price " +
			  " FROM " + Params.dbName + ".coupon_operation CO " +
			  " LEFT JOIN " + Params.dbName + ".coupon C ON CO.coupon_id = C.coupon_id " +
			  " LEFT JOIN " + Params.dbName + ".coupon_type CT ON C.coupon_type_id = CT.coupon_type_id " +
			  " WHERE 1 = 1 " +
			  " AND CO.restaurant_id =" + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) +
			  (extraCond != null ? extraCond.toString() : "") +
			  " GROUP BY CT.coupon_type_id ";
		
		final List<CouponEffect> result = new ArrayList<>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			//获取到优惠券id
			CouponEffect couponEffect = new CouponEffect(dbCon.rs.getInt("coupon_type_id"));
			//开始日期
			couponEffect.setBeginDate(extraCond.beginDate);
			//结束日期
			couponEffect.setEndDate(extraCond.endDate);
			//优惠活动名称
			couponEffect.setCouponName(dbCon.rs.getString("coupon_name"));
			//优惠券面额
			couponEffect.setCouponPrice(dbCon.rs.getFloat("coupon_price"));
			//共发送总数量
			couponEffect.setIssuedAmount(dbCon.rs.getInt("issued_amount"));
			//共发送面额
			couponEffect.setIssuedPrice(dbCon.rs.getFloat("issued_price")); 
			//使用总数量
			couponEffect.setUsedAmount(dbCon.rs.getInt("used_amount"));
			//已使用总面额
			couponEffect.setUsedPrice(dbCon.rs.getFloat("used_price"));
			result.add(couponEffect);
		}
		dbCon.rs.close();
		
		sql = " SELECT CT.coupon_type_id, COUNT(*) AS sales_amount, SUM(TMP.actual_price) AS sales_price FROM( " +
			    " SELECT O.id, MAX(CO.`coupon_id`) AS coupon_id, MAX(O.actual_price) AS actual_price " +
			    " FROM " + Params.dbName + ".coupon_operation CO " +
			    " JOIN " + Params.dbName + ".order_history O ON CO.associate_id = O.id " +
			    " LEFT JOIN `" + Params.dbName + "`.`coupon` C ON  C.`coupon_id` = CO.coupon_id " +
			    " WHERE 1 = 1 " +
			    " AND CO.restaurant_id = " + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) +
			    " AND CO.operate =  " + CouponOperation.Operate.ORDER_USE.getVal() +
			    (extraCond != null ? extraCond.toString() : "") +
			    " GROUP BY O.id " +
				" ) AS TMP " +
			 " LEFT JOIN `" + Params.dbName + "`.`coupon` C ON C.`coupon_id` = TMP.coupon_id " +
			 " LEFT JOIN `" + Params.dbName + "`.`coupon_type` CT ON CT.`coupon_type_id` = C.`coupon_type_id`  " +
		     " GROUP BY CT.coupon_type_id; ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			int index = result.indexOf(new CouponEffect(dbCon.rs.getInt("coupon_type_id")));
			if(index >= 0){
				//拉动消费次数
				result.get(index).setSalesAmount(dbCon.rs.getInt("sales_amount"));
				//拉动消费额
				result.get(index).setEffectSales(dbCon.rs.getFloat("sales_price"));
			}
			
		}
		dbCon.rs.close();
		
		return result;
	}
	
	
	

}
