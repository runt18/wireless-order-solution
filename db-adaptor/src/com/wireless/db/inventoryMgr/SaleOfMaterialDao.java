package com.wireless.db.inventoryMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.pojo.inventoryMgr.SaleOfMaterial;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;

public class SaleOfMaterialDao {

	/**
	 * 销售对账单列表
	 * @param dbCon
	 * @param staff
	 * @param materialId
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 */
	public static List<SaleOfMaterial> saleOfMaterialList(DBCon dbCon, Staff staff, int materialId, String extraCond, String orderClause) throws SQLException{
		List<SaleOfMaterial> saleOfMaterials = new ArrayList<SaleOfMaterial>();
		String sql = "SELECT TMP.food_id, TMP.food_name, TMP.total, TMP.rate, (TMP.rate * TMP.total) consume FROM (" +
					" SELECT (SELECT cONsumptiON FROM food_material S_FM WHERE S_FM.food_id = FM.food_id and S_FM.material_id = " + materialId +") " +
					" AS rate, FM.food_id, F.name food_name, SUM(OF.order_count) total "+
					" FROM food_material FM " +
					" JOIN food F ON F.food_id = FM.food_id " +
					" JOIN kitchen K ON F.kitchen_id = K.kitchen_id " +
					" JOIN order_food_history OF ON OF.food_id = FM.food_id " +
					" WHERE FM.restaurant_id = " + staff.getRestaurantId() +
					" AND FM.material_id = " + materialId + 
					(extraCond != null ? extraCond : " ") +
					" GROUP BY FM.food_id " +
					") AS TMP";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			SaleOfMaterial som = new SaleOfMaterial();
			Food f = new Food(dbCon.rs.getInt("food_id"));
			f.setName(dbCon.rs.getString("food_name"));
			som.setFood(f);
			som.setAmount(dbCon.rs.getInt("total"));
			som.setRate(dbCon.rs.getFloat("rate"));
			som.setConsume(dbCon.rs.getFloat("consume"));
			
			saleOfMaterials.add(som);
		}		
		
		return saleOfMaterials;
	} 
	
	/**
	 * 销售对账单列表
	 * @param staff
	 * @param materialId
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 */
	public static List<SaleOfMaterial> saleOfMaterialList(Staff staff, int materialId, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return saleOfMaterialList(dbCon, staff, materialId, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
}
