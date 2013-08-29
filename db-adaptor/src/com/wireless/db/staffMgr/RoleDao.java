package com.wireless.db.staffMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StaffError;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Role.Category;
import com.wireless.pojo.staffMgr.Role.InsertBuilder;
import com.wireless.pojo.staffMgr.Role.Type;
import com.wireless.pojo.staffMgr.Staff;

public class RoleDao {

	/**
	 * Get the role according to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff
	 * @param roleId
	 * 			the role id
	 * @return the role to this specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the role to this id does NOT exist
	 */
	public static Role getRoleById(DBCon dbCon, Staff staff, int roleId) throws SQLException, BusinessException{
		List<Role> result = getRoles(dbCon, staff, " AND role_id = " + roleId, null);
		if(result.isEmpty()){
			throw new BusinessException(StaffError.ROLE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	/**
	 * Get the role according to specific id.
	 * @param staff
	 * 			the staff
	 * @param roleId
	 * 			the role id
	 * @return the role to this specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the role to this id does NOT exist
	 */
	public static Role getRoleById(Staff staff, int roleId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getRoleById(dbCon, staff, roleId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	private static List<Role> getRoles(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		String sql;
		
		//Get the basic info to each role
		sql = " SELECT role_id, restaurant_id, name, type, cate " +
			  " FROM " + Params.dbName + ".role" +
			  " WHERE restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<Role> roles = new ArrayList<Role>();
		while(dbCon.rs.next()){
			Role role = new Role(dbCon.rs.getInt("role_id"));
			role.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			role.setName(dbCon.rs.getString("name"));
			role.setType(Type.valueOf(dbCon.rs.getInt("type")));
			role.setCategory(Category.valueOf(dbCon.rs.getInt("cate")));
			roles.add(role);
		}
		dbCon.rs.close();
		
		for(Role role : roles){
			//Get the privileges to each role
			sql = " SELECT " +
				  " P.pri_id, P.pri_code, P.cate, RP.restaurant_id " + 
				  " FROM " + Params.dbName + ".role_privilege RP " +
				  " JOIN " + Params.dbName + ".privilege P ON RP.pri_id = P.pri_id " +
				  " WHERE RP.role_id = " + role.getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Privilege privilege = new Privilege(dbCon.rs.getInt("pri_id"),
													Code.valueOf(dbCon.rs.getInt("pri_code")),
													dbCon.rs.getInt("restaurant_id"));
				
				role.addPrivilege(privilege);
			}
			dbCon.rs.close();
			
			//Get the allowed discounts in case of the discount privilege
			for(Privilege privilege : role.getPrivileges()){
				if(privilege.getCode() == Code.DISCOUNT){
					sql = " SELECT D.discount_id, D.restaurant_id, D.name, D.level, D.status "	+
						  " FROM " + Params.dbName + ".role_discount RD " +
						  " JOIN " + Params.dbName + ".discount D ON RD.discount_id = D.discount_id " +
						  " WHERE RD.role_id = " + role.getId();
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					while(dbCon.rs.next()){
						Discount discount = new Discount(dbCon.rs.getInt("discount_id"));
						discount.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
						discount.setName(dbCon.rs.getString("name"));
						discount.setLevel(dbCon.rs.getShort("level"));
						discount.setStatus(dbCon.rs.getInt("status"));
						privilege.addDiscount(discount);
					}
					dbCon.rs.close();
				}
			}
		}
		
		return Collections.unmodifiableList(roles);
	}
	
	
	/**
	 * Get the list of Role.
	 * @param staff
	 * 			the information of Staff
	 * @param extraCond
	 * 			the extra condition
	 * @param otherClause
	 * 			the other Clause
	 * @return	list of Role
	 * @throws SQLException
	 */
	public static List<Role> getRoles(Staff staff, String extraCond, String otherClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getRoles(dbCon, staff, extraCond, otherClause);
			
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Insert a Role.
	 * @param staff
	 * 			the Staff
	 * @param builder
	 * 			the detail of Role
	 * @return	the id of Role just create
	 * @throws SQLException
	 */
	public static int insertRole(Staff staff, InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			return insertRole(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Insert a Role.
	 * @param dbCon
	 * @param staff
	 * @param builder
	 * @return
	 * @throws SQLException
	 */
	public static int insertRole(DBCon dbCon, Staff staff, InsertBuilder builder) throws SQLException{
		String sql;
		int roleId;
		sql = "INSERT INTO " + Params.dbName + ".role(restaurant_id, name, type, cate) " +
				" VALUES (" +
				builder.getRestaurantId() + ", " +
				"'" + builder.getName() + "', " +
				builder.getType().getVal() + ", " +
				builder.getCategoty().getVal() + 
				")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			roleId = dbCon.rs.getInt(1);
			//关联权限和折扣
			for (Privilege privilege : builder.getPrivileges()) {
				if(privilege.getId() == 0){
					DBCon priCon = new DBCon();
					try{
						priCon.connect();
						String selectPid = "SELECT pri_id FROM " + Params.dbName + ".privilege" + " WHERE pri_code = " + privilege.getCode().getVal();
						priCon.rs = priCon.stmt.executeQuery(selectPid);
						if(priCon.rs.next()){
							privilege.setId(priCon.rs.getInt("pri_id"));
						}
					}finally{
						priCon.disconnect();
					}

					
				}
				String pSql = "INSERT INTO " + Params.dbName + ".role_privilege(role_id, pri_id, restaurant_id) " +
								" VALUES(" +
								roleId + ", " +
								privilege.getId() + ", " +
								staff.getRestaurantId() + ")";
				dbCon.stmt.executeUpdate(pSql);
				if(privilege.getCode() == Code.DISCOUNT){
					for (Discount discount : privilege.getDiscounts()) {
						String rdSql = "INSERT INTO " + Params.dbName + ".role_discount(role_id, discount_id) " +
								" VALUES(" +
								roleId + ", " +
								discount.getId() + ")";
						dbCon.stmt.executeUpdate(rdSql);
						
					}
				}
			}
		}else{
			throw new SQLException("The id is not generated successfully.");
		}
		return roleId;
	}
	/**
	 * Update the information of Role.
	 * @param staff
	 * 			the Staff
	 * @param role
	 * 			the role to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static void updateRole(Staff staff, Role role) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			updateRole(dbCon, staff, role);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Update the information of Role.
	 * @param dbCon
	 * @param staff
	 * @param role
	 * @throws SQLException
	 */
	public static void updateRole(DBCon dbCon, Staff staff, Role role) throws SQLException{
		String sql ;
		sql = "UPDATE " + Params.dbName + ".role SET " +
				" name = '" + role.getName() + "'" +
				" WHERE role_id = " + role.getId();
		dbCon.stmt.executeUpdate(sql);
		//删除权限关联
		String delRPSql = "DELETE FROM " + Params.dbName + ".role_privilege" +
							" WHERE role_id = " + role.getId();
		dbCon.stmt.executeUpdate(delRPSql);
		//删除折扣关联
		String delRDSql = "DELETE FROM " + Params.dbName + ".role_discount" + 
							" WHERE role_id = " + role.getId();
		dbCon.stmt.executeUpdate(delRDSql);
		//重新关联权限和折扣
		for (Privilege privilege : role.getPrivileges()) {
			String pSql = "INSERT INTO " + Params.dbName + ".role_privilege(role_id, pri_id, restaurant_id) " +
					" VALUES(" +
					role.getId() + ", " +
					privilege.getId() + ", " +
					staff.getRestaurantId() + ")";
			dbCon.stmt.executeUpdate(pSql);
			
			if(privilege.getCode() == Code.DISCOUNT){
				for (Discount discount : privilege.getDiscounts()) {
					String rdSql = "INSERT INTO " + Params.dbName + ".role_discount(role_id, discount_id) " +
							" VALUES(" +
							role.getId() + ", " +
							discount.getId() + ")";
					dbCon.stmt.executeUpdate(rdSql);
					
				}
			}
		}
				
	}
	
	/**
	 * Delete the Role.
	 * @param dbCon
	 * 			the database connection
	 * @param roleId
	 * @throws SQLException
	 */
	public static void deleteRole(DBCon dbCon, int roleId) throws SQLException{
		String sql = "DELETE FROM " + Params.dbName + ".role" + 
					" WHERE role_id = " + roleId;
		dbCon.stmt.executeUpdate(sql);
		//删除权限关联
		String delRPSql = "DELETE FROM " + Params.dbName + ".role_privilege" +
							" WHERE role_id = " + roleId;
		dbCon.stmt.executeUpdate(delRPSql);
		//删除折扣关联
		String delRDSql = "DELETE FROM " + Params.dbName + ".role_discount" + 
							" WHERE role_id = " + roleId;
		dbCon.stmt.executeUpdate(delRDSql);
		
	}
	
	/**
	 * Delete the Role.
	 * @param roleId
	 * 			the id of Role
	 * @throws SQLException
	 */
	public static void deleteRole(int roleId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteRole(dbCon, roleId);
		}finally{
			dbCon.disconnect();
		}
	}
	
}
