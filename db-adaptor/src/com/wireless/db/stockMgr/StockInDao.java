package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.stockMgr.StockIn;
import com.wireless.pojo.stockMgr.StockIn.InsertBuilder;
import com.wireless.pojo.stockMgr.StockIn.Status;
import com.wireless.pojo.stockMgr.StockInDetail;
import com.wireless.pojo.util.DateUtil;
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
		String selectDeptIn = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDeptIn().getId();
		
		dbCon.rs = dbCon.stmt.executeQuery(selectDeptIn);
		dbCon.rs.next();
		String deptInName = dbCon.rs.getString(1);
		String selectDeptOut = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDeptOut().getId();
		dbCon.rs = dbCon.stmt.executeQuery(selectDeptOut);
		dbCon.rs.next();
		String deptOutName = dbCon.rs.getString(1);
		
		int stockId = 0;
		try{
			dbCon.conn.setAutoCommit(false);
			String insertsql = "INSERT INTO " + Params.dbName + ".stock_in (restaurant_id, birth_date, " +
					"ori_stock_id, ori_stock_date, dept_in, dept_in_name, dept_out, dept_out_name, supplier_id, supplier_name, operator_id, operator, amount, price, type, sub_type, status, comment) "+
					" VALUES( " +
					+ stockIn.getRestaurantId() + ", "
					+ "'" + DateUtil.format(stockIn.getBirthDate()) + "', "
					//+ 20190909 + ","
					+ "'" + stockIn.getOriStockId() + "', "
					+ "'" + DateUtil.format(stockIn.getOriStockIdDate()) + "', "
					+ stockIn.getDeptIn().getId() + ", "
					+ "'" + deptInName + "', " 
					+ stockIn.getDeptOut().getId() + ", "
					+ "'" + deptOutName + "', "
					+ stockIn.getSupplier().getSupplierId() + ", "
					+ "'" + stockIn.getSupplier().getName() + "', "
					+ stockIn.getOperatorId() + ", "
					+ "'" + stockIn.getOperator() + "', "
					+ stockIn.getTotalAmount() + ", "
					+ stockIn.getTotalPrice() + ", "
					+ stockIn.getType().getVal() + ", " 
					+ stockIn.getSubType().getVal() + ", "
					+ stockIn.getStatus().getVal() + ", "
					+ "'" + stockIn.getComment() + "'" 
					+ ")";
			dbCon.stmt.executeUpdate(insertsql, Statement.RETURN_GENERATED_KEYS);
			dbCon.rs = dbCon.stmt.getGeneratedKeys();
			
			if(dbCon.rs.next()){
				stockId = dbCon.rs.getInt(1);
				for (StockInDetail sDetail : stockIn.getStockDetails()) {
					System.out.println("stock"+stockId);
					sDetail.setStockInId(stockId);
					StockInDetailDao.InsertStockInDetail(dbCon, sDetail);
				}			
			}
			dbCon.conn.commit();
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw new SQLException("The id is not generated successfully");
		}
		System.out.println("return"+stockId);
		return stockId;
		
		
		

		
		
				
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
		if(deleteStockIn(dbCon, " AND restaurant_id = " + term.restaurantID + " AND id = " + stockInId) == 0){
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
	public static void updateStockIn(Terminal term, StockIn stockIn) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			updateStockIn(dbCon, term, stockIn);
		}finally{
			dbCon.disconnect();
		}
		
	}
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
	public static void updateStockIn(DBCon dbCon, Terminal term, StockIn stockIn) throws SQLException, BusinessException{
		//StockIn stockIn = uBuilder.build();
		String sql;
		sql = "UPDATE " + Params.dbName + ".stock_in SET " +
				" approver_id = " + stockIn.getApproverId() + ", " +
				" approver = '" + stockIn.getApprover() + "'," +
				" approve_date = " + stockIn.getApproverDate() + ", " +
				" amount = " + stockIn.getTotalAmount() + ", " +
				" price = " + stockIn.getTotalPrice() + ", " +
				" status = " + stockIn.getStatus().getVal() +
				" WHERE id = " + stockIn.getId() + 
				" AND restaurant_id = " + stockIn.getRestaurantId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException("不能通过审核,此库单不存在");
		}else{
			if(stockIn.getStatus() == Status.AUDIT){
				
			}
		}
		
		
		
				
	}
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
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockIns(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
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
		List<StockIn> stockIns = new ArrayList<StockIn>();
		String sql;
		sql = "SELECT " +
				" id, restaurant_id, birth_date, ori_stock_id, ori_stock_date, dept_in, dept_in_name, dept_out, dept_out_name, supplier_id, supplier_name," +
				" operator_id, operator, amount, price, type, sub_type, status, comment " +
				" FROM " + Params.dbName +".stock_in " +
				" WHERE restaurant_id = " + term.restaurantID +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			StockIn stockIn = new StockIn();
			stockIn.setId(dbCon.rs.getInt("id"));
			stockIn.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			stockIn.setBirthDate(dbCon.rs.getLong("birth_date"));
			stockIn.setOriStockId(dbCon.rs.getString("ori_stock_id"));
			stockIn.setOriStockIdDate(dbCon.rs.getTimestamp("ori_stock_date").getTime());
			stockIn.getDeptIn().setId(dbCon.rs.getShort("dept_in"));
			stockIn.getDeptIn().setName(dbCon.rs.getString("dept_in_name"));
			stockIn.getDeptOut().setId(dbCon.rs.getShort("dept_out"));
			stockIn.getDeptOut().setName(dbCon.rs.getString("dept_out_name"));
			stockIn.getSupplier().setSupplierid(dbCon.rs.getInt("supplier_id"));
			stockIn.getSupplier().setName(dbCon.rs.getString("supplier_name"));
			stockIn.setOperatorId(dbCon.rs.getInt("operator_id"));
			stockIn.setOperator(dbCon.rs.getString("operator"));
			stockIn.setAmount(dbCon.rs.getFloat("amount"));
			stockIn.setPrice(dbCon.rs.getFloat("price"));
			stockIn.setType(dbCon.rs.getInt("type"));
			stockIn.setSubType(dbCon.rs.getInt("sub_type"));
			stockIn.setStatus(dbCon.rs.getInt("status"));
			stockIn.setComment(dbCon.rs.getString("comment"));
			stockIns.add(stockIn);
		}
		
		dbCon.rs.close();
		return stockIns;
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
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockInById(dbCon, term, stockInId);
		}finally{
			dbCon.disconnect();
		}
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
		List<StockIn> stockIns = getStockIns(dbCon, term, " AND id = " + stockInId, null);
		if(stockIns.isEmpty()){
			throw new BusinessException("没有此库单");
		}else{
			return stockIns.get(0);
		}
		
	}
	
	
}
