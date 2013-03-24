package com.wireless.db.client.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.client.client.ClientDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.Client;
import com.wireless.pojo.client.ClientType;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberCard;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.system.Staff;
import com.wireless.util.SQLUtil;

public class MemberDao {
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static int getMemberCount(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		int count = 0;
		String querySQL = "SELECT COUNT(A.member_id) FROM " + Params.dbName + ".member A WHERE 1=1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next()){
			count = dbCon.rs.getInt(1);
		}
		return count;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static int getMemberCount(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = MemberDao.getMemberCount(dbCon, params);
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
	public static List<Member> getMember(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		List<Member> list = new ArrayList<Member>();
		Member item = null;
		MemberCard memberCard = null;
		MemberType memberType = null;
		Staff staff = null;
		Client client = null;
			
		String querySQL = "SELECT "
				+ " A.member_id, A.restaurant_id, A.base_balance, A.extra_balance, A.point, A.birth_date, A.last_mod_date, "
				+ " A.comment member_comment, A.status member_status, A.last_staff_id, (A.base_balance + A.extra_balance) totalBalance,"
				+ " B.member_type_id, B.name member_type_name, B.exchange_rate, B.charge_rate, B.discount_id member_type_disocunt_id, "
				+ " C.member_card_id, C.member_card_alias, C.status member_card_status, C.comment member_card_comment,"
				+ " C.last_staff_id member_card_last_staff_id, C.last_mod_date member_card_last_mod_date,"
				+ " E.client_id, E.name client_name, E.client_type_id, E.sex, E.birth_date, E.level, E.tele, E.mobile, "
				+ " E.birthday, E.id_card, E.company, E.taste_pref, E.taboo, E.contact_addr, E.comment client_comment,"
				+ " CT.name client_type_name, CT.parent_id, "
				+ " F.staff_id, F.staff_name"
				+ " FROM "
				+ Params.dbName + ".member A"
				+ " LEFT JOIN "
				+ Params.dbName + ".member_type B ON A.restaurant_id = B.restaurant_id AND A.member_type_id = B.member_type_id"
				+ " LEFT JOIN "
				+ Params.dbName + ".member_card C ON A.restaurant_id = C.restaurant_id AND A.member_card_id = C.member_card_id "
				+ " LEFT JOIN "
				+ Params.dbName + ".client_member D ON A.restaurant_id = D.restaurant_id AND A.member_id = D.member_id"
				+ " LEFT JOIN "
				+ Params.dbName + ".client E ON E.restaurant_id = D.restaurant_id AND E.client_id = D.client_id"
				+ " LEFT JOIN "
				+ Params.dbName + ".client_type CT ON E.restaurant_id = CT.restaurant_id AND E.client_type_id = CT.client_type_id"
				+ " JOIN "
				+ "  (SELECT staff_id, name staff_name, restaurant_id FROM " + Params.dbName + ".staff"
				+ "  UNION ALL "
				+ "  SELECT terminal_id staff_id, owner_name staff_name, restaurant_id FROM " + Params.dbName + ".terminal) F "
				+ " ON A.restaurant_id = F.restaurant_id AND A.last_staff_id = F.staff_id"
				+ " WHERE 1=1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new Member();
			staff = item.getStaff();
			memberCard = item.getMemberCard();
			memberType = item.getMemberType();
			client = item.getClient();
			
			item.setId(dbCon.rs.getInt("member_id"));
			item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			item.setPoint(dbCon.rs.getInt("point"));
			item.setBaseBalance(dbCon.rs.getFloat("base_balance"));
			item.setExtraBalance(dbCon.rs.getFloat("extra_balance"));
			item.setBirthDate(dbCon.rs.getTimestamp("birth_date").getTime());
			item.setLastModDate(dbCon.rs.getTimestamp("last_mod_date").getTime());
			item.setComment(dbCon.rs.getString("member_comment"));
			item.setStatus(dbCon.rs.getInt("member_status"));
			item.setMemberTypeID(dbCon.rs.getInt("member_type_id"));
			item.setMemberCardID(dbCon.rs.getInt("member_card_id"));
			item.setClientID(dbCon.rs.getInt("client_id"));
			
			staff.setId(dbCon.rs.getInt("staff_id"));
			staff.setName(dbCon.rs.getString("staff_name"));
			
			memberCard.setId(dbCon.rs.getInt("member_card_id"));
			memberCard.setAliasID(dbCon.rs.getString("member_card_alias"));
			
			memberType.setTypeID(dbCon.rs.getInt("member_type_id"));
			memberType.setName(dbCon.rs.getString("member_type_name"));
			memberType.setExchangeRate(dbCon.rs.getFloat("exchange_rate"));
			memberType.setChargeRate(dbCon.rs.getFloat("charge_rate"));
			memberType.getDiscount().setId(dbCon.rs.getInt("member_type_disocunt_id"));
			
			client.setClientID(dbCon.rs.getInt("client_id"));
			client.setName(dbCon.rs.getString("client_name"));
			client.setSex(dbCon.rs.getInt("sex"));
			client.setTele(dbCon.rs.getString("tele"));
			client.setMobile(dbCon.rs.getString("mobile"));
			client.setBirthday(dbCon.rs.getTimestamp("birthday") != null ? dbCon.rs.getTimestamp("birthday").getTime() : 0);
			client.setIDCard(dbCon.rs.getString("id_card"));
			client.setCompany(dbCon.rs.getString("company"));
			client.setTastePref(dbCon.rs.getString("taste_pref"));
			client.setTaboo(dbCon.rs.getString("taboo"));
			client.setContactAddress(dbCon.rs.getString("contact_addr"));
			client.setComment(dbCon.rs.getString("client_comment"));
			client.setBirthDate(dbCon.rs.getString("birth_date"));
			client.setLevel(dbCon.rs.getInt("level"));
			client.setClientTypeID(dbCon.rs.getInt("client_type_id"));
			
			client.setClientType(new ClientType(
					dbCon.rs.getInt("client_type_id"), 
					dbCon.rs.getString("client_type_name"), 
					dbCon.rs.getInt("parent_id"), 
					dbCon.rs.getInt("restaurant_id")
			));
			
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
	public static List<Member> getMember(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberDao.getMember(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public static Member getMember(int id) throws SQLException{
		List<Member> ml = null;
		Member m = null;
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.member_id = " + id);
		ml = MemberDao.getMember(params);
		if(ml != null && ml.size() > 0){
			m = ml.get(0);
		}
		return m;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public static Member getMember(DBCon dbCon, int id) throws SQLException{
		List<Member> ml = null;
		Member m = null;
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.member_id = " + id);
		ml = MemberDao.getMember(dbCon, params);
		if(ml != null && ml.size() > 0){
			m = ml.get(0);
		}
		return m;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param m
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int insertMember(DBCon dbCon, Member m) throws SQLException, BusinessException{
		int count = 0;
		// 绑定所有基础信息对象的restaurantID
		m.getClient().setRestaurantID(m.getRestaurantID());
		m.getMemberType().setRestaurantID(m.getRestaurantID());
		m.getMemberCard().setRestaurantID(m.getRestaurantID());
		m.getStaff().setRestaurantID(m.getRestaurantID());
		
		String insertSQL = "";
		MemberCard memberCard = m.getMemberCard();
		Staff staff = m.getStaff();
		
		// 查找当前请求操作人信息
		staff = getOperationStaff(dbCon, staff);
		m.setStaffID(staff.getId());
		memberCard.setLastStaffID(staff.getId());
		
		// 会员卡资料处理
		Map<Object, Object> params = new HashMap<Object, Object>();
		String tempExtre = " AND restaurant_id = " + m.getRestaurantID() 
					+ " AND member_card_alias = " + memberCard.getAliasID();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, tempExtre);
		List<MemberCard> mcl = MemberCardDao.getMemberCard(dbCon, params);
		if(mcl == null || mcl.size() == 0){
			// 原会员卡不存在
			// 设置会员卡状态为正在使用
			memberCard.setStatus(MemberCard.STATUS_ACTIVE);
			memberCard.setComment(Member.OPERATION_INSERT + MemberCard.OPERATION_INSERT);
			// 添加会员卡资料并绑定会员
			count = MemberCardDao.insertMemberCard(dbCon, memberCard);
			if(count == 0){
				throw new BusinessException("操作失败, 添加会员卡资料失败, 未知错误.", 9967);
			}
			dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.SQL_QUERY_LAST_INSERT_ID);// 获取新生成会员卡信息数据编号
			if(dbCon.rs != null && dbCon.rs.next()){
				m.setMemberCardID(dbCon.rs.getInt(1));
				dbCon.rs = null;
			}
		}else{
			MemberCard tempMC = mcl.get(0);
			if(tempMC.getStatus() == MemberCard.Status.NORMAL){
				m.setMemberCardID(tempMC.getId());
				params = new HashMap<Object, Object>();
				tempMC.setStatus(MemberCard.STATUS_ACTIVE);
				tempMC.setComment(Member.OPERATION_UPDATE + MemberCard.OPERATION_RESET + MemberCard.OPERATION_ENABLE);
				params.put(MemberCard.class, tempMC);
				params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND member_card_alias = " + memberCard.getAliasID());
				count = MemberCardDao.updateMemberCard(dbCon, params);
				if(count == 0){
					throw new BusinessException("操作失败, 设置会员卡状态失败, 未知错误.", 9968);
				}
			}else if(tempMC.getStatus() == MemberCard.Status.ACTIVE){
				throw new BusinessException("操作失败, 该会员卡已被使用.", 9968);
			}else if(tempMC.getStatus() == MemberCard.Status.DISABLE){
				throw new BusinessException("操作失败, 该会员卡已被禁用.", 9968);
			}else if(tempMC.getStatus() == MemberCard.Status.LOST){
				throw new BusinessException("操作失败, 该会员卡已挂失.", 9968);
			}
		}
		
		// 添加会员资料 *************************
		insertSQL = "INSERT INTO member "
					+ " (restaurant_id, member_type_id, member_card_id, birth_date, last_mod_date, last_staff_id, comment, status) "
					+ " VALUES("
					+ m.getRestaurantID() + ","
					+ m.getMemberTypeID() + ","
					+ m.getMemberCardID() + ","
					+ "NOW(),"
					+ "NOW(),"
					+ m.getStaffID() + ","
					+ "'" + m.getComment() + "',"
					+ m.getStatus()
					+ " )";
		count = dbCon.stmt.executeUpdate(insertSQL);
		if(count == 0){
			throw new BusinessException("操作失败, 添加会员资料失败, 请检查数据格式是否正确.", 9966);
		}else{
			dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.SQL_QUERY_LAST_INSERT_ID);// 获取新生成会员卡信息数据编号
			if(dbCon.rs != null && dbCon.rs.next()){
				m.setId(dbCon.rs.getInt(1));
				dbCon.rs = null;
			}
		}
		
		// 客户信息处理
		bindMemberClient(dbCon, m);
		return count;
	}
	
	/**
	 * 
	 * @param m
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int insertMember(Member m) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = insertMember(dbCon, m);
			dbCon.conn.commit();
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param m
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int updateMember(DBCon dbCon, Member m) throws SQLException, BusinessException{
		int count = 0;
		// 绑定所有基础信息对象的restaurantID
		m.getClient().setRestaurantID(m.getRestaurantID());
		m.getMemberType().setRestaurantID(m.getRestaurantID());
		m.getMemberCard().setRestaurantID(m.getRestaurantID());
		m.getStaff().setRestaurantID(m.getRestaurantID());		
		
		String updateSQL = "";
		MemberCard memberCard = m.getMemberCard();
		Staff staff = m.getStaff();
		
		// 查找当前请求操作人信息
		staff = getOperationStaff(dbCon, staff);
		m.setStaffID(staff.getId());
		memberCard.setLastStaffID(staff.getId());			
		/*
		// 处理会员卡信息
		MemberCard oldCard = null, newCard = null;
		Map<Object, Object> paramsSet = new HashMap<Object, Object>();
		paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.member_card_id = (SELECT member_card_id FROM " + Params.dbName + ".member WHERE member_id = " + m.getId() + ")");
		List<MemberCard> tempCardList = MemberCardDao.getMemberCard(dbCon, paramsSet);
		if(tempCardList == null || tempCardList.size() == 0){
			throw new BusinessException("操作失败, 获取原会员卡信息失败.", 9966);
		}else{
			// 获取原会员卡信息
			oldCard = tempCardList.get(0);
			// 处理修改会员卡信息操作
			if(!oldCard.getAliasID().equals(memberCard.getAliasID())){
				// 处理员会员卡信息(变为可用状态)
				oldCard.setStatus(MemberCard.STATUS_NORMAL);
				oldCard.setComment(MemberCard.OPERATION_RESET + MemberCard.OPERATION_ENABLE);
				paramsSet = new HashMap<Object, Object>();
				paramsSet.put(MemberCard.class, oldCard);
				paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.member_card_id = " + oldCard.getId());
				count = MemberCardDao.updateMemberCard(dbCon, paramsSet);
				if(count == 0){
					throw new BusinessException("操作失败, 修改原会员卡信息失败.", 9968);
				}
				// 处理新会员卡信息
				paramsSet = new HashMap<Object, Object>();
				paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.member_card_alias = " + memberCard.getAliasID());
				tempCardList = MemberCardDao.getMemberCard(dbCon, paramsSet);
				if(tempCardList == null || tempCardList.size() == 0){
					// 新建会员卡信息
					memberCard.setComment(Member.OPERATION_UPDATE + MemberCard.OPERATION_INSERT);
					memberCard.setStatus(MemberCard.STATUS_ACTIVE);
					// 添加新会员卡
					MemberCardDao.insertMemberCard(dbCon, memberCard);
					dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.SQL_QUERY_LAST_INSERT_ID);// 获取新生成会员卡信息数据编号
					if(dbCon.rs != null && dbCon.rs.next()){
						memberCard.setId(dbCon.rs.getInt(1));
						dbCon.rs = null;
					}
				}else{
					// 修改会员卡信息
					newCard = tempCardList.get(0);
					if(newCard.getStatus() == MemberCard.STATUS_NORMAL){
						newCard.setStatus(MemberCard.STATUS_ACTIVE);
						newCard.setComment(MemberCard.OPERATION_RESET + MemberCard.OPERATION_ACTIVE);
						m.setMemberCard(newCard);
						memberCard = m.getMemberCard();
						paramsSet = new HashMap<Object, Object>();
						paramsSet.put(MemberCard.class, memberCard);
						paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.member_card_alias = " + memberCard.getAliasID());
						MemberCardDao.updateMemberCard(dbCon, paramsSet);
					}else if(newCard.getStatus() == MemberCard.STATUS_ACTIVE){
						throw new BusinessException("操作失败, 该会员卡已绑定其他会员.", 9968);
					}else if(newCard.getStatus() == MemberCard.STATUS_DISABLE){
						throw new BusinessException("操作失败, 该会员卡已被禁用.", 9968);
					}else if(newCard.getStatus() == MemberCard.STATUS_LOST){
						throw new BusinessException("操作失败, 该会员卡已挂失.", 9968);
					}else{
						throw new BusinessException("操作失败, 该会员卡未能正常使用, 请重新选择其他会员卡.", 9968);
					}
				}
			}else{
				m.setMemberCard(oldCard);
				memberCard = m.getMemberCard();
			}
		}
		*/
		// 客户信息处理
		count = bindMemberClient(dbCon, m);
		
		updateSQL = "UPDATE " + Params.dbName + ".member SET"
//				  + " member_type_id = " + m.getMemberTypeID() + ", member_card_id = " + memberCard.getId() + ", status = " + m.getStatus() + "," 
				  + " member_type_id = " + m.getMemberTypeID() + ", status = " + m.getStatus() + "," 
				  + " last_mod_date = NOW(), last_staff_id = " + staff.getId() + ", comment = '" + m.getComment() + "'"
				  + " WHERE member_id = " + m.getId();
		count = dbCon.stmt.executeUpdate(updateSQL);
		if(count == 0){
			throw new BusinessException("操作失败, 修改会员信息失败, 未知错误.", 9967);
		}
		return count;
	}
	
	/**
	 * 
	 * @param m
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int updateMember(Member m) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = updateMember(dbCon, m);
			dbCon.conn.commit();
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param m
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int updateMemberBalance(DBCon dbCon, Member m) throws SQLException, BusinessException{
		int count = 0;
		String updateSQL = "UPDATE " + Params.dbName + ".member SET"
				  + " base_balance = " + m.getBaseBalance() + ", extra_balance = " + m.getExtraBalance() + "," 
				  + " last_mod_date = NOW(), last_staff_id = " + m.getStaff().getId() + ", comment = '" + m.getComment() + "'"
				  + " WHERE member_id = " + m.getId();
		count = dbCon.stmt.executeUpdate(updateSQL);
		return count;
	}
	
	/**
	 * 
	 * @param m
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int updateMemberBalance(Member m) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = MemberDao.updateMemberBalance(dbCon, m);
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param m
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int updateMemberPoint(DBCon dbCon, Member m) throws SQLException, BusinessException{
		int count = 0;
		String updateSQL = "UPDATE " + Params.dbName + ".member SET"
				  + " point = " + m.getPoint() + "," 
				  + " last_mod_date = NOW(), last_staff_id = " + m.getStaff().getId() + ", comment = '" + m.getComment() + "'"
				  + " WHERE member_id = " + m.getId();
		count = dbCon.stmt.executeUpdate(updateSQL);
		return count;
	}
	
	/**
	 * 
	 * @param m
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int updateMemberPoint(Member m) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = MemberDao.updateMemberPoint(dbCon, m);
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param m
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int bindMemberClient(DBCon dbCon, Member m) throws SQLException, BusinessException{
		int count = 0;
		String querySQL = "", insertSQL = "", deleteSQL = "";
		Client client = m.getClient();
		// 删除原有关系
		deleteSQL = "DELETE FROM " + Params.dbName + ".client_member WHERE restaurant_id = " + m.getRestaurantID() + " AND member_id = " + m.getId();
		dbCon.stmt.executeUpdate(deleteSQL);
		// 客户信息处理
		if(client.getLevel() == Client.LEVEL_RESERVED){
			/**
			 * 不记名会员信息处理, 获取匿名用户信息
			 */
			querySQL = "SELECT client_id FROM " + Params.dbName + ".client WHERE restaurant_id = " + m.getRestaurantID() + " AND level = " + Client.LEVEL_RESERVED;
			count = 0;
			dbCon.rs = dbCon.stmt.executeQuery(querySQL);
			if(dbCon.rs != null && dbCon.rs.next()){
				client.setClientID(dbCon.rs.getInt(1));
				dbCon.rs = null;
				querySQL = "";
			}else{
				throw new BusinessException("操作失败, 匿名用户资料获取失败.", 9972);
			}
		}else if(client.getLevel() == Client.LEVEL_NORMAL){
			/**
			 * 普通会员信息处理
			 */
			if(client.getClientID() == 0){
				// 新建客户信息
				count = 0;
				count = ClientDao.insertClient(dbCon, client);
				if(count == 0){
					throw new BusinessException("操作失败, 客户资料新建失败, 未知错误.", 9971);
				}
				dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.SQL_QUERY_LAST_INSERT_ID);// 获取客户信息编号
				if(dbCon.rs != null && dbCon.rs.next()){
					client.setClientID(dbCon.rs.getInt(1));
					dbCon.rs = null;
					querySQL = "";
				}
			}else{
				// 绑定已有客户信息
				// 验证选择绑定客户信息是否存在, 如果不存在则新建客户信息
				querySQL = "SELECT COUNT(client_id) FROM " + Params.dbName + ".client WHERE client_id = " + client.getClientID();
				count = 0;
				dbCon.rs = dbCon.stmt.executeQuery(querySQL);
				if(dbCon.rs != null && dbCon.rs.next()){
					count = dbCon.rs.getInt(1);
					dbCon.rs = null;
					querySQL = "";
				}
				if(count == 0){
					// 新建客户信息
					count = ClientDao.insertClient(dbCon, client);
					if(count == 0){
						throw new BusinessException("操作失败, 客户资料新建失败, 未知错误.", 9970);
					}
					dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.SQL_QUERY_LAST_INSERT_ID);// 获取客户信息编号
					if(dbCon.rs != null && dbCon.rs.next()){
						client.setClientID(dbCon.rs.getInt(1));
						dbCon.rs = null;
						querySQL = "";
					}
				}
			}
		}
		// 绑定客户资料与会员资料关系
		insertSQL = "INSERT INTO " + Params.dbName + ".client_member (client_id, member_id, restaurant_id) "
				+ " VALUES("
				+ client.getClientID() + ","
				+ m.getId() + ","
				+ client.getRestaurantID()
				+ " )";
		count = dbCon.stmt.executeUpdate(insertSQL);
		if(count == 0){
			throw new BusinessException("操作失败, 客户资料绑定失败, 未知错误.", 9969);
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static Staff getOperationStaff(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		String querySQL = "SELECT terminal_id FROM " + Params.dbName + ".terminal "
				 + " WHERE restaurant_id = " + staff.getRestaurantID() + " AND pin = " + staff.getPin();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next()){
			staff.setId(dbCon.rs.getInt(1));
			dbCon.rs.getInt(1);
			dbCon.rs = null;
			querySQL = "";
		}else{
			throw new BusinessException("操作失败, 当前操作人员信息获取失败.", 9973);
		}	
		return staff;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param m
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int deleteMember(DBCon dbCon, Member m) throws SQLException, BusinessException {
		int count = 0;
		String deleteSQL = "";
		// 绑定所有基础信息对象的restaurantID
		m.getClient().setRestaurantID(m.getRestaurantID());
		m.getMemberCard().setRestaurantID(m.getRestaurantID());
		m.getStaff().setRestaurantID(m.getRestaurantID());
		Client client = m.getClient();
		Staff staff = m.getStaff();
		
		staff = getOperationStaff(dbCon, staff);
		
		// 删除与客户资料的绑定关系
		deleteSQL = "DELETE FROM " + Params.dbName + ".client_member WHERE restaurant_id = " + m.getRestaurantID() + " AND member_id = " + m.getId() + " AND client_id = " + client.getClientID();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		if(count == 0){
			throw new BusinessException("操作失败, 与客户资料的绑定关系删除失败.", 9963);
		}
		
		// 设置绑定的会员卡状态为活动状态(可提供其他会员使用)
		Map<Object, Object> paramsSet = new HashMap<Object, Object>();
		MemberCard umc = new MemberCard();
		umc.setStatus(MemberCard.STATUS_NORMAL);
		umc.setRestaurantID(m.getRestaurantID());
		umc.setComment(Member.OPERATION_DELETE + MemberCard.OPERATION_RESET + MemberCard.OPERATION_ENABLE);
		umc.setLastStaffID(staff.getId());
		paramsSet.put(MemberCard.class, umc);
		paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, " AND member_card_id = (SELECT member_card_id FROM " + Params.dbName + ".member WHERE member_id = " + m.getId() + ")");
		count = MemberCardDao.updateMemberCard(paramsSet);
		if(count == 0){
			throw new BusinessException("操作失败, 绑定的会员卡状态设置失败.", 9963);
		}
		
		// 删除会员资料
		deleteSQL = "DELETE FROM " + Params.dbName + ".member WHERE member_id = " + m.getId() + " AND restaurant_id = " + m.getRestaurantID();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		if(count == 0){
			throw new BusinessException("操作失败, 会员资料删除失败, 未知错误.", 9963);
		}
		
		return count;
	}
	
	/**
	 * 
	 * @param m
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int deleteMember(Member m) throws SQLException, BusinessException {
		int count = 0;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = MemberDao.deleteMember(dbCon, m);
			dbCon.conn.commit();
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param mo
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int recharge(DBCon dbCon, MemberOperation mo) throws SQLException, BusinessException {
		int count = 0;
		Member m = MemberDao.getMember(dbCon, mo.getMemberID());
		MemberType mt = m.getMemberType();
		
		mo.setMemberCardID(m.getMemberCardID());
		mo.setMemberCardAlias(m.getMemberCard().getAliasID());
		mo.setOperationType(MemberOperation.OperationType.CHARGE.getValue());
		mo.setDeltaBaseBalance(mo.getChargeMoney());
		mo.setDeltaExtraBalance((int)(mo.getChargeMoney() * Math.abs(mt.getChargeRate() - 1)));
		mo.setDeltaPoint((int)(mo.getChargeMoney() * Math.abs(mt.getExchangeRate() - 1)));
		mo.setRemainingBaseBalance(mo.getDeltaBaseBalance() + m.getBaseBalance());
		mo.setRemainingExtraBalance(mo.getDeltaGiftBalance() + m.getExtraBalance());
		mo.setRemainingPoint(mo.getDeltaPoint() + m.getPoint());
		
		// updateBalance
		Member updateBalance = Member.buildToBalance(mo.getMemberID(), mo.getRemainingBaseBalance(), mo.getRemainingExtraBalance(), mo.getStaffID(), Member.OPERATION_UPDATE + Member.OPERATION_CHARGE);
		count = MemberDao.updateMemberBalance(dbCon, updateBalance);
		if(count == 0){
			throw new BusinessException("操作失败, 修改会员金额信息失败!", 9989);
		}
		
		// updatePoint
		Member updatePoint = Member.buildToPoint(mo.getMemberID(), mo.getRemainingPoint(), mo.getStaffID(), Member.OPERATION_UPDATE + Member.OPERATION_CHARGE);
		count = MemberDao.updateMemberPoint(dbCon, updatePoint);
		if(count == 0){
			throw new BusinessException("操作失败, 修改会员积分信息失败!", 9988);
		}
		
		// 插入操作痕迹
		count = MemberOperationDao.insertMemberOperation(dbCon, mo);
		if(count == 0){
			throw new BusinessException("操作失败, 添加充值操作日志失败, 请联系客服人员!", 9987);
		}
		return count;
	}
	
	/**
	 * 
	 * @param mo
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int recharge(MemberOperation mo) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = MemberDao.recharge(dbCon, mo);
			dbCon.conn.commit();
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param m
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int changeMemberCard(DBCon dbCon, Member m) throws SQLException, BusinessException{
		int count = 0;
		// 验证新旧卡是否一样, 是则不用修改
		Member check = MemberDao.getMember(m.getId());
		if(check.getMemberCard().getAliasID().equals(m.getMemberCard().getAliasID())){
			throw new BusinessException("操作失败, 新旧卡一样, 无需修改.", 9988);
		}
		// 验证新卡是否存在, 是否可用
		Map<Object, Object> cp = new HashMap<Object, Object>();
		cp.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.restaurant_id = " + m.getRestaurantID() + " AND A.member_card_alias = '" + m.getMemberCard().getAliasID() + "'");
		List<MemberCard> ncl = MemberCardDao.getMemberCard(dbCon, cp);
		MemberCard newCard = null;
		if(ncl == null || ncl.size() == 0){
			newCard = new MemberCard();
			newCard.setRestaurantID(m.getRestaurantID());
			newCard.setAliasID(m.getMemberCard().getAliasID());
			newCard.setStatus(MemberCard.STATUS_ACTIVE);
			newCard.setLastStaffID(m.getStaffID());
			newCard.setComment(Member.OPERATION_UPDATE + MemberCard.OPERATION_CHANGE + MemberCard.OPERATION_INSERT);
			count = MemberCardDao.insertMemberCard(dbCon, newCard);
			if(count == 0){
				throw new BusinessException("操作失败, 添加新会员卡信息失败, 请尝试更换其他会员卡.", 9987);
			}else{
				dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.SQL_QUERY_LAST_INSERT_ID);
				if(dbCon.rs != null && dbCon.rs.next()){
					newCard.setId(dbCon.rs.getInt(1));
				}
			}
		}else{
			newCard = ncl.get(0);
			if(newCard.getStatus() == MemberCard.Status.ACTIVE){
				throw new BusinessException("操作失败, 该会员卡已被其他会员使用, 请更换其他会员卡 .", 9986);
			}else if(newCard.getStatus() == MemberCard.Status.LOST){
				throw new BusinessException("操作失败, 该会员卡已挂失, 请更换其他会员卡 .", 9985);
			}else if(newCard.getStatus() == MemberCard.Status.DISABLE){
				throw new BusinessException("操作失败, 该会员卡已禁用, 请更换其他会员卡 .", 9984);
			}
		}
		
		String updateSQL = "UPDATE member SET member_card_id = " + newCard.getId()
						+ " WHERE member_id = " + m.getId() + " AND restaurant_id = " + m.getRestaurantID();
		count = dbCon.stmt.executeUpdate(updateSQL);
		if(count == 0){
			throw new BusinessException("操作失败, 更换会员卡信息, 请尝试更换其他会员卡或联系客服人员.", 9999);
		}
		return count;
	}
	
	/**
	 * 
	 * @param m
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int changeMemberCard(Member m) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = MemberDao.changeMemberCard(dbCon, m);
			dbCon.conn.commit();
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
}
