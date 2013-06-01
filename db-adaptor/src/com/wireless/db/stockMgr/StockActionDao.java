package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.InsertBuilder;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.util.DateUtil;
import com.wireless.protocol.Terminal;

public class StockActionDao {

	/**
	 * Insert a new stock.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the stockIn builder to insert
	 * @return	the id to stock just created
	 * @throws SQLException
	 * 			if failed to execute any SQL statement 
	 */
	public static int insertStockIn(DBCon dbCon,Terminal term, InsertBuilder builder) throws SQLException{
		StockAction stockIn = builder.build();
		String deptInName = "";
		String deptOutName = "";
		String SupplierName = "";
		
		String selectDeptIn = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDeptIn().getId() + " AND restaurant_id = " +term.restaurantID;		
		dbCon.rs = dbCon.stmt.executeQuery(selectDeptIn);
		if(dbCon.rs.next()){
			deptInName = dbCon.rs.getString(1);
		}
	
		String selectDeptOut = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDeptOut().getId() + " AND restaurant_id = " +term.restaurantID;
		dbCon.rs = dbCon.stmt.executeQuery(selectDeptOut);
		if(dbCon.rs.next()){
			deptOutName = dbCon.rs.getString(1);
		}
		
		String selectSupplierName = "SELECT name FROM " + Params.dbName + ".supplier WHERE supplier_id = " + builder.getSupplier().getSupplierId();
		dbCon.rs = dbCon.stmt.executeQuery(selectSupplierName);
		if(dbCon.rs.next()){
			SupplierName = dbCon.rs.getString(1);
		}		
		
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
					+ "'" + SupplierName + "', "
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
				for (StockActionDetail sDetail : stockIn.getStockDetails()) {
					sDetail.setStockInId(stockId);
					StockActionDetailDao.insertStockInDetail(dbCon, sDetail);
				}			
			}
			dbCon.conn.commit();
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw new SQLException("The id is not generated successfully");
		}finally{
			dbCon.conn.setAutoCommit(true);
		}
		return stockId;
		
		
		

		
		
				
	}
	/**
	 * Insert a new stock.
	 * @param builder
	 * 			the stockIn builder to insert
	 * @return	the id to stock just created
	 * @throws SQLException
	 * 			if failed to execute any SQL statement 
	 */	
	public static int insertStockIn(Terminal term, InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insertStockIn(dbCon,term, builder);
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
	 * 			if the stock to update does not exist
	 */
	public static void updateStockIn(Terminal term, StockAction stockIn) throws SQLException, BusinessException{
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
	 * 			if the stock to update does not exist
	 */
	public static void updateStockIn(DBCon dbCon, Terminal term, StockAction stockIn) throws SQLException, BusinessException{
		//StockIn stockIn = uBuilder.build();
		String sql;
		sql = "UPDATE " + Params.dbName + ".stock_in SET " +
				" approver_id = " + stockIn.getApproverId() + ", " +
				" approver = '" + stockIn.getApprover() + "'," +
				" approve_date = " + "'" +DateUtil.format(stockIn.getApproverDate()) + "', " +
				" amount = " + stockIn.getTotalAmount() + ", " +
				" price = " + stockIn.getTotalPrice() + ", " +
				" status = " + stockIn.getStatus().getVal() +
				" WHERE id = " + stockIn.getId() + 
				" AND restaurant_id = " + stockIn.getRestaurantId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException("不能通过审核,此库单不存在");
		}/*else{
			if(stockIn.getStatus() == Status.AUDIT){
				
			}
		}*/
	}
	
	/**
	 * Only to get the stock according to id.
	 * @param term
	 * 			the Terminal
	 * @param stockInId
	 * 			the id of stock
	 * @return	the detail of stock
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if this stock is not exist
	 */
	public static StockAction getStockInById(Terminal term, int stockInId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockInById(dbCon, term, stockInId);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Only to get the stock according to id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the Terminal
	 * @param stockInId
	 * 			the id of stock
	 * @return	the detail of stock
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if this stock is not exist
	 */
	public static StockAction getStockInById(DBCon dbCon, Terminal term, int stockInId) throws SQLException, BusinessException{
		List<StockAction> stockIns = getStockIns(dbCon, term, " AND id = " + stockInId, null);
		if(stockIns.isEmpty()){
			throw new BusinessException("没有此库单");
		}else{
			return stockIns.get(0);
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
	public static List<StockAction> getStockIns(Terminal term, String extraCond, String orderClause) throws SQLException{
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
	public static List<StockAction> getStockIns(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		List<StockAction> stockIns = new ArrayList<StockAction>();
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
			StockAction stockIn = new StockAction();
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
	 * Get the stock and stockDetail according to terminal and stockIn_id.
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
	public static StockAction getStockAndDetailById(Terminal term, int stockInId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockAndDetailById(dbCon, term, stockInId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Get the stock and stockDetail according to terminal and stockIn_id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the Terminal
	 * @param stockInId
	 * 			the id of stockIn
	 * @return	the detail to this StockIn_id
	 * @param SQLException 
	 * 			if failed to execute any SQL statement
	 * @param BusinessException
	 * 			if the stockIn to query does not exist
	 */
	public static StockAction getStockAndDetailById(DBCon dbCon, Terminal term, int stockInId) throws SQLException, BusinessException{
		List<StockAction> stockIns = getStockAndDetail(dbCon, term, " AND s.id = " + stockInId, null);
		if(stockIns.isEmpty()){
			throw new BusinessException("没有此库单");
		}else{
			return stockIns.get(0);
		}
		
	}
	/**
	 * Get the list of stockIn and stockDetail according to extraCond.
	 * @param term
	 * 			the Terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the stockIn result if successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<StockAction> getStockAndDetail(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockIns(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Get the list of stockIn and stockDetail according to extraCond.
	 * @param dbCon
	 * 			the database connection 
	 * @param term
	 * 			the Terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return	the list holding the stockIn result if successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<StockAction> getStockAndDetail(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		List<StockAction> stockIns = new ArrayList<StockAction>();
		StockAction stockIn = new StockAction();
		String sql;
		sql = "SELECT " +
				" s.id, s.restaurant_id, s.birth_date, s.ori_stock_id, s.ori_stock_date, s.dept_in, s.dept_in_name, s.dept_out, s.dept_out_name, s.supplier_id, s.supplier_name," +
				" s.operator_id, s.operator, s.amount, s.price, s.type, s.sub_type, s.status, s.comment, d.id, d.stock_in_id, d.material_id, d.name, d.price, d.amount " +
				" FROM " + Params.dbName +".stock_in as s " +
				" INNER JOIN " + Params.dbName + ".stock_in_detail as d " +
				" ON s.id = d.stock_in_id" +
				" WHERE s.restaurant_id = " + term.restaurantID +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			StockActionDetail sDetail = new StockActionDetail();
			sDetail.setId(dbCon.rs.getInt("d.id"));
			sDetail.setStockInId(dbCon.rs.getInt("d.stock_in_id"));
			sDetail.setMaterialId(dbCon.rs.getInt("d.material_id"));
			sDetail.setName(dbCon.rs.getString("d.name"));
			sDetail.setPrice(dbCon.rs.getFloat("d.price"));
			sDetail.setAmount(dbCon.rs.getFloat("d.amount"));
			stockIn.addStockDetail(sDetail);
			
			stockIn.setId(dbCon.rs.getInt("id"));
			stockIn.setRestaurantId(dbCon.rs.getInt("s.restaurant_id"));
			stockIn.setBirthDate(dbCon.rs.getLong("s.birth_date"));
			stockIn.setOriStockId(dbCon.rs.getString("s.ori_stock_id"));
			stockIn.setOriStockIdDate(dbCon.rs.getTimestamp("s.ori_stock_date").getTime());
			stockIn.getDeptIn().setId(dbCon.rs.getShort("s.dept_in"));
			stockIn.getDeptIn().setName(dbCon.rs.getString("s.dept_in_name"));
			stockIn.getDeptOut().setId(dbCon.rs.getShort("s.dept_out"));
			stockIn.getDeptOut().setName(dbCon.rs.getString("s.dept_out_name"));
			stockIn.getSupplier().setSupplierid(dbCon.rs.getInt("s.supplier_id"));
			stockIn.getSupplier().setName(dbCon.rs.getString("s.supplier_name"));
			stockIn.setOperatorId(dbCon.rs.getInt("s.operator_id"));
			stockIn.setOperator(dbCon.rs.getString("s.operator"));
			stockIn.setAmount(dbCon.rs.getFloat("s.amount"));
			stockIn.setPrice(dbCon.rs.getFloat("s.price"));
			stockIn.setType(dbCon.rs.getInt("s.type"));
			stockIn.setSubType(dbCon.rs.getInt("s.sub_type"));
			stockIn.setStatus(dbCon.rs.getInt("s.status"));
			stockIn.setComment(dbCon.rs.getString("s.comment"));	
			
		}
		stockIns.add(stockIn);
		
		dbCon.rs.close();
		return stockIns;
	}
	
	
}
