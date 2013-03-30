package com.wireless.db.menuMgr;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.frontBusiness.QueryMenu;
import com.wireless.db.tasteRef.TasteRefDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PlanError;
import com.wireless.exception.FoodError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.FoodBasic;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.protocol.Food;
import com.wireless.util.SQLUtil;

public class FoodBasicDao {
	
	/**
	 * 
	 * @param dbCon
	 * @param fb
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int insertFoodBaisc(DBCon dbCon, FoodBasic fb) throws BusinessException, SQLException{
		int count = 0;
		String insertSQL = "", querySQL = "";
		// 检查菜品是否存在
		querySQL = "SELECT count(food_alias) count FROM " + Params.dbName + ".food WHERE restaurant_id = " + fb.getRestaurantID() + " AND food_alias = " + fb.getAliasID();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			count = dbCon.rs.getInt("count");
			break;
		}
		if(count > 0){
			throw new BusinessException("操作失败,该编号菜品已经存在!");
		}
		// 新增菜品信息
		insertSQL = "INSERT INTO " + Params.dbName + ".food" 
				+ " ( food_alias, name, pinyin, restaurant_id, kitchen_id, kitchen_alias, status, taste_ref_type, food.desc ) "
				+ "values("
				+ fb.getAliasID() + ", " 
				+ "'" + fb.getFoodName() + "', " 
				+ "'" + fb.getPinyin() + "', " 
				+ fb.getRestaurantID() + ", " 
				+ fb.getKitchen().getKitchenID() + ", " 
				+ fb.getKitchen().getKitchenAliasID() + ", " 
				+ fb.getStatus() + ", " 
				+ FoodBasic.TASTE_SMART_REF + ", "
				+ (fb.getDesc() == null ? null : "'" + fb.getDesc() + "'")
				+ ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		// 获取新增菜品数据编号
		dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.SQL_QUERY_LAST_INSERT_ID);
		if(dbCon.rs != null && dbCon.rs.next()){
			fb.setFoodID(dbCon.rs.getInt(1));
			dbCon.rs = null;
		}
		
		// 新增菜谱价格方案信息
		insertSQL = "INSERT INTO food_price_plan (restaurant_id, food_id, price_plan_id, unit_price)"
				  + " SELECT " + fb.getRestaurantID() + "," + fb.getFoodID() + ",price_plan_id," + fb.getUnitPrice() + " FROM price_plan WHERE restaurant_id = " + fb.getRestaurantID();
		count = dbCon.stmt.executeUpdate(insertSQL);
		if(count == 0){
			throw new BusinessException(PlanError.PRICE_FOOD_INSERT);
		}
		
		// 
		try{
			Food[] updateFood = QueryMenu.queryFoods(" AND FOOD.food_id = " + fb.getFoodID(), null);
			if(updateFood.length != 0){
				TasteRefDao.execByFood(updateFood[0]);
			}
		} catch(Exception e){
			throw new BusinessException(FoodError.TASTE_UPDATE_FAIL);
		}	
		
		return count;
	}
	
	/**
	 * 
	 * @param fb
	 * @throws Exception
	 */
	public static int insertFoodBaisc(FoodBasic fb) throws Exception{		
		int count = 0;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = insertFoodBaisc(dbCon, fb);
			dbCon.conn.commit();
		} catch(Exception e){
			throw e;
		} finally {
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param fb
	 * @param content
	 * @throws Exception
	 */
	public static void insertFoodBaisc(FoodBasic fb, String content) throws Exception{
		try{
			FoodBasicDao.insertFoodBaisc(fb);
			
			try{
				Food[] updateFood = QueryMenu.queryFoods(" AND FOOD.food_id = " + fb.getFoodID(), null);
				if(updateFood.length != 0){
					TasteRefDao.execByFood(updateFood[0]);
				}
				FoodCombinationDao.updateFoodCombination(fb.getFoodID(), fb.getRestaurantID(), fb.getStatus(), content);
			} catch(Exception e){
				throw new BusinessException(FoodError.COMBO_UPDATE_FAIL);
			}
			
		} catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param fb
	 * @return
	 * @throws Exception
	 */
	public static int updateFoodBaisc(DBCon dbCon, FoodBasic fb) throws Exception{
		int count = 0;
		String updateSQL = "";
		// 修改当前活动价格方案信息 
		updateSQL = "UPDATE " + Params.dbName + ".food_price_plan SET unit_price = " + fb.getUnitPrice()
				  + " WHERE food_id = " + fb.getFoodID()
				  + " AND price_plan_id = (SELECT price_plan_id FROM " + Params.dbName + ".price_plan WHERE restaurant_id = " + fb.getRestaurantID() + " AND status = " + PricePlan.STATUS_ACTIVITY + ")";
		count = dbCon.stmt.executeUpdate(updateSQL);
		if(count == 0){
			throw new BusinessException(FoodError.UPDATE_PRICE_FAIL);
		}
		// 修改菜品基础信息
		updateSQL = "UPDATE " + Params.dbName + ".food " +
				" SET name = '" + fb.getFoodName() + "', " + 
				" pinyin = '"+ fb.getPinyin() + "', " + 
				" kitchen_id =  " + (fb.getKitchen().getKitchenID() < 0 ? null : fb.getKitchen().getKitchenID()) + ", " + 
				" kitchen_alias = " + fb.getKitchen().getKitchenAliasID() + ", " + 
				" status =  " + fb.getStatus() + ", " + 
				" food.desc = " + (fb.getDesc() == null || fb.getDesc().trim().length() == 0 ? null : "'" + fb.getDesc() + "'") +
				" WHERE restaurant_id=" + fb.getRestaurantID() + " and food_id = " + fb.getFoodID();
		
		count = dbCon.stmt.executeUpdate(updateSQL);
		if(count != 1){
			throw new BusinessException(FoodError.UPDATE_FAIL);
		}
		return count;
	}
	
	/**
	 * 
	 * @param fb
	 * @throws Exception
	 */
	public static int updateFoodBaisc(FoodBasic fb) throws Exception{
		int count = 0;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = FoodBasicDao.updateFoodBaisc(dbCon, fb);
			dbCon.conn.commit();
		} catch(Exception e){
			throw e;
		} finally {
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param fb
	 * @return
	 * @throws Exception
	 */
	public static int deleteFood(DBCon dbCon, FoodBasic fb) throws Exception{
		int count = 0;
		String querySQL = "", deleteSQL = "", tableIDList = "";
		
		// 验证删除菜品是否正在营业使用过程中
		querySQL = "SELECT A.id, A.table_id, A.table_alias, A.table_name, B.food_id, SUM(B.order_count) order_count "
				 + " FROM " + Params.dbName + ".order A, " + Params.dbName + ".order_food B"
				 + " WHERE A.id = B.order_id AND A.status = " + Order.STATUS_UNPAID + " AND A.restaurant_id = " + fb.getRestaurantID()
				 + " GROUP BY B.order_id, B.food_id "
				 + " HAVING B.food_id = " + fb.getFoodID()
				 + " ORDER BY A.table_alias ";
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		int index = 0;
		while(dbCon.rs != null && dbCon.rs.next()){
			if(index >= 0 && index < 5){
				tableIDList += ((index > 0 ? "," : "") + dbCon.rs.getInt("table_alias"));
			}else{
				tableIDList += ".";
			}
			index++;
		}
		if(!tableIDList.trim().isEmpty()){
			throw new BusinessException(FoodError.DELETE_FAIL_IS_USED);
		}
		
		// delete food
		deleteSQL = "DELETE FROM " + Params.dbName + ".food WHERE food_id = " + fb.getFoodID() + " and restaurant_id = " + fb.getRestaurantID();
		dbCon.stmt.executeUpdate(deleteSQL);
		
		// delete foodTaste
		deleteSQL = "DELETE FROM " + Params.dbName + ".food_taste_rank WHERE food_id = " + fb.getFoodID() + " and restaurant_id = " + fb.getRestaurantID();
		dbCon.stmt.executeUpdate(deleteSQL);
		deleteSQL = "DELETE FROM " + Params.dbName + ".food_taste WHERE food_id = " + fb.getFoodID() + " and restaurant_id = " + fb.getRestaurantID();
		dbCon.stmt.executeUpdate(deleteSQL);
		
		// delete foodMaterial
		deleteSQL = "DELETE FROM " + Params.dbName + ".food_material WHERE food_id = " + fb.getFoodID() + " and restaurant_id = " + fb.getRestaurantID();
		dbCon.stmt.executeUpdate(deleteSQL);
		
		// delete foodCombination
		deleteSQL = "DELETE FROM " + Params.dbName + ".combo WHERE food_id = " + fb.getFoodID() + " and restaurant_id = " + fb.getRestaurantID();
		dbCon.stmt.executeUpdate(deleteSQL);
		
		// delete foodPricePlan
		deleteSQL = "DELETE FROM " + Params.dbName + ".food_price_plan WHERE food_id = " + fb.getFoodID() + " and restaurant_id = " + fb.getRestaurantID();
		dbCon.stmt.executeUpdate(deleteSQL);
		
		return count;
	}
	
	/**
	 * 
	 * @param fb
	 * @throws Exception
	 */
	public static int deleteFood(FoodBasic fb) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = deleteFood(dbCon, fb);
			dbCon.conn.commit();
		} catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		} finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param foodID
	 * @param newPath
	 * @throws Exception
	 */
	public static void updateFoodImageName(int restaurantID, int foodID, String imgName) throws Exception{
		FoodBasic fb = new FoodBasic();
		fb.setRestaurantID(restaurantID);
		fb.setFoodID(foodID);
		fb.setImg(imgName);
		updateFoodImageName(fb);
	}
	
	/**
	 * 
	 * @param fb
	 * @throws Exception
	 */
	public static void updateFoodImageName(FoodBasic fb) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String updateSQL = "update " + Params.dbName + ".food set "
						       + " img = " + (fb.getImg() == null || fb.getImg().trim().length() == 0 ? null : "'" + fb.getImg() + "'") 
						       + " where food_id = " + fb.getFoodID() + " and restaurant_id = " + fb.getRestaurantID();
			
			int count = dbCon.stmt.executeUpdate(updateSQL);
			if(count <= 0){
				throw new BusinessException("操作失败,未更新编号为" + fb.getFoodID() + "的菜品图片信息!");
			}
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param fb
	 * @return
	 */
	public static FoodBasic getFoodBasicImage(FoodBasic fb) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String selectSQL = "select A.img" 
						       + " from " + Params.dbName + ".food A"
						       + " where A.food_id = " + fb.getFoodID() + " and A.restaurant_id = " + fb.getRestaurantID();
			
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			
			if(dbCon.rs != null && dbCon.rs.next()){
				fb.setImg(dbCon.rs.getString("img"));
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return fb;
	}
	
}
