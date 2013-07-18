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
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PrintFunc;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.protocol.Terminal;

public class PrinterDao {

	/**
	 * Insert a new printer
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the builder to new printer
	 * @return the id to printer just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the printer with the same name has exist before
	 */
	public static int insert(DBCon dbCon, Terminal term, Printer.InsertBuilder builder) throws SQLException, BusinessException{
		
		Printer printerToAdd = builder.build();
		
		String sql;
		
		//Check to see whether the printer with the name exist before
		sql = " SELECT * FROM " + Params.dbName + ".printer" +
		      " WHERE restaurant_id = " + printerToAdd.getRestaurantId() +
		      " AND name = '" + printerToAdd.getName() + "'";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(PrintSchemeError.DUPLICATE_PRINTER);
		}
		dbCon.rs.close();
		
		//Insert a new printer
		sql = " INSERT INTO " + Params.dbName + ".printer " +
		      " (restaurant_id, name, alias, style ) " +
			  " VALUES ( " +
		      printerToAdd.getRestaurantId() + "," +
			  "'" + printerToAdd.getName() + "'," +
		      "'" + printerToAdd.getAlias() + "'," +
			  printerToAdd.getStyle().getVal() +
		      ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of printer is not generated successfully.");
		}	
	}
	
	/**
	 * Update a specific printer.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the builder to update printer
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the name of printer to update has exist before
	 * 			throws if the printer to update does NOT exist
	 */
	public static void update(DBCon dbCon, Terminal term, Printer.UpdateBuilder builder) throws SQLException, BusinessException{
		
		Printer printerToUpdate = builder.build();
		
		String sql;
		
		//Check to see whether the printer with the name exist before
		sql = " SELECT * FROM " + Params.dbName + ".printer" +
		      " WHERE restaurant_id = " + printerToUpdate.getRestaurantId() +
		      " AND name = '" + printerToUpdate.getName() + "'";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(PrintSchemeError.DUPLICATE_PRINTER);
		}
		dbCon.rs.close();
		
		sql = " UPDATE " + Params.dbName + ".printer SET " +
			  " name = " + "'" + printerToUpdate.getName() + "'" + 
			  " ,alias = " + "'" + printerToUpdate.getAlias() + "'" +
			  " ,style = " + printerToUpdate.getStyle().getVal() + 
			  " WHERE 1 = 1 " +
			  " AND printer_id = " + printerToUpdate.getId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PrintSchemeError.PRINTER_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the printer and its associated print functions according to the specified id.
	 * @param dbCon
	 * 			the database connection
	 * @param terminal
	 * 			the terminal
	 * @param printerId
	 * 			the printer id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the printer to delete does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Terminal term, int printerId) throws SQLException, BusinessException{
		String sql;
		
		sql = " SELECT func_id FROM " + Params.dbName + ".print_func" +
		      " WHERE printer_id = " + printerId;
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<Integer> funcIdToRemove = new ArrayList<Integer>();
		while(dbCon.rs.next()){
			//Remove each print function
			funcIdToRemove.add(dbCon.rs.getInt("func_id"));
		}
		dbCon.rs.close();
		
		for(int funcId : funcIdToRemove){
			PrintFuncDao.removeFunc(dbCon, term, funcId);
		}
		
		sql = " DELETE FROM " + Params.dbName + ".printer WHERE printer_id = " + printerId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PrintSchemeError.PRINTER_NOT_EXIST);
		}
	}
	
	/**
	 * Get the printer along with associated print functions to a specified restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @return the printer to this specific restaurant
	 * @throws SQLException
	 * 			throws if the printer to delete does NOT exist
	 */
	public static List<Printer> getPrinters(DBCon dbCon, Terminal term) throws SQLException{
		
		String sql;
		sql = " SELECT printer_id, restaurant_id, name, alias, style FROM " + Params.dbName + ".printer WHERE restaurant_id = " + term.restaurantID;

		List<Printer> result = new ArrayList<Printer>();
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Printer printer = new Printer(dbCon.rs.getString("name"), 
										  PStyle.valueOf(dbCon.rs.getInt("style")),
										  dbCon.rs.getInt("restaurant_id"));
			printer.setId(dbCon.rs.getInt("printer_id"));
			printer.setAlias(dbCon.rs.getString("alias"));
			result.add(printer);
		}
		dbCon.rs.close();
		
		for(Printer printer : result){
			for(PrintFunc func : PrintFuncDao.getFuncByPrinterId(dbCon, printer.getId())){
				try{
					printer.addFunc(func);
				}catch(BusinessException ignored){}
			}
		}
		
		return Collections.unmodifiableList(result);
	}
}
