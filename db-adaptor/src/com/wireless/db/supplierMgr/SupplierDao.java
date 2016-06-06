package com.wireless.db.supplierMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.SupplierError;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.supplierMgr.Supplier;

public class SupplierDao {
	
	public static class ExtraCond{
		private Integer start;
		private Integer limit;
		private String name;
		private String telePhone;
		private String contact;
		private Integer supplierId;
		
		public ExtraCond setId(int supplierId){
			this.supplierId = supplierId;
			return this;
		}
		
		public ExtraCond setName(String name){
			this.name = name;
			return this;
		}
		
		public ExtraCond setTelePhone(String telePhone){
			this.telePhone = telePhone;
			return this;
		}
		
		public ExtraCond setContact(String contact){
			this.contact = contact;
			return this;
		}
		
		public ExtraCond setLimit(int start, int limit){
			this.start = start;
			this.limit = limit;
			return this;
		}
		
		@Override
		public String toString() {
			final StringBuilder extraCond = new StringBuilder();
			
			if(contact != null){
				extraCond.append(" AND contact LIKE '%" + contact + "%' ");
			}
			
			if(name != null){
				extraCond.append(" AND name LIKE '%" + name + "%' ");
			}
			
			if(telePhone != null){
				extraCond.append(" AND tele LIKE '%" + telePhone + "%' ");
			}
			
			if(supplierId != null){
				extraCond.append(" AND supplier_id = " + supplierId);
			}
			return extraCond.toString();
		}
		
	}
	
	
	/**
	 * Insert a new supplier to supplier table.
	 * @param dbCon
	 * 			the database connection
	 * @param supplier
	 * 			the supplier to insert
	 * @return the supplier_id of supplier just create 
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the supplier_id of supplier has exist
	 */
	public static int insert(DBCon dbCon, Supplier.InsertBuilder builder) throws SQLException,BusinessException{
		String sql;
		sql = "INSERT INTO " + Params.dbName + ".supplier " 
				+ " (restaurant_id, name, tele, addr, contact, comment)"
				+ " VALUES(" 
				+ builder.getRestaurantId() + ", "
				+ "'" + builder.getName() + "', "
				+ "'" + builder.getTele() + "', "
				+ "'" + builder.getAddr() + "', "
				+ "'" + builder.getContact() + "', "
				+ "'" + builder.getComment() + "'"
				+ ")";
		dbCon.stmt.executeUpdate(sql,Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new BusinessException(SupplierError.SUPPLIER_IS_EXIST);
		}
		
		
	}
	
	
	/**
	 * Insert a new supplier to supplier table.
	 * @param supplier
	 * 			the supplier to insert
	 * @return the supplier_id if insert successfully
	 * @throws Exception
	 * 			if failed to insert
	 */
	public static int insert(Supplier.InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * The method to delete supplier.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @return  the amount of tables to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".supplier " +
			  " WHERE 1=1 " +
			  " AND restaurant_id = " + staff.getId() +
			  (extraCond != null ? extraCond.toString() : "");
		return dbCon.stmt.executeUpdate(sql);
	}
	
	
	/**
	 * Delete supplier according to extra condition.
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return	the amount of tables to delete if successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
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
	 * Delete supplier by id. 
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param supplierId
	 * 			the supplier_id of supplier
	 * @throws SQLException
	 *			if failed to execute any SQL Statement  			
	 * @throws BusinessException
	 * 			if the supplier to delete does not exist
	 */
	public static int deleteById(DBCon dbCon, Staff staff, int supplierId) throws SQLException,BusinessException{
		return deleteByCond(dbCon, staff, new ExtraCond().setId(supplierId));
	}
	
	
	/**
	 * Delete supplier by id. 
	 * @param term	
	 * 			the terminal
	 * @param supplierId
	 * 			the id of supplier to delete 
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 * @throws BusinessException
	 * 			if the supplier to delete does not exist 
	 */
	public static void deleteById(Staff staff, int supplierId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, staff, supplierId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Update supplier according to supplier and terminal. 
	 * @param term
	 * 			the terminal
	 * @param supplier
	 * 			the supplier to update 
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 * @throws BusinessException
	 * 			if the table to update does not exist
	 */
	public static void update(Staff staff,Supplier.UpdateBuilder builder) throws SQLException,BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			update(dbCon, staff, builder);
			
		}finally {
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Update supplier according to supplier.
	 * @param dbCon
	 * 			the database connection 
	 * @param staff
	 * 			the terminal
	 * @param supplier
	 * 			the supplier to update 
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 * @throws BusinessException
	 * 			if the table to update does not exist 
	 */
	public static void update(DBCon dbCon, Staff staff, Supplier.UpdateBuilder builder) throws SQLException,BusinessException{
		String sql;
		sql = " UPDATE " + Params.dbName + ".supplier SET" +
			  " name = '" + builder.getName() + "', " +
			  " tele = '" + builder.getTele() + "', " +
			  " addr = '" + builder.getAddr() + "', " +
			  " contact = '" + builder.getContact() + "', " +
			  " comment = '" + builder.getComment() + "' " +
			  " WHERE restaurant_id = " + staff.getRestaurantId() +
			  " AND supplier_id = " + builder.getId();
			  
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(SupplierError.SUPPLIER_UPDATE);
		}
				
		
	}
	
	
	/**
	 * Select suppliers according to terminal and extra condition.
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return	return the list holding the table result if successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 */
	public static List<Supplier> getByCond(Staff term, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
		
	}
	
	
	/**
	 * Get the suppliers according to terminal and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return	the the list holding the table result 
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 */
	public static List<Supplier> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		final List<Supplier> suppliers = new ArrayList<Supplier>();
		String sql = "SELECT" +
					" supplier_id,restaurant_id,name,tele,addr,contact,comment " +
					" FROM " + Params.dbName + ".supplier " +
					" WHERE restaurant_id = " + staff.getRestaurantId() + " " +
					(extraCond != null ? extraCond.toString() : "") +
					(orderClause == null ? "" : orderClause) + 
					((extraCond != null && (extraCond.start != null && extraCond.limit != null)) ? (" LIMIT " + extraCond.start + "," + extraCond.limit) : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			final Supplier supplier = new Supplier(dbCon.rs.getInt("supplier_id"));
			supplier.setRestaurantid(dbCon.rs.getInt("restaurant_id"));
			supplier.setName(dbCon.rs.getString("name"));
			supplier.setTele(dbCon.rs.getString("tele"));
			supplier.setAddr(dbCon.rs.getString("addr"));
			supplier.setContact(dbCon.rs.getString("contact"));
			supplier.setComment(dbCon.rs.getString("comment"));
			suppliers.add(supplier);
		}
		
		dbCon.rs.close();
		return suppliers;
	}
	
	
	/**
	 * Get the supplier according to supplier_id.
	 * @param staff
	 * 			the terminal
	 * @param supplierId
	 * 			the supplier_id to query
	 * @return	the supplier's detail if successfully
	 * @throws BusinessException
	 * 			if the supplier to find does not exist
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 */
	public static Supplier getById(Staff staff, int supplierId) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, supplierId);
		}finally{
			dbCon.disconnect();
		}
		
	}
	
	
	/**
	 * Get the supplier according to supplier_id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param supplierid
	 * 			the supplier_id to query
	 * @return	the supplier's detail
	 * @throws BusinessException
	 * 			if the supplier to find does not exist
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement
	 */
	private static Supplier getById(DBCon dbCon, Staff term, int supplierid) throws BusinessException, SQLException{
		List<Supplier> result = getByCond(dbCon, term, new ExtraCond().setId(supplierid), "");
		if(result.isEmpty()){
			throw new BusinessException(SupplierError.SUPPLIER_SELECT);
		}else{
			return result.get(0);
		}
	}
	
	
	/**
	 * Get the amount of supplier according to extra condition.
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return	the amount of supplier
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int getSupplierCount(Staff term, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getSupplierCount(dbCon, term, extraCond, orderClause);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Get the amount of supplier according to extra condition.
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return	the amount of supplier
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static int getSupplierCount(DBCon dbCon, Staff term, ExtraCond extraCond, String orderClause) throws SQLException{
			String sql = "SELECT COUNT(*) "+
						" FROM " + Params.dbName + ".supplier" +
						" WHERE restaurant_id = " + term.getRestaurantId() + " " +
						(extraCond != null ? extraCond.toString() : "")+ 
						(orderClause != null ? orderClause : "") + 
						((extraCond != null && (extraCond.start != null && extraCond.limit != null)) ? (" LIMIT " + extraCond.start + "," + extraCond.limit) : "");
						
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				return dbCon.rs.getInt(1);
			}else{
				return 0;
			}
	}
	

}
