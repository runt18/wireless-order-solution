package com.wireless.db.weixin;

import java.sql.SQLException;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.DeviceError;
import com.wireless.pojo.weixin.weixinInfo.WeixinInfo;

public class WeixinInfoDao {
	/**
	 * 
	 * @param builder
	 * @return
	 * @throws SQLException
	 */
	public static int insert(WeixinInfo.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, builder);
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
	public static int insert(DBCon dbCon, WeixinInfo.InsertBuilder builder) throws SQLException{
		WeixinInfo wxInfo = builder.build();
		String sql = " INSERT INTO " + Params.dbName + ".weixin_misc " +
					"(restaurant_id, bound_coupon_type, weixin_logo, weixin_info, weixin_promote)" +
					 " VALUES (" +
					 wxInfo.getRestaurantId() + ", " +
					 wxInfo.getBoundCouponType() + ", " +
					 "'" + wxInfo.getWeixinLogo() + "', " +
					 "'" + wxInfo.getWeixinInfo() + "', " +
					 "'" + wxInfo.getWeixinPromote() + "') ";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of device is not generated successfully.");
		}
	}
	/**
	 * 
	 * @param dbCon
	 * @param builder
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void update(DBCon dbCon, WeixinInfo.UpdateBuilder builder) throws SQLException, BusinessException{
		WeixinInfo wxInfo = builder.build();
		
		String sql = "UPDATE "+ Params.dbName + ".weixin_misc SET " +
						" restaurant_id = " + wxInfo.getRestaurantId() +
						(wxInfo.getBoundCouponType() != 0 ? " ,bound_coupon_type = " + wxInfo.getBoundCouponType() : "") +
						(wxInfo.getWeixinLogo() != null ? " ,weixin_logo = '" + wxInfo.getWeixinLogo() + "'" : "") +
						(wxInfo.getWeixinInfo() != null ? " ,weixin_info = '" + wxInfo.getWeixinInfo() + "'" : "") +
						(wxInfo.getWeixinPromote() != null ? " ,weixin_promote = '" + wxInfo.getWeixinPromote() + "'" : "") +
						" WHERE restaurant_id = " + wxInfo.getRestaurantId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(DeviceError.DEVICE_NOT_EXIST);
		}
		
	}
	
	/**
	 * 
	 * @param builder
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void update(WeixinInfo.UpdateBuilder builder) throws SQLException, BusinessException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, builder);
		}finally{
			dbCon.disconnect();
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
	private static WeixinInfo getWeixinInfo(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		WeixinInfo wx = new WeixinInfo();
		String sql = "SELECT restaurant_id, bound_coupon_type, weixin_logo, weixin_info, weixin_promote FROM " +
					Params.dbName + ".weixin_misc WHERE 1 = 1" + 
					(extraCond != null ? extraCond : " ");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			wx.setBoundCouponType(dbCon.rs.getInt("bound_coupon_type"));
			wx.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			wx.setWeixinInfo(dbCon.rs.getString("weixin_info"));
			wx.setWeixinLogo(dbCon.rs.getString("weixin_logo"));
			wx.setWeixinPromote(dbCon.rs.getString("weixin_promote"));
		}
		dbCon.rs.close();
		return wx;
	}
	
	/**
	 * 
	 * @param restaurantId
	 * @return
	 * @throws SQLException
	 */
	public static WeixinInfo getWeixinInfo(int restaurantId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getWeixinInfo(dbCon, " AND restaurant_id = " + restaurantId, null);
		}finally{
			dbCon.disconnect();
		}
		
	}

}
