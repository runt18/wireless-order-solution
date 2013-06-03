package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.stockMgr.StockTake;
import com.wireless.pojo.stockMgr.StockTake.InsertBuilder;
import com.wireless.pojo.stockMgr.StockTake.UpdateBuilder;
import com.wireless.protocol.Terminal;

public class StockTakeDao {
	/**
	 * Insert a new stockTake.
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the stockTake builder to insert
	 * @return	the id of stockTake just created
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int insertStockTake(Terminal term, InsertBuilder builder) throws SQLException{
		return 0;
	}
	/**
	 * Insert a new stockTake.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the stockTake builder to insert
	 * @return	the id of stockTake just created
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int insertStockTake(DBCon dbCon, Terminal term, InsertBuilder builder) throws SQLException{
		return 0;
	}
	/**
	 * Get the list of stockTake according to extra condition.
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return  the list holding the stockTake result if successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<StockTake> getStockTakes(Terminal term, String extraCond, String orderClause) throws SQLException{
		return null;
	}
	/**
	 * Get the list of stockTake according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<StockTake> getStockTakes(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		return null;
	}
	/**
	 * Get the list of stockTake according to id of stockTake.
	 * @param term
	 * 			the terminal
	 * @param id
	 * 			the id of stockTake
	 * @return	the detail of stockTake
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTake is not exist
	 */
	public static StockTake getStockTakeById(Terminal term, int id) throws SQLException,BusinessException {
		return null;
	}
	/**
	 * Get the list of stockTake according to id of stockTake.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param id
	 * 			the id of stockTake
	 * @return	the detail of stockTake
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTake is not exist
	 */
	public static StockTake getStockTakeById(DBCon dbCon, Terminal term, int id) throws SQLException,BusinessException{
		return null;
	}
	/**
	 * Update stockTake according to UpdateBuilder.
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the stockTake to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTake is not exist
	 */
	public static void updateStockTake(Terminal term, UpdateBuilder builder) throws SQLException,BusinessException{}
	/**
	 * Update stockTake according to UpdateBuilder.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the stockTake to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTake is not exist
	 */
	public static void updateStockTake(DBCon dbCon, Terminal term, UpdateBuilder builder) throws SQLException,BusinessException{}
	/**
	 * Delete stockTake by id
	 * @param term
	 * 			the terminal
	 * @param id
	 * 			the id of the stockTake
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTake is not exist
	 */
	public static void deleteStockTake(Terminal term, int id) throws SQLException,BusinessException{}
	/**
	 * Delete stockTake by id
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param id
	 * 			the id of the stockTake
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTake is not exist
	 */
	public static void deleteStockTake(DBCon dbCon, Terminal term, int id) throws SQLException,BusinessException{}
	
	
}
