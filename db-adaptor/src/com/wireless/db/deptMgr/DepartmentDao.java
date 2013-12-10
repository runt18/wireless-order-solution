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
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the department result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	
	/**
	 * Get the department to a specified restaurant defined in {@link Staff} and other extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the department result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static List<Department> getDepartments(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException{
		
		List<Department> result = new ArrayList<Department>();
		
		String sql = " SELECT dept_id, name, restaurant_id, type FROM " + Params.dbName + ".department DEPT " +
					 " WHERE 1 = 1 AND DEPT.dept_id <> " + Department.DeptId.DEPT_TMP.getVal() + " AND DEPT.dept_id <> " + Department.DeptId.DEPT_NULL.getVal() +
					 " AND DEPT.restaurant_id = " + term.getRestaurantId() +
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
	
	
	public static List<Department> getDepartmentsForDept(DBCon dbCon, Staff term) throws SQLException{
		return getDepartments(dbCon, term, " AND DEPT.dept_id <> " + Department.DeptId.DEPT_WAREHOUSE.getVal(), null);
	}
	
	public static List<Department> getDepartmentsForDept(Staff term) throws SQLException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			return getDepartmentsForDept(dbCon, term);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<Department> getDepartmentsForWarehouse(DBCon dbCon, Staff term) throws SQLException{
		return getDepartments(dbCon, term, null, null);
	}
	
	public static List<Department> getDepartmentsForWarehouse(Staff term) throws SQLException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			return getDepartmentsForWarehouse(dbCon, term);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the department to a specified restaurant according to id.
	 * @param term
	 * 			the terminal
	 * @param deptId
	 * 			the department id to find
	 * @return the department to specified restaurant and id
	 * @throws BusinessException
	 * 			throws if the specified department does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Department getDepartmentById(Staff term, int deptId) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getDepartmentById(dbCon, term, deptId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the department to a specified restaurant according to id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param deptId
	 * 			the department id to find
	 * @return the department to specified restaurant and id
	 * @throws BusinessException
	 * 			throws if the specified department does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Department getDepartmentById(DBCon dbCon, Staff term, int deptId) throws BusinessException, SQLException{
		List<Department> result = getDepartments(dbCon, term, " AND DEPT.dept_id = " + deptId, null);
		if(result.isEmpty()){
			throw new BusinessException("The department(id = " + deptId + ",restaurant_id = " + term.getRestaurantId() + ") does NOT exist.");
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
