package com.wireless.db.staffMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.pojo.staffMgr.Staff;

public class PrivilegeDao {
	/**
	 * Get the list of Privilege.
	 * @param dbCon
	 * @param staff
	 * @return
	 * @throws SQLException
	 */
	public static List<Privilege> getPrivileges(DBCon dbCon, Staff staff) throws SQLException{
		String sql = "SELECT pri_id, pri_code, cate FROM " + Params.dbName + ".privilege";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<Privilege> list = new ArrayList<Privilege>();
		while(dbCon.rs.next()){
			Privilege p = new Privilege(dbCon.rs.getInt("pri_id"), Code.valueOf(dbCon.rs.getInt("pri_code")), staff.getRestaurantId());
			list.add(p);
		}
		dbCon.rs.close();
		return list;
	}
	/**
	 * Get the list of Privilege.
	 * @param staff
	 * 			the terminal
	 * @return
	 * @throws SQLException
	 */
	public static List<Privilege> getPrivileges(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPrivileges(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
		
	}
	
}
