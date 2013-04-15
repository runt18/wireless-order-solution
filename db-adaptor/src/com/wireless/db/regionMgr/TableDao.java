package com.wireless.db.regionMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;
import com.wireless.util.SQLUtil;

public class TableDao {

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
		String querySQL = "SELECT * FROM "+Params.dbName+".table A LEFT JOIN "+Params.dbName+".region B ON B.region_id = A.region_id AND B.restaurant_id = A.restaurant_id "+extraCond+" "+orderClause;
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
	 * Update a specified table.
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
	public static void update(DBCon dbCon, Terminal term, Table table) throws SQLException, BusinessException{
		//TODO
		String updateSQL = "UPDATE "+Params.dbName+".table SET "+
				Params.dbName+".table.table_alias = "+table.getTableAlias()+","+
				Params.dbName+".table.restaurant_id = "+table.getRestaurantID()+","+
				Params.dbName+".table.region_id = "+table.getRegion().getId()+","+
				Params.dbName+".table.name = '"+table.getTableName()+"',"+
				Params.dbName+".table.minimum_cost = "+table.getMinimumCost()+","+
				Params.dbName+".table.custom_num = "+table.getCustomNum()+","+
				Params.dbName+".table.category = "+table.getCategory()+","+
				Params.dbName+".table.status = 0,"+
				Params.dbName+".table.service_rate = "+table.getServiceRate()+
				" WHERE "+
				Params.dbName+".table.table_id = "+table.getTableID()+";";
		dbCon.stmt.executeUpdate(updateSQL);
	}
	
	/**
	 * Update a specified table.
	 * @param term
	 * 			the terminal
	 * @param tblToUpdate the table {@link Table} to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the table to update does NOT exist
	 */
	public static void update(Terminal term, Table tblToUpdate) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, term, tblToUpdate);
		}finally{
			dbCon.disconnect();
		}
	}
	
}
