package com.wireless.db.distMgr;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.pojo.distMgr.DiscountPojo;
import com.wireless.protocol.Terminal;

public class QueryDiscount {
	
	public static DiscountPojo exec(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = "";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		DiscountPojo result = new DiscountPojo();
		while(dbCon.rs.next()){
			
		}
		return result;
	}
	
}
