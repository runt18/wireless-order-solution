package com.wireless.db.staffMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.RestaurantError;
import com.wireless.exception.StaffError;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Device;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.staffMgr.Staff.InsertBuilder;
import com.wireless.pojo.staffMgr.Staff.UpdateBuilder;

public class StaffDao {

	public static class ExtraCond{
		private int staffId;
		private int roleId;
		private Role.Category category;
		private String name;
		private final List<Privilege.Code> codes = new ArrayList<Privilege.Code>();
		private final List<Staff.Type> types = new ArrayList<Staff.Type>();
		
		private int restaurantId;
		
		public ExtraCond(){
		}
		
		private ExtraCond setRestaurantId(int restaurantId){
			this.restaurantId = restaurantId;
			return this;
		}
		
		public ExtraCond setStaff(int staffId){
			this.staffId = staffId;
			return this;
		}
		
		public ExtraCond setRole(int roleId){
			this.roleId = roleId;
			return this;
		}
		
		public ExtraCond setCategory(Role.Category category){
			this.category = category;
			return this;
		}
		
		public ExtraCond setName(String name){
			this.name = name;
			return this;
		}
		
		public ExtraCond addPrivilegeCode(Privilege.Code code){
			if(!codes.contains(code)){
				codes.add(code);
			}
			return this;
		}
		
		public ExtraCond addType(Staff.Type type){
			this.types.add(type);
			return this;
		}
		
		@Override
		public String toString(){
			final StringBuilder extraCond = new StringBuilder();
			if(restaurantId > 0){
				extraCond.append(" AND STAFF.restaurant_id = " + restaurantId);
			}
			if(staffId > 0){
				extraCond.append(" AND STAFF.staff_id = " + staffId);
			}
			if(roleId > 0){
				extraCond.append(" AND STAFF.role_id = " + roleId);
			}
			if(category != null){
				extraCond.append(" AND ROLE.cate = " + category.getVal());
			}
			if(name != null){
				extraCond.append(" AND STAFF.name like '%" + name + "%'");
			}
			
			final StringBuilder typeCond = new StringBuilder();
			for(Staff.Type type : types){
				if(typeCond.length() != 0){
					typeCond.append(",");
				}
				typeCond.append(type.getVal());
			}
			if(typeCond.length() != 0){
				extraCond.append(" AND.STAFF.type IN (" + typeCond.toString() + ")");
			}
			
			if(!codes.isEmpty()){
				StringBuilder priCode = new StringBuilder();
				for(Privilege.Code code : codes){
					if(priCode.length() > 0){
						priCode.append(",");
					}
					priCode.append(code.getVal());
				}
				String sql;
				sql = " SELECT role_id " + 
					  " FROM " + Params.dbName + ".role_privilege RP " + 
					  " JOIN " + Params.dbName + ".privilege P ON RP.pri_id = P.pri_id " +
					  " WHERE RP.restaurant_id = " + restaurantId +
					  " AND P.pri_code IN ( " + priCode.toString() + ")" +
					  " GROUP BY role_id ";
				extraCond.append(" AND STAFF.role_id IN (" + sql + ")");
			}
			return extraCond.toString();
		}
	}
	
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
		sql = " SELECT expire_date, REST.id AS restaurant_id " +
		      " FROM " + Params.dbName + ".restaurant REST " +
			  " JOIN " + Params.dbName + ".staff STAFF ON REST.id = STAFF.restaurant_id " +
			  " WHERE STAFF.staff_id = " + staffId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		int restaurantId = 0;
		if(dbCon.rs.next()){
			if(dbCon.rs.getTimestamp("expire_date") != null){
				if(dbCon.rs.getTimestamp("expire_date").getTime() < System.currentTimeMillis()){
					throw new BusinessException(RestaurantError.RESTAURANT_EXPIRED);
				}
			}
			restaurantId = dbCon.rs.getInt("restaurant_id");
		}else{
			throw new BusinessException(StaffError.STAFF_NOT_EXIST);
		}
		
		dbCon.rs.close();

		return getByCond(dbCon, restaurantId, new ExtraCond().setStaff(staffId), null).get(0);

	}
	
	/**
	 * Get the admin to specific restaurant
	 * @param restaurantId
	 * 			the restaurant id
	 * @return the admin to this restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the admin to this restaurant does NOT exist
	 */
	public static Staff getAdminByRestaurant(int restaurantId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getAdminByRestaurant(dbCon, restaurantId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the admin to specific restaurant
	 * @param dbCon
	 * 			the database connection
	 * @param restaurantId
	 * 			the restaurant id
	 * @return the admin to this restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the admin to this restaurant does NOT exist
	 */
	public static Staff getAdminByRestaurant(DBCon dbCon, int restaurantId) throws SQLException, BusinessException{
		List<Staff> result = getByCond(dbCon, restaurantId, new ExtraCond().setCategory(Role.Category.ADMIN) , null);
		if(result.isEmpty()){
			throw new BusinessException(StaffError.STAFF_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}

	/**
	 * Get the staff to specific device {@link #Device}.
	 * @param dbCon
	 * 			the database connection
	 * @param device
	 * 			the device 
	 * @return the staff to this device
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 */
	public static List<Staff> getByDevice(Device device) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByDevice(dbCon, device);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the staff to specific device {@link #Device}.
	 * @param dbCon
	 * 			the database connection
	 * @param device
	 * 			the device 
	 * @return the staff to this device
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 */
	public static List<Staff> getByDevice(DBCon dbCon, Device device) throws SQLException, BusinessException{
		return getByCond(dbCon, device.getRestaurantId(), new ExtraCond().addType(Staff.Type.NORMAL).addType(Staff.Type.RESERVED), null);
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
	public static Staff getById(int staffId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staffId);
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
	public static Staff getById(DBCon dbCon, int staffId) throws SQLException, BusinessException{
		List<Staff> result = getByCond(dbCon, 0, new ExtraCond().setStaff(staffId), null);
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
	public static List<Staff> getByRestaurant(int restaurantId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByRestaurant(dbCon, restaurantId);
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
	public static List<Staff> getByRestaurant(DBCon dbCon, int restaurantId) throws SQLException, BusinessException{
		return getByCond(dbCon, restaurantId, null, null);
	}
	
	/**
	 * Get staff which is like specific name.
	 * @param staff
	 * 			the staff to perform this action
	 * @param staffName
	 * 			the staff name to get
	 * @return the result to staff like the name
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any role to the staff does NOT exist
	 */
	public static List<Staff> getByName(Staff staff, String staffName) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByName(dbCon, staff, staffName);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get staff which is like specific name.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param staffName
	 * 			the staff name to get
	 * @return the result to staff like the name
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any role to the staff does NOT exist
	 */
	public static List<Staff> getByName(DBCon dbCon, Staff staff, String staffName) throws SQLException, BusinessException{
		return getByCond(dbCon, staff, new ExtraCond().setName(staffName));
	}
	
	/**
	 * Get the staff to specific role
	 * @param staff
	 * 			the staff to perform this action
	 * @param roleId
	 * 			the id to the role
	 * @return the staffs to specific role
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any role to the staff does NOT exist
	 */
	public static List<Staff> getByRole(Staff staff, int roleId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByRole(dbCon, staff, roleId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the staff to specific role
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param roleId
	 * 			the id to the role
	 * @return the staffs to specific role
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any role to the staff does NOT exist
	 */
	public static List<Staff> getByRole(DBCon dbCon, Staff staff, int roleId) throws SQLException, BusinessException{
		return getByCond(dbCon, staff, new ExtraCond().setRole(roleId));
	}

	/**
	 * Get the staffs according to specific extra condition {@link ExtraCond}
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * @return the staff to extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the role to any staff does NOT exist
	 */
	public static List<Staff> getByCond(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Get the staffs according to specific extra condition {@link ExtraCond}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * @return the staff to extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the role to any staff does NOT exist
	 */
	public static List<Staff> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		return getByCond(dbCon, staff.getRestaurantId(), extraCond, null);
	}
	
	/**
	 * Get the staffs according to specific extra condition {@link ExtraCond}
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * @return the staff to extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the role to any staff does NOT exist
	 */
	private static List<Staff> getByCond(DBCon dbCon, int restaurantId, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException{
		
		String sql = " SELECT "	+
					 " STAFF.staff_id, STAFF.restaurant_id, STAFF.name, STAFF.role_id, STAFF.tele, STAFF.pwd, STAFF.type AS staff_type, " +
					 " REST.type AS restaurant_type " +
					 " FROM " + Params.dbName + ".staff STAFF " + " " +
					 " JOIN " + Params.dbName + ".restaurant REST ON REST.id = STAFF.restaurant_id " +
					 " LEFT JOIN " + Params.dbName + ".role ROLE ON STAFF.role_id = ROLE.role_id " +
					 " WHERE 1 = 1 " +
					 (extraCond != null ? extraCond.setRestaurantId(restaurantId) : " ") +
					 (orderClause != null ? orderClause : " ORDER BY ROLE.cate, STAFF.staff_id ");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		final List<Staff> result = new ArrayList<Staff>();
		while(dbCon.rs.next()){
			Staff staff = new Staff(dbCon.rs.getInt("staff_id"));
			
			staff.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			staff.setName(dbCon.rs.getString("name"));
			staff.setRole(new Role(dbCon.rs.getInt("role_id")));
			staff.setMobile(dbCon.rs.getString("tele"));
			staff.setPwd(dbCon.rs.getString("pwd"));
			staff.setType(Staff.Type.valueOf(dbCon.rs.getInt("staff_type")));
			staff.setRestaurantType(Restaurant.Type.valueOf(dbCon.rs.getInt("restaurant_type")));
			
			result.add(staff);
		}
		dbCon.rs.close();
		
		for(Staff staff : result){
			//Get the group id to each staff in case of branch.
			if(staff.isBranch()){
				sql = " SELECT group_id FROM " + Params.dbName + ".restaurant_chain WHERE branch_id = " + staff.getRestaurantId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					staff.setGroupId(dbCon.rs.getInt("group_id"));
				}
				dbCon.rs.close();
			}
			
			//Get the associated role to each staff.
			staff.setRole(RoleDao.getById(dbCon, staff, staff.getRole().getId()));
		}
		
		return result;
	}
	
	/**
	 * Insert a new staff according to builder {@link Staff#InsertBuilder}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new staff
	 * @return the staff id just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new staff according to builder {@link Staff#InsertBuilder}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new staff
	 * @return the staff id just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, InsertBuilder builder) throws SQLException{
		String sql;
		
		Staff s = builder.build();
		
		sql = " INSERT INTO " + Params.dbName + ".staff(restaurant_id, role_id, name, tele, pwd, type) VALUES(" +
			  staff.getRestaurantId() + ", " +
			  s.getRole().getId() + ", " +
			  "'" + s.getName() + "', " +
			  "'" + s.getMobile() + "', " +
			  " MD5('" + s.getPwd() + "'), " +
			  s.getType().getVal() + ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int staffId;
		if(dbCon.rs.next()){
			staffId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("failed to insert staff");
		}
		dbCon.rs.close();
		
		return staffId;
	}
	
	/**
	 * Update the staff according to builder {@link Staff#UpdateBuilder}
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			<li>throws if the staff to update does NOT exist
	 * 			<li>throws if the staff to update belongs to reserved
	 */
	public static void update(Staff staff, UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the staff according to builder {@link Staff#UpdateBuilder}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			<li>throws if the staff to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, UpdateBuilder builder) throws SQLException, BusinessException{
		String sql;
		
		Staff s = builder.build();
		
		//管理员只能修改密码
		if(getById(dbCon, s.getId()).getRole().getCategory() == Role.Category.ADMIN){
			if(builder.isRoleChanged() || builder.isNameChanged() || builder.isMobileChanged()){
				throw new BusinessException(StaffError.ADMIN_STAFF_NOT_ALLOW_MODIFIED);
			}
		}
		
		sql = " UPDATE " + Params.dbName + ".staff SET " + 
			  " staff_id = " + s.getId() +
			  (builder.isNameChanged() ? " ,name = '" + s.getName() + "'" : "") +
			  (builder.isMobileChanged() ? " ,tele = '" + s.getMobile() + "'" : "") +
			  (builder.isPwdChanged() ? " ,pwd = MD5('" + s.getPwd() + "')" : "") +
			  (builder.isRoleChanged() ? " ,role_id = " + s.getRole().getId() : "") +
			  " WHERE staff_id = " + s.getId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StaffError.STAFF_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the staff to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param staffId
	 * 			the staff id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the staff to delete belongs to reserved 
	 * 			<li>throws if the staff to delete does NOT exist
	 */
	public static void deleteById(Staff staff, int staffId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, staff, staffId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the staff to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param staffId
	 * 			the staff id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the staff to delete belongs to reserved 
	 * 			<li>throws if the staff to delete does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int staffId) throws SQLException, BusinessException{
		String sql;
		
		sql = " SELECT type FROM " + Params.dbName + ".staff WHERE staff_id = " + staffId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			if(Staff.Type.valueOf(dbCon.rs.getInt("type")) == Staff.Type.RESERVED){
				throw new BusinessException(StaffError.RESERVED_STAFF_NOT_ALLOW_MODIFY);
			}
		}
		dbCon.rs.close();
		
		
		if(deleteByCond(dbCon, staff, new ExtraCond().setStaff(staffId)) == 0){
			throw new BusinessException(StaffError.STAFF_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the staff to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount to staff deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any role to staff does NOT exist
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException {
		int amount = 0;
		for(Staff s : getByCond(dbCon, staff, extraCond)){
			if(s.getType() == Staff.Type.RESERVED || s.getType() == Staff.Type.WX){
				continue;
			}
			String sql = " DELETE FROM " + Params.dbName + ".staff WHERE staff_id = " + s.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		return amount;
	}

}
