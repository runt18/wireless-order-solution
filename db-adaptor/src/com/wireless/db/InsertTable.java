package com.wireless.db;

import java.sql.SQLException;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class InsertTable {
	
	/**
	 * Insert a new table record to database.
	 * @param pin
	 *            the pin to terminal
	 * @param model
	 *            the model to terminal
	 * @param table
	 * 			the table to be inserted
	 * @param autoGenID
	 * 			indicating generate table alias id automatically or using the Table parameter
	 * @return
	 * 			the id generated by the database to this table record
	 * @throws BusinessException
	 * 			throws if either of cases below.<br>
 	 *	 		- The terminal is NOT attached with any restaurant.<br>
	 *			- The terminal is expired.<br>
	 * 			- The table id to be inserted has exist before.<br>
	 * 			- Fail to generate the id for this table record.
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static Table exec(long pin, short model, Table table, boolean autoGenID) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, pin, model, table, autoGenID);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new table record to database.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param pin
	 *            the pin to terminal
	 * @param model
	 *            the model to terminal
	 * @param table
	 * 			the table to be inserted
	 * @param autoGenID
	 * 			indicating generate table alias id automatically or using the Table parameter
	 * @return Table
	 * 			the new table generated by the database to this table record
	 * @throws BusinessException
	 * 			throws if either of cases below.<br>
	 * 			- The terminal is NOT attached with any restaurant.<br>
	 *			- The terminal is expired.<br>
	 * 			- The table id to be inserted has exist before.<br>
	 * 			- Fail to generate the id for this table record.
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static Table exec(DBCon dbCon, long pin, short model, Table table, boolean autoGenID) throws BusinessException, SQLException{
		return exec(dbCon, VerifyPin.exec(dbCon, pin, model), table, autoGenID);		
	}
	
	public static Table exec(DBCon dbCon, Terminal term, Table table, boolean autoGenID) throws BusinessException, SQLException{
		
		Table newTbl = new Table();
		newTbl.alias_id = table.alias_id;
		newTbl.name = table.name;
		newTbl.restaurantID = term.restaurant_id;
		newTbl.status = Table.TABLE_IDLE;
		newTbl.category = Order.CATE_NORMAL;
		
		String sql;
		if(autoGenID){
			
			sql = "SELECT MAX(table_alias) + 1 FROM " + Params.dbName + ".table WHERE restaurant_id=" + term.restaurant_id;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				newTbl.alias_id = dbCon.rs.getInt(1);
			}else{
				throw new BusinessException("Fail to generate the table alias id.");
			}
			dbCon.rs.close();
			
		}else{
			sql = "SELECT id FROM " + Params.dbName + 
				  ".table WHERE " +
				  "restaurant_id=" + term.restaurant_id + 
				  " AND " +
				  "table_alias=" + newTbl.alias_id;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				throw new BusinessException("Table(alias_id=" + newTbl.alias_id + ", restaurant_id=" + term.restaurant_id + ") is exist.", ErrorCode.TABLE_EXIST);
			}	
			dbCon.rs.close();
		}

		/**
		 * If set to auto generate table alias id, the alias id would be set to current maximum id value plus one.
		 * Otherwise, set the alias id using the parameter pass into the function. 
		 */
		sql = "INSERT INTO " + Params.dbName + 
			  ".table (`table_id`, `table_alias`, `restaurant_id`, `name`, `category`, `custom_num`, `status`) VALUES( " +
			  "NULL, " +
			  newTbl.alias_id + ", " +
			  term.restaurant_id + ", '" +
			  (newTbl.name != null ? newTbl.name : "") + "', " + 
			  "NULL, " +
			  "NULL, " +
			  Table.TABLE_IDLE +
			  ")";
		
		dbCon.stmt.execute(sql);
		
		return newTbl;
	}
	
}
