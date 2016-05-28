package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ModuleError;
import com.wireless.exception.StockError;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.MaterialCate;
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
import com.wireless.pojo.supplierMgr.Supplier;
import com.wireless.pojo.util.DateUtil;

public class StockActionDao {

	public static class ExtraCond{
		private int id;
		private String oriId;							//原始单号
		private StockAction.Type type;
		private List<StockAction.SubType> subTypes = new ArrayList<StockAction.SubType>();
		private List<StockAction.SubType> exceptionSubTypes = new ArrayList<StockAction.SubType>();
		private List<StockAction.Status> statuses = new ArrayList<StockAction.Status>();
		private String minOriDate;
		private String maxOriDate;
		private int deptIn;
		private int deptOut;
		private int dept;
		private MaterialCate.Type cateType;
		private int supplierId;
		private boolean containsDetail;					//是否获取库单明细
		private boolean isHistory;						//是否查询历史库单
		private boolean isCurrentMonth;					//是否查询当前会计月份
		private Staff staff;
		private String comment;
		
		ExtraCond setStaff(Staff staff){
			this.staff = staff;
			return this;
		}
		
		public ExtraCond setComment(String comment){
			this.comment = comment;
			return this;
		}
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setOriId(String oriId){
			this.oriId = oriId;
			return this;
		}
		
		public ExtraCond setSupplier(int supplierId){
			this.supplierId = supplierId;
			return this;
		}
		
		public ExtraCond setSupplier(Supplier supplier){
			this.supplierId = supplier.getId();
			return this;
		}
		
		public ExtraCond setMaterialCateType(MaterialCate.Type type){
			this.cateType = type;
			return this;
		}
		
		public ExtraCond setDept(int deptId){
			this.dept = deptId;
			return this;
		}
		
		public ExtraCond setDeptIn(int deptIn){
			this.deptIn = deptIn;
			return this;
		}
		
		public ExtraCond setDeptOut(int deptOut){
			this.deptOut = deptOut;
			return this;
		}
		
		public ExtraCond setType(StockAction.Type type){
			this.type = type;
			return this;
		}
		
		public ExtraCond setOriDate(String min, String max){
			this.minOriDate = min;
			this.maxOriDate = max;
			return this;
		}
		
		public ExtraCond addSubType(StockAction.SubType subType){
			this.subTypes.add(subType);
			return this;
		}
		
		public ExtraCond addExceptSubType(StockAction.SubType subType){
			this.exceptionSubTypes.add(subType);
			return this;
		}
		
		public ExtraCond addStatus(StockAction.Status status){
			this.statuses.add(status);
			return this;
		}
		
		public ExtraCond setContainsDetail(boolean onOff){
			this.containsDetail = onOff;
			return this;
		}
		
		public ExtraCond setCurrentMonth(boolean onOff){
			this.isCurrentMonth = true;
			return this;
		}
		
		public ExtraCond setHistory(boolean onOff){
			this.isHistory = onOff;
			return this;
		}
		
		@Override
		public String toString(){
			final StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND S.id = " + id);
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
			
			final StringBuilder exceptSubTypeCond = new StringBuilder();
			for(StockAction.SubType subType : this.exceptionSubTypes){
				if(exceptSubTypeCond.length() > 0){
					exceptSubTypeCond.append(",");
				}
				exceptSubTypeCond.append(subType.getVal());
			}
			if(exceptSubTypeCond.length() > 0){
				extraCond.append(" AND S.sub_type NOT IN ( " + exceptSubTypeCond.toString() + ")");
			}
			
			final StringBuilder statusCond = new StringBuilder();
			for(StockAction.Status status : this.statuses){
				if(statusCond.length() > 0){
					statusCond.append(",");
				}
				statusCond.append(status.getVal());
			}
			if(statusCond.length() > 0){
				extraCond.append(" AND S.status IN ( " + statusCond.toString() + ")");
			}
			
			if(this.isHistory){
				try {
					long monthly = MonthlyBalanceDao.getCurrentMonthTime(staff);
					String curmonth = new SimpleDateFormat("yyyy-MM").format(monthly);
					extraCond.append(" AND S.ori_stock_date < '" + curmonth + "'");
				} catch (SQLException | BusinessException ignored) {
					ignored.printStackTrace();
				}
			}
			
			if(this.isCurrentMonth){
				try {
					long monthly = MonthlyBalanceDao.getCurrentMonthTime(staff);
					String curmonth = new SimpleDateFormat("yyyy-MM").format(monthly);
					extraCond.append(" AND S.ori_stock_date BETWEEN '" + curmonth + "-01' AND '" + curmonth + "-31 23:59:59' ");
				} catch (SQLException | BusinessException ignored) {
					ignored.printStackTrace();
				}
			}
			
			if(minOriDate != null && maxOriDate != null){
				extraCond.append(" AND S.ori_stock_date BETWEEN '" + minOriDate + "' AND '" + maxOriDate + "'");
			}else if(minOriDate != null && maxOriDate == null){
				extraCond.append(" AND S.ori_stock_date >= '" + minOriDate + "'");
			}else if(minOriDate == null && maxOriDate != null){
				extraCond.append(" AND S.ori_stock_date <= '" + maxOriDate + "'");
			}
			
			if(this.type != null){
				extraCond.append(" AND S.type = " + type.getVal());
			}
			
			if(this.deptIn != 0){
				extraCond.append(" AND S.dept_in = " + this.deptIn);
			}
			
			if(this.deptOut != 0){
				extraCond.append(" AND S.dept_out = " + this.deptOut);
			}
			
			if(this.dept != 0){
				extraCond.append(" AND (S.dept_in = " + dept + " OR S.dept_out = " + dept + ")");
			}
			
			if(this.cateType != null){
				extraCond.append(" AND S.cate_type = " + this.cateType.getValue());
			}
			
			if(this.oriId != null){
				extraCond.append(" AND S.ori_stock_id LIKE '%" + this.oriId + "%'");
			}
			
			if(this.supplierId != 0){
				extraCond.append(" AND S.supplier_id = " + this.supplierId);
			}
			
			if(comment != null && !comment.isEmpty()){
				extraCond.append(" AND S.comment LIKE '%" + this.comment + "%' ");
			}
			
			return extraCond.toString();
		}
	}
	
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
	public static int insert(DBCon dbCon, Staff staff, InsertBuilder builder) throws SQLException, BusinessException{
		
		Restaurant restaurant = RestaurantDao.getById(dbCon, staff.getRestaurantId());
		//是否有库存模块授权&入库类型
		if(!restaurant.hasModule(Module.Code.INVENTORY) && (builder.getSubType() != StockAction.SubType.CONSUMPTION)){
			//限制添加的条数
			final int stockActionCountLimit = 50;
			String sql = "SELECT COUNT(*) FROM " + Params.dbName + ".stock_action WHERE restaurant_id = " + staff.getRestaurantId() + " AND sub_type <> " + StockAction.SubType.CONSUMPTION.getVal();
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
		
		/**
		 * 录入库单的最大值
		 * 录入的库单只能在 【当前时间】 之前
		 */
		long maxDate = DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + day);
		
		//比较 时间和月结时间,取最大值
		String sql = " SELECT MAX(date) as date FROM (SELECT  MAX(date_add(month, interval 1 MONTH)) date FROM " + Params.dbName + ".monthly_balance WHERE restaurant_id = " + staff.getRestaurantId() + 
					 " UNION ALL " +
					 " SELECT finish_date AS date FROM " + Params.dbName + ".stock_take WHERE restaurant_id = " + staff.getRestaurantId() + " AND status = " + StockTake.Status.AUDIT.getVal() + ") M";
		long minDate = 0;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			if(dbCon.rs.getTimestamp("date") != null){
				/**
				 * 录入库单的最小值
				 * 录入的库单只能在 【当前会计月份第一天】 【最后一张已经审核的盘点单】 之后
				 */
				minDate = dbCon.rs.getTimestamp("date").getTime();
			}
		}
		dbCon.rs.close();
		
		//如果是消耗类型或初始化类型的单则不需要限定时间
		if(builder.getSubType() != SubType.INIT && builder.getSubType() != SubType.CONSUMPTION && builder.getSubType() != SubType.MORE && builder.getSubType() != SubType.LESS){
			//货单原始时间必须大于最后一次已审核盘点时间或月结,小于当前月最后一天
			if(minDate != 0 && builder.getOriStockDate() < minDate){
				throw new BusinessException(StockError.STOCKACTION_TIME_LATER);

			}else if(builder.getOriStockDate() > maxDate){
				throw new BusinessException(StockError.STOCKACTION_TIME_EARLIER);
			}
		}
		//判断除了初始化, 消耗,盘盈,盘亏单外, 是否正在盘点中
		if(builder.getSubType() != SubType.INIT && builder.getSubType() != SubType.CONSUMPTION && builder.getSubType() != SubType.LESS && builder.getSubType() != SubType.MORE){
			checkStockTake(dbCon, staff);
		}		

		

		StockAction stockAction = builder.build();
		
		String deptInName;
		String deptOutName;
		String supplierName;
		
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
		dbCon.rs.close();
		
		String selectSupplierName = "SELECT name FROM " + Params.dbName + ".supplier WHERE supplier_id = " + builder.getSupplier().getId() + " AND restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(selectSupplierName);
		if(dbCon.rs.next()){
			supplierName = dbCon.rs.getString("name");
		}else{
			supplierName = "";
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
				+ stockAction.getSupplier().getId() + ", "
				+ "'" + supplierName + "', "
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
		}else{
			throw new SQLException("failed to insert stockActionDetail!");
		}
		dbCon.rs.close();
		
		//计算结存数量
		for (StockActionDetail detail : stockAction.getStockDetails()) {
			Material material = MaterialDao.getById(dbCon, staff, detail.getMaterialId());
			if(stockAction.getSubType() == SubType.STOCK_IN || stockAction.getSubType() == SubType.SPILL || stockAction.getSubType() == SubType.MORE){
				material.addStock(detail.getAmount());
			}else if(stockAction.getSubType() == SubType.STOCK_OUT || stockAction.getSubType() == SubType.DAMAGE || stockAction.getSubType() == SubType.LESS || stockAction.getSubType() == SubType.CONSUMPTION){
				material.cutStock(detail.getAmount());
			}
			//初始化库存单不进行加减
			detail.setStockActionId(stockId);
			detail.setName(material.getName());
			detail.setRemaining(material.getStock());
			
			StockActionDetailDao.insert(dbCon, staff, detail);
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
	public static int insert(Staff staff, InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int stockActionId = insert(dbCon, staff, builder);
			dbCon.conn.commit();
			return stockActionId;
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
	 * @param staff
	 * 			the staff to perform this action
	 * @param stockActionId
	 * 			the stockAction_id of stockAction
	 * @throws BusinessException
	 * 			if the stockAction_id is not exist
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static void deleteStockActionById(DBCon dbCon, Staff staff, int stockActionId) throws BusinessException, SQLException{
		if(deleteStockAction(dbCon, " AND restaurant_id = " + staff.getRestaurantId() + " AND id = " + stockActionId) == 0){
			throw new BusinessException(StockError.STOCK_ACTION_NOT_EXIST);
		}
		StockActionDetailDao.deleteByCond(dbCon, staff, new StockActionDetailDao.ExtraCond().setStockAction(stockActionId));
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
	public static void update(DBCon dbCon, Staff staff, int stockActionId, InsertBuilder builder) throws BusinessException, SQLException{
		//判断是否同个部门下进行调拨
		if((builder.getSubType() == SubType.STOCK_IN_TRANSFER || builder.getSubType() == SubType.STOCK_OUT_TRANSFER) && builder.getDeptIn().getId() == builder.getDeptOut().getId()){
			throw new BusinessException(StockError.MATERIAL_DEPT_UPDATE_EXIST);
		}
		//获取当前工作月
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		//获取月份最大天数
		int day = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		long maxDate = DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + day);
		
		
		//比较盘点时间和月结时间,取最大值
		String selectMinDate = "SELECT MAX(date) as date FROM (SELECT  MAX(date_add(month, interval 1 MONTH)) date FROM " + Params.dbName + ".monthly_balance WHERE restaurant_id = " + staff.getRestaurantId() + 
				" UNION ALL " +
				" SELECT finish_date AS date FROM " + Params.dbName + ".stock_take WHERE restaurant_id = " + staff.getRestaurantId() + " AND status = " + StockTake.Status.AUDIT.getVal() + ") M";
		long minDate = 0;
		dbCon.rs = dbCon.stmt.executeQuery(selectMinDate);
		if(dbCon.rs.next()){
			if(dbCon.rs.getTimestamp("date") != null){
				minDate = dbCon.rs.getTimestamp("date").getTime();
			}
		}
		dbCon.rs.close();
		

		//货单原始时间必须大于最后一次盘点时间或月结,小于当前月最后一天
		if(minDate != 0 && builder.getOriStockDate() < minDate){
			throw new BusinessException(StockError.STOCKACTION_TIME_LATER);

		}else if(builder.getOriStockDate() > maxDate){
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
		
		String selectSupplierName = "SELECT name FROM " + Params.dbName + ".supplier WHERE supplier_id = " + builder.getSupplier().getId() + " AND restaurant_id = " + staff.getRestaurantId();
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
				", supplier_id = " + builder.getSupplier().getId() + 
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
			throw new BusinessException(StockError.STOCK_ACTION_NOT_EXIST);
		}
		
		StockActionDetailDao.deleteByCond(dbCon, staff, new StockActionDetailDao.ExtraCond().setStockAction(stockActionId));
		
		for (StockActionDetail sDetail : builder.getStockActionDetails()) {
			Material material = MaterialDao.getById(staff, sDetail.getMaterialId());
			if(builder.getSubType() == SubType.STOCK_IN || builder.getSubType() == SubType.SPILL || builder.getSubType() == SubType.MORE){
				material.addStock(sDetail.getAmount());
			}else if(builder.getSubType() == SubType.STOCK_OUT || builder.getSubType() == SubType.DAMAGE || builder.getSubType() == SubType.LESS || builder.getSubType() == SubType.CONSUMPTION){
				material.cutStock(sDetail.getAmount());
			}
			sDetail.setStockActionId(stockActionId);
			sDetail.setName(material.getName());
			sDetail.setRemaining(material.getStock());
			
			StockActionDetailDao.insert(dbCon, staff, sDetail);
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
	public static void update(Staff term, int stockActionId, InsertBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			update(dbCon, term, stockActionId, builder);
			
			dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
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
	 * @param staff
	 * 			the staff to perform this action
	 * @param stockIn
	 * 			the stockAction to update
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement
	 * @throws BusinessException
	 * 			if the stock to update does not exist
	 */
	public static void audit(Staff staff, AuditBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			audit(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
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
	public static void audit(DBCon dbCon, Staff staff, AuditBuilder builder) throws SQLException, BusinessException{
		StockAction auditStockAction = getById(dbCon, staff, builder.getId());
		//如果操作类型不是盘亏或盘盈,则需要判断是否在盘点中
		if(auditStockAction.getSubType() != SubType.MORE && auditStockAction.getSubType() != SubType.LESS){
			isStockTakeChecking(dbCon, staff);
		}
		StockAction stockAction = builder.build();
		String sql;
		sql = " UPDATE " + Params.dbName + ".stock_action SET " +
			  " approver_id = " + stockAction.getApproverId() + ", " +
			  " approver = '" + stockAction.getApprover() + "'," +
			  " approve_date = " + "'" + (stockAction.getApproverDate() > 0 ? DateUtil.format(stockAction.getApproverDate()) : DateUtil.format(new Date().getTime())) + "', " +
			  " status = " + stockAction.getStatus().getVal() +
			  " WHERE id = " + stockAction.getId() + 
			  " AND restaurant_id = " + staff.getRestaurantId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StockError.STOCKACTION_AUDIT);
		}else{
			StockAction stock = getById(dbCon, staff, stockAction.getId(), true);
			//判断是否通过了审核
			if(stock.getStatus() == Status.AUDIT){
				int deptInId ;
				int deptOutId ;
				for (StockActionDetail stockDetail : stock.getStockDetails()) {
					MaterialDept materialDept;
					List<MaterialDept> materialDepts;
					Material material;
					//判断是库单是什么类型的
					if(stock.getSubType() == SubType.STOCK_IN || stock.getSubType() == SubType.MORE || stock.getSubType() == SubType.SPILL || stock.getSubType() == SubType.INIT){
						deptInId = stock.getDeptIn().getId();

						materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + stockDetail.getMaterialId() + " AND MD.dept_id = " + deptInId, null);
						//判断此部门下是否添加了这个原料
						if(materialDepts.isEmpty()){
							//如果没有就新增一条记录 
							materialDept = new MaterialDept(stockDetail.getMaterialId(), deptInId, staff.getRestaurantId(), stockDetail.getAmount());
							MaterialDeptDao.insertMaterialDept(staff, materialDept);
							//更新剩余数量
							stockDetail.setDeptInRemaining(stockDetail.getAmount());
							
						}else{
							materialDept = materialDepts.get(0);
							//入库单增加部门库存
							materialDept.addStock(stockDetail.getAmount());
							//更新原料_部门表
							MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDept);
							//更新剩余数量
							stockDetail.setDeptInRemaining(materialDept.getStock());
							
						}

						//取消加权平均, 改用参考单价
						material = MaterialDao.getById(dbCon, staff, materialDept.getMaterialId());
						
						if(stock.getSubType() == SubType.INIT){
							//库存初始化时设置库存量
							material.addStock(stockDetail.getAmount());
						}else{
							//入库单增加总库存
							material.addStock(stockDetail.getAmount());		
						}
						
						MaterialDao.update(dbCon, staff, new Material.UpdateBuilder(material.getId()).setIsStockOperation(true).setStock(material.getStock()).setLastModStaff(staff.getName()));
						
						//更新库存明细表
						stockDetail.setRemaining(material.getStock());
						StockActionDetailDao.update(dbCon, stockDetail);
						
					}else if(stock.getSubType() == SubType.STOCK_IN_TRANSFER || stock.getSubType() == SubType.STOCK_OUT_TRANSFER){
						deptInId = stock.getDeptIn().getId();
						deptOutId = stock.getDeptOut().getId();
						
						materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + stockDetail.getMaterialId() + " AND MD.dept_id = " + deptInId, null);
						//判断此部门下是否添加了这个原料
						if(materialDepts.isEmpty()){
							//如果没有就新增一条记录
							materialDept = new MaterialDept(stockDetail.getMaterialId(), deptInId, staff.getRestaurantId(), stockDetail.getAmount());
							MaterialDeptDao.insertMaterialDept(dbCon, staff, materialDept);
							
							//更新剩余数量
							stockDetail.setDeptInRemaining(stockDetail.getAmount());
						}else{
							MaterialDept materialDeptPlus = materialDepts.get(0);
							//入库单增加部门库存
							materialDeptPlus.addStock(stockDetail.getAmount());
							MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDeptPlus);
							
							//更新剩余数量
							stockDetail.setDeptInRemaining(materialDeptPlus.getStock());
						}
						
						materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + stockDetail.getMaterialId() + " AND MD.dept_id = " + deptOutId, null);
						if(materialDepts.isEmpty()){
							//如果没有就新增一条记录
							materialDept = new MaterialDept(stockDetail.getMaterialId(), deptOutId, staff.getRestaurantId(), (-stockDetail.getAmount()));
							MaterialDeptDao.insertMaterialDept(dbCon, staff, materialDept);
							//更新剩余数量
							stockDetail.setDeptOutRemaining(materialDept.getStock());
						}else{
							MaterialDept materialDeptCut = materialDepts.get(0);
							//获取调出部门后对其进行减少
							materialDeptCut.cutStock(stockDetail.getAmount());
							MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDeptCut);
							//更新剩余数量
							stockDetail.setDeptOutRemaining(materialDeptCut.getStock());
						}
						
						//更新库存明细表
						StockActionDetailDao.update(dbCon, stockDetail);
					}else{
						deptOutId = stock.getDeptOut().getId();
						material = MaterialDao.getById(staff, stockDetail.getMaterialId());
						materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + stockDetail.getMaterialId() + " AND MD.dept_id = " + deptOutId, null);
						if(materialDepts.isEmpty()){
							if(stock.getSubType() == SubType.LESS){
								materialDept = new MaterialDept(stockDetail.getMaterialId(), deptOutId, staff.getRestaurantId(), 0);
							}else{
								materialDept = new MaterialDept(stockDetail.getMaterialId(), deptOutId, staff.getRestaurantId(), (-stockDetail.getAmount()));
							}
							
							MaterialDeptDao.insertMaterialDept(dbCon, staff, materialDept);
							
							//更新剩余数量
							stockDetail.setDeptOutRemaining(materialDept.getStock());
							
						}else{
							materialDept = materialDepts.get(0);
							//出库单减少部门中库存
							materialDept.cutStock(stockDetail.getAmount());
							//更新原料_部门表
							MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDept);
							
							//更新剩余数量
							stockDetail.setDeptOutRemaining(materialDept.getStock());
						}
						//出库单减少总库存
						material.cutStock(stockDetail.getAmount());
						MaterialDao.update(dbCon, staff, new Material.UpdateBuilder(material.getId()).setIsStockOperation(true).setStock(material.getStock()).setLastModStaff(staff.getName()));
						
						//更新剩余数量
						stockDetail.setRemaining(material.getStock());
						
						//更新库存明细表
						StockActionDetailDao.update(dbCon, stockDetail);
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
		
		long maxDate = DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + day);
		
		
		//比较盘点时间和月结时间,取最大值
		String sql = " SELECT MAX(date) as date FROM (SELECT  MAX(date_add(month, interval 1 MONTH)) date FROM " + Params.dbName + ".monthly_balance WHERE restaurant_id = " + staff.getRestaurantId() + 
					 " UNION ALL " +
					 " SELECT finish_date AS date FROM " + Params.dbName + ".stock_take WHERE restaurant_id = " + staff.getRestaurantId() + " AND status = " + StockTake.Status.AUDIT.getVal() + ") M";
		long minDate = 0;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			if(dbCon.rs.getTimestamp("date") != null){
				minDate = dbCon.rs.getTimestamp("date").getTime();
			}
		}
		dbCon.rs.close();
		

		/**
		 * 反审核能修改库单时间  库单时间    小于【会计月份的最后一天】  大于【最后一次的月结时间】 或  【盘点时间】 
		 */
		//货单原始时间必须大于最后一次盘点时间或月结,小于当前月最后一天
		if(minDate != 0 && builder.getOriStockDate() < minDate){
			throw new BusinessException(StockError.STOCKACTION_TIME_LATER);

		}else if(builder.getOriStockDate() > maxDate){
			throw new BusinessException(StockError.STOCKACTION_TIME_EARLIER);
		}		
		
		
		//获取库单和detail
		StockAction updateStockAction = getById(dbCon, staff, stockInId, true);
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
				//更新material
				MaterialDao.update(dbCon, staff, new Material.UpdateBuilder(material.getId()).setIsStockOperation(true).setStock(material.getStock()).setLastModStaff(staff.getName()));
				
				//更新库存明细表
				sActionDetail.setRemaining(material.getStock());
				StockActionDetailDao.update(dbCon, sActionDetail);
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
					materialDeptCut.addStock(sActionDetail.getAmount());
					MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDeptCut);
					//更新剩余数量
					sActionDetail.setDeptOutRemaining(materialDeptCut.getStock());
				
				//更新库存明细表
				StockActionDetailDao.update(dbCon, sActionDetail);
			}else{
				deptOutId = updateStockAction.getDeptOut().getId();
				material = MaterialDao.getById(dbCon, staff, sActionDetail.getMaterialId());
				materialDepts = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + sActionDetail.getMaterialId() + " AND MD.dept_id = " + deptOutId, null);
					materialDept = materialDepts.get(0);
					//还原部门中库存
					materialDept.addStock(sActionDetail.getAmount());
					//更新原料_部门表
					MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDept);
					
					//更新剩余数量
					sActionDetail.setDeptOutRemaining(materialDept.getStock());
					
				//还原总库存
				material.addStock(sActionDetail.getAmount());
				//更新material
				MaterialDao.update(dbCon, staff, new Material.UpdateBuilder(material.getId()).setIsStockOperation(true).setStock(material.getStock()).setLastModStaff(staff.getName()));
				
				//更新剩余数量
				sActionDetail.setRemaining(material.getStock());
				
				//更新库存明细表
				StockActionDetailDao.update(dbCon, sActionDetail);
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
		
		String selectSupplierName = "SELECT name FROM " + Params.dbName + ".supplier WHERE supplier_id = " + builder.getSupplier().getId() + " AND restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(selectSupplierName);
		if(dbCon.rs.next()){
			SupplierName = dbCon.rs.getString("name");
		}else{
			SupplierName = "";
		}		
		dbCon.rs.close(); 
		StockAction reAuditStockAction = builder.build();

		sql = "UPDATE " + Params.dbName + ".stock_action SET " + 
				" ori_stock_id = '" + builder.getOriStockId() + "' " +
				", ori_stock_date = '" + DateUtil.format(builder.getOriStockDate()) + "' " +
				", comment = '" + builder.getComment() + "' " +
				", supplier_id = " + builder.getSupplier().getId() + 
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
				" status = " + StockAction.Status.RE_AUDIT.getVal() +
				" WHERE id = " + stockInId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StockError.STOCK_ACTION_NOT_EXIST);
		}
		
		StockActionDetailDao.deleteByCond(dbCon, staff, new StockActionDetailDao.ExtraCond().setStockAction(stockInId));
		
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
				materialDept.addStock(sActionDetail.getAmount());
				//更新原料_部门表
				MaterialDeptDao.updateMaterialDept(dbCon, staff, materialDept);
				//更新剩余数量
				sActionDetail.setDeptInRemaining(materialDept.getStock());
					
				material = MaterialDao.getById(dbCon, staff, sActionDetail.getMaterialId());
				
				//增加总库存
				material.addStock(sActionDetail.getAmount());		
				MaterialDao.update(dbCon, staff, new Material.UpdateBuilder(material.getId()).setIsStockOperation(true).setStock(material.getStock()).setLastModStaff(staff.getName()));
				
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
					materialDeptPlus.addStock(sActionDetail.getAmount());
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
				MaterialDao.update(dbCon, staff, new Material.UpdateBuilder(material.getId()).setIsStockOperation(true).setStock(material.getStock()).setLastModStaff(staff.getName()));
//				reCalcMaterials.add(material);
				
				//更新剩余数量
				sActionDetail.setRemaining(material.getStock());
				
				//更新库存明细表
//				StockActionDetailDao.updateStockDetail(dbCon, sActionDetail);
			}
			StockActionDetailDao.insert(dbCon, staff, sActionDetail);
		}			
		
	}
	
	/**
	 * Get the stock action according to id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id of stock action
	 * @param detail
	 * 			true means containing detail, otherwise false
	 * @return	the stock action to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if stock action to this id does NOT exist
	 */
	public static StockAction getById(Staff staff, int id, boolean detail) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, id, detail);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Get the stock action according to id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id of stock action
	 * @return	the stock action to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if stock action to this id does NOT exist
	 */
	public static StockAction getById(Staff staff, int id) throws SQLException, BusinessException{
		return getById(staff, id, false);
	}
	
	/**
	 * Get the stock action according to id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id of stock action
	 * @param detail
	 * 			true means containing detail, otherwise false
	 * @return	the stock action to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if stock action to this id does NOT exist
	 */
	public static StockAction getById(DBCon dbCon, Staff staff, int id, boolean detail) throws SQLException, BusinessException{
		List<StockAction> stockIns = getByCond(dbCon, staff, new ExtraCond().setId(id).setContainsDetail(detail), null);
		if(stockIns.isEmpty()){
			throw new BusinessException(StockError.STOCK_ACTION_NOT_EXIST);
		}else{
			return stockIns.get(0);
		}
		
	}
	
	/**
	 * Get the stock action according to id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id of stock action
	 * @return	the stock action to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if stock action to this id does NOT exist
	 */
	public static StockAction getById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		return getById(dbCon, staff, id, false);
	}
	
	/**
	 * Select stockIn according to extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action 
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @param SQLException
	 * 			if failed to execute any SQL statement
	 * @return	the list holding the stockIn result if successfully
	 */
	public static List<StockAction> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Select stockIn according to terminal and extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action 
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @param orderClause
	 * 			the order clause
	 * @param SQLException
	 * 			if failed to execute any 
	 * @return	the list holding the stockIn result if successfully
	 */
	public static List<StockAction> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		final List<StockAction> stockActions = new ArrayList<StockAction>();
		String sql;
		sql = " SELECT S.* " +
			  " FROM " + Params.dbName +".stock_action S " +
			  " WHERE restaurant_id = " + staff.getRestaurantId() +
			  (extraCond == null ? "" : extraCond.setStaff(staff)) +
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
			stockAction.setApprover(dbCon.rs.getString("S.approver"));
			stockAction.setApproverId(dbCon.rs.getInt("S.approver_id"));
			if(dbCon.rs.getTimestamp("S.approve_date") != null){
				stockAction.setApproverDate(dbCon.rs.getTimestamp("S.approve_date").getTime());
			}
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
		
		if(extraCond.containsDetail){
			for(StockAction stockAction : stockActions){
				stockAction.setDetails(StockActionDetailDao.getByCond(dbCon, staff, new StockActionDetailDao.ExtraCond().setStockAction(stockAction), null));
			}
		}
		
		return stockActions;
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
		String selectMaxDate = " SELECT MAX(date) as date FROM (SELECT  MAX(date_add(month, interval 1 MONTH)) date FROM " + Params.dbName + ".monthly_balance WHERE restaurant_id = " + staff.getRestaurantId() + 
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
