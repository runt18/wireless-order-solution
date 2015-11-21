package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ModuleError;
import com.wireless.exception.StockError;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.restaurantMgr.Module;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.AuditBuilder;
import com.wireless.pojo.stockMgr.StockAction.InsertBuilder;
import com.wireless.pojo.stockMgr.StockAction.Status;
import com.wireless.pojo.stockMgr.StockAction.SubType;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.stockMgr.StockTake;
import com.wireless.pojo.util.DateUtil;

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
	public static int insertStockAction(DBCon dbCon, Staff staff, InsertBuilder builder) throws SQLException, BusinessException{
		
		Restaurant restaurant = RestaurantDao.getById(staff.getRestaurantId());
		//是否有库存模块授权&入库类型
		if(!restaurant.hasModule(Module.Code.INVENTORY) && (builder.getSubType() != StockAction.SubType.USE_UP)){
			//限制添加的条数
			final int stockActionCountLimit = 50;
			String sql = "SELECT COUNT(*) FROM " + Params.dbName + ".stock_action WHERE restaurant_id = " + staff.getRestaurantId() + " AND sub_type <> " + StockAction.SubType.USE_UP.getVal();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			int stockActionCount = 0;
			if(dbCon.rs.next()){
				stockActionCount = dbCon.rs.getInt(1);
			}
			if(stockActionCount > stockActionCountLimit){
				throw new BusinessException(ModuleError.INVENTORY_LIMIT);
			}
			dbCon.rs.close(); 
		}
		//判断是否同个部门下进行调拨
		if((builder.getSubType() == SubType.STOCK_IN_TRANSFER || builder.getSubType() == SubType.STOCK_OUT_TRANSFER) && builder.getDeptIn().getId() == builder.getDeptOut().getId()){
			throw new BusinessException(StockError.MATERIAL_DEPT_EXIST);
		}
		//获取当前工作月
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		
		//获取月份最大天数
		int day = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		long lastDate = DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + day);
		
		
		//比较盘点时间和月结时间,取最大值
		String selectMaxDate = "SELECT MAX(date) as date FROM (SELECT  MAX(date_add(month, interval 1 MONTH)) date FROM " + Params.dbName + ".monthly_balance WHERE restaurant_id = " + staff.getRestaurantId() + 
								" UNION ALL " +
								" SELECT finish_date AS date FROM " + Params.dbName + ".stock_take WHERE restaurant_id = " + staff.getRestaurantId() + " AND status = " + StockTake.Status.AUDIT.getVal() + ") M";
		long maxDate = 0;
		dbCon.rs = dbCon.stmt.executeQuery(selectMaxDate);
		if(dbCon.rs.next()){
			if(dbCon.rs.getTimestamp("date") != null){
				maxDate = dbCon.rs.getTimestamp("date").getTime();
			}else{
				Calendar max = Calendar.getInstance();
				maxDate = DateUtil.parseDate(max.get(Calendar.YEAR) + "-" + (max.get(Calendar.MONTH)+1) + "-01");
			}
		}
		dbCon.rs.close();
		
		//如果是消耗类型或初始化类型的单则不需要限定时间
		if(builder.getSubType() != SubType.INIT && builder.getSubType() != SubType.USE_UP && builder.getSubType() != SubType.MORE && builder.getSubType() != SubType.LESS){
			//货单原始时间必须大于最后一次已审核盘点时间或月结,小于当前月最后一天
			if(builder.getOriStockDate() < maxDate){
				throw new BusinessException(StockError.STOCKACTION_TIME_LATER);

			}else if(builder.getOriStockDate() > lastDate){
				throw new BusinessException(StockError.STOCKACTION_TIME_EARLIER);
			}
		}
		//判断除了初始化, 消耗,盘盈,盘亏单外, 是否正在盘点中
		if(builder.getSubType() != SubType.INIT && builder.getSubType() != SubType.USE_UP && builder.getSubType() != SubType.LESS && builder.getSubType() != SubType.MORE){
			checkStockTake(dbCon, staff);
		}		

		

		StockAction stockAction = builder.build();
		
		String deptInName;
		String deptOutName;
		String SupplierName;
		
		String selectDeptIn = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDeptIn().getId() + " AND restaurant_id = " + staff.getRestaurantId();		
		dbCon.rs = dbCon.stmt.executeQuery(selectDeptIn);
		if(dbCon.rs.next()){
			deptInName = dbCon.rs.getString("name");
		}else{
			deptInName = "";
		}
		dbCon.rs.close(); 
		
		String selectDeptOut = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDeptOut().getId() + " AND restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(selectDeptOut);
		if(dbCon.rs.next()){
			deptOutName = dbCon.rs.getString("name");
		}else{
			deptOutName = "";
		}
		
		String selectSupplierName = "SELECT name FROM " + Params.dbName + ".supplier WHERE supplier_id = " + builder.getSupplier().getSupplierId() + " AND restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(selectSupplierName);
		if(dbCon.rs.next()){
			SupplierName = dbCon.rs.getString("name");
		}else{
			SupplierName = "";
		}		
		dbCon.rs.close(); 
		
		int stockId;
		String insertsql = "INSERT INTO " + Params.dbName + ".stock_action (restaurant_id, birth_date, " +
				"ori_stock_id, ori_stock_date, dept_in, dept_in_name, dept_out, dept_out_name, supplier_id, supplier_name, operator_id, operator, amount, price, actual_price, cate_type, type, sub_type, status, comment) "+
				" VALUES( " +
				+ stockAction.getRestaurantId() + ", "
				+ "'" + DateUtil.format(new Date().getTime()) + "', "
				+ "'" + stockAction.getOriStockId() + "', "
				+ "'" + DateUtil.formatToDate(stockAction.getOriStockDate()) + "', "
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
				+ stockAction.getActualPrice() + ", "
				+ stockAction.getCateType().getValue() + ", " 
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
				Material material = MaterialDao.getById(staff, sDetail.getMaterialId());
				if(stockAction.getSubType() == SubType.STOCK_IN || stockAction.getSubType() == SubType.SPILL || stockAction.getSubType() == SubType.MORE){
					material.plusStock(sDetail.getAmount());
				}else if(stockAction.getSubType() == SubType.STOCK_OUT || stockAction.getSubType() == SubType.DAMAGE || stockAction.getSubType() == SubType.LESS || stockAction.getSubType() == SubType.USE_UP){
					material.cutStock(sDetail.getAmount());
				}
				//初始化库存单不进行加减
				sDetail.setStockActionId(stockId);
				sDetail.setName(material.getName());
				sDetail.setRemaining(material.getStock());
				
				StockActionDetailDao.insertStockActionDetail(dbCon, staff, sDetail);
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
	public static int insertStockAction(Staff term, InsertBuilder builder) throws SQLException, BusinessException{
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
	public static int deleteStockAction(Staff term, String extraCond) throws SQLException{
		
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
	public static int deleteStockAction(DBCon dbCon, Staff term, String extraCond) throws SQLException{
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
	public static void deleteStockActionById(Staff term, int stockActionId) throws BusinessException, SQLException{
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
	public static void deleteStockActionById(DBCon dbCon, Staff term, int stockActionId) throws BusinessException, SQLException{
		if(deleteStockAction(dbCon, " AND restaurant_id = " + term.getRestaurantId() + " AND id = " + stockActionId) == 0){
			throw new BusinessException(StockError.STOCKACTION_DELETE);
		};
		StockActionDetailDao.deleteStockDetail(" AND stock_action_id = " + stockActionId);
	}
	/**
	 * Update StockAction according to stockActionId and InsertBuilder.
	 * @param dbCon
	 * 			the database
	 * @param staff
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
	public static void updateStockAction(DBCon dbCon, Staff staff, int stockActionId, InsertBuilder builder) throws BusinessException, SQLException{
		//判断是否同个部门下进行调拨
		if((builder.getSubType() == SubType.STOCK_IN_TRANSFER || builder.getSubType() == SubType.STOCK_OUT_TRANSFER) && builder.getDeptIn().getId() == builder.getDeptOut().getId()){
			throw new BusinessException(StockError.MATERIAL_DEPT_UPDATE_EXIST);
		}
		//获取当前工作月
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		//获取月份最大天数
		int day = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		long lastDate = DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + day);
		
		
		//比较盘点时间和月结时间,取最大值
		String selectMaxDate = "SELECT MAX(date) as date FROM (SELECT  MAX(date_add(month, interval 1 MONTH)) date FROM " + Params.dbName + ".monthly_balance WHERE restaurant_id = " + staff.getRestaurantId() + 
				" UNION ALL " +
				" SELECT finish_date AS date FROM " + Params.dbName + ".stock_take WHERE restaurant_id = " + staff.getRestaurantId() + " AND status = " + StockTake.Status.AUDIT.getVal() + ") M";
		long maxDate = 0;
		dbCon.rs = dbCon.stmt.executeQuery(selectMaxDate);
		if(dbCon.rs.next()){
			if(dbCon.rs.getTimestamp("date") != null){
				maxDate = dbCon.rs.getTimestamp("date").getTime();
			}else{
				Calendar max = Calendar.getInstance();
				maxDate = DateUtil.parseDate(max.get(Calendar.YEAR) + "-" + (max.get(Calendar.MONTH)+1) + "-01");
			}
		}
		dbCon.rs.close();
		

		//货单原始时间必须大于最后一次盘点时间或月结,小于当前月最后一天
		if(builder.getOriStockDate() < maxDate){
			throw new BusinessException(StockError.STOCKACTION_TIME_LATER);

		}else if(builder.getOriStockDate() > lastDate){
			throw new BusinessException(StockError.STOCKACTION_TIME_EARLIER);
		}
	
		
		String deptInName;
		String deptOutName;
		String SupplierName;
		
		String selectDeptIn = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDeptIn().getId() + " AND restaurant_id = " + staff.getRestaurantId();		
		dbCon.rs = dbCon.stmt.executeQuery(selectDeptIn);
		if(dbCon.rs.next()){
			deptInName = dbCon.rs.getString("name");
		}else{
			deptInName = "";
		}
		dbCon.rs.close(); 
		
		String selectDeptOut = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDeptOut().getId() + " AND restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(selectDeptOut);
		if(dbCon.rs.next()){
			deptOutName = dbCon.rs.getString("name");
		}else{
			deptOutName = "";
		}
		
		String selectSupplierName = "SELECT name FROM " + Params.dbName + ".supplier WHERE supplier_id = " + builder.getSupplier().getSupplierId() + " AND restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(selectSupplierName);
		if(dbCon.rs.next()){
			SupplierName = dbCon.rs.getString("name");
		}else{
			SupplierName = "";
		}		
		dbCon.rs.close(); 
		StockAction updateStockAction = builder.build();

		String sql = "UPDATE " + Params.dbName + ".stock_action SET " + 
				" ori_stock_id = '" + builder.getOriStockId() + "' " +
				", ori_stock_date = '" + DateUtil.format(builder.getOriStockDate()) + "' " +
				", comment = '" + builder.getComment() + "' " +
				", supplier_id = " + builder.getSupplier().getSupplierId() + 
				", supplier_name = '" + SupplierName + "'" +
				", dept_in = " + builder.getDeptIn().getId() + 
				", dept_in_name = '" + deptInName + "'" +
				", dept_out = " + builder.getDeptOut().getId() + 
				", dept_out_name ='" + deptOutName + "'" +
				", amount = " + updateStockAction.getTotalAmount() + 
				", price = " + updateStockAction.getTotalPrice() +
				", actual_price = " + updateStockAction.getActualPrice() + 
				" WHERE id = " + stockActionId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StockError.STOCKACTION_UPDATE);
		}
		StockActionDetailDao.deleteStockDetail(dbCon, " AND stock_action_id = " + stockActionId);
		for (StockActionDetail sDetail : builder.getStockActionDetails()) {
			Material material = MaterialDao.getById(staff, sDetail.getMaterialId());
			if(builder.getSubType() == SubType.STOCK_IN || builder.getSubType() == SubType.SPILL || builder.getSubType() == SubType.MORE){
				material.plusStock(sDetail.getAmount());
			}else if(builder.getSubType() == SubType.STOCK_OUT || builder.getSubType() == SubType.DAMAGE || builder.getSubType() == SubType.LESS || builder.getSubType() == SubType.USE_UP){
				material.cutStock(sDetail.getAmount());
			}
			sDetail.setStockActionId(stockActionId);
			sDetail.setName(material.getName());
			sDetail.setRemaining(material.getStock());
			
			StockActionDetailDao.insertStockActionDetail(dbCon, staff, sDetail);
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
	public static void updateStockAction(Staff term, int stockActionId, InsertBuilder builder) throws BusinessException, SQLException{
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
	public static boolean isStockTakeChecking(DBCon dbCon, Staff term) throws SQLException{
		return !StockTakeDao.getStockTakes(dbCon, term, 
										   " AND status = " + StockTake.Status.CHECKING.getVal(), 
										   null).isEmpty();
		
	}
	public static boolean checkStockTake(Staff term) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return checkStockTake(dbCon, term);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static boolean checkStockTake(DBCon dbCon, Staff term) throws SQLException, BusinessException{
		if(isStockTakeChecking(dbCon, term)){
			throw new BusinessException(StockError.STOCKACTION_INSERT); 
		}else{
			return false;
		}
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
	public static void auditStockAction(Staff term, AuditBuilder builder) throws SQLException, BusinessException{
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
	 * @param staff
	 * 			the terminal
	 * @param stockAction
	 * 			the stockAction to update
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement
	 * @throws BusinessException
	 * 			if the stock to update does not exist
	 */
	public static void auditStockAction(DBCon dbCon, Staff staff, AuditBuilder builder) throws SQLException, BusinessException{
		StockAction auditStockAction = getStockActionById(dbCon, staff, builder.getId());
		//如果操作类型不是盘亏或盘盈,则需要判断是否在盘点中
		if(auditStockAction.getSubType() != SubType.MORE && auditStockAction.getSubType() != SubType.LESS){
			isStockTakeChecking(dbCon, staff);
		}
		StockAction stockAction = builder.build();
		String sql;
		sql = "UPDATE " + Params.dbName + ".stock_action SET " +
				" approver_id = " + stockAction.getApproverId() + ", " +
				" approver = '" + stockAction.getApprover() + "'," +
				" approve_date = " + "'" + (stockAction.getApproverDate() > 0 ? DateUtil.format(stockAction.getApproverDate()) : DateUtil.format(new Date().getTime())) + "', " +
				" status = " + stockAction.getStatus().getVal() +
				" WHERE id = " + stockAction.getId() + 
				" AND restaurant_id = " + staff.getRestaurantId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StockError.STOCKACTION_AUDIT);
		}else{
			StockAction updateStockAction = getStockAndDetailById(dbCon, staff, stockAction.getId());
			//判断是否通过了审核
			if(updateStockAction.getStatus() == Status.AUDIT){
				//初始化库存单不进行加减
				if(updateStockAction.getSubType() != SubType.INIT){
					int deptInId ;
					int deptOutId ;
					for (StockActionDetail sActionDetail : updateStockAction.getStockDetails()) {
						MaterialDept materialDept;
						List<MaterialDept> materialDepts;
						Material material;
						//判断是库单是什么类型的
						if(updateStockAction.getSubType() == SubType.STOCK_IN || updateStockAction.getSubType() == SubType.MORE || updateStockAction.getSubType() == SubType.SPILL){
							deptInId = updateStockAction.getDeptIn().getId();

							materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptInId, null);
							//判断此部门下是否添加了这个原料
							if(materialDepts.isEmpty()){
								//如果没有就新增一条记录 
								materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptInId, staff.getRestaurantId(), sActionDetail.getAmount());
								MaterialDeptDao.insertMaterialDept(staff, materialDept);
								//更新剩余数量
								sActionDetail.setDeptInRemaining(sActionDetail.getAmount());
								
							}else{
								materialDept = materialDepts.get(0);
								//入库单增加部门库存
								materialDept.plusStock(sActionDetail.getAmount());
								//更新原料_部门表
								MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDept);
								//更新剩余数量
								sActionDetail.setDeptInRemaining(materialDept.getStock());
								
							}

							//取消加权平均, 改用参考单价
							material = MaterialDao.getById(staff, materialDept.getMaterialId());
							
							//入库单增加总库存
							material.plusStock(sActionDetail.getAmount());		
							//更新原料表
							material.setLastModStaff(staff.getName());
							MaterialDao.update(dbCon, material);
							
							//更新库存明细表
							sActionDetail.setRemaining(material.getStock());
							StockActionDetailDao.updateStockDetail(dbCon, sActionDetail);
						}else if(updateStockAction.getSubType() == SubType.STOCK_IN_TRANSFER || updateStockAction.getSubType() == SubType.STOCK_OUT_TRANSFER){
							deptInId = updateStockAction.getDeptIn().getId();
							deptOutId = updateStockAction.getDeptOut().getId();
							
							materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptInId, null);
							//判断此部门下是否添加了这个原料
							if(materialDepts.isEmpty()){
								//如果没有就新增一条记录
								materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptInId, staff.getRestaurantId(), sActionDetail.getAmount());
								MaterialDeptDao.insertMaterialDept(dbCon, staff, materialDept);
								
								//更新剩余数量
								sActionDetail.setDeptInRemaining(sActionDetail.getAmount());
							}else{
								MaterialDept materialDeptPlus = materialDepts.get(0);
								//入库单增加部门库存
								materialDeptPlus.plusStock(sActionDetail.getAmount());
								MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDeptPlus);
								
								//更新剩余数量
								sActionDetail.setDeptInRemaining(materialDeptPlus.getStock());
							}
							
							materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptOutId, null);
							if(materialDepts.isEmpty()){
								//如果没有就新增一条记录
								materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptOutId, staff.getRestaurantId(), (-sActionDetail.getAmount()));
								MaterialDeptDao.insertMaterialDept(dbCon, staff, materialDept);
								//更新剩余数量
								sActionDetail.setDeptOutRemaining(materialDept.getStock());
							}else{
								MaterialDept materialDeptCut = materialDepts.get(0);
								//获取调出部门后对其进行减少
								materialDeptCut.cutStock(sActionDetail.getAmount());
								MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDeptCut);
								//更新剩余数量
								sActionDetail.setDeptOutRemaining(materialDeptCut.getStock());
							}
							
							//更新库存明细表
							StockActionDetailDao.updateStockDetail(dbCon, sActionDetail);
						}else{
							deptOutId = updateStockAction.getDeptOut().getId();
							material = MaterialDao.getById(staff, sActionDetail.getMaterialId());
							materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptOutId, null);
							if(materialDepts.isEmpty()){
								if(updateStockAction.getSubType() == SubType.LESS){
									materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptOutId, staff.getRestaurantId(), 0);
								}else{
									materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptOutId, staff.getRestaurantId(), (-sActionDetail.getAmount()));
								}
								
								MaterialDeptDao.insertMaterialDept(dbCon, staff, materialDept);
								
								//更新剩余数量
								sActionDetail.setDeptOutRemaining(materialDept.getStock());
								
							}else{
								materialDept = materialDepts.get(0);
								//出库单减少部门中库存
								materialDept.cutStock(sActionDetail.getAmount());
								//更新原料_部门表
								MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDept);
								
								//更新剩余数量
								sActionDetail.setDeptOutRemaining(materialDept.getStock());
							}
							//出库单减少总库存
							material.cutStock(sActionDetail.getAmount());
							//更新原料表
							material.setLastModStaff(staff.getName());
							MaterialDao.update(dbCon, material);
							
							//更新剩余数量
							sActionDetail.setRemaining(material.getStock());
							
							//更新库存明细表
							StockActionDetailDao.updateStockDetail(dbCon, sActionDetail);
						}
					
					}					
				}
				

			}
		}
	}
	
	/**
	 * 反审核库单
	 * @param staff 反审核人员
	 * @param builder 
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void reAuditStockAction(Staff staff, int stockInId, InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
//			dbCon.conn.setAutoCommit(false);
			reAuditStockAction(dbCon, staff, stockInId, builder);
//			dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
//			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		
	}	
	
	/**
	 * 反审核库单
	 * @param dbCon
	 * @param staff
	 * @param stockInId
	 * @throws BusinessException 
	 * @throws SQLException 
	 */
	public static void reAuditStockAction(DBCon dbCon, Staff staff, int stockInId, InsertBuilder builder) throws SQLException, BusinessException{
		
		//判断是否同个部门下进行调拨
		if((builder.getSubType() == SubType.STOCK_IN_TRANSFER || builder.getSubType() == SubType.STOCK_OUT_TRANSFER) && builder.getDeptIn().getId() == builder.getDeptOut().getId()){
			throw new BusinessException(StockError.MATERIAL_DEPT_UPDATE_EXIST);
		}
		//获取当前工作月
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		//获取月份最大天数
		int day = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		long lastDate = DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + day);
		
		
		//比较盘点时间和月结时间,取最大值
		String selectMaxDate = "SELECT MAX(date) as date FROM (SELECT  MAX(date_add(month, interval 1 MONTH)) date FROM " + Params.dbName + ".monthly_balance WHERE restaurant_id = " + staff.getRestaurantId() + 
				" UNION ALL " +
				" SELECT finish_date AS date FROM " + Params.dbName + ".stock_take WHERE restaurant_id = " + staff.getRestaurantId() + " AND status = " + StockTake.Status.AUDIT.getVal() + ") M";
		long maxDate = 0;
		dbCon.rs = dbCon.stmt.executeQuery(selectMaxDate);
		if(dbCon.rs.next()){
			if(dbCon.rs.getTimestamp("date") != null){
				maxDate = dbCon.rs.getTimestamp("date").getTime();
			}else{
				Calendar max = Calendar.getInstance();
				maxDate = DateUtil.parseDate(max.get(Calendar.YEAR) + "-" + (max.get(Calendar.MONTH)+1) + "-01");
			}
		}
		dbCon.rs.close();
		

		//货单原始时间必须大于最后一次盘点时间或月结,小于当前月最后一天
		if(builder.getOriStockDate() < maxDate){
			throw new BusinessException(StockError.STOCKACTION_TIME_LATER);

		}else if(builder.getOriStockDate() > lastDate){
			throw new BusinessException(StockError.STOCKACTION_TIME_EARLIER);
		}		
		
		
		//获取库单和detail
		StockAction updateStockAction = getStockAndDetailById(dbCon, staff, stockInId);
		int deptInId ;
		int deptOutId ;
		//还原material和materialDept
		for (StockActionDetail sActionDetail : updateStockAction.getStockDetails()) {
			MaterialDept materialDept;
			List<MaterialDept> materialDepts;
			Material material;
			//判断是库单是什么类型的
			if(updateStockAction.getSubType() == SubType.STOCK_IN || updateStockAction.getSubType() == SubType.MORE || updateStockAction.getSubType() == SubType.SPILL){
				deptInId = updateStockAction.getDeptIn().getId();

				materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptInId, null);
				
				//还原materialDept的stock
				//审核时已经添加, 所以一定有一条materialDept记录
				materialDept = materialDepts.get(0);
				//入库单增加部门库存
				materialDept.cutStock(sActionDetail.getAmount());
				//更新原料_部门表
				MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDept);
				//更新剩余数量
				sActionDetail.setDeptInRemaining(materialDept.getStock());
					
				material = MaterialDao.getById(dbCon, staff, materialDept.getMaterialId());
				//还原总库存
				material.cutStock(sActionDetail.getAmount());		
				//更新原料表
				material.setLastModStaff(staff.getName());
				//更新material
				MaterialDao.update(dbCon, material);
				
				//更新库存明细表
				sActionDetail.setRemaining(material.getStock());
				StockActionDetailDao.updateStockDetail(dbCon, sActionDetail);
			}else if(updateStockAction.getSubType() == SubType.STOCK_IN_TRANSFER || updateStockAction.getSubType() == SubType.STOCK_OUT_TRANSFER){
				deptInId = updateStockAction.getDeptIn().getId();
				deptOutId = updateStockAction.getDeptOut().getId();
				
				//还原入库调拨
				materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptInId, null);
					MaterialDept materialDeptPlus = materialDepts.get(0);
					//还原部门库存
					materialDeptPlus.cutStock(sActionDetail.getAmount());
					MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDeptPlus);
					
					//更新剩余数量
					sActionDetail.setDeptInRemaining(materialDeptPlus.getStock());
				
				//还原出库调拨
				materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptOutId, null);
					MaterialDept materialDeptCut = materialDepts.get(0);
					//还原部门库存
					materialDeptCut.plusStock(sActionDetail.getAmount());
					MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDeptCut);
					//更新剩余数量
					sActionDetail.setDeptOutRemaining(materialDeptCut.getStock());
				
				//更新库存明细表
				StockActionDetailDao.updateStockDetail(dbCon, sActionDetail);
			}else{
				deptOutId = updateStockAction.getDeptOut().getId();
				material = MaterialDao.getById(dbCon, staff, sActionDetail.getMaterialId());
				materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptOutId, null);
					materialDept = materialDepts.get(0);
					//还原部门中库存
					materialDept.plusStock(sActionDetail.getAmount());
					//更新原料_部门表
					MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDept);
					
					//更新剩余数量
					sActionDetail.setDeptOutRemaining(materialDept.getStock());
					
				//还原总库存
				material.plusStock(sActionDetail.getAmount());
				//更新原料表
				material.setLastModStaff(staff.getName());
				//更新material
				MaterialDao.update(dbCon, material);
				
				//更新剩余数量
				sActionDetail.setRemaining(material.getStock());
				
				//更新库存明细表
				StockActionDetailDao.updateStockDetail(dbCon, sActionDetail);
			}
		
		}		
		
		//修改为反审核状态
		String deptInName;
		String deptOutName;
		String SupplierName;
		
		String selectDeptIn = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDeptIn().getId() + " AND restaurant_id = " + staff.getRestaurantId();		
		dbCon.rs = dbCon.stmt.executeQuery(selectDeptIn);
		if(dbCon.rs.next()){
			deptInName = dbCon.rs.getString("name");
		}else{
			deptInName = "";
		}
		dbCon.rs.close(); 
		
		String selectDeptOut = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDeptOut().getId() + " AND restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(selectDeptOut);
		if(dbCon.rs.next()){
			deptOutName = dbCon.rs.getString("name");
		}else{
			deptOutName = "";
		}
		
		String selectSupplierName = "SELECT name FROM " + Params.dbName + ".supplier WHERE supplier_id = " + builder.getSupplier().getSupplierId() + " AND restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(selectSupplierName);
		if(dbCon.rs.next()){
			SupplierName = dbCon.rs.getString("name");
		}else{
			SupplierName = "";
		}		
		dbCon.rs.close(); 
		StockAction reAuditStockAction = builder.build();

		String sql = "UPDATE " + Params.dbName + ".stock_action SET " + 
				" ori_stock_id = '" + builder.getOriStockId() + "' " +
				", ori_stock_date = '" + DateUtil.format(builder.getOriStockDate()) + "' " +
				", comment = '" + builder.getComment() + "' " +
				", supplier_id = " + builder.getSupplier().getSupplierId() + 
				", supplier_name = '" + SupplierName + "'" +
				", dept_in = " + builder.getDeptIn().getId() + 
				", dept_in_name = '" + deptInName + "'" +
				", dept_out = " + builder.getDeptOut().getId() + 
				", dept_out_name ='" + deptOutName + "'" +
				", amount = " + reAuditStockAction.getTotalAmount() + 
				", price = " + reAuditStockAction.getTotalPrice() +
				", actual_price = " + reAuditStockAction.getActualPrice() + 
				", approver_id = " + staff.getId() + ", " +
				" approver = '" + staff.getName() + "'," +
				" approve_date = " + "'" + DateUtil.format(new Date().getTime()) + "', " +
				" status = " + StockAction.Status.DELETE.getVal() +
				" WHERE id = " + stockInId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StockError.STOCKACTION_UPDATE);
		}
		StockActionDetailDao.deleteStockDetail(dbCon, " AND stock_action_id = " + stockInId);
/*		for (StockActionDetail sDetail : builder.getStockActionDetails()) {
			Material material = MaterialDao.getById(sDetail.getMaterialId());
			if(builder.getSubType() == SubType.STOCK_IN || builder.getSubType() == SubType.SPILL || builder.getSubType() == SubType.MORE){
				material.plusStock(sDetail.getAmount());
			}else if(builder.getSubType() == SubType.STOCK_OUT || builder.getSubType() == SubType.DAMAGE || builder.getSubType() == SubType.LESS || builder.getSubType() == SubType.USE_UP){
				material.cutStock(sDetail.getAmount());
			}
			sDetail.setStockActionId(stockInId);
			sDetail.setName(material.getName());
			sDetail.setRemaining(material.getStock());
			
			StockActionDetailDao.insertStockActionDetail(dbCon, sDetail);
		}	*/	
		
		//重新计算material和materialDept
		for (StockActionDetail sActionDetail : builder.getStockActionDetails()) {
			sActionDetail.setStockActionId(stockInId);
			MaterialDept materialDept;
			List<MaterialDept> materialDepts;
			Material material = null;
			//判断是库单是什么类型的
			if(updateStockAction.getSubType() == SubType.STOCK_IN || updateStockAction.getSubType() == SubType.MORE || updateStockAction.getSubType() == SubType.SPILL){
				deptInId = updateStockAction.getDeptIn().getId();

				materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptInId, null);
				
				//还原materialDept的stock
				//审核时已经添加, 所以一定有一条materialDept记录
				materialDept = materialDepts.get(0);
				//入库单增加部门库存
				materialDept.plusStock(sActionDetail.getAmount());
				//更新原料_部门表
				MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDept);
				//更新剩余数量
				sActionDetail.setDeptInRemaining(materialDept.getStock());
					
				material = MaterialDao.getById(dbCon, staff, sActionDetail.getMaterialId());
				
				//增加总库存
				material.plusStock(sActionDetail.getAmount());		
				//更新原料表
				material.setLastModStaff(staff.getName());
				MaterialDao.update(dbCon, material);
				
				//更新库存明细表
				sActionDetail.setRemaining(material.getStock());
//				StockActionDetailDao.updateStockDetail(dbCon, sActionDetail);
			}else if(updateStockAction.getSubType() == SubType.STOCK_IN_TRANSFER || updateStockAction.getSubType() == SubType.STOCK_OUT_TRANSFER){
				deptInId = updateStockAction.getDeptIn().getId();
				deptOutId = updateStockAction.getDeptOut().getId();
				
				//还原入库调拨
				materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptInId, null);
					MaterialDept materialDeptPlus = materialDepts.get(0);
					//还原部门库存
					materialDeptPlus.plusStock(sActionDetail.getAmount());
					MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDeptPlus);
					
					//更新剩余数量
					sActionDetail.setDeptInRemaining(materialDeptPlus.getStock());
				
				//还原出库调拨
				materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptOutId, null);
					MaterialDept materialDeptCut = materialDepts.get(0);
					//还原部门库存
					materialDeptCut.cutStock(sActionDetail.getAmount());
					MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDeptCut);
					//更新剩余数量
					sActionDetail.setDeptOutRemaining(materialDeptCut.getStock());
				
				//更新库存明细表
//				StockActionDetailDao.updateStockDetail(dbCon, sActionDetail);
			}else{
				deptOutId = updateStockAction.getDeptOut().getId();

				materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptOutId, null);
					materialDept = materialDepts.get(0);
					//还原部门中库存
					materialDept.cutStock(sActionDetail.getAmount());
					//更新原料_部门表
					MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDept);
					
					//更新剩余数量
					sActionDetail.setDeptOutRemaining(materialDept.getStock());
					
				material = MaterialDao.getById(dbCon, staff, sActionDetail.getMaterialId());
				//还原总库存
				material.cutStock(sActionDetail.getAmount());
				//更新原料表
				material.setLastModStaff(staff.getName());
				MaterialDao.update(dbCon, material);
//				reCalcMaterials.add(material);
				
				//更新剩余数量
				sActionDetail.setRemaining(material.getStock());
				
				//更新库存明细表
//				StockActionDetailDao.updateStockDetail(dbCon, sActionDetail);
			}
			StockActionDetailDao.insertStockActionDetail(dbCon, staff, sActionDetail);
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
	public static StockAction getStockActionById(Staff term, int stockInId) throws SQLException, BusinessException{
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
	public static StockAction getStockActionById(DBCon dbCon, Staff term, int stockInId) throws SQLException, BusinessException{
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
	public static List<StockAction> getStockActions(Staff term, String extraCond, String orderClause) throws SQLException{
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
	public static List<StockAction> getStockActions(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException{
		List<StockAction> stockActions = new ArrayList<StockAction>();
		String sql;
		sql = "SELECT" +
				" id, restaurant_id, birth_date, ori_stock_id, ori_stock_date, dept_in, dept_in_name, dept_out, dept_out_name, supplier_id, supplier_name," +
				" operator_id, operator, amount, price, actual_price, cate_type, type, sub_type, status, comment " +
				" FROM " + Params.dbName +".stock_action " +
				" WHERE restaurant_id = " + term.getRestaurantId() +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			StockAction stockAction = new StockAction(dbCon.rs.getInt("id"));
			stockAction.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			stockAction.setBirthDate(dbCon.rs.getTimestamp("birth_date").getTime());
			stockAction.setOriStockId(dbCon.rs.getString("ori_stock_id"));
			stockAction.setOriStockDate(dbCon.rs.getTimestamp("ori_stock_date").getTime());
			stockAction.getDeptIn().setId(dbCon.rs.getShort("dept_in"));
			stockAction.getDeptIn().setName(dbCon.rs.getString("dept_in_name"));
			stockAction.getDeptOut().setId(dbCon.rs.getShort("dept_out"));
			stockAction.getDeptOut().setName(dbCon.rs.getString("dept_out_name"));
			stockAction.getSupplier().setSupplierid(dbCon.rs.getInt("supplier_id"));
			stockAction.getSupplier().setName(dbCon.rs.getString("supplier_name"));
			stockAction.setOperatorId(dbCon.rs.getInt("operator_id"));
			stockAction.setOperator(dbCon.rs.getString("operator"));
			stockAction.setAmount(dbCon.rs.getFloat("amount"));
			stockAction.setPrice(dbCon.rs.getFloat("price"));
			stockAction.setActualPrice(dbCon.rs.getFloat("actual_price"));
			stockAction.setCateType(dbCon.rs.getInt("cate_type"));
			stockAction.setType(dbCon.rs.getInt("type"));
			stockAction.setSubType(dbCon.rs.getInt("sub_type"));
			stockAction.setStatus(dbCon.rs.getInt("status"));
			stockAction.setComment(dbCon.rs.getString("comment"));
			
			stockActions.add(stockAction);
		}
		
		dbCon.rs.close();
		return stockActions;
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
	public static StockAction getStockAndDetailById(Staff term, int stockInId) throws SQLException, BusinessException{
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
	public static StockAction getStockAndDetailById(DBCon dbCon, Staff term, int stockInId) throws SQLException, BusinessException{
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
	public static List<StockAction> getStockAndDetail(Staff term, String extraCond, String orderClause) throws SQLException{
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
	public static List<StockAction> getStockAndDetail(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = "SELECT " +
				" S.id, S.restaurant_id, S.birth_date, S.ori_stock_id, S.ori_stock_date, S.dept_in, S.dept_in_name, S.dept_out, S.dept_out_name, S.supplier_id, S.supplier_name," +
				" S.operator_id, S.operator, S.approver, S.approver_id, S.approve_date, S.amount, S.price, S.actual_price, S.cate_type, S.type, S.sub_type, S.status, S.comment, " +
				" D.id, D.stock_action_id, D.material_id, D.name, D.price, D.amount as d_amount, D.remaining " +
				" FROM " + Params.dbName +".stock_action as S " +
				" LEFT JOIN " + Params.dbName + ".stock_action_detail as D " +
				" ON S.id = D.stock_action_id" +
				" WHERE S.restaurant_id = " + term.getRestaurantId() +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		Map<StockAction, StockAction> result = new LinkedHashMap<StockAction, StockAction>();
		while(dbCon.rs.next()){
			StockAction stockAction = new StockAction(dbCon.rs.getInt("S.id"));
			
			stockAction.setRestaurantId(dbCon.rs.getInt("S.restaurant_id"));
			stockAction.setBirthDate(dbCon.rs.getTimestamp("S.birth_date").getTime());
			stockAction.setOriStockId(dbCon.rs.getString("S.ori_stock_id"));
			stockAction.setOriStockDate(dbCon.rs.getTimestamp("S.ori_stock_date").getTime());
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
			stockAction.setActualPrice(dbCon.rs.getFloat("S.actual_price"));
			stockAction.setCateType(dbCon.rs.getInt("S.cate_type"));
			stockAction.setType(dbCon.rs.getInt("S.type"));
			stockAction.setSubType(dbCon.rs.getInt("S.sub_type"));
			stockAction.setStatus(dbCon.rs.getInt("S.status"));
			stockAction.setComment(dbCon.rs.getString("S.comment"));	
			
			final StockActionDetail detail;
			if(dbCon.rs.getInt("D.id") != 0){
				detail = new StockActionDetail();
				detail.setId(dbCon.rs.getInt("D.id"));
				detail.setStockActionId(dbCon.rs.getInt("D.stock_action_id"));		
				detail.setMaterialId(dbCon.rs.getInt("D.material_id"));
				detail.setName(dbCon.rs.getString("D.name"));
				detail.setPrice(dbCon.rs.getFloat("D.price"));
				detail.setAmount(dbCon.rs.getFloat("d_amount"));
				detail.setRemaining(dbCon.rs.getFloat("D.remaining"));
			}else{
				detail = null;
			}
			
			if(result.containsKey(stockAction) && detail != null){
				result.get(stockAction).addStockDetail(detail);
			}else if(!result.containsKey(stockAction)){
				if(detail != null){
					stockAction.addStockDetail(detail);
				}
				result.put(stockAction, stockAction);
			}
			
		}
		dbCon.rs.close();
		
		if(result.values().isEmpty()){
			return Collections.emptyList();
		}else{
			return new ArrayList<StockAction>(result.values());
		}
	}
	
	
	/**
	 * 获取最近一次盘点审核时间或月结时间
	 * @return
	 * @throws SQLException 
	 */
	public static long getStockActionInsertTime(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockActionInsertTime(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 获取最近一次盘点审核时间或月结时间
	 * @param dbCon
	 * @param staff
	 * @return
	 * @throws SQLException
	 */
	public static long getStockActionInsertTime(DBCon dbCon, Staff staff) throws SQLException{
		String selectMaxDate = "SELECT MAX(date) as date FROM (SELECT  MAX(date_add(month, interval 1 MONTH)) date FROM " + Params.dbName + ".monthly_balance WHERE restaurant_id = " + staff.getRestaurantId() + 
				" UNION ALL " +
				" SELECT finish_date AS date FROM " + Params.dbName + ".stock_take WHERE restaurant_id = " + staff.getRestaurantId() + " AND (status = " + StockTake.Status.AUDIT.getVal() + " OR status = " + StockTake.Status.CHECKING.getVal() + ")) M";
		dbCon.rs = dbCon.stmt.executeQuery(selectMaxDate);
		final long minDay;
		if(dbCon.rs.next()){
			if(dbCon.rs.getTimestamp("date") != null){
				minDay = dbCon.rs.getTimestamp("date").getTime();
			}else{
				minDay = 0;
			}
		}else{
			minDay = 0;
		}
		return minDay;		
		
	}
}
