package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.protocol.Terminal;

public class MaterialDeptDao {
	/**
	 * Insert a new MaterialDept.
	 * @param term
	 * 			the Terminal
	 * @param mDept
	 * 			the detail of MaterialDept
	 * @return	the id of MaterialDept just create
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static void insertMaterialDept(Terminal term, MaterialDept mDept) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			insertMaterialDept(dbCon, term, mDept);
		}finally{
			dbCon.disconnect();
		}
				
	}
	/**
	 * Insert a new MaterialDept.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the Terminal
	 * @param mDept
	 * 			the detail of MaterialDept
	 * @return	the id of MaterialDept just create
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static void insertMaterialDept(DBCon dbCon, Terminal term, MaterialDept mDept)throws SQLException{
		String sql;
		sql = "INSERT INTO " + Params.dbName + ".material_dept (material_id, dept_id, restaurant_id, stock) " +
				" VALUES(" +
				mDept.getMaterialId() + ", " +
				mDept.getDeptId() + ", " +
				mDept.getRestaurantId() + ", " +
				mDept.getStock() + ")";
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new SQLException("Failed to insert!");
		}
	}
	/**
	 * Update the MaterialDept according to MaterialDept.
	 * @param term
	 * 			the Terminal
	 * @param mDept
	 * 			the MaterialDept to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the MaterialDept is not exist
	 */
	public static void updateMaterialDept(Terminal term, MaterialDept mDept) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			updateMaterialDept(dbCon, term, mDept);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Update the MaterialDept according to MaterialDept.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the Terminal
	 * @param mDept
	 * 			the MaterialDept to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the MaterialDept is not exist
	 */
	public static void updateMaterialDept(DBCon dbCon, Terminal term, MaterialDept mDept) throws SQLException, BusinessException{
		String sql;
		sql = "UPDATE " + Params.dbName + ".material_dept " +
				" SET stock = " + mDept.getStock() + 
				" WHERE material_id = " + mDept.getMaterialId() + 
				" AND dept_id = " + mDept.getDeptId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException("The materialDept is not exist!");
		}
		
	}
	/**
	 * Get the list of MaterialDept according to extra condition and Terminal
	 * @param term
	 * 			the Terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list of MaterialDept 
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<MaterialDept> getMaterialDepts(Terminal term, String extraCond, String orderClause)throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getMaterialDepts(dbCon, term, extraCond, null);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Get the list of MaterialDept according to extra condition and Terminal
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the Terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list of MaterialDept 
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<MaterialDept> getMaterialDepts(DBCon dbCon, Terminal term, String extraCond, String orderClause)throws SQLException{
		List<MaterialDept> mDepts = new ArrayList<MaterialDept>();
		String sql;
		sql = "SELECT material_id, dept_id, restaurant_id, stock " +
				" FROM " + Params.dbName + ".material_dept " +
				" WHERE 1=1 " +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			MaterialDept mDept = new MaterialDept();
			mDept.setMaterialId(dbCon.rs.getInt("material_id"));
			mDept.setDeptId(dbCon.rs.getInt("dept_id"));
			mDept.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			mDept.setStock(dbCon.rs.getFloat("stock"));
			mDepts.add(mDept);
		}
		return mDepts;
	}
	
	
}
