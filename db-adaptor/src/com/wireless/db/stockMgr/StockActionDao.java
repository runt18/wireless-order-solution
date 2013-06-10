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
import com.wireless.pojo.stockMgr.StockAction.SubType;
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
		StockAction stockAction = builder.build();
		
		String deptInName;
		String deptOutName;
		String SupplierName;
		
		String selectDeptIn = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDeptIn().getId() + " AND restaurant_id = " +term.restaurantID;		
		dbCon.rs = dbCon.stmt.executeQuery(selectDeptIn);
		if(dbCon.rs.next()){
			deptInName = dbCon.rs.getString("name");
		}else{
			deptInName = "";
		}
	
		String selectDeptOut = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDeptOut().getId() + " AND restaurant_id = " +term.restaurantID;
		dbCon.rs = dbCon.stmt.executeQuery(selectDeptOut);
		if(dbCon.rs.next()){
			deptOutName = dbCon.rs.getString("name");
		}else{
			deptOutName = "";
		}
		
		String selectSupplierName = "SELECT name FROM " + Params.dbName + ".supplier WHERE supplier_id = " + builder.getSupplier().getSupplierId();
		dbCon.rs = dbCon.stmt.executeQuery(selectSupplierName);
		if(dbCon.rs.next()){
			SupplierName = dbCon.rs.getString("name");
		}else{
			SupplierName = "";
		}		
		
		int stockId;
		try{
			dbCon.conn.setAutoCommit(false);
			String insertsql = "INSERT INTO " + Params.dbName + ".stock_action (restaurant_id, birth_date, " +
					"ori_stock_id, ori_stock_date, dept_in, dept_in_name, dept_out, dept_out_name, supplier_id, supplier_name, operator_id, operator, amount, price, cate_type, type, sub_type, status, comment) "+
					" VALUES( " +
					+ stockAction.getRestaurantId() + ", "
					+ "'" + DateUtil.format(new Date().getTime()) + "', "
					//+ 20190909 + ","
					+ "'" + stockAction.getOriStockId() + "', "
					+ "'" + DateUtil.format(stockAction.getOriStockIdDate()) + "', "
					+ stockAction.getDeptIn().getId() + ", "
					+ "'" + deptInName + "', " 
					+ stockAction.getDeptOut().getId() + ", "
					+ "'" + deptOutName + "', "
					+ stockAction.getSupplier().getSupplierId() + ", "
					+ "'" + SupplierName + "', "
					+ stockAction.getOperatorId() + ", "
					+ "'" + stockAction.getOperator() + "', "
					+ stockAction.getTotalAmount() + ", "
					+ stockAction.getTotalPrice() + ", "
					+ stockAction.getCateType().getValue() + ", " +
					+ stockAction.getType().getVal() + ", " 
					+ stockAction.getSubType().getVal() + ", "
					+ stockAction.getStatus().getVal() + ", "
					+ "'" + stockAction.getComment() + "'" 
					+ ")";
			dbCon.stmt.executeUpdate(insertsql, Statement.RETURN_GENERATED_KEYS);
			dbCon.rs = dbCon.stmt.getGeneratedKeys();
			
			if(dbCon.rs.next()){
				stockId = dbCon.rs.getInt(1);
				for (StockActionDetail sDetail : stockAction.getStockDetails()) {
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
			//判断是否通过了审核
			if(updateStockAction.getStatus() == Status.AUDIT){
				int deptInId ;
				int deptOutId ;
				for (StockActionDetail sActionDetail : updateStockAction.getStockDetails()) {
					MaterialDept materialDept;
					List<MaterialDept> materialDepts;
					Material material;
					//判断是库单是什么类型的
					if(updateStockAction.getSubType() == SubType.STOCK_IN){
						deptInId = updateStockAction.getDeptIn().getId();

						materialDepts = MaterialDeptDao.getMaterialDepts(term, " AND material_id = " + sActionDetail.getMaterialId() + " AND dept_id = " + deptInId, null);
						//判断此部门下是否添加了这个原料
						if(materialDepts.isEmpty()){
							//如果没有就新增一条记录
							materialDept = new MaterialDept();
							materialDept.setDeptId(deptInId);
							materialDept.setMaterialId(sActionDetail.getMaterialId());
							
							materialDept.setStock(sActionDetail.getAmount());
							materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptInId, term.restaurantID, sActionDetail.getAmount());
							MaterialDeptDao.insertMaterialDept(term, materialDept);
							
						}else{
							materialDept = materialDepts.get(0);
							//入库单增加部门库存
							materialDept.plusStock(sActionDetail.getAmount());
						}
						//更新原料_部门表
						MaterialDeptDao.updateMaterialDept(term, materialDept);
						
						material = MaterialDao.getById(materialDept.getMaterialId());
						//入库单增加总库存
						material.plusStock(sActionDetail.getAmount());		
						//更新原料表
						material.setLastModStaff(term.owner);
						MaterialDao.update(material);
					}else if(updateStockAction.getSubType() == SubType.STOCK_IN_TRANSFER || updateStockAction.getSubType() == SubType.STOCK_OUT_TRANSFER){
						deptInId = updateStockAction.getDeptIn().getId();
						deptOutId = updateStockAction.getDeptOut().getId();
						
						materialDepts = MaterialDeptDao.getMaterialDepts(term, " AND material_id = " + sActionDetail.getMaterialId() + " AND dept_id = " + deptInId, null);
						//判断此部门下是否添加了这个原料
						if(materialDepts.isEmpty()){
							//如果没有就新增一条记录
							materialDept = new MaterialDept();
							materialDept.setDeptId(deptInId);
							materialDept.setMaterialId(sActionDetail.getMaterialId());
							
							materialDept.setStock(sActionDetail.getAmount());
							materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptInId, term.restaurantID, sActionDetail.getAmount());
							MaterialDeptDao.insertMaterialDept(term, materialDept);
							
						}else{
							MaterialDept materialDeptPlus = materialDepts.get(0);
							//入库单增加部门库存
							materialDeptPlus.plusStock(sActionDetail.getAmount());
							MaterialDeptDao.updateMaterialDept(term, materialDeptPlus);
						}
						
						MaterialDept materialDeptCut = MaterialDeptDao.getMaterialDepts(term, " AND material_id = " + sActionDetail.getMaterialId() + " AND dept_id = " + deptOutId, null).get(0);
						//获取调出部门后对其进行减少
						materialDeptCut.cutStock(sActionDetail.getAmount());
						MaterialDeptDao.updateMaterialDept(term, materialDeptCut);
					}else if(updateStockAction.getSubType() == SubType.STOCK_OUT){
						deptOutId = updateStockAction.getDeptOut().getId();
						
						materialDepts = MaterialDeptDao.getMaterialDepts(term, " AND material_id = " + sActionDetail.getMaterialId() + " AND dept_id = " + deptOutId, null);
						if(materialDepts.isEmpty()){
							throw new BusinessException("此部门下还没添加这个原料,不能退货!");
						}else{
							materialDept = materialDepts.get(0);
							//出库单减少部门中库存
							materialDept.cutStock(sActionDetail.getAmount());
						}
						//更新原料_部门表
						MaterialDeptDao.updateMaterialDept(term, materialDept);
						
						material = MaterialDao.getById(materialDept.getMaterialId());
						//出库单减少总库存
						material.cutStock(sActionDetail.getAmount());
						//更新原料表
						material.setLastModStaff(term.owner);
						MaterialDao.update(material);	

					}else if(updateStockAction.getSubType() == SubType.DAMAGE || updateStockAction.getSubType() == SubType.SPILL){
						deptInId = updateStockAction.getDeptIn().getId();
						materialDept = MaterialDeptDao.getMaterialDepts(term, " AND material_id = " + sActionDetail.getMaterialId() + " AND dept_id = " + deptInId, null).get(0);
						//获得部门信息后判断如果是报溢就增加,是报损就减少
						if(updateStockAction.getSubType() == SubType.DAMAGE){
							materialDept.cutStock(sActionDetail.getAmount());
						}else{
							materialDept.plusStock(sActionDetail.getAmount());
						}
						//更新原料_部门表
						MaterialDeptDao.updateMaterialDept(term, materialDept);
					}
				
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
		List<StockAction> stockIns = getStockActions(dbCon, term, " AND id = " + stockInId, null);
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
	public static List<StockAction> getStockActions(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockActions(dbCon, term, extraCond, orderClause);
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
	public static List<StockAction> getStockActions(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
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
		List<StockAction> stockIns = getStockAndDetail(dbCon, term, " AND S.id = " + stockInId, null);
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
			return getStockActions(dbCon, term, extraCond, orderClause);
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
				" S.id, S.restaurant_id, S.birth_date, S.ori_stock_id, S.ori_stock_date, S.dept_in, S.dept_in_name, S.dept_out, S.dept_out_name, S.supplier_id, S.supplier_name," +
				" S.operator_id, S.operator, S.approver, S.approver_id, S.approve_date, S.amount, S.price, S.cate_type, S.type, S.sub_type, S.status, S.comment, D.id, D.stock_action_id, D.material_id, D.name, D.price, D.amount " +
				" FROM " + Params.dbName +".stock_action as S " +
				" INNER JOIN " + Params.dbName + ".stock_action_detail as D " +
				" ON S.id = D.stock_action_id" +
				" WHERE S.restaurant_id = " + term.restaurantID +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		HashMap<StockAction, StockAction> result = new HashMap<StockAction, StockAction>();
		while(dbCon.rs.next()){
			//StockAction stockIn = new StockAction();
			StockActionDetail sDetail = new StockActionDetail();
			
			sDetail.setId(dbCon.rs.getInt("D.id"));
			sDetail.setStockActionId(dbCon.rs.getInt("D.stock_action_id"));		
			sDetail.setMaterialId(dbCon.rs.getInt("D.material_id"));
			sDetail.setName(dbCon.rs.getString("D.name"));
			sDetail.setPrice(dbCon.rs.getFloat("D.price"));
			sDetail.setAmount(dbCon.rs.getFloat("D.amount"));
			
			stockIn.setId(dbCon.rs.getInt("S.id"));
			stockIn.setRestaurantId(dbCon.rs.getInt("S.restaurant_id"));
			stockIn.setBirthDate(dbCon.rs.getTimestamp("S.birth_date").getTime());
			stockIn.setOriStockId(dbCon.rs.getString("S.ori_stock_id"));
			stockIn.setOriStockIdDate(dbCon.rs.getTimestamp("S.ori_stock_date").getTime());
			stockIn.getDeptIn().setId(dbCon.rs.getShort("S.dept_in"));
			stockIn.getDeptIn().setName(dbCon.rs.getString("S.dept_in_name"));
			stockIn.getDeptOut().setId(dbCon.rs.getShort("S.dept_out"));
			stockIn.getDeptOut().setName(dbCon.rs.getString("S.dept_out_name"));
			stockIn.getSupplier().setSupplierid(dbCon.rs.getInt("S.supplier_id"));
			stockIn.getSupplier().setName(dbCon.rs.getString("S.supplier_name"));
			stockIn.setOperatorId(dbCon.rs.getInt("S.operator_id"));
			stockIn.setOperator(dbCon.rs.getString("S.operator"));
			stockIn.setApprover(dbCon.rs.getString("S.approver"));
			stockIn.setApproverId(dbCon.rs.getInt("S.approver_id"));
			if(dbCon.rs.getTimestamp("S.approve_date") != null){
				stockIn.setApproverDate(dbCon.rs.getTimestamp("S.approve_date").getTime());
			}
			stockIn.setAmount(dbCon.rs.getFloat("S.amount"));
			stockIn.setPrice(dbCon.rs.getFloat("S.price"));
			stockIn.setCateType(dbCon.rs.getInt("S.cate_type"));
			stockIn.setType(dbCon.rs.getInt("S.type"));
			stockIn.setSubType(dbCon.rs.getInt("S.sub_type"));
			stockIn.setStatus(dbCon.rs.getInt("S.status"));
			stockIn.setComment(dbCon.rs.getString("S.comment"));	
			
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
