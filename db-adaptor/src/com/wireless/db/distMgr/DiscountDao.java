package com.wireless.db.distMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PlanError;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.distMgr.Discount.Status;
import com.wireless.pojo.distMgr.DiscountPlan;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;

public class DiscountDao {
	
	/**
	 * Get the discount along with its discount plan to a specified restaurant defined in {@link Staff}
	 * and other extra condition. 
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the discount and discount plan of its own
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Discount> getDiscount(Staff term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getDiscount(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the discount along with its discount plan to a specified restaurant defined in {@link Staff}
	 * and other extra condition. 
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the discount and discount plan of its own
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Discount> getDiscount(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " DIST.discount_id, DIST.restaurant_id, DIST.name AS dist_name, DIST.level, DIST.status AS dist_status, " +
			  " DIST_PLAN.dist_plan_id, DIST_PLAN.kitchen_id, DIST_PLAN.rate, " +
			  " KITCHEN.name AS kitchen_name, KITCHEN.kitchen_alias, " +
			  " CASE WHEN DIST_PLAN.discount_id IS NULL THEN '0' ELSE '1' END AS has_plan " +
			  " FROM " + 
			  Params.dbName + ".discount DIST " +
			  " LEFT JOIN " +
			  Params.dbName + ".discount_plan DIST_PLAN " +
			  " ON DIST_PLAN.discount_id = DIST.discount_id " +
			  " LEFT JOIN " +
			  Params.dbName + ".kitchen KITCHEN " +
			  " ON DIST_PLAN.kitchen_id = KITCHEN.kitchen_id " +
			  " WHERE 1=1 " +
			  " AND DIST.restaurant_id = " + term.getRestaurantId() +
			  (extraCond == null ? "" : extraCond) + " " +
			  (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		Map<Discount, Discount> discounts = new LinkedHashMap<Discount, Discount>();
		
		while(dbCon.rs.next()){
			Discount key = new Discount();
			key.setId(dbCon.rs.getInt("discount_id"));
			key.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			key.setName(dbCon.rs.getString("dist_name"));
			key.setLevel(dbCon.rs.getShort("level"));
			key.setStatus(dbCon.rs.getInt("dist_status"));

			Kitchen kitchen = new Kitchen();
			kitchen.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			kitchen.setId(dbCon.rs.getInt("kitchen_id"));
			kitchen.setAliasId(dbCon.rs.getShort("kitchen_alias"));
			kitchen.setName(dbCon.rs.getString("kitchen_name"));
			
			float rate = dbCon.rs.getFloat("rate");
			DiscountPlan plan = null;
			if(dbCon.rs.getBoolean("has_plan") && rate != 1){
				plan = new DiscountPlan(dbCon.rs.getInt("dist_plan_id"), kitchen,  rate);
			}
			
			Discount discount = discounts.get(key);
			if(discount != null){
				discount.addPlan(plan);
			}else{
				key.addPlan(plan);
				discounts.put(key, key);
			}
			
		}
		dbCon.rs.close();
		
		return new ArrayList<Discount>(discounts.values());
	}
	
	/**
	 * Get the pure discount info(such as id, name and so on).
	 * @param term
	 * 			The terminal to query.
	 * @param extraCond
	 * 			The extra condition.
	 * @param orderClause
	 * 			The order clause.
	 * @return The array holding the pure discount info.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	public static List<Discount> getPureDiscount(Staff term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureDiscount(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the pure discount info(such as id, name and so on).
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			The database connection.
	 * @param term
	 * 			The terminal to query.
	 * @param extraCond
	 * 			The extra condition.
	 * @param orderClause
	 * 			The order clause.
	 * @return The list holding the pure discount info.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	public static List<Discount> getPureDiscount(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " DIST.discount_id, DIST.restaurant_id, DIST.name AS dist_name, DIST.level, DIST.status " +
			  " FROM " +  Params.dbName + ".discount DIST " +
			  " WHERE 1=1 " +
			  " AND DIST.restaurant_id = " + term.getRestaurantId() +
			  (extraCond == null ? "" : extraCond) + " " +
			  (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<Discount> pureDiscounts = new ArrayList<Discount>();
		while(dbCon.rs.next()){
			Discount distPojo = new Discount();
			distPojo.setId(dbCon.rs.getInt("discount_id"));
			distPojo.setName(dbCon.rs.getString("dist_name"));
			distPojo.setLevel(dbCon.rs.getInt("level"));
			distPojo.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			distPojo.setStatus(dbCon.rs.getInt("status"));
			pureDiscounts.add(distPojo);
		}
		
		return pureDiscounts;
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
		String selectSQL = "SELECT A.dist_plan_id, A.rate, B.discount_id, B.name as discount_name, B.restaurant_id, B.level, B.status, K.kitchen_id, K.name as kitchen_name "
				+ " FROM " +  Params.dbName + ".discount_plan A LEFT JOIN " +  Params.dbName + ".kitchen K ON A.kitchen_id = K.kitchen_id, " +  Params.dbName + ".discount B "
				+ " WHERE A.discount_id = B.discount_id "
				+ (extraCond == null ? "" : extraCond) 
				+ " " 
				+ (orderClause == null ? "" : orderClause);

		dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
		
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new DiscountPlan();
			
			item.setPlanID(dbCon.rs.getInt("dist_plan_id"));
			item.setRate(dbCon.rs.getFloat("rate"));
			
			item.getDiscount().setId(dbCon.rs.getInt("discount_id"));
			item.getDiscount().setName(dbCon.rs.getString("discount_name"));
			item.getDiscount().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			item.getDiscount().setLevel(dbCon.rs.getInt("level"));
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
		if(pojo.isDefault() || pojo.isDefaultReserved()){
			Discount opojo = null;
			String oSQL = "SELECT discount_id, status, restaurant_id FROM " +  Params.dbName + ".discount "
						+ "WHERE restaurant_id = " + pojo.getRestaurantId() + " AND status in (" + Status.DEFAULT.getVal() + "," + Status.DEFAULT_RESERVED.getVal() + ")";
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
				}else if(opojo.getStatus() == Status.DEFAULT_RESERVED){
					opojo.setStatus(Status.RESERVED);
				}
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
						+ " (restaurant_id, name, level, status)"
						+ " values(" + pojo.getRestaurantId() + ",'" + pojo.getName() + "'," + pojo.getLevel()+ "," + pojo.getStatus().getVal() + ")";
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
			 List<Kitchen> kl = KitchenDao.getKitchens(dbCon, term, " AND KITCHEN.type = " + Kitchen.Type.NORMAL.getVal(), null);
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
							+ " ,level = " + pojo.getLevel()
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
		String selectSQL = "SELECT count(discount_id) count FROM " +  Params.dbName + ".discount_plan WHERE discount_id = " + pojo.getId();
		dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
		if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
			throw new BusinessException(PlanError.DISCOUNT_DELETE_HAS_KITCHEN);
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
				throw new BusinessException(PlanError.DISCOUNT_DELETE_FAIL);
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
			throw new BusinessException(PlanError.DISCOUNT_PLAN_INSERT_HAS_KITCHEN);
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
			count = DiscountDao.insertDiscountPlan(new DBCon(), pojo);
			if(count == 0){
				throw new BusinessException(PlanError.DISCOUNT_PLAN_INSERT_FAIL);
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
		String updateSQL = "UPDATE " +  Params.dbName + ".discount_plan SET rate = " + pojo.getRate() + " WHERE dist_plan_id = " + pojo.getPlanID();
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
				throw new BusinessException(PlanError.DISCOUNT_PLAN_UPDATE_FAIL);
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
		String deleteSQL = "DELETE FROM " +  Params.dbName + ".discount_plan WHERE dist_plan_id = " + pojo.getPlanID();
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
				throw new BusinessException(PlanError.DISCOUNT_PLAN_DELETE_FAIL);
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
			throw new BusinessException(PlanError.DISCOUNT_PLAN_UPDATE_RATE_EMPTY);
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
				throw new BusinessException(PlanError.DISCOUNT_PLAN_UPDATE_FAIL);
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
	
}
