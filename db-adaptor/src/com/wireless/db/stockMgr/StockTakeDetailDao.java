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
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.pojo.stockMgr.StockTake;
import com.wireless.pojo.stockMgr.StockTakeDetail;

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
	 * @throws BusinessException 
	 */
	public static int insertstockTakeDetail(Staff term, StockTakeDetail sTakeDetail) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insertstockTakeDetail(dbCon, term, sTakeDetail);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Insert a new stockTakeDetail.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param builder
	 * 			the stockTakeDetail builder to insert
	 * @return	the id of stockTakeDetail just created
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static int insertstockTakeDetail(DBCon dbCon, Staff staff, StockTakeDetail sTakeDetail) throws SQLException, BusinessException{
		Material material = MaterialDao.getById(dbCon, staff, sTakeDetail.getMaterial().getId());
		StockTake stockTake = StockTakeDao.getStockTakeById(dbCon, staff, sTakeDetail.getStockTakeId());
		List<MaterialDept> materialDepts = MaterialDeptDao.getMaterialDepts(dbCon, staff, " AND MD.material_id = " + sTakeDetail.getMaterial().getId() + " AND MD.dept_id = " + stockTake.getDept().getId(), null);
		if(!materialDepts.isEmpty()){
			sTakeDetail.setExpectAmount(materialDepts.get(0).getStock());
		}
		String sql;	
		sql = "INSERT INTO " + Params.dbName + ".stock_take_detail (stock_take_id, material_id, " +
				"name, actual_amount, expect_amount, delta_amount)" + 
				" VALUES (" +
				sTakeDetail.getStockTakeId() + "," +
				sTakeDetail.getMaterial().getId() + "," +
				"'" + material.getName() + "', " +
				sTakeDetail.getActualAmount() + "," +
				sTakeDetail.getExpectAmount() + "," +
				sTakeDetail.getTotalDelta() + ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id is not generated successfully");
		}
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
	public static List<StockTakeDetail> getstockTakeDetails(Staff term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getstockTakeDetails(dbCon, term, extraCond, null);
		}finally{
			dbCon.disconnect();
		}
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
	public static List<StockTakeDetail> getstockTakeDetails(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException{
		List<StockTakeDetail> sTakeDetails = new ArrayList<StockTakeDetail>();
		String sql ;
		sql = "SELECT id, stock_take_id, material_id, name, actual_amount, expect_amount, delta_amount " +
				" FROM " + Params.dbName + ".stock_take_detail" +
				" WHERE 1=1 " +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			StockTakeDetail sTakeDetail = new StockTakeDetail();
			sTakeDetail.setId(dbCon.rs.getInt("id"));
			sTakeDetail.setStockTakeId(dbCon.rs.getInt("stock_take_id"));
			sTakeDetail.setMaterialId(dbCon.rs.getInt("material_id"));
			sTakeDetail.setMaterialName(dbCon.rs.getString("name"));
			sTakeDetail.setActualAmount(dbCon.rs.getFloat("actual_amount"));
			sTakeDetail.setExpectAmount(dbCon.rs.getFloat("expect_amount"));
			sTakeDetail.setDeltaAmount(dbCon.rs.getFloat("delta_amount"));
			
			sTakeDetails.add(sTakeDetail);
		}
		dbCon.rs.close();
		return sTakeDetails;
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
	public static StockTakeDetail getstockTakeDetailById(Staff term, int id) throws SQLException,BusinessException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getstockTakeDetailById(dbCon, term, id);
		}finally{
			dbCon.disconnect();
		}
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
	public static StockTakeDetail getstockTakeDetailById(DBCon dbCon, Staff term, int id) throws SQLException,BusinessException{
		List<StockTakeDetail> list = getstockTakeDetails(dbCon, term, " AND id = " + id, null);
		if(list.isEmpty()){
			throw new BusinessException(StockError.STOCKTAKE_DETAIL_SELECT);
		}else{
			return list.get(0);
		}
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
	public static void updateStockTakeDetail(Staff term, StockTakeDetail tDetail) throws SQLException,BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			updateStockTakeDetail(dbCon, term, tDetail);
		}finally{
			dbCon.disconnect();
		}
	}
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
	public static void updateStockTakeDetail(DBCon dbCon, Staff term, StockTakeDetail tDetail) throws SQLException,BusinessException{
		String sql;
		sql = "UPDATE " + Params.dbName + ".stock_take_detail" + 
				" SET actual_amount = " + tDetail.getActualAmount() +
				" WHERE id = " + tDetail.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StockError.STOCKTAKE_DETAIL_UPDATE);
		}
	}
	/**
	 * Delete stockTakeDetail by id
	 * @param id
	 * 			the id of the stockTakeDetail
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTakeDetail is not exist
	 */
	public static void deleteStockTakeDetailById(int id) throws SQLException,BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteStockTakeDetailById(dbCon, id);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Delete stockTakeDetail by id
	 * @param dbCon
	 * 			the database connection
	 * @param id
	 * 			the id of the stockTakeDetail
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTakeDetail is not exist
	 */
	public static void deleteStockTakeDetailById(DBCon dbCon, int id) throws SQLException,BusinessException{
		if(deleteStockTakeDetail(dbCon, " AND id = " + id) == 0){
			throw new BusinessException(StockError.STOCKTAKE_DETAIL_DELETE);
		}
	}
	/**
	 * Delete stockTakeDetail according to extra condition
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int deleteStockTakeDetail(DBCon dbCon, String extraCond) throws SQLException{
		String sql;
		sql = "DELETE FROM " + Params.dbName + ".stock_take_detail" + 
				" WHERE 1=1" +
				(extraCond == null ? "" : extraCond);
		return dbCon.stmt.executeUpdate(sql);
	}
	
	public static int deleteStockTakeDetail(String extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return deleteStockTakeDetail(dbCon, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Get the date of deltaReport. 
	 * @param term
	 * @param extraCond
	 * @param orderClause
	 * @return	the list of StockTakeDetail
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<StockTakeDetail> deltaReport(Staff term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return deltaReport(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
		
	}
	/**
	 * Get the date of deltaReport.
	 * @param dbCon
	 * @param term
	 * @param extraCond
	 * @param orderClause
	 * @return	the list of StockTakeDetail
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<StockTakeDetail> deltaReport(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException{
		String sql;
		List<StockTakeDetail> list = new ArrayList<StockTakeDetail>();
		sql = "SELECT T.dept_name, M.material_id, M.name, M.price, TD.actual_amount, TD.expect_amount, TD.delta_amount FROM (wireless_order_db.stock_take as T " +
				"INNER JOIN wireless_order_db.stock_take_detail as TD ON T.id = TD.stock_take_id) " +
				"INNER JOIN wireless_order_db.material as M ON M.material_id = TD.material_id " +
				" WHERE T.restaurant_id = " + term.getRestaurantId() + 
				(extraCond == null ? "" : extraCond) + 
				(orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			StockTakeDetail stockTakeDetail = new StockTakeDetail();
			stockTakeDetail.getMaterial().setId(dbCon.rs.getInt("material_id"));
			stockTakeDetail.getMaterial().setName(dbCon.rs.getString("name"));
			stockTakeDetail.getMaterial().setPrice(dbCon.rs.getFloat("price"));
			stockTakeDetail.setActualAmount(dbCon.rs.getFloat("actual_amount"));
			stockTakeDetail.setExpectAmount(dbCon.rs.getFloat("expect_amount"));
			
			list.add(stockTakeDetail);
		}
		return list;
	}

}
