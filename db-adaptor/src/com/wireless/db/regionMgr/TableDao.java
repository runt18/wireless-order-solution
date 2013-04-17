package com.wireless.db.regionMgr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
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
		String sql = "SELECT * FROM "+Params.dbName+".table LEFT JOIN "+Params.dbName+".region ON "+Params.dbName+".region.region_id = "+Params.dbName+".table.region_id AND "+Params.dbName+".region.restaurant_id = "+Params.dbName+".table.restaurant_id AND "+Params.dbName+".table.table_id = "+tableId;
		ResultSet rs = dbCon.stmt.executeQuery(sql);
		Table table = new Table();
		while(rs.next()){
			Region region = new Region();
			region.setRestaurantId(rs.getInt("restaurant_id"));
			region.setName(rs.getString("name"));
			region.setId(rs.getShort("region_id"));
			table.setCategory(rs.getInt("category"));
			table.setCustomNum(rs.getInt("custom_num"));
			table.setMimnmuCost(rs.getFloat("minimum_cost"));
			table.setRegion(region);
			table.setRestaurantID(rs.getInt("restaurant_id"));
			table.setServiceRate(rs.getFloat("service_rate"));
			table.setStatus(rs.getInt("status"));
			table.setTableAlias(rs.getInt("table_alias"));
			table.setTableID(rs.getInt("table_id"));
			table.setTableName(rs.getString("name"));
			break;
		}
		return table;
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
		String sql = "SELECT * FROM "+Params.dbName+".table LEFT JOIN "+Params.dbName+".region ON "+Params.dbName+".region.region_id = "+Params.dbName+".table.region_id AND "+Params.dbName+".region.restaurant_id = "+Params.dbName+".table.restaurant_id AND "+Params.dbName+".table.table_alias = "+tableAlias;
		ResultSet rs = dbCon.stmt.executeQuery(sql);
		Table table = new Table();
		while(rs.next()){
			Region region = new Region();
			region.setRestaurantId(rs.getInt("restaurant_id"));
			region.setName(rs.getString("name"));
			region.setId(rs.getShort("region_id"));
			table.setCategory(rs.getInt("category"));
			table.setCustomNum(rs.getInt("custom_num"));
			table.setMimnmuCost(rs.getFloat("minimum_cost"));
			table.setRegion(region);
			table.setRestaurantID(rs.getInt("restaurant_id"));
			table.setServiceRate(rs.getFloat("service_rate"));
			table.setStatus(rs.getInt("status"));
			table.setTableAlias(rs.getInt("table_alias"));
			table.setTableID(rs.getInt("table_id"));
			table.setTableName(rs.getString("name"));
			break;
		}
		return table;
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
						  " AND TBL.restaurant_id = " + term.restaurantID +
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
			table.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			table.setServiceRate(dbCon.rs.getFloat("service_rate"));
			table.setStatus(dbCon.rs.getInt("status"));
			table.setTableAlias(dbCon.rs.getInt("table_alias"));
			table.setTableID(dbCon.rs.getInt("table_id"));
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
						   " table_id = " + tblToUpdate.getTableID();
		dbCon.stmt.executeUpdate(updateSQL);
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
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the alias id to new table has been exist before
	 */
	public static void insert(DBCon dbCon, Terminal term, Table tblToInsert) throws SQLException, BusinessException{
		String sql = "INSERT INTO "+Params.dbName+".table(table_id,table_alias,restaurant_id,region_id,NAME,minimum_cost,enabled,custom_num,category,STATUS,service_rate) VALUES(0,"+tblToInsert.getTableAlias()+","+tblToInsert.getRestaurantID()+","+tblToInsert.getRegion().getId()+",'"+tblToInsert.getTableName()+"',"+tblToInsert.getMinimumCost()+","+tblToInsert.getStatus()+","+tblToInsert.getCustomNum()+","+tblToInsert.getCategory()+","+tblToInsert.getStatus()+","+tblToInsert.getServiceRate()+");";
		//String sql_2 = "INSERT INTO "+Params.dbName+".region(restaurant_id,region_id,NAME) VALUES ("+tblToInsert.getRestaurantID()+","+tblToInsert.getRegion().getId()+",'"+tblToInsert.getRegion().getName()+"');";
		dbCon.stmt.executeUpdate(sql);
		//dbCon.stmt.executeUpdate(sql_2);
	}
	
	/**
	 * Insert a new table to a specified restaurant.
	 * The alias id to new table must be unique in a restaurant.
	 * @param term
	 * 			the terminal
	 * @param tblToInsert
	 * 			the table to insert
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the alias id to new table has been exist before
	 */
	public static void insert(Terminal term, Table tblToInsert) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			insert(dbCon, term, tblToInsert);
		}finally{
			dbCon.disconnect();
		}
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
		sql = " DELETE FROM " + Params.dbName + ".table TBL " +
			  " WHERE 1 = 1 " +
			  (extraCond != null ? extraCond : "");
		return dbCon.stmt.executeUpdate(sql);
	}
}
