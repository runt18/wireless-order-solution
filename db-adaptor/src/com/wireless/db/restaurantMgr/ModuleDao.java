package com.wireless.db.restaurantMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.restaurantMgr.Module;

public class ModuleDao {

	public static List<Module> getAll() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getAll(dbCon);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<Module> getAll(DBCon dbCon) throws SQLException{
		List<Module> result = new ArrayList<Module>();
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".module";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			result.add(new Module(dbCon.rs.getInt("module_id"),
								  Module.Code.valueOf(dbCon.rs.getInt("code"))));
		}
		dbCon.rs.close();
		
		return result;
	}
	
}
