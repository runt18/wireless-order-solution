package com.wireless.db.inventoryMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorLevel;
import com.wireless.exception.FoodError;
import com.wireless.exception.MaterialError;
import com.wireless.pojo.inventoryMgr.FoodMaterial;
import com.wireless.util.SQLUtil;

public class FoodMaterialDao {
	
	/**
	 * 
	 * @param dbCon
	 * @param item
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int insert(DBCon dbCon, FoodMaterial item) throws BusinessException, SQLException{
		int count = 0;
		String querySQL = "", insertSQL = "";
		querySQL = "SELECT COUNT(*) FROM food WHERE food_id = " + item.getFoodId();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt(1) == 0){
			throw new BusinessException(FoodError.NOT_FIND);
		}
		insertSQL = "INSERT INTO food_material (food_id, material_id, restaurant_id, consumption)"
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
	public static void insert(FoodMaterial item) throws BusinessException, SQLException{
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
			throw new BusinessException("操作未完全成功, 以下编号原料未能绑定菜品信息:" + midList.toString(), ErrorLevel.WARNING);
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
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<FoodMaterial> getList(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		List<FoodMaterial> list = new ArrayList<FoodMaterial>();
		FoodMaterial item = null;
		
		String querySQL = "SELECT FM.restaurant_id, FM.food_id, FM.material_id, FM.consumption, F.name food_name, M.name material_name, MC.name material_cate_name"
						+ " FROM "
						+ " food_material FM JOIN food F ON FM.food_id = F.food_id"
						+ " JOIN material M ON FM.material_id = M.material_id"
						+ " JOIN material_cate MC ON M.cate_id = MC.cate_id"
						+ " WHERE 1=1";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new FoodMaterial(dbCon.rs.getInt("restaurant_id"), 
				dbCon.rs.getInt("food_id"), 
				dbCon.rs.getInt("material_id"), 
				dbCon.rs.getFloat("consumption"), 
				dbCon.rs.getString("food_name"), 
				dbCon.rs.getString("material_name"), 
				dbCon.rs.getString("material_cate_name")
			);
			
			list.add(item);
			item = null;
		}
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<FoodMaterial> getList(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return FoodMaterialDao.getList(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
}
