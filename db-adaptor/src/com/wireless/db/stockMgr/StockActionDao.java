package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.InsertBuilder;
import com.wireless.pojo.stockMgr.StockAction.Status;
import com.wireless.pojo.stockMgr.StockAction.Type;
import com.wireless.pojo.stockMgr.StockAction.UpdateBuilder;
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
	public static int insertStockAction(DBCon dbCon,Terminal term, InsertBuilder builder) throws SQLException{
		StockAction stockIn = builder.build();
		String deptInName;
		String deptOutName;
		String SupplierName;
		
		String selectDeptIn = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDeptIn().getId() + " AND restaurant_id = " +term.restaurantID;		
		dbCon.rs = dbCon.stmt.executeQuery(selectDeptIn);
		if(dbCon.rs.next()){
			deptInName = dbCon.rs.getString(1);
		}else{
			deptInName = "";
		}
	
		String selectDeptOut = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDeptOut().getId() + " AND restaurant_id = " +term.restaurantID;
		dbCon.rs = dbCon.stmt.executeQuery(selectDeptOut);
		if(dbCon.rs.next()){
			deptOutName = dbCon.rs.getString(1);
		}else{
			deptOutName = "";
		}
		
		String selectSupplierName = "SELECT name FROM " + Params.dbName + ".supplier WHERE supplier_id = " + builder.getSupplier().getSupplierId();
		dbCon.rs = dbCon.stmt.executeQuery(selectSupplierName);
		if(dbCon.rs.next()){
			SupplierName = dbCon.rs.getString(1);
		}else{
			SupplierName = "";
		}		
		
		int stockId;
		try{
			dbCon.conn.setAutoCommit(false);
			String insertsql = "INSERT INTO " + Params.dbName + ".stock_action (restaurant_id, birth_date, " +
					"ori_stock_id, ori_stock_date, dept_in, dept_in_name, dept_out, dept_out_name, supplier_id, supplier_name, operator_id, operator, amount, price, cate_type, type, sub_type, status, comment) "+
					" VALUES( " +
					+ stockIn.getRestaurantId() + ", "
					+ "'" + DateUtil.format(new Date().getTime()) + "', "
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
					+ stockIn.getCateType().getValue() + ", " +
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
					sDetail.setStockActionId(stockId);
					StockActionDetailDao.insertStockActionDetail(dbCon, sDetail);
				}			
			}else{
				dbCon.conn.rollback();
				throw new SQLException("Failed to insert stockActionDetail!");
			}
			dbCon.conn.commit();
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw new SQLException("The id is not generated successfully!!");
		}finally{
			dbCon.conn.setAutoCommit(true);
		}
		return stockId;
	}
	/**
	 * Insert a new stock.
	 * @param builder
	 * 			the stockAction builder to insert
	 * @return	the id to stock just created
	 * @throws SQLException
	 * 			if failed to execute any SQL statement 
	 */	
	public static int insertStockAction(Terminal term, InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insertStockAction(dbCon,term, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Delete the stockAction according to extra condition of a specified restaurant defined in terminal.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @return	the amount of stockActions to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */	
	public static int deleteStockAction(DBCon dbCon, String extraCond) throws SQLException{
		String sql;
		sql = "DELETE FROM " + Params.dbName + ".stock_action " +
				" WHERE 1=1 " +
				(extraCond == null ? "" : extraCond);
		return dbCon.stmt.executeUpdate(sql);
	}
	/**
	 * Delete the stockAction according to extra condition of a specified restaurant defined in terminal.
	 * @param term
	 * 			the Terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return	the amount of stockActions to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockAction_id is not exist
	 */
	public static int deleteStockAction(Terminal term, String extraCond) throws SQLException{
		
		return 0;
	}
	/**
	 * Delete the stockAction according to extra condition of a specified restaurant defined in terminal.
	 * @param dbCon
	 * 			the database connection 
	 * @param term
	 * 			the terminal 
	 * @param extraCond
	 * 			the extra condition
	 * @return	the amount of stockActions to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int deleteStockAction(DBCon dbCon, Terminal term, String extraCond) throws SQLException{
		return 0;
	}
	/**
	 * Delete the stockAction according to stockAction_id.
	 * @param term
	 * 			the terminal
	 * @param stockActionId
	 * 			the stockAction_id of stockAction
	 * @throws BusinessException
	 * 			if the stockAction_id is not exist
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static void deleteStockActionById(Terminal term, int stockActionId) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteStockActionById(dbCon, term, stockActionId);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Delete the stockAction according to stockAction_id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param stockActionId
	 * 			the stockAction_id of stockAction
	 * @throws BusinessException
	 * 			if the stockAction_id is not exist
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static void deleteStockActionById(DBCon dbCon, Terminal term, int stockActionId) throws BusinessException, SQLException{
		if(deleteStockAction(dbCon, " AND restaurant_id = " + term.restaurantID + " AND id = " + stockActionId) == 0){
			throw new BusinessException("此库单不存在!!");
		};
	}
	/**
	 * Update stockAction according to stockAction and terminal.
	 * @param term
	 * 			the terminal
	 * @param stockIn
	 * 			the stockAction to update
	 * @return 
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement
	 * @throws BusinessException
	 * 			if the stock to update does not exist
	 */
	public static void auditStockAction(Terminal term, UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			auditStockAction(dbCon, term, builder);
		}finally{
			dbCon.disconnect();
		}
		
	}
	/**
	 * Update stockAction according to stockAction and terminal.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param stockAction
	 * 			the stockAction to update
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement
	 * @throws BusinessException
	 * 			if the stock to update does not exist
	 */
	public static void auditStockAction(DBCon dbCon, Terminal term, UpdateBuilder builder) throws SQLException, BusinessException{
		StockAction stockAction = builder.build();
		String sql;
		sql = "UPDATE " + Params.dbName + ".stock_action SET " +
				" approver_id = " + stockAction.getApproverId() + ", " +
				" approver = '" + stockAction.getApprover() + "'," +
				" approve_date = " + "'" + DateUtil.format(stockAction.getApproverDate()) + "', " +
				" status = " + stockAction.getStatus().getVal() +
				" WHERE id = " + stockAction.getId() + 
				" AND restaurant_id = " + term.restaurantID;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException("不能通过审核,此库单不存在");
		}else{
			StockAction updateStockAction = getStockAndDetailById(term, stockAction.getId());
			int deptId;
			if(updateStockAction.getStatus() == Status.AUDIT){
				for (StockActionDetail sActionDetail : updateStockAction.getStockDetails()) {
					MaterialDept materialDept;
					List<MaterialDept> materialDepts;
					Material material;
					//判断是入库还是出库单
					if(updateStockAction.getType() == Type.STOCK_IN){
						deptId = updateStockAction.getDeptIn().getId();
						materialDept = new MaterialDept();
						materialDept.setDeptId(deptId);
						materialDept.setMaterialId(sActionDetail.getMaterialId());
						
						materialDept.setStock(sActionDetail.getAmount());
						materialDepts = MaterialDeptDao.getMaterialDepts(term, " AND material_id = " + sActionDetail.getMaterialId() + " AND dept_id = " + deptId, null);
						//判断此部门下是否添加了这个原料
						if(materialDepts.isEmpty()){
							//如果没有就新增一条记录
							materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptId, term.restaurantID, sActionDetail.getAmount());
							MaterialDeptDao.insertMaterialDept(term, materialDept);
							
						}else{
							materialDept = materialDepts.get(0);
							//入库单增加部门库存
							materialDept.plusStock(sActionDetail.getAmount());
						}
						material = MaterialDao.getById(materialDept.getMaterialId());
						//入库单增加总库存
						material.plusStock(sActionDetail.getAmount());					
					}else{
						deptId = updateStockAction.getDeptOut().getId();
						
						materialDepts = MaterialDeptDao.getMaterialDepts(term, " AND material_id = " + sActionDetail.getMaterialId() + " AND dept_id = " + deptId, null);
						if(materialDepts.isEmpty()){
							//throw new BusinessException("此部门下还没添加这个原料!");
							//如果没有,则数量就为负数
							materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptId, term.restaurantID, (-sActionDetail.getAmount()));
							MaterialDeptDao.insertMaterialDept(term, materialDept);
						}else{
							materialDept = materialDepts.get(0);
							//出库单减少部门中库存
							materialDept.cutStock(sActionDetail.getAmount());
						}
						material = MaterialDao.getById(materialDept.getMaterialId());
						//出库单减少总库存
						material.cutStock(sActionDetail.getAmount());
					}
					
					//更新原料_部门表
					MaterialDeptDao.updateMaterialDept(term, materialDept);
					//更新原料表
					material.setLastModStaff(term.owner);
					MaterialDao.update(material);	
					
					
				}
			}
		}
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
				" operator_id, operator, amount, price, cate_type, type, sub_type, status, comment " +
				" FROM " + Params.dbName +".stock_action " +
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
			stockIn.setCateType(dbCon.rs.getInt("cate_type"));
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
		StockAction stockIn = new StockAction();
		String sql;
		sql = "SELECT " +
				" s.id, s.restaurant_id, s.birth_date, s.ori_stock_id, s.ori_stock_date, s.dept_in, s.dept_in_name, s.dept_out, s.dept_out_name, s.supplier_id, s.supplier_name," +
				" s.operator_id, s.operator, s.amount, s.price, s.cate_type, s.type, s.sub_type, s.status, s.comment, d.id, d.stock_action_id, d.material_id, d.name, d.price, d.amount " +
				" FROM " + Params.dbName +".stock_action as s " +
				" INNER JOIN " + Params.dbName + ".stock_action_detail as d " +
				" ON s.id = d.stock_action_id" +
				" WHERE s.restaurant_id = " + term.restaurantID +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		HashMap<StockAction, StockAction> result = new HashMap<StockAction, StockAction>();
		while(dbCon.rs.next()){
			//StockAction stockIn = new StockAction();
			StockActionDetail sDetail = new StockActionDetail();
			
			sDetail.setId(dbCon.rs.getInt("d.id"));
			sDetail.setStockActionId(dbCon.rs.getInt("d.stock_action_id"));		
			sDetail.setMaterialId(dbCon.rs.getInt("d.material_id"));
			sDetail.setName(dbCon.rs.getString("d.name"));
			sDetail.setPrice(dbCon.rs.getFloat("d.price"));
			sDetail.setAmount(dbCon.rs.getFloat("d.amount"));
			
			stockIn.setId(dbCon.rs.getInt("id"));
			stockIn.setRestaurantId(dbCon.rs.getInt("s.restaurant_id"));
			stockIn.setBirthDate(dbCon.rs.getTimestamp("s.birth_date").getTime());
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
			stockIn.setCateType(dbCon.rs.getInt("s.cate_type"));
			stockIn.setType(dbCon.rs.getInt("s.type"));
			stockIn.setSubType(dbCon.rs.getInt("s.sub_type"));
			stockIn.setStatus(dbCon.rs.getInt("s.status"));
			stockIn.setComment(dbCon.rs.getString("s.comment"));	
			
			if(result.get(stockIn) == null){
				stockIn.addStockDetail(sDetail);
				result.put(stockIn, stockIn);
			}else{
				result.get(stockIn).addStockDetail(sDetail);
			}
			
		}
		dbCon.rs.close();
		
		return result.values().size() > 0 ? new ArrayList<StockAction>(result.values()) : null; 
	}
	
	
}
