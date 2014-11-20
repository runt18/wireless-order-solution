package com.wireless.db.weixin.order;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WxOrderError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;

public class WxOrderDao {
	
	public static class ExtraCond{
		private int id;
		private int code;
		private int orderId;
		private WxOrder.Status status;
		private WxOrder.Type type;
		private String weixinSerial;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setCode(int code){
			this.code = code;
			return this;
		}
		
		public ExtraCond setOrder(int orderId){
			this.orderId = orderId;
			return this;
		}
		
		public ExtraCond setOrder(Order order){
			this.orderId = order.getId();
			return this;
		}
		
		public ExtraCond setStatus(WxOrder.Status status){
			this.status = status;
			return this;
		}
		
		public ExtraCond setType(WxOrder.Type type){
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
			if(id != 0){
				extraCond.append(" AND wx_order_id = " + id);
			}
			if(code != 0){
				extraCond.append(" AND code = " + code);
			}
			if(orderId != 0){
				extraCond.append(" AND order_id = " + orderId);
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
	 * Insert the new weixin order for inside according to builder {@link WxOrder#InsertBuilder4Inside}
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder {@link WxOrder#InsertBuilder4Inside}
	 * @return the id to weixin order just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, WxOrder.InsertBuilder4Inside builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int wxOrderId = insert(dbCon, staff, builder);
			dbCon.conn.commit();
			return wxOrderId;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Insert the new weixin order for inside according to builder {@link WxOrder#InsertBuilder4Inside}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder {@link WxOrder#InsertBuilder4Inside}
	 * @return the id to weixin order just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, WxOrder.InsertBuilder4Inside builder) throws SQLException{
		
		WxOrder wxOrder = builder.build();
		
		//Make the previous inside committed orders invalid.
		for(WxOrder order : getByCond(dbCon, staff, new ExtraCond().setWeixin(wxOrder.getWeixinSerial()).setType(WxOrder.Type.INSIDE).setStatus(WxOrder.Status.COMMITTED))){
			try{
				update(dbCon, staff, new WxOrder.UpdateBuilder(order.getId()).setStatus(WxOrder.Status.INVALID));
			}catch(BusinessException ignored){
				ignored.printStackTrace();
			}
		}

		//Insert the new inside order.
		return insert(dbCon, staff, wxOrder);
	}
	
	private static int insert(DBCon dbCon, Staff staff, WxOrder wxOrder) throws SQLException{
		
		String sql;
		
		//Generate the operation code.
		sql = " SELECT IFNULL(MAX(code) + 1, 1) FROM " + Params.dbName + ".weixin_order WHERE restaurant_id = " + staff.getRestaurantId();
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
			  " NOW(), " +
			  wxOrder.getStatus().getVal() + "," +
			  wxOrder.getType().getVal() + "," +
			  code +
			  " ) ";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int wxOrderId;
		if(dbCon.rs.next()){
			wxOrderId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id to wx order is NOT generated successfully.");
		}
		dbCon.rs.close();
		
		//Insert the associated order foods.
		for(OrderFood of : wxOrder.getFoods()){
			sql = " INSERT INTO " + Params.dbName + ".weixin_order_food " +
				  " (wx_order_id, food_id, food_count) VALUES( " +
				  wxOrderId + "," +
				  of.getFoodId() + "," +
				  of.getCount() + 
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}
		
		return wxOrderId;
	}
	
	/**
	 * Update the wx order according to specific builder {@link WxOrder#UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder {@link WxOrder#UpdateBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the wx order to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, WxOrder.UpdateBuilder builder) throws SQLException, BusinessException{
		
		WxOrder wxOrder = builder.build();
		
		String sql;
		sql = " UPDATE " + Params.dbName + ".weixin_order SET " +
			  " wx_order_id = " + wxOrder.getId() +
			  (builder.isStatusChanged() ? " ,status = " + wxOrder.getStatus().getVal() : "") +
			  (builder.isOrderChanged() ? " ,order_id = " + wxOrder.getOrderId() : "") +
			  " WHERE wx_order_id = " + wxOrder.getId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(WxOrderError.WX_ORDER_NOT_EXIST);
		}
	}
	
	/**
	 * Get the wx order to specific wx code.
	 * @param staff
	 * 			the staff to perform this action
	 * @param code
	 * 			the code to wx order
	 * @return the wx order to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the wx order to this id does NOT exist
	 * 			<li>any food belongs to this wx order does NOT exist
	 */
	public static WxOrder getByCode(Staff staff, int code) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCode(dbCon, staff, code);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the wx order to specific wx code.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param code
	 * 			the code to wx order
	 * @return the wx order to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the wx order to this id does NOT exist
	 * 			<li>any food belongs to this wx order does NOT exist
	 */
	public static WxOrder getByCode(DBCon dbCon, Staff staff, int code) throws SQLException, BusinessException{
		List<WxOrder> result = getByCond(dbCon, staff, new ExtraCond().setCode(code));
		if(result.isEmpty()){
			throw new BusinessException(WxOrderError.WX_ORDER_NOT_EXIST);
		}else{
			WxOrder wxOrder = result.get(0);
			fillDetail(dbCon, staff, wxOrder);
			return wxOrder;
		}
	}
	
	/**
	 * Get the wx order to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to wx order
	 * @return the wx order to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the wx order to this id does NOT exist
	 * 			<li>any food belongs to this wx order does NOT exist
	 */
	public static WxOrder getById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the wx order to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to wx order
	 * @return the wx order to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the wx order to this id does NOT exist
	 * 			<li>any food belongs to this wx order does NOT exist
	 */
	public static WxOrder getById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		List<WxOrder> result = getByCond(dbCon, staff, new ExtraCond().setId(id));
		if(result.isEmpty()){
			throw new BusinessException(WxOrderError.WX_ORDER_NOT_EXIST);
		}else{
			WxOrder wxOrder = result.get(0);
			fillDetail(dbCon, staff, wxOrder);
			return wxOrder;
		}
	}
	
	private static void fillDetail(DBCon dbCon, Staff staff, WxOrder wxOrder) throws SQLException, BusinessException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".weixin_order_food WHERE wx_order_id = " + wxOrder.getId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			OrderFood of = new OrderFood();
			of.asFood().copyFrom(FoodDao.getById(staff, dbCon.rs.getInt("food_id")));
			of.setCount(dbCon.rs.getFloat("food_count"));
			wxOrder.addFood(of);
		}
		dbCon.rs.close();
		wxOrder.setMember(MemberDao.getByWxSerial(dbCon, staff, wxOrder.getWeixinSerial()));
	}
	
	/**
	 * Get the weixin order according to specific extra condition{@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result to wx order
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<WxOrder> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".weixin_order " +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<WxOrder> result = new ArrayList<WxOrder>();
		while(dbCon.rs.next()){
			WxOrder wxOrder = new WxOrder(dbCon.rs.getInt("wx_order_id"));
			wxOrder.setRestaurant(dbCon.rs.getInt("restaurant_id"));
			wxOrder.setStatus(WxOrder.Status.valueOf(dbCon.rs.getInt("status")));
			wxOrder.setType(WxOrder.Type.valueOf(dbCon.rs.getInt("type")));
			wxOrder.setCode(dbCon.rs.getInt("code"));
			wxOrder.setOrderId(dbCon.rs.getInt("order_id"));
			wxOrder.setBirthDate(dbCon.rs.getTimestamp("birth_date").getTime());
			wxOrder.setWeixinSerial(dbCon.rs.getString("weixin_serial"));
			result.add(wxOrder);
		}
		dbCon.rs.close();
		
		return result;
	}

	/**
	 * Delete the wx order to specific id. 
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the wx order id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL 
	 * @throws BusinessException
	 */
	public static void deleteById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the wx order to specific id. 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the wx order id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL 
	 * @throws BusinessException
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(id)) == 0){
			throw new BusinessException(WxOrderError.WX_ORDER_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the wx order to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to wx order deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(WxOrder wxOrder : getByCond(dbCon, staff, extraCond)){
			String sql;
			//Delete the wx order.
			sql = " DELETE FROM " + Params.dbName + ".weixin_order WHERE wx_order_id = " + wxOrder.getId();
			dbCon.stmt.executeUpdate(sql);
			//Delete the associated order food.
			sql = " DELETE FROM " + Params.dbName + ".weixin_order_food WHERE wx_order_id = " + wxOrder.getId();
			dbCon.stmt.executeUpdate(sql);
			
			amount++;
		}
		return amount;
	}
}
