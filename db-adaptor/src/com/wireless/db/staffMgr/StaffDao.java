package com.wireless.db.staffMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.RestaurantError;
import com.wireless.exception.StaffError;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;

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
	 * @param restaurantId
	 * 			the restaurant id
	 * @return the staffs to this restaurant
	 * @throws SQLException
	 * 			throws if failed execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the role to any staff does NOT exist
	 */
	public static List<Staff> getStaffs(int restaurantId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStaffs(dbCon, restaurantId);
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
	public static List<Staff> getStaffs(DBCon dbCon, int restaurantId) throws SQLException, BusinessException{
		return getStaffs(dbCon, " AND STAFF.restaurant_id = " + restaurantId, null);
	}

	private static List<Staff> getStaffs(DBCon dbCon, String extraCond, String orderClause) throws SQLException, BusinessException{
		
		String sql = " SELECT "	+
					 " STAFF.staff_id, STAFF.restaurant_id, STAFF.name, STAFF.role_id, STAFF.tele, STAFF.pwd, STAFF.type AS staff_type" +
					 " FROM " + Params.dbName + ".staff STAFF " + " " +
					 " WHERE 1 = 1 " +
					 (extraCond != null ? extraCond : " ") +
					 (orderClause != null ? orderClause : " ORDER BY STAFF.staff_id ");
		
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
	
	
	

}
