package com.wireless.db.distributionMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.inventoryMgr.MaterialCateDao;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.DistributionError;
import com.wireless.pojo.distributionMgr.StockDistribution;
import com.wireless.pojo.distributionMgr.StockDistribution.InsertBuilder;
import com.wireless.pojo.distributionMgr.StockDistribution.Status;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.SubType;
import com.wireless.pojo.util.SortedList;

public class StockDistributionDao {
	
	public static class ExtraCond{
		private Integer id;
		private Integer stockActionId;
		private Integer stockInRestaurantId;
		private Integer stockOutRestaurantId;
		private Status status;
		private StockActionDao.ExtraCond cond4StockAction;
		private Integer associateId;
		private boolean isContainDetails;
		private boolean isGroupDistribution;
		private Integer stockRestaurant;
		private Integer fuzzyId;
		private boolean isBranchConcatGroup;
		
		private Staff staff;

		public ExtraCond setIsBranchConcatGroup(boolean onOff){
			this.isBranchConcatGroup = onOff;
			return this;
		}
		
		public ExtraCond setFuzzyId(int fuzzyId){
			this.fuzzyId = fuzzyId;
			return this;
		}
		
		public StockActionDao.ExtraCond getCond4StockAction(){
			return this.cond4StockAction;
		}
		
		public ExtraCond setIsGroupDistribution(boolean onOff){
			this.isGroupDistribution = onOff;
			return this;
		}
		
		public ExtraCond setStockRestaurant(int stockRestaurant){
			this.stockRestaurant = stockRestaurant;
			return this;
		}
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setContainDetails(boolean onOff){
			this.isContainDetails = onOff;
			return this;
		}
		
		public ExtraCond setCond4StockAction(StockActionDao.ExtraCond cond4StockAction) {
			this.cond4StockAction = cond4StockAction;
			return this;
		}

		private ExtraCond setStaff(Staff staff){
			this.staff = staff;
			return this;
		}
		
		public ExtraCond setAssociateId(int associateId){
			this.associateId = associateId;
			return this;
		}
		
		public ExtraCond setStockActionId(int stockActionId){
			this.stockActionId = stockActionId;
			return this;
		}
		
		public ExtraCond setStockInRestaurantId(int stockInRestaurantId){
			this.stockInRestaurantId = stockInRestaurantId;
			return this;
		}
		
		public ExtraCond setStockOutRestaurantId(int stockOutRestaurantId){
			this.stockOutRestaurantId = stockOutRestaurantId;
			return this;
		}
		
		public ExtraCond setStatus(Status status){
			this.status = status;
			return this;
		}
		
		public ExtraCond setStatus(int status){
			this.status = StockDistribution.Status.valueOf(status);
			return this;
		}
		
		@Override
		public String toString() {
			StringBuilder extraCond = new StringBuilder();
			
			if(this.id != null){
				extraCond.append(" AND id = " + this.id);
			}
			
			if(this.stockActionId != null){
				extraCond.append(" AND stock_action_id = " + this.stockActionId);
			}
			
			if(this.stockInRestaurantId != null){
				extraCond.append(" AND stock_in_restaurant = " + this.stockInRestaurantId);
			}
			
			if(this.stockOutRestaurantId != null){
				extraCond.append(" AND stock_out_restaurant = " + this.stockOutRestaurantId);
			}
			
			if(this.status != null){
				extraCond.append(" AND status = " + this.status.getValue());
			}
			
			if(this.associateId != null){
				extraCond.append(" AND associate_id = " + this.associateId);
			}
			
			if(this.fuzzyId != null){
				extraCond.append(" AND (id LIKE '%" + this.fuzzyId + "%' OR associate_id LIKE '%" + this.fuzzyId + "%') " );
			}
			
			if(this.stockRestaurant != null){
				extraCond.append(" AND stock_in_restaurant = " + this.stockRestaurant + " OR stock_out_restaurant = " + this.stockRestaurant);
			}
			
			if(this.cond4StockAction != null){
				try {
					final StringBuilder stockActionIds = new StringBuilder();
					List<Staff> staffs = new ArrayList<>();
					
					if(isGroupDistribution){
						Restaurant groupRestaurant = RestaurantDao.getById(staff.getRestaurantId());
						for(Restaurant restaurant : groupRestaurant.getBranches()){
							final Staff staff = new Staff();
							staff.setRestaurantId(restaurant.getId());
							staffs.add(staff);
						}
						staffs.add(staff);
					}else{
						if(isBranchConcatGroup){
							final Staff gourpStaff = new Staff();
							gourpStaff.setRestaurantId(staff.getGroupId());
							staffs.add(gourpStaff);
						}
						staffs.add(staff);
					}
					
					for(Staff branchStaff : staffs){
						
						for(StockAction stockAction : StockActionDao.getByCond(branchStaff, cond4StockAction, null)){
							if(stockActionIds.length() == 0){
								stockActionIds.append(stockAction.getId());
							}else{
								stockActionIds.append(", " + stockAction.getId());
							}
						}
					}
					
					if(stockActionIds.length() != 0){
						extraCond.append(" AND SD.stock_action_id IN ( " + stockActionIds + ")");
					}else{
						extraCond.append(" AND 1 = 2 ");
					}
				} catch (SQLException | BusinessException ignored) {
					ignored.printStackTrace();
				}
			}
			
			return extraCond.toString();
		}
	}
	
	/**
	 * insert stock_distribution by insertBuilder
	 * @param staff
	 * @param builder
	 * @return
	 * @throws SQLException
	 */
	public static int insert(Staff staff, InsertBuilder builder)throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int id = insert(dbCon, staff, builder);
			dbCon.conn.commit();
			return id;
		} catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		} catch (SQLException e) {
			dbCon.conn.rollback();
			throw e;
		}finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert stock_distribution by insert builder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 *			the insert builder {@link StockDistribution#InsertBuilder} 
	 * @return the id to stock distribution just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 *			throws if any cases below
	 *			<li>stockOut restaurant and stockIn restaurant is not in same group
	 *			<li>the previous stock distribution had not audit or reAudit
	 */
	public static int insert(DBCon dbCon, Staff staff, StockDistribution.InsertBuilder builder)throws SQLException, BusinessException{
		final StockAction stockAction = builder.getBuilder().build();
		final StockDistribution stockDistribution = builder.build();
		
		check(dbCon, stockDistribution.getStockInRestaurantId(), stockDistribution.getStockOutRestaurantId());
		
		/**
		 * 建立配送单之前  需要找到关联单号  及  要将前一张单制为已关联状态
		 */
		if(stockAction.getSubType() == SubType.DISTRIBUTION_RECEIVE){
			if(!staff.isBranch()){
				throw new BusinessException(DistributionError.RECEIVE_BUILD_ERR);
			}
			
			final Staff preRestaurantStaff = StaffDao.getAdminByRestaurant(dbCon, stockDistribution.getStockOutRestaurantId());
			final StockDistribution preStockDistribution = getByCond(dbCon, preRestaurantStaff, new ExtraCond().setId(stockDistribution.getAssociateId())
																			.setCond4StockAction(new StockActionDao.ExtraCond().addSubType(SubType.DISTRIBUTION_SEND))).get(0);
		
			final StockAction preStockAction = StockActionDao.getById(preRestaurantStaff, preStockDistribution.getStockActionId());
			if(!(preStockAction.getStatus() == StockAction.Status.AUDIT || preStockAction.getStatus() == StockAction.Status.RE_AUDIT)){
				throw new BusinessException(DistributionError.DISTRIBUTION_NOT_AUDIT);
			}
			update(dbCon, staff, StockDistribution.UpdateBuilder.newDistributionSend(preStockDistribution.getId()).setStatus(Status.MARRIED));
			
		}else if(stockAction.getSubType() == SubType.DISTRIBUTION_RETURN){
			if(!staff.isBranch()){
				throw new BusinessException(DistributionError.RETURN_BUILD_ERR);
			}
			
			final StockDistribution preStockDistribution = getByCond(dbCon, staff, new ExtraCond().setAssociateId(stockDistribution.getAssociateId())
																			.setCond4StockAction(new StockActionDao.ExtraCond().addSubType(SubType.DISTRIBUTION_RECEIVE))).get(0);
		
			final StockAction preStockAction = StockActionDao.getById(staff, preStockDistribution.getStockActionId());
			if(!(preStockAction.getStatus() == StockAction.Status.AUDIT || preStockAction.getStatus() == StockAction.Status.RE_AUDIT)){
				throw new BusinessException(DistributionError.DISTRIBUTION_NOT_AUDIT);
			}
			update(dbCon, staff, StockDistribution.UpdateBuilder.newDistributionReturn(preStockDistribution.getId()).setStatus(Status.MARRIED));
			
		}else if(stockAction.getSubType() == SubType.DISTRIBUTION_RECOVERY){
			if(!staff.isGroup()){
				throw new BusinessException(DistributionError.RECOVERY_BUILD_ERR);
			}
			
			final Staff preRestaurantStaff = StaffDao.getAdminByRestaurant(dbCon, stockDistribution.getStockOutRestaurantId());
			final StockDistribution preStockDistribution = getByCond(dbCon, preRestaurantStaff, new ExtraCond().setAssociateId(stockDistribution.getAssociateId())
																			.setCond4StockAction(new StockActionDao.ExtraCond().addSubType(SubType.DISTRIBUTION_RETURN))).get(0);
		
			final StockAction preStockAction = StockActionDao.getById(preRestaurantStaff, preStockDistribution.getStockActionId());
			if(!(preStockAction.getStatus() == StockAction.Status.AUDIT || preStockAction.getStatus() == StockAction.Status.RE_AUDIT)){
				throw new BusinessException(DistributionError.DISTRIBUTION_NOT_AUDIT);
			}
			update(dbCon, staff, StockDistribution.UpdateBuilder.newDistributionRecovery(preStockDistribution.getId()).setStatus(Status.MARRIED));
		}else if(stockAction.getSubType() == SubType.DISTRIBUTION_SEND){
			if(!staff.isGroup()){
				throw new BusinessException(DistributionError.SEND_BUILD_ERR);
			}
			
			if(stockDistribution.getAssociateId() != 0){
				final Staff preRestaurantStaff = StaffDao.getAdminByRestaurant(dbCon, stockDistribution.getStockInRestaurantId());
				final StockDistribution preStockDistribution = getByCond(dbCon, preRestaurantStaff, new ExtraCond().setId(stockDistribution.getAssociateId())
																				.setCond4StockAction(new StockActionDao.ExtraCond().addSubType(SubType.DISTRIBUTION_APPLY))).get(0);
			
				final StockAction preStockAction = StockActionDao.getById(preRestaurantStaff, preStockDistribution.getStockActionId());
				if(!(preStockAction.getStatus() == StockAction.Status.AUDIT || preStockAction.getStatus() == StockAction.Status.RE_AUDIT)){
					throw new BusinessException(DistributionError.DISTRIBUTION_NOT_AUDIT);
				}
				update(dbCon, staff, StockDistribution.UpdateBuilder.newDistributionSend(preStockDistribution.getId()).setStatus(Status.MARRIED));
			}
		}else if(stockAction.getSubType() == SubType.DISTRIBUTION_APPLY && !staff.isBranch()){
			throw new BusinessException(DistributionError.APPLY_BUILD_ERR);
		}
		
		final int stockActionId = StockActionDao.insert(dbCon, staff, builder.getBuilder());
		
		String sql = " INSERT INTO " + Params.dbName + ".stock_distribution" +
					 " (`stock_action_id`, `stock_in_restaurant`, `stock_out_restaurant`, `status`, `associate_id`)VALUES( " + 
					 stockActionId + ", " + 
					 stockDistribution.getStockInRestaurantId() + ", " + 
					 stockDistribution.getStockOutRestaurantId() + ", " + 
					 stockDistribution.getStatus().getValue() + ", " + 
					 stockDistribution.getAssociateId() +
					 ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int id = 0;
		if(dbCon.rs.next()){
			id = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		return id;
	}
	
	/**
	 * update stock_distribution by updateBuilder
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder{@link StockDistribution.UpdateBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the stockIn restaurant and stockOut restaurant is not in the same group
	 */
	public static void update(Staff staff, StockDistribution.UpdateBuilder builder)throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			update(dbCon, staff, builder);
			dbCon.conn.commit();
		} catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		} catch (BusinessException e) {
			dbCon.conn.rollback();
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * update stock_distribution by updateBuilder
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder{@link StockDistribution.UpdateBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the stockIn restaurant and stockOut restaurant is not in the same group
	 */
	public static void update(DBCon dbCon, Staff staff, StockDistribution.UpdateBuilder builder)throws SQLException, BusinessException{
		final StockDistribution stockDistribution = builder.build();
		
		//确认出库门店和入库门店是连锁
		if(stockDistribution.getStockInRestaurantId() != 0 && stockDistribution.getStockOutRestaurantId() != 0){
			check(dbCon, stockDistribution.getStockInRestaurantId(), stockDistribution.getStockOutRestaurantId());
		}
		
		//由于绑定中不需要修改stockAction所以可以跳过
		if(builder.getBuilder() != null){
			StockActionDao.update(dbCon, staff, builder.getBuilder());
		}
		
		String sql = " UPDATE " + Params.dbName + ".stock_distribution SET " +
					 " id = id " + 
					 (builder.isAssociateIdChange() ? ", associate_id = " + stockDistribution.getAssociateId() : "") + 
					 (builder.isStatusChange() ? ", status = " + stockDistribution.getStatus().getValue() : "") + 
					 (builder.isStockInRestaurantChange() ? ", stock_in_restaurant = " + stockDistribution.getStockInRestaurantId() : "") +
					 (builder.isStockOutRestaurantChange() ? ", stock_out_restaurant = " + stockDistribution.getStockOutRestaurantId() : "") +
					 " WHERE id = " + stockDistribution.getId();
	
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException("the id of stock_distribution is invaild");
		};
	}
	
	/**
	 * get the stock_distribution by id
	 * @param staff
	 * 			the staff perform this action
	 * @param id
	 * 			the id of stock_distribution
	 * @return
	 * @throws BusinessException
	 * 			throws if any case below
	 * 			<li>the id of stock_distribution is no exist
	 * @throws SQLException
	 * 			throws if failed to execute statement
	 */
	public static StockDistribution getById(Staff staff, int id) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			return getById(dbCon, staff, id);
		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * get the stock_distribution by id
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff perform this action
	 * @param id
	 * 			the id of stock_distribution
	 * @return
	 * @throws SQLException
	 * 			throws if failed to execute statement
	 * @throws BusinessException
	 * 			throws if any case below
	 * 			<li>the id of stock_distribution is no exist
	 */
	public static StockDistribution getById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		List<StockDistribution> result = getByCond(dbCon, staff, new ExtraCond().setId(id));
		if(result.size() == 0){
			throw new BusinessException(DistributionError.DISTRIBUTION_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * get the stockDistribution by extraCond
	 * @param staff
	 * 			the staff perform this action
	 * @param extraCond
	 * 			the extraCond to search stock_distribution 
	 * @return
	 * @throws SQLException
	 * 			throws if failed to execute statement
	 * @throws BusinessException
	 * 			throws if any case below
	 * 			<li>the stockActionId of stockAction is no exist
	 */
	public static List<StockDistribution> getByCond(Staff staff, ExtraCond extraCond)throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * get the stockDistribution by extraCond
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extraCond to search stock_distribution
	 * @return
	 * @throws SQLException
	 * 			throws if failed to execute statement
	 * @throws BusinessException
	 * 			throws if any case below
	 * 			<li>the stockActionId of stockAction is no exist
	 */
	public static List<StockDistribution> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond)throws SQLException, BusinessException{
		
		String sql = " SELECT SD.`id`, SD.`stock_action_id`, SD.`stock_in_restaurant`, SD.`stock_out_restaurant`, SD.`status`, SD.`associate_id` FROM " + Params.dbName + ".stock_distribution SD" + 
					 " WHERE 1 = 1 " +
					 (extraCond == null ? "" : extraCond.setStaff(staff).toString()) +
					 " ORDER BY SD.`id` DESC ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		final List<StockDistribution> result = new ArrayList<>();
		while(dbCon.rs.next()){
			final StockDistribution stockDistribution = new StockDistribution(dbCon.rs.getInt("id"));
			stockDistribution.setStockActionId(dbCon.rs.getInt("stock_action_id"));
			stockDistribution.setStockInRestaurantId(dbCon.rs.getInt("stock_in_restaurant"));
			stockDistribution.setStockOutRestaurantId(dbCon.rs.getInt("stock_out_restaurant"));
			stockDistribution.setStatus(StockDistribution.Status.valueOf(dbCon.rs.getInt("status")));
			stockDistribution.setAssociateId(dbCon.rs.getInt("associate_id"));
			result.add(stockDistribution);
		}
		dbCon.rs.close();
		
		for(StockDistribution stockDistribution : result){
			List<StockAction> stockActions = StockActionDao.getByCond(dbCon, staff, new StockActionDao.ExtraCond().setId(stockDistribution.getStockActionId()).setContainsDetail(extraCond.isContainDetails), "");
			if(stockActions.size() > 0){
				stockDistribution.setStockAction(stockActions.get(0));
			}else{
				stockDistribution.setStockAction(StockActionDao.getById(dbCon, StaffDao.getAdminByRestaurant(staff.getRestaurantId() == stockDistribution.getStockInRestaurantId() ? stockDistribution.getStockOutRestaurantId() : stockDistribution.getStockInRestaurantId()), stockDistribution.getStockActionId(), extraCond.isContainDetails));
			}
			stockDistribution.setStockInRestaurant(RestaurantDao.getById(dbCon, stockDistribution.getStockInRestaurantId()));
			stockDistribution.setStockOutRestaurant(RestaurantDao.getById(dbCon, stockDistribution.getStockOutRestaurantId()));
		}
		
		return result;
	}
	
	/**
	 * delete stock_distribution by extraCond
	 * @param staff
	 * 			the staff perform this action
	 * @param extraCond
	 * 			the extraCond to search stock_distribution 
	 * @return
	 * @throws SQLException
	 * 			throws if failed to execute statement
	 */
	public static int deleteByCond(Staff staff, ExtraCond extraCond)throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int count = deleteByCond(dbCon, staff, extraCond);
			dbCon.conn.commit();
			return count;
		} catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		} catch(SQLException e) {
			dbCon.conn.rollback();
			throw e;
		}finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * delete stock_distribution by extraCond
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff perform this action
	 * @param extraCond
	 * 			the extraCond to search stock_distribution 
	 * @return
	 * @throws SQLException
	 * 			throws if failed to execute statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond)throws SQLException, BusinessException{
		int amount = 0;
		//删除库单及删除连锁单
		for(StockDistribution stockDistribution : getByCond(dbCon, staff, extraCond)){
			
			StockActionDao.deleteStockActionById(dbCon, staff, stockDistribution.getStockActionId());
			
			String sql = " DELETE FROM " + Params.dbName + ".stock_distribution " + 
					 	 " WHERE id = " + stockDistribution.getId();
		
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		
		return amount;
	}
	
	/**
	 * delete stock_distribution by id
	 * @param dbCon
	 * @param staff
	 * @param id
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int deleteById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		return deleteByCond(dbCon, staff, new ExtraCond().setId(id));
	}
	
	/**
	 * delete stock_distribution by id
	 * @param staff
	 * @param id
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void deleteById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			deleteById(dbCon, staff, id);
			dbCon.conn.commit();
		} catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * audit by auditBuilder
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff perform this action
	 * @param builder
	 * 			the auditBuilder {@link StockDistribution.AuditBuilder}}
	 * @throws BusinessException
	 * 			throws if any case below
	 * @throws SQLException
 * 				throws if failed to execute statement
	 */
	public static void audit(DBCon dbCon, Staff staff, StockDistribution.AuditBuilder builder) throws BusinessException, SQLException{
		
		builder.setStockActionId(getById(dbCon, staff, builder.build().getId()).getStockActionId());
		
		StockAction stockAction = builder.build().getStockAction();
	
		StockActionDao.audit(dbCon, staff, StockAction.AuditBuilder.newStockActionAudit(stockAction.getId()).setApprover(staff.getName())
																									 		.setApproverId(staff.getId()));
	}
	
	/**
	 * audit by auditBuilder
	 * @param staff
	 * 			the staff perform this action
	 * @param builder
	 * 			the auditBuilder {@link StockDistribution.AuditBuilder}}
	 * @throws BusinessException
	 * 			throws if any case below
	 * @throws SQLException
	 * 			throws if failed to execute statement
	 */
	public static void audit(Staff staff, StockDistribution.AuditBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			audit(dbCon, staff, builder);
			dbCon.conn.commit();
		} catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		} catch (SQLException e) {
			dbCon.conn.rollback();
			throw e;
		}finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * reAudit by reAuditBuilder
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff perform this action
	 * @param builder
	 * 			the reauditBuilder{@link StockDistribution.ReAuditBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute statement
	 * @throws BusinessException
	 * 			throws if any case below
	 * 			<li> the stockIn restaurant and stockOut restaurant is not in the same group
	 */
	public static void reAudit(DBCon dbCon, Staff staff, StockDistribution.ReAuditBuilder builder) throws SQLException, BusinessException{
		StockDistribution stockDistribution = builder.build();
		
		if(stockDistribution.getStockInRestaurantId() != 0 && stockDistribution.getStockOutRestaurantId() != 0){
			check(dbCon, stockDistribution.getStockInRestaurantId(), stockDistribution.getStockOutRestaurantId());
		}
		StockActionDao.reAuditStockAction(dbCon, staff, builder.getBuilder());
		
		String sql = " UPDATE " + Params.dbName + ".stock_distribution SET " + 
					 " id = id " +
					 (builder.isAssociateIdChange() ? ", associate_id = " + stockDistribution.getAssociateId() : "") + 
					 (builder.isStockInRestaurantChange() ? ", stock_in_restaurant = " + stockDistribution.getStockInRestaurantId() : "") + 
					 (builder.isStockOutRestaurantChange() ? ", stock_out_restaurant = " + stockDistribution.getStockOutRestaurantId() : "") +
					 " WHERE id = " + stockDistribution.getId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException("the id of stock_distribution is invaild");
		};
	}
	
	/**
	 * reAudit by reAuditBuilder
	 * @param staff
	 * 			the staff perform this action
	 * @param builder
	 * 			the reauditBuilder{@link StockDistribution.ReAuditBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute statement
	 * @throws BusinessException
	 * 			throws if any case below
	 * 			<li> the stockIn restaurant and stockOut restaurant is not in the same group
	 */
	public static void reAudit(Staff staff, StockDistribution.ReAuditBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			reAudit(dbCon, staff, builder);
			dbCon.conn.commit();
		} catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		} catch (BusinessException e) {
			dbCon.conn.rollback();
			throw e;
		} finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 配送初始化
	 * @param staff
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void init(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			init(dbCon, staff);
			dbCon.conn.commit();
		} catch (Exception e) {
			dbCon.conn.rollback();
			throw e;
		} finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * 配送初始化
	 * @param dbCon
	 * @param staff
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void init(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		
 		if(!staff.isBranch()){
 			throw new BusinessException("不是连锁下面的门店不能进行配送初始化");
 		}
 		final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.getGroupId());
 		
 		if(MaterialDao.getByCond(dbCon, staff, new MaterialDao.ExtraCond().setHasAssociate(true)).isEmpty()){
 		
			//讲总部的类别和货品复制到分店中
			final List<MaterialCate> groupMaterialCates = MaterialCateDao.getByCond(dbCon, groupStaff, new MaterialCateDao.ExtraCond().setType(MaterialCate.Type.MATERIAL));
			
			for(MaterialCate materialCate : groupMaterialCates){
			 	int materialCateId = MaterialCateDao.insert(dbCon, staff, new MaterialCate.InsertBuilder().setName(materialCate.getName()).setType(MaterialCate.Type.MATERIAL));
			 	
			 	List<Material> groupMaterials = MaterialDao.getByCond(dbCon, groupStaff, new MaterialDao.ExtraCond().setCate(materialCate.getId()));
			 	for(Material material : groupMaterials){
			 		MaterialDao.insert(dbCon, staff, new Material.InsertBuilder().setAssociateId(material.getId())
																				 .setName(material.getName())
																				 .setMaterialCate(materialCateId)
																				 .setPrice(material.getPrice())
																				 .setLastModStaff(staff.getName()));
			 	}
			}
			
			final List<Material> groupGoods = MaterialDao.getByCond(dbCon, groupStaff, new MaterialDao.ExtraCond().setCateType(MaterialCate.Type.GOOD));
			
			int materialCateId = MaterialCateDao.insert(dbCon, staff, new MaterialCate.InsertBuilder().setName("配送商品").setType(MaterialCate.Type.MATERIAL));
			for(Material good : groupGoods){
				MaterialDao.insert(dbCon, staff, new Material.InsertBuilder().setAssociateId(good.getId())
																			 .setName(good.getName())
																			 .setMaterialCate(materialCateId)
																			 .setPrice(good.getPrice())
																			 .setLastModStaff(staff.getName()));
			}
 		}else{
 			sync(dbCon, staff);
 		}
		
		//删除门店与总店的配送单
		StockDistributionDao.deleteByCond(dbCon, staff, new StockDistributionDao.ExtraCond().setCond4StockAction(new StockActionDao.ExtraCond().addSubType(StockAction.SubType.DISTRIBUTION_APPLY)
																																			   .addSubType(StockAction.SubType.DISTRIBUTION_SEND)
																																			   .addSubType(StockAction.SubType.DISTRIBUTION_RECEIVE)
																																			   .addSubType(StockAction.SubType.DISTRIBUTION_RETURN)
																																			   .addSubType(StockAction.SubType.DISTRIBUTION_RECOVERY)));
		deleteByCond(dbCon, groupStaff, new ExtraCond().setStockRestaurant(staff.getRestaurantId())
													   .setCond4StockAction(new StockActionDao.ExtraCond().addSubType(StockAction.SubType.DISTRIBUTION_APPLY)
															   											  .addSubType(StockAction.SubType.DISTRIBUTION_SEND)
															   											  .addSubType(StockAction.SubType.DISTRIBUTION_RECEIVE)
															   											  .addSubType(StockAction.SubType.DISTRIBUTION_RETURN)
															   											  .addSubType(StockAction.SubType.DISTRIBUTION_RECOVERY)));
 	}
	
	
	/**
	 * 
	 * @param staff
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void sync(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			sync(dbCon, staff);
			dbCon.conn.commit();
		} catch (Exception e) {
			dbCon.conn.rollback();
			throw e;
		} finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void sync(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		
		if(!staff.isBranch()){
			throw new BusinessException("配送同步只能是集团下的门店才能执行..");
		}
		
		List<Material> groupMaterials = SortedList.newInstance(MaterialDao.getByCond(dbCon, StaffDao.getAdminByRestaurant(dbCon, staff.getGroupId()), null), new Comparator<Material>() {
			@Override
			public int compare(Material o1, Material o2) {
				if(o1.getId() > o2.getId()){
					return 1;
				}else if(o1.getId() < o2.getId()){
					return -1;
				}else{
					return 0;
				}
			}
		});
		List<Material> branchMaterials = MaterialDao.getByCond(dbCon, staff, null);
		
		//获取总店比分店多的货品
		final Material material2Compare = new Material(0);
		for(Material branchMaterial : branchMaterials){
			material2Compare.setId(branchMaterial.getAssociateId());
			int index4Group = groupMaterials.indexOf(material2Compare);
//			Integer index4Group = null;
//			for(Material groupMaterial : groupMaterials){
//				if(groupMaterial.getId() == branchMaterial.getAssociateId()){
//					index4Group = groupMaterials.indexOf(groupMaterial);
//					break;
//				}
//			}
			
			if(index4Group >= 0){
				groupMaterials.remove(index4Group);
			}
		}
		
		//获取同步默认的类型
		List<MaterialCate> defaultCate = MaterialCateDao.getByCond(dbCon, staff, new MaterialCateDao.ExtraCond().setCategory(MaterialCate.Category.DISTRIBUTION));
		final MaterialCate distirbutionCate;
		if(defaultCate.isEmpty()){
			distirbutionCate = MaterialCateDao.getById(dbCon, staff, MaterialCateDao.insert(dbCon, staff, new MaterialCate.InsertBuilder().setCategory(MaterialCate.Category.DISTRIBUTION)
																																				   .setName("总部配送")
																																				   .setType(MaterialCate.Type.MATERIAL))); 
		}else{
			distirbutionCate = defaultCate.get(0);
		}
		
		for(Material material : groupMaterials){
			MaterialDao.insert(dbCon, staff, new Material.InsertBuilder().setAssociateId(material.getId())
																		 .setName(material.getName())
																		 .setMaterialCate(distirbutionCate)
																		 .setPrice(material.getPrice())
																		 .setLastModStaff(staff.getName()));
		}
	}
	
	/**
	 * check for the stockOut restaurant and stockIn restaurant  is group
	 * @param dbCon
	 * 			the database connection
	 * @param stockIn
	 * 			the stock in restaurant of the action
	 * @param stockOut
	 * 			the stock out restaurant of the action
	 * @throws BusinessException
	 * 			throws if any case below
	 * 			<li> the stockIn restaurant and stockOut restaurant is not in the same group
	 * @throws SQLException
	 * 			throws if failed to execute statement
	 */
	private static void check(DBCon dbCon, int stockIn, int stockOut) throws BusinessException, SQLException{
		Staff stockInStaff = StaffDao.getAdminByRestaurant(dbCon, stockIn);
		Staff stockOutStaff = StaffDao.getAdminByRestaurant(dbCon, stockOut);
		
		if(!((stockInStaff.isGroup() && (stockInStaff.getRestaurantId() == stockOutStaff.getGroupId())) || (stockOutStaff.isGroup() && (stockInStaff.getGroupId() == stockOutStaff.getRestaurantId())))){
			throw new BusinessException(DistributionError.RESTAURANT_NOT_GROUP);
		}
	}
}
