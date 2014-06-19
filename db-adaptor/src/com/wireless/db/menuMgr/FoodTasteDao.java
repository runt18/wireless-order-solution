package com.wireless.db.menuMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.menuMgr.FoodTaste;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteCategory;
import com.wireless.pojo.tasteMgr.TasteCategory.Status;
import com.wireless.pojo.tasteMgr.TasteCategory.Type;

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
				   + " TC.name category_name, TC.type category_type, TC.status category_status "
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
			taste.getCategory().setType(Type.valueOf(dbCon.rs.getInt("category_type")));
			taste.getCategory().setStatus(Status.valueOf(dbCon.rs.getInt("category_status")));
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
	
	
}
