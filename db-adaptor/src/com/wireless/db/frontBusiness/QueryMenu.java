package com.wireless.db.frontBusiness;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.tasteMgr.TasteCategoryDao;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.FoodMenu;
import com.wireless.pojo.staffMgr.Staff;

public class QueryMenu {

	/**
	 * Get the food menu according to the specific restaurant.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the food menu
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static FoodMenu exec(Staff staff) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the food menu according to the specific restaurant.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			The staff to perform this action
	 * @return the food menu
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static FoodMenu exec(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		return new FoodMenu(FoodDao.getFoods(dbCon, staff, null, null),
						    TasteCategoryDao.get(dbCon, staff),
							TasteDao.getTastes(dbCon, staff, null, null),
							KitchenDao.getNormalKitchens(dbCon, staff),
			    			DepartmentDao.getNormalDepartments(dbCon, staff),
			    			DiscountDao.getDiscount(dbCon, staff, null, null),
			    			CancelReasonDao.getReasons(dbCon, staff, null, null));
	}
	
}
