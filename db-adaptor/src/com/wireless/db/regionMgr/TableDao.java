package com.wireless.db.regionMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.TableError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.regionMgr.Table.InsertBuilder;
import com.wireless.pojo.staffMgr.Staff;

public class TableDao {

	public static class ExtraCond{
		private int id;
		private int aliasId;
		private String name;
		private Region.RegionId regionId;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setAliasId(int aliasId){
			this.aliasId = aliasId;
			return this;
		}
		
		public ExtraCond setName(String name){
			this.name = name;
			return this;
		}
		
		public ExtraCond setRegion(Region.RegionId regionId){
			this.regionId = regionId;
			return this;
		}
		
		public ExtraCond setRegion(int regionId){
			this.regionId = Region.RegionId.valueOf(regionId);
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND TBL.table_id = " + id);
			}
			if(aliasId != 0){
				extraCond.append(" AND TBL.table_alias = " + aliasId);
			}
			if(name != null){
				extraCond.append(" AND TBL.name LIKE '%" + name + "%'");
			}
			if(regionId != null){
				extraCond.append(" AND REGION.region_id = " + regionId.getId());
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Get the table detail according to table id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param tableId
	 * 			the table id to query
	 * @return the detail to this table id
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to query does NOT exist
	 */
	public static Table getById(DBCon dbCon, Staff staff, int tableId) throws SQLException, BusinessException{
		
		List<Table> result = getByCond(dbCon, staff, new ExtraCond().setId(tableId), null);
		if(result.isEmpty()){
			throw new BusinessException(TableError.TABLE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the table detail according to table id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param tableId
	 * 			the table id to query
	 * @return the detail to this table id
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to query does NOT exist
	 */
	public static Table getById(Staff staff, int tableId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, tableId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the table detail according to table alias of a specified restaurant defined in terminal {@link Staff}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param tableId
	 * 			the table id to query
	 * @return the detail to this table id
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to query does NOT exist
	 */
	public static Table getByAlias(DBCon dbCon, Staff staff, int tableAlias) throws SQLException, BusinessException{
		List<Table> result = getByCond(dbCon, staff, new ExtraCond().setAliasId(tableAlias), null);
		if(result.isEmpty()){
			throw new BusinessException(TableError.TABLE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the table detail according to table alias of a specified restaurant defined in terminal {@link Staff}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param tableId
	 * 			the table id to query
	 * @return the detail to this table id
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to query does NOT exist
	 */
	public static Table getByAlias(Staff staff, int tableAlias) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByAlias(dbCon, staff, tableAlias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Get the tables according to a specified restaurant defined in {@link Staff} and other condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the table result
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	private static List<Table> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		List<Table> result = new ArrayList<Table>();
		String sql;
		sql = " SELECT " +
			  " REGION.name AS region_name, REGION.region_id, REGION.restaurant_id, " +
			  " TBL.table_id, TBL.table_alias, TBL.name AS tbl_name, TBL.minimum_cost, " +
			  " O.custom_num, O.category, IFNULL(O.status, -1) AS status " + 
			  " FROM " + Params.dbName + ".table TBL " +
			  " LEFT JOIN " + Params.dbName + ".region REGION ON REGION.region_id = TBL.region_id AND REGION.restaurant_id = TBL.restaurant_id " +
			  " LEFT JOIN ( SELECT * FROM " + Params.dbName + ".order WHERE restaurant_id = " + staff.getRestaurantId() + " AND status = " + Order.Status.UNPAID.getVal() + " GROUP BY table_id ) AS O ON O.table_id = TBL.table_id " + 
			  " WHERE 1 = 1 " +
			  " AND TBL.restaurant_id = " + staff.getRestaurantId() + " " +
			  (extraCond != null ? extraCond : "") + " " + 
			  (orderClause != null ? orderClause : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while (dbCon.rs.next()) {
			Table table = new Table();
			Region region = new Region(dbCon.rs.getShort("region_id"));
			region.setName(dbCon.rs.getString("region_name"));
			region.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			table.setRegion(region);
			table.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
			table.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			if(dbCon.rs.getInt("status") == Order.Status.UNPAID.getVal()){
				table.setStatus(Table.Status.BUSY);
				table.setCategory(Order.Category.valueOf(dbCon.rs.getInt("category")));
				table.setCustomNum(dbCon.rs.getInt("custom_num"));
			}else{
				table.setStatus(Table.Status.IDLE);
			}
			table.setTableAlias(dbCon.rs.getInt("table_alias"));
			table.setTableId(dbCon.rs.getInt("table_id"));
			table.setTableName(dbCon.rs.getString("tbl_name"));
			result.add(table);
		}
		dbCon.rs.close();
		
		return result;		
	}
	
	/**
	 * Get the tables according to a specified restaurant defined in {@link Staff} and other condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the table result
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<Table> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the general information to a specified table according to builder {@link Table#UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder the builder update table
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, Table.UpdateBuilder builder) throws SQLException, BusinessException{
		Table tblToUpdate = builder.build();
		
		String updateSQL = " UPDATE " + Params.dbName + ".table SET " +
						   " table_id = " + tblToUpdate.getTableId() +
						   (builder.isRegionChanged() ? " ,region_id = " + tblToUpdate.getRegion().getId() : "")+
						   (builder.isNameChanged() ? " ,name = '" + tblToUpdate.getName() + "'" : "") +
						   (builder.isMiniCostChanged() ? " ,minimum_cost = " + tblToUpdate.getMinimumCost() : "") +
						   " WHERE " +
						   " table_id = " + tblToUpdate.getTableId();
		if(dbCon.stmt.executeUpdate(updateSQL) == 0){
			throw new BusinessException(TableError.TABLE_NOT_EXIST);
		}
	}
	
	/**
	 * Update the general information to a specified table according to builder {@link Table#UpdateBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder the builder update table
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to update does NOT exist
	 */
	public static void update(Staff staff, Table.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new table to a specified restaurant according to insert builder {@link Table#InsertBuilder}.
	 * The alias id to new table must be unique in a restaurant.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insertion builder 
	 * @return the table to insert along with id just generated if insert successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the alias id to new table has been exist before
	 */
	public static int insert(Staff staff, InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new table to a specified restaurant according to insertion builder.
	 * The alias id to new table must be unique in a restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param builder
	 * 			the insertion builder 
	 * @return the table to insert along with id just generated if insert successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the alias id to new table has been exist before
	 */
	public static int insert(DBCon dbCon, Staff staff, InsertBuilder builder) throws SQLException, BusinessException{
		
		Table tblToInsert = builder.build();
		
		String sql;
		
		sql = " SELECT * FROM " + Params.dbName + ".table WHERE restaurant_id = " + staff.getRestaurantId() + " AND table_alias = " + tblToInsert.getAliasId() + " LIMIT 1 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(TableError.DUPLICATED_TABLE_ALIAS);
		}
		dbCon.rs.close();
		sql = " INSERT INTO " + Params.dbName + ".table " +
		 	  "(`table_alias`, `restaurant_id`, `name`, `region_id`, `minimum_cost`) VALUES( " +
			  tblToInsert.getAliasId() + ", " + 
			  staff.getRestaurantId() + ", " +
			  "'" + tblToInsert.getName() + "', " +
			  tblToInsert.getRegion().getId() + ", " +
			  tblToInsert.getMinimumCost() + 
			  " ) ";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		//Get the generated id to this new table. 
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of new table is not generated successfully.");
		}
	}
	
	/**
	 * Insert tables according to specific builder {@link Table#BatchInsertBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder
	 * @return the amount to table inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, Table.BatchInsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert tables according to specific builder {@link Table#BatchInsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder
	 * @return the amount to table inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, Table.BatchInsertBuilder builder) throws SQLException{
		int amount = 0;
		for(Table.InsertBuilder singleBuilder : builder.build()){
			try{
				insert(dbCon, staff, singleBuilder);
				amount++;
			}catch(BusinessException e){
				e.printStackTrace();
			}
		}
		return amount;
	}
	
	/**
	 * Delete a table according to table id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param tableId
	 * 			the table id to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to delete does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int tableId) throws SQLException, BusinessException{
		if(delete(dbCon, staff, new ExtraCond().setId(tableId)) == 0){
			throw new BusinessException(TableError.TABLE_NOT_EXIST);
		}
	}
	
	/**
	 * Delete a table according to table id.
	 * @param staff
	 * 			the terminal
	 * @param tableId
	 * 			the table id to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to delete does NOT exist
	 */
	public static void deleteById(Staff staff, int tableId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, staff, tableId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete a table according to table alias id of a specified restaurant defined in terminal {@link Staff}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param tableAlias
	 * 			the table alias to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to delete does NOT exist
	 */
	public static void deleteByAliasId(DBCon dbCon, Staff staff, int tableAlias) throws SQLException, BusinessException{
		if(delete(dbCon, staff, new ExtraCond().setAliasId(tableAlias)) == 0){
			throw new BusinessException(TableError.TABLE_NOT_EXIST);
		}
	}
	
	/**
	 * Delete a table according to table alias id of a specified restaurant defined in terminal {@link Staff}.
	 * @param staff
	 * 			the terminal
	 * @param tableAlias
	 * 			the table alias to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to delete does NOT exist
	 */
	public static void deleteByAliasId(Staff staff, int tableAlias) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteByAliasId(dbCon, staff, tableAlias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the table according to extra condition of a specified restaurant defined in terminal {@link Staff}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount of tables to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static int delete(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		int amount = 0;
		for(Table table : getByCond(dbCon, staff, extraCond, null)){
			if(table.isBusy()){
				throw new BusinessException("【" + Table.Status.BUSY.getDesc() +	 "】状态的餐厅不能删除", TableError.TABLE_DELETE_NOT_ALLOW);
			}
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".table WHERE table_id = " + table.getTableId();
			dbCon.stmt.executeUpdate(sql);
			amount++;
		}
		return amount;
	}
	
	/**
	 * Delete the table according to extra condition of a specified restaurant defined in terminal {@link Staff}.
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount of tables to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static int delete(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int amount = delete(dbCon, staff, extraCond);
			dbCon.conn.commit();
			return amount;
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the amount of table to a specified restaurant defined in terminal {@link Staff} and extra condition.
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount of table
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int getTableCount(Staff staff, String extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getTableCount(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the amount of table to a specified restaurant defined in terminal {@link Staff} and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount of table
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int getTableCount(DBCon dbCon, Staff staff, String extraCond) throws SQLException{
		String sql;
		sql = " SELECT COUNT(*) FROM " + Params.dbName + ".table TBL" +
			  " WHERE 1 = 1" + 
			  " AND TBL.restaurant_id = " + staff.getRestaurantId() + " " +	
			  (extraCond != null ? extraCond : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			return 0;
		}
	}
}
