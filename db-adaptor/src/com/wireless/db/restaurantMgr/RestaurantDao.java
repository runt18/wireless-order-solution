 package com.wireless.db.restaurantMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.RestaurantError;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.ppMgr.PricePlan;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.DateUtil;

public class RestaurantDao {
	
	/**
	 * Query a restaurant according to specified id.
	 * @param restaurantId
	 * 			the id to restaurant to query
	 * @return the query restaurant result
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 				if the restaurant to query does NOT exist
	 */
	public static Restaurant getById(int restaurantId) throws SQLException, BusinessException{

		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, restaurantId);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Query a restaurant according to specified id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the query restaurant result 
	 * @throws SQLException
	 * 				if failed to execute any SQL statements
	 * @throws BusinessException
	 * 				if the restaurant to query does NOT exist
	 */
	public static Restaurant getById(DBCon dbCon, int restaurantId) throws SQLException, BusinessException{
		List<Restaurant> result = getByCond(dbCon, " AND id = " + restaurantId, null);
		if(result.isEmpty()){
			throw new BusinessException(RestaurantError.RESTAURANT_NOT_FOUND);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the restaurant according to account
	 * @param account
	 * 			the account to restaurant
	 * @return the restaurant associated with the account
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the restaurant to query does NOT exist
	 */
	public static Restaurant getByAccount(String account) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByAccount(dbCon, account);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the restaurant according to account
	 * @param dbCon
	 * 			the database connection
	 * @param account
	 * 			the account to restaurant
	 * @return the restaurant associated with the account
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the restaurant associated with this account does NOT exist
	 */
	public static Restaurant getByAccount(DBCon dbCon, String account) throws SQLException, BusinessException{
		List<Restaurant> result = getByCond(dbCon, " AND account = '" + account + "'", null);
		if(result.isEmpty()){
			throw new BusinessException(RestaurantError.RESTAURANT_NOT_FOUND);
		}else{
			return result.get(0);
		}
	}
	
	public static List<Restaurant> getByCond(String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	private static List<Restaurant> getByCond(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		
		List<Restaurant> result = new ArrayList<Restaurant>();
		
		String sql = " SELECT * FROM " + Params.dbName + ".restaurant " +
				 	 " WHERE 1 = 1 " +
				 	 " AND id > " + Restaurant.RESERVED_7 + " " +
				 	 (extraCond != null ? extraCond : "") + " " +
				 	 (orderClause != null ? orderClause : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			Restaurant restaurant = new Restaurant();
			restaurant.setAccount(dbCon.rs.getString("account"));
			restaurant.setId(dbCon.rs.getInt("id"));
			restaurant.setRecordAlive(dbCon.rs.getInt("record_alive"));
			restaurant.setInfo(dbCon.rs.getString("restaurant_info"));
			restaurant.setName(dbCon.rs.getString("restaurant_name"));
			restaurant.setTele1(dbCon.rs.getString("tele1"));
			restaurant.setTele2(dbCon.rs.getString("tele2"));
			restaurant.setAddress(dbCon.rs.getString("address"));
			restaurant.setLiveness(dbCon.rs.getFloat("liveness"));
			if(dbCon.rs.getTimestamp("expire_date") != null){
				restaurant.setExpireDate(dbCon.rs.getTimestamp("expire_date").getTime());
			}
			
			if(dbCon.rs.getTimestamp("birth_date") != null){
				restaurant.setBirthDate(dbCon.rs.getTimestamp("birth_date").getTime());
			}

			result.add(restaurant);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Update a restaurant according to a builder.
	 * @param builder
	 * 			the builder to update a restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the restaurant to update does NOT exist
	 */
	public static void update(Restaurant.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update a restaurant according to a builder.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the builder to update a restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the restaurant to update does NOT exist
	 */
	public static void update(DBCon dbCon, Restaurant.UpdateBuilder builder) throws SQLException, BusinessException{
		
		String sql;
		//Check to whether the duplicated account exist
		sql = " SELECT * FROM " + Params.dbName + ".restaurant WHERE account = '" + builder.getAccount() + "'" + " AND " + " id <> " + builder.getId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(RestaurantError.DUPLICATED_RESTAURANT_ACCOUNT);
		}
		dbCon.rs.close();
		
		sql = " UPDATE " + Params.dbName + ".restaurant SET " +
			  " id = " + builder.getId() +
			  (builder.getAccount() != null ? " ,account = '" + builder.getAccount() + "'" : "") +
			  (builder.getRestaurantName() != null ? " ,restaurant_name = '" + builder.getRestaurantName() + "'" : "") +
			  (builder.getRestaurantInfo() != null ? " ,restaurant_info = '" + builder.getRestaurantInfo() + "'" : "") +
			  (builder.getTele1() != null ? " ,tele1 = '" + builder.getTele1() + "'" : "") +
			  (builder.getTele2() != null ? " ,tele2 = '" + builder.getTele2() + "'" : "") +
			  (builder.getAddress() != null ? " ,address = '" + builder.getAddress() + "'" : "") +
			  (builder.getRecordAlive() != null ? " ,record_alive = " + builder.getRecordAlive().getSeconds() + "" : "") +
			  (builder.getExpireDate() != 0 ? " ,expire_date = '" + DateUtil.format(builder.getExpireDate()) + "'" : "") +
			  " WHERE id = " + builder.getId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(RestaurantError.RESTAURANT_NOT_FOUND);
		}
		
		//Update the password to administrator of restaurant.
		Staff admin = StaffDao.getStaffsByRoleCategory(dbCon, builder.getId(), Role.Category.ADMIN).get(0);
		Staff.StaffUpdateBuilder staffBuilder = new Staff.StaffUpdateBuilder(admin.getId())
														 .setStaffPwd(builder.getPwd());
		StaffDao.updateStaff(dbCon, staffBuilder);
	}
	
	/**
	 * Insert the new restaurant and related information.
	 * @param builder
	 * 			the builder to new restaurant
	 * @return the id to restaurant just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the restaurant account to insert has been exist before 
	 */
	public static int insert(Restaurant.InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the new restaurant and related information.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the builder to new restaurant
	 * @return the id to restaurant just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the restaurant account to insert has been exist before 
	 */
	public static int insert(DBCon dbCon, Restaurant.InsertBuilder builder) throws SQLException, BusinessException{
		
		Restaurant restaurant = builder.build();
		
		try{
			
			String sql;
			//Check to whether the duplicated account exist
			sql = " SELECT * FROM " + Params.dbName + ".restaurant WHERE account = '" + restaurant.getAccount() + "'";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				throw new BusinessException(RestaurantError.DUPLICATED_RESTAURANT_ACCOUNT);
			}
			dbCon.rs.close();
			
			//Create the new restaurant
			sql = " INSERT INTO " + Params.dbName + ".restaurant " +
				  " (account, birth_date, restaurant_name, restaurant_info, tele1, tele2, address, record_alive, expire_date) " +
				  " VALUES(" +
				  "'" + restaurant.getAccount() + "'," +
				  "'" + DateUtil.format(restaurant.getBirthDate()) + "'," +
				  "'" + restaurant.getName() + "'," +
				  "'" + restaurant.getInfo() + "'," +
				  "'" + restaurant.getTele1() + "'," +
				  "'" + restaurant.getTele2() + "'," +
				  "'" + restaurant.getAddress() + "'," +
				  restaurant.getRecordAlive() + "," +
				  "'" + DateUtil.format(restaurant.getExpireDate()) + "'" +			  
				  " ) ";
			
			dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			dbCon.rs = dbCon.stmt.getGeneratedKeys();
			if(dbCon.rs.next()){
				restaurant.setId(dbCon.rs.getInt(1));
			}else{
				throw new SQLException("The id of printer is not generated successfully.");
			}
			dbCon.rs.close();
			
			//Insert the staff
			Staff staff = initStaff(dbCon, restaurant.getId(), builder.getPwd());
			
			//Insert '商品' material category
			initMaterialCate(dbCon, restaurant.getId());
			
			//Insert默认的活动价格方案
			initPricePlan(dbCon, restaurant.getId());
			
			//Insert the '大牌', '中牌', '例牌' and popular tastes
			initTastes(dbCon, staff);

			//Insert the departments ranged from 1 to 10
			initDepartment(dbCon, staff);

			//Insert the kitchens ranged from 1 to 50
			initKitchen(dbCon, staff);
			
			//Insert the regions ranged from 1 - 10
			initRegion(dbCon, staff);
			
			//Insert the tables
			initTable(dbCon, staff);
			
			//Insert the '无折扣'折扣方案
			initDiscount(dbCon, staff);
			
			//Insert the popular cancel reasons
			initCancelReason(dbCon, staff);
			
			//Insert the setting
			initSetting(dbCon, staff);
			
			return restaurant.getId();
			
		}catch(Exception e){
			if(restaurant.getId() > Restaurant.RESERVED_7){
				deleteById(restaurant.getId());
			}
			throw new SQLException(e);
		}

	}
	
	private static void initSetting(DBCon dbCon, Staff staff) throws SQLException{
		String sql;
		
		sql = " INSERT INTO " + Params.dbName + ".setting " +
			  " (`restaurant_id`) " +
			  " VALUES( " +
			  staff.getRestaurantId() +
			  ")";
		dbCon.stmt.executeUpdate(sql);
		//插入当前月份的第一天作为当前库存的会计月份
		sql = " UPDATE " + Params.dbName + ".setting SET " +
			  " current_material_month = DATE_SUB(CURDATE(), INTERVAL DAY(CURDATE()) - 1 DAY) " +
			  " WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.stmt.executeUpdate(sql);
	}
	
	private static void initCancelReason(DBCon dbCon, Staff staff) throws SQLException{
		for(CancelReason.DefaultCR defCancelReason : CancelReason.DefaultCR.values()){
			CancelReasonDao.insert(dbCon, staff, new CancelReason.InsertBuilder(staff.getRestaurantId(), defCancelReason.getReason()));
		}
	}
	
	private static void initTable(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		TableDao.insert(staff, new Table.InsertBuilder(1, staff.getRestaurantId(), Region.RegionId.REGION_1.getId()));
		TableDao.insert(staff, new Table.InsertBuilder(2, staff.getRestaurantId(), Region.RegionId.REGION_1.getId()));
		TableDao.insert(staff, new Table.InsertBuilder(3, staff.getRestaurantId(), Region.RegionId.REGION_1.getId()));
		TableDao.insert(staff, new Table.InsertBuilder(5, staff.getRestaurantId(), Region.RegionId.REGION_1.getId()));
		TableDao.insert(staff, new Table.InsertBuilder(6, staff.getRestaurantId(), Region.RegionId.REGION_1.getId()));
		TableDao.insert(staff, new Table.InsertBuilder(7, staff.getRestaurantId(), Region.RegionId.REGION_1.getId()));
		TableDao.insert(staff, new Table.InsertBuilder(8, staff.getRestaurantId(), Region.RegionId.REGION_1.getId()));
		TableDao.insert(staff, new Table.InsertBuilder(9, staff.getRestaurantId(), Region.RegionId.REGION_1.getId()));
		TableDao.insert(staff, new Table.InsertBuilder(10, staff.getRestaurantId(), Region.RegionId.REGION_1.getId()));
	}
	
	private static void initRegion(DBCon dbCon, Staff staff) throws SQLException{
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(staff.getRestaurantId(), Region.RegionId.REGION_1));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(staff.getRestaurantId(), Region.RegionId.REGION_2));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(staff.getRestaurantId(), Region.RegionId.REGION_3));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(staff.getRestaurantId(), Region.RegionId.REGION_4));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(staff.getRestaurantId(), Region.RegionId.REGION_5));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(staff.getRestaurantId(), Region.RegionId.REGION_6));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(staff.getRestaurantId(), Region.RegionId.REGION_7));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(staff.getRestaurantId(), Region.RegionId.REGION_8));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(staff.getRestaurantId(), Region.RegionId.REGION_9));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(staff.getRestaurantId(), Region.RegionId.REGION_10));
	}
	
	private static void initDepartment(DBCon dbCon, Staff staff) throws SQLException{
		DepartmentDao.insert(dbCon, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_1));
		DepartmentDao.insert(dbCon, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_2));
		DepartmentDao.insert(dbCon, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_3));
		DepartmentDao.insert(dbCon, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_4));
		DepartmentDao.insert(dbCon, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_5));
		DepartmentDao.insert(dbCon, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_6));
		DepartmentDao.insert(dbCon, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_7));
		DepartmentDao.insert(dbCon, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_8));
		DepartmentDao.insert(dbCon, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_9));
		DepartmentDao.insert(dbCon, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_10));
		DepartmentDao.insert(dbCon, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_TMP));
		DepartmentDao.insert(dbCon, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_NULL));
	}
	
	private static void initKitchen(DBCon dbCon, Staff staff) throws SQLException{
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_1));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_2));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_3));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_4));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_5));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_6));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_7));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_8));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_9));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_10));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_11));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_12));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_13));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_14));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_15));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_16));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_17));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_18));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_19));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_20));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_21));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_22));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_23));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_24));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_25));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_26));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_27));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_28));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_29));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_30));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_31));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_32));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_33));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_34));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_35));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_36));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_37));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_38));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_39));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_40));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_41));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_42));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_43));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_44));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_45));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_46));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_47));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_48));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_49));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_50));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_TEMP));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder(staff.getRestaurantId(), Kitchen.KitchenAlias.KITCHEN_NULL));
	}
	
	private static void initDiscount(DBCon dbCon, Staff staff) throws SQLException{
		DiscountDao.insert(dbCon, staff, new Discount.NotDiscountBuilder(staff.getRestaurantId()));
	}
	
	private static void initTastes(DBCon dbCon, Staff staff) throws SQLException{
		//Insert the '例牌'
		TasteDao.insert(dbCon, staff, new Taste.RegularInsertBuilder(staff.getRestaurantId()));
		//Insert the '中牌'
		TasteDao.insert(dbCon, staff, new Taste.MediumInsertBuilder(staff.getRestaurantId()));
		//Insert the '大牌'
		TasteDao.insert(dbCon, staff, new Taste.LargeInsertBuilder(staff.getRestaurantId()));
	}
	
	private static Staff initStaff(DBCon dbCon, int restaurantId, String pwd) throws SQLException, BusinessException{
		Staff staff = new Staff();
		staff.setRestaurantId(restaurantId);
		//insert '管理员' role
		int adminRoleId = RoleDao.insertRole(dbCon, staff, new Role.DefAdminBuilder(restaurantId));
		//insert '管理员' staff
		int staffId = StaffDao.insertStaff(dbCon, new Staff.DefAdminBuilder(pwd, restaurantId, new Role(adminRoleId)));
		//insert '老板' role
		RoleDao.insertRole(dbCon, staff, new Role.DefBossBuilder(restaurantId));
		//insert '财务' role
		RoleDao.insertRole(dbCon, staff, new Role.DefFinanceBuilder(restaurantId));
		//insert '店长' role
		RoleDao.insertRole(dbCon, staff, new Role.DefManagerBuilder(restaurantId));
		//insert '收银员' role
		RoleDao.insertRole(dbCon, staff, new Role.DefCashierBuilder(restaurantId));
		//insert '服务员' role
		RoleDao.insertRole(dbCon, staff, new Role.DefWaiterBuilder(restaurantId));
		
		staff = new Staff.DefAdminBuilder(pwd, restaurantId, new Role(adminRoleId)).build();
		staff.setRestaurantId(restaurantId);
		staff.setId(staffId);
		
		return staff;
	}
	
	private static void initMaterialCate(DBCon dbCon, int restaurantId) throws SQLException{
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".material_cate " +
			  "(`restaurant_id`, `name`, `type`)" +
			  " VALUES(" +
			  restaurantId + "," +
			  "'" + MaterialCate.Type.GOOD.getText() + "'," +
			  MaterialCate.Type.GOOD.getValue() +
			  " ) ";
		dbCon.stmt.executeUpdate(sql);
	}
	
	private static void initPricePlan(DBCon dbCon, int restaurantId) throws SQLException{
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".price_plan " +
			  "(`restaurant_id`, `name`, `status`)" +
			  " VALUES(" +
			  restaurantId + "," +
			  "'默认方案'" + "," +
			  PricePlan.Status.ACTIVITY.getVal() + 
			  ")";
		dbCon.stmt.executeUpdate(sql);
	}
	
	public static void deleteById(int restaurantId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			deleteById(dbCon, restaurantId);
			dbCon.conn.commit();
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static void deleteById(DBCon dbCon, int restaurantId) throws SQLException{
		String sql;
		
		//Delete the restaurant
		sql = " DELETE FROM " + Params.dbName + ".restaurant WHERE id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete '商品' material category
		sql = " DELETE FROM " + Params.dbName + ".material_cate WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete 默认的活动价格方案
		sql = " DELETE FROM " + Params.dbName + ".price_plan WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the '大牌', '中牌', '例牌' and popular tastes
		sql = " DELETE FROM " + Params.dbName + ".taste WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);

		//Delete the '无折扣'折扣方案
		sql = " DELETE FROM " + Params.dbName + ".discount_plan WHERE discount_id IN " + 
			  "( SELECT discount_id FROM " + Params.dbName + ".discount WHERE restaurant_id = " + restaurantId + ")";
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the '无折扣'折扣方案
		sql = " DELETE FROM " + Params.dbName + ".discount WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the kitchens
		sql = " DELETE FROM " + Params.dbName + ".kitchen WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the departments
		sql = " DELETE FROM " + Params.dbName + ".department WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the regions
		sql = " DELETE FROM " + Params.dbName + ".region WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the tables
		sql = " DELETE FROM " + Params.dbName + ".table WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the popular cancel reasons
		sql = " DELETE FROM " + Params.dbName + ".cancel_reason WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the staff
		sql = " DELETE FROM " + Params.dbName + ".staff WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the role privileges
		sql = " DELETE FROM " + Params.dbName + ".role_privilege WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the role
		sql = " DELETE FROM " + Params.dbName + ".role WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
	}
	
	/**
	 * Update a specified restaurant. 
	 * @param term
	 * 			the terminal
	 * @param restaurant
	 * 			the restaurant to update
	 * @return the count to modified restaurant record
	 * @throws BusinessException
	 * 			if the restaurant to update does NOT exist
	 * @throws SQLException
	 * 			if failed to execute any SQL statements
	 */
	public static void update(Staff term, Restaurant restaurant) throws SQLException, BusinessException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, term, restaurant);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update a specified restaurant. 
	 * @param dbCon
	 * 			The database connection
	 * @param term
	 * 			the terminal
	 * @param restaurant
	 * 			the restaurant to update
	 * @return the count to modified restaurant record
	 * @throws BusinessException
	 * 			if the restaurant to update does NOT exist
	 * @throws SQLException
	 * 			if failed to execute any SQL statements
	 */
	private static void update(DBCon dbCon, Staff term, Restaurant restaurant) throws SQLException, BusinessException{
		String sql = " UPDATE " + Params.dbName + ".restaurant SET " +
					 " restaurant_info = '" + restaurant.getInfo() + "'," +
					 " restaurant_name = '" + restaurant.getName() + "'," +
					 " address = '" + restaurant.getAddress() + "'," +
					 " restaurant.tele1 = '" + restaurant.getTele1() + "'," +
					 " restaurant.tele2 = '" + restaurant.getTele2() + "' " +
					 " WHERE " +
					 " id = " + term.getRestaurantId();
		
		if(dbCon.stmt.executeUpdate(sql) != 1){
			throw new BusinessException(RestaurantError.UPDATE_RESTAURANT_FAIL);
		}
	}
	
	/**
	 * Calculate and update the liveness to each restaurant.
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void calcLiveness() throws SQLException{ 
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			String sql;
			//Get all the restaurants
			sql = " SELECT id FROM " + Params.dbName + ".restaurant WHERE id > " + Restaurant.RESERVED_7;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			List<Restaurant> restaurants = new ArrayList<Restaurant>();
			while(dbCon.rs.next()){
				restaurants.add(new Restaurant(dbCon.rs.getInt("id")));
			}
			dbCon.rs.close();
			
			//Calculate the liveness to each restaurant
			for(Restaurant restaurant : restaurants){
				restaurant.setLiveness(calcLiveness(dbCon, restaurant.getId()));
			}
			
			//Update all liveness to each restaurant
			for(Restaurant restaurant : restaurants){
				sql = " UPDATE " + Params.dbName + ".restaurant SET " +
					  " liveness = " + restaurant.getLiveness() +
					  " WHERE id = " + restaurant.getId();
				dbCon.stmt.executeUpdate(sql);
			}
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the liveness to a specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param restaurantId
	 * 			the restaurant id to calculate liveness
	 * @return the liveness to this restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statment
	 */
	private static float calcLiveness(DBCon dbCon, int restaurantId) throws SQLException{
		
		dbCon.stmt.execute("SET @order_amount_threshold = 5");
		dbCon.stmt.execute("SET @total_days = 10");
		
		String sql;
		
		sql = " SELECT ROUND(COUNT(*) / @total_days, 1) AS liveness FROM " +
			  " ( SELECT COUNT(*) AS order_amount FROM " +
			  Params.dbName + ".order_history " + " WHERE 1 = 1 " +
			  " AND restaurant_id = " + restaurantId +
			  " AND order_date BETWEEN DATE_SUB(CURDATE(), interval @total_days day) AND CURDATE() " +
			  " GROUP BY DATE(order_date) " +
			  " HAVING order_amount >= @order_amount_threshold " +
			  " ) AS TMP ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		float liveness = 0;
		if(dbCon.rs.next()){
			liveness = dbCon.rs.getFloat("liveness");
		}
		dbCon.rs.close();
		
		return liveness;
	}
}










