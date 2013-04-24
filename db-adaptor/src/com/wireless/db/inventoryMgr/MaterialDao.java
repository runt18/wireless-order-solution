package com.wireless.db.inventoryMgr;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.protocol.Terminal;

public class MaterialDao {
	public static boolean insert(Terminal terminal,Material material) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, terminal, material);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static boolean insert(DBCon dbCon,Terminal terminal,Material material) throws SQLException{
		String sql = "INSERT INTO "+Params.dbName+".material("+
				"wireless_order_db.material.material_id,"+
				"wireless_order_db.material.cate_id,"+
				"wireless_order_db.material.amount,"+
				"wireless_order_db.material.price,"+
				"wireless_order_db.material.name,"+
				"wireless_order_db.material.status,"+
				"wireless_order_db.material.last_mod_staff,"+
				"wireless_order_db.material.last_mod_date) values ("+
				"0,"+
				((material.getCateId() != null) ?(material.getCateId()) : "0")+","+
				((material.getAmount() != null) ?(material.getAmount()) : "0.0")+","+
				((material.getPrice() != null) ?(material.getPrice()) : "0.0")+","+
				((material.getName() != null) ?("'"+material.getName()+"'") : "(NULL)")+","+
				((material.getStatus() != null) ?(material.getStatus()) : "0")+","+
				((material.getLastModStaff() != null) ?("'"+material.getLastModStaff()+"'") : "(NULL)")+","+
				((material.getLastModDate() != null) ?("'"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(material.getLastModDate())+"'") : "(NULL)")+");"+"";
		return dbCon.stmt.executeUpdate(sql)>0;
	}
	public static List<Material> select(Terminal terminal,String whereCondition) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return select(dbCon, terminal, whereCondition);
		}
		finally{	
			dbCon.disconnect();
		}
	}
	private static List<Material> select(DBCon dbCon,Terminal terminal,String whereCondition) throws SQLException{
		List<Material> materials = new ArrayList<Material>();
		String sql = "SELECT * FROM wireless_order_db.material "+whereCondition;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Material material = new Material();
			material.setMaterialId((Integer)dbCon.rs.getObject("material_id"));
			material.setCateId((Integer)dbCon.rs.getObject("cate_id"));
			material.setAmount((Float)dbCon.rs.getObject("amount"));
			material.setPrice((Float)dbCon.rs.getObject("price"));
			material.setName((String)dbCon.rs.getObject("name"));
			material.setStatus((Integer)dbCon.rs.getObject("status"));
			material.setLastModStaff((String)dbCon.rs.getObject("last_mod_staff"));
			material.setLastModDate((Date)dbCon.rs.getObject("last_mod_date"));
			materials.add(material);
		}
		return materials;
	}
	public static boolean delete(Terminal terminal,String whereCondition) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return delete(dbCon, terminal, whereCondition);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static boolean delete(DBCon dbCon,Terminal terminal,String whereCondition) throws SQLException{
		String sql = "DELETE FROM wireless_order_db.material "+whereCondition;
		return dbCon.stmt.executeUpdate(sql)>0;
	}
	public static boolean update(Terminal terminal,Material material,String whereCondition) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return update(dbCon, terminal, material,whereCondition);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static boolean update(DBCon dbCon,Terminal terminal,Material material,String whereCondition) throws SQLException{
		String sql = "UPDATE wireless_order_db.material SET "+
				"wireless_order_db.material.cate_id = "+((material.getCateId() != null) ?(material.getCateId()) : "0")+","+
				"wireless_order_db.material.amount = "+((material.getAmount() != null) ?(material.getAmount()) : "0.0")+","+
				"wireless_order_db.material.price = "+((material.getPrice() != null) ?(material.getPrice()) : "0.0")+","+
				"wireless_order_db.material.name = "+((material.getName() != null) ?("'"+material.getName()+"'") : "(NULL)")+","+
				"wireless_order_db.material.status = "+((material.getStatus() != null) ?(material.getStatus()) : "0")+","+
				"wireless_order_db.material.last_mod_staff = "+((material.getLastModStaff() != null) ?("'"+material.getLastModStaff()+"'") : "(NULL)")+","+
				"wireless_order_db.material.last_mod_date = "+((material.getLastModDate() != null) ?("'"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(material.getLastModDate())+"'") : "(NULL)")+" "+whereCondition+";";
		return dbCon.stmt.executeUpdate(sql)>0;
	}
}
