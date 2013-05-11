package com.wireless.db.inventoryMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MaterialError;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.util.SQLUtil;

public class MaterialDao {
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<Material> getContent(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		List<Material> list = new ArrayList<Material>();
		Material item = null;
		String querySQL = "SELECT M.material_id, M.restaurant_id, M.price, M.stock, M.name, M.status, M.last_mod_staff, M.last_mod_date,"
						+ " MC.cate_id, MC.name cate_name"
						+ " FROM Material_cate MC, Material M "
						+ " WHERE MC.restaurant_id = M.restaurant_id "
						+ " AND MC.cate_id = M.cate_id";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new Material();
			item.setId(dbCon.rs.getInt("material_id"));
			item.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			item.setPrice(dbCon.rs.getFloat("price"));
			item.setStock(dbCon.rs.getFloat("stock"));
			item.setName(dbCon.rs.getString("name"));
			item.setLastModDate(dbCon.rs.getTimestamp("last_mod_date").getTime());
			item.setLastModStaff(dbCon.rs.getString("last_mod_staff"));
			item.setStatus(dbCon.rs.getInt("status"));
			item.setCate(dbCon.rs.getInt("cate_id"), dbCon.rs.getString("cate_name"));
			
			list.add(item);
		}
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<Material> getContent(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MaterialDao.getContent(dbCon, params);
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
	public static Material getById(DBCon dbCon, int id) throws SQLException{
		List<Material> list = null;
		Material item = null;
		Map<Object, Object> params = new LinkedHashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.material_id = " + id);
		list = MaterialDao.getContent(dbCon, params);
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
	public static Material getById(int id) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MaterialDao.getById(dbCon, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param m
	 * @return
	 * @throws SQLException
	 */
	public static int insert(DBCon dbCon, Material m) throws SQLException{
		int count = 0;
		String insertSQL = "INSERT INTO material"
						 + " (restaurant_id, cate_id, price, stock, status, name, last_mod_staff, last_mod_date)"
						 + " values("
						 + m.getRestaurantId() + ","
						 + m.getCate().getId() + ","
						 + "0," + "0,"
						 + m.getStatus().getValue() + ","
						 + "'" + m.getName() + "'" + ","
						 + "'" + m.getLastModStaff() + "'" + ","
						 + "NOW()"
						 + ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		return count;
	}
	
	/**
	 * 
	 * @param m
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void insert(Material m) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			int count = MaterialDao.insert(dbCon, m);
			if(count == 0){
				throw new BusinessException(MaterialError.INSERT_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param m
	 * @return
	 * @throws SQLException
	 */
	public static int update(DBCon dbCon, Material m) throws SQLException{
		int count = 0;
		String updateSQL = "UPDATE material SET "
						 + " cate_id = " + m.getCate().getId()
						 + " ,name = '" + m.getName() + "'"
						 + " ,last_mod_staff = '" + m.getLastModStaff() + "'"
						 + " ,last_mod_date = NOW()"
						 + " WHERE material_id = " + m.getId()
						 + " AND restaurant_id = " + m.getRestaurantId();
		count = dbCon.stmt.executeUpdate(updateSQL);
		return count;
	}
	
	/**
	 * 
	 * @param m
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void update(Material m) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			int count = MaterialDao.update(dbCon, m);
			if(count == 0){
				throw new BusinessException(MaterialError.UPDATE_FAIL);
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
	 * @throws SQLException
	 */
	public static int delete(DBCon dbCon, int id) throws SQLException{
		int count = 0;
		//TODO 未检查历史记录
		
		String deleteSQL = "DELETE FROM material "
						 + " WHERE material_id = " + id;
		count = dbCon.stmt.executeUpdate(deleteSQL);
		return count;
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
			int count = MaterialDao.delete(dbCon, id);
			if(count == 0){
				throw new BusinessException(MaterialError.DELETE_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
	}
}





















