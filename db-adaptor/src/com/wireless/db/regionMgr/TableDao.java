package com.wireless.db.regionMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.regionMgr.Table.InsertBuilder;
import com.wireless.protocol.Terminal;

public class TableDao {

	/**
	 * Get the table detail according to table id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param tableId
	 * 			the table id to query
	 * @return the detail to this table id
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to query does NOT exist
	 */
	public static Table getTableById(DBCon dbCon, Terminal term, int tableId) throws SQLException, BusinessException{
		
		List<Table> result = getTables(dbCon, term, "AND TBL.table_id = " + tableId, null);
		if(result.isEmpty()){
			throw new BusinessException(ProtocolError.TABLE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the table detail according to table id.
	 * @param term
	 * 			the terminal
	 * @param tableId
	 * 			the table id to query
	 * @return the detail to this table id
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to query does NOT exist
	 */
	public static Table getTableById(Terminal term, int tableId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getTableById(dbCon, term, tableId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the table detail according to table alias of a specified restaurant defined in terminal {@link Terminal}.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param tableId
	 * 			the table id to query
	 * @return the detail to this table id
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to query does NOT exist
	 */
	public static Table getTableByAlias(DBCon dbCon, Terminal term, int tableAlias) throws SQLException, BusinessException{
		List<Table> result = getTables(dbCon, term, "AND TBL.table_alias = " + tableAlias, null);
		if(result.isEmpty()){
			throw new BusinessException(ProtocolError.TABLE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the table detail according to table alias of a specified restaurant defined in terminal {@link Terminal}.
	 * @param term
	 * 			the terminal
	 * @param tableId
	 * 			the table id to query
	 * @return the detail to this table id
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to query does NOT exist
	 */
	public static Table getTableByAlias(Terminal term, int tableAlias) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getTableByAlias(term, tableAlias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Get the tables according to a specified restaurant defined in {@link Terminal} and other condition.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the table result
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<Table> getTables(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		List<Table> result = new ArrayList<Table>();
		String querySQL = " SELECT * FROM " + Params.dbName + ".table TBL " +
						  " LEFT JOIN " + Params.dbName + ".region REGION " +
						  " ON REGION.region_id = TBL.region_id AND REGION.restaurant_id = TBL.restaurant_id " +
						  " WHERE 1 = 1 " +
						  " AND TBL.restaurant_id = " + term.restaurantID + " " +
						  (extraCond != null ? extraCond : "") + " " + 
						  (orderClause != null ? orderClause : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		
		while (dbCon.rs.next()) {
			Table table = new Table();
			Region region = new Region();
			region.setId(dbCon.rs.getShort("region_id"));
			region.setName(dbCon.rs.getString("name"));
			region.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			table.setRegion(region);
			table.setCategory(dbCon.rs.getInt("category"));
			table.setCustomNum(dbCon.rs.getInt("custom_num"));
			table.setMimnmuCost(dbCon.rs.getFloat("minimum_cost"));
			table.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			table.setServiceRate(dbCon.rs.getFloat("service_rate"));
			table.setStatus(dbCon.rs.getInt("status"));
			table.setTableAlias(dbCon.rs.getInt("table_alias"));
			table.setTableId(dbCon.rs.getInt("table_id"));
			table.setTableName(dbCon.rs.getString("name"));
			result.add(table);
		}
		dbCon.rs.close();
		
		return result;		
	}
	
	/**
	 * Get the tables according to a specified restaurant defined in {@link Terminal} and other condition.
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the table result
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<Table> getTables(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getTables(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the general information to a specified table according to table id defined in table {@link Table}.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param tblToUpdate the table {@link Table} to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to update does NOT exist
	 */
	public static void updateById(DBCon dbCon, Terminal term, Table tblToUpdate) throws SQLException, BusinessException{
		String updateSQL = " UPDATE " + Params.dbName + ".table SET " +
						   " region_id = " + tblToUpdate.getRegion().getId() + "," +
						   " name = '" + tblToUpdate.getTableName() + "'," +
						   " minimum_cost = " + tblToUpdate.getMinimumCost() + "," +
						   " service_rate = " + tblToUpdate.getServiceRate() +
						   " WHERE " +
						   " table_id = " + tblToUpdate.getTableId();
		int rowAffected = dbCon.stmt.executeUpdate(updateSQL);
		if(rowAffected != 1){
			throw new BusinessException(ProtocolError.TABLE_NOT_EXIST);
		}
	}
	
	/**
	 * Update the general information to a specified table according to table id defined in table {@link Table}.
	 * @param term
	 * 			the terminal
	 * @param tblToUpdate the table {@link Table} to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to update does NOT exist
	 */
	public static void updateById(Terminal term, Table tblToUpdate) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			updateById(dbCon, term, tblToUpdate);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new table to a specified restaurant.
	 * The alias id to new table must be unique in a restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param tblToInsert
	 * 			the table to insert
	 * @return the id to table just created
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the alias id to new table has been exist before
	 */
	private static int insert(DBCon dbCon, Terminal term, Table tblToInsert) throws SQLException, BusinessException{
		
		String sql;
		
		sql = " SELECT * FROM " + Params.dbName + ".table " +
			  " WHERE " +
			  " restaurant_id = " + term.restaurantID + 
			  " AND " + 
			  " table_alias = " + tblToInsert.getTableAlias();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(ProtocolError.TABLE_EXIST);
		}
		dbCon.rs.close();
		
		sql = " INSERT INTO " + Params.dbName + ".table " +
		 	  "(`table_alias`, `restaurant_id`, `name`, `region_id`, `minimum_cost`, `service_rate`) VALUES( " +
			  tblToInsert.getTableAlias() + ", " + 
			  tblToInsert.getRestaurantId() + ", " +
			  "'" + tblToInsert.getTableName() + "', " +
			  tblToInsert.getRegion().getId() + ", "	+
			  tblToInsert.getMinimumCost() + "," +
			  tblToInsert.getServiceRate() + " ) ";
		
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
	 * Insert a new table to a specified restaurant according to insertion builder.
	 * The alias id to new table must be unique in a restaurant.
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the insertion builder 
	 * @return the table to insert along with id just generated if insert successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the alias id to new table has been exist before
	 */
	public static Table insert(Terminal term, InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, term, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new table to a specified restaurant according to insertion builder.
	 * The alias id to new table must be unique in a restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the insertion builder 
	 * @return the table to insert along with id just generated if insert successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the alias id to new table has been exist before
	 */
	public static Table insert(DBCon dbCon, Terminal term, InsertBuilder builder) throws SQLException, BusinessException{
		Table tblToInsert = builder.build();
		tblToInsert.setTableId(insert(dbCon, term, tblToInsert));
		return tblToInsert;
	}
	
	/**
	 * Delete a table according to table id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param tableId
	 * 			the table id to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to delete does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Terminal term, int tableId) throws SQLException, BusinessException{
		if(delete(dbCon, " AND TBL.table_id = " + tableId) == 0){
			throw new BusinessException("删除的餐台不存在");
		}
	}
	
	/**
	 * Delete a table according to table id.
	 * @param term
	 * 			the terminal
	 * @param tableId
	 * 			the table id to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to delete does NOT exist
	 */
	public static void deleteById(Terminal term, int tableId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, term, tableId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete a table according to table alias id of a specified restaurant defined in terminal {@link Terminal}.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param tableAlias
	 * 			the table alias to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to delete does NOT exist
	 */
	public static void deleteByAliasId(DBCon dbCon, Terminal term, int tableAlias) throws SQLException, BusinessException{
		if(delete(dbCon, " AND TBL.restaurant_id = " + term.restaurantID + " AND TBL.table_alias = " + tableAlias) == 0){
			throw new BusinessException("删除的餐台不存在");
		}
	}
	
	/**
	 * Delete a table according to table alias id of a specified restaurant defined in terminal {@link Terminal}.
	 * @param term
	 * 			the terminal
	 * @param tableAlias
	 * 			the table alias to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to delete does NOT exist
	 */
	public static void deleteByAliasId(Terminal term, int tableAlias) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteByAliasId(dbCon, term, tableAlias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the table according to extra condition of a specified restaurant defined in terminal {@link Terminal}.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount of tables to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int delete(DBCon dbCon, Terminal term, String extraCond) throws SQLException{
		return delete(dbCon, " AND TBL.restaurant_id = " + term.restaurantID + (extraCond != null ? extraCond : ""));
	}
	
	/**
	 * Delete the table according to extra condition of a specified restaurant defined in terminal {@link Terminal}.
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount of tables to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int delete(Terminal term, String extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return delete(dbCon, term, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the table according to extra condition.
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount of tables to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	private static int delete(DBCon dbCon, String extraCond) throws SQLException{
		String sql;
		sql = " DELETE TBL FROM " + Params.dbName + ".table AS TBL " +
			  " WHERE 1 = 1 " +
			  (extraCond != null ? extraCond : "");
		return dbCon.stmt.executeUpdate(sql);
	}
}
