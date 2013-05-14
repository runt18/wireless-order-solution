package com.wireless.db.supplierMgr;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.supplierMgr.Supplier;
import com.wireless.protocol.Terminal;

public class SupplierDao {
	
	/**
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
	public static int insert(DBCon dbCon, Supplier supplier) throws SQLException,BusinessException{
		String sql;
		sql = "INSERT INTO " + Params.dbName + ".supplier " 
				+ " (restaurant_id, name, tele, addr, contact, comment)"
				+ " VALUES(" 
				+ supplier.getRestaurantId() + ", "
				+ "'" + supplier.getName() + "', "
				+ "'" + supplier.getTele() + "', "
				+ "'" + supplier.getAddr() + "', "
				+ "'" + supplier.getContact() + "', "
				+ "'" + supplier.getComment() + "'"
				+ ")";
		dbCon.stmt.executeUpdate(sql,Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new BusinessException("添加失败,用户已存在!");
		}
		
		
	}
	/**
	 * insert a new supplier to supplier table
	 * @param supplier
	 * 			the supplier to insert
	 * @return the supplier_id if insert successfully
	 * @throws Exception
	 */
	public static int insert(Supplier supplier) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, supplier);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * the method to delete supplier
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @return  the amount of tables to delete
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement
	 */
	public static int delete(DBCon dbCon, String extraCond) throws SQLException{
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".supplier " +
			  " WHERE 1=1 " +
			  (extraCond != null ? extraCond : "");
		return dbCon.stmt.executeUpdate(sql);
	}
	/**
	 * delete supplier according to extra condition  
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return	the amount of tables to delete if successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement
	 */
	public static int delete(DBCon dbCon, Terminal term, String extraCond) throws SQLException{
		return delete(dbCon, " AND restaurant_id = " + term.restaurantID + " " + (extraCond != null ? extraCond : ""));
	}
	/**
	 * delete supplier according to extra condition
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @return	the amount of tables to delete if successfully
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 */
	public static int delete(Terminal term, String extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return delete(dbCon, term, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * delete supplier by id 
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
	public static void deleteById(DBCon dbCon, Terminal term, int supplierId) throws SQLException,BusinessException{
		if(delete(dbCon, " AND restaurant_id = " + term.restaurantID + " AND supplier_id = " + supplierId) == 0){
			throw new BusinessException("此餐桌不存在!");
		}
	}
	/**
	 *delete supplier by id 
	 * @param term	
	 * 			the terminal
	 * @param supplierId
	 * 			the id of supplier to delete 
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 * @throws BusinessException
	 * 			if the supplier to delete does not exist 
	 */
	public static void deleteById(Terminal term, int supplierId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, term, supplierId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * update supplier according to supplier and terminal 
	 * @param term
	 * 			the terminal
	 * @param supplier
	 * 			the supplier to update 
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 * @throws BusinessException
	 * 			if the table to update does not exist
	 */
	public static void update(Terminal term,Supplier supplier) throws SQLException,BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			update(dbCon, term, supplier);
			
		}finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * update supplier according to supplier
	 * @param dbCon
	 * 			the database connection 
	 * @param term
	 * 			the terminal
	 * @param supplier
	 * 			the supplier to update 
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 * @throws BusinessException
	 * 			if the table to update does not exist 
	 */
	public static void update(DBCon dbCon, Terminal term, Supplier supplier) throws SQLException,BusinessException{
		String sql;
		sql = "UPDATE " + Params.dbName + ".supplier SET" +
			  " name = '" + supplier.getName() + "'" +
			  " WHERE restaurant_id = " + term.restaurantID +
			  " AND supplier_id = " + supplier.getSupplierId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException("供应商信息修改失败!");
		}
				
		
	}
	/**
	 * select suppliers according to terminal and extra condition
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
	public static List<Supplier> getSuppliers(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getSuppliers(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
		
	}
	/**
	 * get the suppliers according to terminal and extra condition
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return	the the list holding the table result 
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 */
	public static List<Supplier> getSuppliers(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		List<Supplier> suppliers = new ArrayList<Supplier>();
		try{
			dbCon.connect();
			String sql = "SELECT" +
						" supplier_id,restaurant_id,name,tele,addr,contact,comment " +
						" FROM " + Params.dbName + ".supplier " +
						" WHERE restaurant_id = " + term.restaurantID + " " +
						(extraCond == null ? "" : extraCond) +
						(orderClause == null ? "" : orderClause);
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while(dbCon.rs.next()){
				suppliers.add(new Supplier(dbCon.rs.getInt("supplier_id"),
											dbCon.rs.getInt("restaurant_id"),
											dbCon.rs.getString("name"),
											dbCon.rs.getString("tele"),
											dbCon.rs.getString("addr"),
											dbCon.rs.getString("contact"),
											dbCon.rs.getString("comment")));
			}
			
			dbCon.rs.close();
			return suppliers;
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * get the supplier according to supplier_id
	 * @param term
	 * 			the terminal
	 * @param supplierId
	 * 			the supplier_id to query
	 * @return	the supplier's detail if successfully
	 * @throws BusinessException
	 * 			if the supplier to find does not exist
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 */
	public static Supplier getSupplierById(Terminal term, int supplierId) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getSupplierById(dbCon, term, supplierId);
		}finally{
			dbCon.disconnect();
		}
		
	}
	/**
	 * get the supplier according to supplier_id
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
	public static Supplier getSupplierById(DBCon dbCon, Terminal term, int supplierid) throws BusinessException, SQLException{
		List<Supplier> result = getSuppliers(dbCon, term, " and supplier_id = " + supplierid, null);
		if(result.isEmpty()){
			throw new BusinessException("没有这个供应商!");
		}else{
			return result.get(0);
		}
	}
	

}
