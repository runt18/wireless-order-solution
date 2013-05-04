package com.wireless.db.foodMgr;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.foodMgr.FoodMaterial;
import com.wireless.protocol.Terminal;
public class FoodMaterialDAO{
	public static boolean add(Terminal terminal,FoodMaterial foodMaterial) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return add(dbCon, terminal, foodMaterial);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static boolean add(DBCon dbCon,Terminal terminal,FoodMaterial foodMaterial) throws SQLException{
		String sql = "INSERT INTO "+Params.dbName+".food_material("+
							"wireless_order_db.food_material.food_id,"+
							"wireless_order_db.food_material.material_id,"+
							"wireless_order_db.food_material.restaurant_id,"+
							"wireless_order_db.food_material.consumption) values ("+
							"0,"+
							((foodMaterial.getMaterialId() != null) ?(foodMaterial.getMaterialId()) : "0")+","+
							((foodMaterial.getRestaurantId() != null) ?(foodMaterial.getRestaurantId()) : "0")+","+
							((foodMaterial.getConsumption() != null) ?(foodMaterial.getConsumption()) : "0.0")+");";
		return dbCon.stmt.execute(sql);
	}
	public static boolean remove(Terminal terminal,String whereCondition)  throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return remove(dbCon, terminal, whereCondition);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static boolean remove(DBCon dbCon,Terminal terminal,String whereCondition)  throws SQLException{
		String sql = "DELETE FROM wireless_order_db.food_material "+whereCondition;
		return dbCon.stmt.executeUpdate(sql)>0;
	}
	public static List<FoodMaterial> query(Terminal terminal,String andCondition) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return query(dbCon, terminal, andCondition);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static List<FoodMaterial> query(DBCon dbCon,Terminal terminal,String andCondition) throws SQLException{
		List<FoodMaterial> foodMaterials = new ArrayList<FoodMaterial>();
		String sql = 
			" SELECT "+
			" FM.*, "+
			" F.name AS fname, "+
			" M.name AS mname, "+
			" MC.name AS cname "+
			" FROM wireless_order_db.food_material FM, "+
			" wireless_order_db.material M, "+
			" wireless_order_db.food F, "+
			" wireless_order_db.material_cate MC "+
			" WHERE FM.material_id = M.material_id "+
			" AND FM.food_id = F.food_id "+
			" AND FM.restaurant_id = F.restaurant_id "+
			" AND M.cate_id = MC.cate_id "+andCondition;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			FoodMaterial foodMaterial = new FoodMaterial();
			foodMaterial.setFoodId((Integer)dbCon.rs.getInt("food_id"));
			foodMaterial.setMaterialId((Integer)dbCon.rs.getInt("material_id"));
			foodMaterial.setRestaurantId((Integer)dbCon.rs.getInt("restaurant_id"));
			foodMaterial.setConsumption((Float)dbCon.rs.getObject("consumption"));
			foodMaterial.setFname((String)dbCon.rs.getObject("fname"));
			foodMaterial.setCname((String)dbCon.rs.getObject("cname"));
			foodMaterial.setMname((String)dbCon.rs.getObject("mname"));
			foodMaterials.add(foodMaterial);
	}
		return foodMaterials;
	}
	public static boolean update(Terminal terminal,FoodMaterial foodMaterial,String whereCondition)  throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return update(dbCon, terminal, foodMaterial, whereCondition);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static boolean update(DBCon dbCon,Terminal terminal,FoodMaterial foodMaterial,String whereCondition)  throws SQLException{
		String sql = "UPDATE wireless_order_db.food_material SET "+
							"wireless_order_db.food_material.material_id = "+((foodMaterial.getMaterialId() != null) ?(foodMaterial.getMaterialId()) : "0")+","+
							"wireless_order_db.food_material.restaurant_id = "+((foodMaterial.getRestaurantId() != null) ?(foodMaterial.getRestaurantId()) : "0")+","+
							"wireless_order_db.food_material.consumption = "+((foodMaterial.getConsumption() != null) ?(foodMaterial.getConsumption()) : "0.0")+" "+whereCondition+";";
		return dbCon.stmt.executeUpdate(sql)>0;
	}

}
