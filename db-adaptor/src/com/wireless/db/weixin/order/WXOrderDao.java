package com.wireless.db.weixin.order;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.weixin.order.WXOrder;
import com.wireless.pojo.weixin.order.WXOrderFood;

public class WXOrderDao {
	
	/**
	 * 定制标识码
	 * @param dbCon
	 * @param rid
	 * @return
	 * @throws SQLException
	 */
	static int defineCode(DBCon dbCon, int rid) throws SQLException{
		int code = 0;
		boolean has = true;
		String srid = "{rid}", scode = "{code}";
		String querySQL = "SELECT COUNT(id) FROM weixin_order WHERE restaurant_id = " + srid + " AND code = " + scode;
		ResultSet res = null;
		do{
			has = true;
			code = (int) (Math.random() * 10000);
			if(code > 1000 && code < 9999){
				res = dbCon.stmt.executeQuery(querySQL.replace(srid, rid+"").replace(scode, code+""));
				if(res != null && res.next() && res.getInt(1) == 0){
					has = false;
				}
			}
		}while(has);
		if(res != null){
			res.close();
			res = null;
		}
		return code;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param food
	 * @throws SQLException
	 */
	public static void insertWXOrderFood(DBCon dbCon, WXOrderFood food) throws SQLException{
		String insertSQL = "INSERT INTO weixin_order_food (weixin_order_id, food_id, food_count)"
			+ " VALUES("
			+ food.getOrderId() + ","
			+ food.getFoodId() + ","
			+ food.getFoodCount()
			+ ")";
		dbCon.stmt.executeUpdate(insertSQL);
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param weixinRestaurantSerial
	 * @param weixinMemberSerial
	 * @param order
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static WXOrder insert(DBCon dbCon, String weixinRestaurantSerial, String weixinMemberSerial, WXOrder.InsertBuilder ib)
		throws SQLException, BusinessException{
		WXOrder order = ib.build();
		WXOrderFood tempOrderFood = null;
		
		// TODO 处理账单信息, 新增或合拼
		
		
		int rid = WeixinRestaurantDao.getRestaurantIdByWeixin(weixinRestaurantSerial);
		String insertSQL = "";
		// 生成标识码
		order.setCode(defineCode(dbCon, rid));
		order.setRid(rid);
		order.setMemberSerial(weixinMemberSerial);
		order.setBirthDate(new Date().getTime());
		order.setStatus(WXOrder.Status.NO_USED);
		
		insertSQL = "INSERT INTO `weixin_order`"
			+ " (restaurant_id, weixin_serial, weixin_serial_crc, birth_date, status, code) "
			+ " VALUES("
			+ order.getRid() + ","
			+ "'" + order.getMemberSerial() + "',"
			+ "CRC32('" + order.getMemberSerial() + "'),"
			+ "'" + order.getBirthDateFormat() + "',"
			+ order.getStatus().getValue() + ","
			+ order.getCode()
			+ ")";
		dbCon.stmt.executeUpdate(insertSQL, Statement.RETURN_GENERATED_KEYS);
		ResultSet res = dbCon.stmt.getGeneratedKeys();
		if(res != null && res.next()){
			order.setId(res.getInt(1));
		}
		
		if(order.getFoods() != null){
			for(int i = 0; i < order.getFoods().size(); i++){
				tempOrderFood = order.getFoods().get(i);
				tempOrderFood.setOrderId(order.getId());
				insertWXOrderFood(dbCon, tempOrderFood);
			}
		}
		
		return order;
	}
	
	/**
	 * 
	 * @param rSerial
	 * @param mSerial
	 * @param ib
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static WXOrder insert(String rSerial, String mSerial, WXOrder.InsertBuilder ib) throws SQLException, BusinessException{
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			WXOrder order = insert(dbCon, rSerial, mSerial, ib);;
			dbCon.conn.commit();
			return order;
		}finally{
			if(dbCon != null) dbCon.disconnect();
		}
	}
	
}
