package com.wireless.db.menuMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PricePlanError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;

public class PricePlanDao {

	public static class ExtraCond{
		private int id;
		private MemberType memberType;
		private Role role;
		private String roleCond;
		private int orderId;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setMemberType(MemberType memberType){
			this.memberType = memberType;
			return this;
		}
		
		public ExtraCond setRole(Role role){
			this.role = role;
			return this;
		}
		
		public ExtraCond setOrder(Order order){
			this.orderId = order.getId();
			return this;
		}
		
		public ExtraCond setOrder(int orderId){
			this.orderId = orderId;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder(); 
			if(id != 0){
				extraCond.append(" AND PP.price_plan_id = " + id);
			}
			if(memberType != null){
				String sql;
				sql = " SELECT price_plan_id FROM " + Params.dbName + ".member_type_price WHERE member_type_id = " + memberType.getId();
				extraCond.append(" AND PP.price_plan_id IN (" + sql + ")");
			}
			if(role != null){
				extraCond.append(" AND price_plan_id IN(" + roleCond + ")");
			}
			if(orderId != 0){
				String sql = " SELECT price_plan_id FROM " + Params.dbName + ".order WHERE id = " + orderId;
				extraCond.append(" AND price_plan_id IN (" + sql + ")");
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert the price plan according to builder {@link PricePlan#InsertBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the price plan builder
	 * @return the id to price plan just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, PricePlan.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the price plan according to builder {@link PricePlan#InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the price plan builder
	 * @return the id to price plan just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, PricePlan.InsertBuilder builder) throws SQLException{
		PricePlan pp = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".price_plan " +
			  " (restaurant_id, type, name) VALUES ( " +
			  staff.getRestaurantId() + "," +
			  pp.getType().getVal() + "," +
			  "'" + pp.getName() + "'" +
			  ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		//Get the generated id to this new table. 
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of new price plan is not generated successfully.");
		}
	}
	
	/**
	 * Update the price plan according to specific builder {@link PricePlan#UpdateBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the price plan to update does NOT exist
	 */
	public static void update(Staff staff, PricePlan.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the price plan according to specific builder {@link PricePlan#UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the price plan to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, PricePlan.UpdateBuilder builder) throws SQLException, BusinessException{
		PricePlan pricePlan = builder.build();
		String sql;
		sql = " UPDATE " + Params.dbName + ".price_plan SET " +
			  " price_plan_id = " + pricePlan.getId() +
			  (builder.isNameChanged() ? ",name = '" + pricePlan.getName() + "'" : "") +
			  (builder.isTypeChanged() ? " ,type = " + pricePlan.getType().getVal() : "") +
			  " WHERE price_plan_id = " + pricePlan.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PricePlanError.PRICE_PLAN_NOT_EXIST);
		}
	}
	
	/**
	 * Get the price plan to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the price plan id 
	 * @return the price plan
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the price plan to this specific id does NOT exist
	 */
	public static PricePlan getById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the price plan to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the price plan id 
	 * @return the price plan
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the price plan to this specific id does NOT exist
	 */
	public static PricePlan getById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		List<PricePlan> result = getByCond(dbCon, staff, new ExtraCond().setId(id));
		if(result.isEmpty()){
			throw new BusinessException(PricePlanError.PRICE_PLAN_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the price plan to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result to price plan
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PricePlan> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the price plan to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result to price plan
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PricePlan> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		
		if(extraCond != null && extraCond.role != null){
			sql = "SELECT * FROM " + Params.dbName + ".role_price_plan WHERE role_id = " + extraCond.role.getId() + " LIMIT 1 ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				extraCond.roleCond = " SELECT price_plan_id FROM " + Params.dbName + ".role_price_plan WHERE role_id = " + extraCond.role.getId();
			}else{
				extraCond.roleCond = " SELECT price_plan_id FROM " + Params.dbName + ".price_plan WHERE restaurant_id = " + staff.getRestaurantId();
			}
			dbCon.rs.close();
		}
		
		sql = " SELECT * FROM " + Params.dbName + ".price_plan PP " +
		      " WHERE 1 = 1 " +
			  " AND PP.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<PricePlan> result = new ArrayList<>();
		
		while(dbCon.rs.next()){
			PricePlan pp = new PricePlan(dbCon.rs.getInt("price_plan_id"));
			pp.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			pp.setName(dbCon.rs.getString("name"));
			pp.setType(PricePlan.Type.valueOf(dbCon.rs.getInt("type")));
			result.add(pp);
		}
		
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Delete the price plan to specific id
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the price plan id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the price plan to delete does NOT exist
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
	 * Delete the price plan to specific id
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the price plan id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the price plan to delete does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(id)) == 0){
			throw new BusinessException(PricePlanError.PRICE_PLAN_NOT_EXIST);
		}
	}
	
	private static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		int amount = 0;
		for(PricePlan plan : getByCond(dbCon, staff, extraCond)){
			String sql;
			//Check to see whether the price plan to delete is used by member chain.
			sql = " SELECT COUNT(*) FROM " + Params.dbName + ".member_chain_price WHERE price_plan_id = " + plan.getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				if(dbCon.rs.getInt(1) > 0){
					throw new BusinessException("会员连锁正在使用此价格方案，不能删除", PricePlanError.DELETE_NOT_ALLOW);
				}
			}
			dbCon.rs.close();
			//Delete the price plan.
			sql = " DELETE FROM " + Params.dbName + ".price_plan WHERE price_plan_id = " + plan.getId();
			dbCon.stmt.executeUpdate(sql);
			//Delete the associated food price to this plan.
			sql = " DELETE FROM " + Params.dbName + ".food_price_plan WHERE price_plan_id = " + plan.getId();
			dbCon.stmt.executeUpdate(sql);
			//Delete the associated role price plan to this plan.
			sql = " DELETE FROM " + Params.dbName + ".role_price_plan WHERE price_plan_id = " + plan.getId();
			dbCon.stmt.executeUpdate(sql);
			//Delete the associated memberType price plan to this plan.
			sql = " DELETE FROM " + Params.dbName + ".member_type_price WHERE price_plan_id = " + plan.getId();
			dbCon.stmt.executeUpdate(sql);			
			
			amount++;
		}
		return amount;
	}
	
}
