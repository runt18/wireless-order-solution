package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.util.DateUtil;

public class StockInitDao {
	public static class ExtraCond{
		private int materialId;
		private String name;
		private int cateType;
		private int cateId;
		
		
		
		public void setMaterialId(int materialId) {
			this.materialId = materialId;
		}



		public void setName(String name) {
			this.name = name;
		}



		public void setCateType(int cateType) {
			this.cateType = cateType;
		}



		public void setCateId(int cateId) {
			this.cateId = cateId;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(cateType != 0){
				extraCond.append(" AND MC.type = " + cateType);
			}
			if(cateId != 0){
				extraCond.append(" AND MC.cate_id = " + cateId);
			}	
			if(name != null){
				extraCond.append(" AND M.name LIKE '%" + name + "%' ");
			}
			if(materialId != 0){
				extraCond.append(" AND M.material_id = " + materialId);
			}				
			
			return extraCond.toString();
		}
	}
	
	/**
	 * Get a dept's material stock
	 * @param dbCon
	 * @param staff
	 * @param deptId
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 */
	public static List<Material> getMaterialStockByDeptId(DBCon dbCon, Staff staff, int deptId, ExtraCond extraCond, String orderClause) throws SQLException{
		List<Material> materials = new ArrayList<>();
		String sql = "SELECT M.material_id, M.name, M.price, "
					+ " MC.cate_id, MC.name cate_name, MC.type cate_type, "
					+ " CASE "
					+ " WHEN (MD.dept_id != "+ deptId +" OR MD.dept_id is null) THEN 0 " 
					+ " ELSE MD.stock "
					+ "END AS deptOrTotal_stock "
					+ " FROM  material M JOIN material_cate MC ON M.cate_id = MC.cate_id "
					+ " LEFT JOIN wireless_order_db.material_dept MD ON MD.material_id = M.material_id AND MD.dept_id = " + deptId
					+ " WHERE M.restaurant_id = " + staff.getRestaurantId() 
					+ (extraCond != null?extraCond:"");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			Material m = new Material();
			m.setId(dbCon.rs.getInt("material_id"));
			m.setName(dbCon.rs.getString("name"));
			m.setPrice(dbCon.rs.getFloat("price"));
			m.setStock(dbCon.rs.getFloat("deptOrTotal_stock"));
			m.setCate(dbCon.rs.getInt("cate_id"), dbCon.rs.getString("cate_name"), dbCon.rs.getInt("cate_type"));
			if(m.getCate().getType() == MaterialCate.Type.GOOD){
				m.setGood(true);
			}
			materials.add(m);
		}
		dbCon.rs.close();
		
		return materials;
	}
	
	/**
	 * Get a dept's material stock
	 * @param staff
	 * @param deptId
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 */
	public static List<Material> getMaterialStockByDeptId(Staff staff, int deptId, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getMaterialStockByDeptId(dbCon, staff, deptId, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}	
	
	/**
	 * Get every material's stock
	 * @param dbCon
	 * @param staff
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 */
	public static List<Material> getMaterialStock(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		List<Material> materials = new ArrayList<>();
		String sql = "SELECT M.material_id, M.name, M.price, "
					+ "M.stock AS deptOrTotal_stock "
					+ " FROM  material M JOIN material_cate MC ON M.cate_id = MC.cate_id "
					+ " LEFT JOIN wireless_order_db.material_dept MD ON MD.material_id = M.material_id"
					+ " WHERE M.restaurant_id = " + staff.getRestaurantId() 
					+ (extraCond != null?extraCond:"");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			Material m = new Material();
			m.setId(dbCon.rs.getInt("material_id"));
			m.setName(dbCon.rs.getString("name"));
			m.setPrice(dbCon.rs.getFloat("price"));
			m.setStock(dbCon.rs.getFloat("deptOrTotal_stock"));
			materials.add(m);
		}
		dbCon.rs.close();
		
		return materials;
	}	
	
	/**
	 * Get every material's stock
	 * @param staff
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 */
	public static List<Material> getMaterialStock(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getMaterialStock(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}	
	
	
	/**
	 * Initialize the stock detail
	 * @param staff
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 */
	public static void initStock(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			initStock(dbCon, staff);
			dbCon.conn.commit();
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}	
	
	/**
	 * Initialize the stock detail
	 * @param dbCon
	 * @param staff
	 * @param extraCond
	 * @param orderClause
	 * @throws SQLException
	 */
	public static void initStock(DBCon dbCon, Staff staff) throws SQLException{
		String sql;
		//删除部门物料对应数据
		sql = "DELETE FROM " + Params.dbName + ".material_dept WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.stmt.executeUpdate(sql);
		
		//删除库存明细记录
		sql = "DELETE SD FROM " + Params.dbName + ".stock_action_detail SD " + 
				" JOIN " + Params.dbName + ".stock_action S ON S.id = SD.stock_action_id " +
				" WHERE S.restaurant_id = " + staff.getRestaurantId();
		dbCon.stmt.executeUpdate(sql);				
		
		//删除库存记录
		sql = "DELETE FROM " + Params.dbName + ".stock_action WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.stmt.executeUpdate(sql);

		//删除盘点明细记录
		sql = "DELETE SD FROM " + Params.dbName + ".stock_take_detail SD " + 
				" JOIN " + Params.dbName + ".stock_take S ON S.id = SD.stock_take_id " +
				" WHERE S.restaurant_id = " + staff.getRestaurantId();
		dbCon.stmt.executeUpdate(sql);	
		
		//删除盘点记录
		sql = "DELETE FROM " + Params.dbName + ".stock_take WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.stmt.executeUpdate(sql);
		
		//删除月结明细记录
		sql = "DELETE MD FROM " + Params.dbName + ".monthly_balance_detail MD " + 
				" JOIN " + Params.dbName + ".monthly_balance M ON M.id = MD.monthly_balance_id " +
				" WHERE M.restaurant_id = " + staff.getRestaurantId();
		dbCon.stmt.executeUpdate(sql);	
		
		//删除月结记录
		sql = "DELETE FROM " + Params.dbName + ".monthly_balance WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.stmt.executeUpdate(sql);
		
		//把material库存清为0
		sql = "UPDATE " + Params.dbName + ".material M, " + Params.dbName + ".material_cate MC SET " +
				" M.stock = 0 " +
				" WHERE M.cate_id = MC.cate_id AND MC.restaurant_id = " + staff.getRestaurantId();
		dbCon.stmt.executeUpdate(sql);
		
		
		sql = " INSERT INTO " + Params.dbName + ".stock_init " +
				  " ( restaurant_id, init_date ) VALUES( " +
				  staff.getRestaurantId() + "," +
				  "'" + DateUtil.format(System.currentTimeMillis()) + "'" +
				  ")";
			
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
	}
	
	
	public static boolean isInit(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return isInit(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}		
		
	}
	
	public static boolean isInit(DBCon dbCon, Staff staff) throws SQLException{
		int count = 0;
		
		//月结不做计算, 初始化时默认加上月的月结
//		String sql = "SELECT COUNT(*) FROM monthly_balance WHERE restaurant_id = "+ staff.getRestaurantId();
//		
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		
//		if(dbCon.rs.next()){
//			count += dbCon.rs.getInt(1);
//		}
//		dbCon.rs.close();
		//初始化和消耗不算入库存数
		String sql = "SELECT COUNT(*) FROM stock_action WHERE restaurant_id = "+ staff.getRestaurantId() 
				+ " AND sub_type NOT IN("+ StockAction.SubType.USE_UP.getVal() +", "+ StockAction.SubType.INIT.getVal() +")";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		if(dbCon.rs.next()){
			count += dbCon.rs.getInt(1);
		}
		dbCon.rs.close();		
		
		sql = "SELECT COUNT(*) FROM stock_take WHERE restaurant_id = "+ staff.getRestaurantId();
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		if(dbCon.rs.next()){
			count += dbCon.rs.getInt(1);
		}
		dbCon.rs.close();		
		
		if(count == 0){
			return true;
		}else{
			return false;
		}
		
	}
	
	/**
	 * 获取期初建账时间
	 * @param staff
	 * @return
	 * @throws SQLException
	 */
	public static long getInitDate(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getInitDate(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}		
		
	}
	
	/**
	 * 获取期初建账时间
	 * @param dbCon
	 * @param staff
	 * @return
	 * @throws SQLException
	 */
	public static long getInitDate(DBCon dbCon, Staff staff) throws SQLException{
		String sql = "SELECT init_date FROM " + Params.dbName + ".stock_init " +
					" WHERE restaurant_id = " + staff.getRestaurantId() +
					" ORDER BY id DESC";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		long initDate = 0;
		if(dbCon.rs.next()){
			initDate = dbCon.rs.getTimestamp("init_date").getTime();
		}
		
		return initDate;
	}

}
