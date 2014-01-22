package com.wireless.db.deptMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.DeptError;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.menuMgr.Kitchen.Type;
import com.wireless.pojo.staffMgr.Staff;

public class KitchenDao {
	
	/**
	 * Swap the display id between two kitchens.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the swap display builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the kitchen to swap does NOT exist
	 */
	public static void swap(Staff staff, Kitchen.SwapDisplayBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			swap(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Swap the display id between two kitchens.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the swap display builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the kitchen to swap does NOT exist
	 */
	public static void swap(DBCon dbCon, Staff staff, Kitchen.SwapDisplayBuilder builder) throws SQLException, BusinessException{
		Kitchen kA = getById(dbCon, staff, builder.getIdA());
		Kitchen kB = getById(dbCon, staff, builder.getIdB());
		
		String sql;
		sql = " UPDATE " + Params.dbName + ".kitchen SET " +
			  " display_id = " + kA.getDisplayId() +
			  " WHERE kitchen_id = " + kB.getId();
		dbCon.stmt.executeUpdate(sql);
		
		sql = " UPDATE " + Params.dbName + ".kitchen SET " +
			  " display_id = " + kB.getDisplayId() +
			  " WHERE kitchen_id = " + kA.getId();
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Update a kitchen according to update builder
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the kitchen to update does NOT exist
	 */
	public static void update(Staff staff, Kitchen.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update a kitchen according to update builder
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the kitchen to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, Kitchen.UpdateBuilder builder) throws SQLException, BusinessException{
		Kitchen k = builder.build();
		String sql;
		sql = " UPDATE " + Params.dbName + ".kitchen SET " +
			  " kitchen_id = " + k.getId() +
			  (builder.isNameChanged() ? ",name = '" + k.getName() + "'": "") +
			  (builder.isDeptChanged() ? ",dept_id = " + k.getDept().getId() : "") +
			  (builder.isAllowTmpChanged() ? ",is_allow_temp = " + (k.isAllowTemp() ? 1 : 0) : "") +
			  " WHERE kitchen_id = " + k.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(DeptError.KITCHEN_NOT_EXIST);
		}
	}
	
	/**
	 * Remove a kitchen and put it back to the pool containing unused kitchens.
	 * @param staff
	 * 			the staff to perform this action
	 * @param kitchenId
	 * 			the kitchen id to remove
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the kitchen to delete does NOT exist.
	 */
	public static void remove(Staff staff, int kitchenId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			remove(dbCon, staff, kitchenId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Remove a kitchen and put it back to the pool containing unused kitchens.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param kitchenId
	 * 			the kitchen id to remove
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the kitchen to delete is NOT empty<br>
	 * 			<li>throws if the kitchen to delete does NOT exist
	 */
	public static void remove(DBCon dbCon, Staff staff, int kitchenId) throws SQLException, BusinessException{
		String sql;
		//Check to see whether any food is associated with the kitchen to remove
		sql = " SELECT food_id FROM " + Params.dbName + ".food WHERE kitchen_id = " + kitchenId + " LIMIT 1 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(DeptError.KITCHEN_NOT_EMPTY);
		}
		dbCon.rs.close();
		
		sql = " UPDATE " + Params.dbName + ".kitchen SET " +
			  " kitchen_id = " + kitchenId + 
			  " ,type = " + Kitchen.Type.IDLE.getVal() + 
			  " ,dept_id = " + Department.DeptId.DEPT_NULL.getVal() + 
			  " ,is_allow_temp = 0 " +
			  " WHERE kitchen_id = " + kitchenId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(DeptError.KITCHEN_NOT_EXIST);
		}
	}
	
	/**
	 * Add a kitchen from the pool containing idle kitchens.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to add kitchen
	 * @return the id to kitchen just added
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the unused kitchen has been run out of<br>
	 * 			<li>throws if the kitchen to update does NOT exist
	 */
	public static int add(Staff staff, Kitchen.AddBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return add(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Add a kitchen from the pool containing unused kitchens.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to add kitchen
	 * @return the id to kitchen just added
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the unused kitchen has been run out of<br>
	 * 			<li>throws if the kitchen to update does NOT exist
	 */
	public static int add(DBCon dbCon, Staff staff, Kitchen.AddBuilder builder) throws SQLException, BusinessException{
		String sql;
		//Check to see whether any unused kitchens exist.
		sql = " SELECT kitchen_id FROM " + Params.dbName + ".kitchen " +
			  " WHERE 1 = 1 " +
			  " AND type = " + Type.IDLE.getVal() + 
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  " ORDER BY kitchen_id LIMIT 1 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int kitchenId = 0;
		if(dbCon.rs.next()){
			kitchenId = dbCon.rs.getInt(1);
		}else{
			throw new BusinessException(DeptError.INSUFFICIENT_IDLE_KITCHEN);
		}
		dbCon.rs.close();
		
		Kitchen k = builder.build();
		//Update the kitchen type to normal so as to be ready for user.
		sql = " UPDATE " + Params.dbName + ".kitchen SET " +
			  " kitchen_id = " + kitchenId + 
			  " ,type = " + Kitchen.Type.NORMAL.getVal() +
			  " ,dept_id = " + k.getDept().getId() + 
			  " ,name = '" + k.getName() + "'" +
			  " ,is_allow_temp = " + (k.isAllowTemp() ? 1 : 0) +
			  " WHERE kitchen_id = " + kitchenId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(DeptError.KITCHEN_NOT_EXIST);
		}
		
		return kitchenId;
	}
	
	/**
	 * Get the kitchens to a specific type.
	 * @param staff
	 * 			the staff to perform this action
	 * @param type
	 * 			the kitchen type
	 * @return the kitchens to this specific type
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Kitchen> getByType(Staff staff, Kitchen.Type type) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByType(dbCon, staff, type);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the kitchens to a specific type.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param type
	 * 			the kitchen type
	 * @return the kitchens to this specific type
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Kitchen> getByType(DBCon dbCon, Staff staff, Kitchen.Type type) throws SQLException{
		return getByCond(dbCon, staff, " AND K.type = " + type.getVal(), null);
	}
	
	/**
	 * Get the kitchen according to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param kitchenId
	 * 			the kitchen id to get
	 * @return the kitchen to this specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the kitchen to this id does NOT exist
	 */
	public static Kitchen getById(Staff staff, int kitchenId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, kitchenId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the kitchen according to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param kitchenId
	 * 			the kitchen id to get
	 * @return the kitchen to this specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the kitchen to this id does NOT exist
	 */
	public static Kitchen getById(DBCon dbCon, Staff staff, int kitchenId) throws SQLException, BusinessException{
		List<Kitchen> result = getByCond(dbCon, staff, " AND K.kitchen_id = " + kitchenId, null);
		if(result.isEmpty()){
			throw new BusinessException(DeptError.KITCHEN_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the kitchens to a specified restaurant defined in {@link Staff} and other extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the kitchen result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static List<Kitchen> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		List<Kitchen> result = new ArrayList<Kitchen>();
		String sql;
		sql = " SELECT " +
			  " K.restaurant_id, K.kitchen_id, K.display_id AS kitchen_dispay_id, " +
			  " K.name AS kitchen_name, K.type AS kitchen_type, K.is_allow_temp AS is_allow_temp, " +
			  " D.dept_id, D.name AS dept_name, D.type AS dept_type, D.display_id AS dept_display_id " +
			  " FROM " + Params.dbName + ".kitchen K " +
			  " JOIN " + Params.dbName + ".department D ON K.dept_id = D.dept_id AND K.restaurant_id = D.restaurant_id " +
		  	  " WHERE 1=1 " +
			  " AND K.restaurant_id = " + staff.getRestaurantId() +
		  	  (extraCond == null ? "" : extraCond) + " " +
		  	  (orderClause == null ? " ORDER BY K.display_id " : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Kitchen k = new Kitchen(dbCon.rs.getInt("kitchen_id"));
			k.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			k.setDisplayId(dbCon.rs.getShort("kitchen_dispay_id"));
			k.setName(dbCon.rs.getString("kitchen_name"));
			k.setAllowTemp(dbCon.rs.getBoolean("is_allow_temp"));
			k.setType(dbCon.rs.getShort("kitchen_type"));
			k.setDept(new Department(dbCon.rs.getString("dept_name"), 
			 						 dbCon.rs.getShort("dept_id"), 
			 						 dbCon.rs.getInt("restaurant_id"),
			 						 Department.Type.valueOf(dbCon.rs.getShort("dept_type")),
			 						 dbCon.rs.getInt("dept_display_id")));
			result.add(k);
		}
		dbCon.rs.close();
		return Collections.unmodifiableList(result);
		
	}
	
	/**
	 * Get the kitchens to a specific department.
	 * @param staff
	 * 			the staff to perform this action
	 * @param deptId
	 * 			the id to department
	 * @return the kitchens to specific department
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Kitchen> getByDept(Staff staff, Department.DeptId deptId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByDept(dbCon, staff, deptId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the kitchens to a specific department.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param deptId
	 * 			the id to department
	 * @return the kitchens to specific department
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Kitchen> getByDept(DBCon dbCon, Staff staff, Department.DeptId deptId) throws SQLException{
		return getByCond(dbCon, staff, " AND K.dept_id = " + deptId.getVal(), null);
	}
	
	/**
	 * Insert a new kitchen.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new kitchen
	 * @return the id to kitchen just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, Kitchen.InsertBuilder builder) throws SQLException{
		Kitchen kitchenToAdd = builder.build();
		
		String sql;

		int displayId = 0;

		if(kitchenToAdd.isNull() || kitchenToAdd.isTemp()){
			displayId = 0;
		}else{
			//Calculate the display id if case of normal kitchen.
			sql = " SELECT IFNULL(MAX(display_id), 0) + 1 FROM " + Params.dbName + ".kitchen WHERE restaurant_id = " + staff.getRestaurantId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				displayId = dbCon.rs.getInt(1);
			}
			dbCon.rs.close();
		}
		
		sql = " INSERT INTO " + Params.dbName + ".kitchen" +
		      " (restaurant_id, name, type, dept_id, display_id) " +
			  " VALUES ( " +
		      staff.getRestaurantId() + "," +
		      "'" + kitchenToAdd.getName() + "'," +
		      kitchenToAdd.getType().getVal() + "," +
		      kitchenToAdd.getDept().getId() + "," +
		      displayId +
		      " ) ";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int kitchenId = 0;
		if(dbCon.rs.next()){
			kitchenId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("Failed to generated the kitchen id.");
		}
		
		return kitchenId;
	}
	
	/**
	 * Get the monthSettle kitchens 
	 * @param dbCon
	 * @param staff
	 * @param extraCond
	 * @param otherClause
	 * @return
	 * @throws SQLException
	 */
	public static List<Kitchen> getMonthSettleKitchen(DBCon dbCon, Staff staff, String extraCond, String otherClause) throws SQLException{
		
		String sql = "SELECT K.kitchen_id, K.name FROM " + Params.dbName + ".kitchen K " + 
					" JOIN food F ON K.kitchen_id = F.kitchen_id " +
					" WHERE K.restaurant_id = " + staff.getRestaurantId() + 
					" AND F.stock_status = " + Food.StockStatus.GOOD.getVal() +
					(extraCond == null ? "" : extraCond) + " " +
			  		(otherClause == null ? "" : otherClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<Kitchen> kitchens = new ArrayList<Kitchen>();
		while(dbCon.rs.next()){
			Kitchen k = new Kitchen(dbCon.rs.getInt("kitchen_id"));
			k.setName(dbCon.rs.getString("name"));
			kitchens.add(k);
		}
		return kitchens;
	}
	
	/**
	 * Get the monthSettle kitchens. 
	 * @param staff
	 * @param extraCond
	 * @param otherClause
	 * @return
	 * @throws SQLException
	 */
	public static List<Kitchen> getMonthSettleKitchen(Staff staff, String extraCond, String otherClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getMonthSettleKitchen(dbCon, staff, extraCond, otherClause);
		}finally{
			dbCon.disconnect();
		}
		
	}
	
}
