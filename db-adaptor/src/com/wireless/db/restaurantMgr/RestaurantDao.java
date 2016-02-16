 package com.wireless.db.restaurantMgr;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.member.MemberCondDao;
import com.wireless.db.member.MemberTypeDao;
import com.wireless.db.printScheme.PrinterDao;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.serviceRate.ServicePlanDao;
import com.wireless.db.sms.SMStatDao;
import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.system.BillBoardDao;
import com.wireless.db.system.BusinessHourDao;
import com.wireless.db.tasteMgr.TasteCategoryDao;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.db.weixin.action.WxKeywordDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ModuleError;
import com.wireless.exception.RestaurantError;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.member.MemberCond;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Module;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.serviceRate.ServicePlan;
import com.wireless.pojo.sms.SMStat;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.system.BillBoard;
import com.wireless.pojo.system.BusinessHour;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteCategory;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.weixin.action.WxKeyword;

public class RestaurantDao {
	
	public static class ExtraCond{
		private int id;
		private String fuzzy;
		private String name;
		private String account;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setFuzzy(String fuzzy){
			this.fuzzy = fuzzy;
			return this;
		}
		
		public ExtraCond setName(String name){
			this.name = name;
			return this;
		}
		
		public ExtraCond setAccount(String account){
			this.account = account;
			return this;
		}
		
		@Override
		public String toString(){
			final StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND id = " + id);
			}
			if(fuzzy != null){
				extraCond.append(" AND (restaurant_name LIKE '%" + fuzzy + "%' OR account LIKE '%" + fuzzy + "%')");
			}
			if(name != null){
				extraCond.append(" AND restaurant_name LIKE '%" + name + "%'");
			}
			if(account != null){
				extraCond.append(" AND account = '" + account + "'");
			}
			return extraCond.toString();
		}
	}
	
	public static final class LivessnessResult{
		private final int amount;
		private final int elapsed;
		public LivessnessResult(int amount, int elapsed) {
			this.amount = amount;
			this.elapsed = elapsed;
		}
		public int getAmount(){
			return this.amount;
		}
		public int getElapsed(){
			return this.elapsed;
		}
		@Override
		public String toString(){
			return "The calculation to " + amount + " restaurant(s) liveness takes " + elapsed + " sec.";
		}
	}
	
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
		List<Restaurant> result = getByCond(dbCon, new ExtraCond().setId(restaurantId), null);
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
		List<Restaurant> result = getByCond(dbCon, new ExtraCond().setAccount(account), null);
		if(result.isEmpty()){
			throw new BusinessException(RestaurantError.RESTAURANT_NOT_FOUND);
		}else{
			return result.get(0);
		}
	}
	
	public static List<Restaurant> getByCond(ExtraCond extraCond, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<Restaurant> getByCond(DBCon dbCon, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException{
		
		List<Restaurant> result = new ArrayList<Restaurant>();
		
		String sql = " SELECT * FROM " + Params.dbName + ".restaurant " +
				 	 " WHERE 1 = 1 " +
				 	 " AND id > " + Restaurant.RESERVED_7 + " " +
				 	 (extraCond != null ? extraCond.toString() : "") + " " +
				 	 (orderClause != null ? orderClause : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			Restaurant restaurant = new Restaurant(dbCon.rs.getInt("id"));
			restaurant.setAccount(dbCon.rs.getString("account"));
			restaurant.setRecordAlive(dbCon.rs.getInt("record_alive"));
			restaurant.setInfo(dbCon.rs.getString("restaurant_info"));
			restaurant.setName(dbCon.rs.getString("restaurant_name"));
			restaurant.setDianpingId(dbCon.rs.getInt("dianping_id"));
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
			
			restaurant.setType(Restaurant.Type.valueOf(dbCon.rs.getInt("type")));
			
			restaurant.setPublicKey(dbCon.rs.getString("public_key"));
			restaurant.setPrivateKey(dbCon.rs.getString("private_key"));
			
			restaurant.setBeeCloudAppId(dbCon.rs.getString("bee_cloud_app_id"));
			restaurant.setBeeCloudAppSecret(dbCon.rs.getString("bee_cloud_app_secret"));
			result.add(restaurant);
		}
		dbCon.rs.close();
		
		for(Restaurant eachRestaurant : result){
			//Get the modules to each restaurant.
			sql = " SELECT M.module_id, M.code FROM " + Params.dbName + ".restaurant_module RM "	+
				  " JOIN " + Params.dbName + ".module M " + " ON RM.module_id = M.module_id " + 
				  " WHERE RM.restaurant_id = " + eachRestaurant.getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				eachRestaurant.addModule(new Module(dbCon.rs.getInt("module_id"), Module.Code.valueOf(dbCon.rs.getInt("code"))));
			}
			dbCon.rs.close();
			
			if(eachRestaurant.getType() == Restaurant.Type.GROUP){
				//Get the branches to each restaurant
				sql = " SELECT branch_id FROM " + Params.dbName + ".restaurant_chain WHERE group_id = " + eachRestaurant.getId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				while(dbCon.rs.next()){
					eachRestaurant.addBranch(RestaurantDao.getById(dbCon.rs.getInt("branch_id")));
				}
				dbCon.rs.close();
			}
		}
		
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
			dbCon.conn.setAutoCommit(false);
			update(dbCon, builder);
			dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
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

		Restaurant restaurant = builder.build();
		
		String sql;
		if(builder.isAccountChanged()){
			//Check to whether the duplicated account exist
			sql = " SELECT * FROM " + Params.dbName + ".restaurant WHERE account = '" + restaurant.getAccount() + "'" + " AND " + " id <> " + builder.getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				throw new BusinessException(RestaurantError.DUPLICATED_RESTAURANT_ACCOUNT);
			}
			dbCon.rs.close();
		}
		
		//Update the basic.
		sql = " UPDATE " + Params.dbName + ".restaurant SET " +
			  " id = " + builder.getId() +
			  (builder.isAccountChanged() ? " ,account = '" + restaurant.getAccount() + "'" : "") +
			  (builder.isRestaurantNameChanged() ? " ,restaurant_name = '" + restaurant.getName() + "'" : "") +
			  (builder.isRestaurantInfoChanged() ? " ,restaurant_info = '" + restaurant.getInfo() + "'" : "") +
			  (builder.isDianpingIdChanged() ? " ,dianping_id = " + restaurant.getDianpingId() : "") +
			  (builder.isTele1Changed() ? " ,tele1 = '" + restaurant.getTele1() + "'" : "") +
			  (builder.isTele2Changed() ? " ,tele2 = '" + restaurant.getTele2() + "'" : "") +
			  (builder.isAddressChanged() ? " ,address = '" + restaurant.getAddress() + "'" : "") +
			  (builder.isRecordAliveChanged() ? " ,record_alive = " + restaurant.getRecordAlive() + "" : "") +
			  (builder.isExpireDateChanged() ? " ,expire_date = '" + DateUtil.format(restaurant.getExpireDate()) + "'" : "") +
			  (builder.isRSAChanged() ? " ,public_key = '" + restaurant.getPublicKey() + "', private_key = '" + restaurant.getPrivateKey() + "'" : "") +
			  (builder.isBeeCloudChanged() ? " ,bee_cloud_app_id = '" + restaurant.getBeeCloudAppId() + "', bee_cloud_app_secret = '" + restaurant.getBeeCloudAppSecret() + "'" : "") +
			  " WHERE id = " + builder.getId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(RestaurantError.RESTAURANT_NOT_FOUND);
		}
		
		//Update the module.
		if(builder.isModuleChanged()){
			sql = " DELETE FROM " + Params.dbName + ".restaurant_module WHERE restaurant_id = " + builder.getId();
			dbCon.stmt.executeUpdate(sql);
			for(Module module : restaurant.getModules()){
				insertModule(dbCon, StaffDao.getAdminByRestaurant(builder.getId()), module);
			}
		}
		
		//Update the password to administrator of restaurant.
		if(builder.isPwdChanged()){
			Staff admin = StaffDao.getAdminByRestaurant(dbCon, builder.getId());
			StaffDao.update(dbCon, admin, new Staff.UpdateBuilder(admin.getId()).setStaffPwd(builder.getPwd()));
		}
		
		//Update the branches.
		if(builder.isBranchChanged() && RestaurantDao.getById(dbCon, restaurant.getId()).getType() != Restaurant.Type.BRANCE){
			//Update the branches to restaurant & delete from restaurant chain
			sql = " UPDATE " + Params.dbName + ".restaurant SET type = " + Restaurant.Type.RESTAURANT.getVal() + " WHERE id IN ( " +
					  " SELECT branch_id FROM " + Params.dbName + ".restaurant_chain WHERE group_id = " + restaurant.getId() + ")";
			dbCon.stmt.executeUpdate(sql);
				
			sql = " DELETE FROM " + Params.dbName + ".restaurant_chain WHERE group_id = " + restaurant.getId();
			dbCon.stmt.executeUpdate(sql);
			
			if(restaurant.hasBranches()){
				sql = " UPDATE " + Params.dbName + ".restaurant SET type = " + Restaurant.Type.GROUP.getVal() + " WHERE id = " + restaurant.getId();
				dbCon.stmt.executeUpdate(sql);
				
				for(Restaurant branch : restaurant.getBranches()){
					//Check to see whether the branch is group.
					if(RestaurantDao.getById(dbCon, branch.getId()).getType() == Restaurant.Type.RESTAURANT){
						sql = " DELETE FROM " + Params.dbName + ".restaurant_chain WHERE group_id = " + branch.getId();
						dbCon.stmt.executeUpdate(sql);
						
						sql = " INSERT INTO " + Params.dbName + ".restaurant_chain " +
							  " ( group_id, branch_id ) VALUES ( " +
							  restaurant.getId() + "," +
							  branch.getId() + 
							  ")";
						dbCon.stmt.executeUpdate(sql);
						
						sql = " UPDATE " + Params.dbName + ".restaurant SET type = " + Restaurant.Type.BRANCE.getVal() + " WHERE id = " + branch.getId();
						dbCon.stmt.executeUpdate(sql);
					}else{
						throw new BusinessException("普通餐厅才能成为连锁门店", RestaurantError.RESTAURANT_CHAIN_ERROR);
					}
				}
			}else{
				sql = " UPDATE " + Params.dbName + ".restaurant SET type = " + Restaurant.Type.RESTAURANT.getVal() + " WHERE id = " + restaurant.getId();
				dbCon.stmt.executeUpdate(sql);
			}
			
			//Clear the unused member chain discount.
			sql = " DELETE FROM " + Params.dbName + ".member_chain_discount WHERE branch_id NOT IN ( " +
				  " SELECT branch_id FROM " + Params.dbName + ".restaurant_chain WHERE group_id = " + restaurant.getId() + ")";
			dbCon.stmt.executeUpdate(sql);
			
			//Clear the unused member chain price plan.
			sql = " DELETE FROM " + Params.dbName + ".member_chain_price WHERE branch_id NOT IN ( " +
				  " SELECT branch_id FROM " + Params.dbName + ".restaurant_chain WHERE group_id = " + restaurant.getId() + ")";
			dbCon.stmt.executeUpdate(sql);
		}
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
				  " (account, birth_date, restaurant_name, restaurant_info, tele1, tele2, address, record_alive, expire_date, dianping_id, public_key, private_key) " +
				  " VALUES(" +
				  "'" + restaurant.getAccount() + "'," +
				  "'" + DateUtil.format(restaurant.getBirthDate()) + "'," +
				  "'" + restaurant.getName() + "'," +
				  "'" + restaurant.getInfo() + "'," +
				  "'" + restaurant.getTele1() + "'," +
				  "'" + restaurant.getTele2() + "'," +
				  "'" + restaurant.getAddress() + "'," +
				  restaurant.getRecordAlive() + "," +
				  "'" + DateUtil.format(restaurant.getExpireDate()) + "'," +
				  restaurant.getDianpingId() + "," +
				  "'" + restaurant.getPublicKey() + "'," +
				  "'" + restaurant.getPrivateKey() + "'" +
				  " ) ";
			
			dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			dbCon.rs = dbCon.stmt.getGeneratedKeys();
			if(dbCon.rs.next()){
				restaurant.setId(dbCon.rs.getInt(1));
			}else{
				throw new SQLException("The id of restaurant is not generated successfully.");
			}
			dbCon.rs.close();
			
			//Insert the staff
			Staff staff = initStaff(dbCon, restaurant.getId(), builder.getPwd());
			
			//Insert the '大牌', '中牌', '例牌' and popular tastes
			initTastes(dbCon, staff);

			//Insert the departments and kitchens 
			initDepartmentAndKitchen(dbCon, staff);

			//Insert the regions ranged from 1 - 10
			initRegion(dbCon, staff);
			
			//Insert the tables
			initTable(dbCon, staff);
			
			//Insert the '无折扣'折扣方案
			initDiscount(dbCon, staff);
			
			//Insert the '免服务费'方案
			initServicePlan(dbCon, staff);
			
			//Insert the popular cancel reasons
			initCancelReason(dbCon, staff);
			
			//Insert the setting
			initSetting(dbCon, staff);
			
			//Insert a weixin member type
			initMemberType(dbCon, staff);
			
			//Insert the printers
			initPrinter(dbCon, staff);
			
			//Insert the weixin restaurant
			initWeixinRestaurant(dbCon, staff);
			
			//Insert the module
			initModule(dbCon, staff, restaurant);
			
			//Insert the SMS state
			initSMStat(dbCon, staff);
			
			//Insert the business hour
			initBusinessHour(dbCon, staff);
			
			//Insert the member condition
			initMemberCond(dbCon, staff);
			
			//Insert the 'exception' weixin keyword
			initWxKeyword(dbCon, staff);
			
			return restaurant.getId();
			
		}catch(Exception e){
			if(restaurant.getId() > Restaurant.RESERVED_7){
				deleteById(restaurant.getId());
			}
			throw new SQLException(e);
		}

	}
	
	private static void initWxKeyword(DBCon dbCon, Staff staff) throws SQLException{
		WxKeywordDao.insert(dbCon, staff, new WxKeyword.InsertBuilder(null, WxKeyword.Type.EXCEPTION));
	}
	
	private static void initMemberCond(DBCon dbCon, Staff staff) throws SQLException{
		MemberCondDao.insert(dbCon, staff, new MemberCond.InsertBuilder("活跃会员").setRangeType(MemberCond.RangeType.LAST_3_MONTHS).setConsumeAmount(0, 5));
		MemberCondDao.insert(dbCon, staff, new MemberCond.InsertBuilder("活跃会员").setRangeType(MemberCond.RangeType.LAST_3_MONTHS).setConsumeAmount(3, 0));
	};
	
	private static void initBusinessHour(DBCon dbCon, Staff staff) throws SQLException, ParseException{
		BusinessHourDao.insert(dbCon, staff, new BusinessHour.InsertBuilder("早市", new HourRange("6:00", "11:00", DateUtil.Pattern.HOUR)));
		BusinessHourDao.insert(dbCon, staff, new BusinessHour.InsertBuilder("午市", new HourRange("11:00", "15:00", DateUtil.Pattern.HOUR)));
		BusinessHourDao.insert(dbCon, staff, new BusinessHour.InsertBuilder("晚市", new HourRange("15:00", "23:00", DateUtil.Pattern.HOUR)));
	}
	
	private static void initSMStat(DBCon dbCon, Staff staff) throws SQLException{
		SMStatDao.insert(dbCon, staff, new SMStat.InsertBuilder(staff.getRestaurantId()));
	}
	
	private static void insertModule(DBCon dbCon, Staff staff, Module module) throws SQLException, BusinessException{
		int moduleId = 0;
		String sql = " SELECT module_id FROM " + Params.dbName + ".module WHERE code = " + module.getCode().getVal();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			moduleId = dbCon.rs.getInt("module_id");
		}else{
			throw new BusinessException(ModuleError.MODULE_NOT_EXIST);
		}
		dbCon.rs.close();
		
		sql = " INSERT INTO " + Params.dbName + ".restaurant_module " +
			  " (restaurant_id, module_id) " +
			  " VALUES(" +
			  staff.getRestaurantId() + "," +
			  moduleId + ")";
		dbCon.stmt.executeUpdate(sql);
	}
	
	private static void initModule(DBCon dbCon, Staff staff, Restaurant restaurant) throws SQLException, BusinessException{
		for(Module module : restaurant.getModules()){
			insertModule(dbCon, staff, module);
		}
	}
	
	private static void initWeixinRestaurant(DBCon dbCon, Staff staff) throws SQLException{
		WxRestaurantDao.insert(dbCon, staff);
	}
	
	private static void initPrinter(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		PrinterDao.insert(dbCon, staff, new Printer.InsertBuilder("GP-80250-201", PStyle.PRINT_STYLE_80MM));
		PrinterDao.insert(dbCon, staff, new Printer.InsertBuilder("GP-80250-202", PStyle.PRINT_STYLE_80MM));
		PrinterDao.insert(dbCon, staff, new Printer.InsertBuilder("GP-80250-203", PStyle.PRINT_STYLE_80MM));
		PrinterDao.insert(dbCon, staff, new Printer.InsertBuilder("GP-80250-204", PStyle.PRINT_STYLE_80MM));
		PrinterDao.insert(dbCon, staff, new Printer.InsertBuilder("GP-80250-205", PStyle.PRINT_STYLE_80MM));
		PrinterDao.insert(dbCon, staff, new Printer.InsertBuilder("GP-80250-206", PStyle.PRINT_STYLE_80MM).setEnabled(false));
		PrinterDao.insert(dbCon, staff, new Printer.InsertBuilder("GP-80250-207", PStyle.PRINT_STYLE_80MM).setEnabled(false));
		PrinterDao.insert(dbCon, staff, new Printer.InsertBuilder("GP-80250-208", PStyle.PRINT_STYLE_80MM).setEnabled(false));
		PrinterDao.insert(dbCon, staff, new Printer.InsertBuilder("GP-80250-209", PStyle.PRINT_STYLE_80MM).setEnabled(false));
		PrinterDao.insert(dbCon, staff, new Printer.InsertBuilder("GP-80250-210", PStyle.PRINT_STYLE_80MM).setEnabled(false));
	}
	
	private static void initMemberType(DBCon dbCon, Staff staff) throws SQLException{
		MemberType.InsertBuilder builder = new MemberType.InsertBuilder("微信会员", 
																		DiscountDao.getDefault(dbCon, staff))
														 .setType(MemberType.Type.WEIXIN);
		MemberTypeDao.insert(dbCon, staff, builder);
	}
	
	private static void initSetting(DBCon dbCon, Staff staff) throws SQLException{
		String sql;
		
		sql = " INSERT INTO " + Params.dbName + ".setting " +
			  " (`restaurant_id`) " +
			  " VALUES( " +
			  staff.getRestaurantId() +
			  ")";
		dbCon.stmt.executeUpdate(sql);
	}
	
	private static void initCancelReason(DBCon dbCon, Staff staff) throws SQLException{
		for(CancelReason.DefaultCR defCancelReason : CancelReason.DefaultCR.values()){
			CancelReasonDao.insert(dbCon, staff, new CancelReason.InsertBuilder(staff.getRestaurantId(), defCancelReason.getReason()));
		}
	}
	
	private static void initTable(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		TableDao.insert(staff, new Table.InsertBuilder(1, Region.RegionId.REGION_1));
		TableDao.insert(staff, new Table.InsertBuilder(2, Region.RegionId.REGION_1));
		TableDao.insert(staff, new Table.InsertBuilder(3, Region.RegionId.REGION_1));
		TableDao.insert(staff, new Table.InsertBuilder(5, Region.RegionId.REGION_1));
		TableDao.insert(staff, new Table.InsertBuilder(6, Region.RegionId.REGION_1));
		TableDao.insert(staff, new Table.InsertBuilder(7, Region.RegionId.REGION_1));
		TableDao.insert(staff, new Table.InsertBuilder(8, Region.RegionId.REGION_1));
		TableDao.insert(staff, new Table.InsertBuilder(9, Region.RegionId.REGION_1));
		TableDao.insert(staff, new Table.InsertBuilder(10, Region.RegionId.REGION_1));
	}
	
	private static void initRegion(DBCon dbCon, Staff staff) throws SQLException{
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_1, "大厅", Region.Status.BUSY));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_2, "包房", Region.Status.BUSY));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_3, "外卖", Region.Status.BUSY));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_4));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_5));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_6));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_7));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_8));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_9));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_10));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_11));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_12));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_13));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_14));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_15));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_16));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_17));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_18));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_19));
		RegionDao.insert(dbCon, staff, new Region.InsertBuilder(Region.RegionId.REGION_20));
	}
	
	private static void initDepartmentAndKitchen(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_1).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_2).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_3).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_4).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_5).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_6).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_7).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_8).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_9).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_10).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_11).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_12).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_13).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_14).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_15).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_16).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_17).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_18).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_19).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_20).setType(Department.Type.IDLE));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_TMP));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_NULL));
		DepartmentDao.insert(dbCon, staff, new Department.InsertBuilder(staff.getRestaurantId(), Department.DeptId.DEPT_WAREHOUSE));

		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨1", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨2", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨3", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨4", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨5", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨6", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨7", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨8", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨9", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨10", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨11", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨12", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨13", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨14", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨15", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨16", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨17", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨18", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨19", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨20", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨21", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨22", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨23", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨24", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨25", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨26", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨27", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨28", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨29", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨30", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨31", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨32", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨33", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨34", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨35", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨36", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨37", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨38", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨39", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨40", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨41", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨42", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨43", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨44", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨45", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨46", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨47", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨48", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨49", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨50", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨51", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨52", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨53", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨54", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨55", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨56", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨57", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨58", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨59", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨60", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨61", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨62", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨63", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨64", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨65", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨66", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨67", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨68", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨69", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨70", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨71", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨72", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨73", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨74", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨75", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨76", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨77", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨78", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨79", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("分厨80", Department.DeptId.DEPT_NULL, Kitchen.Type.IDLE));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("空厨房", Department.DeptId.DEPT_NULL, Kitchen.Type.NULL));
		KitchenDao.insert(dbCon, staff, new Kitchen.InsertBuilder("临时厨房", Department.DeptId.DEPT_TMP, Kitchen.Type.TEMP));
		
		short deptId = DepartmentDao.add(dbCon, staff, new Department.AddBuilder("中厨"));
		KitchenDao.add(dbCon, staff, new Kitchen.AddBuilder("中厨分厨1", Department.DeptId.valueOf(deptId)));
		KitchenDao.add(dbCon, staff, new Kitchen.AddBuilder("中厨分厨2", Department.DeptId.valueOf(deptId)));
		KitchenDao.add(dbCon, staff, new Kitchen.AddBuilder("中厨分厨3", Department.DeptId.valueOf(deptId)));
		KitchenDao.add(dbCon, staff, new Kitchen.AddBuilder("中厨分厨4", Department.DeptId.valueOf(deptId)));
		KitchenDao.add(dbCon, staff, new Kitchen.AddBuilder("中厨分厨5", Department.DeptId.valueOf(deptId)));
		
		deptId = DepartmentDao.add(dbCon, staff, new Department.AddBuilder("酒水"));
		KitchenDao.add(dbCon, staff, new Kitchen.AddBuilder("酒水分厨1", Department.DeptId.valueOf(deptId)));
		KitchenDao.add(dbCon, staff, new Kitchen.AddBuilder("酒水分厨2", Department.DeptId.valueOf(deptId)));
		KitchenDao.add(dbCon, staff, new Kitchen.AddBuilder("酒水分厨3", Department.DeptId.valueOf(deptId)));

		deptId = DepartmentDao.add(dbCon, staff, new Department.AddBuilder("杂项"));
		KitchenDao.add(dbCon, staff, new Kitchen.AddBuilder("杂项分厨1", Department.DeptId.valueOf(deptId)));
		KitchenDao.add(dbCon, staff, new Kitchen.AddBuilder("杂项分厨2", Department.DeptId.valueOf(deptId)));
		
	}
	
	private static void initServicePlan(DBCon dbCon, Staff staff) throws SQLException{
		ServicePlanDao.insert(dbCon, staff, new ServicePlan.InsertBuilder("免服务费").setType(ServicePlan.Type.RESERVED).setStatus(ServicePlan.Status.DEFAULT));
	}
	
	private static void initDiscount(DBCon dbCon, Staff staff) throws SQLException{
		DiscountDao.insert(dbCon, staff, new Discount.EmptyBuilder());
	}
	
	private static void initTastes(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		int categoryId; 
		
		//Insert the '通用' category
		categoryId = TasteCategoryDao.insert(dbCon, staff, new TasteCategory.InsertBuilder(staff.getRestaurantId(), "通用"));
		TasteCategory commonCategory = TasteCategoryDao.getById(dbCon, staff, categoryId);
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "打包", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "加快", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "补单免做", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "分席上", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "清淡", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "少盐", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "少油", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "免辣", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "加辣", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "加姜", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "加葱", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "免姜", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "免葱", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "冻饮", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "热饮", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "VIP用", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "即起", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "生上", commonCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "开锅", commonCategory));

		//Insert the '海鲜' category
		categoryId = TasteCategoryDao.insert(dbCon, staff, new TasteCategory.InsertBuilder(staff.getRestaurantId(), "海鲜"));
		TasteCategory seefoodCategory = TasteCategoryDao.getById(dbCon, staff, categoryId);
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "清蒸", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "豉汁蒸", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "酸菜", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "水煮", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "香煎", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "椒盐", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "白灼", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "刺身", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "铁板", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "美极", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "榄角蒸", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "荷叶蒸", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "红枣枸子蒸", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "豉汁金钱片", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "砂锅", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "锡纸", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "油浸", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "药材浸", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "蒜子焖", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "姜葱焖", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "红焖", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "开锅", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "起片", seefoodCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "酱爆", seefoodCategory));

		//Insert the '蔬菜' category
		categoryId = TasteCategoryDao.insert(dbCon, staff, new TasteCategory.InsertBuilder(staff.getRestaurantId(), "蔬菜"));
		TasteCategory vegatableCategory = TasteCategoryDao.getById(dbCon, staff, categoryId);
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "盐水", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "上汤", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "生炒", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "蒜蓉炒", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "豉汁炒", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "姜汁炒", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "耗油", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "煲淋", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "咸猪骨煲", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "豆豉炒", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "豆豉鲮鱼炒", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "辣炒", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "凉拌", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "原味", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "肉片炒", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "牛肉炒", vegatableCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "鲜尤炒", vegatableCategory));
		
		//Insert the '西餐' category
		categoryId = TasteCategoryDao.insert(dbCon, staff, new TasteCategory.InsertBuilder(staff.getRestaurantId(), "西餐"));
		TasteCategory westernCategory = TasteCategoryDao.getById(dbCon, staff, categoryId);
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "黑椒汁", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "蒜蓉汁", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "红酒汁", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "洋葱汁", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "蘑菇汁", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "咖喱汁", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "香草汁", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "番茄汁", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "加铁板", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "加白饭", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "加意粉", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "全熟", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "九成熟", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "八成熟", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "七成熟", westernCategory));
		TasteDao.insert(dbCon, staff, new Taste.InsertBuilder(staff.getRestaurantId(), "六成熟", westernCategory));
		
		//Insert the '规格' category
		categoryId = TasteCategoryDao.insert(dbCon, staff, new TasteCategory.SpecInsertBuilder(staff.getRestaurantId()));
		TasteCategory spec = TasteCategoryDao.getById(dbCon, staff, categoryId);
		//Insert the '例牌'
		TasteDao.insert(dbCon, staff, new Taste.RegularInsertBuilder(staff.getRestaurantId(), spec));
		//Insert the '中牌'
		TasteDao.insert(dbCon, staff, new Taste.MediumInsertBuilder(staff.getRestaurantId(), spec));
		//Insert the '大牌'
		TasteDao.insert(dbCon, staff, new Taste.LargeInsertBuilder(staff.getRestaurantId(), spec));
	}
	
	private static Staff initStaff(DBCon dbCon, int restaurantId, String pwd) throws SQLException, BusinessException{
		Staff staff = new Staff();
		staff.setRestaurantId(restaurantId);
		//insert '管理员' role
		int adminRoleId = RoleDao.insert(dbCon, staff, new Role.AdminBuilder(restaurantId));
		//insert '管理员' staff
		int staffId = StaffDao.insert(dbCon, staff, new Staff.AdminBuilder(pwd, new Role(adminRoleId)));
		//insert '老板' role
		RoleDao.insert(dbCon, staff, new Role.BossBuilder(restaurantId));
		//insert '财务' role
		RoleDao.insert(dbCon, staff, new Role.FinancerBuilder(restaurantId));
		//insert '店长' role
		RoleDao.insert(dbCon, staff, new Role.ManagerBuilder(restaurantId));
		//insert '收银员' role
		RoleDao.insert(dbCon, staff, new Role.CashierBuilder(restaurantId));
		//insert '服务员' role
		RoleDao.insert(dbCon, staff, new Role.DefWaiterBuilder(restaurantId));
		
		return StaffDao.getById(dbCon, staffId);
	}
	
//	private static void initMaterialCate(DBCon dbCon, int restaurantId) throws SQLException{
//		String sql;
//		sql = " INSERT INTO " + Params.dbName + ".material_cate " +
//			  "(`restaurant_id`, `name`, `type`)" +
//			  " VALUES(" +
//			  restaurantId + "," +
//			  "'" + MaterialCate.Type.GOOD.getText() + "'," +
//			  MaterialCate.Type.GOOD.getValue() +
//			  " ) ";
//		dbCon.stmt.executeUpdate(sql);
//	}
	
	/**
	 * Delete the restaurant to specific id.
	 * @param restaurantId
	 * 			the restaurant id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the restaurant belongs to group
	 */
	public static void deleteById(int restaurantId) throws SQLException, BusinessException{
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
	
	/**
	 * Delete the restaurant to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param restaurantId
	 * 			the restaurant id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the restaurant belongs to group
	 */
	public static void deleteById(DBCon dbCon, int restaurantId) throws SQLException, BusinessException{
		String sql;

		Restaurant restaurant = getById(dbCon, restaurantId);
		if(restaurant.hasBranches()){
			throw new BusinessException("集团下还有门店，不能删除");
		}
		
		//Delete the restaurant
		sql = " DELETE FROM " + Params.dbName + ".restaurant WHERE id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the restaurant chain
		sql = " DELETE FROM " + Params.dbName + ".restaurant_chain WHERE branch_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete '商品' material category
		sql = " DELETE FROM " + Params.dbName + ".material_cate WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the taste category
		sql = " DELETE FROM " + Params.dbName + ".taste_category WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the '大牌', '中牌', '例牌' and popular tastes
		sql = " DELETE FROM " + Params.dbName + ".taste WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);

		//Delete the '无折扣'折扣方案
		sql = " DELETE FROM " + Params.dbName + ".discount_plan WHERE discount_id IN " + 
			  "( SELECT discount_id FROM " + Params.dbName + ".discount WHERE restaurant_id = " + restaurantId + ")";
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the discount
		sql = " DELETE FROM " + Params.dbName + ".discount WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the '免服务费'方案
		sql = " DELETE FROM " + Params.dbName + ".service_rate WHERE plan_id IN " +
			  " ( SELECT plan_id FROM " + Params.dbName + ".service_plan WHERE restaurant_id = " + restaurantId + ")";
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the service plan
		sql = " DELETE FROM " + Params.dbName + ".service_plan WHERE restaurant_id = " + restaurantId;
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
		
		//Delete the setting
		sql = " DELETE FROM " + Params.dbName + ".setting WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the member type discount
		sql = " DELETE FROM " + Params.dbName + ".member_type_discount WHERE member_type_id IN (" +
			  " SELECT member_type_id FROM " + Params.dbName + ".member_type WHERE restaurant_id = " + restaurantId + ")";
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the member type price plan
		sql = " DELETE FROM " + Params.dbName + ".member_type_price WHERE member_type_id IN (" +
			  " SELECT member_type_id FROM " + Params.dbName + ".member_type WHERE restaurant_id = " + restaurantId + ")";
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the member type
		sql = " DELETE FROM " + Params.dbName + ".member_type WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the printers
		sql = " DELETE FROM " + Params.dbName + ".printer WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the weixin misc
		sql = " DELETE FROM " + Params.dbName + ".weixin_restaurant WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the restaurant module
		sql = " DELETE FROM " + Params.dbName + ".restaurant_module WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);

		//Delete the business hour
		sql = " DELETE FROM " + Params.dbName + ".business_hour WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the weixin keyword
		sql = " DELETE FROM " + Params.dbName + ".weixin_keyword WHERE restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Calculate and update the liveness to each restaurant.
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static LivessnessResult calcLiveness() throws SQLException{ 
		DBCon dbCon = new DBCon();
		try{
			long beginTime = System.currentTimeMillis();
			
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
			
			int amount = 0;
			//Update all liveness to each restaurant
			for(Restaurant restaurant : restaurants){
				sql = " UPDATE " + Params.dbName + ".restaurant SET " +
					  " liveness = " + restaurant.getLiveness() +
					  " WHERE id = " + restaurant.getId();
				dbCon.stmt.executeUpdate(sql);
				amount++;
			}
			
			return new LivessnessResult(amount, (int)(System.currentTimeMillis() - beginTime) / 1000);
			
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
	
	public static class ExpiredResult{
		public final int amount;
		private final int elapsed;
		ExpiredResult(int amount, int elapsed){
			this.amount = amount;
			this.elapsed = elapsed;
		}
		@Override
		public String toString(){
			return "generate " + amount + " expired bill board(s) takes " + elapsed + " sec.";
		}
	}
	
	public static ExpiredResult calcExpired() throws SQLException, BusinessException{
		final int TWO_WEEKS = 3600 * 24 * 1000 * 7;
		long beginTime = System.currentTimeMillis();
		int amount = 0;
		for(Restaurant r : getByCond(null, null)){
			long remaining = r.getExpireDate() - System.currentTimeMillis();
			
			if(Math.abs(remaining) < TWO_WEEKS && remaining > 0){
				try{
					String body = "亲爱的【$(account)】用户，您的账户将于$(expired_date)到期，为免影响您的正常使用，请及时联络客服人员进行续费，非常感谢您对智易软件的支持:-)";
					body = body.replace("$(account)", r.getName());
					body = body.replace("$(expired_date)", (remaining / (3600 * 24 * 1000) + 1) + "天后(" + DateUtil.format(r.getExpireDate(), DateUtil.Pattern.DATE) + ")");
					body = "<p><strong><span style=\"color:#632423;font-size:24px\">" + body + "</span></strong></p>";
					BillBoardDao.insert(BillBoard.InsertBuilder.build4Restaurant("餐厅账号到期提醒", r.getId(), DateUtil.format(System.currentTimeMillis() + 3600))
															   .setBody(body));
					amount++;
				}catch(BusinessException | ParseException ignored){
					ignored.printStackTrace();
				}
			}
		}
		return new ExpiredResult(amount, (int)(System.currentTimeMillis() - beginTime) / 1000);
	}
}










