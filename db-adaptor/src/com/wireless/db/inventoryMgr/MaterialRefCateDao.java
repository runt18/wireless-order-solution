package com.wireless.db.inventoryMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.MaterialRefCate;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class MaterialRefCateDao {
	public static List<MaterialRefCate> selectMaterial(Terminal terminal,String limitCondition,String name) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return selectMaterial(dbCon, terminal,limitCondition,name);
		}
		finally{
			dbCon.disconnect();
		}
	}
	private static List<MaterialRefCate> selectMaterial(DBCon dbCon,Terminal terminal,String limitCondition,String name) throws SQLException{
		List<MaterialRefCate> materialRefCates = new ArrayList<MaterialRefCate>();
		String sql = "SELECT (SELECT COUNT(*) FROM wireless_order_db.material A,wireless_order_db.material_cate B WHERE A.cate_id = B.cate_id AND B.restaurant_id = "+terminal.restaurantID+""+(name.equals("")?"":" AND B.name = '"+name+"'")+") AS all_count,A.material_id,A.name,A.price,A.status,A.amount,A.last_mod_date,A.last_mod_staff,B.name AS cate_name FROM wireless_order_db.material A,wireless_order_db.material_cate B WHERE A.cate_id = B.cate_id AND B.restaurant_id = "+terminal.restaurantID+" "+(name.equals("")?"":"AND B.name = '"+name+"'")+" "+limitCondition;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			MaterialRefCate materialRefCate = new MaterialRefCate();
			materialRefCate.setAmount((Float)dbCon.rs.getObject("amount"));
			materialRefCate.setCateName((String)dbCon.rs.getObject("cate_name"));
			materialRefCate.setLastModDate((Date)dbCon.rs.getObject("last_mod_date"));
			materialRefCate.setLastModStaff((String)dbCon.rs.getObject("last_mod_staff"));
			materialRefCate.setMaterialId((Integer)dbCon.rs.getObject("material_id"));
			materialRefCate.setName((String)dbCon.rs.getObject("name"));
			materialRefCate.setPrice((Float)dbCon.rs.getObject("price"));
			materialRefCate.setStatus((Integer)dbCon.rs.getObject("status"));
			materialRefCate.setAllCount((Integer)dbCon.rs.getInt("all_count"));
			materialRefCates.add(materialRefCate);
		}
		return materialRefCates;
	}
}
