package com.wireless.db.weixin.order;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.weixin.order.WXOrder;

public class WXOrderDao {
	
	public static class ExtraCond{
		private int code;
		private WXOrder.Status status;
		private WXOrder.Type type;
		private String weixinSerial;
		
		public ExtraCond setCode(int code){
			this.code = code;
			return this;
		}
		
		public ExtraCond setStatus(WXOrder.Status status){
			this.status = status;
			return this;
		}
		
		public ExtraCond setType(WXOrder.Type type){
			this.type = type;
			return this;
		}
		
		public ExtraCond setWeixin(String weixinSerial){
			this.weixinSerial = weixinSerial;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(code != 0){
				extraCond.append(" AND code = " + code);
			}
			if(weixinSerial != null){
				extraCond.append(" AND weixin_serial = '" + weixinSerial + "' AND weixin_serial_crc = CRC32('" + weixinSerial + "')");
			}
			if(type != null){
				extraCond.append(" AND type = " + type.getVal());
			}
			if(status != null){
				extraCond.append(" AND status = " + status.getVal());
			}
			return extraCond.toString();
		}
	}

	/**
	 * Insert the new weixin order for inside according to builder {@link WXOrder#InsertBuilder4Inside}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder {@link WXOrder#InsertBuilder4Inside}
	 * @return the id to weixin order just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, WXOrder.InsertBuilder4Inside builder) throws SQLException{
		
		WXOrder wxOrder = builder.build();
		
		//Make the previous inside committed orders invalid.
		for(WXOrder order : getByCond(dbCon, staff, new ExtraCond().setWeixin(wxOrder.getWeixinSerial()).setType(WXOrder.Type.INSIDE).setStatus(WXOrder.Status.COMMITTED))){
			update(dbCon, staff, new WXOrder.UpdateBuilder(order.getId()).setStatus(WXOrder.Status.INVALID));
		}

		//Insert the new inside order.
		return insert(dbCon, staff, wxOrder);
	}
	
	private static int insert(DBCon dbCon, Staff staff, WXOrder wxOrder) throws SQLException{
		
		String sql;
		
		//Generate the operation code.
		sql = " SELECT MAX(code) + 1 FROM " + Params.dbName + ".weixin_order WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int code = 0;
		if(dbCon.rs.next()){
			code = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//Insert the weixin order.
		sql = " INSERT INTO " + Params.dbName + ".`weixin_order` " +
			  " (restaurant_id, weixin_serial, weixin_serial_crc, birth_date, status, type, code) " +
			  " VALUES( " +
			  staff.getRestaurantId() + ","	+
			  "'" + wxOrder.getWeixinSerial() + "'," +
			  "CRC32('" + wxOrder.getWeixinSerial() + "'),"	+
			  "'" + DateUtil.format(wxOrder.getBirthDate()) + "'," +
			  wxOrder.getStatus().getVal() + "," +
			  wxOrder.getType().getVal() + "," +
			  code +
			  " ) ";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int orderId;
		if(dbCon.rs.next()){
			orderId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id to wx order is NOT generated successfully.");
		}
		dbCon.rs.close();
		
		//Insert the associated order foods.
		for(OrderFood of : wxOrder.getFoods()){
			sql = " INSERT INTO " + Params.dbName + ".wx_order_food " +
				  " (wx_order_id, food_id, food_account) VALUES( " +
				  orderId + "," +
				  of.getFoodId() + "," +
				  of.getCount() + 
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}
		
		return orderId;
	}
	
	static void update(DBCon dbCon, Staff staff, WXOrder.UpdateBuilder builder) throws SQLException{
		
		WXOrder wxOrder = builder.build();
		
		String sql;
		sql = " UPDATE " + Params.dbName + ".wx_order SET " +
			  " id = " + wxOrder.getId() +
			  (builder.isStatusChanged() ? " ,status = " + wxOrder.getStatus().getVal() : "") +
			  " WHERE id = " + wxOrder.getId();
		
		dbCon.stmt.executeUpdate(sql);
	}
	
	public static List<WXOrder> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".wx_order " +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<WXOrder> result = new ArrayList<WXOrder>();
		while(dbCon.rs.next()){
			WXOrder wxOrder = new WXOrder(dbCon.rs.getInt("id"));
			wxOrder.setRestaurant(dbCon.rs.getInt("restaurant_id"));
			wxOrder.setStatus(WXOrder.Status.valueOf(dbCon.rs.getInt("status")));
			wxOrder.setType(WXOrder.Type.valueOf(dbCon.rs.getInt("type")));
			wxOrder.setCode(dbCon.rs.getInt("code"));
			wxOrder.setBirthDate(dbCon.rs.getLong("birth_date"));
			wxOrder.setWeixinSerial(dbCon.rs.getString("weixin_serial"));
			result.add(wxOrder);
		}
		dbCon.rs.close();
		return result;
	}
	
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(WXOrder wxOrder : getByCond(dbCon, staff, extraCond)){
			String sql;
			//Delete the wx order.
			sql = " DELETE FROM " + Params.dbName + ".wx_order WHERE id = " + wxOrder.getId();
			dbCon.stmt.executeUpdate(sql);
			//Delete the associated order food.
			sql = " DELETE FROM " + Params.dbName + ".wx_order_food WHERE wx_order_id = " + wxOrder.getId();
			dbCon.stmt.executeUpdate(sql);
			
			amount++;
		}
		return amount;
	}
}
