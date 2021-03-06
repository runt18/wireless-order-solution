package com.wireless.db.deptMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.DeptError;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;

public class DepartmentDao {

	public static class ExtraCond{
		private Department.DeptId deptId;
		private final List<Department.Type> types = new ArrayList<>();
		private String name;
		
		public ExtraCond setId(Department.DeptId deptId){
			this.deptId = deptId;
			return this;
		}
		
		public ExtraCond setId(int id){
			this.deptId = Department.DeptId.valueOf(id);
			return this;
		}
		
		public ExtraCond setName(String name){
			this.name = name;
			return this;
		}
		
		public ExtraCond addType(Department.Type type){
			types.add(type);
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(deptId != null){
				extraCond.append(" AND DEPT.dept_id = " + deptId.getVal());
			}
			StringBuilder typeCond = new StringBuilder();
			for(Department.Type type : types){
				if(typeCond.length() == 0){
					typeCond.append(type.getVal());
				}else{
					typeCond.append(",").append(type.getVal());
				}
			}
			if(typeCond.length() != 0){
				extraCond.append(" AND type IN (" + typeCond.toString() + ")");
			}
			
			if(name != null){
				extraCond.append(" AND DEPT.name = '" + name + "' ");
			}
			
			return extraCond.toString();
		}
	}
	
	/**
	 * Move the department up to another.  
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the move builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the department to move does NOT exist
	 */
	public static void move(Staff staff, Department.MoveBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			move(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Move the department up to another.  
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the move builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the department to move does NOT exist
	 */
	public static void move(DBCon dbCon, Staff staff, Department.MoveBuilder builder) throws SQLException, BusinessException{
		Department from = getById(dbCon, staff, builder.from());
		Department to = getById(dbCon, staff, builder.to());
		
		String sql;
		if(from.getDisplayId() < to.getDisplayId()){
			sql = " UPDATE " + Params.dbName + ".department SET " +
				  " display_id = display_id - 1 " +
				  " WHERE 1 = 1 " +
				  " AND display_id > " + from.getDisplayId() +
				  " AND display_id < " + to.getDisplayId() + 
				  //" AND type = " + Department.Type.NORMAL.getVal() +
				  " AND restaurant_id = " + staff.getRestaurantId();
			dbCon.stmt.executeUpdate(sql);
			
			sql = " UPDATE " + Params.dbName + ".department SET " +
				  " display_id = " + (to.getDisplayId() - 1) +
				  " WHERE dept_id = " + from.getId() + 
				  " AND restaurant_id = " + staff.getRestaurantId();
			dbCon.stmt.executeUpdate(sql);
			
		}else if(from.getDisplayId() > to.getDisplayId()){
			sql = " UPDATE " + Params.dbName + ".department SET " +
				  " display_id = display_id + 1 " +
				  " WHERE 1 = 1 " +
				  " AND display_id >= " + to.getDisplayId() +
				  " AND display_id < " + from.getDisplayId() +
				  //" AND type = " + Department.Type.NORMAL.getVal() +
				  " AND restaurant_id = " + staff.getRestaurantId();
			dbCon.stmt.executeUpdate(sql);
			
			sql = " UPDATE " + Params.dbName + ".department SET " +
				  " display_id = " + to.getDisplayId() +
				  " WHERE dept_id = " + from.getId() +
				  " AND restaurant_id = " + staff.getRestaurantId();
			dbCon.stmt.executeUpdate(sql);
		}
		
	}
	
	/**
	 * Get the department to a specified restaurant defined in {@link Staff} and other extra condition.
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
	public static List<Department> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
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
	public static List<Department> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		
		final List<Department> result = new ArrayList<Department>();
		
		String sql = " SELECT dept_id, name, restaurant_id, type, display_id FROM " + Params.dbName + ".department DEPT " +
					 " WHERE 1 = 1 " +
					 " AND DEPT.restaurant_id = " + staff.getRestaurantId() +
					 (extraCond != null ? extraCond.toString() : "") + " " +
					 (orderClause != null ? orderClause : " ORDER BY display_id ");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			result.add(new Department(dbCon.rs.getString("name"),
									  dbCon.rs.getShort("dept_id"),
									  dbCon.rs.getInt("restaurant_id"),
									  Department.Type.valueOf(dbCon.rs.getShort("type")),
									  dbCon.rs.getInt("display_id")));
			
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Get the department to a specific type.
	 * @param staff
	 * 			the staff to perform this action
	 * @param type
	 * 			the department type
	 * @return the department to this specific type
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Department> getByType(Staff staff, Department.Type type) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByType(dbCon, staff, type);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the department to a specific type.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param type
	 * 			the department type
	 * @return the department to this specific type
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Department> getByType(DBCon dbCon, Staff staff, Department.Type type) throws SQLException{
		return getByCond(dbCon, staff, new ExtraCond().addType(type) , null);
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
		List<Department> result = new ArrayList<Department>(getByType(dbCon, staff, Department.Type.NORMAL));
		result.addAll(getByCond(dbCon, staff, new ExtraCond().addType(Department.Type.WARE_HOUSE), null));
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
	public static Department getById(Staff staff, int deptId) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, deptId);
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
	public static Department getById(DBCon dbCon, Staff staff, int deptId) throws BusinessException, SQLException{
		List<Department> result = getByCond(dbCon, staff, new ExtraCond().setId(deptId), null);
		if(result.isEmpty()){
			throw new BusinessException(DeptError.DEPT_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Remove a department and put it back to idle pool.
	 * @param staff
	 * 			the staff to perform this action
	 * @param deptId
	 * 			the department id to remove
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the department is NOT empty
	 * 			throws if the department to remove does NOT exist
	 */
	public static void remove(Staff staff, int deptId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			remove(dbCon, staff, deptId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Remove a department and put it back to idle pool.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param deptId
	 * 			the department id to remove
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the department is NOT empty
	 * 			<li>throws if the department to remove does NOT exist
	 */
	public static void remove(DBCon dbCon, Staff staff, int deptId) throws SQLException, BusinessException{
		String sql;
		
		//Check to see whether the department contains any kitchen.
		sql = " SELECT kitchen_id FROM " + Params.dbName + ".kitchen WHERE " +
			  " restaurant_id = " + staff.getRestaurantId() + 
			  " AND dept_id = " + deptId +
			  " AND type = " + Kitchen.Type.NORMAL.getVal() +
			  " LIMIT 1 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(DeptError.DEPT_NOT_EMPTY);
		}
		dbCon.rs.close();
		
		//Delete the print scheme associated with this department.
		sql = " DELETE FROM " + Params.dbName + ".func_dept WHERE 1 = 1 " +
			  " AND dept_id = " + deptId +
			  " AND restaurant_id = " + staff.getRestaurantId();
		dbCon.stmt.executeUpdate(sql);
		
		//Update the department status to idle.
		sql = " UPDATE " + Params.dbName + ".department SET " +
			  " dept_id = " + deptId +
			  " ,type = " + Department.Type.IDLE.getVal() +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  " AND dept_id = " + deptId; 
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(DeptError.DEPT_NOT_EXIST);
		}
	}
	
	/**
	 * Add the department from the idle pool.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to add department
	 * @return the department id to add
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if insufficient idle department
	 * 			throws if the department to add 
	 */
	public static short add(Staff staff, Department.AddBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return add(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Add the department from the idle pool.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to add department
	 * @return the department id to add
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if insufficient idle department
	 * 			<li>throws if the department to add 
	 */
	public static short add(DBCon dbCon, Staff staff, Department.AddBuilder builder) throws SQLException, BusinessException{
		String sql;
		//Check to see whether any unused kitchens exist.
		sql = " SELECT dept_id FROM " + Params.dbName + ".department " +
			  " WHERE 1 = 1 " +
			  " AND type = " + Department.Type.IDLE.getVal() + 
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  " ORDER BY dept_id LIMIT 1 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		short deptId = 0;
		if(dbCon.rs.next()){
			deptId = dbCon.rs.getShort(1);
		}else{
			throw new BusinessException(DeptError.INSUFFICIENT_IDLE_DEPT);
		}
		dbCon.rs.close();
		
		Department d = builder.build();
		sql = " UPDATE " + Params.dbName + ".department SET " +
			  " dept_id = " + deptId +
			  " ,name = '" + d.getName() + "'" +
			  " ,type = " + Department.Type.NORMAL.getVal() +
			  " WHERE 1 = 1 " +
			  " AND dept_id = " + deptId +
			  " AND restaurant_id = " + staff.getRestaurantId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(DeptError.DEPT_NOT_EXIST);
		}
		
		//Update the name associated feast kitchen. 
		List<Kitchen> result = KitchenDao.getByCond(dbCon, staff, new KitchenDao.ExtraCond().setDeptId(deptId).setType(Kitchen.Type.FEAST), null);
		KitchenDao.update(dbCon, staff, new Kitchen.UpdateBuilder(result.get(0).getId()).setName(d.getName() + "酒席费"));

		return deptId;
	}
	
	/**
	 * Update the department according to a builder.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to update the department
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the department to update does NOT exist
	 */
	public static void update(Staff staff, Department.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the department according to a builder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to update the department
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the department to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, Department.UpdateBuilder builder) throws SQLException, BusinessException{
		Department d = builder.build();
		
		//Update the name associated feast kitchen. 
		List<Kitchen> result = KitchenDao.getByCond(dbCon, staff, new KitchenDao.ExtraCond().setDeptId(d.getId()).setType(Kitchen.Type.FEAST), null);
		KitchenDao.update(dbCon, staff, new Kitchen.UpdateBuilder(result.get(0).getId()).setName(d.getName() + "酒席费"));
		
		String sql;
		sql = " UPDATE " + Params.dbName + ".department SET " +
			  " name = '" + d.getName() + "'" +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  " AND dept_id = " + d.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(DeptError.DEPT_NOT_EXIST);
		}
	}
	
	/**
	 * Insert a new department.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new department
	 * @return the id to department just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static short insert(DBCon dbCon, Staff staff, Department.InsertBuilder builder) throws SQLException{

		String sql;
		
		Department deptToInsert = builder.build();

		int displayId = 0;
		if(deptToInsert.isNull() || deptToInsert.isTemp() || deptToInsert.isWare()){
			displayId = 0;
		}else{		
			//Calculate the display id in case of normal kitchen.
			sql = " SELECT IFNULL(MAX(display_id), 0) + 1 FROM " + Params.dbName + ".department WHERE restaurant_id = " + staff.getRestaurantId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				displayId = dbCon.rs.getInt(1);
			}
			dbCon.rs.close();
		}
		
		sql = " INSERT INTO " + Params.dbName + ".department " +
		      " (dept_id, restaurant_id, name, type, display_id) " +
			  " VALUES(" +
		      deptToInsert.getId() + "," +
		      deptToInsert.getRestaurantId() + "," +
			  "'" + deptToInsert.getName() + "'," +
		      deptToInsert.getType().getVal() + "," +
			  displayId +
		      ")";
		
		dbCon.stmt.executeUpdate(sql);
		
		//Insert the associated kitchen to feast.
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(deptToInsert.getName() + "酒席费", Department.DeptId.valueOf(deptToInsert.getId()), Kitchen.Type.FEAST));
		
		return deptToInsert.getId();
	}
	
}
