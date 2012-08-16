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
						 " where A.restaurant_id = B.restaurant_id and A.taste_id = B.taste_id and A.food_id = C.food_id " +
						 " and A.food_id = " + ft.getFoodID() +
						 " and A.restaurant_id = " + ft.getRestaurantID() +
						 " order by A.rank " +
						 " ";
					
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				item = new FoodTaste();
				item.setFoodID(dbCon.rs.getInt("food_id"));
				item.setFoodName(dbCon.rs.getString("name"));
				item.setTasteID(dbCon.rs.getShort("taste_id"));
				item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
				item.setRank(dbCon.rs.getInt("rank"));
				item.setTasteAlias(dbCon.rs.getShort("taste_alias"));
				item.setTasteName(dbCon.rs.getString("taste_name"));
				item.setTastePrice(dbCon.rs.getFloat("price"));
				item.setTasteCategory(dbCon.rs.getShort("category"));
				item.setTasteRate(dbCon.rs.getFloat("rate"));
				item.setTasteCalc(dbCon.rs.getShort("calc"));
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
	public static int deleteFoodTaste(int foodID, short tasteID, int restaurantID) throws Exception{
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
	public static int deleteDiffTaste(int foodID, String tasteID, int restaurantID) throws Exception{
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
	public static int updateFoodTasteRefType(int foodID, int restaurantID, short tasteRefType) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			String sql = " update " + Params.dbName + ".food set " +
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
	public static void updataBySmart(int foodID, int restaurantID) throws Exception{
		updateFoodTasteRefType(foodID, restaurantID, WebParams.TASTE_SMART_REF);
		Food[] updateFood = QueryMenu.queryFoods(" AND FOOD.food_id = " + foodID, null);
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
	public static int updataByManual(int foodID, String tasteID, int restaurantID) throws Exception{
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
	
	/**
	 * 
	 * @param parent
	 * @param list
	 * @throws Exception
	 */
	public static void updateFoodTaste(FoodTaste parent, FoodTaste[] list) throws Exception{
		DBCon dbCon = new DBCon();
		
		try{
			if(parent == null){
				throw new Exception("操作失败,获取菜品信息失败!");
			}
			
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			String deleteSQL = "delete from " + Params.dbName + ".food_taste_rank where food_id = " + parent.getFoodID() + " and restaurant_id = " + parent.getRestaurantID();
			StringBuffer insertSQL = new StringBuffer();
			
			FoodTasteDao.updateFoodTasteRefType(parent.getFoodID(), parent.getRestaurantID(), parent.getTasteRefType());
			
			if(parent.getTasteRefType() == WebParams.TASTE_SMART_REF){
				Food[] updateFood = QueryMenu.queryFoods(" AND FOOD.food_id = " + parent.getFoodID(), null);
				if(updateFood.length != 1){
					throw new Exception("操作失败,修改菜品口味关联方式为智能关联时发生异常!");
				}
				TasteRef.execByFood(updateFood[0]);
			}else if(parent.getTasteRefType() == WebParams.TASTE_MANUAL_REF){
				dbCon.stmt.execute(deleteSQL);
				
				if(list != null && list.length > 0){
					insertSQL.append("insert into food_taste_rank ");
					insertSQL.append("(food_id, taste_id, restaurant_id, rank) ");
					insertSQL.append(" values");
					for(int i = 0; i< list.length; i++){
						insertSQL.append(i > 0 ? "," : "");
						insertSQL.append("(");
						insertSQL.append(parent.getFoodID());
						insertSQL.append(",");
						insertSQL.append(list[i].getTasteID());
						insertSQL.append(",");
						insertSQL.append(parent.getRestaurantID());
						insertSQL.append(",");
						insertSQL.append(list[i].getRank());
						insertSQL.append(")");
					}
					dbCon.stmt.executeUpdate(insertSQL.toString());
				}
			}
			
			dbCon.conn.commit();
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param parent
	 * @param content
	 * @throws Exception
	 */
	public static void updateFoodTaste(FoodTaste parent, String content) throws Exception{
		try{
			FoodTaste[] list = null;
			FoodTaste item = null;
			String[] sl = content.split("<split>");
			if(sl != null && sl.length != 0){
				if(sl.length == 1 && sl[0].trim().length() == 0){
					list = null;
				}else{
					list = new FoodTaste[sl.length];
					for(int i = 0; i < sl.length; i++){
						String[] temp = sl[i].split(",");
						item = new FoodTaste();
						item.setFoodID(parent.getFoodID());
						item.setRestaurantID(parent.getRestaurantID());
						item.setTasteID(Integer.parseInt(temp[0]));
						item.setRank(Integer.parseInt(temp[1]));
						list[i] = item;
						item = null;
					}
				}
				FoodTasteDao.updateFoodTaste(parent, list);
			}
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 
	 * @param foodID
	 * @param restaurantID
	 * @param tasteRefType
	 * @param content
	 * @throws Exception
	 */
	public static void updateFoodTaste(int foodID, int restaurantID, short tasteRefType, String content) throws Exception{
		try{
			FoodTaste parent = new FoodTaste();
			parent.setFoodID(foodID);
			parent.setRestaurantID(restaurantID);
			parent.setTasteRefType(tasteRefType);
			FoodTasteDao.updateFoodTaste(parent, content);
		}catch(Exception e){
			throw e;
		}
	}
	
}
