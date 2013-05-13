package com.wireless.db.supplierMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.supplierMgr.Supplier;
import com.wireless.pojo.system.Terminal;

public class SupplierDao {
	
	public static int insert(DBCon dbCon, Supplier supplier) throws SQLException,BusinessException{
		int count = 0;
		String sql;
		sql = "INSERT INTO " + Params.dbName + ".supplier " 
				+ " VALUES(" 
				+ supplier.getSupplierid() + ", "
				+ supplier.getRestaurantid() + ", "
				+ "'" + supplier.getName() + "', "
				+ "'" + supplier.getTele() + "', "
				+ "'" + supplier.getAddr() + "'"
				+ ")";
		count = dbCon.stmt.executeUpdate(sql);
		if(count == 0){
			throw new BusinessException("添加失败!");
		}
		
		return count;
		
		
	}
	
	public static int insert(Supplier supplier) throws Exception{
		int count = 0;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			insert(dbCon, supplier);
		}catch(Exception e){
			throw e;
		}
		finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	public static int delete(DBCon dbCon, Supplier supplier) throws SQLException{
		int count = 0;
		String sql = "delete from " + Params.dbName + ".supplier where supplier_id = " + supplier.getSupplierid() + " and restaurant_id = " + supplier.getRestaurantid();
		count = dbCon.stmt.executeUpdate(sql);
		return count;
	}
	
	
	public static int delete(Supplier supplier) throws Exception{
		int count = 0;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			delete(dbCon, supplier);
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
		
	}
	
	public static void update(Terminal term,Supplier supplier) throws SQLException,BusinessException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			update(dbCon, term, supplier);
			
		}finally {
			dbCon.disconnect();
		}
	}
	
	
	public static void update(DBCon dbCon, Terminal term, Supplier supplier) throws SQLException,BusinessException{
		String sql;
		sql = "UPDATE " + Params.dbName + ".supplier" +
			  "SET name = '" + supplier.getName() + "'" +
			  "WHERE restaurant_id = " + term.getRestaurantID() +
			  " AND supplier_id = " + supplier.getSupplierid();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException("供应商信息修改失败!");
		}
				
		
	}
	
	public static List<Supplier> getSuppliers(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getSuppliers(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
		
	}
	
	public static List<Supplier> getSuppliers(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		List<Supplier> suppliers = new ArrayList<Supplier>();
		try{
			dbCon.connect();
			String sql = "SELECT" +
						" supplier_id,restaurant_id,name,tele,addr " +
						" FROM " + Params.dbName + ".supplier " +
						" WHERE restaurant_id = " + term.getRestaurantID() + " " +
						(extraCond == null ? "" : extraCond) +
						(orderClause == null ? "" : orderClause);
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while(dbCon.rs.next()){
				suppliers.add(new Supplier(dbCon.rs.getInt("supplier_id"),
											dbCon.rs.getInt("restaurant_id"),
											dbCon.rs.getString("name"),
											dbCon.rs.getString("tele"),
											dbCon.rs.getString("addr")));
			}
			
			dbCon.rs.close();
			return suppliers;
		}finally{
			dbCon.disconnect();
		}
	}
	

}
