package com.wireless.db.inventoryMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;
public class MaterialCateDao {
	public static boolean insert(Terminal terminal,MaterialCate materialCate) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, terminal, materialCate);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static boolean insert(DBCon dbCon,Terminal terminal,MaterialCate materialCate) throws SQLException{
		String sql = "INSERT INTO "+Params.dbName+".material_cate("+
				"wireless_order_db.material_cate.cate_id,"+
				"wireless_order_db.material_cate.restaurant_id,"+
				"wireless_order_db.material_cate.name,"+
				"wireless_order_db.material_cate.type,"+
				"wireless_order_db.material_cate.parent_id) values ("+
				"0,"+
				((materialCate.getRestaurantId() != null) ?(materialCate.getRestaurantId()) : "0")+","+
				((materialCate.getName() != null) ?("'"+materialCate.getName()+"'") : "(NULL)")+","+
				((materialCate.getType() != null) ?(materialCate.getType()) : "0")+","+
				((materialCate.getParentId() != null) ?(materialCate.getParentId()) : "0")+");"+"";
		return dbCon.stmt.executeUpdate(sql)>0;
	}
	public static boolean update(Terminal terminal,MaterialCate materialCate,String whereCondition) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return update(dbCon, terminal, materialCate, whereCondition);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static boolean update(DBCon dbCon,Terminal terminal,MaterialCate materialCate,String whereCondition) throws SQLException{
		String sql = "UPDATE wireless_order_db.material_cate SET "+
				"wireless_order_db.material_cate.restaurant_id = "+((materialCate.getRestaurantId() != null) ?(materialCate.getRestaurantId()) : "0")+","+
				"wireless_order_db.material_cate.name = "+((materialCate.getName() != null) ?("'"+materialCate.getName()+"'") : "(NULL)")+","+
				"wireless_order_db.material_cate.type = "+((materialCate.getType() != null) ?(materialCate.getType()) : "0")+","+
				"wireless_order_db.material_cate.parent_id = "+((materialCate.getParentId() != null) ?(materialCate.getParentId()) : "0")+" "+whereCondition+";";
		
		return dbCon.stmt.executeUpdate(sql)>0;
	}
	public static List<MaterialCate> select(Terminal terminal,String whereCondition) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return select(dbCon, terminal, whereCondition);
		}
		finally{
			dbCon.disconnect();
		}
		
	} 
	private static List<MaterialCate> select(DBCon dbCon,Terminal terminal,String whereCondition) throws SQLException{
		List<MaterialCate> materialCates = new ArrayList<MaterialCate>();
		String sql = "SELECT * FROM wireless_order_db.material_cate "+whereCondition;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			MaterialCate materialCate = new MaterialCate();
			materialCate.setCateId((Integer)dbCon.rs.getObject("cate_id"));
			materialCate.setRestaurantId((Integer)dbCon.rs.getInt("restaurant_id"));
			materialCate.setName((String)dbCon.rs.getObject("name"));
			materialCate.setType((Integer)dbCon.rs.getObject("type"));
			materialCate.setParentId((Integer)dbCon.rs.getObject("parent_id"));
			materialCates.add(materialCate);
		}
		return materialCates;
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
		String sql = "DELETE FROM wireless_order_db.material_cate "+whereCondition;
		return dbCon.stmt.executeUpdate(sql)>0;
	}
}
