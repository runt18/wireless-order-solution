package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.stockMgr.StockTake;
import com.wireless.pojo.stockMgr.StockTake.InsertStockTakeBuilder;
import com.wireless.pojo.stockMgr.StockTake.UpdateBuilder;
import com.wireless.pojo.stockMgr.StockTakeDetail;
import com.wireless.pojo.util.DateUtil;
import com.wireless.protocol.Terminal;
import com.wireless.util.SQLUtil;

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
	public static int insertStockTake(Terminal term, InsertStockTakeBuilder builder) throws SQLException{
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
	public static int insertStockTake(DBCon dbCon, Terminal term, InsertStockTakeBuilder builder) throws SQLException{
		StockTake sTake = builder.build();
		String deptName;
		
		String selectDept = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDept().getId() + " AND restaurant_id = " +term.restaurantID;		
		dbCon.rs = dbCon.stmt.executeQuery(selectDept);
		if(dbCon.rs.next()){
			deptName = dbCon.rs.getString(1);
		}else{
			deptName = "";
		}
		int stockTakeId;
		
		try{
			dbCon.conn.setAutoCommit(false);
			String sql = "INSERT INTO " + Params.dbName + ".stock_take(restaurant_id, dept_id, dept_name, " +
					"material_cate_id, status, parent_id, operator, operator_id, start_date, comment)" +
					" VALUES( " +
					sTake.getRestaurantId() + ", " +
					sTake.getDept().getId() + ", " +
					"'" + deptName + "', " +
					sTake.getCateType().getValue() + ", " +
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
					StockTakeDetailDao.insertstockTakeDetail(dbCon, term, tDetail);
				}
			}else{
				dbCon.conn.rollback();
				throw new SQLException("The id is not generated successfully!!");
			}
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw new SQLException("Failed to insert stockTakeDetail !");
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
		String sql ;
		sql = "SELECT ST.id, ST.restaurant_id, ST.dept_id, ST.dept_name, ST.material_cate_id, ST.status, ST.parent_id, ST.operator, ST.operator_id, " +
				"ST.approver, ST.approver_id, ST.start_date, ST.finish_date, ST.comment, TD.id, TD.STock_take_id, TD.material_id, TD.name, TD.actual_amount, TD.expect_amount, TD.delta_amount" +
				" FROM " + Params.dbName + ".stock_take as ST " +
				" INNER JOIN " + Params.dbName + ".stock_take_detail as TD " +
				" ON ST.id = TD.stock_take_id " +
				" WHERE ST.restaurant_id = " + term.restaurantID +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		Map<StockTake, StockTake> result = new HashMap<StockTake, StockTake>();
		
		while(dbCon.rs.next()){
			StockTake sTake = new StockTake();
			StockTakeDetail sTakeDetail = new StockTakeDetail();
			
			sTakeDetail.setId(dbCon.rs.getInt("TD.id"));
			sTakeDetail.setStockTakeId(dbCon.rs.getInt("TD.stock_take_id"));
			sTakeDetail.setMaterialId(dbCon.rs.getInt("TD.material_id"));
			sTakeDetail.setMaterialName(dbCon.rs.getString("TD.name"));
			sTakeDetail.setActualAmount(dbCon.rs.getFloat("TD.actual_amount"));
			sTakeDetail.setExpectAmount(dbCon.rs.getFloat("TD.expect_amount"));
			sTakeDetail.setDeltaAmount(dbCon.rs.getFloat("TD.delta_amount"));
			
			sTake.setId(dbCon.rs.getInt("ST.id"));
			sTake.setRestaurantId(dbCon.rs.getInt("ST.restaurant_id"));
			sTake.setDeptId(dbCon.rs.getInt("ST.dept_id"));
			sTake.setDeptName(dbCon.rs.getString("ST.dept_name"));
			sTake.setCateType(dbCon.rs.getInt("ST.material_cate_id"));
			sTake.setStatus(dbCon.rs.getInt("ST.status"));
			sTake.setParentId(dbCon.rs.getInt("ST.parent_id"));
			sTake.setOperatorId(dbCon.rs.getInt("ST.operator_id"));
			sTake.setOperator(dbCon.rs.getString("ST.operator"));
			sTake.setApprover(dbCon.rs.getString("ST.approver"));
			sTake.setApproverId(dbCon.rs.getInt("ST.approver_id"));
			sTake.setStartDate(dbCon.rs.getTimestamp("ST.start_date").getTime());
			if(dbCon.rs.getTimestamp("ST.finish_date") != null){
				sTake.setFinishDate(dbCon.rs.getTimestamp("ST.finish_date").getTime());
			}			
			sTake.setComment(dbCon.rs.getString("ST.comment"));
			
			if(result.get(sTake) == null){
				sTake.addStockTakeDetail(sTakeDetail);
				result.put(sTake, sTake);
			}else{
				result.get(sTake).addStockTakeDetail(sTakeDetail);
			}
		}
		dbCon.rs.close();
		return result.values().size() > 0 ? new ArrayList<StockTake>(result.values()) : new ArrayList<StockTake>();
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
		List<StockTake> list = getStockTakesAndDetail(dbCon, term, " AND ST.id = " + id, null);
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
			sTake.setCateType(dbCon.rs.getInt("material_cate_id"));
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
	public static List<Integer> updateStockTake(Terminal term, UpdateBuilder builder) throws SQLException,BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return updateStockTake(dbCon, term, builder);
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
	public static List<Integer> updateStockTake(DBCon dbCon, Terminal term, UpdateBuilder builder) throws SQLException,BusinessException{
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
		
		
		StockTake stockTake = getStockTakeAndDetailById(term, builder.getId());
		com.wireless.pojo.stockMgr.StockAction.InsertBuilder stockActionBuild = null;
		Map<com.wireless.pojo.stockMgr.StockAction.InsertBuilder, com.wireless.pojo.stockMgr.StockAction.InsertBuilder> insertBuilders = new HashMap<com.wireless.pojo.stockMgr.StockAction.InsertBuilder, com.wireless.pojo.stockMgr.StockAction.InsertBuilder>();
		int stockActionId = 0;
		
		for (StockTakeDetail stockTakeDetail : stockTake.getStockTakeDetails()) {

			if(stockTakeDetail.getDeltaAmount() > 0){
				System.out.println(">0");
				stockActionBuild = StockAction.InsertBuilder.newMore(term.restaurantID)
				   .setOperatorId((int) term.pin).setOperator(term.owner)
				   .setComment("good")
				   .setDeptIn(stockTake.getDept().getId())
				   .setCateType(stockTake.getCateType().getValue());
				Map<Object, Object> param = new HashMap<Object, Object>();
				param.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + term.restaurantID + " AND M.material_id = " + stockTakeDetail.getMaterial().getId());
				Material material = MaterialDao.getContent(param).get(0);
				System.out.println("reid"+stockActionBuild.getRestaurantId());
				
				if(insertBuilders.get(stockActionBuild) == null){
					stockActionBuild.addDetail(new StockActionDetail(material.getId(),material.getName(), material.getPrice(), stockTakeDetail.getDeltaAmount()));
					insertBuilders.put(stockActionBuild, stockActionBuild);
				}else{
					insertBuilders.get(stockActionBuild).addDetail(new StockActionDetail(material.getId(),material.getName(), material.getPrice(), stockTakeDetail.getDeltaAmount()));
				}
			}else if(stockTakeDetail.getDeltaAmount() < 0){
				System.out.println("<0");
				stockActionBuild = StockAction.InsertBuilder.newLess(term.restaurantID)
														   .setOperatorId((int) term.pin).setOperator(term.owner)
														   .setComment("good")
														   .setDeptIn(stockTake.getDept().getId())
														   .setCateType(stockTake.getCateType().getValue());
				Map<Object, Object> param = new HashMap<Object, Object>();
				param.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + term.restaurantID + " AND M.material_id = " + stockTakeDetail.getMaterial().getId());
				Material material = MaterialDao.getContent(param).get(0);
				System.out.println("reid"+stockActionBuild.getRestaurantId());
				if(insertBuilders.get(stockActionBuild) == null){
					stockActionBuild.addDetail(new StockActionDetail(material.getId(),material.getName(), material.getPrice(), stockTakeDetail.getDeltaAmount()));
					insertBuilders.put(stockActionBuild, stockActionBuild);
				}else{
					insertBuilders.get(stockActionBuild).addDetail(new StockActionDetail(material.getId(),material.getName(), material.getPrice(), stockTakeDetail.getDeltaAmount()));
				}
				
			}
		}
		List<Integer> result;
		if(!stockActionBuild.getStockInDetails().isEmpty()){
			result = new ArrayList<Integer>();
			for (com.wireless.pojo.stockMgr.StockAction.InsertBuilder stockActionInsertBuild : insertBuilders.values()) {
				stockActionId = StockActionDao.insertStockAction(term, stockActionInsertBuild);
				com.wireless.pojo.stockMgr.StockAction.UpdateBuilder updateBuilder = StockAction.UpdateBuilder.newStockActionAudit(stockActionId)
										.setApproverId((int) term.pin).setApprover(term.owner)
										.setApproverDate(DateUtil.parseDate("2013-06-03"));
				StockActionDao.auditStockAction(term, updateBuilder);
			}
			
		}else{
			result = new ArrayList<Integer>();
		}
		
		return result;
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
