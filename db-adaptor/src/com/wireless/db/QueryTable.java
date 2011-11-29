package com.wireless.db;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
		
		

		DBCon dbCon = new DBCon();

		try {   
			
			dbCon.connect();			
			
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			
			ArrayList<Table> tables = new ArrayList<Table>();
			
			//get the idle tables
			String sql = "SELECT alias_id AS table_id, name AS table_name, minimum_cost FROM " + Params.dbName + 
						 ".table a WHERE NOT EXISTS (SELECT id FROM " + Params.dbName + 
						 ".order b WHERE (a.alias_id = b.table_id OR a.alias_id=b.table2_id) AND b.restaurant_id=a.restaurant_id AND total_price IS NULL)" +
						 " AND restaurant_id=" + term.restaurant_id;
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Table idleTable = new Table();
				idleTable.restaurant_id = term.restaurant_id;
				idleTable.alias_id = dbCon.rs.getInt("table_id");
				idleTable.name = dbCon.rs.getString("table_name");
				idleTable.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
				idleTable.status = Table.TABLE_IDLE;
				tables.add(idleTable);
			}
			dbCon.rs.close();
			
			//get the busy tables
			sql = "SELECT a.*, o.custom_num, o.category FROM " + Params.dbName +
				  ".`table` a  INNER JOIN " + Params.dbName +
				  ".`order` o ON o.restaurant_id = a.restaurant_id AND o.total_price IS NULL AND (a.alias_id=o.table_id OR a.alias_id=table2_id)" +
				  " WHERE a.restaurant_id=" + term.restaurant_id;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Table busyTable = new Table();
				busyTable.restaurant_id = term.restaurant_id;
				busyTable.alias_id = dbCon.rs.getInt("alias_id");
				busyTable.name = dbCon.rs.getString("name");
				busyTable.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
				busyTable.custom_num = dbCon.rs.getShort("custom_num");
				busyTable.category = dbCon.rs.getShort("category");
				busyTable.status = Table.TABLE_BUSY;
				tables.add(busyTable);
			}			

			Collections.sort(tables, new Comparator<Table>(){

				public int compare(Table table1, Table table2) {
					// TODO Auto-generated method stub
					if(table1.alias_id > table2.alias_id){
						return 1;
					}else if(table1.alias_id < table2.alias_id){
						return -1;
					}else{
						return 0;
					}
				}
				
			});
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
	public static Table exec(int pin, short model, int tableID) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();		
		
		try{
			dbCon.connect();

			return exec(dbCon, pin, model, tableID);
			
		}finally{
			dbCon.disconnect();
		}
		
	}
	
	/**
	 * Get the table information according the specific table alias id and restaurant id.
	 * Assure the terminal with this pin is NOT expired before invoking this method.
	 * Note that the database should be connected before invoking this method
	 * @param dbCon the database connection
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param tableID the table alias id to query
	 * @return the table information
	 * @throws BusinessException throws if the table to query does NOT exist
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Table exec(DBCon dbCon, int pin, short model, int tableID) throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(dbCon, pin, model);
		
		/**
		 * Check to see if the table with this alias id is exist or not.
		 */
		String sql = "SELECT name, minimum_cost FROM `" + Params.dbName +
					"`.`table` WHERE alias_id=" + tableID + 
					" AND restaurant_id=" + term.restaurant_id;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			String tableName = dbCon.rs.getString("name");
			float minimumCost = dbCon.rs.getFloat("minimum_cost");
			/**
			 * Check to see if the table with this alias id is idle or busy
			 */
			 sql = "SELECT custom_num, category FROM `" + Params.dbName + 
				   "`.`order` WHERE (table_id = " + tableID +
				   " OR table2_id = " + tableID + ")" + 
				   " AND restaurant_id = " + term.restaurant_id +
				   " AND total_price IS NULL";
			 dbCon.rs = dbCon.stmt.executeQuery(sql);
			 Table table = new Table();
			 table.restaurant_id = term.restaurant_id;
			 table.alias_id = tableID;
			 table.name = tableName;
			 table.setMinimumCost(minimumCost);
			 
			 if(dbCon.rs.next()){
				 table.custom_num = dbCon.rs.getByte("custom_num");
				 table.category = dbCon.rs.getShort("category");
				 table.status = Table.TABLE_BUSY;
			 }else{
				 table.status = Table.TABLE_IDLE;
			 }
			 
			 dbCon.rs.close();
			 return table;
				 
		}else{
			throw new BusinessException("The table(alias_id=" + tableID + ") to query does NOT exist.", ErrorCode.TABLE_NOT_EXIST);
		}		

	}
	
}

