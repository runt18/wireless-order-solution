package com.wireless.db.distMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryMenu;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.distMgr.DiscountPojo;
import com.wireless.protocol.Terminal;

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
			  " FROM " + 
			  Params.dbName + ".discount DIST " +
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
	 * @param pojo
	 * @param plan
	 * @throws Exception
	 */
	public static void addDiscount(DiscountPojo pojo, DiscountPlan plan) throws Exception{
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
								+ " (discount_id, kitchen_id, kitchen_alias, rate)";
					 insertSQL += " values";
					 for(int i = 0; i < kl.length; i++){
						 insertSQL += ( i > 0 ? "," : "");
						 insertSQL += ("("+discountID+","+kl[i].kitchenID+","+kl[i].aliasID+","+plan.rate+")");
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
	
}
