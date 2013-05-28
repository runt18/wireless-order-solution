package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.stockMgr.StockIn;
import com.wireless.protocol.Terminal;

public class StockInDao {

	/**
	 * Insert a new stock table.
	 * @param dbCon
	 * 			the database connection
	 * @param stockIn
	 * 			the stockIn to insert
	 * @return	the id to table just created
	 * @throws SQLException
	 * 			if failed to execute any SQL statement 
	 */
	public static int insertStockIn(DBCon dbCon, StockIn stockIn) throws SQLException{
		
		return 0;
	}
	/**
	 * Insert a new stock table.
	 * @param stockIn
	 * 			the stockIn to insert
	 * @return	the id to table just created
	 * @throws SQLException
	 * 			if failed to execute any SQL statement 
	 */	
	public static int insertStockIn(StockIn stockIn) throws SQLException{
		
		return 0;
	}
	/**
	 * Delete the stockIn according to extra condition of a specified restaurant defined in terminal.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @return	the amount of stockIns to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */	
	public static int deleteStockIn(DBCon dbCon, String extraCond) throws SQLException{
		
		return 0;
	}
	/**
	 * Delete the stockIn according to extra condition of a specified restaurant defined in terminal.
	 * @param term
	 * 			the Terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return	the amount of stockIns to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockIn_id is not exist
	 */
	public static int deleteStockIn(Terminal term, String extraCond) throws SQLException{
		
		return 0;
	}
	/**
	 * Delete the stockIn according to extra condition of a specified restaurant defined in terminal.
	 * @param dbCon
	 * 			the database connection 
	 * @param term
	 * 			the terminal 
	 * @param extraCond
	 * 			the extra condition
	 * @return	the amount of stockIns to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int deleteStockIn(DBCon dbCon, Terminal term, String extraCond) throws SQLException{
		return 0;
	}
	/**
	 * Delete the stockIn according to stockIn_id.
	 * @param term
	 * 			the terminal
	 * @param stockInId
	 * 			the stockIn_id of stockIn
	 * @throws BusinessException
	 * 			if the stockIn_id is not exist
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static void deleteStockInById(Terminal term, int stockInId) throws BusinessException, SQLException{}
	/**
	 * Delete the stockIn according to stockIn_id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param stockInId
	 * 			the stockIn_id of stockIn
	 * @throws BusinessException
	 * 			if the stockIn_id is not exist
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static void deleteStockInById(DBCon dbCon, Terminal term, int stockInId) throws BusinessException, SQLException{}
	/**
	 * Update stockIn according to stockIn and terminal.
	 * @param term
	 * 			the terminal
	 * @param stockIn
	 * 			the stockIn to update
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement
	 * @throws BusinessException
	 * 			if the table to update does not exist
	 */
	public static void updateStockIn(Terminal term, StockIn stockIn) throws SQLException, BusinessException{}
	/**
	 * Update stockIn according to stockIn and terminal.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param stockIn
	 * 			the stockIn to update
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement
	 * @throws BusinessException
	 * 			if the table to update does not exist
	 */
	public static void updateStockIn(DBCon dbCon, Terminal term, StockIn stockIn) throws SQLException, BusinessException{}
	/**
	 * Select stockIn according to terminal and extra condition.
	 * @param term
	 * 			the terminal 
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return	the list holding the stockIn result if successfully
	 */
	public static List<StockIn> getStockIns(Terminal term, String extraCond, String orderClause){
		
		return null;
	}
	/**
	 * Select stockIn according to terminal and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal 
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return	the list holding the stockIn result if successfully
	 */
	public static List<StockIn> getStockIns(DBCon dbCon, Terminal term, String extraCond, String orderClause){
		
		return null;
	}
	
	
}
