package com.wireless.db.menuMgr;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.menuMgr.FoodMaterial;

public class FoodMaterialDao {
	
	public static void updateFoodMaterial(FoodMaterial item){
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			String sql = "UPDATE " + Params.dbName + ".food_material SET consumption = " + item.getConsumption()
						 + " WHERE"
						 + " restaurant_id = " + item.getRestaurantID()
						 + " AND food_id = " + item.getFoodID()
						 + " AND material_id = " + item.getMaterialID();
			
			dbCon.stmt.executeUpdate(sql);
			
		} catch(Exception e){
			System.out.println(e.getMessage());
		} finally{
			dbCon.disconnect();
		}
		
	}
}
