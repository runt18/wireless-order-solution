package com.wireless.db.menuMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.tasteRef.TasteRefDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorLevel;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodTaste;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteCategory;
import com.wireless.pojo.tasteMgr.TasteCategory.Status;

public class FoodTasteDao {
	
	/**
	 * 
	 * @param dbCon
	 * @param restaurantId
	 * @param foodId
	 * @return
	 * @throws SQLException
	 */
	public static List<FoodTaste> getFoodCommonTaste(DBCon dbCon, int restaurantId, int foodId) throws SQLException{
		List<FoodTaste> list = new ArrayList<FoodTaste>();
		FoodTaste item = null;
		Taste taste = null;
		String sql = " SELECT A.food_id, A.taste_id, A.restaurant_id, A.rank, "  
				   + " B.preference as taste_name, B.price, B.category_id, B.rate, B.calc, B.type, "
				   + " C.name food_name, C.food_alias food_alias, "
				   + " TC.name category_name, TC.type category_type "
				   + " FROM " + Params.dbName + ".food_taste_rank A, " + Params.dbName + ".taste B LEFT JOIN " + Params.dbName + ".taste_category TC ON B.category_id = TC.category_id, " + Params.dbName + ".food C "
				   + " WHERE A.restaurant_id = B.restaurant_id AND A.taste_id = B.taste_id AND A.food_id = C.food_id "
				   + " AND A.food_id = " + foodId
				   + " AND A.restaurant_id = " + restaurantId
				   + " ORDER BY A.rank ";
					
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			taste = new Taste(dbCon.rs.getInt("taste_id"));
			taste.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			taste.setPreference(dbCon.rs.getString("taste_name"));
			taste.setPrice(dbCon.rs.getFloat("price"));
			taste.setCategory(new TasteCategory(dbCon.rs.getInt("category_id"), dbCon.rs.getString("category_name")));
			taste.getCategory().setStatus(Status.valueOf(dbCon.rs.getInt("category_type")));
			taste.setRate(dbCon.rs.getFloat("rate"));
			taste.setCalc(dbCon.rs.getInt("calc"));
			taste.setType(dbCon.rs.getInt("type"));
			taste.setRank(dbCon.rs.getInt("rank"));
			item = new FoodTaste(taste);
			item.setFood(dbCon.rs.getInt("food_id"), 
					dbCon.rs.getInt("food_alias"), 
					taste.getRestaurantId(),
					dbCon.rs.getString("food_name"));
			list.add(item);
			item = null;
		}
		dbCon.rs.close();
		dbCon.rs = null;
		return list;
	}
	
	public static List<FoodTaste> getFoodCommonTaste(int restaurantId, int foodId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return FoodTasteDao.getFoodCommonTaste(dbCon, restaurantId, foodId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param ft
	 * @return
	 * @throws SQLException
	 */
	public static int insertFoodTaste(FoodTaste ft) throws SQLException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			FoodTasteDao.updateFoodTasteRefType(ft.getFood().getFoodId(), ft.getFood().getRestaurantId(), Food.TasteRef.MANUAL);
			dbCon.connect();
			String sql = "insert into " + Params.dbName + ".food_taste_rank (food_id, restaurant_id, taste_id, rank) " +
					" values(" +
					ft.getFood().getFoodId() + "," +
					ft.getFood().getRestaurantId() + "," +
					ft.getTaste().getTasteId() + "," +
					ft.getTaste().getRank() + "" +
					")";
			count = dbCon.stmt.executeUpdate(sql);
		} catch(SQLException e){
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
	 * @throws SQLException
	 */
	public static int deleteModel(String extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			String sql = "DELETE FROM food_taste_rank " +
						 " WHERE 1=1 " + 
						 (extraCond == null ? "" : extraCond);
			count = dbCon.stmt.executeUpdate(sql);
		} catch(SQLException e){
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
	 * @throws SQLException
	 */
	public static int deleteFoodTaste(FoodTaste ft) throws SQLException{
		FoodTasteDao.updateFoodTasteRefType(ft.getFood().getFoodId(), ft.getFood().getRestaurantId(), Food.TasteRef.MANUAL);
		String extraCond = " AND food_id = " + ft.getFood().getFoodId() +
						   " AND taste_id in (" + ft.getFood().getFoodId() + ")" + 
						   " AND restaurant_id = " + ft.getFood().getRestaurantId() + "";
		return FoodTasteDao.deleteModel(extraCond);
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
	 * @throws SQLException
	 */
	public static int deleteDiffTaste(FoodTaste ft) throws SQLException{
		FoodTasteDao.updateFoodTasteRefType(ft.getFood().getFoodId(), ft.getFood().getRestaurantId(), Food.TasteRef.MANUAL);
		String extraCond = " AND food_id = " + ft.getFood().getFoodId() +
				   " AND restaurant_id = " + ft.getFood().getRestaurantId() + 
				   " AND taste_id not in (" + ft.getTaste().getTasteId() + ")";
		return FoodTasteDao.deleteModel(extraCond);
	}
	
	/**
	 * 
	 * @param foodID
	 * @param tasteID
	 * @param restaurantID
	 * @return
	 * @throws Exception
	 */
	public static int deleteDiffTaste(long foodID, String tasteID, int restaurantID) throws SQLException{
		FoodTasteDao.updateFoodTasteRefType(foodID, restaurantID, Food.TasteRef.MANUAL);
		String extraCond = " AND food_id = " + foodID +
				   " AND restaurant_id = " + restaurantID +
				   " AND taste_id not in (" + tasteID + ")";
		return FoodTasteDao.deleteModel(extraCond);
	}
	
	/**
	 * 
	 * @param foodId
	 * @param restaurantId
	 * @param tasteRefType
	 * @return
	 * @throws SQLException
	 */
	public static int updateFoodTasteRefType(long foodId, int restaurantId, Food.TasteRef tasteRefType) throws SQLException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			String sql = " update " + Params.dbName + ".food set " +
						 " taste_ref_type = " + tasteRefType.getVal() + 
						 " where 1=1 " +
						 " and restaurant_id = " + restaurantId + 
						 " and food_id = " + foodId;
			count = dbCon.stmt.executeUpdate(sql);
		} catch(SQLException e) {
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
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void updateFoodTaste(FoodTaste parent, FoodTaste[] list) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		
		try{
			if(parent == null){
				throw new BusinessException("操作失败, 获取菜品信息失败!", ErrorLevel.ERROR);
			}
			
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			String deleteSQL = "delete from " + Params.dbName + ".food_taste_rank where food_id = " + parent.getFood().getFoodId() + " and restaurant_id = " + parent.getFood().getRestaurantId();
			StringBuffer insertSQL = new StringBuffer();
			
			FoodTasteDao.updateFoodTasteRefType(parent.getFood().getFoodId(), parent.getFood().getRestaurantId(), parent.getFood().getTasteRefType());
			
			if(parent.getFood().getTasteRefType() == Food.TasteRef.SMART){
				List<Food> updateFood = FoodDao.getPureFoods(" AND FOOD.food_id = " + parent.getFood().getFoodId(), null);
				if(updateFood.isEmpty()){
					throw new BusinessException("操作失败,修改菜品口味关联方式为智能关联时发生异常!", ErrorLevel.ERROR);
				}else{
					TasteRefDao.execByFood(updateFood.get(0));
				}
			}else if(parent.getFood().getTasteRefType() == Food.TasteRef.MANUAL){
				dbCon.stmt.execute(deleteSQL);
				
				if(list != null && list.length > 0){
					insertSQL.append("insert into food_taste_rank ");
					insertSQL.append("(food_id, taste_id, restaurant_id, rank) ");
					insertSQL.append(" values");
					for(int i = 0; i< list.length; i++){
						insertSQL.append(i > 0 ? "," : "");
						insertSQL.append("(");
						insertSQL.append(parent.getFood().getFoodId());
						insertSQL.append(",");
						insertSQL.append(list[i].getTaste().getTasteId());
						insertSQL.append(",");
						insertSQL.append(parent.getFood().getRestaurantId());
						insertSQL.append(",");
						insertSQL.append(list[i].getTaste().getRank());
						insertSQL.append(")");
					}
					dbCon.stmt.executeUpdate(insertSQL.toString());
				}
			}
			
			dbCon.conn.commit();
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
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
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void updateFoodTaste(FoodTaste parent, String content) throws BusinessException, SQLException{
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
					item.getFood().setFoodId(parent.getFood().getFoodId());
					item.getFood().setRestaurantId(parent.getFood().getRestaurantId());
					item.getTaste().setTasteId(Integer.parseInt(temp[0]));
					item.getTaste().setRank(Integer.parseInt(temp[1]));
					list[i] = item;
					item = null;
				}
			}
			FoodTasteDao.updateFoodTaste(parent, list);
		}
	}
	
	/**
	 * 
	 * @param foodId
	 * @param restaurantId
	 * @param tasteRefType
	 * @param content
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void updateFoodTaste(int foodId, int restaurantId, Food.TasteRef tasteRefType, String content) throws BusinessException, SQLException{
		FoodTaste parent = new FoodTaste();
		parent.getFood().setFoodId(foodId);
		parent.getFood().setRestaurantId(restaurantId);
		parent.getFood().setTasteRefType(tasteRefType);
		FoodTasteDao.updateFoodTaste(parent, content);
	}
	
}
