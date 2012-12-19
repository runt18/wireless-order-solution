package com.wireless.db.menuMgr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.FoodBasic;
import com.wireless.pojo.menuMgr.FoodTaste;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.util.WebParams;

public class MenuDao {
	
	/**
	 * 
	 * @param restaurantID
	 * @return
	 * @throws Exception
	 */
	public static List<FoodBasic> getFood(int restaurantID) throws Exception{
		return MenuDao.getFood(" and A.restaurant_id = " + restaurantID, " order by A.food_alias");
	}
	
	/**
	 * 
	 * @param restaurantID
	 * @return
	 * @throws Exception
	 */
	public static List<FoodTaste> getFoodTaste(int restaurantID) throws Exception{
		return MenuDao.getFoodTaste(" and A.restaurant_id = " + restaurantID, " order by A.taste_alias");
	}
	
	/**
	 * 
	 * @param restaurantID
	 * @return
	 * @throws Exception
	 */
	public static List<Kitchen> getKitchen(int restaurantID) throws Exception{
		return MenuDao.getKitchen(" and A.restaurant_id = " + restaurantID, " order by A.kitchen_alias");
	}
	
	/**
	 * 
	 * @param restaurantID
	 * @return
	 * @throws Exception
	 */
	public static List<Department> getDepartment(int restaurantID) throws Exception{
		return MenuDao.getDepartment(" and A.restaurant_id = " + restaurantID, " order by A.dept_id");
	}	
	
	/**
	 * 
	 * @param cond
	 * @param orderBy
	 * @return
	 * @throws Exception
	 */
	public static List<FoodBasic> getFood(String cond, String orderBy) throws Exception{
		List<FoodBasic> list = new ArrayList<FoodBasic>();
		FoodBasic item = null;
		Kitchen kitchen = null;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			String selectSQL = "SELECT" 
							+ " A.food_id, A.food_alias, A.restaurant_id, A.name food_name, A.pinyin, A.status, A.taste_ref_type, A.desc, A.img, A.kitchen_id, A.kitchen_alias, "
							+ " B.name kitchen_name, B.dept_id,  C.unit_price "
							+ " FROM " + Params.dbName + ".food A" 
							+ " LEFT JOIN "
							+ Params.dbName + ".kitchen B ON A.kitchen_id = B.kitchen_id "
							+ " LEFT JOIN "
							+ Params.dbName + ".food_price_plan C ON A.food_id = C.food_id AND C.price_plan_id = (SELECT price_plan_id FROM price_plan TT WHERE TT.restaurant_id = A.restaurant_id AND status = 1) "
							+ " WHERE 1=1 "
							+ (cond != null && cond.trim().length() > 0 ? " " + cond : "")
							+ (orderBy != null && orderBy.trim().length() > 0 ? " " + orderBy : "");
			
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			
			while(dbCon.rs != null && dbCon.rs.next()){
				item = new FoodBasic();
				kitchen = item.getKitchen();
				
				item.setFoodID(dbCon.rs.getInt("food_id"));
				item.setFoodAliasID(dbCon.rs.getInt("food_alias"));
				item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
				item.setFoodName(dbCon.rs.getString("food_name"));
				item.setPinyin(dbCon.rs.getString("pinyin"));
				item.setUnitPrice(dbCon.rs.getDouble("unit_price"));
				item.setStatus(dbCon.rs.getByte("status"));
				item.setTasteRefType(dbCon.rs.getInt("taste_ref_type"));
				item.setDesc(dbCon.rs.getString("desc"));
				item.setImg(dbCon.rs.getString("img"));
				
				kitchen.setKitchenID(dbCon.rs.getInt("kitchen_id"));
				kitchen.setKitchenAliasID(dbCon.rs.getInt("kitchen_alias"));
				kitchen.setKitchenName(dbCon.rs.getString("kitchen_name"));
				kitchen.getDept().setDeptID(dbCon.rs.getInt("dept_id"));
				
				item.setKitchen(kitchen);
				list.add(item);
				kitchen = null;
				item = null;
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return list;
	}
	
	/**
	 * 
	 * @param cond
	 * @param orderBy
	 * @return
	 * @throws Exception
	 */
	public static List<FoodTaste> getFoodTaste(String cond, String orderBy) throws Exception{
		List<FoodTaste> list = new ArrayList<FoodTaste>();
		FoodTaste item = null;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			String selectSQL = "select"
							+ " A.taste_id, A.taste_alias, A.restaurant_id, A.preference, A.price, A.category, A.rate, A.calc, A.type "
							+ " from " + Params.dbName + ".taste A "
							+ " where 1=1 "
							+ (cond != null && cond.trim().length() > 0 ? " " + cond : "")
							+ (orderBy != null && orderBy.trim().length() > 0 ? " " + orderBy : "");
			
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			
			while(dbCon.rs != null && dbCon.rs.next()){
				item = new FoodTaste();
				
				item.setTasteID(dbCon.rs.getInt("taste_id"));
				item.setTasteAliasID(dbCon.rs.getInt("taste_alias"));
				item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
				item.setTasteName(dbCon.rs.getString("preference"));
				item.setTastePrice(dbCon.rs.getDouble("price"));
				item.setTasteCategory(dbCon.rs.getInt("category"));
				item.setTasteRate(dbCon.rs.getDouble("rate"));
				item.setTasteCalc(dbCon.rs.getInt("calc"));
				item.setType(dbCon.rs.getInt("type"));
				
				list.add(item);
				item = null;
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return list;
	}
	
	/**
	 * 
	 * @param cond
	 * @param orderBy
	 * @return
	 * @throws Exception
	 */
	public static List<Kitchen> getKitchen(String cond, String orderBy) throws Exception{
		List<Kitchen> list = new ArrayList<Kitchen>();
		Kitchen item = null;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			String selectSQL = "select"
							+ " A.kitchen_id, A.kitchen_alias, A.restaurant_id, A.name kitchen_name, "
							+ " B.dept_id, B.name dept_name"
							+ " from " + Params.dbName + ".kitchen A left join " + Params.dbName + ".department B on A.dept_id = B.dept_id and A.restaurant_id = B.restaurant_id "
							+ " where 1=1 "
							+ (cond != null && cond.trim().length() > 0 ? " " + cond : "")
							+ (orderBy != null && orderBy.trim().length() > 0 ? " " + orderBy : "");;
			
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			
			while(dbCon.rs != null && dbCon.rs.next()){
				item = new Kitchen();
				
				item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
				item.setKitchenID(dbCon.rs.getInt("kitchen_id"));
				item.setKitchenAliasID(dbCon.rs.getInt("kitchen_alias"));
				item.setKitchenName(dbCon.rs.getString("kitchen_name"));
				item.setDept(dbCon.rs.getInt("dept_id"), dbCon.rs.getString("dept_name"));
				
				list.add(item);
				item = null;
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return list;
	}
	
	/**
	 * 
	 * @param cond
	 * @param orderBy
	 * @return
	 * @throws Exception
	 */
	public static List<Department> getDepartment(String cond, String orderBy) throws Exception{
		List<Department> list = new ArrayList<Department>();
		Department item = null;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			String selectSQL = "select"
							+ " A.restaurant_id, A.dept_id, A.name "
							+ " from " + Params.dbName + ".department A "
							+ " where 1=1 "
							+ (cond != null && cond.trim().length() > 0 ? " " + cond : "")
							+ (orderBy != null && orderBy.trim().length() > 0 ? " " + orderBy : "");;
			
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			
			while(dbCon.rs != null && dbCon.rs.next()){
				item = new Department();
				
				item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
				item.setDeptID(dbCon.rs.getInt("dept_id"));
				item.setDeptName(dbCon.rs.getString("name"));
				
				list.add(item);
				item = null;
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return list;
	}
	
	/**
	 * 
	 * @param dept
	 * @throws Exception
	 */
	public static void updateDepartment(Department dept) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			dbCon.conn.setAutoCommit(false);
			
			String updateSQL = "";
			
			updateSQL = "UPDATE " 
					+ Params.dbName + ".department "
					+ " SET name = '" + dept.getDeptName() + "'"
					+ " WHERE restaurant_id=" + dept.getRestaurantID()
					+ " AND dept_id = " + dept.getDeptID();
			
			if(dbCon.stmt.executeUpdate(updateSQL) == 0){
				throw new BusinessException("操作失败,修改部门信息失败.");
			}
			
			// 
			updateSQL = "UPDATE " 
					+ Params.dbName + ".material_dept "
					+ " SET dept_name = '" + dept.getDeptName() + "'"
					+ " WHERE restaurant_id=" + dept.getRestaurantID()
					+ " AND dept_id = " + dept.getDeptID();
			
//			if(dbCon.stmt.executeUpdate(updateSQL) == 0){
//				throw new BusinessException("操作失败,修改部门食材信息失败.");
//			}
//			
			dbCon.conn.commit();
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param kitchen
	 * @throws Exception
	 */
	public static int updateKitchen(Kitchen kitchen) throws Exception{
		int count = 0;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			String updateSQL = "UPDATE " + Params.dbName + ".kitchen SET " 
							+ " name = '" + kitchen.getKitchenName()+ "', dept_id = " + kitchen.getDept().getDeptID() + ", is_allow_temp = " + kitchen.isAllowTemp()
							+ " WHERE restaurant_id = " + kitchen.getRestaurantID() + " and kitchen_id = " + kitchen.getKitchenID();
			
			count = dbCon.stmt.executeUpdate(updateSQL);
			if(count == 0){
				throw new BusinessException(9950);
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public static List<PricePlan> getPricePlan(DBCon dbCon, Map<String, Object> params) throws Exception{
		List<PricePlan> list = new ArrayList<PricePlan>();
		PricePlan item = null;
		Object extra = null, orderBy = null;
		if(params != null){
			extra = params.get(WebParams.SQL_PARAMS_EXTRA);
			orderBy = params.get(WebParams.SQL_PARAMS_ORDERBY);
		}
		String querySQL = "SELECT "
						+ " A.restaurant_id, A.price_plan_id, A.name, A.status "
						+ " FROM price_plan A "
						+ " WHERE 1=1 "
						+ (extra != null  ? " " + extra : "")
						+ (orderBy != null ? " " + orderBy : "");
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new PricePlan(
					dbCon.rs.getInt("restaurant_id"),
					dbCon.rs.getInt("price_plan_id"),
					dbCon.rs.getString("name"),
					dbCon.rs.getShort("status")
					);
			
			list.add(item);
			item = null;
		}
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<PricePlan> getPricePlan(Map<String, Object> params) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPricePlan(dbCon, params);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param pricePlan
	 * @return
	 * @throws Exception
	 */
	public static int insertPricePlan(DBCon dbCon, PricePlan pricePlan) throws Exception{
		int count = 0;
		String insertSQL = "", querySQL = "";
		int newID = 0, copyID = pricePlan.getId();
		// 处理方案状态数据
		pricePlan.setId(0); // 初始化方案编号
		updateupdatePricePlanStatus(dbCon, pricePlan);
				
		// 添加方案基础信息
		insertSQL = "INSERT INTO price_plan (restaurant_id, name, status)"
				  + " VALUES(" + pricePlan.getRestaurantID() + ",'" + pricePlan.getName() + "'," + pricePlan.getStatus() + ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		if(count == 0){
			throw new BusinessException("操作失败, 添加方案信息失败, 请检查数据格式.", 9919);
		}
		// 获取新添加方案编号
		dbCon.rs = dbCon.stmt.executeQuery(WebParams.QUERY_LAST_ID_SQL);
		if(dbCon.rs != null && dbCon.rs.next()){
			newID = dbCon.rs.getInt(1);
		}
		
		if(copyID == 0){
			// 新增方案菜品详细信息
			insertSQL = "INSERT INTO food_price_plan (price_plan_id , food_id, unit_price, restaurant_id)"
					  + " SELECT " + newID + ",A.food_id,0," + pricePlan.getRestaurantID() + " FROM food A WHERE restaurant_id = " + pricePlan.getRestaurantID();
			count = dbCon.stmt.executeUpdate(insertSQL);
			if(count == 0){
				throw new BusinessException("操作失败, 添加菜品价格信息失败, 数据库操作异常.", 9918);
			}
		}else{
			// 验证选择复制的方案信息
			count = 0;
			querySQL = "SELECT COUNT(price_plan_id) FROM price_plan WHERE price_plan_id = " + copyID + " AND restaurant_id = " + pricePlan.getRestaurantID();
			dbCon.rs = dbCon.stmt.executeQuery(querySQL);
			if(dbCon.rs != null && dbCon.rs.next()){
				count = dbCon.rs.getInt(1);
				dbCon.rs.close();
			}
			if(count == 0){
				throw new BusinessException("操作失败, 选择复制的方案信息不存在, 请重新选择.", 9917);
			}
			// 复制方案菜品详细信息
			insertSQL = "INSERT INTO food_price_plan (price_plan_id , food_id, unit_price, restaurant_id)"
					  + " SELECT " + newID + ",A.food_id,A.unit_price,A.restaurant_id FROM food_price_plan A WHERE A.price_plan_id = " + copyID;
			count = dbCon.stmt.executeUpdate(insertSQL);
			if(count == 0){
				throw new BusinessException("操作失败, 添加菜品价格信息失败, 数据库操作异常.", 9918);
			}
		}
		return count;
	}
	
	/**
	 * 
	 * @param pricePlan
	 * @return
	 * @throws Exception
	 */
	public static int insertPricePlan(PricePlan pricePlan) throws Exception{
		int count = 0;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = insertPricePlan(dbCon, pricePlan);
			dbCon.conn.commit();
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 处理方案状态数据
	 * @param pricePlan
	 * @return
	 * @throws Exception
	 */
	public static int updateupdatePricePlanStatus(DBCon dbCon, PricePlan pricePlan) throws Exception{
		int count = 0;
		String querySQL = "", updateSQL = "";
		if(pricePlan.getStatus() == PricePlan.STATUS_ACTIVITY){
			updateSQL = "UPDATE price_plan SET status = " + PricePlan.STATUS_NORMAL + " WHERE restaurant_id = " + pricePlan.getRestaurantID();
			count = dbCon.stmt.executeUpdate(updateSQL);
			if(count == 0){
				throw new BusinessException("操作失败, 设置价格方案状态失败.", 9939);
			}
		}else if(pricePlan.getStatus() == PricePlan.STATUS_NORMAL){
			// 修改操作才检查
			if(pricePlan.getId() > 0){
				querySQL = "SELECT COUNT(*) FROM price_plan "
						 + " WHERE price_plan_id <> " + pricePlan.getId()
						 + " AND restaurant_id = " + pricePlan.getRestaurantID()
						 + " AND status = " + PricePlan.STATUS_ACTIVITY;
				dbCon.rs = dbCon.stmt.executeQuery(querySQL);
				if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt(1) == 0){
					throw new BusinessException("操作失败, 必须有一个价格方案为:活动状态.", 9937);
				}				
			}
		}else{
			throw new BusinessException("操作失败, 价格方案状态不合法, 请重新检查.", 9938);
		}	
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @return
	 */
	public static int updatePricePlan(DBCon dbCon, PricePlan pricePlan) throws Exception{
		int count = 0;
		String updateSQL = "";
		// 处理非活动状态数据
		updateupdatePricePlanStatus(dbCon, pricePlan);
		
		updateSQL = "UPDATE price_plan SET "
						 + " name = '" + pricePlan.getName() + "', status = " + pricePlan.getStatus() 
						 + " WHERE price_plan_id = " + pricePlan.getId();
		count = dbCon.stmt.executeUpdate(updateSQL);
		if(count == 0){
			throw new BusinessException("操作失败, 修改折扣方案信息失败, 数据库操作异常.", 9940);
		}
		return count;
	}
	
	/**
	 * 
	 * @param pricePlan
	 * @return
	 * @throws Exception
	 */
	public static int updatePricePlan(PricePlan pricePlan) throws Exception{
		int count = 0;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = updatePricePlan(dbCon, pricePlan);
			dbCon.conn.commit();
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param pricePlan
	 * @return
	 * @throws Exception
	 */
	public static int deletePricePlan(DBCon dbCon, PricePlan pricePlan) throws Exception{
		int count = 0;
		String querySQL = "", deleteSQL = "";
		// 验证方案状态
		querySQL = "SELECT status FROM price_plan WHERE price_plan_id = " + pricePlan.getId();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getShort(1) == PricePlan.STATUS_ACTIVITY){
			throw new BusinessException("操作失败, 该价格方案为活动状态, 正在使用中的不允许删除.", 9938);
		}
		// 删除方案下所有菜谱价格信息
		deleteSQL = "DELETE FROM food_price_plan WHERE price_plan_id = " + pricePlan.getId();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		if(count == 0){
			throw new BusinessException("操作失败, 删除该方案下所有菜品价格信息失败.", 9937);
		}
		// 删除方案基础信息
		deleteSQL = "DELETE FROM price_plan WHERE price_plan_id = " + pricePlan.getId();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		if(count == 0){
			throw new BusinessException("操作失败, 删除价格方案信息失败.", 9930);
		}
		return count;
	}
	
	/**
	 * 
	 * @param pricePlan
	 * @return
	 * @throws Exception
	 */
	public static int deletePricePlan(PricePlan pricePlan) throws Exception{
		int count = 0;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = deletePricePlan(dbCon, pricePlan);
			dbCon.conn.commit();
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
}






























