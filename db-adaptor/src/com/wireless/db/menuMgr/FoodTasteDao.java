package com.wireless.db.menuMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryMenu;
import com.wireless.db.tasteRef.TasteRef;
import com.wireless.pojo.menuMgr.FoodTaste;
import com.wireless.protocol.Food;
import com.wireless.util.WebParams;

public class FoodTasteDao {
	
	/**
	 * 
	 * @param arg
	 * @return
	 * @throws Exception
	 */
	public static FoodTaste[] getFoodCommonTaste(FoodTaste ft) throws Exception{
		
		DBCon dbCon = new DBCon();
		List<FoodTaste> list = new ArrayList<FoodTaste>();
		FoodTaste item = null;
		FoodTaste[] rs = null;
		try{
			dbCon.connect();
			
			String sql = " select A.food_id, A.taste_id, A.restaurant_id, A.rank, " + 
						 " B.taste_alias, B.preference as taste_name, B.price, B.category, B.rate, B.calc, " +
						 " C.name " +
						 " from " + Params.dbName + ".food_taste_rank A, " + Params.dbName + ".taste B, " + Params.dbName + ".food C " +
						 " where A.restaurant_id = B.restaurant_id and a.taste_id = B.taste_id and A.food_id = C.food_id " +
						 " and A.food_id = " + ft.getFoodID() +
						 " and A.restaurant_id = " + ft.getRestaurantID() +
						 " order by A.rank desc " +
						 " ";
					
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				item = new FoodTaste();
				item.setFoodID(dbCon.rs.getInt("food_id"));
				item.setFoodName(dbCon.rs.getInt("name"));
				item.setTasteID(dbCon.rs.getLong("taste_id"));
				item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
				item.setRank(dbCon.rs.getInt("rank"));
				item.setTasteAlias(dbCon.rs.getLong("taste_alias"));
				item.setTasteName(dbCon.rs.getString("taste_name"));
				item.setTastePrice(dbCon.rs.getFloat("price"));
				item.setTasteCategory(dbCon.rs.getLong("category"));
				item.setTasteRate(dbCon.rs.getFloat("rate"));
				item.setTasteCalc(dbCon.rs.getLong("calc"));
				list.add(item);
				item = null;
			}
			
		} catch(Exception e){
			throw e;
		} finally{
			dbCon.disconnect();
			rs = list.toArray(new FoodTaste[list.size()]);
		}
		
		return rs;
	}
	
	/**
	 * 
	 * @param ft
	 * @return
	 * @throws Exception
	 */
	public static int insertFoodTaste(FoodTaste ft) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			updateFoodTasteRefType(ft.getFoodID(), ft.getRestaurantID(), WebParams.TASTE_MANUAL_REF);
			
			dbCon.connect();
			String sql = "insert into " + Params.dbName + ".food_taste_rank (food_id, restaurant_id, taste_id, rank) " +
					" values(" +
					ft.getFoodID() + "," +
					ft.getRestaurantID() + "," +
					ft.getTasteID() + "," +
					ft.getRank() + "" +
					")";
			count = dbCon.stmt.executeUpdate(sql);
		} catch(Exception e){
			throw e;
		} finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param extraCond
	 * @return
	 * @throws Exception
	 */
	public static int deleteModel(String extraCond) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			String sql = "delete from food_taste_rank " +
						 " where 1=1 " + 
						 (extraCond == null ? "" : extraCond);
			count = dbCon.stmt.executeUpdate(sql);
		} catch(Exception e){
			throw e;
		} finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param ft
	 * @return
	 * @throws Exception
	 */
	public static int deleteFoodTaste(FoodTaste ft) throws Exception{
		updateFoodTasteRefType(ft.getFoodID(), ft.getRestaurantID(), WebParams.TASTE_MANUAL_REF);
		String extraCond = " and food_id = " + ft.getFoodID() +
						   " and taste_id in (" + ft.getTasteID() + ")" + 
						   " and restaurant_id = " + ft.getRestaurantID() + "";
		return deleteModel(extraCond);
	}
	
	/**
	 * 
	 * @param foodID
	 * @param tasteID
	 * @param restaurantID
	 * @return
	 * @throws Exception
	 */
	public static int deleteFoodTaste(long foodID, long tasteID, long restaurantID) throws Exception{
		updateFoodTasteRefType(foodID, restaurantID, WebParams.TASTE_MANUAL_REF);
		String extraCond = " and food_id = " + foodID +
						   " and restaurant_id = " + restaurantID + 
						   " and taste_id in (" + tasteID + ")";
		return deleteModel(extraCond);
	}
	
	/**
	 * 
	 * @param ft
	 * @return
	 * @throws Exception
	 */
	public static int deleteDiffTaste(FoodTaste ft) throws Exception{
		updateFoodTasteRefType(ft.getFoodID(), ft.getRestaurantID(), WebParams.TASTE_MANUAL_REF);
		String extraCond = " and food_id = " + ft.getFoodID() +
				   " and restaurant_id = " + ft.getRestaurantID() + 
				   " and taste_id not in (" + ft.getTasteID() + ")";
		return deleteModel(extraCond);
	}
	
	/**
	 * 
	 * @param foodID
	 * @param tasteID
	 * @param restaurantID
	 * @return
	 * @throws Exception
	 */
	public static int deleteDiffTaste(long foodID, String tasteID, long restaurantID) throws Exception{
		updateFoodTasteRefType(foodID, restaurantID, WebParams.TASTE_MANUAL_REF);
		String extraCond = " and food_id = " + foodID +
				   " and restaurant_id = " + restaurantID +
				   " and taste_id not in (" + tasteID + ")";
		return deleteModel(extraCond);
	}
	
	/**
	 * 
	 * @param foodID
	 * @param restaurantID
	 * @param tasteRefType
	 * @return
	 * @throws Exception
	 */
	public static int updateFoodTasteRefType(long foodID, long restaurantID, long tasteRefType) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			String sql = " update " + Params.dbName + ".FOOD SET " +
						 " taste_ref_type = " + tasteRefType + 
						 " where 1=1 " +
						 " and restaurant_id = " + restaurantID + 
						 " and food_id = " + foodID;
			count = dbCon.stmt.executeUpdate(sql);
		} catch(Exception e) {
			throw e;
		} finally {
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param foodID
	 * @return
	 * @throws SQLException 
	 */
	public static void updataBySmart(long foodID, long restaurantID) throws Exception{
		updateFoodTasteRefType(foodID, restaurantID, WebParams.TASTE_SMART_REF);
		Food[] updateFood = QueryMenu.queryFoods(" and food.food_id = " + foodID, null);
		if(updateFood.length != 1){
			throw new Exception();
		}
		TasteRef.execByFood(updateFood[0]);
	}
	
	/**
	 * 
	 * @param foodID
	 * @param tasteID
	 * @param restaurantID
	 * @return
	 * @throws Exception
	 */
	public static int updataByManual(long foodID, String tasteID, long restaurantID) throws Exception{
		if(tasteID == null || tasteID.trim().length() == 0){
			throw new Exception();
		}
		// 容错处理
		String[] check = tasteID.split(",");
		tasteID = "";
		for(int i = 0; i < check.length; i++){
			if(check[i].trim().length() > 0){
				tasteID += (tasteID.length() > 0 ? "," : "");
				tasteID += check[i].trim();
			}
		}
		if(tasteID.trim().length() == 0){
			throw new Exception();
		}
		deleteDiffTaste(foodID, tasteID, restaurantID);
		return updateFoodTasteRefType(foodID, restaurantID, WebParams.TASTE_MANUAL_REF);
	}
	
}
