package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PayTypeError;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.staffMgr.Staff;

public class PayTypeDao {

	public static class ExtraCond{
		private int id;
		private final List<PayType.Type> types = new ArrayList<>();
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond addType(PayType.Type type){
			types.add(type);
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND pay_type_id = " + id);
			}
			
			StringBuilder typeCond = new StringBuilder();
			for(PayType.Type type : types){
				if(typeCond.length() == 0){
					typeCond.append(type.getVal());
				}else{
					typeCond.append("," + type.getVal());
				}
			}
			if(typeCond.length() != 0){
				extraCond.append(" AND type IN(" + typeCond.toString() + ")");
			}
			
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert a new pay type according to builder {@link PayType#InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder
	 * @return the id to pay type just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, PayType.InsertBuilder builder) throws SQLException{
		PayType payType = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".pay_type" +
			  " (restaurant_id, name, type) VALUES( " +
			  staff.getRestaurantId() + "," +
			  "'" + payType.getName() + "'," +
			  payType.getType().getVal() + 
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int id = 0;
		if(dbCon.rs.next()){
			id = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The pay type id is NOT generated successfully.");
		}
		dbCon.rs.close();
		
		return id;
	}
	
	/**
	 * Insert a new pay type according to builder {@link PayType#InsertBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder
	 * @return the id to pay type just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, PayType.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the pay type according to specific builder.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if either cases below
	 * 			<li>the pay type to update does NOT exist
	 * 			<li>the pay type does NOT belong to be normal
	 */
	public static void update(Staff staff, PayType.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the pay type according to specific builder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if either cases below
	 * 			<li>the pay type to update does NOT exist
	 * 			<li>the pay type does NOT belong to be normal
	 */
	public static void update(DBCon dbCon, Staff staff, PayType.UpdateBuilder builder) throws SQLException, BusinessException{
		PayType payType = builder.build();
		
		PayType original = getById(dbCon, staff, payType.getId());
		if(original.getType() != PayType.Type.EXTRA){
			throw new BusinessException("只有【" + PayType.Type.EXTRA.toString() + "】的付款类型才可以修改", PayTypeError.UPDATE_NOT_ALLOW);
		}
		
		String sql;
		sql = " UPDATE " + Params.dbName + ".pay_type SET " +
		      " pay_type_id = " + payType.getId() +
		      (builder.isNameChanged() ? ",name = '" + payType.getName() + "'" : "") +
		      " WHERE pay_type_id = " + payType.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PayTypeError.PAY_TYPE_NOT_EXIST);
		}
	}
	
	/**
	 * Get the pay type to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the pay type id
	 * @return the pay type to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the pay type to this id does NOT exist
	 */
	public static PayType getById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the pay type to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the pay type id
	 * @return the pay type to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the pay type to this id does NOT exist
	 */
	public static PayType getById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		List<PayType> result = getByCond(dbCon, staff, new ExtraCond().setId(id));
		if(result.isEmpty()){
			throw new BusinessException(PayTypeError.PAY_TYPE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the result pay type to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition{@link ExtraCond}
	 * @return the result list to pay type
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PayType> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the result pay type to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition{@link ExtraCond}
	 * @return the result list to pay type
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PayType> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{

		String sql;
		
		sql = " SELECT * FROM " + Params.dbName + ".pay_type" + 
			  " WHERE 1 = 1 " +
			  " AND restaurant_id IN (0, " + staff.getRestaurantId() + ")" +
			  (extraCond != null ? extraCond.toString() : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		final List<PayType> result = new ArrayList<PayType>();
		while(dbCon.rs.next()){
			PayType payType = new PayType(dbCon.rs.getInt("pay_type_id"));
			payType.setName(dbCon.rs.getString("name"));
			payType.setType(PayType.Type.valueOf(dbCon.rs.getInt("type")));
			result.add(payType);
		}
		dbCon.rs.close();
		
		return result;
	}

	/**
	 * Delete the pay type to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the pay type id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement 
	 * @throws BusinessException
	 * 			throws if the pay type to delete does NOT exist
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
	 * Delete the pay type to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the pay type id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement 
	 * @throws BusinessException
	 * 			throws if the pay type to delete does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(id)) == 0){
			throw new BusinessException(PayTypeError.PAY_TYPE_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the pay type according to extra condition {@link ExtraCond}
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount to pay type deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return deleteByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the pay type according to extra condition {@link ExtraCond}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount to pay type deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(PayType type : getByCond(dbCon, staff, extraCond)){
			if(type.getType() == PayType.Type.EXTRA){
				String sql;
				sql = " DELETE FROM " + Params.dbName + ".pay_type WHERE pay_type_id = " + type.getId();
				dbCon.stmt.executeUpdate(sql);
				amount++;
			}
		}
		return amount;
	}
	
}
