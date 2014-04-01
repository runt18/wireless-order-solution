package com.wireless.db.weixin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WeixinMemberError;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.weixinInfo.WeixinInfo;

public class WeixinInfoDao {
	/**
	 * 
	 * @param builder
	 * @return
	 * @throws SQLException
	 */
	public static void insert(Staff staff, WeixinInfo.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param builder
	 * @return
	 * @throws SQLException
	 */
	public static void insert(DBCon dbCon, Staff staff, WeixinInfo.InsertBuilder builder) throws SQLException{
		WeixinInfo wxInfo = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".weixin_misc " +
			  " (restaurant_id)" +
			  " VALUES (" +
			  wxInfo.getRestaurantId() + 
			  " ) ";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

	}
	/**
	 * 
	 * @param dbCon
	 * @param builder
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void update(DBCon dbCon, Staff staff, WeixinInfo.UpdateBuilder builder) throws SQLException, BusinessException{
		WeixinInfo wxInfo = builder.build();
		
		String sql;
		sql = " UPDATE "+ Params.dbName + ".weixin_misc SET " +
			  " restaurant_id = " + wxInfo.getRestaurantId() +
			  (builder.isBoundCouponTypeChanged() ? " ,bound_coupon_type = " + wxInfo.getBoundCouponType() : "") +
			  (builder.isWeixinLogoChanged() ? " ,weixin_logo = '" + wxInfo.getWeixinLogo() + "'" : "") +
			  (builder.isWeixinInfoChanged() ? " ,weixin_info = '" + wxInfo.getWeixinInfo() + "'" : "") +
			  (builder.isWeixinPromotChanged() ? " ,weixin_promote = '" + wxInfo.getWeixinPromote() + "'" : "") +
			  " WHERE restaurant_id = " + wxInfo.getRestaurantId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(WeixinMemberError.WEIXIN_INFO_NOT_EXIST);
		}
		
	}
	
	/**
	 * 
	 * @param builder
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void update(Staff staff, WeixinInfo.UpdateBuilder builder) throws SQLException, BusinessException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static void delete(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			delete(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static void delete(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".weixin_misc WHERE restaurant_id = " + staff.getRestaurantId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(WeixinMemberError.WEIXIN_INFO_NOT_EXIST);
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 */
	private static List<WeixinInfo> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		
		List<WeixinInfo> result = new ArrayList<WeixinInfo>();
		
		String sql;
		sql = " SELECT restaurant_id, bound_coupon_type, weixin_logo, weixin_info, weixin_promote FROM " +
			  Params.dbName + ".weixin_misc WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : " ") + 
			  (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			WeixinInfo wx = new WeixinInfo(dbCon.rs.getInt("restaurant_id"));
			wx.setBoundCouponType(dbCon.rs.getInt("bound_coupon_type"));
			wx.setWeixinInfo(dbCon.rs.getString("weixin_info"));
			wx.setWeixinLogo(dbCon.rs.getString("weixin_logo"));
			wx.setWeixinPromote(dbCon.rs.getString("weixin_promote"));
			result.add(wx);
		}
		dbCon.rs.close();
		return result;
	}
	
	/**
	 * 
	 * @param restaurantId
	 * @return
	 * @throws SQLException
	 * @throws BusinessException 
	 */
	public static WeixinInfo getByRestaurant(int restaurantId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			List<WeixinInfo> result = getByCond(dbCon, StaffDao.getAdminByRestaurant(restaurantId), null, null);
			if(result.isEmpty()){
				throw new BusinessException(WeixinMemberError.WEIXIN_INFO_NOT_EXIST);
			}else{
				return result.get(0);
			}
		}finally{
			dbCon.disconnect();
		}
		
	}

}
