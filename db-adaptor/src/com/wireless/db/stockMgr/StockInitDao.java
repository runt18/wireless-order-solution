package com.wireless.db.stockMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.staffMgr.Staff;

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
	
	
	public static List<Material> getMaterialStockByDeptId(DBCon dbCon, Staff staff, int deptId, ExtraCond extraCond, String orderClause) throws SQLException{
		List<Material> materials = new ArrayList<>();
		String sql = "SELECT M.material_id, M.name, M.price, "
					+ "CASE "
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
			materials.add(m);
		}
		dbCon.rs.close();
		
		return materials;
	}
	
	public static List<Material> getMaterialStockByDeptId(Staff staff, int deptId, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getMaterialStockByDeptId(dbCon, staff, deptId, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}	
	
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
	
	public static List<Material> getMaterialStock(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getMaterialStock(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}	
	

}
