package com.wireless.db.system;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.oss.OssImageDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BillBoardError;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.system.BillBoard;
import com.wireless.pojo.system.BillBoard.Status;
import com.wireless.pojo.system.BillBoard.Type;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.StringHtml;

public class BillBoardDao {
	
	public static class ExtraCond{
		private int id;
		private int restaurantId;
		private int systemId;
		private Status status;
		private Type type;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setRestaurant(int restaurantId){
			this.restaurantId = restaurantId;
			return this;
		}
		
		public ExtraCond setRestaurant(Restaurant restaurant){
			this.restaurantId = restaurant.getId();
			return this;
		}
		
		public ExtraCond setStatus(Status status){
			this.status = status;
			return this;
		}
		
		public ExtraCond setSystem(int systemId){
			this.systemId = systemId;
			return this;
		}
		
		public ExtraCond setType(Type type){
			this.type = type;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND billboard_id = " + id);
			}
			if(restaurantId != 0){
				extraCond.append(" AND restaurant_id = " + restaurantId);
			}
			if(status != null){
				extraCond.append(" AND status = " + status.getVal());
			}
			if(type != null){
				extraCond.append(" AND type = " + type.getVal());
			}
			if(systemId != 0){
				extraCond.append(" AND system_id = " + systemId);
			}
			return extraCond.toString();
		}
		
	}
	
	/**
	 * Insert a new bill board to specific builder {@link BillBoard.InsertBuilder}.
	 * @param builder
	 * 			the insert builder {@link BillBoard.InsertBuilder}
	 * @return the id to bill board just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static int insert(BillBoard.InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new bill board to specific builder {@link BillBoard.InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the insert builder {@link BillBoard.InsertBuilder}
	 * @return the id to bill board just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the admin to restaurant does NOT exist
	 */
	public static int insert(DBCon dbCon, BillBoard.InsertBuilder builder) throws SQLException, BusinessException{
		BillBoard billBoard = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".billboard " +
			  "( title, body, created, expired, type, status, system_id, restaurant_id ) VALUES ( " +
			  "'" + billBoard.getTitle() + "'," +
			  "'" + new StringHtml(billBoard.getBody(), StringHtml.ConvertTo.TO_NORMAL) + "'," +
			  "'" + DateUtil.format(billBoard.getCreated(), DateUtil.Pattern.DATE_TIME) + "'," +
			  "'" + DateUtil.format(billBoard.getExpired(), DateUtil.Pattern.DATE_TIME) + "'," +
			  billBoard.getType().getVal() + "," +
			  billBoard.getStatus().getVal() + "," +
			  (billBoard.getType() == BillBoard.Type.SYSTEM_ASSOCIATED ? billBoard.getSystemId() : " NULL ") + ", " +
			  (billBoard.getType() != BillBoard.Type.SYSTEM ? billBoard.getRestaurantId() : " NULL ") +
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		final int id;
		if(dbCon.rs.next()){
			id = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of bill board is not generated successfully.");
		}
		dbCon.rs.close();
		
		//Update the associated oss image to this billboard's body.
		final Staff staff;
		if(billBoard.getType() == BillBoard.Type.RESTAURANT){
			staff = StaffDao.getAdminByRestaurant(dbCon, billBoard.getRestaurantId());
		}else{
			staff = new Staff();
			staff.setRestaurantId(0);
		}
		OssImageDao.update(dbCon, staff, new OssImage.UpdateBuilder4Html(OssImage.Type.BILL_BOARD, id).setHtml(billBoard.getBody()));
		
		//Insert the system billboard to each restaurant. 
		if(billBoard.getType() == BillBoard.Type.SYSTEM){
			billBoard.setId(id);
			for(Restaurant eachRestaurant : RestaurantDao.getByCond(dbCon, null, null)){
				try{
					insert(dbCon, BillBoard.InsertBuilder.build4SystemAssociated(billBoard, eachRestaurant.getId()));
				}catch(BusinessException | SQLException ignored){
					ignored.printStackTrace();
				}
			}
		}
		
		return id;
	}
	
	/**
	 * Get the bill board to specific id.
	 * @param id
	 * 			the bill board id
	 * @return the bill board to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the bill board to this id does NOT exist
	 */
	public static BillBoard getById(int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the bill board to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @return the bill board to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the bill board to this id does NOT exist
	 */
	public static BillBoard getById(DBCon dbCon, int id) throws SQLException, BusinessException{
		List<BillBoard> result = getByCond(dbCon, new ExtraCond().setId(id));
		if(result.isEmpty()){
			throw new BusinessException(BillBoardError.BILL_BOARD_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	public static List<BillBoard> getByCond(ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<BillBoard> getByCond(DBCon dbCon, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".billboard WHERE 1 = 1 " + (extraCond != null ? extraCond.toString() : "");
		final List<BillBoard> result = new ArrayList<BillBoard>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			BillBoard item = new BillBoard(dbCon.rs.getInt("billboard_id"));
			item.setTitle(dbCon.rs.getString("title"));
			item.setBody(new StringHtml(dbCon.rs.getString("body"), StringHtml.ConvertTo.TO_HTML).toString());
			item.setCreated(dbCon.rs.getTimestamp("created").getTime());
			item.setExpired(dbCon.rs.getTimestamp("expired").getTime());
			item.setType(BillBoard.Type.valueOf(dbCon.rs.getInt("type")));
			item.setStatus(BillBoard.Status.valueOf(dbCon.rs.getInt("status")));
			item.setSystemId(dbCon.rs.getInt("system_id"));
			item.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			result.add(item);
		}
		dbCon.rs.close();
		return result;
	}

	/**
	 * Update the bill board to update builder {@link BillBoard.UpdateBuilder}.
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the bill board to update does NOT exist
	 */
	public static void update(BillBoard.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the bill board to update builder {@link BillBoard.UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the bill board to update does NOT exist
	 */
	public static void update(DBCon dbCon, BillBoard.UpdateBuilder builder) throws SQLException, BusinessException{
		BillBoard billBoard = builder.build();
		String sql;
		sql = " UPDATE " + Params.dbName + ".billboard SET " +
			  " billboard_id = " + billBoard.getId() +
			  (builder.isTitleChanged() ? " ,title = '" + billBoard.getTitle() + "'" : "") +
			  (builder.isBodyChanged() ? " ,body = '" + new StringHtml(billBoard.getBody(), StringHtml.ConvertTo.TO_NORMAL) + "'" : "") +
			  (builder.isStatusChanged() ? " ,status = " + billBoard.getStatus().getVal() : "") +
			  (builder.isExpiredChanged() ? " ,expired = '" + DateUtil.format(billBoard.getExpired()) + "'" : "") +
			  " WHERE billboard_id = " + billBoard.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(BillBoardError.BILL_BOARD_NOT_EXIST);
		}
		
		//Update the associated oss image to this billboard's body.
		if(builder.isBodyChanged()){
			final Staff staff;
			if(billBoard.getType() == BillBoard.Type.RESTAURANT){
				staff = StaffDao.getAdminByRestaurant(dbCon, billBoard.getRestaurantId());
			}else{
				staff = new Staff();
				staff.setRestaurantId(0);
			}
			OssImageDao.update(dbCon, staff, new OssImage.UpdateBuilder4Html(OssImage.Type.BILL_BOARD, billBoard.getId()).setHtml(billBoard.getBody()));
		}
		
		billBoard = getById(dbCon, billBoard.getId());
		if(billBoard.getType() == BillBoard.Type.SYSTEM){
			deleteByCond(dbCon, new BillBoardDao.ExtraCond().setSystem(billBoard.getId()));
			for(Restaurant eachRestaurant : RestaurantDao.getByCond(dbCon, null, null)){
				try{
					insert(dbCon, BillBoard.InsertBuilder.build4SystemAssociated(billBoard, eachRestaurant.getId()));
				}catch(BusinessException | SQLException ignored){
					ignored.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Delete the bill board to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param id
	 * 			the bill board id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the bill board to delete does NOT exist.
	 */
	public static void deleteById(int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the bill board to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param id
	 * 			the bill board id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the bill board to delete does NOT exist.
	 */
	public static void deleteById(DBCon dbCon, int id) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, new ExtraCond().setId(id)) == 0){
			throw new BusinessException(BillBoardError.BILL_BOARD_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the bill board to specific extra condition {@link ExtraCond}.
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to bill board to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
 	 * @throws BusinessException 
	 * 			throws if the admin to restaurant belongs to billboard deleted does NOT exist
	 */
	public static int deleteByCond(ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return deleteByCond(dbCon, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the bill board to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to bill board to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the admin to restaurant belongs to billboard deleted does NOT exist
	 */
	public static int deleteByCond(DBCon dbCon, ExtraCond extraCond) throws SQLException, BusinessException{
		int amount = 0;
		for(BillBoard billBoard : getByCond(dbCon, extraCond)){
			String sql = " DELETE FROM " + Params.dbName + ".billboard WHERE billboard_id = " + billBoard.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
			//Delete the associated oss image to this promotion
			final Staff staff;
			if(billBoard.getType() == BillBoard.Type.RESTAURANT){
				staff = StaffDao.getAdminByRestaurant(dbCon, billBoard.getRestaurantId());
			}else{
				staff = new Staff();
				staff.setRestaurantId(0);
			}
			OssImageDao.delete(dbCon, staff, new OssImageDao.ExtraCond().setAssociated(OssImage.Type.BILL_BOARD, billBoard.getId()));
			
			//Delete the associated system billboard
			if(billBoard.getType() == BillBoard.Type.SYSTEM){
				deleteByCond(dbCon, new ExtraCond().setSystem(billBoard.getId()));
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
			return "remove " + amount + " expired bill board(s) takes " + elapsed + " sec.";
		}
	}
	
	public static Result cleanup() throws SQLException{
		long beginTime = System.currentTimeMillis();
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			int amount = 0;
			for(BillBoard billBoard : getByCond(dbCon, null)){
				if(billBoard.isExpired()){
					try{
						deleteById(billBoard.getId());
						amount++;
					}catch(BusinessException ignored){
						ignored.printStackTrace();
					}
				}
			}
			return new Result(amount, (int)(System.currentTimeMillis() - beginTime) / 1000);
		}finally{
			dbCon.disconnect();
		}
	}
}
