package com.wireless.db.printScheme;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PrintSchemeError;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.PrintFunc;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;

public class PrintFuncDao {
	
	public static class ExtraCond{
		private int id;
		private int printerId;
		private PType type;
		private int enabled = -1;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setPrinter(Printer printer){
			this.printerId = printer.getId();
			return this;
		}
		
		public ExtraCond setPrinter(int printerId){
			this.printerId = printerId;
			return this;
		}
		
		public ExtraCond setType(PType type){
			this.type = type;
			return this;
		}
		
		public ExtraCond setEnabled(boolean onOff){
			this.enabled = onOff ? 1 : 0;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND func_id = " + id);
			}
			if(printerId != 0){
				extraCond.append(" AND printer_id = " + printerId);
			}
			if(type != null){
				extraCond.append(" AND type = " + type.getVal());
			}
			if(enabled != -1){
				extraCond.append(" AND enabled = " + enabled);
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Add a new print function to specific printer
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param func
	 * 			the function to add
	 * @return the id to print function just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the printer does NOT exist
	 * 			throws if the function type has exist before
	 */
	private static int addFunc(DBCon dbCon, Staff staff, PrintFunc func) throws SQLException, BusinessException{
		
		String sql;
		
		//Check to see whether the printer is exist
		sql = " SELECT * FROM " + Params.dbName + ".printer WHERE printer_id = " + func.getPrinterId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(!dbCon.rs.next()){
			throw new BusinessException(PrintSchemeError.PRINTER_NOT_EXIST);
		}
		dbCon.rs.close();
		
		//Delete the function has exist before.
		deleteByCond(dbCon, staff, new ExtraCond().setPrinter(func.getPrinterId()).setType(func.getType()));
		
		sql = " INSERT INTO " + Params.dbName + ".print_func" +
		      "( `printer_id`, `repeat`, `type`, `comment`, `enabled`, `extra`, `extra_str` )" +
			  " VALUES( " +
			  func.getPrinterId() + 
			  "," + func.getRepeat() + 
			  "," + func.getType().getVal() +  
			  "," + (func.hasComment() ? "'" + func.getComment() + "'" : " NULL ") + 
			  "," + (func.isEnabled() ? "1" : "0") +
			  "," + func.getExtra() +
			  "," + (func.hasExtraStr() ? "'" + func.getExtraStr() + "'" : " NULL ") + 
		      ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int funcId;
		if(dbCon.rs.next()){
			funcId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of print function is not generated successfully.");
		}	
		
		//Insert the department to this print function
		for(Department dept : func.getDepartment()){
			sql = " INSERT INTO " + Params.dbName + ".func_dept" +
				  "( func_id, dept_id, restaurant_id )" +
				  " VALUES(" +
				  funcId + "," +
				  dept.getId() + "," +
				  staff.getRestaurantId() + 
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}
		
		//Insert the kitchens to this print function
		for(Kitchen kitchen : func.getKitchens()){
			sql = " INSERT INTO " + Params.dbName + ".func_kitchen" +
				  "( func_id, kitchen_id, restaurant_id )" +
				  " VALUES( " +
				  funcId + "," +
				  kitchen.getId() + "," +
				  staff.getRestaurantId() + 
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}
		
		//Insert the regions to this print function
		for(Region region : func.getRegions()){
			sql = " INSERT INTO " + Params.dbName + ".func_region" +
				  "( func_id, region_id, restaurant_id )" +
				  " VALUES( " +
				  funcId + "," +
				  region.getId() + "," +
				  staff.getRestaurantId() +
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}
		
		return funcId;
	}
	
	/**
	 * Add a new summary print function to specific printer
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param func
	 * 			the function to add
	 * @return the id to extra print function just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the printer does NOT exist
	 * 			throws if the function type has exist before
	 */
	public static int addFunc(DBCon dbCon, Staff staff, PrintFunc.SummaryBuilder builder) throws SQLException, BusinessException{
		return addFunc(dbCon, staff, builder.build());
	}
	
	/**
	 * Add a new detail print function to specific printer
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param func
	 * 			the function to add
	 * @return the id to print function just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the printer does NOT exist
	 * 			throws if the function type has exist before
	 */
	public static int addFunc(DBCon dbCon, Staff staff, PrintFunc.DetailBuilder builder) throws SQLException, BusinessException{
		int funcId = 0;
		for(PrintFunc func : builder.build()){
			funcId = addFunc(dbCon, staff, func);
		}
		return funcId;
	}
	/**
	 * Add a new  print function to specific printer
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param func
	 * 			the function to add
	 * @return the id to print function just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the printer does NOT exist
	 * 			throws if the function type has exist before
	 */
	public static int addFunc(DBCon dbCon, Staff staff, PrintFunc.Builder builder) throws SQLException, BusinessException{
		return addFunc(dbCon, staff, builder.build());
	}
	
	/**
	 * Remove the specific print function according to function id.
	 * @param staff
	 * 			the terminal
	 * @param funcId
	 * 			the function id to remove
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the function to delete does NOT exist
	 */
	public static void deleteById(Staff staff, int funcId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			deleteById(dbCon, staff, funcId);
			
			dbCon.conn.commit();
		}finally{
			dbCon.conn.setAutoCommit(true);
			dbCon.disconnect();
		}
	}
	
	/**
	 * Remove the specific print function according to function id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param funcId
	 * 			the function id to remove
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the function to delete does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int funcId) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(funcId)) == 0){
			throw new BusinessException(PrintSchemeError.FUNC_TYPE_NOT_EXIST);
		}
	}
	
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(PrintFunc printFunc : getByCond(dbCon, staff, extraCond)){
			String sql;
			
			sql = " DELETE FROM " + Params.dbName + ".func_dept WHERE func_id = " + printFunc.getId();
			dbCon.stmt.executeUpdate(sql);
			
			sql = " DELETE FROM " + Params.dbName + ".func_kitchen WHERE func_id = " + printFunc.getId();
			dbCon.stmt.executeUpdate(sql);
			
			sql = " DELETE FROM " + Params.dbName + ".func_region WHERE func_id = " + printFunc.getId();
			dbCon.stmt.executeUpdate(sql);
			
			sql = " DELETE FROM " + Params.dbName + ".print_func WHERE func_id = " + printFunc.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
			//Delete the associated detail print function.
			if(printFunc.getType() == PType.PRINT_ORDER_DETAIL){
				amount += deleteByCond(dbCon, staff, new ExtraCond().setPrinter(printFunc.getPrinterId()).setType(PType.PRINT_CANCELLED_FOOD_DETAIL)); 
			}
		}
		return amount;
	}
	
	
	/**
	 * Update the summary print function according to specific builder {@link PrintFunc#SummaryUpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to summary print function
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 */
	public static void updateFunc(DBCon dbCon, Staff staff, PrintFunc.SummaryUpdateBuilder builder) throws SQLException, BusinessException{
		updateFunc(dbCon, staff, builder.build());
	}
	
	/**
	 * Update the detail print function according to specific builder {@link PrintFunc#DetailUpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to detail print function
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 */
	public static void updateFunc(DBCon dbCon, Staff staff, PrintFunc.DetailUpdateBuilder builder) throws SQLException, BusinessException{
		PrintFunc detailExtraFunc = getByCond(dbCon, staff, new ExtraCond().setPrinter(builder.getPrinterId()).setType(PType.PRINT_ORDER_DETAIL)).get(0);
		PrintFunc detailCancelFunc = getByCond(dbCon, staff, new ExtraCond().setPrinter(builder.getPrinterId()).setType(PType.PRINT_CANCELLED_FOOD_DETAIL)).get(0);
		for(PrintFunc.UpdateBuilder eachBuilder : builder.build(detailExtraFunc.getId(), detailCancelFunc.getId())){
			updateFunc(dbCon, staff, eachBuilder);
		}
	}
	
	/**
	 * Update the print function according to builder {@link PrintFucn#UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to print function
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the print function to update does NOT exist
	 */
	public static void updateFunc(DBCon dbCon, Staff staff, PrintFunc.UpdateBuilder builder) throws SQLException, BusinessException{
		String sql;
		
		PrintFunc func = builder.build();
		
		//Get the printer function id.
		final List<PrintFunc> associatedFuncs = getByCond(dbCon, staff, new ExtraCond().setPrinter(func.getPrinterId()).setType(func.getType()));
		if(associatedFuncs.isEmpty()){
			throw new BusinessException("修改失败, 【" + func.getType().getDesc() + "】的打印功能不存在");
		}else{
			func.setId(associatedFuncs.get(0).getId());
		}
		
		sql = " UPDATE " + Params.dbName + ".print_func SET " +
			  " func_id = " + func.getId() +
			  (builder.isRepeatChanged() ? " ,`repeat` = " + func.getRepeat() : "") +
			  (builder.isCommentChanged() ? " ,comment = '" + func.getComment() + "'" : "") +
			  (builder.isEnabledChanged() ? " ,enabled = " + (func.isEnabled() ? "1" : "0") : "") +
			  (builder.isExtraChanged() ? " ,extra = " + func.getExtra() : "") +
			  (builder.isExtraStrChanged() ? " ,extra_str = '" + func.getExtraStr() + "'" : "") +
			  " WHERE func_id = " +  func.getId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PrintSchemeError.PRINTER_NOT_EXIST);
		}
		
		//Update the department.
		if(builder.isDeptChanged()){
			sql = " DELETE FROM " + Params.dbName + ".func_dept WHERE func_id = " + func.getId();
			dbCon.stmt.executeUpdate(sql);
			
			for(Department dept : func.getDepartment()){
				sql = " INSERT INTO " + Params.dbName + ".func_dept ( func_id, dept_id, restaurant_id ) VALUES ( " +
					  func.getId() + "," +
					  dept.getId() + "," +
					  staff.getRestaurantId() + 
					  ")";
				dbCon.stmt.executeUpdate(sql);
			}
		}
		
		//Update the kitchen.
		if(builder.isKitchenChanged()){
			sql = " DELETE FROM " + Params.dbName + ".func_kitchen WHERE func_id = " + func.getId();
			dbCon.stmt.executeUpdate(sql);
			
			for(Kitchen kitchen : func.getKitchens()){
				sql = " INSERT INTO " + Params.dbName + ".func_kitchen ( func_id, kitchen_id, restaurant_id ) VALUES( " +
					  func.getId() + "," +
					  kitchen.getId() + "," +
					  staff.getRestaurantId() + 
					  ")";
				dbCon.stmt.executeUpdate(sql);
			}
		}
		
		//Update the regions.
		if(builder.isRegionChanged()){
			sql = " DELETE FROM " + Params.dbName + ".func_region WHERE func_id = " + func.getId();
			dbCon.stmt.executeUpdate(sql);
			
			for(Region region : func.getRegions()){
				sql = " INSERT INTO " + Params.dbName + ".func_region ( func_id, region_id, restaurant_id ) VALUES( " +
					  func.getId() + "," +
					  region.getId() + "," +
					  staff.getRestaurantId() +
					  ")";
				dbCon.stmt.executeUpdate(sql);
			}
		}
		
	}
	

	/**
	 * Get the printer function to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param funcId
	 * 			the id to printer function
	 * @return the printer function to this specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the printer function to this specific id does NOT exist
	 */
	public static PrintFunc getById(DBCon dbCon, Staff staff, int funcId) throws SQLException, BusinessException{
		List<PrintFunc> result = getByCond(dbCon, staff, new ExtraCond().setId(funcId));
		if(result.isEmpty()){
			throw new BusinessException(PrintSchemeError.PRINTER_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}

	/**
	 * Get the printer function to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the printer functions to this extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PrintFunc> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the printer function to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the printer functions to this extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PrintFunc> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		
		sql = " SELECT * FROM " + 
			  Params.dbName + ".print_func WHERE 1 = 1 " + 
			  (extraCond != null ? extraCond.toString() : "") +
			  " ORDER BY type ";

		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		final List<PrintFunc> result = new ArrayList<PrintFunc>();
		while(dbCon.rs.next()){
			PrintFunc func = new PrintFunc(PType.valueOf(dbCon.rs.getInt("type")), dbCon.rs.getInt("repeat"));
			func.setId(dbCon.rs.getInt("func_id"));
			func.setPrinterId(dbCon.rs.getInt("printer_id"));
			func.setComment(dbCon.rs.getString("comment"));
			func.setEnabled(dbCon.rs.getBoolean("enabled"));
			func.setExtra(dbCon.rs.getInt("extra"));
			result.add(func);
		}
		dbCon.rs.close();
		
		for(PrintFunc func : result){
			//Get the department to this function
			sql = " SELECT DEPT.name, DEPT.dept_id, DEPT.restaurant_id " +
				  " FROM " + Params.dbName + ".func_dept FD" + 
				  " JOIN " + Params.dbName + ".department DEPT ON FD.dept_id = DEPT.dept_id AND FD.restaurant_id = DEPT.restaurant_id " +
				  " WHERE FD.func_id = " + func.getId();
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				func.addDepartment(new Department(dbCon.rs.getInt("restaurant_id"),
												  dbCon.rs.getShort("dept_id"),
												  dbCon.rs.getString("name")));
			}
			dbCon.rs.close();
			
			//Get the kitchens to this function
			sql = " SELECT K.kitchen_id, K.display_id, K.name, K.restaurant_id "	+
				  " FROM " + Params.dbName + ".func_kitchen FK " + 
				  " JOIN " + Params.dbName + ".kitchen K ON FK.kitchen_id = K.kitchen_id " +
				  " WHERE FK.func_id = " + func.getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Kitchen kitchenToAdd = new Kitchen(dbCon.rs.getInt("kitchen_id"));
				kitchenToAdd.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				kitchenToAdd.setDisplayId(dbCon.rs.getInt("display_id"));
				kitchenToAdd.setName(dbCon.rs.getString("name"));
				func.addKitchen(kitchenToAdd);
			}
			dbCon.rs.close();
			
			//Get the regions to this function
			sql = " SELECT R.region_id, R.name, R.restaurant_id " +
				  " FROM " + Params.dbName + ".func_region FR " + 
				  " JOIN " + Params.dbName + ".region R ON FR.region_id = R.region_id AND FR.restaurant_id = R.restaurant_id " +
			      " WHERE FR.func_id = " + func.getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Region regionToAdd = new Region(dbCon.rs.getShort("region_id"));
				regionToAdd.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				regionToAdd.setName(dbCon.rs.getString("name"));
				func.addRegion(regionToAdd);
			}
			dbCon.rs.close();
		}
	
		return result;
	}
	
	/**
	 * Get the print functions to the specific printer 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param printerId
	 * 			the id to printer
	 * @return the print functions to this printer
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PrintFunc> getByPrinter(DBCon dbCon, Staff staff, Printer printer) throws SQLException{
		return getByCond(dbCon, staff, new ExtraCond().setPrinter(printer).setEnabled(true));
	}
}
