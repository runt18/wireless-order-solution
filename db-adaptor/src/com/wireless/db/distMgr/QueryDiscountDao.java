package com.wireless.db.distMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryMenu;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.distMgr.DiscountPlanPojo;
import com.wireless.pojo.distMgr.DiscountPojo;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Terminal;

@SuppressWarnings({"finally"})
public class QueryDiscountDao {
	
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
	public static DiscountPojo[] execPureDiscount(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execPureDiscount(dbCon, term, extraCond, orderClause);
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
	 * @return The array holding the pure discount info.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	public static DiscountPojo[] execPureDiscount(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " DIST.discount_id, DIST.restaurant_id, DIST.name AS dist_name, DIST.level " +
			  " FROM " +  Params.dbName + ".discount DIST " +
			  " WHERE 1=1 " +
			  " AND DIST.restaurant_id = " + term.restaurantID +
			  (extraCond == null ? "" : extraCond) + " " +
			  (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<DiscountPojo> pureDiscounts = new ArrayList<DiscountPojo>();
		while(dbCon.rs.next()){
			DiscountPojo distPojo = new DiscountPojo();
			distPojo.setId(dbCon.rs.getInt("discount_id"));
			distPojo.setName(dbCon.rs.getString("dist_name"));
			distPojo.setLevel(dbCon.rs.getInt("level"));
			distPojo.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			pureDiscounts.add(distPojo);
		}
		
		return pureDiscounts.toArray(new DiscountPojo[pureDiscounts.size()]);
		
	}
	
	/**
	 * 
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws Exception
	 */
	public static DiscountPlanPojo[] getDiscountPlan(String extraCond, String orderClause) throws Exception{
		List<DiscountPlanPojo> list = new ArrayList<DiscountPlanPojo>();
		DiscountPlanPojo item = null;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			String selectSQL = "SELECT A.dist_plan_id, A.rate, B.discount_id,  B.name as discount_name, B.restaurant_id, B.level, K.kitchen_id, K.name as kitchen_name "
							+ " FROM " +  Params.dbName + ".discount_plan A LEFT JOIN " +  Params.dbName + ".kitchen K ON A.kitchen_id = K.kitchen_id, " +  Params.dbName + ".discount B "
							+ " WHERE A.discount_id = B.discount_id "
							+ (extraCond == null ? "" : extraCond) 
							+ " " 
							+ (orderClause == null ? "" : orderClause);
			
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			
			while(dbCon.rs != null && dbCon.rs.next()){
				item = new DiscountPlanPojo();
				
				item.setPlanID(dbCon.rs.getInt("dist_plan_id"));
				item.setRate(dbCon.rs.getFloat("rate"));
				
				item.getDiscount().setId(dbCon.rs.getInt("discount_id"));
				item.getDiscount().setName(dbCon.rs.getString("discount_name"));
				item.getDiscount().setRestaurantID(dbCon.rs.getInt("restaurant_id"));
				item.getDiscount().setLevel(dbCon.rs.getInt("level"));
				
				item.getKitchen().setKitchenID(dbCon.rs.getInt("kitchen_id"));
				item.getKitchen().setKitchenName(dbCon.rs.getString("kitchen_name"));
				
				list.add(item);
				item = null;
			}
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
			return list.toArray(new DiscountPlanPojo[list.size()]);
		}
	}
	
	/**
	 * 
	 * @param pojo
	 * @param plan
	 * @throws Exception
	 */
	public static void insertDiscount(DiscountPojo pojo, DiscountPlanPojo plan) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			String insertSQL = "INSERT INTO " +  Params.dbName + ".discount " 
							+ " (restaurant_id, name, level)"
							+ " values(" + pojo.getRestaurantID() + ",'" + pojo.getName() + "'," + pojo.getLevel()+ ")";
			dbCon.stmt.executeUpdate(insertSQL);
			
			// 获得新方案数据编号
			dbCon.rs = dbCon.stmt.executeQuery("SELECT discount_id FROM " +  Params.dbName + ".discount WHERE restaurant_id = " + pojo.getRestaurantID() + " ORDER BY discount_id DESC LIMIT 0,1");
			Integer discountID = null;
			if(dbCon.rs != null && dbCon.rs.next()){
				discountID = dbCon.rs.getInt("discount_id");
			}
			
			// 生成所有厨房默认折扣
			if(discountID != null && plan != null){
				 Kitchen[] kl = QueryMenu.queryKitchens(dbCon, " AND KITCHEN.restaurant_id = " + pojo.getRestaurantID() + " AND KITCHEN.type <> 1", null);
				 if(kl.length > 0){
					 insertSQL = "";
					 insertSQL = "INSERT INTO " +  Params.dbName + ".discount_plan " 
								+ " (discount_id, kitchen_id, rate)";
					 insertSQL += " values";
					 for(int i = 0; i < kl.length; i++){
						 insertSQL += ( i > 0 ? "," : "");
						 insertSQL += ("("+discountID+","+kl[i].kitchenID+","+plan.getRate()+")");
					 }
					 dbCon.stmt.executeUpdate(insertSQL);
				 }
			}
			
			dbCon.conn.commit();
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param pojo
	 * @throws Exception
	 */
	public static void updateDiscount(DiscountPojo pojo) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			String updateSQL = "UPDATE " +  Params.dbName + ".discount SET "
							+ " name = '" + pojo.getName() + "'"
							+ " ,level = " + pojo.getLevel()
							+ " WHERE restaurant_id = " + pojo.getRestaurantID() + " AND discount_id = " + pojo.getId();
			
			if(dbCon.stmt.executeUpdate(updateSQL) == 0){
				throw new Exception("操作失败, 找不到该记录原信息.");
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param pojo
	 * @throws Exception
	 */
	public static void deleteDiscount(DiscountPojo pojo) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			String selectSQL = "SELECT count(discount_id) count FROM " +  Params.dbName + ".discount_plan WHERE discount_id = " + pojo.getId();
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
				throw new BusinessException("");
			}
			
			String deleteSQL = "DELETE FROM " +  Params.dbName + ".discount " + " WHERE restaurant_id = " + pojo.getRestaurantID() + " AND discount_id = " + pojo.getId();
			dbCon.stmt.executeUpdate(deleteSQL);
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param pojo
	 * @throws Exception
	 */
	public static void insertDiscountPlan(DiscountPlanPojo pojo) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String selectSQL = "SELECT count(discount_id) count FROM " +  Params.dbName + ".discount_plan WHERE discount_id = " + pojo.getDiscount().getId() + " AND kitchen_id = " + pojo.getKitchen().getKitchenID();
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
				throw new BusinessException("");
			}
			
			String insertSQL = "INSERT INTO " +  Params.dbName + ".discount_plan (discount_id, kitchen_id, rate) "
							+ " values(" + pojo.getDiscount().getId() + "," + pojo.getKitchen().getKitchenID() + "," + pojo.getRate() + ")";
			dbCon.stmt.executeUpdate(insertSQL);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param pojo
	 * @throws Exception
	 */
	public static void updateDiscountPlan(DiscountPlanPojo pojo) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String selectSQL = "SELECT count(discount_id) count FROM " +  Params.dbName + ".discount_plan WHERE discount_id = " + pojo.getDiscount().getId() + " AND kitchen_id = " + pojo.getKitchen().getKitchenID();
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
				throw new BusinessException(9996);
			}
			
			String updateSQL = "UPDATE " +  Params.dbName + ".discount_plan SET rate = " + pojo.getRate() + " WHERE dist_plan_id = " + pojo.getPlanID();
			if(dbCon.stmt.executeUpdate(updateSQL) == 0){
				throw new Exception();
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param pojo
	 * @throws Exception
	 */
	public static void deleteDiscountPlan(DiscountPlanPojo pojo) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			String deleteSQL = "DELETE FROM " +  Params.dbName + ".discount_plan WHERE dist_plan_id = " + pojo.getPlanID();
			if(dbCon.stmt.executeUpdate(deleteSQL) == 0){
				throw new Exception();
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
}
