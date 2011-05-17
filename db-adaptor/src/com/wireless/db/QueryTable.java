package com.wireless.db;


import java.sql.SQLException;
import java.util.ArrayList;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class QueryTable {
	
	/**
	 * Get all the table information to restaurant that the terminal is attached to. 
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @return An array holding all the table information
	 * @throws BusinessException throws if the terminal is NOT attached to any restaurant
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Table[] exec(int pin, short model) throws BusinessException, SQLException{
		
		Terminal terminal = VerifyPin.exec(pin, model);

		DBCon dbCon = new DBCon();

		try {   
			
			dbCon.connect();			
	
			ArrayList<Table> tables = new ArrayList<Table>();
			
			//get the idle tables
			String sql = "SELECT table_id FROM " + Params.dbName + ".order a WHERE id = (SELECT max(id) FROM " + 
						Params.dbName + ".order b WHERE a.table_id = b.table_id AND a.restaurant_id=b.restaurant_id) " +
						"AND total_price IS NOT NULL AND restaurant_id=" + terminal.restaurant_id;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Table idleTable = new Table();
				idleTable.alias_id = dbCon.rs.getShort(1);
				idleTable.status = Table.TABLE_IDLE;
				tables.add(idleTable);
			}
			dbCon.rs.close();
			
			sql = "SELECT alias_id FROM " + Params.dbName + ".table WHERE alias_id NOT IN (SELECT distinct table_id FROM " +
					Params.dbName + ".order WHERE restaurant_id=" + terminal.restaurant_id + ")" + " AND restaurant_id=" + terminal.restaurant_id;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Table idleTable = new Table();
				idleTable.alias_id = dbCon.rs.getShort(1);
				tables.add(idleTable);
			}
			dbCon.rs.close();
			
			//get the busy tables
			sql = "SELECT table_id, custom_num FROM " + Params.dbName + ".order a WHERE id=(SELECT max(id) FROM " + Params.dbName + 
				".order b WHERE a.table_id = b.table_id and a.restaurant_id=b.restaurant_id) and total_price IS NULL AND restaurant_id="+
				terminal.restaurant_id;
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Table busyTable = new Table();
				busyTable.alias_id = dbCon.rs.getShort("table_id");
				busyTable.custom_num = dbCon.rs.getShort("custom_num");
				busyTable.status = Table.TABLE_BUSY;
				tables.add(busyTable);
			}			

			return tables.toArray(new Table[tables.size()]);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the table information according to specific alias id.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param tableID the table alias id to query
	 * @return the table information to specific alias id
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The table alias id to query does NOT exist.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Table exec(int pin, short model, short tableID) throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(pin, model);
		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			/**
			 * Check to see if the table with this alias id is exist or not.
			 */
			String sql = "SELECT id, enabled FROM `" + Params.dbName +
						"`.`table` WHERE alias_id=" + tableID + 
						" AND restaurant_id=" + term.restaurant_id + " AND enabled=1";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				/**
				 * Check to see if the table with this alias id is idle or busy
				 */
				 sql = "SELECT custom_num FROM `" + Params.dbName + 
					   "`.`order` WHERE table_id = " + tableID +
					   " AND restaurant_id = " + term.restaurant_id +
					   " AND total_price IS NULL";
				 dbCon.rs = dbCon.stmt.executeQuery(sql);
				 Table table = new Table();
				 table.alias_id = tableID;
				 
				 if(dbCon.rs.next()){
					 table.custom_num = dbCon.rs.getByte("custom_num");
					 table.status = Table.TABLE_BUSY;
				 }else{
					 table.status = Table.TABLE_IDLE;
				 }
				 
				 return table;
				 
			}else{
				throw new BusinessException("The table(alias_id=" + tableID + ") to query does NOT exist.", ErrorCode.TABLE_NOT_EXIST);
			}
			
		}finally{
			dbCon.disconnect();
		}
	}
	
}
