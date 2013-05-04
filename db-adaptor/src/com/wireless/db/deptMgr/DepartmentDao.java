package com.wireless.db.deptMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.protocol.Terminal;

public class DepartmentDao {

	/**
	 * Get the department to a specified restaurant defined in {@link Terminal} and other extra condition.
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
	public static List<Department> getDepartments(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getDepartments(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the department to a specified restaurant defined in {@link Terminal} and other extra condition.
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
	public static List<Department> getDepartments(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		
		List<Department> result = new ArrayList<Department>();
		
		String sql = " SELECT dept_id, name, restaurant_id, type FROM " + Params.dbName + ".department DEPT " +
					 " WHERE 1 = 1 " +
					 " AND DEPT.restaurant_id = " + term.restaurantID +
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
	
	public static void update(Terminal term, Department deptToUpdate) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, term, deptToUpdate);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static void update(DBCon dbCon, Terminal term, Department deptToUpdate) throws SQLException, BusinessException{
		//TODO
	}
	
}
