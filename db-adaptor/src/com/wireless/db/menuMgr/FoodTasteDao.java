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
			
			String sql = "SELECT A.FOOD_ID, A.TASTE_ID, A.RESTAURANT_ID, A.RANK, " +
					" B.TASTE_ALIAS, B.PREFERENCE AS TASTE_NAME, B.PRICE, B.CATEGORY, B.RATE, B.CALC, " +
					" C.NAME " +
					" FROM  " + Params.dbName + ".FOOD_TASTE_RANK A ," + Params.dbName + ".TASTE B, " + Params.dbName + ".FOOD C " +
					" WHERE A.RESTAURANT_ID = B.RESTAURANT_ID AND A.TASTE_ID = B.TASTE_ID AND A.FOOD_ID = C.FOOD_ID " +
					" AND A.FOOD_ID = " + ft.getFoodID() +
					" AND A.RESTAURANT_ID = " + ft.getRestaurantID() +
					" ORDER BY A.RANK DESC " +
					" "; 
					
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				item = new FoodTaste();
				item.setFoodID(dbCon.rs.getLong("FOOD_ID"));
				item.setFoodName(dbCon.rs.getString("NAME"));
				item.setTasteID(dbCon.rs.getLong("TASTE_ID"));
				item.setRestaurantID(dbCon.rs.getLong("RESTAURANT_ID"));
				item.setRank(dbCon.rs.getInt("RANK"));
				item.setTasteAlias(dbCon.rs.getLong("TASTE_ALIAS"));
				item.setTasteName(dbCon.rs.getString("TASTE_NAME"));
				item.setTastePrice(dbCon.rs.getFloat("PRICE"));
				item.setTasteCategory(dbCon.rs.getLong("CATEGORY"));
				item.setTasteRate(dbCon.rs.getFloat("RATE"));
				item.setTasteCalc(dbCon.rs.getLong("CALC"));
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
			String sql = "INSERT INTO " + Params.dbName + ".FOOD_TASTE_RANK (FOOD_ID, RESTAURANT_ID, TASTE_ID, RANK) " +
					" VALUES(" +
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
			String sql = "DELETE FROM FOOD_TASTE_RANK " +
						 " WHERE 1=1 " + 
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
		String extraCond = " AND FOOD_ID = " + ft.getFoodID() +
						   " AND TASTE_ID IN (" + ft.getTasteID() + ")" + 
						   " AND RESTAURANT_ID = " + ft.getRestaurantID() + "";
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
		String extraCond = " AND FOOD_ID = " + foodID +
						   " AND RESTAURANT_ID = " + restaurantID + 
						   " AND TASTE_ID IN (" + tasteID + ")";
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
		String extraCond = " AND FOOD_ID = " + ft.getFoodID() +
				   " AND RESTAURANT_ID = " + ft.getRestaurantID() + 
				   " AND TASTE_ID NOT IN (" + ft.getTasteID() + ")";
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
		String extraCond = " AND FOOD_ID = " + foodID +
				   " AND RESTAURANT_ID = " + restaurantID +
				   " AND TASTE_ID NOT IN (" + tasteID + ")";
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
			String sql = "UPDATE WIRELESS_ORDER_DB.FOOD SET " +
						 " TASTE_REF_TYPE = " + tasteRefType + 
						 " WHERE 1=1 " +
						 " AND RESTAURANT_ID = " + restaurantID + 
						 " AND FOOD_ID = " + foodID;
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
		Food[] updateFood = QueryMenu.queryFoods(" AND FOOD.FOOD_ID = " + foodID, null);
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
