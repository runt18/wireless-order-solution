package com.wireless.db.staffMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.pojo.staffMgr.Staff;

public class PrivilegeDao {
	
	/**
	 * Get the list of Privilege to specific extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond 
	 * 			the extra condition for select privilege
	 * @param otherClause	
	 * 			the order clause	
	 * @return the privilege to specific extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Privilege> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT pri_id, pri_code, cate FROM " + Params.dbName + ".privilege" +
			  " WHERE 1 = 1 " +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<Privilege> result = new ArrayList<Privilege>();
		while(dbCon.rs.next()){
			Privilege p = new Privilege(dbCon.rs.getInt("pri_id"), Code.valueOf(dbCon.rs.getInt("pri_code")), staff.getRestaurantId());
			result.add(p);
		}
		dbCon.rs.close();
		
		for(Privilege p : result){
			if(p.getCode() == Code.DISCOUNT){
				for(Discount each : DiscountDao.getPureAll(dbCon, staff)){
					p.addDiscount(each);
				}
				break;
			}
		}
		
		Collections.sort(result, Privilege.BY_CATE);
		
		return result;
	}
	
	/**
	 * Get the list of Privilege to specific extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond 
	 * 			the extra condition for select privilege
	 * @param otherClause	
	 * 			the order clause	
	 * @return the privilege to specific extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Privilege> getByCond(Staff staff, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
		
	}
	
}
