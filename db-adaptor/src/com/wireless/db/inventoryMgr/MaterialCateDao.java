package com.wireless.db.inventoryMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MaterialError;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;

public class MaterialCateDao {
	
	public static class ExtraCond {
		private int id;
		private String name;
		private MaterialCate.Type type;
		public ExtraCond setId(int id) {
			this.id = id;
			return this;
		}
		
		public ExtraCond setName(String name) {
			this.name = name;
			return this;
		}
		
		public ExtraCond setType(MaterialCate.Type type) {
			this.type = type;
			return this;
		}
		
		public String toString() {
			final StringBuilder extraCond = new StringBuilder();
			if(id > 0){
				extraCond.append(" AND cate_id = " + this.id);
			}
			
			if(name != null && !name.isEmpty()){
				extraCond.append(" AND name = " + this.name);
			}
			
			if(type != null){
				extraCond.append(" AND type = " + this.type.getValue());
			}
			
			return extraCond.toString();
		}
	}
	
	
	
	
	/**
	 * Insert the material category by builder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff perform this action
	 * @param builder
	 * 			the builder to insert this material category
	 * @return
	 * @throws SQLException
	 * 			throw if failed to execute SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, MaterialCate.InsertBuilder builder) throws SQLException{
		MaterialCate materialCate = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".material_cate " 
			  + " (`restaurant_id`, `name`, `type`)" + " VALUES(" 
			  + staff.getRestaurantId() + ", " 
			  + "'" + materialCate.getName()  + "', " 
			  + materialCate.getType().getValue() + ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int id = 0;
		if(dbCon.rs.next()){
			id = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The materialCate id NOT generated successfully");
		} 
		
		return id;
	}
	
	
	/**
	 * Insert the material category by builder.
	 * @param staff
	 * 			the staff perform this action
	 * @param builder
	 * 			the builder to insert this material category
	 * @return
	 * @throws BusinessException
	 * 			throws if material category to this builder is NOT exist
	 * @throws SQLException
	 * 			throw if failed to execute SQL statement
	 */
	public static int insert(Staff staff, MaterialCate.InsertBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Update the material category by builder.
	 * @param dbCon
	 * 			the database to connection
	 * @param staff
	 * 			the staff perform this action
	 * @param builder
	 * 			the builder to update this material category
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if material category to this builder is NOT exist 
	 */
	public static void update(DBCon dbCon, Staff staff, MaterialCate.UpdateBuilder builder) throws SQLException, BusinessException{
		MaterialCate materialCate = builder.build();
		String sql;
		sql = " UPDATE " + Params.dbName + ".material_cate SET "
			  + " cate_id = cate_id "
			  + (builder.isNameChanged() ? ", name = '" + materialCate.getName() + "' " : "" )
			  + (builder.isTypeChanged() ? ", type = " + materialCate.getType().getValue() : "")
			  + " WHERE cate_id = " + materialCate.getId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(MaterialError.CATE_NOT_EXIST);
		}
	}
	
	
	
	/**
	 * Update the material category by builder
	 * @param staff
	 * 			the staff perform this action
	 * @param builder
	 * 			the builder to update this material category
	 * @throws BusinessException
	 * 			throws if material category to this builder does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void update(Staff staff, MaterialCate.UpdateBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			MaterialCateDao.update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the material category to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to material category
	 * @return the material category to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the material category to this id does NOT exist
	 */
	public static MaterialCate getById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		List<MaterialCate> result = getByCond(dbCon, staff, new MaterialCateDao.ExtraCond().setId(id));
		if(result.isEmpty()){
			throw new BusinessException(MaterialError.CATE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	
	
	/**
	 * Get the material category to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to material category
	 * @return the material category to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the material category to this id does NOT exist
	 */
	public static MaterialCate getById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			return getById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
		
	}
	
	/**
	 * Get the material category to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition to material category
	 * @return
	 * @throws SQLException
	 * 			throws if the material category to these conditions does NOT exist
	 */
	public static List<MaterialCate> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".material_cate " + 
			  " WHERE 1 = 1 " + 
			  " AND restaurant_id = " + staff.getRestaurantId() + 
			  (extraCond != null ? extraCond.toString() : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		final List<MaterialCate> result = new ArrayList<>();
		while(dbCon.rs.next()){
			MaterialCate materialCate = new MaterialCate(dbCon.rs.getInt("cate_id"));
			materialCate.setName(dbCon.rs.getString("name"));
			materialCate.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			materialCate.setType(MaterialCate.Type.valueOf(dbCon.rs.getInt("type")));
			result.add(materialCate);
		}
		dbCon.rs.close();
		return result;
	}
	
	
	
	/**
	 * Get the material category to extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition to material category
	 * @return
	 * @throws SQLException
	 * 			throws if the material category to these conditions does NOT exist
	 */
	public static List<MaterialCate> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}

	
	/**
	 * Delete material category by the extra condition. 
	 * @param dbCon
	 * 			the database to connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition to material category
	 * @return
	 * @throws BusinessException
	 * 			throws if material category to these conditions does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws BusinessException, SQLException {
		int amount = 0;
		for(MaterialCate materialCate : getByCond(staff, extraCond)){
			String sql = " DELETE FROM " + Params.dbName + ".material_cate WHERE cate_id = " + materialCate.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount ++;
			}
		}
		return amount;
	}
	
	
	/**
	 * Delete material category by the extra condition
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition to material category
	 * @return
	 * @throws BusinessException
	 * 			throws if material category to these conditions does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(Staff staff, ExtraCond extraCond) throws BusinessException, SQLException {
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			return deleteByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
}
