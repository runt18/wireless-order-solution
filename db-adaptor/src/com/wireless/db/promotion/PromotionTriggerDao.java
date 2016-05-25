package com.wireless.db.promotion;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.promotion.PromotionTrigger;
import com.wireless.pojo.staffMgr.Staff;

public class PromotionTriggerDao {

	public static class ExtraCond{
		private int id;
		private int promotionId;
		private PromotionTrigger.Type type;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setPromotion(Promotion promotion){
			this.promotionId = promotion.getId();
			return this;
		}
		
		public ExtraCond setPromotion(int promotionId){
			this.promotionId = promotionId;
			return this;
		}
		
		public ExtraCond setType(PromotionTrigger.Type type){
			this.type = type;
			return this;
		}
		
		@Override
		public String toString(){
			final StringBuilder extraCond = new StringBuilder();
			if(id > 0){
				extraCond.append(" AND PT.id = " + id);
			}
			if(promotionId > 0){
				extraCond.append(" AND PT.promotion_id = " + promotionId);
			}
			if(type != null){
				extraCond.append(" AND PT.type = " + type.getVal());
			}
			return extraCond.toString();
		}
	}

	/**
	 * Insert the promotion trigger according to builder {@link PromotionTrigger#InsertBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder {@link PromotionTrigger#InsertBuilder}
	 * @return the id to promotion trigger inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, PromotionTrigger.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the promotion trigger according to builder {@link PromotionTrigger#InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder {@link PromotionTrigger#InsertBuilder}
	 * @return the id to promotion trigger inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, PromotionTrigger.InsertBuilder builder) throws SQLException{
		PromotionTrigger trigger = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".promotion_trigger " +
		      " (restaurant_id, promotion_id, type, rule, extra) VALUES ( " +
			  staff.getRestaurantId() +
			  "," + trigger.getPromotionId() + 
			  "," + trigger.getType().getVal() +
			  "," + (trigger.getType().isIssue() ? trigger.getIssueRule().getVal() : trigger.getUseRule().getVal()) +
			  "," + trigger.getExtra() +
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int triggerId;
		if(dbCon.rs.next()){
			triggerId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("Failed to generated the promotion trigger id.");
		}
		
		return triggerId;
	}
	
	/**
	 * Update the promotion trigger to update builder {@link PromotionTrigger#UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the promotion trigger to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, PromotionTrigger.UpdateBuilder builder) throws SQLException, BusinessException{
		PromotionTrigger trigger = builder.build();
		String sql;
		sql = " UPDATE " + Params.dbName + ".promotion_trigger SET " +
			  (builder.isTypeChanged() ? " ,type = " + trigger.getType().getVal() : "") +
			  (builder.isIssueRuleChanged() ? " ,rule = " + trigger.getIssueRule().getVal() : "") +
			  (builder.isUseRuleChanged() ? " ,rule = " + trigger.getUseRule().getVal() : "") +
			  (builder.isExtraChanged() ? " ,extra = " + trigger.getExtra() : "") +
			  " WHERE id = " + trigger.getId(); 
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException("");
		}
	}
	
	/**
	 * Get the promotion triggers according to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the promotion triggers to this extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PromotionTrigger> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the promotion triggers according to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the promotion triggers to this extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PromotionTrigger> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".promotion_trigger PT " + 
		      " WHERE 1 = 1 " +
			  " AND restaurant_id = " +  (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) +
			  (extraCond != null ? extraCond.toString() : "");
		
		final List<PromotionTrigger> result = new ArrayList<>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			PromotionTrigger trigger = new PromotionTrigger(dbCon.rs.getInt("id"));
			trigger.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			trigger.setPromotionId(dbCon.rs.getInt("promotion_id"));
			trigger.setType(PromotionTrigger.Type.valueOf(dbCon.rs.getInt("type")));
			if(trigger.getType().isIssue()){
				trigger.setIssueRule(PromotionTrigger.IssueRule.valueOf(dbCon.rs.getInt("rule")));
			}else if(trigger.getType().isUse()){
				trigger.setUseRule(PromotionTrigger.UseRule.valueOf(dbCon.rs.getInt("rule")));
			}
			trigger.setExtra(dbCon.rs.getInt("extra"));
			result.add(trigger);
		}
		dbCon.rs.close();
		return result;
	}
	
	/**
	 * Delete the promotion trigger to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount to deleted 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(PromotionTrigger trigger : getByCond(dbCon, staff, extraCond)){
			String sql = " DELETE FROM " + Params.dbName + ".promotion_trigger WHERE id = " + trigger.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		return amount;
	}
}
