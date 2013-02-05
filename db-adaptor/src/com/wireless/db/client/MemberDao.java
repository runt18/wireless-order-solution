package com.wireless.db.client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.distMgr.QueryDiscountDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.Client;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberCard;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.distMgr.DiscountPlanPojo;
import com.wireless.pojo.distMgr.DiscountPojo;
import com.wireless.pojo.system.Staff;
import com.wireless.protocol.Discount;
import com.wireless.util.SQLUtil;

public class MemberDao {
	
	/**
	 * 
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws Exception
	 */
	public static List<MemberType> getMemberType(String extraCond, String orderClause) throws Exception{
		List<MemberType> list = new ArrayList<MemberType>();
		MemberType item = null;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			String querySQL = " SELECT A.member_type_id, A.restaurant_id, A.name, A.discount_id, A.discount_type, A.charge_rate, A.exchange_rate, A.attribute"
							+ " ,CASE WHEN A.discount_type = 1 THEN ( SELECT t2.rate FROM " + Params.dbName + ".discount t1, " + Params.dbName + ".discount_plan t2 WHERE t1.discount_id = t2.discount_id  AND t1.discount_id = A.discount_id LIMIT 0,1) ELSE 0 END AS discount_rate"
							+ " FROM " + Params.dbName + ".member_type A"
							+ " WHERE 1=1"
							+ (extraCond != null ? extraCond : "")
							+ (orderClause != null ? orderClause : "");
			dbCon.rs = dbCon.stmt.executeQuery(querySQL);
			while(dbCon.rs != null && dbCon.rs.next()){
				item = new MemberType();
				
				item.setTypeID(dbCon.rs.getInt("member_type_id"));
				item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
				item.setName(dbCon.rs.getString("name"));
				item.setDiscountID(dbCon.rs.getInt("discount_id"));
				item.setDiscountType(dbCon.rs.getInt("discount_type"));
				item.setDiscountRate(dbCon.rs.getDouble("discount_rate"));
				item.setChargeRate(dbCon.rs.getDouble("charge_rate"));
				item.setExchangeRate(dbCon.rs.getDouble("exchange_rate"));
				item.setAttribute(dbCon.rs.getInt("attribute"));
				
				list.add(item);
				item = null;
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return list;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param mt
	 * @throws Exception
	 */
	public static int insertMemberType(DBCon dbCon, MemberType mt) throws Exception{
		int count = 0;
		try{
			dbCon.conn.setAutoCommit(false);
			// 生成全单折扣方案
			if(mt.getDiscountType() == 1){
				MemberDao.createDiscount(dbCon, mt);
			}
			// 插入新数据
			String insertSQL = " INSERT INTO member_type ( restaurant_id, name, discount_id, discount_type, exchange_rate, charge_rate, attribute )"
					+ " VALUES("
					+ mt.getRestaurantID() + ","
					+ "'" + mt.getName() + "',"
					+ mt.getDiscountID() + ","
					+ mt.getDiscountType() + ","
					+ mt.getExchangeRate() + ","
					+ mt.getChargeRate() + ","
					+ mt.getAttribute() 
					+ ")";
			count = dbCon.stmt.executeUpdate(insertSQL);
			dbCon.conn.commit();
		}catch(Exception e){
			throw e;
		}
		return count;
	}
	
	/**
	 * 
	 * @param mt
	 * @throws Exception
	 */
	public static int insertMemberType(MemberType mt) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = insertMemberType(dbCon, mt);
			if(count == 0){
				throw new BusinessException("操作失败, 添加新会员类型未成功.", 9979);
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
	 * @param mt
	 * @throws Exception
	 */
	public static void updateMemberType(MemberType mt) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			// 处理原折扣方式相关的折扣方案
			String querySQL = "SELECT count(restaurant_id) AS count FROM " + Params.dbName + ".discount "
							+ "WHERE restaurant_id = " + mt.getRestaurantID() + " AND discount_id = " + mt.getOther().get(MemberType.OLD_DISCOUNTID_KEY) + " AND status = " + Discount.MEMBERTYPE;
			dbCon.rs = dbCon.stmt.executeQuery(querySQL);
			boolean isEntire = (dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0);
			if(mt.getDiscountType() == MemberType.DISCOUNT_TYPE_DISCOUNT){
				// 处理已关联的全单折扣方案				
				if(isEntire){
					String deleteSQL = "DELETE FROM " + Params.dbName + ".discount "
									 + "WHERE discount_id = " + mt.getOther().get(MemberType.OLD_DISCOUNTID_KEY) + " AND restaurant_id = " + mt.getRestaurantID()  + " AND status = " + Discount.MEMBERTYPE;;
					if(dbCon.stmt.executeUpdate(deleteSQL) > 0){
						deleteSQL = "DELETE FROM " + Params.dbName + ".discount_plan WHERE discount_id = " + mt.getOther().get(MemberType.OLD_DISCOUNTID_KEY);
						dbCon.stmt.executeUpdate(deleteSQL);
					}
				}	
			}else if(mt.getDiscountType() == MemberType.DISCOUNT_TYPE_ENTIRE){
				if(isEntire){
					String updateSQL = "UPDATE discount_plan SET rate = " + mt.getDiscountRate() + " WHERE discount_id = " + mt.getOther().get(MemberType.OLD_DISCOUNTID_KEY);
					if(dbCon.stmt.executeUpdate(updateSQL) == 0){
						throw new BusinessException("操作失败, 修改会员类型全单折扣率失败,未知错误.", 9974);
					}
				}else{
					// 生成全单折扣方案
					MemberDao.createDiscount(dbCon, mt);
				}
			}
			
			// 更新数据
			String updateSQL = " UPDATE member_type SET "
							 + " name = '" + mt.getName() + "', discount_id = " + mt.getDiscountID() + ", discount_type = " + mt.getDiscountType() + ", "
							 + " exchange_rate = " + mt.getExchangeRate() + ", charge_rate = " + mt.getChargeRate() + ", attribute = " + mt.getAttribute()
							 + " WHERE restaurant_id = " + mt.getRestaurantID() + " AND member_type_id = " + mt.getTypeID();
			
			if(dbCon.stmt.executeUpdate(updateSQL) == 0){
				throw new BusinessException("操作失败, 会员类型信息修改失败,请检查数据格式.", 9978);
			}
			
			dbCon.conn.commit();
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param mt
	 * @throws Exception
	 */
	public static void deleteMemberType(MemberType mt) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			String querySQL = "";
			String deleteSQL = "";
			
			// 检查该类型下是否还有会员
			querySQL = "SELECT count(restaurant_id) AS count FROM " + Params.dbName + ".member WHERE restaurant_id = " + mt.getRestaurantID() + " AND member_type_id = " + mt.getTypeID();
			dbCon.rs = dbCon.stmt.executeQuery(querySQL);
			if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
				throw new BusinessException("操作失败, 该类型下已有会员,不允许删除.", 9977);
			}
			
			// 删除全单折扣方案相关信息
			if(mt.getDiscountType() == MemberType.DISCOUNT_TYPE_ENTIRE){
				querySQL = "SELECT count(restaurant_id) AS count FROM " + Params.dbName + ".discount WHERE restaurant_id = " + mt.getRestaurantID() + " AND discount_id = " + mt.getDiscountID() + " AND status = " + Discount.MEMBERTYPE;
				dbCon.rs = dbCon.stmt.executeQuery(querySQL);
				if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
					deleteSQL = "DELETE FROM " + Params.dbName + ".discount WHERE discount_id = " + mt.getDiscountID() + " AND restaurant_id = " + mt.getRestaurantID()  + " AND status = " + Discount.MEMBERTYPE;;
					if(dbCon.stmt.executeUpdate(deleteSQL) > 0){
						deleteSQL = "DELETE FROM " + Params.dbName + ".discount_plan WHERE discount_id = " + mt.getDiscountID();
						dbCon.stmt.executeUpdate(deleteSQL);
					}
				}
			}
			
			// 删除会员类型
			deleteSQL = "DELETE FROM " + Params.dbName + ".member_type WHERE member_type_id = " + mt.getTypeID() + " AND restaurant_id = " + mt.getRestaurantID();
			
			if(dbCon.stmt.executeUpdate(deleteSQL) == 0){
				throw new BusinessException("操作失败, 未找到要删除的原纪录." , 9976);
			}
			
			dbCon.conn.commit();
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 生成全单折扣方案
	 * @param dbCon
	 * @param mt
	 * @throws Exception
	 */
	private static void createDiscount(DBCon dbCon, MemberType mt) throws Exception{
		try{
			DiscountPojo dp = new DiscountPojo();
			DiscountPlanPojo dpp = new DiscountPlanPojo();
			
			dp.setName(mt.getRestaurantID() + "_QDZK_" + mt.getName() + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "" );
			dp.setRestaurantID(mt.getRestaurantID());
			dp.setLevel(0);
			dp.setStatus(Discount.MEMBERTYPE);
			
			dpp.setRate(Float.valueOf(String.valueOf(mt.getDiscountRate())));
			
			try{
				// 生成全单折扣方案信息, 并回写全单折扣方案编号
				if(QueryDiscountDao.insertDiscountBody(dbCon, dp, dpp) > 0){
					dbCon.rs = dbCon.stmt.executeQuery("SELECT discount_id FROM " +  Params.dbName + ".discount WHERE restaurant_id = " + mt.getRestaurantID() + " ORDER BY discount_id DESC LIMIT 0,1");
					if(dbCon.rs != null && dbCon.rs.next()){
						mt.setDiscountID(dbCon.rs.getInt("discount_id"));
					}
				}else{
					throw new BusinessException("操作失败, 设置会员类型全单折扣信息失败,未知错误." , 9974);
				}
			}catch(Exception e){
				throw e;
			}
		}catch(Exception e){
			throw new BusinessException("操作失败, 设置会员类型全单折扣信息失败,未知错误." , 9975);
		}
	}
	
	/**
	 * 
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws Exception
	 */
	public static List<Member> getMember(String extraCond, String orderClause) throws Exception{
		List<Member> list = new ArrayList<Member>();
		Member item = null;
		MemberCard memberCard = null;
		MemberType memberType = null;
		Staff staff = null;
		Client client = null;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			String querySQL = "SELECT "
							+ " A.member_id, A.restaurant_id, A.base_balance, A.extra_balance, A.point, A.birth_date, A.last_mod_date, "
							+ " A.comment member_comment, A.status member_status, A.last_staff_id, (A.base_balance + A.extra_balance) totalBalance,"
							+ " B.member_type_id, B.name member_type_name,"
							+ " C.member_card_id, C.member_card_alias, C.status member_card_status, C.comment member_card_comment,"
							+ " C.last_staff_id member_card_last_staff_id, C.last_mod_date member_card_last_mod_date,"
							+ " E.client_id, E.name client_name, E.client_type_id, E.sex, E.birth_date, E.level, E.tele, E.mobile, "
							+ " E.birthday, E.id_card, E.company, E.taste_pref, E.taboo, E.contact_addr, E.comment client_comment,"
							+ " F.staff_id, F.staff_name"
							+ " FROM "
							+ Params.dbName + ".member A"
							+ " LEFT JOIN "
							+ Params.dbName + ".member_type B ON A.restaurant_id = B.restaurant_id AND  A.member_type_id = B.member_type_id"
							+ " LEFT JOIN "
							+ Params.dbName + ".member_card C ON A.restaurant_id = C.restaurant_id AND A.member_card_id = C.member_card_id AND C.status = 0"
							+ " LEFT JOIN "
							+ Params.dbName + ".client_member D ON A.restaurant_id = D.restaurant_id AND A.member_id = D.member_id"
							+ " LEFT JOIN "
							+ Params.dbName + ".client E ON E.restaurant_id = D.restaurant_id AND E.client_id = D.client_id"
							+ " JOIN "
							+ "  (SELECT staff_id, name staff_name, restaurant_id FROM " + Params.dbName + ".staff"
							+ "  UNION ALL "
							+ "  SELECT terminal_id staff_id, owner_name staff_name, restaurant_id FROM " + Params.dbName + ".terminal) F "
							+ " ON A.restaurant_id = F.restaurant_id AND A.last_staff_id = F.staff_id"
							+ " WHERE 1=1"
							+ (extraCond != null ? extraCond : "")
							+ (orderClause != null ? orderClause : "");
			
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
				
				list.add(item);
				item = null;
			}
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return list;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param m
	 * @return
	 * @throws Exception
	 */
	public static int insertMember(DBCon dbCon, Member m) throws Exception{
		dbCon.conn.setAutoCommit(false);
		int count = 0;
		// 绑定所有基础信息对象的restaurantID
		m.getClient().setRestaurantID(m.getRestaurantID());
		m.getMemberType().setRestaurantID(m.getRestaurantID());
		m.getMemberCard().setRestaurantID(m.getRestaurantID());
		m.getStaff().setRestaurantID(m.getRestaurantID());
		
		String querySQL = "", insertSQL = "";
		MemberCard memberCard = m.getMemberCard();
		Staff staff = m.getStaff();
		
		// 查找当前请求操作人信息
		staff = getOperationStaff(dbCon, staff);
		m.setStaffID(staff.getId());
		memberCard.setLastStaffID(staff.getId());
		
		// 会员卡资料处理
		querySQL = "SELECT COUNT(member_card_id) FROM " + Params.dbName + ".member_card"
				   + " WHERE restaurant_id = " + m.getRestaurantID() + " AND member_card_alias = " + memberCard.getAliasID();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		count = 0;
		if(dbCon.rs != null && dbCon.rs.next()){
			count = dbCon.rs.getInt(1);
			dbCon.rs = null;
			querySQL = "";
		}
		if(count != 0){
			throw new BusinessException("操作失败, 该会员卡已绑定其他会员.", 9968);
		}
		// 添加会员卡资料并绑定会员
		count = insertMemberCard(dbCon, memberCard);
		if(count == 0){
			throw new BusinessException("操作失败, 添加会员卡资料失败, 未知错误.", 9967);
		}
		dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.QUERY_LAST_ID_SQL);// 获取新生成会员卡信息数据编号
		if(dbCon.rs != null && dbCon.rs.next()){
			m.setMemberCardID(dbCon.rs.getInt(1));
			dbCon.rs = null;
		}
		memberCard = m.getMemberCard();
		
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
			dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.QUERY_LAST_ID_SQL);// 获取新生成会员卡信息数据编号
			if(dbCon.rs != null && dbCon.rs.next()){
				m.setId(dbCon.rs.getInt(1));
				dbCon.rs = null;
			}
		}
		
		// 客户信息处理
		bindMemberClient(dbCon, m);
		
		dbCon.conn.commit();
		return count;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public static int insertMember(Member m) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = insertMember(dbCon, m);
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
	 * @param m
	 * @return
	 * @throws Exception
	 */
	public static int updateMember(DBCon dbCon, Member m) throws Exception{
		int count = 0;
		dbCon.conn.setAutoCommit(false);
		// 绑定所有基础信息对象的restaurantID
		m.getClient().setRestaurantID(m.getRestaurantID());
		m.getMemberType().setRestaurantID(m.getRestaurantID());
		m.getMemberCard().setRestaurantID(m.getRestaurantID());
		m.getStaff().setRestaurantID(m.getRestaurantID());		
		
		String querySQL = "", updateSQL = "";
		MemberCard memberCard = m.getMemberCard();
		Staff staff = m.getStaff();
		
		// 查找当前请求操作人信息
		staff = getOperationStaff(dbCon, staff);
		m.setStaffID(staff.getId());
		memberCard.setLastStaffID(staff.getId());			
		
		// 验证会员卡信息
		querySQL = "SELECT member_card_id, member_card_alias FROM " + Params.dbName + ".member_card "
				 + " WHERE status = 0 "
				 + " AND member_card_id = (SELECT member_card_id FROM " + Params.dbName + ".member WHERE member_id = " + m.getId() + ")";
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next()){
			memberCard.setId(dbCon.rs.getInt("member_card_id"));
			String oldCardAlias = dbCon.rs.getString("member_card_alias");
			if(!oldCardAlias.equals(memberCard.getAliasID())){
				querySQL = "SELECT COUNT(*) FROM " + Params.dbName + ".member_card "
						 + " WHERE "
						 + " restaurant_id = " + memberCard.getRestaurantID() + " AND member_card_alias = '" + memberCard.getAliasID() + "'";
				dbCon.rs = dbCon.stmt.executeQuery(querySQL);
				if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt(1) > 0){
					throw new BusinessException("操作失败, 该会员卡已绑定其他会员.", 9968);
				}else{
					// 设置原会员卡状态为禁用
					updateSQL = "UPDATE member_card SET last_mod_date = NOW(), status = " + MemberCard.STATUS_DISABLE
							  + " ,comment = '" + Member.OPERATION_UPDATE + MemberCard.OPERATION_DISABLE + "', last_staff_id = " + staff.getId()
							  + " WHERE member_card_id = " + memberCard.getId();
					dbCon.stmt.executeUpdate(updateSQL);
					memberCard.setComment(Member.OPERATION_UPDATE + MemberCard.OPERATION_INSERT);
					// 添加新会员卡
					insertMemberCard(dbCon, memberCard);
					dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.QUERY_LAST_ID_SQL);// 获取新生成会员卡信息数据编号
					if(dbCon.rs != null && dbCon.rs.next()){
						memberCard.setId(dbCon.rs.getInt(1));
						dbCon.rs = null;
					}
				}
			}
			dbCon.rs = null;
		}else{
			throw new BusinessException("操作失败, 获取原会员卡信息失败.", 9966);
		}
		
		// 客户信息处理
		count = bindMemberClient(dbCon, m);
		
		updateSQL = "UPDATE member SET"
				  + " member_type_id = " + m.getMemberTypeID() + ", member_card_id = " + memberCard.getId() + ", status = " + m.getStatus() + "," 
				  + " last_mod_date = NOW(), last_staff_id = " + staff.getId() + ", comment = '" + m.getComment() + "'"
				  + " WHERE member_id = " + m.getId();
		count = dbCon.stmt.executeUpdate(updateSQL);
		if(count == 0){
			throw new BusinessException("操作失败, 修改会员信息失败, 未知错误.", 9967);
		}
		
		dbCon.conn.commit();
		return count;
	}
	
	/**
	 * 
	 * @param m
	 * @return
	 * @throws Exception
	 */
	public static int updateMember(Member m) throws Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = updateMember(dbCon, m);
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
	 * @param mc
	 * @return
	 * @throws Exception
	 */
	public static int insertMemberCard(DBCon dbCon, MemberCard mc) throws Exception{
		int count = 0;
		// 添加会员卡资料并绑定会员
		String insertSQL = "INSERT INTO member_card (restaurant_id, member_card_alias, status, last_staff_id, last_mod_date, comment) "
				+ " VALUES("
				+ mc.getRestaurantID() + ","
				+ mc.getAliasID() + ","
				+ MemberCard.STATUS_NORMAL + ","
				+ mc.getLastStaffID() + ","
				+ "NOW(),"
				+ "'" + mc.getComment() + "'"
				+ " )";
		count = dbCon.stmt.executeUpdate(insertSQL);
		if(count == 0){
			throw new BusinessException("操作失败, 会员卡资料新建失败, 未知错误.", 9971);
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param m
	 * @return
	 * @throws Exception
	 */
	public static int bindMemberClient(DBCon dbCon, Member m) throws Exception{
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
				dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.QUERY_LAST_ID_SQL);// 获取客户信息编号
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
					dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.QUERY_LAST_ID_SQL);// 获取客户信息编号
					if(dbCon.rs != null && dbCon.rs.next()){
						client.setClientID(dbCon.rs.getInt(1));
						dbCon.rs = null;
						querySQL = "";
					}
				}
			}
		}
		// 绑定客户资料与会员资料关系
		insertSQL = "INSERT INTO client_member (client_id, member_id, restaurant_id) "
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
	 * @throws Exception
	 */
	public static Staff getOperationStaff(DBCon dbCon, Staff staff) throws Exception{
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
	 * @throws Exception
	 */
	public static int deleteMember(DBCon dbCon, Member m) throws Exception {
		int count = 0;
		String updateSQL = "", deleteSQL = "";
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
		
		// 设置绑定的会员卡状态为禁用
		updateSQL = "UPDATE member_card SET last_mod_date = NOW(), status = " + MemberCard.STATUS_DISABLE
				  + " ,comment = '" + Member.OPERATION_DELETE + MemberCard.OPERATION_DISABLE + "', last_staff_id = " + staff.getId()
				  + " WHERE member_card_id = (SELECT member_card_id FROM " + Params.dbName + ".member WHERE member_id = " + m.getId() + ")";
		count = dbCon.stmt.executeUpdate(updateSQL);
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
	 * @throws Exception
	 */
	public static int deleteMember(Member m) throws Exception {
		int count = 0;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = MemberDao.deleteMember(dbCon, m);
			dbCon.conn.commit();
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
}


