package com.wireless.db.staffMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.RestaurantError;
import com.wireless.exception.StaffError;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.staffMgr.Staff.StaffInsertBuilder;
import com.wireless.pojo.staffMgr.Staff.StaffUpdateBuilder;

public class StaffDao {

	/**
	 * Verify a staff to check stuff below.
	 * 1 - check if the attached restaurant is expired
	 * 2 - check if the staff owns the specific privilege   
	 * @param staffId
	 * 			the staff id to verify
	 * @param code
	 * 			the privilege code
	 * @return the staff to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the staff to this id does NOT exist
	 * 		    throws if the restaurant attached with this staff has been expired
	 */
	public static Staff verify(int staffId, Privilege.Code code) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return verify(dbCon, staffId, code);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Verify a staff to check stuff below.
	 * 1 - check if the attached restaurant is expired
	 * 2 - check if the staff owns the specific privilege   
	 * @param dbCon
	 * 			database connection
	 * @param staffId
	 * 			the staff id to verify
	 * @param code
	 * 			the privilege code
	 * @return the staff to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the staff to this id does NOT exist
	 * 		    throws if the restaurant attached with this staff has been expired
	 * 			throws if the privilege code is NOT allowed
	 */
	public static Staff verify(DBCon dbCon, int staffId, Privilege.Code code) throws SQLException, BusinessException{
		
		Staff staff = verify(dbCon, staffId);
		
		if(staff.getRole().hasPrivilege(code)){
			return staff;
		}else{
			throw new BusinessException(StaffError.PERMISSION_NOT_ALLOW);
		}

	}
	
	/**
	 * Verify a staff to check if the attached restaurant is expired
	 * @param staffId
	 * 			the staff id to verify
	 * @return the staff to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the staff to this id does NOT exist
	 * 		    throws if the restaurant attached with this staff has been expired
	 */
	public static Staff verify(int staffId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return verify(dbCon, staffId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Verify a staff to check if the attached restaurant is expired
	 * @param dbCon
	 * 			database connection
	 * @param staffId
	 * 			the staff id to verify
	 * @return the staff to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the staff to this id does NOT exist
	 * 		    throws if the restaurant attached with this staff has been expired
	 */
	public static Staff verify(DBCon dbCon, int staffId) throws SQLException, BusinessException{
		String sql;
		sql = " SELECT expire_date " +
		      " FROM " + Params.dbName + ".restaurant REST " +
			  " JOIN " + Params.dbName + ".staff STAFF ON " + 
		      " REST.id = STAFF.restaurant_id " +
			  " WHERE STAFF.staff_id = " + staffId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		if(dbCon.rs.next()){
			if(dbCon.rs.getTimestamp("expire_date") != null){
				if(dbCon.rs.getTimestamp("expire_date").getTime() < System.currentTimeMillis()){
					throw new BusinessException(RestaurantError.RESTAURANT_EXPIRED);
				}
			}
		}else{
			throw new BusinessException(StaffError.STAFF_NOT_EXIST);
		}
		
		dbCon.rs.close();

		return getStaffById(dbCon, staffId);

	}
	
	/**
	 * Get the staff by specific id.
	 * @param staffId
	 * 			the staff id 
	 * @return the staff to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the staff to find does NOT exist
	 */
	public static Staff getStaffById(int staffId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStaffById(dbCon, staffId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the staff by specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staffId
	 * 			the staff id 
	 * @return the staff to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the staff to find does NOT exist
	 */
	public static Staff getStaffById(DBCon dbCon, int staffId) throws SQLException, BusinessException{
		List<Staff> result = getStaffs(dbCon, " AND STAFF.staff_id = " + staffId, null);
		if(result.isEmpty()){
			throw new BusinessException(StaffError.STAFF_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the staffs to a specific restaurant.
	 * @param extraCond
	 * 			the extra condition
	 * @return the staffs to this restaurant
	 * @throws SQLException
	 * 			throws if failed execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the role to any staff does NOT exist
	 */
	public static List<Staff> getStaffs(String extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStaffs(dbCon, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the staffs to a specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param restaurantId
	 * 			the restaurant id
	 * @return the staffs to this restaurant
	 * @throws SQLException
	 * 			throws if failed execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the role to any staff does NOT exist
	 */
	public static List<Staff> getStaffs(DBCon dbCon, String extraCond) throws SQLException, BusinessException{
		return getStaffs(dbCon, extraCond, null);
	}

	/**
	 * Get the staff to specific role category and restaurant
	 * @param dbCon
	 * 			the database connection
	 * @param restaurantId
	 * 			the restaurant id
	 * @param cate
	 * 			the role category
	 * @return the staff to a specific role
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the role to any staff does NOT exist
	 */
	public static List<Staff> getStaffsByRoleCategory(DBCon dbCon, int restaurantId, Role.Category cate) throws SQLException, BusinessException{
		return getStaffs(dbCon, " AND STAFF.restaurant_id = " + restaurantId + " AND ROLE.cate = " + cate.getVal(), null);
	}
	
	private static List<Staff> getStaffs(DBCon dbCon, String extraCond, String orderClause) throws SQLException, BusinessException{
		
		String sql = " SELECT "	+
					 " STAFF.staff_id, STAFF.restaurant_id, STAFF.name, STAFF.role_id, STAFF.tele, STAFF.pwd, STAFF.type AS staff_type" +
					 " FROM " + Params.dbName + ".staff STAFF " + " " +
					 " LEFT JOIN " + Params.dbName + ".role ROLE " +
					 " ON STAFF.role_id = ROLE.role_id " +
					 " WHERE 1=1 " +
					 (extraCond != null ? extraCond : " ") +
					 (orderClause != null ? orderClause : " ORDER BY ROLE.cate, STAFF.staff_id ");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<Staff> result = new ArrayList<Staff>();
		while(dbCon.rs.next()){
			Staff staff = new Staff();
			
			staff.setId(dbCon.rs.getInt("staff_id"));
			staff.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			staff.setName(dbCon.rs.getString("name"));
			staff.setRole(new Role(dbCon.rs.getInt("role_id")));
			staff.setMobile(dbCon.rs.getString("tele"));
			staff.setPwd(dbCon.rs.getString("pwd"));
			staff.setType(Staff.Type.valueOf(dbCon.rs.getInt("staff_type")));
			
			result.add(staff);
		}
		dbCon.rs.close();
		
		//Get the associated role to each staff
		for(Staff staff : result){
			staff.setRole(RoleDao.getRoleById(dbCon, staff, staff.getRole().getId()));
		}
		
		return Collections.unmodifiableList(result);
	}
	
	/**
	 * Insert a new staff.
	 * @param builder
	 * 			the information of staff
	 * @return	the id of staff just create
	 * @throws SQLException
	 */
	public static int insertStaff(StaffInsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insertStaff(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Insert a new staff.
	 * @param dbCon
	 * @param builder
	 * @return
	 * @throws SQLException
	 */
	public static int insertStaff(DBCon dbCon, StaffInsertBuilder builder) throws SQLException{
		String sql = "INSERT INTO " + Params.dbName + ".staff(restaurant_id, role_id, name, tele, pwd, type) VALUES(" +
					builder.getRestaurantId() + ", " +
					builder.getRole().getId() + ", " +
					"'" + builder.getName() + "', " +
					"'" + builder.getMobile() + "', " +
					"MD5('" + builder.getPwd() + "'), " +
					builder.getType().getVal() + ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int staffId;
		if(dbCon.rs.next()){
			staffId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("failed to insert staff");
		}
					
		return staffId;
	}
	
	/**
	 * Update the information of staff.
	 * @param dbCon
	 * @param staff
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the staff to update does NOT exist
	 */
	public static void updateStaff(StaffUpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			updateStaff(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the information of staff.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the staff to update does NOT exist
	 */
	public static void updateStaff(DBCon dbCon, StaffUpdateBuilder builder) throws SQLException, BusinessException{
		String sql;
		sql = " UPDATE " + Params.dbName + ".staff SET " + 
			  " staff_id = " + builder.getStaffId() +
			  (builder.getStaffName() != null ? " ,name = '" + builder.getStaffName() + "'" : "") +
			  (builder.getMobile() != null ? " ,tele = '" + builder.getMobile() + "'" : "") +
			  (builder.getStaffPwd() != null ? " ,pwd = MD5('" + builder.getStaffPwd() + "')" : "") +
			  (builder.getRoleId() != 0 ? " ,role_id = " + builder.getRoleId() : "") +
			  " WHERE staff_id = " + builder.getStaffId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StaffError.STAFF_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the staff by ID.
	 * @param staffId
	 * 			the id of staff
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static void deleteStaff(int staffId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteStaff(dbCon, staffId);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Delete the staff by ID.
	 * @param dbCon
	 * @param staffId
	 * @throws SQLException
	 */
	public static void deleteStaff(DBCon dbCon, int staffId) throws SQLException{
		String sql = "DELETE FROM " + Params.dbName + ".staff" +
					" WHERE staff_id = " + staffId;
		
		dbCon.stmt.executeUpdate(sql);
	}
	
	

}
