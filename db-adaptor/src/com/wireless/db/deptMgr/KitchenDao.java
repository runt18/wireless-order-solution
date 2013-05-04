package com.wireless.db.deptMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.DeptError;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.protocol.Terminal;

public class KitchenDao {
	
	/**
	 * Get the kitchens to a specified restaurant defined in {@link Terminal} and other extra condition.
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the kitchen result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Kitchen> getKitchens(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getKitchens(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the kitchens to a specified restaurant defined in {@link Terminal} and other extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the kitchen result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Kitchen> getKitchens(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		List<Kitchen> kitchens = new ArrayList<Kitchen>();
		String sql = " SELECT " +
					 " KITCHEN.restaurant_id, KITCHEN.kitchen_id, KITCHEN.kitchen_alias, " +
					 " KITCHEN.name AS kitchen_name, KITCHEN.type AS kitchen_type, KITCHEN.is_allow_temp AS is_allow_temp, " +
					 " DEPT.dept_id, DEPT.name AS dept_name, DEPT.type AS dept_type FROM " + 
			  		 Params.dbName + ".kitchen KITCHEN " +
					 " JOIN " +
					 Params.dbName + ".department DEPT " +
					 " ON KITCHEN.dept_id = DEPT.dept_id AND KITCHEN.restaurant_id = DEPT.restaurant_id " +
			  		 " WHERE 1=1 " +
					 " AND KITCHEN.restaurant_id = " + term.restaurantID +
			  		 (extraCond == null ? "" : extraCond) + " " +
			  		 (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			kitchens.add(new Kitchen.Builder(dbCon.rs.getShort("kitchen_alias"), dbCon.rs.getString("kitchen_name"), dbCon.rs.getInt("restaurant_id"))
								.setAllowTemp(dbCon.rs.getBoolean("is_allow_temp"))
								.setKitchenId(dbCon.rs.getLong("kitchen_id"))
								.setType(dbCon.rs.getShort("kitchen_type"))
								.setDept(new Department(dbCon.rs.getString("dept_name"), 
											 		dbCon.rs.getShort("dept_id"), 
											 		dbCon.rs.getInt("restaurant_id"),
											 		Department.Type.valueOf(dbCon.rs.getShort("dept_type")))).build());
		}
		dbCon.rs.close();
		
		return kitchens;
		
	}
	
	/**
	 * Get the kitchen to a specified restaurant defined in {@link Terminal} and kitchen alias
	 * @param term
	 * 			the terminal
	 * @param kitchenAlias
	 * 			the kitchen alas
	 * @return the kitchen to a specified restaurant and kitchen alias
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the kitchen does NOT exist
	 */
	public static Kitchen getKitchenByAlias(Terminal term, int kitchenAlias) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getKitchenByAlias(dbCon, term, kitchenAlias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the kitchen to a specified restaurant defined in {@link Terminal} and kitchen alias
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param kitchenAlias
	 * 			the kitchen alas
	 * @return the kitchen to a specified restaurant and kitchen alias
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the kitchen does NOT exist
	 */
	public static Kitchen getKitchenByAlias(DBCon dbCon, Terminal term, int kitchenAlias) throws SQLException, BusinessException{
		List<Kitchen> result = getKitchens(dbCon, term,
										   " AND KITCHEN.kitchen_alias = " + kitchenAlias,
										   null);
		if(result.isEmpty()){
			throw new BusinessException("The kitchen(alias_id = " + kitchenAlias + ", restaurant_id = " + term.restaurantID + ") does NOT exist.", DeptError.KITCHEN_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	public static void update(DBCon dbCon, Terminal term, Kitchen kitchenToUpdate) throws SQLException, BusinessException{
		//TODO
	}
	
}
