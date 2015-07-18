package com.wireless.db.staffMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.pojo.staffMgr.Privilege4Price;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;

public class PrivilegeDao {
	
	public static class ExtraCond{
		private Role role;
		
		public ExtraCond setRole(Role role){
			this.role = role;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(role != null){
				String sql = " SELECT pri_id FROM " + Params.dbName + ".role_privilege WHERE role_id = " + role.getId();
				extraCond.append(" AND pri_id IN (" + sql + ")");
			}
			return extraCond.toString();
		}
	}
	
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
	public static List<Privilege> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT pri_id, pri_code FROM " + Params.dbName + ".privilege" +
			  " WHERE 1 = 1 " +
			  (extraCond != null ? extraCond : " ");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		final List<Privilege> result = new ArrayList<Privilege>();
		while(dbCon.rs.next()){
			final Privilege privilege;
			if(dbCon.rs.getInt("pri_code") == Code.PRICE_PLAN.getVal()){
				privilege = new Privilege4Price(dbCon.rs.getInt("pri_id"));
				
			}else{
				privilege = new Privilege(dbCon.rs.getInt("pri_id"),
			  							  Code.valueOf(dbCon.rs.getInt("pri_code")));
			}			
			privilege.setRestaurantId(staff.getRestaurantId());
			result.add(privilege);
		}
		dbCon.rs.close();
		
		for(Privilege p : result){
			if(p.getCode() == Code.DISCOUNT){
				if(extraCond != null && extraCond.role != null){
					for(Discount d : DiscountDao.getByRole(dbCon, staff, extraCond.role)){
						p.addDiscount(d);
					}
				}else{
					for(Discount each : DiscountDao.getPureAll(dbCon, staff)){
						p.addDiscount(each);
					}
				}
				
			}else if(p.getCode() == Code.PRICE_PLAN){
				if(extraCond != null && extraCond.role != null){
					for(PricePlan pricePlan : PricePlanDao.getByCond(dbCon, staff, new PricePlanDao.ExtraCond().setRole(extraCond.role))){
						((Privilege4Price)p).addPricePlan(pricePlan);
					}
				}else{
					for(PricePlan pricePlan : PricePlanDao.getByCond(dbCon, staff, null)){
						((Privilege4Price)p).addPricePlan(pricePlan);
					}
				}
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
	public static List<Privilege> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
		
	}
	
}
