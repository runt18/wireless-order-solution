package com.wireless.db.inventoryMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorLevel;
import com.wireless.exception.FoodError;
import com.wireless.exception.MaterialError;
import com.wireless.pojo.inventoryMgr.FoodMaterial;
import com.wireless.pojo.inventoryMgr.MaterialCate;
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
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void insertList(DBCon dbCon, List<FoodMaterial> list) throws NullPointerException, BusinessException, SQLException{
		if(list == null || list.isEmpty()){
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
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static int delete(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		int count = 0;
		String deleteSQL = "DELETE FROM food_material FM WHERE 1=1 ";
		deleteSQL  = SQLUtil.bindSQLParams(deleteSQL, params);
		count = dbCon.stmt.executeUpdate(deleteSQL);
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public static int delete(DBCon dbCon, int id) throws SQLException{
		Map<Object, Object> params = new LinkedHashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND FM.id = " + id);
		return FoodMaterialDao.delete(dbCon, params);
	}
	
	/**
	 * 
	 * @param id
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void delete(int id) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			FoodMaterialDao.delete(dbCon, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param foodId
	 * @param deleteGood
	 * @return
	 * @throws SQLException
	 */
	public static int deleteAll(DBCon dbCon, int foodId, boolean deleteGood) throws SQLException{
		int count = 0;
		String deleteSQL = "DELETE FROM food_material WHERE food_id = " + foodId;
		if(!deleteGood){
			String querySQL = "";
			int goodId = 0;
			querySQL = "SELECT T1.material_id FROM "
					 + "food_material T1 JOIN material T2 ON T1.material_id = T2.material_id "
					 + "JOIN material_cate T3 ON T2.cate_id = T3.cate_id AND T3.type = " + MaterialCate.Type.GOOD.getValue() 
					 + " WHERE T1.food_id = " + foodId;
			dbCon.rs = dbCon.stmt.executeQuery(querySQL);
			if(dbCon.rs != null && dbCon.rs.next()){
				goodId = dbCon.rs.getInt(1);
			}
			if(goodId > 0){
				deleteSQL += (" AND material_id <> " + goodId);
			}
		}
		count = dbCon.stmt.executeUpdate(deleteSQL);
		return count;
	}
	
	/**
	 * 
	 * @param foodId
	 * @param deleteGood
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void deleteAll(int foodId, boolean deleteGood) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			FoodMaterialDao.deleteAll(dbCon, foodId, deleteGood);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param foodId
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void deleteAll(int foodId) throws BusinessException, SQLException{
		FoodMaterialDao.deleteAll(foodId, false);
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param foodId
	 * @param list
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void update(DBCon dbCon, int foodId, List<FoodMaterial> list) throws BusinessException, SQLException{
		if(list == null ){
			throw new NullPointerException("The list is null. ");
		}
		// 删除除商品资料以外的库存资料关系
		try{
			FoodMaterialDao.deleteAll(dbCon, foodId, false);
		}catch(Exception e){
			throw new BusinessException(MaterialError.BINDING_DELETE_FAIL);
		}
		if(!list.isEmpty()){
			// 生成现有记录关系
			FoodMaterialDao.insertList(dbCon, list);
		}
	}
	
	/**
	 * 
	 * @param foodId
	 * @param list
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void update(int foodId, List<FoodMaterial> list) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			FoodMaterialDao.update(dbCon, foodId, list);
		}finally{
			dbCon.disconnect();
		}
	}
	
}
