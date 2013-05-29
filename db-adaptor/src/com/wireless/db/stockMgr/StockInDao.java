package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.stockMgr.StockIn;
import com.wireless.pojo.stockMgr.StockIn.InsertBuilder;
import com.wireless.protocol.Terminal;

public class StockInDao {

	/**
	 * Insert a new stock table.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the stockIn builder to insert
	 * @return	the id to table just created
	 * @throws SQLException
	 * 			if failed to execute any SQL statement 
	 */
	public static int insertStockIn(DBCon dbCon, InsertBuilder builder) throws SQLException{
		StockIn stockIn = builder.build();
		String sql ;
		sql = "INSERT INTO " + Params.dbName + ".stock_in (restaurant_id, birth_date, " +
				"ori_stock_id, ori_stock_date, dept_in, dept_out, supplier_id, operator_id, operator, operate_date, amount, price, type, sub_type, status, comment) "+
				" VALUES( " +
				+ stockIn.getRestaurantId() + ", "
				+ stockIn.getBirthDate() + ", "
				+ "'" + stockIn.getOriStockId() + "', "
				+ stockIn.getOriStockIdDate() +
				+ stockIn.getDeptIn().getId() + ", "
				+ stockIn.getDeptOut().getId() + ", "
				+ stockIn.getOperatorId() + ", "
				+ "'" + stockIn.getOperator() + "', "
				+ stockIn.getTotalAmount() + ", "
				+ stockIn.getTotalPrice() + ", "
				+ stockIn.getType().getVal() + ", " 
				+ stockIn.getSubType().getVal() + ", "
				+ stockIn.getStatus().getVal() + ", "
				+ "'" + stockIn.getComment() + "'" 
				+ ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id is not generated successfully");
		}
				
	}
	/**
	 * Insert a new stock table.
	 * @param builder
	 * 			the stockIn builder to insert
	 * @return	the id to table just created
	 * @throws SQLException
	 * 			if failed to execute any SQL statement 
	 */	
	public static int insertStockIn(InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insertStockIn(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
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
		String sql;
		sql = "DELETE FROM " + Params.dbName + ".stock_in " +
				" WHERE 1=1 " +
				(extraCond == null ? "" : extraCond);
		return dbCon.stmt.executeUpdate(sql);
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
	public static void deleteStockInById(Terminal term, int stockInId) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteStockInById(dbCon, term, stockInId);
		}finally{
			dbCon.disconnect();
		}
	}
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
	public static void deleteStockInById(DBCon dbCon, Terminal term, int stockInId) throws BusinessException, SQLException{
		if(deleteStockIn(dbCon, " AND restautant_id = " + term.restaurantID + " AND id = " + stockInId) == 0){
			throw new BusinessException("此库单不存在!!");
		};
	}
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
	 * @param SQLException
	 * 			if failed to execute any SQL statement
	 * @return	the list holding the stockIn result if successfully
	 */
	public static List<StockIn> getStockIns(Terminal term, String extraCond, String orderClause) throws SQLException{
		
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
	 * @param SQLException
	 * 			if failed to execute any 
	 * @return	the list holding the stockIn result if successfully
	 */
	public static List<StockIn> getStockIns(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		
		return null;
	}
	/**
	 * Select stockIn according to terminal and stockIn_id
	 * @param term
	 * 			the Terminal 
	 * @param stockInId
	 * 			the id of stockIn 
	 * @param SQLException 
	 * 			if failed to execute any SQL statement
	 * @param BusinessException
	 * 			if the stockIn to query does not exist
	 * @return	the detail to this StockIn_id
	 */
	public static StockIn getStockInById(Terminal term, int stockInId) throws SQLException, BusinessException{
		return null;
	}
	/**
	 * Select stockIn according to terminal and stockIn_id
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the Terminal
	 * @param stockInId
	 * 			the id of stockIn
	 * @return	the detail to this StockIn_id
	 */
	public static StockIn getStockInById(DBCon dbCon, Terminal term, int stockInId) throws SQLException, BusinessException{
		return null;
	}
	
	
}
