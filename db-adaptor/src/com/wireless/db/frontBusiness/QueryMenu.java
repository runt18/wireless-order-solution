package com.wireless.db.frontBusiness;

import java.sql.SQLException;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.protocol.Food;
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

	private static Kitchen[] queryKitchens(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		//get all the kitchen information to this restaurant,
		List<Kitchen> kitchens = KitchenDao.getKitchens(dbCon, term, extraCond, orderClause);
		
		return kitchens.toArray(new Kitchen[kitchens.size()]);
		
	}
	
	private static Department[] queryDepartments(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		//get tall the super kitchen information to this restaurant
//		ArrayList<Department> departments = new ArrayList<Department>();
//		String sql = " SELECT dept_id, name, restaurant_id, type FROM " + Params.dbName + ".department DEPT " +
//					 " WHERE 1 = 1 " +
//					 (extraCond != null ? extraCond : "") + " " +
//					 (orderClause != null ? orderClause : "");
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		while(dbCon.rs.next()){
//			departments.add(new Department(dbCon.rs.getString("name"),
//									   	   dbCon.rs.getShort("dept_id"),
//									   	   dbCon.rs.getInt("restaurant_id"),
//									   	   Department.Type.valueOf(dbCon.rs.getShort("type"))));
//			
//		}
//		dbCon.rs.close();
		
		List<Department> departments = DepartmentDao.getDepartments(dbCon, term, extraCond, orderClause);
		
		return departments.toArray(new Department[departments.size()]);
	}
	
	/**
	 * Query the specific taste information.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon 
	 * 			the database connection
	 * @param mRestaurantID 
	 * 			the restaurant id
	 * @param category 
	 * 			the category of taste to query, one the values below.<br>
	 * 	 	    - Taste.CATE_TASTE
	 *          - Taste.CATE_STYLE
	 *          - Taste.CATE_SPEC
	 * @return the taste information
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement
	 */
	private static Taste[] queryTastes(DBCon dbCon, Terminal term, Taste.Category category, String extraCond, String orderClause) throws SQLException{

		List<Taste> tastes = TasteDao.getTasteByCategory(dbCon, term, category);
		
		return tastes.toArray(new Taste[tastes.size()]);
	}
	
	/**
	 * Get the discount and corresponding plan detail, along with the kitchen details.
	 * Note that the database should be connected before connected.
	 * @param dbCon
	 * 			The database connection.
	 * @param extraCond
	 * 			The extra condition.
	 * @param orderClause
	 * 			The order clause.
	 * @return The array holding the discount info matching the condition. 
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	private static Discount[] queryDiscounts(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
//		String sql;
//		sql = " SELECT " +
//			  " DIST.discount_id, DIST.restaurant_id, DIST.name AS dist_name, DIST.level, DIST.status AS dist_status, " +
//			  " DIST_PLAN.dist_plan_id, DIST_PLAN.kitchen_id, DIST_PLAN.rate, " +
//			  " KITCHEN.name AS kitchen_name, KITCHEN.kitchen_alias, " +
//			  " CASE WHEN DIST_PLAN.discount_id IS NULL THEN '0' ELSE '1' END AS has_plan " +
//			  " FROM " + 
//			  Params.dbName + ".discount DIST " +
//			  " LEFT JOIN " +
//			  Params.dbName + ".discount_plan DIST_PLAN " +
//			  " ON DIST_PLAN.discount_id = DIST.discount_id " +
//			  " LEFT JOIN " +
//			  Params.dbName + ".kitchen KITCHEN " +
//			  " ON DIST_PLAN.kitchen_id = KITCHEN.kitchen_id " +
//			  " WHERE 1=1 " +
//			  (extraCond == null ? "" : extraCond) + " " +
//			  (orderClause == null ? "" : orderClause);
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		
//		LinkedHashMap<PDiscount, List<PDiscountPlan>> discounts = new LinkedHashMap<PDiscount, List<PDiscountPlan>>();
//		
//		while(dbCon.rs.next()){
//			PDiscount discount = new PDiscount(dbCon.rs.getInt("discount_id"));
//			discount.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
//			discount.setName(dbCon.rs.getString("dist_name"));
//			discount.setLevel(dbCon.rs.getShort("level"));
//			discount.setStatus(dbCon.rs.getInt("dist_status"));
//
//			Kitchen kitchen = new Kitchen();
//			kitchen.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
//			kitchen.setId(dbCon.rs.getInt("kitchen_id"));
//			kitchen.setAliasId(dbCon.rs.getShort("kitchen_alias"));
//			kitchen.setName(dbCon.rs.getString("kitchen_name"));
//			
//			List<PDiscountPlan> plans = discounts.get(discount);
//			if(plans == null){				
//				plans = new LinkedList<PDiscountPlan>();
//			}
//			
//			float rate = dbCon.rs.getFloat("rate");
//			if(dbCon.rs.getBoolean("has_plan") && rate != 1){
//				plans.add(new PDiscountPlan(kitchen, rate));
//			}
//			discounts.put(discount, plans);
//		}
//		
//		for(Map.Entry<PDiscount, List<PDiscountPlan>> entry : discounts.entrySet()){
//			entry.getKey().setPlans(entry.getValue().toArray(new PDiscountPlan[entry.getValue().size()]));
//		}
//		
//		return discounts.keySet().toArray(new PDiscount[discounts.size()]);		
		List<Discount> discounts = DiscountDao.getDiscount(dbCon, term, extraCond, orderClause);
		return discounts.toArray(new Discount[discounts.size()]);
	}
	
	/**
	 * Get the cancel reason according to specific condition and order clause
	 * Note that the database should be connected before connected.
	 * @param dbCon
	 * 			The database connection.
	 * @param extraCond
	 * 			The extra condition.
	 * @param orderClause
	 * 			The order clause.
	 * @return The array holding the cancel reasons. 
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	private static CancelReason[] queryCancelReasons(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		List<CancelReason> result = CancelReasonDao.getReasons(dbCon, term, extraCond, orderClause);
		return result.toArray(new CancelReason[result.size()]);
	}

	private static Food[] queryFoods(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		List<Food> result = FoodDao.getFoods(dbCon, term, extraCond, orderClause);
		return result.toArray(new Food[result.size()]);
	}
}
