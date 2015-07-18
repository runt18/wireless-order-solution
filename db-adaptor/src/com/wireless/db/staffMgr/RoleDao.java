package com.wireless.db.staffMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StaffError;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.pojo.staffMgr.Privilege4Price;
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
	public static Role getById(DBCon dbCon, Staff staff, int roleId) throws SQLException, BusinessException{
		List<Role> result = getByCond(dbCon, staff, " AND role_id = " + roleId, null);
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
	public static Role getyById(Staff staff, int roleId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, roleId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the roles to specific category {@link Role#Category}
	 * @param staff
	 * 			the staff to perform this action
	 * @param category
	 * 			the role category
	 * @return the roles to this category
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Role> getByCategory(Staff staff, Role.Category category) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCategory(dbCon, staff, category);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the roles to specific category {@link Role#Category}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param category
	 * 			the role category
	 * @return the roles to this category
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Role> getByCategory(DBCon dbCon, Staff staff, Role.Category category) throws SQLException{
		return getByCond(dbCon, staff, " AND cate = " + category.getVal(), null);
	}
	
	private static List<Role> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		String sql;
		
		//Get the basic info to each role
		sql = " SELECT role_id, restaurant_id, name, type, cate " +
			  " FROM " + Params.dbName + ".role" +
			  " WHERE restaurant_id = " + staff.getRestaurantId() + " " +
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
			role.setPrivileges(PrivilegeDao.getByCond(dbCon, staff, new PrivilegeDao.ExtraCond().setRole(role)));
		}
		
		return roles;
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
			return getByCond(dbCon, staff, extraCond, otherClause);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new role according to builder{@link Role.InsertBuilder}
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert role
	 * @return the id to role just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int roleId = insert(dbCon, staff, builder);
			dbCon.conn.commit();
			return roleId;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Insert a new role according to builder{@link Role.InsertBuilder}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert role
	 * @return the id to role just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, Role.InsertBuilder builder) throws SQLException{
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
		}else{
			throw new SQLException("The id is not generated successfully.");
		}
		dbCon.rs.close();
		
		//Insert the associated privileges, discounts and price plans.
		insertPrivileges(dbCon, staff, roleId, builder.getPrivileges());
		
		return roleId;
	}
	
	private static void insertPrivileges(DBCon dbCon, Staff staff, int roleId, List<Privilege> privileges) throws SQLException{
		for (Privilege privilege : privileges) {
			String sql = " SELECT pri_id FROM " + Params.dbName + ".privilege" + " WHERE pri_code = " + privilege.getCode().getVal();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				privilege.setId(dbCon.rs.getInt("pri_id"));
			}else{
				continue;
			}
			dbCon.rs.close();
				
			sql = " INSERT INTO " + Params.dbName + ".role_privilege(role_id, pri_id, restaurant_id) " +
				  " VALUES( " +
				  roleId + ", " +
				  privilege.getId() + ", " +
				  staff.getRestaurantId() + ")";
			dbCon.stmt.executeUpdate(sql);
			
			//Insert the associated discounts.
			if(privilege.getCode() == Code.DISCOUNT){
				for (Discount discount : privilege.getDiscounts()) {
					//Check to see whether the discount exist
					boolean isExist = false;
					sql = " SELECT COUNT(*) FROM " + Params.dbName + ".discount WHERE discount_id = " + discount.getId();
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					if(dbCon.rs.next()){
						if(dbCon.rs.getInt(1) > 0){
							isExist = true;
						}
					}
					dbCon.rs.close();
					if(isExist){
						sql = " INSERT INTO " + Params.dbName + ".role_discount (role_id, discount_id) " +
							  " VALUES(" +
							  roleId + ", " +
							  discount.getId() + ")";
						dbCon.stmt.executeUpdate(sql);
					}
				}
			}
			
			//Insert the associated price plans.
			if(privilege.getCode() == Code.PRICE_PLAN){
				for(PricePlan pricePlan : ((Privilege4Price)privilege).getPricePlans()){
					//Check to see whether the price plan exist
					if(PricePlanDao.getByCond(dbCon, staff, new PricePlanDao.ExtraCond().setId(pricePlan.getId())).isEmpty()){
						continue;
					}else{
						sql = " INSERT INTO " + Params.dbName + ".role_price_plan (role_id, price_plan_id) " +
							  " VALUES( " +
							  roleId + "," +
							  pricePlan.getId() + ")";
						dbCon.stmt.executeUpdate(sql);
					}
				}
			}
		}
	}
	
	/**
	 * Update the information of Role.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to update role
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the role to update does NOT exist
	 */
	public static void update(Staff staff, Role.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.conn.setAutoCommit(false);
			dbCon.connect();
			update(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(SQLException | BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the information of Role.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to update role
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if either cases below
	 * 			<li>the role to update does NOT exist
	 * 			<li>the role to update belongs to reserved
	 */
	public static void update(DBCon dbCon, Staff staff, Role.UpdateBuilder builder) throws SQLException, BusinessException{

		Role role = builder.build();

		String sql ;
		
		//Check if the role belongs to reserved.
		sql = " SELECT type FROM " + Params.dbName + ".role WHERE role_id = " + role.getId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			if(Role.Type.valueOf(dbCon.rs.getInt("type")) == Role.Type.RESERVED){
				throw new BusinessException(StaffError.RESERVED_ROLE_NOT_ALLOW_MODIFY);
			}
		}
		dbCon.rs.close();
		
		sql = " UPDATE " + Params.dbName + ".role SET " +
			  " role_id = " + role.getId() +
			  (builder.isNameChanged() ? (" ,name = '" + role.getName() + "'") : "") +
			  " WHERE role_id = " + role.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StaffError.ROLE_NOT_EXIST);
		}
		
		if(builder.isPrivilegeChanged()){
			
			//删除权限关联
			sql = " DELETE FROM " + Params.dbName + ".role_privilege WHERE role_id = " + role.getId();
			dbCon.stmt.executeUpdate(sql);
			
			//删除折扣关联
			sql = " DELETE FROM " + Params.dbName + ".role_discount WHERE role_id = " + role.getId();
			dbCon.stmt.executeUpdate(sql);
			
			//删除价格方案关联
			sql = " DELETE FROM " + Params.dbName + ".role_price_plan WHERE role_id = " + role.getId();
			dbCon.stmt.executeUpdate(sql);
			
			//重新关联权限和折扣
			insertPrivileges(dbCon, staff, role.getId(), role.getPrivileges());
		}
	}
	
	/**
	 * Delete the specific role
	 * @param dbCon
	 * 			the database connection
	 * @param roleId
	 * 			the role id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			<li>throws if the role to delete belongs to reserved
	 * 			<li>throws if the role to delete does NOT exist
	 */
	public static void deleteRole(DBCon dbCon, int roleId) throws SQLException, BusinessException{
		String sql;
		
		//Check if the role belongs to reserved.
		sql = " SELECT type FROM " + Params.dbName + ".role WHERE role_id = " + roleId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			if(Role.Type.valueOf(dbCon.rs.getInt("type")) == Role.Type.RESERVED){
				throw new BusinessException(StaffError.RESERVED_ROLE_NOT_ALLOW_MODIFY);
			}
		}
		dbCon.rs.close();
		
		sql = " DELETE FROM " + Params.dbName + ".role WHERE role_id = " + roleId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StaffError.ROLE_NOT_EXIST);
		}
		//删除权限关联
		sql = "DELETE FROM " + Params.dbName + ".role_privilege WHERE role_id = " + roleId;
		dbCon.stmt.executeUpdate(sql);
		
		//删除折扣关联
		sql = "DELETE FROM " + Params.dbName + ".role_discount WHERE role_id = " + roleId;
		dbCon.stmt.executeUpdate(sql);
		
		//删除价格方案关联
		sql = "DELETE FROM " + Params.dbName + ".role_price_plan WHERE role_id = " + roleId;
		dbCon.stmt.executeUpdate(sql);
		
	}
	
	/**
	 * Delete the specific role
	 * @param dbCon
	 * 			the database connection
	 * @param roleId
	 * 			the role id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			<li>throws if the role to delete belongs to reserved
	 * 			<li>throws if the role to delete does NOT exist
	 */
	public static void deleteRole(int roleId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteRole(dbCon, roleId);
		}finally{
			dbCon.disconnect();
		}
	}
	
}
