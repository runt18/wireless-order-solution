package com.wireless.db.billStatistics;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.billStatistics.CouponUsage;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.promotion.CouponOperation;
import com.wireless.pojo.staffMgr.Staff;

public class CalcCouponStatisticsDao {
	
	public static class ExtraCond{
		private DutyRange range;
		private CouponOperation.OperateType operateType;
		private CouponOperation.Operate operate;
		private int associateId;
		
		public ExtraCond setAssociateId(int associateId){
			this.associateId = associateId;
			return this;
		}
		
		public ExtraCond setOperate(CouponOperation.Operate operate){
			this.operate = operate;
			return this;
		}
		
		public ExtraCond setRange(DutyRange range){
			this.range = range;
			return this;
		}
		
		public ExtraCond setOperateType(CouponOperation.OperateType operateType){
			this.operateType = operateType;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(range != null){
				extraCond.append(" AND CO.operate_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'");
			}
			if(operate != null){
				extraCond.append(" AND CO.operate = " + operate.getVal());
			}
			if(associateId != 0){
				extraCond.append(" AND associate_id = " + associateId);
			}
			if(operateType != null){
				StringBuilder operateCond = new StringBuilder();
				for(CouponOperation.Operate operate : operateType.operationOf()){
					if(operateCond.length() > 0){
						operateCond.append(",");
					}
					operateCond.append(operate.getVal());
				}
				if(operateCond.length() > 0){
					extraCond.append(" AND CO.operate IN ( " + operateCond + ")");
				}
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Calculate the usage to used & issued coupons.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the coupon usage {@link CouponUsage}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * 		
	 */
	public static CouponUsage calcUsage(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		final CouponUsage result = new CouponUsage();
		String sql;
		//Get the usage to used coupon.
		sql = " SELECT TMP.coupon_name, COUNT(*) AS amount, SUM(TMP.coupon_price) AS price " +
			  " FROM ( " + makeSql(staff, extraCond.setOperateType(CouponOperation.OperateType.USE)) + " ) AS TMP " +
			  " GROUP BY TMP.coupon_type_id " +
			  " HAVING amount > 0";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			result.addUse(dbCon.rs.getString("coupon_name"), dbCon.rs.getInt("amount"), dbCon.rs.getFloat("price"));
		}
		dbCon.rs.close();
		
		//Get the usage to issued coupon.
		sql = " SELECT TMP.coupon_name, COUNT(*) AS amount, SUM(TMP.coupon_price) AS price " +
			  " FROM ( " + makeSql(staff, extraCond.setOperateType(CouponOperation.OperateType.ISSUE)) + " ) AS TMP " +
			  " GROUP BY TMP.coupon_type_id " +
			  " HAVING amount > 0 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			result.addIssue(dbCon.rs.getString("coupon_name"), dbCon.rs.getInt("amount"), dbCon.rs.getFloat("price"));
		}
		dbCon.rs.close();
		
		return result;
	}
	
	private static String makeSql(Staff staff, ExtraCond extraCond){
		String sql;
		sql = " SELECT CO.coupon_name, CO.coupon_price, C.coupon_type_id " +
			  " FROM " + Params.dbName + ".coupon_operation CO " +
			  " LEFT JOIN " + Params.dbName + ".coupon C ON CO.coupon_id = C.coupon_id " +
			  " WHERE 1 = 1 " +
			  " AND CO.restaurant_id = " + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) +
			  (extraCond != null ? extraCond.toString() : "");
		return sql;
	}
}
