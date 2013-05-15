package com.wireless.db.frontBusiness;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Terminal;

public class QueryMenu {

	/**
	 * Get the food menu according to the specific restaurant.
	 * @param mRestaurantID
	 * 			The restaurant id.
	 * @return the food menu
	 * @throws SQLException
	 * 			Throws if fail to execute any SQL statement.
	 */
	public static FoodMenu exec(Terminal term) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the food menu according to the specific restaurant.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			The database connection.
	 * @param term
	 * 			The terminal to query.
	 * @return the food menu
	 * @throws SQLException
	 * 			Throws if fail to execute any SQL statement.
	 */
	public static FoodMenu exec(DBCon dbCon, Terminal term) throws SQLException{
		return new FoodMenu(FoodDao.getFoods(dbCon, term, null, null), 
							TasteDao.getTasteByCategory(dbCon, term, Taste.Category.TASTE),
							TasteDao.getTasteByCategory(dbCon, term, Taste.Category.STYLE),
							TasteDao.getTasteByCategory(dbCon, term, Taste.Category.SPEC),
							KitchenDao.getKitchens(dbCon, term, " AND KITCHEN.type = " + Kitchen.Type.NORMAL.getVal(), null),
			    			DepartmentDao.getDepartments(dbCon, term, " AND DEPT.type = " + Department.Type.NORMAL.getVal(), null),
			    			DiscountDao.getDiscount(dbCon, term, null, null),
			    			CancelReasonDao.getReasons(dbCon, term, null, null));
	}
	
	/**
	 * Get the food menu including three information below.<br>
	 * - Food<br>
	 * - Taste<br>
	 * - Kitchen
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @return the food menu holding all the information
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attache to any restaurant.<br>
	 * 							 - The terminal is expired.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static FoodMenu exec(long pin, short model) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			return exec(dbCon, VerifyPin.exec(dbCon, pin, model));
			
		}finally{
			dbCon.disconnect();
		}
	}

}
