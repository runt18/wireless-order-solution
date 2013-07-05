package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StockError;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.AuditBuilder;
import com.wireless.pojo.stockMgr.StockAction.InsertBuilder;
import com.wireless.pojo.stockMgr.StockAction.Status;
import com.wireless.pojo.stockMgr.StockAction.SubType;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.stockMgr.StockTake;
import com.wireless.pojo.system.Setting;
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
	 * @throws BusinessException 
	 * 			if the OriStockIdDate is before than the last stockTake time
	 */
	public static int insertStockAction(DBCon dbCon,Terminal term, InsertBuilder builder) throws SQLException, BusinessException{
		//获取当前工作月
		long currentDate = 0;
		Calendar c = Calendar.getInstance();
		String selectSetting = "SELECT setting_id, current_material_month FROM "+ Params.dbName + ".setting WHERE restaurant_id = " + term.restaurantID;
		dbCon.rs = dbCon.stmt.executeQuery(selectSetting);
		if(dbCon.rs.next()){
			if(dbCon.rs.getTimestamp("current_material_month") != null){
				currentDate = dbCon.rs.getTimestamp("current_material_month").getTime();
				c.setTime(new Date(currentDate));
			}else{
				//FIXME 当前月的值有多种情况,这里是按用户第一次使用入库的时候初始化 
				c.setTime(new Date());
				
				Setting setting = new Setting();
				setting.setId(dbCon.rs.getInt("setting_id"));
				long first = DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + "01");
				setting.setCurrentMonth(first);
				SystemDao.updateCurrentMonth(setting);
				
			}
			
		}
		dbCon.rs.close();
		//获取月份最大天数
		int day = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		long lastDate = DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + day);
		
		
		//比较盘点时间和月结时间,取最大值
		String selectMaxDate = "SELECT MAX(date) as date FROM (SELECT current_material_month AS date FROM " + Params.dbName + ".setting UNION ALL " +
								" SELECT finish_date AS date FROM " + Params.dbName + ".stock_take where status = " + com.wireless.pojo.stockMgr.StockTake.Status.AUDIT.getVal() + ") M";
		long maxDate = 0;
		dbCon.rs = dbCon.stmt.executeQuery(selectMaxDate);
		if(dbCon.rs.next()){
			maxDate = dbCon.rs.getTimestamp("date").getTime();
		}
		dbCon.rs.close();
		
		//如果是消耗类型的单则不需要限定时间
		if(builder.getSubType() != SubType.USE_UP && builder.getSubType() != SubType.MORE && builder.getSubType() != SubType.LESS){
			//货单原始时间必须大于最后一次已审核盘点时间或月结,小于当前月最后一天
			if(builder.getOriStockIdDate() < maxDate){
				throw new BusinessException(StockError.STOCKACTION_TIME_LATER);

			}else if(builder.getOriStockIdDate() > lastDate){
				throw new BusinessException(StockError.STOCKACTION_TIME_EARLIER);
			}
		}

				

		

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
		dbCon.rs.close(); 
		
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
		dbCon.rs.close(); 
		
		int stockId;
		String insertsql = "INSERT INTO " + Params.dbName + ".stock_action (restaurant_id, birth_date, " +
				"ori_stock_id, ori_stock_date, dept_in, dept_in_name, dept_out, dept_out_name, supplier_id, supplier_name, operator_id, operator, amount, price, cate_type, type, sub_type, status, comment) "+
				" VALUES( " +
				+ stockAction.getRestaurantId() + ", "
				+ "'" + DateUtil.format(new Date().getTime()) + "', "
				+ "'" + stockAction.getOriStockId() + "', "
				+ "'" + DateUtil.formatToDate(stockAction.getOriStockIdDate()) + "', "
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
			//计算结存数量
			for (StockActionDetail sDetail : stockAction.getStockDetails()) {
				Material material = MaterialDao.getById(sDetail.getMaterialId());
				if(stockAction.getSubType() == SubType.STOCK_IN || stockAction.getSubType() == SubType.SPILL || stockAction.getSubType() == SubType.MORE){
					material.plusStock(sDetail.getAmount());
				}else if(stockAction.getSubType() == SubType.STOCK_OUT || stockAction.getSubType() == SubType.DAMAGE || stockAction.getSubType() == SubType.LESS || stockAction.getSubType() == SubType.USE_UP){
					material.cutStock(sDetail.getAmount());
				}
				sDetail.setStockActionId(stockId);
				sDetail.setName(material.getName());
				sDetail.setRemaining(material.getStock());
				
				StockActionDetailDao.insertStockActionDetail(dbCon, sDetail);
			}			
		}else{
			throw new SQLException("Failed to insert stockActionDetail!");
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
	 * @throws BusinessException 
	 * 			if the OriStockIdDate is before than the last stockTake time
	 */	
	public static int insertStockAction(Terminal term, InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		int stockActionId;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			stockActionId = insertStockAction(dbCon,term, builder);
			dbCon.conn.commit();
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}
		finally{
			dbCon.conn.setAutoCommit(true);
			dbCon.disconnect();
		}
		return stockActionId;
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
			throw new BusinessException(StockError.STOCKACTION_DELETE);
		};
		StockActionDetailDao.deleteStockDetail(" AND stock_action_id = " + stockActionId);
	}
	/**
	 * Update StockAction according to stockActionId and InsertBuilder.
	 * @param dbCon
	 * 			the database
	 * @param term
	 * 			the Terminal
	 * @param stockActionId
	 * 			the id of this stockAction
	 * @param builder
	 * 			the StockAction to update
	 * @throws BusinessException
	 * 			if the StockAction is not exist
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static void updateStockAction(DBCon dbCon, Terminal term, int stockActionId, InsertBuilder builder) throws BusinessException, SQLException{
	
		//获取当前工作月
		long currentDate = 0;
		Calendar c = Calendar.getInstance();
		String selectSetting = "SELECT setting_id, current_material_month FROM "+ Params.dbName + ".setting WHERE restaurant_id = " + term.restaurantID;
		dbCon.rs = dbCon.stmt.executeQuery(selectSetting);
		if(dbCon.rs.next()){
			if(dbCon.rs.getTimestamp("current_material_month") != null){
				currentDate = dbCon.rs.getTimestamp("current_material_month").getTime();
				c.setTime(new Date(currentDate));
			}else{
				c.setTime(new Date());
				
				Setting setting = new Setting();
				setting.setId(dbCon.rs.getInt("setting_id"));
				long first = DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + "01");
				setting.setCurrentMonth(first);
				SystemDao.updateCurrentMonth(setting);
				
			}
			
		}
		dbCon.rs.close();
		//获取月份最大天数
		int day = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		long lastDate = DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + day);
		
		
		//比较盘点时间和月结时间,取最大值
		String selectMaxDate = "SELECT MAX(date) as date FROM (SELECT current_material_month AS date FROM " + Params.dbName + ".setting UNION ALL " +
				" SELECT finish_date AS date FROM " + Params.dbName + ".stock_take where status = 2) M";
		long maxDate = 0;
		dbCon.rs = dbCon.stmt.executeQuery(selectMaxDate);
		if(dbCon.rs.next()){
			maxDate = dbCon.rs.getTimestamp("date").getTime();
		}
		dbCon.rs.close();
		

		//货单原始时间必须大于最后一次盘点时间或月结,小于当前月最后一天
		if(builder.getOriStockIdDate() < maxDate){
			throw new BusinessException(StockError.STOCKACTION_TIME_LATER);

		}else if(builder.getOriStockIdDate() > lastDate){
			throw new BusinessException(StockError.STOCKACTION_TIME_EARLIER);
		}
	
		
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
		dbCon.rs.close(); 
		
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
		dbCon.rs.close(); 
		StockAction updateStockAction = builder.build();

		String sql = "UPDATE " + Params.dbName + ".stock_action " + 
				" SET ori_stock_id = '" + builder.getOriStockId() + "' " +
				", ori_stock_date = '" + DateUtil.format(builder.getOriStockIdDate()) + "' " +
				", comment = '" + builder.getComment() + "' " +
				", supplier_id = " + builder.getSupplier().getSupplierId() + 
				", supplier_name = '" + SupplierName + "'" +
				", dept_in = " + builder.getDeptIn().getId() + 
				", dept_in_name = '" + deptInName + "'" +
				", dept_out = " + builder.getDeptOut().getId() + 
				", dept_out_name ='" + deptOutName + "'" +
				", amount = " + updateStockAction.getTotalAmount() + 
				", price = " + updateStockAction.getTotalPrice() +
				" WHERE id = " + stockActionId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StockError.STOCKACTION_UPDATE);
		}
		StockActionDetailDao.deleteStockDetail(dbCon, " AND stock_action_id = " + stockActionId);
		for (StockActionDetail sDetail : builder.getStockActionDetails()) {
			Material material = MaterialDao.getById(sDetail.getMaterialId());
			if(builder.getSubType() == SubType.STOCK_IN || builder.getSubType() == SubType.SPILL || builder.getSubType() == SubType.MORE){
				material.plusStock(sDetail.getAmount());
			}else if(builder.getSubType() == SubType.STOCK_OUT || builder.getSubType() == SubType.DAMAGE || builder.getSubType() == SubType.LESS || builder.getSubType() == SubType.USE_UP){
				material.cutStock(sDetail.getAmount());
			}
			sDetail.setStockActionId(stockActionId);
			sDetail.setName(material.getName());
			sDetail.setRemaining(material.getStock());
			
			StockActionDetailDao.insertStockActionDetail(dbCon, sDetail);
		}
		
	}
	/**
	 * Update StockAction according to stockActionId and InsertBuilder.
	 * @param term
	 * 			the Terminal
	 * @param stockActionId
	 * 			the id of this stockAction
	 * @param builder
	 * 			the StockAction to update
	 * @throws BusinessException
	 * 			if the StockAction is not exist
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static void updateStockAction(Terminal term, int stockActionId, InsertBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			updateStockAction(dbCon, term, stockActionId, builder);
			
			dbCon.conn.commit();
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}
		finally{
			dbCon.conn.setAutoCommit(true);
			dbCon.disconnect();
		}
	}
	/**
	 * Whether conducting stockTake.
	 * @param term
	 * 			The Terminal
	 * @return	true for  conducting stockTake, false for not stockTake
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static boolean isStockTakeChecking(Terminal term) throws SQLException{
		return !StockTakeDao.getStockTakes(term, 
										   " AND status = " + StockTake.Status.CHECKING.getVal(), 
										   null).isEmpty();
		
	}
	/**
	 * Audit stockAction according to stockAction and terminal.
	 * @param term
	 * 			the terminal
	 * @param stockIn
	 * 			the stockAction to update
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement
	 * @throws BusinessException
	 * 			if the stock to update does not exist
	 */
	public static void auditStockAction(Terminal term, AuditBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			auditStockAction(dbCon, term, builder);
			dbCon.conn.commit();
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}
		finally{
			dbCon.conn.setAutoCommit(true);
			dbCon.disconnect();
		}
		
	}
	/**
	 * Audit stockAction according to stockAction and terminal.
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
	public static void auditStockAction(DBCon dbCon, Terminal term, AuditBuilder builder) throws SQLException, BusinessException{
		StockAction auditStockAction = getStockActionById(dbCon, term, builder.getId());
		//如果操作类型不是盘亏或盘盈,则需要判断是否在盘点中
		if(auditStockAction.getSubType() != SubType.MORE && auditStockAction.getSubType() != SubType.LESS){
			isStockTakeChecking(term);
		}
		StockAction stockAction = builder.build();
		String sql;
		sql = "UPDATE " + Params.dbName + ".stock_action SET " +
				" approver_id = " + stockAction.getApproverId() + ", " +
				" approver = '" + stockAction.getApprover() + "'," +
				" approve_date = " + "'" + DateUtil.format(new Date().getTime()) + "', " +
				" status = " + stockAction.getStatus().getVal() +
				" WHERE id = " + stockAction.getId() + 
				" AND restaurant_id = " + term.restaurantID;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StockError.STOCKACTION_AUDIT);
		}else{
			StockAction updateStockAction = getStockAndDetailById(dbCon, term, stockAction.getId());
			//判断是否通过了审核
			if(updateStockAction.getStatus() == Status.AUDIT){
				int deptInId ;
				int deptOutId ;
				for (StockActionDetail sActionDetail : updateStockAction.getStockDetails()) {
					MaterialDept materialDept;
					List<MaterialDept> materialDepts;
					Material material;
					//判断是库单是什么类型的
					if(updateStockAction.getSubType() == SubType.STOCK_IN || updateStockAction.getSubType() == SubType.MORE || updateStockAction.getSubType() == SubType.SPILL){
						deptInId = updateStockAction.getDeptIn().getId();

						materialDepts = MaterialDeptDao.getMaterialDepts(term, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptInId, null);
						//判断此部门下是否添加了这个原料
						if(materialDepts.isEmpty()){
							//如果没有就新增一条记录
							materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptInId, term.restaurantID, sActionDetail.getAmount());
							MaterialDeptDao.insertMaterialDept(term, materialDept);
							
						}else{
							materialDept = materialDepts.get(0);
							//入库单增加部门库存
							materialDept.plusStock(sActionDetail.getAmount());
							//更新原料_部门表
							MaterialDeptDao.updateMaterialDept(dbCon, term, materialDept);
						}

						
						material = MaterialDao.getById(materialDept.getMaterialId());
						//计算加权平均价格
						material.stockInAvgPrice(sActionDetail.getPrice(), sActionDetail.getAmount());
						//入库单增加总库存
						material.plusStock(sActionDetail.getAmount());		
						//更新原料表
						material.setLastModStaff(term.owner);
						MaterialDao.update(dbCon, material);
					}else if(updateStockAction.getSubType() == SubType.STOCK_IN_TRANSFER || updateStockAction.getSubType() == SubType.STOCK_OUT_TRANSFER){
						deptInId = updateStockAction.getDeptIn().getId();
						deptOutId = updateStockAction.getDeptOut().getId();
						
						materialDepts = MaterialDeptDao.getMaterialDepts(term, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptInId, null);
						//判断此部门下是否添加了这个原料
						if(materialDepts.isEmpty()){
							//如果没有就新增一条记录
							materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptInId, term.restaurantID, sActionDetail.getAmount());
							MaterialDeptDao.insertMaterialDept(dbCon, term, materialDept);
							
						}else{
							MaterialDept materialDeptPlus = materialDepts.get(0);
							//入库单增加部门库存
							materialDeptPlus.plusStock(sActionDetail.getAmount());
							MaterialDeptDao.updateMaterialDept(dbCon, term, materialDeptPlus);
						}
						
						materialDepts = MaterialDeptDao.getMaterialDepts(term, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptOutId, null);
						if(materialDepts.isEmpty()){
							//如果没有就新增一条记录
							materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptOutId, term.restaurantID, (-sActionDetail.getAmount()));
							MaterialDeptDao.insertMaterialDept(dbCon, term, materialDept);
							
						}else{
							MaterialDept materialDeptCut = materialDepts.get(0);
							//获取调出部门后对其进行减少
							materialDeptCut.cutStock(sActionDetail.getAmount());
							MaterialDeptDao.updateMaterialDept(dbCon, term, materialDeptCut);
						}
						
					}else{
						deptOutId = updateStockAction.getDeptOut().getId();
						material = MaterialDao.getById(sActionDetail.getMaterialId());
						materialDepts = MaterialDeptDao.getMaterialDepts(term, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptOutId, null);
						if(materialDepts.isEmpty()){
							if(updateStockAction.getSubType() == SubType.LESS){
								materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptOutId, term.restaurantID, 0);
							}else{
								materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptOutId, term.restaurantID, (-sActionDetail.getAmount()));
							}
							
							MaterialDeptDao.insertMaterialDept(dbCon, term, materialDept);
						}else{
							materialDept = materialDepts.get(0);
							//出库单减少部门中库存
							materialDept.cutStock(sActionDetail.getAmount());
							//更新原料_部门表
							MaterialDeptDao.updateMaterialDept(dbCon, term, materialDept);
						}
						//material = MaterialDao.getById(materialDept.getMaterialId());
						//计算加权平均价格
						material.stockOutAvgPrice(sActionDetail.getPrice(), sActionDetail.getAmount());
						//出库单减少总库存
						material.cutStock(sActionDetail.getAmount());
						//更新原料表
						material.setLastModStaff(term.owner);
						MaterialDao.update(dbCon, material);	

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
	public static StockAction getStockActionById(Terminal term, int stockInId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockActionById(dbCon, term, stockInId);
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
	public static StockAction getStockActionById(DBCon dbCon, Terminal term, int stockInId) throws SQLException, BusinessException{
		List<StockAction> stockIns = getStockActions(dbCon, term, " AND id = " + stockInId, null);
		if(stockIns.isEmpty()){
			throw new BusinessException(StockError.STOCKACTION_SELECT);
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
		sql = "SELECT" +
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
			throw new BusinessException(StockError.STOCKACTION_SELECT);
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
			return getStockAndDetail(dbCon, term, extraCond, orderClause);
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
		String sql;
		sql = "SELECT " +
				" S.id, S.restaurant_id, S.birth_date, S.ori_stock_id, S.ori_stock_date, S.dept_in, S.dept_in_name, S.dept_out, S.dept_out_name, S.supplier_id, S.supplier_name," +
				" S.operator_id, S.operator, S.approver, S.approver_id, S.approve_date, S.amount, S.price, S.cate_type, S.type, S.sub_type, S.status, S.comment, D.id, D.stock_action_id, D.material_id, D.name, D.price, D.amount as d_amount, D.remaining " +
				" FROM " + Params.dbName +".stock_action as S " +
				" INNER JOIN " + Params.dbName + ".stock_action_detail as D " +
				" ON S.id = D.stock_action_id" +
				" WHERE S.restaurant_id = " + term.restaurantID +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		Map<StockAction, StockAction> result = new LinkedHashMap<StockAction, StockAction>();
		while(dbCon.rs.next()){
			StockAction stockAction = new StockAction();
			StockActionDetail sDetail = new StockActionDetail();
			
			sDetail.setId(dbCon.rs.getInt("D.id"));
			sDetail.setStockActionId(dbCon.rs.getInt("D.stock_action_id"));		
			sDetail.setMaterialId(dbCon.rs.getInt("D.material_id"));
			sDetail.setName(dbCon.rs.getString("D.name"));
			sDetail.setPrice(dbCon.rs.getFloat("D.price"));
			sDetail.setAmount(dbCon.rs.getFloat("d_amount"));
			sDetail.setRemaining(dbCon.rs.getFloat("remaining"));
			
			stockAction.setId(dbCon.rs.getInt("S.id"));
			stockAction.setRestaurantId(dbCon.rs.getInt("S.restaurant_id"));
			stockAction.setBirthDate(dbCon.rs.getTimestamp("S.birth_date").getTime());
			stockAction.setOriStockId(dbCon.rs.getString("S.ori_stock_id"));
			stockAction.setOriStockIdDate(dbCon.rs.getTimestamp("S.ori_stock_date").getTime());
			stockAction.getDeptIn().setId(dbCon.rs.getShort("S.dept_in"));
			stockAction.getDeptIn().setName(dbCon.rs.getString("S.dept_in_name"));
			stockAction.getDeptOut().setId(dbCon.rs.getShort("S.dept_out"));
			stockAction.getDeptOut().setName(dbCon.rs.getString("S.dept_out_name"));
			stockAction.getSupplier().setSupplierid(dbCon.rs.getInt("S.supplier_id"));
			stockAction.getSupplier().setName(dbCon.rs.getString("S.supplier_name"));
			stockAction.setOperatorId(dbCon.rs.getInt("S.operator_id"));
			stockAction.setOperator(dbCon.rs.getString("S.operator"));
			stockAction.setApprover(dbCon.rs.getString("S.approver"));
			stockAction.setApproverId(dbCon.rs.getInt("S.approver_id"));
			if(dbCon.rs.getTimestamp("S.approve_date") != null){
				stockAction.setApproverDate(dbCon.rs.getTimestamp("S.approve_date").getTime());
			}
			stockAction.setAmount(dbCon.rs.getFloat("S.amount"));
			stockAction.setPrice(dbCon.rs.getFloat("S.price"));
			stockAction.setCateType(dbCon.rs.getInt("S.cate_type"));
			stockAction.setType(dbCon.rs.getInt("S.type"));
			stockAction.setSubType(dbCon.rs.getInt("S.sub_type"));
			stockAction.setStatus(dbCon.rs.getInt("S.status"));
			stockAction.setComment(dbCon.rs.getString("S.comment"));	
			
			if(result.get(stockAction) == null){
				stockAction.addStockDetail(sDetail);
				result.put(stockAction, stockAction);
			}else{
				result.get(stockAction).addStockDetail(sDetail);
			}
			
		}
		dbCon.rs.close();
		
		return result.values().size() > 0 ? new ArrayList<StockAction>(result.values()) : new ArrayList<StockAction>(); 
	}
	
	
}
