package com.wireless.db.printScheme;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;

public class PrintFuncDao {
	
	/**
	 * Add a new print function to specific printer
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param printerId
	 * 			the printer to add function
	 * @param func
	 * 			the function to add
	 * @return the id to print function just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the printer does NOT exist
	 * 			throws if the function type has exist before
	 */
	private static int addFunc(DBCon dbCon, Staff term, int printerId, PrintFunc func) throws SQLException, BusinessException{
		
		String sql;
		
		//Check to see whether the printer is exist
		sql = " SELECT * FROM " + Params.dbName + ".printer WHERE printer_id = " + printerId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(!dbCon.rs.next()){
			throw new BusinessException(PrintSchemeError.PRINTER_NOT_EXIST);
		}
		dbCon.rs.close();
		
		//Check to see whether the function has exist before
		sql = " SELECT * FROM " + Params.dbName + ".print_func WHERE " +
			  " printer_id = " + printerId + " AND " + " type = " + func.getType().getVal();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(PrintSchemeError.DUPLICATE_FUNC_TYPE);
		}
		dbCon.rs.close();
		
		sql = " INSERT INTO " + Params.dbName + ".print_func" +
		      "( `printer_id`, `repeat`, `type` )" +
			  " VALUES( " +
		      printerId + "," +
			  func.getRepeat() + "," +
		      func.getType().getVal() +
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
		if(!func.isDeptAll()){
			sql = " INSERT INTO " + Params.dbName + ".func_dept" +
				  "( func_id, dept_id, restaurant_id )" +
				  " VALUES(" +
				  funcId + "," +
				  func.getDepartment().getId() + "," +
				  term.getRestaurantId() + 
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}
		
		//Insert the kitchens to this print function
		for(Kitchen kitchen : func.getKitchens()){
			sql = " INSERT INTO " + Params.dbName + ".func_kitchen" +
				  "( func_id, kitchen_alias, restaurant_id )" +
				  " VALUES( " +
				  funcId + "," +
				  kitchen.getAliasId() + "," +
				  term.getRestaurantId() + 
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}
		
		//Insert the regions to this print function
		for(Region region : func.getRegions()){
			sql = " INSERT INTO " + Params.dbName + ".func_region" +
				  "( func_id, region_id, restaurant_id )" +
				  " VALUES( " +
				  funcId + "," +
				  region.getRegionId() + "," +
				  term.getRestaurantId() +
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}
		
		return funcId;
	}
	
	/**
	 * Add a new summary print function to specific printer
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param printerId
	 * 			the printer to add function
	 * @param func
	 * 			the function to add
	 * @return the id to print function just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the printer does NOT exist
	 * 			throws if the function type has exist before
	 */
	public static int addFunc(DBCon dbCon, Staff term, int printerId, PrintFunc.SummaryBuilder builder) throws SQLException, BusinessException{
		return addFunc(dbCon, term, printerId, builder.build());
	}
	
	/**
	 * Add a new detail print function to specific printer
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param printerId
	 * 			the printer to add function
	 * @param func
	 * 			the function to add
	 * @return the id to print function just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the printer does NOT exist
	 * 			throws if the function type has exist before
	 */
	public static int addFunc(DBCon dbCon, Staff term, int printerId, PrintFunc.DetailBuilder builder) throws SQLException, BusinessException{
		return addFunc(dbCon, term, printerId, builder.build());
	}
	/**
	 * Add a new  print function to specific printer
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param printerId
	 * 			the printer to add function
	 * @param func
	 * 			the function to add
	 * @return the id to print function just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the printer does NOT exist
	 * 			throws if the function type has exist before
	 */
	public static int addFunc(DBCon dbCon, Staff term, int printerId, PrintFunc.Builder builder) throws SQLException, BusinessException{
		return addFunc(dbCon, term, printerId, builder.build());
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
	public static void removeFunc(Staff staff, int funcId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			removeFunc(dbCon, staff, funcId);
			
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
	public static void removeFunc(DBCon dbCon, Staff staff, int funcId) throws SQLException, BusinessException{
		String sql;
		
		sql = " DELETE FROM " + Params.dbName + ".func_dept WHERE func_id = " + funcId;
		dbCon.stmt.executeUpdate(sql);
		
		sql = " DELETE FROM " + Params.dbName + ".func_kitchen WHERE func_id = " + funcId;
		dbCon.stmt.executeUpdate(sql);
		
		sql = " DELETE FROM " + Params.dbName + ".func_region WHERE func_id = " + funcId;
		dbCon.stmt.executeUpdate(sql);
		
		sql = " DELETE FROM " + Params.dbName + ".print_func WHERE func_id = " + funcId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PrintSchemeError.FUNC_TYPE_NOT_EXIST);
		}
	}
	
	public static void updateFunc(DBCon dbCon, Staff term, int printerId, PrintFunc func, int funcId) throws SQLException, BusinessException{
		String sql;
		
		//Check to see whether the printer is exist
		sql = " SELECT * FROM " + Params.dbName + ".printer WHERE printer_id = " + printerId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(!dbCon.rs.next()){
			throw new BusinessException(PrintSchemeError.PRINTER_NOT_EXIST);
		}
		dbCon.rs.close();
		
		sql = "UPDATE " + Params.dbName + ".print_func SET " +
				" `repeat` = " + func.getRepeat() +
				" ,type = " + func.getType().getVal() +
				" WHERE func_id = " +  funcId;
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		
		sql = " DELETE FROM " + Params.dbName + ".func_dept WHERE func_id = " + funcId;
		dbCon.stmt.executeUpdate(sql);
		
		sql = " DELETE FROM " + Params.dbName + ".func_kitchen WHERE func_id = " + funcId;
		dbCon.stmt.executeUpdate(sql);
		
		sql = " DELETE FROM " + Params.dbName + ".func_region WHERE func_id = " + funcId;
		dbCon.stmt.executeUpdate(sql);
		
		//Insert the department to this print function
		if(!func.isDeptAll()){
			sql = " INSERT INTO " + Params.dbName + ".func_dept" +
				  "( func_id, dept_id, restaurant_id )" +
				  " VALUES(" +
				  funcId + "," +
				  func.getDepartment().getId() + "," +
				  term.getRestaurantId() + 
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}
		
		//Insert the kitchens to this print function
		for(Kitchen kitchen : func.getKitchens()){
			sql = " INSERT INTO " + Params.dbName + ".func_kitchen" +
				  "( func_id, kitchen_alias, restaurant_id )" +
				  " VALUES( " +
				  funcId + "," +
				  kitchen.getAliasId() + "," +
				  term.getRestaurantId() + 
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}
		
		//Insert the regions to this print function
		for(Region region : func.getRegions()){
			sql = " INSERT INTO " + Params.dbName + ".func_region" +
				  "( func_id, region_id, restaurant_id )" +
				  " VALUES( " +
				  funcId + "," +
				  region.getRegionId() + "," +
				  term.getRestaurantId() +
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}
		
		
	}
	
	/**
	 * Get the print functions to the specific printer 
	 * @param printerId
	 * 			the id to printer
	 * @return the print functions to this printer
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PrintFunc> getFuncByPrinterId(int printerId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getFuncByPrinterId(dbCon, printerId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the print functions to the specific printer 
	 * @param dbCon
	 * 			the database connection
	 * @param printerId
	 * 			the id to printer
	 * @return the print functions to this printer
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<PrintFunc> getFuncByPrinterId(DBCon dbCon, int printerId) throws SQLException{
		String sql;
		
		sql = " SELECT func_id, `repeat`, `type` FROM " + Params.dbName + ".print_func WHERE printer_id = " + printerId + " ORDER BY type ";

		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<PrintFunc> result = new ArrayList<PrintFunc>();
		while(dbCon.rs.next()){
			PrintFunc func = new PrintFunc(PType.valueOf(dbCon.rs.getInt("type")), dbCon.rs.getInt("repeat"));
			func.setId(dbCon.rs.getInt("func_id"));
			result.add(func);
		}
		dbCon.rs.close();
		
		for(PrintFunc func : result){
			//Get the department to this function
			sql = " SELECT DEPT.name, DEPT.dept_id, DEPT.restaurant_id FROM " +
				  Params.dbName + ".func_dept FD" + " JOIN " +
				  Params.dbName + ".department DEPT" + " ON " + 
				  " FD.dept_id = DEPT.dept_id AND FD.restaurant_id = DEPT.restaurant_id " +
				  " WHERE FD.func_id = " + func.getId();
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				func.setDepartment(new Department(dbCon.rs.getInt("restaurant_id"),
												  dbCon.rs.getShort("dept_id"),
												  dbCon.rs.getString("name")));
			}
			dbCon.rs.close();
			
			//Get the kitchens to this function
			sql = " SELECT KITCHEN.kitchen_id, KITCHEN.kitchen_alias, KITCHEN.name, KITCHEN.restaurant_id FROM " +
				  Params.dbName + ".func_kitchen FK " + " JOIN " +
				  Params.dbName + ".kitchen KITCHEN " + " ON " +
				  " FK.kitchen_alias = KITCHEN.kitchen_alias AND FK.restaurant_id = KITCHEN.restaurant_id " +
				  " WHERE FK.func_id = " + func.getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Kitchen kitchenToAdd = new Kitchen();
				kitchenToAdd.setId(dbCon.rs.getLong("kitchen_id"));
				kitchenToAdd.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				kitchenToAdd.setAliasId(dbCon.rs.getShort("kitchen_alias"));
				kitchenToAdd.setName(dbCon.rs.getString("name"));
				func.addKitchen(kitchenToAdd);
			}
			dbCon.rs.close();
			
			//Get the regions to this function
			sql = " SELECT REGION.region_id, REGION.name, REGION.restaurant_id FROM " +
				  Params.dbName + ".func_region FR " + " JOIN " +
			      Params.dbName + ".region REGION " + " ON " +
				  " FR.region_id = REGION.region_id AND FR.restaurant_id = REGION.restaurant_id " +
			      " WHERE FR.func_id = " + func.getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Region regionToAdd = new Region();
				regionToAdd.setRegionId(dbCon.rs.getShort("region_id"));
				regionToAdd.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				regionToAdd.setName(dbCon.rs.getString("name"));
				func.addRegion(regionToAdd);
			}
			dbCon.rs.close();
		}
	
		return Collections.unmodifiableList(result);
	}
}
