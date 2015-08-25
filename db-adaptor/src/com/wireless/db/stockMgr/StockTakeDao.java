package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.inventoryMgr.MaterialCateDao;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StockError;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.AuditBuilder;
import com.wireless.pojo.stockMgr.StockAction.InsertBuilder;
import com.wireless.pojo.stockMgr.StockAction.SubType;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.stockMgr.StockTake;
import com.wireless.pojo.stockMgr.StockTake.InsertStockTakeBuilder;
import com.wireless.pojo.stockMgr.StockTake.Status;
import com.wireless.pojo.stockMgr.StockTake.UpdateStockTakeBuilder;
import com.wireless.pojo.stockMgr.StockTakeDetail;
import com.wireless.pojo.util.DateUtil;
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
	 * @throws BusinessException
	 * 			if there has stockAction is not audit
	 */
	public static int insertStockTake(Staff term, InsertStockTakeBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		int stockTakeId = 0;
		try{
			dbCon.conn.setAutoCommit(false);
			stockTakeId = insertStockTake(dbCon, term, builder);
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
		return stockTakeId;
	}
	
	public static boolean isUnauditStockAction(Staff term) throws SQLException{
		List<StockAction> list = StockActionDao.getStockActions(term, 
			    " AND status = " + StockAction.Status.UNAUDIT.getVal() + 
				" AND sub_type <> " + SubType.USE_UP.getVal(), 
				null);
		if(!list.isEmpty()){
			return true ;
		}else{
			return false;
		}
	}
	
	public static void checkStockAction(Staff term) throws SQLException, BusinessException{
		if(isUnauditStockAction(term)){
			throw new BusinessException(StockError.STOCKACTION_UNAUDIT);
		}
	}
	
	/**
	 * Insert a new stockTake.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the stockTake builder to insert
	 * @return	the id of stockTake just created
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if there has stockAction is not audit  
	 */
	public static int insertStockTake(DBCon dbCon, Staff staff, InsertStockTakeBuilder builder) throws SQLException, BusinessException{
		//判断是否有未审核的库单
		checkStockAction(staff);
		//判断此部门的某个货品类型是否重复盘点
		List<StockTake> stockTakeList = getStockTakes(dbCon, staff, " AND dept_id = " + builder.getDept().getId() + " AND material_cate_id = " + builder.getCateId() + " AND status = " + Status.CHECKING.getVal(), null); 
		if(!stockTakeList.isEmpty()){
			throw new BusinessException(StockError.STOCKTAKE_HAVE_EXIST);
		}
		int cateType ;
		//盘点时选了货品小类
		if(builder.getCateId() != 0){
			MaterialCate materialCate = MaterialCateDao.getById(builder.getCateId());
			//通过小类再一次获取大类,保证准确性
			cateType = materialCate.getType().getValue();
			for (StockTakeDetail stockTakeDetail : builder.getStockTakeDetails()) {
				Material material = MaterialDao.getById(staff, stockTakeDetail.getMaterial().getId()) ;
				if(material.getCate().getId() != builder.getCateId()){
					throw new BusinessException(StockError.STOCKTAKE_NOT_MATERIAL);
				}
			}
		}else{
			cateType = builder.getCateType().getValue();
			for (StockTakeDetail stockTakeDetail : builder.getStockTakeDetails()) {
//				Map<Object, Object> params = new LinkedHashMap<Object, Object>();
//				params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MC.type = " + builder.getCateType().getValue() + " AND M.material_id = " + stockTakeDetail.getMaterial().getId() );
				List<Material> materials = MaterialDao.getByCond(staff, new MaterialDao.ExtraCond().setCateType(builder.getCateType()).setId(stockTakeDetail.getMaterial().getId()));
				if(materials.isEmpty()){
					throw new BusinessException(StockError.STOCKTAKE_NOT_MATERIAL_TYPE);
				}
			}
		}
		
		StockTake sTake = builder.build();
		String deptName;
		String MaterialCateName;
		String selectDept = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDept().getId() + " AND restaurant_id = " + staff.getRestaurantId();		
		dbCon.rs = dbCon.stmt.executeQuery(selectDept);
		if(dbCon.rs.next()){
			deptName = dbCon.rs.getString(1);
		}else{
			deptName = "";
		}
		String selectCateName = "SELECT name FROM " + Params.dbName + ".material_cate WHERE cate_id = " + sTake.getMaterialCate().getId() + " AND restaurant_id = " + staff.getRestaurantId();		
		dbCon.rs = dbCon.stmt.executeQuery(selectCateName);
		if(dbCon.rs.next()){
			MaterialCateName = dbCon.rs.getString(1);
		}else{
			MaterialCateName = "";
		}
		
		int stockTakeId;
		String sql = "INSERT INTO " + Params.dbName + ".stock_take(restaurant_id, dept_id, dept_name, " +
				"material_cate_id, material_cate_name, material_cate_type, status, operator, operator_id, start_date, comment)" +
				" VALUES( " +
				sTake.getRestaurantId() + ", " +
				sTake.getDept().getId() + ", " +
				"'" + deptName + "', " +
				sTake.getMaterialCate().getId() + ", " +
				"'" + MaterialCateName + "', " +
				cateType + ", " +
				sTake.getStatus().getVal() + ", " +
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
				StockTakeDetailDao.insertstockTakeDetail(dbCon, staff, tDetail);
			}
		}else{
			throw new SQLException("The id is not generated successfully.");
		}
		return stockTakeId;
		
	}
	/**
	 * Before Insert stockTake .
	 * @param term
	 * @return	if the system date in currentMonth, return true
	 * @throws SQLException
	 * @throws BusinessException
	 * 			if the system date not in currentMonth
	 */
	public static boolean beforeInsertStockTake(Staff term) throws SQLException, BusinessException{
		long currentDate = 0;
		currentDate = MonthlyBalanceDao.getCurrentMonthTimeByRestaurant(term.getRestaurantId());
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(currentDate));
		c.add(Calendar.MONTH, +1);
		long nowDate = new Date().getTime();
		if(c.getTime().getTime() <= nowDate || nowDate < currentDate){
			throw new BusinessException(StockError.STOCKTAKE_BEFORE_INSERT);
		}else{
			return true;
		}
		
	}
	/**
	 * Update the StockTake according to stockTakeId and InsertStockTakeBuilder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action 
	 * @param stockTakeId
	 * 			the id of this StockTake
	 * @param builder
	 * 			the StockTake to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the StockTake is not exist
	 */
	public static void updateStockTake(DBCon dbCon, Staff staff, StockTake builder) throws SQLException, BusinessException{
		//判断盘点单是否已审核
		StockTake stockTake = StockTakeDao.getStockTakeById(staff, builder.getId());
		if(stockTake.getStatus() == Status.AUDIT){
			throw new BusinessException(StockError.STOCKTAKE_UPDATE_AUDIT);
		}
		
		
		if(stockTake.getMaterialCate().getId() != 0){
			for (StockTakeDetail stockTakeDetail : builder.getStockTakeDetails()) {
				Material material = MaterialDao.getById(staff, stockTakeDetail.getMaterial().getId()) ;
				if(material.getCate().getId() != stockTake.getMaterialCate().getId()){
					throw new BusinessException(StockError.STOCKTAKE_NOT_MATERIAL);
				}
			}
		}else{
			for (StockTakeDetail stockTakeDetail : builder.getStockTakeDetails()) {
				Map<Object, Object> params = new LinkedHashMap<Object, Object>();
				params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MC.type = " + builder.getCateType().getValue() + " AND M.material_id = " + stockTakeDetail.getMaterial().getId() );
				List<Material> materials = MaterialDao.getByCond(staff, new MaterialDao.ExtraCond().setCateType(builder.getCateType()).setId(stockTakeDetail.getMaterial().getId()));
				if(materials.isEmpty()){
					throw new BusinessException(StockError.STOCKTAKE_NOT_MATERIAL_TYPE);
				}
			}
		}
		
		
		String deptName;

		String selectDept = "SELECT name FROM " + Params.dbName + ".department WHERE dept_id = " + builder.getDept().getId() + " AND restaurant_id = " + staff.getRestaurantId();		
		dbCon.rs = dbCon.stmt.executeQuery(selectDept);
		if(dbCon.rs.next()){
			deptName = dbCon.rs.getString("name");
		}else{
			deptName = "";
		}
		String sql = "UPDATE " + Params.dbName + ".stock_take" + 
					" SET dept_id = " + builder.getDept().getId() +
					", dept_name = '" + deptName + "' " +
					", comment = '" + builder.getComment() + "' " +
					" WHERE id = " + builder.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StockError.STOCKTAKE_UPDATE);
		}
		StockTakeDetailDao.deleteStockTakeDetail(dbCon, " AND stock_take_id = " + builder.getId());
		for (StockTakeDetail tDetail : builder.getStockTakeDetails()) {
			tDetail.setStockTakeId(builder.getId());
			StockTakeDetailDao.insertstockTakeDetail(dbCon, staff, tDetail);
		}
	}
	/**
	 * Update the StockTake according to stockTakeId and InsertStockTakeBuilder.
	 * @param term
	 * 			the Terminal
	 * @param stockTakeId
	 * 			the id of this StockTake
	 * @param builder
	 * 			the StockTake to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the StockTake is not exist
	 */
	public static void updateStockTake(Staff term, int stockTakeId, InsertStockTakeBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			
			dbCon.conn.setAutoCommit(false);
			StockTake stockTake = builder.build();
			stockTake.setId(stockTakeId);
			updateStockTake(dbCon, term, stockTake);
			dbCon.conn.commit();
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.conn.setAutoCommit(true);
			dbCon.disconnect();
		}
	}
	
	public static void updateStockTake(Staff term,StockTake builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			updateStockTake(dbCon, term, builder);
		}finally{
			dbCon.disconnect();
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
	public static List<StockTake> getStockTakesAndDetail(Staff term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getStockTakesAndDetail(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}	/**
	 * Get the list of stockTake according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return	the list of StockTake
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<StockTake> getStockTakesAndDetail(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException{

		String sql ;
		sql = "SELECT ST.id, ST.restaurant_id, ST.dept_id, ST.dept_name, ST.material_cate_id, ST.material_cate_name, ST.status, ST.material_cate_type, ST.operator, ST.operator_id, " +
				"ST.approver, ST.approver_id, ST.start_date, ST.finish_date, ST.comment, TD.id, TD.STock_take_id, TD.material_id, TD.name, TD.actual_amount, TD.expect_amount, TD.delta_amount" +
				" FROM " + Params.dbName + ".stock_take as ST " +
				" INNER JOIN " + Params.dbName + ".stock_take_detail as TD " +
				" ON ST.id = TD.stock_take_id " +
				" WHERE ST.restaurant_id = " + term.getRestaurantId() +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		Map<StockTake, StockTake> result = new LinkedHashMap<StockTake, StockTake>();
		
		while(dbCon.rs.next()){
			StockTake stockTake = new StockTake();
			StockTakeDetail sTakeDetail = new StockTakeDetail();
			
			sTakeDetail.setId(dbCon.rs.getInt("TD.id"));
			sTakeDetail.setStockTakeId(dbCon.rs.getInt("TD.stock_take_id"));
			sTakeDetail.setMaterialId(dbCon.rs.getInt("TD.material_id"));
			sTakeDetail.setMaterialName(dbCon.rs.getString("TD.name"));
			sTakeDetail.setActualAmount(dbCon.rs.getFloat("TD.actual_amount"));
			sTakeDetail.setExpectAmount(dbCon.rs.getFloat("TD.expect_amount"));
			sTakeDetail.setDeltaAmount(dbCon.rs.getFloat("TD.delta_amount"));
			
			stockTake.setId(dbCon.rs.getInt("ST.id"));
			stockTake.setRestaurantId(dbCon.rs.getInt("ST.restaurant_id"));
			stockTake.setDeptId(dbCon.rs.getInt("ST.dept_id"));
			stockTake.setDeptName(dbCon.rs.getString("ST.dept_name"));
			stockTake.setCateType(MaterialCate.Type.valueOf(dbCon.rs.getInt("ST.material_cate_type")));
			stockTake.setStatus(dbCon.rs.getInt("ST.status"));
			if(dbCon.rs.getInt("ST.material_cate_id") != 0){
				stockTake.getMaterialCate().setId(dbCon.rs.getInt("ST.material_cate_id"));
				stockTake.getMaterialCate().setName(dbCon.rs.getString("ST.material_cate_name"));				
			}else{
				stockTake.getMaterialCate().setId(-1);
				stockTake.getMaterialCate().setName(stockTake.getCateType() == MaterialCate.Type.GOOD ? "全部商品" : "全部原料");					
			}

			stockTake.setOperatorId(dbCon.rs.getInt("ST.operator_id"));
			stockTake.setOperator(dbCon.rs.getString("ST.operator"));
			stockTake.setApprover(dbCon.rs.getString("ST.approver"));
			stockTake.setApproverId(dbCon.rs.getInt("ST.approver_id"));
			stockTake.setStartDate(dbCon.rs.getTimestamp("ST.start_date").getTime());
			if(dbCon.rs.getTimestamp("ST.finish_date") != null){
				stockTake.setFinishDate(dbCon.rs.getTimestamp("ST.finish_date").getTime());
			}			
			stockTake.setComment(dbCon.rs.getString("ST.comment"));
			
			if(result.get(stockTake) == null){
				stockTake.addStockTakeDetail(sTakeDetail);
				result.put(stockTake, stockTake);
			}else{
				result.get(stockTake).addStockTakeDetail(sTakeDetail);
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
	public static StockTake getStockTakeAndDetailById(Staff term, int id) throws SQLException,BusinessException {
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
	public static StockTake getStockTakeAndDetailById(DBCon dbCon, Staff term, int id) throws SQLException,BusinessException{
		List<StockTake> list = getStockTakesAndDetail(dbCon, term, " AND ST.id = " + id, null);
		if(list.isEmpty()){
			throw new BusinessException(StockError.STOCKTAKE_SELECT);
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
	public static List<StockTake> getStockTakes(Staff term, String extraCond, String orderClause) throws SQLException{
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
	 * @return	the list of StockTake
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<StockTake> getStockTakes(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException{
		List<StockTake> sTakes = new ArrayList<StockTake>();
		String sql ;
		sql = "SELECT id, restaurant_id, dept_id, dept_name, material_cate_id, material_cate_name, status, material_cate_type, operator, operator_id, " +
				"approver, approver_id, start_date, finish_date, comment " +
				" FROM " + Params.dbName + ".stock_take" +
				" WHERE restaurant_id = " + term.getRestaurantId() +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			StockTake sTake = new StockTake();
			sTake.setId(dbCon.rs.getInt("id"));
			sTake.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			sTake.setDeptId(dbCon.rs.getInt("dept_id"));
			sTake.setDeptName(dbCon.rs.getString("dept_name"));
			sTake.setCateType(MaterialCate.Type.valueOf(dbCon.rs.getInt("material_cate_type")));
			sTake.setStatus(dbCon.rs.getInt("status"));
			sTake.getMaterialCate().setId(dbCon.rs.getInt("material_cate_id"));
			sTake.getMaterialCate().setName(dbCon.rs.getString("material_cate_name"));
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
	public static StockTake getStockTakeById(Staff term, int id) throws SQLException,BusinessException {
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
	public static StockTake getStockTakeById(DBCon dbCon, Staff term, int id) throws SQLException,BusinessException{
		List<StockTake> list = getStockTakes(dbCon, term, " AND id = " + id, null);
		if(list.isEmpty()){
			throw new BusinessException(StockError.STOCKTAKE_SELECT);
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
	public static List<Integer> auditStockTake(Staff term, UpdateStockTakeBuilder builder) throws SQLException,BusinessException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		List<Integer> list;
		try{
			dbCon.conn.setAutoCommit(false);
			list = auditStockTake(dbCon, term, builder);
			dbCon.conn.commit();

		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.conn.setAutoCommit(true);
			dbCon.disconnect();
		}
		
		if(list.isEmpty()){
			 return Collections.emptyList();
		}else{
			return list;
		}
	}
	/**
	 * Get the list of StockTakeDetail have not stockTake 
	 * @param staff
	 * @param stockTakeId
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static List<Material> getNotStockTakeDetail(Staff staff, int stockTakeId) throws SQLException, BusinessException{
		StockTake stockTake = getStockTakeAndDetailById(staff, stockTakeId);
		List<Material> list;
		if(stockTake.getMaterialCate().getId() == 0){
//			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
//			params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MC.type = " + stockTake.getCateType().getValue());
			list = MaterialDao.getByCond(staff, new MaterialDao.ExtraCond().setCateType(stockTake.getCateType()));
			
		}else{
//			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
//			params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MC.cate_id = " + stockTake.getMaterialCate().getId());
			list = MaterialDao.getByCond(staff, new MaterialDao.ExtraCond().setCate(stockTake.getMaterialCate().getId()));
		}
		for (StockTakeDetail stockTakeDetail : stockTake.getStockTakeDetails()) {
			for(Iterator<Material> iter = list.iterator(); iter.hasNext();){
				if(iter.next().getId() == stockTakeDetail.getMaterial().getId()){
					iter.remove();
				}
			}
		}
		return list;
	}
	/**
	 * Check if there have other material haven't stockTake  
	 * @param staff
	 * @param stockTakeId
	 * @param deptId
	 * @return the result of stockTake : 1(exist not stockTake) 0(finish stockTake)
	 * @throws SQLException
	 * @throws BusinessException
	 * 			if the some material is not exist in this department
	 */
	public static void beforeAudit(Staff staff, int stockTakeId) throws SQLException, BusinessException{
		
		StockTake stockTake = getStockTakeAndDetailById(staff, stockTakeId);
		
		if(stockTake.getMaterialCate().getId() == 0){
//			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
//			params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MC.type = " + stockTake.getCateType().getValue());
			List<Material> list = MaterialDao.getByCond(staff, new MaterialDao.ExtraCond().setCateType(stockTake.getCateType()));
			
			if(stockTake.getStockTakeDetails().size() < list.size()){
				throw new BusinessException(StockError.STOCKTAKE_DETAIL_NOT_STOCKTAKE);
			}
			
		}else{
//			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
//			params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MC.cate_id = " + stockTake.getMaterialCate().getId());
			List<Material> list = MaterialDao.getByCond(staff, new MaterialDao.ExtraCond().setCate(stockTake.getMaterialCate().getId()));
			if(stockTake.getStockTakeDetails().size() < list.size()){
				throw new BusinessException(StockError.STOCKTAKE_DETAIL_NOT_STOCKTAKE);
			}
		}
	}
	/**
	 * User choose to keep the data
	 * @param term
	 * @param stockTakeId
	 * @throws SQLException
	 * @throws BusinessException
	 * 			if the stockTake is not exist
	 */
	public static void keep(Staff staff, int stockTakeId) throws SQLException, BusinessException{
		StockTake stockTake = getStockTakeAndDetailById(staff, stockTakeId);
		List<Material> list;
		//判断是选了是否选了小类
		if(stockTake.getMaterialCate().getId() == 0){
//			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
//			params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MC.type = " + stockTake.getCateType().getValue());
			list = MaterialDao.getByCond(staff, new MaterialDao.ExtraCond().setCateType(stockTake.getCateType()));
			
		}else{
//			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
//			params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MC.cate_id = " + stockTake.getMaterialCate().getId());
			list = MaterialDao.getByCond(staff, new MaterialDao.ExtraCond().setCate(stockTake.getMaterialCate()));
		}
		List<MaterialDept> materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.dept_id = " + stockTake.getDept().getId(), null);
		//把盘漏的货品挑选出来
		for (StockTakeDetail stockTakeDetail : stockTake.getStockTakeDetails()) {
			for(Iterator<Material> iter = list.iterator(); iter.hasNext();){
				if(iter.next().getId() == stockTakeDetail.getMaterial().getId()){
					iter.remove();
				}
			}
		}
		
		//判断盘漏的货品是否在m-d表中, 有则把表中的数据填入明细
		for (MaterialDept md : materialDepts) {
			for(Iterator<Material> iter = list.iterator(); iter.hasNext();){
				if(md.getMaterialId() == iter.next().getId()){
					StockTakeDetail tDetail = new StockTakeDetail();
					tDetail.setMaterialId(md.getMaterialId());
					tDetail.setStockTakeId(stockTakeId);
					tDetail.setExpectAmount(md.getStock());
					tDetail.setActualAmount(md.getStock());
					
					
					stockTake.addStockTakeDetail(tDetail);
					iter.remove();
				}

			}
		}
		//剩下的是无m-d信息的盘漏货品, 生成新的明细
		for (Material material : list) {
			StockTakeDetail tDetail = new StockTakeDetail();
			tDetail.setMaterialId(material.getId());
			tDetail.setStockTakeId(stockTakeId);
			tDetail.setExpectAmount(0);
			tDetail.setActualAmount(0);
			
			stockTake.addStockTakeDetail(tDetail);
		}
		try{
			updateStockTake(staff, stockTake);
		}catch(Exception e){
			throw new BusinessException(StockError.STOCKTAKE_UPDATE);
		}
	}
	
	/**
	 * User choose to reset the data
	 * @param staff
	 * @param stockTakeId
	 * @throws SQLException
	 * @throws BusinessException
	 * 			if the stockTake is not exist
	 */
	public static void reset(Staff staff, int stockTakeId) throws SQLException, BusinessException{
		StockTake stockTake = getStockTakeAndDetailById(staff, stockTakeId);
		List<Material> list;
		if(stockTake.getMaterialCate().getId() == 0){
//			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
//			params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MC.type = " + stockTake.getCateType().getValue());
			list = MaterialDao.getByCond(staff, new MaterialDao.ExtraCond().setCateType(stockTake.getCateType()));
			
		}else{
//			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
//			params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MC.cate_id = " + stockTake.getMaterialCate().getId());
			list = MaterialDao.getByCond(staff, new MaterialDao.ExtraCond().setCate(stockTake.getMaterialCate()));
		}
		List<MaterialDept> materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.dept_id = " + stockTake.getDept().getId(), null);
		for (StockTakeDetail stockTakeDetail : stockTake.getStockTakeDetails()) {
			for(Iterator<Material> iter = list.iterator(); iter.hasNext();){
				if(iter.next().getId() == stockTakeDetail.getMaterial().getId()){
					iter.remove();
					break;
				}
			}
		}
		//在原有的基础上再添加明细
		
		for (MaterialDept md : materialDepts) {
			for(Iterator<Material> iter = list.iterator(); iter.hasNext();){
				if(md.getMaterialId() == iter.next().getId()){
					StockTakeDetail tDetail = new StockTakeDetail();
					tDetail.setMaterialId(md.getMaterialId());
					tDetail.setStockTakeId(stockTakeId);
					tDetail.setExpectAmount(md.getStock());
					tDetail.setActualAmount(0);
					
					stockTake.addStockTakeDetail(tDetail);
					iter.remove();
					break;
				}

			}
		}
		
		for (Material material : list) {
			StockTakeDetail tDetail = new StockTakeDetail();
			tDetail.setMaterialId(material.getId());
			tDetail.setStockTakeId(stockTakeId);
			tDetail.setExpectAmount(0);
			tDetail.setActualAmount(0);
			
			stockTake.addStockTakeDetail(tDetail);
		}
		try{
			updateStockTake(staff, stockTake);
		}catch(Exception e){
			throw new BusinessException(StockError.STOCKTAKE_UPDATE);
		}
	}

	/**
	 * Update stockTake according to UpdateBuilder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param builder
	 * 			the stockTake to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the stockTake is not exist
	 */
	
	public static List<Integer> auditStockTake(DBCon dbCon, Staff staff, UpdateStockTakeBuilder builder) throws SQLException,BusinessException{
		beforeAudit(staff, builder.getId());
		String sql;
		List<Integer> result;

		sql = "UPDATE " + Params.dbName + ".stock_take" + 
				" SET approver = " + "'" + builder.getApprover() + "', " +
				" approver_id = " + builder.getApproverId() + ", " +
				" finish_date = " + "'" + DateUtil.format(new Date().getTime()) + "', " +
				" status = " + builder.getStatus().getVal() +
				" WHERE id = " + builder.getId() + 
				" AND restaurant_id = " + staff.getRestaurantId();
	
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StockError.STOCKTAKE_DETAIL_UPDATE);
		}
		
		StockTake stockTake = getStockTakeAndDetailById(dbCon, staff, builder.getId());
		
		InsertBuilder stockActionInsertBuild = null;
		//定义库单Builder的集合
		Map<InsertBuilder, InsertBuilder> insertBuilders = new HashMap<InsertBuilder, InsertBuilder>();
		int stockActionId = 0;
		
		for (StockTakeDetail stockTakeDetail : stockTake.getStockTakeDetails()) {
			//大于0则是盘盈
			if(stockTakeDetail.getDeltaAmount() > 0){
				
				stockActionInsertBuild = StockAction.InsertBuilder.newMore(staff.getRestaurantId())
								   .setOperatorId(staff.getId()).setOperator(staff.getName())
								   .setOriStockDate(new Date().getTime())
								   .setComment(stockTake.getComment())
								   .setDeptIn(stockTake.getDept().getId())
								   .setCateType(stockTake.getCateType().getValue());
				
				Material material = MaterialDao.getById(dbCon, staff, stockTakeDetail.getMaterial().getId());
				
				//用Map方法判断builder是否存在
				if(insertBuilders.get(stockActionInsertBuild) == null){
					stockActionInsertBuild.addDetail(new StockActionDetail(material.getId(), material.getPrice(), stockTakeDetail.getTotalDelta()));
					insertBuilders.put(stockActionInsertBuild, stockActionInsertBuild);
				}else{
					insertBuilders.get(stockActionInsertBuild).addDetail(new StockActionDetail(material.getId(), material.getPrice(), stockTakeDetail.getTotalDelta()));
				}
			}else if(stockTakeDetail.getDeltaAmount() < 0){
				stockActionInsertBuild = StockAction.InsertBuilder.newLess(staff.getRestaurantId())
														   .setOperatorId(staff.getId()).setOperator(staff.getName())
														   .setOriStockDate(new Date().getTime())
														   .setComment(stockTake.getComment())
														   .setDeptOut(stockTake.getDept().getId())
														   .setCateType(stockTake.getCateType().getValue());
				//获取原料信息
				Material material = MaterialDao.getById(dbCon, staff, stockTakeDetail.getMaterial().getId());
				
				if(insertBuilders.get(stockActionInsertBuild) == null){
					stockActionInsertBuild.addDetail(new StockActionDetail(material.getId(), material.getPrice(), Math.abs(stockTakeDetail.getTotalDelta())));
					insertBuilders.put(stockActionInsertBuild, stockActionInsertBuild);
				}else{
					insertBuilders.get(stockActionInsertBuild).addDetail(new StockActionDetail(material.getId(), material.getPrice(), Math.abs(stockTakeDetail.getTotalDelta())));
				}
				
			}
		}
		
		//如果不为空,证明是有盘盈或盘亏
		if(!insertBuilders.isEmpty()){
			result = new ArrayList<Integer>();
			for (InsertBuilder InsertBuild : insertBuilders.values()) {
				stockActionId = StockActionDao.insertStockAction(dbCon, staff, InsertBuild);
				AuditBuilder updateBuilder = StockAction.AuditBuilder.newStockActionAudit(stockActionId)
											.setApproverId(staff.getId()).setApprover(staff.getName());
				StockActionDao.auditStockAction(dbCon, staff, updateBuilder);
				result.add(stockActionId);
			}
			
		}else{
			result = Collections.emptyList();
		}
		//当所有盘点任务结束后, 对消耗单进行处理
		if(!StockActionDao.isStockTakeChecking(dbCon, staff)){
			//判断是否有消耗类型的库单未审核,有则变成审核通过
			List<StockAction> list = StockActionDao.getStockActions(dbCon, staff, " AND sub_type = " + SubType.USE_UP.getVal() + " AND status = " + StockAction.Status.UNAUDIT.getVal(), null);
			if(!list.isEmpty()){
				for (StockAction useUpStockAction : list) {
					AuditBuilder updateBuilder = StockAction.AuditBuilder.newStockActionAudit(useUpStockAction.getId())
												.setApprover(useUpStockAction.getOperator()).setApproverId(useUpStockAction.getOperatorId());
					StockActionDao.auditStockAction(dbCon, staff, updateBuilder);
				}
			}
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
	public static void deleteStockTakeById(Staff term, int id) throws SQLException,BusinessException{
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
	public static void deleteStockTakeById(DBCon dbCon, Staff term, int id) throws SQLException,BusinessException{
		
		if(deleteStockTake(dbCon, term, " AND id = " + id) == 0){
			throw new BusinessException(StockError.STOCKTAKE_DELETE);
		}
		StockTakeDetailDao.deleteStockTakeDetail(" AND stock_take_id = " + id);
		
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
	public static int deleteStockTake(DBCon dbCon, Staff term, String extraCond) throws SQLException{
		String sql;
		sql = "DELETE FROM " + Params.dbName + ".stock_take" + 
				" WHERE restaurant_id = " + term.getRestaurantId() +
				(extraCond == null ? "" : extraCond);
		return dbCon.stmt.executeUpdate(sql);
	}
	
	
}
