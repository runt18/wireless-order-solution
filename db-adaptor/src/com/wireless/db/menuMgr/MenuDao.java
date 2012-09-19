package com.wireless.db.menuMgr;

import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.FoodBasic;
import com.wireless.pojo.menuMgr.FoodTaste;
import com.wireless.pojo.menuMgr.Kitchen;

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
			
			String selectSQL = "select" 
							+ " A.food_id, A.food_alias, A.restaurant_id, A.name food_name, A.pinyin, A.unit_price, A.status, A.taste_ref_type, A.desc, A.img, A.kitchen_id, A.kitchen_alias, "
							+ " B.name kitchen_name, B.dept_id, B.discount, B.discount_2, B.discount_3, B.member_discount_1, B.member_discount_2, B.member_discount_3 "
							+ " from food A left join kitchen B on A.kitchen_id = B.kitchen_id "
							+ " where 1=1 "
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
				kitchen.setDiscount1(dbCon.rs.getDouble("discount"));
				kitchen.setDiscount2(dbCon.rs.getDouble("discount_2"));
				kitchen.setDiscount3(dbCon.rs.getDouble("discount_3"));
				kitchen.setMemberDiscount1(dbCon.rs.getDouble("member_discount_1"));
				kitchen.setMemberDiscount2(dbCon.rs.getDouble("member_discount_2"));
				kitchen.setMemberDiscount3(dbCon.rs.getDouble("member_discount_3"));
				
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
							+ " from taste A "
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
							+ " A.kitchen_id, A.kitchen_alias, A.restaurant_id, A.name kitchen_name, A.discount, A.discount_2, A.discount_3, A.member_discount_1, A.member_discount_2, A.member_discount_3, "
							+ " B.dept_id, B.name dept_name"
							+ " from kitchen A left join department B on A.dept_id = B.dept_id and A.restaurant_id = B.restaurant_id "
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
				item.setDiscount1(dbCon.rs.getDouble("discount"));
				item.setDiscount2(dbCon.rs.getDouble("discount_2"));
				item.setDiscount3(dbCon.rs.getDouble("discount_3"));
				item.setMemberDiscount1(dbCon.rs.getDouble("member_discount_1"));
				item.setMemberDiscount2(dbCon.rs.getDouble("member_discount_2"));
				item.setMemberDiscount3(dbCon.rs.getDouble("member_discount_3"));
				
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
							+ " from department A "
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
	
}
