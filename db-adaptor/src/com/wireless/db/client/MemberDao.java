package com.wireless.db.client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.distMgr.QueryDiscountDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.distMgr.DiscountPlanPojo;
import com.wireless.pojo.distMgr.DiscountPojo;
import com.wireless.protocol.Discount;

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
							+ " ,CASE WHEN A.discount_type = 1 THEN ( SELECT t2.rate FROM discount t1, discount_plan t2 WHERE t1.discount_id = t2.discount_id  AND t1.discount_id = A.discount_id LIMIT 0,1) ELSE 0 END AS discount_rate"
							+ " FROM member_type A"
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
	 * @param mt
	 * @throws Exception
	 */
	public static void insertMemberType(MemberType mt) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			// 生成全单折扣方案
			if(mt.getDiscountType() == 1){
				MemberDao.createDiscount(dbCon, mt);
			}
			
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
			
			if(dbCon.stmt.executeUpdate(insertSQL) == 0){
				throw new BusinessException("操作失败, 添加新会员类型未成功.", 9979);
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
	public static void updateMemberType(MemberType mt) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			// 处理原折扣方式相关的折扣方案
			String querySQL = "SELECT count(restaurant_id) AS count FROM discount WHERE restaurant_id = " + mt.getRestaurantID() + " AND discount_id = " + mt.getOther().get(MemberType.OLD_DISCOUNTID_KEY) + " AND status = " + Discount.MEMBERTYPE;
			dbCon.rs = dbCon.stmt.executeQuery(querySQL);
			boolean isEntire = (dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0);
			if(mt.getDiscountType() == 0){
				// 处理已关联的全单折扣方案				
				if(isEntire){
					String deleteSQL = "DELETE FROM discount WHERE discount_id = " + mt.getOther().get(MemberType.OLD_DISCOUNTID_KEY) + " AND restaurant_id = " + mt.getRestaurantID()  + " AND status = " + Discount.MEMBERTYPE;;
					if(dbCon.stmt.executeUpdate(deleteSQL) > 0){
						deleteSQL = "DELETE FROM discount_plan WHERE discount_id = " + mt.getOther().get(MemberType.OLD_DISCOUNTID_KEY);
						dbCon.stmt.executeUpdate(deleteSQL);
					}
				}	
			}else if(mt.getDiscountType() == 1){
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
			
			String querySQL = "SELECT count(restaurant_id) AS count FROM member WHERE restaurant_id = " + mt.getRestaurantID() + " AND member_type_id = " + mt.getTypeID();
			dbCon.rs = dbCon.stmt.executeQuery(querySQL);
			if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt("count") > 0){
				throw new BusinessException("操作失败, 该类型下已有会员,不允许删除.", 9977);
			}
			
			
			
			
			
			
			
			
			
			String deleteSQL = "DELETE FROM member_type WHERE member_type_id = " + mt.getTypeID() + " AND restaurant_id = " + mt.getRestaurantID();
			
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
			
			dpp.setRate(Float.valueOf(mt.getDiscountRate() + ""));
			
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
			e.printStackTrace();
			throw new BusinessException("操作失败, 设置会员类型全单折扣信息失败,未知错误." , 9975);
		}
	}
	
}




































