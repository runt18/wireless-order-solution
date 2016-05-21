package com.wireless.db.weixin.order;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.member.MemberDao;
import com.wireless.db.member.TakeoutAddressDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.menuMgr.FoodUnitDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WxOrderError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.TakeoutAddress;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.weixin.order.WxOrder;

public class WxOrderDao {
	
	public static class ExtraCond{
		private int id;
		private int code;
		private int orderId;
		private final List<WxOrder.Status> status = new ArrayList<WxOrder.Status>();
		private WxOrder.Type type;
		private String weixinSerial;
		private int memberId;
		private int tableId;
		
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
		
		public ExtraCond addStatus(WxOrder.Status status){
			this.status.add(status);
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
		
		public ExtraCond setMember(Member member){
			this.memberId = member.getId();
			return this;
		}
		
		public ExtraCond setTableId(int tableId){
			this.tableId = tableId;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND WO.wx_order_id = " + id);
			}
			if(code != 0){
				extraCond.append(" AND WO.code = " + code);
			}
			if(orderId != 0){
				extraCond.append(" AND WO.order_id = " + orderId);
			}
			if(weixinSerial != null){
				String sql = " SELECT member_id FROM " + Params.dbName + ".weixin_member WHERE weixin_serial = '" + weixinSerial + "' AND weixin_serial_crc = CRC32('" + weixinSerial + "')";
				extraCond.append(" AND WO.member_id IN (" + sql + ")");
			}
			if(type != null){
				extraCond.append(" AND WO.type = " + type.getVal());
			}
			if(memberId != 0){
				extraCond.append(" AND WO.member_id = " + memberId);
			}
			final StringBuilder statusCond = new StringBuilder();
			for(WxOrder.Status s : status){
				if(statusCond.length() == 0){
					statusCond.append(s.getVal());
				}else{
					statusCond.append("," + s.getVal());
				}
			}
			if(statusCond.length() != 0){
				extraCond.append(" AND WO.status IN ( " + statusCond.toString() + ")");
			}
			
			if(tableId != 0){
				extraCond.append(" AND WO.table_id = " + tableId);
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
	 * @throws BusinessException 
	 * 			throws if the member to this weixin serial does NOT exist
	 */
	public static int insert(Staff staff, WxOrder.InsertBuilder4Inside builder) throws SQLException, BusinessException{
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
	 * @throws BusinessException 
	 * 			throws if the member to this weixin serial does NOT exist
	 */
	public static int insert(DBCon dbCon, Staff staff, WxOrder.InsertBuilder4Inside builder) throws SQLException, BusinessException{
		
		WxOrder wxOrder = builder.build();
		
		List<Member> members = MemberDao.getByCond(dbCon, staff, new MemberDao.ExtraCond().setWeixinSerial(builder.getWxSerial()), null);
		if(members.isEmpty()){
			throw new BusinessException("没找到相应的会员信息", WxOrderError.WX_INSERT_ORDER_NOT_ALLOW);
		}else{
			if(members.get(0).isRaw()){
				throw new BusinessException("会员必须绑定手机或实体卡才能下单", WxOrderError.WX_INSERT_ORDER_NOT_ALLOW);
			}
			wxOrder.setMember(members.get(0));
		}
		
		//Make the previous inside committed orders invalid.
		for(WxOrder order : getByCond(dbCon, staff, new ExtraCond().setMember(wxOrder.getMember()).setType(WxOrder.Type.INSIDE).addStatus(WxOrder.Status.COMMITTED), null)){
			try{
				update(dbCon, staff, new WxOrder.UpdateBuilder(order.getId()).setStatus(WxOrder.Status.INVALID));
			}catch(BusinessException ignored){
				ignored.printStackTrace();
			}
		}

		//Insert the new inside order.
		return insert(dbCon, staff, wxOrder);
	}
	
	/**
	 * Insert the new weixin order for take out according to builder {@link WxOrder#InsertBuilder4Takeout}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder {@link WxOrder#InsertBuilder4Takeout}
	 * @return the id to weixin order just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the take-out address to this order does NOT exist
	 */
	public static int insert(Staff staff, WxOrder.InsertBuilder4Takeout builder) throws SQLException, BusinessException{
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
	 * Insert the new weixin order for take out according to builder {@link WxOrder#InsertBuilder4Takeout}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder {@link WxOrder#InsertBuilder4Takeout}
	 * @return the id to weixin order just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the take-out address to this order does NOT exist
	 */
	public static int insert(DBCon dbCon, Staff staff, WxOrder.InsertBuilder4Takeout builder) throws SQLException, BusinessException{
		WxOrder wxOrder = builder.build();
		
		Member member = MemberDao.getByWxSerial(dbCon, staff, builder.getWxSerial());
		if(TakeoutAddressDao.getByCond(dbCon, staff, new TakeoutAddressDao.ExtraCond().setMember(member).setId(wxOrder.getTakeoutAddress().getId())).isEmpty()){
			throw new BusinessException("外卖地址信息不属于此会员", WxOrderError.WX_TAKE_OUT_ORDER_NOT_ALLOW);
		}
		
		wxOrder.setTakoutAddress(TakeoutAddressDao.getById(dbCon, staff, wxOrder.getTakeoutAddress().getId()));
		
		int id = insert(dbCon, staff, wxOrder);
		String sql;
		sql = " UPDATE " + Params.dbName + ".weixin_order SET " +
			  " wx_order_id = " + id +
			  " ,address_id = " + wxOrder.getTakeoutAddress().getId() +
			  " ,address = '" + wxOrder.getTakeoutAddress().getAddress() + "'" +
			  " WHERE wx_order_id = " + id;
		dbCon.stmt.executeUpdate(sql);
		
		//Update the last used to take-out address
		sql = " UPDATE " + Params.dbName + ".take_out_address SET " +
			  " last_used = NOW() " +
			  " WHERE id = " + wxOrder.getTakeoutAddress().getId();
		dbCon.stmt.executeUpdate(sql);
		
		return id;
	}
	
	private static int insert(DBCon dbCon, Staff staff, WxOrder wxOrder) throws SQLException, BusinessException{
		
		String sql;
		
//		//Check to see whether the member to this weixin serial exist.
//		final List<Member> result = MemberDao.getByCond(dbCon, staff, new MemberDao.ExtraCond().setWeixinSerial(wxOrder.getWeixinSerial()), null);
//		if(result.isEmpty()){
//			throw new BusinessException("微信序列号对应的会员不存在", MemberError.MEMBER_NOT_EXIST);
//		}else{
//			wxOrder.setMember(result.get(0));
//		}
		
		//Generate the operation code.
		sql = " SELECT IFNULL(MAX(code) + 1, 100) FROM " + Params.dbName + ".weixin_order WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int code = 0;
		if(dbCon.rs.next()){
			code = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//Insert the weixin order.
		sql = " INSERT INTO " + Params.dbName + ".`weixin_order` " +
			  " (restaurant_id, table_id, order_id, member_id, birth_date, status, type, code, comment) " +
			  " VALUES( " +
			  staff.getRestaurantId() + ","	+
			  (wxOrder.hasTable() ? wxOrder.getTable().getId() : " NULL ") + "," +
			  (wxOrder.getOrderId() != 0 ? wxOrder.getOrderId() : " NULL ") + "," +
			  wxOrder.getMember().getId() + "," +
			  " NOW(), " +
			  wxOrder.getStatus().getVal() + "," +
			  wxOrder.getType().getVal() + "," +
			  code + "," +
			  (wxOrder.hasComment() ? "'" + wxOrder.getComment() + "'" : " NULL ") +
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
				  " (wx_order_id, food_id, food_count, food_unit_id) VALUES( " +
				  wxOrderId + "," +
				  of.getFoodId() + "," +
				  of.getCount() + "," +
				  (of.hasFoodUnit() ? of.getFoodUnit().getId() : "NULL") + 
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}
		
		return wxOrderId;
	}

	/**
	 * Update the wx order according to specific builder {@link WxOrder#UpdateBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder {@link WxOrder#UpdateBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the wx order to update does NOT exist
	 */
	public static void update(Staff staff, WxOrder.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
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
			  (builder.isTableChanged() ? " ,table_id = " + wxOrder.getTable().getId() : "") +
			  " WHERE wx_order_id = " + wxOrder.getId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(WxOrderError.WX_ORDER_NOT_EXIST);
		}
		
		if(wxOrder.getStatus() == WxOrder.Status.ORDER_ATTACHED){
			//Count the wx order amount after order attached.
			Member member = MemberDao.getById(dbCon, staff, getById(dbCon, staff, wxOrder.getId()).getMember().getId());
			MemberDao.update(dbCon, staff, new Member.UpdateBuilder(member.getId()).setWxOrderAmount(member.getWxOrderAmount() + 1));
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
		List<WxOrder> result = getByCond(dbCon, staff, new ExtraCond().setCode(code), null);
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
		List<WxOrder> result = getByCond(dbCon, staff, new ExtraCond().setId(id), null);
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
			if(dbCon.rs.getInt("food_unit_id") != 0){
				of.setFoodUnit(FoodUnitDao.getById(staff, dbCon.rs.getInt("food_unit_id")));
			}
			wxOrder.addFood(of);
		}
		dbCon.rs.close();
		wxOrder.setMember(MemberDao.getById(dbCon, staff, wxOrder.getMember().getId()));
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
	public static List<WxOrder> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
		
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
	public static List<WxOrder> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT WO.*, R.restaurant_name FROM " + Params.dbName + ".weixin_order WO " +
			  " JOIN " + Params.dbName + ".restaurant R ON WO.restaurant_id = R.id " +
			  " WHERE 1 = 1 " +
			  " AND WO.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : " ") +
			  (orderClause != null ? orderClause : " ORDER BY birth_date DESC ");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<WxOrder> result = new ArrayList<WxOrder>();
		while(dbCon.rs.next()){
			WxOrder wxOrder = new WxOrder(dbCon.rs.getInt("wx_order_id"));
			wxOrder.setRestaurant(dbCon.rs.getInt("restaurant_id"));
			wxOrder.setRestaurantName(dbCon.rs.getString("restaurant_name"));
			wxOrder.setStatus(WxOrder.Status.valueOf(dbCon.rs.getInt("status")));
			wxOrder.setType(WxOrder.Type.valueOf(dbCon.rs.getInt("type")));
			wxOrder.setComment(dbCon.rs.getString("comment"));
			if(wxOrder.getType() == WxOrder.Type.TAKE_OUT){
				TakeoutAddress address = new TakeoutAddress(dbCon.rs.getInt("address_id"));
				address.setAddress(dbCon.rs.getString("address"));
				wxOrder.setTakoutAddress(address);
			}
			wxOrder.setCode(dbCon.rs.getInt("code"));
			wxOrder.setOrderId(dbCon.rs.getInt("order_id"));
			wxOrder.setBirthDate(dbCon.rs.getTimestamp("birth_date").getTime());
			wxOrder.setMember(new Member(dbCon.rs.getInt("member_id")));
			if(dbCon.rs.getInt("table_id") != 0){
				wxOrder.setTable(new Table(dbCon.rs.getInt("table_id")));
			}
			result.add(wxOrder);
		}
		dbCon.rs.close();
		
		for(WxOrder wxOrder : result){
			if(wxOrder.hasTable()){
				try{
					wxOrder.setTable(TableDao.getById(dbCon, staff, wxOrder.getTable().getId()));
				}catch(BusinessException ignored){
					ignored.printStackTrace();
				}
			}
		}
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
	 * @throws BusinessException 
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		int amount = 0;
		for(WxOrder wxOrder : getByCond(dbCon, staff, extraCond, null)){
			if(OrderDao.getByCond(dbCon, staff, new OrderDao.ExtraCond(DateType.TODAY).setOrderId(wxOrder.getOrderId()), null).isEmpty()){
				String sql;
				//Delete the wx order.
				sql = " DELETE FROM " + Params.dbName + ".weixin_order WHERE wx_order_id = " + wxOrder.getId();
				dbCon.stmt.executeUpdate(sql);
				//Delete the associated order food.
				sql = " DELETE FROM " + Params.dbName + ".weixin_order_food WHERE wx_order_id = " + wxOrder.getId();
				dbCon.stmt.executeUpdate(sql);
				
				amount++;
			}
		}
		return amount;
	}
	
	public static class Result{
		public final int amount;
		private final int elapsed;
		Result(int amount, int elapsed){
			this.amount = amount;
			this.elapsed = elapsed;
		}
		@Override
		public String toString(){
			return "remove " + amount + " wx order(s) takes " + elapsed + " sec.";
		}
	}
	
	public static Result cleanup() throws SQLException, BusinessException{
		long beginTime = System.currentTimeMillis();
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			int amount = 0;
			//Delete all the invalid and committed wx order.
			for(Restaurant restaurant : RestaurantDao.getByCond(null, null)){
				amount += deleteByCond(dbCon, StaffDao.getAdminByRestaurant(dbCon, restaurant.getId()), null);
			}
			return new Result(amount, (int)(System.currentTimeMillis() - beginTime) / 1000);
		}finally{
			dbCon.disconnect();
		}
	}
}
