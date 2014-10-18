package com.wireless.db.menuMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PricePlanError;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.staffMgr.Staff;

public class PricePlanDao {

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
				extraCond.append(" AND PP.price_plan_id = " + id);
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
	 */
	public static void deleteById(Staff staff, int id) throws SQLException{
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
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int id) throws SQLException{
		delete(dbCon, staff, new ExtraCond().setId(id));
	}
	
	private static int delete(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(PricePlan plan : getByCond(dbCon, staff, extraCond)){
			String sql;
			//Delete the price plan.
			sql = " DELETE FROM " + Params.dbName + ".price_plan WHERE price_plan_id = " + plan.getId();
			dbCon.stmt.executeUpdate(sql);
			//Delete the associated food price to this plan.
			sql = " DELETE FROM " + Params.dbName + ".food_price_plan WHERE price_plan_id = " + plan.getId();
			dbCon.stmt.executeUpdate(sql);
			amount++;
		}
		return amount;
	}
	
}