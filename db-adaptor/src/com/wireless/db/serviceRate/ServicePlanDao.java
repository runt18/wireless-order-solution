package com.wireless.db.serviceRate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ServiceRateError;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.serviceRate.ServicePlan;
import com.wireless.pojo.serviceRate.ServicePlan.Status;
import com.wireless.pojo.serviceRate.ServicePlan.Type;
import com.wireless.pojo.serviceRate.ServiceRate;
import com.wireless.pojo.staffMgr.Staff;

public class ServicePlanDao {

	public static class ExtraCond{
		private int planId;
		private int regionId;
		private Status status;
		private Type type;
		
		public ExtraCond setPlanId(int planId){
			this.planId = planId;
			return this;
		}
		
		public ExtraCond setRegion(int regionId){
			this.regionId = regionId;
			return this;
		}
		
		public ExtraCond setStatus(Status status){
			this.status = status;
			return this;
		}
		
		public ExtraCond setType(Type type){
			this.type = type;
			return this;
		}
		
		String toPlan(){
			StringBuilder planCond = new StringBuilder();
			if(this.planId > 0){
				planCond.append(" AND plan_id = " + planId);
			}
			if(this.status != null){
				planCond.append(" AND status = " + status.getVal());
			}
			if(this.type != null){
				planCond.append(" AND type = " + type.getVal());
			}
			return planCond.toString();
		}
		
		String toRate(){
			StringBuilder rateCond = new StringBuilder();
			if(this.regionId > 0){
				rateCond.append(" AND SR.region_id = " + regionId);		
			}
			return rateCond.toString();
		}
	}
	
	public static enum ShowType{
		BY_PLAN("按方案显示"),
		BY_REGION("按区域显示");
		
		private final String desc;
		
		ShowType(String desc){
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return desc;
		}
	}
	
	/**
	 * Insert the service plan according to builder {@link ServiceRate#InsertBuilder}
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a service plan
	 * @return the id to service plan just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, ServicePlan.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int spId = insert(dbCon, staff, builder);
			dbCon.conn.commit();
			return spId;
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the service plan according to builder {@link ServiceRate#InsertBuilder}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a service plan
	 * @return the id to service plan just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, ServicePlan.InsertBuilder builder) throws SQLException{
		ServicePlan sp = builder.build();
		
		String sql;
		
		//Update other discount status to normal if the discount to insert is default.
		if(sp.isDefault()){
			sql = " UPDATE " + Params.dbName + ".service_plan SET " +
				  " status = " + ServicePlan.Status.NORMAL.getVal() +
				  " WHERE restaurant_id = " + staff.getRestaurantId();
			dbCon.stmt.executeUpdate(sql);
		}
		
		sql = " INSERT INTO " + Params.dbName + ".service_plan " +
			  " (restaurant_id, name, type, status) " +
			  " VALUES( " +
			  staff.getRestaurantId() + "," +
			  "'" + sp.getName() + "'," +
			  sp.getType().getVal() + "," +
			  sp.getStatus().getVal() +
			  " ) ";
		
		//Insert the service plan.
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int spId;
		if(dbCon.rs.next()){
			spId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("Failed to generated the service plan id.");
		}
		
		//Insert each service rate plan with the initial rate
		if(builder.getRate() > 0){
			sql = " INSERT INTO " +  Params.dbName + ".service_rate "	+ 
				  " (plan_id, restaurant_id, region_id, rate) VALUES ";
			int i = 0;
			for(Region region : RegionDao.getByCond(dbCon, staff, new RegionDao.ExtraCond().setStatus(Region.Status.BUSY), null)){
				sql += ( i > 0 ? "," : "");
				sql += ("(" + spId + "," + staff.getRestaurantId() + "," + region.getId() + "," + builder.getRate() + ")");
				i++;
			}
			dbCon.stmt.executeUpdate(sql);
		}		
		return spId;
	}
	
	/**
	 * Delete the service plan and associated service rate.
	 * @param staff
	 * 			the staff to perform this action
	 * @param spId
	 * 			the id of service plan to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the service plan does NOT exist
	 */
	public static void delete(Staff staff, int spId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			delete(dbCon, staff, spId);
			dbCon.conn.commit();
		}catch(SQLException | BusinessException e){
			dbCon.conn.rollback();
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the service plan and associated service rate.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param spId
	 * 			the id of service plan to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the service plan does NOT exist
	 * 			<li>throws if the service plan belongs to reserved
	 */
	public static void delete(DBCon dbCon, Staff staff, int spId) throws SQLException, BusinessException{
		String sql;
		
		sql = " SELECT type FROM " + Params.dbName + ".service_plan WHERE plan_id = " + spId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			if(ServicePlan.Type.valueOf(dbCon.rs.getInt("type")) == ServicePlan.Type.RESERVED){
				throw new BusinessException(ServiceRateError.RESERVED_SERVICE_PLAN_NOT_ALLOW_DELETE);
			}
		}
		dbCon.rs.close();
		
		sql = " DELETE FROM " + Params.dbName + ".service_plan WHERE plan_id = " + spId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(ServiceRateError.SERVICE_RATE_PLAN_NOT_EXIST);
		}
		
		sql = " DELETE FROM " + Params.dbName + ".service_rate WHERE plan_id = " + spId;
		dbCon.stmt.executeUpdate(sql);

	}
	
	/**
	 * Update the service plan according to builder {@link ServicePlan.UpdateBuilder}
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the service plan to update does NOT exist
	 */
	public static void update(Staff staff, ServicePlan.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			update(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(SQLException | BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the service plan according to builder {@link ServicePlan.UpdateBuilder}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the service plan to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, ServicePlan.UpdateBuilder builder) throws SQLException, BusinessException{
		
		ServicePlan sp = builder.build();
		
		String sql;
		
		//Update the status to this discount
		if(builder.isStatusChanged() && sp.isDefault()){
			sql = " UPDATE " + Params.dbName + ".service_plan SET status = " + ServicePlan.Status.DEFAULT.getVal() +
				  " WHERE plan_id = " + sp.getPlanId();
			dbCon.stmt.executeUpdate(sql);
			
			sql = " UPDATE " + Params.dbName + ".service_plan SET status = " + ServicePlan.Status.NORMAL.getVal() +
				  " WHERE 1 = 1 " +
				  " AND restaurant_id = " + staff.getRestaurantId() + 
				  " AND plan_id <> " + sp.getPlanId();
			dbCon.stmt.executeUpdate(sql);
		}
		
		//Update other field to this discount.
		sql = " UPDATE " + Params.dbName + ".service_plan SET " +
			  " plan_id = " + sp.getPlanId() +
			  (builder.isNameChanged() ? " ,name = '" + sp.getName() + "'" : "") +
			  " WHERE plan_id = " + sp.getPlanId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(ServiceRateError.SERVICE_RATE_PLAN_NOT_EXIST);
		}

		//Update the service rate to specific region
		for(ServiceRate eachRate : sp.getRates()){
			eachRate.setRegion(RegionDao.getById(dbCon, staff, eachRate.getRegion().getId()));
			if(eachRate.getRegion().isBusy()){
				//Check to see the region is contained in service plan before.
				sql = " SELECT IF(COUNT(*) = 0, 0, 1) AS is_exist FROM " + Params.dbName + ".service_rate " +
					  " WHERE region_id = " + eachRate.getRegion().getId() + 
					  " AND plan_id = " + sp.getPlanId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				boolean isExist = false;
				if(dbCon.rs.next()){
					isExist = dbCon.rs.getBoolean("is_exist");
				}
				dbCon.rs.close();
				
				if(isExist){
					sql = " UPDATE " + Params.dbName + ".service_rate SET rate = " + eachRate.getRate() +
						  " WHERE region_id = " + eachRate.getRegion().getId() +
						  " AND plan_id = " + sp.getPlanId();
					dbCon.stmt.executeUpdate(sql);
				}else{
					sql = " INSERT INTO " + Params.dbName + ".service_rate " +
						  " (`plan_id`, `restaurant_id`, `region_id`, `rate`) VALUES( " +
						  sp.getPlanId() + "," +
						  staff.getRestaurantId() + "," +
						  eachRate.getRegion().getId() + "," +
						  eachRate.getRate() +
						  ")";
					dbCon.stmt.executeUpdate(sql);
				}
			}
		}
	}
	
	/**
	 * Get the service rate to specific plan and region.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param planId
	 * 			the id to service plan
	 * @param region
	 * 			the region
	 * @return the service rate to plan and region
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the service plan to this region does NOT exist
	 */
	public static ServicePlan getByRegion(DBCon dbCon, Staff staff, int planId, Region region) throws SQLException, BusinessException{
		List<ServicePlan> result = getByCond(dbCon, staff, 
											 new ServicePlanDao.ExtraCond().setPlanId(planId).setRegion(region.getId()), 
 											 ShowType.BY_PLAN);
		
		if(result.isEmpty()){
			return getReservedByRegion(dbCon, staff, region);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the default rate to specific region.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param region
	 * 			the region
	 * @return the default service rate to this region
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the default service plan does NOT exist
	 */
	public static ServicePlan getDefaultByRegion(DBCon dbCon, Staff staff, Region region) throws SQLException, BusinessException{
		List<ServicePlan> result = getByCond(dbCon, staff, new ExtraCond().setStatus(Status.DEFAULT).setRegion(region.getId()), ShowType.BY_PLAN);
		if(result.isEmpty()){
			return getReservedByRegion(dbCon, staff, region);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the reserved rate to specific region.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param region
	 * 			the region
	 * @return the default service rate to this region
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the reserved service plan does NOT exist
	 */
	private static ServicePlan getReservedByRegion(DBCon dbCon, Staff staff, Region region) throws SQLException, BusinessException{
		List<ServicePlan> result = getByCond(dbCon, staff, new ExtraCond().setType(Type.RESERVED).setRegion(region.getId()), ShowType.BY_PLAN);
		if(result.isEmpty()){
			throw new BusinessException(ServiceRateError.SERVICE_RATE_PLAN_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the service plan to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param planId
	 * 			the plan id
	 * @param showType
	 * 			the show type {@link ShowType}
	 * @return the service plan to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the service plan does NOT exist
	 */
	public static ServicePlan getById(Staff staff, int planId, ShowType showType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, planId, showType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the service plan to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param planId
	 * 			the plan id
	 * @param showType
	 * 			the show type {@link ShowType}
	 * @return the service plan to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the service plan does NOT exist
	 */
	public static ServicePlan getById(DBCon dbCon, Staff staff, int planId, ShowType showType) throws SQLException, BusinessException{
		List<ServicePlan> plans = getByCond(dbCon, staff, new ExtraCond().setPlanId(planId), showType);
		if(plans.isEmpty()){
			throw new BusinessException(ServiceRateError.SERVICE_RATE_PLAN_NOT_EXIST);
		}else{
			return plans.get(0);
		}
	}
	
	/**
	 * Get the general service plan to specific restaurant.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the service plan to specific restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ServicePlan> getAll(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getAll(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the general service plan to specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the service plan to specific restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ServicePlan> getAll(DBCon dbCon, Staff staff) throws SQLException{
		return getByCond(dbCon, staff, null, null);
	}
	
	/**
	 * Get the service plan according to extra condition {@link ExtraCond}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @param showType
	 * 			the show type {@link ShowType}
	 * @return the service plan matched the extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ServicePlan> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, ShowType showType) throws SQLException{
		List<ServicePlan> result = new ArrayList<ServicePlan>();
		
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".service_plan " +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toPlan() : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			ServicePlan sp = new ServicePlan(dbCon.rs.getInt("plan_id"));
			sp.setName(dbCon.rs.getString("name"));
			sp.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			sp.setType(ServicePlan.Type.valueOf(dbCon.rs.getInt("type")));
			sp.setStatus(ServicePlan.Status.valueOf(dbCon.rs.getInt("status")));
			result.add(sp);
		}
		dbCon.rs.close();
		
		if(showType != null){
			for(ServicePlan eachPlan : result){
				if(showType == ShowType.BY_PLAN){
					sql = " SELECT " + 
						  " SR.rate_id, SR.restaurant_id, SR.rate, " +
						  " R.region_id, R.name, R.status, R.display_id " +
						  " FROM " + Params.dbName + ".service_rate SR " +
						  " JOIN " + Params.dbName + ".region R " + 
						  " ON SR.region_id = R.region_id AND SR.restaurant_id = R.restaurant_id " +
						  " WHERE SR.plan_id = " + eachPlan.getPlanId() +
						  (extraCond != null ? extraCond.toRate() : "") +
						  " ORDER BY R.display_id ";
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					while(dbCon.rs.next()){
						ServiceRate sr = new ServiceRate(dbCon.rs.getInt("rate_id"));
						sr.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
						Region region = new Region(dbCon.rs.getInt("region_id"));
						region.setName(dbCon.rs.getString("name"));
						region.setStatus(Region.Status.valueOf(dbCon.rs.getInt("status")));
						region.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
						region.setDisplayId(dbCon.rs.getInt("display_id"));
						sr.setRegion(region);
						sr.setRate(dbCon.rs.getFloat("rate"));
						eachPlan.addRate(sr);
					}
					dbCon.rs.close();
					
				}else{
					sql = " SELECT " +
						  " SR.rate_id, IF(SR.rate IS NULL, 0, 1) AS has_plan, SR.rate, " +
						  " R.region_id, R.restaurant_id, R.name, R.status, R.display_id " +
						  " FROM " + Params.dbName + ".region R " +
						  " LEFT JOIN " + Params.dbName + ".service_rate SR " +
						  " ON R.region_id = SR.region_id AND SR.plan_id = " + eachPlan.getPlanId() +
						  " WHERE 1 = 1 " +
						  " AND R.status = " + Region.Status.BUSY.getVal() + 
						  " AND R.restaurant_id = " + staff.getRestaurantId() +
  						  (extraCond != null ? extraCond.toRate() : "") +
						  " ORDER BY R.display_id ";
					
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					while(dbCon.rs.next()){
						ServiceRate sr = new ServiceRate(dbCon.rs.getInt("rate_id"));
						sr.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
						if(dbCon.rs.getBoolean("has_plan")){
							sr.setRate(dbCon.rs.getFloat("rate"));
						}else{
							sr.setRate(0);
						}
						Region region = new Region(dbCon.rs.getInt("region_id"));
						region.setName(dbCon.rs.getString("name"));
						region.setStatus(Region.Status.valueOf(dbCon.rs.getInt("status")));
						region.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
						region.setDisplayId(dbCon.rs.getInt("display_id"));
						sr.setRegion(region);
						eachPlan.addRate(sr);
					}
					dbCon.rs.close();
				}
			}
		}
		
		return result;
	}
	
}
