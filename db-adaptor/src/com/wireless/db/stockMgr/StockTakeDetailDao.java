package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.stockMgr.StockTakeDetail;
import com.wireless.pojo.stockMgr.StockTakeDetail.InsertStockTakeDetail;
import com.wireless.protocol.Terminal;

public class StockTakeDetailDao {

	/**
	 * Insert a new stockTakeDetail.
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the stockTakeDetail builder to insert
	 * @return	the id of stockTake just created
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int insertstockTakeDetail(Terminal term, InsertStockTakeDetail builder) throws SQLException{
		return 0;
	}
	/**
	 * Insert a new stockTakeDetail.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the stockTakeDetail builder to insert
	 * @return	the id of stockTakeDetail just created
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int insertstockTakeDetail(DBCon dbCon, Terminal term, InsertStockTakeDetail builder) throws SQLException{
		return 0;
	}
	/**
	 * Get the list of stockTakeDetail according to extra condition.
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return  the list holding the stockTakeDetail result if successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<StockTakeDetail> getstockTakeDetails(Terminal term, String extraCond, String orderClause) throws SQLException{
		return null;
	}
	/**
	 * Get the list of stockTakeDetail according to extra condition.
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
	public static List<StockTakeDetail> getstockTakeDetails(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		return null;
	}
	/**
	 * Get the list of stockTakeDetail according to id of stockTakeDetail.
	 * @param term
	 * 			the terminal
	 * @param id
	 * 			the id of stockTakeDetail
	 * @return	the detail of stockTakeDetail
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTakeDetail is not exist
	 */
	public static StockTakeDetail getstockTakeDetailById(Terminal term, int id) throws SQLException,BusinessException {
		return null;
	}
	/**
	 * Get the list of stockTakeDetail according to id of stockTakeDetail.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param id
	 * 			the id of stockTakeDetail
	 * @return	the detail of stockTakeDetail
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTakeDetail is not exist
	 */
	public static StockTakeDetail getstockTakeDetailById(DBCon dbCon, Terminal term, int id) throws SQLException,BusinessException{
		return null;
	}
	/**
	 * Update stockTakeDetail according to UpdateBuilder.
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the stockTakeDetail to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTakeDetail is not exist
	 */
	public static void updatestockTakeDetail(Terminal term, StockTakeDetail tDetail) throws SQLException,BusinessException{}
	/**
	 * Update stockTakeDetail according to UpdateBuilder.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the stockTakeDetail to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTakeDetail is not exist
	 */
	public static void updatestockTakeDetail(DBCon dbCon, Terminal term, StockTakeDetail tDetail) throws SQLException,BusinessException{}
	/**
	 * Delete stockTakeDetail by id
	 * @param term
	 * 			the terminal
	 * @param id
	 * 			the id of the stockTakeDetail
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTakeDetail is not exist
	 */
	public static void deletestockTakeDetail(Terminal term, int id) throws SQLException,BusinessException{}
	/**
	 * Delete stockTakeDetail by id
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param id
	 * 			the id of the stockTakeDetail
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTakeDetail is not exist
	 */
	public static void deletestockTakeDetail(DBCon dbCon, Terminal term, int id) throws SQLException,BusinessException{}

}
