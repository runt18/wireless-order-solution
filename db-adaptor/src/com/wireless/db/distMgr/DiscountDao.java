package com.wireless.db.distMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.DiscountError;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.distMgr.Discount.Status;
import com.wireless.pojo.distMgr.Discount.Type;
import com.wireless.pojo.distMgr.DiscountPlan;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;

public class DiscountDao {
	
	public static enum ShowType{
		BY_PLAN("按方案显示"),
		BY_KITCHEN("按厨房显示");
		
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
	 * Get the default the discount to a specific restaurant.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the default discount to this restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Discount getDefault(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getDefault(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the default the discount to a specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the default discount to this restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Discount getDefault(DBCon dbCon, Staff staff) throws SQLException{
		List<Discount> result = getByCond(dbCon, staff, " AND DIST.status = " + Discount.Status.DEFAULT.getVal() + ")", null, ShowType.BY_PLAN);
		if(result.isEmpty()){
			return getByCond(dbCon, staff, " AND DIST.status = " + Discount.Type.RESERVED.getVal(), null, ShowType.BY_PLAN).get(0);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the discounts to specific member type.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberTypeId
	 * 			the id to member type
	 * @return the discounts to this member type
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member type does NOT exist
	 */
	public static List<Discount> getByMemberType(DBCon dbCon, Staff staff, int memberTypeId) throws SQLException, BusinessException{
		String sql;
		sql = " SELECT discount_id FROM " + Params.dbName + ".member_type_discount WHERE member_type_id = " + memberTypeId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			dbCon.rs.close();
			return getByCond(dbCon, staff, " AND DIST.discount_id IN (" + sql + ")", null, ShowType.BY_PLAN);
		}else{
			return Collections.emptyList();
		}
	}
	
	/**
	 * Get the discounts to specific member type.
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberTypeId
	 * 			the id to member type
	 * @return the discounts to this member type
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member type does NOT exist
	 */
	public static List<Discount> getByMemberType(Staff staff, int memberTypeId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByMemberType(dbCon, staff, memberTypeId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the discounts to specific role.
	 * @param staff
	 * 			the staff to perform this action
	 * @param role
	 * 			the role to discounts
	 * @return the discounts to a role staff
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Discount> getByRole(Staff staff, Role role) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByRole(dbCon, staff, role);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the discounts to specific role.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param role
	 * 			the role to discounts
	 * @return the discounts to a role staff
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Discount> getByRole(DBCon dbCon, Staff staff, Role role) throws SQLException{
		String sql;
		sql = " SELECT discount_id FROM " + Params.dbName + ".role_discount WHERE role_id = " + role.getId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			dbCon.rs.close();
			return getByCond(dbCon, staff, " AND DIST.discount_id IN (" + sql + ")", null, ShowType.BY_PLAN);
		}else{
			dbCon.rs.close();
			return getByCond(dbCon, staff, null, null, ShowType.BY_PLAN);
		}
	}
	
	/**
	 * Get the discount according to a specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param discountId
	 * 			the discount id
	 * @return the discount to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the discount to this id does NOT exist
	 */
	public static Discount getById(Staff staff, int discountId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, discountId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the discount according to a specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param discountId
	 * 			the discount id
	 * @return the discount to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the discount to this id does NOT exist
	 */
	public static Discount getById(DBCon dbCon, Staff staff, int discountId) throws SQLException, BusinessException{
		return getById(dbCon, staff, discountId, ShowType.BY_PLAN);
	}
	
	/**
	 * Get the discount according to a specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param discountId
	 * 			the discount id
	 * @param showType
	 * 			the show type {@link ShowType}
	 * @return the discount to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the discount to this id does NOT exist
	 */
	public static Discount getById(Staff staff, int discountId, ShowType showType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, discountId, showType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the discount according to a specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param discountId
	 * 			the discount id
	 * @param showType
	 * 			the show type {@link ShowType}
	 * @return the discount to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the discount to this id does NOT exist
	 */
	public static Discount getById(DBCon dbCon, Staff staff, int discountId, ShowType showType) throws SQLException, BusinessException{
		List<Discount> result = getByCond(dbCon, staff, " AND DIST.discount_id = " + discountId, null, showType);
		if(result.isEmpty()){
			throw new BusinessException(DiscountError.DISCOUNT_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the discount to specific restaurant defined in staff.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the discounts to specific restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Discount> getAll(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getAll(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the discount to specific restaurant defined in staff.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the discounts to specific restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Discount> getAll(DBCon dbCon, Staff staff) throws SQLException{
		return getByCond(dbCon, staff, null, null, ShowType.BY_PLAN);
	}
	
	/**
	 * Get the discount along with its discount plan to specified extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the discount along with its discount plan to specified extra condition 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static List<Discount> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause, ShowType showType) throws SQLException{
		
		List<Discount> result = getPureByCond(dbCon, staff, extraCond, orderClause);
		for(Discount each : result){
			String sql;
			if(showType == ShowType.BY_PLAN){
				sql = " SELECT " +
					  " DIST_PLAN.dist_plan_id, DIST_PLAN.kitchen_id, DIST_PLAN.rate, " +
					  " KITCHEN.name AS kitchen_name, KITCHEN.display_id " +
					  " FROM " + Params.dbName + ".discount_plan DIST_PLAN " +
					  " JOIN " + Params.dbName + ".kitchen KITCHEN ON DIST_PLAN.kitchen_id = KITCHEN.kitchen_id " +
					  " WHERE 1 = 1 " +
					  " AND DIST_PLAN.discount_id = " + each.getId();
				
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				while(dbCon.rs.next()){
					if(dbCon.rs.getFloat("rate") != 1){
						DiscountPlan dp = new DiscountPlan(dbCon.rs.getInt("dist_plan_id"));
						
						dp.setRate(dbCon.rs.getFloat("rate"));
						
						Kitchen k = new Kitchen(dbCon.rs.getInt("kitchen_id"));
						k.setRestaurantId(staff.getRestaurantId());
						k.setDisplayId(dbCon.rs.getShort("display_id"));
						k.setName(dbCon.rs.getString("kitchen_name"));
						dp.setKitchen(k);
						
						each.addPlan(dp);
					}
				}
				dbCon.rs.close();
				
			}else if(showType == ShowType.BY_KITCHEN){
				sql = " SELECT " +
					  " KITCHEN.name AS kitchen_name, KITCHEN.display_id, " +
					  " DEPT.dept_id, DEPT.name AS dept_name, DEPT.display_id AS dept_display_id, " +
					  " DIST_PLAN.dist_plan_id, DIST_PLAN.kitchen_id, DIST_PLAN.rate, IF(DIST_PLAN.rate IS NULL, 0, 1) AS has_plan " +
					  " FROM " + Params.dbName + ".kitchen KITCHEN " +
					  " JOIN " + Params.dbName + ".department DEPT ON KITCHEN.dept_id = DEPT.dept_id AND KITCHEN.restaurant_id = DEPT.restaurant_id " + 
					  " LEFT JOIN " + Params.dbName + ".discount_plan DIST_PLAN ON DIST_PLAN.kitchen_id = KITCHEN.kitchen_id AND DIST_PLAN.discount_id = " + each.getId() +
					  " WHERE 1 = 1 " +
					  " AND KITCHEN.type = " + Kitchen.Type.NORMAL.getVal() +
					  " AND KITCHEN.restaurant_id = " + staff.getRestaurantId() +
					  " ORDER BY DEPT.display_id, KITCHEN.display_id ";
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				while(dbCon.rs.next()){
					float rate;
					if(dbCon.rs.getBoolean("has_plan")){
						rate = dbCon.rs.getFloat("rate");
					}else{
						rate = 1;
					}
					DiscountPlan dp = new DiscountPlan(dbCon.rs.getInt("dist_plan_id"));
					
					dp.setRate(rate);
					
					Kitchen k = new Kitchen(dbCon.rs.getInt("kitchen_id"));
					k.setRestaurantId(staff.getRestaurantId());
					k.setDisplayId(dbCon.rs.getShort("display_id"));
					k.setName(dbCon.rs.getString("kitchen_name"));
					
					Department d = new Department(dbCon.rs.getInt("dept_id"));
					d.setName(dbCon.rs.getString("dept_name"));
					d.setDisplayId(dbCon.rs.getInt("dept_display_id"));
					k.setDept(d);
					
					dp.setKitchen(k);
					
					each.addPlan(dp);
				}
				dbCon.rs.close();	  
			}
		}
		
		return result;
	}
	
	/**
	 * Get the pure discount without details to specific restaurant.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition.
	 * @param orderClause
	 * 			the order clause.
	 * @return the list holding the pure discount info
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Discount> getPureAll(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureAll(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the pure discount without details to specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition.
	 * @param orderClause
	 * 			the order clause.
	 * @return the list holding the pure discount info
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Discount> getPureAll(DBCon dbCon, Staff staff) throws SQLException{
		return getPureByCond(dbCon, staff, null, null);
	}
	
	/**
	 * Get the pure discount without details to specific extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition.
	 * @param orderClause
	 * 			the order clause.
	 * @return the list holding the pure discount info
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static List<Discount> getPureByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " DIST.discount_id, DIST.restaurant_id, DIST.name AS dist_name, DIST.status, DIST.type " +
			  " FROM " +  Params.dbName + ".discount DIST " +
			  " WHERE 1=1 " +
			  " AND DIST.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond == null ? "" : extraCond) + " " +
			  (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<Discount> result = new ArrayList<Discount>();
		while(dbCon.rs.next()){
			Discount discount = new Discount(dbCon.rs.getInt("discount_id"));
			discount.setName(dbCon.rs.getString("dist_name"));
			discount.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			discount.setStatus(dbCon.rs.getInt("status"));
			discount.setType(Type.valueOf(dbCon.rs.getInt("type")));
			result.add(discount);
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 */
	public static List<DiscountPlan> getDiscountPlan(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		List<DiscountPlan> list = new ArrayList<DiscountPlan>();
		DiscountPlan item = null;
		String selectSQL = "SELECT A.dist_plan_id, A.rate, B.discount_id, B.name as discount_name, B.type, B.restaurant_id, B.status, K.kitchen_id, K.name as kitchen_name "
				+ " FROM " +  Params.dbName + ".discount_plan A LEFT JOIN " +  Params.dbName + ".kitchen K ON A.kitchen_id = K.kitchen_id, " +  Params.dbName + ".discount B "
				+ " WHERE A.discount_id = B.discount_id "
				+ (extraCond == null ? "" : extraCond) 
				+ " " 
				+ (orderClause == null ? "" : orderClause);

		dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
		
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new DiscountPlan(dbCon.rs.getInt("dist_plan_id"));
			
			item.setRate(dbCon.rs.getFloat("rate"));
			
			item.getDiscount().setId(dbCon.rs.getInt("discount_id"));
			item.getDiscount().setName(dbCon.rs.getString("discount_name"));
			item.getDiscount().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			item.getDiscount().setType(Type.valueOf(dbCon.rs.getInt("type")));
			item.getDiscount().setStatus(dbCon.rs.getInt("status"));
			
			item.getKitchen().setId(dbCon.rs.getInt("kitchen_id"));
			item.getKitchen().setName(dbCon.rs.getString("kitchen_name"));
			
			list.add(item);
			item = null;
		}
		return list;
	}
	
	/**
	 * 
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 */
	public static List<DiscountPlan> getDiscountPlan(String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return DiscountDao.getDiscountPlan(dbCon, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param pojo
	 * @throws Exception
	 */
	public static void checkUpdateOrInsertDiscount(DBCon dbCon, Discount pojo) throws SQLException{
		if(pojo.isDefault()){
			Discount opojo = null;
			String oSQL = "SELECT discount_id, status, restaurant_id FROM " +  Params.dbName + ".discount "
						+ "WHERE restaurant_id = " + pojo.getRestaurantId() + " AND status in (" + Status.DEFAULT.getVal() + ")";
			dbCon.rs = dbCon.stmt.executeQuery(oSQL);
			if(dbCon.rs != null && dbCon.rs.next()){
				opojo = new Discount();
				opojo.setId(dbCon.rs.getInt("discount_id"));
				opojo.setStatus(dbCon.rs.getInt("status"));
				opojo.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			}
			if(opojo != null){
				if(opojo.getStatus() == Status.DEFAULT){
					opojo.setStatus(Status.NORMAL);
				}
//				else if(opojo.getStatus() == Status.DEFAULT_RESERVED){
//					opojo.setStatus(Type.RESERVED);
//				}
				dbCon.stmt.executeUpdate("UPDATE " +  Params.dbName + ".discount SET status = " + opojo.getStatus().getVal() + " WHERE discount_id = " + opojo.getId());
			}
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param pojo
	 * @param plan
	 * @return
	 * @throws Exception
	 */
	public static int insertDiscountBody(DBCon dbCon, Discount pojo, DiscountPlan plan) throws SQLException{
		int count = 0;
		// 处理原默认方案信息
		DiscountDao.checkUpdateOrInsertDiscount(dbCon, pojo);
		
		String insertSQL = "INSERT INTO " +  Params.dbName + ".discount " 
						+ " (restaurant_id, name, type, status)"
						+ " values(" + pojo.getRestaurantId() + ",'" + pojo.getName() + "'," + pojo.getType().getVal()+ "," + pojo.getStatus().getVal() + ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		
		// 获得新方案数据编号
		dbCon.rs = dbCon.stmt.executeQuery("SELECT discount_id FROM " +  Params.dbName + ".discount WHERE restaurant_id = " + pojo.getRestaurantId() + " ORDER BY discount_id DESC LIMIT 0,1");
		Integer discountID = null;
		if(dbCon.rs != null && dbCon.rs.next()){
			discountID = dbCon.rs.getInt("discount_id");
		}
		
		// 生成所有厨房默认折扣
		if(discountID != null && plan != null){
			 Staff term = new Staff();
			 term.setRestaurantId(pojo.getRestaurantId());
			 List<Kitchen> kl = KitchenDao.getByType(dbCon, term, Kitchen.Type.NORMAL);
			 insertSQL = "INSERT INTO " +  Params.dbName + ".discount_plan " 
						+ " (discount_id, kitchen_id, rate)";
			 insertSQL += " values";
			 int i = 0;
			 for(Kitchen k : kl){
				 insertSQL += ( i > 0 ? "," : "");
				 insertSQL += ("(" + discountID + "," + k.getId() + "," + plan.getRate() + ")");
				 i++;
			 }
			 dbCon.stmt.executeUpdate(insertSQL);
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param pojo
	 * @param plan
	 * @throws Exception
	 */
	public static int insertDiscount(DBCon dbCon, Discount pojo, DiscountPlan plan) throws Exception{
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			count = DiscountDao.insertDiscountBody(dbCon, pojo, plan);
			
			dbCon.conn.commit();
		}catch(Exception e){
			count = 0;
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param pojo
	 * @param plan
	 * @throws Exception
	 */
	public static int insertDiscount(Discount pojo, DiscountPlan plan) throws Exception{
		return DiscountDao.insertDiscount(new DBCon(), pojo, plan);
	}
	
	/**
	 * 
	 * @param pojo
	 * @throws Exception
	 */
	public static int updateDiscount(DBCon dbCon, Discount pojo) throws Exception{
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			// 处理原默认方案信息
			DiscountDao.checkUpdateOrInsertDiscount(dbCon, pojo);
						
			String updateSQL = "UPDATE " +  Params.dbName + ".discount SET "
							+ " name = '" + pojo.getName() + "'"
							+ " ,type = " + pojo.getType().getVal()
							+ " ,status = " + pojo.getStatus().getVal()
							+ " WHERE restaurant_id = " + pojo.getRestaurantId() + " AND discount_id = " + pojo.getId();
			
			count = dbCon.stmt.executeUpdate(updateSQL) ;
			if(count == 0){
				throw new Exception("操作失败, 找不到该记录原信息.");
			}
			
			dbCon.conn.commit();
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param pojo
	 * @return 
	 * @throws Exception
	 */
	public static int updateDiscount(Discount pojo) throws Exception{
		return DiscountDao.updateDiscount(new DBCon(), pojo);
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param pojo
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int deleteDiscount(DBCon dbCon, Discount pojo) throws BusinessException, SQLException{
		int count = 0;
		String selectSQL = "SELECT count(discount_id) count FROM " +  Params.dbName + ".member_type_discount WHERE discount_id = " + pojo.getId();
		dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
		if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
			throw new BusinessException(DiscountError.DISCOUNT_DELETE_HAS_MEMBER);
		}
		String deleteSQL = "DELETE FROM " +  Params.dbName + ".discount " + " WHERE restaurant_id = " + pojo.getRestaurantId() + " AND discount_id = " + pojo.getId();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		return count;
	}
	
	/**
	 * 
	 * @param pojo
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int deleteDiscount(Discount pojo) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = DiscountDao.deleteDiscount(dbCon, pojo);
			if(count == 0){
				throw new BusinessException(DiscountError.DISCOUNT_DELETE_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param pojo
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int insertDiscountPlan(DBCon dbCon, DiscountPlan pojo) throws BusinessException, SQLException{
		int count = 0;
		String selectSQL = "SELECT count(discount_id) count FROM " +  Params.dbName + ".discount_plan WHERE discount_id = " + pojo.getDiscount().getId() + " AND kitchen_id = " + pojo.getKitchen().getId();
		dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
		if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
			throw new BusinessException(DiscountError.DISCOUNT_PLAN_INSERT_HAS_KITCHEN);
		}
		
		String insertSQL = "INSERT INTO " +  Params.dbName + ".discount_plan (discount_id, kitchen_id, rate) "
						+ " values(" + pojo.getDiscount().getId() + "," + pojo.getKitchen().getId() + "," + pojo.getRate() + ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		return count;
	}
	
	/**
	 * 
	 * @param pojo
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int insertDiscountPlan(DiscountPlan pojo) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = DiscountDao.insertDiscountPlan(dbCon, pojo);
			if(count == 0){
				throw new BusinessException(DiscountError.DISCOUNT_PLAN_INSERT_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param pojo
	 * @return
	 * @throws SQLException
	 */
	public static int updateDiscountPlan(DBCon dbCon, DiscountPlan pojo) throws SQLException{
		int count = 0;
		String updateSQL = "UPDATE " +  Params.dbName + ".discount_plan SET rate = " + pojo.getRate() + " WHERE dist_plan_id = " + pojo.getId();
		count = dbCon.stmt.executeUpdate(updateSQL);
		return count;
	}
	
	/**
	 * 
	 * @param pojo
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int updateDiscountPlan(DiscountPlan pojo) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = DiscountDao.updateDiscountPlan(dbCon, pojo);
			if(count == 0){
				throw new BusinessException(DiscountError.DISCOUNT_PLAN_UPDATE_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param pojo
	 * @return
	 * @throws SQLException
	 */
	public static int deleteDiscountPlan(DBCon dbCon, DiscountPlan pojo) throws SQLException{
		int count = 0;
		String deleteSQL = "DELETE FROM " +  Params.dbName + ".discount_plan WHERE dist_plan_id = " + pojo.getId();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		return count;
	}
	
	/**
	 * 
	 * @param pojo
	 * @return
	 * @throws Exception
	 */
	public static int deleteDiscountPlan(DiscountPlan pojo) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = DiscountDao.deleteDiscountPlan(dbCon, pojo);
			if(count == 0){
				throw new BusinessException(DiscountError.DISCOUNT_PLAN_DELETE_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 批量修改某折扣方案下菜品折扣信息
	 * @param dbCon
	 * @param pojo
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int updateDiscountPlanRate(DBCon dbCon, DiscountPlan pojo) throws BusinessException, SQLException{
		int count = 0;
		String querySQL = "SELECT count(discount_id) count FROM discount_plan WHERE discount_id = " + pojo.getDiscount().getId();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") == 0){
			throw new BusinessException(DiscountError.DISCOUNT_PLAN_UPDATE_RATE_EMPTY);
		}
		
		String updateSQL = "UPDATE " +  Params.dbName + ".discount_plan SET "
						 + " rate = " + pojo.getRate()
						 + " WHERE discount_id = " + pojo.getDiscount().getId();
		count = dbCon.stmt.executeUpdate(updateSQL);
		return count;
	}
	
	/**
	 * 
	 * @param pojo
	 * @return
	 * @throws Exception
	 */
	public static int updateDiscountPlanRate(DiscountPlan pojo) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = updateDiscountPlanRate(dbCon, pojo);
			if(count == 0){
				throw new BusinessException(DiscountError.DISCOUNT_PLAN_UPDATE_FAIL);
			}
			dbCon.conn.commit();
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * Insert a new discount according to insert builder{@link Discount.InsertBuilder}
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the new discount builder
	 * @return the id to discount just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, Discount.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			int discountId = insert(dbCon, staff, builder);
			
			dbCon.conn.commit();
			
			return discountId;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new discount according to insert builder{@link Discount.InsertBuilder}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the new discount builder
	 * @return the id to discount just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, Discount.InsertBuilder builder) throws SQLException{
		Discount discountToInsert = builder.build();
		String sql;
		
		//Update other discount status to normal if the discount to insert is default.
		if(discountToInsert.isDefault()){
			sql = " UPDATE " + Params.dbName + ".discount SET " +
				  " status = " + Discount.Status.NORMAL.getVal() +
				  " WHERE restaurant_id = " + staff.getRestaurantId();
			dbCon.stmt.executeUpdate(sql);
		}
		
		//Insert the basic info of discount.
		sql = " INSERT INTO " + Params.dbName + ".discount " +
			  " (`restaurant_id`, `name`, `status`, `type`) " +
			  " VALUES(" +
			  staff.getRestaurantId() + "," +
			  "'" + discountToInsert.getName() + "'," +
			  discountToInsert.getStatus().getVal() + "," +
			  discountToInsert.getType().getVal() +
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int discountId;
		if(dbCon.rs.next()){
			discountId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("Failed to generated the discount id.");
		}
	
		//Insert each discount plan with the initial rate
		sql = " INSERT INTO " +  Params.dbName + ".discount_plan "	+ 
			  " (discount_id, kitchen_id, rate) VALUES ";
		int i = 0;
		for(Kitchen k : KitchenDao.getByType(dbCon, staff, Kitchen.Type.NORMAL)){
			sql += ( i > 0 ? "," : "");
			sql += ("(" + discountId + "," + k.getId() + "," + builder.getRate() + ")");
			i++;
		}
		dbCon.stmt.executeUpdate(sql);
		
		return discountId;
	}
	
	/**
	 * Update the discount plan according to specific builder {@link Discount.UpdatePlanBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update plan builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below 
	 * 			<li>the discount associated with the plans to update does NOT exist
	 */
	public static void updatePlan(Staff staff, Discount.UpdatePlanBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			updatePlan(dbCon, staff, builder);
			dbCon.conn.commit();
			
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the discount plan according to specific builder {@link Discount.UpdatePlanBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update plan builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below 
	 * 			<li>the discount associated with the plans to update does NOT exist
	 */
	public static void updatePlan(DBCon dbCon, Staff staff, Discount.UpdatePlanBuilder builder) throws SQLException, BusinessException{
		Discount dist = builder.build();
		String sql;
		for(DiscountPlan dp : dist.getPlans()){
			//Check to see whether the kitchen exist. 
			dp.getKitchen().copyFrom(KitchenDao.getById(dbCon, staff, dp.getKitchen().getId()));
			//Check to see whether the kitchen is normal.
			if(dp.getKitchen().isNormal()){
				//Check to see the kitchen is contained in discount plan before.
				sql = " SELECT IF(COUNT(*) = 0, 0, 1) AS is_exist FROM " + Params.dbName + ".discount_plan " +
					  " WHERE kitchen_id = " + dp.getKitchen().getId() + 
					  " AND discount_id = " + dist.getId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				boolean isExist = false;
				if(dbCon.rs.next()){
					isExist = dbCon.rs.getBoolean("is_exist");
				}
				dbCon.rs.close();
				
				if(isExist){
					sql = " UPDATE " + Params.dbName + ".discount_plan SET rate = " + dp.getRate() +
						  " WHERE kitchen_id = " + dp.getKitchen().getId() +
						  " AND discount_id = " + dist.getId();
					dbCon.stmt.executeUpdate(sql);
				}else{
					sql = " INSERT INTO " + Params.dbName + ".discount_plan " +
						  " (`discount_id`, `kitchen_id`, `rate`) VALUES( " +
						  dist.getId() + "," +
						  dp.getKitchen().getId() + "," +
						  dp.getRate() +
						  ")";
					dbCon.stmt.executeUpdate(sql);
				}
			}
		}
	}
	
	/**
	 * Update the discount according to specific builder{@link Discount.UpdateBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the discount to update does NOT exist
	 */
	public static void update(Staff staff, Discount.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			update(dbCon, staff, builder);
			dbCon.conn.commit();
			
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the discount according to specific builder {@link Discount.UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the discount to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, Discount.UpdateBuilder builder) throws SQLException, BusinessException{
		Discount discount = builder.build();
		String sql;
		
		//Update the status to this discount
		if(builder.isStatusChanged() && discount.isDefault()){
			sql = " UPDATE " + Params.dbName + ".discount SET status = " + Discount.Status.DEFAULT.getVal() +
				  " WHERE discount_id = " + discount.getId();
			dbCon.stmt.executeUpdate(sql);
			
			sql = " UPDATE " + Params.dbName + ".discount SET status = " + Discount.Status.NORMAL.getVal() +
				  " WHERE 1 = 1 " +
				  " AND restaurant_id = " + staff.getRestaurantId() + 
				  " AND discount_id <> " + discount.getId();
			dbCon.stmt.executeUpdate(sql);
		}
		
		//Update other field to this discount.
		sql = " UPDATE " + Params.dbName + ".discount SET " +
			  " discount_id = " + discount.getId() +
			  (builder.isNameChanged() ? " ,name = '" + discount.getName() + "'" : "") +
			  " WHERE discount_id = " + discount.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(DiscountError.DISCOUNT_NOT_EXIST);
		}
		
	}
	
	/**
	 * Delete the discount and its plans to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the discount id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the discount to delete belongs to reserved
	 * 			<li>the discount to delete does NOT exist
	 */
	public static void delete(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			delete(dbCon, staff, id);
			dbCon.conn.commit();
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the discount and its plans to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the discount id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the discount to delete belongs to reserved
	 * 			<li>the discount is used by member type
	 * 			<li>the discount to delete does NOT exist
	 */
	public static void delete(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		String sql;
		
		//Check to see whether the discount belongs to reserved.
		sql = " SELECT type FROM " + Params.dbName + ".discount WHERE discount_id = " + id;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			if(Discount.Type.valueOf(dbCon.rs.getInt("type")) == Type.RESERVED){
				throw new BusinessException(DiscountError.RESERVED_NOT_ALLOW_DELETE);
			}
		}else{
			throw new BusinessException(DiscountError.DISCOUNT_NOT_EXIST);
		}
		dbCon.rs.close();
		
		//Check to see whether the discount to delete is used by member type.
		sql = " SELECT COUNT(*) FROM " +  Params.dbName + ".member_type_discount WHERE discount_id = " + id;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			if(dbCon.rs.getInt(1) > 0){
				throw new BusinessException(DiscountError.DISCOUNT_USED_BY_MEMBER_TYPE);
			}
		}
		dbCon.rs.close();
		
		//Delete the discount plan associated with it.
		sql = "DELETE FROM " + Params.dbName + ".discount_plan WHERE discount_id = " + id;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the discount.
		sql = " DELETE FROM " + Params.dbName + ".discount WHERE discount_id = " + id;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(DiscountError.DISCOUNT_NOT_EXIST);
		}
	}
	
}
