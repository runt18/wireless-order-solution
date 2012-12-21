package com.wireless.db.menuMgr;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryMenu;
import com.wireless.db.tasteRef.TasteRefDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.FoodBasic;
import com.wireless.protocol.Food;
import com.wireless.util.WebParams;

public class FoodBasicDao {
	
	/**
	 * 
	 * @param fb
	 * @throws Exception
	 */
	public static void insertFoodBaisc(FoodBasic fb) throws Exception{		
		DBCon dbCon = new DBCon();
		int queryCount = 0;
		
		try{
			dbCon.connect();
			
			String querySql = "select count(food_alias) 'count' from " + Params.dbName + ".food where restaurant_id = " + fb.getRestaurantID() + " and food_alias = " + fb.getFoodAliasID();
			dbCon.rs = dbCon.stmt.executeQuery(querySql);
			
			while(dbCon.rs != null && dbCon.rs.next()){
				queryCount = dbCon.rs.getInt("count");
				break;
			}
			
			if(queryCount > 0){
				throw new BusinessException("操作失败,该编号菜品已经存在!");
			}
			
			String insertSql = "insert into " + Params.dbName + ".food" 
					+ " ( food_alias, name, pinyin, unit_price, restaurant_id, kitchen_id, kitchen_alias, status, taste_ref_type, food.desc ) "
					+ "values("
					+ fb.getFoodAliasID() + ", " 
					+ "'" + fb.getFoodName() + "', " 
					+ "'" + fb.getPinyin() + "', " 
					+ fb.getUnitPrice() + ", " 
					+ fb.getRestaurantID() + ", " 
					+ fb.getKitchen().getKitchenID() + ", " 
					+ fb.getKitchen().getKitchenAliasID() + ", " 
					+ fb.getStatus() + ", " 
					+ FoodBasic.TASTE_SMART_REF + ", "
					+ (fb.getDesc() == null ? null : "'" + fb.getDesc() + "'")
					+ ")";
			
			dbCon.stmt.executeUpdate(insertSql);
			
			String selectSQL = "select food_id from " + Params.dbName + ".food A where A.food_alias = " + fb.getFoodAliasID() + " and A.restaurant_id = " + fb.getRestaurantID();
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			while(dbCon.rs != null && dbCon.rs.next()){
				fb.setFoodID(dbCon.rs.getInt("food_id"));
			}
			
			try{
				Food[] updateFood = QueryMenu.queryFoods(" AND FOOD.food_id = " + fb.getFoodID(), null);
				if(updateFood.length != 0){
					TasteRefDao.execByFood(updateFood[0]);
				}
			} catch(Exception e){
				throw new BusinessException("警告,已保存新添加菜品信息,但更新口味信息失败!", WebParams.TIP_CODE_WARNING);
			}			
			
		} catch(Exception e){
			throw e;
		} finally {
			dbCon.disconnect();
		}
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
				throw new BusinessException("警告,已保存新添加菜品信息,但保存套餐信息失败!", WebParams.TIP_CODE_ERROE);
			}
			
		} catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 
	 * @param fb
	 * @throws Exception
	 */
	public static void updateFoodBaisc(FoodBasic fb) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String updateSQL = "update " + Params.dbName + ".food " +
					" set name = '" + fb.getFoodName() + "', " + 
					" pinyin = '"+ fb.getPinyin() + "', " + 
					" unit_price =  " + fb.getUnitPrice() + ", " + 
					" kitchen_id =  " + (fb.getKitchen().getKitchenID() < 0 ? null : fb.getKitchen().getKitchenID()) + ", " + 
					" kitchen_alias = " + fb.getKitchen().getKitchenAliasID() + ", " + 
					" status =  " + fb.getStatus() + ", " + 
					" food.desc = " + (fb.getDesc() == null || fb.getDesc().trim().length() == 0 ? null : "'" + fb.getDesc() + "'") +
					" where restaurant_id=" + fb.getRestaurantID() + " and food_id = " + fb.getFoodID();
			
			int count = dbCon.stmt.executeUpdate(updateSQL);
			if(count != 1){
				throw new Exception("操作失败,找不到要修改的菜品!");
			}
		} catch(Exception e){
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param fb
	 * @throws Exception
	 */
	public static void deleteFood(FoodBasic fb) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			String deleteSQL = "";
			
			// delete food
			deleteSQL = "delete from " + Params.dbName + ".food where food_id = " + fb.getFoodID() + " and restaurant_id = " + fb.getRestaurantID();
			dbCon.stmt.executeUpdate(deleteSQL);
			
			// delete foodTaste
			deleteSQL = "delete from " + Params.dbName + ".food_taste_rank where food_id = " + fb.getFoodID() + " and restaurant_id = " + fb.getRestaurantID();
			dbCon.stmt.executeUpdate(deleteSQL);
			deleteSQL = "delete from " + Params.dbName + ".food_taste where food_id = " + fb.getFoodID() + " and restaurant_id = " + fb.getRestaurantID();
			dbCon.stmt.executeUpdate(deleteSQL);
			
			// delete foodMaterial
			deleteSQL = "delete from " + Params.dbName + ".food_material where food_id = " + fb.getFoodID() + " and restaurant_id = " + fb.getRestaurantID();
			dbCon.stmt.executeUpdate(deleteSQL);
			
			// delete foodCombination
			deleteSQL = "delete from " + Params.dbName + ".combo where food_id = " + fb.getFoodID() + " and restaurant_id = " + fb.getRestaurantID();
			dbCon.stmt.executeUpdate(deleteSQL);
			
			dbCon.conn.commit();
		} catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		} finally{
			dbCon.disconnect();
		}
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
