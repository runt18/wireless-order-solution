package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StockError;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.protocol.Terminal;
import com.wireless.util.PinyinUtil;

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
	 * @param materialDept
	 * 			the detail of MaterialDept
	 * @return	the id of MaterialDept just create
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static void insertMaterialDept(DBCon dbCon, Terminal term, MaterialDept materialDept)throws SQLException{
		String sql;
		sql = "INSERT INTO " + Params.dbName + ".material_dept (material_id, dept_id, restaurant_id, stock) " +
				" VALUES(" +
				materialDept.getMaterialId() + ", " +
				materialDept.getDeptId() + ", " +
				materialDept.getRestaurantId() + ", " +
				materialDept.getStock() + ")";
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
	 * @param materialDept
	 * 			the MaterialDept to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			if the MaterialDept is not exist
	 */
	public static void updateMaterialDept(DBCon dbCon, Terminal term, MaterialDept materialDept) throws SQLException, BusinessException{
		String sql;
		sql = "UPDATE " + Params.dbName + ".material_dept " +
				" SET stock = " + materialDept.getStock() + 
				" WHERE material_id = " + materialDept.getMaterialId() + 
				" AND dept_id = " + materialDept.getDeptId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(StockError.MATERIAL_DEPT_UPDATE);
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
	 * @throws BusinessException 
	 * 			if the material_id is not exist
	 */
	public static List<MaterialDept> getMaterialDepts(Terminal term, String extraCond, String orderClause)throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getMaterialDepts(dbCon, term, extraCond, orderClause);
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
	 * @throws BusinessException 
	 * 			if the material_id is not exist
	 */
	public static List<MaterialDept> getMaterialDepts(DBCon dbCon, Terminal term, String extraCond, String orderClause)throws SQLException, BusinessException{
		List<MaterialDept> mDepts = new ArrayList<MaterialDept>();
		String sql;
		sql = "SELECT MD.material_id, MD.dept_id, MD.restaurant_id, MD.stock, M.price, M.name, M.stock as m_stock, D.name as d_name" +
				" FROM (" + Params.dbName + ".material_dept as MD INNER JOIN " + Params.dbName + ".material as M ON MD.material_id = M.material_id )" +
				" INNER JOIN " + Params.dbName + ".department as D ON MD.dept_id = D.dept_id AND MD.restaurant_id = D.restaurant_id " + 
				" WHERE MD.restaurant_id = " + term.restaurantID +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			MaterialDept mDept = new MaterialDept();
			
			mDept.setMaterialId(dbCon.rs.getInt("material_id"));
			mDept.getMaterial().setName(dbCon.rs.getString("name"));
			mDept.getMaterial().setPrice(dbCon.rs.getFloat("price"));
			mDept.getMaterial().setStock(dbCon.rs.getFloat("m_stock"));
			mDept.getMaterial().setPinyin(PinyinUtil.cn2Spell(dbCon.rs.getString("name")));
			
			mDept.setDeptId(dbCon.rs.getInt("dept_id"));
			mDept.getDept().setName(dbCon.rs.getString("d_name"));
			
			mDept.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			mDept.setStock(dbCon.rs.getFloat("stock"));
			
			mDepts.add(mDept);
		}
		return mDepts;
	}
	
	
	public static List<MaterialDept> getMaterialDeptState(Terminal term, String extraCond, String orderClause)throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getMaterialDeptState(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	public static List<MaterialDept> getMaterialDeptState(DBCon dbCon, Terminal term, String extraCond, String orderClause)throws SQLException, BusinessException{
		List<MaterialDept> mDepts = new ArrayList<MaterialDept>();
		String sql;
		sql = "SELECT MD.material_id, MD.dept_id, MD.restaurant_id, MD.stock, M.price, M.name, M.stock as m_stock, D.name as d_name" +
				" FROM ((" + Params.dbName + ".material_dept as MD INNER JOIN " + Params.dbName + ".material as M ON MD.material_id = M.material_id )" +
				" INNER JOIN " + Params.dbName + ".material_cate as MC ON M.cate_id = MC.cate_id) " + 
				" INNER JOIN " + Params.dbName + ".department as D ON MD.dept_id = D.dept_id AND MD.restaurant_id = D.restaurant_id " +
				" WHERE MD.restaurant_id = " + term.restaurantID +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			MaterialDept mDept = new MaterialDept();
			
			mDept.setMaterialId(dbCon.rs.getInt("material_id"));
			mDept.getMaterial().setName(dbCon.rs.getString("name"));
			mDept.getMaterial().setPrice(dbCon.rs.getFloat("price"));
			mDept.getMaterial().setStock(dbCon.rs.getFloat("m_stock"));
			mDept.getMaterial().setPinyin(PinyinUtil.cn2Spell(dbCon.rs.getString("name")));
			
			mDept.setDeptId(dbCon.rs.getInt("dept_id"));
			mDept.getDept().setName(dbCon.rs.getString("d_name"));
			
			mDept.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			mDept.setStock(dbCon.rs.getFloat("stock"));
			
			mDepts.add(mDept);
		}
		return mDepts;
		
	}
	
	
}
