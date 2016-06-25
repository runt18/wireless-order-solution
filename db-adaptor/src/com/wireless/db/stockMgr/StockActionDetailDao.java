package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StockError;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.supplierMgr.Supplier;

public class StockActionDetailDao {

	public static class ExtraCond{
		private int id;
		private int materialId;								//商品或原料Id
		private List<StockAction.SubType> subTypes = new ArrayList<StockAction.SubType>();	//库单操作子类型
		private List<StockAction.Status> status = new ArrayList<StockAction.Status>();		//库单状态
		private int stockActionId;							//库单Id
		public int deptId = -1;								//出入库部门		
		private int deptIn = -1;							//入库部门
		private int deptOut = -1;							//出库部门
		private int materialCateId;							//商品或原料类型
		private MaterialCate.Type materialCateType;			//商品或原料大类
		private int supplierId;								//供应商
		private Staff staff;
		private String minOriDate;							//原始库单时间
		private String maxOriDate;
		private boolean isOnlyAmount;
		
		private ExtraCond setStaff(Staff staff){
			this.staff = staff;
			return this;
		}
		
		public ExtraCond setIsOnlyAmount(boolean onOff){
			this.isOnlyAmount = onOff;
			return this;
		}
		
		public boolean isOnlyAmount(){
			return this.isOnlyAmount;
		}
		
		public ExtraCond setOriDate(String min, String max){
			this.minOriDate = min;
			this.maxOriDate = max;
			return this;
		}
		
		public ExtraCond setSupplier(Supplier supplier){
			this.supplierId = supplier.getId();
			return this;
		}
		
		public ExtraCond setSupplier(int supplierId){
			this.supplierId = supplierId;
			return this;
		}
		
		public ExtraCond addSubType(StockAction.SubType subType){
			this.subTypes.add(subType);
			return this;
		}
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setMaterialCate(MaterialCate materialCate){
			this.materialCateId = materialCate.getId();
			return this;
		}
		
		public ExtraCond setMaterialCate(int materialCateId){
			this.materialCateId = materialCateId;
			return this;
		}

		public ExtraCond setMaterialCateType(MaterialCate.Type type){
			this.materialCateType = type;
			return this;
		}
		
		public ExtraCond setDept(Department dept){
			this.deptId = dept.getId();
			return this;
		}
		
		public ExtraCond setDept(int deptId){
			this.deptId = deptId;
			return this;
		}
		
		public ExtraCond setDeptIn(int deptIn){
			this.deptIn = deptIn;
			return this;
		}
		
		public ExtraCond setDeptIn(Department deptIn){
			this.deptIn = deptIn.getId();
			return this;
		}
		
		public ExtraCond setDeptOut(int deptOut){
			this.deptOut = deptOut;
			return this;
		}
		
		public ExtraCond setDeptOut(Department deptOut){
			this.deptIn = deptOut.getId();
			return this;
		}
		
		public ExtraCond setStockAction(StockAction stockAction){
			this.stockActionId = stockAction.getId();
			return this;
		}
		
		public ExtraCond setStockAction(int stockActionId){
			this.stockActionId = stockActionId;
			return this;
		}
		
		public ExtraCond addStatus(StockAction.Status status){
			this.status.add(status);
			return this;
		}
		
		public ExtraCond setMaterial(Material material){
			this.materialId = material.getId();
			return this;
		}
		
		public ExtraCond setMaterial(int materialId){
			this.materialId = materialId;
			return this;
		}
		
		@Override
		public String toString(){
			final StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND D.id = " + id);
			}
			if(materialId != 0){
				extraCond.append(" AND D.material_id = " + materialId);
			}
			final StringBuilder statusCond = new StringBuilder();
			for(StockAction.Status eachStatus : status){
				if(statusCond.length() > 0){
					statusCond.append(",");
				}
				statusCond.append(eachStatus.getVal());
			}
			if(statusCond.length() > 0){
				extraCond.append(" AND S.status IN ( " + statusCond.toString() + ")");
			}
			
			if(stockActionId != 0){
				extraCond.append(" AND D.stock_action_id = " + stockActionId);
			}
			
			if(deptId != -1){
				extraCond.append(" AND (S.dept_in = " + deptId + " OR S.dept_out = " + deptId + ")");
			}
			
			if(deptIn != -1){
				extraCond.append(" AND S.dept_in = " + deptIn);
			}
			
			if(deptOut != -1){
				extraCond.append(" AND S.dept_out = " + deptOut);
			}
			
			if(materialCateId != 0){
				String sql;
				sql = " SELECT material_id FROM " + Params.dbName + ".material M " +
					  " JOIN " + Params.dbName + ".material_cate MC ON M.cate_id = MC.cate_id " +
					  " WHERE MC.cate_id = " + materialCateId +
					  " AND M.restaurant_id = " + staff.getRestaurantId(); 
				extraCond.append(" AND material_id IN ( " + sql + ")");
			}
			
			if(materialCateType != null){
				String sql;
				sql = " SELECT material_id FROM " + Params.dbName + ".material M " +
					  " JOIN " + Params.dbName + ".material_cate MC ON M.cate_id = MC.cate_id " +
					  " WHERE MC.type = " + materialCateType.getValue() +
					  " AND M.restaurant_id = " + staff.getRestaurantId();
				extraCond.append(" AND material_id IN ( " + sql + ")");
			}
			
			final StringBuilder subTypeCond = new StringBuilder();
			for(StockAction.SubType subType : subTypes){
				if(subTypeCond.length() > 0){
					subTypeCond.append(",");
				}
				subTypeCond.append(subType.getVal());
			}
			if(subTypeCond.length() > 0){
				extraCond.append(" AND S.sub_type IN ( " + subTypeCond.toString() + ")");
			}
			
			if(this.supplierId != 0){
				extraCond.append(" AND S.supplier_id = " + this.supplierId);
			}
			
			if(minOriDate != null && maxOriDate != null){
				extraCond.append(" AND S.ori_stock_date BETWEEN '" + minOriDate + "' AND '" + maxOriDate + "'");
			}else if(minOriDate != null && maxOriDate == null){
				extraCond.append(" AND S.ori_stock_date >= '" + minOriDate + "'");
			}else if(minOriDate == null && maxOriDate != null){
				extraCond.append(" AND S.ori_stock_date <= '" + maxOriDate + "'");
			}
			
			return extraCond.toString();
		}
	}
	
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
	public static int insert(DBCon dbCon, Staff staff, StockActionDetail stockDetail) throws SQLException, BusinessException{
		Material material = MaterialDao.getById(dbCon, staff, stockDetail.getMaterialId());
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".stock_action_detail (material_id,name,stock_action_id, price, amount, dept_in_remaining, dept_out_remaining, remaining) " +
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
		
		try{
			if(dbCon.rs.next()){
				return dbCon.rs.getInt(1);
			}else{
				throw new SQLException("The id is not generated successfully");
			}
		}finally{
			dbCon.rs.close();
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
	public static int insert(Staff staff, StockActionDetail stockDetail) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, stockDetail);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the stock action detail to extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @param orderClause
	 * 			the order clause
	 * @return the result to stock action detail
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<StockActionDetail> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the stock action detail to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @param orderClause
	 * 			the order clause
	 * @return the result to stock action detail
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<StockActionDetail> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  (extraCond.isOnlyAmount ? " COUNT(*) " :
			  " D.id, D.stock_action_id, D.material_id, D.name, D.price, D.amount, D.dept_in_remaining, D.dept_out_remaining, D.remaining ") +
			  " FROM " + Params.dbName + ".stock_action_detail D " +
			  " JOIN " + Params.dbName + ".stock_action S ON D.stock_action_id = S.id " +
			  " WHERE 1 = 1 " +
			  " AND S.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond == null ? "" : extraCond.setStaff(staff).toString()) +
			  (orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<StockActionDetail> result = new ArrayList<StockActionDetail>();
		
		if(extraCond.isOnlyAmount){
			if(dbCon.rs.next()){
				result = Collections.nCopies(dbCon.rs.getInt(1), null);
			}else{
				result = Collections.emptyList();
			}
		}else{
			while(dbCon.rs.next()){
				StockActionDetail detail = new StockActionDetail();
				detail.setId(dbCon.rs.getInt("id"));
				detail.setStockActionId(dbCon.rs.getInt("stock_action_id"));
				detail.setMaterialId(dbCon.rs.getInt("material_id"));
				detail.setName(dbCon.rs.getString("name"));
				detail.setPrice(dbCon.rs.getFloat("price"));
				detail.setAmount(dbCon.rs.getFloat("amount"));
				detail.setDeptInRemaining(dbCon.rs.getFloat("dept_in_remaining"));
				detail.setDeptOutRemaining(dbCon.rs.getFloat("dept_out_remaining"));
				detail.setRemaining(dbCon.rs.getFloat("remaining"));
				result.add(detail);
			}
		}
		
		dbCon.rs.close();
		return result;
	}

	/**
	 * Delete the stock action detail to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the stock detail id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the stock action detail to delete does NOT exist
	 */
	public static void deleteById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the stock action detail to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the stock detail id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the stock action detail to delete does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(id)) == 0){
			throw new BusinessException(StockError.STOCK_ACTION_DETAIL_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the stock action detail to extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to stock detail deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return deleteByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the stock action detail to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to stock detail deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(StockActionDetail detail : getByCond(dbCon, staff, extraCond, null)){
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".stock_action_detail WHERE id = " + detail.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		return amount;
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
	public static void update(StockActionDetail stockDetail) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, stockDetail);
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
	public static void update(DBCon dbCon, StockActionDetail stockDetail) throws SQLException, BusinessException{
		String sql;
		sql = " UPDATE " + Params.dbName + ".stock_action_detail SET " +
			  " price = " + stockDetail.getPrice() + ", " +
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
