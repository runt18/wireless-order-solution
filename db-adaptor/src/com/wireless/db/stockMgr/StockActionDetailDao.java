package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StockError;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockActionDetail;

public class StockActionDetailDao {

	/**
	 * Insert a new StockDetail.
	 * @param dbCon
	 * 			the database connection
	 * @param stockDetail
	 * 			the stockDetail to insert
	 * @return the id of stockDetail just create
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the material does NOT exist 
	 */
	public static int insertStockActionDetail(DBCon dbCon, Staff staff, StockActionDetail stockDetail) throws SQLException, BusinessException{
		Material material = MaterialDao.getById(staff, stockDetail.getMaterialId());
		String sql;
		sql = "INSERT INTO " + Params.dbName + ".stock_action_detail (material_id,name,stock_action_id, price, amount, dept_in_remaining, dept_out_remaining, remaining) " +
				" VALUES( " +
				stockDetail.getMaterialId() + ", " +
				"'" + material.getName() + "', " +
				stockDetail.getStockActionId() + ", " +
				stockDetail.getPrice() + ", " +
				stockDetail.getAmount() + ", " +
				stockDetail.getDeptInRemaining() + ", " +
				stockDetail.getDeptOutRemaining() + ", " +
				stockDetail.getRemaining() + ")"; 
		
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
	 * @throws BusinessException 
	 */
	public static int insertStockActionDetail(Staff staff, StockActionDetail stockDetail) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insertStockActionDetail(dbCon, staff, stockDetail);
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
	public static List<StockActionDetail> getStockActionDetails(Staff term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockActionDetails(dbCon, term, extraCond, orderClause);
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
	public static List<StockActionDetail> getStockActionDetails(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException{
		List<StockActionDetail> sDetails = new ArrayList<StockActionDetail>();
		String sql;
		sql = "SELECT id, stock_action_id, material_id, name, price, amount, dept_in_remaining, dept_out_remaining, remaining " +
				" FROM " + Params.dbName + ".stock_action_detail " +
				" WHERE 1=1" +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : extraCond);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			StockActionDetail sDetail = new StockActionDetail();
			sDetail.setId(dbCon.rs.getInt("id"));
			sDetail.setStockActionId(dbCon.rs.getInt("stock_action_id"));
			sDetail.setMaterialId(dbCon.rs.getInt("material_id"));
			sDetail.setName(dbCon.rs.getString("name"));
			sDetail.setPrice(dbCon.rs.getFloat("price"));
			sDetail.setAmount(dbCon.rs.getFloat("amount"));
			sDetail.setDeptInRemaining(dbCon.rs.getFloat("dept_in_remaining"));
			sDetail.setDeptOutRemaining(dbCon.rs.getFloat("dept_out_remaining"));
			sDetail.setRemaining(dbCon.rs.getFloat("remaining"));
			
			sDetails.add(sDetail);
		}
		dbCon.rs.close();
		return sDetails;
	}
	
	/**
	 * Select stockActionDetail according to terminal and stockActionDetail_id.
	 * @param term
	 * 			the Terminal 
	 * @param stockActionId
	 * 			the id of stockActionDetail 
	 * @param SQLException 
	 * 			if failed to execute any SQL statement
	 * @param BusinessException
	 * 			if the stockActionDetail to query does not exist
	 * @return	the detail to this stockActionDetail
	 */
	public static StockActionDetail getStockActionDetailById(Staff term, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockActionDetailById(dbCon, term, id);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Select stockActionDetail according to terminal and stockActionDetail_id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the Terminal
	 * @param id
	 * 			the id of stockDetail
	 * @return	the detail to this stockActionDetail
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockActionDetail to query does not exist
	 */
	public static StockActionDetail getStockActionDetailById(DBCon dbCon, Staff term, int id) throws SQLException, BusinessException{
		List<StockActionDetail> list = getStockActionDetails(dbCon, term, " AND id= " + id, null);
		if(list.isEmpty()){
			throw new BusinessException(StockError.STOCKTAKE_DETAIL_SELECT);
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
	public static void deleteStockDetailById(Staff term, int id) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteStockDetailById(dbCon, term, id);
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
	public static void deleteStockDetailById(DBCon dbCon, Staff term, int id) throws BusinessException, SQLException{
		if(deleteStockDetail(dbCon, " AND id = " + id) == 0){
			throw new BusinessException(StockError.STOCKTAKE_DETAIL_DELETE);
		}
	}
	/**
	 * Delete the stockDetail according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @return	the amount of stockActions to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int deleteStockDetail(DBCon dbCon, String extraCond) throws SQLException{
		String sql;
		sql = "DELETE FROM " + Params.dbName + ".stock_action_detail" + 
				" WHERE 1=1" +
				(extraCond == null ? "" : extraCond);
		return dbCon.stmt.executeUpdate(sql);
	}
	
	public static void deleteStockDetail(String extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteStockDetail(dbCon, extraCond);
		}finally{
			dbCon.disconnect();
		}
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
	public static void updateStockDetail(StockActionDetail stockDetail) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			updateStockDetail(dbCon, stockDetail);
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
	public static void updateStockDetail(DBCon dbCon, StockActionDetail stockDetail) throws SQLException, BusinessException{
		String sql;
		sql = "UPDATE " + Params.dbName + ".stock_action_detail" +
				" SET price = " + stockDetail.getPrice() + ", " +
				" amount = " + stockDetail.getAmount() + ", " + 
				" dept_in_remaining = " + stockDetail.getDeptInRemaining() + ", " +
				" dept_out_remaining = " + stockDetail.getDeptOutRemaining() + ", " + 
				" remaining = " + stockDetail.getRemaining() + 
				" WHERE id = " + stockDetail.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StockError.STOCKTAKE_DETAIL_UPDATE);
		}
		
		
	}
}
