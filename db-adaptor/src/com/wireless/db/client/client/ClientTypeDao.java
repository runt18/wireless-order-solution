package com.wireless.db.client.client;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ClientError;
import com.wireless.pojo.client.ClientType;
import com.wireless.util.SQLUtil;

public class ClientTypeDao {
	
	/**
	 * 
	 * @param dbCon
	 * @param ct
	 * @return
	 * @throws SQLException
	 */
	public static int insertClientType(DBCon dbCon, ClientType ct) throws SQLException{
		int count = 0;
		String insertSQL = "INSERT INTO " + Params.dbName + ".client_type (name, parent_id, restaurant_id)" 
				+ " values('" + ct.getName() + "'," + ct.getParentID() + "," + ct.getRestaurantID() + ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		return count;
	}
	
	/**
	 * 
	 * @param ct
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int insertClientType(ClientType ct) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = ClientTypeDao.insertClientType(dbCon, ct);
			if(count == 0){
				throw new BusinessException(ClientError.TYPE_INSERT_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param ct
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int deleteClientType(DBCon dbCon, ClientType ct) throws SQLException, BusinessException{
		int count = 0;
		String querySQL = "";
		// 审查该类型是否大类,是则不允许删除
		querySQL = "SELECT count(*) count FROM " + Params.dbName + ".client_type WHERE parent_id = " + ct.getTypeID();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
			throw new BusinessException(ClientError.TYPE_UPDATE_FAIL_IS_PARENT);
		}
		
		// 审查该类型下是否已有客户,有则不允许删除
		querySQL = "SELECT count(*) count FROM " + Params.dbName + ".client WHERE client_type_id = " + ct.getTypeID();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
			throw new BusinessException(ClientError.TYPE_UPDATE_FAIL_HAS_CLIENT);
		}
		
		String updateSQL = "DELETE FROM " + Params.dbName + ".client_type WHERE client_type_id = " + ct.getTypeID();
		count = dbCon.stmt.executeUpdate(updateSQL);
		if(count == 0){
			throw new BusinessException(ClientError.TYPE_DELETE_FAIL);
		}
		return count;
	}
	
	/**
	 * 
	 * @param ct
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int deleteClientType(ClientType ct) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = ClientTypeDao.deleteClientType(dbCon, ct);
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param ct
	 * @return
	 * @throws SQLException
	 */
	public static int updateClientType(DBCon dbCon, ClientType ct) throws SQLException{
		int count = 0;
		String updateSQL = "UPDATE " + Params.dbName + ".client_type SET name = '" + ct.getName() + "', parent_id = " + ct.getParentID() + "  WHERE client_type_id = " + ct.getTypeID();
		count = dbCon.stmt.executeUpdate(updateSQL) ;
		return count;
	}
	
	/**
	 * 
	 * @param ct
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int updateClientType(ClientType ct) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = ClientTypeDao.updateClientType(dbCon, ct);
			if(count == 0){
				throw new BusinessException(ClientError.TYPE_UPDATE_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<ClientType> getClientType(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		List<ClientType> list = new ArrayList<ClientType>();
		ClientType item = null;
		String querySQL = "SELECT client_type_id, name, parent_id, restaurant_id "
				+ " FROM " + Params.dbName + ".client_type WHERE 1 = 1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new ClientType();
			
			item.setTypeID(dbCon.rs.getInt("client_type_id"));
			item.setName(dbCon.rs.getString("name"));
			item.setParentID(dbCon.rs.getInt("parent_id"));
			item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			
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
	public static List<ClientType> getClientType(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return ClientTypeDao.getClientType(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
}
