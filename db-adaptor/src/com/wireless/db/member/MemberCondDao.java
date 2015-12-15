package com.wireless.db.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.member.MemberCond;
import com.wireless.pojo.member.MemberCond.RangeType;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.staffMgr.Staff;

public class MemberCondDao {

	public static class ExtraCond{
		private int id;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND id = " + id);
			}
			return extraCond.toString();
		}
	}

	/**
	 * Insert the member condition to builder {@link MemberCond.InsertBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the member condition insert builder
	 * @return the id to member condition just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, MemberCond.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the member condition to builder {@link MemberCond.InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the member condition insert builder
	 * @return the id to member condition just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, MemberCond.InsertBuilder builder) throws SQLException{
		MemberCond cond = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".member_cond" +
			  " ( restaurant_id, name, member_type_id, range_type, begin_date, end_date, min_consume_money, max_consume_money, min_consume_amount, max_consume_amount, min_balance, max_balance, min_last_consumption, max_last_consumption ) VALUES ( " +
			  staff.getRestaurantId() + "," +
			  "'" + cond.getName() + "'" + "," +
			  (cond.hasMemberType() ? cond.getMemberType().getId() : " NULL ") + "," +
			  cond.getRangeType().getVal() + "," +
			  (cond.hasRange() ? "'" + cond.getRange().getOnDutyFormat() + "'" : " NULL ") + "," +
			  (cond.hasRange() ? "'" + cond.getRange().getOffDutyFormat() + "'" : " NULL ") + "," +
			  cond.getMinConsumeMoney() + "," +
			  cond.getMaxConsumeMoney() + "," +
			  cond.getMinConsumeAmount() + "," +
			  cond.getMaxConsumeAmount() + "," +
			  cond.getMinBalance() + "," +
			  cond.getMaxBalance() + "," +
			  cond.getMinLastConsumption() + "," +
			  cond.getMaxLastConsumption() +
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int id = 0;
		if(dbCon.rs.next()){
			id = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of member condition is not generated successfully.");
		}
		return id;
	}
	
	/**
	 * Update the member condition according specific builder {@link MemberCond.UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder to member condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member condition to update does NOT exist
	 */
	public static void update(Staff staff, MemberCond.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the member condition according specific builder {@link MemberCond.UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder to member condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member condition to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, MemberCond.UpdateBuilder builder) throws SQLException, BusinessException{
		MemberCond cond = builder.build();
		String sql;
		sql = " UPDATE " + Params.dbName + ".member_cond SET " +
			  " id = " + cond.getId() +
			  (builder.isNameChanged() ? " ,name = '" + cond.getName() + "'" : "") +
			  (builder.isMemberTypeChanged() ? " ,member_type_id = " + (cond.hasMemberType() ? cond.getMemberType().getId() : "NULL") : "") +
			  (builder.isBalanceChanged() ? " ,min_balance = " + cond.getMinBalance() + " ,max_balance = " + cond.getMaxBalance() : "") +
			  (builder.isConsumeAmountChanged() ? " ,min_consume_amount = " + cond.getMinConsumeAmount() + " ,max_consume_amount = " + cond.getMaxConsumeAmount() : "") +
			  (builder.isConsumeMoneyChanged() ? " ,min_consume_money = " + cond.getMinConsumeMoney() + " ,max_consume_money = " + cond.getMaxConsumeMoney() : "") +
			  (builder.isRangeTypeChanged() ? " ,range_type = " + cond.getRangeType().getVal() : "") +
			  (builder.isRangeChanged() ? " ,begin_date = '" + cond.getRange().getOnDutyFormat() + "' ,end_date = '" + cond.getRange().getOffDutyFormat() + "'" : "") +
			  (builder.isLastConsumptionChanged() ? " ,min_last_consumption = " + cond.getMinLastConsumption()  + ",max_last_consumption = " + cond.getMaxLastConsumption() : "") +
			  " WHERE id = " + cond.getId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(MemberError.MEMBER_CONDITION_NOT_EXIST);
		}
	}
	
	/**
	 * Get the member condition to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to member condition
	 * @return the member condition to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member to this id does NOT exist
	 */
	public static MemberCond getById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member condition to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to member condition
	 * @return the member condition to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member to this id does NOT exist
	 */
	public static MemberCond getById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		List<MemberCond> result = getByCond(dbCon, staff, new ExtraCond().setId(id));
		if(result.isEmpty()){
			throw new BusinessException(MemberError.MEMBER_CONDITION_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the member condition to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * @param extraCond
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberCond> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member condition to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the member condition matched the extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<MemberCond> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".member_cond " +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		
		final List<MemberCond> result = new ArrayList<MemberCond>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			MemberCond memberCond = new MemberCond(dbCon.rs.getInt("id"));
			memberCond.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			memberCond.setName(dbCon.rs.getString("name"));
			if(dbCon.rs.getInt("member_type_id") != 0){
				memberCond.setMemberType(new MemberType(dbCon.rs.getInt("member_type_id")));
			}
			
			memberCond.setRangeType(RangeType.valueOf(dbCon.rs.getInt("range_type")));
			
			if(memberCond.getRangeType() == MemberCond.RangeType.LAST_1_MONTH){
				//近1月
				Calendar c = Calendar.getInstance();
				c.add(Calendar.MONTH, -1);
				memberCond.setRange(new DutyRange(c.getTime().getTime(), System.currentTimeMillis()));
				
			}if(memberCond.getRangeType() == MemberCond.RangeType.LAST_2_MONTHS){
				//近2月
				Calendar c = Calendar.getInstance();
				c.add(Calendar.MONTH, -2);
				memberCond.setRange(new DutyRange(c.getTime().getTime(), System.currentTimeMillis()));
				
			}if(memberCond.getRangeType() == MemberCond.RangeType.LAST_3_MONTHS){
				//近3月
				Calendar c = Calendar.getInstance();
				c.add(Calendar.MONTH, -3);
				memberCond.setRange(new DutyRange(c.getTime().getTime(), System.currentTimeMillis()));
				
			}else if(memberCond.getRangeType() == MemberCond.RangeType.USER_DEFINE){
				if(dbCon.rs.getTimestamp("begin_date") != null && dbCon.rs.getTimestamp("end_date") != null){
					memberCond.setRange(new DutyRange(dbCon.rs.getTimestamp("begin_date").getTime(), dbCon.rs.getTimestamp("end_date").getTime()));
				}
			}
			
			memberCond.setMinBalance(dbCon.rs.getFloat("min_balance"));
			memberCond.setMaxBalance(dbCon.rs.getFloat("max_balance"));
			memberCond.setMinConsumeAmount(dbCon.rs.getInt("min_consume_amount"));
			memberCond.setMaxConsumeAmount(dbCon.rs.getInt("max_consume_amount"));
			memberCond.setMinConsumeMoney(dbCon.rs.getFloat("min_consume_money"));
			memberCond.setMaxConsumeMoney(dbCon.rs.getFloat("max_consume_money"));
			memberCond.setMinLastConsumption(dbCon.rs.getInt("min_last_consumption"));
			memberCond.setMaxLastConsumption(dbCon.rs.getInt("max_last_consumption"));
			result.add(memberCond);
		}
		dbCon.rs.close();
		return result;
	}
	
	/**
	 * Delete the member condition to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member condition to this id does NOT exist
	 */
	public static void deleteById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the member condition to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member condition to this id does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(id)) == 0){
			throw new BusinessException(MemberError.MEMBER_CONDITION_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the member condition according to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount to member condition deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(MemberCond cond : getByCond(dbCon, staff, extraCond)){
			String sql = " DELETE FROM " + Params.dbName + ".member_cond WHERE id = " + cond.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		return amount;
	}
}
