package com.wireless.db.regionMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.protocol.Terminal;

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
		
		//TODO
		
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
	public static void update(DBCon dbCon, Terminal term, Table tblToUpdate) throws SQLException, BusinessException{
		//TODO
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
