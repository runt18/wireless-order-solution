package com.wireless.db.staffMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StaffError;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.pojo.staffMgr.Role;
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
			roles.add(role);
		}
		dbCon.rs.close();
		
		for(Role role : roles){
			//Get the privileges to each role
			sql = " SELECT " +
				  " P.pri_id, P.pri_code, P.cate, RP.discount_privilege_id, RP.restaurant_id " + 
				  " FROM " + Params.dbName + ".role_privilege RP " +
				  " JOIN " + Params.dbName + ".privilege P ON RP.pri_id = P.pri_id " +
				  " WHERE RP.role_id = " + role.getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Privilege privilege = new Privilege(dbCon.rs.getInt("pri_id"),
													Code.valueOf(dbCon.rs.getInt("pri_code")),
													dbCon.rs.getInt("restaurant_id"));
				
				privilege.setDiscountPrivilegeId(dbCon.rs.getInt("discount_privilege_id"));
				role.addPrivilege(privilege);
			}
			dbCon.rs.close();
			
			//Get the allowed discounts in case of the discount privilege
			for(Privilege privilege : role.getPrivileges()){
				if(privilege.getCode() == Code.DISCOUNT){
					sql = " SELECT D.discount_id, D.restaurant_id, D.name, D.level, D.status "	+
						  " FROM " + Params.dbName + ".discount_privilege DP " +
						  " JOIN " + Params.dbName + ".discount D ON DP.discount_id = D.discount_id " +
						  " WHERE DP.discount_privilege_id = " + privilege.getDiscountPrivilegeId();
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
	
}
