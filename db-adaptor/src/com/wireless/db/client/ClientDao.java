package com.wireless.db.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.Client;
import com.wireless.pojo.client.ClientType;
import com.wireless.util.SQLUtil;

public class ClientDao {
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<ClientType> getClientType(DBCon dbCon, Map<Object, Object> params) throws Exception{
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
	 * @throws Exception
	 */
	public static List<ClientType> getClientType(Map<Object, Object> params) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return ClientDao.getClientType(dbCon, params);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param ct
	 * @throws Exception
	 */
	public static int insertClientType(ClientType ct) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			String insertSQL = "INSERT INTO " + Params.dbName + ".client_type (name, parent_id, restaurant_id)" 
							+ " values('" + ct.getName() + "'," + ct.getParentID() + "," + ct.getRestaurantID() + ")";
			count = dbCon.stmt.executeUpdate(insertSQL);
			if(count == 0){
				throw new BusinessException("操作失败, 插入新客户类型失败,未知错误.", 9990);
			}
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param ct
	 * @throws Exception
	 */
	public static int updateClientType(DBCon dbCon, ClientType ct) throws Exception{
		int count = 0;
		String updateSQL = "UPDATE " + Params.dbName + ".client_type SET name = '" + ct.getName() + "', parent_id = " + ct.getParentID() + "  WHERE client_type_id = " + ct.getTypeID();
		count = dbCon.stmt.executeUpdate(updateSQL) ;
		return count;
	}
	
	/**
	 * 
	 * @param ct
	 * @return
	 * @throws Exception
	 */
	public static int updateClientType(ClientType ct) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = ClientDao.updateClientType(dbCon, ct);
			if(count == 0){
				throw new BusinessException("操作失败, 未找到要修改的记录.", 9995);
			}
		}catch(Exception e){
			throw e;
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
	 * @throws Exception
	 */
	public static int deleteClientType(DBCon dbCon, ClientType ct) throws Exception{
		int count = 0;
		String querySQL = "";
		// 审查该类型是否大类,是则不允许删除
		querySQL = "SELECT count(*) count FROM " + Params.dbName + ".client_type WHERE parent_id = " + ct.getTypeID();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
			throw new BusinessException("操作失败, 该类型下还包含其他子类型.", 9994);
		}
		
		// 审查该类型下是否已有客户,有则不允许删除
		querySQL = "SELECT count(*) count FROM " + Params.dbName + ".client WHERE client_type_id = " + ct.getTypeID();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
			throw new BusinessException("操作失败, 该类型下还有客户.", 9993);
		}
		
		String updateSQL = "DELETE FROM " + Params.dbName + ".client_type WHERE client_type_id = " + ct.getTypeID();
		count = dbCon.stmt.executeUpdate(updateSQL);
		if(count == 0){
			throw new BusinessException("操作失败, 未找到要删除的记录.", 9992);
		}
		return count;
	}
	
	/**
	 * 
	 * @param ct
	 * @return
	 * @throws Exception
	 */
	public static int deleteClientType(ClientType ct) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = ClientDao.deleteClientType(dbCon, ct);
		}catch(Exception e){
			throw e;
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
	 * @throws Exception
	 */
	public static List<Client> getClient(DBCon dbCon, Map<Object, Object> params) throws Exception{
		List<Client> list = new ArrayList<Client>();
		Client item = null;
		String querySQL = "SELECT A.client_id, A.client_type_id, A.restaurant_id, A.name AS client_name, A.sex, A.birth_date, A.level, "
				+ " A.tele, A.mobile, A.birthday, A.id_card, A.company, A.taste_pref, A.taboo, A.contact_addr, A.comment,"
				+ " B.name AS client_type_name, B.parent_id,"
				+ " (SELECT count(*) FROM " + Params.dbName + ".client_member TT WHERE TT.client_id = A.client_id) AS member_account"
				+ "	FROM " + Params.dbName + ".client A, " + Params.dbName + ".client_type B "
				+ " WHERE A.restaurant_id = B.restaurant_id AND A.client_type_id = B.client_type_id ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new Client();
			item.setClientID(dbCon.rs.getInt("client_id"));
			item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			item.setName(dbCon.rs.getString("client_name"));
			item.setSex(dbCon.rs.getInt("sex"));
			item.setTele(dbCon.rs.getString("tele"));
			item.setMobile(dbCon.rs.getString("mobile"));
			item.setBirthday(dbCon.rs.getTimestamp("birthday") != null ? dbCon.rs.getTimestamp("birthday").getTime() : 0);
			item.setIDCard(dbCon.rs.getString("id_card"));
			item.setCompany(dbCon.rs.getString("company"));
			item.setTastePref(dbCon.rs.getString("taste_pref"));
			item.setTaboo(dbCon.rs.getString("taboo"));
			item.setContactAddress(dbCon.rs.getString("contact_addr"));
			item.setComment(dbCon.rs.getString("comment"));
			item.setMemberAccount(dbCon.rs.getInt("member_account"));
			item.setBirthDate(dbCon.rs.getString("birth_date"));
			item.setLevel(dbCon.rs.getInt("level"));
			item.setClientTypeID(dbCon.rs.getInt("client_type_id"));
			item.setClientType(
				dbCon.rs.getInt("client_type_id"), 
				dbCon.rs.getString("client_type_name"), 
				dbCon.rs.getInt("parent_id"), 
				dbCon.rs.getInt("restaurant_id")
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
	 * @throws Exception
	 */
	public static List<Client> getClient(Map<Object, Object> params) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return ClientDao.getClient(dbCon, params);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param c
	 * @return
	 */
	public static int insertClient(DBCon dbCon, Client c) throws Exception{
		int count = 0;
		String insertSQL = "INSERT INTO " + Params.dbName + ".client "
				+ " (restaurant_id, client_type_id, name, sex, tele, mobile, birthday, id_card, company, taste_pref, taboo, contact_addr, comment, birth_date)"
				+ " values(" 
				+ c.getRestaurantID() + "," + c.getClientType().getTypeID() + ",'" + c.getName() + "'," + c.getSex() + ","
				+ " '" + c.getTele() + "','" + c.getMobile() + "'," + (c.getBirthdayFormat() != null ? "'" + c.getBirthdayFormat() + "'" : null) + ",'" + c.getIDCard() + "',"
				+ " '" + c.getCompany()+ "','" + c.getTastePref() + "','" + c.getTaboo() + "','" + c.getContactAddress() + "','" + c.getComment()+ "', NOW()"
				+ ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		return count;
	}
	
	/**
	 * 
	 * @param c
	 * @throws Exception
	 */
	public static int insertClient(Client c) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = insertClient(dbCon, c);
			if(count == 0){
				throw new BusinessException("操作失败, 未添加新客户信息,数据操作异常.", 9989);
			}
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param c
	 * @throws Exception
	 */
	public static int updateClient(DBCon dbCon, Client c) throws Exception{
		int count = 0;
		String updateSQL = "UPDATE " +  Params.dbName + ".client SET "
				+ " client_type_id = " + c.getClientType().getTypeID() + ", name = '" + c.getName() + "', sex = " + c.getSex() + ","
				+ " tele = '" + c.getTele() + "', mobile = '" + c.getMobile()+ "', birthday = " + (c.getBirthdayFormat() != null ? "'" + c.getBirthdayFormat() + "'" : null) + ", "
				+ " id_card = '" + c.getIDCard() + "', company = '" + c.getCompany()+ "', taste_pref = '" + c.getTastePref() + "', "
				+ " taboo = '" + c.getTaboo() + "', contact_addr = '" + c.getContactAddress() + "', comment = '" + c.getComment() + "'"
				+ " WHERE restaurant_id = " + c.getRestaurantID() + " AND client_id = " + c.getClientID();
		count = dbCon.stmt.executeUpdate(updateSQL);;
		return count;
	}
	
	/**
	 * 
	 * @param c
	 * @throws Exception
	 */
	public static int updateClient(Client c) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = updateClient(dbCon, c);
			if(count == 0){
				throw new BusinessException("操作失败, 未找到要修改的原记录.", 9988);
			}
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param c
	 * @return
	 * @throws Exception
	 */
	public static int deleteClient(DBCon dbCon, Client c) throws Exception{
		int count = 0;
		List<Integer> member = new ArrayList<Integer>();
		String memberID = "";
		
		// 处理已关联会员账号
		String querySQL = "SELECT member_id FROM " + Params.dbName + ".client_member "
						+ " WHERE restaurant_id = " + c.getRestaurantID() + " AND client_id = " + c.getClientID();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			member.add(dbCon.rs.getInt("member_id"));
		}
		if(member.size() > 0){
			for(int i = 0; i < member.size(); i++){
				memberID += (i > 0 ? "," : "");
				memberID += member.get(i);
			}
			String updateSQL = "UPDATE " +  Params.dbName + ".member SET status = 1, last_mod_date = NOW() WHERE restaurant_id = " + c.getRestaurantID() + " AND member_id in (" + memberID + ")";
			if(dbCon.stmt.executeUpdate(updateSQL) != member.size()){
				throw new BusinessException("操作失败, 该客户已关联的会员账号信息处理失败.", 9987);
			}
			String deleteSQL = "DELETE FROM " +  Params.dbName + ".client_member WHERE restaurant_id = " + c.getRestaurantID() + " AND client_id = " + c.getClientID();
			count = dbCon.stmt.executeUpdate(deleteSQL);
			if(count != member.size()){
				throw new BusinessException("操作失败, 该客户已关联的会员账号处理失败.", 9986);
			}
		}
		
		String deleteSQL = "DELETE FROM " + Params.dbName + ".client WHERE restaurant_id = " + c.getRestaurantID() + " AND client_id = " + c.getClientID();
		if(dbCon.stmt.executeUpdate(deleteSQL) == 0){
			throw new BusinessException("操作失败, 未找到要删除的原纪录.", 9985);
		}
		return count;
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 * @throws Exception
	 */
	public static int deleteClient(Client c) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = ClientDao.deleteClient(dbCon, c);
			dbCon.conn.commit();
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
}
