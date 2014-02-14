package com.wireless.db.menuMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FoodError;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodTaste;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteCategory;
import com.wireless.pojo.tasteMgr.TasteCategory.Status;
import com.wireless.pojo.tasteMgr.TasteCategory.Type;
import com.wireless.util.SQLUtil;

public class MenuDao {
	
	/**
	 * 
	 * @param restaurantID
	 * @return
	 * @throws Exception
	 */
	public static List<Kitchen> getKitchen(int restaurantID) throws Exception{
		return MenuDao.getKitchen(" and A.restaurant_id = " + restaurantID, " order by A.display_id");
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
	 * @param foodId
	 * @return
	 * @throws SQLException
	 */
	public static Food getFoodById(int foodId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MenuDao.getFoodById(dbCon, foodId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param foodId
	 * @return
	 * @throws SQLException
	 */
	public static Food getFoodById(DBCon dbCon, int foodId) throws SQLException{
		List<Food> list = MenuDao.getFood(dbCon, " AND A.food_id = " + foodId, null);
		if(list != null && !list.isEmpty())
			return list.get(0);
		else
			return null;
	}
	
	/**
	 * 
	 * @param restaurantId
	 * @param alias
	 * @return
	 * @throws SQLException
	 */
	public static Food getFoodByAlias(int restaurantId, int alias) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getFoodByAlias(dbCon, restaurantId, alias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param restaurantId
	 * @param alias
	 * @return
	 * @throws SQLException
	 */
	public static Food getFoodByAlias(DBCon dbCon, int restaurantId, int alias) throws SQLException{
		List<Food> list = MenuDao.getFood(dbCon, " AND A.restaurant_id = " + restaurantId + " AND A.food_alias = " + alias, null);
		if(list != null && !list.isEmpty())
			return list.get(0);
		else
			return null;
	}
	
	/**
	 * 
	 * @param restaurantId
	 * @return
	 * @throws SQLException
	 */
	public static List<Food> getFoodByRestaurant(int restaurantId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MenuDao.getFoodByRestaurant(dbCon, restaurantId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param restaurantId
	 * @return
	 * @throws SQLException
	 */
	public static List<Food> getFoodByRestaurant(DBCon dbCon, int restaurantId) throws SQLException{
		return MenuDao.getFood(dbCon, " AND A.restaurant_id = " + restaurantId, " ORDER BY A.food_alias");
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param cond
	 * @param orderBy
	 * @return
	 * @throws SQLException
	 */
	public static List<Food> getFood(DBCon dbCon, String cond, String orderBy) throws SQLException{
		List<Food> list = new ArrayList<Food>();
		Food item = null;
		Kitchen kitchen = null;
			
		String selectSQL = "SELECT" 
				+ " A.food_id, A.food_alias, A.restaurant_id, A.name food_name, A.pinyin, A.status, A.taste_ref_type, "
				+ " A.desc, A.img, A.kitchen_id, A.stock_status,  "
				+ " B.name kitchen_name, B.display_id AS kitchen_display_id, B.dept_id, C.unit_price "
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
			item = new Food(dbCon.rs.getInt("food_id"));
			kitchen = item.getKitchen();
			
			item.setAliasId(dbCon.rs.getInt("food_alias"));
			item.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			item.setName(dbCon.rs.getString("food_name"));
			item.setPinyin(dbCon.rs.getString("pinyin"));
			item.setPrice(dbCon.rs.getFloat("unit_price"));
			item.setStatus(dbCon.rs.getShort("status"));
			item.setTasteRefType(dbCon.rs.getInt("taste_ref_type"));
			item.setDesc(dbCon.rs.getString("desc"));
			item.setImage(dbCon.rs.getString("img"));
			item.setStockStatus(dbCon.rs.getInt("stock_status"));
			
			kitchen.setId(dbCon.rs.getInt("kitchen_id"));
			kitchen.setDisplayId(dbCon.rs.getInt("kitchen_display_id"));
			kitchen.setName(dbCon.rs.getString("kitchen_name"));
			kitchen.getDept().setId(dbCon.rs.getShort("dept_id"));
			
			item.setKitchen(kitchen);
			list.add(item);
			kitchen = null;
			item = null;
		}
		return list;
	}
	
	/**
	 * 
	 * @param cond
	 * @param orderBy
	 * @return
	 * @throws SQLException
	 */
	public static List<Food> getFood(String cond, String orderBy) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MenuDao.getFood(dbCon, cond, orderBy);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param restaurantID
	 * @return
	 * @throws Exception
	 */
	public static List<FoodTaste> getFoodTaste(int restaurantID) throws Exception{
		return MenuDao.getFoodTaste(" and A.restaurant_id = " + restaurantID, " order by A.taste_id");
	}
	
	/**
	 * 
	 * @param cond
	 * @param orderBy
	 * @return
	 * @throws SQLException
	 */
	public static List<FoodTaste> getFoodTaste(String cond, String orderBy) throws SQLException{
		List<FoodTaste> list = new ArrayList<FoodTaste>();
		FoodTaste item = null;
		Taste taste = null;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String querySQL = "SELECT "
							+ " A.taste_id, A.restaurant_id, A.preference, A.price, A.category_id, A.rate, A.calc, A.type, "
							+ " B.name category_name, B.type category_type, B.status category_status"
							+ " FROM " + Params.dbName + ".taste A LEFT JOIN " + Params.dbName + ".taste_category B ON A.category_id = B.category_id "
							+ " WHERE 1=1 "
							+ (cond != null && cond.trim().length() > 0 ? " " + cond : "")
							+ (orderBy != null && orderBy.trim().length() > 0 ? " " + orderBy : "");
			dbCon.rs = dbCon.stmt.executeQuery(querySQL);
			while(dbCon.rs != null && dbCon.rs.next()){
				taste = new Taste(dbCon.rs.getInt("taste_id"));
				taste.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				taste.setPreference(dbCon.rs.getString("preference"));
				taste.setPrice(dbCon.rs.getFloat("price"));
				taste.setCategory(new TasteCategory(dbCon.rs.getInt("category_id"), dbCon.rs.getString("category_name")));
				taste.getCategory().setType(Type.valueOf(dbCon.rs.getInt("category_type")));
				taste.getCategory().setStatus(Status.valueOf(dbCon.rs.getInt("category_status")));
				taste.setRate(dbCon.rs.getFloat("rate"));
				taste.setCalc(dbCon.rs.getInt("calc"));
				taste.setType(dbCon.rs.getInt("type"));
				
				item = new FoodTaste(taste);
				
				list.add(item);
				taste = null;
				item = null;
			}
			
		}catch(SQLException e){
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
							+ " A.kitchen_id, A.display_id, A.restaurant_id, A.name kitchen_name, A.is_allow_temp, "
							+ " B.dept_id, B.name dept_name"
							+ " from " + Params.dbName + ".kitchen A left join " + Params.dbName + ".department B on A.dept_id = B.dept_id and A.restaurant_id = B.restaurant_id "
							+ " where 1=1 "
							+ (cond != null && cond.trim().length() > 0 ? " " + cond : "")
							+ (orderBy != null && orderBy.trim().length() > 0 ? " " + orderBy : "");;
			
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			
			while(dbCon.rs != null && dbCon.rs.next()){
				item = new Kitchen(dbCon.rs.getInt("kitchen_id"));
				item.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				item.setDisplayId(dbCon.rs.getInt("display_id"));
				item.setName(dbCon.rs.getString("kitchen_name"));
				item.setAllowTemp(dbCon.rs.getBoolean("is_allow_temp"));
				item.setDept(dbCon.rs.getShort("dept_id"), dbCon.rs.getString("dept_name"));
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
				item = new Department(dbCon.rs.getShort("dept_id"));
				
				item.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				item.setName(dbCon.rs.getString("name"));
				
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
					+ " SET name = '" + dept.getName() + "'"
					+ " WHERE restaurant_id=" + dept.getRestaurantId()
					+ " AND dept_id = " + dept.getId();
			
			if(dbCon.stmt.executeUpdate(updateSQL) == 0){
				throw new BusinessException("操作失败,修改部门信息失败.");
			}
			
			// 
			updateSQL = "UPDATE " 
					+ Params.dbName + ".material_dept "
					+ " SET dept_name = '" + dept.getName() + "'"
					+ " WHERE restaurant_id=" + dept.getRestaurantId()
					+ " AND dept_id = " + dept.getId();
			
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
	public static int updateKitchen(Kitchen kitchen) throws SQLException{
		int count = 0;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String updateSQL = "UPDATE " + Params.dbName + ".kitchen SET " 
							+ " name = '" + kitchen.getName()+ "', dept_id = " + kitchen.getDept().getId() + ", is_allow_temp = " + kitchen.isAllowTemp()
							+ " WHERE restaurant_id = " + kitchen.getRestaurantId() + " and kitchen_id = " + kitchen.getId();
			
			count = dbCon.stmt.executeUpdate(updateSQL);
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	
	
	
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<CancelReason> getCancelReason(DBCon dbCon, Map<Object, Object> params) throws Exception{
		List<CancelReason> list = new ArrayList<CancelReason>();
		CancelReason item = null;
		String querySQL = "SELECT A.cancel_reason_id, A.reason, A.restaurant_id"
						+ " FROM " + Params.dbName + ".cancel_reason A "
						+ " WHERE 1=1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new CancelReason(
					dbCon.rs.getInt("cancel_reason_id"),
					dbCon.rs.getString("reason"),
					dbCon.rs.getInt("restaurant_id"));
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
	public static List<CancelReason> getCancelReason(Map<Object, Object> params) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MenuDao.getCancelReason(dbCon, params);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param cr
	 * @return
	 * @throws SQLException
	 */
	public static int insertCancelReason(DBCon dbCon, CancelReason cr) throws SQLException{
		int count = 0;
		String insertSQL = "INSERT INTO cancel_reason (reason, restaurant_id)"
						 + " VALUES('" + cr.getReason().trim() + "'," + cr.getRestaurantID() + ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		return count;
	}
	
	/**
	 * 
	 * @param cr
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int insertCancelReason(CancelReason cr) throws BusinessException, SQLException{
		int count = 0;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			count = MenuDao.insertCancelReason(dbCon, cr);
			if(count == 0){
				throw new BusinessException(FoodError.CR_INSERT);
			}
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param cr
	 * @return
	 * @throws SQLException
	 */
	public static int updateCancelReason(DBCon dbCon, CancelReason cr) throws SQLException{
		int count = 0;
		String updateSQL = "UPDATE " + Params.dbName + ".cancel_reason SET reason = '" + cr.getReason().trim() + "'"
						 + " WHERE cancel_reason_id = " + cr.getId();
		count = dbCon.stmt.executeUpdate(updateSQL);
		return count;
	}
	
	/**
	 * 
	 * @param cr
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int updateCancelReason(CancelReason cr) throws BusinessException, SQLException{
		int count = 0;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			count = MenuDao.updateCancelReason(dbCon, cr);
			if(count == 0){
				throw new BusinessException(FoodError.CR_UPDATE);
			}
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param cr
	 * @return
	 * @throws Exception
	 */
	public static int deleteCancelReason(DBCon dbCon, CancelReason cr) throws Exception{
		int count  = 0;
		String querySQL = "", deleteSQL = "";
		// 检查退菜原因是否正在使用
		querySQL = "SELECT COUNT(B.id) FROM " + Params.dbName + ".order A, order_food B "
				 + " WHERE A.id = B.order_id AND A.status = " + Order.Status.UNPAID.getVal() 
				 + " AND B.cancel_reason_id = " + cr.getId();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next()){
			count = dbCon.rs.getInt(1);
			if(count != 0){
				throw new BusinessException(FoodError.CR_DELETE_IS_USED);
			}
		}
		// 删除退菜原因
		deleteSQL = "DELETE FROM " + Params.dbName + ".cancel_reason WHERE cancel_reason_id = " + cr.getId() + " AND restaurant_id = " + cr.getRestaurantID();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		if(count == 0){
			throw new BusinessException(FoodError.CR_DELETE);
		}
		return count;
	}
	
	/**
	 * 
	 * @param cr
	 * @return
	 * @throws Exception
	 */
	public static int deleteCancelReason(CancelReason cr) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = MenuDao.deleteCancelReason(dbCon, cr);
			dbCon.conn.commit();
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
}


