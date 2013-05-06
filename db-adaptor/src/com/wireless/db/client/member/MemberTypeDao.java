package com.wireless.db.client.member;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.distMgr.Discount.Status;
import com.wireless.pojo.distMgr.DiscountPlan;
import com.wireless.util.SQLUtil;

public class MemberTypeDao {

	/**
	 * 
	 * @param dbCon
	 * @param mt
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int insertMemberType(DBCon dbCon, MemberType mt) throws SQLException, BusinessException{
		int count = 0;
		// 生成全单折扣方案
		if(mt.getDiscountType() == 1){
			MemberTypeDao.createDiscount(dbCon, mt);
		}
		// 插入新数据
		String insertSQL = " INSERT INTO " + Params.dbName + ".member_type ( restaurant_id, name, discount_id, discount_type, exchange_rate, charge_rate, attribute )"
				+ " VALUES("
				+ mt.getRestaurantID() + ","
				+ "'" + mt.getName() + "',"
				+ mt.getDiscount().getId() + ","
				+ mt.getDiscountType() + ","
				+ mt.getExchangeRate() + ","
				+ mt.getChargeRate() + ","
				+ mt.getAttribute().getVal()
				+ ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		return count;
	}
	
	/**
	 * 
	 * @param mt
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int insertMemberType(MemberType mt) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = insertMemberType(dbCon, mt);
			dbCon.conn.commit();
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 生成全单折扣方案
	 * @param dbCon
	 * @param mt
	 * @throws SQLException
	 * @throws BusinessException
	 */
	private static void createDiscount(DBCon dbCon, MemberType mt) throws SQLException, BusinessException{
		Discount dp = new Discount();
		DiscountPlan dpp = new DiscountPlan();
		
		dp.setName(mt.getRestaurantID() + "_QDZK_" + mt.getName() + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "" );
		dp.setRestaurantId(mt.getRestaurantID());
		dp.setLevel(0);
		dp.setStatus(Status.MEMBER_TYPE);
		
		dpp.setRate(Float.valueOf(String.valueOf(mt.getDiscountRate())));
		
		try{
			// 生成全单折扣方案信息, 并回写全单折扣方案编号
			if(DiscountDao.insertDiscountBody(dbCon, dp, dpp) > 0){
				dbCon.rs = dbCon.stmt.executeQuery("SELECT discount_id FROM " +  Params.dbName + ".discount WHERE restaurant_id = " + mt.getRestaurantID() + " ORDER BY discount_id DESC LIMIT 0,1");
				if(dbCon.rs != null && dbCon.rs.next()){
					mt.getDiscount().setId(dbCon.rs.getInt("discount_id"));
				}
			}else{
				throw new BusinessException(MemberError.TYPE_SET_ORDER_DISCOUNT);
			}
		}catch(BusinessException e){
			throw e;
		}catch(SQLException e){
			throw e;
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param mt
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int deleteMemberType(DBCon dbCon, MemberType mt) throws BusinessException, SQLException{
		int count = 0;
		String querySQL = "";
		String deleteSQL = "";
		
		// 检查该类型下是否还有会员
		querySQL = "SELECT count(restaurant_id) AS count FROM " + Params.dbName + ".member WHERE restaurant_id = " + mt.getRestaurantID() + " AND member_type_id = " + mt.getTypeID();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
			throw new BusinessException(MemberError.TYPE_DELETE_ISNOT_EMPTY);
		}
		
		// 删除全单折扣方案相关信息
		if(mt.getDiscountType() == MemberType.DISCOUNT_TYPE_ENTIRE){
			querySQL = "SELECT count(restaurant_id) AS count FROM " + Params.dbName + ".discount WHERE restaurant_id = " + mt.getRestaurantID() + " AND discount_id = " + mt.getDiscount().getId() + " AND status = " + Status.MEMBER_TYPE.getVal();
			dbCon.rs = dbCon.stmt.executeQuery(querySQL);
			if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
				deleteSQL = "DELETE FROM " + Params.dbName + ".discount WHERE discount_id = " + mt.getDiscount().getId() + " AND restaurant_id = " + mt.getRestaurantID()  + " AND status = " + Status.MEMBER_TYPE.getVal();
				if(dbCon.stmt.executeUpdate(deleteSQL) > 0){
					deleteSQL = "DELETE FROM " + Params.dbName + ".discount_plan WHERE discount_id = " + mt.getDiscount().getId();
					dbCon.stmt.executeUpdate(deleteSQL);
				}
			}
		}
		
		// 删除会员类型
		deleteSQL = "DELETE FROM " + Params.dbName + ".member_type WHERE member_type_id = " + mt.getTypeID() + " AND restaurant_id = " + mt.getRestaurantID();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		return count;
	}
	
	/**
	 * 
	 * @param mt
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int deleteMemberType(MemberType mt) throws BusinessException, SQLException {
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = MemberTypeDao.deleteMemberType(dbCon, mt);
			if(count == 0){
				throw new BusinessException(MemberError.TYPE_DELETE);
			}
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
	 * @param mt
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int updateMemberType(DBCon dbCon, MemberType mt) throws SQLException, BusinessException{
		int count = 0;
		// 处理原折扣方式相关的折扣方案
		String querySQL = "SELECT count(restaurant_id) AS count FROM " + Params.dbName + ".discount "
				+ "WHERE restaurant_id = " + mt.getRestaurantID() + " AND discount_id = " + mt.getOther().get(MemberType.OLD_DISCOUNTID_KEY) + " AND status = " + Status.MEMBER_TYPE.getVal();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		boolean isEntire = (dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0);
		if(mt.getDiscountType() == MemberType.DISCOUNT_TYPE_DISCOUNT){
			// 处理已关联的全单折扣方案				
			if(isEntire){
				String deleteSQL = "DELETE FROM " + Params.dbName + ".discount "
								 + "WHERE discount_id = " + mt.getOther().get(MemberType.OLD_DISCOUNTID_KEY) + " AND restaurant_id = " + mt.getRestaurantID()  + " AND status = " + Status.MEMBER_TYPE.getVal();
				if(dbCon.stmt.executeUpdate(deleteSQL) > 0){
					deleteSQL = "DELETE FROM " + Params.dbName + ".discount_plan WHERE discount_id = " + mt.getOther().get(MemberType.OLD_DISCOUNTID_KEY);
					dbCon.stmt.executeUpdate(deleteSQL);
				}
			}	
		}else if(mt.getDiscountType() == MemberType.DISCOUNT_TYPE_ENTIRE){
			if(isEntire){
				String updateSQL = "UPDATE " + Params.dbName + ".discount_plan SET rate = " + mt.getDiscountRate() + " WHERE discount_id = " + mt.getOther().get(MemberType.OLD_DISCOUNTID_KEY);
				if(dbCon.stmt.executeUpdate(updateSQL) == 0){
					throw new BusinessException(MemberError.TYPE_SET_ORDER_DISCOUNT);
				}
			}else{
				// 生成全单折扣方案
				MemberTypeDao.createDiscount(dbCon, mt);
			}
		}
		
		// 更新数据
		String updateSQL = " UPDATE " + Params.dbName + ".member_type SET "
						 + " name = '" + mt.getName() + "', discount_id = " + mt.getDiscount().getId() + ", discount_type = " + mt.getDiscountType() + ", "
						 + " exchange_rate = " + mt.getExchangeRate() + ", charge_rate = " + mt.getChargeRate() + ", attribute = " + mt.getAttribute().getVal()
						 + " WHERE restaurant_id = " + mt.getRestaurantID() + " AND member_type_id = " + mt.getTypeID();
		count = dbCon.stmt.executeUpdate(updateSQL);
		return count;
	}
	
	/**
	 * 
	 * @param mt
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int updateMemberType(MemberType mt) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			count = MemberTypeDao.updateMemberType(dbCon, mt);
			if(count == 0){
				throw new BusinessException(MemberError.TYPE_UPDATE);
			}
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
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberType> getMemberType(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		List<MemberType> list = new ArrayList<MemberType>();
		Discount discount = null; 
		MemberType item = null;
//		Object hasPlan = params.get(DiscountPlanPojo.class);
		String querySQL = " SELECT A.member_type_id, A.restaurant_id, A.name, A.discount_id, A.discount_type, A.charge_rate, A.exchange_rate, A.attribute"
				+ " ,CASE WHEN A.discount_type = 1 THEN ( SELECT t2.rate FROM " + Params.dbName + ".discount t1, " + Params.dbName + ".discount_plan t2 WHERE t1.discount_id = t2.discount_id  AND t1.discount_id = A.discount_id LIMIT 0,1) ELSE 0 END AS discount_rate"
				+ " ,B.name discount_name, B.status discount_status "
				+ " FROM " + Params.dbName + ".member_type A LEFT JOIN discount B ON A.discount_id = B.discount_id"
				+ " WHERE 1=1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new MemberType();
			discount = new Discount();
			
			item.setTypeID(dbCon.rs.getInt("member_type_id"));
			item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			item.setName(dbCon.rs.getString("name"));
			item.setDiscountRate(dbCon.rs.getFloat("discount_rate"));
			item.setDiscountType(dbCon.rs.getInt("discount_type"));
			item.setChargeRate(dbCon.rs.getFloat("charge_rate"));
			item.setExchangeRate(dbCon.rs.getFloat("exchange_rate"));
			item.setAttribute(dbCon.rs.getInt("attribute"));
			
			discount.setId(dbCon.rs.getInt("discount_id"));
			discount.setName(dbCon.rs.getString("discount_name"));
			discount.setStatus(dbCon.rs.getInt("discount_status"));
//			if(hasPlan != null && hasPlan instanceof Boolean && Boolean.valueOf(hasPlan.toString()) == true){
//				DiscountPlanPojo[] dp = QueryDiscountDao.getDiscountPlan(" AND B.discount_id = " + discount.getId(), " ORDER BY A.dist_plan_id ");
//				discount.setPlans(new ArrayList<DiscountPlanPojo>(Arrays.asList(dp)));
//			}else{
//				discount.setPlans(null);
//			}
//			discount.setPlans(null);
			item.setDiscount(discount);
			
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
	public static List<MemberType> getMemberType(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberTypeDao.getMemberType(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * 	The return value may be empty
	 * @throws SQLException
	 */
	public static MemberType getMemberTypeById(DBCon dbCon, int id) throws SQLException{
		List<MemberType> list = null;
		MemberType mt = null;
		Map<Object, Object> params = new HashMap<Object, Object>();
		list = MemberTypeDao.getMemberType(dbCon, params);
		if(!list.isEmpty()){
			mt = list.get(0);
		}
		return mt;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public static MemberType getMemberTypeById(int id) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberTypeDao.getMemberTypeById(dbCon, id);
		}finally{
			dbCon.disconnect();
		}
	}
}
