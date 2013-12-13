package com.wireless.db.deptMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;

public class DepartmentDao {

	/**
	 * Get the department to a specified restaurant defined in {@link Staff} and other extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the department result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static List<Department> getDepartments(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		
		List<Department> result = new ArrayList<Department>();
		
		String sql = " SELECT dept_id, name, restaurant_id, type FROM " + Params.dbName + ".department DEPT " +
					 " WHERE 1 = 1 " +
					 " AND DEPT.restaurant_id = " + staff.getRestaurantId() +
					 (extraCond != null ? extraCond : "") + " " +
					 (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			result.add(new Department(dbCon.rs.getString("name"),
									  dbCon.rs.getShort("dept_id"),
									  dbCon.rs.getInt("restaurant_id"),
									  Department.Type.valueOf(dbCon.rs.getShort("type"))));
			
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Get the normal departments to a specified restaurant defined in {@link Staff}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the normal departments to this restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Department> getNormalDepartments(DBCon dbCon, Staff staff) throws SQLException{
		return getDepartments(dbCon, staff, " AND DEPT.type = " + Department.Type.NORMAL.getVal() , null);
	}
	
	/**
	 * Get the normal departments to a specified restaurant defined in {@link Staff}.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the normal departments to this restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Department> getNormalDepartments(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			return getNormalDepartments(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the departments for inventory.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the departments to inventory
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the department to warehouse does NOT exist
	 */
	public static List<Department> getDepartments4Inventory(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		List<Department> result = getNormalDepartments(dbCon, staff);
		result.add(getDepartmentById(dbCon, staff, Department.DeptId.DEPT_WAREHOUSE.getVal()));
		return result;
	}
	
	/**
	 * Get the departments for inventory.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the departments to inventory
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the department to warehouse does NOT exist
	 */
	public static List<Department> getDepartments4Inventory(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			return getDepartments4Inventory(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the department to a specified restaurant according to id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param deptId
	 * 			the department id to find
	 * @return the department to specified restaurant and id
	 * @throws BusinessException
	 * 			throws if the specified department does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Department getDepartmentById(Staff staff, int deptId) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getDepartmentById(dbCon, staff, deptId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the department to a specified restaurant according to id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param deptId
	 * 			the department id to find
	 * @return the department to specified restaurant and id
	 * @throws BusinessException
	 * 			throws if the specified department does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Department getDepartmentById(DBCon dbCon, Staff staff, int deptId) throws BusinessException, SQLException{
		List<Department> result = getDepartments(dbCon, staff, " AND DEPT.dept_id = " + deptId, null);
		if(result.isEmpty()){
			throw new BusinessException("The department(id = " + deptId + ",restaurant_id = " + staff.getRestaurantId() + ") does NOT exist.");
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Insert a new department.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the builder to insert a new department
	 * @return the id to department just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static short insert(DBCon dbCon, Department.InsertBuilder builder) throws SQLException{
		Department deptToInsert = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".department " +
		      " (dept_id, restaurant_id, name, type) " +
			  " VALUES(" +
		      deptToInsert.getId() + "," +
		      deptToInsert.getRestaurantId() + "," +
			  "'" + deptToInsert.getName() + "'," +
		      deptToInsert.getType().getVal() +
		      ")";
		
		dbCon.stmt.executeUpdate(sql);
		
		return deptToInsert.getId();
	}
}
