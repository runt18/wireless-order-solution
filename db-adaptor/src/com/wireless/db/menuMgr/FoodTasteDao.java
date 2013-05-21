package com.wireless.db.menuMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.tasteRef.TasteRefDao;
import com.wireless.pojo.dishesOrder.Food;
import com.wireless.pojo.menuMgr.FoodTaste;

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
						 " B.taste_alias, B.preference as taste_name, B.price, B.category, B.rate, B.calc, B.type, " +
						 " C.name " +
						 " from " + Params.dbName + ".food_taste_rank A, " + Params.dbName + ".taste B, " + Params.dbName + ".food C " +
						 " where A.restaurant_id = B.restaurant_id and A.taste_id = B.taste_id and A.food_id = C.food_id " +
						 " and A.food_id = " + ft.getFoodId() +
						 " and A.restaurant_id = " + ft.getRestaurantId() +
						 " order by A.rank " +
						 " ";
					
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				item = new FoodTaste();
				item.setFoodId(dbCon.rs.getInt("food_id"));
				item.setName(dbCon.rs.getString("name"));
				item.setTasteID(dbCon.rs.getShort("taste_id"));
				item.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				item.setRank(dbCon.rs.getInt("rank"));
				item.setTasteAliasID(dbCon.rs.getInt("taste_alias"));
				item.setTasteName(dbCon.rs.getString("taste_name"));
				item.setTastePrice(dbCon.rs.getFloat("price"));
				item.setTasteCategory(dbCon.rs.getInt("category"));
				item.setTasteRate(dbCon.rs.getFloat("rate"));
				item.setTasteCalc(dbCon.rs.getInt("calc"));
				item.setType(dbCon.rs.getInt("type"));
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
			FoodTasteDao.updateFoodTasteRefType(ft.getFoodId(), ft.getRestaurantId(), Food.TasteRef.MANUAL);
			dbCon.connect();
			String sql = "insert into " + Params.dbName + ".food_taste_rank (food_id, restaurant_id, taste_id, rank) " +
					" values(" +
					ft.getFoodId() + "," +
					ft.getRestaurantId() + "," +
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
		FoodTasteDao.updateFoodTasteRefType(ft.getFoodId(), ft.getRestaurantId(), Food.TasteRef.MANUAL);
		String extraCond = " and food_id = " + ft.getFoodId() +
						   " and taste_id in (" + ft.getTasteID() + ")" + 
						   " and restaurant_id = " + ft.getRestaurantId() + "";
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
	public static int deleteFoodTaste(long foodID, short tasteID, int restaurantID) throws Exception{
		FoodTasteDao.updateFoodTasteRefType(foodID, restaurantID, Food.TasteRef.MANUAL);
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
		FoodTasteDao.updateFoodTasteRefType(ft.getFoodId(), ft.getRestaurantId(), Food.TasteRef.MANUAL);
		String extraCond = " and food_id = " + ft.getFoodId() +
				   " and restaurant_id = " + ft.getRestaurantId() + 
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
	public static int deleteDiffTaste(long foodID, String tasteID, int restaurantID) throws Exception{
		FoodTasteDao.updateFoodTasteRefType(foodID, restaurantID, Food.TasteRef.MANUAL);
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
	public static int updateFoodTasteRefType(long foodID, int restaurantID, Food.TasteRef tasteRefType) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			String sql = " update " + Params.dbName + ".food set " +
						 " taste_ref_type = " + tasteRefType.getVal() + 
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
	public static void updataBySmart(long foodID, int restaurantID) throws Exception{
		FoodTasteDao.updateFoodTasteRefType(foodID, restaurantID, Food.TasteRef.SMART);
		List<Food> updateFood = FoodDao.getPureFoods(" AND FOOD.food_id = " + foodID, null);
		if(updateFood.isEmpty()){
			throw new Exception();
		}else{
			TasteRefDao.execByFood(updateFood.get(0));
		}
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
		FoodTasteDao.deleteDiffTaste(foodID, tasteID, restaurantID);
		return FoodTasteDao.updateFoodTasteRefType(foodID, restaurantID, Food.TasteRef.MANUAL);
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
			
			String deleteSQL = "delete from " + Params.dbName + ".food_taste_rank where food_id = " + parent.getFoodId() + " and restaurant_id = " + parent.getRestaurantId();
			StringBuffer insertSQL = new StringBuffer();
			
			FoodTasteDao.updateFoodTasteRefType(parent.getFoodId(), parent.getRestaurantId(), parent.getTasteRefType());
			
			if(parent.getTasteRefType() == Food.TasteRef.SMART){
				List<Food> updateFood = FoodDao.getPureFoods(" AND FOOD.food_id = " + parent.getFoodId(), null);
				if(updateFood.isEmpty()){
					throw new Exception("操作失败,修改菜品口味关联方式为智能关联时发生异常!");
				}else{
					TasteRefDao.execByFood(updateFood.get(0));
				}
			}else if(parent.getTasteRefType() == Food.TasteRef.MANUAL){
				dbCon.stmt.execute(deleteSQL);
				
				if(list != null && list.length > 0){
					insertSQL.append("insert into food_taste_rank ");
					insertSQL.append("(food_id, taste_id, restaurant_id, rank) ");
					insertSQL.append(" values");
					for(int i = 0; i< list.length; i++){
						insertSQL.append(i > 0 ? "," : "");
						insertSQL.append("(");
						insertSQL.append(parent.getFoodId());
						insertSQL.append(",");
						insertSQL.append(list[i].getTasteID());
						insertSQL.append(",");
						insertSQL.append(parent.getRestaurantId());
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
						item.setFoodId(parent.getFoodId());
						item.setRestaurantId(parent.getRestaurantId());
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
	public static void updateFoodTaste(int foodID, int restaurantID, Food.TasteRef tasteRefType, String content) throws Exception{
		try{
			FoodTaste parent = new FoodTaste();
			parent.setFoodId(foodID);
			parent.setRestaurantId(restaurantID);
			parent.setTasteRefType(tasteRefType);
			FoodTasteDao.updateFoodTaste(parent, content);
		}catch(Exception e){
			throw e;
		}
	}
	
}
