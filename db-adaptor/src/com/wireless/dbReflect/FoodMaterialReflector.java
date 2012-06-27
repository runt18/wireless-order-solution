package com.wireless.dbReflect;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.dbObject.FoodMaterial;

/**
 * The DB reflector is designed to the bridge between the FoodMaterial instance of
 * protocol and database.
 * 
 * @author Ying.Zhang 
 */
public class FoodMaterialReflector {

	public static FoodMaterial[] getFoodMaterial(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " FOOD_MATE.consumption AS consumption, FOOD_MATE.material_id AS material_id, FOOD_MATE.food_id AS food_id" +
			  " FROM " + Params.dbName + ".food_material FOOD_MATE " +
			  " WHERE 1=1 " +
			  (extraCond == null ? "" :  " " + extraCond) +
			  (orderClause == null ? "" : " " + orderClause);
	
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		ArrayList<FoodMaterial> foodMaterials = new ArrayList<FoodMaterial>();
		while(dbCon.rs.next()){
			FoodMaterial foodMaterial = new FoodMaterial();
			foodMaterial.food.foodID = dbCon.rs.getLong("food_id");
			foodMaterial.material.materialID = dbCon.rs.getLong("material_id");
			foodMaterial.consumption = dbCon.rs.getFloat("consumption");
			
			foodMaterials.add(foodMaterial);
		}
		
		return foodMaterials.toArray(new FoodMaterial[foodMaterials.size()]);
	}
	
}
