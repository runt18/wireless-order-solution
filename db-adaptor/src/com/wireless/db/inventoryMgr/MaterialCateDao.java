package com.wireless.db.inventoryMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MaterialError;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.SQLUtil;

public class MaterialCateDao {
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<MaterialCate> getContent(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		List<MaterialCate> list = new ArrayList<MaterialCate>();
		MaterialCate item = null;
		String querySQL = "SELECT MC.cate_id, MC.restaurant_id, MC.name, MC.type"
						+ " FROM wireless_order_db.material_cate MC"
						+ " WHERE 1=1";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new MaterialCate(dbCon.rs.getInt("cate_id"));
			item.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			item.setName(dbCon.rs.getString("name"));
			item.setType(MaterialCate.Type.valueOf(dbCon.rs.getInt("type")));
			
			list.add(item);
			item = null;
		}
		dbCon.rs.close();
		dbCon.rs = null;
		
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<MaterialCate> getContent(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MaterialCateDao.getContent(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public static MaterialCate getById(DBCon dbCon, int id) throws SQLException{
		List<MaterialCate> list = null;
		MaterialCate item = null;
		Map<Object, Object> params = new LinkedHashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MC.cate_id = " + id);
		list = MaterialCateDao.getContent(dbCon, params);
		if(list != null && !list.isEmpty()){
			item = list.get(0);
		}
		return item;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public static MaterialCate getById(int id) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MaterialCateDao.getById(dbCon, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param mc
	 * @return
	 * @throws SQLException
	 */
	public static int insert(DBCon dbCon, MaterialCate mc) throws SQLException{
		int count = 0;
		String insertSQL = "INSERT material_cate (restaurant_id, name, type)"
						 + " VALUES(" + mc.getRestaurantId() + ", '" + mc.getName() + "', " + mc.getType().getValue() + ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		return count;
	}
	
	/**
	 * 
	 * @param mc
	 * @return
	 * @throws SQLException
	 */
	public static void insert(MaterialCate mc) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			int count = MaterialCateDao.insert(dbCon, mc);
			if(count == 0){
				throw new BusinessException(MaterialError.CATE_INSERT_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param mc
	 * @return
	 * @throws SQLException
	 */
	public static int update(DBCon dbCon, MaterialCate mc) throws SQLException{
		int count = 0;
		String updateSQL = "UPDATE material_cate SET "
						 + " name = '" + mc.getName() + "'"
						 + " ,type = " + mc.getType().getValue()
						 + " WHERE cate_id = " + mc.getId();
		count = dbCon.stmt.executeUpdate(updateSQL);
		return count;
	}
	
	/**
	 * 
	 * @param mc
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void update(MaterialCate mc) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			int count = MaterialCateDao.update(dbCon, mc);
			if(count == 0){
				throw new BusinessException(MaterialError.CATE_UPDATE_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param id
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int delete(DBCon dbCon, Staff staff, int id) throws BusinessException, SQLException {
		int count = 0;
		String querySQL = "SELECT COUNT(*) FROM material WHERE cate_id = " + id;
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt(1) > 0){

			
			String selectFoodMaterial = "DELETE FM FROM food_material FM JOIN material M ON M.material_id = FM.material_id JOIN material_cate MC ON MC.cate_id = M.cate_id "+
										" WHERE FM.restaurant_id = " + staff.getRestaurantId() +" AND MC.cate_id = " + id;
			
			dbCon.stmt.executeUpdate(selectFoodMaterial);
			
			
			//throw new BusinessException(MaterialError.CATE_DELETE_FAIL_HAS_CHILD);
			String deleteAll = "DELETE FROM material WHERE cate_id = " + id;
			dbCon.stmt.executeUpdate(deleteAll);
			
		}
		String deleteSQL = "DELETE FROM material_cate WHERE cate_id = " + id;
		count = dbCon.stmt.executeUpdate(deleteSQL);
		return count;
	}
	
	/**
	 * 
	 * @param id
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void delete(Staff staff, int id) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			int count = MaterialCateDao.delete(dbCon, staff, id);
			if(count == 0){
				throw new BusinessException(MaterialError.CATE_DELETE_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
	}
}
