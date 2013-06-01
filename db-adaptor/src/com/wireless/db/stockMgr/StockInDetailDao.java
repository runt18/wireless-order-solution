package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.stockMgr.StockInDetail;
import com.wireless.protocol.Terminal;

public class StockInDetailDao {

	/**
	 * Insert a new StockDetail.
	 * @param dbCon
	 * 			the database connection
	 * @param stockDetail
	 * 			the stockDetail to insert
	 * @return the id of stockDetail just create
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int InsertStockInDetail(DBCon dbCon, StockInDetail stockDetail) throws SQLException{
		String sql;
		sql = "INSERT INTO " + Params.dbName + ".stock_in_detail (material_id,name,stock_in_id, price, amount) " +
				" VALUES( " +
				stockDetail.getMaterialId() + ", " +
				"'" + stockDetail.getName() + "', " +
				stockDetail.getStockInId() + ", " +
				stockDetail.getPrice() + ", " +
				stockDetail.getAmount() + ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id is not generated successfully");
		}
		
	}
	/**
	 * Insert a new StockDetail.
	 * @param stockDetail
	 * 			the stockDetail to insert
	 * @return the id of stockDetail just create
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int InsertStockInDetail(StockInDetail stockDetail) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return InsertStockInDetail(dbCon, stockDetail);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Get the list of stockDetail according to extra condition.  
	 * @param term
	 * 			the Terminal 
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return	the list holding the stockDetail result if successfully 
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<StockInDetail> GetStockInDetails(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return GetStockInDetails(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the list of stockDetail according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the Terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return	the list holding the stockDetail result if successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<StockInDetail> GetStockInDetails(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		List<StockInDetail> sDetails = new ArrayList<StockInDetail>();
		String sql;
		sql = "SELECT id, stock_in_id, material_id, name, price, amount " +
				" FROM " + Params.dbName + ".stock_in_detail " +
				" WHERE 1=1" +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : extraCond);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			StockInDetail sDetail = new StockInDetail();
			sDetail.setId(dbCon.rs.getInt("id"));
			sDetail.setStockInId(dbCon.rs.getInt("stock_in_id"));
			sDetail.setMaterialId(dbCon.rs.getInt("material_id"));
			sDetail.setName(dbCon.rs.getString("name"));
			sDetail.setPrice(dbCon.rs.getFloat("price"));
			sDetail.setAmount(dbCon.rs.getFloat("amount"));
			sDetails.add(sDetail);
		}
		dbCon.rs.close();
		return sDetails;
	}
	
	/**
	 * Select stockInDetail according to terminal and stockInDetail_id.
	 * @param term
	 * 			the Terminal 
	 * @param stockInId
	 * 			the id of stockInDetail 
	 * @param SQLException 
	 * 			if failed to execute any SQL statement
	 * @param BusinessException
	 * 			if the stockInDetail to query does not exist
	 * @return	the detail to this StockInDetail
	 */
	public static StockInDetail GetStockInDetailById(Terminal term, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return GetStockInDetailById(dbCon, term, id);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Select stockInDetail according to terminal and stockInDetail_id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the Terminal
	 * @param id
	 * 			the id of stockDetail
	 * @return	the detail to this StockInDetail
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockInDetail to query does not exist
	 */
	public static StockInDetail GetStockInDetailById(DBCon dbCon, Terminal term, int id) throws SQLException, BusinessException{
		List<StockInDetail> list = GetStockInDetails(dbCon, term, " AND id= " + id, null);
		if(list.isEmpty()){
			throw new BusinessException("此明细单不存在!");
		}else{
			return list.get(0);
		}
	}
	
	/**
	 * Delete the stockDetail according to id.
	 * @param term
	 * 			the Terminal
	 * @param id
	 * 			the stock_detail_id
	 * @throws BusinessException
	 * 			if the stock_detail_id is not exist
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static void DeleteStockDetailById(Terminal term, int id) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			DeleteStockDetailById(dbCon, term, id);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Delete the stockDetail according to id. 
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the Terminal
	 * @param id
	 * 			the stock_detail_id 
	 * @throws BusinessException
	 * 			if the stock_detail_id is not exist 
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static void DeleteStockDetailById(DBCon dbCon, Terminal term, int id) throws BusinessException, SQLException{
		if(DeleteStockDetail(dbCon, " AND id = " + id) == 0){
			throw new BusinessException("不能删除,此明细单不存在");
		}
	}
	/**
	 * Delete the stockDetail according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @return	the amount of stockIns to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int DeleteStockDetail(DBCon dbCon, String extraCond) throws SQLException{
		String sql;
		sql = "DELETE FROM " + Params.dbName + ".stock_in_detail" + 
				" WHERE 1=1" +
				(extraCond == null ? "" : extraCond);
		return dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Update the stockDetail according to the stockDetail.
	 * @param stockDetail
	 * 			the stockDetail to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockDetail is not exist
	 */
	public static void UpdateStockDetail(StockInDetail stockDetail) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			UpdateStockDetail(dbCon, stockDetail);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Update the stockDetail according to the stockDetail
	 * @param dbCon
	 * 			the database connection
	 * @param stockDetail
	 * 			the stockDetail to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockDetail is not exist
	 */
	public static void UpdateStockDetail(DBCon dbCon, StockInDetail stockDetail) throws SQLException, BusinessException{
		String sql;
		sql = "UPDATE " + Params.dbName + ".stock_in_detail" +
				" SET stock_in_id = " + stockDetail.getStockInId() + ", " +
				" price = " + stockDetail.getPrice() + ", " +
				" amount = " + stockDetail.getAmount() + 
				" WHERE id = " + stockDetail.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException("the id of stockDetail is not exist");
		}
		
		
	}
}
