package com.wireless.db.printScheme;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PrintSchemeError;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.staffMgr.Staff;

public class PrinterDao {

	public static class ExtraCond{
		private int id;
		private String name;
		private Boolean isEnabled;
		private Printer.Oriented oriented;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setName(String name){
			this.name = name;
			return this;
		}
		
		public ExtraCond setEnabled(boolean onOff){
			this.isEnabled = onOff;
			return this;
		}
		
		public ExtraCond setOriented(Printer.Oriented oriented){
			this.oriented = oriented;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND printer_id = " + id);
			}
			if(name != null){
				extraCond.append(" AND name = '" + name + "'");
			}
			if(isEnabled != null){
				extraCond.append(" AND enabled = " + (isEnabled ? 1 : 0));
			}
			if(oriented != null){
				extraCond.append(" AND oriented = " + oriented.getVal());
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert a new printer
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param builder
	 * 			the builder to new printer
	 * @return the id to printer just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the printer with the same name has exist before
	 */
	public static int insert(DBCon dbCon, Staff staff, Printer.InsertBuilder builder) throws SQLException, BusinessException{
		
		Printer printerToAdd = builder.build();
		
		String sql;
		
		//Check to see whether the printer with the name exist before
		sql = " SELECT * FROM " + Params.dbName + ".printer" +
		      " WHERE restaurant_id = " + staff.getRestaurantId() +
		      " AND name = '" + printerToAdd.getName() + "'";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(PrintSchemeError.DUPLICATE_PRINTER);
		}
		dbCon.rs.close();
		
		//Insert a new printer
		sql = " INSERT INTO " + Params.dbName + ".printer " +
		      " ( restaurant_id, name, alias, style, enabled, oriented ) " +
			  " VALUES ( " +
		      staff.getRestaurantId() + "," +
			  "'" + printerToAdd.getName() + "'," +
		      "'" + printerToAdd.getAlias() + "'," +
			  printerToAdd.getStyle().getVal() + "," +
		      (printerToAdd.isEnabled() ? 1 : 0) + "," +
			  printerToAdd.getOriented().getVal() +
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
	 * @param staff
	 * 			the terminal
	 * @param builder
	 * 			the builder to update printer
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the name of printer to update has exist before
	 * 			throws if the printer to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, Printer.UpdateBuilder builder) throws SQLException, BusinessException{
		
		Printer printerToUpdate = builder.build();
		
		String sql;
		
		sql = " UPDATE " + Params.dbName + ".printer SET " +
			  " printer_id = " + printerToUpdate.getId() +
			  (builder.isNameChanged() ? " ,name = " + "'" + printerToUpdate.getName() + "'" : "") + 
			  (builder.isAliasChanged() ? " ,alias = " + "'" + printerToUpdate.getAlias() + "'" : "") +
			  (builder.isStyleChanged() ? " ,style = " + printerToUpdate.getStyle().getVal() : "") + 
			  (builder.isEnabledChanged() ? " ,enabled = " + (printerToUpdate.isEnabled() ? 1 : 0) : "") +
			  (builder.isOrientedChanged() ? " ,oriented = " + printerToUpdate.getOriented().getVal() : "") +
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
	public static void deleteById(DBCon dbCon, Staff staff, int printerId) throws SQLException, BusinessException{
		
		//Delete the associated printer function.
		PrintFuncDao.deleteByCond(dbCon, staff, new PrintFuncDao.ExtraCond().setPrinter(printerId));
		
		String sql = " DELETE FROM " + Params.dbName + ".printer WHERE printer_id = " + printerId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(PrintSchemeError.PRINTER_NOT_EXIST);
		}
	}
	
	/**
	 * Get the printers along with associated print functions to a specified id
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param printerId
	 * 			the printer id to get
	 * @return the printer to this specific restaurant
	 * @throws SQLException
	 * 			throws if the printer to delete does NOT exist
	 * @throws BusinessException
	 * 			throws if the printer to find does NOT exist
	 */
	public static Printer getById(DBCon dbCon, Staff staff, int printerId) throws SQLException, BusinessException{
		List<Printer> result = getByCond(dbCon, staff, new ExtraCond().setId(printerId));
		if(result.isEmpty()){
			throw new BusinessException(PrintSchemeError.PRINTER_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the printer to extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result to printers
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Printer> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the printer to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result to printers
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Printer> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT printer_id, restaurant_id, name, alias, style, enabled, oriented FROM " + Params.dbName + ".printer " +
			  " WHERE restaurant_id = " + staff.getRestaurantId() + " " +
			  (extraCond != null ? extraCond : "") +
			  " ORDER BY enabled DESC, CONVERT(`name` USING GBK) ASC ";

		final List<Printer> result = new ArrayList<Printer>();
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Printer printer = new Printer(dbCon.rs.getInt("printer_id"));
			printer.setName(dbCon.rs.getString("name"));
			printer.setStyle(PStyle.valueOf(dbCon.rs.getInt("style")));
			printer.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			printer.setEnabled(dbCon.rs.getBoolean("enabled"));
			printer.setAlias(dbCon.rs.getString("alias"));
			printer.setOriented(Printer.Oriented.valueOf(dbCon.rs.getInt("oriented")));
			result.add(printer);
		}
		dbCon.rs.close();
		
		for(Printer printer : result){
			printer.setFuncs(PrintFuncDao.getByPrinter(dbCon, staff, printer));
		}
		
		return result;
	}
}
