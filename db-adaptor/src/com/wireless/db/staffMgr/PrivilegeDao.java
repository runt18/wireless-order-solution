package com.wireless.db.staffMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.pojo.staffMgr.Staff;

public class PrivilegeDao {
	/**
	 * Get the list of Privilege.
	 * @param dbCon
	 * @param staff
	 * @param extraCond 
	 * 			the extra condition for select Privilege
	 * @param otherClause	
	 * 			the condition for select Discount		
	 * @return
	 * @throws SQLException
	 */
	public static List<Privilege> getPrivileges(DBCon dbCon, Staff staff, String extraCond, String otherClause) throws SQLException{
		String sql = "SELECT pri_id, pri_code, cate FROM " + Params.dbName + ".privilege" +
					" WHERE 1=1 " +
					  (extraCond != null ? extraCond : "") +
					" ORDER BY cate ";
					dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<Privilege> list = new ArrayList<Privilege>();
		while(dbCon.rs.next()){
			Privilege p = new Privilege(dbCon.rs.getInt("pri_id"), Code.valueOf(dbCon.rs.getInt("pri_code")), staff.getRestaurantId());
			if(Code.valueOf(dbCon.rs.getInt("pri_code")) == Code.DISCOUNT){
				DBCon priCon = new DBCon();
				try{
					priCon.connect();
					sql = " SELECT discount_id, restaurant_id, name, level, status "	+
							  " FROM " + Params.dbName + ".discount " +
							  " WHERE restaurant_id = " + staff.getRestaurantId() +
							  (otherClause != null ? otherClause : "");
					priCon.rs = priCon.stmt.executeQuery(sql);
						while(priCon.rs.next()){
							Discount discount = new Discount(priCon.rs.getInt("discount_id"));
							discount.setRestaurantId(priCon.rs.getInt("restaurant_id"));
							discount.setName(priCon.rs.getString("name"));
							discount.setLevel(priCon.rs.getShort("level"));
							discount.setStatus(priCon.rs.getInt("status"));
							p.addDiscount(discount);
						}
				}finally{
					priCon.rs.close();
					priCon.disconnect();
				}
			}
			
			list.add(p);
		}
		dbCon.rs.close();
		return list;
	}
	/**
	 * Get the list of Privilege.
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition for select Privilege
	 * @param otherClause
	 * 			the condition for select Discount
	 * @return
	 * @throws SQLException
	 */
	public static List<Privilege> getPrivileges(Staff staff, String extraCond, String otherClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPrivileges(dbCon, staff, extraCond, otherClause);
		}finally{
			dbCon.disconnect();
		}
		
	}
	
}
