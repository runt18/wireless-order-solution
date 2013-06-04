package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.stockMgr.StockTake;
import com.wireless.pojo.stockMgr.StockTakeDetail;
import com.wireless.pojo.stockMgr.StockTake.InsertBuilder;
import com.wireless.pojo.stockMgr.StockTake.UpdateBuilder;
import com.wireless.pojo.util.DateUtil;
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
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insertStockTake(dbCon, term, builder);
		}finally{
			dbCon.disconnect();
		}
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
		StockTake sTake = builder.build();
		String deptName = "";
		
		String selectDept = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDept().getId() + " AND restaurant_id = " +term.restaurantID;		
		dbCon.rs = dbCon.stmt.executeQuery(selectDept);
		if(dbCon.rs.next()){
			deptName = dbCon.rs.getString(1);
		}
		int stockTakeId = 0;
		dbCon.conn.setAutoCommit(false);
		try{
			String sql = "INSERT INTO " + Params.dbName + ".stock_take(restaurant_id, dept_id, dept_name, " +
					"material_cate_id, status, parent_id, operator, operator_id, start_date, comment)" +
					" VALUES( " +
					sTake.getRestaurantId() + ", " +
					sTake.getDept().getId() + ", " +
					"'" + deptName + "', " +
					sTake.getMaterialCateId() + ", " +
					sTake.getStatus().getVal() + ", " +
					sTake.getParentId() + ", " +
					"'" + sTake.getOperator() + "', " +
					sTake.getOperatorId() + ", " +
					"'" + DateUtil.format(sTake.getStartDate()) + "', " +
					"'" + sTake.getComment() + "'" +
					")";
			dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			dbCon.rs = dbCon.stmt.getGeneratedKeys();		
			if(dbCon.rs.next()){
				stockTakeId = dbCon.rs.getInt(1);
				for (StockTakeDetail tDetail : sTake.getStockTakeDetails()) {
					tDetail.setStockTakeId(stockTakeId);
					StockTakeDetailDao.insertstockTakeDetail(term, tDetail);
				}
			}
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw new SQLException("The id is not generated successfully!!");
		}finally{
			dbCon.conn.setAutoCommit(true);
		}
		return stockTakeId;
		
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
	public static List<StockTake> getStockTakesAndDetail(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockTakesAndDetail(dbCon, term, extraCond, null);
		}finally{
			dbCon.disconnect();
		}
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
	public static List<StockTake> getStockTakesAndDetail(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		List<StockTake> sTakes = new ArrayList<StockTake>();
		String sql ;
		sql = "SELECT st.id, st.restaurant_id, st.dept_id, st.dept_name, st.material_cate_id, st.status, st.parent_id, st.operator, st.operator_id, " +
				"st.approver, st.approver_id, st.start_date, st.finish_date, st.comment, td.id, td.stock_take_id, td.material_id, td.name, td.actual_amount, td.expect_amount, td.delta_amount" +
				" FROM " + Params.dbName + ".stock_take as st " +
				" INNER JOIN " + Params.dbName + ".stock_take_detail as td " +
				" ON st.id = td.stock_take_id " +
				" WHERE restaurant_id = " + term.restaurantID +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		StockTake sTake = new StockTake();
		if(dbCon.rs.next()){
			StockTakeDetail sTakeDetail = new StockTakeDetail();
			sTakeDetail.setId(dbCon.rs.getInt("td.id"));
			sTakeDetail.setStockTakeId(dbCon.rs.getInt("td.stock_take_id"));
			sTakeDetail.setMaterialId(dbCon.rs.getInt("td.material_id"));
			sTakeDetail.setMaterialName(dbCon.rs.getString("td.name"));
			sTakeDetail.setActualAmount(dbCon.rs.getFloat("td.actual_amount"));
			sTakeDetail.setExpectAmount(dbCon.rs.getFloat("td.expect_amount"));
			sTakeDetail.setDeltaAmount(dbCon.rs.getFloat("td.delta_amount"));
			
			sTake.setId(dbCon.rs.getInt("st.id"));
			sTake.setRestaurantId(dbCon.rs.getInt("st.restaurant_id"));
			sTake.setDeptId(dbCon.rs.getInt("st.dept_id"));
			sTake.setDeptName(dbCon.rs.getString("st.dept_name"));
			sTake.setMaterialCateId(dbCon.rs.getInt("st.material_cate_id"));
			sTake.setStatus(dbCon.rs.getInt("st.status"));
			sTake.setParentId(dbCon.rs.getInt("st.parent_id"));
			sTake.setOperatorId(dbCon.rs.getInt("st.operator_id"));
			sTake.setOperator(dbCon.rs.getString("st.operator"));
			sTake.setApprover(dbCon.rs.getString("st.approver"));
			sTake.setApproverId(dbCon.rs.getInt("st.approver_id"));
			sTake.setStartDate(dbCon.rs.getTimestamp("st.start_date").getTime());
			if(dbCon.rs.getTimestamp("st.finish_date") != null){
				sTake.setFinishDate(dbCon.rs.getTimestamp("st.finish_date").getTime());
			}			
			sTake.setComment(dbCon.rs.getString("st.comment"));
			
			sTake.addStockTakeDetail(sTakeDetail);
		}
		sTakes.add(sTake);
		dbCon.rs.close();
		return sTakes;
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
	public static StockTake getStockTakeAndDetailById(Terminal term, int id) throws SQLException,BusinessException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockTakeAndDetailById(dbCon, term, id);
		}finally{
			dbCon.disconnect();
		}
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
	public static StockTake getStockTakeAndDetailById(DBCon dbCon, Terminal term, int id) throws SQLException,BusinessException{
		List<StockTake> list = getStockTakesAndDetail(dbCon, term, " AND st.id = " + id, null);
		if(list.isEmpty()){
			throw new BusinessException("此盘点单不存在!");
		}else{
			return list.get(0);
		}
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
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockTakes(dbCon, term, extraCond, null);
		}finally{
			dbCon.disconnect();
		}
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
		List<StockTake> sTakes = new ArrayList<StockTake>();
		String sql ;
		sql = "SELECT id, restaurant_id, dept_id, dept_name, material_cate_id, status, parent_id, operator, operator_id, " +
				"approver, approver_id, start_date, finish_date, comment " +
				" FROM " + Params.dbName + ".stock_take" +
				" WHERE restaurant_id = " + term.restaurantID +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			StockTake sTake = new StockTake();
			sTake.setId(dbCon.rs.getInt("id"));
			sTake.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			sTake.setDeptId(dbCon.rs.getInt("dept_id"));
			sTake.setDeptName(dbCon.rs.getString("dept_name"));
			sTake.setMaterialCateId(dbCon.rs.getInt("material_cate_id"));
			sTake.setStatus(dbCon.rs.getInt("status"));
			sTake.setParentId(dbCon.rs.getInt("parent_id"));
			sTake.setOperatorId(dbCon.rs.getInt("operator_id"));
			sTake.setOperator(dbCon.rs.getString("operator"));
			sTake.setApprover(dbCon.rs.getString("approver"));
			sTake.setApproverId(dbCon.rs.getInt("approver_id"));
			sTake.setStartDate(dbCon.rs.getTimestamp("start_date").getTime());
			if(dbCon.rs.getTimestamp("finish_date") != null){
				sTake.setFinishDate(dbCon.rs.getTimestamp("finish_date").getTime());
			}
			sTake.setComment(dbCon.rs.getString("comment"));
			
			sTakes.add(sTake);
		}
		dbCon.rs.close();
		return sTakes;
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
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockTakeById(dbCon, term, id);
		}finally{
			dbCon.disconnect();
		}
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
		List<StockTake> list = getStockTakes(dbCon, term, " AND id = " + id, null);
		if(list.isEmpty()){
			throw new BusinessException("此盘点单不存在!");
		}else{
			return list.get(0);
		}
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
	public static void updateStockTake(Terminal term, UpdateBuilder builder) throws SQLException,BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			updateStockTake(dbCon, term, builder);
		}finally{
			dbCon.disconnect();
		}
	}
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
	public static void updateStockTake(DBCon dbCon, Terminal term, UpdateBuilder builder) throws SQLException,BusinessException{
		String sql;
		if(builder.getApprover() != null){
			sql = "UPDATE " + Params.dbName + ".stock_take" + 
					" SET approver = " + "'" + builder.getApprover() + "', " +
					" approver_id = " + builder.getApproverId() + ", " +
					" finish_date = " + "'" + DateUtil.format(builder.getFinishDate()) + "', " +
					" status = " + builder.getStatus().getVal() +
					" WHERE id = " + builder.getId() + 
					" AND restaurant_id = " + term.restaurantID;
		}else{
			sql = "UPDATE " + Params.dbName + ".stock_take" + 
					" SET status = " + builder.getStatus().getVal() +
					" WHERE id = " + builder.getId() + 
					" AND restaurant_id = " + term.restaurantID;
		}
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException("修改失败,此盘点明细单不存在!");
		}
	}
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
	public static void deleteStockTakeById(Terminal term, int id) throws SQLException,BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteStockTakeById(dbCon, term, id);
		}finally{
			dbCon.disconnect();
		}
	}
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
	public static void deleteStockTakeById(DBCon dbCon, Terminal term, int id) throws SQLException,BusinessException{
		if(deleteStockTake(dbCon, term, " AND id = " + id) == 0){
			throw new BusinessException("删除失败,此盘点单不存在!");
		}
	}
	/**
	 * Delete stockTake according to extra condition
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int deleteStockTake(DBCon dbCon, Terminal term, String extraCond) throws SQLException{
		String sql;
		sql = "DELETE FROM " + Params.dbName + ".stock_take" + 
				" WHERE restaurant_id = " + term.restaurantID +
				(extraCond == null ? "" : extraCond);
		return dbCon.stmt.executeUpdate(sql);
	}
	
	
}
