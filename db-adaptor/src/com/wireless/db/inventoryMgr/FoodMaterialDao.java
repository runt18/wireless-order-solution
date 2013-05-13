package com.wireless.db.inventoryMgr;

import java.sql.SQLException;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorLevel;
import com.wireless.exception.MaterialError;
import com.wireless.pojo.inventoryMgr.FoodMaterial;

public class FoodMaterialDao {
	
	/**
	 * 
	 * @param dbCon
	 * @param item
	 * @return
	 * @throws SQLException
	 */
	public static int insert(DBCon dbCon, FoodMaterial item) throws SQLException{
		int count = 0;
		String insertSQL = "INSERT INTO food_material (food_id, material_id, restaurant_id, consumption)"
						 + " VALUES(" + item.getFoodId() + ", " 
						 + item.getMaterialId() + ", " 
						 + item.getRestaurantId() + ", " 
						 + item.getConsumption() + ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		return count;	
	}
	
	/**
	 * 
	 * @param item
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void insertFoodMaterial(FoodMaterial item) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			int count = FoodMaterialDao.insert(dbCon, item);
			if(count == 0){
				throw new BusinessException(MaterialError.BINDING_INSERT_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param list
	 * @return
	 * @throws SQLException
	 */
	public static void insertList(DBCon dbCon, List<FoodMaterial> list) throws BusinessException, SQLException{
		if(list == null || !list.isEmpty()){
			throw new NullPointerException("The list is null or empty. ");
		}
		StringBuffer midList = null;
		for(int i = 0; i < list.size(); i++){
			try{
				FoodMaterialDao.insert(dbCon, list.get(i));
			}catch(Exception e){
				if(midList == null)
					midList = new StringBuffer();
				midList.append((i > 0 ? "," : "") + list.get(i).getMaterialId());
			}
		}
		if(midList != null){
			throw BusinessException.defined("操作未完全成功, 以下编号原料未能绑定菜品信息:" + midList.toString(), ErrorLevel.WARNING);
		}
	}
	
	/**
	 * 
	 * @param list
	 * @throws SQLException
	 */
	public static void insertList(List<FoodMaterial> list) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			FoodMaterialDao.insertList(dbCon, list);
		}finally{
			dbCon.disconnect();
		}
	}
	
}
